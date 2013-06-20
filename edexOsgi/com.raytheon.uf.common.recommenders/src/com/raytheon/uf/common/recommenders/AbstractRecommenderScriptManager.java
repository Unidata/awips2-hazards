/**
 * This software was developed and / or modified by Raytheon Company,
 * pursuant to Contract DG133W-05-CQ-1067 with the US Government.
 * 
 * U.S. EXPORT CONTROLLED TECHNICAL DATA
 * This software product contains export-restricted data whose
 * export/transfer/disclosure is restricted by U.S. law. Dissemination
 * to non-U.S. persons whether in the United States or abroad requires
 * an export license or other authorization.
 * 
 * Contractor Name:        Raytheon Company
 * Contractor Address:     6825 Pine Street, Suite 340
 *                         Mail Stop B8
 *                         Omaha, NE 68106
 *                         402.291.0100
 * 
 * See the AWIPS II Master Rights File ("Master Rights File.pdf") for
 * further licensing information.
 **/
package com.raytheon.uf.common.recommenders;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

import jep.JepException;

import com.raytheon.uf.common.dataplugin.events.EventSet;
import com.raytheon.uf.common.dataplugin.events.IEvent;
import com.raytheon.uf.common.localization.FileUpdatedMessage;
import com.raytheon.uf.common.localization.FileUpdatedMessage.FileChangeType;
import com.raytheon.uf.common.localization.IPathManager;
import com.raytheon.uf.common.localization.LocalizationContext;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationLevel;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationType;
import com.raytheon.uf.common.localization.LocalizationFile;
import com.raytheon.uf.common.localization.PathManagerFactory;
import com.raytheon.uf.common.python.PyUtil;
import com.raytheon.uf.common.python.concurrent.PythonJobCoordinator;
import com.raytheon.uf.common.python.controller.PythonScriptController;
import com.raytheon.uf.common.recommenders.executors.RecommenderMetadataExecutor;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.common.util.FileUtil;

/**
 * Manages the inventory of recommenders and sends requests to run recommenders
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 17, 2013            mnash     Initial creation
 * 
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */

public abstract class AbstractRecommenderScriptManager extends
        PythonScriptController {

    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(AbstractRecommenderScriptManager.class);

    private static final String GET_SCRIPT_METADATA = "getScriptMetadata";

    /*
     * A cached list of the current recommenders, for use by anything that wants
     * all of them. We cache so that we don't have to run getScriptMetadata
     * continuously for every script.
     */
    protected Map<String, EventRecommender> inventory = null;

    protected static LocalizationFile recommenderDir;

    /**
     * @param filePath
     * @param anIncludePath
     * @param classLoader
     * @param aPythonClassName
     * @throws JepException
     */
    protected AbstractRecommenderScriptManager(String filePath,
            String anIncludePath, ClassLoader classLoader,
            String aPythonClassName) throws JepException {
        super(filePath, anIncludePath, classLoader, aPythonClassName);
        inventory = new ConcurrentHashMap<String, EventRecommender>();

        String scriptPath = buildRecommenderPath();
        jep.eval(INTERFACE + " = RecommenderInterface('" + scriptPath + "')");
        List<String> errors = getStartupErrors();
        if (errors.size() > 0) {
            StringBuffer sb = new StringBuffer();
            sb.append("Error importing the following recommenders:\n");
            for (String s : errors) {
                sb.append(s);
                sb.append("\n");
            }
            statusHandler.handle(Priority.WARN,
                    "Some recommenders were not able to be imported",
                    sb.toString());
        }

        jep.eval("import sys");
        jep.eval("sys.argv = ['RecommenderInterface']");

        Thread thread = new Thread("Retrieve Recommender List") {
            @Override
            public void run() {
                retrieveRecommenderList();
            };
        };
        thread.run();
    }

    /**
     * Builds the path to the recommenders in localization.
     * 
     * @return
     */
    protected static String buildRecommenderPath() {
        IPathManager pathMgr = PathManagerFactory.getPathManager();
        LocalizationContext userContext = pathMgr.getContext(
                LocalizationType.COMMON_STATIC, LocalizationLevel.USER);
        LocalizationContext siteContext = pathMgr.getContext(
                LocalizationType.COMMON_STATIC, LocalizationLevel.SITE);
        LocalizationContext baseContext = pathMgr.getContext(
                LocalizationType.COMMON_STATIC, LocalizationLevel.BASE);

        String fileLoc = "python" + File.separator + "events" + File.separator
                + "recommenders";

        recommenderDir = pathMgr.getLocalizationFile(baseContext, fileLoc);
        String userPath = pathMgr.getLocalizationFile(userContext, fileLoc)
                .getFile().getPath();
        String sitePath = pathMgr.getLocalizationFile(siteContext, fileLoc)
                .getFile().getPath();
        String basePath = recommenderDir.getFile().getPath();
        return PyUtil.buildJepIncludePath(userPath, sitePath, basePath);
    }

    /**
     * Builds the path to the recommender interface Python script.
     * 
     * @return
     */
    protected static String buildScriptPath() {
        IPathManager pathMgr = PathManagerFactory.getPathManager();
        LocalizationContext baseContext = pathMgr.getContext(
                LocalizationType.COMMON_STATIC, LocalizationLevel.BASE);

        String fileLoc = "python" + File.separator + "events" + File.separator
                + "recommenders" + File.separator + "config";

        recommenderDir = pathMgr.getLocalizationFile(baseContext, fileLoc);
        String recommenderScriptPath = FileUtil.join(recommenderDir.getFile()
                .getPath(), "RecommenderInterface.py");
        return PyUtil.buildJepIncludePath(recommenderScriptPath);
    }

    /**
     * Mashes together the Python path for the recommenders.
     * 
     * @return
     */
    protected static String buildPythonPath() {
        IPathManager manager = PathManagerFactory.getPathManager();
        String pythonPath = manager.getFile(recommenderDir.getContext(),
                "python").getPath();
        String dataAccessPath = FileUtil.join(pythonPath, "dataaccess");
        String dataTimePath = FileUtil.join(pythonPath, "time");
        String recommenderConfigPath = recommenderDir.getFile().getPath();
        String recommenderDirPath = recommenderDir.getFile().getParentFile()
                .getPath();
        String eventsPath = FileUtil.join(pythonPath, "events");
        String utilitiesPath = FileUtil.join(eventsPath, "utilities");
        String gfePath = FileUtil.join(pythonPath, "gfe");

        String includePath = PyUtil.buildJepIncludePath(pythonPath,
                recommenderConfigPath, recommenderDirPath, dataAccessPath,
                dataTimePath, eventsPath, utilitiesPath, gfePath);
        return includePath;
    }

    /**
     * This method does the all encompassing execution of a recommender. This
     * should not be called by clients.
     * 
     * @param recommenderName
     * @return
     */
    public abstract List<IEvent> executeEntireRecommender(String recommenderName);

    /**
     * This method just runs the execute method on the recommender. This method
     * assumes all the information that is being passed in is correct. This
     * should not be called by clients.
     * 
     * @param recommenderName
     * @param dialogValues
     * @param spatialValues
     * @return
     */
    public List<IEvent> executeRecommender(String recommenderName,
            EventSet<IEvent> eventSet, Map<String, String> dialogValues,
            Map<String, String> spatialValues) {
        final Map<String, Object> args = getStarterMap(recommenderName);
        args.put("eventSet", eventSet);
        args.put("dialogInputMap", dialogValues);
        args.put("spatialInputMap", spatialValues);
        Object retVal = null;
        try {
            retVal = execute("execute", INTERFACE, args);
        } catch (JepException e) {
            statusHandler.handle(Priority.ERROR,
                    "Unable to execute recommender", e);
        }
        return resolveEvents(retVal);
    }

    /**
     * Method to take in and retrieve information from either spatial info or
     * dialog info.
     * 
     * @param recName
     * @param methodName
     * @return
     */
    @SuppressWarnings("unchecked")
    public Map<String, String> getInfo(String recName, String methodName) {
        Object retVal = null;
        try {
            final Map<String, Object> args = getStarterMap(recName);
            retVal = execute(methodName, INTERFACE, args);
        } catch (JepException e) {
            statusHandler.handle(Priority.ERROR, "Unable to get info from "
                    + methodName, e);
        }
        if (retVal == null) {
            retVal = new HashMap<String, String>();
        }
        return (Map<String, String>) retVal;
    }

    /**
     * Retrieve the recommenders based on the file name
     */
    @SuppressWarnings("unchecked")
    private void retrieveRecommenderList() {
        IPathManager manager = PathManagerFactory.getPathManager();
        LocalizationFile[] lFiles = manager.listStaticFiles(
                recommenderDir.getName(), new String[] { "py" }, false, true);
        for (LocalizationFile lFile : lFiles) {
            lFile.addFileUpdatedObserver(this);
            EventRecommender reco = setMetadata(lFile);
            if (reco != null) {
                inventory.put(reco.getName(), reco);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.common.python.controller.PythonScriptController#fileUpdated
     * (com.raytheon.uf.common.localization.FileUpdatedMessage)
     */
    @Override
    public void fileUpdated(FileUpdatedMessage message) {
        IPathManager pathMgr = PathManagerFactory.getPathManager();
        FileChangeType type = message.getChangeType();
        String[] dirs = message.getFileName().split(File.separator);
        String name = dirs[dirs.length - 1];
        String filename = resolveCorrectName(name);
        if (type == FileChangeType.UPDATED) {
            EventRecommender rec = setMetadata(pathMgr.getLocalizationFile(
                    message.getContext(), message.getFileName()));
            if (rec != null) {
                inventory.put(filename, rec);
            }
        } else if (type == FileChangeType.ADDED) {
            if (inventory.get(filename) != null) {
                inventory.get(filename).getFile()
                        .removeFileUpdatedObserver(this);
                inventory.remove(filename);
            }
            EventRecommender rec = setMetadata(pathMgr.getLocalizationFile(
                    message.getContext(), message.getFileName()));
            if (rec != null) {
                inventory.put(filename, rec);
            }
            rec.getFile().addFileUpdatedObserver(this);
        } else if (type == FileChangeType.DELETED) {
            EventRecommender rec = inventory.remove(filename);
            rec.getFile().removeFileUpdatedObserver(this);
        }
        super.fileUpdated(message);
    }

    private EventRecommender setMetadata(LocalizationFile file) {
        final String modName = resolveCorrectName(file.getFile().getName());
        Map<String, String> results = null;
        try {
            if (isInstantiated(modName) == false) {
                instantiatePythonScript(modName);
            }
            Map<String, Object> args = getStarterMap(modName);
            results = (HashMap<String, String>) execute(GET_SCRIPT_METADATA,
                    INTERFACE, args);
        } catch (JepException e) {
            statusHandler.handle(Priority.WARN, "Recommender " + modName
                    + " is unable to be instantiated", e);
            return null;
        } catch (Throwable e) {
            RecommenderMetadataExecutor<AbstractRecommenderScriptManager> executor = new RecommenderMetadataExecutor<AbstractRecommenderScriptManager>(
                    modName);
            PythonJobCoordinator<AbstractRecommenderScriptManager> coordinator = PythonJobCoordinator
                    .getInstance("Recommenders");
            try {
                results = coordinator.submitSyncJob(executor);
            } catch (InterruptedException e1) {
                statusHandler.handle(Priority.PROBLEM,
                        e1.getLocalizedMessage(), e1);
            } catch (ExecutionException e1) {
                statusHandler.handle(Priority.PROBLEM,
                        e1.getLocalizedMessage(), e1);
            }
        }
        EventRecommender reco = new EventRecommender();
        reco.setName(modName);
        reco.setFile(file);
        if (results != null) {
            Object auth = results.get(EventRecommender.AUTHOR);
            Object desc = results.get(EventRecommender.DESCRIPTION);
            Object vers = results.get(EventRecommender.VERSION);
            reco.setAuthor(auth != null ? auth.toString() : "");
            reco.setDescription(desc != null ? desc.toString() : "");
            reco.setVersion(vers != null ? vers.toString() : "");
        }
        return reco;
    }

    /**
     * Need to handle both lists and IEvent objects that come out of
     * recommenders.
     * 
     * @param obj
     * @return
     */
    @SuppressWarnings("unchecked")
    protected List<IEvent> resolveEvents(Object obj) {
        List<IEvent> events = new ArrayList<IEvent>();
        if (obj instanceof List) {
            events.addAll((List<IEvent>) obj);
        } else if (obj instanceof IEvent) {
            events.add((IEvent) obj);
        } else if (obj == null) {
            // do nothing, we just want to return an empty events
        } else {
            statusHandler.handle(Priority.CRITICAL,
                    "Must return a single event or multiple event objects");
        }
        return events;
    }

    protected static String resolveCorrectName(String name) {
        if (name.endsWith(".py")) {
            name = name.replace(".py", "");
        }
        return name;
    }

    public List<EventRecommender> getInventory() {
        return new ArrayList<EventRecommender>(inventory.values());
    }
}

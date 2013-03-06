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
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import jep.JepException;

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
import com.raytheon.uf.common.python.concurrent.IPythonJobListener;
import com.raytheon.uf.common.python.controller.PythonScriptController;
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

public abstract class AbstractRecommenderScriptManager<E extends IEvent>
        extends PythonScriptController {

    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(AbstractRecommenderScriptManager.class);

    private static final String GET_SCRIPT_METADATA = "getScriptMetadata";

    /*
     * A cached list of the current recommenders, for use by anything that wants
     * all of them. We cache so that we don't have to run getScriptMetadata
     * continuously for every script.
     */
    protected List<EventRecommender> inventory = null;

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
        inventory = new CopyOnWriteArrayList<EventRecommender>();
        recommenderDir.addFileUpdatedObserver(this);

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
                + "recommenders";

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
        String recommenderDirPath = recommenderDir.getFile().getPath();
        String eventsPath = FileUtil.join(pythonPath, "events");
        String utilitiesPath = FileUtil.join(eventsPath, "utilities");
        String gfePath = FileUtil.join(pythonPath, "gfe");

        String includePath = PyUtil.buildJepIncludePath(pythonPath,
                recommenderDirPath, dataAccessPath, dataTimePath, eventsPath,
                utilitiesPath, gfePath);
        return includePath;
    }

    /**
     * This method, as implemented by subclasses, needs to run the necessary
     * parts (all of them) of the recommender in correct order. This method
     * should be called by clients.
     * 
     * @param recommenderName
     */
    public abstract void runEntireRecommender(String recommenderName,
            IPythonJobListener<List<E>> listener);

    /**
     * This method, as implemented by subclasses, executes just the execute
     * method of a recommender. This method should be called by clients.
     * 
     * @param recommenderName
     * @param spatialInfo
     * @param dialogInfo
     * @param listener
     */
    public abstract void runExecuteRecommender(String recommenderName,
            Set<IEvent> eventSet, Map<String, String> spatialInfo,
            Map<String, String> dialogInfo, IPythonJobListener<List<E>> listener);

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
            Set<IEvent> eventSet, Map<String, String> dialogValues,
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
     * This method may do different things depending on the implementation.
     * Subclasses retrieve information about a possible dialog, or possibly read
     * from a file if no dialog should be present.
     * 
     * @param recommenderName
     * @return
     */
    protected abstract Map<String, String> getDialogInfo(String recommenderName);

    /**
     * This method may do different things depending on the implementation.
     * Subclasses retrieve information about the spatial info, or possibly read
     * from a file if no spatial info should be present.
     * 
     * @param recommenderName
     * @return
     */
    protected abstract Map<String, String> getSpatialInfo(String recommenderName);

    /**
     * This method may do different things depending on the implementation.
     * Subclasses retrieve script metadata from the file most likely.
     * 
     * @param recommenderName
     * @return
     */
    protected abstract Map<String, String> getScriptMetadata(
            String recommenderName);

    /**
     * Method to take in and retrieve information from either spatial info or
     * dialog info.
     * 
     * @param recName
     * @param methodName
     * @return
     */
    public Map<String, String> getInfo(String recName, String methodName) {
        Object retVal = null;
        try {
            final Map<String, Object> args = getStarterMap(recName);
            retVal = execute(methodName, INTERFACE, args);
        } catch (JepException e) {
            statusHandler.handle(Priority.ERROR, "Unable to get info from "
                    + methodName, e);
        }
        return (Map<String, String>) retVal;
    }

    /**
     * Retrieve the recommenders based on the file name
     */
    @SuppressWarnings("unchecked")
    private void retrieveRecommenderList() {
        Map<String, Object> results = null;
        LocalizationFile[] lFiles = PathManagerFactory.getPathManager()
                .listFiles(recommenderDir.getContext(),
                        recommenderDir.getName(), new String[] { "py" }, false,
                        true);
        for (LocalizationFile lFile : lFiles) {
            final String moduleName = resolveCorrectName(lFile.getFile()
                    .getName());
            try {
                if (isInstantiated(moduleName) == false) {
                    instantiatePythonScript(moduleName);
                }

                Map<String, Object> args = getStarterMap(moduleName);
                results = (HashMap<String, Object>) execute(
                        GET_SCRIPT_METADATA, INTERFACE, args);
                if (results != null) {
                    EventRecommender reco = new EventRecommender();
                    reco.setName(moduleName);
                    reco.setFile(lFile);
                    Object auth = results.get(EventRecommender.AUTHOR);
                    Object desc = results.get(EventRecommender.DESCRIPTION);
                    Object vers = results.get(EventRecommender.VERSION);
                    reco.setAuthor(auth != null ? auth.toString() : "");
                    reco.setDescription(desc != null ? desc.toString() : "");
                    reco.setVersion(vers != null ? vers.toString() : "");
                    getInventory().add(reco);
                }
            } catch (JepException e) {
                statusHandler.handle(Priority.INFO, moduleName
                        + " failed to instantiate.", e);
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
        FileChangeType type = message.getChangeType();
        if (type == FileChangeType.UPDATED) {
            for (EventRecommender rec : inventory) {
                if (rec.getFile().getName().equals(message.getFileName())) {
                    updateMetadata(rec);
                    break;
                }
            }
        } else if (type == FileChangeType.ADDED) {
            for (EventRecommender rec : inventory) {
                if (rec.getFile().getName().equals(message.getFileName())
                        && rec.getFile()
                                .getContext()
                                .getLocalizationLevel()
                                .compareTo(
                                        message.getContext()
                                                .getLocalizationLevel()) < 0) {
                    updateMetadata(rec);
                    break;
                } else {
                    EventRecommender newRec = new EventRecommender();
                    // TODO get the file from the path manager?
                    inventory.add(newRec);
                }
            }
        } else if (type == FileChangeType.DELETED) {
            for (EventRecommender rec : inventory) {
                // TODO, need a better check here
                if (rec.getFile().getName().equals(message.getFileName())) {
                    inventory.remove(rec);
                    break;
                }
            }
        }
        super.fileUpdated(message);
    }

    private void updateMetadata(EventRecommender rec) {
        try {
            if (isInstantiated(rec.getName()) == false) {
                instantiatePythonScript(rec.getName());
            }
            Map<String, Object> args = getStarterMap(rec.getName());
            execute(GET_SCRIPT_METADATA, INTERFACE, args);
        } catch (JepException e) {
            statusHandler.handle(Priority.ERROR,
                    "Unable to update metadata on file " + rec.getName(), e);
        }
    }

    /**
     * Need to handle both lists and IEvent objects that come out of
     * recommenders.
     * 
     * @param obj
     * @return
     */
    protected List<IEvent> resolveEvents(Object obj) {
        List<IEvent> events = new ArrayList<IEvent>();
        if (obj instanceof List) {
            events.addAll((List<IEvent>) obj);
        } else if (obj instanceof IEvent) {
            events.add((IEvent) obj);
            return events;
        }
        return events;
    }

    protected static String resolveCorrectName(String name) {
        if (name.endsWith(".py")) {
            name = name.replace(".py", "");
        }
        return name;
    }

    public synchronized List<EventRecommender> getInventory() {
        return inventory;
    }
}

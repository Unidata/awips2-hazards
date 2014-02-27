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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
 * Jan 17, 2013            mnash       Initial creation
 * Jul 12, 2013 1257       bsteffen    Convert recommender dialog info to use
 *                                     Serializeables for values instead of
 *                                     Strings.
 * Aug 15, 2013  750       jramer      Added some paths needed by storm
 *                                     tracking tools
 * Dec 4, 2013  2461       bkowal      Recommenders at other localization
 *                                     levels can now override the base recommender.
 * Jan 20, 2014 2766       bkowal      Updated to use the Python Overrider
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

    private static final String RECOMMENDERS_LOCALIZATION_DIR = "python"
            + File.separator + "events" + File.separator + "recommenders";

    private static final String RECOMMENDERS_CONFIG_LOCALIZATION_DIR = RECOMMENDERS_LOCALIZATION_DIR
            + File.separator + "config";

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

        jep.eval(INTERFACE + " = RecommenderInterface('" + scriptPath + "', '"
                + RECOMMENDERS_LOCALIZATION_DIR + "')");
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
        recommenderDir = getLocalizationFile(LocalizationType.COMMON_STATIC,
                LocalizationLevel.BASE, RECOMMENDERS_LOCALIZATION_DIR);
        String userPath = constructUserLocalizationRecommenderPath();
        String sitePath = constructSiteLocalizationRecommenderPath();
        String basePath = constructBaseLocalizationRecommenderPath();
        return PyUtil.buildJepIncludePath(basePath, sitePath, userPath);
    }

    private static String constructBaseLocalizationRecommenderPath() {
        return constructLocalizationPath(LocalizationType.COMMON_STATIC,
                LocalizationLevel.BASE, RECOMMENDERS_LOCALIZATION_DIR);
    }

    private static String constructSiteLocalizationRecommenderPath() {
        return constructLocalizationPath(LocalizationType.COMMON_STATIC,
                LocalizationLevel.SITE, RECOMMENDERS_LOCALIZATION_DIR);
    }

    private static String constructUserLocalizationRecommenderPath() {
        return constructLocalizationPath(LocalizationType.COMMON_STATIC,
                LocalizationLevel.USER, RECOMMENDERS_LOCALIZATION_DIR);
    }

    private static LocalizationFile getLocalizationFile(
            LocalizationType localizationType,
            LocalizationLevel localizationLevel, String fileLocation) {
        IPathManager pathMgr = PathManagerFactory.getPathManager();
        LocalizationContext localizationContext = pathMgr.getContext(
                localizationType, localizationLevel);

        return pathMgr.getLocalizationFile(localizationContext, fileLocation);
    }

    private static String constructLocalizationPath(
            LocalizationType localizationType,
            LocalizationLevel localizationLevel, String fileLocation) {
        return getLocalizationFile(localizationType, localizationLevel,
                fileLocation).getFile().getPath();
    }

    /**
     * Builds the path to the recommender interface Python script.
     * 
     * @return
     */
    protected static String buildScriptPath() {
        recommenderDir = getLocalizationFile(LocalizationType.COMMON_STATIC,
                LocalizationLevel.BASE, RECOMMENDERS_CONFIG_LOCALIZATION_DIR);
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
        String recommenderDirPath = constructBaseLocalizationRecommenderPath();
        String recommenderSitePath = constructSiteLocalizationRecommenderPath();
        String recommenderUserPath = constructUserLocalizationRecommenderPath();
        String eventsPath = FileUtil.join(pythonPath, "events");
        String utilitiesPath = FileUtil.join(eventsPath, "utilities");
        String gfePath = FileUtil.join(pythonPath, "gfe");
        String bridgePath = FileUtil.join(pythonPath, "bridge");
        String trackUtilPath = FileUtil.join(pythonPath, "trackUtilities");
        String geoUtilPath = FileUtil.join(pythonPath, "geoUtilities");
        String genUtilPath = FileUtil.join(pythonPath, "generalUtilities");
        String logUtilPath = FileUtil.join(pythonPath, "logUtilities");
        String localizationUtilitiesPath = FileUtil.join(pythonPath,
                "localizationUtilities");
        String dataStoragePath = FileUtil.join(pythonPath, "dataStorage");

        String includePath = PyUtil.buildJepIncludePath(pythonPath,
                recommenderConfigPath, recommenderUserPath,
                recommenderSitePath, recommenderDirPath, dataAccessPath,
                dataTimePath, eventsPath, utilitiesPath, gfePath, bridgePath,
                trackUtilPath, geoUtilPath, genUtilPath, logUtilPath,
                localizationUtilitiesPath, dataStoragePath);
        return includePath;
    }

    /**
     * This method does the all encompassing execution of a recommender. This
     * should not be called by clients.
     * 
     * @param recommenderName
     * @return
     */
    public abstract EventSet<IEvent> executeEntireRecommender(
            String recommenderName);

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
    public EventSet<IEvent> executeRecommender(String recommenderName,
            EventSet<IEvent> eventSet, Map<String, Serializable> dialogValues,
            Map<String, Serializable> spatialValues) {
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
    public <T> Map<String, T> getInfo(String recName, String methodName) {
        Object retVal = null;
        try {
            final Map<String, Object> args = getStarterMap(recName);
            retVal = execute(methodName, INTERFACE, args);
        } catch (JepException e) {
            statusHandler.handle(Priority.ERROR, "Unable to get info from "
                    + methodName, e);
        }
        if (retVal == null) {
            retVal = new HashMap<String, T>();
        }
        return (Map<String, T>) retVal;
    }

    /**
     * Retrieve the recommenders based on the file name
     */
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

    @SuppressWarnings("unchecked")
    private EventRecommender setMetadata(LocalizationFile file) {
        final String modName = resolveCorrectName(file.getFile().getName());
        Map<String, Serializable> results = null;
        try {
            if (isInstantiated(modName) == false) {
                instantiatePythonScript(modName);
            }
            Map<String, Object> args = getStarterMap(modName);
            results = (Map<String, Serializable>) execute(GET_SCRIPT_METADATA,
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
            Object threadManager = results.get(EventRecommender.THREAD_MANAGER);
            reco.setAuthor(auth != null ? auth.toString() : "");
            reco.setDescription(desc != null ? desc.toString() : "");
            reco.setVersion(vers != null ? vers.toString() : "");
            reco.setThreadManager(threadManager != null ? threadManager
                    .toString()
                    : AbstractRecommenderEngine.DEFAULT_RECOMMENDER_JOB_COORDINATOR);
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
    protected EventSet<IEvent> resolveEvents(Object obj) {
        EventSet<IEvent> events = new EventSet<IEvent>();
        // EventSet is what is intended, all others are going to be removed
        // eventually. This is only for backwards compatibility.
        if (obj instanceof EventSet) {
            events.setAttributes(((EventSet<IEvent>) obj).getAttributes());
            events.addAll((Set<IEvent>) obj);
        } else if (obj instanceof List) {
            statusHandler.handle(Priority.VERBOSE, "Return objects of type "
                    + obj.getClass()
                    + " are not supported, but will handle for now.");
            events.addAll((List<IEvent>) obj);
        } else if (obj instanceof IEvent) {
            statusHandler.handle(Priority.VERBOSE, "Return objects of type "
                    + obj.getClass()
                    + " are not supported, but will handle for now.");
            events.add((IEvent) obj);
        } else if (obj == null) {
            // do nothing, we just want to return an empty events
        } else {
            statusHandler.handle(Priority.CRITICAL,
                    "Must return an event set of objects");
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

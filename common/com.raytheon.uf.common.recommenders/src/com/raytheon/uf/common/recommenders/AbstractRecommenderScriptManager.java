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

import gov.noaa.gsd.common.visuals.VisualFeaturesList;

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
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.utilities.PythonBuildPaths;
import com.raytheon.uf.common.hazards.configuration.HazardsConfigurationConstants;
import com.raytheon.uf.common.localization.FileUpdatedMessage;
import com.raytheon.uf.common.localization.FileUpdatedMessage.FileChangeType;
import com.raytheon.uf.common.localization.ILocalizationFileObserver;
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
 * Mar 19, 2014 3293       bkowal      Added the REGION localization level.
 * Apr 09, 2014 3395       bkowal      Improve recommender inventory management.
 *                                     Scripts are now only loaded and initialized
 *                                     as they are used. Scripts that are updated
 *                                     via localization are now dropped after the
 *                                     initial update and they will only be
 *                                     re-loaded / re-initialized if the updated
 *                                     script is actually used instead of after
 *                                     every change / save.
 * Aug 18, 2014 4243       Chris.Golden Changed getInventory(recommenderName) to
 *                                      only return a single recommender.
 * Oct 13, 2014 3790       Robert.Blum  Processing file changes when initializing
 *                                      the recommender incase a update was made. 
 *                                      Also reloading next localization level if a
 *                                      recommender is deleted.
 * Dec 12, 2014 4124       Kevin.Manross Add "textUtilities" to python/JEP include path
 * Jan 29, 2015 3626       Chris.Golden Added EventSet to arguments for getting dialog
 *                                      info.
 * Feb 16, 2015 5071       Robert.Blum  Changes to reload Recommenders without restarting Cave.
 * Feb 26, 2015 6306       mduff        Pass site id in to build paths for provided site.
 * Nov 17, 2015 3473       Robert.Blum  Moved all python files under HazardServices
 *                                      localization dir.
 * Feb 12, 2016 14923      Robert.Blum  Picking up overrides of EventUtilities directory
 * Jun 23, 2016 19537      Chris.Golden Changed to use visual features for spatial info.
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

    /* python/events/utilities directory */
    protected static LocalizationFile eventUtilDir;

    /*
     * A cached list of the current recommenders, for use by anything that wants
     * all of them. We cache so that we don't have to run getScriptMetadata
     * continuously for every script.
     */
    protected Map<String, EventRecommender> inventory = null;

    private boolean pendingEventUtilitiesUpdates = false;

    protected static LocalizationFile recommenderDir;

    private final ILocalizationFileObserver eventUtilDirObserver;

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

        recommenderDir = PythonBuildPaths
                .buildLocalizationDirectory(HazardsConfigurationConstants.PYTHON_LOCALIZATION_RECOMMENDERS_DIR);
        recommenderDir.addFileUpdatedObserver(this);

        String scriptPath = PythonBuildPaths
                .buildDirectoryPath(
                        HazardsConfigurationConstants.PYTHON_LOCALIZATION_RECOMMENDERS_DIR,
                        null);

        IPathManager manager = PathManagerFactory.getPathManager();
        LocalizationContext baseContext = manager.getContext(
                LocalizationType.COMMON_STATIC, LocalizationLevel.BASE);
        eventUtilDir = manager.getLocalizationFile(baseContext,
                HazardsConfigurationConstants.EVENT_UTILITIES_LOCALIZATION_DIR);
        eventUtilDirObserver = new EventUtilitiesDirectoryUpdateObserver();
        eventUtilDir.addFileUpdatedObserver(eventUtilDirObserver);

        jep.eval(INTERFACE + " = RecommenderInterface('" + scriptPath + "', '"
                + HazardsConfigurationConstants.RECOMMENDERS_LOCALIZATION_DIR
                + "')");
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
    }

    private static LocalizationFile getLocalizationFile(
            LocalizationType localizationType,
            LocalizationLevel localizationLevel, String fileLocation) {
        IPathManager pathMgr = PathManagerFactory.getPathManager();
        LocalizationContext localizationContext = pathMgr.getContext(
                localizationType, localizationLevel);

        return pathMgr.getLocalizationFile(localizationContext, fileLocation);
    }

    /**
     * Builds the path to the recommender interface Python script.
     * 
     * @return
     */
    protected static String buildScriptPath() {
        recommenderDir = getLocalizationFile(
                LocalizationType.COMMON_STATIC,
                LocalizationLevel.BASE,
                HazardsConfigurationConstants.RECOMMENDERS_CONFIG_LOCALIZATION_DIR);
        String recommenderScriptPath = FileUtil.join(recommenderDir.getFile()
                .getPath(), "RecommenderInterface.py");
        return PyUtil.buildJepIncludePath(recommenderScriptPath);
    }

    /**
     * Mashes together the Python path for the recommenders.
     * 
     * @return
     */
    protected static String buildPythonPath(String site) {
        IPathManager manager = PathManagerFactory.getPathManager();

        List<String> includePathList = new ArrayList<>();

        String pythonPath = manager.getFile(recommenderDir.getContext(),
                "python").getPath();

        String hazardServicesPythonPath = manager
                .getFile(
                        recommenderDir.getContext(),
                        HazardsConfigurationConstants.HAZARD_SERVICES_PYTHON_LOCALIZATION_DIR)
                .getPath();

        if (pythonPath != null) {
            includePathList.add(pythonPath);
        }

        if (hazardServicesPythonPath != null) {
            includePathList.add(hazardServicesPythonPath);
        }

        String recommenderConfigPath = recommenderDir.getFile().getPath();
        if (recommenderConfigPath != null) {
            includePathList.add(recommenderConfigPath);
        }

        LocalizationLevel[] levels = manager.getAvailableLevels();
        for (int i = levels.length - 1; i >= 0; i--) {
            LocalizationLevel level = levels[i];
            LocalizationContext lc = manager.getContext(
                    LocalizationType.COMMON_STATIC, level);
            if (site != null
                    && (level == LocalizationLevel.SITE || level == LocalizationLevel.CONFIGURED)) {
                lc.setContextName(site);
            }
            includePathList
                    .add(manager
                            .getLocalizationFile(
                                    lc,
                                    HazardsConfigurationConstants.RECOMMENDERS_LOCALIZATION_DIR)
                            .getFile().getPath());
        }

        String dataAccessPath = FileUtil.join(pythonPath, "dataaccess");
        includePathList.add(dataAccessPath);

        String dataTimePath = FileUtil.join(pythonPath, "time");
        includePathList.add(dataTimePath);

        String gfePath = FileUtil.join(pythonPath, "gfe");
        includePathList.add(gfePath);

        String eventsPath = FileUtil.join(hazardServicesPythonPath, "events");
        includePathList.add(eventsPath);

        String utilitiesPath = FileUtil.join(eventsPath, "utilities");
        includePathList.add(utilitiesPath);

        String productGenPath = FileUtil.join(eventsPath, "productgen");
        includePathList.add(productGenPath);

        String bridgePath = FileUtil.join(hazardServicesPythonPath, "bridge");
        includePathList.add(bridgePath);

        String trackUtilPath = FileUtil.join(hazardServicesPythonPath,
                "trackUtilities");
        includePathList.add(trackUtilPath);

        String geoUtilPath = FileUtil.join(hazardServicesPythonPath,
                "geoUtilities");
        includePathList.add(geoUtilPath);

        String genUtilPath = FileUtil.join(hazardServicesPythonPath,
                "generalUtilities");
        includePathList.add(genUtilPath);

        String logUtilPath = FileUtil.join(hazardServicesPythonPath,
                "logUtilities");
        includePathList.add(logUtilPath);

        String localizationUtilitiesPath = FileUtil.join(
                hazardServicesPythonPath, "localizationUtilities");
        includePathList.add(localizationUtilitiesPath);

        String dataStoragePath = FileUtil.join(hazardServicesPythonPath,
                "dataStorage");
        includePathList.add(dataStoragePath);

        String textUtilitiesPath = FileUtil.join(hazardServicesPythonPath,
                "textUtilities");
        includePathList.add(textUtilitiesPath);

        String includePath = PyUtil.buildJepIncludePath(includePathList
                .toArray(new String[includePathList.size()]));
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
            String recommenderName, EventSet<IEvent> eventSet);

    /**
     * This method will check the inventory to verify that the specified
     * recommender has been loaded and initialized. If the recommender is not
     * found in the inventory, the method will attempt to load and initialize
     * the recommender.
     * 
     * @param recommenderName
     *            The name of the recommender
     * @return true if the recommender is in the inventory or if the recommender
     *         has been successfully loaded and initialized; otherwise, false
     */
    public boolean verifyRecommenderIsLoaded(String recommenderName) {
        /*
         * Determine if some kind of update was made to a python module. If so
         * then initialize the recommender. This is needed because the
         * inventory.containsKey does not account for super classes.
         */
        boolean updateMade = true;
        // If no pending updates reset flag to false
        if (pendingAdds.isEmpty() && pendingReloads.isEmpty()
                && pendingRemoves.isEmpty()) {
            updateMade = false;
        } else {
            processFileUpdates();
        }

        // If there are pending EventUtilities updates reload the modules
        if (pendingEventUtilitiesUpdates) {
            try {
                reloadEventUtilities();
                pendingEventUtilitiesUpdates = false;
            } catch (JepException e) {
                statusHandler.handle(Priority.WARN,
                        "Event Utilities were unable to be imported", e);
            }
        }

        if (this.inventory.containsKey(recommenderName)
                && (updateMade == false)) {
            return true;
        }

        return this.initializeRecommender(recommenderName);
    }

    /**
     * This method just runs the execute method on the recommender. This method
     * assumes all the information that is being passed in is correct. This
     * should not be called by clients.
     * 
     * @param recommenderName
     *            Name of the recommender to be run.
     * @param dialogValues
     *            Map pairing dialog parameter names with their values as
     *            specified by the user; may be empty.
     * @param visualFeatures
     *            Visual features, the geometries of which provide any spatial
     *            input needed from the user; may be empty.
     * @return
     */
    public EventSet<IEvent> executeRecommender(String recommenderName,
            EventSet<IEvent> eventSet, Map<String, Serializable> dialogValues,
            VisualFeaturesList visualFeatures) {
        /*
         * This function call may only be needed in this location for the
         * automated tests and not in a typical usage scenario?
         */
        if (this.verifyRecommenderIsLoaded(recommenderName) == false) {
            return resolveEvents(null);
        }

        final Map<String, Object> args = getStarterMap(recommenderName);
        args.put("eventSet", eventSet);
        args.put("dialogInputMap", dialogValues);
        args.put("visualFeatures", visualFeatures);
        Object retVal = null;
        try {
            retVal = execute("execute", INTERFACE, args);
        } catch (JepException e) {
            statusHandler.handle(Priority.ERROR,
                    "Unable to execute recommender", e);
        }
        return resolveEvents(retVal);
    }

    private boolean initializeRecommender(String recName) {
        long startTime = System.currentTimeMillis();
        LocalizationFile localizationFile = this
                .lookupRecommenderLocalization(recName);
        if (localizationFile == null) {
            statusHandler.handle(Priority.PROBLEM,
                    "Unable to find recommender: " + recName + "!");
            return false;
        }

        // load the recommender.
        EventRecommender reco = setMetadata(localizationFile);
        if (reco != null) {
            inventory.put(reco.getName(), reco);
        } else {
            statusHandler.handle(Priority.PROBLEM,
                    "Failed to initialize recommender: " + recName + "!");
            return false;
        }

        localizationFile.addFileUpdatedObserver(this);

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        statusHandler.handle(Priority.VERBOSE, "Initialized Recommender "
                + recName + " in " + duration + " ms.");

        return true;
    }

    /**
     * Method to retrieve information either a dialog description for a dialog
     * to be shown to the user to gather parameter values, or to retrieve
     * metadata about the recommender.
     * 
     * @param recName
     *            Recommender name.
     * @param methodName
     *            Name of the Python method to be executed.
     * @param eventSet
     *            Event set providing context.
     * @return Map pairing parameter names with their values.
     */
    @SuppressWarnings("unchecked")
    public <T> Map<String, T> getInfo(String recName, String methodName,
            EventSet<IEvent> eventSet) {
        if (this.verifyRecommenderIsLoaded(recName) == false) {
            return new HashMap<String, T>();
        }
        Object retVal = null;
        try {
            final Map<String, Object> args = getStarterMap(recName);
            if (eventSet != null) {
                args.put("eventSet", eventSet);
            }
            retVal = execute(methodName, INTERFACE, args);
        } catch (JepException e) {
            statusHandler.handle(Priority.ERROR, "Unable to get info from "
                    + methodName + " for Recommender " + recName, e);
        }
        if (retVal == null) {
            retVal = new HashMap<String, T>();
        }
        return (Map<String, T>) retVal;
    }

    /**
     * Method to retrieve the list of visual features, if any, that the
     * recommender is to use to gather spatial input from the user.
     * 
     * @param recName
     *            Recommender name.
     * @param eventSet
     *            Event set providing context.
     * @return List of visual features.
     */
    public VisualFeaturesList getVisualFeatures(String recName,
            EventSet<IEvent> eventSet) {
        if (this.verifyRecommenderIsLoaded(recName) == false) {
            return new VisualFeaturesList();
        }
        try {
            final Map<String, Object> args = getStarterMap(recName);
            if (eventSet != null) {
                args.put("eventSet", eventSet);
            }
            return (VisualFeaturesList) execute(
                    HazardConstants.RECOMMENDER_GET_SPATIAL_INFO_METHOD,
                    INTERFACE, args);
        } catch (JepException e) {
            statusHandler.handle(Priority.ERROR, "Unable to get info from "
                    + HazardConstants.RECOMMENDER_GET_SPATIAL_INFO_METHOD
                    + " for recommender " + recName, e);
            return new VisualFeaturesList();
        }
    }

    private LocalizationFile lookupRecommenderLocalization(final String recName) {
        IPathManager manager = PathManagerFactory.getPathManager();
        LocalizationFile[] lFiles = manager.listStaticFiles(
                recommenderDir.getName(), new String[] { "py" }, false, true);
        for (LocalizationFile lFile : lFiles) {
            final String modName = resolveCorrectName(lFile.getFile().getName());
            if (recName.equals(modName)) {
                return lFile;
            }
        }

        return null;
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
        String[] dirs = message.getFileName().split(File.separator);
        String name = dirs[dirs.length - 1];
        String filename = resolveCorrectName(name);
        if (this.inventory.get(filename) != null) {
            this.inventory.get(filename).getFile()
                    .removeFileUpdatedObserver(this);
            final String modName = resolveCorrectName(name);
            statusHandler.handle(Priority.VERBOSE,
                    "Removing initialized Recommender " + modName
                            + " due to update.");
            this.inventory.remove(filename);
        }

        if (message.getChangeType() == FileChangeType.DELETED) {
            IPathManager pm = PathManagerFactory.getPathManager();
            LocalizationFile lf = pm.getLocalizationFile(message.getContext(),
                    message.getFileName());
            if (lf != null) {
                File toDelete = lf.getFile();
                toDelete.delete();
            }
            pendingRemoves.add(filename);

            // Check to see if a another level needs loaded
            Map<LocalizationLevel, LocalizationFile> map = pm
                    .getTieredLocalizationFile(LocalizationType.COMMON_STATIC,
                            message.getFileName());

            // Remove the deleted level
            if (map.containsKey(lf.getContext().getLocalizationLevel())) {
                map.remove(lf.getContext().getLocalizationLevel());
            }

            // If another level exists load it
            if (!map.isEmpty()) {
                pendingAdds.add(filename);
            }

        } else {
            super.fileUpdated(message);
        }
    }

    @SuppressWarnings("unchecked")
    private EventRecommender setMetadata(LocalizationFile file) {
        final String modName = resolveCorrectName(file.getFile().getName());
        Map<String, Serializable> results = null;

        try {
            reloadModule(modName);
            instantiatePythonScript(modName);
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

    public EventRecommender getRecommender(String recommenderName) {
        this.verifyRecommenderIsLoaded(recommenderName);
        for (EventRecommender rec : inventory.values()) {
            if (rec.getName().equals(recommenderName)) {
                return rec;
            }
        }
        return null;
    }

    /**
     * Reloads the updated eventUtilities modules in the interpreter's "cache".
     * 
     * @throws JepException
     *             If an Error is thrown during python execution.
     */
    protected void reloadEventUtilities() throws JepException {
        execute("importEventUtility", INTERFACE, null);
    }

    private class EventUtilitiesDirectoryUpdateObserver implements
            ILocalizationFileObserver {

        @Override
        public void fileUpdated(FileUpdatedMessage message) {
            IPathManager pm = PathManagerFactory.getPathManager();
            LocalizationFile lf = pm.getLocalizationFile(message.getContext(),
                    message.getFileName());

            if (message.getChangeType() == FileChangeType.ADDED
                    || message.getChangeType() == FileChangeType.UPDATED) {
                if (lf != null) {
                    lf.getFile();
                }
            } else if (message.getChangeType() == FileChangeType.DELETED) {
                if (lf != null) {
                    File toDelete = lf.getFile();
                    toDelete.delete();
                }
            }
            pendingEventUtilitiesUpdates = true;
        }
    }
}
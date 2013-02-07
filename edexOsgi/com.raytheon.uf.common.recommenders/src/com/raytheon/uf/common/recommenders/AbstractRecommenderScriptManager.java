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

        String scriptPath = recommenderDir.getFile().getPath();
        jep.eval(INTERFACE + " = RecommenderInterface('" + scriptPath + "')");
        List<String> errors = getStartupErrors();
        if (errors.size() > 0) {
            StringBuffer sb = new StringBuffer();
            sb.append("Error importing the following recommenders:\n");
            for (String s : errors) {
                sb.append(s);
                sb.append("\n");
            }
            System.out.println(sb);
        }

        jep.eval("import sys");
        jep.eval("sys.argv = ['RecommenderInterface']");

        retrieveRecommenderList();
    }

    /**
     * Builds the path to the recommender interface Python script.
     * 
     * @return
     */
    protected static String buildRecommenderPath() {
        IPathManager pathMgr = PathManagerFactory.getPathManager();
        LocalizationContext context = pathMgr.getContext(
                LocalizationType.COMMON_STATIC, LocalizationLevel.BASE);

        recommenderDir = pathMgr.getLocalizationFile(context, "recommenders");

        String recommenderScriptPath = FileUtil.join(recommenderDir.getFile()
                .getPath(), "RecommenderInterface.py");
        return recommenderScriptPath;
    }

    /**
     * Mashes together the Python path for the recommenders.
     * 
     * @return
     */
    protected static String buildPythonPath() {
        IPathManager pathMgr = PathManagerFactory.getPathManager();
        File pythonDir = pathMgr.getFile(recommenderDir.getContext(), "python");
        String dataAccessPath = FileUtil
                .join(pythonDir.getPath(), "dataaccess");
        String dataTimePath = FileUtil.join(pythonDir.getPath(), "time");
        String recommenderDirPath = recommenderDir.getFile().getPath();
        String eventsPath = FileUtil.join(recommenderDirPath, "events");
        String utilitiesPath = FileUtil.join(recommenderDirPath, "utilities");
        String gfePath = FileUtil.join(pythonDir.getPath(), "gfe");
        return PyUtil.buildJepIncludePath(pythonDir.getPath(),
                recommenderDirPath, dataAccessPath, dataTimePath, eventsPath,
                utilitiesPath, gfePath);
    }

    /**
     * This method, as implemented by subclasses, needs to run the necessary
     * parts of the recommender in correct order.
     * 
     * @param recommenderName
     */
    public abstract void runRecommender(String recommenderName,
            IPythonJobListener<List<E>> listener);

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

    public List<EventRecommender> getInventory() {
        return inventory;
    }
}

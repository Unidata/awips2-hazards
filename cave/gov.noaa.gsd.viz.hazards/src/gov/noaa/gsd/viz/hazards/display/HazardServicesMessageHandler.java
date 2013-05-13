/**
 * This software was developed and / or modified by the National Oceanic and
 * Atmospheric Administration (NOAA), Earth System Research Laboratory (ESRL),
 * Global Systems Division (GSD), Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.display;

import gov.noaa.gsd.viz.hazards.display.action.ProductEditorAction;
import gov.noaa.gsd.viz.hazards.jsonutilities.Dict;
import gov.noaa.gsd.viz.hazards.jsonutilities.DictList;
import gov.noaa.gsd.viz.hazards.spatialdisplay.HazardServicesDrawingAction;
import gov.noaa.gsd.viz.hazards.spatialdisplay.HazardServicesMouseHandlers;
import gov.noaa.gsd.viz.hazards.spatialdisplay.SpatialView.SpatialViewCursorTypes;
import gov.noaa.gsd.viz.hazards.utilities.Utilities;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;

import jep.Jep;
import jep.JepException;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.widgets.Display;
import org.osgi.framework.Bundle;

import com.raytheon.uf.common.dataplugin.events.IEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.HazardEventManager;
import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.IHazardEventManager;
import com.raytheon.uf.common.hazards.productgen.IGeneratedProduct;
import com.raytheon.uf.common.python.PyUtil;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.DataTime;
import com.raytheon.uf.viz.core.VizApp;
import com.raytheon.uf.viz.core.drawables.IDescriptor.FramesInfo;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.localization.LocalizationManager;
import com.raytheon.viz.core.mode.CAVEMode;
import com.raytheon.viz.ui.VizWorkbenchManager;
import com.raytheon.viz.ui.editor.AbstractEditor;

/**
 * Description: Handles messages delegated from the message listener object.
 * These are typically messages received from the presenters.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 29, 2013            bryon.lawrence      Initial creation
 * 
 * </pre>
 * 
 * @author bryon.lawrence
 * @version 1.0
 */
public final class HazardServicesMessageHandler {

    // Private Variables

    /**
     * Key indicating that an event has ended and that the cancellation or
     * expiration product needs to be previewed.
     */
    private final String PREVIEW_ENDED = "previewEnded";

    /**
     * Canned hazard services setting identifiers
     */
    private final String FLOOD_SETTING = "Flood";

    private final String WSW_SETTING = "WSW";

    private final String TOR_SETTING = "TOR";

    /**
     * Perspective Identifiers A goal is to make this code perspective agnostic.
     * So, these constants will be going away.
     */
    private final String HYDRO_PERSPECTIVE = "Hydro";

    private final String GFE_PERSPECTIVE = "GFE";

    private final String D2D_PERSPECTIVE = "D2D";

    /**
     * True/False string representations
     */
    private final String TRUE_FLAG = "True";

    private final String FALSE_FLAG = "False";

    /**
     * The key for a hazard's label in a hazard event dict.
     */
    private final String HAZARD_LABEL = "label";

    /**
     * Possible return types for hazard dictionaries
     */
    private final String RETURN_TYPE = "returnType";

    private final String POINT_RETURN_TYPE = "Point";

    private final String STAGING_INFO_RETURN_TYPE = "stagingInfo";

    private final String IEVENT_LIST_RETURN_TYPE = "IEvent List";

    private final String EVENT_DICTS_RETURN_TYPE = "EventDicts";

    /**
     * Indicates that a range of selected times has been updated.
     */
    private final String RANGE_OF_SELECTED_TIMES = "Range";

    /**
     * Indicates that a single selected time has been updated.
     */
    private final String SINGLE_SELECTED_TIME = "Single";

    /**
     * Specifies a JSON format
     */
    private final String JSON = "json";

    /**
     * The selection call back is the recommender to run (if any) when a hazard
     * is selected.
     */
    private final String SELECTION_CALLBACK = "selectionCallback";

    /**
     * Entries on the right-click, pop-up menu
     */
    private final String CONETXT_MENU_BRING_TO_FRONT = "Bring to Front";

    private final String CONTEXT_MENU_SEND_TO_BACK = "Send to Back";

    private final String CONTEXT_MENU_ADD_REMOVE_SHAPES = "Add/Remove Shapes";

    private final String CONTEXT_MENU_HAZARD_INFORMATION_DIALOG = "Hazard Information Dialog";

    private final String CONTEXT_MENU_SAVE = "Save";

    private final String CONTEXT_MENU_REMOVE_POTENTIAL_HAZARDS = "Remove Potential Hazards";

    private final String CONTEXT_MENU_DELETE = "Delete";

    private final String CONTEXT_MENU_MOVE_ENTIRE_ELEMENT = "Move Entire Element";

    private final String CONTEXT_MENU_ADD_POINT = "Add Point";

    private final String CONTEXT_MENU_DELETE_POINT = "Delete Point";

    private final String CONTEXT_MENU_END = "End";

    private final String CONTEXT_MENU_ISSUE = "Issue";

    private final String CONTEXT_MENU_PROPOSE = "Propose";

    /**
     * Constants representing CAVE frame information.
     */
    private final String FRAME_TIMES = "frameTimes";

    private final String FRAME_INDEX = "frameIndex";

    private final String FRAME_COUNT = "frameCount";

    /**
     * A key used to represent results from a recommender
     */
    private final String RECOMMENDER = "Recommender";

    /**
     * Key for retrieving event state information from hazard event dict meta
     * data.
     */
    private final String EVENT_STATE = "eventState";

    /**
     * Meta data hazard dictionary key.
     */
    private final String META_DATA = "metaData";

    /**
     * Logging mechanism.
     */
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(HazardServicesMessageHandler.class);

    // Private Static Variables

    /**
     * Interface to SessionManager (via proxy).
     */
    private static ModelDecorator model = null;

    /**
     * JEP connection. Allows Java to run Python and Python to run Java.
     */
    private static Jep jep;

    private static String caveMode;

    private static String siteID;

    /**
     * Resolved path of SessionManager code.
     */
    private static String PATH_TO_SESSION_MANAGER = null;
    static {

        // Try retrieving the serverCode directory from the
        Bundle sessionManagerBundle = Platform
                .getBundle(Utilities.SESSION_MANAGER_PLUGIN);

        statusHandler.debug("Utilities.SESSION_MANAGER_PLUGIN: "
                + Utilities.SESSION_MANAGER_PLUGIN);
        statusHandler.debug("sessionManagerBundle: " + sessionManagerBundle);

        if (sessionManagerBundle != null) {

            // The plug-in has been loaded. Try to find the serverCode
            // directory.
            try {

                // Look here first. Calling getEntry twice is more
                // efficient that calling find entries once.
                URL sessionManagerURL = sessionManagerBundle
                        .getEntry(File.separator + "src" + File.separator
                                + "sessionManager");

                statusHandler.debug("sessionManagerURL: " + sessionManagerURL);

                if (sessionManagerURL != null) {
                    PATH_TO_SESSION_MANAGER = FileLocator.resolve(
                            sessionManagerURL).getPath();

                    statusHandler.debug("PATH_TO_SESSION_MANAGER: "
                            + PATH_TO_SESSION_MANAGER);
                }

            } catch (IOException e) {
                statusHandler.error(
                        "Could not resolve path to session manager.", e);
            }
        }

        if (PATH_TO_SESSION_MANAGER == null) {
            HazardServicesAppBuilder
                    .warnUser("You cannot run Hazard Services because your system is "
                            + "not configured correctly. Plug-in "
                            + Utilities.SESSION_MANAGER_PLUGIN
                            + File.separator
                            + "src"
                            + File.separator
                            + "sessionManager could not be found.");

        }
    }

    /**
     * An instance of the Hazard Services app builder.
     */
    private HazardServicesAppBuilder appBuilder = null;

    // Public Static Methods

    /**
     * Get the model proxy.
     * 
     * @return Model proxy.
     */
    public static IHazardServicesModel getModelProxy() {
        return model;
    }

    /**
     * This method converts an array of String event ids to a string with the a
     * JSON list format, e.g. "[1,2,3,4]"
     * 
     * @param eventIDs
     *            A array of Strings representing event ids.
     * @return a string with a JSON list format of event ids.
     */
    public static String convertEventIDs(final String[] eventIDs) {
        StringBuffer eventIDstring = new StringBuffer();
        eventIDstring.append('[');

        for (String eventID : eventIDs) {
            eventIDstring.append("\"" + eventID + "\"");
            eventIDstring.append(',');
        }

        if (eventIDs.length > 0) {
            int index = eventIDstring.lastIndexOf(",");
            eventIDstring.deleteCharAt(index);
        }

        eventIDstring.append(']');

        return eventIDstring.toString();
    }

    // Private Static Methods

    /**
     * Instantiates Hazard Services model class for subsequent calls
     * 
     * @return A Model object.
     */
    private static IHazardServicesModel instantiateModel() {
        try {
            /*
             * Retrieve the class loader used to find classes in this plug-in.
             * This needs to be passed into JEP so that it can find the java
             * classes it needs.
             */
            ClassLoader loader = HazardServicesActivator.getDefault()
                    .getClass().getClassLoader();
            String jepIncludePath = buildJepIncludePath();
            jep = new Jep(false, jepIncludePath, loader);
            jep.eval("import JavaImporter");
            jep.runScript(PATH_TO_SESSION_MANAGER + File.separator
                    + "SessionManager.py");

            IHazardServicesModel model = (IHazardServicesModel) jep
                    .invoke("getProxy");
            return model;
        } catch (JepException e) {
            statusHandler.error("Error building Hazard Services JEP interface",
                    e);
            return null;
        }
    }

    /**
     * Builds the JEP include path. This includes the paths to all of the python
     * resources that Hazard Services will need. This removes dependence from
     * the PYTHONPATH environment variable.
     * 
     * @param
     * @return An path like string containing all of the python resources needed
     *         by Hazard Services.
     */
    static String[] pythonLocalizationDirectories = { "python",
            "python" + File.separator + "bridge",
            "python" + File.separator + "dataStorage",
            "python" + File.separator + "events",
            "python" + File.separator + "geoUtilities",
            "python" + File.separator + "logUtilities",
            "python" + File.separator + "shapeUtilities",
            "python" + File.separator + "textUtilities",
            "python" + File.separator + "VTECutilities",
            "python" + File.separator + "events" + File.separator + "utilities" };

    private static String buildJepIncludePath() {
        try {

            Bundle bundle = Platform.getBundle(Utilities
                    .getSessionManagerPlugin());
            statusHandler.debug("buildJepIncludePath: "
                    + Utilities.getSessionManagerPlugin());
            File file = new File(FileLocator.resolve(bundle.getResource("src"))
                    .getPath());
            file = file.getParentFile().getParentFile().getParentFile();

            /**
             * A real hack. The other directories are not plugins so we can't
             * get them using getBundle. Just navigate to them.
             */
            String basePath = file.getPath();
            List<String> sourcePaths = Utilities
                    .buildPythonSourcePaths(basePath);

            for (String locPath : pythonLocalizationDirectories) {
                String path = Utilities.buildUtilitiesPath(locPath);
                sourcePaths.add(path);
            }

            return PyUtil.buildJepIncludePath(sourcePaths
                    .toArray(new String[0]));
        } catch (IOException e) {
            statusHandler.error("Error building Hazard Services JEP interface",
                    e);
            return null;
        }
    }

    // Public Constructors
    /**
     * Construct a standard instance.
     * 
     * @param appBuilder
     *            A reference to the Hazard Services app builder
     * @param currentTime
     *            The current time in milliseconds, based on the CAVE current
     *            time.
     * @param staticSettingID
     *            The identifier for the setting to load.
     * @param dynamicSettingJSON
     *            Settings related to configurations the user has made but has
     *            not save to a static settings file.
     * @param state
     *            Saved session state to initialize this session from the
     *            previous session.
     * 
     */
    public HazardServicesMessageHandler(HazardServicesAppBuilder appBuilder,
            String currentTime, String staticSettingID,
            String dynamicSettingJSON, String state) {
        this.appBuilder = appBuilder;
        model = new ModelDecorator(instantiateModel());

        IHazardEventManager hazardEventManager = new HazardEventManager(
                HazardEventManager.Mode.PRACTICE);
        model.setHazardEventManager(hazardEventManager);

        caveMode = (CAVEMode.getMode()).toString();
        siteID = LocalizationManager.getInstance().getCurrentSite();

        model.initialize(currentTime, currentTime, staticSettingID,
                dynamicSettingJSON, caveMode, siteID, state);
    }

    // Methods

    /**
     * This method is called when the Selected Event is changed on a component:
     * the Spatial Display by clicking on an event area OR the Temporal Display
     * by clicking on an event label OR a Recommender creating a new event.
     * 
     * @param eventIDs
     *            The selected event(s)
     * @param multipleSelection
     *            whether or not this is a part of a multiple selection action
     * @param originator
     *            Where this action originated from
     * 
     * @throws VizException
     */
    public void updateSelectedEvents(final String eventIDs,
            boolean multipleSelection, String originator) throws VizException {
        Boolean found = model.checkForEventsWithState(eventIDs,
                Utilities.HAZARD_EVENT_STATE_POTENTIAL);

        if (!found) {

            // Run any selectionCallback tools associated with the selected
            // events
            String toolList = model.getEventValues(eventIDs,
                    SELECTION_CALLBACK, JSON,
                    Utilities.HAZARD_EVENT_STATE_POTENTIAL);
            DictList tools = DictList.getInstance(toolList);
            for (int i = 0; i < tools.size(); i++) {
                String toolName = tools.getDynamicallyTypedValue(i);
                runTool(toolName);
            }
        }

        String updateTime = model.updateSelectedEvents(eventIDs,
                multipleSelection, originator);

        if (updateTime.contains(SINGLE_SELECTED_TIME)) {
            appBuilder.notifyModelChanged(EnumSet
                    .of(IHazardServicesModel.Element.SELECTED_TIME));
            updateCaveSelectedTime();
        }
        if (updateTime.contains(RANGE_OF_SELECTED_TIMES)) {
            appBuilder.notifyModelChanged(EnumSet
                    .of(IHazardServicesModel.Element.SELECTED_TIME_RANGE));
        }

        notifyModelEventsChanged();
        appBuilder.showHazardDetail();
        if (originator.equals(HazardServicesAppBuilder.TEMPORAL_ORIGINATOR)) {
            appBuilder.recenterRezoomDisplay();
        }

    }

    /**
     * This method runs a tool chosen from the Toolbar. If parameters must be
     * gathered from the user, this method will do so. If not, it will simply
     * run the tool.
     * 
     * @param toolName
     *            The name of the tool to run.
     */
    public void runTool(String toolName) {

        // Check if this tool requires user input from the display...
        String spatialInputJSON = model.getSpatialInfo(toolName);
        String dialogInputJSON = model.getDialogInfo(toolName);

        if (spatialInputJSON != null || dialogInputJSON != null) {
            if (spatialInputJSON != null) {
                // This will generally need to be asynchronous
                processSpatialInput(toolName, spatialInputJSON);
            }

            if (dialogInputJSON != null) {
                // If the dialog dictionary is non-empty, display the
                // subview for gathering tool parameters.
                if (!dialogInputJSON.equals("")) {
                    appBuilder.showToolParameterGatherer(toolName,
                            dialogInputJSON);
                }
            }
        } else {
            // Otherwise, just run the tool.
            runTool(toolName, null, null);
        }
    }

    /**
     * Updates the CAVE selected time.
     * 
     * @return
     */
    void updateCaveSelectedTime() {
        appBuilder.notifyModelChanged(EnumSet
                .of(IHazardServicesModel.Element.CAVE_TIME));
    }

    /**
     * Send notification to listeners (generally presenters) of hazard events
     * changing in the model.
     */
    void notifyModelEventsChanged() {
        appBuilder.notifyModelChanged(EnumSet
                .of(IHazardServicesModel.Element.EVENTS));
    }

    /**
     * Processes JSON representing spatial input required by a tool and
     * determines what action is needed to retrieve that spatial input. For
     * example, the storm track tool needs a drag/drop dot to mark the location
     * of a storm. Based on this, this routine will load the Drag Drop mouse
     * handler to retrieve this input.
     * 
     * @param toolName
     *            The name of the tool being run.
     * @param spatialInput
     *            JSON string describing the type of spatial input required.
     */
    private void processSpatialInput(String toolName, String spatialInput) {
        if (spatialInput != null) {
            Dict spatialDict = Dict.getInstance(spatialInput);

            String returnType = (String) spatialDict.get(RETURN_TYPE);

            if (returnType.equals(POINT_RETURN_TYPE)) {
                String label = (String) spatialDict.get(HAZARD_LABEL);

                /*
                 * Activate the storm tracking mouse handler
                 */
                appBuilder.requestMouseHandler(
                        HazardServicesMouseHandlers.DRAG_DROP_DRAWING,
                        toolName, label);
            }
        }
    }

    /**
     * This method is called when a tool is run and parameters have already been
     * collected for the tool execution.
     * 
     * @param toolName
     *            The name of the tool to run
     * @param sourceKey
     *            The source of the included json
     * @param json
     *            The json to pass to the tool as run data.
     * 
     * @throws VizException
     */
    public void runTool(String toolName, String sourceKey, String json) {

        if (sourceKey != null && json != null) {
            json = "{ \"" + sourceKey + "\":" + json + "}";
        }

        IHazardServicesModel sessionManager = model;
        appBuilder.setCursor(SpatialViewCursorTypes.WAIT_CURSOR);
        String resultJSON = null;

        while (Display.getCurrent().readAndDispatch()) {

            // No action.
        }
        Display.getCurrent().update();

        // Send the latest frames to the session manager for the tool
        // to use.
        resultJSON = sessionManager.runTool(toolName, json);
        appBuilder.setCursor(SpatialViewCursorTypes.MOVE_POINT_CURSOR);

        notifyModelEventsChanged();

        if (resultJSON != null) {

            Dict resultDict = Dict.getInstance(resultJSON);
            Dict metaData = (Dict) resultDict.get(META_DATA);

            /*
             * 
             * If the return type is Graph Data, launch the grapher.
             */
            if (metaData != null) {
                String returnType = (String) metaData.get(RETURN_TYPE);
                String eventState = (String) metaData.get(EVENT_STATE);

                if (returnType.equals(EVENT_DICTS_RETURN_TYPE)) {
                    if (eventState != null
                            && eventState
                                    .equalsIgnoreCase(Utilities.HAZARD_EVENT_STATE_POTENTIAL)) {
                        notifyModelEventsChanged();
                    } else {
                        String eventIDs = sessionManager.getSelectedEvents();

                        if (eventIDs != "[]") {
                            try {
                                updateSelectedEvents(eventIDs, false,
                                        RECOMMENDER);

                            } catch (VizException e) {
                                statusHandler
                                        .error("Error updating Hazard Services Spatial Display",
                                                e);
                            }
                        }
                    }

                }

            }
        }

    }

    /**
     * Receives notification that the results of an asynchronous recommender
     * result are now available. This method processes these results, updating
     * the model state and notifying presenters of new events.
     * 
     * @param recommenderID
     *            The name of the recommender
     * @param eventList
     *            A list of recommended events
     * @return
     */
    void handleRecommenderResults(String recommenderID,
            final List<IEvent> eventList) {

        String resultJSON = model.handleRecommenderResult(recommenderID,
                eventList);

        notifyModelEventsChanged();

        if (resultJSON != null) {

            Dict resultDict = Dict.getInstance(resultJSON);
            Dict metaData = (Dict) resultDict.get(META_DATA);

            /*
             * 
             * If the return type is Graph Data, launch the grapher.
             */
            if (metaData != null) {
                String returnType = (String) metaData.get(RETURN_TYPE);
                String eventState = (String) metaData.get(EVENT_STATE);

                if (returnType.equals(EVENT_DICTS_RETURN_TYPE)
                        || returnType.equals(IEVENT_LIST_RETURN_TYPE)) {
                    if (eventState != null
                            && eventState
                                    .equalsIgnoreCase(Utilities.HAZARD_EVENT_STATE_POTENTIAL)) {
                        notifyModelEventsChanged();
                    } else {
                        String eventIDs = model.getSelectedEvents();

                        if (eventIDs != "[]") {
                            try {
                                updateSelectedEvents(eventIDs, false,
                                        RECOMMENDER);

                            } catch (VizException e) {
                                statusHandler
                                        .error("Error updating Hazard Services Spatial Display",
                                                e);
                            }
                        }
                    }

                }

            }
        }

    }

    public void handleProductGeneratorResult(String toolID,
            final List<IGeneratedProduct> productList) {

        String resultJSON = model.handleProductGeneratorResult(toolID,
                productList);

        if (resultJSON != null) {

            Dict resultDict = Dict.getInstance(resultJSON);
            String returnType = (String) resultDict.get(RETURN_TYPE);

            if (returnType == null) {
                /*
                 * Need to make sure that the hazard services views are updated
                 * to reflect new hazard state.
                 */
                notifyModelEventsChanged();
                return;
            }

            // Preview the products
            appBuilder.showProductEditorView(resultJSON);
        }

    }

    /**
     * Shuts down the Hazard Services session.
     */
    public void closeHazardServices() {
        appBuilder.dispose();
    }

    /**
     * This method is called when events are deleted. This operation can only be
     * performed on Pending or Proposed events.
     * 
     * @param eventIDs
     *            Identifiers of events to be deleted.
     */
    private void deleteEvent(String eventIDs) {
        statusHandler.debug("HazardServicesMessageHandler: deleteEvent: "
                + eventIDs);

        model.deleteEvent(eventIDs);
        notifyModelEventsChanged();
        appBuilder.hideHazardDetail();
    }

    /**
     * Changes the state of the selected events to the state given by the state
     * parameter
     * 
     * @param state
     *            New state of the selected events.
     */
    private void changeState(String state) {
        String events = model.getSelectedEvents();

        model.changeState(events, state);

        notifyModelEventsChanged();
        appBuilder.hideHazardDetail();
    }

    /**
     * Issues the events upon user confirmation.
     */
    private void issueEvents() {
        if (HazardServicesAppBuilder.getUserAnswerToQuestion("Are you sure "
                + "you want to issue the hazard event(s)?")) {
            generateProducts(true);
            notifyModelEventsChanged();
        }
    }

    /**
     * Launch the Staging Dialog if necessary OR return the Generated Products
     * 
     * @param issueFlag
     *            Flag indicating whether or not this is the result of an issue
     *            action.
     * @return Products that were generated.
     */
    private void generateProducts(boolean issueFlag) {

        String issue = FALSE_FLAG;

        if (issueFlag) {
            issue = TRUE_FLAG;
        }

        /*
         * Get StagingInfo OR GeneratedProducts
         */
        String returnDict_json = model.createProductsFromEventIDs(issue);

        Dict returnDict = Dict.getInstance(returnDict_json);

        /*
         * If returnDict's returnType is stagingInfo, invoke the Product Staging
         * Dialog
         */
        if (returnDict.get(RETURN_TYPE).equals(STAGING_INFO_RETURN_TYPE)) {
            appBuilder.showProductStagingView(issueFlag, returnDict);
        }
    }

    /**
     * This method is called when a storm track point is moved on the Spatial
     * Display OR when a polygon is changed by the Cave user
     * 
     * Appropriate adjustments are made to the event and then the Spatial
     * Display is re-drawn
     * 
     * @param jsonText
     *            A JSON string containing a dict to replace portions of the
     *            event with.
     * @throws VizException
     */
    public void modifySpatialDisplayObject(String jsonText) throws VizException {
        model.modifyEventArea(jsonText);
        appBuilder.notifyModelChanged(EnumSet
                .of(IHazardServicesModel.Element.EVENTS));
    }

    /**
     * This method is called when the user clicks "Reset" on the Tool bar to
     * reset the hazards to the canned case for the given setting.
     * 
     * @param type
     *            Type of entities to reset. *
     */
    public void reset(String type) {
        model.reset(type);

        String perspectiveID = appBuilder.getCurrentPerspectiveDescriptor()
                .getId();

        if (perspectiveID.contains(D2D_PERSPECTIVE)) {
            appBuilder.setInitialSetting(TOR_SETTING);
        } else if (perspectiveID.contains(GFE_PERSPECTIVE)) {
            appBuilder.setInitialSetting(WSW_SETTING);
        } else if (perspectiveID.contains(HYDRO_PERSPECTIVE)) {
            appBuilder.setInitialSetting(FLOOD_SETTING);
        }

        changeSetting(appBuilder.getInitialSetting(), false, true);
    }

    /**
     * Changes the setting to the new setting identifier.
     * 
     * @param settingID
     *            New setting to be used.
     * @param saveEvents
     *            Flag indicating whether or not to save existing events.
     * @param eventsChanged
     *            Flag indicating whether or not hazard events have changed as
     *            part of this change.
     */
    public void changeSetting(String settingID, boolean saveEvents,
            boolean eventsChanged) {

        model.initialize(model.getSelectedTime(), model.getCurrentTime(),
                settingID, "", caveMode, siteID, model.getState(saveEvents));

        appBuilder.notifyModelChanged(eventsChanged ? EnumSet.of(
                IHazardServicesModel.Element.EVENTS,
                IHazardServicesModel.Element.SETTINGS,
                IHazardServicesModel.Element.TOOLS,
                IHazardServicesModel.Element.VISIBLE_TIME_DELTA,
                IHazardServicesModel.Element.DYNAMIC_SETTING) : EnumSet.of(
                IHazardServicesModel.Element.SETTINGS,
                IHazardServicesModel.Element.TOOLS,
                IHazardServicesModel.Element.VISIBLE_TIME_DELTA,
                IHazardServicesModel.Element.DYNAMIC_SETTING));
    }

    /**
     * Updates the selected time either in CAVE or the Console. Selected time
     * updates in CAVE appear in the Console. Selected time updates in the
     * Console appear in CAVE.
     * 
     * @param selectedTime_ms
     *            The new selected time in milliseconds
     * @param originator
     *            Where the selected time was changed. Can be "CAVE_ORIGINATOR"
     *            for CAVE or "TEMPORAL_ORIGINATOR" for the Console.
     */
    public void updateSelectedTime(String selectedTime_ms, String originator)
            throws VizException {
        model.updateSelectedTime(selectedTime_ms);

        if (originator.equals(HazardServicesAppBuilder.CAVE_ORIGINATOR)) {
            appBuilder.notifyModelChanged(EnumSet
                    .of(IHazardServicesModel.Element.SELECTED_TIME));
        }

        if (originator.equals(HazardServicesAppBuilder.TEMPORAL_ORIGINATOR)) {
            updateCaveSelectedTime();
        }
    }

    /**
     * Update the selected time range.
     * 
     * @param selectedTimeStart_ms
     *            Start of the selected time range, or -1 if no range exists.
     * @param selectedTimeEnd_ms
     *            End of the selected time range, or -1 if no range exists.
     * @param originator
     *            Originator of this update.
     */
    public void updateSelectedTimeRange(String selectedTimeStart_ms,
            String selectedTimeEnd_ms, String originator) {
        model.updateSelectedTimeRange(selectedTimeStart_ms, selectedTimeEnd_ms);
        appBuilder.notifyModelChanged(EnumSet
                .of(IHazardServicesModel.Element.SELECTED_TIME_RANGE));
    }

    /**
     * This method is called when the visible time range is changed in the
     * Temporal Window.
     */
    public void updateVisibleTimeRange(String jsonStartTime,
            String jsonEndTime, String originator) {
        model.setTimeLineVisibleTimes(jsonStartTime, jsonEndTime);
        appBuilder.notifyModelChanged(EnumSet
                .of(IHazardServicesModel.Element.VISIBLE_TIME_RANGE));
    }

    /**
     * Set the add to selected mode as specified.
     * 
     * @param state
     *            New state of the add-to-selected mode.
     */
    public void setAddToSelected(String state) {
        model.setAddToSelected(state);
    }

    /**
     * Updates information for an event taking into consideration the
     * originator.
     * 
     * @param jsonText
     *            The json containing the portions of the event which are being
     *            updated.
     * @param originator
     *            A string representing the CAVE component from which this
     *            request originated.
     * @return
     */
    public void updateEventData(String jsonText, String originator) {
        model.updateEventData(jsonText, originator);
        appBuilder.notifyModelChanged(EnumSet
                .of(IHazardServicesModel.Element.EVENTS));
    }

    /**
     * This method is called when the event type is changed in the Hazard
     * Information Dialog
     * 
     * @param jsonText
     *            Contains information regarding the event type.
     */
    public void updateEventType(String jsonText) {
        model.getSelectedEvents();
        model.updateEventData(jsonText,
                HazardServicesAppBuilder.HAZARD_INFO_ORIGINATOR);
        notifyModelEventsChanged();
    }

    /**
     * Handle the specified action.
     * 
     * @param action
     *            The action name
     * @param jsonText
     *            JSON describing the action.
     * @return
     */
    public void handleAction(String action, String jsonText) {
        model.handleAction(action, jsonText);
    }

    /**
     * Update the specified static setting.
     * 
     * @param jsonSetting
     *            JSON string holding a dictionary of key-value pairs defining
     *            the setting being saved.
     */
    public void updateStaticSetting(String jsonSetting) {
        model.updateStaticSettings(jsonSetting);
    }

    /**
     * Create a new static setting.
     * 
     * @param jsonSetting
     *            JSON string holding a dictionary of key-value pairs defining
     *            the setting being saved.
     */
    public void createStaticSetting(String jsonSetting) {
        String settingID = model.newStaticSettings(jsonSetting);
        changeSetting(settingID, true, false);
    }

    /**
     * Respond to the dynamic setting having changed.
     * 
     * @param jsonDynamicSetting
     *            JSON string holding the dictionary defining the dynamic
     *            setting.
     */
    public void dynamicSettingChanged(String jsonDynamicSetting) {
        IHazardServicesModel sessionManager = model;
        sessionManager.initialize(sessionManager.getSelectedTime(),
                sessionManager.getCurrentTime(),
                sessionManager.getCurrentSettingsID(), jsonDynamicSetting,
                caveMode, siteID, sessionManager.getState(true));
        appBuilder.notifyModelChanged(EnumSet
                .of(IHazardServicesModel.Element.DYNAMIC_SETTING));
    }

    /**
     * This method is called when a new event is created in the Spatial Display.
     * The originator argument is for extensibility in case the capability to
     * add events is added to other components.
     * 
     * @param eventArea
     *            Json string representing a newly created eventArea
     * @param eventID
     *            The id of the event. If this is null, then a new event and id
     *            are created.
     * @param originator
     *            The originator of the new event (for example, "Spatial"). This
     *            should be an enum.
     * @return An eventID for the new event area.
     */
    public String newEventArea(String eventArea, String eventID,
            String originator) {
        if (eventID == null) {
            eventID = model.newEvent(eventArea);
        }

        try {
            updateSelectedEvents(convertEventIDs(new String[] { eventID }),
                    false, originator);

        } catch (VizException e) {
            statusHandler
                    .error("In HazardServicesMessageHandler: newEventArea(): error updating selected events.",
                            e);
        }

        return eventID;
    }

    /**
     * This method is invoked when the current CAVE time changes. It updates the
     * current time state in the model proxy.
     * 
     * @param currentTime_ms
     *            The new current time value in milliseconds
     * @param originator
     *            The originator of the current time update. For the moment,
     *            this should be set to "Cave". This should be an enumeration
     *            not a string.
     * 
     * @throws VizException
     */
    public void updateCurrentTime(String currentTime_ms, String originator)
            throws VizException {
        model.updateCurrentTime(currentTime_ms);

        if (originator.equals(HazardServicesAppBuilder.CAVE_ORIGINATOR)) {
            appBuilder.notifyModelChanged(EnumSet
                    .of(IHazardServicesModel.Element.CURRENT_TIME));
        }
    }

    /**
     * Generates products for preview.
     */
    public void preview() {
        generateProducts(false);
    }

    /**
     * Convenience method for retrieving the current editor. Each perspective
     * has its own editor. So, when a reference to the current editor is needed,
     * it is safer to query for the current editor than to rely on a stored
     * editor reference.
     * 
     * @return Reference to the current CAVE editor.
     */
    public AbstractEditor getCurrentEditor() {
        return ((AbstractEditor) VizWorkbenchManager.getInstance()
                .getActiveEditor());
    }

    /**
     * Updates the model with CAVE frame information.
     */
    public void sendFrameInformationToSessionManager() {

        AbstractEditor editor = getCurrentEditor();
        FramesInfo framesInfo = editor.getActiveDisplayPane().getDescriptor()
                .getFramesInfo();

        if (framesInfo != null) {
            final int frameCount = framesInfo.getFrameCount();
            final int frameIndex = framesInfo.getFrameIndex();
            DataTime[] dataFrames = framesInfo.getFrameTimes();

            if (frameIndex >= 0) {
                final List<Long> dataTimeList = new ArrayList<Long>();

                if (dataFrames != null) {
                    for (DataTime dataTime : dataFrames) {
                        Calendar cal = dataTime.getValidTime();
                        dataTimeList.add(cal.getTimeInMillis());
                    }
                }

                Dict frameDict = new Dict();
                frameDict.put(FRAME_COUNT, frameCount);
                frameDict.put(FRAME_INDEX, frameIndex);
                frameDict.put(FRAME_TIMES, dataTimeList);

                final String framesJSON = frameDict.toJSONString();

                /*
                 * Need to make sure that the modelProxy is called by the thread
                 * which started JEP. In this case, it is the VizApp thread.
                 */
                VizApp.runAsync(new Runnable() {

                    @Override
                    public void run() {
                        model.updateFrameInfo(framesJSON);

                        /*
                         * Make sure the HazardServices selected time is in-sync
                         * with the frame being viewed.
                         */
                        if ((frameCount > 0) && (frameIndex != -1)) {
                            try {
                                updateSelectedTime(
                                        dataTimeList.get(frameIndex).toString(),
                                        HazardServicesAppBuilder.CAVE_ORIGINATOR);
                            } catch (VizException e) {
                                statusHandler.error(
                                        "HazardServicesMessageHandler:", e);
                            }
                        }
                    }
                });

            }

        }
    }

    /**
     * Handles the selection from the right click context menu on the Hazard
     * Services Spatial Display.
     * 
     * @param label
     *            The label of the selected menu item.
     */
    public void handleContextMenuSelection(String label) {
        if (label.contains(CONTEXT_MENU_PROPOSE)) {
            changeState(Utilities.HAZARD_EVENT_STATE_PROPOSED);
        } else if (label.contains(CONTEXT_MENU_ISSUE)) {
            issueEvents();
        } else if (label.contains(CONTEXT_MENU_END)) {
            String events = model.getSelectedEvents();
            model.changeState(events, PREVIEW_ENDED);
            preview();
        } else if (label.equals(CONTEXT_MENU_DELETE_POINT)) {
            appBuilder.modifyShape(HazardServicesDrawingAction.DELETE_POINT);
        } else if (label.equals(CONTEXT_MENU_ADD_POINT)) {
            appBuilder.modifyShape(HazardServicesDrawingAction.ADD_POINT);
        } else if (label.equals(CONTEXT_MENU_MOVE_ENTIRE_ELEMENT)) {
            appBuilder.modifyShape(HazardServicesDrawingAction.MOVE_ELEMENT);
        } else if (label.contains(CONTEXT_MENU_DELETE)) {
            deleteEvent(model.getSelectedEvents());
        } else if (label.contains(CONTEXT_MENU_HAZARD_INFORMATION_DIALOG)) {
            /*
             * Save off any changes the user has made in the HID. Otherwise,
             * this would be lost when selecting different events.
             */
            appBuilder.showHazardDetail();
        } else if (label.contains(CONTEXT_MENU_REMOVE_POTENTIAL_HAZARDS)) {
            model.removeEvents(Utilities.HAZARD_EVENT_STATE,
                    Utilities.HAZARD_EVENT_STATE_POTENTIAL);
            notifyModelEventsChanged();
        } else if (label.contains(CONTEXT_MENU_SAVE)) {
            model.putHazards();
        } else if (label.equals(CONTEXT_MENU_ADD_REMOVE_SHAPES)) {
            String eventIDs = model.getSelectedEvents();
            appBuilder.loadGeometryOverlayForSelectedEvent(eventIDs);
        } else if (label.equals(CONTEXT_MENU_SEND_TO_BACK)) {
            model.sendSelectedHazardsToBack();
            notifyModelEventsChanged();
        } else if (label.equals(CONETXT_MENU_BRING_TO_FRONT)) {
            model.sendSelectedHazardsToFront();
            notifyModelEventsChanged();
        } else {
            // Check if the selected context menu item is specific to
            // the
            // selected event. Retrieve the callback associated with
            // this context menu item.
            String callback = model.getContextMenuEntryCallback(label);

            if ((callback != null) && (callback.length() > 0)) {
                // For now, delete the event this action was
                // started on...
                model.deleteEvent(model.getSelectedEvents());
                runTool(callback);
            }
        }
    }

    /**
     * Closes the JEP connection and frees up the JEP resources. There may be
     * some leaked memory associated with numpy.
     */
    public void closeJepConnection() {
        if (jep != null) {
            jep.close();
            jep = null;
        }
    }

    /**
     * Changes the state of an event to "Proposed".
     */
    public void setProposedState() {
        changeState(Utilities.HAZARD_EVENT_STATE_PROPOSED);
        appBuilder.hideHazardDetail();
        appBuilder.closeProductEditorView();
    }

    /**
     * Changes the state of an event to "Issued".
     */
    public void setIssuedState() {
        issueEvents();
        appBuilder.closeProductEditorView();
    }

    /**
     * Sets the state of an event to "Dismissed".
     * 
     * @param
     * @return
     */
    public void setDismissedState() {
        appBuilder.hideHazardDetail();
        appBuilder.closeProductEditorView();
    }

    /**
     * Changes the state of an event based on an action taken by the user in the
     * Product Display dialog.
     * 
     * @param action
     *            The action the user took on the event (probably needs to be an
     *            enumeration).
     */
    public void handleProductDisplayAction(ProductEditorAction action) {
        String productDisplayAction = action.getAction();
        String productDisplayJSON = action.getJSONText();

        if (productDisplayAction.equals(CONTEXT_MENU_PROPOSE)) {
            changeState(Utilities.HAZARD_EVENT_STATE_PROPOSED);
        } else if (productDisplayAction.equals(CONTEXT_MENU_ISSUE)) {
            if (HazardServicesAppBuilder
                    .getUserAnswerToQuestion("Are you sure "
                            + "you want to issue the hazard event(s)?")) {
                model.createProductsFromHazardEventSets(TRUE_FLAG,
                        productDisplayJSON);
                notifyModelEventsChanged();
            }
        }

        appBuilder.closeProductEditorView();

    }

    /**
     * Handles the product display dialog continue action.
     * 
     * @param issueFlag
     *            Flag indicating whether or not this is the result of an issue.
     * @param hazardEventSets
     *            Hazard events from which to create products.
     */
    public void handleProductDisplayContinueAction(String issueFlag,
            String hazardEventSets) {
        String returnDict_json = model.createProductsFromHazardEventSets(
                issueFlag, hazardEventSets);

        if (!issueFlag.equalsIgnoreCase(TRUE_FLAG)) {
            appBuilder.showProductEditorView(returnDict_json);
            notifyModelEventsChanged();
        }
    }

    /**
     * Handles a timer action.
     * 
     * @param caveTime
     *            CAVE time.
     */
    public void handleTimerAction(Date caveTime) {
        if (appBuilder.getTimer().isAlive()
                && !appBuilder.getTimer().isInterrupted()) {

            long caveTimeMS = caveTime.getTime();

            try {
                updateCurrentTime(Long.toString(caveTimeMS),
                        HazardServicesAppBuilder.CAVE_ORIGINATOR);
            } catch (VizException e) {
                statusHandler.error(
                        "Error updating Hazard Services components", e);
            }
        }
    }

    /**
     * Update the visible time delta.
     */
    public void updateConsoleVisibleTimeDelta() {
        appBuilder.notifyModelChanged(EnumSet
                .of(IHazardServicesModel.Element.VISIBLE_TIME_DELTA));
    }

    /**
     * Request that a mouse handler be loaded.
     * 
     * @param mouseHandler
     *            Mouse handler to be loaded.
     * @param args
     *            Additional optional arguments.
     */
    public void requestMouseHandler(HazardServicesMouseHandlers mouseHandler,
            String... args) {
        appBuilder.requestMouseHandler(mouseHandler, args);
    }

    /**
     * Get benchmarking stats for display.
     * 
     * @return Benchmarking stats suitable for display.
     */
    public String getBenchmarkingStats() {
        return model.getBenchmarkingStats();
    }

}

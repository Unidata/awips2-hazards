/**
 * This software was developed and / or modified by the National Oceanic and
 * Atmospheric Administration (NOAA), Earth System Research Laboratory (ESRL),
 * Global Systems Division (GSD), Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.display;

import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.*;
import gov.noaa.gsd.viz.hazards.display.action.ModifyHazardGeometryAction;
import gov.noaa.gsd.viz.hazards.display.action.ModifyStormTrackAction;
import gov.noaa.gsd.viz.hazards.display.action.NewHazardAction;
import gov.noaa.gsd.viz.hazards.display.action.ProductEditorAction;
import gov.noaa.gsd.viz.hazards.jsonutilities.Dict;
import gov.noaa.gsd.viz.hazards.pythonjoblistener.HazardServicesRecommenderJobListener;
import gov.noaa.gsd.viz.hazards.spatialdisplay.HazardServicesDrawingAction;
import gov.noaa.gsd.viz.hazards.spatialdisplay.HazardServicesMouseHandlers;
import gov.noaa.gsd.viz.hazards.spatialdisplay.SpatialView.SpatialViewCursorTypes;
import gov.noaa.gsd.viz.hazards.utilities.Utilities;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.eclipse.swt.widgets.Display;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.raytheon.uf.common.dataplugin.events.EventSet;
import com.raytheon.uf.common.dataplugin.events.IEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardState;
import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.HazardEventManager;
import com.raytheon.uf.common.dataplugin.events.hazards.event.BaseHazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardEventUtilities;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.hazards.productgen.IGeneratedProduct;
import com.raytheon.uf.common.python.concurrent.IPythonJobListener;
import com.raytheon.uf.common.recommenders.AbstractRecommenderEngine;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.ISimulatedTimeChangeListener;
import com.raytheon.uf.common.time.SimulatedTime;
import com.raytheon.uf.common.time.TimeRange;
import com.raytheon.uf.viz.core.VizApp;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.localization.LocalizationManager;
import com.raytheon.uf.viz.hazards.sessionmanager.ISessionManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.ISessionConfigurationManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Settings;
import com.raytheon.uf.viz.hazards.sessionmanager.events.ISessionEventManager;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventsModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.impl.ObservedHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.time.ISessionTimeManager;
import com.raytheon.uf.viz.hazards.sessionmanager.time.SelectedTimeChanged;
import com.raytheon.viz.core.mode.CAVEMode;
import com.raytheon.viz.ui.perspectives.VizPerspectiveListener;

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
 * Jun 24, 2013            bryon.lawrence      Removed the 'Move Entire Element'
 *                                             option from the right-click context
 *                                             menu.
 * Jul 19, 2013   1257     bsteffen            Notification support for session manager.
 * Jul 20, 2013    585     Chris.Golden        Changed to support loading from bundle,
 *                                             including making the model and JEP
 *                                             instances be member variables instead
 *                                             of class-scoped.
 * Jul 18, 2013   1264     Chris.Golden        Added support for drawing lines and
 *                                             points.
 * Aug 06, 2013   1265     bryon.lawrence      Added support for undo/redo.
 * Aug  9, 2013 1921       daniel.s.schaffer@noaa.gov  Support of replacement of JSON with POJOs
 * Aug 21, 2013 1921       daniel.s.schaffer@noaa.gov  Call recommender framework directly
 * Aug 22, 2013    787     bryon.lawrence      Added method to find setting linked to
 *                                             the current CAVE perspective.
 * Aug 29, 2013 1921       bryon.lawrence      Modified to not pass JSON event id list
 *                                             to loadGeometryOverlayForSelectedEvent().
 * Aug 30, 2013 1921       bryon.lawrence      Added code to pass hazard events as a part of
 *                                             the EventSet passed to a recommender when it
 *                                             is run.
 * Oct 22, 2013 1463       bryon.lawrence      Added methods for hazard 
 *                                             conflict detection.
 * Nov 04, 2013 2182     daniel.s.schaffer@noaa.gov      Started refactoring
 * Nov 15, 2013  2182       daniel.s.schaffer@noaa.gov    Refactoring JSON - ProductStagingDialog
 * Nov 20, 2013 2460    daniel.s.schaffer@noaa.gov  Reset now removing all events from practice table
 * Nov 16, 2013  2166       daniel.s.schaffer@noaa.gov    Tidying
 * Nov 27, 2013  1462      bryon.lawrence      Added methods to support display
 *                                             of hazard hatch areas.
 * nov 29, 2013  2378      bryon.lawrence     Cleaned up methods which support proposing and issuing hazards.
 * Dec 3, 2013   1472      bkowal              subtype field is now subType
 * Dec 03, 2013 2182 daniel.s.schaffer@noaa.gov Refactoring - eliminated IHazardsIF
 * Dec 08, 2013 2539       bryon.lawrence     Updated to ensure current time 
 *                                            indicate immediately reflects
 *                                            user changes to CAVE clock.
 * 
 * Dec 08, 2013 2155       bryon.lawrence     Removed logic in runTool which
 *                                            which seemed to be leading
 *                                            to an occasional race condition.
 * Dec 08, 2013 2375       bryon.lawrence     Added code to add updated hazard type to
 *                                            dynamic settings.
 * </pre>
 * 
 * @author bryon.lawrence
 * @version 1.0
 */
public final class HazardServicesMessageHandler implements
        ISimulatedTimeChangeListener {

    // Private Constants

    /**
     * The key for a hazard's label in a hazard event dict.
     */
    private final String HAZARD_LABEL = "label";

    /**
     * Possible return types for hazard dictionaries
     */
    private final String RETURN_TYPE = "returnType";

    private final String POINT_RETURN_TYPE = "Point";

    /**
     * Indicates that a range of selected times has been updated.
     */
    private final String RANGE_OF_SELECTED_TIMES = "Range";

    /**
     * Indicates that a single selected time has been updated.
     */
    private final String SINGLE_SELECTED_TIME = "Single";

    // Private Static Constants

    /**
     * Logging mechanism.
     */
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(HazardServicesMessageHandler.class);

    // Private Variables

    private final String caveMode;

    private final String siteID;

    /**
     * An instance of the Hazard Services app builder.
     */
    private HazardServicesAppBuilder appBuilder = null;

    private final ISessionManager sessionManager;

    private final ISessionEventManager sessionEventManager;

    private final ISessionTimeManager sessionTimeManager;

    private final ISessionConfigurationManager sessionConfigurationManager;

    private final ObjectMapper jsonObjectMapper = new ObjectMapper();

    private final HazardServicesProductGenerationHandler productGeneratorHandler;

    // Public Static Methods

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

    // Public Constructors
    /**
     * Construct a standard instance.
     * 
     * @param appBuilder
     *            A reference to the Hazard Services app builder
     * @param currentTime
     *            The current time, based on the CAVE current time.
     * @param dynamicSettingJSON
     *            Settings related to configurations the user has made but has
     *            not save to a static settings file.
     * @param eventBus
     * @param state
     *            Saved session state to initialize this session from the
     *            previous session.
     * 
     */
    @SuppressWarnings("deprecation")
    public HazardServicesMessageHandler(HazardServicesAppBuilder appBuilder,
            Date currentTime, String dynamicSettingJSON, EventBus eventBus) {
        this.appBuilder = appBuilder;
        this.sessionManager = appBuilder.getSessionManager();

        this.productGeneratorHandler = new HazardServicesProductGenerationHandler(
                sessionManager, appBuilder.getEventBus());
        this.sessionEventManager = sessionManager.getEventManager();
        this.sessionTimeManager = sessionManager.getTimeManager();
        this.sessionConfigurationManager = sessionManager
                .getConfigurationManager();

        /*
         * TODO Need to consolidate event buses
         */
        sessionManager.registerForNotification(this);
        eventBus.register(this);

        caveMode = (CAVEMode.getMode()).toString();
        siteID = LocalizationManager.getInstance().getCurrentSite();

        String staticSettingID = getSettingForCurrentPerspective();

        sessionManager.initialize(currentTime, staticSettingID,
                dynamicSettingJSON, caveMode, siteID);
        SimulatedTime.getSystemTime().addSimulatedTimeChangeListener(this);
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
    public void updateSelectedEvents(final List<String> eventIDs,
            String originator) throws VizException {
        String updateTime = setSelectedEvents(eventIDs, originator);

        if (updateTime.contains(SINGLE_SELECTED_TIME)) {
            appBuilder.notifyModelChanged(EnumSet
                    .of(HazardConstants.Element.SELECTED_TIME));
            updateCaveSelectedTime();
        }
        if (updateTime.contains(RANGE_OF_SELECTED_TIMES)) {
            appBuilder.notifyModelChanged(EnumSet
                    .of(HazardConstants.Element.SELECTED_TIME_RANGE));
        }

        notifyModelEventsChanged();
        appBuilder.showHazardDetail();
        if (originator.equals(HazardServicesAppBuilder.TEMPORAL_ORIGINATOR)) {
            appBuilder.recenterRezoomDisplay();
        }

    }

    private String setSelectedEvents(List<String> eventIDs, String originator) {
        Collection<IHazardEvent> selectedEvents = fromIDs(eventIDs);
        Date selectedTime = sessionTimeManager.getSelectedTime();

        sessionEventManager.setSelectedEvents(selectedEvents);
        if (originator.equalsIgnoreCase("Temporal")
                && !selectedTime.equals(sessionTimeManager.getSelectedTime())) {
            return "Single";
        } else {
            return "None";
        }
    }

    private Collection<IHazardEvent> fromIDs(List<String> eventIDs) {
        Collection<IHazardEvent> events = new ArrayList<IHazardEvent>();
        for (String eventId : eventIDs) {
            events.add(sessionEventManager.getEventById(eventId));
        }
        return events;
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
        AbstractRecommenderEngine<?> recommenderEngine = appBuilder
                .getSessionManager().getRecommenderEngine();

        // Check if this tool requires user input from the display...
        Map<String, Serializable> spatialInput = recommenderEngine
                .getSpatialInfo(toolName);
        Map<String, Serializable> dialogInput = recommenderEngine
                .getDialogInfo(toolName);

        if (!spatialInput.isEmpty() || !dialogInput.isEmpty()) {
            if (!spatialInput.isEmpty()) {
                // This will generally need to be asynchronous
                processSpatialInput(toolName, spatialInput);
            }

            if (!dialogInput.isEmpty()) {
                // If the dialog dictionary is non-empty, display the
                // subview for gathering tool parameters.
                if (!dialogInput.isEmpty()) {
                    appBuilder.showToolParameterGatherer(toolName, dialogInput);
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
                .of(HazardConstants.Element.CAVE_TIME));
    }

    /**
     * Send notification to listeners (generally presenters) of hazard events
     * changing in the model.
     */
    void notifyModelEventsChanged() {
        appBuilder.notifyModelChanged(EnumSet
                .of(HazardConstants.Element.EVENTS));
    }

    /**
     * Processes spatial input required by a tool and determines what action is
     * needed to retrieve that spatial input. For example, the storm track tool
     * needs a drag/drop dot to mark the location of a storm. Based on this,
     * this routine will load the Drag Drop mouse handler to retrieve this
     * input.
     * 
     * @param toolName
     *            The name of the tool being run.
     * @param spatialInput
     *            the type of spatial input required.
     */
    private void processSpatialInput(String toolName,
            Map<String, Serializable> spatialInput) {

        String returnType = (String) spatialInput.get(RETURN_TYPE);

        if (returnType.equals(POINT_RETURN_TYPE)) {
            String label = (String) spatialInput.get(HAZARD_LABEL);

            /*
             * Activate the storm tracking mouse handler
             */
            appBuilder.requestMouseHandler(
                    HazardServicesMouseHandlers.STORM_TOOL_DRAG_DOT_DRAWING,
                    toolName, label);
        }

    }

    /**
     * This method is called when a tool is run and parameters have already been
     * collected for the tool execution.
     * 
     * @param toolName
     *            The name of the tool to run
     * @param sourceKey
     *            The source of the runData
     * @param spatialInfo
     *            Spatial info to pass to the tool.
     * @param dialogInfo
     *            Dialog info to pass to the tool.
     * 
     * @throws VizException
     */
    public void runTool(String toolName, Map<String, Serializable> spatialInfo,
            Dict dialogInfo) {

        ISessionManager sessionManager = appBuilder.getSessionManager();
        appBuilder.setCursor(SpatialViewCursorTypes.WAIT_CURSOR);
        Display.getCurrent().update();

        EventSet<IEvent> eventSet = new EventSet<IEvent>();

        /*
         * Add events to the event set.
         */
        Collection<IHazardEvent> hazardEvents = sessionManager
                .getEventManager().getEvents();

        /*
         * HazardEvent.py canConvert() does not know anything about observed
         * hazard events. Also, HazardEvent.py is in a common plug-in while
         * observed hazard event is in a viz plug-in. So, convert the observed
         * hazard events to base hazard events.
         */
        for (IHazardEvent event : hazardEvents) {
            BaseHazardEvent baseHazardEvent = new BaseHazardEvent(event);
            eventSet.add(baseHazardEvent);
        }

        /*
         * Add session information to event set.
         */
        eventSet.addAttribute(HazardConstants.CURRENT_TIME, sessionManager
                .getTimeManager().getCurrentTime().getTime());

        Dict frameInfo = HazardServicesEditorUtilities.buildFrameInformation();
        eventSet.addAttribute("framesInfo",
                (Serializable) Utilities.asMap(frameInfo));

        HashMap<String, Serializable> staticSettings = buildStaticSettings();
        eventSet.addAttribute(HazardConstants.STATIC_SETTINGS, staticSettings);
        eventSet.addAttribute(HazardConstants.SITEID, sessionManager
                .getConfigurationManager().getSiteID());
        eventSet.addAttribute("hazardMode", caveMode.equals(CAVEMode.PRACTICE
                .toString()) ? HazardEventManager.Mode.PRACTICE.toString()
                : HazardEventManager.Mode.OPERATIONAL.toString());

        sessionManager.getRecommenderEngine().runExecuteRecommender(toolName,
                eventSet, spatialInfo, Utilities.asMap(dialogInfo),
                getRecommenderListener(toolName));

        appBuilder.setCursor(SpatialViewCursorTypes.MOVE_NODE_CURSOR);

        notifyModelEventsChanged();

    }

    /**
     * TODO Get this from the session manager somehow.
     */
    @Deprecated
    private HashMap<String, Serializable> buildStaticSettings() {
        HashMap<String, Serializable> staticSettings = Maps.newHashMap();

        staticSettings.put("defaultDuration", 1800000);
        staticSettings.put("defaultSiteID", "OAX");
        return staticSettings;
    }

    private IPythonJobListener<EventSet<IEvent>> getRecommenderListener(
            String toolName) {
        return new HazardServicesRecommenderJobListener(
                appBuilder.getEventBus(), toolName);
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
            final EventSet<IEvent> eventList) {

        sessionManager.handleRecommenderResult(recommenderID, eventList);

        notifyModelEventsChanged();

    }

    public void handleProductGeneratorResult(String toolID,
            final List<IGeneratedProduct> productList) {

        String resultJSON = productGeneratorHandler
                .handleProductGeneratorResult(toolID, productList);

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
    private void deleteEvent(Collection<IHazardEvent> events) {
        statusHandler.debug("HazardServicesMessageHandler: deleteEvent: "
                + events);

        for (IHazardEvent event : events) {
            sessionEventManager.removeEvent(event);
        }
        notifyModelEventsChanged();
        appBuilder.hideHazardDetail();
    }

    /**
     * Changes the state of the selected events to the state given by the state
     * parameter
     */
    public void changeSelectedEventsToProposedState() {

        Collection<IHazardEvent> events = sessionEventManager
                .getSelectedEvents();

        for (IHazardEvent event : events) {
            sessionEventManager.proposeEvent(event);
        }

        notifyModelEventsChanged();
        appBuilder.hideHazardDetail();
        appBuilder.closeProductEditorView();
    }

    private void updateToPreviewEnded() {
        Collection<IHazardEvent> events = sessionEventManager
                .getSelectedEvents();

        for (IHazardEvent event : events) {
            event.addHazardAttribute(HazardConstants.PREVIEW_STATE,
                    HazardConstants.PREVIEW_STATE_ENDED);
        }
    }

    /**
     * Issues the events upon user confirmation.
     */
    private void issueEvents() {
        if (continueIfThereAreHazardConflicts()) {
            generateProducts(true);
            notifyModelEventsChanged();
        }
    }

    /**
     * Launch the Staging Dialog if necessary OR return the Generated Products
     * 
     * @param issue
     *            Flag indicating whether or not this is the result of an issue
     *            action.
     * @return Products that were generated.
     */
    private void generateProducts(boolean issue) {

        if (productGeneratorHandler.productGenerationRequired()) {
            productGeneratorHandler.generateProducts(issue);
        } else {
            ProductStagingInfo productStagingInfo = productGeneratorHandler
                    .buildProductStagingInfo();
            appBuilder.showProductStagingView(issue, productStagingInfo);
        }
    }

    /**
     * This method is called when a storm track point is moved on the Spatial
     * Display
     * 
     * Appropriate adjustments are made to the event and then the Spatial
     * Display is re-drawn
     * 
     * @param action
     */

    @Subscribe
    public void handleStormTrackModification(ModifyStormTrackAction action) {
        runTool(HazardConstants.MODIFY_STORM_TRACK_TOOL,
                action.getParameters(), null);
        appBuilder.notifyModelChanged(EnumSet
                .of(HazardConstants.Element.EVENTS));
    }

    @Subscribe
    public void handleNewHazard(NewHazardAction action) {
        notifyModelEventsChanged();
    }

    @Subscribe
    public void handleHazardGeometryModification(
            ModifyHazardGeometryAction action) {
        IHazardEvent event = sessionEventManager.getEventById(action
                .getEventID());
        event.setGeometry(action.getGeometry());
        appBuilder.notifyModelChanged(EnumSet
                .of(HazardConstants.Element.EVENTS));
    }

    /**
     * This method is called when the user clicks "Reset" on the Tool bar to
     * reset the hazards to the canned case for the given setting.
     * 
     * @param type
     *            Type of entities to reset. *
     */
    public void reset(String type) {
        sessionManager.reset();

        /*
         * Switch back to the default settings only if resetting the settings.
         */
        if (type == HazardConstants.RESET_SETTINGS) {
            changeSetting(getSettingForCurrentPerspective(), false, true);
        }
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

        sessionManager.initialize(sessionTimeManager.getSelectedTime(),
                settingID, "", caveMode, siteID);

        appBuilder.notifyModelChanged(eventsChanged ? EnumSet.of(
                HazardConstants.Element.EVENTS,
                HazardConstants.Element.SETTINGS,
                HazardConstants.Element.TOOLS,
                HazardConstants.Element.VISIBLE_TIME_DELTA,
                HazardConstants.Element.DYNAMIC_SETTING) : EnumSet.of(
                HazardConstants.Element.SETTINGS,
                HazardConstants.Element.TOOLS,
                HazardConstants.Element.VISIBLE_TIME_DELTA,
                HazardConstants.Element.DYNAMIC_SETTING));
    }

    /**
     * Updates the selected time either in CAVE or the Console. Selected time
     * updates in CAVE appear in the Console. Selected time updates in the
     * Console appear in CAVE.
     * 
     * @param selectedTime
     * @param originator
     *            Where the selected time was changed. Can be "CAVE_ORIGINATOR"
     *            for CAVE or "TEMPORAL_ORIGINATOR" for the Console.
     */
    public void updateSelectedTime(Date selectedTime, String originator)
            throws VizException {
        sessionTimeManager.setSelectedTime(selectedTime);

        if (originator.equals(HazardServicesAppBuilder.CAVE_ORIGINATOR)) {
            appBuilder.notifyModelChanged(EnumSet
                    .of(HazardConstants.Element.SELECTED_TIME));
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
        TimeRange selectedRange = new TimeRange(toDate(selectedTimeStart_ms),
                toDate(selectedTimeEnd_ms));
        sessionTimeManager.setSelectedTimeRange(selectedRange);
        appBuilder.notifyModelChanged(EnumSet
                .of(HazardConstants.Element.SELECTED_TIME_RANGE));
    }

    private Date toDate(String timeInMillisAsLongAsString) {
        return new Date(Long.valueOf(timeInMillisAsLongAsString));
    }

    /**
     * This method is called when the visible time range is changed in the
     * Temporal Window.
     */
    public void updateVisibleTimeRange(String jsonStartTime,
            String jsonEndTime, String originator) {
        TimeRange visibleRange = new TimeRange(toDate(jsonStartTime),
                toDate(jsonEndTime));
        sessionTimeManager.setVisibleRange(visibleRange);
        appBuilder.notifyModelChanged(EnumSet
                .of(HazardConstants.Element.VISIBLE_TIME_RANGE));
    }

    /**
     * Set the add to selected mode as specified.
     * 
     * @param state
     *            New state of the add-to-selected mode.
     */
    public void setAddToSelected(String state) {
        sessionConfigurationManager.getSettings().setAddToSelected(
                state.equalsIgnoreCase(ADD_PENDING_TO_SELECTED_ON));
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
     * @param isUserInitiated
     *            Flag indicating whether or not the updated data are the result
     *            of a user-edit.
     * @return
     */
    public void updateEventData(String jsonText, String originator,
            boolean isUserInitiated) {
        _updateEventData(jsonText, originator, isUserInitiated);
        appBuilder.notifyModelChanged(EnumSet
                .of(HazardConstants.Element.EVENTS));
    }

    /**
     * This method is called when the event type is changed in the Hazard
     * Information Dialog
     * 
     * @param jsonText
     *            Contains information regarding the event type.
     */
    public void updateEventType(String jsonText) {
        _updateEventData(jsonText,
                HazardServicesAppBuilder.HAZARD_INFO_ORIGINATOR, true);
        notifyModelEventsChanged();
    }

    private void _updateEventData(String jsonText, String source,
            Boolean isUserInitiated) {
        JsonNode jnode = fromJson(jsonText, JsonNode.class);
        IHazardEvent event = sessionEventManager.getEventById(jnode.get(
                HazardConstants.HAZARD_EVENT_IDENTIFIER).getValueAsText());

        Iterator<String> fields = jnode.getFieldNames();
        while (fields.hasNext()) {
            String key = fields.next();
            if (HazardConstants.HAZARD_EVENT_IDENTIFIER.equals(key)) {
                ;
            } else if (HAZARD_EVENT_FULL_TYPE.equals(key)) {
                IHazardEvent oldEvent = null;

                if (!sessionEventManager.canChangeType(event)) {
                    oldEvent = event;
                    event = new BaseHazardEvent(event);
                    event.setEventID("");
                    event.setState(HazardState.PENDING);
                    event.addHazardAttribute(HazardConstants.REPLACES,
                            sessionConfigurationManager.getHeadline(oldEvent));
                    // New event should not have product information
                    event.removeHazardAttribute(EXPIRATIONTIME);
                    event.removeHazardAttribute(ISSUETIME);
                    event.removeHazardAttribute(VTEC_CODES);
                    event.removeHazardAttribute(ETNS);
                    event.removeHazardAttribute(PILS);
                    Date d = new Date();
                    event.setIssueTime(d);
                    Collection<IHazardEvent> selection = sessionEventManager
                            .getSelectedEvents();
                    event = sessionEventManager.addEvent(event);
                    selection.add(event);
                    sessionEventManager.setSelectedEvents(selection);
                }
                String fullType = jnode.get(key).getValueAsText();
                if (!fullType.isEmpty()) {
                    String[] phenSig = fullType.split(" ")[0].split("\\.");
                    event.setPhenomenon(phenSig[0]);
                    event.setSignificance(phenSig[1]);
                    if (phenSig.length > 2) {
                        event.setSubType(phenSig[2]);
                    } else {
                        event.setSubType(null);
                    }

                    /*
                     * Make sure the updated hazard type is a part of the
                     * visible types in the current setting. If not, add it.
                     */
                    Set<String> visibleTypes = sessionConfigurationManager
                            .getSettings().getVisibleTypes();
                    visibleTypes.add(HazardEventUtilities.getHazardType(event));
                    appBuilder.notifyModelChanged(EnumSet
                            .of(HazardConstants.Element.DYNAMIC_SETTING));
                } else {
                    event.setPhenomenon(null);
                    event.setSignificance(null);
                    event.setSubType(null);
                }

                if (oldEvent != null) {
                    oldEvent.addHazardAttribute("replacedBy",
                            sessionConfigurationManager.getHeadline(event));
                    oldEvent.addHazardAttribute("previewState",
                            HazardConstants.HazardState.ENDED.getValue());
                }
            } else if (HAZARD_EVENT_START_TIME.equals(key)) {
                if (!sessionEventManager.canChangeTimeRange(event)) {
                    event = new BaseHazardEvent(event);
                    Collection<IHazardEvent> selection = sessionEventManager
                            .getSelectedEvents();
                    event = sessionEventManager.addEvent(event);
                    selection.add(event);
                    sessionEventManager.setSelectedEvents(selection);
                }
                event.setStartTime(new Date(jnode.get(key).getLongValue()));
            } else if (HAZARD_EVENT_END_TIME.equals(key)) {
                if (!sessionEventManager.canChangeTimeRange(event)) {
                    event = new BaseHazardEvent(event);
                    Collection<IHazardEvent> selection = sessionEventManager
                            .getSelectedEvents();
                    event = sessionEventManager.addEvent(event);
                    selection.add(event);
                    sessionEventManager.setSelectedEvents(selection);
                }
                event.setEndTime(new Date(jnode.get(key).getLongValue()));
            } else if (jnode.get(key).isArray()) {
                ArrayNode arrayNode = (ArrayNode) jnode.get(key);
                List<String> stringList = Lists.newArrayList();

                for (int i = 0; i < arrayNode.size(); i++) {
                    stringList.add(arrayNode.get(i).getValueAsText());
                }

                /*
                 * Do no pass data as arrays. It is better to pass them as
                 * lists. Using arrays causes problems. For instance, an event
                 * passed to the the Product Generation Framework will have its
                 * cta array converted to a Python list. When returned to the
                 * HMI, this Python list is converted to a Java list. So, arrays
                 * are not consistently handled. The type is not preserved.
                 */
                event.addHazardAttribute(key, (Serializable) stringList);
            } else if (jnode.get(key).isContainerNode()) {
                throw new UnsupportedOperationException(
                        "Support for container node not implemented yet.");
            } else {
                JsonNode primitive = jnode.get(key);
                if (primitive.isTextual()) {
                    event.addHazardAttribute(key, primitive.getValueAsText());
                } else if (primitive.isBoolean()) {
                    event.addHazardAttribute(key, primitive.getBooleanValue());
                } else if (primitive.isNumber()) {
                    Object currentVal = event.getHazardAttribute(key);
                    if (currentVal instanceof Integer) {
                        event.addHazardAttribute(key, primitive.getIntValue());
                    } else {
                        event.addHazardAttribute(key,
                                new Date(primitive.getLongValue()));
                    }
                } else {
                    throw new UnsupportedOperationException("Not implemented");
                }
            }
        }

        if (isUserInitiated && event instanceof ObservedHazardEvent) {
            ((ObservedHazardEvent) event).setModified(true);
        }

    }

    private <T> T fromJson(String str, Class<T> clazz) {
        try {
            return jsonObjectMapper.readValue(str, clazz);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Update the specified static setting.
     * 
     * @param jsonSetting
     *            JSON string holding a dictionary of key-value pairs defining
     *            the setting being saved.
     */
    public void updateStaticSetting(String settings) {
        Settings settingsObj = fromJson(settings, Settings.class);
        sessionConfigurationManager.getSettings().apply(settingsObj);
        sessionConfigurationManager.saveSettings();
    }

    /**
     * Create a new static setting.
     * 
     * @param jsonSetting
     *            JSON string holding a dictionary of key-value pairs defining
     *            the setting being saved.
     */
    public void createStaticSetting(String jsonSetting) {
        String settingID = newStaticSettings(jsonSetting);
        changeSetting(settingID, true, false);
    }

    private String newStaticSettings(String settings) {
        Settings settingsObj = fromJson(settings, Settings.class);
        settingsObj.setSettingsID(settingsObj.getDisplayName());
        sessionConfigurationManager.getSettings().apply(settingsObj);
        sessionConfigurationManager.saveSettings();
        return settingsObj.getSettingsID();
    }

    /**
     * Respond to the dynamic setting having changed.
     * 
     * @param jsonDynamicSetting
     *            JSON string holding the dictionary defining the dynamic
     *            setting.
     */
    public void dynamicSettingChanged(String jsonDynamicSetting) {
        sessionManager.initialize(sessionManager.getTimeManager()
                .getSelectedTime(), sessionConfigurationManager.getSettings()
                .getSettingsID(), jsonDynamicSetting, caveMode, siteID);
        appBuilder.notifyModelChanged(EnumSet
                .of(HazardConstants.Element.DYNAMIC_SETTING));
    }

    /**
     * This method is invoked when the current CAVE time changes.
     * 
     * @param originator
     *            The originator of the current time update. For the moment,
     *            this should be set to "Cave". This should be an enumeration
     *            not a string.
     * 
     * @throws VizException
     */
    public void updateCurrentTime(String originator) throws VizException {

        if (originator.equals(HazardServicesAppBuilder.CAVE_ORIGINATOR)) {
            appBuilder.notifyModelChanged(EnumSet
                    .of(HazardConstants.Element.CURRENT_TIME));
        }
    }

    public void updateSite(String site) {
        appBuilder.notifyModelChanged(EnumSet.of(HazardConstants.Element.SITE));
    }

    /**
     * Generates products for preview.
     */
    public void preview() {
        generateProducts(false);
    }

    /**
     * Updates the model with CAVE frame information.
     */
    public void sendFrameInformationToSessionManager() {

        Dict frameDict = HazardServicesEditorUtilities.buildFrameInformation();
        if (!frameDict.isEmpty()) {
            VizApp.runAsync(new FrameUpdater(frameDict));
        }

    }

    private class FrameUpdater implements Runnable {

        private final Dict frameDict;

        private FrameUpdater(final Dict frameDict) {
            this.frameDict = frameDict;
        }

        @Override
        public void run() {
            /*
             * Make sure the HazardServices selected time is in-sync with the
             * frame being viewed.
             */
            Integer frameCount = frameDict
                    .getDynamicallyTypedValue(HazardServicesEditorUtilities.FRAME_COUNT);
            Integer frameIndex = frameDict
                    .getDynamicallyTypedValue(HazardServicesEditorUtilities.FRAME_INDEX);
            List<Long> dataTimeList = frameDict
                    .getDynamicallyTypedValue(HazardServicesEditorUtilities.FRAME_TIMES);
            if ((frameCount > 0) && (frameIndex != -1)) {
                try {
                    updateSelectedTime(new Date(dataTimeList.get(frameIndex)),
                            HazardServicesAppBuilder.CAVE_ORIGINATOR);
                } catch (VizException e) {
                    statusHandler.error("HazardServicesMessageHandler:", e);
                }
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
        if (label.contains(HazardConstants.CONTEXT_MENU_PROPOSE)) {
            changeSelectedEventsToProposedState();
        } else if (label.contains(HazardConstants.CONTEXT_MENU_ISSUE)) {
            issueEvents();
        } else if (label.contains(HazardConstants.CONTEXT_MENU_END)) {
            updateToPreviewEnded();
            preview();
        } else if (label.equals(HazardConstants.CONTEXT_MENU_DELETE_NODE)) {
            appBuilder.modifyShape(HazardServicesDrawingAction.DELETE_NODE);
        } else if (label.equals(HazardConstants.CONTEXT_MENU_ADD_NODE)) {
            appBuilder.modifyShape(HazardServicesDrawingAction.ADD_NODE);
        } else if (label.contains(HazardConstants.CONTEXT_MENU_DELETE)) {
            deleteEvent(sessionEventManager.getSelectedEvents());
        } else if (label
                .contains(HazardConstants.CONTEXT_MENU_HAZARD_INFORMATION_DIALOG)) {
            /*
             * Save off any changes the user has made in the HID. Otherwise,
             * this would be lost when selecting different events.
             */
            appBuilder.showHazardDetail();
        } else if (label
                .contains(HazardConstants.CONTEXT_MENU_REMOVE_POTENTIAL_HAZARDS)) {
            removeEventsWithState(HazardConstants.HazardState.POTENTIAL
                    .getValue());
            notifyModelEventsChanged();
        } else if (label.equals(HazardConstants.CONTEXT_MENU_ADD_REMOVE_SHAPES)) {
            appBuilder.loadGeometryOverlayForSelectedEvent();
        } else if (label.equals(HazardConstants.CONTEXT_MENU_SEND_TO_BACK)) {
            sessionEventManager
                    .sortEvents(ISessionEventManager.SEND_SELECTED_BACK);
            notifyModelEventsChanged();
        } else if (label.equals(HazardConstants.CONETXT_MENU_BRING_TO_FRONT)) {
            sessionEventManager
                    .sortEvents(ISessionEventManager.SEND_SELECTED_FRONT);
            notifyModelEventsChanged();
        } else if (label
                .equals(HazardConstants.CONTEXT_MENU_CLIP_AND_REDUCE_SELECTED_HAZARDS)) {
            sessionEventManager.clipSelectedHazardGeometries();
            sessionEventManager.reduceSelectedHazardGeometries();
        }
    }

    private void removeEventsWithState(String stateValue) {
        HazardState state = HazardState.valueOf(stateValue.toUpperCase());
        for (IHazardEvent event : sessionEventManager.getEventsByState(state)) {
            sessionEventManager.removeEvent(event);
        }
    }

    /**
     * Prepare for shutdown by removing references to the model.
     */
    public void prepareForShutdown() {
        SimulatedTime.getSystemTime().removeSimulatedTimeChangeListener(this);
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

        if (productDisplayAction.equals(HazardConstants.CONTEXT_MENU_PROPOSE)) {
            changeSelectedEventsToProposedState();
        } else if (productDisplayAction
                .equals(HazardConstants.CONTEXT_MENU_ISSUE)) {
            if (continueIfThereAreHazardConflicts()) {

                productGeneratorHandler.createProductsFromHazardEventSets(true,
                        productDisplayJSON);
                notifyModelEventsChanged();
            }
        }

        appBuilder.closeProductEditorView();

    }

    /**
     * Handles the product display dialog continue action.
     * 
     * @param issue
     *            Flag indicating whether or not this is the result of an issue.
     * @param productStagingInfo
     * 
     */
    public void handleProductDisplayContinueAction(boolean issue,
            ProductStagingInfo productStagingInfo) {
        productGeneratorHandler.createProductsFromProductStagingInfo(issue,
                productStagingInfo);
        if (!issue) {
            // appBuilder.showProductEditorView(returnDict_json);
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

            try {
                updateCurrentTime(HazardServicesAppBuilder.CAVE_ORIGINATOR);
            } catch (VizException e) {
                statusHandler.error(
                        "Error updating Hazard Services components", e);
            }
        }
    }

    /**
     * Handles an undo action from the Console.
     * 
     * @param
     * @return
     */
    public void handleUndoAction() {
        this.sessionManager.undo();
    }

    /**
     * Handles a redo action from the Console.
     * 
     * @param
     * @return
     */
    public void handleRedoAction() {
        this.sessionManager.redo();
    }

    /**
     * Update the visible time delta.
     */
    public void updateConsoleVisibleTimeDelta() {
        appBuilder.notifyModelChanged(EnumSet
                .of(HazardConstants.Element.VISIBLE_TIME_DELTA));
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
     * Examines all hazards looking for potential conflicts.
     * 
     * @param
     * @return
     */
    public void checkHazardConflicts() {

        ISessionEventManager sessionEventManager = sessionManager
                .getEventManager();

        Map<IHazardEvent, Map<IHazardEvent, Collection<String>>> conflictMap = sessionEventManager
                .getAllConflictingEvents();

        if (!conflictMap.isEmpty()) {
            launchConflictingHazardsDialog(conflictMap, false);
        }

    }

    /**
     * Toggles on/off automatic conflict checking.
     */
    public void toggleAutoCheckConflicts() {
        sessionManager.toggleAutoHazardChecking();
    }

    /**
     * Toggle on/off the display of hazard hatch areas.
     * 
     * @param
     * @return
     */
    public void toggleHatchedAreaDisplay() {
        sessionManager.toggleHatchedAreaDisplay();
    }

    /**
     * Examines all hazards looking for potential conflicts. Returns the user's
     * decision as to whether or not to continue with the conflicts.
     * 
     * @param
     * @return The user's decision to continue (true) or not (false) if there
     *         are existing
     */
    private Boolean continueIfThereAreHazardConflicts() {

        Boolean userResponse = true;

        ISessionEventManager sessionEventManager = sessionManager
                .getEventManager();

        Map<IHazardEvent, Map<IHazardEvent, Collection<String>>> conflictMap = sessionEventManager
                .getAllConflictingEvents();

        if (!conflictMap.isEmpty()) {
            userResponse = launchConflictingHazardsDialog(conflictMap, true);
        }

        return userResponse;
    }

    @Subscribe
    public void sessionEventsModified(final SessionEventsModified notification) {
        VizApp.runAsync(new Runnable() {

            @Override
            public void run() {
                notifyModelEventsChanged();
            }
        });
    }

    @Subscribe
    public void selectedTimeChanged(SelectedTimeChanged notification) {
        VizApp.runAsync(new Runnable() {

            @Override
            public void run() {
                appBuilder.notifyModelChanged(EnumSet
                        .of(HazardConstants.Element.SELECTED_TIME));
            }
        });
    }

    public ISessionManager getSessionManager() {
        return sessionManager;
    }

    /**
     * Retrieves the setting for the current perspective. If this perspective is
     * not specified by any Setting, then this method defaults to the first
     * setting in the list of available settings.
     * 
     * @param
     * @return The setting identifier
     */
    private String getSettingForCurrentPerspective() {

        String perspectiveID = VizPerspectiveListener
                .getCurrentPerspectiveManager().getPerspectiveId();
        List<Settings> settingsList = sessionManager.getConfigurationManager()
                .getSettingsList();

        for (Settings settings : settingsList) {
            Set<String> settingPerspectiveList = settings.getPerspectiveIDs();

            if (settingPerspectiveList != null
                    && settingPerspectiveList.contains(perspectiveID)) {
                return settings.getSettingsID();
            }
        }

        /*
         * It might be better to create a default settings object. It would not
         * be represented in the Localization perspective. Rather, it would be
         * in memory. I'm assuming that there will always be settings
         * information available. Is that dangerous?
         */
        return settingsList.get(0).getSettingsID();
    }

    /**
     * Launches a dialog displaying conflicting hazards. It is up to the user as
     * to whether or not to fix them.
     * 
     * @param conflictingHazardMap
     *            A map of hazards and hazards which conflict with them.
     * @param requiresConfirmation
     *            Indicates whether or not this dialog should require user
     *            confirmation (Yes or No).
     * @return The return value from the dialog based on the user's selection.
     */
    private Boolean launchConflictingHazardsDialog(
            final Map<IHazardEvent, Map<IHazardEvent, Collection<String>>> conflictingHazardMap,
            final Boolean requiresConfirmation) {

        Boolean userSelection = true;

        if (!conflictingHazardMap.isEmpty()) {
            StringBuffer message = new StringBuffer(
                    "Conflicting Hazards: The following hazard conflicts exist: ");

            if (requiresConfirmation) {
                message.append("Continue?\n");
            } else {
                message.append("\n");
            }

            for (IHazardEvent hazardEvent : conflictingHazardMap.keySet()) {

                String phenSig = HazardEventUtilities
                        .getHazardType(hazardEvent);
                message.append("Event ID:" + hazardEvent.getEventID() + "("
                        + phenSig + ") Conflicts With: ");

                Map<IHazardEvent, Collection<String>> conflictingHazards = conflictingHazardMap
                        .get(hazardEvent);

                for (IHazardEvent conflictingHazard : conflictingHazards
                        .keySet()) {
                    String conflictingPhenSig = HazardEventUtilities
                            .getHazardType(conflictingHazard);
                    message.append("Event ID:" + conflictingHazard.getEventID()
                            + "(" + conflictingPhenSig + ") ");

                    Collection<String> conflictingAreas = conflictingHazards
                            .get(conflictingHazard);

                    if (!conflictingAreas.isEmpty()) {
                        message.append("\n\tAreas:");

                        for (String area : conflictingAreas) {
                            message.append(" " + area);
                        }
                    }

                }

                message.append("\n");
            }

            if (requiresConfirmation) {
                userSelection = appBuilder.getUserAnswerToQuestion(message
                        .toString());

            } else {
                appBuilder.warnUser(message.toString());
            }
        }

        return userSelection;
    }

    @Override
    public void timechanged() {
        try {
            updateCurrentTime(HazardServicesAppBuilder.CAVE_ORIGINATOR);
        } catch (VizException e) {
            statusHandler.error("Error updating Hazard Services current time",
                    e);
        }
    }
}

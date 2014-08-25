/**
 * This software was developed and / or modified by the National Oceanic and
 * Atmospheric Administration (NOAA), Earth System Research Laboratory (ESRL),
 * Global Systems Division (GSD), Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.display;

import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HAZARD_EVENT_CHECKED;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HAZARD_EVENT_END_TIME;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HAZARD_EVENT_END_TIME_UNTIL_FURTHER_NOTICE;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HAZARD_EVENT_FULL_TYPE;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HAZARD_EVENT_IDENTIFIER;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HAZARD_EVENT_START_TIME;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HAZARD_MODE;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.REPLACED_BY;
import gov.noaa.gsd.common.eventbus.BoundedReceptionEventBus;
import gov.noaa.gsd.viz.hazards.UIOriginator;
import gov.noaa.gsd.viz.hazards.contextmenu.ContextMenuHelper;
import gov.noaa.gsd.viz.hazards.display.action.ConsoleAction;
import gov.noaa.gsd.viz.hazards.display.action.CurrentSettingsAction;
import gov.noaa.gsd.viz.hazards.display.action.HazardDetailAction;
import gov.noaa.gsd.viz.hazards.display.action.HazardServicesCloseAction;
import gov.noaa.gsd.viz.hazards.display.action.ModifyStormTrackAction;
import gov.noaa.gsd.viz.hazards.display.action.ProductEditorAction;
import gov.noaa.gsd.viz.hazards.display.action.ProductStagingAction;
import gov.noaa.gsd.viz.hazards.display.action.SpatialDisplayAction;
import gov.noaa.gsd.viz.hazards.display.action.StaticSettingsAction;
import gov.noaa.gsd.viz.hazards.display.action.ToolAction;
import gov.noaa.gsd.viz.hazards.jsonutilities.Dict;
import gov.noaa.gsd.viz.hazards.product.ReviewAction;
import gov.noaa.gsd.viz.hazards.pythonjoblistener.HazardServicesRecommenderJobListener;
import gov.noaa.gsd.viz.hazards.servicebackup.ChangeSiteAction;
import gov.noaa.gsd.viz.hazards.spatialdisplay.HazardServicesDrawingAction;
import gov.noaa.gsd.viz.hazards.spatialdisplay.HazardServicesMouseHandlers;
import gov.noaa.gsd.viz.hazards.spatialdisplay.SpatialView.SpatialViewCursorTypes;
import gov.noaa.gsd.viz.hazards.timer.TimerAction;
import gov.noaa.gsd.viz.hazards.utilities.Utilities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.engio.mbassy.listener.Handler;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.google.common.collect.Lists;
import com.raytheon.uf.common.dataplugin.events.EventSet;
import com.raytheon.uf.common.dataplugin.events.IEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.GeometryType;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardStatus;
import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.HazardEventManager;
import com.raytheon.uf.common.dataplugin.events.hazards.event.BaseHazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardEventUtilities;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.hazards.productgen.GeneratedProductList;
import com.raytheon.uf.common.hazards.productgen.data.ProductData;
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
import com.raytheon.uf.viz.hazards.sessionmanager.config.SettingsModified;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Settings;
import com.raytheon.uf.viz.hazards.sessionmanager.events.ISessionEventManager;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventAdded;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventGeometryModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventsModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.impl.ObservedHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.events.impl.SessionEventManager;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.IOriginator;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.Originator;
import com.raytheon.uf.viz.hazards.sessionmanager.product.IProductGenerationComplete;
import com.raytheon.uf.viz.hazards.sessionmanager.product.ProductFormats;
import com.raytheon.uf.viz.hazards.sessionmanager.product.ProductInformation;
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
 * Feb 03, 2014 2155       Chris.Golden       Fixed bug that caused floating-
 *                                            point values to be interpreted
 *                                            as long integers when doing
 *                                            conversions to/from JSON.
 * Feb 07, 2014  2890      bkowal             Product Generation JSON refactor.
 * Feb 19, 2014  2915      bkowal             JSON settings re-factor
 * Feb 19, 2014  2161      Chris.Golden       Added ability to handle console action
 *                                            indicating that until further notice
 *                                            has changed for an event.
 * Apr 11, 2014  2819      Chris.Golden       Fixed bugs with the Preview and Issue
 *                                            buttons in the HID remaining grayed out
 *                                            when they should be enabled.
 * Apr 12, 2014  2925      Chris.Golden       Moved some business logic into the session
 *                                            manager, and altered to work with class-
 *                                            based metadata, as well as doing general
 *                                            clean-up.
 * May 15, 2014  2925      Chris.Golden       Minor changes to support new HID. Also
 *                                            changed instantiation of presenters to not
 *                                            include view as one of the constructor
 *                                            arguments, since the view is set post-
 *                                            construction now (to avoid having a view
 *                                            be initialized by the Presenter superclass
 *                                            before the subclass has finished being
 *                                            built).
 * Apr 23, 2014  1480      jsanchez           Handled reviewable products.
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

    /**
     * Indicates that no selected time has been updated.
     */
    private final String NO_SELECTED_TIME = "None";

    // Private Static Constants

    /**
     * Logging mechanism.
     */
    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(HazardServicesMessageHandler.class);

    // Private Variables

    /**
     * An instance of the Hazard Services app builder.
     */
    private HazardServicesAppBuilder appBuilder = null;

    private final ISessionManager<ObservedHazardEvent> sessionManager;

    private final ISessionEventManager<ObservedHazardEvent> sessionEventManager;

    private final ISessionTimeManager sessionTimeManager;

    private final ISessionConfigurationManager sessionConfigurationManager;

    private final HazardServicesProductGenerationHandler productGeneratorHandler;

    private final BoundedReceptionEventBus<Object> eventBus;

    // Public Constructors
    /**
     * Construct a standard instance.
     * 
     * @param appBuilder
     *            A reference to the Hazard Services app builder
     * @param currentTime
     *            The current time, based on the CAVE current time.
     * @param state
     *            Saved session state to initialize this session from the
     *            previous session.
     * 
     */
    public HazardServicesMessageHandler(HazardServicesAppBuilder appBuilder,
            Date currentTime) {
        this.appBuilder = appBuilder;
        this.sessionManager = appBuilder.getSessionManager();

        this.productGeneratorHandler = new HazardServicesProductGenerationHandler(
                sessionManager, appBuilder.getEventBus());
        this.sessionEventManager = sessionManager.getEventManager();
        this.sessionTimeManager = sessionManager.getTimeManager();
        this.sessionConfigurationManager = sessionManager
                .getConfigurationManager();
        this.eventBus = appBuilder.getEventBus();

        this.eventBus.subscribe(this);

        sessionConfigurationManager.setSiteID(LocalizationManager.getInstance()
                .getCurrentSite());

        String staticSettingID = getSettingForCurrentPerspective();

        sessionConfigurationManager.changeSettings(staticSettingID);
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
     * @param UIOriginator
     *            Where this action originated from
     * 
     * @throws VizException
     */
    public void updateSelectedEvents(final List<String> eventIDs,
            IOriginator originator) throws VizException {
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

        if (originator != UIOriginator.SPATIAL_DISPLAY) {
            appBuilder.recenterRezoomDisplay();
        }

    }

    private String setSelectedEvents(List<String> eventIDs,
            IOriginator originator) {
        Collection<ObservedHazardEvent> selectedEvents = fromIDs(eventIDs);
        Date selectedTime = sessionTimeManager.getSelectedTime();

        sessionEventManager.setSelectedEvents(selectedEvents, originator);
        if ((originator == UIOriginator.CONSOLE)
                && !selectedTime.equals(sessionTimeManager.getSelectedTime())) {
            return SINGLE_SELECTED_TIME;
        } else {
            return NO_SELECTED_TIME;
        }
    }

    private Collection<ObservedHazardEvent> fromIDs(List<String> eventIDs) {
        Collection<ObservedHazardEvent> events = new ArrayList<>(
                eventIDs.size());
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
    private void updateCaveSelectedTime() {
        appBuilder.notifyModelChanged(EnumSet
                .of(HazardConstants.Element.CAVE_TIME));
    }

    /**
     * Send notification to listeners (generally presenters) of hazard events
     * changing in the model.
     */
    private void notifyModelEventsChanged() {
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
    private void runTool(String toolName,
            Map<String, Serializable> spatialInfo,
            Map<String, Serializable> dialogInfo) {

        appBuilder.setCursor(SpatialViewCursorTypes.WAIT_CURSOR);
        Display.getCurrent().update();

        EventSet<IEvent> eventSet = new EventSet<>();

        /*
         * Add events to the event set.
         */
        Collection<ObservedHazardEvent> hazardEvents = sessionManager
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

        eventSet.addAttribute(HazardConstants.SITE_ID, sessionManager
                .getConfigurationManager().getSiteID());
        eventSet.addAttribute(
                HAZARD_MODE,
                (CAVEMode.getMode()).toString().equals(
                        CAVEMode.PRACTICE.toString()) ? HazardEventManager.Mode.PRACTICE
                        .toString() : HazardEventManager.Mode.OPERATIONAL
                        .toString());

        sessionManager.getRecommenderEngine().runExecuteRecommender(toolName,
                eventSet, spatialInfo, dialogInfo,
                getRecommenderListener(toolName));

        appBuilder.setCursor(SpatialViewCursorTypes.MOVE_VERTEX_CURSOR);

        notifyModelEventsChanged();

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
     * @param eventList
     *            A list of recommended events
     * @return
     */
    private void handleRecommenderResults(final EventSet<IEvent> eventList) {

        sessionManager.handleRecommenderResult(eventList);

        /*
         * Make sure the updated hazard type is a part of the visible types in
         * the current setting. If not, add it.
         */
        Set<String> visibleTypes = sessionConfigurationManager.getSettings()
                .getVisibleTypes();
        int startSize = visibleTypes.size();
        for (IEvent ievent : eventList) {
            IHazardEvent event = (IHazardEvent) ievent;
            visibleTypes.add(HazardEventUtilities.getHazardType(event));
        }

        if (startSize == visibleTypes.size()) {
            notifyModelEventsChanged();
        } else {
            appBuilder.notifyModelChanged(EnumSet.of(
                    HazardConstants.Element.EVENTS,
                    HazardConstants.Element.CURRENT_SETTINGS));
        }

    }

    @Handler
    public void handleProductGenerationCompletion(
            IProductGenerationComplete productGenerationComplete) {
        if (productGenerationComplete.isIssued()) {
            for (GeneratedProductList generatedProductList : productGenerationComplete
                    .getGeneratedProducts()) {
                for (IEvent event : generatedProductList.getEventSet()) {
                    IHazardEvent hazardEvent = (IHazardEvent) event;
                    ObservedHazardEvent oEvent = sessionEventManager
                            .getEventById(hazardEvent.getEventID());
                    if (oEvent.getStatus().equals(HazardStatus.PENDING)) {
                        oEvent.setStatus(HazardStatus.ISSUED);
                        oEvent.clearUndoRedo();
                        oEvent.setModified(false);

                    } else if (isChangeToEndedStateNeeded(hazardEvent)) {
                        oEvent.setStatus(HazardStatus.ENDED);
                    }

                }
            }
            sessionManager.setIssueOngoing(false);
        } else {
            appBuilder.showProductEditorView(productGenerationComplete
                    .getGeneratedProducts());
        }
    }

    /**
     * If an ending hazard is issued or an issued hazard is replaced, we need to
     * change it's state to ended.
     * 
     * @param hazardEvent
     * @return
     */
    private boolean isChangeToEndedStateNeeded(IHazardEvent hazardEvent) {
        return hazardEvent.getStatus().equals(HazardStatus.ENDING)
                || hazardEvent.getHazardAttribute(REPLACED_BY) != null;
    }

    /**
     * Shuts down the Hazard Services session.
     */
    private void closeHazardServices() {
        appBuilder.dispose();
    }

    /**
     * This method is called when events are deleted. This operation can only be
     * performed on Pending or Proposed events.
     * 
     * @param eventIDs
     *            Identifiers of events to be deleted.
     */
    private void deleteEvent(Collection<ObservedHazardEvent> events) {
        statusHandler.debug("HazardServicesMessageHandler: deleteEvent: "
                + events);

        sessionEventManager.removeEvents(events, null);

        notifyModelEventsChanged();
        appBuilder.hideHazardDetail();
    }

    /**
     * Changes the state of the selected events to the state given by the state
     * parameter
     */
    private void changeSelectedEventsToProposedState(IOriginator originator) {

        Collection<ObservedHazardEvent> events = sessionEventManager
                .getSelectedEvents();

        for (ObservedHazardEvent event : events) {
            sessionEventManager.proposeEvent(event, originator);
        }

        notifyModelEventsChanged();
        appBuilder.hideHazardDetail();
        appBuilder.closeProductEditorView();
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

        if (issue) {
            sessionManager.setIssueOngoing(true);
        } else {
            sessionManager.setPreviewOngoing(true);
        }

        if (productGeneratorHandler.productGenerationRequired(issue)) {

            List<String> unsupportedHazards = sessionManager
                    .getProductManager().getUnsupportedHazards();

            Collection<ProductInformation> selectedProducts = sessionManager
                    .getProductManager().getSelectedProducts(issue);

            boolean continueWithGeneration = true;

            if (!unsupportedHazards.isEmpty()) {
                StringBuffer message = new StringBuffer(
                        "Products for the following hazard types are not yet supported: ");
                for (String type : unsupportedHazards) {
                    message.append(type + " ");
                }

                if (!selectedProducts.isEmpty()) {
                    message.append("\nPress Continue to generate products for the supported hazard types.");
                    continueWithGeneration = appBuilder.getContinueCanceller()
                            .getUserAnswerToQuestion("Unsupported HazardTypes",
                                    message.toString());
                } else {
                    appBuilder.getWarner().warnUser("Unsupported HazardTypes",
                            message.toString());
                    continueWithGeneration = false;
                }

            }

            if (continueWithGeneration) {
                productGeneratorHandler.generateProducts(issue);
            } else {
                if (issue) {
                    sessionManager.setIssueOngoing(false);
                } else {
                    sessionManager.setPreviewOngoing(false);
                }
            }

        } else {
            ProductStagingInfo productStagingInfo = productGeneratorHandler
                    .buildProductStagingInfo(issue);
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

    @Handler
    public void handleStormTrackModification(ModifyStormTrackAction action) {
        runTool(HazardConstants.MODIFY_STORM_TRACK_TOOL,
                action.getParameters(), null);
        notifyModelEventsChanged();
    }

    @Handler
    public void handleNewHazard(SessionEventAdded action) {
        notifyModelEventsChanged();
    }

    @Handler
    public void handleHazardGeometryModification(
            SessionEventGeometryModified action) {
        notifyModelEventsChanged();
    }

    /**
     * This method is called when the user clicks "Reset" on the Tool bar to
     * reset the hazards to the canned case for the given setting.
     * 
     * @param type
     *            Type of entities to reset. *
     */
    private void reset(String type) {
        sessionManager.reset();
    }

    /**
     * Changes the setting to the new setting identifier.
     * 
     * @param settingID
     *            New setting to be used.
     * @param eventsChanged
     *            Flag indicating whether or not hazard events have changed as
     *            part of this change.
     */
    private void changeSetting(String settingID, boolean eventsChanged) {

        sessionConfigurationManager.changeSettings(settingID);

        appBuilder.notifyModelChanged(eventsChanged ? EnumSet.of(
                HazardConstants.Element.EVENTS,
                HazardConstants.Element.SETTINGS,
                HazardConstants.Element.TOOLS,
                HazardConstants.Element.VISIBLE_TIME_DELTA,
                HazardConstants.Element.CURRENT_SETTINGS) : EnumSet.of(
                HazardConstants.Element.SETTINGS,
                HazardConstants.Element.TOOLS,
                HazardConstants.Element.VISIBLE_TIME_DELTA,
                HazardConstants.Element.CURRENT_SETTINGS));

    }

    /**
     * TODO, this method will be moved once this class is refactored, but it
     * should be able to be moved quickly and easily
     */
    private void saveSetting() {
        sessionConfigurationManager.saveSettings();
    }

    private void saveAsSetting(String settingsId) {
        Settings settings = new Settings(
                sessionConfigurationManager.getSettings());
        String name = settingsId.replaceAll("\\P{Alnum}", "");
        settings.setSettingsID(name);
        settings.setStaticSettingsID(settingsId);
        settings.setDisplayName(settingsId);
        sessionConfigurationManager.getSettings().apply(settings);
        sessionConfigurationManager.saveSettings();
        List<Settings> availableSettings = sessionConfigurationManager
                .getAvailableSettings();
        availableSettings.add(settings);
    }

    /**
     * Updates the selected time either in CAVE or the Console. Selected time
     * updates in CAVE appear in the Console. Selected time updates in the
     * Console appear in CAVE.
     * 
     * @param selectedTime
     * @param UIOriginator
     */
    private void updateSelectedTime(Date selectedTime, IOriginator originator)
            throws VizException {
        sessionTimeManager.setSelectedTime(selectedTime);

        if (originator == Originator.OTHER) {
            appBuilder.notifyModelChanged(EnumSet
                    .of(HazardConstants.Element.SELECTED_TIME));
        }

        if (originator == UIOriginator.CONSOLE) {
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
     */
    private void updateSelectedTimeRange(String selectedTimeStart_ms,
            String selectedTimeEnd_ms) {
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
    private void updateVisibleTimeRange(String jsonStartTime, String jsonEndTime) {
        TimeRange visibleRange = new TimeRange(toDate(jsonStartTime),
                toDate(jsonEndTime));
        sessionTimeManager.setVisibleRange(visibleRange, UIOriginator.CONSOLE);
        appBuilder.notifyModelChanged(EnumSet
                .of(HazardConstants.Element.VISIBLE_TIME_RANGE));
    }

    /**
     * Set the add to selected mode as specified.
     * 
     * @param state
     *            New state of the add-to-selected mode.
     */
    private void setAddToSelected(SpatialDisplayAction.ActionIdentifier state) {
        sessionConfigurationManager.getSettings().setAddToSelected(
                state.equals(SpatialDisplayAction.ActionIdentifier.ON));
    }

    /**
     * Set the add geometry to selected mode as specified.
     * 
     * @param state
     *            New state of the add-geometry-to-selected mode.
     * @return
     */
    private void setAddGeometryToSelected(
            SpatialDisplayAction.ActionIdentifier state) {
        sessionConfigurationManager.getSettings().setAddGeometryToSelected(
                state.equals(SpatialDisplayAction.ActionIdentifier.ON));

    }

    /**
     * Updates information for an event taking into consideration the
     * UIOriginator.
     * 
     * @param map
     *            The portions of the event which are being updated.
     * @param isUserInitiated
     *            Flag indicating whether or not the updated data are the result
     *            of a user-edit.
     * @return
     */
    private void updateEventData(Map<String, Serializable> map,
            boolean isUserInitiated, IOriginator originator) {
        _updateEventData(map, isUserInitiated, originator);
        appBuilder.notifyModelChanged(
                EnumSet.of(HazardConstants.Element.EVENTS), originator);
    }

    /**
     * Update event data. This event should no longer be required once we move
     * away from map representations of hazard events and use POJOs instead.
     * (Also, all of this code will be directly executed by the presenters
     * anyway when we change over to direct manipulation of the model by
     * presenters.)
     */
    @SuppressWarnings("unchecked")
    @Deprecated
    private void _updateEventData(Map<String, Serializable> map,
            Boolean isUserInitiated, IOriginator originator) {
        ObservedHazardEvent oEvent = sessionEventManager
                .getEventById((String) map
                        .get(HazardConstants.HAZARD_EVENT_IDENTIFIER));
        if (oEvent == null) {
            return;
        }
        for (String key : map.keySet()) {
            if (HazardConstants.HAZARD_EVENT_IDENTIFIER.equals(key)) {
                ;
            } else if (HAZARD_EVENT_FULL_TYPE.equals(key)) {
                String fullType = (String) map.get(key);
                String[] phenSigSubType = HazardEventUtilities
                        .getHazardPhenSigSubType(fullType);
                sessionEventManager.setEventType(oEvent, phenSigSubType[0],
                        phenSigSubType[1], phenSigSubType[2], originator);
            } else if (HAZARD_EVENT_START_TIME.equals(key)) {
                oEvent.setStartTime(
                        new Date(((Number) map.get(key)).longValue()),
                        originator);
            } else if (HAZARD_EVENT_END_TIME.equals(key)) {
                oEvent.setEndTime(
                        new Date(((Number) map.get(key)).longValue()),
                        originator);
            } else if (map.get(key) instanceof Collection) {
                List<String> stringList = new ArrayList<>(
                        (Collection<String>) map.get(key));

                /*
                 * Do no pass data as arrays. It is better to pass them as
                 * lists. Using arrays causes problems. For instance, an event
                 * passed to the the Product Generation Framework will have its
                 * cta array converted to a Python list. When returned to the
                 * HMI, this Python list is converted to a Java list. So, arrays
                 * are not consistently handled. The type is not preserved.
                 */
                oEvent.addHazardAttribute(key, (Serializable) stringList);
            } else {
                Object primitive = map.get(key);
                if (primitive.getClass() == String.class) {
                    oEvent.addHazardAttribute(key, (String) primitive);
                } else if (primitive.getClass() == Boolean.class) {
                    oEvent.addHazardAttribute(key, (Boolean) primitive);
                } else if (primitive.getClass() == Float.class) {
                    oEvent.addHazardAttribute(key, (Float) primitive);
                } else if (primitive.getClass() == Double.class) {
                    oEvent.addHazardAttribute(key, (Double) primitive);
                } else if (primitive instanceof Number) {
                    Object currentVal = oEvent.getHazardAttribute(key);
                    if (currentVal instanceof Integer) {
                        oEvent.addHazardAttribute(key,
                                ((Number) primitive).intValue());
                    } else if (currentVal instanceof Long) {
                        oEvent.addHazardAttribute(key,
                                ((Number) primitive).longValue());
                    } else if (currentVal instanceof Date) {
                        oEvent.addHazardAttribute(key, new Date(
                                ((Number) primitive).longValue()));
                    }
                } else {
                    throw new UnsupportedOperationException(
                            "not implemented for key = "
                                    + key
                                    + " with value \""
                                    + primitive
                                    + "\" of type "
                                    + (primitive == null ? "null" : primitive
                                            .getClass().getSimpleName()));
                }
            }
        }

        if (isUserInitiated) {
            oEvent.setModified(true);
        }

    }

    /**
     * Respond to the current setting having changed.
     * 
     * @param settings
     */
    public void changeCurrentSettings(Settings settings) {
        sessionConfigurationManager.getSettings().apply(settings);
        appBuilder.notifyModelChanged(EnumSet
                .of(HazardConstants.Element.CURRENT_SETTINGS));
    }

    /**
     * This method is invoked when the current CAVE time changes.
     * 
     * @param UIOriginator
     *            The UIOriginator of the current time update. For the moment,
     *            this should be set to "Cave". This should be an enumeration
     *            not a string.
     * 
     * @throws VizException
     */
    private void updateCurrentTime(IOriginator originator) throws VizException {

        if (originator == Originator.OTHER) {
            appBuilder.notifyModelChanged(EnumSet
                    .of(HazardConstants.Element.CURRENT_TIME));
        }
    }

    private void updateSite(String site) {
        sessionConfigurationManager.getSettings().getVisibleSites().add(site);
        appBuilder.notifyModelChanged(EnumSet.of(HazardConstants.Element.SITE));
    }

    /**
     * Generates products for preview.
     */
    private void preview() {
        generateProducts(false);
    }

    public void generateReviewableProduct(List<ProductData> productData) {
        productGeneratorHandler.generateReviewableProduct(productData);
    }

    /**
     * Updates the model with CAVE frame information.
     */
    void sendFrameInformationToSessionManager() {

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
                            Originator.OTHER);
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
    private void handleContextMenuSelection(String label) {
        if (label
                .equals(ContextMenuHelper.ContextMenuSelections.PROPOSE_ALL_SELECTED_HAZARDS
                        .getValue())) {
            changeSelectedEventsToProposedState(null);
        } else if (label.equals(HazardConstants.CONTEXT_MENU_DELETE_VERTEX)) {
            appBuilder.modifyShape(HazardServicesDrawingAction.DELETE_VERTEX);
        } else if (label.equals(HazardConstants.CONTEXT_MENU_ADD_VERTEX)) {
            appBuilder.modifyShape(HazardServicesDrawingAction.ADD_VERTEX);
        } else if (label
                .equals(ContextMenuHelper.ContextMenuSelections.DELETE_ALL_SELECTED_HAZARDS
                        .getValue())) {
            deleteEvent(sessionEventManager.getSelectedEvents());
        } else if (label
                .contains(HazardConstants.CONTEXT_MENU_HAZARD_INFORMATION_DIALOG)) {
            /*
             * Save off any changes the user has made in the HID. Otherwise,
             * this would be lost when selecting different events.
             */
            appBuilder.showHazardDetail();
        } else if (label
                .contains(ContextMenuHelper.ContextMenuSelections.REMOVE_POTENTIAL_HAZARDS
                        .getValue())) {
            removeEventsWithState(HazardConstants.HazardStatus.POTENTIAL
                    .getValue());
            notifyModelEventsChanged();
        } else if (label.equals(HazardConstants.CONTEXT_MENU_ADD_REMOVE_SHAPES)) {
            appBuilder.loadGeometryOverlayForSelectedEvent();
        } else if (label.equals(HazardConstants.CONTEXT_MENU_SEND_TO_BACK)) {
            sessionEventManager
                    .sortEvents(SessionEventManager.SEND_SELECTED_BACK);
            notifyModelEventsChanged();
        } else if (label.equals(HazardConstants.CONTEXT_MENU_BRING_TO_FRONT)) {
            sessionEventManager
                    .sortEvents(SessionEventManager.SEND_SELECTED_FRONT);
            notifyModelEventsChanged();
        } else if (label
                .equals(HazardConstants.CONTEXT_MENU_CLIP_AND_REDUCE_SELECTED_HAZARDS)) {
            sessionEventManager.clipSelectedHazardGeometries();
            sessionEventManager.reduceSelectedHazardGeometries();
        } else {
            throw new IllegalArgumentException("Unexpected label " + label);
        }
    }

    private void removeEventsWithState(String stateValue) {
        HazardStatus state = HazardStatus.valueOf(stateValue.toUpperCase());
        for (ObservedHazardEvent event : sessionEventManager
                .getEventsByStatus(state)) {
            sessionEventManager.removeEvent(event, null);
        }
    }

    /**
     * Changes the state of an event to "Issued".
     */
    private void setIssuedState() {
        issueEvents();
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
    private void handleProductDisplayAction(ProductEditorAction action) {
        switch (action.getHazardAction()) {
        case PROPOSE:
            changeSelectedEventsToProposedState(action.getOriginator());
            break;
        case ISSUE:
            if (this.continueIfThereAreHazardConflicts()) {
                productGeneratorHandler.createProductsFromHazardEventSets(true,
                        action.getGeneratedProductsList());
                notifyModelEventsChanged();
            }
            break;
        case CORRECT:
            for (GeneratedProductList products : action
                    .getGeneratedProductsList()) {
                ProductInformation information = new ProductInformation();
                information.setProductGeneratorName(products.getProductInfo());
                ProductFormats productFormats = sessionConfigurationManager
                        .getProductGeneratorTable().getProductFormats(
                                information.getProductGeneratorName());
                information.setProductFormats(productFormats);
                information.setProducts(products);
                sessionManager.getProductManager().issueCorrection(information);
            }

            break;
        default:
            // do nothing
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
    private void handleProductDisplayContinueAction(boolean issue,
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
    private void handleTimerAction(Date caveTime) {
        if (appBuilder.getTimer().isAlive()
                && !appBuilder.getTimer().isInterrupted()) {

            try {
                updateCurrentTime(Originator.OTHER);
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
    private void handleUndoAction() {
        this.sessionManager.undo();
    }

    /**
     * Handles a redo action from the Console.
     * 
     * @param
     * @return
     */
    private void handleRedoAction() {
        this.sessionManager.redo();
    }

    /**
     * Update the visible time delta.
     */
    void updateConsoleVisibleTimeDelta() {
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
    private void requestMouseHandler(HazardServicesMouseHandlers mouseHandler,
            String... args) {
        appBuilder.requestMouseHandler(mouseHandler, args);
    }

    /**
     * Examines all hazards looking for potential conflicts.
     * 
     * @param
     * @return
     */
    private void checkHazardConflicts() {

        ISessionEventManager<ObservedHazardEvent> sessionEventManager = sessionManager
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
    private void toggleAutoCheckConflicts() {
        sessionManager.toggleAutoHazardChecking();
    }

    /**
     * Toggle on/off the display of hazard hatch areas.
     * 
     * @param
     * @return
     */
    private void toggleHatchedAreaDisplay() {
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

        ISessionEventManager<ObservedHazardEvent> sessionEventManager = sessionManager
                .getEventManager();

        Map<IHazardEvent, Map<IHazardEvent, Collection<String>>> conflictMap = sessionEventManager
                .getAllConflictingEvents();

        if (!conflictMap.isEmpty()) {
            userResponse = launchConflictingHazardsDialog(conflictMap, true);
        }

        return userResponse;
    }

    /**
     * This will no longer be needed once presenters listen directly for session
     * events.
     */
    @Handler
    @Deprecated
    public void settingsModified(final SettingsModified notification) {
        appBuilder.notifyModelChanged(EnumSet
                .of(HazardConstants.Element.CURRENT_SETTINGS));
    }

    /**
     * This will no longer be needed once presenters listen directly for session
     * events.
     */
    @Handler
    @Deprecated
    public void sessionEventsModified(final SessionEventsModified notification) {
        notifyModelEventsChanged();
    }

    /**
     * This will no longer be needed once presenters listen directly for session
     * events.
     */
    @Handler
    @Deprecated
    public void selectedTimeChanged(SelectedTimeChanged notification) {
        appBuilder.notifyModelChanged(EnumSet
                .of(HazardConstants.Element.SELECTED_TIME));
    }

    /**
     * Handle a received spatial display action. This method is called
     * implicitly by the event bus when actions of this type are sent across the
     * latter.
     * 
     * @param spatialDisplayAction
     *            Action received.
     */
    @Handler
    public void spatialDisplayActionOccurred(
            final SpatialDisplayAction spatialDisplayAction) {
        SpatialDisplayAction.ActionType actionType = spatialDisplayAction
                .getActionType();
        statusHandler.debug("SpatialDisplayActionOccurred actionType: "
                + actionType);
        switch (actionType) {
        case SELECTED_EVENTS_CHANGED:
            try {
                statusHandler.debug(getClass().getName()
                        + "spatialDisplayActionOccurred(): eventIDs: "
                        + spatialDisplayAction.getSelectedEventIDs());
                List<String> convertedEventIDs = Lists
                        .newArrayList(spatialDisplayAction
                                .getSelectedEventIDs());
                updateSelectedEvents(convertedEventIDs,
                        UIOriginator.SPATIAL_DISPLAY);

            } catch (VizException e) {
                statusHandler.error(getClass().getName()
                        + "spatialDisplayActionOccurred(): "
                        + "Unable to handle selected events changed.", e);
            }
            break;

        case ADD_PENDING_TO_SELECTED:
            setAddToSelected(spatialDisplayAction.getActionIdentifier());
            break;

        case ADD_GEOMETRY_TO_SELECTED:
            setAddGeometryToSelected(spatialDisplayAction.getActionIdentifier());

        case DRAWING:
            if (spatialDisplayAction.getActionIdentifier().equals(
                    SpatialDisplayAction.ActionIdentifier.SELECT_EVENT)) {
                // Activate the select hazard mouse handler
                requestMouseHandler(HazardServicesMouseHandlers.SINGLE_SELECTION);
            } else if (spatialDisplayAction.getActionIdentifier().equals(
                    SpatialDisplayAction.ActionIdentifier.DRAW_POLYGON)
                    || spatialDisplayAction.getActionIdentifier().equals(
                            SpatialDisplayAction.ActionIdentifier.DRAW_LINE)
                    || spatialDisplayAction.getActionIdentifier().equals(
                            SpatialDisplayAction.ActionIdentifier.DRAW_POINT)) {
                GeometryType geometryType = (spatialDisplayAction
                        .getActionIdentifier()
                        .equals(SpatialDisplayAction.ActionIdentifier.DRAW_POLYGON) ? GeometryType.POLYGON
                        : (spatialDisplayAction
                                .getActionIdentifier()
                                .equals(SpatialDisplayAction.ActionIdentifier.DRAW_LINE) ? GeometryType.LINE
                                : GeometryType.POINT));

                // Activate the hazard drawing mouse handler.
                requestMouseHandler(HazardServicesMouseHandlers.VERTEX_DRAWING,
                        geometryType.getValue());
            } else if (spatialDisplayAction
                    .getActionIdentifier()
                    .equals(SpatialDisplayAction.ActionIdentifier.DRAW_FREE_HAND_POLYGON)) {
                requestMouseHandler(HazardServicesMouseHandlers.FREEHAND_DRAWING);
            } else if (spatialDisplayAction.getActionIdentifier().equals(
                    SpatialDisplayAction.ActionIdentifier.SELECT_BY_AREA)) {
                String tableName = spatialDisplayAction.getMapsDbTableName();
                String displayName = spatialDisplayAction.getLegendName();
                requestMouseHandler(HazardServicesMouseHandlers.DRAW_BY_AREA,
                        tableName, displayName);
            }
            break;

        case DMTS:
            String lonLat = spatialDisplayAction.getDragToLongitude() + ","
                    + spatialDisplayAction.getDragToLatitude();
            runTool(lonLat, null, null);
            break;

        case CONTEXT_MENU_SELECTED:
            String label = spatialDisplayAction.getContextMenuLabel();
            handleContextMenuSelection(label);
            break;

        case DISPLAY_DISPOSED:
            eventBus.unsubscribe(this);
            closeHazardServices();
            break;

        case FRAME_CHANGED:
            sendFrameInformationToSessionManager();
            break;

        case RUN_TOOL:
            runTool(spatialDisplayAction.getToolName(),
                    spatialDisplayAction.getToolParameters(), null);
            break;

        case UPDATE_EVENT_METADATA:
            updateEventData(spatialDisplayAction.getToolParameters(), true,
                    spatialDisplayAction.getOriginator());
            break;

        case UNDO:
            handleUndoAction();
            break;

        case REDO:
            handleRedoAction();
            break;

        default:
            throw new UnsupportedOperationException(String.format(
                    "ActionType %s not handled", actionType));
        }

    }

    /**
     * Handle a received console display action. This method is called
     * implicitly by the event bus when actions of this type are sent across the
     * latter.
     * 
     * @param consoleAction
     *            Action received.
     */
    @Handler
    public void consoleActionOccurred(final ConsoleAction consoleAction) {
        switch (consoleAction.getActionType()) {
        case RESET:
            reset(consoleAction.getId());
            break;

        case CHANGE_MODE:
            if (consoleAction.getId().equals(ConsoleAction.CHECK_CONFLICTS)) {
                checkHazardConflicts();
            } else if (consoleAction.getId().equals(
                    ConsoleAction.AUTO_CHECK_CONFLICTS)) {
                toggleAutoCheckConflicts();
            } else if (consoleAction.getId().equals(
                    ConsoleAction.SHOW_HATCHED_AREA)) {
                toggleHatchedAreaDisplay();
            }
            break;

        case SELECTED_TIME_CHANGED:
            try {
                updateSelectedTime(consoleAction.getNewTime(),
                        UIOriginator.CONSOLE);
            } catch (VizException e) {
                statusHandler.error("HazardServicesMessageListener."
                        + "consoleActionOccurred(): Unable to update "
                        + "selected time.", e);
            }
            break;

        case VISIBLE_TIME_RANGE_CHANGED:
            updateVisibleTimeRange(consoleAction.getStartTime(),
                    consoleAction.getEndTime());
            break;

        case SELECTED_TIME_RANGE_CHANGED:
            updateSelectedTimeRange(consoleAction.getStartTime(),
                    consoleAction.getEndTime());
            break;

        case CHECK_BOX: {
            Map<String, Serializable> eventInfo = new HashMap<>();
            eventInfo.put(HAZARD_EVENT_IDENTIFIER, consoleAction.getId());
            eventInfo.put(HAZARD_EVENT_CHECKED, consoleAction.getChecked());
            updateEventData(eventInfo, true, consoleAction.getOriginator());
            break;
        }

        case SELECTED_EVENTS_CHANGED:

            try {
                List<String> eventIDsString = Lists.newArrayList(consoleAction
                        .getSelectedEventIDs());
                updateSelectedEvents(eventIDsString, UIOriginator.CONSOLE);

            } catch (VizException e) {
                statusHandler.error("Error updating selected events", e);
            }
            break;

        case EVENT_TIME_RANGE_CHANGED: {
            Map<String, Serializable> eventInfo = new HashMap<>();
            eventInfo.put(HAZARD_EVENT_IDENTIFIER, consoleAction.getId());
            eventInfo.put(HAZARD_EVENT_START_TIME,
                    Long.parseLong(consoleAction.getStartTime()));
            eventInfo.put(HAZARD_EVENT_END_TIME,
                    Long.parseLong(consoleAction.getEndTime()));
            updateEventData(eventInfo, true, consoleAction.getOriginator());
            break;
        }

        case EVENT_END_TIME_UNTIL_FURTHER_NOTICE_CHANGED: {
            Map<String, Serializable> eventInfo = new HashMap<>();
            eventInfo.put(HAZARD_EVENT_IDENTIFIER, consoleAction.getId());
            eventInfo.put(HAZARD_EVENT_END_TIME_UNTIL_FURTHER_NOTICE,
                    consoleAction.getChecked());
            updateEventData(eventInfo, true, consoleAction.getOriginator());
            break;
        }

        case SITE_CHANGED:
            updateSite(consoleAction.getId());
            break;

        case CLOSE:
            eventBus.unsubscribe(this);
            closeHazardServices();
            break;

        case RUN_AUTOMATED_TESTS:
            /*
             * Nothing to do here
             */
            break;

        default:
            throw new IllegalArgumentException("Unexpected action type "
                    + consoleAction.getActionType());
        }

    }

    /**
     * Handle a received hazard detail action. This method is called implicitly
     * by the event bus when actions of this type are sent across the latter.
     * 
     * @param hazardDetailAction
     *            Action received.
     */
    @Handler
    public void hazardDetailActionOccurred(
            final HazardDetailAction hazardDetailAction) {
        switch (hazardDetailAction.getActionType()) {
        case PREVIEW:
            preview();
            break;

        case PROPOSE:
            changeSelectedEventsToProposedState(hazardDetailAction
                    .getOriginator());
            break;

        case ISSUE:
            setIssuedState();
            break;

        case REVIEW:
            Map<String, Serializable> parameters = hazardDetailAction
                    .getParameters();
            ArrayList<ProductData> productData = (ArrayList<ProductData>) parameters
                    .get(ReviewAction.PRODUCT_DATA_PARAM);
            generateReviewableProduct(productData);
            break;
        default:
            throw new IllegalArgumentException("Unsupported actionType "
                    + hazardDetailAction.getActionType());
        }

    }

    /**
     * Handle a received product editor action. This method is called implicitly
     * by the event bus when actions of this type are sent across the latter.
     * 
     * @param productEditorAction
     *            Action received.
     */
    @Handler
    public void productEditorActionOccurred(
            final ProductEditorAction productEditorAction) {
        handleProductDisplayAction(productEditorAction);
    }

    /**
     * Handle a received product staging action. This method is called
     * implicitly by the event bus when actions of this type are sent across the
     * latter.
     * 
     * @param productStagingAction
     *            Action received.
     */
    @Handler
    public void productStagingActionOccurred(
            final ProductStagingAction productStagingAction) {

        // If the action equals Continue, that means we have to generate
        // product and make sure that we will issue those products or not.
        // Thus we need a return message that contains issueFlag and revised
        // productList
        handleProductDisplayContinueAction(productStagingAction.getIssueFlag()
                .equals(Boolean.TRUE.toString()),
                productStagingAction.getProductStagingInfo());

    }

    @Handler
    public void currentSettingsActionOccurred(
            final CurrentSettingsAction settingsAction) {
        changeCurrentSettings(settingsAction.getSettings());
    }

    /**
     * Handle a received settings action. This method is called implicitly by
     * the event bus when actions of this type are sent across the latter.
     * 
     * @param settingsAction
     *            Action received.
     */
    @Handler
    public void settingsActionOccurred(final StaticSettingsAction settingsAction) {
        switch (settingsAction.getActionType()) {

        case SETTINGS_MODIFIED:
            changeCurrentSettings(settingsAction.getSettings());
            break;

        case SETTINGS_CHOSEN:
            changeSetting(settingsAction.getSettingID(), true);
            break;
        case SAVE:
            saveSetting();
            break;
        case SAVE_AS:
            saveAsSetting(settingsAction.getSettings().getSettingsID());
            changeSetting(settingsAction.getSettingID(), true);
            break;
        default:
            Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                    .getShell();
            MessageDialog.openInformation(shell, null,
                    "This feature is not yet implemented.");

        }
    }

    /**
     * Handle a received timer action. This method is called implicitly by the
     * event bus when actions of this type are sent across the latter.
     * 
     * @param timerAction
     *            Action received.
     */
    @Handler
    public void timerActionOccurred(final TimerAction timerAction) {
        handleTimerAction(timerAction.getCaveTime());
    }

    /**
     * Handle a received tool action. This method is called implicitly by the
     * event bus when actions of this type are sent across the latter.
     * 
     * @param toolAction
     *            Action received.
     */
    @Handler
    public void toolActionOccurred(final ToolAction action) {
        switch (action.getActionType()) {
        case RUN_TOOL:
            runTool(action.getToolName());
            break;

        case RUN_TOOL_WITH_PARAMETERS:
            runTool(action.getToolName(), null, action.getAuxiliaryDetails());
            break;

        case TOOL_RECOMMENDATIONS:
            EventSet<IEvent> eventList = action.getRecommendedEventList();
            handleRecommenderResults(eventList);
            break;

        default:
            statusHandler
                    .debug("HazardServicesMessageListener: Unrecognized tool action :"
                            + action.getActionType());
            break;
        }

    }

    @Handler
    public void changeSiteOccurred(ChangeSiteAction action) {
        sessionManager.getConfigurationManager().setSiteID(action.getSite());
        Settings currentSetting = sessionManager.getConfigurationManager()
                .getSettings();
        currentSetting.getVisibleSites().add(action.getSite());
        ConsoleAction cAction = new ConsoleAction(
                ConsoleAction.ActionType.SITE_CHANGED);
        cAction.setId(action.getSite());
        consoleActionOccurred(cAction);
    }

    /**
     * Handle a received shut down action.
     * 
     * @param closeAction
     *            The Hazard Services shutdown notification.
     */
    @Handler
    public void closeActionOccurred(final HazardServicesCloseAction closeAction) {
        eventBus.unsubscribe(this);
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
                .getAvailableSettings();

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
                appBuilder.warnUser("Conflicting Hazards", message.toString());
            }
        }

        return userSelection;
    }

    @Override
    public void timechanged() {
        try {
            updateCurrentTime(Originator.OTHER);
        } catch (VizException e) {
            statusHandler.error("Error updating Hazard Services current time",
                    e);
        }
    }
}
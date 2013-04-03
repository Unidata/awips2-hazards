/**
 * This software was developed and / or modified by the National Oceanic and
 * Atmospheric Administration (NOAA), Earth System Research Laboratory (ESRL),
 * Global Systems Division (GSD), Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.display;

import gov.noaa.gsd.viz.hazards.display.action.ConsoleAction;
import gov.noaa.gsd.viz.hazards.display.action.HazardDetailAction;
import gov.noaa.gsd.viz.hazards.display.action.HazardServicesCloseAction;
import gov.noaa.gsd.viz.hazards.display.action.ProductEditorAction;
import gov.noaa.gsd.viz.hazards.display.action.ProductStagingAction;
import gov.noaa.gsd.viz.hazards.display.action.SettingsAction;
import gov.noaa.gsd.viz.hazards.display.action.SpatialDisplayAction;
import gov.noaa.gsd.viz.hazards.display.action.ToolAction;
import gov.noaa.gsd.viz.hazards.jsonutilities.Dict;
import gov.noaa.gsd.viz.hazards.spatialdisplay.HazardServicesMouseHandlers;
import gov.noaa.gsd.viz.hazards.timer.TimerAction;
import gov.noaa.gsd.viz.hazards.utilities.Utilities;
import gov.noaa.gsd.viz.mvp.EventBusSingleton;

import java.util.List;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.raytheon.uf.common.dataplugin.events.IEvent;
import com.raytheon.uf.common.hazards.productgen.IGeneratedProduct;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.core.exception.VizException;

/**
 * Description: Handles messages from Hazard Services components which are
 * received over the Google EventBus. When a message is received, this class
 * swill do one of two things: 1) If the action requires an update to model
 * state, then the action is delegated to the message handler. 2) If the action
 * only affects the display or undisplay of other Hazard Services components,
 * then the app builder is called.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 28, 2013            bryon.lawrence      Initial creation
 * 
 * </pre>
 * 
 * @author bryon.lawrence
 * @version 1.0
 */
public class HazardServicesMessageListener {

    // Private Static Constants

    /**
     * Logging mechanism.
     */
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(HazardServicesMessageListener.class);

    private static final String DIALOG_INFO_KEY = "dialogInfo";

    private static final String SPATIAL_INFO_KEY = "spatialInfo";

    // Private Constants

    /**
     * Event bus to which this listener is subscribed.
     */
    private final EventBus eventBus;

    /**
     * Handler of messages received by this listener.
     */
    private final HazardServicesMessageHandler messageHandler;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param messageHandler
     *            An instance of the hazard services message handler. This
     *            object mediates the display interactions according to the
     *            forecast process.
     */
    public HazardServicesMessageListener(
            HazardServicesMessageHandler messageHandler) {
        this.eventBus = EventBusSingleton.getInstance();
        this.eventBus.register(this);
        this.messageHandler = messageHandler;
    }

    // Public Methods

    /**
     * Handle a received spatial display action. This method is called
     * implicitly by the event bus when actions of this type are sent across the
     * latter.
     * 
     * @param spatialDisplayAction
     *            Action received.
     */
    @Subscribe
    public void spatialDisplayActionOccurred(
            final SpatialDisplayAction spatialDisplayAction) {
        String actionType = spatialDisplayAction.getActionType();
        statusHandler
                .debug("Mediator SpatialDisplayActionOccurred actionType: "
                        + actionType);

        if (actionType.equals("SelectedEventsChanged")) {
            try {
                statusHandler.debug("HazardServicesMessageListener."
                        + "spatialDisplayActionOccurred(): eventIDs: "
                        + spatialDisplayAction.getSelectedEventIDs());
                String convertedEventIDs = HazardServicesMessageHandler
                        .convertEventIDs(spatialDisplayAction
                                .getSelectedEventIDs());
                messageHandler.updateSelectedEvents(convertedEventIDs,
                        spatialDisplayAction.isMultipleSelection(), "Spatial");

            } catch (VizException e) {
                statusHandler.error("HazardServicesMessageListener."
                        + "spatialDisplayActionOccurred(): "
                        + "Unable to handle selected events changed.", e);
            }
        } else if (actionType.equals("ModifyEventArea")) {
            String jsonText = spatialDisplayAction.getModifyEventJSON();

            try {
                messageHandler.modifySpatialDisplayObject(jsonText);
            } catch (VizException e) {
                statusHandler.error("HazardServicesMessageListener."
                        + "spatialDisplayActionOccurred(): "
                        + "Unable to modify event area.", e);
            }
        } else if (actionType.equals("addToSelected")) {
            messageHandler.setAddToSelected(spatialDisplayAction
                    .getActionIdentifier());
        } else if (actionType.equals("Drawing")) {
            if (spatialDisplayAction.getActionIdentifier().equalsIgnoreCase(
                    "SelectMultiEvents")) {
                // Activate the multiple select hazard mouse handler
                messageHandler
                        .requestMouseHandler(HazardServicesMouseHandlers.MULTI_SELECTION);
            } else if (spatialDisplayAction.getActionIdentifier()
                    .equalsIgnoreCase("SelectEvent")) {
                // Activate the select hazard mouse handler
                messageHandler
                        .requestMouseHandler(HazardServicesMouseHandlers.SINGLE_SELECTION);
            } else if (spatialDisplayAction.getActionIdentifier()
                    .equalsIgnoreCase("drawPolygon")
                    || spatialDisplayAction.getActionIdentifier()
                            .equalsIgnoreCase("drawLine")
                    || spatialDisplayAction.getActionIdentifier()
                            .equalsIgnoreCase("drawPoint")) {

                // Activate the hazard drawing mouse handler.
                messageHandler
                        .requestMouseHandler(HazardServicesMouseHandlers.EVENTBOX_DRAWING);
            } else if (spatialDisplayAction.getActionIdentifier().equals(
                    "DrawFreeHandPolygon")) {
                messageHandler
                        .requestMouseHandler(HazardServicesMouseHandlers.FREEHAND_DRAWING);
            } else if (spatialDisplayAction.getActionIdentifier().equals(
                    "SelectByArea")) {
                String tableName = spatialDisplayAction.getMapsDbTableName();
                String displayName = spatialDisplayAction.getLegendName();
                messageHandler.requestMouseHandler(
                        HazardServicesMouseHandlers.DRAW_BY_AREA, tableName,
                        displayName);
            }
        } else if (actionType.equals("DMTS")) {
            String lonLat = spatialDisplayAction.getDragToLongitude() + ","
                    + spatialDisplayAction.getDragToLatitude();
            messageHandler.runTool(lonLat, null, null);
        } else if (actionType.equals("ContextMenuSelected")) {
            String label = spatialDisplayAction.getContextMenuLabel();
            messageHandler.handleContextMenuSelection(label);
        } else if (actionType.equals("DisplayDisposed")) {
            eventBus.unregister(this);
            messageHandler.closeHazardServices();
        } else if (actionType.equals("FrameChanged")) {
            messageHandler.sendFrameInformationToSessionManager();
        } else if (actionType.equals("runTool")) {
            messageHandler.runTool(spatialDisplayAction.getToolName(),
                    SPATIAL_INFO_KEY, spatialDisplayAction.getJSON());
        } else if (actionType.equals("newEventArea")) {
            messageHandler.newEventArea(spatialDisplayAction.getJSON(),
                    spatialDisplayAction.getEventID(), "Spatial");
        } else if (actionType.equals("updateEventData")) {
            messageHandler.updateEventData(spatialDisplayAction.getJSON(), "");
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
    @Subscribe
    public void consoleActionOccurred(final ConsoleAction consoleAction) {
        if (consoleAction.getAction().equals("Reset")) {
            messageHandler.reset(consoleAction.getId());
        } else if (consoleAction.getAction().equals("SelectedTimeChanged")) {
            try {
                messageHandler.updateSelectedTime(consoleAction.getNewTime(),
                        HazardServicesAppBuilder.TEMPORAL_ORIGINATOR);
            } catch (VizException e) {
                statusHandler.error("HazardServicesMessageListener."
                        + "consoleActionOccurred(): Unable to update "
                        + "selected time.", e);
            }
        }

        else if (consoleAction.getAction().equals("VisibleTimeRangeChanged")) {
            messageHandler.updateVisibleTimeRange(consoleAction.getStartTime(),
                    consoleAction.getEndTime(),
                    HazardServicesAppBuilder.TEMPORAL_ORIGINATOR);
        } else if (consoleAction.getAction().equals("SelectedTimeRangeChanged")) {
            messageHandler.updateSelectedTimeRange(
                    consoleAction.getStartTime(), consoleAction.getEndTime(),
                    HazardServicesAppBuilder.TEMPORAL_ORIGINATOR);
        } else if (consoleAction.getAction().equals("CheckBox")) {
            Dict eventInfo = new Dict();
            eventInfo.put(Utilities.HAZARD_EVENT_IDENTIFIER,
                    consoleAction.getId());
            eventInfo.put(Utilities.HAZARD_EVENT_CHECKED,
                    consoleAction.getChecked());
            String jsonText = eventInfo.toJSONString();
            messageHandler.updateEventData(jsonText,
                    HazardServicesAppBuilder.TEMPORAL_ORIGINATOR);
        } else if (consoleAction.getAction().equals("Redraw")) {

            // Do nothing at this point; this used to clear and
            // repopulate the temporal display's hazard event list,
            // but this was not needed. We kept the event around
            // in case in the future the Mediator needs to know
            // about temporal display redraws.
        } else if (consoleAction.getAction().equals("SelectedEventsChanged")) {
            try {
                String eventIDsString = HazardServicesMessageHandler
                        .convertEventIDs(consoleAction.getSelectedEventIDs());
                messageHandler.updateSelectedEvents(eventIDsString, false,
                        "Temporal");

            } catch (VizException e) {
                statusHandler.error("Error updating selected events", e);
            }
        } else if (consoleAction.getAction().equals("EventTimeRangeChanged")) {
            Dict eventInfo = new Dict();
            eventInfo.put(Utilities.HAZARD_EVENT_IDENTIFIER,
                    consoleAction.getId());
            eventInfo.put(Utilities.HAZARD_EVENT_START_TIME,
                    Long.parseLong(consoleAction.getStartTime()));
            eventInfo.put(Utilities.HAZARD_EVENT_END_TIME,
                    Long.parseLong(consoleAction.getEndTime()));
            messageHandler.updateEventData(eventInfo.toJSONString(),
                    HazardServicesAppBuilder.TEMPORAL_ORIGINATOR);
        } else if (consoleAction.getAction().equals("Close")) {
            eventBus.unregister(this);
            messageHandler.closeHazardServices();
        }
    }

    /**
     * Handle a received hazard detail action. This method is called implicitly
     * by the event bus when actions of this type are sent across the latter.
     * 
     * @param hazardDetailAction
     *            Action received.
     */
    @Subscribe
    public void hazardDetailActionOccurred(
            final HazardDetailAction hazardDetailAction) {
        if (hazardDetailAction.getAction().equalsIgnoreCase("Preview")) {
            messageHandler.preview();
        } else if (hazardDetailAction.getAction().equalsIgnoreCase("Propose")) {
            messageHandler.setProposedState();
        } else if (hazardDetailAction.getAction().equalsIgnoreCase("Issue")) {
            messageHandler.setIssuedState();
        } else if (hazardDetailAction.getAction().equalsIgnoreCase("Dismiss")) {
            messageHandler.setDismissedState();
        } else if (hazardDetailAction.getAction().equalsIgnoreCase(
                "updateTimeRange")) {
            messageHandler.updateEventData(hazardDetailAction.getJSONText(),
                    HazardServicesAppBuilder.HAZARD_INFO_ORIGINATOR);
        } else if (hazardDetailAction.getAction().equalsIgnoreCase(
                "updateEventType")) {
            messageHandler.updateEventType(hazardDetailAction.getJSONText());
        } else if (hazardDetailAction.getAction().equalsIgnoreCase(
                "updateEventMetadata")) {
            messageHandler.updateEventData(hazardDetailAction.getJSONText(),
                    HazardServicesAppBuilder.HAZARD_INFO_ORIGINATOR);
        } else {
            messageHandler.handleAction(hazardDetailAction.getAction(),
                    hazardDetailAction.getJSONText());
        }
    }

    /**
     * Handle a received product editor action. This method is called implicitly
     * by the event bus when actions of this type are sent across the latter.
     * 
     * @param productEditorAction
     *            Action received.
     */
    @Subscribe
    public void productEditorActionOccurred(
            final ProductEditorAction productEditorAction) {
        messageHandler.handleProductDisplayAction(productEditorAction);
    }

    /**
     * Handle a received product staging action. This method is called
     * implicitly by the event bus when actions of this type are sent across the
     * latter.
     * 
     * @param productStagingAction
     *            Action received.
     */
    @Subscribe
    public void productStagingActionOccurred(
            final ProductStagingAction productStagingAction) {
        if (productStagingAction.getAction().equals("Continue")) {

            // If the action equals Continue, that means we have to generate
            // product and make sure that we will issue those products or not.
            // Thus we need a return message that contains issueFlag and revised
            // productList
            messageHandler.handleProductDisplayContinueAction(
                    productStagingAction.getIssueFlag(),
                    productStagingAction.getJSONText());
        }
    }

    /**
     * Handle a received settings action. This method is called implicitly by
     * the event bus when actions of this type are sent across the latter.
     * 
     * @param settingsAction
     *            Action received.
     */
    @Subscribe
    public void settingsActionOccurred(final SettingsAction settingsAction) {
        if (settingsAction.getAction().equals("DynamicSettingChanged")) {
            messageHandler.dynamicSettingChanged(settingsAction.getDetail());
        } else if (settingsAction.getAction().equals("SettingChosen")) {
            messageHandler
                    .changeSetting(settingsAction.getDetail(), true, true);
        } else if (settingsAction.getAction().equals("Save")) {
            messageHandler.updateStaticSetting(settingsAction.getDetail());
        } else if (settingsAction.getAction().equals("Save As")) {
            messageHandler.createStaticSetting(settingsAction.getDetail());
        }
    }

    /**
     * Handle a received timer action. This method is called implicitly by the
     * event bus when actions of this type are sent across the latter.
     * 
     * @param timerAction
     *            Action received.
     */
    @Subscribe
    public void timerActionOccurred(final TimerAction timerAction) {
        messageHandler.handleTimerAction(timerAction.getCaveTime());
    }

    /**
     * Handle a received tool action. This method is called implicitly by the
     * event bus when actions of this type are sent across the latter.
     * 
     * @param toolAction
     *            Action received.
     */
    @Subscribe
    public void toolActionOccurred(final ToolAction action) {
        switch (action.getAction()) {
        case RUN_TOOL:
            messageHandler.runTool(action.getDetails());
            break;

        case RUN_TOOL_WITH_PARAMETERS:
            messageHandler.runTool(action.getDetails(), DIALOG_INFO_KEY,
                    action.getAuxiliaryDetails());
            break;

        case TOOL_RECOMMENDATIONS:
            String toolID = action.getAuxiliaryDetails();
            List<IEvent> eventList = action.getRecommendedEventList();
            messageHandler.handleRecommenderResults(toolID, eventList);
            break;

        case PRODUCTS_GENERATED:
            toolID = action.getAuxiliaryDetails();
            List<IGeneratedProduct> productList = action.getProductList();
            messageHandler.handleProductGeneratorResult(toolID, productList);
            break;

        default:
            statusHandler
                    .debug("HazardServicesMessageListener: Unrecognized tool action :"
                            + action.getAction());
            break;
        }

    }

    /**
     * Handle a received shut down action.
     * 
     * @param closeAction
     *            The Hazard Services shutdown notification.
     */
    @Subscribe
    public void HazardServicesShutDownListener(
            final HazardServicesCloseAction closeAction) {
        eventBus.unregister(this);
    }
}

/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.display.test;

import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.*;
import gov.noaa.gsd.viz.hazards.display.HazardServicesAppBuilder;
import gov.noaa.gsd.viz.hazards.display.action.ConsoleAction;
import gov.noaa.gsd.viz.hazards.display.action.CurrentSettingsAction;
import gov.noaa.gsd.viz.hazards.display.action.HazardDetailAction;
import gov.noaa.gsd.viz.hazards.display.action.SpatialDisplayAction;
import gov.noaa.gsd.viz.hazards.display.action.StaticSettingsAction;
import gov.noaa.gsd.viz.hazards.display.action.ToolAction;
import gov.noaa.gsd.viz.hazards.jsonutilities.Dict;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;
import com.raytheon.uf.common.dataplugin.events.EventSet;
import com.raytheon.uf.common.dataplugin.events.IEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardState;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Settings;

/**
 * Description: {@link FunctionalTest} of storm track tool.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Dec 11, 2013 2182       daniel.s.schaffer@noaa.gov      Initial creation
 * 
 * </pre>
 * 
 * @author daniel.s.schaffer@noaa.gov
 * @version 1.0
 */
public class StormTrackFunctionalTest extends FunctionalTest {

    private Settings savedCurrentSettings;

    private enum Steps {
        START, MODIFY_TOOL, CHANGE_BACK_CURRENT_SETTINGS

    }

    private Steps step;

    private String eventID;

    public StormTrackFunctionalTest(HazardServicesAppBuilder appBuilder) {
        super(appBuilder);
    }

    @Subscribe
    public void consoleActionOccurred(final ConsoleAction consoleAction) {
        step = Steps.START;
        savedCurrentSettings = appBuilder.getCurrentSettings();
        autoTestUtilities.changeStaticSettings(CANNED_TORNADO_SETTING);

    }

    @Subscribe
    public void spatialDisplayActionOccurred(
            final SpatialDisplayAction spatialDisplayAction) {

        try {
            switch (step) {
            case START:
                break;
            default:
                break;

            }

        } catch (Exception e) {
            handleException(e);
        }
    }

    @Subscribe
    public void hazardDetailActionOccurred(
            final HazardDetailAction hazardDetailAction) {
        try {
            switch (step) {

            default:
                break;

            }
        } catch (Exception e) {
            handleException(e);
        }
    }

    @Subscribe
    public void toolActionOccurred(final ToolAction action) {
        try {
            switch (action.getActionType()) {
            case TOOL_RECOMMENDATIONS:
                if (step.equals(Steps.START)) {
                    EventSet<IEvent> events = action.getRecommendedEventList();
                    assertEquals(events.size(), 1);
                    IHazardEvent event = (IHazardEvent) events.iterator()
                            .next();
                    assertEquals(event.getHazardType(), "FF.W.Convective");
                    assertEquals(event.getState(), HazardState.PENDING);
                    Dict consoleEvent = mockConsoleView.getHazardEvents()
                            .get(0);
                    eventID = consoleEvent
                            .getDynamicallyTypedValue(HAZARD_EVENT_IDENTIFIER);
                    @SuppressWarnings("unchecked")
                    List<Map<String, Serializable>> trackPoints = (List<Map<String, Serializable>>) event
                            .getHazardAttribute(TRACK_POINTS);
                    Map<String, Serializable> lastPoint = trackPoints
                            .get(trackPoints.size() - 1);
                    Long pointID = (Long) lastPoint.get(POINTID);
                    Dict toolParameters = new Dict();
                    toolParameters.put(POINTID, pointID);
                    toolParameters.put(HAZARD_EVENT_IDENTIFIER, eventID);
                    toolParameters.put(HAZARD_EVENT_SHAPE_TYPE,
                            HAZARD_EVENT_SHAPE_TYPE_DOT);
                    List<Double> point = Lists.newArrayList(-98.76, 40.29);
                    toolParameters.put(SYMBOL_NEW_LAT_LON, point);

                    SpatialDisplayAction modifyAction = new SpatialDisplayAction(
                            SpatialDisplayAction.ActionType.RUN_TOOL,
                            MODIFY_STORM_TRACK_TOOL, toolParameters);
                    step = Steps.MODIFY_TOOL;
                    eventBus.post(modifyAction);
                } else {
                    EventSet<IEvent> events = action.getRecommendedEventList();
                    assertEquals(events.size(), 1);
                    IHazardEvent event = (IHazardEvent) events.iterator()
                            .next();
                    assertEquals(event.getState(), HazardState.PENDING);
                    assertEquals(event.getEventID(), eventID);
                    autoTestUtilities
                            .changeStaticSettings(CANNED_FLOOD_SETTING);
                }

                break;

            default:
                testError();

            }
        } catch (Exception e) {
            handleException(e);
        }
    }

    @Subscribe
    public void staticSettingsActionOccurred(
            final StaticSettingsAction settingsAction) {
        try {
            switch (step) {

            case START:
                /*
                 * We'll skip over the "drag me part" because it's too hard to
                 * test that the dot appears. We'll start after the dot has been
                 * dragged.
                 */
                Dict toolParameters = new Dict();
                Dict pointsDict = new Dict();
                toolParameters.put(SPATIAL_INFO, pointsDict);
                List<Object> points = Lists.newArrayList();
                pointsDict.put(POINTS, points);
                List<Object> outerList = Lists.newArrayList();
                points.add(outerList);
                List<Double> xyLoc = Lists.newArrayList(41.22, -97.10);
                Double zLoc = 1297137600.0;
                outerList.add(xyLoc);
                outerList.add(zLoc);

                // step = Steps.RUN_TOOL;
                SpatialDisplayAction action = new SpatialDisplayAction(
                        SpatialDisplayAction.ActionType.RUN_TOOL,
                        STORM_TRACK_TOOL, toolParameters);
                eventBus.post(action);
                break;

            case MODIFY_TOOL:
                step = Steps.CHANGE_BACK_CURRENT_SETTINGS;
                autoTestUtilities.changeCurrentSettings(savedCurrentSettings);
                break;

            default:
                testError();
                break;

            }
        } catch (Exception e) {
            handleException(e);
        }

    }

    @Subscribe
    public void currrentSettingsActionOccurred(
            final CurrentSettingsAction settingsAction) {
        try {
            testSuccess();

        } catch (Exception e) {
            handleException(e);
        }

    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}

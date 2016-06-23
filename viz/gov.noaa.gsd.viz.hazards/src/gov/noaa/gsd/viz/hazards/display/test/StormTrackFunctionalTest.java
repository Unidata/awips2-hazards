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

import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HAZARD_EVENT_IDENTIFIER;
import gov.noaa.gsd.common.visuals.VisualFeature;
import gov.noaa.gsd.common.visuals.VisualFeaturesList;
import gov.noaa.gsd.common.visuals.VisualFeaturesListJsonConverter;
import gov.noaa.gsd.viz.hazards.UIOriginator;
import gov.noaa.gsd.viz.hazards.display.HazardServicesAppBuilder;
import gov.noaa.gsd.viz.hazards.display.action.CurrentSettingsAction;
import gov.noaa.gsd.viz.hazards.display.action.StaticSettingsAction;
import gov.noaa.gsd.viz.hazards.display.action.ToolAction;
import gov.noaa.gsd.viz.hazards.jsonutilities.Dict;

import java.util.Date;

import net.engio.mbassy.listener.Handler;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.raytheon.uf.common.dataplugin.events.EventSet;
import com.raytheon.uf.common.dataplugin.events.IEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardStatus;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.ISettings;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventAdded;
import com.raytheon.uf.viz.hazards.sessionmanager.events.impl.ObservedHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.recommenders.RecommenderExecutionContext;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;

/**
 * Description: {@link FunctionalTest} of storm track tool.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Dec 11, 2013 2182       daniel.s.schaffer@noaa.gov      Initial creation
 * Apr 09, 2014 2925       Chris.Golden Fixed to work with new HID event propagation.
 * May 18, 2014 2925       Chris.Golden More changes to get it to work with the new HID.
 *                                      Also changed to ensure that ongoing preview and
 *                                      ongoing issue flags are set to false at the end
 *                                      of each test, and moved the steps enum into the
 *                                      base class.
 * Dec 05, 2014 4124       Chris.Golden Changed to work with ObservedSettings.
 * Dec 13, 2014 4959       Dan Schaffer Spatial Display cleanup and other bug fixes
 * </pre>
 * 
 * @author daniel.s.schaffer@noaa.gov
 * @version 1.0
 */
public class StormTrackFunctionalTest extends
        FunctionalTest<StormTrackFunctionalTest.Steps> {

    @SuppressWarnings("unused")
    private final IUFStatusHandler statusHandler = UFStatus
            .getHandler(getClass());

    private ISettings savedCurrentSettings;

    protected enum Steps {
        START, MODIFY_TOOL, CHANGE_BACK_CURRENT_SETTINGS

    }

    private String eventID;

    public StormTrackFunctionalTest(HazardServicesAppBuilder appBuilder) {
        super(appBuilder);
    }

    @Override
    protected void runFirstStep() {

        step = Steps.START;
        savedCurrentSettings = appBuilder.getCurrentSettings();
        autoTestUtilities.changeStaticSettings(CANNED_TORNADO_SETTING);

    }

    @Handler(priority = -1)
    public void handleSessionEventAdded(SessionEventAdded notification) {
        if (step.equals(Steps.START)) {
            Dict consoleEvent = mockConsoleView.getHazardEvents().get(0);
            eventID = consoleEvent
                    .getDynamicallyTypedValue(HAZARD_EVENT_IDENTIFIER);
            ObservedHazardEvent event = eventManager.getEventById(eventID);
            VisualFeaturesList visualFeatures = event.getVisualFeatures();
            VisualFeature lastPoint = null;
            for (int j = visualFeatures.size() - 1; j >= 0; j--) {
                if (visualFeatures.get(j).getIdentifier().matches("^[-0-9.]+$")) {
                    lastPoint = visualFeatures.get(j);
                    break;
                }
            }
            lastPoint.setGeometry(new Date(sessionManager.getTimeManager()
                    .getSelectedTime().getLowerBound()), new GeometryFactory()
                    .createPoint(new Coordinate(-98.76, 40.29)));
            event.setVisualFeature(lastPoint, UIOriginator.SPATIAL_DISPLAY);
            stepCompleted();
            step = Steps.MODIFY_TOOL;
        }
    }

    @Handler(priority = -1)
    public void toolActionOccurred(final ToolAction action) {
        try {
            switch (action.getRecommenderActionType()) {
            case RECOMMENDATIONS:
                if (step.equals(Steps.START)) {
                    EventSet<IEvent> events = action.getRecommendedEventList();
                    assertEquals(events.size(), 1);
                    IHazardEvent event = (IHazardEvent) events.iterator()
                            .next();
                    assertEquals(event.getHazardType(), "FF.W.Convective");
                    assertEquals(event.getStatus(), HazardStatus.PENDING);
                } else {
                    EventSet<IEvent> events = action.getRecommendedEventList();
                    assertEquals(events.size(), 1);
                    IHazardEvent event = (IHazardEvent) events.iterator()
                            .next();
                    assertEquals(event.getStatus(), HazardStatus.PENDING);
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

    @Handler(priority = -1)
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
                VisualFeaturesList visualFeatures = VisualFeaturesListJsonConverter
                        .fromJson(" [ { "
                                + "\"identifier\": \"stormTrackDot\", "
                                + "\"geometry\": \"POINT(-97.10, 41.22)\""
                                + " } ]");
                sessionManager.getRecommenderManager().runRecommender(
                        STORM_TRACK_TOOL,
                        RecommenderExecutionContext.getEmptyContext(),
                        visualFeatures, null);
                break;

            case MODIFY_TOOL:
                stepCompleted();
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

    @Handler(priority = -1)
    public void currrentSettingsActionOccurred(
            final CurrentSettingsAction settingsAction) {
        try {
            stepCompleted();
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

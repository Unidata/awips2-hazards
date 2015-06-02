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

import static gov.noaa.gsd.viz.hazards.display.test.AutoTestUtilities.CON_VTEC_STRING;
import static gov.noaa.gsd.viz.hazards.display.test.AutoTestUtilities.EXA_VTEC_STRING;
import static gov.noaa.gsd.viz.hazards.display.test.AutoTestUtilities.FLASH_FLOOD_WATCH_PHEN_SIG;
import gov.noaa.gsd.viz.hazards.display.HazardServicesAppBuilder;

import java.util.List;

import net.engio.mbassy.listener.Handler;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventAdded;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventGeometryModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventStatusModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventTypeModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.impl.ObservedHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.product.IProductGenerationComplete;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

/**
 * Description: {@link FunctionalTest} of changing the area of an event.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 22, 2013 2166       daniel.s.schaffer@noaa.gov      Initial creation
 * Nov  04, 2013   2182     daniel.s.schaffer@noaa.gov      Started refactoring
 * Nov 15, 2013  2182       daniel.s.schaffer@noaa.gov    Refactoring JSON - ProductStagingDialog
 * Nov 16, 2013  2166       daniel.s.schaffer@noaa.gov    Using new utility
 * Jan 10, 2014  2890      bkowal      Now subscribes to a notification indicating that
 *                                     all product generation is complete.
 * Apr 09, 2014    2925       Chris.Golden Fixed to work with new HID event propagation.
 * May 18, 2014    2925       Chris.Golden More changes to get it to work with the new HID.
 *                                         Also changed to ensure that ongoing preview and
 *                                         ongoing issue flags are set to false at the end
 *                                         of each test, and moved the steps enum into the
 *                                         base class.
 * Dec 13, 2014 4959       Dan Schaffer Spatial Display cleanup and other bug fixes
 * </pre>
 * 
 * @author daniel.s.schaffer@noaa.gov
 * @version 1.0
 */
public class ChangeHazardAreaFunctionalTest extends
        FunctionalTest<ChangeHazardAreaFunctionalTest.Steps> {

    @SuppressWarnings("unused")
    private final IUFStatusHandler statusHandler = UFStatus
            .getHandler(getClass());

    protected enum Steps {
        START, ISSUE_FLASH_FLOOD_WATCH, READY_FOR_PREVIEW, PREVIEW_MODIFIED_EVENT, TEST_ENDED
    }

    public ChangeHazardAreaFunctionalTest(HazardServicesAppBuilder appBuilder) {
        super(appBuilder);
    }

    @Override
    protected void runFirstStep() {
        step = Steps.START;
        autoTestUtilities.createEvent(-96.0, 41.0);
    }

    boolean productEditorHasBeenUpdated = false;

    @Handler(priority = -1)
    public void sessionModifiedOccurred(final SessionModified action) {
        if ((step == Steps.PREVIEW_MODIFIED_EVENT)
                && (appBuilder.getSessionManager().isIssueOngoing() == false)
                && appBuilder.getSessionManager().isPreviewOngoing()) {
            productEditorHasBeenUpdated = true;
        } else if ((step == Steps.TEST_ENDED)
                && (appBuilder.getSessionManager().isIssueOngoing() == false)
                && (appBuilder.getSessionManager().isPreviewOngoing() == false)) {
            stepCompleted();
            testSuccess();
        }
    }

    @Handler(priority = -1)
    public void sessionEventStateModifiedOccurred(
            final SessionEventStatusModified action) {

        try {
            switch (step) {

            case ISSUE_FLASH_FLOOD_WATCH:
                handleCompletedIssuance();
                break;

            default:

            }
        } catch (Exception e) {
            handleException(e);
        }
    }

    @Handler(priority = -1)
    public void sessionEventAddedOccurred(SessionEventAdded action) {
        try {
            switch (step) {
            case START:
                autoTestUtilities
                        .assignSelectedEventType(AutoTestUtilities.FLASH_FLOOD_WATCH_FULLTYPE);
                break;

            default:
                break;
            }

        } catch (Exception e) {
            handleException(e);
        }
    }

    @Handler(priority = -1)
    public void sessionEventTypeModifiedOccurred(SessionEventTypeModified action) {
        try {
            if (step == Steps.START) {
                ObservedHazardEvent event = autoTestUtilities
                        .getSelectedEvent();
                if (!"FF".equals(event.getPhenomenon())
                        || !"A".equals(event.getSignificance())) {
                    return;
                }
                stepCompleted();

                step = Steps.ISSUE_FLASH_FLOOD_WATCH;
                autoTestUtilities.issueFromHID();
            }
        } catch (Exception e) {
            handleException(e);
        }
    }

    @Handler(priority = -1)
    public void handleHazardGeometryModification(
            SessionEventGeometryModified action) {

        try {
            switch (step) {

            case READY_FOR_PREVIEW:
                stepCompleted();
                step = Steps.PREVIEW_MODIFIED_EVENT;
                autoTestUtilities.previewFromHID();
                break;

            default:
                break;

            }

        } catch (Exception e) {
            handleException(e);
        }
    }

    @Handler(priority = -1)
    public void handleProductGeneratorResult(
            final IProductGenerationComplete productGenerationComplete) {
        try {
            switch (step) {

            case PREVIEW_MODIFIED_EVENT:
                if (productEditorHasBeenUpdated == false) {
                    return;
                }
                List<String> products = autoTestUtilities
                        .legacyProductsFromEditorView(mockProductEditorView);
                assertEquals(products.size(), 1);
                String legacy = products.get(0);
                assertTrue(legacy.contains(EXA_VTEC_STRING + "."
                        + FLASH_FLOOD_WATCH_PHEN_SIG));
                assertTrue(legacy.contains(CON_VTEC_STRING + "."
                        + FLASH_FLOOD_WATCH_PHEN_SIG));
                stepCompleted();
                step = Steps.TEST_ENDED;

                /*
                 * Preview ongoing needs to be reset, since a preview was
                 * started but is being canceled.
                 */
                mockProductEditorView.invokeDismissButton();
                break;

            case ISSUE_FLASH_FLOOD_WATCH:
                handleCompletedIssuance();
                break;

            default:

            }
        } catch (Exception e) {
            handleException(e);
        }

    }

    private void handleCompletedIssuance() {

        ObservedHazardEvent event = autoTestUtilities.getSelectedEvent();
        Geometry geometry = event.getGeometry();
        Coordinate[] coordinates = geometry.getCoordinates();
        Coordinate modifiedPoint = coordinates[1];

        modifiedPoint.y = 42.0;
        SessionEventGeometryModified newAction = new SessionEventGeometryModified(
                eventManager, event, null);
        stepCompleted();
        step = Steps.READY_FOR_PREVIEW;
        eventBus.publishAsync(newAction);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}

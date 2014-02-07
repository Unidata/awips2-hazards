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
import gov.noaa.gsd.viz.hazards.display.action.ConsoleAction;
import gov.noaa.gsd.viz.hazards.display.action.HazardDetailAction;
import gov.noaa.gsd.viz.hazards.jsonutilities.Dict;
import gov.noaa.gsd.viz.hazards.productstaging.ProductConstants;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.google.common.eventbus.Subscribe;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventAdded;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventGeometryModified;
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
 * </pre>
 * 
 * @author daniel.s.schaffer@noaa.gov
 * @version 1.0
 */
public class ChangeHazardAreaFunctionalTest extends FunctionalTest {

    private enum Steps {
        START, ISSUE_FLASH_FLOOD_WATCH, READY_FOR_PREVIEW, PREVIEW_MODIFIED_EVENT
    }

    private Steps step;

    public ChangeHazardAreaFunctionalTest(HazardServicesAppBuilder appBuilder) {
        super(appBuilder);
    }

    @Subscribe
    public void consoleActionOccurred(final ConsoleAction consoleAction) {
        step = Steps.START;

        autoTestUtilities.createEvent(-96.0, 41.0);
    }

    @Subscribe
    public void handleNewHazard(SessionEventAdded action) {
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

    @Subscribe
    public void handleHazardGeometryModification(
            SessionEventGeometryModified action) {

        try {
            switch (step) {

            case READY_FOR_PREVIEW:
                step = Steps.PREVIEW_MODIFIED_EVENT;
                autoTestUtilities.previewEvent();
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
            if (step == Steps.START) {
                step = Steps.ISSUE_FLASH_FLOOD_WATCH;
                autoTestUtilities.issueEvent();
            }
        } catch (Exception e) {
            handleException(e);
        }

    }

    @Subscribe
    public void handleProductGeneratorResult(
            final IProductGenerationComplete productGenerationComplete) {
        try {
            switch (step) {

            case ISSUE_FLASH_FLOOD_WATCH:
                IHazardEvent event = autoTestUtilities.getSelectedEvent();
                Geometry geometry = event.getGeometry();
                Coordinate[] coordinates = geometry.getCoordinates();
                Coordinate modifiedPoint = coordinates[1];

                modifiedPoint.y = 42.0;
                SessionEventGeometryModified action = new SessionEventGeometryModified(
                        eventManager, event);
                step = Steps.READY_FOR_PREVIEW;
                eventBus.post(action);
                break;

            case PREVIEW_MODIFIED_EVENT:

                Dict products = autoTestUtilities
                        .productsFromEditorView(mockProductEditorView);
                String legacy = products
                        .getDynamicallyTypedValue(ProductConstants.ASCII_PRODUCT_KEY);
                assertTrue(legacy.contains(EXA_VTEC_STRING + "."
                        + FLASH_FLOOD_WATCH_PHEN_SIG));
                assertTrue(legacy.contains(CON_VTEC_STRING + "."
                        + FLASH_FLOOD_WATCH_PHEN_SIG));
                testSuccess();

                break;

            default:
                testError();
            }
        } catch (Exception e) {
            handleException(e);
        }

    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}

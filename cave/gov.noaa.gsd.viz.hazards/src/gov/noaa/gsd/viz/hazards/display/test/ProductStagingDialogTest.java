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

import static gov.noaa.gsd.viz.hazards.display.test.AutoTestUtilities.*;
import gov.noaa.gsd.viz.hazards.display.HazardServicesAppBuilder;
import gov.noaa.gsd.viz.hazards.display.ProductStagingInfo;
import gov.noaa.gsd.viz.hazards.display.ProductStagingInfo.Product;
import gov.noaa.gsd.viz.hazards.display.action.HazardDetailAction;
import gov.noaa.gsd.viz.hazards.display.action.SpatialDisplayAction;

import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.google.common.eventbus.Subscribe;
import com.raytheon.uf.common.dataplugin.events.EventSet;
import com.raytheon.uf.common.dataplugin.events.IEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.hazards.productgen.IGeneratedProduct;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Choice;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Field;
import com.raytheon.uf.viz.hazards.sessionmanager.product.ProductGenerated;

/**
 * Description: {@link FunctionalTest} of the product staging dialog.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 15, 2013    2182       daniel.s.schaffer@noaa.gov      Initial creation
 * 
 * </pre>
 * 
 * @author daniel.s.schaffer@noaa.gov
 * @version 1.0
 */
public class ProductStagingDialogTest extends FunctionalTest {

    private static final double FIRST_EVENT_CENTER_Y = 41.0;

    private static final double FIRST_EVENT_CENTER_X = -96.0;

    private enum Steps {
        START, EVENT0, EVENT1
    }

    private Steps step;

    public ProductStagingDialogTest(HazardServicesAppBuilder appBuilder) {
        super(appBuilder);
    }

    @Override
    protected void run() {
        try {
            super.run();
            step = Steps.START;
            autoTestUtilities.createEvent(FIRST_EVENT_CENTER_X, 41.0);
        } catch (Exception e) {
            handleException(e);
        }

    }

    @Subscribe
    public void spatialDisplayActionOccurred(
            final SpatialDisplayAction spatialDisplayAction) {

        try {
            switch (step) {
            case START:
                autoTestUtilities
                        .assignEventType(AutoTestUtilities.AREAL_FLOOD_WATCH_FULLTYPE);

                break;

            case EVENT0:
                autoTestUtilities
                        .assignEventType(AutoTestUtilities.FLASH_FLOOD_WATCH_FULLTYPE);
                break;

            default:
                testError();

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
            case START:
                step = Steps.EVENT0;
                autoTestUtilities.createEvent(FIRST_EVENT_CENTER_X,
                        FIRST_EVENT_CENTER_Y + 3 * EVENT_BUILDER_OFFSET);
                break;

            case EVENT0:
                step = Steps.EVENT1;
                autoTestUtilities.previewEvent();
                break;

            case EVENT1:
                break;

            default:
                testError();

            }
        } catch (Exception e) {
            handleException(e);
        }

    }

    @Subscribe
    public void handleProductGeneratorResult(ProductGenerated generated) {
        ProductStagingInfo productStagingInfo = mockProductStagingView
                .getProductStagingInfo();
        assertEquals(productStagingInfo.getProducts().size(), 1);
        Product product = productStagingInfo.getProducts().get(0);
        assertEquals(product.getSelectedEventIDs().size(), 1);
        List<Field> fields = product.getFields();
        assertEquals(fields.size(), 1);
        Field field = fields.get(0);
        List<Choice> choices = field.getChoices();
        assertEquals(choices.size(), 2);
        checkChoice(choices.get(0));
        checkChoice(choices.get(1));
        List<IGeneratedProduct> products = generated.getProducts();
        assertEquals(products.size(), 1);
        IGeneratedProduct generatedProduct = products.get(0);
        assertTrue(generatedProduct.getProductID().equals(
                FLOOD_WATCH_PRODUCT_ID));
        EventSet<IEvent> eventSet = generatedProduct.getEventSet();
        assertEquals(eventSet.size(), 1);
        IHazardEvent event = (IHazardEvent) eventSet.iterator().next();
        assertEquals(event.getEventID(), product.getSelectedEventIDs().get(0));
        assertEquals(event.getPhenomenon(), "FF");
        assertEquals(event.getSignificance(), "A");
        assertEquals(event.getState(), HazardConstants.HazardState.PENDING);
        testSuccess();

    }

    private void checkChoice(Choice choice) {
        boolean validType = choice.getDisplayString().contains(
                AREAL_FLOOD_WATCH_PHEN_SIG)
                || choice.getDisplayString().contains(
                        FLASH_FLOOD_WATCH_PHEN_SIG);
        assertTrue(validType);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}

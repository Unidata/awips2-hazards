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

import static gov.noaa.gsd.viz.hazards.display.test.AutoTestUtilities.AREAL_FLOOD_WATCH_PHEN_SIG;
import static gov.noaa.gsd.viz.hazards.display.test.AutoTestUtilities.EVENT_BUILDER_OFFSET;
import static gov.noaa.gsd.viz.hazards.display.test.AutoTestUtilities.FLASH_FLOOD_WATCH_PHEN_SIG;
import static gov.noaa.gsd.viz.hazards.display.test.AutoTestUtilities.FLOOD_WATCH_PRODUCT_ID;
import gov.noaa.gsd.viz.hazards.display.HazardServicesAppBuilder;
import gov.noaa.gsd.viz.hazards.display.ProductStagingInfo;
import gov.noaa.gsd.viz.hazards.display.ProductStagingInfo.Product;
import gov.noaa.gsd.viz.hazards.display.action.ConsoleAction;
import gov.noaa.gsd.viz.hazards.display.action.HazardDetailAction;

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
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventAdded;
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
 * Nov 16, 2013  2166       daniel.s.schaffer@noaa.gov    Using renamed utility
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
        START, EVENT0, EVENT1, PREVIEW
    }

    private Steps step;

    public ProductStagingDialogTest(HazardServicesAppBuilder appBuilder) {
        super(appBuilder);
    }

    @Subscribe
    public void consoleActionOccurred(final ConsoleAction consoleAction) {
        step = Steps.START;
        autoTestUtilities.createEvent(FIRST_EVENT_CENTER_X, 41.0);

    }

    @Subscribe
    public void handleNewHazard(SessionEventAdded action) {

        try {
            switch (step) {
            case START:
                autoTestUtilities
                        .assignSelectedEventType(AutoTestUtilities.AREAL_FLOOD_WATCH_FULLTYPE);

                break;

            case EVENT0:
                step = Steps.EVENT1;
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
    public void hazardDetailActionOccurred(
            final HazardDetailAction hazardDetailAction) {
        try {
            switch (step) {
            case START:
                step = Steps.EVENT0;
                autoTestUtilities.createEvent(FIRST_EVENT_CENTER_X,
                        FIRST_EVENT_CENTER_Y + 3 * EVENT_BUILDER_OFFSET);
                break;

            case EVENT1:
                step = Steps.PREVIEW;
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

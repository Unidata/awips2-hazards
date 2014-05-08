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

import java.util.List;

import net.engio.mbassy.listener.Handler;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.raytheon.uf.common.dataplugin.events.EventSet;
import com.raytheon.uf.common.dataplugin.events.IEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.hazards.productgen.GeneratedProductList;
import com.raytheon.uf.common.hazards.productgen.IGeneratedProduct;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Choice;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Field;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventAdded;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.impl.ObservedHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.product.IProductGenerationComplete;

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
 * Jan 10, 2014  2890      bkowal      Now subscribes to a notification that
 *                                     indicates all product generation is complete.
 * Apr 09, 2014 2925       Chris.Golden Fixed to work with new HID event propagation.
 * </pre>
 * 
 * @author daniel.s.schaffer@noaa.gov
 * @version 1.0
 */
public class ProductStagingDialogTest extends FunctionalTest {

    private final IUFStatusHandler statusHandler = UFStatus
            .getHandler(getClass());

    private static final double FIRST_EVENT_CENTER_Y = 41.0;

    private static final double FIRST_EVENT_CENTER_X = -96.0;

    private enum Steps {
        START, EVENT0, EVENT1, PREVIEW
    }

    private Steps step;

    public ProductStagingDialogTest(HazardServicesAppBuilder appBuilder) {
        super(appBuilder);
    }

    @Override
    protected void runFirstStep() {
        step = Steps.START;
        autoTestUtilities.createEvent(FIRST_EVENT_CENTER_X, 41.0);

    }

    @Override
    protected String getCurrentStep() {
        return step.toString();
    }

    private void stepCompleted() {
        statusHandler.debug("Completed step " + step);
    }

    @Handler(priority = -1)
    public void sessionEventAddedOccurred(SessionEventAdded action) {

        try {
            switch (step) {
            case START:
                autoTestUtilities
                        .assignSelectedEventType(AutoTestUtilities.AREAL_FLOOD_WATCH_FULLTYPE);

                break;

            case EVENT0:
                stepCompleted();
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

    @Handler(priority = -1)
    public void sessionEventModifiedOccurred(SessionEventModified action) {
        try {
            if (step == Steps.START) {
                ObservedHazardEvent event = autoTestUtilities
                        .getSelectedEvent();
                if (!"FA".equals(event.getPhenomenon())
                        || !"A".equals(event.getSignificance())) {
                    return;
                }
                stepCompleted();
                step = Steps.EVENT0;
                autoTestUtilities.createEvent(FIRST_EVENT_CENTER_X,
                        FIRST_EVENT_CENTER_Y + 3 * EVENT_BUILDER_OFFSET);
            } else if (step == Steps.EVENT1) {
                ObservedHazardEvent event = autoTestUtilities
                        .getSelectedEvent();
                if (!"FF".equals(event.getPhenomenon())
                        || !"A".equals(event.getSignificance())) {
                    return;
                }
                stepCompleted();
                step = Steps.PREVIEW;
                autoTestUtilities.previewEvent();
            }

        } catch (Exception e) {
            handleException(e);
        }
    }

    @Handler(priority = -1)
    public void handleProductGeneratorResult(
            final IProductGenerationComplete productGenerationComplete) {
        ProductStagingInfo productStagingInfo = mockProductStagingView
                .getProductStagingInfo();
        assertEquals(productStagingInfo.getProducts().size(), 1);
        Product product = productStagingInfo.getProducts().get(0);
        assertEquals(product.getSelectedEventIDs().size(), 1);
        List<Field> fields = product.getFields();
        Field field = fields.get(0).getFields().get(0);
        List<Choice> choices = field.getChoices();
        assertEquals(choices.size(), 2);
        checkChoice(choices.get(0));
        checkChoice(choices.get(1));
        assertEquals(productGenerationComplete.getGeneratedProducts().size(), 1);
        GeneratedProductList products = productGenerationComplete
                .getGeneratedProducts().get(0);
        assertEquals(products.size(), 1);
        IGeneratedProduct generatedProduct = products.get(0);
        assertTrue(generatedProduct.getProductID().equals(
                FLOOD_WATCH_PRODUCT_ID));
        EventSet<IEvent> eventSet = products.getEventSet();
        assertEquals(eventSet.size(), 1);
        IHazardEvent event = (IHazardEvent) eventSet.iterator().next();
        assertEquals(event.getEventID(), product.getSelectedEventIDs().get(0));
        assertEquals(event.getPhenomenon(), "FF");
        assertEquals(event.getSignificance(), "A");
        assertEquals(event.getStatus(), HazardConstants.HazardStatus.PENDING);
        stepCompleted();
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

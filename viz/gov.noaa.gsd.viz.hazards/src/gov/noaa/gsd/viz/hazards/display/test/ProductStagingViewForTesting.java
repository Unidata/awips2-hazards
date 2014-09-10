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

import gov.noaa.gsd.viz.hazards.productstaging.IProductStagingView;
import gov.noaa.gsd.viz.mvp.widgets.ICommandInvocationHandler;
import gov.noaa.gsd.viz.mvp.widgets.ICommandInvoker;

import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.viz.hazards.sessionmanager.product.ProductStagingInfo;

/**
 * Description: Mock {@link IProductStagingView} used for testing.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 22, 2013  2166      daniel.s.schaffer@noaa.gov      Initial creation
 * Nov 15, 2013  2182       daniel.s.schaffer@noaa.gov    Refactoring JSON - ProductStagingDialog
 * Apr 11, 2014  2819      Chris.Golden      Fixed bugs with the Preview and Issue
 *                                           buttons in the HID remaining grayed out
 *                                           when they should be enabled.
 * May 18, 2014  2925      Chris.Golden      Changed to work with new MVP framework.
 * Jun 30, 2014 3512    Chris.Golden         Changed to work with changes to
 *                                           ICommandInvoker.
 * </pre>
 * 
 * @author daniel.s.schaffer@noaa.gov
 * @version 1.0
 */
@SuppressWarnings("rawtypes")
public class ProductStagingViewForTesting implements IProductStagingView {

    private boolean toBeIssued;

    private ProductStagingInfo productStagingInfo;

    private final ICommandInvoker<String> commandInvoker = new ICommandInvoker<String>() {

        @Override
        public void setEnabled(String identifier, boolean enable) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setCommandInvocationHandler(
                ICommandInvocationHandler<String> handler) {

            /*
             * This is deliberate. It seems weird, but essentially what is being
             * done here is that as soon as the mock dialog is brought up, the
             * presenter's process of binding event handlers to the dialog
             * causes the dialog to immediately fire the invocation handler
             * supplied. This allows the mock product editor to come up, which
             * in turn causes a ProductGenerationComplete notification to go
             * out, which can in turn be caught by the functional test using
             * this mock view.
             */
            handler.commandInvoked(HazardConstants.CONTINUE_BUTTON);
        }
    };

    @Override
    public void dispose() {

    }

    @Override
    public List contributeToMainUI(Enum type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void showProductStagingDetail(boolean toBeIssued,
            ProductStagingInfo productStagingInfo) {
        this.toBeIssued = toBeIssued;
        this.productStagingInfo = productStagingInfo;

    }

    @Override
    public ICommandInvoker getCommandInvoker() {
        return commandInvoker;
    }

    @Override
    public boolean isToBeIssued() {
        return toBeIssued;
    }

    @Override
    public ProductStagingInfo getProductStagingInfo() {
        return productStagingInfo;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}

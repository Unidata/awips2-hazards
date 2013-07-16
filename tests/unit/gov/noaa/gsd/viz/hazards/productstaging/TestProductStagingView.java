/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.productstaging;

import gov.noaa.gsd.viz.hazards.display.RCPMainUserInterfaceElement;
import gov.noaa.gsd.viz.hazards.jsonutilities.Dict;
import gov.noaa.gsd.viz.mvp.widgets.ICommandInvocationHandler;
import gov.noaa.gsd.viz.mvp.widgets.ICommandInvoker;

import java.util.Collections;
import java.util.List;

/**
 * Description: Test implementation of the product staging view.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 01, 2013            Bryon.Lawrence      Initial creation
 * Jul 15, 2013     585    Chris.Golden        Changed to use new version of IView.
 * </pre>
 * 
 * @author Bryon.Lawrence
 * @version 1.0
 */
public class TestProductStagingView implements
        IProductStagingView<Object, RCPMainUserInterfaceElement> {
    /**
     * Flag indicating whether or not a product should be issued.
     */
    private boolean toBeIssued = false;

    /**
     * Product information passed into the product staging dialog.
     */
    Dict productStagingInfo = null;

    /**
     * Continue command invocation handler.
     */
    private ICommandInvocationHandler continueHandler = null;

    /**
     * Continue command invoker.
     */
    private final ICommandInvoker continueInvoker = new ICommandInvoker() {
        @Override
        public void setCommandInvocationHandler(
                ICommandInvocationHandler handler) {
            continueHandler = handler;
        }
    };

    /**
     * @param
     * @return
     */
    @Override
    public void dispose() {
    }

    /**
     * Get any contributions to the main UI that the implementation desires to
     * make. Note that this method may be called multiple times per <code>type
     * </code> to (re)populate the main UI with the specified <code>type</code>;
     * implementations are responsible for cleaning up after contributed items
     * that may exist from a previous call with the same <code>type</code>.
     * 
     * @param type
     *            Type of contribution to be made to the main user interface.
     * @return List of contributions; this may be empty if none are to be made.
     */
    @Override
    public final List<Object> contributeToMainUI(
            RCPMainUserInterfaceElement type) {
        return Collections.emptyList();
    }

    /**
     * @param
     * @return
     */
    @Override
    public void showProductStagingDetail(boolean toBeIssued,
            Dict productStagingInfo) {

        this.toBeIssued = toBeIssued;
        this.productStagingInfo = productStagingInfo;
    }

    /**
     * @param
     * @return
     */
    @Override
    public ICommandInvoker getContinueInvoker() {
        return continueInvoker;
    }

    /**
     * @param
     * @return
     */
    @Override
    public boolean isToBeIssued() {
        return toBeIssued;
    }

    /**
     * @param
     * @return
     */
    @Override
    public Dict getProductInfo() {
        return productStagingInfo;
    }

    /**
     * 
     * @param
     * @return
     */
    public void continueButtonPressed() {
        continueHandler.commandInvoked("Continue");
    }

}

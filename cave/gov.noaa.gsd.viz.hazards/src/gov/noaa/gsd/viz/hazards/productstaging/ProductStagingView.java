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
import gov.noaa.gsd.viz.mvp.widgets.ICommandInvoker;

import org.eclipse.ui.IActionBars;
import org.eclipse.ui.PlatformUI;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

/**
 * Description: Settings view, an implementation of ISettingsView that provides
 * an SWT-based view.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 18, 2013            Bryon.Lawrence      Initial creation
 * 
 * </pre>
 * 
 * @author bryon.lawrence
 * @version 1.0
 */
public class ProductStagingView implements
        IProductStagingView<IActionBars, RCPMainUserInterfaceElement> {

    // Private Static Constants

    /**
     * Logging mechanism.
     */
    @SuppressWarnings("unused")
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(ProductStagingView.class);

    // Private Variables

    /**
     * Product staging dialog.
     */
    private ProductStagingDialog productStagingDialog = null;

    // Public Constructors

    /**
     * Construct a standard instance.
     */
    public ProductStagingView() {

        // No action.
    }

    // Public Methods

    @Override
    public final void dispose() {
        closeProductStagingDialog();
        productStagingDialog = null;
    }

    @Override
    public final boolean contributeToMainUI(IActionBars mainUI,
            RCPMainUserInterfaceElement type) {
        return false;
    }

    @Override
    public void showProductStagingDetail(boolean toBeIssued,
            Dict productStagingInfo) {

        // Close the dialog if it is already open.
        if (productStagingDialog != null) {
            closeProductStagingDialog();
        }

        // Create the dialog from scratch.
        productStagingDialog = new ProductStagingDialog(PlatformUI
                .getWorkbench().getActiveWorkbenchWindow().getShell());
        productStagingDialog.initialize(toBeIssued, productStagingInfo);
        productStagingDialog.setBlockOnOpen(false);
        productStagingDialog.open();
    }

    @Override
    public ICommandInvoker getContinueInvoker() {
        return productStagingDialog.getContinueInvoker();
    }

    @Override
    public boolean isToBeIssued() {
        return productStagingDialog.isToBeIssued();
    }

    @Override
    public Dict getProductInfo() {
        return productStagingDialog.getProductList();
    }

    // Private Methods

    /**
     * Close the product staging dialog.
     */
    private void closeProductStagingDialog() {
        if ((productStagingDialog != null)
                && (productStagingDialog.getShell() != null)
                && (!productStagingDialog.getShell().isDisposed())) {
            productStagingDialog.close();
        }
    }
}

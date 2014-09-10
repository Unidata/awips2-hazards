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
import gov.noaa.gsd.viz.mvp.widgets.ICommandInvoker;

import java.util.Collections;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.PlatformUI;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.hazards.sessionmanager.product.ProductStagingInfo;

/**
 * Description: Settings view, an implementation of ISettingsView that provides
 * an SWT-based view.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 18, 2013            Bryon.Lawrence    Initial creation.
 * Jul 15, 2013     585    Chris.Golden      Changed to support loading from bundle.
 * Nov 15, 2013  2182       daniel.s.schaffer@noaa.gov    Refactoring JSON - ProductStagingDialog
 * Apr 11, 2014   2819     Chris.Golden      Fixed bugs with the Preview and Issue
 *                                           buttons in the HID remaining grayed out
 *                                           when they should be enabled.
 * May 08, 2014   2925     Chris.Golden      Changed to work with MVP framework changes.
 * </pre>
 * 
 * @author bryon.lawrence
 * @version 1.0
 */
public class ProductStagingView implements
        IProductStagingView<Action, RCPMainUserInterfaceElement> {

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
    public final List<? extends Action> contributeToMainUI(
            RCPMainUserInterfaceElement type) {
        return Collections.emptyList();
    }

    @Override
    public void showProductStagingDetail(boolean toBeIssued,
            ProductStagingInfo productStagingInfo) {

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
    public ICommandInvoker<String> getCommandInvoker() {
        return productStagingDialog.getCommandInvoker();
    }

    @Override
    public boolean isToBeIssued() {
        return productStagingDialog.isToBeIssued();
    }

    @Override
    public ProductStagingInfo getProductStagingInfo() {
        return productStagingDialog.getProductStagingInfo();
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

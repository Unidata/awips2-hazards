/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.producteditor;

import gov.noaa.gsd.viz.hazards.display.RCPMainUserInterfaceElement;
import gov.noaa.gsd.viz.hazards.jsonutilities.Dict;
import gov.noaa.gsd.viz.mvp.widgets.ICommandInvoker;

import java.util.List;

import org.eclipse.ui.IActionBars;
import org.eclipse.ui.PlatformUI;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

/**
 * Product editor view, an implementation of IProductEditorView that provides an
 * SWT-based view which shows product information.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan, 2013               Bryon.Lawrence Initial induction into repo
 * Feb 19, 2013            Bryon.Lawrence Converted MVP architecture
 * </pre>
 * 
 * @author bryon.lawrence
 * @version 1.0
 */
public final class ProductEditorView implements
        IProductEditorView<IActionBars, RCPMainUserInterfaceElement> {

    // Private Static Constants

    /**
     * Logging mechanism.
     */
    @SuppressWarnings("unused")
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(ProductEditorView.class);

    // Private Variables

    /**
     * Product editor dialog.
     */
    private ProductEditorDialog productEditorDialog = null;

    // Public Constructors

    /**
     * Construct a standard instance of this view.
     */
    public ProductEditorView() {

        // No action.
    }

    // Public Methods

    @Override
    public void initialize() {

        // No action.
    }

    @Override
    public void dispose() {
        if (productEditorDialog != null) {
            productEditorDialog.close();
            productEditorDialog = null;
        }
    }

    @Override
    public final boolean contributeToMainUI(IActionBars mainUI,
            RCPMainUserInterfaceElement type) {
        return false;
    }

    @Override
    public boolean showProductEditorDetail(String productInfo) {
        closeProductEditorDialog();
        Dict productDict = Dict.getInstance(productInfo);
        List<Dict> generatedProductsDictList = productDict
                .getDynamicallyTypedValue("generatedProducts");
        List<Dict> hazardEventSetsList = productDict
                .getDynamicallyTypedValue("hazardEventSets");

        if (productInfo != "") {
            productEditorDialog = new ProductEditorDialog(PlatformUI
                    .getWorkbench().getActiveWorkbenchWindow().getShell(),
                    generatedProductsDictList, hazardEventSetsList);
            productEditorDialog.open();
            return true;
        }

        return false;
    }

    @Override
    public void closeProductEditorDialog() {

        if ((productEditorDialog != null)
                && (productEditorDialog.getShell() != null)
                && (!productEditorDialog.getShell().isDisposed())) {
            productEditorDialog.close();
        }

        productEditorDialog = null;
    }

    @Override
    public List<Dict> getGeneratedProductsDictList() {
        return productEditorDialog.getGeneratedProductsDictList();
    }

    @Override
    public List<Dict> getHazardEventSetsList() {
        return productEditorDialog.getHazardEventSetsList();
    }

    @Override
    public ICommandInvoker getIssueInvoker() {
        return productEditorDialog.getIssueInvoker();
    }

    @Override
    public ICommandInvoker getDismissInvoker() {
        return productEditorDialog.getDismissInvoker();
    }

    @Override
    public ICommandInvoker getShellClosedInvoker() {
        return productEditorDialog.getShellClosedInvoker();
    }
}

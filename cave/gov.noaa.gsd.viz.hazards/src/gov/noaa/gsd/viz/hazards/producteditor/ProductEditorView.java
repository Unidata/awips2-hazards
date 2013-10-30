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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.PlatformUI;

import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.hazards.productgen.GeneratedProduct;
import com.raytheon.uf.common.hazards.productgen.IGeneratedProduct;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.productgen.dialog.ProductGenerationDialog;

/**
 * Product editor view, an implementation of IProductEditorView that provides an
 * SWT-based view which shows product information.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan, 2013               Bryon.Lawrence    Initial induction into repo.
 * Feb 19, 2013            Bryon.Lawrence    Converted MVP architecture.
 * Jul 15, 2013     585    Chris.Golden      Changed to support loading from bundle.
 * Sep 19, 2013     2046    mnash           Update for product generation.
 * </pre>
 * 
 * @author bryon.lawrence
 * @version 1.0
 */
public final class ProductEditorView implements
        IProductEditorView<Action, RCPMainUserInterfaceElement> {

    // Private Static Constants

    private List<Dict> hazardEventSets;

    private List<Dict> generatedProducts;

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
    private ProductGenerationDialog productGenerationDialog = null;

    // private ProductEditorDialog productEditorDialog = null;

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
        if (productGenerationDialog != null) {
            productGenerationDialog.close();
            productGenerationDialog = null;
        }
    }

    @Override
    public final List<? extends Action> contributeToMainUI(
            RCPMainUserInterfaceElement type) {
        return Collections.emptyList();
    }

    @Override
    public boolean showProductEditorDetail(String productInfo) {
        Dict productDict = Dict.getInstance(productInfo);
        generatedProducts = productDict
                .getDynamicallyTypedValue(HazardConstants.GENERATED_PRODUCTS);
        hazardEventSets = productDict
                .getDynamicallyTypedValue(HazardConstants.HAZARD_EVENT_SETS);

        if ("".equals(productInfo) == false) {
            productGenerationDialog = new ProductGenerationDialog(PlatformUI
                    .getWorkbench().getActiveWorkbenchWindow().getShell());
            // FIXME TODO XXX this is a temporary change, will be OBE by JSON
            // removal.
            List<IGeneratedProduct> products = new ArrayList<IGeneratedProduct>();
            if (generatedProducts != null) {
                for (Dict d : generatedProducts) {
                    Dict val = (Dict) d.get("products");
                    GeneratedProduct product = new GeneratedProduct(
                            (String) d.get("productID"));
                    for (String format : val.keySet()) {
                        List<Object> text = new ArrayList<Object>();
                        text.add(val.get(format));
                        product.addEntry(format, text);
                    }
                    products.add(product);
                }
            }
            productGenerationDialog.setProducts(products);
            return true;
        }

        return false;
    }

    @Override
    public void closeProductEditorDialog() {

        if ((productGenerationDialog != null)
                && (productGenerationDialog.getShell() != null)
                && (!productGenerationDialog.getShell().isDisposed())) {
            productGenerationDialog.close();
        }

        productGenerationDialog = null;
    }

    @Override
    public List<Dict> getGeneratedProductsDictList() {
        return generatedProducts;
    }

    @Override
    public List<Dict> getHazardEventSetsList() {
        return hazardEventSets;
    }

    @Override
    public ICommandInvoker getIssueInvoker() {
        return productGenerationDialog.getIssueInvoker();
    }

    @Override
    public ICommandInvoker getDismissInvoker() {
        return productGenerationDialog.getDismissInvoker();
    }

    @Override
    public ICommandInvoker getShellClosedInvoker() {
        return productGenerationDialog.getShellClosedInvoker();
    }

    @Override
    public void openDialog() {
        productGenerationDialog.open();
    }
}

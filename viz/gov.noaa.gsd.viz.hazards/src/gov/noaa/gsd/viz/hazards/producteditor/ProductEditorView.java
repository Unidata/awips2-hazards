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
import gov.noaa.gsd.viz.mvp.widgets.ICommandInvoker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.action.Action;

import com.raytheon.uf.common.hazards.productgen.GeneratedProductList;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.core.VizApp;
import com.raytheon.uf.viz.productgen.dialog.ProductEditor;
import com.raytheon.viz.ui.VizWorkbenchManager;

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
 * Feb 07, 2014  2890      bkowal       Product Generation JSON refactor.
 * Feb 18, 2014  2702      jsanchez     Cleaned up code as part of the refactor.
 * Apr 11, 2014  2819      Chris.Golden      Fixed bugs with the Preview and Issue
 *                                           buttons in the HID remaining grayed out
 *                                           when they should be enabled.
 * May 08, 2014  2925      Chris.Golden Changed to work with MVP framework changes.
 * Jun 18, 2014  3519      jsanchez         Replaced ProductGenerationDialog with ProductEditor.
 * Jul 09, 2014  3214      jsanchez     Listens if the selected events have changed.
 * Feb 26, 2015  6306      mduff        Pass site id to product editor.
 * May 13, 2015  6899      Robert.Blum  Removed notifySessionEventsModified().
 * </pre>
 * 
 * @author bryon.lawrence
 * @version 1.0
 */
public final class ProductEditorView implements
        IProductEditorView<Action, RCPMainUserInterfaceElement> {

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
    private ProductEditor productEditor;

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
        if (productEditor != null) {
            productEditor.close();
            productEditor = null;
        }
    }

    @Override
    public final List<? extends Action> contributeToMainUI(
            RCPMainUserInterfaceElement type) {
        return Collections.emptyList();
    }

    @Override
    public boolean showProductEditorDetail(
            final List<GeneratedProductList> generatedProductsList,
            final String siteId) {
        VizApp.runSync(new Runnable() {
            @Override
            public void run() {

                productEditor = new ProductEditor(VizWorkbenchManager
                        .getInstance().getCurrentWindow().getShell(),
                        generatedProductsList, siteId);
            }
        });
        return true;
    }

    @Override
    public void closeProductEditorDialog() {

        if ((productEditor != null) && (productEditor.getShell() != null)
                && (!productEditor.getShell().isDisposed())) {
            productEditor.close();
        }

        productEditor = null;
    }

    @Override
    public List<GeneratedProductList> getGeneratedProductsList() {
        if (productEditor != null) {
            return productEditor.getGeneratedProductListStorage();
        }
        return new ArrayList<GeneratedProductList>();
    }

    @Override
    public ICommandInvoker<String> getIssueInvoker() {
        return productEditor.getIssueInvoker();
    }

    @Override
    public ICommandInvoker<String> getDismissInvoker() {
        return productEditor.getDismissInvoker();
    }

    @Override
    public void openDialog() {
        VizApp.runSync(new Runnable() {
            @Override
            public void run() {
                productEditor.open();
            }
        });

    }
}
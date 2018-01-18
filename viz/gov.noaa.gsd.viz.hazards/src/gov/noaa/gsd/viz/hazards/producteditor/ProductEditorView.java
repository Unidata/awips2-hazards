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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.action.IAction;

import com.raytheon.uf.common.hazards.configuration.types.HazardTypes;
import com.raytheon.uf.common.hazards.productgen.GeneratedProductList;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.core.VizApp;
import com.raytheon.uf.viz.productgen.dialog.ProductEditor;
import com.raytheon.viz.ui.VizWorkbenchManager;

import gov.noaa.gsd.viz.hazards.display.RcpMainUiElement;
import gov.noaa.gsd.viz.mvp.widgets.ICommandInvoker;

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
 * Jul 15, 2013     585    Chris.Golden Changed to support loading from bundle.
 * Sep 19, 2013     2046    mnash       Update for product generation.
 * Feb 07, 2014  2890      bkowal       Product Generation JSON refactor.
 * Feb 18, 2014  2702      jsanchez     Cleaned up code as part of the refactor.
 * Apr 11, 2014  2819      Chris.Golden Fixed bugs with the Preview and Issue
 *                                      buttons in the HID remaining grayed out
 *                                      when they should be enabled.
 * May 08, 2014  2925      Chris.Golden Changed to work with MVP framework changes.
 * Jun 18, 2014  3519      jsanchez     Replaced ProductGenerationDialog with ProductEditor.
 * Jul 09, 2014  3214      jsanchez     Listens if the selected events have changed.
 * Feb 26, 2015  6306      mduff        Pass site id to product editor.
 * May 13, 2015  6899      Robert.Blum  Removed notifySessionEventsModified().
 * Dec 04, 2015 12981      Roger.Ferrel Checks to prevent issuing unwanted
 *                                      expiration product.
 * Mar 30, 2016  8837      Robert.Blum  Added changeSite() for service backup.
 * Dec 12, 2016 21504      Robert.Blum  Updates for disabling IssueAll and Save 
 *                                      when events become locked on the Product Editor.
 * Apr 05, 2017 32733      Robert.Blum  Removed unused parameter.
 * Apr 27, 2017 11853      Chris.Golden Made names of methods more consistent, and
 *                                      added a method to check to see if the
 *                                      product editor is open. Also made thread usage
 *                                      more consistent.
 * Dec 17, 2017 20739      Chris.Golden Refactored away access to directly mutable
 *                                      session events.
 * Jan 17, 2018 33428      Chris.Golden Changed to work with new, more flexible
 *                                      toolbar contribution code.
 * </pre>
 * 
 * @author bryon.lawrence
 * @version 1.0
 */
public final class ProductEditorView
        implements IProductEditorView<String, IAction, RcpMainUiElement> {

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

        /*
         * TODO: Move code to ensure correct thread is used to presenter.
         */
        VizApp.runSync(new Runnable() {
            @Override
            public void run() {

                if (productEditor != null) {
                    productEditor.close();
                    productEditor = null;
                }
            }
        });
    }

    @Override
    public final Map<? extends String, List<? extends IAction>> contributeToMainUi(
            RcpMainUiElement type) {
        return Collections.emptyMap();
    }

    @Override
    public boolean isProductEditorOpen() {
        return (productEditor != null ? productEditor.isOpen() : false);
    }

    @Override
    public boolean showProductEditor(
            final List<GeneratedProductList> generatedProductsList,
            final String siteId, final HazardTypes hazardTypes) {

        /*
         * TODO: Move code to ensure correct thread is used to presenter.
         */
        VizApp.runSync(new Runnable() {
            @Override
            public void run() {
                productEditor = new ProductEditor(
                        VizWorkbenchManager.getInstance().getCurrentWindow()
                                .getShell(),
                        generatedProductsList, siteId, hazardTypes);
            }
        });
        return true;
    }

    @Override
    public void closeProductEditor() {

        /*
         * TODO: Move code to ensure correct thread is used to presenter.
         */
        VizApp.runSync(new Runnable() {
            @Override
            public void run() {
                if ((productEditor != null)
                        && (productEditor.getShell() != null)
                        && (!productEditor.getShell().isDisposed())) {
                    productEditor.close();
                }
                productEditor = null;
            }
        });
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

        /*
         * TODO: Move code to ensure correct thread is used to presenter.
         */
        VizApp.runSync(new Runnable() {
            @Override
            public void run() {
                productEditor.open();
            }
        });

    }

    @Override
    public void changeSite(String site) {
        if (productEditor != null) {
            productEditor.changeSite(site);
        }
    }

    @Override
    public void handleHazardEventLock() {
        productEditor.setHazardEventLocked(true);
        productEditor.updateButtons();
        productEditor.disableSaveButtons();
    }
}
/**
 * This software was developed and / or modified by Raytheon Company,
 * pursuant to Contract DG133W-05-CQ-1067 with the US Government.
 * 
 * U.S. EXPORT CONTROLLED TECHNICAL DATA
 * This software product contains export-restricted data whose
 * export/transfer/disclosure is restricted by U.S. law. Dissemination
 * to non-U.S. persons whether in the United States or abroad requires
 * an export license or other authorization.
 * 
 * Contractor Name:        Raytheon Company
 * Contractor Address:     6825 Pine Street, Suite 340
 *                         Mail Stop B8
 *                         Omaha, NE 68106
 *                         402.291.0100
 * 
 * See the AWIPS II Master Rights File ("Master Rights File.pdf") for
 * further licensing information.
 **/
package gov.noaa.gsd.viz.hazards.product;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jface.action.IAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

import com.google.common.collect.ImmutableList;
import com.raytheon.uf.common.hazards.productgen.data.ProductData;
import com.raytheon.uf.common.hazards.productgen.data.ProductDataUtil;
import com.raytheon.uf.common.time.SimulatedTime;
import com.raytheon.uf.viz.core.VizApp;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Tool;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.ToolType;
import com.raytheon.viz.core.mode.CAVEMode;
import com.raytheon.viz.ui.VizWorkbenchManager;

import gov.noaa.gsd.viz.hazards.display.AbstractProductSelectionDlg;
import gov.noaa.gsd.viz.hazards.display.ProductCorrectionSelectionDlg;
import gov.noaa.gsd.viz.hazards.display.ProductViewerSelectionDlg;
import gov.noaa.gsd.viz.hazards.display.RcpMainUiElement;
import gov.noaa.gsd.viz.hazards.display.action.ToolAction;
import gov.noaa.gsd.viz.hazards.toolbar.PulldownAction;

/**
 * Product view, an implementation of IToolsView that provides an SWT-based view
 * for the product toolbar button.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Apr 29, 2016   16373    mpduff       Initial creation
 * Jul 20, 2016   19443    mpduff       Renamed class and variable.
 * Jun 26, 2017   19207    Chris.Golden Added note about bringing this into
 *                                      line with MVP design at some future point.
 * Jan 17, 2018   33428    Chris.Golden Changed to work with new, more flexible
 *                                      toolbar contribution code.
 * Jan 27, 2017   22308    Robert.Blum  Updates for pulling products from text
 *                                      database.
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class ProductView
        implements IProductView<String, IAction, RcpMainUiElement> {

    // Private Static Constants

    /**
     * Name of the file holding the image for the product toolbar button icon.
     */
    private static final String PRODUCTS_TOOLBAR_IMAGE_FILE_NAME = "product.gif";

    /**
     * Product toolbar button tooltip text.
     */
    private static final String PRODUCTS_TOOLBAR_BUTTON_TOOLTIP_TEXT = "Products";

    // Private Classes

    /**
     * Tools pulldown menu action.
     */
    private class ProductsPulldownAction extends PulldownAction {

        /**
         * Flag indicating whether or not the menu should be repopulated with
         * product names.
         */
        private boolean productsChanged = true;

        /**
         * Listener for menu item invocations.
         */
        private final SelectionListener listener = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                ToolType type = ((Tool) event.widget.getData()).getToolType();
                if (type == ToolType.NON_HAZARD_PRODUCT_GENERATOR) {
                    presenter.publish(new ToolAction(
                            ToolAction.RecommenderActionEnum.RUN_RECOMMENDER,
                            ((Tool) event.widget.getData()).getToolName(),
                            ((Tool) event.widget.getData()).getToolType()));
                } else if ((type == ToolType.PRODUCT_CORRECTOR)
                        || (type == ToolType.PRODUCT_VIEWER)) {

                    /*
                     * TODO: When bringing the product package into line with
                     * the MVP design, this code should of course move out of
                     * the view; there is knowledge required of the model here.
                     * The presenter should call
                     * ISessionProductManager.showUserProductViewerSelection()
                     * to accomplish what's here, and the closing of the product
                     * viewer selection dialog if the wrong info is displayed
                     * should be transferred to HazardServicesAppBuilder's
                     * implementation of IProductViewerChooser.
                     */

                    /*
                     * If correcting and previously viewing, or vice versa,
                     * close the product correction or viewer selection dialog,
                     * as it is open in the wrong mode for what's needed now.
                     */
                    boolean lastCorrection = correction;
                    correction = (type == ToolType.PRODUCT_CORRECTOR);
                    if ((correction != lastCorrection)
                            && (selectionDialog != null)
                            && (selectionDialog.isDisposed() == false)) {
                        selectionDialog.close();
                    }

                    /*
                     * Get the appropriate product data.
                     */
                    String mode = CAVEMode.getMode().toString();
                    Date time = SimulatedTime.getSystemTime().getTime();
                    List<ProductData> productData = (correction
                            ? ProductDataUtil
                                    .retrieveCorrectableProductData(mode, time)
                            : ProductDataUtil.retrieveViewableProductData(mode,
                                    time));

                    /*
                     * Create the appropriate dialog and open it if it is not
                     * already showing, or just bring it to the top if it is
                     * showing.
                     */
                    Shell shell = VizWorkbenchManager.getInstance()
                            .getCurrentWindow().getShell();
                    if ((selectionDialog == null)
                            || selectionDialog.isDisposed()) {
                        selectionDialog = (correction
                                ? new ProductCorrectionSelectionDlg(shell,
                                        presenter, productData)
                                : new ProductViewerSelectionDlg(shell,
                                        presenter,
                                        presenter.getSessionManager()
                                                .getEventManager().getEvents(),
                                        false));
                        VizApp.runSync(new Runnable() {
                            @Override
                            public void run() {
                                selectionDialog.open();
                            }
                        });
                    } else {
                        selectionDialog.bringToTop();
                    }
                }
            }
        };

        // Public Constructors

        /**
         * Construct a standard instance.
         */
        public ProductsPulldownAction() {
            super("");
            setImageDescriptor(getImageDescriptorForFile(
                    PRODUCTS_TOOLBAR_IMAGE_FILE_NAME));
            setToolTipText(PRODUCTS_TOOLBAR_BUTTON_TOOLTIP_TEXT);
            productsChanged();
        }

        /**
         * Receive notification that the tools have changed.
         */
        public void productsChanged() {
            productsChanged = true;
            setEnabled(true);
            setEnabled(productIdentifiersForNames.isEmpty() == false);
        }

        // Protected Methods

        @Override
        public Menu doGetMenu(Control parent, Menu menu) {

            /*
             * If the tools have changed, recreate the menu's contents.
             */
            if (productsChanged) {

                /*
                 * If the menu has not yet been created, do so now; otherwise,
                 * delete its contents.
                 */
                if (menu == null) {
                    menu = new Menu(parent);
                } else {
                    for (MenuItem item : menu.getItems()) {
                        item.dispose();
                    }
                }

                for (Entry<String, Tool> entry : productIdentifiersForNames
                        .entrySet()) {
                    ToolType toolType = entry.getValue().getToolType();
                    if (toolType == ToolType.NON_HAZARD_PRODUCT_GENERATOR) {
                        MenuItem rvsMI = new MenuItem(menu, SWT.PUSH);
                        rvsMI.setText("Generate RVS");
                        rvsMI.setData(entry.getValue());
                        rvsMI.addSelectionListener(listener);

                    } else if (toolType == ToolType.PRODUCT_CORRECTOR) {
                        MenuItem correctionMI = new MenuItem(menu, SWT.PUSH);
                        correctionMI.setText("Correct Product(s)");
                        correctionMI.setData(entry.getValue());
                        correctionMI.addSelectionListener(listener);
                    } else if (toolType == ToolType.PRODUCT_VIEWER) {
                        MenuItem viewMI = new MenuItem(menu, SWT.PUSH);
                        viewMI.setText("View Product(s)");
                        viewMI.setData(entry.getValue());
                        viewMI.addSelectionListener(listener);
                    }
                    /*
                     * Reset the tools changed flag.
                     */
                    productsChanged = false;
                }
            }

            return menu;
        }
    }

    /**
     * Map of tool names to their associated identifiers.
     */
    private final Map<String, Tool> productIdentifiersForNames = new LinkedHashMap<>();

    /**
     * Presenter.
     */
    private ProductPresenter presenter = null;

    private ProductsPulldownAction productPullDownAction;

    private AbstractProductSelectionDlg selectionDialog;

    private boolean correction;

    @Override
    public void dispose() {
        // No-op

    }

    @Override
    public Map<? extends String, List<? extends IAction>> contributeToMainUi(
            RcpMainUiElement type) {
        if (type == RcpMainUiElement.TOOLBAR) {
            productPullDownAction = new ProductsPulldownAction();

            Map<String, List<? extends IAction>> map = new HashMap<>(1, 1.0f);
            map.put(PRODUCT_PULL_DOWN_IDENTIFIER,
                    ImmutableList.of(productPullDownAction));
            return map;
        }
        return Collections.emptyMap();
    }

    @Override
    public void showToolParameterGatherer(Tool tool, String eventType,
            Map<String, Serializable> dialogInput,
            Map<String, Serializable> initialInput) {
        // NO-OP
    }

    @Override
    public void setTools(List<Tool> products) {
        /*
         * Get the names and identifiers of the tools.
         */
        productIdentifiersForNames.clear();
        for (Tool tool : products) {
            productIdentifiersForNames.put(tool.getDisplayName(), tool);
        }

        /*
         * Notify the pulldown that the tools have changed.
         */
        if (productPullDownAction != null) {
            productPullDownAction.productsChanged();
        }
    }

    @Override
    public void initialize(ProductPresenter presenter, List<Tool> products) {
        this.presenter = presenter;
        setTools(products);
    }

}

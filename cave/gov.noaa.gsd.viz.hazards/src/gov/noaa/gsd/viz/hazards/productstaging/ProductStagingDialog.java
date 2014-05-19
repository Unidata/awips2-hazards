/**
 * This software was developed and / or modified by the National Oceanic and
 * Atmospheric Administration (NOAA), Earth System Research Laboratory (ESRL),
 * Global Systems Division (GSD), Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.productstaging;

import gov.noaa.gsd.viz.hazards.display.ProductStagingInfo;
import gov.noaa.gsd.viz.hazards.display.ProductStagingInfo.Product;
import gov.noaa.gsd.viz.hazards.jsonutilities.Dict;
import gov.noaa.gsd.viz.hazards.ui.BasicDialog;
import gov.noaa.gsd.viz.megawidgets.MegawidgetException;
import gov.noaa.gsd.viz.megawidgets.MegawidgetManager;
import gov.noaa.gsd.viz.mvp.widgets.ICommandInvocationHandler;
import gov.noaa.gsd.viz.mvp.widgets.ICommandInvoker;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.viz.ui.dialogs.ModeListener;

/**
 * This Product Staging Dialog handles multiple hazards per product. This is a
 * part of Hazard Life Cycle.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 10, 2012            shouming.wei      Initial creation
 * Feb     2013            Bryon L.    Refactored for use in MVP
 *                                     architecture.  Added JavaDoc.
 *                                     Cleaned up logic.
 * Feb     2013            Bryon L.    Added ability to use megawidgets for
 *                                     user-defined content.
 * Feb-Mar 2013            Chris G.    More refactoring for MVP reasons,
 *                                     general cleanup.
 * Jun 04, 2013            Chris G.    Added support for changing background
 *                                     and foreground colors in order to stay
 *                                     in synch with CAVE mode.
 * Jul 18, 2013    585     Chris G.    Changed to support loading from bundle.
 * Nov 15, 2013   2182     daniel.s.schaffer Refactoring JSON - ProductStagingDialog
 * Dec 16, 2013   2545     Chris G.    Added current time provider for
 *                                     megawidget use.
 * Apr 11, 2014   2819     Chris G.    Fixed bugs with the Preview and Issue
 *                                     buttons in the HID remaining grayed out
 *                                     when they should be enabled.
 * Apr 14, 2014   2925     Chris G.    Minor changes to support megawidget framework
 *                                     changes.
 * May 08, 2014   2925     Chris G.    Changed to work with MVP framework changes.
 * </pre>
 * 
 * @author shouming.wei
 * @version 1.0
 */
class ProductStagingDialog extends BasicDialog {

    // Private Static Constants

    /**
     * Dialog title.
     */
    private static final String DIALOG_TITLE = "Product Staging";

    /**
     * OK button text.
     */
    private static final String OK_BUTTON_TEXT = "Continue...";

    /**
     * Logging mechanism.
     */
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(ProductStagingDialog.class);

    // Private Variables

    /**
     * Product staging information.
     */
    private ProductStagingInfo productStagingInfo;

    /**
     * Flag indicating whether or not the product is to be issued.
     */
    private boolean toBeIssued = false;

    /**
     * Megawidget manager for staging info megawidgets.
     */
    @SuppressWarnings("unused")
    private MegawidgetManager stagingMegawidgetManager = null;

    /**
     * Megawidget manager for dialog info megawidgets.
     */
    @SuppressWarnings("unused")
    private final MegawidgetManager dialogMegawidgetManager = null;

    /**
     * Continue command invocation handler.
     */
    private ICommandInvocationHandler<String> commandHandler = null;

    /**
     * Command invoker.
     */
    private final ICommandInvoker<String> commandInvoker = new ICommandInvoker<String>() {

        @Override
        public void setEnabled(String identifier, boolean enable) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setCommandInvocationHandler(String identifier,
                ICommandInvocationHandler<String> handler) {
            commandHandler = handler;
        }
    };

    // Private Classes

    /**
     * Megawidget manager for this dialog.
     */
    private class DialogMegawidgetManager extends MegawidgetManager {

        // Public Constructors

        private final Product stagingProduct;

        /**
         * Construct a standard instance.
         * 
         * @param parent
         *            Parent composite in which the megawidgets are to be
         *            created.
         * @param specifiers
         *            List of dictionaries, each of the latter holding the
         *            parameters of a megawidget specifier. Each megawidget
         *            specifier must have an identifier that is unique within
         *            this list.
         * @param state
         *            State to be viewed and/or modified via the megawidgets
         *            that are constructed. Each megawidget specifier defined by
         *            <code>specifiers</code> should have an entry in this
         *            dictionary, mapping the specifier's identifier to the
         *            value that the megawidget will take on.
         * @param stagingProduct
         * @throws MegawidgetException
         *             If one of the megawidget specifiers is invalid, or if an
         *             error occurs while creating or initializing one of the
         *             megawidgets.
         */
        public DialogMegawidgetManager(Composite parent, List<Dict> specifiers,
                Dict state, Product stagingProduct) throws MegawidgetException {
            super(parent, specifiers, state, 0L, 0L, null);
            this.stagingProduct = stagingProduct;
        }

        // Protected Methods

        @Override
        protected final void commandInvoked(String identifier,
                String extraCallback) {

            /*
             * No action.
             */
        }

        @Override
        protected final void stateElementChanged(String identifier, Object state) {
            if (identifier.equals(HazardConstants.HAZARD_EVENT_IDS)) {
                @SuppressWarnings("unchecked")
                List<String> selectedEventIDs = (ArrayList<String>) state;
                stagingProduct.setSelectedEventIDs(selectedEventIDs);
            } else {
                Map<String, Serializable> dialogSelections = stagingProduct
                        .getDialogSelections();
                dialogSelections.put(identifier, (String) state);
            }
        }
    }

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param parent
     *            Parent shell for this dialog.
     */
    public ProductStagingDialog(Shell parent) {
        super(parent);
        setShellStyle(SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        setBlockOnOpen(false);
    }

    // Public Methods

    /**
     * Initialize the content.
     * 
     * @param isToBeIssued
     *            Flag indicating whether or not the product staging dialog is
     *            being called as the result of a product issue action.
     * @param productStagingInfo
     *            Potential products for staging
     */
    public void initialize(boolean isToBeIssued,
            ProductStagingInfo productStagingInfo) {
        this.toBeIssued = isToBeIssued;
        this.productStagingInfo = productStagingInfo;
    }

    /**
     * Get the command invoker.
     * 
     * @return Command invoker.
     */
    public ICommandInvoker<String> getCommandInvoker() {
        return commandInvoker;
    }

    /**
     * Determine whether or not the product staging dialog is or was being
     * displayed as a result of an issue action.
     * 
     * @return True if the product staging dialog is or was being displayed as
     *         the result of an issue action, false otherwise.
     */
    public boolean isToBeIssued() {
        return toBeIssued;
    }

    public ProductStagingInfo getProductStagingInfo() {
        return productStagingInfo;
    }

    // Protected Methods

    @Override
    protected int getDialogBoundsStrategy() {
        return DIALOG_PERSISTLOCATION;
    }

    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText(DIALOG_TITLE);
    }

    @Override
    protected void handleShellCloseEvent() {
        buttonPressed(IDialogConstants.CANCEL_ID);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite top = (Composite) super.createDialogArea(parent);
        top.setLayout(new FillLayout());
        CTabFolder tabFolder = new CTabFolder(top, SWT.TOP);
        tabFolder.setBorderVisible(true);
        new ModeListener(tabFolder);
        List<ProductStagingInfo.Product> products = productStagingInfo
                .getProducts();
        for (ProductStagingInfo.Product product : products) {

            if (product.getFields().size() > 0
                    || product.getDialogSelections().size() > 0) {
                CTabItem tabItem = new CTabItem(tabFolder, SWT.NONE);
                tabItem.setText(product.getProductGenerator());
                Control control = createTabFolderPage(tabFolder, product);
                tabItem.setControl(control);
            }
        }
        return top;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        super.createButtonsForButtonBar(parent);
        Button ok = getButton(IDialogConstants.OK_ID);
        ok.setText(OK_BUTTON_TEXT);
        setButtonLayoutData(ok);
    }

    @Override
    protected void buttonPressed(int buttonId) {
        super.buttonPressed(buttonId);
        if (commandHandler != null) {
            if (buttonId == IDialogConstants.OK_ID) {
                commandHandler.commandInvoked(HazardConstants.CONTINUE_BUTTON);
            } else if (buttonId == IDialogConstants.CANCEL_ID) {
                commandHandler.commandInvoked(HazardConstants.CANCEL_BUTTON);
            }
        }
    }

    // Private Methods

    /**
     * Creates the contents of one product tab.
     * 
     * @param tabFolder
     *            The tabFolder to populate.
     * @param product
     *            Product for this tab page.
     * @return Created tab folder page.
     */
    private Control createTabFolderPage(CTabFolder tabFolder, Product product) {
        ScrolledComposite scrolledComposite = new ScrolledComposite(tabFolder,
                SWT.V_SCROLL);
        Composite tabFolderPage = new Composite(scrolledComposite, SWT.NONE);

        /*
         * Create the layout for the main panel.
         */
        GridLayout tabLayout = new GridLayout(1, true);
        tabLayout.marginRight = 5;
        tabLayout.marginLeft = 5;
        tabLayout.marginBottom = 5;
        tabLayout.marginHeight = 3;
        tabLayout.marginWidth = 3;
        tabFolderPage.setLayout(tabLayout);

        stagingMegawidgetManager = buildMegawidgets(tabFolderPage, product);

        /*
         * Configure the scrolled composite.
         */
        scrolledComposite.setContent(tabFolderPage);
        scrolledComposite.setExpandHorizontal(true);
        scrolledComposite.setShowFocusedControl(true);
        tabFolderPage.pack();
        scrolledComposite.setMinHeight(tabFolderPage.computeSize(SWT.DEFAULT,
                SWT.DEFAULT).y);
        return scrolledComposite;
    }

    /**
     * Constructs one or more megawidgets based on the contents of the supplied
     * dictionary. These megawidgets will be constructed in the supplied panel.
     * 
     * @param panel
     *            The container (parent) for the constructed megawidgets
     * @param product
     *            product in the {@link ProductStagingInfo}
     * @return Megawidget manager that was created.
     */
    private MegawidgetManager buildMegawidgets(Composite panel, Product product) {

        /*
         * Get the dictionary holding the values for the megawidgets.
         */
        Dict values = new Dict();
        values.put(HazardConstants.HAZARD_EVENT_IDS,
                product.getSelectedEventIDs());

        /*
         * Create the megawidget manager, which will in turn create the
         * megawidgets and bind them to the values dictionary, and return it.
         */
        List<Dict> specifiersList = product.fieldsAsDicts();

        try {
            return new DialogMegawidgetManager(panel, specifiersList, values,
                    product);
        } catch (MegawidgetException e) {
            statusHandler
                    .error("ProductStagingDialog.buildMegawidgets(): Unable to create "
                            + "megawidget manager due to megawidget construction problem.",
                            e);
            return null;
        }
    }
}

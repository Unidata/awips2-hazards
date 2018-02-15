/**
 * This software was developed and / or modified by the National Oceanic and
 * Atmospheric Administration (NOAA), Earth System Research Laboratory (ESRL),
 * Global Systems Division (GSD), Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.productstaging;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;

import gov.noaa.gsd.viz.hazards.productstaging.ProductStagingPresenter.Command;
import gov.noaa.gsd.viz.hazards.ui.BasicDialog;
import gov.noaa.gsd.viz.megawidgets.IMegawidgetManagerListener;
import gov.noaa.gsd.viz.megawidgets.MegawidgetException;
import gov.noaa.gsd.viz.megawidgets.MegawidgetManager;
import gov.noaa.gsd.viz.megawidgets.MegawidgetPropertyException;
import gov.noaa.gsd.viz.megawidgets.MegawidgetSpecifierManager;
import gov.noaa.gsd.viz.mvp.widgets.ICommandInvocationHandler;
import gov.noaa.gsd.viz.mvp.widgets.ICommandInvoker;
import gov.noaa.gsd.viz.mvp.widgets.IQualifiedStateChangeHandler;
import gov.noaa.gsd.viz.mvp.widgets.IQualifiedStateChanger;

/**
 * Description: Product staging dialog, used to stage products that are to be
 * created via preview or issuance of hazard events. This involves displaying
 * megawidgets that are used to show/harvest additional metadata information.
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
 * Jun 23, 2014   4010     Chris G.    Changed to work with megawidget manager
 *                                     changes.
 * Jun 30, 2014   3512     Chris G.    Changed to work with more megawidget manager
 *                                     changes, and with changes to ICommandInvoker.
 * Sep 09, 2014   4042     Chris G.    Changed to work with megawidget specifiers
 *                                     in map form instead of as Field instances.
 * Oct 03, 2014   4042     Chris G.    Completely refactored to work as a two-step
 *                                     dialog, with the first stage allowing the
 *                                     user to choose additional events to go into
 *                                     each of the products (if applicable), and
 *                                     the second step allowing the user to change
 *                                     any product-generator-specific parameters
 *                                     specified for the products (again, if
 *                                     applicable).
 * Oct 20, 2014   4818     Chris G.    Removed scrolled composite from step 2 of
 *                                     the dialog, since scrolling is now handled
 *                                     by the megawidgets.
 * Dec 15, 2014   4211     Tracy H.    Added better explanation
 * Jul 23, 2015   4245     Chris G.    Updated to work with new version of megawidget
 *                                     manager listener.
 * Aug 12, 2015   4123     Chris.G.    Changed to work with latest version of
 *                                     megawidget manager listener.
 * Feb 24, 2016  13929     Robert.Blum Remove first part of staging dialog.
 * Mar 21, 2017  29996     Robert.Blum Updated to allow refreshMetadata to work.
 * Oct 10, 2017  39151     Chris G.    Changed to handle new parameter for megawidget
 *                                     manager constructor.
 * </pre>
 * 
 * @author shouming.wei
 * @version 1.0
 */
class ProductStagingDialog extends BasicDialog implements IProductStagingView {

    // Private Static Constants

    /**
     * Dialog title.
     */
    private static final String DIALOG_TITLE = "Product Staging";

    /**
     * OK button text.
     */
    private static final String OK_BUTTON_TEXT = "Continue";

    /**
     * Logging mechanism.
     */
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(ProductStagingDialog.class);

    // Private Variables

    /**
     * Tab folder used to hold the product tabs.
     */
    private CTabFolder tabFolder;

    /**
     * Names of products for which tabs are to be shown.
     */
    private List<String> productNames;

    /**
     * Map of product names to megawidget specifier managers providing the
     * specifiers for the megawidgets to be built for said products.
     */
    private Map<String, MegawidgetSpecifierManager> megawidgetSpecifierManagersForProductNames;

    /**
     * Map of product names to megawidget managers with the megawidgets for said
     * products.
     */
    private Map<String, MegawidgetManager> megawidgetManagersForProductNames;

    /**
     * Minimum visible time for graphical time megawidgets.
     */
    private long minimumVisibleTime;

    /**
     * Maximum visible time for graphical time megawidgets.
     */
    private long maximumVisibleTime;

    /**
     * Product metadata state change handler. The qualifier is the product
     * having its associated metadata state changed, while the identifier is the
     * metadata identifier that is experiencing the state change. There is no
     * associated {@link IQualifiedStateChanger} because the state cannot be
     * changed, have its enabled state or editability changed, etc. by the
     * presenter.
     */
    private IQualifiedStateChangeHandler<String, String, Object> productMetadataChangeHandler;

    /**
     * Button invocation handler, for the buttons at the bottom of the dialog.
     * The identifier is the command. There is no associated
     * {@link ICommandInvoker} because the enabled state of the buttons cannot
     * be changed by the presenter.
     */
    private ICommandInvocationHandler<Command> buttonInvocationHandler;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param parent
     *            Parent shell for this dialog.
     */
    public ProductStagingDialog(Shell parent) {
        super(parent);
        setShellStyle(SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE);
        setBlockOnOpen(false);
    }

    // Public Methods

    /**
     * Initialize the content.
     * 
     * @param productNames
     *            Names of the products for which widgets are to be shown to
     *            allow the changing of product-specific metadata.
     * @param megawidgetSpecifierManagersForProductNames
     *            Map of product names to megawidget specifier managers
     *            providing the specifiers for the megawidgets to be built for
     *            said products.
     * @param minimumVisibleTime
     *            Minimum visible time for any widgets displaying time
     *            graphically.
     * @param maximumVisibleTime
     *            Maximum visible time for any widgets displaying time
     *            graphically.
     */
    public void initialize(List<String> productNames,
            Map<String, MegawidgetSpecifierManager> megawidgetSpecifierManagersForProductNames,
            long minimumVisibleTime, long maximumVisibleTime) {
        this.productNames = productNames;
        this.megawidgetSpecifierManagersForProductNames = megawidgetSpecifierManagersForProductNames;
        this.minimumVisibleTime = minimumVisibleTime;
        this.maximumVisibleTime = maximumVisibleTime;
        this.megawidgetManagersForProductNames = new HashMap<>();

        /*
         * If the shell exists, the dialog is already showing, in which case the
         * widgets need to be created. Otherwise, they will be created when
         * createDialogArea() is called.
         */
        if ((getShell() != null) && getShell().isVisible()) {
            createTabs();
        }
    }

    @Override
    public void setProductMetadataChangeHandler(
            IQualifiedStateChangeHandler<String, String, Object> handler) {
        productMetadataChangeHandler = handler;
    }

    @Override
    public void setButtonInvocationHandler(
            ICommandInvocationHandler<Command> handler) {
        buttonInvocationHandler = handler;
    }

    @Override
    public boolean close() {
        getShell().setCursor(null);
        return super.close();
    }

    /**
     * Get the megawidget manager for the specified product.
     * 
     * @param productName
     * @return
     */
    public MegawidgetManager getMegawidgetManager(String productName) {
        return megawidgetManagersForProductNames.get(productName);
    }

    // Protected Methods

    @Override
    protected int getDialogBoundsStrategy() {
        return DIALOG_PERSISTLOCATION | DIALOG_PERSISTSIZE;
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
        tabFolder = new CTabFolder(top, SWT.TOP);
        tabFolder.setBorderVisible(true);
        createTabs();
        tabFolder.pack();
        return top;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        boolean okLeft = (Display.getDefault()
                .getDismissalAlignment() == SWT.LEFT);
        createButton(parent,
                (okLeft ? IDialogConstants.OK_ID : IDialogConstants.CANCEL_ID),
                (okLeft ? OK_BUTTON_TEXT : IDialogConstants.CANCEL_LABEL),
                okLeft);
        createButton(parent,
                (okLeft ? IDialogConstants.CANCEL_ID : IDialogConstants.OK_ID),
                (okLeft ? IDialogConstants.CANCEL_LABEL : OK_BUTTON_TEXT),
                !okLeft);
    }

    @Override
    protected void buttonPressed(int buttonId) {
        super.buttonPressed(buttonId);
        if (buttonInvocationHandler != null) {
            if (buttonId == IDialogConstants.OK_ID) {
                getShell().setCursor(
                        Display.getDefault().getSystemCursor(SWT.CURSOR_WAIT));
            }
            buttonInvocationHandler
                    .commandInvoked(buttonId == IDialogConstants.OK_ID
                            ? Command.CONTINUE : Command.CANCEL);
        }
    }

    @Override
    protected Point getInitialSize() {

        /*
         * Use a default size unless a saved size is found, in which case use
         * that.
         */
        Point minimumSize = new Point(700, 450);
        Point size = new Point(minimumSize.x, minimumSize.y);
        IDialogSettings settings = getDialogBoundsSettings();
        if (settings != null) {
            try {
                int width = settings.getInt(DIALOG_WIDTH);
                if (width != DIALOG_DEFAULT_BOUNDS) {
                    size.x = width;
                }
                int height = settings.getInt(DIALOG_HEIGHT);
                if (height != DIALOG_DEFAULT_BOUNDS) {
                    size.y = height;
                }
            } catch (NumberFormatException e) {
                statusHandler
                        .debug("Bad dialog size (" + settings.get(DIALOG_WIDTH)
                                + "," + settings.get(DIALOG_HEIGHT)
                                + "); using default values.");
                size = minimumSize;
            }
        }
        getShell().setMinimumSize(minimumSize);
        return size;
    }

    /**
     * Respond to the OK button being pressed. This implementation does nothing,
     * since the dialog is closed by the controlling view if needed. (The button
     * press still sends a notification of its invocation.)
     */
    @Override
    protected void okPressed() {

        /*
         * No action.
         */
    }

    // Private Methods

    /**
     * Create the tabs.
     */
    private void createTabs() {
        tabFolder.setRedraw(false);
        resetContents();
        for (final String productName : productNames) {
            CTabItem tabItem = new CTabItem(tabFolder, SWT.NONE);
            tabItem.setText(productName);
            Composite composite = createTabPageComposite(tabFolder, false);
            createTabWidgets(productName, composite);
            tabItem.setControl(composite);
        }
        tabFolder.setSelection(0);
        getShell().setCursor(null);
        tabFolder.setRedraw(true);
    }

    /**
     * Create the widgets for the specified product name's tab.
     * 
     * @param productName
     *            Name of the product for which the widgets are to be created.
     * @param composite
     *            Parent of the created widgets.
     */
    private void createTabWidgets(final String productName,
            Composite composite) {
        MegawidgetSpecifierManager specifierManager = megawidgetSpecifierManagersForProductNames
                .get(productName);
        Map<String, Object> startingStates = new HashMap<>();
        specifierManager.populateWithStartingStates(startingStates);
        try {
            MegawidgetManager megawidgetManager = new MegawidgetManager(
                    composite, specifierManager, startingStates,
                    new IMegawidgetManagerListener() {

                        @Override
                        public void commandInvoked(MegawidgetManager manager,
                                String identifier) {

                            /*
                             * No action; interdependencies script may react if
                             * so configured.
                             */
                        }

                        @Override
                        public void stateElementChanged(
                                MegawidgetManager manager, String identifier,
                                Object state) {
                            if (productMetadataChangeHandler != null) {
                                productMetadataChangeHandler.stateChanged(
                                        productName, identifier, state);
                            }
                        }

                        @Override
                        public void stateElementsChanged(
                                MegawidgetManager manager,
                                Map<String, ?> statesForIdentifiers) {
                            if (productMetadataChangeHandler != null) {
                                productMetadataChangeHandler.statesChanged(
                                        productName,
                                        new HashMap<>(statesForIdentifiers));
                            }
                        }

                        @Override
                        public void sizeChanged(MegawidgetManager manager,
                                String identifier) {

                            /*
                             * No action; size changes of any children should be
                             * handled by scrollable wrapper megawidget.
                             */
                        }

                        @Override
                        public void visibleTimeRangeChanged(
                                MegawidgetManager manager, String identifier,
                                long lower, long upper) {
                            /*
                             * No action; the visible time range of any
                             * megawidget in the product staging dialog is not
                             * tied to the visible time range elsewhere.
                             */
                        }

                        @Override
                        public void sideEffectMutablePropertyChangeErrorOccurred(
                                MegawidgetManager manager,
                                MegawidgetPropertyException exception) {
                            statusHandler
                                    .error("Error occurred while attempting to "
                                            + "apply megawidget interdependencies: "
                                            + exception, exception);
                        }
                    }, null, minimumVisibleTime, maximumVisibleTime);
            megawidgetManagersForProductNames.put(productName,
                    megawidgetManager);
        } catch (MegawidgetException e) {
            statusHandler
                    .error("unexpected problem creating metadata megawidgets for product "
                            + productName + ": " + e, e);
        }
    }

    /**
     * Reset the contents of the dialog to allow it to be reused.
     */
    private void resetContents() {
        for (CTabItem item : tabFolder.getItems()) {
            item.dispose();
        }
        megawidgetManagersForProductNames.clear();
    }

    /**
     * Create the composite to be used to hold the contents of a tab page.
     * 
     * @param parent
     *            Parent of the content composite.
     * @param pad
     *            Flag indicating whether or not to add padding around the
     *            composite.
     * @return Created tab folder page.
     */
    private Composite createTabPageComposite(Composite parent, boolean pad) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(1, true);
        if (pad) {
            layout.marginLeft = layout.marginRight = layout.marginTop = layout.marginBottom = 0;
            layout.marginWidth = 10;
            layout.marginHeight = 5;
        } else {
            layout.marginWidth = layout.marginHeight = 0;
        }
        composite.setLayout(layout);
        return composite;
    }

    /**
     * Refreshes the metadata for the specified product name.
     * 
     * @param productName
     * @param manager
     */
    public void refreshStagingMetadata(String productName,
            MegawidgetSpecifierManager manager,
            Map<String, Map<String, Object>> visiblePages) {
        megawidgetSpecifierManagersForProductNames.put(productName, manager);
        // Find the tab that needs updated
        for (CTabItem item : tabFolder.getItems()) {
            if (item.getText().equals(productName)) {
                Composite comp = (Composite) item.getControl();
                // Dispose of the previous Megawidgets
                for (Control control : comp.getChildren()) {
                    control.dispose();
                }

                // Create the new Megawidgets
                createTabWidgets(productName, comp);
                try {
                    // Set the correct visible pages
                    megawidgetManagersForProductNames.get(productName)
                            .setMutableProperties(visiblePages);
                } catch (MegawidgetPropertyException e) {
                    statusHandler.handle(Priority.PROBLEM,
                            e.getLocalizedMessage(), e);
                }
            }
        }
    }
}

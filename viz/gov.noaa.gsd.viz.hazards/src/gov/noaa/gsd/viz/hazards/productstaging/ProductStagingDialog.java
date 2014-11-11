/**
 * This software was developed and / or modified by the National Oceanic and
 * Atmospheric Administration (NOAA), Earth System Research Laboratory (ESRL),
 * Global Systems Division (GSD), Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.productstaging;

import gov.noaa.gsd.viz.hazards.productstaging.ProductStagingPresenter.Command;
import gov.noaa.gsd.viz.hazards.ui.BasicDialog;
import gov.noaa.gsd.viz.megawidgets.BoundedChoicesMegawidgetSpecifier;
import gov.noaa.gsd.viz.megawidgets.CheckListMegawidget;
import gov.noaa.gsd.viz.megawidgets.CheckListSpecifier;
import gov.noaa.gsd.viz.megawidgets.IMegawidgetManagerListener;
import gov.noaa.gsd.viz.megawidgets.IMultiLineSpecifier;
import gov.noaa.gsd.viz.megawidgets.ISpecifier;
import gov.noaa.gsd.viz.megawidgets.IStateChangeListener;
import gov.noaa.gsd.viz.megawidgets.IStateful;
import gov.noaa.gsd.viz.megawidgets.IStatefulSpecifier;
import gov.noaa.gsd.viz.megawidgets.MegawidgetException;
import gov.noaa.gsd.viz.megawidgets.MegawidgetManager;
import gov.noaa.gsd.viz.megawidgets.MegawidgetPropertyException;
import gov.noaa.gsd.viz.megawidgets.MegawidgetSpecifierManager;
import gov.noaa.gsd.viz.mvp.widgets.ICommandInvocationHandler;
import gov.noaa.gsd.viz.mvp.widgets.ICommandInvoker;
import gov.noaa.gsd.viz.mvp.widgets.IQualifiedStateChangeHandler;
import gov.noaa.gsd.viz.mvp.widgets.IQualifiedStateChanger;
import gov.noaa.gsd.viz.mvp.widgets.IStateChangeHandler;
import gov.noaa.gsd.viz.mvp.widgets.IStateChanger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.google.common.collect.ImmutableMap;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

/**
 * Description: Product staging dialog, used to stage products that are to be
 * created via preview or issuance of hazard events. Staging involves at most
 * two steps. The first step is displayed only if at least one product to be
 * created is of a type that would allow additional, currently unselected hazard
 * events to be incorporated into its creation; it allows the user to choose
 * which hazard events are to be incorporated. The second step is displayed only
 * if at least one product to be created has additional metadata information to
 * be collected from the user. Depending upon what hazard events are selected
 * when preview or issuance is attempted, none, one or both steps may be shown.
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
     * Type of the associated events megawidget.
     */
    private static final String CHECK_LIST_MEGAWIDGET_TYPE = "CheckList";

    /**
     * Identifier prefix of the associated events megawidget.
     */
    private static final String ASSOCIATED_EVENTS_IDENTIFIER_PREFIX = "associatedEvents-";

    /**
     * Label for the associated events megawidget.
     */
    private static final String COMBINE_MESSAGE = "When issuing this hazard, there are "
            + "other related hazards that could be included in the legacy product:";

    /**
     * Specifier parameters for the category combo box megawidget.
     */
    private static final ImmutableMap<String, Object> ASSOCIATED_EVENTS_SPECIFIER_PARAMETERS;
    static {
        Map<String, Object> map = new HashMap<>(3, 1.0f);
        map.put(ISpecifier.MEGAWIDGET_TYPE, CHECK_LIST_MEGAWIDGET_TYPE);
        map.put(ISpecifier.MEGAWIDGET_LABEL, COMBINE_MESSAGE);
        map.put(IMultiLineSpecifier.MEGAWIDGET_VISIBLE_LINES, 10);
        ASSOCIATED_EVENTS_SPECIFIER_PARAMETERS = ImmutableMap.copyOf(map);
    }

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
     * Associated events listener, used to receive notifications concerning
     * associated events changing from checklist megawidgets and pass on the
     * changes to the proper state change handler.
     */
    private final IStateChangeListener associatedEventsListener = new IStateChangeListener() {

        @SuppressWarnings("unchecked")
        @Override
        public void megawidgetStateChanged(IStateful megawidget,
                String identifier, Object state) {
            if (associatedEventsChangeHandler != null) {
                associatedEventsChangeHandler.stateChanged(
                        identifier
                                .substring(ASSOCIATED_EVENTS_IDENTIFIER_PREFIX
                                        .length()), (List<String>) state);
            }
        }

        @Override
        public void megawidgetStatesChanged(IStateful megawidget,
                Map<String, Object> statesForIdentifiers) {
            throw new UnsupportedOperationException(
                    "cannot change multiple states simultaneously");
        }
    };

    /**
     * Listener for window resizing events that change the scrolled composites'
     * sizes.
     */
    private final ControlListener scrollableResizeListener = new ControlAdapter() {
        @Override
        public void controlResized(ControlEvent e) {

            /*
             * Schedule a resize of the page increment for each scrolled
             * composite to happen after the laying out of the panels is
             * complete. The latter must be done asynchronously to ensure the
             * laying out is done before it proceeds, otherwise it gets the
             * wrong information from the scrollbars.
             */
            Display.getDefault().asyncExec(new Runnable() {
                @Override
                public void run() {
                    for (ScrolledComposite scrolledComposite : scrolledCompositesForMegawidgetManagers
                            .values()) {
                        recalculateScrolledCompositePageIncrement(scrolledComposite);
                    }
                }
            });
        }
    };

    /**
     * Megawidget creation time parameter map.
     */
    private final Map<String, Object> associatedEventsCreationTimeParams = new HashMap<>(
            1, 1.0f);

    /**
     * Step that the dialog should be showing; may be 0 (meaning no step has
     * been chosen yet), 1, or 2.
     */
    private int step;

    /**
     * Names of products for which tabs are to be shown (used for both the first
     * and second steps).
     */
    private List<String> productNames;

    /**
     * Map of product names to the identifiers of the hazard events that may be
     * associated with said products (used for the first step).
     */
    private Map<String, List<String>> possibleEventIdsForProductNames;

    /**
     * Map of product names to lists of descriptions of hazard events that may
     * be associated with said products by the user. For each such list, the
     * descriptions within it are associated with the event identifiers at the
     * same indices within the list for the corresponding product found in
     * <code>possibleEventIdsForProductNames</code>.
     */
    private Map<String, List<String>> possibleEventDescriptionsForProductNames;

    /**
     * Map of product names to the identifiers of the hazard events that are
     * associated with said products (used for the first step).
     */
    private Map<String, List<String>> selectedEventIdsForProductNames;

    /**
     * Flag indicating, when the dialog is showing step two, whether or not the
     * first step was skipped. This must be remembered so that the Back button
     * may be enabled or disabled as appropriate.
     */
    private boolean firstStepSkipped;

    /**
     * Map of product names to megawidget specifier managers providing the
     * specifiers for the megawidgets to be built for said products (used for
     * the second step).
     */
    private Map<String, MegawidgetSpecifierManager> megawidgetSpecifierManagersForProductNames;

    /**
     * Map of megawidget managers for different products to the associated
     * scrollable composites (used for the second step).
     */
    private final Map<MegawidgetManager, ScrolledComposite> scrolledCompositesForMegawidgetManagers = new HashMap<>();

    /**
     * Set of scrollable composites whose contents are currently changing.
     */
    private final Set<ScrolledComposite> scrolledCompositesUndergoingChange = new HashSet<>();

    /**
     * Minimum visible time for graphical time megawidgets.
     */
    private long minimumVisibleTime;

    /**
     * Maximum visible time for graphical time megawidgets.
     */
    private long maximumVisibleTime;

    /**
     * Associated events state change handler. The identifier is that of the
     * product having its associated events changed. There is no associated
     * {@link IStateChanger} because the state cannot be changed, have its
     * enabled state or editability changed, etc. by the presenter.
     */
    private IStateChangeHandler<String, List<String>> associatedEventsChangeHandler;

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
        associatedEventsCreationTimeParams.put(IStateful.STATE_CHANGE_LISTENER,
                associatedEventsListener);
    }

    // Public Methods

    /**
     * Initialize the content for the first step.
     * 
     * @param productNames
     *            Names of the products for which widgets are to be shown to
     *            allow the selection of hazard events to be associated with
     *            said products.
     * @param possibleEventIdsForProductNames
     *            Map of product names to lists of hazard events that may be
     *            associated with said products by the user.
     * @param possibleEventDescriptionsForProductNames
     *            Map of product names to lists of descriptions of hazard events
     *            that may be associated with said products by the user. For
     *            each such list, the descriptions within it are associated with
     *            the event identifiers at the same indices within the list for
     *            the corresponding product found in
     *            <code>possibleEventIdsForProductNames</code>.
     * @param selectedEventIdsForProductNames
     *            Map of product names to lists of hazard events that should
     *            start out as associated with said products when the widgets
     *            allowing the changing of selection are first displayed.
     */
    public void initializeFirstStep(List<String> productNames,
            Map<String, List<String>> possibleEventIdsForProductNames,
            Map<String, List<String>> possibleEventDescriptionsForProductNames,
            Map<String, List<String>> selectedEventIdsForProductNames) {
        this.productNames = productNames;
        this.possibleEventIdsForProductNames = possibleEventIdsForProductNames;
        this.possibleEventDescriptionsForProductNames = possibleEventDescriptionsForProductNames;
        this.selectedEventIdsForProductNames = selectedEventIdsForProductNames;
        step = 1;

        /*
         * If the shell exists, the dialog is already showing, in which case the
         * widgets for the first step need to be created. Otherwise, they will
         * be created when createDialogArea() is called.
         */
        if ((getShell() != null) && getShell().isVisible()) {
            createWidgetsForFirstStep();
            getButton(IDialogConstants.BACK_ID).setEnabled(false);
        }
    }

    /**
     * Initialize the content for the second step.
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
     * @param firstStepSkipped
     *            Flag indicating whether or not the first step was skipped.
     */
    public void initializeSecondStep(
            List<String> productNames,
            Map<String, MegawidgetSpecifierManager> megawidgetSpecifierManagersForProductNames,
            long minimumVisibleTime, long maximumVisibleTime,
            boolean firstStepSkipped) {
        this.productNames = productNames;
        this.megawidgetSpecifierManagersForProductNames = megawidgetSpecifierManagersForProductNames;
        this.minimumVisibleTime = minimumVisibleTime;
        this.maximumVisibleTime = maximumVisibleTime;
        this.firstStepSkipped = firstStepSkipped;
        step = 2;

        /*
         * If the shell exists, the dialog is already showing, in which case the
         * widgets for the second step need to be created. Otherwise, they will
         * be created when createDialogArea() is called.
         */
        if ((getShell() != null) && getShell().isVisible()) {
            createWidgetsForSecondStep();
            getButton(IDialogConstants.BACK_ID).setEnabled(!firstStepSkipped);
        }
    }

    @Override
    public void setAssociatedEventsChangeHandler(
            IStateChangeHandler<String, List<String>> handler) {
        associatedEventsChangeHandler = handler;
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

        /*
         * If the step has been set, create the widgets appropriate to that
         * step. Otherwise, they will be created later, when a step is set.
         */
        if (step == 1) {
            createWidgetsForFirstStep();
        } else if (step == 2) {
            createWidgetsForSecondStep();
        }
        tabFolder.pack();
        return top;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        boolean okLeft = (Display.getDefault().getDismissalAlignment() == SWT.LEFT);
        createButton(parent, (okLeft ? IDialogConstants.OK_ID
                : IDialogConstants.CANCEL_ID), (okLeft ? OK_BUTTON_TEXT
                : IDialogConstants.CANCEL_LABEL), okLeft);
        Button backButton = createButton(parent, IDialogConstants.BACK_ID,
                IDialogConstants.BACK_LABEL, false);
        if (step > 0) {
            backButton.setEnabled((step == 2) && (firstStepSkipped == false));
        }
        createButton(parent, (okLeft ? IDialogConstants.CANCEL_ID
                : IDialogConstants.OK_ID),
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
                    .commandInvoked(buttonId == IDialogConstants.OK_ID ? Command.CONTINUE
                            : (buttonId == IDialogConstants.BACK_ID ? Command.BACK
                                    : Command.CANCEL));
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
                statusHandler.debug("Bad dialog size ("
                        + settings.get(DIALOG_WIDTH) + ","
                        + settings.get(DIALOG_HEIGHT)
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
     * Create the widgets needed to show the first step.
     */
    private void createWidgetsForFirstStep() {
        tabFolder.setRedraw(false);
        resetContents();
        for (String productName : productNames) {
            CTabItem tabItem = new CTabItem(tabFolder, SWT.NONE);
            tabItem.setText(productName);
            Composite composite = createTabPageComposite(tabFolder);
            Map<String, Object> rawSpecifier = new HashMap<>(
                    ASSOCIATED_EVENTS_SPECIFIER_PARAMETERS);
            rawSpecifier.put(ISpecifier.MEGAWIDGET_IDENTIFIER,
                    ASSOCIATED_EVENTS_IDENTIFIER_PREFIX + productName);
            List<String> possibleEventIds = possibleEventIdsForProductNames
                    .get(productName);
            List<String> possibleEventDescriptions = possibleEventDescriptionsForProductNames
                    .get(productName);
            List<Object> choices = new ArrayList<>(possibleEventIds.size());
            for (int j = 0; j < possibleEventIds.size(); j++) {
                Map<String, Object> choice = new HashMap<>(2, 1.0f);
                choice.put(BoundedChoicesMegawidgetSpecifier.CHOICE_IDENTIFIER,
                        possibleEventIds.get(j));
                choice.put(BoundedChoicesMegawidgetSpecifier.CHOICE_NAME,
                        possibleEventDescriptions.get(j));
                choices.add(choice);
            }
            rawSpecifier.put(
                    BoundedChoicesMegawidgetSpecifier.MEGAWIDGET_VALUE_CHOICES,
                    choices);
            rawSpecifier.put(IStatefulSpecifier.MEGAWIDGET_STATE_VALUES,
                    selectedEventIdsForProductNames.get(productName));
            try {
                new CheckListSpecifier(rawSpecifier).createMegawidget(
                        composite, CheckListMegawidget.class,
                        associatedEventsCreationTimeParams);
            } catch (MegawidgetException e) {
                statusHandler.error(
                        "unexpected problem creating checklist megawidget for product "
                                + productName, e);
            }
            composite.pack();
            tabItem.setControl(composite);
        }
        tabFolder.setSelection(0);
        tabFolder.setRedraw(true);
    }

    /**
     * Create the widgets needed to show the second step.
     */
    private void createWidgetsForSecondStep() {
        tabFolder.setRedraw(false);
        resetContents();
        for (final String productName : productNames) {
            CTabItem tabItem = new CTabItem(tabFolder, SWT.NONE);
            tabItem.setText(productName);

            ScrolledComposite scrolledComposite = new ScrolledComposite(
                    tabFolder, SWT.H_SCROLL | SWT.V_SCROLL);
            Composite composite = createTabPageComposite(scrolledComposite);
            scrolledComposite.setContent(composite);
            scrolledComposite.setExpandHorizontal(true);
            scrolledComposite.setExpandVertical(true);
            scrolledComposite.getHorizontalBar().setIncrement(
                    HazardConstants.SCROLLBAR_BUTTON_INCREMENT);
            scrolledComposite.getVerticalBar().setIncrement(
                    HazardConstants.SCROLLBAR_BUTTON_INCREMENT);
            scrolledComposite.addControlListener(scrollableResizeListener);

            MegawidgetSpecifierManager specifierManager = megawidgetSpecifierManagersForProductNames
                    .get(productName);
            Map<String, Object> startingStates = new HashMap<>();
            specifierManager.populateWithStartingStates(startingStates);
            try {
                MegawidgetManager megawidgetManager = new MegawidgetManager(
                        composite, specifierManager, startingStates,
                        new IMegawidgetManagerListener() {

                            @Override
                            public void commandInvoked(
                                    MegawidgetManager manager, String identifier) {

                                /*
                                 * No action; interdependencies script may react
                                 * if so configured.
                                 */
                            }

                            @Override
                            public void stateElementChanged(
                                    MegawidgetManager manager,
                                    String identifier, Object state) {
                                if (productMetadataChangeHandler != null) {
                                    productMetadataChangeHandler.stateChanged(
                                            productName, identifier, state);
                                }
                            }

                            @Override
                            public void stateElementsChanged(
                                    MegawidgetManager manager,
                                    Map<String, Object> statesForIdentifiers) {
                                if (productMetadataChangeHandler != null) {
                                    productMetadataChangeHandler.statesChanged(
                                            productName, statesForIdentifiers);
                                }
                            }

                            @Override
                            public void sizeChanged(MegawidgetManager manager,
                                    String identifier) {
                                recalculateScrolledCompositeClientArea(scrolledCompositesForMegawidgetManagers
                                        .get(manager));
                            }

                            @Override
                            public void sideEffectMutablePropertyChangeErrorOccurred(
                                    MegawidgetManager manager,
                                    MegawidgetPropertyException exception) {
                                statusHandler
                                        .error("Error occurred while attempting to "
                                                + "apply megawidget interdependencies",
                                                exception);
                            }
                        }, minimumVisibleTime, maximumVisibleTime);
                scrolledCompositesForMegawidgetManagers.put(megawidgetManager,
                        scrolledComposite);
                recalculateScrolledCompositeClientArea(scrolledComposite);
            } catch (MegawidgetException e) {
                statusHandler.error(
                        "unexpected problem creating metadata megawidgets for product "
                                + productName, e);
            }
            tabItem.setControl(scrolledComposite);
        }
        tabFolder.setSelection(0);
        getShell().setCursor(null);
        tabFolder.setRedraw(true);
    }

    /**
     * Reset the contents of the dialog to allow it to be reused.
     */
    private void resetContents() {
        for (CTabItem item : tabFolder.getItems()) {
            item.dispose();
        }
        scrolledCompositesForMegawidgetManagers.clear();
        scrolledCompositesUndergoingChange.clear();
    }

    /**
     * Create the composite to be used to hold the contents of a tab page.
     * 
     * @param parent
     *            Parent of the content
     * @param product
     *            Product for this tab page.
     * @return Created tab folder page.
     */
    private Composite createTabPageComposite(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(1, true);
        layout.marginLeft = layout.marginRight = layout.marginTop = layout.marginBottom = 0;
        layout.marginWidth = 10;
        layout.marginHeight = 5;
        composite.setLayout(layout);
        return composite;
    }

    /**
     * Recalculate the specified scrolled composite's client area.
     * 
     * @param scrolledComposite
     *            Scrolled composite that is to have its client area size
     *            recalculated.
     */
    private void recalculateScrolledCompositeClientArea(
            ScrolledComposite scrolledComposite) {
        if (scrolledCompositesUndergoingChange.contains(scrolledComposite)
                || scrolledComposite.isDisposed()) {
            return;
        }
        scrolledCompositesUndergoingChange.add(scrolledComposite);
        Control content = scrolledComposite.getContent();
        Point size = content.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        content.setSize(size);
        scrolledComposite.setMinSize(size);
        recalculateScrolledCompositePageIncrement(scrolledComposite);
        scrolledCompositesUndergoingChange.remove(scrolledComposite);
    }

    /**
     * Recalculate the specified scrolled composite's page increment.
     * 
     * @param scrolledComposite
     *            Scrolled composite that is to have its page increment
     *            recalculated.
     */
    private void recalculateScrolledCompositePageIncrement(
            ScrolledComposite scrolledComposite) {
        if (scrolledComposite.isDisposed()) {
            return;
        }
        scrolledComposite.getHorizontalBar().setPageIncrement(
                scrolledComposite.getHorizontalBar().getThumb());
        scrolledComposite.getVerticalBar().setPageIncrement(
                scrolledComposite.getVerticalBar().getThumb());
    }
}

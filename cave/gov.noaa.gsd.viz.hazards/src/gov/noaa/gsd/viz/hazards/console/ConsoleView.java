/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.console;

import gov.noaa.gsd.viz.hazards.display.HazardServicesActivator;
import gov.noaa.gsd.viz.hazards.display.RCPMainUserInterfaceElement;
import gov.noaa.gsd.viz.hazards.display.ViewPartDelegatorView;
import gov.noaa.gsd.viz.hazards.display.action.ConsoleAction;
import gov.noaa.gsd.viz.hazards.jsonutilities.Dict;
import gov.noaa.gsd.viz.hazards.servicebackup.ChangeSiteAction;
import gov.noaa.gsd.viz.hazards.toolbar.BasicAction;
import gov.noaa.gsd.viz.hazards.toolbar.ComboAction;
import gov.noaa.gsd.viz.hazards.toolbar.IContributionManagerAware;
import gov.noaa.gsd.viz.hazards.toolbar.SeparatorAction;
import gov.noaa.gsd.viz.mvp.IMainUiContributor;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.internal.WorkbenchPage;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.hazards.sessionmanager.alerts.IHazardAlert;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Settings;

/**
 * Console view, an implementation of IConsoleView that provides an Eclipse
 * ViewPart-based view.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Chris.Golden      Initial induction into repo
 * May 08, 2013            Chris.Golden      Moved view-part-managing code
 *                                           to new superclass.
 * Jul 12, 2013     585    Chris.Golden      Changed to support loading from bundle.
 * Aug 22, 2013     787    Bryon.Lawrence    Added references to constants for 
 *                                           RESET_EVENTS, RESET_SETTINGS, and 
 *                                           RESET_ACTION
 * Aug 22, 2013    1936    Chris.Golden      Added console countdown timers.
 * Oct 22, 2013    1463    Bryon.Lawrence    Added menu options for hazard 
 *                                           conflict detection.
 * Oct 22, 2013    1462    Bryon.Lawrence    Added menu options for hatched
 *                                           area display options.
 * 
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
@SuppressWarnings("restriction")
public class ConsoleView extends ViewPartDelegatorView<ConsoleViewPart>
        implements IConsoleView<Action, RCPMainUserInterfaceElement> {

    // Public Static Constants

    /**
     * Reset events command menu item text.
     */
    public static final String RESET_EVENTS_COMMAND_MENU_TEXT = "Reset Events";

    /**
     * Reset settings command menu item text.
     */
    public static final String RESET_SETTINGS_COMMAND_MENU_TEXT = "Reset Settings";

    /**
     * Check hazard conflicts command menu item text.
     */
    public static final String CHECK_HAZARD_CONFLICTS_MENU_TEXT = "Check Hazard Conflicts";

    /**
     * Auto check hazard conflicts command menu item text.
     */
    public static final String AUTO_CHECK_HAZARD_CONFLICTS_MENU_TEXT = "Auto Check Hazard Conflicts";

    /**
     * Show hazard area command menu item text.
     */
    public static final String SHOW_HATCHED_AREAS_MENU_TEXT = "Show Hatched Areas";

    /**
     * Suffix for the preferences key used to determine whether or not to detach
     * the view part when next showing it (assuming that it is not being shown
     * as a result of a bundle load).
     */
    private static final String FORCE_DETACH_CONSOLE_WHEN_NEXT_SHOWING_SUFFIX = ".forceDetachConsoleWhenNextShowing";

    /**
     * Suffix for the preferences key holding the X value of the bounds of the
     * last saved detached view; this is only relevant if the value for the
     * preference key <code>FORCE_DETACH_CONSOLE_WHEN_NEXT_SHOWING_SUFFIX</code>
     * for the same perspective is <code>true</code>.
     */
    private static final String LAST_DETACHED_BOUNDS_X_SUFFIX = ".lastDetachedBoundsX";

    /**
     * Suffix for the preferences key holding the Y value of the bounds of the
     * last saved detached view; this is only relevant if the value for the
     * preference key <code>FORCE_DETACH_CONSOLE_WHEN_NEXT_SHOWING_SUFFIX</code>
     * for the same perspective is <code>true</code>.
     */
    private static final String LAST_DETACHED_BOUNDS_Y_SUFFIX = ".lastDetachedBoundsY";

    /**
     * Suffix for the preferences key holding the width value of the bounds of
     * the last saved detached view; this is only relevant if the value for the
     * preference key <code>FORCE_DETACH_CONSOLE_WHEN_NEXT_SHOWING_SUFFIX</code>
     * for the same perspective is <code>true</code>.
     */
    private static final String LAST_DETACHED_BOUNDS_WIDTH_SUFFIX = ".lastDetachedBoundsWidth";

    /**
     * Suffix for the preferences key holding the height value of the bounds of
     * the last saved detached view; this is only relevant if the value for the
     * preference key <code>FORCE_DETACH_CONSOLE_WHEN_NEXT_SHOWING_SUFFIX</code>
     * for the same perspective is <code>true</code>.
     */
    private static final String LAST_DETACHED_BOUNDS_HEIGHT_SUFFIX = ".lastDetachedBoundsHeight";

    /**
     * Logging mechanism.
     */
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(ConsoleView.class);

    // Package Interfaces

    /**
     * Interface that must be implemented by actions in order to allow the
     * temporal display to be set after construction.
     */
    interface ITemporalDisplayAware {

        // Public Methods

        /**
         * Set the temporal display to that specified.
         * 
         * @param temporalDisplay
         *            Temporal display.
         */
        public void setTemporalDisplay(TemporalDisplay temporalDisplay);
    }

    // Private Classes

    /**
     * Standard action.
     */
    private class BasicConsoleAction extends BasicAction {

        // Private Variables

        /**
         * Action type.
         */
        private final ConsoleAction.ActionType actionType;

        /**
         * Action name.
         */
        private final String actionName;

        // Public Constructors

        /**
         * Construct a standard instance.
         * 
         * @param text
         *            Text to be displayed.
         * @param iconFileName
         *            File name of the icon to be displayed, or
         *            <code>null</code> if no icon is to be associated with this
         *            action.
         * @param style
         *            Style; one of the <code>IAction</code> style constants.
         * @param toolTipText
         *            Tool tip text, or <code>null</code> if none is required.
         * @param actionType
         *            Type of action to be taken when this action is invoked.
         * @param actionName
         *            Name of action to be taken when this action is invoked;
         *            ignored for actions of checkbox style, since they always
         *            send "on" or "off" as their action name.
         */
        public BasicConsoleAction(String text, String iconFileName, int style,
                String toolTipText, ConsoleAction.ActionType actionType,
                String actionName) {
            super(text, iconFileName, style, toolTipText);
            this.actionType = actionType;
            this.actionName = actionName;
        }

        // Public Methods

        /**
         * Run the action.
         */
        @Override
        public void run() {
            fireConsoleAction(actionType, actionName);
        }
    }

    /**
     * Timeline navigation action. Each instance is for one of the navigation
     * buttons in the toolbar.
     */
    private class NavigationAction extends BasicAction implements
            ITemporalDisplayAware {

        // Private Variables

        /**
         * Temporal display to be manipulated by changes in this action's state.
         * Note that this may be set to a non-<code>null</code> value only after
         * member methods are called, which is why a check for <code>null</code>
         * is performed in those cases.
         */
        private TemporalDisplay temporalDisplay;

        // Public Constructors

        /**
         * Construct a standard instance.
         * 
         * @param iconFileName
         *            File name of the icon to be displayed.
         * @param toolTipText
         *            Tool tip text, or <code>null</code> if none is required.
         */
        public NavigationAction(String iconFileName, String toolTipText) {
            super("", iconFileName, Action.AS_PUSH_BUTTON, toolTipText);
        }

        // Public Methods

        /**
         * Set the temporal display to that specified.
         * 
         * @param temporalDisplay
         *            Temporal display.
         */
        @Override
        public void setTemporalDisplay(TemporalDisplay temporalDisplay) {
            this.temporalDisplay = temporalDisplay;
        }

        /**
         * Run the action.
         */
        @Override
        public void run() {
            if (temporalDisplay == null) {
                return;
            }
            if (getToolTipText().equals(
                    TemporalDisplay.TOOLBAR_BUTTON_DESCRIPTIONS.get(0))) {
                temporalDisplay.zoomTimeOut();
            } else if (getToolTipText().equals(
                    TemporalDisplay.TOOLBAR_BUTTON_DESCRIPTIONS.get(1))) {
                temporalDisplay.pageTimeBack();
            } else if (getToolTipText().equals(
                    TemporalDisplay.TOOLBAR_BUTTON_DESCRIPTIONS.get(2))) {
                temporalDisplay.panTimeBack();
            } else if (getToolTipText().equals(
                    TemporalDisplay.TOOLBAR_BUTTON_DESCRIPTIONS.get(3))) {
                temporalDisplay.showCurrentTime();
            } else if (getToolTipText().equals(
                    TemporalDisplay.TOOLBAR_BUTTON_DESCRIPTIONS.get(4))) {
                temporalDisplay.panTimeForward();
            } else if (getToolTipText().equals(
                    TemporalDisplay.TOOLBAR_BUTTON_DESCRIPTIONS.get(5))) {
                temporalDisplay.pageTimeForward();
            } else if (getToolTipText().equals(
                    TemporalDisplay.TOOLBAR_BUTTON_DESCRIPTIONS.get(6))) {
                temporalDisplay.zoomTimeIn();
            }
        }
    }

    /**
     * Selected time mode combo action.
     */
    private class SelectedTimeModeAction extends ComboAction implements
            ITemporalDisplayAware {

        // Private Variables

        /**
         * Temporal display to be manipulated by changes in this action's state.
         * Note that this may be set to a non-<code>null</code> value only after
         * member methods are called, which is why a check for <code>null</code>
         * is performed in those cases.
         */
        private TemporalDisplay temporalDisplay;

        /**
         * Listener for menu item invocations.
         */
        private final SelectionListener listener = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {

                // If the menu item has been selected (not
                // deselected), then a selected time mode has been
                // chosen.
                MenuItem item = (MenuItem) event.widget;
                if (item.getSelection()) {

                    // Update the visuals to indicate the new time
                    // mode name.
                    setSelectedChoice(item.getText());

                    // Remember the newly selected time mode name and
                    // fire off the action.
                    if (temporalDisplay != null) {
                        temporalDisplay.setSelectedTimeMode(item.getText());
                    }
                }
            }
        };

        // Public Constructors

        /**
         * Construct a standard instance.
         */
        public SelectedTimeModeAction() {
            super(TemporalDisplay.SELECTED_TIME_MODE_TEXT);
        }

        // Public Methods

        /**
         * Set the temporal display to that specified.
         * 
         * @param temporalDisplay
         *            Temporal display.
         */
        @Override
        public void setTemporalDisplay(TemporalDisplay temporalDisplay) {
            this.temporalDisplay = temporalDisplay;
        }

        // Protected Methods

        /**
         * Get the menu for the specified parent, possibly reusing the specified
         * menu if provided.
         * 
         * @param parent
         *            Parent control.
         * @param menu
         *            Menu that was created previously, if any; this may be
         *            reused, or disposed of completely.
         * @return Menu.
         */
        @Override
        protected Menu doGetMenu(Control parent, Menu menu) {

            // If the menu has not yet been created, do so now;
            // otherwise, just update it to ensure the right
            // menu item is selected.
            if (menu == null) {
                menu = new Menu(parent);
                for (int j = 0; j < TemporalDisplay.SELECTED_TIME_MODE_CHOICES
                        .size(); j++) {
                    MenuItem item = new MenuItem(menu, SWT.RADIO, j);
                    item.setText(TemporalDisplay.SELECTED_TIME_MODE_CHOICES
                            .get(j));
                    item.addSelectionListener(listener);
                    if ((temporalDisplay != null)
                            && TemporalDisplay.SELECTED_TIME_MODE_CHOICES
                                    .get(j).equals(
                                            temporalDisplay
                                                    .getSelectedTimeMode())) {
                        item.setSelection(true);
                    }
                }
            } else {
                for (MenuItem item : menu.getItems()) {
                    item.setSelection((temporalDisplay != null)
                            && item.getText().equals(
                                    temporalDisplay.getSelectedTimeMode()));
                }
            }

            // Return the menu.
            return menu;
        }
    }

    // Private Variables

    /**
     * Console presenter.
     */
    private ConsolePresenter presenter;

    /**
     * Reset events command action.
     */
    private Action resetEventsCommandAction;

    /**
     * Reset settings command action.
     */
    private Action resetSettingsCommandAction;

    /**
     * Check hazard conflicts command action.
     */
    private Action checkHazardConflictsAction;

    /**
     * Auto check hazard conflicts command action.
     */
    private Action autoCheckHazardConflictsAction;

    /**
     * Show hazard area command action.
     */
    private Action showHatchedAreaAction;

    /**
     * Flag indicating whether or not the temporal controls should be in the
     * toolbar.
     */
    private boolean temporalControlsInToolBar;

    /**
     * Selected time mode combo action.
     */
    private SelectedTimeModeAction selectedTimeModeAction;

    /**
     * Map of button identifiers to the associated toolbar navigation actions.
     * These are constructed if necessary as contributions to the main user
     * interface and then passed to the view part when it comes into existence.
     */
    private final Map<String, Action> actionsForButtonIdentifiers = Maps
            .newHashMap();

    /**
     * View part listener.
     */
    private final IPartListener2 partListener = new IPartListener2() {

        @Override
        public void partActivated(IWorkbenchPartReference partRef) {

            // No action.
        }

        @Override
        public void partBroughtToTop(IWorkbenchPartReference partRef) {

            // No action.
        }

        @Override
        public void partClosed(IWorkbenchPartReference partRef) {
            if (partRef == getViewPartReference()) {
                statusHandler
                        .debug("ConsoleView.partClosed(): console view part closed.");
                presenter.fireAction(new ConsoleAction(
                        ConsoleAction.ActionType.CLOSE, (String) null));
            }
        }

        @Override
        public void partDeactivated(IWorkbenchPartReference partRef) {

            // No action.
        }

        @Override
        public void partOpened(IWorkbenchPartReference partRef) {

            // No action.
        }

        @Override
        public void partHidden(IWorkbenchPartReference partRef) {

            // No action.
        }

        @Override
        public void partVisible(IWorkbenchPartReference partRef) {
            if (partRef == getViewPartReference()) {
                Display.getCurrent().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        detachIfForcedDetachIsRequired();
                    }
                });
            }
        }

        @Override
        public void partInputChanged(IWorkbenchPartReference partRef) {

            // No action.
        }
    };

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param loadedFromBundle
     *            Flag indicating whether or not the view is being instantiated
     *            as a result of a bundle load.
     */
    public ConsoleView(final boolean loadedFromBundle) {
        super(ConsoleViewPart.ID, ConsoleViewPart.class);

        // Show the view part.
        showViewPart();

        // Execute further manipulation of the view part immediately,
        // or delay such execution until the view part is created if
        // it has not yet been created yet.
        executeOnCreatedViewPart(new Runnable() {
            @Override
            public void run() {

                // If this view is being created as the result of a
                // bundle load, determine whether the view part
                // should be started off as hidden, and if so, hide
                // it.
                boolean potentialDetach = true;
                if (loadedFromBundle) {
                    potentialDetach = false;
                    if (getViewPart().isDocked() == false) {
                        WorkbenchPage page = (WorkbenchPage) getActiveWorkbenchPage(true);
                        IPreferenceStore preferenceStore = HazardServicesActivator
                                .getDefault().getPreferenceStore();
                        preferenceStore
                                .setValue(
                                        page.getPerspective().getId()
                                                + FORCE_DETACH_CONSOLE_WHEN_NEXT_SHOWING_SUFFIX,
                                        true);
                        Rectangle bounds = getViewPart().getShell().getBounds();
                        preferenceStore.setValue(page.getPerspective().getId()
                                + LAST_DETACHED_BOUNDS_X_SUFFIX, bounds.x);
                        preferenceStore.setValue(page.getPerspective().getId()
                                + LAST_DETACHED_BOUNDS_Y_SUFFIX, bounds.y);
                        preferenceStore.setValue(page.getPerspective().getId()
                                + LAST_DETACHED_BOUNDS_WIDTH_SUFFIX,
                                bounds.width);
                        preferenceStore.setValue(page.getPerspective().getId()
                                + LAST_DETACHED_BOUNDS_HEIGHT_SUFFIX,
                                bounds.height);
                        page.attachView(page
                                .findViewReference(ConsoleViewPart.ID));
                    }
                    setViewPartVisible(false);
                }

                // If this view needs to be forcibly detached,
                // detach it and use the previously saved bounds
                // as its shell's boundaries.
                if (potentialDetach) {
                    detachIfForcedDetachIsRequired();
                }
            }
        });

        // Register the part listener for view part events so that
        // the closing of the console view part may be responded
        // to.
        setPartListener(partListener);
    }

    // Public Methods

    /**
     * Initialize the view.
     * 
     * @param presenter
     *            Presenter managing this view.
     * @param selectedTime
     *            Selected time as epoch time in milliseconds.
     * @param currentTime
     *            Current time as epoch time in milliseconds.
     * @param visibleTimeRange
     *            Amount of time visible at once in the time line as an epoch
     *            time range in milliseconds.
     * @param hazardEvents
     * @param currentSettings
     * @param availableSettings
     * @param jsonFilters
     *            JSON string holding a list of dictionaries providing filter
     *            megawidget specifiers.
     * @param activeAlerts
     *            Currently active alerts.
     * @param temporalControlsInToolBar
     *            Flag indicating whether or not temporal display controls are
     *            to be shown in the toolbar. If <code>false</code>, they are
     *            shown in the temporal display composite itself.
     */
    @Override
    public final void initialize(final ConsolePresenter presenter,
            final Date selectedTime, final Date currentTime,
            final long visibleTimeRange, final List<Dict> hazardEvents,
            final Settings currentSettings,
            final List<Settings> availableSettings, final String jsonFilters,
            final ImmutableList<IHazardAlert> activeAlerts,
            final boolean temporalControlsInToolBar) {
        this.presenter = presenter;
        this.temporalControlsInToolBar = temporalControlsInToolBar;
        executeOnCreatedViewPart(new Runnable() {
            @Override
            public void run() {
                getViewPart().initialize(presenter, selectedTime, currentTime,
                        visibleTimeRange, hazardEvents, currentSettings,
                        availableSettings, jsonFilters, activeAlerts,
                        temporalControlsInToolBar);
            }
        });
    }

    /**
     * Accept contributions to the main user interface, which this view
     * controls, of the specified type from the specified contributors.
     * 
     * @param contributors
     *            List of potential contributors.
     * @param type
     *            Type of main UI contributions to accept from the contributors.
     */
    @Override
    public final void acceptContributionsToMainUI(
            List<? extends IMainUiContributor<Action, RCPMainUserInterfaceElement>> contributors,
            final RCPMainUserInterfaceElement type) {

        // Iterate through the contributors, asking each in turn for
        // its contributions and adding them to the list of total con-
        // tributions. When at least one contribution is made and the
        // last contribution specified is not a separator, a separator
        // is placed after the contributions to render them visually
        // distinct from what comes next.
        final List<Action> totalContributions = Lists.newArrayList();
        for (IMainUiContributor<Action, RCPMainUserInterfaceElement> contributor : contributors) {
            List<? extends Action> contributions = contributor
                    .contributeToMainUI(type);
            totalContributions.addAll(contributions);
            if ((contributions.size() > 0)
                    && ((contributions.get(contributions.size() - 1) instanceof SeparatorAction) == false)) {
                totalContributions.add(new SeparatorAction());
            }
        }

        // Do the rest only when there is a view part ready to take
        // the contributions.
        executeOnCreatedViewPart(new Runnable() {
            @Override
            public void run() {

                // Remove all toolbar or menubar items first, since
                // there may be ones left over from a previous invo-
                // cation of this method.
                IActionBars actionBars = getViewPart()
                        .getMainActionBarsManager();
                IContributionManager contributionManager = (type
                        .equals(RCPMainUserInterfaceElement.TOOLBAR) ? actionBars
                        .getToolBarManager() : actionBars.getMenuManager());
                contributionManager.removeAll();

                // Iterate through the list of total contributions,
                // passing each in turn to the manager.
                for (Action contribution : totalContributions) {
                    if (contribution instanceof SeparatorAction) {
                        contributionManager.add(new Separator());
                    } else {
                        contributionManager.add(contribution);
                        if (contribution instanceof IContributionManagerAware) {
                            ((IContributionManagerAware) contribution)
                                    .setContributionManager(contributionManager);
                        }
                    }
                }

                // Update the contribution manager in order to work
                // around what appears to be an Eclipse bug. The
                // latter manifests itself by not drawing the last
                // action added to the toolbar. This update seems to
                // force the toolbar to render itself and all its
                // actions properly.
                contributionManager.update(true);
            }
        });
    }

    /**
     * Contribute to the main UI, if desired. Note that this method may be
     * called multiple times per <code>type</code> to (re)populate the main UI
     * with the specified <code>type</code>; implementations are responsible for
     * cleaning up after contributed items that may exist from a previous call
     * with the same <code>type</code>.
     * 
     * @param type
     *            Type of contribution to be made to the main user interface.
     * @return List of contributions; this may be empty if none are to be made.
     */
    @Override
    public final List<? extends Action> contributeToMainUI(
            RCPMainUserInterfaceElement type) {
        if (type == RCPMainUserInterfaceElement.TOOLBAR) {
            if (temporalControlsInToolBar) {

                // Create the selected time mode action.
                List<Action> list = Lists.newArrayList();
                list.add(new SeparatorAction());
                selectedTimeModeAction = new SelectedTimeModeAction();
                list.add(selectedTimeModeAction);

                // Create the navigation actions for the toolbar.
                list.add(new SeparatorAction());
                for (int j = 0; j < TemporalDisplay.TOOLBAR_BUTTON_IMAGE_FILE_NAMES
                        .size(); j++) {
                    Action action = new NavigationAction(
                            TemporalDisplay.TOOLBAR_BUTTON_IMAGE_FILE_NAMES
                                    .get(j),
                            TemporalDisplay.TOOLBAR_BUTTON_DESCRIPTIONS.get(j));
                    actionsForButtonIdentifiers.put(
                            TemporalDisplay.BUTTON_IMAGE_NAMES.get(j), action);
                    list.add(action);
                }

                // Pass these to the view part when it is ready.
                executeOnCreatedViewPart(new Runnable() {
                    @Override
                    public void run() {
                        getViewPart().setToolBarActions(
                                actionsForButtonIdentifiers,
                                selectedTimeModeAction);
                    }
                });
                return list;
            }
            return Collections.emptyList();
        } else {
            resetEventsCommandAction = new BasicConsoleAction(
                    RESET_EVENTS_COMMAND_MENU_TEXT, null,
                    Action.AS_PUSH_BUTTON, null,
                    ConsoleAction.ActionType.RESET, ConsoleAction.RESET_EVENTS);
            resetSettingsCommandAction = new BasicConsoleAction(
                    RESET_SETTINGS_COMMAND_MENU_TEXT, null,
                    Action.AS_PUSH_BUTTON, null,
                    ConsoleAction.ActionType.RESET,
                    ConsoleAction.RESET_SETTINGS);
            SeparatorAction sep = new SeparatorAction();
            checkHazardConflictsAction = new BasicConsoleAction(
                    CHECK_HAZARD_CONFLICTS_MENU_TEXT, null,
                    Action.AS_PUSH_BUTTON, null,
                    ConsoleAction.ActionType.CHANGE_MODE,
                    ConsoleAction.CHECK_CONFLICTS);

            autoCheckHazardConflictsAction = new BasicConsoleAction(
                    AUTO_CHECK_HAZARD_CONFLICTS_MENU_TEXT, null,
                    Action.AS_CHECK_BOX, null,
                    ConsoleAction.ActionType.CHANGE_MODE,
                    ConsoleAction.AUTO_CHECK_CONFLICTS);

            showHatchedAreaAction = new BasicConsoleAction(
                    SHOW_HATCHED_AREAS_MENU_TEXT, null, Action.AS_CHECK_BOX,
                    null, ConsoleAction.ActionType.CHANGE_MODE,
                    ConsoleAction.SHOW_HATCHED_AREA);

            Action changeSiteAction = new ChangeSiteAction(presenter);
            List<Action> actions = Lists.newArrayList(resetEventsCommandAction,
                    resetSettingsCommandAction, sep,
                    checkHazardConflictsAction, autoCheckHazardConflictsAction,
                    showHatchedAreaAction, sep, changeSiteAction);

            return actions;
        }
    }

    /**
     * Ensure the view is visible.
     */
    @Override
    public final void ensureVisible() {
        executeOnCreatedViewPart(new Runnable() {
            @Override
            public void run() {
                setViewPartVisible(true);
            }
        });
    }

    /**
     * Update the current time.
     */
    @Override
    public final void updateCurrentTime(final Date currentTime) {
        executeOnCreatedViewPart(new Runnable() {
            @Override
            public void run() {
                getViewPart().updateCurrentTime(currentTime);
            }
        });
    }

    /**
     * Update the selected time.
     * 
     */
    @Override
    public final void updateSelectedTime(final Date selectedTime) {
        executeOnCreatedViewPart(new Runnable() {
            @Override
            public void run() {
                getViewPart().updateSelectedTime(selectedTime);
            }
        });
    }

    /**
     * Update the selected time range.
     * 
     * @param jsonRange
     *            JSON string holding a list with two elements: the start time
     *            of the selected time range epoch time in milliseconds, and the
     *            end time of the selected time range epoch time in
     *            milliseconds.
     */
    @Override
    public final void updateSelectedTimeRange(final String jsonRange) {
        executeOnCreatedViewPart(new Runnable() {
            @Override
            public void run() {
                getViewPart().updateSelectedTimeRange(jsonRange);
            }
        });
    }

    /**
     * Update the visible time delta.
     * 
     * @param jsonVisibleTimeDelta
     *            JSON string holding the amount of time visible at once in the
     *            time line as an epoch time range in milliseconds.
     */
    @Override
    public final void updateVisibleTimeDelta(final String jsonVisibleTimeDelta) {
        executeOnCreatedViewPart(new Runnable() {
            @Override
            public void run() {
                getViewPart().updateVisibleTimeDelta(jsonVisibleTimeDelta);
            }
        });
    }

    /**
     * Update the visible time range.
     * 
     * @param jsonEarliestVisibleTime
     *            JSON string holding the earliest visible time in the time line
     *            as an epoch time range in milliseconds.
     * @param jsonLatestVisibleTime
     *            JSON string holding the latest visible time in the time line
     *            as an epoch time range in milliseconds.
     */
    @Override
    public final void updateVisibleTimeRange(
            final String jsonEarliestVisibleTime,
            final String jsonLatestVisibleTime) {
        executeOnCreatedViewPart(new Runnable() {
            @Override
            public void run() {
                getViewPart().updateVisibleTimeRange(jsonEarliestVisibleTime,
                        jsonLatestVisibleTime);
            }
        });
    }

    /**
     * Get the list of the current hazard events.
     * 
     * @return List of the current hazard events.
     */
    @Override
    public final List<Dict> getHazardEvents() {
        ConsoleViewPart viewPart = getViewPart();
        if (viewPart == null) {
            return Collections.emptyList();
        }
        return viewPart.getHazardEvents();
    }

    /**
     * Set the hazard events to those specified.
     * 
     * @param hazardEvents
     * @param currentSettings
     */
    @Override
    public final void setHazardEvents(final List<Dict> hazardEvents,
            final Settings currentSetttings) {
        executeOnCreatedViewPart(new Runnable() {
            @Override
            public void run() {
                getViewPart()
                        .updateConsoleForChanges(hazardEvents, currentSetttings);
            }
        });
    }

    /**
     * Update the specified hazard event.
     * 
     * @param hazardEvent
     *            JSON string holding a dictionary defining an event. The
     *            dictionary must contain an <code>eventID</code> key mapping to
     *            the event identifier as a value. All other mappings specify
     *            properties that are to have their values to those associated
     *            with the properties in the dictionary.
     */
    @Override
    public final void updateHazardEvent(final String hazardEvent) {
        executeOnCreatedViewPart(new Runnable() {
            @Override
            public void run() {
                getViewPart().updateHazardEvent(hazardEvent);
            }
        });
    }

    /**
     * Set the list of currently active alerts.
     * 
     * @param activeAlerts
     *            List of currently active alerts.
     */
    @Override
    public void setActiveAlerts(final ImmutableList<IHazardAlert> activeAlerts) {
        executeOnCreatedViewPart(new Runnable() {
            @Override
            public void run() {
                getViewPart().updateActiveAlerts(activeAlerts);
            }
        });
    }

    /**
     * 
     * @return the current dynamic setting being used.
     */
    @Override
    public final Settings getCurrentSettings() {
        ConsoleViewPart viewPart = getViewPart();
        return viewPart.getCurrentSettings();
    }

    /**
     * Set the settings to those specified.
     * 
     * @param currentSettingsID
     * @param settings
     */
    @Override
    public final void setSettings(final String currentSettingsID,
            final List<Settings> settings) {
        executeOnCreatedViewPart(new Runnable() {
            @Override
            public void run() {
                getViewPart().setSettings(currentSettingsID, settings);
            }
        });
    }

    // Private Methods

    /**
     * Fire a console action event to its listener.
     * 
     * @param actionType
     *            Type of action.
     * @param actionName
     *            Name of action.
     */
    private void fireConsoleAction(ConsoleAction.ActionType actionType,
            String actionName) {
        presenter.fireAction(new ConsoleAction(actionType, actionName));
    }

    /**
     * Detach the view part if a forced detach is required.
     */
    private void detachIfForcedDetachIsRequired() {
        WorkbenchPage page = (WorkbenchPage) getActiveWorkbenchPage(true);
        IPreferenceStore preferenceStore = HazardServicesActivator.getDefault()
                .getPreferenceStore();
        if (preferenceStore.getBoolean(page.getPerspective().getId()
                + FORCE_DETACH_CONSOLE_WHEN_NEXT_SHOWING_SUFFIX)) {
            preferenceStore.setValue(page.getPerspective().getId()
                    + FORCE_DETACH_CONSOLE_WHEN_NEXT_SHOWING_SUFFIX, false);
            if (getViewPart().isDocked()) {
                page.detachView(page.findViewReference(ConsoleViewPart.ID));
                final Rectangle bounds = new Rectangle(
                        preferenceStore.getInt(page.getPerspective().getId()
                                + LAST_DETACHED_BOUNDS_X_SUFFIX),
                        preferenceStore.getInt(page.getPerspective().getId()
                                + LAST_DETACHED_BOUNDS_Y_SUFFIX),
                        preferenceStore.getInt(page.getPerspective().getId()
                                + LAST_DETACHED_BOUNDS_WIDTH_SUFFIX),
                        preferenceStore.getInt(page.getPerspective().getId()
                                + LAST_DETACHED_BOUNDS_HEIGHT_SUFFIX));

                // Set the bounds, but then schedule the setting of
                // the location to occur later, since immediate
                // execution seems to be ignored; instead, the
                // location (via either setBounds() or setLocation())
                // is simply set to be the center of the display if
                // the asyncExec() is not used.
                getViewPart().getShell().setBounds(bounds);
                Display.getCurrent().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        getViewPart().getShell()
                                .setLocation(bounds.x, bounds.y);
                    }
                });
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * gov.noaa.gsd.viz.hazards.console.IConsoleView#updateTitle(java.lang.String
     * )
     */
    @Override
    public void updateTitle(String title) {
        getViewPart().updateSite(title);
    }
}

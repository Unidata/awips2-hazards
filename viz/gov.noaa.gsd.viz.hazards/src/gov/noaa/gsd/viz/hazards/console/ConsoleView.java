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

import gov.noaa.gsd.viz.hazards.actions.ChangeVtecFormatAction;
import gov.noaa.gsd.viz.hazards.display.HazardServicesActivator;
import gov.noaa.gsd.viz.hazards.display.RCPMainUserInterfaceElement;
import gov.noaa.gsd.viz.hazards.display.action.ConsoleAction;
import gov.noaa.gsd.viz.hazards.jsonutilities.Dict;
import gov.noaa.gsd.viz.hazards.product.ReviewAction;
import gov.noaa.gsd.viz.hazards.servicebackup.ChangeSiteAction;
import gov.noaa.gsd.viz.hazards.toolbar.BasicAction;
import gov.noaa.gsd.viz.hazards.toolbar.ComboAction;
import gov.noaa.gsd.viz.hazards.toolbar.IContributionManagerAware;
import gov.noaa.gsd.viz.hazards.toolbar.SeparatorAction;
import gov.noaa.gsd.viz.hazards.ui.ViewPartDelegateView;
import gov.noaa.gsd.viz.mvp.IMainUiContributor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
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
import com.google.common.collect.Range;
import com.raytheon.uf.viz.hazards.sessionmanager.alerts.IHazardAlert;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.ObservedSettings;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Settings;
import com.raytheon.viz.core.mode.CAVEMode;

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
 * Feb 19, 2014    2161    Chris.Golden      Added passing of set of events allowing
 *                                           "until further notice" to the view part.
 * Apr 15, 2014     696    David.Gillingham  Add ChangeVtecFormatAction to menu.
 * Apr 23, 2014    1480    jsanchez          Added a Correct menu to the console.
 * Nov 18, 2014    4124    Chris.Golden      Adapted to new time manager.
 * Dec 05, 2014    4124    Chris.Golden      Changed to use ObservedSettings.
 * Dec 13, 2014    4959    Dan Schaffer      Spatial Display cleanup and other bug fixes
 * Feb 06, 2015    2331    Chris.Golden      Removed bogus debug message, and also
 *                                           changed to use time range boundaries for
 *                                           the events.
 * May 05, 2015 6898       Chris.Cody        Pan & Scale Visible and Selected Time
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
@SuppressWarnings("restriction")
public class ConsoleView extends ViewPartDelegateView<ConsoleViewPart>
        implements IConsoleView<Action, RCPMainUserInterfaceElement> {

    // Public Static Constants

    /**
     * Reset events command menu item text.
     */
    public static final String RESET_EVENTS_COMMAND_MENU_TEXT = "Reset Events";

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
     * preference key {@link #FORCE_DETACH_CONSOLE_WHEN_NEXT_SHOWING_SUFFIX} for
     * the same perspective is true.
     */
    private static final String LAST_DETACHED_BOUNDS_X_SUFFIX = ".lastDetachedBoundsX";

    /**
     * Suffix for the preferences key holding the Y value of the bounds of the
     * last saved detached view; this is only relevant if the value for the
     * preference key {@link #FORCE_DETACH_CONSOLE_WHEN_NEXT_SHOWING_SUFFIX} for
     * the same perspective is true.
     */
    private static final String LAST_DETACHED_BOUNDS_Y_SUFFIX = ".lastDetachedBoundsY";

    /**
     * Suffix for the preferences key holding the width value of the bounds of
     * the last saved detached view; this is only relevant if the value for the
     * preference key {@link #FORCE_DETACH_CONSOLE_WHEN_NEXT_SHOWING_SUFFIX} for
     * the same perspective is true.
     */
    private static final String LAST_DETACHED_BOUNDS_WIDTH_SUFFIX = ".lastDetachedBoundsWidth";

    /**
     * Suffix for the preferences key holding the height value of the bounds of
     * the last saved detached view; this is only relevant if the value for the
     * preference key {@link #FORCE_DETACH_CONSOLE_WHEN_NEXT_SHOWING_SUFFIX} for
     * the same perspective is true.
     */
    private static final String LAST_DETACHED_BOUNDS_HEIGHT_SUFFIX = ".lastDetachedBoundsHeight";

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
         *            File name of the icon to be displayed, or <code>null
         *            </code> if no icon is to be associated with this action.
         * @param style
         *            Style; one of the {@link IAction} style constants.
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

        @Override
        public void setTemporalDisplay(TemporalDisplay temporalDisplay) {
            this.temporalDisplay = temporalDisplay;
        }

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

        @Override
        public void setTemporalDisplay(TemporalDisplay temporalDisplay) {
            this.temporalDisplay = temporalDisplay;
        }

        // Protected Methods

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
    private final Map<String, Action> actionsForButtonIdentifiers = new HashMap<>();

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
                presenter.publish(new ConsoleAction(
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

    @Override
    public final void initialize(final ConsolePresenter presenter,
            final Date selectedTime, final Date currentTime,
            final long visibleTimeRange, final List<Dict> hazardEvents,
            final Map<String, Range<Long>> startTimeBoundariesForEventIds,
            final Map<String, Range<Long>> endTimeBoundariesForEventIds,
            final ObservedSettings currentSettings,
            final List<Settings> availableSettings, final String jsonFilters,
            final ImmutableList<IHazardAlert> activeAlerts,
            final Set<String> eventIdentifiersAllowingUntilFurtherNotice,
            final boolean temporalControlsInToolBar) {
        this.presenter = presenter;
        this.temporalControlsInToolBar = temporalControlsInToolBar;
        executeOnCreatedViewPart(new Runnable() {
            @Override
            public void run() {
                getViewPart().initialize(presenter, selectedTime, currentTime,
                        visibleTimeRange, hazardEvents,
                        startTimeBoundariesForEventIds,
                        endTimeBoundariesForEventIds, currentSettings,
                        availableSettings, jsonFilters, activeAlerts,
                        eventIdentifiersAllowingUntilFurtherNotice,
                        temporalControlsInToolBar);
            }
        });
    }

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
        final List<Action> totalContributions = new ArrayList<>();
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

    @Override
    public final List<? extends Action> contributeToMainUI(
            RCPMainUserInterfaceElement type) {
        if (type == RCPMainUserInterfaceElement.TOOLBAR) {
            if (temporalControlsInToolBar) {

                // Create the selected time mode action.
                List<Action> list = new ArrayList<>();
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
            showHatchedAreaAction.setChecked(true);

            Action reviewAction = new ReviewAction(presenter);
            List<Action> actions = Lists.newArrayList(resetEventsCommandAction,
                    sep, checkHazardConflictsAction,
                    autoCheckHazardConflictsAction, showHatchedAreaAction, sep,
                    reviewAction);
            if (CAVEMode.PRACTICE.equals(CAVEMode.getMode())) {
                Action changeVtecFormat = new ChangeVtecFormatAction(presenter
                        .getSessionManager().getProductManager());
                actions.add(changeVtecFormat);
                actions.add(sep);
            }

            Action changeSiteAction = new ChangeSiteAction(presenter);
            actions.add(changeSiteAction);

            return actions;
        }
    }

    @Override
    public final void ensureVisible() {
        executeOnCreatedViewPart(new Runnable() {
            @Override
            public void run() {
                setViewPartVisible(true);
            }
        });
    }

    @Override
    public final void updateCurrentTime(final Date currentTime) {
        executeOnCreatedViewPart(new Runnable() {
            @Override
            public void run() {
                getViewPart().updateCurrentTime(currentTime);
            }
        });
    }

    @Override
    public final void updateSelectedTimeRange(final Date start, final Date end) {
        executeOnCreatedViewPart(new Runnable() {
            @Override
            public void run() {
                getViewPart().updateSelectedTimeRange(start, end);
            }
        });
    }

    @Override
    public final void updateVisibleTimeDelta(final long visibleTimeDelta) {
        executeOnCreatedViewPart(new Runnable() {
            @Override
            public void run() {
                getViewPart().updateVisibleTimeDelta(visibleTimeDelta);
            }
        });
    }

    @Override
    public final void updateEventTimeRangeBoundaries(final Set<String> eventIds) {
        executeOnCreatedViewPart(new Runnable() {
            @Override
            public void run() {
                getViewPart().updateEventTimeRangeBoundaries(eventIds);
            }
        });
    }

    @Override
    public final void updateVisibleTimeRange(final long earliestVisibleTime,
            final long latestVisibleTime) {
        executeOnCreatedViewPart(new Runnable() {
            @Override
            public void run() {
                getViewPart().updateVisibleTimeRange(earliestVisibleTime,
                        latestVisibleTime);
            }
        });
    }

    @Override
    public final List<Dict> getHazardEvents() {
        ConsoleViewPart viewPart = getViewPart();
        if (viewPart == null) {
            return Collections.emptyList();
        }
        return viewPart.getHazardEvents();
    }

    @Override
    public final void setHazardEvents(final List<Dict> hazardEvents,
            final ObservedSettings currentSetttings) {
        executeOnCreatedViewPart(new Runnable() {
            @Override
            public void run() {
                getViewPart().updateConsoleForChanges(hazardEvents,
                        currentSetttings);
            }
        });
    }

    @Override
    public final void updateHazardEvent(final String hazardEvent) {
        executeOnCreatedViewPart(new Runnable() {
            @Override
            public void run() {
                getViewPart().updateHazardEvent(hazardEvent);
            }
        });
    }

    @Override
    public void setActiveAlerts(final ImmutableList<IHazardAlert> activeAlerts) {
        executeOnCreatedViewPart(new Runnable() {
            @Override
            public void run() {
                getViewPart().updateActiveAlerts(activeAlerts);
            }
        });
    }

    @Override
    public final ObservedSettings getCurrentSettings() {
        ConsoleViewPart viewPart = getViewPart();
        return viewPart.getCurrentSettings();
    }

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

    @Override
    public void updateTitle(String title) {
        getViewPart().updateSite(title);
    }

    // Protected Methods

    /**
     * Respond to an attempt to execute some action via
     * {@link #executeOnCreatedViewPart(Runnable)} upon a view part when the
     * view part is not in existence and no attempt has been made to create it.
     * This should never occur, so an exception is thrown.
     * 
     * @param job
     *            Action for which execution was attempted.
     * @throws IllegalStateException
     *             Whenever this method is invoked.
     */
    @Override
    protected void actionExecutionAttemptedUponNonexistentViewPart(Runnable job) {
        throw new IllegalStateException(
                "view part creation not attempted before invocation of action: "
                        + job);
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
        presenter.publish(new ConsoleAction(actionType, actionName));
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
}

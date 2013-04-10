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

import gov.noaa.gsd.viz.hazards.display.RCPMainUserInterfaceElement;
import gov.noaa.gsd.viz.hazards.display.action.ConsoleAction;
import gov.noaa.gsd.viz.hazards.jsonutilities.Dict;
import gov.noaa.gsd.viz.mvp.IView;

import java.util.List;

import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

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
 * 
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class ConsoleView implements
        IConsoleView<IActionBars, RCPMainUserInterfaceElement> {

    // Private Static Constants

    /**
     * Logging mechanism.
     */
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(ConsoleView.class);

    // Private Variables

    /**
     * Console presenter.
     */
    private ConsolePresenter presenter = null;

    /**
     * Console view part.
     */
    private ConsoleViewPart viewPart = null;

    /**
     * View part listener.
     */
    private final IPartListener partListener = new IPartListener() {
        @Override
        public void partActivated(IWorkbenchPart part) {

            // No action.
        }

        @Override
        public void partBroughtToTop(IWorkbenchPart part) {

            // No action.
        }

        @Override
        public void partClosed(IWorkbenchPart part) {
            if ((viewPart != null) && (part == viewPart)) {
                statusHandler
                        .debug("ConsoleView.partClosed(): console view part closed.");
                presenter.fireAction(new ConsoleAction("Close", (String) null));
            }
        }

        @Override
        public void partDeactivated(IWorkbenchPart part) {

            // No action.
        }

        @Override
        public void partOpened(IWorkbenchPart part) {

            // No action.
        }
    };

    // Public Constructors

    /**
     * Construct a standard instance.
     */
    public ConsoleView() {

        // Get the active window.
        IWorkbenchWindow window = PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow();
        if (window == null) {
            statusHandler
                    .error("ConsoleView.<init>: PlatformUI.getWorkbench()."
                            + "getActiveWorkbenchWindow() returned null.");
            return;
        }

        // Get the active page.
        IWorkbenchPage page = window.getActivePage();
        if (page == null) {
            statusHandler
                    .error("ConsoleView.<init>: PlatformUI.getWorkbench()."
                            + "getActiveWorkbenchWindow().getActivePage() returned null.");
            return;
        }

        // Register the part listener for view part events.
        page.addPartListener(partListener);

        // Create and activate the console view part.
        try {
            page.showView(ConsoleViewPart.ID, null,
                    IWorkbenchPage.VIEW_ACTIVATE);
        } catch (PartInitException e) {
            statusHandler.error(
                    "ConsoleView.<init>: Unable to show console view part.", e);
            return;
        }
        IViewPart viewPart = page.findView(ConsoleViewPart.ID);
        if (viewPart == null) {
            statusHandler
                    .error("ConsoleView.<init>: Unable to find console view part.");
            return;
        } else if ((viewPart instanceof ConsoleViewPart) == false) {
            statusHandler
                    .error("ConsoleView.<init>: Could not find view part that was "
                            + "of type ConsoleViewPart.");
            return;
        }
        this.viewPart = (ConsoleViewPart) viewPart;
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
     * @param jsonHazardEvents
     *            JSON string holding a list of hazard events in dictionary
     *            form.
     * @param jsonSettings
     *            JSON string holding a dictionary providing settings.
     * @param jsonFilters
     *            JSON string holding a list of dictionaries providing filter
     *            megawidget specifiers.
     * @param temporalControlsInToolBar
     *            Flag indicating whether or not temporal display controls are
     *            to be shown in the toolbar. If <code>false</code>, they are
     *            shown in the temporal display composite itself.
     */
    @Override
    public final void initialize(ConsolePresenter presenter, long selectedTime,
            long currentTime, long visibleTimeRange, String jsonHazardEvents,
            String jsonSettings, String jsonFilters,
            boolean temporalControlsInToolBar) {
        this.presenter = presenter;
        viewPart.initialize(presenter, selectedTime, currentTime,
                visibleTimeRange, jsonHazardEvents, jsonSettings, jsonFilters,
                temporalControlsInToolBar);
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
            List<? extends IView<IActionBars, RCPMainUserInterfaceElement>> contributors,
            RCPMainUserInterfaceElement type) {

        // Remove all toolbar or menubar items first, since there may
        // be ones left over from a previous invocation of this method.
        IActionBars actionBars = viewPart.getMainActionBarsManager();
        IContributionManager contributionManager = (type
                .equals(RCPMainUserInterfaceElement.TOOLBAR) ? actionBars
                .getToolBarManager() : actionBars.getMenuManager());
        contributionManager.removeAll();

        // Iterate through the contributors, asking each in turn
        // for its contributions. When a contribution is made, a
        // separator is placed after the contributions to render
        // them visually distinct from what comes next.
        for (IView<IActionBars, RCPMainUserInterfaceElement> contributor : contributors) {
            if (contributor.contributeToMainUI(actionBars, type)) {
                contributionManager.add(new Separator());
            }
        }

        // Update the contribution manager in order to work around
        // what appears to be an Eclipse bug. The latter manifests
        // itself by not drawing the last action added to the
        // toolbar. This update seems to force the toolbar to render
        // itself and all its actions properly.
        contributionManager.update(true);
    }

    /**
     * Prepare for disposal.
     */
    @Override
    public final void dispose() {

        // Stop listening for workbench page part changes.
        // Exceptions are ignored because it is possible that
        // the active page will not be around at this point.
        try {
            PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                    .getActivePage().removePartListener(partListener);
        } catch (Exception e) {

            // No action.
        }

        // Remove the console view from the display. Exceptions
        // are caught in case CAVE is closing, since the work-
        // bench, window, page, or view part may not be around
        // at this point.
        if (viewPart != null) {
            try {
                IWorkbenchPage page = PlatformUI.getWorkbench()
                        .getActiveWorkbenchWindow().getActivePage();
                if (page.isPartVisible(viewPart)) {
                    PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                            .getActivePage().hideView(viewPart);
                    statusHandler
                            .debug("ConsoleView.dispose(): Closing console view part.");
                }
            } catch (Exception e) {
                statusHandler
                        .info("ConsoleView.dispose(): Could not close view: "
                                + e);
            }
        }

        // Forget the member data.
        presenter = null;
        viewPart = null;
    }

    /**
     * Contribute to the main UI, if desired. Note that this method may be
     * called multiple times per <code>type
     * </code> to (re)populate the main UI with the specified <code>type</code>;
     * implementations are responsible for cleaning up after contributed items
     * that may exist from a previous call with the same <code>type</code>.
     * 
     * @param mainUI
     *            Main user interface to which to contribute.
     * @param type
     *            Type of contribution to be made to the main user interface.
     * @return True if items were contributed, otherwise false.
     */
    @Override
    public final boolean contributeToMainUI(IActionBars mainUI,
            RCPMainUserInterfaceElement type) {
        return viewPart.contributeToMainUI(mainUI, type);
    }

    /**
     * Update the current time.
     * 
     * @param jsonCurrentTime
     *            JSON string holding the current time.
     */
    @Override
    public final void updateCurrentTime(String jsonCurrentTime) {
        viewPart.updateCurrentTime(jsonCurrentTime);
    }

    /**
     * Update the selected time.
     * 
     * @param jsonSelectedTime
     *            JSON string holding the selected time.
     */
    @Override
    public final void updateSelectedTime(String jsonSelectedTime) {
        viewPart.updateSelectedTime(jsonSelectedTime);
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
    public final void updateSelectedTimeRange(String jsonRange) {
        viewPart.updateSelectedTimeRange(jsonRange);
    }

    /**
     * Update the visible time delta.
     * 
     * @param jsonVisibleTimeDelta
     *            JSON string holding the amount of time visible at once in the
     *            time line as an epoch time range in milliseconds.
     */
    @Override
    public final void updateVisibleTimeDelta(String jsonVisibleTimeDelta) {
        viewPart.updateVisibleTimeDelta(jsonVisibleTimeDelta);
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
    public final void updateVisibleTimeRange(String jsonEarliestVisibleTime,
            String jsonLatestVisibleTime) {
        viewPart.updateVisibleTimeRange(jsonEarliestVisibleTime,
                jsonLatestVisibleTime);
    }

    /**
     * Get the list of the current hazard events.
     * 
     * @return List of the current hazard events.
     */
    @Override
    public final List<Dict> getHazardEvents() {
        return viewPart.getHazardEvents();
    }

    /**
     * Set the hazard events to those specified.
     * 
     * @param hazardEvents
     *            JSON string holding an array of dictionaries, each of the
     *            latter holding an event as a set of key-value pairs.
     */
    @Override
    public final void setHazardEvents(String hazardEvents) {
        viewPart.updateHazardEvents(hazardEvents);
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
    public final void updateHazardEvent(String hazardEvent) {
        viewPart.updateHazardEvent(hazardEvent);
    }

    /**
     * Get the dictionary defining the current dynamic setting being used.
     * 
     * @return Dictionary defining the current dynamic setting being used.
     */
    @Override
    public final Dict getDynamicSetting() {
        return viewPart.getDynamicSetting();
    }

    /**
     * Set the settings to those specified.
     * 
     * @param jsonSettings
     *            JSON string holding a dictionary an entry for the list of
     *            settings, and another entry for the current setting
     *            identifier.
     */
    @Override
    public final void setSettings(String jsonSettings) {
        viewPart.setSettings(jsonSettings);
    }
}

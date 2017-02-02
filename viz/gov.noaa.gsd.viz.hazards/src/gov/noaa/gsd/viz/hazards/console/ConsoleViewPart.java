/**
 * This software was developed and / or modified by the National Oceanic and
 * Atmospheric Administration (NOAA), Earth System Research Laboratory (ESRL),
 * Global Systems Division (GSD), Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.console;

import gov.noaa.gsd.common.utilities.IRunnableAsynchronousScheduler;
import gov.noaa.gsd.common.utilities.Sort;
import gov.noaa.gsd.common.utilities.TimeResolution;
import gov.noaa.gsd.viz.hazards.alerts.CountdownTimer;
import gov.noaa.gsd.viz.hazards.console.ConsolePresenter.TimeRangeType;
import gov.noaa.gsd.viz.hazards.toolbar.ComboAction;
import gov.noaa.gsd.viz.hazards.ui.DockTrackingViewPart;
import gov.noaa.gsd.viz.mvp.widgets.ICommandInvoker;
import gov.noaa.gsd.viz.mvp.widgets.IListStateChanger;
import gov.noaa.gsd.viz.mvp.widgets.IStateChanger;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;
import com.raytheon.viz.ui.dialogs.ModeListener;

/**
 * Console view part, used to display the main control widgets for Hazard
 * Services within CAVE via an Eclipse view part. Does most of the heavy lifting
 * for the console view.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Chris.Golden      Initial induction into repo
 * June 4, 2013            Chris.Golden      Added support for changing background
 *                                           and foreground colors in order to stay
 *                                           in synch with CAVE mode.
 * Jul 12, 2013    585     Chris.Golden      Changed to support loading from bundle.
 * Jul 18, 2013   1264     Chris.Golden      Added support for drawing lines and
 *                                           points.
 * Aug 22, 2013   1936     Chris.Golden      Added console countdown timers.
 * Nov 04, 2013   2182     daniel.s.schaffer Started refactoring
 * Nov 29, 2013   2380     daniel.s.schaffer Continued consolidation of constants
 * Feb 19, 2014   2161     Chris.Golden      Added passing of set of events allowing
 *                                           "until further notice" to the temporal
 *                                           display.
 * Nov 18, 2014   4124     Chris.Golden      Adapted to new time manager.
 * Dec 05, 2014   4124     Chris.Golden      Changed to use ObservedSettings.
 * Feb 09, 2015   6370     Chris.Golden      Fixed problem with header menu in
 *                                           Console not always showing columns that
 *                                           are available in the current settings
 *                                           if the latter has been changed.
 * Feb 09, 2015   2331     Chris.Golden      Changed to use time range boundaries
 *                                           for the events.
 * May 05, 2015   6898     Chris.Cody        Pan & Scale Visible and Selected Time
 * Aug 15, 2016  18376     Chris.Golden      Added code to make garbage collection of
 *                                           the session manager more likely.
 * Oct 19, 2016  21873     Chris.Golden      Added time resolution tracking tied to
 *                                           settings.
 * Feb 01, 2017   15556    Chris.Golden      Complete refactoring to address MVP
 *                                           design concerns, untangle spaghetti, and
 *                                           add history list viewing.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class ConsoleViewPart extends DockTrackingViewPart implements
        IConsoleTree {

    // Public Static Constants

    /**
     * Identifier of the view.
     */
    public static final String ID = "gov.noaa.gsd.viz.hazards.console.view";

    // Private Variables

    /**
     * Name of the currently selected setting.
     */
    private String selectedSettingName;

    /**
     * Name of the current site being used.
     */
    private String currentSite;

    /**
     * Action bars manager.
     */
    private IActionBars actionBars;

    /**
     * Body of the console.
     */
    private ConsoleBody body;

    /**
     * Mode listener, used to respond to CAVE mode changes.
     */
    private ModeListener modeListener;

    /**
     * View that is using this as its user interface manifestation.
     * 
     * @deprecated Should no longer be needed once the
     *             {@link #getContextMenuItems(String, Date)} is not being used.
     */
    @Deprecated
    private ConsoleView view;

    // Public Methods

    /**
     * Initialize the view.
     * 
     * @param view
     *            View that is using this view part as its user interface
     *            manifestation.
     * @param selectedTime
     *            Selected time
     * @param currentTime
     *            Current time
     * @param visibleTimeRange
     *            Amount of time visible at once in the time line as an epoch
     *            time range in milliseconds.
     * @param timeResolution
     *            Overall time resolution.
     * @param filterSpecifiers
     *            List of maps, each map holding a megawidget specifier that is
     *            to act as a filter.
     * @param currentSite
     *            Current site identifier.
     * @param temporalControlsInToolBar
     *            Flag indicating whether or not temporal display controls are
     *            to be shown in the toolbar. If false, they are shown in the
     *            temporal display composite itself.
     */
    public void initialize(ConsoleView view, Date selectedTime,
            Date currentTime, long visibleTimeRange,
            TimeResolution timeResolution,
            ImmutableList<Map<String, Object>> filterSpecifiers,
            String currentSite, boolean temporalControlsInToolBar) {
        this.view = view;
        this.currentSite = currentSite;
        body.initialize(selectedTime, currentTime, visibleTimeRange,
                timeResolution, filterSpecifiers, temporalControlsInToolBar);
    }

    @Override
    public void createPartControl(Composite parent) {
        super.createPartControl(parent);

        /*
         * Get the action bars and create the body of this view part.
         */
        actionBars = getViewSite().getActionBars();
        body = new ConsoleBody(this);
        body.createDisplayComposite(parent);

        /*
         * Create a CAVE mode listener, which will set the foreground and
         * background colors appropriately according to the CAVE mode whenever a
         * paint event occurs. This is done with the grandparent of this parent,
         * because this ensures that the part's toolbar gets colored
         * appropriately as well.
         */
        modeListener = new ModeListener(parent.getParent().getParent());
    }

    @Override
    public void setFocus() {
        body.setFocus();
    }

    @Override
    public void dispose() {
        if (modeListener != null) {
            modeListener.dispose();
        }
        super.dispose();
    }

    /**
     * Get the action bars manager.
     * 
     * @return Action bars manager.
     */
    public IActionBars getMainActionBarsManager() {
        return actionBars;
    }

    /**
     * Set the map of toolbar widget identifiers to their actions and the
     * selected time mode action. These are constructed elsewhere and provided
     * to this object if appropriate.
     * 
     * @param map
     *            Map of toolbar widget identifiers to their actions.
     * @param selectedTimeModeAction
     *            Selected time mode action.
     */
    public void setToolBarActions(final Map<String, Action> map,
            ComboAction selectedTimeModeAction) {
        body.setToolBarActions(map, selectedTimeModeAction);
    }

    /**
     * Set the name of the current settings to that specified.
     * 
     * @param settingsName
     *            Name of the current settings.
     */
    public void setSettingsName(String settingsName) {
        selectedSettingName = settingsName;

        /*
         * Show as the text the currently selected setting name, if any.
         */
        if (selectedSettingName != null) {
            setTitleText();
        }
    }

    @Override
    public ICommandInvoker<Sort> getSortInvoker() {
        return (body != null ? body.getSortInvoker() : null);
    }

    @Override
    public IStateChanger<TimeRangeType, Range<Long>> getTimeRangeChanger() {
        return (body != null ? body.getTimeRangeChanger() : null);
    }

    @Override
    public IStateChanger<String, ConsoleColumns> getColumnsChanger() {
        return (body != null ? body.getColumnsChanger() : null);
    }

    @Override
    public IStateChanger<String, Object> getColumnFiltersChanger() {
        return (body != null ? body.getColumnFiltersChanger() : null);
    }

    @Override
    public IListStateChanger<String, TabularEntity> getTreeContentsChanger() {
        return (body != null ? body.getTreeContentsChanger() : null);
    }

    @Override
    public void setCurrentTime(Date currentTime) {
        body.setCurrentTime(currentTime);
    }

    @Override
    public void setTimeResolution(TimeResolution timeResolution,
            Date currentTime) {
        body.setTimeResolution(timeResolution, currentTime);
    }

    @Override
    public void setSorts(ImmutableList<Sort> sorts) {
        body.setSorts(sorts);
    }

    @Override
    public void setActiveCountdownTimers(
            ImmutableMap<String, CountdownTimer> countdownTimersForEventIdentifiers) {
        body.setActiveCountdownTimers(countdownTimersForEventIdentifiers);
    }

    // Package-Private Methods

    /**
     * Handle the site identifier changing.
     * 
     * @param siteIdentifier
     *            New site identifier.
     */
    void siteChanged(String siteIdentifier) {
        this.currentSite = siteIdentifier;
        setTitleText();
    }

    /**
     * Get the context menu items appropriate to the specified event.
     * 
     * @param identifier
     *            Identifier of the tabular entity that was chosen with the
     *            context menu invocation, or <code>null</code> if none was
     *            chosen.
     * @param persistedTimestamp
     *            Timestamp indicating when the entity was persisted; may be
     *            <code>null</code>.
     * @return Actions for the menu items to be shown.
     * @deprecated See
     *             {@link ConsolePresenter#getContextMenuItems(String, Date, IRunnableAsynchronousScheduler)}
     *             .
     */
    @Deprecated
    List<IContributionItem> getContextMenuItems(String identifier,
            Date persistedTimestamp) {
        return view.getContextMenuItems(identifier, persistedTimestamp);
    }

    // Private Methods

    /**
     * Set the title bar text for the view.
     */
    private void setTitleText() {
        String titlePrefix = getPartName();
        int colonIndex = titlePrefix.indexOf(": ");
        if (colonIndex == -1) {
            titlePrefix = titlePrefix + ": ";
        } else {
            titlePrefix = titlePrefix.substring(0, colonIndex + 2);
        }
        setPartName(titlePrefix + selectedSettingName + " - " + currentSite);
    }
}

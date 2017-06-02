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

import gov.noaa.gsd.common.utilities.TimeResolution;
import gov.noaa.gsd.viz.hazards.console.ConsolePresenter.Command;
import gov.noaa.gsd.viz.hazards.console.ConsolePresenter.Toggle;
import gov.noaa.gsd.viz.hazards.console.ConsolePresenter.VtecFormatMode;
import gov.noaa.gsd.viz.mvp.IMainUiContributor;
import gov.noaa.gsd.viz.mvp.IView;
import gov.noaa.gsd.viz.mvp.widgets.ICommandInvocationHandler;
import gov.noaa.gsd.viz.mvp.widgets.IStateChangeHandler;
import gov.noaa.gsd.viz.mvp.widgets.IStateChanger;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;

/**
 * Console view, an interface describing the methods that a class must implement
 * in order to act as a view for a console presenter.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Chris.Golden      Initial induction into repo
 * Jul 12, 2013     585    Chris.Golden      Changed to support loading from bundle.
 * Aug 22, 2013    1936    Chris.Golden      Added console countdown timers.
 * Feb 19, 2014    2161    Chris.Golden      Added passing of set of events allowing
 *                                           "until further notice" to the view.
 * Nov 18, 2014    4124    Chris.Golden      Adapted to new time manager.
 * Dec 05, 2014    4124    Chris.Golden      Changed to use ObservedSettings.
 * Feb 10, 2015    2331    Chris.Golden      Changed to use time range boundaries
 *                                           for the events.
 * May 05, 2015    6898    Chris.Cody        Pan & Scale Visible and Selected Time
 * Oct 19, 2016   21873    Chris.Golden      Added time resolution tracking tied to
 *                                           settings.
 * Feb 01, 2017   15556    Chris.Golden      Complete refactoring to address MVP
 *                                           design concerns, untangle spaghetti, and
 *                                           add history list viewing.
 * Jun 26, 2017   19207    Chris.Golden      Removed obsolete product viewer selection
 *                                           code.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public interface IConsoleView<C, E extends Enum<E>> extends IView<C, E>,
        IConsoleTree {

    // Public Methods

    /**
     * Initialize the view.
     * 
     * @param presenter
     *            Console presenter.
     * @param selectedTime
     *            Selected time as epoch time in milliseconds.
     * @param currentTime
     *            Current time as epoch time in milliseconds.
     * @param visibleTimeRange
     *            Amount of time visible at once in the time line as an epoch
     *            time range in milliseconds.
     * @param timeResolution
     *            Overall time resolution.
     * @param filterSpecifiers
     *            List of maps, each one holding a specifier for a megawidget
     *            representing a filter.
     * @param currentSite
     *            Current site identifier.
     * @param backupSites
     *            Backup sites, used to populate the change sites menu.
     * @param temporalControlsInToolBar
     *            Flag indicating whether or not temporal display controls are
     *            to be shown in the toolbar. If false, they are shown in the
     *            temporal display composite itself.
     */
    public void initialize(ConsolePresenter presenter, Date selectedTime,
            Date currentTime, long visibleTimeRange,
            TimeResolution timeResolution,
            ImmutableList<Map<String, Object>> filterSpecifiers,
            String currentSite, ImmutableList<String> backupSites,
            boolean temporalControlsInToolBar);

    /**
     * Accept contributions to the main user interface, which this view
     * controls, of the specified type from the specified contributors.
     * 
     * @param contributors
     *            List of potential contributors.
     * @param type
     *            Type of main UI contributions to accept from the contributors.
     */
    public void acceptContributionsToMainUI(
            List<? extends IMainUiContributor<C, E>> contributors, E type);

    /**
     * Set the command invocation handler.
     * 
     * @param commandInvocationHandler
     *            Command invocation handler.
     */
    public void setCommandInvocationHandler(
            ICommandInvocationHandler<Command> commandInvocationHandler);

    /**
     * Set the toggle state change handler.
     * 
     * @param toggleStateChangeHandler
     *            Toggle state change handler.
     */
    public void setToggleChangeHandler(
            IStateChangeHandler<Toggle, Boolean> toggleStateChangeHandler);

    /**
     * Set the VTEC mode state change handler.
     * 
     * @param vtecModeStateChangeHandler
     *            VTEC mode state change handler.
     */
    public void setVtecModeChangeHandler(
            IStateChangeHandler<String, VtecFormatMode> vtecModeStateChangeHandler);

    /**
     * Get the site state changer.
     * 
     * @return Site state changer.
     */
    public IStateChanger<String, String> getSiteChanger();

    /**
     * Ensure the view is visible.
     */
    public void ensureVisible();

    /**
     * Set the name of the current settings to that specified.
     * 
     * @param settingsName
     *            Name of the current settings.
     */
    public void setSettingsName(String settingsName);
}

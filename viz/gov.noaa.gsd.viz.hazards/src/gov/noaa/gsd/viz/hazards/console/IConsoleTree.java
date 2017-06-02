/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Evaluation & Decision Support Branch (EDS)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.console;

import gov.noaa.gsd.common.utilities.Sort;
import gov.noaa.gsd.common.utilities.TimeResolution;
import gov.noaa.gsd.viz.hazards.alerts.CountdownTimer;
import gov.noaa.gsd.viz.hazards.console.ConsolePresenter.TimeRangeType;
import gov.noaa.gsd.viz.hazards.contextmenu.ContextMenuHelper.IContributionItemUpdater;
import gov.noaa.gsd.viz.mvp.widgets.ICommandInvoker;
import gov.noaa.gsd.viz.mvp.widgets.IListStateChanger;
import gov.noaa.gsd.viz.mvp.widgets.IStateChanger;

import java.util.Date;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;

/**
 * Description: Interface describing the methods that must be implemented by a
 * class intended to act in some context as a console tree.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Dec 14, 2016   15556    Chris.Golden Initial creation.
 * Jun 30, 2017   19223    Chris.Golden Added ability to change the text and
 *                                      enabled state of a row menu's menu item
 *                                      after it is displayed.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public interface IConsoleTree extends IContributionItemUpdater {

    /**
     * Get the sort invoker.
     * 
     * @return Sort invoker.
     */
    ICommandInvoker<Sort> getSortInvoker();

    /**
     * Get the time range state changer.
     * 
     * @return Time range state changer.
     */
    IStateChanger<TimeRangeType, Range<Long>> getTimeRangeChanger();

    /**
     * Get the columns state changer.
     * 
     * @return Columns state changer.
     */
    IStateChanger<String, ConsoleColumns> getColumnsChanger();

    /**
     * Get the column-based filters state changer.
     * 
     * @return Column-based filters state changer.
     */
    IStateChanger<String, Object> getColumnFiltersChanger();

    /**
     * Get the tree contents state changer.
     * 
     * @return Tree contents state changer.
     */
    IListStateChanger<String, TabularEntity> getTreeContentsChanger();

    /**
     * Set the current time.
     * 
     * @param currentTime
     *            New current time.
     */
    public void setCurrentTime(Date currentTime);

    /**
     * Set the time resolution.
     * 
     * @param timeResolution
     *            Time resolution.
     * @param currentTime
     *            Current time.
     */
    public void setTimeResolution(TimeResolution timeResolution,
            Date currentTime);

    /**
     * Set the sorts currently in use.
     * 
     * @param sorts
     *            Sorts to be used.
     */
    public void setSorts(ImmutableList<Sort> sorts);

    /**
     * Set the currently active countdown timers.
     * 
     * @param countdownTimersForEventIdentifiers
     *            Map of event identifiers to their currently active countdown
     *            timers.
     */
    public void setActiveCountdownTimers(
            ImmutableMap<String, CountdownTimer> countdownTimersForEventIdentifiers);
}

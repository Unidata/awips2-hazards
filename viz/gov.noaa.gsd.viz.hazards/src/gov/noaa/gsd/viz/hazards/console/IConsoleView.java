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

import gov.noaa.gsd.viz.hazards.jsonutilities.Dict;
import gov.noaa.gsd.viz.mvp.IMainUiContributor;
import gov.noaa.gsd.viz.mvp.IView;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;
import com.raytheon.uf.viz.hazards.sessionmanager.alerts.IHazardAlert;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.ObservedSettings;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Settings;

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
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public interface IConsoleView<C, E extends Enum<E>> extends IView<C, E> {

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
     *            Hazard events, each in dictionary form.
     * @param startTimeBoundariesForEventIds
     *            Map of event identifiers to their start time range boundaries.
     * @param endTimeBoundariesForEventIds
     *            Map of event identifiers to their end time range boundaries.
     * @param currentSettings
     *            Currently selected settings.
     * @param availableSettings
     *            All available settings.
     * @param jsonFilters
     *            JSON string holding a list of dictionaries providing filter
     *            megawidget specifiers.
     * @param activeAlerts
     *            Currently active alerts.
     * @param eventIdentifiersAllowingUntilFurtherNotice
     *            Set of the hazard event identifiers that at any given moment
     *            allow the toggling of their "until further notice" mode. The
     *            set is unmodifiable; attempts to modify it will result in an
     *            {@link UnsupportedOperationException}. Note that this set is
     *            kept up-to-date, and thus will always contain only those
     *            events that can have their "until further notice" mode toggled
     *            at the instant at which it is checked.
     * @param temporalControlsInToolBar
     *            Flag indicating whether or not temporal display controls are
     *            to be shown in the toolbar. If false, they are shown in the
     *            temporal display composite itself.
     */
    public void initialize(ConsolePresenter presenter, Date selectedTime,
            Date currentTime, long visibleTimeRange, List<Dict> hazardEvents,
            Map<String, Range<Long>> startTimeBoundariesForEventIds,
            Map<String, Range<Long>> endTimeBoundariesForEventIds,
            ObservedSettings currentSettings, List<Settings> availableSettings,
            String jsonFilters, ImmutableList<IHazardAlert> activeAlerts,
            Set<String> eventIdentifiersAllowingUntilFurtherNotice,
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
     * Ensure the view is visible.
     */
    public void ensureVisible();

    /**
     * Update the current time.
     * 
     * @param currentTime
     *            New current time.
     */
    public void updateCurrentTime(Date currentTime);

    /**
     * Update the selected time range.
     * 
     * @param start
     *            Start time of the selected time range, or <code>null</code> if
     *            there is no selected time range.
     * @param end
     *            End time of the selected time range, or <code>null</code> if
     *            there is no selected time range.
     */
    public void updateSelectedTimeRange(Date start, Date end);

    /**
     * Update the visible time delta.
     * 
     * @param jsonVisibleTimeDelta
     *            JSON string holding the amount of time visible at once in the
     *            time line as an epoch time range in milliseconds.
     */
    public void updateVisibleTimeDelta(String jsonVisibleTimeDelta);

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
    public void updateVisibleTimeRange(String jsonEarliestVisibleTime,
            String jsonLatestVisibleTime);

    /**
     * Update the time range boundaries for the events.
     * 
     * @param eventIds
     *            Identifiers of the events that have had their time range
     *            boundaries changed.
     */
    public void updateEventTimeRangeBoundaries(Set<String> eventIds);

    /**
     * Get the list of the current hazard events.
     * 
     * @return List of the current hazard events.
     */
    public List<Dict> getHazardEvents();

    /**
     * Set the hazard events to those specified.
     * 
     * @param eventsAsDicts
     *            List of maps, each representing a hazard event.
     * @param currentSettings
     *            Currently selected settings.
     */
    public void setHazardEvents(List<Dict> eventsAsDicts,
            ObservedSettings currentSettings);

    /**
     * Update the specified hazard event.
     * 
     * @param hazardEventJSON
     *            JSON string holding a dictionary defining an event. The
     *            dictionary must contain an <code>eventID</code> key mapping to
     *            the event identifier as a value. All other mappings specify
     *            properties that are to have their values to those associated
     *            with the properties in the dictionary.
     */
    public void updateHazardEvent(String hazardEventJSON);

    /**
     * Set the list of currently active alerts.
     * 
     * @param activeAlerts
     *            List of currently active alerts.
     */
    public void setActiveAlerts(ImmutableList<IHazardAlert> activeAlerts);

    /**
     * Get the current settings.
     * 
     * @return Current settings.
     */
    public ObservedSettings getCurrentSettings();

    /**
     * Set the settings to those specified.
     * 
     * @param currentSettingsID
     *            Identifier of the new current settings.
     * @param settings
     *            List of settings.
     */
    public void setSettings(String currentSettingsID, List<Settings> settings);

    /**
     * Update the title of the implementing GUI.
     * 
     * @param title
     *            New title.
     */
    public void updateTitle(String title);
}

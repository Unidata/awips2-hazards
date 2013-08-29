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
import gov.noaa.gsd.viz.mvp.IView;

import java.util.List;

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
 * Jul 12, 2013    585     Chris.Golden      Changed to support loading from bundle.
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
    public void initialize(ConsolePresenter presenter, long selectedTime,
            long currentTime, long visibleTimeRange, String jsonHazardEvents,
            String jsonSettings, String jsonFilters,
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
            List<? extends IView<C, E>> contributors, E type);

    /**
     * Ensure the view is visible.
     */
    public void ensureVisible();

    /**
     * Update the current time.
     * 
     * @param jsonCurrentTime
     *            JSON string holding the current time.
     */
    public void updateCurrentTime(String jsonCurrentTime);

    /**
     * Update the selected time.
     * 
     * @param jsonSelectedTime
     *            JSON string holding the selected time.
     */
    public void updateSelectedTime(String jsonSelectedTime);

    /**
     * Update the selected time range.
     * 
     * @param jsonRange
     *            JSON string holding a list with two elements: the start time
     *            of the selected time range epoch time in milliseconds, and the
     *            end time of the selected time range epoch time in
     *            milliseconds.
     */
    public void updateSelectedTimeRange(String jsonRange);

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
     * Get the list of the current hazard events.
     * 
     * @return List of the current hazard events.
     */
    public List<Dict> getHazardEvents();

    /**
     * Set the hazard events to those specified.
     * 
     * @param hazardEventsJSON
     *            JSON string holding an array of dictionaries, each of the
     *            latter holding an event as a set of key-value pairs.
     */
    public void setHazardEvents(String hazardEventsJSON);

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
     * Get the dictionary defining the current dynamic setting being used.
     * 
     * @return Dictionary defining the current dynamic setting being used.
     */
    public Dict getDynamicSetting();

    /**
     * Set the settings to those specified.
     * 
     * @param jsonSettings
     *            JSON string holding a dictionary an entry for the list of
     *            settings, and another entry for the current setting
     *            identifier.
     */
    public void setSettings(String jsonSettings);

    /**
     * Updates the title of the implementing gui
     * 
     * @param title
     */
    public void updateTitle(String title);
}

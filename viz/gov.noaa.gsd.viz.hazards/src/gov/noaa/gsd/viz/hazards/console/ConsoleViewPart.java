/**
 * This software was developed and / or modified by the National Oceanic and
 * Atmospheric Administration (NOAA), Earth System Research Laboratory (ESRL),
 * Global Systems Division (GSD), Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.console;

import gov.noaa.gsd.viz.hazards.jsonutilities.Dict;
import gov.noaa.gsd.viz.hazards.toolbar.ComboAction;
import gov.noaa.gsd.viz.hazards.ui.DockTrackingViewPart;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;

import com.google.common.collect.ImmutableList;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationLevel;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.core.localization.LocalizationManager;
import com.raytheon.uf.viz.hazards.sessionmanager.alerts.IHazardAlert;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Settings;
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
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class ConsoleViewPart extends DockTrackingViewPart {

    // Public Static Constants

    /**
     * Identifier of the view.
     */
    public static final String ID = "gov.noaa.gsd.viz.hazards.console.view";

    // Private Static Constants

    /**
     * Logging mechanism.
     */
    @SuppressWarnings("unused")
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(ConsoleViewPart.class);

    // Private Variables

    /**
     * Name of the currently selected setting.
     */
    private String selectedSettingName;

    /**
     * Name of the current site being used.
     */
    private String currentSite = null;

    /**
     * Action bars manager.
     */
    private IActionBars actionBars = null;

    /**
     * Temporal display.
     */
    private TemporalDisplay temporalDisplay = null;

    /**
     * Mode listener, used to respond to CAVE mode changes.
     */
    private ModeListener modeListener = null;

    // Public Methods

    /**
     * Initialize the view.
     * 
     * @param presenter
     *            Presenter managing this view part.
     * @param selectedTime
     *            Selected time
     * @param currentTime
     *            Current time
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
            Settings currentSettings, List<Settings> availableSettings,
            String jsonFilters, ImmutableList<IHazardAlert> activeAlerts,
            Set<String> eventIdentifiersAllowingUntilFurtherNotice,
            boolean temporalControlsInToolBar) {
        this.currentSite = LocalizationManager
                .getContextName(LocalizationLevel.SITE);
        String currentSettingsID = currentSettings.getSettingsID();
        setSettings(currentSettingsID, availableSettings);
        temporalDisplay.initialize(presenter, selectedTime, currentTime,
                visibleTimeRange, hazardEvents, currentSettings, jsonFilters,
                activeAlerts, eventIdentifiersAllowingUntilFurtherNotice,
                temporalControlsInToolBar);
    }

    @Override
    public void createPartControl(Composite parent) {
        super.createPartControl(parent);

        // Get the action bars and create the body of this view part.
        actionBars = getViewSite().getActionBars();
        temporalDisplay = new TemporalDisplay();
        temporalDisplay.createDisplayComposite(parent);

        // Create a CAVE mode listener, which will set the foreground
        // and background colors appropriately according to the CAVE
        // mode whenever a paint event occurs. This is done with the
        // grandparent of this parent, because this ensures that the
        // part's toolbar gets colored appropriately as well.
        modeListener = new ModeListener(parent.getParent().getParent());
    }

    @Override
    public void setFocus() {
        temporalDisplay.setFocus();
    }

    @Override
    public void dispose() {
        modeListener.dispose();
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
        temporalDisplay.setToolBarActions(map, selectedTimeModeAction);
    }

    /**
     * Get the minimum visible time.
     * 
     * @return Minimum visible time.
     */
    public long getMinimumVisibleTime() {
        return temporalDisplay.getMinimumVisibleTime();
    }

    /**
     * Get the maximum visible time.
     * 
     * @return Maximum visible time.
     */
    public long getMaximumVisibleTime() {
        return temporalDisplay.getMaximumVisibleTime();
    }

    /**
     * Update the visible time delta.
     * 
     * @param newVisibleTimeRange
     *            JSON string holding the amount of time visible at once in the
     *            time line as an epoch time range in milliseconds.
     */
    public void updateVisibleTimeDelta(String newVisibleTimeDelta) {
        temporalDisplay.updateVisibleTimeDelta(newVisibleTimeDelta);
    }

    /**
     * Update the visible time range.
     * 
     * @param newEarliestVisibleTime
     *            JSON string holding the earliest visible time in the time line
     *            as an epoch time range in milliseconds.
     * @param newLatestVisibleTime
     *            JSON string holding the latest visible time in the time line
     *            as an epoch time range in milliseconds.
     */
    public void updateVisibleTimeRange(String newEarliestVisibleTime,
            String newLatestVisibleTime) {
        temporalDisplay.updateVisibleTimeRange(newEarliestVisibleTime,
                newLatestVisibleTime);
    }

    /**
     * Update the current time.
     * 
     * @param currentTime
     *            JSON string holding the current time.
     */
    public void updateCurrentTime(Date currentTime) {
        temporalDisplay.updateCurrentTime(currentTime);
    }

    /**
     * Update the selected time.
     * 
     * @param selectedTime
     *            JSON string holding the selected time.
     */
    public void updateSelectedTime(Date selectedTime) {
        temporalDisplay.updateSelectedTime(selectedTime);
    }

    /**
     * Update the selected time range.
     * 
     * @param range
     *            JSON string holding a list with two elements: the start time
     *            of the selected time range epoch time in milliseconds, and the
     *            end time of the selected time range epoch time in
     *            milliseconds.
     */
    public void updateSelectedTimeRange(String range) {
        temporalDisplay.updateSelectedTimeRange(range);
    }

    /**
     * Get the list of the current hazard events.
     * 
     * @return List of the current hazard events.
     */
    public List<Dict> getHazardEvents() {
        return temporalDisplay.getEvents();
    }

    /**
     * Add the specified hazard events.
     * 
     * @param hazardEvents
     * @param currentSetttings
     */
    public void updateConsoleForChanges(List<Dict> hazardEvents,
            Settings currentSettings) {
        temporalDisplay.clearEvents();
        temporalDisplay.updateHazardEvents(hazardEvents);
        temporalDisplay.updateSettings(currentSettings);
        temporalDisplay.updateConsole(null);
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
    public void updateHazardEvent(String hazardEvent) {
        temporalDisplay.updateEvent(hazardEvent);
    }

    /**
     * Update the list of currently active alerts.
     * 
     * @param activeAlerts
     *            List of currently active alerts.
     */
    public void updateActiveAlerts(ImmutableList<IHazardAlert> activeAlerts) {
        temporalDisplay.updateActiveAlerts(activeAlerts);
    }

    /**
     * @return the settings currently in use.
     */
    public Settings getCurrentSettings() {
        return temporalDisplay.getCurrentSettings();
    }

    /**
     * Set the settings to those specified.
     * 
     * @param currentSettingsID
     * @param availableSettings
     */
    public void setSettings(String currentSettingsID,
            List<Settings> availableSettings) {

        if (currentSettingsID == null) {
            return;
        }

        // Get the names and identifiers of the settings.
        for (Settings settings : availableSettings) {

            if (currentSettingsID.equals(settings.getSettingsID())) {
                selectedSettingName = settings.getDisplayName();
                break;
            }

        }

        // Show as the text the currently selected setting
        // name, if any.
        if (selectedSettingName != null) {
            setTitleText();
        }
    }

    public void updateSite(String site) {
        this.currentSite = site;
        setTitleText();
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
/**
 * This software was developed and / or modified by the National Oceanic and
 * Atmospheric Administration (NOAA), Earth System Research Laboratory (ESRL),
 * Global Systems Division (GSD), Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.console;

import gov.noaa.gsd.viz.hazards.display.RCPMainUserInterfaceElement;
import gov.noaa.gsd.viz.hazards.display.action.ConsoleAction;
import gov.noaa.gsd.viz.hazards.jsonutilities.Dict;
import gov.noaa.gsd.viz.hazards.toolbar.BasicAction;
import gov.noaa.gsd.viz.hazards.utilities.Utilities;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.ViewPart;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
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
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class ConsoleViewPart extends ViewPart {

    // Public Static Constants

    /**
     * Action detail indicating settings are to be reset.
     */
    public static final String SETTINGS = "Settings";

    /**
     * Action detail indicating events are to be reset.
     */
    public static final String EVENTS = "Events";

    /**
     * Identifier of the view.
     */
    public static final String ID = "gov.noaa.gsd.viz.hazards.console.view";

    /**
     * Key into a setting dictionary where the displayable name of the setting
     * is found as a value.
     */
    private static final String DISPLAY_NAME = "displayName";

    // Private Static Constants

    /**
     * Logging mechanism.
     */
    @SuppressWarnings("unused")
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(ConsoleViewPart.class);

    // Private Classes

    /**
     * Standard action.
     */
    private class BasicConsoleAction extends BasicAction {

        // Private Variables

        /**
         * Action type.
         */
        private final String actionType;

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
                String toolTipText, String actionType, String actionName) {
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

    // Private Variables

    /**
     * Presenter managing this view part.
     */
    private ConsolePresenter presenter = null;

    /**
     * Reset events command action.
     */
    private Action resetEventsCommandAction = null;

    /**
     * Reset settings command action.
     */
    private Action resetSettingsCommandAction = null;

    /**
     * Name of the currently selected setting.
     */
    private String selectedSettingName = null;

    /**
     * Action bars manager.
     */
    private IActionBars actionBars = null;

    /**
     * Temporal display.
     */
    private TemporalDisplay temporalDisplay = null;

    // Public Methods

    /**
     * Initialize the view.
     * 
     * @param presenter
     *            Presenter managing this view part.
     * @param selectedTime
     *            Selected time as epoch time in milliseconds.
     * @param currentTime
     *            Current time as epoch time in milliseconds.
     * @param visibleTimeRange
     *            Amount of time visible at once in the time line as an epoch
     *            time range in milliseconds.
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
            long currentTime, long visibleTimeRange, String hazardEvents,
            String jsonSettings, String jsonFilters,
            boolean temporalControlsInToolBar) {
        this.presenter = presenter;
        setSettings(jsonSettings);
        temporalDisplay.initialize(presenter, selectedTime, currentTime,
                visibleTimeRange, hazardEvents, jsonFilters,
                temporalControlsInToolBar);
    }

    /**
     * Create the part control for the view.
     * 
     * @param parent
     *            Parent composite.
     */
    @Override
    public void createPartControl(Composite parent) {
        actionBars = getViewSite().getActionBars();
        temporalDisplay = new TemporalDisplay();
        temporalDisplay.createDisplayComposite(parent);

        // Create a CAVE mode listener, which will set the foreground
        // and background colors appropriately according to the CAVE
        // mode whenever a paint event occurs. This is done with the
        // grandparent of this parent, because this ensures that the
        // part's toolbar gets colored appropriately as well.
        new ModeListener(parent.getParent().getParent());
    }

    /**
     * Contribute to the main UI, if desired. Note that this method may be
     * called multiple times per <code>type</code> to (re)populate the main UI
     * with the specified <code>type</code>; implementations are responsible for
     * cleaning up after contributed items that may exist from a previous call
     * with the same <code>type</code>.
     * 
     * @param mainUI
     *            Main user interface to which to contribute.
     * @param type
     *            Type of contribution to be made to the main user interface.
     * @return True if items were contributed, otherwise false.
     */
    public final boolean contributeToMainUI(IActionBars mainUI,
            RCPMainUserInterfaceElement type) {
        if (type == RCPMainUserInterfaceElement.TOOLBAR) {
            return temporalDisplay.contributeToMainUI(mainUI, type);
        } else {
            resetEventsCommandAction = new BasicConsoleAction("Reset Events",
                    null, Action.AS_PUSH_BUTTON, null, "Reset", EVENTS);
            resetSettingsCommandAction = new BasicConsoleAction(
                    "Reset Settings", null, Action.AS_PUSH_BUTTON, null,
                    "Reset", SETTINGS);
            IMenuManager menuManager = mainUI.getMenuManager();
            menuManager.add(resetEventsCommandAction);
            menuManager.add(resetSettingsCommandAction);
            return true;
        }
    }

    /**
     * Set the focus to this view.
     */
    @Override
    public void setFocus() {
        temporalDisplay.setFocus();
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
    public void updateCurrentTime(String currentTime) {
        temporalDisplay.updateCurrentTime(currentTime);
    }

    /**
     * Update the selected time.
     * 
     * @param selectedTime
     *            JSON string holding the selected time.
     */
    public void updateSelectedTime(String selectedTime) {
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
     *            JSON string holding an array of dictionaries, each of the
     *            latter holding an event as a set of key-value pairs.
     */
    public void updateHazardEvents(String hazardEvents) {
        temporalDisplay.clearEvents();
        temporalDisplay.setComponentData(hazardEvents);
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
     * Get the dictionary defining the dynamic setting currently in use.
     * 
     * @return Dictionary defining the dynamic setting currently in use.
     */
    public Dict getDynamicSetting() {
        return temporalDisplay.getDynamicSetting();
    }

    /**
     * Set the settings to those specified.
     * 
     * @param jsonSettings
     *            JSON string holding a dictionary with an entry for the list of
     *            settings, and another entry for the current setting
     *            identifier.
     */
    public void setSettings(String jsonSettings) {

        // Get the dictionary from the JSON, and the list of settings
        // from that.
        Dict settingDict = Dict.getInstance(jsonSettings);
        ArrayList<Dict> settings = settingDict
                .getDynamicallyTypedValue(Utilities.SETTINGS_LIST);
        if ((settings == null) || (settings.size() < 1)) {
            return;
        }

        // Get the identifier of the currently selected setting.
        String currentIdentifier = settingDict
                .getDynamicallyTypedValue(Utilities.SETTINGS_CURRENT_IDENTIFIER);

        // Get the names and identifiers of the settings.
        for (int j = 0; j < settings.size(); j++) {
            if (currentIdentifier.equals(settings.get(j).get(
                    Utilities.SETTINGS_LIST_IDENTIFIER))) {
                selectedSettingName = settings.get(j).getDynamicallyTypedValue(
                        DISPLAY_NAME);
                break;
            }
        }

        // Show as the text the currently selected setting
        // name, if any.
        if (selectedSettingName != null) {
            setTitleText();
        }
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
        setPartName(titlePrefix + selectedSettingName);
    }

    /**
     * Fire a console action event to its listener.
     * 
     * @param actionType
     *            Type of action.
     * @param actionName
     *            Name of action.
     */
    private void fireConsoleAction(String actionType, String actionName) {
        presenter.fireAction(new ConsoleAction(actionType, actionName));
    }
}

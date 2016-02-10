/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.display.action;

import java.util.Date;

import com.raytheon.uf.viz.hazards.sessionmanager.originator.IOriginator;

/**
 * Represents an action originating from the H.S. console.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Chris.Golden      Initial induction into repo
 * Sep 14, 2015  3473      Chris.Cody  Implement Hazard Services Import/Export through Central Registry server.
 * </pre>
 * 
 * @author Chris.Golden
 */
public class ConsoleAction {

    public enum ActionType {
        RESET, CHANGE_MODE, VISIBLE_TIME_RANGE_CHANGED,

        SELECTED_TIME_RANGE_CHANGED, CHECK_BOX, SELECTED_EVENTS_CHANGED,

        EVENT_TIME_RANGE_CHANGED, SITE_CHANGED, CLOSE,

        RUN_AUTOMATED_TESTS, RUN_PRODUCT_GENERATION_TESTS,

        EVENT_END_TIME_UNTIL_FURTHER_NOTICE_CHANGED,

        SITE_DATA_OPERATION
    }

    public static final String CHECK_CONFLICTS = "CheckConflicts";

    public static final String AUTO_CHECK_CONFLICTS = "AutoCheckConflicts";

    public static final String SHOW_HATCHED_AREA = "ShowHazardHatchedArea";

    public static final String RESET_EVENTS = "Events";

    public static final String EXPORT_SITE_CONFIG = "ExportSiteConfig";

    public static final String IMPORT_SITE_CONFIG = "ImportSiteConfig";

    // Applies in instances where a setting or a tool
    // are chosen.
    private ActionType actionType;

    private String selectedTimeAsString = null;

    private String auxString1 = null;

    private String auxString2 = null;

    private String[] auxStringArray = null;

    private boolean checked = false;

    private IOriginator originator;

    /**
     * Construct an instance of a ConsoleAction
     * 
     * @param actionType
     * 
     *            The action type.
     */
    public ConsoleAction(ActionType actionType) {
        this.actionType = actionType;
    }

    /**
     * Construct an instance of a ConsoleAction
     * 
     * @param actionType
     * 
     *            The action type.
     * 
     * @param idOrNewTime
     * 
     *            The ID of the action, or the new time.
     */
    public ConsoleAction(ActionType actionType, String idOrNewTime) {
        this.actionType = actionType;
        this.selectedTimeAsString = idOrNewTime;
    }

    /**
     * Construct an instance of a ConsoleAction
     * 
     * @param actionType
     * 
     *            The action type.
     * 
     * @param id
     * 
     *            The ID of the action.
     * 
     * @param checked
     * 
     *            Boolean indicating whether or not the item is checked.
     */
    public ConsoleAction(ActionType actionType, String id, boolean checked) {
        this.actionType = actionType;
        this.selectedTimeAsString = id;
        this.checked = checked;
    }

    /**
     * Construct an instance of a ConsoleAction
     * 
     * @param actionType
     * 
     *            The action type.
     * 
     * @param selectedEventIds
     * 
     *            Array of selected event IDs.
     */
    public ConsoleAction(ActionType actionType, String[] selectedEventIds) {
        this.actionType = actionType;
        this.auxStringArray = selectedEventIds;
    }

    /**
     * Construct an instance of a ConsoleAction
     * 
     * @param actionType
     * 
     *            The action type.
     * 
     * @param startTime
     * 
     *            Start time.
     * 
     * @param endTime
     * 
     *            End time.
     * 
     */
    public ConsoleAction(ActionType actionType, String startTime, String endTime) {
        this.actionType = actionType;
        this.auxString1 = startTime;
        this.auxString2 = endTime;
    }

    /**
     * Construct an instance of a ConsoleAction
     * 
     * @param actionType
     * 
     *            The action type.
     * 
     * @param actionId
     * 
     *            The ID of the action
     * 
     * @param legendName
     * 
     *            The name of the legend, as it is to appear on the CAVE
     *            display, or the start time.
     * 
     * @param mapsDbTableName
     * 
     *            The name of the maps DB table to retrieve geometries from, or
     *            the end time.
     */
    public ConsoleAction(ActionType actionType, String actionId,
            String legendNameOrStartTime, String mapsDbTableNameOrEndTime) {
        this(actionType, actionId);
        this.auxString1 = legendNameOrStartTime;
        this.auxString2 = mapsDbTableNameOrEndTime;
    }

    /**
     * 
     * @param name
     */
    public void setId(String id) {
        this.selectedTimeAsString = id;
    }

    /**
     * 
     * 
     */
    public String getId() {
        return selectedTimeAsString;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public boolean getChecked() {
        return checked;
    }

    public void setNewTime(String newTime) {
        selectedTimeAsString = newTime;
    }

    public Date getNewTime() {
        return new Date(Long.parseLong(selectedTimeAsString));
    }

    public void setStartTime(String startTime) {
        auxString1 = startTime;
    }

    public String getStartTime() {
        return auxString1;
    }

    public void setEndTime(String endTime) {
        auxString2 = endTime;
    }

    public String getEndTime() {
        return auxString2;
    }

    /**
     * 
     * @param actionType
     */
    public void setActionType(ActionType actionType) {
        this.actionType = actionType;
    }

    /**
     * 
     * 
     */
    public ActionType getActionType() {
        return actionType;
    }

    public void setLegendName(String legendName) {
        this.auxString1 = legendName;
    }

    public String getLegendName() {
        return auxString1;
    }

    public void setMapsDbTableName(String mapsDbTableName) {
        this.auxString2 = mapsDbTableName;
    }

    public String getMapsDbTableName() {
        return auxString2;
    }

    public void setSelectedEventIDs(String[] selectedEventIDs) {
        this.auxStringArray = selectedEventIDs;
    }

    public String[] getSelectedEventIDs() {
        return auxStringArray;
    }

    /**
     * @param originator
     *            the originator to set
     */
    public void setOriginator(IOriginator originator) {
        this.originator = originator;
    }

    /**
     * @return the originator
     */
    public IOriginator getOriginator() {
        return originator;
    }
}
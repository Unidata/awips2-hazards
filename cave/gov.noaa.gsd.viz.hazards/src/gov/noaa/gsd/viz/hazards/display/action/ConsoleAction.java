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

import gov.noaa.gsd.viz.mvp.IAction;

/**
 * Represents an action originating from the H.S. console.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Chris.Golden      Initial induction into repo
 * </pre>
 * 
 * @author Chris.Golden
 */
public class ConsoleAction implements IAction {

    // Applies in instances where a setting or a tool
    // are chosen.
    private String action = null;

    private String id = null;

    private String auxString1 = null;

    private String auxString2 = null;

    private String[] auxStringArray = null;

    private boolean checked = false;

    // Hide the default constructor.
    // Force the actionType and actionName
    // to be defined when the object is created.
    @SuppressWarnings("unused")
    private ConsoleAction() {
        // TODO Auto-generated constructor stub
    }

    /**
     * Construct an instance of a ConsoleAction
     * 
     * @param actionType
     * 
     *            The action type.
     */
    public ConsoleAction(String actionType) {
        action = actionType;
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
    public ConsoleAction(String actionType, String idOrNewTime) {
        action = actionType;
        this.id = idOrNewTime;
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
    public ConsoleAction(String actionType, String id, boolean checked) {
        action = actionType;
        this.id = id;
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
    public ConsoleAction(String actionType, String[] selectedEventIds) {
        action = actionType;
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
    public ConsoleAction(String actionType, String startTime, String endTime) {
        action = actionType;
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
    public ConsoleAction(String actionType, String actionId,
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
        this.id = id;
    }

    /**
     * 
     * 
     */
    public String getId() {
        return id;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public boolean getChecked() {
        return checked;
    }

    public void setNewTime(String newTime) {
        id = newTime;
    }

    public String getNewTime() {
        return id;
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
     * @param action
     */
    public void setAction(String action) {
        this.action = action;
    }

    /**
     * 
     * 
     */
    public String getAction() {
        return action;
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
}
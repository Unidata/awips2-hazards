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

import gov.noaa.gsd.viz.hazards.jsonutilities.Dict;
import gov.noaa.gsd.viz.mvp.IAction;

import com.raytheon.uf.viz.core.drawables.IDescriptor.FramesInfo;

/**
 * Action class "fired" from the SpatialDisplay when the state of the Spatial
 * Display changes, usually in response to a user's action.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Bryon.Lawrence      Initial induction into repo
 * Aug 21, 2013 1921       daniel.s.schaffer@noaa.gov  Call recommender framework directly
 * 
 * </pre>
 * 
 * @author Bryon.Lawrence
 */
public class SpatialDisplayAction implements IAction {
    private FramesInfo framesInfo = null;

    private String actionType = null;

    private String actionIdentifier = null;

    private String legendName = null;

    private String mapsDbTableName = null;

    private String modifyEventJSON = null;

    private String[] selectedEventIDs = null;

    private String contextMenuLabel = null;

    private String toolName = null;

    private Dict toolParameters = null;

    private String eventID = null;

    private long timeInMilliSeconds = 0;

    private double dragToLatitude = 0;

    private double dragToLongitude = 0;

    public SpatialDisplayAction(String actionType) {
        this.actionType = actionType;
    }

    public SpatialDisplayAction(String actionType, long timeInMilliseconds) {
        this(actionType);
        this.timeInMilliSeconds = timeInMilliseconds;
    }

    public SpatialDisplayAction(String actionType, FramesInfo framesInfo) {
        this(actionType);
        this.framesInfo = framesInfo;
    }

    public SpatialDisplayAction(String actionType, double dragToLatitude,
            double dragToLongitude) {
        this(actionType);
        this.dragToLatitude = dragToLatitude;
        this.dragToLongitude = dragToLongitude;
    }

    public SpatialDisplayAction(String actionType, String actionIdentifier) {
        this.actionType = actionType;
        this.actionIdentifier = actionIdentifier;
    }

    public SpatialDisplayAction(String actionType, String toolName,
            Dict toolParameters) {
        this.actionType = actionType;
        this.toolName = toolName;
        this.toolParameters = toolParameters;
    }

    public SpatialDisplayAction(String actionType, String actionIdentifier,
            String legendName, String mapsDbTableName) {
        this.actionType = actionType;
        this.actionIdentifier = actionIdentifier;
        this.legendName = legendName;
        this.mapsDbTableName = mapsDbTableName;
    }

    public SpatialDisplayAction(String actionType, String[] selectedEventIds) {
        this(actionType);
        this.selectedEventIDs = selectedEventIds;
    }

    public SpatialDisplayAction(String actionType, int menuPosition,
            String contextMenulabel) {

        this(actionType);
        this.contextMenuLabel = contextMenulabel;
        // Not doing anything menu position right now.
    }

    public void setTimeInMilliSeconds(long timeInMilliSeconds) {
        this.timeInMilliSeconds = timeInMilliSeconds;
    }

    public long getTimeInMilliSeconds() {
        return timeInMilliSeconds;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getActionType() {
        return actionType;
    }

    public String getActionIdentifier() {
        return actionIdentifier;
    }

    public String getLegendName() {
        return legendName;
    }

    public String getMapsDbTableName() {
        return mapsDbTableName;
    }

    public void setModifyEventJSON(String modifyEventJSON) {
        this.modifyEventJSON = modifyEventJSON;
    }

    public String getModifyEventJSON() {
        return modifyEventJSON;
    }

    public void setDragToLatitude(double dragToLatitude) {
        this.dragToLatitude = dragToLatitude;
    }

    public double getDragToLatitude() {
        return dragToLatitude;
    }

    public void setDragToLongitude(double dragToLongitude) {
        this.dragToLongitude = dragToLongitude;
    }

    public double getDragToLongitude() {
        return dragToLongitude;
    }

    public void setSelectedEventIDs(String selectedEventIDs[]) {
        this.selectedEventIDs = selectedEventIDs;
    }

    public String[] getSelectedEventIDs() {
        return selectedEventIDs;
    }

    public void setContextMenuLabel(String contextMenuLabel) {
        this.contextMenuLabel = contextMenuLabel;
    }

    public String getContextMenuLabel() {
        return contextMenuLabel;
    }

    /**
     * @return the framesInfo
     */
    public FramesInfo getFramesInfo() {
        return framesInfo;
    }

    /**
     * @return the toolName
     */
    public String getToolName() {
        return toolName;
    }

    public Dict getToolParameters() {
        return toolParameters;
    }

    public void setToolParameters(Dict toolParameters) {
        this.toolParameters = toolParameters;
    }

    /**
     * @param eventID
     *            the eventID to set
     */
    public void setEventID(String eventID) {
        this.eventID = eventID;
    }

    /**
     * @return the eventID
     */
    public String getEventID() {
        return eventID;
    }

}

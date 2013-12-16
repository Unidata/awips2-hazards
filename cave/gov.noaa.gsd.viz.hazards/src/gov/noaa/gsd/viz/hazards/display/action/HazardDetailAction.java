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
 * This action is "fired" from the Hazard Information Dialog when its state
 * changes. Registered observers receive this object and act on it.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Bryon.Lawrence      Initial induction into repo
 * 
 * </pre>
 * 
 * @author Bryon.Lawrence
 */
public class HazardDetailAction implements IAction {
    private String action;

    private String jsonText;

    /*
     * Need to distinguish between Hazard Detail events which are user-initiated
     * versus those which are not. This helps determine whether or not to tag an
     * event as modified by the user.
     */
    private Boolean isUserInitiated = true;

    public HazardDetailAction(String action) {
        // TODO Auto-generated constructor stub
        this.action = action;
    }

    public HazardDetailAction(String action, String jsonText) {
        // TODO Auto-generated constructor stub
        this.action = action;
        this.jsonText = jsonText;
    }

    public HazardDetailAction(String action, String jsonText,
            Boolean isUserInitiated) {
        this(action, jsonText);
        this.isUserInitiated = isUserInitiated;

    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getAction() {
        return action;
    }

    public void setJSONText(String jsonText) {
        this.jsonText = jsonText;
    }

    public String getJSONText() {
        return jsonText;
    }

    /**
     * @return the isUserInitiated
     */
    public Boolean getIsUserInitiated() {
        return isUserInitiated;
    }

    /**
     * @param isUserInitiated the isUserInitiated to set
     */
    public void setIsUserInitiated(Boolean isUserInitiated) {
        this.isUserInitiated = isUserInitiated;
    }

}

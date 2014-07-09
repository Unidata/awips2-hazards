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

import java.io.Serializable;
import java.util.Map;

import com.raytheon.uf.viz.hazards.sessionmanager.originator.IOriginator;

/**
 * This action is "fired" from the Hazard Information Dialog when its state
 * changes. Registered observers receive this object and act on it.
 * 
 * TODO: This (and many other XXXAction classes within this same package) should
 * no longer be needed once refactoring is completed allowing presenters to
 * directly manipulate the model, instead of they or views having to send
 * messages to do so.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Bryon.Lawrence      Initial induction into repo
 * Apr 23, 2014 1480       jsanchez     Added ActionType CORRECT.
 * </pre>
 * 
 * @author Bryon.Lawrence
 */
@Deprecated
public class HazardDetailAction {

    public enum ActionType {
        PREVIEW, PROPOSE, ISSUE, DISMISS, UPDATE_TIME_RANGE, UPDATE_EVENT_TYPE, UPDATE_EVENT_METADATA, REVIEW
    }

    private ActionType actionType;

    private Map<String, Serializable> parameters;

    /*
     * Need to distinguish between Hazard Detail events which are user-initiated
     * versus those which are not. This helps determine whether or not to tag an
     * event as modified by the user.
     */
    private Boolean isUserInitiated = true;

    private IOriginator originator;

    public HazardDetailAction(ActionType actionType) {
        this.actionType = actionType;
    }

    public HazardDetailAction(ActionType actionType,
            Map<String, Serializable> parameters) {
        this.actionType = actionType;
        this.parameters = parameters;
    }

    public HazardDetailAction(ActionType actionType,
            Map<String, Serializable> parameters, Boolean isUserInitiated) {
        this(actionType, parameters);
        this.isUserInitiated = isUserInitiated;

    }

    public void setActionType(ActionType actionType) {
        this.actionType = actionType;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public void setParameters(Map<String, Serializable> parameters) {
        this.parameters = parameters;
    }

    public Map<String, Serializable> getParameters() {
        return parameters;
    }

    /**
     * @return the isUserInitiated
     */
    public Boolean getIsUserInitiated() {
        return isUserInitiated;
    }

    /**
     * @param isUserInitiated
     *            the isUserInitiated to set
     */
    public void setIsUserInitiated(Boolean isUserInitiated) {
        this.isUserInitiated = isUserInitiated;
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

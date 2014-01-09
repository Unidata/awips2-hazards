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

/**
 * Represents an action originating from the settings view.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 09, 2014            daniel.s.schaffer@noaa.gov      Initial induction into repo
 * 
 * </pre>
 * 
 * @author daniel.s.schaffer@noaa.gov
 */
public class StaticSettingsAction {

    public enum ActionType {
        NEW, SETTINGS_MODIFIED, SETTINGS_CHOSEN, SAVE, SAVE_AS, DIALOG, REVERT
    }

    private final ActionType actionType;

    private String detail;

    public StaticSettingsAction(ActionType actionType) {
        this.actionType = actionType;
    }

    public StaticSettingsAction(ActionType actionType, String detail) {
        this.actionType = actionType;
        this.detail = detail;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public String getDetail() {
        return detail;
    }
}
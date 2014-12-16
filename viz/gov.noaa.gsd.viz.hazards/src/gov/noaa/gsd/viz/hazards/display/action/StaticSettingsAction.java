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

import com.raytheon.uf.viz.hazards.sessionmanager.config.types.ISettings;

/**
 * Represents an action originating from the settings view.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 09, 2014            daniel.s.schaffer@noaa.gov      Initial induction into repo
 * Feb 19, 2014 2915       bkowal      JSON settings re-factor.
 * Dec 05, 2014 4124       Chris.Golden Changed to work with ISettings.
 * </pre>
 * 
 * @author daniel.s.schaffer@noaa.gov
 */
public class StaticSettingsAction extends AbstractSettingsAction {

    public enum ActionType {
        NEW, SETTINGS_MODIFIED, SETTINGS_CHOSEN, SAVE, SAVE_AS, DIALOG, REVERT
    }

    private final ActionType actionType;

    private String settingID;

    public StaticSettingsAction(ActionType actionType) {
        super(null);
        this.actionType = actionType;
    }

    public StaticSettingsAction(ActionType actionType, ISettings settings) {
        super(settings);
        this.actionType = actionType;
    }

    public StaticSettingsAction(ActionType actionType, String settingID) {
        super(null);
        this.actionType = actionType;
        this.settingID = settingID;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public String getSettingID() {
        return settingID;
    }
}
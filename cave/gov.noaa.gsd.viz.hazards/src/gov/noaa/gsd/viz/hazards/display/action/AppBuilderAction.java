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
 * Objects of this class are "fired" from the hazard services app builder over
 * the event bus.
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
public class AppBuilderAction {

    public enum ActionName {
        SET_SELECTION_DRAWING_ACTION, SET_CONSOLE_TIMELINE_DURATION, UPDATE_CAVE_SELECTED_TIME, USE_SETTING_ZOOM_PARAMETERS, SEND_FRAME_INFORMATION_TO_SESSION_MANAGER
    }

    private ActionName actionName = null;

    private String description = null;

    private Long value = null;

    public AppBuilderAction(ActionName actionName) {
        this.actionName = actionName;
    }

    public AppBuilderAction(ActionName actionName, String description) {
        this(actionName);
        this.description = description;
    }

    public AppBuilderAction(ActionName actionName, Long value) {
        this(actionName);
        this.value = value;
    }

    public ActionName getAction() {
        return actionName;
    }

    public String getDescription() {
        return description;
    }

    /**
     * @return the value
     */
    public Long getValue() {
        return value;
    }

}

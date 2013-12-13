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
 * Represents an action originating from the alerts view.
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
public class AlertsAction {

    private String action = null;

    private String detail = null;

    // Hide the default constructor.
    // Force the actionType and actionName
    // to be defined when the object is created.
    @SuppressWarnings("unused")
    private AlertsAction() {
        // TODO Auto-generated constructor stub
    }

    /**
     * Construct an instance of a ConsoleAction
     * 
     * @param actionType
     * 
     *            The action type.
     */
    public AlertsAction(String action, String detail) {
        this.action = action;
        this.detail = detail;
    }

    /**
     * 
     * 
     */
    public String getDetail() {
        return detail;
    }

    /**
     * 
     * 
     */
    public String getAction() {
        return action;
    }
}
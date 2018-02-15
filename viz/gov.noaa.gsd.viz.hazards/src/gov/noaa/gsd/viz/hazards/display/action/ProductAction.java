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

/**
 * This action is "fired" from product related dialogs when their states change.
 * Registered observers receive this object and act on it.
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
 * Jul 30, 2015 9681       Robert.Blum  Added ActionType VIEW.
 * Feb 01, 2017 15556      Chris.Golden Removed unneeded elements and renamed,
 *                                      since it is only used by the product
 *                                      editor and product selection dialog
 *                                      now.
 * Jun 08, 2017 16373      Chris.Golden Added ActionType REVIEW.
 * Apr 24, 2018 22308      Chris.Golden Removed ActionType VIEW.
 * </pre>
 * 
 * @author Bryon.Lawrence
 */
@Deprecated
public class ProductAction {

    public enum ActionType {
        PREVIEW, REVIEW
    }

    private ActionType actionType;

    private Map<String, Serializable> parameters;

    public ProductAction(ActionType actionType) {
        this.actionType = actionType;
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
}

/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Evaluation & Decision Support Branch (EDS)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.toolbar;

import java.util.List;

import org.eclipse.jface.action.IAction;

/**
 * Action consisting only a drop-down menu that holds other actions. It is used
 * together with a {@link MostRecentlyUsedAction}; when an action is chosen from
 * its drop-down menu, that action is assigned as the principal of the
 * associated instance of said class, as well as being run.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer      Description
 * ------------ ---------- ------------- --------------------------
 * Jan 03, 2018   33428    Chris.Golden  Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 */
public class ActionChooserAction<P extends BasicAction> extends
        ActionMenuAction<ActionChooserAction<P>, MostRecentlyUsedAction<P>, P> {

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param actionChoices
     *            List of actions that are the choices for the drop-down menu.
     *            Note that all the specified actions must have the same style,
     *            and that style must be one of the following:
     *            {@link IAction#AS_PUSH_BUTTON}, {@link IAction#AS_CHECK_BOX},
     *            or {@link IAction#AS_RADIO_BUTTON}.
     * @param mostRecentlyUsedAction
     *            Most recently used action wrapper paired with this chooser
     *            action. When an action is chosen from this chooser's menu, it
     *            becomes the most recently used action.
     */
    public ActionChooserAction(List<? extends P> actionChoices,
            MostRecentlyUsedAction mostRecentlyUsedAction) {
        super(actionChoices, ALL_ALLOWABLE_STYLES, mostRecentlyUsedAction);
    }
}

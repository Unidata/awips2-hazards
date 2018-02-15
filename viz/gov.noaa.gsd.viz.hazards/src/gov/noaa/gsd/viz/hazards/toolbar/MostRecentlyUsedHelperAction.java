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
 * Helper action consisting only a drop-down menu that holds other actions. It
 * is used together with a {@link MostRecentlyUsedAction}; when an action is
 * chosen from its drop-down menu, that action is assigned as the principal of
 * the associated instance of said class, as well as being run.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer      Description
 * ------------ ---------- ------------- --------------------------
 * Jan 03, 2018   33428    Chris.Golden  Initial creation.
 * Mar 22, 2018   15561    Chris.Golden  Refactored to make the drop-down
 *                                       provider be considered a helper,
 *                                       rather than the main-button-providing
 *                                       class, and to allow the helper and
 *                                       main button provider to communicate
 *                                       amongst themselves without needing
 *                                       help from code using them.
 * </pre>
 * 
 * @author Chris.Golden
 */
public class MostRecentlyUsedHelperAction<P extends BasicAction> extends
        ActionAndMenuHelperAction<MostRecentlyUsedAction<P>, MostRecentlyUsedHelperAction<P>, P> {

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
     */
    public MostRecentlyUsedHelperAction(List<? extends P> actionChoices) {
        super(actionChoices, ALL_ALLOWABLE_STYLES);
    }
}

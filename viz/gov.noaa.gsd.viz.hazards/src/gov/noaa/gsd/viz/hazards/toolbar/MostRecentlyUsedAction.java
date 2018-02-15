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

/**
 * Wrapper for an action that is the most-recently-chosen one from the drop-down
 * of an {@link MostRecentlyUsedHelperAction} instance.
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer      Description
 * ------------ ---------- ------------- --------------------------
 * Jan 06, 2018   33428    Chris.Golden  Initial creation.
 * Mar 22, 2018   15561    Chris.Golden  Refactored to make the drop-down
 *                                       provider be considered a helper,
 *                                       rather than the main-button-providing
 *                                       class, and to allow the helper and
 *                                       main button provider to communicate
 *                                       amongst themselves without needing
 *                                       help from code using them.
 * </pre>
 *
 * @author golden
 */
public class MostRecentlyUsedAction<P extends BasicAction> extends
        ActionAndMenuAction<MostRecentlyUsedAction<P>, MostRecentlyUsedHelperAction<P>, P> {

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param principal
     *            Action that is currently the one being wrapped by this action.
     * @param helper
     *            Helper for this action.
     */
    public MostRecentlyUsedAction(P principal,
            MostRecentlyUsedHelperAction<P> helper) {
        super(principal, helper);
    }

    // Public Methods

    @Override
    public void run() {
        if (getPrincipal() != null) {
            getPrincipal().run();
        }
    }
}

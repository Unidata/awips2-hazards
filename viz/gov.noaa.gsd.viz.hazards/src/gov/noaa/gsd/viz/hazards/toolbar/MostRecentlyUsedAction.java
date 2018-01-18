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
 * of an {@link ActionChooserAction} instance.
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer      Description
 * ------------ ---------- ------------- --------------------------
 * Jan 6, 2018    33428    Chris.Golden  Initial creation.
 * </pre>
 *
 * @author golden
 */
public class MostRecentlyUsedAction<P extends BasicAction> extends
        ActionMenuActionHelper<ActionChooserAction<P>, MostRecentlyUsedAction<P>, P> {

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param principal
     *            Action that is currently the one being wrapped by this action.
     */
    public MostRecentlyUsedAction(P principal) {
        super(principal);
    }

    // Public Methods

    @Override
    public void run() {
        if (getPrincipal() != null) {
            getPrincipal().run();
        }
    }
}

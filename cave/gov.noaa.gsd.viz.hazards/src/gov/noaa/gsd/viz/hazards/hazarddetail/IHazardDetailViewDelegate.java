/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.hazarddetail;

import gov.noaa.gsd.viz.mvp.IView;
import gov.noaa.gsd.viz.mvp.widgets.IStateChanger;

/**
 * Interface describing the methods that must be implemented by a class that
 * functions as a hazard detail view delegator, managed by an hazard detail
 * presenter, and managing a {@link HazardDetailViewPart}.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Chris.Golden      Initial induction into repo
 * May 10, 2013            Chris.Golden      Change to Eclipse view implementation.
 * Nov 14, 2013            Bryon.Lawrence    Added code to support hazard conflict 
 *                                           detection.
 * Feb 19, 2014    2161    Chris.Golden      Added passing of set of events allowing
 *                                           "until further notice" to the view
 *                                           during initialization.
 * May 15, 2014    2925    Chris.Golden      Renamed and essentially rewritten to
 *                                           provide far better separation of
 *                                           concerns between model, view, view
 *                                           delegate, and presenter.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public interface IHazardDetailViewDelegate<C, E extends Enum<E>> extends
        IView<C, E>, IHazardDetailView {

    // Public Methods

    /**
     * Get the detail view visibility state changer.
     * 
     * @return Detail view visibility state changer.
     */
    public IStateChanger<String, Boolean> getDetailViewVisibilityChanger();
}

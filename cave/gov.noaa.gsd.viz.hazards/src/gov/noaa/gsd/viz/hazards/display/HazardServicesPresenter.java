/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.display;

import gov.noaa.gsd.viz.mvp.IView;
import gov.noaa.gsd.viz.mvp.Presenter;

/**
 * Superclass from which to derive presenters for specific types of views for
 * Hazard Services.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Chris.Golden      Initial induction into repo
 * 
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public abstract class HazardServicesPresenter<V extends IView<?, ?>> extends
        Presenter<IHazardServicesModel, IHazardServicesModel.Element, V> {

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param model
     *            Model to be handled by this presenter.
     * @param view
     *            View to be handled by this presenter.
     */
    public HazardServicesPresenter(IHazardServicesModel model, V view) {
        super(model, view);
    }
}

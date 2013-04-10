/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.alerts;

import gov.noaa.gsd.viz.hazards.display.HazardServicesPresenter;
import gov.noaa.gsd.viz.hazards.display.IHazardServicesModel;
import gov.noaa.gsd.viz.hazards.display.IHazardServicesModel.Element;
import gov.noaa.gsd.viz.hazards.jsonutilities.Dict;
import gov.noaa.gsd.viz.hazards.jsonutilities.DictList;

import java.util.EnumSet;

/**
 * Alerts presenter, used to mediate between the model and the alerts view.
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
public class AlertsPresenter extends HazardServicesPresenter<IAlertsView<?, ?>> {

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param model
     *            Model to be handled by this presenter.
     * @param view
     *            Alerts view to be handled by this presenter.
     */
    public AlertsPresenter(IHazardServicesModel model, IAlertsView<?, ?> view) {
        super(model, view);
    }

    // Public Methods

    /**
     * Receive notification of a model change.
     * 
     * @param changes
     *            Set of elements within the model that have changed.
     */
    @Override
    public final void modelChanged(EnumSet<Element> changed) {

        // No action.
    }

    /**
     * Show a subview providing setting detail for the current alert.
     */
    public final void showAlertDetail() {

        // Get the parameters for the alert subview.
        DictList fields = DictList.getInstance(getModel().getConfigItem(
                "alertConfig"));
        /*
         * TODO Temporary Kludge. The values should be set to the current
         * configuration, not the one named "ALERTS1". But right now, it's not
         * even possible for the user to select a different tab.
         */
        Dict values = (Dict) Dict
                .getInstance(getModel().getAlertConfigValues()).get("ALERTS1");

        // Have the view open the alert detail subview.
        getView().showAlertDetail(fields, values);
    }

    // Protected Methods

    /**
     * Initialize the specified view in a subclass-specific manner.
     * 
     * @param view
     *            View to be initialized.
     */
    @Override
    protected final void initialize(IAlertsView<?, ?> view) {
        getView().initialize(this);
    }
}

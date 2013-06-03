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

import gov.noaa.gsd.viz.hazards.display.HazardServicesAppBuilder;
import gov.noaa.gsd.viz.hazards.display.HazardServicesPresenter;
import gov.noaa.gsd.viz.hazards.display.IHazardServicesModel;
import gov.noaa.gsd.viz.hazards.display.IHazardServicesModel.Element;
import gov.noaa.gsd.viz.hazards.jsonutilities.DictList;
import gov.noaa.gsd.viz.hazards.utilities.Utilities;

import java.util.EnumSet;

/**
 * Hazard detail presenter, used to mediate between the model and the hazard
 * detail view.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Chris.Golden      Initial induction into repo
 * May 10, 2013            Chris.Golden      Change to Eclipse view implementation.
 * 
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class HazardDetailPresenter extends
        HazardServicesPresenter<IHazardDetailView<?, ?>> {

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param model
     *            Model to be handled by this presenter.
     * @param view
     *            Hazard detail view to be handled by this presenter.
     */
    public HazardDetailPresenter(IHazardServicesModel model,
            IHazardDetailView<?, ?> view) {
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
    public void modelChanged(EnumSet<Element> changed) {
        if (changed.contains(Element.VISIBLE_TIME_RANGE)) {
            getView()
                    .setVisibleTimeRange(
                            Long.parseLong(getModel()
                                    .getTimeLineEarliestVisibleTime()),
                            Long.parseLong(getModel()
                                    .getTimeLineLatestVisibleTime()));
        }
        if (changed.contains(Element.EVENTS)) {
            getView().updateHazardDetail(
                    DictList.getInstance(getModel().getComponentData(
                            HazardServicesAppBuilder.HAZARD_INFO_ORIGINATOR,
                            "all")), getModel().getLastSelectedEventID());
        }
    }

    /**
     * Show a subview providing setting detail for the current hazard events.
     * 
     * @param force
     *            Flag indicating whether or not to force the showing of the
     *            subview. This may be used as a hint by views if they are
     *            considering not showing the subview for whatever reason.
     */
    public final void showHazardDetail(boolean force) {

        // Get the hazard events to be displayed in detail, and
        // determine which event should be foregrounded.
        String jsonEventsList = getModel().getComponentData(
                HazardServicesAppBuilder.HAZARD_INFO_ORIGINATOR, "all");
        DictList eventsList = DictList.getInstance(jsonEventsList);
        if ((force == false)
                && ((eventsList == null) || (eventsList.size() == 0))) {
            return;
        }
        String topEventID = getModel().getLastSelectedEventID();

        // Have the view open the alert detail subview.
        getView().showHazardDetail(eventsList, topEventID, force);
    }

    /**
     * Hide the hazard detail subview.
     * 
     * @param force
     *            Flag indicating whether or not to force the hiding of the
     *            subview. This may be used as a hint by views if they are
     *            considering not hiding the subview for whatever reason.
     */
    public final void hideHazardDetail(boolean force) {
        getView().hideHazardDetail(force);
    }

    // Protected Methods

    /**
     * Initialize the specified view in a subclass-specific manner.
     * 
     * @param view
     *            View to be initialized.
     */
    @Override
    protected void initialize(IHazardDetailView<?, ?> view) {

        // Get the basic initialization info for the subview.
        String basicInfo = getModel().getConfigItem(
                Utilities.HAZARD_INFO_GENERAL_CONFIG);
        String metadataMegawidgets = getModel().getConfigItem(
                Utilities.HAZARD_INFO_METADATA_CONFIG);
        getView().initialize(this, basicInfo, metadataMegawidgets,
                Long.parseLong(getModel().getTimeLineEarliestVisibleTime()),
                Long.parseLong(getModel().getTimeLineLatestVisibleTime()));
    }
}

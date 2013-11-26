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

import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;

import com.google.common.collect.Maps;
import com.google.common.eventbus.EventBus;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;

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
 * Jun 25, 2013            Chris.Golden      Added code to prevent reentrant
 *                                           behavior when receiving an event-
 *                                           changed notification.
 * Jul 15, 2013     585    Chris.Golden      Changed to support loading from bundle,
 *                                           including the passing in of the event
 *                                           bus so that the latter is no longer a
 *                                           singleton.
 * Nov 14, 2013    1463    Bryon.Lawrence    Added code to support hazard conflict
 *                                           detection.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class HazardDetailPresenter extends
        HazardServicesPresenter<IHazardDetailView<?, ?>> {

    // Private Variables

    /**
     * Flag indicating whether or not an event change notification is being
     * processed.
     */
    private boolean eventChange = false;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param model
     *            Model to be handled by this presenter.
     * @param view
     *            Hazard detail view to be handled by this presenter.
     * @param eventBus
     *            Event bus used to signal changes.
     */
    public HazardDetailPresenter(IHazardServicesModel model,
            IHazardDetailView<?, ?> view, EventBus eventBus) {
        super(model, view, eventBus);
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
        if (changed.contains(Element.EVENTS) && (eventChange == false)) {
            eventChange = true;

            getView().updateHazardDetail(
                    DictList.getInstance(getModel().getComponentData(
                            HazardServicesAppBuilder.HAZARD_INFO_ORIGINATOR,
                            "all")), getModel().getLastSelectedEventID(),
                    getConflictingEventsForSelectedEvents());
            eventChange = false;
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
        getView().showHazardDetail(eventsList, topEventID,
                getConflictingEventsForSelectedEvents(), force);
    }

    /**
     * Hide the hazard detail subview.
     */
    public final void hideHazardDetail() {
        getView().hideHazardDetail(false);
    }

    // Protected Methods

    /**
     * Initialize the specified view in a subclass-specific manner.
     * 
     * @param view
     *            View to be initialized.
     */
    @Override
    public void initialize(IHazardDetailView<?, ?> view) {

        // Get the basic initialization info for the subview.
        String basicInfo = getModel().getConfigItem(
                Utilities.HAZARD_INFO_GENERAL_CONFIG);
        String metadataMegawidgets = getModel().getConfigItem(
                Utilities.HAZARD_INFO_METADATA_CONFIG);
        getView().initialize(this, basicInfo, metadataMegawidgets,
                Long.parseLong(getModel().getTimeLineEarliestVisibleTime()),
                Long.parseLong(getModel().getTimeLineLatestVisibleTime()));

        // Update the view with the currently selected hazard events,
        // if any.
        getView()
                .updateHazardDetail(
                        DictList.getInstance(getModel()
                                .getComponentData(
                                        HazardServicesAppBuilder.HAZARD_INFO_ORIGINATOR,
                                        "all")),
                        getModel().getLastSelectedEventID(),
                        getConflictingEventsForSelectedEvents());
    }

    /**
     * Returns a map of conflicting events for the currently selected event ids
     * if auto hazard checking is 'on'.
     * 
     * @param
     * @return
     */
    private Map<String, Collection<IHazardEvent>> getConflictingEventsForSelectedEvents() {
        Map<String, Collection<IHazardEvent>> eventConflictList;

        if (getSessionManager().isAutoHazardCheckingOn()) {
            eventConflictList = getSessionManager().getEventManager()
                    .getConflictingEventsForSelectedEvents();

        } else {
            eventConflictList = Maps.newHashMap();
        }

        return eventConflictList;
    }

}

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

import gov.noaa.gsd.viz.hazards.display.HazardServicesPresenter;
import gov.noaa.gsd.viz.hazards.display.deprecated.DeprecatedUtilities;
import gov.noaa.gsd.viz.hazards.jsonutilities.DeprecatedEvent;
import gov.noaa.gsd.viz.hazards.jsonutilities.DictList;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.time.TimeRange;
import com.raytheon.uf.viz.hazards.sessionmanager.ISessionManager;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.impl.ObservedHazardEvent;

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
 * Dec 03, 2013    2182    daniel.s.schaffer eliminated IHazardsIF
 * Feb 19, 2014    2161    Chris.Golden      Added passing of set of events allowing
 *                                           "until further notice" to the view
 *                                           during initialization.
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
    public HazardDetailPresenter(ISessionManager<ObservedHazardEvent> model,
            IHazardDetailView<?, ?> view, EventBus eventBus) {
        super(model, view, eventBus);
    }

    // Public Methods

    @Override
    public void modelChanged(EnumSet<HazardConstants.Element> changed) {
        if (changed.contains(HazardConstants.Element.VISIBLE_TIME_RANGE)) {
            TimeRange timeRange = timeManager.getVisibleRange();
            getView().setVisibleTimeRange(timeRange.getStart().getTime(),
                    timeRange.getEnd().getTime());
        }
        if (changed.contains(HazardConstants.Element.EVENTS)
                && (eventChange == false)) {
            eventChange = true;

            DictList eventsAsDictList = adaptEventsForDisplay();
            getView().updateHazardDetail(eventsAsDictList,
                    eventManager.getLastSelectedEventID(),
                    getConflictingEventsForSelectedEvents());
            eventChange = false;
        }

    }

    /**
     * TODO This is how Matt, Chris and Dan think model changes should be
     * handled. We should move
     * {@link HazardDetailPresenter#modelChanged(EnumSet)} into here.
     */
    @Subscribe
    public void sessionModified(SessionModified event) {
        getView().setPreviewOngoing(getModel().isPreviewOngoing());
        getView().setIssueOngoing(getModel().isIssueOngoing());
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
        DictList eventsAsDictList = adaptEventsForDisplay();
        if ((force == false)
                && ((eventsAsDictList == null) || (eventsAsDictList.size() == 0))) {
            return;
        }
        String topEventID = eventManager.getLastSelectedEventID();

        // Have the view open the alert detail subview.
        getView().showHazardDetail(eventsAsDictList, topEventID,
                getConflictingEventsForSelectedEvents(), force);
    }

    /**
     * Hide the hazard detail subview.
     */
    public final void hideHazardDetail() {
        getView().hideHazardDetail(false);
    }

    // Protected Methods

    @Override
    public void initialize(IHazardDetailView<?, ?> view) {

        // Get the basic initialization info for the subview.
        String basicInfo = jsonConverter.toJson(configurationManager
                .getHazardInfoConfig());
        String metadataMegawidgets = jsonConverter.toJson(configurationManager
                .getHazardInfoOptions());
        TimeRange timeRange = timeManager.getVisibleRange();
        getView().initialize(this, basicInfo, metadataMegawidgets,
                timeRange.getStart().getTime(), timeRange.getEnd().getTime(),
                eventManager.getEventIdsAllowingUntilFurtherNotice());

        // Update the view with the currently selected hazard events,
        // if any.
        DictList eventsAsDictList = adaptEventsForDisplay();

        getView().updateHazardDetail(eventsAsDictList,
                eventManager.getLastSelectedEventID(),
                getConflictingEventsForSelectedEvents());
    }

    private DictList adaptEventsForDisplay() {
        Collection<ObservedHazardEvent> selectedEvents = eventManager
                .getSelectedEvents();
        DeprecatedEvent[] jsonEvents = DeprecatedUtilities
                .eventsAsJSONEvents(selectedEvents);
        DeprecatedUtilities.adaptJSONEvent(jsonEvents, selectedEvents,
                configurationManager, timeManager);
        String eventsAsJSON = DeprecatedUtilities.eventsAsNodeJSON(
                selectedEvents, jsonEvents);
        return DictList.getInstance(eventsAsJSON);
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
            eventConflictList = new HashMap<>();
        }

        return eventConflictList;
    }

}

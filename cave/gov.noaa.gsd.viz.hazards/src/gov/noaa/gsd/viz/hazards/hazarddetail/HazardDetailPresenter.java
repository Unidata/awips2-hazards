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

import gov.noaa.gsd.common.eventbus.BoundedReceptionEventBus;
import gov.noaa.gsd.viz.hazards.UIOriginator;
import gov.noaa.gsd.viz.hazards.display.HazardServicesPresenter;
import gov.noaa.gsd.viz.hazards.display.deprecated.DeprecatedUtilities;
import gov.noaa.gsd.viz.hazards.jsonutilities.DeprecatedEvent;
import gov.noaa.gsd.viz.hazards.jsonutilities.DictList;
import gov.noaa.gsd.viz.mvp.Presenter;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import net.engio.mbassy.listener.Handler;

import com.google.common.eventbus.Subscribe;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.time.TimeRange;
import com.raytheon.uf.viz.hazards.sessionmanager.ISessionManager;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventsModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.impl.ObservedHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.time.VisibleTimeRangeChanged;

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
 * Apr 09, 2014    2925    Chris.Golden      Refactored extensively to support new
 *                                           class-based metadata, as well as to
 *                                           conform to new event propagation scheme.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class HazardDetailPresenter extends
        HazardServicesPresenter<IHazardDetailView<?, ?>> {

    // Private Variables

    /**
     * Map of selected event identifiers to the events with which they conflict,
     * if any, as last fetched.
     */
    private Map<String, Collection<IHazardEvent>> oldConflictingEventsForSelectedEvents;

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
            IHazardDetailView<?, ?> view,
            BoundedReceptionEventBus<Object> eventBus) {
        super(model, view, eventBus);
    }

    // Public Methods

    /**
     * TODO: This method will be removed altogether from the {@link Presenter}
     * in the future, when all needed notifications are subscribed to directly
     * by each <code>Presenter</code>. For now, this method simply does nothing,
     * as other member methods marked with {@link Subscribe} are being used to
     * receive needed notifications. This subclass of <code>Presenter</code> is
     * thus closer to what is planned for all <code>Presenter</code>s in the
     * future.
     */
    @Deprecated
    @Override
    public void modelChanged(EnumSet<HazardConstants.Element> changed) {

        // No action
    }

    /**
     * Respond to the visible time range changing.
     * 
     * @param event
     *            Event that occurred.
     */
    @Handler
    public void visibleTimeRangeChanged(final VisibleTimeRangeChanged event) {
        TimeRange timeRange = timeManager.getVisibleRange();
        getView().setVisibleTimeRange(timeRange.getStart().getTime(),
                timeRange.getEnd().getTime());
    }

    /**
     * Respond to the hazard events changing in some way.
     * 
     * @param event
     *            Event that occurred.
     */
    @Handler
    public void sessionEventsModified(final SessionEventsModified event) {

        /*
         * TODO: Because conflicting events are not tracked dynamically by the
         * session event manager, and thus cannot be checked at any time without
         * specifically querying said manager (as opposed to, for example, the
         * set of events that allow until further notice), they must be checked
         * each time through here. Ideally, what should occur is the conflicting
         * events set should be tracked dynamically, and notifications sent out
         * specifically indicating that the set has changed when it does. This
         * way, this code wouldn't be required.
         */
        Map<String, Collection<IHazardEvent>> conflictingEventsForSelectedEvents = getConflictingEventsForSelectedEvents();
        if ((event.getOriginator() == UIOriginator.HAZARD_INFORMATION_DIALOG)
                && conflictingEventsForSelectedEvents
                        .equals(oldConflictingEventsForSelectedEvents)) {
            return;
        }
        oldConflictingEventsForSelectedEvents = conflictingEventsForSelectedEvents;

        /*
         * TODO: This is a brute-force approach to updating the HID; a better
         * implementation would allow more fine-grained changes to be processed.
         */
        DictList eventsAsDictList = adaptEventsForDisplay();
        getView().updateHazardDetail(eventsAsDictList,
                eventManager.getLastSelectedEventID(),
                conflictingEventsForSelectedEvents);
    }

    /**
     * Respond to session modifications by determining whether an issue or
     * preview are ongoing.
     * 
     * @param event
     *            Event that occurred.
     */
    @Handler
    public void sessionModified(final SessionModified event) {
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

        /*
         * Get the hazard events to be displayed in detail, and determine which
         * event should be foregrounded.
         */
        DictList eventsAsDictList = adaptEventsForDisplay();
        if ((force == false)
                && ((eventsAsDictList == null) || (eventsAsDictList.size() == 0))) {
            return;
        }
        String topEventID = eventManager.getLastSelectedEventID();

        /*
         * Have the view open the alert detail subview.
         */
        oldConflictingEventsForSelectedEvents = getConflictingEventsForSelectedEvents();
        getView().showHazardDetail(eventsAsDictList, topEventID,
                oldConflictingEventsForSelectedEvents, force);
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

        /*
         * Get the basic initialization info for the subview.
         */
        String basicInfo = jsonConverter.toJson(configurationManager
                .getHazardInfoConfig());
        String metadataMegawidgets = jsonConverter.toJson(configurationManager
                .getHazardInfoOptions());
        TimeRange timeRange = timeManager.getVisibleRange();
        getView().initialize(this, basicInfo, metadataMegawidgets,
                timeRange.getStart().getTime(), timeRange.getEnd().getTime(),
                eventManager.getEventIdsAllowingUntilFurtherNotice());

        /*
         * Update the view with the currently selected hazard events, if any.
         */
        DictList eventsAsDictList = adaptEventsForDisplay();
        oldConflictingEventsForSelectedEvents = getConflictingEventsForSelectedEvents();
        getView().updateHazardDetail(eventsAsDictList,
                eventManager.getLastSelectedEventID(),
                oldConflictingEventsForSelectedEvents);
    }

    // Package Methods

    /**
     * Set the type of the specified event.
     * <p>
     * TODO: Should be private.
     * 
     * @param eventId
     *            Identifier of the hazard event to have its type changed.
     * @param fullType
     *            New full type for the hazard event.
     */
    void setHazardType(String eventId, String fullType) {
        ObservedHazardEvent event = eventManager.getEventById(eventId);
        if (event != null) {
            String[] phenSig;
            if (!fullType.isEmpty()) {
                phenSig = fullType.split(" ")[0].split("\\.");
                String subType = null;
                if (phenSig.length > 2) {
                    subType = phenSig[2];
                }
                eventManager.setEventType(event, phenSig[0], phenSig[1],
                        subType, UIOriginator.HAZARD_INFORMATION_DIALOG);
            } else {
                eventManager.setEventType(event, null, null, null,
                        UIOriginator.HAZARD_INFORMATION_DIALOG);
            }
        }
    }

    /**
     * Change the start time of the specified event.
     * <p>
     * TODO: Should be private.
     * 
     * @param eventId
     *            Identifier of the hazard event to have its start time changed.
     * @param startTime
     *            New start time.
     */
    void setHazardStartTime(String eventId, long startTime) {
        ObservedHazardEvent event = eventManager.getEventById(eventId);
        if (event != null) {
            event.setStartTime(new Date(startTime),
                    UIOriginator.HAZARD_INFORMATION_DIALOG);
        }
    }

    /**
     * Change the end time of the specified event.
     * <p>
     * TODO: Should be private.
     * 
     * @param eventId
     *            Identifier of the hazard event to have its end time changed.
     * @param endTime
     *            New end time.
     */
    void setHazardEndTime(String eventId, long endTime) {
        ObservedHazardEvent event = eventManager.getEventById(eventId);
        if (event != null) {
            event.setEndTime(new Date(endTime),
                    UIOriginator.HAZARD_INFORMATION_DIALOG);
        }
    }

    /**
     * Change the specified attribute of the specified event.
     * <p>
     * TODO: Should be private.
     * 
     * @param eventId
     *            Identifier of the hazard event to have its attribute changed.
     * @param key
     *            Key of the attribute to be changed.
     * @param value
     *            New value of the attribute.
     */
    void setHazardAttribute(String eventId, String key, Serializable value) {
        ObservedHazardEvent event = eventManager.getEventById(eventId);
        if (event != null) {
            event.addHazardAttribute(key, value,
                    UIOriginator.HAZARD_INFORMATION_DIALOG);
        }
    }

    // Private Methods

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

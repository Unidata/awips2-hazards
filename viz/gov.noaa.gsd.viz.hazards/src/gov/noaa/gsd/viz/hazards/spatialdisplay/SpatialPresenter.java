/**
 * This software was developed and / or modified by the National Oceanic and
 * Atmospheric Administration (NOAA), Earth System Research Laboratory (ESRL),
 * Global Systems Division (GSD), Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.spatialdisplay;

import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HAZARD_EVENT_SELECTED;
import gov.noaa.gsd.common.eventbus.BoundedReceptionEventBus;
import gov.noaa.gsd.viz.hazards.UIOriginator;
import gov.noaa.gsd.viz.hazards.display.HazardServicesPresenter;
import gov.noaa.gsd.viz.hazards.spatialdisplay.mousehandlers.MouseHandlerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.engio.mbassy.listener.Handler;

import com.google.common.collect.Lists;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardStatus;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.ISessionManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.ObservedSettings;
import com.raytheon.uf.viz.hazards.sessionmanager.events.ISessionEventManager;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventAdded;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventGeometryModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventRemoved;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventTimeRangeModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventTypeModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventsModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionHatchingToggled;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionSelectedEventsModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.impl.ObservedHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.IOriginator;
import com.raytheon.uf.viz.hazards.sessionmanager.time.ISessionTimeManager;
import com.raytheon.uf.viz.hazards.sessionmanager.time.SelectedTime;

/**
 * Spatial presenter, used to mediate between the model and the spatial view.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Chris.Golden      Initial induction into repo
 * Jul 12, 2013     585    Chris.Golden      Changed to support loading from bundle,
 *                                           including the passing in of the event
 *                                           bus so that the latter is no longer a
 *                                           singleton.
 * Aug 6, 2013     1265    Bryon.Lawrence    Added support for undo/redo.
 * Aug  9, 2013 1921       daniel.s.schaffer@noaa.gov  Support of replacement of JSON with POJOs
 * Nov 23, 2013    1462    bryon.lawrence    Added support for drawing hazard
 *                                           hatched areas.
 * Dec 03, 2013 2182 daniel.s.schaffer@noaa.gov Refactoring - eliminated IHazardsIF
 * May 17, 2014 2925       Chris.Golden      Added newly required implementation of
 *                                           reinitialize(), and made initialize()
 *                                           protected as it is called by setView().
 * Aug 28, 2014 2532       Robert.Blum       Commented out zooming when settings are
 *                                           changed.
 * Nov 18, 2014  4124      Chris.Golden      Adapted to new time manager.
 * Dec 05, 2014  4124      Chris.Golden      Changed to work with newly parameterized
 *                                           config manager.
 * Dec 13, 2014 4959       Dan Schaffer Spatial Display cleanup and other bug fixes
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class SpatialPresenter extends
        HazardServicesPresenter<ISpatialView<?, ?>> implements IOriginator {

    /**
     * Mouse handler factory.
     */
    private MouseHandlerFactory mouseFactory = null;

    /**
     * List of the currently selected events. This is needed for the case of
     * "multiple-deselection".
     */
    private final List<String> selectedEventIDs = new ArrayList<>();

    /**
     * Construct a standard instance.
     * 
     * @param model
     *            Model to be handled by this presenter.
     * @param view
     *            Spatial view to be handled by this presenter.
     * @param eventBus
     *            Event bus used to signal changes.
     */
    public SpatialPresenter(
            ISessionManager<ObservedHazardEvent, ObservedSettings> model,
            BoundedReceptionEventBus<Object> eventBus) {
        super(model, eventBus);
    }

    // Public Methods

    /**
     * Receive notification of a model change.
     * 
     * @param changes
     *            Set of elements within the model that have changed.
     */
    @Override
    public void modelChanged(EnumSet<HazardConstants.Element> changed) {
        if (changed.contains(HazardConstants.Element.CURRENT_SETTINGS)) {

            ObservedSettings settings = getModel().getConfigurationManager()
                    .getSettings();
            getView().setSettings(settings);
        } else if (changed.contains(HazardConstants.Element.CAVE_TIME)) {
            updateCaveSelectedTime();
        }

        updateSpatialDisplay();
    }

    @Handler
    public void sessionSelectedEventsModified(
            SessionSelectedEventsModified notification) {
        updateSpatialDisplay();
    }

    @Handler
    public void sessionEventTimeRangeModified(
            SessionEventTimeRangeModified notification) {
        updateSpatialDisplay();
    }

    @Handler
    public void sessionEventTypeModified(SessionEventTypeModified notification) {
        updateSpatialDisplay();
    }

    @Handler
    public void sessionEventGeometryModified(
            SessionEventGeometryModified notification) {
        updateSpatialDisplay();
    }

    @Handler
    public void sessionEventRemoved(SessionEventRemoved notification) {
        updateSpatialDisplay();
    }

    @Handler
    public void sessionHatchingToggled(SessionHatchingToggled notification) {
        updateSpatialDisplay();
    }

    @Handler
    public void sessionEventAdded(SessionEventAdded notification) {
        updateSpatialDisplay();
    }

    @Handler
    public void sessionEventsModified(SessionEventsModified notification) {
        updateSpatialDisplay();
    }

    /**
     * Update the event areas drawn in the spatial view.
     */
    public void updateSpatialDisplay() {
        getView().setUndoEnabled(getModel().isUndoable());
        getView().setRedoEnabled(getModel().isRedoable());

        /**
         * TODO For reasons that are not clear to Chris Golden and Dan Schaffer,
         * this method is called for the old SpatialDisplay when you switch
         * perspectives and create a new {@link SpatialDisplay}. But part of the
         * changing perspective process is to nullify the appBuilder so we have
         * to do a check here. It would be good to take some time to understand
         * why this method is called in the old one when you switch
         * perspectives.
         */

        ISessionManager<ObservedHazardEvent, ObservedSettings> sessionManager = getModel();
        if (sessionManager == null) {
            return;
        }

        ISessionEventManager<ObservedHazardEvent> eventManager = sessionManager
                .getEventManager();
        Collection<ObservedHazardEvent> events = eventManager
                .getCheckedEvents();
        ISessionTimeManager timeManager = sessionManager.getTimeManager();

        SelectedTime selectedTime = timeManager.getSelectedTime();
        filterEventsForTime(events, selectedTime);

        Map<String, Boolean> eventOverlapSelectedTime = new HashMap<>();
        Map<String, Boolean> forModifyingStormTrack = new HashMap<>();
        Map<String, Boolean> eventEditability = new HashMap<>();

        for (ObservedHazardEvent event : events) {
            String eventID = event.getEventID();
            eventOverlapSelectedTime.put(eventID,
                    doesEventOverlapSelectedTime(event, selectedTime));
            forModifyingStormTrack
                    .put(event.getEventID(),
                            event.getHazardAttribute(HazardConstants.TRACK_POINTS) != null);
            eventEditability.put(event.getEventID(),
                    eventManager.canEventAreaBeChanged(event));
        }

        updateSelectedEvents(events);
        /*
         * TODO It might be possible to optimize here by checking if the events
         * have changed before displaying.
         */
        getView().drawEvents(events, eventOverlapSelectedTime,
                forModifyingStormTrack, eventEditability,
                getModel().isAutoHazardCheckingOn(),
                getModel().areHatchedAreasDisplayed());

    }

    private void updateSelectedEvents(Collection<ObservedHazardEvent> events) {
        selectedEventIDs.clear();
        for (ObservedHazardEvent hazardEvent : events) {
            String eventID = hazardEvent.getEventID();
            Boolean isSelected = (Boolean) hazardEvent
                    .getHazardAttribute(HAZARD_EVENT_SELECTED);

            if (isSelected != null && isSelected
                    && !selectedEventIDs.contains(eventID)) {

                /*
                 * Since there can be multiple polygons per event, represent
                 * each event only once.
                 */
                selectedEventIDs.add(eventID);
            }
        }
    }

    /**
     * Updates the CAVE time.
     * 
     * @param selectedTime_ms
     *            The Hazard Services selected time in milliseconds.
     * @return
     */
    public void updateCaveSelectedTime() {
        getView().manageViewFrames(getSelectedTime());
    }

    /**
     * Get the selected time.
     * 
     * @return Selected time.
     */
    public Date getSelectedTime() {
        return new Date(getModel().getTimeManager().getSelectedTime()
                .getLowerBound());
    }

    @Override
    protected void initialize(ISpatialView<?, ?> view) {
        if (mouseFactory == null) {
            mouseFactory = new MouseHandlerFactory(this);
        }
        getView().initialize(this, mouseFactory);
        updateSpatialDisplay();
    }

    @Override
    protected void reinitialize(ISpatialView<?, ?> view) {

        /*
         * No action.
         */
    }

    /**
     * Convenience method for testing if the event is contained within the
     * selected time range.
     * 
     * @param
     * @return true if overlap
     */
    private Boolean doesEventOverlapSelectedTime(IHazardEvent event,
            SelectedTime selectedRange) {
        return selectedRange.intersects(event.getStartTime().getTime(), event
                .getEndTime().getTime());
    }

    public void handleSelection(String clickedEventId, boolean multipleSelection) {
        /*
         * If this is a multiple selection operation (left mouse click with the
         * Ctrl or Shift key held down), then check if the user is selecting
         * something that was already selected and treat this as a deselect.
         */
        if (multipleSelection && selectedEventIDs.contains(clickedEventId)) {
            selectedEventIDs.remove(clickedEventId);
            String[] eventIDs = selectedEventIDs.toArray(new String[0]);
            updateSelectedEventIds(eventIDs);
        } else if (multipleSelection) {
            selectedEventIDs.add(clickedEventId);
            String[] eventIDs = selectedEventIDs.toArray(new String[0]);
            updateSelectedEventIds(eventIDs);
        } else {
            updateSelectedEventIds(new String[] { clickedEventId });
        }
    }

    /**
     * When an event is selected on the spatial display, the model is updated.
     * 
     * @param eventIDs
     *            The identifiers of the events selected in the spatial display.
     */
    public void updateSelectedEventIds(String[] eventIDs) {
        List<String> selectedEventIDs = Lists.newArrayList(eventIDs);
        ISessionEventManager<?> sessionEventManager = getModel()
                .getEventManager();
        sessionEventManager.setSelectedEventForIDs(selectedEventIDs,
                UIOriginator.SPATIAL_DISPLAY);
    }

    private void filterEventsForTime(Collection<ObservedHazardEvent> events,
            SelectedTime selectedTime) {
        Iterator<ObservedHazardEvent> it = events.iterator();
        while (it.hasNext()) {
            IHazardEvent event = it.next();

            /*
             * Test for unissued storm track operations. These should not be
             * filtered out by time.
             */
            if (!(event.getHazardAttribute(HazardConstants.TRACK_POINTS) != null && !HazardStatus
                    .hasEverBeenIssued(event.getStatus()))) {

                if (!doesEventOverlapSelectedTime(event, selectedTime)) {
                    it.remove();
                }
            }

        }
    }
}

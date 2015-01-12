/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.console;

import gov.noaa.gsd.common.eventbus.BoundedReceptionEventBus;
import gov.noaa.gsd.common.utilities.JSONConverter;
import gov.noaa.gsd.viz.hazards.UIOriginator;
import gov.noaa.gsd.viz.hazards.display.HazardServicesPresenter;
import gov.noaa.gsd.viz.hazards.display.deprecated.DeprecatedUtilities;
import gov.noaa.gsd.viz.hazards.jsonutilities.DeprecatedEvent;
import gov.noaa.gsd.viz.hazards.jsonutilities.Dict;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.engio.mbassy.listener.Handler;

import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.hazards.sessionmanager.ISessionManager;
import com.raytheon.uf.viz.hazards.sessionmanager.alerts.HazardAlertsModified;
import com.raytheon.uf.viz.hazards.sessionmanager.config.SettingsModified;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.ObservedSettings;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Console;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.StartUpConfig;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventAdded;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventAttributesModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventRemoved;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventStatusModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventTimeRangeModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventTypeModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionSelectedEventsModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.impl.ObservedHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.time.ISessionTimeManager;
import com.raytheon.uf.viz.hazards.sessionmanager.time.SelectedTime;
import com.raytheon.uf.viz.hazards.sessionmanager.time.SelectedTimeChanged;

/**
 * Console presenter, used to manage the console view.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Chris.Golden      Initial induction into repo
 * Jul 15, 2013     585    Chris.Golden      Changed to support loading from bundle,
 *                                           including the passing in of the event
 *                                           bus so that the latter is no longer a
 *                                           singleton.
 * Aug 16, 2013    1325    daniel.s.schaffer Alerts integration
 * Aug 22, 2013    1936    Chris.Golden      Added console countdown timers.
 * Nov 04, 2013    2182    daniel.s.schaffer Started refactoring
 * Dec 03, 2013    2182    daniel.s.schaffer Refactoring - eliminated IHazardsIF
 * Jan 28, 2014    2161    Chris.Golden      Fixed bug that caused equality checks
 *                                           of old and new hazard events to always
 *                                           return false. Also added passing of
 *                                           set of events allowing "until further
 *                                           notice" to the view during initialization.
 * May 17, 2014    2925    Chris.Golden      Added newly required implementation of
 *                                           reinitialize(), and made initialize()
 *                                           protected as it is called by setView().
 * Nov 18, 2014    4124    Chris.Golden      Changed to use a handler method to watch
 *                                           for selected time changes, and adapted to
 *                                           new time manager.
 * Dec 05, 2014    4124    Chris.Golden      Changed to use a handler method to watch
 *                                           for settings changes, and to work with
 *                                           newly parameterized config manager. Also
 *                                           added in code to ignore changes that
 *                                           originated with this presenter.
 * Jan 08, 2015    2394    Chris.Golden      Added code to ensure that the river mile
 *                                           hazard attribute is always included in
 *                                           the dictionaries sent to the view when it
 *                                           is present in the original events.
 * Dec 13, 2014 4959       Dan Schaffer Spatial Display cleanup and other bug fixes
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class ConsolePresenter extends
        HazardServicesPresenter<IConsoleView<?, ?>> {

    // Private Static Constants

    /**
     * Logging mechanism.
     */
    @SuppressWarnings("unused")
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(ConsolePresenter.class);

    /**
     * River mile key in hazard attributes; this will not be needed once the
     * console refactor has been done and hazard events no longer need to be
     * converted to dictionaries.
     */
    @Deprecated
    private static final String RIVER_MILE = "riverMile";

    // Private Variables

    /**
     * JSON converter.
     */
    private final JSONConverter jsonConverter = new JSONConverter();

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param model
     *            Model to be handled by this presenter.
     * @param eventBus
     *            Event bus used to signal changes.
     */
    public ConsolePresenter(
            ISessionManager<ObservedHazardEvent, ObservedSettings> model,
            BoundedReceptionEventBus<Object> eventBus) {
        super(model, eventBus);
    }

    // Public Methods

    /**
     * Respond to the selected time changing.
     * 
     * @change Change that occurred.
     */
    @Handler
    public void selectedTimeChanged(SelectedTimeChanged change) {
        if (change.getOriginator() != UIOriginator.CONSOLE) {
            SelectedTime selectedTime = getModel().getTimeManager()
                    .getSelectedTime();
            getView().updateSelectedTimeRange(
                    new Date(selectedTime.getLowerBound()),
                    new Date(selectedTime.getUpperBound()));
        }
    }

    /**
     * Respond to the current settings changing.
     * 
     * @change Change that occurred.
     */
    @Handler
    public void currentSettingsChanged(SettingsModified change) {
        if (change.getOriginator() != UIOriginator.CONSOLE) {
            updateHazardEventsForSettingChange();
        }
    }

    @Override
    @Deprecated
    public final void modelChanged(EnumSet<HazardConstants.Element> changed) {
        ISessionTimeManager timeManager = getModel().getTimeManager();
        if (changed.contains(HazardConstants.Element.CURRENT_TIME)) {
            getView().updateCurrentTime(timeManager.getCurrentTime());
        }
        if (changed.contains(HazardConstants.Element.VISIBLE_TIME_DELTA)) {
            String timeDelta = Long.toString(getModel()
                    .getConfigurationManager().getSettings()
                    .getDefaultTimeDisplayDuration());
            getView().updateVisibleTimeDelta(timeDelta);
        }
        if (changed.contains(HazardConstants.Element.VISIBLE_TIME_RANGE)) {
            String earliestTime = jsonConverter.fromDate(timeManager
                    .getVisibleTimeRange().getStart());
            String latestTime = jsonConverter.fromDate(timeManager
                    .getVisibleTimeRange().getEnd());
            getView().updateVisibleTimeRange(earliestTime, latestTime);
        }
        if (changed.contains(HazardConstants.Element.SETTINGS)) {
            getView()
                    .setSettings(
                            getModel().getConfigurationManager().getSettings()
                                    .getSettingsID(),
                            getModel().getConfigurationManager()
                                    .getAvailableSettings());
        }
        if (changed.contains(HazardConstants.Element.EVENTS)) {
            updateHazardEventsForEventChange();
        }
        if (changed.contains(HazardConstants.Element.SITE)) {
            getView().updateTitle(
                    getModel().getConfigurationManager().getSiteID());
        }
    }

    @Override
    public void dispose() {
        getModel().unregisterForNotification(this);
    }

    // Protected Methods

    @Override
    protected final void initialize(IConsoleView<?, ?> view) {

        /*
         * Determine whether the time line navigation buttons should be in the
         * console toolbar, or below the console's table.
         */
        boolean temporalControlsInToolBar = true;
        StartUpConfig startUpConfig = getModel().getConfigurationManager()
                .getStartUpConfig();
        if (startUpConfig != null) {
            Console console = startUpConfig.getConsole();
            if (console != null) {
                String timeLineNavigation = console.getTimeLineNavigation();
                if (timeLineNavigation != null) {
                    temporalControlsInToolBar = !(timeLineNavigation
                            .equals(HazardConstants.START_UP_CONFIG_CONSOLE_TIMELINE_NAVIGATION_BELOW));
                }
            }
        }

        List<Dict> eventsAsDicts = adaptEventsForDisplay();

        /*
         * Initialize the view.
         */
        ISessionTimeManager timeManager = getModel().getTimeManager();
        view.initialize(this, new Date(timeManager.getSelectedTime()
                .getLowerBound()), timeManager.getCurrentTime(), getModel()
                .getConfigurationManager().getSettings()
                .getDefaultTimeDisplayDuration(), eventsAsDicts, getModel()
                .getConfigurationManager().getSettings(), getModel()
                .getConfigurationManager().getAvailableSettings(),
                jsonConverter.toJson(getModel().getConfigurationManager()
                        .getFilterConfig()), getModel().getAlertsManager()
                        .getActiveAlerts(), getModel().getEventManager()
                        .getEventIdsAllowingUntilFurtherNotice(),
                temporalControlsInToolBar);
    }

    @Override
    protected final void reinitialize(IConsoleView<?, ?> view) {

        /*
         * No action.
         */
    }

    // Private Methods

    private List<Dict> adaptEventsForDisplay() {
        Collection<ObservedHazardEvent> currentEvents = getModel()
                .getEventManager().getEventsForCurrentSettings();

        DeprecatedEvent[] jsonEvents = DeprecatedUtilities
                .eventsAsJSONEvents(currentEvents);
        DeprecatedUtilities.adaptJSONEvent(jsonEvents, currentEvents,
                getModel().getConfigurationManager(), getModel()
                        .getTimeManager());
        List<Dict> result = new ArrayList<>();
        Iterator<ObservedHazardEvent> eventsIterator = currentEvents.iterator();
        for (DeprecatedEvent event : jsonEvents) {
            Dict dict = Dict.getInstance(jsonConverter.toJson(event));

            /*
             * Longs are being converted to doubles unintentionally by this
             * adaptation, so convert them back.
             */
            dict.put(HazardConstants.HAZARD_EVENT_START_TIME, ((Number) dict
                    .get(HazardConstants.HAZARD_EVENT_START_TIME)).longValue());
            dict.put(HazardConstants.HAZARD_EVENT_END_TIME, ((Number) dict
                    .get(HazardConstants.HAZARD_EVENT_END_TIME)).longValue());
            if (dict.containsKey(HazardConstants.RISE_ABOVE)) {
                dict.put(HazardConstants.RISE_ABOVE, ((Number) dict
                        .get(HazardConstants.RISE_ABOVE)).longValue());
                dict.put(HazardConstants.CREST,
                        ((Number) dict.get(HazardConstants.CREST)).longValue());
                dict.put(HazardConstants.FALL_BELOW, ((Number) dict
                        .get(HazardConstants.FALL_BELOW)).longValue());
            }

            /*
             * Add the river mile to the dictionary, if it is present.
             */
            ObservedHazardEvent originalEvent = eventsIterator.next();
            Object riverMile = originalEvent.getHazardAttribute(RIVER_MILE);
            if (riverMile != null) {
                dict.put(RIVER_MILE, riverMile);
            }

            result.add(dict);
        }
        return result;
    }

    private void updateHazardEventsForSettingChange() {
        List<Dict> eventsAsDicts = adaptEventsForDisplay();
        getView().setHazardEvents(eventsAsDicts,
                getModel().getConfigurationManager().getSettings());
    }

    private void updateHazardEventsForEventChange() {

        List<Dict> eventsAsDicts = adaptEventsForDisplay();

        /*
         * Get the hazard events as shown by the view, and compare them to the
         * hazard events in the model. If the number of events and their
         * identifiers are unchanged, then one or more events have had their
         * parameters changed; otherwise, the event set as a whole has changed.
         */
        List<Dict> oldEvents = getView().getHazardEvents();
        Map<String, Dict> oldEventsMap = null;
        Map<String, Dict> newEventsMap = null;
        boolean eventsListUnchanged = (eventsAsDicts.size() == oldEvents.size());
        if (eventsListUnchanged) {
            oldEventsMap = new HashMap<>();
            newEventsMap = new HashMap<>();
            for (int j = 0; j < eventsAsDicts.size(); j++) {
                Dict event = oldEvents.get(j);
                oldEventsMap.put((String) event
                        .get(HazardConstants.HAZARD_EVENT_IDENTIFIER), event);
                event = eventsAsDicts.get(j);
                newEventsMap.put((String) event
                        .get(HazardConstants.HAZARD_EVENT_IDENTIFIER), event);
            }
            if (!oldEventsMap.keySet().equals(newEventsMap.keySet())) {
                eventsListUnchanged = false;
            }
        }

        /*
         * If one or more events have had their parameters changed, iterate
         * through them and determine which have been changed, and update the
         * view accordingly; otherwise, update the view's entire list.
         */
        if (eventsListUnchanged) {
            for (String identifier : oldEventsMap.keySet()) {
                if (!oldEventsMap.get(identifier).equals(
                        newEventsMap.get(identifier))) {
                    getView().updateHazardEvent(
                            newEventsMap.get(identifier).toJSONString());
                }
            }
        } else {
            getView().setHazardEvents(eventsAsDicts,
                    getModel().getConfigurationManager().getSettings());
        }
    }

    /*
     * TODO It's not at all clear that all of these handlers are needed. Some
     * optimization is needed here. This requires completely understanding the
     * eventing in Hazard Services; a fairly time consuming process that will be
     * done when Red-Mine 3975 is completed.
     */
    @Handler
    public void alertsModified(HazardAlertsModified notification) {
        getView().setActiveAlerts(notification.getActiveAlerts());
    }

    @Handler
    public void sessionSelectedEventsModified(
            SessionSelectedEventsModified notification) {
        updateHazardEventsForEventChange();
    }

    @Handler
    public void sessionEventAttributesModified(
            SessionEventAttributesModified notification) {
        updateHazardEventsForEventChange();
    }

    @Handler
    public void sessionEventRemoved(SessionEventRemoved notification) {
        updateHazardEventsForEventChange();
    }

    @Handler
    public void sessionEventTimeRangeModified(
            SessionEventTimeRangeModified notification) {
        updateHazardEventsForEventChange();
    }

    @Handler
    public void sessionEventTypeModified(SessionEventTypeModified notification) {
        updateHazardEventsForEventChange();
    }

    /*
     * TODO In particular, it did not seem to Dan that these are needed but Dan
     * could be wrong.
     */
    @Handler
    public void sessionEventAdded(SessionEventAdded notification) {
        updateHazardEventsForEventChange();
    }

    @Handler
    public void sessionEventStatusModified(
            SessionEventStatusModified notification) {
        updateHazardEventsForEventChange();
    }

}

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

import gov.noaa.gsd.viz.hazards.display.HazardServicesPresenter;
import gov.noaa.gsd.viz.hazards.display.deprecated.DeprecatedUtilities;
import gov.noaa.gsd.viz.hazards.jsonutilities.DeprecatedEvent;
import gov.noaa.gsd.viz.hazards.jsonutilities.Dict;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.TimeRange;
import com.raytheon.uf.viz.hazards.sessionmanager.ISessionManager;
import com.raytheon.uf.viz.hazards.sessionmanager.alerts.HazardAlertsModified;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Console;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.StartUpConfig;
import com.raytheon.uf.viz.hazards.sessionmanager.events.impl.ObservedHazardEvent;

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

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param model
     *            Model to be handled by this presenter.
     * @param view
     *            Console view to be handled by this presenter.
     * @param eventBus
     *            Event bus used to signal changes.
     */
    public ConsolePresenter(ISessionManager<ObservedHazardEvent> model,
            IConsoleView<?, ?> view, EventBus eventBus) {
        super(model, view, eventBus);
    }

    // Public Methods

    @Override
    public final void modelChanged(EnumSet<HazardConstants.Element> changed) {
        if (changed.contains(HazardConstants.Element.CURRENT_TIME)) {
            getView().updateCurrentTime(timeManager.getCurrentTime());
        }
        if (changed.contains(HazardConstants.Element.SELECTED_TIME)) {
            getView().updateSelectedTime(timeManager.getSelectedTime());
        }
        if (changed.contains(HazardConstants.Element.SELECTED_TIME_RANGE)) {
            TimeRange range = getModel().getTimeManager()
                    .getSelectedTimeRange();
            String updateTimeRange = jsonConverter.toJson(new String[] {
                    jsonConverter.fromDate(range.getStart()),
                    jsonConverter.fromDate(range.getEnd()) });
            getView().updateSelectedTimeRange(updateTimeRange);
        }
        if (changed.contains(HazardConstants.Element.VISIBLE_TIME_DELTA)) {
            String timeDelta = Long.toString(getModel()
                    .getConfigurationManager().getSettings()
                    .getDefaultTimeDisplayDuration());
            getView().updateVisibleTimeDelta(timeDelta);
        }
        if (changed.contains(HazardConstants.Element.VISIBLE_TIME_RANGE)) {
            String earliestTime = jsonConverter.fromDate(timeManager
                    .getVisibleRange().getStart());
            String latestTime = jsonConverter.fromDate(timeManager
                    .getVisibleRange().getEnd());
            getView().updateVisibleTimeRange(earliestTime, latestTime);
        }
        if (changed.contains(HazardConstants.Element.SETTINGS)) {
            getView().setSettings(
                    configurationManager.getSettings().getSettingsID(),
                    configurationManager.getAvailableSettings());
        }
        if (changed.contains(HazardConstants.Element.CURRENT_SETTINGS)) {
            updateHazardEventsForSettingChange();
        }
        if (changed.contains(HazardConstants.Element.EVENTS)) {
            updateHazardEventsForEventChange();
        }
        if (changed.contains(HazardConstants.Element.SITE)) {
            view.updateTitle(configurationManager.getSiteID());
        }
    }

    @Override
    public void dispose() {
        getModel().unregisterForNotification(this);
    }

    // Protected Methods

    @Override
    public final void initialize(IConsoleView<?, ?> view) {

        /**
         * TODO Can this be pushed into the super class? If so then all
         * subclasses would have to do that otherwise double registration will
         * occur.
         */
        eventBus.register(this);

        /*
         * Determine whether the time line navigation buttons should be in the
         * console toolbar, or below the console's table.
         */
        boolean temporalControlsInToolBar = true;
        StartUpConfig startUpConfig = configurationManager.getStartUpConfig();
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
        view.initialize(this, timeManager.getSelectedTime(), timeManager
                .getCurrentTime(), configurationManager.getSettings()
                .getDefaultTimeDisplayDuration(), eventsAsDicts,
                configurationManager.getSettings(), configurationManager
                        .getAvailableSettings(), jsonConverter
                        .toJson(configurationManager.getFilterConfig()),
                alertsManager.getActiveAlerts(), eventManager
                        .getEventIdsAllowingUntilFurtherNotice(),
                temporalControlsInToolBar);
    }

    private List<Dict> adaptEventsForDisplay() {
        Collection<ObservedHazardEvent> currentEvents = eventManager
                .getEventsForCurrentSettings();

        DeprecatedEvent[] jsonEvents = DeprecatedUtilities
                .eventsAsJSONEvents(currentEvents);
        DeprecatedUtilities.adaptJSONEvent(jsonEvents, currentEvents,
                configurationManager, timeManager);
        List<Dict> result = new ArrayList<>();
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
            result.add(dict);
        }
        return result;
    }

    // Private Methods

    private void updateHazardEventsForSettingChange() {
        List<Dict> eventsAsDicts = adaptEventsForDisplay();

        /**
         * Optimization. Only update the view of the hazard events if the
         * settings are changed by a different component. If it was by this
         * component, then the view current settings and the
         * configurationManager current settings will be identical so no need to
         * update the view. This optimization is particularly useful when you
         * sort the console rows!!!
         * 
         * TODO An optimization that was here needs to be resurrected. Need to
         * take another approach; probably by including the originator in the
         * {@link CurrentSettingsAction}. The code would skip call if the
         * originator is the Console.
         */

        getView().setHazardEvents(eventsAsDicts,
                configurationManager.getSettings());
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
                    configurationManager.getSettings());
        }
    }

    @Subscribe
    public void alertsModified(HazardAlertsModified notification) {
        view.setActiveAlerts(notification.getActiveAlerts());
    }
}

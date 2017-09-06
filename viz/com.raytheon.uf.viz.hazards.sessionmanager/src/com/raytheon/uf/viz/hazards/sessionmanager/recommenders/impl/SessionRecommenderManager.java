/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package com.raytheon.uf.viz.hazards.sessionmanager.recommenders.impl;

import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HAZARD_AREA;
import gov.noaa.gsd.common.eventbus.BoundedReceptionEventBus;
import gov.noaa.gsd.common.visuals.VisualFeaturesList;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.engio.mbassy.listener.Handler;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.raytheon.uf.common.dataplugin.events.EventSet;
import com.raytheon.uf.common.dataplugin.events.IEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardStatus;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.Trigger;
import com.raytheon.uf.common.dataplugin.events.hazards.event.BaseHazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardEventUtilities;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.registry.HazardEventServiceException;
import com.raytheon.uf.common.python.concurrent.IPythonJobListener;
import com.raytheon.uf.common.recommenders.AbstractRecommenderEngine;
import com.raytheon.uf.common.recommenders.EventRecommender;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.DataTime;
import com.raytheon.uf.viz.core.VizApp;
import com.raytheon.uf.viz.core.drawables.IDescriptor.FramesInfo;
import com.raytheon.uf.viz.core.localization.LocalizationManager;
import com.raytheon.uf.viz.hazards.sessionmanager.ISessionManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.ISessionConfigurationManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.SiteChanged;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.ObservedSettings;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.ToolType;
import com.raytheon.uf.viz.hazards.sessionmanager.events.ISessionEventManager;
import com.raytheon.uf.viz.hazards.sessionmanager.events.impl.ObservedHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.messenger.IMessenger;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.IOriginator;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.RecommenderOriginator;
import com.raytheon.uf.viz.hazards.sessionmanager.recommenders.ISessionRecommenderManager;
import com.raytheon.uf.viz.hazards.sessionmanager.recommenders.RecommenderExecutionContext;
import com.raytheon.uf.viz.hazards.sessionmanager.time.SelectedTime;
import com.raytheon.uf.viz.recommenders.CAVERecommenderEngine;
import com.raytheon.viz.core.mode.CAVEMode;
import com.vividsolutions.jts.geom.Coordinate;

/**
 * Description: Manager of recommenders and their execution.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Oct 27, 2015   12762    Chris.Golden Initial creation.
 * Jan 28, 2016   12762    Chris.Golden Changed to use new attribute identifiers
 *                                      constant.
 * Feb 24, 2016   14667    Robert.Blum  Limiting Flash Flood Recommender to basins
 *                                      inside the CWA.
 * Feb 25, 2016   14740    kbisanz      Add default FRAME_INDEX and FRAME_COUNT to
 *                                      frameMap
 * Mar 03, 2016   14004    Chris.Golden Changed to pass recommender identifier to
 *                                      the method handling recommender results.
 * Mar 04, 2016   15933    Chris.Golden Added ability to run multiple recommenders
 *                                      in sequence in response to a time interval
 *                                      trigger, instead of just one recommender.
 * Mar 06, 2016   15676    Chris.Golden Added more contextual information for
 *                                      recommender triggering, and changed the
 *                                      the recommender input EventSet to only
 *                                      include the events the recommender desires.
 * Mar 14, 2016   12145    mduff        Handle error thrown by event manager.
 * Mar 31, 2016    8837    Robert.Blum  Changes for Service Backup.
 * Apr 27, 2016   18266    Chris.Golden Added the inclusion of the latest data time
 *                                      in the recommender input event set if asked
 *                                      for by the recommender.
 * May 02, 2016   18235    Chris.Golden Marked any events added or modified by
 *                                      recommennders as from recommenders if the
 *                                      recommender that created them wanted their
 *                                      origins set.
 * May 03, 2016   18376    Chris.Golden Changed to support reuse of Jep instance
 *                                      between H.S. sessions in the same CAVE
 *                                      session, since stopping and starting the
 *                                      Jep instances when the latter use numpy is
 *                                      dangerous.
 * May 06, 2016   18202    Robert.Blum  Changes for operational mode.
 * May 10, 2016   18240    Chris.Golden Added ability to specify arbitrary extra
 *                                      event set attributes as part of the context
 *                                      in which a recommender execution is
 *                                      occurring.
 * May 18, 2016   17342    Ben.Phillippe Passing both site identifier and localized
 *                                      site identifier to recommenders.
 * Jun 23, 2016   19537    Chris.Golden Changed to use visual features for spatial
 *                                      info collection. Also changed to not notify
 *                                      the user of a recommender not producing any
 *                                      recommendations if the recommender is run
 *                                      automatically in response to an event
 *                                      changing, etc.
 * Jul 25, 2016   19537    Chris.Golden Fixed bug that manifested itself when a
 *                                      null dialog info was provided by a
 *                                      recommender, causing an exception.
 * Jul 27, 2016   19924    Chris.Golden Changed to pass all data layer times to
 *                                      a recommender when requested, not just the
 *                                      latest data layer.
 * Aug 15, 2016   18376    Chris.Golden Added code to make garbage collection of the
 *                                      messenger instance passed in (which is the
 *                                      app builder) more likely.
 * Feb 01, 2017   15556    Chris.Golden Minor cleanup.
 * Feb 21, 2017   29138    Chris.Golden Added use of session manager's runnable
 *                                      asynchronous scheduler.
 * Apr 13, 2017   33142    Chris.Golden Added use of new method in session manager to
 *                                      clear the set of identifiers of events that
 *                                      have been removed when initiating recommender
 *                                      execution.
 * May 31, 2017   34684    Chris.Golden Moved recommender-specific methods to the
 *                                      session recommender manager where they belong.
 *                                      Also added support for executing recommenders
 *                                      due to hazard event selection changes.
 * Aug 15, 2017   37426    Chris.Golden Added ability for fine-grained control of
 *                                      setOrigin (per hazard event in result event set).
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class SessionRecommenderManager implements ISessionRecommenderManager {

    // Private Static Constants

    /**
     * Maximum size of the
     * {@link #sequentialRecommendersForExecutionContextIdentifiers} map.
     */
    private static final int MAXIMUM_SEQUENTIAL_RECOMMENDERS_MAP_SIZE = 100;

    // Private Static Variables

    /**
     * Status handler.
     */
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(SessionRecommenderManager.class);

    // Private Variables

    /**
     * Session manager.
     */
    private final ISessionManager<ObservedHazardEvent, ObservedSettings> sessionManager;

    /**
     * Messenger used to communicate with the user.
     */
    private IMessenger messenger;

    /**
     * Engine to be used to actually run recommenders.
     */
    private final AbstractRecommenderEngine<?> recommenderEngine;

    /**
     * Set of event identifiers for those events that have been completely
     * removed since the commencement of the last recommender execution. As
     * events are removed, they are added to this set, and then when a
     * recommender is run, this set is emptied before it starts. When a
     * recommender completes execution and returns an event set, any events
     * included in the latter are checked against this set to ensure that they
     * were not removed for some other reason while the recommender was running,
     * and any that were removed are ignored.
     */
    private final Set<String> identifiersOfEventsRemovedSinceLastRecommenderRun = new HashSet<>();

    /**
     * <p>
     * Map of recommender execution context identifiers to lists of recommenders
     * that are to be run sequentially in the order they are specified in the
     * list, with each one waiting for the previous one to complete before
     * running. Note that only execution contexts that are to be used for two or
     * more recommenders specified via
     * {@link #runRecommenders(List, RecommenderExecutionContext)} have entries
     * in this map; no entries are created for execution contexts associated
     * with the running of just one recommender.
     * </p>
     * <p>
     * It is impossible to guarantee that an entry in this map will be deleted
     * when its recommenders have been run, since any interruption in running
     * any of the recommenders (e.g. canceling an input dialog for a recommender
     * in the list) will cause the corresponding entry in the map to never get
     * deleted, since no notification of the failure to complete the sequential
     * running will be provided to this manager. Thus, this is implemented as a
     * map with a set maximum size, which deletes the oldest entry whenever it
     * reaches that size and needs to store a new entry. The number of entries
     * that may be stored simultaneously is
     * {@link #MAXIMUM_SEQUENTIAL_RECOMMENDERS_MAP_SIZE}.
     * </p>
     */
    private final Map<Long, List<String>> sequentialRecommendersForExecutionContextIdentifiers = new LinkedHashMap<Long, List<String>>(
            MAXIMUM_SEQUENTIAL_RECOMMENDERS_MAP_SIZE + 1, 0.75f, true) {

        // Private Static Constants

        /**
         * Serial version UID.
         */
        private static final long serialVersionUID = 1L;

        // Protected Methods

        @Override
        protected final boolean removeEldestEntry(
                Map.Entry<Long, List<String>> eldest) {
            return (size() > MAXIMUM_SEQUENTIAL_RECOMMENDERS_MAP_SIZE);
        }
    };

    // Constructors

    /**
     * Construct a standard instance.
     * 
     * @param sessionManager
     *            Session manager that is building this manager.
     * @param messenger
     *            Messenger used to communicate with the user.
     * @param eventBus
     *            Event bus to be used for notifications.
     */
    public SessionRecommenderManager(
            ISessionManager<ObservedHazardEvent, ObservedSettings> sessionManager,
            IMessenger messenger, BoundedReceptionEventBus<Object> eventBus) {
        this.sessionManager = sessionManager;
        this.messenger = messenger;
        recommenderEngine = CAVERecommenderEngine.getInstance();
        recommenderEngine.setSite(sessionManager.getConfigurationManager()
                .getSiteID());
        eventBus.subscribe(recommenderEngine);
    }

    // Public Methods

    /**
     * Respond to the current site changing.
     * 
     * @param change
     *            Change that occurred.
     */
    @Handler(priority = 1)
    public void siteChanged(SiteChanged change) {
        recommenderEngine.setSite(sessionManager.getConfigurationManager()
                .getSiteID());
    }

    @Override
    public EventRecommender getRecommender(String recommenderIdentifier) {
        return recommenderEngine.getInventory(recommenderIdentifier);
    }

    @Override
    public void rememberRemovedEventIdentifier(String eventIdentifier) {
        identifiersOfEventsRemovedSinceLastRecommenderRun.add(eventIdentifier);
    }

    @Override
    public void runRecommender(String recommenderIdentifier,
            RecommenderExecutionContext context) {

        /*
         * Create the event set to be used, and add the execution context to it.
         */
        EventSet<IEvent> eventSet = new EventSet<>();
        eventSet.addAttribute(HazardConstants.CENTER_POINT_LAT_LON,
                getCenterPointAsDictionary());
        addContextAsEventSetAttributes(context, eventSet);

        /*
         * Determine whether or not any spatial or dialog input is required; if
         * either or both are, request the appropriate input. Spatial input is
         * taken by displaying one or more visual features for the user, and
         * waiting for one of them to be manipulated in some way; dialog input
         * is taken by putting up a dialog and waiting for the user to proceed.
         * Otherwise, just run the recommender.
         */
        VisualFeaturesList visualFeatures = recommenderEngine.getSpatialInfo(
                recommenderIdentifier, eventSet);
        Map<String, Serializable> dialogDescription = recommenderEngine
                .getDialogInfo(recommenderIdentifier, eventSet);
        if (((visualFeatures != null) && (visualFeatures.isEmpty() == false))
                || ((dialogDescription != null) && (dialogDescription.isEmpty() == false))) {
            if ((visualFeatures != null) && (visualFeatures.isEmpty() == false)) {
                messenger.getToolParameterGatherer().getToolSpatialInput(
                        recommenderIdentifier, ToolType.RECOMMENDER, context,
                        visualFeatures);
            }
            if ((dialogDescription != null)
                    && (dialogDescription.isEmpty() == false)) {
                dialogDescription.put(HazardConstants.FILE_PATH_KEY,
                        recommenderEngine.getInventory(recommenderIdentifier)
                                .getFile().getFile().getPath());
                messenger.getToolParameterGatherer().getToolParameters(
                        recommenderIdentifier, ToolType.RECOMMENDER, context,
                        dialogDescription);
            }
        } else {
            runRecommender(recommenderIdentifier, context, null, null);
        }
    }

    @Override
    public void runRecommenders(List<String> recommenderIdentifiers,
            RecommenderExecutionContext context) {
        if (recommenderIdentifiers.size() > 1) {
            sequentialRecommendersForExecutionContextIdentifiers.put(
                    context.getIdentifier(), recommenderIdentifiers);
        }
        runRecommender(recommenderIdentifiers.get(0), context);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void runRecommender(final String recommenderIdentifier,
            final RecommenderExecutionContext context,
            VisualFeaturesList visualFeatures,
            Map<String, Serializable> dialogInfo) {

        /*
         * Get the recommender metadata, and decide what events are to be
         * included in the event set based upon its values.
         */
        Map<String, Serializable> metadata = recommenderEngine
                .getScriptMetadata(recommenderIdentifier);
        Boolean onlyIncludeTriggerEvent = (Boolean) metadata
                .get(HazardConstants.RECOMMENDER_METADATA_ONLY_INCLUDE_TRIGGER_EVENTS);
        List<String> includeEventTypesList = (List<String>) metadata
                .get(HazardConstants.RECOMMENDER_METADATA_INCLUDE_EVENT_TYPES);
        Set<String> includeEventTypes = (includeEventTypesList != null ? new HashSet<>(
                includeEventTypesList) : null);
        Boolean includeDataLayerTimes = (Boolean) metadata
                .get(HazardConstants.RECOMMENDER_METADATA_INCLUDE_DATA_LAYER_TIMES);
        Boolean includeCwaGeometry = (Boolean) metadata
                .get(HazardConstants.RECOMMENDER_METADATA_INCLUDE_CWA_GEOMETRY);

        /*
         * Create the event set, determine which events are to be added to it
         * based upon the recommender metadata retrieved above, and add a copy
         * of each such event to the set.
         */
        EventSet<IEvent> eventSet = new EventSet<>();
        if (Boolean.TRUE.equals(onlyIncludeTriggerEvent)
                && ((context.getTrigger() == Trigger.HAZARD_EVENT_VISUAL_FEATURE_CHANGE)
                        || (context.getTrigger() == Trigger.HAZARD_EVENT_MODIFICATION) || (context
                        .getTrigger() == Trigger.HAZARD_EVENT_SELECTION))) {
            for (String eventIdentifier : context.getEventIdentifiers()) {
                eventSet.add(createBaseHazardEvent(sessionManager
                        .getEventManager().getEventById(eventIdentifier)));
            }
        } else {
            Collection<ObservedHazardEvent> hazardEvents = sessionManager
                    .getEventManager().getEvents();
            for (ObservedHazardEvent event : hazardEvents) {
                if ((includeEventTypes == null)
                        || includeEventTypes.contains(event.getHazardType())) {
                    eventSet.add(createBaseHazardEvent(event));
                }
            }
        }

        /*
         * Clear the set of removed event identifiers, since it should only hold
         * identifiers of events that have been removed since the last
         * commencement of a recommender's execution, and said commencement is
         * about to occur.
         */
        identifiersOfEventsRemovedSinceLastRecommenderRun.clear();

        /*
         * Add the execution context parameters to the event set.
         */
        addContextAsEventSetAttributes(context, eventSet);

        /*
         * Add session information to event set.
         */
        long currentTime = sessionManager.getTimeManager().getCurrentTime()
                .getTime();
        eventSet.addAttribute(HazardConstants.CURRENT_TIME, currentTime);
        eventSet.addAttribute(HazardConstants.SELECTED_TIME, sessionManager
                .getTimeManager().getSelectedTime().getLowerBound());
        eventSet.addAttribute(HazardConstants.FRAMES_INFO,
                getFramesInfoAsDictionary());

        /*
         * If the data times are to be included, add them to the event set as
         * well, using a list of just the current time if none are available.
         */
        final String toolName = (String) metadata
                .get(HazardConstants.RECOMMENDER_METADATA_TOOL_NAME);
        if (Boolean.TRUE.equals(includeDataLayerTimes)) {
            List<Long> dataLayerTimes = sessionManager
                    .getDisplayResourceContextProvider()
                    .getTimeMatchBasisDataLayerTimes();
            Serializable times = (dataLayerTimes == null ? Lists
                    .newArrayList(currentTime)
                    : (dataLayerTimes instanceof Serializable ? (Serializable) dataLayerTimes
                            : new ArrayList<>(dataLayerTimes)));
            eventSet.addAttribute(HazardConstants.DATA_TIMES, times);
        }
        if (Boolean.TRUE.equals(includeCwaGeometry)) {
            eventSet.addAttribute(HazardConstants.CWA_GEOMETRY, sessionManager
                    .getEventManager().getCwaGeometry());
        }

        /*
         * Get the engine to initiate the execution of the recommender.
         */
        Boolean background = (Boolean) metadata
                .get(HazardConstants.RECOMMENDER_METADATA_BACKGROUND);
        final boolean notify = ((Boolean.TRUE.equals(background) == false) && ((context
                .getTrigger() == Trigger.NONE) || (context.getTrigger() == Trigger.HAZARD_TYPE_FIRST)));
        recommenderEngine.runExecuteRecommender(recommenderIdentifier,
                eventSet, visualFeatures, dialogInfo,
                new IPythonJobListener<EventSet<IEvent>>() {

                    @Override
                    public void jobFinished(final EventSet<IEvent> result) {

                        /*
                         * Ensure that the thread used to process the result is
                         * the one used by the session manager.
                         */
                        sessionManager.getRunnableAsynchronousScheduler()
                                .schedule(new Runnable() {

                                    @Override
                                    public void run() {

                                        /*
                                         * If no events were recommended and
                                         * notification of such should occur,
                                         * then notify the user.
                                         */
                                        if (notify && result.isEmpty()) {
                                            messenger
                                                    .getWarner()
                                                    .warnUser(
                                                            toolName,
                                                            toolName
                                                                    + " completed. "
                                                                    + "No recommendations "
                                                                    + "were generated.");
                                        }

                                        /*
                                         * Handle the resulting changes to the
                                         * session.
                                         */
                                        handleRecommenderResult(
                                                recommenderIdentifier, result);

                                        /*
                                         * If this recommender is being run as
                                         * part of a list of recommenders to be
                                         * run sequentially, then run the next
                                         * one, and if the next one is the last
                                         * one in the sequence, remove the list
                                         * from the sequential-recommenders map
                                         * since it will no longer be
                                         * referenced.
                                         */
                                        long contextIdentifier = context
                                                .getIdentifier();
                                        List<String> sequentialRecommenders = sequentialRecommendersForExecutionContextIdentifiers
                                                .get(contextIdentifier);
                                        if (sequentialRecommenders != null) {
                                            int nextIndex = sequentialRecommenders
                                                    .indexOf(recommenderIdentifier) + 1;
                                            String nextRecommenderIdentifier = sequentialRecommenders
                                                    .get(nextIndex);
                                            if (nextIndex == sequentialRecommenders
                                                    .size() - 1) {
                                                sequentialRecommendersForExecutionContextIdentifiers
                                                        .remove(contextIdentifier);
                                            }
                                            runRecommender(
                                                    nextRecommenderIdentifier,
                                                    context);
                                        }
                                    }
                                });
                    }

                    @Override
                    public void jobFailed(Throwable e) {
                        statusHandler.error("Recommender " + toolName
                                + " failed.", e);
                    }
                });
    }

    @Override
    public void shutdown() {
        recommenderEngine.shutdownEngine();
        messenger = null;
    }

    // Private Methods

    /**
     * Create a base hazard event copy of the specified hazard event.
     * 
     * @param event
     *            Event to be copied.
     * @return Base hazard event copy.
     */
    private BaseHazardEvent createBaseHazardEvent(ObservedHazardEvent event) {
        BaseHazardEvent copy = new BaseHazardEvent(event);

        /*
         * TODO: Change recommenders so that they have a separate set of
         * selected event identifiers passed to them, instead of having
         * selection be an attribute of the individual hazards.
         */
        copy.addHazardAttribute(HazardConstants.HAZARD_EVENT_SELECTED,
                sessionManager.getSelectionManager().isSelected(event));
        return copy;
    }

    /**
     * Add the specified execution context parameters to the specified event
     * set.
     */
    private void addContextAsEventSetAttributes(
            RecommenderExecutionContext context, EventSet<IEvent> eventSet) {
        eventSet.addAttribute(HazardConstants.SITE_ID, sessionManager
                .getConfigurationManager().getSiteID());
        eventSet.addAttribute(HazardConstants.LOCALIZED_SITE_ID,
                LocalizationManager.getInstance().getSite());
        eventSet.addAttribute(HazardConstants.HAZARD_MODE, CAVEMode.getMode()
                .toString());
        eventSet.addAttribute(HazardConstants.RECOMMENDER_EXECUTION_TRIGGER,
                context.getTrigger().toString());
        eventSet.addAttribute(HazardConstants.RECOMMENDER_EVENT_TYPE,
                context.getEventType());
        eventSet.addAttribute(
                HazardConstants.RECOMMENDER_TRIGGER_EVENT_IDENTIFIERS,
                context.getEventIdentifiers());
        eventSet.addAttribute(
                HazardConstants.RECOMMENDER_TRIGGER_ATTRIBUTE_IDENTIFIERS,
                context.getAttributeIdentifiers());
        eventSet.addAttribute(HazardConstants.RECOMMENDER_TRIGGER_ORIGIN,
                context.getOrigin().toString());
        if (context.getExtraEventSetAttributes() != null) {
            for (Map.Entry<String, Serializable> entry : context
                    .getExtraEventSetAttributes().entrySet()) {
                if (eventSet.getAttribute(entry.getKey()) == null) {
                    eventSet.addAttribute(entry.getKey(), entry.getValue());
                }
            }
        }
    }

    /**
     * Get the center point in lat-lon coordinates as parameters in a
     * dictionary, in order to allow it to be passed to a recommender during
     * execution.
     * 
     * @return Dictionary holding the lat-lon coordinates. This is specified as
     *         a {@link HashMap} in order to ensure it is {@link Serializable}.
     */
    private HashMap<String, Serializable> getCenterPointAsDictionary() {
        HashMap<String, Serializable> dictionary = new HashMap<>(2, 1.0f);
        Coordinate centerPoint = sessionManager.getSpatialContextProvider()
                .getLatLonCenterPoint();
        if (centerPoint != null) {
            dictionary.put(HazardConstants.COORDINATE_LAT, centerPoint.y);
            dictionary.put(HazardConstants.COORDINATE_LON, centerPoint.x);
        }
        return dictionary;
    }

    /**
     * Get frames information as parameters in a dictionary, in order to allow
     * it to be passed to a recommender during execution.
     * 
     * @return Dictionary holding the frames information. This is specified as a
     *         {@link HashMap} in order to ensure it is {@link Serializable}.
     */
    private HashMap<String, Serializable> getFramesInfoAsDictionary() {
        HashMap<String, Serializable> dictionary = new HashMap<>();
        FramesInfo framesInfo = sessionManager.getFrameContextProvider()
                .getFramesInfo();
        if (framesInfo != null) {
            dictionary.put(HazardConstants.CURRENT_FRAME,
                    framesInfo.getCurrentFrame());
            dictionary.put(HazardConstants.FRAME_COUNT, 0);
            dictionary.put(HazardConstants.FRAME_INDEX,
                    HazardConstants.NO_FRAMES_INDEX);
            int frameIndex = framesInfo.getFrameIndex();
            DataTime[] dataFrames = framesInfo.getFrameTimes();
            if (frameIndex >= 0) {
                ArrayList<Long> dataTimeList = Lists.newArrayList();
                if (dataFrames != null) {
                    for (DataTime dataTime : dataFrames) {
                        Calendar cal = dataTime.getValidTime();
                        dataTimeList.add(cal.getTimeInMillis());
                    }
                }
                dictionary.put(HazardConstants.FRAME_COUNT,
                        framesInfo.getFrameCount());
                dictionary.put(HazardConstants.FRAME_INDEX, frameIndex);
                dictionary.put(HazardConstants.FRAME_TIMES, dataTimeList);
            }
        }
        return dictionary;
    }

    /**
     * Handle the result of a recommender run.
     * 
     * @param recommenderIdentifier
     *            Identifier of the recommender that ran.
     * @param events
     *            Set of events that were created or modified by the
     *            recommender.
     */
    private void handleRecommenderResult(String recommenderIdentifier,
            EventSet<IEvent> events) {

        /*
         * If an event set was returned by the recommender as a result, ingest
         * the events and respond to any attributes included within the set.
         */
        if (events != null) {
            ISessionEventManager<ObservedHazardEvent> eventManager = sessionManager
                    .getEventManager();

            /*
             * Get the attributes of the event set indicating whether any or all
             * events are to be saved to history lists or to the latest version
             * set, and determine if said attributes indicate that all events
             * provided in the event set should be saved in one of the two ways,
             * if specific ones should be saved one or both ways, or none of
             * them should be saved. Do the same for the treating of events that
             * are to be saved either way as issuances.
             */
            Serializable addToHistoryAttribute = events
                    .getAttribute(HazardConstants.RECOMMENDER_RESULT_SAVE_TO_HISTORY);
            Serializable addToDatabaseAttribute = events
                    .getAttribute(HazardConstants.RECOMMENDER_RESULT_SAVE_TO_DATABASE);
            boolean addToHistory = Boolean.TRUE.equals(addToHistoryAttribute);
            boolean addToDatabase = ((addToHistory == false) && Boolean.TRUE
                    .equals(addToDatabaseAttribute));

            /*
             * Determine whether or not the events that are to be saved to the
             * history list and/or to the latest version set are to be treated
             * as issuances.
             */
            boolean treatAsIssuance = Boolean.TRUE
                    .equals(events
                            .getAttribute(HazardConstants.RECOMMENDER_RESULT_TREAT_AS_ISSUANCE));

            /*
             * Determine whether or not the events should have their user name,
             * workstation, and source set. This defaults to true. A boolean may
             * be provided, meaning that all events will have their origin set
             * (or none), or a map of hazard event identifiers to booleans may
             * be given instead, allowing a more fine-grained approach.
             */
            Set<String> eventIdentifiersNeedingOriginSet = new HashSet<>(
                    events.size(), 1.0f);
            Serializable setOriginObj = events
                    .getAttribute(HazardConstants.RECOMMENDER_RESULT_SET_ORIGIN);
            if (Boolean.FALSE.equals(setOriginObj) == false) {
                for (IEvent event : events) {
                    if (event instanceof IHazardEvent) {
                        String eventIdentifier = ((IHazardEvent) event)
                                .getEventID();
                        if (eventIdentifier != null) {
                            eventIdentifiersNeedingOriginSet
                                    .add(((IHazardEvent) event).getEventID());
                        }
                    }
                }
                if (setOriginObj instanceof Map<?, ?>) {
                    Map<?, ?> setOriginFlagsForEventIdentifiers = (Map<?, ?>) setOriginObj;
                    for (Map.Entry<?, ?> entry : setOriginFlagsForEventIdentifiers
                            .entrySet()) {
                        if (Boolean.FALSE.equals(entry.getValue())) {
                            eventIdentifiersNeedingOriginSet.remove(entry
                                    .getKey());
                        }
                    }
                }
            }

            /*
             * Determine whether or not all hazard events that are brand new
             * (i.e., just created by the recommender) should be saved to either
             * the history list or the database.
             */
            boolean saveAllNewToHistory = isListContainingNullElement(addToHistoryAttribute);
            boolean saveAllNewToDatabase = ((saveAllNewToHistory == false) && isListContainingNullElement(addToDatabaseAttribute));
            List<IHazardEvent> addedNewEvents = (saveAllNewToHistory
                    || saveAllNewToDatabase ? new ArrayList<IHazardEvent>()
                    : null);

            /*
             * Create a list to hold the events to be saved if all events are
             * specified as requiring saving. If instead specific events are
             * specified that are to be saved one or both ways, create a map
             * that will be used to pair the identifiers of events that are
             * created with the events themselves.
             */
            List<IHazardEvent> addedEvents = (addToHistory || addToDatabase ? new ArrayList<IHazardEvent>(
                    events.size()) : null);
            Map<String, IHazardEvent> addedEventsForIdentifiers = ((addedEvents == null)
                    && ((addToHistoryAttribute instanceof List) || (addToDatabaseAttribute instanceof List)) ? new HashMap<String, IHazardEvent>(
                    events.size(), 1.0f) : null);

            /*
             * If a list of event identifiers for which the events are to be
             * deleted was provided, remember any (and their associated events)
             * that are for events that are found to exist and to never have
             * been issued.
             */
            Object toBeDeleted = events
                    .getAttribute(HazardConstants.RECOMMENDER_RESULT_DELETE_EVENT_IDENTIFIERS);
            Set<String> identifiersOfEventsToBeDeleted = null;
            IOriginator originator = new RecommenderOriginator(
                    recommenderIdentifier);
            if (toBeDeleted != null) {
                if (toBeDeleted instanceof Collection == false) {
                    statusHandler
                            .warn("Ignoring "
                                    + recommenderIdentifier
                                    + " result event set attribute \""
                                    + HazardConstants.RECOMMENDER_RESULT_DELETE_EVENT_IDENTIFIERS
                                    + "\" because it is not a list of event identifiers.");
                } else {

                    /*
                     * Iterate through the elements of the provided collection,
                     * remembering any events for which the identifiers are
                     * valid and that have never been issued.
                     */
                    identifiersOfEventsToBeDeleted = new HashSet<>(
                            ((Collection<?>) toBeDeleted).size(), 1.0f);
                    List<ObservedHazardEvent> eventsToBeDeleted = new ArrayList<>(
                            ((Collection<?>) toBeDeleted).size());
                    for (Object element : (Collection<?>) toBeDeleted) {
                        boolean success = false;
                        if (element instanceof String) {
                            ObservedHazardEvent event = eventManager
                                    .getEventById((String) element);
                            if ((event != null)
                                    && (HazardStatus.hasEverBeenIssued(event
                                            .getStatus()) == false)) {
                                identifiersOfEventsToBeDeleted.add(event
                                        .getEventID());
                                eventsToBeDeleted.add(event);
                                success = true;
                            }
                        }
                        if (success == false) {
                            statusHandler
                                    .warn("Ignoring "
                                            + recommenderIdentifier
                                            + " result event set attribute \""
                                            + HazardConstants.RECOMMENDER_RESULT_DELETE_EVENT_IDENTIFIERS
                                            + "\" list element \""
                                            + element
                                            + "\" because it is not an existing, never-issued event identifier.");
                        }
                    }

                    /*
                     * If there are any events that are to be deleted, delete
                     * them now.
                     */
                    if (eventsToBeDeleted.isEmpty() == false) {
                        eventManager
                                .removeEvents(eventsToBeDeleted, originator);
                    }
                }
            } else {
                identifiersOfEventsToBeDeleted = Collections.emptySet();
            }

            /*
             * Iterate through the hazard events provided as the result, adding
             * hazard warning areas for each, setting their user name and
             * workstation if appropriate, and then telling the event manager to
             * add them.
             */
            List<ObservedHazardEvent> addedOrModifiedEvents = new ArrayList<>(
                    events.size());
            for (IEvent event : events) {
                if (event instanceof IHazardEvent) {

                    /*
                     * Get the hazard event, and if it is not new, ensure that
                     * it does not have the identifier of an event that was
                     * removed by some other action while this recommender was
                     * running. If it was removed during recommender execution,
                     * ignore it, as it should not be around anymore. Also
                     * ignore it if it is to be deleted per this recommender's
                     * request.
                     */
                    IHazardEvent hazardEvent = (IHazardEvent) event;
                    boolean isNew = (hazardEvent.getEventID() == null);
                    if ((isNew == false)
                            && (identifiersOfEventsRemovedSinceLastRecommenderRun
                                    .contains(hazardEvent.getEventID()) || identifiersOfEventsToBeDeleted
                                    .contains(hazardEvent.getEventID()))) {
                        continue;
                    }

                    /*
                     * Add the hazard area for the event, and if the recommender
                     * wants the origin set for this particular hazard event, do
                     * so now.
                     */
                    Map<String, String> ugcHatchingAlgorithms = eventManager
                            .buildInitialHazardAreas(hazardEvent);
                    hazardEvent.addHazardAttribute(HAZARD_AREA,
                            (Serializable) ugcHatchingAlgorithms);
                    if (isNew
                            || eventIdentifiersNeedingOriginSet
                                    .contains(hazardEvent.getEventID())) {
                        hazardEvent.setUserName(LocalizationManager
                                .getInstance().getCurrentUser());
                        hazardEvent.setWorkStation(VizApp.getHostName());
                        hazardEvent.setSource(IHazardEvent.Source.RECOMMENDER);
                    }

                    /*
                     * Add the event (or modify an existing event by merging the
                     * new version into it).
                     */
                    hazardEvent
                            .removeHazardAttribute(HazardConstants.HAZARD_EVENT_SELECTED);
                    ObservedHazardEvent addedEvent = null;
                    try {
                        addedEvent = eventManager.addEvent(hazardEvent,
                                originator);
                    } catch (HazardEventServiceException e) {
                        statusHandler.error(
                                "Could not add hazard event generated by "
                                        + recommenderIdentifier + ".", e);
                        continue;
                    }
                    addedOrModifiedEvents.add(addedEvent);

                    /*
                     * If the event is new and new events are to be all saved to
                     * history or database, add it to the new events list;
                     * otherwise, if all events (new or existing) are to be
                     * saved to history or database, add it to the list for all
                     * events; otherwise, if only some events are to be saved to
                     * one or both, place it in the map of identifiers to
                     * events.
                     */
                    if (isNew && (addedNewEvents != null)) {
                        addedNewEvents.add(addedEvent);
                    } else if (addedEvents != null) {
                        addedEvents.add(addedEvent);
                    } else if (addedEventsForIdentifiers != null) {
                        addedEventsForIdentifiers.put(addedEvent.getEventID(),
                                addedEvent);
                    }
                }
            }

            /*
             * If all brand new hazard events are to be saved to the history or
             * database, perform the save.
             */
            if (addedNewEvents != null) {
                eventManager.saveEvents(addedNewEvents, saveAllNewToHistory,
                        treatAsIssuance);
            }

            /*
             * If the recommender indicated that all events it returned should
             * be saved (to history lists or to the latest version set), do so.
             * Otherwise, if the recommender specified the events to be saved to
             * history lists and/or to the latest version set, get the events
             * that go with the identifiers specified, and save them as
             * appropriate.
             */
            if ((addedEvents != null) && (addedEvents.isEmpty() == false)) {
                eventManager.saveEvents(addedEvents, addToHistory,
                        treatAsIssuance);
            } else if ((addedEventsForIdentifiers != null)
                    && (addedEventsForIdentifiers.isEmpty() == false)) {
                if (addToHistoryAttribute instanceof List) {
                    eventManager.saveEvents(
                            getEventsFromIdentifiers(
                                    (List<?>) addToHistoryAttribute,
                                    addedEventsForIdentifiers), true,
                            treatAsIssuance);
                } else if (addToDatabaseAttribute instanceof List) {

                    /*
                     * Ensure that if a hazard identifier is present in both
                     * this list and the list for history list saving, it is
                     * removed from this list, since it has already been saved
                     * above.
                     */
                    List<?> addToDatabaseList = null;
                    if (addToHistoryAttribute instanceof List) {
                        Set<?> pruned = Sets.difference(new HashSet<>(
                                (List<?>) addToDatabaseAttribute),
                                new HashSet<>((List<?>) addToHistoryAttribute));
                        addToDatabaseList = new ArrayList<>(pruned);
                    } else {
                        addToDatabaseList = (List<?>) addToDatabaseAttribute;
                    }
                    eventManager.saveEvents(
                            getEventsFromIdentifiers(addToDatabaseList,
                                    addedEventsForIdentifiers), false,
                            treatAsIssuance);
                }
            }

            /*
             * Make sure the updated hazard type is a part of the visible types
             * in the current setting. If not, add it.
             */
            ISessionConfigurationManager<ObservedSettings> configManager = sessionManager
                    .getConfigurationManager();
            Set<String> visibleTypes = configManager.getSettings()
                    .getVisibleTypes();
            int startSize = visibleTypes.size();
            for (ObservedHazardEvent event : addedOrModifiedEvents) {
                visibleTypes.add(HazardEventUtilities.getHazardType(event));
            }
            if (startSize != visibleTypes.size()) {
                configManager.getSettings().setVisibleTypes(visibleTypes);
            }

            /*
             * If a selected time has been included in the event set that was
             * returned, use it.
             */
            Object newSelectedTime = events
                    .getAttribute(HazardConstants.SELECTED_TIME);
            if (newSelectedTime != null) {
                SelectedTime selectedTime = null;
                if (newSelectedTime instanceof List) {
                    List<?> list = (List<?>) newSelectedTime;
                    selectedTime = (list.size() > 1 ? new SelectedTime(
                            ((Number) list.get(0)).longValue(),
                            ((Number) list.get(1)).longValue())
                            : new SelectedTime(
                                    ((Number) list.get(0)).longValue()));
                } else {
                    selectedTime = new SelectedTime(
                            ((Number) newSelectedTime).longValue());
                }
                sessionManager.getTimeManager().setSelectedTime(selectedTime,
                        originator);
            }
        }
    }

    /**
     * Determine whether or not the specified list contains at least one null
     * element.
     * 
     * @param list
     *            Potential list to be examined; typed as an <code>Object</code>
     *            for convenience, since callers will be using such.
     * @return <code>true</code> if the list contains at least one null element,
     *         <code>false</code> otherwise.
     */
    private boolean isListContainingNullElement(Object list) {
        if (list instanceof List == false) {
            return false;
        }
        for (Object identifier : (List<?>) list) {
            if (identifier == null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Given the specified event identifiers and map of event identifiers to
     * their events, get a list of any events specified by the former that have
     * an entry in the latter.
     * 
     * @param eventIdentifiers
     *            Event identifiers for which to find events. The element type
     *            is unknown as this makes invocation easier, since this
     *            parameter is cast from {@link Object} by callers.
     * @param eventsForIdentifiers
     *            Map of event identifiers to their corresponding events.
     * @return List of events that go with the event identifiers and that are
     *         found in the map.
     */
    private List<IHazardEvent> getEventsFromIdentifiers(
            List<?> eventIdentifiers,
            Map<String, IHazardEvent> eventsForIdentifiers) {

        Set<String> identifiersToSave = new HashSet<>(
                eventsForIdentifiers.size(), 1.0f);
        for (Object element : eventIdentifiers) {
            if (element != null) {
                identifiersToSave.add(element.toString());
            }
        }
        List<IHazardEvent> eventsToSave = new ArrayList<>(
                identifiersToSave.size());
        for (String identifier : Sets.intersection(
                eventsForIdentifiers.keySet(), identifiersToSave)) {
            eventsToSave.add(eventsForIdentifiers.get(identifier));
        }
        return eventsToSave;
    }
}

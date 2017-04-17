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

import gov.noaa.gsd.common.eventbus.BoundedReceptionEventBus;
import gov.noaa.gsd.common.visuals.VisualFeaturesList;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.raytheon.uf.common.dataplugin.events.EventSet;
import com.raytheon.uf.common.dataplugin.events.IEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.Trigger;
import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.HazardEventManager;
import com.raytheon.uf.common.dataplugin.events.hazards.event.BaseHazardEvent;
import com.raytheon.uf.common.python.concurrent.IPythonJobListener;
import com.raytheon.uf.common.recommenders.AbstractRecommenderEngine;
import com.raytheon.uf.common.recommenders.EventRecommender;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.DataTime;
import com.raytheon.uf.viz.core.drawables.IDescriptor.FramesInfo;
import com.raytheon.uf.viz.hazards.sessionmanager.ISessionManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.ObservedSettings;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.ToolType;
import com.raytheon.uf.viz.hazards.sessionmanager.events.impl.ObservedHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.messenger.IMessenger;
import com.raytheon.uf.viz.hazards.sessionmanager.recommenders.ISessionRecommenderManager;
import com.raytheon.uf.viz.hazards.sessionmanager.recommenders.RecommenderExecutionContext;
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
 * Mar 03, 2016   14004    Chris.Golden Changed to pass recommender identifier to
 *                                      the method handling recommender results.
 * Mar 04, 2016   15933    Chris.Golden Added ability to run multiple recommenders
 *                                      in sequence in response to a time interval
 *                                      trigger, instead of just one recommender.
 * Mar 06, 2016   15676    Chris.Golden Added more contextual information for
 *                                      recommender triggering, and changed the
 *                                      the recommender input EventSet to only
 *                                      include the events the recommender desires.
 * Apr 27, 2016   18266    Chris.Golden Added the inclusion of the latest data time
 *                                      in the recommender input event set if asked
 *                                      for by the recommender.
 * May 03, 2016   18376    Chris.Golden Changed to support reuse of Jep instance
 *                                      between H.S. sessions in the same CAVE
 *                                      session, since stopping and starting the
 *                                      Jep instances when the latter use numpy is
 *                                      dangerous.
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
        eventBus.subscribe(recommenderEngine);
    }

    @Override
    public EventRecommender getRecommender(String recommenderIdentifier) {
        return recommenderEngine.getInventory(recommenderIdentifier);
    }

    @Override
    public void runRecommender(String recommenderIdentifier,
            RecommenderExecutionContext context) {

        /*
         * Create the event set to be used, and add the execution context to it.
         */
        EventSet<IEvent> eventSet = new EventSet<>();
        addContextAsEventSetAttributes(context, eventSet);
        eventSet.addAttribute(HazardConstants.CENTER_POINT_LAT_LON,
                getCenterPointAsDictionary());
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
                .get(HazardConstants.RECOMMENDER_METADATA_ONLY_INCLUDE_TRIGGER_EVENT);
        List<String> includeEventTypesList = (List<String>) metadata
                .get(HazardConstants.RECOMMENDER_METADATA_INCLUDE_EVENT_TYPES);
        Set<String> includeEventTypes = (includeEventTypesList != null ? new HashSet<>(
                includeEventTypesList) : null);
        Boolean includeDataLayerTimes = (Boolean) metadata
                .get(HazardConstants.RECOMMENDER_METADATA_INCLUDE_DATA_LAYER_TIMES);

        /*
         * Create the event set, determine which events are to be added to it
         * based upon the recommender metadata retrieved above, and add a copy
         * of each such event to the set.
         */
        EventSet<IEvent> eventSet = new EventSet<>();
        if (Boolean.TRUE.equals(onlyIncludeTriggerEvent)
                && ((context.getTrigger() == Trigger.HAZARD_EVENT_VISUAL_FEATURE_CHANGE) || (context
                        .getTrigger() == Trigger.HAZARD_EVENT_MODIFICATION))) {
            eventSet.add(createBaseHazardEvent(sessionManager.getEventManager()
                    .getEventById(context.getEventIdentifier())));
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
         * Clear the set of removed event identifiers that the session manager
         * holds, since it should only hold identifiers of events that have been
         * removed since the last commencement of a recommender's execution, and
         * said commencement is about to occur.
         */
        sessionManager.clearRemovedEventIdentifiers();

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
        eventSet.addAttribute(HazardConstants.SITE_ID, sessionManager
                .getConfigurationManager().getSiteID());
        eventSet.addAttribute(
                HazardConstants.HAZARD_MODE,
                (CAVEMode.getMode()).toString().equals(
                        CAVEMode.PRACTICE.toString()) ? HazardEventManager.Mode.PRACTICE
                        .toString() : HazardEventManager.Mode.OPERATIONAL
                        .toString());

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
                                         * Let the session manager handle the
                                         * rest.
                                         */
                                        sessionManager.handleRecommenderResult(
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
        eventSet.addAttribute(HazardConstants.RECOMMENDER_EXECUTION_TRIGGER,
                context.getTrigger().toString());
        eventSet.addAttribute(HazardConstants.RECOMMENDER_EVENT_TYPE,
                context.getEventType());
        eventSet.addAttribute(
                HazardConstants.RECOMMENDER_TRIGGER_EVENT_IDENTIFIER,
                context.getEventIdentifier());
        eventSet.addAttribute(
                HazardConstants.RECOMMENDER_TRIGGER_ATTRIBUTE_IDENTIFIERS,
                context.getAttributeIdentifiers());
        eventSet.addAttribute(HazardConstants.RECOMMENDER_TRIGGER_ORIGIN,
                context.getOrigin().toString());
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
}

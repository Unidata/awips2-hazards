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

import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.FILE_PATH_KEY;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HAZARD_MODE;
import gov.noaa.gsd.common.eventbus.BoundedReceptionEventBus;
import gov.noaa.gsd.common.utilities.IRunnableAsynchronousScheduler;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.Lists;
import com.raytheon.uf.common.dataplugin.events.EventSet;
import com.raytheon.uf.common.dataplugin.events.IEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.HazardEventManager;
import com.raytheon.uf.common.dataplugin.events.hazards.event.BaseHazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.python.concurrent.IPythonJobListener;
import com.raytheon.uf.common.recommenders.AbstractRecommenderEngine;
import com.raytheon.uf.common.recommenders.EventRecommender;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.DataTime;
import com.raytheon.uf.viz.core.VizApp;
import com.raytheon.uf.viz.core.drawables.IDescriptor.FramesInfo;
import com.raytheon.uf.viz.hazards.sessionmanager.ISessionManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.ObservedSettings;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.ToolType;
import com.raytheon.uf.viz.hazards.sessionmanager.events.impl.ObservedHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.messenger.IMessenger;
import com.raytheon.uf.viz.hazards.sessionmanager.recommenders.ISessionRecommenderManager;
import com.raytheon.uf.viz.hazards.sessionmanager.recommenders.RecommenderExecutionContext;
import com.raytheon.uf.viz.recommenders.CAVERecommenderEngine;
import com.raytheon.uf.viz.recommenders.interactive.InteractiveRecommenderEngine;
import com.raytheon.viz.core.mode.CAVEMode;

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
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class SessionRecommenderManager implements ISessionRecommenderManager {

    // Private Static Constants

    /**
     * Scheduler to be used to ensure that result notifications are published on
     * the main thread. For now, the main thread is the UI thread; when this is
     * changed, this will be rendered obsolete, as at that point there will need
     * to be a blocking queue of {@link Runnable} instances available to allow
     * the new worker thread to be fed jobs. At that point, this should be
     * replaced with an object that enqueues the <code>Runnable</code>s,
     * probably a singleton that may be accessed by the various components in
     * gov.noaa.gsd.viz.hazards and elsewhere (presumably passed to the session
     * manager when the latter is created).
     */
    @Deprecated
    private static final IRunnableAsynchronousScheduler RUNNABLE_ASYNC_SCHEDULER = new IRunnableAsynchronousScheduler() {

        @Override
        public void schedule(Runnable runnable) {

            /*
             * Since the UI thread is currently the thread being used for nearly
             * everything, just run any asynchronous tasks there.
             */
            VizApp.runAsync(runnable);
        }
    };

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
    private final IMessenger messenger;

    /**
     * Engine to be used to actually run recommenders.
     */
    private final AbstractRecommenderEngine<?> recommenderEngine;

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
        recommenderEngine = new CAVERecommenderEngine();
        recommenderEngine.injectEngine(new InteractiveRecommenderEngine());
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
        /*
         * Determine whether or not any spatial or dialog input is required; if
         * either or both are, request the appropriate input. Otherwise, just
         * run the recommender.
         */
        Map<String, Serializable> spatialInput = recommenderEngine
                .getSpatialInfo(recommenderIdentifier);
        Map<String, Serializable> dialogInput = recommenderEngine
                .getDialogInfo(recommenderIdentifier, eventSet);
        if (!spatialInput.isEmpty() || !dialogInput.isEmpty()) {
            if (!spatialInput.isEmpty()) {
                spatialInput.put(HazardConstants.RECOMMENDER_EVENT_TYPE,
                        context.getEventType());
                messenger.getToolParameterGatherer().requestToolSpatialInput(
                        recommenderIdentifier, ToolType.RECOMMENDER, context,
                        spatialInput);
            }
            if (!dialogInput.isEmpty()) {
                if (!dialogInput.isEmpty()) {
                    dialogInput.put(FILE_PATH_KEY, recommenderEngine
                            .getInventory(recommenderIdentifier).getFile()
                            .getFile().getPath());
                    messenger.getToolParameterGatherer().getToolParameters(
                            recommenderIdentifier, ToolType.RECOMMENDER,
                            context, dialogInput);
                }
            }
        } else {
            runRecommender(recommenderIdentifier, context, null, null);
        }
    }

    /*
     * This method should be private, and thus is marked deprecated; see
     * interface for details.
     */
    @Override
    @Deprecated
    public void runRecommender(final String recommenderIdentifier,
            RecommenderExecutionContext context,
            Map<String, Serializable> spatialInfo,
            Map<String, Serializable> dialogInfo) {

        /*
         * Create the event set, and add events to it.
         */
        EventSet<IEvent> eventSet = new EventSet<>();
        Collection<ObservedHazardEvent> hazardEvents = sessionManager
                .getEventManager().getEvents();

        /*
         * HazardEvent.py canConvert() does not know anything about observed
         * hazard events. Also, HazardEvent.py is in a common plug-in while
         * observed hazard event is in a viz plug-in. So, convert the observed
         * hazard events to base hazard events.
         */
        for (IHazardEvent event : hazardEvents) {
            BaseHazardEvent baseHazardEvent = new BaseHazardEvent(event);
            eventSet.add(baseHazardEvent);
        }

        /*
         * Add the execution context parameters to the event set.
         */
        addContextAsEventSetAttributes(context, eventSet);

        /*
         * Add session information to event set.
         */
        eventSet.addAttribute(HazardConstants.CURRENT_TIME, sessionManager
                .getTimeManager().getCurrentTime().getTime());
        eventSet.addAttribute(HazardConstants.FRAMES_INFO,
                getFramesInfoAsDictionary());
        eventSet.addAttribute(HazardConstants.SITE_ID, sessionManager
                .getConfigurationManager().getSiteID());
        eventSet.addAttribute(
                HAZARD_MODE,
                (CAVEMode.getMode()).toString().equals(
                        CAVEMode.PRACTICE.toString()) ? HazardEventManager.Mode.PRACTICE
                        .toString() : HazardEventManager.Mode.OPERATIONAL
                        .toString());

        /*
         * Get the engine to initiate the execution of the recommender.
         */
        Map<String, Serializable> metadata = recommenderEngine
                .getScriptMetadata(recommenderIdentifier);
        final String toolName = (String) metadata
                .get(HazardConstants.RECOMMENDER_METADATA_TOOL_NAME);
        Boolean background = (Boolean) metadata
                .get(HazardConstants.RECOMMENDER_METADATA_BACKGROUND);
        final boolean notify = (Boolean.TRUE.equals(background) == false);
        recommenderEngine.runExecuteRecommender(recommenderIdentifier,
                eventSet, spatialInfo, dialogInfo,
                new IPythonJobListener<EventSet<IEvent>>() {

                    @Override
                    public void jobFinished(final EventSet<IEvent> result) {

                        /*
                         * Ensure that the thread used to process the result is
                         * the one used by the session manager.
                         */
                        SessionRecommenderManager.RUNNABLE_ASYNC_SCHEDULER
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
                                                                    + " completed. No recommendations were generated.");
                                        }

                                        /*
                                         * Let the session manager handle the
                                         * rest.
                                         */
                                        sessionManager
                                                .handleRecommenderResult(result);
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
    }

    // Private Methods

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

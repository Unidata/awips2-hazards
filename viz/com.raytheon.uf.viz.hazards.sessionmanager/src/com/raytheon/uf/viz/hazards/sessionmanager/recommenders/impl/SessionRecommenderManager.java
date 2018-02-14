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

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.raytheon.uf.common.dataplugin.events.EventSet;
import com.raytheon.uf.common.dataplugin.events.IEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardEventFirstClassAttribute;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardStatus;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.RecommenderTriggerOrigin;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.Trigger;
import com.raytheon.uf.common.dataplugin.events.hazards.event.BaseHazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardEventUtilities;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEventView;
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
import com.raytheon.uf.viz.hazards.sessionmanager.events.EventAttributesModification;
import com.raytheon.uf.viz.hazards.sessionmanager.events.EventGeometryModification;
import com.raytheon.uf.viz.hazards.sessionmanager.events.EventIssuanceCountModification;
import com.raytheon.uf.viz.hazards.sessionmanager.events.EventStatusModification;
import com.raytheon.uf.viz.hazards.sessionmanager.events.EventTimeRangeModification;
import com.raytheon.uf.viz.hazards.sessionmanager.events.EventTypeModification;
import com.raytheon.uf.viz.hazards.sessionmanager.events.EventVisualFeaturesModification;
import com.raytheon.uf.viz.hazards.sessionmanager.events.IEventModification;
import com.raytheon.uf.viz.hazards.sessionmanager.events.ISessionEventManager;
import com.raytheon.uf.viz.hazards.sessionmanager.events.ISessionEventManager.EventPropertyChangeResult;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionBatchNotificationsToggled;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventsRemoved;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionSelectedEventsModified;
import com.raytheon.uf.viz.hazards.sessionmanager.impl.ISessionNotificationSender;
import com.raytheon.uf.viz.hazards.sessionmanager.impl.ISessionNotificationSender.IIntraNotificationHandler;
import com.raytheon.uf.viz.hazards.sessionmanager.messenger.IMessenger;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.IOriginator;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.Originator;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.RecommenderOriginator;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.RevertOriginator;
import com.raytheon.uf.viz.hazards.sessionmanager.recommenders.ISessionRecommenderManager;
import com.raytheon.uf.viz.hazards.sessionmanager.recommenders.RecommenderExecutionContext;
import com.raytheon.uf.viz.hazards.sessionmanager.time.SelectedTime;
import com.raytheon.uf.viz.recommenders.CAVERecommenderEngine;
import com.raytheon.viz.core.mode.CAVEMode;
import com.vividsolutions.jts.geom.Coordinate;

import gov.noaa.gsd.common.utilities.Merger;
import gov.noaa.gsd.common.visuals.VisualFeaturesList;

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
 * Sep 26, 2016   21758    Chris.Golden Changed call to removeEvents() to provide new
 *                                      parameter.
 * Nov 17, 2016   22119    Kevin.Bisanz Set hazard siteID in handleRecommenderResult()
 * Dec 12, 2016   21504    Robert.Blum  Switched from user name and workstation to
 *                                      WsId.
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
 *                                      setOrigin (per hazard event in result event
 *                                      set).
 * Aug 15, 2017   22757    Chris.Golden Added ability for recommenders to specify
 *                                      either a message to display, or a dialog to
 *                                      display, with their results (that is, within
 *                                      the returned event set).
 * Sep 27, 2017   38072    Chris.Golden Removed methods that should not be public, and
 *                                      added private inner classes implementing the
 *                                      interfaces to be used to provide callback
 *                                      objects for dialog and spatial input, and for
 *                                      the displaying of results. Instances of these
 *                                      private classes are now used to allow UI
 *                                      elements to notify the recommender manager that
 *                                      it can proceed with the execution of a
 *                                      recommender, or that the execution is complete.
 *                                      Also added queuing of recommender execution
 *                                      requests if a recommender is running, with the
 *                                      requests being mergeable to allow multiple
 *                                      requests to be potentially collapsed into one.
 *                                      This allows needless recommender executions
 *                                      triggered by similar notifications to be
 *                                      avoided. Also added tracking of hazard event
 *                                      modifications made while a recommender is
 *                                      running, so that any changes made to hazard
 *                                      events by a recommender's resulting event set
 *                                      can have said modifications overlaid onto them.
 *                                      This avoids having a recommender run return a
 *                                      modified event that has, for example, a generic
 *                                      attribute that was set to a new value by the
 *                                      user while the recommender was running, and then
 *                                      having that modified event overwrite the one
 *                                      stored as part of this session. Finally, moved
 *                                      the code from event manager to this class that
 *                                      handles event modifications and selections
 *                                      triggering recommender runs, and modified said
 *                                      code to ensure that a multi-part modification
 *                                      that only needs to trigger one recommender does
 *                                      so only one time.
 * Oct 23, 2017   21730    Chris.Golden Adjusted IIntraNotificationHander implementations
 *                                      to make their isSynchronous() methods take the
 *                                      new parameter, using this parameter in the case
 *                                      of the batching notification to make it be
 *                                      handled synchronously if batching is starting,
 *                                      asynchronously if ending. This allows the ending
 *                                      of notification, and thus the running of any
 *                                      queued recommender execution requests, to be
 *                                      delayed until all the other notifications that
 *                                      actually trigger recommenders have been processed,
 *                                      which in turn allows batching of recommender runs
 *                                      to result in more batching and less individual
 *                                      runs.
 * Dec 07, 2017   41886    Chris.Golden Removed Java 8/JDK 1.8 usage.
 * Dec 17, 2017   20739    Chris.Golden Refactored away access to directly mutable session
 *                                      events. Also changed to use mergeHazardEvents()
 *                                      instead of addEvent() when merging changes to an
 *                                      existing hazard event back into the canonical
 *                                      session event, since addEvent() is no longer
 *                                      allowed to handle such cases.
 * Jan 17, 2018   45580    Chris.Golden Changed to use the same event-set-construction
 *                                      algorithm for building event sets for dialog
 *                                      and spatial parameter gathering, and for actual
 *                                      recommender execution.
 * Jan 26, 2018   33428    Chris.Golden Added use of new first-class attribute of hazard
 *                                      events, issuance count, to trigger recommenders
 *                                      that are supposed to run when the hazard status
 *                                      changes. This allows issuance and reissuance to
 *                                      be treated symmetrically by recommenders.
 * Jan 30, 2018   45994    Chris.Golden Added the provision of user name and workstation
 *                                      to the recommenders via their input event set.
 *                                      Also removed the option of letting a recommender
 *                                      decide whether or not to set the origin (user
 *                                      name, workstation, and source) of an event that
 *                                      has been modified by said recommender; these
 *                                      elements are always modified. Also fixed bugs
 *                                      caused by recommenders modifying events that
 *                                      as of  yet have no hazard type.
 * Feb 06, 2018   46258    Chris.Golden Added "revert" to recommender trigger origin, and
 *                                      added ability to treat event modifications as a
 *                                      result of a recommender run as being actual
 *                                      modifications from the perspectives of the
 *                                      session events.
 * Feb 13, 2018   20595    Chris.Golden Added "type" as a recommender execution trigger.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class SessionRecommenderManager implements ISessionRecommenderManager {

    // Private Enumerated Types

    /**
     * Actions that can be taken to set a boolean value.
     */
    private enum SetBooleanAction {
        DO_NOT_SET, SET_TRUE, SET_FALSE
    }

    // Private Classes

    /**
     * Base class for execution continuers of the currently running recommender.
     */
    private abstract class ExecutionContinuer {

        // Protected Variables

        /**
         * Identifier of the recommender for which execution is to be continued.
         * If this does not match the current
         * {@link #runningRecommenderIdentifier} of the enclosing object, this
         * continuer is to be ignored.
         */
        protected final String recommenderIdentifier;

        /**
         * Context in which the recommender for which execution is to be
         * continued is running. If this does not match the current
         * {@link #runningContext} of the enclosing object, this continuer is to
         * be ignored.
         */
        protected final RecommenderExecutionContext context;

        // Private Constructors

        /**
         * Construct a standard instance.
         */
        private ExecutionContinuer() {
            this.recommenderIdentifier = runningRecommenderIdentifier;
            this.context = runningContext;
        }

        // Protected Methods

        /**
         * Determine whether or not this receiver is relevant to the recommender
         * that is currently running, if any.
         * 
         * @return <code>true</code> if this receiver is relevant,
         *         <code>false</code> otherwise. If the latter, any parameters
         *         provided to the receiver are ignored.
         */
        protected final boolean isRelevant() {
            return (recommenderIdentifier.equals(runningRecommenderIdentifier)
                    && context.getIdentifier() == runningContext
                            .getIdentifier());
        }
    }

    /**
     * Implementation of a dialog parameters receiver.
     */
    private class DialogParametersReceiver extends ExecutionContinuer
            implements IDialogParametersReceiver {

        // Public Methods

        @Override
        public void receiveDialogParameters(
                Map<String, Serializable> parameters) {
            if (isRelevant()) {
                if (parameters != null) {
                    executeRunningRecommender(null, parameters);
                } else {
                    cancelRunningRecommender();
                }
            } else {
                statusHandler.error(
                        "Received dialog parameters for recommender "
                                + recommenderIdentifier + " (" + context
                                + ") which is not currently running; ignoring",
                        new Exception());
            }
        }
    }

    /**
     * Implementation of a spatial parameters receiver.
     */
    private class SpatialParametersReceiver extends ExecutionContinuer
            implements ISpatialParametersReceiver {

        // Public Methods

        @Override
        public void receiveSpatialParameters(VisualFeaturesList parameters) {
            if (isRelevant()) {
                if (parameters != null) {
                    executeRunningRecommender(parameters, null);
                } else {
                    cancelRunningRecommender();
                }
            } else {
                statusHandler.error(
                        "Received spatial parameters for recommender "
                                + recommenderIdentifier + " (" + context
                                + ") which is not currently running; ignoring",
                        new Exception());
            }
        }
    }

    /**
     * Implementation of a results display complete notifier.
     */
    private class ResultsDisplayCompleteNotifier extends ExecutionContinuer
            implements IResultsDisplayCompleteNotifier {

        // Public Methods

        @Override
        public void resultsDisplayCompleted() {
            if (isRelevant()) {
                finishRunningRecommender();
            } else {
                statusHandler.error(
                        "Received results display completion notification for recommender "
                                + recommenderIdentifier + " (" + context
                                + ") which is not currently running; ignoring",
                        new Exception());
            }
        }
    }

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
    private final ISessionManager<ObservedSettings> sessionManager;

    /**
     * Notification sender.
     */
    private final ISessionNotificationSender notificationSender;

    /**
     * Messenger used to communicate with the user.
     */
    private IMessenger messenger;

    /**
     * Engine to be used to actually run recommenders.
     */
    private final AbstractRecommenderEngine<?> recommenderEngine;

    /**
     * Map of recommender identifiers to metadata they provided when queried.
     */
    private final Map<String, Map<String, Serializable>> metadataForRecommenders = new HashMap<>();

    /**
     * Identifier of the recommender that is currently running; if
     * <code>null</code>, no recommender is running. Note that this variable and
     * {@link #runningContext} are either both <code>null</code> or both not
     * <code>null</code>. Access to this variable is synchronized on
     * {@link #pendingRecommenderExecutionRequests}.
     */
    private String runningRecommenderIdentifier;

    /**
     * Execution context of the recommender that is currently running; if
     * <code>null</code>, no recommender is running. Note that this variable and
     * {@link #runningRecommenderIdentifier} are either both <code>null</code>
     * or both not <code>null</code>. Access to this variable is synchronized on
     * {@link #pendingRecommenderExecutionRequests}.
     */
    private RecommenderExecutionContext runningContext;

    /**
     * Flag indicating whether batching of notifications is currently occurring.
     * Recommender execution requests are accumulated when batching mode is on,
     * as this means that two or more such requests may be able to be collapsed
     * into one before execution. Access to this variable is synchronized on
     * {@link #pendingRecommenderExecutionRequests}.
     */
    private boolean batching;

    /**
     * Pending recommender execution requests. If a recommender is currently
     * running, or if batching of notifications is currently occurring, the
     * request at the head of this list is to be run after the currently running
     * recommender completes and no batching is occurring, then the next one,
     * and so on until this list is empty. Access to this variable is
     * synchronized on itself.
     */
    private final List<RecommenderExecutionRequest> pendingRecommenderExecutionRequests = new ArrayList<>();

    /**
     * Map pairing identifiers of events that have been modified since the start
     * of the last recommender run with the modifications they have undergone.
     * Access to this variable is synchronized on
     * {@link #pendingRecommenderExecutionRequests}.
     */
    private final Map<String, List<IEventModification>> modificationsForIdentifiersOfEventsModifiedSinceLastRecommenderRun = new HashMap<>();

    /**
     * Set of event identifiers for those events that have been completely
     * removed since the commencement of the last recommender execution. As
     * events are removed, they are added to this set, and then when a
     * recommender is run, this set is emptied before it starts. When a
     * recommender completes execution and returns an event set, any events
     * included in the latter are checked against this set to ensure that they
     * were not removed for some other reason while the recommender was running,
     * and any that were removed are ignored. Access to this variable is
     * synchronized on {@link #pendingRecommenderExecutionRequests}.
     */
    private final Set<String> identifiersOfEventsRemovedSinceLastRecommenderRun = new HashSet<>();

    /**
     * Updated site identifier, to be changed before the next pending
     * recommender execution request is fulfilled. Access to this variable is
     * synchronized on {@link #pendingRecommenderExecutionRequests}.
     */
    private String updatedSiteIdentifier;

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

    /**
     * Intra-managerial notification handler for batch notification toggles.
     */
    private IIntraNotificationHandler<SessionBatchNotificationsToggled> batchNotificationToggleHandler = new IIntraNotificationHandler<SessionBatchNotificationsToggled>() {

        @Override
        public void handleNotification(
                SessionBatchNotificationsToggled notification) {
            setBatchingToggleAndRunNextRequestedRecommender(
                    notification.isBatching() ? SetBooleanAction.SET_TRUE
                            : SetBooleanAction.SET_FALSE,
                    false);
        }

        @Override
        public boolean isSynchronous(
                SessionBatchNotificationsToggled notification) {

            /*
             * It is important that any batching notifications indicating
             * batching is starting be processed immediately, whereas those that
             * indicate batching is ending get delayed so that other
             * notifications are handled first.
             */
            return notification.isBatching();
        }
    };

    /**
     * Intra-managerial notification handler for event selection changes.
     */
    private IIntraNotificationHandler<SessionSelectedEventsModified> eventSelectionChangeHandler = new IIntraNotificationHandler<SessionSelectedEventsModified>() {

        @Override
        public void handleNotification(
                SessionSelectedEventsModified notification) {
            triggerRecommendersForHazardEventSelectionChanges(notification);
        }

        @Override
        public boolean isSynchronous(
                SessionSelectedEventsModified notification) {
            return false;
        }
    };

    /**
     * Intra-managerial notification handler for event modifications.
     */
    private IIntraNotificationHandler<SessionEventModified> eventModifiedHandler = new IIntraNotificationHandler<SessionEventModified>() {

        @Override
        public void handleNotification(SessionEventModified notification) {

            /*
             * Record any modifications to hazard events if a recommender is
             * currently running, so that those modifications can be applied to
             * that recommender's resulting events to ensure that session
             * changes do not get overwritten by the recommender's results.
             */
            recordModificationsToHazardEventIfNecessary(notification);

            /*
             * Run any recommenders that should be triggered by the
             * modifications.
             */
            triggerRecommendersForHazardEventModifications(notification);
        }

        @Override
        public boolean isSynchronous(SessionEventModified notification) {
            return true;
        }
    };

    /**
     * Intra-managerial notification handler for event removals.
     */
    private IIntraNotificationHandler<SessionEventsRemoved> eventsRemovedHandler = new IIntraNotificationHandler<SessionEventsRemoved>() {

        @Override
        public void handleNotification(SessionEventsRemoved notification) {

            /*
             * If a recommender is running, record the removed hazard events.
             */
            synchronized (pendingRecommenderExecutionRequests) {
                if (runningRecommenderIdentifier != null) {

                    /*
                     * TODO: When moving to Java 8, remove the code below that
                     * is not commented out, and then uncomment the commented
                     * out code immediately below it.
                     */
                    for (IHazardEventView event : notification.getEvents()) {
                        identifiersOfEventsRemovedSinceLastRecommenderRun
                                .add(event.getEventID());
                    }
                    // identifiersOfEventsRemovedSinceLastRecommenderRun
                    // .addAll(notification.getEvents().stream()
                    // .map(IHazardEventView::getEventID)
                    // .collect(Collectors.toList()));
                }
            }
        }

        @Override
        public boolean isSynchronous(SessionEventsRemoved notification) {
            return true;
        }
    };

    /**
     * Intra-managerial notification handler for site changes.
     */
    private IIntraNotificationHandler<SiteChanged> siteChangeHandler = new IIntraNotificationHandler<SiteChanged>() {

        @Override
        public void handleNotification(SiteChanged notification) {
            siteChanged(notification);
        }

        @Override
        public boolean isSynchronous(SiteChanged notification) {
            return false;
        }
    };

    // Constructors

    /**
     * Construct a standard instance.
     * 
     * @param sessionManager
     *            Session manager that is building this manager.
     * @param notificationSender
     *            Notification sender.
     * @param messenger
     *            Messenger used to communicate with the user.
     */
    public SessionRecommenderManager(
            ISessionManager<ObservedSettings> sessionManager,
            ISessionNotificationSender notificationSender,
            IMessenger messenger) {
        this.sessionManager = sessionManager;
        this.notificationSender = notificationSender;
        this.messenger = messenger;
        recommenderEngine = CAVERecommenderEngine.getInstance();
        recommenderEngine
                .setSite(sessionManager.getConfigurationManager().getSiteID());

        notificationSender.registerIntraNotificationHandler(
                SessionBatchNotificationsToggled.class,
                batchNotificationToggleHandler);
        notificationSender.registerIntraNotificationHandler(
                SessionEventModified.class, eventModifiedHandler);
        notificationSender.registerIntraNotificationHandler(
                SessionEventsRemoved.class, eventsRemovedHandler);
        notificationSender.registerIntraNotificationHandler(
                SessionSelectedEventsModified.class,
                eventSelectionChangeHandler);
        notificationSender.registerIntraNotificationHandler(SiteChanged.class,
                siteChangeHandler);
    }

    // Public Methods

    @Override
    public EventRecommender getRecommender(String recommenderIdentifier) {
        return recommenderEngine.getInventory(recommenderIdentifier);
    }

    @Override
    public void runRecommender(String recommenderIdentifier,
            RecommenderExecutionContext context) {
        runRecommenders(Lists.newArrayList(recommenderIdentifier), context);
    }

    @Override
    public void runRecommenders(List<String> recommenderIdentifiers,
            RecommenderExecutionContext context) {
        if (recommenderIdentifiers.isEmpty()) {
            throw new IllegalArgumentException(
                    "at least one recommender identifier must be supplied");
        }

        /*
         * If a recommender is currently running, or batching of requests is
         * currently toggled on, add a request for these recommenders to be
         * executed and do nothing more. The request may be merged with an
         * existing request if one exists that is similar enough. If no
         * recommender is currently running, set the flag indicating that one is
         * running to true to ensure that this one can be run next.
         */
        synchronized (pendingRecommenderExecutionRequests) {
            if ((runningRecommenderIdentifier != null) || batching) {
                Merger.merge(pendingRecommenderExecutionRequests,
                        new RecommenderExecutionRequest(context,
                                recommenderIdentifiers));
                return;
            }
            runningRecommenderIdentifier = recommenderIdentifiers.get(0);
            runningContext = context;
        }

        /*
         * Remember the recommenders to be run and prep for running the first
         * one, gathering parameters for the latter as necessary.
         */
        runRecommendersGatheringParametersAsNecessary(recommenderIdentifiers);
    }

    @Override
    public void eventCommandInvoked(String eventIdentifier,
            String commandIdentifier) {
        Map<String, String> recommendersForTriggerIdentifiers = sessionManager
                .getEventManager()
                .getRecommendersForTriggerIdentifiers(eventIdentifier);
        if (recommendersForTriggerIdentifiers.containsKey(commandIdentifier)) {
            runRecommender(
                    recommendersForTriggerIdentifiers.get(commandIdentifier),
                    RecommenderExecutionContext
                            .getHazardEventModificationContext(eventIdentifier,
                                    Sets.newHashSet(commandIdentifier),
                                    RecommenderTriggerOrigin.USER));
        }
    }

    @Override
    public void shutdown() {
        notificationSender.unregisterIntraNotificationHandler(
                batchNotificationToggleHandler);
        notificationSender
                .unregisterIntraNotificationHandler(eventModifiedHandler);
        notificationSender
                .unregisterIntraNotificationHandler(eventsRemovedHandler);
        notificationSender.unregisterIntraNotificationHandler(
                eventSelectionChangeHandler);
        notificationSender
                .unregisterIntraNotificationHandler(siteChangeHandler);
        recommenderEngine.shutdownEngine();
        messenger = null;
    }

    // Private Methods

    /**
     * If a recommender is running, record the modifications indicated by the
     * specified notification.
     * 
     * @param notification
     *            Hazard event modified notification.
     */
    private void recordModificationsToHazardEventIfNecessary(
            SessionEventModified notification) {

        /*
         * If a recommender is running, record the modifications as associated
         * with the identifier of the event modified.
         */
        synchronized (pendingRecommenderExecutionRequests) {
            if (runningRecommenderIdentifier != null) {
                List<IEventModification> modifications = modificationsForIdentifiersOfEventsModifiedSinceLastRecommenderRun
                        .get(notification.getEvent().getEventID());
                if (modifications == null) {
                    modifications = new ArrayList<>(
                            notification.getModifications());
                    modificationsForIdentifiersOfEventsModifiedSinceLastRecommenderRun
                            .put(notification.getEvent().getEventID(),
                                    modifications);
                } else {
                    for (IEventModification modification : notification
                            .getModifications()) {
                        Merger.merge(modifications, modification);
                    }
                }
            }
        }
    }

    /**
     * Trigger any recommenders that are to be run in response to the specified
     * hazard event modification.
     * 
     * @param notification
     *            Hazard event modified notification.
     */
    private void triggerRecommendersForHazardEventModifications(
            SessionEventModified notification) {

        /*
         * Iterate through the modifications, compiling two things: a map (with
         * iteration order being the order in which elements are added, to
         * ensure that entries put in first are iterated through first) pairing
         * triggered recommender identifiers with the hazard attributes (generic
         * and first-class) that triggered them, and a record of what
         * recommender is to be triggered by any visual feature changes (if any
         * occurred), together with the identifiers of the visual feature(s)
         * that changed, and the ordinal indicating when (if any visual feature
         * recommender triggering is happening) the visual feature triggered
         * recommender should be run with respect to the hazard attribute
         * triggered recommenders.
         */
        Map<String, Set<String>> modifiedAttributesForTriggeredRecommenders = new LinkedHashMap<>();
        int visualFeatureTriggeredRecommenderOrdinal = -1;
        String visualFeatureTriggeredRecommender = null;
        Set<String> modifiedVisualFeatureIdentifiers = null;
        for (IEventModification modification : notification
                .getModifications()) {
            if (modification instanceof EventTimeRangeModification) {
                addTriggeredRecommenderEntryForModifiedAttribute(
                        modifiedAttributesForTriggeredRecommenders,
                        notification,
                        HazardEventFirstClassAttribute.TIME_RANGE);
            } else if ((modification instanceof EventStatusModification)
                    || ((modification instanceof EventIssuanceCountModification)
                            && (notification.getEvent()
                                    .getIssuanceCount() > 1))) {

                /*
                 * Note that changes in status, as well as issuance count
                 * changes that indicate a reissuance, are both treated as
                 * status changes in terms of triggering.
                 */
                addTriggeredRecommenderEntryForModifiedAttribute(
                        modifiedAttributesForTriggeredRecommenders,
                        notification, HazardEventFirstClassAttribute.STATUS);
            } else if (modification instanceof EventGeometryModification) {
                addTriggeredRecommenderEntryForModifiedAttribute(
                        modifiedAttributesForTriggeredRecommenders,
                        notification, HazardEventFirstClassAttribute.GEOMETRY);
            } else if (modification instanceof EventTypeModification) {
                addTriggeredRecommenderEntryForModifiedAttribute(
                        modifiedAttributesForTriggeredRecommenders,
                        notification, HazardEventFirstClassAttribute.TYPE);
            } else if (modification instanceof EventVisualFeaturesModification) {

                /*
                 * If no visual feature triggered recommender has been found
                 * until now, get the recommender identifier, and if one is
                 * found, compile the identifiers of the visual feature(s) that
                 * changed to trigger this recommender, as well as the size of
                 * the attribute triggered recommenders map, so that the point
                 * at which the visual feature triggered recommender should be
                 * run with respect to the recommenders in the map. If this is
                 * not the first visual feature modification found that is to
                 * trigger a recommender, just add the changed visual features'
                 * identifiers to the set of changed identifiers.
                 */
                if (visualFeatureTriggeredRecommender == null) {
                    visualFeatureTriggeredRecommender = getTriggeredRecommenderForFirstClassAttributeChange(
                            notification.getEvent(),
                            HazardEventFirstClassAttribute.VISUAL_FEATURE,
                            notification.getOriginator());
                    if (visualFeatureTriggeredRecommender != null) {
                        modifiedVisualFeatureIdentifiers = new HashSet<>(
                                ((EventVisualFeaturesModification) modification)
                                        .getVisualFeatureIdentifiers());
                        visualFeatureTriggeredRecommenderOrdinal = modifiedAttributesForTriggeredRecommenders
                                .size();
                    }
                } else {
                    modifiedVisualFeatureIdentifiers
                            .addAll(((EventVisualFeaturesModification) modification)
                                    .getVisualFeatureIdentifiers());
                }
            } else if (modification instanceof EventAttributesModification) {
                addTriggeredRecommenderEntryForModifiedAttributes(
                        modifiedAttributesForTriggeredRecommenders,
                        notification,
                        ((EventAttributesModification) modification)
                                .getAttributeKeys());
            }
        }

        /*
         * Iterate through the recommenders to be run, executing each in turn.
         * If visual feature changes are to trigger a recommender as well, run
         * that recommender at the appropriate point within the triggered
         * recommenders.
         */
        int count = 0;
        for (Map.Entry<String, Set<String>> entry : modifiedAttributesForTriggeredRecommenders
                .entrySet()) {

            runVisualFeaturesTriggeredRecommenderIfAppropriate(
                    visualFeatureTriggeredRecommender,
                    modifiedVisualFeatureIdentifiers, notification,
                    visualFeatureTriggeredRecommenderOrdinal, count++);

            runAttributeTriggeredRecommender(entry.getKey(), entry.getValue(),
                    notification);
        }

        runVisualFeaturesTriggeredRecommenderIfAppropriate(
                visualFeatureTriggeredRecommender,
                modifiedVisualFeatureIdentifiers, notification,
                visualFeatureTriggeredRecommenderOrdinal, count);
    }

    /**
     * If the specified first-class attribute of the event referenced by the
     * specified notification is associated with a recommender to be triggered
     * by the attribute's modification, update the specified map to include an
     * entry pairing said recommender's identifier with this attribute (or with
     * this attribute and any others that are already found as the value within
     * the map for said recommender).
     * 
     * @param modifiedAttributesForTriggeredRecommenders
     *            Map pairing recommender identifiers with the attributes that
     *            trigger them.
     * @param notification
     *            Notification of the event modification that is being
     *            considered.
     * @param attribute
     *            First-class hazard event attribute that was modified.
     */
    private void addTriggeredRecommenderEntryForModifiedAttribute(
            Map<String, Set<String>> modifiedAttributesForTriggeredRecommenders,
            SessionEventModified notification,
            HazardEventFirstClassAttribute attribute) {
        String recommender = getTriggeredRecommenderForFirstClassAttributeChange(
                notification.getEvent(), attribute,
                notification.getOriginator());
        if (recommender != null) {
            Set<String> modifiedAttributes = modifiedAttributesForTriggeredRecommenders
                    .get(recommender);
            if (modifiedAttributes == null) {
                modifiedAttributes = Sets.newHashSet(attribute.toString());
                modifiedAttributesForTriggeredRecommenders.put(recommender,
                        modifiedAttributes);
            } else {
                modifiedAttributes.add(attribute.toString());
            }
        }
    }

    /**
     * If the specified generic attributes of the event referenced by the
     * specified notification is associated with recommenders to be triggered by
     * the attributes' modification, update the specified map to include entries
     * pairing said recommenders' identifiers with their associated attributes
     * (or with said attributes and any others that are already found as the
     * value within the map for a given recommender).
     * 
     * @param modifiedAttributesForTriggeredRecommenders
     *            Map pairing recommender identifiers with the attributes that
     *            trigger them.
     * @param notification
     *            Notification of the event modification that is being
     *            considered.
     * @param attributes
     *            Generic attributes that were modified.
     */
    private void addTriggeredRecommenderEntryForModifiedAttributes(
            Map<String, Set<String>> modifiedAttributesForTriggeredRecommenders,
            SessionEventModified notification, Set<String> attributes) {

        /*
         * Determine whether or not a recommender triggered this change, and if
         * so, note its name, since recommenders should not be triggered by
         * changes caused by the earlier runs of those same recommenders.
         */
        IOriginator originator = notification.getOriginator();
        String cause = null;
        if (originator instanceof RecommenderOriginator) {
            cause = ((RecommenderOriginator) originator).getName();
        }

        /*
         * See if at least one of the attributes that changed is a recommender
         * trigger. If so, put together a mapping of recommenders that need to
         * be run to the set of one or more attributes that triggered them.
         */
        Map<String, String> recommendersForTriggerIdentifiers = sessionManager
                .getEventManager().getRecommendersForTriggerIdentifiers(
                        notification.getEvent().getEventID());
        Set<String> triggers = Sets.intersection(
                recommendersForTriggerIdentifiers.keySet(), attributes);
        for (String trigger : triggers) {

            /*
             * Get the recommender to be triggered by this attribute identifier;
             * if it is the same as the recommender that caused the attribute to
             * change, do nothing with it.
             */
            String recommender = recommendersForTriggerIdentifiers.get(trigger);
            if (recommender.equals(cause)) {
                continue;
            }

            /*
             * If the recommender is already associated with a set of triggers,
             * add this trigger to the set; otherwise, create a new set holding
             * this trigger and associate it with the recommender.
             */
            if (modifiedAttributesForTriggeredRecommenders
                    .containsKey(recommender)) {
                modifiedAttributesForTriggeredRecommenders.get(recommender)
                        .add(trigger);
            } else {
                Set<String> modifiedAttributes = Sets.newHashSet(trigger);
                modifiedAttributesForTriggeredRecommenders.put(recommender,
                        modifiedAttributes);
            }
        }
    }

    /**
     * Get the identifier of the recommender that is triggered by a change to
     * the specified first class attribute for the specified hazard event, if
     * one is indeed triggered.
     * 
     * @param event
     *            Hazard event that was changed.
     * @param attribute
     *            First-class attribute that experienced a value change.
     * @param originator
     *            Originator of the change.
     * @return Identifier of the recommender that is triggered, or
     *         <code>null</code> if no recommender is triggered.
     */
    private String getTriggeredRecommenderForFirstClassAttributeChange(
            IHazardEventView event, HazardEventFirstClassAttribute attribute,
            IOriginator originator) {

        /*
         * Get the recommender identifier that is to be triggered by this
         * first-class attribute; if there is one, and it is not the same as the
         * recommender (if any) that caused the change, return it.
         */
        String recommender = sessionManager.getConfigurationManager()
                .getRecommenderTriggeredByChange(event, attribute);
        if ((recommender != null)
                && ((originator instanceof RecommenderOriginator == false)
                        || (recommender
                                .equals(((RecommenderOriginator) originator)
                                        .getName()) == false))) {
            return recommender;
        }
        return null;
    }

    /**
     * Run the specified visual feature triggered recommender with the specified
     * visual feature identifiers as triggers if the specified ordinal value
     * equals the specified count.
     * 
     * @param recommenderIdentifier
     *            Identifier of the recommender to run if appropriate.
     * @param visualFeatureIdentifiers
     *            Visual feature identifiers to pass to the recommender as the
     *            triggering elements.
     * @param notification
     *            Notification containing the visual feature modification.
     * @param ordinal
     *            Ordinal value, to be compared with <code>count</code>.
     * @param count
     *            Count, to be compared with <code>ordinal</code>
     */
    private void runVisualFeaturesTriggeredRecommenderIfAppropriate(
            String recommenderIdentifier, Set<String> visualFeatureIdentifiers,
            SessionEventModified notification, int ordinal, int count) {
        if (ordinal == count) {
            runRecommender(recommenderIdentifier,
                    RecommenderExecutionContext
                            .getHazardEventVisualFeatureChangeContext(
                                    notification.getEvent().getEventID(),
                                    visualFeatureIdentifiers,
                                    getRecommenderTriggerOriginatorFromOriginator(
                                            notification.getOriginator())));
        }
    }

    /**
     * Run the specified attribute triggered recommender with the specified
     * attribute identifiers as triggers.
     * 
     * @param recommenderIdentifier
     *            Identifier of the recommender to run if appropriate.
     * @param attributeIdentifiers
     *            Attribute identifiers to pass to the recommender as the
     *            triggering elements.
     * @param notification
     *            Notification containing the attribute modifications.
     */
    private void runAttributeTriggeredRecommender(String recommenderIdentifier,
            Set<String> attributeIdentifiers,
            SessionEventModified notification) {
        runRecommender(recommenderIdentifier,
                RecommenderExecutionContext.getHazardEventModificationContext(
                        notification.getEvent().getEventID(),
                        attributeIdentifiers,
                        getRecommenderTriggerOriginatorFromOriginator(
                                notification.getOriginator())));
    }

    /**
     * Trigger any recommenders that are to be run in response to the specified
     * hazard event selection change.
     * 
     * @param notification
     *            Change that occurred.
     */
    private void triggerRecommendersForHazardEventSelectionChanges(
            SessionSelectedEventsModified notification) {

        /*
         * Iterate through the events that changed their selection state,
         * compiling a mapping of recommenders to be triggered for those events
         * to those events for which to trigger those particular recommenders.
         * Thus, for example, if three hazard events changed selection state,
         * two of which are to trigger recommender A when they change selection
         * and one of which is to trigger recommender B in such cases, the
         * result of this loop will be a map of two entries, one holding
         * recommender A associated with the two events that are to trigger it,
         * the other holding recommender B associated with the remaining event.
         */
        Map<String, Set<String>> eventIdentifiersForTriggeredRecommenders = new HashMap<>(
                notification.getEventIdentifiers().size(), 1.0f);
        for (String eventIdentifier : notification.getEventIdentifiers()) {
            IHazardEventView event = sessionManager.getEventManager()
                    .getEventById(eventIdentifier);
            if (event == null) {
                continue;
            }
            String recommender = getTriggeredRecommenderForFirstClassAttributeChange(
                    event, HazardEventFirstClassAttribute.SELECTION,
                    notification.getOriginator());
            if (recommender != null) {
                Set<String> eventIdentifiers = eventIdentifiersForTriggeredRecommenders
                        .get(recommender);
                if (eventIdentifiers == null) {
                    eventIdentifiers = Sets.newHashSet(eventIdentifier);
                    eventIdentifiersForTriggeredRecommenders.put(recommender,
                            eventIdentifiers);
                } else {
                    eventIdentifiers.add(eventIdentifier);
                }
            }
        }

        /*
         * Iterate through the entries in the map compiled above, running the
         * appropriate recommender for each one if said recommender is not the
         * same as the recommender (if any) that caused the selection change.
         */
        for (Map.Entry<String, Set<String>> entry : eventIdentifiersForTriggeredRecommenders
                .entrySet()) {
            sessionManager.getRecommenderManager()
                    .runRecommender(entry.getKey(), RecommenderExecutionContext
                            .getHazardEventSelectionChangeContext(
                                    entry.getValue(),
                                    getRecommenderTriggerOriginatorFromOriginator(
                                            notification.getOriginator())));
        }
    }

    /**
     * Convert the specified originator into a recommender trigger originator.
     * 
     * @param originator
     *            Originator to be converted.
     * @return Recommender trigger originator.
     */
    private RecommenderTriggerOrigin getRecommenderTriggerOriginatorFromOriginator(
            IOriginator originator) {
        return ((originator instanceof RecommenderOriginator)
                || originator.equals(Originator.OTHER)
                        ? RecommenderTriggerOrigin.OTHER
                        : (originator instanceof RevertOriginator
                                ? RecommenderTriggerOrigin.REVERT
                                : (originator.equals(Originator.DATABASE)
                                        ? RecommenderTriggerOrigin.DATABASE
                                        : RecommenderTriggerOrigin.USER)));
    }

    /**
     * Run the specified recommenders sequentially, waiting for the first to
     * complete before running the second and so on. If parameters must be
     * gathered from the user via spatial or dialog input, this method will do
     * so. If not, it will simply run the recommenders. It is assumed that the
     * recommender with the identifier found at the top of the specified list is
     * indeed currently running.
     * 
     * @param recommenderIdentifiers
     *            Identifiers of the recommenders to be run, in the order in
     *            which they should be run.
     */
    private void runRecommendersGatheringParametersAsNecessary(
            List<String> recommenderIdentifiers) {

        /*
         * If more than one recommender is to be run, associate the list of
         * recommenders to be run with this context's identifier so that the
         * subsquent ones may be run after the first one.
         */
        if (recommenderIdentifiers.size() > 1) {
            sequentialRecommendersForExecutionContextIdentifiers.put(
                    runningContext.getIdentifier(), recommenderIdentifiers);
        }

        /*
         * Run the first recommender in the list of those to be run.
         */
        runRecommenderGatheringParametersAsNecessary();
    }

    /**
     * Proceed with the recommender that is currently running. If parameters
     * must be gathered from the user via spatial or dialog input, this method
     * will do so. If not, it will simply run the recommender. It is assumed
     * that a recommender is indeed currently running.
     */
    private void runRecommenderGatheringParametersAsNecessary() {

        /*
         * Get the recommender metadata, and determine whether or not dialog
         * info and/or spatial info are needed.
         */
        Map<String, Serializable> metadata = getRecommenderMetadata(
                runningRecommenderIdentifier);
        boolean getDialogInfoNeeded = (Boolean.FALSE.equals(metadata.get(
                HazardConstants.RECOMMENDER_METADATA_GET_DIALOG_INFO_NEEDED)) == false);
        boolean getSpatialInfoNeeded = (Boolean.FALSE.equals(metadata.get(
                HazardConstants.RECOMMENDER_METADATA_GET_SPATIAL_INFO_NEEDED)) == false);

        /*
         * Create the event set to be used if dialog and/or spatial info is to
         * be fetched; if no event set can be created, cancel the running of the
         * recommender.
         */
        EventSet<IEvent> eventSet = null;
        if (getDialogInfoNeeded || getSpatialInfoNeeded) {
            eventSet = createEventSet();
            if (eventSet == null) {
                cancelRunningRecommender();
                return;
            }
        }

        /*
         * Determine whether or not any spatial or dialog input is required; if
         * either or both are, request the appropriate input. Spatial input is
         * taken by displaying one or more visual features for the user, and
         * waiting for one of them to be manipulated in some way; dialog input
         * is taken by putting up a dialog and waiting for the user to proceed.
         * Otherwise, just run the recommender.
         */
        VisualFeaturesList visualFeatures = (getSpatialInfoNeeded
                ? recommenderEngine.getSpatialInfo(runningRecommenderIdentifier,
                        eventSet)
                : null);
        Map<String, Serializable> dialogDescription = (getDialogInfoNeeded
                ? recommenderEngine.getDialogInfo(runningRecommenderIdentifier,
                        eventSet)
                : null);
        if (((visualFeatures != null) && (visualFeatures.isEmpty() == false))
                || ((dialogDescription != null)
                        && (dialogDescription.isEmpty() == false))) {
            if ((visualFeatures != null)
                    && (visualFeatures.isEmpty() == false)) {
                messenger.getToolParameterGatherer().getToolSpatialInput(
                        ToolType.RECOMMENDER, visualFeatures,
                        new SpatialParametersReceiver());
            }
            if ((dialogDescription != null)
                    && (dialogDescription.isEmpty() == false)) {
                dialogDescription
                        .put(HazardConstants.FILE_PATH_KEY,
                                recommenderEngine
                                        .getInventory(
                                                runningRecommenderIdentifier)
                                        .getFile().getFile().getPath());
                messenger.getToolParameterGatherer().getToolParameters(
                        ToolType.RECOMMENDER, dialogDescription,
                        new DialogParametersReceiver());
            }
        } else {
            executeRunningRecommender(null, null);
        }
    }

    /**
     * Execute the recommender that is currently running with the specified
     * user-provided dialog and/or spatial parameters. It is assumed that a
     * recommender is indeed currently running.
     * 
     * @param visualFeatures
     *            List of visual features provided by the recommender earlier to
     *            allow the user to input spatial info, if any.
     * @param dialogInfo
     *            Map of dialog parameters, if any.
     */
    @SuppressWarnings("unchecked")
    private void executeRunningRecommender(VisualFeaturesList visualFeatures,
            Map<String, Serializable> dialogInfo) {

        /*
         * Create the event set, and if the creation fails, cancel the running
         * of the recommender.
         */
        EventSet<IEvent> eventSet = createEventSet();
        if (eventSet == null) {
            cancelRunningRecommender();
            return;
        }

        /*
         * Clear the set of removed event identifiers, since it should only hold
         * identifiers of events that have been removed since the last
         * commencement of a recommender's execution, and said commencement is
         * about to occur. Do the same thing with the map of event identifiers
         * to modifications.
         */
        synchronized (pendingRecommenderExecutionRequests) {
            identifiersOfEventsRemovedSinceLastRecommenderRun.clear();
            modificationsForIdentifiersOfEventsModifiedSinceLastRecommenderRun
                    .clear();
        }

        /*
         * Get the engine to initiate the execution of the recommender.
         */
        final String toolName = (String) getRecommenderMetadata(
                runningRecommenderIdentifier)
                        .get(HazardConstants.RECOMMENDER_METADATA_TOOL_NAME);
        recommenderEngine.runExecuteRecommender(runningRecommenderIdentifier,
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
                                 * If a results message was supplied, display
                                 * the message for the user. Otherwise, if a
                                 * results dialog description was provided, show
                                 * the dialog. In the latter case, note that
                                 * this is occurring, so that the next
                                 * recommender to be run in the sequence (if
                                 * any) will not be run a few lines down -- the
                                 * results dialog must be closed by the user
                                 * before the next one is run.
                                 */
                                boolean showingResultsDialog = false;
                                String resultMessage = (String) result
                                        .getAttribute(
                                                HazardConstants.RECOMMENDER_RESULT_MESSAGE);
                                Map<String, Serializable> resultDialogDescription = (Map<String, Serializable>) result
                                        .getAttribute(
                                                HazardConstants.RECOMMENDER_RESULT_DIALOG);
                                if (resultMessage != null) {
                                    messenger.getWarner().warnUser(toolName,
                                            resultMessage);
                                } else if (resultDialogDescription != null) {
                                    showingResultsDialog = true;
                                    resultDialogDescription.put(
                                            HazardConstants.FILE_PATH_KEY,
                                            recommenderEngine
                                                    .getInventory(
                                                            runningRecommenderIdentifier)
                                                    .getFile().getFile()
                                                    .getPath());
                                    messenger.getToolParameterGatherer()
                                            .showToolResults(
                                                    ToolType.RECOMMENDER,
                                                    resultDialogDescription,
                                                    new ResultsDisplayCompleteNotifier());
                                }

                                /*
                                 * Handle the resulting changes to the session.
                                 */
                                handleRecommenderResult(
                                        runningRecommenderIdentifier, result);

                                /*
                                 * If no results are being displayed, run the
                                 * next recommender in the sequence of
                                 * recommenders, if any.
                                 */
                                if (showingResultsDialog == false) {
                                    finishRunningRecommender();
                                }
                            }
                        });
                    }

                    @Override
                    public void jobFailed(Throwable e) {
                        statusHandler.error(
                                "Recommender " + toolName + " failed.", e);
                    }
                });
    }

    /**
     * Finish up execution of the currently running recommender. It is assumed
     * that a recommender is indeed currently running.
     */
    private void finishRunningRecommender() {

        /*
         * If the currently running recommender is being run as part of a list
         * of recommenders to be run sequentially, then run the next one.
         * Otherwise, execute the next recommender in the requests queue, if
         * any.
         */
        long contextIdentifier = runningContext.getIdentifier();
        List<String> sequentialRecommenders = sequentialRecommendersForExecutionContextIdentifiers
                .get(contextIdentifier);
        if (sequentialRecommenders != null) {

            /*
             * Get the identifier of the next recommender to be run in the
             * sequence and remember it. If said next recommender is the last
             * one in the sequence, remove the list from the
             * sequential-recommenders map since it will no longer be
             * referenced.
             */
            int nextIndex = sequentialRecommenders
                    .indexOf(runningRecommenderIdentifier) + 1;
            synchronized (pendingRecommenderExecutionRequests) {
                runningRecommenderIdentifier = sequentialRecommenders
                        .get(nextIndex);
            }
            if (nextIndex == sequentialRecommenders.size() - 1) {
                sequentialRecommendersForExecutionContextIdentifiers
                        .remove(contextIdentifier);
            }

            /*
             * Run the next recommender.
             */
            runRecommenderGatheringParametersAsNecessary();
        } else {

            /*
             * If the site has been updated during the running of the
             * recommender, process it now.
             */
            updateSiteIfNecessary();

            /*
             * Run the next requested recommender if any requests are pending
             * and batching is off.
             */
            setBatchingToggleAndRunNextRequestedRecommender(
                    SetBooleanAction.DO_NOT_SET, true);
        }
    }

    /**
     * Cancel the currently running recommender. It is assumed that a
     * recommender is indeed currently running.
     */
    private void cancelRunningRecommender() {

        /*
         * Remove the list of recommenders to be run sequentially after this
         * one, if such a list is found.
         */
        sequentialRecommendersForExecutionContextIdentifiers
                .remove(runningContext.getIdentifier());

        /*
         * Clean up after the attempted running of the recommender.
         */
        finishRunningRecommender();
    }

    /**
     * Set the batching flag as speciifed, and then run the next requested
     * recommender if appropriate.
     * 
     * @param setBatchingAction
     *            Action to be taken with regard to the batching flag.
     * @param assumeNoRunningRecommender
     *            Flag indicating whether or not to reset the running
     *            recommender tracker member variables if batching is now (or
     *            already was) toggled on or there are no pending recommender
     *            requests.
     */
    private void setBatchingToggleAndRunNextRequestedRecommender(
            SetBooleanAction setBatchingAction,
            boolean assumeNoRunningRecommender) {

        List<String> recommenderIdentifiers = null;
        synchronized (pendingRecommenderExecutionRequests) {

            /*
             * If the batching flag is to be set, set it now.
             */
            if (setBatchingAction == SetBooleanAction.SET_TRUE) {
                batching = true;
            } else if (setBatchingAction == SetBooleanAction.SET_FALSE) {
                batching = false;
            }

            /*
             * If there are recommender execution requests pending and batching
             * is toggled off, and either no recommender is running or there is
             * no reason to worry about whether one is running or not, handle
             * the next one. Otherwise, reset the trackers of the currently
             * running recommender if there is no reason to worry about whether
             * a recommender is running or not.
             */
            if ((batching == false)
                    && (pendingRecommenderExecutionRequests.isEmpty() == false)
                    && (assumeNoRunningRecommender
                            || (runningRecommenderIdentifier == null))) {

                /*
                 * Take the request at the head of the queue and record the
                 * first recomender identifier it provides the currently running
                 * recommender.
                 */
                RecommenderExecutionRequest request = pendingRecommenderExecutionRequests
                        .remove(0);
                recommenderIdentifiers = request.getRecommenderIdentifiers();
                runningRecommenderIdentifier = recommenderIdentifiers.get(0);
                runningContext = request.getContext();

            } else if (assumeNoRunningRecommender) {

                /*
                 * Reset the running recommender trackers.
                 */
                runningRecommenderIdentifier = null;
                runningContext = null;
            }
        }

        /*
         * If the above resulted in a new recommender now running, start the
         * process.
         */
        if (recommenderIdentifiers != null) {
            runRecommendersGatheringParametersAsNecessary(
                    recommenderIdentifiers);
        }
    }

    /**
     * Respond to the current site changing.
     * 
     * @param change
     *            Change that occurred.
     */
    private void siteChanged(SiteChanged change) {

        /*
         * If a recommender is currently running, remember the new site so that
         * it may be updated later. Otherwise, update the site immediately. The
         * delay when a recommender is running is to avoid having the site
         * change mid-execution for a recommender.
         */
        String newSiteIdentifier = sessionManager.getConfigurationManager()
                .getSiteID();
        synchronized (pendingRecommenderExecutionRequests) {
            if (runningRecommenderIdentifier != null) {
                updatedSiteIdentifier = newSiteIdentifier;
            } else {
                updateSite();
            }
        }
    }

    /**
     * Update the site if necessary, that is, if
     * {@link #siteChanged(SiteChanged)} was invoked while a recommender was
     * running.
     */
    private void updateSiteIfNecessary() {
        synchronized (pendingRecommenderExecutionRequests) {
            if (updatedSiteIdentifier != null) {
                updateSite();
            }
        }
    }

    /**
     * Update the site.
     */
    private void updateSite() {
        recommenderEngine.setSite(updatedSiteIdentifier);
        updatedSiteIdentifier = null;
    }

    /**
     * Get the metadata for the specified recommender.
     * 
     * @param recommenderIdentifier
     *            Identifier of the recommender for which to fetch the metadata.
     * @return Map of metadata keys to values for the recommender.
     */
    private Map<String, Serializable> getRecommenderMetadata(
            String recommenderIdentifier) {
        Map<String, Serializable> metadata = metadataForRecommenders
                .get(recommenderIdentifier);
        if (metadata == null) {
            metadata = recommenderEngine
                    .getScriptMetadata(recommenderIdentifier);
            metadataForRecommenders.put(recommenderIdentifier, metadata);
        }
        return metadata;
    }

    /**
     * Create an event set to run a recommender or to fetch dialog or spaital
     * parameters.
     * 
     * @return Event set that was created, or <code>null</code> if no event set
     *         could be created.
     */
    @SuppressWarnings("unchecked")
    private EventSet<IEvent> createEventSet() {

        /*
         * Get the recommender metadata, and decide what events are to be
         * included in the event set based upon its values.
         */
        Map<String, Serializable> metadata = getRecommenderMetadata(
                runningRecommenderIdentifier);
        Boolean onlyIncludeTriggerEvent = (Boolean) metadata.get(
                HazardConstants.RECOMMENDER_METADATA_ONLY_INCLUDE_TRIGGER_EVENTS);
        List<String> includeEventTypesList = (List<String>) metadata
                .get(HazardConstants.RECOMMENDER_METADATA_INCLUDE_EVENT_TYPES);
        Set<String> includeEventTypes = (includeEventTypesList != null
                ? new HashSet<>(includeEventTypesList) : null);
        Boolean includeDataLayerTimes = (Boolean) metadata.get(
                HazardConstants.RECOMMENDER_METADATA_INCLUDE_DATA_LAYER_TIMES);
        Boolean includeCwaGeometry = (Boolean) metadata
                .get(HazardConstants.RECOMMENDER_METADATA_INCLUDE_CWA_GEOMETRY);

        /*
         * Create the event set, determine which events are to be added to it
         * based upon the recommender metadata retrieved above, and add a copy
         * of each such event to the set.
         */
        EventSet<IEvent> eventSet = new EventSet<>();
        if (Boolean.TRUE.equals(onlyIncludeTriggerEvent) && ((runningContext
                .getTrigger() == Trigger.HAZARD_EVENT_VISUAL_FEATURE_CHANGE)
                || (runningContext
                        .getTrigger() == Trigger.HAZARD_EVENT_MODIFICATION)
                || (runningContext
                        .getTrigger() == Trigger.HAZARD_EVENT_SELECTION))) {

            /*
             * Since the recommender is meant to only be passed the triggering
             * hazard event(s), place only those into the input event set. If
             * they cannot be found, they may have been removed after they
             * caused the triggering of the recommender run. If none are found,
             * return nothing, since the event set cannot be created.
             */
            for (String eventIdentifier : runningContext
                    .getEventIdentifiers()) {
                IHazardEventView event = sessionManager.getEventManager()
                        .getEventById(eventIdentifier);
                if (event != null) {
                    eventSet.add(createBaseHazardEvent(event));
                }
            }
            if (eventSet.isEmpty()) {
                return null;
            }
        } else {

            /*
             * Include all events that belong (either every event in the
             * session, or only those events with the right hazard types) in the
             * input event set.
             */
            Collection<? extends IHazardEventView> hazardEvents = sessionManager
                    .getEventManager().getEvents();
            for (IHazardEventView event : hazardEvents) {
                if ((includeEventTypes == null)
                        || includeEventTypes.contains(event.getHazardType())) {
                    eventSet.add(createBaseHazardEvent(event));
                }
            }
        }

        /*
         * Add the execution context parameters to the event set.
         */
        addContextAsEventSetAttributes(runningContext, eventSet);

        /*
         * Add session information to event set.
         */
        long currentTime = sessionManager.getTimeManager().getCurrentTime()
                .getTime();
        eventSet.addAttribute(HazardConstants.CENTER_POINT_LAT_LON,
                getCenterPointAsDictionary());
        eventSet.addAttribute(HazardConstants.CURRENT_TIME, currentTime);
        eventSet.addAttribute(HazardConstants.SELECTED_TIME, sessionManager
                .getTimeManager().getSelectedTime().getLowerBound());
        eventSet.addAttribute(HazardConstants.FRAMES_INFO,
                getFramesInfoAsDictionary());
        eventSet.addAttribute(HazardConstants.USER_NAME,
                VizApp.getWsId().getUserName());
        eventSet.addAttribute(HazardConstants.WORKSTATION,
                VizApp.getWsId().getHostName());

        /*
         * If the data times are to be included, add them to the event set as
         * well, using a list of just the current time if none are available.
         */
        if (Boolean.TRUE.equals(includeDataLayerTimes)) {
            List<Long> dataLayerTimes = sessionManager
                    .getDisplayResourceContextProvider()
                    .getTimeMatchBasisDataLayerTimes();
            Serializable times = (dataLayerTimes == null
                    ? Lists.newArrayList(currentTime)
                    : (dataLayerTimes instanceof Serializable
                            ? (Serializable) dataLayerTimes
                            : new ArrayList<>(dataLayerTimes)));
            eventSet.addAttribute(HazardConstants.DATA_TIMES, times);
        }
        if (Boolean.TRUE.equals(includeCwaGeometry)) {
            eventSet.addAttribute(HazardConstants.CWA_GEOMETRY,
                    sessionManager.getEventManager().getCwaGeometry());
        }

        return eventSet;
    }

    /**
     * Create a base hazard event copy of the specified hazard event.
     * 
     * @param event
     *            Event to be copied.
     * @return Base hazard event copy.
     */
    private BaseHazardEvent createBaseHazardEvent(IHazardEventView event) {
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
        eventSet.addAttribute(HazardConstants.SITE_ID,
                sessionManager.getConfigurationManager().getSiteID());
        eventSet.addAttribute(HazardConstants.LOCALIZED_SITE_ID,
                LocalizationManager.getInstance().getSite());
        eventSet.addAttribute(HazardConstants.HAZARD_MODE,
                CAVEMode.getMode().toString());
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
            ISessionConfigurationManager<ObservedSettings> configManager = sessionManager
                    .getConfigurationManager();
            ISessionEventManager eventManager = sessionManager
                    .getEventManager();

            sessionManager.startBatchedChanges();

            /*
             * Get the attributes of the event set indicating whether any or all
             * events are to be saved to history lists or to the latest version
             * set, and determine if said attributes indicate that all events
             * provided in the event set should be saved in one of the two ways,
             * if specific ones should be saved one or both ways, or none of
             * them should be saved. Do the same for the treating of events that
             * are to be saved either way as issuances.
             */
            Serializable addToHistoryAttribute = events.getAttribute(
                    HazardConstants.RECOMMENDER_RESULT_SAVE_TO_HISTORY);
            Serializable addToDatabaseAttribute = events.getAttribute(
                    HazardConstants.RECOMMENDER_RESULT_SAVE_TO_DATABASE);
            boolean addToHistory = Boolean.TRUE.equals(addToHistoryAttribute);
            boolean addToDatabase = ((addToHistory == false)
                    && Boolean.TRUE.equals(addToDatabaseAttribute));

            /*
             * Determine whether or not the events that are to be saved to the
             * database are to be kept locked after the save, instead of the
             * usual practice of unlocking them. Also determine whether or not
             * any modifications being made to events should be counted as
             * modifications from the events' perspectives.
             */
            boolean keepLocked = Boolean.TRUE.equals(events.getAttribute(
                    HazardConstants.RECOMMENDER_RESULT_KEEP_LOCKED_WHEN_SAVING_TO_DATABASE));
            boolean doNotCountAsModification = Boolean.TRUE
                    .equals(events.getAttribute(
                            HazardConstants.RECOMMENDER_RESULT_DO_NOT_COUNT_AS_MODIFICATION));

            /*
             * Determine whether or not the events that are to be saved to the
             * history list and/or to the latest version set are to be treated
             * as issuances.
             */
            boolean treatAsIssuance = Boolean.TRUE.equals(events.getAttribute(
                    HazardConstants.RECOMMENDER_RESULT_TREAT_AS_ISSUANCE));

            /*
             * Determine whether or not all hazard events that are brand new
             * (i.e., just created by the recommender) should be saved to either
             * the history list or the database.
             */
            boolean saveAllNewToHistory = isListContainingNullElement(
                    addToHistoryAttribute);
            boolean saveAllNewToDatabase = ((saveAllNewToHistory == false)
                    && isListContainingNullElement(addToDatabaseAttribute));
            List<IHazardEventView> addedNewEvents = (saveAllNewToHistory
                    || saveAllNewToDatabase ? new ArrayList<IHazardEventView>()
                            : null);

            /*
             * Create a list to hold the events to be saved if all events are
             * specified as requiring saving. If instead specific events are
             * specified that are to be saved one or both ways, create a map
             * that will be used to pair the identifiers of events that are
             * created with the events themselves.
             */
            List<IHazardEventView> addedEvents = (addToHistory || addToDatabase
                    ? new ArrayList<IHazardEventView>(events.size()) : null);
            Map<String, IHazardEventView> addedEventsForIdentifiers = ((addedEvents == null)
                    && ((addToHistoryAttribute instanceof List)
                            || (addToDatabaseAttribute instanceof List))
                                    ? new HashMap<String, IHazardEventView>(
                                            events.size(), 1.0f)
                                    : null);

            /*
             * If a list of event identifiers for which the events are to be
             * deleted was provided, remember any (and their associated events)
             * that are for events that are found to exist and to never have
             * been issued.
             */
            Object toBeDeleted = events.getAttribute(
                    HazardConstants.RECOMMENDER_RESULT_DELETE_EVENT_IDENTIFIERS);
            Set<String> identifiersOfEventsToBeDeleted = null;
            IOriginator originator = new RecommenderOriginator(
                    recommenderIdentifier);
            if (toBeDeleted != null) {
                if (toBeDeleted instanceof Collection == false) {
                    statusHandler.warn("Ignoring " + recommenderIdentifier
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
                    List<IHazardEventView> eventsToBeDeleted = new ArrayList<>(
                            ((Collection<?>) toBeDeleted).size());
                    for (Object element : (Collection<?>) toBeDeleted) {
                        boolean success = false;
                        if (element instanceof String) {
                            IHazardEventView event = eventManager
                                    .getEventById((String) element);
                            if ((event != null)
                                    && (HazardStatus.hasEverBeenIssued(
                                            event.getStatus()) == false)) {
                                identifiersOfEventsToBeDeleted
                                        .add(event.getEventID());
                                eventsToBeDeleted.add(event);
                                success = true;
                            }
                        }
                        if (success == false) {
                            statusHandler.warn("Ignoring "
                                    + recommenderIdentifier
                                    + " result event set attribute \""
                                    + HazardConstants.RECOMMENDER_RESULT_DELETE_EVENT_IDENTIFIERS
                                    + "\" list element \"" + element
                                    + "\" because it is not an existing, never-issued event identifier.");
                        }
                    }

                    /*
                     * If there are any events that are to be deleted, delete
                     * them now.
                     */
                    if (eventsToBeDeleted.isEmpty() == false) {
                        eventManager.removeEvents(eventsToBeDeleted, false,
                                originator);
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
            List<IHazardEventView> addedOrModifiedEvents = new ArrayList<>(
                    events.size());
            synchronized (pendingRecommenderExecutionRequests) {
                for (IEvent event : events) {
                    if (event instanceof IHazardEvent) {

                        /*
                         * Get the hazard event, and if it is not new, ensure
                         * that it does not have the identifier of an event that
                         * was removed by some other action while this
                         * recommender was running. If it was removed during
                         * recommender execution, ignore it, as it should not be
                         * around anymore. Also ignore it if it is to be deleted
                         * per this recommender's request.
                         */
                        IHazardEvent hazardEvent = (IHazardEvent) event;
                        boolean isNew = ((hazardEvent.getEventID() == null)
                                || (hazardEvent.getEventID().trim()
                                        .length() == 0));
                        if ((isNew == false)
                                && (identifiersOfEventsRemovedSinceLastRecommenderRun
                                        .contains(hazardEvent.getEventID())
                                        || identifiersOfEventsToBeDeleted
                                                .contains(hazardEvent
                                                        .getEventID()))) {
                            continue;
                        }

                        /*
                         * Add the hazard area for the event.
                         */
                        if (HazardEventUtilities
                                .getHazardType(hazardEvent) != null) {
                            Map<String, String> ugcHatchingAlgorithms = eventManager
                                    .buildInitialHazardAreas(hazardEvent);
                            hazardEvent.addHazardAttribute(HAZARD_AREA,
                                    (Serializable) ugcHatchingAlgorithms);
                        }

                        /*
                         * If this is an existing hazard event, iterate through
                         * any modifications that the session copy has undergone
                         * since the recommender started running, modifying the
                         * recommender's result event copy to include all such
                         * modifications. This avoids having the changes made to
                         * the event within the session overwritten by the
                         * recommender's version.
                         */
                        if (isNew == false) {
                            List<IEventModification> modifications = modificationsForIdentifiersOfEventsModifiedSinceLastRecommenderRun
                                    .get(hazardEvent.getEventID());
                            if (modifications != null) {
                                IHazardEventView sessionEvent = eventManager
                                        .getEventById(hazardEvent.getEventID());
                                if (sessionEvent != null) {
                                    for (IEventModification modification : modifications) {
                                        modification.apply(sessionEvent,
                                                hazardEvent);
                                    }
                                }
                            }
                        }

                        /*
                         * Add the site identifier if it not already set, set
                         * the workstation identifier and source.
                         */
                        if (hazardEvent.getSiteID() == null) {
                            hazardEvent.setSiteID(configManager.getSiteID());
                        }
                        hazardEvent.setWsId(VizApp.getWsId());
                        hazardEvent.setSource(IHazardEvent.Source.RECOMMENDER);

                        /*
                         * Add the event if it is new, or modify an existing
                         * event by merging the new version into it.
                         */
                        hazardEvent.removeHazardAttribute(
                                HazardConstants.HAZARD_EVENT_SELECTED);
                        IHazardEventView addedEvent = null;
                        if (isNew) {
                            try {
                                addedEvent = eventManager.addEvent(hazardEvent,
                                        originator);
                            } catch (HazardEventServiceException e) {
                                statusHandler.error(
                                        "Could not add hazard event generated by "
                                                + recommenderIdentifier + ".",
                                        e);
                                continue;
                            }
                        } else {
                            addedEvent = eventManager
                                    .getEventById(hazardEvent.getEventID());
                            if (addedEvent != null) {
                                EventPropertyChangeResult result = eventManager
                                        .mergeHazardEvents(hazardEvent,
                                                addedEvent, false, false, true,
                                                doNotCountAsModification,
                                                originator);
                                if (result != EventPropertyChangeResult.SUCCESS) {
                                    statusHandler
                                            .warn("Could not modify hazard event as requested by "
                                                    + recommenderIdentifier
                                                    + " due to "
                                                    + (result == EventPropertyChangeResult.FAILURE_DUE_TO_EVENT_NOT_FOUND
                                                            ? "an inability to find the original event."
                                                            : "an inability to lock the event."));
                                    continue;
                                }
                            }
                        }
                        addedOrModifiedEvents.add(addedEvent);

                        /*
                         * If the event is new and new events are to be all
                         * saved to history or database, add it to the new
                         * events list; otherwise, if all events (new or
                         * existing) are to be saved to history or database, add
                         * it to the list for all events; otherwise, if only
                         * some events are to be saved to one or both, place it
                         * in the map of identifiers to events.
                         */
                        if (isNew && (addedNewEvents != null)) {
                            addedNewEvents.add(addedEvent);
                        } else if (addedEvents != null) {
                            addedEvents.add(addedEvent);
                        } else if (addedEventsForIdentifiers != null) {
                            addedEventsForIdentifiers
                                    .put(addedEvent.getEventID(), addedEvent);
                        }
                    }
                }

                /*
                 * Clear the removed event identifiers and the modified events
                 * map.
                 */
                identifiersOfEventsRemovedSinceLastRecommenderRun.clear();
                modificationsForIdentifiersOfEventsModifiedSinceLastRecommenderRun
                        .clear();
            }

            /*
             * If all brand-new hazard events are to be saved to the history or
             * database, perform the save.
             */
            if (addedNewEvents != null) {
                eventManager.saveEvents(addedNewEvents, saveAllNewToHistory,
                        keepLocked, treatAsIssuance, originator);
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
                eventManager.saveEvents(addedEvents, addToHistory, keepLocked,
                        treatAsIssuance, originator);
            } else if ((addedEventsForIdentifiers != null)
                    && (addedEventsForIdentifiers.isEmpty() == false)) {
                if (addToHistoryAttribute instanceof List) {
                    eventManager.saveEvents(
                            getEventsFromIdentifiers(
                                    (List<?>) addToHistoryAttribute,
                                    addedEventsForIdentifiers),
                            true, false, treatAsIssuance, originator);
                } else if (addToDatabaseAttribute instanceof List) {

                    /*
                     * Ensure that if a hazard identifier is present in both
                     * this list and the list for history list saving, it is
                     * removed from this list, since it has already been saved
                     * above.
                     */
                    List<?> addToDatabaseList = null;
                    if (addToHistoryAttribute instanceof List) {
                        Set<?> pruned = Sets.difference(
                                new HashSet<>((List<?>) addToDatabaseAttribute),
                                new HashSet<>((List<?>) addToHistoryAttribute));
                        addToDatabaseList = new ArrayList<>(pruned);
                    } else {
                        addToDatabaseList = (List<?>) addToDatabaseAttribute;
                    }
                    eventManager.saveEvents(
                            getEventsFromIdentifiers(addToDatabaseList,
                                    addedEventsForIdentifiers),
                            false, keepLocked, treatAsIssuance, originator);
                }
            }

            /*
             * Make sure the updated hazard type is a part of the visible types
             * in the current setting. If not, add it.
             */
            Set<String> visibleTypes = configManager.getSettings()
                    .getVisibleTypes();
            int startSize = visibleTypes.size();
            for (IHazardEventView event : addedOrModifiedEvents) {
                String hazardType = HazardEventUtilities.getHazardType(event);
                if (hazardType != null) {
                    visibleTypes.add(hazardType);
                }
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
                    selectedTime = (list.size() > 1
                            ? new SelectedTime(
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

            sessionManager.finishBatchedChanges();
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
    private List<IHazardEventView> getEventsFromIdentifiers(
            List<?> eventIdentifiers,
            Map<String, IHazardEventView> eventsForIdentifiers) {

        Set<String> identifiersToSave = new HashSet<>(
                eventsForIdentifiers.size(), 1.0f);
        for (Object element : eventIdentifiers) {
            if (element != null) {
                identifiersToSave.add(element.toString());
            }
        }
        List<IHazardEventView> eventsToSave = new ArrayList<>(
                identifiersToSave.size());
        for (String identifier : Sets.intersection(
                eventsForIdentifiers.keySet(), identifiersToSave)) {
            eventsToSave.add(eventsForIdentifiers.get(identifier));
        }
        return eventsToSave;
    }
}

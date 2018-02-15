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
import com.raytheon.uf.common.dataplugin.events.GenericSessionObjectManager;
import com.raytheon.uf.common.dataplugin.events.IEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardEventFirstClassAttribute;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardStatus;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.RecommenderTriggerOrigin;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.Trigger;
import com.raytheon.uf.common.dataplugin.events.hazards.event.AbstractHazardServicesEventIdUtil;
import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardEventUtilities;
import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardServicesEventIdUtil;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEventView;
import com.raytheon.uf.common.dataplugin.events.hazards.event.SessionHazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.registry.HazardEventServiceException;
import com.raytheon.uf.common.python.concurrent.IPythonJobListener;
import com.raytheon.uf.common.recommenders.AbstractRecommenderEngine;
import com.raytheon.uf.common.recommenders.EventRecommender;
import com.raytheon.uf.common.recommenders.executors.MutablePropertiesAndVisualFeatures;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.DataTime;
import com.raytheon.uf.common.time.SimulatedTime;
import com.raytheon.uf.common.time.TimeRange;
import com.raytheon.uf.common.util.Pair;
import com.raytheon.uf.viz.core.VizApp;
import com.raytheon.uf.viz.core.drawables.IDescriptor.FramesInfo;
import com.raytheon.uf.viz.core.localization.LocalizationManager;
import com.raytheon.uf.viz.hazards.sessionmanager.ISessionManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.ISessionConfigurationManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.SiteChanged;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.ObservedSettings;
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
import com.raytheon.uf.viz.hazards.sessionmanager.tools.ToolExecutionIdentifier;
import com.raytheon.uf.viz.hazards.sessionmanager.tools.ToolParameterDialogSpecifier;
import com.raytheon.uf.viz.hazards.sessionmanager.tools.ToolResultDialogSpecifier;
import com.raytheon.uf.viz.recommenders.CAVERecommenderEngine;
import com.raytheon.viz.core.mode.CAVEMode;
import com.vividsolutions.jts.geom.Coordinate;

import gov.noaa.gsd.common.utilities.ICurrentTimeProvider;
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
 * Feb 16, 2017   28708    Chris.Golden Added setting is issue site identifier for
 *                                      recommended events as necessary.
 * Feb 21, 2017   29138    Chris.Golden Added use of session manager's runnable
 *                                      asynchronous scheduler.
 * Apr 13, 2017   33142    Chris.Golden Added use of new method in session manager to
 *                                      clear the set of identifiers of events that
 *                                      have been removed when initiating recommender
 *                                      execution.
 * Apr 28, 2017   33430    Robert.Blum  Removed use of HazardMode.
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
 * Feb 21, 2018   46736    Chris.Golden Changed to allow a hazard event to not be locked
 *                                      when being changed if it is marked as a "do not
 *                                      count as a modification" change by the
 *                                      recommender, and mergeHazardEvents() is now
 *                                      called using its new signature.
 * Feb 28, 2018   47113    Chris.Golden Simplified recommender result handling code that
 *                                      saves provided events to the history list or to
 *                                      the database, and made it more flexible by now
 *                                      allowing the "keep locked" result event set
 *                                      attribute to specify event identifiers, instead
 *                                      of only being a boolean.
 * Mar 20, 2018   48027    Chris.Golden Changed to ensure that if an event returned by
 *                                      a recommender is to be persisted to the history
 *                                      list as per the recommender's instructions,
 *                                      the event is not auto-persisted due to a status
 *                                      change when it is merged into the session copy
 *                                      of the event.
 * Mar 29, 2018   48027    Chris.Golden Removed "hazard event visual feature changed"
 *                                      recommender trigger, as it has been folded into
 *                                      "hazard event modified".
 * May 08, 2018   15561    Chris.Golden Changed BaseHazardEvent to SessionHazardEvent.
 * May 22, 2018    3782    Chris.Golden Changed recommender parameter gathering to be
 *                                      much more flexible, allowing the user to change
 *                                      dialog parameters together with visual features,
 *                                      and allowing visual feature changes to be made
 *                                      multiple times before the execution proceeds.
 *                                      Also synchronized access to member variables.
 *                                      Also refactored tool dialog so that it does not
 *                                      take raw maps for its parameters, and to be
 *                                      closer to the MVP design guidelines.
 * May 30, 2018   14791    Chris.Golden Added generic session object manager usage, so
 *                                      that recommenders can store objects that persist
 *                                      on the client for the duration of the session.
 * Jun 06, 2018   15561    Chris.Golden Added use of temporary event identifiers for
 *                                      events created by recommenders.
 * 
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

    // Private Static Constants

    /**
     * Maximum size of the
     * {@link #sequentialRecommendersForExecutionContextIdentifiers} map.
     */
    private static final int MAXIMUM_SEQUENTIAL_RECOMMENDERS_MAP_SIZE = 100;

    /**
     * Current time provider.
     */
    private static final ICurrentTimeProvider CURRENT_TIME_PROVIDER = new ICurrentTimeProvider() {
        @Override
        public long getCurrentTime() {
            return SimulatedTime.getSystemTime().getMillis();
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
     * Flag indicating whether or not practice mode is in effect.
     */
    private final boolean practiceMode = !CAVEMode.OPERATIONAL
            .equals(CAVEMode.getMode());

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
     * Generic session object manager, used by recommenders to store and
     * retrieve arbitrary values associated with keys of the recommenders'
     * choice as needed.
     */
    private final GenericSessionObjectManager genericSessionObjectManager = new GenericSessionObjectManager();

    /**
     * Event set in use during any parameter gathering from the user.
     */
    private EventSet<IEvent> parameterGatheringEventSet;

    /**
     * Flag indicating whether parameter gathering via dialog is currently
     * occurring.
     */
    private boolean parameterGatheringViaDialog;

    /**
     * Mutable dialog properties, in use during dialog-based parameter gathering
     * from the user.
     */
    private Map<String, Map<String, Object>> parameterGatheringMutableProperties;

    /**
     * Visual features used for gathering information, in use during
     * dialog-based parameter gathering from the user.
     */
    private VisualFeaturesList parameterGatheringVisualFeatures;

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
                                    Collections.<String> emptySet(),
                                    RecommenderTriggerOrigin.USER));
        }
    }

    @Override
    public void parameterDialogChanged(ToolExecutionIdentifier identifier,
            Collection<String> parameterIdentifiers,
            Map<String, Map<String, Object>> mutableProperties) {
        if (isRelevant(identifier)) {

            /*
             * Remember the mutable properties.
             */
            parameterGatheringMutableProperties = mutableProperties;

            /*
             * Let the recommender handle the parameter change (or
             * initialization), and get the resulting mutable properties that
             * have changed, and the new set of visual features, if any.
             */
            MutablePropertiesAndVisualFeatures results = recommenderEngine
                    .handleDialogParameterChange(identifier.getToolIdentifier(),
                            null, parameterIdentifiers,
                            parameterGatheringMutableProperties,
                            Collections.<String> emptySet(),
                            parameterGatheringVisualFeatures, true);

            /*
             * Ensure that the parameter gatherers are updated.
             */
            updateDialogParameterGatherers(identifier, results);
        } else {
            statusHandler.error(
                    "Received dialog parameters for recommender " + identifier
                            + " which is not currently running; ignoring",
                    new Exception());
        }
    }

    @Override
    public void parameterDialogComplete(ToolExecutionIdentifier identifier,
            Map<String, Serializable> valuesForParameters) {
        if (isRelevant(identifier)) {

            /*
             * Get the visual features to be displayed while the recommender is
             * running, if any, and send them to the spatial display.
             */
            MutablePropertiesAndVisualFeatures result = recommenderEngine
                    .handleDialogParameterChange(identifier.getToolIdentifier(),
                            null, Collections.<String> emptySet(),
                            parameterGatheringMutableProperties,
                            Collections.<String> emptySet(),
                            parameterGatheringVisualFeatures, false);
            messenger.getToolParameterGatherer().getToolSpatialInput(identifier,
                    (result != null ? result.getVisualFeatures() : null));

            /*
             * Execute the recommender.
             */
            executeRunningRecommender(parameterGatheringVisualFeatures,
                    valuesForParameters);
        } else {
            statusHandler.error(
                    "Received dialog parameters for recommender " + identifier
                            + " which is not currently running; ignoring",
                    new Exception());
        }
    }

    @Override
    public void parameterDialogCancelled(ToolExecutionIdentifier identifier) {
        if (isRelevant(identifier)) {
            cancelRunningRecommender();
        } else {
            statusHandler.error(
                    "Received dialog cancellation for recommender " + identifier
                            + " which is not currently running; ignoring",
                    new Exception());
        }
    }

    @Override
    public void spatialParametersChanged(ToolExecutionIdentifier identifier,
            Collection<String> parameterIdentifiers,
            VisualFeaturesList parameters) {
        if (isRelevant(identifier)) {

            /*
             * If the visual features that changed are part of dialog-based
             * parameter gathering, handle the change one way; if part of a
             * spatial-only parameter gathering, handle it another way.
             */
            if (parameterGatheringViaDialog) {

                /*
                 * Do nothing if the recommender's handle-parameter-change
                 * dialog is not to be invoked.
                 */
                if (Boolean.TRUE.equals(getRecommenderMetadata(
                        identifier.getToolIdentifier()).get(
                                HazardConstants.RECOMMENDER_METADATA_HANDLE_DIALOG_PARAMETER_CHANGES)) == false) {
                    return;
                }

                /*
                 * Let the recommender handle the parameter change, and get the
                 * resulting mutable properties that have changed, and the new
                 * set of visual features, if any.
                 */
                MutablePropertiesAndVisualFeatures results = recommenderEngine
                        .handleDialogParameterChange(
                                identifier.getToolIdentifier(), null,
                                Collections.<String> emptySet(),
                                parameterGatheringMutableProperties,
                                parameterIdentifiers, parameters, true);

                /*
                 * Ensure that the parameter gatherers are updated.
                 */
                updateDialogParameterGatherers(identifier, results);

            } else {

                /*
                 * Check to see if the process of spatial input gathering is not
                 * complete.
                 */
                boolean continueGatheringSpatialInfo = (recommenderEngine
                        .isSpatialInfoComplete(identifier.getToolIdentifier(),
                                null, parameters) == false);

                /*
                 * Get the visual features to be displayed either for the next
                 * round of gathering, or to show the user while the recommender
                 * is executing (the latter if gathering of spatial input is
                 * complete).
                 */
                VisualFeaturesList visualFeatures = recommenderEngine
                        .getSpatialInfo(identifier.getToolIdentifier(), null,
                                parameters, continueGatheringSpatialInfo);

                /*
                 * If spatial input must continue to be gathered, pass the
                 * visual features on so that they are displayed; otherwise,
                 * pass on any visual features to be used for display purposes
                 * only while the recommender is running, and execute the
                 * recommender.
                 */
                messenger.getToolParameterGatherer()
                        .getToolSpatialInput(identifier, visualFeatures);
                if (continueGatheringSpatialInfo == false) {
                    executeRunningRecommender(parameters, null);
                }
            }
        } else {
            statusHandler.error(
                    "Received spatial parameters for recommender " + identifier
                            + " which is not currently running; ignoring",
                    new Exception());
        }
    }

    @Override
    public void resultDialogClosed(ToolExecutionIdentifier identifier) {
        if (isRelevant(identifier)) {
            finishRunningRecommender();
        } else {
            statusHandler.error(
                    "Received results display completion notification for recommender "
                            + identifier
                            + " which is not currently running; ignoring",
                    new Exception());
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
         * Iterate through the modifications, compiling a map (with iteration
         * order being the order in which elements are added, to ensure that
         * entries put in first are iterated through first) pairing triggered
         * recommender identifiers with both the hazard attributes (generic and
         * first-class) and/or the visual features that triggered them.
         */
        Map<String, Pair<Set<String>, Set<String>>> modifiedAttributesForTriggeredRecommenders = new LinkedHashMap<>();
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
                addTriggeredRecommenderEntryForModifiedVisualFeatures(
                        modifiedAttributesForTriggeredRecommenders,
                        notification,
                        ((EventVisualFeaturesModification) modification)
                                .getVisualFeatureIdentifiers());
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
         */
        for (Map.Entry<String, Pair<Set<String>, Set<String>>> entry : modifiedAttributesForTriggeredRecommenders
                .entrySet()) {
            runAttributeTriggeredRecommender(entry.getKey(),
                    entry.getValue().getFirst(), entry.getValue().getSecond(),
                    notification);
        }
    }

    /**
     * If visual feature changes are associated with a recommender to be
     * triggered, update the specified map to include an entry pairing said
     * recommender's identifier with these visual features (or with these visual
     * features and any others that are already found as the value within the
     * map for said recommender).
     * 
     * @param modifiedAttributesForTriggeredRecommenders
     *            Map pairing recommender identifiers with two things: the
     *            attributes that trigger them, and the visual features that
     *            trigger them (one of the two may be empty).
     * @param notification
     *            Notification of the event modification that is being
     *            considered.
     * @param visualFeatures
     *            Identifiers of the visual features that have changed.
     */
    private void addTriggeredRecommenderEntryForModifiedVisualFeatures(
            Map<String, Pair<Set<String>, Set<String>>> modifiedAttributesForTriggeredRecommenders,
            SessionEventModified notification, Set<String> visualFeatures) {
        String recommender = getTriggeredRecommenderForFirstClassAttributeChange(
                notification.getEvent(),
                HazardEventFirstClassAttribute.VISUAL_FEATURE,
                notification.getOriginator());
        if (recommender != null) {
            Pair<Set<String>, Set<String>> modifiedAttributes = modifiedAttributesForTriggeredRecommenders
                    .get(recommender);
            if (modifiedAttributes == null) {
                modifiedAttributesForTriggeredRecommenders.put(recommender,
                        new Pair<Set<String>, Set<String>>(
                                new HashSet<String>(),
                                new HashSet<>(visualFeatures)));
            } else {
                modifiedAttributes.getSecond().addAll(visualFeatures);
            }
        }
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
     *            Map pairing recommender identifiers with two things: the
     *            attributes that trigger them, and the visual features that
     *            trigger them (one of the two may be empty).
     * @param notification
     *            Notification of the event modification that is being
     *            considered.
     * @param attribute
     *            First-class hazard event attribute that was modified; must be
     *            anything but
     *            {@link HazardEventFirstClassAttribute#VISUAL_FEATURE}, since
     *            the latter should be handled via
     *            {@link #addTriggeredRecommenderEntryForModifiedVisualFeatures(Map, SessionEventModified, Set)}
     *            .
     */
    private void addTriggeredRecommenderEntryForModifiedAttribute(
            Map<String, Pair<Set<String>, Set<String>>> modifiedAttributesForTriggeredRecommenders,
            SessionEventModified notification,
            HazardEventFirstClassAttribute attribute) {
        String recommender = getTriggeredRecommenderForFirstClassAttributeChange(
                notification.getEvent(), attribute,
                notification.getOriginator());
        if (recommender != null) {
            Pair<Set<String>, Set<String>> modifiedAttributes = modifiedAttributesForTriggeredRecommenders
                    .get(recommender);
            if (modifiedAttributes == null) {
                modifiedAttributesForTriggeredRecommenders.put(recommender,
                        new Pair<Set<String>, Set<String>>(
                                Sets.newHashSet(attribute.toString()),
                                new HashSet<String>()));
            } else {
                modifiedAttributes.getFirst().add(attribute.toString());
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
     *            Map pairing recommender identifiers with two things: the
     *            attributes that trigger them, and the visual features that
     *            trigger them (one of the two may be empty).
     * @param notification
     *            Notification of the event modification that is being
     *            considered.
     * @param attributes
     *            Generic attributes that were modified.
     */
    private void addTriggeredRecommenderEntryForModifiedAttributes(
            Map<String, Pair<Set<String>, Set<String>>> modifiedAttributesForTriggeredRecommenders,
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
                        .getFirst().add(trigger);
            } else {
                modifiedAttributesForTriggeredRecommenders.put(recommender,
                        new Pair<Set<String>, Set<String>>(
                                Sets.newHashSet(trigger),
                                new HashSet<String>()));
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
     * Run the specified attribute triggered recommender with the specified
     * attribute identifiers as triggers.
     * 
     * @param recommenderIdentifier
     *            Identifier of the recommender to run if appropriate.
     * @param attributeIdentifiers
     *            Attribute identifiers to pass to the recommender as the
     *            triggering elements.
     * @param visualFeatureIdentifiers
     *            Visual feature identifiers to pass to the recommender as the
     *            triggering elements.
     * @param notification
     *            Notification containing the attribute modifications.
     */
    private void runAttributeTriggeredRecommender(String recommenderIdentifier,
            Set<String> attributeIdentifiers,
            Set<String> visualFeatureIdentifiers,
            SessionEventModified notification) {
        runRecommender(recommenderIdentifier,
                RecommenderExecutionContext.getHazardEventModificationContext(
                        notification.getEvent().getEventID(),
                        attributeIdentifiers, visualFeatureIdentifiers,
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
     * Update the dialog parameter gatherer user interface elements by applying
     * the specified changed mutable properties and visual features.
     * 
     * @param identifier
     *            Tool execution identifier.
     * @param mutablePropertiesAndVisualFeatures
     *            Changed mutable properties, and the set of visual features.
     */
    private void updateDialogParameterGatherers(
            ToolExecutionIdentifier identifier,
            MutablePropertiesAndVisualFeatures mutablePropertiesAndVisualFeatures) {

        /*
         * Merge the changed mutable properties into the copy of all mutable
         * properties held by this object, and forward the changes to the
         * dialog.
         */
        Map<String, Map<String, Object>> changedMutableProperties = mutablePropertiesAndVisualFeatures
                .getMutableProperties();
        if (changedMutableProperties != null) {
            for (Map.Entry<String, Map<String, Object>> entry : changedMutableProperties
                    .entrySet()) {
                parameterGatheringMutableProperties.get(entry.getKey())
                        .putAll(entry.getValue());
            }
            messenger.getToolParameterGatherer()
                    .updateToolParameters(identifier, changedMutableProperties);
        }

        /*
         * Track the new visual features list, and forward the changes to the
         * spatial display.
         */
        parameterGatheringVisualFeatures = mutablePropertiesAndVisualFeatures
                .getVisualFeatures();
        messenger.getToolParameterGatherer().getToolSpatialInput(identifier,
                parameterGatheringVisualFeatures);
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
            RecommenderExecutionContext context;
            synchronized (pendingRecommenderExecutionRequests) {
                context = runningContext;
            }
            sequentialRecommendersForExecutionContextIdentifiers
                    .put(context.getIdentifier(), recommenderIdentifiers);
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
        String recommenderIdentifier;
        RecommenderExecutionContext context;
        synchronized (pendingRecommenderExecutionRequests) {
            recommenderIdentifier = runningRecommenderIdentifier;
            context = runningContext;
        }
        Map<String, Serializable> metadata = getRecommenderMetadata(
                recommenderIdentifier);
        boolean getDialogInfoNeeded = (Boolean.FALSE.equals(metadata.get(
                HazardConstants.RECOMMENDER_METADATA_GET_DIALOG_INFO_NEEDED)) == false);
        boolean getSpatialInfoNeeded = (Boolean.FALSE.equals(metadata.get(
                HazardConstants.RECOMMENDER_METADATA_GET_SPATIAL_INFO_NEEDED)) == false);
        boolean handleDialogParameterChangeNeeded = Boolean.TRUE.equals(metadata
                .get(HazardConstants.RECOMMENDER_METADATA_HANDLE_DIALOG_PARAMETER_CHANGES));

        /*
         * Create the event set to be used if dialog and/or spatial info is to
         * be fetched; if no event set can be created, cancel the running of the
         * recommender.
         */
        if (getDialogInfoNeeded || getSpatialInfoNeeded) {
            parameterGatheringEventSet = createEventSet();
            if (parameterGatheringEventSet == null) {
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
                ? recommenderEngine.getSpatialInfo(recommenderIdentifier,
                        parameterGatheringEventSet, null, true)
                : null);
        Map<String, Serializable> dialogDescription = (getDialogInfoNeeded
                ? recommenderEngine.getDialogInfo(recommenderIdentifier,
                        parameterGatheringEventSet)
                : null);
        if (((visualFeatures != null) && (visualFeatures.isEmpty() == false))
                || ((dialogDescription != null)
                        && (dialogDescription.isEmpty() == false))) {
            if ((visualFeatures != null)
                    && (visualFeatures.isEmpty() == false)) {
                messenger.getToolParameterGatherer()
                        .getToolSpatialInput(
                                new ToolExecutionIdentifier(
                                        recommenderIdentifier, context),
                                visualFeatures);
            }
            if ((dialogDescription != null)
                    && (dialogDescription.isEmpty() == false)) {

                TimeRange timeRange = sessionManager.getTimeManager()
                        .getVisibleTimeRange();
                parameterGatheringViaDialog = true;
                parameterGatheringVisualFeatures = getVisualFeaturesFromRawSpecifier(
                        dialogDescription);

                ToolParameterDialogSpecifier dialogSpecifier = new ToolParameterDialogSpecifier(
                        dialogDescription,
                        recommenderEngine.getInventory(recommenderIdentifier)
                                .getFile().getFile().getPath(),
                        timeRange.getStart().getTime(),
                        timeRange.getEnd().getTime(), CURRENT_TIME_PROVIDER);

                messenger.getToolParameterGatherer().getToolParameters(
                        new ToolExecutionIdentifier(recommenderIdentifier,
                                context),
                        dialogSpecifier, handleDialogParameterChangeNeeded);
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
    private void executeRunningRecommender(VisualFeaturesList visualFeatures,
            Map<String, Serializable> dialogInfo) {

        /*
         * Forget any event set or parameters compiled for parameter gathering.
         */
        parameterGatheringEventSet = null;
        parameterGatheringViaDialog = false;
        parameterGatheringMutableProperties = null;
        parameterGatheringVisualFeatures = null;

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
        String recommenderIdentifier;
        RecommenderExecutionContext context;
        synchronized (pendingRecommenderExecutionRequests) {
            recommenderIdentifier = runningRecommenderIdentifier;
            context = runningContext;
            identifiersOfEventsRemovedSinceLastRecommenderRun.clear();
            modificationsForIdentifiersOfEventsModifiedSinceLastRecommenderRun
                    .clear();
        }

        /*
         * Get the engine to initiate the execution of the recommender.
         */
        final String recommenderName = (String) getRecommenderMetadata(
                recommenderIdentifier)
                        .get(HazardConstants.RECOMMENDER_METADATA_TOOL_NAME);
        final ToolExecutionIdentifier toolExecutionIdentifier = new ToolExecutionIdentifier(
                recommenderIdentifier, context);
        recommenderEngine.runExecuteRecommender(recommenderIdentifier, eventSet,
                visualFeatures, dialogInfo,
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
                                         * Erase any spatial input visual
                                         * features that were showing.
                                         */
                                        messenger.getToolParameterGatherer()
                                                .finishToolSpatialInput(
                                                        toolExecutionIdentifier);

                                        /*
                                         * Handle the resulting changes to the
                                         * session.
                                         */
                                        Map<String, String> permanentIdsForTemporaryIds = handleRecommenderResult(
                                                toolExecutionIdentifier
                                                        .getToolIdentifier(),
                                                result);

                                        /*
                                         * Show any results dialog that was
                                         * specified by the results. If no
                                         * results are being displayed, run the
                                         * next recommender in the sequence of
                                         * recommenders, if any.
                                         */
                                        if (showResultsDialogAsAppropriate(
                                                recommenderName,
                                                toolExecutionIdentifier, result,
                                                permanentIdsForTemporaryIds) == false) {
                                            finishRunningRecommender();
                                        }
                                    }
                                });
                    }

                    @Override
                    public void jobFailed(final Throwable e) {

                        /*
                         * Ensure that the thread used to process the result is
                         * the one used by the session manager.
                         */
                        sessionManager.getRunnableAsynchronousScheduler()
                                .schedule(new Runnable() {

                                    @Override
                                    public void run() {
                                        messenger.getToolParameterGatherer()
                                                .finishToolSpatialInput(
                                                        toolExecutionIdentifier);
                                        statusHandler.error("Recommender "
                                                + recommenderName + " failed.",
                                                e);
                                    }
                                });
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
        long contextIdentifier;
        synchronized (pendingRecommenderExecutionRequests) {
            contextIdentifier = runningContext.getIdentifier();
        }
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
            int nextIndex;
            ;
            synchronized (pendingRecommenderExecutionRequests) {
                nextIndex = sequentialRecommenders
                        .indexOf(runningRecommenderIdentifier) + 1;
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
         * Forget any event set and parameters compiled for parameter gathering.
         */
        parameterGatheringEventSet = null;
        parameterGatheringViaDialog = false;
        parameterGatheringMutableProperties = null;
        parameterGatheringVisualFeatures = null;

        /*
         * Ensure that any gathering of spatial parameters is cancelled.
         */
        String recommenderIdentifier;
        RecommenderExecutionContext context;
        synchronized (pendingRecommenderExecutionRequests) {
            recommenderIdentifier = runningRecommenderIdentifier;
            context = runningContext;
        }
        ToolExecutionIdentifier toolExecutionIdentifier = new ToolExecutionIdentifier(
                recommenderIdentifier, context);
        messenger.getToolParameterGatherer()
                .finishToolSpatialInput(toolExecutionIdentifier);

        /*
         * Remove the list of recommenders to be run sequentially after this
         * one, if such a list is found.
         */
        long contextIdentifier;
        synchronized (pendingRecommenderExecutionRequests) {
            contextIdentifier = runningContext.getIdentifier();
        }
        sequentialRecommendersForExecutionContextIdentifiers
                .remove(contextIdentifier);

        /*
         * Clean up after the attempted running of the recommender.
         */
        finishRunningRecommender();
    }

    /**
     * Set the batching flag as specified, and then run the next requested
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
        String recommenderIdentifier;
        RecommenderExecutionContext context;
        synchronized (pendingRecommenderExecutionRequests) {
            recommenderIdentifier = runningRecommenderIdentifier;
            context = runningContext;
        }
        Map<String, Serializable> metadata = getRecommenderMetadata(
                recommenderIdentifier);
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
        if (Boolean.TRUE.equals(onlyIncludeTriggerEvent) && ((context
                .getTrigger() == Trigger.HAZARD_EVENT_MODIFICATION)
                || (context.getTrigger() == Trigger.HAZARD_EVENT_SELECTION))) {

            /*
             * Since the recommender is meant to only be passed the triggering
             * hazard event(s), place only those into the input event set. If
             * they cannot be found, they may have been removed after they
             * caused the triggering of the recommender run. If none are found,
             * return nothing, since the event set cannot be created.
             */
            for (String eventIdentifier : context.getEventIdentifiers()) {
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
        addContextAsEventSetAttributes(context, eventSet);

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
        eventSet.addAttribute(HazardConstants.SESSION_OBJECTS,
                genericSessionObjectManager);

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
    private SessionHazardEvent createBaseHazardEvent(IHazardEventView event) {
        SessionHazardEvent copy = new SessionHazardEvent(event);

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
        eventSet.addAttribute(HazardConstants.RUN_MODE,
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
        eventSet.addAttribute(
                HazardConstants.RECOMMENDER_TRIGGER_VISUAL_FEATURE_IDENTIFIERS,
                context.getVisualFeatureIdentifiers());
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
     * @return Map pairing any temporary event identifiers from events created
     *         by the recommender to the permanent identifiers said events were
     *         given when added to the session.
     */
    private Map<String, String> handleRecommenderResult(
            String recommenderIdentifier, EventSet<IEvent> events) {

        /*
         * If an event set was returned by the recommender as a result, ingest
         * the events and respond to any attributes included within the set.
         */
        Map<String, String> permanentIdsForTemporaryIds = new HashMap<>(
                events.size(), 1.0f);
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
            boolean addToHistory = Boolean.TRUE.equals(addToHistoryAttribute);
            Set<Object> addToHistoryEventIdentifiers = (addToHistoryAttribute instanceof Collection
                    ? new HashSet<>((Collection<?>) addToHistoryAttribute)
                    : Collections.emptySet());
            Serializable addToDatabaseAttribute = events.getAttribute(
                    HazardConstants.RECOMMENDER_RESULT_SAVE_TO_DATABASE);
            boolean addToDatabase = ((addToHistory == false)
                    && Boolean.TRUE.equals(addToDatabaseAttribute));
            Set<Object> addToDatabaseEventIdentifiers = ((addToHistory == false)
                    && (addToDatabaseAttribute instanceof Collection)
                            ? new HashSet<>(
                                    (Collection<?>) addToDatabaseAttribute)
                            : Collections.emptySet());

            /*
             * Determine whether or not the events that are to be saved to the
             * database are to be kept locked after the save, instead of the
             * usual practice of unlocking them.
             */
            Serializable keepLockedAttribute = events.getAttribute(
                    HazardConstants.RECOMMENDER_RESULT_KEEP_LOCKED_WHEN_SAVING_TO_DATABASE);
            boolean keepLocked = Boolean.TRUE.equals(keepLockedAttribute);
            if (addToHistory) {
                keepLocked = false;
            }
            Set<Object> keepLockedEventIdentifiers = ((addToHistory == false)
                    && (keepLockedAttribute instanceof Collection)
                            ? new HashSet<>((Collection<?>) keepLockedAttribute)
                            : Collections.emptySet());

            /*
             * Determine whether or not any modifications being made to events
             * should be counted as modifications from the events' perspectives.
             * Also determine whether or not the events that are to be saved to
             * the history list and/or to the latest version set are to be
             * treated as issuances.
             */
            boolean doNotCountAsModification = Boolean.TRUE
                    .equals(events.getAttribute(
                            HazardConstants.RECOMMENDER_RESULT_DO_NOT_COUNT_AS_MODIFICATION));
            boolean treatAsIssuance = Boolean.TRUE.equals(events.getAttribute(
                    HazardConstants.RECOMMENDER_RESULT_TREAT_AS_ISSUANCE));

            /*
             * Create three lists to hold the events to be saved to history
             * list, to database, and to database but kept locked, respectively.
             */
            List<IHazardEventView> resultingEventsToSaveToHistory = new ArrayList<IHazardEventView>(
                    events.size());
            List<IHazardEventView> resultingEventsToSaveToDatabase = new ArrayList<IHazardEventView>(
                    events.size());
            List<IHazardEventView> resultingEventsToSaveToDatabaseLocked = new ArrayList<IHazardEventView>(
                    events.size());

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
                    recommenderIdentifier, (doNotCountAsModification == false));
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
             * Iterate through the hazard events provided as the result, telling
             * the event manager to add them to the session or modifying the
             * existing copies in the session.
             */
            AbstractHazardServicesEventIdUtil eventIdUtil = HazardServicesEventIdUtil
                    .getInstance(practiceMode);
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
                                        .length() == 0)
                                || eventIdUtil.isTemporaryEventID(
                                        hazardEvent.getEventID()));
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
                         * Add the site identifier and/or issue site identifier
                         * if they are not already set, and then set the
                         * workstation identifier and source.
                         */
                        if (hazardEvent.getSiteID() == null) {
                            hazardEvent.setSiteID(configManager.getSiteID());
                        }
                        if (hazardEvent.getIssueSiteID() == null) {
                            hazardEvent
                                    .setIssueSiteID(configManager.getSiteID());
                        }

                        hazardEvent.setWsId(VizApp.getWsId());
                        hazardEvent.setSource(IHazardEvent.Source.RECOMMENDER);

                        /*
                         * Add the event if it is new, or modify an existing
                         * event by merging the new version into it.
                         */
                        hazardEvent.removeHazardAttribute(
                                HazardConstants.HAZARD_EVENT_SELECTED);
                        IHazardEventView resultingEvent = null;
                        if (isNew) {
                            try {
                                String temporaryEventIdentifier = hazardEvent
                                        .getEventID();
                                if ((temporaryEventIdentifier != null)
                                        && (temporaryEventIdentifier.trim()
                                                .length() == 0)) {
                                    temporaryEventIdentifier = null;
                                }
                                resultingEvent = eventManager
                                        .addEvent(hazardEvent, originator);
                                if (temporaryEventIdentifier != null) {
                                    permanentIdsForTemporaryIds.put(
                                            temporaryEventIdentifier,
                                            resultingEvent.getEventID());
                                }
                            } catch (HazardEventServiceException e) {
                                statusHandler.error(
                                        "Could not add hazard event generated by "
                                                + recommenderIdentifier + ".",
                                        e);
                                continue;
                            }
                        } else {
                            resultingEvent = eventManager
                                    .getEventById(hazardEvent.getEventID());
                            if (resultingEvent != null) {

                                /*
                                 * Merge the changes into the session copy of
                                 * the event. Only signal that the event should
                                 * be persisted if its status is changing if it
                                 * is not listed as one that is to be saved to
                                 * the history list; if true was supplied in all
                                 * cases for this parameter, the event would be
                                 * saved to the history list once for the status
                                 * change here and once for the
                                 * "save to history list" reason, if the latter
                                 * was specified by the recommender.
                                 */
                                EventPropertyChangeResult result = eventManager
                                        .mergeHazardEvents(hazardEvent,
                                                resultingEvent, false,
                                                ((addToHistory == false)
                                                        && (addToHistoryEventIdentifiers
                                                                .contains(
                                                                        resultingEvent
                                                                                .getEventID()) == false)),
                                                false, doNotCountAsModification,
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
                        addedOrModifiedEvents.add(resultingEvent);

                        /*
                         * If the event is to be saved to history or to the
                         * database, put it in the appropriate list. Note that
                         * the identifier provided by the event taken from the
                         * event set, and not the identifier of the session
                         * version of the event, is used, since the recommender
                         * results' lists of events to be saved to history, etc.
                         * will use the temporary identifiers.
                         */
                        if (addToHistory || addToHistoryEventIdentifiers
                                .contains(hazardEvent.getEventID())) {
                            resultingEventsToSaveToHistory.add(resultingEvent);
                        } else if (addToDatabase
                                || addToDatabaseEventIdentifiers
                                        .contains(hazardEvent.getEventID())) {
                            if (keepLocked || keepLockedEventIdentifiers
                                    .contains(hazardEvent.getEventID())) {
                                resultingEventsToSaveToDatabaseLocked
                                        .add(resultingEvent);
                            } else {
                                resultingEventsToSaveToDatabase
                                        .add(resultingEvent);
                            }
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
             * Save any events to the history list or the database as directed
             * by the recommender.
             */
            if (resultingEventsToSaveToHistory.isEmpty() == false) {
                eventManager.saveEvents(resultingEventsToSaveToHistory, true,
                        false, treatAsIssuance, originator);
            }
            if (resultingEventsToSaveToDatabase.isEmpty() == false) {
                eventManager.saveEvents(resultingEventsToSaveToDatabase, false,
                        false, treatAsIssuance, originator);
            }
            if (resultingEventsToSaveToDatabaseLocked.isEmpty() == false) {
                eventManager.saveEvents(resultingEventsToSaveToDatabaseLocked,
                        false, true, treatAsIssuance, originator);
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

        return permanentIdsForTemporaryIds;
    }

    /**
     * Show any results dialog called for by the specified resulting event set.
     * 
     * @param recommenderName
     *            Name of the recommender.
     * @param identifier
     *            Identifier of the recommender execution.
     * @param result
     *            Resulting event set that may indicate a results dialog should
     *            be shown.
     * @param permanentIdsForTemporaryIds
     *            Map pairing temporary event identifiers for events created by
     *            the recommender with the permanent identifiers said events
     *            were assigned when brought into the session.
     * @return <code>true</code> if a results dialog is being shown,
     *         <code>false</code> otherwise.
     */
    private boolean showResultsDialogAsAppropriate(String recommenderName,
            final ToolExecutionIdentifier identifier, EventSet<IEvent> result,
            Map<String, String> permanentIdsForTemporaryIds) {

        /*
         * If a results message was supplied, display the message for the user.
         * Otherwise, if a results dialog description was provided, show the
         * dialog. In either of these cases, note that this is occurring, so
         * that the next recommender to be run in the sequence (if any) will not
         * be run a few lines down -- the results dialog must be closed by the
         * user before the next one is run.
         */
        String resultMessage = (String) result
                .getAttribute(HazardConstants.RECOMMENDER_RESULT_MESSAGE);
        @SuppressWarnings("unchecked")
        Map<String, Serializable> resultDialogDescription = (Map<String, Serializable>) result
                .getAttribute(HazardConstants.RECOMMENDER_RESULT_DIALOG);
        if (resultMessage != null) {
            resultMessage = HazardServicesEventIdUtil.getInstance(practiceMode)
                    .replaceTemporaryEventIDs(resultMessage,
                            permanentIdsForTemporaryIds);
            messenger.getWarner().warnUserAsynchronously(recommenderName,
                    resultMessage, new Runnable() {
                        @Override
                        public void run() {
                            SessionRecommenderManager.this
                                    .resultDialogClosed(identifier);
                        }
                    });
            return true;
        } else if (resultDialogDescription != null) {
            TimeRange timeRange = sessionManager.getTimeManager()
                    .getVisibleTimeRange();
            resultDialogDescription = replaceTemporaryEventIdentifiers(
                    resultDialogDescription, permanentIdsForTemporaryIds);
            ToolResultDialogSpecifier dialogSpecifier = new ToolResultDialogSpecifier(
                    resultDialogDescription,
                    recommenderEngine
                            .getInventory(identifier.getToolIdentifier())
                            .getFile().getFile().getPath(),
                    timeRange.getStart().getTime(),
                    timeRange.getEnd().getTime(), CURRENT_TIME_PROVIDER);
            messenger.getToolParameterGatherer().showToolResults(identifier,
                    dialogSpecifier);
            return true;
        }
        return false;
    }

    /**
     * Find any temporary event identifiers found within strings that are values
     * of the specified map (or values in nested maps, or elements in nested
     * collections), and replace these temporary identifiers with their
     * permanent counterparts.
     * 
     * @param map
     *            Map in which to perform the substitution.
     * @param permanentIdsForTemporaryIds
     *            Map pairing temporary event identifiers for events created by
     *            the recommender with the permanent identifiers said events
     *            were assigned when brought into the session.
     * @return Map with substitutions.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Serializable> replaceTemporaryEventIdentifiers(
            Map<String, Serializable> map,
            Map<String, String> permanentIdsForTemporaryIds) {
        Map<String, Serializable> newMap = new HashMap<>(map.size(), 1.0f);
        for (Map.Entry<String, Serializable> entry : map.entrySet()) {
            if (entry.getValue() instanceof String) {
                newMap.put(entry.getKey(),
                        HazardServicesEventIdUtil.getInstance(practiceMode)
                                .replaceTemporaryEventIDs(
                                        (String) entry.getValue(),
                                        permanentIdsForTemporaryIds));
            } else if (entry.getValue() instanceof Map) {
                newMap.put(entry.getKey(),
                        (Serializable) replaceTemporaryEventIdentifiers(
                                (Map<String, Serializable>) entry.getValue(),
                                permanentIdsForTemporaryIds));
            } else if (entry.getValue() instanceof Collection) {
                newMap.put(entry.getKey(),
                        (Serializable) replaceTemporaryEventIdentifiers(
                                (Collection<Serializable>) entry.getValue(),
                                permanentIdsForTemporaryIds));
            } else {
                newMap.put(entry.getKey(), entry.getValue());
            }
        }
        return newMap;
    }

    /**
     * Find any temporary event identifiers found within strings that elements
     * of the specified collection (or values in nested maps, or elements in
     * nested collections), and replace these temporary identifiers with their
     * permanent counterparts.
     * 
     * @param collection
     *            Collection in which to perform the substitution.
     * @param permanentIdsForTemporaryIds
     *            Map pairing temporary event identifiers for events created by
     *            the recommender with the permanent identifiers said events
     *            were assigned when brought into the session.
     */
    @SuppressWarnings("unchecked")
    private Collection<Serializable> replaceTemporaryEventIdentifiers(
            Collection<Serializable> collection,
            Map<String, String> permanentIdsForTemporaryIds) {

        /*
         * Collection must be either a list or a set; otherwise, no replacement
         * will be done.
         */
        Collection<Serializable> newCollection = (collection instanceof List
                ? new ArrayList<Serializable>(collection.size())
                : (collection instanceof Set
                        ? new HashSet<Serializable>(collection.size(), 1.0f)
                        : null));
        if (newCollection == null) {
            statusHandler.warn("Could not perform temporary event identifier "
                    + "replacement on elements of collection of type "
                    + collection.getClass());
            return collection;
        }

        for (Serializable element : collection) {
            if (element instanceof String) {
                newCollection.add(HazardServicesEventIdUtil
                        .getInstance(practiceMode).replaceTemporaryEventIDs(
                                (String) element, permanentIdsForTemporaryIds));
            } else if (element instanceof Map) {
                newCollection
                        .add((Serializable) replaceTemporaryEventIdentifiers(
                                (Map<String, Serializable>) element,
                                permanentIdsForTemporaryIds));
            } else if (element instanceof Collection) {
                newCollection
                        .add((Serializable) replaceTemporaryEventIdentifiers(
                                (Collection<Serializable>) element,
                                permanentIdsForTemporaryIds));
            } else {
                newCollection.add(element);
            }
        }
        return newCollection;
    }

    /**
     * Get the visual features list from the specified raw specifier, if any is
     * included.
     * 
     * @param rawSpecifier
     *            Raw specifier from which to fetch the visual features list.
     * @return Visual features list, or <code>null</code> if none is found
     *         within the raw specifier.
     * @throws IllegalArgumentException
     *             If <code>rawSpecifier</code> does not hold a valid
     *             specification.
     */
    private VisualFeaturesList getVisualFeaturesFromRawSpecifier(
            Map<String, ?> rawSpecifier) {
        VisualFeaturesList visualFeaturesList = null;
        try {
            visualFeaturesList = (VisualFeaturesList) rawSpecifier
                    .get(HazardConstants.VISUAL_FEATURES_KEY);
            if ((visualFeaturesList != null) && visualFeaturesList.isEmpty()) {
                visualFeaturesList = null;
            }
            return visualFeaturesList;
        } catch (Exception e) {
            throw new IllegalArgumentException("invalid visual features list",
                    e);
        }
    }

    /**
     * Determine whether or not the specified tool execution identifier is
     * currently relevant, that is, it identifiers the specific execution of a
     * recommender that is currently in process, if any.
     * 
     * @return <code>true</code> if this identifier is relevant,
     *         <code>false</code> otherwise.
     */
    private boolean isRelevant(ToolExecutionIdentifier identifier) {
        synchronized (pendingRecommenderExecutionRequests) {
            return (identifier.getToolIdentifier()
                    .equals(runningRecommenderIdentifier)
                    && identifier.getContext().equals(runningContext));
        }
    }
}

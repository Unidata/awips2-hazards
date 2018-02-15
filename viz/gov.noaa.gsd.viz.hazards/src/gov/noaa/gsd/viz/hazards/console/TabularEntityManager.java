/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Evaluation & Decision Support Branch (EDS)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.console;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardStatus;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEventView;
import com.raytheon.uf.common.dataplugin.events.locks.LockInfo;
import com.raytheon.uf.common.dataplugin.events.locks.LockInfo.LockStatus;
import com.raytheon.uf.common.message.WsId;
import com.raytheon.uf.common.util.Pair;
import com.raytheon.uf.viz.hazards.sessionmanager.ISessionManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.ISessionConfigurationManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.ObservedSettings;
import com.raytheon.uf.viz.hazards.sessionmanager.events.ISessionEventManager;
import com.raytheon.uf.viz.hazards.sessionmanager.events.ISessionSelectionManager;
import com.raytheon.uf.viz.hazards.sessionmanager.locks.ISessionLockManager;

import gov.noaa.gsd.common.utilities.Sort;
import gov.noaa.gsd.common.utilities.Sort.SortDirection;
import gov.noaa.gsd.viz.hazards.UIOriginator;
import gov.noaa.gsd.viz.hazards.alerts.CountdownTimer;
import gov.noaa.gsd.viz.mvp.widgets.IListStateChangeHandler;

/**
 * Description: Manager for {@link TabularEntity} instances within the
 * {@link ConsolePresenter}. Note that in the descriptions of variables and
 * methods below, "hazard event version" and "event version" refer to a hazard
 * event in the form it takes at a particular point in time, that is, either its
 * current (latest) form, or historical (one of the states in which it was
 * persisted).
 * <p>
 * Note that it is assumed that the history list for any given event, if found,
 * will hold zero or more events, with each of those events having an insert
 * time. Furthermore, each one is assumed to have a fixed place in a <i>reversed
 * version</i> of the list, that is, the oldest one will always be at the end of
 * the list, the second-oldest at the next-to-last position in the list, and so
 * on. So, the last one would always have index <code>0</code> in the reversed
 * list, the second-to-last one invariably would have index <code>1</code> in
 * the reversed list, etc.
 * </p>
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Dec 16, 2016   15556    Chris.Golden Initial creation.
 * Dec 19, 2016   21504    Robert.Blum  Adapted to hazard locking.
 * Feb 16, 2017   29138    Chris.Golden Changed to remove notion of visibility
 *                                      of events in the history list, since
 *                                      all events in the history list are now
 *                                      visible.
 * Mar 16, 2017   15528    Chris.Golden Added support for indicating unsaved
 *                                      changes in a hazard event. Also changed
 *                                      over from having the checked attribute as
 *                                      part of hazard events to having checked
 *                                      status tracked by the event manager.
 * Mar 30, 2017   15528    Chris.Golden Changed to always show "ending" status
 *                                      events as modified (bold text).
 * Apr 20, 2017   33376    Chris.Golden Fixed bug in binary sort used to order
 *                                      console rows that under certain
 *                                      circumstances could cause the loop to
 *                                      run forever, freezing CAVE.
 * May 01, 2017   33528    mduff        Changed lock status text to "E" if locked
 *                                      for editing.
 * Aug 01, 2017   35979    Roger.Ferrel Lock status now displays "Edit" instead
 *                                      of "E".
 * Dec 17, 2017   20739    Chris.Golden Refactored away access to directly
 *                                      mutable session events. Also added use of
 *                                      locking workstation and user name for
 *                                      column cells when an event is locked,
 *                                      instead of the event's workstation and
 *                                      user name.
 * Jan 17, 2018   33428    Chris.Golden Changed to work with new, more flexible
 *                                      toolbar contribution code.
 * Mar 20, 2018   48027    Chris.Golden Fixed problem with historical events
 *                                      being sometimes shown as locked, and with
 *                                      workstations and users listed who have
 *                                      them locked, when they are not actually
 *                                      locked.
 * May 01, 2018   15561    Chris.Golden Fixed until-further-notice to be allowed
 *                                      only if not ending, ended, elapsing, or
 *                                      elapsed.
 * May 04, 2018   50032    Chris.Golden Fixed bug that caused replacing of tabular
 *                                      entities for an event that is not found
 *                                      to have previously had a tabular entity
 *                                      to not show the event. Now, the tabular
 *                                      entity is added if no old one is found to
 *                                      replace.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
class TabularEntityManager {

    // Private Interfaces

    /**
     * Interface that must be implemented by fetchers of arbitrary properties of
     * hazard events.
     */
    private interface IHazardEventPropertyFetcher {

        /**
         * Get the value of an implementation-specific property of the specified
         * hazard event.
         * 
         * @param event
         *            Hazard event from which to fetch the property.
         * @param property
         *            Name of the property to be fetched.
         * @param sessionManager
         *            Session manager.
         * @return Value of the property.
         */
        Object getProperty(IHazardEventView event, String property,
                ISessionManager<ObservedSettings> sessionManager);
    }

    // Private Static Constants

    /**
     * Minimum initial size of a newly created historical entities list if the
     * historical events from which the entities are to be generated is greater
     * than this number.
     */
    private static final int MINIMUM_INITIAL_HISTORICAL_ENTITIES_SIZE = 10;

    /**
     * Slop to be added to an existing historical entities list's size when new
     * historical events have been generated for an event with existing
     * historical entities already associated with its earlier historical
     * versions.
     */
    private static final int HISTORICAL_ENTITIES_SIZE_SLOP = 4;

    /**
     * Initial entities representing current versions of hazard evnts list size.
     */
    private static final int INITIAL_ENTITIES_LIST_SIZE = 100;

    /**
     * Hazard event attribute value fetcher.
     */
    private static final IHazardEventPropertyFetcher HAZARD_ATTRIBUTE_FETCHER = new IHazardEventPropertyFetcher() {

        @Override
        public Object getProperty(IHazardEventView event, String property,
                ISessionManager<ObservedSettings> sessionManager) {
            return event.getHazardAttribute(property);
        }
    };

    /**
     * Map of sort identifiers that refer to first-class properties of hazard
     * events to property value fetchers for those first-class properties.
     */
    private static final ImmutableMap<String, IHazardEventPropertyFetcher> FETCHERS_FOR_HAZARD_EVENT_PROPERTIES;

    static {
        Map<String, IHazardEventPropertyFetcher> map = new HashMap<>();
        map.put(HazardConstants.HAZARD_EVENT_IDENTIFIER,
                new IHazardEventPropertyFetcher() {
                    @Override
                    public Object getProperty(IHazardEventView event,
                            String property,
                            ISessionManager<ObservedSettings> sessionManager) {
                        return event.getEventID();
                    }
                });
        map.put(HazardConstants.HAZARD_EVENT_DISPLAY_IDENTIFIER,
                new IHazardEventPropertyFetcher() {
                    @Override
                    public Object getProperty(IHazardEventView event,
                            String property,
                            ISessionManager<ObservedSettings> sessionManager) {
                        return event.getDisplayEventID();
                    }
                });
        map.put(HazardConstants.LOCK_STATUS, new IHazardEventPropertyFetcher() {
            @Override
            public Object getProperty(IHazardEventView event, String property,
                    ISessionManager<ObservedSettings> sessionManager) {
                return getLockStatusDescription(event,
                        sessionManager.getLockManager());
            }
        });
        map.put(HazardConstants.HAZARD_EVENT_PHEN,
                new IHazardEventPropertyFetcher() {
                    @Override
                    public Object getProperty(IHazardEventView event,
                            String property,
                            ISessionManager<ObservedSettings> sessionManager) {
                        return event.getPhenomenon();
                    }
                });
        map.put(HazardConstants.HAZARD_EVENT_SIG,
                new IHazardEventPropertyFetcher() {
                    @Override
                    public Object getProperty(IHazardEventView event,
                            String property,
                            ISessionManager<ObservedSettings> sessionManager) {
                        return event.getSignificance();
                    }
                });
        map.put(HazardConstants.HAZARD_EVENT_SUB_TYPE,
                new IHazardEventPropertyFetcher() {
                    @Override
                    public Object getProperty(IHazardEventView event,
                            String property,
                            ISessionManager<ObservedSettings> sessionManager) {
                        return event.getSubType();
                    }
                });
        map.put(HazardConstants.HAZARD_EVENT_TYPE,
                new IHazardEventPropertyFetcher() {
                    @Override
                    public Object getProperty(IHazardEventView event,
                            String property,
                            ISessionManager<ObservedSettings> sessionManager) {
                        return event.getHazardType();
                    }
                });
        map.put(HazardConstants.HAZARD_EVENT_STATUS,
                new IHazardEventPropertyFetcher() {
                    @Override
                    public Object getProperty(IHazardEventView event,
                            String property,
                            ISessionManager<ObservedSettings> sessionManager) {
                        return (event.getStatus() == null ? null
                                : event.getStatus().getValue());
                    }
                });
        map.put(HazardConstants.HAZARD_EVENT_START_TIME,
                new IHazardEventPropertyFetcher() {
                    @Override
                    public Object getProperty(IHazardEventView event,
                            String property,
                            ISessionManager<ObservedSettings> sessionManager) {
                        return event.getStartTime();
                    }
                });
        map.put(HazardConstants.HAZARD_EVENT_END_TIME,
                new IHazardEventPropertyFetcher() {
                    @Override
                    public Object getProperty(IHazardEventView event,
                            String property,
                            ISessionManager<ObservedSettings> sessionManager) {
                        return event.getEndTime();
                    }
                });
        map.put(HazardConstants.CREATION_TIME,
                new IHazardEventPropertyFetcher() {
                    @Override
                    public Object getProperty(IHazardEventView event,
                            String property,
                            ISessionManager<ObservedSettings> sessionManager) {
                        return event.getCreationTime();
                    }
                });
        map.put(HazardConstants.WORKSTATION, new IHazardEventPropertyFetcher() {
            @Override
            public Object getProperty(IHazardEventView event, String property,
                    ISessionManager<ObservedSettings> sessionManager) {
                return getWorkstationInfoForEvent(event,
                        sessionManager.getLockManager()).getHostName();
            }
        });
        map.put(HazardConstants.USER_NAME, new IHazardEventPropertyFetcher() {
            @Override
            public Object getProperty(IHazardEventView event, String property,
                    ISessionManager<ObservedSettings> sessionManager) {
                return getWorkstationInfoForEvent(event,
                        sessionManager.getLockManager()).getUserName();
            }
        });
        map.put(HazardConstants.SITE_ID, new IHazardEventPropertyFetcher() {
            @Override
            public Object getProperty(IHazardEventView event, String property,
                    ISessionManager<ObservedSettings> sessionManager) {
                return event.getSiteID();
            }
        });
        FETCHERS_FOR_HAZARD_EVENT_PROPERTIES = ImmutableMap.copyOf(map);
    }

    // Private Variables

    /**
     * Session manager.
     */
    private ISessionManager<ObservedSettings> sessionManager;

    /**
     * View associated with the presenter using this manager.
     */
    private IConsoleView<?, ?, ?> view;

    /**
     * List of root tabular entities representing current hazard event versions.
     */
    private final List<TabularEntity> tabularEntities = new ArrayList<>(
            INITIAL_ENTITIES_LIST_SIZE);

    /**
     * Map associating each current hazard event version that has an associated
     * tabular entity with the index of said entity within
     * {@link #tabularEntities}.
     */
    private final Map<String, Integer> indicesForEvents = new HashMap<>();

    /**
     * Map associating hazard event versions (either the current one, if no
     * {@link Integer} is included in the key, or a persisted version if an
     * <code>Integer</code> is included giving the index of the persisted
     * version in the reversed history list for the event) with their tabular
     * entities.
     */
    private final Map<Pair<String, Integer>, TabularEntity> tabularEntitiesForIdentifiers = new HashMap<>();

    /**
     * Flag indicating whether or not to show the history lists for the various
     * hazard events.
     */
    private boolean showHistoryList;

    /**
     * List of sort algorithms to be used when ordering the events backing the
     * entities. This is an unmodifiable view of a list that is kept up to date
     * by the presenter.
     */
    private final List<Sort> sorts;

    /**
     * Map of sort identifiers to the comparators to be used for those sorts.
     * This is an unmodifiable view of a map that is kept up to date by the
     * presenter.
     */
    private final Map<String, Comparator<?>> comparatorsForSortIdentifiers;

    /**
     * Map of sort identifiers to the types of the data to be used for those
     * sorts. This is an unmodifiable view of a map that is kept up to date by
     * the presenter.
     */
    private final Map<String, Class<?>> typesForSortIdentifiers;

    /**
     * Comparator that uses the {@link #sorts} to sort the events backing the
     * entities.
     */
    private final Comparator<String> sortComparator = new Comparator<String>() {

        @Override
        public int compare(String o1, String o2) {
            return compareHazardEvents(o1, o2);
        }
    };

    /**
     * Map pairing event identifiers with the associated hazard events.
     * <p>
     * TODO: The session event manager should really be responsible for keeping
     * such a map around, as it would allow its getEventById() method to be much
     * faster. Since it does not do this right now, this information is cached
     * here.
     * </p>
     */
    private final Map<String, IHazardEventView> eventsForIdentifiers = new HashMap<>();

    /**
     * Map of event identifiers to countdown timers, for those events that have
     * such.
     */
    private Map<String, CountdownTimer> countdownTimersForEventIdentifiers = Collections
            .emptyMap();

    /**
     * Tree contents state change handler. The identifier is ignored.
     */
    private final IListStateChangeHandler<String, TabularEntity> treeContentsChangeHandler = new IListStateChangeHandler<String, TabularEntity>() {

        @Override
        public void listElementChanged(String identifier,
                TabularEntity element) {
            handleUserChangesToEntities(Sets.newHashSet(element));
        }

        @Override
        public void listElementsChanged(String identifier,
                Set<TabularEntity> elements) {
            handleUserChangesToEntities(elements);
        }
    };

    // Private Static Methods

    /**
     * Get a text description of the specified event's lock status.
     * 
     * @param event
     *            Event for which to generate the description.
     * @param lockManager
     *            Lock manager.
     * @return Text description.
     */
    private static String getLockStatusDescription(IHazardEventView event,
            ISessionLockManager lockManager) {
        LockInfo lockInfo = lockManager
                .getHazardEventLockInfo(event.getEventID());
        LockStatus status = lockInfo.getLockStatus();
        if (status == LockStatus.LOCKABLE) {
            return "U";
        } else if (status == LockStatus.LOCKED_BY_ME) {
            return "Edit";
        } else {
            WsId workstation = lockManager
                    .getWorkStationHoldingHazardLock(event.getEventID());
            return "L:" + (workstation == null ? "(unknown)"
                    : workstation.getUserName());
        }
    }

    /**
     * Get the workstation identifier information for the specified event for
     * display purposes.
     * 
     * @param event
     *            Event for which to retrieve the workstation information.
     * @param lockManager
     *            Lock manager.
     * @return Workstation information.
     */
    private static WsId getWorkstationInfoForEvent(IHazardEventView event,
            ISessionLockManager lockManager) {
        WsId workstationInfo = lockManager
                .getWorkStationHoldingHazardLock(event.getEventID());
        if (workstationInfo != null) {
            return workstationInfo;
        }
        return event.getWsId();
    }

    // Package-Private Constructors

    /**
     * Construct a standard instance.
     * 
     * @param sessionManager
     *            Session manager to be used.
     * @param sorts
     *            List of sort algorithms to be used when ordering the events
     *            backing the entities. This is an unmodifiable view of a list
     *            that is kept up to date by the presenter.
     * @param comparatorsForSortIdentifiers
     *            Map of sort identifiers to the comparators to be used for
     *            those sorts. This is an unmodifiable view of a map that is
     *            kept up to date by the presenter.
     * @param typesForSortIdentifiers
     *            Map of sort identifiers to the types of the data to be used
     *            for those sorts. This is an unmodifiable view of a map that is
     *            kept up to date by the presenter.
     */
    TabularEntityManager(ISessionManager<ObservedSettings> sessionManager,
            List<Sort> sorts,
            Map<String, Comparator<?>> comparatorsForSortIdentifiers,
            Map<String, Class<?>> typesForSortIdentifiers) {
        this.sessionManager = sessionManager;
        this.sorts = sorts;
        this.comparatorsForSortIdentifiers = comparatorsForSortIdentifiers;
        this.typesForSortIdentifiers = typesForSortIdentifiers;
    }

    // Package-Private Methods

    /**
     * Set the view to that specified.
     * 
     * @param view
     *            View to be used.
     */
    void setView(IConsoleView<?, ?, ?> view) {
        this.view = view;
        view.getTreeContentsChanger().set(null, tabularEntities);
    }

    /**
     * Get the tree contents change handler.
     * 
     * @return Tree contents change handler.
     */
    IListStateChangeHandler<String, TabularEntity> getTreeContentsChangeHandler() {
        return treeContentsChangeHandler;
    }

    /**
     * Dispose of the manager.
     */
    void dispose() {
        sessionManager = null;
    }

    /**
     * Add entities for the specified hazard event.
     * 
     * @param event
     *            Hazard event for which to add entities.
     */
    void addEntitiesForEvent(IHazardEventView event) {

        /*
         * If the event to have entities added for it is not currently something
         * that should be displayed, do nothing.
         */
        List<IHazardEventView> events = sessionManager.getEventManager()
                .getEventsForCurrentSettings();
        if (events.indexOf(event) == -1) {
            return;
        }

        /*
         * Add the event to the map of identifiers to events.
         */
        eventsForIdentifiers.put(event.getEventID(), event);

        /*
         * If an entity already exists representing the current version of this
         * event, do nothing.
         */
        String eventIdentifier = event.getEventID();
        Pair<String, Integer> entityIdentifier = new Pair<String, Integer>(
                eventIdentifier, null);
        if (tabularEntitiesForIdentifiers.containsKey(entityIdentifier)) {
            return;
        }

        /*
         * Create the entity and any child entities needed to represent
         * historical versions if appropriate.
         */
        TabularEntity entity = createEntitiesForEvent(event, entityIdentifier,
                tabularEntitiesForIdentifiers);

        /*
         * Determine where the entity should be inserted.
         */
        int insertionIndex = getInsertionIndexForEntity(entity);

        /*
         * Insert the entity at the appropriate index, and update the map of
         * indices for events.
         */
        tabularEntities.add(insertionIndex, entity);
        for (Map.Entry<String, Integer> entry : indicesForEvents.entrySet()) {
            if (entry.getValue() >= insertionIndex) {
                entry.setValue(entry.getValue() + 1);
            }
        }
        indicesForEvents.put(eventIdentifier, insertionIndex);

        /*
         * Tell the list state changer of the insertion.
         */
        view.getTreeContentsChanger().insertElement(null, insertionIndex,
                entity);
    }

    /**
     * Update the list of child entities representing the versions of the latest
     * child entity for the specified hazard event.
     * <p>
     * <strong>Note</strong>: This method assumes that the only update required
     * is to ensure that any new historical events that did not exist before are
     * to have corresponding entities created for them, and said entities are to
     * be prepended to the historical entities list for the current version's
     * entity. It is furthermore assumed that any such new historical versions
     * of the event are found at the end of the history list, not inserted here
     * or there within it.
     * </p>
     * 
     * @param event
     *            Hazard event for which to add the latest child entity.
     */
    void updateChildEntityListForEvent(IHazardEventView event) {

        /*
         * Do nothing if the history list is not being shown.
         */
        if (showHistoryList == false) {
            return;
        }

        /*
         * Get the history list for the event, and if one is found, ensure that
         * all the visible entries in said list have corresponding tabular
         * entities.
         */
        String eventIdentifier = event.getEventID();
        List<IHazardEventView> historicalEvents = sessionManager
                .getEventManager().getEventHistoryById(eventIdentifier);
        if (historicalEvents != null) {

            /*
             * Find the entity already created for the current version of this
             * event.
             */
            Pair<String, Integer> entityIdentifier = new Pair<>(eventIdentifier,
                    null);
            TabularEntity entity = tabularEntitiesForIdentifiers
                    .get(entityIdentifier);
            if (entity == null) {
                return;
            }

            /*
             * Get the list of previously-created historical entities from the
             * current version's entity.
             */
            List<TabularEntity> oldHistoricalEntities = entity.getChildren();

            /*
             * Create a new list to hold any existing historical entities as
             * well as the new ones.
             */
            List<TabularEntity> historicalEntities = new ArrayList<>(
                    oldHistoricalEntities.size()
                            + HISTORICAL_ENTITIES_SIZE_SLOP);

            /*
             * Iterate through the historical versions of the events, assuming
             * that each one found is already represented by the next entity in
             * the old historical entities list (with the latter being run
             * through in reverse order) until the point is reached when the
             * supply of old historical entities is exhausted, at which point a
             * new historical entity is created for each of the remaining
             * visible historical events. Then reverse the newly created list,
             * since the historical entities should always be newest first.
             */
            int count = 0;
            int oldEntityIndex = oldHistoricalEntities.size() - 1;
            for (IHazardEventView historicalEvent : historicalEvents) {
                TabularEntity historicalEntity = null;
                if (oldEntityIndex == -1) {
                    historicalEntity = buildTabularEntityForEvent(
                            historicalEvent, count, null, null);
                    tabularEntitiesForIdentifiers.put(
                            new Pair<>(eventIdentifier, count),
                            historicalEntity);
                } else {
                    historicalEntity = oldHistoricalEntities
                            .get(oldEntityIndex--);
                }
                historicalEntities.add(historicalEntity);
                count++;
            }
            Collections.reverse(historicalEntities);

            /*
             * If there are new entities, create a new entity for the current
             * version of the event with the new historical entities list as its
             * children, and replace the old one.
             */
            if (historicalEntities.size() != oldHistoricalEntities.size()) {

                /*
                 * Create the new parent entity and remember it.
                 */
                entity = TabularEntity.build(entity, historicalEntities);
                tabularEntitiesForIdentifiers.put(entityIdentifier, entity);

                /*
                 * Determine where the old version of the current entity is in
                 * the main list and replace it.
                 */
                int replacementIndex = indicesForEvents.get(eventIdentifier);
                tabularEntities.set(replacementIndex, entity);

                /*
                 * Tell the list state changer of the replacement.
                 */
                view.getTreeContentsChanger().replaceElement(null,
                        replacementIndex, entity);
            }
        }
    }

    /**
     * Replace the root entity for the specified hazard event with an updated
     * one.
     * 
     * @param event
     *            Hazard event for which to replace the root entity.
     */
    void replaceRootEntityForEvent(IHazardEventView event) {

        /*
         * Get the old entity for this event, if one is found. If not, treat
         * this as an addition.
         */
        String eventIdentifier = event.getEventID();
        Pair<String, Integer> entityIdentifier = new Pair<>(eventIdentifier,
                null);
        TabularEntity oldEntity = tabularEntitiesForIdentifiers
                .get(entityIdentifier);
        if (oldEntity == null) {
            addEntitiesForEvent(event);
            return;
        }

        /*
         * If the event to have its root entity replaced is not currently
         * something that should be displayed, remove its entities instead.
         */
        List<IHazardEventView> events = sessionManager.getEventManager()
                .getEventsForCurrentSettings();
        if (events.indexOf(event) == -1) {
            removeEntitiesForEvent(event);
            return;
        }

        /*
         * Create the tabular entity for the current version of the event.
         */
        TabularEntity entity = buildTabularEntityForEvent(event, null,
                oldEntity, oldEntity.getChildren());
        if (entity == oldEntity) {
            return;
        }
        tabularEntitiesForIdentifiers.put(entityIdentifier, entity);

        /*
         * Determine where the old version of the current entity is in the main
         * list and replace it.
         */
        int replacementIndex = indicesForEvents.get(eventIdentifier);
        tabularEntities.set(replacementIndex, entity);

        /*
         * Tell the list state changer of the replacement.
         */
        view.getTreeContentsChanger().replaceElement(null, replacementIndex,
                entity);
    }

    /**
     * Replace the root entity for the specified hazard event, and all child
     * entities representing the historical versions of the event with the
     * specified indices, with updated ones.
     * 
     * @param event
     *            Hazard event for which to replace the entities.
     * @param historicalIndices
     *            Indices of historical versions for which the entities should
     *            also be replaced; may be empty.
     */
    void replaceEntitiesForEvent(IHazardEventView event,
            Set<Integer> historicalIndices) {

        /*
         * Just replace the root entity for the event if no historical indices
         * are specified; otherwise, go through and replace the historical
         * entities as appropriate, then the root entity.
         */
        if (historicalIndices.isEmpty() || (showHistoryList == false)) {
            replaceRootEntityForEvent(event);
        } else {

            /*
             * Get the old version of the root entity.
             */
            String eventIdentifier = event.getEventID();
            Pair<String, Integer> entityIdentifier = new Pair<>(eventIdentifier,
                    null);
            TabularEntity oldEntity = tabularEntitiesForIdentifiers
                    .get(entityIdentifier);
            if (oldEntity == null) {
                return;
            }

            /*
             * If the event to have entities replaced is not currently something
             * that should be displayed, remove its entities instead.
             */
            List<IHazardEventView> events = sessionManager.getEventManager()
                    .getEventsForCurrentSettings();
            if (events.indexOf(event) == -1) {
                removeEntitiesForEvent(event);
                return;
            }

            /*
             * Iterate through the historical versions of the event, creating a
             * new entity for it if it has an index specified as one of the
             * indices needing replacement, or reusing a previously created
             * historical entity if not, and placing the entity in the list.
             * Reverse the list ordering at the end since historical entities
             * should have the newest ones first.
             */
            List<IHazardEventView> historicalEvents = sessionManager
                    .getEventManager().getEventHistoryById(eventIdentifier);
            List<TabularEntity> historicalEntities = null;
            if ((historicalEvents != null)
                    && (historicalEvents.isEmpty() == false)) {
                historicalEntities = new ArrayList<>(
                        oldEntity.getChildren().size());
                int count = 0;
                for (IHazardEventView historicalEvent : historicalEvents) {
                    Pair<String, Integer> historicalIdentifier = new Pair<>(
                            eventIdentifier, count);
                    TabularEntity oldHistoricalEntity = tabularEntitiesForIdentifiers
                            .get(historicalIdentifier);
                    TabularEntity entity = (historicalIndices.contains(count)
                            ? buildTabularEntityForEvent(historicalEvent, count,
                                    oldHistoricalEntity, null)
                            : oldHistoricalEntity);
                    historicalEntities.add(entity);
                    tabularEntitiesForIdentifiers.put(historicalIdentifier,
                            entity);
                    count++;
                }
                Collections.reverse(historicalEntities);
            }

            /*
             * Create the new parent entity and remember it.
             */
            TabularEntity entity = this.buildTabularEntityForEvent(event, null,
                    oldEntity, historicalEntities);
            if (entity == oldEntity) {
                return;
            }
            tabularEntitiesForIdentifiers.put(entityIdentifier, entity);

            /*
             * Determine where the old version of the current entity is in the
             * main list and replace it.
             */
            int replacementIndex = indicesForEvents.get(eventIdentifier);
            tabularEntities.set(replacementIndex, entity);

            /*
             * Tell the list state changer of the replacement.
             */
            view.getTreeContentsChanger().replaceElement(null, replacementIndex,
                    entity);
        }
    }

    /**
     * Remove any entities representing the specified hazard event.
     * 
     * @param event
     *            Hazard event for which to remove representative entities.
     */
    void removeEntitiesForEvent(IHazardEventView event) {
        removeEntitiesForEvent(event.getEventID());
    }

    /**
     * Remove any entities representing the specified hazard event.
     * 
     * @param eventIdentifier
     *            Identifier of the hazard event for which to remove
     *            representative entities.
     */
    void removeEntitiesForEvent(String eventIdentifier) {

        /*
         * Find the tabular entity representing the current version of this
         * event and remove it from the map of entity identifiers to entities.
         */
        TabularEntity entity = tabularEntitiesForIdentifiers
                .remove(new Pair<String, Integer>(eventIdentifier, null));
        if (entity != null) {

            /*
             * Iterate through any historical entities that are children of the
             * found entity, removing each of them in turn from the same map.
             */
            List<TabularEntity> historicalEntities = entity.getChildren();
            for (TabularEntity historicalEntity : historicalEntities) {
                tabularEntitiesForIdentifiers.remove(new Pair<>(eventIdentifier,
                        historicalEntity.getHistoryIndex()));
            }

            /*
             * Remove the current version entity from the main list.
             */
            int removalIndex = indicesForEvents.get(eventIdentifier);
            tabularEntities.remove(removalIndex);
            indicesForEvents.remove(eventIdentifier);

            /*
             * Adjust the recorded indices of any following events.
             */
            for (Map.Entry<String, Integer> entry : indicesForEvents
                    .entrySet()) {
                if (entry.getValue() > removalIndex) {
                    entry.setValue(entry.getValue() - 1);
                }
            }

            /*
             * Tell the list state changer of the removal.
             */
            view.getTreeContentsChanger().removeElement(null, removalIndex);
        }
    }

    /**
     * Clear and recreate all entities.
     */
    void recreateAllEntities() {

        /*
         * Remember the old entities so that they may be reused where possible,
         * and clear records of all the old entities, including their ordering
         * and indices.
         */
        Map<Pair<String, Integer>, TabularEntity> oldTabularEntitiesForIdentifiers = new HashMap<>(
                tabularEntitiesForIdentifiers);
        tabularEntitiesForIdentifiers.clear();
        tabularEntities.clear();
        indicesForEvents.clear();

        /*
         * Get the sorted identifiers of the events to be displayed.
         */
        List<String> sortedEventIdentifiers = getSortedEventIdentifiers();

        /*
         * Iterate through the event identifiers in sorted order, creating
         * entities for each in turn, reusing entities where possible.
         */
        int index = 0;
        for (String eventIdentifier : sortedEventIdentifiers) {

            /*
             * Create the entity and any child entities needed to represent
             * historical versions if appropriate.
             */
            tabularEntities.add(createEntitiesForEvent(
                    eventsForIdentifiers.get(eventIdentifier),
                    new Pair<String, Integer>(eventIdentifier, null),
                    oldTabularEntitiesForIdentifiers));
            indicesForEvents.put(eventIdentifier, index++);
        }

        /*
         * Notify the view's tree contents changer of the repopulation.
         */
        view.getTreeContentsChanger().set(null, tabularEntities);
    }

    /**
     * Set the flag indicating whether or not to show the history list for the
     * various events.
     * 
     * @param showHistoryList
     *            Flag indicating whether or not to show the history list for
     *            the various events.
     */
    void setShowHistoryList(boolean showHistoryList) {

        /*
         * Do nothing if the show history list flag has not changed.
         */
        if (this.showHistoryList == showHistoryList) {
            return;
        }

        /*
         * Remember the new state of the flag.
         */
        this.showHistoryList = showHistoryList;

        /*
         * Iterate through the existing tabular entities, adding historical
         * entities as children if the history lists are now being shown, or
         * removing any such entities if they are now being hidden.
         */
        List<TabularEntity> oldTabularEntities = new ArrayList<>(
                tabularEntities);
        Map<Pair<String, Integer>, TabularEntity> oldTabularEntitiesForIdentifiers = new HashMap<>(
                tabularEntitiesForIdentifiers);
        tabularEntities.clear();
        tabularEntitiesForIdentifiers.clear();
        ISessionEventManager eventManager = sessionManager.getEventManager();
        for (TabularEntity entity : oldTabularEntities) {

            /*
             * If the history list should now be visible, recreate the entity,
             * which will cause any historical versions' entities to be created
             * as well as its children; otherwise, if there are currently one or
             * more child entities, recreate the entity without any children.
             */
            Pair<String, Integer> entityIdentifier = new Pair<String, Integer>(
                    entity.getIdentifier(), null);
            if (showHistoryList) {
                entity = createEntitiesForEvent(
                        eventManager.getEventById(entity.getIdentifier()),
                        entityIdentifier, oldTabularEntitiesForIdentifiers);
            } else {
                if (entity.getChildren().isEmpty() == false) {
                    entity = TabularEntity.build(entity, null);
                    tabularEntitiesForIdentifiers.put(entityIdentifier, entity);
                }
            }

            /*
             * Add the created (or reused) entity to the list.
             */
            tabularEntities.add(entity);
        }

        /*
         * Notify the view's tree contents changer of the repopulation.
         */
        view.getTreeContentsChanger().set(null, tabularEntities);
    }

    /**
     * Sort the hazard events using the current sort algorithms.
     */
    void sortHazardEvents() {

        /*
         * Determine whether there is at least one sort column, and do the sort
         * if there is.
         */
        if (sorts.isEmpty() == false) {

            /*
             * Get the sorted identifiers of the events to be displayed.
             */
            List<String> sortedEventIdentifiers = getSortedEventIdentifiers();

            /*
             * Compile a map of event identifiers to their tabular entities in
             * preparation for rebuilding the tabular entities list.
             */
            Map<String, TabularEntity> tabularEntitiesForIdentifiers = new HashMap<>();
            for (TabularEntity entity : tabularEntities) {
                tabularEntitiesForIdentifiers.put(entity.getIdentifier(),
                        entity);
            }

            /*
             * Clear the tabular entities list and the map of event identifiers
             * to indices within said list, then rebuild both in the newly
             * sorted order.
             */
            tabularEntities.clear();
            indicesForEvents.clear();
            for (int index = 0; index < sortedEventIdentifiers
                    .size(); index++) {
                String identifier = sortedEventIdentifiers.get(index);
                tabularEntities
                        .add(tabularEntitiesForIdentifiers.get(identifier));
                indicesForEvents.put(identifier, index);
            }

            /*
             * Notify the view's tree contents changer of the sort results.
             */
            view.getTreeContentsChanger().set(null, tabularEntities);
        }
    }

    /**
     * Set the active countdown timers.
     * 
     * @param countdownTimersForEventIdentifiers
     *            Map of event identifiers to their associated countdown timers,
     *            for events that have such.
     */
    void setActiveCountdownTimers(
            ImmutableMap<String, CountdownTimer> countdownTimersForEventIdentifiers) {

        this.countdownTimersForEventIdentifiers = countdownTimersForEventIdentifiers;

        /*
         * Iterate through the current sort algorithms; if one of them sorts by
         * countdown timer, re-sort the events.
         */
        for (Sort sort : sorts) {
            if (comparatorsForSortIdentifiers
                    .get(sort.getAttributeIdentifier()) == null) {
                sortHazardEvents();
                break;
            }
        }
    }

    // Private Methods

    /**
     * Handle user changes to the specified tabular entities.
     * 
     * @param entities
     *            Entities that have been changed by the user.
     */
    private void handleUserChangesToEntities(Set<TabularEntity> entities) {

        /*
         * Iterate through the changed entities, handling each one in turn.
         */
        ISessionEventManager eventManager = sessionManager.getEventManager();
        Set<Pair<String, Integer>> unselectedEntityIdentifiers = new HashSet<>(
                entities.size() * 2, 1.0f);
        Set<Pair<String, Integer>> selectedEntityIdentifiers = new HashSet<>(
                entities.size() * 2, 1.0f);
        Set<IHazardEventView> eventsRejectingChanges = new HashSet<>();
        for (TabularEntity entity : entities) {

            /*
             * Ensure that the entity is a root entity, not a child entity.
             */
            if (entity.getHistoryIndex() != null) {
                throw new IllegalArgumentException(
                        "cannot handle user change to historical "
                                + "entity directly; must be handled indirectly through change to parent");
            }

            /*
             * Get the old version of this entity, doing nothing with the new
             * entity if there is no old version found.
             */
            Pair<String, Integer> entityIdentifier = new Pair<>(
                    entity.getIdentifier(), null);
            TabularEntity oldEntity = tabularEntitiesForIdentifiers
                    .get(entityIdentifier);
            if (oldEntity != null) {

                /*
                 * Associate the new version with its identifier.
                 */
                tabularEntitiesForIdentifiers.put(entityIdentifier, entity);

                /*
                 * Replace the old entity with the new one in the main list of
                 * entities.
                 */
                tabularEntities.set(
                        indicesForEvents.get(entity.getIdentifier()), entity);

                /*
                 * If the entity has changed its until further notice state,
                 * change the appropriate attribute of the corresponding event.
                 * If the change is rejected, make a note of this so that the
                 * view can be notified of the rejection.
                 */
                IHazardEventView event = eventManager
                        .getEventById(entity.getIdentifier());
                if (oldEntity.isEndTimeUntilFurtherNotice() != entity
                        .isEndTimeUntilFurtherNotice()) {
                    Map<String, Serializable> changedAttributes = new HashMap<>();
                    changedAttributes.put(
                            HazardConstants.HAZARD_EVENT_END_TIME_UNTIL_FURTHER_NOTICE,
                            entity.isEndTimeUntilFurtherNotice());
                    if (eventManager.changeEventProperty(event,
                            ISessionEventManager.ADD_EVENT_ATTRIBUTES,
                            changedAttributes,
                            UIOriginator.CONSOLE) != ISessionEventManager.EventPropertyChangeResult.SUCCESS) {
                        eventsRejectingChanges.add(event);
                    }
                }

                /*
                 * If the entity's checked state has changed, tell the session
                 * event manager about the change.
                 */
                if (oldEntity.isChecked() != entity.isChecked()) {
                    eventManager.setEventChecked(event, entity.isChecked(),
                            UIOriginator.CONSOLE);
                }

                /*
                 * If the entity's time range has changed, change the start and
                 * end times to match in the corresponding event. If the change
                 * is rejected, make a note of this so that the view can be
                 * notified of the rejection.
                 */
                if (oldEntity.getTimeRange()
                        .equals(entity.getTimeRange()) == false) {
                    if (eventManager.changeEventProperty(event,
                            ISessionEventManager.SET_EVENT_TIME_RANGE,
                            new Pair<>(
                                    new Date(entity.getTimeRange()
                                            .lowerEndpoint()),
                                    new Date(entity.getTimeRange()
                                            .upperEndpoint())),
                            UIOriginator.CONSOLE) != ISessionEventManager.EventPropertyChangeResult.SUCCESS) {
                        eventsRejectingChanges.add(event);
                    }
                }

                /*
                 * Add the entity's identifier to the newly selected or
                 * unselected set if its selection state has changed.
                 */
                recordEntitySelectionChange(entity, oldEntity,
                        selectedEntityIdentifiers, unselectedEntityIdentifiers,
                        entityIdentifier);

                /*
                 * Iterate through any child entities of the new entity,
                 * comparing them to the corresponding child entities of the old
                 * entity, to see if their selection states have changed, and
                 * record any such changes.
                 */
                List<TabularEntity> children = entity.getChildren();
                List<TabularEntity> oldChildren = oldEntity.getChildren();
                int numChildren = children.size();
                int oldNumChildren = oldChildren.size();
                for (int index = 0; index < numChildren; index++) {
                    recordEntitySelectionChange(
                            children.get(numChildren - (index + 1)),
                            oldChildren.get(oldNumChildren - (index + 1)),
                            selectedEntityIdentifiers,
                            unselectedEntityIdentifiers, null);
                }
            }
        }

        /*
         * Create new entities for any events that rejected a time range change,
         * and forward them to the view.
         */
        for (IHazardEventView event : eventsRejectingChanges) {
            replaceRootEntityForEvent(event);
        }

        /*
         * If entities were deselected and/or selected, notify the selection
         * manager of the change(s).
         */
        if ((unselectedEntityIdentifiers.isEmpty() == false)
                && (selectedEntityIdentifiers.isEmpty() == false)) {
            sessionManager.getSelectionManager()
                    .setSelectedEventVersionIdentifiers(
                            Sets.union(
                                    Sets.difference(
                                            sessionManager.getSelectionManager()
                                                    .getSelectedEventVersionIdentifiers(),
                                            unselectedEntityIdentifiers),
                                    selectedEntityIdentifiers),
                            UIOriginator.CONSOLE);
        } else if (unselectedEntityIdentifiers.isEmpty() == false) {
            sessionManager.getSelectionManager()
                    .removeEventVersionsFromSelectedEvents(
                            unselectedEntityIdentifiers, UIOriginator.CONSOLE);
        } else {
            sessionManager.getSelectionManager()
                    .addEventVersionsToSelectedEvents(selectedEntityIdentifiers,
                            UIOriginator.CONSOLE);
        }
    }

    /**
     * Record any change in selection of the specified entity as compared to its
     * selection state previously by adding it to either the specified selected
     * set or the specified unselected set, if there has been a selection state
     * change for the entity.
     * 
     * @param entity
     *            New version of the entity that may have changed as compared to
     *            the old one.
     * @param oldEntity
     *            Old version of the entity, to which to compare the new version
     *            to look for selection changes.
     * @param selectedEntityIdentifiers
     *            Set to which to add the identifier of the entity if it has
     *            become selected.
     * @param unselectedEntityIdentifiers
     *            Set to which to add the identifier of the entity if it has
     *            become unselected.
     * @param entityIdentifier
     *            Convenience object in case the caller has the entity
     *            identifier already created. May be <code>null</code>, in which
     *            case the appropriate identifier will be created if it must be
     *            added to either the selected or unselected sets.
     */
    private void recordEntitySelectionChange(TabularEntity entity,
            TabularEntity oldEntity,
            Set<Pair<String, Integer>> selectedEntityIdentifiers,
            Set<Pair<String, Integer>> unselectedEntityIdentifiers,
            Pair<String, Integer> entityIdentifier) {

        /*
         * If the entity has changed selection state, add its identifier to the
         * set of unselected entities if it is now unselected, or to the set of
         * selected entities if now selected.
         */
        if (oldEntity.isSelected() != entity.isSelected()) {
            if (entityIdentifier == null) {
                entityIdentifier = new Pair<String, Integer>(
                        entity.getIdentifier(), entity.getHistoryIndex());
            }
            if (entity.isSelected()) {
                selectedEntityIdentifiers.add(entityIdentifier);
            } else {
                unselectedEntityIdentifiers.add(entityIdentifier);
            }
        }

    }

    /**
     * Create the entities for the specified event.
     * 
     * @param event
     *            Event for which to create the entities.
     * @param entityIdentifier
     *            Identifier of the root entity to be created.
     * @param tabularEntitiesForIdentifiers
     *            Map of entity identifiers to tabular entities; this will be
     *            used to fetch existing versions of entities to be created to
     *            see if they may be reused.
     * @return Entity representing the current version of the event, with child
     *         entities representing visible historical versions of the event if
     *         appropriate. Note that any entities that are created or reused
     *         will have had entries added in the map referenced by the instance
     *         variable {@link #tabularEntitiesForIdentifiers}.
     */
    private TabularEntity createEntitiesForEvent(IHazardEventView event,
            Pair<String, Integer> entityIdentifier,
            Map<Pair<String, Integer>, TabularEntity> tabularEntitiesForIdentifiers) {

        /*
         * Create the list of tabular entities representing historical versions
         * if there are any and if the history list is to be showing. The list
         * is reversed after creation, since the last element in the list must
         * be the oldest in the history list.
         */
        String eventIdentifier = event.getEventID();
        List<TabularEntity> historicalEntities = null;
        if (showHistoryList && (sessionManager.getEventManager()
                .getHistoricalVersionCountForEvent(eventIdentifier) > 0)) {
            List<IHazardEventView> historicalEvents = sessionManager
                    .getEventManager().getEventHistoryById(eventIdentifier);
            if ((historicalEvents != null)
                    && (historicalEvents.isEmpty() == false)) {
                historicalEntities = new ArrayList<>(
                        getInitialHistoricalEntitiesSize(
                                historicalEvents.size()));
                int count = 0;
                for (IHazardEventView historicalEvent : historicalEvents) {
                    Pair<String, Integer> historicalIdentifier = new Pair<>(
                            eventIdentifier, count);
                    TabularEntity entity = buildTabularEntityForEvent(
                            historicalEvent, count,
                            tabularEntitiesForIdentifiers
                                    .get(historicalIdentifier),
                            null);
                    historicalEntities.add(entity);
                    this.tabularEntitiesForIdentifiers.put(historicalIdentifier,
                            entity);
                    count++;
                }
                Collections.reverse(historicalEntities);
            }
        }

        /*
         * Create the tabular entity for the current version of the hazard
         * event.
         */
        TabularEntity entity = buildTabularEntityForEvent(event, null,
                tabularEntitiesForIdentifiers.get(entityIdentifier),
                historicalEntities);
        this.tabularEntitiesForIdentifiers.put(entityIdentifier, entity);
        return entity;
    }

    /**
     * Build a tabular entity representing the specified hazard event.
     * 
     * @param event
     *            Hazard event version to be represented by the new entity.
     * @param historyIndex
     *            Index of this new entity in the reversed history list of the
     *            event if it has been persisted, otherwise <code>null</code>.
     * @param previousVersion
     *            Previous version of the tabular entity used to represent this
     *            hazard event version, if any exists.
     * @param children
     *            Child tabular entities, if any, for the new entity.
     * @return New tabular entity.
     */
    private TabularEntity buildTabularEntityForEvent(IHazardEventView event,
            Integer historyIndex, TabularEntity previousVersion,
            List<TabularEntity> children) {

        /*
         * Put together the parameters required for the new tabular entity
         * representing the event. Note that the time range boundaries are set
         * to be single points (as opposed to a range of possible values) if
         * this is a persisted event, since in that case the time range cannot
         * be changed by the user.
         */
        String eventIdentifier = event.getEventID();
        ISessionConfigurationManager<?> configManager = sessionManager
                .getConfigurationManager();
        ISessionEventManager eventManager = sessionManager.getEventManager();
        ISessionSelectionManager selectionManager = sessionManager
                .getSelectionManager();
        Range<Long> timeRange = Range.closed(event.getStartTime().getTime(),
                event.getEndTime().getTime());

        /*
         * TODO: For Redmine issue #26716, we need to allow time sliders to be
         * disabled on a per-hazard-type basis. Once we get there, replace the
         * "false" below with:
         * 
         * ((historyIndex == null) && (event type allows time sliders to be
         * moved) && (sessionManager.getLockManager().getHazardEventLockInfo(
         * eventIdentifier)).getLockStatus() != LockStatus.LOCKED_BY_OTHER)
         */
        boolean timeRangeEditable = false;

        Range<Long> lowerTimeBoundaries = (timeRangeEditable
                ? eventManager.getStartTimeBoundariesForEventIds()
                        .get(eventIdentifier)
                : Range.singleton(timeRange.lowerEndpoint()));
        Range<Long> upperTimeBoundaries = (timeRangeEditable
                ? eventManager.getEndTimeBoundariesForEventIds()
                        .get(eventIdentifier)
                : Range.singleton(timeRange.upperEndpoint()));

        /*
         * Create the attributes map for the tabular entity. This consists of
         * all the hazard attribute entries, plus entries for some first-class
         * fields of the hazard event. The attributes will be used to populate
         * cells in the row representing the hazard event in the console's tree
         * table.
         */
        Map<String, Serializable> attributes = new HashMap<>(
                event.getHazardAttributes());
        attributes.put(HazardConstants.HAZARD_EVENT_DISPLAY_IDENTIFIER,
                event.getDisplayEventID());
        attributes
                .put(HazardConstants.LOCK_STATUS,
                        (historyIndex == null
                                ? TabularEntityManager.getLockStatusDescription(
                                        event, sessionManager.getLockManager())
                                : null));
        attributes.put(HazardConstants.HAZARD_EVENT_PHEN,
                event.getPhenomenon());
        attributes.put(HazardConstants.HAZARD_EVENT_SIG,
                event.getSignificance());
        attributes.put(HazardConstants.HAZARD_EVENT_SUB_TYPE,
                event.getSubType());
        attributes.put(HazardConstants.HAZARD_EVENT_TYPE,
                event.getHazardType());
        attributes.put(HazardConstants.HEADLINE,
                configManager.getHeadline(event));
        attributes.put(HazardConstants.HAZARD_EVENT_STATUS,
                event.getStatus().getValue());
        attributes.put(HazardConstants.HAZARD_EVENT_START_TIME,
                timeRange.lowerEndpoint());
        attributes.put(HazardConstants.HAZARD_EVENT_END_TIME,
                timeRange.upperEndpoint());
        attributes.put(HazardConstants.CREATION_TIME,
                event.getCreationTime().getTime());
        attributes.put(HazardConstants.WORKSTATION,
                (historyIndex == null
                        ? getWorkstationInfoForEvent(event,
                                sessionManager.getLockManager()).getHostName()
                        : null));
        attributes.put(HazardConstants.USER_NAME,
                (historyIndex == null
                        ? getWorkstationInfoForEvent(event,
                                sessionManager.getLockManager()).getUserName()
                        : null));
        attributes.put(HazardConstants.SITE_ID, event.getSiteID());

        /*
         * Create the tabular entity.
         */
        boolean unsaved = false;
        if ((historyIndex == null)
                && (((event.getStatus() == HazardStatus.ISSUED)
                        && eventManager.isEventModified(event))
                        || (event.getStatus() == HazardStatus.ENDING))) {
            unsaved = true;
        }
        return TabularEntity.build(previousVersion, eventIdentifier,
                historyIndex, event.getInsertTime(), unsaved, timeRange,
                Boolean.TRUE.equals(event.getHazardAttribute(
                        HazardConstants.HAZARD_EVENT_END_TIME_UNTIL_FURTHER_NOTICE)),
                (configManager.getDurationChoices(event).size() > 0),
                lowerTimeBoundaries, upperTimeBoundaries,
                eventManager.getTimeResolutionsForEventIds()
                        .get(eventIdentifier),
                ((historyIndex == null)
                        && eventManager.getEventIdsAllowingUntilFurtherNotice()
                                .contains(eventIdentifier)
                        && (HazardStatus.endingEndedOrElapsed(
                                event.getStatus()) == false)),
                selectionManager.getSelectedEventVersionIdentifiers()
                        .contains(new Pair<String, Integer>(event.getEventID(),
                                historyIndex)),
                ((historyIndex == null) && eventManager.isEventChecked(event)),
                attributes, configManager.getColor(event), children);
    }

    /**
     * Given the specified historical events count, get a good initial size for
     * a list that is to hold historical entities corresponding to said events.
     * Note that the initial size is not necessarily the same as the event
     * count, since some historical events are generally invisible if there are
     * a large number of them within the list.
     * 
     * @param historicalEventsCount
     *            Number of historical events in the list from which any visible
     *            ones are to be translated into entities.
     * @return Good guess at the initial size of the list to hold the historical
     *         entities to be created.
     */
    private int getInitialHistoricalEntitiesSize(int historicalEventsCount) {
        if (historicalEventsCount < MINIMUM_INITIAL_HISTORICAL_ENTITIES_SIZE) {
            return historicalEventsCount;
        } else if (historicalEventsCount
                / 2 < MINIMUM_INITIAL_HISTORICAL_ENTITIES_SIZE) {
            return MINIMUM_INITIAL_HISTORICAL_ENTITIES_SIZE;
        } else {
            return historicalEventsCount / 2;
        }
    }

    /**
     * Get the index within {@link #tabularEntities} at which the specified
     * tabular entity should be inserted, given the current sorting algorithms.
     * 
     * @param entity
     *            Tabular entity to be inserted. It is assumed this is not an
     *            entity representing a historical version of an event.
     * @return Index within {@link #tabularEntities} at which the tabular entity
     *         should be inserted, within the range
     *         <code>[0, <i>listSize</i>]</code>; it will be the upper boundary
     *         if there is currently no sorting being done.
     */
    private int getInsertionIndexForEntity(TabularEntity entity) {

        /*
         * If there is no sorting, the entity should simply be appended.
         */
        if (sorts.isEmpty()) {
            return tabularEntities.size();
        }

        /*
         * Perform a binary search to find the insertion index.
         */
        int max = tabularEntities.size();
        int lower = 0;
        int upper = max;
        String identifier = entity.getIdentifier();
        while (lower != upper) {
            int index = (lower + upper) / 2;
            int result = compareHazardEvents(identifier,
                    tabularEntities.get(index).getIdentifier());
            if (result == 0) {
                lower = upper = index;
            } else if (result < 0) {
                upper = index;
            } else {
                if (lower == index) {
                    lower++;
                } else {
                    lower = index;
                }
            }
        }

        /*
         * If the insertion index points to an entity that is equivalent to this
         * one in terms of ordering, do more work to determine exactly where the
         * insertion index should be. Otherwise, just return the insertion index
         * that was found.
         */
        if ((lower < max) && (compareHazardEvents(identifier,
                tabularEntities.get(lower).getIdentifier()) == 0)) {

            /*
             * Found the lower and upper boundaries of the range of tabular
             * entities equivalent to this one in terms of ordering.
             */
            int lowerEquivalentRange = lower;
            for (int index = lower - 1; index >= 0; index--) {
                if (compareHazardEvents(identifier,
                        tabularEntities.get(index).getIdentifier()) != 0) {
                    break;
                }
                lowerEquivalentRange = index;
            }
            int upperEquivalentRange = lower;
            for (int index = lower + 1; index < max; index++) {
                if (compareHazardEvents(identifier,
                        tabularEntities.get(index).getIdentifier()) != 0) {
                    break;
                }
                upperEquivalentRange = index;
            }

            /*
             * Compile a mapping of all the identifiers of events that are
             * equivalent in terms of ordering within the above-figured range to
             * their indices in the sorted identifiers list. Then get a set of
             * these same identifiers as a copy of the key set of the map, so
             * that the latter may be modified.
             */
            Map<String, Integer> indicesForEquivalentIdentifiers = new HashMap<>(
                    upperEquivalentRange + 1 - lowerEquivalentRange, 1.0f);
            for (int index = lowerEquivalentRange; index <= upperEquivalentRange; index++) {
                indicesForEquivalentIdentifiers
                        .put(tabularEntities.get(index).getIdentifier(), index);
            }
            Set<String> equivalentIdentifiers = new HashSet<>(
                    indicesForEquivalentIdentifiers.keySet());

            /*
             * Iterate through the events in the reverse of canonical non-sorted
             * order in order to find the event for which a tabular entity is
             * being inserted. While searching for it, record the most recently
             * encountered other equivalent event, so that the closest
             * sorting-equivalent event immediately following it will be known.
             * Note that iteration is done in reverse because new events are
             * likely to be at the end of the list.
             */
            String nextEquivalentEventIdentifier = null;
            List<IHazardEventView> events = sessionManager.getEventManager()
                    .getEventsForCurrentSettings();
            for (int index = events.size() - 1; (index >= 0)
                    && (equivalentIdentifiers.isEmpty() == false); index--) {
                IHazardEventView event = events.get(index);
                if (event.getEventID().equals(identifier)) {
                    break;
                }
                if (equivalentIdentifiers.remove(event.getEventID())) {
                    nextEquivalentEventIdentifier = event.getEventID();
                }
            }

            /*
             * If no equivalent event was found to follow this tabular entity's
             * event, the latter is to be inserted at the beginning of the range
             * of equivalent events if equivalent ranges are being reversed, or
             * at the end of said range if no reversal is being done.
             */
            boolean reversingEquivalents = isReversingEquivalentEntityRanges();
            if (nextEquivalentEventIdentifier == null) {
                return (reversingEquivalents ? lowerEquivalentRange
                        : upperEquivalentRange + 1);
            } else {
                return indicesForEquivalentIdentifiers
                        .get(nextEquivalentEventIdentifier)
                        + (reversingEquivalents ? 1 : 0);
            }
        } else {
            return lower;
        }
    }

    /**
     * Get a list of the identifiers of all events to be shown in the console's
     * tree, in canonical ordering if there are no sorting algorithms to be
     * applied, or in sorted order if such algorithms are found.
     * 
     * @return Sorted list of the identifiers of all events to be displayed in
     *         the console tree.
     */
    private List<String> getSortedEventIdentifiers() {

        /*
         * Get the list of hazard events, and compile the list of their event
         * identifiers so that it may be sorted. Also create a mapping of the
         * event identifiers to the events.
         */
        List<String> sortedEventIdentifiers = new ArrayList<>();
        eventsForIdentifiers.clear();
        for (IHazardEventView event : sessionManager.getEventManager()
                .getEventsForCurrentSettings()) {
            eventsForIdentifiers.put(event.getEventID(), event);
            sortedEventIdentifiers.add(event.getEventID());
        }

        /*
         * Determine whether there is at least one sort column, and do the sort
         * if there is.
         */
        if (sorts.isEmpty() == false) {

            /*
             * Sort the event identifiers.
             */
            Collections.sort(sortedEventIdentifiers, sortComparator);

            /*
             * If this sort requires sorting-equivalent ranges of events to be
             * reversed, iterate through the sorted items, finding any ranges
             * that are (from a sort perspective) equivalent, and swap those
             * ranges' orders. This needs to be done because otherwise, for
             * example, any time that the user changes the sort direction on a
             * column that has the same value for every row (or the same value
             * for two or more fields), the rows with equivalent values will not
             * swap vertical positions with respect to one another.
             */
            if (isReversingEquivalentEntityRanges()) {
                int startOfRange = -1;
                List<Range<Integer>> ranges = new ArrayList<>();
                for (int j = 0; j < sortedEventIdentifiers.size() - 1; j++) {
                    int result = compareHazardEvents(
                            sortedEventIdentifiers.get(j),
                            sortedEventIdentifiers.get(j + 1));
                    if (result == 0) {
                        if (startOfRange == -1) {
                            startOfRange = j;
                        }
                    } else if (startOfRange != -1) {
                        ranges.add(Range.closed(startOfRange, j));
                        startOfRange = -1;
                    }
                }
                if (startOfRange != -1) {
                    ranges.add(Range.closed(startOfRange,
                            sortedEventIdentifiers.size() - 1));
                }
                if (ranges.isEmpty() == false) {
                    for (Range<Integer> range : ranges) {
                        int start = range.lowerEndpoint();
                        int end = range.upperEndpoint();
                        int numSwaps = (end + 1 - start) / 2;
                        for (int j = 0; j < numSwaps; j++) {
                            String tempEventId = sortedEventIdentifiers
                                    .get(start + j);
                            sortedEventIdentifiers.set(start + j,
                                    sortedEventIdentifiers.get(end - j));
                            sortedEventIdentifiers.set(end - j, tempEventId);
                        }
                    }
                }
            }
        }

        /*
         * Return the sorted event identifiers.
         */
        return sortedEventIdentifiers;
    }

    /**
     * Determine whether or not to reverse the order of any ranges of events
     * that end up being equivalent in terms of sorting. The determination is
     * made by summing the sort directions of all the sorts and then taking
     * their remainder after dividing by 2; if the result is 0, no reversing
     * will be needed, but if it is 1, such event ranges should have their order
     * reversed.
     * 
     * @return <code>true</code> if equivalent entity ranges should have their
     *         ordering reversed, <code>false</code> otherwise.
     */
    private boolean isReversingEquivalentEntityRanges() {

        /*
         * Add up the sort directions' ordinals (meaning 0 for ascending sort, 1
         * for descending sort).
         */
        int sortDirectionSum = 0;
        for (Sort sort : sorts) {
            sortDirectionSum += sort.getSortDirection().ordinal();
        }

        /*
         * Return true if the sort direction sum is an odd number.
         */
        return (sortDirectionSum % 2 == 1);
    }

    /**
     * Compare the hazard events with the specified identifiers to determine
     * their ordering, using the {@link #sorts} sorting algorithms.
     * 
     * @param firstEventIdentifier
     *            Identifier of the first event to be compared.
     * @param secondEventIdentifier
     *            Identifier of the second event to be compared.
     * @return A number that is less than 0, 0, or greater than 0 indicating
     *         whether the first hazard event should be before, at the same
     *         position as, or after the second hazard event in ordering,
     *         respectively.
     */
    private int compareHazardEvents(String firstEventIdentifier,
            String secondEventIdentifier) {

        /*
         * Perform the comparison starting with the highest-priority sort, and
         * proceeding on down from there only if the previous comparison yielded
         * an equality result.
         */
        IHazardEventView firstEvent = eventsForIdentifiers
                .get(firstEventIdentifier);
        IHazardEventView secondEvent = eventsForIdentifiers
                .get(secondEventIdentifier);
        for (Sort sort : sorts) {

            /*
             * Compare the objects using a comparator if supplied, or as
             * expiration times if not.
             */
            Comparator<?> comparator = comparatorsForSortIdentifiers
                    .get(sort.getAttributeIdentifier());
            Class<?> type = typesForSortIdentifiers
                    .get(sort.getAttributeIdentifier());
            int result;
            if (comparator != null) {
                result = compareHazardEvents(firstEvent, secondEvent,
                        sort.getAttributeIdentifier(), sort.getSortDirection(),
                        type, comparator);
            } else {
                result = compare(sort.getSortDirection(),
                        countdownTimersForEventIdentifiers
                                .get(firstEventIdentifier).getExpireTime(),
                        countdownTimersForEventIdentifiers
                                .get(secondEventIdentifier).getExpireTime());
            }

            /*
             * If the result is not equality, return it.
             */
            if (result != 0) {
                return result;
            }
        }

        /*
         * Return equality if all the sorts yielded such.
         */
        return 0;
    }

    /**
     * Compare the two hazard events to determine their ordering if sorted by
     * the specified property.
     * 
     * @param firstEvent
     *            First event to be compared.
     * @param secondEvent
     *            Second event to be compared.
     * @param property
     *            Name of the property of the hazard events from which to take
     *            the values to perform the comparison.
     * @param sortDirection
     *            Direction of the sort.
     * @param typeClass
     *            Type of the values to be used for the comparison.
     * @param comparator
     *            Comparator to be used to perform the comparison of the values
     *            taken from the hazard events.
     * @return A number that is less than 0, 0, or greater than 0 indicating
     *         whether the first hazard event should be before, at the same
     *         position as, or after the second hazard event in ordering,
     *         respectively.
     */
    @SuppressWarnings("unchecked")
    private int compareHazardEvents(IHazardEventView firstEvent,
            IHazardEventView secondEvent, String property,
            SortDirection sortDirection, Class<?> typeClass,
            Comparator<?> comparator) {
        int result;
        if (typeClass == String.class) {
            result = ((Comparator<String>) comparator).compare(
                    getPropertyFromHazardEvent(firstEvent, property,
                            String.class),
                    getPropertyFromHazardEvent(secondEvent, property,
                            String.class));
        } else if (typeClass == Boolean.class) {
            result = ((Comparator<Boolean>) comparator).compare(
                    getPropertyFromHazardEvent(firstEvent, property,
                            Boolean.class),
                    getPropertyFromHazardEvent(secondEvent, property,
                            Boolean.class));
        } else if (typeClass == Double.class) {
            result = ((Comparator<Double>) comparator).compare(
                    getPropertyFromHazardEvent(firstEvent, property,
                            Double.class),
                    getPropertyFromHazardEvent(secondEvent, property,
                            Double.class));
        } else {
            result = ((Comparator<Date>) comparator).compare(
                    getPropertyFromHazardEvent(firstEvent, property,
                            Date.class),
                    getPropertyFromHazardEvent(secondEvent, property,
                            Date.class));
        }
        return result * (sortDirection == SortDirection.ASCENDING ? 1 : -1);
    }

    /**
     * Get the value of the specified property from the specified hazard event.
     * 
     * @param event
     *            Event from which to fetch the property value.
     * @param property
     *            Name of the property to be fetched.
     * @param typeClass
     *            Type to which to cast the result.
     * @return Value of the property, cast as specified, or <code>null</code> if
     *         there is no such property of the specified event.
     */
    @SuppressWarnings("unchecked")
    private <T> T getPropertyFromHazardEvent(IHazardEventView event,
            String property, Class<T> typeClass) {

        /*
         * Get the property fetcher for this property; if it is not found in the
         * map holding first-class entries, use the generic attribute fetcher.
         */
        IHazardEventPropertyFetcher fetcher = FETCHERS_FOR_HAZARD_EVENT_PROPERTIES
                .get(property);
        if (fetcher == null) {
            fetcher = HAZARD_ATTRIBUTE_FETCHER;
        }

        /*
         * Get the property, and return it cast as appropriate. If it should be
         * a string, convert it to one; if it should be a double, assume it's a
         * number and take its double value; otherwise, just cast it when
         * returning.
         */
        Object value = fetcher.getProperty(event, property, sessionManager);
        if (value == null) {
            return null;
        }
        if (typeClass == String.class) {
            return (T) value.toString();
        }
        if (typeClass == Double.class) {
            return (T) Double.valueOf(((Number) value).doubleValue());
        }
        return (T) value;
    }

    /**
     * Compare the first and second values.
     * 
     * @param sortDirection
     *            Direction of sorting.
     * @param firstValue
     *            First value to be compared.
     * @param secondValue
     *            Second value to be compared.
     * @return A value less than, equal to, or greater than 0 indicating that
     *         the first value is less than, equal to, or greater than the
     *         second, respectively.
     */
    private <T extends Comparable<T>> int compare(SortDirection sortDirection,
            T firstValue, T secondValue) {
        int result = (firstValue == null ? -1
                : (secondValue == null ? 1
                        : (firstValue.compareTo(secondValue))));
        return result * (sortDirection == SortDirection.ASCENDING ? 1 : -1);
    }
}

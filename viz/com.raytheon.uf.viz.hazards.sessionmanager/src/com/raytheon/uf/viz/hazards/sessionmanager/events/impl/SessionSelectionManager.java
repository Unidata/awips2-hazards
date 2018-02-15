/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Evaluation & Decision Support Branch (EDS)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package com.raytheon.uf.viz.hazards.sessionmanager.events.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import com.google.common.base.Predicate;
import com.google.common.collect.Sets;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEventView;
import com.raytheon.uf.common.util.Pair;
import com.raytheon.uf.viz.hazards.sessionmanager.events.ISessionSelectionManager;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionLastAccessedEventModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionSelectedEventsModified;
import com.raytheon.uf.viz.hazards.sessionmanager.impl.ISessionNotificationSender;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.IOriginator;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.Originator;

/**
 * Description: Implementation of the manager of the event selected set.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Jan 09, 2017   15556    Chris.Golden Initial creation.
 * Feb 16, 2017   29138    Chris.Golden Changed to not persist hazard
 *                                      events to database when
 *                                      changing from potential to
 *                                      pending status.
 * Jun 21, 2017   18375    Chris.Golden Removed setting of potential
 *                                      events to pending status when
 *                                      they are selected.
 * Sep 27, 2017   38072    Chris.Golden Now makes use of batching of
 *                                      notifications.
 * Dec 17, 2017   20739    Chris.Golden Refactored away access to
 *                                      directly mutable session
 *                                      events.
 * Apr 20, 2018   30227    Chris.Golden Fixed bug with code handling
 *                                      removal of event version
 *                                      identifier from selection set.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class SessionSelectionManager implements ISessionSelectionManager {

    // Private Static Constants

    /**
     * Integer comparator that reverses the natural ordering.
     */
    private static final Comparator<Integer> REVERSE_INTEGER_COMPARATOR = new Comparator<Integer>() {

        @Override
        public int compare(Integer o1, Integer o2) {
            return o2.compareTo(o1);
        }
    };

    // Private Variables

    /**
     * Session event manager.
     */
    private final SessionEventManager eventManager;

    /**
     * Notification sender.
     */
    private final ISessionNotificationSender notificationSender;

    /**
     * Subset of {@link #events} consisting of those hazard events for which
     * either a current version, one or more historical versions, or both, are
     * selected.
     */
    private final List<IHazardEventView> selectedEvents = new ArrayList<>();

    /**
     * Identifiers of the events found within {@link #selectedEvents}.
     */
    private final Set<String> selectedEventIdentifiers = new HashSet<>();

    /**
     * The {@link #selectedEventIdentifiers} but canonically ordered so that
     * they occur in the same order with respect to one another as the events
     * with these identifiers do within {@link #events}.
     */
    private final List<String> selectedEventIdentifiersOrdered = new ArrayList<>();

    /**
     * Identifiers, each consisting of an event identifier and either
     * <code>null</code> if it represents the current version of said event, or
     * else an index into the history list for the event if it represents a
     * historical version of said event. All the versions found here are
     * selected.
     */
    private final Set<Pair<String, Integer>> selectedCurrentAndHistoricalEventIdentifiers = new HashSet<>();

    /**
     * The {@link #selectedCurrentAndHistoricalEventIdentifiers} but canonically
     * ordered so that they occur in the same order with respect to one another
     * as the events with these identifiers do within {@link #events}.
     */
    private final List<Pair<String, Integer>> selectedCurrentAndHistoricalEventIdentifiersOrdered = new ArrayList<>();

    /**
     * Map pairing event identifiers for which at least one historical version
     * is selected with the indices of the version(s) selected, said indices
     * being into the history list for that event. The selected indices for a
     * given event identifier are collected into a sorted set, which when
     * iterated over returns the indices in reverse natural order, e.g. 7, 3, 1,
     * 0.
     */
    private final Map<String, SortedSet<Integer>> selectedIndicesForHistoricalEventIdentifiers = new HashMap<>();

    /**
     * Stack of identifiers of event versions that were most recently accessed,
     * with the top of the stack holding the most recently accessed event
     * version's identifier, the next one down holding the next-most-recently
     * accessed, etc.
     */
    private final Deque<Pair<String, Integer>> accessedEventsStack = new LinkedList<>();

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param eventManager
     *            Session event manager holding the events that will be used to
     *            create and maintain the selection set.
     * @param notificationSender
     *            Sender of notifications that must be posted concerning changes
     *            to the selection set.
     */
    public SessionSelectionManager(SessionEventManager eventManager,
            ISessionNotificationSender notificationSender) {
        this.eventManager = eventManager;
        this.notificationSender = notificationSender;
    }

    // Public Methods

    @Override
    public boolean isSelected(IHazardEventView eventView) {
        return isSelected(eventView.getEventID());
    }

    @Override
    public boolean isSelected(String eventIdentifier) {
        return selectedEventIdentifiers.contains(eventIdentifier);
    }

    @Override
    public boolean isSelected(Pair<String, Integer> eventVersionIdentifier) {
        return selectedCurrentAndHistoricalEventIdentifiers
                .contains(eventVersionIdentifier);
    }

    @Override
    public List<IHazardEventView> getSelectedEvents() {
        return Collections.unmodifiableList(selectedEvents);
    }

    @Override
    public Set<String> getSelectedEventIdentifiers() {
        return Collections.unmodifiableSet(selectedEventIdentifiers);
    }

    @Override
    public List<String> getSelectedEventIdentifiersList() {
        return Collections.unmodifiableList(selectedEventIdentifiersOrdered);
    }

    @Override
    public Set<Pair<String, Integer>> getSelectedEventVersionIdentifiers() {
        return Collections
                .unmodifiableSet(selectedCurrentAndHistoricalEventIdentifiers);
    }

    @Override
    public List<Pair<String, Integer>> getSelectedEventVersionIdentifiersList() {
        return Collections.unmodifiableList(
                selectedCurrentAndHistoricalEventIdentifiersOrdered);
    }

    @Override
    public void setSelectedEvents(
            Collection<? extends IHazardEventView> selectedEventViews,
            IOriginator originator) {

        Set<String> selectedEventIdentifiers = new HashSet<>(
                selectedEventViews.size());
        for (IHazardEventView eventView : selectedEventViews) {
            selectedEventIdentifiers.add(eventView.getEventID());
        }

        setSelectedEventIdentifiers(selectedEventIdentifiers, originator);
    }

    @Override
    public void setSelectedEventIdentifiers(
            Set<String> selectedEventIdentifiers, IOriginator originator) {

        /*
         * Create a set of identifiers of current and, optionally, historical
         * versions of the events specified as the new selection set. Include
         * the current versions of any such events, as well as any historical
         * versions of the same events that are already selected. This preserves
         * the historical versions' selection for those events that are to
         * remain selected. Also create a set of selected event identifiers, and
         * a corresponding list of selected events.
         */
        Set<String> toBeSelected = selectedEventIdentifiers;
        List<IHazardEventView> selectedEvents = new ArrayList<>(
                toBeSelected.size());
        selectedEventIdentifiers = new LinkedHashSet<>(toBeSelected.size(),
                1.0f);
        Set<Pair<String, Integer>> selectedCurrentAndHistoricalEventIdentifiers = new LinkedHashSet<>(
                toBeSelected.size() * 2, 1.0f);
        for (IHazardEventView eventView : eventManager.getEvents()) {
            String eventIdentifier = eventView.getEventID();
            if (toBeSelected.contains(eventIdentifier)) {
                selectedEvents.add(eventView);
                selectedEventIdentifiers.add(eventIdentifier);
                Pair<String, Integer> currentIdentifier = new Pair<>(
                        eventIdentifier, null);
                selectedCurrentAndHistoricalEventIdentifiers
                        .add(currentIdentifier);
                SortedSet<Integer> selectedHistoricalIndices = selectedIndicesForHistoricalEventIdentifiers
                        .get(eventIdentifier);
                if (selectedHistoricalIndices != null) {
                    for (Integer index : selectedHistoricalIndices) {
                        Pair<String, Integer> historicalIdentifier = new Pair<>(
                                eventIdentifier, index);
                        selectedCurrentAndHistoricalEventIdentifiers
                                .add(historicalIdentifier);
                    }
                }
            }
        }

        /*
         * Change the selection set.
         */
        setSelectedEventIdentifiers(selectedEvents, selectedEventIdentifiers,
                selectedCurrentAndHistoricalEventIdentifiers, originator, null);
    }

    @Override
    public void setSelectedEventVersionIdentifiers(
            Set<Pair<String, Integer>> selectedEventVersionIdentifiers,
            IOriginator originator) {

        /*
         * Remember the event versions that the caller is trying to select, and
         * compile a set of the identifiers of events for which current or
         * historical versions are to be selected. Also compile a set of the
         * identifiers of events for which specifically current versions are to
         * be selected, and finally, create a map of the selected historical
         * indices for each of the events.
         */
        Set<Pair<String, Integer>> toBeSelected = selectedEventVersionIdentifiers;
        Set<String> eventsToBeSelected = new HashSet<>(toBeSelected.size(),
                1.0f);
        Set<String> currentEventsToBeSelected = new HashSet<>(
                toBeSelected.size(), 1.0f);
        Map<String, SortedSet<Integer>> selectedIndicesForHistoricalEventIdentifiers = new HashMap<>(
                toBeSelected.size(), 1.0f);
        for (Pair<String, Integer> identifier : toBeSelected) {
            eventsToBeSelected.add(identifier.getFirst());
            if (identifier.getSecond() == null) {
                currentEventsToBeSelected.add(identifier.getFirst());
            } else {
                addHistoricalIndexToIndicesForHistoricalEventIdentifiers(
                        selectedIndicesForHistoricalEventIdentifiers,
                        identifier);
            }
        }

        /*
         * Create a list of events for which some version will be selected, as
         * well as an ordered set of the same events' identifiers, and then an
         * ordered set of the identifiers of current and historical events that
         * will be selected. Then iterate through the session events in
         * canonical order, adding to these sets and lists as appropriate.
         */
        List<IHazardEventView> selectedEvents = new ArrayList<>(
                toBeSelected.size());
        Set<String> selectedEventIdentifiers = new LinkedHashSet<>(
                toBeSelected.size(), 1.0f);
        Set<Pair<String, Integer>> selectedCurrentAndHistoricalEventIdentifiers = new LinkedHashSet<>(
                toBeSelected.size(), 1.0f);
        for (IHazardEventView eventView : eventManager.getEvents()) {
            String eventIdentifier = eventView.getEventID();
            if (eventsToBeSelected.contains(eventIdentifier)) {
                selectedEvents.add(eventView);
                selectedEventIdentifiers.add(eventIdentifier);
                if (currentEventsToBeSelected.contains(eventIdentifier)) {
                    selectedCurrentAndHistoricalEventIdentifiers.add(
                            new Pair<String, Integer>(eventIdentifier, null));
                }
                SortedSet<Integer> selectedHistoricalIndices = selectedIndicesForHistoricalEventIdentifiers
                        .get(eventIdentifier);
                if (selectedHistoricalIndices != null) {
                    for (Integer index : selectedHistoricalIndices) {
                        selectedCurrentAndHistoricalEventIdentifiers
                                .add(new Pair<>(eventIdentifier, index));
                    }
                }
            }
        }

        /*
         * Remove any selected indices compiled above that are associated with
         * events that were not found in the session events and thus cannot be
         * selected.
         */
        Set<String> eventsNotFound = Sets.difference(
                selectedIndicesForHistoricalEventIdentifiers.keySet(),
                selectedEventIdentifiers);
        for (String eventIdentifier : eventsNotFound) {
            selectedIndicesForHistoricalEventIdentifiers
                    .remove(eventIdentifier);
        }

        /*
         * Change the selection set.
         */
        setSelectedEventIdentifiers(selectedEvents, selectedEventIdentifiers,
                selectedCurrentAndHistoricalEventIdentifiers, originator,
                selectedIndicesForHistoricalEventIdentifiers);
    }

    @Override
    public void addEventToSelectedEvents(String selectedEventIdentifier,
            IOriginator originator) {
        addEventToSelectedEvents(
                new Pair<String, Integer>(selectedEventIdentifier, null),
                originator);
    }

    @Override
    public void addEventVersionToSelectedEvents(
            Pair<String, Integer> selectedEventVersionIdentifier,
            IOriginator originator) {
        addEventToSelectedEvents(selectedEventVersionIdentifier, originator);
    }

    @Override
    public void addEventsToSelectedEvents(Set<String> selectedEventIdentifiers,
            IOriginator originator) {

        /*
         * Compile a set of identifiers of the current versions of the specified
         * events to be added.
         */
        Set<Pair<String, Integer>> selectedEventVersionIdentifiers = new HashSet<>(
                selectedEventIdentifiers.size(), 1.0f);
        for (String eventIdentifier : selectedEventIdentifiers) {
            selectedEventVersionIdentifiers
                    .add(new Pair<String, Integer>(eventIdentifier, null));
        }

        /*
         * Add the specified versions.
         */
        addEventsToSelectedEvents(selectedEventIdentifiers,
                selectedEventVersionIdentifiers, originator);
    }

    @Override
    public void addEventVersionsToSelectedEvents(
            Set<Pair<String, Integer>> selectedEventVersionIdentifiers,
            IOriginator originator) {

        /*
         * Filter out any historical version identifiers that have indices that
         * are too high for their events' history lists.
         */
        selectedEventVersionIdentifiers = new HashSet<>(
                Sets.filter(selectedEventVersionIdentifiers,
                        new Predicate<Pair<String, Integer>>() {
                            @Override
                            public boolean apply(Pair<String, Integer> input) {
                                if (input.getSecond() == null) {
                                    return true;
                                }
                                Integer historicalCount = eventManager
                                        .getHistoricalVersionCountForEvent(
                                                input.getFirst());
                                return ((historicalCount != null) && (input
                                        .getSecond() < historicalCount));
                            }
                        }));

        /*
         * Compile a set of the events whose selection is being altered.
         */
        Set<String> selectedEventIdentifiers = new HashSet<>(
                selectedEventVersionIdentifiers.size(), 1.0f);
        for (Pair<String, Integer> identifier : selectedEventVersionIdentifiers) {
            selectedEventIdentifiers.add(identifier.getFirst());
        }

        /*
         * Add the specified versions.
         */
        addEventsToSelectedEvents(selectedEventIdentifiers,
                selectedEventVersionIdentifiers, originator);
    }

    @Override
    public void removeEventFromSelectedEvents(String selectedEventIdentifier,
            IOriginator originator) {

        /*
         * Do nothing if this event is not selected.
         */
        if (selectedEventIdentifiers
                .contains(selectedEventIdentifier) == false) {
            return;
        }

        /*
         * Compile the set of all selected versions of this event.
         */
        Set<Pair<String, Integer>> selectedCurrentAndHistoricalEventIdentifiers = getAllSelectedVersionsOfEvent(
                selectedEventIdentifier);

        /*
         * Remove all versions of the event from the selected set.
         */
        removeEventsFromSelectedEvents(Sets.newHashSet(selectedEventIdentifier),
                (selectedCurrentAndHistoricalEventIdentifiers
                        .contains(new Pair<String, Integer>(
                                selectedEventIdentifier, null))
                                        ? Sets.newHashSet(
                                                selectedEventIdentifier)
                                        : Collections.<String> emptySet()),
                selectedCurrentAndHistoricalEventIdentifiers,
                selectedIndicesForHistoricalEventIdentifiers, originator);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void removeEventVersionFromSelectedEvents(
            Pair<String, Integer> selectedEventVersionIdentifier,
            IOriginator originator) {

        /*
         * If this event version is not selected, there is nothing to be done.
         */
        if (selectedCurrentAndHistoricalEventIdentifiers
                .contains(selectedEventVersionIdentifier) == false) {
            return;
        }

        /*
         * Determine how many versions of this event will remain selected after
         * this one is deselected; if this was the only version of this event
         * that was selected, remove the event from the selection set.
         */
        SortedSet<Integer> selectedIndices = selectedIndicesForHistoricalEventIdentifiers
                .get(selectedEventVersionIdentifier.getFirst());
        int numSelectedVersions = (selectedCurrentAndHistoricalEventIdentifiers
                .contains(new Pair<String, Integer>(
                        selectedEventVersionIdentifier.getFirst(), null)) ? 1
                                : 0)
                + (selectedIndices != null ? selectedIndices.size() : 0);
        IHazardEventView removedEventView = null;
        if (numSelectedVersions == 1) {
            int removalIndex = selectedEventIdentifiersOrdered
                    .indexOf(selectedEventVersionIdentifier.getFirst());
            if (removalIndex != -1) {
                selectedEventIdentifiers
                        .remove(selectedEventVersionIdentifier.getFirst());
                selectedEventIdentifiersOrdered.remove(removalIndex);
                removedEventView = selectedEvents.remove(removalIndex);
            } else {
                throw new IllegalStateException(
                        "event to be deselected not found in list of selected session events");
            }
        }

        /*
         * Remove the event version identifier from the list and set of selected
         * versions, and remove its historical index, if it is a historical
         * version, from the set of selected historical versions for this event.
         */
        int removalIndex = selectedCurrentAndHistoricalEventIdentifiersOrdered
                .indexOf(selectedEventVersionIdentifier);
        if (removalIndex != -1) {
            selectedCurrentAndHistoricalEventIdentifiers
                    .remove(selectedEventVersionIdentifier);
            selectedCurrentAndHistoricalEventIdentifiersOrdered
                    .remove(removalIndex);
            if (selectedIndices != null) {
                if (selectedEventVersionIdentifier.getSecond() != null) {
                    selectedIndices
                            .remove(selectedEventVersionIdentifier.getSecond());
                }
                if (selectedIndices.isEmpty()) {
                    selectedIndicesForHistoricalEventIdentifiers
                            .remove(selectedEventVersionIdentifier.getFirst());
                }
            }
        } else {
            throw new IllegalStateException(
                    "event version to be deselected not found in set of selected session events");
        }

        notificationSender.startAccumulatingAsyncNotifications();

        /*
         * Send out a notification concerning the selection change.
         */
        notificationSender
                .postNotificationAsync(new SessionSelectedEventsModified(this,
                        (removedEventView != null
                                ? Sets.newHashSet(selectedEventVersionIdentifier
                                        .getFirst())
                                : Collections.<String> emptySet()),
                        Sets.newHashSet(selectedEventVersionIdentifier),
                        originator));

        /*
         * Update information about conflicts for selected events if no version
         * of this event is selected any longer.
         */
        if (removedEventView != null) {
            eventManager.updateConflictingEventsForSelectedEventIdentifiers(
                    removedEventView, true);
        }

        notificationSender.finishAccumulatingAsyncNotifications();
    }

    @Override
    public void removeEventsFromSelectedEvents(
            Set<String> selectedEventIdentifiers, IOriginator originator) {

        /*
         * Do nothing if these events are not selected.
         */
        selectedEventIdentifiers = new HashSet<>(Sets.intersection(
                this.selectedEventIdentifiers, selectedEventIdentifiers));
        if (selectedEventIdentifiers.isEmpty()) {
            return;
        }

        /*
         * Compile the set of all selected versions of these events, as well as
         * a set of all the selected current versions.
         */
        Set<String> selectedCurrentEventIdentifiers = new HashSet<>(
                selectedEventIdentifiers.size(), 1.0f);
        Set<Pair<String, Integer>> selectedCurrentAndHistoricalEventIdentifiers = new HashSet<>(
                selectedEventIdentifiers.size() * 2, 1.0f);
        for (String eventIdentifier : selectedEventIdentifiers) {
            if (this.selectedCurrentAndHistoricalEventIdentifiers.contains(
                    new Pair<String, Integer>(eventIdentifier, null))) {
                selectedCurrentEventIdentifiers.add(eventIdentifier);
            }
            selectedCurrentAndHistoricalEventIdentifiers
                    .addAll(getAllSelectedVersionsOfEvent(eventIdentifier));
        }

        /*
         * Remove all versions of the events from the selected set.
         */
        removeEventsFromSelectedEvents(selectedEventIdentifiers,
                selectedCurrentEventIdentifiers,
                selectedCurrentAndHistoricalEventIdentifiers,
                selectedIndicesForHistoricalEventIdentifiers, originator);
    }

    @Override
    public void removeEventVersionsFromSelectedEvents(
            Set<Pair<String, Integer>> selectedEventVersionIdentifiers,
            IOriginator originator) {

        /*
         * If these events are already unselected, there is nothing to be done.
         */
        selectedEventVersionIdentifiers = new HashSet<>(
                Sets.intersection(selectedEventVersionIdentifiers,
                        this.selectedCurrentAndHistoricalEventIdentifiers));
        if (selectedEventVersionIdentifiers.isEmpty()) {
            return;
        }

        /*
         * Compile a set of the current event identifiers to be deselected, and
         * a map of historical version indices for each event identifier that
         * are to be deselected.
         */
        Set<String> newlyDeselectedCurrentEventIdentifiers = new HashSet<>(
                selectedEventVersionIdentifiers.size(), 1.0f);
        Map<String, SortedSet<Integer>> newlyDeselectedIndicesForHistoricalEventIdentifiers = new HashMap<>(
                selectedEventVersionIdentifiers.size(), 1.0f);
        populateCurrentAndHistoricalVersionRecords(
                newlyDeselectedCurrentEventIdentifiers,
                newlyDeselectedIndicesForHistoricalEventIdentifiers,
                selectedEventVersionIdentifiers);

        /*
         * Get the set of all event identifiers that are having at least one
         * current or historical version removed from the selection set, and
         * iterate through a modifiable copy of that set. For each that has more
         * versions currently selected than are to be deselected, remove it from
         * the set, as the event as a whole is not to be deselected.
         */
        Set<String> newlyDeselectedEventIdentifiers = new HashSet<>(Sets.union(
                newlyDeselectedCurrentEventIdentifiers,
                newlyDeselectedIndicesForHistoricalEventIdentifiers.keySet()));
        for (Iterator<String> iterator = newlyDeselectedEventIdentifiers
                .iterator(); iterator.hasNext();) {
            String eventIdentifier = iterator.next();
            SortedSet<Integer> selectedIndices = selectedIndicesForHistoricalEventIdentifiers
                    .get(eventIdentifier);
            int numSelected = (selectedCurrentAndHistoricalEventIdentifiers
                    .contains(new Pair<String, Integer>(eventIdentifier, null))
                            ? 1 : 0)
                    + (selectedIndices != null ? selectedIndices.size() : 0);
            SortedSet<Integer> newlyDeselectedIndices = newlyDeselectedIndicesForHistoricalEventIdentifiers
                    .get(eventIdentifier);
            int numNewlyDeselected = (newlyDeselectedCurrentEventIdentifiers
                    .contains(eventIdentifier) ? 1 : 0)
                    + (newlyDeselectedIndices != null
                            ? newlyDeselectedIndices.size() : 0);
            if (numSelected > numNewlyDeselected) {
                iterator.remove();
            }
        }

        /*
         * Remove the appropriate versions of the events from the selected set.
         */
        removeEventsFromSelectedEvents(newlyDeselectedEventIdentifiers,
                newlyDeselectedCurrentEventIdentifiers,
                selectedEventVersionIdentifiers,
                newlyDeselectedIndicesForHistoricalEventIdentifiers,
                originator);
    }

    @Override
    public String getLastAccessedSelectedEvent() {
        Pair<String, Integer> identifier = getLastAccessedSelectedEventVersion();
        return (identifier == null ? null : identifier.getFirst());
    }

    @Override
    public Pair<String, Integer> getLastAccessedSelectedEventVersion() {
        if (accessedEventsStack.isEmpty()) {
            return null;
        }
        Pair<String, Integer> identifier = accessedEventsStack.peek();
        if (selectedCurrentAndHistoricalEventIdentifiers.contains(identifier)) {
            return identifier;
        } else {
            accessedEventsStack.pop();
            return getLastAccessedSelectedEventVersion();
        }
    }

    @Override
    public void setLastAccessedSelectedEvent(String eventIdentifier,
            IOriginator originator) {
        setLastAccessedSelectedEventVersion(
                new Pair<String, Integer>(eventIdentifier, null), originator);
    }

    @Override
    public void setLastAccessedSelectedEventVersion(
            Pair<String, Integer> eventVersionIdentifier,
            IOriginator originator) {
        if (eventVersionIdentifier
                .equals(accessedEventsStack.peek()) == false) {
            accessedEventsStack.remove(eventVersionIdentifier);
            accessedEventsStack.push(eventVersionIdentifier);
            notificationSender.postNotificationAsync(
                    new SessionLastAccessedEventModified(this, originator));
        }
    }

    // Private Methods

    /**
     * Set the selection lists and sets to those specified.
     * 
     * @param selectedEventViews
     *            Views of current versions of events that are to be selected.
     * @param selectedEventIdentifiers
     *            Idenifiers of the selected hazard events; must iterate in the
     *            order in which the identifiers are to be used.
     * @param selectedCurrentAndHistoricalEventIdentifiers
     *            Idenifiers of the current and historical versions of hazard
     *            events that are to be selected; must iterate in the order in
     *            which the identifiers are to be used.
     * @param originator
     *            Originator of the action.
     * @param selectedIndicesForHistoricalEventIdentifiers
     *            Map of event identifiers to the indices of historical versions
     *            of these events that are to be selected. This map's contents
     *            will replace those of
     *            {@link #selectedIndicesForHistoricalEventIdentifiers} unless
     *            this is specified as <code>null</code>, in which case instead
     *            the latter will be pruned of entries with keys that are not
     *            within <code>selectedEventIdentifiers</code>.
     */
    private void setSelectedEventIdentifiers(
            List<IHazardEventView> selectedEventViews,
            Set<String> selectedEventIdentifiers,
            Set<Pair<String, Integer>> selectedCurrentAndHistoricalEventIdentifiers,
            IOriginator originator,
            Map<String, SortedSet<Integer>> selectedIndicesForHistoricalEventIdentifiers) {

        /*
         * If the selected events set has not changed, do nothing.
         */
        if (selectedCurrentAndHistoricalEventIdentifiers
                .equals(this.selectedCurrentAndHistoricalEventIdentifiers)) {
            return;
        }

        /*
         * Remember the old selected event identifiers so as to have something
         * to compare the new ones to later, and do the same for the old
         * selected current and historical identifiers.
         */
        Set<String> oldSelectedEventIdentifiers = new HashSet<>(
                this.selectedEventIdentifiers);
        Set<Pair<String, Integer>> oldSelectedCurrentAndHistoricalEventIdentifiers = new HashSet<>(
                this.selectedCurrentAndHistoricalEventIdentifiers);

        /*
         * Replace the old identifiers in the selection sets and lists with the
         * new identifiers.
         */
        this.selectedEvents.clear();
        this.selectedEvents.addAll(selectedEventViews);
        this.selectedEventIdentifiers.clear();
        this.selectedEventIdentifiers.addAll(selectedEventIdentifiers);
        this.selectedEventIdentifiersOrdered.clear();
        this.selectedEventIdentifiersOrdered.addAll(selectedEventIdentifiers);
        this.selectedCurrentAndHistoricalEventIdentifiers.clear();
        this.selectedCurrentAndHistoricalEventIdentifiers
                .addAll(selectedCurrentAndHistoricalEventIdentifiers);
        this.selectedCurrentAndHistoricalEventIdentifiersOrdered.clear();
        this.selectedCurrentAndHistoricalEventIdentifiersOrdered
                .addAll(selectedCurrentAndHistoricalEventIdentifiers);

        /*
         * If the selected historical indices only need pruning, remove any
         * selected indices for events for which the current versions are no
         * longer selected. Otherwise, rebuild the map of events to selected
         * historical indices.
         */
        if (selectedIndicesForHistoricalEventIdentifiers == null) {
            Set<String> newlyDeselectedEventIdentifiers = Sets.difference(
                    oldSelectedEventIdentifiers, this.selectedEventIdentifiers);
            for (String eventIdentifier : newlyDeselectedEventIdentifiers) {
                this.selectedIndicesForHistoricalEventIdentifiers
                        .remove(eventIdentifier);
            }
        } else {
            this.selectedIndicesForHistoricalEventIdentifiers.clear();
            this.selectedIndicesForHistoricalEventIdentifiers
                    .putAll(selectedIndicesForHistoricalEventIdentifiers);
        }

        notificationSender.startAccumulatingAsyncNotifications();

        /*
         * Determine which events changed selection state and send out a
         * notification of any such change.
         */
        Set<String> changedSelectionStateEventIdentifiers = new HashSet<>(
                Sets.symmetricDifference(oldSelectedEventIdentifiers,
                        this.selectedEventIdentifiers));
        Set<Pair<String, Integer>> changedSelectionStateCurrentAndHistoricalEventIdentifiers = new HashSet<>(
                Sets.symmetricDifference(
                        oldSelectedCurrentAndHistoricalEventIdentifiers,
                        this.selectedCurrentAndHistoricalEventIdentifiers));
        if (changedSelectionStateCurrentAndHistoricalEventIdentifiers
                .isEmpty() == false) {
            notificationSender.postNotificationAsync(
                    new SessionSelectedEventsModified(this,
                            changedSelectionStateEventIdentifiers,
                            changedSelectionStateCurrentAndHistoricalEventIdentifiers,
                            originator));
        }

        /*
         * Set the last accessed event if there are events selected.
         */
        if (selectedCurrentAndHistoricalEventIdentifiersOrdered
                .isEmpty() == false) {

            /*
             * If no events are newly selected, make the last accessed event be
             * the last one still selected. Otherwise, if one event is newly
             * selected, make it the last accessed; finally, if more than one
             * event is newly selected, make the event from said events that is
             * last in the selection the last one accessed.
             */
            Set<Pair<String, Integer>> newlySelectedCurrentAndHistoricalEventIdentifiers = Sets
                    .difference(
                            this.selectedCurrentAndHistoricalEventIdentifiers,
                            oldSelectedCurrentAndHistoricalEventIdentifiers);
            if (newlySelectedCurrentAndHistoricalEventIdentifiers.isEmpty()) {
                setLastAccessedSelectedEventVersion(
                        selectedCurrentAndHistoricalEventIdentifiersOrdered
                                .get(selectedCurrentAndHistoricalEventIdentifiersOrdered
                                        .size() - 1),
                        Originator.OTHER);
            } else if (newlySelectedCurrentAndHistoricalEventIdentifiers
                    .size() == 1) {
                setLastAccessedSelectedEventVersion(
                        newlySelectedCurrentAndHistoricalEventIdentifiers
                                .iterator().next(),
                        Originator.OTHER);
            } else {
                for (int index = selectedCurrentAndHistoricalEventIdentifiersOrdered
                        .size() - 1; index > -1; index--) {
                    if (newlySelectedCurrentAndHistoricalEventIdentifiers
                            .contains(
                                    selectedCurrentAndHistoricalEventIdentifiersOrdered
                                            .get(index))) {
                        setLastAccessedSelectedEventVersion(
                                selectedCurrentAndHistoricalEventIdentifiersOrdered
                                        .get(index),
                                Originator.OTHER);
                        break;
                    }
                }
            }
        }

        /*
         * Update information about conflicts for selected events.
         */
        eventManager.updateConflictingEventsForSelectedEventIdentifiers(null,
                false);

        notificationSender.finishAccumulatingAsyncNotifications();
    }

    /**
     * Add the specified version of an event to the selected set.
     * 
     * @param selectedEventVersionIdentifier
     *            Identifier of the version of the event to be added.
     * @param originator
     *            Originator of the action.
     */
    @SuppressWarnings("unchecked")
    private void addEventToSelectedEvents(
            Pair<String, Integer> selectedEventVersionIdentifier,
            IOriginator originator) {

        /*
         * If this version of this event is already selected, or it is a
         * historical version that does not exist, there is nothing to be done.
         */
        Integer historicalCount = eventManager
                .getHistoricalVersionCountForEvent(
                        selectedEventVersionIdentifier.getFirst());
        if (selectedCurrentAndHistoricalEventIdentifiers
                .contains(selectedEventVersionIdentifier)
                || ((selectedEventVersionIdentifier.getSecond() != null)
                        && ((historicalCount == null)
                                || (historicalCount <= selectedEventVersionIdentifier
                                        .getSecond())))) {
            return;
        }

        /*
         * Iterate through the events in canonical order in order to determine
         * where in the selected events list the newly selected event should be
         * inserted, and where in the current and historical selected events
         * list the specified version's identifier should be inserted.
         */
        int insertionIndex = 0;
        int currentAndHistoricalInsertionIndex = 0;
        IHazardEventView selectedEventView = null;
        for (IHazardEventView eventView : eventManager.getEvents()) {
            String eventIdentifier = eventView.getEventID();
            if (eventIdentifier
                    .equals(selectedEventVersionIdentifier.getFirst())) {
                selectedEventView = eventView;
                if (selectedEventVersionIdentifier.getSecond() != null) {
                    if (selectedCurrentAndHistoricalEventIdentifiers
                            .contains(new Pair<>(eventIdentifier, null))) {
                        currentAndHistoricalInsertionIndex++;
                    }
                    SortedSet<Integer> selectedIndices = selectedIndicesForHistoricalEventIdentifiers
                            .get(eventIdentifier);
                    if (selectedIndices == null) {
                        selectedIndicesForHistoricalEventIdentifiers.put(
                                eventIdentifier,
                                createHistoricalIndicesSortedSet());
                    } else {
                        for (Integer index : selectedIndices) {
                            if (selectedEventVersionIdentifier
                                    .getSecond() > index) {
                                break;
                            }
                            currentAndHistoricalInsertionIndex++;
                        }
                    }
                }
                break;
            } else if (selectedEventIdentifiers.contains(eventIdentifier)) {
                insertionIndex++;
                currentAndHistoricalInsertionIndex += getSelectedCurrentAndHistoricalVersionCount(
                        eventIdentifier);
            }
        }

        /*
         * If the event was found in the events list, insert it into the
         * selected events lists and sets, both for current versions of events
         * and the combination current and historical versions as appropriate.
         */
        boolean addOverallEvent = false;
        if (selectedEventView != null) {
            if (selectedEventIdentifiers.contains(
                    selectedEventVersionIdentifier.getFirst()) == false) {
                addOverallEvent = true;
                selectedEvents.add(insertionIndex, selectedEventView);
                selectedEventIdentifiers
                        .add(selectedEventVersionIdentifier.getFirst());
                selectedEventIdentifiersOrdered.add(insertionIndex,
                        selectedEventVersionIdentifier.getFirst());
            }
            selectedCurrentAndHistoricalEventIdentifiers
                    .add(selectedEventVersionIdentifier);
            selectedCurrentAndHistoricalEventIdentifiersOrdered.add(
                    currentAndHistoricalInsertionIndex,
                    selectedEventVersionIdentifier);
            if (selectedEventVersionIdentifier.getSecond() != null) {
                selectedIndicesForHistoricalEventIdentifiers
                        .get(selectedEventVersionIdentifier.getFirst())
                        .add(selectedEventVersionIdentifier.getSecond());
            }
        } else {
            throw new IllegalStateException(
                    "event to be selected not found in list of session events");
        }

        notificationSender.startAccumulatingAsyncNotifications();

        /*
         * Send out a notification concerning the selection change.
         */
        notificationSender
                .postNotificationAsync(new SessionSelectedEventsModified(this,
                        (addOverallEvent
                                ? Sets.newHashSet(selectedEventVersionIdentifier
                                        .getFirst())
                                : Collections.<String> emptySet()),
                        Sets.newHashSet(selectedEventVersionIdentifier),
                        originator));

        /*
         * Set the last accessed event version to the one just added.
         */
        if (selectedCurrentAndHistoricalEventIdentifiersOrdered
                .isEmpty() == false) {
            setLastAccessedSelectedEventVersion(selectedEventVersionIdentifier,
                    Originator.OTHER);
        }

        /*
         * Update information about conflicts for selected events.
         */
        eventManager.updateConflictingEventsForSelectedEventIdentifiers(
                selectedEventView, false);

        notificationSender.finishAccumulatingAsyncNotifications();
    }

    /**
     * Add the specified current and historical versions of events to the
     * selection set.
     * 
     * @param selectedEventIdentifiers
     *            Idenifiers of the newly selected hazard events.
     * @param selectedCurrentAndHistoricalEventIdentifiers
     *            Idenifiers of the current and historical versions of hazard
     *            events that are to be selected.
     * @param originator
     *            Originator of the action.
     */
    private void addEventsToSelectedEvents(Set<String> selectedEventIdentifiers,
            Set<Pair<String, Integer>> selectedCurrentAndHistoricalEventIdentifiers,
            IOriginator originator) {

        /*
         * If these event versions are already selected, there is nothing to be
         * done.
         */
        selectedCurrentAndHistoricalEventIdentifiers = Sets.difference(
                selectedCurrentAndHistoricalEventIdentifiers,
                this.selectedCurrentAndHistoricalEventIdentifiers);
        if (selectedCurrentAndHistoricalEventIdentifiers.isEmpty()) {
            return;
        }

        /*
         * Make a modifiable copy of the immutable difference set, so that the
         * copy may be used when iterating through the session events in
         * canonical order below. Also create a set of the identifiers of the
         * to-be-selected current versions of events, and a map of event
         * identifiers to any to-be-selected historical indices for those
         * events.
         */
        Set<String> newlySelectedEventIdentifiers = new HashSet<>(
                Sets.difference(selectedEventIdentifiers,
                        this.selectedEventIdentifiers));
        Set<String> newlySelectedCurrentEventIdentifiers = new HashSet<>(
                newlySelectedEventIdentifiers.size(), 1.0f);
        Map<String, SortedSet<Integer>> newlySelectedIndicesForHistoricalEventIdentifiers = new HashMap<>(
                newlySelectedEventIdentifiers.size() * 2, 1.0f);
        for (Pair<String, Integer> identifier : selectedCurrentAndHistoricalEventIdentifiers) {
            if (identifier.getSecond() == null) {
                newlySelectedCurrentEventIdentifiers.add(identifier.getFirst());
            }
        }
        populateCurrentAndHistoricalVersionRecords(
                newlySelectedCurrentEventIdentifiers,
                newlySelectedIndicesForHistoricalEventIdentifiers,
                selectedCurrentAndHistoricalEventIdentifiers);

        /*
         * Remember the set of event identifiers that are about to be selected
         * for later, as well as the state of the selected current and
         * historical event version identifiers prior to the new selection.
         */
        selectedEventIdentifiers = new HashSet<>(newlySelectedEventIdentifiers);
        Set<Pair<String, Integer>> oldSelectedCurrentAndHistoricalEventIdentifiers = new HashSet<>(
                this.selectedCurrentAndHistoricalEventIdentifiers);

        /*
         * Iterate through the events in canonical order in order to determine
         * where in the selected event versions list the newly selected event
         * versions should be inserted, inserting each as it is encountered in
         * the iteration.
         */
        int insertionIndex = 0;
        int currentAndHistoricalInsertionIndex = 0;
        Pair<String, Integer> lastAdded = null;
        for (IHazardEventView eventView : eventManager.getEvents()) {

            /*
             * If this overall event has not yet been selected, insert it into
             * the selection set.
             */
            String eventIdentifier = eventView.getEventID();
            boolean selectedSomething = false;
            if (newlySelectedEventIdentifiers.remove(eventView.getEventID())) {

                /*
                 * Add the event and its identifier to the appropriate lists and
                 * set.
                 */
                this.selectedEventIdentifiers.add(eventIdentifier);
                this.selectedEventIdentifiersOrdered.add(insertionIndex,
                        eventIdentifier);
                this.selectedEvents.add(insertionIndex, eventView);
                insertionIndex++;
                selectedSomething = true;
            }

            /*
             * If the current version of this event is to be selected, add it to
             * the appropriate set and list.
             */
            if (newlySelectedCurrentEventIdentifiers.remove(eventIdentifier)) {
                Pair<String, Integer> currentIdentifier = new Pair<>(
                        eventIdentifier, null);
                this.selectedCurrentAndHistoricalEventIdentifiers
                        .add(currentIdentifier);
                this.selectedCurrentAndHistoricalEventIdentifiersOrdered.add(
                        currentAndHistoricalInsertionIndex, currentIdentifier);
                lastAdded = currentIdentifier;
                currentAndHistoricalInsertionIndex++;
                selectedSomething = true;
            }

            /*
             * If at least one historical version of this event is to be
             * selected, add said version(s).
             */
            SortedSet<Integer> newlySelectedIndices = newlySelectedIndicesForHistoricalEventIdentifiers
                    .remove(eventIdentifier);
            if (newlySelectedIndices != null) {

                /*
                 * Create a copy of the indices to be newly selected, as they
                 * will be added to the existing selected historical indices set
                 * later.
                 */
                SortedSet<Integer> newlySelectedIndicesCopy = new TreeSet<>(
                        newlySelectedIndices);

                /*
                 * Get the existing selected historical indices set for this
                 * event, creating it if it does not already exist.
                 */
                SortedSet<Integer> selectedIndices = selectedIndicesForHistoricalEventIdentifiers
                        .get(eventIdentifier);
                if (selectedIndices == null) {
                    selectedIndices = createHistoricalIndicesSortedSet();
                    selectedIndicesForHistoricalEventIdentifiers
                            .put(eventIdentifier, selectedIndices);
                }

                /*
                 * Iterate through the existing selected historical indices, for
                 * each one determining if any new indices should be inserted
                 * before it, and inserting historical version identifiers for
                 * each of them into the appropriate set and list as necessary.
                 */
                for (Integer index : selectedIndices) {
                    for (Iterator<Integer> iterator = newlySelectedIndices
                            .iterator(); iterator.hasNext();) {
                        Integer newIndex = iterator.next();
                        if (newIndex <= index) {
                            break;
                        }
                        iterator.remove();
                        Pair<String, Integer> historicalIdentifier = new Pair<>(
                                eventIdentifier, newIndex);
                        this.selectedCurrentAndHistoricalEventIdentifiers
                                .add(historicalIdentifier);
                        this.selectedCurrentAndHistoricalEventIdentifiersOrdered
                                .add(currentAndHistoricalInsertionIndex,
                                        historicalIdentifier);
                        lastAdded = historicalIdentifier;
                        currentAndHistoricalInsertionIndex++;
                    }
                    if (newlySelectedIndices.isEmpty()) {
                        break;
                    }
                }

                /*
                 * If there are still new indices to add, insert them into the
                 * appropriate list and set now.
                 */
                for (Integer newIndex : newlySelectedIndices) {
                    Pair<String, Integer> historicalIdentifier = new Pair<>(
                            eventIdentifier, newIndex);
                    this.selectedCurrentAndHistoricalEventIdentifiers
                            .add(historicalIdentifier);
                    this.selectedCurrentAndHistoricalEventIdentifiersOrdered
                            .add(currentAndHistoricalInsertionIndex,
                                    historicalIdentifier);
                    lastAdded = historicalIdentifier;
                    currentAndHistoricalInsertionIndex++;
                }

                /*
                 * Add the newly selected historical indices all at once to the
                 * existing selected historical indices set.
                 */
                selectedIndices.addAll(newlySelectedIndicesCopy);
                selectedSomething = true;
            }

            /*
             * If there are no more to-be-selected events left, stop.
             */
            if (selectedSomething && newlySelectedEventIdentifiers.isEmpty()
                    && newlySelectedCurrentEventIdentifiers.isEmpty()
                    && newlySelectedIndicesForHistoricalEventIdentifiers
                            .isEmpty()) {
                break;
            }

            /*
             * If no version of this event was just selected, and it is already
             * selected, increment the insertion indices as appropriate.
             */
            if ((selectedSomething == false) && this.selectedEventIdentifiers
                    .contains(eventIdentifier)) {
                insertionIndex++;
                currentAndHistoricalInsertionIndex += getSelectedCurrentAndHistoricalVersionCount(
                        eventIdentifier);
            }
        }

        /*
         * If at least one event that was to be selected was not found in the
         * events list, throw an error.
         */
        if ((newlySelectedEventIdentifiers.isEmpty() == false)
                || (newlySelectedCurrentEventIdentifiers.isEmpty() == false)
                || (newlySelectedIndicesForHistoricalEventIdentifiers
                        .isEmpty() == false)) {
            throw new IllegalStateException(
                    "event(s) to be selected not found in list of session events");
        }

        notificationSender.startAccumulatingAsyncNotifications();

        /*
         * Send out a notification concerning the selection change.
         */
        notificationSender
                .postNotificationAsync(new SessionSelectedEventsModified(this,
                        selectedEventIdentifiers,
                        new HashSet<>(Sets.difference(
                                this.selectedCurrentAndHistoricalEventIdentifiers,
                                oldSelectedCurrentAndHistoricalEventIdentifiers)),
                        originator));

        /*
         * Set the last accessed event version to the last one of those just
         * added.
         */
        if (lastAdded != null) {
            setLastAccessedSelectedEventVersion(lastAdded, Originator.OTHER);
        }

        /*
         * Update information about conflicts for selected events.
         */
        eventManager.updateConflictingEventsForSelectedEventIdentifiers(null,
                false);

        notificationSender.finishAccumulatingAsyncNotifications();
    }

    /**
     * Remove the specified current and historical versions of events from the
     * selection set.
     * 
     * @param selectedEventIdentifiers
     *            Identifiers of the newly deselected hazard events.
     * @param selectedCurrentEventIdentifiers
     *            Identifiers of the hazard events for which the current
     *            versions are newly deselected.
     * @param selectedCurrentAndHistoricalEventIdentifiers
     *            Idenifiers of the current and historical versions of hazard
     *            events that are to be deselected.
     * @param selectedIndicesForHistoricalEventIdentifiers
     *            Map holding an entry for every event identifier found as part
     *            of a {@link Pair} in
     *            <code>selectedCurrentAndHistoricalEventIdentifiers</code>;
     *            each entry pairs one of these event identifiers with the
     *            historical indices for that event that are to be removed.
     * @param originator
     *            Originator of the action.
     */
    private void removeEventsFromSelectedEvents(
            Set<String> selectedEventIdentifiers,
            Set<String> selectedCurrentEventIdentifiers,
            Set<Pair<String, Integer>> selectedCurrentAndHistoricalEventIdentifiers,
            Map<String, SortedSet<Integer>> selectedIndicesForHistoricalEventIdentifiers,
            IOriginator originator) {

        /*
         * Make a copy of the provided identifiers for the overall events and
         * the current versions that are to be deselected, so that these sets
         * may be modified in the iteration below, and the originals information
         * may be shared later.
         */
        Set<String> newlyDeselectedEventIdentifiers = new HashSet<>(
                selectedEventIdentifiers);
        Set<String> newlyDeselectedCurrentEventIdentifiers = new HashSet<>(
                selectedCurrentEventIdentifiers);
        Map<String, SortedSet<Integer>> newlyDeselectedIndicesForHistoricalEventIdentifiers = new HashMap<>(
                selectedIndicesForHistoricalEventIdentifiers);

        /*
         * Iterate through the events in canonical order in order to determine
         * from where in the selected event versions list the newly deselected
         * event versions should be removed, removing each as it is encountered
         * in the iteration.
         */
        int removalIndex = 0;
        int currentAndHistoricalRemovalIndex = 0;
        for (IHazardEventView eventView : eventManager.getEvents()) {

            /*
             * If this overall event is to be deselected, removal it from the
             * selection set. Otherwise, if it is selected, increment the
             * removal index to bypass it.
             */
            String eventIdentifier = eventView.getEventID();
            boolean deselectedSomething = false;
            if (newlyDeselectedEventIdentifiers
                    .remove(eventView.getEventID())) {

                /*
                 * Remove the event and its identifier from the appropriate
                 * lists and set.
                 */
                this.selectedEventIdentifiers.remove(eventIdentifier);
                this.selectedEventIdentifiersOrdered.remove(removalIndex);
                this.selectedEvents.remove(removalIndex);
                deselectedSomething = true;
            } else if (this.selectedEventIdentifiers
                    .contains(eventIdentifier)) {
                removalIndex++;
            }

            /*
             * If the current version of this event is to be deselected, remove
             * it from the appropriate set and list. Otherwise, if it is
             * selected, increment the version removal index to bypass it.
             */
            Pair<String, Integer> currentIdentifier = new Pair<>(
                    eventIdentifier, null);
            if (newlyDeselectedCurrentEventIdentifiers
                    .remove(eventIdentifier)) {
                this.selectedCurrentAndHistoricalEventIdentifiers
                        .remove(currentIdentifier);
                this.selectedCurrentAndHistoricalEventIdentifiersOrdered
                        .remove(currentAndHistoricalRemovalIndex);
                deselectedSomething = true;
            } else if (this.selectedCurrentAndHistoricalEventIdentifiers
                    .contains(currentIdentifier)) {
                currentAndHistoricalRemovalIndex++;
            }

            /*
             * If at least one historical version of this event is to be
             * deselected, remove it from the appropriate set and list, and
             * increment the version removal index for any that are not to be
             * removed but that are selected. Otherwise, just increment the
             * version removal index by the number of selected historical
             * versions for this event, if any.
             */
            SortedSet<Integer> newlyDeselectedIndices = newlyDeselectedIndicesForHistoricalEventIdentifiers
                    .remove(eventIdentifier);
            SortedSet<Integer> selectedIndices = this.selectedIndicesForHistoricalEventIdentifiers
                    .get(eventIdentifier);
            if (newlyDeselectedIndices != null) {

                /*
                 * Ensure that all the to-be-deselected indices are present in
                 * the selected indices set.
                 */
                if (Sets.difference(newlyDeselectedIndices, selectedIndices)
                        .isEmpty() == false) {
                    throw new IllegalStateException(
                            "historical event version to be deselected not found in set of selected session events");
                }

                /*
                 * Iterate through the existing selected historical indices, for
                 * each one determining if it should be removed, and removing
                 * historical version identifiers for each of the removed ones
                 * them from the appropriate set and list as necessary.
                 */
                Iterator<Integer> selectedIterator = selectedIndices.iterator();
                Iterator<Integer> newlyDeselectedIterator = newlyDeselectedIndices
                        .iterator();
                while (newlyDeselectedIterator.hasNext()) {
                    Integer selected = selectedIterator.next();
                    Integer newlyDeselected = newlyDeselectedIterator.next();
                    while (newlyDeselected < selected) {
                        currentAndHistoricalRemovalIndex++;
                        selected = selectedIterator.next();
                    }
                    Pair<String, Integer> historicalIdentifier = new Pair<>(
                            eventIdentifier, newlyDeselected);
                    this.selectedCurrentAndHistoricalEventIdentifiers
                            .remove(historicalIdentifier);
                    this.selectedCurrentAndHistoricalEventIdentifiersOrdered
                            .remove(currentAndHistoricalRemovalIndex);
                }

                /*
                 * Increment the removal index for every selected index
                 * remaining after having removed the ones that are to be
                 * deselected.
                 */
                while (selectedIterator.hasNext()) {
                    currentAndHistoricalRemovalIndex++;
                    selectedIterator.next();
                }

                /*
                 * Remove the newly deselected historical indices all at once
                 * from the existing selected historical indices set, removing
                 * the entire set itself if all the previously selected
                 * historical indices have been deselected.
                 */
                if (newlyDeselectedIndices.size() == selectedIndices.size()) {
                    this.selectedIndicesForHistoricalEventIdentifiers
                            .remove(eventIdentifier);
                } else {
                    selectedIndices.removeAll(newlyDeselectedIndices);
                }
                deselectedSomething = true;
            } else if (selectedIndices != null) {
                currentAndHistoricalRemovalIndex += selectedIndices.size();
            }

            /*
             * If there are no more to-be-deselected events left, stop.
             */
            if (deselectedSomething && newlyDeselectedEventIdentifiers.isEmpty()
                    && newlyDeselectedCurrentEventIdentifiers.isEmpty()
                    && newlyDeselectedIndicesForHistoricalEventIdentifiers
                            .isEmpty()) {
                break;
            }
        }

        /*
         * If at least one event that was to be deselected was not found in the
         * events list, throw an error.
         */
        if ((newlyDeselectedEventIdentifiers.isEmpty() == false)
                || (newlyDeselectedCurrentEventIdentifiers.isEmpty() == false)
                || (newlyDeselectedIndicesForHistoricalEventIdentifiers
                        .isEmpty() == false)) {
            throw new IllegalStateException(
                    "event(s) to be deselected not found in selected set of session events");
        }

        notificationSender.startAccumulatingAsyncNotifications();

        /*
         * Send out a notification concerning the selection change.
         */
        notificationSender
                .postNotificationAsync(new SessionSelectedEventsModified(this,
                        selectedEventIdentifiers,
                        selectedCurrentAndHistoricalEventIdentifiers,
                        originator));

        /*
         * Update information about conflicts for selected events.
         */
        eventManager.updateConflictingEventsForSelectedEventIdentifiers(null,
                false);

        notificationSender.finishAccumulatingAsyncNotifications();
    }

    /**
     * Given the specified set of current and historical event identifiers,
     * populate the specified set of current event identifiers with the
     * identifiers found in the first set that are for current versions, and
     * populate the specified map of event identifiers to the indices of
     * historical events with indices of any identifiers in the first set that
     * are for historical versions.
     * 
     * @param currentEventIdentifiers
     *            Set of identifiers of current events to be populated. This
     *            set's contents will be altered.
     * @param indicesForHistoricalEventIdentifiers
     *            Map pairing event identifiers with the indices of historical
     *            versions of that event to be populated. This map's contents
     *            will be altered.
     * @param selectedCurrentAndHistoricalEventIdentifiers
     *            Set of identifiers to be used to populate the other
     *            parameters.
     */
    private void populateCurrentAndHistoricalVersionRecords(
            Set<String> currentEventIdentifiers,
            Map<String, SortedSet<Integer>> indicesForHistoricalEventIdentifiers,
            Set<Pair<String, Integer>> selectedCurrentAndHistoricalEventIdentifiers) {
        for (Pair<String, Integer> identifier : selectedCurrentAndHistoricalEventIdentifiers) {
            if (identifier.getSecond() == null) {
                currentEventIdentifiers.add(identifier.getFirst());
            } else {
                addHistoricalIndexToIndicesForHistoricalEventIdentifiers(
                        indicesForHistoricalEventIdentifiers, identifier);
            }
        }
    }

    /**
     * Add the historical index from the specified current or historical version
     * identifier to the appropriate sorted set in the specified map of event
     * identifiers to historical indices, if the identifier is for a historical
     * version.
     * 
     * @param indicesForHistoricalEventIdentifiers
     *            Map to which to add the historical index, if any. This map's
     *            contents will be altered.
     * @param identifier
     *            Identifier from which to take the historical index.
     */
    private void addHistoricalIndexToIndicesForHistoricalEventIdentifiers(
            Map<String, SortedSet<Integer>> indicesForHistoricalEventIdentifiers,
            Pair<String, Integer> identifier) {

        /*
         * If this is not a historical version identifier, do nothing.
         */
        if (identifier.getSecond() == null) {
            return;
        }

        /*
         * If there are no historical versions of this event, or if the index
         * provided by the identifier is outside the bounds of the history list
         * for the event, do nothing.
         */
        Integer historicalVersionCount = eventManager
                .getHistoricalVersionCountForEvent(identifier.getFirst());
        if ((historicalVersionCount == null)
                || (historicalVersionCount <= identifier.getSecond())) {
            return;
        }

        /*
         * Get the set of selected historical indices provided; if one is not
         * found, create it.
         */
        SortedSet<Integer> selectedIndices = indicesForHistoricalEventIdentifiers
                .get(identifier.getFirst());
        if (selectedIndices == null) {
            selectedIndices = createHistoricalIndicesSortedSet();
            indicesForHistoricalEventIdentifiers.put(identifier.getFirst(),
                    selectedIndices);
        }

        /*
         * Add the historical index to the set.
         */
        selectedIndices.add(identifier.getSecond());
    }

    /**
     * Create a new historical indices sorted set, to be used as a value in an
     * entry in the {@link #selectedIndicesForHistoricalEventIdentifiers} map.
     * 
     * @return Created historical indices sorted set
     */
    private SortedSet<Integer> createHistoricalIndicesSortedSet() {
        return new TreeSet<>(REVERSE_INTEGER_COMPARATOR);
    }

    /**
     * Get the number of current and historical versions of the specified hazard
     * event that are selected.
     * 
     * @param eventIdentifier
     *            Identifier of the event for which the number of selected
     *            versions is to be calculated.
     * @return Number of versions of the hazard event that are selected.
     */
    private int getSelectedCurrentAndHistoricalVersionCount(
            String eventIdentifier) {
        int count = (selectedCurrentAndHistoricalEventIdentifiers
                .contains(new Pair<>(eventIdentifier, null)) ? 1 : 0);
        SortedSet<Integer> selectedIndices = selectedIndicesForHistoricalEventIdentifiers
                .get(eventIdentifier);
        if (selectedIndices != null) {
            count += selectedIndices.size();
        }
        return count;
    }

    /**
     * Get the identifiers of all the versions, current and/or historical, of
     * the specified hazard event that are selected.
     * 
     * @param selectedEventIdentifier
     *            Event identifier.
     * @return Identifiers of all the versions of the hazard event that are
     *         selected.
     */
    private Set<Pair<String, Integer>> getAllSelectedVersionsOfEvent(
            String selectedEventIdentifier) {
        Set<Pair<String, Integer>> selectedCurrentAndHistoricalEventIdentifiers = new HashSet<>(
                4, 1.0f);
        Pair<String, Integer> currentIdentifier = new Pair<String, Integer>(
                selectedEventIdentifier, null);
        if (this.selectedCurrentAndHistoricalEventIdentifiers
                .contains(currentIdentifier)) {
            selectedCurrentAndHistoricalEventIdentifiers.add(currentIdentifier);
        }
        SortedSet<Integer> selectedIndices = selectedIndicesForHistoricalEventIdentifiers
                .get(selectedEventIdentifier);
        if (selectedIndices != null) {
            for (Integer index : selectedIndices) {
                selectedCurrentAndHistoricalEventIdentifiers
                        .add(new Pair<>(selectedEventIdentifier, index));
            }
        }
        return selectedCurrentAndHistoricalEventIdentifiers;
    }
}

/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package com.raytheon.uf.viz.hazards.sessionmanager.events;

import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.raytheon.uf.common.util.Pair;
import com.raytheon.uf.viz.hazards.sessionmanager.ISessionNotification;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.IOriginator;

import gov.noaa.gsd.common.utilities.IMergeable;
import gov.noaa.gsd.common.utilities.MergeResult;

/**
 * Description: Notification of a change to the list of selected events, whether
 * the change is a reordering of what was already in the list, the addition of
 * one or more events, the removal of one or more events, or more than one of
 * these conditions.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * May 09, 2014    2925    Chris.Golden Initial creation.
 * Aug 18, 2016   19537    Chris.Golden Added set of identifiers of hazard
 *                                      events that have had their selection
 *                                      state changed.
 * Feb 01, 2017   15556    Chris.Golden Changed to be based upon new superclass.
 *                                      Also added a new selected versions
 *                                      set, indicating which versions of
 *                                      which hazard events have changed
 *                                      selection.
 * Sep 27, 2017   38072    Chris.Golden Implemented merge() method.
 * Dec 07, 2017   41886    Chris.Golden Removed Java 8/JDK 1.8 usage.
 * Dec 17, 2017   20739    Chris.Golden Refactored away access to directly
 *                                      mutable session events.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class SessionSelectedEventsModified extends SessionSelectionModified {

    // Private Variables

    /**
     * Identifiers of hazard events that have had their selection state changed.
     */
    private final Set<String> eventIdentifiers;

    /**
     * Identifiers of current and historical versions of events that have had
     * their selection state changed. Each such identifier consists of the event
     * identifier (just as is found within {@link #eventIdentifiers} paired with
     * either <code>null</code>, if it indicates the current version, or the
     * index of the version in the reversed history list of the event, if it
     * indicates a historical version.
     */
    private final Set<Pair<String, Integer>> currentAndHistoricalEventIdentifiers;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param selectionManager
     *            Selection manager.
     * @param eventIdentifiers
     *            Identifiers of the hazard events that have had their selection
     *            state changed. May be an empty set, if nothing has changed in
     *            terms of current versions of events that are selected.
     * @param currentAndHistoricalEventIdentifiers
     *            Identifiers of current and historical versions of events that
     *            have had their selection state changed. Each such identifier
     *            consists of the event identifier (just as is found within
     *            <code>eventIdentifiers</code> paired with either
     *            <code>null</code> , if it indicates the current version, or
     *            the index of the version in the reversed history list of the
     *            event, if it indicates a historical version.
     * @param originator
     *            Originator of the event.
     */
    public SessionSelectedEventsModified(
            ISessionSelectionManager selectionManager,
            Set<String> eventIdentifiers,
            Set<Pair<String, Integer>> currentAndHistoricalEventIdentifiers,
            IOriginator originator) {
        super(selectionManager, originator);
        this.eventIdentifiers = ImmutableSet.copyOf(eventIdentifiers);
        this.currentAndHistoricalEventIdentifiers = ImmutableSet
                .copyOf(currentAndHistoricalEventIdentifiers);
    }

    // Public Methods

    /**
     * Get the identifiers of the hazard events that have had their selection
     * state changed. Note that the returned set is not modifiable.
     * 
     * @return Identifiers of the hazard events.
     */
    public Set<String> getEventIdentifiers() {
        return eventIdentifiers;
    }

    /**
     * Get the identifiers of current and historical versions of events that
     * have had their selection state changed. Each such identifier consists of
     * the event identifier (just as is found within the set provided by
     * {@link #getEventIdentifiers()} paired with either <code>null</code>, if
     * it indicates the current version, or the index of the version in the
     * reversed history list of the event, if it indicates a historical version.
     * Note that the returned set is not modifiable.
     * 
     * @return Identifiers of current and historical versions of the hazard
     *         events.
     */
    public Set<Pair<String, Integer>> getCurrentAndHistoricalEventIdentifiers() {
        return currentAndHistoricalEventIdentifiers;
    }

    @Override
    public MergeResult<? extends ISessionNotification> merge(
            ISessionNotification original, ISessionNotification modified) {

        /*
         * If the new notification has the same originator as this one, and is
         * of the same type, merge the two together by combining their changes;
         * otherwise, the merge has failed.
         */
        if ((modified instanceof SessionSelectedEventsModified)
                && getOriginator()
                        .equals(((SessionSelectedEventsModified) modified)
                                .getOriginator())) {
            Set<String> eventIdentifiers = new HashSet<>(getEventIdentifiers());
            eventIdentifiers.addAll(((SessionSelectedEventsModified) modified)
                    .getEventIdentifiers());
            Set<Pair<String, Integer>> currentAndHistoricalEventIdentifiers = new HashSet<>(
                    getCurrentAndHistoricalEventIdentifiers());
            currentAndHistoricalEventIdentifiers
                    .addAll(((SessionSelectedEventsModified) modified)
                            .getCurrentAndHistoricalEventIdentifiers());
            return IMergeable.Helper.getSuccessObjectCancellationResult(
                    new SessionSelectedEventsModified(getSelectionManager(),
                            eventIdentifiers,
                            currentAndHistoricalEventIdentifiers,
                            getOriginator()));
        }
        return IMergeable.Helper.getFailureResult();
    }
}

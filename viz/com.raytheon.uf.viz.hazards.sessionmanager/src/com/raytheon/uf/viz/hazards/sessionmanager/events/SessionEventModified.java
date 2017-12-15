/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Evaluation & Decision Support Branch (EDS)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package com.raytheon.uf.viz.hazards.sessionmanager.events;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEventView;
import com.raytheon.uf.viz.hazards.sessionmanager.ISessionNotification;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.IOriginator;

import gov.noaa.gsd.common.utilities.IMergeable;
import gov.noaa.gsd.common.utilities.MergeResult;
import gov.noaa.gsd.common.utilities.Merger;

/**
 * Notification that will be sent out to notify all components that an event in
 * the session has had its properties changed in some way.
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Sep 20, 2017   38072    Chris.Golden Initial creation.
 * Dec 07, 2017   41886    Chris.Golden Removed Java 8/JDK 1.8 usage.
 * Dec 17, 2017   20739    Chris.Golden Refactored away access to directly
 *                                      mutable session events.
 * </pre>
 *
 * @author Chris.Golden
 */
public class SessionEventModified extends AbstractSessionEventModified {

    // Private Variables

    /**
     * List of modifications to the event.
     */
    private final List<IEventModification> modifications;

    /**
     * Map pairing classes of the modifications in {@link #modifications} with
     * lists of the associated modifications.
     */
    private final Map<Class<? extends IEventModification>, List<IEventModification>> modificationsForClasses;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param eventManager
     *            Event manager.
     * @param event
     *            Event that has been modified.
     * @param modification
     *            Modification that was made.
     * @param originator
     *            Originator of the change.
     */
    public SessionEventModified(ISessionEventManager eventManager,
            IHazardEventView event, IEventModification modification,
            IOriginator originator) {
        super(eventManager, event, originator);
        this.modifications = ImmutableList.of(modification);
        this.modificationsForClasses = getModificationsForClasses(
                this.modifications);
    }

    /**
     * Construct a standard instance.
     * 
     * @param eventManager
     *            Event manager.
     * @param event
     *            Event that has been modified.
     * @param modifications
     *            Modifications that were made.
     * @param originator
     *            Originator of the change.
     */
    public SessionEventModified(ISessionEventManager eventManager,
            IHazardEventView event, List<IEventModification> modifications,
            IOriginator originator) {
        super(eventManager, event, originator);
        this.modifications = ImmutableList.copyOf(modifications);
        this.modificationsForClasses = getModificationsForClasses(
                this.modifications);
    }

    // Public Methods

    /**
     * Get the modifications made to the event. Note that the returned list is
     * not modifiable.
     * 
     * @return Modifications made to the event.
     */
    public List<IEventModification> getModifications() {
        return modifications;
    }

    /**
     * Get the classes of all the modifications made to the event. Note that the
     * returned set is not modifiable.
     * 
     * @return Classes of all the modifications made to the event.
     */
    public Set<Class<? extends IEventModification>> getClassesOfModifications() {
        return modificationsForClasses.keySet();
    }

    /**
     * Get the modifications of the specified class, if any.
     * 
     * @param modificationClass
     *            Class of the modifications that are desired.
     * @return List of modifications of the specified class; may be empty.
     */
    public List<IEventModification> getModificationsForClass(
            Class<? extends IEventModification> modificationClass) {
        List<IEventModification> modifications = modificationsForClasses
                .get(modificationClass);
        return (modifications == null
                ? Collections.<IEventModification> emptyList() : modifications);
    }

    @Override
    public MergeResult<? extends ISessionNotification> merge(
            ISessionNotification original, ISessionNotification modified) {

        /*
         * If the new notification says that the event that this notifications
         * indicates was modified has been removed, nullify this notification;
         * otherwise, if the new notification indicates that the same event
         * addressed by this one has been modified in some way and said
         * notification has the same originator as this one, merge the two of
         * them; otherwise, no merge is possible.
         */
        if ((original instanceof SessionEventsRemoved) && getEventIdentifiers(
                ((SessionEventsRemoved) original).getEvents())
                        .contains(getEvent().getEventID())) {
            return IMergeable.Helper
                    .getSuccessSubjectCancellationResult(modified);

        } else if ((modified instanceof SessionEventModified)
                && getOriginator().equals(
                        ((SessionEventModified) modified).getOriginator())
                && getEvent().getEventID()
                        .equals(((SessionEventModified) modified).getEvent()
                                .getEventID())) {

            /*
             * Copy the immutable modifications list to a new one, and then
             * iterate through the modifications of the new notification,
             * merging each in turn into the copy of this notification's
             * modifications list.
             */
            List<IEventModification> modifications = new ArrayList<>(
                    this.modifications);
            for (IEventModification modification : ((SessionEventModified) modified)
                    .getModifications()) {
                Merger.merge(modifications, modification);
            }

            /*
             * Return a result indicating that the object notification is
             * nullified and the subject has taken in all of the object's
             * modifications.
             */
            return IMergeable.Helper.getSuccessObjectCancellationResult(
                    new SessionEventModified(getEventManager(), getEvent(),
                            modifications, getOriginator()));

        } else {
            return IMergeable.Helper.getFailureResult();
        }
    }

    // Private Methods

    /**
     * Get a map pairing classes of the specified modifications to lists of the
     * associated modifications.
     * 
     * @param modifications
     *            Modifications from which to compile the map.
     * @return Map pairing classes of the specified modifications to lists of
     *         the associated modifications.
     */
    private Map<Class<? extends IEventModification>, List<IEventModification>> getModificationsForClasses(
            List<IEventModification> modifications) {

        /*
         * Group the modifications into a map with keys of classes and values of
         * lists of the modifications for those classes, and then re-stream it
         * and convert the sublists into immutable lists.
         * 
         * TODO: When moving to Java 8, remove all the code below that is not
         * commented out, and uncomment the code immediately below it.
         */
        Map<Class<? extends IEventModification>, List<IEventModification>> modificationsForClasses = new HashMap<>(
                modifications.size(), 1.0f);
        for (IEventModification modification : modifications) {
            List<IEventModification> theseModifications = modificationsForClasses
                    .get(modification.getClass());
            if (theseModifications == null) {
                theseModifications = new ArrayList<>();
                modificationsForClasses.put(modification.getClass(),
                        theseModifications);
            }
            theseModifications.add(modification);
        }
        Map<Class<? extends IEventModification>, List<IEventModification>> immutableModificationsForClasses = new HashMap<>(
                modificationsForClasses.size(), 1.0f);
        for (Map.Entry<Class<? extends IEventModification>, List<IEventModification>> entry : modificationsForClasses
                .entrySet()) {
            immutableModificationsForClasses.put(entry.getKey(),
                    ImmutableList.copyOf(entry.getValue()));
        }
        return ImmutableMap.copyOf(immutableModificationsForClasses);
        // return ImmutableMap
        // .copyOf(modifications.stream()
        // .collect(Collectors
        // .groupingBy(IEventModification::getClass))
        // .entrySet().stream()
        // .collect(Collectors.toMap(entry -> entry.getKey(),
        // entry -> ImmutableList.copyOf(entry.getValue()))));
    }
}

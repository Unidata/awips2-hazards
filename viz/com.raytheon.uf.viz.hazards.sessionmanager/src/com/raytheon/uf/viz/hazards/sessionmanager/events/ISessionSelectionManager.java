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

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEventView;
import com.raytheon.uf.common.util.Pair;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.IOriginator;

/**
 * Description: Manager of the selected set of events.
 * <p>
 * Both the current versions of events and historical versions may be selected.
 * Methods to retrieve or set both current and historical versions return or
 * take as parameters {@link Pair} elements; each such <code>Pair</code> holds
 * the event identifier as a {@link String}, and, either <code>null</code> if it
 * indicates the current version of the event, or {@link Integer} if it
 * specifies a historical version. In the latter case, the <code>Integer</code>
 * provides the index into the history list of the event.
 * </p>
 * <p>
 * Methods that return either elements of the generic type <code>E</code> or
 * else <code>String</code> event identifiers, in contrast, indicate which
 * events have at least one version, current and/or historical, selected.
 * Methods that take as parameters these same types may be used to set the
 * current versions of hazard events as selected or unselected, and some such
 * methods have an effect on the historical versions that are selected as well;
 * see their individual descriptions for more information.
 * </p>
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Jan 09, 2017   15556    Chris.Golden Initial creation.
 * Dec 17, 2017   20739    Chris.Golden Refactored away access to directly
 *                                      mutable session events.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public interface ISessionSelectionManager {

    /**
     * Determine whether or not the specified event has its current and/or at
     * least one of its historical versions currently selected.
     * 
     * @param eventView
     *            View of the event to be checked.
     * @return <code>true</code> if the event is selected, <code>false</code>
     *         otherwise.
     */
    public boolean isSelected(IHazardEventView eventView);

    /**
     * Determine whether or not the specified event has its current and/or at
     * least one of its historical versions currently selected.
     * 
     * @param eventIdentifier
     *            Identifier of the event to be checked.
     * @return <code>true</code> if the event is selected, <code>false</code>
     *         otherwise.
     */
    public boolean isSelected(String eventIdentifier);

    /**
     * Determine whether or not the specified event version is currently
     * selected.
     * 
     * @param eventVersionIdentifier
     *            Identifier of the event version to be checked.
     * @return <code>true</code> if the version is selected, <code>false</code>
     *         otherwise.
     */
    public boolean isSelected(Pair<String, Integer> eventVersionIdentifier);

    /**
     * Get the selected events ordered canonically. This method returns the
     * current versions of any event for which either the current version, one
     * or more historical versions, or both are selected.
     * 
     * @return Views of selected events in canonical order. The list is an
     *         unmodifiable view, so it will always stay up to date with the
     *         latest selection. Attempts to modify it will result in an
     *         {@link UnsupportedOperationException}.
     */
    public List<IHazardEventView> getSelectedEvents();

    /**
     * Get the event identifiers of selected events as a set. This method
     * returns identifiers of any event for which either the current version,
     * one or more historical versions, or both are selected.
     * <p>
     * This method is a better option than
     * {@link #getSelectedEventIdentifiersList()} when checking to determine if
     * particular events are selected, as {@link Set} instances typicallly have
     * better performance than {@link List} instances for
     * {@link Collection#contains(Object)} invocations.
     * </p>
     * 
     * @return Selected event identifiers. The set is an unmodifiable view, so
     *         it will always stay up to date with the latest selection.
     *         Attempts to modify it will result in an
     *         {@link UnsupportedOperationException}.
     */
    public Set<String> getSelectedEventIdentifiers();

    /**
     * Get the selected event identifiers ordered canonically. This method
     * returns identifiers of any event for which either the current version,
     * one or more historical versions, or both are selected.
     * 
     * @return Selected event identifiers in canonical order. The list is an
     *         unmodifiable view, so it will always stay up to date with the
     *         latest selection. Attempts to modify it will result in an
     *         {@link UnsupportedOperationException}.
     */
    public List<String> getSelectedEventIdentifiersList();

    /**
     * Get the event version identifiers of selected events as a set.
     * <p>
     * This method is a better option than
     * {@link #getSelectedEventVersionIdentifiersList()} when checking to
     * determine if particular event versions are selected, as {@link Set}
     * instances typicallly have better performance than {@link List} instances
     * for {@link Collection#contains(Object)} invocations.
     * </p>
     * 
     * @return Selected event version identifiers. The set is an unmodifiable
     *         view, so it will always stay up to date with the latest
     *         selection. Attempts to modify it will result in an
     *         {@link UnsupportedOperationException}.
     */
    public Set<Pair<String, Integer>> getSelectedEventVersionIdentifiers();

    /**
     * Get the selected event version identifiers ordered canonically.
     * 
     * @return Selected event version identifiers in canonical order. The list
     *         is an unmodifiable view, so it will always stay up to date with
     *         the latest selection. Attempts to modify it will result in an
     *         {@link UnsupportedOperationException}.
     */
    public List<Pair<String, Integer>> getSelectedEventVersionIdentifiersList();

    /**
     * Set the selected events to those specified, deselecting all others. It is
     * assumed that these events are current versions, not historical ones.
     * <p>
     * This method affects the selection of historical versions of events as
     * well. Historical versions of any events not found in the specified
     * collection are deselected. For events that are found in the specified
     * collection, any historical versions that were selected remain so, while
     * others remain unselected.
     * </p>
     * 
     * @param selectedEventViews
     *            Views of new selected events.
     * @param originator
     *            Originator of the change.
     */
    public void setSelectedEvents(
            Collection<? extends IHazardEventView> selectedEventViews,
            IOriginator originator);

    /**
     * Select the current versions of the events that go with the specified
     * identifiers, unselecting all others.
     * <p>
     * This method affects the selection of historical versions of events as
     * well. Historical versions of any events not found in the specified
     * collection are deselected. For events that are found in the specified
     * collection, any historical versions that were selected remain so, while
     * others remain unselected.
     * </p>
     * 
     * @param selectedEventIdentifiers
     *            Identifiers of the new selected events.
     * @param originator
     *            Originator of the change.
     */
    public void setSelectedEventIdentifiers(
            Set<String> selectedEventIdentifiers, IOriginator originator);

    /**
     * Select the versions of the events that go with the specified identifiers,
     * unselecting all others.
     * 
     * @param selectedEventVersionIdentifiers
     *            Identifiers of the new selected versions of the events.
     * @param originator
     *            Originator of the change.
     */
    public void setSelectedEventVersionIdentifiers(
            Set<Pair<String, Integer>> selectedEventVersionIdentifiers,
            IOriginator originator);

    /**
     * Add the current version of the specified event to the set of selected
     * events, if it is not already selected.
     * 
     * @param selectedEventIdentifier
     *            Event to be added to the selected set.
     * @param originator
     *            Originator of the change.
     */
    public void addEventToSelectedEvents(String selectedEventIdentifier,
            IOriginator originator);

    /**
     * Add the specified current or historical event to the set of selected
     * events, if it is not already selected.
     * 
     * @param selectedEventVersionIdentifier
     *            Event to be added to the selected set.
     * @param originator
     *            Originator of the change.
     */
    public void addEventVersionToSelectedEvents(
            Pair<String, Integer> selectedEventVersionIdentifier,
            IOriginator originator);

    /**
     * Add the current versions of the specified events to the set of selected
     * events, if they are not already selected.
     * 
     * @param selectedEventIdentifiers
     *            Events to be added to the selected set.
     * @param originator
     *            Originator of the change.
     */
    public void addEventsToSelectedEvents(Set<String> selectedEventIdentifiers,
            IOriginator originator);

    /**
     * Add the specified current and historical versions of events to the set of
     * selected events, if they are not already selected.
     * 
     * @param selectedEventVersionIdentifiers
     *            Events to be added to the selected set.
     * @param originator
     *            Originator of the change.
     */
    public void addEventVersionsToSelectedEvents(
            Set<Pair<String, Integer>> selectedEventVersionIdentifiers,
            IOriginator originator);

    /**
     * Remove the current and historical versions of the specified event from
     * the set of selected events, if any versions of it are currently selected.
     * 
     * @param selectedEventIdentifier
     *            Event to be removed from the selected set.
     * @param originator
     *            Originator of the change.
     */
    public void removeEventFromSelectedEvents(String selectedEventIdentifier,
            IOriginator originator);

    /**
     * Remove the specified current or historical versions of an event from the
     * set of selected events, if it is currently selected.
     * 
     * @param selectedEventVersionIdentifier
     *            Event to be removed from the selected set.
     * @param originator
     *            Originator of the change.
     */
    public void removeEventVersionFromSelectedEvents(
            Pair<String, Integer> selectedEventVersionIdentifier,
            IOriginator originator);

    /**
     * Remove the current and historical versions of the specified events from
     * the set of selected events, if any versions of them are currently
     * selected.
     * 
     * @param selectedEventIdentifiers
     *            Events to be removed from the selected set.
     * @param originator
     *            Originator of the change.
     */
    public void removeEventsFromSelectedEvents(
            Set<String> selectedEventIdentifiers, IOriginator originator);

    /**
     * Remove the specified current and historical versions of events from the
     * set of selected events, if said versions are currently selected.
     * 
     * @param selectedEventVersionIdentifiers
     *            Events to be removed from the selected set.
     * @param originator
     *            Originator of the change.
     */
    public void removeEventVersionsFromSelectedEvents(
            Set<Pair<String, Integer>> selectedEventVersionIdentifiers,
            IOriginator originator);

    /**
     * Get the identifier of the selected event that was most recently accessed
     * (topmost in a GUI view, modified, etc.). This method returns the
     * identifier of the event for which either the current version or one of
     * its historical versions was last accessed.
     * 
     * @return Identifier of the most recently accessed selected event.
     */
    public String getLastAccessedSelectedEvent();

    /**
     * Get the identifier of the selected version of the event that was most
     * recently accessed (topmost in a GUI view, modified, etc.). The version
     * may be the current version, or one of the historical versions.
     * 
     * @return Identifier of the most recently accessed selected event version.
     */
    public Pair<String, Integer> getLastAccessedSelectedEventVersion();

    /**
     * Set the most recently accessed event version to the current version of
     * the event with the specified identifier.
     * 
     * @param eventIdentifier
     *            Identifier of the event for which the current version is being
     *            set to be the most recently accessed.
     * @param originator
     *            Originator of the change.
     */
    public void setLastAccessedSelectedEvent(String eventIdentifier,
            IOriginator originator);

    /**
     * Set the most recently accessed event version to that with the specified
     * identifier.
     * 
     * @param eventVersionIdentifier
     *            Identifier of the version (current or historical) of the event
     *            that is to be the most recently accessed.
     * @param originator
     *            Originator of the change.
     */
    public void setLastAccessedSelectedEventVersion(
            Pair<String, Integer> eventVersionIdentifier,
            IOriginator originator);
}

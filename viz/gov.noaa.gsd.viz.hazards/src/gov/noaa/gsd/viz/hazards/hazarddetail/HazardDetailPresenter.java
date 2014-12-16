/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.hazarddetail;

import gov.noaa.gsd.common.eventbus.BoundedReceptionEventBus;
import gov.noaa.gsd.viz.hazards.UIOriginator;
import gov.noaa.gsd.viz.hazards.display.HazardServicesPresenter;
import gov.noaa.gsd.viz.hazards.display.action.HazardDetailAction;
import gov.noaa.gsd.viz.hazards.display.action.HazardDetailAction.ActionType;
import gov.noaa.gsd.viz.megawidgets.MegawidgetSpecifierManager;
import gov.noaa.gsd.viz.mvp.widgets.ICommandInvocationHandler;
import gov.noaa.gsd.viz.mvp.widgets.IQualifiedStateChangeHandler;
import gov.noaa.gsd.viz.mvp.widgets.IStateChangeHandler;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.engio.mbassy.listener.Enveloped;
import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.subscription.MessageEnvelope;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardStatus;
import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardEventUtilities;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.time.TimeRange;
import com.raytheon.uf.viz.hazards.sessionmanager.ISessionManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.ObservedSettings;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Choice;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.HazardInfoConfig;
import com.raytheon.uf.viz.hazards.sessionmanager.events.ISessionEventManager;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionAutoCheckConflictsModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventAllowUntilFurtherNoticeModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventAttributesModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventMetadataModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventRemoved;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventScriptExtraDataAvailable;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventStatusModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventTimeRangeModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventTypeModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionLastChangedEventModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionSelectedEventConflictsModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionSelectedEventsModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.impl.ObservedHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.OriginatedSessionNotification;
import com.raytheon.uf.viz.hazards.sessionmanager.time.VisibleTimeRangeChanged;

/**
 * Hazard detail presenter, used to mediate between the model and the hazard
 * detail view.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Chris.Golden      Initial induction into repo
 * May 10, 2013            Chris.Golden      Change to Eclipse view implementation.
 * Jun 25, 2013            Chris.Golden      Added code to prevent reentrant
 *                                           behavior when receiving an event-
 *                                           changed notification.
 * Jul 15, 2013     585    Chris.Golden      Changed to support loading from bundle,
 *                                           including the passing in of the event
 *                                           bus so that the latter is no longer a
 *                                           singleton.
 * Nov 14, 2013    1463    Bryon.Lawrence    Added code to support hazard conflict
 *                                           detection.
 * Dec 03, 2013    2182    daniel.s.schaffer eliminated IHazardsIF
 * Feb 19, 2014    2161    Chris.Golden      Added passing of set of events allowing
 *                                           "until further notice" to the view
 *                                           during initialization.
 * Apr 09, 2014    2925    Chris.Golden      Refactored extensively to support new
 *                                           class-based metadata, as well as to
 *                                           conform to new event propagation scheme.
 * May 15, 2014    2925    Chris.Golden      Together with changes made in last
 *                                           2925 changeset, essentially rewritten
 *                                           to provide far better separation of
 *                                           concerns between model, view, and
 *                                           presenter; almost exclusively switch
 *                                           to scheme whereby the model is changed
 *                                           directly instead of via messages, and
 *                                           model changes are detected via various
 *                                           event-bus-listener methods (the sole
 *                                           remaining holdouts are the issue,
 *                                           propose, and preview commands, which
 *                                           are still sent via message to the
 *                                           message handler); and preparation for
 *                                           multithreading in the future.
 * Jun 25, 2014    4009    Chris.Golden      Added code to cache extra data held by
 *                                           metadata megawidgets between view
 *                                           instantiations.
 * Jun 30, 2014    3512    Chris.Golden      Changed to work with new versions of
 *                                           MVP widget classes.
 * Jul 03, 2014    3512    Chris.Golden      Added code to allow a duration selector
 *                                           to be displayed instead of an absolute
 *                                           date/time selector for the end time of
 *                                           a hazard event.
 * Aug 15, 2014    4243    Chris.Golden      Added ability to invoke event-modifying
 *                                           scripts via metadata-specified notifier
 *                                           megawidgets.
 * Sep 16, 2014    4753    Chris.Golden      Changed event script running to include
 *                                           mutable properties.
 * Oct 15, 2014    3498    Chris.Golden      Fixed bug where HID disappeared when
 *                                           switching perspectives, and could not
 *                                           be made visible again without bouncing
 *                                           H.S. (and sometimes CAVE).
 * Nov 18, 2014    4124    Chris.Golden      Adapted to new time manager.
 * Dec 05, 2014    4124    Chris.Golden      Changed to work with newly parameterized
 *                                           config manager.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class HazardDetailPresenter extends
        HazardServicesPresenter<IHazardDetailViewDelegate<?, ?>> {

    // Private Static Constants

    /**
     * String to be displayed for an event with no hazard type.
     */
    private static final String BLANK_TYPE_CHOICE = "";

    /**
     * Maximum number of events for which extra data objects may be stored by
     * the cache.
     */
    private static final int MAXIMUM_EVENT_EXTRA_DATA_CACHE_SIZE = 50;

    // Public Enumerated Types

    /**
     * Commands that may be invoked within the hazard detail view.
     */
    public enum Command {
        PREVIEW, PROPOSE, ISSUE
    };

    // Public Classes

    /**
     * Encapsulation of the displayable elements of a selected event identifier.
     */
    public class DisplayableEventIdentifier {

        // Private Variables

        /**
         * String describing the event.
         */
        private final String description;

        /**
         * Flag indicating whether or not the event has a conflict with at least
         * one other event.
         */
        private final boolean conflicting;

        // Private Constructors

        /**
         * Construct a standard instance.
         * 
         * @param description
         *            Description of the event.
         * @param conflicting
         *            Flag indicating whether or not the event has a conflict
         *            with at least one other event.
         */
        private DisplayableEventIdentifier(String description,
                boolean conflicting) {
            this.description = description;
            this.conflicting = conflicting;
        }

        // Public Methods

        /**
         * Get the description of the event.
         * 
         * @return Description of the event.
         */
        public final String getDescription() {
            return description;
        }

        /**
         * Determine whether or not the event has a conflict with at least one
         * other event.
         * 
         * @return True if the event has a conflict with at least one other
         *         event, false otherwise.
         */
        public final boolean isConflicting() {
            return conflicting;
        }
    }

    // Private Variables

    /**
     * Flag indicating whether or not the detail view is showing.
     */
    private boolean detailViewShowing;

    /**
     * List of selected event identifiers.
     */
    private List<String> selectedEventIdentifiers;

    /**
     * List of selected event identifier displayable elements; each one goes
     * with the event identifier from {@link #selectedEventIdentifiers} at the
     * corresponding index.
     */
    private List<DisplayableEventIdentifier> selectedEventDisplayables;

    /**
     * Currently visible event identifier within the
     * {@link #selectedEventIdentifiers}.
     */
    private String visibleEventIdentifier;

    /**
     * List of hazard category identifiers.
     */
    private final ImmutableList<String> categories;

    /**
     * Selected category.
     */
    private String selectedCategory;

    /**
     * Map pairing categories with lists of hazard type identifiers.
     */
    private final ImmutableMap<String, ImmutableList<String>> typeListsForCategories;

    /**
     * Map pairing categories with lists of hazard type descriptions.
     */
    private final ImmutableMap<String, ImmutableList<String>> typeDescriptionListsForCategories;

    /**
     * Map of event identifiers to their megawidget specifier managers.
     */
    private final Map<String, MegawidgetSpecifierManager> specifierManagersForSelectedEvents = new HashMap<>();

    /**
     * <p>
     * Map of event identifiers to maps holding extra data for the associated
     * metadata megawidgets. Only the most recently used extra data maps are
     * cached away; the maximum number that can be cached is
     * {@link #MAXIMUM_EVENT_EXTRA_DATA_CACHE_SIZE}.
     * </p>
     * <p>
     * This map is passed by reference to views during their initialization,
     * allowing them to update it so that if they are destroyed and then
     * recreated, the extra data stored here from the old view will be provided
     * to the new one.
     * </p>
     * <p>
     * Note that his map, once created, should be considered read-only by
     * instances of this class; the views may examine or modify it, and this may
     * occur in a different thread.
     * </p>
     */
    private final Map<String, Map<String, Map<String, Object>>> extraDataForEvents = new LinkedHashMap<String, Map<String, Map<String, Object>>>(
            MAXIMUM_EVENT_EXTRA_DATA_CACHE_SIZE + 1, 0.75f, true) {

        // Private Static Constants

        /**
         * Serial version UID.
         */
        private static final long serialVersionUID = 1L;

        // Protected Methods

        @Override
        protected final boolean removeEldestEntry(
                Map.Entry<String, Map<String, Map<String, Object>>> eldest) {
            return (size() > MAXIMUM_EVENT_EXTRA_DATA_CACHE_SIZE);
        }
    };

    /**
     * Set of identifiers of events that allow "until further notice" mode to be
     * toggled. This is kept up to date elsewhere, so it does not need to be
     * refreshed by this object. (It is also unmodifiable here.)
     */
    private final Set<String> eventIdentifiersAllowingUntilFurtherNotice;

    /**
     * Map of selected event identifiers to the events with which they conflict,
     * if any, as last fetched. This is kept up to date elsewhere, so it does
     * not need to be refreshed by this object. (It is also unmodifiable here.)
     */
    private final Map<String, Collection<IHazardEvent>> conflictingEventsForSelectedEventIdentifiers;

    /**
     * Detail view visibility change handler. The identifier is ignored.
     */
    private final IStateChangeHandler<String, Boolean> detailViewVisibilityChangeHandler = new IStateChangeHandler<String, Boolean>() {

        @Override
        public void stateChanged(String identifier, Boolean value) {
            if (Boolean.TRUE.equals(value)) {
                if (detailViewShowing == false) {
                    updateEntireView(getModel().getEventManager()
                            .getSelectedEvents());
                }
            } else {
                detailViewShowing = false;
            }
        }

        @Override
        public void statesChanged(Map<String, Boolean> valuesForIdentifiers) {
            handleUnsupportedOperationAttempt("visibility");
        }
    };

    /**
     * Visible event state change handler. The identifier is ignored.
     */
    private final IStateChangeHandler<String, String> visibleEventChangeHandler = new IStateChangeHandler<String, String>() {

        @Override
        public void stateChanged(String identifier, String value) {
            visibleEventIdentifier = value;
            ObservedHazardEvent event = getVisibleEvent();
            if (event != null) {
                getModel().getEventManager().setLastModifiedSelectedEvent(
                        event, UIOriginator.HAZARD_INFORMATION_DIALOG);
                if (selectedEventIdentifiers.contains(visibleEventIdentifier)) {
                    updateViewVisibleEvent();
                }
            }
        }

        @Override
        public void statesChanged(Map<String, String> valuesForIdentifiers) {
            handleUnsupportedOperationAttempt("visible event");
        }
    };

    /**
     * Category state change handler. The identifier is that of the changed
     * event.
     */
    private final IStateChangeHandler<String, String> categoryChangeHandler = new IStateChangeHandler<String, String>() {

        @Override
        public void stateChanged(String identifier, String value) {
            selectedCategory = value;
            ObservedHazardEvent event = getEventByIdentifier(identifier);
            if (event != null) {
                getModel().getEventManager().setEventCategory(event,
                        selectedCategory,
                        UIOriginator.HAZARD_INFORMATION_DIALOG);
                if (identifier.equals(visibleEventIdentifier)) {
                    updateViewTypeList(event);
                }
            }
        }

        @Override
        public void statesChanged(Map<String, String> valuesForIdentifiers) {
            handleUnsupportedOperationAttempt("category");
        }
    };

    /**
     * Type state change handler. The identifier is that of the changed event.
     */
    private final IStateChangeHandler<String, String> typeChangeHandler = new IStateChangeHandler<String, String>() {

        @Override
        public void stateChanged(String identifier, String value) {

            /*
             * Ensure that the event is around.
             */
            ObservedHazardEvent event = getEventByIdentifier(identifier);
            if (event == null) {
                return;
            }

            /*
             * Change the event's type.
             */
            String[] components = HazardEventUtilities
                    .getHazardPhenSigSubType(value);
            getModel().getEventManager().setEventType(event, components[0],
                    components[1], components[2],
                    UIOriginator.HAZARD_INFORMATION_DIALOG);

            /*
             * Since the changing of the event type might have instead resulted
             * in another event being created, refreshing of the selected event
             * displayables should only be done if no event was added and the
             * type was truly changed.
             */
            List<ObservedHazardEvent> selectedEvents = getModel()
                    .getEventManager().getSelectedEvents();
            List<String> selectedEventIdentifiers = compileSelectedEventIdentifiers(selectedEvents);
            if (selectedEventIdentifiers
                    .equals(HazardDetailPresenter.this.selectedEventIdentifiers)) {
                selectedEventDisplayables = compileSelectedEventDisplayables(selectedEvents);
                updateViewSelectedEvents();
                updateViewDurations(event);
                updateViewButtonsEnabledStates();
            }
        }

        @Override
        public void statesChanged(Map<String, String> valuesForIdentifiers) {
            handleUnsupportedOperationAttempt("type");
        }
    };

    /**
     * Time range state change handler. The identifier is that of the changed
     * event.
     */
    private final IStateChangeHandler<String, TimeRange> timeRangeChangeHandler = new IStateChangeHandler<String, TimeRange>() {

        @Override
        public void stateChanged(String identifier, TimeRange value) {
            ObservedHazardEvent event = getEventByIdentifier(identifier);
            if (event != null) {
                event.setTimeRange(new Date(value.getStart().getTime()),
                        new Date(value.getEnd().getTime()),
                        UIOriginator.HAZARD_INFORMATION_DIALOG);
            }
        }

        @Override
        public void statesChanged(Map<String, TimeRange> valuesForIdentifiers) {
            handleUnsupportedOperationAttempt("time range");
        }
    };

    /**
     * Metadata state change handler. The qualifier is the identifier of the
     * changed event, while the identifier is that of the metadata that changed.
     */
    private final IQualifiedStateChangeHandler<String, String, Serializable> metadataChangeHandler = new IQualifiedStateChangeHandler<String, String, Serializable>() {

        @Override
        public void stateChanged(String qualifier, String identifier,
                Serializable value) {
            ObservedHazardEvent event = getEventByIdentifier(qualifier);
            if (event != null) {
                event.addHazardAttribute(identifier, value,
                        UIOriginator.HAZARD_INFORMATION_DIALOG);
            }
        }

        @Override
        public void statesChanged(String qualifier,
                Map<String, Serializable> valuesForIdentifiers) {
            ObservedHazardEvent event = getEventByIdentifier(qualifier);
            if (event != null) {
                event.addHazardAttributes(valuesForIdentifiers,
                        UIOriginator.HAZARD_INFORMATION_DIALOG);
            }
        }
    };

    /**
     * Notifier invocation handler. The identifier is that of the event for
     * which the invocation is occurring coupled with that of the notifier that
     * has been invoked.
     */
    private final ICommandInvocationHandler<EventScriptInfo> notifierInvocationHandler = new ICommandInvocationHandler<EventScriptInfo>() {

        @Override
        public void commandInvoked(EventScriptInfo identifier) {
            ObservedHazardEvent event = getModel().getEventManager()
                    .getEventById(identifier.getEventIdentifier());
            if (event != null) {
                getModel().getEventManager().scriptCommandInvoked(event,
                        identifier.getDetailIdentifier(),
                        identifier.getMutableProperties());
            }
        }
    };

    /**
     * Button invocation handler. The identifier is the command.
     */
    private final ICommandInvocationHandler<Command> buttonInvocationHandler = new ICommandInvocationHandler<Command>() {

        @Override
        public void commandInvoked(Command identifier) {

            /*
             * TODO: This should call one or more methods directly in the
             * session manager, methods that also allow other UI components to
             * call them. This sending of an action via the event bus is the
             * only part of this UI component's code that is not directly
             * manipulating the model (i.e. session manager).
             */
            HazardDetailAction action = new HazardDetailAction(
                    (identifier == Command.PREVIEW ? ActionType.PREVIEW
                            : (identifier == Command.PROPOSE ? ActionType.PROPOSE
                                    : ActionType.ISSUE)));
            action.setOriginator(UIOriginator.HAZARD_INFORMATION_DIALOG);
            fireAction(action);
        }
    };

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param model
     *            Model to be handled by this presenter.
     * @param eventBus
     *            Event bus used to signal changes.
     */
    public HazardDetailPresenter(
            ISessionManager<ObservedHazardEvent, ObservedSettings> model,
            BoundedReceptionEventBus<Object> eventBus) {
        super(model, eventBus);
        List<ObservedHazardEvent> selectedEvents = model.getEventManager()
                .getSelectedEvents();
        selectedEventIdentifiers = compileSelectedEventIdentifiers(selectedEvents);
        selectedEventDisplayables = compileSelectedEventDisplayables(selectedEvents);
        eventIdentifiersAllowingUntilFurtherNotice = model.getEventManager()
                .getEventIdsAllowingUntilFurtherNotice();
        conflictingEventsForSelectedEventIdentifiers = model.getEventManager()
                .getConflictingEventsForSelectedEvents();

        /*
         * Get the hazard categories list, and a map of those categories to
         * their type lists, as well as a a similar map to their type
         * displayable lists. The latter two each start with an empty string,
         * used to indicate that no type has been chosen.
         */
        List<String> categories = new ArrayList<>();
        Map<String, ImmutableList<String>> typesForCategories = new HashMap<>();
        Map<String, ImmutableList<String>> typeDescriptionsForCategories = new HashMap<>();
        HazardInfoConfig categoriesAndTypes = getModel()
                .getConfigurationManager().getHazardInfoConfig();
        for (Choice categoryAndTypes : categoriesAndTypes.getHazardCategories()) {
            categories.add(categoryAndTypes.getDisplayString());
            List<Choice> typeChoices = categoryAndTypes.getChildren();
            List<String> types = new ArrayList<String>(typeChoices.size());
            List<String> typeDescriptions = new ArrayList<String>(
                    typeChoices.size());
            types.add(BLANK_TYPE_CHOICE);
            typeDescriptions.add(BLANK_TYPE_CHOICE);
            for (Choice hazardType : categoryAndTypes.getChildren()) {
                types.add(hazardType.getIdentifier());
                typeDescriptions.add(hazardType.getDisplayString());
            }
            typesForCategories.put(categoryAndTypes.getDisplayString(),
                    ImmutableList.copyOf(types));
            typeDescriptionsForCategories.put(
                    categoryAndTypes.getDisplayString(),
                    ImmutableList.copyOf(typeDescriptions));
        }
        this.categories = ImmutableList.copyOf(categories);
        this.typeListsForCategories = ImmutableMap.copyOf(typesForCategories);
        this.typeDescriptionListsForCategories = ImmutableMap
                .copyOf(typeDescriptionsForCategories);
    }

    // Public Methods

    /*
     * TODO: This method will be removed altogether from the Presenter in the
     * future, when all needed notifications are subscribed to directly by each
     * Presenter. For now, this method simply does nothing, as other member
     * methods marked with @Handler are being used to receive needed
     * notifications. This subclass of Presenter is thus far closer to what is
     * planned for all Presenters in the future.
     */
    @Deprecated
    @Override
    public void modelChanged(EnumSet<HazardConstants.Element> changed) {

        /*
         * No action.
         */
    }

    /**
     * Respond to session modifications by determining whether an issue or
     * preview are ongoing.
     * 
     * @param change
     *            Change that occurred.
     */
    @Handler
    public void sessionModified(final SessionModified change) {
        if (detailViewShowing) {
            updateViewButtonsEnabledStates();
        }
    }

    /**
     * Respond to the removal of an event.
     * 
     * @param change
     *            Change that occurred.
     */
    @Handler
    public void sessionEventRemoved(final SessionEventRemoved change) {

        /*
         * Remove any cached specifier manager for this event, and tell the view
         * that there are no metadata megawidgets for this event either.
         */
        String identifier = change.getEvent().getEventID();
        specifierManagersForSelectedEvents.remove(identifier);
    }

    /**
     * Respond to the list of selected events possibly having changed.
     * 
     * @param change
     *            Change that occurred.
     */
    @Handler
    public void sessionSelectedEventsModified(
            final SessionSelectedEventsModified change) {

        /*
         * If the detail view is not showing and there are selected events, show
         * it. In this case nothing more needs to be done, since part of the
         * showing process is updating it to include the correct information.
         */
        List<ObservedHazardEvent> selectedEvents = getModel().getEventManager()
                .getSelectedEvents();
        if ((selectedEvents.size() > 0) && (detailViewShowing == false)) {
            showHazardDetail();
            return;
        }

        /*
         * Get the selected events' identifiers as a list.
         */
        List<String> selectedEventIdentifiers = compileSelectedEventIdentifiers(selectedEvents);

        /*
         * If the list is different from the previously-current one, make it the
         * current one.
         */
        if (selectedEventIdentifiers.equals(this.selectedEventIdentifiers) == false) {

            /*
             * Remember the new selected events list, and get the visible event.
             */
            this.selectedEventIdentifiers = selectedEventIdentifiers;
            String oldVisibleEventIdentifier = visibleEventIdentifier;
            visibleEventIdentifier = getVisibleEventIdentifier();

            /*
             * Notify the view of the new list of selected events.
             */
            selectedEventDisplayables = compileSelectedEventDisplayables(selectedEvents);
            if (detailViewShowing) {
                updateViewSelectedEvents();
            }

            /*
             * Send on all details about the event to the view if the visible
             * event has changed.
             */
            if (detailViewShowing
                    && (visibleEventIdentifier != oldVisibleEventIdentifier)
                    && ((visibleEventIdentifier == null) || ((visibleEventIdentifier
                            .isEmpty() || selectedEventIdentifiers
                            .contains(visibleEventIdentifier)) && (oldVisibleEventIdentifier
                            .equals(visibleEventIdentifier) == false)))) {
                updateViewVisibleEvent();
            }
        }

        /*
         * If there are now no selected events and the view is showing, hide it.
         */
        if (selectedEvents.isEmpty() && detailViewShowing) {
            hideHazardDetail();
        }
    }

    /**
     * Respond to the last modified event in the list of selected events
     * possibly having changed.
     * 
     * @param change
     *            Change that occurred.
     */
    @Handler
    public void sessionLastModifiedSelectedEventModified(
            final SessionLastChangedEventModified change) {

        /*
         * Do nothing if the view is not showing, or if the selected event
         * identifiers do not contain the new visible event identifier.
         */
        if (detailViewShowing == false) {
            return;
        }
        String newVisibleEventIdentifier = getVisibleEventIdentifier();

        if (selectedEventIdentifiers.contains(newVisibleEventIdentifier) == false) {
            return;
        }

        /*
         * If the last selected event identifier is not the same as it was
         * before, update the view to match if the view was not the originator
         * of the change.
         */
        String oldVisibleEventIdentifier = visibleEventIdentifier;
        visibleEventIdentifier = newVisibleEventIdentifier;
        if ((visibleEventIdentifier != oldVisibleEventIdentifier)
                && ((visibleEventIdentifier == null) || (visibleEventIdentifier
                        .equals(oldVisibleEventIdentifier) == false))) {
            if (isNotOrigin(change)) {
                getView().getVisibleEventChanger().setState(null,
                        visibleEventIdentifier);
            }

            /*
             * If the event identifier is valid, update the event details.
             */
            if (selectedEventIdentifiers.contains(visibleEventIdentifier)) {
                updateViewVisibleEvent();
            }
        }
    }

    /**
     * Respond to the map of selected event identifiers to the events with which
     * they conflict having changed, or the auto-checking for conflicts having
     * been toggled.
     * 
     * @param change
     *            Change that occurred.
     */
    @Handler
    @Enveloped(messages = { SessionSelectedEventConflictsModified.class,
            SessionAutoCheckConflictsModified.class })
    public void sessionSelectedEventConflictsModified(
            final MessageEnvelope change) {
        if (detailViewShowing == false) {
            return;
        }

        /*
         * Rebuild the list of selected event displayables; if it has changed,
         * notify the view.
         */
        List<ObservedHazardEvent> selectedEvents = getModel().getEventManager()
                .getSelectedEvents();
        List<DisplayableEventIdentifier> selectedEventDisplayables = compileSelectedEventDisplayables(selectedEvents);
        if (this.selectedEventDisplayables.equals(selectedEventDisplayables) == false) {
            this.selectedEventDisplayables = selectedEventDisplayables;
            updateViewSelectedEvents();
        }
    }

    /**
     * Respond to an event's type having changed.
     * 
     * @param change
     *            Change that occurred.
     */
    @Handler
    public void sessionEventTypeModified(final SessionEventTypeModified change) {
        if (detailViewShowing == false) {
            return;
        }

        /*
         * Update the type in the view if this is not the source of the change,
         * and if the event having its type changed is currently visible.
         */
        if (isNotOrigin(change) && isVisibleEventModified(change)) {
            ObservedHazardEvent event = getVisibleEvent();
            if (event != null) {
                updateViewType(event);
            }
        }
    }

    /**
     * Respond to an event's time range having changed.
     * 
     * @param change
     *            Change that occurred.
     */
    @Handler
    public void sessionEventTimeRangeModified(
            final SessionEventTimeRangeModified change) {
        if (detailViewShowing && isNotOrigin(change)
                && isVisibleEventModified(change)) {
            ObservedHazardEvent event = getVisibleEvent();
            if (event != null) {
                updateViewTimeRange(event);
            }
        }
    }

    /**
     * Respond to an event's metadata megawidget specifiers having changed.
     * 
     * @param change
     *            Change that occurred.
     */
    @Handler
    public void sessionEventMetadataSpecifiersModified(
            final SessionEventMetadataModified change) {
        String eventIdentifier = change.getEvent().getEventID();
        ObservedHazardEvent event = getEventByIdentifier(eventIdentifier);
        if (event != null) {
            specifierManagersForSelectedEvents.remove(eventIdentifier);
            cacheMetadataSpecifiers(event);
            if (detailViewShowing && isVisibleEventModified(change)) {
                updateViewMetadataSpecifiers(event);
            }
        }
    }

    /**
     * Respond to an event's megawidgets' mutable properties changing.
     * 
     * @param change
     *            Changed mutable properties.
     */
    @Handler
    public void sessionEventScriptMutablePropertiesModified(
            final SessionEventScriptExtraDataAvailable change) {
        String eventIdentifier = change.getEvent().getEventID();
        ObservedHazardEvent event = getEventByIdentifier(eventIdentifier);
        if (event != null) {
            getView().getMetadataChanger().changeMegawidgetMutableProperties(
                    eventIdentifier, change.getMutableProperties());
        }
    }

    /**
     * Respond to the set of events allowing "until further notice" for their
     * end times having changed.
     * 
     * @param change
     *            Change that occurred.
     */
    @Handler
    public void sessionEventAllowUntilFurtherNoticeModified(
            final SessionEventAllowUntilFurtherNoticeModified change) {
        if (detailViewShowing && isVisibleEventModified(change)) {
            updateViewEndTimeUntilFurtherNoticeEnabled(change.getEvent());
        }
    }

    /**
     * Respond to an event's attributes having changed.
     * 
     * @param change
     *            Change that occurred.
     */
    @Handler
    public void sessionEventAttributesModified(
            final SessionEventAttributesModified change) {
        if (detailViewShowing && isNotOrigin(change)
                && isVisibleEventModified(change)) {
            ObservedHazardEvent event = getVisibleEvent();
            if (event != null) {
                if (change.getAttributeKeys().contains(
                        ISessionEventManager.ATTR_HAZARD_CATEGORY)) {
                    updateViewCategory(event);
                    updateViewTypeList(event);
                }
                updateViewMetadataValues(event, change.getAttributeKeys());
            }
        }
    }

    /**
     * Respond to an event's status having changed.
     * 
     * @param change
     *            Change that occurred.
     */
    @Handler
    public void sessionEventStatusModified(
            final SessionEventStatusModified change) {
        if (detailViewShowing) {
            if (isVisibleEventModified(change)) {
                updateViewCategoryEditability(getVisibleEvent());
            }
            updateViewButtonsEnabledStates();
        }
    }

    /**
     * Respond to the visible time range changing.
     * 
     * @param change
     *            Change that occurred.
     */
    @Handler
    public void visibleTimeRangeChanged(final VisibleTimeRangeChanged change) {
        if (detailViewShowing && isNotOrigin(change)) {
            updateViewVisibleTimeRange();
        }
    }

    /**
     * Show a view providing setting detail for the current hazard events.
     */
    public final void showHazardDetail() {
        showHazardDetail(false);
    }

    /**
     * Hide the hazard detail view.
     */
    public final void hideHazardDetail() {
        detailViewShowing = false;
        getView().getDetailViewVisibilityChanger().setState(null, false);
    }

    // Protected Methods

    @Override
    protected void initialize(IHazardDetailViewDelegate<?, ?> view) {

        /*
         * Initialize the view.
         */
        TimeRange timeRange = getModel().getTimeManager().getVisibleTimeRange();
        getView().initialize(categories, timeRange.getStart().getTime(),
                timeRange.getEnd().getTime(),
                getModel().getTimeManager().getCurrentTimeProvider(),
                extraDataForEvents);

        /*
         * Show the detail view if appropriate.
         */
        if (getModel().getEventManager().getSelectedEvents().size() > 0) {
            showHazardDetail(true);
        } else {
            hideHazardDetail();
        }

        /*
         * Register the various handlers with the view.
         */
        getView().getButtonInvoker().setCommandInvocationHandler(
                buttonInvocationHandler);
        getView().getCategoryChanger().setStateChangeHandler(
                categoryChangeHandler);
        getView().getTypeChanger().setStateChangeHandler(typeChangeHandler);
        getView().getDetailViewVisibilityChanger().setStateChangeHandler(
                detailViewVisibilityChangeHandler);
        getView().getMetadataChanger().setStateChangeHandler(
                metadataChangeHandler);
        getView().getNotifierInvoker().setCommandInvocationHandler(
                notifierInvocationHandler);
        getView().getTimeRangeChanger().setStateChangeHandler(
                timeRangeChangeHandler);
        getView().getVisibleEventChanger().setStateChangeHandler(
                visibleEventChangeHandler);
    }

    @Override
    protected void reinitialize(IHazardDetailViewDelegate<?, ?> view) {
        detailViewShowing = getView().getDetailViewVisibilityChanger()
                .getState(null);
    }

    // Private Methods

    /**
     * Show a view providing setting detail for the current hazard events.
     * 
     * @param force
     *            Flag indicating whether or not to force the showing of the
     *            hazard detail, even if it appears to already be showing.
     */
    private void showHazardDetail(boolean force) {

        /*
         * Do nothing unless there are events selected and the detail view is
         * currently hidden.
         */
        if (detailViewShowing && (force == false)) {
            return;
        }

        /*
         * Show the detail view.
         */
        getView().getDetailViewVisibilityChanger().setState(null, true);

        /*
         * Fill in all the information the view should display.
         */
        updateEntireView(getModel().getEventManager().getSelectedEvents());
    }

    /**
     * Determine the visible event identifier.
     * 
     * @return Last modified selected event, if found, or just the currently
     *         selected event otherwise.
     */
    private String getVisibleEventIdentifier() {
        String result = "";
        ObservedHazardEvent selectedEvent = getModel().getEventManager()
                .getLastModifiedSelectedEvent();
        if (selectedEvent != null) {
            result = selectedEvent.getEventID();
        }
        return result;
    }

    /**
     * Determine whether or not the currently visible event is the subject of
     * the specified modification.
     * 
     * @param change
     *            Modification that is to be checked.
     * @return True if the modification is of the currently visible event, false
     *         otherwise.
     */
    private boolean isVisibleEventModified(SessionEventModified change) {
        return change.getEvent().getEventID().equals(visibleEventIdentifier);
    }

    /**
     * Determine whether or not this presenter is the originator of the
     * specified modification.
     * 
     * @param change
     *            Modification that is to be checked.
     * @return True if the modification did not originate with this presenter,
     *         false otherwise.
     */
    private boolean isNotOrigin(OriginatedSessionNotification change) {
        return (change.getOriginator() != UIOriginator.HAZARD_INFORMATION_DIALOG);
    }

    /**
     * Compile the list of selected event identifiers.
     * 
     * @param selectedEvents
     *            Currently selected events.
     * @return List holding all the specified events' identifiers.
     */
    private List<String> compileSelectedEventIdentifiers(
            List<ObservedHazardEvent> selectedEvents) {
        if (selectedEvents != null) {
            List<String> list = new ArrayList<>(selectedEvents.size());
            for (ObservedHazardEvent event : selectedEvents) {
                list.add(event.getEventID());
            }
            return list;
        }
        return Collections.emptyList();
    }

    /**
     * Compile the list of selected event displayables.
     * 
     * @param selectedEvents
     *            Currently selected events.
     * @return List holding all the specified events' displayables.
     */
    private List<DisplayableEventIdentifier> compileSelectedEventDisplayables(
            List<ObservedHazardEvent> selectedEvents) {
        if (selectedEvents != null) {
            List<DisplayableEventIdentifier> list = new ArrayList<>(
                    selectedEvents.size());
            boolean showConflicts = getModel().isAutoHazardCheckingOn();
            for (ObservedHazardEvent event : selectedEvents) {
                String type = HazardEventUtilities.getHazardType(event);
                boolean conflict = false;
                if (showConflicts) {
                    Collection<IHazardEvent> conflictingEvents = conflictingEventsForSelectedEventIdentifiers
                            .get(event.getEventID());
                    conflict = ((conflictingEvents != null) && (conflictingEvents
                            .size() > 0));
                }
                list.add(new DisplayableEventIdentifier(event.getEventID()
                        + (type != null ? " " + type : ""), conflict));
            }
            return list;
        }
        return Collections.emptyList();
    }

    /**
     * Update the view to use the currently visible time range.
     */
    private void updateViewVisibleTimeRange() {
        TimeRange timeRange = getModel().getTimeManager().getVisibleTimeRange();
        getView().getVisibleTimeRangeChanger().setState(
                null,
                new TimeRange(timeRange.getStart().getTime(), timeRange
                        .getEnd().getTime()));
    }

    /**
     * Update the view to enable or disable the end time "until further notice"
     * toggle.
     */
    private void updateViewEndTimeUntilFurtherNoticeEnabled(
            ObservedHazardEvent event) {
        getView().getMetadataChanger().setEnabled(
                event.getEventID(),
                HazardConstants.HAZARD_EVENT_END_TIME_UNTIL_FURTHER_NOTICE,
                eventIdentifiersAllowingUntilFurtherNotice
                        .contains(visibleEventIdentifier));
    }

    /**
     * Update the view to show the currently selected events.
     */
    private void updateViewSelectedEvents() {
        getView().getVisibleEventChanger().setChoices(null,
                selectedEventIdentifiers, selectedEventDisplayables,
                visibleEventIdentifier);
    }

    /**
     * Update the view to use the current category for the selected event.
     * 
     * @param event
     *            Event for which the update should occur.
     */
    private void updateViewCategory(ObservedHazardEvent event) {
        selectedCategory = getModel().getConfigurationManager()
                .getHazardCategory(event);
        getView().getCategoryChanger().setState(event.getEventID(),
                selectedCategory);
    }

    /**
     * Update the view to have the specified editability for the category.
     * 
     * @param event
     *            Event for which the update should occur.
     */
    private void updateViewCategoryEditability(ObservedHazardEvent event) {
        HazardStatus status = event.getStatus();
        getView().getCategoryChanger().setEditable(event.getEventID(),
                !HazardStatus.hasEverBeenIssued(status));
    }

    /**
     * Update the view to use the type list and type that goes with the current
     * category for the specified event.
     * 
     * @param event
     *            Event for which the update should occur.
     */
    private void updateViewTypeList(ObservedHazardEvent event) {
        String selectedType = event.getHazardType();
        getView().getTypeChanger().setChoices(event.getEventID(),
                typeListsForCategories.get(selectedCategory),
                typeDescriptionListsForCategories.get(selectedCategory),
                (selectedType == null ? BLANK_TYPE_CHOICE : selectedType));
        updateViewDurations(event);
    }

    /**
     * Update the view to use the current type for the event.
     * 
     * @param event
     *            Event for which the update should occur.
     */
    private void updateViewType(ObservedHazardEvent event) {
        String selectedType = event.getHazardType();
        getView().getTypeChanger().setState(event.getEventID(),
                (selectedType == null ? BLANK_TYPE_CHOICE : selectedType));
        updateViewDurations(event);
    }

    /**
     * Update the view to use the current time range for the event.
     * 
     * @param event
     *            Event for which the update should occur.
     */
    private void updateViewTimeRange(ObservedHazardEvent event) {
        getView().getTimeRangeChanger().setState(
                event.getEventID(),
                new TimeRange(event.getStartTime().getTime(), event
                        .getEndTime().getTime()));
    }

    /**
     * Update the view to use the duration list goes with the current event.
     * 
     * @param event
     *            Event for which the update should occur.
     */
    private void updateViewDurations(ObservedHazardEvent event) {
        getView().getDurationChanger().setChoices(event.getEventID(),
                getModel().getConfigurationManager().getDurationChoices(event),
                null, null);
    }

    /**
     * Update the view to use the current metadata specifier manager.
     * 
     * @param event
     *            Event for which the update should occur.
     */
    private void updateViewMetadataSpecifiers(ObservedHazardEvent event) {
        getView().getMetadataChanger().setMegawidgetSpecifierManager(
                event.getEventID(),
                specifierManagersForSelectedEvents.get(visibleEventIdentifier),
                new HashMap<>(event.getHazardAttributes()));
    }

    /**
     * Update the view to use the current metadata values.
     * 
     * @param event
     *            Event for which the update should occur.
     * @param names
     *            Names of the attributes to be updated, or <code>null</code> if
     *            all attributes should be updated.
     */
    private void updateViewMetadataValues(ObservedHazardEvent event,
            Set<String> names) {
        getView().getMetadataChanger().setStates(
                event.getEventID(),
                new HashMap<>(names == null ? event.getHazardAttributes()
                        : Maps.filterKeys(event.getHazardAttributes(),
                                Predicates.in(names))));
    }

    /**
     * Ensure that the cache of metadata specifier managers contains one for the
     * specified event.
     * 
     * @param event
     *            Event for which to ensure the cache has a manager. It must be
     *            a valid event (i.e., not <code>null</code>).
     */
    private void cacheMetadataSpecifiers(ObservedHazardEvent event) {
        if (specifierManagersForSelectedEvents.containsKey(event.getEventID()) == false) {
            specifierManagersForSelectedEvents
                    .put(event.getEventID(), getModel().getEventManager()
                            .getMegawidgetSpecifiers(event));
        }
    }

    /**
     * Update the button states in the view.
     */
    private void updateViewButtonsEnabledStates() {

        /*
         * TODO: This logic determining which status changing actions are
         * available should probably be in the session event manager, since
         * other UI elements will need this same functionality (albeit perhaps
         * not making this determination only based on the visible event within
         * the selected events).
         */
        ObservedHazardEvent event = getVisibleEvent();
        boolean enable = ((event != null)
                && (HazardEventUtilities.getHazardType(event) != null)
                && (event.getStatus() != HazardStatus.ENDED)
                && (getModel().isPreviewOngoing() == false) && (getModel()
                .isIssueOngoing() == false));
        getView().getButtonInvoker().setEnabled(Command.PREVIEW, enable);
        getView().getButtonInvoker().setEnabled(
                Command.PROPOSE,
                (enable && getModel().getEventManager()
                        .getEventIdsAllowingProposal()
                        .contains(visibleEventIdentifier)));
        getView().getButtonInvoker().setEnabled(Command.ISSUE, enable);
    }

    /**
     * Update the view to display the currently visible event.
     */
    private void updateViewVisibleEvent() {
        ObservedHazardEvent event = getVisibleEvent();
        if (event != null) {
            cacheMetadataSpecifiers(event);
            updateViewVisibleTimeRange();
            updateViewCategory(event);
            updateViewCategoryEditability(event);
            updateViewTypeList(event);
            updateViewTimeRange(event);
            updateViewMetadataSpecifiers(event);
            updateViewEndTimeUntilFurtherNoticeEnabled(event);
        }
        updateViewButtonsEnabledStates();
    }

    /**
     * Update the entire detail view.
     * 
     * @param selectedEvents
     *            Selected events.
     */
    private void updateEntireView(List<ObservedHazardEvent> selectedEvents) {
        detailViewShowing = true;
        selectedEventIdentifiers = compileSelectedEventIdentifiers(selectedEvents);
        visibleEventIdentifier = getVisibleEventIdentifier();
        selectedEventDisplayables = compileSelectedEventDisplayables(selectedEvents);
        updateViewSelectedEvents();
        updateViewVisibleEvent();
    }

    /**
     * Get the currently visible event.
     * 
     * @return Currently visible event, or <code>null</code> if there is no
     *         visible event.
     */
    private ObservedHazardEvent getVisibleEvent() {
        return (visibleEventIdentifier == null ? null : getModel()
                .getEventManager().getEventById(visibleEventIdentifier));
    }

    /**
     * Get the specified event.
     * 
     * @param identifier
     *            Event identifier.
     * @return Event with the specified identifier, or <code>null</code> if
     *         there is no such event.
     */
    private ObservedHazardEvent getEventByIdentifier(String identifier) {
        return getModel().getEventManager().getEventById(identifier);
    }

    /**
     * Throw an unsupported operation exception for attempts to change multiple
     * states that are not appropriate.
     * 
     * @param description
     *            Description of the element for which an attempt to change
     *            multiple states was made.
     * @throw UnsupportedOperationException Whenever this method is called.
     */
    private void handleUnsupportedOperationAttempt(String description)
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException(
                "cannot change multiple states associated with detail view "
                        + description);
    }
}

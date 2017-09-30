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
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardStatus;
import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardEventUtilities;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEventParameterDescriber;
import com.raytheon.uf.common.dataplugin.events.hazards.event.collections.HazardHistoryList;
import com.raytheon.uf.common.hazards.configuration.types.HazardTypeEntry;
import com.raytheon.uf.common.time.TimeRange;
import com.raytheon.uf.common.util.Pair;
import com.raytheon.uf.viz.hazards.sessionmanager.ISessionManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.ObservedSettings;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Choice;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.HazardInfoConfig;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.StartUpConfig;
import com.raytheon.uf.viz.hazards.sessionmanager.events.AbstractSessionEventModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.EventAllowUntilFurtherNoticeModification;
import com.raytheon.uf.viz.hazards.sessionmanager.events.EventAttributesModification;
import com.raytheon.uf.viz.hazards.sessionmanager.events.EventMetadataModification;
import com.raytheon.uf.viz.hazards.sessionmanager.events.EventStatusModification;
import com.raytheon.uf.viz.hazards.sessionmanager.events.EventTimeRangeModification;
import com.raytheon.uf.viz.hazards.sessionmanager.events.EventTypeModification;
import com.raytheon.uf.viz.hazards.sessionmanager.events.IEventModification;
import com.raytheon.uf.viz.hazards.sessionmanager.events.ISessionEventManager;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionAutoCheckConflictsModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventScriptExtraDataAvailable;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventsRemoved;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventsTimeRangeBoundariesModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionLastAccessedEventModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionPreviewOrIssueOngoingModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionSelectedEventConflictsModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionSelectedEventsModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.impl.ObservedHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.OriginatedSessionNotification;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.Originator;
import com.raytheon.uf.viz.hazards.sessionmanager.time.VisibleTimeRangeChanged;

import gov.noaa.gsd.common.eventbus.BoundedReceptionEventBus;
import gov.noaa.gsd.common.utilities.Utils;
import gov.noaa.gsd.viz.hazards.UIOriginator;
import gov.noaa.gsd.viz.hazards.display.HazardServicesPresenter;
import gov.noaa.gsd.viz.hazards.hazarddetail.IHazardDetailView.TimeRangeBoundary;
import gov.noaa.gsd.viz.megawidgets.MegawidgetSpecifierManager;
import gov.noaa.gsd.viz.mvp.widgets.ICommandInvocationHandler;
import gov.noaa.gsd.viz.mvp.widgets.IQualifiedStateChangeHandler;
import gov.noaa.gsd.viz.mvp.widgets.IStateChangeHandler;
import net.engio.mbassy.listener.Enveloped;
import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.subscription.MessageEnvelope;

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
 * Dec 13, 2014    4959    Dan Schaffer      Spatial Display cleanup and other bug fixes
 * Jan 08, 2015    5700    Chris.Golden      Changed to generalize the meaning of a
 *                                           command invocation for a particular
 *                                           event, since it no longer only means
 *                                           that an event-modifying script is to be
 *                                           executed; it may instead trigger a
 *                                           metadata refresh.
 * Feb 03, 2015    2331    Chris.Golden      Added support for limiting the values that
 *                                           an event's start or end time can take on.
 * Feb 10, 2015    6393    Chris.Golden      Added use of hazard event parameter
 *                                           describers in descriptive text for each
 *                                           hazard event, so that the elements of each
 *                                           hazard event shown in the tab text may be
 *                                           localized.
 * Mar 06, 2015    3850    Chris.Golden      Added code to make the category and type
 *                                           lists change according to whether the
 *                                           event being shown has a point ID (if not
 *                                           yet issued), or what it can be replaced
 *                                           by (if issued).
 * Apr 09, 2015    7382    Chris.Golden      Added "show start-end time sliders" flag.
 * Apr 15, 2015    3508    Chris.Golden      Added "hazard detail to be wide" flag.
 * May 20, 2015    8192    Chris.Cody        Set HID Durations for new Events on type selection
 * Jun 26, 2015    7919    Robert.Blum       Enabled Issue/Preview button for Ended hazards.
 * Jul 29, 2015    9306    Chris.Cody        Add processing for HazardSatus.ELAPSED
 * Aug 19, 2015    4245    Chris.Golden      Added ability to change visible time range from
 *                                           within the associated view.
 * Oct 04, 2016   22736    Chris.Golden      Added flag indicating whether or not metadata
 *                                           has its interdependency script reinitialize if
 *                                           unchanged, so that when a hazard event is
 *                                           selected, it triggers the reinitialization.
 * Oct 19, 2016   21873    Chris.Golden      Added time resolution tracking tied to events.
 * Dec 16, 2016   27006    bkowal            Adjust the available event types when an event
 *                                           is a replacement for another event.
 * Feb 01, 2017   15556    Chris.Golden      Changed to work with new selection manager.
 *                                           Added awareness of selection of historical
 *                                           snapshots of events, so that the view may
 *                                           display such snapshots differently from the
 *                                           way it displays current events.
 * Apr 20, 2017   33376    Chris.Golden      Fixed bug causing the Until Further Notice
 *                                           checkbox to always be disabled, even when an
 *                                           event that allows UFN is being displayed in
 *                                           the HID. Also fixed similar cause of a bug
 *                                           that made the Propose button never enabled
 *                                           event when it should have been.
 * Apr 27, 2017   11866    Chris.Golden      Added code to prevent command buttons from
 *                                           being enabled when the event is ended or
 *                                           elapsed.
 * Jun 27, 2017   20347    Chris.Golden      Hazard detail view now pops up if a hazard
 *                                           event is changed to status ending and it is
 *                                           selected.
 * Sep 27, 2017   38072    Chris.Golden      Changed to use new SessionEventModified
 *                                           notification. Also moved to Java 8 streams.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class HazardDetailPresenter
        extends HazardServicesPresenter<IHazardDetailViewDelegate<?, ?>> {

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

        /**
         * Timestamp indicating when the event was persisted to the database, or
         * <code>null</code> if the event is not a historical snapshot.
         */
        private final Date persistTimestamp;

        // Private Constructors

        /**
         * Construct a standard instance.
         * 
         * @param description
         *            Description of the event.
         * @param conflicting
         *            Flag indicating whether or not the event has a conflict
         *            with at least one other event.
         * @param persistTimestamp
         *            Timestamp indicating when the event was persisted to the
         *            database, or <code>null</code> if the event is not a
         *            historical snapshot.
         */
        private DisplayableEventIdentifier(String description,
                boolean conflicting, Date persistTimestamp) {
            this.description = description;
            this.conflicting = conflicting;
            this.persistTimestamp = persistTimestamp;
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
         * @return <code>true</code> if the event has a conflict with at least
         *         one other event, <code>false</code> otherwise.
         */
        public final boolean isConflicting() {
            return conflicting;
        }

        /**
         * Get the timestamp indicating when the event was persisted to the
         * database, if it is a historical snapshot.
         * 
         * @return Persist timestamp, or <code>null</code> if it is not a
         *         historical snapshot.
         */
        public final Date getPersistTimestamp() {
            return persistTimestamp;
        }
    }

    // Private Enumerated Types

    /**
     * Elements of the view.
     */
    private enum ViewElement {
        CATEGORY_EDITABILITY, CATEGORY_VALUE, TYPE_LIST, TYPE_EDITABILITY, TYPE_VALUE, TIME_RANGE, END_TIME_UNTIL_FURTHER_NOTICE_ENABLED, METADATA_SPECIFIERS, METADATA_VALUES, SELECTED_EVENT_DISPLAYABLES, BUTTONS_ENABLED
    };

    // Private Variables

    /**
     * Hazard detail handler.
     */
    private final IHazardDetailHandler hazardDetailHandler;

    /**
     * Flag indicating whether or not the detail view is showing.
     */
    private boolean detailViewShowing;

    /**
     * List of selected event version identifiers.
     */
    private List<Pair<String, Integer>> selectedEventVersionIdentifiers;

    /**
     * List of selected event displayable elements; each one goes with the event
     * version identifier from {@link #selectedEventVersionIdentifiers} at the
     * corresponding index.
     */
    private List<DisplayableEventIdentifier> selectedEventDisplayables;

    /**
     * Currently visible event version identifier within the
     * {@link #selectedEventVersionIdentifiers}.
     */
    private Pair<String, Integer> visibleEventVersionIdentifier;

    /**
     * List of hazard category identifiers.
     */
    private final ImmutableList<String> categories;

    /**
     * List of hazard category identifiers containing at least one type that
     * does not require a point identifier.
     */
    private final ImmutableList<String> categoriesNoPointIdRequired;

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
     * Map pairing categories with lists of hazard type identifiers that do not
     * require a point identifier.
     */
    private final ImmutableMap<String, ImmutableList<String>> typeListsForCategoriesNoPointIdRequired;

    /**
     * Map pairing categories with lists of hazard type descriptions that do not
     * require a point identifier.
     */
    private final ImmutableMap<String, ImmutableList<String>> typeDescriptionListsForCategoriesNoPointIdRequired;

    /**
     * Map pairing hazard type identifiers with their descriptions.
     */
    private final ImmutableMap<String, String> descriptionsForTypes;

    /**
     * Map of event version identifiers to their megawidget specifier managers.
     */
    private final Map<Pair<String, Integer>, MegawidgetSpecifierManager> specifierManagersForSelectedEventVersions = new HashMap<>();

    /**
     * <p>
     * Map of event version identifiers to maps holding extra data for the
     * associated metadata megawidgets. Only the most recently used extra data
     * maps are cached away; the maximum number that can be cached is
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
    private final Map<Pair<String, Integer>, Map<String, Map<String, Object>>> extraDataForEventVersionIdentifiers = new LinkedHashMap<Pair<String, Integer>, Map<String, Map<String, Object>>>(
            MAXIMUM_EVENT_EXTRA_DATA_CACHE_SIZE + 1, 0.75f, true) {

        // Private Static Constants

        /**
         * Serial version UID.
         */
        private static final long serialVersionUID = 1L;

        // Protected Methods

        @Override
        protected final boolean removeEldestEntry(
                Map.Entry<Pair<String, Integer>, Map<String, Map<String, Object>>> eldest) {
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
     * Tab text describers, used to generate the title text for a particular
     * hazard event's tab.
     */
    private List<IHazardEventParameterDescriber> tabTextDescribers;

    /**
     * Detail view visibility change handler. The identifier is ignored.
     */
    private final IStateChangeHandler<String, Boolean> detailViewVisibilityChangeHandler = new IStateChangeHandler<String, Boolean>() {

        @Override
        public void stateChanged(String identifier, Boolean value) {
            if (Boolean.TRUE.equals(value)) {
                if (detailViewShowing == false) {
                    updateEntireView();
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
    private final IStateChangeHandler<String, Pair<String, Integer>> visibleEventChangeHandler = new IStateChangeHandler<String, Pair<String, Integer>>() {

        @Override
        public void stateChanged(String identifier,
                Pair<String, Integer> value) {
            visibleEventVersionIdentifier = value;
            getModel().getSelectionManager()
                    .setLastAccessedSelectedEventVersion(
                            visibleEventVersionIdentifier,
                            UIOriginator.HAZARD_INFORMATION_DIALOG);
            if (selectedEventVersionIdentifiers
                    .contains(visibleEventVersionIdentifier)) {
                updateViewVisibleEvent(false);
            }
        }

        @Override
        public void statesChanged(
                Map<String, Pair<String, Integer>> valuesForIdentifiers) {
            handleUnsupportedOperationAttempt("visible event");
        }
    };

    /**
     * Visible time range change handler. The identifier is ignored.
     */
    private final IStateChangeHandler<String, TimeRange> visibleTimeRangeChangeHandler = new IStateChangeHandler<String, TimeRange>() {

        @Override
        public void stateChanged(String identifier, TimeRange value) {
            getModel().getTimeManager().setVisibleTimeRange(value,
                    UIOriginator.HAZARD_INFORMATION_DIALOG);
        }

        @Override
        public void statesChanged(Map<String, TimeRange> valuesForIdentifiers) {
            handleUnsupportedOperationAttempt("visible time range");
        }
    };

    /**
     * Category state change handler. The identifier is that of the changed
     * event version.
     */
    private final IStateChangeHandler<Pair<String, Integer>, String> categoryChangeHandler = new IStateChangeHandler<Pair<String, Integer>, String>() {

        @Override
        public void stateChanged(Pair<String, Integer> identifier,
                String value) {
            ensureNotHistorical(identifier);
            selectedCategory = value;
            ObservedHazardEvent event = getEventByIdentifier(
                    identifier.getFirst());
            if (event != null) {
                getModel().getEventManager().setEventCategory(event,
                        selectedCategory,
                        UIOriginator.HAZARD_INFORMATION_DIALOG);
                updateSelectedEventDisplayablesIfChanged();
                if (identifier.equals(visibleEventVersionIdentifier)) {
                    updateViewTypeList(event, identifier);
                }
            }
        }

        @Override
        public void statesChanged(
                Map<Pair<String, Integer>, String> valuesForIdentifiers) {
            handleUnsupportedOperationAttempt("category");
        }
    };

    /**
     * Type state change handler. The identifier is that of the changed event
     * version.
     */
    private final IStateChangeHandler<Pair<String, Integer>, String> typeChangeHandler = new IStateChangeHandler<Pair<String, Integer>, String>() {

        @Override
        public void stateChanged(Pair<String, Integer> identifier,
                String value) {

            /*
             * Ensure that the event is around.
             */
            ensureNotHistorical(identifier);
            ObservedHazardEvent event = getEventByIdentifier(
                    identifier.getFirst());
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
            List<Pair<String, Integer>> selectedEventVersionIdentifiers = getModel()
                    .getSelectionManager()
                    .getSelectedEventVersionIdentifiersList();
            if (selectedEventVersionIdentifiers.equals(
                    HazardDetailPresenter.this.selectedEventVersionIdentifiers)) {
                selectedEventDisplayables = compileSelectedEventDisplayables();
                updateViewSelectedEvents();
                updateViewDurations(event, visibleEventVersionIdentifier);
                updateViewButtonsEnabledStates();
            }
        }

        @Override
        public void statesChanged(
                Map<Pair<String, Integer>, String> valuesForIdentifiers) {
            handleUnsupportedOperationAttempt("type");
        }
    };

    /**
     * Time range state change handler. The identifier is that of the changed
     * event version.
     */
    private final IStateChangeHandler<Pair<String, Integer>, TimeRange> timeRangeChangeHandler = new IStateChangeHandler<Pair<String, Integer>, TimeRange>() {

        @Override
        public void stateChanged(Pair<String, Integer> identifier,
                TimeRange value) {

            /*
             * Do nothing unless the event is around. If it is, but the setting
             * of its time range fails, it is because the time range would have
             * violated the start and/or end time boundaries; in that case,
             * notify the view of the reset to the original values.
             */
            ensureNotHistorical(identifier);
            ObservedHazardEvent event = getEventByIdentifier(
                    identifier.getFirst());
            if ((event != null)
                    && (getModel().getEventManager().setEventTimeRange(event,
                            new Date(value.getStart().getTime()),
                            new Date(value.getEnd().getTime()),
                            UIOriginator.HAZARD_INFORMATION_DIALOG) == false)) {
                updateViewTimeRange(event, identifier);
            }
        }

        @Override
        public void statesChanged(
                Map<Pair<String, Integer>, TimeRange> valuesForIdentifiers) {
            handleUnsupportedOperationAttempt("time range");
        }
    };

    /**
     * Metadata state change handler. The qualifier is the identifier of the
     * changed event version, while the identifier is that of the metadata that
     * changed.
     */
    private final IQualifiedStateChangeHandler<Pair<String, Integer>, String, Serializable> metadataChangeHandler = new IQualifiedStateChangeHandler<Pair<String, Integer>, String, Serializable>() {

        @Override
        public void stateChanged(Pair<String, Integer> qualifier,
                String identifier, Serializable value) {
            ensureNotHistorical(qualifier);
            ObservedHazardEvent event = getEventByIdentifier(
                    qualifier.getFirst());
            if (event != null) {
                event.addHazardAttribute(identifier, value,
                        UIOriginator.HAZARD_INFORMATION_DIALOG);
                updateSelectedEventDisplayablesIfChanged();
            }
        }

        @Override
        public void statesChanged(Pair<String, Integer> qualifier,
                Map<String, Serializable> valuesForIdentifiers) {
            ensureNotHistorical(qualifier);
            ObservedHazardEvent event = getEventByIdentifier(
                    qualifier.getFirst());
            if (event != null) {
                event.addHazardAttributes(valuesForIdentifiers,
                        UIOriginator.HAZARD_INFORMATION_DIALOG);
                updateSelectedEventDisplayablesIfChanged();
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
            ObservedHazardEvent event = getEventByIdentifier(
                    identifier.getEventIdentifier());
            if (event != null) {
                getModel().eventCommandInvoked(event,
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
            if (identifier == Command.PREVIEW) {
                preview();
            } else if (identifier == Command.PROPOSE) {
                propose();
            } else if (identifier == Command.ISSUE) {
                issue();
            }
        }
    };

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param model
     *            Model to be handled by this presenter.
     * @param hazardDetailHandler
     *            Handler to be notified when the hazard detail presenter needs
     *            to interact with other UI elements.
     * @param eventBus
     *            Event bus used to signal changes.
     */
    public HazardDetailPresenter(
            ISessionManager<ObservedHazardEvent, ObservedSettings> model,
            IHazardDetailHandler hazardDetailHandler,
            BoundedReceptionEventBus<Object> eventBus) {
        super(model, eventBus);
        this.hazardDetailHandler = hazardDetailHandler;
        selectedEventVersionIdentifiers = new ArrayList<>(
                model.getSelectionManager()
                        .getSelectedEventVersionIdentifiersList());
        selectedEventDisplayables = compileSelectedEventDisplayables();
        eventIdentifiersAllowingUntilFurtherNotice = model.getEventManager()
                .getEventIdsAllowingUntilFurtherNotice();
        conflictingEventsForSelectedEventIdentifiers = model.getEventManager()
                .getConflictingEventsForSelectedEvents();

        /*
         * Get the hazard categories list, and a map of those categories to
         * their type lists, as well as a a similar map to their type
         * displayable lists. The lists in the maps each start with an empty
         * string, used to indicate that no type has been chosen. And do all the
         * same work, but this time generating a category list and the two maps
         * only for those categories and types not requiring point identifiers.
         */
        List<String> categories = new ArrayList<>();
        List<String> categoriesNoPointIdRequired = new ArrayList<>();
        Map<String, ImmutableList<String>> typesForCategories = new HashMap<>();
        Map<String, ImmutableList<String>> typeDescriptionsForCategories = new HashMap<>();
        Map<String, ImmutableList<String>> typesForCategoriesNoPointIdRequired = new HashMap<>();
        Map<String, ImmutableList<String>> typeDescriptionsForCategoriesNoPointIdRequired = new HashMap<>();
        Map<String, String> descriptionsForTypes = new HashMap<>();
        HazardInfoConfig categoriesAndTypes = getModel()
                .getConfigurationManager().getHazardInfoConfig();
        for (Choice categoryAndTypes : categoriesAndTypes
                .getHazardCategories()) {
            List<Choice> typeChoices = categoryAndTypes.getChildren();
            List<String> types = new ArrayList<String>(typeChoices.size() + 1);
            List<String> typeDescriptions = new ArrayList<String>(
                    typeChoices.size() + 1);
            List<String> typesNoPointIdRequired = new ArrayList<String>(
                    typeChoices.size() + 1);
            List<String> typeDescriptionsNoPointIdRequired = new ArrayList<String>(
                    typeChoices.size() + 1);
            for (Choice hazardType : categoryAndTypes.getChildren()) {
                types.add(hazardType.getIdentifier());
                typeDescriptions.add(hazardType.getDisplayString());
                descriptionsForTypes.put(hazardType.getIdentifier(),
                        hazardType.getDisplayString());
                if (getModel().getConfigurationManager()
                        .isPointIdentifierRequired(
                                hazardType.getIdentifier()) == false) {
                    typesNoPointIdRequired.add(hazardType.getIdentifier());
                    typeDescriptionsNoPointIdRequired
                            .add(hazardType.getDisplayString());
                }
            }
            if (types.isEmpty() == false) {
                types.add(0, BLANK_TYPE_CHOICE);
                typeDescriptions.add(0, BLANK_TYPE_CHOICE);
                typesForCategories.put(categoryAndTypes.getDisplayString(),
                        ImmutableList.copyOf(types));
                typeDescriptionsForCategories.put(
                        categoryAndTypes.getDisplayString(),
                        ImmutableList.copyOf(typeDescriptions));
                categories.add(categoryAndTypes.getDisplayString());
            }
            if (typesNoPointIdRequired.isEmpty() == false) {
                typesNoPointIdRequired.add(0, BLANK_TYPE_CHOICE);
                typeDescriptionsNoPointIdRequired.add(0, BLANK_TYPE_CHOICE);
                typesForCategoriesNoPointIdRequired.put(
                        categoryAndTypes.getDisplayString(),
                        ImmutableList.copyOf(typesNoPointIdRequired));
                typeDescriptionsForCategoriesNoPointIdRequired
                        .put(categoryAndTypes.getDisplayString(), ImmutableList
                                .copyOf(typeDescriptionsNoPointIdRequired));
                categoriesNoPointIdRequired
                        .add(categoryAndTypes.getDisplayString());
            }
        }
        this.categories = ImmutableList.copyOf(categories);
        this.typeListsForCategories = ImmutableMap.copyOf(typesForCategories);
        this.typeDescriptionListsForCategories = ImmutableMap
                .copyOf(typeDescriptionsForCategories);
        this.categoriesNoPointIdRequired = ImmutableList
                .copyOf(categoriesNoPointIdRequired);
        this.typeListsForCategoriesNoPointIdRequired = ImmutableMap
                .copyOf(typesForCategoriesNoPointIdRequired);
        this.typeDescriptionListsForCategoriesNoPointIdRequired = ImmutableMap
                .copyOf(typeDescriptionsForCategoriesNoPointIdRequired);
        this.descriptionsForTypes = ImmutableMap.copyOf(descriptionsForTypes);
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
     * Respond to session preview or issue ongoing modifications.
     * 
     * @param change
     *            Change that occurred.
     */
    @Handler
    public void sessionPreviewOrIssueOngoingModified(
            SessionPreviewOrIssueOngoingModified change) {
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
    public void sessionEventsRemoved(SessionEventsRemoved change) {

        /*
         * Remove any cached specifier managers associated with versions of any
         * of the events that have been removed.
         */
        Set<String> identifiers = change.getEvents().stream()
                .map(IHazardEvent::getEventID).collect(Collectors.toSet());
        specifierManagersForSelectedEventVersions.entrySet().removeIf(
                entry -> identifiers.contains(entry.getKey().getFirst()));
    }

    /**
     * Respond to the list of selected events possibly having changed.
     * 
     * @param change
     *            Change that occurred.
     */
    @Handler
    public void sessionSelectedEventsModified(
            SessionSelectedEventsModified change) {

        /*
         * Get the selected event versions' identifiers as a list.
         */
        List<Pair<String, Integer>> selectedEventVersionIdentifiers = getModel()
                .getSelectionManager().getSelectedEventVersionIdentifiersList();

        /*
         * If the detail view is not showing and there are selected events, show
         * it. In this case nothing more needs to be done, since part of the
         * showing process is updating it to include the correct information.
         */
        if ((selectedEventVersionIdentifiers.size() > 0)
                && (detailViewShowing == false)) {
            showHazardDetail();
            return;
        }

        /*
         * If the list is different from the previously-current one, make it the
         * current one.
         */
        if (selectedEventVersionIdentifiers
                .equals(this.selectedEventVersionIdentifiers) == false) {

            /*
             * Remember the new selected events list, and get the visible event.
             */
            this.selectedEventVersionIdentifiers = new ArrayList<>(
                    selectedEventVersionIdentifiers);
            Pair<String, Integer> oldVisibleEventVersionIdentifier = visibleEventVersionIdentifier;
            visibleEventVersionIdentifier = getVisibleEventVersionIdentifier();

            /*
             * Notify the view of the new list of selected events.
             */
            selectedEventDisplayables = compileSelectedEventDisplayables();
            if (detailViewShowing) {
                updateViewSelectedEvents();
            }

            /*
             * Send on all details about the event to the view if the visible
             * event has changed.
             */
            if (detailViewShowing && (Utils.equal(visibleEventVersionIdentifier,
                    oldVisibleEventVersionIdentifier) == false)) {
                updateViewVisibleEvent(true);
            }
        }

        /*
         * If there are now no selected events and the view is showing, hide it.
         */
        if (selectedEventVersionIdentifiers.isEmpty() && detailViewShowing) {
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
    public void sessionLastAccessedSelectedEventModified(
            SessionLastAccessedEventModified change) {

        /*
         * Do nothing if the view is not showing, or if the selected event
         * identifiers do not contain the new visible event identifier.
         */
        if (detailViewShowing == false) {
            return;
        }
        Pair<String, Integer> newVisibleEventVersionIdentifier = getVisibleEventVersionIdentifier();
        if (selectedEventVersionIdentifiers
                .contains(newVisibleEventVersionIdentifier) == false) {
            return;
        }

        /*
         * If the last selected event identifier is not the same as it was
         * before, update the view to match if the view was not the originator
         * of the change.
         */
        Pair<String, Integer> oldVisibleEventVersionIdentifier = visibleEventVersionIdentifier;
        visibleEventVersionIdentifier = newVisibleEventVersionIdentifier;
        if (Utils.equal(visibleEventVersionIdentifier,
                oldVisibleEventVersionIdentifier) == false) {
            if (isNotOrigin(change)) {
                getView().getVisibleEventChanger().setState(null,
                        visibleEventVersionIdentifier);
            }

            /*
             * If the event version identifier is valid, update the event
             * details.
             */
            if (selectedEventVersionIdentifiers
                    .contains(visibleEventVersionIdentifier)) {
                updateViewVisibleEvent(false);
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
    public void sessionSelectedEventConflictsModified(MessageEnvelope change) {
        if (detailViewShowing == false) {
            return;
        }
        updateSelectedEventDisplayablesIfChanged();
    }

    /**
     * Respond to an event having changed.
     * 
     * @param change
     *            Change that occurred.
     */
    @Handler
    public void sessionEventModified(SessionEventModified change) {

        /*
         * Create a set of view elements to be used to track which elements have
         * been updated already, in order to allow methods invoked here to not
         * re-update something that has already been updated.
         */
        EnumSet<ViewElement> updatedElements = EnumSet
                .noneOf(ViewElement.class);

        /*
         * Since a status update for the event might result in the dialog being
         * shown, check for any such updates first.
         */
        if (change.getClassesOfModifications()
                .contains(EventStatusModification.class)) {
            sessionEventStatusModified(change, updatedElements);
        }

        /*
         * Iterate through the modifications, reacting to any that will result
         * in a potential view change by updating the appropriate parts of the
         * view.
         */
        for (IEventModification modification : change.getModifications()) {
            if (modification instanceof EventTypeModification) {
                sessionEventTypeModified(change, updatedElements);
            } else if (modification instanceof EventTimeRangeModification) {
                sessionEventTimeRangeModified(change, updatedElements);
            } else if (modification instanceof EventMetadataModification) {
                sessionEventMetadataSpecifiersModified(change, updatedElements);
            } else if (modification instanceof EventAllowUntilFurtherNoticeModification) {
                sessionEventAllowUntilFurtherNoticeModified(change,
                        updatedElements);
            } else if (modification instanceof EventAttributesModification) {
                sessionEventAttributesModified(change,
                        (EventAttributesModification) modification,
                        updatedElements);
            }
        }
    }

    /**
     * Respond to an event's time range boundaries having changed.
     * 
     * @param change
     *            Change that occurred.
     */
    @Handler
    public void sessionEventTimeRangeBoundariesModified(
            SessionEventsTimeRangeBoundariesModified change) {
        if (detailViewShowing && isVisibleEventVersionInCollection(
                change.getEventIdentifiers())) {
            IHazardEvent event = getVisibleEvent();
            if (event != null) {
                updateViewTimeRangeBoundaries(event,
                        visibleEventVersionIdentifier);
                updateViewDurations(event, visibleEventVersionIdentifier);
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
            SessionEventScriptExtraDataAvailable change) {
        String eventIdentifier = change.getEvent().getEventID();
        ObservedHazardEvent event = getEventByIdentifier(eventIdentifier);
        if (event != null) {
            getView().getMetadataChanger().changeMegawidgetMutableProperties(
                    new Pair<String, Integer>(eventIdentifier, null),
                    change.getMutableProperties());
        }
    }

    /**
     * Respond to the visible time range changing.
     * 
     * @param change
     *            Change that occurred.
     */
    @Handler
    public void visibleTimeRangeChanged(VisibleTimeRangeChanged change) {
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
         * Get the list of hazard event parameter describers that will be used
         * to generate tab title text, determine whether or not the start-end
         * time UI elements should show a scale bar, and determine whether or
         * not the view is to be built to be optimized for wide viewing.
         */
        StartUpConfig startUpConfig = getModel().getConfigurationManager()
                .getStartUpConfig();
        String[] tabText = (startUpConfig != null
                ? startUpConfig.getHazardDetailTabText() : null);
        tabTextDescribers = HazardEventUtilities
                .getHazardParameterDescribers(tabText);
        boolean showStartEndTimeScale = startUpConfig
                .isShowingHazardDetailStartEndTimeScale();
        boolean buildForWideViewing = startUpConfig.isHazardDetailToBeWide();
        boolean includeIssueButton = startUpConfig.includeIssueButton();

        /*
         * Initialize the view.
         */
        TimeRange timeRange = getModel().getTimeManager().getVisibleTimeRange();
        getView().initialize(timeRange.getStart().getTime(),
                timeRange.getEnd().getTime(),
                getModel().getTimeManager().getCurrentTimeProvider(),
                showStartEndTimeScale, buildForWideViewing, includeIssueButton,
                extraDataForEventVersionIdentifiers);

        /*
         * Show the detail view if appropriate.
         */
        if (getModel().getSelectionManager().getSelectedEvents()
                .isEmpty() == false) {
            showHazardDetail(true);
        } else {
            hideHazardDetail();
        }

        /*
         * Register the various handlers with the view.
         */
        getView().getButtonInvoker()
                .setCommandInvocationHandler(buttonInvocationHandler);
        getView().getVisibleTimeRangeChanger()
                .setStateChangeHandler(visibleTimeRangeChangeHandler);
        getView().getCategoryChanger()
                .setStateChangeHandler(categoryChangeHandler);
        getView().getTypeChanger().setStateChangeHandler(typeChangeHandler);
        getView().getDetailViewVisibilityChanger()
                .setStateChangeHandler(detailViewVisibilityChangeHandler);
        getView().getMetadataChanger()
                .setStateChangeHandler(metadataChangeHandler);
        getView().getNotifierInvoker()
                .setCommandInvocationHandler(notifierInvocationHandler);
        getView().getTimeRangeChanger()
                .setStateChangeHandler(timeRangeChangeHandler);
        getView().getVisibleEventChanger()
                .setStateChangeHandler(visibleEventChangeHandler);
    }

    @Override
    protected void reinitialize(IHazardDetailViewDelegate<?, ?> view) {
        detailViewShowing = getView().getDetailViewVisibilityChanger()
                .getState(null);
    }

    // Private Methods

    /**
     * Respond to an event's type having changed.
     * 
     * @param change
     *            Change that encompasses this modification.
     * @param updatedElements
     *            Set of view elements that have already been updated. This set
     *            will have any view elements added to it by this method that
     *            are updated as a result of this invocation.
     */
    private void sessionEventTypeModified(SessionEventModified change,
            Set<ViewElement> updatedElements) {
        if (detailViewShowing == false) {
            return;
        }

        /*
         * Update the type in the view if this is not the source of the change,
         * and if the event having its type changed is currently visible.
         */
        if (isNotOrigin(change) && isVisibleEventVersionModified(change)) {
            IHazardEvent event = getVisibleEvent();
            if ((event != null) && (updatedElements
                    .contains(ViewElement.TYPE_VALUE) == false)) {
                updateViewType(event, visibleEventVersionIdentifier);
                updatedElements.add(ViewElement.TYPE_VALUE);
            }
            if (updatedElements.contains(
                    ViewElement.SELECTED_EVENT_DISPLAYABLES) == false) {
                updateSelectedEventDisplayablesIfChanged();
                updatedElements.add(ViewElement.SELECTED_EVENT_DISPLAYABLES);
            }
        }
    }

    /**
     * Respond to an event's time range having changed.
     * 
     * @param change
     *            Change that encompasses this modification.
     * @param updatedElements
     *            Set of view elements that have already been updated. This set
     *            will have any view elements added to it by this method that
     *            are updated as a result of this invocation.
     */
    private void sessionEventTimeRangeModified(SessionEventModified change,
            Set<ViewElement> updatedElements) {
        if (detailViewShowing && isNotOrigin(change)
                && isVisibleEventVersionModified(change)) {
            IHazardEvent event = getVisibleEvent();
            if ((event != null) && (updatedElements
                    .contains(ViewElement.TIME_RANGE) == false)) {
                updateViewTimeRange(event, visibleEventVersionIdentifier);
                updatedElements.add(ViewElement.TIME_RANGE);
            }
        }
    }

    /**
     * Respond to an event's metadata megawidget specifiers having changed.
     * 
     * @param change
     *            Change that encompasses this modification.
     * @param updatedElements
     *            Set of view elements that have already been updated. This set
     *            will have any view elements added to it by this method that
     *            are updated as a result of this invocation.
     */
    private void sessionEventMetadataSpecifiersModified(
            SessionEventModified change, Set<ViewElement> updatedElements) {
        String eventIdentifier = change.getEvent().getEventID();
        ObservedHazardEvent event = getEventByIdentifier(eventIdentifier);
        if ((event != null) && (updatedElements
                .contains(ViewElement.METADATA_SPECIFIERS) == false)) {
            Pair<String, Integer> identifier = new Pair<>(eventIdentifier,
                    null);
            specifierManagersForSelectedEventVersions.remove(identifier);
            cacheMetadataSpecifiers(event, identifier);
            if (detailViewShowing && isVisibleEventVersionModified(change)) {
                updateViewMetadataSpecifiers(event, identifier, false);
            }
            updatedElements.add(ViewElement.METADATA_SPECIFIERS);
        }
    }

    /**
     * Respond to the set of events allowing "until further notice" for their
     * end times having changed.
     * 
     * @param change
     *            Change that encompasses this modification.
     * @param updatedElements
     *            Set of view elements that have already been updated. This set
     *            will have any view elements added to it by this method that
     *            are updated as a result of this invocation.
     */
    private void sessionEventAllowUntilFurtherNoticeModified(
            SessionEventModified change, Set<ViewElement> updatedElements) {
        if (detailViewShowing && isVisibleEventVersionModified(change)
                && (updatedElements.contains(
                        ViewElement.END_TIME_UNTIL_FURTHER_NOTICE_ENABLED) == false)) {
            updateViewEndTimeUntilFurtherNoticeEnabled(change.getEvent(),
                    visibleEventVersionIdentifier);
            updatedElements
                    .add(ViewElement.END_TIME_UNTIL_FURTHER_NOTICE_ENABLED);
        }
    }

    /**
     * Respond to an event's status having changed.
     * 
     * @param change
     *            Change that encompasses this modification.
     * @param updatedElements
     *            Set of view elements that have already been updated. This set
     *            will have any view elements added to it by this method that
     *            are updated as a result of this invocation.
     */
    private void sessionEventStatusModified(SessionEventModified change,
            Set<ViewElement> updatedElements) {
        if (detailViewShowing) {
            if (isVisibleEventVersionModified(change)) {
                IHazardEvent event = getVisibleEvent();
                if (updatedElements
                        .contains(ViewElement.CATEGORY_EDITABILITY) == false) {
                    updateViewCategoryEditability(event,
                            visibleEventVersionIdentifier);
                    updatedElements.add(ViewElement.CATEGORY_EDITABILITY);
                }
                if (updatedElements.contains(ViewElement.TYPE_LIST) == false) {
                    updateViewTypeList(event, visibleEventVersionIdentifier);
                    updatedElements.add(ViewElement.TYPE_LIST);
                }
                if (updatedElements
                        .contains(ViewElement.TYPE_EDITABILITY) == false) {
                    updateViewTypeEditability(event,
                            visibleEventVersionIdentifier);
                    updatedElements.add(ViewElement.TYPE_EDITABILITY);
                }
            }
            if (updatedElements.contains(
                    ViewElement.SELECTED_EVENT_DISPLAYABLES) == false) {
                updateSelectedEventDisplayablesIfChanged();
                updatedElements.add(ViewElement.SELECTED_EVENT_DISPLAYABLES);
            }
            if (updatedElements
                    .contains(ViewElement.BUTTONS_ENABLED) == false) {
                updateViewButtonsEnabledStates();
                updatedElements.add(ViewElement.BUTTONS_ENABLED);
            }
        } else if ((change.getEvent().getStatus() == HazardStatus.ENDING)
                && (change.getOriginator() != Originator.DATABASE) && getModel()
                        .getSelectionManager().isSelected(change.getEvent())) {
            showHazardDetail();
        }
    }

    /**
     * Respond to an event's attributes having changed.
     * 
     * @param change
     *            Change that encompasses this modification.
     * @param modification
     *            Modification that occurred.
     * @param updatedElements
     *            Set of view elements that have already been updated. This set
     *            will have any view elements added to it by this method that
     *            are updated as a result of this invocation.
     */
    private void sessionEventAttributesModified(SessionEventModified change,
            EventAttributesModification modification,
            Set<ViewElement> updatedElements) {
        if (detailViewShowing && isNotOrigin(change)
                && isVisibleEventVersionModified(change)) {
            IHazardEvent event = getVisibleEvent();
            if (event != null) {
                if (modification.getAttributeKeys()
                        .contains(HazardConstants.HAZARD_EVENT_CATEGORY)) {
                    if (updatedElements
                            .contains(ViewElement.CATEGORY_VALUE) == false) {
                        updateViewCategory(event,
                                visibleEventVersionIdentifier);
                        updatedElements.add(ViewElement.CATEGORY_VALUE);
                    }
                    if (updatedElements
                            .contains(ViewElement.TYPE_LIST) == false) {
                        updateViewTypeList(event,
                                visibleEventVersionIdentifier);
                        updatedElements.add(ViewElement.TYPE_LIST);
                    }
                }
                if (updatedElements
                        .contains(ViewElement.METADATA_VALUES) == false) {
                    updateViewMetadataValues(event,
                            visibleEventVersionIdentifier,
                            modification.getAttributeKeys());
                    updatedElements.add(ViewElement.METADATA_VALUES);
                }
            }
            if (updatedElements.contains(
                    ViewElement.SELECTED_EVENT_DISPLAYABLES) == false) {
                updateSelectedEventDisplayablesIfChanged();
                updatedElements.add(ViewElement.SELECTED_EVENT_DISPLAYABLES);
            }
        }
    }

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
        updateEntireView();
    }

    /**
     * Determine the visible event version identifier.
     * 
     * @return Identifier of the last accessed selected event version, if found,
     *         or <code>null</code> otherwise.
     */
    private Pair<String, Integer> getVisibleEventVersionIdentifier() {
        return getModel().getSelectionManager()
                .getLastAccessedSelectedEventVersion();
    }

    /**
     * Determine whether or not the currently visible event is the subject of
     * the specified modification.
     * 
     * @param change
     *            Modification that is to be checked.
     * @return <code>true</code> if the modification is of the currently visible
     *         event, <code>false</code> otherwise.
     */
    private boolean isVisibleEventVersionModified(
            AbstractSessionEventModified change) {
        return ((visibleEventVersionIdentifier != null) && change.getEvent()
                .getEventID().equals(visibleEventVersionIdentifier.getFirst()));
    }

    /**
     * Determine whether or not the currently visible event version is found
     * within the specified collection of events.
     * 
     * @param eventIdentifiers
     *            Identifiers of events to be checked for containment of the
     *            currently visible event.
     * @return <code>true</code> if the currently visible event is found within
     *         the specified events, <code>false</code> otherwise.
     */
    private boolean isVisibleEventVersionInCollection(
            Collection<String> eventIdentifiers) {
        if ((visibleEventVersionIdentifier != null)
                && (visibleEventVersionIdentifier.getSecond() == null)) {
            for (String eventIdentifier : eventIdentifiers) {
                if (eventIdentifier
                        .equals(visibleEventVersionIdentifier.getFirst())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Determine whether or not this presenter is the originator of the
     * specified modification.
     * 
     * @param change
     *            Modification that is to be checked.
     * @return <code>true</code> if the modification did not originate with this
     *         presenter, <code>false</code> otherwise.
     */
    private boolean isNotOrigin(OriginatedSessionNotification change) {
        return (change
                .getOriginator() != UIOriginator.HAZARD_INFORMATION_DIALOG);
    }

    /**
     * Compile the list of selected event displayables.
     * 
     * @return List holding all the specified events' displayables.
     */
    private List<DisplayableEventIdentifier> compileSelectedEventDisplayables() {
        List<Pair<String, Integer>> selectedEventVersionIdentifiers = getModel()
                .getSelectionManager().getSelectedEventVersionIdentifiersList();
        ISessionEventManager<ObservedHazardEvent> eventManager = getModel()
                .getEventManager();
        if (selectedEventVersionIdentifiers.isEmpty() == false) {
            List<DisplayableEventIdentifier> list = new ArrayList<>(
                    selectedEventVersionIdentifiers.size());
            boolean showConflicts = getModel().isAutoHazardCheckingOn();
            for (Pair<String, Integer> identifier : selectedEventVersionIdentifiers) {

                /*
                 * Determine whether or not there is a conflict with other
                 * hazard events.
                 */
                boolean conflict = false;
                if (showConflicts && (identifier.getSecond() == null)) {
                    Collection<IHazardEvent> conflictingEvents = conflictingEventsForSelectedEventIdentifiers
                            .get(identifier.getFirst());
                    conflict = ((conflictingEvents != null)
                            && (conflictingEvents.size() > 0));
                }

                /*
                 * Get the hazard event, either the current version or a
                 * historical snapshot depending upon which is selected.
                 */
                IHazardEvent event = null;
                if (identifier.getSecond() == null) {
                    event = eventManager.getEventById(identifier.getFirst());
                } else {
                    event = eventManager
                            .getEventHistoryById(identifier.getFirst())
                            .get(identifier.getSecond());
                }

                /*
                 * Put together the title text.
                 */
                StringBuffer buffer = new StringBuffer("");
                for (IHazardEventParameterDescriber describer : tabTextDescribers) {
                    String description = describer.getDescription(event);
                    if ((description != null)
                            && (description.isEmpty() == false)) {
                        if (buffer.length() > 0) {
                            buffer.append(" ");
                        }
                        buffer.append(description);
                    }
                }

                list.add(new DisplayableEventIdentifier(buffer.toString(),
                        conflict, (identifier.getSecond() != null
                                ? event.getInsertTime() : null)));
            }
            return list;
        }
        return Collections.emptyList();
    }

    /**
     * Update the selected event displayables if they have changed.
     */
    private void updateSelectedEventDisplayablesIfChanged() {

        /*
         * Rebuild the list of selected event displayables; if it has changed,
         * notify the view.
         */
        List<DisplayableEventIdentifier> eventDisplayables = compileSelectedEventDisplayables();
        if (selectedEventDisplayables.equals(eventDisplayables) == false) {
            selectedEventDisplayables = eventDisplayables;
            updateViewSelectedEvents();
            updateViewVisibleEvent(false);
        }
    }

    /**
     * Update the view to use the currently visible time range.
     */
    private void updateViewVisibleTimeRange() {
        TimeRange timeRange = getModel().getTimeManager().getVisibleTimeRange();
        getView().getVisibleTimeRangeChanger().setState(null, new TimeRange(
                timeRange.getStart().getTime(), timeRange.getEnd().getTime()));
    }

    /**
     * Update the view to enable or disable the end time "until further notice"
     * toggle.
     * 
     * @param event
     *            Event for which the update should occur.
     * @param eventVersionIdentifier
     *            Identifier of the event version.
     */
    private void updateViewEndTimeUntilFurtherNoticeEnabled(IHazardEvent event,
            Pair<String, Integer> eventVersionIdentifier) {

        /*
         * Check with the session manager for the event itself if it is an
         * editable event; if instead it is a historical snapshot of an event,
         * check with the configuration manager to see if the event type allows
         * until further notice.
         */
        if (event instanceof ObservedHazardEvent) {
            getView().getMetadataChanger().setEnabled(eventVersionIdentifier,
                    HazardConstants.HAZARD_EVENT_END_TIME_UNTIL_FURTHER_NOTICE,
                    eventIdentifiersAllowingUntilFurtherNotice.contains(
                            visibleEventVersionIdentifier.getFirst()));
        } else {
            HazardTypeEntry hazardType = getModel().getConfigurationManager()
                    .getHazardTypes()
                    .get(HazardEventUtilities.getHazardType(event));
            getView().getMetadataChanger().setEnabled(eventVersionIdentifier,
                    HazardConstants.HAZARD_EVENT_END_TIME_UNTIL_FURTHER_NOTICE,
                    ((hazardType != null)
                            && hazardType.isAllowUntilFurtherNotice()));
        }
    }

    /**
     * Update the view to show the currently selected events.
     */
    private void updateViewSelectedEvents() {
        getView().getVisibleEventChanger().setChoices(null,
                selectedEventVersionIdentifiers, selectedEventDisplayables,
                visibleEventVersionIdentifier);
    }

    /**
     * Update the view to use the category list and category that goes with the
     * specified event version.
     * 
     * @param event
     *            Event for which the update should occur.
     * @param eventVersionIdentifier
     *            Identifier of the event version.
     */
    private void updateViewCategoryList(IHazardEvent event,
            Pair<String, Integer> eventVersionIdentifier) {

        /*
         * If the event is editable (a current version), give it the full list
         * of categories it could have. Otherwise, simply give it the one
         * category it already has, since it cannot change categories anyway.
         */
        selectedCategory = getModel().getConfigurationManager()
                .getHazardCategory(event);
        if (event instanceof ObservedHazardEvent) {
            boolean hasPointId = (event
                    .getHazardAttribute(HazardConstants.POINTID) != null);
            getView().getCategoryChanger()
                    .setChoices(eventVersionIdentifier, (hasPointId ? categories
                            : categoriesNoPointIdRequired),
                            (hasPointId ? categories
                                    : categoriesNoPointIdRequired),
                            selectedCategory);
        } else {
            List<String> categories = Lists.newArrayList(selectedCategory);
            getView().getCategoryChanger().setChoices(eventVersionIdentifier,
                    categories, categories, selectedCategory);
        }
        updateViewDurations(event, eventVersionIdentifier);
    }

    /**
     * Update the view to use the current category for the selected event
     * version.
     * 
     * @param event
     *            Event for which the update should occur.
     * @param eventVersionIdentifier
     *            Identifier of the event version.
     */
    private void updateViewCategory(IHazardEvent event,
            Pair<String, Integer> eventVersionIdentifier) {
        selectedCategory = getModel().getConfigurationManager()
                .getHazardCategory(event);
        getView().getCategoryChanger().setState(eventVersionIdentifier,
                selectedCategory);
    }

    /**
     * Update the view to have the appropriate editability for the category.
     * 
     * @param event
     *            Event for which the update should occur.
     * @param eventVersionIdentifier
     *            Identifier of the event version.
     */
    private void updateViewCategoryEditability(IHazardEvent event,
            Pair<String, Integer> eventVersionIdentifier) {
        if (event instanceof ObservedHazardEvent) {
            HazardStatus status = event.getStatus();
            getView().getCategoryChanger().setEditable(eventVersionIdentifier,
                    (HazardStatus.hasEverBeenIssued(status) == false));
        } else {
            getView().getCategoryChanger().setEditable(eventVersionIdentifier,
                    false);
        }
    }

    /**
     * Update the view to use the type list and type that goes with the current
     * category for the specified event version.
     * 
     * @param event
     *            Event for which the update should occur.
     * @param eventVersionIdentifier
     *            Identifier of the event version.
     */
    private void updateViewTypeList(IHazardEvent event,
            Pair<String, Integer> eventVersionIdentifier) {

        /*
         * If the event is editable (a current version), give it the full list
         * of types it could have. Otherwise, simply give it the one type it
         * already has, since it cannot change types anyway.
         */
        String selectedType = event.getHazardType();
        if (selectedType == null) {
            selectedType = BLANK_TYPE_CHOICE;
        }
        if (event instanceof ObservedHazardEvent) {

            /*
             * If the hazard event is not yet issued, it can have a wide variety
             * of types (though less are available as possibilities if it has no
             * point identifier); otherwise, if it is issued, it can only have
             * the types that can serve as replacements for it; otherwise, since
             * it is ending or ended, it cannot have its type changed.
             */
            switch (event.getStatus()) {
            case POTENTIAL:
            case PENDING:
            case PROPOSED:

                /*
                 * If the hazard event is the replacement for another hazard
                 * event, limit the possible types appropriately; otherwise,
                 * limit them based purely upon whether or not the event has as
                 * point identifier.
                 */
                String replacedType = (String) event.getHazardAttribute(
                        HazardConstants.REPLACED_HAZARD_TYPE);
                if (replacedType != null) {
                    updateReplacedEventTypeChoices(event,
                            eventVersionIdentifier, replacedType, selectedType,
                            false);
                } else {
                    boolean hasPointId = (event.getHazardAttribute(
                            HazardConstants.POINTID) != null);
                    getView().getTypeChanger().setChoices(
                            eventVersionIdentifier,
                            (hasPointId ? typeListsForCategories
                                    : typeListsForCategoriesNoPointIdRequired)
                                            .get(selectedCategory),
                            (hasPointId ? typeDescriptionListsForCategories
                                    : typeDescriptionListsForCategoriesNoPointIdRequired)
                                            .get(selectedCategory),
                            (selectedType == null ? BLANK_TYPE_CHOICE
                                    : selectedType));
                }
                break;
            case ISSUED:
                updateReplacedEventTypeChoices(event, eventVersionIdentifier,
                        selectedType, selectedType, true);
                break;
            case ELAPSED:
            case ENDING:
            case ENDED:
                getView().getTypeChanger().setChoices(eventVersionIdentifier,
                        Lists.newArrayList(selectedType),
                        Lists.newArrayList(
                                descriptionsForTypes.get(selectedType)),
                        selectedType);
            }
        } else {
            getView().getTypeChanger().setChoices(eventVersionIdentifier,
                    Lists.newArrayList(selectedType),
                    Lists.newArrayList(descriptionsForTypes.get(selectedType)),
                    selectedType);
        }
        updateViewDurations(event, eventVersionIdentifier);
    }

    /**
     * Update the view to have the appropriate editability for the type.
     * 
     * @param event
     *            Event for which the update should occur.
     * @param eventVersionIdentifier
     *            Identifier of the event version.
     */
    private void updateViewTypeEditability(IHazardEvent event,
            Pair<String, Integer> eventVersionIdentifier) {
        if (event instanceof ObservedHazardEvent) {
            HazardStatus status = event.getStatus();
            getView().getTypeChanger().setEditable(eventVersionIdentifier,
                    (status != HazardStatus.ELAPSED)
                            && (status != HazardStatus.ENDING)
                            && (status != HazardStatus.ENDED));
        } else {
            getView().getTypeChanger().setEditable(eventVersionIdentifier,
                    false);
        }
    }

    /**
     * Update the view to use the current type for the event version.
     * 
     * @param event
     *            Event for which the update should occur.
     * @param eventVersionIdentifier
     *            Identifier of the event version.
     */
    private void updateViewType(IHazardEvent event,
            Pair<String, Integer> eventVersionIdentifier) {
        String selectedType = event.getHazardType();
        getView().getTypeChanger().setState(eventVersionIdentifier,
                (selectedType == null ? BLANK_TYPE_CHOICE : selectedType));
        updateViewDurations(event, eventVersionIdentifier);
    }

    /**
     * Update the type choices available for the specified replacement hazard
     * event, given the specified type of the event that was replaced by this
     * one.
     * 
     * @param event
     *            Hazard event for which to determine possible choices.
     * @param eventVersionIdentifier
     *            Identifier of the event version.
     * @param originalType
     *            Original type of the hazard event before it was replaced by
     *            this one.
     * @param choiceToSelect
     *            choiceToSelect Choice that is to be selected to begin with.
     * @param includeOriginalType
     *            Flag indicating whether or not to include the original type in
     *            the list of possible types.
     */
    private void updateReplacedEventTypeChoices(IHazardEvent event,
            Pair<String, Integer> eventVersionIdentifier, String originalType,
            String choiceToSelect, boolean includeOriginalType) {
        List<String> possibleReplacementTypes = getModel()
                .getConfigurationManager().getReplaceByTypes(originalType);
        int replacementTypesCount = possibleReplacementTypes.size()
                + (includeOriginalType ? 1 : 0);
        List<String> types = new ArrayList<>(replacementTypesCount);
        List<String> descriptions = new ArrayList<>(replacementTypesCount);
        if (includeOriginalType) {
            types.add(originalType);
            descriptions.add(descriptionsForTypes.get(originalType));
        }
        for (String type : possibleReplacementTypes) {
            types.add(type);
            descriptions.add(descriptionsForTypes.get(type));
        }
        getView().getTypeChanger().setChoices(eventVersionIdentifier, types,
                descriptions, choiceToSelect);
    }

    /**
     * Update the view to use the current time range for the event version.
     * 
     * @param event
     *            Event for which the update should occur.
     * @param eventVersionIdentifier
     *            Identifier of the event version.
     */
    private void updateViewTimeRange(IHazardEvent event,
            Pair<String, Integer> eventVersionIdentifier) {
        getView().getTimeRangeChanger().setState(eventVersionIdentifier,
                new TimeRange(event.getStartTime().getTime(),
                        event.getEndTime().getTime()));

        /*
         * TODO: For Redmine issue #26716, we need to allow time sliders to be
         * disabled on a per-hazard-type basis. Once we get there, replace the
         * "false" below with:
         * 
         * (event is instance of ObservedHazardEvent)
         * 
         * with:
         * 
         * ((event is instance of ObservedHazardEvent) && (event type allows
         * time sliders to be moved))
         * 
         * What should exactly happen with the HID? Should the entry fields be
         * disabled as well?
         */
        boolean editable = (event instanceof ObservedHazardEvent);
        getView().getTimeRangeChanger().setEditable(eventVersionIdentifier,
                editable);
        getView().getMetadataChanger().setEditable(eventVersionIdentifier,
                HazardConstants.HAZARD_EVENT_END_TIME_UNTIL_FURTHER_NOTICE,
                editable);
    }

    /**
     * Update the view to use the current time range boundaries for the event
     * version.
     * 
     * @param event
     *            Event for which the update should occur.
     * @param eventVersionIdentifier
     *            Identifier of the event version.
     */
    private void updateViewTimeRangeBoundaries(IHazardEvent event,
            Pair<String, Integer> eventVersionIdentifier) {
        Map<TimeRangeBoundary, Range<Long>> map = new HashMap<>(2, 1.0f);
        if (event instanceof ObservedHazardEvent) {
            Range<Long> bounds = getModel().getEventManager()
                    .getStartTimeBoundariesForEventIds()
                    .get(event.getEventID());
            if (bounds == null) {
                return;
            }
            map.put(TimeRangeBoundary.START, bounds);
            bounds = getModel().getEventManager()
                    .getEndTimeBoundariesForEventIds().get(event.getEventID());
            if (bounds == null) {
                return;
            }
            map.put(TimeRangeBoundary.END, bounds);
        } else {
            map.put(TimeRangeBoundary.START,
                    Range.singleton(event.getStartTime().getTime()));
            map.put(TimeRangeBoundary.END,
                    Range.singleton(event.getEndTime().getTime()));
        }
        getView().getTimeRangeBoundariesChanger()
                .setStates(eventVersionIdentifier, map);
    }

    /**
     * Update the view to use the duration list goes with the current event
     * version.
     * 
     * @param event
     *            Event for which the update should occur.
     * @param eventVersionIdentifier
     *            Identifier of the event version.
     */
    private void updateViewDurations(IHazardEvent event,
            Pair<String, Integer> eventVersionIdentifier) {
        getView().getTimeResolutionChanger().setState(null,
                getModel().getEventManager().getTimeResolutionsForEventIds()
                        .get(event.getEventID()));
        getView().getDurationChanger().setChoices(eventVersionIdentifier,
                getModel().getEventManager().getDurationChoices(event), null,
                null);
    }

    /**
     * Update the view to use the current metadata specifier manager.
     * 
     * @param event
     *            Event for which the update should occur.
     * @param eventVersionIdentifier
     *            Identifier of the event version.
     * @param reinitializeIfUnchanged
     *            Flag indicating whether or not the metadata manager, if
     *            unchanged as a result of this call, should reinitialize its
     *            components.
     */
    private void updateViewMetadataSpecifiers(IHazardEvent event,
            Pair<String, Integer> eventVersionIdentifier,
            boolean reinitializeIfUnchanged) {
        getView().getMetadataChanger().setMegawidgetSpecifierManager(
                eventVersionIdentifier,
                specifierManagersForSelectedEventVersions
                        .get(visibleEventVersionIdentifier),
                new HashMap<>(event.getHazardAttributes()),
                (eventVersionIdentifier.getSecond() == null),
                reinitializeIfUnchanged);
    }

    /**
     * Update the view to use the current metadata values.
     * 
     * @param event
     *            Event for which the update should occur.
     * @param eventVersionIdentifier
     *            Identifier of the event version for which the cache is to be
     *            checked.
     * @param names
     *            Names of the attributes to be updated, or <code>null</code> if
     *            all attributes should be updated.
     */
    private void updateViewMetadataValues(IHazardEvent event,
            Pair<String, Integer> eventVersionIdentifier, Set<String> names) {

        /*
         * Set the states of all the hazard attributes if no names are provided;
         * if names are provided, only set the states of the hazard attributes
         * with those names.
         */
        getView().getMetadataChanger().setStates(eventVersionIdentifier,
                new HashMap<>(names == null ? event.getHazardAttributes()
                        : event.getHazardAttributes().entrySet().stream()
                                .filter(entry -> names.contains(entry.getKey()))
                                .collect(Collectors.toMap(
                                        entry -> entry.getKey(),
                                        entry -> entry.getValue()))));
    }

    /**
     * Ensure that the cache of metadata specifier managers contains one for the
     * specified event.
     * 
     * @param event
     *            Event for which to ensure the cache has a manager. It must be
     *            a valid event (i.e., not <code>null</code>).
     * @param eventVersionIdentifier
     *            Identifier of the event version for which the cache is to be
     *            checked.
     */
    private void cacheMetadataSpecifiers(IHazardEvent event,
            Pair<String, Integer> eventVersionIdentifier) {
        if (specifierManagersForSelectedEventVersions
                .containsKey(eventVersionIdentifier) == false) {
            specifierManagersForSelectedEventVersions
                    .put(eventVersionIdentifier, getModel().getEventManager()
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
        IHazardEvent event = getVisibleEvent();
        boolean enable = ((event != null)
                && (event instanceof ObservedHazardEvent)
                && (HazardEventUtilities.getHazardType(event) != null)
                && (getModel().isPreviewOngoing() == false)
                && (getModel().isIssueOngoing() == false)
                && (event.getStatus() != HazardStatus.ENDED)
                && (event.getStatus() != HazardStatus.ELAPSED));
        getView().getButtonInvoker().setEnabled(Command.PREVIEW, enable);
        getView().getButtonInvoker().setEnabled(Command.PROPOSE,
                (enable && getModel().getEventManager()
                        .getEventIdsAllowingProposal()
                        .contains(visibleEventVersionIdentifier.getFirst())));
        getView().getButtonInvoker().setEnabled(Command.ISSUE, enable);
    }

    /**
     * Update the view to display the currently visible event version.
     * 
     * @param reinitializeIfUnchanged
     *            Flag indicating whether or not the visible event version's
     *            metadata manager, if unchanged as a result of this call,
     *            should reinitialize its components.
     */
    private void updateViewVisibleEvent(boolean reinitializeIfUnchanged) {
        IHazardEvent event = getVisibleEvent();
        if (event != null) {
            cacheMetadataSpecifiers(event, visibleEventVersionIdentifier);
            updateViewVisibleTimeRange();
            updateViewCategoryList(event, visibleEventVersionIdentifier);
            updateViewCategoryEditability(event, visibleEventVersionIdentifier);
            updateViewTypeList(event, visibleEventVersionIdentifier);
            updateViewTypeEditability(event, visibleEventVersionIdentifier);
            updateViewTimeRangeBoundaries(event, visibleEventVersionIdentifier);
            updateViewTimeRange(event, visibleEventVersionIdentifier);
            updateViewMetadataSpecifiers(event, visibleEventVersionIdentifier,
                    reinitializeIfUnchanged);
            updateViewEndTimeUntilFurtherNoticeEnabled(event,
                    visibleEventVersionIdentifier);
        }
        updateViewButtonsEnabledStates();
    }

    /**
     * Update the entire detail view.
     */
    private void updateEntireView() {
        detailViewShowing = true;
        selectedEventVersionIdentifiers = new ArrayList<>(
                getModel().getSelectionManager()
                        .getSelectedEventVersionIdentifiersList());
        visibleEventVersionIdentifier = getVisibleEventVersionIdentifier();
        selectedEventDisplayables = compileSelectedEventDisplayables();
        updateViewSelectedEvents();
        updateViewVisibleEvent(false);
    }

    /**
     * Preview appropriate hazard events.
     */
    private void preview() {
        getModel().generate(false);
    }

    /**
     * Propose appropriate hazard events.
     */
    private void propose() {

        Collection<ObservedHazardEvent> events = getModel()
                .getSelectionManager().getSelectedEvents();

        for (ObservedHazardEvent event : events) {
            getModel().getEventManager().proposeEvent(event,
                    UIOriginator.HAZARD_INFORMATION_DIALOG);
        }

        hazardDetailHandler.closeProductEditor();
    }

    /**
     * Issue appropriate hazard events.
     */
    private void issue() {
        if (hazardDetailHandler.shouldContinueIfThereAreHazardConflicts()) {
            getModel().generate(true);
        }
    }

    /**
     * Get the currently visible event.
     * 
     * @return Currently visible event, or <code>null</code> if there is no
     *         visible event.
     */
    private IHazardEvent getVisibleEvent() {
        if (visibleEventVersionIdentifier == null) {
            return null;
        } else if (visibleEventVersionIdentifier.getSecond() == null) {
            return getEventByIdentifier(
                    visibleEventVersionIdentifier.getFirst());
        } else {
            HazardHistoryList historyList = getModel().getEventManager()
                    .getEventHistoryById(
                            visibleEventVersionIdentifier.getFirst());
            return historyList.get(visibleEventVersionIdentifier.getSecond());
        }
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
     * Ensure that the specified event version identifier is not that of a
     * historical event snapshot.
     * 
     * @param eventVersionIdentifier
     *            Event version identifier.
     * @throws IllegalArgumentException
     *             If the specified event version identifier is for a historical
     *             event snapshot.
     */
    private void ensureNotHistorical(
            Pair<String, Integer> eventVersionIdentifier) {
        if (eventVersionIdentifier.getSecond() != null) {
            throw new IllegalArgumentException(
                    "cannot change state of historical event");
        }
    }

    /**
     * Throw an unsupported operation exception for attempts to change multiple
     * states that are not appropriate.
     * 
     * @param description
     *            Description of the element for which an attempt to change
     *            multiple states was made.
     * @throws UnsupportedOperationException
     *             Whenever this method is called.
     */
    private void handleUnsupportedOperationAttempt(String description)
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException(
                "cannot change multiple states associated with detail view "
                        + description);
    }
}

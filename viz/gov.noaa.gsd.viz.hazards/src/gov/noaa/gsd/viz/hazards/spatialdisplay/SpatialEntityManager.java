/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Evaluation & Decision Support Branch (EDS)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.spatialdisplay;

import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HIGH_RESOLUTION_GEOMETRY_IS_VISIBLE;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.VISIBLE_GEOMETRY;
import gov.noaa.gsd.common.utilities.geometry.AdvancedGeometryCollection;
import gov.noaa.gsd.common.utilities.geometry.AdvancedGeometryUtilities;
import gov.noaa.gsd.common.utilities.geometry.GeometryWrapper;
import gov.noaa.gsd.common.utilities.geometry.IAdvancedGeometry;
import gov.noaa.gsd.common.visuals.BorderStyle;
import gov.noaa.gsd.common.visuals.DragCapability;
import gov.noaa.gsd.common.visuals.FillStyle;
import gov.noaa.gsd.common.visuals.SpatialEntity;
import gov.noaa.gsd.common.visuals.SymbolShape;
import gov.noaa.gsd.common.visuals.VisualFeature;
import gov.noaa.gsd.common.visuals.VisualFeaturesList;
import gov.noaa.gsd.viz.hazards.spatialdisplay.SpatialPresenter.SpatialEntityType;
import gov.noaa.gsd.viz.hazards.spatialdisplay.entities.HazardEventEntityIdentifier;
import gov.noaa.gsd.viz.hazards.spatialdisplay.entities.HazardEventHatchingEntityIdentifier;
import gov.noaa.gsd.viz.hazards.spatialdisplay.entities.HazardEventVisualFeatureEntityIdentifier;
import gov.noaa.gsd.viz.hazards.spatialdisplay.entities.IEntityIdentifier;
import gov.noaa.gsd.viz.hazards.spatialdisplay.entities.IHazardEventEntityIdentifier;
import gov.noaa.gsd.viz.hazards.spatialdisplay.entities.ToolVisualFeatureEntityIdentifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;
import com.raytheon.uf.common.colormap.Color;
import com.raytheon.uf.common.dataaccess.geom.IGeometryData;
import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardEventUtilities;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.hazards.configuration.types.HatchingStyle;
import com.raytheon.uf.common.hazards.configuration.types.HazardTypeEntry;
import com.raytheon.uf.common.hazards.configuration.types.HazardTypes;
import com.raytheon.uf.viz.core.IGraphicsTarget.LineStyle;
import com.raytheon.uf.viz.hazards.sessionmanager.ISessionManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.ISessionConfigurationManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.ObservedSettings;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.ToolType;
import com.raytheon.uf.viz.hazards.sessionmanager.events.ISessionEventManager;
import com.raytheon.uf.viz.hazards.sessionmanager.events.impl.ObservedHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.geomaps.GeoMapUtilities;
import com.raytheon.uf.viz.hazards.sessionmanager.time.SelectedTime;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.Polygonal;

/**
 * Description: Manager for {@link SpatialEntity} instances within the
 * {@link SpatialPresenter} .
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Jul 22, 2016   19537    Chris.Golden Initial creation.
 * Jul 28, 2016   19537    Chris.Golden Fixed bugs related to tracking
 *                                      spatial entity indices, which
 *                                      among other things caused any
 *                                      attempts to remove a hazard event
 *                                      to incorrectly change the indices
 *                                      and thus eventually lead to
 *                                      index-out-of-bounds exceptions.
 * Aug 15, 2016   18376    Chris.Golden Added code to make garbage
 *                                      collection of the session manager
 *                                      more likely.
 * Aug 23, 2016   19537    Chris.Golden Continued extensive spatial display
 *                                      refactor, including changing the
 *                                      code to reuse spatial entities that
 *                                      do not need to be updated, thus
 *                                      avoiding many unnecessary redraws.
 * Sep 12, 2016   15934    Chris.Golden Changed to work with advanced
 *                                      geometries.
 * Sep 27, 2016   15928    Chris.Golden Changed spatial entity creation to
 *                                      only mark the entities as rotatable
 *                                      and/or scaleable if they are
 *                                      selected.
 * Oct 19, 2016   21873    Chris.Golden Removed rounding of selected time,
 *                                      as this is now done elsewhere (and
 *                                      shouldn't be the spatial display's
 *                                      concern).
 * Nov 17, 2016   26313    Chris.Golden Changed to work with revamped
 *                                      GeoMapUtilities.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
class SpatialEntityManager {

    // Package-Private Enumerated Types

    /**
     * Action to be taken with regard to display of a hazard event.
     */
    enum EventDisplayChange {
        NONE, ADD_OR_REPLACE, REMOVE
    };

    // Private Static Classes

    /**
     * Encapsulation of the result of a spatial entity creation attempt for a
     * hazard event.
     */
    private static class CreationResult {

        // Private Variables

        /**
         * Flag indicating whether or not the creation attempt resulted in one
         * or more spatial entities being created or reused.
         */
        private final boolean spatialEntitiesCreatedOrReused;

        /**
         * Map of spatial entity types to flags indicating whether or not all
         * the spatial entities created or reused for those types were reused;
         * if <code>spatialEntitiesCreatedOrReused</code> is <code>false</code>,
         * this will be <code>null</code>.
         */
        private final Map<SpatialEntityType, Boolean> reusedForTypes;

        // Public Constructors

        /**
         * Construct a standard instance.
         * 
         * @param spatialEntitiesCreatedOrReused
         *            Flag indicating whether or not the creation attempt
         *            resulted in one or more spatial entities being created or
         *            reused.
         * @param reusedForTypes
         *            Map of spatial entity types to flags indicating whether or
         *            not all the spatial entities created or reused for those
         *            types were reused; if
         *            <code>spatialEntitiesCreatedOrReused</code> is
         *            <code>false</code>, this will be <code>null</code>.
         */
        public CreationResult(boolean spatialEntitiesCreatedOrReused,
                Map<SpatialEntityType, Boolean> reusedForTypes) {
            this.spatialEntitiesCreatedOrReused = spatialEntitiesCreatedOrReused;
            this.reusedForTypes = reusedForTypes;
        }
    }

    /**
     * Created or reused spatial entity and a flag indicating whether or not
     * said entity was reused (as opposed to created).
     */
    private static class CreatedSpatialEntity {

        // Private Variables

        /**
         * Created or reused spatial entities.
         */
        private final SpatialEntity<? extends IHazardEventEntityIdentifier> spatialEntity;

        /**
         * Flag indicating whether or not <code>spatialEntity</code> was reused
         * instead of created.
         */
        private final boolean reused;

        // Public Constructors

        /**
         * Construct a standard instance.
         * 
         * @param spatialEntity
         *            Created or reused spatial entity.
         * @param reused
         *            Flag indicating whether or not <code>spatialEntity</code>
         *            was reused instead of created.
         */
        public CreatedSpatialEntity(
                SpatialEntity<? extends IHazardEventEntityIdentifier> spatialEntity,
                boolean reused) {
            this.spatialEntity = spatialEntity;
            this.reused = reused;
        }
    }

    /**
     * List of created or reused spatial entities and a flag indicating whether
     * or not all the entities in the list were reused (as opposed to created).
     */
    private static class CreatedSpatialEntities {

        // Private Variables

        /**
         * List of created or reused spatial entities.
         */
        private final List<SpatialEntity<? extends IHazardEventEntityIdentifier>> spatialEntities;

        /**
         * Flag indicating whether or not all the entities in
         * <code>spatialEntities</code> were reused instead of created.
         */
        private final boolean allReused;

        // Public Constructors

        /**
         * Construct a standard instance.
         * 
         * @param spatialEntities
         *            List of created or reused spatial entities.
         * @param allReused
         *            Flag indicating whether or not all the entities in
         *            <code>spatialEntities</code> were reused instead of
         *            created.
         */
        public CreatedSpatialEntities(
                List<SpatialEntity<? extends IHazardEventEntityIdentifier>> spatialEntities,
                boolean allReused) {
            this.spatialEntities = spatialEntities;
            this.allReused = allReused;
        }
    }

    /**
     * Encapsulation of a sublist of spatial entities, including its position
     * and size within a list.
     */
    private static class Sublist {

        // Private Variables

        /**
         * Index of the sublist's starting point in the list.
         */
        private final int index;

        /**
         * Number of elements in the sublist.
         */
        private final int size;

        /**
         * Sublist.
         */
        private final List<SpatialEntity<? extends IHazardEventEntityIdentifier>> list;

        // Public Constructors

        /**
         * Construct a standard instance.
         * 
         * @param index
         *            Index of the sublist's starting point in the list.
         * @param size
         *            Number of elements in the list.
         */
        public Sublist(
                List<SpatialEntity<? extends IHazardEventEntityIdentifier>> list,
                int index, int size) {
            this.list = list;
            this.index = index;
            this.size = size;
        }
    }

    // Private Static Constants

    /**
     * Set of spatial entity types associated with hazard events.
     */
    private static final EnumSet<SpatialEntityType> HAZARD_EVENT_SPATIAL_ENTITY_TYPES = EnumSet
            .of(SpatialEntityType.HATCHING, SpatialEntityType.UNSELECTED,
                    SpatialEntityType.SELECTED);

    /**
     * Empty created spatial entities.
     */
    private static final CreatedSpatialEntities EMPTY_CREATED_SPATIAL_ENTITIES = new CreatedSpatialEntities(
            Collections
                    .<SpatialEntity<? extends IHazardEventEntityIdentifier>> emptyList(),
            false);

    /**
     * Empty creation result.
     */
    private static final CreationResult EMPTY_CREATION_RESULT = new CreationResult(
            false, null);

    /**
     * Standard diameter in pixels of a hazard event point shape.
     */
    private static final double HAZARD_EVENT_POINT_DIAMETER = 8.5;

    /**
     * Standard diameter in pixels of a hazard event point shape when selected.
     */
    private static final double HAZARD_EVENT_POINT_SELECTED_DIAMETER = 10.5;

    /**
     * Standard label offset length in pixels from the center of a hazard event
     * with a point-type geometry.
     */
    private static final double HAZARD_EVENT_SINGLE_POINT_TEXT_OFFSET_LENGTH = 10.0;

    /**
     * Standard label offset direction in degrees for a hazard event with a
     * point-type geometry.
     */
    private static final double HAZARD_EVENT_SINGLE_POINT_TEXT_OFFSET_DIRECTION = 90.0;

    /**
     * Standard label offset length in pixels from the center of a hazard event
     * with a multipoint-type geometry.
     */
    private static final double HAZARD_EVENT_MULTI_POINT_TEXT_OFFSET_LENGTH = 0.0;

    /**
     * Standard label offset direction in degrees for a hazard event with a
     * multipoint-type geometry.
     */
    private static final double HAZARD_EVENT_MULTI_POINT_TEXT_OFFSET_DIRECTION = 0.0;

    /**
     * Standard font point size for hazard event labels.
     */
    private static final int HAZARD_EVENT_FONT_POINT_SIZE = 15;

    /**
     * Clear (completely transparent) color.
     */
    private static final Color TRANSPARENT_COLOR = new Color();

    // Private Variables

    /**
     * Session manager.
     */
    private ISessionManager<ObservedHazardEvent, ObservedSettings> sessionManager;

    /**
     * View associated with the presenter using this manager.
     */
    private ISpatialView<?, ?> view;

    /**
     * Read-only set that will always contain the currently selected event
     * identifiers.
     */
    private final Set<String> selectedEventIdentifiers;

    /**
     * Geometry factory.
     */
    private final GeometryFactory geometryFactory = new GeometryFactory();

    /**
     * Geo-map utilities, for collecting map-related geometries for hatching.
     */
    private GeoMapUtilities geoMapUtilities;

    /**
     * List of hatching spatial entities.
     */
    private final List<SpatialEntity<? extends IHazardEventEntityIdentifier>> hatchingSpatialEntities = new ArrayList<>(
            SpatialPresenter.INITIAL_SIZES_FOR_SPATIAL_ENTITIES_LISTS
                    .get(SpatialEntityType.HATCHING));

    /**
     * List of unselected spatial entities.
     */
    private final List<SpatialEntity<? extends IHazardEventEntityIdentifier>> unselectedSpatialEntities = new ArrayList<>(
            SpatialPresenter.INITIAL_SIZES_FOR_SPATIAL_ENTITIES_LISTS
                    .get(SpatialEntityType.UNSELECTED));

    /**
     * List of selected spatial entities.
     */
    private final List<SpatialEntity<? extends IHazardEventEntityIdentifier>> selectedSpatialEntities = new ArrayList<>(
            SpatialPresenter.INITIAL_SIZES_FOR_SPATIAL_ENTITIES_LISTS
                    .get(SpatialEntityType.SELECTED));

    /**
     * List of spatial entities generated from the visual features within the
     * list provided to the last invocation of
     * {@link #updateEntitiesForToolVisualFeatures(VisualFeaturesList, ToolType, String)}
     * .
     */
    private final List<SpatialEntity<ToolVisualFeatureEntityIdentifier>> toolSpatialEntities = new ArrayList<>(
            SpatialPresenter.INITIAL_SIZES_FOR_SPATIAL_ENTITIES_LISTS
                    .get(SpatialEntityType.TOOL));

    /**
     * Map associating spatial entity types with the lists of spatial entities
     * of those types.
     */
    private final Map<SpatialEntityType, List<? extends SpatialEntity<? extends IEntityIdentifier>>> spatialEntitiesForTypes = new EnumMap<>(
            SpatialEntityType.class);

    /**
     * Map associating spatial entity types with maps that in turn pair each
     * hazard event that is currently showing at least one spatial entity of
     * that type with the index of its first such entity within the
     * corresponding list in {@link #spatialEntitiesForTypes}. Note that there
     * is no entry in this map for {@link SpatialEntityType#TOOL}.
     */
    private final Map<SpatialEntityType, Map<String, Integer>> indicesForEventsForTypes = new EnumMap<>(
            SpatialEntityType.class);

    /**
     * Map of identifiers to spatial entities.
     */
    private final Map<IEntityIdentifier, SpatialEntity<? extends IEntityIdentifier>> spatialEntitiesForIdentifiers = new HashMap<>();

    /**
     * Identifiers of currently selected spatial entities.
     */
    private final Set<IEntityIdentifier> selectedSpatialEntityIdentifiers = new HashSet<>();

    // Package-Private Constructors

    /**
     * Construct a standard instance.
     * 
     * @param sessionManager
     *            Session manager to be used.
     * @param selectedEventIdentifiers
     *            Read-only set that will always contain the currently selected
     *            event identifiers.
     */
    SpatialEntityManager(
            ISessionManager<ObservedHazardEvent, ObservedSettings> sessionManager,
            Set<String> selectedEventIdentifiers) {
        this.sessionManager = sessionManager;
        this.geoMapUtilities = new GeoMapUtilities(
                sessionManager.getConfigurationManager());
        this.selectedEventIdentifiers = selectedEventIdentifiers;
        spatialEntitiesForTypes.put(SpatialEntityType.HATCHING,
                hatchingSpatialEntities);
        spatialEntitiesForTypes.put(SpatialEntityType.UNSELECTED,
                unselectedSpatialEntities);
        spatialEntitiesForTypes.put(SpatialEntityType.SELECTED,
                selectedSpatialEntities);
        spatialEntitiesForTypes
                .put(SpatialEntityType.TOOL, toolSpatialEntities);
        indicesForEventsForTypes.put(SpatialEntityType.HATCHING,
                new HashMap<String, Integer>());
        indicesForEventsForTypes.put(SpatialEntityType.UNSELECTED,
                new HashMap<String, Integer>());
        indicesForEventsForTypes.put(SpatialEntityType.SELECTED,
                new HashMap<String, Integer>());
    }

    // Package-Private Methods

    /**
     * Set the view to that specified.
     * 
     * @param view
     *            View to be used.
     */
    void setView(ISpatialView<?, ?> view) {
        this.view = view;
    }

    /**
     * Get an unmodifiable view of the selected spatial entity identifiers.
     * 
     * @return Unmodifiable view of the selected spatial entity identifiers.
     */
    Set<IEntityIdentifier> getSelectedSpatialEntityIdentifiers() {
        return Collections.unmodifiableSet(selectedSpatialEntityIdentifiers);
    }

    /**
     * Dispose of the manager.
     */
    void dispose() {
        sessionManager = null;
        geoMapUtilities = null;
    }

    /**
     * Add spatial entities for the specified event.
     * 
     * @param event
     *            Event for which to add spatial entities.
     */
    void addEntitiesForEvent(IHazardEvent event) {

        /*
         * Find the event within the list of events that may be visible in the
         * spatial display. If it is not found to be visible, do nothing more
         * with it.
         */
        List<ObservedHazardEvent> events = sessionManager.getEventManager()
                .getCheckedEvents();
        int eventIndex = events.indexOf(event);
        if (eventIndex != -1) {

            /*
             * Create the spatial entities for the event, if any.
             */
            List<SpatialEntity<? extends IHazardEventEntityIdentifier>> hatchingEntities = new ArrayList<>();
            List<SpatialEntity<? extends IHazardEventEntityIdentifier>> unselectedEntities = new ArrayList<>();
            List<SpatialEntity<? extends IHazardEventEntityIdentifier>> selectedEntities = new ArrayList<>();
            SelectedTime selectedRange = sessionManager.getTimeManager()
                    .getSelectedTime();
            Date selectedTime = getSelectedTimeForVisualFeatures();
            if (createSpatialEntitiesForEvent((ObservedHazardEvent) event,
                    hatchingEntities, unselectedEntities, selectedEntities,
                    selectedRange, selectedTime, sessionManager
                            .getConfigurationManager().getHazardTypes(),
                    sessionManager.areHatchedAreasDisplayed()).spatialEntitiesCreatedOrReused == false) {
                return;
            }

            /*
             * Add each list of generated spatial entities to the list of the
             * appropriate type of spatial entities for all events.
             */
            boolean changed = addEventSpatialEntities(
                    SpatialEntityType.HATCHING, hatchingEntities, events,
                    eventIndex);
            changed |= addEventSpatialEntities(SpatialEntityType.UNSELECTED,
                    unselectedEntities, events, eventIndex);
            changed |= addEventSpatialEntities(SpatialEntityType.SELECTED,
                    selectedEntities, events, eventIndex);

            /*
             * Tell the view about the change.
             */
            if (changed) {
                view.refresh();
            }
        }
    }

    /**
     * Replace spatial entities for the specified event with updated ones.
     * 
     * @param event
     *            Event for which to replace spatial entities.
     * @param indexMayHaveChanged
     *            Flag indicating whether or not the event's position in the
     *            events list may have changed.
     * @param forceReplacement
     *            Flag indicating whether or not replacement should be forced,
     *            even if the new and old spatial entities are identical.
     */
    void replaceEntitiesForEvent(IHazardEvent event,
            boolean indexMayHaveChanged, boolean forceReplacement) {

        /*
         * Find the event within the list of events that may be visible in the
         * spatial display, and create spatial entities for it if it is found.
         */
        List<ObservedHazardEvent> events = sessionManager.getEventManager()
                .getCheckedEvents();
        int eventIndex = events.indexOf(event);
        CreationResult creationResult = null;
        List<SpatialEntity<? extends IHazardEventEntityIdentifier>> hatchingEntities = null;
        List<SpatialEntity<? extends IHazardEventEntityIdentifier>> unselectedEntities = null;
        List<SpatialEntity<? extends IHazardEventEntityIdentifier>> selectedEntities = null;
        if (eventIndex != -1) {

            /*
             * Create the updated spatial entities for the event, if any.
             */
            hatchingEntities = new ArrayList<>();
            unselectedEntities = new ArrayList<>();
            selectedEntities = new ArrayList<>();
            SelectedTime selectedRange = sessionManager.getTimeManager()
                    .getSelectedTime();
            Date selectedTime = getSelectedTimeForVisualFeatures();
            creationResult = createSpatialEntitiesForEvent(
                    (ObservedHazardEvent) event, hatchingEntities,
                    unselectedEntities, selectedEntities, selectedRange,
                    selectedTime, sessionManager.getConfigurationManager()
                            .getHazardTypes(),
                    sessionManager.areHatchedAreasDisplayed());
        }

        /*
         * If entities were created, replace the old entities with the new ones;
         * otherwise, treat it as a removal of the event's existing entities, if
         * any.
         */
        if ((creationResult != null)
                && creationResult.spatialEntitiesCreatedOrReused) {

            /*
             * For each type of spatial entity associated with events, replace
             * any that were associated with the event in the past with new
             * ones.
             */
            String eventIdentifier = event.getEventID();
            replaceEventSpatialEntities(SpatialEntityType.HATCHING,
                    hatchingEntities, events, eventIndex, eventIdentifier,
                    indexMayHaveChanged,
                    ((forceReplacement == false) && Boolean.TRUE
                            .equals(creationResult.reusedForTypes
                                    .get(SpatialEntityType.HATCHING))));
            replaceEventSpatialEntities(SpatialEntityType.UNSELECTED,
                    unselectedEntities, events, eventIndex, eventIdentifier,
                    indexMayHaveChanged,
                    ((forceReplacement == false) && Boolean.TRUE
                            .equals(creationResult.reusedForTypes
                                    .get(SpatialEntityType.UNSELECTED))));
            replaceEventSpatialEntities(SpatialEntityType.SELECTED,
                    selectedEntities, events, eventIndex, eventIdentifier,
                    indexMayHaveChanged,
                    ((forceReplacement == false) && Boolean.TRUE
                            .equals(creationResult.reusedForTypes
                                    .get(SpatialEntityType.SELECTED))));
            view.refresh();
        } else {
            removeEntitiesForEvent(event);
        }
    }

    /**
     * Remove spatial entities for the specified event.
     * 
     * @param event
     *            Event for which to remove spatial entities.
     */
    void removeEntitiesForEvent(IHazardEvent event) {

        /*
         * Remove any spatial entities associated with the event.
         */
        String eventIdentifier = event.getEventID();
        boolean changed = removeEventSpatialEntities(
                SpatialEntityType.HATCHING, eventIdentifier);
        changed |= removeEventSpatialEntities(SpatialEntityType.UNSELECTED,
                eventIdentifier);
        changed |= removeEventSpatialEntities(SpatialEntityType.SELECTED,
                eventIdentifier);

        /*
         * Tell the view about the change.
         */
        if (changed) {
            view.refresh();
        }
    }

    /**
     * Update spatial entities for tool visual features. Any old tool-related
     * spatial entities will be removed, and new ones put in their place.
     * 
     * @param toolVisualFeatures
     *            Visual features to be used to generate new spatial entities.
     * @param toolType
     *            Type of the tool with which the spatial entities are to be
     *            associated.
     * @param toolIdentifier
     *            Identifier of the tool with which the spatial entities are to
     *            be associated.
     */
    void updateEntitiesForToolVisualFeatures(
            VisualFeaturesList toolVisualFeatures, ToolType toolType,
            String toolIdentifier) {

        /*
         * Generate the new spatial entities, and tell the view about the
         * change.
         */
        if (replaceToolSpatialEntities(toolVisualFeatures, toolType,
                toolIdentifier, getSelectedTimeForVisualFeatures())) {
            view.refresh();
        }
    }

    /**
     * Clear and recreate all spatial entities.
     * 
     * @param toolVisualFeatures
     *            Visual features to be used to generate new spatial entities.
     * @param toolType
     *            Type of the tool with which the
     *            <code>toolVisualFeatures</code> spatial entities are to be
     *            associated.
     * @param toolIdentifier
     *            Identifier of the tool with which the
     *            <code>toolVisualFeatures</code> spatial entities are to be
     *            associated.
     */
    void recreateAllEntities(VisualFeaturesList toolVisualFeatures,
            ToolType toolType, String toolIdentifier) {

        /*
         * Iterate through the hazard events, compiling lists of spatial
         * entities from all events that either have no visual features, but
         * whose time ranges intersect the current selected time range, or if
         * they have visual features, that are visible at the current selected
         * time range's start time.
         */
        SelectedTime selectedRange = sessionManager.getTimeManager()
                .getSelectedTime();
        Date selectedTime = getSelectedTimeForVisualFeatures();
        HazardTypes hazardTypes = sessionManager.getConfigurationManager()
                .getHazardTypes();
        boolean hatching = sessionManager.areHatchedAreasDisplayed();
        Map<SpatialEntityType, List<SpatialEntity<? extends IHazardEventEntityIdentifier>>> newSpatialEntitiesForTypes = new EnumMap<>(
                SpatialEntityType.class);
        for (SpatialEntityType type : HAZARD_EVENT_SPATIAL_ENTITY_TYPES) {
            newSpatialEntitiesForTypes
                    .put(type,
                            new ArrayList<SpatialEntity<? extends IHazardEventEntityIdentifier>>(
                                    SpatialPresenter.INITIAL_SIZES_FOR_SPATIAL_ENTITIES_LISTS
                                            .get(type)));
        }
        for (ObservedHazardEvent event : sessionManager.getEventManager()
                .getCheckedEvents()) {
            createSpatialEntitiesForEvent(event,
                    newSpatialEntitiesForTypes.get(SpatialEntityType.HATCHING),
                    newSpatialEntitiesForTypes
                            .get(SpatialEntityType.UNSELECTED),
                    newSpatialEntitiesForTypes.get(SpatialEntityType.SELECTED),
                    selectedRange, selectedTime, hazardTypes, hatching);
        }

        /*
         * Repopulate the selected spatial entity identifiers record.
         */
        selectedSpatialEntityIdentifiers.clear();
        for (SpatialEntity<? extends IEntityIdentifier> spatialEntity : newSpatialEntitiesForTypes
                .get(SpatialEntityType.SELECTED)) {
            selectedSpatialEntityIdentifiers.add(spatialEntity.getIdentifier());
        }

        /*
         * Associate the spatial entities with their identifiers, and remember
         * the starting indices for each hazard event for each of the types of
         * spatial entity that may be associated with that event. Also mark any
         * type that has new spatial entities as having changed.
         */
        Set<SpatialEntityType> changedEntityTypes = EnumSet
                .noneOf(SpatialEntityType.class);
        spatialEntitiesForIdentifiers.clear();
        for (Map.Entry<SpatialEntityType, List<SpatialEntity<? extends IHazardEventEntityIdentifier>>> entry : newSpatialEntitiesForTypes
                .entrySet()) {
            repopulateEntityAssociationsAndIndicesForEvents(entry.getKey(),
                    entry.getValue());
            if (doListsHoldSameReferences(
                    spatialEntitiesForTypes.get(entry.getKey()),
                    entry.getValue()) == false) {
                changedEntityTypes.add(entry.getKey());
            }
        }

        /*
         * For any hazard-event-related spatial entity types that have changed,
         * set the associated list to hold the new spatial entities.
         */
        for (SpatialEntityType type : changedEntityTypes) {
            setSpatialEntities(type, newSpatialEntitiesForTypes.get(type));
        }

        /*
         * If any spatial-information-collecting visual features are currently
         * in existence for a tool, create spatial entities for the specified
         * visual features as appropriate, and if at least one is created, mark
         * the tool-related spatial entity type as changed.
         */
        if (replaceToolSpatialEntities(toolVisualFeatures, toolType,
                toolIdentifier, selectedTime)) {
            changedEntityTypes.add(SpatialEntityType.TOOL);
        }

        /*
         * Tell the view to redraw if something changed..
         */
        if (changedEntityTypes.isEmpty() == false) {
            view.refresh();
        }
    }

    /**
     * Determine what action must be taken with regard to creating or removing
     * the spatial features for the specified hazard event given the specified
     * selected time change.
     * 
     * @param event
     *            Event to be checked.
     * @param oldTime
     *            Old selected time.
     * @param newTime
     *            New selected time.
     * @return Action that must be taken to show, hide, or regenerate the
     *         spatial entities for the hazard event, if any.
     */
    EventDisplayChange getSpatialEntityActionForEventWithChangedSelectedTime(
            ObservedHazardEvent event, SelectedTime oldTime,
            SelectedTime newTime) {

        /*
         * If there is no previous selected time, addition may be required.
         */
        if (oldTime == null) {
            return EventDisplayChange.ADD_OR_REPLACE;
        }

        /*
         * Visual features may require changes as a result of any change to the
         * lower bound of the selected time.
         */
        VisualFeaturesList visualFeatures = event.getVisualFeatures();
        if ((visualFeatures != null)
                && (visualFeatures.isEmpty() == false)
                && isChangedSelectedTimeAffectingVisualFeatures(oldTime,
                        newTime)) {
            return EventDisplayChange.ADD_OR_REPLACE;
        }

        /*
         * Determine whether or not the event would be showing within the old
         * selected time and the new selected time, and compare the results,
         * returning the appropriate action.
         */
        boolean inOldTime = oldTime.intersects(event.getStartTime().getTime(),
                event.getEndTime().getTime());
        boolean inNewTime = newTime.intersects(event.getStartTime().getTime(),
                event.getEndTime().getTime());
        return (inOldTime == inNewTime ? EventDisplayChange.NONE
                : (inOldTime ? EventDisplayChange.REMOVE
                        : EventDisplayChange.ADD_OR_REPLACE));
    }

    /**
     * Get the first spatial entity found in the list of spatial entities that
     * has an at least partly polygonal geometry and represents the specified
     * event.
     * 
     * @param eventIdentifier
     *            Identifier of the event that the spatial entity to be fetched
     *            must represent.
     * @return Spatial entity, or <code>null</code> if none is found that
     *         represents the event and is at least partly polygonal.
     */
    SpatialEntity<? extends IEntityIdentifier> getFirstPolygonalSpatialEntityForEvent(
            String eventIdentifier) {

        /*
         * Determine which list of spatial entities, the one of selected or the
         * one of unselected, contains entities for this event; if neither does,
         * return nothing.
         */
        List<SpatialEntity<? extends IHazardEventEntityIdentifier>> spatialEntities = null;
        int index = -1;
        if (indicesForEventsForTypes.get(SpatialEntityType.UNSELECTED)
                .containsKey(eventIdentifier)) {
            spatialEntities = unselectedSpatialEntities;
            index = indicesForEventsForTypes.get(SpatialEntityType.UNSELECTED)
                    .get(eventIdentifier);
        } else if (indicesForEventsForTypes.get(SpatialEntityType.SELECTED)
                .containsKey(eventIdentifier)) {
            spatialEntities = selectedSpatialEntities;
            index = indicesForEventsForTypes.get(SpatialEntityType.SELECTED)
                    .get(eventIdentifier);
        }
        if (spatialEntities != null) {

            /*
             * Iterate through all entities in the list that go with this hazard
             * event, finding the first one that is or contains a polygon; if no
             * such entity is found for this hazard event, return nothing.
             */
            for (int j = index; j < spatialEntities.size(); j++) {
                SpatialEntity<? extends IHazardEventEntityIdentifier> spatialEntity = spatialEntities
                        .get(j);
                if (spatialEntity.getIdentifier().getEventIdentifier()
                        .equals(eventIdentifier) == false) {
                    break;
                }
                IAdvancedGeometry geometry = spatialEntity.getGeometry();
                if ((geometry instanceof GeometryWrapper)
                        && isOrContainsPolygon(((GeometryWrapper) geometry)
                                .getGeometry())) {
                    return spatialEntity;
                } else if (geometry instanceof AdvancedGeometryCollection) {
                    for (IAdvancedGeometry subGeometry : ((AdvancedGeometryCollection) geometry)
                            .getChildren()) {
                        if ((subGeometry instanceof GeometryWrapper)
                                && isOrContainsPolygon(((GeometryWrapper) subGeometry)
                                        .getGeometry())) {
                            return spatialEntity;
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Determine whether or not the specified selected time change should affect
     * the generation of spatial entities from visual features.
     * <p>
     * TODO: If visual features are ever altered to not key off of the lower
     * bound of the selected time only, this will need changing.
     * </p>
     * 
     * @param oldTime
     *            Old selected time.
     * @param newTime
     *            New selected time.
     * @return <code>true</code> if the selected time change affects the
     *         generation of spatial entities from visual features,
     *         <code>false</code> otherwise.
     */
    boolean isChangedSelectedTimeAffectingVisualFeatures(SelectedTime oldTime,
            SelectedTime newTime) {
        return ((oldTime == null) || (oldTime.getLowerBound() != newTime
                .getLowerBound()));
    }

    // Private Methods

    /**
     * Get the position and size of the sublist of spatial entities associated
     * with the specified event identifier within the specified entities list,
     * using the specified map to find the starting index.
     * 
     * @param eventIdentifier
     *            Event identifier.
     * @param indicesForEvents
     *            Map of event identifiers to the starting indices of any
     *            spatial entities associated with them in
     *            <code>spatialEntities</code>; <code>eventIdentifier</code> may
     *            or may not have a valid entry within this map.
     * @param spatialEntities
     *            List of spatial entities from which to get the position and
     *            size of the sublist, if any.
     * @return Position and size of the sublist, or <code>null</code> if there
     *         are no spatial entities within the list for this hazard event.
     */
    private Sublist getEventEntitySublistWithinList(
            String eventIdentifier,
            Map<String, Integer> indicesForEvents,
            List<? extends SpatialEntity<? extends IHazardEventEntityIdentifier>> spatialEntities) {
        if (indicesForEvents.containsKey(eventIdentifier)) {
            int count = 0;
            int startIndex = indicesForEvents.get(eventIdentifier);
            List<SpatialEntity<? extends IHazardEventEntityIdentifier>> sublist = new ArrayList<>();
            for (int j = startIndex; j < spatialEntities.size(); j++) {
                if (spatialEntities.get(j).getIdentifier().getEventIdentifier()
                        .equals(eventIdentifier) == false) {
                    break;
                }
                sublist.add(spatialEntities.get(j));
                count++;
            }
            return new Sublist(sublist, startIndex, count);
        }
        return null;
    }

    /**
     * Get a range map holding the specified index and offset, indicating that
     * indices at or beyond this one should be offset by the specified amount.
     * 
     * @param index
     *            Index from which point onward to apply the offset.
     * @param offset
     *            Offset.
     * @return Range map holding the specified index and offset.
     */
    private RangeMap<Integer, Integer> getOffsetsForIndicesRangeMap(int index,
            int offset) {
        RangeMap<Integer, Integer> offsetsForIndices = TreeRangeMap.create();
        offsetsForIndices.put(Range.atLeast(index), offset);
        return offsetsForIndices;
    }

    /**
     * For the specified type of spatial entity and using the specified list,
     * clear and repopulate the map associating event identifiers with the
     * indices of those events' first spatial entity in the specified list, as
     * well as repopulating the association of spatial entities with
     * identifiers.
     * 
     * @param type
     *            Type of spatial entities for which repopulation is required.
     *            Must be any value besides <code>null</code> or
     *            {@link SpatialEntityType#TOOL}, the latter because events are
     *            not associated with tool spatial entities.
     * @param spatialEntities
     *            Spatial entities to be used for this type.
     */
    private void repopulateEntityAssociationsAndIndicesForEvents(
            SpatialEntityType type,
            List<? extends SpatialEntity<? extends IHazardEventEntityIdentifier>> spatialEntities) {
        Map<String, Integer> indicesForEvents = indicesForEventsForTypes
                .get(type);
        indicesForEvents.clear();
        for (int j = 0; j < spatialEntities.size(); j++) {
            SpatialEntity<? extends IHazardEventEntityIdentifier> spatialEntity = spatialEntities
                    .get(j);
            IHazardEventEntityIdentifier identifier = spatialEntity
                    .getIdentifier();
            spatialEntitiesForIdentifiers.put(identifier, spatialEntity);
            String eventIdentifier = identifier.getEventIdentifier();
            if (indicesForEvents.containsKey(eventIdentifier) == false) {
                indicesForEvents.put(eventIdentifier, j);
            }
        }
    }

    /**
     * Add the specified spatial entities of the specified type, generated for
     * the hazard event at the specified index of the specified event list, to
     * the records.
     * 
     * @param type
     *            Type of spatial entities to be added. Must be any value
     *            besides <code>null</code> or {@link SpatialEntityType#TOOL},
     *            the latter because events are not associated with tool spatial
     *            entities.
     * @param spatialEntities
     *            Spatial entities to be added; may be an empty list.
     * @param events
     *            List of hazard events.
     * @param eventIndex
     *            Index of the event in <code>events</code> for which the
     *            spatial entities were generated.
     * @return <code>true</code> if an addition was made, that is, the entities
     *         list was not empty, <code>false</code> otherwise.
     */
    private <E extends SpatialEntity<? extends IEntityIdentifier>> boolean addEventSpatialEntities(
            SpatialEntityType type, List<E> spatialEntities,
            List<ObservedHazardEvent> events, int eventIndex) {
        if (spatialEntities.isEmpty() == false) {

            /*
             * Get the insertion index; if -1, the entities are to be appended
             * to the list. If not, then offset any following indices in the
             * hatching records to keep said indices relevant after the
             * insertion.
             */
            int insertionIndex = getInsertionIndexForEntities(type, events,
                    eventIndex);
            Map<String, Integer> indicesForEvents = indicesForEventsForTypes
                    .get(type);
            if (insertionIndex != -1) {
                RangeMap<Integer, Integer> offsetsForIndices = getOffsetsForIndicesRangeMap(
                        insertionIndex, spatialEntities.size());
                updateEntityIndicesMap(offsetsForIndices, indicesForEvents);
            }

            /*
             * Make an index entry for these hatching entities, make
             * associations, and add or insert the spatial entities as
             * appropriate.
             */
            indicesForEvents.put(events.get(eventIndex).getEventID(),
                    (insertionIndex == -1 ? spatialEntitiesForTypes.get(type)
                            .size() : insertionIndex));
            if (createAssociationsOfIdentifiersWithEntities(type,
                    spatialEntities)) {
                selectedSpatialEntityIdentifiersChanged();
            }
            if (insertionIndex == -1) {
                addSpatialEntities(type, spatialEntities);
            } else {
                insertSpatialEntities(type, insertionIndex, spatialEntities);
            }
            return true;
        }
        return false;
    }

    /**
     * Replace all spatial entities of the specified type associated with the
     * specified hazard event with the specified entities, generated for the
     * hazard event at the specified index of the specified event list, to the
     * records.
     * 
     * @param type
     *            Type of spatial entities to be replaced. Must be any value
     *            besides <code>null</code> or {@link SpatialEntityType#TOOL},
     *            the latter because events are not associated with tool spatial
     *            entities.
     * @param spatialEntities
     *            Spatial entities to be used as replacements; may be an empty
     *            list.
     * @param events
     *            List of hazard events.
     * @param eventIndex
     *            Index of the event in <code>events</code> for which the
     *            spatial entities were generated.
     * @param eventIdentifier
     *            Identifier of the event for which to replace spatial entities.
     * @param indexMayHaveChanged
     *            Flag indicating whether or not the index at which the old
     *            spatial entities are found, if any, may be different from the
     *            index at which the new spatial entities should be found
     *            (because the event changed its position in the event list).
     * @param checkForReuse
     *            Flag indicating whether or not the replacement spatial
     *            entities are the same as the ones being replaced, including
     *            their order and total number. If nothing has changed, then
     *            nothing will need replacement.
     * @return <code>true</code> if a replacement was made, that is, the
     *         entities list was not empty (and at least one entity changed)
     *         and/or there were existing entities of this type for this event
     *         that were removed, <code>false</code> otherwise.
     */
    @SuppressWarnings("unchecked")
    private <E extends SpatialEntity<? extends IHazardEventEntityIdentifier>> boolean replaceEventSpatialEntities(
            SpatialEntityType type, List<E> spatialEntities,
            List<ObservedHazardEvent> events, int eventIndex,
            String eventIdentifier, boolean indexMayHaveChanged,
            boolean checkForReuse) {

        /*
         * Get the sublist of entities within the main list for entities of this
         * type that is to be removed and replaced, if any.
         */
        boolean changed = true;
        Map<String, Integer> indicesForEvents = indicesForEventsForTypes
                .get(type);
        Sublist sublist = getEventEntitySublistWithinList(
                eventIdentifier,
                indicesForEvents,
                (List<? extends SpatialEntity<? extends IHazardEventEntityIdentifier>>) spatialEntitiesForTypes
                        .get(type));

        /*
         * If there is a sublist to replace and there are new entities, replace
         * the old ones with the new ones; otherwise, if there are no old ones
         * but there are new ones, insert the new ones; otherwise, if there are
         * old ones but not new ones, remove the old ones; and finally, if there
         * are neither new nor old ones, nothing will have changed.
         */
        if ((sublist != null) && (spatialEntities.isEmpty() == false)) {

            /*
             * Get the index at which the new entities are to be inserted, which
             * may differ from where the old entities were placed.
             */
            int newIndex = (indexMayHaveChanged ? getInsertionIndexForEntities(
                    type, events, eventIndex) : sublist.index);

            /*
             * If the insertion index has not changed, check for reuse should
             * occur, and the new and old lists hold exactly the same entities
             * in the same order, do nothing more, as no entities need
             * replacement (because all entities that were provided are being
             * reused, and no entities are being removed).
             */
            if (checkForReuse && (sublist.index == newIndex)
                    && doListsHoldSameReferences(sublist.list, spatialEntities)) {
                return false;
            }

            /*
             * Get the offsets for the old indices, which are more complicated
             * if the old and new insertion points are different. Then offset
             * any indices as appropriate.
             */
            RangeMap<Integer, Integer> offsetsForIndices = null;
            if (sublist.index != newIndex) {
                offsetsForIndices = TreeRangeMap.create();
                if (sublist.index + sublist.size < newIndex) {
                    offsetsForIndices.put(
                            Range.atLeast(sublist.index + sublist.size),
                            sublist.size * -1);
                    offsetsForIndices.put(Range.atLeast(newIndex),
                            spatialEntities.size() - sublist.size);
                } else {
                    offsetsForIndices.put(Range.atLeast(newIndex),
                            spatialEntities.size());
                    offsetsForIndices.put(
                            Range.atLeast(sublist.index + sublist.size),
                            spatialEntities.size() - sublist.size);
                }
            } else {
                if (spatialEntities.size() != sublist.size) {
                    offsetsForIndices = getOffsetsForIndicesRangeMap(
                            sublist.index + sublist.size,
                            spatialEntities.size() - sublist.size);
                }
            }
            if (offsetsForIndices != null) {
                updateEntityIndicesMap(offsetsForIndices, indicesForEvents);
            }

            /*
             * Remove the old associations of entity identifiers with their
             * entities, and put in the new ones.
             */
            boolean selectionSetChanged = removeAssociationsOfIdentifiersWithEntities(
                    type, sublist.list);
            selectionSetChanged |= createAssociationsOfIdentifiersWithEntities(
                    type, spatialEntities);
            if (selectionSetChanged) {
                selectedSpatialEntityIdentifiersChanged();
            }

            /*
             * If the new and old insertion indices for this event's spatial
             * entities are not the same, remove the old entities, and then
             * insert the new ones. Otherwise, just replace the old ones with
             * the new ones.
             */
            if (sublist.index != newIndex) {
                removeSpatialEntities(type, sublist.index, sublist.size);
                if (newIndex == -1) {
                    addSpatialEntities(type, spatialEntities);
                } else {
                    insertSpatialEntities(type, newIndex, spatialEntities);
                }
            } else {
                replaceSpatialEntities(type, sublist.index, sublist.size,
                        spatialEntities);
            }

        } else if (spatialEntities.isEmpty() == false) {
            addEventSpatialEntities(type, spatialEntities, events, eventIndex);

        } else if (sublist != null) {
            removeEventSpatialEntities(type, eventIdentifier, sublist,
                    indicesForEvents);

        } else {
            changed = false;
        }

        return changed;
    }

    /**
     * Remove all spatial entities of the specified type associated with the
     * specified hazard event from the records.
     * 
     * @param type
     *            Type of spatial entities to be removed. Must be any value
     *            besides <code>null</code> or {@link SpatialEntityType#TOOL},
     *            the latter because events are not associated with tool spatial
     *            entities.
     * @param eventIdentifier
     *            Identifier of the event for which to remove spatial entities.
     * @return <code>true</code> if one or more entities were removed,
     *         <code>false</code> otherwise.
     */
    @SuppressWarnings("unchecked")
    private boolean removeEventSpatialEntities(SpatialEntityType type,
            String eventIdentifier) {

        /*
         * Get information about the sublist of entities associated with this
         * event that are to be removed, if any.
         */
        Map<String, Integer> indicesForEvents = indicesForEventsForTypes
                .get(type);
        Sublist sublist = getEventEntitySublistWithinList(
                eventIdentifier,
                indicesForEvents,
                (List<? extends SpatialEntity<? extends IHazardEventEntityIdentifier>>) spatialEntitiesForTypes
                        .get(type));

        /*
         * If a sublist is found, remove all its elements.
         */
        if (sublist != null) {
            removeEventSpatialEntities(type, eventIdentifier, sublist,
                    indicesForEvents);
            return true;
        }
        return false;
    }

    /**
     * Remove the specified spatial entities sublist of the specified type
     * associated with the specified hazard event from the records.
     * 
     * @param type
     *            Type of spatial entities to be removed. Must be any value
     *            besides <code>null</code> or {@link SpatialEntityType#TOOL},
     *            the latter because events are not associated with tool spatial
     *            entities.
     * @param eventIdentifier
     *            Identifier of the event for which to remove spatial entities.
     * @param sublist
     *            Information about the sublist of spatial entities to be
     *            removed from the main list; must not be <code>null</code>.
     * @param indicesForEvents
     *            Map of event identifiers to spatial entity indices that is to
     *            be updated with new offsets as appropriate.
     */
    private void removeEventSpatialEntities(SpatialEntityType type,
            String eventIdentifier, Sublist sublist,
            Map<String, Integer> indicesForEvents) {
        RangeMap<Integer, Integer> offsetsForIndices = getOffsetsForIndicesRangeMap(
                sublist.index + sublist.size, sublist.size * -1);
        updateEntityIndicesMap(offsetsForIndices, indicesForEvents);
        indicesForEvents.remove(eventIdentifier);
        if (removeAssociationsOfIdentifiersWithEntities(type, sublist.list)) {
            selectedSpatialEntityIdentifiersChanged();
        }
        removeSpatialEntities(type, sublist.index, sublist.size);
    }

    /**
     * Replace any existing spatial entities representing visual features
     * created by tools for the collection of spatial information from the user
     * with ones created from the specified visual features list.
     * 
     * @param visualFeaturesList
     *            List of visual features provided by a tool for the collection
     *            of spatial information from the user, from which spatial
     *            entities are to be generated.
     * @param toolType
     *            Type of the tool that generated the visual features.
     * @param toolIdentifier
     *            Identifier of the tool that generated the visual features.
     * @param selectedTime
     *            Current selected time, needed to generate the spatial entities
     *            since the visual features may have temporally variant (and
     *            thus dependent upon the selected time) properties.
     * @return <code>true</code> if a change in spatial entities occurred,
     *         <code>false</code> otherwise.
     */
    @SuppressWarnings("unchecked")
    private boolean replaceToolSpatialEntities(
            VisualFeaturesList visualFeaturesList, final ToolType toolType,
            final String toolIdentifier, Date selectedTime) {

        /*
         * Remove records of previous tool spatial entities, except for the
         * associations between the previous entities and their identifiers;
         * these may only be removed now if there are no new visual features for
         * tools. If the latter is not the case, then the old spatial entities
         * may need to be reused, and thus the associations are only removed
         * after the calls to getStateAtTime() below.
         */
        boolean changed = false;
        List<SpatialEntity<ToolVisualFeatureEntityIdentifier>> oldToolSpatialEntities = new ArrayList<>(
                toolSpatialEntities);
        if (toolSpatialEntities.isEmpty() == false) {
            clearSpatialEntities(SpatialEntityType.TOOL);
            if (visualFeaturesList == null) {
                removeAssociationsOfIdentifiersWithEntities(
                        SpatialEntityType.TOOL, toolSpatialEntities);
            }
            changed = true;
        }

        /*
         * If there are visual features in the list, iterate through them,
         * creating spatial entities for any that are visible at the selected
         * time.
         */
        List<SpatialEntity<ToolVisualFeatureEntityIdentifier>> addedSpatialEntities = null;
        if (visualFeaturesList != null) {

            /*
             * Iterate through the visual features, adding spatial entities
             * generated for each in turn to the provided list. Any spatial
             * entities that are marked as "topmost" in the z-ordering are added
             * to another list.
             * 
             * TODO: If visual features are ever altered to not key off of the
             * lower bound of the selected time only, this will need changing.
             */
            List<SpatialEntity<ToolVisualFeatureEntityIdentifier>> topmostSpatialEntities = null;
            for (VisualFeature visualFeature : visualFeaturesList) {
                ToolVisualFeatureEntityIdentifier identifier = new ToolVisualFeatureEntityIdentifier(
                        toolType, toolIdentifier, visualFeature.getIdentifier());
                SpatialEntity<ToolVisualFeatureEntityIdentifier> entity = visualFeature
                        .getStateAtTime(
                                (SpatialEntity<ToolVisualFeatureEntityIdentifier>) spatialEntitiesForIdentifiers
                                        .get(identifier), identifier,
                                selectedTime);
                if (entity != null) {
                    if (entity.isTopmost()) {
                        if (topmostSpatialEntities == null) {
                            topmostSpatialEntities = new ArrayList<>();
                        }
                        topmostSpatialEntities.add(entity);
                    } else {
                        if (addedSpatialEntities == null) {
                            addedSpatialEntities = new ArrayList<>();
                        }
                        addedSpatialEntities.add(entity);
                    }
                }
            }

            /*
             * If at least one topmost spatial entity was generated, append all
             * topmost ones to the main list so that the former end up drawn
             * last, and thus staying on the top of the z-order. (If no non-
             * topmost spatial entities were created, just use the topmost list
             * as the total list).
             */
            if (topmostSpatialEntities != null) {
                if (addedSpatialEntities == null) {
                    addedSpatialEntities = topmostSpatialEntities;
                } else {
                    addedSpatialEntities.addAll(topmostSpatialEntities);
                }
            }

            /*
             * If there were tool spatial entities before this invocation,
             * remove any associations between them and their identifiers. This
             * was not done earlier (at the top of this method) because these
             * associations were needed in order to reuse old spatial entities
             * when calling getStateAtTime() above.
             */
            if (changed) {
                removeAssociationsOfIdentifiersWithEntities(
                        SpatialEntityType.TOOL, toolSpatialEntities);
            }

            /*
             * If spatial entities were generated, associate them with the tool.
             * Determine whether a change occurred by comparing the new list
             * with the old one.
             */
            if (addedSpatialEntities != null) {
                createAssociationsOfIdentifiersWithEntities(
                        SpatialEntityType.TOOL, addedSpatialEntities);
                addSpatialEntities(SpatialEntityType.TOOL, addedSpatialEntities);
                changed = (doListsHoldSameReferences(oldToolSpatialEntities,
                        toolSpatialEntities) == false);
            }
        }
        return changed;
    }

    /**
     * Determine whether or not the two specified lists hold the same references
     * for their elements, in the same order.
     * 
     * @param first
     *            First list to be compared.
     * @param second
     *            Second list to be compared.
     * @return <code>true</code> if the two lists hold the same references in
     *         the same order, <code>false</code> otherwise.
     */
    private boolean doListsHoldSameReferences(List<?> first, List<?> second) {
        if (first.size() != second.size()) {
            return false;
        }
        for (int j = 0; j < first.size(); j++) {
            if (first.get(j) != second.get(j)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Clear the spatial entities list associated with the specified type.
     * 
     * @param type
     *            Type of spatial entities to be cleared.
     */
    private void clearSpatialEntities(SpatialEntityType type) {
        spatialEntitiesForTypes.get(type).clear();
        view.getSpatialEntitiesChanger().clear(type);
    }

    /**
     * Set the spatial entities list associated with the specified type to have
     * the specified contents.
     * 
     * @param type
     *            Type of spatial entities to be set.
     * @param entities
     *            Entities to be used as the contents of the list.
     */
    @SuppressWarnings("unchecked")
    private <E extends SpatialEntity<? extends IEntityIdentifier>> void setSpatialEntities(
            SpatialEntityType type, List<E> entities) {
        List<E> spatialEntities = (List<E>) spatialEntitiesForTypes.get(type);
        spatialEntities.clear();
        spatialEntities.addAll(entities);
        view.getSpatialEntitiesChanger().set(type, entities);
    }

    /**
     * Add the specified spatial entities to the end of the list associated with
     * the specified type.
     * 
     * @param type
     *            Type of spatial entities to be added.
     * @param entities
     *            Entities to be added.
     */
    @SuppressWarnings("unchecked")
    private <E extends SpatialEntity<? extends IEntityIdentifier>> void addSpatialEntities(
            SpatialEntityType type, List<E> entities) {
        ((List<E>) spatialEntitiesForTypes.get(type)).addAll(entities);
        view.getSpatialEntitiesChanger().addElements(type, entities);
    }

    /**
     * Insert the specified spatial entities into the list associated with the
     * specified type at the specified index.
     * 
     * @param type
     *            Type of spatial entities to be inserted.
     * @param index
     *            Index at which to start the insertion.
     * @param entities
     *            Entities to be inserted.
     */
    @SuppressWarnings("unchecked")
    private <E extends SpatialEntity<? extends IEntityIdentifier>> void insertSpatialEntities(
            SpatialEntityType type, int index, List<E> entities) {
        List<E> spatialEntities = (List<E>) spatialEntitiesForTypes.get(type);
        if (index == spatialEntities.size()) {
            addSpatialEntities(type, entities);
        } else {
            spatialEntities.addAll(index, entities);
            view.getSpatialEntitiesChanger().insertElements(type, index,
                    entities);
        }
    }

    /**
     * Replace the specified number of spatial entities starting at the
     * specified index in the list associated with the specified type with the
     * specified entities.
     * 
     * @param type
     *            Type of spatial entities to be replaced.
     * @param index
     *            Index at which to start replacement.
     * @param count
     *            Number of entities to be removed.
     * @param entities
     *            Spatial entities to be inserted at <code>index</code>.
     */
    private <E extends SpatialEntity<? extends IEntityIdentifier>> void replaceSpatialEntities(
            SpatialEntityType type, int index, int count, List<E> entities) {
        removeSpatialEntities(type, index, count);
        insertSpatialEntities(type, index, entities);
    }

    /**
     * Remove the specified number of spatial entities starting at the specified
     * index from the list associated with the specified type.
     * 
     * @param type
     *            Type of spatial entities to be removed.
     * @param index
     *            Index at which to start removal.
     * @param count
     *            Number of entities to be removed.
     */
    private void removeSpatialEntities(SpatialEntityType type, int index,
            int count) {
        List<? extends SpatialEntity<? extends IEntityIdentifier>> spatialEntities = spatialEntitiesForTypes
                .get(type);
        for (int j = 0; j < count; j++) {
            spatialEntities.remove(index);
        }
        view.getSpatialEntitiesChanger().removeElements(type, index, count);
    }

    /**
     * Notify the view that the selected spatial entity identifiers set has
     * changed.
     */
    private void selectedSpatialEntityIdentifiersChanged() {
        view.getSelectedSpatialEntityIdentifiersChanger().setState(null,
                selectedSpatialEntityIdentifiers);
    }

    /**
     * Get the selected time for use in generating spatial entities from visual
     * features.
     * 
     * @return Selected time.
     */
    private Date getSelectedTimeForVisualFeatures() {
        return new Date(sessionManager.getTimeManager().getSelectedTime()
                .getLowerBound());
    }

    /**
     * Create spatial entities as necessary for the specified hazard event,
     * adding each such created entity to one of the three specified lists,
     * depending upon whether said entity is for hatching, an unselected event,
     * or a selected event.
     * 
     * @param event
     *            Event for which to create spatial entities.
     * @param hatchingSpatialEntities
     *            List to which to add any entities generated to represent
     *            hatching.
     * @param unselectedSpatialEntities
     *            List to which to add any entities generated to represent
     *            non-hatching visuals for the event if the latter is
     *            unselected.
     * @param selectedSpatialEntities
     *            List to which to add any entities generated to represent
     *            non-hatching visuals for the event if the latter is selected.
     * @param selectedRange
     *            Selected time range.
     * @param selectedTime
     *            Lower bound of the selected time, rounded to be suitable for
     *            generating spatial entities from visual features.
     * @param hazardTypes
     *            Information about all possible hazard types.
     * @param hatching
     *            Flag indicating whether or not hatching is to be shown.
     * @return Creation result, indicating whether any spatial entities were
     *         created (or reused) for the specified event, and if the former is
     *         <code>true</code>, whether all the entities created or reused for
     *         the different spatial entity types were reused.
     */
    private CreationResult createSpatialEntitiesForEvent(
            ObservedHazardEvent event,
            List<SpatialEntity<? extends IHazardEventEntityIdentifier>> hatchingSpatialEntities,
            List<SpatialEntity<? extends IHazardEventEntityIdentifier>> unselectedSpatialEntities,
            List<SpatialEntity<? extends IHazardEventEntityIdentifier>> selectedSpatialEntities,
            SelectedTime selectedRange, Date selectedTime,
            HazardTypes hazardTypes, boolean hatching) {
        ISessionConfigurationManager<ObservedSettings> configManager = sessionManager
                .getConfigurationManager();
        ISessionEventManager<ObservedHazardEvent> eventManager = sessionManager
                .getEventManager();

        /*
         * Get the visual features list; if it is found to be non-empty, or it
         * is empty but the event's time range intersects the current selected
         * time range, generate spatial entities for the event.
         */
        boolean created = false;
        Map<SpatialEntityType, Boolean> reusedForTypes = null;
        VisualFeaturesList visualFeaturesList = event.getVisualFeatures();
        if ((visualFeaturesList != null) && visualFeaturesList.isEmpty()) {
            visualFeaturesList = null;
        }
        boolean inTimeRange = selectedRange.intersects(event.getStartTime()
                .getTime(), event.getEndTime().getTime());
        if ((visualFeaturesList != null) || inTimeRange) {

            /*
             * Compile the various display properties that will be needed by any
             * spatial entity that indicates it must look like the base geometry
             * for this hazard event in one or more ways.
             */
            final String eventIdentifier = event.getEventID();
            boolean selected = selectedEventIdentifiers
                    .contains(eventIdentifier);
            Color hazardColor = configManager.getColor(event);
            double hazardBorderWidth = configManager.getBorderWidth(event,
                    selected);
            LineStyle lineStyle = configManager.getBorderStyle(event);
            BorderStyle hazardBorderStyle = (lineStyle == LineStyle.DOTTED ? BorderStyle.DOTTED
                    : (lineStyle == LineStyle.DASHED ? BorderStyle.DASHED
                            : BorderStyle.SOLID));
            double hazardPointDiameter = (selected ? HAZARD_EVENT_POINT_SELECTED_DIAMETER
                    : HAZARD_EVENT_POINT_DIAMETER);
            String hazardLabel = getHazardLabel(event);

            /*
             * If hatching is showing and the selected time is within the
             * event's time range, determine whether or not this event requires
             * hatching, and if so, create a spatial entity for it.
             */
            if (hatching && inTimeRange) {
                String hazardType = HazardEventUtilities.getHazardType(event);
                if (hazardType != null) {
                    HazardTypeEntry hazardTypeEntry = hazardTypes
                            .get(hazardType);
                    if (hazardTypeEntry.getHatchingStyle() != HatchingStyle.NONE) {
                        CreatedSpatialEntities entities = createSpatialEntitiesForEventHatching(
                                event, hazardTypeEntry, hazardColor,
                                HAZARD_EVENT_MULTI_POINT_TEXT_OFFSET_LENGTH,
                                HAZARD_EVENT_MULTI_POINT_TEXT_OFFSET_DIRECTION,
                                HAZARD_EVENT_FONT_POINT_SIZE);
                        created = (entities.spatialEntities.isEmpty() == false);
                        reusedForTypes = createAndPut(reusedForTypes,
                                SpatialEntityType.HATCHING,
                                (created && entities.allReused));
                        hatchingSpatialEntities
                                .addAll(entities.spatialEntities);
                    }
                }
            }

            /*
             * Generate spatial entities if there are visual features;
             * otherwise, generate a single spatial entity representing the
             * hazard event.
             */
            if (visualFeaturesList != null) {

                /*
                 * Iterate through the visual features, creating spatial
                 * entities for any that are visible at the selected time. Any
                 * such created spatial entities are added to either the list
                 * for unselected hazard events or selected hazard events,
                 * depending upon whether the event with which the entity is
                 * associated is selected or not.
                 * 
                 * TODO: If visual features are ever altered to not key off of
                 * the lower bound of the selected time only, this will need
                 * changing.
                 */
                CreatedSpatialEntities entities = createSpatialEntitiesForEventVisualFeatures(
                        visualFeaturesList, eventIdentifier, selected,
                        selectedTime, hazardColor, hazardBorderWidth,
                        hazardBorderStyle, hazardPointDiameter, hazardLabel,
                        HAZARD_EVENT_SINGLE_POINT_TEXT_OFFSET_LENGTH,
                        HAZARD_EVENT_SINGLE_POINT_TEXT_OFFSET_DIRECTION,
                        HAZARD_EVENT_MULTI_POINT_TEXT_OFFSET_LENGTH,
                        HAZARD_EVENT_MULTI_POINT_TEXT_OFFSET_DIRECTION,
                        HAZARD_EVENT_FONT_POINT_SIZE);
                (selected ? selectedSpatialEntities : unselectedSpatialEntities)
                        .addAll(entities.spatialEntities);
                if (entities.spatialEntities.isEmpty() == false) {
                    created = true;
                    reusedForTypes = createAndPut(reusedForTypes,
                            (selected ? SpatialEntityType.SELECTED
                                    : SpatialEntityType.UNSELECTED),
                            entities.allReused);
                }
            } else {
                boolean editable = eventManager.canEventAreaBeChanged(event);
                CreatedSpatialEntity entity = createDefaultSpatialEntityForEvent(
                        event, hazardColor, hazardBorderWidth,
                        hazardBorderStyle, hazardPointDiameter, hazardLabel,
                        HAZARD_EVENT_SINGLE_POINT_TEXT_OFFSET_LENGTH,
                        HAZARD_EVENT_SINGLE_POINT_TEXT_OFFSET_DIRECTION,
                        HAZARD_EVENT_MULTI_POINT_TEXT_OFFSET_LENGTH,
                        HAZARD_EVENT_MULTI_POINT_TEXT_OFFSET_DIRECTION,
                        HAZARD_EVENT_FONT_POINT_SIZE, editable,
                        (editable && selected), (editable && selected));
                if (selected) {
                    selectedSpatialEntities.add(entity.spatialEntity);
                    reusedForTypes = createAndPut(reusedForTypes,
                            SpatialEntityType.SELECTED, entity.reused);
                } else {
                    unselectedSpatialEntities.add(entity.spatialEntity);
                    reusedForTypes = createAndPut(reusedForTypes,
                            SpatialEntityType.UNSELECTED, entity.reused);
                }
                created = true;
            }
        }
        return (created == false ? EMPTY_CREATION_RESULT : new CreationResult(
                created, reusedForTypes));
    }

    /**
     * Create a map is one is not provided, and put the specified key-value pair
     * into it.
     * 
     * @param map
     *            Map into which to insert the key-value pair; if
     *            <code>null</code>, a new map will be created.
     * @param key
     *            Key to insert.
     * @param value
     *            Value to insert.
     * @return Map that was provided, or a new map if none was provided.
     */
    private Map<SpatialEntityType, Boolean> createAndPut(
            Map<SpatialEntityType, Boolean> map, SpatialEntityType key,
            Boolean value) {
        if (map == null) {
            map = new EnumMap<>(SpatialEntityType.class);
        }
        map.put(key, value);
        return map;
    }

    /**
     * Create spatial entities representing the specified hazard event's
     * hatching areas.
     * 
     * @param event
     *            Hazard event for which the hatching entities are to be
     *            created.
     * @param hazardTypeEntry
     *            Hazard type information.
     * @param hazardColor
     *            Color of the hazard event.
     * @param textOffsetLength
     *            Distance in pixels to offset any generated text entities from
     *            the centroid of a hatching shape.
     * @param textOffsetDirection
     *            Direction in degrees (with 0 being east and 90 being north) of
     *            the offset specified by <code>textOffsetLength</code> for any
     *            generated text entities from the centroid of a hatching shape.
     * @param hazardTextPointSize
     *            Point size of the font for any text entities.
     * @return List of spatial entities created, and flag indicating whether or
     *         not all the entities in the list were reused instead of created;
     *         may be an empty list.
     */
    @SuppressWarnings("unchecked")
    private CreatedSpatialEntities createSpatialEntitiesForEventHatching(
            IHazardEvent event, HazardTypeEntry hazardTypeEntry,
            Color hazardColor, double textOffsetLength,
            double textOffsetDirection, int hazardTextPointSize) {

        /*
         * Get the mapping of UGC identifiers to geometry data describing the
         * areas to be hatched.
         */
        Map<String, IGeometryData> geometryDataForUgcs = geoMapUtilities
                .buildHazardAreaForEvent(event);

        /*
         * Iterate through the mapping entries, handling each one's geometry in
         * turn if appropriate.
         */
        boolean allReused = true;
        List<SpatialEntity<? extends IHazardEventEntityIdentifier>> spatialEntities = null;
        for (Map.Entry<String, IGeometryData> entry : geometryDataForUgcs
                .entrySet()) {

            /*
             * Only create a spatial entity if the geometry is polygonal.
             */
            IGeometryData geometryData = entry.getValue();
            if ((geometryData != null)
                    && (geometryData.getGeometry() instanceof Polygonal)) {

                /*
                 * If the hatching is WarnGen-style, show a label indicating the
                 * significance of the event.
                 */
                String significance = event.getSignificance();
                boolean showLabel = ((hazardTypeEntry.getHatchingStyle() == HatchingStyle.WARNGEN)
                        && (significance != null) && (significance.isEmpty() == false));

                /*
                 * Build the spatial entity, reusing any previously built entity
                 * with the same identifier for efficiency's sake if this build
                 * requires no changes.
                 */
                HazardEventHatchingEntityIdentifier identifier = new HazardEventHatchingEntityIdentifier(
                        event.getEventID(), entry.getKey());
                SpatialEntity<HazardEventHatchingEntityIdentifier> oldEntity = (SpatialEntity<HazardEventHatchingEntityIdentifier>) spatialEntitiesForIdentifiers
                        .get(identifier);
                SpatialEntity<HazardEventHatchingEntityIdentifier> entity = SpatialEntity
                        .build(oldEntity, identifier, AdvancedGeometryUtilities
                                .createGeometryWrapper(
                                        geometryData.getGeometry(), 0), null,
                                hazardColor, 0.0, BorderStyle.SOLID,
                                FillStyle.HATCHED, 0.0, null,
                                (showLabel ? significance : null), 0.0, 0.0,
                                textOffsetLength, textOffsetDirection,
                                hazardTextPointSize, hazardColor,
                                DragCapability.NONE, false, false, false, false);
                if (spatialEntities == null) {
                    spatialEntities = new ArrayList<>();
                }
                spatialEntities.add(entity);
                if (oldEntity != entity) {
                    allReused = false;
                }
            }
        }
        return (spatialEntities == null ? EMPTY_CREATED_SPATIAL_ENTITIES
                : new CreatedSpatialEntities(spatialEntities, allReused));
    }

    /**
     * Create a spatial entity representing the specified hazard event's base
     * geometry.
     * 
     * @param event
     *            Hazard event for which spatial entity is to be created.
     * @param hazardColor
     *            Color of the hazard event.
     * @param hazardBorderWidth
     *            Border width of the hazard event.
     * @param hazardBorderStyle
     *            Border style of the hazard event.
     * @param hazardPointDiameter
     *            Diameter of the hazard event if it has a puntal geometry.
     * @param hazardLabel
     *            Text label of the hazard event.
     * @param hazardSinglePointTextOffsetLength
     *            Distance in pixels to offset any generated text label from the
     *            center of the geometry if the latter is a point.
     * @param hazardSinglePointTextOffsetDirection
     *            Direction in degrees (with 0 being east and 90 being north) of
     *            the offset specified by
     *            <code>hazardSinglePointTextOffsetLength</code> for any
     *            generated text label from the center of the geometry if the
     *            latter is a point.
     * @param hazardMultiPointTextOffsetLength
     *            Distance in pixels to offset any generated text label from the
     *            centroid of the geometry if the latter is a line or polygon.
     * @param hazardMultiPointTextOffsetDirection
     *            Direction in degrees (with 0 being east and 90 being north) of
     *            the offset specified by
     *            <code>hazardMultiPointTextOffsetLength</code> for any
     *            generated text label from the centroid of the geometry if the
     *            latter is a line or polygon.
     * @param hazardTextPointSize
     *            Point size of the font for any text label.
     * @param editable
     *            Flag indicating whether or not the entity is to be editable or
     *            movable.
     * @param rotatable
     *            Flag indicating whether or not the entity is to be rotatable.
     * @param scaleable
     *            Flag indicating whether or not the entity is to be scaleable.
     * @return Spatial entity created or reused, and flag indicating whether or
     *         not said entity was reused instead of created.
     */
    @SuppressWarnings("unchecked")
    private CreatedSpatialEntity createDefaultSpatialEntityForEvent(
            IHazardEvent event, Color hazardColor, double hazardBorderWidth,
            BorderStyle hazardBorderStyle, double hazardPointDiameter,
            String hazardLabel, double hazardSinglePointTextOffsetLength,
            double hazardSinglePointTextOffsetDirection,
            double hazardMultiPointTextOffsetLength,
            double hazardMultiPointTextOffsetDirection,
            int hazardTextPointSize, boolean editable, boolean rotatable,
            boolean scaleable) {
        IHazardEventEntityIdentifier identifier = new HazardEventEntityIdentifier(
                event.getEventID());
        SpatialEntity<IHazardEventEntityIdentifier> oldEntity = (SpatialEntity<IHazardEventEntityIdentifier>) spatialEntitiesForIdentifiers
                .get(identifier);
        SpatialEntity<? extends IHazardEventEntityIdentifier> entity = SpatialEntity
                .build(oldEntity, identifier, getProcessedBaseGeometry(event),
                        hazardColor, TRANSPARENT_COLOR, hazardBorderWidth,
                        hazardBorderStyle, FillStyle.SOLID,
                        hazardPointDiameter, SymbolShape.CIRCLE, hazardLabel,
                        hazardSinglePointTextOffsetLength,
                        hazardSinglePointTextOffsetDirection,
                        hazardMultiPointTextOffsetLength,
                        hazardMultiPointTextOffsetDirection,
                        hazardTextPointSize, hazardColor,
                        (editable ? DragCapability.ALL : DragCapability.NONE),
                        false, rotatable, scaleable, false);
        return new CreatedSpatialEntity(entity, (entity == oldEntity));
    }

    /**
     * Create spatial entities for the specified visual features that decorate a
     * hazard event.
     * 
     * @param visualFeaturesList
     *            List of visual features that is to be used to generate any
     *            spatial entities.
     * @param eventIdentifier
     *            Identifier of the event that the visual features represent.
     * @param selected
     *            Flag indicating whether or not the hazard event is currently
     *            selected.
     * @param selectedTime
     *            Current selected time, needed to generate the spatial entities
     *            since the visual features may have temporally variant (and
     *            thus dependent upon the selected time) properties.
     * @param hazardColor
     *            Color of the hazard event.
     * @param hazardBorderWidth
     *            Border width of the hazard event.
     * @param hazardBorderStyle
     *            Border style of the hazard event.
     * @param hazardPointDiameter
     *            Diameter of the hazard event if it has a puntal geometry.
     * @param hazardLabel
     *            Text label of the hazard event.
     * @param hazardSinglePointTextOffsetLength
     *            Hazard event's distance in pixels to offset any generated text
     *            label from the center of the geometry if the latter is a
     *            point.
     * @param hazardSinglePointTextOffsetDirection
     *            Hazard event's direction in degrees (with 0 being east and 90
     *            being north) of the offset specified by
     *            <code>hazardSinglePointTextOffsetLength</code> for any
     *            generated text label from the center of the geometry if the
     *            latter is a point.
     * @param hazardMultiPointTextOffsetLength
     *            Hazard event's distance in pixels to offset any generated text
     *            label from the centroid of the geometry if the latter is a
     *            line or polygon.
     * @param hazardMultiPointTextOffsetDirection
     *            Hazard event's direction in degrees (with 0 being east and 90
     *            being north) of the offset specified by
     *            <code>hazardMultiPointTextOffsetLength</code> for any
     *            generated text label from the centroid of the geometry if the
     *            latter is a line or polygon.
     * @param hazardTextPointSize
     *            Point size of the font for any text label.
     * @return List of spatial entities created, and flag indicating whether or
     *         not all the entities in the list were reused instead of created;
     *         may be an empty list.
     */
    @SuppressWarnings("unchecked")
    private CreatedSpatialEntities createSpatialEntitiesForEventVisualFeatures(
            VisualFeaturesList visualFeaturesList,
            final String eventIdentifier, boolean selected, Date selectedTime,
            Color hazardColor, double hazardBorderWidth,
            BorderStyle hazardBorderStyle, double hazardPointDiameter,
            String hazardLabel, double hazardSinglePointTextOffsetLength,
            double hazardSinglePointTextOffsetDirection,
            double hazardMultiPointTextOffsetLength,
            double hazardMultiPointTextOffsetDirection, int hazardTextPointSize) {

        /*
         * If there are visual features in the list, iterate through them,
         * creating spatial entities for any that are visible at the selected
         * time.
         */
        List<SpatialEntity<? extends IHazardEventEntityIdentifier>> addedSpatialEntities = null;
        boolean allReused = true;
        if (visualFeaturesList != null) {

            /*
             * Iterate through the visual features, adding spatial entities
             * generated for each in turn to the provided list. Any spatial
             * entities that are marked as "topmost" in the z-ordering are added
             * to another list.
             */
            List<SpatialEntity<? extends IHazardEventEntityIdentifier>> topmostSpatialEntities = null;
            for (VisualFeature visualFeature : visualFeaturesList) {
                HazardEventVisualFeatureEntityIdentifier identifier = new HazardEventVisualFeatureEntityIdentifier(
                        eventIdentifier, visualFeature.getIdentifier());
                SpatialEntity<HazardEventVisualFeatureEntityIdentifier> oldEntity = (SpatialEntity<HazardEventVisualFeatureEntityIdentifier>) spatialEntitiesForIdentifiers
                        .get(identifier);
                SpatialEntity<? extends IHazardEventEntityIdentifier> entity = visualFeature
                        .getStateAtTime(oldEntity, identifier, selected,
                                selectedTime, hazardColor, hazardBorderWidth,
                                hazardBorderStyle, hazardPointDiameter,
                                hazardLabel, hazardSinglePointTextOffsetLength,
                                hazardSinglePointTextOffsetDirection,
                                hazardMultiPointTextOffsetLength,
                                hazardMultiPointTextOffsetDirection,
                                hazardTextPointSize);
                if (entity != null) {
                    if (entity != oldEntity) {
                        allReused = false;
                    }
                    if (entity.isTopmost()) {
                        if (topmostSpatialEntities == null) {
                            topmostSpatialEntities = new ArrayList<>();
                        }
                        topmostSpatialEntities.add(entity);
                    } else {
                        if (addedSpatialEntities == null) {
                            addedSpatialEntities = new ArrayList<>(
                                    visualFeaturesList.size());
                        }
                        addedSpatialEntities.add(entity);
                    }
                }
            }

            /*
             * If at least one topmost spatial entity was generated, append all
             * topmost ones to the main list so that the former end up drawn
             * last, and thus staying on the top of the z-order.
             */
            if (topmostSpatialEntities != null) {
                if (addedSpatialEntities == null) {
                    addedSpatialEntities = topmostSpatialEntities;
                } else {
                    addedSpatialEntities.addAll(topmostSpatialEntities);
                }
            }
        }
        return (addedSpatialEntities == null ? EMPTY_CREATED_SPATIAL_ENTITIES
                : new CreatedSpatialEntities(addedSpatialEntities, allReused));
    }

    /**
     * Get the index of the point within the spatial entities list associated
     * with the specified type within {@link #spatialEntitiesForTypes} at which
     * to insert spatial entities for the event at the specified index within
     * the specified events list.
     * 
     * @param type
     *            Type of spatial entities for which insertion is required. Must
     *            be any non-null value besides {@link SpatialEntityType#TOOL},
     *            the latter because events are not associated with tool spatial
     *            entities.
     * @param events
     *            List of events.
     * @param eventIndex
     *            Index of the event within <code>events</code> for which
     *            hatching spatial entities are to be inserted.
     * @return Index of the insertion point, or <code>-1</code> if the entities
     *         should instead be appended.
     */
    private int getInsertionIndexForEntities(SpatialEntityType type,
            List<ObservedHazardEvent> events, int eventIndex) {

        /*
         * The index at which to insert the entities is the one where entities
         * of this type reside for the next event after this one. If none is
         * found, then return -1, indicating that the entities are to be added
         * at the end of the list.
         */
        Map<String, Integer> indicesForEvents = indicesForEventsForTypes
                .get(type);
        int nextEntityIndex = -1;
        for (int j = eventIndex + 1; j < events.size(); j++) {
            String eventIdentifier = events.get(j).getEventID();
            if (indicesForEvents.containsKey(eventIdentifier)) {
                nextEntityIndex = indicesForEvents.get(eventIdentifier);
                break;
            }
        }
        return nextEntityIndex;
    }

    /**
     * Update the the specified map of event identifiers to spatial entity
     * indices by offsetting its indices as specified.
     * <p>
     * As an example, suppose the range map provided as a parameter associates
     * all values between 3 and 9 inclusive with -2, and all values 10 and above
     * with 4. Using these parameters will cause this method to alter some
     * indices recorded in the specified map of event identifiers to indices.
     * Any recorded index with a value between 3 and 9 inclusive will have -2
     * added to it, while any such index with a value of 10 or greater will have
     * 4 added to it.
     * 
     * @param offsetsForIndices
     *            Range map pairing ranges of indices with the offsets that
     *            should be applied to them. Not all indices will have entries
     *            within this range map.
     * @param indicesForEvents
     *            Map of event identifiers to spatial entity indices that is to
     *            be updated with new offsets as appropriate.
     */
    private void updateEntityIndicesMap(
            RangeMap<Integer, Integer> offsetsForIndices,
            Map<String, Integer> indicesForEvents) {
        for (Map.Entry<String, Integer> entry : indicesForEvents.entrySet()) {
            int index = entry.getValue();
            Integer offset = offsetsForIndices.get(index);
            if (offset != null) {
                entry.setValue(index + offset);
            }
        }
    }

    /**
     * Create associations between the specified spatial entity identifiers and
     * their entities, as well as adding the entities to the selected set if
     * they are of the appropriate type.
     * 
     * @param type
     *            Type of spatial entities for which to create associations; if
     *            {@link SpatialEntityType#SELECTED}, they will be added to the
     *            selected set as well.
     * @param entities
     *            Spatial entities for which to create associations.
     * @return <code>true</code> if associations were created,
     *         <code>false</code> otherwise.
     */
    private boolean createAssociationsOfIdentifiersWithEntities(
            SpatialEntityType type,
            List<? extends SpatialEntity<? extends IEntityIdentifier>> entities) {
        if (entities != null) {
            for (SpatialEntity<? extends IEntityIdentifier> entity : entities) {
                spatialEntitiesForIdentifiers.put(entity.getIdentifier(),
                        entity);
            }
            if (type == SpatialEntityType.SELECTED) {
                for (SpatialEntity<? extends IEntityIdentifier> entity : entities) {
                    selectedSpatialEntityIdentifiers
                            .add(entity.getIdentifier());
                }
            }
            return (entities.isEmpty() == false);
        }
        return false;
    }

    /**
     * Remove any associations between the specified entity identifiers and
     * their entities, as well as records of the former being selected if they
     * are of the appropriate type.
     * 
     * @param type
     *            Type of spatial entities for which to remove associations; if
     *            {@link SpatialEntityType#SELECTED}, any records of them as
     *            selected will be removed as well.
     * @param entities
     *            Spatial entities for which to remove associations.
     * @return <code>true</code> if associations were removed,
     *         <code>false</code> otherwise.
     */
    private boolean removeAssociationsOfIdentifiersWithEntities(
            SpatialEntityType type,
            List<? extends SpatialEntity<? extends IEntityIdentifier>> entities) {
        if (entities != null) {
            for (SpatialEntity<? extends IEntityIdentifier> entity : entities) {
                spatialEntitiesForIdentifiers.remove(entity.getIdentifier());
            }
            if (type == SpatialEntityType.SELECTED) {
                for (SpatialEntity<? extends IEntityIdentifier> entity : entities) {
                    selectedSpatialEntityIdentifiers.remove(entity
                            .getIdentifier());
                }
            }
            return (entities.isEmpty() == false);
        }
        return false;
    }

    /**
     * Get the label to be displayed for the specified hazard event.
     * 
     * @param event
     *            Hazard event for which to get the displayable label.
     * @return Label to be displayed.
     */
    private String getHazardLabel(IHazardEvent event) {
        StringBuilder sb = new StringBuilder();
        sb.append(event.getDisplayEventID());
        String hazardType = HazardEventUtilities.getHazardType(event);
        if (hazardType != null) {
            sb.append(" ");
            sb.append(hazardType);
        }
        return sb.toString();
    }

    /**
     * Determine whether the specified geometry is polygonal, or contains a
     * nested geometry that is polygonal.
     * 
     * @param geometry
     *            Geometry to be checked.
     * @return <code>true</code> if the geometry is or contains a polygon,
     *         <code>false</code> otherwise.
     */
    private boolean isOrContainsPolygon(Geometry geometry) {
        if (geometry instanceof Polygonal) {
            return true;
        } else if (geometry instanceof GeometryCollection) {
            for (int j = 0; j < geometry.getNumGeometries(); j++) {
                if (isOrContainsPolygon(geometry.getGeometryN(j))) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Get the processed base geometry for the specified hazard event. It is
     * assumed that the base geometry has no nested geometry collections, that
     * is, it may itself be a geometry collection, but its component geometries
     * are not.
     * <p>
     * TODO: Simplifying the geometry may be something that should be done when
     * setting the hazard event's geometry, not each time the event is rendered
     * in the display. Should this algorithm therefore be moved? It seems
     * wasteful to do it each time spatial entities are regenerated for a hazard
     * event, and also it means that any such spatial entities that the user
     * modifies to modify the base geometry will result in the hazard event's
     * base geometry being overwritten with the simplified geometry anyway,
     * which is problematic.
     * </p>
     * 
     * @param event
     *            Hazard event.
     * @return Processed base geometry. This starts with either the standard
     *         geometry if the hazard event's high-resolution geometry is to be
     *         visible, or else the product geometry if the high-resolution
     *         geometry is not to be shown, and is then processed to remove
     *         undesirable characteristics.
     */
    private IAdvancedGeometry getProcessedBaseGeometry(IHazardEvent event) {

        /*
         * Get either the standard geometry, or the product geometry if the
         * high-resolution geometry is not to be shown.
         */
        IAdvancedGeometry geometry = (HIGH_RESOLUTION_GEOMETRY_IS_VISIBLE
                .equals(event.getHazardAttribute(VISIBLE_GEOMETRY)) ? event
                .getGeometry() : AdvancedGeometryUtilities
                .createGeometryWrapper(event.getProductGeometry(), 0));

        /*
         * Compile a list of polygons for the geometry if it is a collection.
         */
        AdvancedGeometryCollection collection = (geometry instanceof AdvancedGeometryCollection ? (AdvancedGeometryCollection) geometry
                : null);
        List<Polygon> polygons = null;
        if (collection != null) {
            for (IAdvancedGeometry subGeometry : collection.getChildren()) {
                if (subGeometry instanceof GeometryWrapper) {
                    Geometry subBaseGeometry = ((GeometryWrapper) subGeometry)
                            .getGeometry();
                    if (subBaseGeometry instanceof Polygon) {
                        if (polygons == null) {
                            polygons = new ArrayList<>(collection.getChildren()
                                    .size());
                        }
                        polygons.add((Polygon) subBaseGeometry);
                    }
                }
            }
        }

        /*
         * If this geometry is made up entirely of polygons, prune it.
         * Otherwise, if it is a polygon, remove any holes from it.
         */
        if ((polygons != null)
                && (polygons.size() == collection.getChildren().size())) {

            /*
             * For each polygon with the multi-polygon, attempt to prune out
             * holes.
             */
            Geometry newGeometry = geometryFactory.createGeometryCollection(
                    getPrunedPolygons(polygons).toArray(
                            new Geometry[polygons.size()])).buffer(0);
            geometry = AdvancedGeometryUtilities.createGeometryWrapper(
                    newGeometry, 0);

        } else if (geometry instanceof GeometryWrapper) {
            GeometryWrapper wrapper = (GeometryWrapper) geometry;
            Geometry baseGeometry = wrapper.getGeometry();
            if (baseGeometry instanceof Polygon) {

                /*
                 * Remove any holes in the polygon.
                 */
                geometry = AdvancedGeometryUtilities.createGeometryWrapper(
                        getPolygonWithHolesRemoved((Polygon) baseGeometry),
                        wrapper.getRotation());
            }
        }

        /*
         * Return the result.
         */
        return geometry;
    }

    /**
     * Get a new geometry equal to the outer ring of the specified polygon, with
     * any holes in the shape removed.
     * <p>
     * TODO: Why is this a good thing? Should there never be holes in polygonal
     * base geometries of hazard events? Apparently this was done as part of
     * Redmine issue #6090, to remove goosenecks. If this is necessary, should
     * it be done here, or when assigning geometries to hazard events? Perhaps
     * if done the latter way, it could be more discriminating and
     * context-aware, and only simplify like this when appropriate.
     * </p>
     * 
     * @param polygon
     *            Polygon to have its holes removed.
     * @return Polygon with the holes removed, or the original polygon if it had
     *         no holes.
     */
    private Polygon getPolygonWithHolesRemoved(Polygon polygon) {
        if (polygon.getNumInteriorRing() == 0) {
            return polygon;
        }
        return geometryFactory.createPolygon(
                (LinearRing) polygon.getExteriorRing(), null);
    }

    /**
     * Prune each of the specified polygons of holes within reason.
     * 
     * @param polygons
     *            Polygons to be pruned.
     * @return Polygons with holes pruned out of them within reason.
     */
    private List<Geometry> getPrunedPolygons(List<Polygon> polygons) {
        List<Geometry> newGeometries = new ArrayList<>(polygons.size());
        for (Polygon polygon : polygons) {
            newGeometries.add(getPolygonWithHolesPruned(polygon));
        }
        return newGeometries;
    }

    /**
     * Attempt to remove interior holes from a polygon. Up to three passes are
     * made over the polygon, each one expanding any interior rings and merging
     * rings back in.
     * <p>
     * TODO: Would it be better to do this when assigning the geometries to
     * hazard events, instead of each time a hazard event base geometry needs to
     * be turned into a spatial entity? Perhaps if done the latter way, it could
     * be more discriminating and context-aware, and only simplify like this
     * when appropriate.
     * </p>
     * 
     * @param polygon
     *            Polygon to have holes pruned out.
     * @return Polygon with the holes pruned out after three iterations.
     */
    private Geometry getPolygonWithHolesPruned(Polygon polygon) {
        return HazardEventGeometryAggregator.deholePolygon(polygon,
                geometryFactory);
    }
}

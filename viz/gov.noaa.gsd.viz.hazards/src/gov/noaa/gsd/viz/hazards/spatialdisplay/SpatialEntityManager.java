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
import gov.noaa.gsd.common.visuals.BorderStyle;
import gov.noaa.gsd.common.visuals.DragCapability;
import gov.noaa.gsd.common.visuals.FillStyle;
import gov.noaa.gsd.common.visuals.SpatialEntity;
import gov.noaa.gsd.common.visuals.SymbolShape;
import gov.noaa.gsd.common.visuals.VisualFeature;
import gov.noaa.gsd.common.visuals.VisualFeaturesList;
import gov.noaa.gsd.viz.hazards.spatialdisplay.entities.HazardEventEntityIdentifier;
import gov.noaa.gsd.viz.hazards.spatialdisplay.entities.HazardEventHatchingEntityIdentifier;
import gov.noaa.gsd.viz.hazards.spatialdisplay.entities.HazardEventVisualFeatureEntityIdentifier;
import gov.noaa.gsd.viz.hazards.spatialdisplay.entities.IEntityIdentifier;
import gov.noaa.gsd.viz.hazards.spatialdisplay.entities.IHazardEventEntityIdentifier;
import gov.noaa.gsd.viz.hazards.spatialdisplay.entities.ToolVisualFeatureEntityIdentifier;
import gov.noaa.gsd.viz.mvp.widgets.IListStateChanger;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.time.DateUtils;

import com.google.common.collect.Lists;
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
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.Polygonal;

/**
 * Description: Manager for spatial entities within the {@link SpatialPresenter}
 * .
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
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
class SpatialEntityManager {

    // Private Static Constants

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
     * 
     * @deprecated Should be replaced by a {@link IListStateChanger} in the
     *             future.
     */
    @Deprecated
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
     * Overall list of spatial entities.
     */
    private final List<SpatialEntity<? extends IEntityIdentifier>> spatialEntities = new ArrayList<>();

    /**
     * Map of identifiers to spatial entities.
     */
    private final Map<IEntityIdentifier, SpatialEntity<? extends IEntityIdentifier>> spatialEntitiesForIdentifiers = new HashMap<>();

    /**
     * Map of event identifiers to lists of spatial entities representing those
     * hazard events.
     */
    private final Map<String, List<SpatialEntity<? extends IHazardEventEntityIdentifier>>> spatialEntitiesForEventIdentifiers = new HashMap<>();

    /**
     * List of spatial entities, if any, generated from the visual features
     * within {@link #toolVisualFeatures}.
     */
    private final List<SpatialEntity<ToolVisualFeatureEntityIdentifier>> toolSpatialEntities = new ArrayList<>();

    /**
     * Index within {@link spatialEntities} of the first non-hatching spatial
     * entity associated with an unselected hazard event.
     */
    private int unselectedEventStartIndex;

    /**
     * Index within {@link spatialEntities} of the first non-hatching spatial
     * entity associated with a selected hazard event.
     */
    private int selectedEventStartIndex;

    /**
     * Index within {@link spatialEntities} of the first spatial entity
     * generated from one of the visual features found in
     * {@link #toolVisualFeatures}.
     */
    private int toolVisualFeatureStartIndex;

    /**
     * Map pairing each hazard event that is currently showing at least one
     * hatching spatial entity with the index of its first such entity within
     * {@link #spatialEntities}.
     */
    private final Map<String, Integer> hatchingIndicesForEvents = new HashMap<>();

    /**
     * Map pairing each hazard event that is currently unselected and visible
     * with the index of its first non-hatching spatial entity within
     * {@link #spatialEntities}.
     */
    private final Map<String, Integer> unselectedIndicesForEvents = new HashMap<>();

    /**
     * Map pairing each hazard event that is currently selected and visible with
     * the index of its first non-hatching spatial entity within
     * {@link #spatialEntities}.
     */
    private final Map<String, Integer> selectedIndicesForEvents = new HashMap<>();

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
            List<SpatialEntity<? extends IHazardEventEntityIdentifier>> hatchingSpatialEntities = new ArrayList<>();
            List<SpatialEntity<? extends IHazardEventEntityIdentifier>> unselectedSpatialEntities = new ArrayList<>();
            List<SpatialEntity<? extends IHazardEventEntityIdentifier>> selectedSpatialEntities = new ArrayList<>();
            SelectedTime selectedRange = sessionManager.getTimeManager()
                    .getSelectedTime();
            Date selectedTime = getSelectedTimeForVisualFeatures();
            if (createSpatialEntitiesForEvent((ObservedHazardEvent) event,
                    hatchingSpatialEntities, unselectedSpatialEntities,
                    selectedSpatialEntities, selectedRange, selectedTime,
                    sessionManager.getConfigurationManager().getHazardTypes(),
                    sessionManager.areHatchedAreasDisplayed()) == false) {
                return;
            }

            /*
             * Create a range map to hold the ranges of indices that must be
             * offset as a result of this addition.
             */
            RangeMap<Integer, Integer> offsetsForIndices = TreeRangeMap
                    .create();

            /*
             * If hatching spatial entities were generated, find the index where
             * they should be inserted.
             */
            int hatchingInsertionIndex = -1;
            if (hatchingSpatialEntities.isEmpty() == false) {

                /*
                 * Get the insertion index; if -1, the entities are to be
                 * appended to the list. If not, then add an entry to the
                 * offsets-for-indices range map indicating that for any index
                 * greater than or equal to the insertion index, offsetting
                 * should occur in order to keep said indices relevant after the
                 * insertion.
                 */
                hatchingInsertionIndex = getInsertionIndexForHatchingEntities(
                        events, eventIndex);
                if (hatchingInsertionIndex != -1) {
                    offsetsForIndices.put(
                            Range.atLeast(hatchingInsertionIndex),
                            hatchingSpatialEntities.size());
                }
            }

            /*
             * If non-hatching spatial entities were generated, find the index
             * where they should be inserted.
             */
            int nonHatchingInsertionIndex = -1;
            if ((unselectedSpatialEntities.isEmpty() == false)
                    || (selectedSpatialEntities.isEmpty() == false)) {

                /*
                 * Get the insertion index; if -1, the entities are to be
                 * appended to the list. If not, then adjust the insertion index
                 * to take into account any hatching entities that are being
                 * added as well. Then add an entry to the offsets-for-indices
                 * range map indicating that for any index greater than or equal
                 * to the insertion index, offsetting should occur in order to
                 * keep said indices relevant after the insertion.
                 */
                nonHatchingInsertionIndex = getInsertionIndexForNonHatchingEntities(
                        events, eventIndex);
                if (nonHatchingInsertionIndex != -1) {
                    if (hatchingInsertionIndex != -1) {
                        nonHatchingInsertionIndex += hatchingSpatialEntities
                                .size();
                    }
                    offsetsForIndices.put(
                            Range.atLeast(nonHatchingInsertionIndex),
                            hatchingSpatialEntities.size()
                                    + unselectedSpatialEntities.size()
                                    + selectedSpatialEntities.size());
                }
            }

            /*
             * Update the recorded spatial entity indices.
             */
            updateRecordedEntityIndices(offsetsForIndices);

            /*
             * Add an entry to each index-for-event map for which one is
             * appropriate for the new event.
             */
            if (hatchingSpatialEntities.isEmpty() == false) {
                hatchingIndicesForEvents.put(event.getEventID(),
                        (hatchingInsertionIndex == -1 ? spatialEntities.size()
                                : hatchingInsertionIndex));
            }
            if (unselectedSpatialEntities.isEmpty() == false) {
                unselectedIndicesForEvents.put(
                        event.getEventID(),
                        (nonHatchingInsertionIndex == -1 ? spatialEntities
                                .size() + hatchingSpatialEntities.size()
                                : nonHatchingInsertionIndex));
            }
            if (selectedSpatialEntities.isEmpty() == false) {
                selectedIndicesForEvents.put(
                        event.getEventID(),
                        (nonHatchingInsertionIndex == -1 ? spatialEntities
                                .size() + hatchingSpatialEntities.size()
                                : nonHatchingInsertionIndex));
            }

            /*
             * Insert the hatching and non-hatching spatial entities into the
             * list.
             */
            if (hatchingSpatialEntities.isEmpty() == false) {
                if (hatchingInsertionIndex == -1) {
                    addSpatialEntities(hatchingSpatialEntities);
                } else {
                    insertSpatialEntities(hatchingInsertionIndex,
                            hatchingSpatialEntities);
                }
            }
            if ((unselectedSpatialEntities.isEmpty() == false)
                    || (selectedSpatialEntities.isEmpty() == false)) {
                if (nonHatchingInsertionIndex == -1) {
                    addSpatialEntities(unselectedSpatialEntities.isEmpty() ? selectedSpatialEntities
                            : unselectedSpatialEntities);
                } else {
                    insertSpatialEntities(
                            nonHatchingInsertionIndex,
                            unselectedSpatialEntities.isEmpty() ? selectedSpatialEntities
                                    : unselectedSpatialEntities);
                }
            }

            /*
             * Tell the view about the change.
             */
            view.drawSpatialEntities(spatialEntities,
                    selectedSpatialEntityIdentifiers);
        }
    }

    /**
     * Remove spatial entities for the specified event.
     * 
     * @param event
     *            Event for which to remove spatial entities.
     */
    void removeEntitiesForEvent(IHazardEvent event) {
        String eventIdentifier = event.getEventID();
        List<SpatialEntity<? extends IHazardEventEntityIdentifier>> entities = spatialEntitiesForEventIdentifiers
                .get(eventIdentifier);
        if ((entities != null) && (entities.isEmpty() == false)) {

            /*
             * Create a range map to hold the ranges of indices that must be
             * offset as a result of this removal.
             */
            RangeMap<Integer, Integer> offsetsForIndices = TreeRangeMap
                    .create();

            /*
             * If there are hatching entities, determine how many there are and
             * add an entry to the range map to offset any following indices
             * appropriately.
             */
            int hatchingCount = 0;
            int hatchingIndex = -1;
            for (SpatialEntity<? extends IHazardEventEntityIdentifier> entity : entities) {
                if (entity.getIdentifier().getClass()
                        .equals(HazardEventHatchingEntityIdentifier.class) == false) {
                    break;
                }
                hatchingCount++;
            }
            if (hatchingCount > 0) {
                hatchingIndex = hatchingIndicesForEvents.get(eventIdentifier);
                offsetsForIndices.put(
                        Range.atLeast(hatchingIndex + hatchingCount),
                        hatchingCount * -1);
            }

            /*
             * If there are non-hatching entities, determine how many there are
             * and add an entry to the range map to offset any following indices
             * appropriately.
             */
            boolean selected = selectedIndicesForEvents
                    .containsKey(eventIdentifier);
            int nonHatchingIndex = (selected ? selectedIndicesForEvents
                    : unselectedIndicesForEvents).get(eventIdentifier);
            int nonHatchingCount = entities.size() - hatchingCount;
            if (nonHatchingCount > 0) {
                offsetsForIndices.put(
                        Range.atLeast(nonHatchingIndex + entities.size()),
                        entities.size() * -1);
            }

            /*
             * Update the recorded spatial entity indices.
             */
            updateRecordedEntityIndices(offsetsForIndices);

            /*
             * Remove the entries from each index-for-event map for which one
             * was recorded.
             */
            if (hatchingCount > 0) {
                hatchingIndicesForEvents.remove(eventIdentifier);
            }
            (selected ? selectedIndicesForEvents : unselectedIndicesForEvents)
                    .remove(eventIdentifier);

            /*
             * Remove any associations between the hazard event and its spatial
             * entities.
             */
            disassociateSpatialEntitiesFromHazardEvent(eventIdentifier);

            /*
             * Remove both the hatching and non-hatching spatial entities from
             * the list, doing the non-hatching first so that the starting
             * non-hatching index is accurate, since the hatching ones have not
             * been removed from below it in the list yet.
             */
            if (nonHatchingCount > 0) {
                removeSpatialEntities(nonHatchingIndex, nonHatchingCount);
            }
            if (hatchingCount > 0) {
                removeSpatialEntities(hatchingIndex, hatchingCount);
            }

            /*
             * Tell the view about the change.
             */
            view.drawSpatialEntities(spatialEntities,
                    selectedSpatialEntityIdentifiers);
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
         * Generate the new spatial entities.
         */
        int lastToolSpatialEntityCount = toolSpatialEntities.size();
        addSpatialEntitiesForToolVisualFeatures(toolVisualFeatures, toolType,
                toolIdentifier, getSelectedTimeForVisualFeatures());

        /*
         * If there were tool spatial entities before and some have been
         * generated now, replace the old ones with the new ones; otherwise, if
         * there were tool spatial entities before, remove the old ones;
         * finally, if there were no old ones but new ones were generated, add
         * the new ones.
         */
        if (lastToolSpatialEntityCount > 0) {
            if (toolSpatialEntities.size() > 0) {
                replaceSpatialEntities(toolVisualFeatureStartIndex,
                        lastToolSpatialEntityCount, toolSpatialEntities);
            } else {
                removeSpatialEntities(toolVisualFeatureStartIndex,
                        lastToolSpatialEntityCount);
            }
            view.drawSpatialEntities(spatialEntities,
                    selectedSpatialEntityIdentifiers);
        } else if (toolSpatialEntities.size() > 0) {
            addSpatialEntities(toolSpatialEntities);
            view.drawSpatialEntities(spatialEntities,
                    selectedSpatialEntityIdentifiers);
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
         * Clear the associations of hazard events with spatial entities, of
         * spatial entity identifiers with spatial entities, and the listing of
         * tool spatial entities.
         */
        spatialEntitiesForIdentifiers.clear();
        spatialEntitiesForEventIdentifiers.clear();
        toolSpatialEntities.clear();

        /*
         * Iterate through the hazard events, compiling lists of spatial
         * entities from all events that either have no visual features, but
         * whose time ranges intersect the current selected time range, or if
         * they have visual features, that are visible at the current selected
         * time range's start time.
         */
        selectedSpatialEntityIdentifiers.clear();
        List<SpatialEntity<? extends IHazardEventEntityIdentifier>> hatchingSpatialEntities = new ArrayList<>();
        List<SpatialEntity<? extends IHazardEventEntityIdentifier>> unselectedSpatialEntities = new ArrayList<>();
        List<SpatialEntity<? extends IHazardEventEntityIdentifier>> selectedSpatialEntities = new ArrayList<>();
        SelectedTime selectedRange = sessionManager.getTimeManager()
                .getSelectedTime();
        Date selectedTime = getSelectedTimeForVisualFeatures();
        HazardTypes hazardTypes = sessionManager.getConfigurationManager()
                .getHazardTypes();
        boolean hatching = sessionManager.areHatchedAreasDisplayed();
        for (ObservedHazardEvent event : sessionManager.getEventManager()
                .getCheckedEvents()) {
            createSpatialEntitiesForEvent(event, hatchingSpatialEntities,
                    unselectedSpatialEntities, selectedSpatialEntities,
                    selectedRange, selectedTime, hazardTypes, hatching);
        }

        /*
         * If any spatial-information-collecting visual features are currently
         * in existence for a tool, iterate through them, creating spatial
         * entities for any that are visible at the selected time, and add them
         * to the list of selected spatial entities.
         */
        if (toolVisualFeatures.isEmpty() == false) {
            addSpatialEntitiesForToolVisualFeatures(toolVisualFeatures,
                    toolType, toolIdentifier, selectedTime);
        }

        /*
         * Remember the index for each of the following: The first unselected
         * event spatial entity, the first selected event spatial entity, and
         * the first tool visual feature spatial entity. Any that are not found
         * in the spatial entities are recorded as -1.
         */
        unselectedEventStartIndex = (unselectedSpatialEntities.isEmpty() ? -1
                : hatchingSpatialEntities.size());
        selectedEventStartIndex = (selectedSpatialEntities.isEmpty() ? -1
                : unselectedSpatialEntities.size()
                        + hatchingSpatialEntities.size());
        toolVisualFeatureStartIndex = (toolSpatialEntities.isEmpty() ? -1
                : selectedSpatialEntities.size()
                        + unselectedSpatialEntities.size()
                        + hatchingSpatialEntities.size());

        /*
         * Remember the starting indices for each hazard event for each of the
         * following: its first hatching spatial entity, its first unselected
         * spatial entity, and its first selected spatial entity.
         */
        repopulateEntityIndicesForEventsMap(hatchingSpatialEntities,
                hatchingIndicesForEvents, 0);
        repopulateEntityIndicesForEventsMap(unselectedSpatialEntities,
                unselectedIndicesForEvents, unselectedEventStartIndex);
        repopulateEntityIndicesForEventsMap(selectedSpatialEntities,
                selectedIndicesForEvents, selectedEventStartIndex);

        /*
         * Concatenate the lists together into one, and have the view draw them.
         */
        spatialEntities.clear();
        spatialEntities.addAll(hatchingSpatialEntities);
        spatialEntities.addAll(unselectedSpatialEntities);
        spatialEntities.addAll(selectedSpatialEntities);
        spatialEntities.addAll(toolSpatialEntities);
        view.drawSpatialEntities(spatialEntities,
                selectedSpatialEntityIdentifiers);
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
        List<SpatialEntity<? extends IHazardEventEntityIdentifier>> spatialEntities = spatialEntitiesForEventIdentifiers
                .get(eventIdentifier);
        if (spatialEntities != null) {
            for (SpatialEntity<? extends IHazardEventEntityIdentifier> spatialEntity : spatialEntities) {
                if (isOrContainsPolygon(spatialEntity.getGeometry())) {
                    return spatialEntity;
                }
            }
        }
        return null;
    }

    // Private Methods

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
     * Clear and repopulate the specified map associating in each case an event
     * identifier with the index of that event's first spatial entity in the
     * specified list, offset as specified.
     * 
     * @param entities
     *            List from which to draw the information.
     * @param entityIndicesForEvents
     *            Map to be repopulated.
     * @param offset
     *            Amount by which to offset each recorded index.
     */
    private void repopulateEntityIndicesForEventsMap(
            List<SpatialEntity<? extends IHazardEventEntityIdentifier>> entities,
            Map<String, Integer> entityIndicesForEvents, int offset) {
        entityIndicesForEvents.clear();
        for (int j = 0; j < entities.size(); j++) {
            String eventIdentifier = entities.get(j).getIdentifier()
                    .getEventIdentifier();
            if (entityIndicesForEvents.containsKey(eventIdentifier) == false) {
                entityIndicesForEvents.put(eventIdentifier, j + offset);
            }
        }
    }

    /**
     * Add the specified spatial entities to the end of the list.
     * 
     * @param entities
     *            Entities to be added.
     * @deprecated To be replaced by appropriate use of
     *             {@link IListStateChanger} during refactor.
     */
    @Deprecated
    private void addSpatialEntities(
            List<? extends SpatialEntity<? extends IEntityIdentifier>> entities) {
        spatialEntities.addAll(entities);
    }

    /**
     * Insert the specified spatial entities at the specified index.
     * 
     * @param index
     *            Index at which to start the insertion.
     * @param entities
     *            Entities to be inserted.
     * @deprecated To be replaced by appropriate use of
     *             {@link IListStateChanger} during refactor.
     */
    @Deprecated
    private void insertSpatialEntities(int index,
            List<? extends SpatialEntity<? extends IEntityIdentifier>> entities) {
        if (index == spatialEntities.size()) {
            addSpatialEntities(entities);
        } else {
            for (int j = 0; j < entities.size(); j++) {
                spatialEntities.add(index + j, entities.get(j));
            }
        }
    }

    /**
     * Replace the specified number of spatial entities starting at the
     * specified index with the specified entities.
     * 
     * @param index
     *            Index at which to start replacement.
     * @param count
     *            Number of entities to be removed.
     * @param entities
     *            Spatial entities to be inserted at <code>index</code>.
     * @deprecated To be replaced by appropriate use of
     *             {@link IListStateChanger} during refactor.
     */
    @Deprecated
    private void replaceSpatialEntities(int index, int count,
            List<? extends SpatialEntity<? extends IEntityIdentifier>> entities) {
        removeSpatialEntities(index, count);
        insertSpatialEntities(index, entities);
    }

    /**
     * Remove the specified number of spatial entities starting at the specified
     * index.
     * 
     * @param index
     *            Index at which to start removal.
     * @param count
     *            Number of entities to be removed.
     * @deprecated To be replaced by appropriate use of
     *             {@link IListStateChanger} during refactor.
     */
    @Deprecated
    private void removeSpatialEntities(int index, int count) {
        for (int j = 0; j < count; j++) {
            spatialEntities.remove(index);
        }
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
     * @paramm hatching Flag indicating whether or not hatching is to be shown.
     * @return <code>true</code> if at least one entity was created,
     *         <code>false</code> otherwise.
     */
    private boolean createSpatialEntitiesForEvent(
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
        VisualFeaturesList visualFeaturesList = event.getVisualFeatures();
        if ((visualFeaturesList != null) && visualFeaturesList.isEmpty()) {
            visualFeaturesList = null;
        }
        if ((visualFeaturesList != null)
                || selectedRange.intersects(event.getStartTime().getTime(),
                        event.getEndTime().getTime())) {

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
             * If hatching is showing, determine whether or not this event
             * requires it, and if so, create a spatial entity for it.
             */
            if (hatching) {
                String hazardType = HazardEventUtilities.getHazardType(event);
                if (hazardType != null) {
                    HazardTypeEntry hazardTypeEntry = hazardTypes
                            .get(hazardType);
                    if (hazardTypeEntry.getHatchingStyle() != HatchingStyle.NONE) {
                        List<SpatialEntity<? extends IHazardEventEntityIdentifier>> entities = createSpatialEntitiesForEventHatching(
                                event, hazardTypeEntry, hazardColor,
                                HAZARD_EVENT_MULTI_POINT_TEXT_OFFSET_LENGTH,
                                HAZARD_EVENT_MULTI_POINT_TEXT_OFFSET_DIRECTION,
                                HAZARD_EVENT_FONT_POINT_SIZE);
                        created = (entities.isEmpty() == false);
                        hatchingSpatialEntities.addAll(entities);
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
                 */
                List<SpatialEntity<? extends IHazardEventEntityIdentifier>> entities = createSpatialEntitiesForEventVisualFeatures(
                        visualFeaturesList, eventIdentifier, selected,
                        selectedTime, hazardColor, hazardBorderWidth,
                        hazardBorderStyle, hazardPointDiameter, hazardLabel,
                        HAZARD_EVENT_SINGLE_POINT_TEXT_OFFSET_LENGTH,
                        HAZARD_EVENT_SINGLE_POINT_TEXT_OFFSET_DIRECTION,
                        HAZARD_EVENT_MULTI_POINT_TEXT_OFFSET_LENGTH,
                        HAZARD_EVENT_MULTI_POINT_TEXT_OFFSET_DIRECTION,
                        HAZARD_EVENT_FONT_POINT_SIZE);
                if (selected) {
                    for (SpatialEntity<? extends IEntityIdentifier> spatialEntity : entities) {
                        selectedSpatialEntityIdentifiers.add(spatialEntity
                                .getIdentifier());
                    }
                    selectedSpatialEntities.addAll(entities);
                } else {
                    unselectedSpatialEntities.addAll(entities);
                }
                created |= (entities.isEmpty() == false);
            } else {
                SpatialEntity<? extends IHazardEventEntityIdentifier> entity = createDefaultSpatialEntityForEvent(
                        event, hazardColor, hazardBorderWidth,
                        hazardBorderStyle, hazardPointDiameter, hazardLabel,
                        HAZARD_EVENT_SINGLE_POINT_TEXT_OFFSET_LENGTH,
                        HAZARD_EVENT_SINGLE_POINT_TEXT_OFFSET_DIRECTION,
                        HAZARD_EVENT_MULTI_POINT_TEXT_OFFSET_LENGTH,
                        HAZARD_EVENT_MULTI_POINT_TEXT_OFFSET_DIRECTION,
                        HAZARD_EVENT_FONT_POINT_SIZE,
                        eventManager.canEventAreaBeChanged(event));
                if (selected) {
                    selectedSpatialEntityIdentifiers
                            .add(entity.getIdentifier());
                    selectedSpatialEntities.add(entity);
                } else {
                    unselectedSpatialEntities.add(entity);
                }
                created = true;
            }
        }
        return created;
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
     * @return List of spatial entities created; may be an empty list.
     */
    private List<SpatialEntity<? extends IHazardEventEntityIdentifier>> createSpatialEntitiesForEventHatching(
            IHazardEvent event, HazardTypeEntry hazardTypeEntry,
            Color hazardColor, double textOffsetLength,
            double textOffsetDirection, int hazardTextPointSize) {

        /*
         * Get the mapping of UGC identifiers to geometry data describing the
         * areas to be hatched.
         */
        String mapDBtableName = hazardTypeEntry.getUgcType();
        String mapLabelParameter = hazardTypeEntry.getUgcLabel();
        String cwa = event.getSiteID();
        Map<String, IGeometryData> geometryDataForUgcs = geoMapUtilities
                .buildHazardAreaForEvent(mapDBtableName, mapLabelParameter,
                        cwa, event);

        /*
         * Iterate through the mapping entries, handling each one's geometry in
         * turn if appropriate.
         */
        List<SpatialEntity<? extends IHazardEventEntityIdentifier>> spatialEntities = new ArrayList<>();
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
                 * 
                 * TODO: This was removed for Redmine issue #3628 -- not sure
                 * why. Determine the reason and uncomment the check or delete
                 * the commented out code.
                 */
                String significance = event.getSignificance();
                boolean showLabel = false;
                // boolean showLabel = ((hazardTypeEntry.getHatchingStyle() ==
                // HatchingStyle.WARNGEN)
                // && (significance != null) && (significance.isEmpty() ==
                // false));
                HazardEventHatchingEntityIdentifier identifier = new HazardEventHatchingEntityIdentifier(
                        event.getEventID(), entry.getKey());
                SpatialEntity<HazardEventHatchingEntityIdentifier> entity = SpatialEntity
                        .build(null, identifier, geometryData.getGeometry(),
                                null, hazardColor, 0.0, BorderStyle.SOLID,
                                FillStyle.HATCHED, 0.0, null,
                                (showLabel ? significance : null), 0.0, 0.0,
                                textOffsetLength, textOffsetDirection,
                                hazardTextPointSize, hazardColor,
                                DragCapability.NONE, false, false, false, false);
                spatialEntities.add(entity);
                associateSpatialEntityWithHazardEvent(entity);
            }
        }
        return spatialEntities;
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
     * @return Spatial entity that was added.
     */
    private SpatialEntity<? extends IHazardEventEntityIdentifier> createDefaultSpatialEntityForEvent(
            IHazardEvent event, Color hazardColor, double hazardBorderWidth,
            BorderStyle hazardBorderStyle, double hazardPointDiameter,
            String hazardLabel, double hazardSinglePointTextOffsetLength,
            double hazardSinglePointTextOffsetDirection,
            double hazardMultiPointTextOffsetLength,
            double hazardMultiPointTextOffsetDirection,
            int hazardTextPointSize, boolean editable) {
        IHazardEventEntityIdentifier identifier = new HazardEventEntityIdentifier(
                event.getEventID());
        SpatialEntity<? extends IHazardEventEntityIdentifier> spatialEntity = SpatialEntity
                .build(null, identifier, getProcessedBaseGeometry(event),
                        hazardColor, TRANSPARENT_COLOR, hazardBorderWidth,
                        hazardBorderStyle, FillStyle.SOLID,
                        hazardPointDiameter, SymbolShape.CIRCLE, hazardLabel,
                        hazardSinglePointTextOffsetLength,
                        hazardSinglePointTextOffsetDirection,
                        hazardMultiPointTextOffsetLength,
                        hazardMultiPointTextOffsetDirection,
                        hazardTextPointSize, hazardColor,
                        (editable ? DragCapability.ALL : DragCapability.NONE),
                        false, editable, editable, false);
        associateSpatialEntityWithHazardEvent(spatialEntity);
        return spatialEntity;
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
     * @return Spatial entities that were added; may be an empty list.
     */
    private List<SpatialEntity<? extends IHazardEventEntityIdentifier>> createSpatialEntitiesForEventVisualFeatures(
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
        if (visualFeaturesList != null) {

            /*
             * Round the selected time down to the nearest minute.
             */
            selectedTime = DateUtils.truncate(selectedTime, Calendar.MINUTE);

            /*
             * Iterate through the visual features, adding spatial entities
             * generated for each in turn to the provided list. Any spatial
             * entities that are marked as "topmost" in the z-ordering are added
             * to another list.
             */
            List<SpatialEntity<? extends IHazardEventEntityIdentifier>> addedSpatialEntities = new ArrayList<>(
                    visualFeaturesList.size());
            List<SpatialEntity<? extends IHazardEventEntityIdentifier>> topmostSpatialEntities = null;
            for (VisualFeature visualFeature : visualFeaturesList) {
                HazardEventVisualFeatureEntityIdentifier identifier = new HazardEventVisualFeatureEntityIdentifier(
                        eventIdentifier, visualFeature.getIdentifier());
                SpatialEntity<? extends IHazardEventEntityIdentifier> entity = visualFeature
                        .getStateAtTime(null, identifier, selected,
                                selectedTime, hazardColor, hazardBorderWidth,
                                hazardBorderStyle, hazardPointDiameter,
                                hazardLabel, hazardSinglePointTextOffsetLength,
                                hazardSinglePointTextOffsetDirection,
                                hazardMultiPointTextOffsetLength,
                                hazardMultiPointTextOffsetDirection,
                                hazardTextPointSize);
                if (entity != null) {
                    if (entity.isTopmost()) {
                        if (topmostSpatialEntities == null) {
                            topmostSpatialEntities = new ArrayList<>();
                        }
                        topmostSpatialEntities.add(entity);
                    } else {
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
                addedSpatialEntities.addAll(topmostSpatialEntities);
            }

            /*
             * Associate each entity that was created with its hazard event.
             */
            associateSpatialEntitiesWithHazardEvent(addedSpatialEntities);

            return addedSpatialEntities;
        }
        return Collections.emptyList();
    }

    /**
     * Add spatial entities for visual features that have been created by tools
     * for the collection of spatial information from the user.
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
     */
    private void addSpatialEntitiesForToolVisualFeatures(
            VisualFeaturesList visualFeaturesList, final ToolType toolType,
            final String toolIdentifier, Date selectedTime) {

        /*
         * Remove records of previous tool spatial entities.
         */
        if (toolSpatialEntities.isEmpty() == false) {
            disassociateSpatialEntitiesWithTool();
        }

        /*
         * Round the selected time down to the nearest minute.
         */
        selectedTime = DateUtils.truncate(selectedTime, Calendar.MINUTE);

        /*
         * If there are visual features in the list, iterate through them,
         * creating spatial entities for any that are visible at the selected
         * time.
         */
        if (visualFeaturesList != null) {

            /*
             * Iterate through the visual features, adding spatial entities
             * generated for each in turn to the provided list. Any spatial
             * entities that are marked as "topmost" in the z-ordering are added
             * to another list.
             */
            List<SpatialEntity<ToolVisualFeatureEntityIdentifier>> addedSpatialEntities = null;
            List<SpatialEntity<ToolVisualFeatureEntityIdentifier>> topmostSpatialEntities = null;
            for (VisualFeature visualFeature : visualFeaturesList) {
                ToolVisualFeatureEntityIdentifier identifier = new ToolVisualFeatureEntityIdentifier(
                        toolType, toolIdentifier, visualFeature.getIdentifier());
                SpatialEntity<ToolVisualFeatureEntityIdentifier> entity = visualFeature
                        .getStateAtTime(null, identifier, selectedTime);
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
             * If spatial entities were generated, associate them with the tool.
             */
            if (addedSpatialEntities != null) {
                associateSpatialEntitiesWithTool(addedSpatialEntities);
            }
        }
    }

    /**
     * Get the index of the point within {@link spatialEntities} at which to
     * insert hatching spatial entities for the event at the specified index
     * within the specified events list.
     * 
     * @param events
     *            List of events.
     * @param eventIndex
     *            Index of the event within <code>events</code> for which
     *            hatching spatial entities are to be inserted.
     * @return Index of the insertion point, or <code>-1</code> if the entities
     *         should instead be appended.
     */
    private int getInsertionIndexForHatchingEntities(
            List<ObservedHazardEvent> events, int eventIndex) {

        /*
         * The index at which to insert the hatching entities is the one where
         * hatching entities reside for the next event after this one that has
         * hatching.
         */
        int nextEntityIndex = -1;
        for (int j = eventIndex + 1; j < events.size(); j++) {
            String eventIdentifier = events.get(j).getEventID();
            if (hatchingIndicesForEvents.containsKey(eventIdentifier)) {
                nextEntityIndex = hatchingIndicesForEvents.get(eventIdentifier);
                break;
            }
        }

        /*
         * If no insertion index was found, the hatching entities are to be
         * placed after all other hatching entities.
         */
        if (nextEntityIndex == -1) {
            nextEntityIndex = (unselectedEventStartIndex == -1 ? (selectedEventStartIndex == -1 ? toolVisualFeatureStartIndex
                    : selectedEventStartIndex)
                    : unselectedEventStartIndex);
        }
        return nextEntityIndex;
    }

    /**
     * Get the index of the point within {@link spatialEntities} at which to
     * insert non-hatching spatial entities for the event at the specified index
     * within the specified events list.
     * 
     * @param events
     *            List of events.
     * @param eventIndex
     *            Index of the event within <code>events</code> for which
     *            non-hatching spatial entities are to be inserted.
     * @return Index of the insertion point, or <code>-1</code> if the entities
     *         should instead be appended.
     */
    private int getInsertionIndexForNonHatchingEntities(
            List<ObservedHazardEvent> events, int eventIndex) {

        /*
         * Determine whether or not the event is selected, and then iterate
         * through all events following it in the list of events in order to
         * find the first event that is currently visible and has the same
         * selection state, and record said event's index.
         */
        boolean selected = selectedEventIdentifiers.contains(events.get(
                eventIndex).getEventID());
        int nextEntityIndex = -1;
        for (int j = eventIndex + 1; j < events.size(); j++) {
            String eventIdentifier = events.get(j).getEventID();
            if ((selectedEventIdentifiers.contains(eventIdentifier) == selected)
                    && ((selected && selectedIndicesForEvents
                            .containsKey(eventIdentifier)) || ((selected == false) && unselectedIndicesForEvents
                            .containsKey(eventIdentifier)))) {
                if (selected) {
                    nextEntityIndex = selectedIndicesForEvents
                            .get(eventIdentifier);
                } else {
                    nextEntityIndex = unselectedIndicesForEvents
                            .get(eventIdentifier);
                }
                break;
            }
        }

        /*
         * If no visible event that is to follow this one in the z-order was
         * found above, use the index that indicated the start of the tool
         * visual features if the new event is selected (since this event is at
         * the top of the selected events in the z-order), or the start of the
         * selected events if the new event is not (since this event is at the
         * top of the unselected events in the z-order).
         */
        if (nextEntityIndex == -1) {
            if (selected) {
                nextEntityIndex = toolVisualFeatureStartIndex;
            } else {
                nextEntityIndex = (selectedEventStartIndex == -1 ? toolVisualFeatureStartIndex
                        : selectedEventStartIndex);
            }
        }
        return nextEntityIndex;
    }

    /**
     * Update the various recorded spatial entity indices (
     * {@link #unselectedEventStartIndex}, {@link #selectedEventStartIndex},
     * {@link #toolVisualFeatureStartIndex}, {@link #hatchingIndicesForEvents},
     * {@link #unselectedIndicesForEvents}, and
     * {@link #selectedIndicesForEvents}) by offsetting them as specified.
     * <p>
     * As an example, suppose the range map provided as a parameter associates
     * all values between 3 and 9 inclusive with -2, and all values 10 and above
     * with 4. Using these parameters will cause this method to alter some
     * indices recorded in the abovementioned member variables. Any recorded
     * index with a value between 3 and 9 inclusive will have -2 added to it,
     * while any such index with a value of 10 or greater will have 4 added to
     * it.
     * </p>
     * 
     * @param offsetsForIndices
     *            Range map pairing ranges of indices with the offsets that
     *            should be applied to them. Not all indices will have entries
     *            within this range map.
     */
    private void updateRecordedEntityIndices(
            RangeMap<Integer, Integer> offsetsForIndices) {
        updateEntityIndicesMap(offsetsForIndices, hatchingIndicesForEvents);
        unselectedEventStartIndex = updateEntityIndex(offsetsForIndices,
                unselectedEventStartIndex);
        updateEntityIndicesMap(offsetsForIndices, unselectedIndicesForEvents);
        selectedEventStartIndex = updateEntityIndex(offsetsForIndices,
                selectedEventStartIndex);
        updateEntityIndicesMap(offsetsForIndices, selectedIndicesForEvents);
        toolVisualFeatureStartIndex = updateEntityIndex(offsetsForIndices,
                toolVisualFeatureStartIndex);
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
     * Update the the specified index by offsetting it as specified.
     * <p>
     * As an example, suppose the range map provided as a parameter associates
     * all values between 3 and 9 inclusive with -2, and all values 10 and above
     * with 4. Using these parameters will cause this method to add -2 to the
     * provided index if it is between 3 and 9 inclusive, or 4 if it is 10 or
     * above.
     * 
     * @param offsetsForIndices
     *            Range map pairing ranges of indices with the offsets that
     *            should be applied to them. Not all indices will have entries
     *            within this range map.
     * @param index
     *            Index to be updated.
     * @return Updated index.
     */
    private int updateEntityIndex(RangeMap<Integer, Integer> offsetsForIndices,
            int index) {
        Integer offset = offsetsForIndices.get(index);
        return (offset != null ? index + offset : index);
    }

    /**
     * Associate the specified spatial entity with the hazard event provided by
     * its identifier.
     * 
     * @param entity
     *            Spatial entity to be associated with a hazard event.
     */
    private void associateSpatialEntityWithHazardEvent(
            SpatialEntity<? extends IHazardEventEntityIdentifier> entity) {

        /*
         * See if the hazard event already has a list of entities associated
         * with it; if not, create one.
         */
        IHazardEventEntityIdentifier identifier = entity.getIdentifier();
        List<SpatialEntity<? extends IHazardEventEntityIdentifier>> list = spatialEntitiesForEventIdentifiers
                .get(identifier.getEventIdentifier());
        if (list == null) {
            list = new ArrayList<>();
            spatialEntitiesForEventIdentifiers.put(
                    identifier.getEventIdentifier(), list);
        }

        /*
         * Add this entity to the list.
         */
        list.add(entity);

        /*
         * Associate this entity with its identifier.
         */
        spatialEntitiesForIdentifiers.put(identifier, entity);
    }

    /**
     * Associate the specified spatial entities with the hazard event provided
     * by their identifiers. Note that it is assumed they all represent the same
     * hazard event.
     * 
     * @param entities
     *            Spatial entities to be associated with a hazard event.
     */
    private void associateSpatialEntitiesWithHazardEvent(
            List<SpatialEntity<? extends IHazardEventEntityIdentifier>> entities) {

        /*
         * If there are no entities, do nothing.
         */
        if (entities.isEmpty()) {
            return;
        }

        /*
         * Get the event identifier of the first entity (since it is assumed
         * that they all have the same event identifier), and see if the hazard
         * event already has a list of entities associated with it. If it does,
         * add these entities to the list; if not, create a list holding these
         * entities.
         */
        String eventIdentifier = entities.iterator().next().getIdentifier()
                .getEventIdentifier();
        List<SpatialEntity<? extends IHazardEventEntityIdentifier>> list = spatialEntitiesForEventIdentifiers
                .get(eventIdentifier);
        if (list == null) {
            list = new ArrayList<>(entities);
            spatialEntitiesForEventIdentifiers.put(eventIdentifier, list);
        } else {
            list.addAll(entities);
        }

        /*
         * Associate the entities with their identifiers.
         */
        for (SpatialEntity<? extends IHazardEventEntityIdentifier> entity : entities) {
            spatialEntitiesForIdentifiers.put(entity.getIdentifier(), entity);
        }
    }

    /**
     * Remove any spatial entities associated with the specified hazard event,
     * disassociating them in the process.
     * 
     * @param eventIdentifier
     *            Hazard event identifier.
     */
    private void disassociateSpatialEntitiesFromHazardEvent(
            String eventIdentifier) {
        List<SpatialEntity<? extends IHazardEventEntityIdentifier>> list = spatialEntitiesForEventIdentifiers
                .remove(eventIdentifier);
        if (list != null) {
            for (SpatialEntity<? extends IHazardEventEntityIdentifier> entity : list) {
                spatialEntitiesForIdentifiers.remove(entity.getIdentifier());
            }
        }
    }

    /**
     * Associate the specified spatial entities with the tool currently
     * attempting to gather spatial information from the user.
     * 
     * @param entities
     *            Entities to be associated.
     */
    private void associateSpatialEntitiesWithTool(
            List<SpatialEntity<ToolVisualFeatureEntityIdentifier>> entities) {

        /*
         * Add all the entities to the list of tool-related spatial entities.
         */
        toolSpatialEntities.addAll(entities);

        /*
         * Associate the entities with their identifiers.
         */
        for (SpatialEntity<ToolVisualFeatureEntityIdentifier> entity : entities) {
            spatialEntitiesForIdentifiers.put(entity.getIdentifier(), entity);
        }
    }

    /**
     * Disassociate any old tool spatial entities with whatever tool was
     * previously attempting to gather spatial information from the user.
     */
    private void disassociateSpatialEntitiesWithTool() {

        /*
         * Disassociate the entities with their identifiers.
         */
        for (SpatialEntity<ToolVisualFeatureEntityIdentifier> entity : toolSpatialEntities) {
            spatialEntitiesForIdentifiers.remove(entity.getIdentifier());
        }

        /*
         * Remove the entities.
         */
        toolSpatialEntities.clear();
    }

    /**
     * Get the processed base geometry for the specified hazard event. The
     * result is guaranteed to have no nested geometry collections, that is, it
     * may itself be a geometry collection, but its component geometries will
     * not be.
     * <p>
     * TODO: Flattening any nested geometry collections, and simplifying the
     * geometry to boot, may be something that should be done when setting the
     * hazard event's geometry, not each time the event is rendered in the
     * display. Should this algorithm therefore be moved? It seems wasteful to
     * do it each time spatial entities are regenerated for a hazard event, and
     * also it means that any such spatial entities that the user modifies to
     * modify the base geometry will result in the hazard event's base geometry
     * being overwritten with the flattened simplified geometry anyway, which is
     * problematic.
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
    private Geometry getProcessedBaseGeometry(IHazardEvent event) {

        /*
         * Get either the standard geometry, or the product geometry if the
         * high-resolution geometry is not to be shown.
         */
        Geometry geometry = (HIGH_RESOLUTION_GEOMETRY_IS_VISIBLE.equals(event
                .getHazardAttribute(VISIBLE_GEOMETRY)) ? event.getGeometry()
                : event.getProductGeometry());

        /*
         * If this is a multi-polygon, process it to Otherwise, if this is some
         * other type of geometry collection, iterate through it, flattening it
         * down to be a collection of non-collections (in case it has nested
         * collections) and further processing the sub-geometries as necessary.
         * Finally, if this is a polygon, process it as appropriate.
         */
        if (geometry instanceof MultiPolygon) {

            /*
             * For each polygon with the multi-polygon, attempt to prune out
             * holes.
             */
            MultiPolygon multiPolygon = (MultiPolygon) geometry;
            geometry = geometryFactory.createGeometryCollection(
                    getPrunedPolygonsFromMultiPolygon(multiPolygon).toArray(
                            new Geometry[multiPolygon.getNumGeometries()]))
                    .buffer(0);

        } else if (geometry instanceof GeometryCollection) {
            List<Geometry> newGeometries = null;
            GeometryCollection geometryCollection = (GeometryCollection) geometry;
            for (int j = 0; j < geometryCollection.getNumGeometries(); j++) {

                /*
                 * If this sub-geometry is a multi-polygon, flatten it and prune
                 * its component polygons; if the sub-geometry is another type
                 * of geometry collection, flatten it; if it is a polygon,
                 * further process it.
                 */
                Geometry subGeometry = geometryCollection.getGeometryN(j);
                List<Geometry> newSubGeometries = getProcessedGeometry(subGeometry);

                /*
                 * If a change has been made to this sub-geometry, and a list of
                 * new geometries has not already been built, do so now,
                 * retroactively copying any sub-geometries from previous
                 * iterations into it. This would occur if, for example, the
                 * first three sub-geometries were found to be unneeding of any
                 * processing, but this sub-geometry is a collection.
                 */
                if ((newSubGeometries != null) && (newGeometries == null)) {
                    newGeometries = new ArrayList<>();
                    for (int k = 0; k < j; k++) {
                        newGeometries.add(geometryCollection.getGeometryN(k));
                    }
                }

                /*
                 * If a new geometries list exists, add any new sub-geometries
                 * created above, or if no list of new sub-geometries was
                 * created, just add this sub-geometry to it.
                 */
                if (newGeometries != null) {
                    if (newSubGeometries != null) {
                        newGeometries.addAll(newSubGeometries);
                    } else {
                        newGeometries.add(subGeometry);
                    }
                }
            }

            /*
             * If a list of new geometries was created, turn it into a geometry
             * collection.
             */
            if (newGeometries != null) {
                geometry = geometryFactory
                        .createGeometryCollection(newGeometries
                                .toArray(new Geometry[newGeometries.size()]));
            }

        } else if (geometry instanceof Polygon) {

            /*
             * Remove any holes in the polygon.
             */
            geometry = getPolygonWithHolesRemoved((Polygon) geometry);
        }

        /*
         * Return the result.
         */
        return geometry;
    }

    /**
     * Process the specified geometry, flattening it so that any sub-geometries
     * (including nested ones) are turned into a list, and return a list of
     * component geometries.
     * 
     * @param geometry
     *            Geometry to be processed.
     * @return List of component geometries of the specified flattened geometry,
     *         processed as appropriate.
     */
    private List<Geometry> getProcessedGeometry(Geometry geometry) {

        /*
         * If this geometry is a multi-polygon, flatten it and prune its
         * component polygons, then return the result; if the geometry is
         * another type of geometry collection, recursively flatten and process
         * it; and if it is a polygon, process it as appropriate. In all three
         * cases, return the resulting list of one or more geometries.
         */
        if (geometry instanceof MultiPolygon) {
            return getPrunedPolygonsFromMultiPolygon((MultiPolygon) geometry);

        } else if (geometry instanceof GeometryCollection) {

            /*
             * Iterate through the component sub-geometries, recursively
             * processing each in turn, and adding the resulting sub-lists of
             * geometries, or the original sub-geometries when no processing is
             * needed, to a list of new geometries. Then return the latter.
             */
            GeometryCollection geometryCollection = (GeometryCollection) geometry;
            List<Geometry> newGeometries = new ArrayList<>();
            for (int j = 0; j < geometryCollection.getNumGeometries(); j++) {
                Geometry subGeometry = geometryCollection.getGeometryN(j);
                List<Geometry> newSubGeometries = getProcessedGeometry(subGeometry);
                if (newSubGeometries != null) {
                    newGeometries.addAll(newSubGeometries);
                } else {
                    newGeometries.add(subGeometry);
                }
            }
            return newGeometries;

        } else if (geometry instanceof Polygon) {

            /*
             * Remove any holes in the polygon; if this results in changes,
             * return it.
             */
            Geometry newPolygon = getPolygonWithHolesRemoved((Polygon) geometry);
            if (newPolygon != geometry) {
                return Lists.<Geometry> newArrayList(newPolygon);
            }
        }
        return null;
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
    protected Geometry getPolygonWithHolesPruned(Polygon polygon) {
        return HazardEventGeometryAggregator.deholePolygon(polygon,
                geometryFactory);
    }

    /**
     * Get the polygon sub-geometries of the specified multi-polygon, with each
     * of the former pruned of holes within reason.
     * 
     * @param multiPolygon
     *            Multi-polygon from which to extract the polygons.
     * @return Polygons with holes pruned out of them within reason.
     */
    private List<Geometry> getPrunedPolygonsFromMultiPolygon(
            MultiPolygon multiPolygon) {
        List<Geometry> newGeometries = new ArrayList<>(
                multiPolygon.getNumGeometries());
        for (int j = 0; j < multiPolygon.getNumGeometries(); j++) {
            newGeometries.add(getPolygonWithHolesPruned((Polygon) multiPolygon
                    .getGeometryN(j)));
        }
        return newGeometries;
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
}

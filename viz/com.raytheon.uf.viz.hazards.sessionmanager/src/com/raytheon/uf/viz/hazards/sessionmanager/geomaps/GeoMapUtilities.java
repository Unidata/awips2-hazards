/**
 * This software was developed and / or modified by Raytheon Company,
 * pursuant to Contract DG133W-05-CQ-1067 with the US Government.
 * 
 * U.S. EXPORT CONTROLLED TECHNICAL DATA
 * This software product contains export-restricted data whose
 * export/transfer/disclosure is restricted by U.S. law. Dissemination
 * to non-U.S. persons whether in the United States or abroad requires
 * an export license or other authorization.
 * 
 * Contractor Name:        Raytheon Company
 * Contractor Address:     6825 Pine Street, Suite 340
 *                         Mail Stop B8
 *                         Omaha, NE 68106
 *                         402.291.0100
 * 
 * See the AWIPS II Master Rights File ("Master Rights File.pdf") for
 * further licensing information.
 **/
package com.raytheon.uf.viz.hazards.sessionmanager.geomaps;

import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HAZARD_AREA;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HAZARD_AREA_ALL;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HAZARD_AREA_INTERSECTION;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HAZARD_AREA_NONE;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HIGH_RESOLUTION_GEOMETRY_IS_VISIBLE;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.VISIBLE_GEOMETRY;
import gov.noaa.gsd.common.utilities.geometry.AdvancedGeometryUtilities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.raytheon.uf.common.dataaccess.DataAccessLayer;
import com.raytheon.uf.common.dataaccess.IDataRequest;
import com.raytheon.uf.common.dataaccess.geom.IGeometryData;
import com.raytheon.uf.common.dataaccess.impl.DefaultGeometryData;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardEventUtilities;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.hazards.configuration.types.HatchingStyle;
import com.raytheon.uf.common.hazards.configuration.types.HazardTypeEntry;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.util.Pair;
import com.raytheon.uf.viz.hazards.sessionmanager.config.ISessionConfigurationManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.ObservedSettings;
import com.raytheon.uf.viz.hazards.sessionmanager.events.impl.ObservedHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.ugcbuilder.IugcToMapGeometryDataBuilder;
import com.raytheon.uf.viz.hazards.sessionmanager.ugcbuilder.impl.CountyUGCBuilder;
import com.raytheon.uf.viz.hazards.sessionmanager.ugcbuilder.impl.FireWXZoneUGCBuilder;
import com.raytheon.uf.viz.hazards.sessionmanager.ugcbuilder.impl.MarineZoneUGCBuilder;
import com.raytheon.uf.viz.hazards.sessionmanager.ugcbuilder.impl.NullUGCBuilder;
import com.raytheon.uf.viz.hazards.sessionmanager.ugcbuilder.impl.OffshoreZoneUGCBuilder;
import com.raytheon.uf.viz.hazards.sessionmanager.ugcbuilder.impl.ZoneUGCBuilder;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.geom.TopologyException;
import com.vividsolutions.jts.precision.GeometryPrecisionReducer;

/**
 * Misc functionality for building the UGC information associated with a hazard
 * event.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 02, 2014 2536       blawrenc    Initial creation
 * Mar 20, 2014 3290       bkowal      A county will only be included in an
 *                                     interoperability hazard if 10% of the
 *                                     county is within the selected region.
 *                                     This check is necessary to handle
 *                                     discrepancies between the different
 *                                     map resolutions.
 * Apr 28, 2014 3556       bkowal      Updated to use the new hazards common 
 *                                     configuration plugin.
 * Dec 05, 2014 2124       Chris.Golden Changed to work with parameterized
 *                                      config manager.
 * Jan 22, 2015 4959       Dan Schaffer Ability to right click to add/remove UGCs from hazards
 * Jan 26, 2015 5952       Dan Schaffer Fix incorrect hazard area designation.
 * Feb  6, 2015 4375       Dan Schaffer Added error check for empty UGCs.
 * Feb 12, 2015 4959       Dan Schaffer Modify MB3 add/remove UGCs to match Warngen
 * Feb 21, 2015 4959       Dan Schaffer Improvements to add/remove UGCs
 * Feb 25, 2015 6632       Dan Schaffer Fixed bug handling hazard outside CWA
 * Mar 13, 2015 6090       Dan Schaffer Fixed goosenecks
 * Mar 24, 2015 6090       Dan Schaffer Goosenecks now working as they do in Warngen
 * May 05, 2015 7624       mduff        Removed multiple geometry point reductions.
 * Aug 14, 2015 9920       Robert.Blum  Parameters are no longer required on mapdata requests.
 * Sep 03, 2015 11213      mduff        Fixed performance issue for initial preview.
 * Oct 13, 2015 12494      Chris Golden Reworked to allow hazard types to include
 *                                      only phenomenon (i.e. no significance) where
 *                                      appropriate.
 * Oct 19, 2015  8765      Robert.Blum  Based hatching on the currently displayed polygon
 *                                      (high/low res).
 * Dec 01, 2015 13172      Robert.Blum  Fixed IllegalArgumentException caused by a
 *                                      GeometryCollection being passed to the union()
 *                                      method.
 * Feb 24, 2016 14667      Robert.Blum  Limiting Flash Flood Recommender to basins inside
 *                                      the CWA.
 * Mar 09, 2016 16059      Kevin.Bisanz Added INCLUSION_FRACTION_MINIMUM to exclude very small
 *                                      inclusionFraction values.
 * Mar 15, 2016 15699      Kevin.Bisanz Fix issues with testInclusion(...).
 * Apr 01, 2016 15193      Kevin.Bisanz Removed geometry contains(...) check and loop from
 *                                      testInclusion(...) such that inclusion is based on
 *                                      area and/or fraction.
 * Apr 04, 2016 15193      Kevin.Bisanz In getIntersectingMapGeometries(...) removed loop
 *                                      over hazard geometry because Geometry.intersect()
 *                                      supports multipolygons and is faster.
 * May 26, 2016 18249      Roger.Ferrel Fix bug in getIntersectingMapGeometries to properly
 *                                      check for small polygons.
 * Jun 23, 2016 19537      Chris.Golden Added support for no-hatching event types.
 * Jul 25, 2016 19537      Chris.Golden Changed buildHazardAreaForEvent() to return a map
 *                                      of UGC+geometry identifiers to the geometries, so
 *                                      that this information may be used when generating
 *                                      hatching polygons within the spatial presenter.
 * Sep 12, 2016 15934      Chris.Golden Changed to work with advanced geometries now used
 *                                      by hazard events.
 * Nov 17, 2016 26313      Chris.Golden Changed to support multiple UGC types per hazard
 *                                      type, and to work with revamped GeoMapUtilities,
 *                                      moving some code that was previously in the
 *                                      SessionEventManager into this class where it
 *                                      belongs. Also cleaned up public vs. private
 *                                      methods, making methods private as appropriate,
 *                                      and added comments.
 * Jul 11, 2017 15561      Chris.Golden Added code to allow hazard types that require
 *                                      neither fraction- nor area-based inclusion testing
 *                                      to automatically be considered to include UGCs that
 *                                      intersect them.
 * 
 * </pre>
 * 
 * @author blawrenc
 * @version 1.0
 */

public class GeoMapUtilities {

    // Private Enumerated Types

    private enum MapGeometryExtractionApproach {
        CONTAINING, CONTAINED, INTERSECTION
    }

    // Private Static Constants

    /**
     * Error to be displayed if an empty geometry would be created by the
     * removal of a UGC from a hazard event's hazard area.
     */
    private static final String EMPTY_GEOMETRY_ERROR = "Deleting this UGC would "
            + "leave the hazard with an empty geometry.";

    /**
     * The default amount of area that must be selected within a country for
     * inclusion. Currently at 10%.
     */
    private static final double DEFAULT_INTEROPERABILITY_OVERLAP_REQUIREMENT = 0.10;

    /**
     * Minimum fraction that should be used to indicate inclusion. The value 0.0
     * is not used because very small numbers (e.g. 3.13692076986235E-9) can
     * result in certain cases when an area is deselected.
     */
    private static final double INCLUSION_FRACTION_MINIMUM = 0.0001;

    private static final double KM_PER_DEGREE_AT_EQUATOR = 111.03;

    /**
     * Mappings between geometry tables in the geodatabase and attributes that
     * need to be retrieved from them to support UGC mapping.
     */
    private static final Map<String, String[]> ATTRIBUTES_FOR_TABLE_NAMES;
    static {
        Map<String, String[]> tempMap = new HashMap<>();

        tempMap.put(HazardConstants.MAPDATA_COUNTY, new String[] {
                HazardConstants.UGC_FIPS, HazardConstants.UGC_STATE });
        tempMap.put(HazardConstants.MAPDATA_ZONE, new String[] {
                HazardConstants.UGC_STATE, HazardConstants.UGC_ZONE });
        tempMap.put(HazardConstants.MAPDATA_FIRE_ZONES, new String[] {
                HazardConstants.UGC_STATE, HazardConstants.UGC_ZONE });
        tempMap.put(HazardConstants.MAPDATA_MARINE_ZONES,
                new String[] { HazardConstants.UGC_ID });
        tempMap.put(HazardConstants.MAPDATA_OFFSHORE,
                new String[] { HazardConstants.UGC_ID });

        ATTRIBUTES_FOR_TABLE_NAMES = Collections.unmodifiableMap(tempMap);
    }

    /**
     * Mappings between geodatabase table names and the UGC builders which
     * correspond to them.
     */
    private static final Map<String, IugcToMapGeometryDataBuilder> UGC_BUILDERS_FOR_TABLE_NAMES;
    static {
        Map<String, IugcToMapGeometryDataBuilder> tempMap = new HashMap<>();

        tempMap.put(HazardConstants.MAPDATA_COUNTY, new CountyUGCBuilder());
        tempMap.put(HazardConstants.MAPDATA_ZONE, new ZoneUGCBuilder());
        tempMap.put(HazardConstants.MAPDATA_FIRE_ZONES,
                new FireWXZoneUGCBuilder());
        tempMap.put(HazardConstants.MAPDATA_MARINE_ZONES,
                new MarineZoneUGCBuilder());
        tempMap.put(HazardConstants.MAPDATA_OFFSHORE,
                new OffshoreZoneUGCBuilder());

        UGC_BUILDERS_FOR_TABLE_NAMES = Collections.unmodifiableMap(tempMap);
    }

    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(GeoMapUtilities.class);

    /**
     * Map for caching geometries retrieved from the geo database.
     */
    private static Map<String, Map<String, Set<IGeometryData>>> mapGeometryCache = new HashMap<>();

    private final ISessionConfigurationManager<ObservedSettings> configManager;

    private final GeometryFactory geometryFactory = new GeometryFactory();

    /**
     * Precision model used to ensure that no topology exceptions occur when
     * intersections are done.
     * 
     * TODO Not sure if this is the right model. Should it be based on a scale?
     * If so, how would the code know what the scale is?
     */
    private final PrecisionModel precisionModel = new PrecisionModel(
            PrecisionModel.FLOATING_SINGLE);

    private Geometry cwaGeometry;

    // Public Constructors

    public GeoMapUtilities(
            ISessionConfigurationManager<ObservedSettings> configManager) {
        this.configManager = configManager;
    }

    // Public Methods

    /**
     * Get the map of UGCs to geometries from the map database tables associated
     * with the specified hazard event, which also intersect with said event's
     * geometry.
     * 
     * @param hazardEvent
     *            Hazard event for which to fetch the intersecting UGCs.
     * @return Map pairing UGCs to geometries that intersect the hazard event.
     */
    public Map<String, IGeometryData> getIntersectingMapGeometriesForUgcs(
            IHazardEvent hazardEvent) {
        Map<String, IGeometryData> geometriesForUgcs = new HashMap<>();
        for (String mapDatabaseTableName : getMapDatabaseTableNames(hazardEvent)) {
            Set<IGeometryData> hazardArea = getIntersectingMapGeometries(true,
                    hazardEvent, mapDatabaseTableName);
            addMappingsWarningIfDuplicateKeys(
                    geometriesForUgcs,
                    getUgcsGeometryDataMapping(mapDatabaseTableName, hazardArea));
        }
        return geometriesForUgcs;
    }

    /**
     * Toggle the UGCs for the specified hazard event that contain the specified
     * point: If a UGC containing the point is already included in the event's
     * hazard area, remove it; if it is not included in said area, add it.
     * 
     * @param hazardEvent
     *            Hazard event to have its UGCs toggled.
     * @param location
     *            Location in latitude-longitude coordinates that UGCs must
     *            contain in order to be added or removed.
     * @return If the change was disallowed, <code>null</code>; otherwise, the
     *         modified hazard area and, if the hazard event's geometry has
     *         changed as well, the new geometry.
     */
    @SuppressWarnings("unchecked")
    public Pair<Map<String, String>, Geometry> addOrRemoveEnclosingUgcs(
            IHazardEvent hazardEvent, Coordinate location) {
        try {

            Map<String, String> hazardAreas = (Map<String, String>) hazardEvent
                    .getHazardAttribute(HAZARD_AREA);
            hazardAreas = new HashMap<>(hazardAreas);

            Set<String> mapDatabaseTableNames = getMapDatabaseTableNames(hazardEvent);
            Geometry locationAsGeometry = geometryFactory.createPoint(location);
            Geometry hazardEventBaseGeometry = hazardEvent
                    .getFlattenedGeometry();

            /*
             * Compile the mapping of UGCs to geometries for those UGCs
             * enclosing the selected location, as well as a mapping of all UGCs
             * to geometries.
             */
            Map<String, IGeometryData> ugcsEnclosingUserSelectedLocation = new HashMap<>();
            Map<String, IGeometryData> allUgcs = new HashMap<>();
            for (String mapDatabaseTableName : mapDatabaseTableNames) {

                Set<IGeometryData> mapGeometryData = getMapGeometries(
                        hazardEvent, mapDatabaseTableName);
                Set<IGeometryData> mapGeometryDataContainingLocation = getContainingMapGeometries(
                        mapGeometryData, locationAsGeometry);

                addMappingsWarningIfDuplicateKeys(
                        ugcsEnclosingUserSelectedLocation,
                        getUgcsGeometryDataMapping(mapDatabaseTableName,
                                mapGeometryDataContainingLocation));

                addMappingsWarningIfDuplicateKeys(
                        allUgcs,
                        getUgcsGeometryDataMapping(mapDatabaseTableName,
                                mapGeometryData));
            }

            /*
             * Get the base geometry, unioning it if necessary.
             */
            Geometry modifiedHazardBaseGeometry = hazardEventBaseGeometry;
            if (isPointBasedHatching(hazardEvent) == false) {
                modifiedHazardBaseGeometry = AdvancedGeometryUtilities
                        .getUnionOfPolygonalElements(hazardEventBaseGeometry);
            }

            /*
             * Iterate through the enclosing UGCs, toggling each one if
             * appropriate.
             */
            for (String enclosingUgc : ugcsEnclosingUserSelectedLocation
                    .keySet()) {
                Geometry enclosingUgcGeometry = allUgcs.get(enclosingUgc)
                        .getGeometry();

                String hazardArea = hazardAreas.get(enclosingUgc);
                if (isWarngenHatching(hazardEvent)) {
                    addRemoveWarngenHatching(hazardAreas, locationAsGeometry,
                            modifiedHazardBaseGeometry, enclosingUgc,
                            hazardArea);
                    if ((hazardAreas.values().contains(HAZARD_AREA_ALL) == false)
                            && (hazardAreas.values().contains(
                                    HAZARD_AREA_INTERSECTION) == false)) {
                        statusHandler.warn(EMPTY_GEOMETRY_ERROR);
                        return null;
                    }
                    if (hazardAreas.get(enclosingUgc) == HazardConstants.HAZARD_AREA_NONE) {
                        modifiedHazardBaseGeometry = modifiedHazardBaseGeometry
                                .difference(enclosingUgcGeometry);
                    } else {
                        modifiedHazardBaseGeometry = modifiedHazardBaseGeometry
                                .union(enclosingUgcGeometry);
                    }
                } else if (isPointBasedHatching(hazardEvent)) {
                    addRemovePointBased(hazardAreas, enclosingUgc, hazardArea);
                } else {
                    modifiedHazardBaseGeometry = addRemoveGfeHatching(
                            hazardAreas, enclosingUgcGeometry,
                            modifiedHazardBaseGeometry, enclosingUgc,
                            hazardArea);
                    if (modifiedHazardBaseGeometry.isEmpty()) {
                        statusHandler.warn(EMPTY_GEOMETRY_ERROR);
                        return null;
                    }
                }
            }

            /*
             * Return the result, including the modified geometry if the hazard
             * event does not use point-based hatching.
             */
            return new Pair<>(hazardAreas,
                    (isPointBasedHatching(hazardEvent) ? null
                            : modifiedHazardBaseGeometry));
        } catch (TopologyException e) {

            /*
             * /* TODO Consider using GeometryPrecisionReducer.
             */
            statusHandler.error("Encountered topology exception.", e);
            return null;
        }
    }

    /**
     * Build a mapping of UGCs to geometries representing the actual hazard area
     * of the specified hazard event.
     * 
     * @param hazardEvent
     *            Hazard event for which to build the area.
     * @return Map of UGCs to geometries describing the actual hazard area
     *         associated with this event.
     */
    public Map<String, IGeometryData> buildHazardAreaForEvent(
            IHazardEvent hazardEvent) {
        Set<String> mapDatabaseTableNames = getMapDatabaseTableNames(hazardEvent);
        String mapLabelParameter = getMapLabelParameter(hazardEvent);
        String cwa = configManager.getSiteID();
        Map<String, IGeometryData> geometriesForUgcs = new HashMap<>();
        for (String mapDatabaseTableName : mapDatabaseTableNames) {
            addMappingsWarningIfDuplicateKeys(
                    geometriesForUgcs,
                    buildHazardAreaForEvent(mapDatabaseTableName,
                            mapLabelParameter, cwa, hazardEvent));
        }
        return geometriesForUgcs;
    }

    /**
     * Clip the geometry GFE-style for the specified hazard event to the County
     * Warning Area (CWA).
     * 
     * @param hazardEvent
     *            Hazard event.
     * @return Clipped geometry.
     */
    public Geometry applyGfeClipping(IHazardEvent hazardEvent) {

        /*
         * Lazy evaluation is required because the SessionConfigurationManager
         * is not fully initialized when this class is constructed.
         */
        if (cwaGeometry == null) {
            cwaGeometry = buildCwaGeometry();
        }

        Geometry hazardGeometry = hazardEvent.getFlattenedGeometry();

        List<Geometry> intersectedGeometries = new ArrayList<>(
                hazardGeometry.getNumGeometries());
        for (int i = 0; i < hazardGeometry.getNumGeometries(); ++i) {
            Geometry g = hazardGeometry.getGeometryN(i);

            if (cwaGeometry.intersects(g)) {
                /*
                 * A documented means of avoiding some intersection problems
                 * that can occur. See
                 * http://tsusiatsoftware.net/jts/jts-faq/jts-faq.html#D9
                 */
                g = GeometryPrecisionReducer.reduce(g, precisionModel);
                Geometry intersectionGeometry = cwaGeometry.intersection(g);
                intersectedGeometries.add(intersectionGeometry);

            }
        }
        Geometry result;
        if (intersectedGeometries.isEmpty()) {

            /*
             * Return an empty geometry.
             */
            result = geometryFactory.createGeometryCollection(null);
        } else {
            result = geometryFactory
                    .createGeometryCollection(intersectedGeometries
                            .toArray(new Geometry[intersectedGeometries.size()]));
            result = result.union();
        }

        return result;
    }

    /**
     * Clip the geometry Warngen-style for the specfied hazard event.
     * 
     * @param hazardEvent
     *            Hazard event to be clipped.
     * @param hazardType
     *            Hazard type entry.
     * @return Clipped geometry.
     */
    public Geometry applyWarngenClipping(ObservedHazardEvent selectedEvent,
            HazardTypeEntry hazardType) {
        Geometry polygonUnion;
        Collection<IGeometryData> geoDataSet = buildHazardAreaForEvent(
                selectedEvent).values();

        List<Geometry> geometries = new ArrayList<>();

        /*
         * Use a union so that the geometry has no interior lines.
         */
        polygonUnion = geometryFactory.createMultiPolygon(null);
        for (IGeometryData geoData : geoDataSet) {
            Geometry geometry = geoData.getGeometry();
            if (geometry instanceof LineString || geometry instanceof Point) {
                continue;
            } else if (geometry instanceof GeometryCollection) {
                GeometryCollection geometryCollection = (GeometryCollection) geometry;
                int numGeo = geometryCollection.getNumGeometries();
                for (int n = 0; n < numGeo; n++) {
                    Geometry g = geometryCollection.getGeometryN(n);
                    if (g instanceof LineString || g instanceof Point) {
                        continue;
                    } else {
                        polygonUnion = polygonUnion.union(g);
                    }
                }
            } else {
                polygonUnion = polygonUnion.union(geometry);
            }
        }

        /*
         * A GeometryCollection is returned because there may be goosenecks
         * (LineStrings) (from a previous conversion to product geometry). The
         * union can be a multi-polygon, so split it out into separate polygons
         * since a collection is being created anyway.
         */
        for (int i = 0; i < polygonUnion.getNumGeometries(); i++) {
            geometries.add(polygonUnion.getGeometryN(i));
        }
        Geometry[] geometriesAsArray = geometries
                .toArray(new Geometry[geometries.size()]);
        GeometryCollection result = geometryFactory
                .createGeometryCollection(geometriesAsArray);
        return result;
    }

    /**
     * Determine whether or not the specified hazard event is of a non-hatching
     * type.
     * 
     * @param hazardEvent
     *            Hazard event to be checked.
     * @return <code>true</code> if the hazard event is a non-hatching type,
     *         <code>false</code> otherwise.
     */
    public boolean isNonHatching(IHazardEvent hazardEvent) {
        HazardTypeEntry hazardTypeEntry = getHazardTypeEntry(hazardEvent);
        return ((hazardTypeEntry != null) && (hazardTypeEntry
                .getHatchingStyle() == HatchingStyle.NONE));
    }

    /**
     * Determine whether or not the specified hazard event is of a Warngen
     * hatching type.
     * 
     * @param hazardEvent
     *            Hazard event to be checked.
     * @return <code>true</code> if the hazard event uses Warngen style
     *         hatching, <code>false</code> otherwise.
     */
    public boolean isWarngenHatching(IHazardEvent hazardEvent) {
        HazardTypeEntry hazardTypeEntry = getHazardTypeEntry(hazardEvent);
        return ((hazardTypeEntry != null) && (hazardTypeEntry
                .getHatchingStyle() == HatchingStyle.WARNGEN));
    }

    /**
     * Determine whether or not the specified hazard event is point-based.
     * 
     * @param hazardEvent
     *            Hazard event to be checked.
     * @return <code>true</code> if the hazard event is point-based,
     *         <code>false</code> otherwise.
     */
    public boolean isPointBasedHatching(IHazardEvent hazardEvent) {
        HazardTypeEntry hazardTypeEntry = getHazardTypeEntry(hazardEvent);
        return ((hazardTypeEntry != null) && hazardTypeEntry.isPointBased());
    }

    // Private Methods

    /**
     * Get the map database table names for the specified hazard event.
     * 
     * @param hazardEvent
     *            Hazard event
     * @return Names of the tables in the map database from which to retrieve
     *         map geometries for the hazard event.
     */
    private Set<String> getMapDatabaseTableNames(IHazardEvent hazardEvent) {
        return getHazardTypeEntry(hazardEvent).getUgcTypes();
    }

    /**
     * Get the map database table label parameter for the specified hazard
     * event.
     * 
     * @param hazardEvent
     *            Hazard event for which to find the map database table label
     *            parameter.
     * @return Name of the field in the map database tables associated with this
     *         hazard event's UGCs to use for labeling.
     */
    private String getMapLabelParameter(IHazardEvent hazardEvent) {
        String mapLabelParameter = configManager.getHazardTypes()
                .get(hazardEvent.getHazardType()).getUgcLabel();
        return mapLabelParameter;
    }

    /**
     * Add or remove the specified UGC from the specified hazard areas map,
     * assuming Warngen-style hatching, depending upon whether the specified
     * geometries intersect and the specified hazard area type.
     * 
     * @param hazardAreas
     *            Hazard areas from which to add or remove the specified UGC.
     * @param locationAsGeometry
     *            Geometry to be checked to see if it intersects the other
     *            geometry.
     * @param nonPointGeometry
     *            Geometry to be checked to see if it intersects the location
     *            geometry.
     * @param ugc
     *            UGC to be added or removed.
     * @param hazardAreaType
     *            Current hazard area type.
     */
    private void addRemoveWarngenHatching(Map<String, String> hazardAreas,
            Geometry locationAsGeometry, Geometry nonPointGeometry, String ugc,
            String hazardAreaType) {
        if (nonPointGeometry.intersects(locationAsGeometry)) {

            /*
             * The hazard area type is null when due to thresholding the
             * hatching in this area not defined to begin with.
             */
            if (hazardAreaType == null
                    || hazardAreaType.equals(HAZARD_AREA_NONE)) {
                hazardAreas.put(ugc, HAZARD_AREA_INTERSECTION);
            } else {
                hazardAreas.put(ugc, HAZARD_AREA_NONE);
            }
        } else {

            /*
             * The hazard area type is null when the user clicks on a UGC that
             * wasn't previously included in the hazardArea.
             */
            if ((hazardAreaType == null)
                    || (hazardAreaType.equals(HAZARD_AREA_ALL) == false)) {
                hazardAreas.put(ugc, HAZARD_AREA_ALL);
            } else {
                hazardAreas.put(ugc, HAZARD_AREA_NONE);
            }
        }
    }

    /**
     * Add or remove the specified UGC from the specified hazard areas map,
     * assuming point-based, depending upon the specified hazard area type.
     * 
     * @param hazardAreas
     *            Hazard area from which to add or remove the specified UGC.
     * @param ugc
     *            UGC to be added or removed.
     * @param hazardAreaType
     *            Current hazard area type.
     */
    private void addRemovePointBased(Map<String, String> hazardAreas,
            String ugc, String hazardAreaType) {
        if ((hazardAreaType == null)
                || (hazardAreaType.equals(HAZARD_AREA_ALL) == false)) {
            hazardAreas.put(ugc, HAZARD_AREA_ALL);
        } else {
            hazardAreas.put(ugc, HAZARD_AREA_NONE);
        }
    }

    /**
     * Add or remove the specified UGC from the specified hazard areas map, and
     * modify the specified geometry accordingly, assuming GFE-style hatching,
     * depending upon the specified hazard area type.
     * 
     * @param hazardAreas
     *            Hazard areas from which to add or remove the specified UGC.
     * @param ugcGeometry
     *            Geometry to be added to or removed from the other geometry,
     *            depending upon the hazard area type.
     * @param geometry
     *            Geometry to be modified.
     * @param ugc
     *            UGC to be added or removed.
     * @param hazardAreaType
     *            Current hazard area type.
     * @return Geometry with the UGC geometry added to or removed from it.
     */
    private Geometry addRemoveGfeHatching(Map<String, String> hazardAreas,
            Geometry ugcGeometry, Geometry geometry, String ugc,
            String hazardAreaType) {
        if ((hazardAreaType == null)
                || (hazardAreaType.equals(HAZARD_AREA_ALL) == false)) {
            geometry = geometry.union(ugcGeometry);
            hazardAreas.put(ugc, HAZARD_AREA_ALL);
        } else {
            geometry = geometry.difference(ugcGeometry);
            hazardAreas.put(ugc, HAZARD_AREA_NONE);
        }
        return geometry;
    }

    /**
     * Add the mappings contained in the specified map to the first map, logging
     * a warning if at least one key was found to be in both the original and
     * the new mappings prior to addition.
     * 
     * @param map
     *            Map to which to add the mappings.
     * @param mappingsToBeAdded
     *            Mappings to be added.
     */
    private void addMappingsWarningIfDuplicateKeys(
            Map<String, IGeometryData> map,
            Map<String, IGeometryData> mappingsToBeAdded) {
        if (addMappings(map, mappingsToBeAdded)) {
            statusHandler
                    .warn("At least one UGC name was found in more than one maps database "
                            + "table; assuming they reference the same spatial area.");
        }
    }

    /**
     * Add the mappings contained in the specified map to the first map.
     * 
     * @param map
     *            Map to which to add the mappings.
     * @param mappingsToBeAdded
     *            Mappings to be added.
     * @return <code>true</code> if at least one key was found in both the
     *         original map and the new mappings prior to the addition.
     */
    private <K, V> boolean addMappings(Map<K, V> map,
            Map<K, V> mappingsToBeAdded) {
        int oldCount = map.size();
        map.putAll(mappingsToBeAdded);
        return (oldCount + mappingsToBeAdded.size() > map.size());
    }

    /**
     * Get the geometries from the maps database which intersect all of the
     * geometries associated with the specified hazard event.
     * 
     * @param applyIntersectionThreshold
     *            <code>true</code> if the intersection thresholds for this
     *            hazard type should be applied when finding the intersection.
     * @param hazardEvent
     *            Hazard event to determine the intersections.
     * @param mapDatabaseTableName
     *            Name of the map database table from which to fetch geometries
     *            to be checked for intersection.
     * @return Intersecting map database geometries. This set will be empty if
     *         there are no intersecting geometries.
     */
    private Set<IGeometryData> getIntersectingMapGeometries(
            boolean applyIntersectionThreshold, IHazardEvent hazardEvent,
            String mapDatabaseTableName) {
        return getIntersectingMapGeometries(applyIntersectionThreshold,
                hazardEvent,
                getMapGeometries(hazardEvent, mapDatabaseTableName));
    }

    /**
     * Get the geometry data from the specified table in the maps database using
     * the column of the table given by the specified hazard event type's map
     * label parameter.
     * 
     * @param hazardEvent
     *            Hazard event for which to fetch the geometries.
     * @param mapDatabaseTableName
     *            Name of the map database table from which to fetch the
     *            geometries.
     * @return Geometry data for the specified table and hazard event.
     */
    private Set<IGeometryData> getMapGeometries(IHazardEvent hazardEvent,
            String mapDatabaseTableName) {
        return getMapGeometries(mapDatabaseTableName,
                getMapLabelParameter(hazardEvent), configManager.getSiteID());
    }

    /**
     * Get the geometry data from specified table in the maps database for the
     * specified County Warning Area (CWA). This method buffers the requested
     * geometries to speed up query efficiency.
     * 
     * @param mapDatabaseTableName
     *            Name of the maps table to from which to retrieve map
     *            geometries.
     * @param mapLabelParameter
     *            Name of the field in the maps database table to use for
     *            labeling.
     * @param cwa
     *            County Warning Area (CWA) identifier (e.g. OAX).
     * @return Geometry data for the specified maps database table and CWA.
     */
    private Set<IGeometryData> getMapGeometries(String mapDatabaseTableName,
            String mapLabelParameter, String cwa) {

        Map<String, Set<IGeometryData>> mapGeometryMap;

        if (mapGeometryCache.containsKey(cwa)) {
            mapGeometryMap = mapGeometryCache.get(cwa);

            if (mapGeometryMap.containsKey(mapDatabaseTableName)) {
                return mapGeometryMap.get(mapDatabaseTableName);
            }
        }

        IDataRequest mapDataRequest = DataAccessLayer.newDataRequest();
        mapDataRequest.setDatatype("maps");
        String mapdataTable = "mapdata." + mapDatabaseTableName;
        mapDataRequest.addIdentifier(HazardConstants.TABLE_IDENTIFIER,
                mapdataTable);
        mapDataRequest.addIdentifier(HazardConstants.GEOMETRY_FIELD_IDENTIFIER,
                "the_geom");
        if (!cwa.equals(HazardConstants.NATIONAL)) {
            mapDataRequest.addIdentifier(
                    HazardConstants.IN_LOCATION_IDENTIFIER, "true");
            mapDataRequest.addIdentifier(
                    HazardConstants.LOCATION_FIELD_IDENTIFIER,
                    HazardConstants.CWA_IDENTIFIER);
            mapDataRequest.setLocationNames(cwa);
            mapDataRequest.addIdentifier(HazardConstants.CWA_IDENTIFIER, cwa);
        }

        /*
         * Label parameter may be an empty string if defined as such in the
         * HazardTypes.py config file.
         */
        List<String> parameterList = new ArrayList<>();

        if (mapLabelParameter != null && mapLabelParameter.length() > 0) {
            parameterList.add(mapLabelParameter);
        }

        if (ATTRIBUTES_FOR_TABLE_NAMES.containsKey(mapDatabaseTableName)) {
            parameterList.addAll(Lists.newArrayList(ATTRIBUTES_FOR_TABLE_NAMES
                    .get(mapDatabaseTableName)));
        }

        if (parameterList.size() > 0) {
            mapDataRequest.setParameters(parameterList
                    .toArray(new String[parameterList.size()]));
        }

        IGeometryData[] geometryData = DataAccessLayer
                .getGeometryData(mapDataRequest);

        if (mapGeometryCache.containsKey(cwa)) {
            mapGeometryMap = mapGeometryCache.get(cwa);
        } else {
            mapGeometryMap = new HashMap<>();
            mapGeometryCache.put(cwa, mapGeometryMap);
        }

        Set<IGeometryData> result = new HashSet<>();
        for (IGeometryData ig : geometryData) {
            result.add(ig);
        }
        mapGeometryMap.put(mapDatabaseTableName, result);

        return result;
    }

    /**
     * Build a map of UGCs to geometries representing the actual hazard area of
     * the specified hazard event. The UGCs in the map are taken from
     * {@link HazardConstants#HAZARD_AREA} attribute of the hazard event.
     * 
     * @param mapDatabaseTableName
     *            Name of the map database table from which to fetch the hazard
     *            area.
     * @param mapLabelParameter
     *            Name of the field in the map database tables to use for
     *            labeling.
     * @param cwa
     *            County Warning Area.
     * @param hazardEvent
     *            Hazard event for which to build the hazard area.
     * @return Map pairing strings holding UGC identifiers with geometry data;
     *         the union of the latter describes the actual hazard area
     *         associated with this event. Note that if a particular UGC hazard
     *         area for the event is of type
     *         {@link HazardConstants#HAZARD_AREA_INTERSECTION}, then the
     *         identifier for any associated geometry data will consist of both
     *         the UGC and the index of the sub-geometry of the hazard event
     *         with which that data is associated.
     */
    @SuppressWarnings("unchecked")
    private Map<String, IGeometryData> buildHazardAreaForEvent(
            String mapDatabaseTableName, String mapLabelParameter, String cwa,
            IHazardEvent hazardEvent) {

        /*
         * TODO: A linked hash map is used to preserve the order in which items
         * are inserted. This is because this method previously returned a List,
         * but it's unclear whether ordering matters. If it does not matter,
         * this should just be made into a standard map.
         */
        Map<String, IGeometryData> result = new LinkedHashMap<>();

        try {
            Map<String, String> hazardArea = (Map<String, String>) hazardEvent
                    .getHazardAttribute(HAZARD_AREA);

            Set<IGeometryData> mapGeometryData = getMapGeometries(hazardEvent,
                    mapDatabaseTableName);
            Map<String, IGeometryData> mapping = getUgcsGeometryDataMapping(
                    mapDatabaseTableName, mapGeometryData);

            for (String ugc : hazardArea.keySet()) {
                IGeometryData mappingData = mapping.get(ugc);
                if (hazardArea.get(ugc).equals(HAZARD_AREA_ALL)) {
                    result.put(ugc, mappingData);
                } else if (hazardArea.get(ugc).equals(HAZARD_AREA_INTERSECTION)) {
                    Geometry mappingGeometry = mappingData.getGeometry();
                    GeometryCollection asCollection = (GeometryCollection) (HIGH_RESOLUTION_GEOMETRY_IS_VISIBLE
                            .equals(hazardEvent
                                    .getHazardAttribute(VISIBLE_GEOMETRY)) ? hazardEvent
                            .getFlattenedGeometry() : hazardEvent
                            .getProductGeometry());
                    for (int geometryIndex = 0; geometryIndex < asCollection
                            .getNumGeometries(); geometryIndex++) {
                        Geometry geometry = asCollection
                                .getGeometryN(geometryIndex);
                        if (geometry instanceof Polygon) {
                            Geometry intersectionGeometry = mappingGeometry
                                    .intersection(geometry);

                            if (!intersectionGeometry.isEmpty()) {
                                DefaultGeometryData intersectionGeometryData = new DefaultGeometryData();
                                intersectionGeometryData
                                        .setGeometry(intersectionGeometry);
                                result.put(ugc + " " + geometryIndex,
                                        intersectionGeometryData);
                            }
                        }
                    }
                }
            }
        } catch (TopologyException e) {
            statusHandler.error("Unexpected geometry error.  Redraw hazard");
        }

        return result;
    }

    /**
     * Get the mapping of UGC names to map geometries.
     * 
     * @param mapDatabaseTableName
     *            Name of the map database table from which to retrieve
     *            geometries.
     * @param mapGeometryData
     *            Map geometries.
     * @return Mapping between UGC names and map geometries.
     */
    private Map<String, IGeometryData> getUgcsGeometryDataMapping(
            String mapDatabaseTableName, Set<IGeometryData> mapGeometryData) {
        return getUgcBuilder(mapDatabaseTableName).ugcsToMapGeometryData(
                mapGeometryData);
    }

    /**
     * Build the appropriate UGC builder based on the provided maps database
     * table name.
     * 
     * @param mapDatabaseTableName
     *            Name of the map database table.
     * @return UGC builder object which knows how to construct UGCs for the
     *         specified map database table.
     */
    private IugcToMapGeometryDataBuilder getUgcBuilder(String geoTableName) {
        if (UGC_BUILDERS_FOR_TABLE_NAMES.containsKey(geoTableName)) {
            return UGC_BUILDERS_FOR_TABLE_NAMES.get(geoTableName);
        } else {
            statusHandler.error("No UGC handler found for maps database table "
                    + geoTableName);
            return new NullUGCBuilder();
        }
    }

    /**
     * Get the hazard type entry for the specified hazard event.
     * 
     * @param hazardEvent
     *            Hazard event for which to fetch the type entry.
     * @return Hazard type entry.
     */
    private HazardTypeEntry getHazardTypeEntry(IHazardEvent hazardEvent) {
        return configManager.getHazardTypes().get(
                HazardEventUtilities.getHazardType(hazardEvent));
    }

    /**
     * Get the subset of the specified geometry data whose geometries intersect
     * all of the geometries associated with the specified hazard event.
     * 
     * @param applyIntersectionThreshold
     *            <code>true</code> if the intersection thresholds for this
     *            hazard type should be applied when finding the intersection.
     * @param hazardEvent
     *            Hazard event to determine the intersections.
     * @param geometryData
     *            Set of geometries to check for intersection.
     * @return Intersecting map database geometries. This set will be empty if
     *         there are no intersecting geometries.
     */
    private Set<IGeometryData> getIntersectingMapGeometries(
            boolean applyIntersectionThreshold, IHazardEvent hazardEvent,
            Set<IGeometryData> geometryData) {
        HazardTypeEntry hazardTypeEntry = getHazardTypeEntry(hazardEvent);
        boolean inclusionFractionTest = hazardTypeEntry
                .isInclusionFractionTest();
        double inclusionFraction = hazardTypeEntry.getInclusionFraction();
        boolean inclusionAreaTest = hazardTypeEntry.isInclusionAreaTest();
        double inclusionAreaInSqKm = hazardTypeEntry.getInclusionAreaInSqKm();
        Set<IGeometryData> result = getIntersectingMapGeometries(
                applyIntersectionThreshold, hazardEvent, geometryData,
                inclusionFractionTest, inclusionFraction, inclusionAreaTest,
                inclusionAreaInSqKm);

        /*
         * By policy, warned areas are recalculated without thresholding if none
         * meet the thresholds. This finds small polygons counties/zones.
         */
        if (result.isEmpty()) {
            result = getIntersectingMapGeometries(false, hazardEvent,
                    geometryData, inclusionFractionTest, inclusionFraction,
                    inclusionAreaTest, inclusionAreaInSqKm);
        }
        return result;
    }

    /**
     * Build the geometry for the current County Warning Area (CWA).
     * 
     * @return Geometry that was built.
     */
    private Geometry buildCwaGeometry() {

        Set<IGeometryData> mapGeometries = getMapGeometries(
                HazardConstants.MAPDATA_CWA, "", configManager.getSiteID());

        List<Geometry> geometries = new ArrayList<>(mapGeometries.size());
        for (IGeometryData mapGeometryData : mapGeometries) {
            Geometry mappingGeometry = mapGeometryData.getGeometry();
            geometries.add(mappingGeometry.union());
        }
        Geometry result = geometryFactory.createGeometryCollection(geometries
                .toArray(new Geometry[geometries.size()]));
        result = GeometryPrecisionReducer.reduce(result, precisionModel);
        return result;
    }

    /**
     * Get the subset of the specified geometry data whose geometries intersect
     * all of the geometries associated with the specified hazard event.
     * 
     * @param applyIntersectionThreshold
     *            <code>true</code> if the intersection thresholds for this
     *            hazard type should be applied when finding the intersection.
     * @param hazardEvent
     *            Hazard event to determine the intersections.
     * @param geometryData
     *            Set of geometries to check for intersection.
     * @param inclusionFractionTest
     *            Flag indicating whether or not to test for fraction inclusion.
     * @param inclusionFraction
     *            Minimum fraction a hazard polygon must cover within a UGC area
     *            before the UGC can be included in the UGC list.
     * @param inclusionAreaTest
     *            Flag indicating whether or not to test for area inclusion.
     * @param inclusionAreaInSqKm
     *            Minimum area in square kilometers a hazard polygon must cover
     *            within a UGC area before the UGC can be included in the UGC
     *            list.
     * @return Intersecting map database geometries. This set will be empty if
     *         there are no intersecting geometries.
     */
    private Set<IGeometryData> getIntersectingMapGeometries(
            boolean applyIntersectionThreshold, IHazardEvent hazardEvent,
            Set<IGeometryData> geometryData, boolean inclusionFractionTest,
            double inclusionFraction, boolean inclusionAreaTest,
            double inclusionAreaInSqKm) {
        Set<IGeometryData> result = new HashSet<>();
        boolean gfeInteroperability = hazardEvent.getHazardAttributes()
                .containsKey(HazardConstants.GFE_INTEROPERABILITY);

        /*
         * Check for small polygons by making inclusions 0.0.
         */
        double myInclusionFraction = applyIntersectionThreshold ? inclusionFraction
                : 0.0;
        double myInclusionAreaInSqKm = applyIntersectionThreshold ? inclusionAreaInSqKm
                : 0.0;

        /*
         * Get the flattened hazard geometry, and reduce it.
         */
        Geometry hazardGeometry = hazardEvent.getFlattenedGeometry();
        Geometry reducedHazardGeometry = GeometryPrecisionReducer.reduce(
                hazardGeometry, precisionModel);

        /*
         * Iterate through the map geometries, adding each in turn that
         * intersects the hazard event geometry.
         */
        for (IGeometryData geoData : geometryData) {

            /*
             * Iterate over the sub-geometries making up the two geometries,
             * checking them for intersection. Only if at least one of the
             * hazard sub-geometries intersects one of the map sub-geometries
             * should further testing be done for this pair.
             */
            boolean intersection = false;
            Geometry mapGeometry = geoData.getGeometry();
            for (int j = 0; j < hazardGeometry.getNumGeometries(); j++) {
                Geometry hazardSubGeometry = hazardGeometry.getGeometryN(j);
                for (int k = 0; k < mapGeometry.getNumGeometries(); k++) {
                    if (hazardSubGeometry.intersects(mapGeometry
                            .getGeometryN(k))) {
                        intersection = true;
                        break;
                    }
                }
                if (intersection) {
                    break;
                }
            }
            if (intersection == false) {
                continue;
            }

            /*
             * Reduce the map geometry.
             */
            Geometry reducedMapGeometry = GeometryPrecisionReducer.reduce(
                    mapGeometry, precisionModel);

            /*
             * The default rule is to include the geometry.
             */
            boolean defaultIncludeGeometry = true;

            /*
             * A default inclusion test is run on interoperability hazards due
             * to differences in grid resolution.
             */
            if (gfeInteroperability) {
                defaultIncludeGeometry = defaultInclusionTest(
                        reducedMapGeometry, reducedHazardGeometry);
            }

            if (defaultIncludeGeometry) {

                /*
                 * If either of the geometries is a GeometryCollection,
                 * testInclusion(...)'s Geometry.intersection call will throw an
                 * exception. If this is ever found to be an issue, the code
                 * will need to loop over each contained Geometry and call
                 * testInclusion(). However, that is slower than a single call
                 * with complex geometries involving multipolygons, so hopefully
                 * this will not be an issue.
                 */
                boolean includeGeometry = testInclusion(reducedMapGeometry,
                        reducedHazardGeometry, inclusionFractionTest,
                        myInclusionFraction, inclusionAreaTest,
                        myInclusionAreaInSqKm);

                if (includeGeometry) {
                    result.add(geoData);
                }
            } else if (defaultIncludeGeometry) {
                result.add(geoData);
            }
        }
        return result;
    }

    /**
     * Get the subset of the specfied map geometries containing the given
     * geometry.
     * 
     * @param allMapGeometryData
     *            Map geometries from which the subset will be computed.
     * @param geometry
     *            Geometry to be used when checking the map geometries for
     *            containment.
     * @return Subset of map geometries containing this geometry.
     */
    private Set<IGeometryData> getContainingMapGeometries(
            Set<IGeometryData> allMapGeometryData, Geometry geometry) {
        return extractMapGeometries(allMapGeometryData, geometry,
                MapGeometryExtractionApproach.CONTAINING);
    }

    /**
     * Extract from the specified map geometries those which have the specified
     * relationship with the specified geometry.
     * 
     * @param allMapGeometryData
     *            Map geometries from which to extract those that match the
     *            criteria.
     * @param geometry
     *            Geometry to be used to determine whether the map geometries
     *            meet the criteria.
     * @param extractionApproach
     *            Type of approach to use when determining two geometries'
     *            relationship.
     * @return Map geometries that satisfy the input criteria.
     */
    private Set<IGeometryData> extractMapGeometries(
            Set<IGeometryData> allMapGeometryData, Geometry geometry,
            MapGeometryExtractionApproach extractionApproach) {
        Set<IGeometryData> result = new HashSet<>();

        for (IGeometryData mapGeometryData : allMapGeometryData) {
            Geometry mapGeometry = mapGeometryData.getGeometry();

            outer: for (int mapGeometryIndex = 0; mapGeometryIndex < mapGeometry
                    .getNumGeometries(); ++mapGeometryIndex) {
                for (int geometryIndex = 0; geometryIndex < geometry
                        .getNumGeometries(); ++geometryIndex) {

                    Geometry mg = mapGeometry.getGeometryN(mapGeometryIndex);
                    Geometry gg = geometry.getGeometryN(geometryIndex);
                    if (extractionApproach == MapGeometryExtractionApproach.CONTAINING) {
                        if (mg.contains(gg)) {
                            result.add(mapGeometryData);
                            continue outer;
                        }
                    } else if (extractionApproach == MapGeometryExtractionApproach.CONTAINED) {
                        if (gg.contains(mg)) {
                            result.add(mapGeometryData);
                            continue outer;
                        }
                    } else {
                        /*
                         * Some trickiness here. We can't just intersect because
                         * we only want a match if the geometries intersect in
                         * at least some interior points. (i.e. there should be
                         * no match if only boundary points intersect).
                         * Geometry#crosses does not work because it returns
                         * false if all of the points are crossed. So take this
                         * approach instead.
                         */
                        if (gg.intersects(mg)) {
                            Geometry difference = gg.difference(mg);

                            if (!gg.equalsTopo(difference)) {
                                result.add(mapGeometryData);
                                continue outer;
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * Test for inclusion using default rules; this is currently used for
     * interoperability hazards.
     * 
     * @param mapGeometry
     *            Map geometry to be tested for inclusion.
     * @param hazardGeometry
     *            Hazard geometry against which to test.
     * @return <code>true</code> if the map geometry should be included,
     *         <code>false</code> otherwise.
     */
    private boolean defaultInclusionTest(Geometry mapGeometry,
            Geometry hazardGeometry) {
        return testInclusion(mapGeometry, hazardGeometry, true,
                DEFAULT_INTEROPERABILITY_OVERLAP_REQUIREMENT, false, 0.0);
    }

    /**
     * Test if the specified map geometry should be included. The test depends
     * on the specified hazard geometry and user configuration of thresholds.
     * <p>
     * Note that even if the configuration says not to do the inclusion fraction
     * test, it must be done for the inclusion being greater than a small number
     * to mitigate the round-off errors in the JTS intersection calculations. If
     * this was not done, then due to round-off error, if the user creates a
     * hazard by selecting by area and specifying a polygon-based hazard type
     * (such as FA.W), the hatching will show neighboring counties erroneously
     * included, and the ultimate product will be wrong as well.
     * </p>
     * 
     * @param mapGeometry
     *            Map geometry to be tested for inclusion.
     * @param hazardGeometry
     *            Hazard geometry against which to test.
     * @param inclusionFractionTest
     *            Flag indicating whether or not to test for fraction inclusion.
     * @param inclusionFraction
     *            Minimum fraction a hazard polygon must cover within a UGC area
     *            before the UGC can be included in the UGC list.
     * @param inclusionAreaTest
     *            Flag indicating whether or not to test for area inclusion.
     * @param inclusionAreaInSqKm
     *            Minimum area in square kilometers a hazard polygon must cover
     *            within a UGC area before the UGC can be included in the UGC
     *            list.
     * @return <code>true</code> if the map geometry should be included,
     *         <code>false</code> otherwise.
     */
    private boolean testInclusion(Geometry mapGeometry,
            Geometry hazardGeometry, boolean inclusionFractionTest,
            double inclusionFraction, boolean inclusionAreaTest,
            double inclusionAreaInSqKm) {
        try {
            Geometry intersection = hazardGeometry.intersection(mapGeometry);
            double intersectionAreaInSquareDegrees = intersection.getArea();
            double intersectionFraction = intersectionAreaInSquareDegrees
                    / mapGeometry.getArea();

            boolean included = false;
            boolean minInclusionSatisfied = intersectionFraction > INCLUSION_FRACTION_MINIMUM;

            if (minInclusionSatisfied) {
                // Check if included by fraction.
                boolean includedByFrac = false;
                if (inclusionFractionTest) {
                    includedByFrac = intersectionFraction > inclusionFraction;
                }

                // Check if included by area.
                boolean includedByArea = false;
                if (inclusionAreaTest) {
                    Point centroid = intersection.getCentroid();
                    double cosLat = Math.cos(Math.toRadians(centroid
                            .getCoordinate().y));
                    double inclusionAreaInSquareDegrees = inclusionAreaInSqKm
                            / (KM_PER_DEGREE_AT_EQUATOR
                                    * KM_PER_DEGREE_AT_EQUATOR * cosLat);

                    includedByArea = intersectionAreaInSquareDegrees > inclusionAreaInSquareDegrees;
                }

                if (inclusionFractionTest && !inclusionAreaTest) {
                    // Only the inclusion fraction test is active.
                    included = includedByFrac;
                } else if (!inclusionFractionTest && inclusionAreaTest) {
                    // Only the inclusion area test is active.
                    included = includedByArea;
                } else if (inclusionFractionTest && inclusionAreaTest) {
                    // Both the inclusion fraction and inclusion area tests are
                    // active and both must indicate inclusion.
                    included = includedByFrac && includedByArea;
                } else {
                    // Neither test is required, so inclusion is automatic.
                    included = true;
                }
            }

            return included;
        } catch (TopologyException e) {

            /*
             * TODO Use GeometryPrecisionReducer?
             */
            statusHandler.warn(
                    "Encountered topology exception when attempting "
                            + "to test for inclusion: ", e.getMessage());
            return false;
        }
    }

    /**
     * Get the CWA geometry.
     * 
     * @return County Warning Area geometry.
     */
    public Geometry getCwaGeometry() {
        if (cwaGeometry == null) {
            cwaGeometry = buildCwaGeometry();
        }
        return cwaGeometry;
    }
}
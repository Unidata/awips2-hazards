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
package com.raytheon.uf.viz.hazards.sessionmanager.hatching;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.raytheon.uf.common.dataaccess.DataAccessLayer;
import com.raytheon.uf.common.dataaccess.IDataRequest;
import com.raytheon.uf.common.dataaccess.geom.IGeometryData;
import com.raytheon.uf.common.dataaccess.impl.DefaultGeometryData;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.config.ISessionConfigurationManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.types.HazardTypeEntry;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

/**
 * Misc functionality for building the hatched areas associated with a hazard
 * event. This needs to be in the VIZ plugin.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 02, 2014 2536      blawrenc     Initial creation
 * 
 * </pre>
 * 
 * @author blawrenc
 * @version 1.0
 */

public class HatchingUtilities {

    /**
     * Contains the mappings between geometry tables in the geodatabase and
     * attributes that need to be retrieved from them to support UGC mapping.
     */
    private static Map<String, String[]> geoTableAttributesMap;

    /**
     * Look-up table for UGC-related columns for tables in the maps geodatabase.
     */
    static {
        Map<String, String[]> tempMap = Maps.newHashMap();

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

        geoTableAttributesMap = Collections.unmodifiableMap(tempMap);
    }

    /**
     * Map for caching geometries retrieved from the geo database.
     */
    private static Map<String, Map<String, List<IGeometryData>>> mapGeometryCache = Maps
            .newHashMap();

    /**
     * Given one or more events, returns a list of geometries from the maps
     * database which intersects all of the geometries associated with each the
     * events.
     * 
     * @param mapDBtableName
     *            The map database table to retrieve geometries from
     * @param labelParameter
     *            The name of the field in the maps db table to use for
     *            labeling.
     * @param cwa
     *            The identifier of the county warning area (e.g. OAX)
     * @param hazardEvent
     *            The hazard event to determine hatching for.
     * @param applyIntersectionThreshold
     *            Whether or not an intersection threshold is applied to the
     *            hazard. This is generally used for short-fused hazards in
     *            determining which counties are included as a part of the
     *            products associated with the hazard.
     * 
     * @return A set of intersecting map db geometries. This set will be empty
     *         if there are no intersecting geometries.
     */
    public static Set<IGeometryData> getIntersectingMapGeometries(
            String mapDBtableName, String labelParameter, String cwa,
            boolean applyIntersectionThreshold,
            ISessionConfigurationManager configManager,
            final IHazardEvent hazardEvent) {

        Geometry geometry = hazardEvent.getGeometry();

        HazardTypeEntry hazardTypeEntry = configManager.getHazardTypes().get(
                hazardEvent.getHazardType());

        Set<IGeometryData> intersectingGeometries = Sets.newHashSet();

        List<IGeometryData> geometryDataList = getMapGeometries(mapDBtableName,
                labelParameter, cwa);

        for (IGeometryData geoData : geometryDataList) {

            Geometry mapGeometry = geoData.getGeometry();

            outer: for (int j = 0; j < mapGeometry.getNumGeometries(); ++j) {

                for (int i = 0; i < geometry.getNumGeometries(); ++i) {

                    Geometry hazardGeometry = geometry.getGeometryN(i);
                    Geometry mapdbGeometry = mapGeometry.getGeometryN(j);

                    if (hazardGeometry.intersects(mapdbGeometry)) {
                        if (applyIntersectionThreshold) {
                            boolean includeGeometry = testInclusion(
                                    mapdbGeometry, hazardGeometry,
                                    hazardTypeEntry);

                            if (includeGeometry
                                    || mapdbGeometry.contains(hazardGeometry)
                                    || hazardGeometry.contains(mapdbGeometry)) {
                                intersectingGeometries.add(geoData);
                                continue outer;
                            }
                        } else {
                            intersectingGeometries.add(geoData);
                            continue outer;
                        }
                    }
                }
            }
        }

        return intersectingGeometries;

    }

    /**
     * Returns a list of geometry data from the maps geodatabase for the
     * specified cwa and maps table.
     * 
     * This method buffers the requested geometries to speed up query
     * efficiency.
     * 
     * @param mapDBtableName
     *            The name of the maps table to retrieve map geometries from
     * @param labelParameter
     *            The name of the field in the maps db table to use for
     *            labeling.
     * @param cwa
     *            The county warning area identifier (e.g. OAX)
     * @return A list of geometry data for the specified geo table and cwa.
     */
    public static List<IGeometryData> getMapGeometries(String mapDBtableName,
            String labelParameter, String cwa) {

        Map<String, List<IGeometryData>> mapGeometryMap;

        if (mapGeometryCache.containsKey(cwa)) {
            mapGeometryMap = mapGeometryCache.get(cwa);

            if (mapGeometryMap.containsKey(mapDBtableName)) {
                return mapGeometryMap.get(mapDBtableName);
            }
        }

        IDataRequest mapDataRequest = DataAccessLayer.newDataRequest();
        mapDataRequest.setDatatype("maps");
        String mapdataTable = "mapdata." + mapDBtableName;
        mapDataRequest.addIdentifier(HazardConstants.TABLE_IDENTIFIER,
                mapdataTable);
        mapDataRequest.addIdentifier(HazardConstants.GEOMETRY_FIELD_IDENTIFIER,
                "the_geom");
        mapDataRequest.addIdentifier(HazardConstants.IN_LOCATION_IDENTIFIER,
                "true");
        mapDataRequest.addIdentifier(HazardConstants.LOCATION_FIELD_IDENTIFIER,
                HazardConstants.CWA_IDENTIFIER);
        mapDataRequest.setLocationNames(cwa);
        mapDataRequest.addIdentifier(HazardConstants.CWA_IDENTIFIER, cwa);

        /*
         * Label parameter may be an empty string if defined as such in the
         * HazardTypes.py config file.
         */
        List<String> parameterList = Lists.newArrayList();

        if (labelParameter != null && labelParameter.length() > 0) {
            parameterList.add(labelParameter);
        }

        if (geoTableAttributesMap.containsKey(mapDBtableName)) {
            parameterList.addAll(Lists.newArrayList(geoTableAttributesMap
                    .get(mapDBtableName)));
        }

        if (parameterList.size() > 0) {
            mapDataRequest.setParameters(parameterList
                    .toArray(new String[parameterList.size()]));
        } else {
            /*
             * TODO: Talk to Raytheon about how to avoid supplying a parameter.
             * A Null Pointer Exception results if no parameters are supplied.
             */
            mapDataRequest.setParameters("gid");
        }

        IGeometryData[] geometryData = DataAccessLayer
                .getGeometryData(mapDataRequest);

        if (mapGeometryCache.containsKey(cwa)) {
            mapGeometryMap = mapGeometryCache.get(cwa);
        } else {
            mapGeometryMap = Maps.newHashMap();
            mapGeometryCache.put(cwa, mapGeometryMap);
        }

        List<IGeometryData> geometryDataList = Arrays.asList(geometryData);
        mapGeometryMap.put(mapDBtableName, geometryDataList);

        return geometryDataList;
    }

    /**
     * Given the geometry associated with a hazard event, builds a set of
     * geometries representing the actual hazard area.
     * 
     * @param mapDBtableName
     *            The name of the map db geo table
     * @param labelParameter
     *            The name of the field in the maps db table to use for
     *            labeling.
     * @param cwa
     *            The county warning area
     * @param hazardEvent
     *            The hazard event to build the hazard area for.
     * 
     * @return A set of IGeometryData objects describing the actual hazard area
     *         associated with this event.
     */
    static public Set<IGeometryData> buildHatchedAreaForEvent(
            String mapDBtableName, String labelParameter, String cwa,
            IHazardEvent hazardEvent, ISessionConfigurationManager configManager) {

        Set<IGeometryData> hatchedArea = Sets.newHashSet();

        if (mapDBtableName.equalsIgnoreCase(HazardConstants.POLYGON_TYPE)
                || mapDBtableName.length() == 0) {
            /*
             * The hazard geometry represents the hazard area. Use the CWA
             * boundary as the basis of the hazard area. Hazard Types without a
             * hatch area defined are treated like polygons.
             * 
             * TODO: May need to consider using HSA as well.
             */
            Set<IGeometryData> clippedGeometries = HatchingUtilities
                    .getClippedMapGeometries(HazardConstants.CWA_IDENTIFIER,
                            labelParameter, cwa, hazardEvent);

            hatchedArea.addAll(clippedGeometries);
        } else if (hazardEvent.getHazardAttributes().containsKey(
                HazardConstants.GEOMETRY_MAP_NAME_KEY)) {
            /*
             * Draw-by-area geometry. The polygon itself represents the hazard
             * area.
             */
            Set<IGeometryData> containedGeometries = HatchingUtilities
                    .getContainedMapGeometries(mapDBtableName, labelParameter,
                            cwa, hazardEvent);

            hatchedArea.addAll(containedGeometries);

        } else {
            Set<IGeometryData> intersectingGeometries = HatchingUtilities
                    .getIntersectingMapGeometries(mapDBtableName,
                            labelParameter, cwa, true, configManager,
                            hazardEvent);

            hatchedArea.addAll(intersectingGeometries);

        }

        return hatchedArea;
    }

    /**
     * Given an event, returns a list of geometries from the maps database which
     * are contained in each of the geometries associated with each the of the
     * events.
     * 
     * @param mapDBtableName
     *            The map database table to retrieve geometries from
     * @param labelParameter
     *            The name of the field in the maps db table to use for
     *            labeling.
     * @param cwa
     *            The identifier of the county warning area (e.g. OAX)
     * @param event
     *            The event to process.
     * 
     * @return A set of geometries. This set will be empty if there are no
     *         containing geometries.
     */
    public static Set<IGeometryData> getContainedMapGeometries(
            String mapDBtableName, String labelParameter, String cwa,
            final IHazardEvent event) {

        List<Geometry> geometryList = Lists.newArrayList();

        geometryList.add(event.getGeometry());

        return getContainedMapGeometries(mapDBtableName, labelParameter, cwa,
                geometryList);
    }

    /**
     * Given one or more geometries, returns a list of geometries from the maps
     * database which are contained within each geometry.
     * 
     * @param mapDBtableName
     *            The map database table to retrieve geometries from
     * @param labelParameter
     *            The name of the field in the maps db table to use for
     *            labeling.
     * @param cwa
     *            The identifier of the county warning area (e.g. OAX)
     * 
     * @param geometryList
     *            A list of one or more geometries
     * 
     * @return A set of geometries. This set will be empty if there are no
     *         contained geometries.
     */
    public static Set<IGeometryData> getContainedMapGeometries(
            String mapDBtableName, String labelParameter, String cwa,
            List<Geometry> geometryList) {

        Set<IGeometryData> containedGeometries = Sets.newHashSet();

        List<IGeometryData> geometryDataList = getMapGeometries(mapDBtableName,
                labelParameter, cwa);

        for (IGeometryData geoData : geometryDataList) {
            Geometry mapGeometry = geoData.getGeometry();

            outer: for (int j = 0; j < mapGeometry.getNumGeometries(); ++j) {

                for (Geometry geometry : geometryList) {
                    for (int i = 0; i < geometry.getNumGeometries(); ++i) {

                        if (geometry.getGeometryN(i).contains(
                                mapGeometry.getGeometryN(j))) {
                            containedGeometries.add(geoData);
                            continue outer;
                        }
                    }

                }
            }

        }

        return containedGeometries;

    }

    /**
     * Given an event, returns a list of geometries from the maps database which
     * are clipped to fit within each geometry of the event.
     * 
     * @param mapDBtableName
     *            The map database table to retrieve geometries from
     * @param labelParameter
     *            The name of the field in the maps db table to use for
     *            labeling.
     * @param cwa
     *            The identifier of the county warning area (e.g. OAX)
     * @param event
     *            The event to retrieve clipped map db geometries for
     * 
     * @return A set of geometries. This set will be empty if there are no
     *         intersecting geometries.
     */
    public static Set<IGeometryData> getClippedMapGeometries(
            String mapDBtableName, String labelParameter, String cwa,
            final IHazardEvent event) {

        List<Geometry> geometryList = Lists.newArrayList();

        geometryList.add(event.getGeometry());

        return getClippedMapGeometries(mapDBtableName, labelParameter, cwa,
                geometryList);
    }

    /**
     * Given one or more geometries, returns a set of geometries from the maps
     * database clipped to fit in each geometry.
     * 
     * @param mapDBtableName
     *            The map database table to retrieve geometries from
     * @param labelParameter
     *            The name of the field in the maps db table to use for
     *            labeling.
     * @param cwa
     *            The identifier of the county warning area (e.g. OAX)
     * @param geometryList
     *            A list of one or more geometries
     * 
     * @return A set of geometries. This set will be empty if there are no
     *         clipped geometries.
     */
    public static Set<IGeometryData> getClippedMapGeometries(
            String mapDBtableName, String labelParameter, String cwa,
            List<Geometry> geometryList) {

        GeometryFactory geoFactory = new GeometryFactory();

        Set<IGeometryData> clippedGeometries = Sets.newHashSet();

        List<IGeometryData> geometryDataList = getMapGeometries(mapDBtableName,
                labelParameter, cwa);

        for (Geometry geometry : geometryList) {

            for (int i = 0; i < geometry.getNumGeometries(); ++i) {
                for (IGeometryData geoData : geometryDataList) {

                    for (int k = 0; k < geoData.getGeometry()
                            .getNumGeometries(); ++k) {

                        Geometry clippedGeometry = geoFactory.createPolygon(
                                null, null);

                        if (geoData.getGeometry().getGeometryN(k)
                                .intersects(geometry.getGeometryN(i))) {
                            Geometry intersectionGeometry = geoData
                                    .getGeometry().getGeometryN(k)
                                    .intersection(geometry.getGeometryN(i));

                            clippedGeometry = clippedGeometry
                                    .union(intersectionGeometry);

                            for (int j = 0; j < clippedGeometry
                                    .getNumGeometries(); ++j) {
                                DefaultGeometryData clippedGeoData = new DefaultGeometryData();
                                clippedGeoData.setGeometry(clippedGeometry
                                        .getGeometryN(j));
                                clippedGeometries.add(clippedGeoData);
                            }
                        }
                    }
                }
            }
        }

        return clippedGeometries;
    }

    /**
     * Tests whether or not to include a geometry from the maps database based
     * on how much a hazard geometry intersects it. Whether or not to test
     * inclusion and what the minimum inclusion is is driven by configuration.
     * 
     * @param mapGeometry
     *            - The geometry to test for inclusion
     * @param hazardGeometry
     *            - The geometry to test for intersection
     * @param hazardTypeEntry
     *            - Hazard type configuration specifying whether or not to test
     *            inclusion and what the minimum inclusion is
     * 
     * @return true - include the map geometry, false - do not include the map
     *         geometry
     */
    private static boolean testInclusion(Geometry mapGeometry,
            Geometry hazardGeometry, HazardTypeEntry hazardTypeEntry) {

        boolean testInclusion = hazardTypeEntry.isInclusionTest();

        if (testInclusion) {

            double inclusionPercentage = hazardTypeEntry
                    .getInclusionPercentage();
            Geometry intersection = hazardGeometry.intersection(mapGeometry);
            double intersectionPercentage = intersection.getArea()
                    / mapGeometry.getArea();

            return intersectionPercentage > inclusionPercentage;
        }

        return true;
    }
}

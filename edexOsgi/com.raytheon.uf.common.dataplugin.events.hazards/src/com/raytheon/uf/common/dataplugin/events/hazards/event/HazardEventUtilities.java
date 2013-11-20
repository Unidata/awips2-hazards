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
package com.raytheon.uf.common.dataplugin.events.hazards.event;

import java.util.Arrays;
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
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

/**
 * Misc functionality for reformatting attributes for hazard events.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 24, 2013 1257       bsteffen    Initial creation
 * Oct 22, 2013 1463       blawrenc   Added methods to retrieve
 *                                    map geometries which 
 *                                    intersect hazard geometries.
 * 
 * 
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */

public class HazardEventUtilities {

    /**
     * Map for caching geometries retrieved from the geo database.
     */
    private static Map<String, Map<String, List<IGeometryData>>> mapGeometryCache = Maps
            .newHashMap();

    public static String getPhenSigSubType(IHazardEvent event) {
        return getPhenSigSubType(event.getPhenomenon(),
                event.getSignificance(), event.getSubtype());
    }

    public static String getPhenSigSubType(String phen, String sig,
            String subType) {
        if (phen == null || sig == null) {
            return null;
        }
        StringBuilder str = new StringBuilder();
        str.append(phen);
        str.append('.');
        str.append(sig);
        if (subType != null) {
            str.append('.');
            str.append(subType);
        }
        return str.toString();
    }

    public static void populateEventForPhenSigSubtype(IHazardEvent event,
            String phenSigSubtype) {
        int endPhen = phenSigSubtype.indexOf('.');
        event.setPhenomenon(phenSigSubtype.substring(0, endPhen));
        int endSig = phenSigSubtype.indexOf('.', endPhen + 1);
        if (endSig > 0) {
            event.setSignificance(phenSigSubtype.substring(endPhen + 1, endSig));
            event.setSubtype(phenSigSubtype.substring(endSig + 1));
        } else {
            event.setSignificance(phenSigSubtype.substring(endPhen + 1));
        }
    }

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
     * @param eventList
     *            A list of one or more events.
     * 
     * @return A set of intersecting map db geometries. This set will be empty
     *         if there are no intersecting geometries.
     */
    public static Set<IGeometryData> getIntersectingMapGeometries(
            String mapDBtableName, String labelParameter, String cwa,
            final IHazardEvent... eventList) {

        List<Geometry> geometryList = Lists.newArrayList();

        for (IHazardEvent event : eventList) {
            geometryList.add(event.getGeometry());
        }

        return getIntersectingMapGeometries(mapDBtableName, labelParameter,
                cwa, geometryList);
    }

    /**
     * Given one or more geometries, returns a list of geometries from the maps
     * database which intersects all of the geometries.
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
     * @return A set of intersecting map db geometries. This set will be empty
     *         if there are no intersecting geometries.
     */

    public static Set<IGeometryData> getIntersectingMapGeometries(
            String mapDBtableName, String labelParameter, String cwa,
            List<Geometry> geometryList) {

        Set<IGeometryData> intersectingGeometries = Sets.newHashSet();

        List<IGeometryData> geometryDataList = getMapGeometries(mapDBtableName,
                labelParameter, cwa);

        for (IGeometryData geoData : geometryDataList) {

            Geometry mapGeometry = geoData.getGeometry();
            Boolean intersectsAllGeometries = true;

            for (Geometry geometry : geometryList) {
                if (!geometry.intersects(mapGeometry)) {
                    intersectsAllGeometries = false;
                    break;
                }
            }

            if (intersectsAllGeometries) {
                intersectingGeometries.add(geoData);
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
         * TODO: Talk to Raytheon about how to avoid supplying a parameter. Gid
         * seems to be common across the mapdata tables.
         */
        /*
         * This is clunky, but I need some way of retrieving names for zones,
         * counties, etc. which can be used to create a more meaningful error
         * message for the forecaster when conflicts are found.
         */
        mapDataRequest.setParameters("gid");

        /*
         * Label parameter may be an empty string if defined as such in the
         * HazardTypes.py config file.
         */
        if (labelParameter.length() > 0) {
            mapDataRequest.setParameters(labelParameter);
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
     * geometries representing the actual warned area.
     * 
     * @param mapDBtableName
     *            The name of the map db geo table
     * @param labelParameter
     *            The name of the field in the maps db table to use for
     *            labeling.
     * @param cwa
     *            The county warning area
     * @param hazardEvent
     *            The hazard event to build the warned area for.
     * 
     * @return A set of IGeometryData objects describing the actual warned area
     *         associated with this event.
     */
    static public Set<IGeometryData> buildWarnedAreaForEvent(
            String mapDBtableName, String labelParameter, String cwa,
            IHazardEvent hazardEvent) {

        Set<IGeometryData> warnedArea = Sets.newHashSet();

        if (mapDBtableName.equalsIgnoreCase(HazardConstants.POLYGON_TYPE)
                || mapDBtableName.length() == 0) {
            /*
             * The hazard geometry represents the warned area. Use the CWA
             * boundary as the basis of the warned area. Hazard Types without a
             * hatch area defined are treated like polygons.
             * 
             * TODO: May need to consider using HSA as well.
             */
            Set<IGeometryData> clippedGeometries = HazardEventUtilities
                    .getClippedMapGeometries(HazardConstants.CWA_IDENTIFIER,
                            labelParameter, cwa, hazardEvent);

            warnedArea.addAll(clippedGeometries);
        } else if (hazardEvent.getHazardAttributes().containsKey(
                HazardConstants.GEOMETRY_MAP_NAME_KEY)) {
            /*
             * Draw-by-area geometry. The polygon itself represents the warned
             * area.
             */
            Set<IGeometryData> containedGeometries = HazardEventUtilities
                    .getContainedMapGeometries(mapDBtableName, labelParameter,
                            cwa, hazardEvent);

            warnedArea.addAll(containedGeometries);

        } else {
            Set<IGeometryData> intersectingGeometries = HazardEventUtilities
                    .getIntersectingMapGeometries(mapDBtableName,
                            labelParameter, cwa, hazardEvent);

            warnedArea.addAll(intersectingGeometries);

        }

        return warnedArea;
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

        for (Geometry geometry : geometryList) {

            for (IGeometryData geoData : geometryDataList) {
                Geometry mapGeometry = geoData.getGeometry();

                if (geometry.contains(mapGeometry)) {
                    containedGeometries.add(geoData);
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
     *         contained geometries.
     */
    public static Set<IGeometryData> getClippedMapGeometries(
            String mapDBtableName, String labelParameter, String cwa,
            List<Geometry> geometryList) {

        GeometryFactory geoFactory = new GeometryFactory();

        Set<IGeometryData> clippedGeometries = Sets.newHashSet();

        List<IGeometryData> geometryDataList = getMapGeometries(mapDBtableName,
                labelParameter, cwa);

        for (Geometry geometry : geometryList) {

            Geometry clippedGeometry = geoFactory.createPolygon(null, null);

            for (IGeometryData geoData : geometryDataList) {
                Geometry mapGeometry = geoData.getGeometry();

                if (mapGeometry.intersects(geometry)) {
                    Geometry intersectionGeometry = mapGeometry
                            .intersection(geometry);
                    clippedGeometry = clippedGeometry
                            .union(intersectionGeometry);
                    DefaultGeometryData clippedGeoData = new DefaultGeometryData();
                    clippedGeoData.setGeometry(clippedGeometry);
                    clippedGeometries.add(clippedGeoData);
                }
            }
        }

        return clippedGeometries;

    }

}

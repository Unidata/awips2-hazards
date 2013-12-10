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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.raytheon.uf.common.dataaccess.DataAccessLayer;
import com.raytheon.uf.common.dataaccess.IDataRequest;
import com.raytheon.uf.common.dataaccess.geom.IGeometryData;
import com.raytheon.uf.common.dataaccess.impl.DefaultGeometryData;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardState;
import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.HazardQueryBuilder;
import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.IHazardEventManager;
import com.raytheon.uf.common.dataplugin.events.hazards.event.collections.HazardHistoryList;
import com.raytheon.uf.common.dataplugin.events.hazards.requests.HazardEventIdRequest;
import com.raytheon.uf.common.serialization.comm.RequestRouter;
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
 * Nov 14, 2013 1472       bkowal      Renamed hazard subtype to subType
 * Oct 22, 2013 1463       blawrenc   Added methods to retrieve
 *                                    map geometries which 
 *                                    intersect hazard geometries.
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

    public static String getHazardType(IHazardEvent event) {
        return getHazardType(event.getPhenomenon(), event.getSignificance(),
                event.getSubType());
    }

    public static String getHazardType(String phen, String sig, String subType) {
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

    public static void populateEventForHazardType(IHazardEvent event,
            String hazardType) {
        int endPhen = hazardType.indexOf('.');
        event.setPhenomenon(hazardType.substring(0, endPhen));
        int endSig = hazardType.indexOf('.', endPhen + 1);
        if (endSig > 0) {
            event.setSignificance(hazardType.substring(endPhen + 1, endSig));
            event.setSubType(hazardType.substring(endSig + 1));
        } else {
            event.setSignificance(hazardType.substring(endPhen + 1));
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

        outer: for (IGeometryData geoData : geometryDataList) {

            Geometry mapGeometry = geoData.getGeometry();

            for (Geometry geometry : geometryList) {

                for (int i = 0; i < geometry.getNumGeometries(); ++i) {
                    if (geometry.getGeometryN(i).intersects(mapGeometry)) {
                        intersectingGeometries.add(geoData);
                        continue outer;
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
         * TODO: Talk to Raytheon about how to avoid supplying a parameter. A
         * Null Pointer Exception results if no parameters are supplied.
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
            IHazardEvent hazardEvent) {

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
            Set<IGeometryData> clippedGeometries = HazardEventUtilities
                    .getClippedMapGeometries(HazardConstants.CWA_IDENTIFIER,
                            labelParameter, cwa, hazardEvent);

            hatchedArea.addAll(clippedGeometries);
        } else if (hazardEvent.getHazardAttributes().containsKey(
                HazardConstants.GEOMETRY_MAP_NAME_KEY)) {
            /*
             * Draw-by-area geometry. The polygon itself represents the hazard
             * area.
             */
            Set<IGeometryData> containedGeometries = HazardEventUtilities
                    .getContainedMapGeometries(mapDBtableName, labelParameter,
                            cwa, hazardEvent);

            hatchedArea.addAll(containedGeometries);

        } else {
            Set<IGeometryData> intersectingGeometries = HazardEventUtilities
                    .getIntersectingMapGeometries(mapDBtableName,
                            labelParameter, cwa, hazardEvent);

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

        outer: for (IGeometryData geoData : geometryDataList) {
            Geometry mapGeometry = geoData.getGeometry();

            for (Geometry geometry : geometryList) {
                for (int i = 0; i < geometry.getNumGeometries(); ++i) {

                    if (geometry.getGeometryN(i).contains(mapGeometry)) {
                        containedGeometries.add(geoData);
                        continue outer;
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

                    Geometry clippedGeometry = geoFactory.createPolygon(null,
                            null);

                    if (geoData.getGeometry().intersects(
                            geometry.getGeometryN(i))) {
                        Geometry intersectionGeometry = geoData.getGeometry()
                                .intersection(geometry);
                        clippedGeometry = clippedGeometry
                                .union(intersectionGeometry);
                        DefaultGeometryData clippedGeoData = new DefaultGeometryData();
                        clippedGeoData.setGeometry(clippedGeometry);
                        clippedGeometries.add(clippedGeoData);
                    }
                }
            }
        }

        return clippedGeometries;

    }

    /**
     * Returns a {@link HazardState} based on the VTEC action code.
     * 
     * @param action
     * @return
     */
    public static HazardState stateBasedOnAction(String action) {
        if ("CAN".equals(action) || "EXP".equals(action)) {
            return HazardState.ENDED;
        } else {
            return HazardState.ISSUED;
        }
    }

    public static List<String> parseEtns(String etns) {
        List<String> parsed = new ArrayList<String>();
        if (etns.contains("[")) {
            etns = etns.replaceAll("\\[|\\]", "");
            String[] split = etns.split(",");
            parsed = Arrays.asList(split);
        } else if (etns.isEmpty() == false) {
            parsed.add(etns);
        }
        return parsed;
    }

    public static boolean isDuplicate(IHazardEventManager manager,
            IHazardEvent event) {
        HazardQueryBuilder builder = new HazardQueryBuilder();
        builder.addKey(HazardConstants.SITEID, event.getSiteID());
        builder.addKey(HazardConstants.PHENOMENON, event.getPhenomenon());
        builder.addKey(HazardConstants.SIGNIFICANCE, event.getSignificance());
        Map<String, HazardHistoryList> hazards = manager
                .getEventsByFilter(builder.getQuery());
        boolean toStore = true;
        for (HazardHistoryList list : hazards.values()) {
            Iterator<IHazardEvent> iter = list.iterator();
            while (iter.hasNext()) {
                IHazardEvent ev = iter.next();
                toStore = HazardEventUtilities.checkDifferentEvents(ev, event);
                if (toStore == false) {
                    break;
                }
            }
            if (toStore == false) {
                break;
            }
        }
        return toStore;
    }

    public static String determineEtn(String site, String action, String etn,
            IHazardEventManager manager) {
        // make a request for the hazard event id from the cluster task
        // table
        HazardEventIdRequest request = new HazardEventIdRequest();
        request.setSiteId(site);
        String value = "";
        boolean createNew = false;
        if (HazardConstants.NEW_ACTION.equals(action) == false) {
            Map<String, HazardHistoryList> map = manager.getBySiteID(site);
            for (Entry<String, HazardHistoryList> entry : map.entrySet()) {
                HazardHistoryList list = entry.getValue();
                for (IHazardEvent ev : list) {
                    List<String> hazEtns = HazardEventUtilities
                            .parseEtns(String.valueOf(ev
                                    .getHazardAttribute(HazardConstants.ETNS)));
                    List<String> recEtn = HazardEventUtilities.parseEtns(etn);
                    if (compareEtns(hazEtns, recEtn)) {
                        value = ev.getEventID();
                        break;
                    }
                }
            }
            if ("".equals(value)) {
                createNew = true;
            }
        }
        if ("NEW".equals(action) || createNew) {
            try {
                value = RequestRouter.route(request).toString();
            } catch (Exception e) {
                throw new RuntimeException(
                        "Unable to make request for hazard event id", e);
            }
        }
        return value;
    }

    /**
     * Determines if events are the same or are different.
     * 
     * @param event1
     * @param event2
     * @return
     */
    public static boolean checkDifferentEvents(IHazardEvent event1,
            IHazardEvent event2) {
        if (event1.getSiteID().equals(event2.getSiteID()) == false) {
            return true;
        }
        if (event1.getPhenomenon().equals(event2.getPhenomenon()) == false) {
            return true;
        }
        if (event1.getSignificance().equals(event2.getSignificance()) == false) {
            return true;
        }
        // TODO, this is necessary when we use the mode to issue products later
        // on
        // if (event1.getHazardMode().equals(event2.getHazardMode()) == false) {
        // return true;
        // }

        Object obj1 = event1.getHazardAttribute(HazardConstants.ETNS);
        List<String> etns1 = null;
        List<String> etns2 = null;
        // this will become OBE by refactor work, right now we have cases where
        // it is a string and some where it is a list
        if (obj1 instanceof String) {
            etns1 = HazardEventUtilities.parseEtns((String) event1
                    .getHazardAttribute(HazardConstants.ETNS));
        } else {
            etns1 = new ArrayList<String>();
            List<Integer> list = (List<Integer>) obj1;
            for (Integer in : list) {
                etns1.add(String.valueOf(in));
            }
        }

        Object obj2 = event2.getHazardAttribute(HazardConstants.ETNS);
        if (obj2 instanceof String) {
            etns2 = HazardEventUtilities.parseEtns((String) event2
                    .getHazardAttribute(HazardConstants.ETNS));
        } else {
            etns2 = new ArrayList<String>();
            List<Integer> list = (List<Integer>) obj2;
            for (Integer in : list) {
                etns2.add(String.valueOf(in));
            }
        }
        if (compareEtns(etns1, etns2) == false) {
            return true;
        }
        return false;
    }

    /**
     * Comparing if any of the ETNs of the first list match any of the second
     * list. The lists can be different lengths depending on the code that hits
     * this.
     * 
     * @param etns1
     * @param etns2
     * @return
     */
    private static boolean compareEtns(List<String> etns1, List<String> etns2) {
        for (String etn1 : etns1) {
            for (String etn2 : etns2) {
                if (Integer.valueOf(etn1).equals(Integer.valueOf(etn2))) {
                    return true;
                }
            }
        }
        return false;
    }
}

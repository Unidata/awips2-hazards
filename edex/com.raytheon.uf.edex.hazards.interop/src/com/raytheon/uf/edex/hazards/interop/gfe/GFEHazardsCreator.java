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
package com.raytheon.uf.edex.hazards.interop.gfe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opengis.referencing.operation.TransformException;

import com.raytheon.uf.common.dataaccess.DataAccessLayer;
import com.raytheon.uf.common.dataaccess.IDataRequest;
import com.raytheon.uf.common.dataaccess.geom.IGeometryData;
import com.raytheon.uf.common.dataplugin.PluginDataObject;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardStatus;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.ProductClass;
import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.HazardEventManager;
import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.HazardEventManager.Mode;
import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.IHazardEventManager;
import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardEventUtilities;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.interoperability.HazardInteroperabilityConstants.INTEROPERABILITY_TYPE;
import com.raytheon.uf.common.dataplugin.gfe.db.objects.GridParmInfo;
import com.raytheon.uf.common.dataplugin.warning.AbstractWarningRecord;
import com.raytheon.uf.common.dataplugin.warning.PracticeWarningRecord;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.TimeRange;
import com.raytheon.uf.edex.hazards.interoperability.util.InteroperabilityUtil;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

/**
 * Supports GFE to Hazard Services interoperability.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 22, 2013            mnash     Initial creation
 * March 3, 2014 3034      bkowal    Improved comparisons of existing hazards.
 *                                   GFE hazards do not initially have ETNs.
 * Mar 24, 2014  3323      bkowal    Use the mode to retrieve the correct
 *                                   GridParmInfo.
 * Apr 08, 2014  3357      bkowal    Updated to use the new interoperability tables.
 * Dec 04, 2014  2826      dgilling  Remove unneeded methods.
 * 
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */

public class GFEHazardsCreator {

    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(GFEHazardsCreator.class);

    // cache of the fips for each state
    private static final Map<String, String> countyFips = new HashMap<String, String>();

    // constants for querying
    private static final String TABLE = "table";

    private static final String GEOM_FIELD = "geomField";

    private static final String MAPS_DB = "maps";

    private static final String LOCATION_FIELD = "locationField";

    private static final String FIPS = "fips";

    private static final String IN_LOCATION = "inLocation";

    private static final String THE_GEOM = "the_geom";

    private static final String MAPDATA_ZONE = "mapdata.zone";

    private static final String MAPDATA_STATES = "mapdata.states";

    private static final String MAPDATA_COUNTY = "mapdata.county";

    private static final String MAPDATA_FIREWX_ZONES = "mapdata.firewxzones";

    private static final String MAPDATA_OFFSHORE_ZONES = "mapdata.offshore";

    private static final String MAPDATA_MARINE_ZONES = "mapdata.marinezones";

    private static final String ID = "id";

    private static final String STATE_ZONE = "state_zone";

    private static final String STATE = "state";

    private static final char ZONE_CHAR = 'Z';

    private static final char COUNTY_CHAR = 'C';

    private static final List<String> MARINE_ZONE_PREFIXES = Arrays.asList(
            "AM", "AN", "GM", "LC", "LE", "LH", "LM", "LO", "LS", "PH", "PK",
            "PM", "PS", "PZ", "SL");

    private final List<String> zones = new ArrayList<String>();

    private final List<String> counties = new ArrayList<String>();

    private final List<String> marineZones = new ArrayList<String>();

    private final List<String> offshoreZones = new ArrayList<String>();

    private final List<String> firewxZones = new ArrayList<String>();

    private final List<String> retrievedAreas = new ArrayList<String>();

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.common.actionregistry.IActionable#handleAction(java.util
     * .Map)
     */
    public void createHazards(List<PluginDataObject> objects) {
        if (objects.isEmpty() == false) {
            for (PluginDataObject ob : objects) {
                if (ob instanceof AbstractWarningRecord) {
                    resetZones();

                    AbstractWarningRecord rec = (AbstractWarningRecord) ob;
                    Mode mode = null;
                    boolean practice = (rec instanceof PracticeWarningRecord);
                    if (practice) {
                        mode = Mode.PRACTICE;
                    } else {
                        mode = Mode.OPERATIONAL;
                    }
                    HazardEventManager manager = new HazardEventManager(mode);

                    buildAreas(rec.getUgcZones());

                    // do querying for all the zones/counties tables
                    List<IGeometryData> geomData = new ArrayList<IGeometryData>();
                    if (counties.isEmpty() == false) {
                        geomData.addAll(getData(counties, MAPDATA_COUNTY, FIPS));
                    }

                    if (zones.isEmpty() == false) {
                        geomData.addAll(getData(zones, MAPDATA_ZONE, STATE_ZONE));
                    }

                    if (firewxZones.isEmpty() == false) {
                        geomData.addAll(getData(firewxZones,
                                MAPDATA_FIREWX_ZONES, STATE_ZONE));
                    }

                    if (marineZones.isEmpty() == false) {
                        geomData.addAll(getData(marineZones,
                                MAPDATA_MARINE_ZONES, ID));
                    }
                    if (offshoreZones.isEmpty() == false) {
                        geomData.addAll(getData(offshoreZones,
                                MAPDATA_OFFSHORE_ZONES, ID));
                    }

                    Geometry geom = buildGeometry(geomData);

                    GridParmInfo gridParmInfo = null;
                    try {
                        gridParmInfo = GridRequestHandler.requestGridParmInfo(
                                mode, rec.getXxxid());
                    } catch (Exception e) {
                        statusHandler.error(
                                "Failed to retrieve Grid Parm Info for site: "
                                        + rec.getXxxid() + ".", e);
                        return;
                    }

                    Geometry newHazardGfeGeometry = null;
                    try {
                        newHazardGfeGeometry = GFERecordUtil
                                .translateHazardPolygonToGfe(
                                        gridParmInfo.getGridLoc(), geom);
                    } catch (TransformException e) {
                        statusHandler
                                .error("GFE Geometry conversion has failed for the new Hazard!",
                                        e);
                        return;
                    }

                    IHazardEvent event = createEvent(manager, rec,
                            newHazardGfeGeometry, practice);

                    final TimeRange hazardGfeTimeRange = GFERecordUtil
                            .createGridTimeRange(event.getStartTime(),
                                    event.getEndTime(),
                                    gridParmInfo.getTimeConstraints());

                    List<IHazardEvent> eventsToUpdate = this.getEventsToUpdate(
                            manager, event, hazardGfeTimeRange.getStart(),
                            hazardGfeTimeRange.getEnd(), rec.getEtn());

                    /*
                     * If an existing event was not found, create a new event.
                     */
                    if (eventsToUpdate == null) {
                        if (event.getEndTime().getTime() < event.getStartTime()
                                .getTime()) {
                            statusHandler
                                    .warn("Product "
                                            + rec.getXxxid()
                                            + " ends before it begins. Skipping record!");
                            return;
                        }
                        statusHandler.info("Creating event: "
                                + event.getEventID());

                        manager.storeEvent(event);

                        InteroperabilityUtil.newOrUpdateInteroperabilityRecord(
                                event, rec.getEtn(), INTEROPERABILITY_TYPE.GFE);
                    } else {
                        for (IHazardEvent eventToUpdate : eventsToUpdate) {
                            if (this.updateHazardWithProduct(eventToUpdate, rec)) {
                                manager.updateEvent(eventToUpdate);
                            }

                            InteroperabilityUtil
                                    .newOrUpdateInteroperabilityRecord(
                                            eventToUpdate, rec.getEtn(),
                                            INTEROPERABILITY_TYPE.GFE);
                        }
                    }
                }
            }
        }
    }

    private List<IHazardEvent> getEventsToUpdate(HazardEventManager manager,
            IHazardEvent potentialEvent, final Date startDate,
            final Date endDate, final String etn) {
        /*
         * Attempt to retrieve the associated hazard from the gfe
         * interoperability table, if there is one.
         */
        List<IHazardEvent> events = GfeInteroperabilityUtil
                .queryForInteroperabilityHazards(potentialEvent.getSiteID(),
                        HazardEventUtilities.getHazardType(potentialEvent),
                        startDate, endDate, manager);
        if (events != null && events.isEmpty() == false) {
            return events;
        }

        /*
         * If it is not found in the gfe interoperability table, check the
         * primary interoperability table - it may be associated with a
         * different site.
         */
        return InteroperabilityUtil.queryInteroperabilityByETNForHazards(
                manager, potentialEvent.getSiteID(),
                HazardEventUtilities.getHazardType(potentialEvent), etn, null);
    }

    private void resetZones() {
        retrievedAreas.clear();
        marineZones.clear();
        offshoreZones.clear();
        zones.clear();
        counties.clear();
        firewxZones.clear();
    }

    /**
     * Builds a list of zone/county area codes for lookup
     * 
     * @param zones
     * @return
     */
    private void buildAreas(Set<String> areas) {
        for (String area : areas) {
            if (area.charAt(2) == ZONE_CHAR) {
                String prefix = area.substring(0, 2);
                if (MARINE_ZONE_PREFIXES.contains(prefix)) {
                    marineZones.add(prefix + area.substring(3));
                    offshoreZones.add(prefix + area.substring(3));
                } else {
                    zones.add(prefix + area.substring(3));
                    firewxZones.add(prefix + area.substring(3));
                }
            } else if (area.charAt(2) == COUNTY_CHAR) {
                String state = area.substring(0, 2);
                if (countyFips.containsKey(state) == false) {
                    IDataRequest request = DataAccessLayer.newDataRequest();
                    request.setDatatype(MAPS_DB);
                    request.addIdentifier(TABLE, MAPDATA_STATES);
                    request.addIdentifier(GEOM_FIELD, THE_GEOM);
                    request.addIdentifier(STATE, state);
                    request.setParameters(FIPS);
                    request.addIdentifier(LOCATION_FIELD, FIPS);
                    String[] data = DataAccessLayer
                            .getAvailableLocationNames(request);
                    if (data.length > 0) {
                        countyFips.put(state, data[0]);
                    }
                }
                counties.add(countyFips.get(state) + area.substring(3));
            }
        }
    }

    /**
     * Retrieves the associated data from tables to get the Geoemtries
     * 
     * @return
     */
    private List<IGeometryData> getData(List<String> areas, String table,
            String locationField) {
        IDataRequest request = DataAccessLayer.newDataRequest();
        request.setDatatype(MAPS_DB);
        request.addIdentifier(TABLE, table);
        request.addIdentifier(LOCATION_FIELD, locationField);
        request.addIdentifier(GEOM_FIELD, THE_GEOM);
        request.addIdentifier(IN_LOCATION, "true");
        request.setLocationNames(areas.toArray(new String[0]));
        return Arrays.asList(DataAccessLayer.getGeometryData(request));
    }

    /**
     * Builds the Geometry based on the results from the DAF
     * 
     * @param data
     * @return
     */
    private Geometry buildGeometry(List<IGeometryData> data) {
        GeometryFactory factory = new GeometryFactory();
        List<Geometry> geometries = new ArrayList<Geometry>();
        for (IGeometryData d : data) {
            if (retrievedAreas.contains(d.getLocationName()) == false) {
                geometries.add(d.getGeometry());
                retrievedAreas.add(d.getLocationName());
            }
        }

        return factory.createGeometryCollection(
                geometries.toArray(new Geometry[0])).buffer(0);
    }

    /**
     * Sets all values on the IHazardEvent object based on the
     * AbstractWarningRecord
     * 
     * @param manager
     * @param rec
     * @param geom
     * @return
     */
    private IHazardEvent createEvent(IHazardEventManager manager,
            AbstractWarningRecord rec, Geometry geom, boolean practice) {
        IHazardEvent event = manager.createEvent();
        event.setSiteID(rec.getXxxid());
        event.setPhenomenon(rec.getPhen());
        event.setSignificance(rec.getSig());
        event.setStartTime(rec.getStartTime().getTime());
        event.setEndTime(rec.getEndTime().getTime());
        ProductClass value = ProductClass.TEST;
        for (ProductClass clazz : ProductClass.values()) {
            if (clazz.getAbbreviation().equals(rec.getProductClass())) {
                value = clazz;
                break;
            }
        }
        event.setHazardMode(value);
        event.setGeometry(geom);

        try {
            event.setEventID(HazardEventUtilities.generateEventID(
                    rec.getXxxid(), practice));
        } catch (Exception e) {
            throw new RuntimeException("Unable to generate hazard event id", e);
        }
        this.updateHazardWithProduct(event, rec);

        return event;
    }

    private boolean updateHazardWithProduct(IHazardEvent event,
            AbstractWarningRecord rec) {
        HazardStatus state = HazardEventUtilities.stateBasedOnAction(rec
                .getAct());
        if (state == event.getStatus()) {
            /*
             * Skip the event if it is already in the correct state.
             */
            statusHandler.info("Skipping update of event: "
                    + event.getEventID());
            return false;
        }
        event.setStatus(state);
        event.addHazardAttribute(HazardConstants.EXPIRATION_TIME, rec
                .getPurgeTime().getTime().getTime());
        event.addHazardAttribute(HazardConstants.ETNS, "[" + rec.getEtn() + "]");
        event.addHazardAttribute(HazardConstants.VTEC_CODES, "[" + rec.getAct()
                + "]");
        event.addHazardAttribute(HazardConstants.PILS, "[" + rec.getPil() + "]");
        event.addHazardAttribute(HazardConstants.ISSUE_TIME, rec.getIssueTime()
                .getTime().getTime());
        event.setCreationTime(rec.getIssueTime().getTime());

        return true;
    }
}

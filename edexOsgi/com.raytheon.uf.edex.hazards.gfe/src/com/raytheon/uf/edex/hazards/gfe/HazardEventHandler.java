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
package com.raytheon.uf.edex.hazards.gfe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.Iterator;

import org.springframework.util.StringUtils;

import com.raytheon.edex.site.SiteUtil;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardState;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.ProductClass;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardNotification;
import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.HazardEventManager;
import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.HazardQueryBuilder;
import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.HazardEventManager.Mode;
import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardEventUtilities;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.collections.HazardHistoryList;
import com.raytheon.uf.common.dataplugin.gfe.dataaccess.GFEDataAccessUtil;
import com.raytheon.uf.common.dataplugin.gfe.db.objects.GFERecord;
import com.raytheon.uf.common.dataplugin.gfe.db.objects.GridParmInfo;
import com.raytheon.uf.common.dataplugin.gfe.db.objects.ParmID;
import com.raytheon.uf.common.dataplugin.gfe.discrete.DiscreteKey;
import com.raytheon.uf.common.dataplugin.gfe.grid.Grid2DBit;
import com.raytheon.uf.common.dataplugin.gfe.reference.ReferenceData;
import com.raytheon.uf.common.dataplugin.gfe.reference.ReferenceID;
import com.raytheon.uf.common.dataplugin.gfe.reference.ReferenceData.CoordinateType;
import com.raytheon.uf.common.dataplugin.gfe.request.GetGridDataRequest;
import com.raytheon.uf.common.dataplugin.gfe.server.message.ServerResponse;
import com.raytheon.uf.common.dataplugin.gfe.server.notify.GridUpdateNotification;
import com.raytheon.uf.common.dataplugin.gfe.server.request.GetGridRequest;
import com.raytheon.uf.common.dataplugin.gfe.slice.DiscreteGridSlice;
import com.raytheon.uf.common.serialization.SerializationUtil;
import com.raytheon.uf.common.serialization.comm.RequestRouter;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.common.time.TimeRange;
import com.vividsolutions.jts.geom.MultiPolygon;

/**
 * Handles a HazardEvent object, such as creating and deleting a GFE grid. This
 * grid is stored in the forecast and official database. This class also takes
 * into account merging with existing grids.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 27, 2013 2277       jsanchez     Initial creation
 * Jan 14, 2014 2755       bkowal       Updated for GFE interoperability. Will
 *                                      now listen for GFE grid updates.
 * 
 * </pre>
 * 
 * @author jsanchez
 * @version 1.0
 */

public class HazardEventHandler {

    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(HazardEventHandler.class);

    private Map<String, GridParmInfo> gridParmInfoMap;

    private static HazardEventHandler instance = new HazardEventHandler();

    private GridParmInfo gridParmInfo;

    private static final String HAZARD_PARM_NAME = "Hazards";

    private static final String PARM_PRACTICE_FCST = ".+_Prac_Fcst_.+";

    private static final String PARM_TEST_FCST = ".+_Test_Fcst_.+";

    private static final String PARM_OPERATIONAL_FCST = ".+__Fcst_.+";

    private static final Pattern PARM_PRACTICE_PATTERN = Pattern
            .compile(PARM_PRACTICE_FCST);

    private static final Pattern PARM_TEST_PATTERN = Pattern
            .compile(PARM_TEST_FCST);

    private static final Pattern PARM_OPERATIONAL_PATTERN = Pattern
            .compile(PARM_OPERATIONAL_FCST);

    // TODO: will be standardized when usage has been refined in Phase II of
    // GFE interoperability modifications.
    private static final String HAZARD_ATTRIBUTE_INTEROPERABILITY = "interoperability";

    /*
     * TODO: this is only a temporary implementation for the time being for the
     * automated tests. When the remaining changes for GFE interoperability are
     * completed, alternate solutions may be implemented.
     */
    private List<String> updatedHazardIDs;

    /**
     * Constructor.
     */
    private HazardEventHandler() {
        gridParmInfoMap = new HashMap<String, GridParmInfo>();
        this.updatedHazardIDs = new ArrayList<String>();
    }

    /**
     * Returns an instance of a HazardEventHandler.
     * 
     * @param siteID
     * @return
     */
    public static synchronized HazardEventHandler getInstance() {
        if (instance == null) {
            instance = new HazardEventHandler();
        }
        instance.setGridParmInfo(SiteUtil.getSite());
        return instance;
    }

    private void setGridParmInfo(String siteID) {
        gridParmInfo = gridParmInfoMap.get(siteID);
        if (gridParmInfo == null) {
            try {
                gridParmInfo = GridRequestHandler.requestGridParmInfo(siteID);
                gridParmInfoMap.put(siteID, gridParmInfo);
                statusHandler.info("HazardEventHandler initialized for "
                        + siteID);
            } catch (Exception e) {
                statusHandler.error(
                        "Unable to create hazard event handler. Please verify you have "
                                + siteID + " activated via GFE.", e);
            }
        }
    }

    /**
     * Handles notifications from camel and creates or deletes a grid
     * appropriately
     */
    public void handleNotification(byte[] bytes) throws Exception {
        HazardNotification notification = SerializationUtil
                .transformFromThrift(HazardNotification.class, bytes);
        IHazardEvent hazardEvent = notification.getEvent();

        switch (notification.getType()) {
        case STORE:
        case UPDATE:
            String phenSig = hazardEvent.getPhenomenon() + "."
                    + hazardEvent.getSignificance();
            boolean convert = GridValidator.needsGridConversion(phenSig);

            /*
             * Determine if this is a hazard that we have just updated due to a
             * grid update and/or creation. This will ensure that we do not
             * attempt to create grids and/or update hazards more times than we
             * need to.
             */
            if (convert
                    && hazardEvent.getHazardAttributes().containsKey(
                            HAZARD_ATTRIBUTE_INTEROPERABILITY) == false) {
                synchronized (this.updatedHazardIDs) {
                    this.updatedHazardIDs.add(hazardEvent.getEventID());
                }
                /*
                 * if the hazard includes the INTEROPERABILITY attribute, it was
                 * created from a grid; so, the grid that would normally be
                 * created already exists.
                 */
                createGrid(hazardEvent, hazardEvent.getStartTime());
            }
            break;
        case DELETE:
            deleteGrid(hazardEvent);
            break;
        }
    }

    public void handleGridNotification(Object msg) {
        if (msg instanceof Collection) {
            for (Object object : (Collection<?>) msg) {
                if (object instanceof GridUpdateNotification) {
                    GridUpdateNotification gridUpdateNotification = (GridUpdateNotification) object;
                    this.handleHazardsGridUpdateNotification(gridUpdateNotification);
                }
            }
        } else if (msg instanceof GridUpdateNotification) {
            GridUpdateNotification gridUpdateNotification = (GridUpdateNotification) msg;
            this.handleHazardsGridUpdateNotification(gridUpdateNotification);
        }
    }

    private void handleHazardsGridUpdateNotification(
            GridUpdateNotification gridUpdateNotification) {
        if (HAZARD_PARM_NAME.equals(gridUpdateNotification.getParmId()
                .getParmName()) == false) {
            return;
        }

        final String parmID = gridUpdateNotification.getParmId().toString();
        final String siteID = gridUpdateNotification.getSiteID();
        GetGridRequest req = new GetGridRequest(
                gridUpdateNotification.getParmId(),
                Arrays.asList(gridUpdateNotification.getHistories().keySet()
                        .toArray(new TimeRange[0])));

        GetGridDataRequest request = new GetGridDataRequest();
        request.addRequest(req);
        request.setSiteID(siteID);
        request.setWorkstationID(gridUpdateNotification.getWorkstationID());

        ServerResponse<?> serverResponse = null;
        try {
            Object object = RequestRouter.route(request);

            if (object instanceof ServerResponse) {
                serverResponse = (ServerResponse<?>) object;
            }
        } catch (Exception e) {
            statusHandler.error("Failed to retrieve grid data!", e);
            return;
        }

        if (serverResponse.isOkay() == false) {
            statusHandler.error("Failed to retrieve grid data: "
                    + serverResponse.getMessages());
            return;
        }

        Mode mode = this.determineHazardsMode(parmID);
        if (mode == null) {
            statusHandler
                    .warn("Failed to determine mode associated with parmID: "
                            + parmID);
            return;
        }
        HazardEventManager hazardEventManager = new HazardEventManager(mode);
        // for now default to PRACTICE mode because everything else in
        // hazard services is.
        // TODO: remove the line below so that the event manager
        // will be based on the true mode.
        hazardEventManager = new HazardEventManager(Mode.PRACTICE);

        @SuppressWarnings("unchecked")
        List<DiscreteGridSlice> gridSlices = (List<DiscreteGridSlice>) serverResponse
                .getPayload();

        for (DiscreteGridSlice discreteGridSlice : gridSlices) {
            Map<DiscreteKey, List<Grid2DBit>> gridsToProcessMap = new HashMap<DiscreteKey, List<Grid2DBit>>();
            this.constructGridsToProcessMap(gridsToProcessMap,
                    discreteGridSlice);

            if (gridsToProcessMap.isEmpty()) {
                continue;
            }

            // retrieve the parameters that will be used for the query
            // from the grid.
            Date startDate = discreteGridSlice.getValidTime().getStart();
            Date endDate = discreteGridSlice.getValidTime().getEnd();
            // there will be one or multiple keys with the hazard type. create a
            // list of discrete keys that will need to be processed. skip keys
            // that
            // are only set to <None>.

            Iterator<DiscreteKey> keyIterator = gridsToProcessMap.keySet()
                    .iterator();
            while (keyIterator.hasNext()) {
                final DiscreteKey discreteKey = keyIterator.next();
                final String hazardType = discreteKey.toString();

                HazardQueryBuilder builder = new HazardQueryBuilder();
                builder.addKey(HazardConstants.SITE_ID, siteID);
                builder.addKey(HazardConstants.PHEN_SIG, hazardType);
                builder.addKey(HazardConstants.HAZARD_EVENT_START_TIME,
                        startDate);
                builder.addKey(HazardConstants.HAZARD_EVENT_END_TIME, endDate);

                Map<String, HazardHistoryList> events = hazardEventManager
                        .getEventsByFilter(builder.getQuery());

                IHazardEvent hazardEvent = null;
                boolean update = false;
                boolean newHazard = false;
                if (events.isEmpty()) {
                    // no hazard to update; create a new hazard.

                    hazardEvent = hazardEventManager.createEvent();
                    newHazard = true;
                    // generate an id
                    try {
                        hazardEvent.setEventID(HazardEventUtilities
                                .generateEventID(siteID));
                    } catch (Exception e) {
                        statusHandler.handle(Priority.PROBLEM,
                                "Failed to generate Hazard Event ID!", e);
                        return;
                    }
                    // initially set the state to PENDING
                    hazardEvent.setState(HazardState.PENDING);

                    // start time
                    hazardEvent.setStartTime(startDate);

                    // end time
                    hazardEvent.setEndTime(endDate);

                    // site id
                    hazardEvent.setSiteID(siteID);

                    // phenomenon & significance (possibly subtype)
                    String[] hazardTypeComponents = StringUtils.split(
                            hazardType, ".");
                    if (hazardTypeComponents.length < 2) {
                        // invalid hazard type; does not include at least both
                        // a phenomenon and a significance.
                        continue;
                    }

                    String phenomenon = hazardTypeComponents[0];
                    hazardEvent.setPhenomenon(phenomenon);
                    String significance = hazardTypeComponents[1];
                    hazardEvent.setSignificance(significance);
                    // is there a subtype?
                    if (hazardTypeComponents.length == 3) {
                        String subtype = hazardTypeComponents[2];
                        hazardEvent.setSubType(subtype);
                    }

                    // set the interoperability flag
                    // for now we will just use a generic boolean; however,
                    // additional metadata will be stored in this field in the
                    // next
                    // phase of GFE interoperability changes.
                    hazardEvent.addHazardAttribute(
                            HAZARD_ATTRIBUTE_INTEROPERABILITY, true);
                } else {
                    // update the hazard that was retrieved.

                    hazardEvent = events.entrySet().iterator().next()
                            .getValue().get(0);
                    boolean originator = false;
                    synchronized (this.updatedHazardIDs) {
                        if (this.updatedHazardIDs.contains(hazardEvent
                                .getEventID())) {
                            this.updatedHazardIDs.remove(hazardEvent
                                    .getEventID());
                            originator = true;
                        }
                    }
                    if (originator) {
                        return;
                    }
                }

                // if the hazard is in pending and has been
                // submitted, update the state, issue time, and mode (for now).
                if (hazardEvent.getState() == HazardState.PENDING
                        && discreteGridSlice
                                .getGridDataHistory()
                                .get(discreteGridSlice.getGridDataHistory()
                                        .size() - 1).getPublishTime() != null) {
                    hazardEvent.setState(HazardState.PROPOSED);

                    // we will also set the ProductClass and issue time now that
                    // the
                    // state has been set to PROPOSED.
                    hazardEvent.setIssueTime(new Date());

                    hazardEvent.setHazardMode(this
                            .determineProductClass(parmID));
                    update = true;
                }

                Grid2DBit grid2DBit = this
                        .mergeGridDataStructures(gridsToProcessMap
                                .get(discreteKey));
                ReferenceData referenceData = new ReferenceData(
                        discreteGridSlice.getGridParmInfo().getGridLoc(),
                        new ReferenceID("temp"), grid2DBit);
                MultiPolygon multiPolygon = referenceData
                        .getPolygons(CoordinateType.LATLON);
                if (hazardEvent.getGeometry() == null) {
                    hazardEvent.setGeometry(multiPolygon);
                } else if (hazardEvent.getGeometry().equalsExact(multiPolygon) == false) {
                    hazardEvent.setGeometry(multiPolygon);
                    update = true;
                }

                if (newHazard) {
                    hazardEventManager.storeEvent(hazardEvent);
                    statusHandler.info("Created hazard "
                            + hazardEvent.getEventID());
                } else if (update) {
                    hazardEventManager.updateEvent(hazardEvent);
                    statusHandler.info("Updated hazard "
                            + hazardEvent.getEventID());
                } else {
                    statusHandler.info("No action has been taken for hazard "
                            + hazardEvent.getEventID());
                }
            }
        }
    }

    private Grid2DBit mergeGridDataStructures(List<Grid2DBit> grids) {
        if (grids.isEmpty()) {
            return null;
        }

        if (grids.size() == 1) {
            return grids.get(0);
        }

        Grid2DBit mergedGrid2DBit = grids.remove(0);
        for (Grid2DBit grid2DBit : grids) {
            mergedGrid2DBit = mergedGrid2DBit.or(grid2DBit);
        }
        return mergedGrid2DBit;
    }

    private void constructGridsToProcessMap(
            Map<DiscreteKey, List<Grid2DBit>> gridsToProcessMap,
            DiscreteGridSlice discreteGridSlice) {
        for (int keyIndex = 0; keyIndex < discreteGridSlice.getKeyList().size(); keyIndex++) {
            DiscreteKey discreteKey = discreteGridSlice.getKeys()[keyIndex];
            String hazardType = discreteGridSlice.getKeyList().get(keyIndex);
            if (DiscreteGridSliceUtil.DK_NONE.equals(hazardType)) {
                continue;
            }

            if (discreteKey.getSubKeys().size() > 1) {
                List<DiscreteGridSlice> seperatedGridSlices = DiscreteGridSliceUtil
                        .separate(discreteGridSlice,
                                discreteGridSlice.getValidTime(), null);
                for (DiscreteGridSlice innerGridSlice : seperatedGridSlices) {
                    this.constructGridsToProcessMap(gridsToProcessMap,
                            innerGridSlice);
                }
            } else {
                if (gridsToProcessMap.containsKey(discreteKey) == false) {
                    gridsToProcessMap.put(discreteKey,
                            new ArrayList<Grid2DBit>());
                }
                // geometry - convert the bit data
                gridsToProcessMap.get(discreteKey).add(
                        discreteGridSlice.eq(discreteKey));
            }
        }
    }

    private Mode determineHazardsMode(String parmID) {
        if (PARM_OPERATIONAL_PATTERN.matcher(parmID).matches()) {
            return Mode.OPERATIONAL;
        } else if (PARM_TEST_PATTERN.matcher(parmID).matches()
                || PARM_PRACTICE_PATTERN.matcher(parmID).matches()) {
            return Mode.PRACTICE;
        }

        return null;
    }

    private ProductClass determineProductClass(String parmID) {
        if (PARM_OPERATIONAL_PATTERN.matcher(parmID).matches()
                || PARM_PRACTICE_PATTERN.matcher(parmID).matches()) {
            return ProductClass.OPERATIONAL;
        } else if (PARM_TEST_PATTERN.matcher(parmID).matches()) {
            return ProductClass.TEST;
        }

        return null;
    }

    /**
     * Creates a gfe grid from an IHazardEvent object. The grid is stored in the
     * forecast and official database.
     * 
     * @param hazardEvent
     * @param currentDate
     */
    public void createGrid(IHazardEvent hazardEvent, Date currentDate)
            throws Exception {
        List<GFERecord> newGFERecords = new ArrayList<GFERecord>();
        TimeRange timeRange = GFERecordUtil.createGridTimeRange(
                hazardEvent.getStartTime(), hazardEvent.getEndTime(),
                gridParmInfo.getTimeConstraints());

        // find hazards that intersect with hazardEvent time range
        List<GFERecord> intersectingRecords = GridRequestHandler
                .findIntersectedGrid(gridParmInfo.getParmID(), timeRange);

        // create a new grid slice from the hazard event
        GFERecord convertedHazardEvent = GFERecordUtil.createGFERecord(
                hazardEvent, gridParmInfo);

        // separate the records and identify which slices need to be merged.
        SeparatedRecords separatedRecords = DiscreteGridSliceUtil
                .separateGFERecords(convertedHazardEvent, intersectingRecords,
                        currentDate);

        // add GFERecords that only had their time ranges updated
        if (separatedRecords.newRecords != null
                && !separatedRecords.newRecords.isEmpty()) {
            newGFERecords.addAll(separatedRecords.newRecords);
        }
        // perform merge
        if (separatedRecords.slicesToMerge != null
                && !separatedRecords.slicesToMerge.isEmpty()) {
            newGFERecords.addAll(DiscreteGridSliceUtil.mergeGridSlices(
                    gridParmInfo, separatedRecords.slicesToMerge));
        }

        // store new records
        TimeRange replacementTimeRange = GFERecordUtil
                .getReplacementTimeRange(newGFERecords);
        GridRequestHandler.store(newGFERecords, gridParmInfo,
                replacementTimeRange);
    }

    /**
     * Deletes the associated grid of the IHazardEvent object from the forecast
     * and official database.
     * 
     * @param hazardEvent
     */
    public void deleteGrid(IHazardEvent hazardEvent) throws Exception {
        String phenSig = hazardEvent.getPhenomenon() + "."
                + hazardEvent.getSignificance();
        TimeRange timeRange = GFERecordUtil.createGridTimeRange(
                hazardEvent.getStartTime(), hazardEvent.getEndTime(),
                gridParmInfo.getTimeConstraints());
        ParmID parmID = gridParmInfo.getParmID();

        // find hazards that intersect with hazardEvent time range
        List<GFERecord> intersectingRecords = GridRequestHandler
                .findIntersectedGrid(parmID, timeRange);

        Map<TimeRange, List<DiscreteGridSlice>> adjacentMap = new HashMap<TimeRange, List<DiscreteGridSlice>>();
        // find grid slices for adjacent time ranges
        if (intersectingRecords.size() > 1) {
            List<TimeRange> adjacentTimeRanges = GridRequestHandler
                    .requestAdjacentTimeRanges(parmID, timeRange);
            for (TimeRange tr : adjacentTimeRanges) {
                GFERecord record = new GFERecord(parmID, tr);
                DiscreteGridSlice slice = (DiscreteGridSlice) GFEDataAccessUtil
                        .getSlice(record);
                adjacentMap.put(tr,
                        DiscreteGridSliceUtil.separate(slice, tr, null));
            }
        }

        SeparatedRecords separatedRecords = DiscreteGridSliceUtil.remove(
                phenSig, timeRange, gridParmInfo, intersectingRecords,
                adjacentMap);

        // remove grids that are not merged with any other grid
        for (TimeRange removeTR : separatedRecords.timeRangesToRemove) {
            GridRequestHandler.store(new ArrayList<GFERecord>(), gridParmInfo,
                    removeTR);
        }

        // store the new grids
        if (separatedRecords.newRecords != null
                && !separatedRecords.newRecords.isEmpty()) {
            TimeRange replacementTimeRange = GFERecordUtil
                    .getReplacementTimeRange(separatedRecords.newRecords);
            GridRequestHandler.store(separatedRecords.newRecords, gridParmInfo,
                    replacementTimeRange);
        }
    }
}

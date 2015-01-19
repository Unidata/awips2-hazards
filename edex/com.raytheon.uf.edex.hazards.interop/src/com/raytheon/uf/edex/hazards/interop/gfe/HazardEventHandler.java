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
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.util.StringUtils;

import com.raytheon.edex.site.SiteUtil;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardStatus;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.ProductClass;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardNotification;
import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.HazardEventManager;
import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.HazardEventManager.Mode;
import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardEventUtilities;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.interoperability.HazardInteroperabilityRecordManager;
import com.raytheon.uf.common.dataplugin.events.hazards.interoperability.HazardsInteroperabilityGFE;
import com.raytheon.uf.common.dataplugin.events.hazards.interoperability.IHazardsInteroperabilityRecord;
import com.raytheon.uf.common.dataplugin.gfe.dataaccess.GFEDataAccessUtil;
import com.raytheon.uf.common.dataplugin.gfe.db.objects.GFERecord;
import com.raytheon.uf.common.dataplugin.gfe.db.objects.GridLocation;
import com.raytheon.uf.common.dataplugin.gfe.db.objects.GridParmInfo;
import com.raytheon.uf.common.dataplugin.gfe.db.objects.ParmID;
import com.raytheon.uf.common.dataplugin.gfe.discrete.DiscreteKey;
import com.raytheon.uf.common.dataplugin.gfe.grid.Grid2DBit;
import com.raytheon.uf.common.dataplugin.gfe.reference.ReferenceData;
import com.raytheon.uf.common.dataplugin.gfe.reference.ReferenceData.CoordinateType;
import com.raytheon.uf.common.dataplugin.gfe.reference.ReferenceID;
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
import com.raytheon.uf.common.util.StringUtil;
import com.vividsolutions.jts.geom.Geometry;
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
 * Feb 18, 2014 2877       bkowal       Interoperability improvements. Prevent
 *                                      unnecessary iterations initiated by
 *                                      CAVE.
 * Feb 20, 2014 2999       bkowal       Additional Interoperability improvements.
 *                                      Handle the cases when multiple hazards
 *                                      are associated with a single gfe grid.
 * Mar 03, 2014 3034       bkowal       Use the gfe interoperability constant.
 *                                      Handle hazard history lists > 1 hazard correctly.
 * Mar 19, 2014 3278       bkowal       Remove hazards that are associated with GFE
 *                                      grids that are deleted.
 * Mar 20, 2014 3301       bkowal       Use the new and more efficient method to combine
 *                                      geometries recommended by the new GeoTools.
 * Mar 30, 2014 3323       bkowal       The mode is now used to retrieve the correct
 *                                      GridParmInfo instead of defaulting to Operational
 *                                      all the time.
 * Apr 08, 2014 3357       bkowal       Updated to use the new interoperability tables. Rewrote
 *                                      GFE geometry matching for interoperability.
 * Apr 24, 2014 3535       bkowal       Ignore update notifications for unrecognized grid types
 *                                      instead of logging a warning. This class will receive
 *                                      every grid update notification.
 * Dec 12, 2014 2826       dgilling     Change fields used by interoperability.
 * Jan 19, 2014 4849       rferrel      Log exceptions getting GFERecords to delete.
 * 
 * </pre>
 * 
 * @author jsanchez
 * @version 1.0
 */

public class HazardEventHandler {

    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(HazardEventHandler.class);

    private boolean initialized;

    private Map<String, GridParmInfo> operationalGridParmInfoMap;

    private Map<String, GridParmInfo> practiceGridParmInfoMap;

    private static HazardEventHandler instance = new HazardEventHandler();

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

    /**
     * Constructor.
     */
    private HazardEventHandler() {
        this.operationalGridParmInfoMap = new HashMap<>();
        this.practiceGridParmInfoMap = new HashMap<>();
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
        instance.initGridParmInfo(SiteUtil.getSite());
        return instance;
    }

    private void initGridParmInfo(String siteID) {
        if (this.operationalGridParmInfoMap.containsKey(siteID) == false) {
            try {
                GridParmInfo gridParmInfo = GridRequestHandler
                        .requestOperationalGridParmInfo(siteID);
                this.operationalGridParmInfoMap.put(siteID, gridParmInfo);
                statusHandler.info("HazardEventHandler initialized for "
                        + siteID + " (OPERATIONAL).");
            } catch (Exception e) {
                this.handleGridParmInfoException(siteID, e);
                return;
            }
        }
        if (this.practiceGridParmInfoMap.containsKey(siteID) == false) {
            try {
                GridParmInfo gridParmInfo = GridRequestHandler
                        .requestPracticeGridParmInfo(siteID);
                this.practiceGridParmInfoMap.put(siteID, gridParmInfo);
                statusHandler.info("HazardEventHandler initialized for "
                        + siteID + " (PRACTICE).");
            } catch (Exception e) {
                this.handleGridParmInfoException(siteID, e);
                return;
            }
        }
        this.initialized = true;
    }

    private GridParmInfo getGridParmInfo(String siteID, Mode mode) {
        switch (mode) {
        case OPERATIONAL:
            return this.operationalGridParmInfoMap.get(siteID);
        default:
            return this.practiceGridParmInfoMap.get(siteID);
        }
    }

    private void handleGridParmInfoException(String siteID, Throwable e) {
        this.initialized = false;
        statusHandler.error(
                "Unable to create hazard event handler. Please verify you have "
                        + siteID + " activated via GFE.", e);
    }

    /**
     * Handles notifications from camel and creates or deletes a grid
     * appropriately
     */
    public void handleNotification(byte[] bytes) throws Exception {
        /*
         * Were we successfully initialized? If not, we will not have a
         * GridParmInfo to use.
         */
        if (this.initialized == false) {
            statusHandler
                    .warn("Initialization failed ... unable to process the Hazard Notification!");
            return;
        }

        HazardNotification notification = SerializationUtil
                .transformFromThrift(HazardNotification.class, bytes);
        IHazardEvent hazardEvent = notification.getEvent();

        /*
         * Determine which parm should be used.
         */
        GridParmInfo gridParmInfo = this.getGridParmInfo(
                hazardEvent.getSiteID(), notification.getMode());

        switch (notification.getType()) {
        case STORE:
        case UPDATE:
            String phenSig = HazardEventUtilities.getHazardType(hazardEvent);
            boolean convert = GridValidator.needsGridConversion(phenSig);

            if (convert) {
                createGrid(hazardEvent, hazardEvent.getStartTime(),
                        gridParmInfo, notification.isPracticeMode());
            }
            break;
        case DELETE:
            deleteGrid(hazardEvent, gridParmInfo, notification.isPracticeMode());
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

    private HazardEventManager getHazardEventManager(String parmID) {
        Mode mode = this.determineHazardsMode(parmID);
        if (mode == null) {
            return null;
        }
        HazardEventManager hazardEventManager = new HazardEventManager(mode);

        return hazardEventManager;
    }

    private HazardsInteroperabilityGFE constructInteroperabilityRecord(
            String siteID, String phenomenon, String significance,
            Date startDate, Date endDate, String eventID, String parmID,
            Geometry geometry) {
        HazardsInteroperabilityGFE record = new HazardsInteroperabilityGFE();
        record.getKey().setSiteID(siteID);
        record.getKey().setPhen(phenomenon);
        record.getKey().setSig(significance);
        record.getKey().setStartDate(startDate);
        record.getKey().setEndDate(endDate);
        record.getKey().setHazardEventID(eventID);
        record.setParmID(parmID);
        record.setGeometry(geometry);

        return record;
    }

    private void handleHazardsGridUpdateNotification(
            GridUpdateNotification gridUpdateNotification) {
        if (HAZARD_PARM_NAME.equals(gridUpdateNotification.getParmId()
                .getParmName()) == false) {
            return;
        }

        final String parmID = gridUpdateNotification.getParmId().toString();
        final String siteID = gridUpdateNotification.getSiteID();
        /*
         * empty history indicates that the grid has been purged.
         */
        if (gridUpdateNotification.getHistories().isEmpty()) {
            /*
             * Determine if there are hazard events that also need to be
             * removed.
             */
            Date startDate = gridUpdateNotification.getReplacementTimeRange()
                    .getStart();
            Date endDate = gridUpdateNotification.getReplacementTimeRange()
                    .getEnd();

            this.handleHazardsGridPurgeNotification(siteID, startDate, endDate,
                    parmID);
            return;
        }

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

        @SuppressWarnings("unchecked")
        List<DiscreteGridSlice> gridSlices = (List<DiscreteGridSlice>) serverResponse
                .getPayload();
        if (gridSlices.isEmpty()) {
            /*
             * Nothing to process.
             */
            return;
        }

        HazardEventManager hazardEventManager = this
                .getHazardEventManager(parmID);
        if (hazardEventManager == null) {
            /*
             * Unrecognized Parm ID.
             */
            return;
        }

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
                String[] hazardParts = StringUtil.split(hazardType, '.');

                List<IHazardEvent> events = null;
                synchronized (this) {
                    events = GfeInteroperabilityUtil
                            .queryForInteroperabilityHazards(siteID,
                                    hazardParts[0], hazardParts[1], startDate,
                                    endDate, hazardEventManager);
                }

                GridLocation gridLocation = discreteGridSlice.getGridParmInfo()
                        .getGridLoc();

                /* Polygons */
                MultiPolygon hazardMultiPolygon = null;
                Geometry gfeMultiPolygon = null;
                Grid2DBit grid2DBit = this
                        .mergeGridDataStructures(gridsToProcessMap
                                .get(discreteKey));
                ReferenceData referenceData = new ReferenceData(gridLocation,
                        new ReferenceID("temp"), grid2DBit);
                hazardMultiPolygon = referenceData
                        .getPolygons(CoordinateType.LATLON);

                /*
                 * Track the hazards that may be updated and the hazards that
                 * will definitely be updated.
                 */
                List<IHazardEvent> updateCandidates = new ArrayList<>();
                List<IHazardEvent> hazardsToUpdate = new ArrayList<>();

                /* Track the hazards that will be created. */
                List<IHazardEvent> hazardsToCreate = new ArrayList<>();
                /*
                 * Track associated gfe interoperability records that may also
                 * be created.
                 */
                List<IHazardsInteroperabilityRecord> gfeInteroperabilityRecords = new ArrayList<>();

                /* Initialize the hazard state. */
                HazardStatus hazardState = HazardStatus.PENDING;

                if (events == null) {
                    // no hazard(s) to update; create a new hazard.

                    IHazardEvent hazardEvent = this.createNewHazard(
                            hazardEventManager, hazardState, startDate,
                            endDate, siteID, hazardType, hazardMultiPolygon,
                            this.determineHazardsMode(parmID) == Mode.PRACTICE);
                    if (hazardEvent == null) {
                        statusHandler.handle(Priority.PROBLEM,
                                "Hazard creation failed!");
                        return;
                    }

                    // initialize the associated gfe interoperability record.
                    HazardsInteroperabilityGFE record = this
                            .constructInteroperabilityRecord(
                                    hazardEvent.getSiteID(),
                                    hazardEvent.getPhenomenon(),
                                    hazardEvent.getSignificance(), startDate,
                                    endDate, hazardEvent.getEventID(), parmID,
                                    hazardMultiPolygon);

                    hazardsToCreate.add(hazardEvent);
                    gfeInteroperabilityRecords.add(record);
                } else {
                    Grid2DBit data = null;

                    /*
                     * Iterate through a full & complete list of the hazards
                     * that were found.
                     */
                    try {
                        for (IHazardEvent iterateHazardEvent : events) {
                            updateCandidates.add(iterateHazardEvent);
                            Geometry hazardGeometry = iterateHazardEvent
                                    .getGeometry();
                            if (data == null) {
                                data = GFERecordUtil
                                        .translateHazardPolygonGrid2Bit(
                                                gridLocation, hazardGeometry);
                            } else {
                                data = data.or(GFERecordUtil
                                        .translateHazardPolygonGrid2Bit(
                                                gridLocation, hazardGeometry));
                            }

                            ReferenceData gfeReferenceData = new ReferenceData(
                                    gridLocation, new ReferenceID("temp"), data);
                            gfeMultiPolygon = gfeReferenceData
                                    .getPolygons(CoordinateType.LATLON);
                        }
                    } catch (Exception e) {
                        statusHandler
                                .handle(Priority.PROBLEM,
                                        "Failed to convert the hazard polygon to a GFE polygon!",
                                        e);
                    }
                }

                // determine if the grid has been published.
                if (discreteGridSlice.getGridDataHistory()
                        .get(discreteGridSlice.getGridDataHistory().size() - 1)
                        .getPublishTime() != null) {
                    // set every hazard that will be updated in the pending
                    // state to proposed

                    /*
                     * update the state that is used for hazard creation just in
                     * case new hazards need to be created based on the current
                     * grid.
                     */
                    hazardState = HazardStatus.PROPOSED;

                    // for now this is the only update that will be made to
                    // a hazard based on requirements - so, a list will be
                    // sufficient to track the hazards that need to be
                    // updated.
                    for (IHazardEvent updateCandidateHazardEvent : updateCandidates) {
                        Date creationTime = new Date();

                        if (updateCandidateHazardEvent.getStatus() == HazardStatus.PENDING) {
                            updateCandidateHazardEvent
                                    .setCreationTime(creationTime);

                            updateCandidateHazardEvent.setHazardMode(this
                                    .determineProductClass(parmID));

                            hazardsToUpdate.add(updateCandidateHazardEvent);
                        }
                    }
                }

                /*
                 * Determine if there is a geometric region that is not
                 * associated with any of the existing hazards yet.
                 */
                if (gfeMultiPolygon != null) {
                    if (gfeMultiPolygon.isEmpty()) {
                        /*
                         * Unlikely scenario.
                         */
                        statusHandler
                                .warn("gfeMultiPolygon is unexpectedly empty!");
                        return;
                    }

                    Geometry differenceGeometry = hazardMultiPolygon
                            .difference(gfeMultiPolygon);

                    if (differenceGeometry.isEmpty() == false) {
                        /*
                         * Create a new hazard for this region. At this point,
                         * this is the only option because there is no easy way
                         * to determine which part of the grid a hazard is
                         * associated with. So, it will not be possible to
                         * determine if a hazard geometry does not completely
                         * match the grid region that it is associated with.
                         */
                        IHazardEvent hazardEvent = this
                                .createNewHazard(
                                        hazardEventManager,
                                        hazardState,
                                        startDate,
                                        endDate,
                                        siteID,
                                        hazardType,
                                        differenceGeometry,
                                        this.determineHazardsMode(parmID) == Mode.PRACTICE);
                        if (hazardEvent == null) {
                            statusHandler.handle(Priority.PROBLEM,
                                    "Hazard creation failed!");
                            return;
                        }

                        // initialize the associated gfe interoperability
                        // record.
                        HazardsInteroperabilityGFE record = this
                                .constructInteroperabilityRecord(
                                        hazardEvent.getSiteID(),
                                        hazardEvent.getPhenomenon(),
                                        hazardEvent.getSignificance(),
                                        startDate, endDate,
                                        hazardEvent.getEventID(), parmID,
                                        differenceGeometry);

                        hazardsToCreate.add(hazardEvent);
                        gfeInteroperabilityRecords.add(record);
                    }
                }

                if (hazardsToUpdate.isEmpty() == false) {
                    hazardEventManager.updateEvents(hazardsToUpdate);
                }
                if (hazardsToCreate.isEmpty() == false) {
                    hazardEventManager.storeEvents(hazardsToCreate);

                    synchronized (this) {
                        HazardInteroperabilityRecordManager
                                .storeRecords(gfeInteroperabilityRecords);
                    }
                }
            }
        }
    }

    private void handleHazardsGridPurgeNotification(String siteID,
            Date startDate, Date endDate, String parmID) {
        HazardEventManager hazardEventManager = this
                .getHazardEventManager(parmID);
        if (hazardEventManager == null) {
            /*
             * Unrecognized Parm ID.
             */
            return;
        }

        List<IHazardEvent> events = GfeInteroperabilityUtil
                .queryForInteroperabilityHazards(siteID, null, null, startDate,
                        endDate, hazardEventManager);

        if (events != null) {
            hazardEventManager.removeEvents(events);
        }
    }

    private IHazardEvent createNewHazard(HazardEventManager hazardEventManager,
            HazardStatus hazardState, Date startDate, Date endDate,
            String siteID, String hazardType, Geometry hazardMultiPolygon,
            boolean practice) {
        IHazardEvent hazardEvent = hazardEventManager.createEvent();

        try {
            hazardEvent.setEventID(HazardEventUtilities.generateEventID(siteID,
                    practice));
        } catch (Exception e) {
            statusHandler.handle(Priority.ERROR,
                    "Hazard ID generation failed.", e);
            return null;
        }

        // initially set the state to PENDING
        hazardEvent.setStatus(hazardState);

        // start time
        hazardEvent.setStartTime(startDate);

        // end time
        hazardEvent.setEndTime(endDate);

        // site id
        hazardEvent.setSiteID(siteID);

        // geometry
        hazardEvent.setGeometry(hazardMultiPolygon);

        // phenomenon & significance (possibly subtype)
        String[] hazardTypeComponents = StringUtils.split(hazardType, ".");
        if (hazardTypeComponents.length < 2) {
            // invalid hazard type; does not include at least both
            // a phenomenon and a significance.
            statusHandler.handle(Priority.ERROR,
                    "Invalid hazard type encountered: " + hazardType);
            return null;
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
        hazardEvent.addHazardAttribute(HazardConstants.GFE_INTEROPERABILITY,
                true);

        return hazardEvent;
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
    public void createGrid(IHazardEvent hazardEvent, Date currentDate,
            GridParmInfo gridParmInfo, boolean practice) throws Exception {

        List<GFERecord> newGFERecords = new ArrayList<GFERecord>();
        TimeRange timeRange = GFERecordUtil.createGridTimeRange(
                hazardEvent.getStartTime(), hazardEvent.getEndTime(),
                gridParmInfo.getTimeConstraints());

        /*
         * Determine if this hazard has already been processed for
         * interoperability.
         * 
         * Alternatively, an EDEX side originator would work significantly
         * better. If the originator was this class, the addition of the new
         * hazard event would be ignored.
         */
        IHazardsInteroperabilityRecord record = null;
        synchronized (this) {
            record = HazardInteroperabilityRecordManager.queryForRecordByPK(
                    hazardEvent.getSiteID(), hazardEvent.getPhenomenon(),
                    hazardEvent.getSignificance(), hazardEvent.getEventID(),
                    timeRange.getStart(), timeRange.getEnd());
        }
        HazardsInteroperabilityGFE existingRecord = null;
        if (record != null) {
            existingRecord = (HazardsInteroperabilityGFE) record;
            Geometry gfeGeometry = GFERecordUtil.translateHazardPolygonToGfe(
                    gridParmInfo.getGridLoc(), hazardEvent.getGeometry());

            /* update the geometry of the new record. */
            statusHandler
                    .info("Updating interoperability information associated with Hazard "
                            + hazardEvent.getEventID());
            existingRecord.setGeometry(gfeGeometry);
            synchronized (this) {
                HazardInteroperabilityRecordManager
                        .updateRecord(existingRecord);
            }
        } else {
            Geometry gfeGeometry = GFERecordUtil.translateHazardPolygonToGfe(
                    gridParmInfo.getGridLoc(), hazardEvent.getGeometry());
            HazardsInteroperabilityGFE newRecord = this
                    .constructInteroperabilityRecord(hazardEvent.getSiteID(),
                            hazardEvent.getPhenomenon(), hazardEvent
                                    .getSignificance(), timeRange.getStart(),
                            timeRange.getEnd(), hazardEvent.getEventID(),
                            gridParmInfo.getParmID().toString(), gfeGeometry);
            synchronized (this) {
                HazardInteroperabilityRecordManager.storeRecord(newRecord);
            }

            statusHandler
                    .info("No interoperability record was found for Hazard "
                            + hazardEvent.getEventID());
        }

        // find hazards that intersect with hazardEvent time range
        List<GFERecord> intersectingRecords = GridRequestHandler
                .findIntersectedGrid(gridParmInfo.getParmID(), timeRange);

        // create a new grid slice from the hazard event
        GFERecord convertedHazardEvent = GFERecordUtil.createGFERecord(
                hazardEvent, gridParmInfo);
        if (convertedHazardEvent == null) {
            /*
             * If the geometry is not closed, then a grid already exists and the
             * hazard was created based on a grid.
             */
            return;
        }

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
                replacementTimeRange, practice);
    }

    /**
     * Deletes the associated grid of the IHazardEvent object from the forecast
     * and official database.
     * 
     * @param hazardEvent
     */
    public void deleteGrid(IHazardEvent hazardEvent, GridParmInfo gridParmInfo,
            boolean practice) throws Exception {
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
                GFERecord gfeRecord = new GFERecord(parmID, tr);
                try {
                    DiscreteGridSlice slice = (DiscreteGridSlice) GFEDataAccessUtil
                            .getSlice(gfeRecord);
                    adjacentMap.put(tr,
                            DiscreteGridSliceUtil.separate(slice, tr, null));
                } catch (Exception ex) {
                    if (statusHandler.isPriorityEnabled(Priority.ERROR)) {
                        String message = String
                                .format("Unable to obtain Discrete Grid Slice for ParamID: %s, in the time range: %s",
                                        parmID.getParmId(), tr.toString());
                        statusHandler.error(message, ex);
                    }
                }
            }
        }

        SeparatedRecords separatedRecords = DiscreteGridSliceUtil.remove(
                phenSig, timeRange, gridParmInfo, intersectingRecords,
                adjacentMap);

        // remove grids that are not merged with any other grid
        for (TimeRange removeTR : separatedRecords.timeRangesToRemove) {
            GridRequestHandler.store(new ArrayList<GFERecord>(), gridParmInfo,
                    removeTR, practice);
        }

        // store the new grids
        if (separatedRecords.newRecords != null
                && !separatedRecords.newRecords.isEmpty()) {
            TimeRange replacementTimeRange = GFERecordUtil
                    .getReplacementTimeRange(separatedRecords.newRecords);
            GridRequestHandler.store(separatedRecords.newRecords, gridParmInfo,
                    replacementTimeRange, practice);
        }

        /*
         * Determine if there are gfe interoperability records that will also
         * need to be removed. Note: if grid removal fails, we will not reach
         * this point.
         */
        final String siteID = hazardEvent.getSiteID();
        final String eventID = hazardEvent.getEventID();

        List<IHazardsInteroperabilityRecord> records = new ArrayList<>();
        IHazardsInteroperabilityRecord record = HazardInteroperabilityRecordManager
                .queryForRecordByPK(siteID, hazardEvent.getPhenomenon(),
                        hazardEvent.getSignificance(), eventID,
                        timeRange.getStart(), timeRange.getEnd());
        if (record != null) {
            records.add(record);
        }
        /*
         * Determine if there are interoperability records that need to be
         * removed.
         */
        // get a list of etns associated with the hazard.
        List<String> etns = HazardEventUtilities.parseEtns(hazardEvent
                .getHazardAttribute(HazardConstants.ETNS).toString());
        for (String etn : etns) {
            record = HazardInteroperabilityRecordManager.queryForRecordByPK(
                    siteID, hazardEvent.getPhenomenon(),
                    hazardEvent.getSignificance(), eventID, etn);
            if (record != null) {
                records.add(record);
            }
        }

        if (records.isEmpty() == false) {
            HazardInteroperabilityRecordManager.removeRecords(records);
        }
    }
}

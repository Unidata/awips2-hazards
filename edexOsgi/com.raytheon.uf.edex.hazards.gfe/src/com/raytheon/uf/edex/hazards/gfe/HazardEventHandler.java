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
import java.util.LinkedList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.Iterator;

import org.geotools.geometry.jts.JTSFactoryFinder;
import org.opengis.referencing.operation.TransformException;
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
import com.raytheon.uf.common.dataplugin.gfe.db.objects.GridLocation;
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
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
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
                        gridParmInfo);
            }
            break;
        case DELETE:
            deleteGrid(hazardEvent, gridParmInfo);
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

    /*
     * This method is based on: http://docs.geotools.org/latest/userguide/library/jts/combine.html
     */
    private Geometry combineIntoOneGeometry(Geometry unionGeometry,
            Geometry geometry) {
        List<Geometry> geometryCollection = new ArrayList<>();
        geometryCollection.add(unionGeometry);
        geometryCollection.add(geometry);
        GeometryFactory factory = JTSFactoryFinder.getGeometryFactory(null);

        // note the following geometry collection may be invalid (say with
        // overlapping polygons)
        GeometryCollection newGeometryCollection = (GeometryCollection) factory
                .buildGeometry(geometryCollection);

        return newGeometryCollection.union();
    }    
    
    private HazardEventManager getHazardEventManager(String parmID) {
        Mode mode = this.determineHazardsMode(parmID);
        if (mode == null) {
            statusHandler
                    .warn("Failed to determine mode associated with parmID: "
                            + parmID);
            return null;
        }
        HazardEventManager hazardEventManager = new HazardEventManager(mode);
        // for now default to PRACTICE mode because everything else in
        // hazard services is.
        // TODO: remove the line below so that the event manager
        // will be based on the true mode.
        hazardEventManager = new HazardEventManager(Mode.PRACTICE);

        return hazardEventManager;
    }

    /*
     * Builds a list of events to examine while handling the HazardHistoryList.
     */
    private List<IHazardEvent> evaluateReturnedEvents(
            Map<String, HazardHistoryList> events) {
        List<IHazardEvent> hazardEventsToIterateOver = null;

        /*
         * determine how many hazards need to be reviewed.
         */
        int hazardsListCount = events.entrySet().size();
        if (hazardsListCount == 1) {
            hazardEventsToIterateOver = events.entrySet().iterator().next()
                    .getValue().getEvents();
        } else {
            hazardEventsToIterateOver = new LinkedList<>();
            for (String hazardEventID : events.keySet()) {
                hazardEventsToIterateOver.addAll(events.get(hazardEventID)
                        .getEvents());
            }
        }

        return hazardEventsToIterateOver;
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

                HazardQueryBuilder builder = new HazardQueryBuilder();
                builder.addKey(HazardConstants.SITE_ID, siteID);
                builder.addKey(HazardConstants.PHEN_SIG, hazardType);
                builder.addKey(HazardConstants.HAZARD_EVENT_START_TIME,
                        startDate);
                builder.addKey(HazardConstants.HAZARD_EVENT_END_TIME, endDate);

                Map<String, HazardHistoryList> events = hazardEventManager
                        .getEventsByFilter(builder.getQuery());

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

                /* Initialize the hazard state. */
                HazardState hazardState = HazardState.PENDING;

                if (events.isEmpty()) {
                    // no hazard(s) to update; create a new hazard.

                    IHazardEvent hazardEvent = this.createNewHazard(
                            hazardEventManager, hazardState, startDate,
                            endDate, siteID, hazardType, hazardMultiPolygon);
                    if (hazardEvent == null) {
                        statusHandler.handle(Priority.PROBLEM,
                                "Hazard creation failed!");
                        return;
                    }

                    hazardsToCreate.add(hazardEvent);
                } else {
                    Geometry hazardGeometry = null;

                    List<IHazardEvent> hazardEventsToIterateOver = this
                            .evaluateReturnedEvents(events);

                    /*
                     * Iterate through a full & complete list of the hazards
                     * that were found.
                     */
                    for (IHazardEvent iterateHazardEvent : hazardEventsToIterateOver) {
                        updateCandidates.add(iterateHazardEvent);
                        if (hazardGeometry == null) {
                            hazardGeometry = iterateHazardEvent.getGeometry();
                        } else {
                            hazardGeometry = this.combineIntoOneGeometry(
                                    hazardGeometry,
                                    iterateHazardEvent.getGeometry());
                        }
                    }

                    /*
                     * Interpret the hazard geometry the same way that GFE
                     * interprets it.
                     */
                    try {
                        gfeMultiPolygon = GFERecordUtil
                                .translateHazardPolygonToGfe(gridLocation,
                                        hazardGeometry);
                    } catch (TransformException e) {
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
                    hazardState = HazardState.PROPOSED;

                    // for now this is the only update that will be made to
                    // a hazard based on requirements - so, a list will be
                    // sufficient to track the hazards that need to be
                    // updated.
                    for (IHazardEvent updateCandidateHazardEvent : updateCandidates) {
                        Date issueTime = new Date();

                        if (updateCandidateHazardEvent.getState() == HazardState.PENDING) {
                            updateCandidateHazardEvent
                                    .setState(HazardState.PROPOSED);

                            /*
                             * presently we are setting the issue time and
                             * ProductClass based on an initial review of how
                             * hazards are stored in the database when they are
                             * created and proposed directly from hazard
                             * services.
                             */
                            updateCandidateHazardEvent
                                    .setState(HazardState.PROPOSED);

                            updateCandidateHazardEvent.setIssueTime(issueTime);

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
                    Geometry noHazardAssociation = hazardMultiPolygon
                            .difference(gfeMultiPolygon);
                    if (noHazardAssociation.isEmpty() == false) {
                        /*
                         * Create a new hazard for this region. At this point,
                         * this is the only option because there is no easy way
                         * to determine which part of the grid a hazard is
                         * associated with. So, it will not be possible to
                         * determine if a hazard geometry does not completely
                         * match the grid region that it is associated with.
                         */
                        IHazardEvent hazardEvent = this.createNewHazard(
                                hazardEventManager, hazardState, startDate,
                                endDate, siteID, hazardType,
                                noHazardAssociation);
                        if (hazardEvent == null) {
                            statusHandler.handle(Priority.PROBLEM,
                                    "Hazard creation failed!");
                            return;
                        }

                        hazardsToCreate.add(hazardEvent);
                    }
                }

                if (hazardsToUpdate.isEmpty() == false) {
                    hazardEventManager.updateEvents(hazardsToUpdate);
                }
                if (hazardsToCreate.isEmpty() == false) {
                    hazardEventManager.storeEvents(hazardsToCreate);
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

        /*
         * Retrieve any events within the time range.
         */
        HazardQueryBuilder builder = new HazardQueryBuilder();
        builder.addKey(HazardConstants.SITE_ID, siteID);
        builder.addKey(HazardConstants.HAZARD_EVENT_START_TIME, startDate);
        builder.addKey(HazardConstants.HAZARD_EVENT_END_TIME, endDate);

        Map<String, HazardHistoryList> events = hazardEventManager
                .getEventsByFilter(builder.getQuery());
        if (events.isEmpty()) {
            /*
             * No events to remove.
             */
            return;
        }

        List<IHazardEvent> hazardEventsToIterateOver = this
                .evaluateReturnedEvents(events);

        /*
         * Build the list of events to remove.
         */
        List<IHazardEvent> eventsToRemove = new ArrayList<>();
        for (IHazardEvent hazardEvent : hazardEventsToIterateOver) {
            if (hazardEvent.getHazardAttributes().containsKey(
                    HazardConstants.GFE_INTEROPERABILITY)) {
                /*
                 * The hazard was created in response to the creation of a GFE
                 * grid.
                 */
                eventsToRemove.add(hazardEvent);
                continue;
            }

            String phenSig = HazardEventUtilities.getHazardType(hazardEvent);
            /*
             * Determine if the event would be associated with a GFE grid.
             */
            boolean convert = GridValidator.needsGridConversion(phenSig);
            if (convert) {
                eventsToRemove.add(hazardEvent);
            }
        }

        if (eventsToRemove.isEmpty() == false) {
            hazardEventManager.removeEvents(eventsToRemove);
        }
    }

    private IHazardEvent createNewHazard(HazardEventManager hazardEventManager,
            HazardState hazardState, Date startDate, Date endDate,
            String siteID, String hazardType, Geometry hazardMultiPolygon) {
        IHazardEvent hazardEvent = hazardEventManager.createEvent();

        try {
            hazardEvent
                    .setEventID(HazardEventUtilities.generateEventID(siteID));
        } catch (Exception e) {
            statusHandler.handle(Priority.ERROR,
                    "Hazard ID generation failed.", e);
            return null;
        }

        // initially set the state to PENDING
        hazardEvent.setState(hazardState);

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
            GridParmInfo gridParmInfo) throws Exception {

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
    public void deleteGrid(IHazardEvent hazardEvent, GridParmInfo gridParmInfo)
            throws Exception {
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

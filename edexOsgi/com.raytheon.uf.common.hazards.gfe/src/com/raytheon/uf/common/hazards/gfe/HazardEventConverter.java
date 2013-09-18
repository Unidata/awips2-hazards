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
package com.raytheon.uf.common.hazards.gfe;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import jep.JepException;

import org.geotools.geometry.jts.JTS;
import org.opengis.metadata.spatial.PixelOrientation;
import org.opengis.referencing.operation.TransformException;

import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.dataplugin.gfe.GridDataHistory;
import com.raytheon.uf.common.dataplugin.gfe.GridDataHistory.OriginType;
import com.raytheon.uf.common.dataplugin.gfe.dataaccess.GFEDataAccessUtil;
import com.raytheon.uf.common.dataplugin.gfe.db.objects.GFERecord;
import com.raytheon.uf.common.dataplugin.gfe.db.objects.GridLocation;
import com.raytheon.uf.common.dataplugin.gfe.db.objects.GridParmInfo;
import com.raytheon.uf.common.dataplugin.gfe.db.objects.ParmID;
import com.raytheon.uf.common.dataplugin.gfe.discrete.DiscreteDefinition;
import com.raytheon.uf.common.dataplugin.gfe.discrete.DiscreteKey;
import com.raytheon.uf.common.dataplugin.gfe.grid.Grid2DBit;
import com.raytheon.uf.common.dataplugin.gfe.grid.Grid2DByte;
import com.raytheon.uf.common.dataplugin.gfe.python.GfePyIncludeUtil;
import com.raytheon.uf.common.dataplugin.gfe.request.AbstractGfeRequest;
import com.raytheon.uf.common.dataplugin.gfe.request.GetDiscreteDefinitionRequest;
import com.raytheon.uf.common.dataplugin.gfe.request.GetGridHistoryRequest;
import com.raytheon.uf.common.dataplugin.gfe.request.GetGridInventoryRequest;
import com.raytheon.uf.common.dataplugin.gfe.request.GetGridParmInfoRequest;
import com.raytheon.uf.common.dataplugin.gfe.request.LockChangeRequest;
import com.raytheon.uf.common.dataplugin.gfe.request.SaveGfeGridRequest;
import com.raytheon.uf.common.dataplugin.gfe.server.lock.LockTable.LockMode;
import com.raytheon.uf.common.dataplugin.gfe.server.message.ServerResponse;
import com.raytheon.uf.common.dataplugin.gfe.server.request.LockRequest;
import com.raytheon.uf.common.dataplugin.gfe.server.request.SaveGridRequest;
import com.raytheon.uf.common.dataplugin.gfe.slice.DiscreteGridSlice;
import com.raytheon.uf.common.dataplugin.gfe.util.GfeUtil;
import com.raytheon.uf.common.geospatial.MapUtil;
import com.raytheon.uf.common.localization.IPathManager;
import com.raytheon.uf.common.localization.PathManagerFactory;
import com.raytheon.uf.common.message.WsId;
import com.raytheon.uf.common.python.PyUtil;
import com.raytheon.uf.common.python.PythonScript;
import com.raytheon.uf.common.serialization.comm.RequestRouter;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.TimeRange;
import com.vividsolutions.jts.geom.MultiPolygon;

/**
 * Converts a HazardEvent object into a GFE Grid by creating a GFERecord and
 * storing it and an hdf5 file. The GFEReocrd will merge with other GFERecords
 * if their timeRanges intersects with the new grid.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 17, 2013 717        jsanchez     Initial creation
 * 
 * </pre>
 * 
 * @author jsanchez
 * @version 1.0
 */

public class HazardEventConverter {

    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(HazardEventConverter.class);

    private static final String MERGE_HAZARDS_FILE = "gfe" + File.separator
            + "userPython" + File.separator + "procedures" + File.separator
            + "MergeHazards.py";

    private static final List<String> preEvals = Arrays
            .asList(new String[] {
                    "import JUtil",
                    "def getHazardsConflictDict() :\n     return JUtil.pyValToJavaObj(HazardsConflictDict)" });

    private static final String PARAM_ID_FORMAT = "Hazards_SFC:%s_GRID__Fcst_00000000_0000";

    private GridParmInfo gridParmInfo;

    private String siteID;

    private DiscreteGridSliceUtil gridSliceUtil;

    private static Map<String, List<String>> hazardsConflictDict;

    /**
     * Cosntructor.
     * 
     * @param siteID
     * @throws Exception
     */
    public HazardEventConverter(String siteID) throws Exception {
        this.siteID = siteID;
        gridParmInfo = getGridParmInfo(siteID);
        gridSliceUtil = new DiscreteGridSliceUtil(gridParmInfo);
        setUpDiscreteDefinition(siteID);
    }

    /**
     * Converts the hazardEvent into a GFERecord and merges it appropriately
     * with existing GFERecords in the gfe table.
     * 
     * TODO Store issued products to the official database and possibly proposed
     * to the forecast database.
     * 
     * @param hazardEvent
     * @param currentDate
     * 
     * @return
     */
    public boolean storeHazardEventAsGrid(IHazardEvent hazardEvent,
            Date currentDate) {
        boolean success = false;
        try {
            List<GFERecord> newGFERecords = new ArrayList<GFERecord>();
            TimeRange timeRange = createGridTimeRange(hazardEvent);
            // find hazards that intersect with hazardEvent time range
            List<GFERecord> intersectingRecords = findIntersectedGrid(
                    gridParmInfo.getParmID(), timeRange);
            // create a new grid slice from the hazard event
            GFERecord convertedHazardEvent = createGFERecord(hazardEvent);

            // separate the records and identify which slices need to be merged.
            SeparatedRecords separatedRecords = gridSliceUtil
                    .separateGFERecords(convertedHazardEvent,
                            intersectingRecords, currentDate);
            // add GFERecords that only had their time ranges updated
            if (separatedRecords.newRecords != null
                    && !separatedRecords.newRecords.isEmpty()) {
                newGFERecords.addAll(separatedRecords.newRecords);
            }
            // perform merge
            if (separatedRecords.slicesToMerge != null
                    && !separatedRecords.slicesToMerge.isEmpty()) {
                newGFERecords.addAll(gridSliceUtil
                        .mergeGridSlices(separatedRecords.slicesToMerge));
            }
            // store the new grid(s).
            saveRecords(newGFERecords);
            success = true;
        } catch (TransformException e) {
            statusHandler.error("Error transforming geometry",
                    e.getLocalizedMessage());
        } catch (Exception e) {
            statusHandler.error("Unable to save records",
                    e.getLocalizedMessage());
        }
        return success;
    }

    /**
     * Sets up the discrete definition for the siteID
     * 
     * @param siteID
     * @throws Exception
     */
    private void setUpDiscreteDefinition(String siteID) throws Exception {
        GetDiscreteDefinitionRequest request = new GetDiscreteDefinitionRequest();
        request.setSiteID(siteID);
        ServerResponse<?> sr = makeRequest(siteID, request);
        DiscreteKey.setDiscreteDefinition(siteID,
                (DiscreteDefinition) sr.getPayload());
    }

    /**
     * Stores the GFERecord objects in the gfe tables.
     * 
     * @param records
     * @return
     * @throws Exception
     */
    private ServerResponse<?> saveRecords(List<GFERecord> records)
            throws Exception {
        // determine the replacement time range
        Date earliest = null;
        Date latest = null;
        for (GFERecord record : records) {
            TimeRange timeRange = record.getTimeRange();
            if (earliest == null || timeRange.getStart().before(earliest)) {
                earliest = timeRange.getStart();
            }

            if (latest == null || timeRange.getEnd().after(latest)) {
                latest = timeRange.getEnd();
            }
        }

        TimeRange replacementTimeRange = new TimeRange(earliest, latest);
        // Make a request to lock the time range of the grid. Otherwise, the
        // grid would be prevented from being saved if locked by someone else.
        LockRequest lreq = new LockRequest(gridParmInfo.getParmID(),
                replacementTimeRange, LockMode.LOCK);
        List<LockRequest> requests = new ArrayList<LockRequest>();
        requests.add(lreq);
        LockChangeRequest request = new LockChangeRequest();
        request.setRequests(requests);
        makeRequest(siteID, request);

        // Make a request to actually save the grid.
        List<SaveGridRequest> sgr = new ArrayList<SaveGridRequest>();
        sgr.add(new SaveGridRequest(gridParmInfo.getParmID(),
                replacementTimeRange, records, false));
        SaveGfeGridRequest saveGfeGridRequest = new SaveGfeGridRequest();
        saveGfeGridRequest.setSaveRequest(sgr);
        ServerResponse<?> response = makeRequest(siteID, saveGfeGridRequest);

        // Unlock
        requests.clear();
        requests.add(new LockRequest(gridParmInfo.getParmID(),
                replacementTimeRange, LockMode.UNLOCK));
        request.setRequests(requests);
        makeRequest(siteID, request);

        return response;
    }

    /**
     * Retrieves the GridParmInfo for the siteID.
     * 
     * @param siteID
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    private GridParmInfo getGridParmInfo(String siteID) throws Exception {
        GetGridParmInfoRequest gridParmInfoRequest = new GetGridParmInfoRequest();
        gridParmInfoRequest.setSiteID(siteID);
        gridParmInfoRequest.setParmIds(Arrays.asList(new ParmID[] { new ParmID(
                String.format(PARAM_ID_FORMAT, siteID)) }));

        ServerResponse<List<GridParmInfo>> response = (ServerResponse<List<GridParmInfo>>) RequestRouter
                .route(gridParmInfoRequest);

        GridParmInfo info = null;
        if (response != null && response.getPayload() != null) {
            List<GridParmInfo> payload = response.getPayload();
            info = payload.get(0);
        }

        return info;
    }

    /**
     * Retrieves the grid inventory for the site's grid parm info.
     * 
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    private static List<TimeRange> getGridInventory(ParmID parmID)
            throws Exception {
        GetGridInventoryRequest request = new GetGridInventoryRequest();
        request.setParmIds(Arrays.asList(new ParmID[] { parmID }));
        ServerResponse<Map<ParmID, List<TimeRange>>> response = (ServerResponse<Map<ParmID, List<TimeRange>>>) makeRequest(
                parmID.getDbId().getSiteId(), request);
        Map<ParmID, List<TimeRange>> map = response.getPayload();
        return map.get(parmID);
    }

    /**
     * Retrieves the grid history for the list of time ranges.
     * 
     * @param siteID
     *            the siteId of the histories needed
     * @param parmID
     *            the parmId of the histories needed
     * @param timeRanges
     *            the time range of histories needed
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    private static Map<TimeRange, List<GridDataHistory>> getGridHistory(
            String siteID, ParmID parmID, List<TimeRange> timeRanges)
            throws Exception {
        GetGridHistoryRequest request = new GetGridHistoryRequest();
        request.setParmID(parmID);
        request.setTimeRanges(timeRanges);
        ServerResponse<?> sr = makeRequest(siteID, request);
        Map<TimeRange, List<GridDataHistory>> histories = (Map<TimeRange, List<GridDataHistory>>) sr
                .getPayload();
        return histories;
    }

    private static ServerResponse<?> makeRequest(String siteID,
            AbstractGfeRequest request) throws Exception {
        request.setSiteID(siteID);
        request.setWorkstationID(new WsId(null, null, "CAVE"));
        return (ServerResponse<?>) RequestRouter.route(request);
    }

    /**
     * Finds hazard GFERecords that intersect with the timeRange.
     * 
     * @param timeRange
     * @return
     */
    private static List<GFERecord> findIntersectedGrid(ParmID parmID,
            TimeRange timeRange) throws Exception {
        List<GFERecord> records = new ArrayList<GFERecord>();
        List<TimeRange> inventoryTimeRanges = getGridInventory(parmID);
        List<TimeRange> intersectingTimeRanges = new ArrayList<TimeRange>();
        for (TimeRange inventoryTimeRange : inventoryTimeRanges) {
            if (timeRange.overlaps(inventoryTimeRange)) {
                intersectingTimeRanges.add(inventoryTimeRange);
            }
        }
        Map<TimeRange, List<GridDataHistory>> histories = getGridHistory(parmID
                .getDbId().getSiteId(), parmID, intersectingTimeRanges);
        for (TimeRange tr : intersectingTimeRanges) {
            GFERecord record = new GFERecord(parmID, tr);
            record.setGridHistory(histories.get(tr));
            record.setMessageData(GFEDataAccessUtil.getSlice(record));
            records.add(record);
        }

        return records;
    }

    /**
     * Checks to see if hazardEvent conflicts with existing discrete grid
     * slices.
     * 
     * @param hazardEvent
     * @return
     * @throws Exception
     * @throws JepException
     */
    @SuppressWarnings("unchecked")
    public static boolean hasConflicts(IHazardEvent hazardEvent) {
        try {
            // get HazardsConflictDict from MergeHazards.py
            if (hazardsConflictDict == null) {
                IPathManager pm = PathManagerFactory.getPathManager();
                File scriptFile = pm.getStaticFile(MERGE_HAZARDS_FILE);
                String python = GfePyIncludeUtil.getCommonPythonIncludePath();
                String utilities = GfePyIncludeUtil.getUtilitiesIncludePath();
                String gfe = GfePyIncludeUtil.getCommonGfeIncludePath();
                String vtec = GfePyIncludeUtil.getVtecIncludePath();

                PythonScript script = new PythonScript(
                        scriptFile.getPath(),
                        PyUtil.buildJepIncludePath(python, utilities, gfe, vtec),
                        HazardEventConverter.class.getClassLoader(), preEvals);
                hazardsConflictDict = (Map<String, List<String>>) script
                        .execute("getHazardsConflictDict", null);
            }

            String phenSig = hazardEvent.getPhenomenon() + "."
                    + hazardEvent.getSignificance();
            TimeRange timeRange = createGridTimeRange(hazardEvent);
            ParmID parmID = new ParmID(String.format(PARAM_ID_FORMAT,
                    hazardEvent.getSiteID()));
            List<GFERecord> potentialRecords = findIntersectedGrid(parmID,
                    timeRange);
            // test if hazardEvent will conflict with existing grids
            if (hazardsConflictDict != null
                    && hazardsConflictDict.get(phenSig) != null) {
                List<String> hazardsConflictList = hazardsConflictDict
                        .get(phenSig);
                for (GFERecord record : potentialRecords) {
                    DiscreteGridSlice gridSlice = (DiscreteGridSlice) record
                            .getMessageData();
                    for (DiscreteKey discreteKey : gridSlice.getKey()) {
                        for (String key : discreteKey.getSubKeys()) {
                            if (hazardsConflictList.contains(key)) {
                                return true;
                            }
                        }
                    }
                }
            }

        } catch (JepException e) {
            statusHandler
                    .error("Error trying to retrieve the HazardsConflictDict from MergeHazards.py",
                            e);
        } catch (Exception e) {
            statusHandler.error(
                    "Error trying to retrieve intersecting gfe records", e);
        }
        return false;
    }

    /**
     * Determines if the HazardEvent should be saved as a grid or if it should
     * be ignored.
     * 
     * @param hazardEvent
     * @return
     */
    public boolean needsConversion(IHazardEvent hazardEvent) {
        // TODO Need to check other phensigs that don't need a
        // conversion
        List<String> phensToIgnore = Arrays.asList(new String[] { "TO.W",
                "SV.W", "EW.W", "MA.W", "FF.W", "FA.W", "FA.Y", "FL.A" });
        String phenSig = hazardEvent.getPhenomenon() + "."
                + hazardEvent.getSignificance();
        return !phensToIgnore.contains(phenSig);
    }

    /**
     * Creates a time range from the start and end time of the event. To fit the
     * time constraints of a GFE grid, the start time is rounded down to the
     * closest hour while the end time is rounded up to the closest hour.
     * 
     * @param event
     * @return
     */
    private static TimeRange createGridTimeRange(IHazardEvent event) {
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.setTime(event.getStartTime());
        startCalendar.set(Calendar.MINUTE, 0);
        startCalendar.set(Calendar.SECOND, 0);

        Calendar endCalendar = Calendar.getInstance();
        endCalendar.setTime(event.getEndTime());
        endCalendar.set(Calendar.MINUTE, 0);
        endCalendar.set(Calendar.SECOND, 0);
        endCalendar.add(Calendar.HOUR, 1);

        return new TimeRange(startCalendar, endCalendar);
    }

    /**
     * Converts the hazardEvent to a GFERecord.
     * 
     * @param hazardEvent
     * @return
     * @throws TransformException
     */
    private GFERecord createGFERecord(IHazardEvent hazardEvent)
            throws TransformException {
        TimeRange timeRange = createGridTimeRange(hazardEvent);
        GFERecord record = new GFERecord(gridParmInfo.getParmID(), timeRange);
        ParmID parmID = gridParmInfo.getParmID();

        // create discrete keys
        String phensig = hazardEvent.getPhenomenon() + "."
                + hazardEvent.getSignificance();
        DiscreteKey[] discretekeys = gridSliceUtil.createSimpleDiscreteKeys(
                parmID, phensig);

        // create Grid History
        GridDataHistory gdh = new GridDataHistory(OriginType.CALCULATED,
                parmID, timeRange);
        gdh.setUpdateTime(new Date(System.currentTimeMillis()));
        GridDataHistory[] gdha = new GridDataHistory[] { gdh };
        record.setGridHistory(gdha);

        // create the Grid2DByte
        GridLocation gridLocation = gridParmInfo.getGridLoc();
        MultiPolygon polygon = GfeUtil.createPolygon(hazardEvent.getGeometry()
                .getCoordinates());
        polygon = (MultiPolygon) JTS.transform(polygon, MapUtil
                .getTransformFromLatLon(PixelOrientation.CENTER, gridLocation));
        Grid2DBit grid2DBit = GfeUtil.filledBitArray(polygon, gridLocation);
        Grid2DByte grid2DByte = new Grid2DByte(grid2DBit.getXdim(),
                grid2DBit.getYdim(), grid2DBit.getBuffer());

        // set discrete grid slice in message data
        DiscreteGridSlice gridSlice = new DiscreteGridSlice(timeRange,
                gridParmInfo, gdha, grid2DByte, discretekeys);
        record.setMessageData(gridSlice);

        return record;
    }
}

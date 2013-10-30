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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.raytheon.edex.site.SiteUtil;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardNotification;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.dataplugin.gfe.dataaccess.GFEDataAccessUtil;
import com.raytheon.uf.common.dataplugin.gfe.db.objects.GFERecord;
import com.raytheon.uf.common.dataplugin.gfe.db.objects.GridParmInfo;
import com.raytheon.uf.common.dataplugin.gfe.db.objects.ParmID;
import com.raytheon.uf.common.dataplugin.gfe.slice.DiscreteGridSlice;
import com.raytheon.uf.common.serialization.SerializationUtil;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.TimeRange;

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

    /**
     * Constructor.
     */
    private HazardEventHandler() {
        gridParmInfoMap = new HashMap<String, GridParmInfo>();
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
            if (convert) {
                createGrid(hazardEvent, hazardEvent.getStartTime());
            }
            break;
        case DELETE:
            deleteGrid(hazardEvent);
            break;
        }
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
            GridRequestHandler.store(null, gridParmInfo, removeTR);
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

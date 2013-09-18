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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.raytheon.uf.common.dataplugin.gfe.GridDataHistory;
import com.raytheon.uf.common.dataplugin.gfe.GridDataHistory.OriginType;
import com.raytheon.uf.common.dataplugin.gfe.db.objects.GFERecord;
import com.raytheon.uf.common.dataplugin.gfe.db.objects.GridParmInfo;
import com.raytheon.uf.common.dataplugin.gfe.db.objects.ParmID;
import com.raytheon.uf.common.dataplugin.gfe.discrete.DiscreteKey;
import com.raytheon.uf.common.dataplugin.gfe.grid.Grid2DByte;
import com.raytheon.uf.common.dataplugin.gfe.slice.DiscreteGridSlice;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.TimeRange;

/**
 * Supports the HazardEventConverter class by helping separating and merging
 * discrete grid slices.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 29, 2013 717        jsanchez     Initial creation
 * 
 * </pre>
 * 
 * @author jsanchez
 * @version 1.0
 */

public class DiscreteGridSliceUtil {

    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(DiscreteGridSliceUtil.class);

    public static final String DK_NONE = "<None>";

    private String siteID;

    private GridParmInfo gridParmInfo;

    /**
     * Constructor.
     * 
     * @param gridParmInfo
     */
    public DiscreteGridSliceUtil(GridParmInfo gridParmInfo) {
        this.gridParmInfo = gridParmInfo;
        this.siteID = gridParmInfo.getParmID().getDbId().getSiteId();
    }

    /**
     * Separates, if any, already merged discrete grid slices into individual
     * grid slices with only one discrete key.
     * 
     * @param gridSlice
     * @param timeRange
     * @return
     */
    private List<DiscreteGridSlice> separate(DiscreteGridSlice gridSlice,
            TimeRange timeRange, Date currentDate) {
        List<DiscreteGridSlice> gridSlices = new ArrayList<DiscreteGridSlice>();

        Grid2DByte byteGrid = gridSlice.getDiscreteGrid();
        DiscreteKey[] splitHazKeys = gridSlice.getKey();
        Set<String> uniqueKeys = new TreeSet<String>();
        for (DiscreteKey discreteKey : splitHazKeys) {
            uniqueKeys.addAll(discreteKey.getSubKeys());
        }

        for (String uKey : uniqueKeys) {
            if (uKey.equals(DK_NONE)) {
                continue;
            }
            String subKey = uKey;

            byte[] bytes = byteGrid.getBytes();
            byte[] mask = new byte[bytes.length];
            // sets the index that has the subKey
            for (int hazIndex = 0; hazIndex < splitHazKeys.length; hazIndex++) {
                if (splitHazKeys[hazIndex].getSubKeys().contains(subKey)) {
                    for (int i = 0; i < bytes.length; i++) {
                        if (bytes[i] == hazIndex) {
                            mask[i] = (byte) 1;
                        }
                    }
                }
            }

            // make the grid
            Grid2DByte grid2DByte = new Grid2DByte(byteGrid.getXdim(),
                    byteGrid.getYdim(), mask);
            ParmID parmID = gridParmInfo.getParmID();
            DiscreteKey[] discretekeys = createSimpleDiscreteKeys(parmID,
                    subKey);

            // create Grid History
            GridDataHistory gdh = new GridDataHistory(OriginType.CALCULATED,
                    parmID, timeRange);
            gdh.setUpdateTime(currentDate);
            GridDataHistory[] gdha = new GridDataHistory[] { gdh };

            DiscreteGridSlice createdGrid = new DiscreteGridSlice(timeRange,
                    gridParmInfo, gdha, grid2DByte, discretekeys);
            gridSlices.add(createdGrid);
        }

        return gridSlices;
    }

    /**
     * Creates simple discrete keys for only one phensig.
     * 
     * @param parmID
     * @param phensig
     * @return
     */
    public DiscreteKey[] createSimpleDiscreteKeys(ParmID parmID, String phensig) {
        DiscreteKey noneKey = new DiscreteKey(siteID, DK_NONE, parmID);
        DiscreteKey discreteKey = new DiscreteKey(siteID, phensig, parmID);
        Set<DiscreteKey> discretekeys = new TreeSet<DiscreteKey>(
                Arrays.asList(new DiscreteKey[] { noneKey, discreteKey }));
        return discretekeys.toArray(new DiscreteKey[discretekeys.size()]);
    }

    /**
     * Creates a SeparatedRecords object.
     * 
     * @param newRecord
     *            - the GFERecord created from an IHazardEvent
     * @param records
     *            - the list of GFERecords that have a time range intersecting
     *            the time range of the newRecord.
     * @return
     */
    public SeparatedRecords separateGFERecords(GFERecord newRecord,
            List<GFERecord> records, Date currentDate) {
        SeparatedRecords separateRecords = new SeparatedRecords();
        List<GFERecord> newRecords = new ArrayList<GFERecord>();
        if (records.isEmpty()) {
            // simple case
            // no records that intersect with the time range of the newRecord
            newRecords.add(newRecord);
        } else {
            DiscreteGridSlice newGridSlice = (DiscreteGridSlice) newRecord
                    .getMessageData();
            TimeRange recordTimeRange = newRecord.getTimeRange();
            Map<TimeRange, List<DiscreteGridSlice>> slicesToMerge = new HashMap<TimeRange, List<DiscreteGridSlice>>();

            for (int i = 0; i < records.size(); i++) {
                GFERecord record = records.get(i);
                TimeRange timeRange = record.getTimeRange();
                if (timeRange.contains(recordTimeRange)) {
                    TimeRange intersection = timeRange
                            .intersection(recordTimeRange);
                    List<DiscreteGridSlice> list = separate(
                            (DiscreteGridSlice) record.getMessageData(),
                            timeRange, currentDate);
                    list.add(newGridSlice);
                    slicesToMerge.put(intersection, list);

                    if (timeRange.getStart().before(intersection.getStart())) {
                        GFERecord rec = adjustTimeRange(record, new TimeRange(
                                timeRange.getStart(), intersection.getStart()));
                        newRecords.add(rec);
                    }

                    if (timeRange.getEnd().after(intersection.getEnd())) {
                        GFERecord rec = adjustTimeRange(record, new TimeRange(
                                intersection.getEnd(), timeRange.getEnd()));
                        newRecords.add(rec);
                    }

                    break;
                } else if (recordTimeRange.contains(timeRange)) {
                    TimeRange intersection = timeRange.intersection(timeRange);
                    List<DiscreteGridSlice> list = separate(
                            (DiscreteGridSlice) record.getMessageData(),
                            timeRange, currentDate);
                    list.add(newGridSlice);
                    slicesToMerge.put(intersection, list);

                    if (recordTimeRange.getStart().before(timeRange.getStart())) {
                        GFERecord rec = adjustTimeRange(newRecord,
                                new TimeRange(recordTimeRange.getStart(),
                                        timeRange.getStart()));
                        newRecords.add(rec);
                    }

                    recordTimeRange = new TimeRange(intersection.getEnd(),
                            recordTimeRange.getEnd());

                    if (i == records.size() - 1) {
                        GFERecord rec = adjustTimeRange(newRecord,
                                recordTimeRange);
                        newRecords.add(rec);
                    }
                } else if (timeRange.getStart().before(
                        recordTimeRange.getStart())) {
                    TimeRange intersection = timeRange
                            .intersection(recordTimeRange);
                    List<DiscreteGridSlice> list = separate(
                            (DiscreteGridSlice) record.getMessageData(),
                            timeRange, currentDate);
                    list.add(newGridSlice);
                    slicesToMerge.put(intersection, list);

                    GFERecord rec0 = adjustTimeRange(record, new TimeRange(
                            timeRange.getStart(), intersection.getStart()));
                    newRecords.add(rec0);

                    recordTimeRange = new TimeRange(intersection.getEnd(),
                            recordTimeRange.getEnd());

                    if (i == records.size() - 1) {
                        GFERecord rec = adjustTimeRange(newRecord,
                                recordTimeRange);
                        newRecords.add(rec);
                    }
                } else if (timeRange.getEnd().after(recordTimeRange.getEnd())) {
                    TimeRange intersection = timeRange
                            .intersection(recordTimeRange);
                    List<DiscreteGridSlice> list = separate(
                            (DiscreteGridSlice) record.getMessageData(),
                            timeRange, currentDate);
                    list.add(newGridSlice);
                    slicesToMerge.put(intersection, list);

                    if (recordTimeRange.getStart().before(timeRange.getStart())) {
                        GFERecord rec = adjustTimeRange(newRecord,
                                new TimeRange(recordTimeRange.getStart(),
                                        intersection.getStart()));
                        newRecords.add(rec);
                    }

                    GFERecord rec = adjustTimeRange(record, new TimeRange(
                            intersection.getEnd(), timeRange.getEnd()));
                    newRecords.add(rec);
                }
            }

            separateRecords.slicesToMerge = slicesToMerge;
        }

        separateRecords.newRecords = newRecords;
        return separateRecords;
    }

    /**
     * Clones the record but sets the time range to the new value.
     * 
     * @param record
     * @param timeRange
     * @return
     */
    private GFERecord adjustTimeRange(GFERecord record, TimeRange timeRange) {
        GFERecord adjustedRecord = new GFERecord(record.getParmId(), timeRange);
        adjustedRecord.setGridHistory(record.getGridHistory());
        adjustedRecord.setMessageData(record.getMessageData());
        return adjustedRecord;
    }

    /**
     * Merges each list of discrete grid slices into one GFERecord.
     * 
     * @param timeRangeMap
     * @return
     */
    public List<GFERecord> mergeGridSlices(
            Map<TimeRange, List<DiscreteGridSlice>> timeRangeMap) {
        List<GFERecord> newRecords = new ArrayList<GFERecord>(timeRangeMap
                .keySet().size());

        for (TimeRange timeRange : timeRangeMap.keySet()) {
            List<DiscreteGridSlice> discreteGridSlices = timeRangeMap
                    .get(timeRange);
            // perform merge
            GFERecord record = new GFERecord(gridParmInfo.getParmID(),
                    timeRange);

            Grid2DByte grid2DByte = null;
            List<String> uniqueKeys = new ArrayList<String>();
            uniqueKeys.add(DK_NONE);

            for (DiscreteGridSlice discreteGridSlice : discreteGridSlices) {
                // the grids should have been separated so there should only be
                // 1 valid key.
                DiscreteKey discreteKey = discreteGridSlice.getKey()[1];
                String addHaz = discreteKey.getSubKeys().get(0);
                Grid2DByte byteGrid = discreteGridSlice.getDiscreteGrid();

                String newKey = null;
                for (String uKey : uniqueKeys) {
                    // Figure out what the new key is
                    newKey = makeNewKey(uKey, addHaz);

                    if (grid2DByte == null) {
                        grid2DByte = byteGrid;
                    } else {
                        // Find the index number for the old key
                        int oldIndex = uniqueKeys.indexOf(uKey);

                        // Find the index number for the new key (newKey is
                        // added if not in hazKey)
                        int newIndex = uniqueKeys.size();

                        // calculate the mask - intersection of mask and
                        // oldIndex values
                        // editMask = (byteGrid==oldIndex) & mask
                        int rows = grid2DByte.getYdim();
                        int cols = grid2DByte.getXdim();
                        for (int col = 0; col < cols; col++) {
                            for (int row = 0; row < rows; row++) {
                                if (byteGrid.get(col, row) == 1
                                        && grid2DByte.get(col, row) == oldIndex) {
                                    // poke in the new values
                                    grid2DByte.set(col, row, newIndex);
                                }
                            }
                        }
                    }
                }

                if (newKey != null) {
                    uniqueKeys.add(newKey);
                }
            }

            Set<DiscreteKey> discretekeys = new TreeSet<DiscreteKey>();
            for (String uniqueKey : uniqueKeys) {
                discretekeys.add(new DiscreteKey(siteID, uniqueKey,
                        gridParmInfo.getParmID()));
            }

            // create Grid History
            GridDataHistory gdh = new GridDataHistory(OriginType.CALCULATED,
                    gridParmInfo.getParmID(), timeRange);
            gdh.setUpdateTime(new Date(System.currentTimeMillis()));
            GridDataHistory[] gdha = new GridDataHistory[] { gdh };
            record.setGridHistory(gdha);

            // set discrete grid slice in message data
            DiscreteGridSlice gridSlice = new DiscreteGridSlice(timeRange,
                    gridParmInfo, gdha, grid2DByte,
                    discretekeys.toArray(new DiscreteKey[discretekeys.size()]));

            String error = gridSlice.isValid();
            if (error != null) {
                statusHandler.error(error);
            }
            record.setMessageData(gridSlice);

            newRecords.add(record);
        }

        return newRecords;
    }

    /*
     * Ported from HazardUtils._makeNewKey
     */
    private String makeNewKey(String oldKey, String phenSig) {
        // check for the dumb cases
        if (oldKey.equalsIgnoreCase(DK_NONE)
                || oldKey.equalsIgnoreCase(phenSig)) {
            return phenSig;
        }
        // split up the key, add the hazard, sort, and reassemble
        List<String> parts = new ArrayList<String>(Arrays.asList(oldKey
                .split("^")));
        parts.add(phenSig);
        // makes sure the same set of subKeys look the same
        Collections.sort(parts);

        // assemble the new key
        String newKey = null;
        for (String p : parts) {
            if (newKey == null) {
                newKey = p;
            } else {
                newKey = combinedKey(newKey, p);
            }
        }
        // just in case
        if (newKey == null) {
            newKey = DK_NONE;
        }

        return newKey;
    }

    /*
     * Ported from HazardUtils._combinedKey
     */
    private String combinedKey(String subKeys, String newKey) {
        List<String> subKeyList = Arrays.asList(subKeys.split("^"));

        // check for same keys
        if (subKeyList.contains(newKey)) {
            return subKeys;
        }

        String defaultCombo = subKeys + "^" + newKey;

        // check for non-VTEC key
        if (newKey.indexOf(".") == -1) {
            return defaultCombo;
        }

        // more exceptions - these phens are above the law
        List<String> exceptions = Arrays
                .asList(new String[] { "TO", "SV", "FF" });
        List<String> sigList = Arrays.asList(new String[] { "W", "Y", "A" });
        if (exceptions.contains(newKey.substring(0, 2))) {
            return defaultCombo;
        }

        subKeyList = Arrays.asList(subKeys.split("^"));
        for (String sk : subKeyList) {
            if (sk.substring(0, 2).equalsIgnoreCase(newKey.substring(0, 2))) {
                String subSig = sk.substring(3);
                String newSig = newKey.substring(3);
                if (subSig.equalsIgnoreCase(newSig)) {
                    return subKeys;
                }

                if (!sigList.contains(subSig) || !sigList.contains(newSig)) {
                    continue;
                }

                if (sigList.indexOf(subSig) > sigList.indexOf(newSig)) {
                    subKeys = subKeys.replace(sk, newKey);
                }

                return subKeys;
            }
        }
        return defaultCombo;
    }
}

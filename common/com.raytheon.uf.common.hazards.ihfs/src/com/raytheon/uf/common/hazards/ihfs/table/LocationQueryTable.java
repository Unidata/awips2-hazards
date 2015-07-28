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
 */
package com.raytheon.uf.common.hazards.ihfs.table;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.raytheon.uf.common.hazards.ihfs.TableColumnData;
import com.raytheon.uf.common.hazards.ihfs.data.AbstractTableData;
import com.raytheon.uf.common.hazards.ihfs.data.LocationTableData;

/**
 * This singleton class describes the data query model of the ihfs.LOCATION
 * table.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 28, 2015 8839       Chris.Cody  Initial Creation
 * 
 * </pre>
 * 
 * @author Chris.Cody
 * @version 1.0
 */
public class LocationQueryTable extends AbstractQueryTable {

    public static final String LocationTableName = "LOCATION";

    private static LocationQueryTable locationQueryTable = null;

    // This declaration makes columnNameList immutable
    private static final List<String> LocationColumnNameList = Arrays.asList(
            "LID", "COUNTY", "COE", "CPM", "DETAIL", "ELEV", "HDATUM", "HSA",
            "HU", "LAT", "LON", "LREMARK", "LREVISE", "NAME", "NETWORK", "RB",
            "RFC", "SBD", "SN", "STATE", "WARO", "WFO", "WSFO", "TYPE", "DES",
            "DET", "POST", "SNTYPE", "TZONE");

    private LocationQueryTable() {
        this(LocationTableName, LocationTableData.class);
    }

    private LocationQueryTable(String tableName,
            Class<? extends AbstractTableData> tableDataClass) {
        super(LocationTableName, LocationColumnNameList,
                LocationTableData.class);

        tableColumnDataMap = new HashMap<>(LocationColumnNameList.size());
        tableColumnDataMap.put("LID", new TableColumnData(tableName, "LID",
                TableColumnData.STRING_TYPE, 8, 0, true));
        tableColumnDataMap.put("COUNTY", new TableColumnData(tableName,
                "COUNTY", TableColumnData.STRING_TYPE, 20, 1, true));
        tableColumnDataMap.put("COE", new TableColumnData(tableName, "COE",
                TableColumnData.STRING_TYPE, 3, 2, false));
        tableColumnDataMap.put("CPM", new TableColumnData(tableName, "CPM",
                TableColumnData.STRING_TYPE, 3, 3, false));
        tableColumnDataMap.put("DETAIL", new TableColumnData(tableName,
                "DETAIL", TableColumnData.STRING_TYPE, 10, 4, false));
        tableColumnDataMap.put("ELEV", new TableColumnData(tableName, "ELEV",
                TableColumnData.DOUBLE_TYPE, TableColumnData.DOUBLE_LEN, 5,
                false));
        tableColumnDataMap.put("HDATUM", new TableColumnData(tableName,
                "HDATUM", TableColumnData.STRING_TYPE, 9, 6, false));
        tableColumnDataMap.put("HSA", new TableColumnData(tableName, "HSA",
                TableColumnData.STRING_TYPE, 3, 7, true));
        tableColumnDataMap.put("HU", new TableColumnData(tableName, "HU",
                TableColumnData.STRING_TYPE, 8, 8, false));
        tableColumnDataMap.put("LAT", new TableColumnData(tableName, "LAT",
                TableColumnData.DOUBLE_TYPE, TableColumnData.DOUBLE_LEN, 9,
                false));
        tableColumnDataMap.put("LON", new TableColumnData(tableName, "LON",
                TableColumnData.DOUBLE_TYPE, TableColumnData.DOUBLE_LEN, 10,
                false));
        tableColumnDataMap.put("LREMARK", new TableColumnData(tableName,
                "LREMARK", TableColumnData.STRING_TYPE, 255, 11, false));
        tableColumnDataMap.put("LREVISE", new TableColumnData(tableName,
                "LREVISE", TableColumnData.DATE_TYPE, TableColumnData.DATE_LEN,
                12, false));
        tableColumnDataMap.put("NAME", new TableColumnData(tableName, "NAME",
                TableColumnData.STRING_TYPE, 50, 13, false));
        tableColumnDataMap.put("NETWORK", new TableColumnData(tableName,
                "NETWORK", TableColumnData.STRING_TYPE, 3, 14, false));
        tableColumnDataMap.put("RB", new TableColumnData(tableName, "RB",
                TableColumnData.STRING_TYPE, 30, 15, false));
        tableColumnDataMap.put("RFC", new TableColumnData(tableName, "RFC",
                TableColumnData.STRING_TYPE, 5, 16, false));
        tableColumnDataMap
                .put("SBD", new TableColumnData(tableName, "SBD",
                        TableColumnData.DATE_TYPE, TableColumnData.DATE_LEN,
                        17, false));
        tableColumnDataMap.put("SN", new TableColumnData(tableName, "SN",
                TableColumnData.STRING_TYPE, 10, 18, false));
        tableColumnDataMap.put("STATE", new TableColumnData(tableName, "STATE",
                TableColumnData.STRING_TYPE, 2, 19, false));
        tableColumnDataMap.put("WARO", new TableColumnData(tableName, "WARO",
                TableColumnData.STRING_TYPE, 3, 20, false));
        tableColumnDataMap.put("WFO", new TableColumnData(tableName, "WFO",
                TableColumnData.STRING_TYPE, 3, 21, true));
        tableColumnDataMap.put("WSFO", new TableColumnData(tableName, "WSFO",
                TableColumnData.STRING_TYPE, 3, 22, false));
        tableColumnDataMap.put("TYPE", new TableColumnData(tableName, "TYPE",
                TableColumnData.STRING_TYPE, 4, 23, false));
        tableColumnDataMap.put("DES", new TableColumnData(tableName, "DES",
                TableColumnData.STRING_TYPE, 30, 24, false));
        tableColumnDataMap.put("DET", new TableColumnData(tableName, "DET",
                TableColumnData.STRING_TYPE, 30, 25, false));
        tableColumnDataMap.put("POST", new TableColumnData(tableName, "POST",
                TableColumnData.INTEGER_TYPE, TableColumnData.INTEGER_LEN, 26,
                false));
        tableColumnDataMap.put("SNTYPE", new TableColumnData(tableName,
                "SNTYPE", TableColumnData.STRING_TYPE, 4, 27, false));
        tableColumnDataMap.put("TZONE", new TableColumnData(tableName, "TZONE",
                TableColumnData.STRING_TYPE, 8, 27, false));

    }

    public static synchronized final LocationQueryTable getInstance() {
        if (locationQueryTable == null) {
            locationQueryTable = new LocationQueryTable();
        }

        return (locationQueryTable);
    }
}

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
import com.raytheon.uf.common.hazards.ihfs.data.RiverStatTableData;

/**
 * This singleton class describes the data query model of the ihfs.RIVERSTAT
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
public class RiverStatQueryTable extends AbstractQueryTable {

    public static final String RiverStatTableName = "RIVERSTAT";

    private static RiverStatQueryTable riverStatQueryTable = null;

    // This declaration makes columnNameList immutable
    private static final List<String> RiverStatColumnNameList = Arrays.asList(
            "LID", "PRIMARY_PE", "BF", "CB", "DA", "RESPONSE_TIME",
            "THRESHOLD_RUNOFF", "FQ", "FS", "GSNO", "LEVEL", "MILE", "POOL",
            "POR", "RATED", "LAT", "LON", "REMARK", "RREVISE", "RSOURCE",
            "STREAM", "TIDE", "BACKWATER", "VDATUM", "ACTION_FLOW", "WSTG",
            "USGS_RATENUM", "UHGDUR", "USE_LATEST_FCST");

    private RiverStatQueryTable() {
        this(RiverStatTableName, RiverStatTableData.class);
    }

    private RiverStatQueryTable(String tableName,
            Class<? extends AbstractTableData> tableDataClass) {
        super(RiverStatTableName, RiverStatColumnNameList,
                RiverStatTableData.class);

        tableColumnDataMap = new HashMap<>(RiverStatColumnNameList.size());
        tableColumnDataMap.put("LID", new TableColumnData(tableName, "LID",
                TableColumnData.STRING_TYPE, 8, 0, true));
        tableColumnDataMap.put("PRIMARY_PE", new TableColumnData(tableName,
                "PRIMARY_PE", TableColumnData.STRING_TYPE, 2, 1, false));
        tableColumnDataMap.put("BF", new TableColumnData(tableName, "BF",
                TableColumnData.DOUBLE_TYPE, TableColumnData.DOUBLE_LEN, 2,
                false));
        tableColumnDataMap.put("CB", new TableColumnData(tableName, "CB",
                TableColumnData.DOUBLE_TYPE, TableColumnData.DOUBLE_LEN, 3,
                false));
        tableColumnDataMap.put("DA", new TableColumnData(tableName, "DA",
                TableColumnData.DOUBLE_TYPE, TableColumnData.DOUBLE_LEN, 4,
                false));
        tableColumnDataMap.put("RESPONSE_TIME", new TableColumnData(tableName,
                "RESPONSE_TIME", TableColumnData.DOUBLE_TYPE,
                TableColumnData.DOUBLE_LEN, 5, false));
        tableColumnDataMap.put("THRESHOLD_RUNOFF", new TableColumnData(
                tableName, "THRESHOLD_RUNOFF", TableColumnData.DOUBLE_TYPE,
                TableColumnData.DOUBLE_LEN, 6, false));
        tableColumnDataMap.put("FQ", new TableColumnData(tableName, "FQ",
                TableColumnData.DOUBLE_TYPE, TableColumnData.DOUBLE_LEN, 7,
                false));
        tableColumnDataMap.put("FS", new TableColumnData(tableName, "FS",
                TableColumnData.DOUBLE_TYPE, TableColumnData.DOUBLE_LEN, 8,
                false));
        tableColumnDataMap.put("GSNO", new TableColumnData(tableName, "GSNO",
                TableColumnData.STRING_TYPE, 10, 9, false));
        tableColumnDataMap.put("LEVEL", new TableColumnData(tableName, "LEVEL",
                TableColumnData.STRING_TYPE, 20, 10, false));
        tableColumnDataMap.put("MILE", new TableColumnData(tableName, "MILE",
                TableColumnData.DOUBLE_TYPE, TableColumnData.DOUBLE_LEN, 11,
                false));
        tableColumnDataMap.put("POOL", new TableColumnData(tableName, "POOL",
                TableColumnData.DOUBLE_TYPE, TableColumnData.DOUBLE_LEN, 12,
                false));
        tableColumnDataMap.put("POR", new TableColumnData(tableName, "POR",
                TableColumnData.STRING_TYPE, 30, 13, false));
        tableColumnDataMap.put("RATED", new TableColumnData(tableName, "RATED",
                TableColumnData.STRING_TYPE, 20, 14, false));
        tableColumnDataMap.put("LAT", new TableColumnData(tableName, "LAT",
                TableColumnData.DOUBLE_TYPE, TableColumnData.DOUBLE_LEN, 15,
                false));
        tableColumnDataMap.put("LON", new TableColumnData(tableName, "LON",
                TableColumnData.DOUBLE_TYPE, TableColumnData.DOUBLE_LEN, 16,
                false));
        tableColumnDataMap.put("REMARK", new TableColumnData(tableName,
                "REMARK", TableColumnData.STRING_TYPE, 255, 17, false));
        tableColumnDataMap.put("RREVISE", new TableColumnData(tableName,
                "RREVISE", TableColumnData.DATE_TYPE, TableColumnData.DATE_LEN,
                18, false));
        tableColumnDataMap.put("RSOURCE", new TableColumnData(tableName,
                "RSOURCE", TableColumnData.STRING_TYPE, 20, 19, false));
        tableColumnDataMap.put("STREAM", new TableColumnData(tableName,
                "STREAM", TableColumnData.STRING_TYPE, 32, 20, false));
        tableColumnDataMap.put("TIDE", new TableColumnData(tableName, "TIDE",
                TableColumnData.STRING_TYPE, 8, 21, false));
        tableColumnDataMap.put("BACKWATER", new TableColumnData(tableName,
                "BACKWATER", TableColumnData.STRING_TYPE, 8, 22, false));
        tableColumnDataMap.put("VDATUM", new TableColumnData(tableName,
                "VDATUM", TableColumnData.STRING_TYPE, 20, 23, false));
        tableColumnDataMap.put("ACTION_FLOW", new TableColumnData(tableName,
                "ACTION_FLOW", TableColumnData.DOUBLE_TYPE,
                TableColumnData.DOUBLE_LEN, 24, false));
        tableColumnDataMap.put("WSTG", new TableColumnData(tableName, "WSTG",
                TableColumnData.DOUBLE_TYPE, TableColumnData.DOUBLE_LEN, 25,
                false));
        tableColumnDataMap.put("ZD", new TableColumnData(tableName, "ZD",
                TableColumnData.DOUBLE_TYPE, TableColumnData.DOUBLE_LEN, 26,
                false));
        tableColumnDataMap.put("RATEDAT", new TableColumnData(tableName,
                "RATEDAT", TableColumnData.DATE_TYPE, TableColumnData.DATE_LEN,
                27, false));
        tableColumnDataMap.put("USGS_RATENUM", new TableColumnData(tableName,
                "USGS_RATENUM", TableColumnData.STRING_TYPE, 5, 28, false));
        tableColumnDataMap.put("UHGDUR", new TableColumnData(tableName,
                "UHGDUR", TableColumnData.INTEGER_TYPE,
                TableColumnData.INTEGER_LEN, 29, false));
        tableColumnDataMap.put("USE_LATEST_FCST", new TableColumnData(
                tableName, "USE_LATEST_FCST", TableColumnData.STRING_TYPE, 1,
                30, false));
    }

    public static synchronized final RiverStatQueryTable getInstance() {
        if (riverStatQueryTable == null) {
            riverStatQueryTable = new RiverStatQueryTable();
        }

        return (riverStatQueryTable);
    }
}

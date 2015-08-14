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
import com.raytheon.uf.common.hazards.ihfs.data.FpInfoTableData;

/**
 * This singleton class describes the data query model of the ihfs.FPINFO (VIEW)
 * table.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 28, 2015 8839       Chris.Cody  Initial Creation
 * Aug 14, 2015 9988       Chris.Cody  Base Database Code changes for Dates
 * 
 * </pre>
 * 
 * @author Chris.Cody
 * @version 1.0
 */
public class FpInfoQueryTable extends AbstractQueryTable {

    public static final String FpInfoTableName = "FPINFO";

    private static FpInfoQueryTable fpInfoQueryTable = null;

    // This declaration makes columnNameList immutable
    private static final List<String> FpInfoColumnNameList = Arrays.asList(
            "LID", "NAME", "COUNTY", "STATE", "HSA", "PRIMARY_BACK",
            "SECONDARY_BACK", "STREAM", "BF", "WSTG", "FS", "FQ",
            "ACTION_FLOW", "PE", "USE_LATEST_FCST", "PROXIMITY", "REACH",
            "GROUP_ID", "ORDINAL", "CHG_THRESHOLD", "REC_TYPE", "BACKHRS",
            "FORWARDHRS", "ADJUSTENDHRS", "MINOR_STAGE", "MODERATE_STAGE",
            "MAJOR_STAGE", "MINOR_FLOW", "MODERATE_FLOW", "MAJOR_FLOW");

    private FpInfoQueryTable() {
        this(FpInfoTableName, FpInfoTableData.class);
    }

    private FpInfoQueryTable(String tableName,
            Class<? extends AbstractTableData> tableDataClass) {
        super(FpInfoTableName, FpInfoColumnNameList, FpInfoTableData.class);

        tableColumnDataMap = new HashMap<>(FpInfoColumnNameList.size());
        tableColumnDataMap.put("LID", new TableColumnData(tableName, "LID",
                TableColumnData.STRING_TYPE, 8, 0, true));
        tableColumnDataMap.put("NAME", new TableColumnData(tableName, "NAME",
                TableColumnData.STRING_TYPE, 50, 1, false));
        tableColumnDataMap.put("COUNTY", new TableColumnData(tableName,
                "COUNTY", TableColumnData.STRING_TYPE, 20, 2, false));
        tableColumnDataMap.put("STATE", new TableColumnData(tableName, "STATE",
                TableColumnData.STRING_TYPE, 2, 3, false));
        tableColumnDataMap.put("HSA", new TableColumnData(tableName, "HSA",
                TableColumnData.STRING_TYPE, 3, 4, false));
        tableColumnDataMap.put("PRIMARY_BACK", new TableColumnData(tableName,
                "PRIMARY_BACK", TableColumnData.STRING_TYPE, 3, 5, false));
        tableColumnDataMap.put("SECONDARY_BACK", new TableColumnData(tableName,
                "SECONDARY_BACK", TableColumnData.STRING_TYPE, 3, 6, false));
        tableColumnDataMap.put("STREAM", new TableColumnData(tableName,
                "STREAM", TableColumnData.STRING_TYPE, 32, 7, false));
        tableColumnDataMap.put("BF", new TableColumnData(tableName, "BF",
                TableColumnData.DOUBLE_TYPE, TableColumnData.DOUBLE_LEN, 8,
                false));
        tableColumnDataMap.put("WSTG", new TableColumnData(tableName, "WSTG",
                TableColumnData.DOUBLE_TYPE, TableColumnData.DOUBLE_LEN, 9,
                false));
        tableColumnDataMap.put("FS", new TableColumnData(tableName, "FS",
                TableColumnData.DOUBLE_TYPE, TableColumnData.DOUBLE_LEN, 10,
                false));
        tableColumnDataMap.put("FQ", new TableColumnData(tableName, "FQ",
                TableColumnData.DOUBLE_TYPE, TableColumnData.DOUBLE_LEN, 11,
                false));
        tableColumnDataMap.put("ACTION_FLOW", new TableColumnData(tableName,
                "ACTION_FLOW", TableColumnData.DOUBLE_TYPE,
                TableColumnData.DOUBLE_LEN, 12, false));
        tableColumnDataMap.put("PE", new TableColumnData(tableName, "PE",
                TableColumnData.STRING_TYPE, 2, 13, false));
        tableColumnDataMap.put("USE_LATEST_FCST", new TableColumnData(
                tableName, "USE_LATEST_FCST", TableColumnData.STRING_TYPE, 1,
                14, false));
        tableColumnDataMap.put("PROXIMITY", new TableColumnData(tableName,
                "PROXIMITY", TableColumnData.STRING_TYPE, 1, 15, false));
        tableColumnDataMap.put("REACH", new TableColumnData(tableName, "ERACH",
                TableColumnData.STRING_TYPE, 80, 16, false));
        tableColumnDataMap.put("GROUP_ID", new TableColumnData(tableName,
                "GROUP_ID", TableColumnData.STRING_TYPE, 8, 17, false));
        tableColumnDataMap.put("ORDINAL", new TableColumnData(tableName,
                "ORDINAL", TableColumnData.INTEGER_TYPE,
                TableColumnData.INTEGER_LEN, 18, false));
        tableColumnDataMap.put("CHG_THRESHOLD", new TableColumnData(tableName,
                "CHG_THRESHOLD", TableColumnData.DOUBLE_TYPE,
                TableColumnData.DOUBLE_LEN, 19, false));
        tableColumnDataMap.put("REC_TYPE", new TableColumnData(tableName,
                "REC_TYPE", TableColumnData.STRING_TYPE, 3, 20, false));
        tableColumnDataMap.put("BACKHRS", new TableColumnData(tableName,
                "BACKHRS", TableColumnData.INTEGER_TYPE,
                TableColumnData.INTEGER_LEN, 21, false));
        tableColumnDataMap.put("FORWARDHRS", new TableColumnData(tableName,
                "FORWARDHRS", TableColumnData.INTEGER_TYPE,
                TableColumnData.INTEGER_LEN, 22, false));
        tableColumnDataMap.put("ADJUSTENDHRS", new TableColumnData(tableName,
                "ADJUSTENDHRS", TableColumnData.DOUBLE_TYPE,
                TableColumnData.DOUBLE_LEN, 23, false));
        tableColumnDataMap.put("MINOR_STAGE", new TableColumnData(tableName,
                "MINOR_STAGE", TableColumnData.DOUBLE_TYPE,
                TableColumnData.DOUBLE_LEN, 24, false));
        tableColumnDataMap.put("MODERATE_STAGE", new TableColumnData(tableName,
                "MODERATE_STAGE", TableColumnData.DOUBLE_TYPE,
                TableColumnData.DOUBLE_LEN, 25, false));
        tableColumnDataMap.put("MAJOR_STAGE", new TableColumnData(tableName,
                "MAJOR_STAGE", TableColumnData.DOUBLE_TYPE,
                TableColumnData.DOUBLE_LEN, 26, false));
        tableColumnDataMap.put("MINOR_FLOW", new TableColumnData(tableName,
                "MINOR_FLOW", TableColumnData.DOUBLE_TYPE,
                TableColumnData.DOUBLE_LEN, 27, false));
        tableColumnDataMap.put("MODERATE_FLOW", new TableColumnData(tableName,
                "MODERATE_FLOW", TableColumnData.DOUBLE_TYPE,
                TableColumnData.DOUBLE_LEN, 28, false));
        tableColumnDataMap.put("MAJOR_FLOW", new TableColumnData(tableName,
                "MAJOR_FLOW", TableColumnData.DOUBLE_TYPE,
                TableColumnData.DOUBLE_LEN, 29, false));
    }

    public static synchronized final FpInfoQueryTable getInstance() {
        if (fpInfoQueryTable == null) {
            fpInfoQueryTable = new FpInfoQueryTable();
        }

        return (fpInfoQueryTable);
    }
}

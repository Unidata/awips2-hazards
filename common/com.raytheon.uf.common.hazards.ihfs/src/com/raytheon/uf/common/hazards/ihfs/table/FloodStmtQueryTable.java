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
import com.raytheon.uf.common.hazards.ihfs.data.FloodStmtTableData;

/**
 * This singleton class describes the data query model of the ihfs.FLOODSTMT
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
public class FloodStmtQueryTable extends AbstractQueryTable {

    public static final String FloodStmtTableName = "FLOODSTMT";

    private static FloodStmtQueryTable floodStmtQueryTable = null;

    // This declaration makes columnNameList immutable
    private static final List<String> FloodStmtColumnNameList = Arrays.asList(
            "LID", "IMPACT_VALUE", "STATEMENT", "RF", "DATESTART", "DATEEND",
            "IMPACT_PE");

    private FloodStmtQueryTable() {
        this(FloodStmtTableName, FloodStmtTableData.class);
    }

    private FloodStmtQueryTable(String tableName,
            Class<? extends AbstractTableData> tableDataClass) {
        super(FloodStmtTableName, FloodStmtColumnNameList,
                FloodStmtTableData.class);

        tableColumnDataMap = new HashMap<>(FloodStmtColumnNameList.size());
        tableColumnDataMap.put("LID", new TableColumnData(tableName, "LID",
                TableColumnData.STRING_TYPE, 8, 0, true));
        tableColumnDataMap.put("IMPACT_VALUE", new TableColumnData(tableName,
                "IMPACT_VALUE", TableColumnData.DOUBLE_TYPE,
                TableColumnData.DOUBLE_LEN, 1, true));
        tableColumnDataMap.put("STATEMENT", new TableColumnData(tableName,
                "STATEMENT", TableColumnData.STRING_TYPE, 512, 2, false));
        tableColumnDataMap.put("RF", new TableColumnData(tableName, "RF",
                TableColumnData.STRING_TYPE, 1, 3, true));
        tableColumnDataMap.put("DATESTART", new TableColumnData(tableName,
                "DATESTART", TableColumnData.STRING_TYPE, 5, 4, true));
        tableColumnDataMap.put("DATEEND", new TableColumnData(tableName,
                "DATEEND", TableColumnData.STRING_TYPE, 5, 5, true));
        tableColumnDataMap.put("IMPACT_PE", new TableColumnData(tableName,
                "IMPACT_PE", TableColumnData.STRING_TYPE, 2, 6, false));
    }

    public static synchronized final FloodStmtQueryTable getInstance() {
        if (floodStmtQueryTable == null) {
            floodStmtQueryTable = new FloodStmtQueryTable();
        }

        return (floodStmtQueryTable);
    }
}

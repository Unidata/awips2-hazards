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
import com.raytheon.uf.common.hazards.ihfs.data.CountiesTableData;

/**
 * This singleton class describes the data query model of the ihfs.COUNTIES
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
public class CountiesQueryTable extends AbstractQueryTable {

    public static final String CountiesTableName = "COUNTIES";

    private static CountiesQueryTable countiesQueryTable = null;

    // This declaration makes columnNameList immutable
    private static final List<String> CountiesColumnNameList = Arrays.asList(
            "COUNTY", "STATE", "COUNTYNUM", "WFO", "PRIMARY_BACK",
            "SECONDARY_BACK");

    private CountiesQueryTable() {
        this(CountiesTableName, CountiesTableData.class);
    }

    private CountiesQueryTable(String tableName,
            Class<? extends AbstractTableData> tableDataClass) {
        super(CountiesTableName, CountiesColumnNameList,
                CountiesTableData.class);

        tableColumnDataMap = new HashMap<>(CountiesColumnNameList.size());
        tableColumnDataMap.put("COUNTY", new TableColumnData(tableName,
                "COUNTY", TableColumnData.STRING_TYPE, 20, 0, true));
        tableColumnDataMap.put("STATE", new TableColumnData(tableName, "STATE",
                TableColumnData.STRING_TYPE, 2, 1, true));
        tableColumnDataMap.put("COUNTYNUM", new TableColumnData(tableName,
                "COUNTYNUM", TableColumnData.STRING_TYPE, 4, 2, false));
        tableColumnDataMap.put("WFO", new TableColumnData(tableName, "WFO",
                TableColumnData.STRING_TYPE, 3, 3, false));
        tableColumnDataMap.put("PRIMARY_BACK", new TableColumnData(tableName,
                "PRIMARY_BACK", TableColumnData.STRING_TYPE, 3, 4, false));
        tableColumnDataMap.put("SECONDARY_BACK", new TableColumnData(tableName,
                "SECONDARY_BACK", TableColumnData.STRING_TYPE, 3, 4, false));
    }

    public static synchronized final CountiesQueryTable getInstance() {
        if (countiesQueryTable == null) {
            countiesQueryTable = new CountiesQueryTable();
        }

        return (countiesQueryTable);
    }
}

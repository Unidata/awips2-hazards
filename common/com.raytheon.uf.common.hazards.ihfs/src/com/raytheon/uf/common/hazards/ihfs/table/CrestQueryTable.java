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
import com.raytheon.uf.common.hazards.ihfs.data.CrestTableData;

/**
 * This singleton class describes the data query model of the ihfs.CREST table.
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
public class CrestQueryTable extends AbstractQueryTable {

    public static final String CrestTableName = "CREST";

    private static CrestQueryTable crestQueryTable = null;

    // This declaration makes columnNameList immutable
    private static final List<String> CrestColumnNameList = Arrays.asList(
            "LID", "DATCRST", "CREMARK", "HW", "JAM", "OLDDATUM", "Q", "STAGE",
            "SUPPRESS", "TIMCRST", "PRELIM");

    private CrestQueryTable() {
        this(CrestTableName, CrestTableData.class);
    }

    private CrestQueryTable(String tableName,
            Class<? extends AbstractTableData> tableDataClass) {
        super(CrestTableName, CrestColumnNameList, CrestTableData.class);

        tableColumnDataMap = new HashMap<>(CrestColumnNameList.size());
        tableColumnDataMap.put("LID", new TableColumnData(tableName, "LID",
                TableColumnData.STRING_TYPE, 8, 0, true));
        tableColumnDataMap.put("DATCRST", new TableColumnData(tableName,
                "DATCRST", TableColumnData.DATE_TYPE, 0, 1, true));
        tableColumnDataMap.put("CREMARK", new TableColumnData(tableName,
                "CREMARK", TableColumnData.STRING_TYPE, 80, 2, true));
        tableColumnDataMap.put("HW", new TableColumnData(tableName, "HW",
                TableColumnData.STRING_TYPE, 1, 3, false));
        tableColumnDataMap.put("JAM", new TableColumnData(tableName, "JAM",
                TableColumnData.STRING_TYPE, 1, 4, false));
        tableColumnDataMap.put("OLDDATUM", new TableColumnData(tableName,
                "OLDDATUM", TableColumnData.STRING_TYPE, 1, 5, false));
        tableColumnDataMap.put("Q", new TableColumnData(tableName, "Q",
                TableColumnData.INTEGER_TYPE, TableColumnData.INTEGER_LEN, 6,
                false));
        tableColumnDataMap.put("STAGE", new TableColumnData(tableName, "STAGE",
                TableColumnData.DOUBLE_TYPE, TableColumnData.DOUBLE_LEN, 7,
                false));
        tableColumnDataMap.put("SUPPRESS", new TableColumnData(tableName,
                "SUPPRESS", TableColumnData.STRING_TYPE, 1, 8, false));
        tableColumnDataMap.put("TIMCRST", new TableColumnData(tableName,
                "TIMCRST", TableColumnData.STRING_TYPE, 5, 9, false));
        tableColumnDataMap.put("PRELIM", new TableColumnData(tableName,
                "PRELIM", TableColumnData.STRING_TYPE, 1, 10, false));
    }

    public static synchronized final CrestQueryTable getInstance() {
        if (crestQueryTable == null) {
            crestQueryTable = new CrestQueryTable();
        }

        return (crestQueryTable);
    }
}

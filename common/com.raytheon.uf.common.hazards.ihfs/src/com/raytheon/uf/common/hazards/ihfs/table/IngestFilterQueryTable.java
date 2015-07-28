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
import com.raytheon.uf.common.hazards.ihfs.data.IngestFilterTableData;

/**
 * This singleton class describes the data query model of the ihfs.INGESTFILTER
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
public class IngestFilterQueryTable extends AbstractQueryTable {

    public static final String IngestFilterTableName = "INGESTFILTER";

    private static IngestFilterQueryTable ingestFilterQueryTable = null;

    // This declaration makes columnNameList immutable
    private static final List<String> IngestFilterColumnNameList = Arrays
            .asList("LID", "PE", "DUR", "TS", "EXTREMUM", "TS_RANK", "INGEST",
                    "OFS_INPUT", "STG2_INPUT");

    private IngestFilterQueryTable() {
        this(IngestFilterTableName, IngestFilterTableData.class);
    }

    private IngestFilterQueryTable(String tableName,
            Class<? extends AbstractTableData> tableDataClass) {
        super(IngestFilterTableName, IngestFilterColumnNameList,
                IngestFilterTableData.class);

        tableColumnDataMap = new HashMap<>(IngestFilterColumnNameList.size());
        tableColumnDataMap.put("LID", new TableColumnData(tableName, "LID",
                TableColumnData.STRING_TYPE, 8, 0, true));
        tableColumnDataMap.put("PE", new TableColumnData(tableName, "PE",
                TableColumnData.STRING_TYPE, 2, 1, true));
        tableColumnDataMap.put("DUR", new TableColumnData(tableName, "DUR",
                TableColumnData.INTEGER_TYPE, TableColumnData.INTEGER_LEN, 2,
                true));
        tableColumnDataMap.put("TS", new TableColumnData(tableName, "TS",
                TableColumnData.STRING_TYPE, 2, 3, true));
        tableColumnDataMap.put("EXTREMUM", new TableColumnData(tableName,
                "EXTREMUM", TableColumnData.STRING_TYPE, 1, 4, true));
        tableColumnDataMap.put("TS_RANK", new TableColumnData(tableName,
                "TS_RANK", TableColumnData.INTEGER_TYPE,
                TableColumnData.INTEGER_LEN, 5, false));
        tableColumnDataMap.put("INGEST", new TableColumnData(tableName,
                "INGEST", TableColumnData.STRING_TYPE, 1, 6, false));
        tableColumnDataMap.put("OFS_INPUT", new TableColumnData(tableName,
                "OFS_INPUT", TableColumnData.STRING_TYPE, 5, 7, false));
        tableColumnDataMap.put("STG2_INPUT", new TableColumnData(tableName,
                "STG2_INPUT", TableColumnData.STRING_TYPE, 1, 8, false));
    }

    public static synchronized final IngestFilterQueryTable getInstance() {
        if (ingestFilterQueryTable == null) {
            ingestFilterQueryTable = new IngestFilterQueryTable();
        }

        return (ingestFilterQueryTable);
    }
}

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
import com.raytheon.uf.common.hazards.ihfs.data.LocAreaTableData;

/**
 * This singleton class describes the data query model of the ihfs.LOCAREA
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
public class LocAreaQueryTable extends AbstractQueryTable {

    public static final String LocAreaTableName = "LOCAREA";

    private static LocAreaQueryTable locAreaQueryTable = null;

    // This declaration makes columnNameList immutable
    private static final List<String> LocAreaColumnNameList = Arrays.asList(
            "LID", "AREA");

    private LocAreaQueryTable() {
        this(LocAreaTableName, LocAreaTableData.class);
    }

    private LocAreaQueryTable(String tableName,
            Class<? extends AbstractTableData> tableDataClass) {
        super(LocAreaTableName, LocAreaColumnNameList, LocAreaTableData.class);

        tableColumnDataMap = new HashMap<>(LocAreaColumnNameList.size());
        tableColumnDataMap.put("LID", new TableColumnData(tableName, "LID",
                TableColumnData.STRING_TYPE, 8, 0, true));
        tableColumnDataMap.put("AREA", new TableColumnData(tableName, "AREA",
                TableColumnData.STRING_TYPE, 80, 1, false));
    }

    public static synchronized final LocAreaQueryTable getInstance() {
        if (locAreaQueryTable == null) {
            locAreaQueryTable = new LocAreaQueryTable();
        }

        return (locAreaQueryTable);
    }
}

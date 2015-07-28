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
import com.raytheon.uf.common.hazards.ihfs.data.ZoneNumTableData;

/**
 * This singleton class describes the data query model of the ihfs.ZONENUM
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
public class ZoneNumQueryTable extends AbstractQueryTable {

    public static final String ZoneNumTableName = "ZONENUM";

    private static ZoneNumQueryTable zoneNumQueryTable = null;

    // This declaration makes columnNameList immutable
    private static final List<String> ZoneNumColumnNameList = Arrays.asList(
            "LID", "STATE", "ZONENUM");

    private ZoneNumQueryTable() {
        this(ZoneNumTableName, ZoneNumTableData.class);
    }

    private ZoneNumQueryTable(String tableName,
            Class<? extends AbstractTableData> tableDataClass) {
        super(ZoneNumTableName, ZoneNumColumnNameList, ZoneNumTableData.class);

        tableColumnDataMap = new HashMap<>(ZoneNumColumnNameList.size());
        tableColumnDataMap.put("LID", new TableColumnData(tableName, "LID",
                TableColumnData.STRING_TYPE, 8, 0, true));
        tableColumnDataMap.put("STATE", new TableColumnData(tableName, "STATE",
                TableColumnData.STRING_TYPE, 2, 1, true));
        tableColumnDataMap.put("ZONENUM", new TableColumnData(tableName,
                "ZONENUM", TableColumnData.STRING_TYPE, 3, 2, true));
    }

    public static synchronized final ZoneNumQueryTable getInstance() {
        if (zoneNumQueryTable == null) {
            zoneNumQueryTable = new ZoneNumQueryTable();
        }

        return (zoneNumQueryTable);
    }
}

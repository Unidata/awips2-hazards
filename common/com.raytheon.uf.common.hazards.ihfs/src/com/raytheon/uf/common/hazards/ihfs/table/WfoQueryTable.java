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
import com.raytheon.uf.common.hazards.ihfs.data.WfoTableData;

/**
 * This singleton class describes the data query model of the ihfs.WFO table.
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
public class WfoQueryTable extends AbstractQueryTable {

    public static final String WfoTableName = "WFO";

    private static WfoQueryTable wfoQueryTable = null;

    // This declaration makes columnNameList immutable
    private static final List<String> WfoColumnNameList = Arrays.asList("WFO");

    private WfoQueryTable() {
        this(WfoTableName, WfoTableData.class);
    }

    private WfoQueryTable(String tableName,
            Class<? extends AbstractTableData> tableDataClass) {
        super(WfoTableName, WfoColumnNameList, WfoTableData.class);

        tableColumnDataMap = new HashMap<>(WfoColumnNameList.size());
        tableColumnDataMap.put("WFO", new TableColumnData(tableName, "WFO",
                TableColumnData.STRING_TYPE, 3, 0, true));
    }

    public static synchronized final WfoQueryTable getInstance() {
        if (wfoQueryTable == null) {
            wfoQueryTable = new WfoQueryTable();
        }

        return (wfoQueryTable);
    }
}

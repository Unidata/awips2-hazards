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
import com.raytheon.uf.common.hazards.ihfs.data.RpfFcstGroupTableData;

/**
 * This singleton class describes the data query model of the ihfs.RPFFCSTGROUP
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
public class RpfFcstGroupQueryTable extends AbstractQueryTable {

    public static final String RpfFcstGroupTableName = "RPFFCSTGROUP";

    private static RpfFcstGroupQueryTable rpfFcstGroupQueryTable = null;

    // This declaration makes columnNameList immutable
    private static final List<String> RpfFcstGroupColumnNameList = Arrays
            .asList("GROUP_ID", "GROUP_NAME", "ORDINAL", "REC_ALL_INCLUDED");

    private RpfFcstGroupQueryTable() {
        this(RpfFcstGroupTableName, RpfFcstGroupTableData.class);
    }

    private RpfFcstGroupQueryTable(String tableName,
            Class<? extends AbstractTableData> tableDataClass) {
        super(RpfFcstGroupTableName, RpfFcstGroupColumnNameList,
                RpfFcstGroupTableData.class);

        tableColumnDataMap = new HashMap<>(RpfFcstGroupColumnNameList.size());
        tableColumnDataMap.put("GROUP_ID", new TableColumnData(tableName,
                "GROUP_ID", TableColumnData.STRING_TYPE, 8, 0, true));
        tableColumnDataMap.put("GROUP_NAME", new TableColumnData(tableName,
                "GROUP_NAME", TableColumnData.STRING_TYPE, 32, 1, true));
        tableColumnDataMap.put("ORDINAL", new TableColumnData(tableName,
                "ORDINAL", TableColumnData.INTEGER_TYPE,
                TableColumnData.INTEGER_LEN, 2, true));
        tableColumnDataMap.put("REC_ALL_INCLUDED", new TableColumnData(
                tableName, "REC_ALL_INCLUDED", TableColumnData.STRING_TYPE, 1,
                3, true));
    }

    public static synchronized final RpfFcstGroupQueryTable getInstance() {
        if (rpfFcstGroupQueryTable == null) {
            rpfFcstGroupQueryTable = new RpfFcstGroupQueryTable();
        }

        return (rpfFcstGroupQueryTable);
    }
}

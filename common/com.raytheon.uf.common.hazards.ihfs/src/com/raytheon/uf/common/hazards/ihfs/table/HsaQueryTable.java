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
import com.raytheon.uf.common.hazards.ihfs.data.HsaTableData;

/**
 * This singleton class describes the data query model of the ihfs.HSA table.
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
public class HsaQueryTable extends AbstractQueryTable {

    public static final String HsaTableName = "HSA";

    private static HsaQueryTable hsaQueryTable = null;

    // This declaration makes columnNameList immutable
    private static final List<String> HsaColumnNameList = Arrays.asList("HSA");

    private HsaQueryTable() {
        this(HsaTableName, HsaTableData.class);
    }

    private HsaQueryTable(String tableName,
            Class<? extends AbstractTableData> tableDataClass) {
        super(HsaTableName, HsaColumnNameList, HsaTableData.class);

        tableColumnDataMap = new HashMap<>(HsaColumnNameList.size());
        tableColumnDataMap.put("HSA", new TableColumnData(tableName, "HSA",
                TableColumnData.STRING_TYPE, 3, 0, true));
    }

    public static synchronized final HsaQueryTable getInstance() {
        if (hsaQueryTable == null) {
            hsaQueryTable = new HsaQueryTable();
        }

        return (hsaQueryTable);
    }
}

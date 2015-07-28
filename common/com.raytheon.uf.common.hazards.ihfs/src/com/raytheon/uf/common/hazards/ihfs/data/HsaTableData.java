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
package com.raytheon.uf.common.hazards.ihfs.data;

import java.io.Serializable;

import com.raytheon.uf.common.hazards.ihfs.IhfsDatabaseException;
import com.raytheon.uf.common.hazards.ihfs.IhfsUtil;
import com.raytheon.uf.common.hazards.ihfs.table.HsaQueryTable;

/**
 * This class is used to contain all column data for a ihfs.HSA table Row.
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
public class HsaTableData extends AbstractTableData {

    private static final long serialVersionUID = 96357700602583520L;

    protected String hsa;

    public HsaTableData() {
        super(HsaQueryTable.getInstance());
    }

    public HsaTableData(Object[] tableData) throws IhfsDatabaseException {
        super(HsaQueryTable.getInstance(), tableData);
    }

    @Override
    public String getId() {
        return (this.hsa);
    }

    @Override
    protected Serializable getColumnByName(String columnName)
            throws IhfsDatabaseException {

        if (columnName != null) {
            switch (columnName) {
            case "HSA":
                return (getHsa());
            default:
                throw (new IhfsDatabaseException("Invalid Column Name "
                        + columnName + " for " + getTableName()));
            }
        }
        return (null);
    }

    @Override
    public void setColumnByName(String columnName, Serializable columnValue)
            throws IhfsDatabaseException {

        if (columnName != null) {
            int idx = columnName.indexOf(".");
            if (idx > 0) {
                String tableName = getTableName();
                String tempTableName = IhfsUtil.parseTableName(columnName);
                if (tableName.equals(tempTableName) == false) {
                    String msg = "Invalid Table Name: " + tempTableName
                            + " for this class ";
                    throw (new IhfsDatabaseException(msg));
                }

                // Remove Table Name from input string
                columnName = IhfsUtil.parseTableName(columnName);
            }
            switch (columnName) {
            case "HSA":
                setHsa((String) columnValue);
                break;
            default:
                String msg = "Invalid Column Name: " + columnName
                        + " for this class";
                throw (new IhfsDatabaseException(msg));
            }
        } else {
            String msg = "Null Column Name";
            throw (new IhfsDatabaseException(msg));
        }
    }

    public String getHsa() {
        return (this.hsa);
    }

    public void setHsa(String hsa) {
        this.hsa = hsa;
    }

}

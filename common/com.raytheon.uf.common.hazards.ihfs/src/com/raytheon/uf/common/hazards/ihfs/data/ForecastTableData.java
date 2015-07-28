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
import com.raytheon.uf.common.hazards.ihfs.table.AbstractQueryTable;

/**
 * This class is the abstract parent for all common ihfs Forecast type table row
 * data.
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
public abstract class ForecastTableData extends IhfsTableData {

    private static final long serialVersionUID = -6921334978612334180L;

    protected Float probability;

    protected Long validTime;

    protected Long basisTime;

    protected ForecastTableData(AbstractQueryTable abstractQueryTable) {
        super(abstractQueryTable);
    }

    protected ForecastTableData(AbstractQueryTable abstractQueryTable,
            Object[] tableData) throws IhfsDatabaseException {
        super(abstractQueryTable, tableData);
    }

    public abstract String getId();

    protected Serializable getColumnByName(String columnName) {

        if (columnName != null) {
            switch (columnName) {
            case "PROBABILITY":
                return (getProbability());
            case "VALIDTIME":
                return (getValidTime());
            case "BASISTIME":
                return (getBasisTime());
            default:
                return (super.getColumnByName(columnName));
            }
        }
        return (null);
    }

    public void setColumnByName(String columnName, Serializable columnValue)
            throws IhfsDatabaseException {

        java.sql.Timestamp timestamp = null;
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
            case "PROBABILITY":
                setProbability(getFloatColumnValue(columnValue));
                break;
            case "VALIDTIME":
                timestamp = (java.sql.Timestamp) columnValue;
                if (timestamp != null) {
                    setValidTime(timestamp.getTime());
                }
                break;
            case "BASISTIME":
                timestamp = (java.sql.Timestamp) columnValue;
                if (timestamp != null) {
                    setBasisTime(timestamp.getTime());
                }
                break;
            default:
                super.setColumnByName(columnName, columnValue);
            }
        } else {
            String msg = "Null Column Name";
            throw (new IhfsDatabaseException(msg));
        }
    }

    public String getProbability() {
        return (this.extremum);
    }

    public void setProbability(Float probability) {
        this.probability = probability;
    }

    public Long getValidTime() {
        return (this.validTime);
    }

    public void setValidTime(Long validTime) {
        this.validTime = validTime;
    }

    public Long getBasisTime() {
        return (this.basisTime);
    }

    public void setBasisTime(Long basisTime) {
        this.basisTime = basisTime;
    }
}

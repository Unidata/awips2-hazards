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
import com.raytheon.uf.common.hazards.ihfs.table.CountiesQueryTable;

/**
 * This class is used to contain all column data for a ihfs.COUNTIES table Row.
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
public class CountiesTableData extends AbstractTableData {

    private static final long serialVersionUID = -5018183531572842512L;

    protected String county;

    protected String state;

    protected String countyNum;

    protected String wfo;

    protected String primaryBack;

    protected String secondaryBack;

    public CountiesTableData() {
        super(CountiesQueryTable.getInstance());
    }

    public CountiesTableData(Object[] tableData) throws IhfsDatabaseException {
        super(CountiesQueryTable.getInstance(), tableData);
    }

    @Override
    public String getId() {
        return (this.county);
    }

    @Override
    protected Serializable getColumnByName(String columnName)
            throws IhfsDatabaseException {

        if (columnName != null) {
            switch (columnName) {
            case "COUNTY":
                return (getCounty());
            case "STATE":
                return (getState());
            case "COUNTYNUM":
                return (getCountyNum());
            case "WFO":
                return (getWfo());
            case "PRIMARY_BACK":
                return (getPrimaryBack());
            case "SECONDARY_BACK":
                return (getSecondaryBack());
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
            case "COUNTY":
                setCounty((String) columnValue);
                break;
            case "STATE":
                setState((String) columnValue);
                break;
            case "COUNTYNUM":
                setCountyNum((String) columnValue);
                break;
            case "WFO":
                setWfo((String) columnValue);
                break;
            case "PRIMARY_BACK":
                setPrimaryBack((String) columnValue);
                break;
            case "SECONDARY_BACK":
                setSecondaryBack((String) columnValue);
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

    public String getCounty() {
        return (this.county);
    }

    public void setCounty(String county) {
        this.county = county;
    }

    public String getState() {
        return (this.state);
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCountyNum() {
        return (this.countyNum);
    }

    public void setCountyNum(String countyNum) {
        this.countyNum = countyNum;
    }

    public String getWfo() {
        return (this.wfo);
    }

    public void setWfo(String wfo) {
        this.wfo = wfo;
    }

    public String getPrimaryBack() {
        return (this.primaryBack);
    }

    public void setPrimaryBack(String primaryBack) {
        this.primaryBack = primaryBack;
    }

    public String getSecondaryBack() {
        return (this.secondaryBack);
    }

    public void setSecondaryBack(String secondaryBack) {
        this.secondaryBack = secondaryBack;
    }

}

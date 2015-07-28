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
import com.raytheon.uf.common.hazards.ihfs.table.FloodStmtQueryTable;

/**
 * This class is used to contain all column data for a ihfs.FLOODSTMT table Row.
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
public class FloodStmtTableData extends AbstractTableData {

    private static final long serialVersionUID = 4805571852460303219L;

    protected String lid;

    protected String impactValue;

    protected String statement;

    protected String rf;

    protected String dateStart;

    protected String dateEnd;

    protected String impactPe;

    public FloodStmtTableData() {
        super(FloodStmtQueryTable.getInstance());
    }

    public FloodStmtTableData(Object[] tableData) throws IhfsDatabaseException {
        super(FloodStmtQueryTable.getInstance(), tableData);
    }

    @Override
    public String getId() {
        return (this.lid);
    }

    @Override
    protected Serializable getColumnByName(String columnName)
            throws IhfsDatabaseException {

        if (columnName != null) {
            switch (columnName) {
            case "LID":
                return (getLid());
            case "IMPACT_VALUE":
                return (getImpactValue());
            case "STATEMENT":
                return (getStatement());
            case "RF":
                return (getRf());
            case "DATESTART":
                return (getDateStart());
            case "DATEEND":
                return (getDateEnd());
            case "IMPACT_PE":
                return (getImpactPe());
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
            case "LID":
                setLid((String) columnValue);
                break;
            case "IMPACT_VALUE":
                setImpactValue((String) columnValue);
                break;
            case "STATEMENT":
                setStatement((String) columnValue);
                break;
            case "RF":
                setRf((String) columnValue);
                break;
            case "DATESTART":
                setDateStart((String) columnValue);
            case "DATEEND":
                setDateEnd((String) columnValue);
                break;
            case "IMPACT_PE":
                setImpactPe((String) columnValue);
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

    public String getLid() {
        return (this.lid);
    }

    public void setLid(String lid) {
        this.lid = lid;
    }

    public String getImpactValue() {
        return (this.impactValue);
    }

    public void setImpactValue(String impactValue) {
        this.impactValue = impactValue;
    }

    public String getStatement() {
        return (this.statement);
    }

    public void setStatement(String statement) {
        this.statement = statement;
    }

    public String getRf() {
        return (this.rf);
    }

    public void setRf(String rf) {
        this.rf = rf;
    }

    public String getDateStart() {
        return (this.dateStart);
    }

    public void setDateStart(String dateStart) {
        this.dateStart = dateStart;
    }

    public String getDateEnd() {
        return (this.dateEnd);
    }

    public void setDateEnd(String dateEnd) {
        this.dateEnd = dateEnd;
    }

    public String getImpactPe() {
        return (this.impactPe);
    }

    public void setImpactPe(String impactPe) {
        this.impactPe = impactPe;
    }

}

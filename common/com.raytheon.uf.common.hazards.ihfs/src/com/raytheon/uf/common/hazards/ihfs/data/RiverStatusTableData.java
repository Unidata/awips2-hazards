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
import com.raytheon.uf.common.hazards.ihfs.table.RiverStatusQueryTable;

/**
 * This class is used to contain all column data for a ihfs.RIVERSTATUS table
 * Row.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 28, 2015 8839       Chris.Cody  Initial Creation
 * Aug 11, 2015 9670       Chris.Cody  Changes from Timestamp to Date
 * 
 * </pre>
 * 
 * @author Chris.Cody
 * @version 1.0
 */
public class RiverStatusTableData extends AbstractTableData {

    private static final long serialVersionUID = -6921334978612334180L;

    protected String lid;

    protected String pe;

    protected Integer dur;

    protected String ts;

    protected String extremum;

    protected Float probability;

    protected Long validTime;

    protected Long basisTime;

    protected Double value;

    public RiverStatusTableData() {
        super(RiverStatusQueryTable.getInstance());
    }

    public RiverStatusTableData(Object[] tableData)
            throws IhfsDatabaseException {
        super(RiverStatusQueryTable.getInstance(), tableData);
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
            case "PE":
                return (getPe());
            case "DUR":
                return (getDur());
            case "TS":
                return (getTs());
            case "EXTREMUM":
                return (getExtremum());
            case "PROBABILITY":
                return (getProbability());
            case "VALIDTIME":
                return (getValidTime());
            case "BASISTIME":
                return (getBasisTime());
            case "VALUE":
                return (getValue());
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

        java.util.Date timestampDate = null;
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
            case "PE":
                setPe((String) columnValue);
                break;
            case "DUR":
                setDur(getIntegerColumnValue(columnValue));
                break;
            case "TS":
                setTs((String) columnValue);
                break;
            case "EXTREMUM":
                setExtremum((String) columnValue);
                break;
            case "PROBABILITY":
                setProbability(getFloatColumnValue(columnValue));
                break;
            case "VALIDTIME":
                timestampDate = (java.util.Date) columnValue;
                if (timestampDate != null) {
                    setValidTime(timestampDate.getTime());
                }
                break;
            case "BASISTIME":
                timestampDate = (java.util.Date) columnValue;
                if (timestampDate != null) {
                    setBasisTime(timestampDate.getTime());
                }
                break;
            case "VALUE":
                setValue(getDoubleColumnValue(columnValue));
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

    public String getLid() {
        return (lid);
    }

    public void setLid(String lid) {
        this.lid = lid;
    }

    public String getPe() {
        return (this.pe);
    }

    public void setPe(String pe) {
        this.pe = pe;
    }

    public Integer getDur() {
        return (this.dur);
    }

    public void setDur(Integer dur) {
        this.dur = dur;
    }

    public String getTs() {
        return (this.ts);
    }

    public void setTs(String ts) {
        this.ts = ts;
    }

    public String getExtremum() {
        return (this.extremum);
    }

    public void setExtremum(String extremum) {
        this.extremum = extremum;
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

    public Double getValue() {
        return (this.value);
    }

    public void setValue(Double value) {
        this.value = value;
    }

}

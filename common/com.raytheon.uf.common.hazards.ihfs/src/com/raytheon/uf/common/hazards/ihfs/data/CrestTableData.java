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
import com.raytheon.uf.common.hazards.ihfs.table.CrestQueryTable;

/**
 * This class is used to contain all column data for a ihfs.CREST table Row.
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
public class CrestTableData extends AbstractTableData {

    private static final long serialVersionUID = 4805571852460303219L;

    protected String lid;

    protected Long datCrst;

    protected String creMark;

    protected String hw;

    protected String jam;

    protected String oldDatum;

    protected Integer q;

    protected Double stage;

    protected String suppress;

    protected String timCrst;

    protected String prelim;

    public CrestTableData() {
        super(CrestQueryTable.getInstance());
    }

    public CrestTableData(Object[] tableData) throws IhfsDatabaseException {
        super(CrestQueryTable.getInstance(), tableData);
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
            case "DATCRST":
                return (getDatCrst());
            case "CREMARK":
                return (getCreMark());
            case "HW":
                return (getHw());
            case "JAM":
                return (getJam());
            case "OLDDATUM":
                return (getOldDatum());
            case "Q":
                return (getQ());
            case "STAGE":
                return (getStage());
            case "SUPPRESS":
                return (getSuppress());
            case "TIMCRST":
                return (getTimCrst());
            case "PRELIM":
                return (getPrelim());
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
            case "DATCRST":
                java.util.Date timestampDate = (java.util.Date) columnValue;
                if (timestampDate != null) {
                    setDatCrst(timestampDate.getTime());
                }
                break;
            case "CREMARK":
                setCreMark((String) columnValue);
                break;
            case "HW":
                setHw((String) columnValue);
                break;
            case "OLDDATUM":
                setOldDatum((String) columnValue);
                break;
            case "Q":
                setQ(getIntegerColumnValue(columnValue));
                break;
            case "STAGE":
                setStage(getDoubleColumnValue(columnValue));
                break;
            case "SUPPRESS":
                setSuppress((String) columnValue);
                break;
            case "TIMCRST":
                setTimCrst((String) columnValue);
                break;
            case "PRELIM":
                setPrelim((String) columnValue);
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
        return (this.lid);
    }

    public void setLid(String lid) {
        this.lid = lid;
    }

    public Long getDatCrst() {
        return (this.datCrst);
    }

    public void setDatCrst(Long datCrst) {
        this.datCrst = datCrst;
    }

    public String getCreMark() {
        return (this.creMark);
    }

    public void setCreMark(String creMark) {
        this.creMark = creMark;
    }

    public String getHw() {
        return (this.hw);
    }

    public void setHw(String hw) {
        this.hw = hw;
    }

    public String getJam() {
        return (this.jam);
    }

    public void setJam(String jam) {
        this.jam = jam;
    }

    public String getOldDatum() {
        return (this.oldDatum);
    }

    public void setOldDatum(String oldDatum) {
        this.oldDatum = oldDatum;
    }

    public Integer getQ() {
        return (this.q);
    }

    public void setQ(Integer q) {
        this.q = q;
    }

    public Double getStage() {
        return (this.stage);
    }

    public void setStage(Double stage) {
        this.stage = stage;
    }

    public String getSuppress() {
        return (this.suppress);
    }

    public void setSuppress(String suppress) {
        this.suppress = suppress;
    }

    public String getTimCrst() {
        return (this.timCrst);
    }

    public void setTimCrst(String timCrst) {
        this.timCrst = timCrst;
    }

    public String getPrelim() {
        return (this.prelim);
    }

    public void setPrelim(String prelim) {
        this.prelim = prelim;
    }

}

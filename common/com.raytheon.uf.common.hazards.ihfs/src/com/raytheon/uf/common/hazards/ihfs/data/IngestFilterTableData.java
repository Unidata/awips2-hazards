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
import com.raytheon.uf.common.hazards.ihfs.table.IngestFilterQueryTable;

/**
 * This class is used to contain all column data for a ihfs.INGESTFILTER table
 * Row.
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
public class IngestFilterTableData extends AbstractTableData {

    private static final long serialVersionUID = -5913640461732352653L;

    protected String lid;

    protected String pe;

    protected Integer dur;

    protected String ts;

    protected String extremum;

    protected Integer tsRank;

    protected String ingest;

    protected String ofsInput;

    protected String stg2Input;

    public IngestFilterTableData() {
        super(IngestFilterQueryTable.getInstance());
    }

    public IngestFilterTableData(Object[] tableData)
            throws IhfsDatabaseException {
        super(IngestFilterQueryTable.getInstance(), tableData);
    }

    public String getId() {
        return (this.lid);
    }

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
            case "TS_RANK":
                return (getTsRank());
            case "INGEST":
                return (getIngest());
            case "OFS_INPUT":
                return (getOfsInput());
            case "STG2_INPUT":
                return (getStg2Input());
            default:
                throw (new IhfsDatabaseException("Invalid Column Name "
                        + columnName + " for " + getTableName()));
            }
        }
        return (null);
    }

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
            case "TS_RANK":
                setTsRank(getIntegerColumnValue(columnValue));
                break;
            case "INGEST":
                setIngest((String) columnValue);
                break;
            case "OFS_INPUT":
                setOfsInput((String) columnValue);
                break;
            case "STG2_INPUT":
                setStg2Input((String) columnValue);
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

    public Integer getTsRank() {
        return (this.tsRank);
    }

    public void setTsRank(Integer tsRank) {
        this.tsRank = tsRank;
    }

    public String getIngest() {
        return (this.ingest);
    }

    public void setIngest(String ingest) {
        this.ingest = ingest;
    }

    public String getOfsInput() {
        return (this.ofsInput);
    }

    public void setOfsInput(String ofsInput) {
        this.ofsInput = ofsInput;
    }

    public String getStg2Input() {
        return (this.stg2Input);
    }

    public void setStg2Input(String stg2Input) {
        this.stg2Input = stg2Input;
    }

}

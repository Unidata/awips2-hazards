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
 * This class is used to contain all column data for a ihfs.HEIGHT table Row.
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
public abstract class IhfsTableData extends AbstractTableData {

    private static final long serialVersionUID = -1161023905412968859L;

    protected String lid;

    protected String pe;

    protected Integer dur;

    protected String ts;

    protected String extremum;

    protected Double value;

    protected String shefQualCode;

    protected Integer qualityCode;

    protected Integer revision;

    protected String productId;

    protected Long productTime;

    protected Long postingTime;

    protected IhfsTableData(AbstractQueryTable abstractQueryTable) {
        super(abstractQueryTable);
    }

    protected IhfsTableData(AbstractQueryTable abstractQueryTable,
            Object[] tableData) throws IhfsDatabaseException {
        super(abstractQueryTable, tableData);
    }

    @Override
    public abstract String getId();

    @Override
    protected Serializable getColumnByName(String columnName) {
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
            case "VALUE":
                return (getValue());
            case "SHEF_QUAL_CODE":
                return (getShefQualCode());
            case "QUALITY_CODE":
                return (getQualityCode());
            case "REVISION":
                return (getRevision());
            case "PRODUCT_ID":
                return (getProductId());
            case "PRODUCTTIME":
                return (getProductTime());
            case "POSTINGTIME":
                return (getPostingTime());
            default:
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
            case "VALUE":
                setValue(getDoubleColumnValue(columnValue));
                break;
            case "SHEF_QUAL_CODE":
                setShefQualCode((String) columnValue);
                break;
            case "QUALITY_CODE":
                setQualityCode(getIntegerColumnValue(columnValue));
                break;
            case "REVISION":
                setRevision(getIntegerColumnValue(columnValue));
                break;
            case "PRODUCT_ID":
                setProductId((String) columnValue);
                break;
            case "PRODUCTTIME":
                timestampDate = (java.util.Date) columnValue;
                if (timestampDate != null) {
                    setProductTime(timestampDate.getTime());
                }
                break;
            case "POSTINGTIME":
                timestampDate = (java.util.Date) columnValue;
                if (timestampDate != null) {
                    setPostingTime(timestampDate.getTime());
                }
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

    public String getTs() {
        return (this.ts);
    }

    public void setTs(String ts) {
        this.ts = ts;
    }

    public Integer getDur() {
        return (this.dur);
    }

    public void setDur(Integer dur) {
        this.dur = dur;
    }

    public String getExtremum() {
        return (this.extremum);
    }

    public void setExtremum(String extremum) {
        this.extremum = extremum;
    }

    public Double getValue() {
        return (this.value);
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public String getShefQualCode() {
        return (this.shefQualCode);
    }

    public void setShefQualCode(String shefQualityCode) {
        this.shefQualCode = shefQualityCode;
    }

    public Integer getQualityCode() {
        return (this.qualityCode);
    }

    public void setQualityCode(Integer qualityCode) {
        this.qualityCode = qualityCode;
    }

    public Integer getRevision() {
        return (this.revision);
    }

    public void setRevision(Integer revision) {
        this.revision = revision;
    }

    public String getProductId() {
        return (this.productId);
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public Long getProductTime() {
        return (this.productTime);
    }

    public void setProductTime(Long productTime) {
        this.productTime = productTime;
    }

    public Long getPostingTime() {
        return (this.postingTime);
    }

    public void setPostingTime(Long postingTime) {
        this.postingTime = postingTime;
    }

}

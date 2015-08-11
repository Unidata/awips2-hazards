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
import com.raytheon.uf.common.hazards.ihfs.table.RiverStatQueryTable;

/**
 * This class is used to contain all column data for a ihfs.RIVERSTAT table Row.
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
public class RiverStatTableData extends AbstractTableData {

    private static final long serialVersionUID = 5821025895658710216L;

    protected String lid;

    protected String primaryPe;

    protected Double bf;

    protected Double cb;

    protected Double da;

    protected Double responseTime;

    protected Double thresholdRunoff;

    protected Double fq;

    protected Double fs;

    protected String gsno;

    protected String level;

    protected Double mile;

    protected Double pool;

    protected String por;

    protected String rated;

    protected Double lat;

    protected Double lon;

    protected String remark;

    protected String rrevise;

    protected String rsource;

    protected String stream;

    protected String tide;

    protected String backwater;

    protected String vdatum;

    protected Double actionFlow;

    protected Double wstg;

    protected Double zd;

    protected Long ratedAt;

    protected String usgsRateNum;

    protected Integer uhgdur;

    protected String useLatestFcst;

    public RiverStatTableData() {
        super(RiverStatQueryTable.getInstance());
    }

    public RiverStatTableData(Object[] tableData) throws IhfsDatabaseException {
        super(RiverStatQueryTable.getInstance(), tableData);
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
            case "PRIMARY_PE":
                return (getPrimaryPe());
            case "BF":
                return (getBf());
            case "CB":
                return (getCb());
            case "DA":
                return (getDa());
            case "RESPONSE_TIME":
                return (getResponseTime());
            case "THRESHOLD_RUNOFF":
                return (getThresholdRunoff());
            case "FQ":
                return (getFq());
            case "FS":
                return (getFs());
            case "GSNO":
                return (getGsno());
            case "LEVEL":
                return (getLevel());
            case "MILE":
                return (getMile());
            case "POOL":
                return (getPool());
            case "POR":
                return (getPor());
            case "RATED":
                return (getRated());
            case "LAT":
                return (getLat());
            case "LON":
                return (getLon());
            case "REMARK":
                return (getRemark());
            case "RREVISE":
                return (getRrevise());
            case "RSOURCE":
                return (getRsource());
            case "STREAM":
                return (getStream());
            case "TIDE":
                return (getTide());
            case "BACKWATER":
                return (getBackWater());
            case "VDATUM":
                return (getVdatum());
            case "ACTION_FLOW":
                return (getActionFlow());
            case "WSTG":
                return (getWstg());
            case "USGS_RATENUM":
                return (getUsgsRateNum());
            case "UHGDUR":
                return (getUhgdur());
            case "USE_LATEST_FCST":
                return (getUseLatestFcst());
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
            case "PRIMARY_PE":
                setPrimaryPe((String) columnValue);
                break;
            case "BF":
                setBf(getDoubleColumnValue(columnValue));
                break;
            case "CB":
                setCb(getDoubleColumnValue(columnValue));
                break;
            case "DA":
                setDa(getDoubleColumnValue(columnValue));
                break;
            case "RESPONSE_TIME":
                setResponseTime(getDoubleColumnValue(columnValue));
                break;
            case "THRESHOLD_RUNOFF":
                setThresholdRunoff(getDoubleColumnValue(columnValue));
                break;
            case "FQ":
                setFq(getDoubleColumnValue(columnValue));
                break;
            case "FS":
                setFs(getDoubleColumnValue(columnValue));
                break;
            case "GSNO":
                setGsno((String) columnValue);
                break;
            case "LEVEL":
                setLevel((String) columnValue);
                break;
            case "MILE":
                setMile(getDoubleColumnValue(columnValue));
                break;
            case "POOL":
                setPool(getDoubleColumnValue(columnValue));
                break;
            case "POR":
                setPor((String) columnValue);
                break;
            case "RATED":
                setRated((String) columnValue);
                break;
            case "LAT":
                setLat(getDoubleColumnValue(columnValue));
                break;
            case "LON":
                setLon(getDoubleColumnValue(columnValue));
                break;
            case "REMARK":
                setRemark((String) columnValue);
                break;
            case "RREVISE":
                setRrevise((String) columnValue);
                break;
            case "RSOURCE":
                setRsource((String) columnValue);
                break;
            case "STREAM":
                setStream((String) columnValue);
                break;
            case "TIDE":
                setTide((String) columnValue);
                break;
            case "BACKWATER":
                setBackWater((String) columnValue);
                break;
            case "VDATUM":
                setVdatum((String) columnValue);
                break;
            case "ACTION_FLOW":
                setActionFlow(getDoubleColumnValue(columnValue));
                break;
            case "WSTG":
                setWstg(getDoubleColumnValue(columnValue));
                break;
            case "ZD":
                setZd(getDoubleColumnValue(columnValue));
                break;
            case "RATEDAT":
                java.util.Date timestampDate = (java.util.Date) columnValue;
                if (timestampDate != null) {
                    setRatedAt(timestampDate.getTime());
                }
                break;
            case "USGS_RATENUM":
                setUsgsRateNum((String) columnValue);
                break;
            case "UHGDUR":
                setUhgdur(getIntegerColumnValue(columnValue));
                break;
            case "USE_LATEST_FCST":
                setUseLatestFcst((String) columnValue);
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

    public String getPrimaryPe() {
        return (this.primaryPe);
    }

    public void setPrimaryPe(String primaryPe) {
        this.primaryPe = primaryPe;
    }

    public Double getBf() {
        return (this.bf);
    }

    public void setBf(Double bf) {
        this.bf = bf;
    }

    public Double getCb() {
        return (this.cb);
    }

    public void setCb(Double cb) {
        this.cb = cb;
    }

    public Double getDa() {
        return (this.da);
    }

    public void setDa(Double da) {
        this.da = da;
    }

    public Double getResponseTime() {
        return (this.responseTime);
    }

    public void setResponseTime(Double responseTime) {
        this.responseTime = responseTime;
    }

    public Double getThresholdRunoff() {
        return (this.thresholdRunoff);
    }

    public void setThresholdRunoff(Double thresholdRunoff) {
        this.thresholdRunoff = thresholdRunoff;
    }

    public Double getFq() {
        return (this.fq);
    }

    public void setFq(Double fq) {
        this.fq = fq;
    }

    public Double getFs() {
        return (this.fs);
    }

    public void setFs(Double fs) {
        this.fs = fs;
    }

    public String getGsno() {
        return (this.gsno);
    }

    public void setGsno(String gsno) {
        this.gsno = gsno;
    }

    public String getLevel() {
        return (this.level);
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public Double getMile() {
        return (this.mile);
    }

    public void setMile(Double mile) {
        this.mile = mile;
    }

    public Double getPool() {
        return (this.pool);
    }

    public void setPool(Double pool) {
        this.pool = pool;
    }

    public String getPor() {
        return (this.por);
    }

    public void setPor(String por) {
        this.por = por;
    }

    public String getRated() {
        return (this.rated);
    }

    public void setRated(String rated) {
        this.rated = rated;
    }

    public Double getLat() {
        return (this.lat);
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLon() {
        return (this.lon);
    }

    public void setLon(Double lon) {
        this.lon = lon;
    }

    public String getRemark() {
        return (this.remark);
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getRrevise() {
        return (this.rrevise);
    }

    public void setRrevise(String rrevise) {
        this.rrevise = rrevise;
    }

    public String getRsource() {
        return (this.rsource);
    }

    public void setRsource(String rsource) {
        this.rsource = rsource;
    }

    public String getStream() {
        return (this.stream);
    }

    public void setStream(String stream) {
        this.stream = stream;
    }

    public String getTide() {
        return (this.tide);
    }

    public void setTide(String tide) {
        this.tide = tide;
    }

    public String getBackWater() {
        return (this.backwater);
    }

    public void setBackWater(String backwater) {
        this.backwater = backwater;
    }

    public String getVdatum() {
        return (this.vdatum);
    }

    public void setVdatum(String vdatum) {
        this.vdatum = vdatum;
    }

    public Double getActionFlow() {
        return (this.actionFlow);
    }

    public void setActionFlow(Double actionFlow) {
        this.actionFlow = actionFlow;
    }

    public Double getWstg() {
        return (this.wstg);
    }

    public void setWstg(Double wstg) {
        this.wstg = wstg;
    }

    public Double getZd() {
        return (this.zd);
    }

    public void setZd(Double zd) {
        this.zd = zd;
    }

    public Long getRatedAt() {
        return (this.ratedAt);
    }

    public void setRatedAt(Long ratedAt) {
        this.ratedAt = ratedAt;
    }

    public String getUsgsRateNum() {
        return (this.usgsRateNum);
    }

    public void setUsgsRateNum(String usgsRateNum) {
        this.usgsRateNum = usgsRateNum;
    }

    public Integer getUhgdur() {
        return (this.uhgdur);
    }

    public void setUhgdur(Integer uhgdur) {
        this.uhgdur = uhgdur;
    }

    public String getUseLatestFcst() {
        return (this.useLatestFcst);
    }

    public void setUseLatestFcst(String useLatestFcst) {
        this.useLatestFcst = useLatestFcst;
    }

}

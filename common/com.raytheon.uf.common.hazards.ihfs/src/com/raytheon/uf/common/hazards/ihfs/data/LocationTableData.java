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
import com.raytheon.uf.common.hazards.ihfs.table.LocationQueryTable;

/**
 * This class is used to contain all column data for a ihfs.LOCATION table Row.
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
public class LocationTableData extends AbstractTableData {

    private static final long serialVersionUID = 2383261710741350521L;

    protected String lid;

    protected String county;

    protected String coe;

    protected String cpm;

    protected String detail;

    protected Double elev;

    protected String hdatum;

    protected String hsa;

    protected String hu;

    protected Double lat;

    protected Double lon;

    protected String lremark;

    protected Long lrevise;

    protected String name;

    protected String network;

    protected String rb;

    protected String rfc;

    protected Long sbd;

    protected String sn;

    protected String state;

    protected String waro;

    protected String wfo;

    protected String wsfo;

    protected String type;

    protected String des;

    protected String det;

    protected Integer post;

    protected String stnType;

    protected String tzone;

    public LocationTableData() {
        super(LocationQueryTable.getInstance());
    }

    public LocationTableData(Object[] tableData) throws IhfsDatabaseException {
        super(LocationQueryTable.getInstance(), tableData);
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
            case "COUNTY":
                return (getCounty());
            case "COE":
                return (getCoe());
            case "CPM":
                return (getCpm());
            case "DETAIL":
                return (getDetail());
            case "ELEV":
                return (getElev());
            case "HDATUM":
                return (getHdatum());
            case "HSA":
                return (getHsa());
            case "HU":
                return (getHu());
            case "LAT":
                return (getLat());
            case "LON":
                return (getLon());
            case "LREMARK":
                return (getLremark());
            case "LREVISE":
                return (getLrevise());
            case "NAME":
                return (getName());
            case "NETWORK":
                return (getNetwork());
            case "RB":
                return (getRb());
            case "RFC":
                return (getRfc());
            case "SBD":
                return (getSbd());
            case "STATE":
                return (getState());
            case "WARO":
                return (getWaro());
            case "WFO":
                return (getWfo());
            case "WSFO":
                return (getWsfo());
            case "TYPE":
                return (getType());
            case "DES":
                return (getDes());
            case "DET":
                return (getDet());
            case "POST":
                return (getPost());
            case "STNTYPE":
                return (getStnType());
            case "TZONE":
                return (getTzone());
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

            java.util.Date timestampDate = null;
            switch (columnName) {
            case "LID":
                setLid((String) columnValue);
                break;
            case "COUNTY":
                setCounty((String) columnValue);
                break;
            case "COE":
                setCoe((String) columnValue);
                break;
            case "CPM":
                setCpm((String) columnValue);
                break;
            case "DETAIL":
                setDetail((String) columnValue);
                break;
            case "ELEV":
                setElev(getDoubleColumnValue(columnValue));
                break;
            case "HDATUM":
                setHdatum((String) columnValue);
                break;
            case "HSA":
                setHsa((String) columnValue);
                break;
            case "HU":
                setHu((String) columnValue);
                break;
            case "LAT":
                setLat(getDoubleColumnValue(columnValue));
                break;
            case "LON":
                setLon(getDoubleColumnValue(columnValue));
                break;
            case "LREMARK":
                setLremark((String) columnValue);
                break;
            case "LREVISE":
                timestampDate = (java.util.Date) columnValue;
                if (timestampDate != null) {
                    setLrevise(timestampDate.getTime());
                }
                break;
            case "NAME":
                setName((String) columnValue);
                break;
            case "NETWORK":
                setNetwork((String) columnValue);
                break;
            case "RB":
                setRb((String) columnValue);
                break;
            case "RFC":
                setRfc((String) columnValue);
                break;
            case "SBD":
                timestampDate = (java.util.Date) columnValue;
                if (timestampDate != null) {
                    setSbd(timestampDate.getTime());
                }
                break;
            case "SN":
                setSn((String) columnValue);
                break;
            case "STATE":
                setState((String) columnValue);
                break;
            case "WARO":
                setWaro((String) columnValue);
                break;
            case "WFO":
                setWfo((String) columnValue);
                break;
            case "WSFO":
                setWsfo((String) columnValue);
                break;
            case "TYPE":
                setType((String) columnValue);
                break;
            case "DES":
                setDes((String) columnValue);
                break;
            case "DET":
                setDet((String) columnValue);
                break;
            case "POST":
                setPost(getIntegerColumnValue(columnValue));
                break;
            case "STNTYPE":
                setStnType((String) columnValue);
                break;
            case "TZONE":
                setTzone((String) columnValue);
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

    public String getCounty() {
        return (this.county);
    }

    public void setCounty(String county) {
        this.county = county;
    }

    public String getCoe() {
        return (this.coe);
    }

    public void setCoe(String coe) {
        this.coe = coe;
    }

    public String getCpm() {
        return (this.cpm);
    }

    public void setCpm(String cpm) {
        this.cpm = cpm;
    }

    public String getDetail() {
        return (this.detail);
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public Double getElev() {
        return (this.elev);
    }

    public void setElev(Double elev) {
        this.elev = elev;
    }

    public String getHdatum() {
        return (this.hdatum);
    }

    public void setHdatum(String hdatum) {
        this.hdatum = hdatum;
    }

    public String getHsa() {
        return (this.hsa);
    }

    public void setHsa(String hsa) {
        this.hsa = hsa;
    }

    public String getHu() {
        return (this.hu);
    }

    public void setHu(String hu) {
        this.hu = hu;
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

    public String getLremark() {
        return (this.lremark);
    }

    public void setLremark(String lremark) {
        this.lremark = lremark;
    }

    public Long getLrevise() {
        return (this.lrevise);
    }

    public void setLrevise(Long lrevise) {
        this.lrevise = lrevise;
    }

    public String getName() {
        return (this.name);
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNetwork() {
        return (this.network);
    }

    public void setNetwork(String network) {
        this.network = network;
    }

    public String getRb() {
        return (this.rb);
    }

    public void setRb(String rb) {
        this.rb = rb;
    }

    public String getRfc() {
        return (this.rfc);
    }

    public void setRfc(String rfc) {
        this.rfc = rfc;
    }

    public Long getSbd() {
        return (this.sbd);
    }

    public void setSbd(Long sbd) {
        this.sbd = sbd;
    }

    public String getSn() {
        return (this.sn);
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public String getState() {
        return (this.state);
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getWaro() {
        return (this.waro);
    }

    public void setWaro(String waro) {
        this.waro = waro;
    }

    public String getWfo() {
        return (this.wfo);
    }

    public void setWfo(String wfo) {
        this.wfo = wfo;
    }

    public String getWsfo() {
        return (this.wsfo);
    }

    public void setWsfo(String wsfo) {
        this.wsfo = wsfo;
    }

    public String getType() {
        return (this.type);
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDes() {
        return (this.des);
    }

    public void setDes(String des) {
        this.des = des;
    }

    public String getDet() {
        return (this.det);
    }

    public void setDet(String det) {
        this.det = det;
    }

    public Integer getPost() {
        return (this.post);
    }

    public void setPost(Integer post) {
        this.post = post;
    }

    public String getStnType() {
        return (this.lid);
    }

    public void setStnType(String stnType) {
        this.stnType = stnType;
    }

    public String getTzone() {
        return (this.tzone);
    }

    public void setTzone(String tzone) {
        this.tzone = tzone;
    }

}

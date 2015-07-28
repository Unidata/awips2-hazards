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
import com.raytheon.uf.common.hazards.ihfs.table.FpInfoQueryTable;

/**
 * This class is used to contain all column data for a ihfs.FPINFO table Row.
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
public class FpInfoTableData extends AbstractTableData {

    private static final long serialVersionUID = 4805571852460303219L;

    protected String lid;

    protected String name;

    protected String county;

    protected String state;

    protected String hsa;

    protected String primaryBack;

    protected String secondaryBack;

    protected String stream;

    protected Double bf;

    protected Double wstg;

    protected Double fs;

    protected Double fq;

    protected Double actionFlow;

    protected String pe;

    protected String useLatestFcst;

    protected String proximity;

    protected String reach;

    protected String groupId;

    protected Integer ordinal;

    protected Double chgThreshold;

    protected String recType;

    protected Integer backHrs;

    protected Integer forwardHrs;

    protected Double adjustEndHrs;

    protected Double minorStage;

    protected Double moderateStage;

    protected Double majorStage;

    protected Double minorFlow;

    protected Double moderateFlow;

    protected Double majorFlow;

    public FpInfoTableData() {
        super(FpInfoQueryTable.getInstance());
    }

    public FpInfoTableData(Object[] tableData) throws IhfsDatabaseException {
        super(FpInfoQueryTable.getInstance(), tableData);
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
            case "NAME":
                return (getName());
            case "COUNTY":
                return (getCounty());
            case "STATE":
                return (getState());
            case "HSA":
                return (getHsa());
            case "PRIMARY_BACK":
                return (getPrimaryBack());
            case "SECONDARY_BACK":
                return (getSecondaryBack());
            case "STREAM":
                return (getStream());
            case "BF":
                return (getBf());
            case "WSTG":
                return (getWstg());
            case "FQ":
                return (getFq());
            case "ACTION_FLOW":
                return (getActionFlow());
            case "PE":
                return (getPe());
            case "USE_LATEST_FCST":
                return (getUseLatestFcst());
            case "PROXIMITY":
                return (getProximity());
            case "REACH":
                return (getReach());
            case "GROUP_ID":
                return (getGroupId());
            case "ORDINAL":
                return (getOrdinal());
            case "CHG_THRESHOLD":
                return (getChgThreshold());
            case "REC_TYPE":
                return (getRecType());
            case "BACKHRS":
                return (getBackHrs());
            case "FORWARDHRS":
                return (getForwardHrs());
            case "ADJUSTENDHRS":
                return (getAdjustEndHrs());
            case "MINOR_STAGE":
                return (getMinorStage());
            case "MODERATE_STAGE":
                return (getModerateStage());
            case "MAJOR_STAGE":
                return (getMajorStage());
            case "MINOR_FLOW":
                return (getMinorFlow());
            case "MODERATE_FLOW":
                return (getModerateFlow());
            case "MAJOR_FLOW":
                return (getMajorFlow());
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
            case "NAME":
                setName((String) columnValue);
                break;
            case "COUNTY":
                setCounty((String) columnValue);
                break;
            case "STATE":
                setState((String) columnValue);
                break;
            case "HSA":
                setHsa((String) columnValue);
                break;
            case "PRIMARY_BACK":
                setPrimaryBack((String) columnValue);
                break;
            case "SECONDARY_BACK":
                setSecondaryBack((String) columnValue);
                break;
            case "STREAM":
                setStream((String) columnValue);
                break;
            case "BF":
                setBf(getDoubleColumnValue(columnValue));
                break;
            case "WSTG":
                setWstg(getDoubleColumnValue(columnValue));
                break;
            case "FS":
                setFs(getDoubleColumnValue(columnValue));
                break;
            case "FQ":
                setFq(getDoubleColumnValue(columnValue));
                break;
            case "ACTION_FLOW":
                setActionFlow(getDoubleColumnValue(columnValue));
                break;
            case "PE":
                setPe((String) columnValue);
                break;
            case "USE_LATEST_FCST":
                setUseLatestFcst((String) columnValue);
                break;
            case "PROXIMITY":
                setProximity((String) columnValue);
                break;
            case "REACH":
                setReach((String) columnValue);
                break;
            case "GROUP_ID":
                setGroupId((String) columnValue);
                break;
            case "ORDINAL":
                setOrdinal(getIntegerColumnValue(columnValue));
                break;
            case "CHG_THRESHOLD":
                setChgThreshold(getDoubleColumnValue(columnValue));
                break;
            case "REC_TYPE":
                setRecType((String) columnValue);
                break;
            case "BACKHRS":
                setBackHrs(getIntegerColumnValue(columnValue));
                break;
            case "FORWARDHRS":
                setForwardHrs(getIntegerColumnValue(columnValue));
                break;
            case "ADJUSTENDHRS":
                setAdjustEndHrs(getDoubleColumnValue(columnValue));
                break;
            case "MINOR_STAGE":
                setMinorStage(getDoubleColumnValue(columnValue));
                break;
            case "MODERATE_STAGE":
                setModerateStage(getDoubleColumnValue(columnValue));
                break;
            case "MAJOR_STAGE":
                setMajorStage(getDoubleColumnValue(columnValue));
                break;
            case "MINOR_FLOW":
                setMinorFlow(getDoubleColumnValue(columnValue));
                break;
            case "MODERATE_FLOW":
                setModerateFlow(getDoubleColumnValue(columnValue));
                break;
            case "MAJOR_FLOW":
                setMajorFlow(getDoubleColumnValue(columnValue));
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

    public String getName() {
        return (this.name);
    }

    public void setName(String name) {
        this.name = name;
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

    public String getHsa() {
        return (this.hsa);
    }

    public void setHsa(String hsa) {
        this.hsa = hsa;
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

    public String getStream() {
        return (this.stream);
    }

    public void setStream(String stream) {
        this.stream = stream;
    }

    public Double getBf() {
        return (this.bf);
    }

    public void setBf(Double bf) {
        this.bf = bf;
    }

    public Double getWstg() {
        return (this.wstg);
    }

    public void setWstg(Double wstg) {
        this.wstg = wstg;
    }

    public Double getFs() {
        return (this.fs);
    }

    public void setFs(Double fs) {
        this.fs = fs;
    }

    public Double getFq() {
        return (this.fq);
    }

    public void setFq(Double fq) {
        this.fq = fq;
    }

    public Double getActionFlow() {
        return (this.actionFlow);
    }

    public void setActionFlow(Double actionFlow) {
        this.actionFlow = actionFlow;
    }

    public String getPe() {
        return (this.pe);
    }

    public void setPe(String pe) {
        this.pe = pe;
    }

    public String getUseLatestFcst() {
        return (this.useLatestFcst);
    }

    public void setUseLatestFcst(String useLatestFcst) {
        this.useLatestFcst = useLatestFcst;
    }

    public String getProximity() {
        return (this.useLatestFcst);
    }

    public void setProximity(String proximity) {
        this.proximity = proximity;
    }

    public String getReach() {
        return (this.reach);
    }

    public void setReach(String reach) {
        this.reach = reach;
    }

    public String getGroupId() {
        return (this.groupId);
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public Integer getOrdinal() {
        return (this.ordinal);
    }

    public void setOrdinal(Integer ordinal) {
        this.ordinal = ordinal;
    }

    public Double getChgThreshold() {
        return (this.chgThreshold);
    }

    public void setChgThreshold(Double chgThreshold) {
        this.chgThreshold = chgThreshold;
    }

    public String getRecType() {
        return (this.recType);
    }

    public void setRecType(String recType) {
        this.recType = recType;
    }

    public Integer getBackHrs() {
        return (this.backHrs);
    }

    public void setBackHrs(Integer backHrs) {
        this.backHrs = backHrs;
    }

    public Integer getForwardHrs() {
        return (this.forwardHrs);
    }

    public void setForwardHrs(Integer forwardHrs) {
        this.forwardHrs = forwardHrs;
    }

    public Double getAdjustEndHrs() {
        return (this.adjustEndHrs);
    }

    public void setAdjustEndHrs(Double adjustEndHrs) {
        this.adjustEndHrs = adjustEndHrs;
    }

    public Double getMinorStage() {
        return (this.minorStage);
    }

    public void setMinorStage(Double minorStage) {
        this.minorStage = minorStage;
    }

    public Double getModerateStage() {
        return (this.moderateStage);
    }

    public void setModerateStage(Double moderateStage) {
        this.moderateStage = moderateStage;
    }

    public Double getMajorStage() {
        return (this.majorStage);
    }

    public void setMajorStage(Double majorStage) {
        this.majorStage = majorStage;
    }

    public Double getMinorFlow() {
        return (this.minorFlow);
    }

    public void setMinorFlow(Double minorFlow) {
        this.minorFlow = minorFlow;
    }

    public Double getModerateFlow() {
        return (this.moderateFlow);
    }

    public void setModerateFlow(Double moderateFlow) {
        this.moderateFlow = moderateFlow;
    }

    public Double getMajorFlow() {
        return (this.majorFlow);
    }

    public void setMajorFlow(Double majorFlow) {
        this.majorFlow = majorFlow;
    }

}

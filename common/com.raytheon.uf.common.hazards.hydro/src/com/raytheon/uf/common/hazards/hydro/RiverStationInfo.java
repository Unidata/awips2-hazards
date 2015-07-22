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
 **/
package com.raytheon.uf.common.hazards.hydro;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

/**
 * This class represents the River Station Info (RIVERSTAT) information for a
 * river forecast point.
 * 
 * This is a Data-Only object.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 08, 2015 6562       Chris.Cody  Initial creation: Restructure River Forecast Points/Recommender
 * Jul 06, 2015 9155       Chris.Cody  RiverStationInfo fields must match RiverStat table Columns
 * Jul 22, 2015 9670       Chris.Cody  Changes for Base database query result numeric casting
 * </pre>
 * 
 * @author Chris.Cody
 */

public class RiverStationInfo {
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(RiverStationInfo.class);

    public static final String TABLE_NAME = "RiverStat";

    public static final String COLUMN_NAME_STRING = "lid, primary_pe, bf, cb, da, response_time, "
            + "threshold_runoff, fq, fs, gsno, level, mile, pool, "
            + "por, rated, lat, lon, remark, rrevise, rsource, stream, "
            + "tide, backwater, vdatum, action_flow, wstg, zd, ratedat, "
            + "usgs_ratenum, uhgdur, use_latest_fcst";

    private final int LID_FIELD_IDX = 0;

    private final int PRIMARY_PE_FIELD_IDX = 1;

    private final int BF_FIELD_IDX = 2;

    private final int CB_FIELD_IDX = 3;

    private final int DA_FIELD_IDX = 4;

    private final int RESPONSE_TIME_FIELD_IDX = 5;

    private final int THRESHOLD_RUNNOFF_FIELD_IDX = 6;

    private final int FQ_FIELD_IDX = 7;

    private final int FS_FIELD_IDX = 8;

    private final int GSNO_FIELD_IDX = 9;

    private final int LEVEL_FIELD_IDX = 10;

    private final int MILE_FIELD_IDX = 11;

    private final int POOL_FIELD_IDX = 12;

    private final int POR_FIELD_IDX = 13;

    private final int RATED_FIELD_IDX = 14;

    private final int LAT_FIELD_IDX = 15;

    private final int LON_FIELD_IDX = 16;

    private final int REMARK_FIELD_IDX = 17;

    private final int RREVISE_FIELD_IDX = 18;

    private final int RSOURCE_FIELD_IDX = 19;

    private final int STREAM_FIELD_IDX = 20;

    private final int TIDE_FIELD_IDX = 21;

    private final int BACKWATER_FIELD_IDX = 22;

    private final int VDATUM_FIELD_IDX = 23;

    private final int ACTION_FLOW_FIELD_IDX = 24;

    private final int WSTG_FIELD_IDX = 25;

    private final int ZD_FIELD_IDX = 26;

    private final int RATEDAT_FIELD_IDX = 27;

    private final int USGS_RATENUM_FIELD_IDX = 28;

    private final int UHGDUR_FIELD_IDX = 29;

    private final int USE_LATEST_FCST_FIELD_IDX = 30;

    /**
     * River station Forecast Point identifier.
     */
    private String lid;

    /**
     * River station Primary Physical Element.
     */
    private String primary_pe;

    /**
     * River station Bank Full (BF).
     */
    private double bankFull;

    /**
     * River station (CB).
     */
    private double cb;

    /**
     * River station (DA).
     */
    private double da;

    /**
     * River station Response Time.
     */
    private double responseTime;

    /**
     * River station Threshold Runoff.
     */
    private double thresholdRunnoff;

    /**
     * River station floodFlow (FQ).
     */
    private double floodFlow;

    /**
     * River station Flood stage (FS).
     */
    private double floodStage;

    /**
     * River station (GSNO)
     */
    private String gsno;

    /**
     * River station (LEVEL)
     */
    private String level;

    /**
     * River station (MILE)
     */
    private double mile;

    /**
     * River station (POOL)
     */
    private double pool;

    /**
     * River station (POR)
     */
    private String por;

    /**
     * River station (RATED)
     */
    private String rated;

    /**
     * River station location Latitude (LAT)
     */
    private double lat;

    /**
     * River station location Longitude (LON)
     */
    private double lon;

    /**
     * River station (REMARK)
     */
    private String remark;

    /**
     * River station Revision Date (RREVISE)
     */
    private long rrevise;

    /**
     * River station (RSOURCE)
     */
    private String rsource;

    /**
     * River station (STREAM)
     */
    private String stream;

    /**
     * River station (TIDE)
     */
    private String tide;

    /**
     * River station (BACKWATER)
     */
    private String backwater;

    /**
     * River station (VDATUM)
     */
    private String vdatum;

    /**
     * River station (ACTION_FLOW)
     */
    private Double actionFlow;

    /**
     * River station (WSTG)
     */
    private double actionStage;

    /**
     * River station (ZD)
     */
    private double zd;

    /**
     * River station Rate Date (RATEDAT)
     */
    private long rateDate;

    /**
     * River station (USGS_RATENUM)
     */
    private String usgsRateNum;

    /**
     * River station (UHGDUR)
     */
    private int uhgdur;

    /**
     * River station Use Latest Forecast (USE_LATEST_FCST)
     */
    private boolean useLatestFcst;

    /**
     * Default Constructor
     * 
     */
    public RiverStationInfo() {
    }

    /**
     * Creates a river forecast station object
     * 
     * @param queryResult
     *            Object Array of Query Result Data
     */
    public RiverStationInfo(Object[] queryResult) {
        if (queryResult != null) {
            int queryResultSize = queryResult.length;
            java.sql.Date sqlDate = null;
            Object queryValue = null;
            for (int i = 0; i < queryResultSize; i++) {
                queryValue = queryResult[i];
                if (queryValue == null) {
                    continue;
                }
                switch (i) {
                case LID_FIELD_IDX:
                    this.lid = (String) queryValue;
                    break;
                case PRIMARY_PE_FIELD_IDX:
                    this.primary_pe = (String) queryValue;
                    break;
                case BF_FIELD_IDX:
                    this.bankFull = ((Number) queryValue).doubleValue();
                    break;
                case CB_FIELD_IDX:
                    this.cb = ((Number) queryValue).doubleValue();
                    break;
                case DA_FIELD_IDX:
                    this.da = ((Number) queryValue).doubleValue();
                    break;
                case RESPONSE_TIME_FIELD_IDX:
                    this.responseTime = ((Number) queryValue).doubleValue();
                    break;
                case THRESHOLD_RUNNOFF_FIELD_IDX:
                    this.thresholdRunnoff = ((Number) queryValue).doubleValue();
                    break;
                case FQ_FIELD_IDX:
                    this.floodFlow = ((Number) queryValue).doubleValue();
                    break;
                case FS_FIELD_IDX:
                    this.floodStage = ((Number) queryValue).doubleValue();
                    break;
                case GSNO_FIELD_IDX:
                    this.gsno = (String) queryValue;
                    break;
                case LEVEL_FIELD_IDX:
                    this.level = (String) queryValue;
                    break;
                case MILE_FIELD_IDX:
                    this.mile = ((Number) queryValue).doubleValue();
                    break;
                case POOL_FIELD_IDX:
                    this.pool = ((Number) queryValue).doubleValue();
                    break;
                case POR_FIELD_IDX:
                    this.por = (String) queryValue;
                    break;
                case RATED_FIELD_IDX:
                    this.rated = (String) queryValue;
                    break;
                case LAT_FIELD_IDX:
                    this.lat = ((Number) queryValue).doubleValue();
                    break;
                case LON_FIELD_IDX:
                    this.lon = ((Number) queryValue).doubleValue();
                    break;
                case REMARK_FIELD_IDX:
                    this.remark = (String) queryValue;
                    break;
                case RREVISE_FIELD_IDX:
                    sqlDate = (java.sql.Date) queryValue;
                    this.rrevise = sqlDate.getTime();
                    break;
                case RSOURCE_FIELD_IDX:
                    this.rsource = (String) queryValue;
                    break;
                case STREAM_FIELD_IDX:
                    this.stream = (String) queryValue;
                    break;
                case TIDE_FIELD_IDX:
                    this.tide = (String) queryValue;
                    break;
                case BACKWATER_FIELD_IDX:
                    this.backwater = (String) queryValue;
                    break;
                case VDATUM_FIELD_IDX:
                    this.vdatum = (String) queryValue;
                    break;
                case ACTION_FLOW_FIELD_IDX:
                    this.actionFlow = ((Number) queryValue).doubleValue();
                    break;
                case WSTG_FIELD_IDX:
                    this.actionStage = ((Number) queryValue).doubleValue();
                    break;
                case ZD_FIELD_IDX:
                    this.zd = ((Number) queryValue).doubleValue();
                    break;
                case RATEDAT_FIELD_IDX:
                    sqlDate = (java.sql.Date) queryValue;
                    this.rateDate = sqlDate.getTime();
                    break;
                case USGS_RATENUM_FIELD_IDX:
                    this.usgsRateNum = (String) queryValue;
                    break;
                case UHGDUR_FIELD_IDX:
                    this.uhgdur = ((Number) queryValue).intValue();
                    break;
                case USE_LATEST_FCST_FIELD_IDX:
                    if ("T".equals((String) queryValue)) {
                        this.useLatestFcst = true;
                    } else {
                        this.useLatestFcst = false;
                    }
                    break;
                default:
                    statusHandler
                            .error("RiverStationInfo Constructor array out of sync with number of data fields. Unknown field for value "
                                    + (String) queryValue);
                }
            }
        }
    }

    /**
     * Get River Forecast LID
     * 
     * @return the identifier of this forecast point
     */
    public String getLid() {
        return lid;
    }

    /**
     * Get River station Primary Physical Element
     */
    public String getPrimary_pe() {
        return (this.primary_pe);
    }

    /**
     * Get River station Bank Full (BF)
     */
    public double getBankFull() {
        return (this.bankFull);
    }

    /**
     * Get River station (CB)
     */
    public double getcb() {
        return (this.cb);
    }

    /**
     * Get River station (DA)
     */
    public double getDa() {
        return (this.da);
    }

    /**
     * Get River station Response Time
     */
    public double getResponseTime() {
        return (this.responseTime);
    }

    /**
     * Get River station Threshold Runoff
     */
    public double getThresholdRunnoff() {
        return (this.thresholdRunnoff);
    }

    /**
     * Get River station floodFlow (FQ)
     */
    public double getFloodFlow() {
        return (this.floodFlow);
    }

    /**
     * Get River station Flood stage (FS)
     */
    public double getfloodStage() {
        return (this.floodStage);
    }

    /**
     * Get River station (GSNO)
     */
    public String getGsno() {
        return (this.gsno);
    }

    /**
     * Get River station (LEVEL)
     */
    public String getLevel() {
        return (this.level);
    }

    /**
     * Get River station (MILE)
     */
    public double getMile() {
        return (this.mile);
    }

    /**
     * Get River station (POOL)
     */
    public double getPool() {
        return (this.pool);
    }

    /**
     * Get River station (POR)
     */
    public String getPpor() {
        return (this.por);
    }

    /**
     * Get River station (RATED)
     */
    public String getRated() {
        return (this.rated);
    }

    /**
     * Get River station location Latitude (LAT)
     */
    public double getLat() {
        return (this.lat);
    }

    /**
     * Get River station location Longitude (LON)
     */
    public double getLon() {
        return (this.lon);
    }

    /**
     * Get River station (REMARK)
     */
    public String getRemark() {
        return (this.remark);
    }

    /**
     * Get River station Revision Date (RREVISE)
     */
    public long getRrevise() {
        return (this.rrevise);
    }

    /**
     * Get River station (RSOURCE)
     */
    public String getRsource() {
        return (this.rsource);
    }

    /**
     * Get River station (STREAM)
     */
    public String getStream() {
        return (this.stream);
    }

    /**
     * Get River station (TIDE)
     */
    public String getTide() {
        return (this.tide);
    }

    /**
     * Get River station (BACKWATER)
     */
    public String getBackwater() {
        return (this.backwater);
    }

    /**
     * Get River station (VDATUM)
     */
    public String getVdatum() {
        return (this.vdatum);
    }

    /**
     * Get River station (ACTION_FLOW)
     */
    public Double actionFlow() {
        return (this.actionFlow);
    }

    /**
     * Get River station (WSTG)
     */
    public double getActionStage() {
        return (this.actionStage);
    }

    /**
     * Get River station (ZD)
     */
    public double getZd() {
        return (this.zd);
    }

    /**
     * Get River station Rate Date (RATEDAT)
     */
    public long getRateDate() {
        return (this.rateDate);
    }

    /**
     * Get River station (USGS_RATENUM)
     */
    public String getUsgsRateNum() {
        return (this.usgsRateNum);
    }

    /**
     * Get River station (UHGDUR)
     */
    public int getUhgdur() {
        return (this.uhgdur);
    }

    /**
     * Get River station Use Latest Forecast (USE_LATEST_FCST)
     */
    public boolean getUseLatestFcst() {
        return (this.useLatestFcst);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}

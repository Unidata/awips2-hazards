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

import com.raytheon.uf.common.hazards.hydro.RiverHydroConstants.HydroFloodCategories;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.vividsolutions.jts.geom.Coordinate;

/**
 * This class represents a river forecast point it contains only data from the
 * FpInfo (FPINFO) View table. It is sub-classed by the RiverForecastPoint data
 * object.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 08, 2015 6562       Chris.Cody  Initial creation: Restructure River Forecast Points/Recommender
 * Jul 22, 2015 9670       Chris.Cody  Changes for Base database query result numeric casting
 * Feb 11, 2016 14796      mduff       Add toString().
 * May 04, 2016 15584      Kevin.Bisanz Rename MAJOR_FLOW, update toString().
 * </pre>
 * 
 * @author Chris.Cody
 */

public class FpInfo {
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(FpInfo.class);

    public static final String TABLE_NAME = "FpInfo"; // Technically a View

    public static final String COLUMN_NAME_STRING = "lid, name, county, state, "
            + "hsa, primary_back, secondary_back, stream, "
            + "bf, wstg, fs, fq, action_flow, pe, "
            + "use_latest_fcst, proximity, reach, group_id, "
            + "ordinal, chg_threshold, rec_type, "
            + "backhrs, forwardhrs, adjustendhrs, "
            + "minor_stage, moderate_stage, major_stage, "
            + "minor_flow, moderate_flow, major_flow ";

    private final int LID_FIELD_IDX = 0;

    private final int NAME_FIELD_IDX = 1;

    private final int COUNTY_FIELD_IDX = 2;

    private final int STATE_FIELD_IDX = 3;

    private final int HSA_FIELD_IDX = 4;

    private final int PRIMARY_BACK_FIELD_IDX = 5;

    private final int SECONDARY_BACK_FIELD_IDX = 6;

    private final int STREAM_FIELD_IDX = 7;

    private final int BF_FIELD_IDX = 8;

    private final int WSTG_FIELD_IDX = 9;

    private final int FS_FIELD_IDX = 10;

    private final int FQ_FIELD_IDX = 11;

    private final int ACTION_FLOW_FIELD_IDX = 12;

    private final int PE_FIELD_IDX = 13;

    private final int USE_LATEST_FCST_FIELD_IDX = 14;

    private final int PROXIMITY_FIELD_IDX = 15;

    private final int REACH_FIELD_IDX = 16;

    private final int GROUP_ID_FIELD_IDX = 17;

    private final int ORDINAL_FIELD_IDX = 18;

    private final int CHG_THRESHOLD_FIELD_IDX = 19;

    private final int REC_TYPE_FIELD_IDX = 20;

    private final int BACK_HRS_FIELD_IDX = 21;

    private final int FORWARD_HRS_FIELD_IDX = 22;

    private final int ADJUST_END_HRS_FIELD_IDX = 23;

    private final int MINOR_STAGE_FIELD_IDX = 24;

    private final int MODERATE_STAGE_FIELD_IDX = 25;

    private final int MAJOR_STAGE_FIELD_IDX = 26;

    private final int MINOR_FLOW_FIELD_IDX = 27;

    private final int MODERATE_FLOW_FIELD_IDX = 28;

    private final int MAJOR_FLOW_FIELD_IDX = 29;

    /**
     * River station identifier
     */
    private String lid;

    /**
     * River station name
     */
    private String name;

    /**
     * River station county
     */
    private String county;

    /**
     * River station state
     */
    private String state;

    /**
     * The Hydrologic Service Area this point is in. (HSA)
     */
    private String hsa;

    /**
     * Primary backup office responsible for this point (PRIMARY_BACK)
     */
    private String primaryBackup;

    /**
     * Secondary backup office responsible for this point (SECONDARY_BACK)
     */
    private String secondaryBackup;

    /**
     * River station stream (STREAM)
     */
    private String stream;

    /**
     * Bankfull stage (BF)
     */
    private double bankFull = RiverHydroConstants.MISSING_VALUE_DOUBLE;

    /**
     * Action stage (WSTG)
     */
    private double actionStage = RiverHydroConstants.MISSING_VALUE_DOUBLE;

    /**
     * Flood stage (FS)
     */
    private double floodStage = RiverHydroConstants.MISSING_VALUE_DOUBLE;

    /**
     * Flood flow (FQ)
     */
    private double floodFlow = RiverHydroConstants.MISSING_VALUE_DOUBLE;

    /**
     * Action flow (ACTION_FLOW)
     */
    private double actionFlow = RiverHydroConstants.MISSING_VALUE_DOUBLE;

    /**
     * Physical element (PE)
     */
    private String physicalElement;

    /**
     * Flag indicating whether or not the latest forecast should be used
     * (USE_LAGEST_FCST)
     */
    private boolean useLatestForecast;

    /**
     * Location this forecast point is close to
     */
    private String proximity;

    /**
     * The forecast point's associated reach on the river
     */
    private String reach;

    /**
     * The river group this point belongs to
     */
    private String groupId;

    private int ordinal;

    /**
     * stage/flow change for non-steady assumption. (CHG_THRESHOLD)
     */
    private double changeThreshold = RiverHydroConstants.MISSING_VALUE_DOUBLE;

    /**
     * Previously used for recommendation type, now it is used to determine
     * output the stage or flow value for impact and historical comparison
     * variables. (REC_TYPE)
     */
    private String recommendationType;

    /**
     * look back hours for observed data (BACKHRS)
     */
    private int backHrs = RiverHydroConstants.MISSING_VALUE;

    /**
     * Look forward hours for forecast data (FORWARDHRS)
     */
    private int forwardHrs = RiverHydroConstants.MISSING_VALUE;

    /**
     * adjusted end hours for PVTEC line. (ADJUSTENDHRS)
     */
    private double adjustEndHrs = RiverHydroConstants.MISSING_VALUE_DOUBLE;

    /**
     * Flood category thresholds Array this forecast point (
     * HydroFloodCategories: NO_FLOOD_CATEGORY, MINOR_FLOOD_CATEGORY,
     * MODERATE_FLOOD_CATEGORY, MAJOR_FLOOD_CATEGORY, RECORD_FLOOD_CATEGORY)
     * 
     * HydroFloodCategories
     */
    private final double floodCategory[] = {
            RiverHydroConstants.MISSING_VALUE_DOUBLE,
            RiverHydroConstants.MISSING_VALUE_DOUBLE,
            RiverHydroConstants.MISSING_VALUE_DOUBLE,
            RiverHydroConstants.MISSING_VALUE_DOUBLE,
            RiverHydroConstants.MISSING_VALUE_DOUBLE };

    private double minorFlow = RiverHydroConstants.MISSING_VALUE_DOUBLE;

    private double moderateFlow = RiverHydroConstants.MISSING_VALUE_DOUBLE;

    private double majorFlow = RiverHydroConstants.MISSING_VALUE_DOUBLE;

    private double minorStage = RiverHydroConstants.MISSING_VALUE_DOUBLE;

    private double moderateStage = RiverHydroConstants.MISSING_VALUE_DOUBLE;

    private double majorStage = RiverHydroConstants.MISSING_VALUE_DOUBLE;

    /**
     * The coordinates of this station's location
     */
    private double latitude;

    private double longitude;

    /**
     * Whether or not this forecast point needs to be included in the hazard
     * recommendation
     */
    private boolean includedInRecommendation;

    /**
     * Default constructor
     */
    public FpInfo() {
    }

    /**
     * Query result constructor.
     * 
     * Called from FloodDAO
     * 
     * @param queryResult
     *            Object Array of Query Result Data
     */
    public FpInfo(Object[] queryResult) {
        if (queryResult != null) {
            int queryResultSize = queryResult.length;
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
                case NAME_FIELD_IDX:
                    this.name = (String) queryValue;
                    break;
                case COUNTY_FIELD_IDX:
                    this.county = (String) queryValue;
                    break;
                case STATE_FIELD_IDX:
                    this.state = (String) queryValue;
                    break;
                case HSA_FIELD_IDX:
                    this.hsa = (String) queryValue;
                    break;
                case PRIMARY_BACK_FIELD_IDX:
                    this.primaryBackup = (String) queryValue;
                    break;
                case SECONDARY_BACK_FIELD_IDX:
                    this.secondaryBackup = (String) queryValue;
                    break;
                case STREAM_FIELD_IDX:
                    this.stream = (String) queryValue;
                    break;
                case BF_FIELD_IDX:
                    this.bankFull = ((Number) queryValue).doubleValue();
                    break;
                case WSTG_FIELD_IDX:
                    this.actionStage = ((Number) queryValue).doubleValue();
                    break;
                case FS_FIELD_IDX:
                    this.floodStage = ((Number) queryValue).doubleValue();
                    break;
                case FQ_FIELD_IDX:
                    this.floodFlow = ((Number) queryValue).doubleValue();
                    break;
                case ACTION_FLOW_FIELD_IDX:
                    this.actionFlow = ((Number) queryValue).doubleValue();
                    break;
                case PE_FIELD_IDX:
                    this.physicalElement = (String) queryValue;
                    break;
                case USE_LATEST_FCST_FIELD_IDX:
                    if ("T".equals(queryValue) == true) {
                        this.useLatestForecast = true;
                    } else {
                        this.useLatestForecast = false;
                    }
                    break;
                case PROXIMITY_FIELD_IDX:
                    this.proximity = (String) queryValue;
                    break;
                case REACH_FIELD_IDX:
                    this.reach = (String) queryValue;
                    break;
                case GROUP_ID_FIELD_IDX:
                    this.groupId = (String) queryValue;
                    break;
                case ORDINAL_FIELD_IDX:
                    this.ordinal = ((Number) queryValue).intValue();
                    break;
                case CHG_THRESHOLD_FIELD_IDX:
                    this.changeThreshold = ((Number) queryValue).doubleValue();
                    break;
                case REC_TYPE_FIELD_IDX:
                    /*
                     * Steal to use the rec_type field in rpffcstpoint. It was
                     * previously used for recommendation type. Now it is used
                     * to determine whether to use stage or flow for the
                     * impact/crest variables. If it is "PE", this means the
                     * value will be based on the primary pe. If it is "NPE",
                     * then it means the opposite.
                     */
                    this.recommendationType = (String) queryValue;
                    break;
                case BACK_HRS_FIELD_IDX:
                    int tempBackHrs = ((Number) queryValue).intValue();
                    if (tempBackHrs > 0) {
                        this.backHrs = tempBackHrs;
                    }
                    break;
                case FORWARD_HRS_FIELD_IDX:
                    int tempForwardHrs = ((Number) queryValue).intValue();
                    if (tempForwardHrs > 0) {
                        this.forwardHrs = tempForwardHrs;
                    }
                    break;
                case ADJUST_END_HRS_FIELD_IDX:
                    double tempAdjustHrs = ((Number) queryValue).doubleValue();
                    if (tempAdjustHrs > 0.0) {
                        this.adjustEndHrs = tempAdjustHrs;
                    }
                    break;
                case MINOR_STAGE_FIELD_IDX:
                    this.minorStage = ((Number) queryValue).doubleValue();
                    break;
                case MODERATE_STAGE_FIELD_IDX:
                    this.moderateStage = ((Number) queryValue).doubleValue();
                    break;
                case MAJOR_STAGE_FIELD_IDX:
                    this.majorStage = ((Number) queryValue).doubleValue();
                    break;
                case MINOR_FLOW_FIELD_IDX:
                    this.minorFlow = ((Number) queryValue).doubleValue();
                    break;
                case MODERATE_FLOW_FIELD_IDX:
                    this.moderateFlow = ((Number) queryValue).doubleValue();
                    break;
                case MAJOR_FLOW_FIELD_IDX:
                    this.majorFlow = ((Number) queryValue).doubleValue();
                    break;
                default:
                    statusHandler
                            .error("RiverPointZoneInfo Constructor array out of sync with number of data fields. Unknown field for value "
                                    + (String) queryValue);
                }
            }

            init();
        }
    }

    private void init() {
        if (this.physicalElement != null) {
            if (this.physicalElement.startsWith("Q") == false) {
                if (this.minorStage != RiverHydroConstants.MISSING_VALUE_DOUBLE) {
                    this.floodCategory[HydroFloodCategories.MINOR_FLOOD_CATEGORY
                            .getRank()] = this.minorStage;
                }
                if (this.moderateStage != RiverHydroConstants.MISSING_VALUE_DOUBLE) {
                    this.floodCategory[HydroFloodCategories.MODERATE_FLOOD_CATEGORY
                            .getRank()] = this.moderateStage;
                }
                if (this.majorStage != RiverHydroConstants.MISSING_VALUE_DOUBLE) {
                    this.floodCategory[HydroFloodCategories.MAJOR_FLOOD_CATEGORY
                            .getRank()] = this.majorStage;
                }
            } else {
                char peSecondChar = this.physicalElement.charAt(1);

                if ((peSecondChar != 'B') && (peSecondChar != 'C')
                        && (peSecondChar != 'E') && (peSecondChar != 'F')
                        && (peSecondChar != 'V')) {
                    /*
                     * Only load Q* PE's if there are not certain types of
                     * non-flow based Q* types.
                     */
                    if (this.minorFlow != RiverHydroConstants.MISSING_VALUE_DOUBLE) {
                        this.floodCategory[HydroFloodCategories.MINOR_FLOOD_CATEGORY
                                .getRank()] = this.minorFlow;
                    }
                    if (this.moderateFlow != RiverHydroConstants.MISSING_VALUE_DOUBLE) {
                        this.floodCategory[HydroFloodCategories.MODERATE_FLOOD_CATEGORY
                                .getRank()] = this.moderateFlow;
                    }
                    if (this.majorFlow != RiverHydroConstants.MISSING_VALUE_DOUBLE) {
                        this.floodCategory[HydroFloodCategories.MAJOR_FLOOD_CATEGORY
                                .getRank()] = this.majorFlow;
                    }
                }
            }
        }
    }

    /**
     * The "actual" field name is "lid"
     * 
     * @return the identifier of this forecast point
     */
    public String getLid() {
        return this.lid;
    }

    /**
     * 
     * @return the group id of this forecast point
     */
    public String getGroupId() {
        return this.groupId;
    }

    /**
     * Sets if this forecast point should be included in a hazard
     * recommendation.
     * 
     * @param includedInRecommendation
     *            whether or not to include this point in hazard recommendation
     */
    public void setIncludedInRecommendation(boolean includedInRecommendation) {
        this.includedInRecommendation = includedInRecommendation;
    }

    /**
     * @return whether or not this forecast point should be included in a
     *         recommendation
     */
    public boolean isIncludedInRecommendation() {
        return this.includedInRecommendation;
    }

    /**
     * @return the name of this forecast point
     */
    public String getName() {
        return this.name;
    }

    /**
     * @return this forecast points flood stage
     */
    public double getFloodStage() {
        return this.floodStage;
    }

    /**
     * @return this forecast points action stage
     */
    public double getActionStage() {
        return this.actionStage;
    }

    /**
     * @return a Coordinate representing this station's location
     */
    public Coordinate getLocation() {
        return new Coordinate(this.longitude, this.latitude);
    }

    /**
     * @return the primary physical element
     */
    public String getPhysicalElement() {
        return this.physicalElement;
    }

    /**
     * @return the flood category reached by the data associated with this point
     */
    public double[] getFloodCategory() {
        return this.floodCategory;
    }

    /**
     * @return the proximity
     */
    public String getProximity() {
        return proximity;
    }

    /**
     * @return the river reach associated with this point
     */
    public String getReach() {
        return this.reach;
    }

    /**
     * @return the action flow value
     */
    public double getActionFlow() {
        return this.actionFlow;
    }

    /**
     * @return the county this point is in
     */
    public String getCounty() {
        return this.county;
    }

    /**
     * @return the state this point resides in
     */
    public String getState() {
        return this.state;
    }

    /**
     * @return the name of the stream this forecast point is on
     */
    public String getStream() {
        return this.stream;
    }

    /**
     * @return the bankfull stage
     */
    public double getBankFull() {
        return this.bankFull;
    }

    /**
     * Get Hydrologic Service Area.
     * 
     * @return the hsa this point belongs to
     * 
     */
    public String getHsa() {
        return this.hsa;
    }

    /**
     * @return the primary backup office for this point
     */
    public String getPrimaryBackup() {
        return this.primaryBackup;
    }

    /**
     * @return the recommendation type associated with this point
     */
    public String getRecommendationType() {
        return this.recommendationType;
    }

    /**
     * @return the secondary backup office for this point
     */
    public String getSecondaryBackup() {
        return this.secondaryBackup;
    }

    /**
     * @return the minor flood category.
     */
    public double getMinorFloodCategory() {
        return this.floodCategory[HydroFloodCategories.MINOR_FLOOD_CATEGORY
                .getRank()];
    }

    /**
     * @return the moderate flood category.
     */
    public double getModerateFloodCategory() {
        return this.floodCategory[HydroFloodCategories.MODERATE_FLOOD_CATEGORY
                .getRank()];
    }

    /**
     * @return the major flood category.
     */
    public double getMajorFloodCategory() {
        return this.floodCategory[HydroFloodCategories.MAJOR_FLOOD_CATEGORY
                .getRank()];
    }

    /**
     * @return the major flood category.
     */
    public double getRecordFloodCategory() {
        return this.floodCategory[HydroFloodCategories.RECORD_FLOOD_CATEGORY
                .getRank()];
    }

    /**
     * @return the floodFlow
     */
    public double getFloodFlow() {
        return this.floodFlow;
    }

    /**
     * @return the minorFlow
     */
    public double getMinorFlow() {
        return this.minorFlow;
    }

    /**
     * @param minorFlow
     *            the minorFlow to set
     */
    public void setMinorFlow(double minorFlow) {
        this.minorFlow = minorFlow;
    }

    /**
     * @return the moderateFlow
     */
    public double getModerateFlow() {
        return this.moderateFlow;
    }

    /**
     * @param moderateFlow
     *            the moderateFlow to set
     */
    public void setModerateFlow(double moderateFlow) {
        this.moderateFlow = moderateFlow;
    }

    /**
     * @return the majorFlow
     */
    public double getMajorFlow() {
        return this.majorFlow;
    }

    /**
     * @param majorFlow
     *            the majorFlow to set
     */
    public void setMajorFlow(double majorFlow) {
        this.majorFlow = majorFlow;
    }

    /**
     * @return the minorStage
     */
    public double getMinorStage() {
        return this.minorStage;
    }

    /**
     * @param minorStage
     *            the minorStage to set
     */
    public void setMinorStage(double minorStage) {
        this.minorStage = minorStage;
    }

    /**
     * @return the moderateStage
     */
    public double getModerateStage() {
        return moderateStage;
    }

    /**
     * @param moderateStage
     *            the moderateStage to set
     */
    public void setModerateStage(double moderateStage) {
        this.moderateStage = moderateStage;
    }

    /**
     * @return the majorStage
     */
    public double getMajorStage() {
        return this.majorStage;
    }

    /**
     * @param majorStage
     *            the majorStage to set
     */
    public void setMajorStage(double majorStage) {
        this.majorStage = majorStage;
    }

    /**
     * @return the useLatestForecast
     */
    public boolean getUseLatestForecast() {
        return this.useLatestForecast;
    }

    /**
     * @param useLatestForecast
     *            the useLatestForecast to set
     */
    public void setUseLatestForecast(boolean useLatestForecast) {
        this.useLatestForecast = useLatestForecast;
    }

    /**
     * @return the ordinal
     */
    public int getOrdinal() {
        return this.ordinal;
    }

    /**
     * @return the changeThreshold
     */
    public double getChangeThreshold() {
        return this.changeThreshold;
    }

    /**
     * @param changeThreshold
     *            the changeThreshold to set
     */
    public void setChangeThreshold(double changeThreshold) {
        this.changeThreshold = changeThreshold;
    }

    /**
     * @return the backHrs
     */
    public int getBackHrs() {
        return this.backHrs;
    }

    /**
     * @return the forwardHrs
     */
    public int getForwardHrs() {
        return this.forwardHrs;
    }

    /**
     * @return the adjustEndHrs
     */
    public double getAdjustEndHrs() {
        return this.adjustEndHrs;
    }

    /**
     * @return the latitude
     */
    public double getLatitude() {
        return this.latitude;
    }

    /**
     * @param latitude
     *            the latitude to set
     */
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    /**
     * @return the longitude
     */
    public double getLongitude() {
        return this.longitude;
    }

    /**
     * @param longitude
     *            the longitude to set
     */
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("LID primaryPE FloodStage FloodFlow: ").append(this.lid)
                .append(" ").append(this.physicalElement).append(" ")
                .append(this.floodStage).append(" ").append(this.floodFlow);
        return sb.toString();
    }
}

package com.raytheon.uf.common.hazards.hydro;

import java.util.Date;
import java.util.List;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

/**
 * 
 * Description: Represents a set of river forecast points grouped together
 * according to the river they are on. These can be treated as a set and hazard
 * events can be generated which contain all of the river forecast points in
 * this group, even if individual points are not reaching above flood level.
 * 
 * This is a Data-Only object. This class contains BOTH table data AND
 * calculated data values. Sub-queries and Calculations are performed within
 * RiverForecastManager.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * June 2011               Bryon.Lawrence      Initial creation
 * Apr 1, 2014  3581       bkowal      Relocate to common hazards hydro
 * May 08, 2015 6562       Chris.Cody  Restructure River Forecast Points/Recommender
 * Jul 22, 2015 9670       Chris.Cody  Changes for Base database query result numeric casting
 * May 04, 2016 15584      Kevin.Bisanz Add toString()
 * May 10, 2016 18240      Kevin.Bisanz Add isPointIdInGroup(..)
 * 
 * </pre>
 * 
 * @author Bryon.Lawrence
 * @version 1.0
 */
public class RiverForecastGroup {
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(RiverForecastGroup.class);

    public static final String TABLE_NAME = "rpffcstgroup";

    public static final String COLUMN_NAME_STRING = "group_id, group_name, ordinal, rec_all_included";

    public static final String DISTINCT_COLUMN_NAME_STRING = "rpffcstgroup.group_id, rpffcstgroup.group_name, rpffcstgroup.ordinal, rpffcstgroup.rec_all_included";

    private final int GROUP_ID_FIELD_IDX = 0;

    private final int GROUP_NAME_FIELD_IDX = 1;

    private final int ORDINAL_FIELD_IDX = 2;

    private final int REC_ALL_INCLUDED_FIELD_IDX = 3;

    /**
     * forecast group id
     */
    private String groupId;

    /**
     * forecast group name
     */
    private String groupName;

    /**
     * ordinal
     */
    private int ordinal;

    /**
     * Recommend All Points In Group.
     * 
     * Whether or not to create a hazard containing a river points in this
     * group.
     */
    private boolean recommendAllPointsInGroup;

    /**
     * The river points contained in this group.
     */
    private List<RiverForecastPoint> forecastPointList;

    /**
     * dynamic info determined from maximum observed forecast data.
     */
    /**
     * Whether or not to include this river group in the recommendation.
     */
    private boolean includedInRecommendation;

    private int maxCurrentObservedCategory;

    private Date maxCurrentObservedTime;

    private int maxForecastCategory;

    private Date maxForecastTime;

    private int maxOMFCategory;

    private Date maxOMFTime;

    /**
     * Creates a River Forecast Group object
     * 
     * @param groupID
     *            Group Identifier
     * @param groupName
     *            Name of Group
     * @param ordinal
     *            numeric order position of group
     * @param isRecommenedInAll
     *            Are the points of this group recommended
     */
    public RiverForecastGroup(String groupId, String groupName, int ordinal,
            boolean isRecommenedInAll) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.ordinal = ordinal;
        this.recommendAllPointsInGroup = isRecommenedInAll;
    }

    /**
     * Create a river forecast group.
     * 
     * @param queryResult
     *            - Information about this river group (rpffcstgroup) from the
     *            IHFS database
     */
    public RiverForecastGroup(Object[] queryResult) {
        if (queryResult != null) {
            int queryResultSize = queryResult.length;
            Object queryValue = null;
            for (int i = 0; i < queryResultSize; i++) {
                queryValue = queryResult[i];
                switch (i) {
                case GROUP_ID_FIELD_IDX:
                    this.groupId = (String) queryValue;
                    break;
                case GROUP_NAME_FIELD_IDX:
                    this.groupName = (String) queryValue;
                    break;
                case ORDINAL_FIELD_IDX:
                    this.ordinal = ((Number) queryValue).intValue();
                    break;
                case REC_ALL_INCLUDED_FIELD_IDX:
                    if ("Y".equals((String) queryValue)) {
                        this.recommendAllPointsInGroup = true;
                    } else {
                        this.recommendAllPointsInGroup = false;
                    }
                    break;
                default:
                    statusHandler
                            .error("RiverForecastGroup Constructor array out of sync with number of data fields. Unknown field for value "
                                    + (String) queryValue);
                }
            }
        }
    }

    /**
     * @return the group_id
     */
    public String getGroupId() {
        return (this.groupId);
    }

    /**
     * @return the group_name of this river group
     */
    public String getGroupName() {
        return (this.groupName);
    }

    /**
     * @return the ordinal of this river group
     */
    public int getOrdinal() {
        return (this.ordinal);
    }

    /**
     * @return true - all river points are included in the recommended event,
     *         false - only river points above flood should be included
     */
    public boolean isRecommendAllPointsInGroup() {
        return recommendAllPointsInGroup;
    }

    /**
     * @param
     * @return The number of River Forecast Points in this group.
     */
    public int getNumberOfForecastPoints() {
        return (forecastPointList != null) ? forecastPointList.size() : 0;
    }

    /**
     * @return the list of river stations in this group.
     */
    public List<RiverForecastPoint> getForecastPointList() {
        return this.forecastPointList;
    }

    /**
     * @return the list of river stations in this group.
     */
    public void setForecastPointList(List<RiverForecastPoint> forecastPointList) {
        this.forecastPointList = forecastPointList;
    }

    /**
     * Determine if the provided point ID is part of this RiverForecastGroup
     * 
     * @param pointID
     * @return True if the point ID is part of this group, false otherwise
     */
    public boolean isPointIdInGroup(String pointID) {
        boolean retval = false;
        if (forecastPointList != null) {
            for (RiverForecastPoint rfp : forecastPointList) {
                if (rfp.getLid().equals(pointID)) {
                    retval = true;
                    break;
                }
            }
        }
        return retval;
    }

    // RIVER FORECAST GROUP DYNAMIC (Computed) DATA
    /**
     * @param includedInRecommendation
     *            whether or not to include this river group in the hazard event
     *            recommendation.
     */
    public void setIncludedInRecommendation(boolean includedInRecommendation) {
        this.includedInRecommendation = includedInRecommendation;
    }

    /**
     * @return whether or not the river group should be included in the
     *         recommendation.
     */
    public boolean isIncludedInRecommendation() {
        return this.includedInRecommendation;
    }

    /**
     * Set the maximum observed flood time.
     * 
     * @param maxCurrentObservedTime
     */
    public void setMaxCurrentObservedTime(Date maxCurrentObservedTime) {
        this.maxCurrentObservedTime = maxCurrentObservedTime;
    }

    /**
     * Get the maximum observed flood time.
     * 
     * @return maxCurrentObservedTime
     */
    public Date getMaxCurrentObservedTime() {
        return this.maxCurrentObservedTime;
    }

    /**
     * Set the max observed forecast category of all the river points in this
     * river group.
     * 
     * @param maxOMFCategory
     */
    public void setMaxOMFCategory(int maxOMFCategory) {
        this.maxOMFCategory = maxOMFCategory;
    }

    /**
     * SGet the max observed forecast category of all the river points in this
     * river group.
     * 
     * @return maxOMFCategory
     */
    public int getMaxOMFCategory() {
        return this.maxOMFCategory;
    }

    /**
     * Set the time of the maximum observed forecast data of all the river
     * points in this river group.
     * 
     * @param maxOMFTime
     */
    public void setMaxOMFTime(Date maxOMFTime) {
        this.maxOMFTime = maxOMFTime;
    }

    /**
     * Get the time of the maximum observed forecast data of all the river
     * points in this river group.
     * 
     * @return maxOMFTime
     */
    public Date getMaxOMFTime() {
        return this.maxOMFTime;
    }

    /**
     * Set the Flood Category (rank) of the maximum observed data of all the
     * river points in this river group.
     * 
     * @param maxCurrentObservedCategory
     */
    public void setMaxCurrentObservedCategory(int maxCurrentObservedCategory) {
        this.maxCurrentObservedCategory = maxCurrentObservedCategory;
    }

    /**
     * Get the Flood Category (rank) of the maximum observed data of all the
     * river points in this river group.
     * 
     * @return maxCurrentObservedCategory
     */
    public int getMaxCurrentObservedCategory() {
        return this.maxCurrentObservedCategory;
    }

    /**
     * Set the Flood Category (rank) of the maximum forecast data of all the
     * river points in this river group.
     * 
     * @param maxForecastCategory
     */
    public void setMaxForecastCategory(int maxForecastCategory) {
        this.maxForecastCategory = maxForecastCategory;
    }

    /**
     * Get the Flood Category (rank) of the maximum forecast data of all the
     * river points in this river group.
     * 
     * @return maxForecastCategory
     */
    public int getMaxForecastCategory() {
        return this.maxForecastCategory;
    }

    /**
     * Set the maxForecastTime
     * 
     * @param maxForecastTime
     */
    public void setMaxForecastTime(Date maxForecastTime) {
        this.maxForecastTime = maxForecastTime;
    }

    /**
     * Get the maximum forecast flood time.
     * 
     * @return maxForecastTime
     */
    public Date getMaxForecastTime() {
        return this.maxForecastTime;
    }

    @Override
    public String toString() {
        return "groupName forecastPointList:" + groupName + " "
                + forecastPointList;
    }
}

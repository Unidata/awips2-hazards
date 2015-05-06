package com.raytheon.uf.common.hazards.hydro;

import java.util.List;

import com.google.common.collect.Lists;

/**
 * Description: Represents a county river forecast group. This is a group of all
 * of the river forecast points in a given county. The observations and
 * forecasts are examined for each forecast point to determine the flood state
 * of the county. This was an optional mode in RiverPro. Generally, river
 * forecast points are examined individually or along a river reach.
 * 
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * June 2011               Bryon.Lawrence    Initial creation
 * March 1 2013            Bryon.Lawrence    Preparing for code review
 * May 1, 2014  3581       bkowal            Relocate to common hazards hydro
 * May 08, 2015 6562       Chris.Cody        Restructure River Forecast Points/Recommender
 * </pre>
 * 
 * @author Bryon.Lawrence
 */

public final class CountyForecastGroup {

    private String hsaId;

    private String state;

    private String county;

    private List<RiverForecastPoint> forecastPointsInCountyList;

    /**
     * Computed data fields.
     */
    private int maxCurrentObservedCategory;

    private long maxCurrentObservedTime;

    private int maxMaxForecastCategory;

    private long maxMaxForecastTime;

    private int maxOMFCategory;

    private long maxOMFTime;

    public CountyForecastGroup() {
        this.hsaId = "";
        this.state = "";
        this.county = "";
    }

    /**
     * Construct an instance of a county forecast group.
     * 
     * @param hsaId
     *            The Hydrologic Service Area id
     * @param state
     *            State Name abbreviation
     * @param county
     *            County Name
     * @param forecastPointList
     *            List of river forecast points for the County
     */
    public CountyForecastGroup(String hsaId, String state, String county,
            List<RiverForecastPoint> forecastPointList) {
        this.hsaId = hsaId;
        this.state = state;
        this.county = county;

        if (forecastPointsInCountyList != null) {
            this.forecastPointsInCountyList = forecastPointList;
        } else {
            this.forecastPointsInCountyList = Lists.newArrayList();
        }
    }

    /**
     * Get Hydrological Service Area Id
     * 
     * @return hsaId
     */
    public String getHsaId() {
        return (this.hsaId);
    }

    /**
     * Get "<State abbr.>|<County Name>" String
     * 
     * 
     * @return "state|county" string
     */
    public String getStateCountyString() {
        return (this.state + "|" + this.county);
    }

    /**
     * Get State Abbreviation
     * 
     * @return state
     */
    public String getState() {
        return (this.state);
    }

    /**
     * Get County Name
     * 
     * @return county
     */
    public String getCounty() {
        return (this.county);
    }

    /**
     * Set the max observed forecast time.
     * 
     * @param maxOMFTime
     */
    public void setMaxOMFTime(long maxOMFTime) {
        this.maxOMFTime = maxOMFTime;
    }

    /**
     * Get the max observed forecast time.
     * 
     * @return maxOMFTime
     */
    public long getMaxOMFTime() {
        return (this.maxOMFTime);
    }

    /**
     * Set the max observed forecast flood category.
     * 
     * @param maxOMFCategory
     */
    public void setMaxOMFCategory(int maxOMFCategory) {
        this.maxOMFCategory = maxOMFCategory;
    }

    /**
     * Get the max observed forecast flood category.
     * 
     * @return maxOMFCategory
     */
    public int getMaxOMFCategory() {
        return maxOMFCategory;
    }

    /**
     * Get Forecast Category for the Computed Maximum of the Point Forecast Time
     * values.
     * 
     * @return maxMaxForecastCategory
     */
    public int getMaxMaxForecastCategory() {
        return this.maxMaxForecastCategory;
    }

    /**
     * Set the max current observed time.
     * 
     * @param maxCurrentObservedTime
     */
    public void setMaxCurrentObservedTime(long maxCurrentObservedTime) {
        this.maxCurrentObservedTime = maxCurrentObservedTime;
    }

    /**
     * Get the max current observed time.
     * 
     * @return maxOMFTime
     */
    public long getMaxCurrentObservedTime() {
        return (this.maxCurrentObservedTime);
    }

    /**
     * Set the max observed flood category.
     * 
     * @param maxCurrentObservedCategory
     */
    public void setMaxCurrentObservedCategory(int maxCurrentObservedCategory) {
        this.maxCurrentObservedCategory = maxCurrentObservedCategory;
    }

    /**
     * Get Forecast Category for the Computed Maximum of the Point Observed Time
     * values.
     * 
     * @return maxMaxForecastCategory
     */
    public int getMaxCurrentObservedCategory() {
        return this.maxCurrentObservedCategory;
    }

    /**
     * Get Computed Maximum of the Point Forecast Time values.
     * 
     * @return maxMaxForecastTime
     */
    public long getMaxMaxForecastTime() {
        return this.maxMaxForecastTime;
    }

    public void setMaxMaxForecastCategory(int maxMaxForecastCategory) {
        this.maxMaxForecastCategory = maxMaxForecastCategory;
    }

    public void setMaxMaxForecastTime(long maxMaxForecastTime) {
        this.maxMaxForecastTime = maxMaxForecastTime;
    }

    /**
     * Set Forecast Point List for HSA: State: County object
     * 
     * @param forecastPointsInCountyList
     */
    public void setForecastPointsInCountyList(
            List<RiverForecastPoint> forecastPointsInCountyList) {
        if (forecastPointsInCountyList != null) {
            this.forecastPointsInCountyList = forecastPointsInCountyList;
        } else {
            this.forecastPointsInCountyList = Lists.newArrayList();
        }
    }

    /**
     * Get Forecast Point List for HSA: State: County object
     * 
     * @return forecastPointsInCountyList
     */
    public List<RiverForecastPoint> getForecastPointsInCountyList() {
        return this.forecastPointsInCountyList;
    }

}

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

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.raytheon.uf.common.hazards.hydro.RiverHydroConstants.HydroFloodCategories;
import com.raytheon.uf.common.hazards.hydro.RiverHydroConstants.HydroGraphTrend;
import com.raytheon.uf.common.util.Pair;

/**
 * This class represents a river forecast point and its calculated meta
 * information and behavior. The table data from the View Table FPINFO is in the
 * FpInfo object. This object contains NO FpInfo table data.
 * 
 * Sub-queries and Calculations are performed within RiverForecastManager. This
 * is a Data-Only object.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 08, 2015 6562       Chris.Cody  Initial creation: Restructure River Forecast Points/Recommender
 * </pre>
 * 
 * @author Bryon.Lawrence
 */

public class RiverForecastPoint extends FpInfo {

    /**
     * Primary Physical Element (PE) as queried from the RiverStat (River
     * Station) table value.
     */
    private String primaryPE;

    /**
     * Current Hydrograph observation
     */
    private HydrographObserved hydrographObserved;

    /**
     * Current hydrograph forecast
     */
    private HydrographForecast hydrographForecast;

    /**
     * Are there previous events for this forecast point?
     */
    private boolean previousProductAvailable;

    /**
     * Previous event's observed value
     */
    private double previousCurObsValue;

    /**
     * Time of the previous event's observed value.
     */
    private Date previousCurrentObsTime;

    /**
     * Previous event's max forecast value
     */
    private double previousMaxFcstValue;

    /**
     * Previous event's max forecast time
     */
    private Date previousMaxFcstTime;

    /**
     * Previous event's max crest time
     */
    private Date previousMaxFcstCTime;

    /**
     * Previous event's maximum observed or forecast category
     */
    private int previousMaxObservedForecastCategory;

    /**
     * Current SHEF observation flood category
     */
    private SHEFObserved currentSHEFObservation;

    /**
     * Current observation flood category
     */
    private int currentObservationCategory;

    /**
     * Index of current observation.
     */
    private int observedCurrentIndex;

    /**
     * maximum SHEF forecast value
     */
    private SHEFForecast maximumSHEFForecast;

    /**
     * maximum forecast flood category
     */
    private int maximumForecastCategory;

    /**
     * maximum SHEF observed value
     */
    private SHEFObserved maximumSHEFObserved;

    /**
     * maximum observed forecast value
     */
    private double maximumObservedForecastValue;

    /**
     * maximum observed forecast category
     */
    private int maximumObservedForecastCategory;

    /**
     * maximum observed forecast time
     */
    private Date maximumObservedForecastTime;

    /*
     * Note that these variables refer to the rise or fall with reference to the
     * previous product, not the current time series.
     */
    /**
     * Rise or fall of current hydrograph crest with respect to previous event
     * hydrograph.
     */
    private HydroGraphTrend riseOrFall;

    /**
     * Rise or fall of observed data with respect to previous event observed
     * data
     */
    private HydroGraphTrend observedRiseOrFall;

    /**
     * Rise or fall of forecast data with respect to previous event forecast
     * data
     */
    private HydroGraphTrend forecastRiseOrFall;

    /*
     * observed and forecast values for complete timeseries. loaded in
     * get_obsfcst_ts() in get_stages.c/ keep track of when the full time series
     * data are loaded. the use_obsH keeps track of how much of the observed
     * time series to use, based on the previous VTEC event end time.
     */

    /**
     * The last observation to use.
     */
    private Date obsCutoffTime;

    /**
     * The time to start loading observations at.
     */
    private Date obsLoadTime;

    /**
     * The load time for the next possible pass into this function
     */
    private Date fullTimeSeriesLoadedTime;

    /**
     * maximum observation data index
     */
    private int observedMaximumIndex;

    /**
     * departure of observed value from flood stage
     */
    private double observedFloodStageDeparture;

    /**
     * Index of 24 hour maximum observation
     */
    private int observedMax24Index;

    /**
     * Index of 6 hour maximum observation
     */
    private int observedMax06Index;

    /*
     * values that may or may not be set depending upon the situation, but will
     * only be set if at least one stage value is available; these values are
     * determined via compute_obs_info()
     */
    /**
     * The time the observed hydrograph falls below flood stage
     */
    private long observedFallBelowTime;

    /**
     * The time the observed hydrograph rises above flood stage
     */
    private long observedRiseAboveTime;

    /**
     * The time the observed hydrograph crests
     */
    private double observedCrestValue;

    /**
     * the time of the observed hydrograph crest
     */
    private long observedCrestTime;

    /*
     * derived forecast values from the full timeseries these values that will
     * be set if at least one stage value
     */

    /**
     * Index of maximum forecast element
     */
    private int maximumForecastIndex;

    /**
     * Forecast departure from flood stage
     */
    private double forecastFloodStageDeparture;

    /**
     * Index of forecast crest.
     */
    private int forecastXfCrestIndex;

    /*
     * Values that may or may not be set depending upon the situation, but will
     * only be set if at least one stage value available; these values are
     * determined via compute_fcst_info()
     */

    /**
     * The time the forecast hydrograph drops below flood stage.
     */
    private long forecastFallBelowTime;

    /**
     * The time the forecast hydrograph rises above flood stage.
     */
    private long forecastRiseAboveTime;

    /**
     * The value of the forecast hydrograph crest.
     */
    private double forecastCrestValue;

    /**
     * The time of the forecast crest.
     */
    private long forecastCrestTime;

    /*
     * Overall fall, rise times
     */
    /**
     * fall below flood stage time.
     */
    private long fallBelowTime;

    /**
     * rise above flood stage time.
     */
    private long riseAboveTime;

    /*
     * The type source corresponding to the overall fall, rise, crest
     */
    /**
     * fall below data type source.
     */
    private String fallBelowTypeSource;

    /**
     * rise above data type source.
     */
    private String riseAboveTypeSource;

    /**
     * Crest type source.
     */
    private String crestTypeSource;

    /*
     * Trend values; for observed trend and overall trend
     */

    /**
     * overall trend in observed and forecast hydrographs
     */
    private HydroGraphTrend trend;

    /**
     * Whether or not this forecast point needs to be included in the hazard
     * recommendation
     */
    private boolean includedInRecommendation;

    private List<CrestHistory> flowCrestHistoryList;

    private List<CrestHistory> stageCrestHistoryList;

    /**
     * Default constructor
     */
    public RiverForecastPoint() {

        initializeActiveValues();
    }

    /**
     * Creates a river forecast point object
     * 
     * @param forecastPointInfo
     *            Information specific to this forecast point
     */
    public RiverForecastPoint(Object[] queryResult) {

        super(queryResult);
        initializeActiveValues();
    }

    private void initializeActiveValues() {
        /*
         * Initialize observed/forecast time-series related variables.
         */
        this.hydrographObserved = null;
        this.hydrographForecast = null;
        this.fullTimeSeriesLoadedTime = null;
    }

    /**
     * Set the Primary Physical Element (PE) as queried from the RiverStat
     * (River Station) table (RiverStat.PRIMARY_PE) for this LID value.
     * 
     * @param primaryPE
     *            RiverStat.primary_pe value
     */
    public void setPrimaryPE(String primaryPE) {
        this.primaryPE = primaryPE;
    }

    /**
     * Get the Primary Physical Element (PE) as queried from the RiverStat
     * (River Station) table (RiverStat.PRIMARY_PE) for this LID value.
     * 
     * @return primaryPE RiverStat.primary_pe value
     */
    public String getPrimaryPE() {
        return (this.primaryPE);
    }

    /**
     * Set the observed value of the previous recommended event
     * 
     * @param previousCurObsValue
     */
    public void setPreviousCurObsValue(double previousCurObsValue) {
        this.previousCurObsValue = previousCurObsValue;
    }

    /**
     * 
     * @return the observed value of the previous recommended event
     */
    public double getPreviousCurObsValue() {
        return previousCurObsValue;
    }

    /**
     * Set the time of the previous observed value
     * 
     * @param previousCurrentObsTime
     */
    public void setPreviousCurrentObsTime(Date previousCurrentObsTime) {
        this.previousCurrentObsTime = previousCurrentObsTime;
    }

    /**
     * 
     * @return The time of the previous observed value
     */
    public Date getPreviousCurrentObsTime() {
        return this.previousCurrentObsTime;
    }

    /**
     * Set is there a previous product available
     * 
     * @param previousProductAvailable
     */
    public void setPreviousProductAvailable(boolean previousProductAvailable) {
        this.previousProductAvailable = previousProductAvailable;
    }

    /**
     * @return is there a previous product available
     */
    public boolean getPreviousProductAvailable() {
        return this.previousProductAvailable;
    }

    /**
     * Set the previousMaxFcstValue
     * 
     * @param previousMaxFcstValue
     */
    public void setPreviousMaxFcstValue(double previousMaxFcstValue) {
        this.previousMaxFcstValue = previousMaxFcstValue;
    }

    /**
     * @return the previous maximum forecast value
     */
    public double getPreviousMaxFcstValue() {
        return this.previousMaxFcstValue;
    }

    /**
     * Set the previousMaxFcstTime
     * 
     * @param previousMaxFcstTime
     */
    public void setPreviousMaxFcstTime(Date previousMaxFcstTime) {
        this.previousMaxFcstTime = previousMaxFcstTime;
    }

    /**
     * @return the previous maximum forecast time
     */
    public Date getPreviousMaxFcstTime() {
        return this.previousMaxFcstTime;
    }

    /**
     * Set the previousMaxFcstCTime
     * 
     * @param previousMaxFcstCTime
     */
    public void setPreviousMaxFcstCTime(Date previousMaxFcstCTime) {
        this.previousMaxFcstCTime = previousMaxFcstCTime;
    }

    /**
     * @return the previous maximum forecast crest time
     */
    public Date getPreviousMaxFcstCTime() {
        return this.previousMaxFcstCTime;
    }

    public void setPreviousMaxObservedForecastCategory(
            int previousMaxObservedForecastCategory) {
        this.previousMaxObservedForecastCategory = previousMaxObservedForecastCategory;
    }

    /**
     * @return the previous event maximum observed forecast category
     */
    public int getPreviousMaxObservedForecastCategory() {
        return this.previousMaxObservedForecastCategory;
    }

    /**
     * Set the currentSHEFObservation
     * 
     * @param currentSHEFObservation
     */
    public void setCurrentObservation(SHEFObserved currentSHEFObservation) {
        this.currentSHEFObservation = currentSHEFObservation;
    }

    /**
     * @return the current observation flood category
     */
    public SHEFObserved getCurrentObservation() {
        return this.currentSHEFObservation;
    }

    /**
     * Set the currentObservationCategory
     * 
     * @param currentObservationCategory
     */
    public void setCurrentObservationCategory(int currentObservationCategory) {
        this.currentObservationCategory = currentObservationCategory;
    }

    /**
     * @return the current observation flood category
     */
    public int getCurrentObservationCategory() {
        return this.currentObservationCategory;
    }

    /**
     * Set the maximumForecastCategory
     * 
     * @param maximumForecastCategory
     */
    public void setMaximumForecastCategory(int maximumForecastCategory) {
        this.maximumForecastCategory = maximumForecastCategory;
    }

    /**
     * @return the maximum forecast flood category
     */
    public int getMaximumForecastCategory() {
        return this.maximumForecastCategory;
    }

    /**
     * Set the maximumSHEFForecast
     * 
     * @param maximumSHEFForecast
     */
    public void setMaximumSHEFForecast(SHEFForecast maximumSHEFForecast) {
        this.maximumSHEFForecast = maximumSHEFForecast;
    }

    /**
     * @return the maximum SHEF forecast
     */
    public SHEFForecast getMaximumSHEFForecast() {
        return this.maximumSHEFForecast;
    }

    /**
     * Set the maximumSHEFObserved
     * 
     * @param maximumSHEFObserved
     */
    public void setMaximumSHEFObserved(SHEFObserved maximumSHEFObserved) {
        this.maximumSHEFObserved = maximumSHEFObserved;
    }

    /**
     * @return the maximum SHEF Observed
     */
    public SHEFObserved getMaximumSHEFObserved() {
        return this.maximumSHEFObserved;
    }

    /**
     * Set the rise above flood stage time
     * 
     * @param riseAboveTime
     */
    public void setRiseAboveTime(long riseAboveTime) {
        this.riseAboveTime = riseAboveTime;
    }

    /**
     * Get the rise above flood stage time
     * 
     * @return
     */
    public long getRiseAboveTime() {
        return this.riseAboveTime;
    }

    /**
     * Set the maximum observed forecast flood category
     * 
     * @param maximumObservedForecastCategory
     */
    public void setMaximumObservedForecastCategory(
            int maximumObservedForecastCategory) {
        this.maximumObservedForecastCategory = maximumObservedForecastCategory;
    }

    /**
     * Get the maximum observed forecast flood category
     * 
     * @return maximumObservedForecastCategory
     */
    public int getMaximumObservedForecastCategory() {
        return this.maximumObservedForecastCategory;
    }

    /**
     * Set the trend of the hydrograph associated with this forecast point
     * 
     * @param riseOrFall
     */
    public void setRiseOrFall(HydroGraphTrend riseOrFall) {
        this.riseOrFall = riseOrFall;
    }

    /**
     * Get the trend of the hydrograph associated with this forecast point
     * 
     * @return riseOrFall
     */
    public HydroGraphTrend getRiseOrFall() {
        return this.riseOrFall;
    }

    /**
     * Sets if this forecast point should be included in a hazard
     * recommendation.
     * 
     * @param includedInRecommendation
     *            whether or not to include this point in hazard recommendation
     */
    @Override
    public void setIncludedInRecommendation(boolean includedInRecommendation) {
        this.includedInRecommendation = includedInRecommendation;
    }

    /**
     * @return whether or not this forecast point should be included in a
     *         recommendation
     */
    @Override
    public boolean isIncludedInRecommendation() {
        return this.includedInRecommendation;
    }

    /**
     * Set the fall below flood stage time
     * 
     * @param fallBelowTime
     */
    public void setFallBelowTime(long fallBelowTime) {
        this.fallBelowTime = fallBelowTime;
    }

    /**
     * Get the fall below flood stage time
     * 
     * @return fallBelowTime
     */
    public long getFallBelowTime() {
        return this.fallBelowTime;
    }

    /**
     * Set the observed crest time
     * 
     * @param observedCrestTime
     */
    public void setObservedCrestTime(long observedCrestTime) {
        this.observedCrestTime = observedCrestTime;
    }

    /**
     * @return the observed crest time
     */
    public long getObservedCrestTime() {
        return this.observedCrestTime;
    }

    /**
     * Set the forecast crest time
     * 
     * @param forecastCrestTime
     */
    public void setForecastCrestTime(long forecastCrestTime) {
        this.forecastCrestTime = forecastCrestTime;
    }

    /**
     * @return the forecast crest time
     */
    public long getForecastCrestTime() {
        return this.forecastCrestTime;
    }

    /**
     * Set the maximum observed forecast value
     * 
     * @param maximumObservedForecastValue
     */
    public void setMaximumObservedForecastValue(
            double maximumObservedForecastValue) {
        this.maximumObservedForecastValue = maximumObservedForecastValue;
    }

    /**
     * Set the maximum observed forecast value
     * 
     * @return maximumObservedForecastValue
     */
    public double getMaximumObservedForecastValue() {
        return this.maximumObservedForecastValue;
    }

    /**
     * Set the maximum observed forecast time
     * 
     * @param maximumObservedForecastTime
     */
    public void setMaximumObservedForecastTime(Date maximumObservedForecastTime) {
        this.maximumObservedForecastTime = maximumObservedForecastTime;
    }

    /**
     * Get the maximum observed forecast time
     * 
     * @return maximumObservedForecastTime
     */
    public Date getMaximumObservedForecastTime() {
        return this.maximumObservedForecastTime;
    }

    /**
     * Set the observed data cutoff time
     * 
     * @param obsCutoffTime
     */
    public void setObsCutoffTime(Date obsCutoffTime) {
        this.obsCutoffTime = obsCutoffTime;
    }

    /**
     * Get the observed data cutoff time
     * 
     * @return obsCutoffTime
     */
    public Date getObsCutoffTime() {
        return this.obsCutoffTime;
    }

    /**
     * Set the observed data load time
     * 
     * @param obsLoadTime
     */
    public void setObsLoadTime(Date obsLoadTime) {
        this.obsLoadTime = obsLoadTime;
    }

    /**
     * Get the observed data load time
     * 
     * @return obsLoadTime
     */
    public Date getObsLoadTime() {
        return this.obsLoadTime;
    }

    /**
     * Set the observed hydrograph trend
     * 
     * @param observedRiseOrFall
     */
    public void setObservedRiseOrFall(HydroGraphTrend observedRiseOrFall) {
        this.observedRiseOrFall = observedRiseOrFall;
    }

    /**
     * Get the the observed hydrograph trend
     * 
     * @return observedRiseOrFall
     */
    public HydroGraphTrend getObservedRiseOrFall() {
        return this.observedRiseOrFall;
    }

    /**
     * Set maximum observation data index
     * 
     * @param forecastRiseOrFall
     */
    public void setForecastRiseOrFall(HydroGraphTrend forecastRiseOrFall) {
        this.forecastRiseOrFall = forecastRiseOrFall;
    }

    /**
     * Get the the forecast hydrograph trend
     * 
     * @return forecastRiseOrFall
     */
    public HydroGraphTrend getForecastRiseOrFall() {
        return this.forecastRiseOrFall;
    }

    /**
     * Set the observed flood stage departure
     * 
     * @param observedFloodStageDeparture
     */
    public void setObservedFloodStageDeparture(
            double observedFloodStageDeparture) {
        this.observedFloodStageDeparture = observedFloodStageDeparture;
    }

    /**
     * Get the observed flood stage departure
     * 
     * @return observedFloodStageDeparture
     */
    public double getObservedFloodStageDeparture() {
        return this.observedFloodStageDeparture;
    }

    /**
     * Set the index of the maximum 24 hour observation
     * 
     * @param observedMax24Index
     */
    public void setObservedMax24Index(int observedMax24Index) {
        this.observedMax24Index = observedMax24Index;
    }

    /**
     * @param index
     *            of the maximum 24 hour observation to set
     */
    public int getObservedMax24Index() {
        return (this.observedMax24Index);
    }

    /**
     * <pre>
     * Emulates the functionality of the <MaxObsStg24> template variable.e.g. 35
     * 
     * Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT
     *       load_variable_value.c -  load_stage_ofp_variable_value()
     *  
     *  @return: The maximum observed stage for the last 24 hours and Shef Quality Code pair
     * </pre>
     */
    public Pair<Double, String> getMaximum24HourObservedStage() {
        String shefQualCode = RiverHydroConstants.MISSING_SHEF_QUALITY_CODE;
        double observedValue = RiverHydroConstants.MISSING_VALUE_DOUBLE;
        SHEFObserved shefObserved = getSHEFObserved(this.observedMax24Index);
        if (shefObserved != null) {
            observedValue = shefObserved.getValue();
            shefQualCode = shefObserved.getShefQualCode();
        }

        return (new Pair<Double, String>(observedValue, shefQualCode));

    }

    /**
     * Set the index of the maximum 06 hour observation
     * 
     * @param observedMax06Index
     */
    public void setObservedMax06Index(int observedMax06Index) {
        this.observedMax06Index = observedMax06Index;
    }

    /**
     * @return the index of the maximu 6 hour observation
     */
    public int getObservedMax06Index() {
        return this.observedMax06Index;
    }

    /**
     * <pre>
     * Emulates the functionality of the <MaxObsStg06> template variable.
     *  e.g. 35
     *  Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT
     *             load_variable_value.c -  load_stage_ofp_variable_value()
     * @return: The maximum observed stage for the last 6 hours  and Shef Quality Code pair.
     * </pre>
     * 
     * .
     */
    public Pair<Double, String> getMaximum6HourObservedStage() {
        String shefQualCode = RiverHydroConstants.MISSING_SHEF_QUALITY_CODE;
        double observedValue = RiverHydroConstants.MISSING_VALUE_DOUBLE;
        SHEFObserved shefObserved = getSHEFObserved(this.observedMax06Index);
        if (shefObserved != null) {
            observedValue = shefObserved.getValue();
            shefQualCode = shefObserved.getShefQualCode();
        }

        return (new Pair<Double, String>(observedValue, shefQualCode));
    }

    /**
     * Set the observed crest value
     * 
     * @param observedCrestValue
     */
    public void setObservedCrestValue(double observedCrestValue) {
        this.observedCrestValue = observedCrestValue;
    }

    /**
     * @return the observed crest value
     */
    public double getObservedCrestValue() {
        return this.observedCrestValue;
    }

    /**
     * Set the forecast flood stage departure
     * 
     * @param observedCrestValue
     */
    public void setForecastFloodStageDeparture(
            double forecastFloodStageDeparture) {
        this.forecastFloodStageDeparture = forecastFloodStageDeparture;
    }

    /**
     * @return the forecast flood stage departure
     */
    public double getForecastFloodStageDeparture() {
        return this.forecastFloodStageDeparture;
    }

    /**
     * Set the forecast maximum crest index (if there is more than one forecast
     * timeseries)
     * 
     * @param forecastXfCrestIndex
     */
    public void setForecastXfCrestIndex(int forecastXfCrestIndex) {
        this.forecastXfCrestIndex = forecastXfCrestIndex;
    }

    /**
     * @return the forecast maximum crest index (if there is more than one
     *         forecast timeseries)
     */
    public int getForecastXfCrestIndex() {
        return this.forecastXfCrestIndex;
    }

    /**
     * Set the forecast crest value
     * 
     * @param forecastCrestValue
     */
    public void setForecastCrestValue(double forecastCrestValue) {
        this.forecastCrestValue = forecastCrestValue;
    }

    /**
     * Get the forecast crest value
     * 
     * @return forecastCrestValue
     */
    public double getForecastCrestValue() {
        return this.forecastCrestValue;
    }

    /**
     * @param the
     *            fallBelowTypeSource
     */
    public void setFallBelowTypeSource(String fallBelowTypeSource) {
        this.fallBelowTypeSource = fallBelowTypeSource;
    }

    /**
     * @return the fallBelowTypeSource
     */
    public String getFallBelowTypeSource() {
        return this.fallBelowTypeSource;
    }

    /**
     * Set flood Rise Above Type Source
     * 
     * @param the
     *            riseAboveTypeSource
     */
    public void setRiseAboveTypeSource(String riseAboveTypeSource) {
        this.riseAboveTypeSource = riseAboveTypeSource;
    }

    /**
     * Get flood Rise Above Type Source
     * 
     * @return the riseAboveTypeSource
     */
    public String getRiseAboveTypeSource() {
        return this.riseAboveTypeSource;
    }

    /**
     * Set flood Crest Type Source
     * 
     * @param the
     *            riseAboveTypeSource
     */
    public void setCrestTypeSource(String crestTypeSource) {
        this.crestTypeSource = crestTypeSource;
    }

    /**
     * Set flood Crest Type Source
     * 
     * @return the crestTypeSource
     */
    public String getCrestTypeSource() {
        return this.crestTypeSource;
    }

    /**
     * Set the flood trend
     * 
     * @param trend
     */
    public void setTrend(HydroGraphTrend hydroGraphTrend) {
        this.trend = hydroGraphTrend;
    }

    /**
     * @return the trend
     */
    public HydroGraphTrend getTrend() {
        return this.trend;
    }

    /**
     * Set the Index of the Current SHEF Observed
     * 
     * @param observedCurrentIndex
     */
    public void setObservedCurrentIndex(int observedCurrentIndex) {
        this.observedCurrentIndex = observedCurrentIndex;
    }

    /**
     * Get the Index of the Current SHEF Observed
     * 
     * @return the observedCurrentIndex
     */
    public int getObservedCurrentIndex() {
        return this.observedCurrentIndex;
    }

    /**
     * <pre>
     * Emulates the functionality of the <ObsStg> template variable.
     * e.g. 35
     * Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT
     *         load_variable_value.c -  load_stage_ofp_variable_value()
     * 
     * @return: The current observed river stage and Shef Quality Code pair.
     * </pre>
     */
    public Pair<Double, String> getObservedCurrentStage() {
        String shefQualCode = RiverHydroConstants.MISSING_SHEF_QUALITY_CODE;
        double observedValue = RiverHydroConstants.MISSING_VALUE_DOUBLE;
        SHEFObserved shefObserved = getSHEFObserved(this.observedCurrentIndex);
        if (shefObserved != null) {
            observedValue = shefObserved.getValue();
            shefQualCode = shefObserved.getShefQualCode();
        }

        return (new Pair<Double, String>(observedValue, shefQualCode));
    }

    public long getObservedCurrentTime() {

        SHEFObserved shefObserved = this
                .getSHEFObserved(this.observedCurrentIndex);
        if (shefObserved != null) {
            return (shefObserved.getObsTime());
        }

        return (RiverHydroConstants.MISSING_VALUE);
    }

    public double getObservedCurrentValue() {

        SHEFObserved shefObserved = this
                .getSHEFObserved(this.observedCurrentIndex);
        if (shefObserved != null) {
            return (shefObserved.getValue());
        }

        return (RiverHydroConstants.MISSING_VALUE_DOUBLE);
    }

    /**
     * @param the
     *            maximumForecastIndex
     */
    public void setMaximumForecastIndex(int maximumForecastIndex) {
        this.maximumForecastIndex = maximumForecastIndex;
    }

    /**
     * @return the maximumForecastIndex
     */
    public int getMaximumForecastIndex() {
        return this.maximumForecastIndex;
    }

    /**
     * <pre>
     * Emulates the functionality of the <MaxObsStg24> template variable.e.g. 35
     * 
     * Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT
     *       load_variable_value.c -  load_stage_ofp_variable_value()
     *  
     *  @return: The maximum observed stage for the last 24 hours and Shef Quality Code pair
     * </pre>
     */
    public Pair<Double, String> getMaximumForecastStage() {
        String shefQualCode = RiverHydroConstants.MISSING_SHEF_QUALITY_CODE;
        double forecastValue = RiverHydroConstants.MISSING_VALUE_DOUBLE;
        SHEFForecast shefForecast = getSHEFForecast(this.maximumForecastIndex);
        if (shefForecast != null) {
            forecastValue = shefForecast.getValue();
            shefQualCode = shefForecast.getShefQualCode();
        }

        return (new Pair<Double, String>(forecastValue, shefQualCode));

    }

    /**
     * <pre>
     * Emulates the functionality of the <MaxObsStg24> template variable.e.g. 35
     * 
     * Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT
     *       load_variable_value.c -  load_stage_ofp_variable_value()
     *  
     *  @return: The maximum observed stage for the last 24 hours and Shef Quality Code pair
     * </pre>
     */
    public long getMaximumForecastTime() {
        long forecastTime = RiverHydroConstants.MISSING_VALUE;
        SHEFForecast shefForecast = getSHEFForecast(this.maximumForecastIndex);
        if (shefForecast != null) {
            forecastTime = shefForecast.getValidTime();
        }

        return (forecastTime);
    }

    /**
     * <pre>
     * Emulates the functionality of the <MaxObsStg24> template variable.e.g. 35
     * 
     * Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT
     *       load_variable_value.c -  load_stage_ofp_variable_value()
     *  
     *  @return: The maximum observed stage for the last 24 hours and Shef Quality Code pair
     * </pre>
     */
    public double getMaximumForecastValue() {
        double forecastValue = RiverHydroConstants.MISSING_VALUE_DOUBLE;
        SHEFForecast shefForecast = getSHEFForecast(this.maximumForecastIndex);
        if (shefForecast != null) {
            forecastValue = shefForecast.getValue();
        }

        return (forecastValue);
    }

    /**
     * Set the observedRiseAboveTime
     * 
     * @param observedRiseAboveTime
     */
    public void setObservedRiseAboveTime(long observedRiseAboveTime) {
        this.observedRiseAboveTime = observedRiseAboveTime;
    }

    /**
     * Get the observedRiseAboveTime
     * 
     * @return observedRiseAboveTime
     */
    public long getObservedRiseAboveTime() {
        return this.observedRiseAboveTime;
    }

    /**
     * Set the observedFallBelowTime
     * 
     * @param observedFallBelowTime
     */
    public void setObservedFallBelowTime(long observedFallBelowTime) {
        this.observedFallBelowTime = observedFallBelowTime;
    }

    /**
     * @return the observedFallBelowTime
     */
    public long getObservedFallBelowTime() {
        return this.observedFallBelowTime;
    }

    /**
     * Set the forecastRiseAboveTime
     * 
     * @param forecastRiseAboveTime
     */
    public void setForecastRiseAboveTime(long forecastRiseAboveTime) {
        this.forecastRiseAboveTime = forecastRiseAboveTime;
    }

    /**
     * @return the forecastRiseAboveTime
     */
    public long getForecastRiseAboveTime() {
        return this.forecastRiseAboveTime;
    }

    /**
     * Set the forecastFallBelowTime
     * 
     * @param forecastFallBelowTime
     */
    public void setForecastFallBelowTime(long forecastFallBelowTime) {
        this.forecastFallBelowTime = forecastFallBelowTime;
    }

    /**
     * @return the forecastFallBelowTime
     */
    public long getForecastFallBelowTime() {
        return this.forecastFallBelowTime;
    }

    /**
     * Set the FORECAST Hydrograph data.
     * 
     * @param the
     *            FORECAST Hydrograph data.
     */
    public void setHydrographForecast(HydrographForecast hydrographForecast) {
        this.hydrographForecast = hydrographForecast;
    }

    /**
     * Get the FORECAST Hydrograph data.
     * 
     * @return the FORECAST Hydrograph data.
     */
    public HydrographForecast getHydrographForecast() {
        return this.hydrographForecast;
    }

    /**
     * Set the OBSERVED Hydrograph data.
     * 
     * @param the
     *            OBSERVED Hydrograph data.
     */
    public void setHydrographObserved(HydrographObserved hydrographObserved) {
        this.hydrographObserved = hydrographObserved;
    }

    /**
     * Get the OBSERVED Hydrograph data.
     * 
     * @return the OBSERVED Hydrograph data.
     */
    public HydrographObserved getHydrographObserved() {
        return this.hydrographObserved;
    }

    /**
     * Set the Stage Crest history List.
     * 
     * @param stageCrestHistoryList
     */
    public void setStageCrestHistoryList(
            List<CrestHistory> stageCrestHistoryList) {
        this.stageCrestHistoryList = stageCrestHistoryList;
    }

    /**
     * Set the Flow Crest history List.
     * 
     * @param flowCrestHistoryList
     */
    public void setFlowCrestHistoryList(List<CrestHistory> flowCrestHistoryList) {
        this.flowCrestHistoryList = flowCrestHistoryList;
    }

    /**
     * Set the load time for the next possible pass into this function
     * 
     * @param fullTimeSeriesLoadedTime
     */
    public void setFullTimeSeriesLoadedTime(Date fullTimeSeriesLoadedTime) {
        this.fullTimeSeriesLoadedTime = fullTimeSeriesLoadedTime;
    }

    /**
     * Get the load time for the next possible pass into this function
     * 
     * @return fullTimeSeriesLoadedTime
     */
    public Date getFullTimeSeriesLoadedTime() {
        return (this.fullTimeSeriesLoadedTime);
    }

    /**
     * Set maximum observation data index
     * 
     * @param observedMaximumIndex
     */
    public void setObservedMaximumIndex(int observedMaximumIndex) {
        this.observedMaximumIndex = observedMaximumIndex;
    }

    /**
     * Get the maximum observation data index
     * 
     * @return observedMaximumIndex
     */
    public int getObservedMaximumIndex() {
        return (this.observedMaximumIndex);
    }

    /**
     * Get the Stage Crest history List.
     * 
     * This returns CrestHistory objects as the result of the query.
     * 
     * @return stageCrestHistoryList
     */
    public List<CrestHistory> getStageCrestHistoryList() {
        return (this.stageCrestHistoryList);
    }

    /**
     * Get the Stage Crest history List.
     * 
     * This returns a List of Pair objects of the Q column and Date of the
     * record
     * 
     * @return stageCrestHistoryList
     */
    public List<Pair<Double, Date>> getStageCrestHistory() {
        List<Pair<Double, Date>> outputStageCrestHistory = null;
        if ((this.stageCrestHistoryList != null && this.stageCrestHistoryList
                .isEmpty() == false)) {

            outputStageCrestHistory = Lists
                    .newArrayListWithExpectedSize(stageCrestHistoryList.size());
            for (CrestHistory stageCrestHistory : stageCrestHistoryList) {
                Double stage = stageCrestHistory.getStage();
                Date datcrst = new java.util.Date(
                        stageCrestHistory.getDatCrst());
                Pair<Double, Date> pair = new Pair<>(stage, datcrst);
                outputStageCrestHistory.add(pair);
            }
        } else {
            outputStageCrestHistory = Lists.newArrayListWithExpectedSize(1);
        }
        return outputStageCrestHistory;
    }

    /**
     * Get the Flow Crest history List.
     * 
     * This returns CrestHistory objects as the result of the query.
     * 
     * @return flowCrestHistoryList
     */
    public List<CrestHistory> getFlowCrestHistoryList() {
        return (this.flowCrestHistoryList);
    }

    public List<Pair<Integer, Date>> getFlowCrestHistory() {
        List<Pair<Integer, Date>> outputFlowCrestHistory = null;
        if ((this.flowCrestHistoryList != null && this.flowCrestHistoryList
                .isEmpty() == false)) {

            outputFlowCrestHistory = Lists
                    .newArrayListWithExpectedSize(flowCrestHistoryList.size());
            for (CrestHistory flowCrestHistory : flowCrestHistoryList) {
                Integer q = flowCrestHistory.getQ();
                Date datcrst = new java.util.Date(flowCrestHistory.getDatCrst());
                Pair<Integer, Date> pair = new Pair<>(q, datcrst);
                outputFlowCrestHistory.add(pair);
            }
        } else {
            outputFlowCrestHistory = Lists.newArrayListWithExpectedSize(1);
        }
        return outputFlowCrestHistory;
    }

    /**
     * Loads the observed forecast values from a previous event.
     * 
     * @param previousEventDict
     */
    public void setFromPreviousObservedForecastValues(
            Map<String, Object> previousEventDict) {
        if (previousEventDict != null) {
            this.setPreviousProductAvailable(true);

            Double obsValue = (Double) previousEventDict.get("currentObsValue");
            this.setPreviousCurObsValue(obsValue);
            Long previousCurObsTime = (Long) previousEventDict
                    .get("currentObsValueTime");
            this.setPreviousCurrentObsTime(new Date(previousCurObsTime));
            Double maxFcstValue = (Double) previousEventDict
                    .get("maxForecastValue");
            this.setPreviousMaxFcstValue(maxFcstValue);
            Long maxForecastTime = (Long) previousEventDict
                    .get("maxForecastTime");
            this.setPreviousMaxFcstTime(new Date(maxForecastTime));
            Long maxForecastCTime = (Long) previousEventDict
                    .get("maxForecastCTime");
            this.setPreviousMaxFcstCTime(new Date(maxForecastCTime));
            Integer previousMaxObservedForecastCategory = (Integer) previousEventDict
                    .get("maxObservedForecastCategory");
            this.setPreviousMaxObservedForecastCategory(previousMaxObservedForecastCategory);
        } else {
            this.setPreviousProductAvailable(false);
            this.setPreviousCurObsValue(RiverHydroConstants.MISSING_VALUE_DOUBLE);
            this.setPreviousCurrentObsTime(null);
            this.setPreviousMaxFcstValue(RiverHydroConstants.MISSING_VALUE_DOUBLE);
            this.setPreviousMaxFcstTime(null);
            this.setPreviousMaxFcstCTime(null);
            this.setPreviousMaxObservedForecastCategory(HydroFloodCategories.NULL_CATEGORY
                    .getRank());
        }
    }

    protected SHEFObserved getSHEFObserved(int index) {
        SHEFObserved shefObserved = null;
        if ((index != RiverHydroConstants.MISSING_VALUE) && (index > -1)) {
            HydrographObserved hydrographObserved = this
                    .getHydrographObserved();
            if (hydrographObserved != null) {
                List<SHEFObserved> shefObservedList = hydrographObserved
                        .getShefHydroDataList();
                if ((shefObservedList != null)
                        && (shefObservedList.size() > index)) {
                    shefObserved = shefObservedList.get(index);
                }
            }
        }
        return (shefObserved);
    }

    protected SHEFForecast getSHEFForecast(int index) {
        SHEFForecast shefForecast = null;
        if ((index != RiverHydroConstants.MISSING_VALUE) && (index > -1)) {
            HydrographForecast hydrographForecast = this
                    .getHydrographForecast();
            if (hydrographForecast != null) {
                List<SHEFForecast> shefForecastList = hydrographForecast
                        .getShefHydroDataList();
                if ((shefForecastList != null)
                        && (shefForecastList.size() > index)) {
                    shefForecast = shefForecastList.get(index);
                }
            }
        }
        return (shefForecast);
    }

}

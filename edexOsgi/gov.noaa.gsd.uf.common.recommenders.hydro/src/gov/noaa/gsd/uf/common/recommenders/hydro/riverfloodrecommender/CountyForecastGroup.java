package gov.noaa.gsd.uf.common.recommenders.hydro.riverfloodrecommender;

import java.util.Date;
import java.util.List;

import com.google.common.collect.Lists;

/**
 * Description: Represents a county river forecast group. This is a group of all
 * of the river forecast points in a given county. The observations and
 * forecasts are examined for each forecast point to determine the flood state
 * of the county. This was an optional mode in RiverPro. Generally, river
 * foreast points are examined individually or along a river reach.
 * 
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * June 2011               Bryon.Lawrence    Initial creation
 * March 1 2013            Bryon.Lawrence    Preparing for code review
 * </pre>
 * 
 * @author Bryon.Lawrence
 */

public final class CountyForecastGroup {
    /*
     * Static info, loaded in by load_grpdata() in get_fp_grp.c
     */
    private String county;

    private String state;

    private final List<RiverForecastPoint> forecastPointsInCountyList;

    /*
     * dynamic info. determined from observed max forecast data.
     */
    private int maxCurrentObservedCategory;

    private Date maxCurrentObservedTime;

    private int maxMaxForecastCategory;

    private Date maxMaxForecastTime;

    private int maxOMFCategory;

    private Date maxOMFTime;

    /*
     * Injected data accessor object.
     */
    private final IFloodRecommenderDAO floodDAO;

    /**
     * Construct an instance of a county forecast group.
     * 
     * @param forecastPointList
     *            List of river forecast points
     * @param countyRecord
     *            Record read from the CountyNum IHFS table
     * @param hsaId
     *            The hydrologic service area id
     * @param floodDAO
     *            The data accessor object.
     */
    public CountyForecastGroup(
            final List<RiverForecastPoint> forecastPointList,
            final Object[] countyRecord, String hsaId,
            final IFloodRecommenderDAO floodDAO) {
        forecastPointsInCountyList = Lists.newArrayList();
        this.floodDAO = floodDAO;
        loadCountyData(forecastPointList, countyRecord, hsaId);
    }

    /**
     * Loads the river forecast points in this particular county.
     * 
     * @param forecastPointList
     *            The master list of river forecast points.
     * @param countyRecord
     *            Record read from the CountyNum IHFS table
     * @param hsaID
     *            The hydrologic service area id
     * 
     * @return
     */
    private void loadCountyData(
            final List<RiverForecastPoint> forecastPointList,
            final Object[] countyRecord, String hsaID) {
        /*
         * Separate the concatenated state and county. The delimiter is a '|'.
         */
        String countyState = countyRecord[0].toString();
        int delimiterPosition = countyState.indexOf('|');
        this.county = countyState.substring(0, delimiterPosition);
        this.state = countyState.substring(++delimiterPosition);

        List<Object[]> countyForecastPointList = null;
        int numberOfForecastPoints = 0;

        countyForecastPointList = floodDAO.getForecastPointsInCountyStateHSA(
                state, county, hsaID);

        if (countyForecastPointList != null) {
            numberOfForecastPoints = countyForecastPointList.size();

            if (numberOfForecastPoints > 0) {
                for (Object[] countyForecastPointRecord : countyForecastPointList) {
                    String forecastPointId = countyForecastPointRecord[0]
                            .toString();

                    for (RiverForecastPoint forecastPoint : forecastPointList) {
                        if (forecastPoint.getId().equals(forecastPointId)) {
                            forecastPointsInCountyList.add(forecastPoint);
                            break;
                        }
                    }
                }
            }

        }
    }

    /**
     * @return The number of river forecast points in this county forecast group
     *         object.
     */
    public int getNumberOfForecastPoints() {
        return forecastPointsInCountyList.size();
    }

    /**
     * Calculates the maximum observed forecast river data for this county
     * group. This determines whether or not the county will be considered in
     * flood and the magnitude/severity of the flooding.
     * 
     * @param
     * @return
     */
    protected void computeCountyMofo() {
        int catval;
        long timeval;
        int max_curobs_cat, max_maxfcst_cat;
        long max_curobs_time, max_maxfcst_time;

        /* get the info for each of the forecast groups */

        /* initialize the group data */

        max_curobs_cat = max_maxfcst_cat = RiverForecastPoint.HydroFloodCategories.NULL_CATEGORY
                .getRank();
        max_curobs_time = max_maxfcst_time = (long) RiverForecastPoint.MISSINGVAL;

        for (RiverForecastPoint forecastPoint : forecastPointsInCountyList) {

            /*
             * check the max current observed category value and omf category.
             * always use the earliest cur observed.
             */

            if (forecastPoint.getCurrentObservationCategory() != RiverForecastPoint.HydroFloodCategories.NULL_CATEGORY
                    .getRank()) {
                catval = forecastPoint.getCurrentObservationCategory();
                timeval = forecastPoint.getCurrentObservation().getValidTime();

                if (catval > max_curobs_cat) {
                    max_curobs_cat = catval;
                    max_curobs_time = timeval;
                } else if (catval == max_curobs_cat) {
                    if (timeval < max_curobs_time
                            || max_curobs_time == RiverForecastPoint.MISSINGVAL) {
                        max_curobs_time = timeval;
                    }
                }
            }

            /*
             * check the max forecast category and omf category. always use the
             * earliest maxfcst
             */

            if (forecastPoint.getMaximumForecastCategory() != RiverForecastPoint.HydroFloodCategories.NULL_CATEGORY
                    .getRank()) {
                catval = forecastPoint.getMaximumForecastCategory();
                timeval = forecastPoint.getMaximumForecast().getValidTime();

                if (catval > max_maxfcst_cat) {
                    max_maxfcst_cat = catval;
                    max_maxfcst_time = timeval;
                } else if (catval == max_maxfcst_cat) {
                    if (timeval < max_maxfcst_time
                            || max_maxfcst_time == RiverForecastPoint.MISSINGVAL) {
                        max_maxfcst_time = timeval;
                    }
                }
            }
        } /* end of loop of fps in group */

        /* load the local variables into the structure */
        this.maxCurrentObservedCategory = max_curobs_cat;
        this.maxCurrentObservedTime = new Date(max_curobs_time);

        this.maxMaxForecastCategory = max_maxfcst_cat;
        this.maxMaxForecastTime = new Date(max_maxfcst_time);

        /*
         * if the cats are equal, use the observed since it is earlier in time.
         */

        if (this.maxCurrentObservedCategory >= this.maxMaxForecastCategory) {
            this.maxOMFCategory = this.maxCurrentObservedCategory;
            this.maxOMFTime = this.maxCurrentObservedTime;
        } else {
            this.maxOMFCategory = this.maxMaxForecastCategory;
            this.maxOMFTime = this.maxMaxForecastTime;
        }
    }

    /**
     * @return the max observed forecast time.
     */
    public Date getMaxOMFTime() {
        return new Date(maxOMFTime.getTime());
    }

    /**
     * @return the max observed forecast flood category.
     */
    public int getMaxOMFCategory() {
        return maxOMFCategory;
    }

}

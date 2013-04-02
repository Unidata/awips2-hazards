package gov.noaa.gsd.uf.common.recommenders.hydro.riverfloodrecommender;

import java.util.Date;
import java.util.List;

/**
 * 
 * Description: Represents a set of river forecast points grouped together
 * according to the river they are on. These can be treated as a set and hazard
 * events can be generated which contain all of the river forecast points in
 * this group, even if individual points are not reaching above flood level.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * June 2011               Bryon.Lawrence      Initial creation
 * 
 * </pre>
 * 
 * @author Bryon.Lawrence
 * @version 1.0
 */
public class RiverForecastGroup {
    /*
     * Static info, loaded in by load_grpdata() in get_fp_grp.c
     */
    /**
     * forecast group id
     */
    private String id;

    /**
     * forecast group name
     */
    private String name;

    /**
     * The river points contained in this group.
     */
    private List<RiverForecastPoint> forecastPointList;

    /**
     * Whether or not to create a hazard containing a river points in this
     * group.
     */
    private boolean recommendAllPointsInGroup;

    /**
     * dynamic info determined from maximum observed forecast data.
     */
    private int maxCurrentObservedCategory;

    private Date maxCurrentObservedTime;

    private int maxMaxForecastCategory;

    private Date maxMaxForecastTime;

    private int maxOMFCategory;

    private Date maxOMFTime;

    /**
     * Whether or not to include this river group in the recommendation.
     */
    private boolean includedInRecommendation;

    /**
     * Create a river forecast group.
     * 
     * @param forecastPointList
     *            - master river forecast point list
     * @param groupRecord
     *            - Information about this river group from the IHFS database
     * @param forecastPointsInGroupList
     *            - List of river forecast points in this group.
     */
    public RiverForecastGroup(final List<RiverForecastPoint> forecastPointList,
            final Object[] groupRecord,
            final List<RiverForecastPoint> forecastPointsInGroupList) {
        loadGroupData(forecastPointList, groupRecord, forecastPointsInGroupList);
    }

    /**
     * Loads forecast point information and IHFS group information into this
     * group object.
     * 
     * @param forecastPointList
     *            The master forecast point list.
     * @param groupRecord
     *            The group information from the IHFS database
     * @param forecastPointsInGroupList
     *            The list of forecast points in this group.
     * 
     * @return
     */
    private void loadGroupData(
            final List<RiverForecastPoint> forecastPointList,
            final Object[] groupRecord,
            final List<RiverForecastPoint> forecastPointsInGroupList) {
        this.id = groupRecord[0].toString();
        this.name = groupRecord[1].toString();
        this.recommendAllPointsInGroup = Boolean.parseBoolean(groupRecord[3]
                .toString());
        this.forecastPointList = forecastPointsInGroupList;
    }

    /**
     * @param
     * @return The number of river stations in this group.
     */
    public int getNumberOfForecastPoints()

    {
        return (forecastPointList != null) ? forecastPointList.size() : 0;
    }

    /**
     * @return the list of river stations in this group.
     */
    public List<RiverForecastPoint> getForecastPointList() {
        return forecastPointList;
    }

    /**
     * Calculates the maximum observed or forecast value for this group.
     * 
     * @param
     * @return
     */
    public void computeGroupMofo() {
        int catval;
        long timeval;
        int max_curobs_cat, max_maxfcst_cat;
        long max_curobs_time, max_maxfcst_time;

        /* get the info for each of the forecast groups */

        /* initialize the group data */

        max_curobs_cat = max_maxfcst_cat = RiverForecastPoint.HydroFloodCategories.NULL_CATEGORY
                .getRank();
        max_curobs_time = max_maxfcst_time = (long) RiverForecastPoint.MISSINGVAL;

        for (RiverForecastPoint forecastPoint : forecastPointList) {

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
     * @return true - all river points are included in the recommended event,
     *         false - only river points above flood should be included
     */
    public boolean isRecommendAllPointsInGroup() {
        return recommendAllPointsInGroup;
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

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
        return includedInRecommendation;
    }

    /**
     * @return the name of this river group
     */
    public String getName() {
        return name;
    }

    /**
     * @return the max observed forecast category of all the river points in
     *         this river group.
     */
    public int getMaxOMFCategory() {
        return maxOMFCategory;
    }

    /**
     * @return the time of the maximum observed forecast data of all the river
     *         points in this river group.
     */
    public Date getMaxOMFTime() {
        return maxOMFTime;
    }

}

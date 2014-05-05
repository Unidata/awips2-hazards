package com.raytheon.uf.common.hazards.hydro;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

import com.google.common.collect.Lists;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

/**
 * 
 * Description: Represents a hydrograph. This is a time series of river data,
 * either observed or forecast.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * June 2011               Bryon.Lawrence    Initial creation
 * March 1, 2013           Bryon.Lawrence    Prep for code review
 * May 1, 2014  3581       bkowal      Relocate to common hazards hydro
 * 
 * </pre>
 * 
 * @author Bryon.Lawrence
 * @version 1.0
 */
public class Hydrograph {

    /**
     * For logging...
     */
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(Hydrograph.class);

    /**
     * Threshold QC value for questionable/bad river data.
     */
    static public final long QUESTIONABLE_BAD_THRESHOLD = 1073741824;

    /**
     * River forecast point identifier
     */
    private String lid;

    /**
     * Physical element
     */
    private String physicalElement;

    /**
     * Type source
     */
    private String typeSource;

    /**
     * Begin time of data time series
     */
    private long obsBeginTime;

    /**
     * End time of data time series
     */
    private long obsEndTime;

    /**
     * Valid time of forecast data
     */
    private long endValidTime;

    /**
     * Basis time of forecast data
     */
    private long basisBTime;

    /**
     * Use latest forecast flag. Determines whether or not an older forecast
     * time series is used.
     */
    private boolean useLatestForecast;

    /**
     * Container for hydro time series data.
     */
    private final List<SHEFObservation> shefHydroDataList;

    /**
     * Flood data access object.
     */
    private IFloodDAO floodDAO;

    /**
     * Default constructor
     */
    public Hydrograph() {
        shefHydroDataList = Lists.newArrayList();
    }

    /**
     * Constructor to create an observed hydrograph.
     * 
     * @param lid
     *            - forecast point id.
     * @param physicalElement
     *            - physical element
     * @param typeSource
     *            - type source
     * @param obsBeginTime
     *            - begin time of observations
     * @param obsEndTime
     *            - end time of observations
     */
    public Hydrograph(String lid, String physicalElement, String typeSource,
            long obsBeginTime, long obsEndTime, IFloodDAO floodDAO) {
        this();
        this.lid = lid;
        this.physicalElement = physicalElement;
        this.typeSource = typeSource;
        this.obsBeginTime = obsBeginTime;
        this.obsEndTime = obsEndTime;
        this.floodDAO = floodDAO;
        buildRiverObsHydrograph();
    }

    /**
     * Constructor to create a forecast hydrograph.
     * 
     * @param lid
     *            - forecast point identifier
     * @param physicalElement
     *            - physical element
     * @param typeSource
     *            - type source
     * @param endValidTime
     *            - forecast valid time
     * @param basisBTime
     *            - forecast basis time
     * @param useLatestForecast
     *            - whether or not to use the latest river forecast
     * @param floodDAO
     *            - flood data accessor object
     */
    public Hydrograph(String lid, String physicalElement, String typeSource,
            long endValidTime, long basisBTime, boolean useLatestForecast,
            IFloodDAO floodDAO) {
        this();
        this.lid = lid;
        this.physicalElement = physicalElement;
        this.typeSource = typeSource;
        this.endValidTime = endValidTime;
        this.basisBTime = basisBTime;
        this.useLatestForecast = useLatestForecast;
        this.floodDAO = floodDAO;
        buildRiverFcstHydrograph();
    }

    /**
     * Builds an observed river hydrograph.
     * 
     * @param
     * @return
     */
    private void buildRiverObsHydrograph() {
        /*
         * Check if the ts is iof length 0. If it is (and this can happen), then
         * assign a typesource.
         */
        if (this.typeSource.length() == 0) {
            String useTS = floodDAO.getBestTS(this.lid, this.physicalElement,
                    "R%", 0);

            if (useTS != null) {
                this.typeSource = useTS;
            } else {
                return;
            }
        }

        /*
         * Get the data for the specified time window and for the determined
         * PEDTSEP entry. Build the where clause depending upon whether
         * considering only passed qc data.
         */

        List<Object[]> observationRecordList = floodDAO
                .getRiverObservedHydrograph(lid, physicalElement, typeSource,
                        obsBeginTime, obsEndTime);

        if (observationRecordList == null || observationRecordList.size() == 0) {
            return;
        }

        for (Object[] obsRecord : observationRecordList) {
            SHEFObservation observation = new SHEFObservation();

            String validTimeString = obsRecord[5].toString();
            long validTime = 0;

            try {
                validTime = floodDAO.getDateFormat().parse(validTimeString)
                        .getTime();
            } catch (ParseException e) {
                /*
                 * Log the error and skip this record.
                 */
                statusHandler
                        .error("Error parsing time: " + validTimeString, e);
                continue;
            }

            observation.setPhysicalElement(obsRecord[1].toString());
            observation.setDuration(Long.parseLong(obsRecord[2].toString()));
            observation.setTypeSource(obsRecord[3].toString());
            observation.setExtremum(obsRecord[4].toString().charAt(0));
            observation.setProbability(-1);
            observation.setBasisTime(0);
            observation.setValue(Double.parseDouble(obsRecord[6].toString()));
            observation.setValidTime(validTime);
            observation.setQualityCode(Long.parseLong(obsRecord[8].toString()));
            observation.setShefQualCode(obsRecord[7].toString());

            this.shefHydroDataList.add(observation);
        }

    }

    /**
     * Constructs a forecast river hydrograph
     * 
     * @param
     * @return
     */
    private void buildRiverFcstHydrograph() {
        buildTSrpfFcstRiv();
    }

    /**
     * Returns the number of data elements in the hydro time series.
     * 
     * @param
     * @return number of data elements in the river time series
     */
    public int getNumberOfShefDataElements() {
        return shefHydroDataList.size();
    }

    /**
     * @return the time series of river data.
     */
    public List<SHEFObservation> getShefHydroDataList() {
        return shefHydroDataList;
    }

    /**
     * Load forecast time series with lid|pe|ts and within current to look
     * forward time frames.
     */
    private void buildTSrpfFcstRiv() {

        Date systemTime = floodDAO.getSystemTime();

        /*
         * Retrieve a list of unique basis times; use descending sort. Only
         * consider forecast data before some ending time, and with some limited
         * basis time ago.
         */
        List<Object[]> basisTimeResults = floodDAO.getRiverForecastBasisTimes(
                lid, physicalElement, typeSource, systemTime, endValidTime,
                basisBTime);
        /*
         * Retrieve the data; the ordering by validtime is important. As before,
         * limit the forecast time valid time window and as needed, the age of
         * the forecast (basistime).
         */
        if (basisTimeResults == null || basisTimeResults.size() == 0) {
            return;
        }

        List<Object[]> forecastResults = floodDAO.getRiverForecastHydrograph(
                lid, physicalElement, typeSource, systemTime, endValidTime,
                basisBTime, useLatestForecast, basisTimeResults);

        boolean[] doKeep = new boolean[forecastResults.size()];

        /*
         * If only retrieving the latest basis time's data or only one basis
         * time was found, then consider all; otherwise, need to adjoin/butt the
         * time series together for the multiple basis times.
         */
        if (useLatestForecast || basisTimeResults.size() <= 1) {
            for (int i = 0; i < forecastResults.size(); ++i) {
                doKeep[i] = true;
            }
        } else {
            try {
                setForecastKeep(basisTimeResults, forecastResults, doKeep);
            } catch (ParseException e) {
                statusHandler.error("Error parsing time", e);
                return;
            }
        }

        /*
         * Now load the values and info to return, knowing which items to keep
         * since all the values have been tagged. First, get the count of the
         * number of values to keep and allocate the data.
         */
        int keepCount = 0;

        for (int i = 0; i < forecastResults.size(); ++i) {
            if (doKeep[i]) {
                keepCount++;
            }
        }

        for (int i = 0; i < forecastResults.size(); ++i) {
            Object[] forecastRecord = forecastResults.get(i);

            if (doKeep[i]) {
                SHEFObservation forecast = new SHEFObservation();

                forecast.setPhysicalElement(forecastRecord[1].toString());
                forecast.setDuration(Integer.parseInt(forecastRecord[2]
                        .toString()));
                forecast.setTypeSource(forecastRecord[3].toString());
                forecast.setExtremum(forecastRecord[4].toString().charAt(0));
                forecast.setProbability(Double.parseDouble(forecastRecord[5]
                        .toString()));
                try {
                    forecast.setValidTime(floodDAO.getDateFormat()
                            .parse(forecastRecord[6].toString()).getTime());
                    forecast.setBasisTime(floodDAO.getDateFormat()
                            .parse(forecastRecord[7].toString()).getTime());
                } catch (ParseException e) {
                    statusHandler.error("Error parsing time", e);
                    continue;
                }

                forecast.setValue(Double.parseDouble(forecastRecord[8]
                        .toString()));
                forecast.setQualityCode(Long.parseLong(forecastRecord[10]
                        .toString()));
                forecast.setShefQualCode(forecastRecord[9].toString());

                shefHydroDataList.add(forecast);
            }
        }
    }

    /**
     * Determine which forecast to keep based on basis times and hydro settings.
     * 
     * @param uniqueBasisList
     *            List of unique forecast basis times
     * @param forecastList
     *            List of forecast data
     * @param doKeep
     *            array of flags indicating whether or not to keep corresponding
     *            forecasts.
     * @return
     * @throws ParseException
     */
    private void setForecastKeep(List<Object[]> uniqueBasisList,
            List<Object[]> forecastList, boolean doKeep[])
            throws ParseException {

        /* Size of forecastList */
        int[] basisIndex = new int[forecastList.size()];

        /* Size of uniqueBasisList */
        long[] tsStartTime = new long[uniqueBasisList.size()];

        /* Size of uniqueBasisList */
        long[] tsEndTime = new long[uniqueBasisList.size()];

        /* Size of uniqueBasisList */
        boolean[] tsFirstCheck = new boolean[uniqueBasisList.size()];

        for (int i = 0; i < tsFirstCheck.length; ++i) {
            tsFirstCheck[i] = false;
        }

        long[] tsBasisTime = new long[uniqueBasisList.size()];

        /*
         * Now loop through the retrieved time series data values and get the
         * start and end times for each of the basis times found.
         */
        for (int i = 0; i < forecastList.size(); ++i) {
            Object[] forecastRecord = forecastList.get(i);
            String forecastBasisTime = forecastRecord[7].toString();

            /*
             * Find out which basis time's time series this value belongs to.
             */
            basisIndex[i] = (int) RiverForecastPoint.MISSINGVAL;

            for (int j = 0; ((j < uniqueBasisList.size() && basisIndex[i] == RiverForecastPoint.MISSINGVAL)); j++) {
                String uniqueBasisTime = uniqueBasisList.get(j)[0].toString();

                if (forecastBasisTime.equals(uniqueBasisTime)) {
                    basisIndex[i] = j;
                }
            }

            if (basisIndex[i] == (int) RiverForecastPoint.MISSINGVAL) {
                statusHandler
                        .debug("Unexpected error assigning basis_index for "
                                + i);
            }

            /*
             * Check if the values constitute the start or end time for the time
             * series and record this times if they do.
             */
            String validTimeString = forecastRecord[6].toString();
            long validTime = floodDAO.getDateFormat().parse(validTimeString)
                    .getTime();

            if (tsFirstCheck[basisIndex[i]]) {
                if (validTime < tsStartTime[basisIndex[i]]) {
                    tsStartTime[basisIndex[i]] = validTime;
                } else if (validTime > tsEndTime[basisIndex[i]]) {
                    tsEndTime[basisIndex[i]] = validTime;
                }
            } else {
                tsStartTime[basisIndex[i]] = validTime;
                tsEndTime[basisIndex[i]] = validTime;
                tsFirstCheck[basisIndex[i]] = true;
            }

        }

        /*
         * For each of the unique basis times, assign the basis time in a
         * convenient array for use in the adjust_started function.
         */
        for (int j = 0; j < uniqueBasisList.size(); ++j) {
            String uniqueBasisTimeString = uniqueBasisList.get(j)[0].toString();
            long basisTime = floodDAO.getDateFormat()
                    .parse(uniqueBasisTimeString).getTime();
            tsBasisTime[j] = basisTime;
        }

        /*
         * Knowing the actual start and end times for the multiple time series,
         * loop thru the time series and adjust the start and end time so that
         * they reflect the time span to use; i.e. there is no overlap. THIS IS
         * THE KEY STEP IN THE PROCESS OF DEFINING AN AGGREGATE VIRTUAL TIME
         * SERIES!!!
         */
        adjustStartEnd(uniqueBasisList, tsBasisTime, tsStartTime, tsEndTime);

        /*
         * Loop through the complete retrieved time series and only keep the
         * value if it lies between the start and end time for this basis time.
         */
        for (int i = 0; i < forecastList.size(); ++i) {
            Object[] forecastRecord = forecastList.get(i);
            String validTimeString = forecastRecord[6].toString();
            long validTime = floodDAO.getDateFormat().parse(validTimeString)
                    .getTime();

            if (validTime >= tsStartTime[basisIndex[i]]
                    && validTime <= tsEndTime[basisIndex[i]]) {
                doKeep[i] = true;
            } else {
                doKeep[i] = false;
            }
        }
    }

    /**
     * This method uses the time series with the latest basis time first, and
     * uses it in its entirety. Then the time series with the next latest basis
     * time is used. If it overlaps portions of the already saved time series,
     * then only that portion which doesn't overlap is used. This process
     * continues until all time series have been considered. In essences, this
     * method adjoins adjacent time series.
     * 
     * @param uniqueBasisList
     *            List of unique forecast basis times
     * @param basisTime
     *            - basis times of forecasts
     * @param startTime
     *            - start times of forecasts
     * @param endTime
     *            - end times of forecasts
     */
    private void adjustStartEnd(List<Object[]> uniqueBasisList,
            long basisTime[], long startTime[], long endTime[]) {
        boolean found = false;
        long tmp_time = 0;
        long full_start_valid_time;
        long full_end_valid_time;
        int cur_index = 0;

        /*
         * Initialize the array to keep track of order of the basis time series
         */
        int[] basisOrder = new int[uniqueBasisList.size()];

        for (int i = 0; i < basisOrder.length; ++i) {
            basisOrder[i] = -1;
        }

        /*
         * Find the order of the time series by their latest basis time. If two
         * time series have the same basis time, use the one that has the
         * earlier starting time. Note that the order is such that the latest
         * basis time is last in the resulting order array.
         */
        for (int i = 0; i < uniqueBasisList.size(); ++i) {
            tmp_time = 0;
            cur_index = 0;

            for (int j = 0; j < uniqueBasisList.size(); j++) {

                /*
                 * Only consider the time series if it hasn't been accounted for
                 * in the order array
                 */
                found = false;
                for (int k = 0; k < i; k++) {
                    if (j == basisOrder[k]) {
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    if (basisTime[j] > tmp_time) {
                        cur_index = j;
                        tmp_time = basisTime[j];
                    }

                    else if (basisTime[j] == tmp_time) {
                        if (startTime[j] < startTime[cur_index]) {
                            cur_index = j;
                            tmp_time = basisTime[j];
                        }
                    }
                }
            }

            basisOrder[i] = cur_index;
        }

        /*
         * do NOT adjust the start and end time of the time series with the
         * latest ending time. loop through all the other time series and adjust
         * their start and end times as necessary so that they do not overlap
         * the time limits of the being-built aggregate time series.
         */

        cur_index = basisOrder[0];
        full_start_valid_time = startTime[cur_index];
        full_end_valid_time = endTime[cur_index];

        for (int i = 1; i < uniqueBasisList.size(); i++) {
            cur_index = basisOrder[i];

            /*
             * each additional time series being considered is checked to see if
             * it falls outside the time window already encompassed by the
             * assembled time series. there are four cases that can occur; each
             * is handled below.
             */

            /*
             * if the basis time series being considered is fully within the
             * time of the already existing time series, then ignore it
             * completely, and reset its times.
             */

            if (startTime[cur_index] >= full_start_valid_time
                    && endTime[cur_index] <= full_end_valid_time) {
                startTime[cur_index] = 0;
                endTime[cur_index] = 0;
            }

            /*
             * if the basis time series being considered covers time both before
             * and after the existing time series, use the portion of it that is
             * before the time series. it is not desirable to use both the
             * before and after portion (this results in a non-contiguous
             * time-series that is weird), and given a choice it is better to
             * use the forecast data early on than the later forecast data, so
             * use the before portion
             */

            else if (startTime[cur_index] <= full_start_valid_time
                    && endTime[cur_index] >= full_end_valid_time) {
                endTime[cur_index] = full_start_valid_time - 1;
                full_start_valid_time = startTime[cur_index];
            }

            /*
             * if the basis time series being considered straddles the beginning
             * or is completely before the existing time series, then use the
             * portion of it that is before the time series.
             */

            else if (startTime[cur_index] <= full_start_valid_time
                    && endTime[cur_index] <= full_end_valid_time) {
                endTime[cur_index] = full_start_valid_time - 1;
                full_start_valid_time = startTime[cur_index];
            }

            /*
             * if the basis time series being considered straddles the end or is
             * completely after the existing time series, then use the portion
             * of it that is after the time series.
             */

            else if (startTime[cur_index] >= full_start_valid_time
                    && endTime[cur_index] >= full_end_valid_time) {
                startTime[cur_index] = full_end_valid_time + 1;
                full_end_valid_time = endTime[cur_index];
            }

        } /* end for loop on the unique ordered basis times */

    }

    /**
     * Finds the maximum value in a time series of river data.
     * 
     * @param
     * @return The maximum river data element
     */
    public SHEFObservation findMaxForecast() {
        double max_value = RiverForecastPoint.MISSINGVAL;
        int max_index = (int) RiverForecastPoint.MISSINGVAL;

        /* just in case */
        if (this.shefHydroDataList.size() == 0) {
            statusHandler.error("ERROR - find_maxfcst called with no records!");
            return null;
        }

        /* loop and get the max */

        for (int i = 0; i < this.shefHydroDataList.size(); i++) {
            if (this.shefHydroDataList.get(i).getValue() > max_value) {
                max_value = this.shefHydroDataList.get(i).getValue();
                max_index = i;
            }
        }

        /* if for some bizarre reason, load the first record */
        if (max_index == RiverForecastPoint.MISSINGVAL) {
            statusHandler.error("ERROR - find_maxfcst couldn't find max?!");
            max_index = 0;
        }

        /* load the record */
        SHEFObservation maxForecastRecord = null;
        try {
            maxForecastRecord = (SHEFObservation) this.shefHydroDataList.get(
                    max_index).clone();
        } catch (CloneNotSupportedException e) {
            statusHandler.error("Could not clone SHEF Observation", e);
        }

        return maxForecastRecord;
    }
}

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
package gov.noaa.gsd.viz.hazards.risecrestfall;

import java.util.ArrayList;
import java.util.List;

import com.raytheon.uf.common.hazards.hydro.HydroConstants;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.core.catalog.DirectDbQuery;
import com.raytheon.uf.viz.core.catalog.DirectDbQuery.QueryLanguage;
import com.raytheon.uf.viz.core.exception.VizException;

/**
 * Utility class for Stage/Discharge conversions.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#  Engineer     Description
 * ------------------------------------------------------------
 * Mar 26, 2015  7205    Robert.Blum  Initial creation
 * 
 * </pre>
 * 
 * @author Robert.Blum
 * @version 1.0
 */

public class StageDischargeUtils {

    private static String previousLid = null;

    private static Rating ratingData = null;

    private static boolean needToFindShiftAmount = false;

    private static String RATING_QUERY = "select lid,stage,discharge from rating where lid=':lid' order by stage asc";

    private static String RATING_SHIFT_QUERY = "select lid,date,shift_amount from ratingshift where lid = ':lid' and active='T' order by date desc";

    static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(StageDischargeUtils.class);

    /**
     * Convert the stage to discharge for the location and stage value passed
     * in.
     * 
     * @param lid
     *            The Location ID
     * @param stage
     *            The Stage Value
     * @return the corresponding discharge
     */
    public static double stage2discharge(String lid, double stage) {
        /*
         * Check to determine if the stage value is missing. If it is then
         * return a flow value of missing.
         */
        if (stage == HydroConstants.MISSING_VALUE) {
            return HydroConstants.RATING_CONVERT_FAILED;
        }

        double discharge = HydroConstants.MISSING_VALUE;

        /*
         * If the lid passed in is NOT the same as the previous lid then copy
         * lid passed in to previous and get the rating curve
         */
        if (!lid.equals(previousLid)) {
            previousLid = lid;

            try {
                if ((ratingData == null)
                        || !ratingData.getLid().equalsIgnoreCase(lid)) {
                    needToFindShiftAmount = true;
                    ratingData = queryRatingData(lid);
                }
            } catch (Exception e) {
                statusHandler.error("Error getting Rating data for " + lid
                        + ": " + e, e);
            }
        }

        /*
         * if the pointer to the head is NULL then that means there is NO rating
         * curve for that location
         */
        if (ratingData == null) {
            return HydroConstants.RATING_CONVERT_FAILED;
        }

        /*
         * If there are less than 2 points (ie. one) then that means there is NO
         * usable rating curve for that location
         */
        if (ratingData.getDischargeValues().size() < 2) {
            return HydroConstants.RATING_CONVERT_FAILED;
        }

        ArrayList<Object[]> ratingShiftArray = null;
        double shiftAmount = 0;
        ArrayList<Double> stageRatingCurve = ratingData.getStageValues();

        /*
         * Determine if there is a shift factor for this rating curve. If there
         * is, then shift each point in the rating curve by this factor.
         */
        if (needToFindShiftAmount) {
            ratingShiftArray = queryRatingShift(lid);

            if ((ratingShiftArray != null) && (ratingShiftArray.size() > 0)) {
                Object[] shiftData = ratingShiftArray.get(0);
                shiftAmount = (Double) shiftData[2];
                double d;
                for (int i = 0; i < stageRatingCurve.size(); i++) {
                    d = stageRatingCurve.get(i);
                    d += shiftAmount;
                    stageRatingCurve.set(i, d);
                }
                ratingData.setStageValues(stageRatingCurve);
            }
            needToFindShiftAmount = false;
        }

        ArrayList<Double> dischargeList = ratingData.getDischargeValues();
        double minStage = stageRatingCurve.get(0);
        double maxStage = stageRatingCurve.get(stageRatingCurve.size() - 1);
        double minDischarge = dischargeList.get(0);
        double maxDischarge = dischargeList.get(dischargeList.size() - 1);

        /*
         * if the stage value passed in is less then the lowest stage in the
         * rating table then extrapolate the discharge
         */
        if (stage < minStage) {
            double nextStage = stageRatingCurve.get(1);
            double stageDifference = nextStage - stage;
            double dischargeDifference = dischargeList.get(1) - minDischarge;
            if (stageDifference == 0) {
                discharge = minDischarge;
            } else {
                discharge = minDischarge
                        - ((dischargeDifference / stageDifference) * (minStage - stage));
            }
        }

        /*
         * if the stage value passed in is greater then the highest stage in the
         * rating table then extrapolate the discharge
         */
        if (stage > maxStage) {
            double prevStage = stageRatingCurve
                    .get(stageRatingCurve.size() - 2);
            double dischargeDifference = maxDischarge
                    - dischargeList.get(dischargeList.size() - 2);
            double stageDifference = maxStage - prevStage;
            if (stageDifference == 0) {
                discharge = maxDischarge;
            } else {
                discharge = maxDischarge
                        + ((dischargeDifference / stageDifference) * (stage - maxStage));
            }
        }

        /*
         * if the stage value passed in is between the lowest and highest stage
         * in the rating table then interpolate the discharge
         */
        if ((stage >= minStage) && (stage <= (maxStage))) {
            double lowerStage = minStage;
            double lowerDischarge = minDischarge;
            for (int i = 1; i < stageRatingCurve.size(); i++) {
                double nextStage = stageRatingCurve.get(i);
                double nextDischarge = dischargeList.get(i);
                if ((stage >= lowerStage) && (stage <= nextStage)) {
                    double dischargeDifference = nextDischarge - lowerDischarge;
                    double stageDifference = nextStage - lowerStage;
                    if (stageDifference == 0) {
                        discharge = lowerDischarge;
                    } else {
                        discharge = lowerDischarge
                                + ((dischargeDifference / stageDifference) * (stage - lowerStage));
                    }
                    break;
                }
                lowerStage = nextStage;
                lowerDischarge = nextDischarge;
            }
        }

        /* If for some reason the discharge is < 0 then return missing */
        if (discharge < 0) {
            return HydroConstants.RATING_CONVERT_FAILED;
        }

        return discharge;
    }

    /**
     * Convert the discharge to stage for the location and discharge value
     * passed in.
     * 
     * @param lid
     *            The Location ID
     * @param stage
     *            The Stage Value
     * @return the corresponding discharge
     */
    public static double discharge2stage(String lid, double discharge) {
        /*
         * Check to see if the discharge value is bad, i.e. missing. If it is
         * bad, then return a stage value of missing.
         */
        if (discharge < 0.0) {
            return HydroConstants.RATING_CONVERT_FAILED;
        }
        double stage = HydroConstants.MISSING_VALUE;
        boolean needToFindShiftAmount = false;

        /*
         * If the lid passed in is NOT the same as the previous lid then copy
         * lid passed in to previous and get the rating curve
         */
        if (!lid.equals(previousLid)) {
            previousLid = lid;
            needToFindShiftAmount = true;

            try {
                if ((ratingData == null)
                        || !ratingData.getLid().equalsIgnoreCase(lid)) {
                    ratingData = queryRatingData(lid);
                    needToFindShiftAmount = true;
                }
            } catch (Exception e) {
                statusHandler.error("Error getting Rating data for " + lid
                        + ": " + e, e);
            }

        }

        /*
         * if the pointer to the head is NULL then that means there is NO rating
         * curve for that location
         */
        if (ratingData == null) {
            return HydroConstants.RATING_CONVERT_FAILED;
        }

        /*
         * If there are less than 2 points (ie. one) then that means there is NO
         * usable rating curve for that location
         */
        if (ratingData.getDischargeValues().size() < 2) {
            statusHandler.info("Rating table has less than 2 points for " + lid
                    + ".");
            return HydroConstants.RATING_CONVERT_FAILED;
        }

        ArrayList<Object[]> ratingShiftArray = null;
        double shiftAmount = 0.0;
        ArrayList<Double> dischargeRatingCurve = ratingData
                .getDischargeValues();

        /*
         * Determine if there is a shift factor for this rating curve. If there
         * is, then shift each point in the rating curve by this factor.
         */
        if (needToFindShiftAmount) {
            ratingShiftArray = queryRatingShift(lid);

            if ((ratingShiftArray != null) && (ratingShiftArray.size() > 0)) {
                Object[] shiftData = ratingShiftArray.get(0);
                shiftAmount = (Double) shiftData[2];
            }
        }

        ArrayList<Double> stageList = ratingData.getStageValues();
        double minDischarge = dischargeRatingCurve.get(0);
        double maxDischarge = dischargeRatingCurve.get(dischargeRatingCurve
                .size() - 1);
        double minStage = stageList.get(0);
        double maxStage = stageList.get(stageList.size() - 1);

        /*
         * if the discharge value passed in is less then the lowest discharge in
         * the rating table then extrapolate the stage
         */
        if (discharge < minDischarge) {
            double nextDischarge = dischargeRatingCurve.get(1);
            double dischargeDifference = nextDischarge - minDischarge;
            double stageDifference = stageList.get(1) - minStage;
            if (dischargeDifference == 0) {
                stage = minStage;
            } else {
                stage = minStage
                        - ((stageDifference / dischargeDifference) * (minDischarge - discharge));
            }
        }

        /*
         * if the discharge value passed in is greater then the highest
         * discharge in the rating table then extrapolate the stage
         */
        if (discharge > maxDischarge) {
            double prevDischarge = dischargeRatingCurve
                    .get(dischargeRatingCurve.size() - 2);
            double dischargeDifference = maxDischarge - prevDischarge;
            double stageDifference = maxStage
                    - stageList.get(stageList.size() - 2);

            if (dischargeDifference == 0) {
                stage = maxStage;
            } else {
                stage = maxStage
                        + ((stageDifference / dischargeDifference) * (discharge - maxDischarge));
            }
        }

        /*
         * If the discharge value passed in is between the lowest and highest
         * discharge in the rating table then interpolate the stage
         */
        if ((discharge <= maxDischarge) && (discharge >= minDischarge)) {
            double lowerStage = minStage;
            double lowerDischarge = minDischarge;

            for (int i = 1; i < dischargeRatingCurve.size(); i++) {
                double nextDischarge = dischargeRatingCurve.get(i);
                double nextStage = stageList.get(i);

                if ((discharge >= lowerDischarge)
                        && (discharge <= nextDischarge)) {
                    double dischargeDifference = nextDischarge - lowerDischarge;
                    double stageDifference = nextStage - lowerStage;

                    if (dischargeDifference == 0) {
                        stage = lowerStage;
                    } else {
                        stage = lowerStage
                                + ((stageDifference / dischargeDifference) * (discharge - lowerDischarge));
                    }
                    break;
                }
                lowerStage = nextStage;
                lowerDischarge = nextDischarge;
            }
        }

        stage += shiftAmount;
        return stage;
    }

    public static boolean checkRatingTable(String lid) {
        boolean retVal = false;
        try {
            if ((ratingData == null)
                    || !ratingData.getLid().equalsIgnoreCase(lid)) {
                ratingData = queryRatingData(lid);
                needToFindShiftAmount = true;
                /*
                 * Check the Rating object for data and return true if data are
                 * available
                 */
                if (ratingData.getStageValues().size() > 2) {
                    retVal = true;
                }
                /* return false if there is no data */
                else {
                    retVal = false;
                }
            } else {
                if (ratingData.getLid().equalsIgnoreCase(lid)) {
                    ratingData = queryRatingData(lid);
                    needToFindShiftAmount = true;
                    if (ratingData.getStageValues().size() > 2) {
                        retVal = true;
                    } else {
                        retVal = false;
                    }
                }
            }

        } catch (Exception e) {
            statusHandler.error("Error getting Rating data for " + lid + ": "
                    + e, e);
        }

        return retVal;
    }

    /**
     * Get the data from the IHFS Rating table for the specified Location Id.
     * 
     * @param lid
     *            The Location ID
     * @return The data from the rating table in IHFS
     * @throws VizException
     */
    private static Rating queryRatingData(String lid) throws VizException,
            NullPointerException {
        /* Query the rating table */
        Rating rating = new Rating(lid);

        List<Object[]> results = DirectDbQuery.executeQuery(
                RATING_QUERY.replace(":lid", lid), HydroConstants.IHFS,
                QueryLanguage.SQL);
        if (results != null) {
            // the Rating constructor already add stage and discharge to it. so
            // clear it...
            rating.getStageValues().clear();
            rating.getDischargeValues().clear();
            for (int i = 0; i < results.size(); i++) {
                Object[] sa = results.get(i);
                if (((sa[1] != null) || (sa[1] != ""))
                        && ((sa[2] != null) || (sa[2] != ""))) {
                    rating.addStage((Double) sa[1]);
                    rating.addDischarge((Double) sa[2]);
                }
            }
        }

        return rating;
    }

    /**
     * Get the data from the IHFS RatingShift table for the specified Location
     * Id.
     * 
     * @param lid
     *            The Location ID
     * @return The data from the ratingShift table in IHFS, null if no data
     *         available
     * @throws VizException
     */
    private static ArrayList<Object[]> queryRatingShift(String lid) {
        /* Query the ratingShift table */
        ArrayList<Object[]> results = null;
        try {
            results = (ArrayList<Object[]>) DirectDbQuery.executeQuery(
                    RATING_SHIFT_QUERY.replace(":lid", lid),
                    HydroConstants.IHFS,
                    QueryLanguage.SQL);
        } catch (Exception e) {
            statusHandler.error("Error getting Rating Shift for " + lid
                    + ": " + e, e);
        }
        return results;
    }
}

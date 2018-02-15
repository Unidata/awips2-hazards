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

import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * Hydrology constants.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ----------  ----------- --------------------------
 * May 08, 2015 6562        Chris.Cody  Initial creation: Restructure River Forecast Points/Recommender
 * Jan 20, 2017 28289       Kevin.Bisanz Add DateFormat for dates to be used in python.
 * Mar 13, 2017 29675       Kevin.Bisanz Remove python date format after refactor.
 * 
 * </pre>
 * 
 * @author Robert.Blum
 * @version 1.0
 */

public class RiverHydroConstants {

    public static final String NEXT = "NEXT";

    /** Rating Conversion failure value */
    public static final int RATING_CONVERT_FAILED = -9999;

    /** Missing Data Value */
    public static final int MISSING_VALUE = -9999;

    public static final double MISSING_VALUE_DOUBLE = -9999;

    public static final String MISSING_VALUE_STRING = "-9999";

    public static final String MISSING_SHEF_QUALITY_CODE = "Z";

    /**
     * Default hours to look for forecast basis time.
     */
    public final static int DEFAULT_OBS_FCST_BASIS_HOURS = 72;

    /**
     * Default number of hours to shift forward flood event end time.
     */
    public final static int DEFAULT_ENDTIME_SHIFT_HOURS = 6;

    /**
     * The default stage window.
     */
    public final static float DEFAULT_STAGE_WINDOW = 0.5f;

    /**
     * The questionable/bad observed river data qc value.
     */
    static public final long QUESTIONABLE_BAD_THRESHOLD = 1073741824;

    /**
     * hydrograph trend descriptors
     */
    public enum HydroGraphTrend {
        RISE, UNCHANGED, FALL, MISSING
    };

    /**
     * Type of data - observation or forecast.
     */
    public enum HydroDataType {
        OBS_DATA, FCST_DATA
    };

    /**
     * Possible flood categories
     */
    public enum HydroFloodCategories {
        NULL_CATEGORY(-1),
        NO_FLOOD_CATEGORY(0),
        MINOR_FLOOD_CATEGORY(1),
        MODERATE_FLOOD_CATEGORY(2),
        MAJOR_FLOOD_CATEGORY(3),
        RECORD_FLOOD_CATEGORY(4);

        /**
         * @param rank
         *            The rank of the flood category.
         */
        private HydroFloodCategories(int rank) {
            this.rank = rank;
        }

        private int rank;

        /**
         * @return the rank of the flood category
         */
        public int getRank() {
            return rank;
        }
    }

    /**
     * ThreadLocal instance of Standard date format for hydro data.
     */
    public static final ThreadLocal<SimpleDateFormat> dateFormat = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            SimpleDateFormat sTemp = new SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss");
            sTemp.setTimeZone(TimeZone.getTimeZone("GMT"));
            return sTemp;
        }
    };

    /**
     * @return the dateFormat
     */
    public static SimpleDateFormat getDateFormat() {
        return dateFormat.get();
    }
}
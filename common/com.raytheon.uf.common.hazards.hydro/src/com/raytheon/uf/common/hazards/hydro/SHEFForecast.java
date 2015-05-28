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

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

/**
 * 
 * Description: Represents a SHEF observation. Hydrometeorological data written
 * to the hydro database are in SHEF format.
 * 
 * This class is not meant to be subclassed. This is a Data-Only object.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 08, 2015 6562       Chris.Cody  Initial creation: Restructure River Forecast Points/Recommender
 * May 28, 2015 7139       Chris.Cody  Add curpp and curpc HydrographPrecip query and processing
 * 
 * </pre>
 * 
 * @author Chris.Cody
 * @version 1.0
 */
public final class SHEFForecast extends SHEFBase {
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(SHEFForecast.class);

    // PE Starts with H
    public static final String TABLE_NAME_HEIGHT = "fcstheight";

    public static final String TABLE_NAME_DISCHARGE = "fcstdischarge";

    public static final String COLUMN_NAME_STRING = "lid, pe, dur, ts, extremum, probability,"
            + " validtime, basistime, value, shef_qual_code, quality_code,"
            + " revision, product_id, producttime, postingtime";

    private final int LID_FIELD_IDX = 0;

    private final int PE_FIELD_IDX = 1;

    private final int DUR_IDX = 2;

    private final int TS_IDX = 3;

    private final int EXTREMUM_IDX = 4;

    private final int PROBABILITY_IDX = 5;

    private final int VALIDTIME_IDX = 6;

    private final int BASISTIME_IDX = 7;

    private final int VALUE_IDX = 8;

    private final int SHEF_QUAL_CODE_IDX = 9;

    private final int QUALITY_CODE_IDX = 10;

    private final int REVISION_IDX = 11;

    private final int PRODUCT_ID_FIELD_IDX = 12;

    private final int PRODUCTTIME_FIELD_IDX = 13;

    private final int POSTINGTIME_FIELD_IDX = 14;

    /**
     * Probability of observation (PROBABILITY) (Forecast only)
     */
    private double probability;

    /**
     * valid time of observation (VALIDTIME) (Forecast only)
     */
    private long validTime;

    /**
     * basis time of observation (BASISTIME) (Forecast only)
     */
    private long basisTime;

    public SHEFForecast() {
        super();
    }

    public SHEFForecast(Object[] queryResult) {
        super();
        if (queryResult != null) {
            int queryResultSize = queryResult.length;
            Object queryValue = null;
            java.sql.Timestamp timestamp = null;
            for (int i = 0; i < queryResultSize; i++) {
                queryValue = queryResult[i];
                if (queryValue == null) {
                    continue;
                }
                switch (i) {
                case LID_FIELD_IDX:
                    this.lid = (String) queryValue;
                    break;
                case PE_FIELD_IDX:
                    this.physicalElement = (String) queryValue;
                    break;
                case DUR_IDX:
                    this.duration = (Integer) queryValue;
                    break;
                case TS_IDX:
                    this.typeSource = (String) queryValue;
                    break;
                case EXTREMUM_IDX:
                    String extremumString = (String) queryValue;
                    if (extremumString != null) {
                        this.extremum = extremumString.charAt(0);
                    }
                    break;
                case PROBABILITY_IDX:
                    Float probFloat = (Float) queryValue;
                    if (probFloat != null) {
                        this.probability = probFloat.doubleValue();
                    }
                    break;
                case VALIDTIME_IDX:
                    timestamp = (java.sql.Timestamp) queryValue;
                    if (timestamp != null) {
                        this.validTime = timestamp.getTime();
                    }
                    break;
                case BASISTIME_IDX:
                    timestamp = (java.sql.Timestamp) queryValue;
                    if (timestamp != null) {
                        this.basisTime = timestamp.getTime();
                    }
                    break;
                case VALUE_IDX:
                    this.value = (Double) queryValue;
                    break;
                case SHEF_QUAL_CODE_IDX:
                    this.shefQualCode = (String) queryValue;
                    break;
                case QUALITY_CODE_IDX:
                    this.qualityCode = (Integer) queryValue;
                    break;
                case REVISION_IDX:
                    this.revision = (Integer) queryValue;
                    break;
                case PRODUCT_ID_FIELD_IDX:
                    this.productId = (String) queryValue;
                    break;
                case PRODUCTTIME_FIELD_IDX:
                    timestamp = (java.sql.Timestamp) queryValue;
                    if (timestamp != null) {
                        this.productTime = timestamp.getTime();
                    }
                    break;
                case POSTINGTIME_FIELD_IDX:
                    timestamp = (java.sql.Timestamp) queryValue;
                    if (timestamp != null) {
                        this.postingTime = timestamp.getTime();
                    }
                    break;
                default:
                    statusHandler
                            .error("SHEFForecast Constructor array out of sync with number of data fields. Unknown field for value "
                                    + (String) queryValue);
                }
            }
        }
    }

    /**
     * @param probability
     *            the probability to set
     */
    public void setProbability(double probability) {
        this.probability = probability;
    }

    /**
     * @return the probability
     */
    public double getProbability() {
        return probability;
    }

    /**
     * @param validTime
     *            the validTime to set
     */
    public void setValidTime(long validTime) {
        this.validTime = validTime;
    }

    /**
     * @return the validTime
     */
    public long getValidTime() {
        return validTime;
    }

    /**
     * @param basisTime
     *            the basisTime to set
     */
    public void setBasisTime(long basisTime) {
        this.basisTime = basisTime;
    }

    /**
     * @return the basisTime
     */
    public long getBasisTime() {
        return basisTime;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.common.hazards.hydro;SHEFBase.#getTime()
     */
    @Override
    public long getTime() {
        return validTime;
    }

}

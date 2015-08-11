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
 * This class represents the River Status (RIVERSTATUS) information for a river
 * forecast point.
 * 
 * This is a Data-Only object.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 08, 2015 6562       Chris.Cody  Initial creation: Restructure River Forecast Points/Recommender
 * Jul 22, 2015 9670       Chris.Cody  Changes for Base database query result numeric casting
 * </pre>
 * 
 * @author Chris.Cody
 */

public class RiverStatus {
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(RiverStatus.class);

    public static final String TABLE_NAME = "RiverStatus";

    public static final String COLUMN_NAME_STRING = "lid, pe, dur, ts, extremum, probability,"
            + " validtime, basistime, value";

    private final int LID_FIELD_IDX = 0;

    private final int PE_FIELD_IDX = 1;

    private final int DUR_IDX = 2;

    private final int TS_IDX = 3;

    private final int EXTREMUM_IDX = 4;

    private final int PROBABILITY_IDX = 5;

    private final int VALIDTIME_IDX = 6;

    private final int BASISTIME_IDX = 7;

    private final int VALUE_IDX = 8;

    /**
     * Forecast Point Identifier (LID)
     */
    protected String lid;

    /**
     * Physical element (PE)
     */
    protected String physicalElement;

    /**
     * Duration of observation (DUR)
     */
    protected int duration;

    /**
     * Type source of observation
     */
    protected String typeSource;

    /**
     * Extremum of observation (e.g. max, min) (EXTREMUM)
     */
    protected char extremum;

    /**
     * Probability of observation (PROBABILITY)
     */
    private double probability;

    /**
     * valid time of observation (VALIDTIME)
     */
    private long validTime;

    /**
     * basis time of observation (BASISTIME)
     */
    private long basisTime;

    /**
     * Observation value (VALUE)
     */
    protected double value;

    /**
     * Default constructor
     */
    public RiverStatus() {
        super();
    }

    public RiverStatus(Object[] queryResult) {
        super();
        if (queryResult != null) {
            int queryResultSize = queryResult.length;
            Object queryValue = null;
            java.util.Date timestampDate = null;
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
                    this.duration = ((Number) queryValue).intValue();
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
                    Float probFloat = ((Number) queryValue).floatValue();
                    if (probFloat != null) {
                        this.probability = probFloat.doubleValue();
                    }
                    break;
                case VALIDTIME_IDX:
                    timestampDate = (java.util.Date) queryValue;
                    if (timestampDate != null) {
                        this.validTime = timestampDate.getTime();
                    }
                    break;
                case BASISTIME_IDX:
                    timestampDate = (java.util.Date) queryValue;
                    if (timestampDate != null) {
                        this.basisTime = timestampDate.getTime();
                    }
                    break;
                case VALUE_IDX:
                    this.value = ((Number) queryValue).doubleValue();
                    break;
                default:
                    statusHandler
                            .error("RiverStatus Constructor array out of sync with number of data fields. Unknown field for value "
                                    + (String) queryValue);
                }
            }
        }
    }

    /**
     * Get Forecast Point Identifier.
     * 
     * @return lid
     */
    public String getLid() {
        return (this.lid);
    }

    /**
     * @return the physical element
     */
    public String getPhysicalElement() {
        return physicalElement;
    }

    /**
     * @return the duration
     */
    public int getDuration() {
        return duration;
    }

    /**
     * @return the typeSource
     */
    public String getTypeSource() {
        return typeSource;
    }

    /**
     * @return the extremum
     */
    public char getExtremum() {
        return extremum;
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
     * @return the validTime
     */
    public long getValidTime() {
        return validTime;
    }

    /**
     * @return the basisTime
     */
    public long getBasisTime() {
        return basisTime;
    }

    /**
     * @return the value
     */
    public double getValue() {
        return value;
    }

}

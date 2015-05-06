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

import org.apache.commons.lang.builder.ToStringBuilder;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

/**
 * This class represents the Historical Crest (CREST) table data for a River
 * forecast point.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 08, 2015 6562       Chris.Cody  Initial creation: Restructure River Forecast Points/Recommender
 * </pre>
 * 
 * @author Chris.Cody
 */

public class CrestHistory {
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(CrestHistory.class);

    public static final String TABLE_NAME = "Crest";

    public static final String COLUMN_NAME_STRING = "lid, q, stage, datcrst, prelim";

    private final int LID_FIELD_IDX = 0;

    private final int Q_FIELD_IDX = 1;

    private final int STAGE_FIELD_IDX = 2;

    private final int DATCRST_FIELD_IDX = 3;

    private final int PRELIM_FIELD_IDX = 4;

    public static String PRELIM_OFFICIAL = "O";

    public static String PRELIM_RECORD = "R";

    /**
     * River station identifier
     */
    private String lid;

    /**
     * Discharge (flow) Rate (Q)
     */
    private int q = RiverHydroConstants.MISSING_VALUE;

    /**
     * Crest Stage (STAGE)
     */
    private double stage = RiverHydroConstants.MISSING_VALUE_DOUBLE;

    /**
     * Date of Crest Value. (DATCRST)
     */
    private long datcrst;

    /**
     * Status of Crest Value. (DATCRST)
     */
    private String prelim;

    /**
     * Default constructor
     */
    public CrestHistory() {
    }

    /**
     * Creates a river forecast point object
     * 
     * @param lid
     *            Associated River Forecast Point Lid (Point ID)
     * @param q
     *            Crest Flow rate (Should be Null if stage has a value)
     * @param stage
     *            Crest Flood Stage (Should be Null if q has a value)
     * @param datcrst
     *            Long data of Crest
     */
    public CrestHistory(String lid, int q, double stage, long datcrst,
            String prelim) {
        this.lid = lid;
        this.q = q;
        this.stage = stage;
        this.datcrst = datcrst;
        this.prelim = prelim;
    }

    /**
     * Query result constructor.
     * 
     * Called from FloodDAO
     * 
     * @param queryResult
     *            Object Array of Query Result Data
     */
    public CrestHistory(Object[] queryResult) {
        if (queryResult != null) {
            int queryResultSize = queryResult.length;
            Object queryValue = null;
            for (int i = 0; i < queryResultSize; i++) {
                queryValue = queryResult[i];
                if (queryValue == null) {
                    continue;
                }
                switch (i) {
                case LID_FIELD_IDX:
                    this.lid = (String) queryValue;
                    break;
                case Q_FIELD_IDX:
                    if (queryValue != null) {
                        this.q = (Integer) queryValue;
                    }
                    break;
                case STAGE_FIELD_IDX:
                    if (queryValue != null) {
                        this.stage = (Double) queryValue;
                    }
                    break;
                case DATCRST_FIELD_IDX:
                    if (queryValue instanceof Date) {
                        this.datcrst = ((java.sql.Date) queryValue).getTime();
                    } else {
                        this.datcrst = (Long) queryValue;
                    }
                    break;
                case PRELIM_FIELD_IDX:
                    this.prelim = (String) queryValue;
                    break;
                default:
                    statusHandler
                            .error("CrestHistory Constructor array out of sync with number of data fields. Unknown field for value "
                                    + (String) queryValue);
                }
            }
        }
    }

    /**
     * Get the identifier of this Crest History
     * 
     * @return lid
     */
    public String getLid() {
        return (this.lid);
    }

    /**
     * Get the Q (Flow Rate) of this Crest History
     * 
     * @return q
     */
    public int getQ() {
        return (this.q);
    }

    /**
     * Get the Stage (Flood Stage) of this Crest History
     * 
     * @return stage
     */
    public double getStage() {
        return (this.stage);
    }

    /**
     * Get the Date of this Crest History
     * 
     * @return datcrst
     */
    public long getDatCrst() {
        return (this.datcrst);
    }

    /**
     * Get the Status (Record or Official) of this Crest History
     * 
     * @return prelim
     */
    public String getPrelim() {
        return (this.prelim);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}

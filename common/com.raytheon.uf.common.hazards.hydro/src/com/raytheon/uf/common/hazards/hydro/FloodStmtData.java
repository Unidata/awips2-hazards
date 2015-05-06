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

import org.apache.commons.lang.builder.ToStringBuilder;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.util.Pair;

/**
 * This class is a data object containing Impacts (FLOODSTMT) table data.
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

public class FloodStmtData {
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(FloodStmtData.class);

    public static final String TABLE_NAME = "FloodStmt";

    public static final String COLUMN_NAME_STRING = "lid, impact_value, statement, rf, datestart, dateend, impact_pe";

    private final int LID_FIELD_IDX = 0;

    private final int IMPACT_VALUE_FIELD_IDX = 1;

    private final int STATEMENT_FIELD_IDX = 2;

    private final int RF_FIELD_IDX = 3;

    private final int DATE_START_FIELD_IDX = 4;

    private final int DATE_END_FIELD_IDX = 5;

    private final int IMPACT_PE_FIELD_IDX = 6;

    /**
     * River station identifier
     */
    private String lid;

    /**
     * Impact Value (IMPACT_VALUE)
     */
    private double impactValue;

    /**
     * Statement (STATEMENT)
     */
    private String statement;

    /**
     * rf Rise/Fall (RF)
     */
    private String rf;

    /**
     * dateStart (DATESTART) Note this is in the form "MM/DD" OR "MM/D"
     */
    private String dateStart;

    /**
     * Parsed int value of datestart Month
     */
    private int monthStart;

    /**
     * Parsed int value of datestart Day
     */
    private int dayStart;

    /**
     * dateEnd (DATEEND) Note this is in the form "MM/DD" OR "MM/D"
     */
    private String dateEnd;

    /**
     * Parsed int value of dateend Month
     */
    private int monthEnd;

    /**
     * Parsed int value of dateend Day
     */
    private int dayEnd;

    /**
     * Impacted Physical Element (IMPACT_PE)
     */
    private String impactPE;

    /**
     * Default constructor
     */
    public FloodStmtData() {
    }

    /**
     * Creates a river forecast point object
     * 
     * @param queryResult
     *            Object Array of Query Result Data
     */
    public FloodStmtData(Object[] queryResult) {
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
                case IMPACT_VALUE_FIELD_IDX:
                    this.impactValue = (Double) queryValue;
                    break;
                case STATEMENT_FIELD_IDX:
                    this.statement = (String) queryValue;
                    break;
                case RF_FIELD_IDX:
                    this.rf = (String) queryValue;
                    break;
                case DATE_START_FIELD_IDX:
                    this.dateStart = (String) queryValue;
                    break;
                case DATE_END_FIELD_IDX:
                    this.dateEnd = (String) queryValue;
                    break;
                case IMPACT_PE_FIELD_IDX:
                    this.impactPE = (String) queryValue;
                    break;
                default:
                    statusHandler
                            .error("RiverPointZoneInfo Constructor array out of sync with number of data fields. Unknown field for value "
                                    + (String) queryValue);
                }
            }

            // Parse Begin Month and Day Values
            // Parse Month
            String[] dateStringArray = null;
            int tempMonth = 0;
            int tempDay = 0;
            if ((this.dateStart != null) && (this.dateStart.length() > 1)) {
                dateStringArray = this.dateStart.split("/");
            }
            if (dateStringArray.length == 2) {
                tempMonth = Integer.parseInt(dateStringArray[0]);
                tempDay = Integer.parseInt(dateStringArray[1]);
                if ((tempMonth >= 1) && (tempMonth <= 12) && (tempDay >= 1)
                        && (tempDay <= 31)) {
                    this.monthStart = tempMonth;
                    this.dayStart = tempDay;
                }
            }
            dateStringArray = this.dateEnd.split("/");
            if (dateStringArray.length == 2) {
                tempMonth = Integer.parseInt(dateStringArray[0]);
                tempDay = Integer.parseInt(dateStringArray[1]);
                if ((tempMonth >= 1) && (tempMonth <= 12) && (tempDay >= 1)
                        && (tempDay <= 31)) {
                    this.monthEnd = tempMonth;
                    this.dayEnd = tempDay;
                }
            }
        }
    }

    /**
     * 
     * @return the identifier of this forecast point
     */
    public String getLid() {
        return (this.lid);
    }

    /**
     * @return the impact state this point resides in
     */
    public double getImpactValue() {
        return (this.impactValue);
    }

    /**
     * @return the statement
     */
    public String getStatement() {
        return (this.statement);
    }

    /**
     * @return the dateStart
     */
    public String getDateStart() {
        return (this.dateStart);
    }

    /**
     * @return the integer Month of the dateStart
     */
    public int getMonthStart() {
        return (this.monthStart);
    }

    /**
     * @return the integer Day of the dateStart
     */
    public int getDayStart() {
        return (this.dayStart);
    }

    /**
     * @return the dateEnd
     */
    public String getDateEnd() {
        return (this.dateEnd);
    }

    /**
     * @return the integer Month of the dateEnd
     */
    public int getMonthEnd() {
        return (this.monthEnd);
    }

    /**
     * @return the integer Day of the dateEnd
     */
    public int getDayEnd() {
        return (this.dayEnd);
    }

    /**
     * @return the impactPE
     */
    public String getImpactPE() {
        return (this.impactPE);
    }

    /**
     * Determine if this is a Rising or Falling measurement.
     * 
     * @return "Rising" or "Falling"
     */
    public String getRiseFallString() {

        // Parse Rise/Fall
        if (this.rf != null) {
            if (this.rf.equals("R") == true) {
                return ("Rising");
            }
        }

        return ("Falling");
    }

    /**
     * Build the Impacts String that is used within the River Pro Flood and
     * Hazard generation Python scripts.
     * 
     * @return Impact data string of the form:
     *         "-MM/DD-MM/DD-Rising||[Statement]" or
     *         "-MM/DD-MM-DD-Falling||[Statement]"
     */
    public String getImpactString() {

        String impactString = "";
        String riseFallString = getRiseFallString();

        if (riseFallString != null) {
            String rfText = "-" + riseFallString + "||";
            impactString = "-" + this.dateStart + "-" + this.dateEnd + rfText
                    + this.statement;
        }

        return (impactString);
    }

    /**
     * Build an Impact Value, Impact String Pair object.
     * 
     * @return
     */
    public Pair<Double, String> getImpactPair() {

        Double value = new Double(this.impactValue);
        String impactString = getImpactString();
        Pair<Double, String> pair = new Pair<>(value, impactString);
        return (pair);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}

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
 * This class represents the Counties (COUNTIES) table data for a River forecast
 * point.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 08, 2015 6562       Chris.Cody  Initial creation: Restructure River Forecast Points/Recommender
 * Feb 19, 2016 15014      Robert.Blum Fixed copy/paste bug.
 * </pre>
 * 
 * @author Chris.Cody
 */

public class CountyStateData {
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(CountyStateData.class);

    public static final String TABLE_NAME = "Counties";

    public static final String QUALIFIED_COLUMN_NAME_STRING = "Counties.state, Counties.county, Counties.countynum, Counties.wfo, Counties.primary_back, Counties.secondary_back";

    private final int LID_FIELD_IDX = 0;

    private final int STATE_FIELD_IDX = 1;

    private final int COUNTY_FIELD_IDX = 2;

    private final int COUNTYNUM_FIELD_IDX = 3;

    private final int WFO_FIELD_IDX = 4;

    private final int PRIMARY_BACK_FIELD_IDX = 5;

    private final int SECONDARY_BACK_FIELD_IDX = 6;

    /**
     * Point Id (LID)
     */
    private String lid;

    /**
     * State Abbreviation (STATE)
     */
    private String state;

    /**
     * County Name (COUNTY)
     */
    private String county;

    /**
     * County Number (code) (COUNTYNUM)
     */
    private String countyNum;

    /**
     * Weather Forecast Office (wfo) (WFO)
     */
    private String wfo;

    /**
     * Primary Backup (PRIMARY_BACK)
     */
    private String primaryBack;

    /**
     * Secondary Backup (SECONDARY_BACK)
     */
    private String secondaryBack;

    /**
     * Default constructor
     */
    public CountyStateData() {
        super();
    }

    public CountyStateData(String lid, String county, String state,
            String countyNum) {
        super();
        this.lid = lid;
        this.county = county;
        this.state = state;
        this.countyNum = countyNum;
    }

    /**
     * Query result constructor.
     * 
     * Called from FloodDAO
     * 
     * @param queryResult
     *            Object Array of Query Result Data
     */
    public CountyStateData(Object[] queryResult) {
        super();
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
                case STATE_FIELD_IDX:
                    this.state = (String) queryValue;
                    break;
                case COUNTY_FIELD_IDX:
                    this.county = (String) queryValue;
                    break;
                case COUNTYNUM_FIELD_IDX:
                    this.countyNum = (String) queryValue;
                    break;
                case WFO_FIELD_IDX:
                    this.wfo = (String) queryValue;
                    break;
                case PRIMARY_BACK_FIELD_IDX:
                    this.primaryBack = (String) queryValue;
                    break;
                case SECONDARY_BACK_FIELD_IDX:
                    this.secondaryBack = (String) queryValue;
                    break;
                default:
                    statusHandler
                            .error("CountyStateData Constructor array out of sync with number of data fields. Unknown field for value "
                                    + (String) queryValue);
                }
            }
        }
    }

    public String getLid() {
        return (this.lid);
    }

    public String getCounty() {
        return (this.county);
    }

    public String getState() {
        return (this.state);
    }

    public String getCountyNum() {
        return (this.countyNum);
    }

    public String getWfo() {
        return (this.wfo);
    }

    public String getPrimaryBack() {
        return (this.primaryBack);
    }

    public String getSecondaryBack() {
        return (this.secondaryBack);
    }

}

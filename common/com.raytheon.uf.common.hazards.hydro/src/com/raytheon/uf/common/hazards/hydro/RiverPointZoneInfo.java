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

/**
 * This class represents the River Zone information (ZONEINFO) for a river
 * forecast point.
 * 
 * This is a Data-Only object.
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

public class RiverPointZoneInfo {
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(RiverPointZoneInfo.class);

    public static final String TABLE_NAME = "ZoneNum";

    public static final String COLUMN_NAME_STRING = "lid, state, zoneNum ";

    private final int LID_FIELD_IDX = 0;

    private final int STATE_FIELD_IDX = 1;

    private final int ZONE_NUM_FIELD_IDX = 2;

    /**
     * River station identifier
     */
    private String lid;

    /**
     * River station state
     */
    private String state;

    /**
     * Zone number
     */
    private String zoneNum;

    /**
     * Default constructor
     */
    public RiverPointZoneInfo() {
    }

    /**
     * Creates a river forecast point object
     * 
     * @param zoneInfo
     *            Zone Information specific to this river point
     */
    public RiverPointZoneInfo(String lid, String state, String zoneNum,
            String descr) {
        this.lid = lid;
        this.state = state;
        this.zoneNum = zoneNum;
    }

    /**
     * Creates a river forecast point object
     * 
     * @param queryResult
     *            Object Array of Query Result Data
     */
    public RiverPointZoneInfo(Object[] queryResult) {
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
                case ZONE_NUM_FIELD_IDX:
                    this.zoneNum = (String) queryValue;
                    break;
                default:
                    statusHandler
                            .error("RiverPointZoneInfo Constructor array out of sync with number of data fields. Unknown field for value "
                                    + (String) queryValue);
                }
            }
        }
    }

    /**
     * 
     * @param
     * @return the identifier of this forecast point
     */
    public String getLid() {
        return (this.lid);
    }

    /**
     * @return the state this point resides in
     */
    public String getState() {
        return (this.state);
    }

    /**
     * @return the zoneNum
     */
    public String getZoneNum() {
        return (this.zoneNum);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}

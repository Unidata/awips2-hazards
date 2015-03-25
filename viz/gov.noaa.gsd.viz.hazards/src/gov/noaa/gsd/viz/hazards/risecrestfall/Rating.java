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

/**
 * Object holds the rating values for the Rise/Crest/Fall editor.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer     Description
 * -----------------------------------------------------------
 * Mar 26, 2015  7205     Robert.Blum  Initial creation
 * 
 * </pre>
 * 
 * @author Robert.Blum
 * @version 1.0
 */

public class Rating {

    static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(Rating.class);

    private String QUERY_SQL = "select lid,stage,discharge from rating where lid =':lid'";

    private String lid = null;
    private ArrayList<Double> stageValues = new ArrayList<Double>();
    private ArrayList<Double> dischargeValues = new ArrayList<Double>();

    public Rating (String lid) {
        this.lid = lid;
        populate(lid);
    }

    private void populate(String lid) {
        try {
            List<Object[]> results = DirectDbQuery.executeQuery(
                    QUERY_SQL.replace(":lid", lid), HydroConstants.IHFS,
                    QueryLanguage.SQL);
            if (results != null) {
                for (int i = 0; i < results.size(); i++) {
                    Object[] sa = results.get(i);
                    if (((sa[1] != null) || (sa[1] != "")) && ((sa[2] != null) || (sa[2] != ""))) {
                        addStage((Double)sa[1]);
                        addDischarge((Double)sa[2]);
                    }
                }
            }
        } catch (Exception e) {
            statusHandler.error("Error getting Rating data for " + lid + ": "
                    + e, e);
        }
    }

    /**
     * @return the lid
     */
    public String getLid() {
        return lid;
    }

    /**
     * @param lid the lid to set
     */
    public void setLid(String lid) {
        this.lid = lid;
    }

    /**
     * @return the stageValues
     */
    public ArrayList<Double> getStageValues() {
        return stageValues;
    }

    /**
     * @param stageValues
     *            the stageValues to set
     */
    public void setStageValues(ArrayList<Double> stageValues) {
        this.stageValues = stageValues;
    }

    /**
     * @return the dischargeValues
     */
    public ArrayList<Double> getDischargeValues() {
        return dischargeValues;
    }

    /**
     * @param dischargeValues
     *            the dischargeValues to set
     */
    public void setDischargeValues(ArrayList<Double> dischargeValues) {
        this.dischargeValues = dischargeValues;
    }

    /**
     * Add a discharge value
     * 
     * @param dischargeValue
     */
    public void addDischarge(double dischargeValue) {
        this.dischargeValues.add(dischargeValue);
    }

    /**
     * Add a stage value
     * 
     * @param stage
     */
    public void addStage(double stageValue) {
        this.stageValues.add(stageValue);
    }

    /**
     * Get the corresponding discharge for the stage value
     * 
     * @param stage
     * @return the discharge corresponding to the stage value
     */
    public double getDischargeForStage(double stage) {
        for (int i = 0; i < this.stageValues.size(); i++) {
            if (this.stageValues.get(i) == stage) {
                return dischargeValues.get(i);
            }
        }
        return HydroConstants.MISSING_VALUE;
    }

    /**
     * Get the corresponding stage for the discharge value
     * 
     * @param discharge
     * @return the stage corresponding to the discharge
     */
    public double getStageForDischarge(double discharge) {
        for (int i = 0; i < this.dischargeValues.size(); i++) {
            if (this.dischargeValues.get(i) == discharge) {
                return stageValues.get(i);
            }
        }
        return HydroConstants.MISSING_VALUE;
    }
}

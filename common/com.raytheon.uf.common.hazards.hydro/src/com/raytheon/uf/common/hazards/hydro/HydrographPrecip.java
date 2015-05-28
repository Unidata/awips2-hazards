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

import java.util.List;

/**
 * 
 * Description: Represents an PRECIP (Precipitation: curpc or curpp) hydrograph.
 * This is a time series of observed precipitation data (SHEFPrecip).
 * 
 * This class does not correspond to any database table. It is a pseudo parent
 * (holder) class for SHEF Precip child data.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 28, 2015 7139       Chris.Cody  Initial creation: Restructure River Forecast Points/Recommender
 * 
 * </pre>
 * 
 * @author Chris.Cody
 * @version 1.0
 */
public class HydrographPrecip extends Hydrograph<SHEFPrecip> {

    /**
     * Observed Begin Time.
     */
    private long obsBeginTime;

    /**
     * Observed End Time.
     */
    private long obsEndTime;

    public HydrographPrecip() {
        super();
    }

    /**
     * Hydrograph Precipitation constructor
     * 
     * @param lid
     *            River Forecast Point identifier
     * @param physicalElement
     *            The SHEF physical element code
     * @param typeSource
     *            The SHEF Type Source code
     * @param obsBeginTime
     *            The lower bound of time window to retrieve observations for
     * @param obsEndTime
     *            The upper bound of the time window to retrieve observations
     *            for.
     * @param shefHydroDataList
     *            List of queried SHEF Precip (curpp or curpc) objects.
     */
    public HydrographPrecip(String lid, String physicalElement,
            String typeSource, long obsBeginTime, long obsEndTime,
            List<SHEFPrecip> shefHydroDataList) {
        super(lid, physicalElement, typeSource, shefHydroDataList);
        this.obsBeginTime = obsBeginTime;
        this.obsEndTime = obsEndTime;
    }

    /**
     * Get Current Start of Observed Time.
     * 
     * @return obsBeginTime
     */
    public long getObsBeginTime() {
        return (this.obsBeginTime);
    }

    /**
     * Get Current End of Observed Time.
     * 
     * @return obsEndTime
     */
    public long getObsEndTime() {
        return (this.obsEndTime);
    }

}

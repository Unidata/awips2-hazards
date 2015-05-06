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

import com.google.common.collect.Lists;

/**
 * 
 * Description: Represents a FORECAST hydrograph. This is a time series of
 * forecast river data (SHEFForecast).
 * 
 * This class does not correspond to any database table. It is a pseudo parent
 * (holder) class for SHEF Forecast child data.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 08, 2015 6562       Chris.Cody  Initial creation: Restructure River Forecast Points/Recommender
 * 
 * </pre>
 * 
 * @author Chris.Cody
 * @version 1.0
 */
public class HydrographForecast extends Hydrograph<SHEFForecast> {

    /**
     * Current System Time
     */
    private long systemTime;

    /**
     * End of Valid time of Forecast data.
     */
    private long endValidTime;

    /**
     * Begin Basis time of Forecast data.
     */
    private long basisBTime;

    /**
     * Use Latest Forecast Data flag.
     */
    private boolean useLatestForecast;

    List<Long> basisTimeList;

    /**
     * Use latest forecast flag. Determines whether or not an older forecast
     * time series is used.
     */

    public HydrographForecast() {
        super();
        this.basisTimeList = Lists.newArrayList();

    }

    public HydrographForecast(String lid, String physicalElement,
            String typeSource, long systemTime, long endValidTime,
            long basisBTime, boolean useLatestForecast,
            List<Long> basisTimeList, List<SHEFForecast> shefHydroDataList) {

        super(lid, physicalElement, typeSource, shefHydroDataList);
        if (basisTimeList != null) {
            this.basisTimeList = basisTimeList;
        } else {
            this.basisTimeList = Lists.newArrayList();
        }

        this.systemTime = systemTime;
        this.endValidTime = endValidTime;
        this.basisBTime = basisBTime;
        this.useLatestForecast = useLatestForecast;
    }

    /**
     * Get Current System Time of Forecast.
     * 
     * @return systemTime
     */
    public long getSystemTime() {
        return (this.systemTime);
    }

    /**
     * Get End of Valid time for Forecast data.
     * 
     * @return endValidTime
     */
    public long getEndValidTime() {
        return (this.endValidTime);
    }

    /**
     * Get Basis time of Forecast data.
     * 
     * @return basisBTime
     */
    public long getBasisBTime() {
        return (this.basisBTime);
    }

    /**
     * Get Use Latest Forecast flag.
     * 
     * @return useLatestForecast
     */
    public boolean getUseLatestForecast() {
        return (this.useLatestForecast);
    }

    /**
     * Get Basis Time List.
     * 
     * @return basisTimeList
     */
    public List<Long> getBasisTimeList() {
        return (this.basisTimeList);
    }

}

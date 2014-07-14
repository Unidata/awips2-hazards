/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package com.raytheon.uf.viz.hazards.sessionmanager.alerts.impl;

import java.util.List;
import java.util.Map;

import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Settings;

/**
 * Description: A strategy for filtering hazard events prior to determining what
 * alerts are needed. This allows for the possibility that alerts should be
 * scheduled based on all events or only the ones corresponding to the current
 * {@link Settings}
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 19, 2013   1325     daniel.s.schaffer@noaa.gov      Initial creation
 * 
 * </pre>
 * 
 * @author daniel.s.schaffer@noaa.gov
 * @version 1.0
 */
public interface IHazardFilterStrategy {

    /**
     * Retrieve the filter for the strategy
     */
    Map<String, List<Object>> getFilter();

}

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

import com.google.common.collect.Maps;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;

/**
 * Description: A {@link IHazardFilterStrategy} that is a "pass through"; allows
 * all {@link IHazardEvent}s
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 19, 2013  1325     daniel.s.schaffer@noaa.gov      Initial creation
 * 
 * </pre>
 * 
 * @author daniel.s.schaffer@noaa.gov
 * @version 1.0
 */
public class AllHazardsFilterStrategy implements IHazardFilterStrategy {

    @Override
    public Map<String, List<Object>> getFilter() {
        return Maps.newHashMap();
    }

}

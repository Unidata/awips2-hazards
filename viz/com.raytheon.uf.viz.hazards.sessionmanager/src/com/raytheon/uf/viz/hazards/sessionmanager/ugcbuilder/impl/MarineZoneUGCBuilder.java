/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package com.raytheon.uf.viz.hazards.sessionmanager.ugcbuilder.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.raytheon.uf.common.dataaccess.geom.IGeometryData;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.viz.hazards.sessionmanager.ugcbuilder.IugcToMapGeometryDataBuilder;

/**
 * 
 * Description: An IUGCBuilder implementation which constructs a list of UGCs
 * from geometry data read from the marinezones table in the maps geodatabase.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 28, 2014            blawrenc    Initial creation
 * Jan 22, 2015 4959       Dan Schaffer Ability to right click to add/remove UGCs from hazards
 * 
 * </pre>
 * 
 * @author blawrenc
 * @version 1.0
 */
public class MarineZoneUGCBuilder implements IugcToMapGeometryDataBuilder {

    @Override
    public Map<String, IGeometryData> ugcsToMapGeometryData(
            Set<IGeometryData> geometryData) {
        Map<String, IGeometryData> result = new HashMap<>();

        for (IGeometryData geoData : geometryData) {
            String zone = geoData.getString(HazardConstants.UGC_ID);
            result.put(zone, geoData);
        }

        return result;
    }

}
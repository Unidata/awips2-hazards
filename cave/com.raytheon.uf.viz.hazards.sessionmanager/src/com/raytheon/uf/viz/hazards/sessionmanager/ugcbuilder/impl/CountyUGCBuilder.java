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

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.raytheon.uf.common.dataaccess.geom.IGeometryData;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.viz.hazards.sessionmanager.ugcbuilder.IUGCBuilder;

/**
 * 
 * Description: An IUGCBuilder implementation which constructs a list of UGCs
 * from geometry data read from the county table in the maps geodatabase.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 28, 2014            blawrenc      Initial creation
 * 
 * </pre>
 * 
 * @author blawrenc
 * @version 1.0
 */

public class CountyUGCBuilder implements IUGCBuilder {

    @Override
    public List<String> buildUGCList(Set<IGeometryData> geometryData) {
        List<String> ugcList = Lists.newArrayList();

        for (IGeometryData geoData : geometryData) {
            String fips = geoData.getString(HazardConstants.UGC_FIPS);
            String state = geoData.getString(HazardConstants.UGC_STATE);
            String ugc = state + "C" + fips.substring(2);
            ugcList.add(ugc);
        }

        return ugcList;
    }
}
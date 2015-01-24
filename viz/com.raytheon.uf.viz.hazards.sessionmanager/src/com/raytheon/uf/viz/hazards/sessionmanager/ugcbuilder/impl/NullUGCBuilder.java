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

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.raytheon.uf.common.dataaccess.geom.IGeometryData;
import com.raytheon.uf.viz.hazards.sessionmanager.ugcbuilder.IugcToMapGeometryDataBuilder;

/**
 * 
 * Description: A null implementation of a {@link IugcToMapGeometryDataBuilder}
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 02, 2014            blawrenc      Initial creation
 * Jan 22, 2015 4959       Dan Schaffer Ability to right click to add/remove UGCs from hazards
 * 
 * </pre>
 * 
 * @author blawrenc
 * @version 1.0
 */

public class NullUGCBuilder implements IugcToMapGeometryDataBuilder {

    @Override
    public Map<String, IGeometryData> ugcsToMapGeometryData(
            Set<IGeometryData> mapGeometryData) {
        return Collections.emptyMap();
    }
}
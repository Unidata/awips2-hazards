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
import com.raytheon.uf.viz.hazards.sessionmanager.ugcbuilder.IUGCBuilder;

/**
 * 
 * Description: A null IUGCBuilder implementation which returns an empty list of
 * UGCs.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 02, 2014            blawrenc      Initial creation
 * 
 * </pre>
 * 
 * @author blawrenc
 * @version 1.0
 */

public class NullUGCBuilder implements IUGCBuilder {

    @Override
    public List<String> buildUGCList(Set<IGeometryData> geometryData) {
        return Lists.newArrayList();
    }
}
/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package com.raytheon.uf.viz.hazards.sessionmanager.product.impl;

import java.util.EnumSet;

import com.raytheon.uf.common.dataplugin.warning.portions.GisUtil.Direction;
import com.raytheon.uf.common.dataplugin.warning.portions.PortionsUtil;
import com.vividsolutions.jts.geom.Geometry;

/**
 * Description: Retrieves {@link Direction}s used in portion of county
 * calculations.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 22, 2014            daniel.s.schaffer@noaa.gov      Initial creation
 * 
 * </pre>
 * 
 * @author daniel.s.schaffer@noaa.gov
 * @version 1.0
 */
class DirectionsRetriever implements IDirectionsRetriever {

    @Override
    public EnumSet<Direction> retrieveDirections(
            Geometry polygonHazardGeometry, PortionsUtil portionsUtil,
            String ugc, Geometry countyGeometry) throws Exception {
        EnumSet<Direction> directionSet;
        directionSet = portionsUtil.getPortions(ugc, countyGeometry,
                polygonHazardGeometry, true);
        return directionSet;
    }

}

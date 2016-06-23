/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package com.raytheon.uf.viz.hazards.sessionmanager;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * Description: Interface describing the methods that must be implemented to act
 * as a provider of spatial context.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Jun 15, 2016   19537    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public interface ISpatialContextProvider {

    /**
     * Get the current center point of the spatial area in lat-lon coordinates.
     * 
     * @return Current center point.
     */
    public Coordinate getLatLonCenterPoint();
}

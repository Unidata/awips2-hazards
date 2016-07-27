/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Evaluation & Decision Support Branch (EDS)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package com.raytheon.uf.viz.hazards.sessionmanager;

import java.util.List;

/**
 * Description: Interface describing the methods that must be implemented to act
 * as a provider of loaded display (viz) resources context.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Jul 27, 2016   19924    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public interface IDisplayResourceContextProvider {

    /**
     * Get the data layer times for the Time Match Basis (TMB) product, if one
     * is being used.
     * 
     * @return Data layer times for the TMB product, if one is in use, otherwise
     *         <code>null</code>.
     */
    public List<Long> getTimeMatchBasisDataLayerTimes();
}

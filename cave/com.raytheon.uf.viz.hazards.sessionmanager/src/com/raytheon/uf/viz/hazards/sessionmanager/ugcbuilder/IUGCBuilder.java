/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package com.raytheon.uf.viz.hazards.sessionmanager.ugcbuilder;

import java.util.List;
import java.util.Set;

import com.raytheon.uf.common.dataaccess.geom.IGeometryData;

/**
 * 
 * Description: Interface implemented by classes which build specific UGC
 * information based on a specific source table in the geodatabase.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 28, 2014            blawrenc    Initial creation
 * 
 * </pre>
 * 
 * @author blawrenc
 * @version 1.0
 */
public interface IUGCBuilder {

    /**
     * Creates a list of UGC information given the set of geometry data objects
     * retrieved from a table in the maps geodatabase.
     * 
     * @param geometryData
     *            The set of geometry data objects from which UGC information
     *            will be derived.
     * @return A list of UGCs.
     */
    public List<String> buildUGCList(Set<IGeometryData> geometryData);
}
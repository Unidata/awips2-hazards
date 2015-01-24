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

import java.util.Map;
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
 * Jan 22, 2015 4959       Dan Schaffer Ability to right click to add/remove UGCs from hazards
 * 
 * </pre>
 * 
 * @author blawrenc
 * @version 1.0
 */
public interface IugcToMapGeometryDataBuilder {

    /**
     * Determine the mapping between UGCs and their associated map
     * {@link IGeometryData}
     * 
     * @param mapGeometryData
     *            the geometryData
     * @return the mapping
     */
    public Map<String, IGeometryData> ugcsToMapGeometryData(
            Set<IGeometryData> mapGeometryData);
}
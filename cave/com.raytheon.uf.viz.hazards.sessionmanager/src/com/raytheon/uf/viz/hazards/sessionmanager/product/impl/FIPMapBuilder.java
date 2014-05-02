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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.raytheon.uf.common.dataaccess.util.DatabaseQueryUtil;
import com.raytheon.uf.common.dataaccess.util.DatabaseQueryUtil.QUERY_MODE;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;

/**
 * Description: Maps FIPS indexes to state abbreviations
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
 * @author TBD_USER
 * @version 1.0
 */
public class FIPMapBuilder {
    private final IUFStatusHandler statusHandler = UFStatus.getHandler(this
            .getClass());

    public Map<String, String> buildFIPStateIndexToAbbreviationMapping() {
        Map<String, String> result = new HashMap<String, String>();
        List<Object[]> queryResult = DatabaseQueryUtil.executeDatabaseQuery(
                QUERY_MODE.MODE_SQLQUERY,
                "select fips, state from mapdata.states;", "maps", "states");
        for (Object[] oneObjectArray : queryResult) {
            try {
                result.put((String) (oneObjectArray[0]),
                        (String) (oneObjectArray[1]));
            } catch (Exception e) {
                statusHandler.handle(Priority.SIGNIFICANT,
                        "Unexpected data from sql query", e);
            }
        }
        return result;
    }
}

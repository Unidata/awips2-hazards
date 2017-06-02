/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package com.raytheon.uf.viz.hazards.sessionmanager.config.types;

import java.util.HashMap;
import java.util.Map;

/**
 * Description: Enumeration of the types of tools available
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 29, 2015 4375       Dan Schaffer initial creation
 * Feb  6, 2015 4375       Fixed bug in deserialization
 * Jun 02, 2015 7138       Robert.Blum  Added new ToolType for Non 
 *                                      Hazard Product Generators.
 * May 02, 2016  16373     mduff        Added PRODUCT_VIEWER and PRODUCT_CORRECTOR.
 * 
 * </pre>
 * 
 * @author daniel.s.schaffer@noaa.gov
 * @version 1.0
 */
public enum ToolType {
    RECOMMENDER, HAZARD_PRODUCT_GENERATOR, NON_HAZARD_PRODUCT_GENERATOR, PRODUCT_VIEWER, PRODUCT_CORRECTOR;

    private static final Map<String, ToolType> stringToEnum = new HashMap<>();

    static {
        for (ToolType value : values()) {
            stringToEnum.put(value.toString(), value);
        }
    }

    public static ToolType fromString(String symbol) {
        return stringToEnum.get(symbol);
    }

    public String asString() {
        return this.name();
    }

}

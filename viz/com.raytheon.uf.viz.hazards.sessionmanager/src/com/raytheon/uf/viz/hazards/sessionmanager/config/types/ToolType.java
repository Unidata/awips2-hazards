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
 * 
 * </pre>
 * 
 * @author daniel.s.schaffer@noaa.gov
 * @version 1.0
 */
public enum ToolType {
    RECOMMENDER("recommender"), PRODUCT_GENERATOR("productGenerator");

    private static final Map<String, ToolType> stringToEnum = new HashMap<>();

    static {
        for (ToolType value : values()) {
            stringToEnum.put(value.toString(), value);
        }
    }

    public static ToolType fromString(String symbol) {
        return stringToEnum.get(symbol);
    }

    private final String toolType;

    private ToolType(String toolType) {
        this.toolType = toolType;
    }

    public String asString() {
        return toolType;
    }

}

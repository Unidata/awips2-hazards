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

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Description: Used for JAXB serialization of {@link ToolType}
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
public class ToolTypeAdapter extends XmlAdapter<String, ToolType> {

    @Override
    public String marshal(ToolType toolType) {
        return toolType.asString();
    }

    @Override
    public ToolType unmarshal(String val) {
        return ToolType.fromString(val);
    }

}

/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.display.action;

import java.io.Serializable;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Description: Action for modifying a storm track.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Dec 13, 2013 2182       daniel.s.schaffer@noaa.gov      Initial creation
 * 
 * </pre>
 * 
 * @author daniel.s.schaffer@noaa.gov
 * @version 1.0
 */
public class ModifyStormTrackAction {

    private Map<String, Serializable> parameters;

    public void setParameters(Map<String, Serializable> parameters) {
        this.parameters = parameters;
    }

    public Map<String, Serializable> getParameters() {
        return parameters;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}

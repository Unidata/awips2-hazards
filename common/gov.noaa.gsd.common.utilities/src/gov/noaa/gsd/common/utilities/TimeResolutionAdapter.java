/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Evaluation & Decision Support Branch (EDS)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.common.utilities;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Description: Used for JAXB serialization of {@link TimeResolution}
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Oct 18, 2016   21873    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class TimeResolutionAdapter extends XmlAdapter<String, TimeResolution> {

    @Override
    public String marshal(TimeResolution timeResolution) {
        return timeResolution.toString();
    }

    @Override
    public TimeResolution unmarshal(String val) {
        return TimeResolution.fromString(val);
    }

}
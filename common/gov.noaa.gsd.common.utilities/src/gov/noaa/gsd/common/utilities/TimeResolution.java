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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Description: Time resolution.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Oct 13, 2016   21873    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public enum TimeResolution {
    SECONDS, MINUTES;

    /**
     * Get the specified time resolution in lower-case. This method is provided
     * to allow this type to be instantiated from a lower-case version of its
     * name, for {@link ObjectMapper}-based JSON deserialization purposes.
     * 
     * @param value
     *            Time resolution to be fetched, but in lower-case.
     * @return Time resolution.
     */
    @JsonCreator
    public static TimeResolution fromString(String value) {
        return valueOf(value.toUpperCase());
    }

    /*
     * Overridden to allow this type to represent itself as lower-case for
     * ObjectMapper-based JSON serialization.
     */
    @Override
    @JsonValue
    public String toString() {
        return super.toString().toLowerCase();
    }
}

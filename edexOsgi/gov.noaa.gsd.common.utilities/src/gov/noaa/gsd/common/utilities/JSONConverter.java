/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.common.utilities;

import java.io.IOException;
import java.util.Date;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * Description: Helper class for serializing/deserializing JSON.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 15, 2013            daniel.s.schaffer@noaa.gov      Initial creation
 * Nov 26, 2013   2366     Chris.Golden                    Moved to non-hazards-specific package.
 * Dec 03, 2013   2182     daniel.s.schaffer@noaa.gov Refactoring - added fromDate to support refactoring
 * </pre>
 * 
 * @author daniel.s.schaffer@noaa.gov
 * @version 1.0
 */
public class JSONConverter extends ObjectMapper {

    public JSONConverter() {
        super();
        this.configure(
                DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public String toJson(Object obj) {
        try {
            return writeValueAsString(obj);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T fromJson(String str, Class<T> clazz) {
        try {
            return readValue(str, clazz);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String fromDate(Date actualDateObject) {
        return Long.toString(actualDateObject.getTime());
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}

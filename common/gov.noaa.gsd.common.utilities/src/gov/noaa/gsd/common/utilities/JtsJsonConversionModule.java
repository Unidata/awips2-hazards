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

import java.io.IOException;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonLocation;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.module.SimpleModule;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.io.WKTWriter;

/**
 * Description: Module to be used by instances of {@link ObjectMapper} to allow
 * the latter to serialize and deserialize JTS {@link Geometry} objects using
 * JSON.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Sep 14, 2016   15934    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class JtsJsonConversionModule extends SimpleModule {

    // Private Static Constants

    /**
     * <a href="https://en.wikipedia.org/wiki/Well-known_text">Well-Known
     * Text</a> reader, used for deserializing geometries. It is thread-local
     * because <code>WKTReader</code> is not explicitly declared to be
     * thread-safe. This class's static methods may be called simultaneously by
     * multiple threads, and each thread must be able to deserialize geometries
     * separately in order to avoid cross-thread pollution.
     */
    private static final ThreadLocal<WKTReader> WKT_READER = new ThreadLocal<WKTReader>() {

        @Override
        protected WKTReader initialValue() {
            return new WKTReader();
        }
    };

    /**
     * <a href="https://en.wikipedia.org/wiki/Well-known_text">Well-Known
     * Text</a> writer, used for serializing geometries. It is thread-local
     * because <code>WKTWriter</code> is not explicitly declared to be
     * thread-safe. This class's static methods may be called simultaneously by
     * multiple threads, and each thread must be able to serialize geometries
     * separately in order to avoid cross-thread pollution.
     */
    private static final ThreadLocal<WKTWriter> WKT_WRITER = new ThreadLocal<WKTWriter>() {

        @Override
        protected WKTWriter initialValue() {
            return new WKTWriter();
        }
    };

    /**
     * Instance of this class to be used in all cases (it is thread-safe once
     * constructed, since all configuration is done in the construction).
     */
    private static final JtsJsonConversionModule INSTANCE = new JtsJsonConversionModule();

    // Public Static Methods

    /**
     * Get the instance to be used. The instance <a
     * href="http://wiki.fasterxml.com/JacksonFAQ">is thread-safe</a>, so it may
     * be used by multiple {@link ObjectMapper} objects across multiple threads.
     * 
     * @return Instance to be used.
     */
    public static JtsJsonConversionModule getInstance() {
        return INSTANCE;
    }

    // Private Constructors

    /**
     * Construct a standard instance.
     */
    private JtsJsonConversionModule() {
        super("Geometry", new Version(1, 0, 0, null));

        /*
         * Configure the module to serialize and deserialize Geometry objects to
         * JSON by using WKT format for the actual serialized version.
         */
        addSerializer(Geometry.class, new JsonSerializer<Geometry>() {

            @Override
            public void serialize(Geometry value, JsonGenerator jsonGenerator,
                    SerializerProvider provider) throws IOException,
                    JsonProcessingException {
                jsonGenerator.writeString(WKT_WRITER.get().write(value));
            }
        });
        addDeserializer(Geometry.class, new JsonDeserializer<Geometry>() {

            @Override
            public Geometry deserialize(JsonParser jsonParser,
                    DeserializationContext context) throws IOException,
                    JsonProcessingException {
                try {
                    return WKT_READER.get().read(jsonParser.getText());
                } catch (ParseException e) {
                    throw new JsonParseException(
                            "could not parse WKT to Geometry", JsonLocation.NA,
                            e);
                }
            }
        });
    }
}

/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Evaluation & Decision Support Branch (EDS)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.common.utilities.geometry;

import java.io.IOException;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Description: Advanced geometry adapter for serializing and deserializing
 * {@link IAdvancedGeometry} objects.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Sep 01, 2016   15934    Chris.Golden Initial creation.
 * Feb 13, 2017   28892    Chris.Golden Changed to use the binary translator
 *                                      to do the marshaling and unmarshaling,
 *                                      as well as using base-64-encoded
 *                                      strings for the serialization
 *                                      instead of JSON in order to reduce
 *                                      the footprint of serialized objects.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class AdvancedGeometryAdapter extends
        XmlAdapter<String, IAdvancedGeometry> {

    @Override
    public IAdvancedGeometry unmarshal(String g) throws IOException {
        return AdvancedGeometryBinaryTranslator
                .deserializeFromCompressedBytesInBase64String(g);
    }

    @Override
    public String marshal(IAdvancedGeometry g) throws IOException {
        return AdvancedGeometryBinaryTranslator
                .serializeToCompressedBytesInBase64String(g);
    }
}

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

import gov.noaa.gsd.common.utilities.JsonConverter;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Description: Advanced geometry adapter for JAXB.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Sep 01, 2016   15934    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class AdvancedGeometryAdapter extends
        XmlAdapter<String, IAdvancedGeometry> {

    @Override
    public IAdvancedGeometry unmarshal(String g) throws Exception {
        return JsonConverter.fromJson(g, IAdvancedGeometry.class);
    }

    @Override
    public String marshal(IAdvancedGeometry g) throws Exception {
        return JsonConverter.toJson(g);
    }
}

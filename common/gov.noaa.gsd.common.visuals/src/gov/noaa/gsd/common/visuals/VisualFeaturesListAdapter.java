/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Evaluation & Decision Support Branch (EDS)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.common.visuals;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Description: Visual features list adapter for JAXB.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Feb 17, 2016   15676    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class VisualFeaturesListAdapter extends
        XmlAdapter<String, VisualFeaturesList> {

    @Override
    public VisualFeaturesList unmarshal(String v) throws Exception {
        return VisualFeaturesListJsonConverter.fromJson(v);
    }

    @Override
    public String marshal(VisualFeaturesList v) throws Exception {
        return VisualFeaturesListJsonConverter.toJson(v);
    }
}

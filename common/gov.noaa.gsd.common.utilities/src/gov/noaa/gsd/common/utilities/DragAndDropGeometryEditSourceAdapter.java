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
 * Used for JAXB serialization of {@link DragAndDropGeometryEditSource}
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Jan 22, 2018   25765    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class DragAndDropGeometryEditSourceAdapter
        extends XmlAdapter<String, DragAndDropGeometryEditSource> {

    @Override
    public String marshal(
            DragAndDropGeometryEditSource dragAndDropGeometryEditSource) {
        return dragAndDropGeometryEditSource.toString();
    }

    @Override
    public DragAndDropGeometryEditSource unmarshal(String val) {
        return DragAndDropGeometryEditSource.fromString(val);
    }

}
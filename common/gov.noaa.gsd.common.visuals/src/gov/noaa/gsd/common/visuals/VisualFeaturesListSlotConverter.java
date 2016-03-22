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

import java.util.ArrayList;
import java.util.List;

import oasis.names.tc.ebxml.regrep.xsd.rim.v4.SlotType;
import oasis.names.tc.ebxml.regrep.xsd.rim.v4.StringValueType;

import com.raytheon.uf.common.registry.ebxml.slots.SlotConverter;

/**
 * Description: Slot converter used to turn lists of {@link VisualFeature}
 * objects into a string for storage in the registry.
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
public class VisualFeaturesListSlotConverter implements SlotConverter {

    // Public Static Constants

    /**
     * Instance to be used for slot conversion.
     */
    public static final VisualFeaturesListSlotConverter INSTANCE = new VisualFeaturesListSlotConverter();

    // Public Methods

    @Override
    public List<SlotType> getSlots(String slotName, Object slotValue)
            throws IllegalArgumentException {
        List<SlotType> slots = new ArrayList<SlotType>();
        if (slotValue instanceof VisualFeaturesList) {
            VisualFeaturesList list = (VisualFeaturesList) slotValue;
            SlotType slot = new SlotType();
            StringValueType type = new StringValueType();
            slot.setName(slotName);
            try {
                type.setStringValue(VisualFeaturesListJsonConverter
                        .toJson(list));
            } catch (Exception e) {
                throw new IllegalStateException(
                        "unexpected error when serializing visual features list",
                        e);
            }
            slot.setSlotValue(type);
            slots.add(slot);
        } else {
            throw new IllegalArgumentException("Object of type "
                    + slotValue.getClass().getName()
                    + " cannot be converted by " + getClass().getName());
        }
        return slots;
    }
}

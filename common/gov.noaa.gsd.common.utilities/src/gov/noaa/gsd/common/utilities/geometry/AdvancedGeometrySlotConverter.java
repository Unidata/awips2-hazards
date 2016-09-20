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

import java.util.ArrayList;
import java.util.List;

import oasis.names.tc.ebxml.regrep.xsd.rim.v4.SlotType;
import oasis.names.tc.ebxml.regrep.xsd.rim.v4.StringValueType;

import com.raytheon.uf.common.registry.ebxml.slots.SlotConverter;

/**
 * Description: Slot converter used to turn {@link IAdvancedGeometry} objects
 * into strings for storage in the registry.
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
public class AdvancedGeometrySlotConverter implements SlotConverter {

    // Public Static Constants

    /**
     * Instance to be used for slot conversion.
     */
    public static final AdvancedGeometrySlotConverter INSTANCE = new AdvancedGeometrySlotConverter();

    // Public Methods

    @Override
    public List<SlotType> getSlots(String slotName, Object slotValue)
            throws IllegalArgumentException {
        List<SlotType> slots = new ArrayList<SlotType>();
        if (slotValue instanceof IAdvancedGeometry) {
            IAdvancedGeometry geometry = (IAdvancedGeometry) slotValue;
            SlotType slot = new SlotType();
            StringValueType type = new StringValueType();
            slot.setName(slotName);
            type.setStringValue(geometry.toString());
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

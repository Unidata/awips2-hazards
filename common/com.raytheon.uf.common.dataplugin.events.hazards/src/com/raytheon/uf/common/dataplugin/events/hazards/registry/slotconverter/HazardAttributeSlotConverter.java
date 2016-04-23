/**
 * This software was developed and / or modified by Raytheon Company,
 * pursuant to Contract DG133W-05-CQ-1067 with the US Government.
 * 
 * U.S. EXPORT CONTROLLED TECHNICAL DATA
 * This software product contains export-restricted data whose
 * export/transfer/disclosure is restricted by U.S. law. Dissemination
 * to non-U.S. persons whether in the United States or abroad requires
 * an export license or other authorization.
 * 
 * Contractor Name:        Raytheon Company
 * Contractor Address:     6825 Pine Street, Suite 340
 *                         Mail Stop B8
 *                         Omaha, NE 68106
 *                         402.291.0100
 * 
 * See the AWIPS II Master Rights File ("Master Rights File.pdf") for
 * further licensing information.
 **/
package com.raytheon.uf.common.dataplugin.events.hazards.registry.slotconverter;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import oasis.names.tc.ebxml.regrep.xsd.rim.v4.BooleanValueType;
import oasis.names.tc.ebxml.regrep.xsd.rim.v4.FloatValueType;
import oasis.names.tc.ebxml.regrep.xsd.rim.v4.IntegerValueType;
import oasis.names.tc.ebxml.regrep.xsd.rim.v4.SlotType;
import oasis.names.tc.ebxml.regrep.xsd.rim.v4.StringValueType;
import oasis.names.tc.ebxml.regrep.xsd.rim.v4.ValueType;

import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardAttribute;
import com.raytheon.uf.common.registry.ebxml.slots.SlotConverter;
import com.vividsolutions.jts.geom.Geometry;

/**
 * 
 * Slot converter used to transform hazard attributes to be stored in the
 * registry
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer      Description
 * ------------ ---------- ------------- --------------------------
 * May 29, 2015    6895    Ben.Phillippe Refactored Hazard Service data access.
 * Mar 03, 2016   16145    Chris.Golden  Added ability to handle Geometry
 *                                       objects found within hazard attributes.
 * Apr 23, 2016   18094    Chris.Golden  Changed to avoid performing any slot
 *                                       conversion on attributes that are not
 *                                       capable of such conversions.
 * </pre>
 * 
 * @author bphillip
 * @version 1.0
 */
public class HazardAttributeSlotConverter implements SlotConverter {

    @SuppressWarnings("unchecked")
    @Override
    public List<SlotType> getSlots(String slotName, Object slotValue)
            throws IllegalArgumentException {
        if (!(slotValue instanceof Set)) {
            throw new IllegalArgumentException("Object of type "
                    + slotValue.getClass().getName()
                    + " cannot be converted by "
                    + HazardAttributeSlotConverter.class.getName());
        }

        Set<HazardAttribute> attributes = (Set<HazardAttribute>) slotValue;
        List<SlotType> slotList = new ArrayList<SlotType>(attributes.size());

        for (HazardAttribute attr : attributes) {
            if (attr.isSlottable() == false) {
                continue;
            }
            Class<?> valueType = attr.getValueType();
            if (valueType == null) {
                ValueType val = getValueType(attr.getValue());
                SlotType slot = new SlotType(attr.getKey(), val);
                slotList.add(slot);
            } else {
                Object valueObject = attr.getValueObject();
                if (valueObject != null) {
                    if (Geometry.class.isAssignableFrom(valueType)) {
                        ValueType val = getValueType(valueObject);
                        SlotType slot = new SlotType(attr.getKey(), val);
                        slotList.add(slot);
                    } else if (HashMap.class.isAssignableFrom(valueType)) {
                        HashMap<String, Object> map = (HashMap<String, Object>) valueObject;
                        for (Entry<String, Object> entry : map.entrySet()) {
                            ValueType val = getValueType(entry.getKey() + "="
                                    + String.valueOf(entry.getValue()));
                            SlotType slot = new SlotType(attr.getKey(), val);
                            slotList.add(slot);
                        }
                    } else {
                        Iterator<Object> iterator = ((Collection<Object>) valueObject)
                                .iterator();
                        while (iterator.hasNext()) {
                            ValueType val = getValueType(iterator.next());
                            SlotType slot = new SlotType(attr.getKey(), val);
                            slotList.add(slot);
                        }
                    }
                }
            }
        }
        return slotList;
    }

    /**
     * Makes a best guess as to the desired type
     * 
     * @param str
     *            The string to transform
     * @return The transformed object
     */
    public static Object determineType(String str) {
        Object retVal = null;

        try {
            retVal = Integer.parseInt(str);
        } catch (NumberFormatException e) {
            try {
                retVal = Long.parseLong(str);
            } catch (NumberFormatException e1) {
                try {
                    retVal = Float.parseFloat(str);
                } catch (NumberFormatException e2) {
                    try {
                        retVal = Boolean.parseBoolean(str);
                    } catch (NumberFormatException e3) {
                        return retVal;
                    }
                }
            }
        }
        return retVal;
    }

    /**
     * Gets a ValueType object for the given value
     * 
     * @param value
     *            The value to get the ValueType object
     * @return The ValueType object
     */
    private ValueType getValueType(Object value) {
        ValueType retVal = null;
        if (value == null) {
            retVal = new StringValueType("");
        } else if (value instanceof String) {
            retVal = new StringValueType((String) value);
        } else if (value instanceof Boolean) {
            retVal = new BooleanValueType((Boolean) value);
        } else if (value instanceof Date) {
            retVal = new IntegerValueType(new BigInteger(String.valueOf(
                    ((Date) value).getTime()).trim()));
        } else if (value instanceof Float) {
            retVal = new FloatValueType(((Float) value).floatValue());
        } else if (value instanceof Double) {
            retVal = new FloatValueType(((Double) value).floatValue());
        } else if (value instanceof Long) {
            retVal = new IntegerValueType(new BigInteger(String.valueOf(value)));
        } else if (value instanceof Integer) {
            retVal = new IntegerValueType((Integer) value);
        } else if (value instanceof Geometry) {
            retVal = new StringValueType(((Geometry) value).toText());
        } else {
            retVal = new StringValueType(value.toString());
        }
        return retVal;
    }
}

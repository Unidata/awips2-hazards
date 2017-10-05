/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Evaluation & Decision Support Branch (EDS)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package com.raytheon.uf.common.dataplugin.events.hazards.event;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.registry.slotconverter.GenericRegistryPropertySlotConverter;
import com.raytheon.uf.common.dataplugin.events.hazards.registry.xmladapters.GenericRegistryPropertyAdapter;
import com.raytheon.uf.common.registry.annotations.RegistryObject;
import com.raytheon.uf.common.registry.annotations.RegistryObjectVersion;
import com.raytheon.uf.common.registry.annotations.SlotAttribute;
import com.raytheon.uf.common.registry.annotations.SlotAttributeConverter;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

import gov.noaa.gsd.common.utilities.Utils;

/**
 * Description: Encapsulation of a generic registry object, suitable for storage
 * in and retrieval from the registry. This is to be used by Python scripts,
 * allowing them to store arbitrary objects (about which the Java framework
 * knows nothing) in the registry.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Oct 02, 2017   38506    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
@XmlRootElement(name = "HazardServicesGenericObject")
@XmlAccessorType(XmlAccessType.NONE)
@DynamicSerialize
@RegistryObject({ HazardConstants.UNIQUE_ID })
@RegistryObjectVersion(value = 1.0f)
public class GenericRegistryObject {

    // Private Variables

    /**
     * Unique identifier.
     */
    @DynamicSerializeElement
    @XmlElement
    @SlotAttribute(HazardConstants.UNIQUE_ID)
    private String uniqueID;

    /**
     * Map of the object's property names to their values.
     * <p>
     * Note: This is a <code>HashMap</code> instead of a <code>Map</code>
     * because JAXB can't handle <code>Map</code> interfaces. (Strangely, it
     * <i>can</i> handle <code>Set</code> interfaces, as is evidenced by
     * <code>HazardEvent</code>'s <code>attributes</code> set -- why is that?)
     * </p>
     */
    @DynamicSerializeElement
    @XmlElement
    @XmlJavaTypeAdapter(GenericRegistryPropertyAdapter.class)
    @SlotAttribute(HazardConstants.GENERIC_PROPERTY)
    @SlotAttributeConverter(GenericRegistryPropertySlotConverter.class)
    private HashMap<String, Serializable> properties = new HashMap<>();

    /**
     * Flag indicating whether or not practice mode is in effect.
     */
    @DynamicSerializeElement
    @XmlAttribute
    @SlotAttribute(HazardConstants.PRACTICE)
    private boolean practice;

    // Public Methods

    /**
     * @return Unique identifier.
     */
    public String getUniqueID() {
        return uniqueID;
    }

    /**
     * @param uniqueID
     *            Unique identifer.
     */
    public void setUniqueID(String uniqueID) {
        this.uniqueID = uniqueID;
    }

    /**
     * @return Properties.
     */
    public Map<String, Serializable> getProperties() {
        return new HashMap<>(properties);
    }

    /**
     * @param properties
     *            Properties.
     */
    public void setProperties(Map<String, Serializable> properties) {
        this.properties = new HashMap<>(properties);
    }

    /**
     * @return <code>true</code> if practice mode, <code>false</code> otherwise.
     */
    public boolean isPractice() {
        return practice;
    }

    /**
     * @param practice
     *            Flag indicating whether practice mode is in effect.
     */
    public void setPractice(boolean practice) {
        this.practice = practice;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((uniqueID == null) ? 0 : uniqueID.hashCode());
        result = prime * result
                + ((properties == null) ? 0 : properties.hashCode());
        result = prime * result + (practice ? 1 : 0);
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other instanceof GenericRegistryObject == false) {
            return false;
        }
        GenericRegistryObject otherGenericObject = (GenericRegistryObject) other;
        if (Utils.equal(uniqueID, otherGenericObject.uniqueID) == false) {
            return false;
        }
        if (Utils.equal(properties, otherGenericObject.properties) == false) {
            return false;
        }
        if (practice != otherGenericObject.practice) {
            return false;
        }
        return true;
    }
}

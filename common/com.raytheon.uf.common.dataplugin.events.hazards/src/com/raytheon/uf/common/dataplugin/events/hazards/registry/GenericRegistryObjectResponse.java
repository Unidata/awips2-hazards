/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Evaluation & Decision Support Branch (EDS)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package com.raytheon.uf.common.dataplugin.events.hazards.registry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.raytheon.uf.common.dataplugin.events.hazards.event.GenericRegistryObject;
import com.raytheon.uf.common.dataplugin.events.hazards.registry.xmladapters.ThrowableXmlAdapter;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

import oasis.names.tc.ebxml.regrep.xsd.rim.v4.RegistryObjectType;

/**
 * 
 * Description: Response used as a return from web service calls involving
 * generic objects.
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
@XmlRootElement(name = "GenericRegistryObjectQueryResult")
@XmlAccessorType(XmlAccessType.NONE)
@DynamicSerialize
public class GenericRegistryObjectResponse {

    // Private Variables

    /**
     * List of generic objects.
     */
    @XmlElement
    @DynamicSerializeElement
    private List<GenericRegistryObject> genericObjects = new ArrayList<>();

    /**
     * List of actual registry objects that encapsulate the generic objects.
     */
    @XmlElement
    @DynamicSerializeElement
    private List<RegistryObjectType> registryObjects = new ArrayList<>();

    /**
     * List of any errors that occurred during the web service call.
     */
    @XmlElement
    @DynamicSerializeElement
    @XmlJavaTypeAdapter(value = ThrowableXmlAdapter.class)
    private List<Throwable> exceptions = new ArrayList<>();

    /**
     * Transient variable used to organize the generic objects.
     */
    @XmlTransient
    private final Map<String, GenericRegistryObject> map = new HashMap<>();

    // Public Methods

    /**
     * Get the map pairing unique identifiers with their generic objects.
     * 
     * @return Map pairing unique identifiers with their generic objects.
     */
    public Map<String, GenericRegistryObject> getMap() {
        for (GenericRegistryObject genericObject : genericObjects) {
            map.put(genericObject.getUniqueID(), genericObject);
        }
        return map;
    }

    /**
     * Merge the specified response with this response.
     * 
     * @param response
     *            Response to merge.
     * @return This object, which is the merge of itself and the other response.
     */
    public GenericRegistryObjectResponse merge(
            GenericRegistryObjectResponse response) {
        genericObjects.addAll(response.getGenericObjects());
        registryObjects.addAll(response.getRegistryObjects());
        exceptions.addAll(response.getExceptions());
        return this;
    }

    /**
     * Get the generic objects that were set via
     * {@link #setGenericObjects(List)}.
     * 
     * @return Generic objects.
     */
    public List<GenericRegistryObject> getGenericObjects() {
        return genericObjects;
    }

    /**
     * Set the generic objects to those specified.
     * 
     * @param genericObjects
     *            Generic objects to be used.
     */
    public void setGenericObjects(List<GenericRegistryObject> genericObjects) {
        this.genericObjects = genericObjects;
    }

    /**
     * Get the registry objects.
     * 
     * @return Registry objects.
     */
    public List<RegistryObjectType> getRegistryObjects() {
        return registryObjects;
    }

    /**
     * Set the registry objects.
     * 
     * @param registryObjects
     *            Registry objects.
     */
    public void setRegistryObjects(List<RegistryObjectType> registryObjects) {
        this.registryObjects = registryObjects;
    }

    /**
     * Get the exceptions.
     * 
     * @return Exceptions.
     */
    public List<Throwable> getExceptions() {
        return exceptions;
    }

    /**
     * Set the exceptions.
     * 
     * @param exceptions
     *            Exceptions.
     */
    public void setExceptions(List<Throwable> exceptions) {
        this.exceptions = exceptions;
    }

    /**
     * Add the specified exception to the list.
     * 
     * @param exception
     *            Exception to be added.
     */
    public void addException(Throwable exception) {
        exceptions.add(exception);
    }

    /**
     * Add the specified exceptions to the list.
     * 
     * @param exceptions
     *            Exceptions to be added.
     */
    public void addExceptions(Collection<Throwable> exceptions) {
        this.exceptions.addAll(exceptions);
    }

    /**
     * Determine whether the result is a success or not.
     * 
     * @return <code>true</code> if the result was a success, <code>false</code>
     *         otherwise.
     */
    public boolean success() {
        return exceptions.isEmpty();
    }
}

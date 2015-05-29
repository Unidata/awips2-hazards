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

import oasis.names.tc.ebxml.regrep.xsd.rim.v4.RegistryObjectType;

import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.collections.HazardHistoryList;
import com.raytheon.uf.common.dataplugin.events.hazards.registry.xmladapters.ThrowableXmlAdapter;

/**
 * 
 * Response used as a return from web service calls
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 29, 2015 6895      Ben.Phillippe Refactored Hazard Service data access
 * 
 * </pre>
 * 
 * @author bphillip
 * @version 1.0
 */
@XmlRootElement(name = "HazardEventQueryResult")
@XmlAccessorType(XmlAccessType.NONE)
public class HazardEventResponse {

    /** The list of hazard events */
    @XmlElement
    private List<HazardEvent> events = new ArrayList<HazardEvent>();

    /** The list of actual registry objects that encapsulate the HazardEvents */
    @XmlElement
    private List<RegistryObjectType> registryObjects = new ArrayList<RegistryObjectType>();

    /** List of any errors that occurred during the web service call */
    @XmlElement
    @XmlJavaTypeAdapter(value = ThrowableXmlAdapter.class)
    private List<Throwable> exceptions = new ArrayList<Throwable>();

    /** Transient variable used to organize the events */
    @XmlTransient
    private Map<String, HazardHistoryList> historyMap = new HashMap<String, HazardHistoryList>();

    /**
     * Creates a new empty HazardEventResponse
     */
    public HazardEventResponse() {

    }

    /**
     * Gets the HazardEvents organized by event ID into HazardHistoryList
     * objects
     * 
     * @return The HazardHistoryList map
     */
    public Map<String, HazardHistoryList> getHistoryMap() {
        for (HazardEvent event : events) {
            String eventID = event.getEventID();
            HazardHistoryList history = historyMap.get(eventID);
            if (history == null) {
                history = new HazardHistoryList();
                historyMap.put(eventID, history);
            }
            history.add(event);
        }
        return historyMap;
    }

    /**
     * Merges a response with this response
     * 
     * @param response
     *            The response to merge
     * @return The merged response
     */
    public HazardEventResponse merge(HazardEventResponse response) {
        this.events.addAll(response.getEvents());
        this.registryObjects.addAll(response.getRegistryObjects());
        this.exceptions.addAll(response.getExceptions());
        return this;
    }

    /**
     * @return the events
     */
    public List<HazardEvent> getEvents() {
        return events;
    }

    /**
     * @param events
     *            the events to set
     */
    public void setEvents(List<HazardEvent> events) {
        this.events = events;
    }

    /**
     * Adds a HazardEvent to this response
     * 
     * @param event
     *            The event to add
     */
    public void addEvent(HazardEvent event) {
        this.events.add(event);
    }

    /**
     * @return the registryObjects
     */
    public List<RegistryObjectType> getRegistryObjects() {
        return registryObjects;
    }

    /**
     * @param registryObjects
     *            the registryObjects to set
     */
    public void setRegistryObjects(List<RegistryObjectType> registryObjects) {
        this.registryObjects = registryObjects;
    }

    /**
     * @return the exceptions
     */
    public List<Throwable> getExceptions() {
        return exceptions;
    }

    /**
     * @param exceptions
     *            the exceptions to set
     */
    public void setExceptions(List<Throwable> exceptions) {
        this.exceptions = exceptions;
    }

    /**
     * Adds a throwable to the list
     * 
     * @param throwable
     *            The throwable to add
     */
    public void addException(Throwable throwable) {
        this.exceptions.add(throwable);
    }

    /**
     * Adds a collection of throwables to the list
     * 
     * @param throwables
     *            The throwables to add
     */
    public void addExceptions(Collection<Throwable> throwables) {
        this.exceptions.addAll(throwables);
    }

    /**
     * @return the success
     */
    public boolean success() {
        return this.exceptions.isEmpty();
    }
}

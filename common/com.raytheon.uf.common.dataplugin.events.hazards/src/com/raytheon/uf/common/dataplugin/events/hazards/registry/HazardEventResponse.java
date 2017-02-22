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

import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.HazardEventManager.Include;
import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.collections.HazardHistoryList;
import com.raytheon.uf.common.dataplugin.events.hazards.registry.xmladapters.ThrowableXmlAdapter;
import com.raytheon.uf.common.serialization.XmlGenericMapAdapter;

/**
 * 
 * Response used as a return from web service calls
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * May 29, 2015 6895      Ben.Phillippe Refactored Hazard Service data access
 * Feb 16, 2017 29138     Chris.Golden  Revamped to slim down the response so
 *                                      that it does not carry extra
 *                                      serialized objects with it that are not
 *                                      needed.
 * </pre>
 * 
 * @author bphillip
 * @version 1.0
 */
@XmlRootElement(name = "HazardEventQueryResult")
@XmlAccessorType(XmlAccessType.NONE)
public class HazardEventResponse {

    // Private Variables

    /**
     * Map of event identifiers to the sizes of their history lists.
     */
    @XmlElement
    @XmlJavaTypeAdapter(XmlGenericMapAdapter.class)
    private Map<String, Integer> historySizeMap = new HashMap<>();

    /**
     * List of hazard events.
     */
    @XmlElement
    private List<HazardEvent> events = new ArrayList<>();

    /**
     * List of actual registry objects that encapsulate the hazard events.
     */
    @XmlElement
    private List<RegistryObjectType> registryObjects = new ArrayList<>();

    /**
     * List of any errors that occurred during the web service call.
     */
    @XmlElement
    @XmlJavaTypeAdapter(value = ThrowableXmlAdapter.class)
    private List<Throwable> exceptions = new ArrayList<>();

    /**
     * Transient variable used to organize the historical versions of the
     * events.
     */
    @XmlTransient
    private final Map<String, HazardHistoryList> historyMap = new HashMap<>();

    /**
     * Transient variable used to organize the latest versions of the events.
     */
    @XmlTransient
    private final Map<String, HazardEvent> latestMap = new HashMap<>();

    /**
     * Transient variable used to indicate which versions of hazard events
     * should be included.
     */
    @XmlTransient
    private final Include include;

    /**
     * Transient variable used to indicate whether the size of the set of hazard
     * events is desired instead of the events themselves.
     */
    @XmlTransient
    private final boolean sizeOnly;

    // Public Static Methods

    /**
     * Create an empty instance that is not intended to include any hazard
     * events or count thereof.
     * 
     * @return New instance.
     */
    public static HazardEventResponse create() {
        return new HazardEventResponse(Include.HISTORICAL_AND_LATEST_EVENTS,
                false);
    }

    /**
     * Create an empty instance that will include all historical and latest
     * versions of events that are subsequently provided to it via
     * {@link #setEvents(List)}.
     * 
     * @return New instance.
     */
    public static HazardEventResponse createIncludingAllHistoricalAndLatest() {
        return new HazardEventResponse(Include.HISTORICAL_AND_LATEST_EVENTS,
                false);
    }

    /**
     * Create an empty instance that includes the specified versions of events
     * that are subsequently provided to it via {@link #setEvents(List)}.
     * 
     * @param include
     *            Versions of hazard events to be included.
     * @return New instance.
     */
    public static HazardEventResponse createIncludingAsSpecified(Include include) {
        return new HazardEventResponse(include, false);
    }

    /**
     * Create an empty instance that includes the size only of the specified
     * versions of events that are subsequently provided to it via
     * {@link #setEvents(List)}.
     * 
     * @param include
     *            Versions of hazard events to be included when counting the
     *            size of the events set.
     * @return New instance.
     */
    public static HazardEventResponse createSizeOnlyIncludingAsSpecified(
            Include include) {
        return new HazardEventResponse(include, true);
    }

    // Private Constructors

    /**
     * Construct a standard instance that will include all historical and latest
     * versions of events that are subsequently provided to it via
     * {@link #setEvents(List)}. Note that this constructor is required for
     * JAXB.
     */
    private HazardEventResponse() {
        this(Include.HISTORICAL_AND_LATEST_EVENTS, false);
    }

    /**
     * Construct a standard instance.
     * 
     * @param include
     *            Indicator of what events to include.
     * @param sizeOnly
     *            Flag indicating whether only the size of the set of events in
     *            the response is to be recorded, or the events themselves.
     */
    private HazardEventResponse(Include include, boolean sizeOnly) {
        this.include = include;
        this.sizeOnly = sizeOnly;
    }

    // Public Methods

    /**
     * Get the latest version of the hazard events associated with their event
     * identifiers.
     * 
     * @return Map of event identifiers to the latest versions of the hazard
     *         events.
     */
    public Map<String, HazardEvent> getLatestMap() {
        for (HazardEvent event : events) {
            latestMap.put(event.getEventID(), event);
        }
        return latestMap;
    }

    /**
     * Get the history lists of the hazard events associated with their event
     * identifiers.
     * 
     * @return Map of event identifiers to the history lists of the hazard
     *         events.
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
     * Get the map of event identifiers to the number of events in their history
     * lists.
     * 
     * @return Map of event identifiers to the number of events in their history
     *         lists.
     */
    public Map<String, Integer> getHistorySizeMap() {
        return historySizeMap;
    }

    /**
     * Merge the specified response with this response.
     * 
     * @param response
     *            Response to merge.
     * @return This object, which is the merge of itself and the other response.
     */
    public HazardEventResponse merge(HazardEventResponse response) {
        this.events.addAll(response.getEvents());
        this.registryObjects.addAll(response.getRegistryObjects());
        this.exceptions.addAll(response.getExceptions());
        return this;
    }

    /**
     * Get the events that were set via {@link #setEvents(List)}, subject to the
     * filtering that said method may have performed.
     * 
     * @return Events.
     */
    public List<HazardEvent> getEvents() {
        return events;
    }

    /**
     * Set the events to those specified. The events that are actually
     * incorporated into this object's list of events will be those that are to
     * be included as per the current {@link Include} setting. Additionally,
     * only the count of the events will be incorporated into this object if the
     * {@link #sizeOnly} flag is set to <code>true</code>.
     * 
     * @param events
     *            Events to set.
     */
    public void setEvents(List<HazardEvent> events) {

        /*
         * If only the size is desired, record the sizes of the history lists
         * that pass through the inclusion filter; otherwise, record the events
         * that pass through the inclusion filter.
         */
        if (sizeOnly) {
            historySizeMap = new HashMap<>(events.size(), 1.0f);
            if ((include == Include.LATEST_EVENTS)
                    || (include == Include.LATEST_OR_MOST_RECENT_HISTORICAL_EVENTS)) {
                for (HazardEvent event : events) {
                    historySizeMap.put(event.getEventID(), 1);
                }
            } else {
                for (HazardEvent event : events) {
                    String eventIdentifier = event.getEventID();
                    if (historySizeMap.containsKey(eventIdentifier)) {
                        historySizeMap.put(eventIdentifier,
                                historySizeMap.get(eventIdentifier) + 1);
                    } else {
                        historySizeMap.put(eventIdentifier, 1);
                    }
                }
            }
        } else {

            /*
             * If both historical and latest events are to be included, just use
             * the list that was passed in; otherwise, if for each event the
             * latest or most recent historical event is wanted (whatever is
             * newest), determine which one is latest for each event identifier,
             * and use those; otherwise, filter out those events that are not
             * latest or are not historical, depending upon which is wanted.
             */
            if (include == Include.HISTORICAL_AND_LATEST_EVENTS) {
                this.events = events;
            } else if (include == Include.LATEST_OR_MOST_RECENT_HISTORICAL_EVENTS) {
                Map<String, HazardEvent> latestEventsForEventIdentifiers = new HashMap<>(
                        events.size(), 1.0f);
                for (HazardEvent event : events) {
                    HazardEvent lastEvent = latestEventsForEventIdentifiers
                            .get(event.getEventID());
                    if ((lastEvent == null)
                            || (event.getInsertTime().compareTo(
                                    lastEvent.getInsertTime()) > 0)) {
                        latestEventsForEventIdentifiers.put(event.getEventID(),
                                event);
                    }
                }
                this.events = new ArrayList<>(
                        latestEventsForEventIdentifiers.values());
            } else {
                boolean wantLatest = (include == Include.LATEST_EVENTS);
                this.events = new ArrayList<>(events.size());
                for (HazardEvent event : events) {
                    if (event.isLatestVersion() == wantLatest) {
                        this.events.add(event);
                    }
                }
            }
        }
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

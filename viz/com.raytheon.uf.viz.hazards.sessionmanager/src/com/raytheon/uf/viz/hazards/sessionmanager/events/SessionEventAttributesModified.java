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
package com.raytheon.uf.viz.hazards.sessionmanager.events;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.raytheon.uf.viz.hazards.sessionmanager.ISessionNotification;
import com.raytheon.uf.viz.hazards.sessionmanager.events.impl.ObservedHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.IOriginator;

/**
 * A Notification that will be sent out through the SessionManager to notify all
 * components that one or more attributes of an event in the session have
 * changed.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Jun 11, 2013 1257       bsteffen     Initial creation
 * Apr 23, 2014 2925       Chris.Golden Changed to attributes (plural), and added
 *                                      set of attributes that actually changed
 *                                      so that the changes can be pinpointed by
 *                                      receivers of this notification.
 * Nov 02, 2016 26024     Chris.Golden  Added old attributes to the payload so
 *                                      that notifications can be queried to
 *                                      determine what the old values were.
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */

public class SessionEventAttributesModified extends SessionEventModified
        implements ISessionNotification {

    // Private Variables

    /**
     * Map of attribute names to their new values. Values may be
     * <code>null</code> if the attributes have been removed.
     */
    private final Map<String, Serializable> attributes;

    /**
     * As with {@link #attributes}, but holding the old values of the attributes
     * (prior to the changes that triggered this notification). Values may be
     * <code>null</code> if they were previously nonexistent.
     */
    private final Map<String, Serializable> oldAttributes;

    // Public Constructors

    /**
     * Construct an instance to serve as a notification that a single attribute
     * changed.
     * 
     * @param eventManager
     *            Manager of the hazard event that was modified.
     * @param event
     *            Event that was modified.
     * @param attributeKey
     *            Key of the attribute that changed.
     * @param attributeValue
     *            New value of the attribute that changed; if <code>null</code>,
     *            the attribute has been removed.
     * @param attributeOldValue
     *            Old value of the attribute that changed; if <code>null</code>,
     *            the attribute has been added.
     * @param originator
     *            Originator of the change.
     */
    public SessionEventAttributesModified(
            ISessionEventManager<ObservedHazardEvent> eventManager,
            ObservedHazardEvent event, String attributeKey,
            Serializable attributeValue, Serializable oldAttributeValue,
            IOriginator originator) {
        super(eventManager, event, originator);
        Map<String, Serializable> attributes = new HashMap<>(1, 1.0f);
        attributes.put(attributeKey, attributeValue);
        this.attributes = Collections.unmodifiableMap(attributes);
        Map<String, Serializable> oldAttributes = new HashMap<>(1, 1.0f);
        oldAttributes.put(attributeKey, oldAttributeValue);
        this.oldAttributes = Collections.unmodifiableMap(oldAttributes);
    }

    /**
     * Construct an instance to serve as a notification that multiple attributes
     * changed.
     * 
     * @param eventManager
     *            Manager of the hazard event that was modified.
     * @param event
     *            Event that was modified.
     * @param attributes
     *            Map of attributes that changed with their new values. Values
     *            may be <code>null</code> if attributes have been removed.
     * @param oldAttributes
     *            Map of attributes that changed with their old values. Values
     *            may be <code>null</code> if attributes have been added.
     * @param originator
     *            Originator of the change.
     */
    public SessionEventAttributesModified(
            ISessionEventManager<ObservedHazardEvent> eventManager,
            ObservedHazardEvent event, Map<String, Serializable> attributes,
            Map<String, Serializable> oldAttributes, IOriginator originator) {
        super(eventManager, event, originator);
        this.attributes = Collections.unmodifiableMap(attributes);
        this.oldAttributes = Collections.unmodifiableMap(oldAttributes);
    }

    // Public Methods

    /**
     * Determine whether or not the specified attribute was changed.
     * 
     * @param key
     *            Key of the attribute to be checked.
     * @return <code>true</code> if the value associated with the attribute was
     *         changed, <code>false</code> otherwise.
     */
    public boolean containsAttribute(String key) {
        return attributes.keySet().contains(key);
    }

    /**
     * Get the attributes that changed.
     * 
     * @return Keys of the attributes that changed.
     */
    public Set<String> getAttributeKeys() {
        return attributes.keySet();
    }

    /**
     * Get the new value of the specified attribute.
     * 
     * @param key
     *            Key of the attribute for which to fetch the value.
     * @return Value of the attribute; may be <code>null</code> if the attribute
     *         has been removed.
     */
    public Serializable getAttribute(String key) {
        return attributes.get(key);
    }

    /**
     * Get the old value of the specified attribute.
     * 
     * @param key
     *            Key of the attribute for which to fetch the old value.
     * @return Old value of the attribute; may be <code>null</code> if the
     *         attribute has been added.
     */
    public Serializable getOldAttribute(String key) {
        return oldAttributes.get(key);
    }
}

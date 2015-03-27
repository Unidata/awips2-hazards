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
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
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
 * Apr 10, 2015 6898       Chris.Cody   Refactored async messaging
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */

public class SessionEventAttributesModified extends SessionEventModified {

    private final Map<String, Serializable> attributes;

    public SessionEventAttributesModified(IHazardEvent event,
            String attributeKey, Serializable attributeValue,
            IOriginator originator) {
        super(event, originator);
        Map<String, Serializable> attributes = Maps.newHashMap();
        attributes.put(attributeKey, attributeValue);
        this.attributes = Collections.unmodifiableMap(attributes);
    }

    public SessionEventAttributesModified(IHazardEvent event,
            Map<String, Serializable> attributes, IOriginator originator) {
        super(event, originator);
        this.attributes = Collections.unmodifiableMap(attributes);
    }

    public boolean containsAttribute(String key) {
        return attributes.keySet().contains(key);
    }

    public Set<String> getAttributeKeys() {
        return attributes.keySet();
    }

    public Serializable getAttribute(String key) {
        return attributes.get(key);
    }

    public Map<String, Serializable> getAttributeMap() {
        return this.attributes;
    }

}

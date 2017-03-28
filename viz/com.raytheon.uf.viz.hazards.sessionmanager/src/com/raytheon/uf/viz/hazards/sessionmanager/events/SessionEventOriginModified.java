/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Evaluation & Decision Support Branch (EDS)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package com.raytheon.uf.viz.hazards.sessionmanager.events;

import com.raytheon.uf.viz.hazards.sessionmanager.ISessionNotification;
import com.raytheon.uf.viz.hazards.sessionmanager.events.impl.ObservedHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.IOriginator;

/**
 * Description: Notification that will be sent out through the SessionManager to
 * notify all components that the origin of a particular hazard event has
 * changed.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Mar 28, 2017   32487    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class SessionEventOriginModified extends SessionEventModified implements
        ISessionNotification {

    public enum Element {
        USER_NAME, WORKSTATION, SITE_IDENTIFIER
    }

    private final Element element;

    public SessionEventOriginModified(
            ISessionEventManager<ObservedHazardEvent> eventManager,
            ObservedHazardEvent event, Element element, IOriginator originator) {
        super(eventManager, event, originator);
        this.element = element;
    }

    public Element getElement() {
        return element;
    }
}

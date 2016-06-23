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

/**
 * Description: Notification that will be sent out through the SessionManager to notify all
 * components that the visual features associated with a particular hazard event have
 * changed.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Mar 01, 2016   15676    Chris.Golden Initial creation.
 * Jun 23, 2016   19537    Chris.Golden Changed to use single list of visual features.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */

import java.util.Collections;
import java.util.Set;

import com.raytheon.uf.viz.hazards.sessionmanager.ISessionNotification;
import com.raytheon.uf.viz.hazards.sessionmanager.events.impl.ObservedHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.IOriginator;

public class SessionEventVisualFeaturesModified extends SessionEventModified
        implements ISessionNotification {

    private final Set<String> visualFeatureIdentifiers;

    public SessionEventVisualFeaturesModified(
            ISessionEventManager<ObservedHazardEvent> eventManager,
            ObservedHazardEvent event, Set<String> visualFeatureIdentifiers,
            IOriginator originator) {
        super(eventManager, event, originator);
        this.visualFeatureIdentifiers = (visualFeatureIdentifiers == null ? Collections
                .<String> emptySet() : Collections
                .unmodifiableSet(visualFeatureIdentifiers));
    }

    public Set<String> getVisualFeatureIdentifiers() {
        return visualFeatureIdentifiers;
    }
}

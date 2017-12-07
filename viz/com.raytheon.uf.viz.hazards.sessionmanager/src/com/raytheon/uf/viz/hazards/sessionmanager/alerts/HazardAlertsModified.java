/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package com.raytheon.uf.viz.hazards.sessionmanager.alerts;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.raytheon.uf.viz.hazards.sessionmanager.ISessionNotification;

import gov.noaa.gsd.common.utilities.IMergeable;
import gov.noaa.gsd.common.utilities.MergeResult;

/**
 * Description: A {@link ISessionNotification} indicating that the state of
 * hazard alerts has changed and making available the active alerts.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Jul 19, 2013   1325     daniel.s.schaffer@noaa.gov      Initial creation
 * Sep 27, 2017  38072     Chris.Golden Implemented merge() method.
 * Dec 07, 2017  41886     Chris.Golden Removed Java 8/JDK 1.8 usage.
 * </pre>
 * 
 * @author daniel.s.schaffer@noaa.gov
 * @version 1.0
 */
public class HazardAlertsModified implements ISessionNotification {

    // Private Variables

    /**
     * Active alerts.
     */
    private final List<IHazardAlert> activeAlerts;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param activeAlerts
     *            Active alerts.
     */
    public HazardAlertsModified(List<IHazardAlert> activeAlerts) {
        this.activeAlerts = (activeAlerts instanceof ImmutableList
                ? activeAlerts : ImmutableList.copyOf(activeAlerts));
    }

    // Public Constructors

    /**
     * Get the active alerts. Note that the returned list is not modifiable.
     * 
     * @return Active alerts.
     */
    public List<IHazardAlert> getActiveAlerts() {
        return activeAlerts;
    }

    @Override
    public MergeResult<? extends ISessionNotification> merge(
            ISessionNotification original, ISessionNotification modified) {
        return IMergeable.Helper.getFailureResult();
    }
}

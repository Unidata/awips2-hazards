/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package com.raytheon.uf.viz.hazards.sessionmanager.alerts.impl;

import java.util.Date;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.alerts.HazardEventExpirationConsoleTimer;
import com.raytheon.uf.viz.hazards.sessionmanager.alerts.HazardEventExpirationSpatialTimer;
import com.raytheon.uf.viz.hazards.sessionmanager.alerts.HazardEventExpirationTimer;
import com.raytheon.uf.viz.hazards.sessionmanager.alerts.IHazardEventAlert;
import com.raytheon.uf.viz.hazards.sessionmanager.alerts.IHazardEventExpirationAlert;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.types.HazardAlertTimerConfigCriterion;

/**
 * Description: A factory that builds {@link IHazardEventAlert}s based on given
 * {@link HazardAlertTimerConfigCriterion} and a given {@link IHazardEvent}
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 19, 2013  1325      daniel.s.schaffer@noaa.gov      Initial creation
 * 
 * </pre>
 * 
 * @author daniel.s.schaffer@noaa.gov
 * @version 1.0
 */
public class HazardEventExpirationAlertFactory {

    public List<IHazardEventAlert> createAlerts(
            HazardAlertTimerConfigCriterion alertCriterion,
            IHazardEvent hazardEvent) {

        List<IHazardEventAlert> result = Lists.newArrayList();

        Set<HazardAlertTimerConfigCriterion.Location> locations = alertCriterion
                .getLocations();
        for (HazardAlertTimerConfigCriterion.Location location : locations) {

            switch (location) {

            case CONSOLE:
                HazardEventExpirationTimer consoleAlert = buildConsoleAlert(
                        alertCriterion, hazardEvent);
                result.add(consoleAlert);
                break;

            case SPATIAL:
                HazardEventExpirationSpatialTimer spatialAlert = buildSpatialAlert(
                        alertCriterion, hazardEvent);
                result.add(spatialAlert);
                break;
            }
        }

        return result;
    }

    private HazardEventExpirationSpatialTimer buildSpatialAlert(
            HazardAlertTimerConfigCriterion alertCriterion,
            IHazardEvent hazardEvent) {
        HazardEventExpirationSpatialTimer spatialAlert = new HazardEventExpirationSpatialTimer(
                hazardEvent.getEventID(), alertCriterion);
        computeActivationTime(alertCriterion, hazardEvent, spatialAlert);
        return spatialAlert;
    }

    private HazardEventExpirationTimer buildConsoleAlert(
            HazardAlertTimerConfigCriterion alertCriterion,
            IHazardEvent hazardEvent) {
        HazardEventExpirationConsoleTimer consoleAlert = new HazardEventExpirationConsoleTimer(
                hazardEvent.getEventID(), alertCriterion);
        computeActivationTime(alertCriterion, hazardEvent, consoleAlert);
        return consoleAlert;
    }

    private void computeActivationTime(
            HazardAlertTimerConfigCriterion alertCriterion,
            IHazardEvent hazardEvent, IHazardEventExpirationAlert alert) {
        Long timeBeforeExpiration = alertCriterion.getMillisBeforeExpiration();

        /**
         * TODO The product generator needs to be modified to store this as a
         * Date
         */
        Long hazardExpiration = (Long) hazardEvent
                .getHazardAttribute(HazardConstants.EXPIRATIONTIME);
        alert.setHazardExpiration(new Date(hazardExpiration));

        Long activationTimeInMillis = hazardExpiration - timeBeforeExpiration;
        alert.setActivationTime(new Date(activationTimeInMillis));
    }
}

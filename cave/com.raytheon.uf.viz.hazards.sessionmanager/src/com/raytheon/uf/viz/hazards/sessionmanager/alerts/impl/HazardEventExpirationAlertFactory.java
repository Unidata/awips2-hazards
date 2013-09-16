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
import com.raytheon.uf.viz.hazards.sessionmanager.alerts.HazardEventExpirationPopUpAlert;
import com.raytheon.uf.viz.hazards.sessionmanager.alerts.HazardEventExpirationSpatialTimer;
import com.raytheon.uf.viz.hazards.sessionmanager.alerts.IHazardEventAlert;
import com.raytheon.uf.viz.hazards.sessionmanager.alerts.IHazardEventExpirationAlert;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.types.HazardEventExpirationAlertConfigCriterion;

/**
 * Description: A factory that builds {@link IHazardEventAlert}s based on given
 * {@link HazardEventExpirationAlertConfigCriterion} and a given
 * {@link IHazardEvent}
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
            HazardEventExpirationAlertConfigCriterion alertCriterion,
            IHazardEvent hazardEvent) {

        List<IHazardEventAlert> result = Lists.newArrayList();

        Set<HazardEventExpirationAlertConfigCriterion.Manifestation> locations = alertCriterion
                .getManifestations();
        for (HazardEventExpirationAlertConfigCriterion.Manifestation location : locations) {

            switch (location) {

            case CONSOLE:
                IHazardEventAlert consoleAlert = buildConsoleAlert(
                        alertCriterion, hazardEvent);
                result.add(consoleAlert);
                break;

            case SPATIAL:
                IHazardEventAlert spatialAlert = buildSpatialAlert(
                        alertCriterion, hazardEvent);
                result.add(spatialAlert);
                break;

            case POPUP:
                IHazardEventAlert popupAlert = buildPopupAlert(alertCriterion,
                        hazardEvent);
                result.add(popupAlert);
                break;
            }
        }

        return result;
    }

    private IHazardEventAlert buildPopupAlert(
            HazardEventExpirationAlertConfigCriterion alertCriterion,
            IHazardEvent hazardEvent) {
        HazardEventExpirationPopUpAlert result = new HazardEventExpirationPopUpAlert(
                hazardEvent.getEventID(), alertCriterion);
        computeActivationTime(alertCriterion, hazardEvent, result);
        return result;
    }

    private IHazardEventAlert buildSpatialAlert(
            HazardEventExpirationAlertConfigCriterion alertCriterion,
            IHazardEvent hazardEvent) {
        HazardEventExpirationSpatialTimer result = new HazardEventExpirationSpatialTimer(
                hazardEvent.getEventID(), alertCriterion);
        computeActivationTime(alertCriterion, hazardEvent, result);
        return result;
    }

    private IHazardEventAlert buildConsoleAlert(
            HazardEventExpirationAlertConfigCriterion alertCriterion,
            IHazardEvent hazardEvent) {
        HazardEventExpirationConsoleTimer result = new HazardEventExpirationConsoleTimer(
                hazardEvent.getEventID(), alertCriterion);
        computeActivationTime(alertCriterion, hazardEvent, result);
        return result;
    }

    private void computeActivationTime(
            HazardEventExpirationAlertConfigCriterion alertCriterion,
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

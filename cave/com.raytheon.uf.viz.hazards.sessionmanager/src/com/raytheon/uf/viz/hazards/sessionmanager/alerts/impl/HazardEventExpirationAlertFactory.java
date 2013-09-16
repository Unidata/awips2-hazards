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
import com.google.common.collect.Sets;
import com.raytheon.uf.common.colormap.Color;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.alerts.HazardEventExpirationAlert;
import com.raytheon.uf.viz.hazards.sessionmanager.alerts.HazardEventExpirationConsoleTimer;
import com.raytheon.uf.viz.hazards.sessionmanager.alerts.HazardEventExpirationPopUpAlert;
import com.raytheon.uf.viz.hazards.sessionmanager.alerts.HazardEventExpirationSpatialTimer;
import com.raytheon.uf.viz.hazards.sessionmanager.alerts.IHazardEventAlert;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.types.HazardEventExpirationAlertConfigCriterion;
import com.raytheon.uf.viz.hazards.sessionmanager.time.ISessionTimeManager;

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

    private final ISessionTimeManager sessionTimeManager;

    public HazardEventExpirationAlertFactory(
            ISessionTimeManager sessionTimeManager) {
        this.sessionTimeManager = sessionTimeManager;
    }

    public List<IHazardEventAlert> createAlerts(
            HazardEventExpirationAlertConfigCriterion alertCriterion,
            IHazardEvent hazardEvent) {

        List<IHazardEventAlert> result = Lists.newArrayList();

        Set<HazardEventExpirationAlertConfigCriterion.Manifestation> locations = alertCriterion
                .getManifestations();
        for (HazardEventExpirationAlertConfigCriterion.Manifestation location : locations) {

            switch (location) {

            case CONSOLE:
                /*
                 * Forecasters wanted an the count-down timer to display
                 * immediately in black
                 */
                IHazardEventAlert immediateConsoleAlert = buildImmediateCountDownTimer(hazardEvent);
                result.add(immediateConsoleAlert);

                /*
                 * Then when the delta before expiration occurs, the black
                 * version is replaced with the configured version.
                 */
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
        setAlertTimes(alertCriterion, hazardEvent, result);
        return result;
    }

    private IHazardEventAlert buildSpatialAlert(
            HazardEventExpirationAlertConfigCriterion alertCriterion,
            IHazardEvent hazardEvent) {
        HazardEventExpirationSpatialTimer result = new HazardEventExpirationSpatialTimer(
                hazardEvent.getEventID(), alertCriterion);
        setAlertTimes(alertCriterion, hazardEvent, result);
        return result;
    }

    private IHazardEventAlert buildImmediateCountDownTimer(
            IHazardEvent hazardEvent) {
        HazardEventExpirationAlertConfigCriterion alertCriterion = new HazardEventExpirationAlertConfigCriterion(
                "",
                HazardEventExpirationAlertConfigCriterion.Units.PERCENT,
                Sets.newHashSet(HazardEventExpirationAlertConfigCriterion.Manifestation.CONSOLE),
                0L, black(), false, false, false);
        HazardEventExpirationConsoleTimer result = new HazardEventExpirationConsoleTimer(
                hazardEvent.getEventID(), alertCriterion);
        setAlertTimes(alertCriterion, hazardEvent, result);
        return result;
    }

    private Color black() {
        return new Color(0, 0, 0, 1);
    }

    private IHazardEventAlert buildConsoleAlert(
            HazardEventExpirationAlertConfigCriterion alertCriterion,
            IHazardEvent hazardEvent) {
        HazardEventExpirationConsoleTimer result = new HazardEventExpirationConsoleTimer(
                hazardEvent.getEventID(), alertCriterion);
        setAlertTimes(alertCriterion, hazardEvent, result);

        return result;
    }

    private void setAlertTimes(
            HazardEventExpirationAlertConfigCriterion alertCriterion,
            IHazardEvent hazardEvent, HazardEventExpirationAlert result) {
        Date hazardExpiration = computeExpirationTime(hazardEvent);
        result.setHazardExpiration(hazardExpiration);
        Date activationTime = computeActivationTime(alertCriterion, hazardEvent);
        result.setActivationTime(activationTime);
    }

    private Date computeExpirationTime(IHazardEvent hazardEvent) {
        /**
         * TODO The product generator needs to be modified to store this as a
         * Date
         */
        Date hazardExpiration = new Date(
                (Long) hazardEvent
                        .getHazardAttribute(HazardConstants.EXPIRATIONTIME));
        return hazardExpiration;
    }

    private Date computeActivationTime(
            HazardEventExpirationAlertConfigCriterion alertCriterion,
            IHazardEvent hazardEvent) {

        Long hazardExpiration = (Long) hazardEvent
                .getHazardAttribute(HazardConstants.EXPIRATIONTIME);

        Long activationTimeInMillis;
        if (alertCriterion.getUnits().equals(
                HazardEventExpirationAlertConfigCriterion.Units.PERCENT)) {
            activationTimeInMillis = activationFromPercentEventCompleted(
                    alertCriterion, hazardExpiration);
        } else {

            activationTimeInMillis = activationFromTimeBeforeExpiration(
                    alertCriterion, hazardExpiration);
        }
        return new Date(activationTimeInMillis);
    }

    /*
     * It was decided that the calculation should be based on the time when the
     * product is issued which is the current time.
     */
    private Long activationFromPercentEventCompleted(
            HazardEventExpirationAlertConfigCriterion alertCriterion,
            Long hazardExpiration) {
        Long result = (long) (hazardExpiration - percentToFraction(100 - alertCriterion
                .getExpirationTime())
                * (hazardExpiration - sessionTimeManager.getCurrentTime()
                        .getTime()));
        return result;
    }

    private Double percentToFraction(Long percent) {
        return percent / 100.0;
    }

    private Long activationFromTimeBeforeExpiration(
            HazardEventExpirationAlertConfigCriterion alertCriterion,
            Long hazardExpiration) {
        Long activationTimeInMillis;
        Long timeBeforeExpiration = alertCriterion.getMillisBeforeExpiration();

        activationTimeInMillis = hazardExpiration - timeBeforeExpiration;
        return activationTimeInMillis;
    }
}

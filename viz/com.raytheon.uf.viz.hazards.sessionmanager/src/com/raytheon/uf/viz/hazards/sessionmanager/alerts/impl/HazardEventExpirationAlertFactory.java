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
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IReadableHazardEvent;
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
 * Date         Ticket#    Engineer      Description
 * ------------ ---------- ------------- --------------------------
 * Jul 19, 2013  1325      daniel.s.schaffer@noaa.gov      Initial creation
 * Dec  1, 2014 3249       Dan Schaffer Issue #3249.  Fixed problem where stale 
 *                                                    alerts would appear when 
 *                                                    you leave hazard services 
 *                                                    and come back much later.
 * Jun 18, 2015  7307      Chris.Cody    Added Hazard End time for requested
 *                                       Time Remaining calculation
 * Oct 29, 2015 11864      Robert.Blum   Expiration time field is now a Date
 *                                       instead of long.
 * Feb 16, 2017 28708      Chris.Golden  Changed to use new constructor for
 *                                       HazardEventExpirationPopupAlert.
 * May 05, 2017 33738      Robert.Blum   Modified to work with newest version of
 *                                       HazardEventExpirationAlertStrategy.
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
            IReadableHazardEvent hazardEvent) {

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
            IReadableHazardEvent hazardEvent) {
        HazardEventExpirationPopUpAlert result = new HazardEventExpirationPopUpAlert(
                hazardEvent.getEventID(), alertCriterion,
                hazardEvent.getIssueSiteID());
        setAlertTimes(alertCriterion, hazardEvent, result);
        return result;
    }

    private IHazardEventAlert buildSpatialAlert(
            HazardEventExpirationAlertConfigCriterion alertCriterion,
            IReadableHazardEvent hazardEvent) {
        HazardEventExpirationSpatialTimer result = new HazardEventExpirationSpatialTimer(
                hazardEvent.getEventID(), alertCriterion);
        setAlertTimes(alertCriterion, hazardEvent, result);
        return result;
    }

    private IHazardEventAlert buildImmediateCountDownTimer(
            IReadableHazardEvent hazardEvent) {
        HazardEventExpirationAlertConfigCriterion alertCriterion = new HazardEventExpirationAlertConfigCriterion(
                "", HazardEventExpirationAlertConfigCriterion.Units.PERCENT,
                Sets.newHashSet(
                        HazardEventExpirationAlertConfigCriterion.Manifestation.CONSOLE),
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
            IReadableHazardEvent hazardEvent) {
        HazardEventExpirationConsoleTimer result = new HazardEventExpirationConsoleTimer(
                hazardEvent.getEventID(), alertCriterion);
        setAlertTimes(alertCriterion, hazardEvent, result);

        return result;
    }

    private void setAlertTimes(
            HazardEventExpirationAlertConfigCriterion alertCriterion,
            IReadableHazardEvent hazardEvent,
            HazardEventExpirationAlert result) {
        Date hazardExpiration = hazardEvent.getExpirationTime();
        result.setHazardExpiration(hazardExpiration);
        Date hazardEnd = hazardEvent.getEndTime();
        result.setHazardEnd(hazardEnd);
        Date activationTime = computeActivationTime(alertCriterion,
                hazardExpiration);
        result.setActivationTime(activationTime);
        /**
         * For now, the alert is deactivated when the hazard expires. So,
         * suppose a hazard expiration time is 11/26/14 at 7am. Now suppose the
         * forecaster exits hazard services and then restarts it at 11/26/14 at
         * 7:10am. The alert is deactivated and so will not appear. Now it is
         * possible that focal points will want to change this algorithm so that
         * the alert is not deactivated until N minutes after hazard expiration.
         * So, if N is 15 then in the above example, the alert would appear. We
         * will come back to this question as needed based on user feedback.
         */
        result.setDeactivationTime(hazardExpiration);
    }

    private Date computeActivationTime(
            HazardEventExpirationAlertConfigCriterion alertCriterion,
            Date hazardExpiration) {

        Long activationTimeInMillis;
        if (alertCriterion.getUnits().equals(
                HazardEventExpirationAlertConfigCriterion.Units.PERCENT)) {
            activationTimeInMillis = activationFromPercentEventCompleted(
                    alertCriterion, hazardExpiration.getTime());
        } else {

            activationTimeInMillis = activationFromTimeBeforeExpiration(
                    alertCriterion, hazardExpiration.getTime());
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
        Long result = (long) (hazardExpiration - percentToFraction(
                100 - alertCriterion.getExpirationTime())
                * (hazardExpiration
                        - sessionTimeManager.getCurrentTime().getTime()));
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

    public void addImmediateAlertsAsNecessary(IReadableHazardEvent hazardEvent,
            List<IHazardEventAlert> alerts) {
        boolean areConsoleAlertsAfterCurrentTime = false;
        for (IHazardEventAlert alert : alerts) {
            if (alert.getClass() == HazardEventExpirationConsoleTimer.class) {
                if (alert.getActivationTime().getTime() < sessionTimeManager
                        .getCurrentTime().getTime()) {
                    /*
                     * Since there already is an active console alert, no
                     * immediate alerts needed
                     */
                    return;
                } else {
                    areConsoleAlertsAfterCurrentTime = true;
                }
            }
        }

        if (areConsoleAlertsAfterCurrentTime) {
            alerts.add(buildImmediateCountDownTimer(hazardEvent));
        }
    }
}

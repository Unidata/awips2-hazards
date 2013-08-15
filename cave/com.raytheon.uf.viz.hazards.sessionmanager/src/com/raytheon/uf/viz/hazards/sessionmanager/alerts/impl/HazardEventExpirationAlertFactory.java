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

import org.joda.time.DateTime;

import com.google.common.collect.Lists;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.hazards.sessionmanager.alerts.HazardAlert;
import com.raytheon.uf.viz.hazards.sessionmanager.alerts.HazardEventExpirationConsoleTimer;
import com.raytheon.uf.viz.hazards.sessionmanager.alerts.HazardEventExpirationSpatialTimer;
import com.raytheon.uf.viz.hazards.sessionmanager.alerts.IHazardEventAlert;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.types.HazardAlertTimerConfigCriterion;

/**
 * Description: A factory that builds {@link IHazardEventAlert}s based
 * on given {@link HazardAlertTimerConfigCriterion} and a given
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

    private final transient IUFStatusHandler statusHandler = UFStatus
	    .getHandler(this.getClass());

    public List<IHazardEventAlert> createAlerts(
	    HazardAlertTimerConfigCriterion alertCriterion,
	    IHazardEvent hazardEvent) {

	List<IHazardEventAlert> result = Lists.newArrayList();

	Set<HazardAlertTimerConfigCriterion.Location> locations = alertCriterion
		.getLocations();
	for (HazardAlertTimerConfigCriterion.Location location : locations) {

	    switch (location) {

	    case CONSOLE:
		HazardEventExpirationConsoleTimer consoleAlert = buildConsoleAlert(
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

    private HazardEventExpirationConsoleTimer buildConsoleAlert(
	    HazardAlertTimerConfigCriterion alertCriterion,
	    IHazardEvent hazardEvent) {
	HazardEventExpirationConsoleTimer consoleAlert = new HazardEventExpirationConsoleTimer(
		hazardEvent.getEventID(), alertCriterion);
	computeActivationTime(alertCriterion, hazardEvent, consoleAlert);
	return consoleAlert;
    }

    private void computeActivationTime(
	    HazardAlertTimerConfigCriterion alertCriterion,
	    IHazardEvent hazardEvent, HazardAlert alert) {
	try {
	    Long timeBeforeExpiration = alertCriterion
		    .getMillisBeforeExpiration();
	    Long hazardExpiration = ((Date) hazardEvent
		    .getHazardAttribute(HazardConstants.EXPIRATIONTIME))
		    .getTime();

	    Long activationTimeInMillis = hazardExpiration
		    - timeBeforeExpiration;
	    alert.setActivationTime(new Date(activationTimeInMillis));
	    /**
	     * TODO Remove this when expiration time is being set.
	     */
	} catch (NullPointerException e) {
	    statusHandler
		    .debug("Alerts will not work until expiration time is set");
	    alert.setActivationTime(new DateTime(2100, 1, 1, 0, 0, 0, 0)
		    .toDate());
	}
    }
}

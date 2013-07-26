/**
 * Hazard Services Alerts.  This code listens for events that require
 * the forecaster to be alerted in some fashion.  When the events occur, the 
 * {@link com.raytheon.uf.viz.hazards.sessionmanager.alerts.impl.HazardSessionAlertsManager}
 * determines the kind of alert needed based on {@link com.raytheon.uf.viz.hazards.sessionmanager.config.impl.types.HazardAlertsConfig}
 * obtained from Localization.  It notifies any interested GUI Presenters which then update alerts
 * in the Views appropriately (ModelViewPresenter pattern at work here).  Alerts can be canceled in a similar fashion.
 * A separate GUI will handle configuration of alerts via in CAVE.  This configuration is persisted to 
 * Localization as mentioned.  Configuration is stored as XML via JAXB.
 * 
 * The {@link com.raytheon.uf.viz.hazards.sessionmanager.alerts.impl.HazardSessionAlertsManager}
 * accepts different {@link com.raytheon.uf.viz.hazards.sessionmanager.alerts.impl.IHazardAlertStrategy}s
 * for constructing {@link com.raytheon.uf.viz.hazards.sessionmanager.alerts.IHazardAlert}s.  
 * 
 * Presenters such as 
 * {@link gov.noaa.gsd.viz.hazards.console.ConsolePresenter} use the 
 * {@link com.raytheon.uf.viz.hazards.sessionmanager.alerts.IHazardAlert}s to create the
 * alert manifestations.
 * 
 * This package contains classes that are required outside of the session manager
 * (ie by {@link gov.noaa.gsd.viz.hazards.display.HazardServicesPresenter}s)
 *  
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 09, 2013  1325      daniel.s.schaffer@noaa.gov     Initial creation
 *
 * </pre>
 *
 * @author daniel.s.schaffer@noaa.gov
 * @version 1.0	
 */

package com.raytheon.uf.viz.hazards.sessionmanager.alerts;
/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.utilities;

import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.event.BaseHazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.registry.HazardEventServiceException;
import com.raytheon.uf.viz.core.VizApp;
import com.raytheon.uf.viz.core.localization.LocalizationManager;
import com.raytheon.uf.viz.hazards.sessionmanager.ISessionManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.ObservedSettings;
import com.raytheon.uf.viz.hazards.sessionmanager.events.InvalidGeometryException;
import com.raytheon.uf.viz.hazards.sessionmanager.events.impl.ObservedHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.IOriginator;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;

import gov.noaa.gsd.common.utilities.geometry.AdvancedGeometryUtilities;
import gov.noaa.gsd.common.utilities.geometry.IAdvancedGeometry;

/**
 * Description: Builder of hazard events from various geometries.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Dec 13, 2013 2182       Dan Schaffer Initial creation
 * Dec 05, 2014 4124       Chris.Golden Changed to work with newly parameterized config
 *                                      manager.
 * Mar 13, 2015 6090       Dan Schaffer Relaxed geometry validity check.
 * Jul 31, 2015 7458       Robert.Blum  Setting userName and workstation fields on events 
 *                                      that are newly created.
 * Mar 14, 2016 12145      mduff        Handle error thrown by event manager.
 * Jul 25, 2016 19537      Chris.Golden Added extensive comments and cleaned up, removing
 *                                      unneeded methods.
 * Sep 12, 2016 15934      Chris.Golden Folded functionality back into the spatial
 *                                      presenter, leaving only deprecated methods being
 *                                      used by obsolete auto-tests.
 * Feb 01, 2017 15556      Chris.Golden Changed to use new selection manager.
 * </pre>
 * 
 * @author Dan Schaffer
 * @version 1.0
 */
public class HazardEventBuilder {

    // Private Variables

    /**
     * Geometry factory.
     */
    private final GeometryFactory geometryFactory = new GeometryFactory();

    /**
     * Session manager to be used when adding events.
     */
    private final ISessionManager<ObservedHazardEvent, ObservedSettings> sessionManager;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param sessionManager
     *            Session manager to be used when adding events.
     */
    public HazardEventBuilder(
            ISessionManager<ObservedHazardEvent, ObservedSettings> sessionManager) {
        this.sessionManager = sessionManager;
    }

    // Public Methods

    /**
     * Build a polygon-based hazard event.
     * 
     * @param coordinates
     *            Coordinates to form the polygon.
     * @return New hazard event.
     * @throws InvalidGeometryException
     *             If the geometry was found to be invalid.
     */
    @Deprecated
    public IHazardEvent buildPolygonHazardEvent(Coordinate[] coordinates)
            throws InvalidGeometryException {
        IAdvancedGeometry geometry = AdvancedGeometryUtilities
                .createGeometryWrapper(geometryFactory.createPolygon(
                        geometryFactory.createLinearRing(coordinates), null),
                        0);

        checkValidity(geometry);

        return finishHazardEventBuild(geometry);
    }

    /**
     * Add the specified hazard event to the event manager.
     * <p>
     * TODO: This functionality should perhaps be relocated to the spatial
     * display. However, it is currently used by the auto-test utilties.
     * </p>
     * 
     * @param event
     *            Event to be added.
     * @param originator
     *            Originator of this addition.
     * @return Resulting new event from the event manager.
     * @throws HazardEventServiceException
     *             If a problem occurs when attempting to add the event.
     * @deprecated Only used by auto-test utilities, which are not being
     *             maintained at this time.
     */
    @Deprecated
    public ObservedHazardEvent addEvent(IHazardEvent event,
            IOriginator originator) throws HazardEventServiceException {

        /*
         * Update the event user and workstation based on who created the event.
         */
        event.setUserName(LocalizationManager.getInstance().getCurrentUser());
        event.setWorkStation(VizApp.getHostName());

        /*
         * If the geometry is to be added to the selected hazard, do this and do
         * nothing with the new event.
         */
        if ((Boolean.TRUE.equals(sessionManager.getConfigurationManager()
                .getSettings().getAddGeometryToSelected()))
                && (event.getHazardType() == null)
                && (sessionManager.getSelectionManager().getSelectedEvents()
                        .size() == 1)) {

            ObservedHazardEvent existingEvent = sessionManager
                    .getSelectionManager().getSelectedEvents().get(0);

            IAdvancedGeometry existingGeometries = existingEvent.getGeometry();
            IAdvancedGeometry newGeometries = event.getGeometry();

            existingEvent.setGeometry(AdvancedGeometryUtilities
                    .createCollection(existingGeometries, newGeometries));

            /*
             * Remove the context menu contribution key so that the now-modified
             * hazard event will not allow the use of select-by-area to modify
             * its geometry.
             */
            existingEvent.removeHazardAttribute(
                    HazardConstants.CONTEXT_MENU_CONTRIBUTION_KEY);
            return existingEvent;
        }

        return sessionManager.getEventManager().addEvent(event, originator);
    }

    // Private Methods

    /**
     * Throw an error if the specified geometry is invalid.
     * 
     * @param geometry
     *            Geometry to be checked.
     * @throws InvalidGeometryException
     *             If the geometry is invalid.
     */
    private void checkValidity(IAdvancedGeometry geometry)
            throws InvalidGeometryException {
        if (geometry.isValid() == false) {
            throw new InvalidGeometryException("invalid geometry: "
                    + geometry.getValidityProblemDescription());
        }
    }

    /**
     * Finish creating a hazard event.
     * 
     * @param geometry
     *            Geometry to be used by the hazard event.
     * @return New hazard event.
     */
    private IHazardEvent finishHazardEventBuild(IAdvancedGeometry geometry) {
        IHazardEvent event = new BaseHazardEvent();
        event.setGeometry(geometry);
        event.setCreationTime(sessionManager.getTimeManager().getCurrentTime());
        return event;
    }
}

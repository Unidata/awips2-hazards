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

import java.util.ArrayList;
import java.util.List;

import com.raytheon.uf.common.dataplugin.events.hazards.event.BaseHazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.viz.core.VizApp;
import com.raytheon.uf.viz.core.localization.LocalizationManager;
import com.raytheon.uf.viz.hazards.sessionmanager.ISessionManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.ObservedSettings;
import com.raytheon.uf.viz.hazards.sessionmanager.events.InvalidGeometryException;
import com.raytheon.uf.viz.hazards.sessionmanager.events.impl.ObservedHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.IOriginator;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.operation.valid.IsValidOp;

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
 * Jul 25, 2016 19537      Chris.Golden Added extensive comments and cleaned up, removing
 *                                      unneeded methods.
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
        Geometry geometry = geometryFactory.createPolygon(
                geometryFactory.createLinearRing(coordinates), null);

        checkValidity(geometry);

        return finishHazardEventBuild(geometry);
    }

    /**
     * Build a hazard event based upon the specified geometry.
     * 
     * @param geometry
     *            Geometry upon which to base the new hazard event.
     * @param checkGeometryValidity
     *            Flag indicating whether or not the geometry's validity is to
     *            be checked.
     * @return New hazard event.
     * @throws InvalidGeometryException
     *             If the geometry was found to be invalid.
     */
    public IHazardEvent buildHazardEvent(Geometry geometry,
            boolean checkGeometryValidity) throws InvalidGeometryException {

        if (checkGeometryValidity) {
            checkValidity(geometry);
        }

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
     */
    public ObservedHazardEvent addEvent(IHazardEvent event,
            IOriginator originator) {

        /*
         * Update the event user and workstation based on who created the event.
         */
        event.setUserName(LocalizationManager.getInstance().getCurrentUser());
        event.setWorkStation(VizApp.getHostName());

        ObservedSettings settings = sessionManager.getConfigurationManager()
                .getSettings();

        /*
         * If the geometry is to be added to the selected hazard, do this and do
         * nothing with the new event.
         */
        if ((Boolean.TRUE.equals(settings.getAddGeometryToSelected()))
                && (event.getHazardType() == null)
                && (sessionManager.getEventManager().getSelectedEvents().size() == 1)) {
            ObservedHazardEvent existingEvent = sessionManager
                    .getEventManager().getSelectedEvents().iterator().next();
            Geometry existingGeometries = existingEvent.getGeometry();
            List<Geometry> geometryList = new ArrayList<>();

            for (int i = 0; i < existingGeometries.getNumGeometries(); ++i) {
                geometryList.add(existingGeometries.getGeometryN(i));
            }

            Geometry newGeometries = event.getGeometry();

            for (int i = 0; i < newGeometries.getNumGeometries(); ++i) {
                geometryList.add(newGeometries.getGeometryN(i));
            }

            GeometryCollection geometryCollection = geometryFactory
                    .createGeometryCollection(geometryList
                            .toArray(new Geometry[geometryList.size()]));
            existingEvent.setGeometry(geometryCollection);
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
    private void checkValidity(Geometry geometry)
            throws InvalidGeometryException {
        if (!geometry.isValid()) {
            IsValidOp op = new IsValidOp(geometry);
            throw new InvalidGeometryException("invalid geometry: "
                    + op.getValidationError().getMessage());
        }
    }

    /**
     * Finish creating a hazard event.
     * 
     * @param geometry
     *            Geometry to be used by the hazard event.
     * @return New hazard event.
     */
    private IHazardEvent finishHazardEventBuild(Geometry geometry) {
        IHazardEvent event = new BaseHazardEvent();
        event.setGeometry(geometry);
        event.setCreationTime(sessionManager.getTimeManager().getCurrentTime());
        return event;
    }
}

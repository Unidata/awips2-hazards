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
 * Description: Constructs hazard events from various geometries represented by
 * {@link Coordinate}s
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Dec 13, 2013 2182       daniel.s.schaffer@noaa.gov      Initial creation
 * Dec 05, 2014 4124       Chris.Golden      Changed to work with newly parameterized
 *                                           config manager.
 * Mar 13, 2015 6090       Dan Schaffer Relaxed geometry validity check.
 * Jul 31, 2015 7458       Robert.Blum  Setting userName and workstation fields on events 
 *                                      that are newly created.
 * </pre>
 * 
 * @author daniel.s.schaffer@noaa.gov
 * @version 1.0
 */
public class HazardEventBuilder {

    private final GeometryFactory geometryFactory = new GeometryFactory();

    private final ISessionManager<ObservedHazardEvent, ObservedSettings> sessionManager;

    public HazardEventBuilder(
            ISessionManager<ObservedHazardEvent, ObservedSettings> sessionManager) {
        this.sessionManager = sessionManager;
    }

    public IHazardEvent buildPolygonHazardEvent(Geometry geometry,
            boolean checkGeometryValidity) throws InvalidGeometryException {

        if (checkGeometryValidity) {
            checkValidity(geometry);
        }

        IHazardEvent event = new BaseHazardEvent();
        finishBuild(event, geometry);
        return event;
    }

    public IHazardEvent buildPolygonHazardEvent(Coordinate[] coordinates)
            throws InvalidGeometryException {
        Geometry geometry = geometryFromCoordinates(coordinates);

        checkValidity(geometry);

        IHazardEvent event = new BaseHazardEvent();
        finishBuild(event, geometry);
        return event;
    }

    public Geometry geometryFromCoordinates(Coordinate[] coordinates) {
        Geometry geometry = geometryFactory.createPolygon(
                geometryFactory.createLinearRing(coordinates), null);
        return geometry;
    }

    public Geometry geometryFromCoordinates(List<Coordinate> coordinates) {
        return geometryFromCoordinates(coordinates
                .toArray(new Coordinate[coordinates.size()]));
    }

    private void checkValidity(Geometry geometry)
            throws InvalidGeometryException {
        if (!geometry.isValid()) {
            IsValidOp op = new IsValidOp(geometry);
            throw new InvalidGeometryException("Invalid Geometry: "
                    + op.getValidationError().getMessage());
        }
    }

    public IHazardEvent buildPointHazardEvent(Coordinate coordinate) {
        Geometry geometry = geometryFactory.createPoint(coordinate);

        IHazardEvent event = new BaseHazardEvent();
        finishBuild(event, geometry);
        return event;
    }

    public IHazardEvent buildLineHazardEvent(Coordinate[] coordinates)
            throws InvalidGeometryException {
        Geometry geometry = geometryFactory.createLineString(coordinates);

        checkValidity(geometry);

        IHazardEvent event = new BaseHazardEvent();
        finishBuild(event, geometry);
        return event;
    }

    private void finishBuild(IHazardEvent event, Geometry geometry) {
        event.setGeometry(geometry);
        event.setCreationTime(sessionManager.getTimeManager().getCurrentTime());
    }

    public ObservedHazardEvent addEvent(IHazardEvent event) {
        return addEvent(event, null);
    }

    public ObservedHazardEvent addEvent(IHazardEvent event,
            IOriginator originator) {
        // Update user/workstation base on who created the event
        event.setUserName(LocalizationManager.getInstance().getCurrentUser());
        event.setWorkStation(VizApp.getHostName());

        ObservedSettings settings = sessionManager.getConfigurationManager()
                .getSettings();

        /*
         * Check if they are adding the Geometry to the selected hazard. In this
         * case do not create a new event.
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
            /*
             * Combine the geometryCollection together!
             */
            // Geometry geom = geometryCollection.union();
            // existingEvent.setGeometry(geom);
            // existingEvent
            // .removeHazardAttribute(HazardConstants.CONTEXT_MENU_CONTRIBUTION_KEY);
            return existingEvent;
        }

        return sessionManager.getEventManager().addEvent(event, originator);
    }

    // public ObservedHazardEvent addEvent(IHazardEvent event) {
    // return addEvent(event, null);
    // }
    //
    // public ObservedHazardEvent addEvent(IHazardEvent event,
    // IOriginator originator) {
    // // Update user/workstation base on who created the event
    // event.setUserName(LocalizationManager.getInstance().getCurrentUser());
    // event.setWorkStation(VizApp.getHostName());
    // return sessionManager.getEventManager().addEvent(event, originator);
    // }

    public IHazardEvent buildPolygonHazardEvent(List<Coordinate> coordinates)
            throws InvalidGeometryException {
        return buildPolygonHazardEvent(coordinates
                .toArray(new Coordinate[coordinates.size()]));
    }

}

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

import java.util.List;

import com.raytheon.uf.common.dataplugin.events.hazards.event.BaseHazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.ISessionManager;
import com.raytheon.uf.viz.hazards.sessionmanager.events.InvalidGeometryException;
import com.raytheon.uf.viz.hazards.sessionmanager.events.impl.ObservedHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.IOriginator;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
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
 * 
 * </pre>
 * 
 * @author daniel.s.schaffer@noaa.gov
 * @version 1.0
 */
public class HazardEventBuilder {

    private final GeometryFactory geometryFactory = new GeometryFactory();

    private final ISessionManager<ObservedHazardEvent> sessionManager;

    public HazardEventBuilder(
            ISessionManager<ObservedHazardEvent> sessionManager) {
        this.sessionManager = sessionManager;
    }

    public IHazardEvent buildPolygonHazardEvent(Geometry geometry)
            throws InvalidGeometryException {

        checkValidity(geometry);

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

    public void addEvent(IHazardEvent event) {
        addEvent(event, null);
    }

    public void addEvent(IHazardEvent event, IOriginator originator) {
        sessionManager.getEventManager().addEvent(event, originator);
    }

    public IHazardEvent buildPolygonHazardEvent(List<Coordinate> coordinates)
            throws InvalidGeometryException {
        return buildPolygonHazardEvent(coordinates
                .toArray(new Coordinate[coordinates.size()]));
    }

}

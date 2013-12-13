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

import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.*;

import com.raytheon.uf.common.dataplugin.events.hazards.event.BaseHazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.ISessionManager;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

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

    private final ISessionManager sessionManager;

    public HazardEventBuilder(ISessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public IHazardEvent buildPolygonHazardEvent(Geometry geometry) {
        IHazardEvent event = new BaseHazardEvent();
        IHazardEvent result = finishBuild(event, geometry);
        return result;
    }

    public IHazardEvent buildPolygonHazardEvent(Coordinate[] coordinates) {
        IHazardEvent event = new BaseHazardEvent();
        Geometry geometry = geometryFactory.createPolygon(
                geometryFactory.createLinearRing(coordinates), null);
        IHazardEvent result = finishBuild(event, geometry);
        return result;
    }

    public IHazardEvent buildPointHazardEvent(Coordinate coordinate) {
        IHazardEvent event = new BaseHazardEvent();
        Geometry geometry = geometryFactory.createPoint(coordinate);
        IHazardEvent result = finishBuild(event, geometry);
        return result;
    }

    public IHazardEvent buildLineHazardEvent(Coordinate[] coordinates) {
        IHazardEvent event = new BaseHazardEvent();
        Geometry geometry = geometryFactory.createLineString(coordinates);
        IHazardEvent result = finishBuild(event, geometry);
        return result;
    }

    private IHazardEvent finishBuild(IHazardEvent event, Geometry geometry) {
        event.setGeometry(geometry);
        event.addHazardAttribute(CREATION_TIME, sessionManager.getTimeManager()
                .getCurrentTime());
        IHazardEvent result = sessionManager.getEventManager().addEvent(event);
        return result;
    }

}

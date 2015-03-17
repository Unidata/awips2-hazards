/**
 * This software was developed and / or modified by the National Oceanic and
 * Atmospheric Administration (NOAA), Earth System Research Laboratory (ESRL),
 * Global Systems Division (GSD), Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.spatialdisplay.drawableelements;

import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HAZARD_EVENT_SELECTED;
import gov.noaa.gsd.viz.hazards.display.HazardServicesEditorUtilities;
import gov.noaa.gsd.viz.hazards.spatialdisplay.DotDrawingAttributes;
import gov.noaa.gsd.viz.hazards.spatialdisplay.HazardServicesDrawingAttributes;
import gov.noaa.gsd.viz.hazards.spatialdisplay.LineDrawingAttributes;
import gov.noaa.gsd.viz.hazards.spatialdisplay.PointDrawingAttributes;
import gov.noaa.gsd.viz.hazards.spatialdisplay.PolygonDrawingAttributes;
import gov.noaa.gsd.viz.hazards.spatialdisplay.SpatialDisplay;
import gov.noaa.gsd.viz.hazards.spatialdisplay.StarDrawingAttributes;
import gov.noaa.gsd.viz.hazards.spatialdisplay.StormTrackDotDrawingAttributes;
import gov.noaa.gsd.viz.hazards.spatialdisplay.TextPositioner;
import gov.noaa.nws.ncep.ui.pgen.elements.AbstractDrawableComponent;
import gov.noaa.nws.ncep.ui.pgen.elements.DECollection;
import gov.noaa.nws.ncep.ui.pgen.elements.Layer;

import java.awt.Color;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.raytheon.uf.common.dataaccess.geom.IGeometryData;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardStatus;
import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardEventUtilities;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.hazards.configuration.types.HazardTypeEntry;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.DataTime;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.hazards.sessionmanager.ISessionManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.ISessionConfigurationManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.ObservedSettings;
import com.raytheon.uf.viz.hazards.sessionmanager.events.impl.ObservedHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.geomaps.GeoMapUtilities;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Lineal;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.Puntal;

/**
 * 
 * Description: Contains factory methods for building the PGEN drawables
 * displayed in Hazard Services
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 02, 2012            bryon.lawrence      Added java doc,
 *                                             refactored to 
 *                                             reduce coupling with
 *                                             drawable classes.
 * Jul 18, 2013   1264     Chris.Golden        Added support for drawing lines and
 *                                             points.
 * Aug  9, 2013 1921       daniel.s.schaffer@noaa.gov  Support of replacement of JSON with POJOs
 * Sep  6, 2013    752     bryon.lawrence      Refactored storm track display logic.
 * Nov 18, 2013 1462       bryon.lawrence      Added hazard area hatching.
 * Nov 23, 2013 1462       bryon.lawrence      Changed polygons to be drawn with no fill by default.
 * Nov 18, 2014 4124       Chris.Golden        Adapted to new time manager.
 * Dec 05, 2014 4124       Chris.Golden        Changed to work with newly parameterized config
 *                                             manager.
 * Dec 13, 2014 4959       Dan Schaffer Spatial Display cleanup and other bug fixes
 * Jan 22, 2015 4959       Dan Schaffer Ability to right click to add/remove UGCs from hazards
 * Jan 26, 2015 5952       Dan Schaffer Fix incorrect hazard area designation.
 * Feb 09, 2015 6260       Dan Schaffer        Fixed bugs in multi-polygon handling
 * Feb 12, 2015 4959       Dan Schaffer Modify MB3 add/remove UGCs to match Warngen
 * Feb 21, 2015 4959       Dan Schaffer Improvements to add/remove UGCs
 * Mar 13, 2015 6090       Dan Schaffer Fixed goosenecks
 * </pre>
 * 
 * @author bryon.lawrence
 * @version 1.0
 */
public class HazardServicesDrawableBuilder {

    /**
     * TODO Are these available anywhere in PGEN? I couldn't find them but they
     * should be!
     */
    private static final String LINE = "Line";

    private static final String DOT = "DOT";

    private static final String FILLED_STAR = "FILLED_STAR";

    private static final String TEXT = "TEXT";

    /**
     * Logging mechanism.
     */
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(HazardServicesDrawableBuilder.class);

    private HazardServicesDrawingAttributes drawingAttributes;

    private final ISessionManager<ObservedHazardEvent, ObservedSettings> sessionManager;

    private final GeoMapUtilities geoMapUtilities;

    private final GeometryFactory geometryFactory = new GeometryFactory();

    public HazardServicesDrawableBuilder(
            ISessionManager<ObservedHazardEvent, ObservedSettings> sessionManager) {
        this.sessionManager = sessionManager;
        this.geoMapUtilities = new GeoMapUtilities(
                sessionManager.getConfigurationManager());
    }

    public AbstractDrawableComponent buildPoint(IHazardEvent hazardEvent,
            int shapeNum, Layer activeLayer, String symbol) {
        DECollection collectionComponent = new DECollection();
        try {
            drawingAttributes = new PointDrawingAttributes(sessionManager,
                    PointDrawingAttributes.Element.OUTER);
            drawingAttributes.setAttributes(shapeNum, hazardEvent);
            drawingAttributes.setTextPosition(TextPositioner.TOP);
            List<Coordinate> points = buildPointCoordinates(shapeNum,
                    hazardEvent);

            collectionComponent.add(new HazardServicesPoint(drawingAttributes,
                    symbol, symbol, points, activeLayer, hazardEvent
                            .getEventID(), true));

            HazardServicesDrawingAttributes drawingAttributes = new PointDrawingAttributes(
                    sessionManager, PointDrawingAttributes.Element.INNER);
            drawingAttributes.setAttributes(shapeNum, hazardEvent);

            collectionComponent.add(new HazardServicesPoint(drawingAttributes,
                    symbol, symbol, points, activeLayer, hazardEvent
                            .getEventID(), false));

        } catch (VizException e) {
            statusHandler.error(
                    "HazardServicesDrawableBuilder.buildPoint(): build "
                            + "of shape failed.", e);
        }

        return collectionComponent;
    }

    /**
     * Builds a PGEN drawable representing a line.
     */
    public AbstractDrawableComponent buildLine(IHazardEvent hazardEvent,
            int shapeNum, Layer activeLayer) {
        AbstractDrawableComponent drawableComponent = null;

        try {
            drawingAttributes = new LineDrawingAttributes(sessionManager);

            List<Coordinate> points = buildCoordinates(shapeNum, hazardEvent);
            drawingAttributes.setAttributes(shapeNum, hazardEvent);

            drawableComponent = new HazardServicesLine(drawingAttributes, LINE,
                    drawingAttributes.getLineStyle().toString(), points,
                    activeLayer, hazardEvent.getEventID());

        } catch (VizException e) {
            statusHandler.error(
                    "HazardServicesDrawableBuilder.buildLine(): build "
                            + "of shape failed.", e);
        }

        return drawableComponent;
    }

    /**
     * Builds a PGEN drawable representing the track line associated with a
     * storm track.
     * 
     * @param hazardEvent
     *            The hazard event containing track information.
     * @param points
     *            The coordinates (lat/lon) representing the track line.
     * @param activeLayer
     *            The layer the PGEN drawables are displayed on.
     * @return A line representing a storm track.
     * 
     */
    public AbstractDrawableComponent buildTrackLine(IHazardEvent hazardEvent,
            List<Coordinate> points, Layer activeLayer) {
        AbstractDrawableComponent drawableComponent = null;

        try {
            drawingAttributes = new LineDrawingAttributes(sessionManager);

            drawingAttributes.setAttributes(0, hazardEvent);

            drawableComponent = new HazardServicesLine(drawingAttributes, LINE,
                    drawingAttributes.getLineStyle().toString(), points,
                    activeLayer, hazardEvent.getEventID());

        } catch (VizException e) {
            statusHandler.error(
                    "HazardServicesDrawableBuilder.buildTrackLine(): build "
                            + "of shape failed.", e);
        }

        return drawableComponent;
    }

    /**
     * Builds a PGEN drawable representing a polygon
     */
    public AbstractDrawableComponent buildPolygon(IHazardEvent hazardEvent,
            int shapeNum, boolean drawFilled, Layer activeLayer) {
        AbstractDrawableComponent drawableComponent = null;

        try {
            drawingAttributes = new PolygonDrawingAttributes(drawFilled,
                    sessionManager);

            drawingAttributes.setAttributes(shapeNum, hazardEvent);

            Coordinate[] coordinates = ((Polygon) hazardEvent.getGeometry()
                    .getGeometryN(shapeNum)).getExteriorRing().getCoordinates();
            LinearRing linearRing = geometryFactory
                    .createLinearRing(coordinates);
            Polygon polygon = geometryFactory.createPolygon(linearRing, null);
            drawableComponent = new HazardServicesPolygon(drawingAttributes,
                    LINE, drawingAttributes.getLineStyle().toString(), polygon,
                    activeLayer, hazardEvent.getEventID());

        } catch (VizException e) {
            statusHandler.error(
                    "HazardServicesDrawableBuilder.buildPolygon(): build "
                            + "of shape failed.", e);
        }

        return drawableComponent;
    }

    /**
     * Builds a PGEN drawable representing a polygon
     */
    public AbstractDrawableComponent buildPolygon(IHazardEvent hazardEvent,
            Geometry hazardHatchArea, Layer activeLayer) {
        AbstractDrawableComponent drawableComponent = null;

        try {
            drawingAttributes = new PolygonDrawingAttributes(sessionManager);

            drawingAttributes.setLineWidth(1);
            drawingAttributes.setColors(drawingAttributes
                    .buildHazardEventColors(hazardEvent,
                            sessionManager.getConfigurationManager()));

            drawingAttributes.setSelected(false);

            drawableComponent = new HazardServicesPolygon(drawingAttributes,
                    LINE, "LINE_DASHED_2", hazardHatchArea, activeLayer,
                    hazardEvent.getEventID());

        } catch (VizException e) {
            statusHandler.error(
                    "HazardServicesDrawableBuilder.buildPolygon(): build "
                            + "of shape failed.", e);
        }

        return drawableComponent;
    }

    /**
     * @return the drawingAttributes
     */
    public HazardServicesDrawingAttributes getDrawingAttributes() {
        return drawingAttributes;
    }

    public AbstractDrawableComponent buildStormTrackDotComponent(
            Layer activeLayer) {
        AbstractDrawableComponent result = null;
        try {
            StormTrackDotDrawingAttributes drawingAttributes = new StormTrackDotDrawingAttributes(
                    sessionManager);
            this.drawingAttributes = drawingAttributes;
            drawingAttributes.setTextPosition(TextPositioner.TOP);
            List<Coordinate> points = drawingAttributes.buildCoordinates();
            drawingAttributes.setAttributes();

            result = new HazardServicesSymbol(drawingAttributes, DOT, DOT,
                    points, activeLayer, HazardConstants.DRAG_DROP_DOT);
            return result;
        } catch (VizException e) {
            statusHandler.error("Could not build storm track dot", e);
        }
        return result;
    }

    public AbstractDrawableComponent buildText(Geometry geometry, String id,
            Layer activeLayer) {
        Point centerPoint;

        centerPoint = geometry.getCentroid();
        AbstractDrawableComponent drawableComponent = new HazardServicesText(
                drawingAttributes, drawingAttributes.getString()[0], TEXT,
                centerPoint, activeLayer, id);

        return drawableComponent;
    }

    public List<AbstractDrawableComponent> buildDrawableComponents(
            SpatialDisplay spatialDisplay, IHazardEvent hazardEvent,
            boolean eventOverlapSelectedTime, Layer activeLayer,
            boolean forModifyingStormTrack, boolean isEventEditable,
            boolean drawHazardHatchArea) {

        List<AbstractDrawableComponent> result = Lists.newArrayList();

        if (forModifyingStormTrack) {
            addComponentsForStormTrackModification(hazardEvent,
                    eventOverlapSelectedTime, result, spatialDisplay,
                    activeLayer);
        } else {

            for (int shapeNum = 0; shapeNum < hazardEvent.getGeometry()
                    .getNumGeometries(); shapeNum++) {

                AbstractDrawableComponent drawableComponent = addShapeComponent(
                        spatialDisplay, hazardEvent, activeLayer, result,
                        drawHazardHatchArea, shapeNum);

                if (drawableComponent instanceof DECollection) {

                    DECollection deCollection = (DECollection) drawableComponent;

                    for (int i = 0; i < deCollection.size(); ++i) {
                        IHazardServicesShape drawable = (IHazardServicesShape) deCollection
                                .getItemAt(i);
                        if (!(drawable instanceof HazardServicesSymbol)) {
                            drawable.setIsEditable(isEventEditable);
                        }

                        drawable.setMovable(isEventEditable);

                    }

                } else {

                    IHazardServicesShape drawable = (IHazardServicesShape) drawableComponent;

                    if (!(drawableComponent instanceof HazardServicesSymbol)) {
                        drawable.setIsEditable(isEventEditable);
                    }

                    drawable.setMovable(isEventEditable);

                }
            }

            addTextComponentAtGeometryCenterPoint(spatialDisplay, result,
                    hazardEvent);

        }
        return result;
    }

    /**
     * Creates the hatched areas associated with the hazard events. Also,
     * creates the hatched area annotations (the "Ws")
     * 
     * @param spatialDisplay
     *            Reference to the Spatial Display tool Layer
     * @param hazardEvent
     *            The hazard event to build the hatched areas for
     * @param activeLayer
     *            The PGEN active layer
     * @param hatchedAreas
     *            A list representing the hazard's hatched area.
     * @param hatchedAreaAnnotations
     *            A list representing the hatched area annotations (the "W's"
     *            associated with short-fused hazards).
     * @param drawHazardHatchArea
     *            Flag indicating whether or not hatched areas should be
     *            displayed.
     * @return
     */
    public void buildhazardAreas(SpatialDisplay spatialDisplay,
            IHazardEvent hazardEvent, Layer activeLayer,
            List<AbstractDrawableComponent> hatchedAreas,
            List<AbstractDrawableComponent> hatchedAreaAnnotations) {

        addHazardHatchArea(spatialDisplay, hazardEvent, activeLayer,
                hatchedAreas, hatchedAreaAnnotations);

    }

    private List<Coordinate> buildCoordinates(int shapeNum,
            IHazardEvent hazardEvent) {
        Geometry geometry = hazardEvent.getGeometry().getGeometryN(shapeNum);

        return Lists.newArrayList(geometry.getCoordinates());
    }

    /**
     * TODO Handle MultiPoint
     */
    private List<Coordinate> buildPointCoordinates(int shapeNum,
            IHazardEvent hazardEvent) {
        Boolean selected = (Boolean) hazardEvent
                .getHazardAttribute(HAZARD_EVENT_SELECTED);
        double radius = 3.0;
        if (selected) {
            radius = 5.0;
        }
        Coordinate centerPointInWorld = hazardEvent.getGeometry()
                .getGeometryN(shapeNum).getCoordinate();

        List<Coordinate> result = drawingAttributes.buildCircleCoordinates(
                radius, centerPointInWorld);
        return result;
    }

    @SuppressWarnings("unchecked")
    private void addComponentsForStormTrackModification(
            IHazardEvent hazardEvent, boolean eventOverlapSelectedTime,
            List<AbstractDrawableComponent> result,
            SpatialDisplay spatialDisplay, Layer activeLayer) {

        Boolean subduePolygon = false;

        /*
         * Build the line string based on the tracking points in the hazard. The
         * track should not be a part of the hazard geometries, because they are
         * not a part of the hazard area. Only the polygon is a part of the
         * hazard area. The line and track points should not be displayed for an
         * issued or proposed hazard.
         */

        /*
         * Add the polygon first so that it is drawn beneath the track line and
         * points.
         */
        AbstractDrawableComponent polygonDrawable = addShapeComponent(
                spatialDisplay, hazardEvent, activeLayer, result, false, 0);

        addTextComponent(spatialDisplay, hazardEvent.getEventID(), result,
                polygonDrawable);

        if (!HazardStatus.hasEverBeenIssued(hazardEvent.getStatus())
                && hazardEvent.getStatus() != HazardStatus.PROPOSED) {
            LineString trackLineString = (LineString) hazardEvent
                    .getHazardAttribute(HazardConstants.STORM_TRACK_LINE);

            List<Coordinate> coordinates = Lists.newArrayList(trackLineString
                    .getCoordinates());
            AbstractDrawableComponent component = buildTrackLine(hazardEvent,
                    coordinates, activeLayer);
            ((IHazardServicesShape) component).setIsEditable(false);
            result.add(component);
            spatialDisplay.addElement(component);
            List<Integer> pivotIndices = (List<Integer>) hazardEvent
                    .getHazardAttribute(HazardConstants.PIVOTS);
            List<Map<String, Serializable>> shapes = (List<Map<String, Serializable>>) hazardEvent
                    .getHazardAttribute(HazardConstants.TRACK_POINTS);

            DataTime currentFrame = HazardServicesEditorUtilities
                    .currentFrame();

            /*
             * Artifically loop one past the natural end of the loop to force
             * the current frame /* to paint last, therefore putting it on top.
             */
            int currentFrameIndex = -1;
            for (int workIdx = 0; workIdx <= trackLineString.getNumPoints(); workIdx++) {
                boolean currentFrameNow = workIdx == trackLineString
                        .getNumPoints();
                int timeIndex = currentFrameNow ? currentFrameIndex : workIdx;
                if (timeIndex < 0) {
                    break;
                }
                Coordinate centerPoint = trackLineString
                        .getCoordinateN(timeIndex);
                Map<String, Serializable> shape = shapes.get(timeIndex);
                Long pointID = (Long) shape.get(HazardConstants.POINTID);
                Date pointDate = new Date(pointID);
                Color[] colors;
                if (currentFrameNow) {
                    colors = new Color[] { Color.WHITE, Color.WHITE };
                } else if (currentFrame != null
                        && pointDate.equals(currentFrame.getRefTime())) {
                    currentFrameIndex = workIdx;
                    continue;
                } else {
                    colors = new Color[] { Color.GRAY, Color.GRAY };
                }
                if (pivotIndices.contains(timeIndex)) {

                    component = buildStar(centerPoint, pointID, 0, hazardEvent,
                            activeLayer, colors);
                } else {
                    component = buildDot(centerPoint, pointID, 0, hazardEvent,
                            activeLayer, colors);
                }
                if (!currentFrameNow) {
                    ((HazardServicesSymbol) component).setMovable(false);
                }
                result.add(component);
                spatialDisplay.addElement(component);
            }

            if (!eventOverlapSelectedTime) {
                subduePolygon = true;
            }

        }

        if (subduePolygon) {
            polygonDrawable.setColors(new Color[] { Color.GRAY, Color.BLACK });
        }
    }

    /**
     * Builds a PGEN drawable representing a star
     * 
     * @param shapeNum
     * @param colors
     */
    public AbstractDrawableComponent buildStar(Coordinate centerPoint,
            Long pointID, int shapeNum, IHazardEvent hazardEvent,
            Layer activeLayer, Color[] colors) {
        AbstractDrawableComponent drawableComponent = null;

        try {
            StarDrawingAttributes drawingAttributes = new StarDrawingAttributes(
                    sessionManager);
            drawingAttributes.setTextPosition(TextPositioner.TOP);
            List<Coordinate> points = drawingAttributes.buildCircleCoordinates(
                    1.0, centerPoint);
            drawingAttributes.setPointID(pointID);
            drawingAttributes.setColors(colors);

            drawableComponent = new HazardServicesSymbol(drawingAttributes,
                    FILLED_STAR, FILLED_STAR, points, activeLayer,
                    hazardEvent.getEventID());

        } catch (VizException e) {
            statusHandler.error(
                    "HazardServicesDrawableBuilder.buildStar(): build "
                            + "of shape failed.", e);
        }

        return drawableComponent;
    }

    /**
     * Builds a PGEN drawable representing a DOT
     * 
     * @param shapeNum
     * @param colors
     */
    public AbstractDrawableComponent buildDot(Coordinate centerPoint,
            Long pointID, int shapeNum, IHazardEvent hazardEvent,
            Layer activeLayer, Color[] colors) {
        AbstractDrawableComponent drawableComponent = null;

        try {
            DotDrawingAttributes drawingAttributes = new DotDrawingAttributes(
                    sessionManager);
            drawingAttributes.setTextPosition(TextPositioner.TOP);
            List<Coordinate> points = drawingAttributes.buildCircleCoordinates(
                    1.0, centerPoint);
            drawingAttributes.setPointID(pointID);
            drawingAttributes.setColors(colors);

            drawableComponent = new HazardServicesSymbol(drawingAttributes,
                    DOT, DOT, points, activeLayer, hazardEvent.getEventID());

        } catch (VizException e) {
            statusHandler.error(
                    "HazardServicesDrawableBuilder.buildDot(): build "
                            + "of shape failed.", e);
        }

        return drawableComponent;
    }

    private AbstractDrawableComponent addShapeComponent(
            SpatialDisplay spatialDisplay, IHazardEvent hazardEvent,
            Layer activeLayer,
            List<AbstractDrawableComponent> drawableComponents,
            boolean drawHazardArea, int shapeNum) {
        AbstractDrawableComponent drawableComponent;

        Geometry geometry = hazardEvent.getGeometry().getGeometryN(shapeNum);

        drawableComponent = addComponentForGeometry(hazardEvent, activeLayer,
                shapeNum, drawHazardArea, geometry);
        /**
         * TODO Figure out how to get rid of this kludge. The kludge works
         * around a failure somewhere else to properly identify whether or not
         * an event is selected. As a result, you cannot modify the polygons.
         */
        if ((Boolean) hazardEvent.getHazardAttribute(HAZARD_EVENT_SELECTED)) {
            spatialDisplay.setSelectedHazardLayer(drawableComponent);
        }

        spatialDisplay.addElement(drawableComponent);
        drawableComponents.add(drawableComponent);
        return drawableComponent;
    }

    /**
     * Builds hatched areas for a hazard event.
     * 
     * @param spatialDisplay
     *            The Hazard Services Spatial Display
     * @param hazardEvent
     *            The hazard event to build hazard areas for
     * @param activeLayer
     *            The PGEN active layer
     * @param hatchedAreas
     *            A list of hatched areas
     * @param hatchedAreaAnnotations
     *            A list of hatched area annotations
     * @return
     */
    private void addHazardHatchArea(SpatialDisplay spatialDisplay,
            IHazardEvent hazardEvent, Layer activeLayer,
            List<AbstractDrawableComponent> hatchedAreas,
            List<AbstractDrawableComponent> hatchedAreaAnnotations) {
        AbstractDrawableComponent drawableComponent;

        String hazardType = HazardEventUtilities.getHazardType(hazardEvent);

        if (hazardType != null) {
            ISessionConfigurationManager<ObservedSettings> configManager = sessionManager
                    .getConfigurationManager();
            HazardTypeEntry hazardTypeEntry = configManager.getHazardTypes()
                    .get(hazardType);
            String mapDBtableName = hazardTypeEntry.getUgcType();

            String mapLabelParameter = hazardTypeEntry.getUgcLabel();

            boolean isWarngenHatching = hazardTypeEntry.isWarngenHatching();

            String cwa = hazardEvent.getSiteID();

            List<IGeometryData> hazardArea = geoMapUtilities
                    .buildHazardAreaForEvent(mapDBtableName, mapLabelParameter,
                            cwa, hazardEvent);

            for (IGeometryData geometryData : hazardArea) {

                /*
                 * Sometimes geometryData are null. Simply skip over them.
                 */
                if (geometryData != null) {

                    for (int i = 0; i < geometryData.getGeometry()
                            .getNumGeometries(); ++i) {

                        Geometry geometry = geometryData.getGeometry()
                                .getGeometryN(i);
                        /*
                         * Skip point and line geometries. Hatching does not
                         * make sense for points and lines (they have no area).
                         */
                        if (!(geometry instanceof Puntal)
                                && !(geometry instanceof Lineal)) {
                            drawableComponent = buildPolygon(hazardEvent,
                                    geometry, activeLayer);
                            hatchedAreas.add(drawableComponent);
                        }
                    }
                }
            }

            if (isWarngenHatching) {
                for (IGeometryData geometryData : hazardArea) {
                    Geometry geometry = geometryData.getGeometry();
                    if (!geometry.isEmpty() && (geometry instanceof Polygon)) {
                        Point centroid = geometryData.getGeometry()
                                .getCentroid();

                        AbstractDrawableComponent textComponent = new HazardServicesText(
                                drawingAttributes,
                                hazardEvent.getSignificance(), TEXT, centroid,
                                activeLayer, hazardEvent.getSignificance());
                        hatchedAreaAnnotations.add(textComponent);
                    }
                }
            }

        }
    }

    private AbstractDrawableComponent addComponentForGeometry(
            IHazardEvent hazardEvent, Layer activeLayer, int shapeNum,
            boolean drawHazardHatchArea, Geometry geometry) {
        AbstractDrawableComponent result;
        Class<?> geometryClass = geometry.getClass();

        if (geometryClass.equals(Point.class)) {
            result = buildPoint(hazardEvent, shapeNum, activeLayer, DOT);
        } else if (geometryClass.equals(LineString.class)) {
            result = buildLine(hazardEvent, shapeNum, activeLayer);
        } else if (geometryClass.equals(Polygon.class)) {
            result = buildPolygon(hazardEvent, shapeNum, false, activeLayer);
        }

        else {
            throw new IllegalArgumentException("Not yet supporting geometry "
                    + geometryClass);
        }
        return result;
    }

    /**
     * Creates a text label at the centroid of a collection of geometries. This
     * allows several geometries associated with one hazard event to share a
     * single label. This helps to reduce the screen clutter which can result
     * when a hazard event has many geometries associated with it.
     * 
     * @param spatialDisplay
     *            The viz resource to draw to.
     * @param drawableComponents
     *            List of drawables to add this text label to.
     * @param hazardEvent
     *            The hazard event being labeled
     * 
     * @return
     */
    public void addTextComponentAtGeometryCenterPoint(
            SpatialDisplay spatialDisplay,
            List<AbstractDrawableComponent> drawableComponents,
            IHazardEvent hazardEvent) {
        if (drawingAttributes.getString() != null
                && drawingAttributes.getString().length > 0) {

            AbstractDrawableComponent text = buildText(
                    hazardEvent.getGeometry(), hazardEvent.getEventID(),
                    spatialDisplay.getActiveLayer());
            spatialDisplay.addElement(text);
            drawableComponents.add(text);
        }
    }

    public void addTextComponent(SpatialDisplay spatialDisplay, String id,
            List<AbstractDrawableComponent> drawableComponents,
            AbstractDrawableComponent associatedShape) {
        if (drawingAttributes.getString() != null
                && drawingAttributes.getString().length > 0) {
            IHazardServicesShape shape = (IHazardServicesShape) (associatedShape instanceof DECollection ? ((DECollection) associatedShape)
                    .getPrimaryDE() : associatedShape);
            AbstractDrawableComponent text = buildText(shape.getGeometry(), id,
                    spatialDisplay.getActiveLayer());
            spatialDisplay.addElement(text);
            drawableComponents.add(text);
        }
    }

}

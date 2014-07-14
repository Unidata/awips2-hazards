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
import gov.noaa.gsd.viz.hazards.spatialdisplay.StarDrawingAttributes;
import gov.noaa.gsd.viz.hazards.spatialdisplay.StormTrackDotDrawingAttributes;
import gov.noaa.gsd.viz.hazards.spatialdisplay.TextPositioner;
import gov.noaa.gsd.viz.hazards.spatialdisplay.ToolLayer;
import gov.noaa.nws.ncep.ui.pgen.elements.AbstractDrawableComponent;
import gov.noaa.nws.ncep.ui.pgen.elements.DECollection;
import gov.noaa.nws.ncep.ui.pgen.elements.Layer;

import java.awt.Color;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.raytheon.uf.common.dataaccess.geom.IGeometryData;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardStatus;
import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardEventUtilities;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.DataTime;
import com.raytheon.uf.common.time.TimeRange;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.hazards.sessionmanager.ISessionManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.ISessionConfigurationManager;
import com.raytheon.uf.viz.hazards.sessionmanager.hatching.HatchingUtilities;
import com.raytheon.uf.viz.hazards.sessionmanager.time.ISessionTimeManager;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Lineal;
import com.vividsolutions.jts.geom.MultiPolygon;
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

    private final ISessionManager sessionManager;

    public HazardServicesDrawableBuilder(ISessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public AbstractDrawableComponent buildPoint(IHazardEvent hazardEvent,
            int shapeNum, Layer activeLayer, String symbol) {
        DECollection collectionComponent = new DECollection();
        try {
            drawingAttributes = new PointDrawingAttributes(sessionManager,
                    PointDrawingAttributes.Element.OUTER);
            drawingAttributes.setAttributes(shapeNum, hazardEvent);
            drawingAttributes.setTextPosition(TextPositioner.TOP);
            List<Coordinate> points = drawingAttributes.buildCoordinates(
                    shapeNum, hazardEvent);

            collectionComponent.add(new HazardServicesPoint(drawingAttributes,
                    symbol, symbol, points, activeLayer, hazardEvent
                            .getEventID(), true));

            HazardServicesDrawingAttributes drawingAttributes = new PointDrawingAttributes(
                    sessionManager, PointDrawingAttributes.Element.INNER);
            drawingAttributes.setAttributes(shapeNum, hazardEvent);
            points = drawingAttributes.buildCoordinates(shapeNum, hazardEvent);

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

            List<Coordinate> points = drawingAttributes.buildCoordinates(
                    shapeNum, hazardEvent);
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

            List<Coordinate> points = drawingAttributes.buildCoordinates(
                    shapeNum, hazardEvent);
            drawingAttributes.setAttributes(shapeNum, hazardEvent);

            drawableComponent = new HazardServicesPolygon(drawingAttributes,
                    LINE, drawingAttributes.getLineStyle().toString(), points,
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

            List<Coordinate> points = Lists
                    .newArrayList(((Polygon) hazardHatchArea).getExteriorRing()
                            .getCoordinates());

            drawingAttributes.setLineWidth(1);
            drawingAttributes.setColors(drawingAttributes
                    .buildHazardEventColors(hazardEvent,
                            sessionManager.getConfigurationManager()));

            drawingAttributes.setSelected(false);

            drawableComponent = new HazardServicesPolygon(drawingAttributes,
                    LINE, "LINE_DASHED_2", points, activeLayer,
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
            Layer activeLayer, GeometryFactory geoFactory) {
        Point centerPoint;

        centerPoint = geometry.getCentroid();
        AbstractDrawableComponent drawableComponent = new HazardServicesText(
                drawingAttributes, drawingAttributes.getString()[0], TEXT,
                centerPoint, activeLayer, id);

        return drawableComponent;
    }

    public List<AbstractDrawableComponent> buildDrawableComponents(
            ToolLayer toolLayer, IHazardEvent hazardEvent, Layer activeLayer,
            boolean isEventAreaEditable, boolean drawHazardHatchArea) {

        List<AbstractDrawableComponent> result = Lists.newArrayList();

        if (forModifyingStormTrack(hazardEvent)) {
            addComponentsForStormTrackModification(hazardEvent, result,
                    toolLayer, activeLayer);
        } else {

            for (int shapeNum = 0; shapeNum < hazardEvent.getGeometry()
                    .getNumGeometries(); shapeNum++) {

                AbstractDrawableComponent drawableComponent = addShapeComponent(
                        toolLayer, hazardEvent, activeLayer, result,
                        drawHazardHatchArea, shapeNum);

                if (drawableComponent instanceof DECollection) {

                    DECollection deCollection = (DECollection) drawableComponent;

                    for (int i = 0; i < deCollection.size(); ++i) {
                        IHazardServicesShape drawable = (IHazardServicesShape) deCollection
                                .getItemAt(i);
                        if (!(drawable instanceof HazardServicesSymbol)) {
                            drawable.setIsEditable(isEventAreaEditable);
                        }

                        drawable.setMovable(isEventAreaEditable);

                    }

                } else {

                    IHazardServicesShape drawable = (IHazardServicesShape) drawableComponent;

                    if (!(drawableComponent instanceof HazardServicesSymbol)) {
                        drawable.setIsEditable(isEventAreaEditable);
                    }

                    drawable.setMovable(isEventAreaEditable);

                }
            }

            addTextComponentAtGeometryCenterPoint(toolLayer, result,
                    hazardEvent);

        }
        return result;
    }

    /**
     * Creates the hatched areas associated with the hazard events. Also,
     * creates the hatched area annotations (the "Ws")
     * 
     * @param toolLayer
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
    public void buildhazardAreas(ToolLayer toolLayer, IHazardEvent hazardEvent,
            Layer activeLayer, List<AbstractDrawableComponent> hatchedAreas,
            List<AbstractDrawableComponent> hatchedAreaAnnotations,
            boolean drawHazardHatchArea) {

        if (drawHazardHatchArea) {
            addHazardHatchArea(toolLayer, hazardEvent, activeLayer,
                    hatchedAreas, hatchedAreaAnnotations);
        }
    }

    public static boolean forModifyingStormTrack(IHazardEvent hazardEvent) {
        return hazardEvent.getHazardAttribute(HazardConstants.TRACK_POINTS) != null;
    }

    @SuppressWarnings("unchecked")
    private void addComponentsForStormTrackModification(
            IHazardEvent hazardEvent, List<AbstractDrawableComponent> result,
            ToolLayer toolLayer, Layer activeLayer) {

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
                toolLayer, hazardEvent, activeLayer, result, false, 0);

        addTextComponent(toolLayer, hazardEvent.getEventID(), result,
                polygonDrawable);

        if (hazardEvent.getStatus() != HazardStatus.ISSUED
                && hazardEvent.getStatus() != HazardStatus.PROPOSED) {
            LineString trackLineString = (LineString) hazardEvent
                    .getHazardAttribute(HazardConstants.STORM_TRACK_LINE);

            List<Coordinate> coordinates = Lists.newArrayList(trackLineString
                    .getCoordinates());
            AbstractDrawableComponent component = buildTrackLine(hazardEvent,
                    coordinates, activeLayer);
            ((IHazardServicesShape) component).setIsEditable(false);
            result.add(component);
            toolLayer.addElement(component);
            List<Integer> pivotIndices = (List<Integer>) hazardEvent
                    .getHazardAttribute(HazardConstants.PIVOTS);
            List<Map<String, Serializable>> shapes = (List<Map<String, Serializable>>) hazardEvent
                    .getHazardAttribute(HazardConstants.TRACK_POINTS);

            DataTime currentFrame = HazardServicesEditorUtilities
                    .currentFrame();

            for (int timeIndex = 0; timeIndex < trackLineString.getNumPoints(); timeIndex++) {
                Coordinate centerPoint = trackLineString
                        .getCoordinateN(timeIndex);
                Map<String, Serializable> shape = shapes.get(timeIndex);
                Long pointID = (Long) shape.get(HazardConstants.POINTID);
                Date pointDate = new Date(pointID);
                Color[] colors;
                if (currentFrame != null
                        && pointDate.equals(currentFrame.getRefTime())) {
                    colors = new Color[] { Color.WHITE, Color.WHITE };
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
                result.add(component);
                toolLayer.addElement(component);
            }

            /*
             * Test whether or not the polygon should be subdued.
             */
            ISessionTimeManager timeManager = sessionManager.getTimeManager();
            TimeRange selectedRange = timeManager.getSelectedTimeRange();
            Date selectedTime = timeManager.getSelectedTime();

            if (!toolLayer.doesEventOverlapSelectedTime(hazardEvent,
                    selectedRange, selectedTime)) {
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
                    "HazardServicesDrawableBuilder.buildStar(): build "
                            + "of shape failed.", e);
        }

        return drawableComponent;
    }

    private AbstractDrawableComponent addShapeComponent(ToolLayer toolLayer,
            IHazardEvent hazardEvent, Layer activeLayer,
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
            toolLayer.setSelectedHazardIHISLayer(drawableComponent);
        }

        toolLayer.addElement(drawableComponent);
        drawableComponents.add(drawableComponent);
        return drawableComponent;
    }

    /**
     * Builds hatched areas for a hazard event.
     * 
     * @param toolLayer
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
    private void addHazardHatchArea(ToolLayer toolLayer,
            IHazardEvent hazardEvent, Layer activeLayer,
            List<AbstractDrawableComponent> hatchedAreas,
            List<AbstractDrawableComponent> hatchedAreaAnnotations) {
        AbstractDrawableComponent drawableComponent;

        String hazardType = HazardEventUtilities.getHazardType(hazardEvent);

        if (hazardType != null) {
            ISessionConfigurationManager configManager = sessionManager
                    .getConfigurationManager();
            String mapDBtableName = configManager.getHazardTypes()
                    .get(hazardType).getHazardHatchArea();

            String mapLabelParameter = configManager.getHazardTypes()
                    .get(hazardType).getHazardHatchLabel();

            String cwa = hazardEvent.getSiteID();

            Set<IGeometryData> hazardArea = HatchingUtilities
                    .buildHatchedAreaForEvent(mapDBtableName,
                            mapLabelParameter, cwa, hazardEvent, configManager);

            for (IGeometryData geometryData : hazardArea) {

                for (int i = 0; i < geometryData.getGeometry()
                        .getNumGeometries(); ++i) {

                    Geometry geometry = geometryData.getGeometry()
                            .getGeometryN(i);
                    /*
                     * Skip point and line geometries. Hatching does not make
                     * sense for points and lines (they have no area).
                     */
                    if (!(geometry instanceof Puntal)
                            && !(geometry instanceof Lineal)) {
                        drawableComponent = buildPolygon(hazardEvent, geometry,
                                activeLayer);
                        hatchedAreas.add(drawableComponent);
                    }
                }
            }

            /*
             * TODO: We will need to refine the "W" annotation used by WarnGEN
             * to indicate which counties are covered by a portion of the hazard
             * polgyon.
             */
            if (mapDBtableName.equals(HazardConstants.POLYGON_TYPE)) {

                hazardArea = HatchingUtilities.getIntersectingMapGeometries(
                        HazardConstants.MAPDATA_COUNTY, mapLabelParameter, cwa,
                        true, configManager, hazardEvent);

                for (IGeometryData geometryData : hazardArea) {
                    Point centroid = geometryData.getGeometry().getCentroid();

                    AbstractDrawableComponent textComponent = new HazardServicesText(
                            drawingAttributes, hazardEvent.getSignificance(),
                            TEXT, centroid, activeLayer,
                            hazardEvent.getSignificance());
                    hatchedAreaAnnotations.add(textComponent);
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
        } else if (geometryClass.equals(Polygon.class)
                || geometryClass.equals(MultiPolygon.class)) {
            result = buildPolygon(hazardEvent, shapeNum, false, activeLayer);
        }

        /**
         * TODO Handle stars for Storm track tool
         */
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
     * @param toolLayer
     *            The viz resource to draw to.
     * @param drawableComponents
     *            List of drawables to add this text label to.
     * @param hazardEvent
     *            The hazard event being labeled
     * 
     * @return
     */
    public void addTextComponentAtGeometryCenterPoint(ToolLayer toolLayer,
            List<AbstractDrawableComponent> drawableComponents,
            IHazardEvent hazardEvent) {
        if (drawingAttributes.getString() != null
                && drawingAttributes.getString().length > 0) {

            AbstractDrawableComponent text = buildText(
                    hazardEvent.getGeometry(), hazardEvent.getEventID(),
                    toolLayer.getActiveLayer(), toolLayer.getGeoFactory());
            toolLayer.addElement(text);
            drawableComponents.add(text);
        }
    }

    public void addTextComponent(ToolLayer toolLayer, String id,
            List<AbstractDrawableComponent> drawableComponents,
            AbstractDrawableComponent associatedShape) {
        if (drawingAttributes.getString() != null
                && drawingAttributes.getString().length > 0) {
            IHazardServicesShape shape = (IHazardServicesShape) (associatedShape instanceof DECollection ? ((DECollection) associatedShape)
                    .getPrimaryDE() : associatedShape);
            AbstractDrawableComponent text = buildText(shape.getGeometry(), id,
                    toolLayer.getActiveLayer(), toolLayer.getGeoFactory());
            toolLayer.addElement(text);
            drawableComponents.add(text);
        }
    }

}

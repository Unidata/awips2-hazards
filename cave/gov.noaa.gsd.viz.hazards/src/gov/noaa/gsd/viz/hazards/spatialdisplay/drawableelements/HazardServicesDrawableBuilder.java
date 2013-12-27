/**
 * This software was developed and / or modified by the National Oceanic and
 * Atmospheric Administration (NOAA), Earth System Research Laboratory (ESRL),
 * Global Systems Division (GSD), Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.spatialdisplay.drawableelements;

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
import gov.noaa.gsd.viz.hazards.utilities.Utilities;
import gov.noaa.nws.ncep.ui.pgen.elements.AbstractDrawableComponent;
import gov.noaa.nws.ncep.ui.pgen.elements.DECollection;
import gov.noaa.nws.ncep.ui.pgen.elements.Layer;

import java.awt.Color;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.google.common.collect.Lists;
import com.raytheon.uf.common.dataaccess.geom.IGeometryData;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardState;
import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardEventUtilities;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationLevel;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.DataTime;
import com.raytheon.uf.common.time.TimeRange;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.localization.LocalizationManager;
import com.raytheon.uf.viz.hazards.sessionmanager.ISessionManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.ISessionConfigurationManager;
import com.raytheon.uf.viz.hazards.sessionmanager.events.ISessionEventManager;
import com.raytheon.uf.viz.hazards.sessionmanager.time.ISessionTimeManager;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

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
            Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                    .getShell();

            drawingAttributes = new PointDrawingAttributes(shell,
                    sessionManager, PointDrawingAttributes.Element.OUTER);
            drawingAttributes.setAttributes(shapeNum, hazardEvent);
            drawingAttributes.setTextPosition(TextPositioner.TOP);
            List<Coordinate> points = drawingAttributes.buildCoordinates(
                    shapeNum, hazardEvent);

            collectionComponent.add(new HazardServicesPoint(drawingAttributes,
                    symbol, symbol, points, activeLayer, hazardEvent
                            .getEventID(), true));

            HazardServicesDrawingAttributes drawingAttributes = new PointDrawingAttributes(
                    shell, sessionManager, PointDrawingAttributes.Element.INNER);
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
            drawingAttributes = new LineDrawingAttributes(PlatformUI
                    .getWorkbench().getActiveWorkbenchWindow().getShell(),
                    sessionManager);

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
            drawingAttributes = new LineDrawingAttributes(PlatformUI
                    .getWorkbench().getActiveWorkbenchWindow().getShell(),
                    sessionManager);

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
            drawingAttributes = new PolygonDrawingAttributes(PlatformUI
                    .getWorkbench().getActiveWorkbenchWindow().getShell(),
                    drawFilled, sessionManager);

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
            drawingAttributes = new PolygonDrawingAttributes(PlatformUI
                    .getWorkbench().getActiveWorkbenchWindow().getShell(),
                    sessionManager);

            List<Coordinate> points = Lists.newArrayList(hazardHatchArea
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
                    PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                            .getShell(), sessionManager);
            this.drawingAttributes = drawingAttributes;
            drawingAttributes.setTextPosition(TextPositioner.TOP);
            List<Coordinate> points = drawingAttributes.buildCoordinates();
            drawingAttributes.setAttributes();

            result = new HazardServicesSymbol(drawingAttributes, DOT, DOT,
                    points, activeLayer, Utilities.DRAG_DROP_DOT);
            return result;
        } catch (VizException e) {
            statusHandler.error("Could not build storm track dot", e);
        }
        return result;
    }

    public AbstractDrawableComponent buildText(Geometry geometry, String id,
            Layer activeLayer, GeometryFactory geoFactory) {
        Point centerPoint;
        AbstractDrawableComponent drawableComponent = null;
        Coordinate[] coords = geometry.getCoordinates();

        if (coords.length > 1) {
            Coordinate[] fullyEnclosedCoords = new Coordinate[coords.length + 1];
            System.arraycopy(coords, 0, fullyEnclosedCoords, 0, coords.length);
            fullyEnclosedCoords[coords.length] = coords[0];
            LineString ls = geoFactory.createLineString(fullyEnclosedCoords);
            centerPoint = ls.getCentroid();
        } else {
            centerPoint = geoFactory.createPoint(coords[0]);
        }

        drawableComponent = new HazardServicesText(drawingAttributes,
                drawingAttributes.getString()[0], TEXT, centerPoint,
                activeLayer, id);

        return drawableComponent;
    }

    public List<AbstractDrawableComponent> buildDrawableComponents(
            ToolLayer toolLayer, IHazardEvent hazardEvent, Layer activeLayer,
            boolean drawHazardHatchArea) {

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

                addTextComponent(toolLayer, hazardEvent.getEventID(), result,
                        drawableComponent);
            }

        }
        return result;
    }

    public List<AbstractDrawableComponent> buildhazardAreas(
            ToolLayer toolLayer, IHazardEvent hazardEvent, Layer activeLayer,
            boolean drawHazardHatchArea) {

        List<AbstractDrawableComponent> result = Lists.newArrayList();

        if (drawHazardHatchArea) {
            addHazardHatchArea(toolLayer, hazardEvent, activeLayer, result);
        }

        return result;
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

        if (hazardEvent.getState() != HazardState.ISSUED
                && hazardEvent.getState() != HazardState.PROPOSED) {
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
                if (pointDate.equals(currentFrame.getRefTime())) {
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
                    PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                            .getShell(), sessionManager);
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
                    PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                            .getShell(), sessionManager);
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
        if ((Boolean) hazardEvent
                .getHazardAttribute(ISessionEventManager.ATTR_SELECTED)) {
            toolLayer.setSelectedHazardIHISLayer(drawableComponent);
        }

        toolLayer.addElement(drawableComponent);
        drawableComponents.add(drawableComponent);
        return drawableComponent;
    }

    /**
     * Builds a hazard area drawable for the provided hazard event.
     * 
     * @param toolLayer
     * @param hazardEvent
     * @param activeLayer
     * @param drawableComponents
     * @return
     */
    private void addHazardHatchArea(ToolLayer toolLayer,
            IHazardEvent hazardEvent, Layer activeLayer,
            List<AbstractDrawableComponent> drawableComponents) {
        AbstractDrawableComponent drawableComponent;

        String hazardType = HazardEventUtilities.getHazardType(hazardEvent);

        if (hazardType != null) {
            ISessionConfigurationManager configManager = sessionManager
                    .getConfigurationManager();
            String mapDBtableName = configManager.getHazardTypes()
                    .get(hazardType).getHazardHatchArea();

            String mapLabelParameter = configManager.getHazardTypes()
                    .get(hazardType).getHazardHatchLabel();

            String cwa = LocalizationManager
                    .getContextName(LocalizationLevel.SITE);

            Set<IGeometryData> hazardArea = HazardEventUtilities
                    .buildHatchedAreaForEvent(mapDBtableName,
                            mapLabelParameter, cwa, hazardEvent);

            for (IGeometryData geometryData : hazardArea) {

                for (int i = 0; i < geometryData.getGeometry()
                        .getNumGeometries(); ++i) {
                    drawableComponent = buildPolygon(hazardEvent, geometryData
                            .getGeometry().getGeometryN(i), activeLayer);
                    drawableComponents.add(drawableComponent);
                }
            }

            /*
             * TODO: We will need to refine the "W" annotation used by WarnGEN
             * to indicate which counties are covered by a portion of the hazard
             * polgyon.
             */
            if (mapDBtableName.equals(HazardConstants.POLYGON_TYPE)) {

                hazardArea = HazardEventUtilities.getIntersectingMapGeometries(
                        HazardConstants.MAPDATA_COUNTY, mapLabelParameter, cwa,
                        true, hazardEvent);

                for (IGeometryData geometryData : hazardArea) {
                    Point centroid = geometryData.getGeometry().getCentroid();

                    AbstractDrawableComponent textComponent = new HazardServicesText(
                            drawingAttributes,
                            HazardConstants.COUNTY_INCLUDED_IN_HAZARD_ANNOTATION,
                            TEXT,
                            centroid,
                            activeLayer,
                            HazardConstants.COUNTY_INCLUDED_IN_HAZARD_ANNOTATION);
                    drawableComponents.add(textComponent);
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

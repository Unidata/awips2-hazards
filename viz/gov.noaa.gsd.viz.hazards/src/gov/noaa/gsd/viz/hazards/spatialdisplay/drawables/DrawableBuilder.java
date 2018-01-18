/**
 * This software was developed and / or modified by the National Oceanic and
 * Atmospheric Administration (NOAA), Earth System Research Laboratory (ESRL),
 * Global Systems Division (GSD), Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.spatialdisplay.drawables;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import gov.noaa.gsd.common.utilities.geometry.AdvancedGeometryCollection;
import gov.noaa.gsd.common.utilities.geometry.AdvancedGeometryUtilities;
import gov.noaa.gsd.common.utilities.geometry.Ellipse;
import gov.noaa.gsd.common.utilities.geometry.GeometryWrapper;
import gov.noaa.gsd.common.utilities.geometry.IAdvancedGeometry;
import gov.noaa.gsd.common.visuals.BorderStyle;
import gov.noaa.gsd.common.visuals.DragCapability;
import gov.noaa.gsd.common.visuals.FillStyle;
import gov.noaa.gsd.common.visuals.SpatialEntity;
import gov.noaa.gsd.common.visuals.SymbolShape;
import gov.noaa.gsd.viz.hazards.spatialdisplay.entities.HazardEventHatchingEntityIdentifier;
import gov.noaa.gsd.viz.hazards.spatialdisplay.entities.IEntityIdentifier;
import gov.noaa.nws.ncep.ui.pgen.display.FillPatternList.FillPattern;
import gov.noaa.nws.ncep.ui.pgen.elements.AbstractDrawableComponent;
import gov.noaa.nws.ncep.ui.pgen.elements.DECollection;

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
 * Mar 24, 2015 6090       Dan Schaffer Goosenecks now working as they do in Warngen
 * May 05, 2015 7624       mduff        Handle MultiPolygons, added deholer method.
 * Jun 24, 2015 6601       Chris.Cody   Change Create by Hazard Type display text
 * Jul 17, 2015 8890       Chris.Cody   Vertices appearing incorrectly on display
 * Oct 13, 2015 12494      Chris Golden Reworked to allow hazard types to include
 *                                      only phenomenon (i.e. no significance) where
 *                                      appropriate.
 * Nov 10, 2015 12762      Chris.Golden Added support for use of new recommender manager.
 * Mar 16, 2016 15676      Chris.Golden Added support for spatial entities.
 * Mar 22, 2016 15676      Chris.Golden Fixed bugs in spatial entity drawable building.
 * Mar 26, 2016 15676      Chris.Golden Fixed bugs with creation of hazard event and
 *                                      spatial entity drawables, and added ability to
 *                                      have spatial entities be moved and edited by the
 *                                      user.
 * Jun 23, 2016 19537      Chris.Golden Removed storm-track-specific code. Also added
 *                                      hatching for hazard events that have visual
 *                                      features, and added ability to use visual
 *                                      features to request spatial info for recommenders.
 * Jul 25, 2016 19537      Chris.Golden Renamed, and removed unneeded methods as part of
 *                                      the move to generate drawables solely from spatial
 *                                      entities.
 * Aug 18, 2016 19537      Chris.Golden Fixed bug that caused exception to be thrown when
 *                                      a hatching polygon had a label to be displayed.
 *                                      Also simplified building of drawables by removing
 *                                      any need to call back into the spatial display;
 *                                      instead, drawables are returned to the caller of
 *                                      the various buildXXXX() methods. Removed obsolete
 *                                      method for amalgamating text drawables, and added
 *                                      a new method that builds an amalgamated text
 *                                      drawable (the work of determining which text
 *                                      drawables should be amalgamated is now done
 *                                      elsewhere, as this isn't really this class's job).
 *                                      Also added ability to break spatial entity label
 *                                      text into different lines, using newline
 *                                      characters as delimiters.
 * Sep 12, 2016 15934      Chris.Golden Changed to work with advanced geometries.
 * Sep 21, 2016 15934      Chris.Golden Added support for ellipse drawing.
 * Sep 29, 2016 15928      Chris.Golden Added method to create bounding box drawables
 *                                      as necessary.
 * Jan 17, 2018 33428      Chris.Golden Changed to work with new version of
 *                                      {@link IDrawable}.
 * </pre>
 * 
 * @author bryon.lawrence
 * @version 1.0
 */
public class DrawableBuilder {

    // Private Static Constants

    /*
     * TODO Are these available anywhere in PGEN? The latter should have them
     * defined as public static constants.
     */
    private static final String DOT = "DOT";

    private static final String STAR = "STAR";

    private static final String FILLED_STAR = "FILLED_STAR";

    /**
     * Color to be used for bounding boxes drawn around other drawables.
     */
    private static final Color BOUNDING_BOX_COLOR = new Color(255, 255, 255,
            180);

    /**
     * Splitter, used to break up text if it has newlines in it into an array of
     * strings, with the newlines being the delimiters between the elements in
     * the array.
     */
    private static final Splitter SPLITTER = Splitter.on("\n");

    /**
     * Map of visual feature symbol shapes to PGEN symbol shape names to be used
     * for the outer shape, that is, the shape that is used to provide the
     * border.
     */
    private static final Map<SymbolShape, String> PGEN_OUTER_SYMBOLS_FOR_VISUAL_FEATURE_SYMBOLS;

    static {
        Map<SymbolShape, String> map = new HashMap<>(2, 1.0f);
        map.put(SymbolShape.CIRCLE, DOT);
        map.put(SymbolShape.STAR, STAR);
        PGEN_OUTER_SYMBOLS_FOR_VISUAL_FEATURE_SYMBOLS = ImmutableMap
                .copyOf(map);
    }

    /**
     * Map of visual feature symbol shapes to PGEN symbol shape names to be used
     * for the inner shape, that is, the shape that is used to provide the fill.
     * If no entry is found for a key that does have an entry in the
     * {@link #PGEN_OUTER_SYMBOLS_FOR_VISUAL_FEATURE_SYMBOLS} map, then the same
     * PGEN symbol shape name is used, but with a reduced size scale.
     */
    private static final Map<SymbolShape, String> PGEN_INNER_SYMBOLS_FOR_VISUAL_FEATURE_SYMBOLS;

    static {
        Map<SymbolShape, String> map = new HashMap<>(1, 1.0f);
        map.put(SymbolShape.STAR, FILLED_STAR);
        PGEN_INNER_SYMBOLS_FOR_VISUAL_FEATURE_SYMBOLS = ImmutableMap
                .copyOf(map);
    }

    /**
     * Map of visual feature symbol shapes to baseline PGEN symbol shape size
     * multipliers, meant to normalize all symbol shapes so that when drawn with
     * the same scale, they end up about the same size.
     */
    private static final Map<SymbolShape, Double> PGEN_SIZE_MULTIPLIERS_FOR_VISUAL_FEATURE_SYMBOLS;

    static {
        Map<SymbolShape, Double> map = new HashMap<>(2, 1.0f);
        map.put(SymbolShape.CIRCLE, 1.0);
        map.put(SymbolShape.STAR, 0.3);
        PGEN_SIZE_MULTIPLIERS_FOR_VISUAL_FEATURE_SYMBOLS = ImmutableMap
                .copyOf(map);
    }

    // Private Variables

    /**
     * Drawing attributes.
     */
    private DrawableAttributes drawingAttributes;

    // Public Methods

    /**
     * Build drawable components for the specified spatial entity.
     * 
     * @param spatialEntity
     *            Spatial entity for which the components are to be built. The
     *            entity's geometry may consist of a single point, line, or
     *            polygon, or else a geometry collection holding any mix of one
     *            or more points, lines, and polygons, but no nested
     *            collections.
     * @return Drawable components that were built; may be an empty list.
     */
    public List<AbstractDrawableComponent> buildDrawables(
            SpatialEntity<? extends IEntityIdentifier> spatialEntity) {

        /*
         * Only create drawables for the entity's geometry if the entity is not
         * intended solely for hazard hatching.
         */
        List<AbstractDrawableComponent> drawableComponents = new ArrayList<>();
        boolean createDrawableAttributes = true;
        if (spatialEntity
                .getIdentifier() instanceof HazardEventHatchingEntityIdentifier == false) {

            /*
             * If the spatial entity has a geometry collection, create a
             * drawable component for each sub-geometry separately. Otherwise,
             * create a single drawable component for the entire geometry.
             */
            createDrawableAttributes = false;
            IAdvancedGeometry geometry = spatialEntity.getGeometry();
            if (geometry instanceof AdvancedGeometryCollection) {
                List<IAdvancedGeometry> subGeometries = ((AdvancedGeometryCollection) geometry)
                        .getChildren();
                for (int j = 0; j < subGeometries.size(); j++) {
                    drawableComponents.add(
                            buildDrawable(spatialEntity, subGeometries.get(j),
                                    (subGeometries.size() == 1 ? -1 : j)));
                }
            } else {
                drawableComponents
                        .add(buildDrawable(spatialEntity, geometry, -1));
            }
        }

        /*
         * Add a text drawable if appropriate. A drawable attributes object must
         * be created if one was not created above (which will be the case if
         * the entity is hatching-only).
         */
        if (hasNonEmptyLabel(spatialEntity)) {
            if (createDrawableAttributes) {
                PolygonDrawableAttributes drawingAttributes = new PolygonDrawableAttributes(
                        (spatialEntity.getFillColor().getAlpha() > 0.0));
                this.drawingAttributes = drawingAttributes;
                drawingAttributes.setSizeScale(2);
                drawingAttributes.setLabel(
                        convertNewlinesToArray(spatialEntity.getLabel()));
                drawingAttributes.setTextPosition(
                        getTextPositionForSpatialEntity(spatialEntity));
            }
            drawableComponents.add(buildText(spatialEntity,
                    (createDrawableAttributes == false)));
        }

        return drawableComponents;
    }

    /**
     * Determine whether or not the specified entity has a non-empty label.
     * 
     * @param spatialEntity
     *            Spatial entity to be checked.
     * @return <code>true</code> if the entity has a non-empty label,
     *         <code>false</code> otherwise.
     */
    private boolean hasNonEmptyLabel(
            SpatialEntity<? extends IEntityIdentifier> spatialEntity) {
        return ((spatialEntity.getLabel() != null)
                && (spatialEntity.getLabel().trim().isEmpty() == false));
    }

    /**
     * Convert the newline-delimited text string into an array of substrings.
     * 
     * @param text
     *            Text string, possibly holding newlines.
     * @return Array holding the specified text as elements.
     */
    private String[] convertNewlinesToArray(String text) {
        List<String> result = SPLITTER.splitToList(text.trim());
        return result.toArray(new String[result.size()]);
    }

    /**
     * Build any hatched areas for the specified spatial entity.
     * 
     * @param spatialEntity
     *            Entity that has hatched geometry.
     * @return Hatched areas that were built.
     */
    public List<AbstractDrawableComponent> buildHatchedAreas(
            SpatialEntity<? extends IEntityIdentifier> spatialEntity) {

        /*
         * Build hatched areas if the entity has a hatch-style fill.
         */
        if (spatialEntity.getFillStyle() == FillStyle.HATCHED) {
            List<AbstractDrawableComponent> hatchedAreas = new ArrayList<>();
            buildHatchedAreas(spatialEntity, spatialEntity.getGeometry(),
                    hatchedAreas);
            return hatchedAreas;
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Build an amalgamated text drawable that combines all the specified source
     * text drawables into one.
     * 
     * @param sourceTextDrawables
     *            Source text drawables to be amalgamated; must be a non-empty
     *            list.
     * @return Amalgamated text drawable.
     */
    public TextDrawable buildAmalgamatedText(
            List<TextDrawable> sourceTextDrawables) {
        TextDrawable amalgamatedTextDrawable = new TextDrawable(
                sourceTextDrawables.get(sourceTextDrawables.size() - 1));
        Set<String> labels = new LinkedHashSet<>(sourceTextDrawables.size(),
                1.0f);
        for (TextDrawable sourceTextDrawable : sourceTextDrawables) {
            String[] labelArray = sourceTextDrawable.getText();
            for (String label : labelArray) {
                if (label != null) {
                    labels.add(label);
                }
            }
        }
        List<String> labelsList = new ArrayList<>(labels);
        Collections.reverse(labelsList);
        amalgamatedTextDrawable
                .setText(labels.toArray(new String[labelsList.size()]));
        return amalgamatedTextDrawable;
    }

    /**
     * Build a bounding box drawable, if one is appropriate, to bound the
     * specified drawable.
     * 
     * @param drawable
     *            Drawable for which to create a bounding box.
     * @param associatedDrawable
     *            Drawable with which to associate the bounding box. This will
     *            generally be a reference to the same drawable that
     *            <code>drawable</code> references, unless the bounding box is
     *            being constructed for the geometry of a modified version of a
     *            drawable, in which case the two parameters may be different.
     * @return Bounding box drawable, or <code>null</code> if none is
     *         appropriate.
     */
    public BoundingBoxDrawable buildBoundingBoxDrawable(
            MultiPointDrawable<?> drawable,
            MultiPointDrawable<?> associatedDrawable) {

        /*
         * If the drawable is rotatable and/or scaleable, create a bounding box
         * drawable out of the manipulatable points.
         */
        if (associatedDrawable.isRotatable()
                || associatedDrawable.isResizable()) {

            /*
             * Get the corner points of the bounding box.
             */
            List<Coordinate> cornerPoints = AdvancedGeometryUtilities
                    .getBoundingBoxCornerPoints(drawable.getGeometry());

            /*
             * Because the bounding box has corner points in lat-lon
             * coordinates, it must be created as a polygon with pseudo-curved
             * edges in order to ensure that, when projected onto a flat
             * surface, a curving latitude line (for example) is followed by the
             * bounding box's horizontal sides if the box is not rotated. This
             * means inserting a number of extra points between the corner
             * points, with the number of points between each dependent upon the
             * distance in lat-lon pseudo-units between those points. Thus,
             * determine the distances in lat-lons between each pair of adjacent
             * corner points.
             */
            List<Double> distancesBetweenCornerPoints = new ArrayList<>(
                    cornerPoints.size());
            double totalDistance = 0.0;
            for (int j = 0; j < cornerPoints.size(); j++) {
                double distance = cornerPoints.get(j).distance(
                        cornerPoints.get((j + 1) % cornerPoints.size()));
                totalDistance += distance;
                distancesBetweenCornerPoints.add(distance);
            }

            /*
             * Create the bounding box points list; size it to hold enough
             * points to have one for each half a degree of lat-lon "distance",
             * plus a repeat of the end point, plus some slop in case the fact
             * that the distances are floating-point numbers causes one more
             * point to be added on each side than is expected from looking at
             * the total. Add the first point twice, once in the beginning and
             * once at the end, in order to close the geometry.
             */
            List<Coordinate> boundingBoxPoints = new ArrayList<>(
                    (int) ((totalDistance * 2.0) + 0.5) + 5);
            for (int j = 0; j < cornerPoints.size() + 1; j++) {
                Coordinate cornerPoint = cornerPoints
                        .get(j % cornerPoints.size());
                boundingBoxPoints.add(cornerPoint);
                if (j < cornerPoints.size()) {
                    Coordinate nextCornerPoint = cornerPoints
                            .get((j + 1) % cornerPoints.size());
                    int numInterimPoints = (int) ((distancesBetweenCornerPoints
                            .get(j) * 2.0) + 0.5);
                    if (numInterimPoints < 2) {
                        numInterimPoints = 2;
                    }
                    double xIncrement = (nextCornerPoint.x - cornerPoint.x)
                            / numInterimPoints;
                    double yIncrement = (nextCornerPoint.y - cornerPoint.y)
                            / numInterimPoints;
                    for (int k = 1; k < numInterimPoints; k++) {
                        boundingBoxPoints.add(
                                new Coordinate(cornerPoint.x + (xIncrement * k),
                                        cornerPoint.y + (yIncrement * k)));
                    }
                }
            }

            /*
             * Create the JTS geometry representing the bounding box.
             */
            Geometry boundingBox = AdvancedGeometryUtilities
                    .getGeometryFactory().createPolygon(boundingBoxPoints
                            .toArray(new Coordinate[boundingBoxPoints.size()]));

            /*
             * Create the drawable to be used as the bounding box.
             */
            PolygonDrawableAttributes boundingBoxAttributes = new PolygonDrawableAttributes(
                    false);
            boundingBoxAttributes.setLineWidth(2.5f);
            boundingBoxAttributes.setDashedLineStyle();
            boundingBoxAttributes.setSizeScale(1);
            boundingBoxAttributes.setColors(new Color[] { BOUNDING_BOX_COLOR });
            return new BoundingBoxDrawable(associatedDrawable,
                    boundingBoxAttributes,
                    (GeometryWrapper) AdvancedGeometryUtilities
                            .createGeometryWrapper(boundingBox, 0));
        }
        return null;
    }

    // Private Methods

    /**
     * Build a single drawable component for the specified spatial entity.
     * 
     * @param spatialEntity
     *            Spatial entity for which the components are to be built. The
     *            entity's geometry may consist of a single point, line, or
     *            polygon.
     * @param geometry
     *            Geometry to be used for building the component; must be either
     *            the geometry of <code>spatialEntity</code>, or one of said
     *            geometry's sub-geometries, if the main geometry is a
     *            collection.
     * @param geometryIndex
     *            Index of the sub-geometry within the geometry of
     *            <code>spatialEntity</code> that is <code>geometry</code>; if
     *            there are no sub-geometries and <code>geometry</code> is the
     *            same as the spatial entity's geometry, this must be
     *            <code>-1</code>.
     * @return Drawable component that was built.
     */
    private AbstractDrawableComponent buildDrawable(
            SpatialEntity<? extends IEntityIdentifier> spatialEntity,
            IAdvancedGeometry geometry, int geometryIndex) {
        AbstractDrawableComponent drawableComponent = null;
        if (geometry.isPotentiallyCurved()) {
            drawableComponent = buildEllipse(spatialEntity, (Ellipse) geometry,
                    geometryIndex);
        } else if (geometry.isPunctual()) {
            drawableComponent = buildPoint(spatialEntity,
                    (GeometryWrapper) geometry, geometryIndex);
        } else if (geometry.isLineal()) {
            drawableComponent = buildLine(spatialEntity,
                    (GeometryWrapper) geometry, geometryIndex);
        } else {
            drawableComponent = buildPolygon(spatialEntity,
                    (GeometryWrapper) geometry, geometryIndex);
        }
        return drawableComponent;
    }

    /**
     * Build any hatched areas for any part of the specified geometry that is
     * polygonal.
     * 
     * @param spatialEntity
     *            Entity of which this geometry is a part.
     * @param geometry
     *            Geometry for which to build the hatched areas.
     * @param hatchedAreas
     *            List to which to add any hatched areas that are built.
     */
    private void buildHatchedAreas(
            SpatialEntity<? extends IEntityIdentifier> spatialEntity,
            IAdvancedGeometry geometry,
            List<AbstractDrawableComponent> hatchedAreas) {
        if (geometry instanceof AdvancedGeometryCollection) {
            AdvancedGeometryCollection geometryCollection = (AdvancedGeometryCollection) geometry;
            for (IAdvancedGeometry child : geometryCollection.getChildren()) {
                buildHatchedAreas(spatialEntity, child, hatchedAreas);
            }
        } else if (geometry.isPolygonal()) {
            drawingAttributes = new PolygonDrawableAttributes(false);
            drawingAttributes.setLineWidth(1);
            Color color = getColor(spatialEntity.getFillColor());
            drawingAttributes.setColors(new Color[] { color, color });
            drawingAttributes.setDottedLineStyle();
            if (geometry.isPotentiallyCurved()) {
                hatchedAreas.add(new EllipseDrawable(
                        spatialEntity.getIdentifier(), drawingAttributes,
                        (Ellipse) geometry.copyOf()));
            } else {
                hatchedAreas.add(new PathDrawable(spatialEntity.getIdentifier(),
                        drawingAttributes,
                        (GeometryWrapper) geometry.copyOf()));
            }
        }
    }

    /**
     * Build a point drawable for the specified spatial entity and geometry.
     * 
     * @param spatialEntity
     *            Spatial entity that the point represents (in whole or in
     *            part).
     * @param geometry
     *            Geometry of the point to be created.
     * @param geometryIndex
     *            Index of the geometry within the spatial entity's geometry if
     *            the latter is a geometry collection, otherwise <code>-1</code>
     *            .
     * @return Point drawable that was built.
     */
    private AbstractDrawableComponent buildPoint(
            SpatialEntity<? extends IEntityIdentifier> spatialEntity,
            GeometryWrapper geometry, int geometryIndex) {
        DECollection collectionComponent = new DECollection();

        /*
         * Get the symbol names to be used for the outer (border) and inner
         * (fill) portions of the symbol.
         */
        String outerSymbol = PGEN_OUTER_SYMBOLS_FOR_VISUAL_FEATURE_SYMBOLS
                .get(spatialEntity.getSymbolShape());
        String innerSymbol = PGEN_INNER_SYMBOLS_FOR_VISUAL_FEATURE_SYMBOLS
                .get(spatialEntity.getSymbolShape());
        double outerDiameter = spatialEntity.getDiameter()
                * PGEN_SIZE_MULTIPLIERS_FOR_VISUAL_FEATURE_SYMBOLS
                        .get(spatialEntity.getSymbolShape());
        double innerDiameter = outerDiameter;
        Color borderColor = getColor(spatialEntity.getBorderColor());
        Color fillColor = (spatialEntity.getFillColor().getAlpha() == 0.0f
                ? Color.BLACK : getColor(spatialEntity.getFillColor()));

        /*
         * If the inner symbol is the same as the outer one, then use a reduced
         * diameter for the inner one, and paint the inner one last, as both of
         * them will be filled, with the inner one simply overlaying the outer
         * one. Otherwise, paint the inner one first, as the outer one will be
         * an unfilled border around the inner one.
         */
        if (innerSymbol == null) {
            collectionComponent.add(buildOuterPoint(spatialEntity, outerSymbol,
                    borderColor, geometry, geometryIndex, outerDiameter));
            innerDiameter -= spatialEntity.getBorderThickness() * 2.0;
            AbstractDrawableComponent innerPoint = buildInnerPoint(
                    spatialEntity, outerSymbol, fillColor, geometry,
                    geometryIndex, innerDiameter);
            if (innerPoint != null) {
                collectionComponent.add(innerPoint);
            }
        } else {
            AbstractDrawableComponent innerPoint = buildInnerPoint(
                    spatialEntity, innerSymbol, fillColor, geometry,
                    geometryIndex, innerDiameter);
            if (innerPoint != null) {
                collectionComponent.add(innerPoint);
            }
            collectionComponent.add(buildOuterPoint(spatialEntity, outerSymbol,
                    borderColor, geometry, geometryIndex, outerDiameter));
        }
        return collectionComponent;
    }

    /**
     * Build the drawable representing the border of a point for the specified
     * geometry of the specified spatial entity.
     * 
     * @param spatialEntity
     *            Spatial entity for which the point is being created.
     * @param symbol
     *            PGEN symbol identifier, indicating what symbol should be
     *            drawn.
     * @param color
     *            Color of the point border to be built.
     * @param geometry
     *            Geometry of the point.
     * @param geometryIndex
     *            Index of the point within the spatial entity's geometry if the
     *            latter is a geometry collection, otherwise <code>-1</code> .
     * @param sizeScale
     *            Size scaling to be applied to the PGEN symbol being created.
     * @return Created drawable.
     */
    private AbstractDrawableComponent buildOuterPoint(
            SpatialEntity<? extends IEntityIdentifier> spatialEntity,
            String symbol, Color color, GeometryWrapper geometry,
            int geometryIndex, double sizeScale) {
        SymbolDrawableAttributes drawingAttributes = new SymbolDrawableAttributes(
                SymbolDrawableAttributes.Element.OUTER);
        this.drawingAttributes = drawingAttributes;
        drawingAttributes.setSolidLineStyle();
        drawingAttributes
                .setLineWidth((float) spatialEntity.getBorderThickness());
        drawingAttributes.setColors(new Color[] { color, color });
        if (hasNonEmptyLabel(spatialEntity)) {
            drawingAttributes
                    .setLabel(convertNewlinesToArray(spatialEntity.getLabel()));
        }
        drawingAttributes.setSizeScale(sizeScale);
        drawingAttributes.setTextPosition(
                getTextPositionForSpatialEntity(spatialEntity));
        drawingAttributes.setGeometryIndex(geometryIndex);
        SymbolDrawable outerPoint = new SymbolDrawable(
                spatialEntity.getIdentifier(), drawingAttributes, symbol,
                geometry);
        outerPoint.setMovable(
                (spatialEntity.getDragCapability() != DragCapability.NONE)
                        && ((geometryIndex == -1) || spatialEntity
                                .isMultiGeometryPointsDraggable()));
        return outerPoint;
    }

    /**
     * Build the drawable representing the interor (fill) of a point for the
     * specified geometry of the specified spatial entity.
     * 
     * @param spatialEntity
     *            Spatial entity for which the point is being created.
     * @param symbol
     *            PGEN symbol identifier, indicating what symbol should be
     *            drawn.
     * @param color
     *            Color of the point fill to be built.
     * @param geometry
     *            Geometry of the point.
     * @param geometryIndex
     *            Index of the point within the spatial entity's geometry if the
     *            latter is a geometry collection, otherwise <code>-1</code> .
     * @param sizeScale
     *            Size scaling to be applied to the PGEN symbol being created.
     * @return Created drawable, or <code>null</code> if no fill is needed.
     */
    private AbstractDrawableComponent buildInnerPoint(
            SpatialEntity<? extends IEntityIdentifier> spatialEntity,
            String symbol, Color color, GeometryWrapper geometry,
            int geometryIndex, double sizeScale) {
        if (sizeScale > 0.0) {
            SymbolDrawableAttributes drawingAttributes = new SymbolDrawableAttributes(
                    SymbolDrawableAttributes.Element.INNER);
            drawingAttributes.setSolidLineStyle();
            drawingAttributes.setLineWidth(0.0f);
            drawingAttributes.setColors(new Color[] { color, color });
            drawingAttributes.setLabel(null);
            drawingAttributes.setSizeScale(sizeScale);
            drawingAttributes.setGeometryIndex(geometryIndex);
            SymbolDrawable innerPoint = new SymbolDrawable(
                    spatialEntity.getIdentifier(), drawingAttributes, symbol,
                    geometry);
            innerPoint.setMovable(false);
            return innerPoint;
        }
        return null;
    }

    /**
     * Build a line drawable for the specified spatial entity and geometry.
     * 
     * @param spatialEntity
     *            Spatial entity that the line represents (in whole or in part).
     * @param geometry
     *            Geometry of the line to be created.
     * @param geometryIndex
     *            Index of the geometry within the spatial entity's geometry if
     *            the latter is a geometry collection, otherwise <code>-1</code>
     *            .
     * @return Line drawable that was built.
     */
    private AbstractDrawableComponent buildLine(
            SpatialEntity<? extends IEntityIdentifier> spatialEntity,
            GeometryWrapper geometry, int geometryIndex) {

        /*
         * Create the path drawable.
         */
        PathDrawable drawableComponent = null;
        LineDrawableAttributes drawingAttributes = new LineDrawableAttributes();
        this.drawingAttributes = drawingAttributes;
        drawingAttributes.setSizeScale(2);
        if (spatialEntity.getBorderStyle() == BorderStyle.SOLID) {
            drawingAttributes.setSolidLineStyle();
        } else if (spatialEntity.getBorderStyle() == BorderStyle.DASHED) {
            drawingAttributes.setDashedLineStyle();
        } else {
            drawingAttributes.setDottedLineStyle();
        }
        drawingAttributes
                .setLineWidth((float) spatialEntity.getBorderThickness());
        Color color = getColor(spatialEntity.getBorderColor());
        drawingAttributes.setColors(new Color[] { color, color });
        drawingAttributes.setFillPattern(FillPattern.SOLID);
        if (hasNonEmptyLabel(spatialEntity)) {
            drawingAttributes
                    .setLabel(convertNewlinesToArray(spatialEntity.getLabel()));
        }
        drawingAttributes.setTextPosition(
                getTextPositionForSpatialEntity(spatialEntity));
        drawingAttributes.setGeometryIndex(geometryIndex);
        drawableComponent = new PathDrawable(spatialEntity.getIdentifier(),
                drawingAttributes, (GeometryWrapper) geometry.copyOf());
        DragCapability dragCapability = spatialEntity.getDragCapability();
        drawableComponent.setMovable((dragCapability == DragCapability.WHOLE)
                || (dragCapability == DragCapability.ALL));
        drawableComponent
                .setVertexEditable((dragCapability == DragCapability.PART)
                        || (dragCapability == DragCapability.ALL));
        drawableComponent.setResizable(spatialEntity.isScaleable());
        drawableComponent.setRotatable(spatialEntity.isRotatable());

        /*
         * Finish the creation of the line by building a bounding box for it if
         * necessary as well.
         */
        return finishMultiPointShape(drawableComponent);
    }

    /**
     * Build a polygon drawable for the specified spatial entity and geometry.
     * 
     * @param spatialEntity
     *            Spatial entity that the polygon represents (in whole or in
     *            part).
     * @param geometry
     *            Geometry of the polygon to be created.
     * @param geometryIndex
     *            Index of the geometry within the spatial entity's geometry if
     *            the latter is a geometry collection, otherwise <code>-1</code>
     *            .
     * @return Polygon drawable that was built.
     */
    private AbstractDrawableComponent buildPolygon(
            SpatialEntity<? extends IEntityIdentifier> spatialEntity,
            GeometryWrapper geometry, int geometryIndex) {

        /*
         * Create the polygon drawable.
         */
        PathDrawable drawableComponent = null;
        PolygonDrawableAttributes drawingAttributes = new PolygonDrawableAttributes(
                (spatialEntity.getFillColor().getAlpha() > 0.0));
        this.drawingAttributes = drawingAttributes;
        drawingAttributes.setSizeScale(2);
        if (spatialEntity.getBorderStyle() == BorderStyle.SOLID) {
            drawingAttributes.setSolidLineStyle();
        } else if (spatialEntity.getBorderStyle() == BorderStyle.DASHED) {
            drawingAttributes.setDashedLineStyle();
        } else {
            drawingAttributes.setDottedLineStyle();
        }
        drawingAttributes
                .setLineWidth((float) spatialEntity.getBorderThickness());
        Color borderColor = getColor(spatialEntity.getBorderColor());
        Color fillColor = getColor(spatialEntity.getFillColor());
        drawingAttributes.setColors(new Color[] { borderColor, fillColor });
        drawingAttributes.setFillPattern(FillPattern.SOLID);
        if (hasNonEmptyLabel(spatialEntity)) {
            drawingAttributes
                    .setLabel(convertNewlinesToArray(spatialEntity.getLabel()));
        }
        drawingAttributes.setTextPosition(
                getTextPositionForSpatialEntity(spatialEntity));
        drawingAttributes.setGeometryIndex(geometryIndex);
        drawableComponent = new PathDrawable(spatialEntity.getIdentifier(),
                drawingAttributes, (GeometryWrapper) geometry.copyOf());
        DragCapability dragCapability = spatialEntity.getDragCapability();
        drawableComponent.setMovable((dragCapability == DragCapability.WHOLE)
                || (dragCapability == DragCapability.ALL));
        drawableComponent
                .setVertexEditable((dragCapability == DragCapability.PART)
                        || (dragCapability == DragCapability.ALL));
        drawableComponent.setResizable(spatialEntity.isScaleable());
        drawableComponent.setRotatable(spatialEntity.isRotatable());

        /*
         * Finish the creation of the polygon by building a bounding box for it
         * if necessary as well.
         */
        return finishMultiPointShape(drawableComponent);
    }

    /**
     * Build an ellipse drawable for the specified spatial entity and geometry.
     * 
     * @param spatialEntity
     *            Spatial entity that the ellipse represents (in whole or in
     *            part).
     * @param geometry
     *            Geometry of the ellipse to be created.
     * @param geometryIndex
     *            Index of the geometry within the spatial entity's geometry if
     *            the latter is a geometry collection, otherwise <code>-1</code>
     *            .
     * @return Ellipse drawable that was built.
     */
    private AbstractDrawableComponent buildEllipse(
            SpatialEntity<? extends IEntityIdentifier> spatialEntity,
            Ellipse geometry, int geometryIndex) {

        /*
         * Create the ellipse drawable.
         */
        EllipseDrawable drawableComponent = null;
        PolygonDrawableAttributes drawingAttributes = new PolygonDrawableAttributes(
                (spatialEntity.getFillColor().getAlpha() > 0.0));
        this.drawingAttributes = drawingAttributes;
        drawingAttributes.setSizeScale(2);
        if (spatialEntity.getBorderStyle() == BorderStyle.SOLID) {
            drawingAttributes.setSolidLineStyle();
        } else if (spatialEntity.getBorderStyle() == BorderStyle.DASHED) {
            drawingAttributes.setDashedLineStyle();
        } else {
            drawingAttributes.setDottedLineStyle();
        }
        drawingAttributes
                .setLineWidth((float) spatialEntity.getBorderThickness());
        Color borderColor = getColor(spatialEntity.getBorderColor());
        Color fillColor = getColor(spatialEntity.getFillColor());
        drawingAttributes.setColors(new Color[] { borderColor, fillColor });
        drawingAttributes.setFillPattern(FillPattern.SOLID);
        if (hasNonEmptyLabel(spatialEntity)) {
            drawingAttributes
                    .setLabel(convertNewlinesToArray(spatialEntity.getLabel()));
        }
        drawingAttributes.setTextPosition(
                getTextPositionForSpatialEntity(spatialEntity));
        drawingAttributes.setGeometryIndex(geometryIndex);
        drawableComponent = new EllipseDrawable(spatialEntity.getIdentifier(),
                drawingAttributes, (Ellipse) geometry.copyOf());
        DragCapability dragCapability = spatialEntity.getDragCapability();
        drawableComponent.setMovable((dragCapability == DragCapability.WHOLE)
                || (dragCapability == DragCapability.ALL));
        drawableComponent.setResizable(spatialEntity.isScaleable());
        drawableComponent.setRotatable(spatialEntity.isRotatable());

        /*
         * Finish the creation of the ellipse by building a bounding box for it
         * if necessary as well.
         */
        return finishMultiPointShape(drawableComponent);
    }

    /**
     * Finish building the specified multi-point drawable by putting a bounding
     * box around it if appropriate.
     * 
     * @param drawable
     *            Drawable to be finished.
     * @return Completed drawable component; this may be a collection of
     *         drawables.
     */
    private AbstractDrawableComponent finishMultiPointShape(
            MultiPointDrawable<?> drawable) {

        /*
         * Create a bounding box drawable, if appropriate to this drawable.
         */
        BoundingBoxDrawable boundingComponent = buildBoundingBoxDrawable(
                drawable, drawable);
        if (boundingComponent != null) {

            /*
             * Create a collection drawable to hold the bounded drawable and the
             * bounding box, and return that.
             */
            DECollection collectionComponent = new DECollection();
            collectionComponent.add(drawable);
            collectionComponent.add(boundingComponent);
            return collectionComponent;
        }
        return drawable;
    }

    /**
     * Build a text drawable for the specified spatial entity and geometry.
     * 
     * @param spatialEntity
     *            Spatial entity whose label the text represents.
     * @param combinable
     *            Flag indicating whether or not the text drawable should be
     *            capable of being combined with other text drawables when they
     *            occupy the same location.
     * @return Text drawable that was built.
     */
    private TextDrawable buildText(
            SpatialEntity<? extends IEntityIdentifier> spatialEntity,
            boolean combinable) {
        Coordinate centroid = AdvancedGeometryUtilities
                .getCentroid(spatialEntity.getGeometry());
        return new TextDrawable(spatialEntity.getIdentifier(),
                drawingAttributes, spatialEntity.getTextSize(),
                getColor(spatialEntity.getTextColor()), centroid, combinable);
    }

    /**
     * Get the text positioner for the specified spatial entity.
     * 
     * @param spatialEntity
     *            Spatial entity for which a text positioner should be fetched.
     * @return Text positioner for the spatial entity.
     */
    private TextPositioner getTextPositionForSpatialEntity(
            SpatialEntity<? extends IEntityIdentifier> spatialEntity) {
        return (spatialEntity.getTextOffsetLength() == 0.0
                ? TextPositioner.CENTERED
                : TextPositioner.getInstance(
                        spatialEntity.getTextOffsetDirection(),
                        spatialEntity.getTextOffsetLength()));
    }

    /**
     * Convert the specified color into an AWT <code>Color</code>.
     * 
     * @param color
     *            Color to be converted.
     * @return Converted color.
     */
    private Color getColor(com.raytheon.uf.common.colormap.Color color) {
        return new Color((int) (color.getRed() * 255.0),
                (int) (color.getGreen() * 255.0),
                (int) (color.getBlue() * 255.0),
                (int) (color.getAlpha() * 255.0));
    }
}

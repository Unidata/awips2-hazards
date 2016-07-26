/**
 * This software was developed and / or modified by the National Oceanic and
 * Atmospheric Administration (NOAA), Earth System Research Laboratory (ESRL),
 * Global Systems Division (GSD), Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.spatialdisplay.drawables;

import gov.noaa.gsd.common.visuals.BorderStyle;
import gov.noaa.gsd.common.visuals.DragCapability;
import gov.noaa.gsd.common.visuals.FillStyle;
import gov.noaa.gsd.common.visuals.SpatialEntity;
import gov.noaa.gsd.common.visuals.SymbolShape;
import gov.noaa.gsd.viz.hazards.spatialdisplay.SpatialDisplay;
import gov.noaa.gsd.viz.hazards.spatialdisplay.entities.HazardEventHatchingEntityIdentifier;
import gov.noaa.gsd.viz.hazards.spatialdisplay.entities.IEntityIdentifier;
import gov.noaa.nws.ncep.ui.pgen.display.FillPatternList.FillPattern;
import gov.noaa.nws.ncep.ui.pgen.elements.AbstractDrawableComponent;
import gov.noaa.nws.ncep.ui.pgen.elements.DECollection;
import gov.noaa.nws.ncep.ui.pgen.elements.Layer;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableMap;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygonal;

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
     * Comparator to sort strings longest to shortest.
     */
    private static final Comparator<String> DECREASING_LENGTH_COMPARATOR = new Comparator<String>() {
        @Override
        public int compare(String o1, String o2) {
            return Integer.compare(o2.length(), o1.length());
        }
    };

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
     * @param spatialDisplay
     *            Spatial display for which the components are to be built.
     * @param spatialEntity
     *            Spatial entity for which the components are to be built. The
     *            entity's geometry may consist of a single point, line, or
     *            polygon, or else a geometry collection holding any mix of one
     *            or more points, lines, and polygons, but no nested
     *            collections.
     * @param activeLayer
     *            PGEN active layer.
     * @param selected
     *            Flag indicating that this entity is part of a hazard event
     *            that is selected.
     */
    public void buildDrawableComponents(SpatialDisplay spatialDisplay,
            SpatialEntity<? extends IEntityIdentifier> spatialEntity,
            Layer activeLayer, boolean selected) {

        /*
         * Only create drawables for the entity's geometry if the entity is not
         * intended solely for hazard hatching.
         */
        if (spatialEntity.getIdentifier() instanceof HazardEventHatchingEntityIdentifier == false) {

            /*
             * If the spatial entity has a geometry collection, create a
             * drawable component for each sub-geometry separately. Otherwise,
             * create a single drawable component for the entire geometry.
             */
            Geometry geometry = spatialEntity.getGeometry();
            if (geometry instanceof GeometryCollection) {
                GeometryCollection geometryCollection = (GeometryCollection) geometry;
                for (int j = 0; j < geometryCollection.getNumGeometries(); j++) {
                    buildDrawableComponent(spatialDisplay, spatialEntity,
                            geometryCollection.getGeometryN(j),
                            (geometryCollection.getNumGeometries() == 1 ? -1
                                    : j), activeLayer, selected);
                }
            } else {
                buildDrawableComponent(spatialDisplay, spatialEntity, geometry,
                        -1, activeLayer, selected);
            }
        }

        /*
         * Add a text drawable if appropriate.
         */
        if (spatialEntity.getLabel() != null
                && spatialEntity.getLabel().isEmpty() == false) {
            TextDrawable text = buildText(spatialEntity,
                    spatialDisplay.getActiveLayer());
            spatialDisplay.addElement(text, spatialEntity, false);
        }
    }

    /**
     * Build any hatched areas for the specified spatial entity.
     * 
     * @param spatialDisplay
     *            Spatial display for which the areas are to be created.
     * @param spatialEntity
     *            Entity that has hatched geometry.
     * @param activeLayer
     *            PGEN active layer.
     */
    public void buildHatchedAreas(SpatialDisplay spatialDisplay,
            SpatialEntity<? extends IEntityIdentifier> spatialEntity,
            Layer activeLayer) {

        /*
         * Build hatched areas if the entity has a hatch-style fill.
         */
        if (spatialEntity.getFillStyle() == FillStyle.HATCHED) {
            buildHatchedAreas(spatialDisplay, spatialEntity,
                    spatialEntity.getGeometry(), activeLayer);
        }
    }

    /**
     * Scan through the specified drawable components and amalgamate text
     * components that use the same centroid coordinate, returning the text
     * components that are to be removed as they are now superfluous. Note that
     * any components returned have been removed by that point from the
     * specified list.
     * <p>
     * This method is executed just prior to spatial display refresh. It looks
     * through all drawable components for {@link TextDrawable} components. For
     * each set of such components that use the same centroid coordinate, it
     * takes the text string arrays from the components, places their string
     * arrays into the first such component, sorts the strings by decreasing
     * length, and puts together a list of the <code>HazardServicesText</code>
     * components containing the now duplicate text strings to be returned and
     * then removed by the caller.
     * </p>
     * 
     * @param drawableComponents
     *            Spatial display drawable components.
     * @return Text components that are now superfluous and must be removed.
     */
    public Collection<AbstractDrawableComponent> amalgamateTextComponents(
            List<AbstractDrawableComponent> drawableComponents) {

        /*
         * Put together a map of coordinates to lists of text components, so
         * that the ones that share coordinates may be grouped together.
         */
        Map<Coordinate, List<TextDrawable>> drawablesForCoordinates = new HashMap<>();
        for (AbstractDrawableComponent component : drawableComponents) {
            if ((component instanceof TextDrawable)
                    && (component instanceof TextDrawable)) {
                TextDrawable textComponent = (TextDrawable) component;
                Coordinate coordinate = textComponent.getPosition();
                List<TextDrawable> componentList = drawablesForCoordinates
                        .get(coordinate);
                if (componentList == null) {
                    componentList = new ArrayList<>();
                    drawablesForCoordinates.put(coordinate, componentList);
                }
                componentList.add(textComponent);
            }
        }

        /*
         * For each list of text components sharing a common coordinate, combine
         * them into a single multi-line text component, and remember the others
         * so that they may be returned as those that need deletion.
         */
        Set<AbstractDrawableComponent> superfluousComponents = new HashSet<>();
        for (Map.Entry<Coordinate, List<TextDrawable>> entry : drawablesForCoordinates
                .entrySet()) {

            /*
             * Do nothing if only one component has this coordinate.
             */
            List<TextDrawable> componentList = entry.getValue();
            if (componentList.size() == 1) {
                continue;
            }

            /*
             * Add the text strings from any components after the first one to
             * the first one, making the latter multi-line, and remove any
             * subsequent ones from the original list, as well as tracking them
             * as superfluous.
             */
            TextDrawable firstTextComponent = null;
            List<String> textStringList = new ArrayList<>();
            String[] textStringArray = null;
            for (int j = 0; j < componentList.size(); j++) {
                TextDrawable currentTextComponent = componentList.get(j);
                textStringArray = currentTextComponent.getText();
                if ((textStringArray != null) && (textStringArray.length > 0)) {
                    for (int k = 0; k < textStringArray.length; k++) {
                        String currentArrayString = textStringArray[k];
                        if ((currentArrayString != null)
                                && (textStringList.contains(currentArrayString) == false)) {
                            textStringList.add(currentArrayString);
                        }
                    }
                }
                if (j == 0) {
                    firstTextComponent = currentTextComponent;
                } else {
                    superfluousComponents.add(currentTextComponent);
                    drawableComponents.remove(currentTextComponent);
                }
            }

            Collections.sort(textStringList, DECREASING_LENGTH_COMPARATOR);
            String[] textSetStringArray = textStringList
                    .toArray(new String[textStringList.size()]);
            firstTextComponent.setText(textSetStringArray);
        }
        return superfluousComponents;
    }

    // Private Methods

    /**
     * Build a single drawable component for the specified spatial entity.
     * 
     * @param spatialDisplay
     *            Spatial display for which the components are to be built.
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
     * @param activeLayer
     *            PGEN active layer.
     * @param selected
     *            Flag indicating that this entity is part of a hazard event
     *            that is selected.
     */
    private void buildDrawableComponent(SpatialDisplay spatialDisplay,
            SpatialEntity<? extends IEntityIdentifier> spatialEntity,
            Geometry geometry, int geometryIndex, Layer activeLayer,
            boolean selected) {

        Class<?> geometryClass = geometry.getClass();

        AbstractDrawableComponent drawableComponent = null;
        if (geometryClass.equals(Point.class)) {
            drawableComponent = buildPoint(spatialEntity, geometry,
                    geometryIndex, activeLayer);
        } else if (geometryClass.equals(LineString.class)) {
            drawableComponent = buildLine(spatialEntity, geometry,
                    geometryIndex, activeLayer);
        } else {
            drawableComponent = buildPolygon(spatialEntity, geometry,
                    geometryIndex, activeLayer);
        }
        spatialDisplay.addElement(drawableComponent, spatialEntity, selected);
    }

    /**
     * Build any hatched areas for any part of the specified geometry that is
     * polygonal.
     * 
     * @param spatialDisplay
     *            Spatial display for which the areas are to be created.
     * @param spatialEntity
     *            Entity of which this geometry is a part.
     * @param geometry
     *            Geometry for which to build the hatched areas.
     * @param activeLayer
     *            PGEN active layer.
     */
    private void buildHatchedAreas(SpatialDisplay spatialDisplay,
            SpatialEntity<? extends IEntityIdentifier> spatialEntity,
            Geometry geometry, Layer activeLayer) {
        if (geometry instanceof GeometryCollection) {
            GeometryCollection geometryCollection = (GeometryCollection) geometry;
            for (int j = 0; j < geometryCollection.getNumGeometries(); j++) {
                buildHatchedAreas(spatialDisplay, spatialEntity,
                        geometryCollection.getGeometryN(j), activeLayer);
            }
        } else if (geometry instanceof Polygonal) {
            drawingAttributes = new PolygonDrawableAttributes(false);
            drawingAttributes.setLineWidth(1);
            Color color = getColor(spatialEntity.getFillColor());
            drawingAttributes.setColors(new Color[] { color, color });
            drawingAttributes.setDottedLineStyle();
            AbstractDrawableComponent hatchedArea = new MultiPointDrawable(
                    spatialEntity.getIdentifier(), drawingAttributes,
                    (Geometry) geometry.clone(), activeLayer);
            spatialDisplay.addHatchedArea(hatchedArea);
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
     * @param activeLayer
     *            PGEN active layer.
     * @return Point drawable that was built.
     */
    private AbstractDrawableComponent buildPoint(
            SpatialEntity<? extends IEntityIdentifier> spatialEntity,
            Geometry geometry, int geometryIndex, Layer activeLayer) {
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
        Coordinate location = geometry.getCoordinate();
        Color borderColor = getColor(spatialEntity.getBorderColor());
        Color fillColor = (spatialEntity.getFillColor().getAlpha() == 0.0f ? Color.BLACK
                : getColor(spatialEntity.getFillColor()));

        /*
         * If the inner symbol is the same as the outer one, then use a reduced
         * diameter for the inner one, and paint the inner one last, as both of
         * them will be filled, with the inner one simply overlaying the outer
         * one. Otherwise, paint the inner one first, as the outer one will be
         * an unfilled border around the inner one.
         */
        if (innerSymbol == null) {
            collectionComponent.add(buildOuterPoint(spatialEntity, activeLayer,
                    outerSymbol, borderColor, location, geometryIndex,
                    outerDiameter));
            innerDiameter -= spatialEntity.getBorderThickness() * 2.0;
            AbstractDrawableComponent innerPoint = buildInnerPoint(
                    spatialEntity, activeLayer, outerSymbol, fillColor,
                    location, geometryIndex, innerDiameter);
            if (innerPoint != null) {
                collectionComponent.add(innerPoint);
            }
        } else {
            AbstractDrawableComponent innerPoint = buildInnerPoint(
                    spatialEntity, activeLayer, innerSymbol, fillColor,
                    location, geometryIndex, innerDiameter);
            if (innerPoint != null) {
                collectionComponent.add(innerPoint);
            }
            collectionComponent.add(buildOuterPoint(spatialEntity, activeLayer,
                    outerSymbol, borderColor, location, geometryIndex,
                    outerDiameter));
        }
        return collectionComponent;
    }

    /**
     * Build the drawable representing the border of a point for the specified
     * geometry of the specified spatial entity.
     * 
     * @param spatialEntity
     *            Spatial entity for which the point is being created.
     * @param activeLayer
     *            PGEN active layer.
     * @param symbol
     *            PGEN symbol identifier, indicating what symbol should be
     *            drawn.
     * @param color
     *            Color of the point border to be built.
     * @param location
     *            Point's location.
     * @param geometryIndex
     *            Index of the point within the spatial entity's geometry if the
     *            latter is a geometry collection, otherwise <code>-1</code> .
     * @param sizeScale
     *            Size scaling to be applied to the PGEN symbol being created.
     * @return Created drawable.
     */
    private AbstractDrawableComponent buildOuterPoint(
            SpatialEntity<? extends IEntityIdentifier> spatialEntity,
            Layer activeLayer, String symbol, Color color, Coordinate location,
            int geometryIndex, double sizeScale) {
        SymbolDrawableAttributes drawingAttributes = new SymbolDrawableAttributes(
                SymbolDrawableAttributes.Element.OUTER);
        this.drawingAttributes = drawingAttributes;
        drawingAttributes.setSolidLineStyle();
        drawingAttributes.setLineWidth((float) spatialEntity
                .getBorderThickness());
        drawingAttributes.setColors(new Color[] { color, color });
        if (spatialEntity.getLabel() != null) {
            drawingAttributes
                    .setLabel(new String[] { spatialEntity.getLabel() });
        }
        drawingAttributes.setSizeScale(sizeScale);
        drawingAttributes
                .setTextPosition(getTextPositionForSpatialEntity(spatialEntity));
        drawingAttributes.setGeometryIndex(geometryIndex);
        SymbolDrawable outerPoint = new SymbolDrawable(
                spatialEntity.getIdentifier(), drawingAttributes, symbol,
                location, activeLayer);
        outerPoint
                .setMovable((spatialEntity.getDragCapability() != DragCapability.NONE)
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
     * @param activeLayer
     *            PGEN active layer.
     * @param symbol
     *            PGEN symbol identifier, indicating what symbol should be
     *            drawn.
     * @param color
     *            Color of the point fill to be built.
     * @param location
     *            Point's location.
     * @param geometryIndex
     *            Index of the point within the spatial entity's geometry if the
     *            latter is a geometry collection, otherwise <code>-1</code> .
     * @param sizeScale
     *            Size scaling to be applied to the PGEN symbol being created.
     * @return Created drawable, or <code>null</code> if no fill is needed.
     */
    private AbstractDrawableComponent buildInnerPoint(
            SpatialEntity<? extends IEntityIdentifier> spatialEntity,
            Layer activeLayer, String symbol, Color color, Coordinate location,
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
                    location, activeLayer);
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
     * @param activeLayer
     *            PGEN active layer.
     * @return Line drawable that was built.
     */
    private AbstractDrawableComponent buildLine(
            SpatialEntity<? extends IEntityIdentifier> spatialEntity,
            Geometry geometry, int geometryIndex, Layer activeLayer) {
        MultiPointDrawable drawableComponent = null;
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
        drawingAttributes.setLineWidth((float) spatialEntity
                .getBorderThickness());
        Color color = getColor(spatialEntity.getBorderColor());
        drawingAttributes.setColors(new Color[] { color, color });
        drawingAttributes.setFillPattern(FillPattern.SOLID);
        if (spatialEntity.getLabel() != null) {
            drawingAttributes
                    .setLabel(new String[] { spatialEntity.getLabel() });
        }
        drawingAttributes
                .setTextPosition(getTextPositionForSpatialEntity(spatialEntity));
        drawingAttributes.setGeometryIndex(geometryIndex);
        drawableComponent = new MultiPointDrawable(
                spatialEntity.getIdentifier(), drawingAttributes,
                (Geometry) geometry.clone(), activeLayer);
        DragCapability dragCapability = spatialEntity.getDragCapability();
        drawableComponent.setEditable((dragCapability == DragCapability.PART)
                || (dragCapability == DragCapability.ALL));
        drawableComponent.setMovable((dragCapability == DragCapability.WHOLE)
                || (dragCapability == DragCapability.ALL));
        return drawableComponent;
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
     * @param activeLayer
     *            PGEN active layer.
     * @return Polygon drawable that was built.
     */
    private AbstractDrawableComponent buildPolygon(
            SpatialEntity<? extends IEntityIdentifier> spatialEntity,
            Geometry geometry, int geometryIndex, Layer activeLayer) {
        MultiPointDrawable drawableComponent = null;
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
        drawingAttributes.setLineWidth((float) spatialEntity
                .getBorderThickness());
        Color borderColor = getColor(spatialEntity.getBorderColor());
        Color fillColor = getColor(spatialEntity.getFillColor());
        drawingAttributes.setColors(new Color[] { borderColor, fillColor });
        drawingAttributes.setFillPattern(FillPattern.SOLID);
        if (spatialEntity.getLabel() != null) {
            drawingAttributes
                    .setLabel(new String[] { spatialEntity.getLabel() });
        }
        drawingAttributes
                .setTextPosition(getTextPositionForSpatialEntity(spatialEntity));
        drawingAttributes.setGeometryIndex(geometryIndex);
        drawableComponent = new MultiPointDrawable(
                spatialEntity.getIdentifier(), drawingAttributes,
                (Geometry) geometry.clone(), activeLayer);
        DragCapability dragCapability = spatialEntity.getDragCapability();
        drawableComponent.setEditable((dragCapability == DragCapability.PART)
                || (dragCapability == DragCapability.ALL));
        drawableComponent.setMovable((dragCapability == DragCapability.WHOLE)
                || (dragCapability == DragCapability.ALL));
        return drawableComponent;
    }

    /**
     * Build a text drawable for the specified spatial entity and geometry.
     * 
     * @param spatialEntity
     *            Spatial entity whose label the text represents.
     * @param activeLayer
     *            PGEN active layer.
     * @return Text drawable that was built.
     */
    private TextDrawable buildText(
            SpatialEntity<? extends IEntityIdentifier> spatialEntity,
            Layer activeLayer) {
        Point centerPoint = spatialEntity.getGeometry().getCentroid();
        return new TextDrawable(spatialEntity.getIdentifier(),
                drawingAttributes, (float) spatialEntity.getTextSize(),
                getColor(spatialEntity.getTextColor()),
                centerPoint.getCoordinate(), activeLayer);
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
        return (spatialEntity.getTextOffsetLength() == 0.0 ? TextPositioner.CENTERED
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

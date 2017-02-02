/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Evaluation & Decision Support Branch (EDS)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.spatialdisplay;

import gov.noaa.gsd.common.utilities.geometry.AdvancedGeometryUtilities;
import gov.noaa.gsd.common.visuals.SpatialEntity;
import gov.noaa.gsd.viz.hazards.spatialdisplay.SpatialPresenter.SpatialEntityType;
import gov.noaa.gsd.viz.hazards.spatialdisplay.drawables.BoundingBoxDrawable;
import gov.noaa.gsd.viz.hazards.spatialdisplay.drawables.DrawableBuilder;
import gov.noaa.gsd.viz.hazards.spatialdisplay.drawables.IDrawable;
import gov.noaa.gsd.viz.hazards.spatialdisplay.drawables.ManipulationPoint;
import gov.noaa.gsd.viz.hazards.spatialdisplay.drawables.MultiPointDrawable;
import gov.noaa.gsd.viz.hazards.spatialdisplay.drawables.PathDrawable;
import gov.noaa.gsd.viz.hazards.spatialdisplay.drawables.RotationManipulationPoint;
import gov.noaa.gsd.viz.hazards.spatialdisplay.drawables.SymbolDrawable;
import gov.noaa.gsd.viz.hazards.spatialdisplay.drawables.TextDrawable;
import gov.noaa.gsd.viz.hazards.spatialdisplay.drawables.VertexManipulationPoint;
import gov.noaa.gsd.viz.hazards.spatialdisplay.entities.IEntityIdentifier;
import gov.noaa.nws.ncep.ui.pgen.PgenRangeRecord;
import gov.noaa.nws.ncep.ui.pgen.PgenSession;
import gov.noaa.nws.ncep.ui.pgen.PgenUtil;
import gov.noaa.nws.ncep.ui.pgen.display.AbstractElementContainer;
import gov.noaa.nws.ncep.ui.pgen.display.CurveFitter;
import gov.noaa.nws.ncep.ui.pgen.display.DefaultElementContainer;
import gov.noaa.nws.ncep.ui.pgen.display.DisplayProperties;
import gov.noaa.nws.ncep.ui.pgen.display.IArc;
import gov.noaa.nws.ncep.ui.pgen.display.IAvnText;
import gov.noaa.nws.ncep.ui.pgen.display.IDisplayable;
import gov.noaa.nws.ncep.ui.pgen.display.ILine;
import gov.noaa.nws.ncep.ui.pgen.display.IMidCloudText;
import gov.noaa.nws.ncep.ui.pgen.display.IMultiPoint;
import gov.noaa.nws.ncep.ui.pgen.display.ISinglePoint;
import gov.noaa.nws.ncep.ui.pgen.display.ISymbol;
import gov.noaa.nws.ncep.ui.pgen.display.IText;
import gov.noaa.nws.ncep.ui.pgen.display.IText.DisplayType;
import gov.noaa.nws.ncep.ui.pgen.display.LineDisplayElement;
import gov.noaa.nws.ncep.ui.pgen.display.RasterElementContainer;
import gov.noaa.nws.ncep.ui.pgen.elements.AbstractDrawableComponent;
import gov.noaa.nws.ncep.ui.pgen.elements.DECollection;
import gov.noaa.nws.ncep.ui.pgen.elements.DrawableElement;
import gov.noaa.nws.ncep.ui.pgen.elements.Symbol;
import gov.noaa.nws.ncep.ui.pgen.gfa.IGfa;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.core.IGraphicsTarget;
import com.raytheon.uf.viz.core.IGraphicsTarget.PointStyle;
import com.raytheon.uf.viz.core.drawables.FillPatterns;
import com.raytheon.uf.viz.core.drawables.IShadedShape;
import com.raytheon.uf.viz.core.drawables.PaintProperties;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.map.IMapDescriptor;
import com.raytheon.viz.core.rsc.jts.JTSCompiler;
import com.raytheon.viz.core.rsc.jts.JTSCompiler.JTSGeometryData;
import com.raytheon.viz.ui.VizWorkbenchManager;
import com.raytheon.viz.ui.editor.AbstractEditor;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

/**
 * Description: Manager of the {@link IDrawable} objects used by the spatial
 * display.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Aug 27, 2016   19537    Chris.Golden Initial creation.
 * Sep 12, 2016   15934    Chris.Golden Changed to work with advanced
 *                                      geometries. Also added hit-testing
 *                                      code that buffers points being
 *                                      tested so as to make them easier to
 *                                      match.
 * Sep 21, 2016   15934    Chris.Golden Replaced MultiPointDrawable with new
 *                                      PathDrawable.
 * Sep 29, 2016   15928    Chris.Golden Changed handlebars to allow for
 *                                      different symbols and scales for
 *                                      different functions (rotation versus
 *                                      scaling versus vertex-moving). Added
 *                                      bounding box drawable replacement
 *                                      capability, so that bounding boxes
 *                                      may be updated visually during an
 *                                      edit by the mouse handlers. Added
 *                                      dimmed look for drawables being edited
 *                                      so that edited versions stand out 
 *                                      more. Fixed bugs in detection of
 *                                      drawables and their components under
 *                                      the mouse cursor. Added ability to
 *                                      handle polygons containing holes when
 *                                      vertex editing.
 * Oct 04, 2016   22736    Chris.Golden Fixed bug causing null pointer
 *                                      exception when tracking combinable
 *                                      text drawables. (Only seen once, so
 *                                      has not been reproducible, but this
 *                                      fix should ensure it does not occur
 *                                      again.)
 * Feb 01, 2017   15556    Chris.Golden Fixed bug caused by entities being
 *                                      added to (for example) the unselected
 *                                      list, then removed from the selected
 *                                      list, meaning that the entities would
 *                                      not show up. Instances now have an
 *                                      awareness of what list entities are
 *                                      from, so that they are not removed
 *                                      inappropriately.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
class DrawableManager {

    // Private Enumerated Types

    /**
     * Renderers for types of handlebars. Each renderer has properties
     * indicating how it is to paint its handlebars, and each also has a
     * {@link HandlebarTypeRenderer#render(List, IGraphicsTarget, PaintProperties)}
     * method that given a list of locations in pixel space renders handlebars
     * at those locations.
     */
    private enum HandlebarTypeRenderer {

        MOVE_VERTEX(Display.getCurrent().getSystemColor(SWT.COLOR_BLACK)
                .getRGB(), Display.getCurrent().getSystemColor(SWT.COLOR_GRAY)
                .getRGB(), 1.7f, 1.3f, PointStyle.DISC),

        SCALE(Display.getCurrent().getSystemColor(SWT.COLOR_BLACK).getRGB(),
                Display.getCurrent().getSystemColor(SWT.COLOR_GRAY).getRGB(),
                1.8f, 1.4f, PointStyle.DISC),

        ROTATE(Display.getCurrent().getSystemColor(SWT.COLOR_BLACK).getRGB(),
                Display.getCurrent().getSystemColor(SWT.COLOR_GRAY).getRGB(),
                2.5f, 2.0f, PointStyle.DISC);

        // Private Variables

        /**
         * Border color.
         */
        private final RGB borderColor;

        /**
         * Fill color.
         */
        private final RGB fillColor;

        /**
         * Border magnification for symbol drawing.
         */
        private final float borderMagnification;

        /**
         * Fill magnification for symbol drawing.
         */
        private final float fillMagnification;

        /**
         * Symbol to be drawn.
         */
        private final PointStyle symbol;

        // Private Constructors

        /**
         * Construct a standard instance.
         * 
         * @param borderColor
         *            Border color.
         * @param fillColor
         *            Fill color.
         * @param borderMagnification
         *            Border magnification for symbol drawing.
         * @param fillMagnification
         *            Fill magnification for symbol drawing.
         * @param symbol
         *            Symbol to be drawn.
         */
        private HandlebarTypeRenderer(RGB borderColor, RGB fillColor,
                float borderMagnification, float fillMagnification,
                PointStyle symbol) {
            this.borderColor = borderColor;
            this.fillColor = fillColor;
            this.borderMagnification = borderMagnification;
            this.fillMagnification = fillMagnification;
            this.symbol = symbol;
        }

        // Public Methods

        /**
         * Render handlebars of this type at the specified locations.
         * 
         * @param points
         *            Locations of the handlebars, each as an two-element array
         *            holding the X and Y coordinates in pixel space.
         * @param target
         *            Graphics target to which to draw the handlebars.
         * @param paintProperties
         *            Paint properties to be used.
         */
        public void render(List<double[]> points, IGraphicsTarget target,
                PaintProperties paintProperties) throws VizException {
            if (points.isEmpty() == false) {
                target.drawPoints(points, borderColor, symbol,
                        borderMagnification);
                target.drawPoints(points, fillColor, symbol, fillMagnification);
            }
        }
    }

    // Private Classes

    /**
     * Vector element container, a replacement for its superclass that creates
     * objects that can handle alpha transparency instead of the usual
     * {@link LineDisplayElement}.
     */
    private class DefaultVectorElementContainer extends DefaultElementContainer {

        // Private Variables

        /**
         * Display element factory customized for spatial display needs,
         * including lines that may have alpha transparency.
         */
        private PgenDisplayElementFactory factory;

        /**
         * Saved display properties, if any; copied from superclass since it is
         * inaccessible to this class's
         * {@link #draw(IGraphicsTarget, PaintProperties, DisplayProperties, boolean)}
         * method.
         */
        private DisplayProperties saveProps = null;

        /**
         * Zoom level, copied from superclass since it is inaccessible to this
         * class's
         * {@link #draw(IGraphicsTarget, PaintProperties, DisplayProperties, boolean)}
         * method.
         */
        private float zoomLevel = 0;

        // Public Constructors

        /**
         * Construct a standard instance.
         * 
         * @param element
         *            Element to be contained.
         * @param mapDescriptor
         *            Map descriptor.
         * @param target
         *            Target on which the contained element will be drawn.
         */
        public DefaultVectorElementContainer(DrawableElement element,
                IMapDescriptor mapDescriptor, IGraphicsTarget target) {
            super(element, mapDescriptor, target);
            factory = new PgenDisplayElementFactory(target, mapDescriptor);
        }

        // Public Methods

        @Override
        public void setMapDescriptor(IMapDescriptor mapDescriptor) {
            super.setMapDescriptor(mapDescriptor);
            factory = new PgenDisplayElementFactory(target, mapDescriptor);
        }

        /*
         * Copied from superclass, with some modifications to strip out code
         * that does not apply (e.g. dprops being given as null) and to speed up
         * drawing when zooming.
         */
        @Override
        public void draw(IGraphicsTarget target, PaintProperties paintProps,
                DisplayProperties dprops, boolean needsCreate) {

            /*
             * Create the displayables if they have not already been created, or
             * if not currently zooming and the zoom level has changed.
             */
            if ((displayEls == null)
                    || ((paintProps.isZooming() == false) && (paintProps
                            .getZoomLevel() != zoomLevel))) {
                needsCreate = true;
                zoomLevel = paintProps.getZoomLevel();
                factory.setLayerDisplayAttributes(dprops.getLayerMonoColor(),
                        dprops.getLayerColor(), dprops.getLayerFilled());
            }

            /*
             * Also create the displayables if the display properties have
             * changed, or if the drawable is of a type requiring recreation.
             */
            if ((dprops != null) && (dprops.equals(saveProps) == false)) {
                factory.setLayerDisplayAttributes(dprops.getLayerMonoColor(),
                        dprops.getLayerColor(), dprops.getLayerFilled());
                needsCreate = true;
            } else if (element instanceof IMidCloudText
                    || element instanceof IAvnText
                    || (element instanceof IText && ((IText) element)
                            .getDisplayType().equals(DisplayType.BOX))
                    || element instanceof IGfa || isCCFPArrow(element)) {
                if (PgenSession.getInstance().getPgenResource()
                        .isNeedsDisplay()) {
                    needsCreate = true;
                }
            }

            /*
             * (Re)create the displayables if needed.
             */
            if (needsCreate) {
                createDisplayables(paintProps);
            }

            /*
             * Remember the display properties.
             */
            saveProps = dprops;

            /*
             * Draw the displayables.
             */
            for (IDisplayable each : displayEls) {
                each.draw(target, paintProps);
            }
        }

        // Protected Methods

        @Override
        protected void createDisplayables(PaintProperties paintProps) {
            displayEls = createDisplayablesForElementContainer(displayEls,
                    factory, element, mapDescriptor, paintProps);
        }
    }

    /**
     * Raster element container, a replacement for its superclass that creates
     * objects that can handle alpha transparency instead of the usual
     * {@link LineDisplayElement}.
     */
    private class DefaultRasterElementContainer extends RasterElementContainer {

        // Private Variables

        /**
         * Display element factory customized for spatial display needs,
         * including lines that may have alpha transparency.
         */
        private PgenDisplayElementFactory factory;

        /**
         * Saved display properties, if any; copied from superclass since it is
         * inaccessible to this class's
         * {@link #draw(IGraphicsTarget, PaintProperties, DisplayProperties, boolean)}
         * method.
         */
        private DisplayProperties saveProps = null;

        // Public Constructors

        /**
         * Construct a standard instance.
         * 
         * @param element
         *            Element to be contained.
         * @param mapDescriptor
         *            Map descriptor.
         * @param target
         *            Target on which the contained element will be drawn.
         */
        public DefaultRasterElementContainer(DrawableElement element,
                IMapDescriptor mapDescriptor, IGraphicsTarget target) {
            super(element, mapDescriptor, target);
            factory = new PgenDisplayElementFactory(target, mapDescriptor);
        }

        // Public Methods

        @Override
        public void setMapDescriptor(IMapDescriptor mapDescriptor) {
            super.setMapDescriptor(mapDescriptor);
            factory = new PgenDisplayElementFactory(target, mapDescriptor);
        }

        /*
         * Copied from superclass, with only modification being the use of the
         * customized factory.
         */
        @Override
        public void draw(IGraphicsTarget target, PaintProperties paintProps,
                DisplayProperties dprops, boolean needsCreate) {

            if (displayEls == null) {
                needsCreate = true;

                /*
                 * TTR971 - needs to set display properties, otherwise the layer
                 * color may not take effect (e.g., after switching projection)
                 */
                factory.setLayerDisplayAttributes(dprops.getLayerMonoColor(),
                        dprops.getLayerColor(), dprops.getLayerFilled());
            }

            if ((dprops != null) && !dprops.equals(saveProps)) {
                factory.setLayerDisplayAttributes(dprops.getLayerMonoColor(),
                        dprops.getLayerColor(), dprops.getLayerFilled());
                needsCreate = true;
            }

            if (needsCreate) {
                createDisplayables(paintProps);
            }

            saveProps = dprops;

            for (IDisplayable each : displayEls) {
                each.draw(target, paintProps);
            }
        }

        // Protected Methods

        @Override
        protected void createDisplayables(PaintProperties paintProps) {
            displayEls = createDisplayablesForElementContainer(displayEls,
                    factory, element, mapDescriptor, paintProps);
        }
    }

    /**
     * Iterator for drawables, providing them in the order they are drawn (that
     * is, first hatching ones, then unselected, then selected, and then
     * tool-related). Note that {@link Iterator#remove()} is not supported by
     * instances of this class. No thread safety is provided by this iterator;
     * unpredictable behavior will occur if the contents of the
     * {@link SpatialEntity} lists managed by the {@link SpatialDisplay}, or
     * {@link DrawableManager#drawablesForSpatialEntities} change while an
     * instance of this iterator is being used.
     */
    private class DrawableIterator implements
            Iterator<AbstractDrawableComponent> {

        // Private Variables

        /**
         * Type of the spatial entity associated with the last iterated
         * drawable.
         */
        private SpatialEntityType type = SpatialEntityType.HATCHING;

        /**
         * Index of the spatial entity associated with the last iterated
         * drawable.
         */
        private int entityIndex;

        /**
         * Index within the list of drawables associated with the spatial entity
         * giving the location of the last iterated drawable.
         */
        private int drawableIndex;

        /**
         * Cached last found next-to-be-iterated drawable. This will not be
         * <code>null</code> if {@link #hasNext()} has been called more recently
         * than {@link #next()}, and if there are more drawables over which to
         * iterate.
         */
        private AbstractDrawableComponent next;

        /**
         * Type of the spatial entity associated with the next iterated
         * drawable. This will only hold a valid value if {@link #next} is not
         * <code>null</code>.
         */
        private SpatialEntityType nextType;

        /**
         * Index of the spatial entity associated with the last iterated
         * drawable. This will only hold a valid value if {@link #next} is not
         * <code>null</code>.
         */
        private int nextEntityIndex;

        /**
         * Index within the list of drawables associated with the spatial entity
         * giving the location of the last iterated drawable. This will only
         * hold a valid value if {@link #next} is not <code>null</code>.
         */
        private int nextDrawableIndex;

        // Public Methods

        @Override
        public boolean hasNext() {

            /*
             * If hasNext() has not been called since the last call to next() or
             * since creation time, find the next drawable.
             */
            if (next == null) {
                findNext();
            }

            /*
             * If a next drawable exists, return true.
             */
            return (next != null);
        }

        @Override
        public AbstractDrawableComponent next() {

            /*
             * If hasNext() has not been called more recently than this method,
             * find the next drawable. If none is found, iteration is complete,
             * so throw an error.
             */
            if (next == null) {
                findNext();
            }
            if (next == null) {
                throw new NoSuchElementException();
            }

            /*
             * Remember the location information associated with the drawable
             * being returned this time, and return the drawable.
             */
            AbstractDrawableComponent theNext = next;
            next = null;
            type = nextType;
            entityIndex = nextEntityIndex;
            drawableIndex = nextDrawableIndex;
            return theNext;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        // Private Methods

        /**
         * Find the next drawable and its associated location information, if
         * there is at least one other drawable over which to iterate. If there
         * is at least one such drawable, then invocation of this method will
         * result in {@link #next} being set to that drawable, as well as
         */
        private void findNext() {

            /*
             * Start by looking for the next drawable for the same spatial
             * entity as last time.
             */
            nextType = type;
            nextEntityIndex = entityIndex;
            nextDrawableIndex = drawableIndex + 1;

            /*
             * Iterate until either it is determined that there are no more
             * drawables over which to iterate, or until one is found.
             */
            while (nextType != null) {
                List<SpatialEntity<? extends IEntityIdentifier>> spatialEntities = spatialDisplay
                        .getSpatialEntities(nextType);
                if (nextEntityIndex == spatialEntities.size()) {
                    nextType = getTypeAfter(nextType);
                    nextEntityIndex = 0;
                    nextDrawableIndex = 0;
                } else {
                    List<AbstractDrawableComponent> drawables = drawablesForSpatialEntities
                            .get(spatialEntities.get(nextEntityIndex));
                    if ((drawables == null)
                            || (nextDrawableIndex == drawables.size())) {
                        nextEntityIndex++;
                        nextDrawableIndex = 0;
                    } else {
                        next = drawables.get(nextDrawableIndex);
                        return;
                    }
                }
            }
        }

        /**
         * Get the next spatial entity type after that specified.
         * 
         * @param type
         *            Type to use as the base when finding the next type; must
         *            not be <code>null</code>.
         * @return Next spatial entity type after that specified, or
         *         <code>null</code> if the specified type is the last one.
         */
        private SpatialEntityType getTypeAfter(SpatialEntityType type) {
            SpatialEntityType[] values = SpatialEntityType.values();
            int ordinal = type.ordinal();
            return (ordinal == values.length - 1 ? null : values[ordinal + 1]);
        }
    }

    // Private Static Constants

    /**
     * Logging mechanism.
     */
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(DrawableManager.class);

    /**
     * Pattern to be used for hatching fills.
     */
    private static final String GL_PATTERN_VERTICAL_DOTTED = "VERTICAL_DOTTED";

    // Private Variables

    /**
     * Spatial display that this manager is assisting.
     */
    private final SpatialDisplay spatialDisplay;

    /**
     * Reactive drawables, that is, those drawables that when moused over may
     * indicate that they are movable, editable, etc.
     */
    private final Set<AbstractDrawableComponent> reactiveDrawables = Sets
            .newIdentityHashSet();

    /**
     * Unmodifiable view of the reactive drawables contained by
     * {@link #reactiveDrawables}.
     */
    private final Set<AbstractDrawableComponent> readOnlyReactiveDrawables = Collections
            .unmodifiableSet(reactiveDrawables);

    /**
     * Map pairing drawables with their associated spatial entities. The
     * mappings involve an N to 1 relationship where N is greater than 0.
     */
    private final Map<AbstractDrawableComponent, SpatialEntity<? extends IEntityIdentifier>> spatialEntitiesForDrawables = new IdentityHashMap<>();

    /**
     * Map pairing spatial entities with their associated drawables. This
     * provides the functional inverse of {@link #spatialEntitiesForDrawables}.
     */
    private final Map<SpatialEntity<? extends IEntityIdentifier>, List<AbstractDrawableComponent>> drawablesForSpatialEntities = new IdentityHashMap<>();

    /**
     * Map pairing spatial entities with their associated types.
     */
    private final Map<SpatialEntity<? extends IEntityIdentifier>, SpatialEntityType> typesForSpatialEntities = new IdentityHashMap<>();

    /**
     * Map pairing locations with the {@link TextDrawable#isCombinable()
     * combinable} text drawables that that occupy those locations. Each of the
     * latter will be part of a value of one entry here, regardless of whether
     * it is alone within the value list or whether it has been combined. Note
     * that any multi-element list used as a value here will also be referenced
     * by {@link #textDrawablesForAmalgamatedDrawables}.
     */
    private final Map<Coordinate, List<TextDrawable>> textDrawablesForLocations = new HashMap<>();

    /**
     * Map pairing text drawables, each of which has been found to occupy the
     * same location as at least one other text drawable, to their associated
     * amalgamated text drawable (created to replace all the ones occupying the
     * same location). The mappings involve an N to 1 relationship where N is
     * greater than 0.
     */
    private final Map<TextDrawable, TextDrawable> amalgamatedDrawablesForTextDrawables = new IdentityHashMap<>();

    /**
     * Map pairing amalgamated text drawables with the text drawables the former
     * were built to replace. This provides the functional inverse of
     * {@link #amalgamatedDrawablesForTextDrawables}. Note that the lists used
     * as values here are references to the value lists of
     * {@link #textDrawablesForLocations}.
     */
    private final Map<TextDrawable, List<TextDrawable>> textDrawablesForAmalgamatedDrawables = new IdentityHashMap<>();

    /**
     * Set of amalgamated text drawables, found as the keys in
     * {@link #textDrawablesForAmalgamatedDrawables} and the values in
     * {@link #amalgamatedDrawablesForTextDrawables}, that need to be updated,
     * possibly either rebuilt because one or more of their associated source
     * text drawables have been removed, or else removed entirely for those that
     * now have less than 2 source text drawables.
     */
    private final Set<TextDrawable> amalgamatedTextDrawablesNeedingUpdate = new HashSet<>();

    /**
     * Set of locations, found as the locations of the keys and values in
     * {@link #textDrawablesForAmalgamatedDrawables} and
     * {@link #amalgamatedDrawablesForTextDrawables}, that need to be updated,
     * either needing an associated amalgamated text drawable to be created or
     * recreated.
     */
    private final Set<Coordinate> locationsNeedingUpdate = new HashSet<>();

    /**
     * Hit-testable geometries for drawables. These are cached here for any
     * drawable that requires a hit-test geometry that differs from its standard
     * geometry.
     */
    private final Map<IDrawable<?>, Geometry> hitTestableGeometriesForDrawables = new IdentityHashMap<>();

    /**
     * Drawables representing hatched areas. A set is used instead of a list as
     * the performance is better when removing an item.
     */
    private final Set<AbstractDrawableComponent> hatchedAreas = new HashSet<>();

    /**
     * Drawable that is being created or modified; this one allows the changes
     * to occur, while the original, preserved in {@link #drawableBeingEdited},
     * is left as it was pre-edit until the edit is complete.
     */
    private AbstractDrawableComponent drawableEditedGhost;

    /**
     * Original version of drawable currently being edited in some way.
     */
    private AbstractDrawableComponent drawableBeingEdited;

    /**
     * Original colors of the {@link #drawableBeingEdited}, for restoration
     * after editing is finished.
     */
    private Color[] originalColorsOfDrawableBeingEdited;

    /**
     * Drawable that is currently highlit, if any.
     */
    private AbstractDrawableComponent highlitDrawable;

    /**
     * Version of {@link #highlitDrawable} that has been been given visual cues
     * to make it look the part.
     */
    private AbstractDrawableComponent highlitDrawableForDrawing;

    /**
     * Flag indicating whether or not the current {@link #highlitDrawable} is
     * active.
     */
    private boolean highlitDrawableActive;

    /**
     * Map of handlebar type renderers to lists of two-element arrays, each
     * array holding an X,Y coordinate pair in pixel space where a handlebar of
     * that type is rendered.
     */
    private final EnumMap<HandlebarTypeRenderer, List<double[]>> locationsForHandlebarTypeRenderers = new EnumMap<>(
            HandlebarTypeRenderer.class);

    /**
     * Map of drawables to their renderings.
     */
    private final Map<AbstractDrawableComponent, AbstractElementContainer> renderingsForDrawables = new HashMap<>();;

    /**
     * Flag indicating whether or not the hatched areas should be redrawn.
     */
    private boolean renderHatchedAreas = false;

    /**
     * Shaded shape used in the actual rendering of the hatched areas.
     */
    private IShadedShape hatchedAreaShadedShape;

    /**
     * Display properties.
     */
    private final DisplayProperties displayProperties;

    /**
     * Builder for the various possible hazard geometries.
     */
    private final DrawableBuilder drawableBuilder = new DrawableBuilder();

    // Package-Private Constructors

    /**
     * Construct a standard instance.
     * 
     * @param spatialDisplay
     *            Spatial display to be assisted.
     */
    DrawableManager(SpatialDisplay spatialDisplay) {
        this.spatialDisplay = spatialDisplay;
        displayProperties = new DisplayProperties();
        displayProperties.setLayerMonoColor(false);
        displayProperties.setLayerFilled(true);
        for (HandlebarTypeRenderer renderer : HandlebarTypeRenderer.values()) {
            locationsForHandlebarTypeRenderers.put(renderer,
                    new ArrayList<double[]>());
        }
    }

    // Package-Private Methods

    /**
     * Dispose of the manager.
     */
    void dispose() {
        if (hatchedAreaShadedShape != null) {
            hatchedAreaShadedShape.dispose();
        }
    }

    /**
     * Add drawables associated with the specified spatial entities. The
     * {@link #locationsNeedingUpdate} set is updated to contain any locations
     * of text drawables that need amalgamated text drawables to be created or
     * recreated as a result of this invocation.
     * <p>
     * Note that any such locations will need the lists associated with them in
     * {@link #textDrawablesForLocations} sorted so that the drawables in said
     * lists are ordered as appropriate given the ordering of the spatial
     * entities that they represent. Additionally, the
     * {@link #amalgamatedDrawablesForTextDrawables} and
     * {@link #textDrawablesForAmalgamatedDrawables} maps will need to be
     * updated as well, since such updates cannot be done until the amalgamated
     * drawables are created.
     * </p>
     * 
     * @param type
     *            Type of the spatial entities.
     * @param spatialEntities
     *            Spatial entities for which to add associated drawables.
     * @return <code>true</code> if the addition caused the currently-edited
     *         drawable to be removed (because it was replaced),
     *         <code>false</code> otherwise.
     */
    boolean addDrawablesForSpatialEntities(
            SpatialEntityType type,
            List<? extends SpatialEntity<? extends IEntityIdentifier>> spatialEntities) {
        boolean result = false;
        for (SpatialEntity<? extends IEntityIdentifier> spatialEntity : spatialEntities) {
            result |= addDrawablesForSpatialEntity(type, spatialEntity);
        }
        return result;
    }

    /**
     * Replace any drawables associated with the specified old spatial entities
     * with drawables associated with the specified new spatial entities. The
     * {@link #amalgamatedTextDrawablesNeedingUpdate} set is updated to contain
     * any amalgamated text drawables that need to be recreated or removed as a
     * result of this invocation, and the {@link #locationsNeedingUpdate} set is
     * updated to contain any locations of text drawables that need amalgamated
     * text drawables to be created or recreated.
     * <p>
     * Note that any such locations will need the lists associated with them in
     * {@link #textDrawablesForLocations} sorted so that the drawables in said
     * lists are ordered as appropriate given the ordering of the spatial
     * entities that they represent. Additionally, the
     * {@link #amalgamatedDrawablesForTextDrawables} and
     * {@link #textDrawablesForAmalgamatedDrawables} maps will need to be
     * updated, since such updates cannot be done until the amalgamated
     * drawables in <code>amalgamatedTextDrawablesNeedingUpdate</code> are
     * rebuilt/removed, and amalgamated drawables in
     * <code>locationsNeedingUpdate</code> are built or rebuilt.
     * </p>
     * 
     * @param type
     *            Type of the spatial entities.
     * @param oldSpatialEntities
     *            Spatial entities for which to remove associated drawables.
     * @param newSpatialEntities
     *            Spatial entities for which to add associated drawables.
     * @return <code>true</code> if the replacement caused the currently-edited
     *         drawable to be removed, <code>false</code> otherwise.
     */
    boolean replaceDrawablesForSpatialEntities(
            SpatialEntityType type,
            List<? extends SpatialEntity<? extends IEntityIdentifier>> oldSpatialEntities,
            List<? extends SpatialEntity<? extends IEntityIdentifier>> newSpatialEntities) {
        boolean result = removeDrawablesForSpatialEntities(type,
                oldSpatialEntities);
        result |= addDrawablesForSpatialEntities(type, newSpatialEntities);
        return result;
    }

    /**
     * Remove the drawables assocaiated with the specified spatial entities. The
     * {@link #amalgamatedTextDrawablesNeedingUpdate} set is updated to contain
     * any amalgamated text drawables that need to be recreated or removed as a
     * result of this invocation.
     * <p>
     * Unlike {@link #addDrawablesForSpatialEntities(SpatialEntityType, List)},
     * this method does not leave the lists of drawables used as values by
     * {@link #textDrawablesForLocations} in need of sorting. However, the
     * {@link #amalgamatedDrawablesForTextDrawables} and
     * {@link #textDrawablesForAmalgamatedDrawables} maps will still need to be
     * updated, since such updates cannot be done until the amalgamated
     * drawables in <code>amalgamatedTextDrawablesNeedingUpdate</code> are
     * rebuilt/removed.
     * </p>
     * 
     * @param type
     *            Type of the spatial entities.
     * @param spatialEntities
     *            Spatial entities for which to remove associated drawables.
     * @return <code>true</code> if the removal caused the currently-edited
     *         drawable to be removed, <code>false</code> otherwise.
     */
    boolean removeDrawablesForSpatialEntities(
            SpatialEntityType type,
            List<? extends SpatialEntity<? extends IEntityIdentifier>> spatialEntities) {
        boolean result = false;
        for (SpatialEntity<? extends IEntityIdentifier> spatialEntity : spatialEntities) {
            result |= removeDrawablesForSpatialEntity(type, spatialEntity);
        }
        return result;
    }

    /**
     * Add drawables associated with the specified spatial entity. The
     * {@link #locationsNeedingUpdate} set is updated to contain any locations
     * of text drawables that need amalgamated text drawables to be created or
     * recreated as a result of this invocation.
     * <p>
     * Note that any such locations will need the lists associated with them in
     * {@link #textDrawablesForLocations} sorted so that the drawables in said
     * lists are ordered as appropriate given the ordering of the spatial
     * entities that they represent. Additionally, the
     * {@link #amalgamatedDrawablesForTextDrawables} and
     * {@link #textDrawablesForAmalgamatedDrawables} maps will need to be
     * updated as well, since such updates cannot be done until the amalgamated
     * drawables are created.
     * </p>
     * 
     * @param type
     *            Type of the spatial entity.
     * @param spatialEntity
     *            Spatial entity for which to add associated drawables.
     * @return <code>true</code> if the addition caused the currently-edited
     *         drawable to be removed (because it replaced another entity),
     *         <code>false</code> otherwise.
     */
    boolean addDrawablesForSpatialEntity(SpatialEntityType type,
            SpatialEntity<? extends IEntityIdentifier> spatialEntity) {

        /*
         * If the spatial entity for which drawables are to be added is already
         * here, remove the old version.
         */
        boolean result = false;
        if (typesForSpatialEntities.containsKey(spatialEntity)) {
            result |= removeDrawablesForSpatialEntity(
                    typesForSpatialEntities.get(spatialEntity), spatialEntity);
        }

        /*
         * Create the hatched area drawables, and make a note of each one.
         */
        List<AbstractDrawableComponent> drawables = new ArrayList<>();
        List<AbstractDrawableComponent> hatchedAreaDrawables = drawableBuilder
                .buildHatchedAreas(spatialEntity);
        for (AbstractDrawableComponent hatchedArea : hatchedAreaDrawables) {
            hatchedAreas.add(hatchedArea);
        }
        if (hatchedAreaDrawables.isEmpty() == false) {
            renderHatchedAreas = true;
        }
        drawables.addAll(hatchedAreaDrawables);

        /*
         * Create any other drawables for the entity, flattening any that are
         * created as drawable collections into their component non-collection
         * drawables.
         */
        List<AbstractDrawableComponent> otherDrawables = new ArrayList<>();
        for (AbstractDrawableComponent drawable : drawableBuilder
                .buildDrawables(spatialEntity)) {
            addNonCollectionDrawablesToList(drawable, otherDrawables);
        }

        /*
         * Mark any non-text, non-hatching drawables as reactive, that is,
         * willing to provide visual cues as to any mutable properties they have
         * as the user passes the cursor over them.
         */
        for (AbstractDrawableComponent drawable : otherDrawables) {
            if (drawable.getClass().equals(TextDrawable.class) == false) {
                reactiveDrawables.add(drawable);
            }
        }
        drawables.addAll(otherDrawables);

        /*
         * Associate the new drawables with the spatial entity.
         */
        drawablesForSpatialEntities.put(spatialEntity, drawables);
        typesForSpatialEntities.put(spatialEntity, type);

        /*
         * For each drawable, create an association between it and the spatial
         * entity, and further process it if it needs to be combined with other
         * drawables.
         */
        for (AbstractDrawableComponent drawable : drawables) {
            spatialEntitiesForDrawables.put(drawable, spatialEntity);
            if (drawable.getClass().equals(TextDrawable.class)
                    && ((TextDrawable) drawable).isCombinable()) {
                TextDrawable textDrawable = (TextDrawable) drawable;
                Coordinate location = textDrawable.getLocation();
                List<TextDrawable> combinableDrawables = textDrawablesForLocations
                        .get(location);
                if (combinableDrawables == null) {
                    combinableDrawables = new ArrayList<>();
                    textDrawablesForLocations
                            .put(location, combinableDrawables);
                } else if (combinableDrawables.isEmpty() == false) {
                    locationsNeedingUpdate.add(location);
                }
                combinableDrawables.add(textDrawable);
            }
        }

        return result;
    }

    /**
     * Replace any drawables associated with the specified old spatial entity
     * with drawables associated with the specified new spatial entity. The
     * {@link #amalgamatedTextDrawablesNeedingUpdate} set is updated to contain
     * any amalgamated text drawables that need to be recreated or removed as a
     * result of this invocation, and the {@link #locationsNeedingUpdate} set is
     * updated to contain any locations of text drawables that need amalgamated
     * text drawables to be created or recreated.
     * <p>
     * Note that any such locations will need the lists associated with them in
     * {@link #textDrawablesForLocations} sorted so that the drawables in said
     * lists are ordered as appropriate given the ordering of the spatial
     * entities that they represent. Additionally, the
     * {@link #amalgamatedDrawablesForTextDrawables} and
     * {@link #textDrawablesForAmalgamatedDrawables} maps will need to be
     * updated, since such updates cannot be done until the amalgamated
     * drawables in <code>amalgamatedTextDrawablesNeedingUpdate</code> are
     * rebuilt/removed, and amalgamated drawables in
     * <code>locationsNeedingUpdate</code> are built or rebuilt.
     * </p>
     * 
     * @param type
     *            Type of the spatial entity.
     * @param oldSpatialEntity
     *            Spatial entity for which to remove associated drawables.
     * @param newSpatialEntity
     *            Spatial entity for which to add associated drawables.
     * @return <code>true</code> if the replacement caused the currently-edited
     *         drawable to be removed, <code>false</code> otherwise.
     */
    boolean replaceDrawablesForSpatialEntity(SpatialEntityType type,
            SpatialEntity<? extends IEntityIdentifier> oldSpatialEntity,
            SpatialEntity<? extends IEntityIdentifier> newSpatialEntity) {
        boolean result = removeDrawablesForSpatialEntity(type, oldSpatialEntity);
        result |= addDrawablesForSpatialEntity(type, newSpatialEntity);
        return result;
    }

    /**
     * Remove all records of any drawables associated with the specified spatial
     * entity. The {@link #amalgamatedTextDrawablesNeedingUpdate} set is updated
     * to contain any amalgamated text drawables that need to be recreated or
     * removed as a result of this invocation.
     * <p>
     * Unlike
     * {@link #addDrawablesForSpatialEntity(SpatialEntityType, SpatialEntity)},
     * this method does not leave the lists of drawables used as values by
     * {@link #textDrawablesForLocations} in need of sorting. However, the
     * {@link #amalgamatedDrawablesForTextDrawables} and
     * {@link #textDrawablesForAmalgamatedDrawables} maps will still need to be
     * updated, since such updates cannot be done until the amalgamated
     * drawables in <code>amalgamatedTextDrawablesNeedingUpdate</code> are
     * rebuilt/removed.
     * </p>
     * 
     * @param type
     *            Type of the spatial entity.
     * @param spatialEntity
     *            Spatial entity for which to remove associated drawables.
     * @return <code>true</code> if the removal caused the currently-edited
     *         drawable to be removed, <code>false</code> otherwise.
     */
    boolean removeDrawablesForSpatialEntity(SpatialEntityType type,
            SpatialEntity<? extends IEntityIdentifier> spatialEntity) {

        /*
         * If the spatial entity for which drawables are to be removed is not of
         * the specified type, do nothing; this means that the spatial entity is
         * associated with another type, so removing its drawables would mean
         * interfering with the drawables of said other type.
         */
        if (type != typesForSpatialEntities.get(spatialEntity)) {
            return false;
        }

        /*
         * If there are drawables for this spatial entity, remove them.
         */
        List<AbstractDrawableComponent> drawables = drawablesForSpatialEntities
                .remove(spatialEntity);
        typesForSpatialEntities.remove(spatialEntity);
        boolean result = false;
        if (drawables != null) {

            /*
             * For each drawable, remove the association it has with this
             * spatial entity, remove it from the reactive set if found within
             * it, remove any associated renderings, and remove any associated
             * hit-testable geometries. Then further process it if it is being
             * used as the highlit drawable and/or edited drawable, and/or if it
             * has been combined with other drawables.
             */
            for (AbstractDrawableComponent drawable : drawables) {

                /*
                 * Remove the link with the entity being removed, and remove the
                 * drawable from the reactive set, as well as removing any
                 * rendering associated with the drawbable. Make a note of
                 * whether hatching needs to be rebuilt if this drawable was for
                 * hatching.
                 */
                spatialEntitiesForDrawables.remove(drawable);
                reactiveDrawables.remove(drawable);
                hitTestableGeometriesForDrawables.remove(drawable);
                removeRenderingForDrawable(drawable);
                renderHatchedAreas |= hatchedAreas.remove(drawable);

                /*
                 * If the drawable is capable of being combined, remove the
                 * reference to it in the list of combinable drawables
                 * associated with its location, and remove the list itself if
                 * this leaves the latter empty.
                 */
                if (drawable.getClass().equals(TextDrawable.class)
                        && ((TextDrawable) drawable).isCombinable()) {
                    Coordinate location = ((TextDrawable) drawable)
                            .getLocation();
                    List<TextDrawable> sourceTextDrawables = textDrawablesForLocations
                            .get(location);
                    if (sourceTextDrawables != null) {
                        sourceTextDrawables.remove(drawable);
                        if (sourceTextDrawables.isEmpty()) {
                            textDrawablesForLocations.remove(location);
                        }
                    }
                }

                /*
                 * If this is the highlit drawable, remove its highlit status.
                 * Then do the same for the drawable being edited, but if it was
                 * being edited, set the result that will be returned to
                 * indicate that the drawable being edited was removed.
                 */
                if (getHighlitDrawable() == drawable) {
                    clearHighlitDrawable();
                    setHandlebarPoints(null);
                }
                if (getDrawableBeingEdited() == drawable) {
                    setDrawableBeingEdited(null);
                    result = true;
                }

                /*
                 * If the drawable has been combined with other drawables, note
                 * that it needs to be removed from the amalgamation.
                 */
                if (amalgamatedDrawablesForTextDrawables.containsKey(drawable)) {

                    /*
                     * Add this amalgamated drawable to the set of those that
                     * will need rebuilding or deletion now that this drawable
                     * is being removed.
                     */
                    TextDrawable amalgamatedTextDrawable = amalgamatedDrawablesForTextDrawables
                            .remove(drawable);
                    amalgamatedTextDrawablesNeedingUpdate
                            .add(amalgamatedTextDrawable);
                }
            }
        }
        return result;
    }

    /**
     * Update any bounding box drawable associated with the specified drawable
     * to bound the specified modified drawable.
     * 
     * @param associatedDrawable
     *            Drawable with which the bounding box drawable to be updated is
     *            associated.
     * @param modifiedDrawable
     *            Modified version of <code>drawable</code> that is to be used
     *            to generate any new bounding box.
     */
    void updateBoundingBoxDrawable(
            AbstractDrawableComponent associatedDrawable,
            AbstractDrawableComponent modifiedDrawable) {

        /*
         * Get the spatial entity that is represented by the drawable with which
         * the bounding box to be replaced is associated, and from it, get the
         * list of drawables (including any such bounding box) that represents
         * it visually. If none is found, then the drawables have disappeared,
         * so nothing needs to be done.
         */
        SpatialEntity<? extends IEntityIdentifier> spatialEntity = spatialEntitiesForDrawables
                .get(associatedDrawable);
        List<AbstractDrawableComponent> drawables = drawablesForSpatialEntities
                .get(spatialEntity);
        if (drawables == null) {
            return;
        }

        /*
         * Find the old bounding box, if there is one.
         */
        BoundingBoxDrawable boundingBoxDrawable = null;
        int indexForBoundingBoxDrawableInsertion = -1;
        for (int j = 0; j < drawables.size(); j++) {
            AbstractDrawableComponent drawable = drawables.get(j);
            if (drawable == associatedDrawable) {
                indexForBoundingBoxDrawableInsertion = j + 1;
            }
            if ((drawable instanceof BoundingBoxDrawable)
                    && (((BoundingBoxDrawable) drawable).getBoundedDrawable() == associatedDrawable)) {
                boundingBoxDrawable = (BoundingBoxDrawable) drawable;
                indexForBoundingBoxDrawableInsertion = j;
                break;
            }
        }

        /*
         * If a bounding box was found, remove it.
         */
        if (boundingBoxDrawable != null) {
            reactiveDrawables.remove(boundingBoxDrawable);
            spatialEntitiesForDrawables.remove(boundingBoxDrawable);
            drawables.remove(boundingBoxDrawable);
        }

        /*
         * Create a new bounding box, if appropriate.
         */
        boundingBoxDrawable = (modifiedDrawable instanceof MultiPointDrawable ? drawableBuilder
                .buildBoundingBoxDrawable(
                        (MultiPointDrawable<?>) modifiedDrawable,
                        (MultiPointDrawable<?>) associatedDrawable) : null);
        if (boundingBoxDrawable != null) {
            reactiveDrawables.add(boundingBoxDrawable);
            spatialEntitiesForDrawables.put(boundingBoxDrawable, spatialEntity);
            drawables.add(indexForBoundingBoxDrawableInsertion,
                    boundingBoxDrawable);
        }
    }

    /**
     * Set the ghost of the drawable being edited to that specified.
     * 
     * @param ghost
     *            New ghost of the drawable being edited.
     */
    void setGhostOfDrawableBeingEdited(AbstractDrawableComponent ghost) {
        drawableEditedGhost = ghost;
    }

    /**
     * Get the drawable currently being edited.
     * 
     * @return Drawable currently being edited.
     */
    DrawableElement getDrawableBeingEdited() {
        return (drawableBeingEdited == null ? null : drawableBeingEdited
                .getPrimaryDE());
    }

    /**
     * Sets the drawable that is being edited to that specified.
     * 
     * @param drawable
     *            Drawable that is now being edited.
     */
    void setDrawableBeingEdited(AbstractDrawableComponent drawable) {

        if (drawableBeingEdited == drawable) {
            return;
        }

        /*
         * If the previous drawable being edited had its colors altered for the
         * edit, set them back to what they were before the edit.
         */
        if (originalColorsOfDrawableBeingEdited != null) {
            if (drawableBeingEdited != null) {
                drawableBeingEdited
                        .setColors(originalColorsOfDrawableBeingEdited);
                removeRenderingForDrawable(drawableBeingEdited);
            }
            originalColorsOfDrawableBeingEdited = null;
        }

        /*
         * Remember the new drawable being edited.
         */
        drawableBeingEdited = drawable;

        /*
         * If there is now a drawable being edited, record its colors, and then
         * compile a new array of colors for it to use during the edit, which
         * effectively dim it by increasing its transparency. This way, the
         * ghost version of the the drawable becomes more prominent while the
         * edit is ongoing.
         */
        if ((drawableBeingEdited != null)
                && (drawableBeingEdited instanceof DrawableElement)) {
            originalColorsOfDrawableBeingEdited = ((DrawableElement) drawableBeingEdited)
                    .getColors();
            Color[] colorsDuringEdit = new Color[originalColorsOfDrawableBeingEdited.length];
            for (int j = 0; j < colorsDuringEdit.length; j++) {
                colorsDuringEdit[j] = new Color(
                        originalColorsOfDrawableBeingEdited[j].getRed(),
                        originalColorsOfDrawableBeingEdited[j].getGreen(),
                        originalColorsOfDrawableBeingEdited[j].getBlue(),
                        (int) ((originalColorsOfDrawableBeingEdited[j]
                                .getAlpha() * 0.5) + 0.5));
            }
            drawableBeingEdited.setColors(colorsDuringEdit);
            removeRenderingForDrawable(drawableBeingEdited);
        }
    }

    /**
     * Get the highlit drawable, that is, the one over which the cursor is
     * currently hovering, or the original drawable that is currently being
     * edited.
     * 
     * @return Highlit drawable, or <code>null</code> if there is none.
     */
    AbstractDrawableComponent getHighlitDrawable() {
        return highlitDrawable;
    }

    /**
     * Set the highlit drawable.
     * 
     * @param drawable
     *            New highlit drawable.
     * @param active
     *            Flag indicating whether or not the drawable that is to be
     *            highlit is currently active.
     */
    void setHighlitDrawable(AbstractDrawableComponent drawable, boolean active) {
        highlitDrawable = drawable;
        highlitDrawableActive = active;

        /*
         * Create the copy of the highlit drawable that looks the part, to be
         * used for actually painting the drawable.
         */
        highlitDrawableForDrawing = ((IDrawable<?>) highlitDrawable)
                .highlitCopyOf(active);
    }

    /**
     * Clear any recorded highlit drawable.
     */
    public void clearHighlitDrawable() {
        highlitDrawable = null;
    }

    /**
     * Set the handlebar points to those specified.
     * 
     * @param points
     *            Points to be used.
     */
    void setHandlebarPoints(List<ManipulationPoint> points) {

        /*
         * Clear the lists of pixel-space locations for all handlebar types.
         */
        for (List<double[]> list : locationsForHandlebarTypeRenderers.values()) {
            list.clear();
        }

        /*
         * If manipulation points were specified, iterate through them,
         * converting each in turn to pixel space and putting the result in the
         * list associated with the handlebar type renderer appropriate to that
         * type of manipulation point.
         */
        if (points != null) {
            for (ManipulationPoint point : points) {
                double[] pixelPoint = spatialDisplay.getDescriptor()
                        .worldToPixel(
                                new double[] { point.getLocation().x,
                                        point.getLocation().y });
                locationsForHandlebarTypeRenderers
                        .get(point instanceof VertexManipulationPoint ? HandlebarTypeRenderer.MOVE_VERTEX
                                : (point instanceof RotationManipulationPoint ? HandlebarTypeRenderer.ROTATE
                                        : HandlebarTypeRenderer.SCALE)).add(
                                pixelPoint);
            }
        }
    }

    /**
     * Given the specified geometry, find all drawables which intersect it.
     * 
     * @param geometry
     *            Geometry to test for intersection in geographic space.
     * @return List of drawables which intersect this geometry.
     */
    List<AbstractDrawableComponent> getIntersectingDrawables(Geometry geometry) {
        Iterator<AbstractDrawableComponent> iterator = getDrawablesIterator();
        List<AbstractDrawableComponent> intersectingDrawables = new ArrayList<>();
        while (iterator.hasNext()) {
            AbstractDrawableComponent drawable = iterator.next();
            Geometry drawableGeometry = getHitTestGeometry((IDrawable<?>) drawable);
            if ((drawableGeometry != null)
                    && geometry.intersects(drawableGeometry)) {
                intersectingDrawables.add(drawable);
            }
        }
        return intersectingDrawables;
    }

    /**
     * Get the mutable drawable, and if editable and with a vertex nearby, the
     * index of said vertex under the specified point.
     * 
     * @param x
     *            Pixel X coordinate.
     * @param y
     *            Pixel Y coordinate.
     * @param activeOnly
     *            Flag indicating whether or not only active mutable drawables
     *            should be considered.
     * @return Information including the drawable and vertex index if an
     *         editable drawable is under the point and said drawable has a
     *         vertex that lies under the point as well; drawable and
     *         <code>-1</code> for the index if a mutable (editable and/or
     *         movable) drawable lies under the point, with a <code>true</code>
     *         value for close-to-edge if the point is near the edge of the
     *         drawable and the drawable is editable; and an empty object if no
     *         mutable drawable lies under the point.
     */
    MutableDrawableInfo getMutableDrawableInfoUnderPoint(int x, int y,
            boolean activeOnly) {
        boolean active = false;
        int edgeIndex = -1;
        ManipulationPoint manipulationPoint = null;
        AbstractDrawableComponent drawable = null;

        /*
         * Get the world coordinate location of the mouse cursor.
         */
        AbstractEditor editor = ((AbstractEditor) VizWorkbenchManager
                .getInstance().getActiveEditor());
        Coordinate location = editor.translateClick(x, y);
        if (location != null) {

            /*
             * Retrieve the currently reactive shapes, and from it compile a set
             * of the identifiers that are currently active and which are
             * modifiable, and (if reactive ones are allowed as well) another
             * set of ones that are currently reactive and modifiable.
             */
            Set<IEntityIdentifier> activeIdentifiers = new HashSet<>(
                    spatialDisplay.getActiveSpatialEntityIdentifiers().size(),
                    1.0f);
            Set<IEntityIdentifier> reactiveIdentifiers = new HashSet<>(
                    (activeOnly ? 0 : reactiveDrawables.size()), 1.0f);
            for (AbstractDrawableComponent reactiveDrawable : reactiveDrawables) {
                if (isDrawableModifiable(reactiveDrawable)
                        || isDrawableBoundingBox(reactiveDrawable)) {
                    IEntityIdentifier identifier = ((IDrawable<?>) reactiveDrawable)
                            .getIdentifier();
                    if (spatialDisplay.getActiveSpatialEntityIdentifiers()
                            .contains(identifier)) {
                        activeIdentifiers.add(identifier);
                    } else if (activeOnly == false) {
                        reactiveIdentifiers.add(identifier);
                    }
                }
            }

            /*
             * First try to find a drawable that completely contains the click
             * point, giving precedence to active ones. There could be several
             * of these.
             */
            List<AbstractDrawableComponent> containingDrawables = getContainingDrawables(
                    location, x, y);
            for (AbstractDrawableComponent containingDrawable : containingDrawables) {
                if (isDrawableModifiable(containingDrawable)
                        || isDrawableBoundingBox(containingDrawable)) {
                    if (activeIdentifiers
                            .contains(((IDrawable<?>) containingDrawable)
                                    .getIdentifier())) {
                        drawable = (containingDrawable instanceof BoundingBoxDrawable ? ((BoundingBoxDrawable) containingDrawable)
                                .getBoundedDrawable() : containingDrawable);
                        active = true;
                        break;
                    } else if ((drawable == null)
                            && reactiveIdentifiers
                                    .contains(((IDrawable<?>) containingDrawable)
                                            .getIdentifier())) {
                        drawable = containingDrawable;
                    }
                }
            }

            /*
             * If no drawable has been found, try to find the closest drawable.
             */
            if (drawable == null) {
                AbstractDrawableComponent nearestDrawable = getNearestDrawable(location);
                if ((nearestDrawable != null)
                        && (activeIdentifiers
                                .contains(((IDrawable<?>) nearestDrawable)
                                        .getIdentifier()) || reactiveIdentifiers
                                .contains(((IDrawable<?>) nearestDrawable)
                                        .getIdentifier()))
                        && (isDrawableModifiable(nearestDrawable) || isDrawableBoundingBox(nearestDrawable))) {
                    drawable = (nearestDrawable instanceof BoundingBoxDrawable ? ((BoundingBoxDrawable) nearestDrawable)
                            .getBoundedDrawable() : nearestDrawable);
                    active = activeIdentifiers
                            .contains(((IDrawable<?>) drawable).getIdentifier());
                }
            }

            /*
             * If an drawable has been found, determine whether the point is
             * close to its edge, and determine whether the point is close to
             * one if its manipulation points.
             */
            if (drawable != null) {
                Coordinate mouseScreenLocation = new Coordinate(x, y);
                if (isDrawableEditable(drawable)) {

                    /*
                     * Get the rings of coordinates making up the geometry, and
                     * for each ring, see if the point is close to it.
                     */
                    List<Coordinate[]> coordinates = AdvancedGeometryUtilities
                            .getCoordinates(AdvancedGeometryUtilities
                                    .getJtsGeometry(((IDrawable<?>) drawable)
                                            .getGeometry()));
                    Point mouseScreenPoint = AdvancedGeometryUtilities
                            .getGeometryFactory().createPoint(
                                    mouseScreenLocation);
                    for (int j = 0; j < coordinates.size(); j++) {

                        /*
                         * Create a line string with screen coordinates, and
                         * determine the distance between the line string and
                         * the given point.
                         */
                        Coordinate[] ringCoordinates = coordinates.get(j);
                        Coordinate[] screenCoordinates = new Coordinate[ringCoordinates.length];
                        for (int k = 0; k < ringCoordinates.length; k++) {
                            double[] coords = editor
                                    .translateInverseClick(ringCoordinates[k]);
                            screenCoordinates[k] = new Coordinate(coords[0],
                                    coords[1]);
                        }
                        LineString lineString = AdvancedGeometryUtilities
                                .getGeometryFactory().createLineString(
                                        screenCoordinates);
                        double distance = mouseScreenPoint.distance(lineString);

                        /*
                         * If the distance is small enough, note that the cursor
                         * is close to the edge of this ring of the drawable.
                         */
                        if (distance <= SpatialDisplay.SLOP_DISTANCE_PIXELS) {
                            edgeIndex = j;
                            break;
                        }
                    }
                }

                /*
                 * Determine whether or not the mouse position is close to one
                 * of the drawable's manipulation points. If it is close enough,
                 * record the manipulation point, picking the one that is
                 * closest.
                 */
                List<ManipulationPoint> manipulationPoints = ((IDrawable<?>) drawable)
                        .getManipulationPoints();
                double minDistance = Double.MAX_VALUE;
                for (ManipulationPoint point : manipulationPoints) {
                    double[] screen = editor.translateInverseClick(point
                            .getLocation());
                    Coordinate pixelLocation = new Coordinate(screen[0],
                            screen[1]);
                    double distance = mouseScreenLocation
                            .distance(pixelLocation);
                    if (distance <= SpatialDisplay.SLOP_DISTANCE_PIXELS) {
                        if (distance < minDistance) {
                            manipulationPoint = point;
                            minDistance = distance;
                        }
                    }
                }
            }
        }
        return new MutableDrawableInfo(drawable, active, edgeIndex,
                manipulationPoint);
    }

    /**
     * Given the specified point, find all drawables which contain it.
     * 
     * @param point
     *            Point to test in geographic space.
     * @param x
     *            X coordinate of point in pixel space.
     * @param y
     *            Y coordinate of point in pixel space.
     * @return List of drawables which contain this point.
     */
    List<AbstractDrawableComponent> getContainingDrawables(Coordinate point,
            int x, int y) {
        Iterator<AbstractDrawableComponent> iterator = getDrawablesIterator();

        /*
         * Iterate through the drawables, checking each in turn.
         */
        Geometry clickPointWithSlop = getClickPointWithSlop(
                AdvancedGeometryUtilities.getGeometryFactory().createPoint(
                        point), x, y);
        if (clickPointWithSlop == null) {
            return Collections.emptyList();
        }

        List<AbstractDrawableComponent> containingDrawables = new ArrayList<>();

        while (iterator.hasNext()) {
            AbstractDrawableComponent drawable = iterator.next();

            /*
             * Skip labels, as they are not selectable.
             */
            if ((drawable instanceof IDrawable)
                    && (drawable instanceof TextDrawable == false)) {
                Geometry drawableGeometry = getHitTestGeometry((IDrawable<?>) drawable);

                if (drawableGeometry != null) {
                    boolean contains = false;
                    if (drawableGeometry instanceof GeometryCollection) {
                        GeometryCollection geometryCollection = (GeometryCollection) drawableGeometry;
                        for (int j = 0; j < geometryCollection
                                .getNumGeometries(); j++) {
                            Geometry geometry = geometryCollection
                                    .getGeometryN(j);
                            contains = clickPointWithSlop.intersects(geometry);
                            if (contains) {
                                break;
                            }
                        }
                    } else {
                        contains = clickPointWithSlop
                                .intersects(drawableGeometry);
                    }
                    if (contains) {
                        containingDrawables.add(drawable);
                    }
                }
            }
        }

        /*
         * The hazards drawn first are on the bottom of the stack while those
         * drawn last are on the top of the stack. Reversing the list makes it
         * easier for applications to find the top-most containing element.
         */
        return Lists.reverse(containingDrawables);
    }

    /**
     * Get the reactive drawables, that is, those that may react when moused
     * over to indicate that they are editable, movable, etc.
     * 
     * @return Reactive drawables.
     */
    Set<AbstractDrawableComponent> getReactiveDrawables() {
        return readOnlyReactiveDrawables;
    }

    /**
     * Determine whether or not the specified drawable is movable.
     * 
     * @param drawable
     *            Drawable to be checked.
     * @return <code>true</code> if the drawable is movable, <code>false</code>
     *         otherwise.
     */
    boolean isDrawableMovable(AbstractDrawableComponent drawable) {
        if (drawable instanceof DECollection) {
            drawable = ((DECollection) drawable).getItemAt(0);
        }
        return ((IDrawable<?>) drawable).isMovable();
    }

    /**
     * Determine whether or not the specified drawable is editable.
     * 
     * @param drawable
     *            Drawable to be checked.
     * @return <code>true</code> if the drawable is editable, <code>false</code>
     *         otherwise.
     */
    boolean isDrawableEditable(AbstractDrawableComponent drawable) {
        if (drawable instanceof DECollection) {
            drawable = ((DECollection) drawable).getItemAt(0);
        }
        return ((IDrawable<?>) drawable).isEditable();
    }

    /**
     * Determine whether or not the specified drawable is rotatable.
     * 
     * @param drawable
     *            Drawable to be checked.
     * @return <code>true</code> if the drawable is rotatable,
     *         <code>false</code> otherwise.
     */
    boolean isDrawableRotatable(AbstractDrawableComponent drawable) {
        if (drawable instanceof DECollection) {
            drawable = ((DECollection) drawable).getItemAt(0);
        }
        return ((IDrawable<?>) drawable).isRotatable();
    }

    /**
     * Determine whether or not the specified drawable is resizable.
     * 
     * @param drawable
     *            Drawable to be checked.
     * @return <code>true</code> if the drawable is resizable,
     *         <code>false</code> otherwise.
     */
    boolean isDrawableResizable(AbstractDrawableComponent drawable) {
        if (drawable instanceof DECollection) {
            drawable = ((DECollection) drawable).getItemAt(0);
        }
        return ((IDrawable<?>) drawable).isResizable();
    }

    /**
     * Determine whether or not the specified drawable is modifiable (that is,
     * movable, editable, resizable, and/or rotatable).
     * 
     * @param drawable
     *            Drawable to be checked.
     * @return <code>true</code> if the drawable is modifiable,
     *         <code>false</code> otherwise.
     */
    boolean isDrawableModifiable(AbstractDrawableComponent drawable) {
        if (drawable instanceof DECollection) {
            drawable = ((DECollection) drawable).getItemAt(0);
        }
        IDrawable<?> castDrawable = (IDrawable<?>) drawable;
        return (castDrawable.isMovable() || castDrawable.isRotatable()
                || castDrawable.isResizable() || castDrawable.isEditable());
    }

    /**
     * Determine whether or not the specified drawable is a bounding box for
     * another drawable.
     * 
     * @param drawable
     *            Drawable to be checked.
     * @return <code>true</code> if the drawable is a bounding box for another
     *         drawable, <code>false</code> otherwise.
     */
    boolean isDrawableBoundingBox(AbstractDrawableComponent drawable) {
        if (drawable instanceof DECollection) {
            DECollection deCollection = (DECollection) drawable;
            return (deCollection.getItemAt(0) instanceof BoundingBoxDrawable);
        }
        return (drawable instanceof BoundingBoxDrawable);
    }

    /**
     * Determine whether the specified drawable is a polygon.
     * 
     * @param drawable
     *            Drawable to be checked.
     * @return <code>true</code> if the drawable is a polygon,
     *         <code>false</code> otherwise.
     */
    boolean isPolygon(AbstractDrawableComponent drawable) {
        return ((drawable instanceof PathDrawable) && ((PathDrawable) drawable)
                .isClosedLine());
    }

    /**
     * Get the spatial entity associated with the specified drawable, if any.
     * 
     * @param drawable
     *            Drawable.
     * @return Associated spatial entity, or <code>null</code> if none is
     *         associated.
     */
    SpatialEntity<? extends IEntityIdentifier> getAssociatedSpatialEntity(
            AbstractDrawableComponent drawable) {
        return spatialEntitiesForDrawables.get(drawable);
    }

    /**
     * Get the first reactive polygon drawable found.
     * 
     * @return First reactive polygon drawable, or <code>null</code> if none are
     *         found.
     */
    PathDrawable getFirstReactivePolygon() {
        for (AbstractDrawableComponent drawable : reactiveDrawables) {
            if ((drawable.getClass() == PathDrawable.class)
                    && ((PathDrawable) drawable).isClosedLine()) {
                return (PathDrawable) drawable;
            }
        }
        return null;
    }

    /**
     * Paint the drawables.
     * 
     * @param target
     *            Graphics target on which to paint.
     * @param paintProperties
     *            Paint properties to be used.
     * @param scaleChanged
     *            Flag indicating whether or not the scale has changed.
     * @throws VizException
     *             If an error occurs during painting.
     */
    void paint(IGraphicsTarget target, PaintProperties paintProperties,
            boolean scaleChanged) throws VizException {

        /*
         * If a zoom operation has just completed, adjust zoom-sensitive
         * drawables.
         */
        if (scaleChanged && (paintProperties.isZooming() == false)) {
            adjustDrawablesSensitiveToZoom();
        }

        /*
         * Paint shaded shapes for hatched areas.
         */
        drawHatchedAreas(target, paintProperties, scaleChanged);

        /*
         * If any amalgamated text drawables need to be created, recreated, or
         * removed, do this before proceeding.
         */
        pruneIntersectingLocationsAndTextDrawables();
        recreateOrRemoveAmalgamatedTextDrawablesNeedingUpdate();
        createOrRecreateAmalgamatedTextDrawablesForLocationsNeedingUpdate();

        /*
         * Iterate through the drawables, painting each in turn.
         */
        Iterator<AbstractDrawableComponent> drawablesIterator = getDrawablesIterator();
        while (drawablesIterator.hasNext()) {

            /*
             * If the drawable is being represented by an amalgamated version,
             * then if it is the last text drawable in the list of drawables
             * that were amalgamated, draw the amalgamated one instead. If it is
             * not the last in the list, skip this drawable (as it should only
             * be drawn once, where the last of the drawables it is representing
             * would be drawn). Also skip it if it is the highlit drawable and
             * is active, as it will be rendered after this loop. (If it is the
             * highlit drawable but is inactive, then it must be drawn here, as
             * the post-loop rendering will simply place a subtler version of
             * the drawable over the top of this one to indicate in a gentler
             * way that the drawable is highlit.)
             */
            AbstractDrawableComponent drawable = getDrawableToBeRendered(drawablesIterator
                    .next());
            if ((drawable == null)
                    || ((drawable == highlitDrawable) && highlitDrawableActive)) {
                continue;
            }

            /*
             * Get the rendering for this drawable and paint it.
             */
            getOrCreateRenderingForDrawable(drawable, target).draw(target,
                    paintProperties, displayProperties);
        }

        /*
         * Paint the highlit drawable, if any.
         */
        if (highlitDrawable != null) {
            paint(highlitDrawableForDrawing, target, paintProperties);
        }

        /*
         * Paint the ghost of the drawable being edited, if any.
         */
        if (drawableEditedGhost != null) {
            paint(drawableEditedGhost, target, paintProperties);
        }

        /*
         * Draw the handle bars.
         */
        drawHandlebars(target, paintProperties);
    }

    // Private Methods

    /**
     * Get an iterator for the PGEN drawables. Note that this iterator does not
     * support {@link Iterator#remove() modification} of the underlying data.
     * 
     * @return Iterator for the PGEN drawables.
     */
    private Iterator<AbstractDrawableComponent> getDrawablesIterator() {
        return new DrawableIterator();
    }

    /**
     * Retrieve the drawable closest to the specified point.
     * 
     * @param point
     *            Point against which to test.
     * @return Nearest drawable.
     */
    private AbstractDrawableComponent getNearestDrawable(Coordinate point) {

        AbstractEditor editor = (AbstractEditor) VizWorkbenchManager
                .getInstance().getActiveEditor();
        double screenCoord[] = editor.translateInverseClick(point);

        /**
         * Note that the distance unit of the JTS distance function is central
         * angle degrees. This seems to match closely with degrees lat and lon.
         */
        double minDist = Double.MAX_VALUE;

        Iterator<AbstractDrawableComponent> iterator = getDrawablesIterator();

        Point clickScreenPoint = AdvancedGeometryUtilities.getGeometryFactory()
                .createPoint(new Coordinate(screenCoord[0], screenCoord[1]));

        AbstractDrawableComponent closestSymbol = null;

        while (iterator.hasNext()) {
            AbstractDrawableComponent drawable = iterator.next();

            if (drawable instanceof TextDrawable == false) {
                Geometry geometry = getHitTestGeometry((IDrawable<?>) drawable);

                if (geometry != null) {

                    /*
                     * Convert the polygon vertices into pixels.
                     */
                    Coordinate[] coords = geometry.getCoordinates();

                    for (int i = 0; i < coords.length; ++i) {
                        screenCoord = editor.translateInverseClick(coords[i]);
                        Point geometryPoint = AdvancedGeometryUtilities
                                .getGeometryFactory().createPoint(
                                        new Coordinate(screenCoord[0],
                                                screenCoord[1]));

                        double distance = clickScreenPoint
                                .distance(geometryPoint);

                        if (distance < minDist) {
                            minDist = distance;
                            closestSymbol = drawable;
                        }
                    }
                }
            }
        }

        if (minDist <= SpatialDisplay.SLOP_DISTANCE_PIXELS) {
            return closestSymbol;
        }

        return null;
    }

    /**
     * Get the hit-testable geometry for the specified drawable.
     * 
     * @param drawable
     *            Drawable for which to get the hit-testable geometry.
     * @return Hit-testable geometry, or <code>null</code> if the geometry is
     *         outside the display bounds.
     */
    private Geometry getHitTestGeometry(IDrawable<?> drawable) {
        if (drawable instanceof SymbolDrawable) {
            Geometry geometry = hitTestableGeometriesForDrawables.get(drawable);
            if (geometry == null) {
                Coordinate coordinate = AdvancedGeometryUtilities
                        .getCentroid(drawable.getGeometry());
                double[] screenCoords = ((AbstractEditor) VizWorkbenchManager
                        .getInstance().getActiveEditor())
                        .translateInverseClick(coordinate);
                if (screenCoords == null) {
                    return null;
                }
                geometry = getClickPointWithSlop(AdvancedGeometryUtilities
                        .getGeometryFactory().createPoint(coordinate),
                        (int) (screenCoords[0] + 0.5),
                        (int) (screenCoords[1] + 0.5));
                if (geometry != null) {
                    hitTestableGeometriesForDrawables.put(drawable, geometry);
                }
            }
            return geometry;
        }
        return AdvancedGeometryUtilities.getJtsGeometry(drawable.getGeometry());
    }

    /**
     * Given the specified point in both geographic coordinates and in pixel
     * coordinates, determine a distance in geographic space that suffices as
     * "slop" area for hit tests for geometries that are difficult to hit (one-
     * and two-dimensional entities), and create a geometry encapsulating this
     * distance around the point.
     * 
     * @param point
     *            Geographical coordinates of point being tested.
     * @param x
     *            Pixel X coordinate of point being tested.
     * @param y
     *            Pixel Y coordinate of point being tested.
     * @return Click point with slop buffering, or <code>null</code> if it was
     *         not possible to calculate it.
     */
    private Geometry getClickPointWithSlop(Point point, int x, int y) {

        /*
         * Find the editor.
         */
        AbstractEditor editor = ((AbstractEditor) VizWorkbenchManager
                .getInstance().getActiveEditor());

        /*
         * Try creating a pixel point at the "slop" distance from the original
         * pixel point in each of the four cardinal directions; for each one,
         * see if this yields a translatable point, and if so, return a geometry
         * resulting from buffering the point by that distance. If not, return
         * nothing.
         */
        Coordinate loc = point.getCoordinate();
        Coordinate offsetLoc = null;
        for (int j = 0; j < 4; j++) {
            offsetLoc = editor
                    .translateClick(
                            x
                                    + (SpatialDisplay.SLOP_DISTANCE_PIXELS * ((j % 2) * (j == 1 ? 1
                                            : -1))),
                            y
                                    + (SpatialDisplay.SLOP_DISTANCE_PIXELS * (((j + 1) % 2) * (j == 0 ? 1
                                            : -1))));
            if (offsetLoc != null) {
                return point.buffer(Math.sqrt(Math
                        .pow(loc.x - offsetLoc.x, 2.0)
                        + Math.pow(loc.y - offsetLoc.y, 2.0)));
            }
        }
        return null;
    }

    /**
     * Create displayables for an element container.
     * 
     * @param displayables
     *            Leftover displayables from before, or <code>null</code>.
     * @param factory
     *            Display element factory, used to create the actual display
     *            elements.
     * @param drawable
     *            Drawable for which display elements are to be created.
     * @param mapDescriptor
     *            Map descriptor to be used.
     * @param paintProperties
     *            Paint properties to be used.
     * @return List of renderings for the specified element's container.
     */
    private List<IDisplayable> createDisplayablesForElementContainer(
            List<IDisplayable> displayables, PgenDisplayElementFactory factory,
            DrawableElement drawable, IMapDescriptor mapDescriptor,
            PaintProperties paintProperties) {

        /*
         * Clean up after any leftover displayables.
         */
        if ((displayables != null) && (displayables.isEmpty() == false)) {
            factory.reset();
        }

        /*
         * Set the range for this element.
         */
        setDrawableElementRange(drawable, factory, mapDescriptor,
                paintProperties);

        /*
         * Create displayables.
         */
        boolean handled = false;
        if (drawable instanceof IText) {
            handled = true;
            displayables = factory.createDisplayElements((IText) drawable,
                    paintProperties);
        } else if (drawable instanceof ISymbol) {
            handled = true;
            displayables = factory.createDisplayElements((ISymbol) drawable,
                    paintProperties);
        } else if (drawable instanceof IMultiPoint) {
            if (drawable instanceof IArc) {
                handled = true;
                displayables = factory.createDisplayElements((IArc) drawable,
                        paintProperties);
            } else if (drawable instanceof ILine) {
                handled = true;
                displayables = factory.createDisplayElements((ILine) drawable,
                        paintProperties, true);
            }
        }
        if (handled == false) {
            statusHandler.error("Unexpected DrawableElement of type "
                    + drawable.getClass().getSimpleName()
                    + "; do not know how to create its displayables.",
                    new IllegalStateException());
            return Collections.emptyList();
        }
        return displayables;
    }

    /**
     * Set a text element's range record. This method's implementation is copied
     * from {@link AbstractElementContainer} since the latter is inaccessible
     * here, with the only changes being that parameters are passed in that in
     * the original implementation are accessible as member variables.
     */
    private void setDrawableElementRange(DrawableElement element,
            PgenDisplayElementFactory factory, IMapDescriptor mapDescriptor,
            PaintProperties paintProperties) {
        if (element instanceof ISinglePoint) {
            PgenRangeRecord rng = null;
            if (element instanceof IText) {
                rng = factory
                        .findTextBoxRange((IText) element, paintProperties);
            } else if (element instanceof ISymbol) {
                rng = factory.findSymbolRange((ISymbol) element,
                        paintProperties);
            }
            if (rng != null) {
                element.setRange(rng);
            }
        } else if (element instanceof IMultiPoint) {
            double[][] pixels = PgenUtil.latlonToPixel(
                    ((IMultiPoint) element).getLinePoints(), mapDescriptor);
            double[][] smoothpts = pixels;
            float density;

            /*
             * Apply parametric smoothing on pixel coordinates, if required.
             * 
             * Note: 1. NMAP2 range calculation does not do smoothing though. 2.
             * Tcm and WatchBox is IMultiPoint but not ILine.
             */
            boolean smoothIt = true;
            if (smoothIt && element instanceof ILine
                    && ((ILine) element).getSmoothFactor() > 0) {
                if (((ILine) element).getSmoothFactor() > 0) {
                    float devScale = 50.0f;
                    if (((ILine) element).getSmoothFactor() == 1) {
                        density = devScale / 1.0f;
                    } else {
                        density = devScale / 5.0f;
                    }

                    smoothpts = CurveFitter.fitParametricCurve(pixels, density);
                }
            }

            Coordinate[] pts = new Coordinate[smoothpts.length];

            for (int ii = 0; ii < smoothpts.length; ii++) {
                pts[ii] = new Coordinate(smoothpts[ii][0], smoothpts[ii][1]);
            }

            boolean closed = false;
            if (element instanceof ILine) {
                closed = ((ILine) element).isClosedLine();
            }

            element.createRange(pts, closed);

        } else {
            statusHandler.error("Unexpected DrawableElement of type "
                    + element.getClass().getSimpleName()
                    + "; do not know how to set its range record.",
                    new IllegalStateException());
        }
    }

    /**
     * Prune {@link #locationsNeedingUpdate} of any coordinate that is used by
     * one of the locations used by a drawable in
     * {@link #amalgamatedTextDrawablesNeedingUpdate} if the number of text
     * drawables at that location are less than 2. If instead the number is 2 or
     * more, prune <code>amalgamatedTextDrawablesNeedingUpdate</code> of the
     * text drawable at that location. When an invocation of this method
     * completes, the effect will be to ensure that none of the locations in
     * <code>locationsNeedingUpdate</code> are used by any of the text drawables
     * in <code>amalgamatedTextDrawablesNeedingUpdate</code>.
     */
    private void pruneIntersectingLocationsAndTextDrawables() {

        /*
         * Iterate through the amalgamated text drawables that are marked as
         * needing updates, checking each one to see if its location is listed
         * as one that needs updating as well. If so, then check and see if the
         * source text drawables number 1 or less, and if so, remove its
         * location from the needing-updates set, since this amalgamated text
         * drawable must be removed, not recreated. If the source text drawables
         * number 2 or more, remove the drawable from the needing-updates set,
         * as it does not need removal, but rather recreation.
         */
        for (Iterator<TextDrawable> iterator = amalgamatedTextDrawablesNeedingUpdate
                .iterator(); iterator.hasNext();) {
            TextDrawable drawable = iterator.next();
            Coordinate location = drawable.getLocation();
            if (locationsNeedingUpdate.contains(location)) {
                List<TextDrawable> sourceTextDrawables = textDrawablesForLocations
                        .get(location);
                if ((sourceTextDrawables == null)
                        || (sourceTextDrawables.size() < 2)) {
                    locationsNeedingUpdate.remove(location);
                } else {
                    iterator.remove();
                }
            }
        }

        /*
         * Remove any locations from the needing-updates set if they have source
         * text drawables numbering 1 or less.
         */
        for (Iterator<Coordinate> iterator = locationsNeedingUpdate.iterator(); iterator
                .hasNext();) {
            List<TextDrawable> textDrawables = textDrawablesForLocations
                    .get(iterator.next());
            if ((textDrawables == null) || (textDrawables.size() < 2)) {
                iterator.remove();
            }
        }
    }

    /**
     * Create or recreate amalgamated text drawables, one for the list of source
     * text drawables at each location within {@link #locationsNeedingUpdate},
     * in response to each such location's list of source text drawables gaining
     * one or more elements. Each list of source text drawables has its elements
     * sorted in the order that the drawables are to be displayed. The
     * <code>locationsNeedingUpdate</code> is left empty.
     */
    private void createOrRecreateAmalgamatedTextDrawablesForLocationsNeedingUpdate() {

        /*
         * For each location that is to be handled, create an entry in a map
         * holding a set of the source text drawables to be sorted, and empty
         * the corresponding original list so that it is ready to have its
         * source text drawables inserted in the proper order.
         */
        Map<Coordinate, Set<TextDrawable>> newTextDrawablesForLocations = new HashMap<>(
                locationsNeedingUpdate.size(), 1.0f);
        for (Coordinate location : locationsNeedingUpdate) {
            List<TextDrawable> sourceTextDrawables = textDrawablesForLocations
                    .get(location);
            Set<TextDrawable> sourceTextDrawablesSet = Sets
                    .<TextDrawable> newIdentityHashSet();
            sourceTextDrawablesSet.addAll(sourceTextDrawables);
            newTextDrawablesForLocations.put(location, sourceTextDrawablesSet);
            sourceTextDrawables.clear();
        }

        /*
         * Iterate through all the drawables, checking each one to see if it is
         * within one of the compiled source text drawable sets above. If it is
         * found within one of them, remove it from the set and insert it into
         * the corresponding list, thus giving the lists the proper order.
         * Remove any set that is rendered empty by this iterative process, and
         * stop iterating when all sets of source text drawables that have yet
         * to be inserted are empty.
         */
        Iterator<AbstractDrawableComponent> drawablesIterator = getDrawablesIterator();
        while ((newTextDrawablesForLocations.isEmpty() == false)
                && drawablesIterator.hasNext()) {
            AbstractDrawableComponent drawable = drawablesIterator.next();
            Coordinate locationCompleted = null;
            for (Map.Entry<Coordinate, Set<TextDrawable>> entry : newTextDrawablesForLocations
                    .entrySet()) {
                if (entry.getValue().remove(drawable)) {
                    textDrawablesForLocations.get(entry.getKey()).add(
                            (TextDrawable) drawable);
                    if (entry.getValue().isEmpty()) {
                        locationCompleted = entry.getKey();
                    }
                    break;
                }
            }
            if (locationCompleted != null) {
                newTextDrawablesForLocations.remove(locationCompleted);
            }
        }

        /*
         * Create the amalgamated text drawable for each list of source text
         * drawables in turn, removing any rendering associated with the old
         * amalgamated text drawable, if there was one. If a location is found
         * to have no associated drawables, remove its mapping to its empty
         * source drawables list, as this means there is nothing at that
         * location.
         */
        for (Coordinate location : locationsNeedingUpdate) {
            List<TextDrawable> sourceTextDrawables = textDrawablesForLocations
                    .get(location);
            if (sourceTextDrawables.isEmpty()) {
                textDrawablesForLocations.remove(location);
            } else {
                removeRenderingForDrawable(amalgamatedDrawablesForTextDrawables
                        .remove(sourceTextDrawables.get(0)));
                createAmalgamatedTextDrawable(sourceTextDrawables);
            }
        }

        locationsNeedingUpdate.clear();
    }

    /**
     * Recreate or remove the amalgamated text drawables found in
     * {@link #amalgamatedTextDrawablesNeedingUpdate} in response to each such
     * drawable's list of source text drawables losing one or more elements. If
     * a given list has two or more elements, its associated amalgamated text
     * drawable must be recreated; otherwise, said drawable must be deleted, as
     * there is no more amalgamation needed. The
     * <code>amalgamatedTextDrawablesNeedingUpdate</code> is left empty.
     */
    private void recreateOrRemoveAmalgamatedTextDrawablesNeedingUpdate() {

        /*
         * Handle each supplied amalgamated text drawable in turn.
         */
        for (TextDrawable amalgamatedTextDrawable : amalgamatedTextDrawablesNeedingUpdate) {

            /*
             * Since this amalgamated text drawable is being removed (whether or
             * not it gets recreated), remove any rendering associated with it.
             */
            removeRenderingForDrawable(amalgamatedTextDrawable);

            /*
             * If the list of source text drawables that go with this
             * amalgamated text drawable has zero or one entries, remove the
             * amalgamated text drawable and its ties with any remaining source
             * drawables. Otherwise, recreate the amalgamated text drawable.
             */
            List<TextDrawable> sourceTextDrawables = textDrawablesForAmalgamatedDrawables
                    .get(amalgamatedTextDrawable);
            if (sourceTextDrawables.size() < 2) {
                textDrawablesForAmalgamatedDrawables
                        .remove(amalgamatedTextDrawable);
                for (TextDrawable sourceTextDrawable : sourceTextDrawables) {
                    amalgamatedDrawablesForTextDrawables
                            .remove(sourceTextDrawable);
                }
            } else {
                createAmalgamatedTextDrawable(sourceTextDrawables);
            }
        }
        amalgamatedTextDrawablesNeedingUpdate.clear();
    }

    /**
     * Create an amalgamated text drawable for the specified source text
     * drawables and record its association with said source text drawables.
     * 
     * @param sourceTextDrawables
     *            Source text drawables to be amalgamated.
     */
    private void createAmalgamatedTextDrawable(
            List<TextDrawable> sourceTextDrawables) {

        /*
         * Create the amalgamated text drawable, and create associations between
         * it and its source text drawables. Also remove any renderings for the
         * source text drawables, since they will not be needed now that the
         * amalgamation is being used instead.
         */
        TextDrawable amalgamatedTextDrawable = drawableBuilder
                .buildAmalgamatedText(sourceTextDrawables);
        textDrawablesForAmalgamatedDrawables.put(amalgamatedTextDrawable,
                sourceTextDrawables);
        for (TextDrawable sourceTextDrawable : sourceTextDrawables) {
            removeRenderingForDrawable(sourceTextDrawable);
            amalgamatedDrawablesForTextDrawables.put(sourceTextDrawable,
                    amalgamatedTextDrawable);
        }
    }

    /**
     * Get the drawable that should actually be rendered, if any, for the
     * specified drawable. This method performs any substitutions required to
     * support amalgamation of drawables, etc.
     * 
     * @param drawable
     *            Drawable to check.
     * @return Drawable to be rendered, or <code>null</code> if nothing should
     *         be rendered for this drawable.
     */
    private AbstractDrawableComponent getDrawableToBeRendered(
            AbstractDrawableComponent drawable) {

        /*
         * If the drawable is being represented by an amalgamated version, then
         * if it is the last text drawable in the list of drawables that were
         * amalgamated, draw the amalgamated one instead. If it is not the last
         * in the list, skip this drawable (as it should only be drawn once,
         * where the last of the drawables it is representing would be drawn).
         */
        TextDrawable amalgamatedTextDrawable = amalgamatedDrawablesForTextDrawables
                .get(drawable);
        if (amalgamatedTextDrawable != null) {
            List<TextDrawable> sourceTextDrawables = textDrawablesForAmalgamatedDrawables
                    .get(amalgamatedTextDrawable);
            if (sourceTextDrawables.get(sourceTextDrawables.size() - 1) == drawable) {
                return amalgamatedTextDrawable;
            } else {
                return null;
            }
        }

        /*
         * Since the drawable is not associated with an amalgamation, just use
         * it.
         */
        return drawable;
    }

    /**
     * Get or create the rendering for the specified drawable.
     * 
     * @param drawable
     *            Drawable for which the rendering is to be fetched.
     * @param target
     *            Graphics target for which the rendering is to be used.
     * @return Fetched or created rendering.
     */
    private AbstractElementContainer getOrCreateRenderingForDrawable(
            AbstractDrawableComponent drawable, IGraphicsTarget target) {
        AbstractElementContainer container = renderingsForDrawables
                .get(drawable);
        if (container == null) {
            if (drawable instanceof Symbol) {
                container = new DefaultRasterElementContainer(
                        (DrawableElement) drawable,
                        spatialDisplay.getDescriptor(), target);
            } else {
                container = new DefaultVectorElementContainer(
                        (DrawableElement) drawable,
                        spatialDisplay.getDescriptor(), target);
            }
            renderingsForDrawables.put(drawable, container);
        }
        return container;
    }

    /**
     * Remove and dispose of any rendering associated with the specified
     * drawable.
     * 
     * @param drawable
     *            Drawable for which to remove any rendering; if
     *            <code>null</code>, no removal will be attempted.
     */
    private void removeRenderingForDrawable(AbstractDrawableComponent drawable) {
        if (drawable == null) {
            return;
        }
        AbstractElementContainer elementContainer = renderingsForDrawables
                .remove(drawable);
        if (elementContainer != null) {
            elementContainer.dispose();
        }
    }

    /**
     * Add all non-collection drawables found within the specified drawable,
     * potentially within nested sub-collections, if the latter is a collection,
     * or the drawable itself if it is not, to the specified list.
     * 
     * @param drawable
     *            Drawable from which to get all non-collection drawables.
     * @param list
     *            List to which to add the non-collection drawables.
     */
    private void addNonCollectionDrawablesToList(
            AbstractDrawableComponent drawable,
            List<AbstractDrawableComponent> list) {
        if (drawable instanceof DECollection) {
            Iterator<AbstractDrawableComponent> iterator = ((DECollection) drawable)
                    .getComponentIterator();
            while (iterator.hasNext()) {
                addNonCollectionDrawablesToList(iterator.next(), list);
            }
        } else {
            list.add(drawable);
        }
    }

    /**
     * Adjust any drawables that are sensitive to zoom changes.
     */
    private void adjustDrawablesSensitiveToZoom() {

        /*
         * Clear any hit-testable geometries that were recorded previously.
         */
        hitTestableGeometriesForDrawables.clear();

        /*
         * Iterate through all text drawables associated directly with spatial
         * entities, notifying each one of the zoom change and, if this results
         * in a change in location, remove its associated rendering. Make a note
         * of any such changes to combinable text drawables, as if any of these
         * change, any amalgamated text drawables will have to be recomputed
         * from scratch. Also build a new locations-to-text-drawables map in
         * case such a change occurs.
         */
        boolean combinableTextLocationChanged = false;
        Map<Coordinate, List<TextDrawable>> newTextDrawablesForLocations = new HashMap<>();
        for (AbstractDrawableComponent drawable : spatialEntitiesForDrawables
                .keySet()) {
            if (drawable.getClass() == TextDrawable.class) {
                TextDrawable textDrawable = (TextDrawable) drawable;
                if (textDrawable.handleZoomChange()) {
                    removeRenderingForDrawable(drawable);
                    combinableTextLocationChanged |= textDrawable
                            .isCombinable();
                }
                if (textDrawable.isCombinable()) {
                    List<TextDrawable> textDrawables = newTextDrawablesForLocations
                            .get(textDrawable.getLocation());
                    if (textDrawables == null) {
                        textDrawables = new ArrayList<>();
                        newTextDrawablesForLocations.put(
                                textDrawable.getLocation(), textDrawables);
                    }
                    textDrawables.add(textDrawable);
                }
            }
        }

        /*
         * If at least one combinable text drawable had its location changed,
         * all amalgamated text drawables will need to be recomputed from
         * scratch. Otherwise, iterate through the amalgamated text drawables,
         * notifying each of the zoom change and removing its associated
         * rendering.
         */
        if (combinableTextLocationChanged) {

            /*
             * Replace the old entries in the locations-to-text-drawables map
             * with the new ones compiled above.
             */
            textDrawablesForLocations.clear();
            textDrawablesForLocations.putAll(newTextDrawablesForLocations);

            /*
             * Remove any renderings for old amalgamated text drawables.
             */
            for (TextDrawable amalgamatedDrawable : textDrawablesForAmalgamatedDrawables
                    .keySet()) {
                removeRenderingForDrawable(amalgamatedDrawable);
            }

            /*
             * Remove any amalgamated text drawables, and any associations
             * between them and lists of text drawables.
             */
            amalgamatedDrawablesForTextDrawables.clear();
            textDrawablesForAmalgamatedDrawables.clear();
            amalgamatedTextDrawablesNeedingUpdate.clear();

            /*
             * Compile the locations needing an update by iterating through all
             * recorded combinable text locations, adding each to the set that
             * has more than 1 text drawable associated with it.
             */
            locationsNeedingUpdate.clear();
            for (Map.Entry<Coordinate, List<TextDrawable>> entry : textDrawablesForLocations
                    .entrySet()) {
                if (entry.getValue().size() > 1) {
                    locationsNeedingUpdate.add(entry.getKey());
                }
            }

        } else {
            for (TextDrawable drawable : textDrawablesForAmalgamatedDrawables
                    .keySet()) {
                if (drawable.handleZoomChange()) {
                    removeRenderingForDrawable(drawable);
                }
            }
        }
    }

    /**
     * Draw entities with hatched areas. This is done using shaded shapes to
     * increase redrawing performance during zoom and pan operations.
     * 
     * @param target
     *            The target upon which to draw.
     * @param paintProperties
     *            Properties describing how drawables appear on the target.
     * @param scaleChanged
     *            Flag indicating whether or not the scale has changed.
     */
    private void drawHatchedAreas(IGraphicsTarget target,
            PaintProperties paintProperties, boolean scaleChanged) {

        /*
         * Draw shaded geometries.
         */
        if (hatchedAreas.size() > 0) {

            /*
             * Only recreate the hatched areas if they have changed, and if not
             * zooming. If they are recreated at each stage in an ongoing zoom
             * action, performance may be negatively impacted.
             */
            if ((renderHatchedAreas || scaleChanged)
                    && (paintProperties.isZooming() == false)) {
                renderHatchedAreas = false;
                if (hatchedAreaShadedShape != null) {
                    hatchedAreaShadedShape.dispose();
                }

                if (hatchedAreas.isEmpty()) {
                    return;
                }
                hatchedAreaShadedShape = target.createShadedShape(false,
                        spatialDisplay.getDescriptor().getGridGeometry());
                JTSCompiler groupCompiler = new JTSCompiler(
                        hatchedAreaShadedShape, null,
                        spatialDisplay.getDescriptor());

                try {
                    for (AbstractDrawableComponent hatchedArea : hatchedAreas) {
                        if ((hatchedArea instanceof PathDrawable)
                                && ((PathDrawable) hatchedArea).isClosedLine()) {
                            PathDrawable polygon = (PathDrawable) hatchedArea;
                            Color[] colors = polygon.getColors();
                            JTSGeometryData data = groupCompiler
                                    .createGeometryData();
                            data.setGeometryColor(new RGB(colors[0].getRed(),
                                    colors[0].getGreen(), colors[0].getBlue()));
                            groupCompiler.handle((Geometry) polygon
                                    .getGeometry().getGeometry().clone(), data);
                        }
                    }

                    hatchedAreaShadedShape.compile();

                    hatchedAreaShadedShape.setFillPattern(FillPatterns
                            .getGLPattern(GL_PATTERN_VERTICAL_DOTTED));

                } catch (VizException e) {
                    statusHandler.error("Error compiling hazard hatched areas",
                            e);
                }

            }

            /*
             * Draw the shaded shape if it exists.
             */
            if (hatchedAreaShadedShape != null) {
                try {
                    target.drawShadedShape(hatchedAreaShadedShape,
                            paintProperties.getAlpha());
                } catch (VizException e) {
                    statusHandler.error("Error drawing hazard hatched areas.",
                            e);
                }
            }
        }
    }

    /**
     * Paint the specified drawable.
     * 
     * @param drawable
     *            Drawable to be painted.
     * @param target
     *            Target onto which to render the drawable.
     * @param paintProperties
     *            Paint properties associated with the target.
     */
    private void paint(AbstractDrawableComponent drawable,
            IGraphicsTarget target, PaintProperties paintProperties) {

        Iterator<DrawableElement> iterator = drawable.createDEIterator();

        while (iterator.hasNext()) {

            DrawableElement element = iterator.next();
            AbstractElementContainer elementContainer = new DefaultVectorElementContainer(
                    element, spatialDisplay.getDescriptor(), target);

            elementContainer.draw(target, paintProperties, displayProperties);
            elementContainer.dispose();
        }
    }

    /**
     * Draw "handle bars" over the selected hazard geometry. The handle bars
     * provide visual cues as to the locations of vertices are for vertex
     * editing operations.
     * 
     * @param target
     *            The target upon which to draw.
     * @param paintProperties
     *            Properties describing how drawables appear on the target.
     * @throws VizException
     *             If a viz problem occurred during drawing.
     */
    private void drawHandlebars(IGraphicsTarget target,
            PaintProperties paintProperties) throws VizException {
        for (Map.Entry<HandlebarTypeRenderer, List<double[]>> entry : locationsForHandlebarTypeRenderers
                .entrySet()) {
            if (entry.getValue().isEmpty() == false) {
                entry.getKey()
                        .render(entry.getValue(), target, paintProperties);
            }
        }
    }
}

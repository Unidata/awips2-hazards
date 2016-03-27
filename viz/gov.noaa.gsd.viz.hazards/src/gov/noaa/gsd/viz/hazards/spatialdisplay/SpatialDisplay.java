/**
 * This software was developed and / or modified by the National Oceanic and
 * Atmospheric Administration (NOAA), Earth System Research Laboratory (ESRL),
 * Global Systems Division (GSD), Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.spatialdisplay;

import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HAZARD_EVENT_SELECTED;
import gov.noaa.gsd.common.eventbus.BoundedReceptionEventBus;
import gov.noaa.gsd.common.visuals.SpatialEntity;
import gov.noaa.gsd.viz.hazards.UIOriginator;
import gov.noaa.gsd.viz.hazards.contextmenu.ContextMenuHelper;
import gov.noaa.gsd.viz.hazards.display.HazardServicesAppBuilder;
import gov.noaa.gsd.viz.hazards.display.action.ModifyStormTrackAction;
import gov.noaa.gsd.viz.hazards.display.action.SpatialDisplayAction;
import gov.noaa.gsd.viz.hazards.display.action.ToolAction;
import gov.noaa.gsd.viz.hazards.spatialdisplay.SpatialView.SpatialViewCursorTypes;
import gov.noaa.gsd.viz.hazards.spatialdisplay.drawableelements.HazardServicesDrawableBuilder;
import gov.noaa.gsd.viz.hazards.spatialdisplay.drawableelements.HazardServicesLine;
import gov.noaa.gsd.viz.hazards.spatialdisplay.drawableelements.HazardServicesPolygon;
import gov.noaa.gsd.viz.hazards.spatialdisplay.drawableelements.HazardServicesSymbol;
import gov.noaa.gsd.viz.hazards.spatialdisplay.drawableelements.HazardServicesText;
import gov.noaa.gsd.viz.hazards.spatialdisplay.drawableelements.IHazardServicesShape;
import gov.noaa.gsd.viz.hazards.spatialdisplay.mousehandlers.SelectionAction;
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
import gov.noaa.nws.ncep.ui.pgen.display.LinePatternManager;
import gov.noaa.nws.ncep.ui.pgen.display.RasterElementContainer;
import gov.noaa.nws.ncep.ui.pgen.elements.AbstractDrawableComponent;
import gov.noaa.nws.ncep.ui.pgen.elements.DECollection;
import gov.noaa.nws.ncep.ui.pgen.elements.DrawableElement;
import gov.noaa.nws.ncep.ui.pgen.elements.Layer;
import gov.noaa.nws.ncep.ui.pgen.elements.Symbol;
import gov.noaa.nws.ncep.ui.pgen.elements.Text;
import gov.noaa.nws.ncep.ui.pgen.gfa.IGfa;

import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.google.common.collect.Lists;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardStatus;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.DataTime;
import com.raytheon.uf.common.time.SimulatedTime;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.viz.core.AbstractTimeMatcher;
import com.raytheon.uf.viz.core.IDisplayPaneContainer;
import com.raytheon.uf.viz.core.IGraphicsTarget;
import com.raytheon.uf.viz.core.IGraphicsTarget.PointStyle;
import com.raytheon.uf.viz.core.datastructure.PerspectiveSpecificProperties;
import com.raytheon.uf.viz.core.drawables.FillPatterns;
import com.raytheon.uf.viz.core.drawables.IDescriptor.FramesInfo;
import com.raytheon.uf.viz.core.drawables.IShadedShape;
import com.raytheon.uf.viz.core.drawables.PaintProperties;
import com.raytheon.uf.viz.core.drawables.ResourcePair;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.map.IMapDescriptor;
import com.raytheon.uf.viz.core.rsc.IInputHandler;
import com.raytheon.uf.viz.core.rsc.IResourceDataChanged;
import com.raytheon.uf.viz.core.rsc.LoadProperties;
import com.raytheon.uf.viz.core.rsc.RenderingOrderFactory;
import com.raytheon.uf.viz.core.rsc.RenderingOrderFactory.ResourceOrder;
import com.raytheon.uf.viz.core.rsc.tools.AbstractMovableToolLayer;
import com.raytheon.uf.viz.d2d.core.D2DProperties;
import com.raytheon.uf.viz.d2d.core.time.D2DTimeMatcher;
import com.raytheon.uf.viz.hazards.sessionmanager.ISessionManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.ISessionConfigurationManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.ObservedSettings;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.ISettings;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.StartUpConfig;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.ToolType;
import com.raytheon.uf.viz.hazards.sessionmanager.events.ISessionEventManager;
import com.raytheon.uf.viz.hazards.sessionmanager.events.impl.ObservedHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.IOriginator;
import com.raytheon.uf.viz.hazards.sessionmanager.recommenders.RecommenderExecutionContext;
import com.raytheon.viz.awipstools.IToolChangedListener;
import com.raytheon.viz.core.rsc.jts.JTSCompiler;
import com.raytheon.viz.core.rsc.jts.JTSCompiler.JTSGeometryData;
import com.raytheon.viz.hydro.perspective.HydroPerspectiveManager;
import com.raytheon.viz.hydro.resource.MultiPointResource;
import com.raytheon.viz.hydrocommon.data.GageData;
import com.raytheon.viz.ui.VizWorkbenchManager;
import com.raytheon.viz.ui.cmenu.AbstractRightClickAction;
import com.raytheon.viz.ui.cmenu.IContextMenuContributor;
import com.raytheon.viz.ui.editor.AbstractEditor;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygonal;
import com.vividsolutions.jts.operation.valid.IsValidOp;

/**
 * This is the AbstractVizResource used for the display of hazards. This
 * resource should remain loaded in CAVE for the run duration of Hazard
 * Services. This resource interfaces with NCEP PGEN code for creating the
 * drawables.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 21, 2011            Xiangbao       Initial creation
 * Jul 21, 2012            Xiangbao       Added multiple events selection
 *                         Bryon.Lawrence Modified
 * May 29, 2013            Bryon.Lawrence Added code to handle multiple
 *                                        deselection of hazards.
 * Jun 04, 2013            Bryon.Lawrence Added support for events with multiple
 *                                        hazard polygons.
 * Jun 14, 2013            Bryon.Lawrence Modified the drawing of the polygon
 *                                        handlebars to use IGraphicsTarget.drawPoints
 *                                        to improve drawing performance.
 * Jun 24, 2013            Bryon.Lawrence Removed the 'Move Entire Element' option from
 *                                        from the right-click context menu.
 * Jul 10, 2013     585    Chris.Golden   Changed to support loading from bundle.
 * Jul 12, 2013            Bryon.Lawrence Added ability to draw persistent shapes.
 * Jul 18, 2013   1264     Chris.Golden   Added support for drawing lines and
 *                                        points.
 * Aug  9, 2013 1921       daniel.s.schaffer@noaa.gov  Support of replacement of JSON with POJOs
 * Aug 22, 2013    1936    Chris.Golden   Added console countdown timers.
 * Aug 27, 2013 1921       Bryon.Lawrence Replaced code to support multi-hazard selection using
 *                                        Shift and Ctrl keys.
 * Sep 10, 2013  752       Bryon.Lawrence Modified to use static method 
 *                                        forModifyingStormTrack in HazardServicesDrawableBuilder
 * Nov  04, 2013 2182     daniel.s.schaffer@noaa.gov      Started refactoring
 * Nov 15, 2013  2182       daniel.s.schaffer@noaa.gov    Refactoring JSON - ProductStagingDialog
 * Nov  18, 2013 1462     Bryon.Lawrence  Added hazard area hatching.
 * Nov  23, 2013 2474     Bryon.Lawrence  Made fix to prevent NPE when using right
 *                                        click context menu.
 * Nov 29, 2013  2378     Bryon.Lawrence  Changed to use hazard event modified flag instead of
 *                                        test for PENDING when testing whether or not to show
 *                                        End Selected Hazard context menu item.
 * Sep 09, 2014  3994     Robert.Blum     Added handleMouseEnter to reset the cursor type.
 * Oct 20, 2014  4780     Robert.Blum     Made fix to Time Matching to update to the Time Match Basis.
 * Nov 18, 2014  4124     Chris.Golden    Adapted to new time manager.
 * Dec 01, 2014  4188      Dan Schaffer Now allowing hazards to be shrunk or expanded when appropriate.
 * Dec 05, 2014  4124      Chris.Golden   Changed to work with newly parameterized config manager
 *                                        and with ObservedSettings.
 * Dec 13, 2014 4959       Dan Schaffer Spatial Display cleanup and other bug fixes
 * Feb 09, 2015 6260       Dan Schaffer   Fixed bugs in multi-polygon handling
 * Feb 10, 2015  3961     Chris.Cody      Add Context Menu (R-Click) for River Point (GageData) objects
 * Feb 12, 2015 4959       Dan Schaffer Modify MB3 add/remove UGCs to match Warngen
 * Feb 15, 2015 2271       Dan Schaffer   Incur recommender/product generator init costs immediately
 * Feb 24, 2015 6499       Dan Schaffer   Disable moving/drawing of point hazards
 * Mar 13, 2015 6090       Dan Schaffer Relaxed geometry validity check.
 * Mar 19, 2015 6938       mduff        Increased size of handlebars to 1.5 mag.
 * Apr 03, 2015 6815       mduff        Fix memory leak.
 * May 05, 2015 7624       mduff        Drawing Optimizations.
 * May 21, 2015 7730       Chris.Cody   Move Add/Delete Vertex to top of Context Menu
 * Jun 11, 2015 7921       Chris.Cody   Correctly render hazard events in D2D when Map Scale changes 
 * Jun 17, 2015 6730       Robert.Blum  Fixed invalid geometry (duplicate rings) bug caused by duplicate
 *                                      ADCs being drawn for one hazard.
 * Jun 22, 2015 7203       Chris.Cody   Prevent Event Text Data overlap in a single county
 * Jun 24, 2015 6601       Chris.Cody   Change Create by Hazard Type display text
 * Jul 07, 2015 7921       Chris.Cody   Re-scale hatching areas and handle bar points
 * Sep 18, 2015 9905       Chris.Cody   Correct Spatial Display selection error
 * Oct 26, 2015 12754      Chris.Golden Fixed drawing bug that caused only one hazard to be drawn at a
 *                                      time, regardless of how many should have been displayed.
 * Nov 10, 2015 12762      Chris.Golden Added support for use of new recommender manager.
 * Mar 16, 2016 15676      Chris.Golden Changed to make visual features work. Will be refactored to
 *                                      remove numerous existing kludges.
 * Mar 24, 2016 15676      Chris.Golden Numerous bug fixes for handlebar points with respect to multiple
 *                                      selection, selection itself of non-editable/movable shapes,
 *                                      visual feature stuff, etc. For the most part, what worked prior
 *                                      to visual features should now work again, with some enhancements
 *                                      in terms of better handlebar point usage and selectability of
 *                                      immutable shapes.
 * Mar 26, 2016 15676      Chris.Golden Added method to check Geometry objects' validity, and method
 *                                      to allow the display to be notified of changes made by the user
 *                                      to visual features. Also fixed selection-tracking bug causing
 *                                      handlebars to appear around deselected hazard event geometries.
 * </pre>
 * 
 * @author Xiangbao Jing
 */
public class SpatialDisplay extends
        AbstractMovableToolLayer<AbstractDrawableComponent> implements
        IContextMenuContributor, IToolChangedListener, IResourceDataChanged,
        IOriginator {

    // Private Classes

    /**
     * Vector element container, a replacement for its superclass that creates
     * objects of type {@link AlphaCapableLineDisplayElement} instead of
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
         * Copied from superclass, with only modification being the use of the
         * customized factory.
         */
        @Override
        public void draw(IGraphicsTarget target, PaintProperties paintProps,
                DisplayProperties dprops, boolean needsCreate) {

            /*
             * For ghost drawing - "needsCreate && dprops == null" - It is
             * always on the active layer so DisplayProperties' "filled" should
             * be true while "monoColor" should be false (using the element's
             * color).
             */
            if (dprops == null) {
                dprops = new DisplayProperties(false, null, true);
            }

            if (needsCreate) {
                dprops.setLayerMonoColor(false);
                dprops.setLayerFilled(true);
            }

            /*
             * For normal drawing........
             */
            if ((displayEls == null) || paintProps.isZooming()) {
                needsCreate = true;

                /*
                 * TTR971 - needs to set display properties, otherwise the layer
                 * color may not take effect (e.g., after switching projection)
                 */
                factory.setLayerDisplayAttributes(dprops.getLayerMonoColor(),
                        dprops.getLayerColor(), dprops.getLayerFilled());
            }

            if (paintProps.getZoomLevel() != zoomLevel) {
                needsCreate = true;
                zoomLevel = paintProps.getZoomLevel();
            }

            if ((dprops != null) && !dprops.equals(saveProps)) {
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
     * Raster element container, a replacement for its superclass that creates
     * objects of type {@link AlphaCapableLineDisplayElement} instead of
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

    // Package Static Constants

    /**
     * Layer name.
     */
    static final String LAYER_NAME = "Hazard Services";

    // Private Static Constants

    /**
     * Pattern to be used for fills.
     */
    private static final String GL_PATTERN_VERTICAL_DOTTED = "VERTICAL_DOTTED";

    /**
     * Distance in pixels of "slop" used when testing for hits on clickable
     * elements.
     */
    private static final int HIT_TEST_SLOP_DISTANCE_PIXELS = (int) SelectionAction.SELECTION_DISTANCE_PIXELS;

    /**
     * Relative size of the handlebars drawn on selected hazards.
     */
    private static final float HANDLEBAR_MAGNIFICATION = 1.5f;

    // Private Static Variables

    /**
     * Logging mechanism.
     */
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(SpatialDisplay.class);

    // Private Variables

    /**
     * App builder.
     */
    private HazardServicesAppBuilder appBuilder;

    /**
     * Map of drawable elements to the containers of their displayables.
     */
    private final ConcurrentMap<AbstractDrawableComponent, AbstractElementContainer> displayMap;

    /**
     * Drawable element used when an element is being created or modified; this
     * one allows the changes to occur, while the original is left as it was
     * pre-edit until the edit is complete.
     */
    private AbstractDrawableComponent elementEditedGhost;

    /**
     * Drawable element currently being edited in some way.
     */
    private AbstractDrawableComponent elementEdited;

    /**
     * Selected elements; these are elements that are part of selected hazard
     * events (whether as base geometries or visual features).
     */
    private final Set<AbstractDrawableComponent> selectedElements = new HashSet<>();

    /**
     * Geometry factory, used for creating points during hit-testing.
     */
    private final GeometryFactory geometryFactory;

    /**
     * Selected element over which the cursor is currently hovering, if any.
     */
    private AbstractDrawableComponent hoverElement;

    /**
     * Builder for the various possible hazard geometries.
     */
    private HazardServicesDrawableBuilder drawableBuilder;

    /**
     * PGEN layer manager.
     */
    private final PgenLayerManager pgenLayerManager;

    /**
     * Flag indicating whether or not this viz resource is acting as the basis
     * for time matching.
     */
    private boolean timeMatchBasis;

    /**
     * Flag indicating whether or not this viz resource was previously acting as
     * the basis for time matching.
     */
    private boolean prevTimeMatchBasis;

    /**
     * Current frame count.
     */
    private int frameCount = -1;

    /**
     * Mouse action handler. There are several different mouse modes for Hazard
     * Services. This class uses a strategy pattern to control the mouse
     * behavior in this viz resource.
     */
    private IInputHandler mouseHandler = null;

    /**
     * Flag indicating whether or not a dispose notification should be generated
     * when this tool layer is disposed of.
     */
    private boolean generateDisposeMessage = true;

    /**
     * Event bus used to fire off notifications.
     */
    private BoundedReceptionEventBus<Object> eventBus = null;

    /**
     * Spatial view that manages this object.
     */
    private SpatialView spatialView;

    /**
     * Color of the hover element's handle bars.
     */
    private org.eclipse.swt.graphics.Color handleBarColor;

    /**
     * Flag indicating whether or not the perspective is currently changing.
     */
    private boolean perspectiveChanging = false;

    /**
     * Vertices of the current hover element converted to world pixels. This
     * recomputed once per change in the hover element for efficiency.
     */
    private final List<double[]> handleBarPoints = new ArrayList<>();

    /**
     * Contains a map in which each entry contains a unique identifier and a
     * list of shapes. These are not events, and they are persisted across zoom,
     * pan and time change operations. It is up to the client to remove them
     * when they should not be displayed anymore.
     */
    private final Map<String, List<AbstractDrawableComponent>> persistentShapeMap;

    /*
     * A list of drawables representing the hatched areas associated with hazard
     * events.
     */
    private final List<AbstractDrawableComponent> hatchedAreas;

    /*
     * Flag indicating whether or not the hatched areas should be redrawn.
     */
    private boolean redrawHatchedAreas = false;

    /*
     * A list of drawables representing the annotations associated with hazard
     * hatch areas.
     */
    private final List<AbstractDrawableComponent> hatchedAreaAnnotations;

    private IShadedShape hatchedAreaShadedShape;

    /**
     * Track Current Map Scale Factor. This is necessary for adjusting to Map
     * Scale (not zoom) changes.
     */
    double mapScaleFactor = 0.0d;

    /**
     * Default Map Scale Name. Does not match any valid scale name. This is by
     * design. This is necessary for adjusting to Map Scale (not zoom) changes
     * for D2D Perspective.
     */
    private final String DEFAULT_MAP_SCALE_NAME = "OTHER";

    /**
     * Track Current Map Scale Name. This is necessary for adjusting to Map
     * Scale (not zoom) changes for D2D Perspective.
     */
    private String mapScale = DEFAULT_MAP_SCALE_NAME;

    /**
     * Track current Map Zoom Level. Changing Paint Properties Zoom level will
     * force a redraw of Spatial Display components.
     */
    private float zoomLevel = 1.0f;

    /**
     * Small Zoom Level change factor to force redraw. The intent is for the
     * drawing methods to recognize a change, but have a change small enough not
     * to alter the rendered image.
     */
    private final float iotaZoomLevelFactor = 0.00001f;

    /**
     * Constructor.
     * 
     * @param resourceData
     *            The resource data for the display
     * @param loadProperties
     *            The properties describing this resource.
     * @param loadedFromBundle
     *            Flag indicating whether or not this tool layer is being
     *            instantiated as a result of a bundle load.
     * @param appBuilder
     *            App builder that already exists, if any. If <code>null</code>,
     *            an app builder will be created as part of ths construction.
     */
    public SpatialDisplay(SpatialDisplayResourceData resourceData,
            LoadProperties loadProperties, final boolean loadedFromBundle,
            final HazardServicesAppBuilder appBuilder) {

        super(resourceData, loadProperties);

        /*
         * Test if the developer/user wants to set the CAVE clock to the canned
         * data time. This is really for developers to facilitate their testing
         * during development. This will be removed for IOC.
         */
        String useCannedTime = System.getenv("HAZARD_SERVICES_USE_CANNED_TIME");
        if (useCannedTime != null && useCannedTime.equalsIgnoreCase("true")) {
            Date date = new Date();
            date.setTime(Long.parseLong(HazardServicesAppBuilder.CANNED_TIME));
            Calendar simulatedDate = TimeUtil.newCalendar(TimeZone
                    .getTimeZone("UTC"));
            simulatedDate.setTime(date);
            SimulatedTime.getSystemTime().setFrozen(true);
            // SimulatedTime.getSystemTime().setFrozen(false);
            SimulatedTime.getSystemTime().setTime(simulatedDate.getTime());
        }

        displayMap = new ConcurrentHashMap<>();
        geometryFactory = new GeometryFactory();

        pgenLayerManager = new PgenLayerManager();
        persistentShapeMap = new HashMap<>();
        hatchedAreas = new ArrayList<>();
        hatchedAreaAnnotations = new ArrayList<>();

        dataTimes = new ArrayList<>();

        /**
         * 
         * The tool layer may be instantiated from within the UI thread, or from
         * another thread (for example, a non-UI thread is used for creating the
         * tool layer as part of a bundle load). Ensure that the rest of the
         * initialization happens on the UI thread.
         */
        Runnable initializationFinisher = new Runnable() {
            @Override
            public void run() {
                handleBarColor = Display.getCurrent().getSystemColor(
                        SWT.COLOR_GRAY);

                if (appBuilder != null) {
                    SpatialDisplay.this.appBuilder = appBuilder;
                    drawableBuilder = new HazardServicesDrawableBuilder(
                            SpatialDisplay.this.appBuilder.getSessionManager());
                } else {
                    SpatialDisplay.this.appBuilder = new HazardServicesAppBuilder(
                            SpatialDisplay.this);
                    drawableBuilder = new HazardServicesDrawableBuilder(
                            SpatialDisplay.this.appBuilder.getSessionManager());
                    SpatialDisplay.this.appBuilder.buildGUIs(loadedFromBundle);
                }

                eventBus = SpatialDisplay.this.appBuilder.getEventBus();

                // If the resource data has a setting to be used, use
                // that; otherwise, give the resource data the setting
                // already in use by the app builder so that it will
                // have it in case it is saved as part of a bundle.
                ISettings settings = ((SpatialDisplayResourceData) getResourceData())
                        .getSettings();
                if (settings != null) {
                    SpatialDisplay.this.appBuilder.setCurrentSettings(settings,
                            UIOriginator.SPATIAL_DISPLAY);

                } else {
                    ObservedSettings observedSettings = SpatialDisplay.this.appBuilder
                            .getCurrentSettings();
                    ((SpatialDisplayResourceData) getResourceData())
                            .setSettings(observedSettings);
                }

            }
        };

        if (Display.getDefault().getThread() == Thread.currentThread()) {
            initializationFinisher.run();
        } else {
            Display.getDefault().asyncExec(initializationFinisher);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.viz.core.rsc.IVizResource#getName()
     */
    @Override
    public String getName() {
        return LAYER_NAME;
    }

    /**
     * This is used to initialize the Hazard Services Tool Layer Abstract Viz
     * Resource. Also, it loads the PGEN line pattern manager. This can take a
     * few seconds, so it is done at Hazard Services start-up.
     * 
     * @see com.raytheon.uf.viz.core.rsc.AbstractVizResource#init(com.raytheon.uf
     *      .viz.core.IGraphicsTarget)
     * @param target
     *            The graphics target which will receive drawables.
     */
    @Override
    public void initInternal(IGraphicsTarget target) throws VizException {
        // This needs to be done to register this tool's mouse listener with
        // the Pane Manager.
        super.initInternal(target);

        // Initialize the line pattern manager here.
        // This saves time with drawing later.
        new Job("Loading PGEN LinePatternManager...") {

            @Override
            protected IStatus run(IProgressMonitor monitor) {
                LinePatternManager.getInstance();
                return Status.OK_STATUS;
            }

        }.schedule();

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.viz.core.rsc.IVizResource#dispose()
     */
    @Override
    protected void disposeInternal() {
        appBuilder = null;

        super.disposeInternal();

        // Fire a spatial display dispose event if the perspective is not
        // changing, since this means that Hazard Services should close.
        if (perspectiveChanging == false) {
            fireSpatialDisplayDisposedActionOccurred();
        }

        if (hatchedAreaShadedShape != null) {
            hatchedAreaShadedShape.dispose();
        }

    }

    @Override
    public void addContextMenuItems(IMenuManager menuManager, int x, int y) {
        /**
         * In here, add the options to delete a vertex and add a vertex, but
         * only if over a selected hazard and point. Retrieve the list of
         * context menu items to add...
         */
        setCurrentEvent(x, y);
        List<IAction> actions = getContextMenuActions();
        for (IAction action : actions) {
            menuManager.add(action);
        }
        menuManager.add(new RiverGageFloodAction());
        menuManager.add(new Separator());

    }

    @Override
    public void toolChanged() {

    }

    @Override
    protected void paint(IGraphicsTarget target, PaintProperties paintProps,
            AbstractDrawableComponent object,
            AbstractMovableToolLayer.SelectionStatus status)
            throws VizException {
        drawProduct(target, paintProps, object);
    }

    @Override
    protected boolean isClicked(IDisplayPaneContainer container,
            Coordinate mouseLoc, AbstractDrawableComponent object) {
        return false;
    }

    @Override
    protected AbstractDrawableComponent makeLive(
            AbstractDrawableComponent object) {
        return null;
    }

    @Override
    protected AbstractDrawableComponent move(Coordinate lastMouseLoc,
            Coordinate mouseLoc, AbstractDrawableComponent object) {
        return null;
    }

    @Override
    protected String getDefaultName() {
        return null;
    }

    @Override
    protected void paintInternal(IGraphicsTarget target,
            PaintProperties paintProps) throws VizException {

        checkForMapRescale(paintProps);

        /*
         * Paint shaded shapes for hatched areas
         */
        drawHatchedAreas(target, paintProps);

        if (elementEditedGhost != null) {
            drawGhostOfElementBeingEdited(target, paintProps);
        }

        super.paintInternal(target, paintProps);

        for (AbstractDrawableComponent visualFeatureDrawable : visualFeatureDrawables) {
            drawProduct(target, paintProps, visualFeatureDrawable);
        }

        /*
         * Draw the hover elements.
         */
        drawHover(target, paintProps);
    }

    @Override
    public boolean handleMouseDown(int x, int y, int mouseButton) {
        if ((mouseHandler != null) && isEditable()) {
            return mouseHandler.handleMouseDown(x, y, mouseButton);
        }

        return false;
    }

    @Override
    public boolean handleMouseDownMove(int x, int y, int button) {
        if ((mouseHandler != null) && isEditable()) {
            return mouseHandler.handleMouseDownMove(x, y, button);
        }
        return false;

    }

    @Override
    public boolean handleMouseUp(int x, int y, int mouseButton) {

        if ((mouseHandler != null) && isEditable()) {
            return mouseHandler.handleMouseUp(x, y, mouseButton);
        }

        return false;
    }

    @Override
    public boolean handleMouseMove(int x, int y) {
        if ((mouseHandler != null) && isEditable()) {
            return mouseHandler.handleMouseMove(x, y);
        } else {
            return false;
        }
    }

    @Override
    public boolean handleKeyDown(int key) {
        if ((mouseHandler != null) && isEditable()) {
            return mouseHandler.handleKeyDown(key);
        } else {
            return false;
        }
    }

    @Override
    public boolean handleKeyUp(int key) {
        if ((mouseHandler != null) && isEditable()) {
            return mouseHandler.handleKeyUp(key);
        } else {
            return false;
        }
    }

    @Override
    public boolean handleMouseEnter(Event event) {
        if ((mouseHandler != null) && isEditable()) {
            return mouseHandler.handleMouseEnter(event);
        } else {
            return handleMouseMove(event.x, event.y);
        }
    }

    @Override
    public DataTime[] getDataTimes() {

        // Save off previous state
        prevTimeMatchBasis = timeMatchBasis;

        // Determine if this rsc is the time match basis.
        AbstractTimeMatcher time = this.getDescriptor().getTimeMatcher();
        if (time instanceof D2DTimeMatcher) {
            if (((D2DTimeMatcher) time).getTimeMatchBasis() == this) {
                timeMatchBasis = true;
            } else {
                timeMatchBasis = false;
            }
        }

        DataTime currentTime = null;
        FramesInfo info = descriptor.getFramesInfo();
        if (timeMatchBasis) {
            /*
             * We only want to calculate more data times if the user has
             * selected more frames than there have been in the past.
             */
            if (this.descriptor.getNumberOfFrames() > this.frameCount) {
                int variance = this.descriptor.getNumberOfFrames()
                        - this.frameCount;

                this.frameCount = this.descriptor.getNumberOfFrames();

                DataTime earliestTime = this.dataTimes.get(0);
                this.fillDataTimeArray(earliestTime, variance);
            }

            // Just switched Time Match Basis update frames
            if (prevTimeMatchBasis == false && timeMatchBasis == true) {
                dataTimes.clear();
                currentTime = new DataTime(SimulatedTime.getSystemTime()
                        .getTime());

                dataTimes.add(currentTime);
                this.fillDataTimeArray(currentTime,
                        this.descriptor.getNumberOfFrames() - 1);
            }
        } else {
            dataTimes.clear();
            // First time called
            if (info.getFrameTimes() != null) {
                for (DataTime dt : info.getFrameTimes()) {
                    dataTimes.add(dt);
                }
                this.descriptor.getTimeMatchingMap().put(this,
                        info.getFrameTimes());
            }
            if (dataTimes.size() == 0) {
                // Case where this tool is time match basis or no data loaded
                if (dataTimes.size() > 0) {
                    currentTime = dataTimes.get(dataTimes.size() - 1);
                } else {
                    currentTime = new DataTime(SimulatedTime.getSystemTime()
                            .getTime());
                }

                dataTimes.add(currentTime);
                this.fillDataTimeArray(currentTime,
                        this.descriptor.getNumberOfFrames() - 1);
            }
        }
        Collections.sort(dataTimes);
        return dataTimes.toArray(new DataTime[dataTimes.size()]);
    }

    @Override
    public ResourceOrder getResourceOrder() {
        return RenderingOrderFactory.ResourceOrder.HIGHEST;
    }

    /**
     * Get the app builder.
     * 
     * @return App builder.
     */
    public HazardServicesAppBuilder getAppBuilder() {
        return appBuilder;
    }

    /**
     * Set the spatial view.
     * 
     * @param view
     *            New spatial view.
     */
    public void setSpatialView(SpatialView view) {
        spatialView = view;
    }

    /**
     * Receive notification that the perspective is changing.
     */
    public void perspectiveChanging() {
        perspectiveChanging = true;
    }

    /**
     * Draws the hazard events on the spatial display.
     * 
     */
    public void drawEvents(Collection<ObservedHazardEvent> events,
            Map<String, Boolean> eventOverlapSelectedTime,
            Map<String, Boolean> forModifyingStormTrack,
            Map<String, Boolean> eventEditability,
            boolean toggleAutoHazardChecking, boolean areHatchedAreasDisplayed) {
        clearDrawables();
        selectedElements.clear();
        setHoverElement(null);

        hatchedAreas.clear();
        hatchedAreaAnnotations.clear();

        Layer activeLayer = getActiveLayer();
        for (ObservedHazardEvent hazardEvent : events) {

            /*
             * Keep an inventory of which events are selected.
             */
            String eventID = hazardEvent.getEventID();
            Boolean isSelected = (Boolean) hazardEvent
                    .getHazardAttribute(HAZARD_EVENT_SELECTED);

            drawableBuilder.buildDrawableComponents(this, hazardEvent,
                    eventOverlapSelectedTime.get(eventID), activeLayer,
                    forModifyingStormTrack.get(eventID),
                    eventEditability.get(eventID), areHatchedAreasDisplayed);

            if (areHatchedAreasDisplayed && isSelected) {
                redrawHatchedAreas = true;
                drawableBuilder.buildhazardAreas(this, hazardEvent,
                        getActiveLayer(), hatchedAreas, hatchedAreaAnnotations);
            }

        }

        List<AbstractDrawableComponent> drawables = new ArrayList<>(
                pgenLayerManager.getActiveLayer().getDrawables());
        Iterator<AbstractDrawableComponent> drawableIterator = drawables
                .iterator();
        while (drawableIterator.hasNext()) {
            AbstractDrawableComponent element = drawableIterator.next();
            if ((element instanceof IHazardServicesShape)
                    && ((IHazardServicesShape) element).isVisualFeature()) {
                drawableIterator.remove();
            }
        }
        repositionTextComponents(drawables);
        hatchedAreaAnnotations.addAll(drawables);
        setObjects(hatchedAreaAnnotations);
        issueRefresh();
    }

    /**
     * List of visual feature drawables.
     */
    private final List<AbstractDrawableComponent> visualFeatureDrawables = new ArrayList<>();

    /**
     * Handle user modification of the geometry of the specified hazard event's
     * specified visual feature.
     * 
     * @param eventIdentifier
     *            Identifier of the hazard event with which the visual feature
     *            is associated.
     * @param featureIdentifier
     *            Visual feature identifier.
     * @param selectedTime
     *            Selected time for which the geometry is to be changed.
     * @param newGeometry
     *            New geometry to be used by the visual feature.
     */
    public void handleUserModificationOfVisualFeature(String eventIdentifier,
            String featureIdentifier, Date selectedTime, Geometry newGeometry) {
        getAppBuilder().getSpatialPresenter()
                .handleUserModificationOfVisualFeature(eventIdentifier,
                        featureIdentifier, selectedTime, newGeometry);
    }

    /**
     * Draw the specified spatial entities.
     * 
     * @param spatialEntities
     *            Spatial entities to be drawn.
     * @param selectedEventIdentifiers
     *            Identifiers of hazard events that are currently selected.
     */
    public void drawSpatialEntities(
            List<SpatialEntity<VisualFeatureSpatialIdentifier>> spatialEntities,
            Set<String> selectedEventIdentifiers) {

        visualFeatureDrawables.clear();

        /*
         * NOTE: It is assumed this this call is always preceded by a
         * drawEvents() call, as no elements are cleared out of the PGEN layer.
         */

        setHoverElement(null);

        Layer activeLayer = getActiveLayer();
        for (SpatialEntity<VisualFeatureSpatialIdentifier> spatialEntity : spatialEntities) {
            drawableBuilder.buildDrawableComponents(this, spatialEntity,
                    activeLayer, selectedEventIdentifiers
                            .contains(spatialEntity.getIdentifier()
                                    .getHazardEventIdentifier()));
        }

        List<AbstractDrawableComponent> drawables = new ArrayList<>(
                pgenLayerManager.getActiveLayer().getDrawables());
        Iterator<AbstractDrawableComponent> drawableIterator = drawables
                .iterator();
        while (drawableIterator.hasNext()) {
            AbstractDrawableComponent element = drawableIterator.next();
            if ((element instanceof IHazardServicesShape == false)
                    || (((IHazardServicesShape) element).isVisualFeature() == false)) {
                drawableIterator.remove();
            }
        }

        visualFeatureDrawables.addAll(drawables);

        issueRefresh();
    }

    /**
     * Scan through all Spatial Display Drawable components and amalgamate Test
     * Components that use the SAME Centroid Coorinate.
     * 
     * This method is executed just prior to Spatial Display refresh. It looks
     * through all Drawable Components for HazardServicesText components. For
     * each set of HazardServicesText components that use the same Centroid
     * Coordinate: It will take the Text String Arrays from the
     * HazardServicesText components; place their string arrays into the first
     * HazardServicesText component; Sort the strings by length (longest first);
     * and remove the HazardServicesText components containing now duplicate
     * Text data.
     * 
     * @param drawableComponents
     *            Spatial Display drawable components
     */
    protected void repositionTextComponents(
            List<AbstractDrawableComponent> drawableComponents) {

        Map<Coordinate, List<HazardServicesText>> coordToDrawableMap = new HashMap<>();

        for (AbstractDrawableComponent component : drawableComponents) {
            if ((component.getPgenType().equals("TEXT") == true)
                    && (component instanceof HazardServicesText)) {
                HazardServicesText textComponent = (HazardServicesText) component;
                Coordinate compCoord = textComponent.getPosition();
                List<HazardServicesText> componentList = coordToDrawableMap
                        .get(compCoord);
                if (componentList == null) {
                    componentList = new ArrayList<>();
                    coordToDrawableMap.put(compCoord, componentList);
                }
                componentList.add(textComponent);
            }
        }

        // Comparator to sort strings longest to shortest.
        Comparator<String> shortestLastComparator = new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return Integer.compare(o2.length(), o1.length());
            }
        };

        for (Coordinate compCoord : coordToDrawableMap.keySet()) {
            List<HazardServicesText> componentList = coordToDrawableMap
                    .get(compCoord);
            int componentListSize = componentList.size();
            if (componentListSize > 1) {
                HazardServicesText firstTextComponent = null;
                List<String> textStringList = new ArrayList<>();
                String[] textStringArray = null;
                for (int i = 0; i < componentListSize; i++) {
                    HazardServicesText currentTextComponent = componentList
                            .get(i);
                    textStringArray = currentTextComponent.getText();
                    if ((textStringArray != null)
                            && (textStringArray.length > 0)) {
                        for (int j = 0; j < textStringArray.length; j++) {
                            String currentArrayString = textStringArray[j];
                            if ((currentArrayString != null)
                                    && (textStringList
                                            .contains(currentArrayString) == false)) {
                                textStringList.add(currentArrayString);
                            }
                        }
                    }

                    if (i == 0) {
                        // Keep only the first Text Component for each
                        // Coordinate
                        firstTextComponent = currentTextComponent;
                    } else {
                        // Remove other Text Components for for each Coordinate
                        this.removeElement(currentTextComponent);
                        drawableComponents.remove(currentTextComponent);
                    }
                }

                Collections.sort(textStringList, shortestLastComparator);
                String[] textSetStringArray = textStringList
                        .toArray(new String[textStringList.size()]);
                firstTextComponent.setText(textSetStringArray);
            }
        }
    }

    public void drawStormTrackDot(String eventType) {

        AbstractDrawableComponent shapeComponent = drawableBuilder
                .buildStormTrackDotComponent(getActiveLayer(), eventType);
        List<AbstractDrawableComponent> drawableComponents = Lists
                .newArrayList(shapeComponent);
        addElement(shapeComponent, true);
        drawableComponents.add(shapeComponent);
        drawableBuilder.addTextComponent(this, HazardConstants.DRAG_DROP_DOT,
                drawableComponents, shapeComponent);

        trackPersistentShapes(HazardConstants.DRAG_DROP_DOT, drawableComponents);
        setObjects(pgenLayerManager.getActiveLayer().getDrawables());
        issueRefresh();
    }

    /**
     * Removes the ghost line from the PGEN drawing layer.
     */
    public void removeGhostOfElementBeingEdited() {
        elementEditedGhost = null;
    }

    /**
     * add a DrawableElement to the productList stored in the resource data
     * class.
     * 
     * @param de
     *            The DrawableElement being added.
     * @param selected
     *            Flag indicating whether or not the element is part of a hazard
     *            event (whether base geometry or visual feature) that is
     *            selected.
     */
    public void addElement(AbstractDrawableComponent de, boolean selected) {
        pgenLayerManager.addElement(de);
        if (selected) {
            selectedElements.add(de);
        }
    }

    /**
     * Removes a DrawableElement from the productList stored in the resource
     * data class.
     * 
     * @param de
     *            The DrawableElement to remove.
     * @return
     */
    public void removeElement(AbstractDrawableComponent de) {
        pgenLayerManager.removeElement(de);

        selectedElements.remove(de);

        AbstractElementContainer elementContainer = displayMap.remove(de);

        if (elementContainer != null) {
            elementContainer.dispose();
        }
    }

    /**
     * Retrieve the active layer. A layer is a collection of drawable elements
     * that can be controlled as a group.
     * 
     * @return Layer
     */
    public Layer getActiveLayer() {
        return pgenLayerManager.getActiveLayer();
    }

    /**
     * Sets the ghost of the element being edited to that specified.
     * 
     * @param ghost
     *            New ghost of the element being edited.
     */
    public void setGhostOfElementBeingEdited(AbstractDrawableComponent ghost) {
        this.elementEditedGhost = ghost;
    }

    /**
     * Removes an event from the list of events contained in the spatial
     * display.
     * 
     * @param eventID
     *            The identifier of the event.
     */
    public void removeEvent(String eventID) {
        List<AbstractDrawableComponent> deList = pgenLayerManager
                .getActiveLayer().getDrawables();

        AbstractDrawableComponent[] deArray = deList
                .toArray(new AbstractDrawableComponent[10]);

        for (AbstractDrawableComponent de : deArray) {

            if (de == null) {
                break;
            }

            if (de instanceof IHazardServicesShape) {
                if (((IHazardServicesShape) de).getID().equals(eventID)) {
                    removeElement(de);
                }
            }

        }

        issueRefresh();

    }

    /**
     * Returns the event identifier for a selected drawable element on the
     * spatial display.
     * 
     * @param element
     *            The element for which to retrieve the id.
     * @param multipleSelection
     *            Indicates whether or not this is a part of a multiple
     *            selection action.
     */
    public void elementClicked(AbstractDrawableComponent element,
            boolean multipleSelection) {

        if (element instanceof DECollection) {
            element = ((DECollection) element).getItemAt(0);
        }

        if (element instanceof IHazardServicesShape) {

            String clickedEventId = ((IHazardServicesShape) element).getID();
            getAppBuilder().getSpatialPresenter().handleSelection(
                    clickedEventId, multipleSelection);
        }

    }

    /**
     * Returns the event identifier for a selected drawable element on the
     * spatial display.
     * 
     * @param element
     *            The element for which to retrieve the id.
     * @return The event ID or null if it is not an event
     */
    public String eventIDForElement(AbstractDrawableComponent element) {

        if (element instanceof DECollection) {
            element = ((DECollection) element).getItemAt(0);
        }

        if (element instanceof IHazardServicesShape) {

            String clickedEventId = ((IHazardServicesShape) element).getID();

            return clickedEventId;

        }

        return null;
    }

    /**
     * Called when multiple elements are selected.
     * 
     * @param eventIDs
     *            The identifiers of the selected events.
     * @return
     */
    public void multipleElementsClicked(Set<String> eventIDs) {
        getAppBuilder().getSpatialPresenter().updateSelectedEventIds(eventIDs);
    }

    /**
     * Removes the label associated with a drawable element.
     * 
     * @param element
     *            The element to remove the label from.
     */
    public void removeElementLabel(AbstractDrawableComponent element) {
        String eventID = eventIDForElement(element);

        if (eventID != null) {

            List<AbstractDrawableComponent> deList = pgenLayerManager
                    .getActiveLayer().getDrawables();

            AbstractDrawableComponent[] deArray = deList
                    .toArray(new AbstractDrawableComponent[10]);

            for (AbstractDrawableComponent de : deArray) {
                if (de == null) {
                    break;
                }

                if (de instanceof Text) {
                    if (((IHazardServicesShape) de).getID().equals(eventID)) {
                        removeElement(de);
                    }
                }

            }
        }
    }

    /**
     * Get the element currently being edited.
     * 
     * @return Element currently being edited.
     */
    public DrawableElement getElementBeingEdited() {
        return (elementEdited == null ? null : elementEdited.getPrimaryDE());
    }

    /**
     * Sets the element that is being edited to that specified.
     * 
     * @param element
     *            Element that is now being edited.
     */
    public void setElementBeingEdited(AbstractDrawableComponent element) {
        elementEdited = element;
    }

    /**
     * Retrieves the drawable closest to the specified point
     * 
     * @param point
     *            The point to test against.
     * @return The nearest drawable component.
     */
    public AbstractDrawableComponent getNearestComponent(Coordinate point) {

        AbstractEditor editor = (AbstractEditor) VizWorkbenchManager
                .getInstance().getActiveEditor();
        double screenCoord[] = editor.translateInverseClick(point);

        /**
         * Note that the distance unit of the JTS distance function is central
         * angle degrees. This seems to match closely with degrees lat and lon.
         * 
         */
        double minDist = Double.MAX_VALUE;

        Iterator<AbstractDrawableComponent> iterator = pgenLayerManager
                .getActiveLayer().getComponentIterator();

        Point clickScreenPoint = geometryFactory.createPoint(new Coordinate(
                screenCoord[0], screenCoord[1]));

        AbstractDrawableComponent closestSymbol = null;

        while (iterator.hasNext()) {
            AbstractDrawableComponent comp = iterator.next();

            if (!(comp instanceof Text)
                    && !(comp instanceof HazardServicesLine)) {
                Geometry p = ((IHazardServicesShape) comp).getGeometry();

                if (p != null) {
                    // Convert the polygon vertices into pixels
                    Coordinate[] coords = p.getCoordinates();

                    for (int i = 0; i < coords.length; ++i) {
                        screenCoord = editor.translateInverseClick(coords[i]);
                        Point geometryPoint = geometryFactory
                                .createPoint(new Coordinate(screenCoord[0],
                                        screenCoord[1]));

                        double distance = clickScreenPoint
                                .distance(geometryPoint);

                        if (distance < minDist) {
                            minDist = distance;
                            closestSymbol = comp;
                        }
                    }
                }
            }
        }

        if (minDist <= SelectionAction.SELECTION_DISTANCE_PIXELS) {
            return closestSymbol;
        }

        return null;
    }

    /**
     * Given a point, finds the containing drawable component.
     * 
     * @param point
     *            Point in geographic space to test against.
     * @param x
     *            X coordinate of point in pixel space.
     * @param y
     *            Y coordinate of point in pixel space.
     * @return The drawable component which contains this point.
     */
    public AbstractDrawableComponent getContainingComponent(Coordinate point,
            int x, int y) {
        Iterator<AbstractDrawableComponent> iterator = pgenLayerManager
                .getActiveLayer().getComponentIterator();

        /**
         * 
         * Check each of the hazard polygons. Normally, there will not be too
         * many of these. However, we can make this more efficient by A using a
         * tree and storing/resusing the Geometries.
         * 
         */
        Point clickPoint = geometryFactory.createPoint(point);
        Geometry clickPointWithSlop = clickPoint
                .buffer(getTranslatedHitTestSlopDistance(point, x, y));

        AbstractDrawableComponent selectedSymbol = null;

        while (iterator.hasNext()) {
            AbstractDrawableComponent comp = iterator.next();

            if (comp instanceof IHazardServicesShape) {
                Geometry p = ((IHazardServicesShape) comp).getGeometry();
                if (p != null) {
                    boolean contains = (p instanceof Polygonal ? clickPoint
                            .within(p) : clickPointWithSlop.intersects(p));
                    if (contains) {
                        if (comp instanceof HazardServicesSymbol) {
                            selectedSymbol = comp;
                        } else {
                            return comp;
                        }
                    }
                }
            }
        }

        return selectedSymbol;
    }

    /**
     * Given point, find all drawables which contain it.
     * 
     * @param point
     *            Point to test in geographic space.
     * @param x
     *            X coordinate of point in pixel space.
     * @param y
     *            Y coordinate of point in pixel space.
     * @return A list of drawables which contain this point.
     */
    public List<AbstractDrawableComponent> getContainingComponents(
            Coordinate point, int x, int y) {
        Iterator<AbstractDrawableComponent> iterator = pgenLayerManager
                .getActiveLayer().getComponentIterator();

        /**
         * Check each of the hazard polygons. Normally, there will not be too
         * many of these. However, we can make this more efficient by using a
         * tree and storing/resusing the Geometries.
         */
        Point clickPoint = geometryFactory.createPoint(point);
        Geometry clickPointWithSlop = clickPoint
                .buffer(getTranslatedHitTestSlopDistance(point, x, y));

        List<AbstractDrawableComponent> containingSymbolsList = new ArrayList<>();

        while (iterator.hasNext()) {
            AbstractDrawableComponent comp = iterator.next();

            // Skip Labels (PGEN Text Objects). These are not
            // selectable for now...
            if (comp instanceof IHazardServicesShape) {
                Geometry p = ((IHazardServicesShape) comp).getGeometry();

                if (p != null) {
                    boolean contains = (p instanceof Polygonal ? clickPoint
                            .within(p) : clickPointWithSlop.intersects(p));
                    if (contains) {
                        containingSymbolsList.add(comp);
                    }
                }
            }
        }

        /*
         * The hazards drawn first are on the bottom of the stack while those
         * drawn last are on the top of the stack. Reversing the list makes it
         * easier for applications to find the top-most containing element.
         */
        return Lists.reverse(containingSymbolsList);
    }

    /**
     * TODO This needs to me moved elsewhere - nothing to do with drawing.
     */
    public void notifyModifiedStormTrack(Map<String, Serializable> parameters) {
        ModifyStormTrackAction action = new ModifyStormTrackAction();
        action.setParameters(parameters);
        eventBus.publish(action);
    }

    /**
     * Adds entries to the right click context menu in CAVE based on the state
     * of the model and the mouse pointer's proximity to displayed hazards.
     * 
     * @param
     * @return A list of entries to add to the context menu.
     */
    public List<IAction> getContextMenuActions() {
        ISessionManager<ObservedHazardEvent, ObservedSettings> sessionManager = appBuilder
                .getSessionManager();
        ContextMenuHelper helper = new ContextMenuHelper(getAppBuilder()
                .getSpatialPresenter(), sessionManager);
        List<IAction> actions = new ArrayList<>();

        IAction action = null;
        // This isn't the best way to determine this, but not sure what to do at
        // the moment.
        boolean drawCursor = spatialView
                .isCurrentCursor(SpatialViewCursorTypes.DRAW_CURSOR);
        boolean moveCursor = spatialView
                .isCurrentCursor(SpatialViewCursorTypes.MOVE_VERTEX_CURSOR);
        String menuLabel = null;
        if (moveCursor) {
            menuLabel = HazardConstants.CONTEXT_MENU_DELETE_VERTEX;
        } else if (drawCursor) {
            menuLabel = HazardConstants.CONTEXT_MENU_ADD_VERTEX;
        }
        if (menuLabel != null) {
            action = helper.newTopLevelAction(menuLabel);
            actions.add(action);
        }

        ISessionEventManager<ObservedHazardEvent> eventManager = sessionManager
                .getEventManager();
        List<IContributionItem> items = helper
                .getSelectedHazardManagementItems();
        if (eventManager.getEventsByStatus(HazardStatus.POTENTIAL).isEmpty() == false) {
            items.add(helper
                    .newAction(ContextMenuHelper.ContextMenuSelections.REMOVE_POTENTIAL_HAZARDS
                            .getValue()));
        }
        action = helper.createMenu("Manage hazards",
                items.toArray(new IContributionItem[0]));

        if (action != null) {
            actions.add(action);
        }

        action = helper.createMenu("Modify area...", helper
                .getSpatialHazardItems().toArray(new IContributionItem[0]));
        if (action != null) {
            actions.add(action);
        }

        action = helper.createMenu("Send to...", helper.getHazardSpatialItems()
                .toArray(new IContributionItem[0]));
        if (action != null) {
            actions.add(action);
        }

        return actions;
    }

    /**
     * Get the selected elements.
     * 
     * @return Selected elements.
     */
    public Set<AbstractDrawableComponent> getSelectedElements() {
        return Collections.unmodifiableSet(selectedElements);
    }

    /**
     * Get the element over which the cursor is currently hovering.
     * 
     * @return Element over which the cursor is currently hovering, or
     *         <code>null</code> if it is not hovering over any selected
     *         element.
     */
    public AbstractDrawableComponent getHoverElement() {
        return hoverElement;
    }

    /**
     * Set the element over which the cursor is currently hovering.
     * 
     * @param comp
     *            New hover element.
     */
    public void setHoverElement(AbstractDrawableComponent comp) {
        hoverElement = comp;

        /*
         * Update the list of world pixels associated with this element.
         */
        handleBarPoints.clear();

        if (comp != null) {
            for (Coordinate point : comp.getPoints()) {
                double[] pixelPoint = descriptor.worldToPixel(new double[] {
                        point.x, point.y });
                handleBarPoints.add(pixelPoint);
            }
        }

        issueRefresh();
    }

    /**
     * Rebuild handlebar points from the specified points.
     * 
     * @param points
     *            Points from which to rebuild handlebar points.
     */
    public void useAsHandlebarPoints(Coordinate[] points) {
        handleBarPoints.clear();
        for (Coordinate point : points) {
            double[] pixelPoint = descriptor.worldToPixel(new double[] {
                    point.x, point.y });
            handleBarPoints.add(pixelPoint);
        }
        issueRefresh();
    }

    /**
     * @return the dataManager
     */
    public PgenLayerManager getDataManager() {
        return pgenLayerManager;
    }

    /**
     * @param mouseHandler
     *            the mouseHandler to set
     */
    public void setMouseHandler(IInputHandler mouseHandler) {
        this.mouseHandler = mouseHandler;
    }

    /**
     * Set the flag indicating whether or not a dispose notification should be
     * generated when this tool layer is disposed of.
     * 
     * @param generateDisposeMessage
     *            Flag indicating whether or not a dispose notification should
     *            be generated when this tool layer is disposed of.
     */
    public void setGenerateDisposeMessage(boolean generateDisposeMessage) {
        this.generateDisposeMessage = generateDisposeMessage;
    }

    /**
     * Determine whether the specified geometry is valid or not, outputting a
     * warning message if it is not.
     * 
     * @param geometry
     *            Geometry to be checked for validity via the
     *            {@link Geometry#isValid()} method.
     * @return True if the geometry is valid, false otherwise.
     */
    public boolean checkGeometryValidity(Geometry geometry) {
        if (geometry.isValid() == false) {
            IsValidOp op = new IsValidOp(geometry);
            statusHandler.warn("Invalid geometry: "
                    + op.getValidationError().getMessage()
                    + ". Geometry modification ignored.");
            return false;
        }
        return true;
    }

    /**
     * Create displayables for an element container.
     * 
     * @param displayElements
     *            Leftover display elements from before, or <code>null</code>.
     * @param factory
     *            Display element factory, used to create the actual display
     *            elements.
     * @param element
     *            Drawable element for which display elements are to be created.
     * @param mapDescriptor
     *            Map descriptor to be used.
     * @param paintProperties
     *            Paint properties to be used.
     * @return List of displayables for the specified element's container.
     */
    private List<IDisplayable> createDisplayablesForElementContainer(
            List<IDisplayable> displayElements,
            PgenDisplayElementFactory factory, DrawableElement element,
            IMapDescriptor mapDescriptor, PaintProperties paintProperties) {

        /*
         * Clean up after any leftover displayable elements.
         */
        if ((displayElements != null) && (displayElements.isEmpty() == false)) {
            factory.reset();
        }

        /*
         * Set the range for this element.
         */
        setDrawableElementRange(element, factory, mapDescriptor,
                paintProperties);

        /*
         * Create displayables.
         */
        boolean handled = false;
        if (element instanceof IText) {
            handled = true;
            displayElements = factory.createDisplayElements((IText) element,
                    paintProperties);
        } else if (element instanceof ISymbol) {
            handled = true;
            displayElements = factory.createDisplayElements((ISymbol) element,
                    paintProperties);
        } else if (element instanceof IMultiPoint) {
            if (element instanceof IArc) {
                handled = true;
                displayElements = factory.createDisplayElements((IArc) element,
                        paintProperties);
            } else if (element instanceof ILine) {
                handled = true;
                displayElements = factory.createDisplayElements(
                        (ILine) element, paintProperties, true);
            }
        }
        if (handled == false) {
            statusHandler.error("Unexpected DrawableElement of type "
                    + element.getClass().getSimpleName()
                    + "; do not know how to create its displayables.",
                    new IllegalStateException());
            return Collections.emptyList();
        }
        return displayElements;
    }

    /**
     * Set a text element's range record. This method's implementation is copied
     * from {@link AbstractElementContainer} since it is inaccessible to this
     * subclass, with the only changes being that parameters are passed in that
     * in the original implementation are accessible as member variables.
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
     * Sends a notification when the spatial display is disposed of, such as in
     * a clear action.
     * 
     * @param
     * @return
     */
    private void fireSpatialDisplayDisposedActionOccurred() {
        if (generateDisposeMessage) {
            final SpatialDisplayAction action = new SpatialDisplayAction(
                    SpatialDisplayAction.ActionType.DISPLAY_DISPOSED);
            eventBus.publishAsync(action);
        }
    }

    /**
     * Draws an element on the Spatial Display.
     * 
     * @param target
     *            The target to draw on.
     * @param paintProps
     *            Describes how drawables will appear.
     */
    private void drawProduct(IGraphicsTarget target,
            PaintProperties paintProps, AbstractDrawableComponent el) {
        Layer layer = pgenLayerManager.getActiveLayer();

        DisplayProperties dprops = buildDisplayProperties(layer);

        AbstractElementContainer container = displayMap.get(el);
        if (container == null) {
            if (el instanceof Symbol) {
                container = new DefaultRasterElementContainer(
                        (DrawableElement) el, descriptor, target);
            } else {
                container = new DefaultVectorElementContainer(
                        (DrawableElement) el, descriptor, target);
            }
            displayMap.put(el, container);
        }

        container.draw(target, paintProps, dprops);
    }

    /**
     * Given the specified point in both geographic coordinates and in pixel
     * coordinates, determine a distance in geographic space that suffices as
     * "slop" area for hit tests for geometries that are difficult to hit (one-
     * and two-dimensional entities).
     * 
     * @param loc
     *            Geographic coordinates of point being tested.
     * @param x
     *            Pixel X coordinate of point being tested.
     * @param y
     *            Pixel Y coordinate of point being tested.
     * @return Distance in geographic space that suffices as "slop" area, or
     *         <code>0.0</code> if the distance could not be calculated.
     */
    private double getTranslatedHitTestSlopDistance(Coordinate loc, int x, int y) {

        // Find the editor.
        AbstractEditor editor = ((AbstractEditor) VizWorkbenchManager
                .getInstance().getActiveEditor());

        // Try creating a pixel point at the "slop" distance from the
        // original pixel point in each of the four cardinal directions;
        // for each one, see if this yields a translatable point, and
        // if so, return the distance between that point and the original
        // geographic point. If this fails, return 0.
        Coordinate offsetLoc = null;
        for (int j = 0; j < 4; j++) {
            offsetLoc = editor
                    .translateClick(
                            x
                                    + (HIT_TEST_SLOP_DISTANCE_PIXELS * ((j % 2) * (j == 1 ? 1
                                            : -1))),
                            y
                                    + (HIT_TEST_SLOP_DISTANCE_PIXELS * (((j + 1) % 2) * (j == 0 ? 1
                                            : -1))));
            if (offsetLoc != null) {
                return Math.sqrt(Math.pow(loc.x - offsetLoc.x, 2.0)
                        + Math.pow(loc.y - offsetLoc.y, 2.0));
            }
        }
        return 0.0;
    }

    /**
     * Clear all drawables for base geometries and visual features from the
     * spatial display. Takes into account events that need to be persisted such
     * as a storm track dot.
     */
    private void clearDrawables() {
        List<AbstractDrawableComponent> deList = pgenLayerManager
                .getActiveLayer().getDrawables();

        // Needed to use an array to prevent concurrency issues.
        AbstractDrawableComponent[] deArray = deList
                .toArray(new AbstractDrawableComponent[0]);

        for (AbstractDrawableComponent de : deArray) {
            if (de == null) {
                continue;
            }

            IHazardServicesShape shape = (IHazardServicesShape) de;
            String eventId = shape.getID();
            List<AbstractDrawableComponent> persistentDrawables = persistentShapeMap
                    .get(eventId);

            if (persistentDrawables == null
                    || !persistentDrawables.contains(de)) {

                removeElement(de);
            }
        }

        issueRefresh();
    }

    private void trackPersistentShapes(String id,
            List<AbstractDrawableComponent> drawables) {
        /*
         * This ensures that a persistent shape with the same id as one that
         * already exists will override it.
         */

        if (persistentShapeMap.containsKey(id)) {
            persistentShapeMap.remove(id);
        }

        if (!drawables.isEmpty()) {
            persistentShapeMap.put(id, drawables);
        }

    }

    /**
     * Draws the ghost of an event that is being created or modified. For
     * instance, if an event is moved, then the ghost of the event is drawn to
     * show where the event will end up when dropped on the map.
     * 
     * @param target
     *            Target which will receive drawables.
     * @param paintProperties
     *            Paint properties associated with the target.
     */
    private void drawGhostOfElementBeingEdited(IGraphicsTarget target,
            PaintProperties paintProperties) {

        Iterator<DrawableElement> iterator = elementEditedGhost
                .createDEIterator();

        while (iterator.hasNext()) {

            DrawableElement element = iterator.next();
            AbstractElementContainer dispEl = new DefaultVectorElementContainer(
                    element, descriptor, target);

            Layer layer = pgenLayerManager.getActiveLayer();
            DisplayProperties dprops = buildDisplayProperties(layer);
            dispEl.draw(target, paintProperties, dprops);
            dispEl.dispose();
        }
    }

    private DisplayProperties buildDisplayProperties(Layer layer) {
        DisplayProperties dprops = new DisplayProperties();
        dprops.setLayerMonoColor(layer.isMonoColor());
        dprops.setLayerColor(layer.getColor());
        dprops.setLayerFilled(layer.isFilled());

        return dprops;
    }

    private List<AbstractDrawableComponent> getContainingComponents(int x, int y) {
        AbstractEditor editor = ((AbstractEditor) VizWorkbenchManager
                .getInstance().getActiveEditor());
        Coordinate loc = editor.translateClick(x, y);
        return getContainingComponents(loc, x, y);
    }

    /**
     * Draws "handle bars" on the selected hazard geometry. These show where
     * vertices are for vertex editing operations.
     * 
     * @param target
     *            The target to draw to.
     * @param paintProps
     *            Describes how drawables appear on the target.
     * @throws VizException
     *             An exception was encountered while drawing the handle bar
     *             points on the target graphic
     */
    private void drawHover(IGraphicsTarget target, PaintProperties paintProps)
            throws VizException {
        if (hoverElement != null) {
            if ((hoverElement instanceof IHazardServicesShape)
                    && ((IHazardServicesShape) hoverElement).isEditable()) {
                if (!handleBarPoints.isEmpty()) {
                    target.drawPoints(handleBarPoints, handleBarColor.getRGB(),
                            PointStyle.DISC, HANDLEBAR_MAGNIFICATION);
                }
            }
        }
    }

    /*
     * TODO: This method seems expensive; why not only rebuild them when
     * handleBarPoints is null, and set the latter to null whenever the selected
     * element changes?
     */
    public void rebuildHandleBarPoints(AbstractDrawableComponent comp) {
        if (comp == null) {
            return;
        }

        List<double[]> newHandleBarPoints = new ArrayList<>();
        for (Coordinate point : comp.getPoints()) {
            double[] pixelPoint = descriptor.worldToPixel(new double[] {
                    point.x, point.y });
            newHandleBarPoints.add(pixelPoint);
        }
        boolean isDifferent = false;
        int existingHandleBarPointsSize = handleBarPoints.size();
        int newHandleBarPointsSize = newHandleBarPoints.size();
        if (existingHandleBarPointsSize == newHandleBarPointsSize) {
            for (int i = 0; ((isDifferent == false) && (i < existingHandleBarPointsSize)); i++) {
                double[] existingHandleBarPointAry = handleBarPoints.get(i);
                double[] newHandleBarPointAry = newHandleBarPoints.get(i);
                int lenPointAry = newHandleBarPointAry.length;
                for (int j = 0; ((isDifferent == false) && (j < lenPointAry)); j++) {
                    if (existingHandleBarPointAry[j] != newHandleBarPointAry[j]) {
                        isDifferent = true;
                    }
                }
            }
        } else {
            isDifferent = true;
        }

        if (isDifferent == true) {
            handleBarPoints.clear();
            handleBarPoints.addAll(newHandleBarPoints);
        }
    }

    /**
     * Draw the hatched areas associated with the hazard events. This is done
     * using shaded shapes to increase redrawing performance during zoom and pan
     * operations.
     * 
     * @param target
     *            The target to draw on
     * @param paintProps
     *            Describes how drawables appear on the target.
     * @return
     */
    private void drawHatchedAreas(IGraphicsTarget target,
            PaintProperties paintProps) {
        /*
         * Draw shaded geometries...
         */
        if (hatchedAreas.size() > 0) {

            /*
             * Only recreate the hatched areas if they have changed.
             */
            if (redrawHatchedAreas) {
                redrawHatchedAreas = false;
                if (hatchedAreaShadedShape != null) {
                    hatchedAreaShadedShape.dispose();
                }

                hatchedAreaShadedShape = target.createShadedShape(false,
                        descriptor.getGridGeometry());
                JTSCompiler groupCompiler = new JTSCompiler(
                        hatchedAreaShadedShape, null, descriptor);

                try {
                    for (AbstractDrawableComponent hatchedArea : hatchedAreas) {
                        if (hatchedArea.getClass() == HazardServicesPolygon.class) {
                            HazardServicesPolygon hazardServicesPolygon = (HazardServicesPolygon) hatchedArea;
                            Color[] colors = hazardServicesPolygon.getColors();
                            JTSGeometryData data = groupCompiler
                                    .createGeometryData();
                            data.setGeometryColor(new RGB(colors[0].getRed(),
                                    colors[0].getGreen(), colors[0].getBlue()));
                            groupCompiler.handle(
                                    (Geometry) hazardServicesPolygon
                                            .getGeometry().clone(), data);
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

            try {
                target.drawShadedShape(hatchedAreaShadedShape,
                        paintProps.getAlpha());
            } catch (VizException e) {
                statusHandler.error("Error drawing hazard hatched areas", e);
            }

        }
    }

    /**
     * Initializes datatime frames.
     * 
     * @param startDataTime
     *            The start time to use.
     * @param numberOfDataTimes
     *            The number of data times to initialize behind the start time.
     * @return
     */
    private void fillDataTimeArray(DataTime startDataTime, int numberOfDataTimes) {
        int fifteenMin = 15 * 60 * 1000;
        long time = startDataTime.getRefTime().getTime();
        DataTime currentDataTime = null;

        for (int i = 0; i < numberOfDataTimes; i++) {
            time -= fifteenMin;
            currentDataTime = new DataTime(new Date(time));
            this.dataTimes.add(currentDataTime);
        }
    }

    /**
     * @param components
     *            over which the user has clicked.
     */
    private void setCurrentEvent(
            List<AbstractDrawableComponent> containingComponents) {
        ISessionEventManager<ObservedHazardEvent> eventManager = appBuilder
                .getSessionManager().getEventManager();
        eventManager.noCurrentEvent();
        if (containingComponents.size() == 1) {
            AbstractDrawableComponent component = containingComponents.get(0);
            if (component instanceof IHazardServicesShape) {
                /*
                 * TODO Ugly, yes but no uglier than the overall approach. This
                 * is why we need refactoring of the spatial display.
                 */
                eventManager.setCurrentEvent(((IHazardServicesShape) component)
                        .getID());
            }
        }
    }

    private void setCurrentEvent(int x, int y) {
        List<AbstractDrawableComponent> containingComponents = getContainingComponents(
                x, y);
        setCurrentEvent(containingComponents);

    }

    /**
     * Capture and process MultiPointResource (GageData) for Hazard Services in
     * a Hydro perspective.
     * <p>
     * The SpatialDisplay object (layer) is only visible when Hazard Services
     * has been activated.
     * 
     * This Action object is automatically added to the Context Menu (right
     * click menu) for Hazard Services.
     * 
     * <pre>
     */
    private class RiverGageFloodAction extends AbstractRightClickAction {

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.action.Action#getText()
         */
        @Override
        public String getText() {
            return "Create Hazard";
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.action.Action#isHidden()
         */
        @Override
        public boolean isHidden() {
            IWorkbenchWindow window = PlatformUI.getWorkbench()
                    .getActiveWorkbenchWindow();
            IWorkbenchPage page = window.getActivePage();
            IPerspectiveDescriptor perspectiveDescriptor = page
                    .getPerspective();
            if (perspectiveDescriptor != null) {
                String perspectiveId = perspectiveDescriptor.getId();
                if ((perspectiveId != null)
                        && (perspectiveId
                                .equals(HydroPerspectiveManager.HYDRO_PERSPECTIVE))) {
                    return (false);
                }
            }
            return (true);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.action.Action#run()
         */
        @Override
        public void run() {

            IWorkbenchWindow window = PlatformUI.getWorkbench()
                    .getActiveWorkbenchWindow();
            IWorkbenchPage page = window.getActivePage();
            IPerspectiveDescriptor perspectiveDescriptor = page
                    .getPerspective();
            if (perspectiveDescriptor != null) {
                String perspectiveId = perspectiveDescriptor.getId();
                if ((perspectiveId != null)
                        && (perspectiveId
                                .equals(HydroPerspectiveManager.HYDRO_PERSPECTIVE))) {
                    processHydroPerspectiveAction();
                }
            }
        }

        private void processHydroPerspectiveAction() {
            for (ResourcePair pair : descriptor.getResourceList()) {
                if (pair.getResource() instanceof MultiPointResource) {
                    MultiPointResource mpr = (MultiPointResource) pair
                            .getResource();
                    GageData gageData = mpr.getSelectedGage();
                    runRecommender(gageData);
                    return;
                }
            }
        }

        /**
         * Process a Right Mouse Button Click on a MultiPointResource (GageData)
         * object. Run the recommender (Tool) associated with the selected
         * hazard type.
         * <p>
         * The tool that is run is configured in StartUpConfig.py :
         * gagePointFirstRecommender property.
         * <p>
         * 
         * @param gageData
         *            River data from the Selected Gage
         */
        private void runRecommender(GageData gageData) {

            if (gageData == null) {
                return;
            }
            String riverGageLid = gageData.getLid();

            /*
             * NOTE: Taken from HazardTypeFirstPresenter.java TODO: Currently,
             * the business logic for running recommenders exist within the
             * HazardServicesMessageHandler. We do not have time to extract said
             * logic and place it in some more appropriate place, and thus will
             * use the deprecated publish() method to send a deprecated
             * notification to run the tool, which the message handler will
             * receive and deal with.
             * 
             * When the message handler is refactored into oblivion, there will
             * be either some sort of helper class for running recommenders, or
             * the session manager will handle it. At that time, this will be
             * changed to directly run the tool, instead of using this
             * deprecated code.
             */

            ISessionManager<ObservedHazardEvent, ObservedSettings> sessionManager = SpatialDisplay.this.appBuilder
                    .getSessionManager();

            ISessionConfigurationManager<ObservedSettings> configManager = sessionManager
                    .getConfigurationManager();
            StartUpConfig startupConfig = configManager.getStartUpConfig();
            String gagePointFirstRecommender = startupConfig
                    .getGagePointFirstRecommender();

            Map<String, Serializable> riverGageInfo = new HashMap<>();
            riverGageInfo.put("selectedPointID", riverGageLid);

            eventBus.publishAsync(new ToolAction(
                    ToolAction.RecommenderActionEnum.RUN_RECOMMENDER_WITH_PARAMETERS,
                    gagePointFirstRecommender, ToolType.RECOMMENDER,
                    riverGageInfo, RecommenderExecutionContext
                            .getEmptyContext()));
        }
    }

    /**
     * Check to see if the last action set the client map to a new scale factor.
     * 
     * <pre>
     * Setting the Map Scale Factor is part of the D2D display and not supported
     * by the Hydro perspective.<br>
     * This method invokes the same resizing methods in the drawing components
     * as a map zoom. This will cause the Spatial Display elements to resize
     * their Geometry coordinates to the correct pixels on the displayed maps.
     * 
     * @param paintProps Display Paint Properties.
     */
    private void checkForMapRescale(PaintProperties paintProps) {
        double newScaleFactor = 0.0d;
        double canvasX = paintProps.getCanvasBounds().width;
        double viewX = paintProps.getView().getExtent().getWidth();
        if (viewX != 0.0d) {
            newScaleFactor = canvasX / viewX;
        } else {
            newScaleFactor = -1.0d;
        }

        boolean isScaleChange = false;
        if (newScaleFactor != this.mapScaleFactor) {
            this.mapScaleFactor = newScaleFactor;
            isScaleChange = true;
        } else {
            // Only do this for D2D
            String newMapScale = null;
            PerspectiveSpecificProperties perspectiveProps = paintProps
                    .getPerspectiveProps();
            if (perspectiveProps != null) {
                if (perspectiveProps instanceof D2DProperties) {
                    D2DProperties d2dPerspectiveProps = (D2DProperties) perspectiveProps;
                    newMapScale = d2dPerspectiveProps.getScale();
                }
            }
            if (newMapScale == null) {
                newMapScale = DEFAULT_MAP_SCALE_NAME;
            }

            if (newMapScale.equals(mapScale) == false) {
                this.mapScale = newMapScale;
                isScaleChange = true;
            } else {
                this.zoomLevel = paintProps.getZoomLevel();
            }
        }

        if (isScaleChange == true) {
            float curZoomLevel = paintProps.getZoomLevel();
            if (this.zoomLevel == curZoomLevel) {
                paintProps
                        .setZoomLevel(curZoomLevel + this.iotaZoomLevelFactor);
            }
            this.redrawHatchedAreas = true;
        }
    }
}

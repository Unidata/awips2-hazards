/**
 * This software was developed and / or modified by the National Oceanic and
 * Atmospheric Administration (NOAA), Earth System Research Laboratory (ESRL),
 * Global Systems Division (GSD), Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.spatialdisplay;

import gov.noaa.gsd.common.visuals.SpatialEntity;
import gov.noaa.gsd.viz.hazards.display.HazardServicesAppBuilder;
import gov.noaa.gsd.viz.hazards.spatialdisplay.SpatialPresenter.SequencePosition;
import gov.noaa.gsd.viz.hazards.spatialdisplay.drawables.DrawableBuilder;
import gov.noaa.gsd.viz.hazards.spatialdisplay.drawables.IDrawable;
import gov.noaa.gsd.viz.hazards.spatialdisplay.drawables.MultiPointDrawable;
import gov.noaa.gsd.viz.hazards.spatialdisplay.drawables.SymbolDrawable;
import gov.noaa.gsd.viz.hazards.spatialdisplay.drawables.TextDrawable;
import gov.noaa.gsd.viz.hazards.spatialdisplay.entities.IEntityIdentifier;
import gov.noaa.gsd.viz.hazards.spatialdisplay.input.BaseInputHandler;
import gov.noaa.gsd.viz.hazards.spatialdisplay.input.InputHandlerFactory;
import gov.noaa.gsd.viz.hazards.spatialdisplay.selectbyarea.SelectByAreaDbMapResource;
import gov.noaa.gsd.viz.hazards.utilities.Utilities;
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
import gov.noaa.nws.ncep.ui.pgen.gfa.IGfa;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.IdentityHashMap;
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
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.GeometryType;
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
import com.raytheon.uf.viz.core.rsc.IResourceDataChanged;
import com.raytheon.uf.viz.core.rsc.LoadProperties;
import com.raytheon.uf.viz.core.rsc.RenderingOrderFactory;
import com.raytheon.uf.viz.core.rsc.RenderingOrderFactory.ResourceOrder;
import com.raytheon.uf.viz.core.rsc.tools.AbstractMovableToolLayer;
import com.raytheon.uf.viz.d2d.core.D2DProperties;
import com.raytheon.uf.viz.d2d.core.time.D2DTimeMatcher;
import com.raytheon.viz.core.rsc.jts.JTSCompiler;
import com.raytheon.viz.core.rsc.jts.JTSCompiler.JTSGeometryData;
import com.raytheon.viz.hydro.perspective.HydroPerspectiveManager;
import com.raytheon.viz.hydro.resource.MultiPointResource;
import com.raytheon.viz.hydrocommon.data.GageData;
import com.raytheon.viz.ui.EditorUtil;
import com.raytheon.viz.ui.VizWorkbenchManager;
import com.raytheon.viz.ui.cmenu.AbstractRightClickAction;
import com.raytheon.viz.ui.cmenu.IContextMenuContributor;
import com.raytheon.viz.ui.editor.AbstractEditor;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.operation.valid.IsValidOp;
import com.vividsolutions.jts.simplify.TopologyPreservingSimplifier;

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
 * Apr 05, 2016 15676      Chris.Golden Made finding all geometries "containing" a point more relaxed
 *                                      about what containment entails; there is now slop distance
 *                                      when looking for a point in a polygon, so that if the point
 *                                      lies slightly outside of the polygon, it still "contains" it.
 *                                      This makes editing polygons much easier.
 * Jun 23, 2016 19537      Chris.Golden Removed storm-track-specific code. Also added hatching for
 *                                      hazard events that have visual features, and added ability to
 *                                      use visual features to request spatial info for recommenders.
 * Jul 25, 2016 19537      Chris.Golden Extensively refactored as the move toward MVP compliance
 *                                      continues. Added Javadoc comments, continued separation of
 *                                      concerns between view, presenter, display, and mouse handlers.
 * </pre>
 * 
 * @author Xiangbao Jing
 */
public class SpatialDisplay extends
        AbstractMovableToolLayer<AbstractDrawableComponent> implements
        IContextMenuContributor, IResourceDataChanged {

    // Public Static Constants

    /**
     * Distance in pixels of "slop" used when testing for hits on clickable
     * elements, etc.
     */
    public static final int SLOP_DISTANCE_PIXELS = 15;

    // Public Enumerated Types

    /**
     * Types of cursors.
     */
    public static enum CursorTypes {

        MOVE_SHAPE_CURSOR(SWT.CURSOR_SIZEALL), MOVE_VERTEX_CURSOR(
                SWT.CURSOR_HAND), ARROW_CURSOR(SWT.CURSOR_ARROW), DRAW_CURSOR(
                SWT.CURSOR_CROSS), WAIT_CURSOR(SWT.CURSOR_WAIT);

        // Private Variables

        /**
         * SWT cursor type that goes with this cursor.
         */
        private final int swtType;

        // Private Constructors

        /**
         * Construct a standard instance.
         * 
         * @param swtType
         *            SWT type of cursor.
         */
        private CursorTypes(int swtType) {
            this.swtType = swtType;
        }

        // Public Methods

        /**
         * Get the SWT cursor type.
         * 
         * @return SWT cursor type.
         */
        public int getSwtType() {
            return swtType;
        }
    };

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

    /**
     * Action used to process the selection of {@link MultiPointResource} gage
     * data in a Hydro perspective.
     */
    private class RiverGageAction extends AbstractRightClickAction {

        // Public Methods

        @Override
        public String getText() {
            return "Create Hazard";
        }

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
                    return false;
                }
            }
            return (getSelectedGageIdentifier() == null);
        }

        @Override
        public void run() {
            spatialView
                    .handleUserInvocationOfGageAction(getSelectedGageIdentifier());
        }

        // Private Methods

        /**
         * Find the first multi-point resource in the descriptor's resource list
         * and get the selected gage identifier from it.
         * 
         * @return Selected gage identifier, or <code>null</code> if no
         *         multi-point resource is loaded, or if no gage is selected.
         */
        private String getSelectedGageIdentifier() {
            for (ResourcePair pair : descriptor.getResourceList()) {
                if (pair.getResource() instanceof MultiPointResource) {
                    MultiPointResource multiPointResource = (MultiPointResource) pair
                            .getResource();
                    GageData gageData = multiPointResource.getSelectedGage();
                    if (gageData != null) {
                        return gageData.getLid();
                    }
                }
            }
            return null;
        }
    }

    // Package Static Constants

    /**
     * Layer name.
     */
    static final String LAYER_NAME = "Hazard Services";

    // Private Static Constants

    /**
     * Color of the hover element's handle bars.
     */
    private static final org.eclipse.swt.graphics.Color HANDLE_BAR_COLOR = Display
            .getCurrent().getSystemColor(SWT.COLOR_GRAY);

    /**
     * Default map scale name. Does not match any valid scale name. This is by
     * design. This is necessary for adjusting to map scale (not zoom) changes
     * for the D2D Perspective.
     */
    private static final String DEFAULT_MAP_SCALE_NAME = "OTHER";

    /**
     * Pattern to be used for fills.
     */
    private static final String GL_PATTERN_VERTICAL_DOTTED = "VERTICAL_DOTTED";

    /**
     * Relative size of the handlebars drawn on selected hazards.
     */
    private static final float HANDLEBAR_MAGNIFICATION = 1.5f;

    /**
     * Small zoom level change factor to force redraw. The intent is for the
     * drawing methods to recognize a change, but have a change small enough not
     * to alter the rendered image.
     */
    private static final float IOTA_ZOOM_LEVEL_FACTOR_DELTA = 0.00001f;

    // Private Static Variables

    /**
     * Logging mechanism.
     */
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(SpatialDisplay.class);

    // Private Variables

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
     * Selected elements.
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
    private final DrawableBuilder drawableBuilder;

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
     * Input handler. The strategy pattern is used to control the input (mouse
     * and keyboard) behavior in this viz resource.
     */
    private BaseInputHandler inputHandler = null;

    /**
     * Spatial view that manages this object.
     */
    private SpatialView spatialView;

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
     * Map pairing drawable components with their associated spatial entities.
     * The mappings involve an N to 1 relationship where N is greater than 0.
     */
    private final Map<AbstractDrawableComponent, SpatialEntity<? extends IEntityIdentifier>> spatialEntitiesForDrawableComponents = new IdentityHashMap<>();

    /**
     * List of drawables representing hatched areas.
     */
    private final List<AbstractDrawableComponent> hatchedAreas;

    /**
     * Flag indicating whether or not the hatched areas should be redrawn.
     */
    private boolean renderHatchedAreas = false;

    /**
     * Shaded shape used in the actual rendering of the hatched areas.
     */
    private IShadedShape hatchedAreaShadedShape;

    /**
     * Input handler factory.
     */
    private final InputHandlerFactory inputHandlerFactory;

    /**
     * Current map scale factor, necessary for adjusting to map scale (not zoom)
     * changes.
     */
    double mapScaleFactor = 0.0;

    /**
     * Track Current Map Scale Name. This is necessary for adjusting to Map
     * Scale (not zoom) changes for D2D Perspective.
     */
    private String mapScale = DEFAULT_MAP_SCALE_NAME;

    /**
     * Current map zoom Level. Changing paint properties zoom level will force a
     * redraw of spatial display components.
     */
    private float zoomLevel = 1.0f;

    /**
     * Display properties.
     */
    private final DisplayProperties displayProperties;

    /**
     * Map of cursor types to the corresponding cursors.
     */
    private final Map<CursorTypes, Cursor> cursorsForCursorTypes = Maps
            .newEnumMap(CursorTypes.class);

    /**
     * River gage menu item.
     */
    private RiverGageAction riverGageAction;

    /**
     * Pixel X coordinate of location from which right-click context menu was
     * last deployed.
     */
    private int contextMenuX;

    /**
     * Pixel Y coordinate of location from which right-click context menu was
     * last deployed.
     */
    private int contextMenuY;

    // Public Methods

    /**
     * Construct a standard instance.
     * 
     * @param resourceData
     *            Resource data for the display.
     * @param loadProperties
     *            Properties describing this resource.
     */
    public SpatialDisplay(final SpatialDisplayResourceData resourceData,
            LoadProperties loadProperties) {

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
            // SimulatedTime.getSystemTime().setFrozen(true);
            SimulatedTime.getSystemTime().setFrozen(false);
            SimulatedTime.getSystemTime().setTime(simulatedDate.getTime());
        }

        /*
         * Create objects necessary for drawing and time-tracking.
         */
        displayMap = new ConcurrentHashMap<>();
        geometryFactory = new GeometryFactory();
        pgenLayerManager = new PgenLayerManager();
        displayProperties = buildDisplayProperties(pgenLayerManager
                .getActiveLayer());
        hatchedAreas = new ArrayList<>();
        drawableBuilder = new DrawableBuilder();
        dataTimes = new ArrayList<>();

        /*
         * Create the input handler factory.
         */
        inputHandlerFactory = new InputHandlerFactory(this);

        /*
         * This resource may be instantiated from within the UI thread, or from
         * another thread (for example, a non-UI thread is used for creating the
         * tool layer as part of a bundle load). Ensure that the rest of the
         * initialization happens on the UI thread. Note that if instantiated as
         * part of a bundle load, this instance will be created before the rest
         * of Hazard Services, whereas otherwise it is created
         */
        Runnable initializationFinisher = new Runnable() {
            @Override
            public void run() {

                /*
                 * Let the resource data finish its construction.
                 */
                resourceData.finishConstruction(SpatialDisplay.this);

                /*
                 * Create the cursors that will be used on the display.
                 */
                Display display = Display.getDefault();
                for (CursorTypes cursor : CursorTypes.values()) {
                    cursorsForCursorTypes.put(cursor,
                            display.getSystemCursor(cursor.getSwtType()));
                }

                /*
                 * Set the default input handler.
                 */
                setCurrentInputHandlerToNonDrawing(InputHandlerType.SINGLE_SELECTION);
            }
        };

        if (Display.getDefault().getThread() == Thread.currentThread()) {
            initializationFinisher.run();
        } else {
            Display.getDefault().asyncExec(initializationFinisher);
        }
    }

    // Public Methods

    @Override
    public String getName() {
        return LAYER_NAME;
    }

    @Override
    public void addContextMenuItems(IMenuManager menuManager, int x, int y) {

        /*
         * Remember the point at which the menu was deployed.
         */
        contextMenuX = x;
        contextMenuY = y;

        /*
         * Cancel any ongoing input mode operation.
         */
        spatialView.handleUserResetOfInputMode();

        /*
         * Get the spatial entity under the point.
         */
        IEntityIdentifier identifier = getSpatialEntityAtPoint(x, y);

        /*
         * Create the list of actions to be turned into menu items. Any null
         * that is added to the list is treated as a separator when making menu
         * items.
         */
        List<IAction> actions = new ArrayList<>();

        /*
         * Determine whether the point clicked is over a vertex of a multi-point
         * editable drawable, and if not, whether it is over a multi-point
         * editable drawable's border. If the former is true, deleting the
         * vertex is a possibility; if the latter, adding a vertex is possible.
         * In either of those cases, add a menu item.
         */
        MutableDrawableInfo info = getMutableDrawableInfoUnderPoint(x, y);
        if (info.getVertexIndex() != -1) {

            /*
             * Ensure there are at least three points left for paths, or five
             * points for polygons (since the latter need to have the last point
             * be identical to the first point, so they need four points at a
             * minimum). If there are, then add the delete vertex menu item.
             */
            if (info.getDrawable().getPoints().size() > (isPolygon(info
                    .getDrawable()) ? 4 : 2)) {
                actions.add(new Action(
                        HazardConstants.CONTEXT_MENU_DELETE_VERTEX) {
                    @Override
                    public void run() {
                        deleteVertex(contextMenuX, contextMenuY);
                    }
                });
            }
        } else if (info.isCloseToEdge()) {
            actions.add(new Action(HazardConstants.CONTEXT_MENU_ADD_VERTEX) {
                @Override
                public void run() {
                    addVertex(contextMenuX, contextMenuY);
                }
            });
        }

        /*
         * Get the rest of the context menu items.
         */
        actions.addAll(spatialView.getContextMenuActions(identifier));

        /*
         * Add the river gage action if appropriate.
         */
        if (riverGageAction == null) {
            riverGageAction = new RiverGageAction();
        }
        if (riverGageAction.isHidden() == false) {
            actions.add(riverGageAction);
        }
        actions.add(null);

        /*
         * For each action, create a menu item; null placeholders result in the
         * creation of separators.
         */
        for (IAction action : actions) {
            if (action == null) {
                menuManager.add(new Separator());
            } else {
                menuManager.add(action);
            }
        }
    }

    @Override
    public boolean handleMouseDown(int x, int y, int mouseButton) {
        if ((inputHandler != null) && isEditable()) {
            return inputHandler.handleMouseDown(x, y, mouseButton);
        }
        return false;
    }

    @Override
    public boolean handleMouseDownMove(int x, int y, int button) {
        if ((inputHandler != null) && isEditable()) {
            return inputHandler.handleMouseDownMove(x, y, button);
        }
        return false;
    }

    @Override
    public boolean handleMouseUp(int x, int y, int mouseButton) {
        if ((inputHandler != null) && isEditable()) {
            return inputHandler.handleMouseUp(x, y, mouseButton);
        }
        return false;
    }

    @Override
    public boolean handleMouseMove(int x, int y) {
        if ((inputHandler != null) && isEditable()) {
            return inputHandler.handleMouseMove(x, y);
        }
        return false;
    }

    @Override
    public boolean handleKeyDown(int key) {
        if ((inputHandler != null) && isEditable()) {
            return inputHandler.handleKeyDown(key);
        }
        return false;
    }

    @Override
    public boolean handleKeyUp(int key) {
        if ((inputHandler != null) && isEditable()) {
            return inputHandler.handleKeyUp(key);
        }
        return false;
    }

    @Override
    public boolean handleMouseEnter(Event event) {
        if ((inputHandler != null) && isEditable()) {
            return inputHandler.handleMouseEnter(event);
        }
        return handleMouseMove(event.x, event.y);
    }

    @Override
    public DataTime[] getDataTimes() {

        /*
         * Save off previous state
         */
        prevTimeMatchBasis = timeMatchBasis;

        /*
         * Determine if this resource is the time match basis.
         */
        AbstractTimeMatcher time = descriptor.getTimeMatcher();
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
             * Calculate more data times if the user has selected more frames
             * than there have been in the past.
             */
            if (descriptor.getNumberOfFrames() > frameCount) {
                int variance = descriptor.getNumberOfFrames() - frameCount;

                frameCount = descriptor.getNumberOfFrames();

                DataTime earliestTime = dataTimes.get(0);
                fillDataTimeArray(earliestTime, variance);
            }

            /*
             * Just switched Time Match Basis update frames.
             */
            if (prevTimeMatchBasis == false && timeMatchBasis == true) {
                dataTimes.clear();
                currentTime = new DataTime(SimulatedTime.getSystemTime()
                        .getTime());

                dataTimes.add(currentTime);
                fillDataTimeArray(currentTime,
                        descriptor.getNumberOfFrames() - 1);
            }
        } else {
            dataTimes.clear();

            /*
             * First time called.
             */
            if (info.getFrameTimes() != null) {
                for (DataTime dt : info.getFrameTimes()) {
                    dataTimes.add(dt);
                }
                descriptor.getTimeMatchingMap().put(this, info.getFrameTimes());
            }
            if (dataTimes.size() == 0) {

                /*
                 * Case where this tool is time match basis or no data loaded.
                 */
                if (dataTimes.size() > 0) {
                    currentTime = dataTimes.get(dataTimes.size() - 1);
                } else {
                    currentTime = new DataTime(SimulatedTime.getSystemTime()
                            .getTime());
                }

                dataTimes.add(currentTime);
                fillDataTimeArray(currentTime,
                        descriptor.getNumberOfFrames() - 1);
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
     * Draw the specified spatial entities.
     * 
     * @param spatialEntities
     *            Spatial entities to be drawn.
     * @param selectedSpatialEntityIdentifiers
     *            Identifiers of spatial entities that are currently selected.
     */
    public void drawSpatialEntities(
            List<SpatialEntity<? extends IEntityIdentifier>> spatialEntities,
            Set<IEntityIdentifier> selectedSpatialEntityIdentifiers) {

        /*
         * Clear any previously created drawables, as well as selected ones and
         * the hover element.
         */
        clearDrawables();
        selectedElements.clear();
        setHoverElement(null);

        /*
         * Note that hatched areas will need re-rendering, and clear them.
         */
        renderHatchedAreas = true;
        hatchedAreas.clear();

        /*
         * Iterate through the spatial entities, building any hatched areas they
         * require, as well as regular drawable components.
         */
        Layer activeLayer = getActiveLayer();
        for (SpatialEntity<? extends IEntityIdentifier> spatialEntity : spatialEntities) {
            drawableBuilder.buildHatchedAreas(this, spatialEntity, activeLayer);
            drawableBuilder.buildDrawableComponents(this, spatialEntity,
                    activeLayer, selectedSpatialEntityIdentifiers
                            .contains(spatialEntity.getIdentifier()));
        }

        /*
         * Get the list of drawables that have been created, and amalgamate any
         * text components that share coordinates and thus may be combined,
         * removing any text components that have thereby become superfluous.
         */
        List<AbstractDrawableComponent> drawables = new ArrayList<>(
                pgenLayerManager.getActiveLayer().getDrawables());
        for (AbstractDrawableComponent component : drawableBuilder
                .amalgamateTextComponents(drawables)) {
            removeElement(component);
        }

        /*
         * Let the superclass track the drawable objects.
         */
        setObjects(drawables);

        /*
         * Refresh the display.
         */
        issueRefresh();
    }

    /**
     * Remove the ghost drawable from the PGEN drawing layer.
     */
    public void removeGhostOfElementBeingEdited() {
        elementEditedGhost = null;
    }

    /**
     * Add the specified drawable to the PGEN layer, as well as associating it
     * with the specified spatial entity.
     * 
     * @param drawable
     *            Drawable to be added.
     * @param spatialEntity
     *            Spatial entity with which this element is associated.
     * @param selected
     *            Flag indicating whether or not the element is selected.
     */
    public void addElement(AbstractDrawableComponent drawable,
            SpatialEntity<? extends IEntityIdentifier> spatialEntity,
            boolean selected) {

        /*
         * Get the non-collection drawables from the provided drawable; this may
         * simply be the drawable itself if it is not a collection, or it may be
         * one or more sub-drawables (potentially nested).
         */
        List<AbstractDrawableComponent> components = new ArrayList<>();
        addNonCollectionDrawableElementsToList(drawable, components);

        /*
         * Iterate through the non-collection drawables, adding each one to the
         * active layer, associating it with the spatial entity that it
         * represents, and marking it as selected if appropriate.
         */
        for (AbstractDrawableComponent component : components) {
            pgenLayerManager.addElement(component);
            spatialEntitiesForDrawableComponents.put(component, spatialEntity);
            if (selected) {
                selectedElements.add(component);
            }
        }
    }

    /**
     * Add the specified hatched area.
     * 
     * @param hatchedArea
     *            Hatched area to be added.
     */
    public void addHatchedArea(AbstractDrawableComponent hatchedArea) {
        hatchedAreas.add(hatchedArea);
    }

    /**
     * Remove a drawable element from the PGEN layer, as well as any other
     * records of it.
     * 
     * @param drawable
     *            Drawable to be removed.
     */
    public void removeElement(AbstractDrawableComponent drawable) {

        /*
         * Get the non-collection drawables from the provided drawable; this may
         * simply be the drawable itself if it is not a collection, or it may be
         * one or more sub-drawables (potentially nested).
         */
        List<AbstractDrawableComponent> components = new ArrayList<>();
        addNonCollectionDrawableElementsToList(drawable, components);

        /*
         * Iterate through the non-collection drawables, removing each one from
         * the active layer, from the map associating it with the spatial entity
         * that it represented, and from the selection collection. Also remove
         * any container used in rendering it.
         */
        for (AbstractDrawableComponent component : components) {
            pgenLayerManager.removeElement(component);
            spatialEntitiesForDrawableComponents.remove(component);
            selectedElements.remove(component);
            AbstractElementContainer elementContainer = displayMap
                    .remove(drawable);
            if (elementContainer != null) {
                elementContainer.dispose();
            }
        }
    }

    /**
     * Get the spatial entity associated with the specified drawable component,
     * if any.
     * 
     * @param drawableComponent
     *            Drawable component.
     * @return Associated spatial entity, or <code>null</code> if none is
     *         associated.
     */
    public SpatialEntity<? extends IEntityIdentifier> getAssociatedSpatialEntity(
            AbstractDrawableComponent drawableComponent) {
        return spatialEntitiesForDrawableComponents.get(drawableComponent);
    }

    /**
     * Retrieve the active layer. A layer is a collection of drawable elements
     * that can be controlled as a group.
     * 
     * @return Active layer.
     */
    public Layer getActiveLayer() {
        return pgenLayerManager.getActiveLayer();
    }

    /**
     * Set the ghost of the element being edited to that specified.
     * 
     * @param ghost
     *            New ghost of the element being edited.
     */
    public void setGhostOfElementBeingEdited(AbstractDrawableComponent ghost) {
        this.elementEditedGhost = ghost;
    }

    /**
     * Handle an attempt by the user to select or deselect a drawable element on
     * the spatial display.
     * 
     * @param drawable
     *            Drawable that was clicked.
     * @param multipleSelection
     *            Indicates whether or not this is a part of a multiple
     *            selection action.
     */
    public void handleUserSingleDrawableSelection(
            AbstractDrawableComponent drawable, boolean multipleSelection) {
        if (drawable instanceof IDrawable) {
            spatialView.handleUserSingleSpatialEntitySelection(
                    ((IDrawable) drawable).getIdentifier(), multipleSelection);
        }
    }

    /**
     * Handle an attempt by the user to set the selection set to include the
     * specified drawables on the spatial display.
     * 
     * @param drawables
     *            Drawables that the user is attempting to make the selection
     *            set.
     */
    public void handleUserMultipleDrawablesSelection(
            Set<AbstractDrawableComponent> drawables) {
        Set<IEntityIdentifier> identifiers = new HashSet<>(drawables.size(),
                1.0f);
        for (AbstractDrawableComponent drawable : drawables) {
            if (drawable instanceof IDrawable) {
                identifiers.add(((IDrawable) drawable).getIdentifier());
            }
        }
        spatialView.handleUserMultipleSpatialEntitiesSelection(identifiers);
    }

    /**
     * Handle user modification of the geometry of the specified drawable.
     * 
     * @param drawable
     *            Drawable to be modified.
     * @param geometry
     *            New geometry to be used by the drawable.
     */
    public void handleUserModificationOfDrawable(
            AbstractDrawableComponent drawable, Geometry geometry) {
        issueRefresh();
        if (drawable instanceof IDrawable) {
            spatialView.handleUserModificationOfSpatialEntity(
                    ((IDrawable) drawable).getIdentifier(), geometry);
        }
    }

    /**
     * Handle the selection of a location on the display.
     * 
     * @param location
     *            Location selected.
     */
    public void handleUserSelectionOfLocation(Coordinate location) {
        spatialView.handleUserLocationSelection(location);
    }

    /**
     * Handle the user creation of a point shape.
     * 
     * @param location
     *            Location at which the point is to be created.
     * @param sequencePosition
     *            Position in the point-creation sequence that this creation
     *            occupies. If {@link SequencePosition#LAST}, the input mode
     *            will be reset as well.
     */
    public void handleUserCreationOfPointShape(Coordinate location,
            SequencePosition sequencePosition) {
        spatialView.handleUserCreationOfShape(geometryFactory
                .createPoint(location));

        /*
         * If this is the first point drawn in a sequence of points, tell the
         * view that until further notice, subsequent created shapes should be
         * added to the selected set. If it is the last point drawn in such a
         * sequence, reset the input mode, which will as part of its routine
         * tell the view that no more created events should be added to the
         * selected set.
         */
        if (sequencePosition == SequencePosition.FIRST) {
            spatialView.handleSetAddCreatedEventsToSelected(true);
        } else if (sequencePosition == SequencePosition.LAST) {
            spatialView.handleUserResetOfInputMode();
        }
    }

    /**
     * Handle the user ending a sequence of point shape creation.
     */
    public void handleUserEndSequenceOfPointShapeCreation() {
        spatialView.handleSetAddCreatedEventsToSelected(false);
    }

    /**
     * Handle the completion of a multi-point drawing action, creating a
     * multi-point shape.
     * 
     * @param shapeType
     *            Type of shape being drawn; must be either
     *            {@link GeometryType#LINE} or {@link GeometryType#POLYGON}.
     * @param points
     *            List of coordinates making up the shape.
     */
    public void handleUserMultiPointDrawingActionComplete(
            GeometryType shapeType, List<Coordinate> points) {
        if (spatialView.isDrawingOfNewShapeInProgress()) {
            handleUserDrawNewShapeCompletion(shapeType, points);
        } else {
            handleUserMultiVertexEditCompletion(points);
        }
        spatialView.handleUserResetOfInputMode();
        issueRefresh();
    }

    /**
     * Handle the completion of a select-by-area drawing action, creating a new
     * polygonal shape or modifying an existing shape.
     * 
     * @param identifier
     *            Identifier of the entity being edited; if <code>null</code>,
     *            no entity is being edited, and a new geometry is being
     *            created.
     * @param selectedGeometries
     *            Geometries selected during the select-by-area process; these
     *            may be combined to create the new geometry.
     */
    public void handleUserSelectByAreaDrawingActionComplete(
            IEntityIdentifier identifier, Set<Geometry> selectedGeometries) {
        spatialView.handleUserSelectByAreaDrawingActionComplete(identifier,
                selectedGeometries);
    }

    /**
     * Handle the user finishing with the use of an input mode, and having said
     * mode reset to default.
     */
    public void handleUserResetOfInputMode() {
        spatialView.handleUserResetOfInputMode();
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
     * Retrieve the drawable closest to the specified point.
     * 
     * @param point
     *            Point against which to test.
     * @return Nearest drawable component.
     */
    public AbstractDrawableComponent getNearestComponent(Coordinate point) {

        AbstractEditor editor = (AbstractEditor) VizWorkbenchManager
                .getInstance().getActiveEditor();
        double screenCoord[] = editor.translateInverseClick(point);

        /**
         * Note that the distance unit of the JTS distance function is central
         * angle degrees. This seems to match closely with degrees lat and lon.
         */
        double minDist = Double.MAX_VALUE;

        Iterator<AbstractDrawableComponent> iterator = pgenLayerManager
                .getActiveLayer().getComponentIterator();

        Point clickScreenPoint = geometryFactory.createPoint(new Coordinate(
                screenCoord[0], screenCoord[1]));

        AbstractDrawableComponent closestSymbol = null;

        while (iterator.hasNext()) {
            AbstractDrawableComponent comp = iterator.next();

            if (comp instanceof TextDrawable == false) {
                Geometry p = ((IDrawable) comp).getGeometry();

                if (p != null) {

                    /*
                     * Convert the polygon vertices into pixels
                     */
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

        if (minDist <= SLOP_DISTANCE_PIXELS) {
            return closestSymbol;
        }

        return null;
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
    public List<AbstractDrawableComponent> getContainingComponents(
            Coordinate point, int x, int y) {
        Iterator<AbstractDrawableComponent> iterator = pgenLayerManager
                .getActiveLayer().getComponentIterator();

        /*
         * Check each of the hazard polygons. Normally, there will not be too
         * many of these. However, this could be made more efficient by using a
         * tree and storing/resusing the geometries.
         */
        Point clickPoint = geometryFactory.createPoint(point);
        Geometry clickPointWithSlop = clickPoint
                .buffer(getTranslatedHitTestSlopDistance(point, x, y));

        List<AbstractDrawableComponent> containingSymbolsList = new ArrayList<>();

        while (iterator.hasNext()) {
            AbstractDrawableComponent comp = iterator.next();

            /*
             * Skip labels (PGEN text objects). These are not selectable for
             * now.
             */
            if (comp instanceof IDrawable) {
                Geometry p = ((IDrawable) comp).getGeometry();

                if (p != null) {
                    boolean contains = false;
                    if (p instanceof GeometryCollection) {
                        GeometryCollection geometryCollection = (GeometryCollection) p;
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
                        contains = clickPointWithSlop.intersects(p);
                    }
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
     * Determine whether or not the drawable is editable.
     * 
     * @param drawableComponent
     *            Drawable to be checked.
     * @return <code>true</code> if the drawable is editable, <code>false</code>
     *         otherwise.
     */
    public boolean isComponentEditable(
            AbstractDrawableComponent drawableComponent) {
        if (drawableComponent instanceof DECollection) {
            DECollection deCollection = (DECollection) drawableComponent;
            ((IDrawable) deCollection.getItemAt(0)).isEditable();
        }
        return ((IDrawable) drawableComponent).isEditable();
    }

    /**
     * Determine whether or not the drawable is movable.
     * 
     * @param drawableComponent
     *            Drawable to be checked.
     * @return <code>true</code> if the drawable is movable, <code>false</code>
     *         otherwise.
     */
    public boolean isComponentMovable(
            AbstractDrawableComponent drawableComponent) {
        if (drawableComponent instanceof DECollection) {
            DECollection deCollection = (DECollection) drawableComponent;
            return ((IDrawable) deCollection.getItemAt(0)).isMovable();
        }
        return ((IDrawable) drawableComponent).isMovable();
    }

    /**
     * Get the mutable drawable, and if editable and with a vertex nearby, the
     * index of said vertex under the specified point.
     * 
     * @param x
     *            Pixel X coordinate.
     * @param y
     *            Pixel Y coordinate.
     * @return Information including the drawable and vertex index if an
     *         editable drawable is under the point and said drawable has a
     *         vertex that lies under the point as well; drawable and
     *         <code>-1</code> for the index if a mutable (editable and/or
     *         movable) drawable lies under the point, with a <code>true</code>
     *         value for close-to-edge if the point is near the edge of the
     *         drawable and the drawable is editable; and an empty object if no
     *         mutable drawable lies under the point.
     */
    public MutableDrawableInfo getMutableDrawableInfoUnderPoint(int x, int y) {
        boolean closeToEdge = false;
        int vertexIndexUnderPoint = -1;
        AbstractDrawableComponent drawable = null;

        /*
         * Get the world coordinate location of the mouse cursor.
         */
        AbstractEditor editor = ((AbstractEditor) VizWorkbenchManager
                .getInstance().getActiveEditor());
        Coordinate location = editor.translateClick(x, y);
        if (location != null) {

            /*
             * Retrieve the currently selected shapes, and from it compile a set
             * of the identifiers that are currently selected and which are
             * editable.
             */
            Set<AbstractDrawableComponent> selectedDrawables = getSelectedElements();
            Set<IEntityIdentifier> selectedIdentifiers = new HashSet<>(
                    selectedDrawables.size(), 1.0f);
            for (AbstractDrawableComponent selectedDrawable : selectedDrawables) {
                if (isComponentEditable(selectedDrawable)
                        || isComponentMovable(selectedDrawable)) {
                    selectedIdentifiers.add(((IDrawable) selectedDrawable)
                            .getIdentifier());
                }
            }

            /*
             * First try to find a component that completely contains the click
             * point. There could be several of these.
             */
            List<AbstractDrawableComponent> containingDrawables = getContainingComponents(
                    location, x, y);
            for (AbstractDrawableComponent containingDrawable : containingDrawables) {
                if (selectedIdentifiers
                        .contains(((IDrawable) containingDrawable)
                                .getIdentifier())
                        && (isComponentEditable(containingDrawable) || isComponentMovable(containingDrawable))) {
                    drawable = containingDrawable;
                    break;
                }
            }

            /*
             * If no element has been found, try to find the closest element.
             */
            if (drawable == null) {
                AbstractDrawableComponent nearestDrawable = getNearestComponent(location);
                if ((nearestDrawable != null)
                        && selectedIdentifiers
                                .contains(((IDrawable) nearestDrawable)
                                        .getIdentifier())
                        && (isComponentEditable(nearestDrawable) || isComponentMovable(nearestDrawable))) {
                    drawable = nearestDrawable;
                }
            }

            /*
             * If an element has been found, see if it can be moved, or if a
             * vertex it contains can be moved, and proceed accordingly.
             */
            if (drawable != null) {

                /*
                 * If the drawable is editable, a vertex may be able to be
                 * moved.
                 */
                if (isComponentEditable(drawable)) {

                    /*
                     * Create a line string with screen coordinates, and
                     * determine the distance between the line string and the
                     * given point.
                     */
                    Coordinate[] shapeCoords = ((IDrawable) drawable)
                            .getGeometry().getCoordinates();
                    Coordinate[] shapeScreenCoords = new Coordinate[shapeCoords.length];
                    for (int i = 0; i < shapeCoords.length; ++i) {
                        double[] coords = editor
                                .translateInverseClick(shapeCoords[i]);
                        shapeScreenCoords[i] = new Coordinate(coords[0],
                                coords[1]);
                    }
                    LineString lineString = geometryFactory
                            .createLineString(shapeScreenCoords);
                    Coordinate mouseScreenCoord = new Coordinate(x, y);
                    Point mouseScreenPoint = geometryFactory
                            .createPoint(mouseScreenCoord);
                    double dist = mouseScreenPoint.distance(lineString);

                    /*
                     * If the distance is small enough, change the cursor to
                     * indicate drawing.
                     */
                    if (dist <= SLOP_DISTANCE_PIXELS) {
                        closeToEdge = true;
                    }

                    /*
                     * Determine whether or not the mouse position is close to
                     * one of the hazard's vertices. Do not check the last
                     * vertex if the shape is a polygon, since the first and
                     * last vertices are always the same for such shapes. If it
                     * is close enough, record the vertex, picking the one that
                     * is closest.
                     */
                    List<Coordinate> coordList = drawable.getPoints();
                    Coordinate coords[] = coordList
                            .toArray(new Coordinate[coordList.size()]);
                    double minDistance = Double.MAX_VALUE;
                    int upperLimit = coords.length
                            - (isPolygon(drawable) ? 1 : 0);
                    for (int j = 0; (j < upperLimit) && (coords[j] != null); ++j) {
                        double[] screen = editor
                                .translateInverseClick(coords[j]);
                        Coordinate vertexScreenCoord = new Coordinate(
                                screen[0], screen[1]);
                        dist = mouseScreenCoord.distance(vertexScreenCoord);
                        if (dist <= SLOP_DISTANCE_PIXELS) {
                            if (dist < minDistance) {
                                vertexIndexUnderPoint = j;
                                minDistance = dist;
                            }
                        }
                    }
                }
            }
        }
        return new MutableDrawableInfo(drawable, closeToEdge,
                vertexIndexUnderPoint);
    }

    /**
     * Determine whether the specified drawable is a polygon.
     * 
     * @param drawable
     *            Drawable to be checked.
     * @return <code>true</code> if the drawable is a polygon,
     *         <code>false</code> otherwise.
     */
    public boolean isPolygon(AbstractDrawableComponent drawable) {
        return ((drawable instanceof MultiPointDrawable) && ((MultiPointDrawable) drawable)
                .isClosedLine());
    }

    /**
     * Determine whether the specified drawable is a polygon.
     * 
     * @param drawable
     *            Drawable to be checked.
     * @return <code>true</code> if the drawable is a polygon,
     *         <code>false</code> otherwise.
     */
    public boolean isPolygon(IDrawable drawable) {
        return ((drawable instanceof MultiPointDrawable) && ((MultiPointDrawable) drawable)
                .isClosedLine());
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
     * @param drawable
     *            New hover element.
     */
    public void setHoverElement(AbstractDrawableComponent drawable) {
        hoverElement = drawable;

        /*
         * Update the list of world pixels associated with this element.
         */
        Coordinate[] pointsArray = null;
        if (drawable != null) {
            List<Coordinate> points = drawable.getPoints();
            pointsArray = points.toArray(new Coordinate[points.size()]);
        }
        useAsHandlebarPoints(pointsArray);
    }

    /**
     * Rebuild handlebar points from the specified points.
     * 
     * @param points
     *            Points from which to rebuild handlebar points.
     */
    public void useAsHandlebarPoints(Coordinate[] points) {
        handleBarPoints.clear();
        if (points != null) {
            for (Coordinate point : points) {
                double[] pixelPoint = descriptor.worldToPixel(new double[] {
                        point.x, point.y });
                handleBarPoints.add(pixelPoint);
            }
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
     * Set the mouse cursor to the specified type.
     * 
     * @param cursorType
     *            Type of cursor to set.
     */
    public void setCursor(CursorTypes cursorType) {
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getShell();
        shell.setCursor(cursorsForCursorTypes.get(cursorType));
    }

    // Protected Methods

    @Override
    protected void initInternal(IGraphicsTarget target) throws VizException {
        super.initInternal(target);

        /*
         * Initialize the line pattern manager here. This saves time with
         * drawing later. Note that this is safe to do in whatever random thread
         * the Job uses, since LinePatternManager.getInstance() is synchronized.
         */
        new Job("Loading PGEN LinePatternManager...") {

            @Override
            protected IStatus run(IProgressMonitor monitor) {
                LinePatternManager.getInstance();
                return Status.OK_STATUS;
            }

        }.schedule();
    }

    @Override
    protected void disposeInternal() {

        /*
         * If the perspective is not changing, notify the view that this display
         * was disposed of.
         */
        if (perspectiveChanging == false) {
            spatialView.handleSpatialDisplayClosed();
        }

        inputHandler = null;
        if (hatchedAreaShadedShape != null) {
            hatchedAreaShadedShape.dispose();
        }

        super.disposeInternal();
    }

    @Override
    protected void paint(IGraphicsTarget target, PaintProperties paintProps,
            AbstractDrawableComponent object,
            AbstractMovableToolLayer.SelectionStatus status)
            throws VizException {

        AbstractElementContainer container = displayMap.get(object);
        if (container == null) {
            if (object instanceof Symbol) {
                container = new DefaultRasterElementContainer(
                        (DrawableElement) object, descriptor, target);
            } else {
                container = new DefaultVectorElementContainer(
                        (DrawableElement) object, descriptor, target);
            }
            displayMap.put(object, container);
        }
        container.draw(target, paintProps, displayProperties);
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

        /*
         * Let the superclass handle painting all the standard (non-hatching,
         * non-ghost, non-hover) drawables.
         */
        super.paintInternal(target, paintProps);

        /*
         * Draw the hover elements.
         */
        drawHover(target, paintProps);
    }

    // Package-Private Methods

    /**
     * Set the current input handler to be the specified type of non-drawing
     * input handler.
     * 
     * @param type
     *            Type of input handler being requested; must be either
     *            {@link InputHandlerType#SINGLE_SELECTION},
     *            {@link InputHandlerType#FREEHAND_MULTI_SELECTION}, or
     *            {@link InputHandlerType#RECTANGLE_MULTI_SELECTION}.
     */
    void setCurrentInputHandlerToNonDrawing(InputHandlerType type) {
        if (inputHandler != null) {
            inputHandler.reset();
        }
        inputHandler = inputHandlerFactory.getNonDrawingInputHandler(type);
    }

    /**
     * Set the current input handler to be the specified type of drawing input
     * handler.
     * 
     * @param handlerType
     *            Type of input handler being requested.
     * @param geometryType
     *            Type of geometry that is to be drawn by the requested handler.
     */
    void setCurrentInputHandlerToDrawing(InputHandlerType handlerType,
            GeometryType geometryType) {
        if (inputHandler != null) {
            inputHandler.reset();
        }
        inputHandler = inputHandlerFactory.getDrawingInputHandler(handlerType,
                geometryType);
    }

    /**
     * Set the current input handler to be the select-by-area input handler.
     * 
     * @param vizResource
     *            Select-by-area viz resource to be used with this handler.
     * @param selectedGeometries
     *            Select-by-area geometries that should start off as selected.
     * @param identifier
     *            Identifier of the entity that is to be edited using
     *            select-by-area; if <code>null</code>, a new geometry is to be
     *            created.
     */
    void setCurrentInputHandlerToSelectByArea(
            SelectByAreaDbMapResource vizResource,
            Set<Geometry> selectedGeometries, IEntityIdentifier identifier) {
        if (inputHandler != null) {
            inputHandler.reset();
        }
        inputHandler = inputHandlerFactory.getSelectByAreaInputHandler(
                vizResource, selectedGeometries, identifier);
    }

    /**
     * Add a new vertex to the editable drawable at the specified point.
     * 
     * @param x
     *            Pixel X coordinate.
     * @param y
     *            Pixel Y coordinate.
     * @return Index of the vertex added, or <code>-1</code> if no vertex was
     *         added.
     */
    public int addVertex(int x, int y) {
        AbstractEditor editor = EditorUtil
                .getActiveEditorAs(AbstractEditor.class);
        AbstractDrawableComponent selectedDrawable = getHoverElement();
        if (selectedDrawable != null) {
            Coordinate pixelPoint = new Coordinate(x, y);

            /*
             * Try using the geometry associated with the drawable.
             */
            IDrawable shape = ((IDrawable) selectedDrawable);
            Coordinate[] coordinates = shape.getGeometry().getCoordinates();

            /*
             * For polygons in which the last coordinate is not equal to the
             * first coordinate, ensure that the line segment connecting the
             * last coordinate to the first is tested.
             */
            boolean checkLastToFirst = (isPolygon(selectedDrawable) && (coordinates[0]
                    .equals(coordinates[coordinates.length - 1]) == false));

            /*
             * Iterate through the line segments, in each case stretching from a
             * lower-indexed one to a higher-indexed one (except when the
             * lower-indexed one is the last one, in which case the line segment
             * to be checked runs between it and the 0th indexed point).
             */
            double minDistance = Double.MAX_VALUE;
            int minPosition = Integer.MIN_VALUE;
            Coordinate colinearCoordinate = null;
            for (int j = 1; j < coordinates.length + (checkLastToFirst ? 1 : 0); j++) {

                /*
                 * Get the first segment coordinate in pixels.
                 */
                double pixelCoordinates[] = editor
                        .translateInverseClick(coordinates[j - 1]);
                Coordinate pixelCoordinateA = new Coordinate(
                        pixelCoordinates[0], pixelCoordinates[1]);

                /*
                 * Get the second segment coordinate in pixels.
                 */
                if (j < coordinates.length) {
                    pixelCoordinates = editor
                            .translateInverseClick(coordinates[j]);
                } else {
                    pixelCoordinates = editor
                            .translateInverseClick(coordinates[0]);
                }
                Coordinate pixelCoordinateB = new Coordinate(
                        pixelCoordinates[0], pixelCoordinates[1]);

                /*
                 * Build the line segment, and get the distance from it to the
                 * point.
                 */
                LineSegment lineSegment = new LineSegment(pixelCoordinateA,
                        pixelCoordinateB);
                double distance = lineSegment.distance(pixelPoint);

                /*
                 * If the distance is less than the minimum required to count,
                 * and less than any previously remembered segment's distance,
                 * remember it, and calculate the point along the line segment
                 * that is closest to the mouse cursor.
                 */
                if (distance <= SpatialDisplay.SLOP_DISTANCE_PIXELS) {
                    if (distance < minDistance) {
                        minDistance = distance;
                        minPosition = j;
                        colinearCoordinate = lineSegment
                                .closestPoint(pixelPoint);
                    }
                }
            }

            /*
             * If a point along one of the line segments was found, add it as a
             * vertex.
             */
            if (colinearCoordinate != null) {
                Coordinate[] newCoordinates = new Coordinate[coordinates.length + 1];

                /*
                 * Add the coordinates before the new point.
                 */
                int k = 0;
                for (k = 0; k < minPosition; k++) {
                    newCoordinates[k] = coordinates[k];
                }

                /*
                 * Get the vertex being added and add it.
                 */
                newCoordinates[k] = editor.translateClick(colinearCoordinate.x,
                        colinearCoordinate.y);

                /*
                 * Add the coordinates after the new point.
                 */
                for (k += 1; k < newCoordinates.length; k++) {
                    newCoordinates[k] = coordinates[k - 1];
                }

                /*
                 * Close the points if they describe a a polygon, create the
                 * modified geometry, and use it.
                 */
                IDrawable entityShape = (IDrawable) selectedDrawable;
                List<Coordinate> coordsAsList = Lists
                        .newArrayList(newCoordinates);
                if (entityShape.getGeometry().getClass() == Polygon.class) {
                    Utilities.closeCoordinatesIfNecessary(coordsAsList);
                }
                Geometry modifiedGeometry = buildModifiedGeometry(entityShape,
                        coordsAsList);

                handleUserModificationOfDrawable(selectedDrawable,
                        modifiedGeometry);
                useAsHandlebarPoints(modifiedGeometry.getCoordinates());
                issueRefresh();
                return minPosition;
            }
        }
        return -1;
    }

    /**
     * Delete the vertex from the editable drawable at the specified point.
     * 
     * @param x
     *            Pixel X coordinate.
     * @param y
     *            Pixel Y coordinate.
     * @return <code>true</code> if a vertex was deleted, <code>false</code>
     *         otherwise.
     */
    public boolean deleteVertex(int x, int y) {

        /*
         * Get the editable drawable under the point, if any, and remove its
         * vertex.
         */
        MutableDrawableInfo info = this.getMutableDrawableInfoUnderPoint(x, y);
        if ((info.getDrawable() != null) && (info.getVertexIndex() >= 0)) {

            /*
             * Ensure there are at least three points left for paths, or five
             * points for polygons (since the latter need to have the last point
             * be identical to the first point, so they need four points at a
             * minimum).
             */
            List<Coordinate> coordinates = new ArrayList<>(info.getDrawable()
                    .getPoints());
            boolean isPolygon = isPolygon(info.getDrawable());
            if (coordinates.size() > (isPolygon ? 4 : 2)) {

                /*
                 * Remove the coordinate at the appropriate index. If the first
                 * coordinate was removed and the shape is a polygon, remove the
                 * last one as well, since the last is always a copy of the
                 * first, and then close the polygon, which will add a copy of
                 * the new first point at the end.
                 */
                coordinates.remove(info.getVertexIndex());
                if (isPolygon && (info.getVertexIndex() == 0)) {
                    coordinates.remove(coordinates.size() - 1);
                    Utilities.closeCoordinatesIfNecessary(coordinates);
                }

                /*
                 * Create the modified geometry and use it.
                 */
                IDrawable entityShape = (IDrawable) info.getDrawable();
                Geometry modifiedGeometry = buildModifiedGeometry(entityShape,
                        coordinates);
                if (checkGeometryValidity(modifiedGeometry)) {
                    handleUserModificationOfDrawable(info.getDrawable(),
                            modifiedGeometry);
                    useAsHandlebarPoints(modifiedGeometry.getCoordinates());
                    issueRefresh();
                    return true;
                } else {
                    issueRefresh();
                }
            }
        }
        return false;
    }

    /**
     * Build a modified geometry from the specified coordinates for the
     * specified original shape.
     * 
     * @param origShape
     *            Original shape to be modified.
     * @param coords
     *            List of coordinates specifying the new geometry's vertices.
     * @return Modified geometry.
     */
    public Geometry buildModifiedGeometry(IDrawable origShape,
            List<Coordinate> coords) {

        /*
         * Create the new geometry.
         */
        Geometry geometry = translateCoordinatesToGeometry(origShape, coords);

        /*
         * If the geometry being replaced is not the whole geometry of the
         * spatial entity, but instead just a sub-geometry within the entity's
         * geometry, create a new geometry collection with all the old
         * geometries as before for all other indices, and the new geometry at
         * its index.
         */
        int newIndex = origShape.getGeometryIndex();
        SpatialEntity<? extends IEntityIdentifier> spatialEntity = getAssociatedSpatialEntity((AbstractDrawableComponent) origShape);
        Geometry originalGeometry = spatialEntity.getGeometry();
        if ((newIndex == -1)
                && (originalGeometry instanceof GeometryCollection)) {
            newIndex = 0;
        }
        if (newIndex != -1) {
            Geometry[] newGeometries = new Geometry[originalGeometry
                    .getNumGeometries()];
            for (int j = 0; j < newGeometries.length; j++) {
                newGeometries[j] = (j == newIndex ? geometry : originalGeometry
                        .getGeometryN(j));
            }
            geometry = geometryFactory.createGeometryCollection(newGeometries);
        }
        return geometry;
    }

    /**
     * Translate the specified coordinates to a geometry of the same type as the
     * specified original shape.
     * 
     * @param originalShape
     *            Original shape, providing the type of geometry that the
     *            coordinates should form.
     * @param coordinates
     *            List of coordinates making up the new geometry.
     * @return Geometry.
     */
    public Geometry translateCoordinatesToGeometry(IDrawable originalShape,
            List<Coordinate> coordinates) {
        Coordinate[] coordinatesAsArray = coordinates
                .toArray(new Coordinate[coordinates.size()]);
        Geometry result;
        if (isPolygon(originalShape)) {
            result = geometryFactory.createPolygon(
                    geometryFactory.createLinearRing(coordinatesAsArray), null);
        } else if (originalShape.getClass() == MultiPointDrawable.class) {
            result = geometryFactory.createLineString(coordinatesAsArray);
        } else if (originalShape.getClass() == SymbolDrawable.class) {
            result = geometryFactory.createPoint(coordinatesAsArray[0]);
        } else {
            throw new IllegalArgumentException("Unexpected geometry "
                    + originalShape.getClass());
        }
        return result;
    }

    // Private Methods

    /**
     * Handle the completion of the drawing of the new shape by the user.
     * 
     * @param shapeType
     *            Type of shape the points make up.
     * @param points
     *            Points to be used to construct the shape.
     */
    private void handleUserDrawNewShapeCompletion(GeometryType shapeType,
            List<Coordinate> points) {

        /*
         * Do nothing if user hasn't drawn enough points to create a path or
         * polygon.
         */
        if (points.size() < (shapeType == GeometryType.LINE ? 2 : 3)) {
            return;
        }

        /*
         * Simplify the number of points in the path or polygon.
         * 
         * TODO: This may eventually need to be user-configurable.
         */
        Geometry newGeometry = null;
        if (shapeType == GeometryType.POLYGON) {
            Utilities.closeCoordinatesIfNecessary(points);
            LinearRing linearRing = geometryFactory.createLinearRing(points
                    .toArray(new Coordinate[points.size()]));
            Geometry polygon = geometryFactory.createPolygon(linearRing, null);
            newGeometry = TopologyPreservingSimplifier
                    .simplify(polygon, 0.0001);
        } else {
            LineString lineString = geometryFactory.createLineString(points
                    .toArray(new Coordinate[points.size()]));
            newGeometry = TopologyPreservingSimplifier.simplify(lineString,
                    0.0001);
        }

        /*
         * Create the entity for the path or polygon.
         */
        spatialView.handleUserCreationOfShape(newGeometry);
    }

    /**
     * Apply the specified path to the first selected polygon that is found as a
     * multi-vertex edit. Note that the direction in which the user draws the
     * replacement points matters. It is assumed they are drawing the
     * replacement points in the same direction as the original polygon. For
     * select-by-area and as recommended by the recommenders, this direction is
     * clockwise.
     * <p>
     * TODO: There are a number of limitations with this method's current
     * implementation. First, it does not allow paths (lines) to be modified,
     * only polygons. Second, it only modifies the first polygon found within
     * the geometry collection for the hazard event. And third, when attempting
     * to find out where the new path joins at each end with the polygon being
     * edited, it only finds the closest vertices in the polygon for each end of
     * the path, not the closest points along the line segments making up the
     * polygon. All these limitations should be corrected, or...
     * </p>
     * <p>
     * It might be better to dispense with this approach entirely, and instead
     * add a drop-down to the console toolbar allowing the choice of one of four
     * drawing modes:
     * </p>
     * <ul>
     * <li>New, meaning new hazard events are created with any lines, points, or
     * polygons that are drawn.</li>
     * <li>Add, meaning that any lines, points or polygons that are drawn are
     * added to the currently selected hazard event. The results are ORed with
     * the existing geometries.</li>
     * <li>Intersect, meaning that any lines, points or polygons that are drawn
     * are ANDed with the current geometry, thus resulting in a new geometry
     * that is an intersection of what was there before and the new geometry.
     * How would this work with points? With lines? Maybe points and lines are
     * not allowed to be drawn in this mode?</li>
     * <li>Remove, meaning that any lines, points and polygons that are drawn
     * are ORed with the current geometry, and the result is removed from the
     * current geometry, leaving only the parts of the old geometry that did not
     * intersect with the new shapes. Again, maybe don't allow points or lines
     * to be drawn in this mode?</li>
     * </ul>
     * <p>
     * If this is done, we could remove the "Add new geometries to selected"
     * button, as it would become superfluous. Also, of course, the last three
     * of the above choices would be unavailable when either zero, or two or
     * more, hazard events were selected.
     * </p>
     * 
     * @param points
     *            Latitude-longitude points making up the path to be applied.
     */
    private void handleUserMultiVertexEditCompletion(List<Coordinate> points) {
        MultiPointDrawable selectedPolygon = null;
        for (AbstractDrawableComponent component : selectedElements) {
            if ((component instanceof MultiPointDrawable)
                    && ((MultiPointDrawable) component).isClosedLine()) {
                selectedPolygon = (MultiPointDrawable) component;
                break;
            }
        }
        if (selectedPolygon == null) {
            return;
        }
        Geometry geometry = selectedPolygon.getGeometry();
        Polygon polygon = getFirstPolygon(geometry);
        if (polygon != null) {
            Coordinate[] origCoordinatesAsArray = polygon.getCoordinates();
            List<Coordinate> origCoordinates = new ArrayList<>(
                    Arrays.asList(origCoordinatesAsArray));
            Utilities.removeDuplicateLastPointAsNecessary(origCoordinates);
            int indexOfFirstPointToRemove = getIndexOfClosestPoint(
                    origCoordinates, points.get(0));
            int indexOfLastPointToRemove = getIndexOfClosestPoint(
                    origCoordinates, points.get(points.size() - 1));

            List<Coordinate> newCoordinates = new ArrayList<>();

            if (indexOfFirstPointToRemove <= indexOfLastPointToRemove) {
                for (int i = 0; i < indexOfFirstPointToRemove; i++) {
                    newCoordinates.add(origCoordinates.get(i));
                }
                for (int i = 0; i < points.size(); i++) {
                    newCoordinates.add(points.get(i));
                }

                for (int i = indexOfLastPointToRemove + 1; i < origCoordinates
                        .size(); i++) {
                    newCoordinates.add(origCoordinates.get(i));
                }
            } else {
                /*
                 * This deals with the case when the user chooses a section to
                 * replace that bounds the first point of the original polygon
                 * (i.e. the replacement section is crossing over an edge
                 * condition).
                 */
                for (int i = 0; i < points.size(); i++) {
                    newCoordinates.add(points.get(i));
                }
                for (int i = indexOfLastPointToRemove + 1; i < indexOfFirstPointToRemove; i++) {
                    newCoordinates.add(origCoordinates.get(i));
                }
            }

            /*
             * Only modify the geometry if the result is a polygon.
             */
            if (newCoordinates.size() >= 3) {
                Utilities.closeCoordinatesIfNecessary(newCoordinates);
                Geometry newGeometry = geometryFactory
                        .createPolygon(geometryFactory
                                .createLinearRing(newCoordinates
                                        .toArray(new Coordinate[newCoordinates
                                                .size()])), null);
                if (checkGeometryValidity(newGeometry)) {
                    handleUserModificationOfDrawable(selectedPolygon,
                            newGeometry);
                }
            }
        }
    }

    /**
     * Extract the first polygon from the specified geometry, meaning either the
     * geometry itself if it is a polygon, or the first polygon found when
     * iterating through the geometry if it is a collection.
     * 
     * @param geometry
     *            Geometry from which to get the first polygon.
     * @return First polygon within the specified geometry, or <code>null</code>
     *         if there is no polygon found.
     */
    private Polygon getFirstPolygon(Geometry geometry) {
        if (geometry instanceof Polygon) {
            return (Polygon) geometry;
        } else if (geometry instanceof GeometryCollection) {
            GeometryCollection geometryCollection = (GeometryCollection) geometry;
            for (int j = 0; j < geometryCollection.getNumGeometries(); j++) {
                Geometry subGeometry = geometryCollection.getGeometryN(j);
                if (subGeometry instanceof Polygon) {
                    return (Polygon) subGeometry;
                }
            }
        }
        return null;
    }

    /**
     * Add all non-collection drawable elements found within the specified
     * drawable element, potentially within nested sub-collections, if the
     * latter is a collection, or the drawable element itself if it is not, to
     * the specified list.
     * 
     * @param drawable
     *            Drawable element from which to get all non-collection
     *            elements.
     * @param list
     *            List to which to add the non-collection drawable elements.
     */
    private void addNonCollectionDrawableElementsToList(
            AbstractDrawableComponent drawable,
            List<AbstractDrawableComponent> list) {
        if (drawable instanceof DECollection) {
            Iterator<AbstractDrawableComponent> iterator = ((DECollection) drawable)
                    .getComponentIterator();
            while (iterator.hasNext()) {
                addNonCollectionDrawableElementsToList(iterator.next(), list);
            }
        } else {
            list.add(drawable);
        }
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
     * Get the index of the closest point out of the specified coordinates to
     * the given coordinate.
     * 
     * @param origCoordinates
     *            Coordinates in which to find the closest point.
     * @param coordinate
     *            Coordinate for which the closest point is to be found.
     * @return Index of the closest point in the specified coordinates.
     */
    private int getIndexOfClosestPoint(List<Coordinate> origCoordinates,
            Coordinate coordinate) {
        int result = 0;
        double minDistance = coordinate.distance(origCoordinates.get(0));
        for (int i = 1; i < origCoordinates.size(); i++) {
            double distance = coordinate.distance(origCoordinates.get(i));
            if (distance < minDistance) {
                result = i;
                minDistance = distance;
            }
        }
        return result;
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

        /*
         * Find the editor.
         */
        AbstractEditor editor = ((AbstractEditor) VizWorkbenchManager
                .getInstance().getActiveEditor());

        /*
         * Try creating a pixel point at the "slop" distance from the original
         * pixel point in each of the four cardinal directions; for each one,
         * see if this yields a translatable point, and if so, return the
         * distance between that point and the original geographic point. If
         * this fails, return 0.
         */
        Coordinate offsetLoc = null;
        for (int j = 0; j < 4; j++) {
            offsetLoc = editor
                    .translateClick(
                            x
                                    + (SLOP_DISTANCE_PIXELS * ((j % 2) * (j == 1 ? 1
                                            : -1))),
                            y
                                    + (SLOP_DISTANCE_PIXELS * (((j + 1) % 2) * (j == 0 ? 1
                                            : -1))));
            if (offsetLoc != null) {
                return Math.sqrt(Math.pow(loc.x - offsetLoc.x, 2.0)
                        + Math.pow(loc.y - offsetLoc.y, 2.0));
            }
        }
        return 0.0;
    }

    /**
     * Clear all drawables for visual features from the spatial display.
     */
    private void clearDrawables() {
        List<AbstractDrawableComponent> deList = pgenLayerManager
                .getActiveLayer().getDrawables();

        /*
         * Apparently need to use an array to prevent concurrency issues.
         * 
         * TODO: Are concurrency issues real? It would seem that this would
         * always be executed in the UI thread. Look into it.
         */
        AbstractDrawableComponent[] deArray = deList
                .toArray(new AbstractDrawableComponent[0]);

        for (AbstractDrawableComponent de : deArray) {
            if (de == null) {
                continue;
            }
            removeElement(de);
        }
    }

    /**
     * Draw the ghost of an entity that is being created or modified. For
     * instance, if an entity is moved, then the ghost of the entity is drawn to
     * show where the entity will end up when dropped on the map.
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
            AbstractElementContainer elementContainer = new DefaultVectorElementContainer(
                    element, descriptor, target);

            elementContainer.draw(target, paintProperties, displayProperties);
            elementContainer.dispose();
        }
    }

    /**
     * Get the display properties for the specified PGEN layer.
     */
    private DisplayProperties buildDisplayProperties(Layer layer) {
        DisplayProperties displayProperties = new DisplayProperties();
        displayProperties.setLayerMonoColor(layer.isMonoColor());
        displayProperties.setLayerColor(layer.getColor());
        displayProperties.setLayerFilled(layer.isFilled());
        return displayProperties;
    }

    /**
     * Get the components containing the specified pixel coordinate.
     * 
     * @param x
     *            Pixel X coordinate.
     * @param y
     *            Pixel Y coordinate.
     * @return List of components containing the specified point.
     */
    private List<AbstractDrawableComponent> getContainingComponents(int x, int y) {
        AbstractEditor editor = ((AbstractEditor) VizWorkbenchManager
                .getInstance().getActiveEditor());
        Coordinate location = editor.translateClick(x, y);
        return getContainingComponents(location, x, y);
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
    private void drawHover(IGraphicsTarget target,
            PaintProperties paintProperties) throws VizException {
        if (hoverElement != null) {
            if ((hoverElement instanceof IDrawable)
                    && ((IDrawable) hoverElement).isEditable()) {
                if (!handleBarPoints.isEmpty()) {
                    target.drawPoints(handleBarPoints,
                            HANDLE_BAR_COLOR.getRGB(), PointStyle.DISC,
                            HANDLEBAR_MAGNIFICATION);
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
     */
    private void drawHatchedAreas(IGraphicsTarget target,
            PaintProperties paintProperties) {
        /*
         * Draw shaded geometries.
         */
        if (hatchedAreas.size() > 0) {

            /*
             * Only recreate the hatched areas if they have changed.
             */
            if (renderHatchedAreas) {
                renderHatchedAreas = false;
                if (hatchedAreaShadedShape != null) {
                    hatchedAreaShadedShape.dispose();
                }

                if (hatchedAreas.isEmpty()) {
                    return;
                }
                hatchedAreaShadedShape = target.createShadedShape(false,
                        descriptor.getGridGeometry());
                JTSCompiler groupCompiler = new JTSCompiler(
                        hatchedAreaShadedShape, null, descriptor);

                try {
                    for (AbstractDrawableComponent hatchedArea : hatchedAreas) {
                        if ((hatchedArea instanceof MultiPointDrawable)
                                && ((MultiPointDrawable) hatchedArea)
                                        .isClosedLine()) {
                            MultiPointDrawable hazardServicesPolygon = (MultiPointDrawable) hatchedArea;
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
                        paintProperties.getAlpha());
            } catch (VizException e) {
                statusHandler.error("Error drawing hazard hatched areas", e);
            }

        }
    }

    /**
     * Initialize data time frames.
     * 
     * @param startDataTime
     *            The start time to use.
     * @param numberOfDataTimes
     *            The number of data times to initialize behind the start time.
     */
    private void fillDataTimeArray(DataTime startDataTime, int numberOfDataTimes) {
        int fifteenMin = 15 * 60 * 1000;
        long time = startDataTime.getRefTime().getTime();
        DataTime currentDataTime = null;

        for (int i = 0; i < numberOfDataTimes; i++) {
            time -= fifteenMin;
            currentDataTime = new DataTime(new Date(time));
            dataTimes.add(currentDataTime);
        }
    }

    /**
     * Get the identifier of the spatial entity under the specified point, if
     * only one is under the point.
     * 
     * @param x
     *            Pixel X coordinate.
     * @param y
     *            Pixel Y coordinate.
     * @return Identifier of the spatial entity if that is the only entity under
     *         the point, or <code>null</code> if less or more than one entity
     *         are under said point.
     */
    private IEntityIdentifier getSpatialEntityAtPoint(int x, int y) {
        List<AbstractDrawableComponent> containingComponents = getContainingComponents(
                x, y);
        if (containingComponents.size() == 1) {
            AbstractDrawableComponent component = containingComponents.get(0);
            if (component instanceof IDrawable) {
                return ((IDrawable) component).getIdentifier();
            }
        }
        return null;
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
        if (newScaleFactor != mapScaleFactor) {
            mapScaleFactor = newScaleFactor;
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
                mapScale = newMapScale;
                isScaleChange = true;
            } else {
                zoomLevel = paintProps.getZoomLevel();
            }
        }

        if (isScaleChange == true) {
            float curZoomLevel = paintProps.getZoomLevel();
            if (zoomLevel == curZoomLevel) {
                paintProps.setZoomLevel(curZoomLevel
                        + IOTA_ZOOM_LEVEL_FACTOR_DELTA);
            }
            renderHatchedAreas = true;
        }
    }
}

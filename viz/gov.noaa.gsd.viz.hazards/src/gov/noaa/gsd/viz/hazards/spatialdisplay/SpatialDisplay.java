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
import gov.noaa.nws.ncep.ui.pgen.display.AbstractElementContainer;
import gov.noaa.nws.ncep.ui.pgen.display.DefaultElementContainer;
import gov.noaa.nws.ncep.ui.pgen.display.DisplayProperties;
import gov.noaa.nws.ncep.ui.pgen.display.ElementContainerFactory;
import gov.noaa.nws.ncep.ui.pgen.display.LinePatternManager;
import gov.noaa.nws.ncep.ui.pgen.elements.AbstractDrawableComponent;
import gov.noaa.nws.ncep.ui.pgen.elements.DECollection;
import gov.noaa.nws.ncep.ui.pgen.elements.DrawableElement;
import gov.noaa.nws.ncep.ui.pgen.elements.Layer;
import gov.noaa.nws.ncep.ui.pgen.elements.Symbol;
import gov.noaa.nws.ncep.ui.pgen.elements.Text;

import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
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
import com.raytheon.uf.viz.core.drawables.FillPatterns;
import com.raytheon.uf.viz.core.drawables.IDescriptor.FramesInfo;
import com.raytheon.uf.viz.core.drawables.IShadedShape;
import com.raytheon.uf.viz.core.drawables.PaintProperties;
import com.raytheon.uf.viz.core.drawables.ResourcePair;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.rsc.IInputHandler;
import com.raytheon.uf.viz.core.rsc.IResourceDataChanged;
import com.raytheon.uf.viz.core.rsc.LoadProperties;
import com.raytheon.uf.viz.core.rsc.RenderingOrderFactory;
import com.raytheon.uf.viz.core.rsc.RenderingOrderFactory.ResourceOrder;
import com.raytheon.uf.viz.core.rsc.tools.AbstractMovableToolLayer;
import com.raytheon.uf.viz.d2d.core.time.D2DTimeMatcher;
import com.raytheon.uf.viz.hazards.sessionmanager.ISessionManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.ISessionConfigurationManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.ObservedSettings;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.ISettings;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.StartUpConfig;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Tool;
import com.raytheon.uf.viz.hazards.sessionmanager.events.ISessionEventManager;
import com.raytheon.uf.viz.hazards.sessionmanager.events.impl.ObservedHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.IOriginator;
import com.raytheon.viz.awipstools.IToolChangedListener;
import com.raytheon.viz.core.rsc.jts.JTSCompiler;
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
 * </pre>
 * 
 * @author Xiangbao Jing
 */
public class SpatialDisplay extends
        AbstractMovableToolLayer<AbstractDrawableComponent> implements
        IContextMenuContributor, IToolChangedListener, IResourceDataChanged,
        IOriginator {

    public static final String DEFAULT_NAME = "Hazard Services";

    /**
     * Logging mechanism.
     */
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(SpatialDisplay.class);

    private static final String GL_PATTERN_VERTICAL_DOTTED = "VERTICAL_DOTTED";

    private static final int HIT_TEST_SLOP_DISTANCE_PIXELS = (int) SelectionAction.SELECTION_DISTANCE_PIXELS;

    /**
     * Controls the relative size of the handlebars drawn on selected hazards.
     */
    private static final float HANDLEBAR_MAGNIFICATION = 1.0f;

    /**
     * A reference to an instance of app builder.
     */
    private HazardServicesAppBuilder appBuilder;

    /*
     * keep track of the previous zoom level for purposes of redrawing PGEN
     * symbols. Symbols are raster objects, so we only want to redraw them when
     * zooming.
     */
    private float previousZoomLevel = 0;

    /**
     * Not pulling in ElementCollectionFilter due to restriction on NCEP PGEN UI
     * package.
     */

    private final ConcurrentMap<AbstractDrawableComponent, AbstractElementContainer> displayMap;

    /**
     * Ghost for pgen element.
     */
    private AbstractDrawableComponent ghost = null;

    /**
     * Variable used to determine if a frame time change needs to be sent to the
     * IHIS Layer.
     */

    /*
     * Elements selected
     */
    private final List<AbstractDrawableComponent> elSelected;

    private AbstractDrawableComponent selectedHazardLayer = null;

    private final GeometryFactory geoFactory;

    // Flag indicating whether or not to draw the handlebars
    // on the selected element.
    private boolean drawSelectedHandleBars = false;

    // Builder for the various possible hazard geometries
    private HazardServicesDrawableBuilder drawableBuilder = null;

    // Data manager for displayed geometries.
    private SpatialDisplayDataManager dataManager = null;

    private boolean timeMatchBasis = false;

    private boolean prevTimeMatchBasis = false;

    private int maximumFrameCount = -1;

    // Mouse action handler. There are several different mouse modes
    // for hazard services. We need to use a strategy pattern to
    // control the mouse behavior in this tool layer.
    private IInputHandler mouseHandler = null;

    private boolean allowDisposeMessage = true;

    /*
     * The previous list of events drawn. Used for testing if a redraw is
     * necessary.
     */
    private Collection<ObservedHazardEvent> previousEvents;

    /*
     * reference to eventBus singleton instance
     */
    private BoundedReceptionEventBus<Object> eventBus = null;

    /**
     * Spatial view.
     */
    private SpatialView spatialView;

    /*
     * Color of the selection handlebars. Since PGEN uses AWT colors, there are
     * areas in this module which use the AWT Color class. Hence, I needed to
     * specify the full package here for the SWT Color class.
     */
    private org.eclipse.swt.graphics.Color handleBarColor;

    /**
     * Flag indicating whether or not the perspective is currently changing.
     */
    private boolean perspectiveChanging = false;

    /*
     * Contains the vertices of the currently selected hazard converted to world
     * pixels. This recomputed once per change in selected hazard for
     * efficiency.
     */
    private final List<double[]> handleBarPoints = new ArrayList<>();

    /*
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
            SimulatedTime.getSystemTime().setTime(simulatedDate.getTime());
        }

        displayMap = new ConcurrentHashMap<>();
        elSelected = new ArrayList<>();
        geoFactory = new GeometryFactory();

        dataManager = new SpatialDisplayDataManager();
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
                    ((SpatialDisplayResourceData) getResourceData())
                            .setSettings(SpatialDisplay.this.appBuilder
                                    .getCurrentSettings());
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
        return DEFAULT_NAME;
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

        /*
         * Draw the hazard event
         */
        drawProduct(target, paintProps, object);

        /*
         * Draw the selected polygon
         */
        if (selectedHazardLayer != null) {
            drawSelected(target, paintProps);
        }

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

        /*
         * Paint shaded shapes for hatched areas
         */
        drawHatchedAreas(target, paintProps);

        if (ghost != null) {
            drawGhost(target, paintProps);
        }

        super.paintInternal(target, paintProps);
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
            if (this.descriptor.getNumberOfFrames() > this.maximumFrameCount) {
                int variance = this.descriptor.getNumberOfFrames()
                        - this.maximumFrameCount;

                this.maximumFrameCount = this.descriptor.getNumberOfFrames();

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
        clearEvents();
        selectedHazardLayer = null;

        hatchedAreas.clear();
        hatchedAreaAnnotations.clear();

        for (ObservedHazardEvent hazardEvent : events) {

            /*
             * Keep an inventory of which events are selected.
             */
            String eventID = hazardEvent.getEventID();
            Boolean isSelected = (Boolean) hazardEvent
                    .getHazardAttribute(HAZARD_EVENT_SELECTED);

            drawableBuilder.buildDrawableComponents(this, hazardEvent,
                    eventOverlapSelectedTime.get(eventID), getActiveLayer(),
                    forModifyingStormTrack.get(eventID),
                    eventEditability.get(eventID), areHatchedAreasDisplayed);

            if (areHatchedAreasDisplayed && isSelected) {
                redrawHatchedAreas = true;
                drawableBuilder.buildhazardAreas(this, hazardEvent,
                        getActiveLayer(), hatchedAreas, hatchedAreaAnnotations);
            }

        }

        List<AbstractDrawableComponent> drawables = dataManager
                .getActiveLayer().getDrawables();

        hatchedAreaAnnotations.addAll(drawables);
        setObjects(hatchedAreaAnnotations);
        issueRefresh();
    }

    public void drawStormTrackDot() {

        AbstractDrawableComponent shapeComponent = drawableBuilder
                .buildStormTrackDotComponent(getActiveLayer());
        List<AbstractDrawableComponent> drawableComponents = Lists
                .newArrayList(shapeComponent);
        addElement(shapeComponent);
        drawableComponents.add(shapeComponent);
        drawableBuilder.addTextComponent(this, HazardConstants.DRAG_DROP_DOT,
                drawableComponents, shapeComponent);

        trackPersistentShapes(HazardConstants.DRAG_DROP_DOT, drawableComponents);
        setObjects(dataManager.getActiveLayer().getDrawables());
        issueRefresh();
    }

    /**
     * Removes the ghost line from the PGEN drawing layer.
     */
    public void removeGhostLine() {

        this.ghost = null;

    }

    /**
     * add a DrawableElement to the productList stored in the resource data
     * class.
     * 
     * @param de
     *            The DrawableElement being added.
     */
    public void addElement(AbstractDrawableComponent de) {
        dataManager.addElement(de);
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
        dataManager.removeElement(de);

        AbstractElementContainer elementContainer = displayMap.get(de);

        if (elementContainer != null) {
            elementContainer.dispose();
        }

        displayMap.remove(de);
    }

    /**
     * Retrieve the active layer. A layer is a collection of drawable elements
     * that can be controlled as a group.
     * 
     * @return Layer
     */
    public Layer getActiveLayer() {
        return dataManager.getActiveLayer();
    }

    /**
     * Sets the ghost line for the PGEN drawing layer.
     * 
     * @param ghost
     *            The ghost to display.
     */
    public void setGhostLine(AbstractDrawableComponent ghost) {
        this.ghost = ghost;
    }

    /**
     * Removes an event from the list of events contained in the spatial
     * display.
     * 
     * @param eventID
     *            The identifier of the event.
     */
    public void removeEvent(String eventID) {
        List<AbstractDrawableComponent> deList = dataManager.getActiveLayer()
                .getDrawables();

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
        getAppBuilder().getSpatialPresenter().updateSelectedEventIds(
                eventIDs.toArray(new String[eventIDs.size()]));
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

            List<AbstractDrawableComponent> deList = dataManager
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
     * Returns the selected element.
     * 
     * @return DrawableElement
     */
    public DrawableElement getSelectedDE() {

        if (elSelected.isEmpty()) {
            return null;
        } else {
            return elSelected.get(0).getPrimaryDE();
        }

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

        Iterator<AbstractDrawableComponent> iterator = dataManager
                .getActiveLayer().getComponentIterator();

        Point clickScreenPoint = geoFactory.createPoint(new Coordinate(
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
                        Point geometryPoint = geoFactory
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
        Iterator<AbstractDrawableComponent> iterator = dataManager
                .getActiveLayer().getComponentIterator();

        /**
         * 
         * Check each of the hazard polygons. Normally, there will not be too
         * many of these. However, we can make this more efficient by A using a
         * tree and storing/resusing the Geometries.
         * 
         */
        Point clickPoint = geoFactory.createPoint(point);
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
        Iterator<AbstractDrawableComponent> iterator = dataManager
                .getActiveLayer().getComponentIterator();

        /**
         * Check each of the hazard polygons. Normally, there will not be too
         * many of these. However, we can make this more efficient by using a
         * tree and storing/resusing the Geometries.
         */
        Point clickPoint = geoFactory.createPoint(point);
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
     * Replace one drawable element in the product list with another drawable
     * element.
     * 
     * @param old
     *            Element to replace
     * @param newde
     *            new drawable element
     */
    public void replaceElement(AbstractDrawableComponent old,
            AbstractDrawableComponent newde) {

        /*
         * displose of resources held by old componenet
         */
        // resetADC(old);

        dataManager.replaceElement(old, newde);
    }

    /**
     * Sets the selected element to the input element.
     * 
     * @param comp
     */
    public void setSelectedDE(AbstractDrawableComponent comp) {

        elSelected.clear();

        if (comp != null) {
            elSelected.add(comp);
        }

    }

    /**
     * TODO This needs to me moved elsewhere - nothing to do with drawing.
     */
    public void notifyModifiedGeometry(String eventID, Geometry geometry) {
        ISessionEventManager<ObservedHazardEvent> sessionEventManager = appBuilder
                .getSessionManager().getEventManager();

        ObservedHazardEvent hazardEvent = sessionEventManager
                .getEventById(eventID);

        if (sessionEventManager.isValidGeometryChange(geometry, hazardEvent)) {
            hazardEvent.setGeometry(geometry);
        }
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

        ISessionEventManager<ObservedHazardEvent> eventManager = sessionManager
                .getEventManager();
        List<IContributionItem> items = helper
                .getSelectedHazardManagementItems();
        if (eventManager.getEventsByStatus(HazardStatus.POTENTIAL).isEmpty() == false) {
            items.add(helper
                    .newAction(ContextMenuHelper.ContextMenuSelections.REMOVE_POTENTIAL_HAZARDS
                            .getValue()));
        }
        IAction action = helper.createMenu("Manage hazards",
                items.toArray(new IContributionItem[0]));

        if (action != null) {
            actions.add(action);
        }

        // This isn't the best way to determine this, but not sure what to do at
        // the moment.
        boolean drawCursor = spatialView
                .isCurrentCursor(SpatialViewCursorTypes.DRAW_CURSOR);
        boolean moveCursor = spatialView
                .isCurrentCursor(SpatialViewCursorTypes.MOVE_VERTEX_CURSOR);
        action = helper.createMenu(
                "Modify area...",
                helper.getSpatialHazardItems(drawCursor, moveCursor).toArray(
                        new IContributionItem[0]));
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
     * Sets the selected hazard.
     * 
     * @param comp
     *            The selected hazard.
     * @return
     */
    public void setSelectedHazardLayer(AbstractDrawableComponent comp) {

        selectedHazardLayer = comp;

        /*
         * Update the list of world pixels associated with this hazard. We only
         * need to do this computation once. This is especially useful for
         * drawing hazard selection handlebars.
         */
        handleBarPoints.clear();

        if (comp != null) {
            for (Coordinate point : comp.getPoints()) {
                double[] pixelPoint = descriptor.worldToPixel(new double[] {
                        point.x, point.y });
                handleBarPoints.add(pixelPoint);
            }
        }
    }

    /**
     * Retrieves the selected hazard.
     * 
     * @param
     * @return The selected hazard.
     */
    public AbstractDrawableComponent getSelectedHazardLayer() {
        return selectedHazardLayer;
    }

    /**
     * Sets flag indicating whether or not to draw handle bars.
     * 
     * @param drawSelectedHandleBars
     *            true - draw handle bars; false - do not draw handlebars.
     * @return
     */
    public void setDrawSelectedHandleBars(boolean drawSelectedHandleBars) {
        this.drawSelectedHandleBars = drawSelectedHandleBars;
        issueRefresh();
    }

    public double[] getSelectedHazardCenterPoint() {
        AbstractDrawableComponent comp = getSelectedHazardLayer();
        Point centerPoint = null;

        if ((comp != null) && !(comp instanceof Text)) {

            if (comp instanceof DECollection
                    && !((DECollection) comp).isEmpty()) {
                comp = ((DECollection) comp).getItemAt(0);
            }

            Geometry p = ((IHazardServicesShape) comp).getGeometry();

            if (p != null) {
                centerPoint = p.getCentroid();
            }
        }

        if (centerPoint != null) {
            return new double[] { centerPoint.getX(), centerPoint.getY() };
        }

        return null;

    }

    /**
     * @return the dataManager
     */
    public SpatialDisplayDataManager getDataManager() {
        return dataManager;
    }

    /**
     * @param mouseHandler
     *            the mouseHandler to set
     */
    public void setMouseHandler(IInputHandler mouseHandler) {
        this.mouseHandler = mouseHandler;
    }

    /**
     * @param allowDisposeMessage
     *            the allowDisposeMessage to set
     */
    public void setAllowDisposeMessage(boolean allowDisposeMessage) {
        this.allowDisposeMessage = allowDisposeMessage;
    }

    /**
     * @return the geoFactory
     */
    public GeometryFactory getGeoFactory() {
        return geoFactory;
    }

    /**
     * Sends a notification when the spatial display is disposed of, such as in
     * a clear action.
     * 
     * @param
     * @return
     */
    private void fireSpatialDisplayDisposedActionOccurred() {
        if (allowDisposeMessage) {
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
        Layer layer = dataManager.getActiveLayer();

        DisplayProperties dprops = new DisplayProperties();
        dprops.setLayerMonoColor(layer.isMonoColor());
        dprops.setLayerColor(layer.getColor());
        dprops.setLayerFilled(layer.isFilled());

        // Do this to force symbols to redraw themselves.
        // This ensures that they scale properly as
        // the user zooms in/out of the display.
        // Only do this if the user is zooming.
        if (el instanceof Symbol || el instanceof Text) {
            if ((paintProps.isZooming())
                    || (previousZoomLevel != paintProps.getZoomLevel())) {
                previousZoomLevel = paintProps.getZoomLevel();
                displayMap.remove(el);

                if (el instanceof HazardServicesText) {
                    ((HazardServicesText) el).updatePosition();
                }
            }
        }

        if (!displayMap.containsKey(el)) {
            AbstractElementContainer container = ElementContainerFactory
                    .createContainer((DrawableElement) el, descriptor, target);
            displayMap.put(el, container);
        }

        displayMap.get(el).draw(target, paintProps, dprops);
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
     * Clear all events from the spatial display. Takes into account events that
     * need to be persisted such as a storm track dot.
     */
    private void clearEvents() {
        List<AbstractDrawableComponent> deList = dataManager.getActiveLayer()
                .getDrawables();

        // Needed to use an array to prevent concurrency issues.
        AbstractDrawableComponent[] deArray = deList
                .toArray(new AbstractDrawableComponent[100]);

        for (AbstractDrawableComponent de : deArray) {
            if (de == null) {
                break;
            }

            String eventID = ((IHazardServicesShape) de).getID();
            List<AbstractDrawableComponent> persistentDrawables = persistentShapeMap
                    .get(eventID);

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
     * instance, if an event is moved, then ghost of the event is drawn to show
     * where the event will end up when dropped on the map.
     * 
     * @param target
     *            The target which will receive drawables
     * @param paintProps
     *            describes how drawables will be displayed
     * @return
     */
    private void drawGhost(IGraphicsTarget target, PaintProperties paintProps) {

        Iterator<DrawableElement> iterator = ghost.createDEIterator();

        while (iterator.hasNext()) {
            drawElement(target, paintProps, iterator.next());
        }
    }

    /**
     * Used by the drawGhost method. Ghosts are transient, so they are not
     * preserved once they are drawn.
     * 
     * @param target
     *            The target to draw on.
     * @param paintProps
     *            Describes how elements are displayed
     * @param el
     *            The PGEN drawable to be displayed.
     * @return
     */
    private void drawElement(IGraphicsTarget target,
            PaintProperties paintProps, DrawableElement el) {

        AbstractElementContainer dispEl = null;

        dispEl = new DefaultElementContainer(el, descriptor, target);
        dispEl.draw(target, paintProps, null);
        dispEl.dispose();
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
    private void drawSelected(IGraphicsTarget target, PaintProperties paintProps)
            throws VizException {

        if ((selectedHazardLayer != null) && (drawSelectedHandleBars)) {
            if ((selectedHazardLayer instanceof IHazardServicesShape)
                    && ((IHazardServicesShape) selectedHazardLayer)
                            .isEditable()) {

                if (!handleBarPoints.isEmpty()) {

                    target.drawPoints(handleBarPoints, handleBarColor.getRGB(),
                            PointStyle.DISC, HANDLEBAR_MAGNIFICATION);
                }
            }
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
                        descriptor.getGridGeometry(), true);
                JTSCompiler groupCompiler = new JTSCompiler(
                        hatchedAreaShadedShape, null, descriptor);

                try {
                    for (AbstractDrawableComponent hatchedArea : hatchedAreas) {
                        if (hatchedArea.getClass() == HazardServicesPolygon.class) {
                            HazardServicesPolygon hazardServicesPolygon = (HazardServicesPolygon) hatchedArea;
                            Color[] colors = hazardServicesPolygon.getColors();
                            Color fillColor = colors[0];
                            groupCompiler.handle(
                                    (Geometry) hazardServicesPolygon
                                            .getGeometry().clone(),
                                    new RGB(fillColor.getRed(), fillColor
                                            .getGreen(), fillColor.getBlue()));
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

            ObservedSettings settings = configManager.getSettings();
            Tool tool = settings.getTool(gagePointFirstRecommender);

            if (tool != null) {
                Map<String, Serializable> riverGageInfo = new HashMap<>();
                riverGageInfo.put("selectedPointID", riverGageLid);

                eventBus.publishAsync(new ToolAction(
                        ToolAction.RecommenderActionEnum.RUN_RECOMMENDER_WITH_PARAMETERS,
                        tool, riverGageInfo, ""));
            } else {
                statusHandler
                        .warn("No Hazard Generation Tool associated with configured value "
                                + gagePointFirstRecommender
                                + " Discarding request.\n Check value set in StartUpConfig.py script.");
            }
        }
    }
}

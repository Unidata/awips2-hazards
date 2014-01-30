/**
 * This software was developed and / or modified by the National Oceanic and
 * Atmospheric Administration (NOAA), Earth System Research Laboratory (ESRL),
 * Global Systems Division (GSD), Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.spatialdisplay;

import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.*;
import gov.noaa.gsd.common.utilities.JSONConverter;
import gov.noaa.gsd.viz.hazards.display.HazardServicesAppBuilder;
import gov.noaa.gsd.viz.hazards.display.action.ModifyStormTrackAction;
import gov.noaa.gsd.viz.hazards.display.action.SpatialDisplayAction;
import gov.noaa.gsd.viz.hazards.jsonutilities.Dict;
import gov.noaa.gsd.viz.hazards.jsonutilities.JSONUtilities;
import gov.noaa.gsd.viz.hazards.spatialdisplay.SpatialView.SpatialViewCursorTypes;
import gov.noaa.gsd.viz.hazards.spatialdisplay.drawableelements.HazardServicesDrawableBuilder;
import gov.noaa.gsd.viz.hazards.spatialdisplay.drawableelements.HazardServicesLine;
import gov.noaa.gsd.viz.hazards.spatialdisplay.drawableelements.HazardServicesSymbol;
import gov.noaa.gsd.viz.hazards.spatialdisplay.drawableelements.HazardServicesText;
import gov.noaa.gsd.viz.hazards.spatialdisplay.drawableelements.IHazardServicesShape;
import gov.noaa.gsd.viz.hazards.spatialdisplay.mousehandlers.SelectionAction;
import gov.noaa.gsd.viz.hazards.utilities.Utilities;
import gov.noaa.nws.ncep.ui.pgen.display.AbstractElementContainer;
import gov.noaa.nws.ncep.ui.pgen.display.DefaultElementContainer;
import gov.noaa.nws.ncep.ui.pgen.display.DisplayElementFactory;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.eventbus.EventBus;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardState;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.DataTime;
import com.raytheon.uf.common.time.SimulatedTime;
import com.raytheon.uf.common.time.TimeRange;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.viz.core.IDisplayPaneContainer;
import com.raytheon.uf.viz.core.IGraphicsTarget;
import com.raytheon.uf.viz.core.IGraphicsTarget.PointStyle;
import com.raytheon.uf.viz.core.PixelExtent;
import com.raytheon.uf.viz.core.VizApp;
import com.raytheon.uf.viz.core.drawables.IDescriptor.FramesInfo;
import com.raytheon.uf.viz.core.drawables.PaintProperties;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.rsc.IInputHandler;
import com.raytheon.uf.viz.core.rsc.IResourceDataChanged;
import com.raytheon.uf.viz.core.rsc.LoadProperties;
import com.raytheon.uf.viz.core.rsc.RenderingOrderFactory;
import com.raytheon.uf.viz.core.rsc.RenderingOrderFactory.ResourceOrder;
import com.raytheon.uf.viz.core.rsc.tools.AbstractMovableToolLayer;
import com.raytheon.uf.viz.hazards.sessionmanager.ISessionManager;
import com.raytheon.uf.viz.hazards.sessionmanager.events.ISessionEventManager;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventGeometryModified;
import com.raytheon.uf.viz.hazards.sessionmanager.modifiable.IModifiable;
import com.raytheon.uf.viz.hazards.sessionmanager.time.ISessionTimeManager;
import com.raytheon.viz.awipstools.IToolChangedListener;
import com.raytheon.viz.ui.VizWorkbenchManager;
import com.raytheon.viz.ui.cmenu.IContextMenuContributor;
import com.raytheon.viz.ui.editor.AbstractEditor;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygonal;
import com.vividsolutions.jts.operation.valid.IsValidOp;

/**
 * This is the AbstractVizResource used for the display of IHIS hazards. This
 * resource should remain loaded in CAVE for the run duration of IHIS. This
 * resource interfaces with NCEP PGEN code for creating the drawables.
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
 * </pre>
 * 
 * @author Xiangbao Jing
 */
public class ToolLayer extends
        AbstractMovableToolLayer<AbstractDrawableComponent> implements
        IContextMenuContributor, IToolChangedListener, IResourceDataChanged {

    /**
     * Logging mechanism.
     */
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(ToolLayer.class);

    public static final String DEFAULT_NAME = "Hazard Services";

    public static final double VERTEX_CIRCLE_RADIUS = 2;

    private static final int HIT_TEST_SLOP_DISTANCE_PIXELS = (int) SelectionAction.SELECTION_DISTANCE_PIXELS;

    /**
     * Controls the relative size of the handlebars drawn on selected hazards.
     */
    public static final float HANDLEBAR_MAGNIFICATION = 1.0f;

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

    private AbstractDrawableComponent selectedHazardIHISLayer = null;

    private final GeometryFactory geoFactory;

    // Flag indicating whether or not to draw the handlebars
    // on the selected element.
    private boolean drawSelectedHandleBars = false;

    // Builder for the various possible hazard geometries
    private HazardServicesDrawableBuilder drawableBuilder = null;

    // Data manager for displayed geometries.
    private ToolLayerDataManager dataManager = null;

    private boolean timeMatchBasis = false;

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
    private Collection<IHazardEvent> previousEvents;

    /*
     * reference to eventBus singleton instance
     */
    private EventBus eventBus = null;

    /**
     * Spatial view.
     */
    private SpatialView spatialView;

    /*
     * List of the currently selected events. This is needed for the case of
     * "multiple-deselection".
     */
    private List<String> selectedEventIDs = null;

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
    private final ArrayList<double[]> handleBarPoints = Lists.newArrayList();

    /*
     * Contains a map in which each entry contains a unique identifier and a
     * list of shapes. These are not events, and they are persisted across zoom,
     * pan and time change operations. It is up to the client to remove them
     * when they should not be displayed anymore.
     */
    private final Map<String, List<AbstractDrawableComponent>> persistentShapeMap;

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
    public ToolLayer(ToolLayerResourceData resourceData,
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

        displayMap = Maps.newConcurrentMap();
        elSelected = Lists.newArrayList();
        geoFactory = new GeometryFactory();

        dataManager = new ToolLayerDataManager();
        persistentShapeMap = Maps.newHashMap();

        dataTimes = Lists.newArrayList();
        selectedEventIDs = Lists.newArrayList();

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
                    ToolLayer.this.appBuilder = appBuilder;
                    drawableBuilder = new HazardServicesDrawableBuilder(
                            ToolLayer.this.appBuilder.getSessionManager());
                } else {
                    ToolLayer.this.appBuilder = new HazardServicesAppBuilder(
                            ToolLayer.this);
                    drawableBuilder = new HazardServicesDrawableBuilder(
                            ToolLayer.this.appBuilder.getSessionManager());
                    ToolLayer.this.appBuilder.buildGUIs(loadedFromBundle);
                }

                eventBus = ToolLayer.this.appBuilder.getEventBus();

                // If the resource data has a setting to be used, use
                // that; otherwise, give the resource data the setting
                // already in use by the app builder so that it will
                // have it in case it is saved as part of a bundle.
                String setting = ((ToolLayerResourceData) getResourceData())
                        .getSetting();
                if (setting != null) {
                    ToolLayer.this.appBuilder.setCurrentSettings(JSONUtilities
                            .settingsFromJSON(setting));
                } else {
                    ((ToolLayerResourceData) getResourceData())
                            .setSetting(new JSONConverter()
                                    .toJson(ToolLayer.this.appBuilder
                                            .getCurrentSettings()));
                }
            }
        };

        if (Display.getDefault().getThread() == Thread.currentThread()) {
            initializationFinisher.run();
        } else {
            Display.getDefault().asyncExec(initializationFinisher);
        }
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
     * Clear all events from the spatial display. Takes into account events that
     * need to be persisted such as a storm track dot.
     */
    public void clearEvents() {
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

    /**
     * Draws event geometries on the Hazard Services Tool Layer.
     * 
     * @param clearAllEvents
     *            Clear all displayed events before rendering the new events.
     * @return
     */
    public void drawEventAreas(boolean clearAllEvents,
            boolean toggleAutoHazardChecking, boolean areHatchedAreasDisplayed) {

        List<AbstractDrawableComponent> hatchedAreas = Lists.newArrayList();

        /**
         * TODO For reasons that are not clear to Chris Golden and Dan Schaffer,
         * this method is called for the old {@link ToolLayer} when you switch
         * perspectives and create a new {@link ToolLayer}. But part of the
         * changing perspective process is to nullify the appBuilder so we have
         * to do a check here. It would be good to take some time to understand
         * why this method is called in the old one when you switch
         * perspectives.
         */
        if (appBuilder == null) {
            return;
        }

        ISessionManager sessionManager = appBuilder.getSessionManager();
        ISessionEventManager eventManager = sessionManager.getEventManager();
        Collection<IHazardEvent> events = eventManager.getCheckedEvents();
        ISessionTimeManager timeManager = sessionManager.getTimeManager();
        TimeRange selectedRange = timeManager.getSelectedTimeRange();
        Date selectedTime = timeManager.getSelectedTime();
        filterEventsForTime(events, selectedRange, selectedTime);

        if (previousEvents != null) {
            if (!areEventsChanged(events)) {
                /*
                 * This list of events is identical to the previously drawn
                 * list. Do nothing more.
                 */
                return;
            }
        }

        if (clearAllEvents) {
            clearEvents();
        }

        previousEvents = events;
        selectedHazardIHISLayer = null;

        selectedEventIDs.clear();

        for (IHazardEvent hazardEvent : events) {

            /*
             * Keep an inventory of which events are selected.
             */
            String eventID = hazardEvent.getEventID();

            Boolean isSelected = (Boolean) hazardEvent
                    .getHazardAttribute(ISessionEventManager.ATTR_SELECTED);

            if (isSelected != null && isSelected
                    && !selectedEventIDs.contains(eventID)) {

                /*
                 * Since there can be multiple polygons per event, represent
                 * each event only once.
                 */
                selectedEventIDs.add(eventID);
            }

            boolean isEventAreaEditable = eventManager
                    .canEventAreaBeChanged(hazardEvent);

            List<AbstractDrawableComponent> drawables = drawableBuilder
                    .buildDrawableComponents(this, hazardEvent,
                            getActiveLayer(), isEventAreaEditable,
                            areHatchedAreasDisplayed);

            if (areHatchedAreasDisplayed && isSelected) {
                hatchedAreas.addAll(drawableBuilder.buildhazardAreas(this,
                        hazardEvent, getActiveLayer(), isSelected
                                && areHatchedAreasDisplayed));
            }

            Boolean isPersistent = (Boolean) hazardEvent
                    .getHazardAttribute(HazardConstants.PERSISTENT_SHAPE);
            trackPersistentShapes(isPersistent, hazardEvent.getEventID(),
                    drawables);
        }

        List<AbstractDrawableComponent> drawables = dataManager
                .getActiveLayer().getDrawables();
        hatchedAreas.addAll(drawables);
        setObjects(hatchedAreas);
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

        trackPersistentShapes(true, HazardConstants.DRAG_DROP_DOT,
                drawableComponents);
        setObjects(dataManager.getActiveLayer().getDrawables());
        issueRefresh();
    }

    private void trackPersistentShapes(Boolean isPersistent, String id,
            List<AbstractDrawableComponent> drawables) {
        /*
         * This ensures that a persistent shape with the same id as one that
         * already exists will override it.
         */

        if (isPersistent != null && isPersistent) {
            if (persistentShapeMap.containsKey(id)) {
                persistentShapeMap.remove(id);
            }

            if (!drawables.isEmpty()) {
                persistentShapeMap.put(id, drawables);
            }
        }
    }

    private boolean areEventsChanged(Collection<IHazardEvent> events) {
        for (IHazardEvent hazardEvent : events) {
            for (IHazardEvent previousEvent : previousEvents) {
                if (hazardEvent.getEventID().equals(previousEvent.getEventID())) {
                    if (!hazardEvent.equals(previousEvent)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private void filterEventsForTime(Collection<IHazardEvent> events,
            TimeRange selectedRange, Date selectedTime) {
        Iterator<IHazardEvent> it = events.iterator();
        while (it.hasNext()) {
            IHazardEvent event = it.next();

            /*
             * Test for unissued storm track operations. These should not be
             * filtered out by time.
             */
            if (!(HazardServicesDrawableBuilder.forModifyingStormTrack(event) && event
                    .getState() != HazardState.ISSUED)) {

                if (!doesEventOverlapSelectedTime(event, selectedRange,
                        selectedTime)) {
                    it.remove();
                }
            }
        }
    }

    /**
     * Convenience method for testing if the event is contained within the
     * selected time or selected time range.
     * 
     * @param
     * @return
     */
    public Boolean doesEventOverlapSelectedTime(IHazardEvent event,
            TimeRange selectedRange, Date selectedTime) {

        TimeRange eventRange = new TimeRange(event.getStartTime(),
                event.getEndTime());
        Boolean overlaps = true;
        if (selectedRange == null || !selectedRange.isValid()) {
            if (!eventRange.contains(selectedTime)) {
                overlaps = false;
            }
        } else if (!eventRange.overlaps(selectedRange)) {
            overlaps = false;
        }

        return overlaps;
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
     * @see
     * com.raytheon.viz.core.rsc.IVizResource#isApplicable(com.raytheon.viz.
     * core.PixelExtent)
     */
    public boolean isApplicable(PixelExtent extent) {
        return true;
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
     * @param df
     *            Builds PGEN drawables for display on the target
     * @return
     */
    private void drawGhost(IGraphicsTarget target, PaintProperties paintProps,
            DisplayElementFactory df) {

        df.setLayerDisplayAttr(false, null, false);

        Iterator<DrawableElement> iterator = ghost.createDEIterator();

        while (iterator.hasNext()) {
            drawElement(target, paintProps, df, iterator.next());
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
     * @param df
     *            Factory for creating PGEN drawables
     * @param el
     *            The PGEN drawable to be displayed.
     * @return
     */
    private void drawElement(IGraphicsTarget target,
            PaintProperties paintProps, DisplayElementFactory df,
            DrawableElement el) {

        AbstractElementContainer dispEl = null;

        dispEl = new DefaultElementContainer(el, descriptor, target);
        dispEl.draw(target, paintProps, null);
        dispEl.dispose();
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
    }

    /**
     * When an event is selected on the IHIS display, this method will be called
     * to notify all listeners.
     * 
     * @param eventIDs
     *            The identifiers of the events selected in the spatial display.
     */
    private void fireSelectedEventActionOccurred(String[] eventIDs) {
        SpatialDisplayAction action = new SpatialDisplayAction(
                SpatialDisplayAction.ActionType.SELECTED_EVENTS_CHANGED,
                eventIDs);
        eventBus.post(action);
    }

    /**
     * 
     */

    /**
     * When an item is selected from the right click CAVE context menu notify
     * all listeners.
     * 
     * @param menuLabel
     *            The label string of the selected context menu item
     */
    private void fireContextMenuItemSelected(String menuLabel) {
        SpatialDisplayAction action = new SpatialDisplayAction(
                SpatialDisplayAction.ActionType.CONEXT_MENU_SELECTED, 0,
                menuLabel);
        eventBus.post(action);
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

            VizApp.runAsync(new Runnable() {
                @Override
                public void run() {
                    eventBus.post(action);
                }
            });
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
     * Parses an RGB string and creates a Color object from the resulting
     * values.
     * 
     * @param string
     *            The RGB string
     * @return A Color object created from the RGB string
     */
    public static Color convertRGBStringToColor(String string) {
        /*
         * Set a default color.
         */
        Color color = new Color(255, 255, 255);

        int[] values = new int[3];
        StringTokenizer tokenizer = new StringTokenizer(string);

        if (tokenizer.countTokens() == 3) {
            int j;

            for (j = 0; tokenizer.hasMoreTokens(); j++) {
                try {
                    values[j] = Integer.parseInt(tokenizer.nextToken());
                } catch (Exception e) {
                    statusHandler.debug("Could not parse RGB color string "
                            + string + " Defaulting to White.");
                    break;
                }

                if ((values[j] < 0) || (values[j] > 255)) {
                    break;
                }
            }

            if (j == 3) {
                color = new Color(values[0], values[1], values[2]);
            }
        }
        return color;
    }

    /**
     * Creates an RGB string based on a Color object.
     * 
     * @param Color
     *            Represents the color to convert to an RGB string.
     * @return An RGB string representation of the color.
     */
    public static String convertColorToRGBString(Color color) {
        return color.getRed() + " " + color.getGreen() + " " + color.getBlue();
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

        // editor.refresh();
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
     * @param fireEvent
     *            Indicates whether or not to fire an event indicating that an
     *            event geometry has been selected on the Spatial Display.
     * @return The event ID.
     */
    public String elementClicked(AbstractDrawableComponent element,
            boolean multipleSelection, boolean fireEvent) {

        if (element instanceof DECollection) {
            element = ((DECollection) element).getItemAt(0);
        }

        if (element instanceof IHazardServicesShape) {

            String clickedEventId = ((IHazardServicesShape) element).getID();

            if (fireEvent) {

                /*
                 * If this is a multiple selection operation (left mouse click
                 * with the Ctrl or Shift key held down), then check if the user
                 * is selecting something that was already selected and treat
                 * this as a deselect.
                 */
                if (multipleSelection
                        && selectedEventIDs.contains(clickedEventId)) {
                    selectedEventIDs.remove(clickedEventId);
                    String[] eventIDs = selectedEventIDs.toArray(new String[0]);
                    fireSelectedEventActionOccurred(eventIDs);
                } else if (multipleSelection) {
                    selectedEventIDs.add(clickedEventId);
                    String[] eventIDs = selectedEventIDs.toArray(new String[0]);
                    fireSelectedEventActionOccurred(eventIDs);
                } else {
                    fireSelectedEventActionOccurred(new String[] { clickedEventId });
                }
            }
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
        fireSelectedEventActionOccurred(eventIDs.toArray(new String[eventIDs
                .size()]));
    }

    /**
     * Removes the label associated with a drawable element.
     * 
     * @param element
     *            The element to remove the label from.
     */
    public void removeElementLabel(AbstractDrawableComponent element) {
        String eventID = elementClicked(element, false, false);

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
     * remove an ADC from the selected list.
     * 
     * @param adc
     */
    public void removeSelected(AbstractDrawableComponent adc) {
        if (elSelected.contains(adc)) {
            elSelected.remove(adc);
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
                    Coordinate[] screenCoords = new Coordinate[coords.length];

                    for (int i = 0; i < coords.length; ++i) {
                        screenCoord = editor.translateInverseClick(coords[i]);
                        screenCoords[i] = new Coordinate(screenCoord[0],
                                screenCoord[1]);
                    }

                    // Create the new Polygon...
                    GeometryFactory gf = p.getFactory();
                    LinearRing linearRing = gf.createLinearRing(screenCoords);

                    double distance = clickScreenPoint.distance(linearRing);

                    if (distance < minDist) {
                        minDist = distance;
                        closestSymbol = comp;
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

        List<AbstractDrawableComponent> containingSymbolsList = Lists
                .newArrayList();

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
     * Notifies listeners of a modified event, for example when a point is added
     * or removed from an event boundary.
     * 
     * @param modifiedEventJSON
     *            A JSON string describing the modified event area.
     * @return
     */
    public void notifyModifiedEvent(String modifiedEventJSON) {
        Dict modifyEvent = Dict.getInstance(modifiedEventJSON);
        String shapeType = modifyEvent
                .getDynamicallyTypedValue(HAZARD_EVENT_SHAPE_TYPE);
        String eventID = modifyEvent
                .getDynamicallyTypedValue(HAZARD_EVENT_IDENTIFIER);
        if (modifyEvent.getDynamicallyTypedValue(SYMBOL_NEW_LAT_LON) == null) {
            List<Dict> shapes = modifyEvent.getDynamicallyTypedValue(SHAPES);
            Geometry geometry = JSONUtilities.geometryFromJSONShapes(shapes);
            if (geometry.isValid()) {
                ISessionEventManager sessionEventManager = appBuilder
                        .getSessionManager().getEventManager();

                IHazardEvent hazardEvent = sessionEventManager
                        .getEventById(eventID);
                hazardEvent.setGeometry(geometry);
                SessionEventGeometryModified action = new SessionEventGeometryModified(
                        sessionEventManager, hazardEvent);
                eventBus.post(action);
            } else {
                IsValidOp op = new IsValidOp(geometry);
                statusHandler.warn("Invalid Geometry: "
                        + op.getValidationError().getMessage()
                        + ": Geometry modification undone");

            }
        } else {
            Dict asDict = Dict.getInstance(modifiedEventJSON);
            ModifyStormTrackAction action = new ModifyStormTrackAction();
            action.setParameters(Utilities.asMap(asDict));
            eventBus.post(action);
        }
    }

    @Override
    public void addContextMenuItems(IMenuManager menuManager, int x, int y) {
        /**
         * In here, add the options to delete a vertex and add a vertex, but
         * only if over a selected hazard and point. Retrieve the list of
         * context menu items to add...
         */
        List<String> entries = getContextMenuEntries();

        if (entries != null && entries.size() > 0) {

            for (String contextMenuEntry : entries) {

                menuManager.add(new Action(contextMenuEntry) {
                    @Override
                    public void run() {
                        super.run();
                        fireContextMenuItemSelected(getText());
                    }
                });
            }
        }

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
     * Adds entries to the right click context menu in CAVE based on the state
     * of the model and the mouse pointer's proximity to displayed hazards.
     * 
     * @param
     * @return A list of entries to add to the context menu.
     */
    public List<String> getContextMenuEntries() {
        ISessionEventManager eventManager = appBuilder.getSessionManager()
                .getEventManager();
        List<String> entries = new ArrayList<String>();
        EnumSet<HazardState> states = EnumSet.noneOf(HazardState.class);

        boolean isModified = false;
        boolean canBeClippedAndReduced = false;

        for (IHazardEvent event : eventManager.getSelectedEvents()) {
            states.add(event.getState());

            if (event instanceof IModifiable
                    && ((IModifiable) event).isModified()) {
                isModified = true;
            }

            if (event.getHazardType() != null) {
                canBeClippedAndReduced = true;
            }

            /*
             * Logic to handle hazard-specific contributions to the context
             * menu. This is used, for example, by the "Add/Remove Shapes" entry
             * which applies to hazard geometries created by the draw-by-area
             * tool.
             */
            @SuppressWarnings("unchecked")
            List<String> contextMenuEntries = (List<String>) event
                    .getHazardAttribute(HazardConstants.CONTEXT_MENU_CONTRIBUTION_KEY);

            if (contextMenuEntries != null) {
                for (String contextMenuEntry : contextMenuEntries) {

                    if (contextMenuEntry
                            .equals(HazardConstants.CONTEXT_MENU_ADD_REMOVE_SHAPES)) {

                        if (!eventManager.canEventAreaBeChanged(event)) {
                            continue;
                        }

                        entries.add(contextMenuEntry);
                    }
                }
            }
        }

        entries.add(HazardConstants.CONTEXT_MENU_HAZARD_INFORMATION_DIALOG);
        if (!isModified && states.contains(HazardState.ISSUED)) {
            entries.add(HazardConstants.END_SELECTED_HAZARDS);
        }
        if (canBeClippedAndReduced) {
            entries.add(HazardConstants.CONTEXT_MENU_CLIP_AND_REDUCE_SELECTED_HAZARDS);
        }
        if (!states.contains(HazardState.PROPOSED) || states.size() > 1) {
            entries.add(HazardConstants.PROPOSE_SELECTED_HAZARDS);
        }
        if (!states.contains(HazardState.ISSUED) || states.size() > 1) {
            entries.add("Issue Selected Hazards");
            entries.add("Delete Selected Hazards");
        }
        if (states.contains(HazardState.PROPOSED)) {
            entries.add("Save Proposed Hazards");
        }
        entries.add("Send to Back");
        entries.add("Bring to Front");

        /*
         * Check for potential events.
         */
        Collection<IHazardEvent> potentialEvents = eventManager
                .getEventsByState(HazardState.POTENTIAL);

        if (!potentialEvents.isEmpty()) {
            entries.add(HazardConstants.REMOVE_POTENTIAL_HAZARDS);
        }

        entries.add("Hazard Occurrence Alerts");

        if (spatialView
                .isCurrentCursor(SpatialViewCursorTypes.MOVE_NODE_CURSOR)) {
            entries.add("Delete Node");
        } else if (spatialView
                .isCurrentCursor(SpatialViewCursorTypes.DRAW_CURSOR)) {
            entries.add("Add Node");
        }

        return entries;
    }

    /**
     * Sets the selected hazard.
     * 
     * @param comp
     *            The selected hazard.
     * @return
     */
    public void setSelectedHazardIHISLayer(AbstractDrawableComponent comp) {

        selectedHazardIHISLayer = comp;

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
    public AbstractDrawableComponent getSelectedHazardIHISLayer() {
        return selectedHazardIHISLayer;
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

        if ((selectedHazardIHISLayer != null) && (drawSelectedHandleBars)) {
            if ((selectedHazardIHISLayer instanceof IHazardServicesShape)
                    && ((IHazardServicesShape) selectedHazardIHISLayer)
                            .isEditable()) {

                if (!handleBarPoints.isEmpty()) {

                    target.drawPoints(handleBarPoints, handleBarColor.getRGB(),
                            PointStyle.DISC, HANDLEBAR_MAGNIFICATION);
                }
            }
        }
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

    public boolean isDrawSelectedHandleBars() {
        return drawSelectedHandleBars;
    }

    public double[] getSelectedHazardCenterPoint() {
        AbstractDrawableComponent comp = getSelectedHazardIHISLayer();
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

    @Override
    public void toolChanged() {

    }

    @Override
    protected void paint(IGraphicsTarget target, PaintProperties paintProps,
            AbstractDrawableComponent object,
            AbstractMovableToolLayer.SelectionStatus status)
            throws VizException {

        // Draw the IHIS event
        drawProduct(target, paintProps, object);

        // If there is a selected polygon
        if (selectedHazardIHISLayer != null) {
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

    /**
     * @return the dataManager
     */
    public ToolLayerDataManager getDataManager() {
        return dataManager;
    }

    @Override
    protected void paintInternal(IGraphicsTarget target,
            PaintProperties paintProps) throws VizException {
        // Retrieve the a PGEN DisplayElementFactory
        DisplayElementFactory df = new DisplayElementFactory(target, descriptor);

        if (ghost != null) {
            drawGhost(target, paintProps, df);
        }

        super.paintInternal(target, paintProps);
    }

    /**
     * @param mouseHandler
     *            the mouseHandler to set
     */
    public void setMouseHandler(IInputHandler mouseHandler) {
        this.mouseHandler = mouseHandler;
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
    public DataTime[] getDataTimes() {
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
        } else {
            FramesInfo info = descriptor.getFramesInfo();
            dataTimes.clear();
            this.maximumFrameCount = this.descriptor.getNumberOfFrames();
            // First time called
            if (info.getFrameTimes() != null) {
                for (DataTime dt : info.getFrameTimes()) {
                    dataTimes.add(dt);
                }
            }

            if (dataTimes.size() == 0) {
                timeMatchBasis = true;
                // Case where this tool is time match basis or no data loaded
                DataTime currentTime = null;
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
     * @param allowDisposeMessage
     *            the allowDisposeMessage to set
     */
    public void setAllowDisposeMessage(boolean allowDisposeMessage) {
        this.allowDisposeMessage = allowDisposeMessage;
    }

    /**
     * @return the allowDisposeMessage
     */
    public boolean isAllowDisposeMessage() {
        return allowDisposeMessage;
    }

    @Override
    public ResourceOrder getResourceOrder() {
        return RenderingOrderFactory.ResourceOrder.HIGHEST;
    }

    /**
     * @return the geoFactory
     */
    public GeometryFactory getGeoFactory() {
        return geoFactory;
    }

}

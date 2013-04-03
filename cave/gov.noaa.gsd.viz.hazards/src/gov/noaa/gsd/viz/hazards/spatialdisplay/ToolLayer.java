/**
 * This software was developed and / or modified by the National Oceanic and
 * Atmospheric Administration (NOAA), Earth System Research Laboratory (ESRL),
 * Global Systems Division (GSD), Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.spatialdisplay;

import gov.noaa.gsd.viz.hazards.display.HazardServicesMessageHandler;
import gov.noaa.gsd.viz.hazards.display.action.SpatialDisplayAction;
import gov.noaa.gsd.viz.hazards.jsonutilities.Dict;
import gov.noaa.gsd.viz.hazards.jsonutilities.DictList;
import gov.noaa.gsd.viz.hazards.spatialdisplay.SpatialView.SpatialViewCursorTypes;
import gov.noaa.gsd.viz.hazards.spatialdisplay.drawableelements.HazardServicesDrawableBuilder;
import gov.noaa.gsd.viz.hazards.spatialdisplay.drawableelements.HazardServicesLine;
import gov.noaa.gsd.viz.hazards.spatialdisplay.drawableelements.HazardServicesSymbol;
import gov.noaa.gsd.viz.hazards.spatialdisplay.drawableelements.IHazardServicesShape;
import gov.noaa.gsd.viz.hazards.spatialdisplay.mousehandlers.SelectionDrawingAction;
import gov.noaa.gsd.viz.hazards.utilities.Utilities;
import gov.noaa.gsd.viz.mvp.EventBusSingleton;
import gov.noaa.nws.ncep.ui.pgen.display.AbstractElementContainer;
import gov.noaa.nws.ncep.ui.pgen.display.DefaultElementContainer;
import gov.noaa.nws.ncep.ui.pgen.display.DisplayElementFactory;
import gov.noaa.nws.ncep.ui.pgen.display.DisplayProperties;
import gov.noaa.nws.ncep.ui.pgen.display.ElementContainerFactory;
import gov.noaa.nws.ncep.ui.pgen.display.IDisplayable;
import gov.noaa.nws.ncep.ui.pgen.display.LinePatternManager;
import gov.noaa.nws.ncep.ui.pgen.elements.AbstractDrawableComponent;
import gov.noaa.nws.ncep.ui.pgen.elements.DrawableElement;
import gov.noaa.nws.ncep.ui.pgen.elements.Layer;
import gov.noaa.nws.ncep.ui.pgen.elements.Symbol;
import gov.noaa.nws.ncep.ui.pgen.elements.SymbolLocationSet;
import gov.noaa.nws.ncep.ui.pgen.elements.Text;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;

import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.DataTime;
import com.raytheon.uf.common.time.SimulatedTime;
import com.raytheon.uf.viz.core.IDisplayPaneContainer;
import com.raytheon.uf.viz.core.IGraphicsTarget;
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
import com.raytheon.uf.viz.core.rsc.tools.AwipsToolsResourceData;
import com.raytheon.viz.awipstools.IToolChangedListener;
import com.raytheon.viz.ui.VizWorkbenchManager;
import com.raytheon.viz.ui.cmenu.IContextMenuContributor;
import com.raytheon.viz.ui.editor.AbstractEditor;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateArrays;
import com.vividsolutions.jts.geom.CoordinateList;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

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
 * </pre>
 * 
 * @author Xiangbao Jing
 */
@SuppressWarnings("unchecked")
public class ToolLayer extends
        AbstractMovableToolLayer<AbstractDrawableComponent> implements
        IContextMenuContributor, IToolChangedListener, IResourceDataChanged {

    /**
     * Logging mechanism.
     */
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(ToolLayer.class);

    public static final String DEFAULT_NAME = "Hazard Services";

    public static double VERTEX_CIRCLE_RADIUS = 2;

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

    private final ConcurrentHashMap<DrawableElement, AbstractElementContainer> displayMap;

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

    /*
     * selected elements that should be displayed with a marker other than the
     * default gray "DOT"
     */
    private Map<AbstractDrawableComponent, Symbol> selectedSymbol = null;

    private GeometryFactory geoFactory = null;

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
    private DictList previousDictList = null;

    /*
     * ID of the selected events, support multiple selection
     */
    private List<String> clickedEventIds = null;

    /*
     * Callback for right button selection invoking in the multiple event
     * selection case. The callback can be an product editor or whatever
     * dialog(or application) which will be invoked after the multiple selecting
     * is done. An application like River Recommender can set up the callback.
     */
    private final String multiSelectCallback = null;

    /*
     * reference to eventBus singleton instance
     */
    private EventBus eventBus = null;

    /**
     * Constructor.
     * 
     * @param resourceData
     *            The resource data for the display
     * @param loadProperties
     *            The properties describing this resource.
     */
    public ToolLayer(AwipsToolsResourceData<ToolLayer> resourceData,
            LoadProperties loadProperties) {

        super(resourceData, loadProperties);

        displayMap = new ConcurrentHashMap<DrawableElement, AbstractElementContainer>();
        elSelected = new ArrayList<AbstractDrawableComponent>();
        selectedSymbol = new HashMap<AbstractDrawableComponent, Symbol>();
        geoFactory = new GeometryFactory();
        drawableBuilder = new HazardServicesDrawableBuilder();
        dataManager = new ToolLayerDataManager();

        dataTimes = new ArrayList<DataTime>();
        eventBus = EventBusSingleton.getInstance();
    }

    /**
     * Clear all events from the spatial display.
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

            removeElement(de);
        }

        issueRefresh();
    }

    /**
     * Draws event geometries on the Hazard Services Tool Layer.
     * 
     * @param JSONeventAreas
     *            - A JSON string contain a list of event dictionaries. Each
     *            dictionary contains information about how to render the event
     *            it contains.
     * @return
     */

    public void drawEventAreas(String JSONeventAreas) {

        DictList dictList = DictList.getInstance(JSONeventAreas);

        if (previousDictList != null) {

            if (dictList.equals(previousDictList)) {
                /*
                 * This list of events is identical to the previously drawn
                 * list. Do nothing more.
                 */
                return;
            }
        }

        previousDictList = dictList;
        clearEvents();
        selectedHazardIHISLayer = null;

        try {
            if (dictList != null) {
                for (int i = 0; i < dictList.size(); ++i) {
                    Dict event = (Dict) dictList.get(i);
                    drawEventArea(event);
                }

                setObjects(dataManager.getActiveLayer().getDrawables());
                issueRefresh();
            }
        } catch (VizException e) {
            statusHandler.error(
                    "ToolLayer.drawEventAreas(): draw of areas failed.", e);
        }

    }

    /**
     * Draws an event geometry on the Hazard Services Tool Layer.
     * 
     * @param obj
     *            - The dictionary containing shape information for the event.
     * @return
     */
    public void drawEventArea(Dict obj) throws VizException {
        AbstractDrawableComponent drawableComponent;
        AbstractDrawableComponent text;
        HazardServicesDrawingAttributes drawingAttributes;
        List<Coordinate> points;

        // Retrieve the event identifier
        String eventID = (String) obj.get(Utilities.HAZARD_EVENT_IDENTIFIER);
        List<Dict> shapeArray = (List<Dict>) obj
                .get(Utilities.HAZARD_EVENT_SHAPES);

        // There could be one or more shapes per event.
        if (shapeArray != null && shapeArray.size() > 0) {
            for (int i = 0; i < shapeArray.size(); ++i) {
                drawableComponent = null;
                text = null;
                drawingAttributes = null;
                points = null;

                Dict shape = shapeArray.get(i);

                /*
                 * Do not try to draw anything that is outside or has a point
                 * that is outside of the grid extent of the display. This seems
                 * to be mainly a problem in the GFE perspective.
                 */
                drawableComponent = drawableBuilder.buildDrawableComponent(
                        shape, eventID, points, getActiveLayer(),
                        getDescriptor());

                if (drawableComponent != null) {
                    drawingAttributes = drawableBuilder.getDrawingAttributes();

                    if (drawingAttributes.getLineWidth() == 4.0) {
                        selectedHazardIHISLayer = drawableComponent;
                    }

                    addElement(drawableComponent);
                    text = null;

                    if (drawingAttributes.getString() != null
                            && drawingAttributes.getString().length > 0) {
                        text = drawableBuilder.buildText(shape, eventID,
                                drawableComponent.getPoints(),
                                getActiveLayer(), geoFactory);
                        addElement(text);
                    }
                } // End of test for null drawable

            } // End of shape array loop.

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
     * 
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
                // TODO Auto-generated method stub
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
        super.disposeInternal();

        // Fire a spatial display dispose event...
        fireSpatialDisplayDisposedActionOccurred();
    }

    /**
     * When an event is selected on the IHIS display, this method will be called
     * to notify all listeners.
     * 
     * @param eventIDs
     *            The identifiers of the events selected in the spatial display.
     * @param multipleSelection
     *            Indicates whether or not this was a part of a multiple
     *            selection action.
     */
    private void fireSelectedEventActionOccurred(String[] eventIDs,
            boolean multipleSelection) {
        SpatialDisplayAction action = new SpatialDisplayAction(
                "SelectedEventsChanged", eventIDs, multipleSelection);
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
                "ContextMenuSelected", 0, menuLabel);
        eventBus.post(action);
    }

    /**
     * Sends a message over the event bus when an event is modified in the
     * spatial display.
     * 
     * @param JSON
     *            string describing the modified event.
     * @return
     */
    private void fireModifiedEventActionOccurred(String modifyEventJSON) {

        SpatialDisplayAction action = new SpatialDisplayAction(
                "ModifyEventArea");
        action.setModifyEventJSON(modifyEventJSON);
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
                    "DisplayDisposed");

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
        if (el instanceof Symbol) {
            if ((paintProps.isZooming())
                    || (previousZoomLevel != paintProps.getZoomLevel())) {
                previousZoomLevel = paintProps.getZoomLevel();
                displayMap.remove(el);
            }
        }

        if (!displayMap.containsKey(el)) {
            AbstractElementContainer container = ElementContainerFactory
                    .createContainer((DrawableElement) el, descriptor, target);
            displayMap.put((DrawableElement) el, container);
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
                if (((IHazardServicesShape) de).getEventID().equals(eventID)) {
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
    public String elementClicked(DrawableElement element,
            boolean multipleSelection, boolean fireEvent) {
        if (element instanceof IHazardServicesShape) {

            String clickedEventId = ((IHazardServicesShape) element)
                    .getEventID();
            if (fireEvent) {
                fireSelectedEventActionOccurred(
                        new String[] { clickedEventId }, multipleSelection);
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
     * 
     * @return
     */
    public void multipleElementsClicked(Set<String> eventIDs) {
        fireSelectedEventActionOccurred(
                eventIDs.toArray(new String[eventIDs.size()]), false);
    }

    // Support multiple selection------------------------------------
    /**
     * Get the selected event IDs for the multiple selection
     */
    public List<String> getSelectedEventIds() {
        return clickedEventIds;
    }

    /**
     * Add a selected event ID into the selectedEventIds for the multiple
     * selection. The adding rule is that add the event ID into the
     * selectedEventIds if it not is existing, otherwise remove it from the
     * selectedEventIds.
     * 
     * @param selectedEventId
     *            The identifier of the selected event.
     * 
     */
    public void addSelectedEventId(String selectedEventId) {
        clickedEventIds.add(selectedEventId);
    }

    /**
     * Set the selected event IDs for the multiple selection
     * 
     * @param selectedEventIds
     *            The selected events.
     */
    public void setSelectedEventIds(ArrayList<String> selectedEventIds) {
        this.clickedEventIds = selectedEventIds;
    }

    /**
     * Tests an event to determine whether or not it is selected.
     * 
     * @param selectedEventId
     *            The identifier of the event to test for selected state
     * @return true - the event is selected; false - the event is not selected.
     */
    public boolean isSelectedEventId(String selectedEventId) {
        if (clickedEventIds == null || clickedEventIds.size() == 0) {
            return false;
        }

        if (!clickedEventIds.contains(selectedEventId)) {
            return false;
        }

        return true;
    }

    /**
     * Removes the label associated with a drawable element.
     * 
     * @param element
     *            The element to remove the label from.
     * 
     */
    public void removeElementLabel(DrawableElement element) {
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
                    if (((IHazardServicesShape) de).getEventID() == eventID) {
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

        // Note that the distance unit of the JTS distance function is central
        // angle degrees.
        // This seems to match closely with degrees lat and lon.
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
                Polygon p = ((IHazardServicesShape) comp).getPolygon();

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

        if (minDist <= SelectionDrawingAction.SELECTION_DISTANCE_PIXELS) {
            return closestSymbol;
        }

        return null;
    }

    /**
     * Given a point, finds the containing drawable component.
     * 
     * @param point
     *            The point to test against
     * @return The drawable component which contains this point.
     */
    public AbstractDrawableComponent getContainingComponent(Coordinate point) {
        Iterator<AbstractDrawableComponent> iterator = dataManager
                .getActiveLayer().getComponentIterator();

        // Check each of the hazard polygons. Normally, there will not be
        // too many of these. However, we can make this more efficient by
        // using a tree and storing/resusing the Geometries.
        Point clickPoint = geoFactory.createPoint(point);

        AbstractDrawableComponent selectedSymbol = null;

        while (iterator.hasNext()) {
            AbstractDrawableComponent comp = iterator.next();

            if (comp instanceof IHazardServicesShape) {
                Polygon p = ((IHazardServicesShape) comp).getPolygon();

                if (p != null) {
                    if (clickPoint.within(p)) {
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
     * @param The
     *            point to test.
     * @return A list of drawables which contain this point.
     */
    public List<AbstractDrawableComponent> getContainingComponents(
            Coordinate point) {
        Iterator<AbstractDrawableComponent> iterator = dataManager
                .getActiveLayer().getComponentIterator();

        // Check each of the hazard polygons. Normally, there will not be
        // too many of these. However, we can make this more efficient by
        // using a tree and storing/resusing the Geometries.
        Point clickPoint = geoFactory.createPoint(point);

        List<AbstractDrawableComponent> containingSymbolsList = new ArrayList<AbstractDrawableComponent>();

        while (iterator.hasNext()) {
            AbstractDrawableComponent comp = iterator.next();

            // Skip Labels (PGEN Text Objects). These are not
            // selectable for now...
            if (comp instanceof IHazardServicesShape) {
                Polygon p = ((IHazardServicesShape) comp).getPolygon();

                if (p != null) {
                    if (clickPoint.within(p)) {
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
        fireModifiedEventActionOccurred(modifiedEventJSON);
    }

    @Override
    public void addContextMenuItems(IMenuManager menuManager, int x, int y) {
        // In here, add the options to delete a vertex
        // and add a vertex, but only if over a selected
        // hazard and point.
        // Retrieve the list of context menu items to add...
        List<String> entries = getContextMenuEntries();

        if (entries != null && entries.size() > 0) {

            for (String contextMenuEntry : entries) {

                menuManager.add(new Action(contextMenuEntry) {
                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        super.run();
                        fireContextMenuItemSelected(getText());
                    }
                });
            }
        }

    }

    /**
     * Adds entries to the right click context menu in CAVE based on the state
     * of the model and the mouse pointer's proximity to displayed hazards.
     * 
     * @param
     * @return A list of entries to add to the context menu.
     */
    private List<String> getContextMenuEntries() {
        List<String> entryList = new ArrayList<String>();
        String jsonString = HazardServicesMessageHandler.getModelProxy()
                .getContextMenuEntries();

        JsonParser parser = new JsonParser();

        JsonElement jsonElement = parser.parse(jsonString);
        entryList = new Gson().fromJson(jsonElement, entryList.getClass());

        if (SpatialViewCursorTypes.MOVE_POINT_CURSOR.isCurrentCursor()) {
            entryList.add("Delete Point");
        } else if (SpatialViewCursorTypes.MOVE_POLYGON_CURSOR.isCurrentCursor()
                || SpatialViewCursorTypes.DRAW_CURSOR.isCurrentCursor()) {
            entryList.add("Add Point");
            entryList.add("Move Entire Element");
        }

        return entryList;
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
     * Draws "PGEN handle bars" on the selected hazard geometry. These show
     * where vertices are for vertex editing operations.
     * 
     * @param target
     *            The target to draw to.
     * @param paintProps
     *            Describes how drawables appear on the target.
     * @return
     */
    private void drawSelected(IGraphicsTarget target, PaintProperties paintProps) {

        if ((selectedHazardIHISLayer != null) && (drawSelectedHandleBars)) {
            if (((IHazardServicesShape) selectedHazardIHISLayer)
                    .canVerticesBeEdited()) {
                DisplayElementFactory df = new DisplayElementFactory(target,
                        descriptor);
                List<IDisplayable> displayEls = new ArrayList<IDisplayable>();
                HashMap<Symbol, CoordinateList> map = new HashMap<Symbol, CoordinateList>();

                Symbol defaultSymbol = new Symbol(null,
                        new Color[] { Color.lightGray }, 2.5f, 7.5, false,
                        null, "Marker", "DOT");
                Symbol selectSymbol = new Symbol(null,
                        new Color[] { Color.red }, 2.5f, 7.5, false, null,
                        "Marker", "DOT");

                CoordinateList defaultPts = new CoordinateList();
                CoordinateList selectPts = new CoordinateList();

                AbstractDrawableComponent el = selectedHazardIHISLayer;

                if (selectedSymbol.containsKey(el)) {
                    Symbol currSym = selectedSymbol.get(el);
                    Coordinate[] pts = CoordinateArrays.toCoordinateArray(el
                            .getPoints());
                    if (map.containsKey(currSym)) {
                        map.get(currSym).add(pts, true);
                    } else {
                        map.put(currSym, new CoordinateList(pts));
                    }
                } else {
                    for (Coordinate point : el.getPoints()) {
                        /*
                         * if (inSelectedIndex(pointIdx)) { selectPts.add(point,
                         * true); } else {
                         */
                        defaultPts.add(point, true);
                        /* } */
                    }
                }

                if (!defaultPts.isEmpty()) {
                    SymbolLocationSet symset = new SymbolLocationSet(
                            defaultSymbol, defaultPts.toCoordinateArray());
                    displayEls.addAll(df.createDisplayElements(symset,
                            paintProps));
                }
                if (!selectPts.isEmpty()) {
                    SymbolLocationSet symset = new SymbolLocationSet(
                            selectSymbol, selectPts.toCoordinateArray());
                    displayEls.addAll(df.createDisplayElements(symset,
                            paintProps));
                }
                if (!map.isEmpty()) {
                    for (Symbol sym : map.keySet()) {
                        SymbolLocationSet symset = new SymbolLocationSet(sym,
                                map.get(sym).toCoordinateArray());
                        displayEls.addAll(df.createDisplayElements(symset,
                                paintProps));
                    }
                }

                // drawElement( target, paintProps, df, symset );
                for (IDisplayable each : displayEls) {
                    each.draw(target, paintProps);
                    each.dispose();
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
            Polygon p = ((IHazardServicesShape) comp).getPolygon();

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
        // TODO Auto-generated method stub

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
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    protected AbstractDrawableComponent makeLive(
            AbstractDrawableComponent object) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected AbstractDrawableComponent move(Coordinate lastMouseLoc,
            Coordinate mouseLoc, AbstractDrawableComponent object) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected String getDefaultName() {
        // TODO Auto-generated method stub
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

}

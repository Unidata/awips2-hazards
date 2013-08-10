/**
 * This software was developed and / or modified by the National Oceanic and
 * Atmospheric Administration (NOAA), Earth System Research Laboratory (ESRL),
 * Global Systems Division (GSD), Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.spatialdisplay;

import gov.noaa.gsd.viz.hazards.display.HazardServicesAppBuilder;
import gov.noaa.gsd.viz.hazards.display.HazardServicesMessageHandler;
import gov.noaa.gsd.viz.hazards.display.RCPMainUserInterfaceElement;
import gov.noaa.gsd.viz.hazards.display.action.SpatialDisplayAction;
import gov.noaa.gsd.viz.hazards.jsonutilities.Dict;
import gov.noaa.gsd.viz.hazards.jsonutilities.DictList;
import gov.noaa.gsd.viz.hazards.spatialdisplay.mousehandlers.MouseHandlerFactory;
import gov.noaa.gsd.viz.hazards.spatialdisplay.mousehandlers.SelectionDrawingAction.SelectionHandler;
import gov.noaa.gsd.viz.hazards.spatialdisplay.selectbyarea.SelectByAreaDbMapResource;
import gov.noaa.gsd.viz.hazards.spatialdisplay.selectbyarea.SelectByAreaDbMapResourceData;
import gov.noaa.gsd.viz.hazards.toolbar.BasicAction;
import gov.noaa.gsd.viz.hazards.toolbar.PulldownAction;
import gov.noaa.gsd.viz.hazards.toolbar.SeparatorAction;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.PlatformUI;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.DataTime;
import com.raytheon.uf.viz.core.AbstractTimeMatcher;
import com.raytheon.uf.viz.core.IDisplayPane;
import com.raytheon.uf.viz.core.VizApp;
import com.raytheon.uf.viz.core.drawables.IDescriptor;
import com.raytheon.uf.viz.core.drawables.IDescriptor.FramesInfo;
import com.raytheon.uf.viz.core.drawables.IDescriptor.IFrameChangedListener;
import com.raytheon.uf.viz.core.drawables.IRenderableDisplay;
import com.raytheon.uf.viz.core.drawables.ResourcePair;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.localization.LocalizationManager;
import com.raytheon.uf.viz.core.map.IMapDescriptor;
import com.raytheon.uf.viz.core.map.MapDescriptor;
import com.raytheon.uf.viz.core.maps.rsc.DbMapResource;
import com.raytheon.uf.viz.core.maps.rsc.DbMapResourceData;
import com.raytheon.uf.viz.core.rsc.AbstractVizResource;
import com.raytheon.uf.viz.core.rsc.IDisposeListener;
import com.raytheon.uf.viz.core.rsc.IInputHandler;
import com.raytheon.uf.viz.core.rsc.LoadProperties;
import com.raytheon.uf.viz.core.rsc.ResourceList;
import com.raytheon.uf.viz.core.rsc.ResourceList.AddListener;
import com.raytheon.uf.viz.core.rsc.ResourceList.RemoveListener;
import com.raytheon.viz.ui.VizWorkbenchManager;
import com.raytheon.viz.ui.editor.AbstractEditor;

/**
 * Spatial display view, which manages the spatial display.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Chris.Golden      Initial induction into repo
 * Jul 10, 2013    585     Chris.Golden      Changed to support loading from bundle.
 * Aug 04, 2013   1265     Bryon.Lawrence    Added support for undo/redo
 * Aug  9, 2013 1921       daniel.s.schaffer@noaa.gov  Support of replacement of JSON with POJOs
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class SpatialView implements
        ISpatialView<Action, RCPMainUserInterfaceElement>,
        IFrameChangedListener, IDisposeListener {

    // Public Enumerated Types

    /**
     * Types of cursors.
     */
    public static enum SpatialViewCursorTypes {

        // Types of cursors.
        MOVE_POLYGON_CURSOR(SWT.CURSOR_SIZEALL), MOVE_POINT_CURSOR(
                SWT.CURSOR_HAND), ARROW_CURSOR(SWT.CURSOR_ARROW), DRAW_CURSOR(
                SWT.CURSOR_CROSS), WAIT_CURSOR(SWT.CURSOR_WAIT);

        // Private Variables

        /**
         * SWT cursor type that goes with this cursor.
         */
        private int swtType;

        // Private Constructors

        /**
         * Construct a standard instance.
         * 
         * @param swtType
         *            SWT type of cursor.
         */
        private SpatialViewCursorTypes(int swtType) {
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

    // Private Static Constants

    /**
     * For now, limit the overlays the user can use in Select by Area
     * operations. Not all overlays from the Maps menu can be used in Select By
     * Area. Also, some do not have a CWA column. The CWA column is used to
     * limit Select by Area to geometries contained within the CWA.
     */
    private static final Set<String> ACCEPTABLE_MAP_OVERLAYS;

    // Initialize the acceptable map overlays set.
    static {
        Set<String> set = new HashSet<String>();
        String[] mapOverlays = { "mapdata.cwa", "mapdata.ffmp_basins",
                "mapdata.firewxzones", "mapdata.zone", "mapdata.basins",
                "mapdata.county", "mapdata.isc" };
        for (String mapOverlay : mapOverlays) {
            set.add(mapOverlay);
        }
        ACCEPTABLE_MAP_OVERLAYS = set;
    };

    /**
     * Logging mechanism.
     */
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(SpatialView.class);

    // Private Classes

    /**
     * Standard action.
     */
    private class BasicSpatialAction extends BasicAction {

        // Private Variables

        /**
         * Action type.
         */
        private final String actionType;

        /**
         * Action name.
         */
        private final String actionName;

        // Public Constructors

        /**
         * Construct a standard instance.
         * 
         * @param text
         *            Text to be displayed.
         * @param iconFileName
         *            File name of the icon to be displayed, or
         *            <code>null</code> if no icon is to be associated with this
         *            action.
         * @param style
         *            Style; one of the <code>IAction</code> style constants.
         * @param toolTipText
         *            Tool tip text, or <code>null</code> if none is required.
         * @param actionType
         *            Type of action to be taken when this action is invoked.
         * @param actionName
         *            Name of action to be taken when this action is invoked;
         *            ignored for actions of checkbox style, since they always
         *            send "on" or "off" as their action name.
         */
        public BasicSpatialAction(String text, String iconFileName, int style,
                String toolTipText, String actionType, String actionName) {
            super(text, iconFileName, style, toolTipText);
            this.actionType = actionType;
            this.actionName = actionName;
        }

        // Public Methods

        /**
         * Run the action.
         */
        @Override
        public void run() {
            fireAction(new SpatialDisplayAction(actionType,
                    (getStyle() == Action.AS_CHECK_BOX ? (isChecked() ? "on"
                            : "off") : actionName)));
        }
    }

    /**
     * Maps for select-by-area pulldown menu action.
     */
    private class SelectByAreaMapsPulldownAction extends PulldownAction {

        // Private Variables

        /**
         * Resource list add listener, for detecting changes in the list of
         * resources currently displayed.
         */
        private final AddListener addListener = new AddListener() {
            @Override
            public void notifyAdd(ResourcePair rp) throws VizException {
                notifyResourceListChanged();
            }
        };

        /**
         * Resource list remove listener, for detecting changes in the list of
         * resources currently displayed.
         */
        private final RemoveListener removeListener = new RemoveListener() {
            @Override
            public void notifyRemove(ResourcePair rp) throws VizException {
                notifyResourceListChanged();
            }
        };

        /**
         * Listener for menu item invocations.
         */
        private final SelectionListener listener = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                fireAction((SpatialDisplayAction) ((MenuItem) event.widget)
                        .getData());
            }
        };

        // Public Constructors

        /**
         * Construct a standard instance.
         */
        public SelectByAreaMapsPulldownAction() {
            super("");
            setImageDescriptor(getImageDescriptorForFile("mapsForSelectByArea.png"));
            setToolTipText("Maps for Select by Area");

            // Enable or disable the action based upon the currently
            // loaded maps, building a list of said maps that are
            // appropriate for select by area operations.
            notifyResourceListChanged();

            // Set up listeners for notifications concerning the
            // addition or removal of resources, which are used to
            // determine what maps are available, if any, for select
            // by area operations, and to enable or disable this
            // action accordingly.
            ResourceList resourceList = ((AbstractEditor) VizWorkbenchManager
                    .getInstance().getActiveEditor()).getActiveDisplayPane()
                    .getDescriptor().getResourceList();
            resourceList.addPostAddListener(addListener);
            resourceList.addPostRemoveListener(removeListener);
        }

        // Public Methods

        /**
         * Dispose of the action.
         */
        @Override
        public void dispose() {
            super.dispose();

            // Remove the listeners for resource list changes.
            // Only do this if there is an active editor.
            AbstractEditor abstractEditor = (AbstractEditor) VizWorkbenchManager
                    .getInstance().getActiveEditor();

            if (abstractEditor != null) {
                ResourceList resourceList = abstractEditor
                        .getActiveDisplayPane().getDescriptor()
                        .getResourceList();
                resourceList.removePostAddListener(addListener);
                resourceList.removePostRemoveListener(removeListener);
            }
        }

        // Protected Methods

        /**
         * Get the menu for the specified parent, possibly reusing the specified
         * menu if provided.
         * 
         * @param parent
         *            Parent control.
         * @param menu
         *            Menu that was created previously, if any; this may be
         *            reused, or disposed of completely.
         * @return Menu.
         */
        @Override
        protected Menu doGetMenu(Control parent, Menu menu) {

            // If the menu has not yet been created, do so now;
            // otherwise, delete its contents.
            if (menu == null) {
                menu = new Menu(parent);
            } else {
                for (MenuItem item : menu.getItems()) {
                    item.dispose();
                }
            }

            // Load the current editor.
            AbstractEditor editor = ((AbstractEditor) VizWorkbenchManager
                    .getInstance().getActiveEditor());

            // Load the list of viz resources associated with it,
            // and iterate through the resource pairs, looking
            // for those that are database map resources. For
            // each of these that is visible, add a menu item.
            IDescriptor descriptor = editor.getActiveDisplayPane()
                    .getDescriptor();
            if (descriptor instanceof IMapDescriptor) {
                IMapDescriptor mapDescriptor = (IMapDescriptor) descriptor;
                ResourceList resourceList = mapDescriptor.getResourceList();
                for (ResourcePair pair : resourceList) {
                    if (pair.getResource() instanceof DbMapResource) {

                        // For now, just take the first one.
                        DbMapResource overlayResource = (DbMapResource) pair
                                .getResource();
                        DbMapResourceData resourceData = overlayResource
                                .getResourceData();
                        String mapName = resourceData.getMapName();
                        String tableName = resourceData.getTable();

                        // Make sure that this is an acceptable
                        // overlay table; if it is, create a menu
                        // item for it.
                        if (ACCEPTABLE_MAP_OVERLAYS.contains(tableName)) {

                            // Create the menu item, giving it a
                            // corresponding action if the map is
                            // visible, or just disabling it if
                            // not.
                            MenuItem item = new MenuItem(menu, SWT.PUSH);
                            item.setText(mapName);
                            if (overlayResource.getProperties().isVisible()) {
                                item.setData(new SpatialDisplayAction(
                                        "Drawing", "SelectByArea", mapName
                                                + " (Select By Area)",
                                        tableName));
                                item.addSelectionListener(listener);
                            } else {
                                item.setEnabled(false);
                            }
                        }
                    }
                }
            }

            // Return the menu.
            return menu;
        }

        // Private Methods

        /**
         * Respond to a possible change in the available maps for select by area
         * operations.
         */
        private void notifyResourceListChanged() {

            // Load the list of viz resources associated with it,
            // and iterate through the resource pairs, looking
            // for those that are database map resources. If at
            // least one is an acceptable resource for select by
            // area operations, this action should be enabled.
            boolean enable = false;

            /*
             * Only do this if there is an active editor.
             */
            AbstractEditor activeEditor = (AbstractEditor) VizWorkbenchManager
                    .getInstance().getActiveEditor();

            if (activeEditor != null) {
                IDescriptor descriptor = activeEditor.getActiveDisplayPane()
                        .getDescriptor();

                if (descriptor instanceof IMapDescriptor) {
                    IMapDescriptor mapDescriptor = (IMapDescriptor) descriptor;
                    ResourceList resourceList = mapDescriptor.getResourceList();
                    for (ResourcePair pair : resourceList) {
                        if (pair.getResource() instanceof DbMapResource) {

                            // For now, just take the first one.
                            String tableName = ((DbMapResource) pair
                                    .getResource()).getResourceData()
                                    .getTable();

                            // Make sure that this is an acceptable
                            // overlay table.
                            if (ACCEPTABLE_MAP_OVERLAYS.contains(tableName)) {
                                enable = true;
                                break;
                            }
                        }
                    }
                }

                // Enable or disable this action based upon whether
                // any maps were found that may be used in select
                // by area operations.
                setEnabled(enable);
            }
        }
    }

    // Private Variables

    /**
     * Map of cursor types to the corresponding cursors.
     */
    private final Map<SpatialViewCursorTypes, Cursor> cursorsForCursorTypes = Maps
            .newEnumMap(SpatialViewCursorTypes.class);

    /**
     * The current mouse handler. The mouse handler selection is driven by the
     * toolbar option chosen. The mouse handler controls how the user interacts
     * with the Hazard Services display.
     */
    private IInputHandler currentMouseHandler;

    /**
     * Hazard Services Tool Layer
     */
    private ToolLayer spatialDisplay = null;

    /**
     * Presenter.
     */
    private SpatialPresenter presenter = null;

    /**
     * Undo command action. NOTE: This may not belong here; which view should
     * manage Undo/Redo? For now, the action is disabled anyway.
     */
    private Action undoCommandAction = null;

    /**
     * Redo command action. NOTE: This may not belong here; which view should
     * manage Undo/Redo? For now, the action is disabled anyway.
     */
    private Action redoCommandAction = null;

    /**
     * Add to selected toggle action.
     */
    private Action addToSelectedToggleAction = null;

    /**
     * Move and select choice action.
     */
    private Action moveAndSelectChoiceAction = null;

    /**
     * Draw noded polygon choice action.
     */
    private Action drawNodedPolygonChoiceAction = null;

    /**
     * Draw freehand polygon choice action.
     */
    private Action drawFreehandPolygonChoiceAction = null;

    /**
     * Draw noded path choice action.
     */
    private Action drawNodedPathChoiceAction = null;

    /**
     * Draw point choice action.
     */
    private Action drawPointChoiceAction = null;

    /**
     * Maps for select by area pulldown action.
     */
    private Action selectByAreaMapsPulldownAction = null;

    private long currentFrameTime = Long.MIN_VALUE;

    /**
     * Map DB display with selectable geometries
     */
    private SelectByAreaDbMapResource selectableGeometryDisplay = null;

    // Mouse handler factory.
    private MouseHandlerFactory mouseFactory = null;

    // Public Constructors

    /**
     * Construct a standard instance.
     */
    public SpatialView(ToolLayer toolLayer) {
        this.spatialDisplay = toolLayer;
        toolLayer.setSpatialView(this);
    }

    // Public Methods

    /**
     * Initialize the view.
     * 
     * @param presenter
     *            Presenter managing this view.
     */
    @Override
    public final void initialize(SpatialPresenter presenter,
            MouseHandlerFactory mouseFactory) {
        this.presenter = presenter;
        this.mouseFactory = mouseFactory;

        /*
         * Initialize the spatial display and add it to the CAVE editor's
         * resource list.
         */
        AbstractEditor editor = ((AbstractEditor) VizWorkbenchManager
                .getInstance().getActiveEditor());

        IDisplayPane displayPane = editor.getActiveDisplayPane();
        displayPane.getBounds();
        displayPane.getRenderableDisplay().getView();

        IDescriptor idesc = editor.getActiveDisplayPane().getDescriptor();

        IMapDescriptor desc = null;

        if (idesc instanceof IMapDescriptor) {
            desc = (IMapDescriptor) idesc;
        }

        try {
            desc.getResourceList().add(spatialDisplay);
            spatialDisplay.initInternal(editor.getActiveDisplayPane()
                    .getTarget());
        } catch (Exception e) {
            statusHandler.error("Error initializing spatial display", e);
        }

        idesc.addFrameChangedListener(this);

        // Create the cursors that will be used by Hazard Services.
        Display display = Display.getCurrent();
        for (SpatialViewCursorTypes cursor : SpatialViewCursorTypes.values()) {
            cursorsForCursorTypes.put(cursor,
                    display.getSystemCursor(cursor.getSwtType()));
        }
    }

    /**
     * Prepare for disposal.
     */
    @Override
    public final void dispose() {
        removeListeners();

        setMouseHandler(null);

        if (spatialDisplay != null) {
            if (spatialDisplay.getDescriptor() != null) {
                spatialDisplay.getDescriptor().removeFrameChangedListener(this);
            }
            spatialDisplay.setAllowDisposeMessage(false);
            spatialDisplay.unload();
            spatialDisplay = null;
        }

        removeGeometryDisplay();
    }

    /**
     * Set the setting.
     * 
     * @param setting
     *            JSON string containing the mapping of key-value pairs making
     *            up the setting.
     */
    @Override
    public void setSetting(String setting) {
        ((ToolLayerResourceData) spatialDisplay.getResourceData())
                .setSetting(setting);
    }

    @Override
    public MapDescriptor getDescriptor() {
        return spatialDisplay.getDescriptor();
    }

    @Override
    public void issueRefresh() {
        spatialDisplay.issueRefresh();
    }

    @Override
    public void clearEvents() {
        spatialDisplay.clearEvents();
    }

    @Override
    public void redoTimeMatching() {
        MapDescriptor desc = spatialDisplay.getDescriptor();

        /*
         * Need to account for the possibility that a time matcher does not
         * exist (for example, in GFE)
         */
        AbstractTimeMatcher timeMatcher = desc.getTimeMatcher();

        if (timeMatcher != null) {
            try {
                desc.getTimeMatcher().redoTimeMatching(desc);
            } catch (VizException e) {
                statusHandler.error("SpatialView.redoTimeMatching():", e);
            }
        }
    }

    @Override
    public void setDisplayZoomParameters(double longitude, double latitude,
            double multiplier) {
        String perspectiveID = getCurrentPerspectiveDescriptor().getId();

        if (!perspectiveID.equals("GFE")) {
            IDisplayPane pane = ((AbstractEditor) VizWorkbenchManager
                    .getInstance().getActiveEditor()).getActiveDisplayPane();
            IRenderableDisplay display = pane.getRenderableDisplay();

            double[] lonLat = { longitude, latitude };
            display.getExtent().reset();
            display.recenter(lonLat);
            display.zoom(1.0 / multiplier);
        }
    }

    @Override
    public double[] getDisplayZoomParameters() {
        AbstractEditor editor = (AbstractEditor) VizWorkbenchManager
                .getInstance().getActiveEditor();
        IDisplayPane pane = editor.getActiveDisplayPane();
        IRenderableDisplay display = pane.getRenderableDisplay();
        double[] params = new double[3];
        double[] center = pane.getDescriptor().pixelToWorld(
                display.getExtent().getCenter());
        for (int j = 0; j < center.length; j++) {
            params[j] = center[j];
        }
        params[2] = 1.0 / display.getZoom();
        return params;
    }

    @Override
    public void recenterRezoomDisplay() {
        // Tell the editor to recenter itself on the selected event.
        // Retrieve the center lat lon of the selected event.
        double centerLatLonArray[] = spatialDisplay
                .getSelectedHazardCenterPoint();

        double[] displayParms = getDisplayZoomParameters();

        if (centerLatLonArray != null) {
            setDisplayZoomParameters(centerLatLonArray[0],
                    centerLatLonArray[1], displayParms[2]);
        }
    }

    @Override
    public final List<? extends Action> contributeToMainUI(
            RCPMainUserInterfaceElement type) {
        if (type == RCPMainUserInterfaceElement.TOOLBAR) {

            // Remove any old listeners first.
            removeListeners();

            // Create the actions.
            undoCommandAction = new BasicSpatialAction("", "undo.png",
                    Action.AS_PUSH_BUTTON, "Undo", "undo", null);
            undoCommandAction.setEnabled(false);
            redoCommandAction = new BasicSpatialAction("", "redo.png",
                    Action.AS_PUSH_BUTTON, "Redo", "redo", null);
            redoCommandAction.setEnabled(false);
            addToSelectedToggleAction = new BasicSpatialAction("",
                    "addToSelected.png", Action.AS_CHECK_BOX,
                    "Add New Pending to Selected", "addToSelected", null);
            moveAndSelectChoiceAction = new BasicSpatialAction("",
                    "moveAndSelect.png", Action.AS_RADIO_BUTTON,
                    "Select Event", "Drawing", "SelectEvent");
            moveAndSelectChoiceAction.setChecked(true);
            drawNodedPolygonChoiceAction = new BasicSpatialAction("",
                    "drawPolygon.png", Action.AS_RADIO_BUTTON, "Draw Polygon",
                    "Drawing", "DrawPolygon");
            drawFreehandPolygonChoiceAction = new BasicSpatialAction("",
                    "drawFreehandPolygon.png", Action.AS_RADIO_BUTTON,
                    "Draw Freehand Polygon", "Drawing", "DrawFreeHandPolygon");
            drawNodedPathChoiceAction = new BasicSpatialAction("",
                    "drawPath.png", Action.AS_RADIO_BUTTON, "Draw Path",
                    "Drawing", "DrawLine");
            drawNodedPathChoiceAction.setEnabled(false);
            drawPointChoiceAction = new BasicSpatialAction("", "drawPoint.png",
                    Action.AS_RADIO_BUTTON, "Draw Point", "Drawing",
                    "DrawPoint");
            drawPointChoiceAction.setEnabled(false);
            selectByAreaMapsPulldownAction = new SelectByAreaMapsPulldownAction();

            // Return the list.
            return Lists.newArrayList(undoCommandAction, redoCommandAction,
                    new SeparatorAction(), addToSelectedToggleAction,
                    new SeparatorAction(), moveAndSelectChoiceAction,
                    drawNodedPolygonChoiceAction,
                    drawFreehandPolygonChoiceAction, drawNodedPathChoiceAction,
                    drawPointChoiceAction, new SeparatorAction(),
                    selectByAreaMapsPulldownAction);
        }
        return Collections.emptyList();
    }

    // Private Methods

    /**
     * Receive notification that a drawing action has been completed.
     * <p>
     * NOTE: THIS SHOULD BE PRIVATE. Having it public is a temporary kludge
     * until the spatial display is truly part of this view, so that the latter
     * can notify the view when button state is to change. Right now, this
     * method is called from the App Builder.
     */
    public void notifyDrawingActionComplete() {
        moveAndSelectChoiceAction.setChecked(true);
        moveAndSelectChoiceAction.run();
        drawNodedPolygonChoiceAction.setChecked(false);
        drawFreehandPolygonChoiceAction.setChecked(false);
        drawNodedPathChoiceAction.setChecked(false);
        drawPointChoiceAction.setChecked(false);
    }

    /**
     * Receive notification that a select-by-area operation has been initiated.
     * <p>
     * NOTE: THIS SHOULD BE PRIVATE. Having it public is a temporary kludge
     * until the spatial display is truly part of this view, so that the latter
     * can notify the view when button state is to change. Right now, this
     * method is called from the App Builder.
     */
    public void notifySelectByAreaInitiated() {
        moveAndSelectChoiceAction.setChecked(false);
        drawNodedPolygonChoiceAction.setChecked(false);
        drawFreehandPolygonChoiceAction.setChecked(false);
        drawNodedPathChoiceAction.setChecked(false);
        drawPointChoiceAction.setChecked(false);
    }

    @Override
    public void drawEvents() {
        spatialDisplay.drawEventAreas(true);
    }

    @Override
    public void setMouseHandler(HazardServicesMouseHandlers mouseHandlerType,
            String... args) {
        /*
         * Unload the draw-by-area resource if it exists.
         */
        unloadGeometryDisplayResource();

        // Mouse handlers are registered to editors,
        // but they operate on specific AbstractVizResources.
        saveCurrentMouseHandler();

        boolean loadMouseHandler = true;

        switch (mouseHandlerType) {
        case SINGLE_SELECTION:
            setCursor(SpatialViewCursorTypes.MOVE_POLYGON_CURSOR);
            break;

        case MULTI_SELECTION:
            setCursor(SpatialViewCursorTypes.ARROW_CURSOR);
            break;

        case SELECTION_RECTANGLE:
            setCursor(SpatialViewCursorTypes.ARROW_CURSOR);
            break;

        case EVENTBOX_DRAWING:
            setCursor(SpatialViewCursorTypes.DRAW_CURSOR);
            break;

        case FREEHAND_DRAWING:
            setCursor(SpatialViewCursorTypes.DRAW_CURSOR);
            break;

        case DRAG_DROP_DRAWING:
            spatialDisplay.drawEventAreas(false);
            break;

        case DRAW_BY_AREA:
            // Make sure this geometry can be used in select by area
            // operations.
            String tableName = args[0];
            String displayName = args[1];

            // Unload the resource if it exists.
            if (isGeometryDisplayResourceLoaded()) {
                unloadGeometryDisplayResource();
            }

            try {
                getGeometryDisplay(tableName, displayName);
                addGeometryDisplayResourceToPerspective();

                // Check if there is a geometry displayed to use for
                // select-by-area. If not then do not complete
                // the loading of the select-by-area mouse handler.
                if (isGeometryDisplayResourceLoaded()) {
                    setCursor(SpatialViewCursorTypes.DRAW_CURSOR);
                    notifySelectByAreaInitiated();
                } else {
                    loadMouseHandler = false;
                    drawingActionComplete();
                }
            } catch (VizException e) {
                loadMouseHandler = false;
                statusHandler.error("In SpatialView.setMouseHandler():", e);
                drawingActionComplete();
            }

            break;

        default:
            statusHandler
                    .debug("SpatialView.setMouseHandler(): Unrecognized Mouse Handler");
            loadMouseHandler = false;
            break;
        }

        if (loadMouseHandler) {
            IInputHandler mouseHandler = mouseFactory.getMouseHandler(
                    mouseHandlerType, args);

            if (mouseHandler != null) {
                setMouseHandler(mouseHandler);
            }
        }

    }

    @Override
    public void modifyShape(HazardServicesDrawingAction drawingAction,
            HazardServicesAppBuilder appBuilder,
            HazardServicesMessageHandler messageHandler) {

        switch (drawingAction) {
        case ADD_POINT:
            IInputHandler mouseHandler = mouseFactory.getMouseHandler(
                    HazardServicesMouseHandlers.SINGLE_SELECTION,
                    new String[] {});
            SelectionHandler addMouseHandler = (SelectionHandler) mouseHandler;
            addMouseHandler.addPoint();
            break;

        case DELETE_POINT:
            mouseHandler = mouseFactory.getMouseHandler(
                    HazardServicesMouseHandlers.SINGLE_SELECTION,
                    new String[] {});

            SelectionHandler deleteMouseHandler = (SelectionHandler) mouseHandler;
            deleteMouseHandler.deletePoint();
            break;

        case MOVE_ELEMENT:
            mouseHandler = mouseFactory.getMouseHandler(
                    HazardServicesMouseHandlers.SINGLE_SELECTION,
                    new String[] {});
            SelectionHandler moveMouseHandler = (SelectionHandler) mouseHandler;
            moveMouseHandler.setMoveEntireElement();
            break;

        case COPY_ELEMENT:
            break;

        default:
            statusHandler
                    .debug("SpatialView.modifyShape(): Unrecognized drawing action.");
            break;
        }
    }

    @Override
    public void frameChanged(IDescriptor descriptor, DataTime oldTime,
            DataTime newTime) {
        FramesInfo framesInfo = spatialDisplay.getDescriptor().getFramesInfo();

        if (newTime != null) {
            long caveNewTime = newTime.getRefTime().getTime();

            if (caveNewTime != currentFrameTime) {
                if (framesInfo != null) {
                    fireAction(new SpatialDisplayAction("FrameChanged",
                            framesInfo));
                }

                currentFrameTime = caveNewTime;
            }
        }
    }

    /**
     * Manage the view frames based on the selected time.
     * 
     * @param selectedTime
     *            the selected time to try to match a view frame to.
     * @return
     */
    @Override
    public void manageViewFrames(Date selectedTime) {
        long selectedTimeMS = selectedTime.getTime();

        int frameIndex = 0;
        long diff;
        long smallestDiff = Long.MAX_VALUE;

        // The selected time to switch CAVE to...
        AbstractEditor editor = ((AbstractEditor) VizWorkbenchManager
                .getInstance().getActiveEditor());
        IDescriptor idesc = editor.getActiveDisplayPane().getDescriptor();
        IMapDescriptor descriptor = null;

        if (idesc instanceof IMapDescriptor) {
            descriptor = (IMapDescriptor) idesc;
        }

        int numberOfFrames = descriptor.getFramesInfo().getFrameCount();
        // Try to find the closest valid time

        if (numberOfFrames > 0) {
            DataTime[] availableDataTimes = descriptor.getFramesInfo()
                    .getFrameTimes();

            for (DataTime time : availableDataTimes) {
                Calendar cal = time.getValidTime();
                diff = Math.abs(cal.getTimeInMillis() - selectedTimeMS);

                // Exact Match.
                if (diff == 0) {
                    break;
                }

                if (smallestDiff < diff) {
                    frameIndex--;
                    break;
                }

                smallestDiff = diff;
                frameIndex++;
            }

            if (frameIndex >= numberOfFrames) {
                frameIndex--;
            }

            FramesInfo newFramesInfo;

            if (availableDataTimes.length == 1) {
                DataTime newDataTime = new DataTime(new Date(selectedTimeMS));
                newFramesInfo = new FramesInfo(new DataTime[] { newDataTime },
                        0);
                currentFrameTime = selectedTimeMS;
            } else {
                newFramesInfo = new FramesInfo(frameIndex);
                currentFrameTime = availableDataTimes[frameIndex].getRefTime()
                        .getTime();
            }

            descriptor.setFramesInfo(newFramesInfo);

            issueRefresh();
        }

    }

    /**
     * Sets the mouse handler.
     * 
     * @param mouseHandler
     *            The mouse handler to load.
     * @return
     */

    private void setMouseHandler(IInputHandler mouseHandler) {

        if (this.currentMouseHandler != null) {
            unSetMouseHandler(this.currentMouseHandler);
        }

        this.currentMouseHandler = mouseHandler;
        spatialDisplay.setMouseHandler(mouseHandler);
    }

    /**
     * Removes the specified mouse handler from the current editor.
     * 
     * @param mouseHandler
     *            the mouseHandler to set
     */
    private void unSetMouseHandler(IInputHandler mouseHandler) {
        this.currentMouseHandler = null;
    }

    /**
     * Saves the current mouse handler for future retrieval.
     */
    void saveCurrentMouseHandler() {
        if (currentMouseHandler != null) {
            unSetMouseHandler(currentMouseHandler);
        }
    }

    /**
     * Unregisters the current mouse handler from the active editor.
     * 
     * @param
     * @return
     */
    @Override
    public void unregisterCurrentMouseHandler() {
        if (currentMouseHandler != null) {
            AbstractEditor editor = ((AbstractEditor) VizWorkbenchManager
                    .getInstance().getActiveEditor());

            if (editor != null && currentMouseHandler != null) {
                editor.unregisterMouseHandler(currentMouseHandler);
                currentMouseHandler = null;
            }
        }
    }

    /**
     * Remove listeners.
     */
    private void removeListeners() {

        // Dispose of the select-by-area maps pulldown action, so as
        // to remove its listeners.
        if (selectByAreaMapsPulldownAction != null) {
            ((SelectByAreaMapsPulldownAction) selectByAreaMapsPulldownAction)
                    .dispose();
        }

    }

    /**
     * Fire an action event to its listener.
     * 
     * @param action
     *            Action.
     */
    private void fireAction(SpatialDisplayAction action) {
        presenter.fireAction(action);
    }

    /**
     * Retrieve the descriptor of the current perspective.
     * 
     * @return The descriptor of the current perspective.
     */
    private IPerspectiveDescriptor getCurrentPerspectiveDescriptor() {
        return VizWorkbenchManager.getInstance().getActiveEditor().getSite()
                .getPage().getPerspective();
    }

    /**
     * Adds the draw by area viz resource to the CAVE editor.
     * 
     * @return
     */
    @Override
    public void addGeometryDisplayResourceToPerspective() {

        AbstractEditor editor = ((AbstractEditor) VizWorkbenchManager
                .getInstance().getActiveEditor());

        IDescriptor idesc = editor.getActiveDisplayPane().getDescriptor();

        if (idesc instanceof IMapDescriptor) {

            IMapDescriptor desc = (IMapDescriptor) idesc;

            // This is ugly, but remove all instances of IHISDbMapResource
            // resources from the list.
            ResourceList rescList = desc.getResourceList();

            for (ResourcePair pair : rescList) {
                if (pair.getResource() instanceof SelectByAreaDbMapResource) {
                    rescList.removeRsc(pair.getResource());
                }
            }

            if (selectableGeometryDisplay != null) {

                rescList = desc.getResourceList();

                if (rescList.containsRsc(selectableGeometryDisplay)) {
                    rescList.removeRsc(selectableGeometryDisplay);
                }

                desc.getResourceList().add(selectableGeometryDisplay);

            }

        }

        return;
    }

    /**
     * Returns a new instance of the draw by area viz resource.
     * 
     * @param table_name
     *            The geo database table to retrieve overlay data for
     * @param mapName
     *            The name of the map (to display in the legend)
     * @return A new instance of the draw by area resource.
     * 
     */
    public SelectByAreaDbMapResource getGeometryDisplay(String table_name,
            String mapName) throws VizException {
        // Create the resource data class for the geo database
        // map resource.
        SelectByAreaDbMapResourceData resourceData = new SelectByAreaDbMapResourceData();
        resourceData.setTable(table_name);
        resourceData.setMapName(mapName);
        resourceData.setGeomField("the_geom");

        // Filter by the CWA. We should try to get this
        // identifier dynamically.
        // Some overlays do not have a cwa field.
        String siteID = LocalizationManager.getInstance().getCurrentSite();
        resourceData.setConstraints(new String[] { "cwa = '" + siteID + "'" });

        // Create the Viz Resource to display in CAVE
        selectableGeometryDisplay = resourceData.construct(
                new LoadProperties(), null);
        selectableGeometryDisplay.registerListener(this);

        return selectableGeometryDisplay;
    }

    /**
     * Removes the geometry viz resource from the CAVE editor
     * 
     * @param
     * @return
     */
    public void removeGeometryDisplay() {

        if (selectableGeometryDisplay != null) {
            AbstractEditor editor = ((AbstractEditor) VizWorkbenchManager
                    .getInstance().getActiveEditor());

            IDescriptor idesc = editor.getActiveDisplayPane().getDescriptor();
            IMapDescriptor desc = null;

            if (idesc instanceof IMapDescriptor) {
                desc = (IMapDescriptor) idesc;

                try {
                    desc.getResourceList().removeRsc(selectableGeometryDisplay);
                } catch (Exception e) {
                    // ignore
                }
            }

            selectableGeometryDisplay = null;
        }

    }

    /**
     * Returns the current instance of the draw by area resoure.
     * 
     * @return the selectableGeometryDisplay
     */
    @Override
    public SelectByAreaDbMapResource getSelectableGeometryDisplay() {
        return selectableGeometryDisplay;
    }

    /**
     * Whenever the user has completed a drawing action (i.e, the user has
     * completed a polygon, a point, a line, or a select by area, then notify
     * the toolbar to reset to the default selected event editing mode.
     */
    @Override
    public void drawingActionComplete() {
        // Unload the resource if it exists.
        if (isGeometryDisplayResourceLoaded()) {
            unloadGeometryDisplayResource();
        }

        /*
         * Notify the view of the completion of the drawing action. NOTE: This
         * should not be here; the spatial display should, when it is made part
         * of the view, notify the view instead.
         */
        notifyDrawingActionComplete();
    }

    /**
     * Tests whether or not the draw by area resource is loaded.
     * 
     * @param
     * @return true - the resource is loaded, false - the resource is not loaded
     */
    public boolean isGeometryDisplayResourceLoaded() {
        AbstractEditor editor = ((AbstractEditor) VizWorkbenchManager
                .getInstance().getActiveEditor());

        IDescriptor idesc = editor.getActiveDisplayPane().getDescriptor();
        IMapDescriptor desc = null;

        if (selectableGeometryDisplay != null) {
            if (idesc instanceof IMapDescriptor) {
                desc = (IMapDescriptor) idesc;

                ResourceList rescList = desc.getResourceList();

                return rescList.containsRsc(selectableGeometryDisplay);
            }
        }

        return false;

    }

    /**
     * Unloads the selected geometry display resource if it is loaded in CAVE.
     */
    public void unloadGeometryDisplayResource() {
        if (selectableGeometryDisplay != null) {
            AbstractEditor editor = ((AbstractEditor) VizWorkbenchManager
                    .getInstance().getActiveEditor());
            IDescriptor idesc = editor.getActiveDisplayPane().getDescriptor();
            IMapDescriptor desc = null;

            if (idesc instanceof IMapDescriptor) {
                desc = (IMapDescriptor) idesc;
                ResourceList rescList = desc.getResourceList();
                rescList.removeRsc(selectableGeometryDisplay);
            }
        }
    }

    /**
     * Checks to determine if a geometry overlay needs to be loaded for a
     * selected event. If multiple geometry overlays need to be loaded this
     * currently only loads the first overlay.
     * 
     * @param
     * @return
     */
    @Override
    public void loadGeometryOverlayForSelectedEvent(String eventIDs_json) {
        /*
         * Need to determine if the selected event was based on a geometry read
         * from the database
         */
        DictList eventIDs = DictList.getInstance(eventIDs_json);

        for (int i = 0; i < eventIDs.size(); ++i) {
            final String eventID = eventIDs.getDynamicallyTypedValue(i);
            String selectedEventJSON = presenter.getEvents(eventID);

            DictList selectedEventDictList = DictList
                    .getInstance(selectedEventJSON);
            Dict selectedEventDict = selectedEventDictList
                    .getDynamicallyTypedValue(0);

            if (selectedEventDict.containsKey("geometryReference")) {

                /*
                 * This was an event generated from a union of one or more
                 * geometries.
                 */
                // Make sure that the geoLayer is not already loaded.
                final String tableName = selectedEventDict
                        .getDynamicallyTypedValue("geometryReference");
                final String displayName = selectedEventDict
                        .getDynamicallyTypedValue("geometryMapName");

                // Launch the select-by-area layer. Give it the table name
                // from
                // above as well as
                // the hazard polygon(s).
                // Unload the resource if it exists.
                if (isGeometryDisplayResourceLoaded()) {
                    unloadGeometryDisplayResource();
                }

                // Need to make sure that we add the geometry display
                // after the old one has been removed...
                VizApp.runAsync(new Runnable() {

                    @Override
                    public void run() {

                        setMouseHandler(
                                HazardServicesMouseHandlers.DRAW_BY_AREA,
                                tableName, displayName, eventID);
                    }
                });

                break;
            }

        }

    }

    /**
     * This function is called when the selected geometry display resource is
     * disposed. This will happen if the user right-clicks on the legend item
     * for this resource and selects the 'unload' option. This will also be
     * called when this resource is removed from the AbstractVizResource list
     * using the removeRsc method.
     */
    public void dbMapResourceUnloaded() {
        selectableGeometryDisplay = null;
        drawingActionComplete();
    }

    /**
     * Sets the mouse cursor to the specified type.
     * 
     * @param cursorType
     *            The type of cursor to set.
     */
    @Override
    public void setCursor(SpatialViewCursorTypes cursorType) {
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getShell();
        shell.setCursor(cursorsForCursorTypes.get(cursorType));
    }

    /**
     * Determine whether this cursor is the current cursor.
     * 
     * @param cursorType
     *            The type of cursor against which to check.
     * @return True if the current cursor is of the specified type, false
     *         otherwise.
     */
    public boolean isCurrentCursor(SpatialViewCursorTypes cursorType) {
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getShell();
        return (shell.getCursor() == cursorsForCursorTypes.get(cursorType));
    }

    /**
     * @return An instance of the spatial display.
     */
    @Override
    public ToolLayer getSpatialDisplay() {
        return spatialDisplay;
    }

    /**
     * @param
     * @return
     */
    @Override
    public void disposed(AbstractVizResource<?, ?> rsc) {
        if (rsc instanceof SelectByAreaDbMapResource) {
            dbMapResourceUnloaded();
        }
    }

    /**
     * Sets the enabled state of the undo button.
     * 
     * @param undoFlag
     *            True - enabled, False - disabled
     * 
     * @return
     */
    @Override
    public void setUndoEnabled(final Boolean undoFlag) {
        /*
         * undoCommandAction can be null until the Console is initialized
         */
        if (this.undoCommandAction != null) {
            this.undoCommandAction.setEnabled(undoFlag);
        }
    }

    /**
     * Sets the enabled state of the redo button.
     * 
     * @param redoFlag
     *            True - enabled, False - disabled
     * 
     * @return
     */
    @Override
    public void setRedoEnabled(final Boolean redoFlag) {
        /*
         * redoCommandAction can be null until the Console is initialized
         */
        if (this.redoCommandAction != null) {
            this.redoCommandAction.setEnabled(redoFlag);
        }
    }
}

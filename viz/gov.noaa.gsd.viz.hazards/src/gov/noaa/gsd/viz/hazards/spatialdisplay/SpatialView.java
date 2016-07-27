/**
 * This software was developed and / or modified by the National Oceanic and
 * Atmospheric Administration (NOAA), Earth System Research Laboratory (ESRL),
 * Global Systems Division (GSD), Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.spatialdisplay;

import gov.noaa.gsd.common.utilities.IRunnableAsynchronousScheduler;
import gov.noaa.gsd.common.visuals.SpatialEntity;
import gov.noaa.gsd.viz.hazards.display.RCPMainUserInterfaceElement;
import gov.noaa.gsd.viz.hazards.spatialdisplay.entities.IEntityIdentifier;
import gov.noaa.gsd.viz.hazards.spatialdisplay.selectbyarea.SelectByAreaContext;
import gov.noaa.gsd.viz.hazards.spatialdisplay.selectbyarea.SelectByAreaDbMapResource;
import gov.noaa.gsd.viz.hazards.spatialdisplay.selectbyarea.SelectByAreaDbMapResourceData;
import gov.noaa.gsd.viz.hazards.toolbar.BasicAction;
import gov.noaa.gsd.viz.hazards.toolbar.PulldownAction;
import gov.noaa.gsd.viz.hazards.toolbar.SeparatorAction;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IPerspectiveDescriptor;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.GeometryType;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.core.IDisplayPane;
import com.raytheon.uf.viz.core.IExtent;
import com.raytheon.uf.viz.core.VizApp;
import com.raytheon.uf.viz.core.drawables.IDescriptor;
import com.raytheon.uf.viz.core.drawables.IRenderableDisplay;
import com.raytheon.uf.viz.core.drawables.ResourcePair;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.localization.LocalizationManager;
import com.raytheon.uf.viz.core.map.IMapDescriptor;
import com.raytheon.uf.viz.core.maps.rsc.DbMapResource;
import com.raytheon.uf.viz.core.maps.rsc.DbMapResourceData;
import com.raytheon.uf.viz.core.rsc.AbstractVizResource;
import com.raytheon.uf.viz.core.rsc.IDisposeListener;
import com.raytheon.uf.viz.core.rsc.LoadProperties;
import com.raytheon.uf.viz.core.rsc.ResourceList;
import com.raytheon.uf.viz.core.rsc.ResourceList.AddListener;
import com.raytheon.uf.viz.core.rsc.ResourceList.RemoveListener;
import com.raytheon.viz.ui.EditorUtil;
import com.raytheon.viz.ui.editor.AbstractEditor;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

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
 * Jul 18, 2013   1264     Chris.Golden      Added support for drawing lines and
 *                                           points.
 * Aug 04, 2013   1265     Bryon.Lawrence    Added support for undo/redo
 * Aug  9, 2013   1921     daniel.s.schaffer@noaa.gov  Support of replacement of JSON with POJOs
 * Aug 29, 2013   1921     bryon.lawrence    Updated loadGeometryOverlayForSelectedEvent
 *                                           to use Java event objects instead of
 *                                           JSON.
 * Nov 16, 2013  2166       daniel.s.schaffer@noaa.gov    Some tidying
 * Nov 27, 2013  1462      bryon.lawrence    Updated to support 
 *                                           display of hazard hatched areas.
 * Apr 09, 2014    2925    Chris.Golden      Changed to ensure that method is called
 *                                           within the UI thread.
 * Dec 05, 2014    4124    Chris.Golden      Changed to work with ObservedSettings.
 * Dec 15, 2014    3846    Tracy Hansen      Added ability to draw points back in
 * Dec 13, 2014 4959       Dan Schaffer Spatial Display cleanup and other bug fixes
 * Feb 03, 2015    3865    Chris.Cody        Check for valid Active Editor class
 * Feb 24, 2015 6499       Dan Schaffer      Disable drawing of point hazards
 * Feb 25, 2015 6600       Dan Schaffer      Fixed bug in spatial display centering
 * Feb 27, 2015 6000       Dan Schaffer      Improved centering behavior
 * Jun 24, 2015 6601       Chris.Cody        Change Create by Hazard Type display text
 * Jul 21, 2015 2921       Robert.Blum       Changes for multi panel displays.
 * Sep 09, 2015 6603       Chris.Cody        Added isSelectByAreaActive to track "by Area"
 *                                           selection state.
 * Mar 16, 2016 15676      Chris.Golden      Changed to make visual features work.
 *                                           Will be refactored to remove numerous
 *                                           existing kludges.
 * Mar 24, 2016 15676      Chris.Golden      Changed method that draws spatial entities
 *                                           to take another parameter.
 * Apr 27, 2016 18266      Chris.Golden      Added support for event-driven tools triggered
 *                                           by data layer changes.
 * Jun 06, 2016 19432      Chris.Golden      Added ability to draw lines and points.
 * Jun 23, 2016 19537      Chris.Golden      Removed storm-track-specific code.
 * Jul 25, 2016 19537      Chris.Golden      Extensively refactored as the move toward MVP
 *                                           compliance continues. Added Javadoc comments,
 *                                           continued separation of concerns between view,
 *                                           presenter, display, and mouse handlers.
 * Jul 27, 2016 19924      Chris.Golden      Removed code related to monitoring data layer
 *                                           changes, as it belongs in the app builder.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class SpatialView implements
        ISpatialView<Action, RCPMainUserInterfaceElement>, IDisposeListener {

    // Private Static Constants

    /**
     * Suffix to append to the legend text for a select-by-area layer.
     */
    private static final String SELECT_BY_AREA_LEGEND_SUFFIX = " (Select By Area)";

    /**
     * Scheduler to be used to make runnables get executed on the main thread.
     * For now, the main thread is the UI thread; when this is changed, this
     * will be rendered obsolete, as at that point there will need to be a
     * blocking queue of {@link Runnable} instances available to allow the new
     * worker thread to be fed jobs. At that point, this should be replaced with
     * an object that enqueues the <code>Runnable</code>s, probably a singleton
     * that may be accessed by the various components in
     * gov.noaa.gsd.viz.hazards and perhaps elsewhere.
     */
    @Deprecated
    private static final IRunnableAsynchronousScheduler RUNNABLE_ASYNC_SCHEDULER = new IRunnableAsynchronousScheduler() {

        @Override
        public void schedule(Runnable runnable) {

            /*
             * Since the UI thread is currently the thread being used for nearly
             * everything, just run any asynchronous tasks there.
             */
            VizApp.runAsync(runnable);
        }
    };

    /**
     * For now, limit the overlays the user can use in Select by Area
     * operations. Not all overlays from the Maps menu can be used in Select By
     * Area. Also, some do not have a CWA column. The CWA column is used to
     * limit Select by Area to geometries contained within the CWA.
     */
    private static final Set<String> ACCEPTABLE_MAP_OVERLAYS;

    /*
     * Initialize the acceptable map overlays set.
     */
    static {
        Set<String> set = Sets.newHashSet();
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

    // Private Enumerated Types

    /**
     * Input modes.
     */
    private enum InputMode {
        SELECT_OR_MODIFY("Select event", "moveAndSelect.png"), DRAW_POINT(
                "Draw points", "drawPoint.png"), DRAW_LINE("Draw path",
                "drawPath.png"), DRAW_POLYGON("Draw polygon", "drawPolygon.png"), DRAW_FREEHAND_POLYGON(
                "Draw freehand polygon", "drawFreehandPolygon.png"), EDIT_POLYGON(
                "Edit polygon", "editPolygon.png"), EDIT_FREEHAND_POLYGON(
                "Edit polygon freehand", "editPolygonFreeHand.png");

        // Private Variables

        /**
         * Text description.
         */
        private final String description;

        /**
         * Name of the file holding the icon to use to represent this mode in
         * the toolbar.
         */
        private final String iconFile;

        // Private Constructors

        /**
         * Construct a standard instance.
         * 
         * @param description
         *            Text description.
         * @param iconFile
         *            Name of the file holding the icon to use to represent this
         *            mode in the toolbar.
         */
        private InputMode(String description, String iconFile) {
            this.description = description;
            this.iconFile = iconFile;
        }

        // Public Methods

        /**
         * Get the text description.
         * 
         * @return Text description.
         */
        public String getDescription() {
            return description;
        }

        /**
         * Get the name of the file holding the icon to use to represent this
         * mode in the toolbar.
         * 
         * @return Icon file name.
         */
        public String getIconFile() {
            return iconFile;
        }
    };

    // Private Classes

    /**
     * Input mode.
     */
    private class InputModeAction extends BasicAction {

        // Private Constants

        /**
         * Input mode.
         */
        private final InputMode inputMode;

        // Public Constructors

        /**
         * Construct a standard instance.
         * 
         * @param inputMode
         *            Input mode.
         */
        public InputModeAction(InputMode inputMode) {
            super("", inputMode.getIconFile(), Action.AS_CHECK_BOX, inputMode
                    .getDescription());
            this.inputMode = inputMode;
        }

        // Public Methods

        @Override
        public void run() {

            /*
             * Do nothing if the spatial display is gone, since this invocation
             * may be the result of the application closing.
             */
            if (spatialDisplay == null) {
                return;
            }

            /*
             * Determine whether or not the new-shape-being-drawn flag should be
             * set.
             */
            switch (inputMode) {
            case DRAW_POLYGON:
            case DRAW_LINE:
            case DRAW_POINT:
            case DRAW_FREEHAND_POLYGON:
                drawingOfNewShapeInProgress = true;
                break;
            default:
                drawingOfNewShapeInProgress = false;
                break;
            }

            /*
             * Uncheck the other input mode buttons.
             */
            for (Map.Entry<InputMode, InputModeAction> entry : actionsForInputModes
                    .entrySet()) {
                if (entry.getKey() != inputMode) {
                    entry.getValue().setChecked(false);
                }
            }

            /*
             * Unload the draw-by-area resource if it exists.
             */
            unloadSelectByAreaVizResourceFromPerspective();

            /*
             * Tell the spatial display to use the appropriate input handler.
             */
            switch (inputMode) {
            case SELECT_OR_MODIFY:
                spatialDisplay
                        .setCurrentInputHandlerToNonDrawing(InputHandlerType.SINGLE_SELECTION);
                break;
            case DRAW_POINT:
                spatialDisplay.setCurrentInputHandlerToDrawing(
                        InputHandlerType.VERTEX_DRAWING, GeometryType.POINT);
                break;
            case DRAW_LINE:
                spatialDisplay.setCurrentInputHandlerToDrawing(
                        InputHandlerType.VERTEX_DRAWING, GeometryType.LINE);
                break;
            case DRAW_POLYGON:
                spatialDisplay.setCurrentInputHandlerToDrawing(
                        InputHandlerType.VERTEX_DRAWING, GeometryType.POLYGON);
                break;
            case DRAW_FREEHAND_POLYGON:
                spatialDisplay
                        .setCurrentInputHandlerToDrawing(
                                InputHandlerType.FREEHAND_DRAWING,
                                GeometryType.POLYGON);
                break;
            case EDIT_POLYGON:
                spatialDisplay.setCurrentInputHandlerToDrawing(
                        InputHandlerType.VERTEX_DRAWING, GeometryType.LINE);
                break;
            case EDIT_FREEHAND_POLYGON:
                spatialDisplay.setCurrentInputHandlerToDrawing(
                        InputHandlerType.FREEHAND_DRAWING, GeometryType.LINE);
                break;
            }
        }
    }

    /**
     * Standard spatial display menu or toolbar action.
     */
    private class BasicSpatialAction extends BasicAction {

        /**
         * Runnable to be executed for this action.
         */
        private final Runnable runnable;

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
         * @param runnable
         *            Runnable to be executed for this action.
         */
        private BasicSpatialAction(String text, String iconFileName, int style,
                String toolTipText, Runnable runnable) {
            super(text, iconFileName, style, toolTipText);
            this.runnable = runnable;
        }

        @Override
        public void run() {
            RUNNABLE_ASYNC_SCHEDULER.schedule(runnable);
        }
    }

    /**
     * Maps for select-by-area pulldown menu action.
     */
    private class SelectByAreaMapsPulldownAction extends PulldownAction {

        /**
         * Listener for menu item invocations.
         */
        private final SelectionListener listener = new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent event) {
                MenuItem item = (MenuItem) event.widget;
                loadSelectByAreaVizResourceAndInputHandler(new SelectByAreaContext(
                        null,
                        null,
                        (String) item
                                .getData(HazardConstants.GEOMETRY_REFERENCE_KEY),
                        (String) item
                                .getData(HazardConstants.GEOMETRY_MAP_NAME_KEY)));
            }
        };

        /**
         * Construct a standard instance.
         */
        private SelectByAreaMapsPulldownAction() {
            super("");
            setImageDescriptor(getImageDescriptorForFile("mapsForSelectByArea.png"));
            setToolTipText("Maps for select by area");

            /*
             * Enable or disable the action based upon the currently loaded
             * maps, building a list of said maps that are appropriate for
             * select by area operations.
             */
            notifyResourceListChanged();
        }

        @Override
        protected Menu doGetMenu(Control parent, Menu menu) {

            /*
             * If the menu has not yet been created, do so now; otherwise,
             * delete its contents.
             */
            if (menu == null) {
                menu = new Menu(parent);
            } else {
                for (MenuItem item : menu.getItems()) {
                    item.dispose();
                }
            }

            /*
             * Load the current editor.
             */
            IDescriptor descriptor = null;
            AbstractEditor abstractEditor = EditorUtil
                    .getActiveEditorAs(AbstractEditor.class);
            if (abstractEditor != null) {
                IDisplayPane displayPane = abstractEditor
                        .getActiveDisplayPane();
                if (displayPane != null) {
                    descriptor = displayPane.getDescriptor();
                }
            }
            /*
             * Load the list of viz resources associated with it, and iterate
             * through the resource pairs, looking for those that are database
             * map resources. For each of these that is visible, add a menu
             * item.
             */
            if ((descriptor != null) && (descriptor instanceof IMapDescriptor)) {
                IMapDescriptor mapDescriptor = (IMapDescriptor) descriptor;
                ResourceList resourceList = mapDescriptor.getResourceList();
                for (ResourcePair pair : resourceList) {
                    if (pair.getResource() instanceof DbMapResource) {

                        /*
                         * For now, just take the first one.
                         */
                        DbMapResource overlayResource = (DbMapResource) pair
                                .getResource();
                        DbMapResourceData resourceData = overlayResource
                                .getResourceData();
                        String mapName = resourceData.getMapName();
                        String tableName = resourceData.getTable();

                        /*
                         * Make sure that this is an acceptable overlay table;
                         * if it is, create a menu item for it.
                         */
                        if (ACCEPTABLE_MAP_OVERLAYS.contains(tableName)) {

                            /*
                             * Create the menu item, giving it a corresponding
                             * action if the map is visible, or just disabling
                             * it if not.
                             */
                            MenuItem item = new MenuItem(menu, SWT.PUSH);
                            item.setText(mapName);
                            if (overlayResource.getProperties().isVisible()) {
                                item.setData(
                                        HazardConstants.GEOMETRY_MAP_NAME_KEY,
                                        mapName + SELECT_BY_AREA_LEGEND_SUFFIX);
                                item.setData(
                                        HazardConstants.GEOMETRY_REFERENCE_KEY,
                                        tableName);
                                item.addSelectionListener(listener);
                            } else {
                                item.setEnabled(false);
                            }
                        }
                    }
                }
            }

            return menu;
        }

        /**
         * Respond to a possible change in the available maps for select by area
         * operations.
         */
        public void notifyResourceListChanged() {

            /*
             * Load the list of viz resources associated with it, and iterate
             * through the resource pairs, looking for those that are database
             * map resources. If at least one is an acceptable resource for
             * select by area operations, this action should be enabled.
             */
            boolean enable = false;

            /*
             * Only do this if there is an active editor.
             */
            IDescriptor descriptor = null;
            AbstractEditor abstractEditor = EditorUtil
                    .getActiveEditorAs(AbstractEditor.class);
            if (abstractEditor != null) {
                for (IDisplayPane displayPane : Arrays.asList(abstractEditor
                        .getDisplayPanes())) {
                    descriptor = displayPane.getDescriptor();
                    if ((descriptor != null)
                            && (descriptor instanceof IMapDescriptor)) {
                        IMapDescriptor mapDescriptor = (IMapDescriptor) descriptor;
                        ResourceList resourceList = mapDescriptor
                                .getResourceList();
                        for (ResourcePair pair : resourceList) {
                            if (pair.getResource() instanceof DbMapResource) {

                                /*
                                 * For now, just take the first one.
                                 */
                                String tableName = ((DbMapResource) pair
                                        .getResource()).getResourceData()
                                        .getTable();

                                /*
                                 * Make sure that this is an acceptable overlay
                                 * table.
                                 */
                                if (ACCEPTABLE_MAP_OVERLAYS.contains(tableName)) {
                                    enable = true;
                                    break;
                                }
                            }
                        }
                    }
                    if (enable) {
                        break;
                    }
                }
            }

            /*
             * Enable or disable this action based upon whether any maps were
             * found that may be used in select by area operations.
             */
            setEnabled(enable);
        }
    }

    // Private Variables

    /**
     * Resource list add listener, for detecting changes in the list of
     * resources currently displayed.
     */
    private final AddListener addListener = new AddListener() {

        @Override
        public void notifyAdd(final ResourcePair resourcePair)
                throws VizException {
            VizApp.runAsync(new Runnable() {

                @Override
                public void run() {
                    if (selectByAreaMapsPulldownAction != null) {
                        selectByAreaMapsPulldownAction
                                .notifyResourceListChanged();
                    }
                }
            });
        }
    };

    /**
     * Resource list remove listener, for detecting changes in the list of
     * resources currently displayed.
     */
    private final RemoveListener removeListener = new RemoveListener() {
        @Override
        public void notifyRemove(final ResourcePair resourcePair)
                throws VizException {
            VizApp.runAsync(new Runnable() {

                @Override
                public void run() {
                    if (selectByAreaMapsPulldownAction != null) {
                        selectByAreaMapsPulldownAction
                                .notifyResourceListChanged();
                    }
                }
            });
        }
    };

    /**
     * CAVE resource layer used as spatial display.
     */
    private SpatialDisplay spatialDisplay;

    /**
     * Presenter.
     */
    private SpatialPresenter presenter;

    /**
     * Undo command action.
     */
    private Action undoCommandAction;

    /**
     * Redo command action.
     */
    private Action redoCommandAction;

    /**
     * Add to selected toggle action.
     */
    private Action addNewEventToSelectedToggleAction;

    /**
     * Map of input modes to their corresponding actions.
     */
    private final EnumMap<InputMode, InputModeAction> actionsForInputModes = new EnumMap<>(
            InputMode.class);

    /**
     * Maps for select by area pulldown action.
     */
    private SelectByAreaMapsPulldownAction selectByAreaMapsPulldownAction;

    /**
     * Add geometry to selected event action.
     */
    private Action addNewGeometryToSelectedEventToggleAction;

    /**
     * Flag indicating whether the drawing of a new shape is in progress.
     */
    private boolean drawingOfNewShapeInProgress;

    /**
     * Select-by-area viz resource currently in use, if any.
     */
    private SelectByAreaDbMapResource selectByAreaVizResource;

    /**
     * Flag indicating whether or not select-by-area input mode is active.
     */
    private boolean selectByAreaActive = false;

    /**
     * Currently selected spatial entity identifiers.
     */
    private final Set<IEntityIdentifier> selectedSpatialEntityIdentifiers = new HashSet<>();

    /**
     * Currently selected time.
     */
    private Date selectedTime;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param spatialDisplay
     *            Spatial display.
     */
    public SpatialView(SpatialDisplay spatialDisplay) {
        this.spatialDisplay = spatialDisplay;
        spatialDisplay.setSpatialView(this);
    }

    // Public Methods

    @Override
    public final void initialize(SpatialPresenter presenter) {
        this.presenter = presenter;

        /*
         * Ensure any previously loaded select-by-area viz resources are
         * removed.
         */
        updatePerspectiveUseOfSelectByAreaVizResource();

        /*
         * Add the spatial display and to the CAVE editor's resource list, and
         * create listeners for the addition and removal of resource layers.
         */
        AbstractEditor abstractEditor = EditorUtil
                .getActiveEditorAs(AbstractEditor.class);
        if (abstractEditor != null) {
            for (IDisplayPane displayPane : Arrays.asList(abstractEditor
                    .getDisplayPanes())) {
                IDescriptor descriptor = displayPane.getDescriptor();
                if ((descriptor != null)
                        && (descriptor instanceof IMapDescriptor)) {
                    descriptor.getResourceList().add(spatialDisplay);
                }
            }
            createResourceListeners(abstractEditor);
        }
    }

    @Override
    public final void dispose() {

        AbstractEditor abstractEditor = EditorUtil
                .getActiveEditorAs(AbstractEditor.class);

        /*
         * Unload from all panes.
         */
        if (abstractEditor != null) {
            for (IDisplayPane displayPane : Arrays.asList(abstractEditor
                    .getDisplayPanes())) {
                for (ResourcePair rp : displayPane.getDescriptor()
                        .getResourceList()) {
                    if (rp.getResource() instanceof SpatialDisplay) {
                        SpatialDisplay display = (SpatialDisplay) rp
                                .getResource();
                        display.unload();
                        displayPane.getDescriptor().getResourceList()
                                .remove(rp);
                    }
                }
            }
        }
        spatialDisplay = null;
        unloadSelectByAreaVizResourceFromPerspective();
    }

    @Override
    public void centerAndZoomDisplay(List<Coordinate> hull, Coordinate center) {

        /*
         * Do nothing unless the center has been supplied.
         */
        if (center != null) {

            /*
             * Get the current perspective; if it is anything but GFE, adjust
             * the center and zoom.
             */
            String perspectiveID = getCurrentPerspectiveDescriptor().getId();
            double[] centerAsArray = new double[] { center.x, center.y };
            AbstractEditor abstractEditor = EditorUtil
                    .getActiveEditorAs(AbstractEditor.class);
            if ((abstractEditor != null)
                    && (perspectiveID.equals("GFE") == false)) {
                for (IDisplayPane pane : Arrays.asList(abstractEditor
                        .getDisplayPanes())) {
                    IRenderableDisplay display = pane.getRenderableDisplay();
                    if (isHullWithinDisplay(hull, display) == false) {
                        double zoom = display.getZoom();
                        display.getExtent().reset();
                        display.recenter(centerAsArray);
                        display.zoom(zoom);
                    }
                }
            }
        }
    }

    @Override
    public void loadSelectByAreaVizResourceAndInputHandler(
            final SelectByAreaContext context) {

        /*
         * Unload any currently loaded select-by-area viz resource.
         */
        if (isSelectByAreaVizResourceLoaded()) {
            unloadSelectByAreaVizResourceFromPerspective();
        }

        /*
         * Get the select-by-area input handler, but do it asynchronously so as
         * to ensure that the old select-by-area viz resource has been fully
         * unloaded first.
         */
        VizApp.runAsync(new Runnable() {

            @Override
            public void run() {

                /*
                 * Attempt to load the new select-by-area viz resource.
                 */
                boolean loadInputHandler = true;
                try {

                    /*
                     * Create the select-by-area viz resource and add it to the
                     * display.
                     */
                    getSelectByAreaVizResource(context.getDatabaseTableName(),
                            context.getLegend());
                    updatePerspectiveUseOfSelectByAreaVizResource();

                    /*
                     * Check if the action above resulted in the display of a
                     * viz resource for select-by-area. If not then do not
                     * complete the loading of the select-by-area input handler.
                     */
                    if (isSelectByAreaVizResourceLoaded()) {
                        notifySelectByAreaInitiated();
                    } else {
                        loadInputHandler = false;
                        handleUserResetOfInputMode();
                    }
                } catch (VizException e) {
                    loadInputHandler = false;
                    statusHandler
                            .error("SpatialView.requestSelectByAreaInputHandler(): "
                                    + "Error loading select-by-area input handler: ",
                                    e);
                    handleUserResetOfInputMode();
                }

                /*
                 * Load the input handler if appropriate.
                 */
                if (loadInputHandler) {
                    spatialDisplay.setCurrentInputHandlerToSelectByArea(
                            selectByAreaVizResource,
                            context.getSelectedGeometries(),
                            context.getIdentifier());
                }
            }
        });
    }

    @Override
    public void setSelectedTime(Date selectedTime) {
        this.selectedTime = selectedTime;
    }

    @Override
    public final List<? extends Action> contributeToMainUI(
            RCPMainUserInterfaceElement type) {
        if (type == RCPMainUserInterfaceElement.TOOLBAR) {

            /*
             * Create the actions.
             */
            undoCommandAction = new BasicSpatialAction("", "undo.png",
                    Action.AS_PUSH_BUTTON, "Undo", new Runnable() {

                        @Override
                        public void run() {
                            presenter.handleUndoCommand();
                        }
                    });
            undoCommandAction.setEnabled(false);
            redoCommandAction = new BasicSpatialAction("", "redo.png",
                    Action.AS_PUSH_BUTTON, "Redo", new Runnable() {

                        @Override
                        public void run() {
                            presenter.handleRedoCommand();
                        }
                    });
            redoCommandAction.setEnabled(false);
            addNewEventToSelectedToggleAction = new BasicSpatialAction("",
                    "addToSelected.png", Action.AS_CHECK_BOX,
                    "Add New Pending to Selected", new Runnable() {

                        @Override
                        public void run() {
                            presenter
                                    .handleToggleAddNewEventToSelectedSet(addNewEventToSelectedToggleAction
                                            .isChecked());
                        }
                    });
            InputModeAction moveAndSelectChoiceAction = new InputModeAction(
                    InputMode.SELECT_OR_MODIFY);
            moveAndSelectChoiceAction.setChecked(true);
            actionsForInputModes.put(InputMode.SELECT_OR_MODIFY,
                    moveAndSelectChoiceAction);
            InputModeAction drawVertexBasedPolygonChoiceAction = new InputModeAction(
                    InputMode.DRAW_POLYGON);
            actionsForInputModes.put(InputMode.DRAW_POLYGON,
                    drawVertexBasedPolygonChoiceAction);
            InputModeAction drawFreehandPolygonChoiceAction = new InputModeAction(
                    InputMode.DRAW_FREEHAND_POLYGON);
            actionsForInputModes.put(InputMode.DRAW_FREEHAND_POLYGON,
                    drawFreehandPolygonChoiceAction);
            InputModeAction drawVertexPathChoiceAction = new InputModeAction(
                    InputMode.DRAW_LINE);
            actionsForInputModes.put(InputMode.DRAW_LINE,
                    drawVertexPathChoiceAction);
            InputModeAction drawPointChoiceAction = new InputModeAction(
                    InputMode.DRAW_POINT);
            actionsForInputModes.put(InputMode.DRAW_POINT,
                    drawPointChoiceAction);
            InputModeAction editVertexBasedPolygonChoiceAction = new InputModeAction(
                    InputMode.EDIT_POLYGON);
            editVertexBasedPolygonChoiceAction.setEnabled(false);
            actionsForInputModes.put(InputMode.EDIT_POLYGON,
                    editVertexBasedPolygonChoiceAction);
            InputModeAction editFreehandVertexBasedPolygonChoiceAction = new InputModeAction(
                    InputMode.EDIT_FREEHAND_POLYGON);
            editFreehandVertexBasedPolygonChoiceAction.setEnabled(false);
            actionsForInputModes.put(InputMode.EDIT_FREEHAND_POLYGON,
                    editFreehandVertexBasedPolygonChoiceAction);
            selectByAreaMapsPulldownAction = new SelectByAreaMapsPulldownAction();

            addNewGeometryToSelectedEventToggleAction = new BasicSpatialAction(
                    "", "addGeometryToSelected.png", Action.AS_CHECK_BOX,
                    "Add Geometry To Selected", new Runnable() {

                        @Override
                        public void run() {
                            presenter
                                    .handleToggleAddGeometryToSelectedEvent(addNewGeometryToSelectedEventToggleAction
                                            .isChecked());
                        }
                    });

            /*
             * Return the list of the actions created.
             */
            return Lists.newArrayList(undoCommandAction, redoCommandAction,
                    new SeparatorAction(), addNewEventToSelectedToggleAction,
                    new SeparatorAction(), moveAndSelectChoiceAction,
                    drawVertexBasedPolygonChoiceAction,
                    drawVertexPathChoiceAction, drawPointChoiceAction,
                    drawFreehandPolygonChoiceAction,
                    editVertexBasedPolygonChoiceAction,
                    editFreehandVertexBasedPolygonChoiceAction,
                    selectByAreaMapsPulldownAction, new SeparatorAction(),
                    addNewGeometryToSelectedEventToggleAction);
        }
        return Collections.emptyList();
    }

    @Override
    public void drawSpatialEntities(
            List<SpatialEntity<? extends IEntityIdentifier>> spatialEntities,
            Set<IEntityIdentifier> selectedSpatialEntityIdentifiers) {

        /*
         * Remember the current set of selected spatial entities.
         */
        this.selectedSpatialEntityIdentifiers.clear();
        this.selectedSpatialEntityIdentifiers
                .addAll(selectedSpatialEntityIdentifiers);

        /*
         * Draw the spatial entities.
         */
        spatialDisplay.drawSpatialEntities(spatialEntities,
                selectedSpatialEntityIdentifiers);
    }

    @Override
    public void disposed(AbstractVizResource<?, ?> rsc) {
        if (rsc instanceof SelectByAreaDbMapResource) {
            dbMapResourceUnloaded();
        }
    }

    @Override
    public void setUndoEnabled(final Boolean enable) {
        if (undoCommandAction != null) {
            undoCommandAction.setEnabled(enable);
        }
    }

    @Override
    public void setRedoEnabled(final Boolean enable) {
        if (redoCommandAction != null) {
            redoCommandAction.setEnabled(enable);
        }
    }

    @Override
    public void setEditMultiPointGeometryEnabled(Boolean enable) {
        for (InputMode mode : EnumSet.of(InputMode.EDIT_POLYGON,
                InputMode.EDIT_FREEHAND_POLYGON)) {
            InputModeAction action = actionsForInputModes.get(mode);
            if (action != null) {
                action.setEnabled(enable);
            }
        }
    }

    @Override
    public void setAddNewGeometryToSelectedToggleState(boolean enable,
            boolean check) {
        if (addNewGeometryToSelectedEventToggleAction != null) {
            addNewGeometryToSelectedEventToggleAction.setEnabled(enable);
            addNewGeometryToSelectedEventToggleAction.setChecked(check);
        }
    }

    // Package-Private Methods

    /**
     * Handle an attempt by the user to select or deselect the specified spatial
     * entity on the spatial display.
     * 
     * @param identifier
     *            Identifier of the spatial entity.
     * @param multipleSelection
     *            Indicates whether or not this is a part of a multiple
     *            selection action.
     */
    void handleUserSingleSpatialEntitySelection(IEntityIdentifier identifier,
            boolean multipleSelection) {

        /*
         * If this is a multiple selection operation, then check if the user is
         * selecting something that was already selected and treat this as a
         * deselect; otherwise, if this is a multiple selection operation, treat
         * it as a select; and finally, if a single selection, clear the
         * selected set and select only the clicked wntity. Note that the member
         * data set that tracks which spatial entities is not changed, since the
         * presenter may not accept the new selection; it is only altered in
         * response to the presenter saying is has been changed.
         */
        Set<IEntityIdentifier> identifiers = null;
        if (multipleSelection) {
            identifiers = new HashSet<>(selectedSpatialEntityIdentifiers);
            if (identifiers.contains(identifier)) {
                identifiers.remove(identifier);
            } else {
                identifiers.add(identifier);
            }
        } else {
            identifiers = Sets.newHashSet(identifier);
        }

        presenter.handleUserSpatialEntitySelectionChange(identifiers);
    }

    /**
     * Handle an attempt by the user to set the selection set to include only
     * the specified spatial entities on the spatial display.
     * 
     * @param identifiers
     *            Identifiers of the spatial entities.
     */
    void handleUserMultipleSpatialEntitiesSelection(
            Set<IEntityIdentifier> identifiers) {
        presenter.handleUserSpatialEntitySelectionChange(identifiers);
    }

    /**
     * Handle user modification of the geometry of the specified spatial entity.
     * 
     * @param identifier
     *            Identifier of the spatial entity.
     * @param geometry
     *            New geometry to be used by the spatial entity.
     */
    void handleUserModificationOfSpatialEntity(IEntityIdentifier identifier,
            Geometry geometry) {
        presenter.handleUserModificationOfSpatialEntity(identifier,
                selectedTime, geometry);
    }

    /**
     * Handle the the setting of the flag indicating whether or not newly
     * created shapes should be added to the current selection set.
     * 
     * @param state
     *            New state of the flag.
     */
    void handleSetAddCreatedEventsToSelected(boolean state) {
        presenter.handleSetAddCreatedEventsToSelected(state);
    }

    /**
     * Handle the completion of a multi-point drawing action, creating a
     * multi-point shape.
     * 
     * @param geometry
     *            New multi-point shape that was created.
     */
    void handleUserCreationOfShape(Geometry geometry) {
        presenter.handleUserShapeCreation(geometry);
    }

    /**
     * Handle the user selection of a location on the spatial display (not a
     * spatial entity).
     * 
     * @param location
     *            Location selected by the user.
     */
    void handleUserLocationSelection(Coordinate location) {
        presenter.handleUserLocationSelection(location);
    }

    /**
     * Handle the user initiation of a river gage action.
     * 
     * @param gageIdentifier
     *            Identifier of the gage for which the action is being invoked.
     */
    void handleUserInvocationOfGageAction(String gageIdentifier) {
        presenter.handleUserInvocationOfGageAction(gageIdentifier);
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
    void handleUserSelectByAreaDrawingActionComplete(
            IEntityIdentifier identifier, Set<Geometry> selectedGeometries) {
        SelectByAreaDbMapResourceData resourceData = selectByAreaVizResource
                .getResourceData();
        presenter.handleUserSelectByAreaCreationOrModification(identifier,
                resourceData.getTable(), resourceData.getMapName(),
                selectedGeometries);
        handleUserResetOfInputMode();
    }

    /**
     * Handle the user finishing with the use of an input mode, and having said
     * mode reset to default.
     */
    void handleUserResetOfInputMode() {

        /*
         * Unload the resource if it exists.
         */
        if (isSelectByAreaVizResourceLoaded()) {
            unloadSelectByAreaVizResourceFromPerspective();
        }

        /*
         * Notify the view of the completion of the drawing action.
         */
        resetInputMode();
    }

    /**
     * Handle the closing of the spatial display.
     */
    void handleSpatialDisplayClosed() {
        presenter.handleSpatialDisplayClosed();
    }

    /**
     * Get the context menu items appropriate to the specified pixel
     * coordinates.
     * 
     * @param entityIdentifier
     *            Identifier of the spatial that was chosen with the context
     *            menu invocation, or <code>null</code> if none was chosen.
     * @return Actions for the menu items to be shown.
     */
    List<IAction> getContextMenuActions(IEntityIdentifier entityIdentifier) {
        return presenter.getContextMenuActions(entityIdentifier,
                RUNNABLE_ASYNC_SCHEDULER);
    }

    /**
     * Determine whether or not a new shape is being drawn.
     * 
     * @return
     */
    boolean isDrawingOfNewShapeInProgress() {
        return drawingOfNewShapeInProgress;
    }

    // Private Methods

    /**
     * Remove the current select-by-area viz resource and forget about it. This
     * differs from the first part of
     * {@link #updatePerspectiveUseOfSelectByAreaVizResource()} in that it
     * removes any select-by-area viz resource in use by this object, not just
     * old ones that are hanging around but unreferenced by this object.
     */
    private void unloadSelectByAreaVizResourceFromPerspective() {
        if (selectByAreaVizResource != null) {
            AbstractEditor abstractEditor = EditorUtil
                    .getActiveEditorAs(AbstractEditor.class);
            if (abstractEditor != null) {
                for (IDisplayPane displayPane : Arrays.asList(abstractEditor
                        .getDisplayPanes())) {
                    IDescriptor idesc = displayPane.getDescriptor();
                    IMapDescriptor desc = null;
                    if (idesc instanceof IMapDescriptor) {
                        desc = (IMapDescriptor) idesc;

                        try {
                            desc.getResourceList().removeRsc(
                                    selectByAreaVizResource);
                        } catch (Exception e) {
                            statusHandler.error("Failure while unloading "
                                    + "select-by-area map database resource.",
                                    e);
                        }
                    }
                }
                selectByAreaVizResource = null;
            }
        }
    }

    /**
     * Remove any currently loaded but old select-by-area viz resources, and if
     * a select-by-area viz resource that is associated with this view is
     * available, load it.
     */
    private void updatePerspectiveUseOfSelectByAreaVizResource() {

        AbstractEditor abstractEditor = EditorUtil
                .getActiveEditorAs(AbstractEditor.class);
        if (abstractEditor != null) {

            for (IDisplayPane displayPane : Arrays.asList(abstractEditor
                    .getDisplayPanes())) {

                IDescriptor idesc = displayPane.getDescriptor();
                if (idesc instanceof IMapDescriptor) {

                    IMapDescriptor desc = (IMapDescriptor) idesc;
                    ResourceList rescList = desc.getResourceList();
                    for (ResourcePair pair : rescList) {
                        if (pair.getResource() instanceof SelectByAreaDbMapResource) {
                            rescList.removeRsc(pair.getResource());
                        }
                    }

                    if (selectByAreaVizResource != null) {
                        rescList = desc.getResourceList();
                        if (rescList.containsRsc(selectByAreaVizResource)) {
                            rescList.removeRsc(selectByAreaVizResource);
                        }
                        desc.getResourceList().add(selectByAreaVizResource);
                    }
                }
            }
        }
    }

    /**
     * Determine whether or not the specified hull area lies within the
     * currently visible portion of the specified display.
     * 
     * @param hull
     *            Coordinates to be checked to ensure they lie within the
     *            currently visible display.
     * @param display
     *            Display to be checked.
     * @return <code>true</code> if the coordinateds lie within the visible
     *         portion of the display, <code>false</code> otherwise.
     */
    private boolean isHullWithinDisplay(List<Coordinate> hull,
            IRenderableDisplay display) {
        IExtent extent = display.getExtent();
        IDescriptor descriptor = display.getDescriptor();
        for (Coordinate coordinate : hull) {
            double[] asPixel = descriptor.worldToPixel(new double[] {
                    coordinate.x, coordinate.y });
            if (!extent.contains(asPixel)) {
                return false;
            }
        }
        return true;

    }

    /**
     * Handle switching back to the standard input mode.
     */
    private void resetInputMode() {

        /*
         * If select-by-area is active, just set the flag to no longer true;
         * otherwise, check the button of, and load the standard, input handler.
         * Note that if the former is the case, the unloading of the
         * select-by-area viz resource will trigger a call to
         * dbMapResourceUnloaded(), which will in turn cause this method to be
         * called again, and that time through, the standard input handler will
         * be loaded.
         */
        if (selectByAreaActive) {
            selectByAreaActive = false;
        } else {
            InputModeAction action = actionsForInputModes
                    .get(InputMode.SELECT_OR_MODIFY);
            action.setChecked(true);
            action.run();
        }

        /*
         * Uncheck the other input mode radio buttons in the toolbar.
         */
        for (Map.Entry<InputMode, InputModeAction> entry : actionsForInputModes
                .entrySet()) {
            if (entry.getKey() != InputMode.SELECT_OR_MODIFY) {
                entry.getValue().setChecked(false);
            }
        }

        /*
         * Ensure that newly-created events are not added to the selected events
         * set.
         */
        presenter.handleSetAddCreatedEventsToSelected(false);
    }

    /**
     * Receive notification that a select-by-area operation has been initiated.
     */
    private void notifySelectByAreaInitiated() {
        for (InputModeAction action : actionsForInputModes.values()) {
            action.setChecked(false);
        }
        selectByAreaActive = true;
    }

    /**
     * Create listeners.
     * 
     * @param editor
     *            Editor in which to find display panes with resources.
     */
    private void createResourceListeners(AbstractEditor editor) {

        /*
         * Set up listeners for notifications concerning the addition or removal
         * of resources.
         */
        for (IDisplayPane displayPane : editor.getDisplayPanes()) {
            if (displayPane != null) {
                ResourceList resourceList = displayPane.getDescriptor()
                        .getResourceList();
                resourceList.addPostAddListener(addListener);
                resourceList.addPostRemoveListener(removeListener);
            }
        }
    }

    /**
     * Retrieve the descriptor of the current perspective.
     * 
     * @return The descriptor of the current perspective.
     */
    private IPerspectiveDescriptor getCurrentPerspectiveDescriptor() {

        IPerspectiveDescriptor perspectiveDescriptor = null;
        AbstractEditor abstractEditor = EditorUtil
                .getActiveEditorAs(AbstractEditor.class);
        if (abstractEditor != null) {
            perspectiveDescriptor = abstractEditor.getSite().getPage()
                    .getPerspective();
        } else {
            perspectiveDescriptor = null;
        }

        return (perspectiveDescriptor);
    }

    /**
     * Get a new instance of the draw by area viz resource.
     * 
     * @param databaseTableName
     *            Geo database table for which to retrieve overlay data.
     * @param legend
     *            Legend to be displayed by the resource.
     * @return New instance of the select-by-area viz resource.
     * @throws VizException
     *             If something goes wrong during viz resource instantiation.
     */
    private SelectByAreaDbMapResource getSelectByAreaVizResource(
            String databaseTableName, String legend) throws VizException {

        /*
         * Create the resource data class for the geo database map resource.
         */
        SelectByAreaDbMapResourceData resourceData = new SelectByAreaDbMapResourceData();
        resourceData.setTable(databaseTableName);
        resourceData.setMapName(legend);
        resourceData.setGeomField("the_geom");

        /*
         * Filter by the CWA.
         * 
         * TODO: This should be fetched dynamically, as some overlays do not
         * have a CWA field.
         */
        String siteID = LocalizationManager.getInstance().getCurrentSite();
        resourceData.setConstraints(new String[] { "cwa = '" + siteID + "'" });

        /*
         * Create the viz resource to display in CAVE.
         */
        selectByAreaVizResource = resourceData.construct(new LoadProperties(),
                null);
        selectByAreaVizResource.registerListener(this);

        return selectByAreaVizResource;
    }

    /**
     * Determine whether or not the select-by-area viz resource is loaded.
     * 
     * @return <code>true</code> if the resource is loaded, <code>false</code>
     *         otherwise.
     */
    private boolean isSelectByAreaVizResourceLoaded() {
        AbstractEditor abstractEditor = EditorUtil
                .getActiveEditorAs(AbstractEditor.class);
        if (selectByAreaVizResource != null) {
            boolean isLoaded = false;
            if (abstractEditor != null) {
                IDescriptor idesc = null;
                IMapDescriptor desc = null;
                for (IDisplayPane displayPane : Arrays.asList(abstractEditor
                        .getDisplayPanes())) {
                    idesc = displayPane.getDescriptor();

                    if (idesc instanceof IMapDescriptor) {
                        desc = (IMapDescriptor) idesc;
                        ResourceList rescList = desc.getResourceList();
                        isLoaded = rescList
                                .containsRsc(selectByAreaVizResource);
                        if (isLoaded) {
                            break;
                        }
                    }
                }
            }
            return isLoaded;
        }
        return false;
    }

    /**
     * Respond to the select-by-area viz resource is disposed. This will happen
     * if the user right-clicks on the legend item for said resource and selects
     * the 'unload' option. This will also be called when this resource is
     * removed from the {@link AbstractVizResource} list using the
     * {@link ResourceList#removeRsc(AbstractVizResource)} method.
     */
    private void dbMapResourceUnloaded() {
        selectByAreaVizResource = null;
        selectByAreaActive = false;
        resetInputMode();
    }
}

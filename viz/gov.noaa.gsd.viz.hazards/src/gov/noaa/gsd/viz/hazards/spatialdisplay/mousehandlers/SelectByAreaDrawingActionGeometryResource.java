/**
 * This software was developed and / or modified by the National Oceanic and
 * Atmospheric Administration (NOAA), Earth System Research Laboratory (ESRL),
 * Global Systems Division (GSD), Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.spatialdisplay.mousehandlers;

import gov.noaa.gsd.viz.hazards.display.action.SpatialDisplayAction;
import gov.noaa.gsd.viz.hazards.spatialdisplay.SpatialPresenter;
import gov.noaa.gsd.viz.hazards.spatialdisplay.SpatialView.SpatialViewCursorTypes;
import gov.noaa.gsd.viz.hazards.spatialdisplay.selectbyarea.SelectByAreaDbMapResource;
import gov.noaa.gsd.viz.hazards.utilities.HazardEventBuilder;
import gov.noaa.nws.ncep.ui.pgen.tools.InputHandlerDefaultImpl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;

import com.google.common.collect.Lists;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.core.rsc.IInputHandler;
import com.raytheon.uf.viz.hazards.sessionmanager.ISessionManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.ObservedSettings;
import com.raytheon.uf.viz.hazards.sessionmanager.events.InvalidGeometryException;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventAdded;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventGeometryModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.impl.ObservedHazardEvent;
import com.raytheon.viz.ui.VizWorkbenchManager;
import com.raytheon.viz.ui.editor.AbstractEditor;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

/**
 * Rect drawing action(refer to RT collaboration)
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Xiangbao Jing      Initial induction into repo
 * Jul 15, 2013      585   Chris.Golden       Changed to no longer be a singleton,
 *                                            and to subclass AbstractMouseHandler
 *                                            so as to make usage less special-case.
 * Aug  9, 2013 1921       daniel.s.schaffer@noaa.gov  Support of replacement of JSON with POJOs
 * Aug 21, 2013 1921       daniel.s.schaffer@noaa.gov  Call recommender framework directly
 * Aug 29, 2013 1921       bryon.lawrence     Modified to send list of context menu
 *                                            contributions instead of dict. A dict
 *                                            is not translated by the model adapter's
 *                                            updateEventData method.
 * Nov  04, 2013 2182     daniel.s.schaffer@noaa.gov      Started refactoring
 * Nov  23, 2013 2474     bryon.lawrence      Replaced string literal with
 *                                            a constant.
 * Sep 09, 2014 3994      Robert.Blum         Added handleMouseEnter to reset the cursor type.
 * Sep 10, 2014 3793      Robert.Blum         Modified handleMouseDown to return false for
 *                                            when the middle mouse button is pressed.
 * Sep 16, 2014 3786      Robert.Blum         Added user feedback when simplifying polygons.
 * Dec 05, 2014 4124      Chris.Golden        Changed to work with newly parameterized
 *                                            config manager.
 * Dec 13, 2014 4959       Dan Schaffer Spatial Display cleanup and other bug fixes
 * Jan 26, 2015 5952       Dan Schaffer Fix incorrect hazard area designation.
 * </pre>
 * 
 * @author Xiangbao Jing
 */
public class SelectByAreaDrawingActionGeometryResource extends
        AbstractMouseHandler {

    /** for logging */
    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(SelectByAreaDrawingActionGeometryResource.class);

    private enum Mode {
        CREATE, ADD_TO_ZONE, REMOVE, NONE
    };

    private String eventID = null;

    private boolean modifyingEvent = false;

    /** The displayed resource */
    private SelectByAreaDbMapResource zoneDisplay;

    /*
     * This map will keep track of the geometries for each hazard that is
     * created by the select-by-area drawing tool.
     */
    private final Map<String, List<Geometry>> hazardGeometryList;

    private final GeometryFactory geometryFactory = new GeometryFactory();

    @Override
    public void setSpatialPresenter(SpatialPresenter spatialPresenter) {
        super.setSpatialPresenter(spatialPresenter);
        zoneDisplay = spatialPresenter.getView().getSelectableGeometryDisplay();
    }

    public SelectByAreaDrawingActionGeometryResource() {
        hazardGeometryList = new HashMap<String, List<Geometry>>();
    }

    @Override
    protected IInputHandler createMouseHandler() {
        return new SelectByAreaHandler();
    }

    @Override
    public IInputHandler getMouseHandler() {
        IInputHandler handler = super.getMouseHandler();

        /*
         * We need to initialize the handler if an existing select-by-area
         * object is being edited.
         */
        if (modifyingEvent) {
            ((SelectByAreaHandler) handler).setSelectedGeoms(eventID);
        } else {
            /*
             * Clear any cached geometries
             */
            ((SelectByAreaHandler) handler).selectedGeometryAsList.clear();
        }

        return handler;
    }

    /**
     * Reset the modifying event flag.
     */
    public void resetModifyingEvent() {
        modifyingEvent = false;
    }

    /**
     * Set the event identifier.
     * 
     * @param eventID
     *            Event identifier.
     */
    public void setEventIdentifier(String eventID) {
        this.eventID = eventID;
        modifyingEvent = true;
    }

    public class SelectByAreaHandler extends InputHandlerDefaultImpl {

        private final Geometry mouseDownGeometry = null;

        private Geometry selectedGeometry = null;

        private List<Geometry> selectedGeometryAsList = new ArrayList<>();

        private Mode mode = Mode.CREATE;

        private final ISessionManager<ObservedHazardEvent, ObservedSettings> sessionManager = getSpatialPresenter()
                .getSessionManager();

        /*
         * (non-Javadoc)
         * 
         * @see com.raytheon.viz.ui.input.IInputHandler#handleMouseDown(int,
         * int, int)
         */
        @Override
        public boolean handleMouseDown(int x, int y, int mouseButton) {
            if (mouseButton == 1) {
                AbstractEditor editor = (AbstractEditor) VizWorkbenchManager
                        .getInstance().getActiveEditor();

                /*
                 * if ( !modifyingEvent ) { selectedGeoms.clear(); // clear the
                 * list.. }
                 */

                Coordinate c = editor.translateClick(x, y);

                selectedGeometry = zoneDisplay.clickOnExistingGeometry(c);

                if (selectedGeometry != null) {
                    /*
                     * Having trouble with the ArrayList contains method,
                     * although according to the java doc it should work. This
                     * is because the Geometry class does not properly override
                     * the Object equals method.
                     */

                    if (!isContainedInSelectedGeometries(selectedGeometry)) {
                        mode = Mode.CREATE;
                        selectedGeometryAsList.add(selectedGeometry);
                    } else {
                        selectedGeometryAsList.remove(selectedGeometry);
                    }

                    zoneDisplay.setSelectedGeometries(selectedGeometryAsList);
                    editor.refresh();
                } else {
                    mode = Mode.NONE;
                }

                return false;
            } else if (mouseButton == 2) {
                return false;
            }
            return true;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.raytheon.viz.ui.input.IInputHandler#handleMouseDownMove(int,
         * int, int)
         */
        @Override
        public boolean handleMouseDownMove(int x, int y, int mouseButton) {
            if (mouseButton == 2) {
                return false;
            }
            AbstractEditor editor = (AbstractEditor) VizWorkbenchManager
                    .getInstance().getActiveEditor();
            Coordinate c = editor.translateClick(x, y);
            Geometry g = null;

            g = zoneDisplay.clickOnExistingGeometry(c);

            if (g != null && !isContainedInSelectedGeometries(g)) {
                selectedGeometryAsList.add(g);
            }

            // Tell the resource to update its display of
            // the selected geometries.
            zoneDisplay.setSelectedGeometries(selectedGeometryAsList);
            editor.refresh();

            return true;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.raytheon.viz.ui.input.IInputHandler#handleMouseUp(int, int,
         * int)
         */
        @Override
        public boolean handleMouseUp(int x, int y, int mouseButton) {

            if (mouseButton == 1) {
                AbstractEditor editor = (AbstractEditor) VizWorkbenchManager
                        .getInstance().getActiveEditor();
                Coordinate c = editor.translateClick(x, y);
                Geometry g = null;

                g = zoneDisplay.clickOnExistingGeometry(c);

                if (g != null && mouseDownGeometry != null) {
                    if (g.equals(mouseDownGeometry)) {
                        if (mode.equals(Mode.CREATE)) {
                        } else {
                            zoneDisplay
                                    .setSelectedGeometries(selectedGeometryAsList);
                        }
                    }
                }

                editor.refresh();
                return false;
            } else if (mouseButton == 3) {
                // Unload the ZoneDbResource??

                // Send off the selected geometries.
                if (selectedGeometryAsList != null
                        && selectedGeometryAsList.size() > 0) {

                    Runnable mergingPolygons = new Runnable() {

                        @Override
                        public void run() {
                            // Try this polygon merging technique instead...
                            Geometry selectedGeometry = geometryFactory
                                    .createMultiPolygon(null);
                            for (Geometry geometry : selectedGeometryAsList) {
                                selectedGeometry = selectedGeometry
                                        .union(geometry);
                            }

                            /*
                             * Clone the list of selected geometries.
                             */
                            List<Geometry> selectedGeometryCopy = new ArrayList<>();

                            for (Geometry geometry : selectedGeometryAsList) {
                                selectedGeometryCopy.add((Geometry) geometry
                                        .clone());
                            }

                            if (!modifyingEvent) {

                                selectedGeometryAsList.clear();

                                // Tell the resource to update its display of
                                // the selected geometries.
                                zoneDisplay
                                        .setSelectedGeometries(selectedGeometryAsList);

                                try {
                                    HazardEventBuilder hazardEventBuilder = new HazardEventBuilder(
                                            sessionManager);
                                    IHazardEvent hazardEvent = hazardEventBuilder
                                            .buildPolygonHazardEvent(selectedGeometry);
                                    ObservedHazardEvent observedHazardEvent = hazardEventBuilder
                                            .addEvent(hazardEvent);
                                    eventID = observedHazardEvent.getEventID();
                                    SessionEventAdded addAction = new SessionEventAdded(
                                            sessionManager.getEventManager(),
                                            observedHazardEvent,
                                            getSpatialPresenter());

                                    getSpatialPresenter().publish(addAction);

                                    if (hazardGeometryList.containsKey(eventID)) {
                                        hazardGeometryList.get(eventID).addAll(
                                                selectedGeometryCopy);

                                    } else {
                                        hazardGeometryList.put(eventID,
                                                selectedGeometryCopy);
                                    }

                                    /*
                                     * Store the geometry table that this hazard
                                     * was originally based on in the eventDict
                                     * as well.
                                     */
                                    String geometryTable = zoneDisplay
                                            .getResourceData().getTable();
                                    String geometryLegend = zoneDisplay
                                            .getResourceData().getMapName();
                                    Map<String, Serializable> geoReferenceDict = new HashMap<>();
                                    geoReferenceDict
                                            .put(HazardConstants.HAZARD_EVENT_IDENTIFIER,
                                                    eventID);
                                    geoReferenceDict
                                            .put(HazardConstants.GEOMETRY_REFERENCE_KEY,
                                                    geometryTable);
                                    geoReferenceDict
                                            .put(HazardConstants.GEOMETRY_MAP_NAME_KEY,
                                                    geometryLegend);
                                    ArrayList<String> contextMenuList = new ArrayList<>();
                                    contextMenuList
                                            .add(HazardConstants.CONTEXT_MENU_ADD_REMOVE_SHAPES);
                                    geoReferenceDict
                                            .put(HazardConstants.CONTEXT_MENU_CONTRIBUTION_KEY,
                                                    contextMenuList);
                                    SpatialDisplayAction action = new SpatialDisplayAction(
                                            SpatialDisplayAction.ActionType.UPDATE_EVENT_METADATA);
                                    action.setToolParameters(geoReferenceDict);
                                    getSpatialPresenter().publish(action);
                                } catch (InvalidGeometryException e) {
                                    statusHandler
                                            .warn("Error creating Select-by-Area polygon: "
                                                    + e.getMessage());
                                }
                            } else {

                                hazardGeometryList.put(eventID,
                                        selectedGeometryCopy);
                                ObservedHazardEvent modifiedEvent = sessionManager
                                        .getEventManager()
                                        .getEventById(eventID);
                                modifiedEvent.setGeometry(selectedGeometry);
                                SessionEventGeometryModified modifyAction = new SessionEventGeometryModified(
                                        sessionManager.getEventManager(),
                                        modifiedEvent, getSpatialPresenter());
                                getSpatialPresenter().publish(modifyAction);

                            }

                            // Let the IHIS layer know that this drawing
                            // action is complete.
                            getSpatialPresenter().getView()
                                    .drawingActionComplete();

                        }
                    };

                    // Change cursor to busy indicator while the runnable is
                    // executed
                    BusyIndicator.showWhile(Display.getCurrent(),
                            mergingPolygons);
                }
            }
            return false;
        }

        @Override
        public boolean handleMouseEnter(Event event) {
            getSpatialPresenter().getView().setCursor(
                    SpatialViewCursorTypes.DRAW_CURSOR);
            return handleMouseMove(event.x, event.y);
        }

        /**
         * Sets the initial geometry to start with. This would be used in the
         * case that an existing geometry is being edited.
         * 
         * @param polygon
         */
        public void setSelectedGeoms(String eventID) {
            /*
             * Ask the selectable geometry display for a list of its polygons
             * contained within this polygon.
             */
            List<Geometry> geometries = hazardGeometryList.get(eventID);
            selectedGeometryAsList = Lists.newArrayList(geometries);
            zoneDisplay.setSelectedGeometries(selectedGeometryAsList);
        }

        private boolean isContainedInSelectedGeometries(
                Geometry selectedGeometry) {

            for (int i = 0; i < selectedGeometryAsList.size(); ++i) {
                if (selectedGeometryAsList.get(i).equals(selectedGeometry)) {
                    selectedGeometryAsList.set(i, selectedGeometry);
                    return true;
                }
            }

            return false;

        }

    }
}

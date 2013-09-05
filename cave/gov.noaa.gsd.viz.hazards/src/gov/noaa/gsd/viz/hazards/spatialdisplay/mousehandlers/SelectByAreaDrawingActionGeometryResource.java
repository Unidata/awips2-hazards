/**
 * This software was developed and / or modified by the National Oceanic and
 * Atmospheric Administration (NOAA), Earth System Research Laboratory (ESRL),
 * Global Systems Division (GSD), Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.spatialdisplay.mousehandlers;

import gov.noaa.gsd.viz.hazards.display.action.SpatialDisplayAction;
import gov.noaa.gsd.viz.hazards.jsonutilities.Dict;
import gov.noaa.gsd.viz.hazards.jsonutilities.EventDict;
import gov.noaa.gsd.viz.hazards.jsonutilities.Polygon;
import gov.noaa.gsd.viz.hazards.spatialdisplay.SpatialPresenter;
import gov.noaa.gsd.viz.hazards.spatialdisplay.selectbyarea.SelectByAreaDbMapResource;
import gov.noaa.gsd.viz.hazards.utilities.Utilities;
import gov.noaa.nws.ncep.ui.pgen.tools.InputHandlerDefaultImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.viz.core.rsc.IInputHandler;
import com.raytheon.viz.ui.VizWorkbenchManager;
import com.raytheon.viz.ui.editor.AbstractEditor;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.simplify.TopologyPreservingSimplifier;

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
 * </pre>
 * 
 * @author Xiangbao Jing
 */
public class SelectByAreaDrawingActionGeometryResource extends
        AbstractMouseHandler {

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
            ((SelectByAreaHandler) handler).selectedGeoms.clear();
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

        private List<Geometry> selectedGeoms = Lists.newArrayList();

        private Mode mode = Mode.CREATE;

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
                        selectedGeoms.add(selectedGeometry);
                    } else {
                        selectedGeoms.remove(selectedGeometry);
                    }

                    zoneDisplay.setSelectedGeometries(selectedGeoms);
                    editor.refresh();
                } else {
                    mode = Mode.NONE;
                }

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
            AbstractEditor editor = (AbstractEditor) VizWorkbenchManager
                    .getInstance().getActiveEditor();
            Coordinate c = editor.translateClick(x, y);
            Geometry g = null;

            g = zoneDisplay.clickOnExistingGeometry(c);

            if (g != null && !isContainedInSelectedGeometries(g)) {
                selectedGeoms.add(g);
            }

            // Tell the resource to update its display of
            // the selected geometries.
            zoneDisplay.setSelectedGeometries(selectedGeoms);
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
                            zoneDisplay.setSelectedGeometries(selectedGeoms);
                        }
                    }
                }

                editor.refresh();
                return false;
            } else {
                // Unload the ZoneDbResource??

                // Send off the selected geometries.
                if (selectedGeoms != null && selectedGeoms.size() > 0) {
                    // Try this polygon merging technique instead...
                    GeometryFactory geoFactory = selectedGeoms.get(0)
                            .getFactory();
                    Geometry geomColl = geoFactory
                            .createGeometryCollection(selectedGeoms
                                    .toArray(new Geometry[1]));
                    Geometry mergedPolygons = geomColl.buffer(0.001);

                    // Geometry mergedPolygons = geomColl.convexHull();
                    mergedPolygons = TopologyPreservingSimplifier.simplify(
                            mergedPolygons, 0.0001);

                    Polygon polygon = new Polygon("", "true", "true", "true",
                            "White", 2, "SOLID", "White",
                            mergedPolygons.getCoordinates());

                    if (!modifyingEvent) {
                        // Request an event ID for this newly drawn polygon.

                        // Send off JSON Message.
                        EventDict eventAreaObject = new EventDict();
                        eventAreaObject.put("eventId", "");
                        eventAreaObject.addShape(polygon);

                        /*
                         * Clone the list of selected geometries.
                         */
                        List<Geometry> copyGeometriesList = Lists
                                .newArrayList();

                        for (Geometry geometry : selectedGeoms) {
                            copyGeometriesList.add((Geometry) geometry.clone());
                        }

                        selectedGeoms.clear();

                        // Tell the resource to update its display of
                        // the selected geometries.
                        zoneDisplay.setSelectedGeometries(selectedGeoms);
                        String eventAreaJSON = eventAreaObject.toJSONString();
                        eventID = getSpatialPresenter().getNewEventAreaId(
                                eventAreaJSON);

                        SpatialDisplayAction action = new SpatialDisplayAction(
                                HazardConstants.NEW_EVENT_SHAPE);
                        action.setToolParameters(Dict
                                .getInstance(eventAreaJSON));
                        action.setEventID(eventID);
                        getSpatialPresenter().fireAction(action);

                        hazardGeometryList.put(eventID, copyGeometriesList);

                        /*
                         * Store the geometry table that this hazard was
                         * originally based on in the eventDict as well.
                         */
                        String geometryTable = zoneDisplay.getResourceData()
                                .getTable();
                        String geometryLegend = zoneDisplay.getResourceData()
                                .getMapName();
                        Dict geoReferenceDict = new Dict();
                        geoReferenceDict.put(Utilities.HAZARD_EVENT_IDENTIFIER,
                                eventID);
                        geoReferenceDict.put(
                                HazardConstants.GEOMETRY_REFERENCE_KEY,
                                geometryTable);
                        geoReferenceDict.put(
                                HazardConstants.GEOMETRY_MAP_NAME_KEY,
                                geometryLegend);
                        ArrayList<String> contextMenuList = Lists
                                .newArrayList();
                        contextMenuList
                                .add(HazardConstants.CONTEXT_MENU_ADD_REMOVE_SHAPES);
                        geoReferenceDict.put(
                                HazardConstants.CONTEXT_MENU_CONTRIBUTION_KEY,
                                contextMenuList);
                        action = new SpatialDisplayAction("updateEventData");
                        action.setToolParameters(geoReferenceDict);
                        getSpatialPresenter().fireAction(action);
                    } else {
                        /*
                         * Construct a modified event message and pass it on to
                         * the mediator.
                         */
                        // Send JSON Message
                        // Convert the object to JSON.
                        EventDict modifiedEventAreaObject = new EventDict();
                        modifiedEventAreaObject.put(
                                Utilities.HAZARD_EVENT_IDENTIFIER, eventID);
                        modifiedEventAreaObject.put(
                                Utilities.HAZARD_EVENT_SHAPE_TYPE,
                                Utilities.HAZARD_EVENT_SHAPE_TYPE_POLYGON);
                        modifiedEventAreaObject.addShape(polygon);

                        SpatialDisplayAction action = new SpatialDisplayAction(
                                "ModifyEventArea");
                        action.setModifyEventJSON(modifiedEventAreaObject
                                .toJSONString());
                        getSpatialPresenter().fireAction(action);

                    }

                    // Let the IHIS layer know that this drawing
                    // action is complete.
                    getSpatialPresenter().getView().drawingActionComplete();

                }

                return false;
            }
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
            zoneDisplay.setSelectedGeometries(geometries);
            selectedGeoms = geometries;
        }

        private boolean isContainedInSelectedGeometries(
                Geometry selectedGeometry) {

            for (int i = 0; i < selectedGeoms.size(); ++i) {
                if (selectedGeoms.get(i).equals(selectedGeometry)) {
                    selectedGeoms.set(i, selectedGeometry);
                    return true;
                }
            }

            return false;

        }

    }
}

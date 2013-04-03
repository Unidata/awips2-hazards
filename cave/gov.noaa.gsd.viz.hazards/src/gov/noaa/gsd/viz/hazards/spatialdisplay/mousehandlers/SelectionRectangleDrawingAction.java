/**
 * This software was developed and / or modified by the National Oceanic and
 * Atmospheric Administration (NOAA), Earth System Research Laboratory (ESRL),
 * Global Systems Division (GSD), Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.spatialdisplay.mousehandlers;

import gov.noaa.gsd.viz.hazards.spatialdisplay.SelectionRectangleDrawingAttributes;
import gov.noaa.gsd.viz.hazards.spatialdisplay.SpatialPresenter;
import gov.noaa.gsd.viz.hazards.spatialdisplay.drawableelements.IHazardServicesShape;
import gov.noaa.nws.ncep.ui.pgen.attrdialog.AttrDlg;
import gov.noaa.nws.ncep.ui.pgen.attrdialog.TrackExtrapPointInfoDlg;
import gov.noaa.nws.ncep.ui.pgen.elements.AbstractDrawableComponent;
import gov.noaa.nws.ncep.ui.pgen.elements.DrawableElement;
import gov.noaa.nws.ncep.ui.pgen.elements.DrawableElementFactory;
import gov.noaa.nws.ncep.ui.pgen.elements.DrawableType;
import gov.noaa.nws.ncep.ui.pgen.elements.Line;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.swt.SWT;

import com.google.common.collect.Lists;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.rsc.IInputHandler;
import com.raytheon.viz.ui.VizWorkbenchManager;
import com.raytheon.viz.ui.editor.AbstractEditor;
import com.raytheon.viz.ui.input.InputAdapter;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

/**
 * Selecting multiple currently events and invoke a callback when it is done.The
 * call back can be a product editor,application or null.The seleccted event IDs
 * can be passed into invoked application. Usage: --Select by clicking an
 * event(button 1 up), end with button 2; --Select by area, button 1 down and
 * move, end with button 1 up.
 * 
 * <pre>
 *  SOFTWARE HISTORY
 *  Date         Ticket#    Engineer    Description
 *  ------------ ---------- ----------- --------------------------
 * 07/15/2012                Xiangbao Jing    Initial creation
 * </pre>
 * 
 * @author Xiangbao Jing
 */
public class SelectionRectangleDrawingAction extends CopyEventDrawingAction {
    /**
     * For logging...
     */
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(SelectionRectangleDrawingAction.class);

    private static SelectionRectangleDrawingAction selectionRectangleDrawingAction = null;

    private SpatialPresenter spatialPresenter;

    /** The mouse handler */

    // protected IInputHandler mouseHandler;
    protected InputAdapter mouseHandler;

    protected AttrDlg attrDlg = null;

    public static final String pgenType = "TornadoWarning";

    public static final String pgenCategory = "MET";

    /**
     * Call this function to retrieve an instance of the EventBoxDrawingAction.
     * 
     * @param ihisDrawingLayer
     * @param ihisMenuBar
     * @return SelectionDrawingAction
     */
    public static SelectionRectangleDrawingAction getInstance(
            SpatialPresenter spatialPresenter) {
        if (selectionRectangleDrawingAction == null) {
            selectionRectangleDrawingAction = new SelectionRectangleDrawingAction(
                    spatialPresenter);
        } else {
            selectionRectangleDrawingAction.setDrawingLayer(spatialPresenter
                    .getView().getSpatialDisplay());
            selectionRectangleDrawingAction
                    .setSpatialPresenter(spatialPresenter);
        }

        return selectionRectangleDrawingAction;
    }

    private SelectionRectangleDrawingAction(SpatialPresenter spatialPresenter) {
        super(spatialPresenter);
        drawingLayer = spatialPresenter.getView().getSpatialDisplay();
        this.spatialPresenter = spatialPresenter;
    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.noaa.gsd.viz.drawing.AbstractDrawingTool#getMouseHandler()
     */
    @Override
    public IInputHandler getMouseHandler() {
        if (mouseHandler == null) {
            mouseHandler = new SelectionHandler();

            try {
                ((SelectionHandler) mouseHandler).drawingAttributes = new SelectionRectangleDrawingAttributes(
                        null);
            } catch (VizException e) {
                statusHandler
                        .error("In SelectionRectangleDrawingAction.getMouseHandler():",
                                e);
            }
        }

        /*
         * Since this is a singleton, make sure state variables are properly
         * initialized.
         */
        ((SelectionHandler) mouseHandler).anchorCorner = null;
        ((SelectionHandler) mouseHandler).dragCorner = null;

        return mouseHandler;
    }

    public void setSpatialPresenter(SpatialPresenter spatialPresenter) {
        this.spatialPresenter = spatialPresenter;
    }

    public SpatialPresenter getSpatialPresenter() {
        return spatialPresenter;
    }

    public class SelectionHandler extends CopyEventDrawingAction.CopyHandler {
        // Minimum screen distance for identify started a selection by area.
        private final int MIN_DISTANCE = 10;

        // For multiple selection by area
        private boolean isSelectByArea = false;

        /*
         * Corners of tracking rectangle
         */
        private Coordinate anchorCorner = null;

        private Coordinate dragCorner = null;

        /*
         * Drawing attributes for this rectangle
         */
        private AttrDlg drawingAttributes = null;

        private final DrawableElementFactory def = new DrawableElementFactory();

        /*
         * Geometry factory. Do not need to recreate this every time it is
         * needed.
         */
        private final GeometryFactory geoFactory = new GeometryFactory();

        /**
         * Attribute dialog for displaying track points info
         */
        TrackExtrapPointInfoDlg trackExtrapPointInfoDlg = null;

        /**
         * instance variable to store the pgenType of the selected
         * drawableElement
         */
        String pgenType;

        /**
         * Contains a set of the clicked event ids. Using a set prevents the
         * possibility of duplicate eventIds.
         */
        Set<String> clickedElementList = new HashSet<String>();

        /**
         * When SHIFT key is up switch the control to single event selection
         * handler.
         */
        @Override
        public boolean handleKeyUp(int key) {

            if (key == SWT.SHIFT) {
                if (isSelectByArea) {
                    handleSelectByArea(null);
                } else {
                    spatialPresenter.getView().drawingActionComplete();
                }

                return true;
            }

            return false;

        }

        /*
         * (non-Javadoc)
         * 
         * @see com.raytheon.viz.ui.input.IInputHandler#handleMouseDown(int,
         * int, int) Do nothing at there for moving mouse
         */
        @Override
        public boolean handleMouseDown(int anX, int aY, int button) {
            // Check if mouse is in geographic extent
            AbstractEditor editor = ((AbstractEditor) VizWorkbenchManager
                    .getInstance().getActiveEditor());
            Coordinate loc = editor.translateClick(anX, aY);

            if (loc == null) {
                return false;
            }

            // First point for area area selection
            anchorCorner = loc;

            return true;
        }

        //
        @Override
        public boolean handleMouseUp(int x, int y, int button) {
            AbstractEditor editor = ((AbstractEditor) VizWorkbenchManager
                    .getInstance().getActiveEditor());
            Coordinate loc = editor.translateClick(x, y);

            if (loc == null) {
                return false;
            }

            if (button == 1) {
                // in area selection status
                if (isSelectByArea) {

                    handleSelectByArea(loc);
                } else if (getDrawingLayer().getSelectedDE() == null)

                {

                    anchorCorner = null;
                    dragCorner = null;

                    // Is any event selected?
                    List<AbstractDrawableComponent> containingComponentsList = getDrawingLayer()
                            .getContainingComponents(loc);

                    // No, do nothing
                    if (containingComponentsList == null) {
                        return false;
                    }

                    // Check selected event ID list, add in if it is existing in
                    // the list,
                    // otherwise, remove it.
                    for (AbstractDrawableComponent drawableComponent : containingComponentsList) {
                        String selectedElementEventID = drawingLayer
                                .elementClicked(
                                        (DrawableElement) drawableComponent,
                                        false, false);
                        if (selectedElementEventID == null) {
                            continue;
                        }

                        if (clickedElementList.contains(selectedElementEventID)) {
                            // if is existing, remove
                            clickedElementList.remove(selectedElementEventID);
                        } else {
                            // if is not existing, add on
                            clickedElementList.add(selectedElementEventID);
                            getDrawingLayer().elementClicked(
                                    (DrawableElement) drawableComponent, false,
                                    false);
                        }
                    }

                }
            } else if (button == 2 && !isSelectByArea) {
                anchorCorner = null;
                dragCorner = null;

                /*
                 * In clicking selection status. End multiple selection, pop up
                 * product editor. Give the control to single selection as the
                 * default. Fire a message from here to the
                 * HazardServicesAppBuilder..
                 */
                if (clickedElementList != null)

                {
                    drawingLayer.multipleElementsClicked(clickedElementList);
                    clickedElementList.clear();
                }

                // Give the control to single selection as the default.
                spatialPresenter.getView().drawingActionComplete();
            }

            // Set the flag to the clicking selection status??????????????
            // isSelectByArea = false;

            anchorCorner = null;
            dragCorner = null;

            return true;

        }

        /**
         * @param
         * @return
         */
        private void handleSelectByArea(Coordinate loc) {
            if (anchorCorner == null || dragCorner == null) {
                getDrawingLayer().removeGhostLine();
                anchorCorner = null;
                dragCorner = null;

                getDrawingLayer().issueRefresh();

                // Indicate that this drawing action is done.
                getSpatialPresenter().getView().drawingActionComplete();

            } else {
                if (loc != null) {
                    dragCorner = loc;
                }

                Envelope envelope = new Envelope(anchorCorner, dragCorner);

                Geometry polygon = geoFactory.toGeometry(envelope);
                getDrawingLayer().removeGhostLine();

                anchorCorner = null;
                dragCorner = null;

                // Indicate that this drawing action is done.
                getSpatialPresenter().getView().drawingActionComplete();

                // what ids are selected?
                clickedElementList.clear();

                Iterator<AbstractDrawableComponent> iterator = getDrawingLayer()
                        .getActiveLayer().getComponentIterator();
                while (iterator.hasNext()) {
                    AbstractDrawableComponent comp = iterator.next();
                    Geometry p = ((IHazardServicesShape) comp).getPolygon();

                    // The event is inside the selected area
                    if (p != null && polygon.contains(p)) {
                        // What it's thevent ID
                        String selectedEventId = drawingLayer.elementClicked(
                                (DrawableElement) comp, false, false);

                        // Put the ID in the selected ID list
                        if (selectedEventId != null) {
                            clickedElementList.add(selectedEventId);
                        }

                    }
                }

                /*
                 * End multiple selection, pop up product editor. Give the
                 * control to single selection as the default. Fire a message
                 * from here to the HazardServicesAppBuilder..
                 */
                if (clickedElementList != null) {
                    drawingLayer.multipleElementsClicked(clickedElementList);
                    clickedElementList.clear();
                }

                // Give the control to single selection as the default.
                spatialPresenter.getView().drawingActionComplete();
            }

            isSelectByArea = false;
        }

        /**
         * Only for the area selection only, do nothing for clicking selection
         */
        @Override
        public boolean handleMouseDownMove(int anX, int aY, int button) {

            AbstractEditor editor = ((AbstractEditor) VizWorkbenchManager
                    .getInstance().getActiveEditor());
            Coordinate loc = editor.translateClick(anX, aY);

            if ((button != 1) || (loc == null)) {
                return false;
            }

            if (anchorCorner == null) {
                anchorCorner = loc;
            } else {
                // Set the flag, is in the area selection status
                if (!isSelectByArea) {
                    Coordinate p0 = new Coordinate(
                            editor.translateInverseClick(anchorCorner)[0],
                            editor.translateInverseClick(anchorCorner)[1]);

                    Coordinate p1 = new Coordinate(
                            editor.translateInverseClick(loc)[0],
                            editor.translateInverseClick(loc)[1]);

                    if (p0.distance(p1) > MIN_DISTANCE) {
                        isSelectByArea = true;
                    }
                }

                dragCorner = loc;

                Envelope envelope = new Envelope(anchorCorner, dragCorner);
                Geometry polygon = geoFactory.toGeometry(envelope);

                Coordinate[] points = polygon.getCoordinates();
                // create the ghost element and put it in the drawing layer
                AbstractDrawableComponent ghost = def.create(DrawableType.LINE,
                        drawingAttributes, "Line", "LINE_SOLID", points,
                        getDrawingLayer().getActiveLayer());

                ArrayList<Coordinate> ghostPts = Lists.newArrayList(points);

                ((Line) ghost).setLinePoints(ghostPts);

                getDrawingLayer().setGhostLine(ghost);
                getDrawingLayer().issueRefresh();
            }

            return true;

        }

        @Override
        public boolean handleMouseMove(int x, int y) {
            AbstractEditor editor = ((AbstractEditor) VizWorkbenchManager
                    .getInstance().getActiveEditor());

            editor.getActiveDisplayPane().setFocus();

            return false;
        }

    }
}

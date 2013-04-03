/**
 * This software was developed and / or modified by the National Oceanic and
 * Atmospheric Administration (NOAA), Earth System Research Laboratory (ESRL),
 * Global Systems Division (GSD), Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.spatialdisplay.mousehandlers;

import gov.noaa.gsd.viz.hazards.display.action.SpatialDisplayAction;
import gov.noaa.gsd.viz.hazards.jsonutilities.JSONUtilities;
import gov.noaa.gsd.viz.hazards.spatialdisplay.PolygonDrawingAttributes;
import gov.noaa.gsd.viz.hazards.spatialdisplay.SpatialPresenter;
import gov.noaa.gsd.viz.hazards.utilities.Utilities;
import gov.noaa.nws.ncep.ui.pgen.attrdialog.AttrDlg;
import gov.noaa.nws.ncep.ui.pgen.display.IAttribute;
import gov.noaa.nws.ncep.ui.pgen.elements.AbstractDrawableComponent;
import gov.noaa.nws.ncep.ui.pgen.elements.DrawableElementFactory;
import gov.noaa.nws.ncep.ui.pgen.elements.DrawableType;
import gov.noaa.nws.ncep.ui.pgen.elements.Line;
import gov.noaa.nws.ncep.ui.pgen.tools.InputHandlerDefaultImpl;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ui.PlatformUI;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.rsc.IInputHandler;
import com.raytheon.viz.ui.VizWorkbenchManager;
import com.raytheon.viz.ui.editor.AbstractEditor;
import com.vividsolutions.jts.geom.Coordinate;

/**
 * 
 * Description: Drawing action for creating hazard events boxes. Each left click
 * by the forecaster becomes a vertex in the polygon. The forecaster closes the
 * polygon via a single right mouse click.
 * 
 * This is a singleton object. Only one instance is created during the lifetime
 * of the Hazard Services program.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 2011                Bryon.Lawrence      Initial creation
 * 
 * </pre>
 * 
 * @author Bryon.Lawrence
 * @version 1.0
 */
public class EventBoxDrawingAction extends AbstractMouseHandler {
    /** for logging */
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(EventBoxDrawingAction.class);

    private static EventBoxDrawingAction eventBoxDrawingAction = null;

    /** The mouse handler */
    protected IInputHandler theHandler;

    /**
     * The PGEN drawing attributes which define how the initial hazard will
     * appear.
     */
    protected AttrDlg drawingAttributes = null;

    /**
     * The presenter responsible for communicating with the spatial view and
     * receiving events from it.
     */
    private SpatialPresenter spatialPresenter;

    /**
     * Call this function to retrieve an instance of the EventBoxDrawingAction.
     * 
     * @param ihisDrawingLayer
     * @param spatialPresenter
     */
    public static EventBoxDrawingAction getInstance(
            SpatialPresenter spatialPresenter) {
        if (eventBoxDrawingAction == null) {
            eventBoxDrawingAction = new EventBoxDrawingAction(spatialPresenter);
        } else {
            eventBoxDrawingAction.setSpatialPresenter(spatialPresenter);
            eventBoxDrawingAction.setDrawingLayer(spatialPresenter.getView()
                    .getSpatialDisplay());
        }

        return eventBoxDrawingAction;

    }

    /**
     * Private constructor which enforces that this is a singleton
     * 
     * @param spatialPresenter
     *            The presenter responsible for the hazard services spatial
     *            display.
     */
    private EventBoxDrawingAction(SpatialPresenter spatialPresenter) {
        super();

        this.spatialPresenter = spatialPresenter;
        this.drawingLayer = spatialPresenter.getView().getSpatialDisplay();

        /*
         * Create the attribute container.
         */
        try {
            drawingAttributes = new PolygonDrawingAttributes(PlatformUI
                    .getWorkbench().getActiveWorkbenchWindow().getShell());
        } catch (VizException e) {
            statusHandler
                    .error("In EvenBoxDrawingAction, could not create drawing attributes.",
                            e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.noaa.gsd.viz.drawing.AbstractDrawingTool#getMouseHandler()
     */
    @Override
    public IInputHandler getMouseHandler() {
        if (theHandler == null) {
            theHandler = new EventBoxDrawingHandler();
        }
        return theHandler;
    }

    /**
     * Setter for spatial presenter. The spatial presenter is responsible for
     * the spatial view and display of hazard information in CAVE.
     * 
     * @param spatialPresenter
     * @return
     */
    public void setSpatialPresenter(SpatialPresenter spatialPresenter) {
        this.spatialPresenter = spatialPresenter;
    }

    /**
     * Getter for spatial presenter.
     * 
     * @param
     * @return The spatial presenter being used by this mouse handler object.
     */
    public SpatialPresenter getSpatialPresenter() {
        return spatialPresenter;
    }

    /**
     * 
     * Description: Mouse handler for drawing hazard event polygons.
     * 
     * <pre>
     * 
     * SOFTWARE HISTORY
     * Date         Ticket#    Engineer    Description
     * ------------ ---------- ----------- --------------------------
     * March 13, 2013            Bryon.Lawrence      Initial creation
     * 
     * </pre>
     * 
     * @author Bryon.Lawrence
     * @version 1.0
     */
    public class EventBoxDrawingHandler extends InputHandlerDefaultImpl {
        /*
         * Points of the new watch box.
         */
        private final List<Coordinate> points = new ArrayList<Coordinate>();

        /*
         * An instance of DrawableElementFactory, which is used to create a new
         * watch box. The factory will probably need to have a tornado warning
         * box added to it.
         */
        private final DrawableElementFactory def = new DrawableElementFactory();

        @Override
        public boolean handleMouseDown(int x, int y, int mouseButton) {
            return false;
        }

        @Override
        public boolean handleMouseMove(int x, int y) {
            AbstractEditor editor = ((AbstractEditor) VizWorkbenchManager
                    .getInstance().getActiveEditor());
            Coordinate loc = editor.translateClick(x, y);

            if (loc == null) {
                return false;
            }

            if (points != null && points.size() >= 1) {

                // create the ghost element and put it in the drawing layer
                AbstractDrawableComponent ghost = def.create(DrawableType.LINE,
                        (IAttribute) drawingAttributes, "Line", "LINE_SOLID",
                        (ArrayList<Coordinate>) points, getDrawingLayer()
                                .getActiveLayer());

                ArrayList<Coordinate> ghostPts = new ArrayList<Coordinate>(
                        points);
                ghostPts.add(loc);
                // ((Line) ghost)
                // .setLinePoints(new ArrayList<Coordinate>(ghostPts));
                ((Line) ghost).setLinePoints(ghostPts);

                getDrawingLayer().setGhostLine(ghost);
                getDrawingLayer().issueRefresh();
            }

            return true;
        }

        // Needed to override this to prevent
        // it from being passed on to CAVE panning routines.
        @Override
        public boolean handleMouseUp(int x, int y, int mouseButton) {
            // TODO Auto-generated method stub
            // return true;
            AbstractEditor editor = ((AbstractEditor) VizWorkbenchManager
                    .getInstance().getActiveEditor());
            Coordinate loc = editor.translateClick(x, y);

            if (loc == null) {
                return false;
            }

            if (mouseButton == 1) {
                points.add(loc);

            } else if (mouseButton == 3) {
                // This is where the ghost object is replaced
                // by the TornadoWarningBox. For the moment
                // just delete the ghost line.
                if (points.size() != 0) {
                    if (points.size() < 2) {
                        getDrawingLayer().removeGhostLine();
                        points.clear();

                        getDrawingLayer().issueRefresh();

                        // Indicate that this drawing action is done.
                        spatialPresenter.getView().drawingActionComplete();
                    } else {
                        points.add(loc);
                        getDrawingLayer().removeGhostLine();

                        // Convert the object to JSON.
                        String jsonString = JSONUtilities.createNewHazardJSON(
                                "", Utilities.HAZARD_EVENT_SHAPE_TYPE_POLYGON,
                                points);

                        points.clear();

                        SpatialDisplayAction action = new SpatialDisplayAction(
                                "newEventArea");
                        action.setJSON(jsonString);
                        spatialPresenter.fireAction(action);

                        // Indicate that this drawing action is done.
                        spatialPresenter.getView().drawingActionComplete();
                    }
                }

                return true;

            }

            return true;

        }

        /**
         * Needed to override this to prevent odd panning behavior when drawing
         * to CAVE
         * 
         * @param
         * @return
         */
        @Override
        public boolean handleMouseDownMove(int x, int y, int mouseButton) {
            return true;
        }

    }
}

/**
 * This software was developed and / or modified by the National Oceanic and
 * Atmospheric Administration (NOAA), Earth System Research Laboratory (ESRL),
 * Global Systems Division (GSD), Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.spatialdisplay.mousehandlers;

import gov.noaa.gsd.viz.hazards.display.HazardServicesMessageHandler;
import gov.noaa.gsd.viz.hazards.display.action.SpatialDisplayAction;
import gov.noaa.gsd.viz.hazards.jsonutilities.JSONUtilities;
import gov.noaa.gsd.viz.hazards.spatialdisplay.SpatialPresenter;
import gov.noaa.gsd.viz.hazards.spatialdisplay.SpatialView.SpatialViewCursorTypes;
import gov.noaa.nws.ncep.ui.pgen.elements.Line;
import gov.noaa.nws.ncep.ui.pgen.elements.Symbol;

import com.raytheon.uf.viz.core.rsc.IInputHandler;
import com.vividsolutions.jts.geom.Coordinate;

/**
 * Drawing action for the drag me to storm dot.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Xiangbao Jing      Initial induction into repo
 * 
 * </pre>
 * 
 * @author Xiangbao Jing
 */
public class DragDropDrawingAction extends CopyEventDrawingAction {
    private static DragDropDrawingAction moveDrawingAction = null;

    private SpatialPresenter spatialPresenter = null;

    private String toolName = null;

    /** The mouse handler */
    protected IInputHandler theHandler;

    public static final String pgenType = "TornadoWarning";

    public static final String pgenCategory = "MET";

    /**
     * Call this function to retrieve an instance of the EventBoxDrawingAction.
     * 
     * @param ihisDrawingLayer
     * @param ihisMenuBar
     */
    public static DragDropDrawingAction getInstance(
            SpatialPresenter spatialPresenter, String toolName) {
        if (moveDrawingAction == null) {
            moveDrawingAction = new DragDropDrawingAction(spatialPresenter,
                    toolName);
        } else {
            moveDrawingAction.setSpatialPresenter(spatialPresenter);
            moveDrawingAction.setToolName(toolName);
            moveDrawingAction.setDrawingLayer(spatialPresenter.getView()
                    .getSpatialDisplay());
        }

        return moveDrawingAction;

    }

    public DragDropDrawingAction(SpatialPresenter spatialPresenter,
            String toolName) {
        super(spatialPresenter);
        drawingLayer = spatialPresenter.getView().getSpatialDisplay();
        this.toolName = toolName;
        this.spatialPresenter = spatialPresenter;
    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.noaa.gsd.viz.drawing.AbstractDrawingTool#getMouseHandler()
     */
    @Override
    public IInputHandler getMouseHandler() {
        if (theHandler == null) {
            theHandler = new MoveHandler();
        }
        return theHandler;
    }

    public void setSpatialPresenter(SpatialPresenter spatialPresenter) {
        this.spatialPresenter = spatialPresenter;
    }

    public SpatialPresenter getSpatialPresenter() {
        return spatialPresenter;
    }

    public void setToolName(String toolName) {
        this.toolName = toolName;
    }

    public String getToolName() {
        return toolName;
    }

    public class MoveHandler extends CopyEventDrawingAction.CopyHandler {
        /*
         * (non-Javadoc)
         * 
         * @see com.raytheon.viz.ui.input.IInputHandler#handleMouseUp(int, int,
         * int)
         */
        @Override
        public boolean handleMouseUp(int x, int y, int button) {

            if (ghostEl != null) {
                Coordinate coord;

                if (ghostEl instanceof Line) {
                    Line dot = (Line) ghostEl;
                    coord = dot.getCentroid();
                } else if (ghostEl instanceof Symbol) {
                    Symbol symbol = (Symbol) ghostEl;
                    coord = symbol.getLocation();
                } else {
                    return false;
                }

                long selectedTime = Long.parseLong(HazardServicesMessageHandler
                        .getModelProxy().getSelectedTime());

                selectedTime /= 1000;
                String json = JSONUtilities.createDragDropPointJSON(coord.y,
                        coord.x, selectedTime);

                SpatialDisplayAction action = new SpatialDisplayAction(
                        "runTool", toolName, json);
                spatialPresenter.fireAction(action);
                spatialPresenter.getView().drawingActionComplete();
            }

            drawingLayer.removeGhostLine();
            drawingLayer.removeEvent("DragDropDot");
            drawingLayer.setSelectedDE(null);
            ghostEl = null;

            // We are done dragging the storm dot. Switch back
            // to the previously used mouse handler.
            spatialPresenter.getView().drawingActionComplete();
            spatialPresenter.getView().setCursor(
                    SpatialViewCursorTypes.ARROW_CURSOR);

            // Tell the Spatial Display to fire a DMTS message

            drawingLayer.issueRefresh();

            return true;

        }
    }
}

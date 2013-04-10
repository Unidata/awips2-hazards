/**
 * This software was developed and / or modified by the National Oceanic and
 * Atmospheric Administration (NOAA), Earth System Research Laboratory (ESRL),
 * Global Systems Division (GSD), Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.spatialdisplay.mousehandlers;

import gov.noaa.gsd.viz.hazards.spatialdisplay.HazardServicesMouseHandlers;
import gov.noaa.gsd.viz.hazards.spatialdisplay.SpatialPresenter;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.core.rsc.IInputHandler;

/**
 * Description: Factory for building mouse handlers.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 28, 2013            Bryon.Lawrence      Initial creation
 * 
 * </pre>
 * 
 * @author Bryon.Lawrence
 * @version 1.0
 */
public class MouseHandlerFactory {

    /**
     * Logging mechanism.
     */
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(MouseHandlerFactory.class);

    /*
     * Private member variables
     */
    private SpatialPresenter presenter = null;

    /**
     * Creates an instance of the mouse handler factory.
     */
    public MouseHandlerFactory(SpatialPresenter presenter) {
        this.presenter = presenter;
    }

    /**
     * Creates the specified mouse handler. Returns the current mouse handler if
     * the requested mouse handler is not recognized.
     * 
     * @param mouseHandler
     *            The requested mouse handler.
     * @param args
     *            optional arguments required by the mouse handler.
     * 
     * @return An instance of the requested mouse handler.
     */
    public IInputHandler createMouseHandler(
            HazardServicesMouseHandlers mouseHandler, String... args) {

        switch (mouseHandler) {
        case SINGLE_SELECTION:
            return SelectionDrawingAction.getInstance(presenter)
                    .getMouseHandler();

        case MULTI_SELECTION:
            return MultiSelectionAction.getInstance(presenter)
                    .getMouseHandler();

        case SELECTION_RECTANGLE:
            return SelectionRectangleDrawingAction.getInstance(presenter)
                    .getMouseHandler();

        case EVENTBOX_DRAWING:
            return EventBoxDrawingAction.getInstance(presenter)
                    .getMouseHandler();

        case FREEHAND_DRAWING:
            return FreeHandHazardDrawingAction.getInstance(presenter)
                    .getMouseHandler();

        case DRAG_DROP_DRAWING:
            return DragDropDrawingAction.getInstance(presenter, args[0])
                    .getMouseHandler();

        case DRAW_BY_AREA:
            // Make sure this geometry can be used in select by area
            // operations.

            String eventID = null;

            if (args.length == 3) {
                eventID = args[2];
            }

            if (eventID == null) {
                return SelectByAreaDrawingActionGeometryResource.getInstance(
                        presenter.getView().getSelectableGeometryDisplay(),
                        presenter).getMouseHandler();
            } else {
                return SelectByAreaDrawingActionGeometryResource.getInstance(
                        presenter.getView().getSelectableGeometryDisplay(),
                        presenter, eventID).getMouseHandler();
            }

        default:

            statusHandler
                    .debug("In MouseHandlerFactor: Unrecognized mouse handler: "
                            + mouseHandler);
            break;

        }

        return null;
    }
}

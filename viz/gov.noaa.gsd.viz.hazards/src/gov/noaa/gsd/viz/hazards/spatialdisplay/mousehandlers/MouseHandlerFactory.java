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

import java.util.Map;

import com.google.common.collect.Maps;
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
 * Jul 15, 2013      585   Chris.Golden        Changed so that various handlers
 *                                             are no longer singletons.
 * Aug  9, 2013 1921       daniel.s.schaffer@noaa.gov  Support of replacement of JSON with POJOs
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

    // Private Variables

    /**
     * Spatial presenter.
     */
    private SpatialPresenter presenter = null;

    /**
     * Map of mouse handler types to the corresponding handlers. As mouse
     * handlers of different types are constructed, they are cached here.
     */
    private final Map<HazardServicesMouseHandlers, AbstractMouseHandler> mouseHandlersForTypes = Maps
            .newEnumMap(HazardServicesMouseHandlers.class);

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param presenter
     *            Spatial presenter.
     */
    public MouseHandlerFactory(SpatialPresenter presenter) {
        this.presenter = presenter;
    }

    /**
     * Gets the specified mouse handler. Returns the current mouse handler if
     * the requested mouse handler is not recognized.
     * 
     * @param mouseHandler
     *            The requested mouse handler.
     * @param args
     *            optional arguments required by the mouse handler.
     * 
     * @return An instance of the requested mouse handler.
     */
    public IInputHandler getMouseHandler(
            HazardServicesMouseHandlers mouseHandler, String... args) {

        // Get the handler action, creating it if it does not already exist.
        AbstractMouseHandler handler = mouseHandlersForTypes.get(mouseHandler);
        if (handler == null) {
            switch (mouseHandler) {
            case SINGLE_SELECTION:
                handler = new SelectionAction();
                break;
            case FREE_HAND_MULTI_SELECTION:
                handler = new FreeHandMultiSelectionAction(
                        presenter.getSessionManager());
                break;
            case RECTANGLE_MULTI_SELECTION:
                handler = new RectangleMultiSelectionAction(
                        presenter.getSessionManager());
                break;
            case VERTEX_DRAWING:
                handler = new VertexHazardDrawingAction(
                        presenter.getSessionManager());
                break;
            case FREEHAND_DRAWING:
                handler = new FreeHandHazardDrawingAction(
                        presenter.getSessionManager());
                break;
            case STORM_TOOL_DRAG_DOT_DRAWING:
                handler = new DragDropAction();
                break;
            case DRAW_BY_AREA:
                handler = new SelectByAreaDrawingActionGeometryResource();
                break;
            default:
                statusHandler
                        .debug("In MouseHandlerFactor: Unrecognized mouse handler: "
                                + mouseHandler);
                break;
            }
            mouseHandlersForTypes.put(mouseHandler, handler);
        }
        handler.setSpatialPresenter(presenter);

        // Perform any handler-specific configuration that might be required.
        switch (mouseHandler) {
        case VERTEX_DRAWING:
            ((VertexHazardDrawingAction) handler).setShapeType(args[0]);
            break;
        case STORM_TOOL_DRAG_DOT_DRAWING:
            ((DragDropAction) handler).setToolName(args[0]);
            break;
        case DRAW_BY_AREA:
            if (args.length == 3) {
                ((SelectByAreaDrawingActionGeometryResource) handler)
                        .setEventIdentifier(args[2]);
            } else {
                ((SelectByAreaDrawingActionGeometryResource) handler)
                        .resetModifyingEvent();
            }
            break;
        default:
            break;
        }

        // Return the mouse handler.
        return handler.getMouseHandler();
    }
}

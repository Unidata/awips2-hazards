/**
 * This software was developed and / or modified by the National Oceanic and
 * Atmospheric Administration (NOAA), Earth System Research Laboratory (ESRL),
 * Global Systems Division (GSD), Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.spatialdisplay.input;

import gov.noaa.gsd.viz.hazards.spatialdisplay.InputHandlerType;
import gov.noaa.gsd.viz.hazards.spatialdisplay.SpatialDisplay;
import gov.noaa.gsd.viz.hazards.spatialdisplay.entities.IEntityIdentifier;
import gov.noaa.gsd.viz.hazards.spatialdisplay.selectbyarea.SelectByAreaDbMapResource;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.GeometryType;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.vividsolutions.jts.geom.Geometry;

/**
 * Description: Factory for building input handlers.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer       Description
 * ------------ ---------- -------------- --------------------------
 * Feb 28, 2013            Bryon.Lawrence Initial creation
 * Jul 15, 2013     585    Chris.Golden   Changed so that various handlers
 *                                        are no longer singletons.
 * Aug  9, 2013    1921    Dan Schaffer   Support of replacement of JSON with POJOs.
 * Jan  7, 2015    4959    Dan Schaffer   Ability to right click to add/remove UGCs
 *                                        from hazards.
 * Jun 23, 2016   19537    Chris.Golden   Removed storm-track-specific code.
 * Jul 25, 2016   19537    Chris.Golden   Renamed and completely revamped to work with
 *                                        new mouse handlers.
 * Sep 21, 2016   15934    Chris.Golden   Changed to support ellipse drawing.
 * </pre>
 * 
 * @author Bryon.Lawrence
 * @version 1.0
 */
public class InputHandlerFactory {

    /**
     * Logging mechanism.
     */
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(InputHandlerFactory.class);

    // Private Variables

    /**
     * Spatial display.
     */
    private final SpatialDisplay spatialDisplay;

    /**
     * Map of input handler types to the corresponding handlers. As input
     * handlers of different types are constructed, they are cached here.
     */
    private final Map<InputHandlerType, BaseInputHandler> inputHandlersForTypes = new EnumMap<>(
            InputHandlerType.class);

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param spatialDisplay
     *            Spatial display for which input handlers are to be provided.
     */
    public InputHandlerFactory(SpatialDisplay spatialDisplay) {
        this.spatialDisplay = spatialDisplay;
    }

    // Public Methods

    /**
     * Get the specified input handler for drawing.
     * 
     * @param handlerType
     *            Type of input handler being requested.
     * @param geometryType
     *            Type of geometry that is to be drawn by the requested handler.
     * @return Input handler.
     */
    public BaseInputHandler getDrawingInputHandler(
            InputHandlerType handlerType, GeometryType geometryType) {

        /*
         * Get the handler, creating it if it does not already exist.
         */
        DrawingInputHandler handler = (DrawingInputHandler) inputHandlersForTypes
                .get(handlerType);
        if (handler == null) {
            switch (handlerType) {
            case VERTEX_DRAWING:
                handler = new VertexDrawingInputHandler(spatialDisplay);
                break;
            case FREEHAND_DRAWING:
                handler = new FreehandDrawingInputHandler(spatialDisplay);
                break;
            case ELLIPSE_DRAWING:
                handler = new EllipseDrawingInputHandler(spatialDisplay);
                break;
            default:
                statusHandler
                        .debug("InputHandlerFactory.getDrawingInputHandler(): "
                                + "Unrecognized drawing input handler: "
                                + handlerType);
                break;
            }
            inputHandlersForTypes.put(handlerType, handler);
        }

        /*
         * Set the handler to its starting state, providing it the geometry type
         * it is to be creating.
         */
        handler.reset();
        handler.setShapeType(geometryType);

        return handler;
    }

    /**
     * Get the specified type of non-drawing input handler.
     * 
     * @param handlerType
     *            Type of input handler being requested.
     * @return Input handler.
     */
    public BaseInputHandler getNonDrawingInputHandler(
            InputHandlerType handlerType) {

        /*
         * Get the handler, creating it if it does not already exist.
         */
        BaseInputHandler handler = inputHandlersForTypes.get(handlerType);
        if (handler == null) {
            switch (handlerType) {
            case SINGLE_SELECTION:
                handler = new SelectionAndModificationInputHandler(
                        spatialDisplay);
                break;
            case FREEHAND_MULTI_SELECTION:
                handler = new FreehandMultiSelectionInputHandler(spatialDisplay);
                break;
            case RECTANGLE_MULTI_SELECTION:
                handler = new RectangleMultiSelectionInputHandler(
                        spatialDisplay);
                break;
            default:
                statusHandler
                        .debug("InputHandlerFactory.getNonDrawingInputHandler(): Unrecognized input handler: "
                                + handlerType);
                break;
            }
            inputHandlersForTypes.put(handlerType, handler);
        }

        /*
         * Set the handler to its starting state.
         */
        handler.reset();

        return handler;
    }

    /**
     * Get the select-by-area input handler.
     * 
     * @param vizResource
     *            Select-by-area viz resource to be used with this handler.
     * @param selectedGeometries
     *            Select-by-area geometries that should start off as selected.
     * @param identifier
     *            Identifier of the entity that is to be edited using
     *            select-by-area; if <code>null</code>, a new geometry is to be
     *            created.
     * @return Input handler.
     */
    public BaseInputHandler getSelectByAreaInputHandler(
            SelectByAreaDbMapResource vizResource,
            Set<Geometry> selectedGeometries, IEntityIdentifier identifier) {

        /*
         * Get the handler, creating it if it does not already exist.
         */
        SelectByAreaInputHandler handler = (SelectByAreaInputHandler) inputHandlersForTypes
                .get(InputHandlerType.DRAW_BY_AREA);
        if (handler == null) {
            handler = new SelectByAreaInputHandler(spatialDisplay);
            inputHandlersForTypes.put(InputHandlerType.DRAW_BY_AREA, handler);
        }

        /*
         * Set the handler to its starting state.
         */
        handler.reset();

        /*
         * Tell the handler what viz resource to use, what geometries should be
         * selected to start with, and which entity if any is being edited.
         */
        handler.setVizResourceAndSelectedGeometries(vizResource,
                selectedGeometries, identifier);

        return handler;
    }
}

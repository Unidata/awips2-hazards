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
import gov.noaa.gsd.viz.hazards.spatialdisplay.SpatialView.SpatialViewCursorTypes;
import gov.noaa.gsd.viz.hazards.spatialdisplay.mousehandlers.MouseHandlerFactory;
import gov.noaa.gsd.viz.hazards.spatialdisplay.selectbyarea.SelectByAreaDbMapResource;
import gov.noaa.gsd.viz.mvp.IView;

import java.util.Date;

import com.raytheon.uf.viz.core.map.MapDescriptor;

/**
 * Interface describing the methods that must be implemented by a class that
 * functions as a spatial display view, managed by a spatial presenter.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Chris.Golden      Initial induction into repo
 * 
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public interface ISpatialView<C, E extends Enum<E>> extends IView<C, E> {

    // Public Methods

    /**
     * Initialize the view.
     * 
     * @param presenter
     *            Presenter managing this view.
     */
    public void initialize(SpatialPresenter presenter,
            MouseHandlerFactory mouseFactory);

    /**
     * Draw events on the view.
     * 
     * @param jsonEvents
     *            Json string representing the events.
     * 
     */
    public void drawEvents(String jsonEvents);

    /**
     * Retrieve the descriptor associated with this view.
     * 
     * @return A descriptor describing this view.
     */
    public MapDescriptor getDescriptor();

    /**
     * Force time matching to be recalculated.
     */
    public void redoTimeMatching();

    /**
     * Set the display's current zoom parameters.
     * 
     * @param longitude
     *            Longitude of the zoom center point.
     * @param latitude
     *            Latitude of the zoom center point
     * @param multiplier
     *            Zoom multiplier.
     */
    public void setDisplayZoomParameters(double longitude, double latitude,
            double multiplier);

    /**
     * Get the display's current zoom parameters.
     * 
     * @return Array of three numbers, the first being the longitude of the zoom
     *         center point, the second being the latitude of the zoom center
     *         point, and the third being the zoom multiplier.
     */
    public double[] getDisplayZoomParameters();

    /**
     * Recenter and rezoom the display on the currently selected hazard.
     */
    public void recenterRezoomDisplay();

    /**
     * Refresh the spatial view.
     */
    public void issueRefresh();

    /**
     * Clear the displayed events
     */
    public void clearEvents();

    /**
     * Sets the mouse handler.
     * 
     * @param appBuilder
     *            The app builder.
     * @param messageHandler
     *            The app message handler
     * @param mouseHandler
     *            The mouse handler to load.
     * @param args
     *            arguments required by the draw by area mouse handler.
     */
    public void setMouseHandler(HazardServicesMouseHandlers mouseHandler,
            String... args);

    /**
     * Unregisters the current mouse handler from the active editor.
     */
    public void unregisterCurrentMouseHandler();

    /**
     * Modify a displayed shape.
     * 
     * @param drawingAction
     *            The modification action.
     * @param appBuilder
     *            App builder.
     * @param messageHandler
     *            Message handler.
     */
    public void modifyShape(HazardServicesDrawingAction drawingAction,
            HazardServicesAppBuilder appBuilder,
            HazardServicesMessageHandler messageHandler);

    /**
     * Manage the view frames based on the selected time.
     * 
     * @param selectedTime
     *            the selected time to try to match a view frame to.
     */
    public void manageViewFrames(Date selectedTime);

    /**
     * Adds the draw by area viz resource to the CAVE editor.
     */
    public void addGeometryDisplayResourceToPerspective();

    /**
     * Sets the mouse cursor to the specified type.
     * 
     * @param cursorType
     *            The type of cursor to set.
     */
    public void setCursor(SpatialViewCursorTypes cursorType);

    /**
     * Whenever the user has completed a drawing action (i.e, the user has
     * completed a polygon, a point, a line, or a select by area, then notify
     * the toolbar to reset to the default selected event editing mode.
     */
    public void drawingActionComplete();

    /**
     * Get the spatial display tool layer.
     * 
     * @return An instance of the spatial display.
     */
    public ToolLayer getSpatialDisplay();

    /**
     * Checks to determine if a geometry overlay needs to be loaded for a
     * selected event. If multiple geometry overlays need to be loaded this
     * currently only loads the first overlay.
     * 
     * @param eventIDs_json
     *            A json string containing the eventID or IDs to load a geometry
     *            overlay for.
     */
    public void loadGeometryOverlayForSelectedEvent(String eventIDs_json);

    /**
     * Returns the current instance of the draw by area resource.
     * 
     * @return the current instance of the draw by area resource.
     */
    public SelectByAreaDbMapResource getSelectableGeometryDisplay();
}

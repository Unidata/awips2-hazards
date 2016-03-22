/**
 * This software was developed and / or modified by the National Oceanic and
 * Atmospheric Administration (NOAA), Earth System Research Laboratory (ESRL),
 * Global Systems Division (GSD), Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.spatialdisplay;

import gov.noaa.gsd.common.visuals.SpatialEntity;
import gov.noaa.gsd.viz.hazards.spatialdisplay.SpatialView.SpatialViewCursorTypes;
import gov.noaa.gsd.viz.hazards.spatialdisplay.mousehandlers.MouseHandlerFactory;
import gov.noaa.gsd.viz.hazards.spatialdisplay.selectbyarea.SelectByAreaDbMapResource;
import gov.noaa.gsd.viz.mvp.IView;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.ObservedSettings;
import com.raytheon.uf.viz.hazards.sessionmanager.events.impl.ObservedHazardEvent;
import com.vividsolutions.jts.geom.Coordinate;

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
 * Jul 12, 2013    585     Chris.Golden      Changed to support loading from bundle.
 * Aug 06, 2013   1265     bryon.lawrence    Added support for undo/redo.
 * Aug  9, 2013 1921       daniel.s.schaffer@noaa.gov  Support of replacement of JSON with POJOs
 * Aug 29, 2013   1921     bryon.lawrence    Removed JSON parameter from
 *                                           loadGeometryOverlayForSelectedEvent().
 * Nov 27, 2013   1462     bryon.lawrence    Updated drawEvents to support display
 *                                           of hazard hatched areas.
 * Dec 05, 2014   4124     Chris.Golden      Changed to work with ObservedSettings.
 * Dec 13, 2014 4959       Dan Schaffer      Spatial Display cleanup and other bug fixes
 * Feb 27, 2015 6000       Dan Schaffer      Improved centering behavior
 * Mar 16, 2016 15676      Chris.Golden      Changed to make visual features work.
 *                                           Will be refactored to remove numerous
 *                                           existing kludges.
 * Mar 24, 2016 15676      Chris.Golden      Changed method that draws spatial entities
 *                                           to take another parameter.
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
     * @param mouseFactory
     *            Mouse handler factory.
     */
    public void initialize(SpatialPresenter presenter,
            MouseHandlerFactory mouseFactory);

    /**
     * Set the settings.
     * 
     * @param settings
     */
    public void setSettings(ObservedSettings settings);

    /**
     * Draw events on the view.
     * 
     * @param eventEditability
     * @param events
     */
    public void drawEvents(Collection<ObservedHazardEvent> events,
            Map<String, Boolean> eventOverlapSelectedTime,
            Map<String, Boolean> forModifyingStormTrack,
            Map<String, Boolean> eventEditability,
            boolean toggleAutoHazardChecking, boolean areHatchedAreasDisplayed);

    /**
     * Draw spatial entities on the view.
     * 
     * @param spatialEntities
     *            Spatial entities.
     * @param selectedEventIdentifiers
     *            Identifiers of events that are currently selected.
     */
    public void drawSpatialEntities(
            List<SpatialEntity<VisualFeatureSpatialIdentifier>> spatialEntities,
            Set<String> selectedEventIdentifiers);

    /**
     * Force time matching to be recalculated.
     */
    public void redoTimeMatching();

    /**
     * Recenter and rezoom the display to the given location if the display
     * doesn't already contain the hull
     * 
     * @param hullCoordinates
     * @param center
     */
    public void recenterRezoomDisplay(Coordinate[] hull, Coordinate center);

    /**
     * Refresh the spatial view.
     */
    public void issueRefresh();

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
     *            The modification action..
     */
    public void modifyShape(HazardServicesDrawingAction drawingAction);

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
    public SpatialDisplay getSpatialDisplay();

    /**
     * Checks to determine if a geometry overlay needs to be loaded for a
     * selected event. If multiple geometry overlays need to be loaded this
     * currently only loads the first overlay.
     * 
     */
    public void loadGeometryOverlayForSelectedEvent();

    /**
     * Returns the current instance of the draw by area resource.
     * 
     * @return the current instance of the draw by area resource.
     */
    public SelectByAreaDbMapResource getSelectableGeometryDisplay();

    /**
     * Sets the enabled state of the undo flag.
     * 
     * @param undoFlag
     *            True - enabled, False - disabled
     * 
     * @return
     */
    public void setUndoEnabled(final Boolean undoFlag);

    /**
     * Sets the enabled state of the redo flag.
     * 
     * @param redoFlag
     *            True - enabled, False - disabled
     * 
     * @return
     */
    public void setRedoEnabled(final Boolean redoFlag);

    /**
     * Enables the edit polygon button. This will be true when there is a single
     * selected hazard event
     * 
     * @param enabled
     *            True - enabled, False - disabled
     */
    public void setEditEventGeometryEnabled(final Boolean enabled);

}

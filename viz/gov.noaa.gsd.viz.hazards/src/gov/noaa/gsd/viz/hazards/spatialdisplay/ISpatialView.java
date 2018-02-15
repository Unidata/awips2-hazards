/**
 * This software was developed and / or modified by the National Oceanic and
 * Atmospheric Administration (NOAA), Earth System Research Laboratory (ESRL),
 * Global Systems Division (GSD), Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.spatialdisplay;

import java.util.Date;
import java.util.List;
import java.util.Set;

import com.raytheon.uf.common.colormap.Color;
import com.vividsolutions.jts.geom.Coordinate;

import gov.noaa.gsd.common.utilities.DragAndDropGeometryEditSource;
import gov.noaa.gsd.common.utilities.geometry.IAdvancedGeometry;
import gov.noaa.gsd.common.visuals.SpatialEntity;
import gov.noaa.gsd.viz.hazards.spatialdisplay.SpatialPresenter.Command;
import gov.noaa.gsd.viz.hazards.spatialdisplay.SpatialPresenter.SpatialEntityType;
import gov.noaa.gsd.viz.hazards.spatialdisplay.SpatialPresenter.Toggle;
import gov.noaa.gsd.viz.hazards.spatialdisplay.entities.IEntityIdentifier;
import gov.noaa.gsd.viz.hazards.spatialdisplay.selectbyarea.SelectByAreaContext;
import gov.noaa.gsd.viz.mvp.IView;
import gov.noaa.gsd.viz.mvp.widgets.ICommandInvoker;
import gov.noaa.gsd.viz.mvp.widgets.IListStateChangeHandler;
import gov.noaa.gsd.viz.mvp.widgets.IListStateChanger;
import gov.noaa.gsd.viz.mvp.widgets.IStateChangeHandler;
import gov.noaa.gsd.viz.mvp.widgets.IStateChanger;

/**
 * Interface describing the methods that must be implemented by a class that
 * functions as a spatial display view, managed by a spatial presenter.
 * <p>
 * The view interacts with the associated presenter via widget abstractions such
 * as {@link IStateChangeHandler}, {@link IListStateChangeHandler}, etc, as per
 * the standard Hazard Services MVP practice.
 * </p>
 * <p>
 * The view responds to changes in hazard events, tool-specified visual
 * features, etc. and turns all visual elements into {@link SpatialEntity}
 * instances. These are then passed to the {@link ISpatialView} for actual
 * display.
 * </p>
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Chris.Golden      Initial induction into repo
 * Jul 12, 2013     585    Chris.Golden      Changed to support loading from bundle.
 * Aug 06, 2013    1265    bryon.lawrence    Added support for undo/redo.
 * Aug  9, 2013    1921    daniel.s.schaffer@noaa.gov  Support of replacement of JSON with POJOs
 * Aug 29, 2013    1921    bryon.lawrence    Removed JSON parameter from
 *                                           loadGeometryOverlayForSelectedEvent().
 * Nov 27, 2013    1462    bryon.lawrence    Updated drawEvents to support display
 *                                           of hazard hatched areas.
 * Dec 05, 2014    4124    Chris.Golden      Changed to work with ObservedSettings.
 * Dec 13, 2014    4959    Dan Schaffer      Spatial Display cleanup and other bug fixes
 * Feb 27, 2015    6000    Dan Schaffer      Improved centering behavior
 * Mar 16, 2016   15676    Chris.Golden      Changed to make visual features work.
 *                                           Will be refactored to remove numerous
 *                                           existing kludges.
 * Mar 24, 2016   15676    Chris.Golden      Changed method that draws spatial entities
 *                                           to take another parameter.
 * Jun 23, 2016   19537    Chris.Golden      Removed storm-track-specific code.
 * Jul 25, 2016   19537    Chris.Golden      Removed a number of methods that got
 *                                           refactored away as the move toward MVP
 *                                           compliance continues.
 * Aug 23, 2016   19537    Chris.Golden      Continued spatial display refactor.
 * Sep 12, 2016   15934    Chris.Golden      Changed to work with advanced geometries.
 * Dec 14, 2016   26813    bkowal            Use the active Hazard Services site when
 *                                           determining which counties to render for
 *                                           "Select by Area".
 * Jun 27, 2017   14789    Robert.Blum       Added passing of select by area color to
 *                                           view.
 * Jan 17, 2018   33428    Chris.Golden      Changed to work with new, more flexible
 *                                           toolbar contribution code, and to provide
 *                                           new enhanced geometry-operation-based
 *                                           edits.
 * Jan 22, 2018   25765    Chris.Golden      Added ability for the settings to specify
 *                                           which drag-and-drop manipulation points
 *                                           are to be prioritized.
 * Mar 22, 2018   15561    Chris.Golden      Added code to ensure that the spatial
 *                                           display's editability is factored into the
 *                                           editability (and visual cues thereof) of
 *                                           spatial entities, into the enabled state of
 *                                           toolbar buttons, and into whether context
 *                                           menu items are provided.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public interface ISpatialView<I, C, E extends Enum<E>> extends IView<I, C, E> {

    // Public Static Constants

    /**
     * Undo identifier.
     */
    public static final String UNDO_IDENTIFIER = "undo";

    /**
     * Redo identifier.
     */
    public static final String REDO_IDENTIFIER = "redo";

    /**
     * Add new event to selected toggle identifier.
     */
    public static final String ADD_NEW_EVENT_TO_SELECTED_TOGGLE_IDENTIFIER = "addNewEventToSelectedToggle";

    /**
     * Move and select choice identifier.
     */
    public static final String MOVE_AND_SELECT_CHOICE_IDENTIFIER = "moveAndSelectChoice";

    /**
     * Drawing choice identifier.
     */
    public static final String DRAWING_CHOICE_IDENTIFIER = "drawingChoice";

    /**
     * Geometry edit mode choice identifier.
     */
    public static final String GEOMETRY_EDIT_MODE_CHOICE_IDENTIFIER = "geometryEditModeChoice";

    /**
     * Select-by-area pulldown choice identifier.
     */
    public static final String SELECT_BY_AREA_PULLDOWN_CHOICE_IDENTIFIER = "selectByAreaPulldownChoice";

    // Public Methods

    /**
     * Initialize the view.
     * <p>
     * TODO: <code>presenter</code> should not be passed in once refactoring is
     * complete; this gives the view way too much access.
     * </p>
     * 
     * @param presenter
     *            Presenter managing this view.
     * @param localizedSite
     *            Identifier of the site to which this CAVE is localized.
     * @param currentSite
     *            Identifier of the current site.
     * @param selectedSpatialEntityIdentifiers
     *            Unmodifiable view of the selected spatial entity identifiers.
     *            This will be kept up to date by the presenter.
     * @param priorityForDragAndDropGeometryEdits
     *            Priority for drag-and-drop geometry edits.
     * @param selectByAreaColor
     *            Color to be used for select-by-area operations.
     */
    public void initialize(SpatialPresenter presenter, String localizedSite,
            String currentSite,
            Set<IEntityIdentifier> selectedSpatialEntityIdentifiers,
            DragAndDropGeometryEditSource priorityForDragAndDropGeometryEdits,
            Color selectByAreaColor);

    /**
     * Get the spatial view editability state changer.
     * 
     * @return Spatial view editability state changer.
     */
    public IStateChanger<Object, Boolean> getEditabilityChanger();

    /**
     * Get the selected spatial entity identifiers state changer.
     * 
     * @return Selected spatial entity identifiers state changer.
     */
    public IStateChanger<Object, Set<IEntityIdentifier>> getSelectedSpatialEntityIdentifiersChanger();

    /**
     * Get the spatial entities list state changer.
     * 
     * @return Spatial entities list state changer.
     */
    public IListStateChanger<SpatialEntityType, SpatialEntity<? extends IEntityIdentifier>> getSpatialEntitiesChanger();

    /**
     * Get the create shape command invoker.
     * 
     * @return Create shape command invoker.
     */
    public ICommandInvoker<IAdvancedGeometry> getCreateShapeInvoker();

    /**
     * Get the modify entity geometry command invoker.
     * 
     * @return Modify entity geometry command invoker.
     */
    public ICommandInvoker<EntityGeometryModificationContext> getModifyGeometryInvoker();

    /**
     * Get the load select-by-area command invoker.
     * 
     * @return Load select-by-area command invoker.
     */
    public ICommandInvoker<SelectByAreaContext> getSelectByAreaInvoker();

    /**
     * Get the select location command invoker.
     * 
     * @return Select location command invoker.
     */
    public ICommandInvoker<Coordinate> getSelectLocationInvoker();

    /**
     * Get the gage action command invoker.
     * 
     * @return Gage action command invoker.
     */
    public ICommandInvoker<String> getGageActionInvoker();

    /**
     * Get the toggle state changer.
     * 
     * @return Toggle state changer.
     */
    public IStateChanger<Toggle, Boolean> getToggleChanger();

    /**
     * Get the miscellaneous command invoker.
     * 
     * @return Miscellaneous command invoker.
     */
    public ICommandInvoker<Command> getCommandInvoker();

    /**
     * Refresh the display.
     */
    public void refresh();

    /**
     * Recenter and rezoom the display to the specified location if the display
     * doesn't already contain the specified hull.
     * 
     * @param hull
     *            Coordinates of the hull area that must be visible.
     * @param center
     *            Center of the new area to be displayed.
     */
    public void centerAndZoomDisplay(List<Coordinate> hull, Coordinate center);

    /**
     * Set the selected time to that specified.
     * 
     * @param selectedTime
     *            New selected time.
     */
    public void setSelectedTime(Date selectedTime);

    /**
     * Set the current site identifier to that specified.
     * 
     * @param currentSite
     *            New current site identifier.
     */
    public void setCurrentSite(String currentSite);

    /**
     * Load the specified select-by-area viz resource and associated input
     * handler.
     * 
     * @param context
     *            Context for the load.
     */
    public void loadSelectByAreaVizResourceAndInputHandler(
            SelectByAreaContext context);

    /**
     * Set the enabled state of the various combine-geometry operation buttons.
     * 
     * @param enable
     *            Flag indicating whether or not combine-geometry operation
     *            buttons should be enabled.
     * @param rememberSelectedAction
     *            If <code>true</code> and <code>enable</code> is
     *            <code>false</code>, then remember the combine-geometry
     *            operation that was active immediately prior to the last time
     *            that this method was called with <code>enable</code> set to
     *            <code>false</code> (which may be this invocation). If
     *            <code>false</code>, any such record of a previously-active
     *            operation is discarded.
     */
    public void setCombineGeometryOperationsEnabled(boolean enable,
            boolean rememberSelectedAction);

    /**
     * Set the priority for drag-and-drop geometry edits.
     * 
     * @param priorityForDragAndDropGeometryEdits
     *            New priority for drag-and-drop geometry edits.
     */
    public void setPriorityForDragAndDropGeometryEdits(
            DragAndDropGeometryEditSource priorityForDragAndDropGeometryEdits);
}
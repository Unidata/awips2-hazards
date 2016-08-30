/**
 * This software was developed and / or modified by the National Oceanic and
 * Atmospheric Administration (NOAA), Earth System Research Laboratory (ESRL),
 * Global Systems Division (GSD), Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.spatialdisplay;

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

import java.util.Date;
import java.util.List;
import java.util.Set;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

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
 * Jun 23, 2016 19537      Chris.Golden      Removed storm-track-specific code.
 * Jul 25, 2016 19537      Chris.Golden      Removed a number of methods that got
 *                                           refactored away as the move toward MVP
 *                                           compliance continues.
 * Aug 23, 2016 19537      Chris.Golden      Continued spatial display refactor.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public interface ISpatialView<C, E extends Enum<E>> extends IView<C, E> {

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
     * @param selectedSpatialEntityIdentifiers
     *            Unmodifiable view of the selected spatial entity identifiers.
     *            This will be kept up to date by the presenter.
     */
    public void initialize(SpatialPresenter presenter,
            Set<IEntityIdentifier> selectedSpatialEntityIdentifiers);

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
    public ICommandInvoker<Geometry> getCreateShapeInvoker();

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
     * Load the specified select-by-area viz resource and associated input
     * handler.
     * 
     * @param context
     *            Context for the load.
     */
    public void loadSelectByAreaVizResourceAndInputHandler(
            SelectByAreaContext context);

    /**
     * Set the enabled state of the edit polygon buttons.
     * 
     * @param enable
     *            Flag indicating whether or not edit polygon buttons should be
     *            enabled.
     */
    public void setEditMultiPointGeometryEnabled(boolean enable);
}
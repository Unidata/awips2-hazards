/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Evaluation & Decision Support Branch (EDS)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.spatialdisplay.input;

import com.vividsolutions.jts.geom.Coordinate;

import gov.noaa.gsd.common.utilities.geometry.IAdvancedGeometry;
import gov.noaa.gsd.viz.hazards.spatialdisplay.SpatialDisplay;
import gov.noaa.gsd.viz.hazards.spatialdisplay.drawables.IDrawable;
import gov.noaa.gsd.viz.hazards.spatialdisplay.drawables.ManipulationPoint;
import gov.noaa.nws.ncep.ui.pgen.elements.AbstractDrawableComponent;

/**
 * Description: Input handler for modifying an edited drawable in some way.
 * Subclasses implement specific behavior (rotation, vertex editing, etc.). The
 * generic parameter <code>M</code> provides the type of the manipulation point
 * that this input handler may utilize.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Sep 29, 2016   15928    Chris.Golden Initial creation.
 * Sep 08, 2017   15561    Chris.Golden Fixed bug that caused geometry modifications
 *                                      performed upon a multi-geometry event to
 *                                      potentially strip away all the geometries
 *                                      making up said event except the one that was
 *                                      modified.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public abstract class ModificationInputHandler<M extends ManipulationPoint>
        extends ModificationCapableInputHandler<M> {

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param spatialDisplay
     *            Spatial display to be used.
     */
    public ModificationInputHandler(SpatialDisplay spatialDisplay) {
        super(spatialDisplay);
    }

    // Public Methods

    /**
     * Initialize the handler to commence editing. The handler must respond by
     * beginning the edit, treating this as a drag of the specified manipulation
     * point to the specified new location.
     * 
     * @param drawableBeingEdited
     *            Drawable that is to be edited using this handler.
     * @param manipulationPoint
     *            Mpoint that is to be used for the edit.
     * @param newLocation
     *            New location in latitude-longitude coordinates of the
     *            <code>manipulationPoint</code>.
     */
    public void initialize(AbstractDrawableComponent drawableBeingEdited,
            M manipulationPoint, Coordinate newLocation) {
        getSpatialDisplay().setDrawableBeingEdited(drawableBeingEdited);
        setManipulationPointUnderMouse(manipulationPoint);
        handleManipulationPointMovement(newLocation);
    }

    @Override
    public boolean handleMouseDownMove(int x, int y, int button) {

        /*
         * Ensure the button held down is the first one.
         */
        if (button != 1) {
            return false;
        }

        /*
         * Get the cursor location in lat-lon coordinates.
         */
        Coordinate location = getLocationFromPixels(x, y);
        if (location == null) {
            return false;
        }

        /*
         * Handle the movement of the manipulation point.
         */
        handleManipulationPointMovement(location);
        return true;
    }

    @Override
    public boolean handleMouseUp(final int x, final int y, int button) {

        /*
         * Ensure the released button is the first one.
         */
        if (button != 1) {
            return false;
        }

        /*
         * If there is a drawable being edited, complete the edit.
         */
        AbstractDrawableComponent editedDrawable = getSpatialDisplay()
                .getDrawableBeingEdited();
        if (getGhostDrawable() != null) {
            IAdvancedGeometry modifiedGeometry = ((IDrawable<?>) getGhostDrawable())
                    .getGeometry();
            getSpatialDisplay().visualCuesNeedUpdatingAtNextRefresh();

            /*
             * If the edited geometry is valid, use the latter as a modified
             * geometry for the spatial entity; otherwise, simply refresh the
             * display.
             */
            if (getSpatialDisplay().checkGeometryValidity(modifiedGeometry)) {
                getSpatialDisplay().handleUserModificationOfDrawable(
                        editedDrawable,
                        getSpatialDisplay()
                                .buildModifiedGeometryForSpatialEntity(
                                        (IDrawable<?>) editedDrawable,
                                        modifiedGeometry));
            } else {
                getSpatialDisplay().updateBoundingBoxDrawable(editedDrawable,
                        editedDrawable);
                getSpatialDisplay().issueRefresh();
            }
            finalizeMouseHandling();

            /*
             * Indicate that this edit is complete by resetting the input
             * handler so that this one is no longer used.
             */
            getSpatialDisplay().handleUserResetOfInputMode();
            return true;
        }
        return false;
    }

    // Protected Methods

    /**
     * Determine whether or not the specified drawable can be edited in the
     * manner provided by the subclass.
     * 
     * @param drawable
     *            Drawable to be checked.
     * @return <code>true</code> if the drawable may be edited in the manner
     *         provided by the subclass, <code>false</code> otherwise.
     */
    protected abstract boolean isEditableViaHandler(
            AbstractDrawableComponent drawable);

    /**
     * Given the specified drawable, manipulation point, and new location of the
     * latter, modify the drawable to reflect the changes made due to the
     * point's movement.
     * 
     * @param drawable
     *            Drawable to be modified.
     * @param manipulationPoint
     *            Manipulation point being moved.
     * @param newLocation
     *            New location of the manipulation point.
     */
    protected abstract void modifyDrawableForManipulationPointMove(
            AbstractDrawableComponent drawable, M manipulationPoint,
            Coordinate newLocation);

    // Private Methods

    /**
     * Handle the movement of the manipulation point being used in this edit to
     * the specified new location.
     * 
     * @param newLocation
     *            New location in world coordinates of the manipulation point.
     */
    protected void handleManipulationPointMovement(Coordinate newLocation) {
        AbstractDrawableComponent editedDrawable = getSpatialDisplay()
                .getDrawableBeingEdited();
        if ((editedDrawable != null) && isEditableViaHandler(editedDrawable)) {

            /*
             * If this is the beginning of the edit, copy the drawable being
             * edited to be used as a ghost. The ghost is the one that will
             * actually be changed.
             */
            if (getGhostDrawable() == null) {
                createGhostDrawable(editedDrawable);
            }
            AbstractDrawableComponent ghostDrawable = getGhostDrawable();

            /*
             * Modify the ghost drawable to reflect the changes created by the
             * movement of the manipulation point.
             */
            modifyDrawableForManipulationPointMove(ghostDrawable,
                    getManipulationPointUnderMouse(), newLocation);

            /*
             * Tell the spatial display about the new ghost drawable.
             */
            getSpatialDisplay().setGhostOfDrawableBeingEdited(ghostDrawable);
            getSpatialDisplay().updateBoundingBoxDrawable(editedDrawable,
                    ghostDrawable);
            getSpatialDisplay().setHandlebarPoints(
                    ((IDrawable<?>) ghostDrawable).getManipulationPoints());
            getSpatialDisplay().issueRefresh();
        }
    }
}

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.swt.widgets.Event;

import com.raytheon.viz.ui.VizWorkbenchManager;
import com.raytheon.viz.ui.editor.AbstractEditor;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import gov.noaa.gsd.viz.hazards.spatialdisplay.SpatialDisplay;
import gov.noaa.gsd.viz.hazards.spatialdisplay.SpatialDisplay.CursorType;
import gov.noaa.gsd.viz.hazards.spatialdisplay.entities.IEntityIdentifier;
import gov.noaa.gsd.viz.hazards.spatialdisplay.selectbyarea.SelectByAreaDbMapResource;

/**
 * Description: Input handler for select-by-area mode.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Jul 05, 2016   19537    Chris.Golden Initial creation (adapted from old
 *                                      SelectByAreaDrawingActionGeometryResource
 *                                      inner class).
 * Jun 07, 2017   34206    Kevin.Bisanz Check for userData on Geometry before equals()
 *                                      in isContainedInSelectedGeometries(..)
 * Jun 29, 2017   34206    Kevin.Bisanz Revert previous change under this ticket.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class SelectByAreaInputHandler extends BaseInputHandler {

    // Private Variables

    /**
     * Displayed select-by-area viz resource.
     */
    private SelectByAreaDbMapResource vizResource;

    /**
     * Identifier of the entity undergoing a select-by-area edit; if
     * <code>null</code>, a new geometry is being created.
     */
    private IEntityIdentifier identifier;

    /**
     * Currently selected geometries.
     * <p>
     * Note: This is a list instead of a set because
     * {@link Geometry#equals(Object)} and {@link Geometry#hashCode()} are
     * inextricably bound together, and the former cannot be used when looking
     * for a particular <code>Geometry</code> in this list; instead,
     * {@link Geometry#equals(Geometry)} is used. Since the latter would not
     * give the same results as the aforementioned <code>equals(Object)</code>,
     * a hash set would not work correctly. Use of a list avoids use of
     * <code>hashCode()</code> entirely.
     * </p>
     */
    private List<Geometry> selectedGeometries = new ArrayList<>();

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param spatialDisplay
     *            Spatial display to be used.
     */
    public SelectByAreaInputHandler(SpatialDisplay spatialDisplay) {
        super(spatialDisplay);
    }

    // Public Methods

    /**
     * Set the select-by-area viz resource and the geometries that are to be
     * selected to start with, and the entity identifier if appropriate.
     * 
     * @param vizResource
     *            Select-by-area viz resource with which this input handler is
     *            to be used.
     * @param selectedGeometries
     *            Geometries to be selected to start with.
     * @param identifier
     *            Identifier of the entity that is to be edited using
     *            select-by-area; if <code>null</code>, a new geometry is to be
     *            created.
     */
    public void setVizResourceAndSelectedGeometries(
            SelectByAreaDbMapResource vizResource,
            Set<Geometry> selectedGeometries, IEntityIdentifier identifier) {
        this.vizResource = vizResource;
        this.selectedGeometries = (selectedGeometries == null
                ? new ArrayList<Geometry>()
                : new ArrayList<>(selectedGeometries));
        this.identifier = identifier;
        vizResource.setSelectedGeometries(
                Collections.unmodifiableList(this.selectedGeometries));
    }

    @Override
    public void reset() {
        getSpatialDisplay().setCursor(CursorType.DRAW_CURSOR);
    }

    @Override
    public boolean handleMouseEnter(Event event) {
        getSpatialDisplay().setCursor(CursorType.DRAW_CURSOR);
        return true;
    }

    @Override
    public boolean handleMouseDown(int x, int y, int mouseButton) {

        /*
         * Ignore mouse button 2 events. If mouse button 1 was pressed, get the
         * geometry under the cursor and toggle its selection state.
         */
        if (mouseButton == 2) {
            return false;
        } else if (mouseButton == 1) {

            AbstractEditor editor = (AbstractEditor) VizWorkbenchManager
                    .getInstance().getActiveEditor();

            Coordinate coordinates = editor.translateClick(x, y);

            Geometry geometry = vizResource
                    .getGeometryContainingLocation(coordinates);

            if (geometry != null) {

                /*
                 * If the geometry is within the selected geometries, remove it;
                 * if not, add it.
                 */
                if (isContainedInSelectedGeometries(geometry, true)) {
                    selectedGeometries.remove(geometry);
                } else {
                    selectedGeometries.add(geometry);
                }
                vizResource.issueRefresh();
            }
        }
        return true;
    }

    @Override
    public boolean handleMouseDownMove(int x, int y, int mouseButton) {

        /*
         * Ignore mouse button 2 drags, and do nothing with mouse button 3
         * drags.
         */
        if (mouseButton == 2) {
            return false;
        } else if (mouseButton == 3) {
            return true;
        }

        /*
         * For mouse button 1 drags, find the geometry that is under the cursor.
         */
        AbstractEditor editor = (AbstractEditor) VizWorkbenchManager
                .getInstance().getActiveEditor();
        Coordinate coordinates = editor.translateClick(x, y);
        Geometry geometry = vizResource
                .getGeometryContainingLocation(coordinates);

        /*
         * If a geometry is found and it is not already selected, add it to the
         * list of selected geometries and tell the viz resource to update.
         */
        if ((geometry != null) && (isContainedInSelectedGeometries(geometry,
                false) == false)) {
            selectedGeometries.add(geometry);
            vizResource.issueRefresh();
        }

        return true;
    }

    @Override
    public boolean handleMouseUp(int x, int y, int mouseButton) {

        /*
         * Do nothing with mouse button 1 events, and ignore mouse button 2
         * events.
         */
        if (mouseButton == 1) {
            return true;
        } else if (mouseButton == 2) {
            return false;
        }

        /*
         * For mouse button 3 events, finish up the select-by-area process.
         */
        if ((selectedGeometries != null) && (selectedGeometries.size() > 0)) {
            getSpatialDisplay().handleUserSelectByAreaDrawingActionComplete(
                    identifier, new HashSet<>(selectedGeometries));
        }
        return true;
    }

    /**
     * Determine whether or not the specified geometry is within the list of
     * currently selected geometries. If it is, optionally replace the one in
     * the list with this one.
     * <p>
     * This method is used instead of the usual {@link List#contains(Object)}
     * because {@link Geometry} objects' implementations of
     * {@link Geometry#equals(Object)} does not always see equivalent geometries
     * as equal. Thus, this method uses {@link Geometry#equals(Geometry)}
     * instead to find an equivalent.
     * </p>
     * 
     * @param selectedGeometry
     *            Geometry to be found in the selected geometries list.
     * @param replaceIfFound
     *            Flag indicating whether or not to replace the geometry found
     *            in the list that is equivalent to
     *            <code>selectedGeometry</code> with the latter.
     * @return <code>true</code> if the geometry is found, <code>false</code>
     *         otherwise.
     * @see #selectedGeometries
     */
    private boolean isContainedInSelectedGeometries(Geometry selectedGeometry,
            boolean replaceIfFound) {
        for (int j = 0; j < selectedGeometries.size(); ++j) {
            if (selectedGeometries.get(j).equals(selectedGeometry)) {
                if (replaceIfFound) {
                    selectedGeometries.set(j, selectedGeometry);
                }
                return true;
            }
        }
        return false;
    }
}

/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Evaluation & Decision Support Branch (EDS)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.spatialdisplay;

import gov.noaa.gsd.viz.hazards.spatialdisplay.drawables.ManipulationPoint;
import gov.noaa.nws.ncep.ui.pgen.elements.AbstractDrawableComponent;

import com.vividsolutions.jts.geom.Polygon;

/**
 * Description: Information about a mutable (editable or movable) drawable near
 * a particular point.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Jul 20, 2016   19537    Chris.Golden Initial creation.
 * Sep 29, 2016   15928    Chris.Golden Changed to hold a reference to
 *                                      a manipulation point for a mutaable
 *                                      drawable, instead of a vertex index.
 *                                      Also changed to support editing
 *                                      polygons with holes in them.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class MutableDrawableInfo {

    // Private Variables

    /**
     * Mutable drawable.
     */
    private final AbstractDrawableComponent drawable;

    /**
     * Flag indicating whether the drawable is active or not.
     */
    private final boolean active;

    /**
     * Index of the edge within the drawable's geometry that the point is close
     * to, if any. If <code>-1</code>, no edge is close. Note that only when a
     * point is close to the edge of one of the holes in a {@link Polygon} with
     * holes in it (said <code>Polygon</code> being used as the drawable's
     * geometry) will this hold a value greater than <code>0</code>; for any
     * open geometry, or closed geometry with no holes, the only edge index
     * possible is <code>0</code> if the point is close to the edge.
     */
    private final int edgeIndex;

    /**
     * Manipulation point under the point for which this information was
     * generated, if the drawable has one there. If not, this will be
     * <code>-1</code>.
     */
    private final ManipulationPoint manipulationPoint;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param drawable
     *            Mutable drawable.
     * @param active
     *            Flag indicating whether the drawable is active or not.
     * @param edgeIndex
     *            Index of the edge within the drawable's geometry that the
     *            point is close to, if any. If <code>-1</code>, no edge is
     *            close. Note that only when a point is close to the edge of one
     *            of the holes in a {@link Polygon} with holes in it (said
     *            <code>Polygon</code> being used as the drawable's geometry)
     *            will this hold a value greater than <code>0</code>; for any
     *            open geometry, or closed geometry with no holes, the only edge
     *            index possible is <code>0</code> if the point is close to the
     *            edge.
     * @param manipulationPoint
     *            Manipulation point under the point for which this information
     *            was generated, if the drawable has one there. If not, this
     *            will be <code>-1</code>.
     */
    public MutableDrawableInfo(AbstractDrawableComponent drawable,
            boolean active, int edgeIndex, ManipulationPoint manipulationPoint) {
        this.drawable = drawable;
        this.active = active;
        this.edgeIndex = edgeIndex;
        this.manipulationPoint = manipulationPoint;
    }

    // Public Methods

    /**
     * Get the mutable drawable.
     * 
     * @return Mutable drawable.
     */
    public AbstractDrawableComponent getDrawable() {
        return drawable;
    }

    /**
     * Return the flag indicating whether the drawable is active or not.
     * 
     * @return <code>true</code> if the drawable is active, <code>false</code>
     *         otherwise.
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Return the index of the edge within the drawable's geometry that the
     * point is close to, if any.
     * 
     * @return Index of the edge within the drawable's geometry that the point
     *         is close to, if any. If <code>-1</code>, no edge is close. Note
     *         that only when a point is close to the edge of one of the holes
     *         in a {@link Polygon} with holes in it (said <code>Polygon</code>
     *         being used as the drawable's geometry) will this hold a value
     *         greater than <code>0</code>; for any open geometry, or closed
     *         geometry with no holes, the only edge index possible is
     *         <code>0</code> if the point is close to the edge.
     */
    public int getEdgeIndex() {
        return edgeIndex;
    }

    /**
     * Get the manipulation point under the point for which this information was
     * generated, if the drawable has one there.
     * 
     * @return Manipulation point under the point for which this information was
     *         generated, if the drawable has one there. If not, this will be
     *         <code>-1</code>.
     */
    public ManipulationPoint getManipulationPoint() {
        return manipulationPoint;
    }

    @Override
    public String toString() {
        return (drawable == null ? "(none)" : drawable + " (active = " + active
                + ", close to edge index = " + edgeIndex
                + ", manipulationPoint = " + manipulationPoint + ")");
    }
}

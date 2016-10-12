/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Evaluation & Decision Support Branch (EDS)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.spatialdisplay.drawables;

import gov.noaa.gsd.viz.hazards.spatialdisplay.SpatialDisplay.CursorType;
import gov.noaa.nws.ncep.ui.pgen.elements.AbstractDrawableComponent;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * Description: Vertex manipulation point, holding the location and index of a
 * drawable's vertex that, if dragged by the user, allows said vertex to be
 * moved.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Sep 29, 2016   15928    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class VertexManipulationPoint extends ManipulationPoint {

    // Private Variables

    /**
     * Index of the linear ring of the polygon in which this vertex is found.
     * <code>0</code> means the outer shell, while a positive number <i>N</i>
     * means the <i>(N - 1)</i>th hole in the polygon.
     */
    private final int linearRingIndex;

    /**
     * Index of the vertex.
     */
    private final int vertexIndex;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param drawable
     *            Drawable to which this point applies.
     * @param location
     *            Location of the point.
     * @param linearRingIndex
     *            Index of the linear ring of the polygon in which this vertex
     *            is found. <code>0</code> means the outer shell, while a
     *            positive number <i>N</i> means the <i>(N - 1)</i>th hole in
     *            the polygon.
     * @param vertexIndex
     *            Index of the vertex.
     */
    public VertexManipulationPoint(AbstractDrawableComponent drawable,
            Coordinate location, int linearRingIndex, int vertexIndex) {
        super(drawable, location);
        this.linearRingIndex = linearRingIndex;
        this.vertexIndex = vertexIndex;
    }

    // Public Methods

    /**
     * Get the index of the linear ring within which the vertex is found.
     * 
     * @return Index of the linear ring of the polygon in which this vertex is
     *         found. <code>0</code> means the outer shell, while a positive
     *         number <i>N</i> means the <i>(N - 1)</i>th hole in the polygon.
     */
    public int getLinearRingIndex() {
        return linearRingIndex;
    }

    /**
     * Get the index of the vertex.
     * 
     * @return Index of the vertex.
     */
    public int getVertexIndex() {
        return vertexIndex;
    }

    @Override
    public CursorType getCursor() {
        return CursorType.MOVE_VERTEX_CURSOR;
    }

    @Override
    public String toString() {
        return "vertex ("
                + (linearRingIndex == 0 ? "outer shell" : "hole index = "
                        + linearRingIndex) + ", index = " + vertexIndex + ")";
    }
}

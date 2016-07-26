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

import gov.noaa.nws.ncep.ui.pgen.elements.AbstractDrawableComponent;

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
     * Flag indicating whether the drawable's edge is close to the point for
     * which this information was generated.
     */
    private final boolean closeToEdge;

    /**
     * Index of the vertex under the point for which this information was
     * generated, if the drawable is editable and a vertex lies close to said
     * point. If not, this will be <code>-1</code>.
     */
    private final int vertexIndex;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param drawable
     *            Mutable drawable.
     * @param closeToEdge
     *            Flag indicating whether the drawable's edge is close to the
     *            point for which this information was generated.
     * @param vertexIndex
     *            Index of the vertex under the point for which this information
     *            was generated, if the drawable is editable and a vertex lies
     *            close to said point. If not, this will be <code>-1</code>.
     */
    public MutableDrawableInfo(AbstractDrawableComponent drawable,
            boolean closeToEdge, int vertexIndex) {
        this.drawable = drawable;
        this.closeToEdge = closeToEdge;
        this.vertexIndex = vertexIndex;
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
     * Return the flag indicating whether the editable drawable's edge is close
     * to the point for which this information was generated.
     * 
     * @return Flag indicating whether or not the point is close to the editable
     *         drawable's edge.
     */
    public boolean isCloseToEdge() {
        return closeToEdge;
    }

    /**
     * Get the index of the vertex under the point for which this information
     * was generated.
     * 
     * @return Index of the vertex under the point for which this information
     *         was generated, if the drawable is editable and a vertex lies
     *         close to said point. If not, this will be <code>-1</code>.
     */
    public int getVertexIndex() {
        return vertexIndex;
    }
}

/**
 * This software was developed and / or modified by the National Oceanic and
 * Atmospheric Administration (NOAA), Earth System Research Laboratory (ESRL),
 * Global Systems Division (GSD), Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.spatialdisplay.drawables;

import gov.noaa.gsd.viz.hazards.spatialdisplay.entities.IEntityIdentifier;
import gov.noaa.nws.ncep.ui.pgen.elements.DECollection;

import com.vividsolutions.jts.geom.Geometry;

/**
 * 
 * Description: Interface describing the methods that must be implemented by all
 * drawables used within the spatial display.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 03, 2012            bryon.lawrence      Initial creation
 * Jul 18, 2013   1264     Chris.Golden        Added support for drawing lines and
 *                                             points.
 * Feb 09, 2015 6260       Dan Schaffer        Fixed bugs in multi-polygon handling
 * Mar 16, 2016 15676      Chris.Golden        Added code to support visual features.
 * Mar 26, 2016 15676      Chris.Golden        Added visual feature identifier.
 * Jun 23, 2016 19537      Chris.Golden        Changed to use better identifiers.
 * Jul 25, 2016 19537      Chris.Golden        Renamed.
 * </pre>
 * 
 * @author bryon.lawrence
 * @version 1.0
 */
public interface IDrawable {

    /**
     * Get the identifier.
     * 
     * @return Identifier.
     */
    public IEntityIdentifier getIdentifier();

    /**
     * Returns the {@link Geometry} version of this shape. All shapes have an
     * associated JTS geometries.
     * 
     * @return JTS geometry representing this shape.
     */
    public Geometry getGeometry();

    /**
     * Get the index of the sub-geometry this shape represents within the
     * overall geometry represented by this shape's {@link DECollection}.
     * 
     * @return Index of the sub-geometry within the overall geometry, or
     *         <code>-1</code> if this shape does not represent a sub-geometry
     *         (or if it does, but it is the only sub-geometry within a
     *         collection).
     */
    public int getGeometryIndex();

    /**
     * @return True if the user can edit this shape.
     */
    public boolean isEditable();

    /**
     * Set the editable status of this shape.
     * 
     * @param editable
     *            true if this shape is editable.
     */
    public void setEditable(boolean editable);

    /**
     * @return True if the user can move this shape.
     */
    public boolean isMovable();

    /**
     * Set the movable status of this shape.
     * 
     * @param isMovable
     *            true if this shape is movable.
     */
    public void setMovable(boolean movable);
}

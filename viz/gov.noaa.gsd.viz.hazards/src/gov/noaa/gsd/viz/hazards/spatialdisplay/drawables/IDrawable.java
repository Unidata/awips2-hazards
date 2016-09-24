/**
 * This software was developed and / or modified by the National Oceanic and
 * Atmospheric Administration (NOAA), Earth System Research Laboratory (ESRL),
 * Global Systems Division (GSD), Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.spatialdisplay.drawables;

import gov.noaa.gsd.common.utilities.geometry.IAdvancedGeometry;
import gov.noaa.gsd.viz.hazards.spatialdisplay.entities.IEntityIdentifier;
import gov.noaa.nws.ncep.ui.pgen.elements.DECollection;

/**
 * 
 * Description: Interface describing the methods that must be implemented by all
 * drawables used within the spatial display. The generic parameter
 * <code>G</code> provides the geometry type that is associated with
 * implementing subclasses.
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
 * Sep 12, 2016 15934      Chris.Golden        Changed to work with advanced
 *                                             geometries.
 * Sep 20, 2016 15934      Chris.Golden        Changed to include methods required
 *                                             for curved geometries, as well as
 *                                             methods to allow copying and
 *                                             translation (offsetting by deltas).
 * </pre>
 * 
 * @author bryon.lawrence
 * @version 1.0
 */
public interface IDrawable<G extends IAdvancedGeometry> {

    /**
     * Get the identifier.
     * 
     * @return Identifier.
     */
    public IEntityIdentifier getIdentifier();

    /**
     * Returns the geometry version of this shape.
     * 
     * @return Geometry representing this shape, or <code>null</code> if no
     *         geometry is associated with it.
     */
    public G getGeometry();

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
     * Determine whether or not the shape is editable.
     * 
     * @return <code>true</code> if the user can edit this shape,
     *         <code>false</code> otherwise.
     */
    public boolean isEditable();

    /**
     * Set the editable status of this shape.
     * 
     * @param editable
     *            Flag indicating whether or not this shape is editable.
     */
    public void setEditable(boolean editable);

    /**
     * Determine whether or not the shape is resizable.
     * 
     * @return <code>true</code> if the user can resize this shape,
     *         <code>false</code> otherwise.
     */
    public boolean isResizable();

    /**
     * Set the resizable status of this shape.
     * 
     * @param editable
     *            Flag indicating whether or not this shape is resizable.
     */
    public void setResizable(boolean resizable);

    /**
     * Determine whether or not the shape is rotatable.
     * 
     * @return <code>true</code> if the user can rotate this shape,
     *         <code>false</code> otherwise.
     */
    public boolean isRotatable();

    /**
     * Set the rotatable status of this shape.
     * 
     * @param rotatable
     *            Flag indicating whether or not this shape is rotatable.
     */
    public void setRotatable(boolean rotatable);

    /**
     * Determine whether or not the shape is editable.
     * 
     * @return <code>true</code> if the user can move this shape,
     *         <code>false</code> otherwise.
     */
    public boolean isMovable();

    /**
     * Set the movable status of this shape.
     * 
     * @param movable
     *            Flag indicating whether or not this shape is movable.
     */
    public void setMovable(boolean movable);

    /**
     * Get a copy of this shape in which the backing {@link IAdvancedGeometry}
     * is copied, not just a reference to the original drawable's geometry.
     * 
     * @return Copy of this shape.
     */
    public <D extends IDrawable<?>> D copyOf();

    /**
     * Offset this drawable by the specified deltas. This offsets the backing
     * {@link IAdvancedGeometry} as well.
     * 
     * @param x
     *            Longitudinal offset.
     * @param y
     *            Latitudinal offset.
     */
    public void offsetBy(double x, double y);
}

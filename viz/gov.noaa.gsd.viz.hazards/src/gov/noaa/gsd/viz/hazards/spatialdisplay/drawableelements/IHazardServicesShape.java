/**
 * This software was developed and / or modified by the National Oceanic and
 * Atmospheric Administration (NOAA), Earth System Research Laboratory (ESRL),
 * Global Systems Division (GSD), Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.spatialdisplay.drawableelements;

import gov.noaa.gsd.viz.hazards.spatialdisplay.VisualFeatureSpatialIdentifier;

import com.vividsolutions.jts.geom.Geometry;

/**
 * 
 * Description: Interface for all IHIS drawables.
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
 * </pre>
 * 
 * @author bryon.lawrence
 * @version 1.0
 */
public interface IHazardServicesShape {

    /**
     * Get the drawable attributes.
     * 
     * @return Drawable attributes.
     */
    public HazardServicesDrawingAttributes getDrawingAttributes();

    /**
     * Get the identifier.
     * 
     * @return Identifier.
     */
    public VisualFeatureSpatialIdentifier getIdentifier();

    /**
     * Returns the JTS geometry version of this shape. All Hazard Services
     * shapes have an associated JTS geometries. This allows the use of JTS
     * tools.
     * 
     * @return JTS geometry representing this shape.
     */
    public Geometry getGeometry();

    /**
     * Determine whether or not this is a visual feature. This determination is
     * made by checking to see if a visual feature identifier has been set to
     * something other than <code>null</code> with the
     * {@link #setVisualFeatureIdentifier(String)} method.
     * 
     * This method is deprecated as it will disappear once all shapes are visual
     * features.
     */
    @Deprecated
    public boolean isVisualFeature();

    /**
     * @return True if the user can edit this shape.
     */
    public boolean isEditable();

    /**
     * @return True if the user can move this shape.
     */
    public boolean isMovable();

    /**
     * Set the editable status of this shape.
     * 
     * @param editable
     *            true if this shape is editable.
     */
    public void setEditable(boolean editable);

    /**
     * Set the movable status of this shape.
     * 
     * @param isMovable
     *            true if this shape is movable.
     */
    public void setMovable(boolean movable);
}

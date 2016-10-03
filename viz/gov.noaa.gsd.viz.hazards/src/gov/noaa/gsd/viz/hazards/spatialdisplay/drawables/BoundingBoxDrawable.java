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

import gov.noaa.gsd.common.utilities.geometry.GeometryWrapper;

import java.util.Collections;
import java.util.List;

/**
 * Description: Bounding box drawable shape, used to indicate the bounding box
 * of other multi-point drawables.
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
public class BoundingBoxDrawable extends PathDrawable {

    // Private Variables

    /**
     * Drawable that is bounded by this drawable.
     */
    private final MultiPointDrawable<?> boundedDrawable;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param boundedDrawable
     *            Drawable that is to be bounded.
     * @param attributes
     *            Drawable attributes.
     * @param geometry
     *            Bounding box. Drawable that is to be bounded.
     */
    public BoundingBoxDrawable(MultiPointDrawable<?> boundedDrawable,
            DrawableAttributes attributes, GeometryWrapper geometry) {
        super(boundedDrawable.getIdentifier(), attributes, geometry);
        this.boundedDrawable = boundedDrawable;
        setClosed(true);
        setEditable(false);
        setRotatable(false);
        setResizable(false);
        setMovable(false);
    }

    // Protected Constructors

    /**
     * Construct a copy instance. This is intended to be used by implementations
     * of {@link #copyOf()}.
     * 
     * @param other
     *            Drawable to be copied.
     */
    protected BoundingBoxDrawable(BoundingBoxDrawable other) {
        super(other);
        this.boundedDrawable = other.boundedDrawable;
    }

    // Public Methods

    /**
     * Get the drawable bounded by this one.
     * 
     * @return Drawable bounded by this one.
     */
    public MultiPointDrawable<?> getBoundedDrawable() {
        return boundedDrawable;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <D extends IDrawable<?>> D copyOf() {
        return (D) new BoundingBoxDrawable(this);
    }

    @Override
    public String toString() {
        return getIdentifier() + " (bounding box)";
    }

    // Protected Methods

    @Override
    protected List<ManipulationPoint> getUpdatedManipulationPoints() {
        return Collections.emptyList();
    }
}

/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.spatialdisplay.drawableelements;

import gov.noaa.gsd.viz.hazards.spatialdisplay.VisualFeatureSpatialIdentifier;
import gov.noaa.nws.ncep.ui.pgen.elements.DrawableElement;
import gov.noaa.nws.ncep.ui.pgen.elements.Line;

import java.awt.Color;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Description: A hazard services drawing shape.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 23, 2013   1264      daniel.s.schaffer  Initial creation
 * Feb 09, 2015 6260       Dan Schaffer        Fixed bugs in multi-polygon handling
 * Mar 16, 2016 15676      Chris.Golden        Added code to support visual features.
 * Mar 26, 2016 15676      Chris.Golden        Added visual feature identifier.
 * </pre>
 * 
 * @author daniel.s.schaffer@noaa.gov
 * @version 1.0
 */
public abstract class HazardServicesShape extends Line implements
        IHazardServicesShape {

    private final VisualFeatureSpatialIdentifier identifier;

    private boolean editable = true;

    private boolean movable = true;

    private final HazardServicesDrawingAttributes drawingAttributes;

    public HazardServicesShape(VisualFeatureSpatialIdentifier identifier,
            HazardServicesDrawingAttributes drawingAttributes) {
        this.identifier = identifier;
        this.drawingAttributes = drawingAttributes;
    }

    @Override
    public VisualFeatureSpatialIdentifier getIdentifier() {
        return identifier;
    }

    @Override
    public HazardServicesDrawingAttributes getDrawingAttributes() {
        return drawingAttributes;
    }

    @Override
    public abstract Geometry getGeometry();

    @Override
    public boolean isVisualFeature() {
        return (identifier.getVisualFeatureIdentifier() != null);
    }

    @Override
    public boolean isEditable() {
        return editable;
    }

    @Override
    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    @Override
    public boolean isMovable() {
        return movable;
    }

    @Override
    public void setMovable(boolean movable) {
        this.movable = movable;
    }

    /*
     * Overridden to ensure that colors' alpha components are copied as well, as
     * the superclass does not do this; it only copies the RGB components of the
     * colors.
     */
    @Override
    public DrawableElement copy() {
        Line copy = (Line) super.copy();
        Color[] colors = new Color[getColors().length];
        for (int j = 0; j < getColors().length; j++) {
            colors[j] = new Color(getColors()[j].getRed(),
                    getColors()[j].getGreen(), getColors()[j].getBlue(),
                    getColors()[j].getAlpha());
        }
        copy.setColors(colors);
        return copy;
    }
}

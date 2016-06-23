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
import gov.noaa.nws.ncep.ui.pgen.elements.Layer;

import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * Description: TODO
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * MMM DD, YYYY            Chris.Golden      Initial creation
 * Jun 23, 2016 19537      Chris.Golden      Changed to use better identifiers.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class HazardServicesPoint extends HazardServicesSymbol {

    /**
     * Flag indicating whether this is an outer or inner portion of the point.
     */
    private final boolean isOuter;

    /**
     * Create an inner or outer portion of a point.
     * 
     * @param drawingAttributes
     *            The attributes controlling the appearance of this Symbol.
     * @param pgenCategory
     *            The PGEN category of this symbol, e.g.
     * @param pgenType
     *            The PGEN type of this symbol, e.g.
     * @param points
     *            The points in this symbol
     * @param activeLayer
     *            The PGEN layer this symbol will be drawn to.
     * @param identifier
     *            The identifier of this symbol.
     * @param isOuter
     *            Flag indicating whether or not this is the outer portion of
     *            the point.
     */
    public HazardServicesPoint(
            HazardServicesDrawingAttributes drawingAttributes,
            String pgenCategory, String pgenType, List<Coordinate> points,
            Layer activeLayer, VisualFeatureSpatialIdentifier identifier,
            boolean isOuter) {
        super(drawingAttributes, pgenCategory, pgenType, points, activeLayer,
                identifier);
        this.isOuter = isOuter;
    }

    /**
     * Determine whether this symbol is the outer part of a point.
     * 
     * @return Flag indicating whether or not this symbol is the outer part of a
     *         point.
     */
    public boolean isOuter() {
        return isOuter;
    }
}

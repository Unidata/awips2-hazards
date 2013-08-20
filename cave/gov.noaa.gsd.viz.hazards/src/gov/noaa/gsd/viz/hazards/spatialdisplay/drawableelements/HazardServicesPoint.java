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

import gov.noaa.gsd.viz.hazards.spatialdisplay.HazardServicesDrawingAttributes;
import gov.noaa.nws.ncep.ui.pgen.elements.Layer;

import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * Description: Point symbol as used by Hazard Services.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug  9, 2013 1921       daniel.s.schaffer@noaa.gov  initial creation
 * </pre>
 * 
 * @author daniel.s.schaffer@noaa.gov
 * @version 1.0
 */
public class HazardServicesPoint extends HazardServicesSymbol {

    /**
     * Construct a standard empty instance.
     */
    public HazardServicesPoint() {
    }

    /**
     * Construct a standard instance.
     * 
     * @param drawingAttributes
     *            Attributes controlling the appearance of this point.
     * @param pgenCategory
     *            PGEN category of this point.
     * @param pgenType
     *            PGEN type of this point.
     * @param coordinates
     *            Coordinates making up the visuals of this point.
     * @param activeLayer
     *            PGEN layer this point will be drawn to.
     * @param eventID
     *            Identifier of this point.
     */
    public HazardServicesPoint(
            HazardServicesDrawingAttributes drawingAttributes,
            String pgenCategory, String pgenType, List<Coordinate> points,
            Layer activeLayer, String eventID) {
        super(drawingAttributes, pgenCategory, pgenType, points, activeLayer,
                eventID);
    }
}

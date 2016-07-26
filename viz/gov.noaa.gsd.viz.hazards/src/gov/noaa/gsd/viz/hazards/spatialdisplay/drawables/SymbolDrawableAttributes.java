/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.spatialdisplay.drawables;

/**
 * Description: Drawing attributes for a Hazard Services point.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 18, 2013     1264   Chris.Golden      Initial creation
 * Aug  9, 2013 1921       daniel.s.schaffer Support of replacement of JSON with POJOs
 * Dec 05, 2014     4124   Chris.Golden      Changed to work with newly parameterized
 *                                           config manager.
 * Feb 09, 2015 6260       Dan Schaffer      Fixed bugs in multi-polygon handling
 * Oct 13, 2015 12494      Chris Golden      Reworked to allow hazard types to include
 *                                           only phenomenon (i.e. no significance) where
 *                                           appropriate.
 * Mar 16, 2016 15676      Chris.Golden      Moved to more appropriate location.
 * Mar 24, 2016 15676      Chris.Golden      Added dotted line style.
 * Jul 25, 2016 19537      Chris.Golden      Renamed, and removed unneeded member data and
 *                                           methods. Also changed to not be base class,
 *                                           since no point-drawing attributes subclass is
 *                                           needed.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class SymbolDrawableAttributes extends DrawableAttributes {

    // Public Enumerated Types

    /**
     * Part of the point that a drawing attributes object is intended for.
     */
    public enum Element {
        INNER, OUTER
    };

    // Private Variables

    private double sizeScale;

    private Element element = Element.INNER;

    // Public Constructors

    /**
     * Construct a standard instance for an inner element.
     */
    public SymbolDrawableAttributes() {
        this(Element.INNER);
        setClosedLine(true);
        setFilled(true);
    }

    /**
     * Construct a standard instance.
     * 
     * @param element
     *            Element of a point for which these attributes are intended.
     */
    public SymbolDrawableAttributes(Element element) {
        this.element = element;
    }

    @Override
    public double getSizeScale() {
        return sizeScale;
    }

    /**
     * Get the element of the point for which these attributes are intended.
     * 
     * @return Element of the point.
     */
    public Element getElement() {
        return element;
    }

    public void setSizeScale(double sizeScale) {
        this.sizeScale = sizeScale;
    }
}

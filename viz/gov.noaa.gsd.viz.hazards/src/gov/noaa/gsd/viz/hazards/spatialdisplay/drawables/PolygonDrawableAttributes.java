/**
 * This software was developed and / or modified by the National Oceanic and
 * Atmospheric Administration (NOAA), Earth System Research Laboratory (ESRL),
 * Global Systems Division (GSD), Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.spatialdisplay.drawables;

/**
 * <Description> The PGEN drawing attributes associated with a polygon drawn on
 * the Spatial Display in Hazard Services. All drawables in Hazard Services are
 * rendered using PGEN drawing classes.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Bryon.Lawrence      Initial induction into repo
 * Aug  9, 2013 1921       daniel.s.schaffer@noaa.gov  Support of replacement of JSON with POJOs
 * Nov 18, 2013 1462       Bryon.Lawrence      Added a constructor which allows
 *                                             the fill state of a polygon
 *                                             to be specified.
 * Dec 05, 2014 4124       Chris.Golden        Changed to work with newly parameterized
 *                                             config manager.
 * Mar 16, 2016 15676      Chris.Golden        Moved to more appropriate location.
 * Mar 24, 2016 15676      Chris.Golden        Added ability to set size scale, and to use
 *                                             dotted line style.
 * Jul 25, 2016 19537      Chris.Golden        Renamed, and removed unneeded member data and
 *                                             methods.
 * </pre>
 * 
 * @author Bryon.Lawrence
 */
public class PolygonDrawableAttributes extends DrawableAttributes {

    public static double SIZE_SCALE = 7.5;

    private double sizeScale = SIZE_SCALE;

    public PolygonDrawableAttributes(boolean filled) {
        setFilled(filled);
        setClosedLine(true);
    }

    /**
     * The size scale for polygons governs the size of the line style pattern
     * elements if it does not have a solid outline.
     */
    @Override
    public double getSizeScale() {
        return sizeScale;
    }

    /**
     * Set the size scale. Note that the size scale for polygons governs the
     * size of the line style pattern elements if it does not have a solid
     * outline.
     * 
     * @param sizeScale
     *            New size scale.
     */
    public void setSizeScale(double sizeScale) {
        this.sizeScale = sizeScale;
    }
}

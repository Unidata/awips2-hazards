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

/**
 * Description: TODO
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 19, 2013  1921         daniel.s.schaffer@noaa.gov      Initial creation
 * Mar 16, 2016 15676      Chris.Golden        Moved to more appropriate location.
 * Mar 24, 2016 15676      Chris.Golden        Added additional line styles.
 * </pre>
 * 
 * @author daniel.s.schaffer@noaa.gov
 * @version 1.0
 */
public enum LineStyle {

    LINE_SOLID("LINE_SOLID"), LINE_DASHED_2("LINE_DASHED_2"), LINE_DASHED_3(
            "LINE_DASHED_3"), LINE_DASHED_4("LINE_DASHED_4");

    private final String name;

    private LineStyle(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

}

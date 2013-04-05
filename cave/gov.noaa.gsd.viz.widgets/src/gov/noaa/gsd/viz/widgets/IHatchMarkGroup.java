/**
 * This software was developed and / or modified by the National Oceanic and
 * Atmospheric Administration (NOAA), Earth System Research Laboratory (ESRL),
 * Global Systems Division (GSD), Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.widgets;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;

/**
 * Hatch mark group, an interface that must be implemented in order to specify
 * the properties of a particular group of hatch marks (vertical line and their
 * labels). Hatch marks are visual elements of the "ruler" display of a
 * <code>MultiValueRuler</code>. Each group is made up of hatch marks of the
 * same height and color and with the same interval between them; thus, for
 * example, a ruler acting as a time line might have a hatch mark group for
 * days, another for six-hour increments, another for hours, and so on.
 * <p>
 * If an implementation's <code>getHeightFraction()</code> returns 1.0, then a
 * horizontal dividing line will be drawn below the labels using the color
 * fetched via <code>getColor()</code>.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Chris.Golden      Initial induction into repo
 * 
 * </pre>
 * 
 * @see MultiValueRuler
 * @author Chris.Golden
 */
public interface IHatchMarkGroup {

    // Public Enumerated Types

    /**
     * Label positions, the possible ways in which to position hatch mark labels
     * with respect to the hatch marks they are labeling.
     */
    public enum LabelPosition {

        // Values

        /**
         * Value indicating each label should be positioned right above the
         * hatch mark it is annotating.
         */
        OVER_HATCH_MARK,

        /**
         * Value indicating each label should be positioned between the hatch
         * mark it is annotating and the next hatch mark from the same group.
         */
        BETWEEN_HATCH_MARKS
    }

    // Public Methods

    /**
     * Get the interval between hatch marks for this group.
     * 
     * @return Interval between hatch marks for this group.
     */
    public long getInterval();

    /**
     * Get the height of the vertical line drawn to represent the hatch mark, as
     * a fraction of the total height of the ruler, with 0 meaning that no line
     * is drawn, and 1 meaning that the line is drawn from the bottom up to the
     * top of the ruler.
     * 
     * @return Height of the vertical line.
     */
    public float getHeightFraction();

    /**
     * Get the color to be used for the hatch mark at the specified value.
     * 
     * @return Color to be used for the hatch mark at the specified value; if
     *         <code>null</code>, the widget's foreground color will be used
     *         instead.
     */
    public Color getColor();

    /**
     * Get the font to be used to draw labels.
     * 
     * @return Font to be used to draw labels, or <code>null</code> if the
     *         widget font is to be used.
     */
    public Font getFont();

    /**
     * Get the longest possible label that this group may use for labeling its
     * hatch marks.
     * 
     * @return Longest possible label that this group may use for labeling its
     *         hatch marks.
     */
    public String getLongestLabel();

    /**
     * Get the label for the hatch mark at the specified value.
     * 
     * @param value
     *            Value for which the label is to be fetched.
     * @return Label for the hatch mark at this value.
     */
    public String getLabel(long value);

    /**
     * Get the horizontal positioning of the labels with respect to the hatch
     * marks they are labeling.
     * 
     * @return Horizontal positioning of the labels.
     */
    public IHatchMarkGroup.LabelPosition getLabelPosition();
}
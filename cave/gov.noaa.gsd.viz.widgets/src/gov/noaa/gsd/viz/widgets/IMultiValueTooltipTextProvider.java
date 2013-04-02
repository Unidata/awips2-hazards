/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.widgets;

/**
 * Interface describing the methods that must be implemented in order to be a
 * multi-value linear control tooltip text provider. The latter may be used to
 * enable the showing of tooltips over multi-value linear controls.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see MultiValueLinearControl
 */
public interface IMultiValueTooltipTextProvider {

    // Public Methods

    /**
     * Get the tooltip text for the specified non-thumb, non-marked value.
     * 
     * @param widget
     *            Widget requesting the text.
     * @param value
     *            Value for which the text is being requested.
     * @return One- or two-element array of strings providing the text to be
     *         displayed in a tooltip above the specified value, or
     *         <code>null</code> if no tooltip should be shown. If one string is
     *         provided, it is to be the body of the tooltip; if two are
     *         provided, the first is the title of the tooltip and the second
     *         the body.
     */
    public String[] getTooltipTextForValue(MultiValueLinearControl widget,
            long value);

    /**
     * Get the tooltip text for the specified constrained thumb value.
     * 
     * @param widget
     *            Widget requesting the text.
     * @param index
     *            Index of the contrained thumb value for which text is being
     *            requested.
     * @param value
     *            Value of the thumb.
     * @return One- or two-element array of strings providing the text to be
     *         displayed in a tooltip above the specified thumb, or
     *         <code>null</code> if no tooltip should be shown. If one string is
     *         provided, it is to be the body of the tooltip; if two are
     *         provided, the first is the title of the tooltip and the second
     *         the body.
     */
    public String[] getTooltipTextForConstrainedThumb(
            MultiValueLinearControl widget, int index, long value);

    /**
     * Get the tooltip text for the specified free thumb value.
     * 
     * @param widget
     *            Widget requesting the text.
     * @param index
     *            Index of the free thumb value for which text is being
     *            requested.
     * @param value
     *            Value of the thumb.
     * @return One- or two-element array of strings providing the text to be
     *         displayed in a tooltip above the specified thumb, or
     *         <code>null</code> if no tooltip should be shown. If one string is
     *         provided, it is to be the body of the tooltip; if two are
     *         provided, the first is the title of the tooltip and the second
     *         the body.
     */
    public String[] getTooltipTextForFreeThumb(MultiValueLinearControl widget,
            int index, long value);
}
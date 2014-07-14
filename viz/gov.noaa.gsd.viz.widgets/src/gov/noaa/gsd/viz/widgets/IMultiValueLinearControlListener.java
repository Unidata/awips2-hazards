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
 * multi-value linear control event listener.
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
 * @author Chris.Golden
 * @version 1.0
 * @see MultiValueLinearControl
 */
public interface IMultiValueLinearControlListener {

    // Public Methods

    /**
     * Receive notification that the specified multi-value linear control's
     * visible value range has changed.
     * 
     * @param widget
     *            Control widget whose visible value range has changed.
     * @param lowerValue
     *            New lowest visible value.
     * @param upperValue
     *            New highest visible value.
     * @param source
     *            Source of the change.
     */
    public void visibleValueRangeChanged(MultiValueLinearControl widget,
            long lowerValue, long upperValue,
            MultiValueLinearControl.ChangeSource source);

    /**
     * Receive notification that at least one of the specified multi-value
     * linear control's constrained thumb values has changed.
     * 
     * @param widget
     *            Control widget whose value or values have changed.
     * @param values
     *            Array indicating in order the new values. The length of the
     *            array is equal to the number of constrained thumbs that the
     *            widget has.
     * @param source
     *            Source of the change.
     */
    public void constrainedThumbValuesChanged(MultiValueLinearControl widget,
            long[] values, MultiValueLinearControl.ChangeSource source);

    /**
     * Receive notification that at least one of the specified multi-value
     * linear control's free thumb values has changed.
     * 
     * @param widget
     *            Control widget whose value or values have changed.
     * @param values
     *            Array indicating the new values. The length of the array is
     *            equal to the number of free thumbs that the widget has.
     * @param source
     *            Source of the change.
     */
    public void freeThumbValuesChanged(MultiValueLinearControl widget,
            long[] values, MultiValueLinearControl.ChangeSource source);
}
/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.megawidgets.displaysettings;

import com.google.common.collect.Range;

/**
 * Description: Interface describing the methods that must be implemented by a
 * display settings object that includes information about text editing element,
 * that is, a megawidget consisting of a text box holding arbitrary text. The
 * generic parameter <code>T</code> provides the type of the text,
 * <code>C</code> provides the type used to hold position information for the
 * caret and the selection range, and <code>P</code> provides the type of the
 * point used to represent a scroll origin.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Feb 12, 2015    4756    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public interface ITextSettings<T, C extends Comparable<C>, P> extends
        ISinglePageScrollSettings<P> {

    /**
     * Get the text.
     * 
     * @return Text.
     */
    public T getText();

    /**
     * Set the text.
     * 
     * @param text
     *            Text.
     */
    public void setText(T text);

    /**
     * Get the selection range.
     * 
     * @return Selection range.
     */
    public Range<C> getSelectionRange();

    /**
     * Set the selection range.
     * 
     * @param range
     *            Selection range.
     */
    public void setSelectionRange(Range<C> range);

    /**
     * Get the caret position.
     * 
     * @return Caret position.
     */
    public C getCaretPosition();

    /**
     * Set the caret position.
     * 
     * @param position
     *            Caret position.
     */
    public void setCaretPosition(C position);
}

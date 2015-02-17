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

import gov.noaa.gsd.viz.megawidgets.IMegawidget;

import com.google.common.collect.Range;

/**
 * Description: Display settings object that includes information about text
 * editing element, that is, a megawidget consisting of a text box holding
 * arbitrary text. The generic parameter <code>T</code> provides the type of the
 * text, <code>C</code> provides the type used to hold position information for
 * the caret and the selection range, and <code>P</code> provides the type of
 * the point used to represent a scroll origin.
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
public class TextSettings<T, C extends Comparable<C>, P> extends
        SinglePageScrollSettings<P> implements ITextSettings<T, C, P> {

    // Private Variables

    /**
     * Text.
     */
    private T textContents;

    /**
     * Selection range.
     */
    private Range<C> selectionRange;

    /**
     * Caret position.
     */
    private C caretPosition;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param megawidgetClass
     *            Class of the megawidget to which these display settings apply.
     */
    public TextSettings(Class<? extends IMegawidget> megawidgetClass) {
        super(megawidgetClass);
    }

    @Override
    public T getText() {
        return textContents;
    }

    @Override
    public void setText(T text) {
        textContents = text;
    }

    @Override
    public Range<C> getSelectionRange() {
        return selectionRange;
    }

    @Override
    public void setSelectionRange(Range<C> range) {
        selectionRange = range;
    }

    @Override
    public C getCaretPosition() {
        return caretPosition;
    }

    @Override
    public void setCaretPosition(C position) {
        caretPosition = position;
    }
}

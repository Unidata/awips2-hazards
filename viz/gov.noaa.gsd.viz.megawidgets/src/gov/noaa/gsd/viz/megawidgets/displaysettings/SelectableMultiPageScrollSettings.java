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

/**
 * Description: Multi-page scrollable display settings for a megawidget that has
 * as part of its display properties a selected index. The generic parameter
 * <code>P</code> provides the type of the point used to represent a scroll
 * origin, while <code>S</code> gives the type of the selection.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Oct 20, 2014    4818    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class SelectableMultiPageScrollSettings<P, S> extends
        MultiPageScrollSettings<P> {

    // Private Variables

    /**
     * Selection.
     */
    private S selection = null;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param megawidgetClass
     *            Class of the megawidget to which these display settings apply.
     */
    public SelectableMultiPageScrollSettings(
            Class<? extends IMegawidget> megawidgetClass) {
        super(megawidgetClass);
    }

    // Public Methods

    /**
     * Get the Selection.
     * 
     * @return Selection, or <code>null</code> if there is no selection.
     */
    public S getSelection() {
        return selection;
    }

    /**
     * Set the selection.
     * 
     * @param selection
     *            Selection; if <code>null</code>, there is no selection.
     */
    public void setSelection(S selection) {
        this.selection = selection;
    }
}

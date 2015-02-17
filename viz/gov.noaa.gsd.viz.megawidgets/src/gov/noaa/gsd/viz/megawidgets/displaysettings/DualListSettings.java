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
 * Description: Display settings object holding information about the selected
 * choices and scroll position of two different lists, that is, a megawidget
 * with two vertically scrollable elements, each one containing a series of
 * choices. The generic parameter <code>C</code> provides the type of the
 * choices found within the lists.
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
public class DualListSettings<C> extends DisplaySettings {

    // Private Variables

    /**
     * First list settings.
     */
    private final ListSettings<C> firstListSettings;

    /**
     * Second list settings.
     */
    private final ListSettings<C> secondListSettings;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param megawidgetClass
     *            Class of the megawidget to which these display settings apply.
     */
    public DualListSettings(Class<? extends IMegawidget> megawidgetClass) {
        super(megawidgetClass);
        firstListSettings = new ListSettings<C>(megawidgetClass);
        secondListSettings = new ListSettings<C>(megawidgetClass);
    }

    // Public Methods

    /**
     * Get the first list settings.
     * 
     * @return First list settings.
     */
    public ListSettings<C> getFirstListSettings() {
        return firstListSettings;
    }

    /**
     * Get the second list settings.
     * 
     * @return Second list settings.
     */
    public ListSettings<C> getSecondListSettings() {
        return secondListSettings;
    }
}

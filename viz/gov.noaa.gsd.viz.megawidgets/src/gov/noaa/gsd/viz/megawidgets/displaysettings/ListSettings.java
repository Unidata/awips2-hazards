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

import java.util.Set;

/**
 * Description: Display settings object holding information about the selected
 * choices and scroll position of a list, that is, a megawidget with one
 * vertically scrollable element containing a series of choices. The generic
 * parameter <code>C</code> provides the type of the choices found within the
 * list.
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
public class ListSettings<C> extends DisplaySettings implements
        IListSettings<C> {

    // Private Variables

    /**
     * Topmost visible choice.
     */
    private C topmostVisibleChoice;

    /**
     * Selected choices.
     */
    private Set<C> selectedChoices;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param megawidgetClass
     *            Class of the megawidget to which these display settings apply.
     */
    public ListSettings(Class<? extends IMegawidget> megawidgetClass) {
        super(megawidgetClass);
    }

    // Public Methods

    @Override
    public C getTopmostVisibleChoice() {
        return topmostVisibleChoice;
    }

    @Override
    public void setTopmostVisibleChoice(C choice) {
        topmostVisibleChoice = choice;
    }

    @Override
    public Set<C> getSelectedChoices() {
        return selectedChoices;
    }

    @Override
    public void setSelectedChoices(Set<C> choices) {
        selectedChoices = choices;
    }
}

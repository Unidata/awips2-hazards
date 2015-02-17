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

import java.util.Set;

/**
 * Description: Interface describing the methods that must be implemented by a
 * display settings object that includes information about the selected choices
 * and scroll position of a list, that is, a megawidget with one vertically
 * scrollable element containing a series of choices. The generic parameter
 * <code>C</code> provides the type of the choices found within the list.
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
public interface IListSettings<C> extends IDisplaySettings {

    /**
     * Get the choice at the top of the scrollable viewport.
     * 
     * @return Choice at the top of the scrollable viewport.
     */
    public C getTopmostVisibleChoice();

    /**
     * Set the choice at the top of the scrollable viewport.
     * 
     * @param choice
     *            Choice at the top of the scrollable viewport.
     */
    public void setTopmostVisibleChoice(C choice);

    /**
     * Get the selected choices.
     * 
     * @return Selected choices.
     */
    public Set<C> getSelectedChoices();

    /**
     * Set the selected choices.
     * 
     * @param choices
     *            Selected choices.
     */
    public void setSelectedChoices(Set<C> choices);
}

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

import java.util.List;
import java.util.Set;

/**
 * Description: Interface describing the methods that must be implemented by a
 * display settings object that includes information about the selected choices
 * and scroll position of a hierarchical list, that is, a megawidget with one
 * vertically scrollable element containing a series of hierarchical choices.
 * The generic parameter <code>C</code> provides the type of the choices found
 * within the list, relative to each one's parent. Thus, if <code>C</code> is of
 * type {@link String}, each choice is specified as a {@link List} of type
 * {@link String} objects, with the first item in the list giving the topmost
 * choice in that choice's hierarchy, down to the second to last item in the
 * list holding the choice's parent, and the last item being the choice itself.
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
public interface IHierarchicalListSettings<C> extends IListSettings<List<C>> {

    /**
     * Get the choices that are expanded, that is, that are not hiding their
     * children.
     * 
     * @return Choices that are expanded.
     */
    public Set<List<C>> getExpandedChoices();

    /**
     * Set the choices that are expanded, that is, that are not hiding their
     * children.
     * 
     * @param choices
     *            Choices that are expanded.
     */
    public void setExpandedChoices(Set<List<C>> choices);
}

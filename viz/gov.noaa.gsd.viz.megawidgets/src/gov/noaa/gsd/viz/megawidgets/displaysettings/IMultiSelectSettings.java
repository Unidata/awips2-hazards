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

import java.util.Collection;
import java.util.Set;

/**
 * Description: Interface describing the methods that must be implemented by a
 * display settings object that includes information about which items are
 * selected in a multi-selection megawidget. The generic parameter
 * <code>S</code> gives the type of the selection.
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
public interface IMultiSelectSettings<S> extends IDisplaySettings {

    /**
     * Determine whether or not the specified item is part of the selection.
     * 
     * @param item
     *            Item to be checked.
     * @return True if the item is part of the selection, false otherwise.
     */
    public boolean isSelected(S item);

    /**
     * Get the selection set.
     * 
     * @return Selection set.
     */
    public Set<S> getSelectedItems();

    /**
     * Clear the selection set.
     */
    public void clearSelectedItems();

    /**
     * Add the specified item to the selection set.
     * 
     * @param item
     *            Item to be selected.
     */
    public void addSelectedItem(S item);

    /**
     * Add the specified items to the selection set.
     * 
     * @param items
     *            Items to be selected.
     */
    public void addSelectedItems(Collection<S> items);

    /**
     * Remove the specified item from the selection set.
     * 
     * @param item
     *            Item to be deselected.
     */
    public void removeSelectedItem(S item);

    /**
     * Remove the specified items from the selection set.
     * 
     * @param items
     *            Items to be deselected.
     */
    public void removeSelectedItems(Collection<S> items);
}

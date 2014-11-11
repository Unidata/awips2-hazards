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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Description: Multi-select display settings. The generic parameter
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
public class MultiSelectSettings<S> extends DisplaySettings implements
        IMultiSelectSettings<S> {

    // Private Variables

    /**
     * Selection set.
     */
    private final Set<S> selection = new HashSet<S>();

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param megawidgetClass
     *            Class of the megawidget to which these display settings apply.
     */
    public MultiSelectSettings(Class<? extends IMegawidget> megawidgetClass) {
        super(megawidgetClass);
    }

    // Public Methods

    @Override
    public boolean isSelected(S item) {
        return selection.contains(item);
    }

    @Override
    public Set<S> getSelectedItems() {
        return new HashSet<>(selection);
    }

    @Override
    public void clearSelectedItems() {
        selection.clear();
    }

    @Override
    public void addSelectedItem(S item) {
        selection.add(item);
    }

    @Override
    public void addSelectedItems(Collection<S> items) {
        selection.addAll(items);
    }

    @Override
    public void removeSelectedItem(S item) {
        selection.remove(item);
    }

    @Override
    public void removeSelectedItems(Collection<S> items) {
        selection.removeAll(items);
    }
}

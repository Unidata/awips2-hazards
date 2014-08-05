/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.megawidgets;

/**
 * Description: Interface describing the methods that must be implemented by
 * megawidgets that use {@link ComboBoxComponentHelper} objects.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Aug 04, 2014    4122    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public interface IComboBoxComponentHolder {

    // Public Methods

    /**
     * Get the currently selected item.
     * 
     * @return Currently selected item.
     */
    public String getSelection();

    /**
     * Set the currently selected item to that specified.
     * 
     * @param item
     *            New currently selected item.
     */
    public void setSelection(String item);
}

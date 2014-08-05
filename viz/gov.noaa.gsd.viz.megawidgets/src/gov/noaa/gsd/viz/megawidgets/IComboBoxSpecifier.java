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
 * Interface describing the methods to be implemented by a megawidget specifier
 * that includes a combo box. Any subclasses of {@link MegawidgetSpecifier}
 * should implement this interface if they are to include a combo box.
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
public interface IComboBoxSpecifier extends ISpecifier {

    // Public Static Constants

    /**
     * Autocomplete parameter name; a combo box may include a boolean associated
     * with this name to indicate whether or not autocomplete should be enabled.
     * Autocomplete allows the user to type into the field and presents a
     * drop-down list of those choices that include the string so entered. If
     * not specified, autocomplete is disabled.
     */
    public static final String AUTOCOMPLETE_ENABLED = "autocomplete";

    // Public Methods

    /**
     * Determine whether or not autocomplete is enabled.
     * 
     * @return True if autocomplete is enabled, false otherwise.
     */
    public boolean isAutocompleteEnabled();
}
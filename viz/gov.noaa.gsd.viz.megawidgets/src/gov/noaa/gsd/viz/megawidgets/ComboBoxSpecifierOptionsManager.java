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

import java.util.Map;

/**
 * Description: Manager of any options associated with instances of
 * {@link IComboBoxSpecifier}. This class may be used by such classes to handle
 * the setting and getting of such options.
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
public class ComboBoxSpecifierOptionsManager {

    // Private Variables

    /**
     * Flag indicating whether or not autocomplete is enabled.
     */
    private final boolean autocomplete;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param specifier
     *            Specifier for which to manage the combo-box-related options.
     * @param parameters
     *            Map containing the parameters to be used to construct the
     *            megawidget specifier, including mappings for the options
     *            needed for the container aspect of said specifier.
     * @throws MegawidgetSpecificationException
     *             If the megawidget specifier parameters are invalid.
     */
    public ComboBoxSpecifierOptionsManager(MegawidgetSpecifier specifier,
            Map<String, Object> parameters)
            throws MegawidgetSpecificationException {

        /*
         * Get the autocomplete flag if available.
         */
        autocomplete = ConversionUtilities.getSpecifierBooleanValueFromObject(
                specifier.getIdentifier(), specifier.getType(),
                parameters.get(IComboBoxSpecifier.AUTOCOMPLETE_ENABLED),
                IComboBoxSpecifier.AUTOCOMPLETE_ENABLED, false);
    }

    // Public Methods

    /**
     * Determine whether or not autocomplete is enabled.
     * 
     * @return True if autocomplete is enabled, false otherwise.
     */
    public boolean isAutocompleteEnabled() {
        return autocomplete;
    }
}

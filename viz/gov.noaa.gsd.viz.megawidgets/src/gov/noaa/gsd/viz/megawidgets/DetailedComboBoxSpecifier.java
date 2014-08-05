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

import gov.noaa.gsd.viz.megawidgets.validators.SingleChoiceValidatorHelper;

import java.util.Map;

/**
 * Detailed combo box megawidget specifier. Each choice may have zero or more
 * detail fields associated with it, each of the latter being itself a
 * megawidget. The detail fields appear in a group-style panel below the combo
 * box.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Jul 23, 2014    4122    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see DetailedComboBoxMegawidget
 */
public class DetailedComboBoxSpecifier extends
        FlatChoicesWithDetailMegawidgetSpecifier<String> implements
        IContainerSpecifier<IControlSpecifier> {

    // Public Static Constants

    /**
     * Expand header to fill horizontal space parameter name; a detailed combo
     * box may include a boolean associated with this name to indicate whether
     * or not the header (label and combo box) should expand to fill any
     * available horizontal space that the grouping fills. If not specified, the
     * header is not expanded horizontally.
     */
    public static final String HEADER_EXPAND_HORIZONTALLY = "headerExpandHorizontally";

    // Private Variables

    /**
     * Container specifier options manager.
     */
    private final ContainerSpecifierOptionsManager<IControlSpecifier> containerOptionsManager;

    /**
     * Combo box options manager.
     */
    private final ComboBoxSpecifierOptionsManager comboBoxOptionsManager;

    /**
     * Flag indicating whether the header should expand to fill the available
     * space of the grouping horizontally.
     */
    private final boolean headerExpandHorizontally;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param parameters
     *            Map holding the parameters that will be used to configure a
     *            megawidget created by this specifier as a set of key-value
     *            pairs.
     * @throws MegawidgetSpecificationException
     *             If the megawidget specifier parameters are invalid.
     */
    public DetailedComboBoxSpecifier(Map<String, Object> parameters)
            throws MegawidgetSpecificationException {
        super(parameters, new SingleChoiceValidatorHelper(
                MEGAWIDGET_VALUE_CHOICES, CHOICE_NAME, CHOICE_IDENTIFIER), true);
        containerOptionsManager = new ContainerSpecifierOptionsManager<>(this,
                IControlSpecifier.class, parameters);
        comboBoxOptionsManager = new ComboBoxSpecifierOptionsManager(this,
                parameters);

        /*
         * Ensure that the header expand flag, if present, is acceptable.
         */
        headerExpandHorizontally = ConversionUtilities
                .getSpecifierBooleanValueFromObject(getIdentifier(), getType(),
                        parameters.get(HEADER_EXPAND_HORIZONTALLY),
                        HEADER_EXPAND_HORIZONTALLY, false);
    }

    @Override
    public int getLeftMargin() {
        return containerOptionsManager.getLeftMargin();
    }

    @Override
    public int getTopMargin() {
        return containerOptionsManager.getTopMargin();
    }

    @Override
    public int getRightMargin() {
        return containerOptionsManager.getRightMargin();
    }

    @Override
    public int getBottomMargin() {
        return containerOptionsManager.getBottomMargin();
    }

    @Override
    public int getColumnSpacing() {
        return containerOptionsManager.getColumnSpacing();
    }

    @Override
    public boolean isHorizontalExpander() {
        return containerOptionsManager.isHorizontalExpander();
    }

    @Override
    public boolean isVerticalExpander() {
        return containerOptionsManager.isVerticalExpander();
    }

    /**
     * Determine whether or not the megawidget's header is to expand to take up
     * available horizontal space.
     * 
     * @return Flag indicating whether or not the megawidget's header is to
     *         expand horizontally.
     */
    public boolean isHeaderHorizontalExpander() {
        return headerExpandHorizontally;
    }

    /**
     * Determine whether or not autocomplete is enabled.
     * 
     * @return True if autocomplete is enabled, false otherwise.
     */
    public final boolean isAutocompleteEnabled() {
        return comboBoxOptionsManager.isAutocompleteEnabled();
    }
}

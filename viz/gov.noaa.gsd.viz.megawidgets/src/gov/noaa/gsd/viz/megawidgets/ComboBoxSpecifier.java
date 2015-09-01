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
 * Combo box megawidget specifier.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Chris.Golden      Initial induction into repo
 * Apr 30, 2013   1277     Chris.Golden      Added support for mutable properties.
 * Oct 21, 2013   2168     Chris.Golden      Changed to implement ISingleLineSpecifier
 *                                           and use ControlSpecifierOptionsManager
 *                                           (composition over inheritance).
 * Oct 31, 2013   2336     Chris.Golden      Changed to accommodate alteration
 *                                           of framework to include notion
 *                                           of bounded (closed set) choices
 *                                           versus unbounded (sets to which
 *                                           arbitrary user-specified choices
 *                                           can be added) choice megawidgets.
 * Apr 24, 2014   2925     Chris.Golden      Changed to work with new validator
 *                                           package, updated Javadoc and other
 *                                           comments.
 * Jun 17, 2014   3982     Chris.Golden      Changed "isFullWidthOfColumn"
 *                                           property to "isFullWidthOfDetailPanel".
 * Aug 04, 2014   4122     Chris.Golden      Changed to include autocomplete
 *                                           functionality.
 * Aug 20, 2015   9617     Robert.Blum       Readonly property is now optional for
 *                                           comboboxes.
 * Aug 28, 2015   9617     Chris.Golden      Fixed code from previous entry for this
 *                                           ticket.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see ComboBoxMegawidget
 */
public class ComboBoxSpecifier extends
        FlatBoundedChoicesMegawidgetSpecifier<String> implements
        ISingleLineSpecifier, IComboBoxSpecifier {

    // Private Variables

    /**
     * Control options manager.
     */
    private final ControlSpecifierOptionsManager controlOptionsManager;

    /**
     * Combo box options manager.
     */
    private final ComboBoxSpecifierOptionsManager comboBoxOptionsManager;

    /**
     * Flag indicating whether or not the megawidget is to expand to fill all
     * available horizontal space within its parent.
     */
    private final boolean horizontalExpander;

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
    public ComboBoxSpecifier(Map<String, Object> parameters)
            throws MegawidgetSpecificationException {
        super(parameters, new SingleChoiceValidatorHelper(
                MEGAWIDGET_VALUE_CHOICES, CHOICE_NAME, CHOICE_IDENTIFIER));
        controlOptionsManager = new ControlSpecifierOptionsManager(this,
                parameters, ControlSpecifierOptionsManager.BooleanSource.FALSE);
        comboBoxOptionsManager = new ComboBoxSpecifierOptionsManager(this,
                parameters);

        /*
         * Get the horizontal expansion flag if available.
         */
        horizontalExpander = ConversionUtilities
                .getSpecifierBooleanValueFromObject(getIdentifier(), getType(),
                        parameters.get(EXPAND_HORIZONTALLY),
                        EXPAND_HORIZONTALLY, false);
    }

    // Public Methods

    @Override
    public final boolean isEditable() {
        return controlOptionsManager.isEditable();
    }

    @Override
    public final int getWidth() {
        return controlOptionsManager.getWidth();
    }

    @Override
    public final boolean isFullWidthOfDetailPanel() {
        return controlOptionsManager.isFullWidthOfDetailPanel();
    }

    @Override
    public final int getSpacing() {
        return controlOptionsManager.getSpacing();
    }

    @Override
    public final boolean isHorizontalExpander() {
        return horizontalExpander;
    }

    @Override
    public final boolean isAutocompleteEnabled() {
        return comboBoxOptionsManager.isAutocompleteEnabled();
    }

    @Override
    public boolean isAllowNewChoiceEnabled() {
        return comboBoxOptionsManager.isAllowNewChoiceEnabled();
    }
}

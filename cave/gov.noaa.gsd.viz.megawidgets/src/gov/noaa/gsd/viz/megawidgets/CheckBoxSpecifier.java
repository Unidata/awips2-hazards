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

import gov.noaa.gsd.viz.megawidgets.validators.BooleanValidator;

import java.util.Map;

/**
 * Checkbox megawidget specifier, providing the specification of a simple single
 * checkbox that sets a value to true or false. The boolean value is always
 * associated with a single state identifier, so the megawidget identifiers for
 * these specifiers must not consist of colon-separated substrings.
 * <p>
 * If multiple checkboxes are desired, grouped together under a label, the
 * {@link CheckBoxesSpecifier} may be more appropriate.
 * </p>
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 13, 2014    2161    Chris.Golden      Initial creation.
 * Apr 24, 2014    2925    Chris.Golden      Changed to work with new validator
 *                                           package, updated Javadoc and other
 *                                           comments.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see CheckBoxMegawidget
 */
public class CheckBoxSpecifier extends StatefulMegawidgetSpecifier implements
        ISingleLineSpecifier {

    // Private Variables

    /**
     * Control options manager.
     */
    private final ControlSpecifierOptionsManager optionsManager;

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
    public CheckBoxSpecifier(Map<String, Object> parameters)
            throws MegawidgetSpecificationException {
        super(parameters, new BooleanValidator());
        optionsManager = new ControlSpecifierOptionsManager(this, parameters,
                ControlSpecifierOptionsManager.BooleanSource.FALSE);

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
        return optionsManager.isEditable();
    }

    @Override
    public final int getWidth() {
        return optionsManager.getWidth();
    }

    @Override
    public final boolean isFullWidthOfColumn() {
        return optionsManager.isFullWidthOfColumn();
    }

    @Override
    public final int getSpacing() {
        return optionsManager.getSpacing();
    }

    @Override
    public final boolean isHorizontalExpander() {
        return horizontalExpander;
    }
}

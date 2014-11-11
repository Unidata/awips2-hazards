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

import gov.noaa.gsd.viz.megawidgets.validators.BoundedComparableValidator;

import java.util.Map;

/**
 * Description: Time megawidget specifier, providing specification of a
 * megawidget that allows the selection of a single date-time. The latter is
 * always associated with a single state identifier, so the megawidget
 * identifiers for these specifiers must not consist of colon-separated
 * substrings.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Dec 13, 2013    2545    Chris.Golden      Initial creation
 * Apr 24, 2014    2925    Chris.Golden      Changed to work with new validator
 *                                           package, updated Javadoc and other
 *                                           comments.
 * Oct 20, 2014    4818    Chris.Golden      Changed to never stretch across
 *                                           the full width of a details panel.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class TimeSpecifier extends TimeMegawidgetSpecifier implements
        ISingleLineSpecifier {

    // Private Variables

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
    public TimeSpecifier(Map<String, Object> parameters)
            throws MegawidgetSpecificationException {
        super(parameters, new BoundedComparableValidator<Long>(parameters,
                null, null, Long.class, 0L, Long.MAX_VALUE / 2L),
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
    public boolean isHorizontalExpander() {
        return horizontalExpander;
    }
}

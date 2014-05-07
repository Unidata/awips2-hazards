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
 * Bounded value megawidget specifier, a base class for specifiers that create
 * megawidgets allowing the selection of a single value bounded within a given
 * range. The latter is always associated with a single state identifier, so the
 * megawidget identifiers for these specifiers must not consist of
 * colon-separated substrings.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 30, 2013    1277    Chris.Golden      Initial creation.
 * Sep 26, 2013    2168    Chris.Golden      Changed erroneous "widget"
 *                                           references to "megawidget"
 *                                           in comments and variable
 *                                           names.
 * Apr 24, 2014    2925     Chris.Golden     Changed to work with new validator
 *                                           package, updated Javadoc and other
 *                                           comments.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see BoundedValueMegawidget
 */
public abstract class BoundedValueMegawidgetSpecifier<T extends Comparable<T>>
        extends StatefulMegawidgetSpecifier {

    // Public Static Constants

    /**
     * Minimum megawidget state value parameter name; a megawidget must include
     * a value of type <code>T</code> as the value associated with this name.
     * The provided value acts as the minimum value that the state is allowed to
     * take on. It must be less than the value associated with the parameter
     * {@link #MEGAWIDGET_MAX_VALUE}.
     */
    public static final String MEGAWIDGET_MIN_VALUE = "minValue";

    /**
     * Maximum megawidget state value parameter name; a megawidget must include
     * a value of type <code>T</code> as the value associated with this name.
     * The provided value acts as the maximum value that the state is allowed to
     * take on. It must be greater than the value associated with the parameter
     * {@link #MEGAWIDGET_MIN_VALUE}.
     */
    public static final String MEGAWIDGET_MAX_VALUE = "maxValue";

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param parameters
     *            Map holding the parameters that will be used to configure a
     *            megawidget created by this specifier as a set of key-value
     *            pairs.
     * @param stateValidator
     *            State validator.
     * @throws MegawidgetSpecificationException
     *             If the megawidget specifier parameters are invalid.
     */
    public BoundedValueMegawidgetSpecifier(Map<String, Object> parameters,
            BoundedComparableValidator<T> stateValidator)
            throws MegawidgetSpecificationException {
        super(parameters, stateValidator);
    }

    // Public Methods

    /**
     * Get the minimum value.
     * 
     * @return Minimum value.
     */
    @SuppressWarnings("unchecked")
    public final T getMinimumValue() {
        return ((BoundedComparableValidator<T>) getStateValidator())
                .getMinimumValue();
    }

    /**
     * Get the maximum value.
     * 
     * @return Maximum value.
     */
    @SuppressWarnings("unchecked")
    public final T getMaximumValue() {
        return ((BoundedComparableValidator<T>) getStateValidator())
                .getMaximumValue();
    }

    /**
     * Get the bounded value's class.
     * 
     * @return Bounded value's class.
     */
    @SuppressWarnings("unchecked")
    public final Class<T> getBoundedValueClass() {
        return ((BoundedComparableValidator<T>) getStateValidator())
                .getStateType();
    }
}

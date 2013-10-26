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
     * <code>MEGAWIDGET_MAX_VALUE</code>.
     */
    public static final String MEGAWIDGET_MIN_VALUE = "minValue";

    /**
     * Maximum megawidget state value parameter name; a megawidget must include
     * a value of type <code>T</code> as the value associated with this name.
     * The provided value acts as the maximum value that the state is allowed to
     * take on. It must be greater than the value associated with the parameter
     * <code>MEGAWIDGET_MIN_VALUE</code>.
     */
    public static final String MEGAWIDGET_MAX_VALUE = "maxValue";

    // Private Variables

    /**
     * Lowest allowable value that <code>minimumValue</code> can take on, or
     * <code>null</code> if there is no lower bound.
     */
    private final T lowestAllowableValue;

    /**
     * Highest allowable value that <code>maximumValue</code> can take on, or
     * <code>null</code> if there is no upper bound.
     */
    private final T highestAllowableValue;

    /**
     * Minimum value.
     */
    private final T minimumValue;

    /**
     * Maximum value.
     */
    private final T maximumValue;

    /**
     * Bounded value class, the class of <code>T</code>.
     */
    private final Class<T> boundedValueClass;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param parameters
     *            Map holding the parameters that will be used to configure a
     *            megawidget created by this specifier as a set of key-value
     *            pairs.
     * @param boundedValueClass
     *            Class of the bounded value; required in order to provide
     *            proper exception messages in situations where the bounding
     *            values are illegal.
     * @param lowest
     *            If not <code>null</code>, the lowest possible value for
     *            <code>MEGAWIDGET_MIN_VALUE</code>.
     * @param highest
     *            If not <code>null</code>, the highest possible value for
     *            <code>MEGAWIDGET_MAX_VALUE</code>.
     * @throws MegawidgetSpecificationException
     *             If the megawidget specifier parameters are invalid.
     */
    public BoundedValueMegawidgetSpecifier(Map<String, Object> parameters,
            Class<T> boundedValueClass, T lowest, T highest)
            throws MegawidgetSpecificationException {
        super(parameters);
        this.boundedValueClass = boundedValueClass;
        this.lowestAllowableValue = lowest;
        this.highestAllowableValue = highest;

        // Get the minimum value allowed.
        minimumValue = getSpecifierDynamicallyTypedObjectFromObject(
                parameters.get(MEGAWIDGET_MIN_VALUE), MEGAWIDGET_MIN_VALUE,
                boundedValueClass, null);
        if ((lowest != null) && (lowest.compareTo(minimumValue) > 0)) {
            throw new MegawidgetSpecificationException(getIdentifier(),
                    getType(), MEGAWIDGET_MIN_VALUE, minimumValue,
                    "must be no less than " + lowest);
        }

        // Get the maximum value allowed.
        maximumValue = getSpecifierDynamicallyTypedObjectFromObject(
                parameters.get(MEGAWIDGET_MAX_VALUE), MEGAWIDGET_MAX_VALUE,
                boundedValueClass, null);
        if ((highest != null) && (highest.compareTo(maximumValue) < 0)) {
            throw new MegawidgetSpecificationException(getIdentifier(),
                    getType(), MEGAWIDGET_MAX_VALUE, maximumValue,
                    "must be no greater than " + highest);
        }

        // Ensure that the minimum and maximum values are
        // valid relative to one another.
        if (minimumValue.compareTo(maximumValue) >= 0) {
            throw new MegawidgetSpecificationException(getIdentifier(),
                    getType(), null, null,
                    "minimum value must be less than maximum (minimum = "
                            + minimumValue + ", maximum = " + maximumValue
                            + ")");
        }
    }

    // Public Methods

    /**
     * Get the lowest allowable value that <code>getMinimumValue()</code> could
     * ever provide for this object, or <code>null</code> if there is no lower
     * bound.
     * 
     * @return Lowest allowable value.
     */
    public final T getLowestAllowableValue() {
        return lowestAllowableValue;
    }

    /**
     * Get the highest allowable value that <code>getMaximumValue()</code> could
     * ever provide for this object, or <code>null</code> if there is no upper
     * bound.
     * 
     * @return Highest allowable value.
     */
    public final T getHighestAllowableValue() {
        return highestAllowableValue;
    }

    /**
     * Get the minimum value.
     * 
     * @return Minimum value.
     */
    public final T getMinimumValue() {
        return minimumValue;
    }

    /**
     * Get the maximum value.
     * 
     * @return Maximum value.
     */
    public final T getMaximumValue() {
        return maximumValue;
    }

    /**
     * Get the bounded value's class.
     * 
     * @return Bounded value's class.
     */
    public final Class<T> getBoundedValueClass() {
        return boundedValueClass;
    }
}

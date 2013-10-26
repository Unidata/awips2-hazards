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
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

/**
 * Bounded value megawidget, a base class for megawidgets that allow the
 * selection of a single value bounded within a given range.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 30, 2013   1277     Chris.Golden      Initial creation.
 * Sep 26, 2013    2168    Chris.Golden      Changed erroneous "widget"
 *                                           references to "megawidget"
 *                                           in comments and variable
 *                                           names.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see BoundedValueMegawidgetSpecifier
 */
public abstract class BoundedValueMegawidget<T extends Comparable<T>> extends
        StatefulMegawidget {

    // Protected Static Constants

    /**
     * Set of all mutable property names for instances of this class.
     */
    protected static final Set<String> MUTABLE_PROPERTY_NAMES;
    static {
        Set<String> names = Sets
                .newHashSet(StatefulMegawidget.MUTABLE_PROPERTY_NAMES);
        names.add(BoundedValueMegawidgetSpecifier.MEGAWIDGET_MIN_VALUE);
        names.add(BoundedValueMegawidgetSpecifier.MEGAWIDGET_MAX_VALUE);
        MUTABLE_PROPERTY_NAMES = ImmutableSet.copyOf(names);
    };

    // Protected Variables

    /**
     * Current value.
     */
    protected T state;

    // Private Variables

    /**
     * Minimum value.
     */
    private T minimumValue;

    /**
     * Maximum value.
     */
    private T maximumValue;

    // Protected Constructors

    /**
     * Construct a standard instance.
     * 
     * @param specifier
     *            Specifier.
     * @param paramMap
     *            Hash table mapping megawidget creation time parameter
     *            identifiers to values.
     */
    protected BoundedValueMegawidget(
            BoundedValueMegawidgetSpecifier<T> specifier,
            Map<String, Object> paramMap) {
        super(specifier, paramMap);
        minimumValue = specifier.getMinimumValue();
        maximumValue = specifier.getMaximumValue();
    }

    // Public Methods

    @Override
    public Set<String> getMutablePropertyNames() {
        return MUTABLE_PROPERTY_NAMES;
    }

    @Override
    public Object getMutableProperty(String name)
            throws MegawidgetPropertyException {
        if (name.equals(BoundedValueMegawidgetSpecifier.MEGAWIDGET_MIN_VALUE)) {
            return getMinimumValue();
        } else if (name
                .equals(BoundedValueMegawidgetSpecifier.MEGAWIDGET_MAX_VALUE)) {
            return getMaximumValue();
        }
        return super.getMutableProperty(name);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setMutableProperty(String name, Object value)
            throws MegawidgetPropertyException {
        if (name.equals(BoundedValueMegawidgetSpecifier.MEGAWIDGET_MIN_VALUE)) {
            setMinimumValue(getPropertyDynamicallyTypedObjectFromObject(value,
                    BoundedValueMegawidgetSpecifier.MEGAWIDGET_MIN_VALUE,
                    ((BoundedValueMegawidgetSpecifier<T>) getSpecifier())
                            .getBoundedValueClass(), null));
        } else if (name
                .equals(BoundedValueMegawidgetSpecifier.MEGAWIDGET_MAX_VALUE)) {
            setMaximumValue(getPropertyDynamicallyTypedObjectFromObject(value,
                    BoundedValueMegawidgetSpecifier.MEGAWIDGET_MAX_VALUE,
                    ((BoundedValueMegawidgetSpecifier<T>) getSpecifier())
                            .getBoundedValueClass(), null));
        } else {
            super.setMutableProperty(name, value);
        }
    }

    @Override
    public void setMutableProperties(Map<String, Object> properties)
            throws MegawidgetPropertyException {

        // If the minimum or maximum values are being set, set them first,
        // ensuring that the two boundaries are set in the order that will
        // allow the set to occur without error (assuming that they are
        // allowable). This also ensures that if the state is being set,
        // it is done after the boundaries are set, so it will be allowed
        // assuming it is within the boundaries.
        BoundedValueMegawidgetSpecifier<T> specifier = getSpecifier();
        Object minValueObj = properties
                .get(BoundedValueMegawidgetSpecifier.MEGAWIDGET_MIN_VALUE);
        T minValue = (minValueObj == null ? null
                : getPropertyDynamicallyTypedObjectFromObject(minValueObj,
                        BoundedValueMegawidgetSpecifier.MEGAWIDGET_MIN_VALUE,
                        specifier.getBoundedValueClass(), null));
        Object maxValueObj = properties
                .get(BoundedValueMegawidgetSpecifier.MEGAWIDGET_MAX_VALUE);
        T maxValue = (maxValueObj == null ? null
                : getPropertyDynamicallyTypedObjectFromObject(maxValueObj,
                        BoundedValueMegawidgetSpecifier.MEGAWIDGET_MAX_VALUE,
                        specifier.getBoundedValueClass(), null));

        // If at least one of the two boundary values are being set, and
        // the minimum is less than the maximum, make sure that the re-
        // sulting range is representable. (If the minimum is not less
        // than the maximum, that will be caught further down; this
        // representability check is done first because it has to occur
        // before anything is set.)
        if ((minValue != null) || (maxValue != null)) {
            T min = (minValue != null ? minValue : minimumValue);
            T max = (maxValue != null ? maxValue : maximumValue);
            if (min.compareTo(max) < 0) {
                ensureValueRangeRepresentable(min, max);
            }
        }

        // If both boundary values are being changed, set them in the
        // order that will not cause a problem (lower bound must always
        // be less than upper bound). Otherwise, just set whichever one
        // is being changed.
        if ((minValue != null) && (maxValue != null)) {
            if (minValue.compareTo(maximumValue) >= 0) {
                setMutableProperty(
                        BoundedValueMegawidgetSpecifier.MEGAWIDGET_MAX_VALUE,
                        maxValue);
                setMutableProperty(
                        BoundedValueMegawidgetSpecifier.MEGAWIDGET_MIN_VALUE,
                        minValue);
            } else {
                setMutableProperty(
                        BoundedValueMegawidgetSpecifier.MEGAWIDGET_MIN_VALUE,
                        minValue);
                setMutableProperty(
                        BoundedValueMegawidgetSpecifier.MEGAWIDGET_MAX_VALUE,
                        maxValue);
            }
        } else if (minValue != null) {
            setMutableProperty(
                    BoundedValueMegawidgetSpecifier.MEGAWIDGET_MIN_VALUE,
                    minValue);
        } else if (maxValue != null) {
            setMutableProperty(
                    BoundedValueMegawidgetSpecifier.MEGAWIDGET_MAX_VALUE,
                    maxValue);
        }

        // Do what would have been done by the superclass method, except for
        // ignoring any min or max value setting, as that has already been
        // done above.
        for (String name : properties.keySet()) {
            if (!name
                    .equals(BoundedValueMegawidgetSpecifier.MEGAWIDGET_MIN_VALUE)
                    && !name.equals(BoundedValueMegawidgetSpecifier.MEGAWIDGET_MAX_VALUE)) {
                setMutableProperty(name, properties.get(name));
            }
        }
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
     * Set the minimum value.
     * 
     * @param value
     *            New minimum value; must be less than the current maximum
     *            value.
     * @throws MegawidgetPropertyException
     *             If the object is not less than the current maximum value.
     */
    public final void setMinimumValue(T value)
            throws MegawidgetPropertyException {

        // Ensure that the value is not null.
        BoundedValueMegawidgetSpecifier<T> specifier = getSpecifier();
        if (value == null) {
            throw new MegawidgetPropertyException(specifier.getIdentifier(),
                    BoundedValueMegawidgetSpecifier.MEGAWIDGET_MIN_VALUE,
                    specifier.getType(), value, "must be instance of "
                            + specifier.getBoundedValueClass(),
                    new NullPointerException());
        }

        // Ensure that the value is not lower than the lowest allowable
        // value for the lower end of the range, if any.
        T lowest = specifier.getLowestAllowableValue();
        if ((lowest != null) && (lowest.compareTo(value) > 0)) {
            throw new MegawidgetPropertyException(specifier.getIdentifier(),
                    BoundedValueMegawidgetSpecifier.MEGAWIDGET_MIN_VALUE,
                    specifier.getType(), value, "must be no less than "
                            + lowest);
        }

        // Ensure the value is less than the maximum value before using it.
        if (value.compareTo(maximumValue) >= 0) {
            throw new MegawidgetPropertyException(specifier.getIdentifier(),
                    BoundedValueMegawidgetSpecifier.MEGAWIDGET_MIN_VALUE,
                    specifier.getType(), value,
                    "minimum value must be less than maximum (minimum = "
                            + value + ", maximum = " + maximumValue);
        }
        minimumValue = value;

        // Synchronize the widgets to the new boundaries.
        synchronizeWidgetsToBounds();

        // If the state is less than the new minimum, set it to the minimum
        // and synchronize the widgets to the new state.
        if ((state == null) || (state.compareTo(minimumValue) < 0)) {
            state = minimumValue;
            synchronizeWidgetsToState();
        }
    }

    /**
     * Set the maximum value.
     * 
     * @param value
     *            New maximum value; must be greater than the current minimum
     *            value.
     * @throws MegawidgetPropertyException
     *             If the object is not greater than the current minimum value.
     */
    public final void setMaximumValue(T value)
            throws MegawidgetPropertyException {

        // Ensure that the value is not null.
        BoundedValueMegawidgetSpecifier<T> specifier = getSpecifier();
        if (value == null) {
            throw new MegawidgetPropertyException(specifier.getIdentifier(),
                    BoundedValueMegawidgetSpecifier.MEGAWIDGET_MAX_VALUE,
                    specifier.getType(), value, "must be instance of "
                            + specifier.getBoundedValueClass(),
                    new NullPointerException());
        }

        // Ensure that the value is not higher than the highest allowable
        // value for the upper end of the range, if any.
        T highest = specifier.getHighestAllowableValue();
        if ((highest != null) && (highest.compareTo(value) < 0)) {
            throw new MegawidgetPropertyException(specifier.getIdentifier(),
                    BoundedValueMegawidgetSpecifier.MEGAWIDGET_MAX_VALUE,
                    specifier.getType(), value, "must be no greater than "
                            + highest);
        }

        // Ensure the value is greater than the minimum value before using it.
        if (value.compareTo(minimumValue) <= 0) {
            throw new MegawidgetPropertyException(specifier.getIdentifier(),
                    BoundedValueMegawidgetSpecifier.MEGAWIDGET_MAX_VALUE,
                    specifier.getType(), value,
                    "maximum value must be greater than minimum (minimum = "
                            + minimumValue + ", maximum = " + value);
        }
        maximumValue = value;

        // Synchronize the widgets to the new boundaries.
        synchronizeWidgetsToBounds();

        // If the state is greater than the new maximum, set it to the maximum
        // and synchronize the widgets to the new state.
        if ((state == null) || (state.compareTo(maximumValue) > 0)) {
            state = maximumValue;
            synchronizeWidgetsToState();
        }
    }

    // Protected Methods

    /**
     * Ensure that the specified minimum to maximum value range is
     * representable.
     * 
     * @param minimum
     *            Minimum value to be checked.
     * @param maximum
     *            Maximum value to be checked.
     * @throws MegawidgetPropertyException
     *             If the range is not representable.
     */
    protected abstract void ensureValueRangeRepresentable(T minimum, T maximum)
            throws MegawidgetPropertyException;

    /**
     * Synchronize the user-facing widgets making up this megawidget to the
     * current boundaries.
     */
    protected abstract void synchronizeWidgetsToBounds();

    /**
     * Synchronize the user-facing widgets making up this megawidget to the
     * current state.
     */
    protected abstract void synchronizeWidgetsToState();

    @Override
    protected Object doGetState(String identifier) {
        return state;
    }

    @Override
    protected void doSetState(String identifier, Object state)
            throws MegawidgetStateException {

        // Ensure that the new state is within bounds and of the correct
        // type.
        BoundedValueMegawidgetSpecifier<T> specifier = getSpecifier();
        T value = getStateDynamicallyTypedObjectFromObject(state, identifier,
                specifier.getBoundedValueClass(), minimumValue);
        if ((value.compareTo(minimumValue) < 0)
                || (value.compareTo(maximumValue) > 0)) {
            throw new MegawidgetStateException(identifier, specifier.getType(),
                    value, "out of bounds (minimum = " + minimumValue
                            + ", maximum = " + maximumValue + " (inclusive))");
        }
        this.state = value;

        // Synchronize the widgets to the new state.
        synchronizeWidgetsToState();
    }

    @Override
    protected String doGetStateDescription(String identifier, Object state)
            throws MegawidgetStateException {
        return (state == null ? null : state.toString());
    }
}
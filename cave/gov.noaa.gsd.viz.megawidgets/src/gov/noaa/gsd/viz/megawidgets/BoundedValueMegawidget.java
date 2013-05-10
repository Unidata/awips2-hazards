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

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
 * 
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
        Set<String> names = new HashSet<String>(
                StatefulMegawidget.MUTABLE_PROPERTY_NAMES);
        names.add(BoundedValueMegawidgetSpecifier.MEGAWIDGET_MIN_VALUE);
        names.add(BoundedValueMegawidgetSpecifier.MEGAWIDGET_MAX_VALUE);
        MUTABLE_PROPERTY_NAMES = Collections.unmodifiableSet(names);
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

    /**
     * Get the mutable property names for this megawidget.
     * 
     * @return Set of names for all mutable properties for this megawidget.
     */
    @Override
    public Set<String> getMutablePropertyNames() {
        return MUTABLE_PROPERTY_NAMES;
    }

    /**
     * Get the current mutable property value for the specified name.
     * 
     * @param name
     *            Name of the mutable property value to be fetched.
     * @return Mutable property value.
     * @throws MegawidgetPropertyException
     *             If the name specifies a nonexistent property.
     */
    @Override
    public Object getMutableProperty(String name)
            throws MegawidgetPropertyException {
        if (name.equals(BoundedValueMegawidgetSpecifier.MEGAWIDGET_MIN_VALUE)) {
            return getMinimumValue();
        } else if (name
                .equals(BoundedValueMegawidgetSpecifier.MEGAWIDGET_MAX_VALUE)) {
            return getMaximumValue();
        } else {
            return super.getMutableProperty(name);
        }
    }

    /**
     * Set the current mutable property value for the specified name.
     * 
     * @param name
     *            Name of the mutable property value to be fetched.
     * @param value
     *            New mutable property value to be used.
     * @throws MegawidgetPropertyException
     *             If the name specifies a nonexistent property, or if the value
     *             is invalid.
     */
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

    /**
     * Set the mutable properties of this megawidget.
     * 
     * @param properties
     *            Map containing keys drawn from the set of all valid property
     *            names, with associated values being the new values for the
     *            properties. Any property with a name-value pair found within
     *            this map is set to the given value; all properties for which
     *            no name-value pairs exist remain as they were before.
     * @throws MegawidgetPropertyException
     *             If at least one name specifies a nonexistent property, or if
     *             at least one value is invalid.
     */
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
     * Synchronize the user-facing widgets making up this megawidget to the
     * current boundaries.
     */
    protected abstract void synchronizeWidgetsToBounds();

    /**
     * Synchronize the user-facing widgets making up this megawidget to the
     * current state.
     */
    protected abstract void synchronizeWidgetsToState();

    /**
     * Get the current state for the specified identifier. This method is called
     * by <code>getState()</code> only after the latter has ensured that the
     * supplied state identifier is valid.
     * 
     * @param identifier
     *            Identifier for which state is desired. Implementations may
     *            assume that the state identifier supplied by this parameter is
     *            valid for this megawidget.
     * @return Object making up the current state for the specified identifier.
     */
    @Override
    protected Object doGetState(String identifier) {
        return state;
    }

    /**
     * Set the current state for the specified identifier. This method is called
     * by <code>setState()</code> only after the latter has ensured that the
     * supplied state identifier is valid, and has set a flag that indicates
     * that this setting of the state will not trigger the megawidget to notify
     * its listener of an invocation.
     * 
     * @param identifier
     *            Identifier for which state is to be set. Implementations may
     *            assume that the state identifier supplied by this parameter is
     *            valid for this megawidget.
     * @param state
     *            Object making up the state to be used for this identifier, or
     *            <code>null</code> if this state should be reset.
     * @throws MegawidgetStateException
     *             If new state is not of a valid type for this <code>
     *             StatefulWidget</code> implementation.
     */
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

    /**
     * Get a shortened description of the specified state for the specified
     * identifier. This method is called by <code>getStateDescription() only
     * after the latter has ensured that the supplied state identifier is valid.
     * 
     * @param identifier
     *            Identifier to which the state would be assigned.
     *            Implementations may assume that the state identifier supplied
     *            by this parameter is valid for this megawidget.
     * @param state
     *            State for which to generate a shortened description.
     * @return Description of the specified state.
     * @throws MegawidgetStateException
     *             If the specified state is not of a valid type for this
     *             <code>StatefulWidget </code> implementation.
     */
    @Override
    protected String doGetStateDescription(String identifier, Object state)
            throws MegawidgetStateException {
        return (state == null ? null : state.toString());
    }
}
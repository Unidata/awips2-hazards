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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

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
 * Sep 26, 2013   2168     Chris.Golden      Changed erroneous "widget"
 *                                           references to "megawidget"
 *                                           in comments and variable
 *                                           names.
 * Apr 24, 2014   2925     Chris.Golden      Changed to work with new
 *                                           validator package, updated
 *                                           Javadoc and other comments.
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
        Set<String> names = new HashSet<>(
                StatefulMegawidget.MUTABLE_PROPERTY_NAMES);
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
     * State validator.
     */
    private final BoundedComparableValidator<T> stateValidator;

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
    @SuppressWarnings("unchecked")
    protected BoundedValueMegawidget(
            BoundedValueMegawidgetSpecifier<T> specifier,
            Map<String, Object> paramMap) {
        super(specifier, paramMap);
        stateValidator = specifier.getStateValidator().copyOf();
        state = (T) specifier.getStartingState(specifier.getIdentifier());
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

    @Override
    public void setMutableProperty(String name, Object value)
            throws MegawidgetPropertyException {
        if (name.equals(BoundedValueMegawidgetSpecifier.MEGAWIDGET_MIN_VALUE)) {
            doSetMinimumValue(value);
        } else if (name
                .equals(BoundedValueMegawidgetSpecifier.MEGAWIDGET_MAX_VALUE)) {
            doSetMaximumValue(value);
        } else {
            super.setMutableProperty(name, value);
        }
    }

    @Override
    public void setMutableProperties(Map<String, Object> properties)
            throws MegawidgetPropertyException {

        /*
         * If the minimum or maximum values are being set, set them before any
         * other mutable properties so that any state setting will occur after
         * this. Set them together as a range if both are changing; otherwise,
         * set whichever one is changing.
         */
        Object minValueObj = properties
                .get(BoundedValueMegawidgetSpecifier.MEGAWIDGET_MIN_VALUE);
        Object maxValueObj = properties
                .get(BoundedValueMegawidgetSpecifier.MEGAWIDGET_MAX_VALUE);
        if ((minValueObj != null) && (maxValueObj != null)) {
            doSetRange(minValueObj, maxValueObj);
        } else if (minValueObj != null) {
            doSetMinimumValue(minValueObj);
        } else if (maxValueObj != null) {
            doSetMaximumValue(maxValueObj);
        }

        /*
         * Do what would have been done by the superclass method, except for
         * ignoring any minimum or maximum value setting, as that has already
         * been done above.
         */
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
        return stateValidator.getMinimumValue();
    }

    /**
     * Get the maximum value.
     * 
     * @return Maximum value.
     */
    public final T getMaximumValue() {
        return stateValidator.getMaximumValue();
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
        doSetMinimumValue(value);
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
        doSetMaximumValue(value);
    }

    /**
     * Set the minimum and maximum values.
     * 
     * @param minimumValue
     *            New minimum value; must be less than <code>maximumValue</code>
     *            .
     * @param maximumValue
     *            New maximum value; must be greater than
     *            <code>minimumValue</code>.
     * @throws MegawidgetPropertyException
     *             If the new values are not valid.
     */
    public final void setRange(T minimumValue, T maximumValue)
            throws MegawidgetPropertyException {
        doSetRange(minimumValue, maximumValue);
    }

    // Protected Methods

    /**
     * Synchronize the user-facing component widgets making up this megawidget
     * to the current boundaries.
     */
    protected abstract void synchronizeComponentWidgetsToBounds();

    /**
     * Get the state validator.
     * 
     * @return State validator.
     */
    @SuppressWarnings("unchecked")
    protected final <V extends BoundedComparableValidator<T>> V getStateValidator() {
        return (V) stateValidator;
    }

    /**
     * Perform the actual work of setting the minimum value.
     * 
     * @param value
     *            New minimum value.
     * @throws MegawidgetPropertyException
     *             If the value is not valid.
     */
    protected final void doSetMinimumValue(Object value)
            throws MegawidgetPropertyException {

        /*
         * Set the new minimum value, ensuring it is valid.
         */
        stateValidator.setMinimumValue(value);

        /*
         * Synchronize the widgets to the new boundaries.
         */
        synchronizeComponentWidgetsToBounds();

        /*
         * If the state is less than the new minimum, set it to the minimum and
         * synchronize the widgets to the new state.
         */
        if ((state == null)
                || (state.compareTo(stateValidator.getMinimumValue()) < 0)) {
            state = stateValidator.getMinimumValue();
            synchronizeComponentWidgetsToState();
        }
    }

    /**
     * Perform the actual work of setting the maximum value.
     * 
     * @param value
     *            New maximum value.
     * @throws MegawidgetPropertyException
     *             If the value is not valid.
     */
    protected final void doSetMaximumValue(Object value)
            throws MegawidgetPropertyException {

        /*
         * Set the new maximum value, ensuring it is valid.
         */
        stateValidator.setMaximumValue(value);

        /*
         * Synchronize the widgets to the new boundaries.
         */
        synchronizeComponentWidgetsToBounds();

        /*
         * If the state is greater than the new maximum, set it to the maximum
         * and synchronize the widgets to the new state.
         */
        if ((state == null)
                || (state.compareTo(stateValidator.getMinimumValue()) < 0)) {
            state = stateValidator.getMinimumValue();
            synchronizeComponentWidgetsToState();
        }
    }

    /**
     * Perform the actual work of setting the minimum and maximum values.
     * 
     * @param minimumValue
     *            New minimum value; must be less than <code>maximumValue</code>
     *            .
     * @param maximumValue
     *            New maximum value; must be greater than
     *            <code>minimumValue</code>.
     * @throws MegawidgetPropertyException
     *             If the new values are not valid.
     */
    protected final void doSetRange(Object minimumValue, Object maximumValue)
            throws MegawidgetPropertyException {

        /*
         * Set the new range, ensuring it is valid.
         */
        stateValidator.setRange(minimumValue, maximumValue);

        /*
         * Synchronize the widgets to the new boundaries.
         */
        synchronizeComponentWidgetsToBounds();

        /*
         * If the state is outside the new boundaries, set it to whichever
         * boundary is closest and synchronize the widgets to the new state.
         */
        if ((state == null)
                || (state.compareTo(stateValidator.getMinimumValue()) < 0)) {
            state = stateValidator.getMinimumValue();
            synchronizeComponentWidgetsToState();
        } else if (state.compareTo(stateValidator.getMaximumValue()) > 0) {
            state = stateValidator.getMaximumValue();
            synchronizeComponentWidgetsToState();
        }
    }

    @Override
    protected Object doGetState(String identifier) {
        return state;
    }

    @Override
    protected void doSetState(String identifier, Object state)
            throws MegawidgetStateException {

        /*
         * Validate the new state and remember it.
         */
        try {
            this.state = stateValidator.convertToStateValue(state);
        } catch (MegawidgetException e) {
            throw new MegawidgetStateException(e);
        }

        /*
         * Synchronize the widgets to the new state.
         */
        synchronizeComponentWidgetsToState();
    }

    @Override
    protected String doGetStateDescription(String identifier, Object state)
            throws MegawidgetStateException {
        return (state == null ? null : state.toString());
    }
}
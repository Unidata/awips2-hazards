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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * Stateful megawidget created by a megawidget specifier.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Chris.Golden      Initial induction into repo
 * Apr 30, 2013   1277     Chris.Golden      Added support for mutable properties.
 * Oct 23, 2013   2168     Chris.Golden      Replaced erroneous references
 *                                           (variable names, comments, etc.) to
 *                                           "widget" with "megawidget" to avoid
 *                                           confusion.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see StatefulMegawidgetSpecifier
 */
public abstract class StatefulMegawidget extends NotifierMegawidget implements
        IStateful {

    // Protected Static Constants

    /**
     * Set of all mutable property names for instances of this class.
     */
    protected static final Set<String> MUTABLE_PROPERTY_NAMES;
    static {
        Set<String> names = Sets
                .newHashSet(NotifierMegawidget.MUTABLE_PROPERTY_NAMES);
        names.add(StatefulMegawidgetSpecifier.MEGAWIDGET_STATE_VALUES);
        MUTABLE_PROPERTY_NAMES = ImmutableSet.copyOf(names);
    };

    // Protected Variables

    /**
     * Flag indicating whether or not the setting of the state is currently
     * occurring.
     */
    protected boolean isSettingState = false;

    // Private Variables

    /**
     * State change listener, or <code>null</code> if no listener is currently
     * being used.
     */
    private final IStateChangeListener stateChangeListener;

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
    protected StatefulMegawidget(StatefulMegawidgetSpecifier specifier,
            Map<String, Object> paramMap) {
        super(specifier, paramMap);
        stateChangeListener = (IStateChangeListener) paramMap
                .get(STATE_CHANGE_LISTENER);
    }

    // Public Methods

    @Override
    public Set<String> getMutablePropertyNames() {
        return MUTABLE_PROPERTY_NAMES;
    }

    @Override
    public Object getMutableProperty(String name)
            throws MegawidgetPropertyException {
        if (name.equals(StatefulMegawidgetSpecifier.MEGAWIDGET_STATE_VALUES)) {
            Map<String, Object> map = Maps.newHashMap();
            for (String identifier : ((StatefulMegawidgetSpecifier) getSpecifier())
                    .getStateIdentifiers()) {
                try {
                    map.put(identifier, getState(identifier));
                } catch (MegawidgetStateException e) {
                    throw new MegawidgetPropertyException(getSpecifier()
                            .getIdentifier(), name, getSpecifier().getType(),
                            null, "querying valid state identifier \""
                                    + identifier + "\" caused internal error",
                            e);
                }
            }
            return map;
        }
        return super.getMutableProperty(name);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setMutableProperty(String name, Object value)
            throws MegawidgetPropertyException {
        if (name.equals(StatefulMegawidgetSpecifier.MEGAWIDGET_STATE_VALUES)) {

            // Ensure that the value is a map of state identifiers to
            // their values.
            Map<String, Object> map = null;
            try {
                map = (HashMap<String, Object>) value;
                if (map == null) {
                    throw new NullPointerException();
                }
            } catch (Exception e) {
                throw new MegawidgetPropertyException(getSpecifier()
                        .getIdentifier(), name, getSpecifier().getType(),
                        value, "bad map of values", e);
            }

            // Set the states to match the values given.
            setStates(map);
        } else {
            super.setMutableProperty(name, value);
        }
    }

    @Override
    public final Object getState(String identifier)
            throws MegawidgetStateException {

        // Ensure that the state identifier is valid.
        ((StatefulMegawidgetSpecifier) getSpecifier())
                .ensureStateIdentifierIsValid(identifier);

        // Get the state for this identifier.
        return doGetState(identifier);
    }

    @Override
    public final void setState(String identifier, Object state)
            throws MegawidgetStateException {

        // Ensure that the state is not being set al-
        // ready before actually setting the state.
        if (isSettingState) {
            return;
        }

        // Ensure that the state identifier is valid.
        ((StatefulMegawidgetSpecifier) getSpecifier())
                .ensureStateIdentifierIsValid(identifier);

        // Compare with the old state; if they are the
        // same, do nothing more.
        Object oldState = doGetState(identifier);
        if ((oldState == state)
                || ((oldState != null) && (state != null) && state
                        .equals(oldState))) {
            return;
        }

        // Set the state, ensuring that the flag that
        // indicates state is being set is high for
        // the duration of the set.
        isSettingState = true;
        try {
            doSetState(identifier, state);
        } catch (MegawidgetStateException e) {
            isSettingState = false;
            throw e;
        }
        isSettingState = false;
    }

    @Override
    public final String getStateDescription(String identifier, Object state)
            throws MegawidgetStateException {

        // Ensure that the state identifier is valid.
        ((StatefulMegawidgetSpecifier) getSpecifier())
                .ensureStateIdentifierIsValid(identifier);

        // Get the description of this state for this
        // identifier.
        return doGetStateDescription(identifier, state);
    }

    // Protected Methods

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
    protected abstract Object doGetState(String identifier);

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
     *             IStateful</code> implementation.
     */
    protected abstract void doSetState(String identifier, Object state)
            throws MegawidgetStateException;

    /**
     * Get a shortened description of the specified state for the specified
     * identifier. This method is called by
     * <code>getStateDescription() only after
     * the latter has ensured that the supplied state
     * identifier is valid.
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
     *             <code>IStateful </code> implementation.
     */
    protected abstract String doGetStateDescription(String identifier,
            Object state) throws MegawidgetStateException;

    /**
     * Set the states to the values in the specified map.
     * 
     * @param states
     *            Map containing keys drawn from the set of all valid state
     *            identifiers, with associated values being the new values for
     *            the states. Any state with a identifier-value pair found
     *            within this map is set to the given value; all states for
     *            which no identifier-value pairs exist remain as they were
     *            before.
     * @throws MegawidgetPropertyException
     *             If at least one identifier specifies a nonexistent state, or
     *             if at least one value is invalid.
     */
    protected void setStates(Map<String, Object> states)
            throws MegawidgetPropertyException {

        // Iterate through the pairs, setting each value as the state
        // for the corresponding identifier.
        for (String identifier : states.keySet()) {
            try {
                setState(identifier, states.get(identifier));
            } catch (MegawidgetStateException e) {
                throw new MegawidgetPropertyException(getSpecifier()
                        .getIdentifier(),
                        StatefulMegawidgetSpecifier.MEGAWIDGET_STATE_VALUES,
                        getSpecifier().getType(), states, "bad map of values",
                        e);
            }
        }
    }

    /**
     * Get an integer from the specified object as a value for the specified
     * state identifier. The object must be either <code>null</code> (only
     * allowed if <code>defValue</code> is not <code>null</code>), or an object
     * of type <code>Number</code>. This method is used to ensure that any value
     * specified as a number, but within the bounds of a standard integer, is
     * properly handled.
     * 
     * @param object
     *            Object holding the integer value.
     * @param identifier
     *            State identifier for which this object could be state.
     * @param defValue
     *            If present, this is the default value to be returned if <code>
     *            object</code> is <code>null</code>; if this parameter is
     *            <code>null</code>, then finding no value for <code>object
     *            </code> causes an exception.
     * @return Integer value.
     * @throws MegawidgetStateException
     *             If the state value is invalid.
     */
    protected final int getStateIntegerValueFromObject(Object object,
            String identifier, Integer defValue)
            throws MegawidgetStateException {
        try {
            return getSpecifier().getIntegerValueFromObject(object, defValue);
        } catch (MegawidgetException e) {
            throw new MegawidgetStateException(identifier, e.getType(),
                    e.getBadValue(), e.getMessage(), e.getCause());
        }
    }

    /**
     * Get an integer object from the specified object as a value for the
     * specified state identifier. The object must be either <code>null
     * </code> (only allowed if <code>defValue</code> is not <code>
     * null</code>), or an object of type <code>Number</code>. This method is
     * used to ensure that any value specified as a number, but within the
     * bounds of a standard integer, is properly handled. If the object is a
     * <code>Integer</code>, it is simply cast to this type and returned.
     * 
     * @param object
     *            Object holding the integer value.
     * @param identifier
     *            State identifier for which this object could be state.
     * @param defValue
     *            If present, this is the default value to be returned if <code>
     *            object</code> is <code>null</code>; if this parameter is
     *            <code>null</code>, then finding no value for <code>object
     *            </code> causes an exception.
     * @return Integer object.
     * @throws MegawidgetStateException
     *             If the state value is invalid.
     */
    protected final Integer getStateIntegerObjectFromObject(Object object,
            String identifier, Integer defValue)
            throws MegawidgetStateException {
        try {
            return getSpecifier().getIntegerObjectFromObject(object, defValue);
        } catch (MegawidgetException e) {
            throw new MegawidgetStateException(identifier, e.getType(),
                    e.getBadValue(), e.getMessage(), e.getCause());
        }
    }

    /**
     * Get a long integer from the specified object as a value for the specified
     * state identifier. The object must be either <code>null</code> (only
     * allowed if <code>defValue</code> is not <code>null</code>), or an object
     * of type <code>Number</code>. This method is used to ensure that any value
     * specified as a number, but within the bounds of a standard long integer,
     * is properly handled.
     * 
     * @param object
     *            Object holding the long integer value.
     * @param identifier
     *            State identifier for which this object could be state.
     * @param defValue
     *            If present, this is the default value to be returned if <code>
     *            object</code> is <code>null</code>; if this parameter is
     *            <code>null</code>, then finding no value for <code>object
     *            </code> causes an exception.
     * @return Long integer value.
     * @throws MegawidgetStatexception
     *             If the state value is invalid.
     */
    protected final long getStateLongValueFromObject(Object object,
            String identifier, Long defValue) throws MegawidgetStateException {
        try {
            return getSpecifier().getLongValueFromObject(object, defValue);
        } catch (MegawidgetException e) {
            throw new MegawidgetStateException(identifier, e.getType(),
                    e.getBadValue(), e.getMessage(), e.getCause());
        }
    }

    /**
     * Get a long integer object from the specified object as a value for the
     * specified state identifier. The object must be either <code>null</code>
     * (only allowed if <code>defValue</code> is not <code>null</code>), or an
     * object of type <code>Number</code>. This method is used to ensure that
     * any value specified as a number, but within the bounds of a standard long
     * integer, is properly handled. If the object is a <code>Long</code>, it is
     * simply cast to this type and returned.
     * 
     * @param object
     *            Object holding the long integer value.
     * @param identifier
     *            State identifier for which this object could be state.
     * @param defValue
     *            If present, this is the default value to be returned if <code>
     *            object</code> is <code>null</code>; if this parameter is
     *            <code>null</code>, then finding no value for <code>object
     *            </code> causes an exception.
     * @return Long integer object.
     * @throws MegawidgetStateException
     *             If the state value is invalid.
     */
    protected final Long getStateLongObjectFromObject(Object object,
            String identifier, Long defValue) throws MegawidgetStateException {
        try {
            return getSpecifier().getLongObjectFromObject(object, defValue);
        } catch (MegawidgetException e) {
            throw new MegawidgetStateException(identifier, e.getType(),
                    e.getBadValue(), e.getMessage(), e.getCause());
        }
    }

    /**
     * Get a boolean from the specified object as a value for the specified
     * state identifier. The object must be either <code>null</code> (only
     * allowed if <code>defValue</code> is not <code>null</code>), or an object
     * of type <code>Boolean</code>, <code>Integer</code> or <code>
     * Long</code>. This method is used to ensure that any value specified as a
     * boolean, or as a long or integer of either 0 or 1, is properly handled.
     * 
     * @param object
     *            Object holding the boolean value.
     * @param identifier
     *            State identifier for which this object could be state.
     * @param defValue
     *            If present, this is the default value to be returned if <code>
     *            object</code> is <code>null</code>; if this parameter is
     *            <code>null</code>, then finding no value for <code>object
     *            </code> causes an exception.
     * @return Boolean value.
     * @throws MegawidgetStateException
     *             If the state value is invalid.
     */
    protected final boolean getStateBooleanValueFromObject(Object object,
            String identifier, Boolean defValue)
            throws MegawidgetStateException {
        try {
            return getSpecifier().getBooleanValueFromObject(object, defValue);
        } catch (MegawidgetException e) {
            throw new MegawidgetStateException(identifier, e.getType(),
                    e.getBadValue(), e.getMessage(), e.getCause());
        }
    }

    /**
     * Get a boolean object from the specified object as a value for the
     * specified state identifier. The object must be either <code>null</code>
     * (only allowed if <code>defValue</code> is not <code>null</code>), or an
     * object of type <code>Boolean</code>, <code>Integer</code> or <code>
     * Long</code>. This method is used to ensure that any value specified as a
     * boolean, or as a long or integer of either 0 or 1, is properly handled.
     * If the object is a <code>Boolean</code>, it is simply cast to this type
     * and returned.
     * 
     * @param object
     *            Object holding the boolean value.
     * @param identifier
     *            State identifier for which this object could be state.
     * @param defValue
     *            If present, this is the default value to be returned if <code>
     *            object</code> is <code>null</code>; if this parameter is
     *            <code>null</code>, then finding no value for <code>object
     *            </code> causes an exception.
     * @return Boolean object.
     * @throws MegawidgetStateException
     *             If the state value is invalid.
     */
    protected final Boolean getStateBooleanObjectFromObject(Object object,
            String identifier, Boolean defValue)
            throws MegawidgetStateException {
        try {
            return getSpecifier().getBooleanObjectFromObject(object, defValue);
        } catch (MegawidgetException e) {
            throw new MegawidgetStateException(identifier, e.getType(),
                    e.getBadValue(), e.getMessage(), e.getCause());
        }
    }

    /**
     * Get a dynamically typed object from the specified object as a value for
     * the specified state identifier. The object must be either <code>null
     * </code> (only allowed if <code>defValue</code> is not <code>null</code>),
     * or an object of dynamic type <code>T</code>.
     * 
     * @param object
     *            Object to be cast or converted.
     * @param identifier
     *            State identifier for which this object could be state.
     * @param requiredClass
     *            Class to which this object must be cast or converted.
     * @param defValue
     *            If present, this is the default value to be returned if <code>
     *            object</code> is <code>null</code>; if this parameter is
     *            <code>null</code>, then finding no value for <code>object
     *            </code> causes an exception.
     * @return Object of the specified dynamic type.
     * @throws MegawidgetStateException
     *             If the state value is invalid.
     */
    protected final <T> T getStateDynamicallyTypedObjectFromObject(
            Object object, String identifier, Class<T> requiredClass, T defValue)
            throws MegawidgetStateException {
        try {
            return getSpecifier().getDynamicallyTypedObjectFromObject(object,
                    requiredClass, defValue);
        } catch (MegawidgetException e) {
            throw new MegawidgetStateException(identifier, e.getType(),
                    e.getBadValue(), e.getMessage(), e.getCause());
        }
    }

    /**
     * Notify the state change listener of a state change. This method should be
     * called by a subclass whenever the latter experiences a state change. It
     * ensures that the state change is not the result of the state being
     * programmatically set before going ahead with the notification.
     * 
     * @param identifier
     *            Identifier for which state has been changed.
     * @param state
     *            New state.
     */
    protected final void notifyListener(String identifier, Object state) {
        if (isSettingState == false) {
            isSettingState = true;
            stateChangeListener.megawidgetStateChanged(this, identifier, state);
            isSettingState = false;
        }
    }
}
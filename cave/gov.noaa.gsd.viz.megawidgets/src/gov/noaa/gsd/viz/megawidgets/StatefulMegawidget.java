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
 * Stateful megawidget created by a megawidget specifier.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Chris.Golden      Initial induction into repo
 * 
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see StatefulMegawidgetSpecifier
 */
public abstract class StatefulMegawidget extends NotifierMegawidget implements
        IStateful {

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
    protected StatefulMegawidget(MegawidgetSpecifier specifier,
            Map<String, Object> paramMap) {
        super(specifier, paramMap);
        stateChangeListener = (IStateChangeListener) paramMap
                .get(STATE_CHANGE_LISTENER);
    }

    // Public Methods

    /**
     * Get the current state for the specified identifier.
     * 
     * @param identifier
     *            Identifier for which state is desired.
     * @return Object making up the current state for that identifier.
     * @throws MegawidgetStateException
     *             If the supplied state identifier is not valid.
     */
    @Override
    public final Object getState(String identifier)
            throws MegawidgetStateException {

        // Ensure that the state identifier is valid.
        ((StatefulMegawidgetSpecifier) getSpecifier())
                .ensureStateIdentifierIsValid(identifier);

        // Get the state for this identifier.
        return doGetState(identifier);
    }

    /**
     * Set the current state for the specified identifier.
     * 
     * @param identifier
     *            Identifier for which state is to be set.
     * @param state
     *            Object making up the state to be used for this identifier, or
     *            <code>null</code> if its state should be reset.
     * @throws MegawidgetStateException
     *             If new state is not of a valid type for this
     *             <code>StatefulWidget</code> implementation, or if the
     *             supplied state identifier is not valid.
     */
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

    /**
     * Get a shortened description of the specified state for the specified
     * identifier.
     * 
     * @param identifier
     *            Identifier to which the state would be assigned.
     * @param state
     *            State for which to generate a shortened description.
     * @return Description of the specified state.
     * @throws MegawidgetStateException
     *             If the specified state is not of a valid type for this
     *             <code>StatefulWidget</code> implementation, or if the
     *             supplied state identifier is not valid.
     */
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
     *            valid for this widget.
     * @return Object making up the current state for the specified identifier.
     */
    protected abstract Object doGetState(String identifier);

    /**
     * Set the current state for the specified identifier. This method is called
     * by <code>setState()</code> only after the latter has ensured that the
     * supplied state identifier is valid, and has set a flag that indicates
     * that this setting of the state will not trigger the widget to notify its
     * listener of an invocation.
     * 
     * @param identifier
     *            Identifier for which state is to be set. Implementations may
     *            assume that the state identifier supplied by this parameter is
     *            valid for this widget.
     * @param state
     *            Object making up the state to be used for this identifier, or
     *            <code>null</code> if this state should be reset.
     * @throws MegawidgetStateException
     *             If new state is not of a valid type for this <code>
     *             StatefulWidget</code> implementation.
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
     *            by this parameter is valid for this widget.
     * @param state
     *            State for which to generate a shortened description.
     * @return Description of the specified state.
     * @throws MegawidgetStateException
     *             If the specified state is not of a valid type for this
     *             <code>StatefulWidget </code> implementation.
     */
    protected abstract String doGetStateDescription(String identifier,
            Object state) throws MegawidgetStateException;

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
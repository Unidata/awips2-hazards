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

import gov.noaa.gsd.viz.megawidgets.validators.BoundedMultiNumberValidator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.widgets.Composite;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;

/**
 * Range megawidget, providing a megawidget that allows the selection of lower
 * and upper bounds. The generic parameter <code>T</code> provides the type of
 * the numbers being manipulated.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Aug 06, 2015   4123     Chris.Golden Initial creation.
 * Oct 01, 2015  11739     Robert.Blum  Fixed notification issue so that the
 *                                      correct attribute in the IHazardEvent
 *                                      could be updated.
 * Jun 08, 2016  14002     Chris.Golden Fixed bug that caused premature
 *                                      validation of range spinner values
 *                                      when the user was typing into them
 *                                      and sendEveryChange was false.  Also
 *                                      added code to flip the two values if
 *                                      during validation the lower one is
 *                                      found to be greater than the upper
 *                                      one. Finally, fixed bug that caused
 *                                      a single state change to potentially
 *                                      result in the wrong notification.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see RangeSpecifier
 */
public abstract class RangeMegawidget<T extends Number & Comparable<T>> extends
        ExplicitCommitStatefulMegawidget implements IControl {

    // Protected Static Constants

    /**
     * Set of all mutable property names for instances of this class.
     */
    protected static final Set<String> MUTABLE_PROPERTY_NAMES;
    static {
        Set<String> names = new HashSet<>(
                StatefulMegawidget.MUTABLE_PROPERTY_NAMES);
        names.add(IControlSpecifier.MEGAWIDGET_EDITABLE);
        names.add(RangeSpecifier.MINIMUM_ALLOWABLE_VALUE);
        names.add(RangeSpecifier.MAXIMUM_ALLOWABLE_VALUE);
        names.add(RangeSpecifier.PAGE_INCREMENT_DELTA);
        MUTABLE_PROPERTY_NAMES = ImmutableSet.copyOf(names);
    };

    // Private Static Constants

    /**
     * Set of mutable properties to be ignored within
     * {@link #setMutableProperties(Map)} when handling the general cases,
     * because they have been dealt with already by that point in the method
     * body.
     */
    private static final Set<String> IGNORE_FOR_SET_MUTABLE_PROPERTIES = Sets
            .newHashSet(RangeSpecifier.MINIMUM_ALLOWABLE_VALUE,
                    RangeSpecifier.MINIMUM_ALLOWABLE_VALUE);

    // Protected Classes

    /**
     * Holder for the spinner and scale helper.
     */
    private class Holder implements ISpinnerAndScaleComponentHolder<T> {

        // Public Methods

        @Override
        public T getMinimumValue(String identifier) {
            return getAdjustedMinimumValue(identifier);
        }

        @Override
        public T getMaximumValue(String identifier) {
            return getAdjustedMaximumValue(identifier);
        }

        @Override
        public T getPageIncrementDelta() {
            return RangeMegawidget.this.getIncrementDelta();
        }

        @Override
        public int getPrecision() {
            return RangeMegawidget.this.getPrecision();
        }

        @Override
        public T getState(String identifier) {
            return statesForIds.get(identifier);
        }

        @SuppressWarnings("unchecked")
        @Override
        public List<String> setState(String identifier, T value) {

            /*
             * Do nothing unless the specified value is new.
             */
            value = getRangeCorrectedValue(identifier, value);
            if (value.equals(statesForIds.get(identifier)) == false) {
                statesForIds.put(identifier, value);
                RangeSpecifier<T> specifier = (RangeSpecifier<T>) getSpecifier();
                List<String> stateIdentifiers = specifier.getStateIdentifiers();
                List<String> changedStates = Lists.newArrayList(identifier);
                T minimumInterval = specifier.getMinimumInterval();

                /*
                 * If the new value is the lower bound, ensure the upper bound
                 * is correct, adjusting if necessary; otherwise, do this with
                 * the lower bound.
                 */
                if (identifier.equals(stateIdentifiers.get(0))) {
                    String upperIdentifier = stateIdentifiers.get(1);
                    if (value
                            .compareTo(stateValidator.subtract(
                                    statesForIds.get(upperIdentifier),
                                    minimumInterval)) > 0) {
                        statesForIds.put(upperIdentifier,
                                stateValidator.add(value, minimumInterval));
                        changedStates.add(upperIdentifier);
                    }
                } else {
                    String lowerIdentifier = stateIdentifiers.get(0);
                    if (value
                            .compareTo(stateValidator.add(
                                    statesForIds.get(lowerIdentifier),
                                    minimumInterval)) < 0) {
                        statesForIds
                                .put(lowerIdentifier, stateValidator.subtract(
                                        value, minimumInterval));
                        changedStates.add(lowerIdentifier);
                    }
                }
                return changedStates;
            } else {
                return null;
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        public List<String> setStates(Map<String, T> valuesForIdentifiers) {

            /*
             * Adjust each of the provided values to fit within their ranges.
             */
            RangeSpecifier<T> specifier = (RangeSpecifier<T>) getSpecifier();
            List<String> stateIdentifiers = specifier.getStateIdentifiers();
            Map<String, T> adjustedValuesForIdentifiers = new HashMap<>(
                    valuesForIdentifiers.size(), 1.0f);
            List<T> newValues = new ArrayList<>(2);
            for (String identifier : stateIdentifiers) {
                T adjustedValue = getRangeCorrectedValue(identifier,
                        valuesForIdentifiers.get(identifier));
                newValues.add(adjustedValue);
                adjustedValuesForIdentifiers.put(identifier, adjustedValue);
            }

            /*
             * Ensure that the upper value is at least the minimum distance from
             * the lower value.
             */
            T minimumInterval = specifier.getMinimumInterval();
            if (newValues.get(0).compareTo(
                    stateValidator.subtract(newValues.get(1), minimumInterval)) > 0) {
                adjustedValuesForIdentifiers.put(stateIdentifiers.get(1),
                        stateValidator.add(newValues.get(0), minimumInterval));
            }

            /*
             * Iterate through the adjusted values, associating each with its
             * identifier if the old associated value is different, and making a
             * record of which identifiers are experiencing a state change.
             */
            List<String> changedStates = null;
            for (Map.Entry<String, T> entry : adjustedValuesForIdentifiers
                    .entrySet()) {
                if (entry.getValue().equals(statesForIds.get(entry.getKey())) == false) {
                    statesForIds.put(entry.getKey(), entry.getValue());
                    if (changedStates == null) {
                        changedStates = Lists.newArrayList(entry.getKey());
                    } else {
                        changedStates.add(entry.getKey());
                    }
                }
            }
            return changedStates;
        }

        @Override
        public void notifyListener(List<String> identifiersOfChangedStates) {
            if (identifiersOfChangedStates.size() == 1) {
                String changedStateIdentifier = identifiersOfChangedStates
                        .get(0);
                RangeMegawidget.this.notifyListener(changedStateIdentifier,
                        statesForIds.get(changedStateIdentifier));
            } else {
                RangeMegawidget.this
                        .notifyListener(new HashMap<>(statesForIds));

            }
        }
    }

    // Protected Variables

    /**
     * Get the specified value range-corrected by ensuring it is within its
     * boundaries.
     * 
     * @param identifier
     *            State identifier with which the value is associated.
     * @param value
     *            Value to be corrected.
     * @return Corrected value.
     */
    private T getRangeCorrectedValue(String identifier, T value) {
        if (value.compareTo(boundariesForIds.get(identifier).lowerEndpoint()) < 0) {
            value = boundariesForIds.get(identifier).lowerEndpoint();
        } else if (value.compareTo(boundariesForIds.get(identifier)
                .upperEndpoint()) > 0) {
            value = boundariesForIds.get(identifier).upperEndpoint();
        }
        return value;
    }

    /**
     * Map of state identifiers to their current values.
     */
    protected final Map<String, T> statesForIds;

    /**
     * Map of state identifiers to their current value boundaries, taking into
     * account each state identifier's allowable range and the minimum interval
     * between each.
     */
    protected final Map<String, Range<T>> boundariesForIds;

    // Private Variables

    /**
     * State validator.
     */
    private final BoundedMultiNumberValidator<T> stateValidator;

    /**
     * Control component helper.
     */
    private final ControlComponentHelper controlHelper;

    /**
     * Spinner and scale component helper.
     */
    private final SpinnerAndScaleComponentHelper<T> spinnerAndScaleHelper;

    // Protected Constructors

    /**
     * Construct a standard instance.
     * 
     * @param specifier
     *            Specifier.
     * @param parent
     *            Parent of the megawidget.
     * @param spinnerAndScaleHelper
     *            Spinner and scale component widgets helper.
     * @param paramMap
     *            Hash table mapping megawidget creation time parameter
     *            identifiers to values.
     */
    @SuppressWarnings("unchecked")
    protected RangeMegawidget(RangeSpecifier<T> specifier, Composite parent,
            SpinnerAndScaleComponentHelper<T> spinnerAndScaleHelper,
            Map<String, Object> paramMap) {
        super(specifier, paramMap);
        stateValidator = specifier.getStateValidator();
        statesForIds = new HashMap<>();
        List<String> stateIdentifiers = specifier.getStateIdentifiers();
        for (String identifier : stateIdentifiers) {
            statesForIds.put(identifier,
                    (T) specifier.getStartingState(identifier));
        }
        boundariesForIds = new HashMap<>(statesForIds.size(), 1.0f);
        updateStateBoundaries();
        controlHelper = new ControlComponentHelper(specifier);
        this.spinnerAndScaleHelper = spinnerAndScaleHelper;
        spinnerAndScaleHelper.setHolder(new Holder());

        /*
         * Build the component widgets.
         */
        spinnerAndScaleHelper.buildParentPanelAndLabel(parent, specifier);
        String lowerIdentifier = stateIdentifiers.get(0);
        spinnerAndScaleHelper.buildSpinner(specifier,
                getAdjustedMinimumValue(lowerIdentifier),
                getAdjustedMaximumValue(lowerIdentifier), getIncrementDelta(),
                specifier.isShowScale(), lowerIdentifier);
        String upperIdentifier = stateIdentifiers.get(1);
        String betweenLabel = specifier.getBetweenBoundsLabel();
        if ((betweenLabel != null) && (betweenLabel.isEmpty() == false)) {
            spinnerAndScaleHelper.buildJoiningLabel(specifier,
                    specifier.getBetweenBoundsLabel(), upperIdentifier);
        }
        spinnerAndScaleHelper.buildSpinner(specifier,
                getAdjustedMinimumValue(upperIdentifier),
                getAdjustedMaximumValue(upperIdentifier), getIncrementDelta(),
                specifier.isShowScale(), upperIdentifier);
        spinnerAndScaleHelper
                .buildScale(
                        specifier,
                        (getAdjustedMinimumValue(lowerIdentifier).compareTo(
                                getAdjustedMinimumValue(upperIdentifier)) > 0 ? getAdjustedMinimumValue(upperIdentifier)
                                : getAdjustedMinimumValue(lowerIdentifier)),
                        (getAdjustedMaximumValue(lowerIdentifier).compareTo(
                                getAdjustedMaximumValue(upperIdentifier)) > 0 ? getAdjustedMaximumValue(lowerIdentifier)
                                : getAdjustedMaximumValue(upperIdentifier)),
                        boundariesForIds, specifier.getMinimumInterval(),
                        getIncrementDelta(), specifier.isShowScale());

        /*
         * Render the spinner uneditable if necessary.
         */
        if (isEditable() == false) {
            setEditable(false);
        }

        /*
         * Synchronize user-facing widgets to the starting state.
         */
        synchronizeComponentWidgetsToState();
    }

    // Public Methods

    @Override
    public Set<String> getMutablePropertyNames() {
        return MUTABLE_PROPERTY_NAMES;
    }

    @Override
    public Object getMutableProperty(String name)
            throws MegawidgetPropertyException {
        if (name.equals(IControlSpecifier.MEGAWIDGET_EDITABLE)) {
            return isEditable();
        } else if (name.equals(RangeSpecifier.MINIMUM_ALLOWABLE_VALUE)) {
            return getMinimumValues();
        } else if (name.equals(RangeSpecifier.MAXIMUM_ALLOWABLE_VALUE)) {
            return getMaximumValues();
        } else if (name.equals(RangeSpecifier.PAGE_INCREMENT_DELTA)) {
            return getIncrementDelta();
        }
        return super.getMutableProperty(name);
    }

    @Override
    public void setMutableProperty(String name, Object value)
            throws MegawidgetPropertyException {
        if (name.equals(IControlSpecifier.MEGAWIDGET_EDITABLE)) {
            setEditable(ConversionUtilities.getPropertyBooleanValueFromObject(
                    getSpecifier().getIdentifier(), getSpecifier().getType(),
                    value, name, null));
        } else if (name.equals(RangeSpecifier.MINIMUM_ALLOWABLE_VALUE)) {
            doSetMinimumValues(value);
        } else if (name.equals(RangeSpecifier.MAXIMUM_ALLOWABLE_VALUE)) {
            doSetMaximumValues(value);
        } else if (name
                .equals(SpinnerSpecifier.MEGAWIDGET_PAGE_INCREMENT_DELTA)) {
            stateValidator.setIncrementDelta(value);
        } else {
            super.setMutableProperty(name, value);
        }
    }

    @Override
    public void setMutableProperties(Map<String, Object> properties)
            throws MegawidgetPropertyException {

        /*
         * Set the minimum and/or maximum allowable values first, so that any
         * state values that may have been specified as part of the mutable
         * properties to be changed are altered only after the boundaries are in
         * place.
         */
        Object minValueObj = properties
                .get(RangeSpecifier.MINIMUM_ALLOWABLE_VALUE);
        Object maxValueObj = properties
                .get(RangeSpecifier.MAXIMUM_ALLOWABLE_VALUE);
        if ((minValueObj != null) && (maxValueObj != null)) {
            doSetRanges(minValueObj, maxValueObj);
        } else if (minValueObj != null) {
            doSetMinimumValues(minValueObj);
        } else if (maxValueObj != null) {
            doSetMaximumValues(maxValueObj);
        }

        /*
         * Set any other mutable properties specified.
         */
        for (String name : properties.keySet()) {
            if (IGNORE_FOR_SET_MUTABLE_PROPERTIES.contains(name) == false) {
                setMutableProperty(name, properties.get(name));
            }
        }
    }

    @Override
    public final boolean isEditable() {
        return controlHelper.isEditable();
    }

    @Override
    public final void setEditable(boolean editable) {
        controlHelper.setEditable(editable);
        spinnerAndScaleHelper.setEditable(editable, controlHelper);
    }

    @Override
    public int getLeftDecorationWidth() {
        return (spinnerAndScaleHelper.getLabel() == null ? 0 : controlHelper
                .getWidestWidgetWidth(spinnerAndScaleHelper.getLabel()));
    }

    @Override
    public void setLeftDecorationWidth(int width) {
        /*
         * TODO RM 8413 - Turning this off since it does not handle megawidgets
         * in different columns correctly. It is cutting off widgets in the
         * right column. Will revisit this when more time is available.
         */
        // if (spinnerAndScaleHelper.getLabel() != null) {
        // controlHelper.setWidgetsWidth(width,
        // spinnerAndScaleHelper.getLabel());
        // }
    }

    @Override
    public final int getRightDecorationWidth() {
        return 0;
    }

    @Override
    public final void setRightDecorationWidth(int width) {

        /*
         * No action.
         */
    }

    /**
     * Get the minimum value for the specified state.
     * 
     * @param identifier
     *            State identifier.
     * @return Minimum value.
     */
    public final T getMinimumValue(String identifier) {
        return stateValidator.getMinimumValue(identifier);
    }

    /**
     * Get the maximum value for the specified state.
     * 
     * @param identifier
     *            State identifier.
     * @return Maximum value.
     */
    public final T getMaximumValue(String identifier) {
        return stateValidator.getMaximumValue(identifier);
    }

    /**
     * Get the minimum values for all states.
     * 
     * @return Map of state identifiers to their minimum values.
     */
    public final Map<String, T> getMinimumValues() {
        return stateValidator.getMinimumValues();
    }

    /**
     * Get the maximum values for all states.
     * 
     * @return Map of state identifiers to their maximum values.
     */
    public final Map<String, T> getMaximumValues() {
        return stateValidator.getMaximumValues();
    }

    /**
     * Get the increment delta.
     * 
     * @return Increment delta.
     */
    public final T getIncrementDelta() {
        return stateValidator.getIncrementDelta();
    }

    /**
     * Set the increment delta.
     * 
     * @param value
     *            New increment delta; must be a positive integer.
     * @throws MegawidgetPropertyException
     *             If the object is not a positive integer.
     */
    public final void setIncrementDelta(Object value)
            throws MegawidgetPropertyException {
        stateValidator.setIncrementDelta(value);
        spinnerAndScaleHelper.synchronizeComponentWidgetsToPageIncrementDelta();
    }

    // Protected Methods

    @SuppressWarnings("unchecked")
    protected final <V extends BoundedMultiNumberValidator<T>> V getStateValidator() {
        return (V) stateValidator;
    }

    @Override
    protected void doSynchronizeComponentWidgetsToState(String identifier) {
        spinnerAndScaleHelper.synchronizeComponentWidgetsToState();
    }

    @Override
    protected final void doSynchronizeComponentWidgetsToState() {
        spinnerAndScaleHelper.synchronizeComponentWidgetsToState();
    }

    @Override
    protected final void doSetEnabled(boolean enable) {
        spinnerAndScaleHelper.setEnabled(enable);
    }

    @Override
    protected Object doGetState(String identifier) {
        return statesForIds.get(identifier);
    }

    @Override
    protected String doGetStateDescription(String identifier, Object state) {
        return (state == null ? null : state.toString());
    }

    @Override
    protected final void doSetState(String identifier, Object state)
            throws MegawidgetStateException {

        /*
         * Create a copy of the state values, except with the specified
         * identifier's new potential value.
         */
        Map<String, Object> map = new HashMap<>();
        for (String thisIdentifier : statesForIds.keySet()) {
            map.put(thisIdentifier, (identifier.equals(thisIdentifier) ? state
                    : statesForIds.get(thisIdentifier)));
        }

        /*
         * Validate the new state.
         */
        Map<String, T> validMap;
        try {
            validMap = stateValidator.convertToStateValues(map);
        } catch (MegawidgetException e) {
            throw new MegawidgetStateException(e);
        }
        statesForIds.put(identifier, validMap.get(identifier));

        /*
         * Synchronize the user-facing widgets with the new state.
         */
        synchronizeComponentWidgetsToState();

        /*
         * Since the state change was programmatic, notify the helper of this.
         */
        spinnerAndScaleHelper.handleProgrammaticStateChange();
    }

    @Override
    protected void ensureStateIsValid(String identifier, Object state)
            throws MegawidgetStateException {
        try {
            stateValidator.convertToStateValue(identifier, state);
        } catch (MegawidgetException e) {
            throw new MegawidgetStateException(e);
        }
    }

    @Override
    protected void doCommitStateChanges(Map<String, Object> newStatesForIds)
            throws MegawidgetStateException {

        /*
         * Create a copy of the state values, except using the new values for
         * those state identifiers that are found in the new map.
         */
        Map<String, Object> map = new HashMap<>();
        for (String identifier : statesForIds.keySet()) {
            map.put(identifier,
                    (newStatesForIds.containsKey(identifier) ? newStatesForIds
                            .get(identifier) : statesForIds.get(identifier)));
        }

        /*
         * Validate the new values.
         */
        Map<String, T> validMap;
        try {
            validMap = stateValidator.convertToStateValues(map);
        } catch (MegawidgetException e) {
            throw new MegawidgetStateException(e);
        }
        statesForIds.clear();
        statesForIds.putAll(validMap);

        /*
         * Synchronize the user-facing widgets with the new state.
         */
        synchronizeComponentWidgetsToState();

        /*
         * Since the state change was programmatic, notify the helper of this.
         */
        spinnerAndScaleHelper.handleProgrammaticStateChange();
    }

    /**
     * Get the minimum value for the specified state, adjusted to work with
     * other states' boundaries.
     * 
     * @param identifier
     *            State identifier. return Minimum value, adjusted for other
     *            states' boundaries.
     */
    protected final T getAdjustedMinimumValue(String identifier) {
        return boundariesForIds.get(identifier).lowerEndpoint();
    }

    /**
     * Get the maximum value for the specified state, adjusted to work with
     * other states' boundaries.
     * 
     * @param identifier
     *            State identifier. return Maximum value, adjusted for other
     *            states' boundaries.
     */
    protected final T getAdjustedMaximumValue(String identifier) {
        return boundariesForIds.get(identifier).upperEndpoint();
    }

    /**
     * Get the precision for the spinner, that is, the number of decimal places
     * that should come after a decimal point.
     * 
     * @return Non-negative number indicating the precision; if <code>0</code>,
     *         no decimal point will be shown.
     */
    protected abstract int getPrecision();

    // Private Methods

    /**
     * Update the current state value boundaries.
     */
    @SuppressWarnings("unchecked")
    private void updateStateBoundaries() {

        /*
         * Iterate through the state identifiers, from lowest to highest,
         * determining the minimum for each, taking into account both the
         * allowable minimum and the interval between each such minimum and the
         * previous identifier's minimum.
         */
        List<String> stateIdentifiers = new ArrayList<>(
                ((RangeSpecifier<T>) getSpecifier()).getStateIdentifiers());
        T minimumInterval = stateValidator.getMinimumInterval();
        Map<String, T> minimums = new HashMap<>(boundariesForIds.size(), 1.0f);
        T lastMinimum = null;
        for (String identifier : stateIdentifiers) {
            T minimum = stateValidator.getMinimumValue(identifier);
            if (lastMinimum != null) {
                T minimumAccountingForPrevious = stateValidator.add(
                        lastMinimum, minimumInterval);
                if (minimumAccountingForPrevious.compareTo(minimum) > 0) {
                    minimum = minimumAccountingForPrevious;
                }
            }
            minimums.put(identifier, minimum);
            lastMinimum = minimum;
        }

        /*
         * Iterate through the state identifiers, from highest to lowest,
         * determining the maximum for each, taking into account both the
         * allowable maximum and the interval between each such maximum and the
         * next identifier's maximum.
         */
        Collections.reverse(stateIdentifiers);
        Map<String, T> maximums = new HashMap<>(boundariesForIds.size(), 1.0f);
        T lastMaximum = null;
        for (String identifier : stateIdentifiers) {
            T maximum = stateValidator.getMaximumValue(identifier);
            if (lastMaximum != null) {
                T maximumAccountingForPrevious = stateValidator.subtract(
                        lastMaximum, minimumInterval);
                if (maximumAccountingForPrevious.compareTo(maximum) < 0) {
                    maximum = maximumAccountingForPrevious;
                }
            }
            maximums.put(identifier, maximum);
            lastMaximum = maximum;
        }

        /*
         * Record the resulting ranges.
         */
        for (String identifier : stateIdentifiers) {
            boundariesForIds.put(
                    identifier,
                    Range.closed(minimums.get(identifier),
                            maximums.get(identifier)));
        }
    }

    /**
     * Perform the actual work of setting the minimum values.
     * 
     * @param values
     *            New minimum values.
     * @throws MegawidgetPropertyException
     *             If the values are not valid.
     */
    private void doSetMinimumValues(Object values)
            throws MegawidgetPropertyException {

        /*
         * Set the new minimum values, ensuring they are valid.
         */
        stateValidator.setMinimumValues(values);
        updateStateBoundaries();

        /*
         * Synchronize the widgets to the new boundaries.
         */
        spinnerAndScaleHelper.synchronizeComponentWidgetsToBounds();

        /*
         * For each state, if it is less than its new minimum, set it
         * appropriately and synchronize the widgets to the new state.
         */
        if (ensureStateValuesRespectMinimums()) {
            synchronizeComponentWidgetsToState();
        }
    }

    /**
     * Perform the actual work of setting the maximum values.
     * 
     * @param values
     *            New maximum values.
     * @throws MegawidgetPropertyException
     *             If the values are not valid.
     */
    private void doSetMaximumValues(Object values)
            throws MegawidgetPropertyException {

        /*
         * Set the new maximum values, ensuring they are valid.
         */
        stateValidator.setMaximumValues(values);
        updateStateBoundaries();

        /*
         * Synchronize the widgets to the new boundaries.
         */
        spinnerAndScaleHelper.synchronizeComponentWidgetsToBounds();

        /*
         * For each state, if it is greater than its new maximum, set it
         * appropriately and synchronize the widgets to the new state.
         */
        if (ensureStateValuesRespectMaximums()) {
            synchronizeComponentWidgetsToState();
        }
    }

    /**
     * Perform the actual work of setting the minimum and maximum values.
     * 
     * @param minimumValues
     *            Map of state identifiers to their new minimums; for each state
     *            identifier, the new minimum must be less than or equal to the
     *            corresponding entry in <code>maximumValues</code>.
     * @param maximumValues
     *            Map of state identifiers to their new maximums; for each state
     *            identifier, the new maximum must be greater than or equal to
     *            the corresponding entry in <code>minimumValues</code>.
     * @throws MegawidgetPropertyException
     *             If the new values are not valid.
     */
    private void doSetRanges(Object minimumValues, Object maximumValues)
            throws MegawidgetPropertyException {

        /*
         * Set the new range, ensuring it is valid.
         */
        stateValidator.setRanges(minimumValues, maximumValues);
        updateStateBoundaries();

        /*
         * Synchronize the widgets to the new boundaries.
         */
        spinnerAndScaleHelper.synchronizeComponentWidgetsToBounds();

        /*
         * For each state, if it falls outside its new boundaries, set it
         * appropriately and synchronize the widgets to the new state.
         */
        boolean stateChanged = ensureStateValuesRespectMinimums();
        stateChanged |= ensureStateValuesRespectMaximums();
        if (stateChanged) {
            synchronizeComponentWidgetsToState();
        }
    }

    /**
     * Ensure that the minimum allowable state values are being respected.
     * 
     * @return True if at least one state value had to be changed.
     */
    @SuppressWarnings("unchecked")
    private boolean ensureStateValuesRespectMinimums() {
        boolean stateChanged = false;
        List<String> stateIdentifiers = ((RangeSpecifier<T>) getSpecifier())
                .getStateIdentifiers();
        T lastState = null;
        for (int j = 0; j < stateIdentifiers.size(); j++) {
            String identifier = stateIdentifiers.get(j);
            T state = statesForIds.get(identifier);
            T minimumGivenLastState = (lastState == null ? state
                    : stateValidator.add(lastState,
                            stateValidator.getMinimumInterval()));
            if ((state == null)
                    || (state.compareTo(getAdjustedMinimumValue(identifier)) < 0)
                    || (state.compareTo(minimumGivenLastState) < 0)) {
                stateChanged = true;
                state = getAdjustedMinimumValue(identifier);
                if (state.compareTo(minimumGivenLastState) < 0) {
                    state = minimumGivenLastState;
                }
                statesForIds.put(identifier, state);
            }
            lastState = state;
        }
        return stateChanged;
    }

    /**
     * Ensure that the maximum allowable state values are being respected.
     * 
     * @return True if at least one state value had to be changed.
     */
    @SuppressWarnings("unchecked")
    private boolean ensureStateValuesRespectMaximums() {
        boolean stateChanged = false;
        List<String> stateIdentifiers = new ArrayList<>(
                ((RangeSpecifier<T>) getSpecifier()).getStateIdentifiers());
        Collections.reverse(stateIdentifiers);
        T lastState = null;
        for (int j = 0; j < stateIdentifiers.size(); j++) {
            String identifier = stateIdentifiers.get(j);
            T state = statesForIds.get(identifier);
            T maximumGivenLastState = (lastState == null ? state
                    : stateValidator.subtract(lastState,
                            stateValidator.getMinimumInterval()));
            if ((state == null)
                    || (state.compareTo(getAdjustedMaximumValue(identifier)) > 0)
                    || (state.compareTo(maximumGivenLastState) > 0)) {
                stateChanged = true;
                state = getAdjustedMaximumValue(identifier);
                if (state.compareTo(maximumGivenLastState) > 0) {
                    state = maximumGivenLastState;
                }
                statesForIds.put(identifier, state);
            }
            lastState = state;
        }
        return stateChanged;
    }
}
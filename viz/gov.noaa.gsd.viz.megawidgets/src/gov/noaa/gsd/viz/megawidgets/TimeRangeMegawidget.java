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

import gov.noaa.gsd.viz.megawidgets.validators.BoundedTimeDeltaStringChoiceValidator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.widgets.Composite;

import com.google.common.collect.ImmutableSet;

/**
 * Time range megawidget, providing the user the ability to select a time range
 * using one absolute date-time selector (for the lower boundary) and a duration
 * selector (for the upper boundary).
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Jul 01, 2014   3512     Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see TimeRangeSpecifier
 */
public class TimeRangeMegawidget extends MultiTimeMegawidget {

    // Protected Static Constants

    /**
     * Set of all mutable property names for instances of this class.
     */
    protected static final Set<String> MUTABLE_PROPERTY_NAMES;
    static {
        Set<String> names = new HashSet<>(
                MultiTimeMegawidget.MUTABLE_PROPERTY_NAMES);
        names.add(TimeRangeSpecifier.MEGAWIDGET_DURATION_CHOICES);
        MUTABLE_PROPERTY_NAMES = ImmutableSet.copyOf(names);
    };

    // Private Variables

    /**
     * Manager of the lower boundary's date-time component.
     */
    private final IDateTimeComponentHolder dateTimeManager = new IDateTimeComponentHolder() {
        @Override
        public long getCurrentTime() {
            return getCurrentTimeProvider().getCurrentTime();
        }

        @Override
        public long renderValueChangeAcceptable(String identifier, long value) {

            /*
             * Make the value valid for the scale widget.
             */
            value = convertToValueAcceptableToScale(value);

            /*
             * If the scale widget's intervals are locked, that means that the
             * interval between the lower and upper values is one of the
             * intervals available as a choice, so any movement of the lower
             * boundary will also move the upper boundary; in this case, check
             * to make sure that the upper boundary does not move beyond the
             * allowable range. Otherwise, make sure that the lower boundary is
             * not being moved beyond the upper boundary.
             */
            if (getScale().isConstrainedThumbIntervalLocked()) {
                if (isFirstValueKeepingAllValuesWithinRange(value)) {
                    return value;
                } else {
                    return -1L;
                }
            } else {
                if (isValueBetweenNeighboringValues(0, value)) {
                    return value;
                } else {
                    return -1L;
                }
            }
        }

        @Override
        public void valueChanged(String identifier, long value,
                boolean rapidChange) {

            /*
             * Set the value if the new value is different from the old.
             */
            if (setValueIfChanged(identifier, 0, value)) {

                /*
                 * If the scale widget's intervals are unlocked, update the
                 * upper boundary time component, since it's possible that the
                 * delta between the upper boundary and the new lower boundary
                 * is now one of the intervals available as a choice.
                 */
                boolean locked = getScale().isConstrainedThumbIntervalLocked();
                if (locked == false) {
                    String otherIdentifier = ((IStatefulSpecifier) getSpecifier())
                            .getStateIdentifiers().get(1);
                    setTimeDeltaComponentValues(timeDelta,
                            getStateInternally(otherIdentifier));
                    synchronizeIntervalLockingWithState();
                }

                /*
                 * Notify listeners if appropriate.
                 */
                if ((isOnlySendEndStateChanges() == false)
                        || (rapidChange == false)) {
                    if (locked) {
                        notifyListener(getStates());
                    } else {
                        notifyListener(identifier, value);
                    }
                }
            }
        }
    };

    /**
     * Manager of the upper boundary's time delta component.
     */
    private final ITimeDeltaComboComponentHolder timeDeltaManager = new ITimeDeltaComboComponentHolder() {

        @Override
        public void valueChanged(String identifier, long value) {

            /*
             * Set the new state of the upper boundary to the sum of the lower
             * boundary and the new delta.
             */
            if (setValueIfChanged(identifier, 1,
                    getStateInternally(((IStatefulSpecifier) getSpecifier())
                            .getStateIdentifiers().get(0)) + value)) {
                notifyListener(
                        identifier,
                        getStateInternally(((IStatefulSpecifier) getSpecifier())
                                .getStateIdentifiers().get(1)));
            }
        }
    };

    /**
     * Lower boundary time component.
     */
    private DateTimeComponent dateTime;

    /**
     * Upper boundary time component.
     */
    private TimeDeltaComboComponent timeDelta;

    /**
     * Bounded time delta string choices validator for use with the duration
     * choices.
     */
    private final BoundedTimeDeltaStringChoiceValidator durationChoicesValidator;

    // Protected Constructors

    /**
     * Construct a standard instance.
     * 
     * @param specifier
     *            Specifier.
     * @param parent
     *            Parent of the megawidget.
     * @param paramMap
     *            Hash table mapping megawidget creation time parameter
     *            identifiers to values.
     * @throws MegawidgetException
     *             If an error occurs while creating or initializing any
     *             megawidgets acting as detail fields for the various states.
     */
    protected TimeRangeMegawidget(TimeRangeSpecifier specifier,
            Composite parent, Map<String, Object> paramMap)
            throws MegawidgetException {
        super(specifier, paramMap);
        createWidgetComponents(specifier, parent, paramMap, 0L);
        durationChoicesValidator = specifier.getDurationChoicesValidator()
                .copyOf();
        synchronizeIntervalLockingWithState();
    }

    // Public Methods

    @Override
    public Object getMutableProperty(String name)
            throws MegawidgetPropertyException {
        if (name.equals(TimeRangeSpecifier.MEGAWIDGET_DURATION_CHOICES)) {
            return timeDelta.getChoiceStrings();
        }
        return super.getMutableProperty(name);
    }

    @Override
    public void setMutableProperty(String name, Object value)
            throws MegawidgetPropertyException {
        if (name.equals(TimeRangeSpecifier.MEGAWIDGET_DURATION_CHOICES)) {
            doSetDurationChoices(value);
        } else {
            super.setMutableProperty(name, value);
        }
    }

    /**
     * Set the duration choices to those specified.
     * 
     * @param value
     *            List of new duration choices.
     * @throws MegawidgetPropertyException
     *             If the choices are invalid.
     */
    public void setDurationChoices(Object value)
            throws MegawidgetPropertyException {
        doSetDurationChoices(value);
    }

    // Protected Methods

    /**
     * Set the duration choices to those specified.
     * 
     * @param value
     *            List of new choices.
     * @throws MegawidgetPropertyException
     *             If the choices are invalid.
     */
    protected void doSetDurationChoices(Object value)
            throws MegawidgetPropertyException {

        /*
         * Set the available choices to those specified.
         */
        durationChoicesValidator.setAvailableChoices(value);

        /*
         * Set the time delta component's choices to be these choices.
         */
        List<?> durationChoiceObjs = durationChoicesValidator
                .getAvailableChoices();
        List<String> durationChoices = new ArrayList<>(
                durationChoiceObjs.size());
        for (Object durationChoiceObj : durationChoiceObjs) {
            durationChoices.add((String) durationChoiceObj);
        }
        timeDelta.setChoices(durationChoices,
                durationChoicesValidator.getTimeDeltasForAvailableChoices());

        /*
         * Update the interval locking mode.
         */
        synchronizeIntervalLockingWithState();
    }

    @Override
    protected void doSetState(String identifier, Object state)
            throws MegawidgetStateException {

        /*
         * Let the superclass do the usual work first.
         */
        super.doSetState(identifier, state);

        /*
         * Make sure the upper boundary time component is synchronized with the
         * current state, since the change to either boundary may result in the
         * time delta becoming one of the interval choices when it was not
         * previously, or vice versa.
         */
        List<String> stateIdentifiers = ((IStatefulSpecifier) getSpecifier())
                .getStateIdentifiers();
        synchronizeTimeComponentToState(stateIdentifiers.get(1));

        /*
         * Ensure that the scale widget's interval is locked or unlocked as
         * appropriate to the latest state of the time delta.
         */
        synchronizeIntervalLockingWithState();
    }

    @Override
    protected ITimeComponent createTimeComponent(int stateIndex,
            Composite parent, String text, int verticalIndent) {

        /*
         * Create a date-time component if this is for the first state
         * identifier, or a time delta component if for the second state
         * identifier.
         */
        String identifier = ((StatefulMegawidgetSpecifier) getSpecifier())
                .getStateIdentifiers().get(stateIndex);
        if (stateIndex == 0) {
            dateTime = new DateTimeComponent(identifier, parent, text,
                    (IControlSpecifier) getSpecifier(),
                    getStateInternally(identifier), false, verticalIndent,
                    isOnlySendEndStateChanges(), dateTimeManager);
            setDateTimeComponentValues(dateTime, getStateInternally(identifier));
            return dateTime;
        } else {
            TimeRangeSpecifier specifier = getSpecifier();
            String firstIdentifier = specifier.getStateIdentifiers().get(0);
            timeDelta = new TimeDeltaComboComponent(identifier, parent, text,
                    specifier, specifier.getDurationChoices(),
                    specifier.getTimeDeltasForDurationChoices(),
                    getStateInternally(identifier)
                            - getStateInternally(firstIdentifier),
                    verticalIndent, timeDeltaManager);
            timeDelta.setComboWidth(dateTime.getDateTimeWidth());
            return timeDelta;
        }
    }

    @Override
    protected void doSynchronizeComponentWidgetsToState() {
        super.doSynchronizeComponentWidgetsToState();
        synchronizeIntervalLockingWithState();
    }

    @Override
    protected void synchronizeTimeComponentToState(String identifier) {
        List<String> stateIdentifiers = ((IStatefulSpecifier) getSpecifier())
                .getStateIdentifiers();
        if (stateIdentifiers.get(0).equals(identifier)) {
            setDateTimeComponentValues(dateTime, getStateInternally(identifier));
        } else {
            setTimeDeltaComponentValues(timeDelta,
                    getStateInternally(identifier)
                            - getStateInternally(stateIdentifiers.get(0)));
        }
    }

    @Override
    protected void unlockedScaleThumbChanged() {

        /*
         * Since a single scale thumb change occurred, the time delta component
         * needs to be updated in case the interval has changed.
         */
        synchronizeTimeComponentToState(((IStatefulSpecifier) getSpecifier())
                .getStateIdentifiers().get(1));

        /*
         * Ensure that the scale widget's interval is locked or unlocked as
         * appropriate to the latest state of the time delta.
         */
        synchronizeIntervalLockingWithState();
    }

    // Private Methods

    /**
     * Set the specified date-time component to show the specified state.
     * 
     * @param dateTime
     *            Date-time component to have its value set.
     * @param value
     *            New value of the date-time component.
     */
    private void setDateTimeComponentValues(DateTimeComponent dateTime,
            long value) {
        dateTime.setState(value);
    }

    /**
     * Set the specified time delta component to show the specified state.
     * 
     * @param timeDelta
     *            Time delta component to have its value set.
     * @param value
     *            New delta value of the time delta component.
     */
    private void setTimeDeltaComponentValues(TimeDeltaComboComponent timeDelta,
            long value) {
        timeDelta.setValue(value);
    }

    /**
     * Synchronize the interval locking mode with the current state. The
     * interval should be locked if the "other delta" text is not showing in the
     * time delta component.
     */
    private void synchronizeIntervalLockingWithState() {
        getScale().setConstrainedThumbIntervalLocked(
                !timeDelta.isOtherDeltaTextShowing());
    }
}
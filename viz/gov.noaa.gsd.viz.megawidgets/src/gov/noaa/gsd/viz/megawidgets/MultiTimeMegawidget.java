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

import gov.noaa.gsd.common.utilities.ICurrentTimeProvider;
import gov.noaa.gsd.viz.megawidgets.validators.BoundedMultiLongValidator;
import gov.noaa.gsd.viz.widgets.DayHatchMarkGroup;
import gov.noaa.gsd.viz.widgets.IHatchMarkGroup;
import gov.noaa.gsd.viz.widgets.IMultiValueLinearControlListener;
import gov.noaa.gsd.viz.widgets.IMultiValueTooltipTextProvider;
import gov.noaa.gsd.viz.widgets.ISnapValueCalculator;
import gov.noaa.gsd.viz.widgets.IVisibleValueZoomCalculator;
import gov.noaa.gsd.viz.widgets.MultiValueLinearControl;
import gov.noaa.gsd.viz.widgets.MultiValueLinearControl.ChangeSource;
import gov.noaa.gsd.viz.widgets.MultiValueRuler;
import gov.noaa.gsd.viz.widgets.MultiValueScale;
import gov.noaa.gsd.viz.widgets.TimeHatchMarkGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Resource;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Multi-time megawidget, providing the user the ability to select one or more
 * times.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 27, 2014   3512     Chris.Golden Initial creation (extracted from
 *                                      TimeScaleMegawidget).
 * Jan 28, 2015   2331     Chris.Golden Added mutable properties allowing the
 *                                      defining of valid boundaries for the
 *                                      values, with potentially a different
 *                                      boundary for each state identifier.
 * Feb 03, 2015   2331     Chris.Golden Fixed bug that caused last thumb to be
 *                                      read-only when the interval between the
 *                                      thumbs was locked and the first thumb
 *                                      was movable.
 * Mar 31, 2015   6873     Chris.Golden Added code to ensure that mouse wheel
 *                                      events are not processed by the
 *                                      megawidget, but are instead passed up
 *                                      to any ancestor that is a scrolled
 *                                      composite.
 * Apr 09, 2015   7382     Chris.Golden Changed to make the scale bar optional.
 * Jul 23, 2015   4245     Chris.Golden Added time ruler above scale widget.
 * Aug 04, 2015   4123     Chris.Golden Changed to work with new signature of
 *                                      UiBuilder method.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see MultiTimeMegawidgetSpecifier
 */
public abstract class MultiTimeMegawidget extends
        ExplicitCommitStatefulMegawidget implements IParent<IControl>,
        IControl, IVisibleTimeRangeChanger {

    // Protected Static Constants

    /**
     * Set of all mutable property names for instances of this class.
     */
    protected static final Set<String> MUTABLE_PROPERTY_NAMES;
    static {
        Set<String> names = new HashSet<>(
                StatefulMegawidget.MUTABLE_PROPERTY_NAMES);
        names.add(IControlSpecifier.MEGAWIDGET_EDITABLE);
        names.add(MultiTimeMegawidgetSpecifier.MEGAWIDGET_STATE_EDITABLES);
        names.add(MultiTimeMegawidgetSpecifier.MINIMUM_ALLOWABLE_TIME);
        names.add(MultiTimeMegawidgetSpecifier.MAXIMUM_ALLOWABLE_TIME);
        names.add(MultiTimeMegawidgetSpecifier.MINIMUM_VISIBLE_TIME);
        names.add(MultiTimeMegawidgetSpecifier.MAXIMUM_VISIBLE_TIME);
        MUTABLE_PROPERTY_NAMES = ImmutableSet.copyOf(names);
    };

    /**
     * Number of milliseconds in a minute.
     */
    protected static final long MINUTE_INTERVAL = TimeUnit.MINUTES.toMillis(1);

    /**
     * Number of milliseconds in an hour.
     */
    protected static final long HOUR_INTERVAL = TimeUnit.HOURS.toMillis(1);

    /**
     * Number of milliseconds in a day.
     */
    protected static final long DAY_INTERVAL = TimeUnit.DAYS.toMillis(1);

    /**
     * Minimum visible time range as an epoch time delta in milliseconds.
     */
    protected static final long MIN_VISIBLE_TIME_RANGE = 2L * HOUR_INTERVAL;

    /**
     * Maximum visible time range as an epoch time delta in milliseconds.
     */
    protected static final long MAX_VISIBLE_TIME_RANGE = 8L * DAY_INTERVAL;

    /**
     * Snap value calculator, used to generate snap-to values for the scale
     * widget.
     */
    protected static final ISnapValueCalculator SNAP_VALUE_CALCULATOR = new ISnapValueCalculator() {

        private final long INTERVAL = MINUTE_INTERVAL;

        private final long HALF_INTERVAL = INTERVAL / 2L;

        @Override
        public long getSnapThumbValue(long value, long minimum, long maximum) {
            long remainder = value % INTERVAL;
            if (remainder < HALF_INTERVAL) {
                value -= remainder;
            } else {
                value += INTERVAL - remainder;
            }
            if (value < minimum) {
                value += INTERVAL
                        * (((minimum - value) / INTERVAL) + ((minimum - value)
                                % INTERVAL == 0 ? 0L : 1L));
            } else if (value > maximum) {
                value -= INTERVAL
                        * (((value - maximum) / INTERVAL) + ((value - maximum)
                                % INTERVAL == 0 ? 0L : 1L));
            }
            return value;
        }
    };

    // Private Static Constants

    /**
     * Set of mutable properties to be ignored within
     * {@link #setMutableProperties(Map)} when handling the general cases,
     * because they have been dealt with already by that point in the method
     * body.
     */
    private static final Set<String> IGNORE_FOR_SET_MUTABLE_PROPERTIES = Sets
            .newHashSet(MultiTimeMegawidgetSpecifier.MINIMUM_VISIBLE_TIME,
                    MultiTimeMegawidgetSpecifier.MAXIMUM_VISIBLE_TIME,
                    MultiTimeMegawidgetSpecifier.MINIMUM_ALLOWABLE_TIME,
                    MultiTimeMegawidgetSpecifier.MAXIMUM_ALLOWABLE_TIME);

    /**
     * Height of vertical padding in pixels above multi-value scales.
     */
    private static final int SCALE_VERTICAL_PADDING_TOP = 1;

    /**
     * Height of vertical padding in pixels below multi-value scales.
     */
    private static final int SCALE_VERTICAL_PADDING_BOTTOM = 6;

    // Private Classes

    /**
     * Listener for the multi-value ruler.
     */
    private class MultiValueRulerListener implements
            IMultiValueLinearControlListener {

        @Override
        public void visibleValueRangeChanged(MultiValueLinearControl widget,
                long lowerValue, long upperValue, ChangeSource source) {
            if (source != ChangeSource.METHOD_INVOCATION) {
                scale.setVisibleValueRange(lowerValue, upperValue);
                if (visibleTimeRangeListener != null) {
                    visibleTimeRangeListener.visibleTimeRangeChanged(
                            MultiTimeMegawidget.this, lowerValue, upperValue);
                }
            }
        }

        @Override
        public void constrainedThumbValuesChanged(
                MultiValueLinearControl widget, long[] values,
                ChangeSource source) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void freeThumbValuesChanged(MultiValueLinearControl widget,
                long[] values, ChangeSource source) {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Listener for the multi-value scale.
     */
    private class MultiValueScaleListener implements
            IMultiValueLinearControlListener {

        @Override
        public void visibleValueRangeChanged(MultiValueLinearControl widget,
                long lowerValue, long upperValue,
                MultiValueLinearControl.ChangeSource source) {

            /*
             * No action.
             */
        }

        @Override
        public void constrainedThumbValuesChanged(
                MultiValueLinearControl widget, long[] values,
                MultiValueLinearControl.ChangeSource source) {

            /*
             * If the change source is not user-GUI interaction, do nothing.
             */
            if ((source != MultiValueLinearControl.ChangeSource.USER_GUI_INTERACTION_ONGOING)
                    && (source != MultiValueLinearControl.ChangeSource.USER_GUI_INTERACTION_COMPLETE)) {
                return;
            }

            /*
             * If only ending state changes are to result in notifications, and
             * this is the first of an ongoing set of state changes, then copy
             * the state before this change is processed.
             */
            if (onlySendEndStateChanges
                    && (source == MultiValueLinearControl.ChangeSource.USER_GUI_INTERACTION_ONGOING)
                    && (lastForwardedStatesForIds == null)) {
                lastForwardedStatesForIds = new HashMap<>(statesForIds);
            }

            /*
             * See if notification of listeners should occur as the new values
             * are processed. If all state changes are to result in
             * notifications, or if this is an ending state change and no
             * ongoing state changes occurred beforehand, notification should
             * occur.
             */
            boolean notify = (!onlySendEndStateChanges || ((lastForwardedStatesForIds == null) && (source == MultiValueLinearControl.ChangeSource.USER_GUI_INTERACTION_COMPLETE)));

            /*
             * Iterate through the thumbs, determining which have changed their
             * values and responding accordingly. Any notification for these
             * values must occur after all the values have been changed, to
             * avoid having a notification of one value change go out that makes
             * that value higher than the next value, even though once all
             * values have been set, they will be in proper ascending order.
             */
            MultiTimeMegawidgetSpecifier specifier = getSpecifier();
            List<String> stateIdentifiers = specifier.getStateIdentifiers();
            Map<String, Object> valuesForChangedStates = new HashMap<>(
                    stateIdentifiers.size());
            for (int j = 0; j < values.length; j++) {

                /*
                 * Get the new value and see if it has changed, and if so, make
                 * a note of the new value and note that it should be forwarded
                 * to any listeners if this is something that should be sent on.
                 */
                String identifier = stateIdentifiers.get(j);
                if ((statesForIds.get(identifier) == null)
                        || (values[j] != statesForIds.get(identifier))) {
                    statesForIds.put(identifier, values[j]);
                    synchronizeTimeComponentToState(identifier);
                    if (notify) {
                        valuesForChangedStates.put(identifier, values[j]);
                    }
                }
            }
            if (scale.isConstrainedThumbIntervalLocked() == false) {
                unlockedScaleThumbChanged();
            }
            notifyListener(valuesForChangedStates);

            /*
             * If only ending state changes are to result in notifications, this
             * is such a state change, and at least one ongoing state change
             * occurred right before it, see if the state is now different from
             * what it was before the preceding set of ongoing state changes
             * occurred.
             */
            if ((lastForwardedStatesForIds != null)
                    && (source == MultiValueLinearControl.ChangeSource.USER_GUI_INTERACTION_COMPLETE)) {

                /*
                 * Compare the current state values with the ones from before
                 * the ongoing state change set occurred; for each of these
                 * pairs that is different, send a notification that the
                 * corresponding state identifier's value has changed.
                 */
                valuesForChangedStates = new HashMap<>(stateIdentifiers.size());
                for (String identifier : stateIdentifiers) {
                    if (statesForIds.get(identifier).equals(
                            lastForwardedStatesForIds.get(identifier)) == false) {
                        valuesForChangedStates.put(identifier,
                                statesForIds.get(identifier));
                    }
                }
                notifyListener(valuesForChangedStates);

                /*
                 * Forget about the last forwarded states, as they are not
                 * needed unless another set of ongoing state changes occurs, in
                 * which case they will be recreated at that time.
                 */
                lastForwardedStatesForIds = null;
            }
        }

        @Override
        public void freeThumbValuesChanged(MultiValueLinearControl widget,
                long[] values, MultiValueLinearControl.ChangeSource source) {

            /*
             * No action.
             */
        }
    }

    // Private Variables

    /**
     * Visible time range change listener.
     */
    private final IVisibleTimeRangeListener visibleTimeRangeListener;

    /**
     * Current time provider.
     */
    private final ICurrentTimeProvider currentTimeProvider;

    /**
     * Map pairing state identifier keys with their current values in
     * milliseconds since the epoch as values.
     */
    private final Map<String, Long> statesForIds;

    /**
     * Copy of {@link #statesForIds} made just before the first state change
     * resulting from a thumb drag is processed. This is used only if the
     * specifier indicates that rapidly-changing values resulting in state
     * changes should not prompt listener notifications.
     */
    private Map<String, Long> lastForwardedStatesForIds;

    /**
     * Map pairing state identifier keys with their current editability flags as
     * values.
     */
    private Map<String, Boolean> editabilityForIds;

    /**
     * Main label of the megawidget, if any.
     */
    private Label mainLabel;

    /**
     * List of labels created for the time components.
     */
    private List<Label> labels;

    /**
     * Mapping of state identifier keys to time components as values.
     */
    private ImmutableMap<String, ITimeComponent> timeComponentsForIds;

    /**
     * Time ruler component.
     */
    private MultiValueRuler ruler;

    /**
     * Multi-thumbed scale component.
     */
    private MultiValueScale scale;

    /**
     * Flag indicating whether state changes that occur as a result of a thumb
     * drag should be forwarded or not.
     */
    private final boolean onlySendEndStateChanges;

    /**
     * Control component helper.
     */
    private final ControlComponentHelper helper;

    /**
     * Detail child megawidget manager.
     */
    private DetailChildrenManager childManager;

    /**
     * List of additional composites created to hold detail megawidgets.
     */
    private List<GridLayout> additionalDetailCompositeLayouts;

    /**
     * State validator.
     */
    private final BoundedMultiLongValidator stateValidator;

    // Protected Constructors

    /**
     * Construct a standard instance.
     * 
     * @param specifier
     *            Specifier.
     * @param paramMap
     *            Hash table mapping megawidget creation time parameter
     *            identifiers to values.
     * @throws MegawidgetException
     *             If an error occurs while creating or initializing any
     *             megawidgets acting as detail fields for the various states.
     */
    protected MultiTimeMegawidget(MultiTimeMegawidgetSpecifier specifier,
            Map<String, Object> paramMap) throws MegawidgetException {
        super(specifier, paramMap);
        visibleTimeRangeListener = (IVisibleTimeRangeListener) paramMap
                .get(VISIBLE_TIME_RANGE_LISTENER);
        helper = new ControlComponentHelper(specifier);
        stateValidator = specifier.getStateValidator();
        statesForIds = new HashMap<>();
        List<String> stateIdentifiers = specifier.getStateIdentifiers();
        for (String identifier : stateIdentifiers) {
            statesForIds.put(identifier,
                    (Long) specifier.getStartingState(identifier));
        }

        /*
         * Get the current time provider.
         */
        ICurrentTimeProvider provider = (ICurrentTimeProvider) paramMap
                .get(MultiTimeMegawidgetSpecifier.CURRENT_TIME_PROVIDER);
        if (provider == null) {
            provider = TimeMegawidgetSpecifier.DEFAULT_CURRENT_TIME_PROVIDER;
        }
        currentTimeProvider = provider;

        onlySendEndStateChanges = !specifier.isSendingEveryChange();
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
        } else if (name
                .equals(MultiTimeMegawidgetSpecifier.MEGAWIDGET_STATE_EDITABLES)) {
            return new HashMap<>(editabilityForIds);
        } else if (name
                .equals(MultiTimeMegawidgetSpecifier.MINIMUM_ALLOWABLE_TIME)) {
            return getMinimumAllowableTimes();
        } else if (name
                .equals(MultiTimeMegawidgetSpecifier.MAXIMUM_ALLOWABLE_TIME)) {
            return getMaximumAllowableTimes();
        } else if (name
                .equals(MultiTimeMegawidgetSpecifier.MINIMUM_VISIBLE_TIME)) {
            return getLowerVisibleTime();
        } else if (name
                .equals(MultiTimeMegawidgetSpecifier.MAXIMUM_VISIBLE_TIME)) {
            return getUpperVisibleTime();
        }
        return super.getMutableProperty(name);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setMutableProperty(String name, Object value)
            throws MegawidgetPropertyException {
        if (name.equals(IControlSpecifier.MEGAWIDGET_EDITABLE)) {
            setEditable(ConversionUtilities.getPropertyBooleanValueFromObject(
                    getSpecifier().getIdentifier(), getSpecifier().getType(),
                    value, name, null));
        } else if (name
                .equals(MultiTimeMegawidgetSpecifier.MEGAWIDGET_STATE_EDITABLES)) {

            /*
             * Ensure that the value is a map of state identifiers to booleans.
             */
            Map<String, Boolean> map = null;
            try {
                map = (HashMap<String, Boolean>) value;
                if (map == null) {
                    throw new NullPointerException();
                }
            } catch (Exception e) {
                throw new MegawidgetPropertyException(getSpecifier()
                        .getIdentifier(), name, getSpecifier().getType(),
                        value, "bad map of booleans", e);
            }

            /*
             * Set each state's editability in turn.
             */
            for (String identifier : map.keySet()) {
                setStateEditable(identifier, map.get(identifier));
            }
        } else if (name
                .equals(MultiTimeMegawidgetSpecifier.MINIMUM_ALLOWABLE_TIME)) {
            setMinimumAllowableTimes(value);
        } else if (name
                .equals(MultiTimeMegawidgetSpecifier.MAXIMUM_ALLOWABLE_TIME)) {
            setMaximumAllowableTimes(value);
        } else if (name
                .equals(MultiTimeMegawidgetSpecifier.MINIMUM_VISIBLE_TIME)) {
            setVisibleTimeRange(
                    ConversionUtilities.getPropertyLongValueFromObject(
                            getSpecifier().getIdentifier(), getSpecifier()
                                    .getType(), value, name, null),
                    getUpperVisibleTime());
        } else if (name
                .equals(MultiTimeMegawidgetSpecifier.MAXIMUM_VISIBLE_TIME)) {
            setVisibleTimeRange(getLowerVisibleTime(),
                    ConversionUtilities.getPropertyLongValueFromObject(
                            getSpecifier().getIdentifier(), getSpecifier()
                                    .getType(), value, name, null));
        } else {
            super.setMutableProperty(name, value);
        }
    }

    @Override
    public void setMutableProperties(Map<String, Object> properties)
            throws MegawidgetPropertyException {

        /*
         * If the minimum or maximum visible times are being set, set them
         * first, ensuring that the two boundaries are set in the order that
         * will allow the set to occur without error (if they are allowable).
         */
        Object minValueObj = properties
                .get(MultiTimeMegawidgetSpecifier.MINIMUM_VISIBLE_TIME);
        Object maxValueObj = properties
                .get(MultiTimeMegawidgetSpecifier.MAXIMUM_VISIBLE_TIME);
        if ((minValueObj != null) && (maxValueObj != null)) {
            if ((minValueObj instanceof Number) == false) {
                MultiTimeMegawidgetSpecifier specifier = getSpecifier();
                throw new MegawidgetPropertyException(
                        specifier.getIdentifier(),
                        (minValueObj instanceof Number ? MultiTimeMegawidgetSpecifier.MAXIMUM_VISIBLE_TIME
                                : MultiTimeMegawidgetSpecifier.MINIMUM_VISIBLE_TIME),
                        specifier.getType(),
                        (minValueObj instanceof Number ? maxValueObj
                                : minValueObj), "must be long integer");
            }
            if (((Number) minValueObj).longValue() >= scale
                    .getUpperVisibleValue()) {
                setMutableProperty(
                        MultiTimeMegawidgetSpecifier.MAXIMUM_VISIBLE_TIME,
                        maxValueObj);
                setMutableProperty(
                        MultiTimeMegawidgetSpecifier.MINIMUM_VISIBLE_TIME,
                        minValueObj);
            } else {
                setMutableProperty(
                        MultiTimeMegawidgetSpecifier.MINIMUM_VISIBLE_TIME,
                        minValueObj);
                setMutableProperty(
                        MultiTimeMegawidgetSpecifier.MAXIMUM_VISIBLE_TIME,
                        maxValueObj);
            }
        } else if (minValueObj != null) {
            setMutableProperty(
                    MultiTimeMegawidgetSpecifier.MINIMUM_VISIBLE_TIME,
                    minValueObj);
        } else if (maxValueObj != null) {
            setMutableProperty(
                    MultiTimeMegawidgetSpecifier.MAXIMUM_VISIBLE_TIME,
                    maxValueObj);
        }

        /*
         * If the minimum or maximum allowable values are being set, set them
         * first, setting them together if both are being set, or one or the
         * other if only one is being set. If setting them together,
         */
        minValueObj = properties
                .get(MultiTimeMegawidgetSpecifier.MINIMUM_ALLOWABLE_TIME);
        maxValueObj = properties
                .get(MultiTimeMegawidgetSpecifier.MAXIMUM_ALLOWABLE_TIME);
        boolean valuesNotYetHandled = true;
        if ((minValueObj != null) && (maxValueObj != null)) {
            if (properties
                    .containsKey(IStatefulSpecifier.MEGAWIDGET_STATE_VALUES)) {
                stateValidator.setRanges(minValueObj, maxValueObj);
                setMutableProperty(
                        IStatefulSpecifier.MEGAWIDGET_STATE_VALUES,
                        properties
                                .get(IStatefulSpecifier.MEGAWIDGET_STATE_VALUES));
                valuesNotYetHandled = false;
                handleAllowableRangesChange();
            } else {
                setAllowableRanges(minValueObj, maxValueObj);
            }
        } else if (minValueObj != null) {
            if (properties
                    .containsKey(IStatefulSpecifier.MEGAWIDGET_STATE_VALUES)) {
                stateValidator.setMinimumValues(minValueObj);
                setMutableProperty(
                        IStatefulSpecifier.MEGAWIDGET_STATE_VALUES,
                        properties
                                .get(IStatefulSpecifier.MEGAWIDGET_STATE_VALUES));
                valuesNotYetHandled = false;
                handleAllowableMinimumsChange();
            } else {
                setMutableProperty(
                        MultiTimeMegawidgetSpecifier.MINIMUM_ALLOWABLE_TIME,
                        minValueObj);
            }
        } else if (maxValueObj != null) {
            if (properties
                    .containsKey(IStatefulSpecifier.MEGAWIDGET_STATE_VALUES)) {
                stateValidator.setMaximumValues(maxValueObj);
                setMutableProperty(
                        IStatefulSpecifier.MEGAWIDGET_STATE_VALUES,
                        properties
                                .get(IStatefulSpecifier.MEGAWIDGET_STATE_VALUES));
                valuesNotYetHandled = false;
                handleAllowableMaximumsChange();
            } else {
                setMutableProperty(
                        MultiTimeMegawidgetSpecifier.MAXIMUM_ALLOWABLE_TIME,
                        minValueObj);
            }
        }

        /*
         * Do what would have been done by the superclass method, except for
         * ignoring anything that has already been handled above.
         */
        for (String name : properties.keySet()) {
            if ((IGNORE_FOR_SET_MUTABLE_PROPERTIES.contains(name) == false)
                    && (valuesNotYetHandled || (name
                            .equals(IStatefulSpecifier.MEGAWIDGET_STATE_VALUES) == false))) {
                setMutableProperty(name, properties.get(name));
            }
        }
    }

    @Override
    public final boolean isEditable() {
        return helper.isEditable();
    }

    @Override
    public final void setEditable(boolean editable) {
        helper.setEditable(editable);
        doSetEditable(editable);
    }

    @Override
    public int getLeftDecorationWidth() {
        return ((labels == null) || (labels.size() == 0) ? 0 : helper
                .getWidestWidgetWidth(labels.toArray(new Label[labels.size()])));
    }

    @Override
    public void setLeftDecorationWidth(int width) {

        /*
         * Iterate through the labels, setting each of them to have the
         * specified width.
         */
        if ((labels != null) && (labels.size() > 0)) {
            helper.setWidgetsWidth(width,
                    labels.toArray(new Label[labels.size()]));
        }

        /*
         * If any additional composites were created to hold detail megawidgets
         * on rows after any of the time component rows, adjust their left
         * margins to match the new width.
         */
        for (GridLayout layout : additionalDetailCompositeLayouts) {
            layout.marginLeft = width;
        }
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

    @Override
    public final List<IControl> getChildren() {
        return (childManager == null ? Collections.<IControl> emptyList()
                : childManager.getDetailMegawidgets());
    }

    /**
     * Get the map of state identifiers to their minimum allowable times.
     * 
     * @return Map of state identifiers to their minimum allowable times.
     */
    public Map<String, Long> getMinimumAllowableTimes() {
        return stateValidator.getMinimumValues();
    }

    /**
     * Get the map of state identifiers to their maximum allowable times.
     * 
     * @return Map of state identifiers to their maximum allowable times.
     */
    public Map<String, Long> getMaximumAllowableTimes() {
        return stateValidator.getMaximumValues();
    }

    /**
     * Set the minimum values for the states.
     * 
     * @param values
     *            Map of state identifiers to their minimum values.
     * @throws MegawidgetPropertyException
     *             If the values are not valid.
     */
    public final void setMinimumAllowableTimes(Object values)
            throws MegawidgetPropertyException {
        stateValidator.setMinimumValues(values);
        handleAllowableMinimumsChange();
    }

    /**
     * Set the maximum values for the states.
     * 
     * @param values
     *            Map of state identifiers to their minimum values.
     * @throws MegawidgetPropertyException
     *             If the values are not valid.
     */
    public final void setMaximumAllowableTimes(Object values)
            throws MegawidgetPropertyException {
        stateValidator.setMaximumValues(values);
        handleAllowableMaximumsChange();
    }

    /**
     * Set the minimum and maximum values.
     * 
     * @param minimumValues
     *            Map of state identifiers to their minimum values.
     * @param maximumValues
     *            Map of state identifiers to their maximum values.
     * @throws MegawidgetPropertyException
     *             If the new values are not valid.
     */
    public final void setAllowableRanges(Object minimumValues,
            Object maximumValues) throws MegawidgetPropertyException {
        stateValidator.setRanges(minimumValues, maximumValues);
        handleAllowableRangesChange();
    }

    /**
     * Get the lower end of the visible time range.
     * 
     * @return Lower end of the visible time range.
     */
    public long getLowerVisibleTime() {
        return scale.getLowerVisibleValue();
    }

    /**
     * Get the upper end of the visible time range.
     * 
     * @return Upper end of the visible time range.
     */
    public long getUpperVisibleTime() {
        return scale.getUpperVisibleValue();
    }

    /**
     * Set the visible time range to that specified.
     * 
     * @param lower
     *            Lower end of the visible time range.
     * @param upper
     *            Upper end of the visible time range.
     */
    public void setVisibleTimeRange(long lower, long upper) {
        ruler.setVisibleValueRange(lower, upper);
        scale.setVisibleValueRange(lower, upper);
    }

    /**
     * Determine whether or not the specified state is editable. Note that if
     * {@link #isEditable()} returns false, then the the individual editability
     * flags for different states are ignored, and the entire megawidget is
     * read-only.
     * 
     * @param identifier
     *            Identifier of the state for which the editability is to be
     *            determined.
     * @return True if the state is editable, false otherwise.
     */
    public final boolean isStateEditable(String identifier) {
        return editabilityForIds.get(identifier);
    }

    /**
     * Set the editability of the specified state. Note that if
     * {@link #isEditable()} returns false, then the the individual editability
     * flags for different states are ignored, and the entire megawidget is
     * read-only.
     * 
     * @param identifier
     *            Identifier of the state for which the editability is to be
     *            determined.
     * @param editable
     *            Flag indicating whether or not the state is to be editable.
     */
    public final void setStateEditable(String identifier, boolean editable) {
        if (editable == editabilityForIds.get(identifier)) {
            return;
        }
        editabilityForIds.put(identifier, editable);
        if (isEditable()) {
            timeComponentsForIds.get(identifier).setEditable(editable, helper);
            setStateThumbEditable(identifier);
        }
    }

    // Protected Methods

    /**
     * Set the thumb in the scale widget that goes with the specified identifier
     * to have the correct editability.
     * 
     * @param identifier
     *            State identifier which which the thumb to have its editability
     *            updated is associated.
     */
    protected final void setStateThumbEditable(String identifier) {
        scale.setConstrainedThumbEditable(
                ((MultiTimeMegawidgetSpecifier) getSpecifier())
                        .getIndicesForStateIdentifiers().get(identifier),
                isScaleThumbEditable(identifier));
    }

    /**
     * Create a time component for the specified state.
     * 
     * @param stateIndex
     *            Index into the state identifiers list for which the time
     *            component is to be created.
     * @param parent
     *            Composite into which to insert the new component.
     * @param text
     *            Label text, or <code>null</code> if none is to be shown.
     * @param verticalIndent
     *            Vertical indent of the component.
     * @return Time component.
     */
    protected abstract ITimeComponent createTimeComponent(int stateIndex,
            Composite parent, String text, int verticalIndent);

    /**
     * Synchronize the time component associated with the specified state
     * identifier to show the value currently associated with said state.
     * 
     * @param identifier
     *            State identifier with which the time component to be
     *            synchronized is associated.
     */
    protected abstract void synchronizeTimeComponentToState(String identifier);

    /**
     * Receive notification that a scale thumb may have moved when the scale's
     * intervals are unlocked.
     */
    protected abstract void unlockedScaleThumbChanged();

    /**
     * Create the widget components of this megawidget. This method must be
     * called by subclass's constructors once they have initialized any member
     * variables.
     * 
     * @param specifier
     *            Specifier.
     * @param parent
     *            Parent of the megawidget.
     * @param paramMap
     *            Hash table mapping megawidget creation time parameter
     *            identifiers to values.
     * @param minimumInterval
     *            Minimum interval to be used for the multi-value scale.
     * @throws MegawidgetException
     *             If an error occurs while creating or initializing any
     *             megawidgets acting as detail fields for the various states.
     */
    protected final void createWidgetComponents(
            MultiTimeMegawidgetSpecifier specifier, Composite parent,
            Map<String, Object> paramMap, long minimumInterval)
            throws MegawidgetException {

        /*
         * Create a panel in which to place the widgets.
         */
        Composite panel = UiBuilder.buildComposite(parent, 1, SWT.NONE,
                UiBuilder.CompositeType.SINGLE_ROW, specifier);
        ((GridData) panel.getLayoutData()).verticalAlignment = SWT.TOP;

        /*
         * Add an overall label if one is specified and if either multiple state
         * identifiers exist, or only one exists but it has its own label. If,
         * however, only one state identifier exists and it has no label, use
         * the main label in its place.
         */
        List<String> stateIdentifiers = specifier.getStateIdentifiers();
        String labelString = specifier.getLabel();
        String firstStateLabelString = specifier.getStateLabel(stateIdentifiers
                .get(0));
        boolean useMainLabelAsStateLabel = false;
        if ((labelString != null) && (labelString.length() > 0)) {
            if ((stateIdentifiers.size() == 1)
                    && ((firstStateLabelString == null) || (firstStateLabelString
                            .length() == 0))) {
                useMainLabelAsStateLabel = true;
                mainLabel = null;
            } else {
                mainLabel = UiBuilder.buildLabel(panel, specifier);
            }
        } else {
            mainLabel = null;
        }

        /*
         * Get the starting value(s) for the different state identifiers, as
         * well as their starting minimum and maximum allowable values, and
         * determine the starting editability states for the state identifiers.
         */
        editabilityForIds = new HashMap<>();
        long[] startingValues = new long[specifier.getStateIdentifiers().size()];
        long[] startingMinimums = new long[startingValues.length];
        long[] startingMaximums = new long[startingValues.length];
        boolean[] startingEditabilities = new boolean[startingValues.length];
        for (int j = 0; j < startingValues.length; j++) {
            String identifier = specifier.getStateIdentifiers().get(j);
            startingValues[j] = statesForIds.get(identifier);
            startingMinimums[j] = specifier.getMinimumValue(identifier);
            startingMaximums[j] = specifier.getMaximumValue(identifier);
            startingEditabilities[j] = specifier.isStateEditable(identifier);
            editabilityForIds.put(identifier, startingEditabilities[j]);
        }

        /*
         * Create the child manager for the detail megawidgets that may be
         * associated with the different states if appropriate.
         */
        childManager = (specifier.getChildMegawidgetSpecifiers().size() > 0 ? new DetailChildrenManager(
                paramMap) : null);

        /*
         * Iterate through the state identifiers, creating time components for
         * each.
         */
        Map<String, ITimeComponent> timeComponentsForIds = new HashMap<>();
        labels = new ArrayList<>();
        additionalDetailCompositeLayouts = new ArrayList<>();
        int greatestHeight = 0;
        for (int j = 0; j < specifier.getStateIdentifiers().size(); j++) {
            String identifier = specifier.getStateIdentifiers().get(j);

            /*
             * Determine what text label, if any, to use for this state
             * identifier.
             */
            String text;
            if (useMainLabelAsStateLabel) {
                useMainLabelAsStateLabel = false;
                text = labelString;
            } else {
                text = specifier.getStateLabel(identifier);
            }

            /*
             * Create the time field component and any detail megawidgets that
             * go with them. If the latter are created and they are the greatest
             * height recorded so far, remember their height.
             */
            int height = createTimeFieldComponent(specifier, panel, identifier,
                    j, text, timeComponentsForIds);
            if (height > greatestHeight) {
                greatestHeight = height;
            }
        }
        this.timeComponentsForIds = ImmutableMap.copyOf(timeComponentsForIds);

        /*
         * Determine which detail megawidgets that were created take up the full
         * width of this megawidget, and align all such megawidgets' components
         * to bring some visual order to the widget soup.
         */
        if (childManager != null) {
            List<IControl> fullWidthDetailMegawidgets = new ArrayList<>();
            for (IControl detailMegawidget : childManager
                    .getDetailMegawidgets()) {
                if (((IControlSpecifier) detailMegawidget.getSpecifier())
                        .isFullWidthOfDetailPanel()) {
                    fullWidthDetailMegawidgets.add(detailMegawidget);
                }
            }
            ControlComponentHelper
                    .alignMegawidgetsElements(fullWidthDetailMegawidgets);
        }

        /*
         * If at least one time component has additional megawidgets, ensure
         * that all the time components have the right minimum height so as to
         * not make the highest row look larger than the others.
         */
        if (greatestHeight > 0) {
            for (ITimeComponent timeComponent : timeComponentsForIds.values()) {
                timeComponent.setHeight(greatestHeight);
            }
        }

        /*
         * Create the time ruler component.
         */
        createRulerComponent(specifier, panel, paramMap);

        /*
         * Bind the ruler component's visible range change events to propagate
         * to the scale and to result in a notification.
         */
        ruler.addMultiValueLinearControlListener(new MultiValueRulerListener());

        /*
         * Create the multi-thumbed scale component.
         */
        createMultiValueScaleComponent(specifier, panel, paramMap,
                minimumInterval, startingValues, startingMinimums,
                startingMaximums, startingEditabilities);

        /*
         * Bind the scale component's value change events to trigger a change in
         * the record of the state for the widget, and a change in the
         * corresponding text component.
         */
        scale.addMultiValueLinearControlListener(new MultiValueScaleListener());

        /*
         * Render the widget uneditable if necessary.
         */
        if (isEditable() == false) {
            doSetEditable(false);
        }
    }

    @Override
    protected final void doSetEnabled(boolean enable) {
        if (mainLabel != null) {
            mainLabel.setEnabled(enable);
        }
        for (ITimeComponent timeComponent : timeComponentsForIds.values()) {
            timeComponent.setEnabled(enable);
        }
        ruler.setEnabled(enable);
        scale.setEnabled(enable);
        for (IControl child : getChildren()) {
            child.setEnabled(enable);
        }
    }

    @Override
    protected final Object doGetState(String identifier) {
        return statesForIds.get(identifier);
    }

    @Override
    protected void doSetState(String identifier, Object state)
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
        Map<String, Long> validMap;
        try {
            validMap = stateValidator.convertToStateValues(map);
        } catch (MegawidgetException e) {
            throw new MegawidgetStateException(e);
        }
        statesForIds.put(identifier, validMap.get(identifier));

        /*
         * Synchronize the user-facing widgets for this state identifier with
         * the new state.
         */
        synchronizeComponentWidgetsToState(identifier);
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
        Map<String, Long> validMap;
        try {
            validMap = stateValidator.convertToStateValues(map);
        } catch (MegawidgetException e) {
            throw new MegawidgetStateException(e);
        }
        statesForIds.clear();
        statesForIds.putAll(validMap);

        /*
         * Synchronize the user-facing component widgets with the new state.
         */
        synchronizeComponentWidgetsToState();
    }

    @Override
    protected final String doGetStateDescription(String identifier, Object state)
            throws MegawidgetStateException {
        return (state == null ? null : DateTimeComponent
                .getStateDescription(ConversionUtilities
                        .getStateLongValueFromObject(identifier, getSpecifier()
                                .getType(), state, null)));
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
    protected void doSynchronizeComponentWidgetsToState() {

        /*
         * Commit the values to the state records, and change the text widgets
         * to match.
         */
        MultiTimeMegawidgetSpecifier specifier = getSpecifier();
        for (String identifier : statesForIds.keySet()) {
            Long value = statesForIds.get(identifier);
            setStateInternally(identifier, value);
            synchronizeTimeComponentToState(identifier);
        }

        /*
         * Compile a list of values, one per thumb, using the new values where
         * appropriate, and the old values where no new values are given.
         */
        List<String> stateIdentifiers = specifier.getStateIdentifiers();
        long[] values = new long[stateIdentifiers.size()];
        for (int j = 0; j < values.length; j++) {
            values[j] = statesForIds.get(stateIdentifiers.get(j));
        }

        /*
         * Tell the scale about the new thumb values.
         */
        try {
            scale.setConstrainedThumbValues(values);
        } catch (Exception e) {
            throw new IllegalStateException("set of time scale values failed",
                    e);
        }
    }

    @Override
    protected void doSynchronizeComponentWidgetsToState(String identifier) {

        /*
         * Record the change in the state records.
         */
        long value = statesForIds.get(identifier);
        setStateInternally(identifier, value);

        /*
         * Tell the scale widget and the time component about the change.
         */
        scale.setConstrainedThumbValue(
                ((MultiTimeMegawidgetSpecifier) getSpecifier())
                        .getIndicesForStateIdentifiers().get(identifier), value);
        synchronizeTimeComponentToState(identifier);
    }

    /**
     * Get the current time provider.
     * 
     * @return Current time provider.
     */
    protected final ICurrentTimeProvider getCurrentTimeProvider() {
        return currentTimeProvider;
    }

    /**
     * Get the state validator.
     * 
     * @return State validator.
     */
    protected final BoundedMultiLongValidator getStateValidator() {
        return stateValidator;
    }

    /**
     * Get the time component for the specified identifier.
     * 
     * @param identifier
     *            Identifier associated with the time component to be fetched.
     * @return Time component for the specified identifier.
     */
    @SuppressWarnings("unchecked")
    protected final <T extends ITimeComponent> T getTimeComponent(
            String identifier) {
        return (T) timeComponentsForIds.get(identifier);
    }

    /**
     * Get the multi-thumbed scale widget.
     * 
     * @return Multi-thumbed scale widget.
     */
    protected final MultiValueScale getScale() {
        return scale;
    }

    /**
     * Determine whether or not this megawidget should only send ending state
     * changes.
     * 
     * @return True if the megawidget should only send ending state changes,
     *         false otherwise.
     */
    protected final boolean isOnlySendEndStateChanges() {
        return onlySendEndStateChanges;
    }

    /**
     * Get the state internally for the specified identifier. This method may be
     * used by subclasses instead of {@link #getState(String)} or
     * {@link #doGetState(String)} to avoid pointless typecasting and exception
     * handling.
     * 
     * @param identifier
     *            Identifier of the state to be fetched.
     * @return Value of the specified state.
     */
    protected final Long getStateInternally(String identifier) {
        return statesForIds.get(identifier);
    }

    /**
     * Get the values for all state identifiers.
     * 
     * @return Copy of the map of state identifiers to their values.
     */
    protected final Map<String, Object> getStates() {
        Map<String, Object> map = new HashMap<>(statesForIds.size());
        map.putAll(statesForIds);
        return map;
    }

    /**
     * Set the state internally for the specified identifier. This method may be
     * used by subclasses instead of {@link #setState(String, Object)} or
     * {@link #doSetState(String, Object)} to avoid pointless typecasting and
     * exception handling.
     * 
     * @param identifier
     *            State identifier to be changed.
     * @param value
     *            New value of the state associated with the identifier.
     */
    protected final void setStateInternally(String identifier, Long value) {
        statesForIds.put(identifier, value);
        if (lastForwardedStatesForIds != null) {
            lastForwardedStatesForIds.put(identifier, value);
        }
    }

    /**
     * Convert the specified value to one that would be acceptable as a
     * multi-value scale value. This means that it will be snapped to the
     * closest legitimate value on the multi-value scale widget, and of course
     * between the minimum and maximum allowable values.
     * 
     * @param identifier
     *            State identifier of the value to be converted.
     * @param value
     *            Value to be converted.
     * @return Converted value.
     */
    protected final long convertToValueAcceptableToScale(String identifier,
            long value) {
        return SNAP_VALUE_CALCULATOR.getSnapThumbValue(value,
                stateValidator.getMinimumValue(identifier),
                stateValidator.getMaximumValue(identifier));
    }

    /**
     * Determine whether or not the specified value is within the range between
     * any neighboring values. The specified value must already have been run
     * through {@link #convertToValueAcceptableToScale(long)}.
     * 
     * @param index
     *            Index of the thumb in the multi-value scale for which this
     *            value is intended.
     * @param value
     *            Value to be checked.
     * @return True if the value is within the range between any neighboring
     *         values, false otherwise.
     */
    protected final boolean isValueBetweenNeighboringValues(int index,
            long value) {
        if ((index < scale.getConstrainedThumbValueCount() - 1)
                && (value > scale.getConstrainedThumbValue(index + 1)
                        - scale.getMinimumDeltaBetweenConstrainedThumbs())) {
            return false;
        } else if ((index > 0)
                && (value < scale.getConstrainedThumbValue(index - 1)
                        + scale.getMinimumDeltaBetweenConstrainedThumbs())) {
            return false;
        }
        return true;
    }

    /**
     * Determine whether or not the specified first value for the multi-value
     * scale would push any of the other values beyond the maximum. The
     * specified value must already have been run through
     * {@link #convertToValueAcceptableToScale(long)}. This method is to be used
     * when the multi-value scale's intervals are locked, and thus moving the
     * first value will also move the other values in concert.
     * 
     * @param value
     *            Value to be checked.
     * @return True if the value keeps all the other values within the allowable
     *         range, false otherwise.
     */
    protected final boolean isFirstValueKeepingAllValuesWithinRange(long value) {
        long delta = scale.getConstrainedThumbValue(scale
                .getConstrainedThumbValueCount() - 1)
                - scale.getConstrainedThumbValue(0);
        return (value + delta <= stateValidator.getHighestAllowableValue());
    }

    /**
     * Set the specified value, supplied by a time component, as the state
     * associated with the specified identifier. If set successfully, the
     * multi-value scale will be updated as well as the internal state record.
     * If the intervals in the multi-value scale are locked, if the state to be
     * changed is the first state in the megawidget, other values will be
     * changed accordingly in order to maintain their intervals.
     * 
     * @param identifier
     *            Identifier of the state to be changed.
     * @param index
     *            Index of the state identifier; must be 0 if the multi-value
     *            scale has its intervals locked.
     * @param value
     *            New value.
     * @return True if the state is changed, false otherwise.
     */
    protected final boolean setValueIfChanged(String identifier, int index,
            long value) {

        /*
         * If the value is actually different from the old value, handle the
         * change. Do it differently depending upon whether or not the scale's
         * intervals are locked and the state being changed is the first one.
         */
        if (scale.getConstrainedThumbValue(index) != value) {
            if (scale.isConstrainedThumbIntervalLocked() && (index == 0)) {

                /*
                 * Compile an array of new values by using the provided value as
                 * the first new value, and offsetting all the other values from
                 * that one by the same offsets they had from the old first
                 * value.
                 */
                List<Long> oldValues = scale.getConstrainedThumbValues();
                long[] values = new long[oldValues.size()];
                values[0] = value;
                long oldFirstValue = oldValues.get(0);
                for (int j = 1; j < values.length; j++) {
                    values[j] = value + oldValues.get(j) - oldFirstValue;
                }

                /*
                 * If the scale accepts the new values, record the new states
                 * internally.
                 */
                if (getScale().setConstrainedThumbValues(values)) {
                    List<String> stateIdentifiers = ((IStatefulSpecifier) getSpecifier())
                            .getStateIdentifiers();
                    for (int j = 0; j < values.length; j++) {
                        setStateInternally(stateIdentifiers.get(j), values[j]);
                    }
                    return true;
                }
            } else {

                /*
                 * If the value has changed and is acceptable to the scale
                 * widget, then change the use it as the new value.
                 */
                if (scale.setConstrainedThumbValue(index, value)) {
                    setStateInternally(identifier, value);
                    return true;
                }
            }
        }

        /*
         * Since no change occurred, reset the time component to show the old
         * value, in case the value that it is presenting is not the same as the
         * one that was passed in here (since the latter will have been
         * processed by the convertToValueAcceptableToScale() method).
         */
        synchronizeTimeComponentToState(identifier);
        return false;
    }

    // Private Methods

    /**
     * Create the time field component for the specified state identifier.
     * 
     * @param specifier
     *            Megawidget specifier.
     * @param parent
     *            Parent composite of any widgets to be created.
     * @param identifier
     *            State identifier for which to create the time field component.
     * @param stateIndex
     *            Index into the state identifiers list for which the time
     *            component is to be created.
     * @param labelText
     *            Text to be used to label the time field.
     * @param timeComponentsForIds
     *            Map of state identifiers to their time components. The time
     *            field component created here will be added to this map.
     * @return Height in pixels of the largest detail field megawidget created
     *         in the same row as the time field widgets, or 0 if no such detail
     *         field megawidget was created.
     * @throw MegawidgetException If an error occurs while creating or
     *        initializing any megawidgets acting as detail fields for this
     *        state.
     */
    private int createTimeFieldComponent(
            MultiTimeMegawidgetSpecifier specifier, Composite parent,
            String identifier, int stateIndex, String labelText,
            Map<String, ITimeComponent> timeComponentsForIds)
            throws MegawidgetException {

        /*
         * If there are additional megawidgets to be placed to the right of the
         * time component, create a composite to act as a parent for both the
         * time component and the additional megawidgets.
         */
        List<IControlSpecifier> detailSpecifiers = specifier
                .getDetailFieldsForState(identifier);
        Composite statePanel = UiBuilder
                .getOrCreateCompositeForComponentWithDetailMegawidgets(
                        detailSpecifiers, parent, SWT.NONE);

        /*
         * Create the time component for this state identifier, and make it
         * read-only if appropriate.
         */
        ITimeComponent timeComponent = createTimeComponent(stateIndex,
                statePanel, labelText,
                (statePanel == parent ? specifier.getSpacing() : 0));
        labels.add(timeComponent.getLabel());
        if (editabilityForIds.get(identifier) == false) {
            timeComponent.setEditable(false, helper);
        }

        /*
         * If there are additional megawidgets, lay out the composite in which
         * the time component and the first row of said megawidgets are found,
         * and create the megawidgets.
         */
        int height = 0;
        if (statePanel != parent) {
            GridData statePanelLayoutData = new GridData(SWT.LEFT, SWT.CENTER,
                    false, false);
            statePanelLayoutData.verticalIndent = specifier.getSpacing();
            statePanel.setLayoutData(statePanelLayoutData);
            List<Composite> additionalComposites = childManager
                    .createDetailChildMegawidgets(statePanel, parent, 0,
                            specifier.isEnabled(), detailSpecifiers)
                    .getComposites();
            for (Composite composite : additionalComposites) {
                additionalDetailCompositeLayouts.add((GridLayout) composite
                        .getLayout());
            }
            height = statePanel.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
        }

        /*
         * Add the time component to the map.
         */
        timeComponentsForIds.put(identifier, timeComponent);
        return height;
    }

    /**
     * Create the ruler component.
     * 
     * @param specifier
     *            Megawidget specifier.
     * @param parent
     *            Parent composite of any widgets to be created.
     * @param paramMap
     *            Hash table mapping megawidget creation time parameter
     *            identifiers to values.
     */
    private void createRulerComponent(MultiTimeMegawidgetSpecifier specifier,
            Composite parent, Map<String, Object> paramMap) {

        /*
         * Create the colors for the time ruler hatch marks, and record them as
         * allocated resources so that they can be disposed of properly.
         */
        List<Color> colors = Lists.newArrayList(new Color(Display.getCurrent(),
                128, 0, 0), new Color(Display.getCurrent(), 0, 0, 128),
                new Color(Display.getCurrent(), 0, 128, 0),
                new Color(Display.getCurrent(), 0, 128, 0),
                new Color(Display.getCurrent(), 131, 120, 103));
        final List<Resource> resources = new ArrayList<>(colors.size() + 1);
        for (Color color : colors) {
            resources.add(color);
        }

        /*
         * Create the time ruler's hatch mark groups.
         */
        List<IHatchMarkGroup> hatchMarkGroups = new ArrayList<>();
        hatchMarkGroups.add(new DayHatchMarkGroup());
        hatchMarkGroups.add(new TimeHatchMarkGroup(6L * HOUR_INTERVAL, 0.25f,
                colors.get(0), null));
        hatchMarkGroups.add(new TimeHatchMarkGroup(HOUR_INTERVAL, 0.18f, colors
                .get(1), null));
        hatchMarkGroups.add(new TimeHatchMarkGroup(30L * MINUTE_INTERVAL,
                0.11f, colors.get(2), null));
        hatchMarkGroups.add(new TimeHatchMarkGroup(10L * MINUTE_INTERVAL,
                0.05f, colors.get(3), null));

        /*
         * Create the ruler and configure it. The actual widget is an instance
         * of an anonymous subclass; the latter is needed because background and
         * foreground color changes must be ignored, since the ModeListener
         * objects may try to change the colors when the CAVE mode changes,
         * which in this case is undesirable.
         */
        ruler = new MultiValueRuler(parent,
                stateValidator.getLowestAllowableValue(),
                stateValidator.getHighestAllowableValue(), hatchMarkGroups) {
            @Override
            public void setBackground(Color background) {

                /*
                 * No action.
                 */
            }

            @Override
            public void setForeground(Color foreground) {

                /*
                 * No action.
                 */
            }
        };

        /*
         * Create the font to be used for smaller labels in the ruler, and
         * record it for later disposal.
         */
        FontData fontData = ruler.getFont().getFontData()[0];
        Font minuteFont = new Font(Display.getCurrent(), fontData.getName(),
                (fontData.getHeight() * 7) / 10, fontData.getStyle());
        resources.add(minuteFont);
        for (int j = 1; j < hatchMarkGroups.size(); j++) {
            ((TimeHatchMarkGroup) hatchMarkGroups.get(j))
                    .setMinuteFont(minuteFont);
        }

        /*
         * Ensure that zoom calculations are done correctly by limiting them,
         * and configure colors, size, etc.
         */
        ruler.setVisibleValueZoomCalculator(new IVisibleValueZoomCalculator() {
            @Override
            public long getVisibleValueRangeForZoom(MultiValueRuler ruler,
                    boolean zoomIn, int amplitude) {
                long range;
                if (zoomIn) {
                    range = (getVisibleTimeDelta() * 2L) / 3L;
                    if (range < MIN_VISIBLE_TIME_RANGE) {
                        return 0L;
                    }
                } else {
                    range = (getVisibleTimeDelta() * 3L) / 2L;
                    if (range > MAX_VISIBLE_TIME_RANGE) {
                        return 0L;
                    }
                }
                return range;
            }
        });
        ruler.setBorderColor(colors.get(4));
        ruler.setHeightMultiplier(2.95f);
        ruler.setSnapValueCalculator(SNAP_VALUE_CALCULATOR);
        ruler.setViewportDraggable(true);
        ruler.setTooltipTextProvider(new IMultiValueTooltipTextProvider() {

            @Override
            public String[] getTooltipTextForValue(
                    MultiValueLinearControl widget, long value) {
                return null;
            }

            @Override
            public String[] getTooltipTextForConstrainedThumb(
                    MultiValueLinearControl widget, int index, long value) {
                return null;
            }

            @Override
            public String[] getTooltipTextForFreeThumb(
                    MultiValueLinearControl widget, int index, long value) {
                return null;
            }
        });

        /*
         * Ensure that the SWT colors and fonts are disposed of when the ruler
         * is.
         */
        ruler.addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(DisposeEvent e) {
                for (Resource resource : resources) {
                    resource.dispose();
                }
            }
        });
        UiBuilder.setMultiValueRulerVisualComponentDimensions(ruler,
                SCALE_VERTICAL_PADDING_TOP, 0);
        ruler.setVisibleValueRange((Long) paramMap
                .get(MultiTimeMegawidgetSpecifier.MINIMUM_VISIBLE_TIME),
                (Long) paramMap
                        .get(MultiTimeMegawidgetSpecifier.MAXIMUM_VISIBLE_TIME));
        ruler.setEnabled(specifier.isEnabled());
        GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gridData.horizontalSpan = 2;
        gridData.verticalIndent = specifier.getSpacing();
        gridData.exclude = (specifier.isShowScale() == false);
        ruler.setLayoutData(gridData);
    }

    /**
     * Create the multi-value scale component.
     * 
     * @param specifier
     *            Megawidget specifier.
     * @param parent
     *            Parent composite of any widgets to be created.
     * @param paramMap
     *            Hash table mapping megawidget creation time parameter
     *            identifiers to values.
     * @param minimumInterval
     *            Minimum interval to be used for the multi-value scale.
     * @param startingValues
     *            Array of values to be used as the initial values.
     * @param startingMinimums
     *            Array of values to be used as the initial minimum values.
     * @param startingMaximums
     *            Array of values to be used as the initial maximum values.
     * @param startingEditabilities
     *            Array of booleans indicating which of the values in the
     *            corresponding indices of <code>startingValues</code> are to be
     *            editable to begin with.
     */
    private void createMultiValueScaleComponent(
            MultiTimeMegawidgetSpecifier specifier, Composite parent,
            Map<String, Object> paramMap, long minimumInterval,
            long[] startingValues, long[] startingMinimums,
            long[] startingMaximums, boolean[] startingEditabilities) {
        scale = new MultiValueScale(parent,
                stateValidator.getLowestAllowableValue(),
                stateValidator.getHighestAllowableValue());
        scale.setSnapValueCalculator(SNAP_VALUE_CALCULATOR);
        UiBuilder.setMultiValueScaleVisualComponentDimensions(scale,
                SCALE_VERTICAL_PADDING_TOP, SCALE_VERTICAL_PADDING_BOTTOM);
        scale.setVisibleValueRange((Long) paramMap
                .get(MultiTimeMegawidgetSpecifier.MINIMUM_VISIBLE_TIME),
                (Long) paramMap
                        .get(MultiTimeMegawidgetSpecifier.MAXIMUM_VISIBLE_TIME));
        scale.setMinimumDeltaBetweenConstrainedThumbs(minimumInterval);
        scale.setConstrainedThumbValues(startingValues);
        for (int j = 0; j < startingEditabilities.length; j++) {
            scale.setAllowableConstrainedValueRange(j, startingMinimums[j],
                    startingMaximums[j]);
            if (startingEditabilities[j] == false) {
                scale.setConstrainedThumbEditable(j, false);
            }
        }
        for (int j = 1; j < startingValues.length; j++) {
            scale.setConstrainedThumbRangeColor(j, Display.getCurrent()
                    .getSystemColor(SWT.COLOR_LIST_SELECTION));
        }
        scale.setEnabled(specifier.isEnabled());
        UiBuilder.ensureMouseWheelEventsPassedUpToAncestor(scale);
        GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gridData.horizontalSpan = 2;
        gridData.exclude = (specifier.isShowScale() == false);
        scale.setLayoutData(gridData);
    }

    /**
     * Get the visible time delta.
     * 
     * @return Visible time delta.
     */
    private long getVisibleTimeDelta() {
        return ruler.getUpperVisibleValue() + 1L - ruler.getLowerVisibleValue();
    }

    /**
     * Change the component widgets to ensure their state matches that of the
     * editable flag.
     * 
     * @param editable
     *            Flag indicating whether the component widgets are to be
     *            editable or read-only.
     */
    private void doSetEditable(boolean editable) {
        for (String identifier : timeComponentsForIds.keySet()) {
            timeComponentsForIds.get(identifier).setEditable(
                    (editable == false ? false
                            : editabilityForIds.get(identifier)), helper);
        }
        Map<String, Integer> indicesForIds = ((MultiTimeMegawidgetSpecifier) getSpecifier())
                .getIndicesForStateIdentifiers();
        for (String identifier : indicesForIds.keySet()) {
            scale.setConstrainedThumbEditable(indicesForIds.get(identifier),
                    (editable && isScaleThumbEditable(identifier)));
        }
    }

    /**
     * Determine whether or not the specified state's thumb in the scale should
     * be editable. It is assumed that the megawidget as a whole is editable.
     * 
     * @param identifier
     *            State identifier associated with the scale thumb that is being
     *            checked for editability.
     * @return True if the scale thumb should be editable, false otherwise.
     */
    private boolean isScaleThumbEditable(String identifier) {
        return (scale.isConstrainedThumbIntervalLocked() ? editabilityForIds
                .get(((IStatefulSpecifier) getSpecifier())
                        .getStateIdentifiers().get(0)) : editabilityForIds
                .get(identifier));
    }

    /**
     * Ensure that the states are ordered correctly, repositioning any that are
     * to the left of, or insufficiently far from, their previous neighbors.
     */
    private void ensureStatesAreOrderedCorrectly() {
        Long lastState = null;
        for (String identifier : ((IStatefulSpecifier) getSpecifier())
                .getStateIdentifiers()) {
            Long state = statesForIds.get(identifier);
            if ((lastState != null)
                    && (lastState + stateValidator.getMinimumInterval() > state)) {
                state = lastState + stateValidator.getMinimumInterval();
                statesForIds.put(identifier, state);
            }
            lastState = state;
        }
    }

    /**
     * Synchronize the component widgets to the boundaries for the states.
     */
    protected void synchronizeComponentWidgetsToBounds() {
        List<String> identifiers = ((IStatefulSpecifier) getSpecifier())
                .getStateIdentifiers();
        for (int j = 0; j < identifiers.size(); j++) {
            String identifier = identifiers.get(j);
            scale.setAllowableConstrainedValueRange(j,
                    stateValidator.getMinimumValue(identifier),
                    stateValidator.getMaximumValue(identifier));
        }
    }

    /**
     * Handle changes to the allowable minimums.
     */
    private void handleAllowableMinimumsChange() {

        /*
         * Iterate through the states, ensuring that each is within the new
         * boundaries, and moving any that are not.
         */
        List<String> identifiers = ((IStatefulSpecifier) getSpecifier())
                .getStateIdentifiers();
        for (String identifier : identifiers) {
            Long state = statesForIds.get(identifier);
            if ((state == null)
                    || (state < stateValidator.getMinimumValue(identifier))) {
                statesForIds.put(identifier,
                        stateValidator.getMinimumValue(identifier));
            }
        }

        cleanUpAfterBoundariesChange();
    }

    /**
     * Handle changes to the allowable maximums.
     */
    private void handleAllowableMaximumsChange() {

        /*
         * Iterate through the states, ensuring that each is within the new
         * boundaries, and moving any that are not.
         */
        List<String> identifiers = ((IStatefulSpecifier) getSpecifier())
                .getStateIdentifiers();
        for (String identifier : identifiers) {
            Long state = statesForIds.get(identifier);
            if ((state == null)
                    || (state > stateValidator.getMaximumValue(identifier))) {
                statesForIds.put(identifier,
                        stateValidator.getMaximumValue(identifier));
            }
        }

        cleanUpAfterBoundariesChange();
    }

    /**
     * Handle changes to the allowable ranges.
     */
    private void handleAllowableRangesChange() {

        /*
         * Iterate through the states, ensuring that each is within the new
         * boundaries, and moving any that are not.
         */
        List<String> identifiers = ((IStatefulSpecifier) getSpecifier())
                .getStateIdentifiers();
        for (String identifier : identifiers) {
            Long state = statesForIds.get(identifier);
            if ((state == null)
                    || (state < stateValidator.getMinimumValue(identifier))) {
                statesForIds.put(identifier,
                        stateValidator.getMinimumValue(identifier));
            } else if (state > stateValidator.getMaximumValue(identifier)) {
                statesForIds.put(identifier,
                        stateValidator.getMaximumValue(identifier));
            }
        }

        cleanUpAfterBoundariesChange();
    }

    /**
     * Clean up after boundary changes, and any state changes that occurred as a
     * result.
     */
    private void cleanUpAfterBoundariesChange() {

        /*
         * Make sure that any alterations made to the state above did not render
         * them out of order.
         */
        ensureStatesAreOrderedCorrectly();

        /*
         * Synchronize the widgets to the new boundaries and potentially new
         * states.
         */
        synchronizeComponentWidgetsToBounds();
        synchronizeComponentWidgetsToState();
    }
}
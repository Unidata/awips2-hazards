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

import gov.noaa.gsd.viz.widgets.IMultiValueLinearControlListener;
import gov.noaa.gsd.viz.widgets.ISnapValueCalculator;
import gov.noaa.gsd.viz.widgets.MultiValueLinearControl;
import gov.noaa.gsd.viz.widgets.MultiValueScale;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Range;
import com.google.common.collect.Ranges;
import com.google.common.collect.Sets;

/**
 * Time scale megawidget, providing the user the ability to select one or more
 * times.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Chris.Golden      Initial induction into repo
 * Apr 30, 2013   1277     Chris.Golden      Added support for mutable properties.
 * Oct 18, 2013   2168     Chris.Golden      Fixed bug that caused notification
 *                                           of state change to be fired before
 *                                           construction of the megawidget was
 *                                           complete, and changed to implement
 *                                           new IControl interface.
 * Nov 05, 2013   2336     Chris.Golden      Added option to not notify listeners
 *                                           of state changes caused by ongoing
 *                                           thumb drags.
 * Dec 16, 2013   2545     Chris.Golden      Changed to use new DateTimeComponent
 *                                           objects instead of text fields for
 *                                           viewing/manipulating each state
 *                                           value above the scale widget.
 * Jan 31, 2014   2710     Chris.Golden      Added minimum interval parameter, to
 *                                           allow the minimum interval between
 *                                           adjacent state values to be configured.
 *                                           Also changed to only send notifications
 *                                           of scale-caused changes after all the
 *                                           values have been recorded in the mega-
 *                                           widget to avoid bugs caused by only
 *                                           one value of N being updated when the
 *                                           state of all values is checked.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see TimeScaleSpecifier
 */
public class TimeScaleMegawidget extends ExplicitCommitStatefulMegawidget
        implements IControl {

    // Protected Static Constants

    /**
     * Set of all mutable property names for instances of this class.
     */
    protected static final Set<String> MUTABLE_PROPERTY_NAMES;
    static {
        Set<String> names = Sets
                .newHashSet(NotifierMegawidget.MUTABLE_PROPERTY_NAMES);
        names.add(IControlSpecifier.MEGAWIDGET_EDITABLE);
        names.add(TimeScaleSpecifier.MINIMUM_VISIBLE_TIME);
        names.add(TimeScaleSpecifier.MAXIMUM_VISIBLE_TIME);
        MUTABLE_PROPERTY_NAMES = ImmutableSet.copyOf(names);
    };

    // Private Static Constants

    /**
     * Number of milliseconds in a minute.
     */
    private static final long MINUTE_INTERVAL = TimeUnit.MINUTES.toMillis(1L);

    /**
     * Width in pixels of the time scale thumbs.
     */
    private static final int SCALE_THUMB_WIDTH = 13;

    /**
     * Height in pixels of the time scale thumbs.
     */
    private static final int SCALE_THUMB_HEIGHT = 21;

    /**
     * Thickness in pixels of the time scale tracks.
     */
    private static final int SCALE_TRACK_THICKNESS = 11;

    /**
     * Width of horizontal padding in pixels to the left and right of the scale
     * widget.
     */
    private static final int SCALE_HORIZONTAL_PADDING = 7;

    /**
     * Height of vertical padding in pixels above and below the scale widget.
     */
    private static final int SCALE_VERTICAL_PADDING_TOP = 1;

    /**
     * Height of vertical padding in pixels above and below the scale widget.
     */
    private static final int SCALE_VERTICAL_PADDING_BOTTOM = 6;

    /**
     * Snap value calculator, used to generate snap-to values for the scale
     * widget.
     */
    private static final ISnapValueCalculator SNAP_VALUE_CALCULATOR = new ISnapValueCalculator() {
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

    // Private Variables

    /**
     * Range of state that are allowed, from minimum to maximum.
     */
    private final Range<Long> bounds;

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
     * Copy of <code>statesForIds</code> made just before the first state change
     * resulting from a thumb drag is processed. This is used only if the
     * specifier indicates that rapidly-changing values resulting in state
     * changes should not prompt listener notifications.
     */
    private Map<String, Long> lastForwardedStatesForIds;

    /**
     * Main label of the megawidget, if any.
     */
    private final Label mainLabel;

    /**
     * List of labels created for the date-time components.
     */
    private final List<Label> labels;

    /**
     * Mapping of state identifier keys to date-time components as values.
     */
    private final Map<String, DateTimeComponent> dateTimesForIds;

    /**
     * Manager of the date-time components.
     */
    private final IDateTimeComponentHolder dateTimeManager = new IDateTimeComponentHolder() {
        @Override
        public long getCurrentTime() {
            return currentTimeProvider.getCurrentTime();
        }

        @Override
        public Range<Long> getAllowableRange(String identifier) {
            return bounds;
        }

        @Override
        public boolean isValueChangeAcceptable(String identifier, long value) {

            // Ensure that the new value is not too close to
            // or beyond a neighboring thumb's value.
            int index = ((TimeScaleSpecifier) getSpecifier())
                    .getIndicesForStateIdentifiers().get(identifier);
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

        @Override
        public void valueChanged(String identifier, long value,
                boolean rapidChange) {

            // If the value has changed and is accep-
            // table to the scale widget, use it as the
            // new value; otherwise, set the value of
            // the date-time component back to what the
            // scale had for this identifier.
            int index = ((TimeScaleSpecifier) getSpecifier())
                    .getIndicesForStateIdentifiers().get(identifier);
            long oldValue = scale.getConstrainedThumbValue(index);
            if (oldValue != value) {
                if (scale.setConstrainedThumbValue(index, value)) {
                    recordStateChange(identifier, value);
                } else {
                    dateTimesForIds.get(identifier).setState(oldValue);
                }
            }

            // Notify listeners if appropriate.
            if ((onlySendEndStateChanges == false) || (rapidChange == false)) {
                notifyListener(identifier, value);
                notifyListener();
            }
        }
    };

    /**
     * Multi-thumbed scale component.
     */
    private final MultiValueScale scale;

    /**
     * Flag indicating whether state changes that occur as a result of a thumb
     * drag should be forwarded or not.
     */
    private final boolean onlySendEndStateChanges;

    /**
     * Control component helper.
     */
    private final ControlComponentHelper helper;

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
     */
    protected TimeScaleMegawidget(TimeScaleSpecifier specifier,
            Composite parent, Map<String, Object> paramMap) {
        super(specifier, paramMap);
        helper = new ControlComponentHelper(specifier);

        // Create a panel in which to place the widgets.
        Composite panel = UiBuilder.buildComposite(parent, 1, SWT.NONE,
                UiBuilder.CompositeType.SINGLE_ROW, specifier);
        ((GridData) panel.getLayoutData()).verticalAlignment = SWT.TOP;

        // Add an overall label if one is specified and if
        // either multiple state identifiers exist, or only
        // one exists but it has its own label. If, however,
        // only one state identifier exists and it has no
        // label, use the main label in its place.
        String labelString = specifier.getLabel();
        List<String> stateIdentifiers = specifier.getStateIdentifiers();
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

        // Get the range of values allowed and the current
        // time provider.
        bounds = Ranges.closed(
                (Long) paramMap.get(TimeScaleSpecifier.MINIMUM_TIME),
                (Long) paramMap.get(TimeScaleSpecifier.MAXIMUM_TIME));
        ICurrentTimeProvider provider = (ICurrentTimeProvider) paramMap
                .get(TimeScaleSpecifier.CURRENT_TIME_PROVIDER);
        if (provider == null) {
            provider = TimeMegawidgetSpecifier.DEFAULT_CURRENT_TIME_PROVIDER;
        }
        currentTimeProvider = provider;

        // Compute the starting value(s) for the different
        // state identifiers, and create and populate the
        // state map with them.
        statesForIds = Maps.newHashMap();
        long[] startingValues = new long[specifier.getStateIdentifiers().size()];
        for (int j = 0; j < startingValues.length; j++) {
            startingValues[j] = (startingValues.length == 1 ? ((bounds
                    .upperEndpoint() - bounds.lowerEndpoint()) / 2L)
                    + bounds.lowerEndpoint() : bounds.lowerEndpoint()
                    + (specifier.getMinimumInterval() * j));
            statesForIds.put(specifier.getStateIdentifiers().get(j),
                    startingValues[j]);
        }

        // Iterate through the state identifiers, creating
        // date-time components for each.
        onlySendEndStateChanges = !specifier.isSendingEveryChange();
        dateTimesForIds = Maps.newHashMap();
        labels = Lists.newArrayList();
        for (String identifier : specifier.getStateIdentifiers()) {

            // Determine what text label, if any, to use
            // for this state identifier.
            String text;
            if (useMainLabelAsStateLabel) {
                useMainLabelAsStateLabel = false;
                text = labelString;
            } else {
                text = specifier.getStateLabel(identifier);
            }

            // Create the date-time component for this state
            // identifier.
            DateTimeComponent dateTime = new DateTimeComponent(identifier,
                    panel, text, specifier, statesForIds.get(identifier),
                    false, onlySendEndStateChanges, dateTimeManager);
            labels.add(dateTime.getLabel());

            // Add the date-time component to the map.
            dateTimesForIds.put(identifier, dateTime);
        }

        // Create the multi-thumbed scale component.
        scale = new MultiValueScale(panel, bounds.lowerEndpoint(),
                bounds.upperEndpoint());
        scale.setSnapValueCalculator(SNAP_VALUE_CALCULATOR);
        scale.setInsets(SCALE_HORIZONTAL_PADDING, SCALE_VERTICAL_PADDING_TOP,
                SCALE_HORIZONTAL_PADDING, SCALE_VERTICAL_PADDING_BOTTOM);
        scale.setComponentDimensions(SCALE_THUMB_WIDTH, SCALE_THUMB_HEIGHT,
                SCALE_TRACK_THICKNESS);
        scale.setVisibleValueRange(
                (Long) paramMap.get(TimeScaleSpecifier.MINIMUM_VISIBLE_TIME),
                (Long) paramMap.get(TimeScaleSpecifier.MAXIMUM_VISIBLE_TIME));
        scale.setMinimumDeltaBetweenConstrainedThumbs(specifier
                .getMinimumInterval());
        scale.setConstrainedThumbValues(startingValues);
        for (int j = 1; j < startingValues.length; j++) {
            scale.setConstrainedThumbRangeColor(j, Display.getCurrent()
                    .getSystemColor(SWT.COLOR_LIST_SELECTION));
        }
        scale.setEnabled(specifier.isEnabled());
        GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gridData.horizontalSpan = 2;
        gridData.verticalIndent = specifier.getSpacing();
        scale.setLayoutData(gridData);

        // Bind the scale component's value change
        // events to trigger a change in the record
        // of the state for the widget, and a change
        // in the corresponding text component.
        scale.addMultiValueLinearControlListener(new IMultiValueLinearControlListener() {
            @Override
            public void visibleValueRangeChanged(
                    MultiValueLinearControl widget, long lowerValue,
                    long upperValue, MultiValueLinearControl.ChangeSource source) {

                // No action.
            }

            @Override
            public void constrainedThumbValuesChanged(
                    MultiValueLinearControl widget, long[] values,
                    MultiValueLinearControl.ChangeSource source) {

                // If the change source is not user-
                // GUI interaction, do nothing
                if ((source != MultiValueLinearControl.ChangeSource.USER_GUI_INTERACTION_ONGOING)
                        && (source != MultiValueLinearControl.ChangeSource.USER_GUI_INTERACTION_COMPLETE)) {
                    return;
                }

                // If only ending state changes are
                // to result in notifications, and
                // this is the first of an ongoing set
                // of state changes, then copy the
                // state before this change is pro-
                // cessed.
                if (onlySendEndStateChanges
                        && (source == MultiValueLinearControl.ChangeSource.USER_GUI_INTERACTION_ONGOING)
                        && (lastForwardedStatesForIds == null)) {
                    lastForwardedStatesForIds = Maps.newHashMap(statesForIds);
                }

                // See if notification of listeners
                // should occur as the new values are
                // processed. If all state changes
                // are to result in notifications, or
                // if this is an ending state change
                // and no ongoing state changes
                // occurred beforehand, notification
                // should occur.
                boolean notify = (!onlySendEndStateChanges || ((lastForwardedStatesForIds == null) && (source == MultiValueLinearControl.ChangeSource.USER_GUI_INTERACTION_COMPLETE)));

                // Iterate through the thumbs, deter-
                // mining which have changed their
                // values and responding accordingly.
                // Any notification for these values
                // must occur after all the values
                // have been changed, to avoid having
                // a notification of one value change
                // go out that makes that value
                // higher than the next value, even
                // though once all values have been
                // set, they will be in proper as-
                // cending order.
                TimeScaleSpecifier specifier = getSpecifier();
                List<String> stateIdentifiers = specifier.getStateIdentifiers();
                List<Integer> indicesOfValuesToNotify = Lists.newArrayList();
                for (int j = 0; j < values.length; j++) {

                    // Get the new value and see if
                    // it has changed, and if so,
                    // make a note of the new value
                    // and note that it should be
                    // forwarded to any listeners if
                    // this is something that should
                    // be sent on.
                    String identifier = stateIdentifiers.get(j);
                    if ((statesForIds.get(identifier) == null)
                            || (values[j] != statesForIds.get(identifier))) {
                        statesForIds.put(identifier, values[j]);
                        TimeScaleMegawidget.this.dateTimesForIds
                                .get(identifier).setState(values[j]);
                        if (notify) {
                            indicesOfValuesToNotify.add(j);
                        }
                    }
                }
                for (int index : indicesOfValuesToNotify) {
                    notifyListener(stateIdentifiers.get(index), values[index]);
                }

                // If only ending state changes are
                // to result in notifications, this
                // is such a state change, and at
                // least one ongoing state change
                // occurred right before it, see if
                // the state is now different from
                // what it was before the preceding
                // set of ongoing state changes
                // occurred.
                if ((lastForwardedStatesForIds != null)
                        && (source == MultiValueLinearControl.ChangeSource.USER_GUI_INTERACTION_COMPLETE)) {

                    // Compare the current state
                    // values with the ones from
                    // before the ongoing state
                    // change set occurred; for
                    // each of these pairs that is
                    // different, send a notifica-
                    // tion that the corresponding
                    // state identifier's value has
                    // changed.
                    for (String identifier : stateIdentifiers) {
                        if (statesForIds.get(identifier).equals(
                                lastForwardedStatesForIds.get(identifier)) == false) {
                            notifyListener(identifier,
                                    statesForIds.get(identifier));
                        }
                    }

                    // Forget about the last forwarded
                    // states, as they are not needed
                    // unless another set of ongoing
                    // state changes occurs, in which
                    // case they will be recreated at
                    // that time.
                    lastForwardedStatesForIds = null;
                    notify = true;
                }

                // Notify listeners that invocation
                // occurred if this is something that
                // should be sent on.
                if (notify) {
                    notifyListener();
                }
            }

            @Override
            public void freeThumbValuesChanged(MultiValueLinearControl widget,
                    long[] values, MultiValueLinearControl.ChangeSource source) {

                // No action.
            }
        });

        // Render the widget uneditable if necessary.
        if (isEditable() == false) {
            doSetEditable(false);
        }
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
        } else if (name.equals(TimeScaleSpecifier.MINIMUM_VISIBLE_TIME)) {
            return getLowerVisibleTime();
        } else if (name.equals(TimeScaleSpecifier.MAXIMUM_VISIBLE_TIME)) {
            return getUpperVisibleTime();
        }
        return super.getMutableProperty(name);
    }

    @Override
    public void setMutableProperty(String name, Object value)
            throws MegawidgetPropertyException {
        if (name.equals(IControlSpecifier.MEGAWIDGET_EDITABLE)) {
            setEditable(getPropertyBooleanValueFromObject(value, name, null));
        } else if (name.equals(TimeScaleSpecifier.MINIMUM_VISIBLE_TIME)) {
            setVisibleTimeRange(
                    getPropertyLongValueFromObject(value, name, null),
                    getUpperVisibleTime());
        } else if (name.equals(TimeScaleSpecifier.MAXIMUM_VISIBLE_TIME)) {
            setVisibleTimeRange(getLowerVisibleTime(),
                    getPropertyLongValueFromObject(value, name, null));
        } else {
            super.setMutableProperty(name, value);
        }
    }

    @Override
    public void setMutableProperties(Map<String, Object> properties)
            throws MegawidgetPropertyException {

        // If the minimum or maximum visible times are being set, set them
        // first, ensuring that the two boundaries are set in the order that
        // will allow the set to occur without error (if they are allowable).
        Object minValueObj = properties
                .get(TimeScaleSpecifier.MINIMUM_VISIBLE_TIME);
        Object maxValueObj = properties
                .get(TimeScaleSpecifier.MAXIMUM_VISIBLE_TIME);
        if ((minValueObj != null) && (maxValueObj != null)) {
            if ((minValueObj instanceof Number) == false) {
                TimeScaleSpecifier specifier = getSpecifier();
                throw new MegawidgetPropertyException(
                        specifier.getIdentifier(),
                        (minValueObj instanceof Number ? TimeScaleSpecifier.MAXIMUM_VISIBLE_TIME
                                : TimeScaleSpecifier.MINIMUM_VISIBLE_TIME),
                        specifier.getType(),
                        (minValueObj instanceof Number ? maxValueObj
                                : minValueObj), "must be long integer");
            }
            if (((Number) minValueObj).longValue() >= scale
                    .getUpperVisibleValue()) {
                setMutableProperty(TimeScaleSpecifier.MAXIMUM_VISIBLE_TIME,
                        maxValueObj);
                setMutableProperty(TimeScaleSpecifier.MINIMUM_VISIBLE_TIME,
                        minValueObj);
            } else {
                setMutableProperty(TimeScaleSpecifier.MINIMUM_VISIBLE_TIME,
                        minValueObj);
                setMutableProperty(TimeScaleSpecifier.MAXIMUM_VISIBLE_TIME,
                        maxValueObj);
            }
        } else if (minValueObj != null) {
            setMutableProperty(TimeScaleSpecifier.MINIMUM_VISIBLE_TIME,
                    minValueObj);
        } else if (maxValueObj != null) {
            setMutableProperty(TimeScaleSpecifier.MAXIMUM_VISIBLE_TIME,
                    maxValueObj);
        }

        // Do what would have been done by the superclass method, except for
        // ignoring any minimum or maximum visible time setting, as that has
        // already been done above.
        for (String name : properties.keySet()) {
            if (!name.equals(TimeScaleSpecifier.MINIMUM_VISIBLE_TIME)
                    && !name.equals(TimeScaleSpecifier.MAXIMUM_VISIBLE_TIME)) {
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
        if ((labels != null) && (labels.size() > 0)) {
            helper.setWidgetsWidth(width,
                    labels.toArray(new Label[labels.size()]));
        }
    }

    @Override
    public final int getRightDecorationWidth() {
        return 0;
    }

    @Override
    public final void setRightDecorationWidth(int width) {

        // No action.
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
        scale.setVisibleValueRange(lower, upper);
    }

    // Protected Methods

    @Override
    protected final void doSetEnabled(boolean enable) {
        if (mainLabel != null) {
            mainLabel.setEnabled(enable);
        }
        for (DateTimeComponent dateTime : dateTimesForIds.values()) {
            dateTime.setEnabled(enable);
        }
        scale.setEnabled(enable);
    }

    @Override
    protected final Object doGetState(String identifier) {
        return statesForIds.get(identifier);
    }

    @Override
    protected final void doSetState(String identifier, Object state)
            throws MegawidgetStateException {

        // Get the value and ensure that it is appro-
        // priate.
        long value = getStateValue(identifier, state);

        // Record the change in the state records.
        recordStateChange(identifier, value);

        // Tell the scale widget and the date-time com-
        // ponent about the change.
        scale.setConstrainedThumbValue(((TimeScaleSpecifier) getSpecifier())
                .getIndicesForStateIdentifiers().get(identifier), value);
        dateTimesForIds.get(identifier).setState(value);
    }

    @Override
    protected final String doGetStateDescription(String identifier, Object state)
            throws MegawidgetStateException {
        return (state == null ? null : DateTimeComponent
                .getStateDescription(getStateLongValueFromObject(state,
                        identifier, null)));
    }

    @Override
    protected void ensureStateIsValid(String identifier, Object state)
            throws MegawidgetStateException {
        getStateValue(identifier, state);
    }

    @Override
    protected void doCommitStateChanges(Map<String, Object> newStatesForIds)
            throws MegawidgetStateException {

        // Commit the values to the state records, and
        // change the text widgets to match.
        TimeScaleSpecifier specifier = getSpecifier();
        for (String identifier : newStatesForIds.keySet()) {
            Long value = getStateLongObjectFromObject(
                    newStatesForIds.get(identifier), identifier, null);
            recordStateChange(identifier, value);
            dateTimesForIds.get(identifier).setState(value);
        }

        // Compile a list of values, one per thumb, us-
        // ing the new values where appropriate, and
        // the old values where no new values are given.
        List<String> stateIdentifiers = specifier.getStateIdentifiers();
        long[] values = new long[stateIdentifiers.size()];
        for (int j = 0; j < values.length; j++) {
            values[j] = (newStatesForIds.containsKey(stateIdentifiers.get(j)) ? statesForIds
                    .get(stateIdentifiers.get(j)) : scale
                    .getConstrainedThumbValue(j));
        }

        // Tell the scale about the new thumb values.
        try {
            scale.setConstrainedThumbValues(values);
        } catch (Exception e) {
            throw new MegawidgetStateException(specifier.getIdentifier(),
                    specifier.getType(), values, e.getMessage());
        }
    }

    // Private Methods

    /**
     * Change the component widgets to ensure their state matches that of the
     * editable flag.
     * 
     * @param editable
     *            Flag indicating whether the component widgets are to be
     *            editable or read-only.
     */
    private void doSetEditable(boolean editable) {
        for (DateTimeComponent dateTime : dateTimesForIds.values()) {
            dateTime.setEditable(editable, helper);
        }
        scale.setEditable(editable);
    }

    /**
     * Get a state value from the specified object.
     * 
     * @param identifier
     *            Identifier of the state being retrieved.
     * @param object
     *            Object from which to retrieve the state.
     * @return Value retrieved from the object.
     * @throws MegawidgetStateException
     *             If the object does not contain a valid state.
     */
    private long getStateValue(String identifier, Object object)
            throws MegawidgetStateException {

        // Ensure that the value is a long integer, and
        // that it falls within the allowable range.
        long minimum = scale.getMinimumAllowableValue();
        long value = getStateLongValueFromObject(object, identifier, minimum);
        if ((value < minimum) || (value > scale.getMaximumAllowableValue())) {
            throw new MegawidgetStateException(identifier, getSpecifier()
                    .getType(), value, "value out of bounds (minimum = "
                    + minimum + ", maximum = "
                    + scale.getMaximumAllowableValue() + " (inclusive))");
        }
        return value;
    }

    /**
     * Record the specified state change.
     * 
     * @param identifier
     *            State identifier to be changed.
     * @param value
     *            New value of the state associated with <code>identifier</code>
     */
    private void recordStateChange(String identifier, Long value) {
        statesForIds.put(identifier, value);
        if (lastForwardedStatesForIds != null) {
            lastForwardedStatesForIds.put(identifier, value);
        }
    }
}
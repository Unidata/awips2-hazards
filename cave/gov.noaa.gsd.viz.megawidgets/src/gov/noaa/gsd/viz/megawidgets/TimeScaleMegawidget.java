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

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
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
    private static final long MINUTE_INTERVAL = 60L * 1000L;

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

    /**
     * String that will generate the longest length in pixels when displayed in
     * a date text entry widget, used to calculate the required width for said
     * widgets.
     */
    private static final String SAMPLE_LONG_DATE_STRING = "00:00 Wed 00-May-0000";

    /**
     * Date formatter for shorter strings.
     */
    private static final SimpleDateFormat SHORT_DATE_FORMATTER = new SimpleDateFormat(
            "HH:mm dd-MM-yy");

    /**
     * Date formatter for longer strings.
     */
    private static final SimpleDateFormat LONG_DATE_FORMATTER = new SimpleDateFormat(
            "HH:mm EEE dd-MMM-yyyy");

    // Configure the date formatter.
    static {
        SHORT_DATE_FORMATTER.setTimeZone(TimeZone.getTimeZone("GMT"));
        LONG_DATE_FORMATTER.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    // Private Variables

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
     * Labels associated with this megawidget, if any.
     */
    private final List<Label> labels;

    /**
     * Map pairing state identifier keys with text components as values.
     */
    private final Map<String, Text> textsForIds;

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
     * Flag indicating whether or not a programmatic change to component values
     * is currently in process.
     */
    private boolean changeInProgress = false;

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

        // Create a panel in which to place the widgets. This
        // is needed in order to group the widgets properly into
        // a single megawidget.
        Composite panel = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(2, false);
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        gridLayout.horizontalSpacing = 10;
        panel.setLayout(gridLayout);
        GridData gridData = new GridData(SWT.FILL, SWT.TOP, true, false);
        gridData.horizontalSpan = specifier.getWidth();
        panel.setLayoutData(gridData);

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

                // Create a label widget.
                mainLabel = new Label(panel, SWT.NONE);
                mainLabel.setText(labelString);
                mainLabel.setEnabled(specifier.isEnabled());

                // Place the label in the grid.
                gridData = new GridData(SWT.LEFT, SWT.CENTER, false, false);
                gridData.horizontalSpan = 2;
                gridData.verticalIndent = specifier.getSpacing();
                mainLabel.setLayoutData(gridData);
            }
        } else {
            mainLabel = null;
        }

        // Iterate through the state identifiers, creating
        // components for each.
        textsForIds = Maps.newHashMap();
        labels = Lists.newArrayList();
        for (String identifier : specifier.getStateIdentifiers()) {

            // Create a label widget. If this is the only
            // state associated with this megawidget, and a
            // main label was specified but no state label
            // was, use the main label in place of the state
            // label.
            Label label = new Label(panel, SWT.NONE);
            if (useMainLabelAsStateLabel) {
                useMainLabelAsStateLabel = false;
                label.setText(labelString);
            } else if ((specifier.getStateLabel(identifier) != null)
                    && (specifier.getStateLabel(identifier).length() > 0)) {
                label.setText(specifier.getStateLabel(identifier));
            }
            label.setEnabled(specifier.isEnabled());

            // Place the label in the grid.
            gridData = new GridData(SWT.LEFT, SWT.CENTER, false, false);
            gridData.verticalIndent = specifier.getSpacing();
            label.setLayoutData(gridData);

            // Add the label to the list.
            labels.add(label);

            // Create a panel in which to place the state text
            // component. This is done so that said text compo-
            // nent may be rendered read-only by disabling the
            // panel, which in SWT has the effect of disabling
            // mouse and keyboard input for the child widgets
            // without making them look disabled.
            Composite subpanel = new Composite(panel, SWT.NONE);
            subpanel.setLayout(new FillLayout());
            gridData = new GridData(SWT.LEFT, SWT.CENTER, false, false);
            gridData.verticalIndent = specifier.getSpacing();
            subpanel.setLayoutData(gridData);

            // Create the text entry component.
            Text text = new Text(subpanel, SWT.BORDER);
            text.setEnabled(specifier.isEnabled());
            text.setText(SAMPLE_LONG_DATE_STRING);
            gridData.widthHint = text.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;

            // Add the text entry component to the map.
            textsForIds.put(identifier, text);
        }

        // Create the state map.
        this.statesForIds = Maps.newHashMap();

        // Create the multi-thumbed scale component.
        onlySendEndStateChanges = !specifier.isSendingEveryChange();
        scale = new MultiValueScale(panel,
                (Long) paramMap.get(TimeScaleSpecifier.MINIMUM_TIME),
                (Long) paramMap.get(TimeScaleSpecifier.MAXIMUM_TIME));
        scale.setSnapValueCalculator(SNAP_VALUE_CALCULATOR);
        scale.setInsets(SCALE_HORIZONTAL_PADDING, SCALE_VERTICAL_PADDING_TOP,
                SCALE_HORIZONTAL_PADDING, SCALE_VERTICAL_PADDING_BOTTOM);
        scale.setComponentDimensions(SCALE_THUMB_WIDTH, SCALE_THUMB_HEIGHT,
                SCALE_TRACK_THICKNESS);
        scale.setVisibleValueRange(
                (Long) paramMap.get(TimeScaleSpecifier.MINIMUM_VISIBLE_TIME),
                (Long) paramMap.get(TimeScaleSpecifier.MAXIMUM_VISIBLE_TIME));
        scale.setMinimumDeltaBetweenConstrainedThumbs(MINUTE_INTERVAL);
        long[] startingValues = new long[specifier.getStateIdentifiers().size()];
        for (int j = 0; j < startingValues.length; j++) {
            startingValues[j] = (startingValues.length == 1 ? (scale
                    .getMinimumAllowableValue() + scale
                    .getMaximumAllowableValue()) / 2L : scale
                    .getMinimumAllowableValue() + (MINUTE_INTERVAL * j));
            this.statesForIds.put(specifier.getStateIdentifiers().get(j),
                    startingValues[j]);
        }
        scale.setConstrainedThumbValues(startingValues);
        for (int j = 1; j < startingValues.length; j++) {
            scale.setConstrainedThumbRangeColor(j, Display.getCurrent()
                    .getSystemColor(SWT.COLOR_LIST_SELECTION));
        }
        scale.setEnabled(specifier.isEnabled());
        gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gridData.horizontalSpan = 2;
        gridData.verticalIndent = specifier.getSpacing();
        scale.setLayoutData(gridData);

        // Associate the state identifiers with the
        // components that go with them.
        for (String key : textsForIds.keySet()) {
            textsForIds.get(key).setData(key);
        }

        // Update the text fields to match the starting
        // values.
        this.updateTextEntryFieldsToMatchScaleValues();

        // Binding the scale component's value change
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

                // Set the flag indicating that a
                // user-GUI interactions has caused
                // a scale change to be in progress.
                changeInProgress = true;

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
                TimeScaleSpecifier specifier = getSpecifier();
                List<String> stateIdentifiers = specifier.getStateIdentifiers();

                for (int j = 0; j < values.length; j++) {

                    // Get the new value and see if
                    // it has changed, and if so,
                    // make a note of the new value
                    // and forward the change to any
                    // listeners if this is some-
                    // thing that should be sent on.
                    String identifier = stateIdentifiers.get(j);
                    if ((statesForIds.get(identifier) == null)
                            || (values[j] != statesForIds.get(identifier))) {
                        statesForIds.put(identifier, values[j]);
                        TimeScaleMegawidget.this.textsForIds.get(identifier)
                                .setText(formatAsDate(values[j], true));
                        if (notify) {
                            notifyListener(identifier, values[j]);
                        }
                    }
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

                // Reset the in-progress flag.
                changeInProgress = false;

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

        // Iterate through the state identifiers,
        // setting up text bindings for each of them.
        for (String identifier : (specifier).getStateIdentifiers()) {

            // Bind loss of focus and Enter keystrokes
            // for the text widget to verify that the
            // text comprises a valid date-time string
            // and, if it has changed, letting the
            // change percolate through the megawidget.
            textsForIds.get(identifier).addFocusListener(new FocusAdapter() {
                @Override
                public void focusLost(FocusEvent e) {
                    validateText((Text) e.widget);
                }
            });
            textsForIds.get(identifier).addSelectionListener(
                    new SelectionAdapter() {
                        @Override
                        public void widgetDefaultSelected(SelectionEvent e) {
                            validateText((Text) e.widget);
                        }
                    });
        }

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
        if (labels != null) {
            for (Label label : labels) {
                label.setEnabled(enable);
            }
        }
        for (Text text : textsForIds.values()) {
            text.setEnabled(enable);
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

        // Ensure the value is appropriate.
        TimeScaleSpecifier specifier = getSpecifier();
        long minimum = scale.getMinimumAllowableValue();
        long value = getStateLongValueFromObject(state, identifier, minimum);
        if ((value < minimum) || (value > scale.getMaximumAllowableValue())) {
            throw new MegawidgetStateException(identifier, specifier.getType(),
                    state, "value out of bounds (minimum = " + minimum
                            + ", maximum = " + scale.getMaximumAllowableValue()
                            + " (inclusive))");
        }

        // Set the flag indicating programmatic change
        // is in progress.
        changeInProgress = true;

        // Record the change in the state records.
        recordStateChange(identifier, value);

        // Tell the scale and the text widgets about the
        // change.
        scale.setConstrainedThumbValue(specifier
                .getIndicesForStateIdentifiers().get(identifier), value);
        textsForIds.get(identifier).setText(formatAsDate(value, true));

        // Reset the change in progress flag.
        changeInProgress = false;
    }

    @Override
    protected final String doGetStateDescription(String identifier, Object state)
            throws MegawidgetStateException {
        return (state == null ? null : formatAsDate(
                getStateLongValueFromObject(state, identifier, null), false));
    }

    @Override
    protected void ensureStateIsValid(String identifier, Object state)
            throws MegawidgetStateException {

        // Ensure that the value is a long integer, and
        // that it falls within the allowable range.
        long minimum = scale.getMinimumAllowableValue();
        long value = getStateLongValueFromObject(state, identifier, minimum);
        if ((value < minimum) || (value > scale.getMaximumAllowableValue())) {
            throw new MegawidgetStateException(identifier, getSpecifier()
                    .getType(), value, "out of bounds (minimum = " + minimum
                    + ", maximum = " + scale.getMaximumAllowableValue());
        }
    }

    @Override
    protected void doCommitStateChanges(Map<String, Object> newStatesForIds)
            throws MegawidgetStateException {

        // Indicate that programmatic change is in pro-
        // gress.
        changeInProgress = true;

        // Commit the values to the state records, and
        // change the text widgets to match.
        TimeScaleSpecifier specifier = getSpecifier();
        for (String identifier : newStatesForIds.keySet()) {
            Long value = getStateLongObjectFromObject(
                    newStatesForIds.get(identifier), identifier, null);
            recordStateChange(identifier, value);
            textsForIds.get(identifier).setText(formatAsDate(value, true));
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

        // Reset the change in progress flag.
        changeInProgress = false;
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
        for (Text text : textsForIds.values()) {
            text.getParent().setEnabled(editable);
            text.setBackground(helper
                    .getBackgroundColor(editable, text, ((labels == null)
                            || (labels.size() == 0) ? (mainLabel == null ? null
                            : mainLabel) : labels.get(0))));
        }
        scale.setEditable(editable);
    }

    /**
     * Format the specified epoch time in milliseconds as a date-time string.
     * 
     * @param time
     *            Time in milliseconds since the epoch.
     * @param full
     *            Flag indicating whether or not the formatting should produce a
     *            longer string, or one that is abbreviated.
     * @return Formatted date-time string.
     */
    private String formatAsDate(long time, boolean full) {
        return (full ? LONG_DATE_FORMATTER : SHORT_DATE_FORMATTER).format(time);
    }

    /**
     * Get the time in milliseconds since the epoch from the specified date-time
     * string.
     * 
     * @param dateTime
     *            Date-time string.
     * @return Time in milliseconds since the epoch if the string could be
     *         parsed; otherwise, <code>-1</code>.
     */
    private long parseDateTime(String dateTime) {
        ParsePosition parsePosition = new ParsePosition(0);
        Date date = LONG_DATE_FORMATTER.parse(dateTime, parsePosition);
        if ((date == null) || (date.getTime() < 0L)) {
            return -1;
        } else {
            return date.getTime();
        }
    }

    /**
     * Validate the text in the specified text widget, and set the corresponding
     * state to match if it is valid.
     * 
     * @param text
     *            Text widget whose contents are to be validated.
     */
    private void validateText(Text text) {

        // If a change that is in progress
        // inspired this event, do nothing.
        if (changeInProgress) {
            return;
        }

        // Get the date value for the date-
        // time string and, if valid, use
        // it as the new state value for
        // the appropriate identifier; if
        // invalid, reset the text to what
        // it was before.
        long value = parseDateTime(text.getText());
        String identifier = (String) text.getData();
        int index = ((TimeScaleSpecifier) getSpecifier())
                .getIndicesForStateIdentifiers().get(identifier);
        long oldValue = scale.getConstrainedThumbValue(index);
        if ((value != -1L) && (value != oldValue)
                && scale.setConstrainedThumbValue(index, value)) {

            // Remember this value for this
            // identifier.
            recordStateChange(identifier, value);

            // Set the text in this text
            // widget to this date/time.
            // This is done because the user
            // may have entered the date-
            // time in a non-canonical form,
            // and this ensures that it is
            // correct.
            text.setText(formatAsDate(value, true));
            notifyListener(identifier, value);
            notifyListener();
        } else {
            text.setText(formatAsDate(oldValue, true));
        }
    }

    /**
     * Update the text entry fields to match the current values.
     */
    private void updateTextEntryFieldsToMatchScaleValues() {

        // Set the flag indicating that the
        // scale value should not be validated.
        changeInProgress = true;

        // Iterate through the thumbs, setting
        // the text entry field for each to
        // the value of that thumb.
        TimeScaleSpecifier specifier = getSpecifier();
        List<String> stateIdentifiers = specifier.getStateIdentifiers();
        for (String stateIdentifier : stateIdentifiers) {
            textsForIds.get(stateIdentifier).setText(
                    formatAsDate(statesForIds.get(stateIdentifier), true));
        }

        // Reset the in-progress flag.
        changeInProgress = false;
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
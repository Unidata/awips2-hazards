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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.eclipse.nebula.widgets.cdatetime.CDT;
import org.eclipse.nebula.widgets.cdatetime.CDateTime;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;

import com.google.common.collect.Range;

/**
 * Description: Date-time component, providing an encapsulation of a single pair
 * of widgets, one allowing the viewing and manipulation of a date, the other a
 * time, to be used as part of time-oriented megawidgets.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Dec 16, 2013    2545    Chris.Golden      Initial creation
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class DateTimeComponent {

    // Private Static Constants

    /**
     * Formatter for date-time strings.
     */
    private static final SimpleDateFormat SHORT_DATE_FORMATTER = new SimpleDateFormat(
            "HH:mm dd-MM-yy");

    /**
     * Time zone.
     */
    private static final TimeZone TIME_ZONE = TimeZone.getTimeZone("GMT");

    // Configure the date-time formatter.
    static {
        SHORT_DATE_FORMATTER.setTimeZone(TIME_ZONE);
    }

    /**
     * Text to display for the date when no date-time has been chosen.
     */
    private static final String NULL_DATE_TEXT = "(no date)";

    /**
     * Text to display for the time when no date-time has been chosen.
     */
    private static final String NULL_TIME_TEXT = "--:--";

    /**
     * Formatter for date strings.
     */
    private static final String DATE_FORMAT = "dd-MMM-yyyy";

    /**
     * Formatter for time strings.
     */
    private static final String TIME_FORMAT = "HH:mm";

    // Private Classes

    /**
     * Date-time widget that correctly sets its component text widget's
     * background color when its background is set, and that forwards adding and
     * removal of key and mouse listeners to the appropriate component SWT
     * widgets.
     */
    private class DateTime extends CDateTime {

        // Public Constructors

        /**
         * Construct a standard instance.
         * 
         * @param parent
         *            Parent composite.
         * @param style
         *            Style.
         */
        public DateTime(Composite parent, int style) {
            super(parent, style);
        }

        // Public Methods

        @Override
        public Color getBackground() {
            return text.getControl().getBackground();
        }

        @Override
        public void setBackground(Color color) {
            super.setBackground(color);
            text.getControl().setBackground(color);
        }

        @Override
        public void addKeyListener(KeyListener listener) {

            // This listener is added to the component text field,
            // and then the text field's other listeners are re-
            // added, since the latter removes them and adds them
            // again, allowing the listener specified here to fire
            // before the built-in listeners do.
            text.getControl().addKeyListener(listener);
            addTextListener();
        }

        @Override
        public void removeKeyListener(KeyListener listener) {
            text.getControl().removeKeyListener(listener);
        }

        @Override
        public void addMouseListener(MouseListener listener) {
            Control spinner = getSpinner();
            if (spinner != null) {
                spinner.addMouseListener(listener);
            }
        }

        @Override
        public void removeMouseListener(MouseListener listener) {
            Control spinner = getSpinner();
            if (spinner != null) {
                spinner.removeMouseListener(listener);
            }
        }

        /**
         * Get a calendar instance with the specified date.
         * 
         * @param date
         *            Date to be used for the current time. If <code>null</code>
         *            the current time will be used instead.
         * @return Calendar instance.
         */
        @Override
        public Calendar getCalendarInstance(Date date) {
            long time = (date == null ? holder.getCurrentTime() : date
                    .getTime());
            Calendar calendar = Calendar.getInstance(TIME_ZONE,
                    Locale.getDefault());
            calendar.setTimeInMillis(time);
            return calendar;
        }

        /**
         * Get a calendar instance with the current time.
         * <p>
         * <b>Note</b>: this is a bit of a hack, since the superclass specifies
         * this method to allow the fetching of a calendar with the time
         * specified as the parameter, but since this method is only called with
         * the system current time as its parameter by Nebula classes, it is
         * safe to just ignore its parameter and simply return the current time.
         * This is done, in turn, so that the current time may be something
         * other than the system current time. Without this hack, the current
         * time would always be as provided by the system, which does not work
         * if a simulated time is being used as the "current" time.
         * </p>
         * 
         * @param timeMillis
         *            This parameter is ignored in this implementation.
         * @return Calendar instance set to the current time.
         */
        @Override
        public Calendar getCalendarInstance(long timeMillis) {
            return getCalendarInstance(null);
        }

        // Protected Methods

        @Override
        protected void postClose(Shell popup) {
            super.postClose(popup);
            notifyListenersOfEndingStateChange();
        }

        // Private Methods

        /**
         * Get the spinner that is a part of this widget, if any.
         * 
         * @return Spinner that is a part of this widget, or <code>null</code>
         *         if there is no such component.
         */
        private Control getSpinner() {

            // This roundabout way of getting the spinner is required so as
            // to allow access to what is a package-private object within
            // CDateTime. The alternative would be to make our own copy of
            // CDateTime and just make all the mods there, but that seemed
            // messier.
            Control[] children = panel.getComposite().getChildren();
            for (Control child : children) {
                if (child instanceof Spinner) {
                    return child;
                }
            }
            return null;
        }
    }

    // Private Variables

    /**
     * Identifier.
     */
    private final String identifier;

    /**
     * Current value in milliseconds since the epoch.
     */
    private long state;

    /**
     * Current delta between <code>state</code> and the value equivalent to
     * midnight on the same day as <code>state</code>, in milliseconds.
     */
    private long stateDeltaSinceMidnight;

    /**
     * Flag indicating whether state changes that occur as a result of a rapidly
     * repeatable action should be forwarded or not.
     */
    private final boolean onlySendEndStateChanges;

    /**
     * Last value that the state change listener knows about; this is used only
     * if <code>onlySendEndStateChanges</code> is true.
     */
    private long lastForwardedState;

    /**
     * Label of the megawidget, if any.
     */
    private final Label label;

    /**
     * Date selector.
     */
    private final DateTime date;

    /**
     * Time selector.
     */
    private final DateTime time;

    /**
     * Date object for the date widget that is reused to avoid constant
     * reallocation.
     */
    private final Date dateTimestamp = new Date();

    /**
     * Date object for the time widget that is reused to avoid constant
     * reallocation.
     */
    private final Date timeTimestamp = new Date();

    /**
     * Calendar object that is reused to avoid constant reallocation.
     */
    private final Calendar calendar = Calendar.getInstance();

    /**
     * Holder if this date-time component.
     */
    private final IDateTimeComponentHolder holder;

    /**
     * Flag indicating whether or not a rapid state change is in progress within
     * the text area of either the date widget or the time widget.
     */
    private boolean rapidStateChangeInTextFieldInProgress;

    // Public Static Methods

    /**
     * Get a description of the specified state.
     * 
     * @param state
     *            State to be rendered in descriptive string form.
     * @return Description in string form of the specified state.
     */
    public static String getStateDescription(long state) {
        return SHORT_DATE_FORMATTER.format(state);
    }

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param identifier
     *            Identifier of this date-time component.
     * @param parent
     *            Parent composite in which to locate the date-time component's
     *            widgets.
     * @param text
     *            Label text to be used, or <code>null</code> if no label is to
     *            be shown.
     * @param specifier
     *            Control specifier for the megawidget that is the holder of the
     *            this date-time component.
     * @param startingValue
     *            Starting value of the date-time component.
     * @param grabExcessHorizontalSpace
     *            Flag indicating whether or not the component's widgets should
     *            grab excess horizontal space.
     * @param onlySendEndStateChanges
     *            Flag indicating whether or not only end state changes should
     *            be sent along as notifications.
     * @param holder
     *            Holder of this date-time component.
     */
    public DateTimeComponent(String identifier, Composite parent, String text,
            IControlSpecifier specifier, long startingValue,
            boolean grabExcessHorizontalSpace, boolean onlySendEndStateChanges,
            IDateTimeComponentHolder holder) {
        this.identifier = identifier;
        this.holder = holder;
        lastForwardedState = state = startingValue;

        // Set up the calendar to be used for finding the
        // closest previous midnight of any given epoch time.
        calendar.setTimeZone(TIME_ZONE);

        // Create the composite holding the components, and
        // the label if appropriate. Remove any horizontal
        // spacing from the panel, as it would be too much
        // space between the date and time widgets; instead,
        // remember the spacing that was going to be used so
        // as to use it to the left of the date widget to
        // visually separate it from the label (if the label
        // is used).
        Composite panel = UiBuilder.buildComposite(parent, 3, SWT.NONE,
                UiBuilder.CompositeType.SINGLE_ROW, specifier);
        ((GridData) panel.getLayoutData()).grabExcessHorizontalSpace = grabExcessHorizontalSpace;
        GridLayout layout = (GridLayout) panel.getLayout();
        int spacingAfterLabel = layout.horizontalSpacing;
        layout.horizontalSpacing = 0;
        label = (text == null ? null : UiBuilder.buildLabel(panel, text,
                specifier));

        // Set up the timestamp trackers and determine
        // whether or not only ending state changes are
        // to be sent along to listeners.
        synchronizeTimestampTrackersToState();
        this.onlySendEndStateChanges = onlySendEndStateChanges;

        // Create the date selector.
        date = new DateTime(panel, CDT.BORDER | CDT.DROP_DOWN | CDT.TAB_FIELDS);
        date.setNullText(NULL_DATE_TEXT);
        date.setPattern(DATE_FORMAT);
        date.setTimeZone(TIME_ZONE);
        date.setSelection(dateTimestamp);
        date.setEnabled(specifier.isEnabled());

        // Place the date selector in the panel's grid.
        GridData gridData = new GridData(SWT.FILL, SWT.CENTER, false, false);
        gridData.horizontalSpan = (label == null ? 2 : 1);
        if (label != null) {
            gridData.horizontalIndent = spacingAfterLabel;
        }
        date.setLayoutData(gridData);

        // Create the time selector.
        time = new DateTime(panel, CDT.BORDER | CDT.SPINNER | CDT.TAB_FIELDS);
        time.setNullText(NULL_TIME_TEXT);
        time.setPattern(TIME_FORMAT);
        time.setTimeZone(TIME_ZONE);
        time.setSelection(timeTimestamp);
        time.setEnabled(specifier.isEnabled());

        // Place the time selector in the panel's grid.
        gridData = new GridData(SWT.FILL, SWT.CENTER, false, false);
        time.setLayoutData(gridData);

        // If only ending state changes are to result
        // in notifications, bind the date-time
        // increment and decrement key presses to indi-
        // cate that a rapid state change is starting,
        // and corresponding key releases to mean that
        // the rapid state change has ended, and that
        // any ending state change should result in a
        // notification for the state change listener.
        // Do the same for mouse presses and releases
        // on the time widget's Up and Down buttons.
        if (onlySendEndStateChanges) {
            KeyListener keyListener = new KeyListener() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (UiBuilder.isDateTimeValueChanger(e)) {
                        rapidStateChangeInTextFieldInProgress = true;
                    }
                }

                @Override
                public void keyReleased(KeyEvent e) {
                    if (UiBuilder.isDateTimeValueChanger(e)) {
                        rapidStateChangeInTextFieldInProgress = false;
                        notifyListenersOfEndingStateChange();
                    }
                }
            };
            date.addKeyListener(keyListener);
            time.addKeyListener(keyListener);
            time.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseDown(MouseEvent e) {
                    rapidStateChangeInTextFieldInProgress = true;
                }

                @Override
                public void mouseUp(MouseEvent e) {
                    rapidStateChangeInTextFieldInProgress = false;
                    notifyListenersOfEndingStateChange();
                }
            });
        }

        // Bind changes to the widgets to trigger state
        // changes and corresponding notifications, and
        // if only ending state changes are to result in
        // notifications of state changes, fire off such
        // a notification when the default selection
        // (Enter key) occurs.
        SelectionListener selectionListener = new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                DateTime widget = (DateTime) e.widget;
                long oldState = state;
                synchronizeStateToWidgets(widget);
                if (((DateTimeComponent.this.onlySendEndStateChanges == false) || isWidgetUndergoingRapidStateChange(widget))
                        && (oldState != state)) {
                    notifyListenersOfRapidStateChange();
                } else {
                    notifyListenersOfEndingStateChange();
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                notifyListenersOfEndingStateChange();
            }
        };
        date.addSelectionListener(selectionListener);
        time.addSelectionListener(selectionListener);
    }

    // Public Methods

    /**
     * Get the label, if any, for this date-time component.
     * 
     * @return Label for this date-time component, or <code>null</code> if there
     *         is no such label.
     */
    public Label getLabel() {
        return label;
    }

    // Protected Methods

    /**
     * Set the date-time component to be enabled or disabled.
     * 
     * @param enable
     *            Flag indicating whether or not the component should be
     *            enabled.
     */
    public void setEnabled(boolean enable) {
        if (label != null) {
            label.setEnabled(enable);
        }
        if ((enable == false) && date.isOpen()) {
            date.setOpen(false);
            notifyListenersOfEndingStateChange();
        }
        date.setEnabled(enable);
        time.setEnabled(enable);
    }

    /**
     * Set the date-time component to be editable or read-only.
     * 
     * @param editable
     *            Flag indicating whether the component widgets are to be
     *            editable or read-only.
     * @param helper
     *            Control component helper to be used to determine what
     *            background color is appropriate for the widgets' fields.
     */
    public void setEditable(boolean editable, ControlComponentHelper helper) {
        if ((editable == false) && date.isOpen()) {
            date.setOpen(false);
            notifyListenersOfEndingStateChange();
        }
        date.getParent().setEnabled(editable);
        Color color = helper.getBackgroundColor(editable, date, label);
        date.setBackground(color);
        time.setBackground(color);
    }

    /**
     * Set the specified value as the that held by the date-time component.
     * 
     * @param value
     *            Value to be held by the date-time component.
     */
    public void setState(long value) {

        // Record the state.
        state = value;
        recordLastNotifiedState();

        // Synchronize the timestamp trackers to the new state.
        synchronizeTimestampTrackersToState();

        // Set the date and time widgets to match the new state.
        date.setSelection(dateTimestamp);
        time.setSelection(timeTimestamp);
    }

    // Private Methods

    /**
     * Synchronize the timestamp trackers to the current state.
     */
    private void synchronizeTimestampTrackersToState() {
        stateDeltaSinceMidnight = getDeltaFromClosestPreviousMidnight(state);
        dateTimestamp.setTime(state - stateDeltaSinceMidnight);
        timeTimestamp.setTime(state);
    }

    /**
     * Synchronize the state to the current values of the widgets.
     * 
     * @param changed
     *            Widget that experienced a change in its value.
     */
    private void synchronizeStateToWidgets(DateTime changed) {

        // If the selection is now null, undo the change, as this
        // is not a valid selection.
        if (changed.getSelection() == null) {
            if (changed == date) {
                date.setSelection(dateTimestamp);
            } else {
                time.setSelection(timeTimestamp);
            }
            return;
        }

        // Remember the previous state in case the new state is
        // rejected.
        long oldState = state;

        // If the changed widget is the date widget, get its
        // value and add the delta between the previous date and
        // the value of the time widget to get the new state.
        // Otherwise, use the new value of the time widget for
        // the new state, and recalculate the delta between it
        // and the closest previous midnight.
        boolean synchTimeWidget = false, synchDateWidget = false;
        if (changed == date) {
            dateTimestamp.setTime(date.getSelection().getTime());
            state = dateTimestamp.getTime() + stateDeltaSinceMidnight;
            timeTimestamp.setTime(state);
            synchTimeWidget = true;
        } else {
            timeTimestamp.setTime(time.getSelection().getTime());
            state = timeTimestamp.getTime();
            stateDeltaSinceMidnight = getDeltaFromClosestPreviousMidnight(state);
            dateTimestamp.setTime(state - stateDeltaSinceMidnight);
        }

        // If the new state is out of bounds, move it in bounds
        // and recalculate the delta between it and the closest
        // previous midnight.
        Range<Long> bounds = holder.getAllowableRange(identifier);
        if (bounds.contains(state) == false) {
            state = (state < bounds.lowerEndpoint() ? bounds.lowerEndpoint()
                    : bounds.upperEndpoint());
            synchronizeTimestampTrackersToState();
            synchTimeWidget = true;
            if (date.getSelection().equals(dateTimestamp) == false) {
                synchDateWidget = true;
            }
        }

        // If the holder does not like the new state, restore
        // the old state.
        if (holder.isValueChangeAcceptable(identifier, state) == false) {
            state = oldState;
            synchronizeTimestampTrackersToState();
            synchDateWidget = (changed == date);
            synchTimeWidget = (changed == time);
        }

        // Set the date widget and/or time widget to match the
        // now-current state as necessary.
        if (synchDateWidget) {
            date.setSelection(dateTimestamp);
        }
        if (synchTimeWidget) {
            time.setSelection(timeTimestamp);
        }
    }

    /**
     * Get the delta in milliseconds between the specified value and the closest
     * previous midnight.
     * 
     * @param value
     *            Value for which the delta is to be found, as epoch time in
     *            milliseconds.
     * @return Delta in milliseconds.
     */
    private long getDeltaFromClosestPreviousMidnight(long value) {
        calendar.setTimeInMillis(value);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return value - calendar.getTimeInMillis();
    }

    /**
     * Record the current state as one of which the state change listener is
     * assumed to be aware.
     */
    private void recordLastNotifiedState() {
        lastForwardedState = state;
    }

    /**
     * Determine whether or not the specified widget is undergoing rapid state
     * change.
     * 
     * @param changed
     *            Widget that has experienced a state change.
     * @return True if the widget is undergoing rapid state change, otherwise
     *         false.
     */
    private boolean isWidgetUndergoingRapidStateChange(DateTime changed) {
        return (rapidStateChangeInTextFieldInProgress || ((changed == date) && date
                .isOpen()));
    }

    /**
     * Notify the state change and notification listeners of a state change that
     * is part of a set of rapidly-occurring changes if necessary.
     */
    private void notifyListenersOfRapidStateChange() {
        holder.valueChanged(identifier, state, true);
    }

    /**
     * Notify the state change and notification listeners of a state change if
     * the current state is not the same as the last state of which the state
     * change listener is assumed to be aware.
     */
    private void notifyListenersOfEndingStateChange() {
        if (onlySendEndStateChanges && (lastForwardedState != state)) {
            recordLastNotifiedState();
            holder.valueChanged(identifier, state, false);
        }
    }
}
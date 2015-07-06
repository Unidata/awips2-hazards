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

import gov.noaa.gsd.viz.megawidgets.validators.BoundedNumberValidator;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Spinner;

import com.google.common.collect.ImmutableSet;

/**
 * Spinner megawidget, allowing the manipulation of numbers.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 23, 2013   2168     Chris.Golden      Initial creation
 * Nov 04, 2013   2336     Chris.Golden      Changed to use multiple bounds on
 *                                           generic wildcard so that T extends
 *                                           both Number and Comparable. Also
 *                                           changed to offer option of not
 *                                           notifying listeners of state
 *                                           changes caused by ongoing thumb
 *                                           drags and spinner button presses.
 * Apr 24, 2014   2925     Chris.Golden      Changed to work with new validator
 *                                           package, updated Javadoc and other
 *                                           comments.
 * Jun 04, 2014   2155     Chris.Golden      Changed scale widget to snap to
 *                                           the current value when a mouse up
 *                                           occurs over it.
 * Jun 24, 2014   4010     Chris.Golden      Changed to no longer be a subclass
 *                                           of NotifierMegawidget.
 * Jul 22, 2014   4259     Chris.Golden      Fixed bug that caused negative
 *                                           values to not work correctly when
 *                                           the spinner included a scale bar
 *                                           below it. Also changed to ensure
 *                                           spinner text field is large enough
 *                                           to show all the characters of the
 *                                           largest possible string it could be
 *                                           called upon to display with its
 *                                           original minimum, maximum, and
 *                                           precision parameters.
 * Oct 20, 2014   4818     Chris.Golden      Changed to only stretch across the
 *                                           available horizontal space if it is
 *                                           configured to expand horizontally.
 *                                           If not, and if it is configured to
 *                                           show a scale widget, ensure that
 *                                           the scale bar is not too narrow,
 *                                           but do not stretch across all
 *                                           available space.
 * Oct 22, 2014   5050     Chris.Golden      Minor change: Used "or" instead of
 *                                           addition for SWT flags.
 * Feb 04, 2015   5919     Benjamin.Phillippe Added getRoundedValue function
 * Mar 31, 2015   6873     Chris.Golden      Added code to ensure that mouse
 *                                           wheel events are not processed by
 *                                           the megawidget, but are instead
 *                                           passed up to any ancestor that is a
 *                                           scrolled composite.
 * Jul 06, 2015   8413     mduff             Removed code to setLeftDecorationWidth.  This code
 *                                           does not properly handle widgets that are in multiple
 *                                           columns.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see SpinnerSpecifier
 */
public abstract class SpinnerMegawidget<T extends Number & Comparable<T>>
        extends BoundedValueMegawidget<T> implements IControl {

    // Protected Static Constants

    /**
     * Set of all mutable property names for instances of this class.
     */
    protected static final Set<String> MUTABLE_PROPERTY_NAMES;
    static {
        Set<String> names = new HashSet<>(
                BoundedValueMegawidget.MUTABLE_PROPERTY_NAMES);
        names.add(IControlSpecifier.MEGAWIDGET_EDITABLE);
        names.add(SpinnerSpecifier.MEGAWIDGET_INCREMENT_DELTA);
        MUTABLE_PROPERTY_NAMES = ImmutableSet.copyOf(names);
    };

    // Protected Variables

    /**
     * Label associated with this megawidget, if any.
     */
    private final Label label;

    /**
     * Spinner component associated with this megawidget.
     */
    private final Spinner spinner;

    /**
     * Scale component associated with this megawidget, if any.
     */
    private final Scale scale;

    /**
     * Integer offset added to the current value to get the scale value, since
     * the SWT {@link Scale} cannot handle negative values.
     */
    private int scaleOffset;

    /**
     * Flag indicating whether state changes that occur as a result of a spinner
     * button press or directional key press, or ongoing scale drag or
     * directional key press, should be forwarded or not.
     */
    private final boolean onlySendEndStateChanges;

    /**
     * Flag indicating whether or not a programmatic change to component values
     * is currently in process.
     */
    private boolean changeInProgress = false;

    /**
     * Last spinner value that the state change listener knows about.
     */
    private int lastForwardedValue;

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
    protected SpinnerMegawidget(SpinnerSpecifier<T> specifier,
            Composite parent, Map<String, Object> paramMap) {
        super(specifier, paramMap);
        helper = new ControlComponentHelper(specifier);

        /*
         * Create the composite holding the components, and the label if
         * appropriate.
         */
        Composite panel = UiBuilder.buildComposite(parent, 2, SWT.NONE,
                UiBuilder.CompositeType.SINGLE_ROW, specifier);
        ((GridData) panel.getLayoutData()).grabExcessHorizontalSpace = specifier
                .isHorizontalExpander();
        if (specifier.isHorizontalExpander() == false) {
            ((GridData) panel.getLayoutData()).horizontalAlignment = SWT.LEFT;
        }
        label = UiBuilder.buildLabel(panel, specifier);

        /*
         * Create the spinner. The maximum must be set twice to bogus values in
         * order to calculate how many pixels are needed per digit, so that the
         * minimum width may be figured below.
         */
        onlySendEndStateChanges = !specifier.isSendingEveryChange();
        spinner = new Spinner(panel, SWT.BORDER | SWT.WRAP);
        spinner.setMaximum(9);
        int oneDigitSpinnerWidthPixels = spinner.computeSize(SWT.DEFAULT,
                SWT.DEFAULT).x;
        spinner.setMaximum(99);
        int digitWidthPixels = spinner.computeSize(SWT.DEFAULT, SWT.DEFAULT).x
                - oneDigitSpinnerWidthPixels;
        int maxNumCharacters = Math.max(
                getDigitsForValue(specifier.getMinimumValue()),
                getDigitsForValue(specifier.getMaximumValue()));
        spinner.setTextLimit(maxNumCharacters);
        spinner.setMinimum(convertValueToSpinner(specifier.getMinimumValue()));
        spinner.setMaximum(convertValueToSpinner(specifier.getMaximumValue()));
        T incrementDelta = specifier.getIncrementDelta();
        spinner.setIncrement(convertValueToSpinner(incrementDelta));
        spinner.setPageIncrement(convertValueToSpinner(incrementDelta));
        spinner.setDigits(getSpinnerPrecision());
        spinner.setEnabled(specifier.isEnabled());
        UiBuilder.ensureMouseWheelEventsPassedUpToAncestor(spinner);

        /*
         * Place the spinner in the parent's grid.
         */
        GridData gridData = new GridData((specifier.isHorizontalExpander()
                || specifier.isShowScale() ? SWT.FILL : SWT.LEFT), SWT.CENTER,
                true, false);
        gridData.horizontalSpan = (label == null ? 2 : 1);
        gridData.minimumWidth = oneDigitSpinnerWidthPixels
                + ((maxNumCharacters - 1) * digitWidthPixels);
        spinner.setLayoutData(gridData);

        /*
         * Add a scale, if one is desired.
         */
        if (specifier.isShowScale()) {

            /*
             * Create the scale.
             */
            scale = new Scale(panel, SWT.HORIZONTAL);
            setScaleBounds(convertValueToSpinner(specifier.getMinimumValue()),
                    convertValueToSpinner(specifier.getMaximumValue()));
            scale.setPageIncrement(convertValueToSpinner(incrementDelta));
            scale.setIncrement(convertValueToSpinner(incrementDelta));
            scale.setEnabled(specifier.isEnabled());
            UiBuilder.ensureMouseWheelEventsPassedUpToAncestor(scale);

            /*
             * Place the spinner in the parent's grid.
             */
            gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
            gridData.horizontalSpan = 2;
            gridData.minimumWidth = 10 * digitWidthPixels;
            scale.setLayoutData(gridData);
        } else {
            scale = null;
        }

        /*
         * If only ending state changes are to result in notifications, bind
         * spinner focus loss to trigger a notification if the value has changed
         * in such a way that the state change listener was not notified. Do the
         * same for key up and mouse up events, so that when the user presses
         * and holds a directional key (arrow up or down, etc.) to change the
         * value, or presses and holds one of the spinner buttons with the
         * mouse, the state change will result in a notification after the key
         * or mouse is released.
         */
        if (onlySendEndStateChanges) {
            spinner.addFocusListener(new FocusAdapter() {
                @Override
                public void focusLost(FocusEvent e) {
                    notifyListenersOfEndingStateChange();
                }
            });
            spinner.addKeyListener(new KeyAdapter() {
                @Override
                public void keyReleased(KeyEvent e) {
                    if (UiBuilder.isSpinnerValueChanger(e)) {
                        notifyListenersOfEndingStateChange();
                    }
                }
            });
            spinner.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseUp(MouseEvent e) {
                    notifyListenersOfEndingStateChange();
                }
            });
        }

        /*
         * Bind the spinner selection event to trigger a change in the state,
         * and to alter the scale's value if one is present.
         */
        spinner.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {

                /*
                 * If this is a result of a programmatic change, do nothing with
                 * it.
                 */
                if (changeInProgress) {
                    return;
                }

                /*
                 * Indicate that a change is in progress.
                 */
                changeInProgress = true;

                /*
                 * If the state is changing, make a record f the change and
                 * alter the scale, if any, to match.
                 */
                int state = SpinnerMegawidget.this.spinner.getSelection();
                if ((SpinnerMegawidget.this.state == null)
                        || (state != convertValueToSpinner(SpinnerMegawidget.this.state))) {
                    SpinnerMegawidget.this.state = convertSpinnerToValue(state);
                    if (SpinnerMegawidget.this.scale != null) {
                        SpinnerMegawidget.this.scale.setSelection(scaleOffset
                                + state);
                    }
                    SpinnerMegawidget.this.spinner.update();
                    notifyListenersOfRapidStateChange();
                }

                /*
                 * Reset the in-progress flag.
                 */
                changeInProgress = false;
            }
        });

        /*
         * If a scale is supplied, add listeners to it.
         */
        if (scale != null) {

            /*
             * If only ending state changes are to result in notifications, bind
             * scale focus loss to trigger a notification if the value has
             * changed in such a way that the state change listener was not
             * notified. Do the same for key up and mouse up events, so that
             * when the user presses and holds a key to change the value, or
             * drags the thumb with the mouse, the state change will result in a
             * notification after the key or mouse is released. Regardless of
             * whether or not the above is done, any mouse up for the scale is
             * bound to snap the scale handle to the current value.
             */
            if (onlySendEndStateChanges) {
                scale.addFocusListener(new FocusAdapter() {
                    @Override
                    public void focusLost(FocusEvent e) {
                        notifyListenersOfEndingStateChange();
                    }
                });
                scale.addKeyListener(new KeyAdapter() {
                    @Override
                    public void keyReleased(KeyEvent e) {
                        notifyListenersOfEndingStateChange();
                    }
                });
                scale.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseUp(MouseEvent e) {
                        snapScaleToCurrentState();
                        notifyListenersOfEndingStateChange();
                    }
                });
            } else {
                scale.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseUp(MouseEvent e) {
                        snapScaleToCurrentState();
                    }
                });
            }

            /*
             * Bind the scale selection event to trigger a change in the state,
             * and to alter the spinner's value as well.
             */
            scale.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {

                    /*
                     * If this is a result of a programmatic change, do nothing
                     * with it.
                     */
                    if (changeInProgress) {
                        return;
                    }

                    /*
                     * Indicate that a change is in progress.
                     */
                    changeInProgress = true;

                    /*
                     * If the state is changing, make a record of the change and
                     * alter the spinner to match.
                     */
                    int state = getRoundedScaleValue();
                    if ((SpinnerMegawidget.this.state == null)
                            || (state != convertValueToSpinner(SpinnerMegawidget.this.state))) {
                        SpinnerMegawidget.this.state = convertSpinnerToValue(state);
                        SpinnerMegawidget.this.spinner.setSelection(state);
                        notifyListenersOfRapidStateChange();
                    }

                    /*
                     * Reset the in-progress flag.
                     */
                    changeInProgress = false;
                }
            });
        }

        /*
         * Render the spinner uneditable if necessary.
         */
        if (isEditable() == false) {
            doSetEditable(false);
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
        } else if (name.equals(SpinnerSpecifier.MEGAWIDGET_INCREMENT_DELTA)) {
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
        } else if (name.equals(SpinnerSpecifier.MEGAWIDGET_INCREMENT_DELTA)) {
            ((BoundedNumberValidator<T>) getStateValidator())
                    .setIncrementDelta(value);
        } else {
            super.setMutableProperty(name, value);
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
        return (label == null ? 0 : helper.getWidestWidgetWidth(label));
    }

    @Override
    public void setLeftDecorationWidth(int width) {
        /*
         * TODO RM 8413 - Turning this off since it does not handle megawidgets
         * in different columns correctly. It is cutting off widgets in the
         * right column. Will revisit this when more time is available.
         */
        // if (label != null) {
        // helper.setWidgetsWidth(width, label);
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
     * Get the increment delta.
     * 
     * @return Increment delta.
     */
    public final T getIncrementDelta() {
        return ((BoundedNumberValidator<T>) getStateValidator())
                .getIncrementDelta();
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
        ((BoundedNumberValidator<T>) getStateValidator())
                .setIncrementDelta(value);
        T incrementDelta = ((BoundedNumberValidator<T>) getStateValidator())
                .getIncrementDelta();
        int spinnerDelta = convertValueToSpinner(incrementDelta);
        spinner.setPageIncrement(spinnerDelta);
        if (scale != null) {
            scale.setPageIncrement(spinnerDelta);
        }
    }

    // Protected Methods

    @Override
    protected final void synchronizeComponentWidgetsToBounds() {
        int minValue = convertValueToSpinner(getMinimumValue());
        int maxValue = convertValueToSpinner(getMaximumValue());
        spinner.setMinimum(minValue);
        spinner.setMaximum(maxValue);
        if (scale != null) {
            setScaleBounds(convertValueToSpinner(getMinimumValue()),
                    convertValueToSpinner(getMaximumValue()));
        }
        spinner.setTextLimit(Math.max(getDigitsForValue(getMinimumValue()),
                getDigitsForValue(getMaximumValue())));
    }

    @Override
    protected final void doSynchronizeComponentWidgetsToState() {
        spinner.setSelection(convertValueToSpinner(state));
        if (scale != null) {
            scale.setSelection(convertValueToSpinner(state) + scaleOffset);
        }
    }

    @Override
    protected final void doSetEnabled(boolean enable) {
        if (label != null) {
            label.setEnabled(enable);
        }
        spinner.setEnabled(enable);
        if (scale != null) {
            scale.setEnabled(enable);
        }
    }

    @Override
    protected void doSetState(String identifier, Object state)
            throws MegawidgetStateException {
        super.doSetState(identifier, state);
        recordLastNotifiedState();
    }

    /**
     * Get the precision for the spinner, that is, the number of decimal places
     * that should come after a decimal point.
     * 
     * @return Non-negative number indicating the precision; if <code>0</code>,
     *         no decimal point will be shown.
     */
    protected abstract int getSpinnerPrecision();

    /**
     * Get the number of characters that are required to show the specified
     * value.
     * 
     * @param value
     *            Value to be shown.
     * @return Number of characters required to show the specified number.
     */
    protected abstract int getDigitsForValue(T value);

    /**
     * Convert the specified megawidget state value to the integer needed to
     * represent said value in the spinner and/or scale widgets.
     * 
     * @param value
     *            Value to be converted.
     * @return Integer equivalent for the spinner and/or scale widgets.
     */
    protected abstract int convertValueToSpinner(T value);

    /**
     * Convert the specified spinner or scale integer to the corresponding
     * megawidget state value.
     * 
     * @param value
     *            Value to be converted.
     * @return Megawidget state value equivalent.
     */
    protected abstract T convertSpinnerToValue(int value);

    // Private Methods

    /**
     * Set the scale's boundaries to those specified.
     * 
     * @param minimumValue
     *            Minimum value to be used.
     * @param maximumValue
     *            Maximum value to be used.
     */
    private void setScaleBounds(int minimumValue, int maximumValue) {
        scaleOffset = (minimumValue < 0 ? -minimumValue : 0);
        scale.setMinimum(scaleOffset + minimumValue);
        scale.setMaximum(scaleOffset + maximumValue);
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
        spinner.getParent().setEnabled(editable);
        spinner.setBackground(helper.getBackgroundColor(editable, spinner,
                label));
    }

    /**
     * Snap the scale to the current state value.
     */
    private void snapScaleToCurrentState() {
        int state = scale.getSelection();
        scale.setSelection(state == scale.getMinimum() ? scale.getMaximum()
                : scale.getMinimum());
        scale.setSelection(state);
    }

    /**
     * Gets the value of the scale rounded to the nearest increment
     * 
     * @return The rounded value
     */
    private int getRoundedScaleValue() {
        double currentState = SpinnerMegawidget.this.scale.getSelection()
                - scaleOffset;
        double increment = scale.getIncrement();

        /*
         * Snaps the scale widget value to the current increment
         */
        double lower = (currentState - currentState % increment);
        double upper = (lower + increment);
        double mid = (upper - lower) / 2 + lower;
        return (int) (currentState >= mid ? upper : lower);
    }

    /**
     * Record the current state as one of which the state change listener is
     * assumed to be aware.
     */
    private void recordLastNotifiedState() {
        lastForwardedValue = spinner.getSelection();
    }

    /**
     * Notify the state change and notification listeners of a state change that
     * is part of a set of rapidly-occurring changes if necessary.
     */
    private void notifyListenersOfRapidStateChange() {
        if (onlySendEndStateChanges == false) {
            notifyListeners();
        }
    }

    /**
     * Notify the state change and notification listeners of a state change if
     * the current state is not the same as the last state of which the state
     * change listener is assumed to be aware.
     */
    private void notifyListenersOfEndingStateChange() {
        if (lastForwardedValue != spinner.getSelection()) {
            recordLastNotifiedState();
            notifyListeners();
        }
    }

    /**
     * Notify listeners of a state change.
     */
    private void notifyListeners() {
        notifyListener(getSpecifier().getIdentifier(),
                SpinnerMegawidget.this.state);
    }
}
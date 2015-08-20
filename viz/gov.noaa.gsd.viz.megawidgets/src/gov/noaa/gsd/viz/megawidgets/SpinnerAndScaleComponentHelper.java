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
import gov.noaa.gsd.viz.widgets.MultiValueLinearControl;
import gov.noaa.gsd.viz.widgets.MultiValueScale;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;

import com.google.common.collect.Range;

/**
 * Description: Helper class for handling some of the grunt work of creating and
 * configuring megawidgets containing both {@link Spinner} and
 * {@link MultiValueScale} components. The generic parameter <code>T</code>
 * specifies the type of value to be manipulated.
 * <p>
 * This class is used by instantiating it, then invoking, in order,
 * {@link #setHolder(ISpinnerAndScaleComponentHolder)},
 * {@link #buildParentPanelAndLabel(Composite, ISingleLineSpecifier)}, and then
 * {@link #buildSpinner(ISingleLineSpecifier, Number, Number, Number, boolean, String, boolean)}
 * one or more times, one per state identifier, to create spinners for each such
 * state, with invocations of
 * {@link #buildJoiningLabel(ISingleLineSpecifier, String, String)} interleaved
 * between each call to <code>buildSpinner()</code>, and finally
 * {@link #buildScale(ISingleLineSpecifier, Number, Number, Number, boolean, int)}
 * . This process constructs all the widget components and gets them ready to be
 * managed. Other methods are provided to allow the components' states, value
 * boundaries, and so on to be manipulated after creation in order to keep them
 * synchronized with the enclosing megawidget.
 * </p>
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 31, 2015   4123     Chris.Golden      Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public abstract class SpinnerAndScaleComponentHelper<T extends Number & Comparable<T>> {

    // Protected Classes

    /**
     * Spinner listener, used by subclasses to listen for changes made by the
     * user to a spinner component.
     */
    protected class SpinnerListener extends SelectionAdapter {

        // Private Variables

        /**
         * Identifier of the state associated with the spinner.
         */
        private final String identifier;

        /**
         * Construct a standard instance.
         * 
         * @param identifier
         *            Identifier associated with this spinner.
         */
        public SpinnerListener(String identifier) {
            this.identifier = identifier;
        }

        // Public Methods

        @Override
        public void widgetSelected(SelectionEvent e) {

            /*
             * If this is a result of a programmatic change, do nothing with it.
             */
            if (changeInProgress) {
                return;
            }
            Spinner spinner = (Spinner) e.widget;

            /*
             * Indicate that a change is in progress.
             */
            changeInProgress = true;

            /*
             * If only ending state changes are to result in notifications, and
             * this is the first of an ongoing set of state changes, then copy
             * the state before this change is processed.
             */
            if (onlySendEndStateChanges
                    && (isLastForwardedStateRecorded() == false)) {
                recordLastForwardedState();
            }

            /*
             * If the state is changing, make a record of the change and alter
             * the scale, if any, to match. Then send a notification to the
             * holder if appropriate.
             */
            T value = convertSpinnerToValue(spinner.getSelection());
            List<String> identifiersOfChangedStates = null;
            if (value.equals(holder.getState(identifier)) == false) {
                identifiersOfChangedStates = holder.setState(identifier, value);
                spinner.update();
            }
            synchronizeComponentWidgetsToState();
            if ((onlySendEndStateChanges == false)
                    && (identifiersOfChangedStates != null)) {
                holder.notifyListener(identifiersOfChangedStates);
            }

            /*
             * Reset the in-progress flag.
             */
            changeInProgress = false;
        }
    }

    /**
     * Multi-value scale listener, used by subclasses to listen for changes made
     * by the user to the scale component, if any is showing.
     */
    protected class ScaleListener implements IMultiValueLinearControlListener {

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
                    && (isLastForwardedStateRecorded() == false)) {
                recordLastForwardedState();
            }

            /*
             * See if notification of listeners should occur as the new values
             * are processed. If all state changes are to result in
             * notifications, or if this is an ending state change and no
             * ongoing state changes occurred beforehand, notification should
             * occur.
             */
            boolean notify = (!onlySendEndStateChanges || ((isLastForwardedStateRecorded() == false) && (source == MultiValueLinearControl.ChangeSource.USER_GUI_INTERACTION_COMPLETE)));

            /*
             * Determine what state has changed and handle the updating of the
             * spinner widget(s) required to keep them synchronized, as well as
             * notifying any listener if appropriate of such changes.
             */
            Map<String, T> newValuesForIdentifiers = new HashMap<>(
                    values.length, 1.0f);
            for (int j = 0; j < values.length; j++) {
                newValuesForIdentifiers.put(stateIdentifiers.get(j),
                        convertScaleToValue(values[j]));
            }
            List<String> identifiersOfChangedStates = holder
                    .setStates(newValuesForIdentifiers);
            synchronizeComponentWidgetsToState();
            if (notify && (identifiersOfChangedStates != null)) {
                holder.notifyListener(identifiersOfChangedStates);
            }

            /*
             * If only ending state changes are to result in notifications, this
             * is such a state change, and at least one ongoing state change
             * occurred right before it, notify the listener of any state change
             * since the last notification.
             */
            if (isLastForwardedStateRecorded()
                    && (source == MultiValueLinearControl.ChangeSource.USER_GUI_INTERACTION_COMPLETE)) {
                notifyListenerOfEndingStateChange();
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
     * State identifiers for the state being represented by these component
     * widgets.
     */
    private final List<String> stateIdentifiers;

    /**
     * Holder of this helper's component widgets.
     */
    private ISpinnerAndScaleComponentHolder<T> holder;

    /**
     * Panel containing the widgets.
     */
    private Composite panel;

    /**
     * Label associated with this megawidget, if any.
     */
    private Label label;

    /**
     * Map of state identifiers to their corresponding labels, meaning in each
     * case the label to the left of the spinner for that state value, if any.
     */
    private final Map<String, Label> labelsForStateIdentifiers = new HashMap<>();

    /**
     * Map of state identifiers to their corresponding spinners.
     */
    private final Map<String, Spinner> spinnersForStateIdentifiers = new HashMap<>();

    /**
     * Scale component associated with this megawidget, if any.
     */
    private MultiValueScale scale;

    /**
     * Flag indicating whether state changes that occur as a result of a spinner
     * button press or directional key press, or ongoing scale drag or
     * directional key press, should be forwarded or not.
     */
    private final boolean onlySendEndStateChanges;

    /**
     * Focus listener for all spinners.
     */
    private final FocusListener spinnerFocusListener;

    /**
     * Focus listener for all spinners.
     */
    private final KeyListener spinnerKeyListener;

    /**
     * Focus listener for all spinners.
     */
    private final MouseListener spinnerMouseListener;

    /**
     * Width of a digit in the spinner in pixels.
     */
    private int digitWidthPixels;

    /**
     * Flag indicating whether or not a programmatic change to component values
     * is currently in process.
     */
    private boolean changeInProgress = false;

    /**
     * Map of state identifiers to their values that is saved prior to any rapid
     * state change for megawidgets that are not to report such rapid changes,
     * so that the last forwarded (to a listener) state of the enclosing
     * megawidget may be compared after an ending state change.
     */
    private Map<String, T> lastForwardedStatesForIdentifiers;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param specifier
     *            Specifier for the megawidget to be built.
     * @param stateIdentifiers
     *            List of state identifiers for the megawidget to be built.
     * @param parent
     *            Parent of the megawidget.
     */
    public SpinnerAndScaleComponentHelper(
            IRapidlyChangingStatefulSpecifier specifier,
            List<String> stateIdentifiers, Composite parent) {
        this.stateIdentifiers = stateIdentifiers;

        /*
         * Determine whether or not the megawidget is to only send end state
         * change notifications to state change listeners. If so, create various
         * listeners to be attached to any spinners that are created.
         */
        onlySendEndStateChanges = !specifier.isSendingEveryChange();
        if (onlySendEndStateChanges) {
            spinnerFocusListener = new FocusAdapter() {
                @Override
                public void focusLost(FocusEvent e) {
                    notifyListenerOfEndingStateChange();
                }
            };
            spinnerKeyListener = new KeyAdapter() {
                @Override
                public void keyReleased(KeyEvent e) {
                    if (UiBuilder.isSpinnerValueChanger(e)) {
                        notifyListenerOfEndingStateChange();
                    }
                }
            };
            spinnerMouseListener = new MouseAdapter() {
                @Override
                public void mouseUp(MouseEvent e) {
                    notifyListenerOfEndingStateChange();
                }
            };
        } else {
            spinnerFocusListener = null;
            spinnerKeyListener = null;
            spinnerMouseListener = null;
        }
    }

    // Public Methods

    /**
     * Set the holder of this helper's widgets.
     * 
     * @param holder
     *            Holder of this helper's component widgets.
     */
    public void setHolder(ISpinnerAndScaleComponentHolder<T> holder) {
        this.holder = holder;
    }

    /**
     * Create the composite holding the components, and the label if
     * appropriate.
     * 
     * @param parent
     *            Composite to be used as the parent of the panel.
     * @param specifier
     *            Specifier for the megawidget being built.
     */
    public void buildParentPanelAndLabel(Composite parent,
            ISingleLineSpecifier specifier) {
        panel = UiBuilder.buildComposite(parent, stateIdentifiers.size() * 2,
                SWT.NONE, UiBuilder.CompositeType.SINGLE_ROW, specifier);
        ((GridData) panel.getLayoutData()).grabExcessHorizontalSpace = specifier
                .isHorizontalExpander();
        if (specifier.isHorizontalExpander() == false) {
            ((GridData) panel.getLayoutData()).horizontalAlignment = SWT.LEFT;
        }
        label = UiBuilder.buildLabel(panel, specifier);
        labelsForStateIdentifiers.put(stateIdentifiers.get(0), label);
    }

    /**
     * Build a label other than the leftmost label, to go between one spinner
     * and the next.
     * 
     * @param specifier
     *            Specifier for the megawidget being built.
     * @param text
     *            Text to be shown in the label, or <code>null</code> if no
     *            label is to be created.
     * @param stateIdentifier
     *            State identifier for which the label is being created.
     */
    public void buildJoiningLabel(ISingleLineSpecifier specifier, String text,
            String stateIdentifier) {
        Label label = null;
        if (text != null) {
            label = UiBuilder.buildLabel(panel, text, specifier);
        }
        labelsForStateIdentifiers.put(stateIdentifier, label);
    }

    /**
     * Create a spinner.
     * 
     * @param specifier
     *            Specifier for the megawidget being built.
     * @param minimumValue
     *            State minimum value that the spinner may hold.
     * @param maximumValue
     *            State maximum value that the spinner may hold.
     * @param pageIncrementDelta
     *            State delta by which to increment when using the Page keys.
     * @param showScale
     *            Flag indicating whether or not a scale widget will be shown
     *            below the spinner(s) in the megawidget as well.
     * @param stateIdentifier
     *            State identifier for which the spinner is being created; if a
     *            subclass has only one state identifier, this may be
     *            <code>null</code>.
     */
    public void buildSpinner(ISingleLineSpecifier specifier, T minimumValue,
            T maximumValue, T pageIncrementDelta, boolean showScale,
            String stateIdentifier) {

        /*
         * Create the spinner. The maximum must be set twice to bogus values in
         * order to calculate how many pixels are needed per digit, so that the
         * minimum width may be figured below.
         */
        Spinner spinner = new Spinner(panel, SWT.BORDER
                | (stateIdentifiers.size() > 1 ? SWT.NONE : SWT.WRAP));
        spinner.setMaximum(9);
        int oneDigitSpinnerWidthPixels = spinner.computeSize(SWT.DEFAULT,
                SWT.DEFAULT).x;
        spinner.setMaximum(99);
        if (digitWidthPixels == 0) {
            digitWidthPixels = spinner.computeSize(SWT.DEFAULT, SWT.DEFAULT).x
                    - oneDigitSpinnerWidthPixels;
        }
        int maxNumCharacters = Math.max(getDigitsForValue(minimumValue),
                getDigitsForValue(maximumValue));
        spinner.setTextLimit(maxNumCharacters);
        spinner.setMinimum(convertValueToSpinner(minimumValue));
        spinner.setMaximum(convertValueToSpinner(maximumValue));
        int incrementDelta = convertValueToSpinner(pageIncrementDelta);
        spinner.setIncrement(incrementDelta);
        spinner.setPageIncrement(incrementDelta);
        spinner.setDigits(holder.getPrecision());
        spinner.setEnabled(specifier.isEnabled());
        UiBuilder.ensureMouseWheelEventsPassedUpToAncestor(spinner);

        /*
         * Place the spinner in the parent's grid.
         */
        GridData gridData = new GridData((specifier.isHorizontalExpander()
                || showScale ? SWT.FILL : SWT.LEFT), SWT.CENTER, true, false);
        gridData.horizontalSpan = (labelsForStateIdentifiers
                .get(stateIdentifier) == null ? 2 : 1);
        gridData.minimumWidth = oneDigitSpinnerWidthPixels
                + ((maxNumCharacters - 1) * digitWidthPixels);
        spinner.setLayoutData(gridData);

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
            spinner.addFocusListener(spinnerFocusListener);
            spinner.addKeyListener(spinnerKeyListener);
            spinner.addMouseListener(spinnerMouseListener);
        }

        /*
         * Bind the spinner selection event to trigger a change in the state,
         * and to alter the scale's value if one is present.
         */
        spinner.addSelectionListener(new SpinnerListener(stateIdentifier));

        /*
         * Remember the spinner for the future.
         */
        spinnersForStateIdentifiers.put(stateIdentifier, spinner);
    }

    /**
     * Build the scale widget, if called for by the specifier.
     * 
     * @param specifier
     *            Specifier for the megawidget being built.
     * @param minimumValue
     *            State minimum value that the spinner may hold.
     * @param maximumValue
     *            State maximum value that the spinner may hold.
     * @param boundariesForIdentifiers
     *            Map of state identifiers to their thumb boundaries, or
     *            <code>null</code> if there are no individual boundaries.
     * @param minimumInterval
     *            Minimum interval between adjacent thumbs, or <code>null</code>
     *            if the minimum interval is 0.
     * @param pageIncrementDelta
     *            State delta by which to increment when using the Page keys.
     * @param showScale
     *            Flag indicating whether or not a scale widget will be shown
     *            below the spinner(s) in the megawidget as well.
     */
    public void buildScale(ISingleLineSpecifier specifier, T minimumValue,
            T maximumValue, Map<String, Range<T>> boundariesForIdentifiers,
            T minimumInterval, T pageIncrementDelta, boolean showScale) {

        /*
         * Add a scale, if one is desired.
         */
        if (showScale) {

            /*
             * Create the scale.
             */
            long minValue = convertValueToScale(minimumValue);
            long maxValue = convertValueToScale(maximumValue);
            scale = new MultiValueScale(panel, minValue, maxValue);
            UiBuilder.setMultiValueScaleVisualComponentDimensions(scale, 0, 0);
            scale.setVisibleValueRange(minValue, maxValue);
            long[] values = new long[stateIdentifiers.size()];
            long interval = (maxValue - minValue) / values.length;
            for (int j = 0; j < values.length; j++) {
                values[j] = minValue + (j * interval);
            }
            scale.setConstrainedThumbValues(values);
            if (boundariesForIdentifiers != null) {
                for (int j = 0; j < stateIdentifiers.size(); j++) {
                    Range<T> boundaries = boundariesForIdentifiers
                            .get(stateIdentifiers.get(j));
                    scale.setAllowableConstrainedValueRange(j,
                            convertValueToScale(boundaries.lowerEndpoint()),
                            convertValueToScale(boundaries.upperEndpoint()));
                }
            }
            if (minimumInterval != null) {
                scale.setMinimumDeltaBetweenConstrainedThumbs(convertValueToScale(minimumInterval));
            } else {
                scale.setMinimumDeltaBetweenConstrainedThumbs(0L);
            }
            scale.setEnabled(specifier.isEnabled());
            UiBuilder.ensureMouseWheelEventsPassedUpToAncestor(scale);

            /*
             * Place the scale in the parent's grid.
             */
            GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
            gridData.horizontalSpan = stateIdentifiers.size() * 2;
            gridData.minimumWidth = 10 * digitWidthPixels;
            scale.setLayoutData(gridData);
        } else {
            scale = null;
        }

        /*
         * If a scale is supplied, add a listener to it to handle changes made
         * by the user via the scale.
         */
        if (scale != null) {
            scale.addMultiValueLinearControlListener(new ScaleListener());
        }
    }

    /**
     * Get the label in use, if any.
     * 
     * @return Label in use, or <code>null</code> if none is showing.
     */
    public Label getLabel() {
        return label;
    }

    /**
     * Synchronize component widgets to the current minimum and maximum
     * boundaries.
     */
    public void synchronizeComponentWidgetsToBounds() {
        for (int j = 0; j < stateIdentifiers.size(); j++) {
            String identifier = stateIdentifiers.get(j);
            Spinner spinner = spinnersForStateIdentifiers.get(identifier);
            T minValue = holder.getMinimumValue(identifier);
            T maxValue = holder.getMaximumValue(identifier);
            spinner.setMinimum(convertValueToSpinner(minValue));
            spinner.setMaximum(convertValueToSpinner(maxValue));
            spinner.setTextLimit(Math.max(getDigitsForValue(minValue),
                    getDigitsForValue(maxValue)));
            if (scale != null) {
                scale.setAllowableConstrainedValueRange(j,
                        convertValueToScale(minValue),
                        convertValueToScale(maxValue));
            }
        }
    }

    /**
     * Synchronize component widgets to the current page increment delta.
     */
    public void synchronizeComponentWidgetsToPageIncrementDelta() {
        int pageIncrementDelta = convertValueToSpinner(holder
                .getPageIncrementDelta());
        for (Spinner spinner : spinnersForStateIdentifiers.values()) {
            spinner.setPageIncrement(pageIncrementDelta);
        }
    }

    /**
     * Synchronize component widgets to the current state.
     */
    public void synchronizeComponentWidgetsToState() {
        for (Map.Entry<String, Spinner> entry : spinnersForStateIdentifiers
                .entrySet()) {
            entry.getValue().setSelection(
                    convertValueToSpinner(holder.getState(entry.getKey())));
        }
        if (scale != null) {
            long[] scaleValues = new long[stateIdentifiers.size()];
            for (int j = 0; j < scaleValues.length; j++) {
                scaleValues[j] = convertValueToScale(holder
                        .getState(stateIdentifiers.get(j)));
            }
            scale.setConstrainedThumbValues(scaleValues);
        }
    }

    /**
     * Enable or disable the component widgets.
     * 
     * @param enable
     *            Flag indicating whether the component widgets are to be
     *            enabled or disabled.
     */
    public void setEnabled(boolean enable) {
        for (Label label : labelsForStateIdentifiers.values()) {
            if (label != null) {
                label.setEnabled(enable);
            }
        }
        for (Spinner spinner : spinnersForStateIdentifiers.values()) {
            spinner.setEnabled(enable);
        }
        if (scale != null) {
            scale.setEnabled(enable);
        }
    }

    /**
     * Enable or disable editability of the component widgetes.
     * 
     * @param editable
     *            Flag indicating whether the component widgets are to be
     *            editable or read-only.
     * @param helper
     *            Control helper to be used to query background colors if
     *            necessary.
     */
    public void setEditable(final boolean editable,
            ControlComponentHelper helper) {
        panel.setEnabled(editable);
        for (Spinner spinner : spinnersForStateIdentifiers.values()) {
            spinner.setBackground(helper.getBackgroundColor(editable, spinner,
                    label));
        }
    }

    /**
     * Handle a programmatic state change that occurred for the enclosing
     * megawidget. This must be called whenever the holder experiences such a
     * state change.
     */
    public void handleProgrammaticStateChange() {
        forgetLastForwardedState();
    }

    // Protected Methods

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
     * represent said value in a spinner widget.
     * 
     * @param value
     *            Value to be converted.
     * @return Integer equivalent for a spinner widget.
     */
    protected abstract int convertValueToSpinner(T value);

    /**
     * Convert the specified spinner integer to the corresponding megawidget
     * state value.
     * 
     * @param value
     *            Value to be converted.
     * @return Megawidget state value equivalent.
     */
    protected abstract T convertSpinnerToValue(int value);

    /**
     * Convert the specified megawidget state value to the integer needed to
     * represent said value in the scale widget.
     * 
     * @param value
     *            Value to be converted.
     * @return Integer equivalent for the spinner and/or scale widgets.
     */
    protected abstract long convertValueToScale(T value);

    /**
     * Convert the specified scale integer to the corresponding megawidget state
     * value.
     * 
     * @param value
     *            Value to be converted.
     * @return Megawidget state value equivalent.
     */
    protected abstract T convertScaleToValue(long value);

    /**
     * Get the holder.
     * 
     * @return Holder.
     */
    protected final ISpinnerAndScaleComponentHolder<T> getHolder() {
        return holder;
    }

    // Private Methods

    /**
     * Determine whether or not the last forwarded state has been recorded. This
     * is used for megawidgets that have been configured to not forward rapid
     * state change notifications.
     * 
     * @return True if the last forwarded state record has been initialized,
     *         false otherwise.
     */
    private boolean isLastForwardedStateRecorded() {
        return (lastForwardedStatesForIdentifiers != null);
    }

    /**
     * Record the last forwarded state. This is used for megawidgets that have
     * been configured to not forward rapid state change notifications.
     */
    private void recordLastForwardedState() {
        lastForwardedStatesForIdentifiers = new HashMap<>(
                stateIdentifiers.size(), 1.0f);
        for (String identifier : stateIdentifiers) {
            lastForwardedStatesForIdentifiers.put(identifier,
                    holder.getState(identifier));
        }
    }

    /**
     * Forget the last forwarded state. This is used for megawidgets that have
     * been configured to not forward rapid state change notifications.
     */
    private void forgetLastForwardedState() {
        lastForwardedStatesForIdentifiers = null;
    }

    /**
     * Notify the state change listener of a state change if the current state
     * is not the same as the last state of which the state change listener is
     * assumed to be aware. This method is used by megawidgets configured to
     * only send non-rapid state change notifications to their listeners.
     */
    private void notifyListenerOfEndingStateChange() {
        if (lastForwardedStatesForIdentifiers != null) {
            List<String> identifiersOfChangedStates = new ArrayList<>(
                    stateIdentifiers.size());
            for (String identifier : stateIdentifiers) {
                if (lastForwardedStatesForIdentifiers.get(identifier).equals(
                        holder.getState(identifier)) == false) {
                    identifiersOfChangedStates.add(identifier);
                }
            }
            forgetLastForwardedState();
            if (identifiersOfChangedStates.isEmpty() == false) {
                holder.notifyListener(identifiersOfChangedStates);
            }
        }
    }
}
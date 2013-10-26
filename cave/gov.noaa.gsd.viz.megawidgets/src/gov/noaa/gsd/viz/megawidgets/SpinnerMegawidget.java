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
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Spinner;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

/**
 * Spinner megawidget, allowing the manipulation a bounded value that has
 * discrete steps.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 23, 2013   2168     Chris.Golden      Initial creation
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see SpinnerSpecifier
 */
public abstract class SpinnerMegawidget<T extends Comparable<T>> extends
        BoundedValueMegawidget<T> implements IControl {

    // Protected Static Constants

    /**
     * Set of all mutable property names for instances of this class.
     */
    protected static final Set<String> MUTABLE_PROPERTY_NAMES;
    static {
        Set<String> names = Sets
                .newHashSet(BoundedValueMegawidget.MUTABLE_PROPERTY_NAMES);
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
     * Flag indicating whether or not a programmatic change to component values
     * is currently in process.
     */
    private boolean changeInProgress = false;

    /**
     * Increment delta.
     */
    private T incrementDelta;

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

        // Create the composite holding the components, and
        // the label if appropriate.
        Composite panel = UiBuilder.buildComposite(parent, 2, SWT.NONE,
                UiBuilder.CompositeType.SINGLE_ROW, specifier);
        boolean expandHorizontally = (specifier.isHorizontalExpander() || specifier
                .isShowScale());
        ((GridData) panel.getLayoutData()).grabExcessHorizontalSpace = expandHorizontally;
        label = UiBuilder.buildLabel(panel, specifier);

        // Create the spinner.
        spinner = new Spinner(panel, SWT.BORDER + SWT.WRAP);
        spinner.setTextLimit(Math.max(
                getDigitsForValue(specifier.getMinimumValue()),
                getDigitsForValue(specifier.getMaximumValue())));
        spinner.setMinimum(convertValueToSpinner(specifier.getMinimumValue()));
        spinner.setMaximum(convertValueToSpinner(specifier.getMaximumValue()));
        incrementDelta = specifier.getIncrementDelta();
        spinner.setPageIncrement(convertValueToSpinner(incrementDelta));
        spinner.setDigits(getSpinnerPrecision());
        spinner.setEnabled(specifier.isEnabled());

        // Place the spinner in the parent's grid.
        GridData gridData = new GridData((expandHorizontally ? SWT.FILL
                : SWT.LEFT), SWT.CENTER, true, false);
        gridData.horizontalSpan = (label == null ? 2 : 1);
        spinner.setLayoutData(gridData);

        // Add a scale, if one is desired.
        if (specifier.isShowScale()) {

            // Create the scale.
            scale = new Scale(panel, SWT.HORIZONTAL);
            scale.setMinimum(convertValueToSpinner(specifier.getMinimumValue()));
            scale.setMaximum(convertValueToSpinner(specifier.getMaximumValue()));
            scale.setPageIncrement(convertValueToSpinner(incrementDelta));
            scale.setEnabled(specifier.isEnabled());

            // Place the spinner in the parent's grid.
            gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
            gridData.horizontalSpan = 2;
            scale.setLayoutData(gridData);
        } else {
            scale = null;
        }

        // Bind the spinner selection event to trigger
        // a change in the state, and to alter the
        // scale's value if one is present.
        spinner.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {

                // If this is a result of a programmatic
                // change, do nothing with it.
                if (changeInProgress) {
                    return;
                }

                // Indicate that a change is in progress.
                changeInProgress = true;

                // If the state is changing, make a record
                // of the change and alter the scale, if
                // any, to match.
                int state = SpinnerMegawidget.this.spinner.getSelection();
                if ((SpinnerMegawidget.this.state == null)
                        || (state != convertValueToSpinner(SpinnerMegawidget.this.state))) {
                    SpinnerMegawidget.this.state = convertSpinnerToValue(state);
                    if (SpinnerMegawidget.this.scale != null) {
                        SpinnerMegawidget.this.scale.setSelection(state);
                    }
                    SpinnerMegawidget.this.spinner.update();
                    notifyListener(getSpecifier().getIdentifier(),
                            SpinnerMegawidget.this.state);
                    notifyListener();
                }

                // Reset the in-progress flag.
                changeInProgress = false;
            }
        });

        // If a scale is supplied, bind its selection
        // event to trigger a change in the state, and
        // to alter the spinner's value as well.
        if (scale != null) {
            scale.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {

                    // If this is a result of a program-
                    // matic change, do nothing with it.
                    if (changeInProgress) {
                        return;
                    }

                    // Indicate that a change is in pro-
                    // gress.
                    changeInProgress = true;

                    // If the state is changing, make a
                    // record of the change and alter the
                    // spinner to match.
                    int state = SpinnerMegawidget.this.scale.getSelection();
                    if ((SpinnerMegawidget.this.state == null)
                            || (state != convertValueToSpinner(SpinnerMegawidget.this.state))) {
                        SpinnerMegawidget.this.state = convertSpinnerToValue(state);
                        SpinnerMegawidget.this.spinner.setSelection(state);
                        notifyListener(getSpecifier().getIdentifier(),
                                SpinnerMegawidget.this.state);
                        notifyListener();
                    }

                    // Reset the in-progress flag.
                    changeInProgress = false;
                }
            });
        }

        // Render the spinner uneditable if necessary.
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
        } else if (name.equals(SpinnerSpecifier.MEGAWIDGET_INCREMENT_DELTA)) {
            return getIncrementDelta();
        }
        return super.getMutableProperty(name);
    }

    @Override
    public void setMutableProperty(String name, Object value)
            throws MegawidgetPropertyException {
        if (name.equals(IControlSpecifier.MEGAWIDGET_EDITABLE)) {
            setEditable(getPropertyBooleanValueFromObject(value, name, null));
        } else if (name.equals(SpinnerSpecifier.MEGAWIDGET_INCREMENT_DELTA)) {
            setIncrementDelta(value);
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
        if (label != null) {
            helper.setWidgetsWidth(width, label);
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
     * Get the increment delta.
     * 
     * @return Increment delta.
     */
    public final T getIncrementDelta() {
        return incrementDelta;
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
        incrementDelta = getPropertyIncrementDeltaObjectFromObject(value,
                SpinnerSpecifier.MEGAWIDGET_INCREMENT_DELTA);
        int spinnerDelta = convertValueToSpinner(incrementDelta);
        spinner.setPageIncrement(spinnerDelta);
        if (scale != null) {
            scale.setPageIncrement(spinnerDelta);
        }
    }

    // Protected Methods

    @Override
    protected final void synchronizeWidgetsToBounds() {
        int minValue = convertValueToSpinner(getMinimumValue());
        int maxValue = convertValueToSpinner(getMaximumValue());
        spinner.setMinimum(minValue);
        spinner.setMaximum(maxValue);
        if (scale != null) {
            scale.setMinimum(minValue);
            scale.setMaximum(maxValue);
        }
        spinner.setTextLimit(Math.max(getDigitsForValue(getMinimumValue()),
                getDigitsForValue(getMaximumValue())));
    }

    @Override
    protected final void synchronizeWidgetsToState() {
        spinner.setSelection(convertValueToSpinner(state));
        if (scale != null) {
            scale.setSelection(convertValueToSpinner(state));
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

    /**
     * Get the property increment delta object from the specified object.
     * 
     * @param object
     *            Object holding the increment delta value.
     * @param name
     *            Name of the parameter for which <code>object</code> is the
     *            value.
     * @return Increment delta object.
     * @throws MegawidgetPropertyException
     *             If the megawidget specifier parameter is invalid.
     */
    protected abstract T getPropertyIncrementDeltaObjectFromObject(
            Object object, String name) throws MegawidgetPropertyException;

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
        spinner.getParent().setEnabled(editable);
        spinner.setBackground(helper.getBackgroundColor(editable, spinner,
                label));
    }
}
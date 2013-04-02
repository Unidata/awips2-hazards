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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Spinner;

/**
 * Integer spinner megawidget, allowing the manipulation of an integer.
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
 * @see IntegerSpinnerSpecifier
 */
public class IntegerSpinnerMegawidget extends StatefulMegawidget {

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
     * Current value.
     */
    private Integer state;

    /**
     * Flag indicating whether or not a programmatic change to component values
     * is currently in process.
     */
    private boolean changeInProgress = false;

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
    protected IntegerSpinnerMegawidget(IntegerSpinnerSpecifier specifier,
            Composite parent, Map<String, Object> paramMap) {
        super(specifier, paramMap);

        // Create a panel in which to place the widgets.
        // This is done so that it may be rendered read-only
        // by disabling the panel, which in SWT has the
        // effect of disabling mouse and keyboard input for
        // the child widgets without making them look dis-
        // abled; in order to group the widgets properly
        // into a single megawidget; and to allow the space
        // for the label, if it exists, to be sized to match
        // other labels.
        Composite panel = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(2, false);
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        gridLayout.horizontalSpacing = 10;
        panel.setLayout(gridLayout);
        GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gridData.horizontalSpan = specifier.getWidth();
        gridData.verticalIndent = specifier.getSpacing();
        panel.setLayoutData(gridData);

        // Add a label if one is required.
        if ((specifier.getLabel() != null)
                && (specifier.getLabel().length() > 0)) {

            // Create a label widget.
            label = new Label(panel, SWT.NONE);
            label.setText(specifier.getLabel());
            label.setEnabled(specifier.isEnabled());

            // Place the label in the parent's grid.
            gridData = new GridData(SWT.LEFT, SWT.CENTER, false, false);
            label.setLayoutData(gridData);
        } else {
            label = null;
        }

        // Create the spinner.
        spinner = new Spinner(panel, SWT.BORDER + SWT.WRAP);
        spinner.setTextLimit(Math.max(
                getDigitsForValue(specifier.getMinimumValue()),
                getDigitsForValue(specifier.getMaximumValue())));
        spinner.setMinimum(specifier.getMinimumValue());
        spinner.setMaximum(specifier.getMaximumValue());
        spinner.setPageIncrement(specifier.getIncrementDelta());
        spinner.setEnabled(specifier.isEnabled());

        // Place the spinner in the parent's grid.
        gridData = new GridData(SWT.LEFT, SWT.CENTER, true, false);
        gridData.horizontalSpan = (label == null ? 2 : 1);
        spinner.setLayoutData(gridData);

        // Add a scale, if one is desired.
        if (specifier.isShowScale()) {

            // Create the scale.
            scale = new Scale(panel, SWT.HORIZONTAL);
            scale.setMinimum(specifier.getMinimumValue());
            scale.setMaximum(specifier.getMaximumValue());
            scale.setPageIncrement(specifier.getIncrementDelta());
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
                int state = IntegerSpinnerMegawidget.this.spinner
                        .getSelection();
                if ((IntegerSpinnerMegawidget.this.state == null)
                        || (state != IntegerSpinnerMegawidget.this.state
                                .intValue())) {
                    IntegerSpinnerMegawidget.this.state = state;
                    if (IntegerSpinnerMegawidget.this.scale != null) {
                        IntegerSpinnerMegawidget.this.scale.setSelection(state);
                    }
                    notifyListener(getSpecifier().getIdentifier(), state);
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
                    int state = IntegerSpinnerMegawidget.this.scale
                            .getSelection();
                    if ((IntegerSpinnerMegawidget.this.state == null)
                            || (state != IntegerSpinnerMegawidget.this.state
                                    .intValue())) {
                        IntegerSpinnerMegawidget.this.state = state;
                        IntegerSpinnerMegawidget.this.spinner
                                .setSelection(state);
                        notifyListener(getSpecifier().getIdentifier(), state);
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

    /**
     * Determine the left decoration width for this megawidget, if the widget
     * has a decoration (label, etc.) to the left of its main component
     * widget(s). This is used to query all sibling megawidgets to determine
     * which one has the largest left decoration.
     * 
     * @return Width in pixels required for the left decoration of this
     *         megawidget, or 0 if the megawidget has no left decoration.
     */
    @Override
    public int getLeftDecorationWidth() {
        return (label == null ? 0 : getWidestWidgetWidth(label));
    }

    /**
     * Set the left decoration width for this megawidget to that specified, if
     * the widget has a decoration to the left of its main component widget(s).
     * This is used to set sibling megawidgets to all use the width of the
     * largest left decoration used by the siblings, if any.
     * 
     * @param width
     *            Width to be used if this megawidget has a left decoration.
     */
    @Override
    public void setLeftDecorationWidth(int width) {
        if (label != null) {
            setWidgetsWidth(width, label);
        }
    }

    // Protected Methods

    /**
     * Change the component widgets to ensure their state matches that of the
     * enabled flag.
     * 
     * @param enable
     *            Flag indicating whether the component widgets are to be
     *            enabled or disabled.
     */
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
     * Change the component widgets to ensure their state matches that of the
     * editable flag.
     * 
     * @param editable
     *            Flag indicating whether the component widgets are to be
     *            editable or read-only.
     */
    @Override
    protected final void doSetEditable(boolean editable) {
        spinner.getParent().setEnabled(editable);
        spinner.setBackground(getBackgroundColor(editable, spinner, label));
    }

    /**
     * Get the current state for the specified identifier. This method is called
     * by <code>getState()</code> only after the latter has ensured that the
     * supplied state identifier is valid.
     * 
     * @param identifier
     *            Identifier for which state is desired. Implementations may
     *            assume that the state identifier supplied by this parameter is
     *            valid for this megawidget.
     * @return Object making up the current state for the specified identifier.
     */
    @Override
    protected final Object doGetState(String identifier) {
        return state;
    }

    /**
     * Set the current state for the specified identifier. This method is called
     * by <code>setState()</code> only after the latter has ensured that the
     * supplied state identifier is valid, and has set a flag that indicates
     * that this setting of the state will not trigger the megawidget to notify
     * its listener of an invocation.
     * 
     * @param identifier
     *            Identifier for which state is to be set. Implementations may
     *            assume that the state identifier supplied by this parameter is
     *            valid for this megawidget.
     * @param state
     *            Object making up the state to be used for this identifier, or
     *            <code>null</code> if this state should be reset.
     * @throws MegawidgetStateException
     *             If new state is not of a valid type for this <code>
     *             StatefulWidget</code> implementation.
     */
    @Override
    protected final void doSetState(String identifier, Object state)
            throws MegawidgetStateException {
        IntegerSpinnerSpecifier specifier = getSpecifier();
        int value = getStateIntegerValueFromObject(state, identifier,
                specifier.getMinimumValue());
        if ((value < specifier.getMinimumValue())
                || (value > specifier.getMaximumValue())) {
            throw new MegawidgetStateException(identifier, specifier.getType(),
                    value, "out of bounds (minimum = "
                            + specifier.getMinimumValue() + ", maximum = "
                            + specifier.getMaximumValue() + " (inclusive))");
        }
        this.state = value;
        spinner.setSelection(value);
        if (scale != null) {
            scale.setSelection(value);
        }
    }

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
     *            by this parameter is valid for this megawidget.
     * @param state
     *            State for which to generate a shortened description.
     * @return Description of the specified state.
     * @throws MegawidgetStateException
     *             If the specified state is not of a valid type for this
     *             <code>StatefulWidget </code> implementation.
     */
    @Override
    protected final String doGetStateDescription(String identifier, Object state)
            throws MegawidgetStateException {
        return (state == null ? null : state.toString());
    }

    // Private Methods

    /**
     * Get the number of characters that are required to show the specified
     * value in base 10.
     * 
     * @param value
     *            Value to be shown.
     * @return Number of characters required to show the specified number using
     *         the specified unit in base 10.
     */
    private int getDigitsForValue(int value) {
        return ((int) Math.floor(Math.log10(Math.abs(value))))
                + (value < 0 ? 1 : 0) + 1;
    }
}
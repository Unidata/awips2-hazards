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

import gov.noaa.gsd.viz.megawidgets.TimeDeltaSpecifier.Unit;

import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;

/**
 * Time delta megawidget, providing the user a spinner widget with an optional
 * drop-down combo box for specifying the units to be used, in order to
 * manipulate a time delta.
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
 * @see TimeDeltaSpecifier
 */
public class TimeDeltaMegawidget extends StatefulMegawidget {

    // Private Variables

    /**
     * Label associated with this megawidget, if any.
     */
    private final Label label;

    /**
     * Spinner component associated with this megawidget.
     */
    private final Spinner spinner;

    /**
     * Units combo box component associated with this megawidget, if any.
     */
    private final Combo combo;

    /**
     * Unit label associated with this megawidget, if any.
     */
    private final Label unitLabel;

    /**
     * Currently selected unit.
     */
    private Unit unit = null;

    /**
     * Current value in milliseconds.
     */
    private Long state = null;

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
    protected TimeDeltaMegawidget(TimeDeltaSpecifier specifier,
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
        GridLayout gridLayout = new GridLayout(3, false);
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
                getDigitsForValue(specifier.getMinimumValue(), specifier
                        .getUnits().get(0)),
                getDigitsForValue(specifier.getMaximumValue(), specifier
                        .getUnits().get(0))));
        setSpinnerParameters(spinner, specifier.getStartingUnit());
        spinner.setEnabled(specifier.isEnabled());

        // Place the spinner in the panel's grid.
        gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gridData.horizontalSpan = (label == null ? 2 : 1);
        spinner.setLayoutData(gridData);

        // If there is more than one possible unit, create the
        // unit choosing combo box; otherwise, create a label
        // indicating the unit.
        List<Unit> units = specifier.getUnits();
        if (units.size() > 1) {

            // Create the combo box.
            unitLabel = null;
            combo = new Combo(panel, SWT.READ_ONLY);
            String[] unitIdentifiers = new String[units.size()];
            int selectedIndex = -1;
            for (int j = 0; j < units.size(); j++) {
                unitIdentifiers[j] = units.get(j).getIdentifier();
                if (units.get(j) == specifier.getStartingUnit()) {
                    selectedIndex = j;
                }
            }
            combo.setItems(unitIdentifiers);
            combo.select(selectedIndex);
            combo.setEnabled(specifier.isEnabled());

            // Place the combo box in the panel's grid.
            gridData = new GridData(SWT.LEFT, SWT.CENTER, false, false);
            combo.setLayoutData(gridData);
        } else {

            // Create the label.
            combo = null;
            unitLabel = new Label(panel, SWT.NONE);
            unitLabel
                    .setText(" " + specifier.getStartingUnit().getIdentifier());
            unitLabel.setEnabled(specifier.isEnabled());

            // Place the combo box in the panel's grid.
            gridData = new GridData(SWT.LEFT, SWT.CENTER, false, false);
            unitLabel.setLayoutData(gridData);
        }

        // Remember the starting unit.
        this.unit = specifier.getStartingUnit();

        // Bind the spinner selection event to trigger a
        // change in the state.
        spinner.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                long state = TimeDeltaMegawidget.this.unit
                        .convertUnitToMilliseconds(TimeDeltaMegawidget.this.spinner
                                .getSelection());
                if ((TimeDeltaMegawidget.this.state == null)
                        || (state != TimeDeltaMegawidget.this.state.longValue())) {
                    TimeDeltaSpecifier specifier = getSpecifier();
                    TimeDeltaMegawidget.this.state = state;
                    notifyListener(specifier.getIdentifier(), specifier
                            .getStateUnit().convertMillisecondsToUnit(state));
                    notifyListener();
                }
            }
        });

        // Bind the units combo box selection event to
        // trigger a change in the unit, and to alter the
        // spinner accordingly, if a combo box was supplied;
        // if it was not, then there are no unit choices, so
        // the units remain constant.
        if (combo != null) {
            combo.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    TimeDeltaMegawidget.this.unit = Unit
                            .get(TimeDeltaMegawidget.this.combo
                                    .getItem(TimeDeltaMegawidget.this.combo
                                            .getSelectionIndex()));
                    setSpinnerParameters(TimeDeltaMegawidget.this.spinner,
                            TimeDeltaMegawidget.this.unit);
                    if (state == null) {
                        TimeDeltaSpecifier specifier = getSpecifier();
                        state = Long.valueOf(specifier.getStateUnit()
                                .convertUnitToMilliseconds(
                                        specifier.getMinimumValue()));
                    }
                    int spinnerValue = TimeDeltaMegawidget.this.unit
                            .convertMillisecondsToUnit(state);
                    state = TimeDeltaMegawidget.this.unit
                            .convertUnitToMilliseconds(spinnerValue);
                    TimeDeltaMegawidget.this.spinner
                            .setSelection(TimeDeltaMegawidget.this.unit
                                    .convertMillisecondsToUnit(state));
                    notifyListener();
                }
            });
        }

        // Render the components uneditable if necessary.
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

    /**
     * Determine the right decoration width for this megawidget, if the widget
     * has a decoration (label, etc.) to the right of its main component
     * widget(s). This is used to query all sibling megawidgets to determine
     * which one has the largest right decoration.
     * 
     * @return Width in pixels required for the right decoration of this
     *         megawidget, or 0 if the megawidget has no right decoration.
     */
    @Override
    public int getRightDecorationWidth() {
        return (combo == null ? (unitLabel == null ? 0
                : getWidestWidgetWidth(unitLabel))
                : getWidestWidgetWidth(combo));
    }

    /**
     * Set the right decoration width for this megawidget to that specified, if
     * the widget has a decoration to the right of its main component widget(s).
     * This is used to set sibling megawidgets to all use the width of the
     * largest right decoration used by the siblings, if any.
     * 
     * @param width
     *            Width to be used if this megawidget has a right decoration.
     */
    @Override
    public void setRightDecorationWidth(int width) {
        if (combo != null) {
            setWidgetsWidth(width, combo);
        } else if (unitLabel != null) {
            setWidgetsWidth(width, unitLabel);
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
        if (combo != null) {
            combo.setEnabled(enable);
        }
        if (unitLabel != null) {
            unitLabel.setEnabled(enable);
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
        if (combo != null) {
            combo.setBackground(getBackgroundColor(editable, combo, label));
        }
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
        if (state == null) {
            return null;
        }
        return ((TimeDeltaSpecifier) getSpecifier()).getStateUnit()
                .convertMillisecondsToUnit(state);
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
        TimeDeltaSpecifier specifier = getSpecifier();
        int value = getStateIntegerValueFromObject(state, identifier,
                specifier.getMinimumValue());
        if ((value < specifier.getMinimumValue())
                || (value > specifier.getMaximumValue())) {
            throw new MegawidgetStateException(identifier, specifier.getType(),
                    value, "out of bounds (minimum = "
                            + specifier.getMinimumValue() + ", maximum = "
                            + specifier.getMaximumValue() + " (inclusive))");
        }
        this.state = specifier.getStateUnit().convertUnitToMilliseconds(value);
        spinner.setSelection(unit.convertMillisecondsToUnit(this.state));
    }

    /**
     * Get a shortened description of the specified state for the specified
     * identifier. This method is called by <code>getStateDescription() only
     * after the latter has ensured that the supplied state identifier is valid.
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
        TimeDeltaSpecifier specifier = getSpecifier();
        return (state == null ? null : Integer.toString(specifier
                .getStateUnit().convertMillisecondsToUnit(
                        getStateLongValueFromObject(state, identifier, null))));
    }

    // Private Methods

    /**
     * Get the number of characters that are required to show the specified
     * value (given in state units), using the specified unit, in base 10.
     * 
     * @param value
     *            Value to be shown.
     * @param unit
     *            Unit to be used for displaying value.
     * @return Number of characters required to show the specified number using
     *         the specified unit in base 10.
     */
    private int getDigitsForValue(int value, Unit unit) {
        return ((int) Math
                .floor(Math.log10(unit
                        .convertMillisecondsToUnit(((TimeDeltaSpecifier) getSpecifier())
                                .getStateUnit().convertUnitToMilliseconds(
                                        Math.abs(value))))))
                + (value < 0 ? 1 : 0) + 1;
    }

    /**
     * Set the value bounds and the page increment for the specified spinner
     * using the specified unit.
     * 
     * @param spinner
     *            Spinner.
     * @param unit
     *            Unit.
     */
    private void setSpinnerParameters(Spinner spinner, Unit unit) {
        TimeDeltaSpecifier specifier = (TimeDeltaSpecifier) getSpecifier();
        spinner.setMinimum(unit.convertMillisecondsToUnit(specifier
                .getStateUnit().convertUnitToMilliseconds(
                        specifier.getMinimumValue())));
        spinner.setMaximum(unit.convertMillisecondsToUnit(specifier
                .getStateUnit().convertUnitToMilliseconds(
                        specifier.getMaximumValue())));
        spinner.setPageIncrement(unit.getPageIncrement());
    }
}
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
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

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
 * Apr 30, 2013   1277     Chris.Golden      Added support for mutable properties.
 * Oct 22, 2013   2168     Chris.Golden      Replaced some GUI creation code with
 *                                           calls to UiBuilder methods to avoid
 *                                           code duplication and encourage uni-
 *                                           form look, and to implement new
 *                                           IControl interface.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see TimeDeltaSpecifier
 */
public class TimeDeltaMegawidget extends BoundedValueMegawidget<Long> implements
        IControl {

    // Protected Static Constants

    /**
     * Set of all mutable property names for instances of this class.
     */
    protected static final Set<String> MUTABLE_PROPERTY_NAMES;
    static {
        Set<String> names = Sets
                .newHashSet(BoundedValueMegawidget.MUTABLE_PROPERTY_NAMES);
        names.add(IControlSpecifier.MEGAWIDGET_EDITABLE);
        names.add(TimeDeltaSpecifier.MEGAWIDGET_CURRENT_UNIT_CHOICE);
        MUTABLE_PROPERTY_NAMES = ImmutableSet.copyOf(names);
    };

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
     * } Units combo box component associated with this megawidget, if any.
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
    protected TimeDeltaMegawidget(TimeDeltaSpecifier specifier,
            Composite parent, Map<String, Object> paramMap) {
        super(specifier, paramMap);
        helper = new ControlComponentHelper(specifier);

        // Create the composite holding the components, and
        // the label if appropriate.
        Composite panel = UiBuilder.buildComposite(parent, 3, SWT.NONE,
                UiBuilder.CompositeType.SINGLE_ROW, specifier);
        ((GridData) panel.getLayoutData()).grabExcessHorizontalSpace = specifier
                .isHorizontalExpander();
        label = UiBuilder.buildLabel(panel, specifier);

        // Create the spinner.
        spinner = new Spinner(panel, SWT.BORDER + SWT.WRAP);
        spinner.setTextLimit(Math.max(
                getDigitsForValue(getMinimumValue(), specifier.getUnits()
                        .get(0)),
                getDigitsForValue(getMaximumValue(), specifier.getUnits()
                        .get(0))));
        setSpinnerParameters(specifier.getCurrentUnit());
        spinner.setEnabled(specifier.isEnabled());

        // Place the spinner in the panel's grid.
        GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
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
                if (units.get(j) == specifier.getCurrentUnit()) {
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
            unitLabel.setText(" " + specifier.getCurrentUnit().getIdentifier());
            unitLabel.setEnabled(specifier.isEnabled());

            // Place the combo box in the panel's grid.
            gridData = new GridData(SWT.LEFT, SWT.CENTER, false, false);
            unitLabel.setLayoutData(gridData);
        }

        // Remember the starting unit.
        this.unit = specifier.getCurrentUnit();

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
                    unitChanged(true);
                }
            });
        }

        // Render the components uneditable if necessary.
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
        } else if (name
                .equals(TimeDeltaSpecifier.MEGAWIDGET_CURRENT_UNIT_CHOICE)) {
            return getCurrentUnit();
        }
        return super.getMutableProperty(name);
    }

    @Override
    public void setMutableProperty(String name, Object value)
            throws MegawidgetPropertyException {
        if (name.equals(IControlSpecifier.MEGAWIDGET_EDITABLE)) {
            setEditable(getPropertyBooleanValueFromObject(value, name, null));
        } else if (name
                .equals(TimeDeltaSpecifier.MEGAWIDGET_CURRENT_UNIT_CHOICE)) {
            setCurrentUnit(value);
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
    public int getRightDecorationWidth() {
        return (combo == null ? (unitLabel == null ? 0 : helper
                .getWidestWidgetWidth(unitLabel)) : helper
                .getWidestWidgetWidth(combo));
    }

    @Override
    public void setRightDecorationWidth(int width) {
        if (combo != null) {
            helper.setWidgetsWidth(width, combo);
        } else if (unitLabel != null) {
            helper.setWidgetsWidth(width, unitLabel);
        }
    }

    /**
     * Get the current unit.
     * 
     * @return Identifier of the current unit.
     */
    public final String getCurrentUnit() {
        return unit.getIdentifier();
    }

    /**
     * Set the current unit.
     * 
     * @param value
     *            New current unit, specified as a string holding the identifier
     *            of the unit. The unit so identified must be one of the unit
     *            choices available to this megawidget.
     * @throws MegawidgetPropertyException
     *             If the object does not identify a unit, or if said unit is
     *             not one of the choices available to this megawidget.
     */
    public final void setCurrentUnit(Object value)
            throws MegawidgetPropertyException {

        // Ensure a unit is specified, and that said unit is one of the valid
        // choices for this megawidget.
        Unit newUnit = null;
        if (value instanceof String) {
            newUnit = Unit.get((String) value);
        }
        TimeDeltaSpecifier specifier = getSpecifier();
        List<Unit> units = specifier.getUnits();
        if ((newUnit == null) || (units.contains(newUnit) == false)) {
            StringBuilder stringBuilder = new StringBuilder();
            for (Unit aUnit : units) {
                if (stringBuilder.length() > 0) {
                    stringBuilder.append(", ");
                }
                stringBuilder.append(aUnit.getIdentifier());
            }
            throw new MegawidgetPropertyException(specifier.getIdentifier(),
                    TimeDeltaSpecifier.MEGAWIDGET_CURRENT_UNIT_CHOICE,
                    specifier.getType(), value, "must be one of ["
                            + stringBuilder + "]");
        }

        // Set the unit to the new unit.
        if (newUnit != unit) {
            combo.select(units.indexOf(newUnit));
            unitChanged(false);
        }
    }

    // Protected Methods

    @Override
    protected void ensureValueRangeRepresentable(Long minimum, Long maximum)
            throws MegawidgetPropertyException {

        // No action; any long range is always representable.
    }

    @Override
    protected final void synchronizeWidgetsToBounds() {
        setSpinnerParameters(unit);
    }

    @Override
    protected final void synchronizeWidgetsToState() {
        spinner.setSelection(unit.convertMillisecondsToUnit(state));
    }

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

    @Override
    protected final Object doGetState(String identifier) {
        if (state == null) {
            return null;
        }
        return ((TimeDeltaSpecifier) getSpecifier()).getStateUnit()
                .convertMillisecondsToUnit(state);
    }

    @Override
    protected final void doSetState(String identifier, Object state)
            throws MegawidgetStateException {

        // Ensure that the specified state is a valid long value.
        TimeDeltaSpecifier specifier = getSpecifier();
        long value = getStateLongValueFromObject(state, identifier,
                getMinimumValue());
        if ((value < getMinimumValue()) || (value > getMaximumValue())) {
            throw new MegawidgetStateException(identifier, specifier.getType(),
                    value, "out of bounds (minimum = " + getMinimumValue()
                            + ", maximum = " + getMaximumValue()
                            + " (inclusive))");
        }
        this.state = specifier.getStateUnit().convertUnitToMilliseconds(value);

        // Synchronize the widgets to the new state.
        synchronizeWidgetsToState();
    }

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
        if (combo != null) {
            combo.setBackground(helper.getBackgroundColor(editable, combo,
                    label));
        }
    }

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
    private int getDigitsForValue(long value, Unit unit) {
        return ((int) Math
                .floor(Math.log10(unit
                        .convertMillisecondsToUnit(((TimeDeltaSpecifier) getSpecifier())
                                .getStateUnit().convertUnitToMilliseconds(
                                        Math.abs(value))))))
                + (value < 0 ? 1 : 0) + 1;
    }

    /**
     * Respond to the current unit having changed within the unit combo box.
     * 
     * @param notify
     *            Flag indicating whether or not notification should occur.
     */
    private void unitChanged(boolean notify) {
        unit = Unit.get(combo.getItem(combo.getSelectionIndex()));
        setSpinnerParameters(unit);
        if (state == null) {
            TimeDeltaSpecifier specifier = getSpecifier();
            state = Long.valueOf(specifier.getStateUnit()
                    .convertUnitToMilliseconds(getMinimumValue()));
        }
        int spinnerValue = unit.convertMillisecondsToUnit(state);
        state = unit.convertUnitToMilliseconds(spinnerValue);
        spinner.setSelection(unit.convertMillisecondsToUnit(state));
        if (notify) {
            notifyListener();
        }
    }

    /**
     * Set the value bounds and the page increment for the pinner using the
     * specified unit.
     * 
     * @param unit
     *            Unit.
     */
    private void setSpinnerParameters(Unit unit) {
        TimeDeltaSpecifier specifier = (TimeDeltaSpecifier) getSpecifier();
        spinner.setMinimum(unit.convertMillisecondsToUnit(specifier
                .getStateUnit().convertUnitToMilliseconds(getMinimumValue())));
        spinner.setMaximum(unit.convertMillisecondsToUnit(specifier
                .getStateUnit().convertUnitToMilliseconds(getMaximumValue())));
        spinner.setPageIncrement(unit.getPageIncrement());
    }
}
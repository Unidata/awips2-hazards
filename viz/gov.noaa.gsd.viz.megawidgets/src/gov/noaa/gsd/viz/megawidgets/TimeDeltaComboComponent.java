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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * Description: Time delta combo box component, providing an encapsulation of a
 * combo box used to allow the viewing and manipulation of a time delta, to be
 * used as part of time-oriented megawidgets.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Jun 27, 2014    3512    Chris.Golden Initial creation.
 * Mar 31, 2015    6873    Chris.Golden Added code to ensure that mouse wheel
 *                                      events are not processed by the
 *                                      megawidget, but are instead passed up
 *                                      to any ancestor that is a scrolled
 *                                      composite.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class TimeDeltaComboComponent implements ITimeComponent {

    /**
     * Default text to display as the sole choice in the combo when the current
     * delta is not one of the choices.
     */
    private static final String OTHER_DELTA_TEXT = "N/A";

    /**
     * Empty array of combo box items.
     */
    private static final String[] NO_ITEMS_LIST = new String[0];

    // Private Variables

    /**
     * Identifier.
     */
    private final String identifier;

    /**
     * List of delta choice strings.
     */
    private List<String> choices;

    /**
     * Map of delta choice strings to their corresponding time deltas in
     * milliseconds.
     */
    private Map<String, Long> timeDeltasForChoices;

    /**
     * Map of time deltas in milliseconds to their corresponding choice strings.
     */
    private Map<Long, String> choicesForTimeDeltas;

    /**
     * Current delta value in milliseconds.
     */
    private long value;

    /**
     * Label of the megawidget, if any.
     */
    private final Label label;

    /**
     * Time delta selector.
     */
    private final Combo combo;

    /**
     * Flag indicating whether or not the other delta text is currently being
     * shown.
     */
    private boolean otherDeltaTextShowing;

    /**
     * Holder of this time delta component.
     */
    private final ITimeDeltaComboComponentHolder holder;

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
     *            this time delta component.
     * @param choices
     *            List of delta choice strings.
     * @param timeDeltasForChoices
     *            Map of delta choice strings to their corresponding time deltas
     *            in milliseconds.
     * @param startingState
     *            Starting delta value of the component.
     * @param verticalIndent
     *            Vertical indent to be applied.
     * @param holder
     *            Holder of this time delta component.
     */
    public TimeDeltaComboComponent(String identifier, Composite parent,
            String text, IControlSpecifier specifier, List<String> choices,
            Map<String, Long> timeDeltasForChoices, long startingState,
            int verticalIndent, ITimeDeltaComboComponentHolder holder) {
        this.identifier = identifier;
        doSetChoices(choices, timeDeltasForChoices);
        value = startingState;
        this.holder = holder;

        /*
         * Create the composite holding the components, and the label if
         * appropriate.
         */
        Composite panel = UiBuilder.buildComposite(parent, 2, SWT.NONE,
                UiBuilder.CompositeType.SINGLE_ROW, specifier);
        GridData panelLayoutData = (GridData) panel.getLayoutData();
        panelLayoutData.horizontalAlignment = SWT.LEFT;
        panelLayoutData.grabExcessHorizontalSpace = false;
        panelLayoutData.verticalIndent = verticalIndent;
        label = (text == null ? null : UiBuilder.buildLabel(panel, text,
                specifier));

        /*
         * Create the time delta selector.
         */
        combo = new Combo(panel, SWT.READ_ONLY);
        UiBuilder.ensureMouseWheelEventsPassedUpToAncestor(combo);
        synchronizeWidgetsToState();

        /*
         * Place the time delta selector in the panel's grid.
         */
        GridData gridData = new GridData(SWT.LEFT, SWT.CENTER, true, false);
        gridData.horizontalSpan = (label == null ? 2 : 1);
        combo.setLayoutData(gridData);

        /*
         * Bind the time delta selection event to trigger a change in the record
         * of the state for the widget.
         */
        combo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (otherDeltaTextShowing) {
                    return;
                }
                int index = combo.getSelectionIndex();
                if (index == -1) {
                    return;
                } else {
                    value = TimeDeltaComboComponent.this.timeDeltasForChoices
                            .get(combo.getItem(index));
                }
                notifyListeners();
            }
        });
    }

    // Public Methods

    @Override
    public Label getLabel() {
        return label;
    }

    @Override
    public void setHeight(int height) {
        ((GridData) label.getParent().getLayoutData()).minimumHeight = height;
    }

    @Override
    public void setEnabled(boolean enable) {
        if (label != null) {
            label.setEnabled(enable);
        }
        combo.setEnabled(enable);
    }

    @Override
    public void setEditable(boolean editable, ControlComponentHelper helper) {
        if (editable == false) {
            combo.select(otherDeltaTextShowing ? 0 : choices
                    .indexOf(choicesForTimeDeltas.get(value)));
        }
        combo.getParent().setEnabled(editable);
        combo.setBackground(editable ? null : helper.getBackgroundColor(
                editable, combo, label));
    }

    /**
     * Set the width of the component's combo widget.
     * 
     * @param width
     *            Width in pixels of the component's combo widget.
     */
    public void setComboWidth(int width) {
        ((GridData) combo.getLayoutData()).minimumWidth = width;
    }

    /**
     * Set the time delta value to that specified.
     * 
     * @param value
     *            New time delta value.
     */
    public void setValue(long value) {
        this.value = value;
        synchronizeWidgetsToState();
    }

    /**
     * Determine whether or not the other delta text is currently showing.
     * 
     * @return True if the other delta text is currently showing, false
     *         otherwise.
     */
    public boolean isOtherDeltaTextShowing() {
        return otherDeltaTextShowing;
    }

    /**
     * Get the delta choices in string form.
     * 
     * @return Delta choices in string form.
     */
    public List<String> getChoiceStrings() {
        return new ArrayList<>(choices);
    }

    /**
     * Get the map of delta choice strings to their corresponding time deltas in
     * milliseconds.
     * 
     * @return Map of delta choice strings to their corresponding time deltas in
     *         milliseconds.
     */
    public Map<String, Long> getTimeDeltasForChoices() {
        return new HashMap<>(timeDeltasForChoices);
    }

    /**
     * Set the choices to those specified and synchronize the component widgets
     * to the state given those choices.
     * 
     * @param choices
     *            List of delta choice strings.
     * @param timeDeltasForChoices
     *            Map of delta choice strings to their corresponding time deltas
     *            in milliseconds.
     */
    public void setChoices(List<String> choices,
            Map<String, Long> timeDeltasForChoices) {
        doSetChoices(choices, timeDeltasForChoices);
        synchronizeWidgetsToState();
    }

    // Private Methods

    /**
     * Set the choices to those specified.
     * 
     * @param choices
     *            List of delta choice strings.
     * @param timeDeltasForChoices
     *            Map of delta choice strings to their corresponding time deltas
     *            in milliseconds.
     */
    private void doSetChoices(List<String> choices,
            Map<String, Long> timeDeltasForChoices) {

        /*
         * Remember the new choices, and create a reverse mapping of time deltas
         * to choices.
         */
        this.choices = choices;
        this.timeDeltasForChoices = timeDeltasForChoices;
        this.choicesForTimeDeltas = new HashMap<>(timeDeltasForChoices.size());
        for (Map.Entry<String, Long> entry : timeDeltasForChoices.entrySet()) {
            this.choicesForTimeDeltas.put(entry.getValue(), entry.getKey());
        }

        /*
         * Reset the combo box's items so that any future synchronization of
         * widgets to the current state will repopulate the combo box.
         */
        if ((combo != null) && (combo.isDisposed() == false)) {
            combo.setItems(NO_ITEMS_LIST);
        }
    }

    /**
     * Synchronize the component widgets to the current state.
     */
    private void synchronizeWidgetsToState() {
        if (combo.isDisposed()) {
            return;
        }

        /*
         * Determine whether or not the other delta text should be showing. If
         * this is the first time this method has been called, or if the other
         * delta text should be showing and is not (or should not be showing,
         * but is), repopulate the combo box appropriately.
         */
        boolean newOtherDeltaTextShowing = !choicesForTimeDeltas.keySet()
                .contains(value);
        if ((combo.getItemCount() == 0)
                || (otherDeltaTextShowing != newOtherDeltaTextShowing)) {
            otherDeltaTextShowing = newOtherDeltaTextShowing;
            String[] items = new String[otherDeltaTextShowing ? 1 : choices
                    .size()];
            if (otherDeltaTextShowing) {
                items[0] = OTHER_DELTA_TEXT;
            } else {
                choices.toArray(items);
            }
            combo.setItems(items);
        }

        /*
         * Select the first item if the other delta text is showing, otherwise
         * select the choice that goes with the current time delta.
         */
        combo.select(otherDeltaTextShowing ? 0 : choices
                .indexOf(choicesForTimeDeltas.get(value)));
    }

    /**
     * Notify the state change listeners of a state change.
     */
    private void notifyListeners() {
        holder.valueChanged(identifier, value);
    }
}
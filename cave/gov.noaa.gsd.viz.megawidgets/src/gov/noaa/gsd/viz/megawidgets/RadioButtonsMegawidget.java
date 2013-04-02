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
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * Radio buttons megawidget, providing a series of radio buttons from which the
 * user may choose a single selection.
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
 * @see RadioButtonsSpecifier
 */
public class RadioButtonsMegawidget extends SingleChoiceMegawidget {

    // Private Variables

    /**
     * Label associated with this megawidget, if any.
     */
    private final Label label;

    /**
     * Radio buttons associated with this megawidget.
     */
    private final List<Button> radioButtons;

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
    protected RadioButtonsMegawidget(RadioButtonsSpecifier specifier,
            Composite parent, Map<String, Object> paramMap) {
        super(specifier, paramMap);

        // Create a grouping composite for the widget, so that
        // the radio button components are grouped together in
        // terms of exclusive selection state.
        Composite subParent = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginWidth = 0;
        subParent.setLayout(gridLayout);

        // Place the grouping composite in the grid.
        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
        gridData.horizontalSpan = specifier.getWidth();
        gridData.verticalIndent = specifier.getSpacing();
        subParent.setLayoutData(gridData);

        // Add a label if one is required.
        if ((specifier.getLabel() != null)
                && (specifier.getLabel().length() > 0)) {

            // Create a label widget.
            label = new Label(subParent, SWT.NONE);
            label.setText(specifier.getLabel());
            label.setEnabled(specifier.isEnabled());

            // Place the label in the grid.
            label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
        } else {
            label = null;
        }

        // For each value, add a radio button.
        List<Button> radioButtons = new ArrayList<Button>();
        for (int j = 0; j < specifier.getChoiceNames().size(); j++) {

            // Create the radio button.
            Button radioButton = new Button(subParent, SWT.RADIO);
            radioButton.setText(specifier.getChoiceNames().get(j));
            radioButton.setEnabled(specifier.isEnabled());

            // Place the widget in the grid.
            radioButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
                    false));
            radioButtons.add(radioButton);
        }

        // Bind each radio button selection event to
        // trigger a change in the record of the state
        // for the widget.
        SelectionListener listener = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Button radioButton = (Button) e.widget;
                if (radioButton.getSelection() == false) {
                    return;
                }
                state = ((ChoicesMegawidgetSpecifier) getSpecifier())
                        .getChoiceFromLongVersion(radioButton.getText());
                notifyListener(getSpecifier().getIdentifier(), state);
                notifyListener();
            }
        };
        for (Button radioButton : radioButtons) {
            radioButton.addSelectionListener(listener);
        }
        this.radioButtons = Collections.unmodifiableList(radioButtons);

        // Render the radio buttons uneditable if ne-
        // cessary.
        if (isEditable() == false) {
            doSetEditable(false);
        }
    }

    // Protected Methods

    /**
     * Receive notification that the megawidget's state has changed.
     * 
     * @param state
     *            New state.
     */
    @Override
    protected final void megawidgetStateChanged(String state) {
        state = ((ChoicesMegawidgetSpecifier) getSpecifier())
                .getLongVersionFromChoice(state);
        for (Button radioButton : radioButtons) {
            radioButton.setSelection(radioButton.getText().equals(state));
        }
    }

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
        for (Button radioButton : radioButtons) {
            radioButton.setEnabled(enable);
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
        if (radioButtons.size() > 0) {
            radioButtons.get(0).getParent().setEnabled(editable);
        }
    }
}
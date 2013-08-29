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

import com.google.common.collect.ImmutableList;

/**
 * Checkboxes megawidget, allowing the selection of zero or more choices, each
 * represented visually as a labeled checkbox.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Chris.Golden      Initial induction into repo
 * Apr 30, 2013   1277     Chris.Golden      Added support for mutable properties.
 * 
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see CheckBoxesSpecifier
 */
public class CheckBoxesMegawidget extends MultipleChoicesMegawidget {

    // Private Variables

    /**
     * Label associated with this megawidget, if any.
     */
    private final Label label;

    /**
     * Checkboxes associated with this megawidget.
     */
    private final List<Button> checkBoxes;

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
    protected CheckBoxesMegawidget(CheckBoxesSpecifier specifier,
            Composite parent, Map<String, Object> paramMap) {
        super(specifier, paramMap);

        // Create a panel in which to place the widgets.
        // This is done so that they may be rendered read-
        // only by disabling the panel, which in SWT has
        // the effect of disabling mouse and keyboard in-
        // put for the child widgets without making them
        // look disabled; and because it is required to
        // group the widgets properly into a single mega-
        // widget.
        Composite panel = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(1, true);
        gridLayout.marginWidth = 0;
        panel.setLayout(gridLayout);
        GridData gridData = new GridData(SWT.FILL, SWT.FILL, false, false);
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

            // Place the label in the grid.
            label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
        } else {
            label = null;
        }

        // For each value, add a checkbox.
        List<Button> checkBoxes = new ArrayList<Button>();
        for (Object choice : choices) {

            // Create the checkbox.
            Button checkBox = new Button(panel, SWT.CHECK);
            checkBox.setText(specifier.getNameOfNode(choice));
            checkBox.setData(specifier.getIdentifierOfNode(choice));
            checkBox.setEnabled(specifier.isEnabled());

            // Place the widget in the grid.
            checkBox.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
                    false));
            checkBoxes.add(checkBox);
        }

        // Bind each checkbox selection event to
        // trigger a change in the record of the state
        // for the widget.
        SelectionListener listener = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Button checkBox = (Button) e.widget;
                String choice = (String) checkBox.getData();
                if (checkBox.getSelection()) {
                    state.add(choice);
                } else {
                    state.remove(choice);
                }
                notifyListener(getSpecifier().getIdentifier(), state);
                notifyListener();
            }
        };
        for (Button checkBox : checkBoxes) {
            checkBox.addSelectionListener(listener);
        }
        this.checkBoxes = ImmutableList.copyOf(checkBoxes);

        // Render the checkboxes uneditable if necessary.
        if (isEditable() == false) {
            doSetEditable(false);
        }
    }

    // Protected Methods

    @Override
    protected final boolean isChoicesListMutable() {
        return false;
    }

    @Override
    protected final void prepareForChoicesChange() {
        throw new UnsupportedOperationException(
                "cannot change choices for checkboxes megawidget");
    }

    @Override
    protected final void synchronizeWidgetsToChoices() {
        throw new UnsupportedOperationException(
                "cannot change choices for checkboxes megawidget");
    }

    @Override
    protected final void synchronizeWidgetsToState() {
        for (Button checkBox : checkBoxes) {
            checkBox.setSelection(state.contains(checkBox.getData()));
        }
    }

    @Override
    protected final void doSetEnabled(boolean enable) {
        if (label != null) {
            label.setEnabled(enable);
        }
        for (Button checkBox : checkBoxes) {
            checkBox.setEnabled(enable);
        }
    }

    @Override
    protected final void doSetEditable(boolean editable) {
        if (checkBoxes.size() > 0) {
            checkBoxes.get(0).getParent().setEnabled(editable);
        }
    }
}
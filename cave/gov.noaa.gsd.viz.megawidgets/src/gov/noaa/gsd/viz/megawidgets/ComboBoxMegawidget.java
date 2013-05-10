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

import java.util.HashMap;
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

/**
 * Combo box megawidget, providing a dropdown combo box allowing the selection
 * of a single choice.
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
 * @see ComboBoxSpecifier
 */
public class ComboBoxMegawidget extends SingleChoiceMegawidget {

    // Private Variables

    /**
     * Label associated with this megawidget, if any.
     */
    private final Label label;

    /**
     * Combo box associated with this megawidget.
     */
    private final Combo comboBox;

    /**
     * Map of choice identifiers to their names.
     */
    private final Map<String, String> choiceNamesForIdentifiers = new HashMap<String, String>();

    /**
     * Map of choice names to their identifiers.
     */
    private final Map<String, String> choiceIdentifiersForNames = new HashMap<String, String>();

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
    protected ComboBoxMegawidget(ComboBoxSpecifier specifier, Composite parent,
            Map<String, Object> paramMap) {
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

            // Place the label in the grid.
            gridData = new GridData(SWT.LEFT, SWT.CENTER, false, false);
            label.setLayoutData(gridData);
        } else {
            label = null;
        }

        // Create the combo box.
        comboBox = new Combo(panel, SWT.READ_ONLY);
        populateComboBoxWithChoices();
        comboBox.setEnabled(specifier.isEnabled());

        // Place the combo box in the grid.
        gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gridData.horizontalSpan = (label == null ? 2 : 1);
        comboBox.setLayoutData(gridData);

        // Bind the combo box selection event to trigger
        // a change in the record of the state for the
        // widget.
        comboBox.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Combo comboBox = (Combo) e.widget;
                int index = comboBox.getSelectionIndex();
                if (index == -1) {
                    state = null;
                } else {
                    state = choiceIdentifiersForNames.get(comboBox
                            .getItem(index));
                }
                notifyListener(getSpecifier().getIdentifier(), state);
                notifyListener();
            }
        });

        // Render the combo box uneditable if necessary.
        if (isEditable() == false) {
            doSetEditable(false);
        }
    }

    // Public Methods

    /**
     * Get the available choices hierarchy.
     * 
     * @return Available choices hierarchy.
     */
    public final List<?> getChoices() {
        return doGetChoices();
    }

    /**
     * Set the choices to those specified. If the current state is not a subset
     * of the new choices, the state will be set to <code>null</code>.
     * 
     * @param value
     *            List of new choices.
     * @throws MegawidgetPropertyException
     *             If the choices are invalid.
     */
    public final void setChoices(Object value)
            throws MegawidgetPropertyException {
        doSetChoices(value);
    }

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

    @Override
    protected final boolean isChoicesListMutable() {
        return true;
    }

    @Override
    protected final void prepareForChoicesChange() {

        // No action.
    }

    @Override
    protected final void synchronizeWidgetsToChoices() {

        // Populate the combo box with the new choices.
        populateComboBoxWithChoices();

        // Ensure that the combo box has the right element selected.
        synchronizeWidgetsToState();
    }

    @Override
    protected final void synchronizeWidgetsToState() {
        String selected = choiceNamesForIdentifiers.get(state);
        if (selected != null) {
            for (int j = 0; j < comboBox.getItemCount(); j++) {
                if (comboBox.getItem(j).equals(selected)) {
                    comboBox.select(j);
                    return;
                }
            }
        }
        comboBox.deselectAll();
    }

    @Override
    protected final void doSetEnabled(boolean enable) {
        if (label != null) {
            label.setEnabled(enable);
        }
        comboBox.setEnabled(enable);
    }

    @Override
    protected final void doSetEditable(boolean editable) {
        comboBox.getParent().setEnabled(editable);
        comboBox.setBackground(getBackgroundColor(editable, comboBox, label));
    }

    // Private Methods

    /**
     * Populate the combo box with choices.
     */
    private void populateComboBoxWithChoices() {

        // Create bidirectional associations between the choice
        // names and their identifiers, and compile a list of
        // the names.
        choiceNamesForIdentifiers.clear();
        choiceIdentifiersForNames.clear();
        ComboBoxSpecifier specifier = getSpecifier();
        String[] names = new String[choices.size()];
        int index = 0;
        for (Object choice : choices) {
            String identifier = specifier.getIdentifierOfNode(choice);
            String name = specifier.getNameOfNode(choice);
            choiceNamesForIdentifiers.put(identifier, name);
            choiceIdentifiersForNames.put(name, identifier);
            names[index++] = name;
        }

        // Set the combo box's choices to the list of names
        // compiled above.
        comboBox.setItems(names);
    }
}
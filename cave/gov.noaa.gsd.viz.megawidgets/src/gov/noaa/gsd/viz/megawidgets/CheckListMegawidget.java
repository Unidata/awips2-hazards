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

import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * Checklist megawidget, allowing the selection of zero or more choices, each
 * represented visually as a labeled checkbox in a scrollable list.
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
 * @see CheckListSpecifier
 */
public class CheckListMegawidget extends MultipleChoicesMegawidget {

    // Private Variables

    /**
     * Label associated with this megawidget, if any.
     */
    private final Label label;

    /**
     * Table associated with this megawidget.
     */
    private final Table table;

    /**
     * Select all items button.
     */
    private final Button allButton;

    /**
     * Deslect all items button.
     */
    private final Button noneButton;

    /**
     * Last position of the vertical scrollbar. This is used whenever the
     * choices are being changed via <code>setChoices()</code> or one of the
     * mutable property manipulation methods, in order to keep a similar visual
     * state to what came before.
     */
    private int scrollPosition = 0;

    /**
     * Identifier of the node that was last selected in the choices list. This
     * is used whenever the choices are being changed via <code>setChoices()
     * </code> or one of the mutable property manipulation methods, in order to
     * keep a similar visual state to what came before.
     */
    private String selectedChoiceIdentifier;

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
    protected CheckListMegawidget(CheckListSpecifier specifier,
            Composite parent, Map<String, Object> paramMap) {
        super(specifier, paramMap);

        // Create a panel in which to place the widgets.
        // This is needed in order to group the widgets pro-
        // perly into a single megawidget.
        Composite panel = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(1, true);
        gridLayout.marginWidth = 0;
        panel.setLayout(gridLayout);
        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
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

        // Create a table to hold the checkable choices.
        table = new Table(panel, SWT.CHECK | SWT.BORDER | SWT.FULL_SELECTION);
        table.setLinesVisible(false);
        table.setHeaderVisible(false);
        table.setEnabled(specifier.isEnabled());
        gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        table.setLayoutData(gridData);
        new TableColumn(table, SWT.NONE);

        // Add all the choices to the table.
        createTableItemsForChoices();

        // Determine the height of the table. This must
        // be done after the above to ensure it will have
        // the right height. Unfortunately using either
        // computeSize() or computeTrim() to try to get
        // the extra vertical space required for the
        // borders, etc. seems to return a bizarrely high
        // value (20 even without a header showing), so
        // an arbitrary number of pixels is added in this
        // case as a cheesy workaround.
        gridData.heightHint = (specifier.getNumVisibleLines() * table
                .getItemHeight()) + 7;

        // Add the Select All and Select None buttons, if appropriate.
        if (specifier.shouldShowAllNoneButtons()) {
            Composite allNoneContainer = new Composite(panel, SWT.FILL);
            FillLayout fillLayout = new FillLayout();
            fillLayout.spacing = 10;
            fillLayout.marginWidth = 10;
            fillLayout.marginHeight = 5;
            allNoneContainer.setLayout(fillLayout);
            allButton = new Button(allNoneContainer, SWT.PUSH);
            allButton.setText("  All  ");
            allButton.setEnabled(specifier.isEnabled()
                    && specifier.isEditable());
            allButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    ChoicesMegawidgetSpecifier specifier = getSpecifier();
                    state.clear();
                    for (Object choice : choices) {
                        state.add(specifier.getIdentifierOfNode(choice));
                    }
                    setAllItemsCheckedState(true);
                    notifyListeners();
                }
            });
            noneButton = new Button(allNoneContainer, SWT.PUSH);
            noneButton.setText("  None  ");
            noneButton.setEnabled(specifier.isEnabled()
                    && specifier.isEditable());
            noneButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    state.clear();
                    setAllItemsCheckedState(false);
                    notifyListeners();
                }
            });
            allNoneContainer.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER,
                    true, false));
        } else {
            allButton = noneButton = null;
        }

        // Bind check events to trigger a change in the
        // record of the state for the widget if
        // editable, or to undo the change if read-only.
        table.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {

                // If this is not a check event, do
                // nothing.
                if (e.detail != SWT.CHECK) {
                    return;
                }

                // If the widget is editable, record
                // the state change; otherwise, undo
                // what was just done.
                if (isEditable()) {
                    String choice = (String) ((TableItem) e.item).getData();
                    int index = state.indexOf(choice);
                    if (index == -1) {
                        state.add(choice);
                    } else {
                        state.remove(index);
                    }
                    notifyListeners();
                } else {
                    e.detail = SWT.NONE;
                    e.doit = false;
                    CheckListMegawidget.this.table.setRedraw(false);
                    TableItem item = (TableItem) e.item;
                    item.setChecked(!item.getChecked());
                    CheckListMegawidget.this.table.setRedraw(true);
                }
            }
        });

        // Render the check list uneditable if necessary.
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

    // Protected Methods

    @Override
    protected final boolean isChoicesListMutable() {
        return true;
    }

    @Override
    protected final void prepareForChoicesChange() {

        // Remember the scrollbar position so that it can be approximately
        // restored.
        scrollPosition = table.getVerticalBar().getSelection();

        // Remember the identifier of the currently selected choice, if any.
        TableItem[] selectedItems = table.getSelection();
        selectedChoiceIdentifier = (selectedItems.length > 0 ? (String) selectedItems[0]
                .getData() : null);
    }

    @Override
    protected final void synchronizeWidgetsToChoices() {

        // Remove all the previous table items.
        table.removeAll();

        // Create the new table items.
        createTableItemsForChoices();

        // Select the appropriate choice, if one with the same identifier
        // as the one selected before is found.
        if (selectedChoiceIdentifier != null) {
            for (TableItem item : table.getItems()) {
                if (item.getData().equals(selectedChoiceIdentifier)) {
                    table.setSelection(item);
                    break;
                }
            }
        }

        // Ensure that the new table items are synced with the old state.
        synchronizeWidgetsToState();

        // Set the scrollbar position to be similar to what it was before.
        table.getVerticalBar().setSelection(scrollPosition);
    }

    @Override
    protected final void synchronizeWidgetsToState() {
        for (int line = 0; line < table.getItemCount(); line++) {
            TableItem item = table.getItem(line);
            item.setChecked(state.contains(item.getData()));
        }
    }

    @Override
    protected final void doSetEnabled(boolean enable) {
        if (label != null) {
            label.setEnabled(enable);
        }
        table.setEnabled(enable);
        if (allButton != null) {
            allButton.setEnabled(isEditable() && enable);
            noneButton.setEnabled(isEditable() && enable);
        }
    }

    @Override
    protected final void doSetEditable(boolean editable) {
        table.setBackground(getBackgroundColor(editable, table, label));
        if (allButton != null) {
            allButton.setEnabled(isEnabled() && editable);
            noneButton.setEnabled(isEnabled() && editable);
        }
    }

    // Private Methods

    /**
     * Create the table items for the choices.
     */
    private void createTableItemsForChoices() {
        CheckListSpecifier specifier = getSpecifier();
        for (Object choice : choices) {
            TableItem item = new TableItem(table, SWT.NONE);
            item.setText(0, specifier.getNameOfNode(choice));
            item.setData(specifier.getIdentifierOfNode(choice));
        }
        table.getColumn(0).pack();
    }

    /**
     * Set the checked state for all the items in the list to the specified
     * value.
     * 
     * @param checked
     *            Flag indicating whether or not the items should be checked.
     */
    private void setAllItemsCheckedState(boolean checked) {
        for (TableItem item : table.getItems()) {
            item.setChecked(checked);
        }
    }

    /**
     * Notify any listeners of a state change and invocation.
     */
    private void notifyListeners() {
        notifyListener(getSpecifier().getIdentifier(), state);
        notifyListener();
    }
}
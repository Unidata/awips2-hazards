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
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

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
 * Oct 22, 2013   2168     Chris.Golden      Replaced some GUI creation code with
 *                                           calls to UiBuilder methods to avoid
 *                                           code duplication and encourage uni-
 *                                           form look. Also changed to implement
 *                                           new IControl interface.
 * Oct 31, 2013   2336     Chris.Golden      Changed to accommodate alteration
 *                                           of framework to include notion
 *                                           of bounded (closed set) choices
 *                                           versus unbounded (sets to which
 *                                           arbitrary user-specified choices
 *                                           can be added) choice megawidgets.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see CheckListSpecifier
 */
public class CheckListMegawidget extends MultipleBoundedChoicesMegawidget
        implements IControl {

    // Protected Static Constants

    /**
     * Set of all mutable property names for instances of this class.
     */
    protected static final Set<String> MUTABLE_PROPERTY_NAMES;
    static {
        Set<String> names = Sets
                .newHashSet(MultipleBoundedChoicesMegawidget.MUTABLE_PROPERTY_NAMES_INCLUDING_CHOICES);
        names.add(IControlSpecifier.MEGAWIDGET_EDITABLE);
        MUTABLE_PROPERTY_NAMES = ImmutableSet.copyOf(names);
    };

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
     * Deselect all items button.
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
    protected CheckListMegawidget(CheckListSpecifier specifier,
            Composite parent, Map<String, Object> paramMap) {
        super(specifier, paramMap);
        helper = new ControlComponentHelper(specifier);

        // Create a panel in which to place the widgets and
        // a label, if appropriate.
        Composite panel = UiBuilder.buildComposite(parent, 1, SWT.NONE,
                UiBuilder.CompositeType.MULTI_ROW_VERTICALLY_EXPANDING,
                specifier);
        label = UiBuilder.buildLabel(panel, specifier);

        // Create a table to hold the checkable choices.
        table = new Table(panel, SWT.CHECK | SWT.BORDER | SWT.FULL_SELECTION);
        table.setLinesVisible(false);
        table.setHeaderVisible(false);
        table.setEnabled(specifier.isEnabled());
        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
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

        // Add the Select All and Select None buttons if appropriate.
        List<Button> buttons = UiBuilder.buildAllNoneButtons(panel, specifier,
                new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        BoundedChoicesMegawidgetSpecifier specifier = getSpecifier();
                        state.clear();
                        for (Object choice : choices) {
                            state.add(specifier.getIdentifierOfNode(choice));
                        }
                        setAllItemsCheckedState(true);
                        notifyListeners();
                    }
                }, new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        state.clear();
                        setAllItemsCheckedState(false);
                        notifyListeners();
                    }
                });
        if (buttons.isEmpty() == false) {
            allButton = buttons.get(0);
            noneButton = buttons.get(1);
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

    @Override
    public Set<String> getMutablePropertyNames() {
        return MUTABLE_PROPERTY_NAMES;
    }

    @Override
    public Object getMutableProperty(String name)
            throws MegawidgetPropertyException {
        if (name.equals(IControlSpecifier.MEGAWIDGET_EDITABLE)) {
            return isEditable();
        }
        return super.getMutableProperty(name);
    }

    @Override
    public void setMutableProperty(String name, Object value)
            throws MegawidgetPropertyException {
        if (name.equals(IControlSpecifier.MEGAWIDGET_EDITABLE)) {
            setEditable(getPropertyBooleanValueFromObject(value, name, null));
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
    public final int getLeftDecorationWidth() {
        return 0;
    }

    @Override
    public final void setLeftDecorationWidth(int width) {

        // No action.
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
        table.setBackground(helper.getBackgroundColor(editable, table, label));
        if (allButton != null) {
            allButton.setEnabled(isEnabled() && editable);
            noneButton.setEnabled(isEnabled() && editable);
        }
    }

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
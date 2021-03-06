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

import gov.noaa.gsd.viz.megawidgets.displaysettings.IDisplaySettings;
import gov.noaa.gsd.viz.megawidgets.displaysettings.ListSettings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
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
 * Mar 06, 2014   2155     Chris.Golden      Fixed bug caused by a lack of
 *                                           defensive copying of the state when
 *                                           notifying a state change listener of
 *                                           a change. Also fixed Javadoc and
 *                                           took advantage of new JDK 1.7
 *                                           features.
 * Apr 24, 2014   2925     Chris.Golden      Changed to work with new validator
 *                                           package, updated Javadoc and other
 *                                           comments.
 * Jun 17, 2014   3982     Chris.Golden      Changed to deselect any selected
 *                                           items before being disabled.
 * Jun 24, 2014   4010     Chris.Golden      Changed to no longer be a subclass
 *                                           of NotifierMegawidget.
 * Jun 24, 2014   4023     Chris.Golden      Changed to prune old state to new
 *                                           choices when available choices are
 *                                           changed.
 * Feb 17, 2015   4756     Chris.Golden      Added display settings saving and
 *                                           restoration.
 * Feb 22, 2015   4756     Chris.Golden      Fixed bug causing null pointer
 *                                           exception in setDisplaySettings()
 *                                           under certain conditions.
 * Mar 31, 2015   6873     Chris.Golden      Added code to ensure that mouse
 *                                           wheel events are not processed by
 *                                           the megawidget, but are instead
 *                                           passed up to any ancestor that is a
 *                                           scrolled composite.
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
        Set<String> names = new HashSet<>(
                MultipleBoundedChoicesMegawidget.MUTABLE_PROPERTY_NAMES_INCLUDING_CHOICES);
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
     * choices are being changed via {@link #setChoices(Object)} or one of the
     * mutable property manipulation methods, in order to keep a similar visual
     * state to what came before.
     */
    private int scrollPosition = 0;

    /**
     * Identifier of the node that was last selected in the choices list. This
     * is used whenever the choices are being changed via
     * {@link #setChoices(Object)} </code> or one of the mutable property
     * manipulation methods, in order to keep a similar visual state to what
     * came before.
     */
    private String selectedChoiceIdentifier;

    /**
     * Control component helper.
     */
    private final ControlComponentHelper helper;

    /**
     * Display settings.
     */
    private final ListSettings<String> displaySettings = new ListSettings<>(
            getClass());

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

        /*
         * Create a panel in which to place the widgets and a label, if
         * appropriate.
         */
        Composite panel = UiBuilder.buildComposite(parent, 1, SWT.NONE,
                UiBuilder.CompositeType.MULTI_ROW_VERTICALLY_EXPANDING,
                specifier);
        label = UiBuilder.buildLabel(panel, specifier);

        /*
         * Create a table to hold the checkable choices.
         */
        table = new Table(panel, SWT.CHECK | SWT.BORDER | SWT.FULL_SELECTION);
        table.setLinesVisible(false);
        table.setHeaderVisible(false);
        table.setEnabled(specifier.isEnabled());
        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        table.setLayoutData(gridData);
        new TableColumn(table, SWT.NONE);
        UiBuilder.ensureMouseWheelEventsPassedUpToAncestor(table);

        /*
         * Add all the choices to the table.
         */
        createTableItemsForChoices();

        /*
         * Determine the height of the table. This must be done after the above
         * to ensure it will have the right height. Unfortunately using either
         * computeSize() or computeTrim() to try to get the extra vertical space
         * required for the borders, etc. seems to return a bizarrely high value
         * (20 even without a header showing), so an arbitrary number of pixels
         * is added in this case as a cheesy workaround.
         */
        gridData.heightHint = (specifier.getNumVisibleLines() * table
                .getItemHeight()) + 7;

        /*
         * Add the Select All and Select None buttons if appropriate.
         */
        List<Button> buttons = UiBuilder.buildAllNoneButtons(panel, specifier,
                new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        BoundedChoicesMegawidgetSpecifier<?> specifier = getSpecifier();
                        state.clear();
                        for (Object choice : getStateValidator()
                                .getAvailableChoices()) {
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

        /*
         * Bind check events to trigger a change in the record of the state for
         * the widget if editable, or to undo the change if read-only. Also bind
         * selection events to record the new selection.
         */
        table.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {

                /*
                 * If this is not a check event, record the current selection
                 * and do nothing more.
                 */
                if (e.detail != SWT.CHECK) {
                    Set<String> selectedChoices = new HashSet<>(table
                            .getSelectionCount(), 1.0f);
                    for (TableItem item : table.getSelection()) {
                        selectedChoices.add((String) item.getData());
                    }
                    displaySettings.setSelectedChoices(selectedChoices);
                    return;
                }

                /*
                 * If the widget is editable, record the state change;
                 * otherwise, undo what was just done.
                 */
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

        /*
         * Bind scrollbar movements to record the topmost item in the list.
         */
        table.getVerticalBar().addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                recordTopmostVisibleChoice();
            }
        });

        /*
         * Render the check list uneditable if necessary.
         */
        if (isEditable() == false) {
            doSetEditable(false);
        }

        /*
         * Synchronize user-facing widgets to the starting state.
         */
        synchronizeComponentWidgetsToState();
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
            setEditable(ConversionUtilities.getPropertyBooleanValueFromObject(
                    getSpecifier().getIdentifier(), getSpecifier().getType(),
                    value, name, null));
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

        /*
         * No action.
         */
    }

    @Override
    public final int getRightDecorationWidth() {
        return 0;
    }

    @Override
    public final void setRightDecorationWidth(int width) {

        /*
         * No action.
         */
    }

    /**
     * Set the choices to those specified. The current state will be pruned of
     * any elements that are not found within the new choices.
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

    @Override
    public IDisplaySettings getDisplaySettings() {
        return displaySettings;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setDisplaySettings(IDisplaySettings displaySettings) {
        if ((displaySettings.getMegawidgetClass() == getClass())
                && (displaySettings instanceof ListSettings)) {
            final ListSettings<String> listSettings = (ListSettings<String>) displaySettings;
            Display.getDefault().asyncExec(new Runnable() {

                @Override
                public void run() {
                    if (table.isDisposed() == false) {

                        /*
                         * Set any choices that exist now and that were selected
                         * (as in selected within the GUI, not checked) before
                         * to be selected now.
                         */
                        Map<String, Integer> indicesForChoices = getIndicesForChoices();
                        Set<String> selectedChoices = listSettings
                                .getSelectedChoices();
                        if ((selectedChoices != null)
                                && (selectedChoices.isEmpty() == false)) {
                            int[] indices = UiBuilder.getIndicesOfChoices(
                                    selectedChoices, indicesForChoices);
                            table.setSelection(indices);
                            selectedChoices = new HashSet<>(indices.length);
                            for (int index : indices) {
                                selectedChoices.add((String) table.getItem(
                                        index).getData());
                            }
                            CheckListMegawidget.this.displaySettings
                                    .setSelectedChoices(selectedChoices);
                        }

                        /*
                         * Set the topmost visible choice in the scrollable
                         * viewport to be what it was before if the latter is
                         * found in the available choices.
                         */
                        String topmostChoice = listSettings
                                .getTopmostVisibleChoice();
                        if (topmostChoice != null) {
                            Integer index = indicesForChoices
                                    .get(topmostChoice);
                            if (index != null) {
                                table.setTopIndex(index);
                                CheckListMegawidget.this.displaySettings
                                        .setTopmostVisibleChoice((String) table
                                                .getItem(index).getData());
                            }
                        }
                    }
                }
            });
        }
    }

    // Protected Methods

    @Override
    protected final boolean isChoicesListMutable() {
        return true;
    }

    @Override
    protected final void prepareForChoicesChange() {

        /*
         * Remember the scrollbar position so that it can be approximately
         * restored.
         */
        scrollPosition = table.getVerticalBar().getSelection();

        /*
         * Remember the identifier of the currently selected choice, if any.
         */
        TableItem[] selectedItems = table.getSelection();
        selectedChoiceIdentifier = (selectedItems.length > 0 ? (String) selectedItems[0]
                .getData() : null);
    }

    @Override
    protected void cancelPreparationForChoicesChange() {

        /*
         * No action.
         */
    }

    @Override
    protected final void synchronizeComponentWidgetsToChoices() {

        /*
         * Remove all the previous table items.
         */
        table.removeAll();

        /*
         * Create the new table items.
         */
        createTableItemsForChoices();

        /*
         * Ensure that the new table items are synced with the old state.
         */
        synchronizeComponentWidgetsToState();

        /*
         * Select the appropriate choice, if one with the same identifier as the
         * one selected before is found.
         */
        displaySettings.setSelectedChoices(null);
        if (selectedChoiceIdentifier != null) {
            for (TableItem item : table.getItems()) {
                if (item.getData().equals(selectedChoiceIdentifier)) {
                    table.setSelection(item);
                    displaySettings.setSelectedChoices(Sets
                            .newHashSet(selectedChoiceIdentifier));
                    break;
                }
            }
        }

        /*
         * Set the scrollbar position to be similar to what it was before.
         */
        table.getVerticalBar().setSelection(scrollPosition);

        /*
         * Record the topmost visible choice.
         */
        recordTopmostVisibleChoice();
    }

    @Override
    protected final void doSynchronizeComponentWidgetsToState() {

        /*
         * Set the selected items to be checked.
         */
        for (int line = 0; line < table.getItemCount(); line++) {
            TableItem item = table.getItem(line);
            item.setChecked(state.contains(item.getData()));
        }

        /*
         * Update the display setting records to match the new state.
         */
        displaySettings.setSelectedChoices(null);
        recordTopmostVisibleChoice();
    }

    @Override
    protected final void doSetEnabled(boolean enable) {
        if (label != null) {
            label.setEnabled(enable);
        }
        if (enable == false) {
            table.setSelection(UiBuilder.NO_SELECTION);
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
        for (Object choice : getStateValidator().getAvailableChoices()) {
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
     * Record the topmost visible choice in the display settings.
     */
    private void recordTopmostVisibleChoice() {
        if (table.getItemCount() > 0) {
            displaySettings.setTopmostVisibleChoice((String) table.getItem(
                    table.getTopIndex()).getData());
        } else {
            displaySettings.setTopmostVisibleChoice(null);
        }
    }

    /**
     * Get a map of the available choices to their indices.
     * 
     * @return Map of available choices to their indices.
     */
    private Map<String, Integer> getIndicesForChoices() {
        List<?> availableChoices = getStateValidator().getAvailableChoices();
        Map<String, Integer> indicesForChoices = new HashMap<>(
                availableChoices.size(), 1.0f);
        CheckListSpecifier specifier = getSpecifier();
        for (int j = 0; j < availableChoices.size(); j++) {
            indicesForChoices.put(
                    specifier.getIdentifierOfNode(availableChoices.get(j)), j);
        }
        return indicesForChoices;
    }

    /**
     * Notify any listeners of a state change and invocation.
     */
    private void notifyListeners() {
        notifyListener(getSpecifier().getIdentifier(), new ArrayList<>(state));
    }
}
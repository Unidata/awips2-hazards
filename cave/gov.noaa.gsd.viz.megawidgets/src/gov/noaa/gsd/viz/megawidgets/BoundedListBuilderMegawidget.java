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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.raytheon.viz.ui.widgets.duallist.ButtonImages;

/**
 * Bounded list builder megawidget, a megawidget that allows the user to build
 * up an orderable list from a closed set of choices.
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
 *                                           form look, and changed to implement
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
 * @see BoundedListBuilderSpecifier
 */
public class BoundedListBuilderMegawidget extends
        MultipleBoundedChoicesMegawidget implements IControl {

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

    // Private Static Constants

    /**
     * Drag and drop transfer types that are valid for the lists.
     */
    private static final Transfer[] DRAG_AND_DROP_TRANSFER_TYPES = new Transfer[] { TextTransfer
            .getInstance() };

    // Private Variables

    /**
     * Label for the list of available choices, if any.
     */
    private final Label availableLabel;

    /**
     * Label for the list of selected choices, if any.
     */
    private final Label selectedLabel;

    /**
     * List of available choices.
     */
    private final Table availableTable;

    /**
     * List of selected choices.
     */
    private final Table selectedTable;

    /**
     * Add all button.
     */
    private final Button addAll;

    /**
     * Add selected button.
     */
    private final Button addSelected;

    /**
     * Remove all button.
     */
    private final Button removeAll;

    /**
     * Remove selected button.
     */
    private final Button removeSelected;

    /**
     * Move up selected button.
     */
    private final Button moveUp;

    /**
     * Move down selected button.
     */
    private final Button moveDown;

    /**
     * Button images supplier.
     */
    private final ButtonImages imagesSupplier;

    /**
     * Available list drag source.
     */
    private final DragSource availableListDragSource;

    /**
     * Selected list drag source.
     */
    private final DragSource selectedListDragSource;

    /**
     * Available list drop target.
     */
    private final DropTarget availableListDropTarget;

    /**
     * Selected list drop target.
     */
    private final DropTarget selectedListDropTarget;

    /**
     * List currently acting as a drag source, if any.
     */
    private Table dragSourceList = null;

    /**
     * List currently acting as a drop target, if any.
     */
    private Table dropTargetList = null;

    /**
     * Map of choice identifiers to their names.
     */
    private final Map<String, String> choiceNamesForIdentifiers = Maps
            .newHashMap();

    /**
     * Last position of the available table's vertical scrollbar. This is used
     * whenever the choices are being changed via <code>setChoices()</code> or
     * one of the mutable property manipulation methods, in order to keep a
     * similar visual state to what came before.
     */
    private int availableScrollPosition = 0;

    /**
     * Last position of the selected table's vertical scrollbar. This is used
     * whenever the choices are being changed via <code>setChoices()</code> or
     * one of the mutable property manipulation methods, in order to keep a
     * similar visual state to what came before.
     */
    private int selectedScrollPosition = 0;

    /**
     * Set of choices in the available table that were last selected. This is
     * used whenever the choices are being changed via <code>setChoices()</code>
     * or one of the mutable property manipulation methods, in order to keep a
     * similar visual state to what came before.
     */
    private final List<String> selectedAvailableChoiceIdentifiers = Lists
            .newArrayList();

    /**
     * List of choices in the selected table that were last selected. This is
     * used whenever the choices are being changed via <code>setChoices()</code>
     * or one of the mutable property manipulation methods, in order to keep a
     * similar visual state to what came before.
     */
    private final List<String> selectedSelectedChoiceIdentifiers = Lists
            .newArrayList();

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
    protected BoundedListBuilderMegawidget(
            BoundedListBuilderSpecifier specifier, Composite parent,
            Map<String, Object> paramMap) {
        super(specifier, paramMap);
        helper = new ControlComponentHelper(specifier);

        // Create the panel that will contain the components,
        // and customize its horizontal spacing to be more
        // appropriate for this megawidget.
        Composite panel = UiBuilder.buildComposite(parent, 4, SWT.NONE,
                UiBuilder.CompositeType.MULTI_ROW_VERTICALLY_EXPANDING,
                specifier);
        ((GridLayout) panel.getLayout()).horizontalSpacing = 13;

        // Create the button images supplier.
        imagesSupplier = new ButtonImages(panel);

        // Determine which labels are needed.
        String availableText = ((specifier.getLabel() != null)
                && (specifier.getLabel().length() > 0) ? specifier.getLabel()
                : null);
        String selectedText = ((specifier.getSelectedLabel() != null)
                && (specifier.getSelectedLabel().length() > 0) ? specifier
                .getSelectedLabel() : null);

        // Add a label for the available items list if one
        // is required.
        availableLabel = (availableText != null ? UiBuilder.buildLabel(panel,
                availableText, specifier) : null);

        // If at least one label is being used, add a
        // spacer to fill the space between the labels,
        // or to the right of the available items label
        // if only that is showing, or to the left of
        // the selected items label if only that is
        // showing.
        if ((availableText != null) || (selectedText != null)) {

            // Create a spacer widget.
            Composite spacer = new Composite(panel, SWT.NONE);

            // Place the spacer in the grid; if both the
            // available and selected lists are labeled,
            // the spacer only needs to be between them;
            // otherwise, if only the available list is
            // labeled, then the spacer needs to span
            // three columns (all columns to the right
            // of the available list); otherwise, only
            // the seledcted list is labeled, meaning
            // the two columns to the left of that label
            // must be filled by the spacer.
            GridData gridData = new GridData(SWT.LEFT, SWT.CENTER, false, false);
            gridData.horizontalSpan = ((availableText != null)
                    && (selectedText != null) ? 1 : (availableText != null ? 3
                    : 2));
            gridData.widthHint = gridData.heightHint = 1;
            spacer.setLayoutData(gridData);
        }

        // Add a label for the selected items list and a
        // spacer to its right if the label is appropriate.
        if (selectedText != null) {

            // Create the label.
            selectedLabel = UiBuilder
                    .buildLabel(panel, selectedText, specifier);

            // Create a spacer widget.
            Composite spacer = new Composite(panel, SWT.NONE);

            // Place the spacer in the grid.
            GridData gridData = new GridData(SWT.LEFT, SWT.CENTER, false, false);
            gridData.widthHint = gridData.heightHint = 1;
            spacer.setLayoutData(gridData);
        } else {
            selectedLabel = null;
        }

        // Associate choice identifiers with their names.
        associateChoiceIdentifiersWithNames();

        // Create the table selection listener.
        SelectionListener listListener = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (isEditable()) {
                    enableOrDisableButtons();
                }
            }
        };

        // Create the available items table.
        availableTable = buildTable(panel, listListener, specifier);
        TableColumn column = availableTable.getColumn(0);
        for (Object choice : choices) {
            TableItem item = new TableItem(availableTable, SWT.NONE);
            item.setText(0, specifier.getNameOfNode(choice));
            item.setData(specifier.getIdentifierOfNode(choice));
        }
        column.pack();

        // Create a panel to hold the buttons between the
        // two lists.
        Composite middlePanel = buildButtonPanel(panel);

        // Create the button listener.
        SelectionListener buttonListener = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {

                // Perform the appropriate action.
                if (e.widget == addAll) {
                    addAll();
                } else if (e.widget == addSelected) {
                    addSelectedAtIndex(getLastSelectedIndex());
                } else if (e.widget == removeAll) {
                    removeAll();
                } else if (e.widget == removeSelected) {
                    removeSelected();
                } else if (e.widget == moveUp) {
                    moveUp();
                } else if (e.widget == moveDown) {
                    moveDown();
                } else {
                    return;
                }

                // Enable and disable buttons as appro-
                // priate.
                enableOrDisableButtons();

                // Change the state accordingly.
                megawidgetWidgetsChanged();
            }
        };

        // Create the add and remove buttons.
        addAll = buildButton(middlePanel, ButtonImages.ButtonImage.AddAll,
                buttonListener);
        addSelected = buildButton(middlePanel, ButtonImages.ButtonImage.Add,
                buttonListener);
        removeSelected = buildButton(middlePanel,
                ButtonImages.ButtonImage.Remove, buttonListener);
        removeAll = buildButton(middlePanel,
                ButtonImages.ButtonImage.RemoveAll, buttonListener);

        // Create the selected items list. As with the
        // available items list, a table is used instead
        // of a list due to the additional functionality
        // provided in SWT by tables over lists.
        selectedTable = buildTable(panel, listListener, specifier);

        // Create a panel to hold the buttons to the right
        // of the selected list.
        Composite rightPanel = buildButtonPanel(panel);

        // Create the move up and down buttons.
        moveUp = buildButton(rightPanel, ButtonImages.ButtonImage.Up,
                buttonListener);
        moveDown = buildButton(rightPanel, ButtonImages.ButtonImage.Down,
                buttonListener);

        // Set the state of the buttons appropriately.
        enableOrDisableButtons();

        // Create a drag listener that will listen for
        // drag events occurring in either list, and
        // respond to them appropriately.
        DragSourceListener dragListener = new DragSourceListener() {
            @Override
            public void dragStart(DragSourceEvent event) {

                // Do not initiate a drag if nothing
                // is selected. Otherwise, remember
                // which list is the source.
                Table sourceList = (event.widget == selectedListDragSource ? selectedTable
                        : availableTable);
                if (sourceList.getSelectionCount() == 0) {
                    event.doit = false;
                } else {
                    dragSourceList = sourceList;
                }
            }

            @Override
            public void dragSetData(DragSourceEvent event) {

                // Only set the data if the data type
                // asked for is text.
                if ((dragSourceList != null)
                        && TextTransfer.getInstance().isSupportedType(
                                event.dataType)) {

                    // Supply an small string if the
                    // drop target is one of the lists
                    // within this megawidget, as they
                    // handle drops themselves. Only
                    // supply a text list of all se-
                    // lected items if the drop target
                    // is elsewhere. The small (non-
                    // zero-length) string is required
                    // in the former case because an
                    // empty string causes an excep-
                    // tion to be thrown.
                    if (dropTargetList == null) {
                        StringBuilder buffer = new StringBuilder();
                        for (String choice : getItemsFromList(dragSourceList,
                                true, false, null)) {
                            if (buffer.length() > 0) {
                                buffer.append("\n");
                            }
                            buffer.append(choice);
                        }
                        event.data = buffer.toString();
                    } else {
                        event.data = " ";
                    }
                }
            }

            @Override
            public void dragFinished(DragSourceEvent event) {

                // Clear the source list reference.
                dragSourceList = null;
            }
        };

        // Create a drop listener that will listen for
        // drop events occurring in either list, and
        // respond accordingly.
        DropTargetListener dropListener = new DropTargetListener() {
            @Override
            public void dragEnter(DropTargetEvent event) {

                // If the source of the drag is the
                // other list, or the source and the
                // target list are both the selected
                // list, then accept this as a poten-
                // tial drop; otherwise, reject it.
                event.detail = DND.DROP_NONE;
                Table dropList = (event.widget == selectedListDropTarget ? selectedTable
                        : availableTable);
                if ((dragSourceList != null)
                        && ((dropList == selectedTable) || (dragSourceList == selectedTable))) {

                    // Find the right data type for
                    // transfer before proceeding.
                    for (TransferData dataType : event.dataTypes) {
                        if (DRAG_AND_DROP_TRANSFER_TYPES[0]
                                .isSupportedType(dataType)) {

                            // Note what sort of drop
                            // is permitted, the data
                            // type allowed, and
                            // which list is the drop
                            // target.
                            event.detail = DND.DROP_MOVE;
                            event.currentDataType = dataType;
                            dropTargetList = dropList;
                            break;
                        }
                    }
                }
            }

            @Override
            public void dragLeave(DropTargetEvent event) {

                // Clear the drop target list reference.
                dropTargetList = null;
            }

            @Override
            public void dragOperationChanged(DropTargetEvent event) {

                // No action.
            }

            @Override
            public void dragOver(DropTargetEvent event) {

                // If the drop target is the selected
                // list, allow it to scroll if the
                // drag is close to its top or bottom,
                // and indicate via visuals that the
                // potential drop point would be after
                // the current item.
                if (dropTargetList == selectedTable) {
                    event.feedback = DND.FEEDBACK_INSERT_AFTER
                            + DND.FEEDBACK_SCROLL;
                }
            }

            @Override
            public void drop(DropTargetEvent event) {

                // Make sure there is still a drop
                // target list; the drop could have
                // been cancelled due to an asyn-
                // chronous setting of state, etc.
                if (dropTargetList == null) {
                    return;
                }

                // If target list is the selected
                // list, add items to it; otherwise,
                // remove items from the selected
                // list, since the source is always
                // the selected list if the target is
                // the available list.
                if (dropTargetList == selectedTable) {
                    int index = (event.item == null ? selectedTable
                            .getItemCount() - 1 : selectedTable
                            .indexOf((TableItem) event.item));

                    // If the source is also the se-
                    // lected list, move the list's
                    // selected items from the old
                    // position to the new one;
                    // otherwise, add the items to
                    // the selected list.
                    if (dragSourceList == selectedTable) {
                        moveToIndex(index);
                    } else {
                        addSelectedAtIndex(index);
                    }
                } else {
                    removeSelected();
                }

                // Clear the drop target list reference.
                dropTargetList = null;

                // Enable and disable buttons as appro-
                // priate.
                enableOrDisableButtons();

                // Change the state accordingly.
                megawidgetWidgetsChanged();
            }

            @Override
            public void dropAccept(DropTargetEvent event) {

                // Remember which list is being dropped
                // over.
                dropTargetList = (event.widget == selectedListDropTarget ? selectedTable
                        : availableTable);
            }
        };

        // Create a drag source for each of the lists,
        // so that they may act as sources for drag and
        // drop actions.
        availableListDragSource = new DragSource(availableTable, DND.DROP_MOVE
                + DND.DROP_COPY);
        availableListDragSource.setTransfer(DRAG_AND_DROP_TRANSFER_TYPES);
        availableTable.setDragDetect(true);
        availableListDragSource.addDragListener(dragListener);

        selectedListDragSource = new DragSource(selectedTable, DND.DROP_MOVE
                + DND.DROP_COPY);
        selectedListDragSource.setTransfer(DRAG_AND_DROP_TRANSFER_TYPES);
        selectedTable.setDragDetect(true);
        selectedListDragSource.addDragListener(dragListener);

        // Create a drop target for each of the lists,
        // so that they may act as targets for drag and
        // drop actions.
        availableListDropTarget = new DropTarget(availableTable, DND.DROP_MOVE);
        availableListDropTarget.setTransfer(DRAG_AND_DROP_TRANSFER_TYPES);
        availableListDropTarget.addDropListener(dropListener);

        selectedListDropTarget = new DropTarget(selectedTable, DND.DROP_MOVE);
        selectedListDropTarget.setTransfer(DRAG_AND_DROP_TRANSFER_TYPES);
        selectedListDropTarget.addDropListener(dropListener);

        // Render the widgets uneditable if necessary.
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

        // Remember the scrollbar positions so that they can be approximately
        // restored.
        availableScrollPosition = availableTable.getVerticalBar()
                .getSelection();
        selectedScrollPosition = selectedTable.getVerticalBar().getSelection();

        // Remember the identifiers of the currently selected choices for
        // each table, if any.
        getItemsFromList(availableTable, true, true,
                selectedAvailableChoiceIdentifiers);
        getItemsFromList(selectedTable, true, true,
                selectedSelectedChoiceIdentifiers);
    }

    @Override
    protected final void synchronizeWidgetsToChoices() {

        // If a drag is mid-process, cancel it.
        dragSourceList = dropTargetList = null;

        // Create the mapping of choice identifiers to names.
        associateChoiceIdentifiersWithNames();

        // Synchronize the widgets with the current state, as this will
        // populate the two tables appropriately.
        synchronizeWidgetsToState();

        // For each of the tables, see what items were selected previously
        // that are present in the new item list for that table, and select
        // those items.
        Table[] tables = { availableTable, selectedTable };
        for (Table table : tables) {
            List<String> selectedChoiceIdentifiers = (table == availableTable ? selectedAvailableChoiceIdentifiers
                    : selectedSelectedChoiceIdentifiers);
            List<TableItem> selectedTableItems = Lists.newArrayList();
            for (TableItem item : table.getItems()) {
                if (selectedChoiceIdentifiers.contains(item.getData())) {
                    selectedTableItems.add(item);
                }
            }
            if (selectedTableItems.size() > 0) {
                table.setSelection(selectedTableItems
                        .toArray(new TableItem[selectedTableItems.size()]));
            }
        }

        // Clear the selected choices lists, as they are no longer needed.
        selectedAvailableChoiceIdentifiers.clear();
        selectedSelectedChoiceIdentifiers.clear();

        // Set the scrollbar positions to be similar to what it was before.
        availableTable.getVerticalBar().setSelection(availableScrollPosition);
        selectedTable.getVerticalBar().setSelection(selectedScrollPosition);
    }

    @Override
    protected final void synchronizeWidgetsToState() {

        // If a drag is mid-process, cancel it.
        dragSourceList = dropTargetList = null;

        // Get a list of the choice identifiers, and set the
        // selected list's contents to match it.
        selectedTable.removeAll();
        for (String choice : state) {
            TableItem item = new TableItem(selectedTable, SWT.NONE);
            item.setText(0, choiceNamesForIdentifiers.get(choice));
            item.setData(choice);
        }
        selectedTable.getColumn(0).pack();

        // Determine which choices are left over, and
        // set the available list's contents to match.
        availableTable.removeAll();
        BoundedListBuilderSpecifier specifier = getSpecifier();
        for (Object choice : choices) {
            String identifier = specifier.getIdentifierOfNode(choice);
            if (state.contains(identifier) == false) {
                TableItem item = new TableItem(availableTable, SWT.NONE);
                item.setText(0, specifier.getNameOfNode(choice));
                item.setData(identifier);
            }
        }
        availableTable.getColumn(0).pack();

        // Enable or disable buttons as appropriate.
        enableOrDisableButtons();
    }

    @Override
    protected void doSetEnabled(boolean enable) {
        if (availableLabel != null) {
            availableLabel.setEnabled(enable);
        }
        if (selectedLabel != null) {
            selectedLabel.setEnabled(enable);
        }
        availableTable.setEnabled(false);
        selectedTable.setEnabled(false);
        enableOrDisableButtons();
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
        Label label = (availableLabel != null ? availableLabel : selectedLabel);
        availableTable.setBackground(helper.getBackgroundColor(editable,
                availableTable, label));
        selectedTable.setBackground(helper.getBackgroundColor(editable,
                selectedTable, label));
        enableOrDisableButtons();
    }

    /**
     * Create a table.
     * 
     * @param parent
     *            Parent composite.
     * @param listener
     *            Selection listener for the table.
     * @param specifier
     *            Megawidget specifier.
     * @return New table.
     */
    private Table buildTable(Composite parent, SelectionListener listener,
            BoundedListBuilderSpecifier specifier) {

        // Create the list. A table is used because tables
        // offer functionality like being able to determine
        // what row lies under a given point.
        Table table = new Table(parent, SWT.BORDER + SWT.MULTI
                + SWT.FULL_SELECTION);
        table.setHeaderVisible(false);
        table.setLinesVisible(false);
        table.setEnabled(specifier.isEnabled());
        new TableColumn(table, SWT.NONE);
        table.addSelectionListener(listener);
        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        table.setLayoutData(gridData);

        // Determine the height of the list. This must
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

        // Return the result.
        return table;
    }

    /**
     * Build a button panel.
     * 
     * @param parent
     *            Parent composite.
     * @return New button panel.
     */
    private Composite buildButtonPanel(Composite parent) {
        Composite panel = new Composite(parent, SWT.NONE);
        FillLayout fillLayout = new FillLayout(SWT.VERTICAL);
        fillLayout.spacing = 5;
        panel.setLayout(fillLayout);
        GridData gridData = new GridData(SWT.CENTER, SWT.CENTER, false, true);
        gridData.widthHint = 50;
        panel.setLayoutData(gridData);
        return panel;
    }

    /**
     * Create a button.
     * 
     * @param parent
     *            Parent composite.
     * @param image
     *            Image to be displayed.
     * @param listener
     *            Listener to be notified of button invocations.
     * @return New button.
     */
    private Button buildButton(Composite parent,
            ButtonImages.ButtonImage image, SelectionListener listener) {
        Button button = new Button(parent, SWT.PUSH);
        button.setImage(imagesSupplier.getImage(image));
        button.addSelectionListener(listener);
        return button;
    }

    /**
     * Associate choice identifiers with names.
     */
    private void associateChoiceIdentifiersWithNames() {
        choiceNamesForIdentifiers.clear();
        BoundedListBuilderSpecifier specifier = getSpecifier();
        for (Object choice : choices) {
            choiceNamesForIdentifiers.put(
                    specifier.getIdentifierOfNode(choice),
                    specifier.getNameOfNode(choice));
        }
    }

    /**
     * Update the buttons enabled state as is appropriate to the current states
     * of the other widgets.
     */
    private void enableOrDisableButtons() {

        // If the megawidget is disabled or read-
        // only, disable all the buttons; other-
        // wise, enable or disable each one as
        // appropriate given the items selected
        // in the lists.
        if ((isEnabled() == false) || (isEditable() == false)) {
            addAll.setEnabled(false);
            addSelected.setEnabled(false);
            removeAll.setEnabled(false);
            removeSelected.setEnabled(false);
            moveUp.setEnabled(false);
            moveDown.setEnabled(false);
        } else {

            // If there are items in the avail-
            // able list, enable the Add All
            // button.
            addAll.setEnabled(availableTable.getItemCount() > 0);

            // If there are selected items in
            // the available list, enable the
            // Add Selected button.
            addSelected.setEnabled(availableTable.getSelectionCount() > 0);

            // If there are items in the se-
            // lected list, enable the Remove
            // All button.
            removeAll.setEnabled(selectedTable.getItemCount() > 0);

            // If there are selected items in
            // the selected list, enable the
            // Remove Selected button.
            removeSelected.setEnabled(selectedTable.getSelectionCount() > 0);

            // If items are selected within the
            // selected list, enable the up and
            // down buttons as appropriate;
            // otherwise, disable them.
            if ((selectedTable.getItemCount() > 0)
                    && (selectedTable.getSelectionCount() > 0)) {

                // Find the highest and lowest
                // selected indices.
                int[] selected = selectedTable.getSelectionIndices();
                int highestIndex = -1, lowestIndex = selectedTable
                        .getItemCount();
                for (int index : selected) {
                    if (index > highestIndex) {
                        highestIndex = index;
                    }
                    if (index < lowestIndex) {
                        lowestIndex = index;
                    }
                }

                // Enable the Move Up button if
                // the lowest selected index is
                // not at the start of the
                // list, and enable the Move
                // Down button if the highest
                // selected index is not at the
                // end of the list.
                moveUp.setEnabled(lowestIndex > 0);
                moveDown.setEnabled(highestIndex < selectedTable.getItemCount() - 1);
            } else {
                moveUp.setEnabled(false);
                moveDown.setEnabled(false);
            }
        }
    }

    /**
     * Add all available items to the selected list.
     */
    private void addAll() {

        // Add all available items to the selected list.
        addItems(getItemsFromList(availableTable, false, true, null),
                getLastSelectedIndex());

        // Remove all available items from the available
        // list.
        availableTable.removeAll();

        // Show the selection in the selected list.
        selectedTable.showSelection();
    }

    /**
     * Add selected available items to the selected list after the specified
     * index.
     * 
     * @param index
     *            Index after which to add the selected available items.
     */
    private void addSelectedAtIndex(int index) {

        // Get the index to be selected in the available
        // list after the selected items are removed, if
        // any, as well as getting a list of the items
        // to be removed in ascending index order.
        List<String> identifiers = Lists.newArrayList();
        int firstUnselectedAfterSelected = getIndexOfFirstUnselectedAfterSelected(
                availableTable, identifiers);

        // Add the items to the selected list.
        addItems(identifiers, index);

        // Get the indices that are currently selected,
        // so that they may be removed.
        int[] indices = availableTable.getSelectionIndices();

        // If an item was found to be selected, select
        // it now.
        if (firstUnselectedAfterSelected > -1) {
            availableTable.setSelection(firstUnselectedAfterSelected);
        }

        // Remove all the items that were moved from
        // the available list.
        availableTable.remove(indices);

        // Show the selection in the available and se-
        // lected lists.
        availableTable.showSelection();
        selectedTable.showSelection();
    }

    /**
     * Remove all items from the selected list.
     */
    private void removeAll() {

        // Get the indices of where the items that were
        // in the selected list are found in the choices
        // list.
        int[] indices = getSelectedItemsChoiceIndices(getItemsFromList(
                selectedTable, false, false, null));

        // Repopulate the available list with all possible
        // choices, and select the items that were added
        // to it.
        addAllItemsToAvailableList();
        availableTable.setSelection(indices);

        // Remove all available items from the available
        // list.
        selectedTable.removeAll();

        // Show the selection in the available list.
        availableTable.showSelection();
    }

    /**
     * Remove selected items from the selected list.
     */
    private void removeSelected() {

        // Get the indices of where the items that were
        // selected in the selected list are found in the
        // choices list.
        int[] indices = getSelectedItemsChoiceIndices(getItemsFromList(
                selectedTable, true, false, null));

        // Repopulate the available list with all possible
        // choices, and select the items that were added
        // to it. The items that are still in the selected
        // list will be removed later on; doing it this
        // way is a bit of a kludge, but makes it easier
        // to ensure that the right items (the ones just
        // put back in the available list) are selected.
        addAllItemsToAvailableList();
        availableTable.setSelection(indices);

        // Get the index to be selected in the selected
        // list after the selected items are removed, if
        // any.
        int firstUnselectedAfterSelected = getIndexOfFirstUnselectedAfterSelected(
                selectedTable, null);

        // Get the indices of the selected items in the
        // selected list so that they may be removed
        // later.
        indices = selectedTable.getSelectionIndices();

        // If an item was found to be selected, select
        // it now.
        if (firstUnselectedAfterSelected > -1) {
            selectedTable.setSelection(firstUnselectedAfterSelected);
        }

        // Remove all selected items from the selected
        // list.
        selectedTable.remove(indices);
        selectedTable.getColumn(0).pack();

        // Remove any items that were just added to the
        // available list that should not be there, be-
        // cause they are still part of the selected list.
        availableTable.remove(getSelectedItemsChoiceIndices(getItemsFromList(
                selectedTable, false, false, null)));
        availableTable.getColumn(0).pack();

        // Show the selection in the available and se-
        // lected lists.
        availableTable.showSelection();
        selectedTable.showSelection();
    }

    /**
     * Move selected items up in the selected list.
     */
    private void moveUp() {

        // Iterate through the selected items
        // starting with the lowest-indexed, re-
        // moving and reinserting each one at an
        // index one lower than it had before.
        int[] indices = selectedTable.getSelectionIndices();
        Arrays.sort(indices);
        for (int j = 0; j < indices.length; j++) {
            TableItem oldItem = selectedTable.getItem(indices[j]);
            String name = oldItem.getText(0);
            String identifier = (String) oldItem.getData();
            selectedTable.remove(indices[j]--);
            TableItem item = new TableItem(selectedTable, SWT.NONE, indices[j]);
            item.setText(0, name);
            item.setData(identifier);

        }
        selectedTable.getColumn(0).pack();

        // Select the just-moved items.
        selectedTable.setSelection(indices);

        // Show the selection in the selected
        // list.
        selectedTable.showSelection();
    }

    /**
     * Move selected items down in the selected list.
     */
    private void moveDown() {

        // Iterate through the selected items
        // starting with the highest-indexed, re-
        // moving and reinserting each one at an
        // index one higher than it had before.
        int[] indices = selectedTable.getSelectionIndices();
        Arrays.sort(indices);
        for (int j = indices.length - 1; j >= 0; j--) {
            TableItem oldItem = selectedTable.getItem(indices[j]);
            String name = oldItem.getText(0);
            String identifier = (String) oldItem.getData();
            selectedTable.remove(indices[j]++);
            TableItem item = new TableItem(selectedTable, SWT.NONE, indices[j]);
            item.setText(0, name);
            item.setData(identifier);
        }

        // Select the just-moved items.
        selectedTable.setSelection(indices);

        // Show the selection in the selected
        // list.
        selectedTable.showSelection();
    }

    /**
     * Move the selected items in the selected list to the specified index in
     * the same list.
     * 
     * @param index
     *            Index to which to move the items; the items will be inserted
     *            just after this index, or if this index is within the selected
     *            items, to just after the closest index before this one that is
     *            unselected.
     */
    private void moveToIndex(int index) {

        // Populate the list of selected items in
        // the order they occur, and find the index
        // that is unselected that is closest to
        // the provided index, either the index it-
        // self if it is unselected, or the closest
        // one above it that is unselected, or just
        // -1 if the selection includes the first
        // item in the list.
        List<String> identifiers = Lists.newArrayList();
        index = getClosestUnselectedIndexAtOrAboveIndex(selectedTable, index,
                identifiers);

        // If a valid index was found, get the item
        // at that index; it will need to be found
        // after the removal of the selected items
        // in order to find its index at that point,
        // so that the insertion may be done just
        // after it.
        TableItem insertionIndexItem = (index == -1 ? null : selectedTable
                .getItem(index));

        // Remove all selected items from the selected
        // list.
        selectedTable.remove(selectedTable.getSelectionIndices());

        // Add the items back at the appropriate index.
        addItems(identifiers,
                (index == -1 ? -1 : selectedTable.indexOf(insertionIndexItem)));

        // Show the selection.
        selectedTable.showSelection();
    }

    /**
     * Get the index of the last selected item in the selected list.
     * 
     * @return Last selected item in the selected list, or <code>-1</code> if no
     *         items are selected.
     */
    private int getLastSelectedIndex() {
        int[] indices = selectedTable.getSelectionIndices();
        int highestIndex = -1;
        for (int index : indices) {
            if (index > highestIndex) {
                highestIndex = index;
            }
        }
        return highestIndex;
    }

    /**
     * Get the unselected index for the specified list that is either the same
     * as the specified index, if the latter is unselected, or that is the
     * closest one that precedes the specified index, if the latter is selected.
     * If an array is specified, fill it with the selected items from the list.
     * 
     * @param table
     *            Table for which the index is to be found.
     * @param index
     *            Index to use as the base.
     * @param list
     *            Optional list; if supplied, it will be populated with the
     *            identifiers of the selected choices in the table, in the order
     *            in which their indices occur.
     * @return Closest unselected index at or before the specified index, or
     *         <code>-1</code> if there is no such index.
     */
    private int getClosestUnselectedIndexAtOrAboveIndex(Table table, int index,
            List<String> list) {

        // Get an array of the selected indices, and
        // sort it so that lower indices precede
        // higher ones.
        int[] indices = table.getSelectionIndices();
        Arrays.sort(indices);

        // Iterate through the indices, finding the
        // one that matches the target index, if
        // the target is indeed selected. If an
        // items list was provided, fill in the
        // items as well.
        int indexIntoSelected = -1;
        for (int j = 0; j < indices.length; j++) {
            if (list != null) {
                list.add((String) table.getItem(indices[j]).getData());
            }
            if ((indexIntoSelected == -1) && (index == indices[j])) {
                indexIntoSelected = j;
                if (list == null) {
                    break;
                }
            }
        }

        // If the target index was not found to be
        // selected, return it.
        if (indexIntoSelected == -1) {
            return index;
        }

        // Iterate backwards through the selected
        // indices to find the first unselected index
        // between them, or if none is found, the
        // first unselected index before the start of
        // the selected indices. If that yields -1,
        // that is not a problem.
        while (indexIntoSelected-- > 0) {
            if (indices[indexIntoSelected] + 1 < indices[indexIntoSelected + 1]) {
                break;
            }
        }
        return indices[indexIntoSelected + 1] - 1;
    }

    /**
     * Get the first unselected index for the specified table following the
     * first contiguous group of selected indices, and optionally fill the
     * specified list with the selected items in the order in which they occur.
     * 
     * @param table
     *            Table for which the index is to be found.
     * @param list
     *            Optional list; if supplied, it will be populated with the
     *            identifiers of all selected choices in the table, in the order
     *            in which their indices occur.
     * @return First unselected index for the table that follows the first
     *         contiguous group of selected indices, or <code>-1</code> if there
     *         are no unselected indices.
     */
    private int getIndexOfFirstUnselectedAfterSelected(Table table,
            List<String> list) {

        // Get an array of the selected indices, and
        // sort it so that lower indices precede
        // higher ones.
        int[] indices = table.getSelectionIndices();
        Arrays.sort(indices);

        // Iterate through the indices, finding the
        // first unselected index after the first
        // contiguous grouping of selected indices.
        // If an items list was provided, fill in
        // the items as well.
        int firstUnselectedAfterSelected = -1;
        for (int j = 0; j < indices.length; j++) {
            if (list != null) {
                list.add((String) table.getItem(indices[j]).getData());
            }
            if ((j > 0) && (firstUnselectedAfterSelected == -1)
                    && (indices[j] > indices[j - 1] + 1)) {
                firstUnselectedAfterSelected = indices[j - 1] + 1;
            }
        }

        // If no index was found, see if there is
        // an unselected item following all the
        // selected indices, and if not, see if
        // one exists before the first selected
        // index.
        if (firstUnselectedAfterSelected == -1) {
            firstUnselectedAfterSelected = indices[indices.length - 1] + 1;
            if (firstUnselectedAfterSelected >= table.getItemCount()) {
                firstUnselectedAfterSelected = indices[0] - 1;
            }
        }

        // Return the result.
        return firstUnselectedAfterSelected;
    }

    /**
     * Add the specified items to the selected list.
     * 
     * @param identifiers
     *            Identifiers of the items to be added.
     * @param index
     *            Index after which to add the items; if it is <code>-1</code>,
     *            they will be added at the start of the list.
     */
    private void addItems(List<String> identifiers, int index) {

        // Ensure that the items are added at the
        // beginning of the list if the index is
        // out of bounds; otherwise, add them just
        // after the index.
        if (index < 0) {
            index = 0;
        } else {
            index++;
        }

        // Iterate through the items, adding each
        // in turn, one after the next.
        int startIndex = index;
        for (String choice : identifiers) {
            TableItem item = new TableItem(selectedTable, SWT.NONE, index++);
            item.setText(0, choiceNamesForIdentifiers.get(choice));
            item.setData(choice);
        }
        selectedTable.getColumn(0).pack();

        // Set the selection to include all the
        // items just added.
        selectedTable.setSelection(startIndex, index - 1);
    }

    /**
     * Add all items to the available list.
     */
    private void addAllItemsToAvailableList() {
        availableTable.removeAll();
        BoundedListBuilderSpecifier specifier = getSpecifier();
        for (Object choice : choices) {
            TableItem item = new TableItem(availableTable, SWT.NONE);
            item.setText(0, specifier.getNameOfNode(choice));
            item.setData(specifier.getIdentifierOfNode(choice));
        }
        availableTable.getColumn(0).pack();
    }

    /**
     * Get a list of indices indicating the locations of the specified choices
     * within the choices list.
     * 
     * @param items
     *            Choices for which indices are to be found.
     * @return List of indices for the specified choices.
     */
    private int[] getSelectedItemsChoiceIndices(List<String> items) {

        // Iterate through the items, finding for
        // each the index indicating where it lives
        // in the choices list.
        int[] indices = new int[items.size()];
        List<String> choiceNames = Lists.newArrayList();
        BoundedListBuilderSpecifier specifier = getSpecifier();
        for (Object choice : choices) {
            choiceNames.add(specifier.getNameOfNode(choice));
        }
        for (int j = 0; j < items.size(); j++) {
            int index = choiceNames.indexOf(items.get(j));
            if (index != -1) {
                indices[j] = index;
            }
        }

        // Return the indices found above.
        return indices;
    }

    /**
     * Set the state to match the selected list's contents.
     */
    private void megawidgetWidgetsChanged() {
        state.clear();
        state.addAll(getItemsFromList(selectedTable, false, false, null));
        notifyListener(getSpecifier().getIdentifier(), state);
        notifyListener();
    }

    /**
     * Get all the contents as a list of strings from the specified list.
     * 
     * @param table
     *            Table from which to fetch the items.
     * @param selectedOnly
     *            Flag indicating whether only the selected items should be
     *            fetched, or just all the items.
     * @param needIdentifiers
     *            Flag indicating whether or not identifiers are to be returned
     *            instead of names.
     * @param list
     *            Optional list to be cleared and populated; if <code>
     *            null</code>, a new list is created.
     * @return List of strings from the selected table.
     */
    private List<String> getItemsFromList(Table table, boolean selectedOnly,
            boolean needIdentifiers, List<String> list) {
        if (list == null) {
            list = Lists.newArrayList();
        } else {
            list.clear();
        }
        if (selectedOnly) {
            int[] indices = table.getSelectionIndices();
            Arrays.sort(indices);
            for (int index : indices) {
                list.add(needIdentifiers ? (String) table.getItem(index)
                        .getData() : table.getItem(index).getText(0));
            }
        } else {
            for (TableItem item : table.getItems()) {
                list.add(needIdentifiers ? (String) item.getData() : item
                        .getText(0));
            }
        }
        return list;
    }
}

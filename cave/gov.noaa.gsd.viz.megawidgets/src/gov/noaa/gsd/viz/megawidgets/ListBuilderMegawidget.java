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
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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

import com.raytheon.viz.ui.widgets.duallist.ButtonImages;

/**
 * List builder megawidget, a megawidget that allows the user to build up an
 * orderable list from a set of choices.
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
 * @see ListBuilderSpecifier
 */
public class ListBuilderMegawidget extends MultipleChoicesMegawidget {

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
     * Drag and drop transfer types that are valid for the lists.
     */
    private final Transfer[] dragAndDropTransferTypes = new Transfer[] { TextTransfer
            .getInstance() };

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
    protected ListBuilderMegawidget(ListBuilderSpecifier specifier,
            Composite parent, Map<String, Object> paramMap) {
        super(specifier, paramMap);

        // Create a panel in which to place the widgets.
        // This is needed in order to group the widgets pro-
        // perly into a single megawidget.
        Composite panel = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(4, false);
        gridLayout.marginWidth = 0;
        gridLayout.horizontalSpacing = 13;
        panel.setLayout(gridLayout);
        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        gridData.horizontalSpan = specifier.getWidth();
        gridData.verticalIndent = specifier.getSpacing();
        panel.setLayoutData(gridData);

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
        if (availableText != null) {

            // Create a label widget.
            availableLabel = new Label(panel, SWT.NONE);
            availableLabel.setText(availableText);
            availableLabel.setEnabled(specifier.isEnabled());

            // Place the label in the grid.
            availableLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER,
                    false, false));
        } else {
            availableLabel = null;
        }

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
            gridData = new GridData(SWT.LEFT, SWT.CENTER, false, false);
            gridData.horizontalSpan = ((availableText != null)
                    && (selectedText != null) ? 1 : (availableText != null ? 3
                    : 2));
            gridData.widthHint = gridData.heightHint = 1;
            spacer.setLayoutData(gridData);
        }

        // Add a label for the selected items list if one
        // is required; if not, then add a spacer if one
        // was created for the available items list.
        if (selectedText != null) {

            // Create a label widget.
            selectedLabel = new Label(panel, SWT.NONE);
            selectedLabel.setText(selectedText);
            selectedLabel.setEnabled(specifier.isEnabled());

            // Place the label in the grid.
            selectedLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER,
                    false, false));

            // Create a spacer widget.
            Composite spacer = new Composite(panel, SWT.NONE);

            // Place the spacer in the grid.
            gridData = new GridData(SWT.LEFT, SWT.CENTER, false, false);
            gridData.widthHint = gridData.heightHint = 1;
            spacer.setLayoutData(gridData);
        } else {
            selectedLabel = null;
        }

        // Create the available items list. A table is
        // used because tables offer functionality like
        // being able to determine what row lies under
        // a given point.
        availableTable = new Table(panel, SWT.BORDER + SWT.MULTI
                + SWT.FULL_SELECTION);
        availableTable.setHeaderVisible(false);
        availableTable.setLinesVisible(false);
        availableTable.setEnabled(specifier.isEnabled());
        TableColumn column = new TableColumn(availableTable, SWT.NONE);
        for (String choice : specifier.getChoiceNames()) {
            TableItem item = new TableItem(availableTable, SWT.NONE);
            item.setText(0, choice);
        }
        column.pack();
        SelectionListener listListener = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (isEditable()) {
                    enableOrDisableButtons();
                }
            }
        };
        availableTable.addSelectionListener(listListener);
        gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        availableTable.setLayoutData(gridData);

        // Determine the height of the list. This must
        // be done after the above to ensure it will have
        // the right height. Unfortunately using either
        // computeSize() or computeTrim() to try to get
        // the extra vertical space required for the
        // borders, etc. seems to return a bizarrely high
        // value (20 even without a header showing), so
        // an arbitrary number of pixels is added in this
        // case as a cheesy workaround.
        gridData.heightHint = (specifier.getNumVisibleLines() * availableTable
                .getItemHeight()) + 7;

        // Create a panel to hold the buttons between the
        // two lists.
        Composite middlePanel = new Composite(panel, SWT.NONE);
        FillLayout fillLayout = new FillLayout(SWT.VERTICAL);
        fillLayout.spacing = 5;
        middlePanel.setLayout(fillLayout);
        gridData = new GridData(SWT.CENTER, SWT.CENTER, false, true);
        gridData.widthHint = 50;
        middlePanel.setLayoutData(gridData);

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
        addAll = new Button(middlePanel, SWT.PUSH);
        addAll.setImage(imagesSupplier
                .getImage(ButtonImages.ButtonImage.AddAll));
        addAll.addSelectionListener(buttonListener);

        addSelected = new Button(middlePanel, SWT.PUSH);
        addSelected.setImage(imagesSupplier
                .getImage(ButtonImages.ButtonImage.Add));
        addSelected.addSelectionListener(buttonListener);

        removeSelected = new Button(middlePanel, SWT.PUSH);
        removeSelected.setImage(imagesSupplier
                .getImage(ButtonImages.ButtonImage.Remove));
        removeSelected.addSelectionListener(buttonListener);

        removeAll = new Button(middlePanel, SWT.PUSH);
        removeAll.setImage(imagesSupplier
                .getImage(ButtonImages.ButtonImage.RemoveAll));
        removeAll.addSelectionListener(buttonListener);

        // Create the selected items list. As with the
        // available items list, a table is used instead
        // of a list due to the additional functionality
        // provided in SWT by tables over lists.
        selectedTable = new Table(panel, SWT.BORDER + SWT.MULTI
                + SWT.FULL_SELECTION);
        availableTable.setHeaderVisible(false);
        availableTable.setLinesVisible(false);
        selectedTable.setEnabled(specifier.isEnabled());
        column = new TableColumn(selectedTable, SWT.NONE);
        selectedTable.addSelectionListener(listListener);
        gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        selectedTable.setLayoutData(gridData);

        // Determine the height of the list. This must
        // be done after the above to ensure it will have
        // the right height. Unfortunately using either
        // computeSize() or computeTrim() to try to get
        // the extra vertical space required for the
        // borders, etc. seems to return a bizarrely high
        // value (20 even without a header showing), so
        // an arbitrary number of pixels is added in this
        // case as a cheesy workaround.
        gridData.heightHint = (specifier.getNumVisibleLines() * selectedTable
                .getItemHeight()) + 7;

        // Create a panel to hold the buttons to the right
        // of the selected list.
        Composite rightPanel = new Composite(panel, SWT.NONE);
        fillLayout = new FillLayout(SWT.VERTICAL);
        fillLayout.spacing = 5;
        rightPanel.setLayout(fillLayout);
        gridData = new GridData(SWT.CENTER, SWT.CENTER, false, true);
        gridData.widthHint = 50;
        rightPanel.setLayoutData(gridData);

        // Create the move up and down buttons.
        moveUp = new Button(rightPanel, SWT.PUSH);
        moveUp.setImage(imagesSupplier.getImage(ButtonImages.ButtonImage.Up));
        moveUp.addSelectionListener(buttonListener);

        moveDown = new Button(rightPanel, SWT.PUSH);
        moveDown.setImage(imagesSupplier
                .getImage(ButtonImages.ButtonImage.Down));
        moveDown.addSelectionListener(buttonListener);

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
                        StringBuffer buffer = new StringBuffer();
                        for (String choice : getItemsFromList(dragSourceList,
                                true)) {
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
                        if (dragAndDropTransferTypes[0]
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
        availableListDragSource.setTransfer(dragAndDropTransferTypes);
        availableTable.setDragDetect(true);
        availableListDragSource.addDragListener(dragListener);

        selectedListDragSource = new DragSource(selectedTable, DND.DROP_MOVE
                + DND.DROP_COPY);
        selectedListDragSource.setTransfer(dragAndDropTransferTypes);
        selectedTable.setDragDetect(true);
        selectedListDragSource.addDragListener(dragListener);

        // Create a drop target for each of the lists,
        // so that they may act as targets for drag and
        // drop actions.
        availableListDropTarget = new DropTarget(availableTable, DND.DROP_MOVE);
        availableListDropTarget.setTransfer(dragAndDropTransferTypes);
        availableListDropTarget.addDropListener(dropListener);

        selectedListDropTarget = new DropTarget(selectedTable, DND.DROP_MOVE);
        selectedListDropTarget.setTransfer(dragAndDropTransferTypes);
        selectedListDropTarget.addDropListener(dropListener);

        // Render the widgets uneditable if necessary.
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
    protected void megawidgetStateChanged(List<String> state) {

        // If a drag is mid-process, cancel it.
        dragSourceList = dropTargetList = null;

        // Get a list of the choice names, and set the
        // selected list's contents to match it.
        ChoicesMegawidgetSpecifier specifier = (ChoicesMegawidgetSpecifier) getSpecifier();
        String[] items = state.toArray(new String[state.size()]);
        selectedTable.removeAll();
        for (String choice : items) {
            TableItem item = new TableItem(selectedTable, SWT.NONE);
            item.setText(0, specifier.getLongVersionFromChoice(choice));
        }
        selectedTable.getColumn(0).pack();

        // Determine which choices are left over, and
        // set the available list's contents to match.
        availableTable.removeAll();
        for (String choice : specifier.getChoiceIdentifiers()) {
            boolean notSelected = true;
            for (String selectedChoice : items) {
                if (choice.equals(selectedChoice)) {
                    notSelected = false;
                    break;
                }
            }
            if (notSelected) {
                TableItem item = new TableItem(availableTable, SWT.NONE);
                item.setText(0, specifier.getLongVersionFromChoice(choice));
            }
        }
        availableTable.getColumn(0).pack();

        // Enable or disable buttons as appropriate.
        enableOrDisableButtons();
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

    /**
     * Change the component widgets to ensure their state matches that of the
     * editable flag.
     * 
     * @param editable
     *            Flag indicating whether the component widgets are to be
     *            editable or read-only.
     */
    @Override
    protected void doSetEditable(boolean editable) {
        Label label = (availableLabel != null ? availableLabel : selectedLabel);
        availableTable.setBackground(getBackgroundColor(editable,
                availableTable, label));
        selectedTable.setBackground(getBackgroundColor(editable, selectedTable,
                label));
        enableOrDisableButtons();
    }

    // Private Methods

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
        addItems(getItemsFromList(availableTable, false),
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
        String[] items = new String[availableTable.getSelectionCount()];
        int firstUnselectedAfterSelected = getIndexOfFirstUnselectedAfterSelected(
                availableTable, items);

        // Add the items to the selected list.
        addItems(items, index);

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
                selectedTable, false));

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
                selectedTable, true));

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

        // Remove all selected items from the available
        // list.
        selectedTable.remove(indices);
        selectedTable.getColumn(0).pack();

        // Remove any items that were just added to the
        // available list that should not be there, be-
        // cause they are still part of the selected list.
        availableTable.remove(getSelectedItemsChoiceIndices(getItemsFromList(
                selectedTable, false)));
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
            String choice = selectedTable.getItem(indices[j]).getText(0);
            selectedTable.remove(indices[j]--);
            TableItem item = new TableItem(selectedTable, SWT.NONE, indices[j]);
            item.setText(0, choice);
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
            String choice = selectedTable.getItem(indices[j]).getText(0);
            selectedTable.remove(indices[j]++);
            TableItem item = new TableItem(selectedTable, SWT.NONE, indices[j]);
            item.setText(0, choice);
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
        String[] items = new String[selectedTable.getSelectionCount()];
        index = getClosestUnselectedIndexAtOrAboveIndex(selectedTable, index,
                items);

        // If a valid index was found, get the item
        // at that index; it will need to be found
        // after the removal of the selected items
        // in order to find its index at that point,
        // so that the insertion may be done just
        // after it.
        TableItem insertionIndexItem = (index == -1 ? null : selectedTable
                .getItem(index));

        // Remove all selected items from the available
        // list.
        selectedTable.remove(selectedTable.getSelectionIndices());

        // Add the items back at the appropriate index.
        addItems(items,
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
     * @param list
     *            List for which the index is to be found.
     * @param index
     *            Index to use as the base.
     * @param items
     *            Optional array; if supplied, it must be large enough to hold
     *            all of the list's selected items, and it will be populated
     *            with said items, in the order in which their indices occur.
     * @return Closest unselected index at or before the specified index, or
     *         <code>-1</code> if there is no such index.
     */
    private int getClosestUnselectedIndexAtOrAboveIndex(Table list, int index,
            String[] items) {

        // Get a list of the selected indices, and
        // sort it so that lower indices precede
        // higher ones.
        int[] indices = list.getSelectionIndices();
        Arrays.sort(indices);

        // Iterate through the indices, finding the
        // one that matches the target index, if
        // the target is indeed selected. If an
        // items array was provided, fill in the
        // items as well.
        int indexIntoSelected = -1;
        for (int j = 0; j < indices.length; j++) {
            if (items != null) {
                items[j] = list.getItem(indices[j]).getText(0);
            }
            if ((indexIntoSelected == -1) && (index == indices[j])) {
                indexIntoSelected = j;
                if (items == null) {
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
     * Get the first unselected index for the specified list following the first
     * contiguous group of selected indices, and optionally fill the specified
     * array with the selected items in the order in which they occur.
     * 
     * @param list
     *            List for which the index is to be found.
     * @param items
     *            Optional array; if supplied, it must be large enough to hold
     *            all of the list's selected items, and it will be populated
     *            with said items, in the order in which their indices occur.
     * @return First unselected index for the list that follows the first
     *         contiguous group of selected indices, or <code>-1</code> if there
     *         are no unselected indices.
     */
    private int getIndexOfFirstUnselectedAfterSelected(Table list,
            String[] items) {

        // Get a list of the selected indices, and
        // sort it so that lower indices precede
        // higher ones.
        int[] indices = list.getSelectionIndices();
        Arrays.sort(indices);

        // Iterate through the indices, finding the
        // first unselected index after the first
        // contiguous grouping of selected indices.
        // If an items array was provided, fill in
        // the items as well.
        int firstUnselectedAfterSelected = -1;
        for (int j = 0; j < indices.length; j++) {
            if (items != null) {
                items[j] = list.getItem(indices[j]).getText(0);
            }
            if ((j > 0) && (firstUnselectedAfterSelected == -1)
                    && (indices[j] > indices[j - 1] + 1)) {
                firstUnselectedAfterSelected = indices[j - 1] + 1;
            }
        }

        // If not index was found, see if there is
        // an unselected item following all the
        // selected indices, and if not, see if
        // one exists before the first selected
        // index.
        if (firstUnselectedAfterSelected == -1) {
            firstUnselectedAfterSelected = indices[indices.length - 1] + 1;
            if (firstUnselectedAfterSelected >= list.getItemCount()) {
                firstUnselectedAfterSelected = indices[0] - 1;
            }
        }

        // Return the result.
        return firstUnselectedAfterSelected;
    }

    /**
     * Add the specified items to the selected list.
     * 
     * @param items
     *            Items to be added.
     * @param index
     *            Index after which to add the items; if it is <code>-1</code>,
     *            they will be added at the start of the list.
     */
    private void addItems(String[] items, int index) {

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
        for (String choice : items) {
            TableItem item = new TableItem(selectedTable, SWT.NONE, index++);
            item.setText(0, choice);
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
        for (String choice : ((ChoicesMegawidgetSpecifier) getSpecifier())
                .getChoiceNames()) {
            TableItem item = new TableItem(availableTable, SWT.NONE);
            item.setText(0, choice);
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
    private int[] getSelectedItemsChoiceIndices(String[] items) {

        // Iterate through the items, finding for
        // each the index indicating where it lives
        // in the choices list.
        int[] indices = new int[items.length];
        List<String> choices = ((ChoicesMegawidgetSpecifier) getSpecifier())
                .getChoiceNames();
        for (int j = 0; j < items.length; j++) {
            int index = choices.indexOf(items[j]);
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
        state = new ArrayList<String>(Arrays.asList(getItemsFromList(
                selectedTable, false)));
        notifyListener(getSpecifier().getIdentifier(), state);
        notifyListener();
    }

    /**
     * Get all the contents as an array of strings from the specified list.
     * 
     * @param list
     *            List from which to fetch the items.
     * @param selectedOnly
     *            Flag indicating whether only the selected items should be
     *            fetched, or just all the items.
     * @return Array of strings from the selected list.
     */
    private String[] getItemsFromList(Table list, boolean selectedOnly) {
        String[] items = new String[selectedOnly ? list.getSelectionCount()
                : list.getItemCount()];
        if (selectedOnly) {
            int[] indices = list.getSelectionIndices();
            Arrays.sort(indices);
            for (int j = 0; j < items.length; j++) {
                items[j] = list.getItem(indices[j]).getText(0);
            }
        } else {
            for (int j = 0; j < items.length; j++) {
                items[j] = list.getItem(j).getText(0);
            }
        }
        return items;
    }
}

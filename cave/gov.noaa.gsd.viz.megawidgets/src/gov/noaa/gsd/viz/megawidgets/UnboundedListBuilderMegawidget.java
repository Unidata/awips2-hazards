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
import java.util.Collection;
import java.util.HashSet;
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
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import com.raytheon.viz.ui.widgets.duallist.ButtonImages;

/**
 * Unbounded list builder megawidget, a megawidget that allows the user to build
 * up an orderable list from an open set of choices, meaning that meaning that
 * the user may add any arbitrary choice to the list, as long as it is unique.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 31, 2013   2336     Chris.Golden      Initial creation.
 * Dec 14, 2013   2545     Chris.Golden      Added ability to hit the Enter key
 *                                           within the text widget to add the
 *                                           entered text to the list as an item
 *                                           if appropriate.
 * Mar 06, 2014   2155     Chris.Golden      Fixed bug caused by a lack of
 *                                           defensive copying of the state when
 *                                           notifying a state change listener of
 *                                           a change. Also fixed Javadoc and
 *                                           took advantage of new JDK 1.7
 *                                           features.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see UnboundedListBuilderSpecifier
 */
public class UnboundedListBuilderMegawidget extends StatefulMegawidget
        implements IControl {

    // Private Static Constants

    /**
     * Drag and drop transfer types that are valid for the table.
     */
    private static final Transfer[] DRAG_AND_DROP_TRANSFER_TYPES = new Transfer[] { TextTransfer
            .getInstance() };

    // Private Variables

    /**
     * Current state as a list of choices.
     */
    private final List<String> state;

    /**
     * Label, if any.
     */
    private final Label label;

    /**
     * Table.
     */
    private final Table table;

    /**
     * Text field, for entering new choices to be added.
     */
    private final Text text;

    /**
     * Add button.
     */
    private final Button add;

    /**
     * Remove button.
     */
    private final Button remove;

    /**
     * Move up button.
     */
    private final Button moveUp;

    /**
     * Move down button.
     */
    private final Button moveDown;

    /**
     * Button images supplier.
     */
    private final ButtonImages imagesSupplier;

    /**
     * Table drag source.
     */
    private final DragSource tableDragSource;

    /**
     * Table drop target.
     */
    private final DropTarget tableDropTarget;

    /**
     * Flag indicating whether or not dragging from this table is occurring.
     */
    private boolean draggingFromTable = false;

    /**
     * Flag indicating whether or not a potential drag-drop is hovering over the
     * table.
     */
    private boolean droppingToTable = false;

    /**
     * Flag indicating whether or not text entry widget changes should be
     * ignored by that widget's listener.
     */
    private boolean ignoreTextFieldChange = false;

    /**
     * Last position of the table's vertical scrollbar. This is used whenever
     * the choices are being changed via {@link #setState(String, Object)} or
     * one of the mutable property manipulation methods, in order to keep a
     * similar visual state to what came before.
     */
    private int scrollPosition = 0;

    /**
     * Set of choices in the table that were last selected. This is used
     * whenever the choices are being changed via
     * {@link #setState(String, Object)} or one of the mutable property
     * manipulation methods, in order to keep a similar visual state to what
     * came before.
     */
    private final List<String> selectedChoices = new ArrayList<>();

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
    protected UnboundedListBuilderMegawidget(
            UnboundedListBuilderSpecifier specifier, Composite parent,
            Map<String, Object> paramMap) {
        super(specifier, paramMap);
        helper = new ControlComponentHelper(specifier);

        // Copy the starting choices into the state.
        state = new ArrayList<>();
        List<?> startingState = (List<?>) specifier.getStartingState(specifier
                .getIdentifier());
        if (startingState != null) {
            for (Object node : startingState) {
                state.add(specifier.getNameOfNode(node));
            }
        }

        // Create the panel that will contain the components,
        // and customize its horizontal spacing to be more
        // appropriate for this megawidget.
        Composite panel = UiBuilder.buildComposite(parent, 2, SWT.NONE,
                UiBuilder.CompositeType.MULTI_ROW_VERTICALLY_EXPANDING,
                specifier);
        ((GridLayout) panel.getLayout()).horizontalSpacing = 13;

        // Create the button images supplier.
        imagesSupplier = new ButtonImages(panel);

        // Create a label if appropriate.
        label = UiBuilder.buildLabel(panel, specifier, 2);

        // Create the table selection listener.
        SelectionListener listListener = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (isEditable()) {
                    enableOrDisableSidePanel();
                }
            }
        };

        // Create the table holding the current choices.
        table = buildTable(panel, listListener, specifier);
        TableColumn column = table.getColumn(0);
        for (Object choice : state) {
            TableItem item = new TableItem(table, SWT.NONE);
            item.setText(0, specifier.getNameOfNode(choice));
        }
        column.pack();

        // Create a panel to hold the widgets to the right.
        Composite sidePanel = buildSidePanel(panel);

        // Create the button listener.
        SelectionListener buttonListener = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {

                // Perform the appropriate action.
                if (e.widget == add) {
                    addNewAtIndex(getLastSelectedIndex());
                } else if (e.widget == remove) {
                    removeSelected();
                } else if (e.widget == moveUp) {
                    moveUp();
                } else if (e.widget == moveDown) {
                    moveDown();
                } else {
                    return;
                }

                // Enable and disable the side panel widgets
                // as appropriate.
                enableOrDisableSidePanel();

                // Change the state accordingly.
                megawidgetWidgetsChanged();
            }
        };

        // Create the text and the buttons. The text needs two
        // listeners, one to enable or disable buttons as its
        // contents change, and one to respond to Enter key-
        // strokes to add a new item to the list, if possible.
        text = new Text(sidePanel, SWT.BORDER);
        text.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                if (ignoreTextFieldChange == false) {
                    enableOrDisableSidePanel();
                }
            }
        });
        text.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                if (add.isEnabled()) {
                    addNewAtIndex(getLastSelectedIndex());
                    enableOrDisableSidePanel();
                    megawidgetWidgetsChanged();
                }
            }
        });
        GC gc = new GC(text);
        FontMetrics fontMetrics = gc.getFontMetrics();
        ((GridData) sidePanel.getLayoutData()).widthHint = text.computeSize(
                20 * fontMetrics.getAverageCharWidth(), SWT.DEFAULT).x;
        gc.dispose();
        add = buildButton(sidePanel, "Add", buttonListener);
        remove = buildButton(sidePanel, "Remove", buttonListener);
        moveUp = buildButton(sidePanel, ButtonImages.ButtonImage.Up,
                buttonListener);
        moveDown = buildButton(sidePanel, ButtonImages.ButtonImage.Down,
                buttonListener);

        // Set the state of the side panel widgets as
        // appropriate.
        enableOrDisableSidePanel();

        // Create a drag listener that will listen for
        // drag events occurring in the table, and re-
        // spond to them appropriately.
        DragSourceListener dragListener = new DragSourceListener() {
            @Override
            public void dragStart(DragSourceEvent event) {

                // Do not initiate a drag if nothing
                // is selected. Otherwise, remember
                // which list is the source.
                if (table.getSelectionCount() == 0) {
                    event.doit = false;
                } else {
                    draggingFromTable = true;
                }
            }

            @Override
            public void dragSetData(DragSourceEvent event) {

                // Only set the data if the data type
                // asked for is text.
                if (draggingFromTable
                        && TextTransfer.getInstance().isSupportedType(
                                event.dataType)) {

                    // Supply an small string if the
                    // drop target is the table within
                    // this megawidget, as it handles
                    // drops itself. Only supply a text
                    // list of all selected items if
                    // the drop target is elsewhere.
                    // The small (non-zero-length)
                    // string is required in the former
                    // case because an empty string
                    // causes an exception to be thrown.
                    if (droppingToTable == false) {
                        StringBuilder buffer = new StringBuilder();
                        for (String choice : getChoiceNames(false, null)) {
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
                draggingFromTable = false;
            }
        };

        // Create a drop listener that will listen for
        // drop events occurring in the table, and
        // respond accordingly.
        DropTargetListener dropListener = new DropTargetListener() {
            @Override
            public void dragEnter(DropTargetEvent event) {

                // If the source of the drag is this
                // table, or is something else that
                // provides a string holding a new-
                // line-delineated list of substrings,
                // then accept this as a potential
                // drop; otherwise, reject it.
                event.detail = DND.DROP_NONE;
                for (TransferData dataType : event.dataTypes) {
                    if (DRAG_AND_DROP_TRANSFER_TYPES[0]
                            .isSupportedType(dataType)) {

                        // Note what sort of drop
                        // is permitted, the data
                        // type allowed, and
                        // which list is the drop
                        // target.
                        event.detail = (draggingFromTable ? DND.DROP_MOVE
                                : DND.DROP_COPY);
                        event.currentDataType = dataType;
                        droppingToTable = true;
                        break;
                    }
                }
            }

            @Override
            public void dragLeave(DropTargetEvent event) {
                droppingToTable = false;
            }

            @Override
            public void dragOperationChanged(DropTargetEvent event) {

                // No action.
            }

            @Override
            public void dragOver(DropTargetEvent event) {

                // If the drop target is the table,
                // allow it to scroll if the drag is
                // close to its top or bottom, and
                // indicate via visuals that the
                // potential drop point would be after
                // the current item.
                if (droppingToTable) {
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
                if (droppingToTable == false) {
                    return;
                }

                // Determine the index at which to
                // move or add the items being
                // dropped, and then move them there
                // if they are sourced from within
                // the table, or copy them there if
                // they are from outside the mega-
                // widget.
                int index = (event.item == null ? table.getItemCount() - 1
                        : table.indexOf((TableItem) event.item));
                if (draggingFromTable) {
                    moveToIndex(index);
                } else {
                    addDroppedAtIndex((String) event.data, index);
                }

                // Enable and disable the side panel widgets
                // as appropriate.
                droppingToTable = false;
                enableOrDisableSidePanel();

                // Change the state accordingly.
                megawidgetWidgetsChanged();
            }

            @Override
            public void dropAccept(DropTargetEvent event) {
                droppingToTable = true;
            }
        };

        // Create a drag source for the table, so that it
        // may act as a source for drag and drop actions.
        tableDragSource = new DragSource(table, DND.DROP_MOVE + DND.DROP_COPY);
        tableDragSource.setTransfer(DRAG_AND_DROP_TRANSFER_TYPES);
        table.setDragDetect(true);
        tableDragSource.addDragListener(dragListener);

        // Create a drop target for the table, so that it
        // may act as a target for drag and drop actions.
        tableDropTarget = new DropTarget(table, DND.DROP_MOVE + DND.DROP_COPY);
        tableDropTarget.setTransfer(DRAG_AND_DROP_TRANSFER_TYPES);
        tableDropTarget.addDropListener(dropListener);

        // Render the widgets uneditable if necessary.
        if (isEditable() == false) {
            doSetEditable(false);
        }
    }

    // Public Methods

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

    // Protected Methods

    @Override
    protected void doSetEnabled(boolean enable) {
        if (label != null) {
            label.setEnabled(enable);
        }
        table.setEnabled(false);
        enableOrDisableSidePanel();
    }

    @Override
    protected Object doGetState(String identifier) {
        return new ArrayList<>(state);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void doSetState(String identifier, Object state)
            throws MegawidgetStateException {

        // Prepare for the state change.
        prepareForStateChange();

        // Set the state to that supplied.
        this.state.clear();
        if (state instanceof String) {
            this.state.add((String) state);
        } else if (state != null) {
            try {
                this.state.addAll((Collection<? extends String>) state);
            } catch (Exception e) {
                throw new MegawidgetStateException(identifier, getSpecifier()
                        .getType(), state, "must be list of choices");
            }
        }

        // Ensure that the choices specified contain no
        // repetition.
        Set<String> choices = new HashSet<>();
        for (String choice : this.state) {
            if (choices.contains(choice)) {
                this.state.clear();
                throw new MegawidgetStateException(identifier, getSpecifier()
                        .getType(), state,
                        "includes duplicate sibling identifier");
            }
            choices.add(choice);
        }

        // Synchronize the widgets to the new state.
        synchronizeWidgetsToState();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected String doGetStateDescription(String identifier, Object state)
            throws MegawidgetStateException {
        if (state instanceof String) {
            return (String) state;
        } else {
            try {
                StringBuilder description = new StringBuilder();
                for (String element : (Collection<? extends String>) state) {
                    if (description.length() > 0) {
                        description.append("; ");
                    }
                    description.append(element);
                }
                return description.toString();
            } catch (Exception e) {
                throw new MegawidgetStateException(identifier, getSpecifier()
                        .getType(), state, "must be list of choices");
            }
        }
    }

    // Private Methods

    /**
     * Prepare for the state to change.
     */
    private void prepareForStateChange() {

        // Remember the scrollbar position so that it can
        // be approximately restored.
        scrollPosition = table.getVerticalBar().getSelection();

        // Remember the identifiers of the currently
        // selected choices for the table, if any.
        getChoiceNames(true, selectedChoices);
    }

    /**
     * Synchronize the component widgets to the current state.
     */
    private void synchronizeWidgetsToState() {

        // If a drag or drop is mid-process, cancel it.
        draggingFromTable = droppingToTable = false;

        // Clear the table and fill it with the current choices.
        table.removeAll();
        for (String choice : state) {
            TableItem item = new TableItem(table, SWT.NONE);
            item.setText(0, choice);
        }
        table.getColumn(0).pack();

        // Enable or disable the side panel widgets as
        // appropriate.
        enableOrDisableSidePanel();

        // See what items were selected previously that
        // are present in the new list, and select those
        // items.
        List<TableItem> selectedTableItems = new ArrayList<>();
        for (TableItem item : table.getItems()) {
            if (selectedChoices.contains(item.getText())) {
                selectedTableItems.add(item);
            }
        }
        if (selectedTableItems.size() > 0) {
            table.setSelection(selectedTableItems
                    .toArray(new TableItem[selectedTableItems.size()]));
        }

        // Clear the selected choices list, as it is no
        // longer needed.
        selectedChoices.clear();

        // Set the scrollbar positions to be similar to
        // what it was before.
        table.getVerticalBar().setSelection(scrollPosition);
    }

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
        enableOrDisableSidePanel();
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
            UnboundedListBuilderSpecifier specifier) {

        // Create the list. A table is used because tables
        // offer functionality like being able to determine
        // what row lies under a given point.
        Table table = new Table(parent, SWT.BORDER | SWT.MULTI
                | SWT.FULL_SELECTION);
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
     * Build a side panel.
     * 
     * @param parent
     *            Parent composite.
     * @return New side panel.
     */
    private Composite buildSidePanel(Composite parent) {
        Composite panel = new Composite(parent, SWT.NONE);
        FillLayout fillLayout = new FillLayout(SWT.VERTICAL);
        fillLayout.spacing = 5;
        panel.setLayout(fillLayout);
        GridData gridData = new GridData(SWT.FILL, SWT.CENTER, false, true);
        // gridData.widthHint = 50;
        panel.setLayoutData(gridData);
        return panel;
    }

    /**
     * Create a button with an image icon.
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
     * Create a button with text.
     * 
     * @param parent
     *            Parent composite.
     * @param text
     *            Text to be displayed.
     * @param listener
     *            Listener to be notified of button invocations.
     * @return New button.
     */
    private Button buildButton(Composite parent, String text,
            SelectionListener listener) {
        Button button = new Button(parent, SWT.PUSH);
        button.setText(text);
        button.addSelectionListener(listener);
        return button;
    }

    /**
     * Update the side panel widgets' enabled state as is appropriate to the
     * current states of the other widgets.
     */
    private void enableOrDisableSidePanel() {

        // If the megawidget is disabled or read-
        // only, disable all the buttons; other-
        // wise, enable or disable each one as
        // appropriate given the items selected
        // in the lists.
        if ((isEnabled() == false) || (isEditable() == false)) {
            setTextEntryContents("");
            text.setEnabled(false);
            add.setEnabled(false);
            remove.setEnabled(false);
            moveUp.setEnabled(false);
            moveDown.setEnabled(false);
        } else {
            text.setEnabled(true);

            // If the string within the text
            // widget is a valid choice (i.e.
            // it is different from all the
            // choices already in existence),
            // enable the Add button.
            String potentialChoice = text.getText().trim();
            boolean textIsUnique = true;
            if ((potentialChoice != null)
                    && (potentialChoice.equals("") == false)) {
                for (String choice : state) {
                    if (choice.trim().equals(potentialChoice)) {
                        textIsUnique = false;
                        break;
                    }
                }
            } else {
                textIsUnique = false;
            }
            add.setEnabled(textIsUnique);

            // If there are selected items in
            // the table, enable the Remove
            // button.
            remove.setEnabled(table.getSelectionCount() > 0);

            // If items are selected within the
            // table, enable the up and down
            // buttons as appropriate; otherwise,
            // disable them.
            if ((table.getItemCount() > 0) && (table.getSelectionCount() > 0)) {

                // Find the highest and lowest
                // selected indices.
                int[] selected = table.getSelectionIndices();
                int highestIndex = -1, lowestIndex = table.getItemCount();
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
                moveDown.setEnabled(highestIndex < table.getItemCount() - 1);
            } else {
                moveUp.setEnabled(false);
                moveDown.setEnabled(false);
            }
        }
    }

    /**
     * Add the new choice from the text field to the table after the specified
     * index.
     * 
     * @param index
     *            Index after which to add the new choice.
     */
    private void addNewAtIndex(int index) {
        addItem(text.getText(), index);
        setTextEntryContents("");
        table.showSelection();
    }

    /**
     * Add the specified item(s), coming from a drag and drop that ended over
     * the table, to the table after the specified index.
     * 
     * @param items
     *            String of newline-delineated substrings, each of which is to
     *            be considered a separate item.
     * @param index
     *            Index after which to add the selected available items.
     */
    private void addDroppedAtIndex(String items, int index) {
        List<String> list = new ArrayList<>();
        String[] itemArray = items.split("\n");
        for (String item : itemArray) {
            item = item.trim();
            if (item.length() > 0) {
                list.add(item);
            }
        }
        if (list.size() > 0) {
            addItems(list, index);
        }
        table.showSelection();
    }

    /**
     * Remove selected items from the table.
     */
    private void removeSelected() {

        // Get the index to be selected in the table after
        // the selected items are removed, if any.
        int firstUnselectedAfterSelected = getIndexOfFirstUnselectedAfterSelected();

        // Get the indices of the selected items in the
        // table so that they may be removed later.
        int[] indices = table.getSelectionIndices();

        // If an item was found to be selected, select
        // it now.
        if (firstUnselectedAfterSelected > -1) {
            table.setSelection(firstUnselectedAfterSelected);
        }

        // Remove all selected items from the table.
        table.remove(indices);
        table.getColumn(0).pack();

        // Show the selection in the table.
        table.showSelection();
    }

    /**
     * Move selected items up in the table.
     */
    private void moveUp() {

        // Iterate through the selected items
        // starting with the lowest-indexed, re-
        // moving and reinserting each one at an
        // index one lower than it had before.
        int[] indices = table.getSelectionIndices();
        Arrays.sort(indices);
        for (int j = 0; j < indices.length; j++) {
            TableItem oldItem = table.getItem(indices[j]);
            String name = oldItem.getText(0);
            table.remove(indices[j]--);
            TableItem item = new TableItem(table, SWT.NONE, indices[j]);
            item.setText(0, name);

        }
        table.getColumn(0).pack();

        // Select the just-moved items.
        table.setSelection(indices);

        // Show the selection in the table.
        table.showSelection();
    }

    /**
     * Move selected items down in the table.
     */
    private void moveDown() {

        // Iterate through the selected items
        // starting with the highest-indexed, re-
        // moving and reinserting each one at an
        // index one higher than it had before.
        int[] indices = table.getSelectionIndices();
        Arrays.sort(indices);
        for (int j = indices.length - 1; j >= 0; j--) {
            TableItem oldItem = table.getItem(indices[j]);
            String name = oldItem.getText(0);
            table.remove(indices[j]++);
            TableItem item = new TableItem(table, SWT.NONE, indices[j]);
            item.setText(0, name);
        }

        // Select the just-moved items.
        table.setSelection(indices);

        // Show the selection in the table.
        table.showSelection();
    }

    /**
     * Move the selected items in the table to the specified index in the same
     * table.
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
        List<String> identifiers = new ArrayList<>();
        index = getClosestUnselectedIndexAtOrAboveIndex(index, identifiers);

        // If a valid index was found, get the item
        // at that index; it will need to be found
        // after the removal of the selected items
        // in order to find its index at that point,
        // so that the insertion may be done just
        // after it.
        TableItem insertionIndexItem = (index == -1 ? null : table
                .getItem(index));

        // Remove all selected items from the table.
        table.remove(table.getSelectionIndices());

        // Add the items back at the appropriate index.
        addItems(identifiers,
                (index == -1 ? -1 : table.indexOf(insertionIndexItem)));

        // Show the selection.
        table.showSelection();
    }

    /**
     * Get the index of the last selected item in the table.
     * 
     * @return Last selected item in the table, or <code>-1</code> if no items
     *         are selected.
     */
    private int getLastSelectedIndex() {
        int[] indices = table.getSelectionIndices();
        int highestIndex = -1;
        for (int index : indices) {
            if (index > highestIndex) {
                highestIndex = index;
            }
        }
        return highestIndex;
    }

    /**
     * Get the unselected index for the table that is either the same as the
     * specified index, if the latter is unselected, or that is the closest one
     * that precedes the specified index, if the latter is selected. If an array
     * is specified, fill it with the selected items from the list.
     * 
     * @param index
     *            Index to use as the base.
     * @param list
     *            Optional list; if supplied, it will be populated with the
     *            identifiers of the selected choices in the table, in the order
     *            in which their indices occur.
     * @return Closest unselected index at or before the specified index, or
     *         <code>-1</code> if there is no such index.
     */
    private int getClosestUnselectedIndexAtOrAboveIndex(int index,
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
                list.add(table.getItem(indices[j]).getText());
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
     * Get the first unselected index for the table following the first
     * contiguous group of selected indices.
     * 
     * @return First unselected index for the table that follows the first
     *         contiguous group of selected indices, or <code>-1</code> if there
     *         are no unselected indices.
     */
    private int getIndexOfFirstUnselectedAfterSelected() {

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
     * Add the specified item to the selected list.
     * 
     * @param choice
     *            Item to be added.
     * @param index
     *            Index after which to add the item; if it is <code>-1</code>,
     *            it will be added at the start of the list.
     */
    private void addItem(String choice, int index) {

        // Ensure that the item is added at the
        // beginning of the list if the index is
        // out of bounds; otherwise, add it just
        // after the index.
        if (index < 0) {
            index = 0;
        } else {
            index++;
        }

        // Add the item.
        TableItem item = new TableItem(table, SWT.NONE, index);
        item.setText(0, choice);
        table.getColumn(0).pack();

        // Set the selection to be the item just
        // added.
        table.setSelection(index);
    }

    /**
     * Add the specified items to the selected list.
     * 
     * @param choices
     *            Items to be added.
     * @param index
     *            Index after which to add the items; if it is <code>-1</code>,
     *            they will be added at the start of the list.
     */
    private void addItems(List<String> choices, int index) {

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
        for (String choice : choices) {
            TableItem item = new TableItem(table, SWT.NONE, index++);
            item.setText(0, choice);
        }
        table.getColumn(0).pack();

        // Set the selection to include all the
        // items just added.
        table.setSelection(startIndex, index - 1);
    }

    /**
     * Set the text entry contents to those specified.
     * 
     * @param string
     *            New contents.
     */
    private void setTextEntryContents(String string) {
        ignoreTextFieldChange = true;
        text.setText(string);
        ignoreTextFieldChange = false;
    }

    /**
     * Set the state to match the selected list's contents.
     */
    private void megawidgetWidgetsChanged() {
        state.clear();
        state.addAll(getChoiceNames(false, null));
        notifyListener(getSpecifier().getIdentifier(), new ArrayList<>(state));
        notifyListener();
    }

    /**
     * Get the current choices as a list of strings.
     * 
     * @param selectedOnly
     *            Flag indicating whether only the selected items should be
     *            fetched, or just all the items.
     * @param list
     *            Optional list to be cleared and populated; if <code>
     *            null</code>, a new list is created.
     * @return List of strings from the selected table.
     */
    private List<String> getChoiceNames(boolean selectedOnly, List<String> list) {
        if (list == null) {
            list = new ArrayList<>();
        } else {
            list.clear();
        }
        if (selectedOnly) {
            int[] indices = table.getSelectionIndices();
            Arrays.sort(indices);
            for (int index : indices) {
                list.add(table.getItem(index).getText(0));
            }
        } else {
            for (TableItem item : table.getItems()) {
                list.add(item.getText(0));
            }
        }
        return list;
    }
}

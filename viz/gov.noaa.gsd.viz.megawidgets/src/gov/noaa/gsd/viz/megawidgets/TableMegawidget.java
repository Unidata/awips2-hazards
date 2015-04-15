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
import gov.noaa.gsd.viz.megawidgets.validators.TableValidator;

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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.google.common.collect.Sets;

/**
 * Description: Table megawidget.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Apr 02, 2015    4162    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class TableMegawidget extends StatefulMegawidget implements IControl {

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
     * Current state.
     */
    private final List<List<Object>> state;

    /**
     * State validator.
     */
    private final TableValidator stateValidator;

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
    @SuppressWarnings("unchecked")
    protected TableMegawidget(TableSpecifier specifier, Composite parent,
            Map<String, Object> paramMap) {
        super(specifier, paramMap);
        state = (List<List<Object>>) specifier.getStartingState(specifier
                .getIdentifier());
        stateValidator = (TableValidator) specifier.getStateValidator();

        /*
         * Create a panel in which to place the widgets and a label, if
         * appropriate.
         */
        Composite panel = UiBuilder.buildComposite(parent, 1, SWT.NONE,
                UiBuilder.CompositeType.MULTI_ROW_VERTICALLY_EXPANDING,
                specifier);
        label = UiBuilder.buildLabel(panel, specifier);

        /*
         * Create a table to display the state.
         */
        table = new Table(panel, SWT.BORDER | SWT.FULL_SELECTION);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        table.setEnabled(specifier.isEnabled());
        final GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        table.setLayoutData(gridData);
        for (String headerLabel : specifier.getColumnHeaderLabels()) {
            TableColumn column = new TableColumn(table, SWT.NONE);
            column.setText(headerLabel);
        }
        UiBuilder.ensureMouseWheelEventsPassedUpToAncestor(table);

        /*
         * Determine the height of the table. Note that the computed height has
         * a seemingly magic number subtracted from it because at different font
         * sizes, the simple calculation of adding the header height to the
         * product of the item heights and the item count consistently gives a
         * result that is 17 pixels too large. Not sure why, but no time to
         * investigate further at the moment.
         */
        gridData.heightHint = (specifier.getNumVisibleLines() * table
                .getItemHeight()) + table.getHeaderHeight() - 17;

        /*
         * Bind selection events to record the new selection.
         */

        table.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Set<String> selectedLines = new HashSet<>(table
                        .getSelectionCount(), 1.0f);
                for (TableItem item : table.getSelection()) {
                    selectedLines.add(getLineDescriptor(item));
                }
                displaySettings.setSelectedChoices(selectedLines);
            }
        });

        /*
         * Bind scrollbar movements to record the topmost item in the list.
         */
        table.getVerticalBar().addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                recordTopmostVisibleLine();
            }
        });

        /*
         * Synchronize user-facing widgets to the starting state.
         */
        synchronizeComponentWidgetsToState();
        for (TableColumn column : table.getColumns()) {
            column.pack();
        }
    }

    // Public Methods

    @Override
    public final boolean isEditable() {
        return false;
    }

    @Override
    public final void setEditable(boolean editable) {

        /*
         * No action.
         */
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
                         * Set any lines that exist now and that were selected
                         * before to be selected now.
                         */
                        Map<String, Integer> indicesForLines = getIndicesForLines();
                        Set<String> selectedLines = listSettings
                                .getSelectedChoices();
                        if ((selectedLines != null)
                                && (selectedLines.isEmpty() == false)) {
                            int[] indices = UiBuilder.getIndicesOfChoices(
                                    selectedLines, indicesForLines);
                            table.setSelection(indices);
                            selectedLines = new HashSet<>(indices.length);
                            for (int index : indices) {
                                selectedLines.add(table.getItem(index).getText(
                                        0));
                            }
                            TableMegawidget.this.displaySettings
                                    .setSelectedChoices(selectedLines);
                        }

                        /*
                         * Set the topmost visible line in the scrollable
                         * viewport to be what it was before if the latter is
                         * found in the existing lines.
                         */
                        String topmostLine = listSettings
                                .getTopmostVisibleChoice();
                        if (topmostLine != null) {
                            Integer index = indicesForLines.get(topmostLine);
                            if (index != null) {
                                table.setTopIndex(index);
                                TableMegawidget.this.displaySettings
                                        .setTopmostVisibleChoice(getLineDescriptor(table
                                                .getItem(index)));
                            }
                        }
                    }
                }
            });
        }
    }

    // Protected Methods

    @Override
    protected Object doGetState(String identifier) {
        List<List<Object>> copy = new ArrayList<>(state.size());
        for (List<Object> element : state) {
            copy.add(new ArrayList<>(element));
        }
        return copy;
    }

    @Override
    protected void doSetState(String identifier, Object state)
            throws MegawidgetStateException {

        /*
         * Convert the provided state to a valid value, and record it.
         */
        List<List<Object>> newState;
        try {
            newState = stateValidator.convertToStateValue(state);
        } catch (MegawidgetException e) {
            throw new MegawidgetStateException(e);
        }
        this.state.clear();
        this.state.addAll(newState);

        /*
         * Synchronize the widgets to the new state.
         */
        synchronizeComponentWidgetsToState();
    }

    @Override
    protected String doGetStateDescription(String identifier, Object state)
            throws MegawidgetStateException {

        /*
         * Ensure the provided state is valid.
         */
        List<List<Object>> list = null;
        try {
            list = stateValidator.convertToStateValue(state);
        } catch (MegawidgetException e) {
            throw new MegawidgetStateException(e);
        }

        /*
         * Build up a description of the state and return it.
         */
        StringBuilder description = new StringBuilder();
        for (List<Object> sublist : list) {
            if (description.length() > 0) {
                description.append("]; ");
            }
            description.append("[");
            boolean firstDone = false;
            for (Object element : sublist) {
                if (firstDone) {
                    description.append("; ");
                } else {
                    firstDone = true;
                }
                description.append(element);
            }
        }
        description.append("]");
        return description.toString();
    }

    @Override
    protected void doSynchronizeComponentWidgetsToState() {

        /*
         * Remove all the previous table items.
         */
        table.removeAll();

        /*
         * Create the new table items.
         */
        createTableItemsForState();

        /*
         * Select the appropriate item, if one with the same first string
         * descriptor as the one selected before is found. Also find the item
         * that should be topmost, if one matches the one that was topmost
         * previously, and set it to be so.
         */
        Set<String> selectedDescriptors = displaySettings.getSelectedChoices();
        int topmostIndex = 0;
        displaySettings.setSelectedChoices(null);
        for (int j = 0; j < table.getItemCount(); j++) {
            TableItem item = table.getItem(j);
            String descriptor = getLineDescriptor(item);
            if ((selectedDescriptors != null)
                    && (selectedDescriptors.contains(descriptor))) {
                table.setSelection(item);
                displaySettings.setSelectedChoices(Sets.newHashSet(descriptor));
            }
            if (descriptor.equals(displaySettings.getTopmostVisibleChoice())) {
                topmostIndex = j;
            }
        }
        table.setTopIndex(topmostIndex);

        /*
         * Record the topmost visible line.
         */
        recordTopmostVisibleLine();
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
    }

    // Private Methods

    /**
     * Create the table items for the state.
     */
    private void createTableItemsForState() {
        for (List<Object> sublist : state) {
            TableItem item = new TableItem(table, SWT.NONE);
            for (int j = 0; (j < sublist.size())
                    && (j < table.getColumnCount()); j++) {
                item.setText(j, sublist.get(j).toString());
            }
        }
    }

    /**
     * Record the topmost visible line in the display settings.
     */
    private void recordTopmostVisibleLine() {
        if (table.getItemCount() > 0) {
            displaySettings.setTopmostVisibleChoice(getLineDescriptor(table
                    .getItem(table.getTopIndex())));
        } else {
            displaySettings.setTopmostVisibleChoice(null);
        }
    }

    /**
     * Get a map of the table row descriptors to their indices.
     * 
     * @return Map of table row descriptors to their indices.
     */
    private Map<String, Integer> getIndicesForLines() {
        Map<String, Integer> indicesForLines = new HashMap<>(
                table.getItemCount(), 1.0f);
        for (int j = 0; j < table.getItemCount(); j++) {
            indicesForLines.put(getLineDescriptor(table.getItem(j)), j);
        }
        return indicesForLines;
    }

    /**
     * Get the descriptor for the specified table row.
     * 
     * @param item
     *            Table row to be described.
     * @return Description.
     */
    private String getLineDescriptor(TableItem item) {
        return item.getText(0);
    }
}

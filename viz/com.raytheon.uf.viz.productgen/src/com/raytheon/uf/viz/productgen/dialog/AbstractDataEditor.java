/**
 * This software was developed and / or modified by Raytheon Company,
 * pursuant to Contract DG133W-05-CQ-1067 with the US Government.
 * 
 * U.S. EXPORT CONTROLLED TECHNICAL DATA
 * This software product contains export-restricted data whose
 * export/transfer/disclosure is restricted by U.S. law. Dissemination
 * to non-U.S. persons whether in the United States or abroad requires
 * an export license or other authorization.
 * 
 * Contractor Name:        Raytheon Company
 * Contractor Address:     6825 Pine Street, Suite 340
 *                         Mail Stop B8
 *                         Omaha, NE 68106
 *                         402.291.0100
 * 
 * See the AWIPS II Master Rights File ("Master Rights File.pdf") for
 * further licensing information.
 **/
package com.raytheon.uf.viz.productgen.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import com.raytheon.uf.common.hazards.productgen.IGeneratedProduct;

/**
 * 
 * Abstract data editor class. Currently, there are two implementations of this
 * class. The first begin the ProductDataEditor. The GUI for this editor is
 * created by the Megawidgets library and contains the editable data that can be
 * modified by the user. The second implementation is the
 * FormattedTextDataEditor. This editor contains the formatted text from the
 * formatter. The text in this editor may be freehand edited by the user based
 * on which product parts are editable.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * 01/15/2015   5109       bphillip     Initial creation
 * 03/11/2015   6889       bphillip     Changed revertButton to undoButton.  More than one undo is now allowed.
 * 03/23/2015   7165       Robert.Blum  Adding * to editor tab and product tab
 *                                      labels when there are unsaved changes.
 * 04/16/2015   7579       Robert.Blum  Removed the Save Button from the Product Editor.
 *                                      Saving the edits are required to generate the correct
 *                                      product, so saving is now done automatically.
 * 05/04/2015   6979       Robert.Blum  Adding Save button back as well as making the save and
 *                                      undo buttons always enabled.
 * 
 * </pre>
 * 
 * @author bphillip
 * @version 1.0
 */
public abstract class AbstractDataEditor extends CTabItem {

    /** Label for the Save button on the editor tab */
    private static final String SAVE_BUTTON_LABEL = "Save";

    /** Label for the Undo button on the editor tab */
    private static final String UNDO_BUTTON_LABEL = "Undo";

    /** The number of buttons on the GUI */
    private static final int BUTTON_COUNT = 2;

    /** Horizontal spacing between Save and Undo buttons */
    private static final int HORIZONTAL_BUTTON_SPACING = 65;

    /** The composite which holds the editor */
    protected Composite editorPane;

    /** The composite which holds the buttons */
    protected Composite editorButtonPane;

    /** The save button widget */
    protected Button saveButton;

    /** The undo button widget */
    protected Button undoButton;

    /** The parent product editor instance to which this data editor belongs */
    protected ProductEditor productEditor;

    /** The generated product associated with this data editor */
    protected IGeneratedProduct product;

    /** Container holding the editable keys for this product */
    protected EditableKeys editableKeys;

    /** The product CTabItem to which this data editor belongs */
    protected CTabItem productTab;

    /**
     * Creates a new Data Editor
     * 
     * @param productEditor
     *            The parent product editor instance
     * @param product
     *            The product associated with this data editor
     * @param parent
     *            The parent tab folder
     * @param style
     *            Style hints
     */
    protected AbstractDataEditor(ProductEditor productEditor,
            CTabItem productTab,
            IGeneratedProduct product, CTabFolder parent, int style) {
        super(parent, style);
        this.productEditor = productEditor;
        this.productTab = productTab;
        this.product = product;

        // Initialize the composite which will hold the data editor
        editorPane = new Composite(parent, SWT.NONE);
        ProductEditorUtil.setLayoutInfo(editorPane, 1, false, SWT.FILL,
                SWT.FILL, true, true);
        setControl(editorPane);
    }

    /**
     * Refreshes the tab contents
     */
    public abstract void refresh();

    /**
     * Checks if this data editor has unsaved changes
     * 
     * @return True if unsaved changes are present, else false
     */
    public abstract boolean hasUnsavedChanges();

    /**
     * Checks if this editor contains editable information
     * 
     * @return True if this editor contains editable information, else false
     */
    public abstract boolean isDataEditable();

    /**
     * Checks if the user has completed all required fields
     * 
     * @return True if the user has completed all required fields, else false
     */
    public abstract boolean requiredFieldsCompleted();

    /**
     * Saves any modified values
     */
    public abstract void saveModifiedValues();

    /**
     * Reverts all changes back to original state
     */
    public abstract void revertValues();

    /**
     * Undoes the last modification to a value
     */
    public abstract void undoModification();

    /**
     * Called by initialize. This method allows subclasses of AbstractDataEditor
     * to make GUI contributes prior to adding the buttons on the bottom of the
     * editor panel
     */
    protected abstract void initializeSubclass();

    /**
     * Gets whether there are modifications which can be undone
     * 
     * @return True if modifications are available to be undone
     */
    protected abstract boolean undosRemaining();

    /**
     * Returns the number of actions that can be undone
     * 
     * @return The number of actions that can be undone
     */
    protected abstract int getUndosRemaining();

    /**
     * Initializes the GUI components for this data editor. Delegates to the
     * initializeSubclass method so the subclass may add any GUI contributes
     * first. Then adds and disables the buttons to the data editor
     */
    protected void initialize() {

        // Create the data editor specific GUI components
        initializeSubclass();

        // Put the buttons on the bottom of the data editor
        createEditorButtons(editorPane);
    }

    /**
     * Creates the Save and Undo buttons
     * 
     * @param editorPane
     *            The parent composite
     */
    protected void createEditorButtons(Composite editorPane) {

        // Initialize the composite to hold the buttons for the editor
        editorButtonPane = new Composite(editorPane, SWT.NONE);
        GridLayout buttonCompLayout = new GridLayout(getButtonCount(), false);
        buttonCompLayout.horizontalSpacing = HORIZONTAL_BUTTON_SPACING;
        GridData buttonCompData = new GridData(SWT.CENTER, SWT.CENTER, true,
                false);
        editorButtonPane.setLayout(buttonCompLayout);
        editorButtonPane.setLayoutData(buttonCompData);
        /*
         * Create the buttons for the editor tab
         */

        saveButton = new Button(editorButtonPane, SWT.PUSH);
        undoButton = new Button(editorButtonPane, SWT.PUSH);

        /*
         * Configure Save button
         */
        saveButton.setText(SAVE_BUTTON_LABEL);
        ProductEditorUtil.setButtonGridData(saveButton);
        saveButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                saveModifiedValues();
                updateTabLabel();
            }
        });

        // Editor save button always enabled.
        saveButton.setEnabled(true);

        /*
         * Configure Undo button
         */
        undoButton.setText(UNDO_BUTTON_LABEL);
        ProductEditorUtil.setButtonGridData(undoButton);
        undoButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                undoModification();
                updateButtonState();
                updateTabLabel();
            }
        });

        // Editor Undo button is always enabled.
        undoButton.setEnabled(true);
    }

    /**
     * Updates the text on the undo button.
     */
    protected void updateButtonState() {
        // Update the undo button with how many undo actions are available
        if (undosRemaining()) {
            undoButton.setText(UNDO_BUTTON_LABEL + "(" + getUndosRemaining()
                    + ")");
        } else {
            undoButton.setText(UNDO_BUTTON_LABEL);
        }
    }

    /**
     * Allow for updating the tab label with an "*" to visually show that the
     * tab has unsaved changes.
     */
    protected void updateTabLabel() {
        String prevLabel = this.getText();
        String prevProductLabel = this.productTab.getText();
        if (hasUnsavedChanges()) {
            // Add the asterisk to the tab label
            if (prevLabel.startsWith("*") == false) {
                setText("*" + prevLabel);
            }
            // Add the asterisk to the product tab label
            if (prevProductLabel.startsWith("*") == false) {
                productTab.setText("*" + prevProductLabel);
            }
        } else {
            prevLabel = prevLabel.replace("*", "");
            setText(prevLabel);
            prevProductLabel = prevProductLabel.replace("*", "");
            productTab.setText(prevProductLabel);
        }
    }

    /**
     * Retrieves the number of buttons present on the editor GUI. The default
     * value is 2 (Save Button and Undo Button).
     * 
     * @return The number of buttons present on the button composite
     */
    protected int getButtonCount() {
        return BUTTON_COUNT;
    }

}

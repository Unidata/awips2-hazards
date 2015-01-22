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

    /** The composite which holds the editor */
    protected Composite editorPane;

    /** The composite which holds the buttons */
    protected Composite editorButtonPane;

    /** The save button widget */
    protected Button saveButton;

    /** The revert button widget */
    protected Button revertButton;

    /** The parent product editor instance to which this data editor belongs */
    protected ProductEditor productEditor;

    /** The generated product associated with this data editor */
    protected IGeneratedProduct product;

    /** Container holding the editable keys for this product */
    protected EditableKeys editableKeys;

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
            IGeneratedProduct product, CTabFolder parent, int style) {
        super(parent, style);
        this.productEditor = productEditor;
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
     * Called by initialize. This method allows subclasses of AbstractDataEditor
     * to make GUI contributes prior to adding the buttons on the bottom of the
     * editor panel
     */
    protected abstract void initializeSubclass();

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
        disableButtons();
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
        GridData buttonCompData = new GridData(SWT.CENTER, SWT.CENTER, true,
                false);
        editorButtonPane.setLayout(buttonCompLayout);
        editorButtonPane.setLayoutData(buttonCompData);
        /*
         * Create the buttons for the editor tab
         */

        saveButton = new Button(editorButtonPane, SWT.PUSH);
        revertButton = new Button(editorButtonPane, SWT.PUSH);

        /*
         * Configure Save button
         */
        saveButton.setText(SAVE_BUTTON_LABEL);
        ProductEditorUtil.setButtonGridData(saveButton);
        saveButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                saveModifiedValues();
                disableSaveButton();
                disableRevertButton();
            }
        });

        // Editor save button is initially disabled
        saveButton.setEnabled(false);

        /*
         * Configure Undo button
         */
        revertButton.setText(UNDO_BUTTON_LABEL);
        ProductEditorUtil.setButtonGridData(revertButton);
        revertButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                revertValues();
                if (hasUnsavedChanges()) {
                    enableSaveButton();
                } else {
                    disableSaveButton();
                }
                disableRevertButton();
            }
        });

        // Editor Undo button is initially disabled
        revertButton.setEnabled(false);
    }

    /**
     * Enables the save and revert buttons
     */
    protected void enableButtons() {
        enableSaveButton();
        enableRevertButton();
    }

    /**
     * Disables the save and revert buttons
     */
    protected void disableButtons() {
        disableSaveButton();
        disableRevertButton();
    }

    /**
     * Enables the save button
     */
    protected void enableSaveButton() {
        setButtonEnabled(this.saveButton,true);
    }

    /**
     * Enables the revert button
     */
    protected void enableRevertButton() {
        setButtonEnabled(this.revertButton, true);
    }

    /**
     * Disables the save button
     */
    protected void disableSaveButton() {
        setButtonEnabled(this.saveButton, false);
    }

    /**
     * Disables the revert button
     */
    protected void disableRevertButton() {
        setButtonEnabled(this.revertButton,false);
    }

    /**
     * Enables/Disables a button
     * @param button The button to enable/disabled
     * @param enabled True if the button is to be enabled, false for disabled
     */
    protected void setButtonEnabled(Button button, boolean enabled){
        if(button != null && !button.isDisposed()){
            button.setEnabled(enabled);
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

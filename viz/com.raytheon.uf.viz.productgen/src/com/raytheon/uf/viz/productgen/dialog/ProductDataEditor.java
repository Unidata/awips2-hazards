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

import gov.noaa.gsd.viz.megawidgets.IControlSpecifier;
import gov.noaa.gsd.viz.megawidgets.IParametersEditorListener;
import gov.noaa.gsd.viz.megawidgets.MegawidgetException;
import gov.noaa.gsd.viz.megawidgets.MegawidgetManager;
import gov.noaa.gsd.viz.megawidgets.MegawidgetStateException;
import gov.noaa.gsd.viz.megawidgets.ParametersEditorFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.raytheon.uf.common.hazards.productgen.EditableEntryMap;
import com.raytheon.uf.common.hazards.productgen.IGeneratedProduct;
import com.raytheon.uf.common.hazards.productgen.KeyInfo;
import com.raytheon.uf.common.hazards.productgen.editable.ProductTextUtil;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.util.Pair;

/**
 * If there are any editable keys in the data dictionary returned by the product
 * generators then this composite will be used.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Jun 13, 2014 3519       jsanchez     Initial creation
 * Jun 24, 2014 4010       Chris.Golden Changed to work with parameters
 *                                      editor changes.
 * Jun 30, 2014 3512       Chris.Golden Changed to work with more
 *                                      parameters editor changes.
 * 01/15/2015   5109       bphillip     Refactored/Renamed
 * 03/11/2015   6889       bphillip     Modifications to allow more than one undo action in the Product Editor
 * 03/19/2015   7108       Robert.Blum  Rename Raw Data to Hazard Data Editor.
 * 03/23/2015   7165       Robert.Blum  Modifications to allow adding * to editor tab and product tab
 *                                      labels when there are unsaved changes.
 * 04/16/2015   7579       Robert.Blum  Changes for amended Product Editor design.
 * 05/07/2015   6979       Robert.Blum  Removed automatic saving of changed/reverted values.
 *                                      Also save/undo buttons are always enabled due to issues
 *                                      being able to correctly enable them via the megawidgets.
 * </pre>
 * 
 * @author jsanchez
 * @version 1.0
 */

public class ProductDataEditor extends AbstractDataEditor {

    /** The log handler */
    private static final IUFStatusHandler handler = UFStatus
            .getHandler(ProductDataEditor.class);

    /** Label used for the tab */
    private static final String TAB_LABEL = "Hazard Data Editor";

    /**
     * The MegawidgetManager responsible for generating the GUI components for
     * this product
     */
    private MegawidgetManager manager;

    /**
     * The history of modifications made to editable data.
     */
    private LinkedList<Pair<KeyInfo, EditableKeyInfo>> modificationHistory = new LinkedList<Pair<KeyInfo, EditableKeyInfo>>();

    /**
     * Creates a new ProductDataEditor
     * 
     * @param productEditor
     *            The parent product editor creating this
     * @param product
     *            The generated product associated with this editor
     * @param parent
     *            The CTabFolder parent object
     * @param style
     *            SWT style flags
     */
    protected ProductDataEditor(ProductEditor productEditor,
            CTabItem productTab,
            IGeneratedProduct product, CTabFolder parent, int style) {
        super(productEditor, productTab, product, parent, style);
    }

    /**
     * Creates the product specific GUI components using the megawidgets library
     * 
     * @param parent
     *            The parent composite to create the GUI components
     */
    protected void initializeSubclass() {

        // Determine the editable keys present in this product
        editableKeys = new EditableKeys(product);

        // Set the tab label
        setText(TAB_LABEL);

        if (isDataEditable() == false) {
            handler.info("There are no editable fields. The data editor cannot be created.");
            return;
        }

        // Create the scroller composite and the layouts
        ScrolledComposite scrollerComposite = new ScrolledComposite(editorPane,
                SWT.BORDER | SWT.V_SCROLL);
        ProductEditorUtil.setLayoutInfo(scrollerComposite, 1, false, SWT.FILL,
                SWT.FILL, true, true, 500, 300);
        Composite parentComposite = new Composite(scrollerComposite, SWT.BORDER);
        ProductEditorUtil.setLayoutInfo(parentComposite, 1, false, SWT.FILL,
                SWT.FILL, true, true);

        /*
         * Create the data structures necessary to pass to the megawidgets
         * library
         */
        List<KeyInfo> keyInfos = new ArrayList<>();
        Map<KeyInfo, Object> valuesForKeyInfos = new HashMap<>();
        for (KeyInfo key : editableKeys.getKeyInfos()) {
            keyInfos.add(key);
            valuesForKeyInfos.put(key, editableKeys.getValue(key));
        }

        try {
            /*
             * Use factory to create megawidgets
             */
            ParametersEditorFactory factory = new ParametersEditorFactory();
            manager = factory.buildParametersEditor(parentComposite, keyInfos,
                    valuesForKeyInfos, System.currentTimeMillis()
                            - TimeUnit.DAYS.toMillis(1L),
                    System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1L),
                    ProductEditorUtil.currentTimeProvider,
                    new IParametersEditorListener<KeyInfo>() {
                        @Override
                        public void parameterValueChanged(KeyInfo keyInfo,
                                Object value) {
                            EditableKeyInfo editableKeyInfo = editableKeys
                                    .getEditableKeyInfo(keyInfo);
                            // Add a new entry in the undo queue
                            modificationHistory.add(new Pair<KeyInfo, EditableKeyInfo>(
                                    keyInfo, editableKeyInfo));

                            Serializable newValue = (Serializable) value;

                            // Update the map with the new value
                            updateKeyInfoValue(editableKeyInfo, keyInfo,
                                    newValue);

                            // Update the editable key value
                            editableKeyInfo.updateValue(newValue);

                            // Update the enabled state of the save and undo buttons
                            updateButtonState();

                            // Update the tab label
                            updateTabLabel();

                            // Regenerate with the updated data
                            productEditor.regenerate(keyInfo);
                        }

                        @Override
                        public void parameterValuesChanged(
                                Map<KeyInfo, Object> valuesForLabels) {
                            for (Map.Entry<KeyInfo, Object> entry : valuesForLabels
                                    .entrySet()) {
                                parameterValueChanged(entry.getKey(),
                                        entry.getValue());
                            }
                        }

                        @Override
                        public void sizeChanged(KeyInfo parameter) {

                            /*
                             * TODO: If resizable megawidgets are to be used
                             * (i.e. if any parameter types are to be registered
                             * as expandable with the parameters editor
                             * factory), respond to this notification by
                             * resizing the scrollable area as appropriate.
                             */
                            throw new UnsupportedOperationException(
                                    "not yet implemented");
                        }
                    });

            // Disables 'displayable'
            // TODO Check if the megawidget for a list handled editable
            List<KeyInfo> displayableKeys = editableKeys.getDisplayableKeys();
            if (displayableKeys.isEmpty() == false) {
                Map<String, Map<String, Object>> mutablePropertiesMap = manager
                        .getMutableProperties();

                for (KeyInfo displayableKey : displayableKeys) {
                    Map<String, Object> properties = mutablePropertiesMap
                            .get(displayableKey.toString());
                    if (properties != null) {
                        properties.put(IControlSpecifier.MEGAWIDGET_EDITABLE,
                                Boolean.FALSE);
                    }
                }

                manager.setMutableProperties(mutablePropertiesMap);
            }

        } catch (MegawidgetException e) {
            handler.error("Error creating megawidgets: " + e, e);
        }

        scrollerComposite.setExpandHorizontal(true);
        scrollerComposite.setExpandVertical(true);
        scrollerComposite.setContent(parentComposite);
        scrollerComposite.setMinSize(parentComposite.computeSize(SWT.DEFAULT,
                SWT.DEFAULT));
        scrollerComposite.layout();
    }

    /**
     * Updates the value held in the keyinfo object
     * 
     * @param editableKeyInfo
     *            The editable key info
     * @param keyInfo
     *            The key info object
     * @param newValue
     *            The new value to assign to the keyinfo object
     */
    private void updateKeyInfoValue(EditableKeyInfo editableKeyInfo,
            KeyInfo keyInfo, Serializable newValue) {
        String format = editableKeyInfo.getFormat();
        for (EditableEntryMap map : product.getEditableEntries()) {
            if (map.getFormat().equals(format)) {
                map.getEditableEntries().put(keyInfo, newValue);
            }
        }
    }

    public void updateValues(IGeneratedProduct product) {
        editableKeys.updateEditableKeys(product);
        Map<String, Object> state = manager.getState();
        for (KeyInfo key : editableKeys.getKeyInfos()) {
            EditableKeyInfo editableKeyInfo = editableKeys
                    .getEditableKeyInfo(key);
            // updates the values displayed in the GUI
            state.put(key.toString(), editableKeyInfo.getOriginalValue());

        }

        try {
            manager.setState(state);
        } catch (MegawidgetStateException exception) {
            handler.error("Error trying to reset megawidget state: "
                    + exception, exception);
        }
    }

    @Override
    public void revertValues() {
        Map<String, Object> state = manager.getState();
        for (KeyInfo key : editableKeys.getKeyInfos()) {
            EditableKeyInfo editableKeyInfo = editableKeys
                    .getEditableKeyInfo(key);
            if (editableKeyInfo.isModified()) {
                editableKeyInfo.revertToOriginalValue();
                updateKeyInfoValue(editableKeyInfo, key,editableKeyInfo.getOriginalValue());
                // updates the values displayed in the GUI
                state.put(key.toString(), editableKeyInfo.getOriginalValue());
            }

        }
        // regenerate with updated data
        productEditor.regenerate(null);
        try {
            manager.setState(state);
        } catch (MegawidgetStateException exception) {
            handler.error("Error trying to reset megawidget state: "
                    + exception, exception);
        }
    }

    @Override
    public void undoModification() {
        if (modificationHistory.isEmpty()) {
            String undoText = "No modifications to undo.";
            Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                    .getShell();
            MessageBox messageBox = new MessageBox(shell, SWT.OK | SWT.ICON_INFORMATION);
            messageBox.setText("Unable to undo");
            messageBox.setMessage(undoText);
            messageBox.open();
            handler.info(undoText);
        } else {
            Map<String, Object> state = manager.getState();
            
            // Gets the previous state information
            Pair<KeyInfo, EditableKeyInfo> keyInfoPair = modificationHistory.removeLast();
            
            // Retrieve the key info object
            KeyInfo keyInfo = keyInfoPair.getFirst();
            
            // Retrieve the editable key info object
            EditableKeyInfo editableKeyInfo = keyInfoPair.getSecond();

            // Update the current value with the previous value
            updateKeyInfoValue(editableKeyInfo, keyInfo,
                    editableKeyInfo.getLastValue());
            
            // Revert the editable key info object to the previous state
            editableKeyInfo.revertToLastValue();
            
            // updates the values displayed in the GUI
            state.put(keyInfo.toString(), editableKeyInfo.getValue());

            // Regenerate with the updated data
            productEditor.regenerate(null);
            try {
                manager.setState(state);
            } catch (MegawidgetStateException exception) {
                handler.error("Error trying to reset megawidget state: "
                        + exception, exception);
            }
        }
    }

    @Override
    protected boolean undosRemaining() {
        return !modificationHistory.isEmpty();
    }

    @Override
    protected int getUndosRemaining() {
        return modificationHistory.size();
    }

    @Override
    public void refresh() {
        // no op
    }

    public boolean isDataEditable() {
        return !editableKeys.isEmpty();
    }

    /**
     * Checks to see if there are any required fields that needs to be
     * completed.
     */

    public boolean requiredFieldsCompleted() {
        for (KeyInfo keyInfo : editableKeys.getKeyInfos()) {
            Serializable value = editableKeys.getModifiedValue(keyInfo, false);
            if (keyInfo.isRequired()
                    && (value == null || String.valueOf(value).trim().length() == 0)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean hasUnsavedChanges() {
        return editableKeys.isModified();
    }

    @Override
    public void saveModifiedValues() {
        boolean showMB = true;
        for (KeyInfo keyInfo : editableKeys.getKeyInfos()) {
            Serializable value = editableKeys.getModifiedValue(keyInfo, true);
            if (value != null) {
                showMB = false;
                ProductTextUtil.createOrUpdateProductText(keyInfo.getName(),
                        keyInfo.getProductCategory(), keyInfo.getProductID(),
                        keyInfo.getSegment(),
                        new ArrayList<Integer>(keyInfo.getEventIDs()), value);
            }
        }

        if (showMB) {
            String saveText = "No modifications to save.";
            Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                    .getShell();
            MessageBox messageBox = new MessageBox(shell, SWT.OK | SWT.ON_TOP
                    | SWT.ICON_INFORMATION);
            messageBox.setText("Unable to save");
            messageBox.setMessage(saveText);
            messageBox.open();
            handler.info(saveText);
        }

        editableKeys.clearModifiedValues();
    }

    /**
     * Returns the last modified value
     * 
     * @return
     */
    public Serializable getLastModifiedValue() {
        Serializable value = null;
        for (KeyInfo keyInfo : editableKeys.getKeyInfos()) {
            Serializable modifiedValue = editableKeys.getModifiedValue(keyInfo,
                    false);
            if (modifiedValue != null) {
                value = modifiedValue;
            }
        }
        return value;
    }
}

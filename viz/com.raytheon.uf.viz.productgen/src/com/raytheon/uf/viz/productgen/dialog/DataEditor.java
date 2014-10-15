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

import gov.noaa.gsd.common.utilities.ICurrentTimeProvider;
import gov.noaa.gsd.viz.megawidgets.IControlSpecifier;
import gov.noaa.gsd.viz.megawidgets.IParametersEditorListener;
import gov.noaa.gsd.viz.megawidgets.MegawidgetException;
import gov.noaa.gsd.viz.megawidgets.MegawidgetManager;
import gov.noaa.gsd.viz.megawidgets.MegawidgetStateException;
import gov.noaa.gsd.viz.megawidgets.ParametersEditorFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;

import com.raytheon.uf.common.hazards.productgen.KeyInfo;
import com.raytheon.uf.common.hazards.productgen.editable.ProductTextUtil;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.SimulatedTime;

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
 * </pre>
 * 
 * @author jsanchez
 * @version 1.0
 */

public class DataEditor {
    private static final IUFStatusHandler handler = UFStatus
            .getHandler(DataEditor.class);

    private final ProductEditor productEditor;

    private final LinkedHashMap<KeyInfo, Serializable> data;

    private MegawidgetManager manager;

    private final Map<KeyInfo, EditableKeyInfo> editableKeyInfoMap = new LinkedHashMap<>();

    private class EditableKeyInfo {

        List<KeyInfo> path;

        boolean modified = false;

        boolean isDisplayable = false;

        Serializable value;

        Serializable originalValue;

        Serializable previousValue;
    }

    private final ICurrentTimeProvider currentTimeProvider = new ICurrentTimeProvider() {
        @Override
        public long getCurrentTime() {
            return SimulatedTime.getSystemTime().getMillis();
        }
    };

    public DataEditor(ProductEditor productEditor,
            LinkedHashMap<KeyInfo, Serializable> data) {
        this.productEditor = productEditor;
        this.data = data;
        determineEditableKeyPaths(null, data, 0);
    }

    public void create(Composite comp) {
        if (isDataEditable() == false) {
            handler.info("There are no editable fields. The data editor cannot be created.");
            return;
        }
        // Setting up the scroller composite and the layouts
        ScrolledComposite scrollerComposite = new ScrolledComposite(comp,
                SWT.BORDER | SWT.V_SCROLL);
        ProductEditor.setLayoutInfo(scrollerComposite, 1, false, SWT.FILL,
                SWT.FILL, true, true, new Point(ProductEditor.ENTRY_PANE_WIDTH,
                        ProductEditor.ENTRY_PANE_HEIGHT));
        Composite parent = new Composite(scrollerComposite, SWT.BORDER);
        ProductEditor.setLayoutInfo(parent, 1, false, SWT.FILL, SWT.FILL, true,
                true, null);

        List<KeyInfo> keyInfos = new ArrayList<>();
        Map<KeyInfo, Object> valuesForKeyInfos = new HashMap<>();
        for (KeyInfo key : new ArrayList<>(editableKeyInfoMap.keySet())) {
            keyInfos.add(key);
            EditableKeyInfo editableKeyInfo = editableKeyInfoMap.get(key);
            valuesForKeyInfos.put(key, editableKeyInfo.value);
        }

        try {
            // Use factory to create megawidgets
            ParametersEditorFactory factory = new ParametersEditorFactory();
            manager = factory.buildParametersEditor(parent, keyInfos,
                    valuesForKeyInfos, System.currentTimeMillis()
                            - TimeUnit.DAYS.toMillis(1L),
                    System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1L),
                    currentTimeProvider,
                    new IParametersEditorListener<KeyInfo>() {
                        @Override
                        public void parameterValueChanged(KeyInfo keyInfo,
                                Object value) {
                            EditableKeyInfo editableKeyInfo = editableKeyInfoMap
                                    .get(keyInfo);

                            Serializable newValue = (Serializable) value;

                            // update the dict with the new value
                            update(editableKeyInfo, keyInfo, newValue);
                            // regenerate with updated data
                            productEditor.regenerate();

                            // mark editableKeyInfo as modified
                            editableKeyInfo.previousValue = editableKeyInfo.value;
                            editableKeyInfo.value = newValue;
                            editableKeyInfo.modified = true;

                            productEditor.getIssueButton().setEnabled(
                                    requiredFieldsCompleted());
                            productEditor.getSaveButton().setEnabled(true);
                            productEditor.getRevertButton().setEnabled(true);
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
            List<KeyInfo> displayableKeys = getDisplayableKeys();
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
        scrollerComposite.setContent(parent);
        scrollerComposite.setMinSize(parent.computeSize(SWT.DEFAULT,
                SWT.DEFAULT));
        scrollerComposite.layout();
    }

    /**
     * Returns the list of editable keys of the data dictionary.
     * 
     * @return
     */
    public List<KeyInfo> getEditableKeys() {

        /*
         * The list will always be in the same order for a given map between
         * calls to determineEditableKeyPaths(); a list is created and returned
         * instead of simply returning the key set in order to indicate that
         * ordering will be maintained.
         */
        return new ArrayList<>(editableKeyInfoMap.keySet());
    }

    @SuppressWarnings("unchecked")
    private void update(EditableKeyInfo editableKeyInfo, KeyInfo keyInfo,
            Serializable newValue) {
        // get path in the dict
        List<KeyInfo> path = new ArrayList<KeyInfo>();
        if (editableKeyInfo.path != null && !editableKeyInfo.path.isEmpty()) {
            path.addAll(editableKeyInfo.path);
        }
        path.add(keyInfo);

        Map<KeyInfo, Serializable> currentDataMap = data;
        for (int counter = 0; counter < path.size(); counter++) {
            KeyInfo key = path.get(counter);
            Serializable currentValue = currentDataMap.get(key);

            if (counter == path.size() - 1 && currentDataMap.containsKey(key)) {
                currentDataMap.put(key, newValue);
                break;
            } else if (currentValue instanceof Map<?, ?>) {
                currentDataMap = (Map<KeyInfo, Serializable>) currentValue;
            } else if (currentValue instanceof ArrayList<?>) {
                /*
                 * need to increment - going down another level
                 */
                KeyInfo nextKey = path.get(++counter);
                int index = nextKey.getIndex();
                List<?> list = (ArrayList<?>) currentValue;

                if (list.get(index) instanceof Map<?, ?>) {
                    Map<KeyInfo, Serializable> map = (Map<KeyInfo, Serializable>) list
                            .get(index);
                    if ((counter == path.size() - 1)
                            && map.containsKey(nextKey)) {
                        map.put(nextKey, newValue);
                        break;
                    } else if (map.get(nextKey) instanceof Map<?, ?>) {
                        // found map to use
                        currentDataMap = (Map<KeyInfo, Serializable>) map
                                .get(nextKey);
                    } else if (map.get(nextKey) instanceof ArrayList<?>) {
                        currentDataMap = map;
                        counter--;
                    }
                }
            }
        }
    }

    public void revertValues() {
        Map<String, Object> state = manager.getState();
        for (KeyInfo key : editableKeyInfoMap.keySet()) {
            EditableKeyInfo editableKeyInfo = editableKeyInfoMap.get(key);
            if (editableKeyInfo.modified) {
                editableKeyInfo.value = editableKeyInfo.previousValue;
                editableKeyInfo.previousValue = null;

                if (editableKeyInfo.value == editableKeyInfo.originalValue) {
                    editableKeyInfo.modified = false;
                }

                update(editableKeyInfo, key, editableKeyInfo.value);
                // updates the values displayed in the GUI
                state.put(key.toString(), editableKeyInfo.value);
            }

        }
        // regenerate with updated data
        productEditor.regenerate();
        try {
            manager.setState(state);
        } catch (MegawidgetStateException exception) {
            handler.error("Error trying to reset megawidget state: "
                    + exception, exception);
        }
    }

    private void clearModifiedValues() {
        for (EditableKeyInfo editableKeyInfo : editableKeyInfoMap.values()) {
            editableKeyInfo.modified = false;
        }
    }

    public boolean isDataEditable() {
        return !editableKeyInfoMap.isEmpty();
    }

    public List<KeyInfo> getDisplayableKeys() {
        List<KeyInfo> displayableKeys = new ArrayList<>();
        for (Entry<KeyInfo, EditableKeyInfo> entry : editableKeyInfoMap
                .entrySet()) {
            if (entry.getValue().isDisplayable) {
                displayableKeys.add(entry.getKey());
            }
        }
        return displayableKeys;
    }

    /**
     * Checks to see if there are any required fields that needs to be
     * completed.
     */

    public boolean requiredFieldsCompleted() {
        for (KeyInfo editableKey : editableKeyInfoMap.keySet()) {
            Serializable value = getModifiedValue(editableKey, false);
            if (editableKey.isRequired()
                    && (value == null || String.valueOf(value).trim().length() == 0)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the last modified value
     * 
     * @return
     */
    public Serializable getOneModifiedValue() {
        Serializable value = null;
        for (KeyInfo keyInfo : editableKeyInfoMap.keySet()) {
            Serializable modifiedValue = getModifiedValue(keyInfo, false);
            if (modifiedValue != null) {
                value = modifiedValue;
            }
        }
        return value;
    }

    public Serializable getModifiedValue(KeyInfo editableKey, boolean saving) {
        EditableKeyInfo editableKeyInfo = editableKeyInfoMap.get(editableKey);
        if (editableKeyInfo.modified) {
            if (saving) {
                editableKeyInfo.originalValue = editableKeyInfo.value;
                editableKeyInfo.modified = false;
            }
            return editableKeyInfo.value;
        }
        return null;
    }

    public boolean hasUnsavedChanges() {
        for (EditableKeyInfo value : editableKeyInfoMap.values()) {
            if (value.modified) {
                return true;
            }
        }
        return false;
    }

    public void saveModifiedValues() {
        for (KeyInfo editableKey : editableKeyInfoMap.keySet()) {
            Serializable value = getModifiedValue(editableKey, true);
            if (value != null) {
                ProductTextUtil.createOrUpdateProductText(
                        editableKey.getName(),
                        editableKey.getProductCategory(),
                        editableKey.getProductID(), editableKey.getSegment(),
                        new ArrayList<Integer>(editableKey.getEventIDs()),
                        value);
            }
        }

        clearModifiedValues();
    }

    /**
     * Determines the path of the editable keys. The path are key values of
     * dictionaries/maps within dictionaries/maps
     * 
     * @param parentPath
     * @param data
     */
    @SuppressWarnings("unchecked")
    private void determineEditableKeyPaths(List<KeyInfo> parentPath,
            Map<KeyInfo, Serializable> data, int index) {

        if (data != null) {
            for (Entry<KeyInfo, Serializable> entry : data.entrySet()) {
                KeyInfo key = entry.getKey();
                Serializable entryValue = entry.getValue();
                boolean isEditable = key.isEditable();
                boolean isDisplayable = key.isDisplayable();

                key.setIndex(index);
                if (isEditable || isDisplayable) {
                    EditableKeyInfo info = new EditableKeyInfo();
                    info.path = parentPath;
                    info.value = entry.getValue();
                    info.originalValue = entry.getValue();
                    info.isDisplayable = isDisplayable;
                    editableKeyInfoMap.put(key, info);
                } else if (entryValue instanceof Map<?, ?>) {
                    Map<KeyInfo, Serializable> subdata = (Map<KeyInfo, Serializable>) entry
                            .getValue();
                    determineEditableKeyPaths(createPath(parentPath, key),
                            subdata, 0);
                } else if (entryValue instanceof ArrayList) {
                    List<Serializable> list = (ArrayList<Serializable>) entry
                            .getValue();
                    determineEditableKeyPaths(createPath(parentPath, key), list);
                }
            }
        }
    }

    private List<KeyInfo> createPath(List<KeyInfo> parentPath, KeyInfo latestKey) {
        List<KeyInfo> path = new ArrayList<>();
        if (parentPath != null) {
            path.addAll(parentPath);
        }
        path.add(latestKey);
        return path;
    }

    @SuppressWarnings("unchecked")
    private void determineEditableKeyPaths(List<KeyInfo> parentPath,
            List<Serializable> list) {
        for (int index = 0; index < list.size(); index++) {
            Serializable item = list.get(index);
            if (item instanceof Map<?, ?>) {
                determineEditableKeyPaths(parentPath,
                        (Map<KeyInfo, Serializable>) item, index);
            } else if (item instanceof ArrayList) {
                determineEditableKeyPaths(parentPath, (List<Serializable>) item);
            }
        }
    }
}

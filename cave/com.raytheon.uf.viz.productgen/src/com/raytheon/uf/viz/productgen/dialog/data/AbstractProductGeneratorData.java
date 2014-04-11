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
package com.raytheon.uf.viz.productgen.dialog.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.swt.custom.StyledText;

import com.raytheon.uf.common.hazards.productgen.KeyInfo;

/**
 * Abstract class to represent the different parts of a product generator data.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 25, 2014            jsanchez     Initial creation
 * 
 * </pre>
 * 
 * @author jsanchez
 * @version 1.0
 */

public abstract class AbstractProductGeneratorData {

    private Map<KeyInfo, Serializable> data;

    protected String segmentID;

    private class EditableKeyInfo {

        List<KeyInfo> path;

        boolean modified = false;

        boolean isDisplayable = false;

        Serializable value;

        Serializable previousValue;
    }

    private Map<KeyInfo, EditableKeyInfo> editableKeyInfoMap = new HashMap<KeyInfo, EditableKeyInfo>();

    public AbstractProductGeneratorData(Map<KeyInfo, Serializable> data,
            String segmentID) {
        this.data = data;
        this.segmentID = segmentID;
        determineEditableKeyPaths(null, data, false);
    }

    abstract public String getDescriptionName();

    public String getSegmentID() {
        return segmentID;
    }

    abstract public void highlight(StyledText styledText);

    /**
     * Returns the set of editable keys of the data dictionary
     * 
     * @return
     */
    public Set<KeyInfo> getEditableKeys() {
        return editableKeyInfoMap.keySet();
    }

    public List<KeyInfo> getDisplayableKeys() {
        List<KeyInfo> displayableKeys = new ArrayList<KeyInfo>();
        for (Entry<KeyInfo, EditableKeyInfo> entry : editableKeyInfoMap
                .entrySet()) {
            if (entry.getValue().isDisplayable) {
                displayableKeys.add(entry.getKey());
            }
        }
        return displayableKeys;
    }

    public Map<KeyInfo, Serializable> getData() {
        return data;
    }

    /**
     * Returns the initial value for the editableKey.
     * 
     * @param editableKey
     * @return
     */
    public Serializable getValue(KeyInfo editableKey) {
        return editableKeyInfoMap.get(editableKey).value;
    }

    public List<KeyInfo> getPath(KeyInfo editableKey) {
        List<KeyInfo> path = new ArrayList<KeyInfo>();
        EditableKeyInfo info = editableKeyInfoMap.get(editableKey);
        if (info != null && info.path != null) {
            path.addAll(info.path);
        }

        return path;
    }

    /**
     * Keeps track of a modified value for the editable key
     * 
     * @param editableKey
     * @param value
     */
    public void modify(KeyInfo editableKey, Serializable value) {
        EditableKeyInfo editableKeyInfo = editableKeyInfoMap.get(editableKey);
        editableKeyInfo.previousValue = editableKeyInfo.value;
        editableKeyInfo.value = value;
        editableKeyInfo.modified = true;
    }

    public Serializable getModifiedValue(KeyInfo editableKey, boolean saving) {
        EditableKeyInfo editableKeyInfo = editableKeyInfoMap.get(editableKey);
        if (editableKeyInfo.modified) {
            if (saving) {
                editableKeyInfo.previousValue = editableKeyInfo.value;
                editableKeyInfo.modified = false;
            }
            return editableKeyInfo.value;
        }
        return null;
    }

    /**
     * If the editableKey was modified, the previous value will be returned and
     * the modified flag will be set to false.
     * 
     * @param editableKey
     * @return
     */
    public Serializable revertValue(KeyInfo editableKey) {
        EditableKeyInfo editableKeyInfo = editableKeyInfoMap.get(editableKey);
        if (editableKeyInfo.modified) {
            editableKeyInfo.modified = false;
            editableKeyInfo.value = editableKeyInfo.previousValue;
            editableKeyInfo.previousValue = null;
            return editableKeyInfo.value;
        }
        return null;
    }

    public void clearModifiedValues() {
        for (EditableKeyInfo editableKeyInfo : editableKeyInfoMap.values()) {
            editableKeyInfo.modified = false;
        }
    }

    /**
     * Determines the path of the editable keys. The path are key values of
     * dictionaries/maps within dictionaries/maps
     * 
     * @param parentPath
     * @param data
     * @param isParentEditable
     */
    private void determineEditableKeyPaths(List<KeyInfo> parentPath,
            Map<KeyInfo, Serializable> data, boolean isParentEditable) {

        if (data != null) {
            for (Entry<KeyInfo, Serializable> entry : data.entrySet()) {
                KeyInfo key = entry.getKey();
                boolean isEditable = key.isEditable() || isParentEditable;
                boolean isDisplayable = key.isDisplayable();

                if (isDisplayable) {
                    EditableKeyInfo info = new EditableKeyInfo();
                    info.isDisplayable = true;
                    info.value = entry.getValue();
                    editableKeyInfoMap.put(key, info);
                    continue;
                }
                if (entry.getValue() instanceof Map<?, ?>) {
                    Map<KeyInfo, Serializable> subdata = (Map<KeyInfo, Serializable>) entry
                            .getValue();
                    List<KeyInfo> path = new ArrayList<KeyInfo>();
                    if (parentPath != null) {
                        path.addAll(parentPath);
                    }
                    path.add(entry.getKey());
                    determineEditableKeyPaths(path, subdata, isEditable);
                } else if (entry.getValue() instanceof ArrayList) {
                    ArrayList<?> list = (ArrayList<?>) entry.getValue();
                    if (list != null && !list.isEmpty()) {
                        Object firstItem = list.get(0);
                        if (firstItem instanceof Map<?, ?>) {
                            List<KeyInfo> path = new ArrayList<KeyInfo>();
                            if (parentPath != null) {
                                path.addAll(parentPath);
                            }
                            path.add(entry.getKey());
                            for (Object item : list) {
                                determineEditableKeyPaths(path,
                                        (Map<KeyInfo, Serializable>) item,
                                        isEditable);
                            }
                        } else if (isEditable) {
                            EditableKeyInfo info = new EditableKeyInfo();
                            info.path = parentPath;
                            info.value = entry.getValue();
                            editableKeyInfoMap.put(entry.getKey(), info);
                        }
                    }
                } else if (isEditable) {
                    EditableKeyInfo info = new EditableKeyInfo();
                    info.path = parentPath;
                    info.value = entry.getValue();
                    editableKeyInfoMap.put(entry.getKey(), info);
                }
            }
        }
    }
}

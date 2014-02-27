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

import com.raytheon.uf.viz.productgen.dialog.ProductGenerationDialogUtility;

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
    private Map<String, Serializable> data;

    private class EditableKeyInfo {

        List<String> path;

        boolean modified = false;

        Serializable value;
    }

    private Map<String, EditableKeyInfo> editableKeyInfoMap = new HashMap<String, EditableKeyInfo>();

    public AbstractProductGeneratorData(Map<String, Serializable> data) {
        this.data = data;
        determineEditableKeyPaths(null, data, false);
    }

    abstract public String getDescriptionName();

    abstract public String getSegmentID();

    /**
     * Returns the set of editable keys of the data dictionary
     * 
     * @return
     */
    public Set<String> getEditableKeys() {
        return editableKeyInfoMap.keySet();
    }

    public Map<String, Serializable> getData() {
        return data;
    }

    /**
     * Returns the initial value for the editableKey.
     * 
     * @param editableKey
     * @return
     */
    public Serializable getValue(String editableKey) {
        return editableKeyInfoMap.get(editableKey).value;
    }

    public List<String> getPath(String editableKey) {
        List<String> path = new ArrayList<String>();
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
    public void modify(String editableKey, Serializable value) {
        editableKeyInfoMap.get(editableKey).value = value;
        editableKeyInfoMap.get(editableKey).modified = true;
    }

    public Serializable getModifiedValue(String editableKey) {
        EditableKeyInfo editableKeyInfo = editableKeyInfoMap.get(editableKey);
        if (editableKeyInfo.modified) {
            return editableKeyInfo.value;
        }
        return null;
    }

    /**
     * Determines the path of the editable keys. The path are key values of
     * dictionaries/maps within dictionaries/maps
     * 
     * @param parentPath
     * @param data
     * @param isParentEditable
     */
    private void determineEditableKeyPaths(List<String> parentPath,
            Map<String, Serializable> data, boolean isParentEditable) {

        if (data != null) {
            for (Entry<String, Serializable> entry : data.entrySet()) {
                boolean isEditable = entry.getKey().contains(
                        ProductGenerationDialogUtility.EDITABLE)
                        || isParentEditable;
                String key = entry.getKey().replace(
                        ProductGenerationDialogUtility.EDITABLE, "");
                if (entry.getValue() instanceof Map<?, ?>) {
                    Map<String, Serializable> subdata = (Map<String, Serializable>) entry
                            .getValue();
                    List<String> path = new ArrayList<String>();
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
                            List<String> path = new ArrayList<String>();
                            if (parentPath != null) {
                                path.addAll(parentPath);
                            }
                            path.add(entry.getKey());
                            for (Object item : list) {
                                determineEditableKeyPaths(path,
                                        (Map<String, Serializable>) item,
                                        isEditable);
                            }
                        } else if (isEditable) {
                            EditableKeyInfo info = new EditableKeyInfo();
                            info.path = parentPath;
                            info.value = entry.getValue();
                            editableKeyInfoMap.put(key, info);
                        }
                    }
                } else if (isEditable) {
                    EditableKeyInfo info = new EditableKeyInfo();
                    info.path = parentPath;
                    info.value = entry.getValue();
                    editableKeyInfoMap.put(key, info);
                }
            }
        }
    }

    abstract public void highlight(StyledText styledText);
}

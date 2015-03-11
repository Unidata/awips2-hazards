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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.raytheon.uf.common.hazards.productgen.IGeneratedProduct;
import com.raytheon.uf.common.hazards.productgen.KeyInfo;
import com.raytheon.uf.common.util.Pair;

/**
 * Container holding the editable key information associated with a generated
 * product
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * 01/15/2015   5109       bphillip     Initial creation
 * 03/11/2015   6889       bphillip     Modifications to allow more than one undo action in the Product Editor
 * 
 * </pre>
 * 
 * @author bphillip
 * @version 1.0
 */
class EditableKeys {

    /** Map of editable items for this product */
    private final Map<KeyInfo, EditableKeyInfo> editableKeyInfoMap = new LinkedHashMap<>();

    /**
     * Creates a new EditableKeys object from a generated product
     * 
     * @param product
     *            The product to create the Editable Keys from
     */
    public EditableKeys(IGeneratedProduct product) {
        determineEditableKeyPaths(null, product.getData(), 0);
    }

    /**
     * Determines if any editable value has been modified
     * 
     * @return True if any value has been modified, else false
     */
    public boolean isModified() {
        for (EditableKeyInfo value : getEditableKeyInfos()) {
            if (value.isModified()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines the path of the editable keys. The path are key values of
     * dictionaries/maps within dictionaries/maps
     * 
     * @param parentPath
     *            The parent path
     * @param data
     *            The data map from the generated product
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
                    info.setPath(parentPath);
                    info.setValue(entry.getValue());
                    info.setOriginalValue(entry.getValue());
                    info.setDisplayable(isDisplayable);
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

    /**
     * Creates the path to a key in the data map
     * 
     * @param parentPath
     *            The path to the key's parent
     * @param latestKey
     *            The key to create the path for
     * @return List of paths to this key
     */
    private List<KeyInfo> createPath(List<KeyInfo> parentPath, KeyInfo latestKey) {
        List<KeyInfo> path = new ArrayList<>();
        if (parentPath != null) {
            path.addAll(parentPath);
        }
        path.add(latestKey);
        return path;
    }

    /**
     * Determines the editable key paths in the data from the generated product
     * 
     * @param parentPath
     *            The parent path
     * @param list
     *            The list of paths to this key
     */
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

    /**
     * Gets the list of keys that are displayable
     * 
     * @return The list of displayable keys
     */
    public List<KeyInfo> getDisplayableKeys() {
        List<KeyInfo> displayableKeys = new ArrayList<>();
        for (Entry<KeyInfo, EditableKeyInfo> entry : editableKeyInfoMap
                .entrySet()) {
            if (entry.getValue().isDisplayable()) {
                displayableKeys.add(entry.getKey());
            }
        }
        return displayableKeys;
    }

    /**
     * Gets the modified value of the specified editable key
     * 
     * @param editableKey
     *            The editable key to get the modified value for
     * @param saving
     *            True if this method is being called during a save operation
     * @return The modified value of the editable key, or null if the key has
     *         not been modified
     */
    public Serializable getModifiedValue(KeyInfo editableKey, boolean saving) {
        EditableKeyInfo editableKeyInfo = editableKeyInfoMap.get(editableKey);
        if (editableKeyInfo.isModified()) {
            if (saving) {
                editableKeyInfo.setOriginalValue(editableKeyInfo.getValue());
                editableKeyInfo.setModified(false);
            }
            return editableKeyInfo.getValue();
        }
        return null;
    }

    /**
     * Checks if the editable key map is empty
     * 
     * @return True if the editable key map is empty, else false
     */
    public boolean isEmpty() {
        return this.editableKeyInfoMap.isEmpty();
    }

    /**
     * Clears the modified flag for all editable keys
     */
    public void clearModifiedValues() {
        for (EditableKeyInfo keyInfo : editableKeyInfoMap.values()) {
            keyInfo.setModified(false);
        }
    }

    /**
     * Gets the list of keyInfos currently managed by this object
     * 
     * @return The list of keyInfos currently managed by this object
     */
    public Set<KeyInfo> getKeyInfos() {
        return editableKeyInfoMap.keySet();
    }

    /**
     * Gets the list of editable key info objects currently managed by this
     * object
     * 
     * @return The list of editable key info objects currently managed by this
     *         object
     */
    public Collection<EditableKeyInfo> getEditableKeyInfos() {
        return editableKeyInfoMap.values();
    }

    /**
     * Gets the entries from the editable key map.
     * 
     * @return The entries from the editable key map.
     */
    public Set<Entry<KeyInfo, EditableKeyInfo>> getEditableKeyEntries() {
        return editableKeyInfoMap.entrySet();
    }

    /**
     * Gets the editable key info associated with the KeyInfo key
     * 
     * @param key
     *            The keyInfo to get the editable key info for
     * @return The editable keyinfo object associated with the given KeyInfo
     */
    public EditableKeyInfo getEditableKeyInfo(KeyInfo key) {
        return editableKeyInfoMap.get(key);
    }

    /**
     * Returns the map entry for the provided KeyInfo
     * 
     * @param key The KeyInfo to retrieve the entry for
     * @return The KeyInfo, EditableKeyInfo map entry have the provided KeyInfo object as its key
     */
    public Pair<KeyInfo, EditableKeyInfo> getEntry(KeyInfo key) {
        return new Pair<KeyInfo, EditableKeyInfo>(key,
                editableKeyInfoMap.get(key));
    }

    /**
     * Gets the path object for the editable object associated with the given
     * KeyInfo
     * 
     * @param key
     *            The lookup key
     * @return The path object associated with the editable key info keyed by
     *         the given KeyInfo. Returns null if key is null
     */
    public List<KeyInfo> getPath(KeyInfo key) {
        EditableKeyInfo keyInfo = getEditableKeyInfo(key);
        return keyInfo == null ? null : keyInfo.getPath();
    }

    /**
     * Gets if the editable keyInfo associated with the given keyinfo is
     * modified
     * 
     * @param key
     *            The lookup key
     * @return True if the editable key info associated with the given key is
     *         modified. Returns false if key is null.
     */
    public boolean isModified(KeyInfo key) {
        EditableKeyInfo keyInfo = getEditableKeyInfo(key);
        return keyInfo == null ? false : keyInfo.isModified();
    }

    /**
     * Gets if this editable keyInfo associated with the given keyInfo is
     * displayable
     * 
     * @param key
     *            The lookup key
     * @return True if the editable key info associated with the given key is
     *         displayable.  Returns False if key is null.
     */
    public boolean isDisplayable(KeyInfo key) {
        EditableKeyInfo keyInfo = getEditableKeyInfo(key);
        return keyInfo == null ? false : keyInfo.isDisplayable();
    }

    /**
     * Gets the value of the editable keyinfo associated with the given keyinfo
     * 
     * @param key
     *            The lookup key
     * @return The value of the editable keyinfo associated with the given
     *         keyinfo.  Returns null if key is null.
     */
    public Serializable getValue(KeyInfo key) {
        EditableKeyInfo keyInfo = getEditableKeyInfo(key);
        return keyInfo == null ? null : keyInfo.getValue();
    }

    /**
     * Gets the original value of the editable keyinfo associated with the given
     * keyinfo
     * 
     * @param key
     *            The lookup key
     * @return The original value of the editable keyinfo associated with the
     *         given keyinfo. Returns null if key is null.
     */
    public Serializable getOriginalValue(KeyInfo key) {
        EditableKeyInfo keyInfo = getEditableKeyInfo(key);
        return keyInfo == null ? null : keyInfo.getOriginalValue();
    }

    /**
     * Gets the previous value of the editable keyinfo associated with the given
     * keyinfo
     * 
     * @param key
     *            The lookup key
     * @return The previous value of the editable keyinfo associated with the
     *         given keyinfo. Returns null if key is null.
     */
    public Serializable getPreviousValue(KeyInfo key) {
        EditableKeyInfo keyInfo = getEditableKeyInfo(key);
        return keyInfo == null ? null : keyInfo.getPreviousValue();
    }
}

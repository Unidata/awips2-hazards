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
package com.raytheon.uf.common.hazards.productgen;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 
 * Generated product created by the ProductGenerator.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 19, 2012            jsanchez     Initial creation
 * Aug 20, 2013 1360       blawrenc     Added event set.
 * Sep 19, 2013 2046       mnash        Update for product generation.
 * Nov  5, 2013 2266       jsanchez     Created getter/setters for eventSet. Added editableEntries.
 * Feb 18, 2013 2702       jsanchez     Used Serializable.
 * 1/15/2015    5109       bphillip     Changed type on editableEntries field
 * 
 * </pre>
 * 
 * @author jsanchez
 * @version 1.0
 */
public class GeneratedProduct implements IGeneratedProduct, ITextProduct {

    private final String productID;

    /** Resulting products generated */
    private Map<String, List<Serializable>> entries = new LinkedHashMap<String, List<Serializable>>();

    /** Map of editable entries - mainly used for GUI highlighting */
    private Map<String, LinkedHashMap<String, Serializable>> editableEntries = new LinkedHashMap<String, LinkedHashMap<String, Serializable>>();

    private LinkedHashMap<KeyInfo, Serializable> data;

    /** Errors thrown executing python product classes */
    private String errors;

    public GeneratedProduct(String productID) {
        this.productID = productID;
    }

    public GeneratedProduct(IGeneratedProduct generatedProduct) {
        this.productID = generatedProduct.getProductID();
        this.entries = generatedProduct.getEntries();
        this.editableEntries = generatedProduct.getEditableEntries();
        this.data = deepCopyHashMap(generatedProduct.getData());
    }

    private LinkedHashMap<KeyInfo, Serializable> deepCopyHashMap(
            LinkedHashMap<KeyInfo, Serializable> map) {
        LinkedHashMap<KeyInfo, Serializable> data = new LinkedHashMap<KeyInfo, Serializable>();
        for (Entry<KeyInfo, Serializable> entry : map.entrySet()) {
            KeyInfo key = entry.getKey();
            Serializable value = entry.getValue();
            if (value instanceof Map) {
                data.put(
                        key,
                        deepCopyHashMap((LinkedHashMap<KeyInfo, Serializable>) value));
            } else if (value instanceof List) {
                data.put(key,
                        deepCopyArrayList((ArrayList<Serializable>) value));
            } else {
                data.put(key, value);
            }
        }

        return data;
    }

    private ArrayList<Serializable> deepCopyArrayList(
            ArrayList<Serializable> list) {
        ArrayList<Serializable> data = new ArrayList<Serializable>();
        for (Serializable item : list) {
            if (item instanceof Map) {
                data.add(deepCopyHashMap((LinkedHashMap<KeyInfo, Serializable>) item));
            } else if (item instanceof List) {
                data.add(deepCopyArrayList((ArrayList<Serializable>) item));
            } else {
                data.add(item);
            }
        }
        return data;
    }

    @Override
    public String getProductID() {
        return productID;
    }

    @Override
    public Map<String, List<Serializable>> getEntries() {
        return entries;
    }

    @Override
    public void setEntries(Map<String, List<Serializable>> entries) {
        this.entries = entries;
    }

    public void addEntry(String key, List<Serializable> entry) {
        this.entries.put(key, entry);
    }

    public LinkedHashMap<KeyInfo, Serializable> getData() {
        return data;
    }

    public void setData(LinkedHashMap<KeyInfo, Serializable> data) {
        this.data = data;
    }

    @Override
    public String getErrors() {
        return errors;
    }

    public void setErrors(String errors) {
        this.errors = errors;
    }

    @Override
    public List<Serializable> getEntry(String format) {
        if (entries != null) {
            return entries.get(format);
        }

        return null;
    }

    public Map<String, LinkedHashMap<String, Serializable>> getEditableEntries() {
        return editableEntries;
    }

    public void setEditableEntries(
            Map<String, LinkedHashMap<String, Serializable>> editableEntries) {
        this.editableEntries = editableEntries;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.common.hazards.productgen.ITextProduct#getText(java.lang
     * .String)
     */
    @Override
    public String getText(String key) {
        StringBuilder builder = new StringBuilder();
        for (Serializable entry : entries.get(key)) {
            builder.append(entry.toString());
        }
        return builder.toString();
    }
}

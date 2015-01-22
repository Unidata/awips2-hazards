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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * Class used by the Product Editor for managing and manipulating data editors.
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
class DataEditorManager {

    /**
     * Map of data editors. The keys are the product names and the values are
     * containers holding the editors associated with that product
     */
    private Map<String, ProductEditorContainer> dataEditorMap = new HashMap<String, ProductEditorContainer>();

    /**
     * Creates a new DataEditorManager.
     */
    protected DataEditorManager() {

    }

    /**
     * Adds a product data editor
     * 
     * @param dataEditor
     *            The product data editor to add
     */
    protected void addProductDataEditor(String productID,
            ProductDataEditor dataEditor) {
        dataEditor.initialize();
        getProductEditorContainer(productID).dataEditor = dataEditor;
    }

    /**
     * Adds a text editor for the product specified
     * 
     * @param productID
     *            The product associated with this text editor
     * @param formatEditor
     *            The editor to add
     */
    protected void addFormattedTextEditor(String productID,
            FormattedTextDataEditor formatEditor) {
        formatEditor.initialize();
        getProductEditorContainer(productID).textEditorMap.put(
                formatEditor.getText(), formatEditor);
    }

    /**
     * Gets all DataEditor instances currently in use by the ProductEditor
     * 
     * @return List of DataEditors currently in use by the ProductEditor
     */
    protected List<AbstractDataEditor> getAllEditors() {
        List<AbstractDataEditor> dataEditors = new ArrayList<AbstractDataEditor>();
        for (String productID : dataEditorMap.keySet()) {
            dataEditors.addAll(getAllEditors(productID));
        }
        return dataEditors;
    }

    /**
     * Gets the data editors associated with a product ID
     * 
     * @param productID
     *            The product ID of the product for which to get the data
     *            editors
     * @return Array of data editors assiciated with the give product
     */
    protected List<AbstractDataEditor> getAllEditors(String productID) {
        return getProductEditorContainer(productID).getAllEditors();
    }

    /**
     * Gets the ProductDataEditor tab associated with the give product
     * 
     * @param productID
     *            The ID of the product for which to get the ProductDataEditor
     *            for
     * @return The ProductDataEditor for the specified product
     */
    protected ProductDataEditor getProductDataEditor(String productID) {
        return getProductEditorContainer(productID).dataEditor;
    }

    /**
     * Gets all formatted text editors associated with the given product ID
     * 
     * @param productID
     *            The product ID
     * @return All formatted text editors associated with the given product ID
     */
    protected List<FormattedTextDataEditor> getFormattedTextDataEditors(
            String productID) {
        return new ArrayList<FormattedTextDataEditor>(
                getProductEditorContainer(productID).textEditorMap.values());
    }

    /**
     * Updates the formatted data editors when a product update occurs
     * 
     * @param productId
     *            The product ID of the product to update
     */
    protected void updateFormattedTextDataEditors(String productId) {
        List<FormattedTextDataEditor> dataEditors = getFormattedTextDataEditors(productId);
        for (FormattedTextDataEditor dataEditor : dataEditors) {
            dataEditor.refresh();
        }
    }

    /**
     * Private utility method to get (initialize if necessary) the container
     * holding the editors for the given product ID
     * 
     * @param productID
     *            The product ID
     * @return Container holding the editors for a given product ID
     */
    private ProductEditorContainer getProductEditorContainer(String productID) {
        ProductEditorContainer container = dataEditorMap.get(productID);
        if (container == null) {
            container = new ProductEditorContainer();
            dataEditorMap.put(productID, container);
        }
        return container;
    }

    /**
     * Private container class used to hold the Data Editor and formatted data
     * editors for a product
     */
    class ProductEditorContainer {

        /** The data editor instance */
        protected ProductDataEditor dataEditor;

        /** Formatted text editors */
        protected Map<String, FormattedTextDataEditor> textEditorMap = new HashMap<String, FormattedTextDataEditor>();

        /**
         * Gets all editors in this container
         * 
         * @return List of all editors in this container
         */
        protected List<AbstractDataEditor> getAllEditors() {
            List<AbstractDataEditor> dataEditors = new ArrayList<AbstractDataEditor>();
            if (dataEditor != null) {
                dataEditors.add(dataEditor);
            }

            dataEditors.addAll(textEditorMap.values());
            return dataEditors;
        }
    }
}

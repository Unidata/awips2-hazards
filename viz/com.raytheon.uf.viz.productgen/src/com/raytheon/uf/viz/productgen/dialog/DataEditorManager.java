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

import com.raytheon.uf.common.dataplugin.events.EventSet;
import com.raytheon.uf.common.dataplugin.events.IEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.hazards.productgen.IGeneratedProduct;

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
 * 04/16/2015   7579       Robert.Blum  Updates for amended Product Editor.
 * 07/08/2015   9063       Benjamin.Phillippe Fixed product name collision in dataEditorMap 
 * 07/23/2015   9625       Robert.Blum  Adjusted productID collision issue for RVS.
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
    protected void addProductDataEditor(IGeneratedProduct product,
            ProductDataEditor dataEditor) {
        dataEditor.initialize();
        getProductEditorContainer(product).dataEditor = dataEditor;
    }

    /**
     * Adds a text viewer for the product specified
     * 
     * @param productID
     *            The product associated with this text editor
     * @param formatViewer
     *            The viewer to add
     */
    public void addFormattedTextViewer(IGeneratedProduct product,
            FormattedTextViewer formattedTextViewer) {
        formattedTextViewer.initialize();
        getProductEditorContainer(product).textViewerMap.put(
                formattedTextViewer.getText(), formattedTextViewer);
    }

    /**
     * Gets all DataEditor instances currently in use by the ProductEditor
     * 
     * @return List of DataEditors currently in use by the ProductEditor
     */
    protected List<AbstractDataEditor> getAllEditors() {
        List<AbstractDataEditor> dataEditors = new ArrayList<AbstractDataEditor>();
        for (ProductEditorContainer container : dataEditorMap.values()) {
            dataEditors.addAll(container.getAllEditors());
        }
        return dataEditors;
    }

    /**
     * Gets the ProductDataEditor tab associated with the give product
     * 
     * @param productID
     *            The ID of the product for which to get the ProductDataEditor
     *            for
     * @return The ProductDataEditor for the specified product
     */
    protected ProductDataEditor getProductDataEditor(IGeneratedProduct product) {
        return getProductEditorContainer(product).dataEditor;
    }

    /**
     * Gets all formatted text viewers associated with the given product ID
     * 
     * @param productID
     *            The product ID
     * @return All formatted text viewers associated with the given product ID
     */
    protected List<FormattedTextViewer> getFormattedTextViewers(
            IGeneratedProduct product) {
        return new ArrayList<FormattedTextViewer>(
                getProductEditorContainer(product).textViewerMap.values());
    }

    /**
     * Updates the formatted text viewers when a product update occurs
     * 
     * @param productId
     *            The product ID of the product to update
     */
    protected void updateFormattedTextViewers(IGeneratedProduct product) {
        List<FormattedTextViewer> dataViewers = getFormattedTextViewers(product);
        for (FormattedTextViewer dataViewer : dataViewers) {
            dataViewer.refresh();
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
    private ProductEditorContainer getProductEditorContainer(
            IGeneratedProduct product) {
        String key = getProductKey(product);
        ProductEditorContainer container = dataEditorMap.get(key);
        if (container == null) {
            container = new ProductEditorContainer();
            dataEditorMap.put(key, container);
        }
        return container;
    }

    /**
     * Private method to get a unique key from the product to prevent collisions
     * in the data editor map
     * 
     * @param product
     *            The product to get the key for
     * @return The product key
     */
    private String getProductKey(IGeneratedProduct product) {
        EventSet<IEvent> eventSet = product.getEventSet();
        if (eventSet.isEmpty() == false) {
            IHazardEvent event = (IHazardEvent) eventSet.iterator().next();
            return event.getEventID() + " " + event.getPhenomenon() + "."
                    + event.getSignificance();
        } else {
            /*
             * RVS workaround since the eventSet is empty. There will only be
             * one RVS in the product editor at a time so the productID
             * conflicts will not occur.
             */
            return product.getProductID();
        }

    }

    /**
     * Private container class used to hold the Data Editor and formatted data
     * editors for a product
     */
    class ProductEditorContainer {

        /** The data editor instance */
        protected ProductDataEditor dataEditor;

        /** Formatted text viewers */
        protected Map<String, FormattedTextViewer> textViewerMap = new HashMap<String, FormattedTextViewer>();
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

            dataEditors.addAll(textViewerMap.values());
            return dataEditors;
        }
    }
}

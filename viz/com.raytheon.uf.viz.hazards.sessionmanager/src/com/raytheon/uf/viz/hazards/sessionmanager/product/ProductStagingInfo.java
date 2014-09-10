/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package com.raytheon.uf.viz.hazards.sessionmanager.product;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.google.common.collect.Lists;

/**
 * Description: Information used to populate the Product Staging Dialog.
 * Designed to support multiple products although, currently, there are no cases
 * of more than one product.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer          Description
 * ------------ ---------- ----------------- --------------------------
 * Nov 15, 2013            daniel.s.schaffer Initial creation
 * Sep 09, 2014    4042    Chris.Golden      Moved to session manager where it
 *                                           belongs, and changed to use maps for
 *                                           megawidget specifiers instead of
 *                                           Field instances,
 * </pre>
 * 
 * @author daniel.s.schaffer@noaa.gov
 * @version 1.0
 */
public class ProductStagingInfo {

    private final List<Product> products = Lists.newArrayList();

    public boolean addProducts(Product... products) {
        return this.products.addAll(Lists.newArrayList(products));
    }

    public List<Product> getProducts() {
        return products;
    }

    public int numProducts() {
        return getProducts().size();
    }

    public static class Product {

        private final String productGenerator;

        private final List<Map<String, Object>> fields = new ArrayList<>();

        private List<String> selectedEventIDs = new ArrayList<>();

        private Map<String, Serializable> dialogSelections = new HashMap<>();

        public Product(String productGenerator) {
            this.productGenerator = productGenerator;
        }

        public boolean addField(Map<String, Object> field) {
            return this.fields.add(field);
        }

        public boolean addFields(List<Map<String, Object>> fields) {
            return this.fields.addAll(fields);
        }

        public boolean addSelectedEventIDs(String... eventIDs) {
            return this.selectedEventIDs.addAll(Lists.newArrayList(eventIDs));
        }

        public String getProductGenerator() {
            return productGenerator;
        }

        public List<Map<String, Object>> getFields() {
            return fields;
        }

        public List<String> getSelectedEventIDs() {
            return selectedEventIDs;
        }

        @Override
        public String toString() {
            return ToStringBuilder.reflectionToString(this);
        }

        public void setSelectedEventIDs(List<String> selectedEventIDs) {
            this.selectedEventIDs = selectedEventIDs;
        }

        public Map<String, Serializable> getDialogSelections() {
            return dialogSelections;
        }

        public void setDialogSelections(
                Map<String, Serializable> dialogSelections) {
            this.dialogSelections = dialogSelections;
        }

    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}

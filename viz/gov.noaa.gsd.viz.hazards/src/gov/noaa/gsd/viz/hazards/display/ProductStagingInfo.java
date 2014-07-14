/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.display;

import gov.noaa.gsd.common.utilities.JSONConverter;
import gov.noaa.gsd.viz.hazards.jsonutilities.Dict;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.google.common.collect.Lists;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Field;

/**
 * Description: Information used to populate the Product Staging Dialog.
 * Designed to support multiple products although, currently, there are no cases
 * of more than one product.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 15, 2013            daniel.s.schaffer@noaa.gov      Initial creation
 * 
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
        private final JSONConverter jsonConverter = new JSONConverter();

        private final String productGenerator;

        private final List<Field> fields = new ArrayList<>();

        private List<String> selectedEventIDs = new ArrayList<>();

        private Map<String, Serializable> dialogSelections = new HashMap<>();

        Product(String productGenerator) {
            this.productGenerator = productGenerator;
        }

        boolean addFields(Field... fields) {
            return this.fields.addAll(Lists.newArrayList(fields));
        }

        boolean addSelectedEventIDs(String... eventIDs) {
            return this.selectedEventIDs.addAll(Lists.newArrayList(eventIDs));
        }

        public String getProductGenerator() {
            return productGenerator;
        }

        public List<Field> getFields() {
            return fields;
        }

        public List<String> getSelectedEventIDs() {
            return selectedEventIDs;
        }

        public List<Dict> fieldsAsDicts() {
            List<Dict> result = Lists.newArrayList();
            for (Field field : fields) {
                Dict fieldAsDict = Dict
                        .getInstance(jsonConverter.toJson(field));
                result.add(fieldAsDict);
            }
            return result;
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

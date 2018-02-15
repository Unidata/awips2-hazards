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

import com.raytheon.uf.common.hazards.productgen.ProductPart;

/**
 * Product Part Manager for the Product Editor. Tracks current and last saved
 * values for each editable product part. As well as which ones have been
 * modified.
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 05, 2017 29996      Robert.Blum Initial creation
 *
 * </pre>
 *
 * @author Robert.Blum
 */

public class ProductPartsManager {

    private List<ProductPart> productParts;

    /** Denotes which ProductParts items have been modified */
    private List<ProductPart> modifiedList = new ArrayList<>();

    /** The current value associated with this Product Part */
    private Map<ProductPart, String> valuesMap = new HashMap<>();

    /** The previous value associated with this Product Part */
    private Map<ProductPart, String> prevValuesMap = new HashMap<>();

    /** The last value that was saved to the Product Text table */
    private Map<ProductPart, String> lastSavedValuesMap = new HashMap<>();

    public ProductPartsManager(List<ProductPart> productParts) {
        this.productParts = productParts;
        for (ProductPart part : this.productParts) {
            String value = part.getCurrentText();
            if (value == null) {
                value = part.getGeneratedText();
                part.setCurrentText(value);
            }
            valuesMap.put(part, value);
            prevValuesMap.put(part, value);
            lastSavedValuesMap.put(part, part.getCurrentText());
        }
    }

    /**
     * Updates the current value of this ProductPart info object
     * 
     * @param value
     *            The value to update.
     */
    public void updateValue(ProductPart part, String value) {
        valuesMap.put(part, value);
        part.setCurrentText(value);
        if (!modifiedList.contains(part)) {
            modifiedList.add(part);
        }
    }

    /**
     * Returns whether or not the specified Product Part is modified.
     * 
     * @return the modified
     */
    public boolean isModified(ProductPart part) {
        return modifiedList.contains(part);
    }

    /**
     * Clears the modified state for the specified Product Part.
     * 
     * @param modified
     *            the modified to set
     */
    public void clearModified(ProductPart part) {
        modifiedList.remove(part);
    }

    /**
     * Gets the current value of the specified Product Part.
     * 
     * @return the value
     */
    public String getValue(ProductPart part) {
        return valuesMap.get(part);
    }

    /**
     * Sets the current value of the specified Product Part.
     * 
     * @param value
     *            the value to set
     */
    public void setValue(ProductPart part, String value) {
        valuesMap.put(part, value);
    }

    /**
     * Gets the previous value of the specified Product Part.
     * 
     * @return the prevValue
     */
    public String getPrevValue(ProductPart part) {
        return prevValuesMap.get(part);
    }

    /**
     * Sets the previous value of the specified Product Part.
     * 
     * @param prevValue
     *            the prevValue to set
     */
    public void setPrevValue(ProductPart part, String prevValue) {
        prevValuesMap.put(part, prevValue);
    }

    /**
     * Gets the last saved value of the specified Product Part.
     * 
     * @return the lastSavedValue
     */
    public String getLastSavedValue(ProductPart part) {
        return lastSavedValuesMap.get(part);
    }

    /**
     * Sets the last saved value of the specified Product Part.
     * 
     * @param lastSavedValue
     *            the lastSavedValue to set
     */
    public void setLastSavedValue(ProductPart part, String lastSavedValue) {
        lastSavedValuesMap.put(part, lastSavedValue);
    }

    /**
     * Gets whether or not any of the Product Parts have unsaved changes.
     * 
     * @return
     */
    public boolean hasUnsavedChanges() {
        for (ProductPart part : this.productParts) {
            if (hasUnsavedChanges(part)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets whether or not the specified Product Part has unsaved changes.
     * 
     * @param productPart
     * @return
     */
    public boolean hasUnsavedChanges(ProductPart productPart) {
        if (productPart.isEditable()) {
            String savedValue = lastSavedValuesMap.get(productPart);
            String currentValue = valuesMap.get(productPart);
            if (currentValue != null) {
                if (!currentValue.equals(savedValue)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Updates the specified Product Part to handle the current value being
     * saved.
     * 
     * @param productPart
     */
    public void updateProductPartForSave(ProductPart productPart) {
        String currentValue = valuesMap.get(productPart);
        lastSavedValuesMap.put(productPart, currentValue);
        modifiedList.remove(productPart);
        productPart.setCurrentText(currentValue);
        productPart.setUsePreviousText(true);
        productPart.setPreviousText(currentValue);
    }

    /**
     * Returns whehter or not all the required Product Parts have been filled
     * out.
     * 
     * @return
     */
    public boolean requiredFieldsCompleted() {
        for (ProductPart productPart : productParts) {
            String value = valuesMap.get(productPart);
            if (productPart.isRequired()
                    && (value == null || value.trim().isEmpty())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Gets a List of Product Parts that are required but are currently not
     * populated.
     * 
     * @return
     */
    public List<ProductPart> getIncompleteRequiredFields() {
        List<ProductPart> incompleteFields = new ArrayList<>();
        for (ProductPart productPart : productParts) {
            String value = String.valueOf(productPart.getCurrentText());
            if (productPart.isRequired()
                    && (value == null || value.trim().isEmpty())) {
                incompleteFields.add(productPart);
            }
        }
        return incompleteFields;
    }

    /**
     * Gets the list of modified Product Parts.
     * 
     * @return
     */
    public List<ProductPart> getModifiedProductParts() {
        return modifiedList;
    }

    /**
     * Clears the list of modified Product Parts.
     */
    public void clearModifiedProductParts() {
        modifiedList.clear();
    }

    /**
     * Gets the list of Product Parts being managed by this Manager.
     * 
     * @return
     */
    public List<ProductPart> getProductParts() {
        return productParts;
    }

    /**
     * Returns whether or not the Product Parts that are being managed are
     * modified.
     * 
     * @return
     */
    public boolean hasModifiedParts() {
        return !modifiedList.isEmpty();
    }
}

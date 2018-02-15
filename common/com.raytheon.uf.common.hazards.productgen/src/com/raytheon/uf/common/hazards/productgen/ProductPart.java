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
import java.util.List;

import javax.persistence.Embeddable;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * A unique portion of a text product that can be configured to be editable.
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
@Embeddable
@DynamicSerialize
public class ProductPart implements Serializable {

    @DynamicSerializeElement
    private String name;

    @DynamicSerializeElement
    private boolean displayable;

    @DynamicSerializeElement
    private String label;

    @DynamicSerializeElement
    private boolean eventIDsInLabel;

    @DynamicSerializeElement
    private boolean editable;

    @DynamicSerializeElement
    private boolean required;

    @DynamicSerializeElement
    private int numLines;

    @DynamicSerializeElement
    private boolean segmentDivider;

    @DynamicSerializeElement
    private String formatMethod;

    @DynamicSerializeElement
    private List<List<ProductPart>> subParts;

    @DynamicSerializeElement
    private KeyInfo keyInfo;

    @DynamicSerializeElement
    private String generatedText;

    @DynamicSerializeElement
    private String previousText;

    @DynamicSerializeElement
    private String currentText;

    @DynamicSerializeElement
    private boolean usePreviousText;

    @DynamicSerializeElement
    private String displayLabel;

    public ProductPart() {

    }

    public ProductPart(ProductPart part) {
        this.name = part.getName();
        this.displayable = part.isDisplayable();
        this.label = part.getLabel();
        this.eventIDsInLabel = part.isEventIDsInLabel();
        this.editable = part.isEditable();
        this.required = part.isRequired();
        this.numLines = part.getNumLines();
        this.segmentDivider = part.isSegmentDivider();
        this.formatMethod = part.getFormatMethod();
        this.subParts = part.getSubParts();
        this.keyInfo = part.getKeyInfo();
        this.generatedText = part.getGeneratedText();
        this.previousText = part.getPreviousText();
        this.currentText = part.getCurrentText();
        this.usePreviousText = part.isUsePreviousText();
        this.displayLabel = part.getDisplayLabel();
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the displayable
     */
    public boolean isDisplayable() {
        return displayable;
    }

    /**
     * @param displayable
     *            the displayable to set
     */
    public void setDisplayable(boolean displayable) {
        this.displayable = displayable;
    }

    /**
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * @param label
     *            the label to set
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * @return the eventIDsInLabel
     */
    public boolean isEventIDsInLabel() {
        return eventIDsInLabel;
    }

    /**
     * @param eventIDsInLabel
     *            the eventIDsInLabel to set
     */
    public void setEventIDsInLabel(boolean eventIDsInLabel) {
        this.eventIDsInLabel = eventIDsInLabel;
    }

    /**
     * @return the editable
     */
    public boolean isEditable() {
        return editable;
    }

    /**
     * @param editable
     *            the editable to set
     */
    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    /**
     * @return the required
     */
    public boolean isRequired() {
        return required;
    }

    /**
     * @param required
     *            the required to set
     */
    public void setRequired(boolean required) {
        this.required = required;
    }

    /**
     * @return the numLines
     */
    public int getNumLines() {
        return numLines;
    }

    /**
     * @param numLines
     *            the numLines to set
     */
    public void setNumLines(int numLines) {
        this.numLines = numLines;
    }

    /**
     * @return the segmentDivider
     */
    public boolean isSegmentDivider() {
        return segmentDivider;
    }

    /**
     * @param segmentDivider
     *            the segmentDivider to set
     */
    public void setSegmentDivider(boolean segmentDivider) {
        this.segmentDivider = segmentDivider;
    }

    /**
     * @return the formatMethod
     */
    public String getFormatMethod() {
        return formatMethod;
    }

    /**
     * @param formatMethod
     *            the formatMethod to set
     */
    public void setFormatMethod(String formatMethod) {
        this.formatMethod = formatMethod;
    }

    /**
     * @return the subParts
     */
    public List<List<ProductPart>> getSubParts() {
        return subParts;
    }

    /**
     * @param subParts
     *            the subParts to set
     */
    public void setSubParts(List<List<ProductPart>> subParts) {
        this.subParts = subParts;
    }

    /**
     * @return the keyInfo
     */
    public KeyInfo getKeyInfo() {
        return keyInfo;
    }

    /**
     * @param keyInfo
     *            the keyInfo to set
     */
    public void setKeyInfo(KeyInfo keyInfo) {
        this.keyInfo = keyInfo;
    }

    /**
     * @return the generatedText
     */
    public String getGeneratedText() {
        return generatedText;
    }

    /**
     * @param generatedText
     *            the generatedText to set
     */
    public void setGeneratedText(String generatedText) {
        this.generatedText = generatedText;

    }

    /**
     * @return the previousText
     */
    public String getPreviousText() {
        return previousText;
    }

    /**
     * @param previousText
     *            the previousText to set
     */
    public void setPreviousText(String previousText) {
        this.previousText = previousText;
    }

    /**
     * @return the currentText
     */
    public String getCurrentText() {
        return currentText;
    }

    /**
     * @param currentText
     *            the currentText to set
     */
    public void setCurrentText(String currentText) {
        this.currentText = currentText;
    }

    /**
     * @return the usePreviousText
     */
    public boolean isUsePreviousText() {
        return usePreviousText;
    }

    /**
     * @param usePreviousText
     *            the usePreviousText to set
     */
    public void setUsePreviousText(boolean usePreviousText) {
        this.usePreviousText = usePreviousText;
    }

    /**
     * @return the displayLabel
     */
    public String getDisplayLabel() {
        if (displayLabel == null) {
            displayLabel = buildDisplayLabel();
        }
        return displayLabel;
    }

    /**
     * @param displayLabel
     *            the displayLabel to set
     */
    public void setDisplayLabel(String displayLabel) {
        this.displayLabel = displayLabel;
    }

    private String buildDisplayLabel() {
        // Add EventIDs to label
        StringBuilder sb = new StringBuilder();
        if (label != null) {
            sb.append(label);
            if (eventIDsInLabel) {
                if (keyInfo.getEventIDs().isEmpty() == false) {
                    boolean firstEvent = true;
                    for (String eventID : keyInfo.getEventIDs()) {
                        if (firstEvent) {
                            // 1st eventID added to label
                            sb.append(" - ");
                            firstEvent = false;
                        } else {
                            sb.append('/');
                        }
                        sb.append(eventID);
                    }
                }
            }
        }
        this.displayLabel = sb.toString();
        return displayLabel;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Name: ").append(name).append("\n");
        sb.append("Label: ").append(label).append("\n");
        sb.append("FormatMethod: ").append(formatMethod).append("\n");
        sb.append("subParts: ").append(subParts).append("\n");
        sb.append("PreviousText: ").append(previousText).append("\n");
        sb.append("GeneratedText: ").append(generatedText).append("\n");
        sb.append("CurrentText: ").append(currentText).append("\n");
        return sb.toString();
    }

    @Override
    public int hashCode() {
        /*
         * Don't use generatedText, currentText, and previousText in the
         * hashCode calculation because users of this class assume those do not
         * factor into equality.
         */
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((label == null) ? 0 : label.hashCode());
        result = prime * result
                + ((formatMethod == null) ? 0 : formatMethod.hashCode());
        result = prime * result
                + ((subParts == null) ? 0 : subParts.hashCode());
        result = prime * result + ((keyInfo == null) ? 0 : keyInfo.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        /*
         * Don't use generatedText, currentText, and previousText in the
         * hashCode calculation because users of this class assume those do not
         * factor into equality.
         */
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (obj instanceof ProductPart == false)
            return false;
        ProductPart other = (ProductPart) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (displayable != other.displayable)
            return false;
        if (label == null) {
            if (other.label != null)
                return false;
        } else if (!label.equals(other.label))
            return false;
        if (eventIDsInLabel != other.eventIDsInLabel)
            return false;
        if (editable != other.editable)
            return false;
        if (required != other.required)
            return false;
        if (numLines != other.numLines)
            return false;
        if (segmentDivider != other.segmentDivider)
            return false;
        if (formatMethod == null) {
            if (other.formatMethod != null)
                return false;
        } else if (!formatMethod.equals(other.formatMethod))
            return false;
        if (subParts == null) {
            if (other.subParts != null)
                return false;
        } else if (!subParts.equals(other.subParts))
            return false;
        if (keyInfo == null) {
            if (other.keyInfo != null)
                return false;
        } else if (!keyInfo.equals(other.keyInfo))
            return false;
        return true;
    }

    public static ProductPart createBasicPart(String generationID) {
        ProductPart part = new ProductPart();
        part.setName(generationID);
        part.setKeyInfo(KeyInfo.createBasicKeyInfo(generationID));
        return part;
    }

    public String getProductText() {
        if (currentText != null) {
            return currentText;
        } else if (usePreviousText) {
            return previousText;
        }
        return generatedText;
    }
}

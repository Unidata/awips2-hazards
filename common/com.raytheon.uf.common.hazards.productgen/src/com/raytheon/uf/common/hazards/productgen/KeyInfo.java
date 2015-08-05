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

import gov.noaa.gsd.common.utilities.collect.IParameterInfo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.persistence.Embeddable;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.raytheon.uf.common.serialization.ISerializableObject;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Basic information about the keys set in the python dictionary.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 1, 2014            jsanchez     Initial creation
 * Apr 10, 2014  2336     Chris.Golden Added implementation of IParameterInfo.
 * Apr 23, 2014  3519     jsanchez     Made this class serializable and added the required field.
 * Jul 28, 2015  9687     Robert.Blum  Added displayLabel field.
 * Aug 03, 2015  8836     Chris.Cody   Changes for a configurable Event Id
 * </pre>
 * 
 * @author jsanchez
 * @version 1.0
 */
@Embeddable
@DynamicSerialize
public class KeyInfo implements IParameterInfo, ISerializableObject,
        Serializable {
    @DynamicSerializeElement
    private String name;

    @DynamicSerializeElement
    private String productCategory;

    @DynamicSerializeElement
    private String productID;

    @DynamicSerializeElement
    private List<String> eventIDs;

    @DynamicSerializeElement
    private String segment;

    @DynamicSerializeElement
    private boolean editable;

    @DynamicSerializeElement
    private boolean displayable;

    @DynamicSerializeElement
    private String label;

    @DynamicSerializeElement
    private boolean required;

    @DynamicSerializeElement
    private boolean displayLabel;

    private int index;

    public KeyInfo() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProductCategory() {
        return productCategory;
    }

    public void setProductCategory(String productCategory) {
        this.productCategory = productCategory;
    }

    public String getProductID() {
        return productID;
    }

    public void setProductID(String productID) {
        this.productID = productID;
    }

    public List<String> getEventIDs() {
        return eventIDs;
    }

    public void setEventIDs(List<String> eventIDs) {
        this.eventIDs = eventIDs;
    }

    public String getSegment() {
        return segment;
    }

    public void setSegment(String segment) {
        this.segment = segment;
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public boolean isDisplayable() {
        return displayable;
    }

    public void setDisplayable(boolean displayable) {
        this.displayable = displayable;
    }

    @Override
    public String getKey() {
        return toString();
    }

    @Override
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public boolean getDisplayLabel() {
        return displayLabel;
    }

    public void setDisplayLabel(boolean displayLabel) {
        this.displayLabel = displayLabel;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((eventIDs == null) ? 0 : eventIDs.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result
                + ((productCategory == null) ? 0 : productCategory.hashCode());
        result = prime * result
                + ((productID == null) ? 0 : productID.hashCode());
        result = prime * result + ((segment == null) ? 0 : segment.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        KeyInfo other = (KeyInfo) obj;
        if (eventIDs == null) {
            if (other.eventIDs != null)
                return false;
        } else if (!eventIDs.equals(other.eventIDs))
            return false;
        if (index != other.index)
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (productCategory == null) {
            if (other.productCategory != null)
                return false;
        } else if (!productCategory.equals(other.productCategory))
            return false;
        if (productID == null) {
            if (other.productID != null)
                return false;
        } else if (!productID.equals(other.productID))
            return false;
        if (segment == null) {
            if (other.segment != null)
                return false;
        } else if (!segment.equals(other.segment))
            return false;
        return true;
    }

    public static KeyInfo createBasicKeyInfo(String name) {
        KeyInfo info = new KeyInfo();
        info.setName(name);
        info.setEventIDs(new ArrayList<String>());
        return info;
    }

    @Deprecated
    public static KeyInfo getElements(String label, Set<KeyInfo> keySet) {
        for (KeyInfo key : keySet) {
            if (key.getLabel().equals(label)) {
                return key;
            }
        }
        return null;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

}

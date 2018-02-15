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
package com.raytheon.uf.common.hazards.productgen.editable;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import com.raytheon.uf.common.serialization.ISerializableObject;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * 
 * The class that represents a unique row in the table. Can be multiple keys, so
 * this is embedding a {@link UserTextId} such that there can be multiple of
 * those.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 26, 2013            mnash     Initial creation
 * Apr 23, 2014 3519       jsanchez  Changed eventID to ArrayList
 * Aug 03, 2015 8836       Chris.Cody  Changes for a configurable Event Id
 * Sep 23, 2016 21734      mark.fegan Change segment type to "text"
 * Nov 04, 2016 22119      Kevin.Bisanz Add officeID.
 * Jun 12, 2017 35022      Kevin.Bisanz Remove productID, add mode, eventIDs is now eventID (String)
 * 
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */
@Embeddable
@DynamicSerialize
public class CustomTextId implements ISerializableObject, Serializable {
    private static final long serialVersionUID = -3199070926894096163L;

    @Column
    @DynamicSerializeElement
    private String key;

    @Column
    @DynamicSerializeElement
    private String productCategory;

    @Column
    @DynamicSerializeElement
    private String mode;

    @Column(columnDefinition = "text")
    @DynamicSerializeElement
    private String segment;

    @Column
    @DynamicSerializeElement
    private String eventID;

    @Column
    @DynamicSerializeElement
    private String officeID;

    /**
     * Default constructor for serialization
     */
    public CustomTextId() {
    }

    public CustomTextId(String key, String productCategory, String mode,
            String segment, String eventID, String officeID) {
        this.key = key;
        this.productCategory = productCategory;
        this.mode = mode;
        this.segment = segment;
        this.eventID = eventID;
        this.officeID = officeID;
    }

    /**
     * @return the key
     */
    public String getKey() {
        return key;
    }

    /**
     * @param key
     *            the key to set
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * @return the productCategory
     */
    public String getProductCategory() {
        return productCategory;
    }

    /**
     * @param productCategory
     *            the productCategory to set
     */
    public void setProductCategory(String productCategory) {
        this.productCategory = productCategory;
    }

    /**
     * @return the mode
     */
    public String getMode() {
        return mode;
    }

    /**
     * @param mode
     *            the mode to set
     */
    public void setMode(String mode) {
        this.mode = mode;
    }

    /**
     * @return the segment
     */
    public String getSegment() {
        return segment;
    }

    /**
     * @param segment
     *            the segment to set
     */
    public void setSegment(String segment) {
        this.segment = segment;
    }

    /**
     * @return the eventID
     */
    public String getEventID() {
        return eventID;
    }

    /**
     * @param eventID
     *            the eventID to set
     */
    public void setEventID(String eventID) {
        this.eventID = eventID;
    }

    /**
     * @return the officeID
     */
    public String getOfficeID() {
        return officeID;
    }

    /**
     * @param officeID
     *            the officeID to set
     */
    public void setOfficeID(String officeID) {
        this.officeID = officeID;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((eventID == null) ? 0 : eventID.hashCode());
        result = prime * result + ((key == null) ? 0 : key.hashCode());
        result = prime * result
                + ((productCategory == null) ? 0 : productCategory.hashCode());
        result = prime * result + ((mode == null) ? 0 : mode.hashCode());
        result = prime * result + ((segment == null) ? 0 : segment.hashCode());
        result = prime * result
                + ((officeID == null) ? 0 : officeID.hashCode());
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
        CustomTextId other = (CustomTextId) obj;
        if (eventID == null) {
            if (other.eventID != null)
                return false;
        } else if (!eventID.equals(other.eventID))
            return false;
        if (key == null) {
            if (other.key != null)
                return false;
        } else if (!key.equals(other.key))
            return false;
        if (productCategory == null) {
            if (other.productCategory != null)
                return false;
        } else if (!productCategory.equals(other.productCategory))
            return false;
        if (mode == null) {
            if (other.mode != null)
                return false;
        } else if (!mode.equals(other.mode))
            return false;
        if (segment == null) {
            if (other.segment != null)
                return false;
        } else if (!segment.equals(other.segment))
            return false;
        if (officeID == null) {
            if (other.officeID != null)
                return false;
        } else if (!officeID.equals(other.officeID))
            return false;
        return true;
    }
}

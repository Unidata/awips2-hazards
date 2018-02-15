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
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.raytheon.uf.common.dataplugin.persist.PersistableDataObject;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * The hibernate object for the storage of product text to retrieve during
 * product generation
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 2, 2013            mnash     Initial creation
 * Jun 18, 2014 3519      jsanchez  Made eventID an array list.
 * Aug 03, 2015 8836      Chris.Cody Changes for a configurable Event Id
 * Nov 04, 2016 22119     Kevin.Bisanz Changes to export product text by officeID
 * Apr 27, 2017 29776     Kevin.Bisanz Add insertTime
 * Jun 12, 2017  35022    Kevin.Bisanz Remove productID, add mode, change eventIDs from list to single eventID.
 * Jun 19, 2017  35022    Kevin.Bisanz Add toString().
 * 
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */

@Entity
@Table(name = "producttext")
@DynamicSerialize
public class ProductText extends PersistableDataObject<CustomTextId>
        implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @DynamicSerializeElement
    private CustomTextId id;

    @Column
    @DynamicSerializeElement
    private String value;

    @Column
    @DynamicSerializeElement
    private Date insertTime;

    /**
     * Intended for serialization only.
     */
    public ProductText() {
    }

    /**
     * Constructor to construct the necessary elements.
     */
    public ProductText(String key, String productCategory, String mode,
            String segment, String eventID, String officeID, Date insertTime,
            String value) {
        id = new CustomTextId(key, productCategory, mode, segment, eventID,
                officeID);
        this.insertTime = insertTime;
        this.value = value;
    }

    /**
     * @return the key
     */
    public String getKey() {
        return this.getId().getKey();
    }

    /**
     * @param key
     *            the key to set
     */
    public void setKey(String key) {
        this.getId().setKey(key);
    }

    public String getProductCategory() {
        return this.getId().getProductCategory();
    }

    public String getMode() {
        return this.getId().getMode();
    }

    public String getSegment() {
        return this.getId().getSegment();
    }

    public String getEventID() {
        return this.getId().getEventID();
    }

    public String getOfficeID() {
        return this.getId().getOfficeID();
    }

    /**
     * @return the id
     */
    public CustomTextId getId() {
        return id;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setId(CustomTextId id) {
        this.id = id;
    }

    /**
     * @return the insertTime
     */
    public Date getInsertTime() {
        return insertTime;
    }

    /**
     * @param insertTime
     *            the insertTime to set
     */
    public void setInsertTime(Date insertTime) {
        this.insertTime = insertTime;
    }

    /**
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * @param value
     *            the value to set
     */
    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getEventID()).append("/");
        sb.append(getKey()).append("/");
        sb.append(getSegment());
        return sb.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
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

        ProductText other = (ProductText) obj;

        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;

        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.value))
            return false;

        if (insertTime == null) {
            if (other.insertTime != null)
                return false;
        } else if (!insertTime.equals(other.insertTime))
            return false;

        return true;
    }
}

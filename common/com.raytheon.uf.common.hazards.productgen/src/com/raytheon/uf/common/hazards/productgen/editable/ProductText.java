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
import java.util.ArrayList;

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
    private Serializable value;

    /**
     * Intended for serialization only.
     */
    public ProductText() {
    }

    /**
     * Constructor to construct the necessary elements.
     */
    public ProductText(String key, String productCategory, String productID,
            String segment, ArrayList<String> eventIDs, String officeID,
            Serializable value) {
        id = new CustomTextId(key, productCategory, productID, segment,
                eventIDs, officeID);
        this.value = value;
    }

    /**
     * @return the key
     */
    public String getKey() {
        return id.key;
    }

    /**
     * @param key
     *            the key to set
     */
    public void setKey(String key) {
        this.id.key = key;
    }

    public String getProductCategory() {
        return this.getId().getProductCategory();
    }

    public String getProductID() {
        return this.getId().getProductID();
    }

    public String getSegment() {
        return this.getId().getSegment();
    }

    public ArrayList<String> getEventIDs() {
        return this.getId().getEventIDs();
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
     * @return the value
     */
    public Serializable getValue() {
        return value;
    }

    /**
     * @param value
     *            the value to set
     */
    public void setValue(Serializable value) {
        this.value = value;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
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
        return true;
    }
}

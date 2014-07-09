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
package com.raytheon.uf.common.hazards.productgen.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.raytheon.uf.common.dataplugin.persist.PersistableDataObject;
import com.raytheon.uf.common.hazards.productgen.KeyInfo;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * The hibernate object for the storage of product data to retrieve for
 * correction or review.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 11, 2014            jsanchez     Initial creation
 * 
 * </pre>
 * 
 * @author jsanchez
 * @version 1.0
 */

@Entity
@Table(name = "productdata")
@DynamicSerialize
public class ProductData extends PersistableDataObject<String> implements
        Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @DynamicSerializeElement
    private CustomDataId id;

    @Column
    @DynamicSerializeElement
    private Date startTime;

    @Column
    @DynamicSerializeElement
    private LinkedHashMap<KeyInfo, Serializable> data;

    public ProductData() {

    }

    public ProductData(String mode, String productGeneratorName,
            ArrayList<Integer> eventIDs, Date startTime,
            LinkedHashMap<KeyInfo, Serializable> data) {
        id = new CustomDataId(mode, productGeneratorName, eventIDs);
        this.startTime = startTime;
        this.data = data;
    }

    public CustomDataId getId() {
        return id;
    }

    public void setId(CustomDataId id) {
        this.id = id;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public ArrayList<Integer> getEventIDs() {
        return id.getEventIDs();
    }

    public String getMode() {
        return id.getMode();
    }

    public void setMode(String mode) {
        this.id.setMode(mode);
    }

    public String getProductGeneratorName() {
        return id.getProductGeneratorName();
    }

    public void setProductGeneratorName(String productGeneratorName) {
        this.id.setProductGeneratorName(productGeneratorName);
    }

    public void setEventIDs(ArrayList<Integer> eventIDs) {
        this.id.setEventIDs(eventIDs);
    }

    public LinkedHashMap<KeyInfo, Serializable> getData() {
        return data;
    }

    public void setData(LinkedHashMap<KeyInfo, Serializable> data) {
        this.data = data;
    }

}

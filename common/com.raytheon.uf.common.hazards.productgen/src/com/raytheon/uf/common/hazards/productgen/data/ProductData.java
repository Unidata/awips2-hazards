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
import java.util.HashMap;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.raytheon.uf.common.dataplugin.persist.PersistableDataObject;
import com.raytheon.uf.common.hazards.productgen.EditableEntryMap;
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
 * Mar 30, 2015    6929    Robert.Blum  Changed startTime to issueTime.
 * May 07, 2015    6979    Robert.Blum  Added editableEntries.
 * Aug 13, 2015    8836    Chris.Cody   Changes for a configurable Event Id
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
    private Date issueTime;

    @Column
    @DynamicSerializeElement
    private HashMap<String, Serializable> data;

    @Column
    @DynamicSerializeElement
    private ArrayList<EditableEntryMap> editableEntries;

    public ProductData() {

    }

    public ProductData(String mode, String productGeneratorName,
            ArrayList<String> eventIDs, Date issueTime,
            HashMap<String, Serializable> data,
            ArrayList<EditableEntryMap> editableEntries) {
        id = new CustomDataId(mode, productGeneratorName, eventIDs);
        this.issueTime = issueTime;
        this.data = data;
        this.editableEntries = editableEntries;
    }

    public CustomDataId getId() {
        return id;
    }

    public void setId(CustomDataId id) {
        this.id = id;
    }

    public Date getIssueTime() {
        return issueTime;
    }

    public void setissueTime(Date issueTime) {
        this.issueTime = issueTime;
    }

    public ArrayList<String> getEventIDs() {
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

    public void setEventIDs(ArrayList<String> eventIDs) {
        this.id.setEventIDs(eventIDs);
    }

    public HashMap<String, Serializable> getData() {
        return data;
    }

    public void setData(HashMap<String, Serializable> data) {
        this.data = data;
    }

    public ArrayList<EditableEntryMap> getEditableEntries() {
        return editableEntries;
    }

    public void setEditableEntries(ArrayList<EditableEntryMap> editableEntries) {
        this.editableEntries = editableEntries;
    }
}

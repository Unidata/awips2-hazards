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
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.raytheon.uf.common.dataplugin.persist.PersistableDataObject;
import com.raytheon.uf.common.hazards.productgen.ProductPart;
import com.raytheon.uf.common.hazards.productgen.ProductUtils;
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
 * Sep 11, 2015   10203    Robert.Blum  Added issueTime to the productdata primary key.
 * Apr 27, 2016   17742    Roger.Ferel  Added getter method for the new columns' values.
 * Jul 06, 2016   18257    Kevin.Bisanz Implemented toString()
 * Aug 09, 2016   17067    Robert.Blum  Changes to work with RVS products.
 * Aug 29, 2016   19223    Kevin.Bisanz Add comment regarding use of concrete
 *                                      classes with serialization.
 * Nov 04, 2016   22119    Kevin.Bisanz Changes to export product data by officeID
 * Feb 01, 2017   15556    Chris.Golden Added copy constructor.
 * Jun 05, 2017   29996    Robert.Blum  EditableEntries are now product parts.
 * Jun 06, 2018   15561    Chris.Golden Made typecasting to floats safer (in case the
 *                                      object is a Number but not a Float).
 * </pre>
 * 
 * @author jsanchez
 * @version 1.0
 */

@Entity
@Table(name = "productdata")
@DynamicSerialize
public class ProductData extends PersistableDataObject<CustomDataId>
        implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String[] EXPIRATION_TIME_KEYS = new String[] {
            "segments", "expirationTime" };

    private static final String[] HAZARD_TYPE_KEYS = new String[] { "segments",
            "sections", "vtecRecord", "key" };

    private static final String[] USER_NAME_KEYS = new String[] { "segments",
            "sections", "hazardEvents", "userName" };

    @Id
    @DynamicSerializeElement
    private CustomDataId id;

    /*
     * A Map does not implement Serializable, but a HashMap does. This
     * variable's data type is intentionally a HashMap instead of a Map so that
     * this can be persisted using Hibernate default behavior without the need
     * to implement a custom class which implements
     * "org.hibernate.usertype.UserType".
     */
    @Column
    @DynamicSerializeElement
    private HashMap<String, Serializable> data;

    /*
     * See comment above this.data for the reason this data type is an ArrayList
     * instead of a List.
     */
    @Column
    @DynamicSerializeElement
    private ArrayList<ProductPart> editableEntries;

    public ProductData() {

    }

    /**
     * Constructor.
     * 
     * @param mode
     * @param productGeneratorName
     * @param eventIDs
     * @param officeID
     * @param issueTime
     * @param data
     * @param editableEntries
     */
    public ProductData(String mode, String productGeneratorName,
            ArrayList<String> eventIDs, String officeID, Date issueTime,
            HashMap<String, Serializable> data,
            ArrayList<ProductPart> editableEntries) {
        id = new CustomDataId(mode, productGeneratorName, eventIDs, officeID,
                issueTime);
        this.data = data;
        this.editableEntries = editableEntries;
    }

    public ProductData(ProductData other) {
        this.id = new CustomDataId(other.getId());
        this.data = (other.data == null ? null : new HashMap<>(other.data));
        this.editableEntries = (other.editableEntries == null ? null
                : new ArrayList<ProductPart>(other.editableEntries.size()));
        if (this.editableEntries != null) {
            for (ProductPart editableEntry : other.editableEntries) {
                this.editableEntries.add(new ProductPart(editableEntry));
            }
        }
    }

    public CustomDataId getId() {
        return id;
    }

    public void setId(CustomDataId id) {
        this.id = id;
    }

    public Date getIssueTime() {
        return id.getIssueTime();
    }

    public void setissueTime(Date issueTime) {
        id.setIssueTime(issueTime);
    }

    public ArrayList<String> getEventIDs() {
        return id.getEventIDs();
    }

    public String getOfficeID() {
        return id.getOfficeID();
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

    public ArrayList<ProductPart> getEditableEntries() {
        return editableEntries;
    }

    public void setEditableEntries(ArrayList<ProductPart> editableEntries) {
        this.editableEntries = editableEntries;
    }

    public Long getExpirationTime() {
        return ((Number) ProductUtils.getDataElement(data,
                EXPIRATION_TIME_KEYS)).longValue();
    }

    public String getHazardType() {
        String type = (String) ProductUtils.getDataElement(data,
                HAZARD_TYPE_KEYS);
        return type == null ? "" : type;
    }

    /**
     * Generate VTEC column display string from the vtec records in the data.
     * 
     * @return vtecStr
     */
    public String getVtecStr() {
        StringBuilder sb = new StringBuilder();
        String prefix = "";
        List<?> segments = (List<?>) data.get("segments");
        if (segments != null && segments.isEmpty() == false) {
            sb.append("[");
            for (Object o : segments) {
                Map<?, ?> segment = (Map<?, ?>) o;
                List<?> sections = (List<?>) segment.get("sections");
                for (Object o2 : sections) {
                    Map<?, ?> section = (Map<?, ?>) o2;
                    Map<?, ?> vtecRecord = (Map<?, ?>) section
                            .get("vtecRecord");
                    sb.append(prefix).append((String) vtecRecord.get("act"));
                    prefix = ", ";
                }
            }
            sb.append("]");
        }
        return sb.toString();
    }

    /**
     * 
     * @return user name or empty string if user name not set.
     */
    public String getUserName() {
        Object value = ProductUtils.getDataElement(data, USER_NAME_KEYS);
        if (value instanceof String) {
            return (String) value;
        }
        return "";
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(getMode());
        sb.append(" ");
        sb.append(getEventIDs());
        sb.append(" ");
        sb.append(getVtecStr());
        sb.append(" ");
        sb.append(getHazardType());
        return sb.toString();
    }
}

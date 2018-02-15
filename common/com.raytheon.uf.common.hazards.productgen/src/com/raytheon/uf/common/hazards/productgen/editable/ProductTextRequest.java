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

import java.util.Date;
import java.util.List;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.serialization.comm.IServerRequest;

/**
 * Serialized request for getting product text from the database.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 26, 2013            mnash     Initial creation
 * Nov 10, 2016 22119      Kevin.Bisanz Add path attribute.
 * Jun 12, 2017 35022      Kevin.Bisanz Change to contain attributes of a ProductText, instead of actual ProductText object.
 * 
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */

@DynamicSerialize
public class ProductTextRequest implements IServerRequest {

    @DynamicSerializeElement
    private ProductRequestType type;

    @DynamicSerializeElement
    private List<String> eventIDs;

    @DynamicSerializeElement
    private String key;

    @DynamicSerializeElement
    private String officeID;

    @DynamicSerializeElement
    private String productCategory;

    @DynamicSerializeElement
    private String mode;

    @DynamicSerializeElement
    private String segment;

    @DynamicSerializeElement
    private String value;

    @DynamicSerializeElement
    private Date insertTime;

    /**
     * Path to export to or import from.
     */
    @DynamicSerializeElement
    private String path;

    public static enum ProductRequestType {
        CREATE,
        DELETE,
        UPDATE,
        RETRIEVE,
        SAVE_OR_UPDATE,
        EXPORT,
        IMPORT;
    }

    /**
     * Constructor required for serialization.
     */
    public ProductTextRequest() {
    }

    public ProductTextRequest(ProductRequestType type, List<String> eventIDs,
            String key, String officeID, String productCategory, String mode,
            String segment, String value, Date insertTime, String path) {
        this.type = type;
        this.eventIDs = eventIDs;
        this.key = key;
        this.officeID = officeID;
        this.productCategory = productCategory;
        this.mode = mode;
        this.segment = segment;
        this.value = value;
        this.insertTime = insertTime;
        this.path = path;
    }

    /**
     * @return the type
     */
    public ProductRequestType getType() {
        return type;
    }

    /**
     * @param type
     *            the type to set
     */
    public void setType(ProductRequestType type) {
        this.type = type;
    }

    /**
     * * @return the path for export or import
     */
    public String getPath() {
        return path;
    }

    /**
     * @param path
     *            the path to set for export or import
     */
    public void setPath(String path) {
        this.path = path;
    }

    public List<String> getEventIDs() {
        return eventIDs;
    }

    public void setEventIDs(List<String> eventIDs) {
        this.eventIDs = eventIDs;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getOfficeID() {
        return officeID;
    }

    public void setOfficeID(String officeID) {
        this.officeID = officeID;
    }

    public String getProductCategory() {
        return productCategory;
    }

    public void setProductCategory(String productCategory) {
        this.productCategory = productCategory;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getSegment() {
        return segment;
    }

    public void setSegment(String segment) {
        this.segment = segment;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Date getInsertTime() {
        return insertTime;
    }

    public void setInsertTime(Date insertTime) {
        this.insertTime = insertTime;
    }
}

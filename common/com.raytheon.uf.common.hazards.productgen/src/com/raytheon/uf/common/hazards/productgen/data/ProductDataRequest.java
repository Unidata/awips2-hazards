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

import java.util.Date;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.serialization.comm.IServerRequest;

/**
 * Serialized request for getting product data from the database.
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

@DynamicSerialize
public class ProductDataRequest implements IServerRequest {

    @DynamicSerializeElement
    private ProductData productData;

    @DynamicSerializeElement
    private ProductRequestType type;

    @DynamicSerializeElement
    private Date currentTime;

    public static enum ProductRequestType {
        CREATE, DELETE, UPDATE, RETRIEVE, SAVE_OR_UPDATE, RETRIEVE_CORRECTABLE;
    }

    /**
     * 
     */
    public ProductDataRequest() {
    }

    /**
     * 
     */
    public ProductDataRequest(ProductData data, ProductRequestType type,
            Date currentTime) {
        this.productData = data;
        this.type = type;
        this.currentTime = currentTime;
    }

    /**
     * @return the productData
     */
    public ProductData getProductData() {
        return productData;
    }

    /**
     * @param productData
     *            the productData to set
     */
    public void setProductData(ProductData productData) {
        this.productData = productData;
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

    public Date getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(Date currentTime) {
        this.currentTime = currentTime;
    }

}

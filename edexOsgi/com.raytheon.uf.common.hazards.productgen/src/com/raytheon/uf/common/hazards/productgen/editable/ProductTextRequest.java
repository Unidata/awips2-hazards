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
 * 
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */

@DynamicSerialize
public class ProductTextRequest implements IServerRequest {

    @DynamicSerializeElement
    private ProductText productText;

    @DynamicSerializeElement
    private ProductRequestType type;

    public static enum ProductRequestType {
        CREATE, DELETE, UPDATE, RETRIEVE, SAVE_OR_UPDATE;
    }

    /**
     * 
     */
    public ProductTextRequest() {
    }

    /**
     * 
     */
    public ProductTextRequest(ProductText text, ProductRequestType type) {
        this.productText = text;
        this.type = type;
    }

    /**
     * @return the productText
     */
    public ProductText getProductText() {
        return productText;
    }

    /**
     * @param productText
     *            the productText to set
     */
    public void setProductText(ProductText productText) {
        this.productText = productText;
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
}

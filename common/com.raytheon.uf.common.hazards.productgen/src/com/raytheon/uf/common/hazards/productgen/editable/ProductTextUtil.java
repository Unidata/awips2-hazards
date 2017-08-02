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
import java.util.List;

import com.raytheon.uf.common.hazards.productgen.editable.ProductTextRequest.ProductRequestType;
import com.raytheon.uf.common.serialization.comm.RequestRouter;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

/**
 * Utility class for getting and storing product text for product generation.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 26, 2013            mnash     Initial creation
 * Apr  7, 2014 2917       jsanchez  Changed the methods to accept eventIDs as a List<Integer>
 * Aug 03, 2015 8836       Chris.Cody Changes for a configurable Event Id
 * Nov 10, 2016 22119      Kevin.Bisanz Changes to export/import product text by officeID.
 * 
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */

public class ProductTextUtil {

    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(ProductTextUtil.class);

    /**
     * Sends a request to EDEX to store the text based on the key in the map,
     * the product category, the product id, the segment, the event id, and the
     * office id.
     * 
     * @param key
     * @param productCategory
     * @param productID
     * @param segment
     * @param eventIDs
     * @param officeID
     * @param value
     */
    public static ProductTextResponse createProductText(String key,
            String productCategory, String productID, String segment,
            ArrayList<String> eventIDs, String officeID, Serializable value) {
        ProductTextResponse response = sendRequest(key, productCategory,
                productID, segment, eventIDs, officeID, value,
                ProductRequestType.CREATE);
        if (response.getExceptions() != null) {
            statusHandler.error(
                    "Unable to store product text, most likely the database already contains an entry",
                    response.getExceptions());
        }
        return response;
    }

    /**
     * Sends a request to EDEX to update the text.
     * 
     * @param key
     * @param productCategory
     * @param productID
     * @param segment
     * @param eventIDs
     * @param officeID
     * @param value
     */
    public static ProductTextResponse updateProductText(String key,
            String productCategory, String productID, String segment,
            ArrayList<String> eventIDs, String officeID, Serializable value) {
        ProductTextResponse response = sendRequest(key, productCategory,
                productID, segment, eventIDs, officeID, value,
                ProductRequestType.UPDATE);
        if (response.getExceptions() != null) {
            statusHandler.error(
                    "Unable to update product text, most likely the database does not contain a matching entry",
                    response.getExceptions());
        }
        return response;
    }

    /**
     * Sends a request to EDEX to delete the text based on the key in the map,
     * the product category, the product id, the segment, the event id, and the
     * office id.
     * 
     * @param key
     * @param productCategory
     * @param productID
     * @param segment
     * @param eventIDs
     * @param officeID
     */
    public static ProductTextResponse deleteProductText(String key,
            String productCategory, String productID, String segment,
            ArrayList<String> eventIDs, String officeID) {
        ProductTextResponse response = sendRequest(key, productCategory,
                productID, segment, eventIDs, officeID, null,
                ProductRequestType.DELETE);
        if (response.getExceptions() != null) {
            statusHandler.error(
                    "Unable to update product text, most likely the database does not contain a matching entry",
                    response.getExceptions());
        }
        return response;
    }

    /**
     * Sends a request to EDEX that retrieves text based on the user, the key in
     * the map, the product category, the product id, the segment, the event id,
     * and the office id.
     * 
     * @param key
     * @param productCategory
     * @param productID
     * @param segment
     * @param eventIDs
     * @param officeID
     * @return
     */
    public static List<ProductText> retrieveProductText(String key,
            String productCategory, String productID, String segment,
            ArrayList<String> eventIDs, String officeID) {
        ProductTextResponse response = sendRequest(key, productCategory,
                productID, segment, eventIDs, officeID, null,
                ProductRequestType.RETRIEVE);
        if (response != null && response.getText() != null) {
            return response.getText();
        }
        return null;
    }

    /**
     * Either stores, or updates an item in the database.
     * 
     * @param key
     * @param productCategory
     * @param productID
     * @param segment
     * @param eventIDs
     * @param officeID
     * @param value
     * @return
     */
    public static ProductTextResponse createOrUpdateProductText(String key,
            String productCategory, String productID, String segment,
            ArrayList<String> eventIDs, String officeID, Serializable value) {
        ProductTextResponse response = sendRequest(key, productCategory,
                productID, segment, eventIDs, officeID, value,
                ProductRequestType.SAVE_OR_UPDATE);
        if (response.getExceptions() != null) {
            statusHandler.error("Unable to store product text",
                    response.getExceptions());
        }
        return response;
    }

    /**
     * Sends a request to EDEX that retrieves text based on the user, the key in
     * the map, the product category, the product id, the segment, the event id,
     * and the office id. The resulting items are then exported to the provided
     * filename.
     *
     * @param key
     * @param productCategory
     * @param productID
     * @param segment
     * @param eventIDs
     * @param officeID
     * @param filePath
     * @return
     */
    public static ProductTextResponse exportProductText(String key,
            String productCategory, String productID, String segment,
            ArrayList<String> eventIDs, String officeID, String filePath) {
        ProductTextResponse response = sendRequest(key, productCategory,
                productID, segment, eventIDs, officeID, null,
                ProductRequestType.EXPORT, filePath);
        if (response.getExceptions() != null) {
            statusHandler.error("Unable to export product text to " + filePath,
                    response.getExceptions());
        }

        return response;
    }

    /**
     * Sends a request to EDEX to import items from the provided file path.
     *
     * @param filePath
     * @return
     */
    public static ProductTextResponse importProductText(String filePath) {
        ProductTextResponse response = sendRequest(null, null, null, null, null,
                null, null, ProductRequestType.IMPORT, filePath);
        if (response.getExceptions() != null) {
            statusHandler.error("Unable to import product text to " + filePath,
                    response.getExceptions());
        }

        return response;
    }

    /**
     * Helper method for sending the request through.
     * 
     * @param key
     * @param productCategory
     * @param productID
     * @param segment
     * @param eventIDs
     * @param officeID
     * @param value
     * @param type
     * @param filePath
     * @return
     */
    private static ProductTextResponse sendRequest(String key,
            String productCategory, String productID, String segment,
            ArrayList<String> eventIDs, String officeID, Serializable value,
            ProductRequestType type) {
        return sendRequest(key, productCategory, productID, segment, eventIDs,
                officeID, value, type, null);
    }

    /**
     * Helper method for sending the request through.
     *
     * @param key
     * @param productCategory
     * @param productID
     * @param segment
     * @param eventIDs
     * @param officeID
     * @param value
     * @param type
     * @param filePath
     * @return
     */
    private static ProductTextResponse sendRequest(String key,
            String productCategory, String productID, String segment,
            ArrayList<String> eventIDs, String officeID, Serializable value,
            ProductRequestType type, String filePath) {
        ProductText text = new ProductText(key, productCategory, productID,
                segment, eventIDs, officeID, value);
        ProductTextRequest request = new ProductTextRequest(text, type,
                filePath);
        ProductTextResponse response = null;
        try {
            response = (ProductTextResponse) RequestRouter.route(request);
        } catch (Exception e) {
            statusHandler.error("Unable to send request to server", e);
        }
        return response;
    }
}

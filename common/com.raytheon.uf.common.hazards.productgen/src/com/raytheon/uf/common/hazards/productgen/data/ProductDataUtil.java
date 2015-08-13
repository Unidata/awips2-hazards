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

import com.raytheon.uf.common.hazards.productgen.EditableEntryMap;
import com.raytheon.uf.common.hazards.productgen.data.ProductDataRequest.ProductRequestType;
import com.raytheon.uf.common.serialization.comm.RequestRouter;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

/**
 * Utility class for getting and storing product data for correction or review.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 11, 2014            jsanchez     Initial creation
 * Mar 30, 2015    6929    Robert.Blum  Changed startTime to issueTime.
 * May 07, 2015    6979    Robert.Blum  Changes for product corrections.
 * Jul 30, 2015    9681    Robert.Blum  Added new method to retrieve all
 *                                      viewable productData.
 * Aug 13, 2015 8836       Chris.Cody   Changes for a configurable Event Id
 * 
 * </pre>
 * 
 * @author jsanchez
 * @version 1.0
 */

public class ProductDataUtil {

    private static final IUFStatusHandler handler = UFStatus
            .getHandler(ProductDataUtil.class);

    /**
     * Sends a request to EDEX to store the data
     * 
     * @param mode
     * @param productGeneratorName
     * @param eventIDs
     * @param issueTime
     * @param data
     * @param editableEntries
     * @return
     */
    public static ProductDataResponse createProductData(String mode,
            String productGeneratorName, ArrayList<String> eventIDs,
            Date issueTime, Map<String, Serializable> data,
            List<EditableEntryMap> editableEntries) {
        ProductDataResponse response = sendRequest(mode, productGeneratorName,
                eventIDs, issueTime, data, editableEntries,
                ProductRequestType.CREATE, null);
        if (response.getExceptions() != null) {
            handler.error(
                    "Unable to store product data, most likely the database already contains an entry",
                    response.getExceptions());
        }
        return response;
    }

    /**
     * Sends a request to EDEX to update the data.
     * 
     * @param mode
     * @param productGeneratorName
     * @param eventIDs
     * @param issueTime
     * @param data
     * @param editableEntries
     * @return
     */
    public static ProductDataResponse updateProductData(String mode,
            String productGeneratorName, ArrayList<String> eventIDs,
            Date issueTime, Map<String, Serializable> data,
            List<EditableEntryMap> editableEntries) {
        ProductDataResponse response = sendRequest(mode, productGeneratorName,
                eventIDs, issueTime, data, editableEntries,
                ProductRequestType.UPDATE, null);
        if (response.getExceptions() != null) {
            handler.error(
                    "Unable to update product data, most likely the database does not contain a matching entry",
                    response.getExceptions());
        }
        return response;
    }

    /**
     * Sends a request to EDEX to delete the data
     * 
     * @param mode
     * @param productGeneratorName
     * @param eventIDs
     * @return
     */
    public static ProductDataResponse deleteProductData(String mode,
            String productGeneratorName, ArrayList<String> eventIDs) {
        ProductDataResponse response = sendRequest(mode, productGeneratorName,
                eventIDs, null, null, null, ProductRequestType.DELETE, null);
        if (response.getExceptions() != null) {
            handler.error(
                    "Unable to delete product data, most likely the database does not contain a matching entry",
                    response.getExceptions());
        }
        return response;
    }

    /**
     * Sends a request to EDEX that retrieves data
     * 
     * @param mode
     * @param productGeneratorName
     * @param eventIDs
     * @return
     */
    public static List<ProductData> retrieveProductData(String mode,
            String productGeneratorName, ArrayList<String> eventIDs) {
        ProductDataResponse response = sendRequest(mode, productGeneratorName,
                eventIDs, null, null, null, ProductRequestType.RETRIEVE, null);
        if (response != null && response.getData() != null) {
            return response.getData();
        }
        return new ArrayList<ProductData>();
    }

    /**
     * Sends a request to EDEX that retrieves data that is correctable.
     * 
     * @param mode
     * @param currentTime
     * @return
     */
    public static List<ProductData> retrieveCorrectableProductData(String mode,
            Date currentTime) {
        ProductDataResponse response = sendRequest(mode, null, null, null,
                null, null, ProductRequestType.RETRIEVE_CORRECTABLE,
                currentTime);
        if (response != null && response.getData() != null) {
            return response.getData();
        }
        return new ArrayList<ProductData>();
    }

    /**
     * Either stores, or updates an item in the database.
     * 
     * @param mode
     * @param productGeneratorName
     * @param eventIDs
     * @param issueTime
     * @param data
     * @param editableEntries
     * @return
     */
    public static ProductDataResponse createOrUpdateProductData(String mode,
            String productGeneratorName, ArrayList<String> eventIDs,
            Date issueTime, Map<String, Serializable> data,
            List<EditableEntryMap> editableEntries) {
        ProductDataResponse response = sendRequest(mode, productGeneratorName,
                eventIDs, issueTime, data, editableEntries,
                ProductRequestType.SAVE_OR_UPDATE, null);
        if (response.getExceptions() != null) {
            handler.error("Unable to store product data",
                    response.getExceptions());
        }
        return response;
    }

    /**
     * Sends a request to EDEX that retrieves data that is viewable.
     * 
     * @param mode
     * @param currentTime
     * @return
     */
    public static List<ProductData> retrieveViewableProductData(String mode,
            Date currentTime) {
        ProductDataResponse response = sendRequest(mode, null, null, null,
                null, null, ProductRequestType.RETRIEVE_VIEWABLE, currentTime);
        if (response != null && response.getData() != null) {
            return response.getData();
        }
        return new ArrayList<ProductData>();
    }

    /**
     * Helper method for sending the request through.
     * 
     * @param mode
     * @param productGeneratorName
     * @param eventIDs
     * @param issueTime
     * @param data
     * @param editableEntries
     * @param type
     * @param currentTime
     * 
     * @return
     */
    private static ProductDataResponse sendRequest(String mode,
            String productGeneratorName, ArrayList<String> eventIDs,
            Date issueTime, Map<String, Serializable> data,
            List<EditableEntryMap> editableEntries, ProductRequestType type,
            Date currentTime) {
        ProductData productData = new ProductData(mode, productGeneratorName,
                eventIDs, issueTime, (HashMap<String, Serializable>) data,
                (ArrayList<EditableEntryMap>) editableEntries);
        ProductDataRequest request = new ProductDataRequest(productData, type,
                currentTime);
        ProductDataResponse response = null;
        try {
            response = (ProductDataResponse) RequestRouter.route(request);
        } catch (Exception e) {
            handler.error("Unable to send request to server", e);
        }
        return response;
    }
}

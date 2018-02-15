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
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.hazards.productgen.GeneratedProductList;
import com.raytheon.uf.common.hazards.productgen.IGeneratedProduct;
import com.raytheon.uf.common.hazards.productgen.ProductPart;
import com.raytheon.uf.common.hazards.productgen.editable.ProductTextRequest.ProductRequestType;
import com.raytheon.uf.common.serialization.comm.RequestRouter;
import com.raytheon.uf.common.status.IPerformanceStatusHandler;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.PerformanceStatus;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.SimulatedTime;

/**
 * Utility class for getting and storing product text for product generation.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Aug 26, 2013            mnash        Initial creation
 * Apr  7, 2014 2917       jsanchez     Changed the methods to accept eventIDs as a List<Integer>
 * Aug 03, 2015 8836       Chris.Cody   Changes for a configurable Event Id
 * Nov 10, 2016 22119      Kevin.Bisanz Changes to export/import product text by officeID.
 * Apr 27, 2017 29776      Kevin.Bisanz Add insertTime to ProductText
 * Jun 05, 2017 29996      Robert.Blum  Changes for new previous text design.
 * Jun 12, 2017 35022      Kevin.Bisanz Remove productID, add mode, value is String.
 * Jun 19, 2017 35022      Kevin.Bisanz Retrieve more previous text with single query, filter in code.
 * Jun 06, 2018 15561      Chris.Golden Added check for non-existent editable product parts.
 * 
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */

public class ProductTextUtil {

    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(ProductTextUtil.class);

    private final static IPerformanceStatusHandler perfLog = PerformanceStatus
            .getHandler("ProductTextUtil:");

    /**
     * Sends a request to EDEX to store the text based on the key in the map,
     * the product category, the product id, the segment, the event id, and the
     * office id.
     * 
     * @param key
     * @param productCategory
     * @param mode
     * @param segment
     * @param eventIDs
     * @param officeID
     * @param value
     */
    public static ProductTextResponse createProductText(String key,
            String productCategory, String mode, String segment,
            List<String> eventIDs, String officeID, String value) {
        Date insertTime = SimulatedTime.getSystemTime().getTime();

        ProductTextResponse response = sendRequest(key, productCategory, mode,
                segment, eventIDs, officeID, insertTime, value,
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
     * @param mode
     * @param segment
     * @param eventIDs
     * @param officeID
     * @param value
     */
    public static ProductTextResponse updateProductText(String key,
            String productCategory, String mode, String segment,
            List<String> eventIDs, String officeID, String value) {
        Date insertTime = SimulatedTime.getSystemTime().getTime();

        ProductTextResponse response = sendRequest(key, productCategory, mode,
                segment, eventIDs, officeID, insertTime, value,
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
     * @param mode
     * @param segment
     * @param eventIDs
     * @param officeID
     */
    public static ProductTextResponse deleteProductText(String key,
            String productCategory, String mode, String segment,
            List<String> eventIDs, String officeID) {
        ProductTextResponse response = sendRequest(key, productCategory, mode,
                segment, eventIDs, officeID, null, null,
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
     * @param mode
     * @param segment
     * @param eventIDs
     * @param officeID
     * @return
     */
    public static List<ProductText> retrieveProductText(String key,
            String productCategory, String mode, String segment,
            List<String> eventIDs, String officeID) {

        ProductTextResponse response = sendRequest(key, productCategory, mode,
                segment, eventIDs, officeID, null, null,
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
     * @param mode
     * @param segment
     * @param eventIDs
     * @param officeID
     * @param value
     * @return
     */
    public static ProductTextResponse createOrUpdateProductText(String key,
            String productCategory, String mode, String segment,
            List<String> eventIDs, String officeID, String value) {
        Date insertTime = SimulatedTime.getSystemTime().getTime();

        ProductTextResponse response = sendRequest(key, productCategory, mode,
                segment, eventIDs, officeID, insertTime, value,
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
     * @param mode
     * @param segment
     * @param eventIDs
     * @param officeID
     * @param filePath
     * @return
     */
    public static ProductTextResponse exportProductText(String key,
            String productCategory, String mode, String segment,
            List<String> eventIDs, String officeID, String filePath) {
        ProductTextResponse response = sendRequest(key, productCategory, mode,
                segment, eventIDs, officeID, null, null,
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
                null, null, null, ProductRequestType.IMPORT, filePath);
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
     * @param mode
     * @param segment
     * @param eventIDs
     * @param officeID
     * @param insertTime
     * @param value
     * @param type
     * @param filePath
     * @return
     */
    private static ProductTextResponse sendRequest(String key,
            String productCategory, String mode, String segment,
            List<String> eventIDs, String officeID, Date insertTime,
            String value, ProductRequestType type) {
        return sendRequest(key, productCategory, mode, segment, eventIDs,
                officeID, insertTime, value, type, null);
    }

    /**
     * Helper method for sending the request through.
     *
     * @param key
     * @param productCategory
     * @param mode
     * @param segment
     * @param eventIDs
     * @param officeID
     * @param value
     * @param type
     * @param filePath
     * @return
     */
    private static ProductTextResponse sendRequest(String key,
            String productCategory, String mode, String segment,
            List<String> eventIDs, String officeID, Date insertTime,
            String value, ProductRequestType type, String filePath) {
        ProductTextRequest request = new ProductTextRequest(type, eventIDs, key,
                officeID, productCategory, mode, segment, value, insertTime,
                filePath);
        ProductTextResponse response = null;
        try {
            response = (ProductTextResponse) RequestRouter.route(request);
        } catch (Exception e) {
            response = new ProductTextResponse();
            response.setExceptions(e);
            statusHandler.error("Unable to send request to server", e);
        }
        return response;
    }

    public static void queryProductText(GeneratedProductList productList) {
        setEditableEntriesOnProduct(productList);
        ProductTextUtil.retrieveProductText(productList);
    }

    /**
     * Retrieves previously saved user edited text from the ProductText table
     * for each editable product part in each generated product.
     * 
     * @param productList
     */
    public static void retrieveProductText(GeneratedProductList productList) {
        perfLog.log("retrieveProductText()");
        for (IGeneratedProduct product : productList) {
            retrieveProductTextForSingleProduct(product);
        }
    }

    /**
     * Retrieves previously saved user edited text from the ProductText table
     * for each editable product part for the specified product.
     * 
     * @param product
     */
    private static void retrieveProductTextForSingleProduct(
            IGeneratedProduct product) {
        Map<String, Serializable> data = product.getData();
        String productCategory = (String) data.get("productCategory");
        String officeID = (String) data.get("siteID");
        String mode = (String) product.getEventSet()
                .getAttribute(HazardConstants.RUN_MODE);

        /*
         * Get all product parts for any event ID in the product all at once.
         */
        Set<String> eventIds = new HashSet<>();
        for (ProductPart part : product.getEditableEntries()) {
            eventIds.addAll(part.getKeyInfo().getEventIDs());
        }
        List<ProductText> productText = retrieveProductText(null /* any part */,
                productCategory, mode, null /* any ugc */,
                new ArrayList<>(eventIds), officeID);
        /*
         * For each product part attempt to find previous productText.
         */
        for (ProductPart part : product.getEditableEntries()) {
            /*
             * Only query for editable parts that don't already have the
             * previous text set. Staging dialog fields (overview synopsis, etc)
             * are queried for in the metadata.
             */
            if (part.isEditable()) {
                /*
                 * Reduce the full list of previously edited text down to items
                 * for this combination of part name and event IDs.
                 */
                List<ProductText> productTextForPart = filterProductText(
                        productText, part.getName(),
                        part.getKeyInfo().getEventIDs());

                /*
                 * UGCs do not have to match exactly. Filter these manually.
                 */
                List<String> partUGCs = splitSegment(
                        part.getKeyInfo().getSegment());

                ProductText textToUse = null;
                for (ProductText savedText : productTextForPart) {
                    List<String> savedUGCs = splitSegment(
                            savedText.getSegment());
                    List<String> workingPartUGCs = new LinkedList<>(partUGCs);
                    workingPartUGCs.removeAll(savedUGCs);
                    /*
                     * If some/all UGCs were removed it indicates overlap in the
                     * UGC lists and we should consider the saved text. For
                     * example, CAN or EXA/EXB situations.
                     */
                    if (workingPartUGCs.size() != partUGCs.size()) {
                        if (textToUse == null) {
                            textToUse = savedText;
                        } else if (savedText.getInsertTime()
                                .after((textToUse.getInsertTime()))) {
                            textToUse = savedText;
                        }
                    }
                }
                if (textToUse != null) {
                    String value = textToUse.getValue();
                    part.setUsePreviousText(true);
                    part.setPreviousText(value);
                    part.setCurrentText(value);
                }
            }
        }
    }

    /**
     * Filter the provided ProductText by part name and/or event ID
     *
     * @param productText
     *            Items input into the filtering.
     * @param partName
     *            Product part name to match, or null if no restriction on part
     *            name.
     * @param eventIDs
     *            Event IDs to match one of, or null if no restriction on event
     *            ID.
     * @return List of ProductText matching part name and event ID criteria.
     */
    private static List<ProductText> filterProductText(
            List<ProductText> productText, String partName,
            List<String> eventIDs) {
        List<ProductText> filtered = new ArrayList<>(productText.size());
        for (ProductText text : productText) {
            /*
             * A null for one of the criteria indicates no restriction for that
             * criteria.
             */
            boolean partNameMatch = (partName == null
                    || partName.equals(text.getKey()));
            boolean eventIdMatch = (eventIDs == null
                    || eventIDs.contains(text.getEventID()));

            if (partNameMatch && eventIdMatch) {
                filtered.add(text);
            }
        }

        return filtered;
    }

    private static List<String> splitSegment(String segment) {
        String[] ugcs = segment.split(",");
        for (int i = 0; i < ugcs.length; ++i) {
            ugcs[i] = ugcs[i].trim();
        }
        return new ArrayList<>(Arrays.asList(ugcs));
    }

    /**
     * Sets the editable entries on the each generated product in the
     * GeneratedProductList.
     * 
     * @param productList
     */
    private static void setEditableEntriesOnProduct(
            GeneratedProductList productList) {
        for (IGeneratedProduct product : productList) {
            Map<String, Serializable> data = product.getData();
            @SuppressWarnings("unchecked")
            List<ProductPart> parts = (List<ProductPart>) data
                    .get("productParts");
            if (parts != null) {
                List<ProductPart> editableParts = new ArrayList<>();
                for (ProductPart part : parts) {
                    if (part.getSubParts() != null
                            && part.getSubParts().isEmpty() == false) {
                        editableParts.addAll(getEditableSubParts(part));
                    } else if (part.isDisplayable() || part.isEditable()) {
                        editableParts.add(part);
                    }
                }
                product.setEditableEntries(editableParts);
            }
        }
    }

    /**
     * Returns a list of all the editable sub parts of the given Product Part.
     * 
     * @param part
     * @return
     */
    private static Collection<ProductPart> getEditableSubParts(
            ProductPart part) {
        List<ProductPart> editableParts = new ArrayList<>();
        for (List<ProductPart> subPartList : part.getSubParts()) {
            for (ProductPart subPart : subPartList) {
                if (subPart.getSubParts() != null
                        && subPart.getSubParts().isEmpty() == false) {
                    editableParts.addAll(getEditableSubParts(subPart));
                } else if (subPart.isDisplayable() || subPart.isEditable()) {
                    editableParts.add(subPart);
                }
            }
        }
        return editableParts;
    }
}

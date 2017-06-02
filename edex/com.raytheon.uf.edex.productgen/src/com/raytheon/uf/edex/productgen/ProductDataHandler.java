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
package com.raytheon.uf.edex.productgen;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import com.raytheon.uf.common.dataquery.db.QueryParam.QueryOperand;
import com.raytheon.uf.common.hazards.productgen.data.ProductData;
import com.raytheon.uf.common.hazards.productgen.data.ProductDataRequest;
import com.raytheon.uf.common.hazards.productgen.data.ProductDataResponse;
import com.raytheon.uf.common.serialization.comm.IRequestHandler;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.edex.database.dao.CoreDao;
import com.raytheon.uf.edex.database.dao.DaoConfig;
import com.raytheon.uf.edex.database.query.DatabaseQuery;

/**
 * Handles ProductDataRequest to create, update, delete, save, and retrieve
 * product data.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 15, 2014            jsanchez     Initial creation
 * Jan 15, 2014 4193       rferrel      Log request
 * Mar 30, 2015 6929       Robert.Blum  Changed startTime to issueTime.
 * Jul 30, 2015 9681       Robert.Blum  Updates for new ProductRequestType.
 * Jul 31, 2015 9682       Robert.Blum  Filtering out products that should not be corrected.
 * Aug 13, 2015 8836       Chris.Cody   Changes for a configurable Event Id
 * Sep 11, 2015 10203      Robert.Blum  Productdata table now holds entries for all issued events
 *                                      not just the latest for that eventID(s).
 * Jul 01, 2016 18257      Kevin.Bisanz Add mode restriction to criteria in case
 *                                      of RETRIEVE_VIEWABLE and
 *                                      RETRIEVE_CORRECTABLE. Change
 *                                      RETRIEVE_CORRECTABLE time comparison
 *                                      from GT to GE.
 * Jul 19, 2016 19207      Robert.Blum  Changes to view products for specific events.
 * Aug 26, 2016 19223      Kevin.Bisanz Changes to get correctable products for
 *                                      specific events.
 * 
 * </pre>
 * 
 * @author jsanchez
 * @version 1.0
 */

public class ProductDataHandler implements IRequestHandler<ProductDataRequest> {

    private static final IUFStatusHandler handler = UFStatus
            .getHandler(ProductDataHandler.class);

    private static final String ISSUE_TIME_COLUMN = "id.issueTime";

    private static final String RVS_GENERATOR_NAME = "RVS_ProductGenerator";

    private static final String ESF_GENERATOR_NAME = "ESF_ProductGenerator";

    private static final String FLW_FLS_GENERATOR_NAME = "FLW_FLS_ProductGenerator";

    private static final int VALID_CORRECTION_DELTA = 10;

    private final CoreDao dao;

    public ProductDataHandler() {
        dao = new CoreDao(DaoConfig.DEFAULT);
    }

    @Override
    public ProductDataResponse handleRequest(ProductDataRequest request)
            throws Exception {
        ProductDataResponse response = new ProductDataResponse();
        ProductData pData = request.getProductData();
        Criteria criteria = null;
        List<ProductData> data = null;
        List<ProductData> filteredData = null;
        if (handler.isPriorityEnabled(Priority.INFO)) {

            List<String> eventIDs = pData.getEventIDs();
            handler.info("ProductGeneratorName: "
                    + pData.getProductGeneratorName() + ", mode: "
                    + pData.getMode() + ", type: " + request.getType()
                    + ", eventIDs: " + (eventIDs == null ? "ALL" : eventIDs));
        }

        switch (request.getType()) {
        case CREATE:
            try {
                dao.create(pData);
            } catch (RuntimeException e) {
                response.setExceptions(e);
            }
            break;
        case UPDATE:
            try {
                dao.update(pData);
            } catch (RuntimeException e) {
                response.setExceptions(e);
            }
            break;
        case DELETE:
            try {
                DatabaseQuery query = new DatabaseQuery(ProductData.class);
                query.addQueryParam("id.mode", pData.getMode(),
                        QueryOperand.EQUALS);
                if (pData.getEventIDs() != null) {
                    query.addQueryParam("id.eventIDs", pData.getEventIDs(),
                            QueryOperand.EQUALS);
                }
                if (pData.getProductGeneratorName() != null) {
                    query.addQueryParam("id.productGeneratorName",
                            pData.getProductGeneratorName(),
                            QueryOperand.EQUALS);
                }
                if (pData.getIssueTime() != null) {
                    query.addQueryParam(ISSUE_TIME_COLUMN,
                            pData.getIssueTime(), QueryOperand.EQUALS);
                }

                dao.deleteByCriteria(query);
            } catch (RuntimeException e) {
                response.setExceptions(e);
            }
            break;
        case SAVE_OR_UPDATE:
            try {
                dao.saveOrUpdate(pData);
            } catch (RuntimeException e) {
                response.setExceptions(e);
            }
            break;
        case RETRIEVE:
            criteria = dao.getSessionFactory().openSession()
                    .createCriteria(ProductData.class);
            criteria.add(Restrictions.eq("id.mode", pData.getMode()));
            criteria.add(Restrictions.eq("id.productGeneratorName",
                    pData.getProductGeneratorName()));
            criteria.add(Restrictions.eq("id.eventIDs", pData.getEventIDs()));
            criteria.add(Restrictions.eq(ISSUE_TIME_COLUMN,
                    pData.getIssueTime()));

            data = criteria.list();
            response = new ProductDataResponse();
            response.setData(data);
            break;
        case RETRIEVE_CORRECTABLE:
            criteria = dao.getSessionFactory().openSession()
                    .createCriteria(ProductData.class);
            criteria.add(Restrictions.eq("id.mode", pData.getMode()));

            /*
             * This only queries for rows that has the current time within 10
             * minutes after the issue time.
             */
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(request.getCurrentTime());
            calendar.add(Calendar.MINUTE, -VALID_CORRECTION_DELTA);
            criteria.add(Restrictions.ge(ISSUE_TIME_COLUMN, calendar.getTime()));

            /*
             * Filter out products that should not be able to be corrected (RVS,
             * HY.O).
             */
            criteria.add(Restrictions.ne("id.productGeneratorName",
                    RVS_GENERATOR_NAME));
            criteria.add(Restrictions.ne("id.productGeneratorName",
                    ESF_GENERATOR_NAME));
            data = criteria.list();

            /*
             * Filter out remaining products that should not be able to be
             * corrected, but can not be filtered by generator name (HY.S).
             */
            for (ProductData productData : new ArrayList<>(data)) {
                if (productData.getProductGeneratorName().equals(
                        FLW_FLS_GENERATOR_NAME)) {
                    boolean remove = checkForHYS(productData);
                    if (remove) {
                        data.remove(productData);
                    }
                }
            }
            response = new ProductDataResponse();
            filteredData = filterPDataForCorrections(data);
            filteredData = filterPDataForEventIDs(filteredData,
                    pData.getEventIDs());
            response.setData(filteredData);
            break;
        case RETRIEVE_VIEWABLE:
            criteria = dao.getSessionFactory().openSession()
                    .createCriteria(ProductData.class);
            criteria.add(Restrictions.eq("id.mode", pData.getMode()));

            data = criteria.list();

            response = new ProductDataResponse();

            // Filter by event ID.
            filteredData = filterPDataForEventIDs(data, pData.getEventIDs());
            response.setData(filteredData);
            break;
        }
        return response;
    }

    /*
     * Filters the list of product data for corrections. Removing any entries
     * that have common eventIDs, keeping only the one with the latest
     * issueTime.
     */
    private List<ProductData> filterPDataForCorrections(
            List<ProductData> pDataList) {
        List<ProductData> filteredPDataList = new ArrayList<>(pDataList);
        for (int i = 0; i < pDataList.size(); i++) {
            for (int j = i + 1; j < pDataList.size(); j++) {
                int value = compareProductData(pDataList.get(i),
                        pDataList.get(j));
                if (value == 1) {
                    filteredPDataList.remove(pDataList.get(j));
                } else if (value == -1) {
                    filteredPDataList.remove(pDataList.get(i));
                }
            }
        }
        return filteredPDataList;
    }

    /**
     * Filter the input data list to contain only products with the event IDs
     * provided. The input list is not modified.
     * 
     * @param data
     *            Data which to filter
     * @param eventIDs
     *            Event IDs to keep. Null or empty list performs no filtering.
     * @return Data that has been filter
     */
    private List<ProductData> filterPDataForEventIDs(List<ProductData> data,
            List<String> eventIDs) {
        List<ProductData> keepList = new ArrayList<>();

        /*
         * If eventIDs are specified, only keep product data that have at least
         * one of those eventIDs.
         */
        if (eventIDs != null && eventIDs.isEmpty() == false) {
            for (ProductData productData : data) {
                for (String eventID : productData.getEventIDs()) {
                    if (eventIDs.contains(eventID)) {
                        keepList.add(productData);
                        break;
                    }
                }
            }
        } else {
            // Nothing to filter on, keep everything.
            keepList.addAll(data);
        }
        return keepList;
    }

    /**
     * Compares the product data objects. If they share any one common eventID
     * then the issueTime is compared. If pd1 issueTime is after pd2 issueTime 1
     * is return. If pd2 issueTime is after pd1 issueTime then -1 is return. If
     * the two product data object don't share any common eventIDs then 0 is
     * return.
     */
    private int compareProductData(ProductData pd1, ProductData pd2) {
        for (String pd1EventID : pd1.getEventIDs()) {
            for (String pd2EventID : pd2.getEventIDs()) {
                if (pd1EventID.equals(pd2EventID)) {
                    if (pd1.getIssueTime().after(pd2.getIssueTime())) {
                        return 1;
                    } else {
                        return -1;
                    }
                }
            }
        }
        return 0;
    }

    /*
     * Determines if the given productData is for a HY.S product that was issued
     * by itself.
     */
    private boolean checkForHYS(ProductData data) {
        boolean removeProductData = true;
        Map<String, Serializable> productDictionary = data.getData();

        /*
         * Pull the segments out of the dictionary and look at the VTEC Records
         * to determine if the product was a HY.S that was issued by itself.
         */
        if (productDictionary.containsKey("segments")) {
            Serializable segments = productDictionary.get("segments");
            if (segments instanceof List) {
                List<Map<String, Serializable>> segmentList = (List<Map<String, Serializable>>) segments;
                for (Map<String, Serializable> segmentDictionary : segmentList) {
                    if (segmentDictionary.containsKey("vtecRecords")) {
                        Serializable vtecRecords = segmentDictionary
                                .get("vtecRecords");
                        if (vtecRecords instanceof List) {
                            List<Map<String, Serializable>> vtecRecordsList = (List<Map<String, Serializable>>) vtecRecords;
                            for (Map<String, Serializable> vtecRecordDictionary : vtecRecordsList) {
                                if (vtecRecordDictionary.containsKey("phensig")) {
                                    if (vtecRecordDictionary.get("phensig")
                                            .equals("HY.S") == false) {
                                        // Not a HY.S - do not remove
                                        removeProductData = false;
                                        break;
                                    }
                                }
                            }
                            if (removeProductData == false) {
                                break;
                            }
                        }
                    }
                }
            }
        }
        return removeProductData;
    }
}

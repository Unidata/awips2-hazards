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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

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
 * 
 * </pre>
 * 
 * @author jsanchez
 * @version 1.0
 */

public class ProductDataHandler implements IRequestHandler<ProductDataRequest> {

    private static final IUFStatusHandler handler = UFStatus
            .getHandler(ProductDataHandler.class);

    private static final String ISSUE_TIME_COLUMN = "issueTime";

    private static final int VALID_CORRECTION_DELTA = 10;

    private CoreDao dao;

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
        if (handler.isPriorityEnabled(Priority.INFO)) {

            List<Integer> eventIDs = pData.getEventIDs();
            handler.info("ProductGeneratorName: "
                    + pData.getProductGeneratorName() + ", mode: "
                    + pData.getMode() + ", type: " + request.getType()
                    + ", eventIDs: " + (eventIDs == null ? "ALL" : eventIDs));
        }

        switch (request.getType()) {
        case CREATE:
            try {
                deleteExisting(pData);
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

                dao.deleteByCriteria(query);
            } catch (RuntimeException e) {
                response.setExceptions(e);
            }
            break;
        case SAVE_OR_UPDATE:
            try {
                deleteExisting(pData);
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

            data = criteria.list();
            response = new ProductDataResponse();
            response.setData(data);
            break;
        case RETRIEVE_CORRECTABLE:
            criteria = dao.getSessionFactory().openSession()
                    .createCriteria(ProductData.class);

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(request.getCurrentTime());
            calendar.add(Calendar.MINUTE, -VALID_CORRECTION_DELTA);

            /*
             * This only queries for rows that has the current time within 10
             * minutes after the issue time.
             */
            criteria.add(Restrictions.gt(ISSUE_TIME_COLUMN, calendar.getTime()));
            data = criteria.list();

            response = new ProductDataResponse();
            response.setData(data);
            break;
        }
        return response;
    }

    /*
     * Deletes from the productdata table that have eventIDs that contain
     * eventIDs in the pData.
     */
    private void deleteExisting(ProductData pData) {
        Criteria criteria = dao.getSessionFactory().openSession()
                .createCriteria(ProductData.class);
        criteria.add(Restrictions.eq("id.productGeneratorName",
                pData.getProductGeneratorName()));
        criteria.add(Restrictions.eq("id.mode", pData.getMode()));

        List<ProductData> data = criteria.list();
        List<ProductData> toRemove = new ArrayList<ProductData>();
        for (ProductData pd : data) {
            for (Integer eventID : pData.getEventIDs()) {
                if (pd.getEventIDs().contains(eventID)) {
                    toRemove.add(pd);
                    break;
                }
            }
        }

        if (toRemove.isEmpty() == false) {
            dao.deleteAll(toRemove);
        }
    }
}

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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import com.raytheon.uf.common.hazards.productgen.editable.ProductText;
import com.raytheon.uf.common.hazards.productgen.editable.ProductTextRequest;
import com.raytheon.uf.common.hazards.productgen.editable.ProductTextResponse;
import com.raytheon.uf.common.serialization.SerializationException;
import com.raytheon.uf.common.serialization.SerializationUtil;
import com.raytheon.uf.common.serialization.comm.IRequestHandler;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.edex.database.dao.CoreDao;
import com.raytheon.uf.edex.database.dao.DaoConfig;

/**
 * Handler to do the CRUD operations on the product text table.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 26, 2013            mnash     Initial creation
 * Nov 10, 2016 22119      Kevin.Bisanz Changes to export product text by officeID
 * Apr 27, 2017 29776      Kevin.Bisanz Add insertTime on ProductText
 * Jun 12, 2017 35022      Kevin.Bisanz Changes to save a ProductText row for each event and part.
 * Jun 20, 2017 35022      Kevin.Bisanz Handle case of event id list being empty but not null.
 * 
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */

public class ProductTextHandler implements IRequestHandler<ProductTextRequest> {

    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(ProductTextHandler.class);

    /**
     * 
     */
    public ProductTextHandler() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.common.serialization.comm.IRequestHandler#handleRequest
     * (com.raytheon.uf.common.serialization.comm.IServerRequest)
     */
    @Override
    public Object handleRequest(ProductTextRequest request) throws Exception {
        CoreDao dao = new CoreDao(DaoConfig.DEFAULT);
        ProductTextResponse response = new ProductTextResponse();
        List<ProductText> productTexts = createProductText(request);
        statusHandler.info(request.getType().name() + " : "
                + prettyPrintRequest(request) + " : " + request.getPath());
        switch (request.getType()) {
        case CREATE:
            try {
                for (ProductText productText : productTexts) {
                    dao.create(productText);
                }
            } catch (RuntimeException e) {
                response.setExceptions(e);
            }
            break;
        case UPDATE:
            try {
                for (ProductText productText : productTexts) {
                    dao.update(productText);
                }
            } catch (RuntimeException e) {
                response.setExceptions(e);
            }
            break;
        case DELETE:
            for (ProductText productText : productTexts) {
                dao.delete(productText);
            }
            try {
            } catch (RuntimeException e) {
                response.setExceptions(e);
            }
            break;
        case SAVE_OR_UPDATE:
            try {
                for (ProductText productText : productTexts) {
                    dao.saveOrUpdate(productText);
                }
            } catch (RuntimeException e) {
                response.setExceptions(e);
            }
            break;
        case RETRIEVE:
            List<ProductText> text = retrieve(request, dao);
            response = new ProductTextResponse();
            response.setText(text);
            break;
        case EXPORT:
            exportRecords(request, dao, response);
            break;
        case IMPORT:
            importRecords(request, dao, response);
            break;
        }
        return response;
    }

    private List<ProductText> createProductText(ProductTextRequest request) {
        List<String> eventIds = request.getEventIDs();
        List<ProductText> productTexts = Collections.EMPTY_LIST;
        if (eventIds != null) {
            productTexts = new ArrayList<>(eventIds.size());
            for (String eventId : eventIds) {
                productTexts.add(new ProductText(request.getKey(),
                        request.getProductCategory(), request.getMode(),
                        request.getSegment(), eventId, request.getOfficeID(),
                        request.getInsertTime(), request.getValue()));
            }
        }
        return productTexts;
    }

    /**
     * Export records
     *
     * @param request
     *            Request containing the criteria of records to be exported
     * @param dao
     * @param response
     */
    private void exportRecords(ProductTextRequest request, CoreDao dao,
            ProductTextResponse response) {
        String filePath = request.getPath();
        if (filePath == null) {
            response.setExceptions(
                    new IllegalStateException("filePath is null"));
            return;
        }

        List<ProductText> text = retrieve(request, dao);

        try (OutputStream os = new FileOutputStream(filePath)) {
            SerializationUtil.transformToThriftUsingStream(text, os);
        } catch (IOException | SerializationException e) {
            statusHandler.error(e.getLocalizedMessage(), e);
            response.setExceptions(e);
        }
    }

    /**
     * Import records
     *
     * @param request
     *            Request containing the file path of DynamicSerialized records
     *            to be imported.
     * @param dao
     * @param response
     */
    private void importRecords(ProductTextRequest request, CoreDao dao,
            ProductTextResponse response) {
        String filePath = request.getPath();
        if (filePath == null) {
            response.setExceptions(
                    new IllegalStateException("filePath is null"));
            return;
        }

        List<ProductText> records = null;
        try (InputStream is = new FileInputStream(filePath)) {
            records = SerializationUtil.transformFromThrift(List.class, is);
            dao.persistAll(records);
        } catch (IOException | SerializationException e) {
            statusHandler.error(e.getLocalizedMessage(), e);
            response.setExceptions(e);
        }
    }

    /**
     * Retrieve records
     *
     * @param pData
     *            Request containing criteria for retrieval
     * @return
     */
    private List<ProductText> retrieve(ProductTextRequest request,
            CoreDao dao) {
        Criteria criteria = dao.getSessionFactory().openSession()
                .createCriteria(ProductText.class);
        if (request.getKey() != null) {
            criteria.add(Restrictions.eq("id.key", request.getKey()));
        }
        if (request.getProductCategory() != null) {
            criteria.add(Restrictions.eq("id.productCategory",
                    request.getProductCategory()));
        }
        if (request.getMode() != null) {
            criteria.add(Restrictions.eq("id.mode", request.getMode()));
        }
        if (request.getSegment() != null) {
            criteria.add(Restrictions.eq("id.segment", request.getSegment()));
        }
        if (request.getEventIDs() != null) {
            if (request.getEventIDs().isEmpty()) {
                /*
                 * Using Restrictions.isNull() instead of Restrictions.in()
                 * prevents an error in which Hibernate would generate incorrect
                 * SQL of: "... this_.eventID in ()". The issue is that the
                 * parenthesis require something between them.
                 */
                criteria.add(Restrictions.isNull("id.eventID"));
                statusHandler.warn("Event ID list is empty");
            } else {
                criteria.add(
                        Restrictions.in("id.eventID", request.getEventIDs()));
            }
        }
        if (request.getOfficeID() != null) {
            criteria.add(Restrictions.eq("id.officeID", request.getOfficeID()));
        }

        List<ProductText> text = criteria.list();
        return text;
    }

    private String prettyPrintRequest(ProductTextRequest request) {
        StringBuilder builder = new StringBuilder();
        builder.append(request.getKey());
        builder.append("/");
        builder.append(request.getEventIDs());
        builder.append("/");
        builder.append(request.getOfficeID());
        builder.append("/");
        builder.append(request.getProductCategory());
        builder.append("/");
        builder.append(request.getMode());
        builder.append("/");
        builder.append(request.getSegment());
        if (request.getValue() != null) {
            builder.append("/");
            builder.append(request.getInsertTime());
            builder.append("/");
            builder.append(request.getValue());
        }
        return builder.toString();
    }
}

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
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import com.raytheon.uf.common.hazards.productgen.editable.CustomTextId;
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
        statusHandler.info(request.getType().name() + " : "
                + prettyPrintKey(request.getProductText()) + " : "
                + request.getPath());
        switch (request.getType()) {
        case CREATE:
            try {
                dao.create(request.getProductText());
            } catch (RuntimeException e) {
                response.setExceptions(e);
            }
            break;
        case UPDATE:
            try {
                dao.update(request.getProductText());
            } catch (RuntimeException e) {
                response.setExceptions(e);
            }
            break;
        case DELETE:
            try {
                dao.delete(request.getProductText());
            } catch (RuntimeException e) {
                response.setExceptions(e);
            }
            break;
        case SAVE_OR_UPDATE:
            try {
                dao.saveOrUpdate(request.getProductText());
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
        ProductText pText = request.getProductText();
        Criteria criteria = dao.getSessionFactory().openSession()
                .createCriteria(ProductText.class);
        if (pText.getKey() != null) {
            criteria.add(Restrictions.eq("id.key", pText.getKey()));
        }
        if (pText.getProductCategory() != null) {
            criteria.add(Restrictions.eq("id.productCategory",
                    pText.getProductCategory()));
        }
        if (pText.getProductID() != null) {
            criteria.add(Restrictions.eq("id.productID", pText.getProductID()));
        }
        if (pText.getSegment() != null) {
            criteria.add(Restrictions.eq("id.segment", pText.getSegment()));
        }
        if (pText.getEventIDs() != null) {
            criteria.add(Restrictions.eq("id.eventIDs", pText.getEventIDs()));
        }
        if (pText.getOfficeID() != null) {
            criteria.add(Restrictions.eq("id.officeID", pText.getOfficeID()));
        }

        List<ProductText> text = criteria.list();
        return text;
    }

    private String prettyPrintKey(ProductText text) {
        StringBuilder builder = new StringBuilder();
        CustomTextId textId = text.getId();
        builder.append(textId.getKey());
        builder.append("/");
        builder.append(textId.getEventIDs());
        builder.append("/");
        builder.append(textId.getOfficeID());
        builder.append("/");
        builder.append(textId.getProductCategory());
        builder.append("/");
        builder.append(textId.getProductID());
        builder.append("/");
        builder.append(textId.getSegment());
        if (text.getValue() != null) {
            builder.append("/");
            builder.append(text.getValue());
        }
        return builder.toString();
    }
}

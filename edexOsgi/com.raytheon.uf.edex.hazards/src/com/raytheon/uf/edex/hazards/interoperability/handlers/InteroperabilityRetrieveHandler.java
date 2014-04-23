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
package com.raytheon.uf.edex.hazards.interoperability.handlers;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.io.Serializable;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import com.raytheon.uf.common.dataplugin.events.hazards.interoperability.IHazardsInteroperabilityRecord;
import com.raytheon.uf.common.dataplugin.events.hazards.interoperability.requests.RecordRetrieveRequest;
import com.raytheon.uf.common.dataplugin.events.hazards.interoperability.requests.RecordRetrieveResponse;
import com.raytheon.uf.common.serialization.comm.IRequestHandler;
import com.raytheon.uf.edex.database.dao.CoreDao;
import com.raytheon.uf.edex.database.dao.DaoConfig;

/**
 * Used to retrieve interoperability records by the specified criteria.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 2, 2014            bkowal     Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class InteroperabilityRetrieveHandler implements
        IRequestHandler<RecordRetrieveRequest> {

    private final CoreDao dao;

    /**
     * 
     */
    public InteroperabilityRetrieveHandler() {
        this.dao = new CoreDao(DaoConfig.DEFAULT);
    }

    @Override
    public Object handleRequest(RecordRetrieveRequest request) throws Exception {
        DetachedCriteria criteria = DetachedCriteria.forClass(request
                .getEntityClass());

        Iterator<String> fieldIterator = request.getParameters().keySet()
                .iterator();
        while (fieldIterator.hasNext()) {
            final String field = fieldIterator.next();
            final Serializable value = request.getParameters().get(field);

            criteria.add(Restrictions.eq(field, value));
        }

        List<?> results = this.dao.getHibernateTemplate().findByCriteria(
                criteria);
        if (results.isEmpty()) {
            return new RecordRetrieveResponse(null);
        }

        List<IHazardsInteroperabilityRecord> records = new ArrayList<>();
        for (Object object : results) {
            if (object instanceof IHazardsInteroperabilityRecord) {
                records.add((IHazardsInteroperabilityRecord) object);
            }
        }

        return new RecordRetrieveResponse(records);
    }
}
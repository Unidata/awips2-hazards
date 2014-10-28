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

import org.hibernate.Session;

import com.raytheon.uf.common.dataplugin.events.hazards.interoperability.IHazardsInteroperabilityRecord;
import com.raytheon.uf.common.dataplugin.events.hazards.interoperability.requests.RecordRetrievePKRequest;
import com.raytheon.uf.common.dataplugin.events.hazards.interoperability.requests.RecordRetrievePKResponse;
import com.raytheon.uf.common.serialization.comm.IRequestHandler;
import com.raytheon.uf.edex.database.dao.CoreDao;
import com.raytheon.uf.edex.database.dao.DaoConfig;

/**
 * Used to retrieve interoperability records by primary key.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 02, 2014            bkowal      Initial creation
 * Oct 21, 2014   5051     mpduff      Change to support Hibernate upgrade.
 * 10/28/2014   5051     bphillip   Change to support Hibernate upgrade
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class InteroperabilityPKRetrieveHandler implements
        IRequestHandler<RecordRetrievePKRequest> {

    private final CoreDao dao;

    /**
     * 
     */
    public InteroperabilityPKRetrieveHandler() {
        this.dao = new CoreDao(DaoConfig.DEFAULT);
    }

    @Override
    public Object handleRequest(RecordRetrievePKRequest request)
            throws Exception {
        Session session = null;
        Object record = null;
        try {
            session = this.dao.getSession();
            record = session.get(request.getEntityClass(), request.getKey());
        } finally {
            if (session != null) {
                session.close();
            }
        }
        if (record instanceof IHazardsInteroperabilityRecord == false) {
            /*
             * This scenario is extremely unlikely (theoretically impossible)
             * based on how Hibernate works. So, no action will be taken.
             */
            return new RecordRetrievePKResponse(null);
        }

        return new RecordRetrievePKResponse(
                (IHazardsInteroperabilityRecord) record);
    }
}
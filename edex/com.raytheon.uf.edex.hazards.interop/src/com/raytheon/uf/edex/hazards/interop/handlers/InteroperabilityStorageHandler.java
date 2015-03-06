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
package com.raytheon.uf.edex.hazards.interop.handlers;

import java.util.Date;

import com.raytheon.uf.common.dataplugin.events.hazards.interoperability.IHazardsInteroperabilityRecord;
import com.raytheon.uf.common.dataplugin.events.hazards.interoperability.requests.RecordStorageRequest;
import com.raytheon.uf.common.serialization.comm.IRequestHandler;
import com.raytheon.uf.edex.database.dao.CoreDao;
import com.raytheon.uf.edex.database.dao.DaoConfig;

/**
 * Used to store, update, remove the provided interoperability records.
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

public class InteroperabilityStorageHandler implements
        IRequestHandler<RecordStorageRequest> {

    private final CoreDao dao;

    public InteroperabilityStorageHandler() {
        this.dao = new CoreDao(DaoConfig.DEFAULT);
    }

    @Override
    public Object handleRequest(RecordStorageRequest request) throws Exception {
        switch (request.getRequestType()) {
        case STORE:
        case UPDATE:
            for (IHazardsInteroperabilityRecord record : request.getRecords()) {
                record.setCreationDate(new Date());

                this.dao.saveOrUpdate(record);
            }
            break;
        case DELETE:
            for (IHazardsInteroperabilityRecord record : request.getRecords()) {
                this.dao.delete(record);
            }
            break;
        }

        return null;
    }
}
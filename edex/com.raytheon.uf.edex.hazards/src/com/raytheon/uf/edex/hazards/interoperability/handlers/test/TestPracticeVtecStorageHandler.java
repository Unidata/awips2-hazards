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
package com.raytheon.uf.edex.hazards.interoperability.handlers.test;

import com.raytheon.uf.common.activetable.PracticeActiveTableRecord;
import com.raytheon.uf.common.dataplugin.events.hazards.interoperability.HazardInteroperabilityConstants.INTEROPERABILITY_TYPE;
import com.raytheon.uf.common.dataplugin.events.hazards.interoperability.HazardInteroperabilityRecordManager;
import com.raytheon.uf.common.dataplugin.events.hazards.interoperability.requests.test.TestPracticeVtecStorageRequest;
import com.raytheon.uf.common.dataplugin.events.hazards.interoperability.requests.test.TestVtecInteroperabilityRelation;
import com.raytheon.uf.common.serialization.comm.IRequestHandler;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.edex.activetable.ActiveTable;
import com.raytheon.uf.edex.database.dao.CoreDao;
import com.raytheon.uf.edex.database.dao.DaoConfig;

/**
 * TODO Add Description
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 11, 2014            jsanchez     Initial creation
 * 
 * </pre>
 * 
 * @author jsanchez
 * @version 1.0
 */

public class TestPracticeVtecStorageHandler implements
        IRequestHandler<TestPracticeVtecStorageRequest> {
    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(TestPracticeVtecStorageHandler.class);

    private static CoreDao practiceDao = new CoreDao(
            DaoConfig.forClass(PracticeActiveTableRecord.class));

    private static final String LOG_PREFIX_STMT = "TEST! TEST! TEST! ";

    @Override
    public Object handleRequest(TestPracticeVtecStorageRequest request)
            throws Exception {
        statusHandler.warn("Processing TestPracticeVtecStorageRequest ...");

        if (request.getRelations().isEmpty()) {
            return null;
        }

        /*
         * Clear any existing records before writing the new ones to mimic the
         * original JSON Vtec Storage.
         */
        ActiveTable.clearPracticeTable(request.getRelations().get(0)
                .getRecord().getXxxid());
        statusHandler.warn(LOG_PREFIX_STMT
                + "Purged PRACTICE vtec records for site: "
                + request.getRelations().get(0).getRecord().getXxxid() + "!");

        for (TestVtecInteroperabilityRelation relation : request.getRelations()) {
            practiceDao.create(relation.getRecord());
            statusHandler.warn(LOG_PREFIX_STMT
                    + "Successfully created vtec record: "
                    + relation.getRecord().toString());

            if (HazardInteroperabilityRecordManager
                    .storeRecord(HazardInteroperabilityRecordManager
                            .constructInteroperabilityRecord(relation
                                    .getRecord().getXxxid(), relation
                                    .getHazardType(), relation.getRecord()
                                    .getEtn(), relation.getEventID(),
                                    INTEROPERABILITY_TYPE.RIVERPRO))) {
                statusHandler
                        .warn(LOG_PREFIX_STMT
                                + "Successfully stored interoperability record for Hazard Event: "
                                + relation.getEventID() + "!");
            }
        }

        return null;
    }
}

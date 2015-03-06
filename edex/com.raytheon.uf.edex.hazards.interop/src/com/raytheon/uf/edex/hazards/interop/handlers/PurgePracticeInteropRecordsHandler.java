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

import java.util.Arrays;
import java.util.Collection;

import com.raytheon.uf.common.dataplugin.events.hazards.interoperability.requests.PurgePracticeInteropRecordsRequest;
import com.raytheon.uf.common.serialization.comm.IRequestHandler;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.edex.database.DataAccessLayerException;
import com.raytheon.uf.edex.database.dao.CoreDao;
import com.raytheon.uf.edex.database.dao.DaoConfig;

/**
 * Request handler for {@code PurgePracticeInteropRecordsRequest}. Deletes all
 * entries from the practice_hazards_interoperability and
 * practice_hazards_interoperability_gfe database tables when handling this
 * request to get the practice warning, hazards, activetable systems back to a
 * clean state.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Dec 08, 2014  #2826     dgilling     Initial creation
 * 
 * </pre>
 * 
 * @author dgilling
 * @version 1.0
 */

public final class PurgePracticeInteropRecordsHandler implements
        IRequestHandler<PurgePracticeInteropRecordsRequest> {

    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(PurgePracticeInteropRecordsHandler.class);

    private final CoreDao dao;

    private static final String DELETE_STATEMENT_FORMAT = "DELETE FROM %s;";

    private static final Collection<String> TABLES_TO_PURGE = Arrays.asList(
            "practice_hazards_interoperability",
            "practice_hazards_interoperability_gfe");

    public PurgePracticeInteropRecordsHandler() {
        this.dao = new CoreDao(DaoConfig.DEFAULT);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.common.serialization.comm.IRequestHandler#handleRequest
     * (com.raytheon.uf.common.serialization.comm.IServerRequest)
     */
    @Override
    public Boolean handleRequest(PurgePracticeInteropRecordsRequest request)
            throws Exception {
        Boolean retVal = Boolean.TRUE;

        for (String tableName : TABLES_TO_PURGE) {
            String deleteStatment = String.format(DELETE_STATEMENT_FORMAT,
                    tableName);

            try {
                dao.executeNativeSql(deleteStatment);
            } catch (DataAccessLayerException e) {
                String message = String.format("Failed to purge the %s table!",
                        tableName);
                statusHandler.error(message, e);
            }
        }

        if (retVal) {
            statusHandler
                    .info("Successfully cleared the hazard services interoperability tables.");
        }

        return retVal;
    }
}

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
package com.raytheon.uf.edex.hazards.database;

import org.apache.commons.lang.math.NumberUtils;

import com.raytheon.uf.common.dataplugin.events.hazards.interoperability.requests.PurgePracticeWarningRequest;
import com.raytheon.uf.common.serialization.comm.IRequestHandler;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.edex.database.DataAccessLayerException;
import com.raytheon.uf.edex.database.dao.CoreDao;
import com.raytheon.uf.edex.database.dao.DaoConfig;

/**
 * Truncates the practice warning table in the metadata database. Currently only
 * invoked during a hazard reset operation.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 8, 2014            jsanchez     Initial creation
 * 
 * </pre>
 * 
 * @author jsanchez
 * @version 1.0
 */

public class PurgePracticeWarningHandler implements
        IRequestHandler<PurgePracticeWarningRequest> {

    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(PurgePracticeWarningHandler.class);

    private final CoreDao dao;

    private static final String DELETE_STATEMENT = "DELETE FROM practicewarning;";

    public PurgePracticeWarningHandler() {
        this.dao = new CoreDao(DaoConfig.DEFAULT);
    }

    @Override
    public Object handleRequest(PurgePracticeWarningRequest request)
            throws Exception {
        Object result = null;
        try {
            result = this.dao.executeNativeSql(DELETE_STATEMENT);
        } catch (DataAccessLayerException e) {
            statusHandler.error("Failed to purge the practice warning table!",
                    e);
            return null;
        }

        int rowsDeleted = NumberUtils.toInt(result.toString(), -1);
        if (rowsDeleted >= 0) {
            statusHandler.info("Successfully removed " + rowsDeleted
                    + " records from the practice warning table.");
        }

        return null;
    }
}

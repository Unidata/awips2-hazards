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
package com.raytheon.uf.edex.recommenders.handler;

import com.raytheon.uf.common.dataplugin.events.EventSet;
import com.raytheon.uf.common.dataplugin.events.IEvent;
import com.raytheon.uf.common.recommenders.requests.ExecuteRecommenderRequest;
import com.raytheon.uf.common.serialization.comm.IRequestHandler;
import com.raytheon.uf.edex.recommenders.EDEXRecommenderEngine;

/**
 * Handles requests made to EDEX to run recommenders
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 7, 2013            mnash        Initial creation
 * Mar 31, 2016 8837      Robert.Blum  Changes for Service Backup.
 * 
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */

public class RecommenderHandler implements
        IRequestHandler<ExecuteRecommenderRequest> {

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.common.serialization.comm.IRequestHandler#handleRequest
     * (com.raytheon.uf.common.serialization.comm.IServerRequest)
     */
    @Override
    public Object handleRequest(final ExecuteRecommenderRequest request)
            throws Exception {
        EDEXRecommenderEngine engine = new EDEXRecommenderEngine();
        engine.setSite(request.getSite());
        EventSet<IEvent> eventSet = new EventSet<IEvent>();
        eventSet.addAttribute("siteID", request.getSite());
        eventSet.addAttribute("timeRange", request.getTimeRange());
        return engine.runRecommender(request.getRecommenderName(), eventSet,
                null, null);
    }
}

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

import java.util.List;

import com.raytheon.uf.common.dataplugin.events.EventSet;
import com.raytheon.uf.common.dataplugin.events.IEvent;
import com.raytheon.uf.common.python.concurrent.IPythonJobListener;
import com.raytheon.uf.common.recommenders.AbstractRecommenderEngine;
import com.raytheon.uf.common.recommenders.requests.ExecuteRecommenderRequest;
import com.raytheon.uf.common.serialization.comm.IRequestHandler;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.edex.recommenders.EDEXRecommenderEngine;
import com.raytheon.uf.edex.recommenders.EDEXRecommenderScriptManager;

/**
 * Handles requests made to EDEX to run recommenders
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 7, 2013            mnash     Initial creation
 * 
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */

public class RecommenderHandler implements
        IRequestHandler<ExecuteRecommenderRequest> {

    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(RecommenderHandler.class);

    private List<IEvent> events;

    private volatile boolean running;

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
        AbstractRecommenderEngine<EDEXRecommenderScriptManager> engine = new EDEXRecommenderEngine();
        IPythonJobListener<List<IEvent>> listener = new IPythonJobListener<List<IEvent>>() {

            @Override
            public void jobFinished(List<IEvent> result) {
                events = result;
                running = false;
            }

            @Override
            public void jobFailed(Throwable e) {
                statusHandler.handle(Priority.ERROR,
                        "Recommender " + request.getRecommenderName()
                                + " failed to successfully run.");
                running = false;
            }
        };
        EventSet<IEvent> eventSet = new EventSet<IEvent>();
        eventSet.addAttribute("site", request.getSite());
        // TODO, we need to return good values here.
        engine.runExecuteRecommender(request.getRecommenderName(), eventSet,
                null, null, listener);
        while (running) {
            // wait
        }
        return events;
    }
}

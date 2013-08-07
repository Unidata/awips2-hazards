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
package com.raytheon.uf.common.hazards.gfe;

import java.util.ArrayList;
import java.util.List;

import com.raytheon.uf.common.actionregistry.IActionable;
import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.HazardEventManager;
import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.HazardEventManager.Mode;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.recommenders.requests.ExecuteRecommenderRequest;
import com.raytheon.uf.common.serialization.comm.RequestRouter;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;

/**
 * Action triggered by the registry that runs the GFE hazards grid recommender.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 22, 2013            mnash     Initial creation
 * 
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */

public class GFEActionable implements IActionable {

    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(GFEActionable.class);

    private static final String GFE_RECOMMENDER = "GFEHazardsGridRecommender";

    /**
     * 
     */
    public GFEActionable() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.common.actionregistry.IActionable#handleAction(java.util
     * .Map)
     */
    @Override
    public void handleAction(Object... objects) {
        Object returnValue = null;
        try {
            ExecuteRecommenderRequest request = new ExecuteRecommenderRequest(
                    GFE_RECOMMENDER);
            // TODO set this better
            request.setSite("OAX");
            returnValue = RequestRouter.route(request);
        } catch (Exception e) {
            statusHandler.handle(Priority.PROBLEM, e.getLocalizedMessage(), e);
        }
        // TODO, convert to whatever needs to come back, why do we not get any
        // back?
        HazardEventManager manager = new HazardEventManager(Mode.PRACTICE);
        List<IHazardEvent> events = new ArrayList<IHazardEvent>();
        for (IHazardEvent event : (List<IHazardEvent>) returnValue) {
            events.add(manager.createEvent(event));
        }
        if (events != null) {
            manager.storeEvents(events);
        }
    }
}

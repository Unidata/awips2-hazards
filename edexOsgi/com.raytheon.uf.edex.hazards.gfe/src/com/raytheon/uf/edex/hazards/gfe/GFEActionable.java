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
package com.raytheon.uf.edex.hazards.gfe;

import java.util.ArrayList;
import java.util.List;

import com.raytheon.uf.common.actionregistry.IActionable;
import com.raytheon.uf.common.dataplugin.events.EventSet;
import com.raytheon.uf.common.dataplugin.events.IEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.HazardEventManager;
import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.HazardEventManager.Mode;
import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardEventUtilities;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.requests.HazardEventIdRequest;
import com.raytheon.uf.common.dataplugin.warning.AbstractWarningRecord;
import com.raytheon.uf.common.dataplugin.warning.PracticeWarningRecord;
import com.raytheon.uf.common.serialization.comm.RequestRouter;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.edex.recommenders.EDEXRecommenderEngine;

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

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.common.actionregistry.IActionable#handleAction(java.util
     * .Map)
     */
    @Override
    public void handleAction(Object... objects) {
        List<IHazardEvent> events = new ArrayList<IHazardEvent>();
        EDEXRecommenderEngine engine = new EDEXRecommenderEngine();
        for (Object ob : objects) {
            AbstractWarningRecord rec = (AbstractWarningRecord) ob;
            if (GridValidator.needsGridConversion(rec.getPhensig()) == false
                    && rec.getGeometry() != null) {
                continue;
            }
            Mode mode = null;
            if (rec instanceof PracticeWarningRecord) {
                mode = Mode.PRACTICE;
            } else {
                mode = Mode.OPERATIONAL;
            }

            Object returnValue = null;
            try {
                EventSet<IEvent> eventSet = new EventSet<IEvent>();
                eventSet.addAttribute(HazardConstants.SITE_ID, rec.getXxxid());
                eventSet.addAttribute(HazardConstants.HAZARD_EVENT_START_TIME,
                        rec.getStartTime().getTime());
                eventSet.addAttribute(HazardConstants.HAZARD_EVENT_END_TIME,
                        rec.getEndTime().getTime());
                eventSet.addAttribute(HazardConstants.PHEN_SIG, rec.getPhensig());
                returnValue = engine.runRecommender(GFE_RECOMMENDER, eventSet,
                        null, null);
            } catch (Exception e) {
                statusHandler.handle(Priority.PROBLEM, e.getLocalizedMessage(),
                        e);
            }

            HazardEventManager manager = new HazardEventManager(Mode.PRACTICE);
            for (IHazardEvent event : (EventSet<IHazardEvent>) returnValue) {
                event.setState(HazardEventUtilities.stateBasedOnAction(rec
                        .getAct()));
                event.setEventID(generateEventID(event.getSiteID()));
                event.addHazardAttribute(HazardConstants.EXPIRATION_TIME, rec
                        .getPurgeTime().getTime().getTime());
                event.addHazardAttribute("etns", "[" + rec.getEtn() + "]");
                event.setIssueTime(rec.getIssueTime().getTime());
                if (HazardEventUtilities.isDuplicate(manager, event) == false) {
                    events.add(manager.createEvent(event));
                }
            }
            manager.storeEvents(events);
        }
    }

    private String generateEventID(String site) {
        HazardEventIdRequest request = new HazardEventIdRequest();
        request.setSiteId(site);
        String value = "";
        try {
            value = RequestRouter.route(request).toString();
        } catch (Exception e) {
            throw new RuntimeException(
                    "Unable to make request for hazard event id", e);
        }
        return value;
    }
}

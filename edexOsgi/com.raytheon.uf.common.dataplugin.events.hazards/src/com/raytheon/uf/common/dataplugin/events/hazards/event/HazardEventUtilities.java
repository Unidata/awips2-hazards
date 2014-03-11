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
package com.raytheon.uf.common.dataplugin.events.hazards.event;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardState;
import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.HazardQueryBuilder;
import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.IHazardEventManager;
import com.raytheon.uf.common.dataplugin.events.hazards.event.collections.HazardHistoryList;
import com.raytheon.uf.common.dataplugin.events.hazards.requests.HazardEventIdRequest;
import com.raytheon.uf.common.serialization.comm.RequestRouter;

/**
 * Misc functionality for reformatting attributes for hazard events.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 24, 2013 1257       bsteffen    Initial creation
 * Nov 14, 2013 1472       bkowal      Renamed hazard subtype to subType
 * Oct 22, 2013 1463       blawrenc   Added methods to retrieve
 *                                    map geometries which 
 *                                    intersect hazard geometries.
 * Jan 14, 2014 2755       bkowal      Created a utility method for
 *                                     generating new Event IDs
 * Feb 02, 2014 2536       blawrenc   Moved geometry classes to a viz side class.
 * Mar 03, 2014 3034       bkowal     Moved common actions into separate methods
 * 
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */

public class HazardEventUtilities {
    public static String getHazardType(IHazardEvent event) {
        return getHazardType(event.getPhenomenon(), event.getSignificance(),
                event.getSubType());
    }

    public static String getHazardType(String phen, String sig, String subType) {
        if (phen == null || sig == null) {
            return null;
        }
        StringBuilder str = new StringBuilder();
        str.append(phen);
        str.append('.');
        str.append(sig);
        if (subType != null) {
            str.append('.');
            str.append(subType);
        }
        return str.toString();
    }

    public static void populateEventForHazardType(IHazardEvent event,
            String hazardType) {
        int endPhen = hazardType.indexOf('.');
        event.setPhenomenon(hazardType.substring(0, endPhen));
        int endSig = hazardType.indexOf('.', endPhen + 1);
        if (endSig > 0) {
            event.setSignificance(hazardType.substring(endPhen + 1, endSig));
            event.setSubType(hazardType.substring(endSig + 1));
        } else {
            event.setSignificance(hazardType.substring(endPhen + 1));
        }
    }

    /**
     * Returns a {@link HazardState} based on the VTEC action code.
     * 
     * @param action
     * @return
     */
    public static HazardState stateBasedOnAction(String action) {
        if ("CAN".equals(action) || "EXP".equals(action)) {
            return HazardState.ENDED;
        } else {
            return HazardState.ISSUED;
        }
    }

    public static List<String> parseEtns(String etns) {
        List<String> parsed = new ArrayList<String>();
        if (etns.contains("[")) {
            etns = etns.replaceAll("\\[|\\]", "");
            String[] split = etns.split(",");
            parsed = Arrays.asList(split);
        } else if (etns.isEmpty() == false) {
            parsed.add(etns);
        }
        return parsed;
    }

    public static Map<String, HazardHistoryList> queryForEvents(
            IHazardEventManager manager, IHazardEvent event) {
        HazardQueryBuilder builder = new HazardQueryBuilder();
        builder.addKey(HazardConstants.SITE_ID, event.getSiteID());
        builder.addKey(HazardConstants.PHENOMENON, event.getPhenomenon());
        builder.addKey(HazardConstants.SIGNIFICANCE, event.getSignificance());
        return manager.getEventsByFilter(builder.getQuery());
    }

    public static boolean isDuplicate(IHazardEventManager manager,
            IHazardEvent event) {
        Map<String, HazardHistoryList> hazards = queryForEvents(manager,
                event);
        boolean isDup = false;
        for (HazardHistoryList list : hazards.values()) {
            Iterator<IHazardEvent> iter = list.iterator();
            while (iter.hasNext()) {
                IHazardEvent ev = iter.next();
                isDup = HazardEventUtilities.checkDifferentEvents(ev, event);
                if (isDup) {
                    break;
                }
            }
            if (isDup) {
                break;
            }
        }
        return isDup;
    }

    public static String determineEtn(String site, String action, String etn,
            IHazardEventManager manager) throws Exception {
        // make a request for the hazard event id from the cluster task
        // table
        HazardEventIdRequest request = new HazardEventIdRequest();
        request.setSiteId(site);
        String value = "";
        boolean createNew = false;
        if (HazardConstants.NEW_ACTION.equals(action) == false) {
            Map<String, HazardHistoryList> map = manager.getBySiteID(site);
            for (Entry<String, HazardHistoryList> entry : map.entrySet()) {
                HazardHistoryList list = entry.getValue();
                for (IHazardEvent ev : list) {
                    List<String> hazEtns = HazardEventUtilities
                            .parseEtns(String.valueOf(ev
                                    .getHazardAttribute(HazardConstants.ETNS)));
                    List<String> recEtn = HazardEventUtilities.parseEtns(etn);
                    if (compareEtns(hazEtns, recEtn)) {
                        value = ev.getEventID();
                        break;
                    }
                }
            }
            if ("".equals(value)) {
                createNew = true;
            }
        }
        if ("NEW".equals(action) || createNew) {
            value = generateEventID(site);
        }
        return value;
    }

    public static String generateEventID(String site) throws Exception {
        HazardEventIdRequest request = new HazardEventIdRequest();
        request.setSiteId(site);
        String value = "";
        try {
            value = RequestRouter.route(request).toString();
        } catch (Exception e) {
            throw new Exception("Unable to make request for hazard event id", e);
        }
        return value;
    }

    public static boolean checkDifferentSiteOrType(IHazardEvent event1,
            IHazardEvent event2) {
        if (event1.getSiteID().equals(event2.getSiteID()) == false) {
            return true;
        }
        if (event1.getPhenomenon().equals(event2.getPhenomenon()) == false) {
            return true;
        }
        if (event1.getSignificance().equals(event2.getSignificance()) == false) {
            return true;
        }
        // TODO, this is necessary when we use the mode to issue products later
        // on
        // if (event1.getHazardMode().equals(event2.getHazardMode()) == false) {
        // return true;
        // }

        return false;
    }

    /**
     * Determines if events are the same or are different.
     * 
     * @param event1
     * @param event2
     * @return
     */
    @SuppressWarnings("unchecked")
    public static boolean checkDifferentEvents(IHazardEvent event1,
            IHazardEvent event2) {
        if (checkDifferentSiteOrType(event1, event2)) {
            return true;
        }

        Object obj1 = event1.getHazardAttribute(HazardConstants.ETNS);
        List<String> etns1 = null;
        List<String> etns2 = null;
        /*
         * Verify that the hazard event actually has ETNs associated with it.
         */
        if (obj1 != null) {
            // this will become OBE by refactor work, right now we have cases
            // where
            // it is a string and some where it is a list
            if (obj1 instanceof String) {
                etns1 = HazardEventUtilities.parseEtns((String) event1
                        .getHazardAttribute(HazardConstants.ETNS));
            } else {
                etns1 = new ArrayList<String>();
                List<Integer> list = (List<Integer>) obj1;
                for (Integer in : list) {
                    etns1.add(String.valueOf(in));
                }
            }

            Object obj2 = event2.getHazardAttribute(HazardConstants.ETNS);
            if (obj2 instanceof String) {
                etns2 = HazardEventUtilities.parseEtns((String) event2
                        .getHazardAttribute(HazardConstants.ETNS));
            } else {
                etns2 = new ArrayList<String>();
                List<Integer> list = (List<Integer>) obj2;
                for (Integer in : list) {
                    etns2.add(String.valueOf(in));
                }
            }
            if (compareEtns(etns1, etns2) == false) {
                return true;
            }
        }

        return false;
    }

    /**
     * Comparing if any of the ETNs of the first list match any of the second
     * list. The lists can be different lengths depending on the code that hits
     * this.
     * 
     * @param etns1
     * @param etns2
     * @return
     */
    private static boolean compareEtns(List<String> etns1, List<String> etns2) {
        for (String etn1 : etns1) {
            for (String etn2 : etns2) {
                if (Integer.valueOf(etn1).equals(Integer.valueOf(etn2))) {
                    return false;
                }
            }
        }
        return true;
    }

}

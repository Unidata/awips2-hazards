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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardStatus;
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
 * Apr 08, 2014 3357       bkowal     Removed unused methods.
 * May 14, 2014 2925       Chris.Golden Cleaned up code and added comments for
 *                                      hazard full-type/type conversion methods.
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */

public class HazardEventUtilities {

    // Private Static Constants

    /**
     * Empty phenomenon-significance-subtype, returned by
     * {@link #getHazardPhenSigSubType(String)} if there is no type.
     */
    private static final String[] EMPTY_PHEN_SIG_SUBTYPE = new String[3];

    // Public Static Methods

    /**
     * Get a full type from the specified hazard event.
     * 
     * @param event
     *            Hazard event.
     * @return Full type as a string, with each component separated from the
     *         next by a period (.), or an empty string if there is no type.
     */
    public static String getHazardType(IHazardEvent event) {
        return getHazardType(event.getPhenomenon(), event.getSignificance(),
                event.getSubType());
    }

    /**
     * Get a full type from the specified type components.
     * 
     * @param phen
     *            Phenomenon, or <code>null</code> if there is no type.
     * @param sig
     *            Significance, or <code>null</code> if there is no type.
     * @param subType
     *            Subtype, or <code>null</code> if there is no type, or the type
     *            does not include a subtype.
     * @return Full type as a string, with each component separated from the
     *         next by a period (.), or an empty string if there is no type.
     */
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

    /**
     * Get the type components (in the order phenomenon, significance, and
     * subtype, or if there is no subtype, <code>null</code>) for the specified
     * type description.
     * 
     * @param type
     *            Type description; this must consist of a string consisting of
     *            a type substring containing no spaces, optionally followed by
     *            a space ( ) and anything after that (which is ignored). The
     *            type substring must itself consist of either two or three
     *            components, each separated from the next by periods (.); the
     *            components must be in the order phenomenon, significance, and
     *            (if included) subtype. For example, the string
     *            "FA.W (Warning)" would be valid; everything after the space
     *            would be ignored, and the "FA" would be parsed as the
     *            phenomenon, while the "W" is parsed as the significance, and
     *            subtype would be taken to be <code>null</code>. Likewise
     *            "FF.W.NonConvective (Warning)" would be parsed as having a
     *            phenomenon of "FF", a significance of "W", and a subtype of
     *            "NonConvective", again with the rest being ignored.
     * @return Array of three elements, the first being the phenomenon, the
     *         second the significance, and the third being either the subtype,
     *         if applicable, or <code>null</code>.
     */
    public static String[] getHazardPhenSigSubType(String type) {
        if (!type.isEmpty()) {
            String[] components = type.split(" ")[0].split("\\.");
            if (components.length == 3) {
                return components;
            }
            String[] phenSigSubType = new String[3];
            for (int j = 0; j < phenSigSubType.length; j++) {
                phenSigSubType[j] = (j < components.length ? components[j]
                        : null);
            }
            return phenSigSubType;
        }
        return EMPTY_PHEN_SIG_SUBTYPE;
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
     * Returns a {@link HazardStatus} based on the VTEC action code.
     * 
     * @param action
     * @return
     */
    public static HazardStatus stateBasedOnAction(String action) {
        if ("CAN".equals(action) || "EXP".equals(action)) {
            return HazardStatus.ENDED;
        } else {
            return HazardStatus.ISSUED;
        }
    }

    public static List<String> parseEtns(String etns) {
        List<String> parsed = new ArrayList<String>();
        if (etns != null && etns.isEmpty() == false) {
            if (etns.contains("[")) {
                etns = etns.replaceAll("\\[|\\]", "");
                String[] split = etns.split(",");
                parsed = Arrays.asList(split);
            } else if (etns.isEmpty() == false) {
                parsed.add(etns);
            }
        }
        return parsed;
    }

    public static String determineEtn(String site, String action, String etn,
            IHazardEventManager manager) throws Exception {
        // make a request for the hazard event id from the cluster task
        // table
        HazardEventIdRequest request = new HazardEventIdRequest();
        request.setPractice(true);
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
            value = generateEventID(site, true);
        }
        return value;
    }

    public static String generateEventID(String site, boolean practice)
            throws Exception {
        HazardEventIdRequest request = new HazardEventIdRequest();
        request.setPractice(practice);
        request.setSiteId(site);
        String value = "";
        try {
            value = RequestRouter.route(request).toString();
        } catch (Exception e) {
            throw new Exception("Unable to make request for hazard event id", e);
        }
        return value;
    }

    // Private Static Methods.

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
            if (etn1 != null && etn1.isEmpty() == false) {
                for (String etn2 : etns2) {
                    if (etn2 != null && etn2.isEmpty() == false) {
                        if (Integer.valueOf(etn1).equals(Integer.valueOf(etn2))) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

}

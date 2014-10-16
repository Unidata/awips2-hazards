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
package com.raytheon.uf.edex.hazards.interoperability.handlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardStatus;
import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.HazardEventManager;
import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.HazardEventManager.Mode;
import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardEventUtilities;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.edex.hazards.interoperability.util.InteroperabilityUtil;

/**
 * Abstract class to hold common methods for interoperabilty handlers.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 18, 2014            jsanchez     Initial creation
 * 
 * </pre>
 * 
 * @author jsanchez
 * @version 1.0
 */

public abstract class AbstractInteroperabilityHandler {

    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(AbstractInteroperabilityHandler.class);

    protected static final String ETN_DICT_FIELD = "etn";

    protected static final String SITE_ID_DICT_FIELD = "xxxid";

    protected static final String SEGTEXT_DICT_FIELD = "segText";

    protected static final String KEY_DICT_FIELD = "key";

    protected static final String KEY_HDLN_FIELD = "hdln";

    protected static final String KEY_HVTECSTR = "hvtecstr";

    protected static final String VTECSTR_FIELD = "vtecstr";

    protected static final String SIG_FIELD = "sig";

    protected static final String PHEN_FIELD = "phen";

    protected static final String OVERVIEW_FIELD = "overviewText";

    protected static final String HEADLINE_FIELD = "hdln";

    protected static final String PHENSIG_FIELD = "phensig";

    protected static final String ACTION_FIELD = "act";

    protected static final String SEG_FIELD = "seg";

    protected static final String START_TIME_FIELD = "startTime";

    protected static final String END_TIME_FIELD = "endTime";

    protected static final String UFN_FIELD = "ufn";

    protected static final String OFFICEID_FIELD = "officeid";

    protected static final String PURGE_TIME_FIELD = "purgeTime";

    protected static final String ISSUE_TIME_FIELD = "issueTime";

    protected static final String PIL_FIELD = "pil";

    protected static final String PRODUCT_CLASS_FIELD = "productClass";

    protected static final String ID_FIELD = "id";

    protected static final String RAW_MESSAGE_DICT_FIELD = "rawMessage";

    protected static final String RAW_MESSAGE_FIELD = "rawmessage";

    protected static final String COUNTY_HEADER_FIELD = "countyheader";

    protected static final String WMO_ID_FIELD = "wmoid";

    protected static final String FLOOD_CREST_FIELD = "floodCrest";

    protected static final String FLOOD_BEGIN_FIELD = "floodBegin";

    protected static final String FLOOD_RECORD_STATUS_FIELD = "floodRecordStatus";

    protected static final String FLOOD_RECORD_STATUS_DICT_FIELD = "floodrecordstatus";

    protected static final String FLOOD_SEVERITY_FIELD = "floodSeverity";

    protected static final String FLOOD_SEVERITY_DICT_FIELD = "floodseverity";

    protected static final String GEOM_FIELD = "geometry";

    protected static final String IMMEDIATE_CAUSE_FIELD = "immediateCause";

    protected static final String LOC_FIELD = "loc";

    protected static final String LOCATION_ID_FIELD = "locationID";

    protected static final String LOCATION_ID_DICT_FIELD = "locatinoId";

    protected static final String MOTION_DIRECTION_FIELD = "motdir";

    protected static final String MOTION_SPEED_FIELD = "motspd";

    protected static final String UGC_ZONE_LIST_FIELD = "ugcZoneList";

    public AbstractInteroperabilityHandler() {

    }

    protected abstract List<Map<String, Object>> retrieveRecords(String siteID,
            String phensig, boolean practice) throws Exception;

    protected abstract Map<String, String> getHeadlineLookUpMap();

    /*
     * Used to create a clone of an active table dict when there are multiple
     * hazard ids associated with the dict etn.
     */
    private Map<String, Object> cloneDict(Map<String, Object> dict)
            throws Exception {
        Map<String, Object> dictClone = new HashMap<>();

        dictClone.putAll(dict);

        return dictClone;
    }

    protected List<Map<String, Object>> validateRecords(
            List<Map<String, Object>> dictList, boolean practice)
            throws Exception {
        List<Map<String, Object>> dictListResult = new ArrayList<>();
        /*
         * Instantiate a Hazard Event Manager.
         */
        Mode mode = practice ? Mode.PRACTICE : Mode.OPERATIONAL;
        HazardEventManager manager = new HazardEventManager(mode);
        for (Map<String, Object> dict : dictList) {
            /*
             * skip vtec records that are no longer active.
             */
            final String etn = InteroperabilityUtil.padEtnString(dict.get(
                    ETN_DICT_FIELD).toString());
            if (dict.containsKey(SITE_ID_DICT_FIELD) == false) {
                statusHandler.warn("Site ID is not present in warning record: "
                        + dict.toString() + "; skipping ...");
                continue;
            }
            final String siteID = dict.get(SITE_ID_DICT_FIELD).toString();

            List<IHazardEvent> events = InteroperabilityUtil
                    .queryInteroperabilityByETNForHazards(manager, siteID,
                            null, etn, null);
            if (events == null || events.isEmpty()) {
                continue;
            }

            for (IHazardEvent event : events) {
                /*
                 * skip ended events.
                 */
                if (event.getStatus() == HazardStatus.ENDED) {
                    continue;
                }

                final String phenSig = event.getPhenomenon() + "."
                        + event.getSignificance();
                if (dict.containsKey(HazardConstants.PHEN_SIG) == false) {
                    statusHandler
                            .warn("PhenSig is not present in warning record: "
                                    + dict.toString() + "; skipping ...");
                    continue;
                }
                if (phenSig.equals(dict.get(HazardConstants.PHEN_SIG)) == false) {
                    /*
                     * only include hazards that have the same phensig.
                     */
                    continue;
                }

                Map<String, Object> recordDictClone = cloneDict(dict);

                recordDictClone.put(HazardConstants.HAZARD_EVENT_IDENTIFIER,
                        event.getEventID());
                final String hazardType = HazardEventUtilities
                        .getHazardType(event);
                recordDictClone.put(KEY_DICT_FIELD, hazardType);
                recordDictClone.put(HazardConstants.HAZARD_EVENT_SUB_TYPE, "");
                if (event.getSubType() != null) {
                    recordDictClone.put(HazardConstants.HAZARD_EVENT_SUB_TYPE,
                            event.getSubType());
                }
                recordDictClone.put(KEY_HVTECSTR,
                        recordDictClone.get(SEGTEXT_DICT_FIELD));
                if (recordDictClone.containsKey(KEY_HDLN_FIELD) == false
                        || recordDictClone.get(KEY_HDLN_FIELD).toString()
                                .isEmpty()) {
                    Map<String, String> headlineLookupMap = getHeadlineLookUpMap();
                    if (headlineLookupMap != null) {
                        String headline = headlineLookupMap.get(hazardType);
                        if (headline == null) {
                            final String message = "Unable to determine headline associated with hazard:"
                                    + hazardType;
                            throw new Exception(message);
                        }
                        recordDictClone.put(KEY_HDLN_FIELD, headline);
                    }
                }

                dictListResult.add(recordDictClone);
            }
        }
        statusHandler.info("Found " + dictListResult.size()
                + " active table dict(s) for site.");
        return dictListResult;
    }
}

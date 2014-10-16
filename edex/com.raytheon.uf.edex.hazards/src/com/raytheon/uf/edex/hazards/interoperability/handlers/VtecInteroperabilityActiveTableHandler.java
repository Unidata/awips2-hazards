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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.raytheon.uf.common.activetable.ActiveTableMode;
import com.raytheon.uf.common.activetable.GetActiveTableDictRequest;
import com.raytheon.uf.common.activetable.GetActiveTableDictResponse;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardStatus;
import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.HazardEventManager;
import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.HazardEventManager.Mode;
import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardEventUtilities;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.interoperability.requests.VtecInteroperabilityActiveTableRequest;
import com.raytheon.uf.common.dataplugin.events.hazards.interoperability.requests.VtecInteroperabilityActiveTableResponse;
import com.raytheon.uf.common.hazards.configuration.ConfigLoader;
import com.raytheon.uf.common.hazards.configuration.HazardsConfigurationConstants;
import com.raytheon.uf.common.hazards.configuration.types.HazardTypeEntry;
import com.raytheon.uf.common.hazards.configuration.types.HazardTypes;
import com.raytheon.uf.common.localization.IPathManager;
import com.raytheon.uf.common.localization.LocalizationFile;
import com.raytheon.uf.common.localization.PathManagerFactory;
import com.raytheon.uf.common.serialization.comm.IRequestHandler;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.edex.activetable.GetActiveTableDictHandler;
import com.raytheon.uf.edex.hazards.interoperability.util.InteroperabilityUtil;

/**
 * Handles requests for vtec record to hazard event mappings for a specified
 * site.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 8, 2014  2826       jsanchez     Initial creation
 * 
 * </pre>
 * 
 * @author jsanchez
 * @version 1.0
 */

public class VtecInteroperabilityActiveTableHandler implements
        IRequestHandler<VtecInteroperabilityActiveTableRequest> {

    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(VtecInteroperabilityActiveTableHandler.class);

    private static final String ETN_DICT_FIELD = "etn";

    private static final String SITE_ID_DICT_FIELD = "xxxid";

    private static final String SEGTEXT_DICT_FIELD = "segText";

    private static final String KEY_DICT_FIELD = "key";

    private static final String KEY_HDLN_FIELD = "hdln";

    private static final String KEY_HVTECSTR = "hvtecstr";

    private Map<String, String> headlineLookupMap;

    public VtecInteroperabilityActiveTableHandler() {
        statusHandler.info("Starting initialization ...");

        this.headlineLookupMap = new HashMap<>();

        /*
         * TODO: if this becomes a permanent need, re-factor.
         */
        IPathManager pm = PathManagerFactory.getPathManager();
        LocalizationFile file = pm
                .getStaticLocalizationFile(HazardsConfigurationConstants.HAZARD_TYPES_PY);
        ConfigLoader<HazardTypes> hazardTypesConfigLoader = new ConfigLoader<HazardTypes>(
                file, HazardTypes.class);
        HazardTypes hazardTypes = hazardTypesConfigLoader.getConfig();

        Iterator<String> hazardTypesIterator = hazardTypes.keySet().iterator();
        while (hazardTypesIterator.hasNext()) {
            final String hazardType = hazardTypesIterator.next();
            HazardTypeEntry entry = hazardTypes.get(hazardType);
            this.headlineLookupMap.put(hazardType, entry.getHeadline());
        }

        statusHandler.info("Initialization Complete!");
    }

    @Override
    public VtecInteroperabilityActiveTableResponse handleRequest(
            VtecInteroperabilityActiveTableRequest request) throws Exception {
        VtecInteroperabilityActiveTableResponse response = new VtecInteroperabilityActiveTableResponse();
        response.setMode(this.getActiveTableMode(request.isPractice()));

        /*
         * First, retrieve the Active Table Records.
         */
        List<Map<String, Object>> activeTableRecordsDictList = null;
        try {
            activeTableRecordsDictList = this.retrieveActiveTableRecords(
                    request.getSiteID(), request.isPractice());
        } catch (Exception e) {
            statusHandler.handle(Priority.PROBLEM, e.getLocalizedMessage(), e);

            response.setSuccess(false);
            response.setExceptionText("Failed to retrieve the active table records: "
                    + e.getLocalizedMessage() + "!");
            return response;
        }
        List<Map<String, Object>> dictListResult = new ArrayList<>();

        if (activeTableRecordsDictList == null
                || activeTableRecordsDictList.isEmpty()) {
            response.setActiveTable(dictListResult);
            statusHandler.info("No active table records were found for site: "
                    + request.getSiteID());
            return response;
        }

        statusHandler.info("Found " + activeTableRecordsDictList.size()
                + " Active Table Records.");

        /*
         * Instantiate a Hazard Event Manager.
         */
        Mode mode = request.isPractice() ? Mode.PRACTICE : Mode.OPERATIONAL;
        HazardEventManager manager = new HazardEventManager(mode);

        for (Map<String, Object> recordDict : activeTableRecordsDictList) {
            /*
             * skip vtec records that are no longer active.
             */
            final String etn = InteroperabilityUtil.padEtnString(recordDict
                    .get(ETN_DICT_FIELD).toString());
            if (recordDict.containsKey(SITE_ID_DICT_FIELD) == false) {
                statusHandler
                        .warn("Site ID is not present in active table record: "
                                + recordDict.toString() + "; skipping ...");
                continue;
            }
            final String siteID = recordDict.get(SITE_ID_DICT_FIELD).toString();

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
                if (recordDict.containsKey(HazardConstants.PHEN_SIG) == false) {
                    statusHandler
                            .warn("PhenSig is not present in active table record: "
                                    + recordDict.toString() + "; skipping ...");
                    continue;
                }
                if (phenSig.equals(recordDict.get(HazardConstants.PHEN_SIG)) == false) {
                    /*
                     * only include hazards that have the same phensig.
                     */
                    continue;
                }

                Map<String, Object> recordDictClone = this
                        .cloneActiveTableDict(recordDict);

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
                    String headline = this.headlineLookupMap.get(hazardType);
                    if (headline == null) {
                        final String message = "Unable to determine headline associated with hazard:"
                                + hazardType;

                        statusHandler.error(message);
                        response.setSuccess(false);
                        response.setExceptionText(message);
                        return response;
                    }
                    recordDictClone.put(KEY_HDLN_FIELD, headline);
                }

                dictListResult.add(recordDictClone);
            }
        }

        statusHandler
                .info("Found " + dictListResult.size()
                        + " active table dict(s) for site "
                        + request.getSiteID() + ".");
        response.setActiveTable(dictListResult);
        return response;
    }

    private List<Map<String, Object>> retrieveActiveTableRecords(String siteID,
            boolean practiceMode) throws Exception {
        GetActiveTableDictRequest request = new GetActiveTableDictRequest();
        request.setRequestedSiteId(siteID);
        request.setMode(this.getActiveTableMode(practiceMode));

        GetActiveTableDictResponse response = new GetActiveTableDictHandler()
                .handleRequest(request);
        return response.getActiveTable();
    }

    private ActiveTableMode getActiveTableMode(boolean practice) {
        return practice ? ActiveTableMode.PRACTICE
                : ActiveTableMode.OPERATIONAL;
    }

    /*
     * Used to create a clone of an active table dict when there are multiple
     * hazard ids associated with the dict etn.
     */
    private Map<String, Object> cloneActiveTableDict(
            Map<String, Object> activeTableDict) {
        Map<String, Object> activeTableDictClone = new HashMap<>();

        activeTableDictClone.putAll(activeTableDict);

        return activeTableDictClone;
    }
}

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
package com.raytheon.uf.edex.hazards.interop.activetable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.raytheon.uf.common.activetable.ActiveTableMode;
import com.raytheon.uf.common.activetable.GetActiveTableDictRequest;
import com.raytheon.uf.common.activetable.GetActiveTableDictResponse;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.HazardEventManager;
import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.HazardEventManager.Mode;
import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardEvent;
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
import com.raytheon.uf.common.serialization.comm.RequestRouter;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.util.CollectionUtil;
import com.raytheon.uf.edex.hazards.interop.InteroperabilityUtil;

/**
 * Request handler for retrieving active records for Hazard Services. Takes
 * initial active table records, cross-checks them against the
 * practice_hazards_interoperability table, and only returns those records for
 * which a record in the interoperability table exists. Additionally, the
 * records returned contain additional metadata from the hazard event.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Dec 12, 2014   2826     dgilling     Initial creation
 * Feb 16, 2017  29138     Chris.Golden Changed to work with new hazard
 *                                      event manager.
 * </pre>
 * 
 * @author dgilling
 * @version 1.0
 */

public final class ActiveTableInteropRetrieveHandler implements
        IRequestHandler<VtecInteroperabilityActiveTableRequest> {

    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(ActiveTableInteropRetrieveHandler.class);

    private static final List<Map<String, Object>> EMPTY_ACTIVE_TABLE = Collections
            .emptyList();

    private static final String SIG_FIELD = "sig";

    private static final String PHEN_FIELD = "phen";

    private static final String ETN_FIELD = "etn";

    private static final String HEADLINE_FIELD = "hdln";

    private static final String KEY_FIELD = "key";

    private static final String SITE_ID_FIELD = "xxxid";

    private final Map<String, String> headlineLookupMap;

    public ActiveTableInteropRetrieveHandler() {
        statusHandler.info("Starting initialization ...");

        this.headlineLookupMap = new HashMap<>();

        IPathManager pm = PathManagerFactory.getPathManager();
        LocalizationFile file = pm
                .getStaticLocalizationFile(HazardsConfigurationConstants.HAZARD_TYPES_PY);
        ConfigLoader<HazardTypes> hazardTypesConfigLoader = new ConfigLoader<HazardTypes>(
                file, HazardTypes.class);
        HazardTypes hazardTypes = hazardTypesConfigLoader.getConfig();

        for (String hazardType : hazardTypes.keySet()) {
            HazardTypeEntry entry = hazardTypes.get(hazardType);
            this.headlineLookupMap.put(hazardType, entry.getHeadline());
        }

        statusHandler.info("Initialization Complete!");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.common.serialization.comm.IRequestHandler#handleRequest
     * (com.raytheon.uf.common.serialization.comm.IServerRequest)
     */
    @Override
    public VtecInteroperabilityActiveTableResponse handleRequest(
            VtecInteroperabilityActiveTableRequest request) throws Exception {
        VtecInteroperabilityActiveTableResponse response = new VtecInteroperabilityActiveTableResponse();

        ActiveTableMode atMode = (!request.isPractice()) ? ActiveTableMode.OPERATIONAL
                : ActiveTableMode.PRACTICE;
        response.setMode(atMode);

        List<Map<String, Object>> activeTableRecords = null;
        try {
            activeTableRecords = retrieveActiveTableRecords(
                    request.getSiteID(), atMode);
        } catch (Exception e) {
            String errorMsg = "Failed to retrieve the active table records: "
                    + e.getLocalizedMessage() + "!";
            statusHandler.error(errorMsg, e);
            return constructFailureResponse(response, errorMsg);
        }

        if (CollectionUtil.isNullOrEmpty(activeTableRecords)) {
            statusHandler.warn("No active table records were found for site: "
                    + request.getSiteID() + " for ActiveTable mode: " + atMode);
            return constructSuccessResponse(response, EMPTY_ACTIVE_TABLE);
        }

        statusHandler.info("Found " + activeTableRecords.size()
                + " Active Table Records.");

        // 2. Get matching records from HazardEventManager
        HazardEventManager.Mode managerMode = (atMode == ActiveTableMode.OPERATIONAL) ? Mode.OPERATIONAL
                : Mode.PRACTICE;
        HazardEventManager hazEventMgr = new HazardEventManager(managerMode);

        List<Map<String, Object>> consolidatedRecords = new ArrayList<>();
        Map<String, IHazardEvent> hazEventCache = new HashMap<>();

        for (Map<String, Object> activeTableDict : activeTableRecords) {
            String etn = InteroperabilityUtil.padEtnString(activeTableDict.get(
                    ETN_FIELD).toString());
            String siteID = activeTableDict.get(SITE_ID_FIELD).toString();
            String phen = activeTableDict.get(PHEN_FIELD).toString();
            String sig = activeTableDict.get(SIG_FIELD).toString();

            IHazardEvent matchingHazardEvent = null;

            String cacheKey = constructCacheKey(activeTableDict);
            IHazardEvent cachedEvent = hazEventCache.get(cacheKey);
            if (cachedEvent != null) {
                matchingHazardEvent = cachedEvent;
            } else {
                List<HazardEvent> hazardEvents = InteroperabilityUtil
                        .queryInteroperabilityByETNForHazards(hazEventMgr,
                                siteID, phen, sig, etn, null);
                if (!CollectionUtil.isNullOrEmpty(hazardEvents)) {
                    matchingHazardEvent = hazardEvents.get(0);
                    hazEventCache.put(cacheKey, matchingHazardEvent);
                }
            }

            if (matchingHazardEvent != null) {
                Map<String, Object> combinedRecord = combineActiveTableRecordWithHazardEvent(
                        activeTableDict, matchingHazardEvent);
                consolidatedRecords.add(combinedRecord);
            }
        }

        statusHandler
                .info("Found " + consolidatedRecords.size()
                        + " active table dict(s) for site "
                        + request.getSiteID() + ".");
        return constructSuccessResponse(response, consolidatedRecords);
    }

    private static VtecInteroperabilityActiveTableResponse constructFailureResponse(
            VtecInteroperabilityActiveTableResponse response,
            String errorMessage) {
        response.setSuccess(false);
        response.setActiveTable(EMPTY_ACTIVE_TABLE);
        response.setExceptionText(errorMessage);
        return response;
    }

    private static VtecInteroperabilityActiveTableResponse constructSuccessResponse(
            VtecInteroperabilityActiveTableResponse response,
            List<Map<String, Object>> activeTable) {
        response.setSuccess(true);
        response.setActiveTable(activeTable);
        return response;
    }

    private static List<Map<String, Object>> retrieveActiveTableRecords(
            String siteID, ActiveTableMode mode) throws Exception {
        GetActiveTableDictRequest req = new GetActiveTableDictRequest();
        req.setRequestedSiteId(siteID);
        req.setMode(mode);

        Object rawResponse = RequestRouter.route(req);
        GetActiveTableDictResponse response = (GetActiveTableDictResponse) rawResponse;
        return response.getActiveTable();
    }

    private static String constructCacheKey(Map<String, Object> activeTableDict) {
        return activeTableDict.get(SITE_ID_FIELD) + ":"
                + activeTableDict.get(PHEN_FIELD) + ":"
                + activeTableDict.get(SIG_FIELD) + ":"
                + activeTableDict.get(ETN_FIELD);
    }

    private Map<String, Object> combineActiveTableRecordWithHazardEvent(
            final Map<String, Object> activeTableRecord,
            final IHazardEvent hazardEvent) {
        Map<String, Object> combinedRecord = new HashMap<>(activeTableRecord);
        combinedRecord.put(HazardConstants.HAZARD_EVENT_IDENTIFIER,
                hazardEvent.getEventID());
        String hazardType = HazardEventUtilities.getHazardType(hazardEvent);
        combinedRecord.put(KEY_FIELD, hazardType);
        combinedRecord.put(HazardConstants.HAZARD_EVENT_SUB_TYPE, "");
        if (hazardEvent.getSubType() != null) {
            combinedRecord.put(HazardConstants.HAZARD_EVENT_SUB_TYPE,
                    hazardEvent.getSubType());
        }
        combinedRecord.put(HEADLINE_FIELD, headlineLookupMap.get(hazardType));
        return combinedRecord;
    }
}

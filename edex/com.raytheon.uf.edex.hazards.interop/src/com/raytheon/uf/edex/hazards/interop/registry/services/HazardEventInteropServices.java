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
package com.raytheon.uf.edex.hazards.interop.registry.services;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.ws.WebServiceContext;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.cxf.annotations.FastInfoset;
import org.springframework.transaction.annotation.Transactional;

import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardEventUtilities;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.registry.HazardEventServiceException;
import com.raytheon.uf.common.dataplugin.events.hazards.registry.HazardEventServicesUtil;
import com.raytheon.uf.common.dataplugin.events.hazards.request.HazardEventQueryRequest;
import com.raytheon.uf.common.dataplugin.gfe.db.objects.GFERecord;
import com.raytheon.uf.common.dataplugin.gfe.db.objects.ParmID;
import com.raytheon.uf.common.dataplugin.gfe.db.objects.TimeConstraints;
import com.raytheon.uf.common.dataplugin.gfe.discrete.DiscreteKey;
import com.raytheon.uf.common.dataplugin.gfe.slice.DiscreteGridSlice;
import com.raytheon.uf.common.dataplugin.hazards.interoperability.HazardInteroperabilityRecord;
import com.raytheon.uf.common.dataplugin.hazards.interoperability.registry.HazardConflictDict;
import com.raytheon.uf.common.dataplugin.hazards.interoperability.registry.HazardInteroperabilityResponse;
import com.raytheon.uf.common.dataplugin.hazards.interoperability.registry.services.IHazardEventInteropServices;
import com.raytheon.uf.common.hazards.configuration.ConfigLoader;
import com.raytheon.uf.common.hazards.configuration.HazardsConfigurationConstants;
import com.raytheon.uf.common.hazards.configuration.types.HazardTypeEntry;
import com.raytheon.uf.common.hazards.configuration.types.HazardTypes;
import com.raytheon.uf.common.localization.IPathManager;
import com.raytheon.uf.common.localization.LocalizationFile;
import com.raytheon.uf.common.localization.PathManagerFactory;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.TimeRange;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.edex.database.dao.CoreDao;
import com.raytheon.uf.edex.database.dao.DaoConfig;
import com.raytheon.uf.edex.hazards.interop.dao.InteropObjectManager;
import com.raytheon.uf.edex.hazards.interop.gfe.GFERecordUtil;
import com.raytheon.uf.edex.hazards.interop.gfe.GridRequestHandler;
import com.raytheon.uf.edex.registry.ebxml.util.EbxmlObjectUtil;

/**
 * 
 * Service implementation for the Hazard Services Interoperability web services
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 29, 2015 6895      Ben.Phillippe Refactored Hazard Service data access
 * Aug 04, 2015 6895      Ben.Phillippe Finished HS data access refactor
 * Aug 20, 2015 6895      Ben.Phillippe Routing registry requests through
 *                                      request server
 * </pre>
 * 
 * @author bphillip
 * @version 1.0
 */
@FastInfoset
@WebService(targetNamespace = IHazardEventInteropServices.NAMESPACE, endpointInterface = "com.raytheon.uf.common.dataplugin.hazards.interoperability.registry.services.IHazardEventInteropServices", portName = "HazardEventInteropServicesPort", serviceName = IHazardEventInteropServices.SERVICE_NAME)
@SOAPBinding
@Transactional
public class HazardEventInteropServices implements IHazardEventInteropServices {

    /** The logger */
    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(HazardEventInteropServices.class);

    private static final List<Map<String, Object>> EMPTY_ACTIVE_TABLE = Collections
            .emptyList();

    private static final Map<String, String> headlineLookupMap = initHeadlineMap();

    private InteropObjectManager interopObjectManager;

    /** Grid request handler for interacting with GFE grids */
    private GridRequestHandler gridRequestHandler;

    /** The hazards conflict dictionary */
    private HazardConflictDict hazardsConflictDict;

    /** Denotes if this is a practice set of services */
    private boolean practice;

    @Resource
    private WebServiceContext wsContext;

    @Override
    @WebMethod(operationName = "store")
    public void store(
            @WebParam(name = "events") HazardInteroperabilityRecord... events) {
        storeEventList(Arrays.asList(events));
    }

    @Override
    @WebMethod(operationName = "storeEventList")
    public void storeEventList(
            @WebParam(name = "events") List<HazardInteroperabilityRecord> events) {
        for (HazardInteroperabilityRecord record : events) {
            record.setPractice(practice);
        }
        interopObjectManager.getInteropDao().persistAll(events);
    }

    @Override
    @WebMethod(operationName = "delete")
    public void delete(
            @WebParam(name = "events") HazardInteroperabilityRecord... events) {
        deleteEventList(Arrays.asList(events));
    }

    @Override
    @WebMethod(operationName = "deleteEventList")
    public void deleteEventList(
            @WebParam(name = "events") List<HazardInteroperabilityRecord> events) {
        interopObjectManager.getInteropDao().deleteAll(events);
    }

    @Override
    @WebMethod(operationName = "deleteAll")
    public void deleteAll() {
        interopObjectManager.getInteropDao().deleteAll(
                interopObjectManager.getInteropDao().getAll());
    }

    @Override
    @WebMethod(operationName = "update")
    public void update(
            @WebParam(name = "events") HazardInteroperabilityRecord... events) {
        updateEventList(Arrays.asList(events));
    }

    @Override
    @WebMethod(operationName = "updateEventList")
    public void updateEventList(
            @WebParam(name = "events") List<HazardInteroperabilityRecord> events) {
        for (HazardInteroperabilityRecord record : events) {
            interopObjectManager.getInteropDao().update(record);
        }
    }

    @Override
    @WebMethod(operationName = "retrieveByParams")
    public HazardInteroperabilityResponse retrieveByParams(
            @WebParam(name = "params") Object... params) {
        HazardInteroperabilityResponse response = new HazardInteroperabilityResponse();
        if (params.length == 0) {
            response.setInteropRecords(interopObjectManager.getInteropDao()
                    .getAll());
        } else if (params.length % 3 != 0) {
            throw new IllegalArgumentException(
                    "Parameters submitted to retrieve must be in divisible by 3");
        } else {
            Object[] newParams = new Object[params.length * 2 / 3];
            StringBuilder query = new StringBuilder();
            query.append("FROM ");
            query.append(HazardInteroperabilityRecord.class.getName());
            query.append(" record where record.practice=:practice ");

            int paramIndex = 0;
            for (int i = 0; i < params.length; i += 3) {
                query.append(" and record.");
                query.append(params[i]);
                query.append(" ");
                query.append(params[i + 1]);
                query.append(":");
                query.append(params[i]);

                newParams[paramIndex++] = params[i];
                newParams[paramIndex++] = params[i + 2];
            }
            List<HazardInteroperabilityRecord> queryResult = interopObjectManager
                    .getInteropDao().executeHQLQuery(query.toString(),
                            newParams);
            response.setInteropRecords(queryResult);
        }
        return response;
    }

    @Override
    @WebMethod(operationName = "retrieve")
    public HazardInteroperabilityResponse retrieve(
            @WebParam(name = "request") HazardEventQueryRequest request)
            throws HazardEventServiceException {
        return retrieveByParams(HazardEventServicesUtil
                .convertQueryToArray(request));
    }

    @Override
    @WebMethod(operationName = "hasConflicts")
    public Boolean hasConflicts(@WebParam(name = "phenSig") String phenSig,
            @WebParam(name = "siteID") String siteID,
            @WebParam(name = "startTime") Date startTime,
            @WebParam(name = "endTime") Date endTime) {
        TimeRange timeRange = GFERecordUtil.createGridTimeRange(startTime,
                endTime, new TimeConstraints(TimeUtil.SECONDS_PER_HOUR,
                        TimeUtil.SECONDS_PER_HOUR, 0));
        boolean hasConflicts = hasConflicts(phenSig, timeRange, siteID);
        return hasConflicts;
    }

    @Override
    @WebMethod(operationName = "retrieveHazardsConflictDict")
    public HazardConflictDict retrieveHazardsConflictDict() {
        if (hazardsConflictDict == null) {
            populateHazardsConflictDict();
        }
        return hazardsConflictDict;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.common.dataplugin.events.hazards.registry.services.
     * IHazardEventInteropServices#purgeInteropRecords()
     */
    @Override
    @WebMethod(operationName = "purgeInteropRecords")
    public void purgeInteropRecords() {
        List<HazardInteroperabilityRecord> records = retrieveByParams()
                .getInteropRecords();
        if (!records.isEmpty()) {
            interopObjectManager.getInteropDao().deleteAll(
                    retrieveByParams().getInteropRecords());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.common.dataplugin.events.hazards.registry.services.
     * IHazardEventInteropServices#purgeWarnings()
     */
    @Override
    @WebMethod(operationName = "purgePracticeWarnings")
    public void purgePracticeWarnings() {
        CoreDao dao = new CoreDao(DaoConfig.DEFAULT);
        Object result = null;
        try {
            result = dao.executeSQLUpdate("DELETE FROM practicewarning;");
        } catch (RuntimeException e) {
            statusHandler.error("Failed to purge the practice warning table!",
                    e);
        }

        int rowsDeleted = NumberUtils.toInt(result.toString(), -1);
        if (rowsDeleted >= 0) {
            statusHandler.info("Successfully removed " + rowsDeleted
                    + " records from the practice warning table.");
        }
    }

    @Override
    @WebMethod(operationName = "ping")
    public String ping() {
        statusHandler.info("Received Ping from "
                + EbxmlObjectUtil.getClientHost(wsContext));
        return "OK";
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    @WebMethod(operationName = "getActiveTable")
    public HazardInteroperabilityResponse getActiveTable(
            @WebParam(name = "siteID") String siteID)
            throws HazardEventServiceException {
        /*
         * TODO: This portion of code is going to be reworked in the next
         * changeset. It is currently not used. The JSON file in the
         * localization directory is currently used for retrieving VTEC for
         * Hazard Services
         */
        HazardInteroperabilityResponse response = new HazardInteroperabilityResponse();
        return response;
        // // Get the active table mode
        // ActiveTableMode activeTableMode = practice ? ActiveTableMode.PRACTICE
        // : ActiveTableMode.OPERATIONAL;
        //
        // // Get the interop mode from the active table mode
        // Mode mode = (activeTableMode == ActiveTableMode.OPERATIONAL) ?
        // Mode.OPERATIONAL
        // : Mode.PRACTICE;
        //
        // // Set the response mode
        // response.setMode(activeTableMode);
        //
        // // Retrieve correct data access objects (practice or operational)
        // AbstractActiveTableDao activeTableDao = interopObjectManager
        // .getActiveTableDao(mode);
        // IHazardEventServices eventServices =
        // HazardEventServicesSoapClient.getServices(mode);
        //
        //
        // // Retrieve the active table records
        // List<ActiveTableRecord> activeTableRecordObjects = activeTableDao
        // .getBySiteID(siteID);
        //
        // // Convert to map
        // List<Map<String, Object>> activeTableRecords = ActiveTableUtil
        // .convertToDict(activeTableRecordObjects, siteID);
        //
        // if (CollectionUtil.isNullOrEmpty(activeTableRecords)) {
        // statusHandler.warn("No active table records were found for site: "
        // + siteID + " for ActiveTable mode: "
        // + activeTableMode);
        // return constructSuccessResponse(response, EMPTY_ACTIVE_TABLE);
        // }
        //
        // statusHandler.info("Found " + activeTableRecords.size()
        // + " Active Table Records.");
        //
        // List<Map<String, Object>> consolidatedRecords = new ArrayList<>();
        // Map<String, IHazardEvent> hazEventCache = new HashMap<>();
        //
        // for (Map<String, Object> activeTableDict : activeTableRecords) {
        // int id = Integer.valueOf(activeTableDict.get("id").toString());
        // String etn = InteroperabilityUtil.padEtnString(activeTableDict.get(
        // "etn").toString());
        // String site = activeTableDict.get("xxxid").toString();
        // String phen = activeTableDict.get("phen").toString();
        // String sig = activeTableDict.get("sig").toString();
        //
        // IHazardEvent matchingHazardEvent = null;
        //
        // String cacheKey = constructCacheKey(activeTableDict);
        // IHazardEvent cachedEvent = hazEventCache.get(cacheKey);
        // if (cachedEvent != null) {
        // matchingHazardEvent = cachedEvent;
        // } else {
        //
        // //FIXME: Fix retrieval of active table objects
        // // if (interopObjectManager.getInteropDao().getByActiveTableID(id) ==
        // null) {
        // // continue;
        // // }
        // List<HazardEvent> hazardEvents = eventServices.retrieve(new
        // HazardEventQueryRequest(practice,
        // HazardConstants.SITE_ID, site)
        // .and(HazardConstants.PHENOMENON, phen)
        // .and(HazardConstants.SIGNIFICANCE, sig)
        // .and(HazardConstants.ETN, etn)).getEvents();
        // if (!CollectionUtil.isNullOrEmpty(hazardEvents)) {
        // matchingHazardEvent = hazardEvents.get(0);
        // hazEventCache.put(cacheKey, matchingHazardEvent);
        // }
        // }
        // if (matchingHazardEvent != null) {
        // Map<String, Object> combinedRecord =
        // combineActiveTableRecordWithHazardEvent(
        // activeTableDict, matchingHazardEvent);
        // consolidatedRecords.add(combinedRecord);
        // }
        // }
        //
        // statusHandler
        // .info("Found " + consolidatedRecords.size()
        // + " active table dict(s) for site "
        // + siteID + ".");
        // return constructSuccessResponse(response, consolidatedRecords);
    }

    private static HazardInteroperabilityResponse constructSuccessResponse(
            HazardInteroperabilityResponse response,
            List<Map<String, Object>> activeTable) {
        return response;
    }

    private static String constructCacheKey(Map<String, Object> activeTableDict) {
        return activeTableDict.get("xxxid") + ":" + activeTableDict.get("phen")
                + ":" + activeTableDict.get("sig") + ":"
                + activeTableDict.get("etn");
    }

    private Map<String, Object> combineActiveTableRecordWithHazardEvent(
            final Map<String, Object> activeTableRecord,
            final IHazardEvent hazardEvent) {
        Map<String, Object> combinedRecord = new HashMap<>(activeTableRecord);
        combinedRecord.put(HazardConstants.HAZARD_EVENT_IDENTIFIER,
                hazardEvent.getEventID());
        String hazardType = HazardEventUtilities.getHazardType(hazardEvent);
        combinedRecord.put("key", hazardType);
        combinedRecord.put(HazardConstants.HAZARD_EVENT_SUB_TYPE, "");
        if (hazardEvent.getSubType() != null) {
            combinedRecord.put(HazardConstants.HAZARD_EVENT_SUB_TYPE,
                    hazardEvent.getSubType());
        }
        combinedRecord.put("hdln", headlineLookupMap.get(hazardType));
        return combinedRecord;
    }

    /**
     * @param interopObjectManager
     *            the interopObjectManager to set
     */
    public void setInteropObjectManager(
            InteropObjectManager interopObjectManager) {
        this.interopObjectManager = interopObjectManager;
    }

    private boolean hasConflicts(String phenSig, TimeRange timeRange,
            String siteID) {
        try {
            retrieveHazardsConflictDict();
            final String parmIDFormat = (practice ? GridRequestHandler.PRACTICE_PARM_ID_FORMAT
                    : GridRequestHandler.OPERATIONAL_PARM_ID_FORMAT);
            ParmID parmID = new ParmID(String.format(parmIDFormat, siteID));
            List<GFERecord> potentialRecords = gridRequestHandler
                    .findIntersectedGrid(parmID, timeRange);
            // test if hazardEvent will conflict with existing grids
            if (hazardsConflictDict != null
                    && hazardsConflictDict.get(phenSig) != null) {
                List<String> hazardsConflictList = hazardsConflictDict
                        .get(phenSig);
                for (GFERecord record : potentialRecords) {
                    DiscreteGridSlice gridSlice = (DiscreteGridSlice) record
                            .getMessageData();
                    for (DiscreteKey discreteKey : gridSlice.getKeys()) {
                        for (String key : discreteKey.getSubKeys()) {
                            if (hazardsConflictList.contains(key)) {
                                return true;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            statusHandler.error(
                    "Error trying to retrieve intersecting gfe records", e);
        }
        return false;
    }

    private static HashMap<String, String> initHeadlineMap() {
        HashMap<String, String> headlineLookupMap = new HashMap<String, String>();

        IPathManager pm = PathManagerFactory.getPathManager();
        LocalizationFile file = pm
                .getStaticLocalizationFile(HazardsConfigurationConstants.HAZARD_TYPES_PY);
        ConfigLoader<HazardTypes> hazardTypesConfigLoader = new ConfigLoader<HazardTypes>(
                file, HazardTypes.class);
        HazardTypes hazardTypes = hazardTypesConfigLoader.getConfig();

        for (String hazardType : hazardTypes.keySet()) {
            HazardTypeEntry entry = hazardTypes.get(hazardType);
            headlineLookupMap.put(hazardType, entry.getHeadline());
        }
        return headlineLookupMap;
    }

    /**
     * Populates hazardConflictsDict with HazardsConflictDict from
     * MergeHazards.py
     */
    private void populateHazardsConflictDict() {
        statusHandler.info("Retrieving the hazard conflict dictionary.");

        hazardsConflictDict = new HazardConflictDict();

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
            hazardsConflictDict.put(hazardType, entry.getHazardConflictList());
        }

        statusHandler
                .info("Successfully retrieved the hazard conflict dictionary!");
    }

    /**
     * @param practice
     *            the practice to set
     */
    public void setPractice(boolean practice) {
        this.practice = practice;
    }

    /**
     * @param gridRequestHandler
     *            the gridRequestHandler to set
     */
    public void setGridRequestHandler(GridRequestHandler gridRequestHandler) {
        this.gridRequestHandler = gridRequestHandler;
    }

}

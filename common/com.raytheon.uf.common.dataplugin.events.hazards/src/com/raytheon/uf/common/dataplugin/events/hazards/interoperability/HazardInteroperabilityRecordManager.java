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
package com.raytheon.uf.common.dataplugin.events.hazards.interoperability;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.interoperability.HazardInteroperabilityConstants.INTEROPERABILITY_TYPE;
import com.raytheon.uf.common.dataplugin.events.hazards.interoperability.requests.RecordRetrievePKRequest;
import com.raytheon.uf.common.dataplugin.events.hazards.interoperability.requests.RecordRetrievePKResponse;
import com.raytheon.uf.common.dataplugin.events.hazards.interoperability.requests.RecordRetrieveRequest;
import com.raytheon.uf.common.dataplugin.events.hazards.interoperability.requests.RecordRetrieveResponse;
import com.raytheon.uf.common.dataplugin.events.hazards.interoperability.requests.RecordStorageRequest;
import com.raytheon.uf.common.dataplugin.events.hazards.interoperability.requests.RecordStorageRequest.RequestType;
import com.raytheon.uf.common.serialization.ExceptionWrapper;
import com.raytheon.uf.common.serialization.comm.RequestRouter;
import com.raytheon.uf.common.serialization.comm.response.ServerErrorResponse;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;

/**
 * Used to query, update, and create Hazard Services interoperability records.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 01, 2014           bkowal       Initial creation
 * Dec 18, 2014 #2826     dgilling     Change fields used in interoperability.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class HazardInteroperabilityRecordManager {

    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(HazardInteroperabilityRecordManager.class);

    /**
     * 
     */
    protected HazardInteroperabilityRecordManager() {
    }

    /**
     * 
     * @param record
     */
    public static boolean storeRecord(IHazardsInteroperabilityRecord record) {
        List<IHazardsInteroperabilityRecord> records = new ArrayList<>(1);
        records.add(record);
        return storeRecords(records);
    }

    public static boolean storeRecords(
            List<IHazardsInteroperabilityRecord> records) {
        RecordStorageRequest request = new RecordStorageRequest(
                RequestType.STORE, records);
        return processStorageRequest(request);
    }

    public static boolean updateRecord(IHazardsInteroperabilityRecord record) {
        List<IHazardsInteroperabilityRecord> records = new ArrayList<>(1);
        records.add(record);
        return updateRecords(records);
    }

    public static boolean updateRecords(
            List<IHazardsInteroperabilityRecord> records) {
        RecordStorageRequest request = new RecordStorageRequest(
                RequestType.UPDATE, records);
        return processStorageRequest(request);
    }

    /**
     * 
     * @param record
     */
    public static boolean removeRecord(IHazardsInteroperabilityRecord record) {
        List<IHazardsInteroperabilityRecord> records = new ArrayList<>(1);
        records.add(record);
        return removeRecords(records);
    }

    public static boolean removeRecords(
            List<IHazardsInteroperabilityRecord> records) {
        RecordStorageRequest request = new RecordStorageRequest(
                RequestType.DELETE, records);
        return processStorageRequest(request);
    }

    private static boolean processStorageRequest(RecordStorageRequest request) {
        try {
            RequestRouter.route(request);
            return true;
        } catch (Exception e) {
            statusHandler.handle(Priority.PROBLEM, e.getLocalizedMessage(), e);
        }
        return false;
    }

    public static List<IHazardsInteroperabilityRecord> queryForRecord(
            Class<? extends IHazardsInteroperabilityRecord> entityClass,
            Map<String, Serializable> parameters) {
        RecordRetrieveRequest request = new RecordRetrieveRequest(entityClass,
                parameters);

        try {
            Object responseObject = RequestRouter.route(request);
            if (responseObject instanceof RecordRetrieveResponse) {
                RecordRetrieveResponse response = (RecordRetrieveResponse) responseObject;
                return response.getRecords();
            } else if (responseObject instanceof ServerErrorResponse) {
                ServerErrorResponse response = (ServerErrorResponse) responseObject;
                statusHandler.handle(Priority.ERROR, response.getException()
                        .getMessage(), ExceptionWrapper
                        .unwrapThrowable(response.getException()));
            } else {
                statusHandler.handle(Priority.PROBLEM,
                        "Received an unexpected response of type "
                                + responseObject.getClass());
            }
        } catch (Exception e) {
            statusHandler.handle(Priority.PROBLEM, e.getLocalizedMessage(), e);
        }

        return null;
    }

    public static IHazardsInteroperabilityRecord queryForRecordByPK(
            String siteID, String phenomenon, String significance,
            String hazardEventID, String etn) {
        HazardsInteroperabilityPK key = new HazardsInteroperabilityPK(siteID,
                phenomenon, significance, hazardEventID, etn);

        return queryForRecordByPK(HazardsInteroperability.class, key);
    }

    public static IHazardsInteroperabilityRecord queryForRecordByPK(
            String siteID, String phenomenon, String significance,
            String hazardEventID, Date startDate, Date endDate) {
        HazardsInteroperabilityGFEPK key = new HazardsInteroperabilityGFEPK(
                siteID, phenomenon, significance, hazardEventID, startDate,
                endDate);

        return queryForRecordByPK(HazardsInteroperabilityGFE.class, key);
    }

    private static IHazardsInteroperabilityRecord queryForRecordByPK(
            Class<? extends IHazardsInteroperabilityRecord> entityClass,
            IHazardsInteroperabilityPrimaryKey key) {
        RecordRetrievePKRequest request = new RecordRetrievePKRequest(
                entityClass, key);

        try {
            Object responseObject = RequestRouter.route(request);
            if (responseObject instanceof RecordRetrievePKResponse) {
                RecordRetrievePKResponse response = (RecordRetrievePKResponse) responseObject;
                return response.getRecord();
            } else if (responseObject instanceof ServerErrorResponse) {
                ServerErrorResponse response = (ServerErrorResponse) responseObject;
                statusHandler.handle(Priority.ERROR, response.getException()
                        .getMessage(), ExceptionWrapper
                        .unwrapThrowable(response.getException()));
            } else {
                statusHandler.handle(Priority.PROBLEM,
                        "Received an unexpected response of type "
                                + responseObject.getClass());
            }
        } catch (Exception e) {
            statusHandler.handle(Priority.PROBLEM, e.getLocalizedMessage(), e);
        }

        return null;
    }

    /**
     * Factory method for {@code IHazardsInteroperabilityRecord}.
     * 
     * @param hazardEvent
     *            {@code IHazardEvent} to base the interoperability record
     *            around.
     * @param etn
     *            The ETN sequence number for the hazard.
     * @param interoperabilityType
     *            Legacy application that handles this hazard.
     * @return A {@code IHazardsInteroperabilityRecord} that corresponds to the
     *         specified hazard event.
     */
    public static IHazardsInteroperabilityRecord constructInteroperabilityRecord(
            IHazardEvent hazardEvent, String etn,
            INTEROPERABILITY_TYPE interoperabilityType) {
        return constructInteroperabilityRecord(hazardEvent.getSiteID(),
                hazardEvent.getPhenomenon(), hazardEvent.getSignificance(),
                etn, hazardEvent.getEventID(), interoperabilityType);
    }

    /**
     * Factory method for {@code IHazardsInteroperabilityRecord}.
     * 
     * @param siteID
     *            3-character site ID where the hazard is ocurring.
     * @param phenomenon
     *            Phenomenon code for the hazard.
     * @param significance
     *            Significance code for the hazard.
     * @param etn
     *            The ETN sequence number for the hazard.
     * @param hazardEventID
     *            Event ID of the hazard event
     * @param interoperabilityType
     *            Legacy application that handles this hazard.
     * @return A {@code IHazardsInteroperabilityRecord} that corresponds to the
     *         specified hazard event.
     */
    public static IHazardsInteroperabilityRecord constructInteroperabilityRecord(
            String siteID, String phenomenon, String significance, String etn,
            String hazardEventID, INTEROPERABILITY_TYPE interoperabilityType) {
        HazardsInteroperability record = new HazardsInteroperability();
        record.getKey().setSiteID(siteID);
        record.getKey().setPhen(phenomenon);
        record.getKey().setSig(significance);
        record.getKey().setEtn(etn);
        record.getKey().setHazardEventID(hazardEventID);
        record.setInteroperabilityType(interoperabilityType);

        return record;
    }
}
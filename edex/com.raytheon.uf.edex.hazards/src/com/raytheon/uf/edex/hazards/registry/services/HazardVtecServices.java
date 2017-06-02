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
package com.raytheon.uf.edex.hazards.registry.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

import javax.annotation.Resource;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.ws.WebServiceContext;

import org.apache.cxf.annotations.FastInfoset;
import org.springframework.transaction.annotation.Transactional;

import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.event.vtec.HazardEventVtec;
import com.raytheon.uf.common.dataplugin.events.hazards.registry.HazardEventServiceException;
import com.raytheon.uf.common.dataplugin.events.hazards.registry.HazardEventServicesUtil;
import com.raytheon.uf.common.dataplugin.events.hazards.registry.services.IHazardVtecServices;
import com.raytheon.uf.common.dataplugin.events.hazards.request.HazardEventQueryRequest;
import com.raytheon.uf.common.dataplugin.events.hazards.request.HazardEventVtecResponse;
import com.raytheon.uf.common.registry.ebxml.FactoryRegistryHandler;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.util.CollectionUtil;
import com.raytheon.uf.edex.registry.ebxml.dao.RegistryObjectDao;

/**
 * Service implementation for the Hazard Services VTEC record web services
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 3, 2016  18193    Ben.Phillippe Initial creation
 * May 06, 2016 18202    Robert.Blum   Changes for operational mode.
 * 
 * </pre>
 * 
 * @author bphillip
 * @version 1.0
 */
@FastInfoset
@WebService(targetNamespace = IHazardVtecServices.NAMESPACE, endpointInterface = "com.raytheon.uf.common.dataplugin.events.hazards.registry.services.IHazardVtecServices", portName = "HazardVtecServicesPort", serviceName = IHazardVtecServices.SERVICE_NAME)
@SOAPBinding
@Transactional
public class HazardVtecServices implements IHazardVtecServices {

    /** The logger */
    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(HazardVtecServices.class);

    /** Data Access Object used for registry objects */
    private RegistryObjectDao dao;

    /** Registry handler used to manipulate registry objects */
    private FactoryRegistryHandler registryHandler;

    /** Denotes if this is a practice set of services */
    private boolean practice;

    /** Web service context */
    @Resource
    private WebServiceContext wsContext;

    @Override
    @WebMethod(operationName = "storeVtec")
    public HazardEventVtecResponse storeVtec(
            @WebParam(name = "vtec") HazardEventVtec... vtec)
            throws HazardEventServiceException {
        return storeVtecList(Arrays.asList(vtec));
    }

    @Override
    @WebMethod(operationName = "storeVtecList")
    public HazardEventVtecResponse storeVtecList(
            @WebParam(name = "vtec") List<HazardEventVtec> vtec)
            throws HazardEventServiceException {
        statusHandler.info("Creating " + vtec.size()
                + " HazardEvent VTEC records: ");
        String userName = wsContext.getUserPrincipal().getName();
        HazardEventVtecResponse response = new HazardEventVtecResponse();
        try {
            for (HazardEventVtec vtecRecord : vtec) {
                vtecRecord.setPractice(this.practice);
                response.addExceptions(registryHandler.storeOrReplaceObject(
                        userName, vtecRecord).getErrors());
            }
        } catch (Throwable e) {
            throw new HazardEventServiceException("Error Storing Events", e);
        }
        return HazardEventServicesUtil.checkResponse("STORE_VTEC", "Created "
                + vtec.size() + " HazardEvents VTEC Records.", response);
    }

    @Override
    @WebMethod(operationName = "deleteVtec")
    public HazardEventVtecResponse deleteVtec(
            @WebParam(name = "vtec") HazardEventVtec... vtec)
            throws HazardEventServiceException {
        return deleteVtecList(Arrays.asList(vtec));
    }

    @Override
    @WebMethod(operationName = "deleteVtecList")
    public HazardEventVtecResponse deleteVtecList(
            @WebParam(name = "vtec") List<HazardEventVtec> vtec)
            throws HazardEventServiceException {
        statusHandler.info("Deleting " + vtec.size()
                + " HazardEvent VTEC records.");
        String userName = wsContext.getUserPrincipal().getName();
        HazardEventVtecResponse response = new HazardEventVtecResponse();
        try {
            response.addExceptions(registryHandler.removeObjects(userName,
                    new ArrayList<HazardEventVtec>(vtec)).getErrors());
        } catch (Throwable e) {
            throw new HazardEventServiceException("Error Deleting Events", e);
        }
        return HazardEventServicesUtil.checkResponse("DELETE", "Deleted "
                + vtec.size() + " HazardEvent VTEC records", response);
    }

    @Override
    @WebMethod(operationName = "deleteVtecByQuery")
    public HazardEventVtecResponse deleteVtecByQuery(
            @WebParam(name = "request") HazardEventQueryRequest request)
            throws HazardEventServiceException {
        statusHandler.info("Deleting VTEC records using query: " + request);
        HazardEventVtecResponse response = new HazardEventVtecResponse();
        HazardEventVtecResponse queryResponse = retrieveVtec(request);

        if (queryResponse.isSuccess()) {
            statusHandler.info("Query for delete returned "
                    + queryResponse.getVtecRecords().size()
                    + " VTEC records to delete.");
            if (!CollectionUtil.isNullOrEmpty(queryResponse.getVtecRecords())) {
                response.merge(deleteVtecList(queryResponse.getVtecRecords()));
            }
        } else {
            response.merge(queryResponse);
        }
        return HazardEventServicesUtil.checkResponse("DELETE_BY_QUERY", "",
                response);
    }

    @Override
    @WebMethod(operationName = "deleteAllVtec")
    public HazardEventVtecResponse deleteAllVtec()
            throws HazardEventServiceException {
        statusHandler
                .info("Deleting all HazardEvent VTEC records from the Registry");
        HazardEventVtecResponse deleteAllResponse = new HazardEventVtecResponse();
        try {
            HazardEventVtecResponse retrieveResponse = retrieveVtec(new HazardEventQueryRequest(
                    practice));

            if (retrieveResponse.success()) {
                if (retrieveResponse.getEvents().isEmpty()) {
                    deleteAllResponse.merge(retrieveResponse);
                } else {
                    HazardEventVtecResponse deleteResponse = deleteVtecList(retrieveResponse
                            .getVtecRecords());
                    if (!deleteResponse.success()) {
                        deleteAllResponse.merge(deleteResponse);
                    }
                }
            } else {
                deleteAllResponse.merge(retrieveResponse);
            }

        } catch (Throwable e) {
            throw new HazardEventServiceException(
                    "Error Deleting VTEC records", e);
        }
        return HazardEventServicesUtil.checkResponse("DELETE_ALL", "",
                deleteAllResponse);
    }

    @Override
    @WebMethod(operationName = "updateVtec")
    public HazardEventVtecResponse updateVtec(
            @WebParam(name = "vtec") HazardEventVtec... vtec)
            throws HazardEventServiceException {
        return updateVtecList(Arrays.asList(vtec));
    }

    @Override
    @WebMethod(operationName = "updateVtecList")
    public HazardEventVtecResponse updateVtecList(
            @WebParam(name = "vtec") List<HazardEventVtec> vtec)
            throws HazardEventServiceException {
        statusHandler.info("Updating " + vtec.size()
                + " HazardEvent VTEC records: ");
        String userName = wsContext.getUserPrincipal().getName();
        HazardEventVtecResponse response = new HazardEventVtecResponse();
        try {
            for (HazardEventVtec vtecRecord : vtec) {
                vtecRecord.setPractice(practice);
                response.addExceptions(registryHandler.storeOrReplaceObject(
                        userName, vtecRecord).getErrors());
            }
        } catch (Throwable e) {
            throw new HazardEventServiceException("Error Updating Events", e);
        }
        return HazardEventServicesUtil.checkResponse("UPDATE", "Updated "
                + vtec.size() + " HazardEvent VTEC records.", response);
    }

    @Override
    @WebMethod(operationName = "retrieveVtecByParams")
    public HazardEventVtecResponse retrieveVtecByParams(
            @WebParam(name = "params") Object... params)
            throws HazardEventServiceException {
        HazardEventQueryRequest request = null;
        if (params.length == 0 || params.length % 3 != 0) {
            throw new IllegalArgumentException(
                    "Incorrect number of arguments submitted to retrieve");
        } else {
            request = HazardEventServicesUtil.convertArrayToQuery(params,
                    practice);
        }
        return retrieveVtec(request);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    @WebMethod(operationName = "retrieveVtec")
    public HazardEventVtecResponse retrieveVtec(
            @WebParam(name = "request") HazardEventQueryRequest request)
            throws HazardEventServiceException {
        statusHandler.info("Executing Query for HazardEvent VTEC Records:\n "
                + request);
        HazardEventVtecResponse response = new HazardEventVtecResponse();
        try {
            String query = HazardEventServicesUtil.createAttributeQuery(
                    practice, HazardEventVtec.class, request.getQueryParams());
            // Workaround to ensure unique results are returned
            response.setVtecRecords(HazardEventServicesUtil.getContentObjects(
                    (new LinkedHashSet(dao.executeHQLQuery(query))),
                    HazardEventVtec.class));
        } catch (Throwable e) {
            throw new HazardEventServiceException(
                    "Error Retrieving Events with request: " + request, e);
        }
        return HazardEventServicesUtil.checkResponse("QUERY", "Retrieved "
                + response.getEvents().size() + " HazardEvent VTEC records.",
                response);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    @WebMethod(operationName = "getHazardVtecTable")
    public HazardEventVtecResponse getHazardVtecTable(
            @WebParam(name = "officeID") String officeID)
            throws HazardEventServiceException {
        HazardEventVtecResponse response = new HazardEventVtecResponse();
        HazardEventQueryRequest queryRequest = new HazardEventQueryRequest(
                practice);
        queryRequest.and(HazardConstants.OFFICE_ID, officeID);
        try {
            String query = HazardEventServicesUtil.createAttributeQuery(
                    practice, HazardEventVtec.class,
                    queryRequest.getQueryParams());
            // Workaround to ensure unique results are returned
            response.setVtecRecords(HazardEventServicesUtil.getContentObjects(
                    (new LinkedHashSet(dao.executeHQLQuery(query))),
                    HazardEventVtec.class));
        } catch (Throwable e) {
            throw new HazardEventServiceException(
                    "Error Retrieving Events with request: " + queryRequest, e);
        }
        return (HazardEventVtecResponse) HazardEventServicesUtil.checkResponse(
                "QUERY", "Retrieved " + response.getVtecRecords().size()
                        + " HazardEvent VTEC Records.", response);
    }

    public RegistryObjectDao getDao() {
        return dao;
    }

    public void setDao(RegistryObjectDao dao) {
        this.dao = dao;
    }

    public FactoryRegistryHandler getRegistryHandler() {
        return registryHandler;
    }

    public void setRegistryHandler(FactoryRegistryHandler registryHandler) {
        this.registryHandler = registryHandler;
    }

    public boolean isPractice() {
        return practice;
    }

    public void setPractice(boolean practice) {
        this.practice = practice;
    }

}

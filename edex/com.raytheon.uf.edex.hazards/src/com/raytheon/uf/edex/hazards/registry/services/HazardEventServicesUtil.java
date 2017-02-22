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
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;

import javax.ws.rs.core.MultivaluedMap;
import javax.xml.bind.JAXBException;

import oasis.names.tc.ebxml.regrep.xsd.rim.v4.RegistryObjectType;
import oasis.names.tc.ebxml.regrep.xsd.rim.v4.StringValueType;

import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.registry.HazardEventResponse;
import com.raytheon.uf.common.dataplugin.events.hazards.registry.HazardEventServiceException;
import com.raytheon.uf.common.dataplugin.events.hazards.registry.query.HazardEventQueryRequest;
import com.raytheon.uf.common.dataplugin.events.hazards.registry.query.HazardQueryParameter;
import com.raytheon.uf.common.dataplugin.events.hazards.registry.slotconverter.HazardAttributeSlotConverter;
import com.raytheon.uf.common.serialization.JAXBManager;

/**
 * Utility used by the Hazard Services web services
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * May 29, 2015 6895      Ben.Phillippe Refactored Hazard Service data access.
 * Feb 16, 2017 29138     Chris.Golden  Changed to work with new hazard event
 *                                      services.
 * </pre>
 * 
 * @author bphillip
 * @version 1.0
 */
class HazardEventServicesUtil {

    /** The base part of the slot query */
    protected static final String QUERY_BASE = "select obj from RegistryObjectType obj ";

    /** Jaxb Manager for marshalling the response */
    protected static JAXBManager responseJaxb;
    static {
        try {
            responseJaxb = new JAXBManager(HazardEvent.class,
                    HazardEventResponse.class, HazardEventQueryRequest.class,
                    HazardQueryParameter.class);
        } catch (JAXBException e) {
            throw new RuntimeException(
                    "Error constructing JAXB manager for Hazard Services!", e);
        }
    }

    /**
     * Private constructor
     */
    private HazardEventServicesUtil() {

    }

    /**
     * Creates a slot query based on a map from a REST call
     * 
     * @param queryParameters
     *            The query parameters
     * @return The constructed HQL query
     */
    protected static String createAttributeQuery(
            MultivaluedMap<String, String> queryParameters) {
        boolean practice = true;
        List<HazardQueryParameter> parameters = new ArrayList<HazardQueryParameter>(
                queryParameters.size());
        for (Entry<String, List<String>> entry : queryParameters.entrySet()) {
            for (String value : entry.getValue()) {
                if (entry.getKey().equalsIgnoreCase("practice")) {
                    practice = Boolean.parseBoolean(value);
                } else {
                    parameters.add(new HazardQueryParameter(entry.getKey(),
                            HazardAttributeSlotConverter.determineType(value)));
                }

            }
        }
        return createAttributeQuery(practice, parameters);
    }

    /**
     * Creates an HQL query from the provided list of query parameters
     * 
     * @param practice
     *            If this is a practice mode query
     * @param queryParameters
     *            The query parameters
     * @return The HQL query to execute
     */
    protected static String createAttributeQuery(boolean practice,
            List<HazardQueryParameter> queryParameters) {
        queryParameters.add(new HazardQueryParameter("registryObjectClassName",
                HazardEvent.class.getName()));
        queryParameters.add(new HazardQueryParameter("practice", practice));
        StringBuilder selectFrom = new StringBuilder(QUERY_BASE);
        StringBuilder whereClause = new StringBuilder(" where ");

        int mapSize = queryParameters.size();
        int i = 0;
        for (HazardQueryParameter parameter : queryParameters) {
            if (parameter.getOperand().trim().equalsIgnoreCase("in")) {
                parameter.setOperand("=");
            }

            // Create joins
            selectFrom.append(" inner join obj.slot as slot");
            selectFrom.append(i);
            selectFrom.append(" inner join slot");
            selectFrom.append(i);
            selectFrom.append(".slotValue as value");
            selectFrom.append(i);

            // Create where clause
            whereClause.append("(slot");
            whereClause.append(i);
            whereClause.append(".name='");
            whereClause.append(parameter.getKey());
            whereClause.append("' and ");

            whereClause.append("(");
            for (int j = 0; j < parameter.getValues().length; j++) {
                String column = getColumnName(parameter.getValues()[j]);
                whereClause.append("value");
                whereClause.append(i);
                whereClause.append(".");
                whereClause.append(column);
                whereClause.append(parameter.getOperand());
                if (column.equals("stringValue")) {
                    whereClause.append("'");
                }
                whereClause.append(parameter.getValues()[j]);
                if (column.equals("stringValue")) {
                    whereClause.append("'");
                }
                if (j != parameter.getValues().length - 1) {
                    whereClause.append(" or ");
                }
            }
            whereClause.append("))");

            if (i != mapSize - 1) {
                whereClause.append(" and ");
            }
            i++;
        }

        return selectFrom.toString() + whereClause.toString();
    }

    /**
     * Gets the column type for the value.
     * 
     * In the value table in the ebxml registry database, each type of object
     * (integer, string, boolean, etc.) has their own column to store data in.
     * This method is retrieves the correct column to query on based on the type
     * of the argument passed in.
     * 
     * @param value
     *            The value
     * @return The column type for the value
     */
    private static String getColumnName(Object value) {
        if (value instanceof String) {
            return "stringValue";
        } else if (value instanceof Boolean) {
            return "booleanValue";
        } else if (value instanceof Date || value instanceof Integer
                || value instanceof Long) {
            return "integerValue";
        } else if (value instanceof Float || value instanceof Double) {
            return "floatValue";
        }
        return "stringValue";
    }

    /**
     * Transforms a list of RegistryObjectType objects into HazardEvent objects
     * 
     * @param result
     *            The list of RegistryObjectType objects
     * @return The list of HazardEvent objects
     * @throws HazardEventServiceException
     *             If errors occur extracting the content slot an unmarshalling
     */
    protected static List<HazardEvent> getHazardEvents(
            List<RegistryObjectType> result) throws HazardEventServiceException {
        List<HazardEvent> hazardEvents = new ArrayList<HazardEvent>(
                result.size());
        if (!result.isEmpty()) {
            for (RegistryObjectType obj : result) {
                HazardEvent event;
                try {
                    event = (HazardEvent) responseJaxb
                            .unmarshalFromXml(((StringValueType) obj
                                    .getSlotByName("content").getSlotValue())
                                    .getStringValue());
                } catch (JAXBException e) {
                    throw new HazardEventServiceException(
                            "Error unmarshalling content slot", e);
                }
                hazardEvents.add(event);
            }
        }
        return hazardEvents;
    }

    /**
     * Creates a marshalled response from a list of RegistryObjectType objects
     * 
     * @param result
     *            The list of RegistryObjectType objects
     * @return The marshalled response
     * @throws HazardEventServiceException
     *             If JAXB errors occur
     */
    protected static String getHazardEventResponse(
            List<RegistryObjectType> result) throws HazardEventServiceException {
        HazardEventResponse response = HazardEventResponse
                .createIncludingAllHistoricalAndLatest();
        response.setEvents(getHazardEvents(result));
        return marshal(response);
    }

    /**
     * Creates a marshalled response from a list of RegistryObjectType objects
     * 
     * @param result
     *            The list of RegistryObjectType objects
     * @return The marshalled response
     * @throws HazardEventServiceException
     *             If JAXB errors occur
     */
    protected static String getRegistryObjectResponse(
            List<RegistryObjectType> result) throws HazardEventServiceException {
        HazardEventResponse response = HazardEventResponse.create();
        response.setRegistryObjects(result);
        return marshal(response);
    }

    /**
     * Marshals an object
     * 
     * @param obj
     *            The object to marshal
     * @return The marshalled objectd
     * @throws HazardEventServiceException
     *             If JAXB Exceptions occur
     */
    private static String marshal(Object obj)
            throws HazardEventServiceException {
        try {
            return responseJaxb.marshalToXml(obj);
        } catch (JAXBException e) {
            throw new HazardEventServiceException("Error marshalling", e);
        }
    }
}

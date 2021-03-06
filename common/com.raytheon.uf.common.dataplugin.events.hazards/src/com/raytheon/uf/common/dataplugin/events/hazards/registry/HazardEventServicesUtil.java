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
package com.raytheon.uf.common.dataplugin.events.hazards.registry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;

import javax.ws.rs.core.MultivaluedMap;
import javax.xml.bind.JAXBException;

import com.raytheon.uf.common.dataplugin.events.ValidationException;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.event.GenericRegistryObject;
import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.vtec.HazardEventVtec;
import com.raytheon.uf.common.dataplugin.events.hazards.registry.slotconverter.HazardAttributeSlotConverter;
import com.raytheon.uf.common.dataplugin.events.hazards.request.GenericRegistryObjectQueryRequest;
import com.raytheon.uf.common.dataplugin.events.hazards.request.HazardEventQueryRequest;
import com.raytheon.uf.common.dataplugin.events.hazards.request.HazardQueryParameter;
import com.raytheon.uf.common.serialization.JAXBManager;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

import oasis.names.tc.ebxml.regrep.xsd.rim.v4.RegistryObjectType;
import oasis.names.tc.ebxml.regrep.xsd.rim.v4.StringValueType;

/**
 * Utility used by the Hazard Services web services
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * May 29, 2015  6895     Ben.Phillippe Refactored Hazard Service data access.
 * Aug 04, 2015  6895     Ben.Phillippe Finished HS data access refactor
 * Aug 20, 2015  6895     Ben.Phillippe Routing registry requests through
 *                                      request server
 * May 03, 2016 18193     Ben.Phillippe Replication of Hazard VTEC Records
 *                                      services.
 * May 06, 2016 18202     Robert.Blum   Changes for operational mode.
 * Dec 08, 2016 26663     Kevin.Bisanz  Fix to createAttributeQuery(boolean,
 *                                      Class<?>, List<HazardQueryParameter>)
 *                                      from Richard Peter to improve query
 *                                      performance.
 * Feb 16, 2017 29138     Chris.Golden  Changed to work with new hazard event
 * Feb 16, 2017 28708     Chris.Golden  Changed to use already defined
 *                                      constants.
 * Oct 02, 2017 38506     Chris.Golden  Changed to include ability to
 *                                      serialize and deserialize
 *                                      {@link GenericRegistryObject}
 *                                      instances and related objects.
 * </pre>
 * 
 * @author bphillip
 * @version 1.0
 */
public class HazardEventServicesUtil {

    /**
     * Interface describing methods to be implemented by any classes acting as
     * query parameter key converters. These are used to generate modified
     * {@link HazardQueryParameter} keys.
     */
    public interface IQueryParameterKeyGenerator {

        /**
         * Transform the specified query parameter key..
         * 
         * @param queryParameterKey
         *            Query parameter key to be transformed.
         * @return Transformed key.
         */
        public String getTransformedParameterKey(String queryParameterKey);
    }

    /** The logger */
    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(HazardEventServicesUtil.class);

    /** The base part of the slot query */
    private static final String QUERY_BASE = "select obj from RegistryObjectType obj ";

    protected static final String SLOT_CRITERIA_CLAUSE = "obj.id in (select slot%1$s.parent_id from SlotType slot%1$s inner join slot%1$s.slotValue value%1$s where slot%1$s.name = '%2$s' and (";

    protected static final String VALUE_CLAUSE = "value%1$s.%2$s %3$s %4$s";

    /** Jaxb Manager for marshalling the response */
    private static JAXBManager responseJaxb;

    static {
        try {
            responseJaxb = new JAXBManager(HazardEvent.class,
                    HazardEventResponse.class, HazardEventQueryRequest.class,
                    HazardQueryParameter.class, HazardEventVtec.class,
                    GenericRegistryObject.class,
                    GenericRegistryObjectResponse.class,
                    GenericRegistryObjectQueryRequest.class);
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
     * @param clazz
     *            Class for which the query is to search.
     * @param queryParameters
     *            The query parameters
     * @param queryParameterKeyGenerator
     *            Optional query parameter key generator. If specified, it is
     *            used to generate modified parameter keys in place of any keys
     *            provided in the <code>queryParameters</code>.
     * @return The constructed HQL query
     */
    public static String createAttributeQuery(Class<?> clazz,
            MultivaluedMap<String, String> queryParameters,
            IQueryParameterKeyGenerator queryParameterKeyGenerator) {
        boolean practice = true;
        List<HazardQueryParameter> parameters = new ArrayList<HazardQueryParameter>(
                queryParameters.size());
        for (Entry<String, List<String>> entry : queryParameters.entrySet()) {
            for (String value : entry.getValue()) {
                if (entry.getKey().equalsIgnoreCase(HazardConstants.PRACTICE)) {
                    practice = Boolean.parseBoolean(value);
                } else {
                    parameters.add(new HazardQueryParameter(entry.getKey(),
                            HazardAttributeSlotConverter.determineType(value)));
                }

            }
        }
        return createAttributeQuery(practice, clazz, parameters,
                queryParameterKeyGenerator);
    }

    /**
     * Creates an HQL query from the provided list of query parameters
     * 
     * @param practice
     *            If this is a practice mode query
     * @param clazz
     *            Class for which the query is to search.
     * @param queryParameters
     *            The query parameters
     * @param queryParameterKeyGenerator
     *            Optional query parameter key generator. If specified, it is
     *            used to generate modified parameter keys in place of any keys
     *            provided in the <code>queryParameters</code>.
     * @return The HQL query to execute
     */
    public static String createAttributeQuery(boolean practice, Class<?> clazz,
            List<HazardQueryParameter> queryParameters,
            IQueryParameterKeyGenerator queryParameterKeyGenerator) {
        queryParameters.add(new HazardQueryParameter("registryObjectClassName",
                clazz.getName()));
        queryParameters.add(
                new HazardQueryParameter(HazardConstants.PRACTICE, practice));
        StringBuilder selectFrom = new StringBuilder(QUERY_BASE);
        StringBuilder whereClause = new StringBuilder(" where ");

        int i = 0;
        for (HazardQueryParameter parameter : queryParameters) {
            if (parameter.getOperand().trim().equalsIgnoreCase("in")) {
                parameter.setOperand("=");
            }

            if (i != 0) {
                whereClause.append(" and ");
            }

            /*
             * Add to where clause, using the query parameter key generator if
             * one was provided to transform the key, unless the parameters are
             * one of the last two added above.
             */
            whereClause.append(String.format(SLOT_CRITERIA_CLAUSE, i,
                    ((queryParameterKeyGenerator != null)
                            && (i < queryParameters.size() - 2)
                                    ? queryParameterKeyGenerator
                                            .getTransformedParameterKey(
                                                    parameter.getKey())
                                    : parameter.getKey())));
            boolean addOr = false;
            for (Object val : parameter.getValues()) {
                if (addOr) {
                    whereClause.append(" or ");
                } else {
                    addOr = true;
                }

                String column = getColumnName(val);
                if (column.equals("stringValue")) {
                    val = "'" + val + "'";
                }

                whereClause.append(String.format(VALUE_CLAUSE, i, column,
                        parameter.getOperand(), val));
            }

            whereClause.append("))");

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
     * Transforms a list of RegistryObjectType objects into the specified object
     * type
     * 
     * @param result
     *            The list of RegistryObjectType objects
     * @return The list of HazardEvent objects
     * @throws HazardEventServiceException
     *             If errors occur extracting the content slot an unmarshalling
     */
    @SuppressWarnings("unchecked")
    public static <T extends Object> List<T> getContentObjects(
            Collection<RegistryObjectType> result, Class<T> clazz)
            throws HazardEventServiceException {
        List<T> objs = new ArrayList<T>(result.size());
        if (!result.isEmpty()) {
            for (RegistryObjectType obj : result) {
                T contentObj;
                try {
                    contentObj = (T) responseJaxb.unmarshalFromXml(
                            ((StringValueType) obj.getSlotByName("content")
                                    .getSlotValue()).getStringValue());
                } catch (JAXBException e) {
                    throw new HazardEventServiceException(
                            "Error unmarshalling content slot", e);
                }
                objs.add(contentObj);
            }
        }
        return objs;
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
    public static String getHazardEventResponse(List<RegistryObjectType> result)
            throws HazardEventServiceException {
        HazardEventResponse response = HazardEventResponse
                .createIncludingAllHistoricalAndLatest();
        response.setEvents(getContentObjects(result, HazardEvent.class));
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
    public static String getRegistryObjectResponse(
            List<RegistryObjectType> result)
            throws HazardEventServiceException {
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

    /**
     * Validates events
     * 
     * @param events
     *            The events to validate
     * @throws ValidationException
     *             If validation errors occur
     */
    public static void validateEvents(List<HazardEvent> events)
            throws ValidationException {
        for (HazardEvent event : events) {
            event.isValid();
        }
    }

    /**
     * Checks the response and outputs the standard message
     * 
     * @param operation
     *            The operation executed
     * @param details
     *            Any additional details to add to the message
     * @param response
     *            The response object
     * @return The REsponse object
     */
    @SuppressWarnings("unchecked")
    public static <T extends HazardEventResponse> T checkResponse(
            String operation, String details,
            final HazardEventResponse response) {
        StringBuilder builder = new StringBuilder();
        if (response.success()) {
            builder.append("Successfully executed [");
            builder.append(operation);
            builder.append("] operation.\n\tDetails: ");
            builder.append(details);
            statusHandler.info(builder.toString());
        } else {
            builder.append("Failed to execute [");
            builder.append(operation);
            builder.append("] operation.\n\tDetails: ");
            builder.append(details);
            statusHandler.error(builder.toString());
        }
        return (T) response;
    }

    /**
     * Converts a query submitted as an array into a query request object
     * 
     * @param params
     *            The parameters to convert
     * @param practice
     *            flag indicating practice mode
     * @return
     */
    public static HazardEventQueryRequest convertArrayToQuery(Object[] params,
            boolean practice) {
        HazardEventQueryRequest request = new HazardEventQueryRequest(practice);
        for (int i = 0; i < params.length; i += 3) {
            if ((params[i] instanceof String)
                    && (params[i + 1] instanceof String)) {
                request.and((String) params[i], (String) params[i + 1],
                        params[i + 2]);
            } else {
                throw new IllegalArgumentException(
                        "Incorrect parameter types received by retrieve query");
            }
        }
        return request;
    }

    /**
     * Converts a query request to an array of parameters
     * 
     * @param query
     *            The query request
     * @return The parameters from the query request converted into an object
     *         array
     */
    public static Object[] convertQueryToArray(HazardEventQueryRequest query) {
        Object[] parameters = new Object[query.getQueryParams().size() * 3];
        int i = 0;
        for (HazardQueryParameter param : query.getQueryParams()) {
            parameters[i++] = param.getKey();
            parameters[i++] = param.getOperand();
            if (param.getValues().length == 1) {
                parameters[i++] = param.getValues()[0];
            } else {
                parameters[i++] = Arrays.asList(param.getValues());
            }
        }
        return parameters;
    }
}

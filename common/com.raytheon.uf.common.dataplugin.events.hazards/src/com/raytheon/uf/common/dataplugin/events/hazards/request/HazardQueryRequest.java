/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Evaluation & Decision Support Branch (EDS)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package com.raytheon.uf.common.dataplugin.events.hazards.request;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.raytheon.uf.common.dataplugin.events.hazards.registry.HazardEventServicesUtil;
import com.raytheon.uf.common.dataplugin.events.hazards.registry.HazardEventServicesUtil.IQueryParameterKeyGenerator;
import com.raytheon.uf.common.registry.constants.CanonicalQueryTypes;
import com.raytheon.uf.common.registry.constants.QueryLanguages;
import com.raytheon.uf.common.registry.constants.QueryReturnTypes;
import com.raytheon.uf.common.registry.ebxml.RegistryUtil;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

import oasis.names.tc.ebxml.regrep.xsd.query.v4.QueryRequest;
import oasis.names.tc.ebxml.regrep.xsd.query.v4.ResponseOptionType;
import oasis.names.tc.ebxml.regrep.xsd.rim.v4.QueryType;
import oasis.names.tc.ebxml.regrep.xsd.rim.v4.SlotType;
import oasis.names.tc.ebxml.regrep.xsd.rim.v4.StringValueType;

/**
 * Description: Base class for subclasses used as request objects for querying
 * for registry objects within Hazard Services. The generic parameter
 * <code>R</code> must be the concrete subclass.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Oct 02, 2017   38506    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public abstract class HazardQueryRequest<R extends HazardQueryRequest<R>>
        extends HazardRequest {

    // Private Variables

    /**
     * List of query parameters.
     */
    @XmlElement
    @DynamicSerializeElement
    private List<HazardQueryParameter> queryParams = new ArrayList<>();

    // Public Constructors

    /**
     * Construct an empty instance for practice mode.
     */
    public HazardQueryRequest() {
        super(true);
    }

    /**
     * Construct an empty instance.
     * 
     * @param practice
     *            Flag indicating whether or not in practice mode.
     */
    public HazardQueryRequest(boolean practice) {
        super(practice);
    }

    /**
     * Construct a standard instance with the given parameters.
     * 
     * @param practice
     *            Flag indicating whether or not in practice mode.
     * @param key
     *            Parameter for which to search.
     * @param operand
     *            Comparison operand to use.
     * @param values
     *            Values for which to search under the key.
     */
    public HazardQueryRequest(boolean practice, String key, String operand,
            Object[] values) {
        super(practice);
        queryParams.add(new HazardQueryParameter(key, operand, values));
    }

    /**
     * Construct a standard instance with the given parameters.
     * 
     * @param practice
     *            Flag indicating whether or not in practice mode.
     * @param key
     *            Parameter for which to search.
     * @param operand
     *            Comparison operand to use.
     * @param value
     *            Value for which to search under the key.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public HazardQueryRequest(boolean practice, String key, String operand,
            Object value) {
        super(practice);
        if (value instanceof Collection) {
            Collection collection = (Collection) value;
            queryParams.add(new HazardQueryParameter(key, "in",
                    collection.toArray(new Object[collection.size()])));
        } else {
            queryParams.add(new HazardQueryParameter(key, operand, value));
        }
    }

    /**
     * Construct a standard instance with the given parameters that will use the
     * equals ( <code>=</code>) operator for comparisons.
     * 
     * @param practice
     *            Flag indicating whether or not in practice mode.
     * @param key
     *            Parameter for which to search.
     * @param value
     *            Value for which to search under the key when testing for
     *            equality.
     */
    public HazardQueryRequest(boolean practice, String key, Object value) {
        this(practice, key, "=", value);
    }

    /**
     * Construct a standard instance with the given parameters that will use the
     * in ( <code>in</code>) operator for comparisons.
     * 
     * @param practice
     *            Flag indicating whether or not in practice mode.
     * @param key
     *            Parameter for which to search.
     * @param values
     *            Values for which to search under the key when testing for the
     *            "in" relationship.
     */
    public HazardQueryRequest(boolean practice, String key, Object[] values) {
        this(practice, key, "in", values);
    }

    /**
     * Construct a standard instance with the given parameters.
     * 
     * @param practice
     *            Flag indicating whether or not in practice mode.
     * @param parameters
     *            Parameters of the query.
     */
    public HazardQueryRequest(boolean practice,
            Collection<HazardQueryParameter> parameters) {
        super(practice);
        queryParams.addAll(parameters);
    }

    // Public Methods

    /**
     * Get the query parameters.
     * 
     * @return Query parameters.
     */
    public List<HazardQueryParameter> getQueryParams() {
        return queryParams;
    }

    /**
     * Set the query parameters.
     * 
     * @param queryParams
     *            Query parameters.
     */
    public void setQueryParams(List<HazardQueryParameter> queryParams) {
        this.queryParams = queryParams;
    }

    /**
     * Concatenate a clause onto the query.
     * 
     * @param key
     *            Parameter for which to search.
     * @param operand
     *            Comparison operand to use.
     * @param values
     *            Values for which to search under the key.
     * @return This object.
     */
    @SuppressWarnings("unchecked")
    public R and(String key, String operand, Object... values) {
        queryParams.add(new HazardQueryParameter(key, operand, values));
        return (R) this;
    }

    /**
     * Concatenate a clause onto the query.
     * 
     * @param key
     *            Parameter for which to search.
     * @param operand
     *            Comparison operand to use. Note that this is ignored if
     *            <code>value</code> is a {@link Collection}; in that case, the
     *            in (<code>in</code>) operand is used instead.
     * @param value
     *            Value for which to search under the key.
     * @return This object.
     */
    @SuppressWarnings({ "unchecked" })
    public R and(String key, String operand, Object value) {
        if (value instanceof Collection) {
            Collection<?> collection = (Collection<?>) value;
            queryParams.add(new HazardQueryParameter(key, "in",
                    collection.toArray(new Object[collection.size()])));
        } else {
            queryParams.add(new HazardQueryParameter(key, operand, value));
        }
        return (R) this;
    }

    /**
     * Concatenate a clause onto the query assuming an equals (<code>=</code>)
     * operand.
     * 
     * @param key
     *            Parameter for which to search.
     * @param value
     *            Value for which to search under the key when testing for
     *            equality.
     * @return This object.
     */
    public R and(String key, Object value) {
        return and(key, "=", value);
    }

    /**
     * Concatenate a clause onto the query assuming an in (<code>in</code>)
     * operand.
     * 
     * @param key
     *            Parameter for which to search.
     * @param values
     *            Values for which to search under the key when testing for the
     *            "in" relationship.
     * @return This object.
     */
    @SuppressWarnings("unchecked")
    public R and(String key, Object[] values) {
        queryParams.add(new HazardQueryParameter(key, values));
        return (R) this;
    }

    /**
     * Concatenate a clause onto the query assuming an in (<code>in</code>)
     * operand.
     * 
     * @param key
     *            Parameter for which to search.
     * @param values
     *            Values for which to search under the key when testing for the
     *            "in" relationship.
     * @return This object.
     */
    @SuppressWarnings("unchecked")
    public R and(String key, Collection<?> values) {
        queryParams.add(new HazardQueryParameter(key,
                values.toArray(new Object[values.size()])));
        return (R) this;
    }

    /**
     * Get the registry query request.
     * 
     * @return Registry query request.
     */
    public QueryRequest getRegistryQueryRequest() {
        String queryExpression = HazardEventServicesUtil.createAttributeQuery(
                isPractice(), getTypeToBeQueried(), this.queryParams,
                getQueryParameterKeyGenerator());
        QueryRequest queryRequest = new QueryRequest();
        queryRequest.setId(RegistryUtil.generateRegistryObjectId());
        ResponseOptionType responseOption = new ResponseOptionType();
        responseOption.setReturnComposedObjects(true);
        responseOption.setReturnType(QueryReturnTypes.REGISTRY_OBJECT);
        QueryType query = new QueryType();
        query.setQueryDefinition(CanonicalQueryTypes.ADHOC_QUERY);
        query.getSlot().add(new SlotType("queryLanguage",
                new StringValueType(QueryLanguages.HQL)));
        query.getSlot().add(new SlotType("queryExpression",
                new StringValueType(queryExpression)));
        queryRequest.setQuery(query);
        return queryRequest;
    }

    /**
     * Get the query parameter key generator, used to generate modified
     * parameter keys in place of any keys provided in
     * {@link HazardQueryParameter} instances when subclasses are being
     * submitted for queries.
     * 
     * @return Query parameter key generator, or <code>null</code> if there is
     *         no need to modify query parameter keys.
     */
    public abstract IQueryParameterKeyGenerator getQueryParameterKeyGenerator();

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        int i = 0;
        for (HazardQueryParameter param : queryParams) {
            builder.append(param);
            if (i != queryParams.size() - 1) {
                builder.append(" AND ");
            }
            i++;
        }

        return builder.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        return prime * super.hashCode()
                + ((queryParams == null) ? 0 : queryParams.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj) == false) {
            return false;
        }
        HazardQueryRequest<?> other = (HazardQueryRequest<?>) obj;
        if (queryParams == null) {
            if (other.queryParams != null) {
                return false;
            }
        } else if (queryParams.equals(other.queryParams) == false) {
            return false;
        }
        return true;
    }

    // Protected Methods

    /**
     * Get the type of object to be queried using this object.
     * 
     * @return Type of object to be queried.
     */
    protected abstract Class<?> getTypeToBeQueried();
}

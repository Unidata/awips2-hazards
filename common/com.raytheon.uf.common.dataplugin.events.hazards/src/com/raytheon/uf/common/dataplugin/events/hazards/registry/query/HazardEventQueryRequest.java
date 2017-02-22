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
package com.raytheon.uf.common.dataplugin.events.hazards.registry.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.HazardEventManager.Include;

/**
 * 
 * Request used to query the hazard event registry
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 29, 2015 6895      Ben.Phillippe Refactored Hazard Service data access
 * Feb 16, 2017 29138     Chris.Golden  Revamped to slim down the responses to
 *                                      these requests so that the former do
 *                                      not carry extra serialized objects
 *                                      with them that are not needed.
 * </pre>
 * 
 * @author bphillip
 * @version 1.0
 */
@XmlRootElement(name = "HazardEventQueryRequest")
@XmlAccessorType(XmlAccessType.NONE)
public class HazardEventQueryRequest {

    // Private Variables

    /**
     * List of query parameters.
     */
    @XmlElement
    private List<HazardQueryParameter> queryParams = new ArrayList<HazardQueryParameter>();

    /**
     * Which categories of hazard events to include in the response.
     */
    @XmlElement
    private Include include = Include.HISTORICAL_AND_LATEST_EVENTS;

    /**
     * Flag indicating, if <code>true</code>, that only the size of the list of
     * hazard events that would otherwise be requested is needed, not the list
     * of events themselves.
     */
    @XmlElement
    private boolean sizeOnly = false;

    // Public Constructors

    /**
     * Create a new empty request that will include both historical and latest
     * events in the response. If another inclusion type is desired,
     * {@link #setInclude(Include)} may be invoked. If only the size of the set
     * of events being requested is needed,
     * {@link #setSizeOnlyRequired(boolean)} may be invoked.
     */
    public HazardEventQueryRequest() {

        /*
         * No action.
         */
    }

    /**
     * Create a new empty request that will include the specified versions of
     * hazard events, both historical and latest versions. If another inclusion
     * type is desired, {@link #setInclude(Include)} may be invoked. If only the
     * size of the set of events being requested is needed,
     * {@link #setSizeOnlyRequired(boolean)} may be invoked.
     * 
     * @param include
     *            Event versions to be included.
     */
    public HazardEventQueryRequest(Include include) {
        this.include = include;
    }

    /**
     * Create a new request with the given parameters that will yield both
     * historical and latest versions of events. If another inclusion type is
     * desired, {@link #setInclude(Include)} may be invoked. If only the size of
     * the set of events being requested is needed,
     * {@link #setSizeOnlyRequired(boolean)} may be invoked.
     * 
     * @param key
     *            Parameter for which to search.
     * @param operand
     *            Comparison operand to use.
     * @param values
     *            Values for which to search under the key.
     */
    public HazardEventQueryRequest(String key, String operand, Object[] values) {
        queryParams.add(new HazardQueryParameter(key, operand, values));
    }

    /**
     * Create a new request with the given parameters that will yield both
     * historical and latest versions of events. If another inclusion type is
     * desired, {@link #setInclude(Include)} may be invoked. If only the size of
     * the set of events being requested is needed,
     * {@link #setSizeOnlyRequired(boolean)} may be invoked.
     * 
     * @param key
     *            Parameter for which to search.
     * @param operand
     *            Comparison operand to use.
     * @param value
     *            Value for which to search under the key.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public HazardEventQueryRequest(String key, String operand, Object value) {
        if (value instanceof Collection) {
            Collection coll = (Collection) value;
            queryParams.add(new HazardQueryParameter(key, "in", coll
                    .toArray(new Object[coll.size()])));
        } else {
            queryParams.add(new HazardQueryParameter(key, operand, value));
        }
    }

    /**
     * Create a new request with the given parameters that will use the equals (
     * <code>=</code>) operator for comparisons that will yield both historical
     * and latest versions of events. If another inclusion type is desired,
     * {@link #setInclude(Include)} may be invoked. If only the size of the set
     * of events being requested is needed,
     * {@link #setSizeOnlyRequired(boolean)} may be invoked.
     * 
     * @param key
     *            Parameter for which to search.
     * @param value
     *            Value for which to search under the key when testing for
     *            equality.
     */
    public HazardEventQueryRequest(String key, Object value) {
        this(key, "=", value);
    }

    /**
     * Create a new request with the given parameters that will use the in (
     * <code>in</code>) operator for comparisons that will yield both historical
     * and latest versions of events. If another inclusion type is desired,
     * {@link #setInclude(Include)} may be invoked. If only the size of the set
     * of events being requested is needed,
     * {@link #setSizeOnlyRequired(boolean)} may be invoked.
     * 
     * @param key
     *            Parameter for which to search.
     * @param values
     *            Values for which to search under the key when testing for the
     *            "in" relationship.
     */
    public HazardEventQueryRequest(String key, Object[] values) {
        this(key, "in", values);
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
     * Get the categories of hazard events to be included.
     * 
     * @return Categories of hazard events to be included.
     */
    public Include getInclude() {
        return include;
    }

    /**
     * Set the categories of hazard events to be included.
     * 
     * @param include
     *            Categories of hazard events to be included.
     */
    public void setInclude(Include include) {
        this.include = include;
    }

    /**
     * Determine whether only the size of the list of hazard events is being
     * requested, or whether the list itself is required.
     * 
     * @return <code>true</code> if only the size of the list of hazard events
     *         is requested, <code>false</code> if the list is required.
     */
    public boolean isSizeOnlyRequired() {
        return sizeOnly;
    }

    /**
     * Set the flag indicating whether only the size of the list of hazard
     * events is being requested, or whether the list itself is required.
     * 
     * @param sizeOnly
     *            Flag indicating, if <code>true</code>, that only the size of
     *            the list of hazard events is requested, or if
     *            <code>false</code>, that the list is required.
     */
    public void setSizeOnlyRequired(boolean sizeOnly) {
        this.sizeOnly = sizeOnly;
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
    public HazardEventQueryRequest and(String key, String operand,
            Object... values) {
        queryParams.add(new HazardQueryParameter(key, operand, values));
        return this;
    }

    /**
     * Concatenate a clause onto the query.
     * 
     * @param key
     *            Parameter for which to search.
     * @param operand
     *            Comparison operand to use.
     * @param value
     *            Value for which to search under the key.
     * @return This object.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public HazardEventQueryRequest and(String key, String operand, Object value) {
        if (value instanceof Collection) {
            Collection collection = (Collection) value;
            queryParams.add(new HazardQueryParameter(key, "in", collection
                    .toArray(new Object[collection.size()])));
        } else {
            queryParams.add(new HazardQueryParameter(key, operand, value));
        }
        return this;
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
    public HazardEventQueryRequest and(String key, Object value) {
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
    public HazardEventQueryRequest and(String key, Object[] values) {
        queryParams.add(new HazardQueryParameter(key, values));
        return this;
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
    public HazardEventQueryRequest and(String key, Collection<Object> values) {
        queryParams.add(new HazardQueryParameter(key, values
                .toArray(new Object[values.size()])));
        return this;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        int i = 0;
        for (HazardQueryParameter param : this.queryParams) {
            builder.append(param);
            if (i != this.queryParams.size() - 1) {
                builder.append(" AND ");
            }
            i++;
        }

        return builder.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((queryParams == null) ? 0 : queryParams.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        HazardEventQueryRequest other = (HazardEventQueryRequest) obj;
        if (queryParams == null) {
            if (other.queryParams != null) {
                return false;
            }
        } else if (!queryParams.equals(other.queryParams)) {
            return false;
        }
        return true;
    }
}

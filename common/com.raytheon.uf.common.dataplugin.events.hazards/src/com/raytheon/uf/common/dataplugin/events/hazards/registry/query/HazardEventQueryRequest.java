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
 * 
 * </pre>
 * 
 * @author bphillip
 * @version 1.0
 */
@XmlRootElement(name = "HazardEventQueryRequest")
@XmlAccessorType(XmlAccessType.NONE)
public class HazardEventQueryRequest {

    /** The list of query parameters */
    @XmlElement
    private List<HazardQueryParameter> queryParams = new ArrayList<HazardQueryParameter>();

    /**
     * Creates a new empty HazardEventQueryRequest
     */
    public HazardEventQueryRequest() {

    }

    /**
     * Creates a new HazardEventQueryRequest with the given values
     * 
     * @param key
     *            The parameter to search for
     * @param operand
     *            The comparison operand to use
     * @param values
     *            The values to search for
     */
    public HazardEventQueryRequest(String key, String operand, Object[] values) {
        this.queryParams.add(new HazardQueryParameter(key, operand, values));
    }

    /**
     * Creates a new HazardEventQueryRequest
     * 
     * @param key
     *            The parameter to search for
     * @param operand
     *            The comparison operand to use
     * @param value
     *            The value to search for
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public HazardEventQueryRequest(String key, String operand, Object value) {
        if (value instanceof Collection) {
            Collection coll = (Collection) value;
            this.queryParams.add(new HazardQueryParameter(key, "in", coll
                    .toArray(new Object[coll.size()])));
        } else {
            this.queryParams.add(new HazardQueryParameter(key, operand, value));
        }
    }

    /**
     * Creates a new HazardEventQueryRequest. This constructor automatically
     * assumes an equals (=) operator is to be used
     * 
     * @param key
     *            The parameter to search for
     * @param value
     *            The value to search for
     */
    public HazardEventQueryRequest(String key, Object value) {
        this(key, "=", value);
    }

    /**
     * Creates a new HazardEventQueryRequest. This constructor automatically
     * assumes an in (in) operator is to be used
     * 
     * @param key
     *            The parameter to search for
     * @param values
     *            The values to search for
     */
    public HazardEventQueryRequest(String key, Object[] values) {
        this(key, "in", values);
    }

    /**
     * @return the queryParams
     */
    public List<HazardQueryParameter> getQueryParams() {
        return queryParams;
    }

    /**
     * @param queryParams
     *            the queryParams to set
     */
    public void setQueryParams(List<HazardQueryParameter> queryParams) {
        this.queryParams = queryParams;
    }

    /**
     * Concatenates a clause onto the query
     * 
     * @param key
     *            The parameter to search for
     * @param operand
     *            The operand to use
     * @param values
     *            The value(s) to search for
     * @return This
     */
    public HazardEventQueryRequest and(String key, String operand,
            Object... values) {
        this.queryParams.add(new HazardQueryParameter(key, operand, values));
        return this;
    }

    /**
     * Concatenates a clause onto the query
     * 
     * @param key
     *            The parameter to search for
     * @param operand
     *            The operand to use
     * @param value
     *            The value to search for
     * @return This
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public HazardEventQueryRequest and(String key, String operand, Object value) {
        if (value instanceof Collection) {
            Collection coll = (Collection) value;
            this.queryParams.add(new HazardQueryParameter(key, "in", coll
                    .toArray(new Object[coll.size()])));
        } else {
            this.queryParams.add(new HazardQueryParameter(key, operand, value));
        }
        return this;
    }

    /**
     * Concatenates a clause onto the query. Assumes an equals (=) operand
     * 
     * @param key
     *            The parameter to search for
     * @param values
     *            The value to search for
     * @return This
     */
    public HazardEventQueryRequest and(String key, Object value) {
        return and(key, "=", value);
    }

    /**
     * Concatenates a clause onto the query. Assumes the in (in) operand
     * 
     * @param key
     *            The parameter to search for
     * @param values
     *            The value(s) to search for
     * @return This
     */
    public HazardEventQueryRequest and(String key, Object[] values) {
        this.queryParams.add(new HazardQueryParameter(key, values));
        return this;
    }

    /**
     * Concatenates a clause onto the query. Assumes the in (in) operand
     * 
     * @param key
     *            The parameter to search for
     * @param values
     *            The value(s) to search for
     * @return This
     */
    public HazardEventQueryRequest and(String key, Collection<Object> values) {
        this.queryParams.add(new HazardQueryParameter(key, values
                .toArray(new Object[values.size()])));
        return this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
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

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((queryParams == null) ? 0 : queryParams.hashCode());
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        HazardEventQueryRequest other = (HazardEventQueryRequest) obj;
        if (queryParams == null) {
            if (other.queryParams != null)
                return false;
        } else if (!queryParams.equals(other.queryParams))
            return false;
        return true;
    }
}

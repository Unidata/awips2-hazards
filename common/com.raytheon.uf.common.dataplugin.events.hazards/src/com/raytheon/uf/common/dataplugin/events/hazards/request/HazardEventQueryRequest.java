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
package com.raytheon.uf.common.dataplugin.events.hazards.request;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.HazardEventManager.Include;
import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.registry.HazardEventServicesUtil.IQueryParameterKeyGenerator;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

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
 * Aug 20, 2015 6895      Ben.Phillippe Routing registry requests through
 *                                      request server
 * May 03, 2016 18193     Ben.Phillippe Replication of Hazard VTEC Records.
 * May 06, 2016 18202     Robert.Blum   Changes for operational mode.
 * Feb 16, 2017 29138     Chris.Golden  Revamped to slim down the responses to
 *                                      these requests so that the former do
 *                                      not carry extra serialized objects
 *                                      with them that are not needed.
 * Oct 02, 2017 38506     Chris.Golden  Moved common elements into a new
 *                                      superclass.
 * </pre>
 * 
 * @author bphillip
 * @version 1.0
 */
@XmlRootElement(name = "HazardEventQueryRequest")
@XmlAccessorType(XmlAccessType.NONE)
@DynamicSerialize
public class HazardEventQueryRequest
        extends HazardQueryRequest<HazardEventQueryRequest> {

    // Private Variables

    /**
     * Which categories of hazard events to include in the response.
     */
    @XmlElement
    @DynamicSerializeElement
    private Include include = Include.HISTORICAL_AND_LATEST_EVENTS;

    /**
     * Flag indicating, if <code>true</code>, that only the size of the list of
     * hazard events that would otherwise be requested is needed, not the list
     * of events themselves.
     */
    @XmlElement
    @DynamicSerializeElement
    private boolean sizeOnlyRequired = false;

    // Public Constructors

    /**
     * Create a new empty request that will include both historical and latest
     * events in the response. If another inclusion type is desired,
     * {@link #setInclude(Include)} may be invoked. If only the size of the set
     * of events being requested is needed,
     * {@link #setSizeOnlyRequired(boolean)} may be invoked.
     * <p>
     * <strong>Note</strong>: Do not use. This should never be used as it may
     * incorrectly default the practice flag. It is only included as it is
     * required by JAXB.
     * </p>
     */
    public HazardEventQueryRequest() {
        super();
    }

    /**
     * Create a new empty request that will include both historical and latest
     * events in the response. If another inclusion type is desired,
     * {@link #setInclude(Include)} may be invoked. If only the size of the set
     * of events being requested is needed,
     * {@link #setSizeOnlyRequired(boolean)} may be invoked.
     * 
     * @param practice
     *            Flag indicating whether or not in practice mode.
     */
    public HazardEventQueryRequest(boolean practice) {
        super(practice);
    }

    /**
     * Create a new empty request that will include the specified versions of
     * hazard events, both historical and latest versions. If another inclusion
     * type is desired, {@link #setInclude(Include)} may be invoked. If only the
     * size of the set of events being requested is needed,
     * {@link #setSizeOnlyRequired(boolean)} may be invoked.
     * 
     * @param practice
     *            Flag indicating whether or not in practice mode.
     * @param include
     *            Event versions to be included.
     */
    public HazardEventQueryRequest(boolean practice, Include include) {
        super(practice);
        this.include = include;
    }

    /**
     * Create a new request with the given parameters that will yield both
     * historical and latest versions of events. If another inclusion type is
     * desired, {@link #setInclude(Include)} may be invoked. If only the size of
     * the set of events being requested is needed,
     * {@link #setSizeOnlyRequired(boolean)} may be invoked.
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
    public HazardEventQueryRequest(boolean practice, String key, String operand,
            Object[] values) {
        super(practice, key, operand, values);
    }

    /**
     * Create a new request with the given parameters that will yield both
     * historical and latest versions of events. If another inclusion type is
     * desired, {@link #setInclude(Include)} may be invoked. If only the size of
     * the set of events being requested is needed,
     * {@link #setSizeOnlyRequired(boolean)} may be invoked.
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
    public HazardEventQueryRequest(boolean practice, String key, String operand,
            Object value) {
        super(practice, key, operand, value);
    }

    /**
     * Create a new request with the given parameters that will use the equals (
     * <code>=</code>) operator for comparisons that will yield both historical
     * and latest versions of events. If another inclusion type is desired,
     * {@link #setInclude(Include)} may be invoked. If only the size of the set
     * of events being requested is needed,
     * {@link #setSizeOnlyRequired(boolean)} may be invoked.
     * 
     * @param practice
     *            Flag indicating whether or not in practice mode.
     * @param key
     *            Parameter for which to search.
     * @param value
     *            Value for which to search under the key when testing for
     *            equality.
     */
    public HazardEventQueryRequest(boolean practice, String key, Object value) {
        super(practice, key, value);
    }

    /**
     * Create a new request with the given parameters that will use the in (
     * <code>in</code>) operator for comparisons that will yield both historical
     * and latest versions of events. If another inclusion type is desired,
     * {@link #setInclude(Include)} may be invoked. If only the size of the set
     * of events being requested is needed,
     * {@link #setSizeOnlyRequired(boolean)} may be invoked.
     * 
     * @param practice
     *            Flag indicating whether or not in practice mode.
     * @param key
     *            Parameter for which to search.
     * @param values
     *            Values for which to search under the key when testing for the
     *            "in" relationship.
     */
    public HazardEventQueryRequest(boolean practice, String key,
            Object[] values) {
        super(practice, key, values);
    }

    // Public Methods

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
        return sizeOnlyRequired;
    }

    /**
     * Set the flag indicating whether only the size of the list of hazard
     * events is being requested, or whether the list itself is required.
     * 
     * @param sizeOnlyRequired
     *            Flag indicating, if <code>true</code>, that only the size of
     *            the list of hazard events is requested, or if
     *            <code>false</code>, that the list is required.
     */
    public void setSizeOnlyRequired(boolean sizeOnlyRequired) {
        this.sizeOnlyRequired = sizeOnlyRequired;
    }

    @Override
    public IQueryParameterKeyGenerator getQueryParameterKeyGenerator() {
        return null;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = prime * super.hashCode()
                + ((include == null) ? 0 : include.hashCode());
        return prime * result + (sizeOnlyRequired ? 1 : 0);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (super.equals(obj) == false) {
            return false;
        }
        HazardEventQueryRequest other = (HazardEventQueryRequest) obj;
        if (include == null) {
            if (other.include != null) {
                return false;
            }
        } else if (include.equals(other.include) == false) {
            return false;
        }
        return true;
    }

    // Protected Methods

    @Override
    protected Class<?> getTypeToBeQueried() {
        return HazardEvent.class;
    }
}

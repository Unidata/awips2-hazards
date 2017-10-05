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

import java.util.Collection;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import com.raytheon.uf.common.dataplugin.events.hazards.event.GenericRegistryObject;
import com.raytheon.uf.common.dataplugin.events.hazards.registry.HazardEventServicesUtil.IQueryParameterKeyGenerator;
import com.raytheon.uf.common.dataplugin.events.hazards.registry.slotconverter.GenericRegistryPropertySlotConverter;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;

/**
 * Description: Request object for querying for generic registry objects.
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
@XmlRootElement(name = "GenericObjectQueryRequest")
@XmlAccessorType(XmlAccessType.NONE)
@DynamicSerialize
public class GenericRegistryObjectQueryRequest
        extends HazardQueryRequest<GenericRegistryObjectQueryRequest> {

    // Private Static Constants

    /**
     * Query parameter key generator, used to ensure that any query parameter
     * keys that are not for the {@link GenericRegistryObject#uniqueID} are
     * prefaced with the appropriate prefix.
     */
    private static final IQueryParameterKeyGenerator QUERY_PARAMETER_KEY_GENERATOR = new IQueryParameterKeyGenerator() {

        @Override
        public String getTransformedParameterKey(String queryParameterKey) {
            return GenericRegistryPropertySlotConverter
                    .getSlotNamePrefixForPropertyName(queryParameterKey)
                    + queryParameterKey;
        }
    };

    // Public Constructors

    /**
     * Construct an empty instance for practice mode.
     * <p>
     * <strong>Note</strong>: Do not use. This should never be used as it may
     * incorrectly default the practice flag. It is only included as it is
     * required by JAXB.
     * </p>
     */
    public GenericRegistryObjectQueryRequest() {
        super();
    }

    /**
     * Construct an empty instance.
     * 
     * @param practice
     *            Flag indicating whether or not in practice mode.
     */
    public GenericRegistryObjectQueryRequest(boolean practice) {
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
    public GenericRegistryObjectQueryRequest(boolean practice, String key,
            String operand, Object[] values) {
        super(practice, key, operand, values);
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
    public GenericRegistryObjectQueryRequest(boolean practice, String key,
            String operand, Object value) {
        super(practice, key, operand, value);
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
    public GenericRegistryObjectQueryRequest(boolean practice, String key,
            Object value) {
        super(practice, key, value);
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
    public GenericRegistryObjectQueryRequest(boolean practice, String key,
            Object[] values) {
        super(practice, key, values);
    }

    /**
     * Construct a standard instance with the given parameters.
     * 
     * @param practice
     *            Flag indicating whether or not in practice mode.
     * @param parameters
     *            Parameters of the query.
     */
    public GenericRegistryObjectQueryRequest(boolean practice,
            Collection<HazardQueryParameter> parameters) {
        super(practice, parameters);
    }

    // Public Methods

    @Override
    public IQueryParameterKeyGenerator getQueryParameterKeyGenerator() {
        return QUERY_PARAMETER_KEY_GENERATOR;
    }

    // Protected Methods

    @Override
    protected Class<?> getTypeToBeQueried() {
        return GenericRegistryObject.class;
    }
}

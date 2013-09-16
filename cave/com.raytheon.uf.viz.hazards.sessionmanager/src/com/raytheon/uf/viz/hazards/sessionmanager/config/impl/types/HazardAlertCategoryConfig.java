/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package com.raytheon.uf.viz.hazards.sessionmanager.config.impl.types;

import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.google.common.collect.Lists;
import com.raytheon.uf.common.serialization.ISerializableObject;
import com.raytheon.uf.viz.hazards.sessionmanager.impl.HazardType;

/**
 * Description: Alert criteria configuration per user defined category such as
 * "Conv Long Term". {@link HazardType}s are partitioned into these categories.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 19, 2013   1325     daniel.s.schaffer@noaa.gov      Initial creation
 * 
 * </pre>
 * 
 * @author daniel.s.schaffer@noaa.gov
 * @version 1.0
 */
@XmlAccessorType(XmlAccessType.NONE)
public class HazardAlertCategoryConfig implements ISerializableObject {

    /**
     * For example, "Conv Long Term"
     */
    @XmlElement
    private String category;

    @XmlElement
    private Set<HazardType> hazardTypes;

    @XmlElement
    private List<HazardEventExpirationAlertConfigCriterion> criteria;

    @SuppressWarnings("unused")
    private HazardAlertCategoryConfig() {

    }

    public HazardAlertCategoryConfig(String category) {
        this.category = category;
    }

    /**
     * @param hazardType
     * @return true if the category includes it else false
     */
    public boolean containsHazardType(HazardType hazardType) {
        return hazardTypes.contains(hazardType);
    }

    /**
     * @param the
     *            hazardType
     * @return the criteria corresponding to this hazardType
     */
    public List<HazardEventExpirationAlertConfigCriterion> getCriteria(
            HazardType hazardType) {
        if (!hazardTypes.contains(hazardType)) {
            throw new IllegalArgumentException("No criteria for hazardType "
                    + hazardType);
        }
        return Lists.newArrayList(criteria);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    /**
     * @param hazardTypes
     *            the hazardTypes to set
     */
    void setHazardTypes(Set<HazardType> hazardTypes) {
        this.hazardTypes = hazardTypes;
    }

    /**
     * @param configuration
     *            the configuration to set
     */
    void setConfiguration(List<HazardEventExpirationAlertConfigCriterion> criteria) {
        this.criteria = criteria;
    }

}

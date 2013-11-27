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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Description: Configuration for Hazard Services alerts.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 19, 2013   1325    daniel.s.schaffer@noaa.gov      Initial creation
 * Nov 20, 2013   2159     daniel.s.schaffer@noaa.gov Now interoperable with DRT
 * 
 * </pre>
 * 
 * @author daniel.s.schaffer@noaa.gov
 * @version 1.0
 */
@XmlRootElement(name = "hazardAlerts")
@XmlAccessorType(XmlAccessType.NONE)
public class HazardAlertsConfig {

    @XmlElement
    private HazardEventExpirationAlertsConfig eventExpiration;

    public HazardEventExpirationAlertsConfig getEventExpirationConfig() {
        return eventExpiration;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    void setEventExpirationConfig(
            HazardEventExpirationAlertsConfig eventExpirationConfig) {
        this.eventExpiration = eventExpirationConfig;
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

}

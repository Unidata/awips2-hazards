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

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.google.common.collect.Lists;
import com.raytheon.uf.common.colormap.Color;
import com.raytheon.uf.common.time.util.TimeUtil;

/**
 * Description: Alert configuration alerts based on hazard expiration time.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 19, 2013   1325    daniel.s.schaffer@noaa.gov      Initial creation
 * 
 * </pre>
 * 
 * @author daniel.s.schaffer@noaa.gov
 * @version 1.0
 */
@XmlAccessorType(XmlAccessType.NONE)
public class HazardEventExpirationAlertConfigCriterion {

    public enum Units {
        HOURS("hours"), MINUTES("minutes"), SECONDS("seconds"), PERCENT(
                "percent_completed");

        private final String displayValue;

        /**
         * @return the displayValue
         */
        public String getDisplayValue() {
            return displayValue;
        }

        private Units(String displayValue) {
            this.displayValue = displayValue;
        }

        @Override
        public String toString() {
            return displayValue;
        }

        public Long toMillisConversion() {
            switch (fromString(displayValue)) {

            case HOURS:
                return TimeUtil.MILLIS_PER_HOUR;

            case MINUTES:
                return TimeUtil.MILLIS_PER_MINUTE;

            case SECONDS:
                return TimeUtil.MILLIS_PER_SECOND;

            default:
                throw new IllegalArgumentException("Unexpected case value");

            }

        }

        public static Units fromString(String value) {
            for (Units units : values()) {
                if (units.displayValue.equals(value)) {
                    return units;
                }
            }
            throw new IllegalArgumentException("Unexpected Units value: "
                    + value);
        }
    }

    public enum Manifestation {

        SPATIAL("Spatial"), CONSOLE("Console"), POPUP("Popup");

        private final String displayValue;

        /**
         * @return the displayValue
         */
        public String getDisplayValue() {
            return displayValue;
        }

        private Manifestation(String displayValue) {
            this.displayValue = displayValue;
        }

        @Override
        public String toString() {
            return displayValue;
        }

        public static Manifestation fromString(String value) {
            for (Manifestation manfistation : values()) {
                if (manfistation.displayValue.equals(value)) {
                    return manfistation;
                }
            }
            throw new IllegalArgumentException(
                    "Unexpected Manifestation value: " + value);
        }

    }

    @XmlElement
    private String name;

    @XmlElement
    private Long expirationTime;

    @XmlElement
    private String units;

    @XmlElement
    private List<String> manifestations;

    @XmlElement
    private Color color;

    @XmlElement
    private boolean isBold;

    @XmlElement
    private boolean isBlinking;

    @XmlElement
    private boolean isItalic;

    /**
     * Interesting point. fontSize only makes sense for count-down timers in the
     * spatial display. So in the ideal code design world, there would be a
     * config class just for spatial display timers that includes this field.
     * However, then that class has to extend a base count-down timer class.
     * That's problematic when using JAXB because the XML ends up containing
     * references to a class name.
     */
    @XmlElement
    private int fontSize;

    @SuppressWarnings("unused")
    private HazardEventExpirationAlertConfigCriterion() {

    }

    public HazardEventExpirationAlertConfigCriterion(String name, Units units,
            Set<Manifestation> manifestations, long expirationTime,
            Color color, boolean isBold, boolean isItalic, boolean isBlinking) {
        this.name = name;
        this.units = units.toString();
        this.manifestations = Lists.newArrayList();
        for (Manifestation manifestation : manifestations) {
            this.manifestations.add(manifestation.toString());
        }
        this.expirationTime = expirationTime;
        this.color = color;
        this.isBold = isBold;
        this.isItalic = isItalic;
        this.isBlinking = isBlinking;
    }

    public Long getMillisBeforeExpiration() {
        return expirationTime * getUnits().toMillisConversion();
    }

    public String getName() {
        return name;
    }

    public Set<Manifestation> getManifestations() {
        Set<Manifestation> result = EnumSet.noneOf(Manifestation.class);
        for (String manifestation : manifestations) {
            result.add(Manifestation.fromString(manifestation));
        }
        return result;
    }

    public Units getUnits() {
        return Units.fromString(units);
    }

    public Color getColor() {
        return color;
    }

    public boolean isBold() {
        return isBold;
    }

    public boolean isBlinking() {
        return isBlinking;
    }

    public boolean isItalic() {
        return isItalic;
    }

    public int getFontSize() {
        return fontSize;
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

    public Long getExpirationTime() {
        return expirationTime;
    }

    void setExpirationTime(Long expirationTime) {
        this.expirationTime = expirationTime;
    }

    void setUnits(Units units) {
        this.units = units.toString();
    }

}

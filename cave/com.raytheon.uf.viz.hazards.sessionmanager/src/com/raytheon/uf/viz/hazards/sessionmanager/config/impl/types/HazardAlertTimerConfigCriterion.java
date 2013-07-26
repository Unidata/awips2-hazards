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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.raytheon.uf.common.colormap.Color;

/**
 * Description: Alert configuration for a count-down timer that appears in the
 * Hazard Services console.
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
public class HazardAlertTimerConfigCriterion {

    public enum Units {
        HOURS("hours"), MINUTES("minutes"), SECONDS("seconds");

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

    public enum Location {

        SPATIAL("Spatial"), CONSOLE("Console"), SPATIAL_AND_CONSOLE(
                "Spatial and Console");

        private final String displayValue;

        /**
         * @return the displayValue
         */
        public String getDisplayValue() {
            return displayValue;
        }

        private Location(String displayValue) {
            this.displayValue = displayValue;
        }

        @Override
        public String toString() {
            return displayValue;
        }

        public static Location fromString(String value) {
            for (Location location : values()) {
                if (location.displayValue.equals(value)) {
                    return location;
                }
            }
            throw new IllegalArgumentException("Unexpected Location value: "
                    + value);
        }

    }

    @XmlElement
    private String name;

    @XmlElement
    private String units;

    @XmlElement
    private String location;

    @XmlElement
    private Long millisBeforeExpiration;

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
    private HazardAlertTimerConfigCriterion() {

    }

    public HazardAlertTimerConfigCriterion(String name, Units units,
            Location location, long millisBeforeExpiration, Color color,
            boolean isBold, boolean isItalic, boolean isBlinking) {
        this.name = name;
        this.units = units.toString();
        this.location = location.toString();
        this.millisBeforeExpiration = millisBeforeExpiration;
        this.color = color;
        this.isBold = isBold;
        this.isItalic = isItalic;
        this.isBlinking = isBlinking;
    }

    public Long getMillisBeforeExpiration() {
        return millisBeforeExpiration;
    }

    public String getName() {
        return name;
    }

    public Location getLocation() {
        return Location.fromString(location);
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

}

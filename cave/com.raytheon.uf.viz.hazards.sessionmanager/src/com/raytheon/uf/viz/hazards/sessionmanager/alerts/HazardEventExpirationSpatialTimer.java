/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package com.raytheon.uf.viz.hazards.sessionmanager.alerts;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.raytheon.uf.common.colormap.Color;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.types.HazardAlertTimerConfigCriterion;

/**
 * Description: A {@link HazardEventAlert} that is a count-down timer in the
 * spatial display.
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
public class HazardEventExpirationSpatialTimer extends HazardEventAlert {

    private final HazardAlertTimerConfigCriterion alertCriterion;

    public HazardEventExpirationSpatialTimer(String eventID,
            HazardAlertTimerConfigCriterion alertCriterion) {
        super(eventID);
        this.alertCriterion = alertCriterion;
    }

    public Color getColor() {
        return alertCriterion.getColor();
    }

    public boolean isBold() {
        return alertCriterion.isBold();
    }

    public boolean isBlinking() {
        return alertCriterion.isBlinking();
    }

    public boolean isItalic() {
        return alertCriterion.isItalic();
    }

    public int getFontSize() {
        return alertCriterion.getFontSize();
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

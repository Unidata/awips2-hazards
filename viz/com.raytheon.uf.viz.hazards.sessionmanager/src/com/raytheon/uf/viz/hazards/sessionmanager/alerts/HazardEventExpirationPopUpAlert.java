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

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardServicesEventIdUtil;
import com.raytheon.uf.common.time.SimulatedTime;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.types.HazardEventExpirationAlertConfigCriterion;
import com.raytheon.viz.core.mode.CAVEMode;

/**
 * Description: A {@link IHazardEventExpirationAlert} that is a pop-up alert.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 09, 2013   1325     daniel.s.schaffer@noaa.gov      Initial creation
 * Nov 20, 2013   2159     daniel.s.schaffer@noaa.gov Now interoperable with DRT
 * May 31, 2016   19141    bkowal      Round expiration time like the console does.
 * Feb 16, 2017   28708    Chris.Golden Changed to take issue site ID in constructor
 *                                      to allow displayable event identifier to be
 *                                      built.
 * </pre>
 * 
 * @author daniel.s.schaffer@noaa.gov
 * @version 1.0
 */
public class HazardEventExpirationPopUpAlert extends HazardEventExpirationAlert
        implements IHazardEventExpirationAlert {

    private SimulatedTime systemTime = SimulatedTime.getSystemTime();

    private final String issueSiteId;

    public HazardEventExpirationPopUpAlert(String eventID,
            HazardEventExpirationAlertConfigCriterion alertCriterion,
            String issueSiteId) {
        super(eventID, alertCriterion);
        this.issueSiteId = issueSiteId;
    }

    public String getText() {
        StringBuilder sb = new StringBuilder();
        sb.append(
                String.format("A product generated by event %s will expire in ",
                        HazardServicesEventIdUtil
                                .getInstance(CAVEMode.OPERATIONAL
                                        .equals(CAVEMode.getMode()) == false)
                                .getDisplayId(getEventID(), issueSiteId)));

        /*
         * Place endTimeMillis on the other side of the minute to compensate for
         * rounding
         */
        long expirationMillis = getHazardExpiration().getTime()
                + TimeUnit.MINUTES.toMillis(1L);
        Long timeBeforeExpiration = expirationMillis - systemTime.getMillis();
        Long days = timeBeforeExpiration / TimeUtil.MILLIS_PER_DAY;
        if (days >= 1) {
            sb.append(String.format("%d to %d days", days, days + 1));
        } else {
            Long hours = timeBeforeExpiration / TimeUtil.MILLIS_PER_HOUR;
            if (hours >= 1) {
                Long diff = timeBeforeExpiration
                        - hours * TimeUtil.MILLIS_PER_HOUR;
                Long minutes = diff / TimeUtil.MILLIS_PER_MINUTE;
                sb.append(String.format("%d hours and %d minutes", hours,
                        minutes));
            } else {
                Long minutes = timeBeforeExpiration
                        / TimeUtil.MILLIS_PER_MINUTE;
                if (minutes >= 1) {
                    sb.append(String.format("%d minutes", minutes));
                } else {
                    Long seconds = timeBeforeExpiration
                            / TimeUtil.MILLIS_PER_SECOND;
                    seconds = Math.max(seconds, 0);
                    sb.append(String.format("%d seconds", seconds));
                }
            }
        }
        return sb.toString();
    }

    void setSystemTime(SimulatedTime systemTime) {
        this.systemTime = systemTime;
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

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

import java.util.Date;

import com.raytheon.uf.common.colormap.Color;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.types.HazardEventExpirationAlertConfigCriterion;

/**
 * Description: Basic implementation of a {@link IHazardEventExpirationAlert}
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 09, 2013   1325     daniel.s.schaffer@noaa.gov      Initial creation
 * Jun 18, 2015  7307      Chris.Cody  Added Hazard End time for requested Time Remaining calculation
 * 
 * </pre>
 * 
 * @author daniel.s.schaffer@noaa.gov
 * @version 1.0
 */
public class HazardEventExpirationAlert extends HazardEventAlert implements
        IHazardEventExpirationAlert {

    protected final HazardEventExpirationAlertConfigCriterion alertCriterion;

    private Date hazardExpiration;

    private Date hazardEnd;

    public HazardEventExpirationAlert(String eventID,
            HazardEventExpirationAlertConfigCriterion alertCriterion) {
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

    @Override
    public Date getHazardExpiration() {
        return hazardExpiration;
    }

    @Override
    public void setHazardExpiration(Date hazardExpiration) {
        this.hazardExpiration = hazardExpiration;
    }

    @Override
    public Date getHazardEnd() {
        return hazardEnd;
    }

    @Override
    public void setHazardEnd(Date hazardEnd) {
        this.hazardEnd = hazardEnd;
    }

    @Override
    public Long getMillisBeforeExpiration() {
        return alertCriterion.getMillisBeforeExpiration();
    }

}
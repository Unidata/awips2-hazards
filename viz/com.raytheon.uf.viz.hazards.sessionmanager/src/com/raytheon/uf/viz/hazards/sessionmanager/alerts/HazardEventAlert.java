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

/**
 * Description: The basic {@link IHazardEventAlert}
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 19, 2013   1325     daniel.s.schaffer@noaa.gov      Initial creation
 * Aug 03, 2015   8836     Chris.Cody   Changes for a configurable Event Id
 * Feb 16, 2017  28708     Chris.Golden Changed to return raw event identifier.
 * </pre>
 * 
 * @author daniel.s.schaffer@noaa.gov
 * @version 1.0
 */
public class HazardEventAlert extends HazardAlert implements IHazardEventAlert {

    private final String eventID;

    public HazardEventAlert(String eventID) {
        this.eventID = eventID;
    }

    @Override
    public String getEventID() {
        return eventID;
    }

}

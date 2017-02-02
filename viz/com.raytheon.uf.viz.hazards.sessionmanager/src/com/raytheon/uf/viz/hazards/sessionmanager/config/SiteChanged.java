/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Evaluation & Decision Support Branch (EDS)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package com.raytheon.uf.viz.hazards.sessionmanager.config;

import com.raytheon.uf.viz.hazards.sessionmanager.originator.IOriginator;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.OriginatedSessionNotification;

/**
 * Description: Notification indicating that the current site has changed.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Dec 16, 2016   15556    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class SiteChanged extends OriginatedSessionNotification {

    // Private Variables

    /**
     * New site identifier.
     */
    private final String siteIdentifier;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param siteIdentifier
     *            New site identifier.
     * @param originator
     *            Originator of the change.
     */
    public SiteChanged(String siteIdentifier, IOriginator originator) {
        super(originator);
        this.siteIdentifier = siteIdentifier;
    }

    // Public Methods

    /**
     * Get the new site identifier.
     * 
     * @return New site identifier.
     */
    public String getSiteIdentifier() {
        return siteIdentifier;
    }
}

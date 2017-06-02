/**
 * This software was developed and / or modified by Raytheon Company,
 * pursuant to Contract DG133W-05-CQ-1067 with the US Government.
 * 
 * U.S. EXPORT CONTROLLED TECHNICAL DATA
 * This software product contains export-restricted data whose
 * export/transfer/disclosure is restricted by U.S. law. Dissemination
 * to non-U.S. persons whether in the United States or abroad requires
 * an export license or other authorization.
 * 
 * Contractor Name:        Raytheon Company
 * Contractor Address:     6825 Pine Street, Suite 340
 *                         Mail Stop B8
 *                         Omaha, NE 68106
 *                         402.291.0100
 * 
 * See the AWIPS II Master Rights File ("Master Rights File.pdf") for
 * further licensing information.
 **/
package com.raytheon.uf.common.dataplugin.events.hazards.registry.services.client;

import com.raytheon.uf.common.dataplugin.events.hazards.registry.services.IHazardEventServices;

/**
 * Web service client for interacting with Hazard Event VTEC Records
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 3, 2016  18193    Ben.Phillippe Initial creation
 * May 06, 2016 18202      Robert.Blum Changes for operational mode.
 * 
 * </pre>
 * 
 * @author bphillip
 * @version 1.0
 */
public class HazardEventServicesSoapClient extends
        AbstractHazardEventServicesSoapClient {

    /** Static singleton operational instance */
    private static IHazardEventServices client;

    /** Static singleton practice instance */
    private static IHazardEventServices practiceClient;

    /**
     * Creates a new HazardEventServicesClient instance
     * 
     * @param practice
     *            True if in practice mode, else false if in operational mode
     */
    private HazardEventServicesSoapClient(boolean practice) {
        super(IHazardEventServices.PATH, IHazardEventServices.NAMESPACE,
                IHazardEventServices.SERVICE_NAME, practice);
    }

    /**
     * Gets the singleton instance
     * 
     * @param practice
     *            True if in practice mode, else false if in operational mode
     * @return The singleton instance
     */
    public static IHazardEventServices getServices(boolean practice) {
        if (practice) {
            if (practiceClient == null) {
                practiceClient = new HazardEventServicesSoapClient(practice)
                        .getPort(IHazardEventServices.class);
            }
            return practiceClient;
        } else if (client == null) {
            client = new HazardEventServicesSoapClient(practice)
                    .getPort(IHazardEventServices.class);
        }
        return client;
    }
}

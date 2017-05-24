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

import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.HazardEventManager;
import com.raytheon.uf.common.dataplugin.events.hazards.registry.services.IHazardEventServices;

/**
 * Web service client for interacting with the registry
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 4, 2015  6895     Ben.Phillippe Finished HS data access refactor
 * 
 * </pre>
 * 
 * @author bphillip
 * @version 1.0
 */
public class HazardEventServicesSoapClient extends
        AbstractHazardEventServicesSoapClient {

    /** Static singleton instance */
    private static IHazardEventServices client;

    /**
     * Creates a new HazardEventServicesClient instance
     * 
     * @param mode
     *            The Hazard Services mode, practice or operational
     */
    private HazardEventServicesSoapClient(HazardEventManager.Mode mode) {
        super(IHazardEventServices.PATH, IHazardEventServices.NAMESPACE,
                IHazardEventServices.SERVICE_NAME, mode);
    }

    /**
     * Creates a new HazardEventServicesClient instance
     * 
     * @param practice
     *            True if in practice mode, else false if in operational mode
     */
    private HazardEventServicesSoapClient(boolean practice) {
        this(practice ? HazardEventManager.Mode.PRACTICE
                : HazardEventManager.Mode.OPERATIONAL);
    }

    /**
     * Gets the singleton instance
     * 
     * @param practice
     *            True if in practice mode, else false if in operational mode
     * @return The singleton instance
     */
    public static IHazardEventServices getServices(boolean practice) {
        if (client == null) {
            client = new HazardEventServicesSoapClient(practice)
                    .getPort(IHazardEventServices.class);
        }
        return client;
    }

    /**
     * Gets the singleton instance
     * 
     * @param mode
     *            The mode, practice or operational
     * @return The singleton instance
     */
    public static IHazardEventServices getServices(HazardEventManager.Mode mode) {
        return new HazardEventServicesSoapClient(mode)
                .getPort(IHazardEventServices.class);
    }
}

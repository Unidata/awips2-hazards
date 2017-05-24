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
package com.raytheon.uf.common.dataplugin.hazards.interoperability.registry.services.client;

import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.HazardEventManager;
import com.raytheon.uf.common.dataplugin.events.hazards.registry.services.client.AbstractHazardEventServicesSoapClient;
import com.raytheon.uf.common.dataplugin.hazards.interoperability.registry.services.IHazardEventInteropServices;

/**
 * Web service client for interacting with interoperability objects in the
 * registry
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 4, 2015  6895     Ben.Phillippe Finished HS data access refactor
 * Aug 20, 2015 6895     Ben.Phillippe Routing registry requests through request server
 * 
 * </pre>
 * 
 * @author bphillip
 * @version 1.0
 */
public class HazardEventInteropServicesSoapClient extends
        AbstractHazardEventServicesSoapClient {

    /** Static singleton instance */
    private static IHazardEventInteropServices client;

    /**
     * Creates a new HazardEventInteropServicesClient instance
     * 
     * @param mode
     *            The Hazard Services mode, practice or operational
     */
    private HazardEventInteropServicesSoapClient(HazardEventManager.Mode mode) {
        super(IHazardEventInteropServices.PATH,
                IHazardEventInteropServices.NAMESPACE,
                IHazardEventInteropServices.SERVICE_NAME, mode);
    }

    /**
     * Creates a new HazardEventInteropServicesClient instance
     * 
     * @param practice
     *            True if in practice mode, else false if in operational mode
     */
    private HazardEventInteropServicesSoapClient(boolean practice) {
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
    public static IHazardEventInteropServices getServices(boolean practice) {
        if (client == null) {
            client = new HazardEventInteropServicesSoapClient(practice)
                    .getPort(IHazardEventInteropServices.class);
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
    public static IHazardEventInteropServices getServices(
            HazardEventManager.Mode mode) {
        return new HazardEventInteropServicesSoapClient(mode)
                .getPort(IHazardEventInteropServices.class);
    }

    /**
     * Static method to check if the services used by this implementation are
     * available.
     */
    public static void checkConnectivity() {
        getServices(true).ping();
        getServices(false).ping();
    }
}

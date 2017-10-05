/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Evaluation & Decision Support Branch (EDS)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package com.raytheon.uf.common.dataplugin.events.hazards.registry.services.client;

import com.raytheon.uf.common.dataplugin.events.hazards.registry.services.IGenericRegistryObjectServices;

/**
 * Description: Web service client for interacting with generic objects.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Oct 02, 2017   38506    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class GenericRegistryObjectServicesSoapClient extends
        AbstractHazardEventServicesSoapClient {

    // Private Static Variables

    /**
     * Static singleton operational instance.
     */
    private static IGenericRegistryObjectServices client;

    /**
     * Static singleton practice instance.
     */
    private static IGenericRegistryObjectServices practiceClient;

    // Public Static Methods

    /**
     * Get the appropriate singleton instance.
     * 
     * @param practice
     *            Flag indicating whether or not practice mode is in effect.
     * @return Singleton instance.
     */
    public static IGenericRegistryObjectServices getServices(boolean practice) {
        if (practice) {
            if (practiceClient == null) {
                practiceClient = new GenericRegistryObjectServicesSoapClient(practice)
                        .getPort(IGenericRegistryObjectServices.class);
            }
            return practiceClient;
        } else if (client == null) {
            client = new GenericRegistryObjectServicesSoapClient(practice)
                    .getPort(IGenericRegistryObjectServices.class);
        }
        return client;
    }

    // Private Constructors

    /**
     * Construct a standard instance.
     * 
     * @param practice
     *            Flag indicating whether or not practice mode is in effect.
     */
    private GenericRegistryObjectServicesSoapClient(boolean practice) {
        super(IGenericRegistryObjectServices.PATH, IGenericRegistryObjectServices.NAMESPACE,
                IGenericRegistryObjectServices.SERVICE_NAME, practice);
    }
}

/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Evaluation & Decision Support Branch (EDS)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package com.raytheon.uf.edex.hazards.handlers;

import com.raytheon.uf.common.dataplugin.events.hazards.registry.GenericRegistryObjectResponse;
import com.raytheon.uf.common.dataplugin.events.hazards.registry.services.client.GenericRegistryObjectServicesSoapClient;
import com.raytheon.uf.common.dataplugin.events.hazards.request.StoreGenericRegistryObjectRequest;
import com.raytheon.uf.common.serialization.comm.IRequestHandler;

/**
 * Description: Handler used to process store generic object requests.
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
public class StoreGenericRegistryObjectHandler
        implements IRequestHandler<StoreGenericRegistryObjectRequest> {

    @Override
    public GenericRegistryObjectResponse handleRequest(
            StoreGenericRegistryObjectRequest request) throws Exception {
        return GenericRegistryObjectServicesSoapClient
                .getServices(request.isPractice())
                .storeList(request.getGenericObjects());
    }
}

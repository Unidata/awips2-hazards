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
package com.raytheon.uf.edex.hazards.handlers;

import java.util.Map;

import com.raytheon.uf.common.dataplugin.events.hazards.event.collections.HazardHistoryList;
import com.raytheon.uf.common.dataplugin.events.hazards.requests.HazardRetrieveRequest;
import com.raytheon.uf.common.dataplugin.events.hazards.requests.HazardRetrieveRequestResponse;
import com.raytheon.uf.common.serialization.comm.IRequestHandler;
import com.raytheon.uf.edex.hazards.DatabaseStorageFactory;
import com.raytheon.uf.edex.hazards.IHazardStorageFactory;
import com.raytheon.uf.edex.hazards.IHazardStorageManager;
import com.raytheon.uf.edex.hazards.RegistryStorageFactory;

/**
 * Handler for requesting data from the registry/database, takes
 * {@link HazardRetrieveRequest}
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 8, 2012            mnash     Initial creation
 * 
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */

public class HazardRequestHandler implements
        IRequestHandler<HazardRetrieveRequest> {

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.common.serialization.comm.IRequestHandler#handleRequest
     * (com.raytheon.uf.common.serialization.comm.IServerRequest)
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Object handleRequest(HazardRetrieveRequest request) throws Exception {
        IHazardStorageManager manager = null;
        IHazardStorageFactory factory = request.isPractice() ? new DatabaseStorageFactory()
                : new RegistryStorageFactory();
        manager = factory.getStorageManager();
        HazardRetrieveRequestResponse response = new HazardRetrieveRequestResponse();
        Map<String, HazardHistoryList> events = manager.retrieve(request
                .getFilters());
        response.setEvents(events);
        return response;
    }
}

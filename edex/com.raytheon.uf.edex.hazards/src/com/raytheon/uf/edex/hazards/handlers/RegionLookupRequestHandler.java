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

import com.raytheon.uf.common.dataplugin.events.hazards.requests.RegionLookupRequest;
import com.raytheon.uf.common.localization.region.RegionLookup;
import com.raytheon.uf.common.serialization.comm.IRequestHandler;

/**
 * Handler that allows python to query EDEX with a given site and returns the
 * region.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 06, 2014 4952       Robert.Blum Initial creation
 * 
 * </pre>
 * 
 * @author Robert.Blum
 * @version 1.0
 */

public class RegionLookupRequestHandler implements
        IRequestHandler<RegionLookupRequest> {

    @Override
    public Object handleRequest(RegionLookupRequest request) throws Exception {
        request.setRegion(RegionLookup.getWfoRegion(request.getSite()));
        return request;
    }

}

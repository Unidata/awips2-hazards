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
package com.raytheon.uf.edex.productgen;

import com.raytheon.uf.common.hazards.productgen.data.HazardSiteDataRequest;
import com.raytheon.uf.common.hazards.productgen.data.HazardSiteDataResponse;
import com.raytheon.uf.common.serialization.comm.IRequestHandler;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.edex.hazards.servicebackup.HazardSiteDataProcessor;

/**
 * This class receives Spring requests from CAVE: Hazard Services to import or
 * export Site specific Hazard Services Localization data.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 14, 2015 3743       Chris.Cody  Initial creation
 * Nov 23, 2015 3743       Robert.Blum Only handles exports.
 * 
 * </pre>
 * 
 * @author Chris.Cody
 * @version 1.0
 */

public class HazardSiteDataHandler implements
        IRequestHandler<HazardSiteDataRequest> {

    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(HazardSiteDataHandler.class);

    /**
     * 
     */
    public HazardSiteDataHandler() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.common.serialization.comm.IRequestHandler#handleRequest
     * (com.raytheon.uf.common.serialization.comm.IServerRequest)
     */
    @Override
    public HazardSiteDataResponse handleRequest(HazardSiteDataRequest request)
            throws Exception {

        HazardSiteDataProcessor hazardSiteDataProcessor = new HazardSiteDataProcessor();

        String resultString = null;

        String siteId = request.getSiteId();
        statusHandler
                .info("HazardSiteDataHandler: EXPORT Hazard Services configuration for: "
                        + siteId);
        resultString = hazardSiteDataProcessor
                .exportApplicationSiteData(siteId);

        HazardSiteDataResponse hazardSiteDataResponse = new HazardSiteDataResponse();
        if ((resultString != null) && (resultString.isEmpty() == false)) {
            hazardSiteDataResponse.setErrorMessage(resultString);
        }

        return hazardSiteDataResponse;
    }
}

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

import java.util.HashMap;
import java.util.Map;

import com.raytheon.uf.common.dataplugin.events.hazards.registry.HazardEventResponse;
import com.raytheon.uf.common.dataplugin.events.hazards.request.GetRegistryInfoRequest;
import com.raytheon.uf.common.registry.ebxml.RegistryUtil;
import com.raytheon.uf.common.security.encryption.AESEncryptor;
import com.raytheon.uf.common.serialization.comm.IRequestHandler;

/**
 * 
 * Handler used to process requests for registry connection information
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 20, 2015 6895     Ben.Phillippe Routing registry requests through request server
 * Mar 14, 2016 16534      mduff       Update for new AESEncryptor.
 * </pre>
 * 
 * @author bphillip
 * @version 1.0
 */
public class GetRegistryInfoHandler
        implements IRequestHandler<GetRegistryInfoRequest> {

    @Override
    public HazardEventResponse handleRequest(GetRegistryInfoRequest request)
            throws Exception {
        AESEncryptor encryptor = new AESEncryptor(
                System.getProperty("edex.security.encryption.key"));
        HazardEventResponse response = HazardEventResponse.create();
        Map<String, String> properties = new HashMap<String, String>();
        properties.put(GetRegistryInfoRequest.REGISTRY_URL_KEY,
                RegistryUtil.LOCAL_REGISTRY_ADDRESS);
        properties.put(GetRegistryInfoRequest.REGISTRY_USER_KEY,
                System.getProperty("edex.security.auth.user"));
        properties.put(GetRegistryInfoRequest.REGISTRY_USER_PASSWORD_KEY,
                encryptor.decrypt(
                        System.getProperty("edex.security.auth.password")));
        response.setPayload(properties);
        return response;
    }
}

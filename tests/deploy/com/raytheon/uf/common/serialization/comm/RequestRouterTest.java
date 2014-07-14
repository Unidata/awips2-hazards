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
package com.raytheon.uf.common.serialization.comm;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;

import com.raytheon.uf.common.auth.RequestConstants;
import com.raytheon.uf.common.registry.RegistryConstants;
import com.raytheon.uf.common.util.DeployTestProperties;
import com.raytheon.uf.common.util.registry.RegistryException;
import com.raytheon.uf.edex.auth.RemoteServerRequestRouter;

/**
 * Test {@link RequestRouter}.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 14, 2012 1286       djohnson     Initial creation
 * Mar 05, 2013 1754       djohnson     Test that infinite loop for request server does not occur.
 * Jul 08, 2013 2106       djohnson     Add setDeployInstance().
 * 
 * </pre>
 * 
 * @author djohnson
 * @version 1.0
 */

public class RequestRouterTest {

    private static final String SERVICE1_KEY = "server1Key";

    private static final String SERVICE2_KEY = "server2Key";

    private final IRequestRouter requestServerRouter = mock(IRequestRouter.class);

    private final IRequestRouter server1Router = mock(IRequestRouter.class);

    private final IRequestRouter server2Router = mock(IRequestRouter.class);

    private final IServerRequest serverRequest = mock(IServerRequest.class);

    /**
     * Configures the {@link RequestRouter} to route to deployed EDEX instances.
     */
    public static void setDeployInstance() {
        try {
            RequestRouterTest.clearRegistry();

            final DeployTestProperties deployTestProperties = DeployTestProperties
                    .getInstance();

            final RemoteServerRequestRouter requestRouter = new RemoteServerRequestRouter(
                    deployTestProperties.getRequestServer());
            final RemoteServerRequestRouter dataDeliveryRouter = new RemoteServerRequestRouter(
                    deployTestProperties.getDataDeliveryServer());
            RequestRouterTest.register(
                    RegistryConstants.EBXML_REGISTRY_SERVICE,
                    dataDeliveryRouter);
            RequestRouterTest.register(RequestConstants.REQUEST_SERVER,
                    requestRouter);
        } catch (RegistryException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Registers a router for the specified server key.
     * 
     * @param key
     *            the key
     * @param router
     *            the router
     * @throws RegistryException
     *             on error
     */
    public static void register(String key, IRequestRouter router)
            throws RegistryException {
        RequestRouter.getRouterRegistry().register(key, router);
    }

    /**
     * Clears the router registry.
     */
    public static void clearRegistry() {
        RequestRouter.getRouterRegistry().clear();
    }
}

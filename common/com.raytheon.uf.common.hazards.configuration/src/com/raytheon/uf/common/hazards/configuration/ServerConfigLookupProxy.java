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
package com.raytheon.uf.common.hazards.configuration;

/**
 * Provides generic access to the product-specific
 * {@link IServerConfigLookupWrapper} for retrieving localization information.
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Dec 2, 2016  26624      bkowal      Initial creation
 *
 * </pre>
 *
 * @author bkowal
 */

public final class ServerConfigLookupProxy {

    private static ServerConfigLookupProxy INSTANCE;

    private final IServerConfigLookupWrapper serverConfigLookupWrapper;

    protected ServerConfigLookupProxy(
            final IServerConfigLookupWrapper serverConfigLookupWrapper) {
        this.serverConfigLookupWrapper = serverConfigLookupWrapper;
    }

    public static synchronized void initInstance(
            final IServerConfigLookupWrapper serverConfigLookupWrapper) {
        INSTANCE = new ServerConfigLookupProxy(serverConfigLookupWrapper);
    }

    public static synchronized ServerConfigLookupProxy getInstance() {
        return INSTANCE;
    }

    public IServerConfigLookupWrapper getServerConfigLookupWrapper() {
        return serverConfigLookupWrapper;
    }
}
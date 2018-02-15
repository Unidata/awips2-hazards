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
package com.raytheon.uf.edex.hazards.configuration;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.hazards.configuration.IServerConfigLookupWrapper;
import com.raytheon.uf.edex.core.EDEXUtil;

/**
 * Edex-side implementation of a {@link IServerConfigLookupWrapper}. Utilizes
 * environment variables to retrieve information about the localization server
 * and site.
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 21, 2017 28470      kbisanz     Initial creation
 * Mar 31, 2017 30536      kbisanz     Implement getServiceId()
 *
 * </pre>
 *
 * @author kbisanz
 */
public class EdexServerConfigLookupWrapper
        implements IServerConfigLookupWrapper {

    private static final String HOST_PORT_REGEX = "^http[s]?:\\/\\/(.+):([0-9]+)(\\/.*)?$";

    private static final Pattern HOST_PORT_PATTERN = Pattern
            .compile(HOST_PORT_REGEX);

    private static final int HOST_GROUP = 1;

    private static final int PORT_GROUP = 2;

    public EdexServerConfigLookupWrapper() {
    }

    private String extractCurrentHostPort(final int group) {
        String httpServer = System.getenv("HTTP_SERVER");
        if (httpServer == null) {
            return null;
        }

        final Matcher matcher = HOST_PORT_PATTERN.matcher(httpServer);
        if (!matcher.matches() || matcher.groupCount() < PORT_GROUP) {
            return null;
        }

        return matcher.group(group);
    }

    @Override
    public String getSite() {
        return EDEXUtil.getEdexSite();
    }

    @Override
    public String getHost() {
        return extractCurrentHostPort(HOST_GROUP);
    }

    @Override
    public String getPort() {
        return extractCurrentHostPort(PORT_GROUP);
    }

    @Override
    public String getServiceId() {
        return HazardConstants.RequestServiceId.DEFAULT.getValue();
    }
}

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
package gov.noaa.gsd.viz.hazards.display;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.raytheon.uf.common.hazards.configuration.IServerConfigLookupWrapper;
import com.raytheon.uf.viz.core.localization.LocalizationManager;

/**
 * Viz-side implementation of a {@link IServerConfigLookupWrapper}. Utilizes
 * {@link LocalizationManager} to retrieve information about the localization
 * server and site.
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

public class VizServerConfigLookupWrapper
        implements IServerConfigLookupWrapper {

    private static final String HOST_PORT_REGEX = "^http[s]?:\\/\\/(.+):([0-9]+)(\\/.*)?$";

    private static final Pattern HOST_PORT_PATTERN = Pattern
            .compile(HOST_PORT_REGEX);

    private static final int HOST_GROUP = 1;

    private static final int PORT_GROUP = 2;

    public VizServerConfigLookupWrapper() {
    }

    private String extractCurrentHostPort(final int group) {
        final Matcher matcher = HOST_PORT_PATTERN.matcher(
                LocalizationManager.getInstance().getLocalizationServer());
        if (!matcher.matches() || matcher.groupCount() < PORT_GROUP) {
            return null;
        }

        return matcher.group(group);
    }

    @Override
    public String getSite() {
        return LocalizationManager.getInstance().getCurrentSite();
    }

    @Override
    public String getHost() {
        return extractCurrentHostPort(HOST_GROUP);
    }

    @Override
    public String getPort() {
        return extractCurrentHostPort(PORT_GROUP);
    }
}
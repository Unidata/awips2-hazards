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
package com.raytheon.uf.common.dataplugin.events.hazards.event;

import com.raytheon.uf.common.dataplugin.events.hazards.registry.services.client.HazardEventRequestServices;

/**
 * Create UNIQUE Hazard Services for Event ID values that may be shared between
 * users, sites, etc.
 * 
 * <pre>
 * Hazard Id format is "ZZ-YYYY-SSS...-III...-999999"
 * ZZ     : 2 Character Application ID
 * YYYY   : 4 Character Year
 * SSS... : 3+ Character Site ID
 * III... : 3+ Character Issuing Site ID
 * 999999 : Up to 6 Numeric Character Serial Event ID *
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer      Description
 * ------------ ---------- ------------- --------------------------
 * Mar 13, 2017 28708      mpduff        Initial creation
 * Apr 19, 2017 33275      mduff         Changed how event ids are used, no
 *                                       more leading zeroes.
 * Jan 23, 2018 20739      Chris.Golden  Changed to allow for site identifiers
 *                                       of more than three characters.
 * Jun 06, 2018 15561      Chris.Golden  Added code to handle temporary event
 *                                       identifiers.
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */
public class HazardServicesEventIdUtil
        extends AbstractHazardServicesEventIdUtil {

    // Public Static Methods

    public static synchronized AbstractHazardServicesEventIdUtil getInstance(
            boolean practiceMode) {
        if (practiceMode) {
            if (practiceInstance == null) {
                practiceInstance = new HazardServicesEventIdUtil(practiceMode);
            }
            return practiceInstance;
        } else {
            if (operationalInstance == null) {
                operationalInstance = new HazardServicesEventIdUtil(
                        practiceMode);
            }
            return operationalInstance;
        }
    }

    // Private Constructors

    private HazardServicesEventIdUtil(boolean practiceMode) {
        setRequestService(HazardEventRequestServices.getServices(practiceMode));
    }

    // Public Methods

    @Override
    public String getDisplayId(String fullId, String issueSiteId) {
        return getDisplayId(fullId, getIdDisplayType(), issueSiteId);
    }

    @Override
    public String getDisplayId(String fullId, IdDisplayType idDisplayType,
            String siteId) {

        if (fullId == null) {
            return null;
        }

        if ((idDisplayType == IdDisplayType.ALWAYS_FULL)
                || isTemporaryEventID(fullId)) {
            return fullId;
        }

        /*
         * Full identifier string is not returned so split into parts.
         */
        String[] parts = fullId.split(SEP);

        if (idDisplayType == IdDisplayType.ALWAYS_SITE
                || idDisplayType == IdDisplayType.ONLY_SERIAL) {
            return getDisplayId(parts, idDisplayType);
        }

        String curAppId = APP_ID;
        String curYear = getCurrentYear();
        String idAppId = parts[APP_IDX];
        String idYear = parts[YEAR_IDX];
        String siteString = siteId + SEP + siteId;
        String idSiteString = parts[SITE_IDX] + SEP + parts[ISSUE_IDX];
        if ((!curAppId.equals(idAppId)) || (!siteString.equals(idSiteString))
                || (!curYear.equals(idYear))) {
            if (IdDisplayType.FULL_ON_DIFF == idDisplayType) {
                return fullId;
            } else if (IdDisplayType.PROG_ON_DIFF == idDisplayType) {
                if (!curAppId.equals(idAppId)) {
                    return fullId;
                } else if (!siteString.equals(idSiteString)) {
                    return parts[SITE_IDX] + SEP + parts[ISSUE_IDX] + SEP
                            + parts[SERIAL_IDX];
                } else if (!curYear.equals(idYear)) {
                    return parts[YEAR_IDX] + SEP + parts[SITE_IDX] + SEP
                            + parts[ISSUE_IDX] + SEP + parts[SERIAL_IDX];
                }
            }
        } else {
            return parts[SERIAL_IDX];
        }

        return fullId;
    }

    // Private Methods

    private String getDisplayId(String[] parts, IdDisplayType idDisplayType) {
        String id = null;
        if (IdDisplayType.ALWAYS_SITE == idDisplayType) {
            id = parts[SITE_IDX] + SEP + parts[ISSUE_IDX] + SEP
                    + parts[SERIAL_IDX];
        } else if (IdDisplayType.ONLY_SERIAL == idDisplayType) {
            id = parts[SERIAL_IDX];
        }

        return id;
    }
}

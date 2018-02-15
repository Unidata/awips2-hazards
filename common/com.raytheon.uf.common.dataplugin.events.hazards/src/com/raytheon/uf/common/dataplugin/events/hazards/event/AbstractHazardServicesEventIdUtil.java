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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.raytheon.uf.common.dataplugin.events.hazards.registry.HazardEventServiceException;
import com.raytheon.uf.common.dataplugin.events.hazards.registry.services.client.HazardEventRequestServices;
import com.raytheon.uf.common.time.SimulatedTime;

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
 * 999999 : 6 Numeric Character Serial Event ID
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 03, 2015 8836       Chris.Cody  Initial creation
 * Aug 20, 2015 6895     Ben.Phillippe Routing registry requests through
 *                                     request server
 * Oct 27, 2015 12077    Ben.Phillippe Removed unnecessary status message
 * Jan 20, 2016 14980      kbisanz     Fixed string compare issue in
 *                                     getDisplayId() causing the full ID to
 *                                     be returned
 * Jun 07, 2016 15561     Chris.Golden Removed constraint upon site identifier
 *                                     for hazard identifiers (used to require
 *                                     it to be three characters).
 * Jan 26, 2017 21635     Roger.Ferrel Added issue site id.
 * Feb 16, 2017 28708     mduff        Refactored this class.
 * Mar 13, 2017 28708     mduff        Changed to be abstract base class.
 * Apr 19, 2017 33275     mduff        Changed how event ids are used, no more
 *                                     leading zeroes.
 * Jan 23, 2018 20739     Chris.Golden Changed to allow for site identifiers
 *                                     of more than three characters.
 * Jun 06, 2018 15561     Chris.Golden Added methods to handle temporary event
 *                                     identifiers.
 * </pre>
 * 
 * @author Chris.Cody
 * @version 1.0
 */
public abstract class AbstractHazardServicesEventIdUtil {

    // Public Enumerated Types

    /**
     * Enumeration of the types of Hazard Event ID to display. This does not
     * affect the type of Id that is generated. This only affects the return
     * value of <Event Class>.getDisplayEventId(...).
     * 
     * <pre>
     * ALWAYS_FULL    Always returns the full ID (default)
     * FULL_ON_DIFF   Always return the full id if there is any difference
     *                between the current settings for: APP_ID, siteId, issueSiteId,
     *                or year.
     * PROG_ON_DIFF   Build a progressively larger Display Id based on the largest
     *                difference between the Current Settings and the given Event Id.
     *                Different App = Full ID:            "ZZ-YYYY-SSS...-III...-999999"
     *                Different Year = Year Level ID:     "YYYY-SSS...-III...-999999"
     *                (Same App)
     *                Different Site = Site Level ID:     "SSS...-III...-999999"
     *                (Same App, Same Year)
     *                Same App, Site and Year: Serial ID: "999999"
     * ALWAYS_SITE    Always display the Site Id, Issue Site Id, and Serial Id:
     *                "SSS...-III...-999999"
     * ONLY_SERIAL    Only displays the Serial ID despite other differences.
     * </pre>
     */
    public enum IdDisplayType {
        ALWAYS_FULL, FULL_ON_DIFF, PROG_ON_DIFF, ALWAYS_SITE, ONLY_SERIAL
    }

    // Protected Static Constants

    protected static final String APP_ID = "HZ";

    protected static final String SEP = "-";

    protected static final String INVALID_APP_ID = "QQ";

    protected static final String INVALID_YEAR = "0000";

    protected static final String INVALID_SITE_ID = "QQQ";

    protected static final String INVALID_ISSUE_SITE_ID = "III";

    protected static final String INVALID_SERIAL_ID = "999999";

    protected static final int APP_IDX = 0;

    protected static final int YEAR_IDX = 1;

    protected static final int SITE_IDX = 2;

    protected static final int ISSUE_IDX = 3;

    protected static final int SERIAL_IDX = 4;

    // Private Static Constants

    private static final int SITE_ID_MIN_LEN = 3;

    private static final int ISSUE_SITE_ID_MIN_LEN = 3;

    private static final SimpleDateFormat YEAR_FORMAT = new SimpleDateFormat(
            "yyyy");

    private static final String INVALID_FULL_ID = INVALID_APP_ID + SEP
            + INVALID_YEAR + SEP + INVALID_SITE_ID + SEP + INVALID_ISSUE_SITE_ID
            + SEP + INVALID_SERIAL_ID;

    private static final String TEMPORARY_EVENT_ID_PREFIX = "--==##";

    private static final String TEMPORARY_EVENT_ID_SUFFIX = "##==--";

    private static final Pattern TEMPORARY_EVENT_ID_PATTERN = Pattern.compile(
            TEMPORARY_EVENT_ID_PREFIX + "([0-9]+)" + TEMPORARY_EVENT_ID_SUFFIX);

    // Protected Static Variables

    protected static AbstractHazardServicesEventIdUtil operationalInstance;

    protected static AbstractHazardServicesEventIdUtil practiceInstance;

    // Private Variables

    private IdDisplayType idDisplayType = IdDisplayType.ALWAYS_FULL;

    private HazardEventRequestServices requestService;

    private long temporaryEventIdCounter = 0L;

    // Public Static Methods

    /**
     * Get the string representing an invalid full Event Id.
     * 
     * @return String representing invalid full Event Id.
     */
    public static String getInvalidFullId() {
        return INVALID_FULL_ID;
    }

    /**
     * Parse Serial Id portion from Full Event Id
     * 
     * @param fullId
     *            Full Hazard Event Id "ZZ-YYYY-SSS...-III...-999999"
     * @return Serial Id sub String
     */
    public static String getSerialIdFromFullId(String fullId) {
        return getSection(fullId, SERIAL_IDX);
    }

    // Private Static Methods

    /**
     * Get the section of the specified full identifier at the specified index.
     * 
     * @param fullId
     *            Full identifier from which to extract the section.
     * @param idx
     *            Index of the section to extract.
     * @return Section.
     */
    private static String getSection(String fullId, int idx) {
        return fullId.split(SEP)[idx];
    }

    // Public Methods

    /**
     * Needed for JUnit Testing.
     * 
     * @param requestService
     */
    public void setRequestService(HazardEventRequestServices requestService) {
        this.requestService = requestService;
    }

    /**
     * Get the ID display type for Hazard Services.
     * 
     * @return ID display type.
     */
    public IdDisplayType getIdDisplayType() {
        return idDisplayType;
    }

    /**
     * Set the ID display type for Hazard Services.
     * 
     * @param newIdDisplayType
     *            New ID display type.
     */
    public void setIdDisplayType(IdDisplayType newIdDisplayType) {
        idDisplayType = newIdDisplayType;
    }

    /**
     * Get a temporary event identifier, used to identify newly created events
     * prior to their addition to a session.
     * 
     * @return Temporary event identifier.
     */
    public String getNewTemporaryEventID() {
        return TEMPORARY_EVENT_ID_PREFIX + temporaryEventIdCounter++
                + TEMPORARY_EVENT_ID_SUFFIX;
    }

    /**
     * Determine whether or not the specified event identifier is a temporary
     * event identifier.
     * 
     * @param identifier
     *            Identifier to be checked.
     * @return <code>true</code> if the identifier is a temporary one,
     *         <code>false</code> otherwise.
     */
    public boolean isTemporaryEventID(String eventId) {
        return TEMPORARY_EVENT_ID_PATTERN.matcher(eventId).matches();
    }

    /**
     * Given the specified string, replace any temporary event identifiers with
     * permanent ones, with the mapping of the latter given by the specified
     * map.
     * 
     * @param str
     *            String for which replacements should occur.
     * @param permanentIdsForTemporaryIds
     *            Map pairing temporary identifiers with the permanent ones
     *            which should replace them.
     * @return String with replacements made.
     */
    public String replaceTemporaryEventIDs(String str,
            Map<String, String> permanentIdsForTemporaryIds) {
        Matcher matcher = TEMPORARY_EVENT_ID_PATTERN.matcher(str);
        StringBuffer buffer = new StringBuffer(str.length() * 2);
        int previousEnd = 0;
        while (matcher.find()) {
            if (matcher.start() > 0) {
                buffer.append(str.substring(previousEnd, matcher.start()));
            }
            String temporaryId = matcher.group();
            String permanentId = permanentIdsForTemporaryIds.get(temporaryId);
            buffer.append(permanentId != null ? permanentId : temporaryId);
            previousEnd = matcher.end();
        }
        if (previousEnd + 1 < str.length()) {
            buffer.append(str.substring(previousEnd));
        }
        return buffer.toString();
    }

    /**
     * Retrieve a Full Event Id String: "ZZ-YYYY-SSS...-III...-999999"
     * 
     * @param eventSiteId
     *            Overriding Site Id value for the Event ID
     * @param eventIssueSiteId
     *            Overriding Issue Site Id value for the Event ID.
     * @return Full Event ID String
     * @throws HazardEventServiceException
     */
    public final synchronized String getNewEventID(String eventSiteId,
            String eventIssueSiteId) throws HazardEventServiceException {
        StringBuilder sb = new StringBuilder();
        if ((eventSiteId == null) || (eventSiteId.length() < SITE_ID_MIN_LEN)) {
            sb.append("Error Invalid Site ID value: ").append(eventSiteId);
        }
        if ((eventIssueSiteId == null)
                || (eventIssueSiteId.length() < ISSUE_SITE_ID_MIN_LEN)) {
            if (sb.length() == 0) {
                sb.append("Error ");
            } else {
                sb.append("and ");
            }
            sb.append("Invalid Issue Site Id: ").append(eventIssueSiteId);
        }
        if (sb.length() == 0) {
            return APP_ID + SEP + getCurrentYear() + SEP + eventSiteId + SEP
                    + eventIssueSiteId + SEP
                    + getNextEventIdNumber(eventSiteId);
        } else {
            throw new HazardEventServiceException(sb.toString());
        }
    }

    /**
     * Parse a Display Id from the given (full) Hazard Id.
     * 
     * @param fullId
     *            Full Hazard Event Id "ZZ-YYYY-SSS...-III...-999999" to check
     * @param issueSiteId
     *            Issue site identifier.
     * @return Display ID.
     */
    public abstract String getDisplayId(String fullId, String issueSiteId);

    /**
     * Parse a Display Id from the given (full) Hazard Id.
     * 
     * @param fullId
     *            Full Hazard Event Id "ZZ-YYYY-SSS...-III...-999999" to check
     * @param idDisplayType
     *            ID display type to be used.
     * @param issueSiteId
     *            Issue site identifier.
     * @return Display ID.
     */
    public abstract String getDisplayId(String fullId,
            IdDisplayType idDisplayType, String siteId);

    // Protected Methods

    /**
     * Get the current year.
     * 
     * @return 4 digit year string.
     */
    protected String getCurrentYear() {
        long systemTimeLong = SimulatedTime.getSystemTime().getMillis();
        Date systemDate = new Date(systemTimeLong);
        return YEAR_FORMAT.format(systemDate);
    }

    /**
     * Parse Year portion from Full Event Id
     * 
     * @param fullId
     *            Full Hazard Event Id "ZZ-YYYY-SSS...-III...-999999"
     * @return Year sub String
     */
    protected String getYearFromFullId(String fullId) {
        return getSection(fullId, YEAR_IDX);
    }

    // Private Methods

    /**
     * Request a new, unique Hazard Event Id Serial Identifier from the
     * HazardEventServices.requestEventId(<siteId>) for the current Site Id.
     * 
     * @param siteId
     * 
     * @return System Unique Hazard Event Id
     */
    private synchronized String getNextEventIdNumber(String siteId)
            throws HazardEventServiceException {
        String queriedEventId = requestService.requestEventId(siteId);
        if ((queriedEventId != null) && (queriedEventId.isEmpty() == false)) {
            return queriedEventId;
        } else {
            throw (new HazardEventServiceException(
                    "HazardEventServices.requestEventId returned a NULL Hazard Event ID"));
        }
    }
}

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

import com.raytheon.uf.common.dataplugin.events.hazards.registry.HazardEventServiceException;
import com.raytheon.uf.common.dataplugin.events.hazards.registry.services.client.HazardEventRequestServices;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.common.time.SimulatedTime;

/**
 * Create UNIQUE Hazard Services for Event ID values that may be shared between
 * users, sites, etc.
 * 
 * <pre>
 * Hazard Id format is "ZZ-YYYY-SSS-999999"
 * ZZ     : 2 Character Application ID
 * YYYY   : 4 Character Year
 * SSS    : 3 Character Site ID
 * 999999 : 6 Numeric Character Serial Event ID
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 03, 2015 8836       Chris.Cody  Initial creation
 *  
 * Aug 20, 2015 6895     Ben.Phillippe Routing registry requests through
 *                                     request server
 * Oct 27, 2015 12077    Ben.Phillippe Removed unnecessary status message
 * Jan 20, 2016 14980      kbisanz     Fixed string compare issue in
 *                                     getDisplayId() causing the full ID to
 *                                     be returned
 * </pre>
 * 
 * @author Chris.Cody
 * @version 1.0
 */
public class HazardServicesEventIdUtil {
    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(HazardServicesEventIdUtil.class);

    /**
     * Enumeration of the types of Hazard Event ID to display. This does not
     * affect the type of Id that is generated. This only affects the return
     * value of <Event Class>.getDisplayEventId(...).
     * 
     * <pre>
     * ALWAYS_FULL    Always returns the full ID (default) 
     * FULL_ON_DIFF   Always return the full id if there is any difference 
     *                between the current settings for: APP_ID, siteId or year.
     * PROG_ON_DIFF   Build a progressively larger Display Id based on the largest
     *                difference between the Current Settings and the given Event Id.
     *                Different App = Full ID             "ZZ-YYYY-SSS-999999"
     *                Different Year = Year Level ID:     "YYYY-SSS-999999"
     *                (Same App)
     *                Different Site = Site Level ID:     "SSS-999999"
     *                (Same App, Same Year)
     *                Same App, Site and Year: Serial ID: "999999"
     * ALWAYS_SITE    Always display the Site Id and Serial Id "SSS-999999"
     * ONLY_SERIAL    Only displays the Serial ID despite other differences.
     */
    public enum IdDisplayType {
        ALWAYS_FULL, FULL_ON_DIFF, PROG_ON_DIFF, ALWAYS_SITE, ONLY_SERIAL
    }

    public static final int FULL_ID_LEN = 18;

    public static final int APP_ID_IDX = 0;

    public static final int APP_ID_LEN = 2;

    public static final int YEAR_IDX = 3;

    public static final int YEAR_LEN = 4;

    public static final int SITE_ID_IDX = 8;

    public static final int SITE_ID_LEN = 3;

    public static final int SERIAL_ID_IDX = 12;

    public static final int SERIAL_ID_LEN = 6;

    public static final String APP_ID = "HZ";

    public static final String SEP = "-";

    public static final String INVALID_APP_ID = "QQ";

    public static final String INVALID_YEAR = "0000";

    public static final String INVALID_SITE_ID = "QQQ";

    public static final String INVALID_SERIAL_ID = "999999";

    public static final String INVALID_FULL_ID = INVALID_APP_ID + SEP
            + INVALID_YEAR + SEP + INVALID_SITE_ID + SEP + INVALID_SERIAL_ID;

    private static final String ZEROS_STRING = "000000";

    private static final int DEFAULT_SERIAL_ID_LEN = 6;

    // Calendar is a LARGE object Date.getYear() is deprecated.
    private static final SimpleDateFormat YEAR_FORMAT = new SimpleDateFormat(
            "yyyy");

    public static boolean ignoreErrorIdValues = true;

    private static IdDisplayType idDisplayType = IdDisplayType.ALWAYS_FULL;

    private static boolean isPracticeMode = true;

    private static String siteId = null;

    /**
     * Set up the Site Id for the Hazard Event Id. This defaults to an Id
     * Display Type of ALWAYS_FULL..
     * 
     * @param newIsPracticeMode
     * @param newSiteId
     *            Hazard Services Site Id
     */
    public static synchronized void setupHazardEventId(
            boolean newIsPracticeMode, String newSiteId)
            throws HazardEventServiceException {
        setupHazardEventId(newIsPracticeMode, newSiteId,
        // IdDisplayType.ALWAYS_FULL);
                IdDisplayType.FULL_ON_DIFF);
    }

    /**
     * Set up the Site Id and Display Type for the Hazard Event Id.
     * 
     * @param newIsPracticeMode
     * @param newSiteId
     *            Hazard Services Site Id
     * @param newIdDisplayType
     *            Hazard Services Event Id Display Type
     */
    public static synchronized void setupHazardEventId(
            boolean newIsPracticeMode, String newSiteId,
            IdDisplayType newIdDisplayType) throws HazardEventServiceException {

        reset();

        isPracticeMode = newIsPracticeMode;
        idDisplayType = newIdDisplayType;

        if (newSiteId != null) {
            isPracticeMode = newIsPracticeMode;
            siteId = newSiteId;
        } else {
            siteId = INVALID_SITE_ID;
            String msg = "Null Site ID set for Hazard Event Id Generator.";
            statusHandler.handle(Priority.CRITICAL, msg);
            throw (new HazardEventServiceException(msg));
        }
    }

    /**
     * Reset internal Values to defaults.
     * 
     * This will not cause the App Id to reset. This will not cause the Serial
     * Id or the data source to reset.
     */
    public static synchronized void reset() {

        statusHandler.handle(Priority.WARN,
                "Resetting Hazard Services Id Data values");
        siteId = INVALID_SITE_ID;
        isPracticeMode = true;
        idDisplayType = IdDisplayType.ALWAYS_FULL;
    }

    public static IdDisplayType getIdDisplayType() {
        return (idDisplayType);
    }

    /**
     * Set the ID display type for Hazard Services.
     * 
     * @param newIdDisplayType
     */
    public static void setIdDisplayType(IdDisplayType newIdDisplayType) {
        idDisplayType = newIdDisplayType;
    }

    /**
     * Get Application Id (HZ) value.
     * 
     * @return Application Id
     */
    public static final String getAppId() {
        return (APP_ID);
    }

    /**
     * Just to be safe get the Current Year every time.
     * 
     * @return 4 digit Year String
     */
    private static String getCurrentYear() {
        long systemTimeLong = SimulatedTime.getSystemTime().getMillis();
        Date systemDate = new Date(systemTimeLong);
        return (YEAR_FORMAT.format(systemDate));
    }

    /**
     * Get Configured Site Id value.
     * 
     * @return Site Id
     */
    public static String getSiteId() {
        if (siteId == null) {
            return (INVALID_SITE_ID);
        }

        return (siteId);
    }

    /**
     * Request a new, unique Hazard Event Id Serial Identifier from the
     * HazardEventServices.requestEventId(<siteId>) for the current Site Id.
     * 
     * 
     * @return System Unique Hazard Event Id
     */
    private static synchronized String getNextEventIdNumber()
            throws HazardEventServiceException {
        String queriedEventId = HazardEventRequestServices.getServices(
                isPracticeMode).requestEventId(siteId);
        if ((queriedEventId != null) && (queriedEventId.isEmpty() == false)) {
            int idLen = queriedEventId.length();
            if (idLen < DEFAULT_SERIAL_ID_LEN) {
                return (ZEROS_STRING.substring(idLen) + queriedEventId);
            } else {
                int startIdx = idLen - DEFAULT_SERIAL_ID_LEN;
                return (queriedEventId.substring(startIdx));
            }
        } else {
            throw (new HazardEventServiceException(
                    "HazardEventServices.requestEventId returned a NULL Hazard Event ID"));
        }
    }

    /**
     * Retrieve a Full Event Id String: "ZZ-YYYY-SSS-999999"
     * 
     * @return Full Event ID String
     */
    public static final synchronized String getNewEventID()
            throws HazardEventServiceException {
        return (getNewEventID(getSiteId()));
    }

    /**
     * Retrieve a Full Event Id String: "ZZ-YYYY-SSS-999999"
     * 
     * @param eventSiteId
     *            Overriding Site Id value for the Event ID
     * @return Full Event ID String
     */
    public static final synchronized String getNewEventID(String eventSiteId)
            throws HazardEventServiceException {
        if ((eventSiteId != null) && (eventSiteId.length() == 3)) {
            return (getAppId() + SEP + getCurrentYear() + SEP + eventSiteId
                    + SEP + getNextEventIdNumber());
        } else {
            throw (new HazardEventServiceException(
                    "Error Invalid Site Id value: "
                            + eventSiteId
                            + " Unable to generate Hazard Services Event Id values."));
        }
    }

    /**
     * Parse App Id portion from Full Event Id
     * 
     * @param fullId
     *            Full Hazard Event Id "ZZ-YYYY-SSS-999999"
     * @return App Id sub String
     */
    public static String getAppIdFromFullId(String fullId) {
        if (fullId != null) {
            if (FULL_ID_LEN == fullId.length()) {
                return (fullId.substring(APP_ID_IDX, (APP_ID_IDX + APP_ID_LEN)));
            }
            if (ignoreErrorIdValues == false) {
                statusHandler.handle(Priority.ERROR, "Invalid Event ID value: "
                        + fullId);
            }
        } else {
            if (ignoreErrorIdValues == false) {
                statusHandler.handle(Priority.ERROR, "NULL Event ID value!");
            }
        }
        return (INVALID_APP_ID);
    }

    /**
     * Parse Year portion from Full Event Id
     * 
     * @param fullId
     *            Full Hazard Event Id "ZZ-YYYY-SSS-999999"
     * @return Year sub String
     */
    public static String getYearFromFullId(String fullId) {
        if (fullId != null) {
            if (FULL_ID_LEN == fullId.length()) {
                return (fullId.substring(YEAR_IDX, (YEAR_IDX + YEAR_LEN)));
            } else {
                if (ignoreErrorIdValues == false) {
                    statusHandler.handle(Priority.ERROR,
                            "Invalid Event ID value: " + fullId);
                }
            }
        } else {
            statusHandler.handle(Priority.ERROR, "NULL Event ID value!");
        }
        return (INVALID_YEAR);
    }

    /**
     * Parse Site Id portion from Full Event Id
     * 
     * @param fullId
     *            Full Hazard Event Id "ZZ-YYYY-SSS-999999"
     * @return Site Id sub String
     */
    public static String getSiteIdFromFullId(String fullId) {
        if (fullId != null) {
            if (FULL_ID_LEN == fullId.length()) {
                return (fullId.substring(SITE_ID_IDX,
                        (SITE_ID_IDX + SITE_ID_LEN)));
            } else {
                if (ignoreErrorIdValues == false) {
                    statusHandler.handle(Priority.ERROR,
                            "Invalid Event ID value: " + fullId);
                }
            }
        } else {
            statusHandler.handle(Priority.ERROR, "NULL Event ID value!");
        }
        return (INVALID_SITE_ID);
    }

    /**
     * Parse Serial Id portion from Full Event Id
     * 
     * @param fullId
     *            Full Hazard Event Id "ZZ-YYYY-SSS-999999"
     * @return Serial Id sub String
     */
    public static String getSerialIdFromFullId(String fullId) {
        if (fullId != null) {
            if (FULL_ID_LEN == fullId.length()) {
                String serial = fullId.substring(SERIAL_ID_IDX,
                        (SERIAL_ID_IDX + SERIAL_ID_LEN));
                return serial.replaceFirst("^0+(?!$)", "");
            } else {
                if (ignoreErrorIdValues == false) {
                    statusHandler.handle(Priority.ERROR,
                            "Invalid Event ID value: " + fullId);
                }
            }
        } else {
            statusHandler.handle(Priority.ERROR, "NULL Event ID value!");
        }
        return (INVALID_SERIAL_ID);
    }

    /**
     * Get the numeric value for the Year portion of the Event Id.
     * 
     * @param fullId
     *            Full Hazard Event Id "ZZ-YYYY-SSS-999999"
     * @return Numeric Year. 0 if the given Event Id is invalid
     */
    public static int getNumericYearFromFullId(String fullId)
            throws NumberFormatException {
        return Integer.parseInt(getYearFromFullId(fullId));
    }

    /**
     * Get the numeric value for the Serial Id portion of the Event Id.
     * 
     * @param fullId
     *            Full Hazard Event Id "ZZ-YYYY-SSS-999999"
     * @return Numeric Serial Id. 999999 if the given Event Id is invalid
     */
    public static int getNumericSerialIdFromFullId(String fullId)
            throws NumberFormatException {
        return Integer.parseInt(getSerialIdFromFullId(fullId));
    }

    /**
     * Check to see if the given it is a Valid ID.
     * 
     * A valid Event Id must be in the form: ZZ-YYYY-SSS-999999 This does not
     * check to see if it is for a valid Application, Site, or Year.
     * 
     * @param fullId
     *            Full Hazard Event Id "ZZ-YYYY-SSS-999999" to check
     * @return true if this is a valid ID
     */
    public static boolean isValidId(String fullId) {
        if (fullId == null) {
            return (false);
        }

        int len = fullId.length();
        if (len != FULL_ID_LEN) {
            return (false);
        }

        char sepChar = SEP.toCharArray()[0];
        char[] charArray = fullId.toCharArray();
        if (Character.isAlphabetic(charArray[0]) == false) {
            return (false);
        }
        if (Character.isAlphabetic(charArray[1]) == false) {
            return (false);
        }
        if (charArray[2] != sepChar) {
            return (false);
        }
        if (Character.isDigit(charArray[3]) == false) {
            return (false);
        }
        if (Character.isDigit(charArray[4]) == false) {
            return (false);
        }
        if (Character.isDigit(charArray[5]) == false) {
            return (false);
        }
        if (Character.isDigit(charArray[6]) == false) {
            return (false);
        }
        if (charArray[7] != sepChar) {
            return (false);
        }
        if (Character.isAlphabetic(charArray[8]) == false) {
            return (false);
        }
        if (Character.isAlphabetic(charArray[9]) == false) {
            return (false);
        }
        if (Character.isAlphabetic(charArray[10]) == false) {
            return (false);
        }
        if (charArray[11] != sepChar) {
            return (false);
        }
        if (Character.isDigit(charArray[12]) == false) {
            return (false);
        }
        if (Character.isDigit(charArray[13]) == false) {
            return (false);
        }
        if (Character.isDigit(charArray[14]) == false) {
            return (false);
        }
        if (Character.isDigit(charArray[15]) == false) {
            return (false);
        }
        if (Character.isDigit(charArray[16]) == false) {
            return (false);
        }
        if (Character.isDigit(charArray[17]) == false) {
            return (false);
        }

        return (true);
    }

    /**
     * Check to see if the given Event ID is for the current Site Id.
     * 
     * @param fullId
     *            Full Hazard Event Id "ZZ-YYYY-SSS-999999" to check
     * @return true if the ID is for the current Site Id
     */
    public static boolean isLocalId(String fullId) {
        if (isValidId(fullId) == true) {
            String idSiteId = getSiteIdFromFullId(fullId);
            if (getSiteId().equals(idSiteId) == true) {
                return (true);
            }
        }
        return (false);
    }

    /**
     * Check to see if the given Event ID is for the present System Time year.
     * 
     * @param fullId
     *            Event Id to check
     * @return true if the ID is for the Current System Time year
     */
    public static boolean isPresentId(String fullId) {
        if (isValidId(fullId) == true) {
            String idYear = getYearFromFullId(fullId);
            if (getCurrentYear().equals(idYear) == true) {
                return (true);
            }
        }
        return (false);
    }

    public static String getDisplayId(String fullId) {
        return (getDisplayId(fullId, idDisplayType));
    }

    /**
     * Parse a Display Id from the given (full) Hazard Id.
     * 
     * @param fullId
     *            Full Hazard Event Id "ZZ-YYYY-SSS-999999" to check
     * @param idDisplayType
     * @return
     */
    public static String getDisplayId(String fullId, IdDisplayType idDisplayType) {

        if ((fullId == null) || (fullId.length() != FULL_ID_LEN)) {
            if (ignoreErrorIdValues == false) {
                statusHandler.handle(Priority.ERROR,
                        "Unable to format Event ID display value. Invalid Event ID value: "
                                + fullId);
            }
            return (fullId);
        }

        if (idDisplayType == IdDisplayType.ALWAYS_FULL) {
            return (fullId);
        }
        if (idDisplayType == IdDisplayType.ALWAYS_SITE) {
            return (fullId.substring(SITE_ID_IDX));
        }
        if (idDisplayType == IdDisplayType.ONLY_SERIAL) {
            return (getSerialIdFromFullId(fullId));
        }

        String curAppId = getAppId();
        String curSiteId = getSiteId();
        String curYear = getCurrentYear();
        String idAppId = getAppIdFromFullId(fullId);
        String idSiteId = getSiteIdFromFullId(fullId);
        String idYear = getYearFromFullId(fullId);
        if ((!curAppId.equals(idAppId)) || (!curSiteId.equals(idSiteId))
                || (!curYear.equals(idYear))) {
            if ((idDisplayType == IdDisplayType.FULL_ON_DIFF)) {
                return (fullId);
            } else if ((idDisplayType == IdDisplayType.PROG_ON_DIFF)) {
                if (!curAppId.equals(idAppId)) {
                    return (fullId.substring(APP_ID_IDX));
                } else if (!curSiteId.equals(idSiteId)) {
                    return (fullId.substring(SITE_ID_IDX));
                } else if (!curYear.equals(idYear)) {
                    return (fullId.substring(YEAR_IDX));
                } else {
                    return (getSerialIdFromFullId(fullId));
                }
            }
        } else {
            return (getSerialIdFromFullId(fullId));
        }

        return (fullId);
    }

}

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
package gov.noaa.gsd.viz.hazards.utilities;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.productgen.validation.util.VtecObject;
import com.raytheon.uf.viz.productgen.validation.util.VtecUtil;

/**
 * Utility class for parsing information out of text products.
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 27, 2017 22308      Robert.Blum Initial creation
 *
 * </pre>
 *
 * @author Robert.Blum
 */

public class ProductParserUtil {

    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(ProductParserUtil.class);

    public static final DateFormat dateFormatter = new SimpleDateFormat(
            "hhmm a z EEE MMM dd yyyy");

    static {
        dateFormatter.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    // Pattern to used to match product issue times like:
    // 1115 AM CST TUE JAN 24 2017
    public static final Pattern datePtrn = Pattern.compile(
            "(\\d{1,2})(\\d{2})\\s(AM|PM)\\s(\\w{3,4})\\s\\w{3}\\s(\\w{3})\\s{1,}(\\d{1,2})\\s(\\d{4})");

    // Pattern to used to match product VTEC lines like:
    // /O.NEW.KOAX.FF.A.0003.170124T1715Z-170125T0200Z/
    public static final Pattern vtecPtrn = Pattern.compile(
            "/[OTEX]\\.([A-Z]{3})\\.[A-Za-z0-9]{4}\\.[A-Z]{2}\\.[WAYSFON]\\.\\d{4}\\.\\d{6}T\\d{4}Z-\\d{6}T\\d{4}Z/");

    /**
     * Parses the vtec strings out of a text product and returns them as vtec
     * objects.
     * 
     * @param product
     * @return
     */
    public static List<VtecObject> getVTECsFromProduct(String product) {
        String[] separatedLines = product.split("\n");
        List<VtecObject> vtecObjs = new ArrayList<>();
        for (String line : separatedLines) {
            Matcher vtecMatcher = vtecPtrn.matcher(line);
            if (vtecMatcher.find()) {
                vtecObjs.add(VtecUtil.parseMessage(line));
            }
        }
        return vtecObjs;
    }

    /**
     * Parses the issue time out of a text product.
     * 
     * @param product
     * 
     * @return the issueTime or null if the issueTime could not be parsed.
     */
    public static Date getIssueTimeFromProduct(String product) {
        String[] separatedLines = product.split("\n");
        for (String line : separatedLines) {
            Matcher dateMatcher = datePtrn.matcher(line);
            if (dateMatcher.find()) {
                String timeString = dateMatcher.group();
                Date issueDate = null;
                try {
                    /*
                     * Adding back in the leading zero if it is missing, so that
                     * the dateFormatter can parse correctly.
                     */
                    if (timeString.substring(3, 4).equals(" ")) {
                        timeString = "0" + timeString;
                    }
                    issueDate = dateFormatter.parse(timeString);
                    return issueDate;
                } catch (ParseException e) {
                    statusHandler
                            .warn("Failed to parse issueTime from product.");
                }
            }
        }
        return null;
    }
}

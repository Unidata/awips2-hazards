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
package com.raytheon.uf.viz.productgen.validation.qc;

import java.util.Calendar;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.time.SimulatedTime;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.viz.productgen.validation.TextWarningConstants;
import com.raytheon.uf.viz.productgen.validation.util.VtecObject;
import com.raytheon.uf.viz.productgen.validation.util.VtecUtil;
import com.raytheon.viz.core.mode.CAVEMode;

/**
 * Imported and integrated from Warn Gen: com.raytheon.viz.texteditor
 * 
 * AWIPS2_baseline/cave/com.raytheon.viz.texteditor/src/com/raytheon
 * /viz/texteditor/qc
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 29, 2015 6617       Chris.Cody  Initial Import. Integrate WarnGen Product Validation.
 * Oct 27, 2015 6617       Robert.Blum Fixes for mixed case validation.
 * May 06, 2016 18202      Robert.Blum Changes for operational/test mode.
 * Jul 14, 2016 20477      Kevin.Bisanz When constructing a Calendar, set timezone
 *                                     before setting millis.
 * Jul 15, 2016 20477      Kevin.Bisanz Use TimeUtil to get Calendar instances.
 * Jul 19, 2016 19926      Kevin.Bisanz Set Calendar.DAY_OF_MONTH when checking UGC line.
 * Aug 17, 2016 20615      Roger.Ferrel Check UGC time stamp for month/year roll over.
 * Feb 14, 2017 28645      Robert.Blum  Third bullet time validation is only done on NEW products.
 * </pre>
 * 
 * @version 1.0
 */
public class TimeConsistentCheck implements IQCCheck {
    private static final Pattern ugcPtrn = Pattern.compile(
            "(((\\w{2}[CZ](\\d{3}-){1,}){1,})|(\\d{3}-){1,})(\\d{2})(\\d{2})(\\d{2})-");

    @Override
    public String runQC(String header, String body, String nnn) {
        String errorMsg = "";

        long simulatedSystemTime = SimulatedTime.getSystemTime().getMillis();

        Matcher m = null;
        VtecObject vtec = VtecUtil.parseMessage(body);

        Calendar calendar = TimeUtil.newGmtCalendar();
        calendar.setTimeInMillis(simulatedSystemTime);

        if (!CAVEMode.OPERATIONAL.equals(CAVEMode.getMode())) {
            if (body.contains(TEST_MESSAGE_LABEL)) {
                body = body.replaceAll(TEST_MESSAGE_LABEL, "");
            }
        }

        if (vtec != null) {
            calendar.add(Calendar.MINUTE, 5);
            if (!vtec.getAction().equals("EXP")
                    && vtec.getEndTime().before(calendar)) {
                errorMsg += "Product has expired or will expire in\nless than 5 minutes. (UGC line)\n";
                return errorMsg;
            }

            // Event ending time vs UGC
            m = ugcPtrn.matcher(body);
            if (m.find()) {
                calendar.setTimeInMillis(simulatedSystemTime);
                int dayOfMonth = Integer.parseInt(m.group(6));
                int hour = Integer.parseInt(m.group(7));
                int minute = Integer.parseInt(m.group(8));
                // Not in UGC timestamp assume same as simulated time.
                int month = calendar.get(Calendar.MONTH);
                int year = calendar.get(Calendar.YEAR);

                // Check for roll over to the next month.
                if (dayOfMonth < calendar.get(Calendar.DAY_OF_MONTH)) {
                    month++;
                    if (month > Calendar.DECEMBER) {
                        month = Calendar.JANUARY;
                        year++;
                    }
                }

                TimeUtil.minCalendarFields(calendar, Calendar.SECOND,
                        Calendar.MILLISECOND);
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                calendar.set(Calendar.HOUR_OF_DAY, hour);
                calendar.set(Calendar.MINUTE, minute);
                calendar.setTimeZone(TimeZone.getTimeZone("GMT"));
                if ((calendar.getTimeInMillis()
                        - vtec.getEndTime().getTimeInMillis()) > (16
                                * TimeUtil.MILLIS_PER_MINUTE)) {
                    errorMsg = "VTEC end time is 15 minutes older\nthan UGC expiration times differ";
                    return errorMsg;
                }
            }

            // Event ending time (second bullet) vs Expiration
            String newBody = body.replaceAll("Until noon", "Until 1200 PM");
            newBody = newBody.replaceAll("Until midnight", "Until 1200 AM");
            m = secondBulletPtrn.matcher(newBody);
            if (m.find()) {
                TimeZone timeZone = TextWarningConstants.timeZoneShortNameMap
                        .get(m.group(4));
                if (timeZone == null) {
                    errorMsg += "Could not determine time zone in second bullet";
                    return errorMsg;
                }
                int am_pm = m.group(3).equals("AM") ? Calendar.AM : Calendar.PM;
                int minute = Integer.parseInt(m.group(2));
                int hour = Integer.parseInt(m.group(1)) == 12 ? 0
                        : Integer.parseInt(m.group(1));

                Calendar secondBulletTime = TimeUtil.newCalendar(timeZone);
                secondBulletTime.setTimeInMillis(simulatedSystemTime);
                if (secondBulletTime.get(Calendar.AM_PM) == Calendar.PM
                        && am_pm == Calendar.AM) {
                    int month = secondBulletTime.get(Calendar.DAY_OF_MONTH);
                    secondBulletTime.set(Calendar.DAY_OF_MONTH, month + 1);
                }
                secondBulletTime.set(Calendar.HOUR, hour);
                secondBulletTime.set(Calendar.MINUTE, minute);
                secondBulletTime.set(Calendar.SECOND, 0);
                secondBulletTime.set(Calendar.AM_PM, am_pm);

                calendar.setTimeInMillis(secondBulletTime.getTimeInMillis());

                if (calendar.get(Calendar.HOUR_OF_DAY) != vtec.getEndTime()
                        .get(Calendar.HOUR_OF_DAY)) {
                    errorMsg += "VTEC and bullet expiration times differ,\nor no * Until line found.\n";
                    return errorMsg;
                }

            } else if (!nnn.equalsIgnoreCase("SVS")
                    && !nnn.equalsIgnoreCase("FFS")
                    && !nnn.equalsIgnoreCase("FLW")
                    && !nnn.equalsIgnoreCase("FLS")
                    && !nnn.equalsIgnoreCase("MWS")) {
                errorMsg += "VTEC and bullet expiration times differ,\nor no * Until line found.\n";
                return errorMsg;
            }

            // Event beginning time vs ending time
            if (vtec.getEndTime().before(vtec.getStartTime())) {
                errorMsg += "VTEC ending time is earlier than\nVTEC beginning time.\n";
                return errorMsg;
            }
        }

        m = thirdBulletPtrn.matcher(body);
        if (m.find()) {
            TimeZone timeZone = TextWarningConstants.timeZoneShortNameMap
                    .get(m.group(4));
            if (timeZone == null) {
                errorMsg += "Could not determine time zone in third bullet";
                return errorMsg;
            }
            int am_pm = m.group(3).equals("AM") ? Calendar.AM : Calendar.PM;
            int minute = Integer.parseInt(m.group(2));
            int hour = Integer.parseInt(m.group(1));
            if (hour == 12) {
                hour = 0;
            }

            /*
             * When constructing a Calendar, set the time zone before setting
             * the time. This ensures that the internal data members are set
             * correctly. Otherwise time in millis is set (setting all
             * year/monthy/day/hour/minute/second variables), the time zone is
             * set (not changing the internal variables), then only
             * hours/minutes/am_pm (with values taken in the set time zone) are
             * set below. Therefore the day is from the GMT time and hour from
             * the current time zone. See the JavaDoc for Calendar regarding
             * set(f, value) and when internal variables are updated.
             */
            Calendar thirdBullettime = TimeUtil.newCalendar(timeZone);
            thirdBullettime.setTimeInMillis(simulatedSystemTime);

            thirdBullettime.set(Calendar.HOUR, hour);
            thirdBullettime.set(Calendar.MINUTE, minute);
            thirdBullettime.set(Calendar.SECOND, 0);
            thirdBullettime.set(Calendar.AM_PM, am_pm);

            Calendar issuetimeCalendar = TimeUtil.newGmtCalendar();
            issuetimeCalendar.setTimeInMillis(simulatedSystemTime);
            long issuetime = issuetimeCalendar.getTimeInMillis();
            if (thirdBullettime.getTimeInMillis() - 60 * 1000 > issuetime) {
                errorMsg += "Event time is later than the MND\nissue time.\n";
            } else if (issuetime - thirdBullettime.getTimeInMillis() > 15 * 60
                    * 1000
                    && HazardConstants.NEW_ACTION.equals(vtec.getAction())) {
                errorMsg += "The event time is more than 15 minutes\nearlier than the issue time.\n";
            }
        }

        return errorMsg;
    }
}

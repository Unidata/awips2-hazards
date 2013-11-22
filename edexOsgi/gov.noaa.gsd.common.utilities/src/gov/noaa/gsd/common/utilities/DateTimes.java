package gov.noaa.gsd.common.utilities;

import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeZone;

/**
 * Description: Provides access to JODA date/time functionality with the
 * implicit assumption that the time zone is UTC.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 04, 2013            daniel.s.schaffer Initial creation
 * Nov 25, 2013    2336    Chris.Golden      Moved to gov.noaa.gsd.common.utilities,
 *                                           and removed unused methods and variables.
 * </pre>
 * 
 * @author daniel.s.schaffer
 * @version 1.0
 */
public class DateTimes {

    public DateTimes() {
    }

    public DateTime newDateTime(Date date) {
        return new DateTime(date, DateTimeZone.UTC);
    }

    public DateTime newDateTime(DateTime dateTime) {
        return new DateTime(dateTime, DateTimeZone.UTC);
    }

    public DateTime newDateTime(long timeInMillis) {
        return new DateTime(timeInMillis, DateTimeZone.UTC);
    }

    public DateTime newDateTime(int timeInSeconds) {
        long timeInMillis = ((long) timeInSeconds)
                * DateTimeConstants.MILLIS_PER_SECOND;
        return new DateTime(timeInMillis, DateTimeZone.UTC);
    }

    public DateTime newDateTime(Object object) {
        return new DateTime(object, DateTimeZone.UTC);
    }
}

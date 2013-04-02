package gov.noaa.gsd.common.utilities;

import java.util.Date;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * Description: Provides access to JODA date/time functionality with the
 * implicit assumption that the time zone is UTC.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 04, 2013            daniel.s.schaffer      Initial creation
 * 
 * </pre>
 * 
 * @author daniel.s.schaffer
 * @version 1.0
 */
public class DateTimes {

    public final static String DATE_FORMAT = "YYYY-MM-dd";

    private final static String LOCAL_TIME_FORMAT = "HHmm'Z'";

    private final DateTimeZone timeZone;

    private final DateTimeFormatter dateFormatter = DateTimeFormat
            .forPattern(DATE_FORMAT);

    private final DateTimeFormatter localTimeFormatter = DateTimeFormat
            .forPattern(LOCAL_TIME_FORMAT);

    private final static String DATE_TIME_FORMAT = DATE_FORMAT + "_"
            + LOCAL_TIME_FORMAT;

    public DateTimes() {
        timeZone = DateTimeZone.UTC;
    }

    public DateTimes(DateTimeZone timeZone) {
        this.timeZone = timeZone;
    }

    public DateTimeZone timeZone() {
        return timeZone;
    }

    public DateTime parse(String pattern, String dateTimeString) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern(pattern)
                .withZone(timeZone);
        return formatter.parseDateTime(dateTimeString);
    }

    public DateTime parseDate(String dateString) {
        return parse(DATE_FORMAT, dateString);
    }

    public LocalTime parseLocalTime(String pattern, String localTimeString) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern(pattern)
                .withZone(timeZone);
        return formatter.parseLocalTime(localTimeString);
    }

    public String printDate(String pattern, DateMidnight dateMidnight) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern(pattern)
                .withZone(timeZone);
        return formatter.print(dateMidnight);
    }

    public String printDate(DateMidnight dateMidnight) {
        return printDate(DATE_FORMAT, dateMidnight);
    }

    public String printDateTime(DateMidnight dateMidnight) {
        return printDate(DATE_TIME_FORMAT, dateMidnight);
    }

    public String printDateTime(DateTime dateTime) {
        return print(DATE_TIME_FORMAT, dateTime);
    }

    public String printDate(DateTime dateTime) {
        return print(DATE_FORMAT, dateTime);
    }

    public String print(String pattern, DateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern(pattern)
                .withZone(timeZone);
        return formatter.print(dateTime);
    }

    public String printLocalTime(String pattern, LocalTime localTime) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern(pattern)
                .withZone(timeZone);
        return formatter.print(localTime);
    }

    public DateTime newDateTime() {
        return new DateTime(DateTimeZone.UTC);
    }

    public DateTime newDateTime(int year, int monthOfYear, int dayOfMonth,
            int hourOfDay, int minuteOfHour) {
        return new DateTime(year, monthOfYear, dayOfMonth, hourOfDay,
                minuteOfHour, DateTimeZone.UTC);
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

    public DateTime dateWithTime(DateTime date, LocalTime time) {
        DateTime newDate = new DateTime(date, timeZone);
        return newDate.withTime(time.getHourOfDay(), time.getMinuteOfHour(),
                time.getSecondOfMinute(), 0);

    }

    public DateTime dateWithTime(DateMidnight date, LocalTime time) {
        return dateWithTime(date.toDateTime(), time);
    }

    public DateTime dateWithTime(DateTime date, LocalTime time,
            Duration duration) {
        DateTime newDate = dateWithTime(date, time);
        return newDate.plus(duration);
    }

    public DateTime dateWithTime(DateMidnight date, LocalTime time,
            Duration duration) {
        return dateWithTime(date.toDateTime(), time, duration);
    }

    public DateTime newDateTime(int year, int month, int day) {
        return new DateTime(year, month, day, 0, 0, 0, 0, timeZone);
    }

    public DateMidnight newDateMidnight() {
        return new DateMidnight(new DateTime());
    }

    public DateMidnight newDateMidnight(int year, int month, int day) {
        return new DateMidnight(year, month, day, timeZone);
    }

    public DateMidnight newDateMidnight(DateMidnight dateMidnight) {
        return new DateMidnight(dateMidnight, timeZone);
    }

    public DateMidnight newDateMidnight(DateTime dateTime) {
        return new DateMidnight(new DateMidnight(dateTime), timeZone);
    }

    public DateMidnight newDateMidnight(String dateString) {
        DateTime dateTime = dateFormatter.parseDateTime(dateString);
        return new DateMidnight(new DateMidnight(dateTime), timeZone);
    }

    public DateMidnight newDateMidnight(java.sql.Date date) {
        return new DateMidnight(date, timeZone);
    }

    public DateMidnight newDateMidnight(java.util.Date date) {
        return new DateMidnight(date, timeZone);
    }

    public DateMidnight newDateMidnight(Object dateMidnight) {
        return new DateMidnight(dateMidnight, timeZone);
    }

    public LocalTime newLocalTime(int hour, int minute) {
        LocalTime time = new LocalTime(0, timeZone);
        return time.withHourOfDay(hour).withMinuteOfHour(minute);
    }

    public LocalTime newLocalTime(DateTime dateTime) {
        DateTime result = new DateTime(dateTime, timeZone);
        return result.toLocalTime();
    }

    public LocalTime newLocalTime(LocalTime localTime) {
        return new LocalTime(localTime, timeZone);
    }

    public LocalTime newLocalTime(Object localTime) {
        return new LocalTime(localTime, timeZone);
    }

    public LocalTime newLocalTime(String localTimeString) {
        DateTime dt = localTimeFormatter.parseDateTime(localTimeString);
        LocalTime lt = dt.toLocalTime();
        return lt;
    }

    public Duration newDuration(String durationString) {
        Long duration = Long.parseLong(durationString);
        return new Duration(duration);
    }

    public Duration durationSeconds(int seconds) {
        return new Duration(secondsToMillis(seconds));
    }

    public Duration durationSeconds(String seconds) {
        return new Duration(secondsToMillis(Integer.parseInt(seconds)));
    }

    public Duration durationMinutes(int minutes) {
        return new Duration(minutesToMillis(minutes));
    }

    public Duration durationMinutes(String minutes) {
        return new Duration(minutesToMillis(Integer.parseInt(minutes)));
    }

    public Duration durationHours(int hours) {
        return new Duration(hoursToMillis(hours));
    }

    public Duration durationHours(String hours) {
        return new Duration(hoursToMillis(Integer.parseInt(hours)));
    }

    public int secondsToMillis(int seconds) {
        return seconds * DateTimeConstants.MILLIS_PER_SECOND;
    }

    public int minutesToMillis(int minutes) {
        return minutes * DateTimeConstants.MILLIS_PER_MINUTE;
    }

    public int hoursToMillis(int minutes) {
        return minutes * DateTimeConstants.MILLIS_PER_HOUR;
    }

    public LocalTime toLocalTime(DateTime dateTime) {
        DateTime result = new DateTime(dateTime, timeZone);
        return result.toLocalTime();
    }

}

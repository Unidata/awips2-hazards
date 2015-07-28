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
 */
package com.raytheon.uf.common.hazards.ihfs;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This is a utility class for parsing and formatting IHFS column Data.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 28, 2015 8839       Chris.Cody  Initial Creation
 * 
 * </pre>
 * 
 * @author Chris.Cody
 * @version 1.0
 */
public class ColumnDataUtil {

    private static final String emptyString = "";

    private static final String regexPatternString = "[\"|\']";

    private static final Matcher removeQuotesMatcher = Pattern.compile(
            regexPatternString).matcher(emptyString);

    protected static SimpleDateFormat dateFormat = IhfsConstants
            .getDateFormat();

    public static final String formatDataToSqlColumn(
            TableColumnData columnData, Object dataObject)
            throws IhfsDatabaseException {
        String parsedColString = IhfsConstants.NULL_SQL_STRING;
        if (IhfsUtil.isRecognizedDataType(dataObject) == true) {
            if (IhfsUtil.areCompatible(columnData, dataObject)) {
                parsedColString = parseData(columnData, dataObject);
            } else {
                String msg = "Data " + dataObject.getClass().getName()
                        + " value <" + dataObject.toString()
                        + "> is not compatible with Column "
                        + columnData.getQualifiedColumnName() + " of type "
                        + columnData.getColumnType();
                throw (new IhfsDatabaseException(msg));
            }
        } else {
            String msg = "Unrecognized Data Type "
                    + dataObject.getClass().getName() + " value <"
                    + dataObject.toString() + "> mismatch Column "
                    + columnData.getQualifiedColumnName() + " of type "
                    + columnData.getColumnType();
            throw (new IhfsDatabaseException(msg));
        }

        return (parsedColString);
    }

    /**
     * Parse input data object into the Database Table Column Type.
     * 
     * @param columnData
     *            Table Column Metadata
     * @param dataObject
     *            Data Value object
     * @return Parsed data for SQL String
     * @throws IhfsDatabaseException
     */
    public static final String parseData(TableColumnData columnData,
            Object dataObject) throws IhfsDatabaseException {
        // At this point if dataObject is an actual null value; it is an error
        if (IhfsUtil.areCompatible(columnData, dataObject) == true) {
            String columnType = columnData.getColumnType();
            int columnLen = columnData.getLength();

            switch (columnType) {
            case TableColumnData.STRING_TYPE:
                return parseToString(dataObject, columnLen);
            case TableColumnData.INTEGER_TYPE:
                return parseToIntegerString(dataObject, columnLen);
            case TableColumnData.FLOAT_TYPE:
                return parseToFloatString(dataObject, columnLen);
            case TableColumnData.TIMESTAMP_TYPE:
                return parseToTimestampString(dataObject, columnLen);
            case TableColumnData.LONG_TYPE:
                return parseToLongString(dataObject, columnLen);
            case TableColumnData.DOUBLE_TYPE:
                return parseToDoubleString(dataObject, columnLen);
            case TableColumnData.DATE_TYPE:
                return parseToDateString(dataObject, columnLen);
            default:
                String msg = "Unrecognized Data Type "
                        + dataObject.getClass().getName() + " value <"
                        + dataObject.toString() + "> mismatch Column "
                        + columnData.getQualifiedColumnName() + " of type "
                        + columnData.getColumnType();
                throw (new IhfsDatabaseException(msg));

            }
        } else {
            String msg = "Incompatible Types: "
                    + dataObject.getClass().getName() + " value <"
                    + dataObject.toString() + "> mismatch Column "
                    + columnData.getQualifiedColumnName() + " of type "
                    + columnData.getColumnType();
            throw (new IhfsDatabaseException(msg));
        }
    }

    /**
     * Parse object to a String value with a column length.
     * 
     * Do not add quotation marks.
     * 
     * @param dataObject
     *            Object to parse into a String value
     * @param columnLen
     *            maximum string length
     * @return
     */
    public static final String parseToString(Object dataObject, int columnLen) {
        String parsedValue = null;

        if (dataObject != null) {
            parsedValue = dataObject.toString();
            if (IhfsConstants.NULL_VALUE.equals(parsedValue) == true) {
                parsedValue = IhfsConstants.NULL_VALUE;
            } else {
                parsedValue = stripString(dataObject.toString());
                if ((columnLen > 0) && (parsedValue.length() > columnLen)) {
                    parsedValue = parsedValue.substring(0, columnLen);
                }
            }
        } else {
            parsedValue = IhfsConstants.NULL_SQL_STRING;
        }

        return (parsedValue);
    }

    /**
     * Parse object to an Integer value.
     * 
     * Note that this does check to see if the input dataObject CAN be parsed.
     * However, the string representation (if given) remains as given.
     * 
     * @param dataObject
     *            Object to parse into a String value
     * @param columnLen
     *            maximum string length (ignored)
     * @return
     */
    public static final String parseToIntegerString(Object dataObject,
            int columnLen) throws IhfsDatabaseException {
        String parsedValue = null;
        if (dataObject != null) {
            if (dataObject instanceof Number) {
                Number dataNumber = (Number) dataObject;
                parsedValue = String.valueOf(dataNumber.intValue());
            } else if (dataObject instanceof String) {
                try {
                    parsedValue = dataObject.toString();
                    parsedValue = parsedValue.trim();
                    Integer temp = Integer.parseInt(parsedValue);
                } catch (Exception ex) {
                    String msg = "Error: Unable to convert input object "
                            + dataObject + " of class "
                            + dataObject.getClass().getName()
                            + " into Column Type "
                            + TableColumnData.INTEGER_TYPE;
                    throw (new IhfsDatabaseException(msg, ex));
                }
            }
        } else {
            // Data object is null
            parsedValue = IhfsConstants.NULL_SQL_STRING;
        }
        return (parsedValue);
    }

    /**
     * Parse object to a Float value.
     * 
     * Note that this does check to see if the input dataObject CAN be parsed.
     * However, the string representation (if given) remains as given.
     * 
     * @param dataObject
     *            Object to parse into a String value
     * @param columnLen
     *            maximum string length (ignored)
     * @return
     */
    public static final String parseToFloatString(Object dataObject,
            int columnLen) throws IhfsDatabaseException {
        String parsedValue = null;
        if (dataObject != null) {
            if (dataObject instanceof Number) {
                Number dataNumber = (Number) dataObject;
                parsedValue = String.valueOf(dataNumber.floatValue());
            } else if (dataObject instanceof String) {
                try {
                    parsedValue = dataObject.toString();
                    parsedValue = parsedValue.trim();
                    Float temp = Float.parseFloat(parsedValue);
                } catch (Exception ex) {
                    String msg = "Error: Unable to convert input object "
                            + dataObject + " of class "
                            + dataObject.getClass().getName()
                            + " into Column Type " + TableColumnData.FLOAT_TYPE;
                    throw (new IhfsDatabaseException(msg, ex));
                }
            }
        } else {
            // Data object is null
            parsedValue = IhfsConstants.NULL_SQL_STRING;
        }
        return (parsedValue);
    }

    /**
     * Parse object to a Long value.
     * 
     * Note that this does check to see if the input dataObject CAN be parsed.
     * However, the string representation (if given) remains as given.
     * 
     * @param dataObject
     *            Object to parse into a String value
     * @param columnLen
     *            maximum string length (ignored)
     * @return
     */
    public static final String parseToLongString(Object dataObject,
            int columnLen) throws IhfsDatabaseException {
        String parsedValue = null;
        if (dataObject != null) {
            if (dataObject instanceof Number) {
                Number dataNumber = (Number) dataObject;
                parsedValue = String.valueOf(dataNumber.longValue());
            } else if (dataObject instanceof String) {
                try {
                    parsedValue = dataObject.toString();
                    parsedValue = parsedValue.trim();
                    Long temp = Long.parseLong(parsedValue);
                } catch (Exception ex) {
                    String msg = "Error: Unable to convert input object "
                            + dataObject + " of class "
                            + dataObject.getClass().getName()
                            + " into Column Type " + TableColumnData.LONG_TYPE;
                    throw (new IhfsDatabaseException(msg, ex));
                }
            }
        } else {
            // Data object is null
            parsedValue = IhfsConstants.NULL_SQL_STRING;
        }
        return (parsedValue);
    }

    /**
     * Parse object to a Double value.
     * 
     * Note that this does check to see if the input dataObject CAN be parsed.
     * However, the string representation (if given) remains as given.
     * 
     * @param dataObject
     *            Object to parse into a String value
     * @param columnLen
     *            maximum string length (ignored)
     * @return
     */
    public static final String parseToDoubleString(Object dataObject,
            int columnLen) throws IhfsDatabaseException {
        String parsedValue = null;
        if (dataObject != null) {
            if (dataObject instanceof Number) {
                Number dataNumber = (Number) dataObject;
                parsedValue = String.valueOf(dataNumber.doubleValue());
            } else if (dataObject instanceof String) {
                try {
                    parsedValue = dataObject.toString();
                    parsedValue = parsedValue.trim();
                    Double temp = Double.parseDouble(parsedValue);
                } catch (Exception ex) {
                    String msg = "Error: Unable to convert input object "
                            + dataObject + " of class "
                            + dataObject.getClass().getName()
                            + " into Column Type "
                            + TableColumnData.DOUBLE_TYPE;
                    throw (new IhfsDatabaseException(msg, ex));
                }
            }
        } else {
            // Data object is null
            parsedValue = IhfsConstants.NULL_SQL_STRING;
        }
        return (parsedValue);
    }

    /**
     * Parse object to a String representation of a Timestamp value.
     * 
     * @param dataObject
     *            Object to parse into a String value
     * @param columnLen
     *            maximum string length (ignored)
     * @return
     */
    public static final String parseToTimestampString(Object dataObject,
            int columnLen) throws IhfsDatabaseException {
        if (dataObject instanceof Long) {
            return (dateFormat.format(new Date((Long) dataObject)));
        } else if (dataObject instanceof Date) {
            return (dateFormat.format(dataObject));
        } else if (dataObject instanceof String) {
            /**
             * <pre>
             * Two things may have happened: 
             * 1. A Date object may have been converted to a String automatically. 
             * (This happens when java chooses to use the java.util.Date Object 
             * as a String instead of an Object) 
             * Resolve: Parse the date string back into a Date object, then
             * Format the Date object into the format that will be recognized
             * by the Database's date parser. 
             * 2. A Date object may have been pre-converted to a recognized String format
             * yyyy-MM-dd HH:mm:SS before being passed in.
             * Resolve: Parse the String into a Date. If successful; use the String.
             */
            String dataObjectString = (String) dataObject;
            try {
                // First try the Cave Date Format
                Date parsedDate = dateFormat.parse(dataObjectString);
                // No exception thrown
            } catch (ParseException pe) {
                // Date String did not parse.
                // Try Java default date format.
                SimpleDateFormat javaDateFormat = new SimpleDateFormat(
                        IhfsConstants.DefaultJavaDateStringPattern);
                javaDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
                try {
                    Date tempJavaSysDate = javaDateFormat
                            .parse(dataObjectString);
                    // Set the newly formatted string as the output.
                    dataObject = dateFormat.format(tempJavaSysDate);
                } catch (ParseException pe1) {
                    String msg = "Unable to parse input Date String <"
                            + dataObjectString
                            + "> into a recognizible date format (yyyy-MM-dd HH:mm:ss or EEE MMM dd HH:mm:ss zzz yyyy) ";
                    throw (new IhfsDatabaseException(msg, pe1));
                }
            }
        }
        return (dataObject.toString());
    }

    /**
     * Parse object to a String representation of a Date value.
     * 
     * @param dataObject
     *            Object to parse into a String value
     * @param columnLen
     *            maximum string length (ignored)
     * @return
     */
    public static final String parseToDateString(Object dataObject,
            int columnLen) throws IhfsDatabaseException {
        // Parse ALL valid DATE values
        if (dataObject instanceof Date) {
            String dateString = dateFormat.format((Date) dataObject);
            return (dateString);
        } else if (dataObject instanceof Long) {
            Date d = new Date((Long) dataObject);
            String dateString = dateFormat.format(d);
            return (dateString);
        } else if (dataObject instanceof String) {
            return (parseToTimestampString((String) dataObject, columnLen));
        } else {
            String msg = "Unable to parse input Date <"
                    + dataObject.toString()
                    + "> of class <"
                    + dataObject.getClass().getName()
                    + "> into a recognizible date format (yyyy-MM-dd HH:mm:ss or EEE MMM dd HH:mm:ss zzz yyyy) ";
            throw (new IhfsDatabaseException(msg));
        }
    }

    /**
     * Remove all single and double quotes from an input string.
     * 
     * @param inputString
     * @return inputString with all leading and trailing whitespace, single and
     *         double quotes removed.
     */
    public static final String stripString(String inputString) {

        String outputString = null;
        if (inputString != null) {
            outputString = removeQuotesMatcher.reset(inputString).replaceAll(
                    emptyString);
            outputString = outputString.trim();
        }

        return (outputString);
    }

    /**
     * Add columnData object to SQL StringBuilder
     * 
     * This method WILL add single quotations to string bast Database Columns.
     * 
     * @param sb
     *            SQL String to add to
     * @param columnData
     *            Table Column Metadata.
     * @param dataObject
     *            Data Object to format for SQL
     * @throws IhfsDatabaseException
     */
    public static final void addToStringBuilder(StringBuilder sb,
            TableColumnData columnData, Object dataObject)
            throws IhfsDatabaseException {

        if (dataObject != null) {
            String columnType = columnData.getColumnType();
            switch (columnType) {
            case TableColumnData.STRING_TYPE:
            case TableColumnData.DATE_TYPE:
            case TableColumnData.TIMESTAMP_TYPE:
                sb.append("'");
                sb.append(dataObject.toString());
                sb.append("'");
                break;
            case TableColumnData.INTEGER_TYPE:
            case TableColumnData.FLOAT_TYPE:
            case TableColumnData.LONG_TYPE:
            case TableColumnData.DOUBLE_TYPE:
                sb.append(dataObject.toString());
                break;
            default:
                String msg = "Unable to parse Column Data. Unknown Type <"
                        + columnType + "> ";
                throw (new IhfsDatabaseException(msg));
            }
        }
    }

}

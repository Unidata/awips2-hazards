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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Utility class for ihfs operations.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 28, 2015 8839       Chris.Cody  Initial Creation
 * Aug 14, 2015 9988       Chris.Cody  Modify Date handling
 * 
 * </pre>
 * 
 * @author Chris.Cody
 * @version 1.0
 */
public class IhfsUtil {

    /**
     * Determine if the data types for the 2 given columns are compatible. This
     * comparison does not include the Length in the case of string columns.
     * 
     * @param column1
     *            First column to check
     * @param column2
     *            Second column to check
     * @return true if the two data types are comparable (i.e. can be compared
     *         as part of an SQL predicate)
     */
    public static boolean areCompatible(TableColumnData column1,
            TableColumnData column2) {
        if ((column1 == null) || (column2 == null)) {
            return (false);
        }
        String columnType1 = column1.getColumnType();
        String columnType2 = column2.getColumnType();

        if ((columnType1 == null) || (columnType2 == null)) {
            return (false);
        }

        if ((columnType1.equals(TableColumnData.STRING_TYPE))
                && (columnType2.equals(TableColumnData.STRING_TYPE) == false)) {
            return (false);
        }

        if ((columnType1.equals(TableColumnData.STRING_TYPE))
                && (columnType2.equals(TableColumnData.STRING_TYPE))) {
            return (true);
        }

        if (columnType1.equals(TableColumnData.INTEGER_TYPE)) {
            if (columnType2.equals(TableColumnData.INTEGER_TYPE)) {
                return (true);
            }
            if (columnType2.equals(TableColumnData.LONG_TYPE)) {
                // Close enough
                return (true);
            }
        }
        if (columnType1.equals(TableColumnData.LONG_TYPE)) {
            if (columnType2.equals(TableColumnData.LONG_TYPE)) {
                return (true);
            }
            if (columnType2.equals(TableColumnData.INTEGER_TYPE)) {
                // Close enough
                return (true);
            }
            if (columnType2.equals(TableColumnData.TIMESTAMP_TYPE)) {
                return (true);
            }
            if (columnType2.equals(TableColumnData.DATE_TYPE)) {
                // Close enough
                return (true);
            }
        }

        if (columnType1.equals(TableColumnData.FLOAT_TYPE)) {
            if (columnType2.equals(TableColumnData.FLOAT_TYPE)) {
                return (true);
            }
            if (columnType2.equals(TableColumnData.DOUBLE_TYPE)) {
                return (true);
            }
        }
        if (columnType1.equals(TableColumnData.DOUBLE_TYPE)) {
            if (columnType2.equals(TableColumnData.DOUBLE_TYPE)) {
                return (true);
            }
            if (columnType2.equals(TableColumnData.FLOAT_TYPE)) {
                return (true);
            }
        }

        if (columnType1.equals(TableColumnData.DATE_TYPE)) {
            if (columnType2.equals(TableColumnData.DATE_TYPE)) {
                return (true);
            }
            if (columnType2.equals(TableColumnData.TIMESTAMP_TYPE)) {
                return (true);
            }
        }

        if (columnType1.equals(TableColumnData.TIMESTAMP_TYPE)) {
            if (columnType2.equals(TableColumnData.TIMESTAMP_TYPE)) {
                return (true);
            }
            if (columnType2.equals(TableColumnData.DATE_TYPE)) {
                return (true);
            }
        }

        return (false);
    }

    /**
     * Determine if the data type for a given value is compatible with the
     * column. This comparison does not include the Length in the case of string
     * columns.
     * 
     * @param column1
     *            Column to check
     * @param value
     *            Value to check
     * @return true if the two data types are comparable (i.e. can be compared
     *         as part of an SQL predicate)
     */
    public static boolean areCompatible(TableColumnData column, Object value) {

        if (column == null) {
            return (false);
        }

        if (value == null) {
            return (true);
        }

        String columnType = column.getColumnType();
        if (columnType.equals(TableColumnData.STRING_TYPE)) {
            if ((value instanceof String) || (value instanceof Number)
                    || (value instanceof Boolean)) {
                return (true);
            }
        } else if ((columnType.equals(TableColumnData.INTEGER_TYPE))
                || (columnType.equals(TableColumnData.LONG_TYPE))) {
            if ((value instanceof Integer) || (value instanceof Long)) {
                return (true);
            }
            if (value instanceof String) {
                try {
                    String tempValue = (String) value;
                    tempValue = tempValue.trim();
                    Long i = Long.parseLong(tempValue);
                    return (true);
                } catch (NumberFormatException nfe) {
                    return (false);
                }
            }
        } else if ((columnType.equals(TableColumnData.FLOAT_TYPE))
                || (columnType.equals(TableColumnData.DOUBLE_TYPE))) {
            if ((value instanceof Float) || (value instanceof Double)) {
                return (true);
            }
            if (value instanceof String) {
                try {
                    String tempValue = (String) value;
                    tempValue = tempValue.trim();
                    Double l = Double.parseDouble(tempValue);
                    return (true);
                } catch (NumberFormatException nfe) {
                    return (false);
                }
            }
        } else if ((columnType.equals(TableColumnData.DATE_TYPE))
                || (columnType.equals(TableColumnData.TIMESTAMP_TYPE))) {
            if ((value instanceof Date) || (value instanceof Calendar)
                    || (value instanceof Long)) {
                return (true);
            }
            if (value instanceof String) {
                try {
                    String tempValue = (String) value;
                    tempValue = tempValue.trim();
                    DateFormat dateFormat = IhfsConstants.getDateFormat();

                    try {
                        // First try the Cave Date Format
                        Date parsedDate = dateFormat.parse(tempValue);
                        // No exception thrown
                    } catch (ParseException pe) {
                        // Date String did not parse.
                        // Try Java default date format.
                        SimpleDateFormat javaDateFormat = new SimpleDateFormat(
                                IhfsConstants.DefaultJavaDateStringPattern);
                        javaDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
                        Date tempJavaSysDate = javaDateFormat.parse(tempValue);
                        tempValue = dateFormat.format(tempJavaSysDate);
                    }
                    return (true);
                } catch (ParseException nfe) {
                    return (false);
                }
            }
        }

        return (false);
    }

    /**
     * Check to ensure that the given object is a type that is recognized.
     * 
     * @param dataObject
     *            Object to check
     * @return True if valid
     */
    public static final boolean isRecognizedDataType(Object dataObject) {

        if (dataObject != null) {
            if (dataObject instanceof String) {
                return (true);
            } else if (dataObject instanceof Integer) {
                return (true);
            } else if (dataObject instanceof Float) {
                return (true);
            } else if (dataObject instanceof Long) {
                return (true);
            } else if (dataObject instanceof Double) {
                return (true);
            } else if (dataObject instanceof BigInteger) {
                return (true);
            } else if (dataObject instanceof BigDecimal) {
                return (true);
            } else if (dataObject instanceof Short) {
                return (true);
            } else if (dataObject instanceof Date) {
                return (true);
            } else if (dataObject instanceof Calendar) {
                return (true);
            }
        } else {
            return (true); // Null is recognized
        }

        return (false);
    }

    /**
     * Parse Table Name out of input string.
     * 
     * If the given string does NOT have a "." in the input; then the output is
     * the input string. It is not checked against all available table names.
     * 
     * @param inputString
     *            A Table.Column or Table name string
     * @return Table Name String
     */
    public static String parseTableName(String inputString) {
        String parsedTableName = null;
        if ((inputString != null) && (inputString.isEmpty() == false)) {
            inputString = inputString.trim().toUpperCase();
            int idx = inputString.indexOf(".");
            if (idx > 0) {
                parsedTableName = inputString.substring(0, idx);
            } else {
                parsedTableName = null;
            }
        }
        return (parsedTableName);
    }

    /**
     * Parse Column Name out of input string.
     * 
     * If the given string does NOT have a "." in the input; then the output is
     * the input string. It is not checked against all available column names.
     * 
     * @param inputString
     *            A Table.Column or Column name string
     * @return Column Name String
     */
    public static String parseColumnName(String inputString) {
        String parsedColumnName = null;
        if ((inputString != null) && (inputString.isEmpty() == false)) {
            inputString = inputString.trim().toUpperCase();
            int idx = inputString.indexOf(".");
            if (idx > 0) {
                idx++;
                parsedColumnName = inputString.substring(idx);
            } else {
                parsedColumnName = inputString;
            }
        }
        return (parsedColumnName);
    }

}

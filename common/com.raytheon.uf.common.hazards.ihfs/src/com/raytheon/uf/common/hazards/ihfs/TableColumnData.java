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

/**
 * This class contains the metadata (Name, Data Type, length, order in table, is
 * key, etc) about an ihfs database table column.
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
public class TableColumnData {

    public static final int INTEGER_LEN = 0;

    public static final int LONG_LEN = 0;

    public static final int FLOAT_LEN = 0;

    public static final int DOUBLE_LEN = 0;

    public static final int DATE_LEN = 0;

    public static final int TIMESTAMP_LEN = 0;

    public final static String STRING_TYPE = "STRING";

    public final static String INTEGER_TYPE = "INTEGER";

    public final static String LONG_TYPE = "LONG";

    public final static String FLOAT_TYPE = "FLOAT";

    public final static String DOUBLE_TYPE = "DOUBLE";

    public final static String TIMESTAMP_TYPE = "TIMESTAMP";

    public final static String DATE_TYPE = "DATE";

    private final String tableName;

    private final String columnName;

    private final String columnType;

    private final int length;

    private final Integer ordinal;

    private final boolean isKey;

    /**
     * Object Constructor. Every column in every table supported by this package
     * should have a corresponding TableColumnData object.
     * 
     * @param tableName
     *            Table Name
     * @param columnName
     *            Column Name
     * @param columnType
     *            Column Data Type
     * @param length
     *            Length (currently used only for strings)
     * @param ordinal
     *            position in database table
     * @param isKey
     *            flag denoting whether this is or is not a key column
     */
    public TableColumnData(String tableName, String columnName,
            String columnType, int length, Integer ordinal, boolean isKey) {
        this.tableName = tableName;
        this.columnName = columnName;
        this.columnType = columnType;
        this.length = length;
        this.ordinal = ordinal;
        this.isKey = isKey;
    }

    /**
     * Retrieve the Name of the parent Table.
     * 
     * 
     * @return Name of parent table
     */
    public String getTableName() {
        return (this.tableName);
    }

    /**
     * Retrieve a string containing <TableName>.<ColumnName>.
     * 
     * @return A fully qualified table.column name string
     */
    public String getQualifiedColumnName() {
        return (this.tableName + "." + this.columnName);
    }

    /**
     * Retrieve name of column
     * 
     * @return Name of column
     */
    public String getColumnName() {
        return (this.columnName);
    }

    /**
     * Retrieve a string type descriptor of the data type for the column.
     * 
     * @return Column Data Type
     */
    public String getColumnType() {
        return (this.columnType);
    }

    /**
     * Retrieve length (max number of characters) of the column. This only
     * applies to strings
     * 
     * @return Length of column
     */
    public int getLength() {
        return (this.length);
    }

    /**
     * Retrieve position of column in parent table. This is a 0 based count.
     * 
     * @return Order of column in table
     */
    public Integer getOrdinal() {
        return (this.ordinal);
    }

    /**
     * Retrieve boolean flag denoting whether this is a key column for the
     * table.
     * 
     * @return isKey
     */
    public boolean isKey() {
        return (this.isKey);
    }

}

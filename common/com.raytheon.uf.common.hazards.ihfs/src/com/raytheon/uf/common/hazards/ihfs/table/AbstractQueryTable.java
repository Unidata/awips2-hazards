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
package com.raytheon.uf.common.hazards.ihfs.table;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.raytheon.uf.common.hazards.ihfs.QueryTableInterface;
import com.raytheon.uf.common.hazards.ihfs.TableColumnData;
import com.raytheon.uf.common.hazards.ihfs.data.AbstractTableData;

/**
 * This is part of the Object Query for the IHFS (Hydro) database. This class is
 * the abstract superclass for all implemented IHFS (Hydro) Table classes.
 * Classes that extend AbstractTableData represent a model for the database
 * table. These classes are not intended to contain data contained within the
 * table that they represent. These classes are used to build Object Queries.
 * Note: This class may need to be extended as the model requires more metadata
 * for represented Hydro Database Tables.
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
public abstract class AbstractQueryTable implements QueryTableInterface {

    protected final String tableName;

    protected final List<String> columnNameList;

    protected Map<String, TableColumnData> tableColumnDataMap;

    protected final Class<? extends AbstractTableData> returnObjClass;

    protected AbstractQueryTable(String queryTableName,
            List<String> queryColumnNameList,
            Class<? extends AbstractTableData> returnObjClass) {

        this.tableName = queryTableName;
        this.columnNameList = queryColumnNameList;
        this.returnObjClass = returnObjClass;
    }

    /**
     * Get the IHFS (Hydro) Database Table Name for the Query Table
     * 
     * @return Table Name
     */
    public String getTableName() {
        return (this.tableName);
    }

    /**
     * Return a list of Table Column Names.
     * 
     * @return Table Column Name List
     */
    public List<String> getColumnNameList() {
        return (getColumnNameList(false));
    }

    /**
     * Return a list of Table Column Names.
     * 
     * @param useFullyQualified
     *            Flag when set to true will return a List of
     *            <TableName>.<ColumnName> values for the Query Table.
     * @return Table Column Name List
     */
    public List<String> getColumnNameList(boolean useFullyQualified) {
        List<String> queryColumnNameList = new ArrayList<>();

        String tempTableName = "";
        if (useFullyQualified == true) {
            tempTableName = this.tableName + ".";
        }
        for (String columnName : this.columnNameList) {
            queryColumnNameList.add(tempTableName + columnName);
        }

        return (queryColumnNameList);
    }

    /**
     * Get the All Column Data for given Column Name.
     * 
     * @param columnName
     *            Name of Query Table Column (not fully qualified)
     * @return Column Data Type @see TableColumnData
     */
    public TableColumnData getTableColumnData(String columnName) {
        if (columnName != null) {
            return (this.tableColumnDataMap.get(columnName));
        }
        return (null);
    }

    /**
     * Get the Data type of the given Column.
     * 
     * @param columnName
     *            Name of Query Table Column (not fully qualified)
     * @return Column Data Type @see TableColumnData
     */
    public String getColumnType(String columnName) {
        if (columnName != null) {
            TableColumnData tableColumnData = getTableColumnData(columnName);
            if (tableColumnData != null) {
                return (tableColumnData.getColumnType());
            }
        }
        return (null);
    }

    /**
     * Return a List of Column Names that have been marked as database table key
     * columns for this Query Table
     * 
     * @return List of Column Names
     */
    public List<String> getKeyColumnNameList() {

        return (getKeyColumnNameList(false));
    }

    /**
     * Return a List of Column Names that have been marked as database table key
     * columns for this Query Table
     * 
     * @param useFullyQualified
     *            When set to True, pre-pend the Table Name to the Column Name
     * @return List of Column Names
     */
    public List<String> getKeyColumnNameList(boolean useFullyQualified) {
        List<String> keyColumnList = new ArrayList<>();

        String tempTableName = "";
        if (useFullyQualified == true) {
            tempTableName = tableName + ".";
        }
        for (String columnName : columnNameList) {
            TableColumnData tableColumnData = tableColumnDataMap
                    .get(columnName);
            if (tableColumnData.isKey() == true) {
                keyColumnList.add(tempTableName + columnName);
            }
        }

        return (keyColumnList);
    }

    /**
     * Return the Class of the AbstractTableData object (instantiatable sub
     * class) for a query which uses columns from the IHFS Table that the
     * implementation of this Query Table object models.
     * 
     * @return An instance of an object that contains a row of data for this
     *         Query Table.
     */
    public Class<? extends AbstractTableData> getTableDataClass() {
        return (this.returnObjClass);
    }

}

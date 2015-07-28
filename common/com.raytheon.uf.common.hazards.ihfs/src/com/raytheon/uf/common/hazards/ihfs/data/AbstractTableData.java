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
package com.raytheon.uf.common.hazards.ihfs.data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.raytheon.uf.common.hazards.ihfs.IhfsDatabaseException;
import com.raytheon.uf.common.hazards.ihfs.table.AbstractQueryTable;

/**
 * This is the abstract super class for all IHFS (Hydro) query result objects.
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
public abstract class AbstractTableData implements Serializable {

    private static final long serialVersionUID = 7566734695236287567L;

    protected AbstractQueryTable queryTable;

    protected AbstractTableData(AbstractQueryTable abstractQueryTable) {
        queryTable = abstractQueryTable;
    }

    public AbstractTableData(AbstractQueryTable abstractQueryTable,
            Object[] tableData) throws IhfsDatabaseException {

        this(abstractQueryTable);

        int idx = 0;
        Serializable columnValue = null;
        for (String columnName : queryTable.getColumnNameList()) {
            try {
                columnValue = (Serializable) tableData[idx];
                setColumnByName(columnName, columnValue);
                idx++;
            } catch (Exception ex) {
                String msg = "Error: Cannot set " + queryTable.getTableName()
                        + " (" + this.getClass().getName()
                        + ") object with data from column: " + columnName
                        + " to value " + columnValue.toString();
                throw (new IhfsDatabaseException(msg));
            }
        }
    }

    /**
     * Get the Id for this data object
     * 
     * @return Data Object Id
     */
    public abstract String getId();

    /**
     * Get the Name of the Table for this data object.
     * 
     * @return Table Name
     */
    public String getTableName() {
        return (this.queryTable.getTableName());
    }

    /**
     * Set the value of a data column by the name of the column.
     * 
     * @param columnName
     *            Name of requested column data
     * @param columnValue
     *            Value of column data
     */
    public abstract void setColumnByName(String columnName,
            Serializable columnValue) throws IhfsDatabaseException;

    /**
     * Get the value of a data column by the name of the column.
     * 
     * @param columnName
     *            Name of requested column data
     * @return Object Data
     */
    protected abstract Serializable getColumnByName(String columnName)
            throws IhfsDatabaseException;

    /**
     * Create a Column Name : Value Map from the object data. This is used for
     * migrating result data objects to Python dictionary objects.
     * 
     * @return a label:value map of the object data
     */
    public Map<String, Serializable> getColumnValueMap()
            throws IhfsDatabaseException {
        List<String> columnNameList = queryTable.getColumnNameList();
        Map<String, Serializable> tableDataMap = new HashMap<>(
                columnNameList.size());

        Serializable columnValue;
        for (String columnName : columnNameList) {
            columnValue = (Serializable) getColumnByName(columnName);
            // null values are not part of the returned Map
            if (columnValue != null) {
                tableDataMap.put(columnName, columnValue);
            }
        }

        return (tableDataMap);
    }

    /**
     * Set all object fields from a Map object.
     * 
     * @param tableDataMap
     *            Input field Data Map
     * @throws IhfsDatabaseException
     */
    public void setByMap(Map<String, Serializable> tableDataMap)
            throws IhfsDatabaseException {
        List<String> columnNameList = queryTable.getColumnNameList();

        Serializable columnValue;
        for (String columnName : tableDataMap.keySet()) {
            if (columnNameList.contains(columnName)) {
                columnValue = tableDataMap.get(columnName);
                setColumnByName(columnName, columnValue);
            } else {
                String msg = "Error: " + this.getTableName()
                        + " does not have a column named: " + columnName;
                throw (new IhfsDatabaseException(msg));
            }
        }
    }

    /**
     * Get the Query Table singleton for this Data Object.
     * 
     * @return AbstractQueryTable singleton.
     */
    public AbstractQueryTable getQueryTable() {
        return (this.queryTable);
    }

    protected Integer getIntegerColumnValue(Object columnValue) {
        Integer integerValue = null;
        if (columnValue != null) {
            integerValue = ((Number) columnValue).intValue();
        }
        return (integerValue);
    }

    protected Short getShortColumnValue(Object columnValue) {
        Short shortValue = null;
        if (columnValue != null) {
            shortValue = ((Number) columnValue).shortValue();
        }
        return (shortValue);
    }

    protected Long getLongColumnValue(Object columnValue) {
        Long longValue = null;
        if (columnValue != null) {
            longValue = ((Number) columnValue).longValue();
        }
        return (longValue);
    }

    protected Float getFloatColumnValue(Object columnValue) {
        Float floatValue = null;
        if (columnValue != null) {
            floatValue = ((Number) columnValue).floatValue();
        }
        return (floatValue);
    }

    protected Double getDoubleColumnValue(Object columnValue) {
        Double doubleValue = null;
        if (columnValue != null) {
            doubleValue = ((Number) columnValue).doubleValue();
        }
        return (doubleValue);
    }

    public String toString() {
        AbstractQueryTable queryTable = this.getQueryTable();
        String fullClassName = this.getClass().getName();
        StringBuilder sb = new StringBuilder();
        sb.append("[ ");
        sb.append("[CLASS = ");
        sb.append(fullClassName);
        sb.append("] ");
        sb.append("[TABLE = ");
        sb.append(this.getTableName());
        sb.append("] ");
        sb.append("[COLUMN LIST = [");
        for (String columnName : queryTable.getColumnNameList()) {
            sb.append("[");
            sb.append(columnName);
            sb.append(" = ");
            try {
                sb.append(this.getColumnByName(columnName));
            } catch (IhfsDatabaseException ide) {
                // This should never happen
                sb.append("INVALID COLUMN NAME: " + columnName);
            }
            sb.append("] ");
        }
        sb.append("] ]");
        sb.append("] ");
        return (sb.toString());
    }
}

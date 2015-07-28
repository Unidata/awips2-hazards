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

import java.util.List;

import com.raytheon.uf.common.hazards.ihfs.data.AbstractTableData;

/**
 * Interface for classes which describe the data query model of an IHFS table.
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
public interface QueryTableInterface {

    /**
     * Get the Name of the Query Table.
     * 
     * @return Query Table Name
     */
    public String getTableName();

    /**
     * Get the List of Column Name Strings for the Query Table.
     * 
     * @return List of Column Names
     */
    public List<String> getColumnNameList();

    /**
     * Get the List of Column Name Strings for the Query Table.
     * 
     * @param useFullyQualified
     *            Flag that will return the Column Names in the form
     *            TableName.ColumnName
     * @return List of Column Names
     */
    public List<String> getColumnNameList(boolean useFullyQualified);

    /**
     * Get the Table Column Data for the Named Column.
     * 
     * The columnName can be fully qualified or just the column name.
     * 
     * @param columnName
     *            Name of Table Column
     * @return
     */
    public TableColumnData getTableColumnData(String columnName);

    /**
     * Return a List of Table Columns that are maked as Key fields for the
     * Table.
     * 
     * @return List of Column Names
     */
    public List<String> getKeyColumnNameList();

    /**
     * Return a List of Table Columns that are maked as Key fields for the
     * Table.
     * 
     * @param useFullyQualified
     *            Flag that will return the Column Names in the form
     *            TableName.ColumnName
     * @return List of Column Names
     */
    public List<String> getKeyColumnNameList(boolean useFullyQualified);

    /**
     * Get a new instance of the AbstractTableData that contains data for this
     * Table.
     * 
     * @return new instance of the correct AbstractTableData object for this
     *         table
     */
    public Class<? extends AbstractTableData> getTableDataClass();

}

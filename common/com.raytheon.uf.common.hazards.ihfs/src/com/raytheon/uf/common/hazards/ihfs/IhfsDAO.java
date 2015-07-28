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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.raytheon.uf.common.dataaccess.util.DatabaseQueryUtil;
import com.raytheon.uf.common.dataaccess.util.DatabaseQueryUtil.QUERY_MODE;
import com.raytheon.uf.common.hazards.ihfs.data.AbstractTableData;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

/**
 * Product data accessor implementation of the IIhfsDAO to access ihfs data.
 * 
 * This class performs queries and creates data objects as a result of the
 * queries.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer            Description
 * ------------ ---------- -----------         --------------------------
 * Jul 28, 2015 8839       Chris.Cody  Initial Creation
 * </pre>
 * 
 * @author Chris.Cody
 * @version 1.0
 */
public class IhfsDAO implements IIhfsDAO {

    /** String constant "ifhs" This IS case sensitive. */
    public static final String IHFS_DATABASE_NAME = "ihfs";

    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(IhfsDAO.class);

    /**
     * Singleton instance of this IHFS data access object
     */
    private static IIhfsDAO ihfsDAOInstance = null;

    /**
     * Private constructor. This prevents it from being called and helps to
     * enforce the Singleton Pattern.
     * 
     * The getInstance method must be used to retrieve an instance of this
     * class.
     */
    private IhfsDAO() {
    }

    /**
     * Retrieves an instance of this IHFS data access object. This class follows
     * the Singleton Pattern.
     * 
     * All instances of this object must be retrieved using this method.
     * 
     * @return An instance of this IHFS data access object
     */
    public synchronized static final IIhfsDAO getInstance() {

        if (ihfsDAOInstance == null) {
            ihfsDAOInstance = new IhfsDAO();
        }

        return ihfsDAOInstance;
    }

    /**
     * Execute an IHFS (Hydro) Query
     * 
     * @param simpleIhfsQuery
     *            Object query to execute.
     * @return List of AbstractTableData sub classes containing data returned by
     *         the query
     */
    @Override
    public List<AbstractTableData> queryIhfsData(SimpleIhfsQuery simpleIhfsQuery)
            throws IhfsDatabaseException {

        List<AbstractTableData> returnDataList = new ArrayList<>();
        try {
            String sqlStatement = simpleIhfsQuery.buildSqlStatement();
            List<String> selectColumnList = simpleIhfsQuery
                    .getSelectColumnList();
            Class<? extends AbstractTableData> returnObjClass = simpleIhfsQuery
                    .getReturnObjClass();

            String infoString = "Query From "
                    + simpleIhfsQuery.getQueryTableNameList().toString();

            List<Object[]> queryResultList = DatabaseQueryUtil
                    .executeDatabaseQuery(QUERY_MODE.MODE_SQLQUERY,
                            sqlStatement, IHFS_DATABASE_NAME, infoString);

            if ((queryResultList != null)
                    && (queryResultList.isEmpty() == false)) {
                AbstractTableData returnDataObject = null;
                for (Object[] queryResult : queryResultList) {
                    returnDataObject = returnObjClass.newInstance();
                    setReturnObjectData(returnDataObject, selectColumnList,
                            queryResult);
                    returnDataList.add(returnDataObject);
                }
            }
        } catch (Exception ex) {
            throw (new IhfsDatabaseException("Unexpected query error.", ex));
        }
        return (returnDataList);
    }

    /**
     * Place queried data into Table Data Object.
     * 
     * @param returnDataObject
     *            Data Object to return
     * @param selectColumnList
     *            List of queried Columns in order of the queryResult
     * @param queryResult
     *            Queried data
     */
    protected void setReturnObjectData(AbstractTableData returnDataObject,
            List<String> selectColumnList, Object[] queryResult) {

        int queryResultSize = queryResult.length;
        for (int i = 0; i < queryResultSize; i++) {
            String columnName = selectColumnList.get(i);
            columnName = IhfsUtil.parseColumnName(columnName);
            Object columnValue = queryResult[i];
            try {
                returnDataObject.setColumnByName(columnName,
                        (Serializable) columnValue);
            } catch (IhfsDatabaseException ide) {
                statusHandler.error("Error setting query result data.", ide);
            }
        }
    }
}

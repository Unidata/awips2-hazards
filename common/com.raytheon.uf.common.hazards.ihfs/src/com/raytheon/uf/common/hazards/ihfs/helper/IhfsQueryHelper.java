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
package com.raytheon.uf.common.hazards.ihfs.helper;

import java.util.ArrayList;
import java.util.List;

import com.raytheon.uf.common.hazards.ihfs.IIhfsDAO;
import com.raytheon.uf.common.hazards.ihfs.IhfsConstants;
import com.raytheon.uf.common.hazards.ihfs.IhfsDAO;
import com.raytheon.uf.common.hazards.ihfs.IhfsDatabaseException;
import com.raytheon.uf.common.hazards.ihfs.IhfsQueryTableFactory;
import com.raytheon.uf.common.hazards.ihfs.PredicateAndComponent;
import com.raytheon.uf.common.hazards.ihfs.PredicateOrComponent;
import com.raytheon.uf.common.hazards.ihfs.QueryPredicateComponent;
import com.raytheon.uf.common.hazards.ihfs.SimpleAggIhfsQuery;
import com.raytheon.uf.common.hazards.ihfs.SimpleIhfsQuery;
import com.raytheon.uf.common.hazards.ihfs.SimplePredicateComponent;
import com.raytheon.uf.common.hazards.ihfs.data.AbstractTableData;
import com.raytheon.uf.common.hazards.ihfs.table.AbstractQueryTable;
import com.raytheon.uf.common.ohd.AppsDefaults;

/**
 * This singleton class is intended to be a "go-between" type of object. It is
 * intended to be used to move data from an external source (i.e. a Python
 * script) and bring it into the package to be converted into an instantiation
 * of a SimpleIhfsQuery.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 28, 2015 8839       Chris.Cody  Initial Creation
 * Aug 14, 2015 9988       Chris.Cody  Add Order By and Group By
 * 
 * </pre>
 * 
 * @author Chris.Cody
 * @version 1.0
 */
public class IhfsQueryHelper {

    private static IhfsQueryHelper myInstance = null;

    private final IIhfsDAO ihfsDAO;

    /**
     * Private singleton constructor.
     */
    private IhfsQueryHelper() {
        this.ihfsDAO = IhfsDAO.getInstance();
    }

    /**
     * Get Singleton instance.
     * 
     * @return
     */
    public static synchronized final IhfsQueryHelper getInstance() {
        if (myInstance == null) {
            myInstance = new IhfsQueryHelper();
        }

        return (myInstance);
    }

    /**
     * Create a QueryPredicateHelper for an "IN" predicate clause
     * 
     * @param conjunction
     *            Conjunction preceding predicate
     * @param tableAndColumnName
     *            Table.Column condition string
     * @param operator
     *            Condition operator ("IN")
     * @param value
     *            Condition literal list or comma separated list string
     * @return Constructed QueryPredicate Helper
     */
    public QueryPredicateHelper createQueryPredicateInHelper(
            String conjunction, String tableAndColumnName, String operator,
            Object value) {
        QueryPredicateHelper qhp = new QueryPredicateHelper(conjunction,
                tableAndColumnName, operator, value);
        return (qhp);
    }

    /**
     * Create a QueryPredicateHelper for a non"IN" predicate clause
     * 
     * @param conjunction
     *            Conjunction preceding predicate
     * @param tableAndColumnName
     *            Table.Column condition string
     * @param operator
     *            Condition operator
     * @param value
     *            Condition literal list or comma separated list string
     * @return Constructed QueryPredicate Helper
     */
    public QueryPredicateHelper createQueryPredicateHelper(String conjunction,
            String tableAndColumnName, String operator, String value) {
        return (new QueryPredicateHelper(conjunction, tableAndColumnName,
                operator, value));
    }

    /**
     * Create and execute a SimpleIhfsQuery from the given parameters.
     * 
     * @param tableName
     *            Name of Primary Query Table (Dictates return object Type)
     * @param selectColumnList
     *            List of Table.Column SELECT strings for the query
     * @param queryPredicateHelperList
     *            List of QueryPredicateHelper objects to form the Where Clause
     * @return
     * @throws Exception
     */
    public List<? extends AbstractTableData> buildAndExecuteSingleTableQuery(
            String tableName, List<String> selectColumnList,
            List<QueryPredicateHelper> queryPredicateHelperList,
            List<String> orderByList, List<String> groupByList)
            throws Exception {

        SimpleIhfsQuery simpleIhfsQuery = buildSingleTableQuery(tableName,
                selectColumnList, queryPredicateHelperList, orderByList,
                groupByList);

        simpleIhfsQuery.validate();
        List<? extends AbstractTableData> queryResults = null;
        if (simpleIhfsQuery != null) {
            queryResults = executeSimpleQuery(simpleIhfsQuery);
        }
        return (queryResults);
    }

    public SimpleIhfsQuery buildSingleTableQuery(String tableName,
            List<String> selectColumnNameList,
            List<QueryPredicateHelper> queryPredicateHelperList,
            List<String> orderByList, List<String> groupByList)
            throws IhfsDatabaseException {

        AbstractQueryTable queryTable = IhfsQueryTableFactory
                .getIhfsQueryTable(tableName);
        List<String> queryColumnNameList = queryTable.getColumnNameList(true);
        if ((selectColumnNameList == null)
                || (selectColumnNameList.isEmpty() == true)) {
            selectColumnNameList = queryColumnNameList;
        } else if (queryColumnNameList.containsAll(selectColumnNameList) == false) {
            boolean isFirst = true;
            StringBuilder colListSB = new StringBuilder();
            for (String colName : queryColumnNameList) {
                if (isFirst == false) {
                    colListSB.append(", ");
                } else {
                    isFirst = false;
                }
                colListSB.append(colName);
            }

            String msg = "Invalid Column Specification. Table: "
                    + queryTable.getTableName()
                    + " does not contain one or more of the given columns: "
                    + colListSB.toString();
            throw (new IhfsDatabaseException(msg));
        }

        List<QueryPredicateComponent> queryPredicateComponentList = buildQueryPredicateList(queryPredicateHelperList);
        SimpleIhfsQuery simpleQuery = new SimpleIhfsQuery(queryTable,
                selectColumnNameList, queryPredicateComponentList, orderByList,
                groupByList);

        return (simpleQuery);
    }

    public List<? extends AbstractTableData> executeSimpleQuery(
            SimpleIhfsQuery simpleIhfsQuery) throws Exception {
        return (ihfsDAO.queryIhfsData(simpleIhfsQuery));
    }

    /**
     * Create and execute a SimpleAggIhfsQuery Aggregate Query from the given
     * parameters.
     * 
     * @param tableName
     *            Name of Primary Query Table
     * @param aggregrateFunctionName
     *            Name of Aggregate function: MIN, MAX, COUNT, SUM
     * @param aggregrateTableColumnName
     *            List of Table.Column SELECT string for the query
     * @param queryPredicateHelperList
     *            List of QueryPredicateHelper objects to form the Where Clause
     * @return Long Aggregate Function output
     * @throws Exception
     */
    public Object buildAndExecuteAggregateQuery(String tableName,
            String aggregrateFunctionName, String aggregrateTableColumnName,
            List<QueryPredicateHelper> queryPredicateHelperList)
            throws Exception {

        SimpleAggIhfsQuery simpleAggIhfsQuery = buildAggregateTableQuery(
                tableName, aggregrateFunctionName, aggregrateTableColumnName,
                queryPredicateHelperList);

        simpleAggIhfsQuery.validate();
        Object queryResult = executeSimpleAggQuery(simpleAggIhfsQuery);
        return (queryResult);
    }

    public SimpleAggIhfsQuery buildAggregateTableQuery(String tableName,
            String aggregrateFunctionName, String aggregrateTableColumnName,
            List<QueryPredicateHelper> queryPredicateHelperList)
            throws IhfsDatabaseException {

        if ((aggregrateFunctionName == null)
                || (aggregrateFunctionName.isEmpty() == true)) {
            String msg = "Null or empry Aggregate Query Function Name.";
            throw (new IhfsDatabaseException(msg));
        }
        if ((aggregrateTableColumnName == null)
                || (aggregrateTableColumnName.isEmpty() == true)) {
            String msg = "Null or empry Aggregate Query ColumnN Name.";
            throw (new IhfsDatabaseException(msg));
        }

        AbstractQueryTable queryTable = IhfsQueryTableFactory
                .getIhfsQueryTable(tableName);

        List<String> queryColumnNameList = queryTable.getColumnNameList(true);
        if (aggregrateTableColumnName.startsWith(tableName) == false) {
            aggregrateTableColumnName = tableName + "."
                    + aggregrateTableColumnName;
        }
        if (queryColumnNameList.contains(aggregrateTableColumnName) == false) {
            String msg = "Invalid Column Specification. Table: "
                    + queryTable.getTableName()
                    + " does not contain the given column: "
                    + aggregrateTableColumnName;
            throw (new IhfsDatabaseException(msg));
        }

        List<QueryPredicateComponent> queryPredicateComponentList = buildQueryPredicateList(queryPredicateHelperList);
        SimpleAggIhfsQuery simpleAggIhfsQuery = new SimpleAggIhfsQuery(
                queryTable, aggregrateFunctionName, aggregrateTableColumnName,
                queryPredicateComponentList);

        return (simpleAggIhfsQuery);
    }

    public Object executeSimpleAggQuery(SimpleAggIhfsQuery simpleAggIhfsQuery)
            throws Exception {
        return (ihfsDAO.queryAggregateIhfsData(simpleAggIhfsQuery));
    }

    protected List<QueryPredicateComponent> buildQueryPredicateList(
            List<QueryPredicateHelper> queryPredicateHelperList)
            throws IhfsDatabaseException {
        List<QueryPredicateComponent> queryPredicateList = new ArrayList<>(
                queryPredicateHelperList.size());
        for (QueryPredicateHelper queryPredicateHelper : queryPredicateHelperList) {
            addQueryPredicateComponents(queryPredicateList,
                    queryPredicateHelper);
        }

        return (queryPredicateList);
    }

    protected void addQueryPredicateComponents(
            List<QueryPredicateComponent> queryPredicateList,
            QueryPredicateHelper queryPredicateHelper)
            throws IhfsDatabaseException {
        if (queryPredicateList.size() != 0) {
            String helperConjunction = queryPredicateHelper.getConjunction();
            if (IhfsConstants.AND.equals(helperConjunction) == true) {
                queryPredicateList.add(PredicateAndComponent.getInstance());
            } else if (IhfsConstants.OR.equals(helperConjunction) == true) {
                queryPredicateList.add(PredicateOrComponent.getInstance());
            }
        }
        QueryPredicateComponent queryPredicateComponent = buildPredicate(queryPredicateHelper);
        if (queryPredicateComponent != null) {
            queryPredicateList.add(queryPredicateComponent);
        }
    }

    protected QueryPredicateComponent buildPredicate(
            QueryPredicateHelper queryPredicateHelper)
            throws IhfsDatabaseException {

        QueryPredicateComponent queryPredicateComponent = null;
        String condidion1AsString = queryPredicateHelper
                .getCondition1AsString();
        String operatorAsString = queryPredicateHelper.getOperator();
        String condidion2AsString = queryPredicateHelper
                .getCondition2AsString();
        queryPredicateComponent = new SimplePredicateComponent(
                condidion1AsString, operatorAsString, condidion2AsString);
        return (queryPredicateComponent);
    }

    public String getAppDefaultsToken(String token) {
        return (AppsDefaults.getInstance().getToken(token));
    }
}

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
     *            List of QueryPre
     * @return
     * @throws Exception
     */
    public List<? extends AbstractTableData> buildAndExecuteSingleTableQuery(
            String tableName, List<String> selectColumnList,
            List<QueryPredicateHelper> queryPredicateHelperList)
            throws Exception {

        SimpleIhfsQuery simpleIhfsQuery = buildSingleTableQuery(tableName,
                selectColumnList, queryPredicateHelperList);

        simpleIhfsQuery.validate();
        List<? extends AbstractTableData> queryResults = null;
        if (simpleIhfsQuery != null) {
            queryResults = executeSimpleQuery(simpleIhfsQuery);
        }
        return (queryResults);
    }

    public SimpleIhfsQuery buildSingleTableQuery(String tableName,
            List<String> selectColumnNameList,
            List<QueryPredicateHelper> queryPredicateHelperList)
            throws IhfsDatabaseException {

        AbstractQueryTable queryTable = IhfsQueryTableFactory
                .getIhfsQueryTable(tableName);
        List<String> queryColumnNameList = queryTable.getColumnNameList(true);
        if ((selectColumnNameList == null)
                || (selectColumnNameList.isEmpty() == true)) {
            selectColumnNameList = queryColumnNameList;
        } else if (queryColumnNameList.containsAll(selectColumnNameList) == false) {
            String msg = "Invalid Column Specification. Table "
                    + queryTable.getTableName()
                    + " does not contain one or more of the given columns: "
                    + queryColumnNameList.toString();
            throw (new IhfsDatabaseException(msg));
        }

        List<QueryPredicateComponent> queryPredicateComponentList = buildQueryPredicateList(queryPredicateHelperList);
        SimpleIhfsQuery simpleQuery = new SimpleIhfsQuery(queryTable,
                queryColumnNameList, queryPredicateComponentList, null, null);

        return (simpleQuery);
    }

    public List<? extends AbstractTableData> executeSimpleQuery(
            SimpleIhfsQuery simpleIhfsQuery) throws Exception {
        return (ihfsDAO.queryIhfsData(simpleIhfsQuery));
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

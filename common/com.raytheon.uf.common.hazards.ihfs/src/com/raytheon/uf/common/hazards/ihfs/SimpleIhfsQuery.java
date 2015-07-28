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

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import com.raytheon.uf.common.hazards.ihfs.data.AbstractTableData;

/**
 * Build a Simple IHFS query.
 * 
 * <pre>
 * This class represents a simple:
 * SELECT Column 1, Column 2, .... Column N 
 * FROM Table 1, Table 2, ... Table N 
 * WHERE Predicate 1 Conjunction 1 .... Predicate N-1 Predicate N
 * [GROUP BY Group Column 1, .... Group Column N]
 * [ORDER BY Sort Column 1, .... Sort Column N]
 * Query.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer            Description
 * ------------ ---------- -----------         --------------------------
 * Jul 28, 2015 8839       Chris.Cody  Initial Creation
 * </pre>
 * 
 * @author Chris.Cody
 * @version 1.0
 */
public class SimpleIhfsQuery {

    private List<String> selectColumnNameList = new ArrayList<>();

    private LinkedHashSet<QueryTableInterface> queryTableSet = new LinkedHashSet<>();

    private List<QueryPredicateComponent> queryPredicateList = new ArrayList<>();

    private List<String> orderByColumnNameList = null;

    private List<String> groupByColumnNameList = null;

    private Class<? extends AbstractTableData> returnObjClass = null;

    /**
     * Default constructor.
     */
    public SimpleIhfsQuery() {

    }

    /**
     * Create a fully defined ihfs database query.
     * 
     * @param queryTable
     *            Query Result Table Type
     * @param selectColumnNameList
     *            List of Table.Column or Column names for the query result
     * @param queryPredicateList
     *            List of valid Predicate Components
     * @param orderByColumnNameList
     *            List of Order By Table.Column or Column names for the query
     *            result
     * @param groupByColumnNameList
     *            List of Group By Table.Column or Column names for the query
     *            result
     */
    public SimpleIhfsQuery(QueryTableInterface queryTable,
            List<String> selectColumnNameList,
            List<QueryPredicateComponent> queryPredicateList,
            List<String> orderByColumnNameList,
            List<String> groupByColumnNameList) {
        List<QueryTableInterface> queryTableList = null;
        if (queryTable != null) {
            queryTableList = new ArrayList<>(1);
            queryTableList.add(queryTable);
        }
        init(queryTableList, selectColumnNameList, queryPredicateList,
                orderByColumnNameList, groupByColumnNameList, null);
    }

    /**
     * Create a fully defined ihfs database query.
     * 
     * @param queryTable
     *            Query Result Table Type
     * @param selectColumnNameList
     *            List of Table.Column or Column names for the query result
     * @param queryPredicateList
     *            List of valid Predicate Components
     * @param orderByColumnNameList
     *            List of Order By Table.Column or Column names for the query
     *            result
     * @param groupByColumnNameList
     *            List of Group By Table.Column or Column names for the query
     *            result
     * @param returnObjClass
     *            definable return Object Class
     */
    public SimpleIhfsQuery(List<QueryTableInterface> queryTableList,
            List<String> selectColumnNameList,
            List<QueryPredicateComponent> queryPredicateList,
            List<String> orderByColumnNameList,
            List<String> groupByColumnNameList,
            Class<? extends AbstractTableData> returnObjClass) {
        init(queryTableList, selectColumnNameList, queryPredicateList,
                orderByColumnNameList, groupByColumnNameList, returnObjClass);
    }

    /**
     * Initialize a fully defined ihfs database query.
     * 
     * @param queryTable
     *            Query Result Table Type
     * @param selectColumnNameList
     *            List of Table.Column or Column names for the query result
     * @param queryPredicateList
     *            List of valid Predicate Components
     * @param orderByColumnNameList
     *            List of Order By Table.Column or Column names for the query
     *            result
     * @param groupByColumnNameList
     *            List of Group By Table.Column or Column names for the query
     *            result
     * @param returnObjClass
     *            definable return Object Class
     */
    private void init(List<QueryTableInterface> queryTableList,
            List<String> selectColumnNameList,
            List<QueryPredicateComponent> queryPredicateList,
            List<String> orderByColumnNameList,
            List<String> groupByColumnNameList,
            Class<? extends AbstractTableData> returnObjClass) {
        if ((queryTableList != null) && (queryTableList.isEmpty() == false)) {
            this.queryTableSet.addAll(queryTableList);
        }

        if ((selectColumnNameList != null)
                && (selectColumnNameList.isEmpty() == false)) {
            this.selectColumnNameList.addAll(selectColumnNameList);
        }

        this.queryPredicateList = queryPredicateList;

        this.orderByColumnNameList = orderByColumnNameList;
        this.groupByColumnNameList = groupByColumnNameList;

        if ((returnObjClass == null) || (this.queryTableSet.size() == 1)) {
            QueryTableInterface queryTable = this.queryTableSet.iterator()
                    .next();
            this.returnObjClass = queryTable.getTableDataClass();
        } else {
            this.returnObjClass = returnObjClass;
        }

    }

    /**
     * Explicitly add a Table to the query by name .
     * 
     * @param queryTableName
     *            Name of a known QueryTableInterface for the Table to be added.
     */
    public void addQueryTableByName(String queryTableName)
            throws IhfsDatabaseException {
        if ((queryTableName != null) && (queryTableName.isEmpty() == false)) {
            addQueryTable(IhfsQueryTableFactory
                    .getIhfsQueryTable(queryTableName));
        }
    }

    /**
     * Explicitly add a Table to the query.
     * 
     * @param queryTable
     *            a QueryTableInterface for the Table to be added.
     */
    public void addQueryTable(QueryTableInterface queryTable) {
        if (queryTable != null) {
            this.queryTableSet.add(queryTable);
        }
    }

    /**
     * Compile a list of ALL table names that are part of the query.
     * 
     * @return List of table names
     */
    public List<String> getQueryTableNameList() {
        List<String> queryTableNameList = new ArrayList<>();
        if ((this.queryTableSet != null)
                && (this.queryTableSet.isEmpty() == false)) {
            for (QueryTableInterface queryTable : queryTableSet) {
                queryTableNameList.add(queryTable.getTableName());
            }
        }
        return (queryTableNameList);
    }

    public List<QueryTableInterface> getQueryTableList() {
        List<QueryTableInterface> queryTableList = new ArrayList<>();
        if ((this.queryTableSet != null)
                && (this.queryTableSet.isEmpty() == false)) {
            queryTableList.addAll(this.queryTableSet);
        }
        return (queryTableList);
    }

    public void setSelectColumnNameList(List<String> selectColumnNameList)
            throws IhfsDatabaseException {
        this.selectColumnNameList = selectColumnNameList;
        if ((this.selectColumnNameList != null)
                && (this.selectColumnNameList.isEmpty() == false)) {
            validateSelectClause();
        }
    }

    public void addSelectColumnName(String selectColumnName) {
        if (this.selectColumnNameList == null) {
            this.selectColumnNameList = new ArrayList<String>();
        }
        if (selectColumnName != null) {
            this.selectColumnNameList.add(selectColumnName);
        }
    }

    public void setQueryPredicateList(
            List<QueryPredicateComponent> queryPredicateList)
            throws IhfsDatabaseException {
        this.queryPredicateList = queryPredicateList;
        if (this.queryPredicateList != null) {
            this.validate();
        }
    }

    public void addQueryPredicate(QueryPredicateComponent queryPredicate)
            throws Exception {
        if (this.queryPredicateList == null) {
            this.queryPredicateList = new ArrayList<>();
        }
        if (queryPredicate != null) {
            checkAgainstLastPredicate(queryPredicate);
            this.queryPredicateList.add(queryPredicate);
        }
    }

    public void addAndQueryPredicate(QueryPredicateComponent queryPredicate)
            throws Exception {
        if (this.queryPredicateList == null) {
            this.queryPredicateList = new ArrayList<>();
        }
        if (queryPredicate != null) {
            if (this.queryPredicateList.size() != 0) {
                PredicateAndComponent andPredicateComponent = PredicateAndComponent
                        .getInstance();
                checkAgainstLastPredicate(andPredicateComponent);
            }
            checkAgainstLastPredicate(queryPredicate);
            this.queryPredicateList.add(queryPredicate);
        }
    }

    public void addOrQueryPredicate(QueryPredicateComponent queryPredicate)
            throws Exception {
        if (this.queryPredicateList == null) {
            this.queryPredicateList = new ArrayList<>();
        }
        if (queryPredicate != null) {
            if (this.queryPredicateList.isEmpty() == false) {
                PredicateOrComponent orPredicateComponent = PredicateOrComponent
                        .getInstance();
                checkAgainstLastPredicate(orPredicateComponent);
                this.queryPredicateList.add(orPredicateComponent);
            }
            checkAgainstLastPredicate(queryPredicate);
            this.queryPredicateList.add(queryPredicate);
        }
    }

    public LinkedHashSet<String> buildFromTableSet() {
        LinkedHashSet<String> fromTableSet = new LinkedHashSet<>();
        LinkedHashSet<String> subFromTableSet = null;

        if ((this.selectColumnNameList != null)
                && (this.selectColumnNameList.isEmpty() == false)) {
            for (String selectColumnName : this.selectColumnNameList) {
                String tableName = IhfsUtil.parseTableName(selectColumnName);
                if (tableName != null) {
                    fromTableSet.add(tableName);
                }
            }
        }

        if ((this.queryPredicateList != null)
                && (this.queryPredicateList.isEmpty() == false)) {
            for (QueryPredicateComponent queryPredicateComponent : this.queryPredicateList) {
                subFromTableSet = queryPredicateComponent.buildFromTableSet();
                if ((subFromTableSet != null)
                        && (subFromTableSet.isEmpty() == false)) {
                    fromTableSet.addAll(subFromTableSet);
                }
            }
        }

        if (fromTableSet.isEmpty()) {
            return (null);
        }

        return (fromTableSet);
    }

    public void clear() {
        if (this.selectColumnNameList != null) {
            this.selectColumnNameList.clear();
        }
        if (this.queryTableSet != null) {
            this.queryTableSet.clear();
        }
        if (this.queryPredicateList != null) {
            this.queryPredicateList.clear();
        }
        if (this.orderByColumnNameList != null) {
            this.orderByColumnNameList.clear();
        }
        if (this.groupByColumnNameList != null) {
            this.groupByColumnNameList.clear();
        }
    }

    /**
     * Validate Ihfs Query.
     * 
     * @throws IhfsDatabaseException
     */
    public void validate() throws IhfsDatabaseException {

        // Check that a valid table and return object class is set
        validateSelectClause();

        validateQueryPredicate();
        // Check that a valid predicate is set
        QueryPredicateComponent previousPredicateComponent = null;
        for (QueryPredicateComponent currentPredicateComponent : this.queryPredicateList) {
            currentPredicateComponent.checkPredicate();
            checkPredicateOrdering(previousPredicateComponent,
                    currentPredicateComponent);
            previousPredicateComponent = currentPredicateComponent;
        }
        checkPredicateOrdering(previousPredicateComponent, null);

        // Check that order by is correct if set
        validateOrderByClause();

        // Check that group by is correct if set
        validateGroupByClause();
    }

    protected void validateQueryPredicate() throws IhfsDatabaseException {

        if ((this.queryPredicateList == null)
                || (this.queryPredicateList.isEmpty() == true)) {
            String msg = "Invalid query. A Query predicate (WHERE clause) must be set.";
            throw (new IhfsDatabaseException(msg));
        }
        // Check that a valid predicate is set
        QueryPredicateComponent previousPredicateComponent = null;
        for (QueryPredicateComponent currentPredicateComponent : this.queryPredicateList) {
            currentPredicateComponent.checkPredicate();
            checkPredicateOrdering(previousPredicateComponent,
                    currentPredicateComponent);
            previousPredicateComponent = currentPredicateComponent;
        }
        checkPredicateOrdering(previousPredicateComponent, null);
    }

    private final void checkAgainstLastPredicate(
            QueryPredicateComponent currentPredicateComponent) throws Exception {
        if (this.queryPredicateList != null) {
            QueryPredicateComponent lastPredicateComponent = null;
            int len = this.queryPredicateList.size();
            if (len > 0) {
                lastPredicateComponent = this.queryPredicateList.get(len - 1);
            }
            checkPredicateOrdering(lastPredicateComponent,
                    currentPredicateComponent);
        }
    }

    private final void checkPredicateOrdering(
            QueryPredicateComponent previousPredicate,
            QueryPredicateComponent currentPredicate)
            throws IhfsDatabaseException {
        if ((previousPredicate == null) && (currentPredicate == null)) {
            throw (new IhfsDatabaseException("No DATA"));
        }
        if (currentPredicate == null) {
            if (previousPredicate instanceof AbstractConjunctionComponent) {
                throw (new IhfsDatabaseException(
                        "Cannot have end a WHERE clause with a conjunction: "
                                + previousPredicate.toString()));
            }
        } else if (currentPredicate instanceof AbstractConjunctionComponent) {
            if (previousPredicate == null) {
                throw (new IhfsDatabaseException(
                        "Cannot have begin the WHERE clause with a conjunction."));
                // Cannot have a conjunction immediately after the where clause
            }
            if (previousPredicate instanceof AbstractConjunctionComponent) {
                throw (new IhfsDatabaseException(
                        "Cannot have 2 predicate conjunctions together: "
                                + previousPredicate.toString() + " "
                                + currentPredicate.toString()));
            }
        } else if (currentPredicate instanceof SimplePredicateComponent) {
            if ((previousPredicate != null)
                    && (previousPredicate instanceof SimplePredicateComponent)) {
                throw (new IhfsDatabaseException(
                        "Cannot have 2 predicate statements together: "
                                + previousPredicate.toString() + " "
                                + currentPredicate.toString()));
            }
            currentPredicate.checkPredicate();
        } else {
            throw (new IhfsDatabaseException(
                    "Unknown predicate condition occurred."));
        }
    }

    protected void buildSelectClause(StringBuilder sb) {

        sb.append(IhfsConstants.SELECT);
        sb.append(" ");
        boolean isFirst = true;
        for (String selectColumn : this.selectColumnNameList) {
            if (isFirst == false) {
                sb.append(",");
            } else {
                isFirst = false;
            }
            sb.append(selectColumn);
        }
        sb.append(" ");
    }

    protected void buildFromClause(StringBuilder sb)
            throws IhfsDatabaseException {

        LinkedHashSet<String> internalTableNameSet = buildFromTableSet();
        for (String internalTableName : internalTableNameSet) {
            QueryTableInterface queryTable = IhfsQueryTableFactory
                    .getIhfsQueryTable(internalTableName);
            this.queryTableSet.add(queryTable);
        }

        sb.append(IhfsConstants.FROM);
        sb.append(" ");
        boolean isFirst = true;
        for (QueryTableInterface fromTable : this.queryTableSet) {
            if (isFirst == false) {
                sb.append(",");
            } else {
                isFirst = false;
            }
            sb.append(fromTable.getTableName());
        }
        sb.append(" ");
    }

    protected void buildWhereClause(StringBuilder sb)
            throws IhfsDatabaseException {

        sb.append(IhfsConstants.WHERE);
        sb.append(" ");
        for (QueryPredicateComponent queryPredicate : this.queryPredicateList) {
            queryPredicate.addToStringBuilder(sb);
        }
        sb.append(" ");
    }

    protected void buildOrderByClause(StringBuilder sb) {

        if ((this.orderByColumnNameList != null)
                && (this.orderByColumnNameList.size() > 0)) {
            sb.append(IhfsConstants.ORDER_BY);
            sb.append(" ");
            boolean isFirst = true;
            for (String orderByColumn : this.orderByColumnNameList) {
                if (isFirst == false) {
                    sb.append(",");
                } else {
                    isFirst = false;
                }
                sb.append(orderByColumn);
            }
            sb.append(" ");
        }
    }

    protected void buildGroupByClause(StringBuilder sb) {

        if ((this.groupByColumnNameList != null)
                && (this.groupByColumnNameList.size() > 0)) {
            sb.append(IhfsConstants.GROUP_BY);
            sb.append(" ");
            boolean isFirst = true;
            for (String groupByColumn : this.groupByColumnNameList) {
                if (isFirst == false) {
                    sb.append(",");
                } else {
                    isFirst = false;
                }
                sb.append(groupByColumn);
            }
            sb.append(" ");
        }
    }

    public String buildSqlStatement() throws IhfsDatabaseException {

        StringBuilder sb = new StringBuilder();
        this.buildSelectClause(sb);
        this.buildFromClause(sb);
        this.buildWhereClause(sb);
        this.buildOrderByClause(sb);
        this.buildGroupByClause(sb);

        return (sb.toString());
    }

    public List<String> getSelectColumnList() {
        return (this.selectColumnNameList);
    }

    public Class<? extends AbstractTableData> getReturnObjClass() {
        return (returnObjClass);
    }

    protected void validateSelectClause() throws IhfsDatabaseException {
        findTableColumnInQueryTableList(this.selectColumnNameList, "Select");
    }

    protected void validateGroupByClause() throws IhfsDatabaseException {
        findTableColumnInQueryTableList(this.groupByColumnNameList, "Group By");
    }

    protected void validateOrderByClause() throws IhfsDatabaseException {
        findTableColumnInQueryTableList(this.orderByColumnNameList, "Order By");
    }

    protected void findTableColumnInQueryTableList(
            List<String> tableColumnNameList, String clause)
            throws IhfsDatabaseException {
        List<QueryTableInterface> queryTableDataList = this.getQueryTableList();
        if ((tableColumnNameList != null)
                && (tableColumnNameList.isEmpty() == false)) {
            for (String tableColumnName : tableColumnNameList) {
                String tableName = IhfsUtil.parseTableName(tableColumnName);
                String columnName = IhfsUtil.parseColumnName(tableColumnName);
                if (tableName.equals(columnName) == true) {
                    String msg = "Invalid " + clause + " Table.Column Name <"
                            + tableColumnName + ">. " + clause
                            + " must specify both Table and Column.";
                    throw (new IhfsDatabaseException(msg));
                }

                boolean foundColumn = false;
                for (QueryTableInterface queryTable : queryTableDataList) {
                    String queryTableName = queryTable.getTableName();
                    if (tableName.equals(queryTableName) == true) {
                        List<String> queryColumnNameList = queryTable
                                .getColumnNameList();
                        if (queryColumnNameList.contains(columnName) == true) {
                            foundColumn = true;
                            break;
                        } else {
                            String msg = "Invalid " + clause + " Column Name <"
                                    + columnName + "> specified for Table <"
                                    + tableName + ">.";
                            throw (new IhfsDatabaseException(msg));
                        }
                    }
                }
                if (foundColumn == false) {
                    String msg = "Invalid " + clause + " Table Name <"
                            + tableName + "> is not part of the current query.";
                    throw (new IhfsDatabaseException(msg));
                }
            }
        }
    }

}

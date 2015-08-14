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

/**
 * Build a Simple IHFS query.
 * 
 * <pre>
 * This class represents a simple abstract base for 
 * simple select ... from ... where ... 
 * and 
 * select aggregrate function() from ... where ...
 * queries
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer            Description
 * ------------ ---------- -----------         --------------------------
 * Aug 14, 2015 9988       Chris.Cody  Initial Creation
 * </pre>
 * 
 * @author Chris.Cody
 * @version 1.0
 */
public abstract class AbstractIhfsQuery {

    protected LinkedHashSet<QueryTableInterface> queryTableSet = new LinkedHashSet<>();

    protected List<QueryPredicateComponent> queryPredicateList = new ArrayList<>();

    /**
     * Default constructor.
     */
    public AbstractIhfsQuery() {

    }

    public abstract Class<? extends Object> getReturnObjClass();

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

        LinkedHashSet<String> selectFromTableSet = buildSelectClauseFromTableSet();
        if ((selectFromTableSet != null)
                && (selectFromTableSet.isEmpty() == false)) {
            fromTableSet.addAll(selectFromTableSet);
        }
        LinkedHashSet<String> predicateFromTableSet = buildPredicateFromTableSet();
        if ((predicateFromTableSet != null)
                && (predicateFromTableSet.isEmpty() == false)) {
            fromTableSet.addAll(predicateFromTableSet);
        }

        return (fromTableSet);
    }

    protected abstract LinkedHashSet<String> buildSelectClauseFromTableSet();

    protected LinkedHashSet<String> buildPredicateFromTableSet() {
        LinkedHashSet<String> predicateFromTableSet = new LinkedHashSet<>();
        LinkedHashSet<String> subFromTableSet = null;

        if ((this.queryPredicateList != null)
                && (this.queryPredicateList.isEmpty() == false)) {
            for (QueryPredicateComponent queryPredicateComponent : this.queryPredicateList) {
                subFromTableSet = queryPredicateComponent.buildFromTableSet();
                if ((subFromTableSet != null)
                        && (subFromTableSet.isEmpty() == false)) {
                    predicateFromTableSet.addAll(subFromTableSet);
                }
            }
        }

        return (predicateFromTableSet);
    }

    public void clear() {

        clearSelectClause();
        clearTableSet();
        clearPredicateClause();
        clearOrderByClause();
        clearGroupByClause();

    }

    protected abstract void clearSelectClause();

    protected void clearTableSet() {
        if (this.queryTableSet != null) {
            this.queryTableSet.clear();
        }
    }

    protected void clearPredicateClause() {
        if (this.queryPredicateList != null) {
            this.queryPredicateList.clear();
        }
    }

    protected abstract void clearOrderByClause();

    protected abstract void clearGroupByClause();

    /**
     * Validate Ihfs Query.
     * 
     * @throws IhfsDatabaseException
     */
    public void validate() throws IhfsDatabaseException {

        // Check that a valid table and return object class is set
        validateSelectClause();

        validateQueryPredicateClause();

        // Check that order by is correct if set
        validateOrderByClause();

        // Check that group by is correct if set
        validateGroupByClause();
    }

    protected abstract void validateSelectClause() throws IhfsDatabaseException;

    protected void validateQueryPredicateClause() throws IhfsDatabaseException {
        validateQueryPredicate();
        // Check that a valid predicate is set
        QueryPredicateComponent previousPredicateComponent = null;
        for (QueryPredicateComponent currentPredicateComponent : this.queryPredicateList) {
            currentPredicateComponent.checkPredicate();
            checkPredicateOrdering(previousPredicateComponent,
                    currentPredicateComponent);
            previousPredicateComponent = currentPredicateComponent;
        }
    }

    protected void validateQueryPredicate() throws IhfsDatabaseException {

        if ((this.queryPredicateList == null)
                || (this.queryPredicateList.isEmpty() == true)) {
            return;
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

    protected abstract void validateOrderByClause()
            throws IhfsDatabaseException;

    protected abstract void validateGroupByClause()
            throws IhfsDatabaseException;

    protected final void checkAgainstLastPredicate(
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

    protected final void checkPredicateOrdering(
            QueryPredicateComponent previousPredicate,
            QueryPredicateComponent currentPredicate)
            throws IhfsDatabaseException {
        if ((previousPredicate == null) && (currentPredicate == null)) {
            throw (new IhfsDatabaseException("No Predicate Data"));
        }
        if (currentPredicate == null) {
            if (previousPredicate instanceof AbstractConjunctionComponent) {
                throw (new IhfsDatabaseException(
                        "Cannot end a WHERE clause with a conjunction: "
                                + previousPredicate.toString()));
            }
        } else if (currentPredicate instanceof AbstractConjunctionComponent) {
            if (previousPredicate == null) {
                throw (new IhfsDatabaseException(
                        "Cannot begin the WHERE clause with a conjunction."));
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

    public String buildSqlStatement() throws IhfsDatabaseException {

        StringBuilder sb = new StringBuilder();
        this.buildSelectClause(sb);
        this.buildFromClause(sb);
        this.buildWhereClause(sb);
        this.buildOrderByClause(sb);
        this.buildGroupByClause(sb);

        return (sb.toString());
    }

    protected abstract void buildSelectClause(StringBuilder sb);

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

        if ((this.queryPredicateList != null)
                && (this.queryPredicateList.isEmpty() == false)) {
            sb.append(IhfsConstants.WHERE);
            sb.append(" ");
            for (QueryPredicateComponent queryPredicate : this.queryPredicateList) {
                queryPredicate.addToStringBuilder(sb);
            }
        }
        sb.append(" ");
    }

    protected abstract void buildOrderByClause(StringBuilder sb);

    protected abstract void buildGroupByClause(StringBuilder sb);

    protected void findTableColumnInQueryTableList(
            List<String> tableColumnNameList, String clause)
            throws IhfsDatabaseException {
        if ((tableColumnNameList != null)
                && (tableColumnNameList.isEmpty() == false)) {
            for (String tableColumnName : tableColumnNameList) {
                findTableColumnInQueryTable(tableColumnName, clause);
            }
        }
    }

    protected void findTableColumnInQueryTable(String tableColumnName,
            String clause) throws IhfsDatabaseException {

        List<QueryTableInterface> queryTableDataList = this.getQueryTableList();
        if ((tableColumnName == null) || (tableColumnName.isEmpty() == true)) {
            String msg = "Null or empty " + clause + " Table.Column Name. "
                    + clause + " must specify both Table and Column.";
            throw (new IhfsDatabaseException(msg));
        }
        String tableName = IhfsUtil.parseTableName(tableColumnName);
        String columnName = IhfsUtil.parseColumnName(tableColumnName);
        if (tableName.equals(columnName) == true) {
            String msg = "Invalid " + clause + ": Table.Column Name <"
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
            String msg = "Invalid " + clause + " Table Name <" + tableName
                    + "> is not part of the current query.";
            throw (new IhfsDatabaseException(msg));
        }
    }

}

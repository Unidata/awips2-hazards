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

import com.raytheon.uf.common.hazards.ihfs.table.AbstractQueryTable;

/**
 * Build a Simple Aggregate Function IHFS query.
 * 
 * <pre>
 * This class represents a simple:
 * SELECT <Aggregate Function> (Column 1)  
 * FROM Table 1, Table 2, ... Table N 
 * WHERE Predicate 1 Conjunction 1 .... Predicate N-1 Predicate N
 * Query.
 * Aggregate Function: MAX(), MIN(), COUNT(), SUM()
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer      Description
 * ------------ ---------- -----------   --------------------------
 * Aug 14, 2015 8839       Chris.Cody    Initial Creation
 * </pre>
 * 
 * @author Chris.Cody
 * @version 1.0
 */
public class SimpleAggIhfsQuery extends AbstractIhfsQuery {

    protected String aggregateSelectFunction = null;

    protected String aggregateSelectColumn = null;

    private Class<? extends Object> returnObjClass = null;

    /**
     * Default constructor.
     */
    public SimpleAggIhfsQuery() {

    }

    /**
     * Create a fully defined ihfs aggregate database query.
     * 
     * @param queryTable
     *            Query Result Table Type
     * @param aggregateSelectFunction
     *            Aggregate Select function MAX, MIN, COUNT, SUM
     * @param aggregateSelectFunction
     *            Table.Column for the query
     * @param queryPredicateList
     *            List of valid Predicate Components
     * @throws IhfsDatabaseException
     */
    public SimpleAggIhfsQuery(QueryTableInterface queryTable,
            String aggregateSelectFunction, String aggregateSelectColumn,
            List<QueryPredicateComponent> queryPredicateList)
            throws IhfsDatabaseException {
        List<QueryTableInterface> queryTableList = null;
        if (queryTable != null) {
            queryTableList = new ArrayList<>(1);
            queryTableList.add(queryTable);
        }
        init(queryTableList, aggregateSelectFunction, aggregateSelectColumn,
                queryPredicateList);
    }

    /**
     * Create a fully defined ihfs database query.
     * 
     * @param queryTable
     *            Query Result Table Type
     * @param aggregateSelectFunction
     *            Aggregate Select function MAX, MIN, COUNT, SUM
     * @param aggregateSelectFunction
     *            Table.Column for the query
     * @param queryPredicateList
     *            List of valid Predicate Components
     * @throws IhfsDatabaseException
     */
    public SimpleAggIhfsQuery(List<QueryTableInterface> queryTableList,
            String aggregateSelectFunction, String aggregateSelectColumn,
            List<QueryPredicateComponent> queryPredicateList)
            throws IhfsDatabaseException {
        init(queryTableList, aggregateSelectFunction, aggregateSelectColumn,
                queryPredicateList);
    }

    /**
     * Initialize a fully defined ihfs aggregate database query.
     * 
     * @param queryTable
     *            Query Result Table Type
     * @param aggregateSelectFunction
     *            Aggregate Select function MAX, MIN, COUNT, SUM
     * @param aggregateSelectFunction
     *            Table.Column for the query
     * @param queryPredicateList
     *            List of valid Predicate Components
     * @throws IhfsDatabaseException
     */
    private void init(List<QueryTableInterface> queryTableList,
            String aggregateSelectFunction, String aggregateSelectColumn,
            List<QueryPredicateComponent> queryPredicateList)
            throws IhfsDatabaseException {
        if ((queryTableList != null) && (queryTableList.isEmpty() == false)) {
            this.queryTableSet.addAll(queryTableList);
        }

        this.aggregateSelectFunction = aggregateSelectFunction;
        this.aggregateSelectColumn = aggregateSelectColumn;

        this.queryPredicateList = queryPredicateList;

        String aggregateTableName = IhfsUtil
                .parseTableName(aggregateSelectColumn);
        String aggregateColumnName = IhfsUtil
                .parseColumnName(aggregateSelectColumn);
        AbstractQueryTable queryTable = IhfsQueryTableFactory
                .getIhfsQueryTable(aggregateTableName);
        TableColumnData tableColumnData = queryTable
                .getTableColumnData(aggregateColumnName);

        returnObjClass = tableColumnData.getClassForColumn();
    }

    public Class<? extends Object> getReturnObjClass() {
        return (returnObjClass);
    }

    public String getAggregateFunctionName() {
        return (this.aggregateSelectFunction);
    }

    public String getAggregateColumnName() {
        return (this.aggregateSelectColumn);
    }

    public void setAggregate(String aggregateSelectFunction,
            String aggregateSelectColumn) throws IhfsDatabaseException {
        this.aggregateSelectFunction = aggregateSelectFunction;
        this.aggregateSelectColumn = aggregateSelectColumn;
        validateSelectClause();
    }

    @Override
    protected void clearSelectClause() {
        this.aggregateSelectFunction = null;
        this.aggregateSelectColumn = null;
    }

    @Override
    protected void clearOrderByClause() {
        // Do nothing
    }

    @Override
    protected void clearGroupByClause() {
        // Do nothing
    }

    @Override
    public LinkedHashSet<String> buildSelectClauseFromTableSet() {
        LinkedHashSet<String> selectFromTableSet = new LinkedHashSet<>();

        if ((this.aggregateSelectColumn != null)
                && (this.aggregateSelectColumn.isEmpty() == false)) {
            String tableName = IhfsUtil
                    .parseTableName(this.aggregateSelectColumn);
            if (tableName != null) {
                selectFromTableSet.add(tableName);
            }
        }
        return (selectFromTableSet);
    }

    @Override
    protected void buildSelectClause(StringBuilder sb) {

        sb.append(IhfsConstants.SELECT);
        sb.append(" ");
        sb.append(aggregateSelectFunction);
        sb.append(IhfsConstants.OPEN_PAREN);
        sb.append(aggregateSelectColumn);
        sb.append(IhfsConstants.CLOSE_PAREN);
        sb.append(" ");
    }

    @Override
    protected void buildOrderByClause(StringBuilder sb) {
        // Do nothing
    }

    @Override
    protected void buildGroupByClause(StringBuilder sb) {
        // Do nothing
    }

    @Override
    protected void validateSelectClause() throws IhfsDatabaseException {

        if ((this.aggregateSelectFunction == null)
                || (this.aggregateSelectFunction.isEmpty() == true)) {
            String msg = "Null or Empty Aggregate Function: "
                    + this.aggregateSelectFunction;
            throw (new IhfsDatabaseException(msg));
        }
        if ((this.aggregateSelectColumn == null)
                || (this.aggregateSelectColumn.isEmpty() == true)) {
            String msg = "Unknown or invalid Aggregate Column "
                    + this.aggregateSelectColumn;
            throw (new IhfsDatabaseException(msg));
        }

        this.aggregateSelectFunction = this.aggregateSelectFunction
                .toUpperCase().trim();

        if ((this.aggregateSelectFunction.equals(IhfsConstants.MIN) == false)
                && (this.aggregateSelectFunction.equals(IhfsConstants.MAX) == false)
                && (this.aggregateSelectFunction.equals(IhfsConstants.COUNT) == false)
                && (this.aggregateSelectFunction.equals(IhfsConstants.SUM) == false)) {
            String msg = "Unknown or invalid Aggregate Function: "
                    + this.aggregateSelectFunction;
            throw (new IhfsDatabaseException(msg));
        }

        findTableColumnInQueryTable(this.aggregateSelectColumn,
                "Select Aggregate Function " + this.aggregateSelectFunction);
    }

    @Override
    protected void validateOrderByClause() throws IhfsDatabaseException {
        // Do nothing
    }

    @Override
    protected void validateGroupByClause() throws IhfsDatabaseException {
        // Do nothing
    }

}

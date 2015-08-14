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
 * Aug 14, 2015 9988       Chris.Cody  Refactor and Add Aggregate query, Order By and Group By
 * </pre>
 * 
 * @author Chris.Cody
 * @version 1.0
 */
public class SimpleIhfsQuery extends AbstractIhfsQuery {

    private List<String> selectColumnNameList = new ArrayList<>();

    private List<String> orderByList = null;

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
            List<String> orderByList, List<String> groupByColumnNameList) {
        List<QueryTableInterface> queryTableList = null;
        if (queryTable != null) {
            queryTableList = new ArrayList<>(1);
            queryTableList.add(queryTable);
        }
        init(queryTableList, selectColumnNameList, queryPredicateList,
                orderByList, groupByColumnNameList, null);
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
            List<String> orderByList, List<String> groupByColumnNameList,
            Class<? extends AbstractTableData> returnObjClass) {
        if ((queryTableList != null) && (queryTableList.isEmpty() == false)) {
            this.queryTableSet.addAll(queryTableList);
        }

        if ((selectColumnNameList != null)
                && (selectColumnNameList.isEmpty() == false)) {
            this.selectColumnNameList.addAll(selectColumnNameList);
        }

        this.queryPredicateList = queryPredicateList;

        this.orderByList = orderByList;
        this.groupByColumnNameList = groupByColumnNameList;

        if ((returnObjClass == null) || (this.queryTableSet.size() == 1)) {
            QueryTableInterface queryTable = this.queryTableSet.iterator()
                    .next();
            this.returnObjClass = queryTable.getTableDataClass();
        } else {
            this.returnObjClass = returnObjClass;
        }

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

    @Override
    protected void clearSelectClause() {
        if (this.selectColumnNameList != null) {
            this.selectColumnNameList.clear();
        }
    }

    @Override
    protected void clearOrderByClause() {
        if (this.orderByList != null) {
            this.orderByList.clear();
        }
    }

    @Override
    protected void clearGroupByClause() {
        if (this.groupByColumnNameList != null) {
            this.groupByColumnNameList.clear();
        }
    }

    @Override
    public LinkedHashSet<String> buildSelectClauseFromTableSet() {
        LinkedHashSet<String> selectFromTableSet = new LinkedHashSet<>();

        if ((this.selectColumnNameList != null)
                && (this.selectColumnNameList.isEmpty() == false)) {
            for (String selectColumnName : this.selectColumnNameList) {
                String tableName = IhfsUtil.parseTableName(selectColumnName);
                if (tableName != null) {
                    selectFromTableSet.add(tableName);
                }
            }
        }

        return (selectFromTableSet);
    }

    @Override
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

    @Override
    protected void buildOrderByClause(StringBuilder sb) {

        if ((this.orderByList != null) && (this.orderByList.size() > 0)) {
            sb.append(IhfsConstants.ORDER_BY);
            sb.append(" ");
            boolean isFirst = true;
            for (String orderByPhrase : this.orderByList) {
                if (isFirst == false) {
                    sb.append(",");
                } else {
                    isFirst = false;
                }
                sb.append(orderByPhrase);
            }
            sb.append(" ");
        }
    }

    @Override
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

    public List<String> getSelectColumnList() {
        return (this.selectColumnNameList);
    }

    public Class<? extends AbstractTableData> getReturnObjClass() {
        return (returnObjClass);
    }

    @Override
    protected void validateSelectClause() throws IhfsDatabaseException {
        findTableColumnInQueryTableList(this.selectColumnNameList, "Select");
    }

    @Override
    protected void validateOrderByClause() throws IhfsDatabaseException {
        if ((this.orderByList != null) && (this.orderByList.isEmpty() == false)) {
            String orderByPhrase = null;
            String tableColumnName = null;
            String sortOrder = null;
            int listLen = this.orderByList.size();
            for (int i = 0; i < listLen; i++) {
                orderByPhrase = this.orderByList.get(i);
                orderByPhrase = orderByPhrase.toUpperCase().trim();
                int index = orderByPhrase.indexOf(" ");
                if (index > 0) {
                    tableColumnName = orderByPhrase.substring(0, index);
                    sortOrder = orderByPhrase.substring(index + 1);
                } else {
                    tableColumnName = orderByPhrase;
                    sortOrder = IhfsConstants.ASC;
                }
                findTableColumnInQueryTable(tableColumnName,
                        IhfsConstants.ORDER_BY);
                if ((sortOrder.equals(IhfsConstants.ASC) == false)
                        && (sortOrder.equals(IhfsConstants.DESC) == false)) {
                    String msg = "Invalid Sort Order <" + sortOrder
                            + ">. Order By must be ASC or DESC.";
                    throw (new IhfsDatabaseException(msg));
                }

                orderByPhrase = tableColumnName + " " + sortOrder;
                orderByList.set(i, orderByPhrase);
            }
        }

    }

    @Override
    protected void validateGroupByClause() throws IhfsDatabaseException {
        findTableColumnInQueryTableList(this.groupByColumnNameList,
                IhfsConstants.GROUP_BY);
    }

}

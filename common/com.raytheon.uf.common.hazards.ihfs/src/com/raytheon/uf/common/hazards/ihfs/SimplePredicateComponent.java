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
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

import com.raytheon.uf.common.hazards.ihfs.table.AbstractQueryTable;

/**
 * This class is a simple, single condition SQL Select query predicate.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 28, 2015 8839       Chris.Cody  Initial Creation
 * Aug 14, 2015 9988       Chris.Cody  Modified Operator Enum handling
 * 
 * </pre>
 * 
 * @author Chris.Cody
 * @version 1.0
 */
public class SimplePredicateComponent extends QueryPredicateComponent {

    private Object originalCondition1Value;

    private TableColumnData predicateColumnData1;

    private PredicateOperator predicateOperator;

    private TableColumnData predicateColumnData2;

    private Object originalCondition2Value;

    private Object predicateValue2;

    public SimplePredicateComponent() {
    }

    /**
     * SimplePredicateComponent Constructor.
     * 
     * @param condition1Column
     *            A TableColumnData for the first condition in the predicate
     * @param predicateOperator
     *            Operator object for the predicate
     * @param condition2Column
     *            A TableColumnData for the second condition in the predicate
     * @throws IhfsDatabaseException
     */
    public SimplePredicateComponent(TableColumnData condition1Column,
            PredicateOperator predicateOperator,
            TableColumnData condition2Column) throws IhfsDatabaseException {
        setPredicate(condition1Column, predicateOperator, condition2Column);
    }

    /**
     * SimplePredicateComponent Constructor.
     * 
     * @param condition1Column
     *            A Table.Column string for the first condition in the predicate
     * @param predicateOperator
     *            Operator string for the predicate
     * @param condition2Column
     *            A Condition String or Table.Column string for the second
     *            condition in the predicate
     * @throws IhfsDatabaseException
     */
    public SimplePredicateComponent(String condition1String,
            String predicateOperatorString, String condition2String)
            throws IhfsDatabaseException {
        setPredicate(condition1String, predicateOperatorString,
                condition2String);
    }

    /**
     * Set Predicate values.
     * 
     * @param condition1Column
     *            A TableColumnData for the first condition in the predicate
     * @param predicateOperator
     *            Operator for the predicate
     * @param condition2Column
     *            A TableColumnData for the second condition in the predicate
     * @throws IhfsDatabaseException
     */
    public void setPredicate(TableColumnData condition1Column,
            PredicateOperator predicateOperator,
            TableColumnData condition2Column) throws IhfsDatabaseException {
        this.originalCondition1Value = condition1Column;
        this.originalCondition2Value = condition2Column;
        setCondition1Column(condition1Column);
        setOperator(predicateOperator);
        setCondition2Column(condition2Column);
    }

    /**
     * Set Predicate Values.
     * 
     * @param condition1Column
     *            A TableColumnData for the first condition in the predicate
     * @param predicateOperator
     *            Operator for the predicate
     * @param condition2Column
     *            A Condition String or Table.Column string for the second
     *            condition in the predicate
     * @throws IhfsDatabaseException
     */
    public void setPredicate(TableColumnData condition1Column,
            String predicateOperatorString, TableColumnData condition2Column)
            throws IhfsDatabaseException {
        this.originalCondition1Value = condition1Column;
        this.originalCondition2Value = condition2Column;
        setCondition1Column(condition1Column);
        setOperatorAsString(predicateOperatorString);
        setCondition2Column(condition2Column);
    }

    /**
     * Set Predicate Values.
     * 
     * @param condition1Column
     *            A Table.Column string for the first condition in the predicate
     * @param predicateOperator
     *            Operator for the predicate
     * @param condition2Column
     *            A Condition String or Table.Column string for the second
     *            condition in the predicate
     * @throws IhfsDatabaseException
     */
    public void setPredicate(String condition1String,
            String predicateOperatorString, String condition2String)
            throws IhfsDatabaseException {
        this.originalCondition1Value = condition1String;
        this.originalCondition2Value = condition2String;
        setCondition1AsString(condition1String);
        setOperatorAsString(predicateOperatorString);
        setCondition2AsString(condition2String);
    }

    public void setPredicate(TableColumnData condition1Column,
            String predicateOperatorString, String condition2String)
            throws IhfsDatabaseException {
        this.originalCondition1Value = condition1Column;
        this.originalCondition2Value = condition2String;

        setCondition1Column(condition1Column);
        setOperatorAsString(predicateOperatorString);
        setCondition2AsString(condition2String);
    }

    public void setPredicate(String condition1String,
            String predicateOperatorString, Object condition2Object)
            throws IhfsDatabaseException {
        this.originalCondition1Value = condition1String;
        this.originalCondition2Value = condition2Object;

        setCondition1AsString(condition1String);
        setOperatorAsString(predicateOperatorString);
        setCondition2AsObject(condition2Object);
    }

    protected void setCondition1AsString(String tableColumnString)
            throws IhfsDatabaseException {
        if ((tableColumnString != null)
                && (tableColumnString.isEmpty() == false)) {
            String tableName = IhfsUtil.parseTableName(tableColumnString);
            String columnName = IhfsUtil.parseColumnName(tableColumnString);
            if ((tableName != null) && (columnName != null)) {
                AbstractQueryTable queryTable = IhfsQueryTableFactory
                        .getIhfsQueryTable(tableName);
                this.predicateColumnData1 = queryTable
                        .getTableColumnData(columnName);
            }
        }
        if (this.predicateColumnData1 == null) {
            // Must give Table.Column or a TableColumnData object
            String msg = "Error Query Predicate First Condition must specify Table AND Column (Table.Column) or set a TableColumnData object.\n"
                    + "The table name can not be interpolated from the input data <"
                    + tableColumnString + ">.";
            throw (new IhfsDatabaseException(msg));
        }
    }

    /**
     * Set Predicate Condition 1.
     * 
     * @param condition1Column
     *            A TableColumnData for the first condition in the predicate
     */
    protected void setCondition1Column(TableColumnData condition1Column) {

        if (condition1Column != null) {
            this.predicateColumnData1 = condition1Column;
        }
    }

    protected void setOperatorAsString(String operatorString)
            throws IhfsDatabaseException {
        if (operatorString != null) {
            try {
                this.predicateOperator = PredicateOperator
                        .getEnum(operatorString);

            } catch (Exception ex) {
                this.predicateOperator = null;
                String msg = "Unable to retrieve valid predicate Operator from input string <"
                        + operatorString + ">";
                throw (new IhfsDatabaseException(msg));
            }
        } else {
            String msg = "Null Predicate Operator is not allowed";
            throw (new IhfsDatabaseException(msg));
        }
    }

    protected void setOperator(PredicateOperator predicateOperator)
            throws IhfsDatabaseException {
        if (predicateOperator != null) {
            this.predicateOperator = predicateOperator;
        } else {
            String msg = "Null Predicate Operator is not allowed";
            throw (new IhfsDatabaseException(msg));
        }
    }

    protected void setCondition2AsObject(Object condition2Object)
            throws IhfsDatabaseException {

        if (condition2Object == null) {
            setCondition2AsString(IhfsConstants.NULL_VALUE);
        } else if (condition2Object instanceof TableColumnData) {
            setCondition2Column((TableColumnData) condition2Object);
        } else if (condition2Object instanceof String) {
            setCondition2AsString((String) condition2Object);
        } else if (condition2Object instanceof List) {
            @SuppressWarnings("unchecked")
            List<Object> objectList = (List<Object>) condition2Object;
            setCondition2AsList(objectList);
        } else {
            if (IhfsUtil.isRecognizedDataType(condition2Object) == true) {
                if (IhfsUtil.areCompatible(this.predicateColumnData1,
                        condition2Object) == true) {
                    this.predicateColumnData2 = null;
                    this.predicateValue2 = ColumnDataUtil.parseData(
                            this.predicateColumnData1, condition2Object);
                }
            }
        }
    }

    protected void setCondition2AsString(String condition2InputString)
            throws IhfsDatabaseException {

        if ((condition2InputString == null)
                || (condition2InputString.isEmpty() == true)) {
            this.predicateValue2 = IhfsConstants.NULL_VALUE;
        } else {
            int commaIdx = condition2InputString.indexOf(",");
            if (commaIdx > 0) {
                // This is a comma delimited string List
                String[] stringArray = condition2InputString.split(",");
                List<String> stringList = new ArrayList<>(
                        Arrays.asList(stringArray));
                this.setCondition2AsList(stringList);
                return;
            } else {
                int dotIdx = condition2InputString.indexOf(".");
                if (dotIdx > 0) {
                    String tableName = IhfsUtil
                            .parseTableName(condition2InputString);
                    String columnName = IhfsUtil
                            .parseColumnName(condition2InputString);
                    if ((tableName != null) && (columnName != null)) {
                        AbstractQueryTable queryTable = IhfsQueryTableFactory
                                .getIhfsQueryTable(tableName);
                        TableColumnData condition2Column = queryTable
                                .getTableColumnData(columnName);
                        this.setCondition2Column(condition2Column);
                        return;
                    }
                }
            }
            // Condition is NOT a Comma Separated List String or a
            // TABLE.COLUMN reference
            this.predicateValue2 = ColumnDataUtil.parseData(
                    this.predicateColumnData1, condition2InputString);
        }
    }

    /**
     * Set Second Predicate condition List (IN) value.
     * 
     * Dev Note:The condition2InputList is not parameterized, since we cannot
     * guarantee its type and parameterizing it to an Object causes a compile
     * error for a List<String> to be passed.
     * 
     * @param condition2InputList
     *            Condition Input List
     * @throws IhfsDatabaseException
     */
    protected void setCondition2AsList(List condition2InputList)
            throws IhfsDatabaseException {

        if ((condition2InputList != null) && (condition2InputList.size() > 0)) {
            if ((this.predicateOperator.equals(PredicateOperator.IN) == false)
                    && (this.predicateOperator.equals(PredicateOperator.NOT_IN) == false)) {
                String msg = "A query predicate list can only be used for IN and NOT IN operations";
                throw (new IhfsDatabaseException(msg));
            }
            List<Object> condition2ValueList = new ArrayList<>(
                    condition2InputList.size());
            for (Object condition2Raw : condition2InputList) {
                Object condition2Parsed = null;
                condition2Parsed = ColumnDataUtil.parseData(
                        this.predicateColumnData1, condition2Raw);
                condition2ValueList.add(condition2Parsed);
            }
            this.predicateValue2 = condition2ValueList;
        } else {
            String msg = "An IN query predicate list can not be set to null";
            throw (new IhfsDatabaseException(msg));
        }
    }

    /**
     * Set Predicate Condition 2.
     * 
     * @param condition1Column
     *            A TableColumnData for the first condition in the predicate
     */
    protected void setCondition2Column(TableColumnData condition2Column)
            throws IhfsDatabaseException {

        if (condition2Column != null) {
            if (IhfsUtil.areCompatible(this.predicateColumnData1,
                    condition2Column)) {
                this.predicateColumnData2 = condition2Column;
            } else {
                String msg = "Query predicate column "
                        + this.predicateColumnData1.getQualifiedColumnName()
                        + " of type "
                        + this.predicateColumnData1.getColumnType()
                        + " is not compatible with "
                        + condition2Column.getQualifiedColumnName()
                        + " of type " + condition2Column.getColumnType();
                throw (new IhfsDatabaseException(msg));
            }
        }
    }

    /**
     * Check the validity of the Predicate.
     */
    public void checkPredicate() throws IhfsDatabaseException {
        checkSimplePredicate();
    }

    /**
     * Check the validity of the SimplePredicateComponent Predicate values.
     */
    public void checkSimplePredicate() throws IhfsDatabaseException {

        if (this.predicateColumnData1 == null) {
            String msg = "Query Predicate first condition cannot be null. It must specify a Table and Column.";
            throw (new IhfsDatabaseException(msg));
        }

        if ((this.predicateColumnData2 != null)
                && (this.predicateValue2 != null)) {
            String msg = "Query Predicate second condition cannot both be set. It must specify either a Table and Column, or a value.";
            throw (new IhfsDatabaseException(msg));
        }
        if ((this.predicateColumnData1 == null)
                && (this.predicateValue2 == null)) {
            String msg = "Query Predicate second condition cannot be null. It must specify a Table and Column, or a value.";
            throw (new IhfsDatabaseException(msg));
        }

        if ((PredicateOperator.IN.equals(this.predicateOperator))
                || (PredicateOperator.NOT_IN.equals(this.predicateOperator))) {
            checkInValuePredicate();
        } else {
            checkSimpleValuePredicate();
        }
    }

    protected void checkSimpleValuePredicate() throws IhfsDatabaseException {

        String predicateTableColType1 = this.predicateColumnData1
                .getColumnType();

        if (this.predicateColumnData2 != null) {
            String predicateTableColType2 = this.predicateColumnData2
                    .getColumnType();

            if (IhfsUtil.areCompatible(this.predicateColumnData1,
                    this.predicateColumnData2) == false) {
                String msg = "SQL validation failure: Table Column Comparison mismatch: Table Column: "
                        + this.predicateColumnData1.getQualifiedColumnName()
                        + " of Type: "
                        + predicateTableColType1
                        + " is incompatible with Table Column: "
                        + this.predicateColumnData2.getQualifiedColumnName()
                        + " of Type: " + predicateTableColType2;
                throw (new IhfsDatabaseException(msg));
            }
        }

        if (this.predicateValue2 != null) {
            if (IhfsUtil.areCompatible(this.predicateColumnData1,
                    this.predicateValue2) == false) {
                String msg = "SQL validation failure: Table Column Comparison mismatch: Table Column: "
                        + this.predicateColumnData1.getQualifiedColumnName()
                        + " of Type: "
                        + predicateTableColType1
                        + " is incompatible with Value: "
                        + this.predicateValue2.toString()
                        + " of Type: "
                        + this.predicateValue2.getClass().getName();
                throw (new IhfsDatabaseException(msg));
            }

        }
    }

    protected void checkInValuePredicate() throws IhfsDatabaseException {
        if ((this.predicateOperator != PredicateOperator.IN)
                && (this.predicateOperator != PredicateOperator.NOT_IN)) {
            String msg = "Query predicate must specify either an IN or NOT IN operator";
            throw (new IhfsDatabaseException(msg));
        }

        if ((this.predicateValue2 == null)
                || (((List) this.predicateValue2).isEmpty() == true)
                || ((this.predicateValue2 instanceof List) == false)) {
            String msg = "Query IN predicate must specify a list of values.";
            throw (new IhfsDatabaseException(msg));
        }

        @SuppressWarnings("unchecked")
        List<Object> predicateValue2List = (List<Object>) this.predicateValue2;
        for (Object predicateValue : predicateValue2List) {
            boolean areCompatible = IhfsUtil.areCompatible(
                    this.predicateColumnData1, predicateValue);
            if (areCompatible == false) {
                String msg = "Error Query IN Predicate Value: "
                        + this.predicateValue2 + " of Class: "
                        + this.predicateValue2.getClass().getName()
                        + " is incompatible with Table Column "
                        + this.predicateColumnData1.getQualifiedColumnName()
                        + " of Type: "
                        + this.predicateColumnData1.getColumnType();
                throw (new IhfsDatabaseException(msg));
            }
        }
    }

    @Override
    public LinkedHashSet<String> buildFromTableSet() {

        LinkedHashSet<String> tableSet = new LinkedHashSet<>(2);

        if (this.predicateColumnData1 != null) {
            tableSet.add(this.predicateColumnData1.getTableName());
        }

        if (this.predicateColumnData2 != null) {
            tableSet.add(this.predicateColumnData2.getTableName());
        }

        return (tableSet);
    }

    public void addToStringBuilder(StringBuilder sb, boolean useFullyQualified)
            throws IhfsDatabaseException {
        if (sb != null) {
            if ((this.predicateOperator != PredicateOperator.IN)
                    && (this.predicateOperator != PredicateOperator.NOT_IN)) {
                addPredicateToStringBuilder(sb, useFullyQualified);
            } else {
                addInPredicateToStringBuilder(sb, useFullyQualified);
            }
        }
    }

    private void addPredicateToStringBuilder(StringBuilder sb,
            boolean useFullyQualified) throws IhfsDatabaseException {

        String tableColumn1String = null;
        if (useFullyQualified == true) {
            tableColumn1String = this.predicateColumnData1
                    .getQualifiedColumnName();
        } else {
            tableColumn1String = this.predicateColumnData1.getColumnName();
        }

        sb.append(tableColumn1String);
        sb.append(" ");
        sb.append(this.predicateOperator);
        sb.append(" ");

        if (this.predicateColumnData2 != null) {
            String tableColumn2String = null;
            if (useFullyQualified == true) {
                tableColumn2String = this.predicateColumnData2
                        .getQualifiedColumnName();
            } else {
                tableColumn2String = this.predicateColumnData2.getColumnName();
            }
            sb.append(tableColumn2String);
        } else if (this.predicateValue2 != null) {
            try {
                ColumnDataUtil.addToStringBuilder(sb,
                        this.predicateColumnData1, this.predicateValue2);
            } catch (Exception ex) {
                String msg = "Unable to append predicate value <"
                        + this.predicateValue2 + "> of type "
                        + this.predicateValue2.getClass().getName()
                        + " Comparison to: "
                        + this.predicateColumnData1.getQualifiedColumnName()
                        + " of type "
                        + this.predicateColumnData1.getColumnType();
                throw (new IhfsDatabaseException(msg));
            }
        }
    }

    private void addInPredicateToStringBuilder(StringBuilder sb,
            boolean useFullyQualified) {

        String tableColumn1 = null;
        if (useFullyQualified == true) {
            tableColumn1 = this.predicateColumnData1.getQualifiedColumnName();
        } else {
            tableColumn1 = this.predicateColumnData1.getColumnName();
        }

        sb.append(tableColumn1);
        sb.append(" ");
        sb.append(this.predicateOperator);
        sb.append(" ");
        sb.append(IhfsConstants.OPEN_PAREN);

        if (this.predicateValue2 != null) {

            boolean isFirst = true;
            String columnType = this.predicateColumnData1.getColumnType();
            List<String> predicateInValueList = (List<String>) this.predicateValue2;
            for (String predicateVal : predicateInValueList) {
                if (isFirst == false) {
                    sb.append(",");
                } else {
                    isFirst = false;
                }
                if (predicateVal.equals(IhfsConstants.NULL_VALUE) == false) {
                    if (columnType.equals(TableColumnData.STRING_TYPE)) {
                        sb.append("\'");
                        sb.append(predicateVal);
                        sb.append("\'");
                    } else {
                        sb.append(predicateVal);
                    }
                } else {
                    sb.append(IhfsConstants.NULL_SQL_STRING);
                }
            }
        } else {
            sb.append(IhfsConstants.NULL_SQL_STRING);
        }
        sb.append(IhfsConstants.CLOSE_PAREN);
    }

}

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

import java.util.List;

/**
 * This class is intended to be a "go-between" type of object. It is intended to
 * be used to move data from an external source (i.e. a Python script) and bring
 * it into the package to be converted into an instantiation of a
 * QueryPredicateComponent. This class holds data that might appear in a Python
 * tuple in a very simple Java object. Python often uses lists of tuples, Java
 * does lists of objects. This is intended to better bridge this gap and
 * minimize data object complexity and data transfer size.
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
public class QueryPredicateHelper {

    String conjunction;

    Object condition1;

    String operator;

    Object condition2;

    public QueryPredicateHelper() {
    }

    /**
     * Create a simplified Query Predicate WITH PRECEEDING CONJUNCTION
     * 
     * @param conjunction
     * @param tableAndColumnName
     * @param operator
     * @param value
     */
    public QueryPredicateHelper(String conjunction, String tableAndColumnName,
            String operator, Object value) {
        setPredicateValues(conjunction, tableAndColumnName, operator, value);
    }

    /**
     * Set Predicate Helper values.
     * 
     * @param conjunction
     * @param tableAndColumnName
     * @param operator
     * @param value
     */
    public void setPredicateValues(String conjunction,
            String tableAndColumnName, String operator, Object value) {
        setConjunction(conjunction);
        setCondition1(tableAndColumnName);
        setOperator(operator);
        setCondition2(value);
    }

    /**
     * Set conjunction value.
     * 
     * @param conjunction
     */
    public void setConjunction(String conjunction) {
        if (conjunction != null) {
            this.conjunction = conjunction.toUpperCase().trim();
        }
    }

    /**
     * Get conjunction value.
     * 
     * @return
     */
    public String getConjunction() {
        return (this.conjunction);
    }

    /**
     * Set first condition value.
     * 
     * This will be a Table.Column String or a TableColumnData object
     * 
     * @param condition1
     *            Table.Column String or a TableColumnData object
     */
    public void setCondition1(Object condition1) {
        this.condition1 = condition1;
    }

    /**
     * Get first condition value.
     * 
     * @return Predicate Condition 1
     */
    public Object getCondition1() {
        return (this.condition1);
    }

    /**
     * Get first condition as a String value.
     * 
     * @return condition1 as a String
     */
    public String getCondition1AsString() {
        return (this.condition1.toString());
    }

    /**
     * Get Predicate operator string
     * 
     * @return operator
     */
    public String getOperator() {
        return (this.operator);
    }

    /**
     * Set Predicate operator string
     * 
     * @param operator
     */
    public void setOperator(String operator) {
        this.operator = operator;
    }

    /**
     * Set the second condition.
     * 
     * This can be a Table.Column String a TableColumnData object, a literal
     * value or list of literal values.
     * 
     * @param condition2
     *            Second predicate condition
     */
    public void setCondition2(Object condition2) {
        this.condition2 = condition2;
    }

    /**
     * Get second condition as a String value.
     * 
     * @return condition2 as a String
     */
    public Object getCondition2() {
        return (this.condition2);
    }

    public String getCondition2AsString() {
        return (this.condition2.toString());
    }

    /**
     * Set second condition (IN) value.
     * 
     * Set a List of Strings for the Predicate Helper.
     * 
     * @param conjunction
     *            Preceeding conjuction
     * @param tableAndColumnName
     *            Condition 1 table and column name
     * @param value
     *            List Of String values for the predicate
     */
    public void setPredicateInValues(String conjunction,
            String tableAndColumnName, List<String> inConditionList) {
        this.setConjunction(conjunction);
        setCondition2(tableAndColumnName);
        setOperator("IN");
        this.condition2 = inConditionList;
    }

}

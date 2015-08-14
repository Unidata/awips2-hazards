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

/**
 * This enum (enumeration) defines the set of predicate operators that are
 * recognized.
 * 
 * NOTE: Do not use valueOf(...) method. Enum cannot be =, >, <, >=, <=, etc.
 * Use getEnum(...) instead.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 28, 2015 8839       Chris.Cody  Initial Creation
 * Aug 14, 2015 9988       Chris.Cody  Modify Operator (enum) parsing
 * 
 * </pre>
 * 
 * @author Chris.Cody
 * @version 1.0
 */
public enum PredicateOperator {
    EQUAL("="), GREATER_THAN(">"), GREATER_OR_EQUAL(">="), LESS_THAN("<"), LESS_OR_EQUAL(
            "<="), NOT_EQUAL("!="), IN("IN"), NOT_IN("NOT IN"), LIKE("LIKE"), NOT_LIKE(
            "NOT LIKE");

    private final String operator;

    /**
     * Private Constructor.
     * 
     * @param operator
     */
    private PredicateOperator(String operator) {
        this.operator = operator;
    }

    /**
     * Return String representation of operator.
     */
    public String toString() {
        return (this.operator);
    }

    public static PredicateOperator getEnum(String operatorString) {
        if (operatorString != null) {
            operatorString = operatorString.trim();
            switch (operatorString) {
            case "=":
                return (EQUAL);
            case ">":
                return (GREATER_THAN);
            case "<":
                return (LESS_THAN);
            case "=>":
            case ">=":
                return (GREATER_OR_EQUAL);
            case "=<":
            case "<=":
                return (LESS_OR_EQUAL);
            case "<>":
            case "!=":
                return (NOT_EQUAL);
            case "in":
            case "In":
            case "IN":
                return (IN);
            case "not in":
            case "Not In":
            case "NOT IN":
                return (NOT_IN);
            case "like":
            case "Like":
            case "LIKE":
                return (LIKE);
            case "not like":
            case "Not Like":
            case "NOT LIKE":
                return (LIKE);
            default:
            }
        }

        return (null);
    }
}

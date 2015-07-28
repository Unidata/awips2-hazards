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
 * This is an abstract class representing any predicate component. A predicate
 * component is a Condition (<COL 1> <OPERATOR> <COL 2>, WIND.LID = FPINFO.LID)
 * or a Conjunction AND / OR.
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
public abstract class QueryPredicateComponent {

    public abstract void checkPredicate() throws IhfsDatabaseException;

    /**
     * Parse through this Predicate Component and extract a List of Table Names
     * that are part of the Predicate Component (if any).
     * 
     * @return A List of Table Names for the component.
     */
    public List<String> getFromTableList() {
        List<String> fromTableList = new ArrayList<>();
        LinkedHashSet<String> fromTableSet = null;
        fromTableSet = buildFromTableSet();
        if ((fromTableSet != null) && (fromTableSet.isEmpty() == false)) {
            fromTableList.addAll(fromTableSet);
        }
        return (fromTableList);
    }

    /**
     * Parse through this Predicate Component and extract a Set of Table Names
     * that are part of the Predicate Component (if any).
     * 
     * @return A Set of Table Names for the component.
     */
    public abstract LinkedHashSet<String> buildFromTableSet();

    /**
     * Convert this component into its string representation.
     * 
     * This method should not be used to assemble a query. Queries should be
     * assembled using a StringBuilder
     * 
     * @return SQL String of this Predicate Component
     */

    public String toString() {
        StringBuilder sb = new StringBuilder();
        try {
            addToStringBuilder(sb);
        } catch (Exception ex) {
            return ("");
        }
        return (sb.toString());
    }

    /**
     * Add this component to an existing String Builder instance.
     * 
     * @param sb
     *            String Builder object for Query
     * @throws IhfsDatabaseException
     */
    public void addToStringBuilder(StringBuilder sb)
            throws IhfsDatabaseException {
        addToStringBuilder(sb, true);
    }

    /**
     * Add this component to an existing String Builder instance.
     * 
     * @param sb
     *            String Builder object for Query
     * @param useFullyQualified
     *            Use Fully Qualified (Table.Column) designations
     * @throws IhfsDatabaseException
     */
    public abstract void addToStringBuilder(StringBuilder sb,
            boolean useFullyQualified) throws IhfsDatabaseException;

}

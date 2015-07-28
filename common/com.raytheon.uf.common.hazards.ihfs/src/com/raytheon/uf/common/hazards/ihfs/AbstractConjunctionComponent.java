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
 *                         402.291.0100
 * 
 * See the AWIPS II Master Rights File ("Master Rights File.pdf") for
 * further licensing information.
 */
package com.raytheon.uf.common.hazards.ihfs;

import java.util.LinkedHashSet;

/**
 * This is part of the Object Query for the IHFS (Hydro) database. This class
 * represents an Abstract conjunction component (AND/OR) for Query Predicate
 * construction. All predicate conjunctions (WHERE clause) must extend this
 * class.
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
public abstract class AbstractConjunctionComponent extends
        QueryPredicateComponent {

    protected final String conjunction;

    /**
     * Protected Abstract super-type constructor.
     * 
     * @param conjunction
     */
    protected AbstractConjunctionComponent(String conjunction) {
        if (conjunction != null) {
            this.conjunction = conjunction.trim();
        } else {
            this.conjunction = "";
        }
    }

    /**
     * Get String value of the conjunction
     * 
     * @return conjunction value (no spaces)
     */
    public final String getConjunction() {
        return (conjunction);
    }

    /**
     * Ignore. Part of component interface.
     * 
     */
    public void checkPredicate() throws IhfsDatabaseException {
        // Do nothing.
    }

    /**
     * Ignore. Part of component interface.
     * 
     */
    public LinkedHashSet<String> buildFromTableSet() {
        // Do nothing.
        return (null);
    }

    /**
     * Add conjunction to StringBuilder SQL statement.
     * 
     * @param sb
     *            StringBuilder for SQL statement
     * @param useFullyQualified
     *            Ignored here. Part of the overall interface
     */
    public void addToStringBuilder(StringBuilder sb, boolean useFullyQualified) {
        if (sb != null) {
            sb.append(" ");
            sb.append(conjunction);
            sb.append(" ");
        }
    }
}

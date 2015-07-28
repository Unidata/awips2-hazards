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
 * This class is used to group (parenthesize) 2 or more
 * SimplePredicateComponents (with internal AbstractConjunction objects) to be
 * used as a single Predicate Component unit.
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
public class MultiConditionPredicateComponent extends QueryPredicateComponent {

    private List<QueryPredicateComponent> queryPredicateComponentList;

    MultiConditionPredicateComponent() {
        this.queryPredicateComponentList = new ArrayList<>();
    }

    public void setQueryPredicateComponentList(
            List<QueryPredicateComponent> queryPredicateComponentList)
            throws IhfsDatabaseException {
        setQueryPredicateComponentList(queryPredicateComponentList, false);
    }

    public void setQueryPredicateComponentList(
            List<QueryPredicateComponent> queryPredicateComponentList,
            boolean omitPredicateCheck) throws IhfsDatabaseException {

        this.queryPredicateComponentList = queryPredicateComponentList;
        if (omitPredicateCheck == false) {
            this.checkPredicate();
        }
    }

    @Override
    public LinkedHashSet<String> buildFromTableSet() {
        LinkedHashSet<String> fromTableSet = new LinkedHashSet<>();
        LinkedHashSet<String> subFromTableSet = null;
        if ((this.queryPredicateComponentList != null)
                && (this.queryPredicateComponentList.isEmpty() == false)) {
            for (QueryPredicateComponent queryPredicateComponent : this.queryPredicateComponentList) {
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

    /**
     * Validate that the MultiConditionPredicateComponent contains a list of
     * valid predicates and conjunctions.
     * 
     * This check ensures that the list does not begin or end with a conjunction
     * and alternates between predicates and conjunctions.
     * 
     * @throws Exception
     */
    public void validate() throws Exception {
        checkPredicate(true);
    }

    /**
     * Check to ensure that the MultiConditionPredicateComponent contains a list
     * of valid predicates and conjunctions.
     * 
     * This check does not ensure that the list does not begin or end with a
     * conjunction. It does check the existing list to ensure that it alternates
     * between predicates and conjunctions.
     * 
     * @throws Exception
     */
    public void checkPredicate() throws IhfsDatabaseException {
        checkPredicate(false);
    }

    /**
     * Check to ensure that the MultiConditionPredicateComponent is valid
     * 
     * All contained sub predicates that it contains must be valid. Predicates
     * must be connected with valid conjunctions.
     * 
     * @param isCompletePredicate
     *            Flag that when set will ensure that the list of predicate
     *            components does not begin or end with a conjunction.
     * @throws IhfsDatabaseException
     */
    public void checkPredicate(boolean isCompletePredicate)
            throws IhfsDatabaseException {

        QueryPredicateComponent previousPredicateComponent = null;
        QueryPredicateComponent currentPredicateComponent = null;

        for (QueryPredicateComponent queryPredicateComponent : queryPredicateComponentList) {
            previousPredicateComponent = currentPredicateComponent;
            currentPredicateComponent = queryPredicateComponent;
            checkPredicateOrdering(previousPredicateComponent,
                    currentPredicateComponent);
            currentPredicateComponent.checkPredicate();
        }
        if (isCompletePredicate == true) {
            if ((previousPredicateComponent != null)
                    && (currentPredicateComponent != null)) {
                checkPredicateOrdering(currentPredicateComponent, null);
            }
        }
    }

    public void addToStringBuilder(StringBuilder sb, boolean useFullyQualified)
            throws IhfsDatabaseException {
        if (sb != null) {
            sb.append(" ");
            sb.append(IhfsConstants.OPEN_PAREN);
            for (QueryPredicateComponent queryPredicateComponent : queryPredicateComponentList) {
                queryPredicateComponent.addToStringBuilder(sb,
                        useFullyQualified);
            }
            sb.append(IhfsConstants.CLOSE_PAREN);
            sb.append(" ");
        }
    }

    private final void checkAgainstLastPredicate(
            QueryPredicateComponent currentPredicateComponent)
            throws IhfsDatabaseException {
        if ((this.queryPredicateComponentList != null)
                && (this.queryPredicateComponentList.isEmpty() == false)) {
            QueryPredicateComponent lastPredicateComponent = null;
            int len = this.queryPredicateComponentList.size();
            if (len > 0) {
                lastPredicateComponent = this.queryPredicateComponentList
                        .get(len - 1);
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
            throw (new IhfsDatabaseException(
                    "No MultiConditionPredicateComponent DATA"));
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
            checkAgainstLastPredicate(currentPredicate);
        } else {
            throw (new IhfsDatabaseException(
                    "Unknown. Unexpected Query Predicate condition"));
        }
    }

}

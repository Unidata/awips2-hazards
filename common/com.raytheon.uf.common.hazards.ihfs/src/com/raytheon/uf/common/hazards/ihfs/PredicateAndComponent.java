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
 * This singleton class is part of the Object Query for the IHFS (Hydro)
 * database. This class represents an "AND" conjunction component for Query
 * Predicate construction. This is a final class and cannot be further extended.
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
public final class PredicateAndComponent extends AbstractConjunctionComponent {

    private static PredicateAndComponent myInstance = null;

    /**
     * Private constructor.
     */
    private PredicateAndComponent() {
        super(IhfsConstants.AND);
    }

    /**
     * Get AND instance.
     * 
     * @return
     */
    public static synchronized PredicateAndComponent getInstance() {
        if (myInstance == null) {
            myInstance = new PredicateAndComponent();
        }
        return (myInstance);
    }

}

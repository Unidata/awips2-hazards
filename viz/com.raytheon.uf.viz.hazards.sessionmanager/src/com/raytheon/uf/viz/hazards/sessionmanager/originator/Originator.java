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
 **/
package com.raytheon.uf.viz.hazards.sessionmanager.originator;

/**
 * Catch-all originator for session based origins of notifications not covered
 * by other originators.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 1, 2014            mnash        Initial creation
 * May 13, 2016   15676   Chris.Golden Added "database" as an originator.
 * Oct 05, 2016   22870   Chris.Golden Added "CAVE" as an originator.
 * Dec 17, 2017   20739   Chris.Golden Added methods to determine whether
 *                                     or not they are the result of direct
 *                                     user input, and whether or not they
 *                                     require hazard events to not be
 *                                     locked by other workstations.
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */
public enum Originator implements IOriginator {
    DATABASE, CAVE, OTHER;

    @Override
    public boolean isDirectResultOfUserInput() {
        return false;
    }

    @Override
    public boolean isNotLockedByOthersRequired() {
        return false;
    }
}

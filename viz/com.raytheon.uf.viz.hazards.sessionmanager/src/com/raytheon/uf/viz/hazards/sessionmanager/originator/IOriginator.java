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
 * Marker interface for originators
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Feb 06, 2014            mnash        Initial creation.
 * Dec 17, 2017  20739     Chris.Golden Added methods to determine whether
 *                                      or not they are the result of direct
 *                                      user input, and whether or not they
 *                                      require hazard events to not be
 *                                      locked by other workstations.
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */

public interface IOriginator {

    /**
     * Determine whether or not this originator was a direct result of user
     * input.
     * 
     * @return <code>true</code> if the user directly caused actions with this
     *         originator, <code>false</code> otherwise.
     */
    public boolean isDirectResultOfUserInput();

    /**
     * Determine whether or not actions with this originator should, when
     * applying to hazard events, require that the event not be locked by
     * another workstation.
     * 
     * @return <code>true</code> if a hazard event change with this originator
     *         requires that the event not be locked by another workstation,
     *         <code>false</code> otherwise.
     */
    public boolean isNotLockedByOthersRequired();
}
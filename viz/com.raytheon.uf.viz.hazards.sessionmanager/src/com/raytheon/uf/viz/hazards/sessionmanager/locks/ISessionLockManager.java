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
package com.raytheon.uf.viz.hazards.sessionmanager.locks;

import java.util.Set;

import com.raytheon.uf.common.dataplugin.events.locks.LockInfo;
import com.raytheon.uf.common.dataplugin.events.locks.LockInfo.LockStatus;
import com.raytheon.uf.common.message.WsId;
import com.raytheon.uf.viz.hazards.sessionmanager.ISessionManager;

/**
 * Interface describing the methods that must be implemented for a class that
 * acts as a session manager of hazard event locks.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer      Description
 * ------------ ---------- ------------- --------------------------
 * Dec 12, 2016   21504    Robert.Blum   Initial creation.
 * </pre>
 * 
 * @author Robert.Blum
 * @version 1.0
 * @param <E>
 */
public interface ISessionLockManager {

    /**
     * Lock the specified hazard event.
     * 
     * @param eventId
     *            Identifier of the event to lock.
     * @return <code>true</code> if the lock was granted, <code>false</code>
     *         otherwise.
     */
    public boolean lockHazardEvent(String eventId);

    /**
     * Lock the specified hazard events.
     * 
     * @param eventIds
     *            Identifiers of the events to lock.
     * @return <code>true</code> if the locks were granted, <code>false</code>
     *         otherwise.
     */
    public boolean lockHazardEvents(Set<String> eventIds);

    /**
     * Lock the specified hazard events for product generation, registering any
     * of said events that are not already locked as to be unlocked
     * automatically when {@link ISessionManager#isPreviewOngoing()} and
     * {@link ISessionManager#isIssueOngoing()} both yield <code>false</code>.
     * 
     * @param eventIds
     *            Identifiers of the events to lock for product generation.
     * @return <code>true</code> if the locks were granted, <code>false</code>
     *         otherwise.
     * @deprecated This method requires too much knowledge of the product
     *             generation process on the part of the lock manager. It is
     *             only needed at this time because the code to unset preview-
     *             and issue-ongoing when product generation is cancelled is
     *             scattered all through the H.S. client code base. At such time
     *             as the latter is easier to track, the implementation of this
     *             method (a) be moved into <code>lockHazardEvents()</code>,
     *             with the latter gaining an extra boolean parameter indicating
     *             whether or not product generation is the goal; and (b) should
     *             do everything it does now, but without the unlocking when
     *             preview-/issue-ongoing are both <code>false</code>. Likewise,
     *             <code>unlockHazardEvents()</code> will need to be modified to
     *             take the same extra parameter, and when that parameter is
     *             <code>true</code>, it will simply see which events have
     *             identifiers listed in the set maintained here, and unlock
     *             those. This will provide a better separation of concerns.
     */
    @Deprecated
    public boolean lockHazardEventsForProductGeneration(Set<String> eventIds);

    /**
     * Break (override) an existing lock for the specified hazard event.
     * 
     * @param eventId
     *            Identifier of the event for which to override the lock.
     * @return <code>true</code> if the lock was broken, <code>false</code>
     *         otherwise.
     */
    public boolean breakHazardEventLock(String eventId);

    /**
     * Unlock the specified hazard event.
     * 
     * @param eventId
     *            Identifier of the event for which to override the lock.
     * @return <code>true</code> if the unlock was granted, <code>false</code>
     *         otherwise.
     */
    public boolean unlockHazardEvent(String eventId);

    /**
     * Unlock the specified hazard events.
     * 
     * @param eventIds
     *            Identifiers of the events for which to override the locks.
     * @return <code>true</code> if the unlocks were granted, <code>false</code>
     *         otherwise.
     */
    public boolean unlockHazardEvents(Set<String> eventIds);

    /**
     * Get information about the lock for the specified hazard event.
     * 
     * @param eventId
     *            Identifier of the event for which to fetch lock information.
     * @return Information about the lock.
     */
    public LockInfo getHazardEventLockInfo(String eventId);

    /**
     * Get the lock status for the specified hazard event.
     * 
     * @param eventId
     *            Identifier of the event for which to fetch lock status.
     * @return Status of the lock.
     */
    public LockStatus getHazardEventLockStatus(String eventId);

    /**
     * Get information about the workstation that holds the lock for the
     * specified hazard event, if any.
     * 
     * @param eventId
     *            Identifier of the event for which to fetch the lock's
     *            workstation information.
     * @return Information about the workstation holding the lock on the event,
     *         or <code>null</code> if the event is not locked.
     */
    public WsId getWorkStationHoldingHazardLock(String eventId);

    /**
     * Get the identifiers of hazard events that have the specified lock status.
     * 
     * @param status
     *            Lock status to be matched.
     * @return Identifiers of the hazard events that have lock statuses that
     *         match that provided.
     */
    public Set<String> getHazardsWithLockStatus(LockStatus status);

    /**
     * Shut down.
     */
    public void shutdown();
}

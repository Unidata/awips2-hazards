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
package com.raytheon.uf.viz.hazards.sessionmanager.time;

import com.raytheon.uf.viz.hazards.sessionmanager.ISessionNotification;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.IOriginator;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.OriginatedSessionNotification;

import gov.noaa.gsd.common.utilities.MergeResult;

/**
 * Notification that will be sent out to notify all components that the selected
 * time has changed.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Jun 11, 2013    1257    bsteffen     Initial creation.
 * Nov 18, 2014    4124    Chris.Golden Changed to use originator and work
 *                                      with revamped time manager.
 * Sep 27, 2017   38072    Chris.Golden Implemented merge() method.
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public class SelectedTimeChanged extends OriginatedSessionNotification {

    // Private Variables

    /**
     * Time manager.
     */
    private final ISessionTimeManager timeManager;

    /**
     * New selected time.
     */
    private final SelectedTime selectedTime;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param timeManager
     *            Time manager.
     * @param selectedTime
     *            New selected time.
     * @param originator
     *            Originator of the change.
     */
    public SelectedTimeChanged(ISessionTimeManager timeManager,
            SelectedTime selectedTime, IOriginator originator) {
        super(originator);
        this.timeManager = timeManager;
        this.selectedTime = selectedTime;
    }

    // Public Methods

    /**
     * Get the time manager.
     * 
     * @return Time manager.
     */
    public ISessionTimeManager getTimeManager() {
        return timeManager;
    }

    /**
     * Get the selected time as of the creation of this notification.
     * 
     * @return Selected time.
     */
    public SelectedTime getSelectedTime() {
        return selectedTime;
    }

    @Override
    public MergeResult<ISessionNotification> merge(
            ISessionNotification original, ISessionNotification modified) {
        return getMergeResultNullifyingSubjectIfSameClassAndOriginator(original,
                modified);
    }
}

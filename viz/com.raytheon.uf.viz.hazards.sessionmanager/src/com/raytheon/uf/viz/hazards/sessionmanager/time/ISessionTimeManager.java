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

import gov.noaa.gsd.common.utilities.ICurrentTimeProvider;

import java.util.Date;

import com.raytheon.uf.common.time.TimeRange;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.IOriginator;

/**
 * Manages selected, current, and visible times for a session.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 21, 2013 1257       bsteffen    Initial creation
 * May 12, 2014 2925       C. Golden   Added originator to visible time
 *                                     range change notification, and
 *                                     added current time provider and
 *                                     getter.
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */

public interface ISessionTimeManager {

    /**
     * get the current time the user has selected.
     * 
     * @return
     */
    public Date getSelectedTime();

    /**
     * Get the current time range the user has selected, in single selection
     * mode this will be an invalid range.
     * 
     * @return
     */
    public TimeRange getSelectedTimeRange();

    /**
     * set the current time the user has selected.
     * 
     * @param selectedTime
     */
    public void setSelectedTime(Date selectedTime);

    /**
     * Set the current time range the user has selected
     * 
     * @param selectedTimeRange
     */
    public void setSelectedTimeRange(TimeRange selectedTimeRange);

    /**
     * Get the current system time.
     * 
     * @return
     */
    public Date getCurrentTime();

    /**
     * Get the current time provider.
     * 
     * @return
     */
    public ICurrentTimeProvider getCurrentTimeProvider();

    /**
     * Set the range of times that should be visible to the user.
     * 
     * @param range
     * @param originator
     */
    public void setVisibleRange(TimeRange range, IOriginator originator);

    /**
     * Get the range of times that should be visible to the user.
     * 
     * @param range
     */
    public TimeRange getVisibleRange();

    /**
     * Execute any shutdown needed.
     */
    public void shutdown();

}

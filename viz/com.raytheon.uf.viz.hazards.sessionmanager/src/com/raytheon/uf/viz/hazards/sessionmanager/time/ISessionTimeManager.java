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
 * Nov 18, 2014 4124       C. Golden   Reorganized and changed over to
 *                                     use a SelectedTime object to
 *                                     represent both single selected
 *                                     time instances, and selected
 *                                     time ranges.
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */

public interface ISessionTimeManager {

    /**
     * Get the current time provider.
     * 
     * @return Current time provider.
     */
    public ICurrentTimeProvider getCurrentTimeProvider();

    /**
     * Get the current system time, as an epoch time in milliseconds.
     * 
     * @return Current system time, as an epoch time in milliseconds.
     */
    public long getCurrentTimeInMillis();

    /**
     * Get the current system time.
     * 
     * @return Current system time.
     */
    public Date getCurrentTime();

    /**
     * Get the lower bound of the selected time range, as an epoch time in
     * milliseconds.
     * 
     * @return Lower bound of the selected time range, as an epoch time in
     *         milliseconds.
     */
    public long getLowerSelectedTimeInMillis();

    /**
     * Get the upper bound of the selected time range, as an epoch time in
     * milliseconds.
     * 
     * @return Upper bound of the selected time range, as an epoch time in
     *         milliseconds.
     */
    public long getUpperSelectedTimeInMillis();

    /**
     * Get the selected time.
     * 
     * @return Selected time.
     */
    public SelectedTime getSelectedTime();

    /**
     * Set the selected time.
     * 
     * @param selectedTime
     *            New selected time.
     * @param originator
     *            Originator of the action.
     */
    public void setSelectedTime(SelectedTime selectedTime,
            IOriginator originator);

    /**
     * Get the lower bound of the visible time range, as an epoch time in
     * milliseconds.
     * 
     * @return Lower bound of the visible time range, as an epoch time in
     *         milliseconds.
     */
    public long getLowerVisibleTimeInMillis();

    /**
     * Get the upper bound of the visible time range, as an epoch time in
     * milliseconds.
     * 
     * @return Upper bound of the visible time range, as an epoch time in
     *         milliseconds.
     */
    public long getUpperVisibleTimeInMillis();

    /**
     * Get the range of times that should be visible to the user.
     * 
     * @return Range of times that should be visible to the user.
     */
    public TimeRange getVisibleTimeRange();

    /**
     * Set the range of times that should be visible to the user.
     * 
     * @param timeRange
     *            New range of times that should be visible to the user.
     * @param originator
     *            Originator of the action.
     */
    public void setVisibleTimeRange(TimeRange timeRange, IOriginator originator);

    /**
     * Execute any shutdown needed.
     */
    public void shutdown();

}

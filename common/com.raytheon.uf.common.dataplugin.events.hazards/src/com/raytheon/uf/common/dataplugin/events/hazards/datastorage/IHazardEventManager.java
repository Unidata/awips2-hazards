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
package com.raytheon.uf.common.dataplugin.events.hazards.datastorage;

import java.util.Collection;
import java.util.Map;

import com.raytheon.uf.common.dataplugin.events.EventSet;
import com.raytheon.uf.common.dataplugin.events.datastorage.IEventManager;
import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IReadableHazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.collections.HazardHistoryList;
import com.raytheon.uf.common.dataplugin.events.hazards.registry.HazardEventServiceException;
import com.raytheon.uf.common.dataplugin.events.hazards.request.HazardEventQueryRequest;

/**
 * Any new hazard event manager must implement this interface, which provides
 * various methods for retrieving hazards based on hazard specific information
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Oct 8, 2012            mnash         Initial creation.
 * May 29, 2015   6895    Ben.Phillippe Refactored Hazard Service data access
 * Aug 20, 2015   6895    Ben.Phillippe Routing registry requests through
 *                                      request server
 * Jan 26, 2016   7623    Ben.Phillippe Implemented locking of HazardEvents
 * Mar 14, 2016  12145    mduff         Cleaned up error handling.
 * Dec 12, 2016  21504    Robert.Blum   Moved locking code to SessionLockManager.
 * Feb 16, 2017  29138    Chris.Golden  Revamped to allow for the querying of
 *                                      historical versions of events, or
 *                                      latest (non-historical) versions, or
 *                                      both. Also added a method to allow for
 *                                      querying the size of a history list,
 *                                      so that the whole history list does not
 *                                      have to be shipped back to the client.
 * Feb 27, 2017  29138    Chris.Golden  Added method to get latest hazard
 *                                      events by site ID.
 * Apr 13, 2017  33142    Chris.Golden  Added ability to delete all events
 *                                      with a particular event identifier.
 * Dec 17, 2017  20739    Chris.Golden  Refactored away access to directly
 *                                      mutable session events.
 * Feb 23, 2018  28387    Chris.Golden  Added getLatestByPhenomenonsAndSiteID()
 *                                      method.
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */

public interface IHazardEventManager extends
        IEventManager<IReadableHazardEvent, HazardEvent, HazardHistoryList> {

    /**
     * Execute the specified query of the registry for history lists of hazard
     * events.
     * 
     * @param request
     *            Query request to be executed.
     * @return Map of event identifiers to their history lists.
     * @throws HazardEventServiceException
     *             If a problem occurs when attempting to query the history.
     */
    Map<String, HazardHistoryList> queryHistory(HazardEventQueryRequest request)
            throws HazardEventServiceException;

    /**
     * Execute the specified query of the registry for the latest versions of
     * hazard events.
     * 
     * @param request
     *            Query request to be executed.
     * @return Map of event identifiers to their history lists.
     * @throws HazardEventServiceException
     *             If a problem occurs when attempting to query the latest.
     */
    Map<String, HazardEvent> queryLatest(HazardEventQueryRequest request)
            throws HazardEventServiceException;

    /**
     * Retrieve the history lists of all hazards with the specified site
     * identifier.
     * 
     * @param site
     *            Site identifier.
     * @param includeLatestVersion
     *            Flag indicating whether or not non-historical latest versions
     *            should be included in the history lists (in each case as the
     *            last item).
     * @return Map of event identifiers to their history lists.
     */
    Map<String, HazardHistoryList> getHistoryBySiteID(String site,
            boolean includeLatestVersion);

    /**
     * Retrieve the history lists of all hazards with specified phenomenon.
     * 
     * @param phenomenon
     *            Phenomenon.
     * @param includeLatestVersion
     *            Flag indicating whether or not non-historical latest versions
     *            should be included in the history lists (in each case as the
     *            last item).
     * @return Map of event identifiers to their history lists.
     */
    Map<String, HazardHistoryList> getHistoryByPhenomenon(String phenomenon,
            boolean includeLatestVersion);

    /**
     * Retrieve the history lists of all hazards with the specified
     * significance.
     * 
     * @param significance
     *            Significance.
     * @param includeLatestVersion
     *            Flag indicating whether or not non-historical latest versions
     *            should be included in the history lists (in each case as the
     *            last item).
     * @return Map of event identifiers to their history lists.
     */
    Map<String, HazardHistoryList> getHistoryBySignificance(String significance,
            boolean includeLatestVersion);

    /**
     * Retrieve the history lists of all hazards with specified phenomenon and
     * significance (phensig).
     * 
     * @param phenomenon
     *            Phenomenon.
     * @param significance
     *            Significance.
     * @param includeLatestVersion
     *            Flag indicating whether or not non-historical latest versions
     *            should be included in the history lists (in each case as the
     *            last item).
     * @return Map of event identifiers to their history lists.
     */
    Map<String, HazardHistoryList> getHistoryByPhenSig(String phen, String sig,
            boolean includeLatestVersion);

    /**
     * Retrieve the history list for the specified event identifier.
     * 
     * @param eventIdentifier
     *            Event identifier.
     * @param includeLatestVersion
     *            Flag indicating whether or not non-historical latest versions
     *            should be included in the history list (as the last item).
     * @return History list for the specified hazard identifier.
     */
    HazardHistoryList getHistoryByEventID(String eventIdentifier,
            boolean includeLatestVersion);

    /**
     * Retrieve the size of the history list for the specified event identifier
     * This may be used in place of
     * {@link #getHistoryByEventID(String, boolean)} in situations where the
     * size of the history list is desired, but the event versions themselves
     * are not needed, since this method requires no transport of serialized
     * hazard events.
     * 
     * @param eventIdentifier
     *            Event identifier.
     * @param includeLatestVersion
     *            Flag indicating whether or not non-historical latest versions
     *            should be included in the size of the history list.
     * @return Size of the history list for the specified hazard identifier, or
     *         <code>-1</code> if no such event is found.
     */
    int getHistorySizeByEventID(String eventIdentifier,
            boolean includeLatestVersion);

    /**
     * Retrieve the latest version of the specified event identifier.
     * 
     * @param eventIdentifier
     *            Event identifier.
     * @param includeHistoricalVersion
     *            Flag indicating whether or not historical latest version
     *            should be included if that is later than the non-historical
     *            latest version.
     * @return Latest version of the hazard event.
     */
    HazardEvent getLatestByEventID(String eventIdentifier,
            boolean includeHistoricalVersion);

    /**
     * Retrieve the latest version of all hazards with the specified site
     * identifier.
     * 
     * @param site
     *            Site identifier.
     * @param includeHistoricalVersions
     *            Flag indicating whether or not historical latest versions
     *            should be included if they are later than the non-historical
     *            latest versions.
     * @return Map of event identifiers to their latest versions.
     */
    Map<String, HazardEvent> getLatestBySiteID(String site,
            boolean includeHistoricalVersions);

    /**
     * Retrieve the latest version of all hazards with the specified site
     * identifier and phenomenons.
     * 
     * @param phenomenons
     *            Phenomenons.
     * @param site
     *            Site identifier.
     * @param includeHistoricalVersions
     *            Flag indicating whether or not historical latest versions
     *            should be included if they are later than the non-historical
     *            latest versions.
     * @return Map of event identifiers to their latest versions.
     */
    Map<String, HazardEvent> getLatestByPhenomenonsAndSiteID(
            Collection<String> phenomenons, String site,
            boolean includeHistoricalVersions);

    /**
     * Store the specified set of events.
     * 
     * @param set
     *            Set of events.
     */
    void storeEventSet(EventSet<HazardEvent> set);

    /**
     * Remove all copies of events with the specified identifier.
     * 
     * @param eventIdentifier
     *            Identifier of the event for which to remove all copies.
     * @return <code>true</code> if the events were removed, <code>false</code>
     *         otherwise.
     */
    boolean removeAllCopiesOfEvent(String eventIdentifier);

    /**
     * Remove all events. This may not be implemented in all cases.
     * 
     * @return <code>true</code> if the events were removed, <code>false</code>
     *         otherwise.
     */
    boolean removeAllEvents();
}

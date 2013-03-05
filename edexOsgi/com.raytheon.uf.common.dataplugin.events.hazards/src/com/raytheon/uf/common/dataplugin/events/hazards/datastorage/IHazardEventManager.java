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

import java.util.Map;

import com.raytheon.uf.common.dataplugin.events.datastorage.IEventManager;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.collections.HazardEventSet;
import com.raytheon.uf.common.dataplugin.events.hazards.event.collections.HazardHistoryList;

/**
 * Any new hazard event manager must implement this interface, which provides
 * various methods for retrieving hazards based on hazard specific information
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 8, 2012            mnash     Initial creation
 * 
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */

public interface IHazardEventManager extends
        IEventManager<IHazardEvent, HazardHistoryList> {

    /**
     * Retrieve all hazards with the issuing site that was passed in
     * 
     * @param site
     *            - a {@link String}
     * @return the history of hazards in a map keyed by the event id
     */
    Map<String, HazardHistoryList> getBySiteID(String site);

    /**
     * Retrieve all hazards with phenomenon that was passed in
     * 
     * @param phenomenon
     *            - a {@link String}
     * @return the history of hazards in a map keyed by the event id
     */
    Map<String, HazardHistoryList> getByPhenomenon(String phenomenon);

    /**
     * Retrieve all hazards with significance that was passed in
     * 
     * @param phenomenon
     *            - a {@link String}
     * @return the history of hazards in a map keyed by the event id
     */
    Map<String, HazardHistoryList> getBySignificance(String significance);

    /**
     * Retrieve all hazards with the phensig that was passed in
     * 
     * @param phen
     *            - a {@link String}
     * @param sig
     *            - a {@link String}
     * @return the history of hazards in a map keyed by the event id
     */
    Map<String, HazardHistoryList> getByPhensig(String phen, String sig);

    /**
     * Takes and eventId and returns all hazards that use that event id. This
     * returns a {@link HazardHistoryList}, which is an ordered list of the
     * history of the hazard.
     * 
     * @param eventId
     *            - a {@link String}
     * @return the history of hazards in a list
     */
    HazardHistoryList getByEventID(String eventId);

    /**
     * Stores a set of events that were grouped together.
     * 
     * @param set
     *            - a {@link HazardEventSet}
     */
    void storeEventSet(HazardEventSet set);
}

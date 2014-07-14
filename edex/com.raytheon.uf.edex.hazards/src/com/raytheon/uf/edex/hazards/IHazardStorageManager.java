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
package com.raytheon.uf.edex.hazards;

import java.util.List;
import java.util.Map;

import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.collections.HazardHistoryList;

/**
 * Interface for ways to access the database, with basic CRUD methods.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 9, 2012            mnash     Initial creation
 * 
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */

public interface IHazardStorageManager<T extends IHazardEvent> {

    /**
     * Store an {@link IHazardEvent} to some sort of storage system as defined
     * by the implementing class.
     * 
     * @param event
     */
    public void store(T event);

    /**
     * Update an {@link IHazardEvent} to some sort of storage system as defined
     * by the implementing class.
     * 
     * @param event
     */
    public void update(T event);

    /**
     * Delete an {@link IHazardEvent} from some sort of storage system as
     * defined by the implementing class.
     * 
     * @param event
     */
    public void delete(T event);

    public void deleteAll(List<T> events);

    /**
     * Retrieve a Map<String,List<IHazardEvent>> from the storage system as
     * defined by the implementing class. The Map contains the event id as the
     * key, and the list of all the hazards with that event id as the value.
     * 
     * @param filters
     * @return
     */
    public Map<String, HazardHistoryList> retrieve(
            Map<String, List<Object>> filters);

}

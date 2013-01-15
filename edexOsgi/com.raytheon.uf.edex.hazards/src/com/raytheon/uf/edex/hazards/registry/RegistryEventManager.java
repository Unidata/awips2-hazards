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
package com.raytheon.uf.edex.hazards.registry;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.collections.HazardHistoryList;
import com.raytheon.uf.common.registry.handler.RegistryHandlerException;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.edex.hazards.IHazardStorageManager;
import com.vividsolutions.jts.geom.Geometry;

/**
 * Manager for calls to the registry for CRUD operations
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

public class RegistryEventManager implements IHazardStorageManager<HazardEvent> {
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(RegistryEventManager.class);

    private HazardRegistryHandler handler;

    private static RegistryEventManager instance;

    public static RegistryEventManager getInstance() {
        if (instance == null) {
            instance = new RegistryEventManager();
        }
        return instance;
    }

    private RegistryEventManager() {
        handler = new HazardRegistryHandler();
    }

    @Override
    public void store(HazardEvent event) {
        try {
            handler.store(event);
            statusHandler.handle(Priority.INFO, "Hazard " + event.getEventId()
                    + " successfully stored to registry");
        } catch (RegistryHandlerException e) {
            statusHandler.handle(
                    Priority.ERROR,
                    "Unable to store " + event.getSite() + "-"
                            + event.getEventId() + "-" + event.getIssueTime(),
                    e);

        }
    }

    @Override
    public void update(HazardEvent event) {
        try {
            handler.update(event);
            statusHandler.handle(Priority.INFO, "Hazard " + event.getEventId()
                    + " successfully updated in registry");
        } catch (RegistryHandlerException e) {
            statusHandler.handle(
                    Priority.ERROR,
                    "Unable to update " + event.getSite() + "-"
                            + event.getEventId() + "-" + event.getIssueTime(),
                    e);
        }
    }

    @Override
    public void delete(HazardEvent event) {
        try {
            handler.delete(event);
            statusHandler.handle(Priority.INFO, "Hazard " + event.getEventId()
                    + " successfully deleted from registry");
        } catch (RegistryHandlerException e) {
            statusHandler.handle(
                    Priority.ERROR,
                    "Unable to delete " + event.getSite() + "-"
                            + event.getEventId() + "-" + event.getIssueTime(),
                    e);
        }
    }

    @Override
    public Map<String, HazardHistoryList> retrieve(Map<String, Object> filters) {
        Map<String, HazardHistoryList> events = new HashMap<String, HazardHistoryList>();
        try {
            // TODO, need to implement the following :
            // get by other values
            Set<HazardEvent> listEvents = new HashSet<HazardEvent>();
            if (filters != null) {
                for (Entry<String, Object> entry : filters.entrySet()) {
                    String finalKey = entry.getKey();
                    if (finalKey.equals(HazardConstants.GEOMETRY)) {
                        listEvents.addAll(handler
                                .getByGeometry((Geometry) entry.getValue()));
                    } else if (finalKey.equals(HazardConstants.STARTTIME)
                            || finalKey.equals(HazardConstants.ENDTIME)) {
                        listEvents.addAll(handler.getByTimeRange(
                                (Date) filters.get(HazardConstants.STARTTIME),
                                (Date) filters.get(HazardConstants.ENDTIME)));
                    } else {
                        listEvents.addAll(handler.getByFilter(filters));
                    }
                }

            } else {
                listEvents.addAll(handler.getAll());
            }
            for (HazardEvent event : listEvents) {
                if (events.containsKey(event.getEventId())) {
                    events.get(event.getEventId()).add((HazardEvent) event);
                } else {
                    HazardHistoryList list = new HazardHistoryList();
                    list.add((HazardEvent) event);
                    events.put(event.getEventId(), list);
                }
            }
        } catch (RegistryHandlerException e) {
            String message = "";
            for (String key : filters.keySet()) {
                message += key + ":" + filters.get(key) + ",";
            }
            statusHandler.handle(Priority.ERROR,
                    "Unable to retrieve hazards based on " + message, e);
        }
        return events;
    }
}

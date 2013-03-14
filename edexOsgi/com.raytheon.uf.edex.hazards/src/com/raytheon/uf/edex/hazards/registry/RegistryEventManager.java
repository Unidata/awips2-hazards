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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
import com.vividsolutions.jts.geom.GeometryFactory;

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
    private static final IUFStatusHandler statusHandler = UFStatus
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
            statusHandler.handle(Priority.INFO, "Hazard " + event.getEventID()
                    + " successfully stored to registry");
        } catch (RegistryHandlerException e) {
            statusHandler.handle(
                    Priority.ERROR,
                    "Unable to store " + event.getSiteID() + "-"
                            + event.getEventID() + "-" + event.getIssueTime(),
                    e);

        }
    }

    @Override
    public void update(HazardEvent event) {
        try {
            handler.update(event);
            statusHandler.handle(Priority.INFO, "Hazard " + event.getEventID()
                    + " successfully updated in registry");
        } catch (RegistryHandlerException e) {
            statusHandler.handle(
                    Priority.ERROR,
                    "Unable to update " + event.getSiteID() + "-"
                            + event.getEventID() + "-" + event.getIssueTime(),
                    e);
        }
    }

    @Override
    public void delete(HazardEvent event) {
        try {
            handler.delete(event);
            statusHandler.handle(Priority.INFO, "Hazard " + event.getEventID()
                    + " successfully deleted from registry");
        } catch (RegistryHandlerException e) {
            statusHandler.handle(
                    Priority.ERROR,
                    "Unable to delete " + event.getSiteID() + "-"
                            + event.getEventID() + "-" + event.getIssueTime(),
                    e);
        }
    }

    @Override
    public Map<String, HazardHistoryList> retrieve(
            Map<String, List<Object>> filters) {
        Map<String, HazardHistoryList> events = new HashMap<String, HazardHistoryList>();
        try {
            // TODO, need to implement the following :
            // get by other values
            Set<HazardEvent> listEvents = new HashSet<HazardEvent>();
            if (filters != null) {
                GeometryFactory factory = new GeometryFactory();
                for (Entry<String, List<Object>> entry : filters.entrySet()) {
                    String finalKey = entry.getKey();
                    if (finalKey.equals(HazardConstants.GEOMETRY)) {
                        List<Geometry> geoms = new ArrayList<Geometry>();
                        for (Object ob : entry.getValue()) {
                            geoms.add((Geometry) ob);
                        }
                        listEvents.addAll(handler.getByGeometry(factory
                                .buildGeometry(geoms)));
                    } else if (finalKey.equals(HazardConstants.STARTTIME)
                            || finalKey.equals(HazardConstants.ENDTIME)) {
                        // we will not support any more than two times, it
                        // doesn't make any sense, we will only support the
                        // first 2 in the filter map
                        if (filters.containsKey(HazardConstants.STARTTIME)
                                && filters.containsKey(HazardConstants.ENDTIME)) {
                            listEvents.addAll(handler.getByTimeRange(
                                    (Date) filters.get(
                                            HazardConstants.STARTTIME).get(0),
                                    (Date) filters.get(HazardConstants.ENDTIME)
                                            .get(0)));
                        } else if (filters
                                .containsKey(HazardConstants.STARTTIME)
                                && filters.containsKey(HazardConstants.ENDTIME) == false) {
                            listEvents.addAll(handler.getByTimeRange(
                                    (Date) filters.get(
                                            HazardConstants.STARTTIME).get(0),
                                    (Date) filters.get(
                                            HazardConstants.STARTTIME).get(1)));
                        } else {
                            listEvents.addAll(handler.getByTimeRange(
                                    (Date) filters.get(HazardConstants.ENDTIME)
                                            .get(0),
                                    (Date) filters.get(HazardConstants.ENDTIME)
                                            .get(1)));
                        }
                    } else if (finalKey.equals(HazardConstants.ENDTIME)) {
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
                if (events.containsKey(event.getEventID())) {
                    events.get(event.getEventID()).add((HazardEvent) event);
                } else {
                    HazardHistoryList list = new HazardHistoryList();
                    list.add((HazardEvent) event);
                    events.put(event.getEventID(), list);
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

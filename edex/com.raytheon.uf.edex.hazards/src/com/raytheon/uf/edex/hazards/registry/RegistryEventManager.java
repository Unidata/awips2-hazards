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
import com.raytheon.uf.common.dataplugin.events.hazards.HazardNotification.NotificationType;
import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.HazardEventManager.Mode;
import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.HazardQueryBuilder;
import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.collections.HazardHistoryList;
import com.raytheon.uf.common.registry.RegistryHandler;
import com.raytheon.uf.common.registry.ebxml.RegistryUtil;
import com.raytheon.uf.common.registry.handler.RegistryHandlerException;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.edex.core.EDEXUtil;
import com.raytheon.uf.edex.hazards.HazardNotifier;
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
 * Oct 30, 2013 #1472     bkowal    Implemented retrieval from the registry
 *                                  by phensig.
 * Nov 04, 2013 2182     daniel.s.schaffer@noaa.gov      Started refactoring
 * Mar 24, 2014 #3323      bkowal   Include the mode in the hazard notification.
 * 
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */

public class RegistryEventManager implements IHazardStorageManager<HazardEvent> {
    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(RegistryEventManager.class);

    private static final Mode MODE = Mode.OPERATIONAL;

    private final HazardRegistryHandler handler;

    private static RegistryEventManager instance;

    public static RegistryEventManager getInstance() {
        if (instance == null) {
            instance = new RegistryEventManager();
        }
        return instance;
    }

    private RegistryEventManager() {
        handler = new HazardRegistryHandler();
        handler.setRegistryHandler(EDEXUtil.getESBComponent(
                RegistryHandler.class, "registryHandler"));
    }

    @Override
    public void store(HazardEvent event) {
        try {
            handler.store(RegistryUtil.registryUser, event);
            statusHandler.handle(Priority.INFO, "Hazard " + event.getEventID()
                    + " successfully stored to registry");
            HazardNotifier.notify(event, NotificationType.STORE, MODE);
        } catch (RegistryHandlerException e) {
            statusHandler.handle(
                    Priority.ERROR,
                    "Unable to store " + event.getSiteID() + "-"
                            + event.getEventID() + "-"
                            + event.getCreationTime(), e);
        }
    }

    @Override
    public void update(HazardEvent event) {
        try {
            handler.update(RegistryUtil.registryUser, event);
            statusHandler.handle(Priority.INFO, "Hazard " + event.getEventID()
                    + " successfully updated in registry");
            HazardNotifier.notify(event, NotificationType.UPDATE, MODE);
        } catch (RegistryHandlerException e) {
            statusHandler.handle(
                    Priority.ERROR,
                    "Unable to update " + event.getSiteID() + "-"
                            + event.getEventID() + "-"
                            + event.getCreationTime(), e);
        }
    }

    @Override
    public void delete(HazardEvent event) {
        try {
            handler.delete(event);
            statusHandler.handle(Priority.INFO, "Hazard " + event.getEventID()
                    + " successfully deleted from registry");
            HazardNotifier.notify(event, NotificationType.DELETE, MODE);
        } catch (RegistryHandlerException e) {
            statusHandler.handle(
                    Priority.ERROR,
                    "Unable to delete " + event.getSiteID() + "-"
                            + event.getEventID() + "-"
                            + event.getCreationTime(), e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.edex.hazards.IHazardStorageManager#deleteAll(java.util
     * .List)
     */
    @Override
    public void deleteAll(List<HazardEvent> events) {
        try {
            handler.delete(events);
            statusHandler.handle(Priority.INFO,
                    "All hazards successfully deleted from registry");
        } catch (RegistryHandlerException e) {
            statusHandler.handle(Priority.ERROR,
                    "Unable to delete all hazards", e);
        }
        for (HazardEvent event : events) {
            HazardNotifier.notify(event, NotificationType.DELETE, MODE);
        }
        HazardNotifier.notify(null, NotificationType.DELETE_ALL, MODE);
    }

    @Override
    public Map<String, HazardHistoryList> retrieve(
            Map<String, List<Object>> filters) {
        Map<String, HazardHistoryList> events = new HashMap<String, HazardHistoryList>();
        try {
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
                    } else if (finalKey
                            .equals(HazardConstants.HAZARD_EVENT_START_TIME)
                            || finalKey
                                    .equals(HazardConstants.HAZARD_EVENT_END_TIME)) {
                        // we will not support any more than two times, it
                        // doesn't make any sense, we will only support the
                        // first 2 in the filter map
                        if (filters
                                .containsKey(HazardConstants.HAZARD_EVENT_START_TIME)
                                && filters
                                        .containsKey(HazardConstants.HAZARD_EVENT_END_TIME)) {
                            listEvents
                                    .addAll(handler
                                            .getByTimeRange(
                                                    (Date) filters
                                                            .get(HazardConstants.HAZARD_EVENT_START_TIME)
                                                            .get(0),
                                                    (Date) filters
                                                            .get(HazardConstants.HAZARD_EVENT_END_TIME)
                                                            .get(0)));
                        } else if (filters
                                .containsKey(HazardConstants.HAZARD_EVENT_START_TIME)
                                && filters
                                        .containsKey(HazardConstants.HAZARD_EVENT_END_TIME) == false) {
                            listEvents
                                    .addAll(handler
                                            .getByTimeRange(
                                                    (Date) filters
                                                            .get(HazardConstants.HAZARD_EVENT_START_TIME)
                                                            .get(0),
                                                    (Date) filters
                                                            .get(HazardConstants.HAZARD_EVENT_START_TIME)
                                                            .get(1)));
                        } else {
                            listEvents
                                    .addAll(handler
                                            .getByTimeRange(
                                                    (Date) filters
                                                            .get(HazardConstants.HAZARD_EVENT_END_TIME)
                                                            .get(0),
                                                    (Date) filters
                                                            .get(HazardConstants.HAZARD_EVENT_END_TIME)
                                                            .get(1)));
                        }
                    } else if (finalKey
                            .equals(HazardConstants.HAZARD_EVENT_END_TIME)) {
                        listEvents
                                .addAll(handler.getByTimeRange(
                                        (Date) filters
                                                .get(HazardConstants.HAZARD_EVENT_START_TIME),
                                        (Date) filters
                                                .get(HazardConstants.HAZARD_EVENT_END_TIME)));
                    } else if (finalKey.equals(HazardConstants.PHEN_SIG)) {
                        for (Object ob : entry.getValue()) {
                            String phensig = ob.toString();

                            String[] splitPhensig = phensig.split("\\.");
                            if (splitPhensig.length != 2
                                    && splitPhensig.length != 3) {
                                statusHandler.handle(Priority.WARN,
                                        "Improperly formatted phensig, skipping "
                                                + phensig);
                                continue;
                            }
                            HazardQueryBuilder hazardQueryBuilder = new HazardQueryBuilder();
                            hazardQueryBuilder
                                    .addKey(HazardConstants.PHENOMENON,
                                            splitPhensig[0]);
                            hazardQueryBuilder.addKey(
                                    HazardConstants.SIGNIFICANCE,
                                    splitPhensig[1]);
                            if (splitPhensig.length == 3) {
                                hazardQueryBuilder.addKey(
                                        HazardConstants.HAZARD_EVENT_SUB_TYPE,
                                        splitPhensig[2]);
                            }
                            listEvents
                                    .addAll(handler
                                            .getByFilter(hazardQueryBuilder
                                                    .getQuery()));
                        }
                    } else {
                        listEvents.addAll(handler.getByFilter(filters));
                    }
                }

            } else {
                listEvents.addAll(handler.getAll());
            }
            for (HazardEvent event : listEvents) {
                if (events.containsKey(event.getEventID())) {
                    events.get(event.getEventID()).add(event);
                } else {
                    HazardHistoryList list = new HazardHistoryList();
                    list.add(event);
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

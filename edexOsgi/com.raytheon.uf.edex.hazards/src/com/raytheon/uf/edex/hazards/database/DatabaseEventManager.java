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
package com.raytheon.uf.edex.hazards.database;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.event.PracticeHazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.PracticeHazardEventPK;
import com.raytheon.uf.common.dataplugin.events.hazards.event.collections.HazardHistoryList;
import com.raytheon.uf.common.geospatial.ISpatialQuery;
import com.raytheon.uf.common.geospatial.SpatialException;
import com.raytheon.uf.common.geospatial.SpatialQueryFactory;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.edex.database.dao.CoreDao;
import com.raytheon.uf.edex.database.dao.DaoConfig;
import com.raytheon.uf.edex.hazards.IHazardStorageManager;
import com.vividsolutions.jts.geom.Geometry;

/**
 * Event manager for calls to the database for hazards
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

public class DatabaseEventManager implements
        IHazardStorageManager<PracticeHazardEvent> {
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(DatabaseEventManager.class);

    private CoreDao dao;

    private static final DatabaseEventManager instance = new DatabaseEventManager();

    public static DatabaseEventManager getInstance() {
        return instance;
    }

    /**
     * 
     */
    private DatabaseEventManager() {
        dao = new CoreDao(DaoConfig.DEFAULT);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.common.dataplugin.events.datastorage.IEventManager#storeEvent
     * (com.raytheon.uf.common.dataplugin.events.IEvent)
     */
    @Override
    public void store(PracticeHazardEvent event) {
        dao.create(event);
        statusHandler.handle(Priority.INFO, "Hazard " + event.getEventID()
                + " successfully stored to database");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.edex.hazards.IHazardStorageManager#delete(com.raytheon
     * .uf.common.dataplugin.events.hazards.event.IHazardEvent)
     */
    @Override
    public void delete(PracticeHazardEvent event) {
        dao.delete(event);
        statusHandler.handle(Priority.INFO, "Hazard " + event.getEventID()
                + " successfully deleted from database");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.edex.hazards.IHazardStorageManager#update(com.raytheon
     * .uf.common.dataplugin.events.hazards.event.IHazardEvent)
     */
    @Override
    public void update(PracticeHazardEvent event) {
        dao.update(event);
        statusHandler.handle(Priority.INFO, "Hazard " + event.getEventID()
                + " successfully updated in database");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.edex.hazards.IHazardStorageManager#retrieve(java.util
     * .Map)
     */
    @SuppressWarnings("unchecked")
    @Override
    public Map<String, HazardHistoryList> retrieve(
            Map<String, List<Object>> filters) {
        Criteria criteria = dao.getSessionFactory().openSession()
                .createCriteria(PracticeHazardEvent.class);

        List<String> keys = new ArrayList<String>();
        // get the fields in the primary key, as they have a slightly different
        // key name
        Field[] fields = PracticeHazardEventPK.class.getDeclaredFields();
        for (Field field : fields) {
            keys.add(field.getName());
        }

        if (filters != null) {
            for (Entry<String, List<Object>> entry : filters.entrySet()) {
                String finalKey = entry.getKey();
                // filter for the geometry
                if (finalKey.equals(HazardConstants.GEOMETRY)) {
                    try {
                        // TODO, is there a better way to do this?
                        ISpatialQuery query = SpatialQueryFactory.create();
                        StringBuilder requestString = new StringBuilder(
                                "select eventid from awips.practice_hazards where ");
                        for (int i = 0; i < entry.getValue().size(); i++) {
                            if (i > 0) {
                                requestString.append(" or ");
                            }
                            Geometry geom = (Geometry) entry.getValue().get(i);
                            requestString
                                    .append("ST_Intersects(geometry, ST_GeomFromText('"
                                            + geom.toText() + "'))");
                        }
                        requestString.append(";");
                        Object[] results = query.dbRequest(
                                requestString.toString(), "metadata");
                        criteria.add(Restrictions.in("key."
                                + HazardConstants.EVENTID, results));
                    } catch (SpatialException e) {
                        statusHandler.handle(Priority.PROBLEM,
                                e.getLocalizedMessage(), e);
                    }

                } else if (finalKey.equals(HazardConstants.STARTTIME)) {
                    // we will not support any more than two times, it doesn't
                    // make any sense, we will only support the first 2 in the
                    // filter map
                    criteria.add(Restrictions.ge(HazardConstants.STARTTIME,
                            filters.get(HazardConstants.STARTTIME).get(0)));
                    if (filters.get(HazardConstants.STARTTIME).size() > 1) {
                        criteria.add(Restrictions.le(HazardConstants.STARTTIME,
                                filters.get(HazardConstants.STARTTIME).get(1)));
                    }
                } else if (finalKey.equals(HazardConstants.ENDTIME)) {
                    // same as above, only support 2 times
                    if (filters.get(HazardConstants.ENDTIME).size() > 1) {
                        criteria.add(Restrictions.ge(HazardConstants.ENDTIME,
                                filters.get(HazardConstants.ENDTIME).get(0)));
                        criteria.add(Restrictions.le(HazardConstants.ENDTIME,
                                filters.get(HazardConstants.ENDTIME).get(1)));
                    } else {
                        criteria.add(Restrictions.le(HazardConstants.ENDTIME,
                                filters.get(HazardConstants.ENDTIME).get(0)));
                    }
                } else {
                    // filter for any specified column in the table
                    if (keys.contains(finalKey)) {
                        finalKey = "key." + entry.getKey();
                    }
                    criteria.add(Restrictions.in(finalKey, entry.getValue()));
                }
            }
        }
        List<PracticeHazardEvent> events = criteria.list();
        Map<String, HazardHistoryList> mapEvents = new HashMap<String, HazardHistoryList>();

        // group them for use later
        for (PracticeHazardEvent event : events) {
            if (mapEvents.containsKey(event.getEventID())) {
                mapEvents.get(event.getEventID()).add(event);
            } else {
                HazardHistoryList list = new HazardHistoryList();
                list.add(event);
                mapEvents.put(event.getEventID(), list);
            }
        }
        return mapEvents;
    }
}

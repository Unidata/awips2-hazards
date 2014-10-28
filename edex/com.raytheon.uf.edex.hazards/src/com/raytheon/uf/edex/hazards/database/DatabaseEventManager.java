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

import org.hibernate.Session;
import org.hibernate.criterion.Conjunction;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Restrictions;

import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardNotification.NotificationType;
import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.HazardEventManager.Mode;
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
import com.raytheon.uf.edex.hazards.HazardNotifier;
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
 * Oct 30, 2013 #1472     bkowal    Updated the phensig retrieval to use disjunctions
 *                                  and conjunctions instead of nested OR and AND
 *                                  statements.
 * Nov 04, 2013 2182     daniel.s.schaffer@noaa.gov      Started refactoring
 * Jan 14, 2014 2755     bkowal     Updated to use DetachedCriteria when selecting
 *                                  events so that the returned events will not
 *                                  be associated with an open session.
 * Mar 24, 2014 #3323    bkowal     Include the mode in the hazard notification.
 * Oct 21, 2014   5051     mpduff      Change to support Hibernate upgrade.
 * 10/28/2014   5051     bphillip   Change to support Hibernate upgrade
 * 
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */

public class DatabaseEventManager implements
        IHazardStorageManager<PracticeHazardEvent> {
    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(DatabaseEventManager.class);

    private static final Mode MODE = Mode.PRACTICE;

    private final CoreDao dao;

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

        HazardNotifier.notify(event, NotificationType.STORE, MODE);
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

        // need to not send the notification if this doesn't delete anything.
        HazardNotifier.notify(event, NotificationType.DELETE, MODE);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.edex.hazards.IHazardStorageManager#deleteAll()
     */
    @Override
    public void deleteAll(List<PracticeHazardEvent> events) {
        dao.deleteAll(events);
        statusHandler.handle(Priority.INFO,
                "All hazards successfully deleted from database");
        // null event, as we will want to handle ALL events
        for (PracticeHazardEvent event : events) {
            HazardNotifier
                    .notify(event, NotificationType.DELETE, MODE);
        }
        HazardNotifier.notify(null, NotificationType.DELETE_ALL, MODE);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.edex.hazards.IHazardStorageManager#update(com.raytheon
     * .uf.common.dataplugin.events.hazards.event.IHazardEvent)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void update(PracticeHazardEvent event) {
        dao.update(event);
        statusHandler.handle(Priority.INFO, "Hazard " + event.getEventID()
                + " successfully updated in database");

        HazardNotifier.notify(event, NotificationType.UPDATE, MODE);
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
        DetachedCriteria criteria = DetachedCriteria
                .forClass(PracticeHazardEvent.class);

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
                        if (results.length > 0) {
                            criteria.add(Restrictions.in("key."
                                    + HazardConstants.HAZARD_EVENT_IDENTIFIER,
                                    results));
                        }
                    } catch (SpatialException e) {
                        statusHandler.handle(Priority.PROBLEM,
                                e.getLocalizedMessage(), e);
                    }

                } else if (finalKey
                        .equals(HazardConstants.HAZARD_EVENT_START_TIME)) {
                    // we will not support any more than two times, it doesn't
                    // make any sense, we will only support the first 2 in the
                    // filter map
                    criteria.add(Restrictions
                            .ge(HazardConstants.HAZARD_EVENT_START_TIME,
                                    filters.get(
                                            HazardConstants.HAZARD_EVENT_START_TIME)
                                            .get(0)));
                    if (filters.get(HazardConstants.HAZARD_EVENT_START_TIME)
                            .size() > 1) {
                        criteria.add(Restrictions
                                .le(HazardConstants.HAZARD_EVENT_START_TIME,
                                        filters.get(
                                                HazardConstants.HAZARD_EVENT_START_TIME)
                                                .get(1)));
                    }
                } else if (finalKey
                        .equals(HazardConstants.HAZARD_EVENT_END_TIME)) {
                    // same as above, only support 2 times
                    if (filters.get(HazardConstants.HAZARD_EVENT_END_TIME)
                            .size() > 1) {
                        criteria.add(Restrictions.ge(
                                HazardConstants.HAZARD_EVENT_END_TIME,
                                filters.get(
                                        HazardConstants.HAZARD_EVENT_END_TIME)
                                        .get(0)));
                        criteria.add(Restrictions.le(
                                HazardConstants.HAZARD_EVENT_END_TIME,
                                filters.get(
                                        HazardConstants.HAZARD_EVENT_END_TIME)
                                        .get(1)));
                    } else {
                        criteria.add(Restrictions.le(
                                HazardConstants.HAZARD_EVENT_END_TIME,
                                filters.get(
                                        HazardConstants.HAZARD_EVENT_END_TIME)
                                        .get(0)));
                    }
                } else if (finalKey.equals(HazardConstants.PHEN_SIG)) {
                    Disjunction criterion = Restrictions.disjunction();
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
                        // build a criterion based on and/or according to
                        // phensigs

                        Conjunction psCriterion = Restrictions.conjunction();
                        psCriterion.add(Restrictions.eq(
                                HazardConstants.PHENOMENON, splitPhensig[0]));
                        psCriterion.add(Restrictions.eq(
                                HazardConstants.SIGNIFICANCE, splitPhensig[1]));
                        if (splitPhensig.length == 3) {
                            psCriterion.add(Restrictions.eq(
                                    HazardConstants.HAZARD_EVENT_SUB_TYPE,
                                    splitPhensig[2]));
                        }

                        criterion.add(psCriterion);
                    }
                    criteria.add(criterion);
                } else {
                    // filter for any specified column in the table
                    if (keys.contains(finalKey)) {
                        finalKey = "key." + entry.getKey();
                    }
                    criteria.add(Restrictions.in(finalKey, entry.getValue()));
                }
            }
        }
        criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
        Session session = dao.getSession();
        try{
            List<PracticeHazardEvent> events = criteria.getExecutableCriteria(
                    session).list();
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
        } finally {
            if(session != null){
                session.close();
            }
        }
    }
}

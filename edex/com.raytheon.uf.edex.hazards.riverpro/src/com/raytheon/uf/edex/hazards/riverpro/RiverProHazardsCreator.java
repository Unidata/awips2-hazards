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
package com.raytheon.uf.edex.hazards.riverpro;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.raytheon.uf.common.dataaccess.util.DatabaseQueryUtil;
import com.raytheon.uf.common.dataaccess.util.DatabaseQueryUtil.QUERY_MODE;
import com.raytheon.uf.common.dataplugin.PluginDataObject;
import com.raytheon.uf.common.dataplugin.annotations.DataURIUtil;
import com.raytheon.uf.common.dataplugin.message.PracticeDataURINotificationMessage;
import com.raytheon.uf.common.dataplugin.shef.tables.Vtecaction;
import com.raytheon.uf.common.dataplugin.shef.tables.Vteccause;
import com.raytheon.uf.common.dataplugin.shef.tables.Vtecevent;
import com.raytheon.uf.common.dataplugin.shef.tables.VteceventId;
import com.raytheon.uf.common.dataplugin.shef.tables.Vtecphenom;
import com.raytheon.uf.common.dataplugin.shef.tables.Vtecpractice;
import com.raytheon.uf.common.dataplugin.shef.tables.VtecpracticeId;
import com.raytheon.uf.common.dataplugin.shef.tables.Vtecrecord;
import com.raytheon.uf.common.dataplugin.shef.tables.Vtecsever;
import com.raytheon.uf.common.dataplugin.shef.tables.Vtecsignif;
import com.raytheon.uf.common.dataplugin.text.db.AfosToAwips;
import com.raytheon.uf.common.dataplugin.warning.AbstractWarningRecord;
import com.raytheon.uf.common.dataplugin.warning.PracticeWarningRecord;
import com.raytheon.uf.common.dataquery.requests.DbQueryRequest;
import com.raytheon.uf.common.dataquery.requests.RequestConstraint;
import com.raytheon.uf.common.dataquery.responses.DbQueryResponse;
import com.raytheon.uf.common.serialization.SerializationUtil;
import com.raytheon.uf.common.serialization.comm.RequestRouter;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.edex.database.DataAccessLayerException;
import com.raytheon.uf.edex.database.dao.CoreDao;
import com.raytheon.uf.edex.database.dao.DaoConfig;
import com.raytheon.uf.edex.plugin.text.dao.AfosToAwipsDao;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

/**
 * Interoperates between Riverpro and Hazard Services
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 11, 2013            mnash       Initial creation
 * Mar  3, 2014 3034       bkowal      Prevent Null Pointer Exception for Geometry
 * Aug 11, 2014 2826       jsanchez    Improved interoperability hazard comparison.
 * 
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */

public class RiverProHazardsCreator {
    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(RiverProHazardsCreator.class);

    private static final String IHFS_DB = "ihfs";

    private static final String SEVERITY = "severity";

    private static final String SIGNIF = "signif";

    private static final String PHENOM = "phenom";

    private static final String VTEC_SIGNIF = "vtecsignif";

    private static final String VTEC_PHENOM = "vtecphenom";

    private static final String IMMED_CAUSE = "immedCause";

    private static final String SELECT_GAUGE_STRING = "select lid,lat,lon from location;";

    private static final String ACTION = "action";

    private static final String RECORD = "record";

    private static final CoreDao dao = new CoreDao(
            DaoConfig.forDatabase(IHFS_DB));

    private static final AfosToAwipsDao a2aDao = new AfosToAwipsDao();

    public void createHazardsFromBytes(byte[] bytes) {
        Map<String, RequestConstraint> vals = new HashMap<String, RequestConstraint>();
        try {
            PracticeDataURINotificationMessage value = SerializationUtil
                    .transformFromThrift(
                            PracticeDataURINotificationMessage.class, bytes);
            String[] uris = value.getDataURIs();
            if (uris.length > 0) {
                vals.putAll(RequestConstraint.toConstraintMapping(DataURIUtil
                        .createDataURIMap(uris[0])));
            } else {
                statusHandler
                        .warn("Empty Practice Data URI Notification Received!");
                return;
            }
            DbQueryRequest request = new DbQueryRequest(vals);
            DbQueryResponse response = (DbQueryResponse) RequestRouter
                    .route(request);
            PluginDataObject[] pdos = response
                    .getEntityObjects(PluginDataObject.class);
            if (pdos.length != 0) {
                createHazards(Arrays.asList(pdos));
            }
        } catch (Exception e) {
            statusHandler.error("Unable to create hazards for pdos", e);
        }
    }

    public void createHazards(List<PluginDataObject> objects) {
        if (objects.isEmpty() == false) {
            // find the gauge locations from the table
            Map<String, Point> gaugeLocations = null;
            try {
                gaugeLocations = calculateGaugeLocations();
            } catch (Throwable t) {
                statusHandler
                        .error("Unable to query for gauge data, events will not be interoperable with RiverPro");
                return;
            }
            // retrieve the available phenomenons for RiverPro
            Map<String, String> phens = retrieveAvailable(PHENOM, VTEC_PHENOM);
            // retrieve the available significances for RiverPro
            Map<String, String> sigs = retrieveAvailable(SIGNIF, VTEC_SIGNIF);
            // loop over each record
            for (PluginDataObject obj : objects) {
                AbstractWarningRecord warning = null;
                if (obj instanceof AbstractWarningRecord) {
                    warning = (AbstractWarningRecord) obj;
                } else {
                    continue;
                }

                if (warning.getGeometry() == null) {
                    continue;
                }

                // are are we doing a correct phen and sig, if not, continue on
                if (phens.containsKey(warning.getPhen())
                        && sigs.containsKey(warning.getSig())) {

                    // loop over all gauge locations
                    for (Entry<String, Point> pt : gaugeLocations.entrySet()) {
                        // only create a record, if the gauge is in the area
                        if (warning.getGeometry().contains(pt.getValue())) {
                            // query the afos_to_awips table to get the product
                            // id
                            String productId = "";
                            try {
                                String xxxId = warning.getOfficeid();
                                String wmottaaii = warning.getWmoid()
                                        .split(" ")[0];
                                List<AfosToAwips> list = a2aDao.lookupAfosId(
                                        wmottaaii, xxxId).getIdList();
                                for (AfosToAwips a2a : list) {
                                    if (a2a.getAfosid().contains(
                                            warning.getPil())) {
                                        productId = a2a.getAfosid();
                                        break;
                                    }
                                }
                            } catch (DataAccessLayerException e) {
                                statusHandler
                                        .handle(Priority.PROBLEM,
                                                "Unable to query afos_to_awips table for afosId",
                                                e);
                            }
                            // testing whether it should go to the practice
                            // table, or to the regular table.
                            if (obj instanceof PracticeWarningRecord) {
                                VtecpracticeId id = new VtecpracticeId(
                                        pt.getKey(), productId, warning
                                                .getIssueTime().getTime());
                                Vtecpractice event = new Vtecpractice(id);
                                event.setBegintime(warning.getStartTime()
                                        .getTime());
                                event.setEndtime(warning.getEndTime().getTime());
                                Calendar floodCrestTime = warning
                                        .getFloodCrest();
                                if (floodCrestTime != null) {
                                    event.setCresttime(floodCrestTime.getTime());
                                }
                                event.setRecord(warning.getFloodRecordStatus());
                                event.setEtn(Short.parseShort(warning.getEtn()));
                                event.setOfficeId(warning.getOfficeid());
                                event.setPhenom(warning.getPhen());
                                event.setSeverity(warning.getFloodSeverity());
                                event.setSignif(warning.getSig());
                                event.setAction(warning.getAct());
                                event.setImmedCause(warning.getImmediateCause());
                                event.setProductmode(warning.getProductClass());
                                event.setRecord(warning.getFloodRecordStatus());
                                dao.create(event);
                            } else {
                                VteceventId id = new VteceventId(pt.getKey(),
                                        productId, warning.getIssueTime()
                                                .getTime());
                                Vtecevent event = new Vtecevent(id);
                                event.setBegintime(warning.getStartTime()
                                        .getTime());
                                event.setEndtime(warning.getEndTime().getTime());
                                Calendar floodCrestTime = warning
                                        .getFloodCrest();
                                if (floodCrestTime != null) {
                                    event.setCresttime(floodCrestTime.getTime());
                                }
                                event.setEtn(Short.parseShort(warning.getEtn()));
                                event.setOfficeId(warning.getOfficeid());
                                event.setProductmode(warning.getProductClass());

                                event.setVteccause(retrieveVtecObject(
                                        warning.getImmediateCause(),
                                        IMMED_CAUSE, Vteccause.class));
                                event.setVtecaction(retrieveVtecObject(
                                        warning.getAct(), ACTION,
                                        Vtecaction.class));
                                event.setVtecphenom(retrieveVtecObject(
                                        warning.getPhen(), PHENOM,
                                        Vtecphenom.class));
                                event.setVtecsignif(retrieveVtecObject(
                                        warning.getSig(), SIGNIF,
                                        Vtecsignif.class));
                                event.setVtecsever(retrieveVtecObject(
                                        warning.getFloodSeverity(), SEVERITY,
                                        Vtecsever.class));
                                event.setVtecrecord(retrieveVtecObject(
                                        warning.getFloodRecordStatus(), RECORD,
                                        Vtecrecord.class));
                                dao.create(event);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Queries the location table to get all the gauge locations so we can write
     * a new row for each location
     * 
     * @return
     */
    private Map<String, Point> calculateGaugeLocations() {
        Map<String, Point> gaugeLocations = new HashMap<String, Point>();
        List<Object[]> objects = DatabaseQueryUtil.executeDatabaseQuery(
                QUERY_MODE.MODE_SQLQUERY, SELECT_GAUGE_STRING, IHFS_DB,
                "location");
        GeometryFactory factory = new GeometryFactory();
        for (Object[] obj : objects) {
            if (obj[0] == null || obj[1] == null || obj[2] == null) {
                continue;
            }
            String id = (String) obj[0];
            double lat = (Double) obj[1];
            double lon = (Double) obj[2] * -1.0;
            // create a point for each gauge coordinate
            Point point = factory.createPoint(new Coordinate(lon, lat));
            gaugeLocations.put(id, point);
        }
        return gaugeLocations;
    }

    /**
     * Helps to retrieve values from the database. Could probably be smarter,
     * this only allows for tables with two columns (which seem to show up a lot
     * in IHFS)
     * 
     * @param col
     * @param table
     * @return
     */
    private Map<String, String> retrieveAvailable(String col, String table) {
        Map<String, String> vals = new HashMap<String, String>();
        List<Object[]> objects = DatabaseQueryUtil.executeDatabaseQuery(
                QUERY_MODE.MODE_SQLQUERY, "select " + col + ",name from "
                        + table + ";", IHFS_DB, table);
        for (Object[] obj : objects) {
            vals.put((String) obj[0], (String) obj[1]);
        }
        return vals;
    }

    /**
     * Tries to generically retrieve an object of type T from the database where
     * the first column value is passed in. This is mainly for the Vtec* table
     * retrieval since we have to have those objects to store to the Vtecevent
     * table.
     * 
     * @param firstColumnValue
     * @param firstColumnName
     * @param clazz
     * @return
     */
    @SuppressWarnings("unchecked")
    private <T extends Object> T retrieveVtecObject(String firstColumnValue,
            String firstColumnName, Class<T> clazz) {
        List<Object[]> objects = DatabaseQueryUtil.executeDatabaseQuery(
                QUERY_MODE.MODE_HQLQUERY, "from " + clazz.getSimpleName()
                        + " where " + firstColumnName + "='" + firstColumnValue
                        + "'", IHFS_DB, clazz.getSimpleName());
        for (Object[] obs : objects) {
            if (obs.length != 0) {
                T cause = (T) obs[0];
                return cause;
            }
        }
        return null;
    }
}
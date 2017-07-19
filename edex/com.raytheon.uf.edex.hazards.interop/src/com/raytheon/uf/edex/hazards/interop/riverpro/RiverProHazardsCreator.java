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
package com.raytheon.uf.edex.hazards.interop.riverpro;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.raytheon.uf.common.dataaccess.util.DatabaseQueryUtil;
import com.raytheon.uf.common.dataaccess.util.DatabaseQueryUtil.QUERY_MODE;
import com.raytheon.uf.common.dataplugin.PluginDataObject;
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
import com.raytheon.uf.common.dataplugin.warning.AbstractWarningRecord;
import com.raytheon.uf.common.dataplugin.warning.PracticeWarningRecord;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.edex.database.DataAccessLayerException;
import com.raytheon.uf.edex.database.dao.CoreDao;
import com.raytheon.uf.edex.database.dao.DaoConfig;
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
 * Dec 04, 2014 2826       dgilling    Remove unneeded methods.
 * Dec 08, 2014 2826       dgilling    Remove unnecessary phen/sig validation from
 *                                     RiverPro database.
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

    private static final String IMMED_CAUSE = "immedCause";

    private static final String SELECT_GAUGE_STRING = "select lid,lat,lon from location;";

    private static final String ACTION = "action";

    private static final String RECORD = "record";

    private static final CoreDao dao = new CoreDao(
            DaoConfig.forDatabase(IHFS_DB));

    /*
     * TODO: As of 17.1.1, the class this variable uses as its type is gone.
     * Since the enclosing class needs overhaul anyway as part of
     * interoperability, the variable declaration is being commented out for
     * now.
     */
    // private static final AfosToAwipsDao a2aDao = new AfosToAwipsDao();

    public void createHazards(List<PluginDataObject> objects) {
        if (!objects.isEmpty()) {
            // find the gauge locations from the table
            Map<String, Point> gaugeLocations = null;
            try {
                gaugeLocations = calculateGaugeLocations();
            } catch (Throwable t) {
                statusHandler.error(
                        "Unable to query for gauge data, events will not be interoperable with RiverPro");
                return;
            }

            for (PluginDataObject obj : objects) {
                AbstractWarningRecord warning = null;
                if (obj instanceof AbstractWarningRecord) {
                    warning = (AbstractWarningRecord) obj;
                } else {
                    statusHandler
                            .warn("Invalid PDO sent to Hazard Services interoperability processing. Skipping record of type "
                                    + obj.getClass().getCanonicalName() + ".");
                    continue;
                }

                if (warning.getGeometry() == null) {
                    statusHandler.warn("Skipping product " + warning.getPil()
                            + " because it has invalid geometry.");
                    continue;
                }

                // loop over all gauge locations
                for (Entry<String, Point> pt : gaugeLocations.entrySet()) {
                    // only create a record, if the gauge is in the area
                    if (warning.getGeometry().contains(pt.getValue())) {
                        // query the afos_to_awips table to get the product
                        // id
                        String productId = "";
                        try {
                            String xxxId = warning.getOfficeid();
                            String wmottaaii = warning.getWmoid().split(" ")[0];

                            /*
                             * TODO: a2aDao has been commented out as of 17.1.1
                             * transition (see TODO above); thus, an exception
                             * is thrown here, since the commented-out code
                             * below cannot be compiled or run.
                             */
                            throw new DataAccessLayerException(
                                    "not implemented");
                            // List<AfosToAwips> list = a2aDao
                            // .lookupAfosId(wmottaaii, xxxId).getIdList();
                            // for (AfosToAwips a2a : list) {
                            // if (a2a.getAfosid()
                            // .contains(warning.getPil())) {
                            // productId = a2a.getAfosid();
                            // break;
                            // }
                            // }
                        } catch (DataAccessLayerException e) {
                            statusHandler.handle(Priority.PROBLEM,
                                    "Unable to query afos_to_awips table for afosId",
                                    e);
                        }
                        // testing whether it should go to the practice
                        // table, or to the regular table.
                        if (obj instanceof PracticeWarningRecord) {
                            VtecpracticeId id = new VtecpracticeId(pt.getKey(),
                                    productId,
                                    warning.getIssueTime().getTime());
                            Vtecpractice event = new Vtecpractice(id);
                            event.setBegintime(
                                    warning.getStartTime().getTime());
                            event.setEndtime(warning.getEndTime().getTime());
                            Calendar floodCrestTime = warning.getFloodCrest();
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
                                    productId,
                                    warning.getIssueTime().getTime());
                            Vtecevent event = new Vtecevent(id);
                            event.setBegintime(
                                    warning.getStartTime().getTime());
                            event.setEndtime(warning.getEndTime().getTime());
                            Calendar floodCrestTime = warning.getFloodCrest();
                            if (floodCrestTime != null) {
                                event.setCresttime(floodCrestTime.getTime());
                            }
                            event.setEtn(Short.parseShort(warning.getEtn()));
                            event.setOfficeId(warning.getOfficeid());
                            event.setProductmode(warning.getProductClass());

                            event.setVteccause(retrieveVtecObject(
                                    warning.getImmediateCause(), IMMED_CAUSE,
                                    Vteccause.class));
                            event.setVtecaction(
                                    retrieveVtecObject(warning.getAct(), ACTION,
                                            Vtecaction.class));
                            event.setVtecphenom(
                                    retrieveVtecObject(warning.getPhen(),
                                            PHENOM, Vtecphenom.class));
                            event.setVtecsignif(
                                    retrieveVtecObject(warning.getSig(), SIGNIF,
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
                QUERY_MODE.MODE_HQLQUERY,
                "from " + clazz.getSimpleName() + " where " + firstColumnName
                        + "='" + firstColumnValue + "'",
                IHFS_DB, clazz.getSimpleName());
        for (Object[] obs : objects) {
            if (obs.length != 0) {
                T cause = (T) obs[0];
                return cause;
            }
        }
        return null;
    }
}
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

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.raytheon.edex.plugin.text.dao.AfosToAwipsDao;
import com.raytheon.uf.common.actionregistry.IActionable;
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
import com.raytheon.uf.common.dataplugin.text.db.AfosToAwips;
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
 * Helps to take {@link AbstractWarningRecord} from the warning ingest route and
 * turns them into the corresponding IHFS table records so that RiverPro can
 * read them later. Should NEVER be used outside of that single task.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 11, 2013            mnash     Initial creation
 * 
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */

class RiverProActionable implements IActionable {

    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(RiverProActionable.class);

    private static final String IHFS_DB = "ihfs";

    private static final String FXA_DB = "fxa";

    private static final CoreDao dao = new CoreDao(
            DaoConfig.forDatabase(IHFS_DB));

    private static final AfosToAwipsDao a2aDao = new AfosToAwipsDao();

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.common.actionregistry.IActionable#handleAction(com.raytheon
     * .uf.common.actionregistry.PluginDataObject[])
     */
    @Override
    public void handleAction(PluginDataObject[] arguments) {
        if (arguments.length > 0) {
            long time = System.currentTimeMillis();
            boolean practice = arguments[0] instanceof PracticeWarningRecord ? true
                    : false;
            // find the gage locations from the table
            Map<String, Point> gageLocations = calculateGageLocations();
            // retrieve the available phenomenons for RiverPro
            Map<String, String> phens = retrieveAvailable("phenom",
                    "vtecphenom");
            // retrieve the available significances for RiverPro
            Map<String, String> sigs = retrieveAvailable("signif", "vtecsignif");
            // loop over each record
            for (PluginDataObject ob : arguments) {
                AbstractWarningRecord warning = (AbstractWarningRecord) ob;
                // are are we doing a correct phen and sig, if not, continue on
                if (phens.containsKey(warning.getPhen())
                        && sigs.containsKey(warning.getSig()) || true) {
                    // loop over all gage locations
                    for (Entry<String, Point> pt : gageLocations.entrySet()) {
                        // only create a record, if the gage
                        if (warning.getGeometry().contains(pt.getValue())) {

                            // query the afos_to_awips table to get the product
                            // id
                            String productId = "";
                            try {
                                String xxxId = warning.getOfficeid();
                                String wmottaaii = warning.getWmoid()
                                        .split(" ")[0];
                                String ccc = retrieveCCC(xxxId);
                                List<AfosToAwips> list = a2aDao.lookupAfosId(
                                        wmottaaii, ccc).getIdList();
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
                            // for (AfosToAwips a2a : list) {
                            // a2a.getAfosid();
                            // }
                            // testing whether it should go to the practice
                            // table, or to the regular table.
                            if (practice) {
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
                                        "immedCause", Vteccause.class));
                                event.setVtecaction(retrieveVtecObject(
                                        warning.getAct(), "action",
                                        Vtecaction.class));
                                event.setVtecphenom(retrieveVtecObject(
                                        warning.getPhen(), "phenom",
                                        Vtecphenom.class));
                                event.setVtecsignif(retrieveVtecObject(
                                        warning.getSig(), "signif",
                                        Vtecsignif.class));
                                event.setVtecsever(retrieveVtecObject(
                                        warning.getFloodSeverity(), "severity",
                                        Vtecsever.class));
                                event.setVtecrecord(retrieveVtecObject(
                                        warning.getFloodRecordStatus(),
                                        "record", Vtecrecord.class));
                                dao.create(event);
                            }
                        }
                    }
                }
            }
            System.out.println("Time : " + (System.currentTimeMillis() - time));
        }
    }

    /**
     * Queries the location table to get all the gage locations so we can write
     * a new row for each location
     * 
     * @return
     */
    private Map<String, Point> calculateGageLocations() {
        Map<String, Point> gageLocations = new HashMap<String, Point>();
        List<Object[]> objects = DatabaseQueryUtil.executeDatabaseQuery(
                QUERY_MODE.MODE_SQLQUERY, "select lid,lat,lon from location;",
                IHFS_DB, "location");
        GeometryFactory factory = new GeometryFactory();
        for (Object[] obj : objects) {
            if (obj[0] == null || obj[1] == null || obj[2] == null) {
                continue;
            }
            String id = (String) obj[0];
            double lat = (Double) obj[1];
            double lon = (Double) obj[2] * -1.0;
            // create a point for each gage coordinate
            Point point = factory.createPoint(new Coordinate(lon, lat));
            gageLocations.put(id, point);
        }
        return gageLocations;
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

    /**
     * Retrieves the CCC for use when finding the Afos Id
     * 
     * @param id
     * @return
     */
    private String retrieveCCC(String id) {
        String query = "select ccc from afoslookup where origin='" + id + "';";
        List<Object[]> objects = DatabaseQueryUtil.executeDatabaseQuery(
                QUERY_MODE.MODE_SQLQUERY, query, FXA_DB, "ccc");
        String ccc = "";
        for (Object[] obs : objects) {
            if (obs[0] != null) {
                ccc = (String) obs[0];
            }
        }
        return ccc;
    }
}

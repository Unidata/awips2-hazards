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
package com.raytheon.uf.edex.hazards.handlers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.raytheon.uf.common.hazards.productgen.request.SpatialQueryRequest;
import com.raytheon.uf.common.serialization.comm.IRequestHandler;
import com.raytheon.uf.common.util.CollectionUtil;
import com.raytheon.uf.edex.database.dao.CoreDao;
import com.raytheon.uf.edex.database.dao.DaoConfig;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;

/**
 * 
 * Handler class for requests for areas affected by a Hazard Event
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Mar 02, 2016            bphillip     Initial creation
 * Aug 10, 2016 21056      Robert.Blum  Updates for pathcast.
 * 
 * </pre>
 * 
 * @author bphillip
 * @version 1.0
 */
public class SpatialQueryHandler implements
        IRequestHandler<SpatialQueryRequest> {

    @Override
    public List<Map<String, Object>> handleRequest(SpatialQueryRequest request)
            throws Exception {
        /*
         * Create a data access object for interacting with the maps database
         */
        CoreDao dao = new CoreDao(DaoConfig.forDatabase("maps"));

        StringBuilder query = new StringBuilder();
        query.append("select ");

        List<String> fieldNames = request.getReturnFields();
        boolean first = true;
        for (String field : fieldNames) {
            if (!first) {
                query.append(", ");
            }
            if (field.equals("the_geom")) {
                query.append("AsBinary(").append(field).append(") as ");
            }
            query.append(field);
            first = false;
        }

        query.append(" from mapdata.").append(request.getTableName());
        query.append(" where ");

        /*
         * According to the PostGIS documentation, functions such as
         * ST_Intersects and others should not be called using GeomtryCollection
         * objects. Therefore, the geometries must be split out and an
         * individual clause must be generated for each geometry contained in
         * the collection
         */
        Geometry[] geoms = getGeometries(request.getGeometry());
        query.append("(");
        /*
         * Iterate over the geometries from the geometry collection and
         * generated clauses for each.
         */
        for (int i = 0; i < geoms.length; i++) {
            query.append("ST_Intersects(the_geom,")
                    .append("(ST_GeomFromText('").append(geoms[i].toString())
                    .append("',4326)))");
            if (i != geoms.length - 1) {
                query.append(" or ");
            }
        }
        query.append(")");

        /*
         * Check if the request contains constraints
         */
        if (request.getConstraints() != null
                && !request.getConstraints().keySet().isEmpty()) {
            query.append(" and ");

            /*
             * If the request contains constraints, process each constraint and
             * generate a where clause for each
             */
            int idx = 0;
            for (String key : request.getConstraints().keySet()) {
                Object value = request.getConstraints().get(key);
                query.append(key);
                if (value instanceof Collection<?>) {

                    Collection<?> valueCollection = (Collection<?>) value;
                    if (valueCollection.size() == 1) {
                        query.append("=").append(wrap(value));
                    } else {
                        query.append(" in (");
                        int collectionIdx = 0;
                        for (Object val : valueCollection) {
                            query.append(wrap(val));
                            if (++collectionIdx != valueCollection.size()) {
                                query.append(",");
                            }
                        }
                        query.append(") ");
                    }
                } else {
                    query.append("=").append(wrap(value));
                }
                if (++idx != request.getConstraints().size()) {
                    query.append(" and ");
                }
            }
        }
        /*
         * Process any sorting fields specified by the request. The 'distance'
         * sort field gets special handling since PostGIS functions are involved
         * for calculating the distance. Distance is calculated by computing the
         * distance from the centroid of the hazard geometry with the centroid
         * of each location in the specified maps table
         */
        if (request.getSortBy().size() % 2 == 0) {
            if (!CollectionUtil.isNullOrEmpty(request.getSortBy())) {
                query.append(" order by ");
                for (int i = 0; i < request.getSortBy().size(); i += 2) {
                    String sortColumn = request.getSortBy().get(i);
                    if (sortColumn.equals("distance")) {
                        query.append(
                                "ST_Distance(ST_Centroid(ST_GeomFromText('")
                                .append(request.getGeometry().toString())
                                .append("',4326)),ST_Centroid(the_geom)) ")
                                .append(request.getSortBy().get(i + 1));
                    } else {
                        query.append(sortColumn).append(" ")
                                .append(request.getSortBy().get(i + 1));
                    }
                    if (i < request.getSortBy().size() - 2) {
                        query.append(",");
                    }
                }
            }
        } else {
            throw new IllegalArgumentException(
                    "Invalid list size for sortBy list in spatial query. List must be an even size");
        }

        /*
         * Limit the maximum results returned if specified in the request
         */
        if (request.getMaxResults() != 0) {
            query.append(" limit ").append(request.getMaxResults());
        }

        /*
         * Execute the query and add the values to the return value
         */
        Object[] results = dao.executeSQLQuery(query.toString());
        List<Map<String, Object>> resultMaps = new ArrayList<Map<String, Object>>(
                results.length);
        for (Object obj : results) {
            if (obj instanceof Object[] == false) {
                obj = new Object[] { obj };
            }
            Object[] objs = (Object[]) obj;
            if (objs.length != fieldNames.size()) {
                throw new Exception(
                        "Column count returned does not match expected column count");
            }
            Map<String, Object> resultMap = new HashMap<>(objs.length * 2);
            for (int i = 0; i < fieldNames.size(); ++i) {
                resultMap.put(fieldNames.get(i), objs[i]);
            }
            resultMaps.add(resultMap);
        }
        return resultMaps;
    }

    /**
     * Wraps string values in single quotes if necessary
     * 
     * @param value
     *            The object to wrap if necessary
     * @return The quote wrapped value if the provided object was a string
     */
    private Object wrap(Object value) {
        if (value instanceof String) {
            return "'" + value.toString() + "'";
        } else {
            return value.toString();
        }
    }

    /**
     * Function used to separate individual geometries from a geometry
     * collection
     * 
     * @param geom
     *            The geometry collection to separate
     * @return An array of geometries contained in the geometry collection
     */
    private Geometry[] getGeometries(Geometry geom) {
        if (geom instanceof GeometryCollection) {
            GeometryCollection geomColl = (GeometryCollection) geom;
            Geometry[] geoms = new Geometry[geomColl.getNumGeometries()];
            for (int i = 0; i < geomColl.getNumGeometries(); i++) {
                geoms[i] = geomColl.getGeometryN(i);
            }
            return geoms;
        } else {
            return new Geometry[] { geom };
        }
    }
}

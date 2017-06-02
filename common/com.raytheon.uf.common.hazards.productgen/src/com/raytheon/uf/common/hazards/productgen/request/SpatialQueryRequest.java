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
package com.raytheon.uf.common.hazards.productgen.request;

import java.util.List;
import java.util.Map;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.serialization.comm.IServerRequest;
import com.vividsolutions.jts.geom.Geometry;

/**
 * 
 * Request class for retrieving the list of cities affected by a Hazard Event
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Mar 02, 2016            bphillip     Initial creation
 * Aug 10, 2016 21056      Robert.Blum  Added list of returnFields.
 * 
 * </pre>
 * 
 * @author bphillip
 * @version 1.0
 */
@DynamicSerialize
public class SpatialQueryRequest implements IServerRequest {

    @DynamicSerializeElement
    private Geometry geometry;

    @DynamicSerializeElement
    private String tableName;

    @DynamicSerializeElement
    private List<String> returnFields;

    @DynamicSerializeElement
    private List<String> sortBy;

    @DynamicSerializeElement
    private Map<String, Object> constraints;

    @DynamicSerializeElement
    private int maxResults = 0;

    public SpatialQueryRequest() {

    }

    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public List<String> getSortBy() {
        return sortBy;
    }

    public void setSortBy(List<String> sortBy) {
        this.sortBy = sortBy;
    }

    public int getMaxResults() {
        return maxResults;
    }

    public void setMaxResults(int maxResults) {
        this.maxResults = maxResults;
    }

    public Map<String, Object> getConstraints() {
        return constraints;
    }

    public void setConstraints(Map<String, Object> constraints) {
        this.constraints = constraints;
    }

    /**
     * @return the returnFields
     */
    public List<String> getReturnFields() {
        return returnFields;
    }

    /**
     * @param returnFields
     *            the returnFields to set
     */
    public void setReturnFields(List<String> returnFields) {
        this.returnFields = returnFields;
    }
}

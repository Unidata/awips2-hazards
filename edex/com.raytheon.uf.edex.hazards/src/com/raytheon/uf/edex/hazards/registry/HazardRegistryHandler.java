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
import java.util.List;
import java.util.Map;

import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardEvent;
import com.raytheon.uf.common.registry.RegistryQueryResponse;
import com.raytheon.uf.common.registry.handler.BaseRegistryObjectHandler;
import com.raytheon.uf.common.registry.handler.RegistryHandlerException;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.vividsolutions.jts.geom.Geometry;

/**
 * Handler for interfacing with the ebXML registry.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct  4, 2012            mnash       Initial creation
 * Jan 08, 2015  4839      ccody       Check registryHandler for nulls and log warning
 * 
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */

public class HazardRegistryHandler extends
        BaseRegistryObjectHandler<HazardEvent, HazardQuery> {

    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(HazardRegistryHandler.class);

    public HazardRegistryHandler() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.common.registry.handler.BaseRegistryObjectHandler#
     * getRegistryObjectClass()
     */
    @Override
    protected Class<HazardEvent> getRegistryObjectClass() {
        return HazardEvent.class;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.common.registry.handler.BaseRegistryObjectHandler#getQuery
     * ()
     */
    @Override
    protected HazardQuery getQuery() {
        return new HazardQuery();
    }

    public List<HazardEvent> getByFilter(Map<String, List<Object>> filters)
            throws RegistryHandlerException {

        List<HazardEvent> filterListResults = null;
        if (registryHandler != null) {
            HazardQuery query = getQuery();

            query.setFilters(filters);
            RegistryQueryResponse<HazardEvent> response = registryHandler
                    .getObjects(query);
            checkResponse(response, "getByFilter");
            filterListResults = response.getResults();
        } else {
            statusHandler
                    .warn("Registry Handler has not been configured. The getByFilter method will return empty results.");
            filterListResults = new ArrayList<HazardEvent>();
        }
        return (filterListResults);
    }

    /**
     * Pass in two dates, and retrieve all hazards that land between those two
     * dates
     * 
     * @param start
     * @param end
     * @return
     * @throws RegistryHandlerException
     */
    public List<HazardEvent> getByTimeRange(Date start, Date end)
            throws RegistryHandlerException {
        // TODO, this should be implemented as part of the registry code
        List<HazardEvent> events = new ArrayList<HazardEvent>();
        for (HazardEvent event : getAll()) {
            if (event.getStartTime() != null && event.getEndTime() != null) {
                if ((event.getStartTime().after(start) || event.getStartTime()
                        .equals(start))
                        && (event.getEndTime().before(end) || event
                                .getEndTime().equals(end))) {
                    events.add(event);
                }
            }
        }
        return events;
    }

    /**
     * Any geometry in the registry that intersects the given geometry will
     * match here.
     * 
     * @param geometry
     * @throws RegistryHandlerException
     */
    public List<HazardEvent> getByGeometry(Geometry geometry)
            throws RegistryHandlerException {
        List<HazardEvent> filterListResults = null;
        if (registryHandler != null) {
            HazardQuery query = getQuery();
            query.setGeometry(geometry);
            RegistryQueryResponse<HazardEvent> response = registryHandler
                    .getObjects(query);
            checkResponse(response, "getByGeometry");
            return response.getResults();
        } else {
            statusHandler
                    .warn("Registry Handler has not been configured. The getByGeometry method will return empty results.");
            filterListResults = new ArrayList<HazardEvent>();
        }
        return (filterListResults);
    }
}

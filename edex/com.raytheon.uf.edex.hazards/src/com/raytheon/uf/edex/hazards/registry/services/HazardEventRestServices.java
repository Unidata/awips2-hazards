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
package com.raytheon.uf.edex.hazards.registry.services;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.registry.HazardEventServiceException;
import com.raytheon.uf.common.dataplugin.events.hazards.registry.HazardEventServicesUtil;
import com.raytheon.uf.common.dataplugin.events.hazards.registry.services.IHazardEventRestServices;
import com.raytheon.uf.edex.registry.ebxml.dao.RegistryObjectDao;

import oasis.names.tc.ebxml.regrep.xsd.rim.v4.RegistryObjectType;

/**
 * 
 * Service implementation for the Hazard Services REST services
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 29, 2015 6895      Ben.Phillippe Refactored Hazard Service data access
 * Aug 4, 2015  6895     Ben.Phillippe Finished HS data access refactor
 * 5/3/2016     18193    Ben.Phillippe Replication of Hazard VTEC Records
 * 
 * </pre>
 * 
 * @author bphillip
 * @version 1.0
 */
@Path("/")
public class HazardEventRestServices implements IHazardEventRestServices {

    /** Registry object data access object */
    private RegistryObjectDao dao;

    /**
     * Creates a new HazardEventRestServices
     */
    public HazardEventRestServices() {

    }

    @Override
    @GET
    @Produces("text/xml")
    @Path("executeQuery")
    public String executeQuery(@Context UriInfo info)
            throws HazardEventServiceException {
        return HazardEventServicesUtil.getHazardEventResponse(query(info));
    }

    @Override
    @GET
    @Produces("text/xml")
    @Path("getRegistryObjects")
    public String getRegistryObjects(@Context UriInfo info)
            throws HazardEventServiceException {
        return HazardEventServicesUtil.getRegistryObjectResponse(query(info));
    }

    private List<RegistryObjectType> query(UriInfo info)
            throws HazardEventServiceException {
        return dao.executeHQLQuery(HazardEventServicesUtil.createAttributeQuery(
                HazardEvent.class, info.getQueryParameters(), null));
    }

    /**
     * @param dao
     *            the dao to set
     */
    public void setDao(RegistryObjectDao dao) {
        this.dao = dao;
    }

}

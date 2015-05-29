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
package com.raytheon.uf.common.dataplugin.events.hazards.registry.services;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import com.raytheon.uf.common.dataplugin.events.hazards.registry.HazardEventServiceException;

/**
 * 
 * Endpoint interface for hazard REST services
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 29, 2015 6895      Ben.Phillippe Refactored Hazard Service data access
 * 
 * </pre>
 * 
 * @author bphillip
 * @version 1.0
 */
@Path("/")
public interface IHazardEventRestServices {

    /**
     * Executes a registry query and returns marshalled HazardEvents
     * 
     * @param info
     *            The uri info containing the query parameters
     * @return The marshalled query response
     * @throws HazardEventServiceException
     *             If errors occur
     */
    @GET
    @Produces("text/xml")
    public String executeQuery(@Context
    UriInfo info) throws HazardEventServiceException;

    /**
     * Executes a registry query and returns marshalled RegistryObjectTypes
     * 
     * @param info
     *            The uri info containing the query parameters
     * @return The marshalled query response
     * @throws HazardEventServiceException
     *             If errors occur
     */
    @GET
    @Produces("text/xml")
    public String getRegistryObjects(@Context
    UriInfo info) throws HazardEventServiceException;
}

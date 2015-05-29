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

import java.util.Date;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import org.apache.cxf.annotations.FastInfoset;

import com.raytheon.uf.common.dataplugin.events.hazards.registry.HazardConflictDict;

/**
 * 
 * Hazard Service endpoint interface for hazard interoperability web services
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
@FastInfoset
@WebService(targetNamespace = IHazardEventServices.NAMESPACE, name = "HazardEventInteropServices")
@SOAPBinding
public interface IHazardEventInteropServices {

    /** The path to this set of services */
    public static final String PATH = "/hazardEventInteropServices";

    /** The namespace for these services */
    public static final String NAMESPACE = "http://services.registry.hazards.interop.edex.uf.raytheon.com/";

    /** The service name */
    public static final String SERVICE_NAME = "HazardEventInteropSvc";

    /**
     * Checks if there are conflicts with existing GFE grids
     * 
     * @param phenSig
     *            The phen/sig
     * @param siteID
     *            The site ID
     * @param startTime
     *            The start time
     * @param endTime
     *            The end time
     * @return True if conflicts exist, else false
     */
    @WebMethod(operationName = "hasConflicts")
    public Boolean hasConflicts(@WebParam(name = "phenSig")
    String phenSig, @WebParam(name = "siteID")
    String siteID, @WebParam(name = "startTime")
    Date startTime, @WebParam(name = "endTime")
    Date endTime);

    /**
     * Retrieves the hazard conflict dictionary
     * 
     * @return The hazard conflict dictionary
     */
    @WebMethod(operationName = "retrieveHazardsConflictDict")
    public HazardConflictDict retrieveHazardsConflictDict();

    /**
     * Method used to test connectivity to this set of services
     * 
     * @return A ping response
     */
    @WebMethod(operationName = "ping")
    public String ping();
}

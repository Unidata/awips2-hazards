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
package com.raytheon.uf.common.dataplugin.events.hazards.registry;

import javax.xml.ws.WebServiceException;

import org.apache.commons.lang.exception.ExceptionUtils;

/**
 * 
 * Exception class thrown from web service calls
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 29, 2015 6895      Ben.Phillippe Refactored Hazard Service data access
 * Jun 23, 2016 20030     Ben.Phillippe Changed parent class to WebServiceException so exceptions will
 *                                      be properly reported
 * 
 * </pre>
 * 
 * @author bphillip
 * @version 1.0
 */
public class HazardEventServiceException extends WebServiceException {

    private static final long serialVersionUID = 1227041184993254411L;

    /**
     * Constructor.
     */
    public HazardEventServiceException() {
        super();
    }

    /**
     * Constructor. The cause stacktrace is converted to a string because CXF
     * does not propogate stack traces
     * 
     * @param message
     * @param cause
     */
    public HazardEventServiceException(String message, Throwable cause) {
        super(message + "\n" + ExceptionUtils.getStackTrace(cause), cause);

    }

    /**
     * Constructor.
     * 
     * @param message
     */
    public HazardEventServiceException(String message) {
        super(message);
    }

    /**
     * Constructor.The cause stacktrace is converted to a string because CXF
     * does not propogate stack traces
     * 
     * @param cause
     */
    public HazardEventServiceException(Throwable cause) {
        super(ExceptionUtils.getStackTrace(cause), cause);
    }
}

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

package com.raytheon.uf.common.dataplugin.events.hazards.request;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;

/**
 * 
 * Request object for retrieving the necessary information to connect to the
 * registry web services
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 20, 2015 6895     Ben.Phillippe Routing registry requests through request server
 * 
 * </pre>
 * 
 * @author bphillip
 * @version 1.0
 */
@DynamicSerialize
public class GetRegistryInfoRequest extends HazardRequest {

    /** Registry URL key */
    public static final String REGISTRY_URL_KEY = "RegistryURL";

    /** Registry user key */
    public static final String REGISTRY_USER_KEY = "RegistryUser";

    /** Registry user password key */
    public static final String REGISTRY_USER_PASSWORD_KEY = "RegistryUserPassword";

    /**
     * Creates a new GetRegistryInfoRequest
     */
    public GetRegistryInfoRequest() {
        super();
    }

    /**
     * Creates a new GetRegistryInfoRequest
     * 
     * @param practice
     *            The practice mode flag
     */
    public GetRegistryInfoRequest(boolean practice) {
        super(practice);
    }

}

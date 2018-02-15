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
package com.raytheon.uf.edex.hazards.configuration;

import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.hazards.configuration.IServerConfigLookupWrapper;

/**
 * Edex-side implementation of a {@link IServerConfigLookupWrapper} specific for
 * Ingest. Utilizes environment variables to retrieve information about the
 * localization server and site.
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 31, 2017 30536      kbisanz     Initial creation
 *
 * </pre>
 *
 * @author kbisanz
 */
public class IngestServerConfigLookupWrapper
        extends EdexServerConfigLookupWrapper {

    @Override
    public String getServiceId() {
        return HazardConstants.RequestServiceId.INGEST.getValue();
    }

}

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
package com.raytheon.uf.common.dataplugin.hazards.interoperability;

import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;


/**
 * Constants used throughout hazard services interoperability.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 01, 2014            bkowal       Initial creation
 * Dec 18, 2014  #2826     dgilling     Change fields used in interoperability.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class HazardInteroperabilityConstants {

    /**
     * 
     */
    protected HazardInteroperabilityConstants() {
    }

    public static enum INTEROPERABILITY_TYPE {
        WARNGEN, RIVERPRO, GFE
    }

    public static class INTEROPERABILITY_KEYS {
        public static final String SITE_ID = KEY_PREFIX
                + HazardConstants.SITE_ID;

        public static final String PHENOMENON = KEY_PREFIX
                + HazardInteroperabilityConstants.PHENOMENON;

        public static final String SIGNIFICANCE = KEY_PREFIX
                + HazardInteroperabilityConstants.SIGNIFICANCE;

        public static final String HAZARD_EVENT_ID = KEY_PREFIX
                + HazardInteroperabilityConstants.HAZARD_EVENT_ID;

        public static final String ETN = KEY_PREFIX
                + HazardInteroperabilityConstants.ETN;
    }

    public static class INTEROPERABILITY_GFE_KEYS {
        public static final String SITE_ID = KEY_PREFIX
                + HazardConstants.SITE_ID;

        public static final String PHENOMENON = KEY_PREFIX
                + HazardInteroperabilityConstants.PHENOMENON;

        public static final String SIGNIFICANCE = KEY_PREFIX
                + HazardInteroperabilityConstants.SIGNIFICANCE;

        public static final String HAZARD_EVENT_ID = KEY_PREFIX
                + HazardInteroperabilityConstants.HAZARD_EVENT_ID;

        public static final String START_DATE = KEY_PREFIX
                + HazardInteroperabilityConstants.START_DATE;

        public static final String END_DATE = KEY_PREFIX
                + HazardInteroperabilityConstants.END_DATE;
    }

    private static final String KEY_PREFIX = "key.";

    public static final String PHENOMENON = "phen";

    public static final String SIGNIFICANCE = "sig";

    public static final String ETN = "etn";

    public static final String HAZARD_EVENT_ID = "hazardEventID";
    
    public static final String ACTIVE_TABLE_ID = "activeTableID";

    public static final String CREATION_DATE = "creationDate";

    public static final String INTEROPERABILITY_TYPE = "interoperabilityType";

    public static final String START_DATE = "startDate";

    public static final String END_DATE = "endDate";

    public static final String PARM_ID = "parmID";

    public static final String GEOMETRY = "geometry";
}
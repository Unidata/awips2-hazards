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
package com.raytheon.uf.common.dataplugin.events.hazards.event.vtec;

import java.util.Map;

import com.raytheon.uf.common.registry.annotations.RegistryObject;
import com.raytheon.uf.common.registry.annotations.RegistryObjectVersion;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;

/**
 * 
 * Record class holding practice Hazard Event VTEC information
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 4/5/2016     16577    Ben.Phillippe Initial creation
 * 5/3/2016     18193    Ben.Phillippe Replication of Hazard VTEC Records
 * 5/5/2016     6895     Ben.Phillippe RiverPro Interoperability changes
 * 
 * </pre>
 * 
 * @author bphillip
 * @version 1.0
 */
@DynamicSerialize
@RegistryObject
@RegistryObjectVersion(value = 1.0f)
public class PracticeHazardEventVtec extends HazardEventVtec {

    public PracticeHazardEventVtec() {
        super();
        this.practice = true;
    }

    public PracticeHazardEventVtec(String ugcZone,
            Map<String, Object> attributes) {
        super(ugcZone, attributes);
        this.practice = true;
    }
}

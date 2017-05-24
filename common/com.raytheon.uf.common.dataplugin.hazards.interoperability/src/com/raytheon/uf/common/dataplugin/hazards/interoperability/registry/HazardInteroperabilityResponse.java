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
package com.raytheon.uf.common.dataplugin.hazards.interoperability.registry;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.HazardEventManager.Include;
import com.raytheon.uf.common.dataplugin.events.hazards.registry.HazardEventResponse;
import com.raytheon.uf.common.dataplugin.hazards.interoperability.HazardInteroperabilityRecord;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Response object used to encapsulate query responses for interoperability
 * records
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 04, 2015 6895     Ben.Phillippe Finished HS data access refactor
 * Aug 20, 2015 6895     Ben.Phillippe Routing registry requests through request server
 * </pre>
 * 
 * @author bphillip
 * @version 1.0
 */
@XmlRootElement(name = "HazardInteroperabilityResponse")
@XmlAccessorType(XmlAccessType.NONE)
@DynamicSerialize
public class HazardInteroperabilityResponse extends HazardEventResponse {

    /**
     * The interoparability records
     */
    @XmlElement
    @DynamicSerializeElement
    private List<HazardInteroperabilityRecord> interopRecords;

    /**
     * Creates a new response object
     */
    public HazardInteroperabilityResponse() {
        super(Include.HISTORICAL_AND_LATEST_EVENTS, false);
    }

    /**
     * @return the interopRecords
     */
    public List<HazardInteroperabilityRecord> getInteropRecords() {
        return interopRecords;
    }

    /**
     * @param interopRecords
     *            the interopRecords to set
     */
    public void setInteropRecords(
            List<HazardInteroperabilityRecord> interopRecords) {
        this.interopRecords = interopRecords;
    }

}

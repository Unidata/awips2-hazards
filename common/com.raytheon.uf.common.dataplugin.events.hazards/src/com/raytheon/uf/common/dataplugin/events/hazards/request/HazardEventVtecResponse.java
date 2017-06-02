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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.raytheon.uf.common.activetable.ActiveTableMode;
import com.raytheon.uf.common.dataplugin.events.hazards.event.vtec.HazardEventVtec;
import com.raytheon.uf.common.dataplugin.events.hazards.registry.HazardEventResponse;
import com.raytheon.uf.common.dataplugin.events.hazards.registry.xmladapters.ActiveTableXmlAdapter;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.util.CollectionUtil;

/**
 * Returns the vtec records mapped to hazards that were retrieved as well as
 * information about any problems that may have occurred when retrieving the
 * requested information.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 8, 2014  2826       jsanchez     Initial creation
 * Aug 20, 2015 6895     Ben.Phillippe Routing registry requests through request server
 * Apr 05, 2016 16577    Ben.Phillippe Moved out of interoperability plugin and renamed
 * May 03, 2016 18193    Ben.Phillippe Replication of Hazard VTEC Records
 * </pre>
 * 
 * @author jsanchez
 * @version 1.0
 */
@DynamicSerialize
@XmlRootElement(name = "VtecInteroperabilityActiveTableResponse")
@XmlAccessorType(XmlAccessType.NONE)
public class HazardEventVtecResponse extends HazardEventResponse implements
        IReturnResults {

    /** The active table */
    @DynamicSerializeElement
    @XmlElement
    private List<HazardEventVtec> vtecRecords;

    /** Practice or Operational Mode */
    @DynamicSerializeElement
    @XmlElement
    private ActiveTableMode mode;

    /** Success flag */
    @DynamicSerializeElement
    @XmlElement
    private boolean success;

    /** Text of the exceptions */
    @DynamicSerializeElement
    @XmlElement
    private String exceptionText;

    public List<HazardEventVtec> getVtecRecords() {
        return vtecRecords;
    }

    public void setVtecRecords(List<HazardEventVtec> vtecRecords) {
        this.vtecRecords = vtecRecords;
    }

    /**
     * @return the mode
     */
    public ActiveTableMode getMode() {
        return mode;
    }

    /**
     * @param mode
     *            the mode to set
     */
    public void setMode(ActiveTableMode mode) {
        this.mode = mode;
    }

    public HazardEventVtecResponse() {
        this.success = true;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getExceptionText() {
        return exceptionText;
    }

    public void setExceptionText(String exceptionText) {
        this.exceptionText = exceptionText;
    }

    @Override
    public List<Map<String, Object>> getResults() {
        return toMap(getVtecRecords());
    }

    private List<Map<String, Object>> toMap(List<HazardEventVtec> vtecs) {
        if (CollectionUtil.isNullOrEmpty(vtecs)) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> retVal = new ArrayList<Map<String, Object>>(
                vtecs.size());
        for (HazardEventVtec vtec : vtecs) {
            retVal.add(vtec.toMap());
        }
        return retVal;
    }
}

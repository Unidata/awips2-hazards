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
package com.raytheon.uf.common.dataplugin.events.hazards.interoperability.requests.test;

import com.raytheon.uf.common.activetable.PracticeActiveTableRecord;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * TODO Add Description
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 11, 2014            jsanchez     Initial creation
 * 
 * </pre>
 * 
 * @author jsanchez
 * @version 1.0
 */
@DynamicSerialize
public class TestVtecInteroperabilityRelation {
    @DynamicSerializeElement
    private PracticeActiveTableRecord record;

    @DynamicSerializeElement
    private String eventID;

    @DynamicSerializeElement
    private String hazardType;

    public TestVtecInteroperabilityRelation() {

    }

    public PracticeActiveTableRecord getRecord() {
        return record;
    }

    public String getEventID() {
        return eventID;
    }

    public void setEventID(String eventID) {
        this.eventID = eventID;
    }

    public String getHazardType() {
        return hazardType;
    }

    public void setHazardType(String hazardType) {
        this.hazardType = hazardType;
    }

    public void setRecord(PracticeActiveTableRecord record) {
        this.record = record;
    }

}

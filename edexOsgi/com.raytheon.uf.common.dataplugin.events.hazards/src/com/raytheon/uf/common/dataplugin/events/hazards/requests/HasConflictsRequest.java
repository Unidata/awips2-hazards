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
package com.raytheon.uf.common.dataplugin.events.hazards.requests;

import java.util.Date;

import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.HazardEventManager.Mode;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.serialization.comm.IServerRequest;

/**
 * Request object to check if the IHazardEvent has any conflicts with any
 * existing grids.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 22, 2013 2277       jsanchez     Initial creation
 * Mar 24, 2014 3323       bkowal       The Mode is now a property that must
 *                                      be provided.
 * 
 * </pre>
 * 
 * @author jsanchez
 * @version 1.0
 */
@DynamicSerialize
public class HasConflictsRequest implements IServerRequest {

    @DynamicSerializeElement
    private String phenSig;

    @DynamicSerializeElement
    private String siteID;

    @DynamicSerializeElement
    private Date startTime;

    @DynamicSerializeElement
    private Date endTime;

    @DynamicSerializeElement
    private boolean practiceMode;

    public HasConflictsRequest() {
    }

    public HasConflictsRequest(Mode mode) {
        this.practiceMode = (mode == Mode.PRACTICE);
    }

    public Mode getMode() {
        return this.practiceMode ? Mode.PRACTICE : Mode.OPERATIONAL;
    }

    public String getPhenSig() {
        return phenSig;
    }

    public void setPhenSig(String phenSig) {
        this.phenSig = phenSig;
    }

    public String getSiteID() {
        return siteID;
    }

    public void setSiteID(String siteID) {
        this.siteID = siteID;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public boolean isPracticeMode() {
        return practiceMode;
    }

    public void setPracticeMode(boolean practiceMode) {
        this.practiceMode = practiceMode;
    }
}
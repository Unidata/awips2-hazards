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
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * August 2013  1360       hansen      Added fields for product information
 *
 **/
package gov.noaa.gsd.viz.hazards.jsonutilities;

import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.CREATION_TIME;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.ETNS;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.EXPIRATION_TIME;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HAZARD_EVENT_CHECKED;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HAZARD_EVENT_END_TIME_UNTIL_FURTHER_NOTICE;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HAZARD_EVENT_SELECTED;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.ISSUE_TIME;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.PILS;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.VTEC_CODES;
import gov.noaa.gsd.viz.hazards.display.deprecated.DeprecatedUtilities;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardStatus;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.events.ISessionEventManager;

/**
 * Implements many of the fields that are used on JSON events.
 * 
 * TODO This class is a nightmare and badly needs to go away. It's involved in
 * some but not all of the POJO/JSON conversions. But it also has some other
 * unrelated logic that converts colors; knows things about dam names. etc. See
 * {@link DeprecatedUtilities#eventsAsNodeJSON} for further discussion.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 21, 2013 1257       bsteffen    Initial creation
 * Aug  9, 2013 1921       daniel.s.schaffer@noaa.gov    Enhance {@link #getGeometry()} to support multi-polygons
 * Aug     2013 1360       hansen      Added fields for product information
 * Aug 25, 2013 1264       Chris.Golden Added support for drawing lines and points.
 * Sep 05, 2013 1264       blawrenc    Added support geometries of any
 *                                     different type (Lineal, Puntal, Polygonal).
 * Nov 14, 2013 1472       bkowal      Renamed hazard subtype to subType
 * Feb 16, 2014 2161       Chris.Golden Added support for end time "until further notice" flag.
 * Jul 31, 2015 7458       Robert.Blum  Added userName and workstation fields.
 * Aug 03, 2015 8836       Chris.Cody   Changes for a configurable Event Id
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
@Deprecated
public class DeprecatedEvent {

    /**
     * 
     */
    private static final String DAM_NAME = "damName";

    private static final String CAUSE = "cause";

    // TODO int
    private String eventID;

    private String displayEventID;

    private String pointID;

    private String streamName;

    private String backupSiteID;

    private double[][] draggedPoints;

    private Long startTime;

    private String hazardCategory;

    private Long creationTime;

    private String modifyCallbackToolName;

    private String status;

    private Long endTime;

    private Boolean endTimeUntilFurtherNotice;

    private String siteID;

    private Boolean checked;

    private String headline;

    private Boolean selected;

    private String color;

    private String type;

    private String fullType;

    private String cause;

    private String phen;

    private String subType;

    private String sig;

    private String damName;

    private Boolean polyModified;

    private Long expirationTime;

    private Long issueTime;

    private String etns;

    private String pils;

    private String vtecCodes;

    private String userName;

    private String workStation;

    public DeprecatedEvent() {
    }

    public DeprecatedEvent(IHazardEvent event) {
        Map<String, Serializable> attr = event.getHazardAttributes();

        eventID = event.getEventID();
        displayEventID = event.getDisplayEventID();
        pointID = (String) event.getHazardAttribute(HazardConstants.POINTID);
        streamName = (String) event
                .getHazardAttribute(HazardConstants.STREAM_NAME);
        setUserName(event.getUserName());
        setWorkStation(event.getWorkStation());
        startTime = event.getStartTime().getTime();
        endTime = event.getEndTime().getTime();
        endTimeUntilFurtherNotice = (Boolean) attr
                .get(HAZARD_EVENT_END_TIME_UNTIL_FURTHER_NOTICE);
        if (event.getCreationTime() != null) {
            /*
             * Has it been issued? Otherwise, don't assign an issueTime
             */
            HazardStatus status = event.getStatus();
            if (HazardStatus.hasEverBeenIssued(status)) {
                issueTime = (Long) event
                        .getHazardAttribute(HazardConstants.ISSUE_TIME);
            }
            creationTime = event.getCreationTime().getTime();
        } else {
            Object cTimeAttr = attr.get(CREATION_TIME);
            if (cTimeAttr instanceof Date) {
                creationTime = ((Date) cTimeAttr).getTime();
            }
        }
        Object hCatAttr = attr.get(ISessionEventManager.ATTR_HAZARD_CATEGORY);
        if (hCatAttr instanceof String) {
            hazardCategory = (String) hCatAttr;
        }
        siteID = event.getSiteID();
        backupSiteID = event.getSiteID();
        phen = event.getPhenomenon();
        sig = event.getSignificance();

        subType = event.getSubType();
        if (subType == null) {
            subType = "";
        }

        if (phen != null && sig != null) {
            type = phen + "." + sig;
            if (subType != null && !subType.isEmpty()) {
                type += "." + subType;
            }
        }

        if (event.getStatus() != null) {
            status = event.getStatus().toString().toLowerCase();
        }

        if (attr.containsKey(CAUSE)) {
            cause = attr.get(CAUSE).toString();
        }
        if (attr.containsKey(DAM_NAME)) {
            damName = attr.get(DAM_NAME).toString();
        }

        if (type != null) {
            fullType = type + " (" + headline + ")";
        }

        checked = (Boolean) attr.get(HAZARD_EVENT_CHECKED);
        color = "255 255 255";
        selected = (Boolean) attr.get(HAZARD_EVENT_SELECTED);

        draggedPoints = new double[0][];

        polyModified = true;

        if (attr.containsKey(EXPIRATION_TIME)) {
            expirationTime = (Long) attr.get(EXPIRATION_TIME);
        }
        if (attr.containsKey(ISSUE_TIME)) {
            issueTime = (Long) attr.get(ISSUE_TIME);
        }
        if (attr.containsKey(VTEC_CODES)) {
            Serializable eventVtecCodes = attr.get(VTEC_CODES);
            if (eventVtecCodes != null) {
                vtecCodes = attr.get(VTEC_CODES).toString();
            } else {
                vtecCodes = "[]";
            }
        }
        if (attr.containsKey(ETNS)) {
            Serializable eventVtecCodes = attr.get(ETNS);
            if (eventVtecCodes != null) {
                etns = attr.get(ETNS).toString();
            } else {
                etns = "[]";
            }
        }
        if (attr.containsKey(PILS)) {
            Serializable eventVtecCodes = attr.get(PILS);
            if (eventVtecCodes != null) {
                pils = attr.get(PILS).toString();
            } else {
                pils = "[]";
            }
        }

    }

    public String getEventID() {
        return eventID;
    }

    public String getDisplayEventID() {
        return displayEventID;
    }

    public void setEventID(String eventID) {
        this.eventID = eventID;
    }

    public String getPointID() {
        return pointID;
    }

    public void setPointID(String pointID) {
        this.pointID = pointID;
    }

    public String getBackupSiteID() {
        return backupSiteID;
    }

    public void setBackupSiteID(String backupSiteID) {
        this.backupSiteID = backupSiteID;
    }

    public double[][] getDraggedPoints() {
        return draggedPoints;
    }

    public void setDraggedPoints(double[][] draggedPoints) {
        this.draggedPoints = draggedPoints;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public String getHazardCategory() {
        return hazardCategory;
    }

    public void setHazardCategory(String hazardCategory) {
        this.hazardCategory = hazardCategory;
    }

    public Long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Long creationTime) {
        this.creationTime = creationTime;
    }

    public String getModifyCallbackToolName() {
        return modifyCallbackToolName;
    }

    public void setModifyCallbackToolName(String modifyCallbackToolName) {
        this.modifyCallbackToolName = modifyCallbackToolName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public Boolean isEndTimeUntilFurtherNotice() {
        return endTimeUntilFurtherNotice;
    }

    public void setEndTimeUntilFurtherNotice(Boolean endTimeUntilFurtherNotice) {
        this.endTimeUntilFurtherNotice = endTimeUntilFurtherNotice;
    }

    public String getSiteID() {
        return siteID;
    }

    public void setSiteID(String siteID) {
        this.siteID = siteID;
    }

    public Boolean getChecked() {
        return checked;
    }

    public void setChecked(Boolean checked) {
        this.checked = checked;
    }

    public String getHeadline() {
        return headline;
    }

    public void setHeadline(String headline) {
        this.headline = headline;
    }

    public Boolean getSelected() {
        return selected;
    }

    public void setSelected(Boolean selected) {
        this.selected = selected;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFullType() {
        return fullType;
    }

    public void setFullType(String fullType) {
        this.fullType = fullType;
    }

    public String getCause() {
        return cause;
    }

    public void setCause(String cause) {
        this.cause = cause;
    }

    public String getPhen() {
        return phen;
    }

    public void setPhen(String phen) {
        this.phen = phen;
    }

    public String getSubType() {
        return subType;
    }

    public void setSubType(String subType) {
        this.subType = subType;
    }

    public String getSig() {
        return sig;
    }

    public void setSig(String sig) {
        this.sig = sig;
    }

    public String getDamName() {
        return damName;
    }

    public void setDamName(String damName) {
        this.damName = damName;
    }

    public void setIssueTime(Long issueTime) {
        this.issueTime = issueTime;
    }

    public Long getIssueTime() {
        return issueTime;
    }

    public void setExpirationTime(Long expirationTime) {
        this.expirationTime = expirationTime;
    }

    public Long getExpirationTime() {
        return expirationTime;
    }

    public void setEtns(String etns) {
        this.etns = etns;
    }

    public String getEtns() {
        return etns;
    }

    public void setVtecCodes(String vtecCodes) {
        this.etns = vtecCodes;
    }

    public String getVtecCodes() {
        return vtecCodes;
    }

    public void setPils(String pils) {
        this.pils = pils;
    }

    public String getPils() {
        return pils;
    }

    public Boolean getPolyModified() {
        return polyModified;
    }

    public void setPolyModified(Boolean polyModified) {
        this.polyModified = polyModified;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    /**
     * @return the streamName
     */
    public String getStreamName() {
        return streamName;
    }

    /**
     * @param streamName
     *            the streamName to set
     */
    public void setStreamName(String streamName) {
        this.streamName = streamName;
    }

    public String getWorkStation() {
        return workStation;
    }

    public void setWorkStation(String workStation) {
        this.workStation = workStation;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

}

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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.registry.annotations.RegistryObject;
import com.raytheon.uf.common.registry.annotations.RegistryObjectVersion;
import com.raytheon.uf.common.registry.annotations.SlotAttribute;
import com.raytheon.uf.common.registry.annotations.SlotAttributeConverter;
import com.raytheon.uf.common.registry.ebxml.slots.DateSlotConverter;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * 
 * Record class holding Hazard Event VTEC information
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 4/5/2016     16577    Ben.Phillippe Initial creation
 * 4/7/2016     16577    Robert.Blum   Fixed casting of seg.
 * 5/3/2016     18193    Ben.Phillippe Replication of Hazard VTEC Records
 * 5/5/2016     6895     Ben.Phillippe RiverPro Interoperability changes
 * 
 * </pre>
 * 
 * @author bphillip
 * @version 1.0
 */
@XmlRootElement(name = "HazardEventVtec")
@XmlAccessorType(XmlAccessType.NONE)
@DynamicSerialize
@RegistryObject({ HazardConstants.OFFICE_ID,
        HazardConstants.EVENT_TRACKING_NUMBER,
        HazardConstants.HAZARD_EVENT_PHEN, HazardConstants.HAZARD_EVENT_SIG,
        HazardConstants.UGC_ZONE_ID })
@RegistryObjectVersion(value = 1.0f)
public class HazardEventVtec {

    private static final String DEFAULT_POINT_ID = "00000";

    private static final String DEFAULT_IMMEDIATE_CAUSE = "UU";

    private static final String DEFAULT_FLOOD_RECORD = "OO";

    @DynamicSerializeElement
    @XmlAttribute
    @SlotAttribute(HazardConstants.PRACTICE)
    protected boolean practice;

    @DynamicSerializeElement
    @XmlAttribute
    @SlotAttribute(HazardConstants.OFFICE_ID)
    protected String officeid;

    @DynamicSerializeElement
    @XmlAttribute
    @SlotAttribute(HazardConstants.HAZARD_EVENT_PHEN)
    protected String phen;

    @DynamicSerializeElement
    @XmlAttribute
    @SlotAttribute(HazardConstants.HAZARD_EVENT_SIG)
    protected String sig;

    @DynamicSerializeElement
    @XmlAttribute
    @SlotAttribute(HazardConstants.ETN)
    protected Integer etn;

    @DynamicSerializeElement
    @XmlAttribute
    protected String ugcZone;

    @DynamicSerializeElement
    @XmlAttribute
    protected Date previousStart;

    @DynamicSerializeElement
    @XmlAttribute
    protected Date previousEnd;

    @DynamicSerializeElement
    @XmlAttribute
    protected String phensig;

    @DynamicSerializeElement
    @XmlAttribute
    protected String status;

    @DynamicSerializeElement
    @XmlAttribute
    protected String hdln;

    @DynamicSerializeElement
    @XmlAttribute
    @SlotAttribute(HazardConstants.ISSUE_TIME)
    @SlotAttributeConverter(DateSlotConverter.class)
    protected Date issueTime;

    @DynamicSerializeElement
    @XmlAttribute
    protected Date endTime;

    @DynamicSerializeElement
    @XmlAttribute
    protected Date startTime;

    @DynamicSerializeElement
    @XmlAttribute
    protected Integer seg;

    @DynamicSerializeElement
    @XmlAttribute
    @SlotAttribute(HazardConstants.EVENT_ID)
    protected String eventID;

    @DynamicSerializeElement
    @XmlAttribute
    protected String pil;

    @DynamicSerializeElement
    @XmlAttribute
    protected String vtecstr;

    @DynamicSerializeElement
    @XmlAttribute
    protected String subtype;

    @DynamicSerializeElement
    @XmlAttribute
    protected String hvtecstr;

    @DynamicSerializeElement
    @XmlAttribute
    protected Integer ufn;

    @DynamicSerializeElement
    @XmlAttribute
    protected String act;

    @DynamicSerializeElement
    @XmlAttribute
    protected String key;

    @DynamicSerializeElement
    @XmlAttribute
    protected String downgradeFromKey;

    @DynamicSerializeElement
    @XmlAttribute
    protected String downgradeFromEtn;

    @DynamicSerializeElement
    @XmlAttribute
    protected String downgradeFromAct;

    @DynamicSerializeElement
    @XmlAttribute
    protected String downgradeFromPhen;

    @DynamicSerializeElement
    @XmlAttribute
    protected String downgradeFromSig;

    @DynamicSerializeElement
    @XmlAttribute
    protected String downgradeFromSubtype;

    @DynamicSerializeElement
    @XmlAttribute
    protected String upgradeFromKey;

    @DynamicSerializeElement
    @XmlAttribute
    protected String upgradeFromEtn;

    @DynamicSerializeElement
    @XmlAttribute
    protected String upgradeFromAct;

    @DynamicSerializeElement
    @XmlAttribute
    protected String upgradeFromPhen;

    @DynamicSerializeElement
    @XmlAttribute
    protected String upgradeFromSig;

    @DynamicSerializeElement
    @XmlAttribute
    protected String upgradeFromSubtype;

    @DynamicSerializeElement
    @XmlAttribute
    protected Date fallBelow;

    @DynamicSerializeElement
    @XmlAttribute
    protected String floodRecord;

    @DynamicSerializeElement
    @XmlAttribute
    protected String immediateCause;

    @DynamicSerializeElement
    @XmlAttribute
    protected Date riseAbove;

    @DynamicSerializeElement
    @XmlAttribute
    protected Date crest;

    @DynamicSerializeElement
    @XmlAttribute
    protected String floodSeverity;

    @DynamicSerializeElement
    @XmlAttribute
    protected String pointID;

    public HazardEventVtec() {
        super();
    }

    @SuppressWarnings("unchecked")
    public HazardEventVtec(String ugcZone, Map<String, Object> attributes) {
        super();
        this.officeid = (String) attributes.get(HazardConstants.OFFICE_ID);
        this.phen = (String) attributes.get(HazardConstants.HAZARD_EVENT_PHEN);
        this.sig = (String) attributes.get(HazardConstants.HAZARD_EVENT_SIG);
        this.etn = (Integer) attributes
                .get(HazardConstants.EVENT_TRACKING_NUMBER);
        this.ugcZone = ugcZone;

        extractHVTECValues(
                (Map<String, Object>) attributes.get(HazardConstants.HVTEC));

        this.phensig = (String) attributes.get(HazardConstants.PHEN_SIG);
        this.status = (String) attributes
                .get(HazardConstants.HAZARD_EVENT_STATUS);
        this.hdln = (String) attributes.get(HazardConstants.HDLN);
        this.issueTime = new Date(Long.parseLong(
                attributes.get(HazardConstants.ISSUE_TIME).toString()) * 1000);
        this.endTime = new Date(Long.parseLong(attributes
                .get(HazardConstants.HAZARD_EVENT_END_TIME).toString()) * 1000);
        this.startTime = new Date(Long.parseLong(attributes
                .get(HazardConstants.HAZARD_EVENT_START_TIME).toString())
                * 1000);
        Object segment = attributes.get(HazardConstants.SEG);
        if (segment instanceof Integer) {
            this.seg = (Integer) segment;
        } else if (segment instanceof String) {
            this.seg = Integer.parseInt((String) segment);
        }
        this.eventID = (String) attributes.get(HazardConstants.EVENT_ID);
        this.pil = (String) attributes.get(HazardConstants.PIL);
        this.vtecstr = (String) attributes.get(HazardConstants.VTECSTR);
        this.subtype = (String) attributes.get(HazardConstants.SUBTYPE);
        this.hvtecstr = (String) attributes.get(HazardConstants.HVTECSTR);
        this.ufn = (Integer) attributes.get(HazardConstants.UFN);
        this.act = (String) attributes.get(HazardConstants.ACT);
        this.key = (String) attributes.get(HazardConstants.KEY);
        if (this.previousStart != null) {
            this.previousStart = new Date(Long.parseLong(
                    attributes.get(HazardConstants.PREVIOUS_START).toString())
                    * 1000);
        }
        if (this.previousEnd != null) {
            this.previousEnd = new Date(Long.parseLong(
                    attributes.get(HazardConstants.PREVIOUS_END).toString())
                    * 1000);
        }

    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put(HazardConstants.OFFICE_ID, officeid);
        map.put(HazardConstants.HAZARD_EVENT_PHEN, phen);
        map.put(HazardConstants.HAZARD_EVENT_SIG, sig);
        map.put(HazardConstants.EVENT_TRACKING_NUMBER, etn);
        map.put(HazardConstants.UGC_ID, ugcZone);
        map.put(HazardConstants.HVTEC, hvtecToMap());
        map.put(HazardConstants.PHEN_SIG, phensig);
        map.put(HazardConstants.HAZARD_EVENT_STATUS, status);
        map.put(HazardConstants.HDLN, hdln);
        map.put(HazardConstants.ISSUE_TIME, issueTime.getTime() / 1000);
        map.put(HazardConstants.HAZARD_EVENT_END_TIME,
                endTime.getTime() / 1000);
        map.put(HazardConstants.HAZARD_EVENT_START_TIME,
                startTime.getTime() / 1000);
        map.put(HazardConstants.SEG, seg);
        map.put(HazardConstants.EVENT_ID, eventID);
        map.put(HazardConstants.PIL, pil);
        map.put(HazardConstants.VTECSTR, vtecstr);
        map.put(HazardConstants.SUBTYPE, subtype);
        map.put(HazardConstants.HVTECSTR, hvtecstr);
        map.put(HazardConstants.UFN, ufn);
        map.put(HazardConstants.ACT, act);
        map.put(HazardConstants.KEY, key);
        if (previousStart != null) {
            map.put(HazardConstants.PREVIOUS_START,
                    previousStart.getTime() / 1000);
        }
        if (previousEnd != null) {
            map.put(HazardConstants.PREVIOUS_END, previousEnd.getTime() / 1000);
        }
        return map;
    }

    public Map<String, Object> hvtecToMap() {
        Map<String, Object> map = new HashMap<>();
        if (fallBelow != null) {
            map.put(HazardConstants.FALL_BELOW, fallBelow.getTime() / 1000);
        }
        map.put(HazardConstants.FLOOD_RECORD, floodRecord);
        map.put(HazardConstants.IMMEDIATE_CAUSE, immediateCause);
        if (riseAbove != null) {
            map.put(HazardConstants.RISE_ABOVE, riseAbove.getTime() / 1000);
        }
        if (crest != null) {
            map.put(HazardConstants.CREST, crest.getTime() / 1000);
        }
        map.put(HazardConstants.FLOOD_SEVERITY_CATEGORY, floodSeverity);
        map.put(HazardConstants.POINTID, pointID);
        return map;
    }

    private void extractHVTECValues(Map<String, Object> map) {
        if (map == null) {
            return;
        }
        if (map.get(HazardConstants.FALL_BELOW) != null) {
            this.fallBelow = new Date(Long.parseLong(
                    map.get(HazardConstants.FALL_BELOW).toString()) * 1000);
        }
        this.floodRecord = (String) map.get(HazardConstants.FLOOD_RECORD);
        if (this.floodRecord == null) {
            this.floodRecord = DEFAULT_FLOOD_RECORD;
        }
        this.immediateCause = (String) map.get(HazardConstants.IMMEDIATE_CAUSE);
        if (this.immediateCause == null) {
            this.immediateCause = DEFAULT_IMMEDIATE_CAUSE;
        }
        if (map.get(HazardConstants.RISE_ABOVE) != null) {
            this.riseAbove = new Date(Long.parseLong(
                    map.get(HazardConstants.RISE_ABOVE).toString()) * 1000);
        }
        if (map.get(HazardConstants.CREST) != null) {
            this.crest = new Date(
                    Long.parseLong(map.get(HazardConstants.CREST).toString())
                            * 1000);
        }
        this.floodSeverity = String
                .valueOf(map.get(HazardConstants.FLOOD_SEVERITY_CATEGORY));
        this.pointID = (String) map.get(HazardConstants.POINTID);
        if (this.pointID == null) {
            this.pointID = DEFAULT_POINT_ID;
        }
    }

    public Date getPreviousStart() {
        return previousStart;
    }

    public void setPreviousStart(Date previousStart) {
        this.previousStart = previousStart;
    }

    public Date getPreviousEnd() {
        return previousEnd;
    }

    public void setPreviousEnd(Date previousEnd) {
        this.previousEnd = previousEnd;
    }

    public String getPhensig() {
        return phensig;
    }

    public void setPhensig(String phensig) {
        this.phensig = phensig;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getHdln() {
        return hdln;
    }

    public void setHdln(String hdln) {
        this.hdln = hdln;
    }

    public Date getIssueTime() {
        return issueTime;
    }

    public void setIssueTime(Date issueTime) {
        this.issueTime = issueTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Integer getSeg() {
        return seg;
    }

    public void setSeg(Integer seg) {
        this.seg = seg;
    }

    public String getEventID() {
        return eventID;
    }

    public void setEventID(String eventID) {
        this.eventID = eventID;
    }

    public String getPil() {
        return pil;
    }

    public void setPil(String pil) {
        this.pil = pil;
    }

    public String getVtecstr() {
        return vtecstr;
    }

    public void setVtecstr(String vtecstr) {
        this.vtecstr = vtecstr;
    }

    public String getSubtype() {
        return subtype;
    }

    public void setSubtype(String subtype) {
        this.subtype = subtype;
    }

    public String getHvtecstr() {
        return hvtecstr;
    }

    public void setHvtecstr(String hvtecstr) {
        this.hvtecstr = hvtecstr;
    }

    public Integer getUfn() {
        return ufn;
    }

    public void setUfn(Integer ufn) {
        this.ufn = ufn;
    }

    public String getAct() {
        return act;
    }

    public void setAct(String act) {
        this.act = act;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Date getFallBelow() {
        return fallBelow;
    }

    public void setFallBelow(Date fallBelow) {
        this.fallBelow = fallBelow;
    }

    public String getFloodRecord() {
        return floodRecord;
    }

    public void setFloodRecord(String floodRecord) {
        this.floodRecord = floodRecord;
    }

    public String getImmediateCause() {
        return immediateCause;
    }

    public void setImmediateCause(String immediateCause) {
        this.immediateCause = immediateCause;
    }

    public Date getRiseAbove() {
        return riseAbove;
    }

    public void setRiseAbove(Date riseAbove) {
        this.riseAbove = riseAbove;
    }

    public Date getCrest() {
        return crest;
    }

    public void setCrest(Date crest) {
        this.crest = crest;
    }

    public String getFloodSeverity() {
        return floodSeverity;
    }

    public void setFloodSeverity(String floodSeverity) {
        this.floodSeverity = floodSeverity;
    }

    public String getPointID() {
        return pointID;
    }

    public void setPointID(String pointID) {
        this.pointID = pointID;
    }

    public String getOfficeid() {
        return officeid;
    }

    public void setOfficeid(String officeid) {
        this.officeid = officeid;
    }

    public String getPhen() {
        return phen;
    }

    public void setPhen(String phen) {
        this.phen = phen;
    }

    public String getSig() {
        return sig;
    }

    public void setSig(String sig) {
        this.sig = sig;
    }

    public Integer getEtn() {
        return etn;
    }

    public void setEtn(Integer etn) {
        this.etn = etn;
    }

    public String getUgcZone() {
        return ugcZone;
    }

    public void setUgcZone(String ugcZone) {
        this.ugcZone = ugcZone;
    }

    public String getDowngradeFromKey() {
        return downgradeFromKey;
    }

    public void setDowngradeFromKey(String downgradeFromKey) {
        this.downgradeFromKey = downgradeFromKey;
    }

    public String getDowngradeFromEtn() {
        return downgradeFromEtn;
    }

    public void setDowngradeFromEtn(String downgradeFromEtn) {
        this.downgradeFromEtn = downgradeFromEtn;
    }

    public String getDowngradeFromAct() {
        return downgradeFromAct;
    }

    public void setDowngradeFromAct(String downgradeFromAct) {
        this.downgradeFromAct = downgradeFromAct;
    }

    public String getDowngradeFromPhen() {
        return downgradeFromPhen;
    }

    public void setDowngradeFromPhen(String downgradeFromPhen) {
        this.downgradeFromPhen = downgradeFromPhen;
    }

    public String getDowngradeFromSig() {
        return downgradeFromSig;
    }

    public void setDowngradeFromSig(String downgradeFromSig) {
        this.downgradeFromSig = downgradeFromSig;
    }

    public String getDowngradeFromSubtype() {
        return downgradeFromSubtype;
    }

    public void setDowngradeFromSubtype(String downgradeFromSubtype) {
        this.downgradeFromSubtype = downgradeFromSubtype;
    }

    public String getUpgradeFromKey() {
        return upgradeFromKey;
    }

    public void setUpgradeFromKey(String upgradeFromKey) {
        this.upgradeFromKey = upgradeFromKey;
    }

    public String getUpgradeFromEtn() {
        return upgradeFromEtn;
    }

    public void setUpgradeFromEtn(String upgradeFromEtn) {
        this.upgradeFromEtn = upgradeFromEtn;
    }

    public String getUpgradeFromAct() {
        return upgradeFromAct;
    }

    public void setUpgradeFromAct(String upgradeFromAct) {
        this.upgradeFromAct = upgradeFromAct;
    }

    public String getUpgradeFromPhen() {
        return upgradeFromPhen;
    }

    public void setUpgradeFromPhen(String upgradeFromPhen) {
        this.upgradeFromPhen = upgradeFromPhen;
    }

    public String getUpgradeFromSig() {
        return upgradeFromSig;
    }

    public void setUpgradeFromSig(String upgradeFromSig) {
        this.upgradeFromSig = upgradeFromSig;
    }

    public String getUpgradeFromSubtype() {
        return upgradeFromSubtype;
    }

    public void setUpgradeFromSubtype(String upgradeFromSubtype) {
        this.upgradeFromSubtype = upgradeFromSubtype;
    }

    public boolean isPractice() {
        return practice;
    }

    public void setPractice(boolean practice) {
        this.practice = practice;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((etn == null) ? 0 : etn.hashCode());
        result = prime * result
                + ((officeid == null) ? 0 : officeid.hashCode());
        result = prime * result + ((phen == null) ? 0 : phen.hashCode());
        result = prime * result + ((sig == null) ? 0 : sig.hashCode());
        result = prime * result + ((ugcZone == null) ? 0 : ugcZone.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        HazardEventVtec other = (HazardEventVtec) obj;
        if (etn == null) {
            if (other.etn != null) {
                return false;
            }
        } else if (!etn.equals(other.etn)) {
            return false;
        }
        if (officeid == null) {
            if (other.officeid != null) {
                return false;
            }
        } else if (!officeid.equals(other.officeid)) {
            return false;
        }
        if (phen == null) {
            if (other.phen != null) {
                return false;
            }
        } else if (!phen.equals(other.phen)) {
            return false;
        }
        if (sig == null) {
            if (other.sig != null) {
                return false;
            }
        } else if (!sig.equals(other.sig)) {
            return false;
        }
        if (ugcZone == null) {
            if (other.ugcZone != null) {
                return false;
            }
        } else if (!ugcZone.equals(other.ugcZone)) {
            return false;
        }
        return true;
    }
}

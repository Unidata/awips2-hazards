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

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.persist.PersistableDataObject;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * 
 * Record class holding any upgrade or downgrade information for a Hazard Event
 * VTEC Record
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 4/5/2016     16577    Ben.Phillippe Initial creation
 * 5/3/2016     18193    Ben.Phillippe Replication of Hazard VTEC Records
 * 
 * </pre>
 * 
 * @author bphillip
 * @version 1.0
 */
@XmlAccessorType(XmlAccessType.NONE)
@DynamicSerialize
@Embeddable
public class UpgradeDowngrade extends PersistableDataObject<Long> {

    private static final long serialVersionUID = 1655831168508672058L;

    @DynamicSerializeElement
    @XmlAttribute
    @Column
    private String key;

    @DynamicSerializeElement
    @XmlAttribute
    @Column
    private Long etn;

    @DynamicSerializeElement
    @XmlAttribute
    @Column
    private String act;

    @DynamicSerializeElement
    @XmlAttribute
    @Column
    private String phen;

    @DynamicSerializeElement
    @XmlAttribute
    @Column
    private String sig;

    @DynamicSerializeElement
    @XmlAttribute
    @Column
    private String subtype;

    public UpgradeDowngrade() {
        super();
    }

    public UpgradeDowngrade(Map<String, Object> map) {
        super();
        this.key = (String) map.get(HazardConstants.KEY);
        this.etn = (Long) map.get(HazardConstants.EVENT_TRACKING_NUMBER);
        this.act = (String) map.get(HazardConstants.ACT);
        this.phen = (String) map.get(HazardConstants.HAZARD_EVENT_PHEN);
        this.sig = (String) map.get(HazardConstants.HAZARD_EVENT_SIG);
        this.subtype = (String) map.get(HazardConstants.HAZARD_EVENT_SUB_TYPE);
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Long getEtn() {
        return etn;
    }

    public void setEtn(Long etn) {
        this.etn = etn;
    }

    public String getAct() {
        return act;
    }

    public void setAct(String act) {
        this.act = act;
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

    public String getSubtype() {
        return subtype;
    }

    public void setSubtype(String subtype) {
        this.subtype = subtype;
    }
}

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
package com.raytheon.uf.viz.hazards.sessionmanager.config.impl.types;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.raytheon.uf.common.colormap.Color;
import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardEventUtilities;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IReadableHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.SessionConfigurationManager;

/**
 * XML compatible object for loading and storing color tables.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * May 24, 2013  1257      bsteffen     Initial creation.
 * Dec 17, 2017 20739      Chris.Golden Refactored away access to directly
 *                                      mutable session events.
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
@XmlRootElement(name = "HazardsColorTable")
@XmlAccessorType(XmlAccessType.NONE)
public class XmlHazardsColorTable implements IHazardsColorTable {

    @XmlElement(name = "Hazard")
    private List<XmlHazardColorEntry> hazards;

    public List<XmlHazardColorEntry> getHazards() {
        return hazards;
    }

    public void setHazards(List<XmlHazardColorEntry> hazards) {
        this.hazards = hazards;
    }

    @Override
    public Color getColor(IReadableHazardEvent event) {
        String key = HazardEventUtilities.getHazardType(event);
        if (key == null) {
            return null;
        }
        for (XmlHazardColorEntry h : hazards) {
            if (h.getLabel().equals(key)) {
                return SessionConfigurationManager.getColor(h.getColor());
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}

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
package com.raytheon.uf.common.dataplugin.events.hazards.event;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Date;
import java.util.Map;

import com.raytheon.uf.common.dataplugin.events.IEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardState;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.ProductClass;
import com.vividsolutions.jts.geom.Geometry;

/**
 * Interface for use with both {@link PracticeHazardEvent} and
 * {@link HazardEvent}
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 1, 2012            mnash     Initial creation
 * 
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */

public interface IHazardEvent extends IEvent {
    public Comparator<IHazardEvent> SORT_BY_ISSUE_TIME = new Comparator<IHazardEvent>() {
        @Override
        public int compare(IHazardEvent o1, IHazardEvent o2) {
            return o1.getIssueTime().compareTo(o2.getIssueTime());
        }
    };

    public String getSiteID();

    public void setSiteID(String site);

    public String getEventID();

    public void setEventID(String uuid);

    public HazardState getState();

    public void setState(HazardState state);

    public String getPhenomenon();

    public void setPhenomenon(String phenomenon);

    public String getSignificance();

    public void setSignificance(String significance);

    public Date getIssueTime();

    public void setIssueTime(Date date);

    public void setEndTime(Date date);

    public void setStartTime(Date date);

    public void setGeometry(Geometry geom);

    public ProductClass getHazardMode();

    public void setHazardMode(ProductClass mode);

    public Map<String, Serializable> getHazardAttributes();

    public void setHazardAttributes(Map<String, Serializable> attributes);

    public void addHazardAttribute(String key, Serializable value);

    public void removeHazardAttribute(String key);

    public Serializable getHazardAttribute(String key);
}
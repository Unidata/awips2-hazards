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
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardStatus;
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
 * Oct 1, 2012             mnash       Initial creation
 * Nov 14, 2013 1472       bkowal      Renamed hazard subtype to subType
 * Apr 23, 2014 2925       Chris.Golden Augmented with additional methods to
 *                                      set the type components atomically, or
 *                                      the start and end time atomically.
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */

public interface IHazardEvent extends IEvent {
    public Comparator<IHazardEvent> SORT_BY_PERSIST_TIME = new Comparator<IHazardEvent>() {
        @Override
        public int compare(IHazardEvent o1, IHazardEvent o2) {
            Serializable o1Attr = o1
                    .getHazardAttribute(HazardConstants.PERSIST_TIME);
            Serializable o2Attr = o2
                    .getHazardAttribute(HazardConstants.PERSIST_TIME);
            Date o1Time = null;
            if (o1Attr instanceof Date) {
                o1Time = (Date) o1Attr;
            } else {
                o1Time = new Date((Long) o1Attr);
            }

            Date o2Time = null;

            if (o2Attr instanceof Date) {
                o2Time = (Date) o2Attr;
            } else {
                o2Time = new Date((Long) o2Attr);
            }

            return o1Time.compareTo(o2Time);
        }
    };

    public String getSiteID();

    public void setSiteID(String site);

    public String getEventID();

    public void setEventID(String eventId);

    public HazardStatus getStatus();

    public void setStatus(HazardStatus state);

    public String getPhenomenon();

    public void setPhenomenon(String phenomenon);

    public String getSignificance();

    public void setSignificance(String significance);

    public String getSubType();

    public void setSubType(String subtype);

    public String getHazardType();

    public void setHazardType(String phenomenon, String significance,
            String subtype);

    public Date getCreationTime();

    public void setCreationTime(Date date);

    public void setStartTime(Date date);

    public void setEndTime(Date date);

    public void setTimeRange(Date startTime, Date endTime);

    public void setGeometry(Geometry geom);

    public ProductClass getHazardMode();

    public void setHazardMode(ProductClass mode);

    public Map<String, Serializable> getHazardAttributes();

    public void setHazardAttributes(Map<String, Serializable> attributes);

    public void addHazardAttribute(String key, Serializable value);

    public void removeHazardAttribute(String key);

    public Serializable getHazardAttribute(String key);
}
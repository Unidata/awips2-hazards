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

import gov.noaa.gsd.common.visuals.VisualFeature;
import gov.noaa.gsd.common.visuals.VisualFeaturesList;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Date;
import java.util.Map;

import com.raytheon.uf.common.dataplugin.events.IEvent;
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
 * Jun 30, 2014 3512       Chris.Golden Added addHazardAttributes() method.
 * Feb 22, 2015 6561       mpduff      Added getter/setter for insert time, changed 
 *                                     SORT_BY_PERSIST_TIME to use insert time
 * Jul 31, 2015 7458       Robert.Blum Added new userName and workstation methods.
 * Aug 03, 2015 8836       Chris.Cody   Changes for a configurable Event Id
 * Mar 01, 2016 15676      Chris.Golden Added visual features to hazard event.
 * Mar 26, 2016 15676      Chris.Golden Added more methods to get and set
 *                                      individual visual features.
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */

public interface IHazardEvent extends IEvent {
    public Comparator<IHazardEvent> SORT_BY_PERSIST_TIME = new Comparator<IHazardEvent>() {
        @Override
        public int compare(IHazardEvent o1, IHazardEvent o2) {
            Date o1Time = o1.getInsertTime();
            Date o2Time = o2.getInsertTime();

            return o1Time.compareTo(o2Time);
        }
    };

    public Geometry getGeometry();

    public Geometry getProductGeometry();

    public VisualFeature getBaseVisualFeature(String identifier);

    /**
     * Replace the base visual feature with the same identifier as the specified
     * visual feature with the latter.
     * 
     * @param visualFeature
     *            New visual feature.
     * @return True if the new visual feature replaced the old one, false if no
     *         base visual feature with the given identifier was found.
     */
    public boolean setBaseVisualFeature(VisualFeature visualFeature);

    public VisualFeaturesList getBaseVisualFeatures();

    public void setBaseVisualFeatures(VisualFeaturesList visualFeatures);

    public VisualFeature getSelectedVisualFeature(String identifier);

    /**
     * Replace the selected visual feature with the same identifier as the
     * specified visual feature with the latter.
     * 
     * @param visualFeature
     *            New visual feature.
     * @return True if the new visual feature replaced the old one, false if no
     *         selected visual feature with the given identifier was found.
     */
    public boolean setSelectedVisualFeature(VisualFeature visualFeature);

    public VisualFeaturesList getSelectedVisualFeatures();

    public void setSelectedVisualFeatures(VisualFeaturesList visualFeatures);

    public void setVisualFeatures(VisualFeaturesList baseVisualFeatures,
            VisualFeaturesList selectedVisualFeatures);

    public String getSiteID();

    public void setSiteID(String site);

    public String getEventID();

    public void setEventID(String eventId);

    public String getDisplayEventID();

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

    public void setInsertTime(Date date);

    public Date getInsertTime();

    public void setUserName(String userName);

    public String getUserName();

    public void setWorkStation(String workStation);

    public String getWorkStation();

    public void setTimeRange(Date startTime, Date endTime);

    public void setGeometry(Geometry geom);

    public void setProductGeometry(Geometry geom);

    public ProductClass getHazardMode();

    public void setHazardMode(ProductClass mode);

    public Map<String, Serializable> getHazardAttributes();

    public void setHazardAttributes(Map<String, Serializable> attributes);

    public void addHazardAttribute(String key, Serializable value);

    public void addHazardAttributes(Map<String, Serializable> attributes);

    public void removeHazardAttribute(String key);

    public Serializable getHazardAttribute(String key);
}
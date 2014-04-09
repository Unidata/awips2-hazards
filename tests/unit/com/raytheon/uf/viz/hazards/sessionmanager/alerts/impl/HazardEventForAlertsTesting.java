/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package com.raytheon.uf.viz.hazards.sessionmanager.alerts.impl;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.google.common.collect.Maps;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardState;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.ProductClass;
import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardEventUtilities;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.vividsolutions.jts.geom.Geometry;

/**
 * Description: Test stub for a {@link IHazardEvent} TODO Replace with a java
 * object mocking approach.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * July 26, 2013  1325      daniel.s.schaffer@noaa.gov      Initial creation
 * Nov 14, 2013 1472       bkowal      Renamed hazard subtype to subType
 * 
 * Dec 2013      2368       thansen    Added getHazardType
 * </pre>
 * 
 * @author daniel.s.schaffer@noaa.gov
 * @version 1.0
 */
public class HazardEventForAlertsTesting implements IHazardEvent {

    private Date startTime;

    private Date endTime;

    private Date creationTime;

    private Geometry geometry;

    private String siteID;

    private String eventID;

    private String phenomenon;

    private String significance;

    private String subtype;

    private ProductClass hazardMode;

    private HazardState state;

    private Map<String, Serializable> hazardAttributes;

    public HazardEventForAlertsTesting() {
        hazardAttributes = Maps.newHashMap();
    }

    /**
     * @return the startTime
     */
    @Override
    public Date getStartTime() {
        return startTime;
    }

    /**
     * @param startTime
     *            the startTime to set
     */
    @Override
    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    /**
     * @return the endTime
     */
    @Override
    public Date getEndTime() {
        return endTime;
    }

    /**
     * @param endTime
     *            the endTime to set
     */
    @Override
    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    /**
     * @return the issueTime
     */
    @Override
    public Date getCreationTime() {
        return creationTime;
    }

    /**
     * @param issueTime
     *            the issueTime to set
     */
    @Override
    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    /**
     * @return the geometry
     */
    @Override
    public Geometry getGeometry() {
        return geometry;
    }

    /**
     * @param geometry
     *            the geometry to set
     */
    @Override
    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    /**
     * @return the siteID
     */
    @Override
    public String getSiteID() {
        return siteID;
    }

    /**
     * @param siteID
     *            the siteID to set
     */
    @Override
    public void setSiteID(String siteID) {
        this.siteID = siteID;
    }

    /**
     * @return the eventID
     */
    @Override
    public String getEventID() {
        return eventID;
    }

    /**
     * @param eventID
     *            the eventID to set
     */
    @Override
    public void setEventID(String eventID) {
        this.eventID = eventID;
    }

    /**
     * @return the phenomenon
     */
    @Override
    public String getPhenomenon() {
        return phenomenon;
    }

    /**
     * @param phenomenon
     *            the phenomenon to set
     */
    @Override
    public void setPhenomenon(String phenomenon) {
        this.phenomenon = phenomenon;
    }

    /**
     * @return the significance
     */
    @Override
    public String getSignificance() {
        return significance;
    }

    /**
     * @param significance
     *            the significance to set
     */
    @Override
    public void setSignificance(String significance) {
        this.significance = significance;
    }

    /**
     * @return the hazardMode
     */
    @Override
    public ProductClass getHazardMode() {
        return hazardMode;
    }

    /**
     * @param hazardMode
     *            the hazardMode to set
     */
    @Override
    public void setHazardMode(ProductClass hazardMode) {
        this.hazardMode = hazardMode;
    }

    /**
     * @return the hazardAttributes
     */
    @Override
    public Map<String, Serializable> getHazardAttributes() {
        return hazardAttributes;
    }

    /**
     * @param hazardAttributes
     *            the hazardAttributes to set
     */
    @Override
    public void setHazardAttributes(Map<String, Serializable> hazardAttributes) {
        this.hazardAttributes = hazardAttributes;
    }

    @Override
    public HazardState getState() {
        return state;
    }

    @Override
    public void setState(HazardState state) {
        this.state = state;
    }

    @Override
    public String getSubType() {
        return subtype;
    }

    @Override
    public void setSubType(String subtype) {
        this.subtype = subtype;
    }

    @Override
    public void addHazardAttribute(String key, Serializable value) {
        hazardAttributes.put(key, value);
    }

    @Override
    public void removeHazardAttribute(String key) {
        hazardAttributes.remove(key);
    }

    @Override
    public Serializable getHazardAttribute(String key) {
        return hazardAttributes.get(key);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public String getHazardType() {
        return HazardEventUtilities.getHazardType(this);
    }

}

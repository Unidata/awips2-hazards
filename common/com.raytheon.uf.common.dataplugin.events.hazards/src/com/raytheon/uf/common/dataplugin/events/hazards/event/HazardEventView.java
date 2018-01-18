/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Evaluation & Decision Support Branch (EDS)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package com.raytheon.uf.common.dataplugin.events.hazards.event;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardStatus;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.ProductClass;
import com.raytheon.uf.common.message.WsId;
import com.vividsolutions.jts.geom.Geometry;

import gov.noaa.gsd.common.utilities.geometry.IAdvancedGeometry;
import gov.noaa.gsd.common.visuals.VisualFeature;
import gov.noaa.gsd.common.visuals.VisualFeaturesList;

/**
 * View of an {@link IHazardEvent}.
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Dec 11, 2017   20739    Chris.Golden Initial creation.
 * Jan 26, 2018   33428    Chris.Golden Added issuance count.
 * </pre>
 *
 * @author golden
 */
public class HazardEventView implements IHazardEventView {

    // Private Variables

    /**
     * Hazard event for which this object is acting as a view.
     */
    private final IHazardEvent event;

    // Public Constructors

    /**
     * Construct an instance for viewing the specified hazard event.
     * 
     * @param event
     *            Event for which this object is to act as a view.
     */
    public HazardEventView(IHazardEvent event) {
        this.event = event;
    }

    // Public Methods

    @Override
    public Date getStartTime() {
        return event.getStartTime();
    }

    @Override
    public Date getEndTime() {
        return event.getEndTime();
    }

    @Override
    public boolean isModified() {
        return event.isModified();
    }

    @Override
    public Geometry getFlattenedGeometry() {
        return event.getFlattenedGeometry();
    }

    @Override
    public Geometry getProductGeometry() {
        return event.getProductGeometry();
    }

    @Override
    public IAdvancedGeometry getGeometry() {
        return event.getGeometry();
    }

    @Override
    public VisualFeature getVisualFeature(String identifier) {
        return event.getVisualFeature(identifier);
    }

    @Override
    public VisualFeaturesList getVisualFeatures() {
        return event.getVisualFeatures();
    }

    @Override
    public String getSiteID() {
        return event.getSiteID();
    }

    @Override
    public String getEventID() {
        return event.getEventID();
    }

    @Override
    public String getDisplayEventID() {
        return event.getDisplayEventID();
    }

    @Override
    public HazardStatus getStatus() {
        return event.getStatus();
    }

    @Override
    public int getIssuanceCount() {
        return event.getIssuanceCount();
    }

    @Override
    public String getPhenomenon() {
        return event.getPhenomenon();
    }

    @Override
    public String getSignificance() {
        return event.getSignificance();
    }

    @Override
    public String getSubType() {
        return event.getSubType();
    }

    @Override
    public String getHazardType() {
        return event.getHazardType();
    }

    @Override
    public String getPhensig() {
        return event.getPhensig();
    }

    @Override
    public Date getCreationTime() {
        return event.getCreationTime();
    }

    @Override
    public Date getInsertTime() {
        return event.getInsertTime();
    }

    @Override
    public WsId getWsId() {
        return event.getWsId();
    }

    @Override
    public ProductClass getHazardMode() {
        return event.getHazardMode();
    }

    @Override
    public Source getSource() {
        return event.getSource();
    }

    @Override
    public Map<String, Serializable> getHazardAttributes() {
        return event.getHazardAttributes();
    }

    @Override
    public Serializable getHazardAttribute(String key) {
        return event.getHazardAttribute(key);
    }
}

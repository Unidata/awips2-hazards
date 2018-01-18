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

import com.raytheon.uf.common.dataplugin.events.IEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardStatus;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.ProductClass;
import com.raytheon.uf.common.message.WsId;
import com.vividsolutions.jts.geom.Geometry;

import gov.noaa.gsd.common.utilities.geometry.IAdvancedGeometry;
import gov.noaa.gsd.common.visuals.VisualFeature;
import gov.noaa.gsd.common.visuals.VisualFeaturesList;

/**
 * Interface describing the methods that must be implemented by classes intended
 * to act as readable hazard events, that is, that expose the values of hazard
 * event properties. Sub-interfaces or implementations may add the capability to
 * modify said properties.
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
public interface IReadableHazardEvent extends IEvent {

    public enum Source {
        USER, RECOMMENDER
    };

    public boolean isModified();

    public Geometry getFlattenedGeometry();

    public Geometry getProductGeometry();

    public IAdvancedGeometry getGeometry();

    public VisualFeature getVisualFeature(String identifier);

    public VisualFeaturesList getVisualFeatures();

    public String getSiteID();

    public String getEventID();

    public String getDisplayEventID();

    public HazardStatus getStatus();

    public int getIssuanceCount();

    public String getPhenomenon();

    public String getSignificance();

    public String getSubType();

    public String getHazardType();

    public String getPhensig();

    public Date getCreationTime();

    public Date getInsertTime();

    public WsId getWsId();

    public ProductClass getHazardMode();

    public Source getSource();

    public Map<String, Serializable> getHazardAttributes();

    public Serializable getHazardAttribute(String key);
}

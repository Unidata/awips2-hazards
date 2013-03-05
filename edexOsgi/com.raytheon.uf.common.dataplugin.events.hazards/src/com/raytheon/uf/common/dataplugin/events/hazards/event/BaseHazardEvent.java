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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardState;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.ProductClass;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

/**
 * An {@link IHazardEvent} that has no storage annotations, rather, it can be
 * converted into any other {@link IHazardEvent}.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 24, 2013            mnash     Initial creation
 * 
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */

public class BaseHazardEvent implements IHazardEvent {
    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(BaseHazardEvent.class);

    private Date startTime;

    private Date endTime;

    private Geometry geometry;

    private String site;

    private String eventId;

    private HazardState hazardState;

    private String phenomenon;

    private String significance;

    private Date issueTime;

    private ProductClass hazardMode;

    private Map<String, Serializable> attributes;

    /**
     * 
     */
    public BaseHazardEvent() {
        eventId = UUID.randomUUID().toString();
        attributes = new HashMap<String, Serializable>();
    }

    public BaseHazardEvent(IHazardEvent event) {
        this();
        setEventID(event.getEventID());
        setSiteID(event.getSiteID());
        setEndTime(event.getEndTime());
        setStartTime(event.getStartTime());
        setIssueTime(event.getIssueTime());
        setGeometry(event.getGeometry());
        setPhenomenon(event.getPhenomenon());
        setSignificance(event.getSignificance());
        setState(event.getState());
        setHazardMode(event.getHazardMode());
        if (event.getHazardAttributes() != null) {
            setHazardAttributes(event.getHazardAttributes());
        }
    }

    @Override
    public Date getStartTime() {
        return startTime;
    }

    @Override
    public Date getEndTime() {
        return endTime;
    }

    @Override
    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(String geom) {
        WKTReader reader = new WKTReader();
        try {
            this.geometry = reader.read(geom);
        } catch (ParseException e) {
            statusHandler.handle(Priority.ERROR,
                    "Unable to read in geometry text", e);
        }
    }

    @Override
    public String getSiteID() {
        return site;
    }

    @Override
    public void setSiteID(String site) {
        this.site = site;
    }

    @Override
    public String getEventID() {
        return eventId;
    }

    @Override
    public void setEventID(String uuid) {
        this.eventId = uuid;
    }

    @Override
    public HazardState getState() {
        return hazardState;
    }

    @Override
    public void setState(HazardState state) {
        this.hazardState = state;
    }

    public void setState(String state) {
        this.hazardState = HazardState.valueOf(String.valueOf(state)
                .toUpperCase());
    }

    @Override
    public String getPhenomenon() {
        return phenomenon;
    }

    @Override
    public void setPhenomenon(String phenomenon) {
        this.phenomenon = phenomenon;
    }

    @Override
    public String getSignificance() {
        return significance;
    }

    @Override
    public void setSignificance(String significance) {
        this.significance = significance;
    }

    @Override
    public Date getIssueTime() {
        return issueTime;
    }

    @Override
    public void setIssueTime(Date date) {
        this.issueTime = date;
    }

    @Override
    public void setEndTime(Date date) {
        this.endTime = date;
    }

    @Override
    public void setStartTime(Date date) {
        this.startTime = date;
    }

    @Override
    public void setGeometry(Geometry geom) {
        this.geometry = geom;
    }

    @Override
    public ProductClass getHazardMode() {
        return hazardMode;
    }

    @Override
    public void setHazardMode(ProductClass mode) {
        this.hazardMode = mode;
    }

    public void setHazardMode(String mode) {
        this.hazardMode = ProductClass.valueOf(mode);
    }

    public Map<String, Serializable> getHazardAttributes() {
        return attributes;
    }

    @Override
    public void setHazardAttributes(Map<String, Serializable> attributes) {
        this.attributes = attributes;
    }

    @Override
    public void addHazardAttribute(String key, Serializable value) {
        attributes.put(key, value);
    }

    @Override
    public void removeHazardAttribute(String key) {
        attributes.remove(key);
    }

    @Override
    public Serializable getHazardAttribute(String key) {
        return attributes.get(key);
    }

}

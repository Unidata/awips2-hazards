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
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * August 2013  1360       hansen      Added fields for product information
 *
 **/
package gov.noaa.gsd.viz.hazards.jsonutilities;

import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.*;
import gov.noaa.gsd.viz.hazards.display.deprecated.DeprecatedUtilities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.codehaus.jackson.annotate.JsonIgnore;

import com.google.common.collect.Lists;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardState;
import com.raytheon.uf.common.dataplugin.events.hazards.event.BaseHazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.events.ISessionEventManager;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Lineal;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.Polygonal;
import com.vividsolutions.jts.geom.Puntal;

/**
 * Implements many of the fields that are used on JSON events.
 * 
 * TODO This class is a nightmare and badly needs to go away. It's involved in
 * some but not all of the POJO/JSON conversions. But it also has some other
 * unrelated logic that converts colors; knows things about dam names. etc. See
 * {@link DeprecatedUtilities#eventsAsNodeJSON} for further discussion.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 21, 2013 1257       bsteffen    Initial creation
 * Aug  9, 2013 1921       daniel.s.schaffer@noaa.gov    Enhance {@link #getGeometry()} to support multi-polygons
 * Aug     2013 1360       hansen      Added fields for product information
 * Aug 25, 2013 1264       Chris.Golden Added support for drawing lines and points.
 * Sep 05, 2013 1264       blawrenc    Added support geometries of any
 *                                     different type (Lineal, Puntal, Polygonal).
 * Nov 14, 2013 1472       bkowal      Renamed hazard subtype to subType
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
@Deprecated
public class DeprecatedEvent {

    /**
     * 
     */
    private static final String DAM_NAME = "damName";

    private static final String CAUSE = "cause";

    // TODO int
    private String eventID;

    private String pointID;

    private DeprecatedShape[] shapes;

    private String backupSiteID;

    private double[][] draggedPoints;

    private Long startTime;

    private String hazardCategory;

    private Long creationTime;

    private String modifyCallbackToolName;

    private String state;

    private Long endTime;

    private String siteID;

    private Boolean checked;

    private String headline;

    private Boolean selected;

    private String color;

    private String type;

    private String fullType;

    private String cause;

    private String phen;

    private String subType;

    private String sig;

    private String damName;

    private String geoType;

    private Boolean polyModified;

    private Long expirationTime;

    private Long issueTime;

    private String etns;

    private String pils;

    private String vtecCodes;

    private static GeometryFactory geometryFactory = new GeometryFactory();

    public DeprecatedEvent() {
    }

    public DeprecatedEvent(IHazardEvent event) {
        Map<String, Serializable> attr = event.getHazardAttributes();

        eventID = event.getEventID();
        pointID = (String) event.getHazardAttribute(HazardConstants.POINTID);
        startTime = event.getStartTime().getTime();
        endTime = event.getEndTime().getTime();
        if (event.getIssueTime() != null) {
            issueTime = event.getIssueTime().getTime();
            creationTime = event.getIssueTime().getTime();
        } else {
            Object cTimeAttr = attr.get(CREATION_TIME);
            if (cTimeAttr instanceof Date) {
                creationTime = ((Date) cTimeAttr).getTime();
            }
        }
        Object hCatAttr = attr.get(ISessionEventManager.ATTR_HAZARD_CATEGORY);
        if (hCatAttr instanceof String) {
            hazardCategory = (String) hCatAttr;
        }
        siteID = event.getSiteID();
        backupSiteID = event.getSiteID();
        phen = event.getPhenomenon();
        sig = event.getSignificance();

        subType = event.getSubType();
        if (subType == null) {
            subType = "";
        }

        if (phen != null && sig != null) {
            type = phen + "." + sig;
            if (subType != null && !subType.isEmpty()) {
                type += "." + subType;
            }
        }

        if (event.getState() != null) {
            state = event.getState().toString().toLowerCase();
        }

        if (attr.containsKey(CAUSE)) {
            cause = attr.get(CAUSE).toString();
        }
        if (attr.containsKey(DAM_NAME)) {
            damName = attr.get(DAM_NAME).toString();
        }

        if (type != null) {
            fullType = type + " (" + headline + ")";
        }

        checked = (Boolean) attr.get(ISessionEventManager.ATTR_CHECKED);
        color = "255 255 255";
        selected = (Boolean) attr.get(ISessionEventManager.ATTR_SELECTED);

        if (event.getState() != HazardState.ENDED
                && Boolean.TRUE.equals(attr
                        .get(ISessionEventManager.ATTR_ISSUED))) {
            state = HazardState.ISSUED.toString().toLowerCase();
        }

        draggedPoints = new double[0][];

        Geometry geom = event.getGeometry();

        int numberOfGeometries = geom.getNumGeometries();
        shapes = new DeprecatedShape[numberOfGeometries];

        for (int i = 0; i < numberOfGeometries; ++i) {
            shapes[i] = convertGeometry(geom.getGeometryN(i));
        }

        if (geom instanceof Polygonal) {
            geoType = HazardConstants.AREA_TYPE;
        } else if (geom instanceof Lineal) {
            geoType = HazardConstants.LINE_TYPE;
        } else if (geom instanceof Puntal) {
            geoType = HazardConstants.POINT_TYPE;
        }

        polyModified = true;

        if (attr.containsKey(EXPIRATION_TIME)) {
            expirationTime = (Long) attr.get(EXPIRATION_TIME);
        }
        if (attr.containsKey(ISSUE_TIME)) {
            issueTime = (Long) attr.get(ISSUE_TIME);
        }
        if (attr.containsKey(VTEC_CODES)) {
            Serializable eventVtecCodes = attr.get(VTEC_CODES);
            if (eventVtecCodes != null) {
                vtecCodes = attr.get(VTEC_CODES).toString();
            } else {
                vtecCodes = "[]";
            }
        }
        if (attr.containsKey(ETNS)) {
            Serializable eventVtecCodes = attr.get(ETNS);
            if (eventVtecCodes != null) {
                etns = attr.get(ETNS).toString();
            } else {
                etns = "[]";
            }
        }
        if (attr.containsKey(PILS)) {
            Serializable eventVtecCodes = attr.get(PILS);
            if (eventVtecCodes != null) {
                pils = attr.get(PILS).toString();
            } else {
                pils = "[]";
            }
        }

    }

    private DeprecatedShape convertGeometry(Geometry geom) {
        List<double[]> points = new ArrayList<double[]>();
        for (Coordinate c : geom.getCoordinates()) {
            points.add(new double[] { c.x, c.y });
        }
        DeprecatedShape shape = new DeprecatedShape();
        shape.setPoints(points.toArray(new double[0][]));
        shape.setShapeType(geom instanceof Polygonal ? "polygon"
                : (geom instanceof Lineal ? "line" : "point"));
        shape.setLabel(eventID + " ");
        if (type != null) {
            shape.setLabel(eventID + " " + type);
        }
        shape.setIsSelected(selected);
        shape.setIsVisible("true");
        shape.setInclude("true");
        return shape;
    }

    public String getEventID() {
        return eventID;
    }

    public void setEventID(String eventID) {
        this.eventID = eventID;
    }

    public String getPointID() {
        return pointID;
    }

    public void setPointID(String pointID) {
        this.pointID = pointID;
    }

    public DeprecatedShape[] getShapes() {
        return shapes;
    }

    public void setShapes(DeprecatedShape[] shapes) {
        this.shapes = shapes;
    }

    public String getBackupSiteID() {
        return backupSiteID;
    }

    public void setBackupSiteID(String backupSiteID) {
        this.backupSiteID = backupSiteID;
    }

    public double[][] getDraggedPoints() {
        return draggedPoints;
    }

    public void setDraggedPoints(double[][] draggedPoints) {
        this.draggedPoints = draggedPoints;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public String getHazardCategory() {
        return hazardCategory;
    }

    public void setHazardCategory(String hazardCategory) {
        this.hazardCategory = hazardCategory;
    }

    public Long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Long creationTime) {
        this.creationTime = creationTime;
    }

    public String getModifyCallbackToolName() {
        return modifyCallbackToolName;
    }

    public void setModifyCallbackToolName(String modifyCallbackToolName) {
        this.modifyCallbackToolName = modifyCallbackToolName;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public String getSiteID() {
        return siteID;
    }

    public void setSiteID(String siteID) {
        this.siteID = siteID;
    }

    public Boolean getChecked() {
        return checked;
    }

    public void setChecked(Boolean checked) {
        this.checked = checked;
    }

    public String getHeadline() {
        return headline;
    }

    public void setHeadline(String headline) {
        this.headline = headline;
    }

    public Boolean getSelected() {
        return selected;
    }

    public void setSelected(Boolean selected) {
        this.selected = selected;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFullType() {
        return fullType;
    }

    public void setFullType(String fullType) {
        this.fullType = fullType;
    }

    public String getCause() {
        return cause;
    }

    public void setCause(String cause) {
        this.cause = cause;
    }

    public String getPhen() {
        return phen;
    }

    public void setPhen(String phen) {
        this.phen = phen;
    }

    public String getSubType() {
        return subType;
    }

    public void setSubType(String subType) {
        this.subType = subType;
    }

    public String getSig() {
        return sig;
    }

    public void setSig(String sig) {
        this.sig = sig;
    }

    public String getDamName() {
        return damName;
    }

    public void setDamName(String damName) {
        this.damName = damName;
    }

    public void setGeoType(String geoType) {
        this.geoType = geoType;
    }

    public String getGeoType() {
        return geoType;
    }

    public void setIssueTime(Long issueTime) {
        this.issueTime = issueTime;
    }

    public Long getIssueTime() {
        return issueTime;
    }

    public void setExpirationTime(Long expirationTime) {
        this.expirationTime = expirationTime;
    }

    public Long getExpirationTime() {
        return expirationTime;
    }

    public void setEtns(String etns) {
        this.etns = etns;
    }

    public String getEtns() {
        return etns;
    }

    public void setVtecCodes(String vtecCodes) {
        this.etns = vtecCodes;
    }

    public String getVtecCodes() {
        return vtecCodes;
    }

    public void setPils(String pils) {
        this.pils = pils;
    }

    public String getPils() {
        return pils;
    }

    public Boolean getPolyModified() {
        return polyModified;
    }

    public void setPolyModified(Boolean polyModified) {
        this.polyModified = polyModified;
    }

    public IHazardEvent toHazardEvent() {
        IHazardEvent event = new BaseHazardEvent();
        if (pointID != null) {
            event.addHazardAttribute(HazardConstants.POINTID, pointID);
        }
        if (startTime != null) {
            event.setStartTime(new Date(startTime));
        }
        if (endTime != null) {
            event.setEndTime(new Date(endTime));
        }
        if (creationTime != null) {
            event.addHazardAttribute(CREATION_TIME, new Date(creationTime));
        }
        if (hazardCategory != null) {
            event.addHazardAttribute(ISessionEventManager.ATTR_HAZARD_CATEGORY,
                    hazardCategory);
        }
        event.setSiteID(siteID);
        event.setPhenomenon(phen);
        event.setSignificance(sig);

        event.setSubType(subType);

        if (state != null) {
            event.setState(HazardState.valueOf(state.toUpperCase()));
        }

        if (cause != null) {
            event.addHazardAttribute("cause", cause);
        }
        if (type != null) {
            event.addHazardAttribute("type", type);
        }
        if (damName != null) {
            event.addHazardAttribute(DAM_NAME, damName);
        }

        event.setGeometry(getGeometry());

        return event;
    }

    @JsonIgnore
    public Geometry getGeometry() {
        assert (shapes != null && shapes.length != 0);
        List<Geometry> geometries = Lists.newArrayList();
        for (DeprecatedShape shape : shapes) {
            if (shape.getShapeType().equals("point")) {
                geometries.add(buildPoint(shape));
            } else if (shape.getShapeType().equals("line")) {
                geometries.add(buildLine(shape));
            } else if (shape.getShapeType().equals("polygon")) {
                geometries.add(buildPolygon(shape));
            } else {
                throw new IllegalStateException(
                        "Cannot get geometry of shape of type \""
                                + shape.getShapeType() + "\"");
            }
        }

        Geometry result = new GeometryCollection(
                geometries.toArray(new Geometry[geometries.size()]),
                geometryFactory);

        return result;
    }

    private Point buildPoint(DeprecatedShape shape) {
        return geometryFactory.createPoint(new Coordinate(shape.points[0][0],
                shape.points[0][1]));
    }

    private LineString buildLine(DeprecatedShape shape) {
        return geometryFactory
                .createLineString(translateShapePointsToCoordinates(shape));
    }

    private Polygon buildPolygon(DeprecatedShape shape) {
        return geometryFactory.createPolygon(geometryFactory
                .createLinearRing(translateShapePointsToCoordinates(shape)),
                null);
    }

    private Coordinate[] translateShapePointsToCoordinates(DeprecatedShape shape) {
        List<Coordinate> coords = new ArrayList<Coordinate>(shape.points.length);
        for (double[] point : shape.points) {
            coords.add(new Coordinate(point[0], point[1]));
        }
        return coords.toArray(new Coordinate[shape.points.length]);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}

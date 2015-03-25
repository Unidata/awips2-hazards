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
package gov.noaa.gsd.viz.hazards.risecrestfall;

import gov.noaa.gsd.viz.hazards.risecrestfall.EventRegion.EventType;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.raytheon.uf.common.hazards.hydro.HydroConstants;
import com.raytheon.uf.common.time.util.TimeUtil;

/**
 * Data to build and draw the Rise/Crest/Fall editor.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 17, 2015   3847     mpduff      Initial creation
 * Mar 17, 2015   6974     mpduff      Scale to larger of max obs/fcst or flood cat lines.
 * Mar 26, 2015   7205     Robert.Blum Using new HydroConstants class.
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class GraphData {

    private final String SPLIT = " - ";

    /** List of points in the observed time series */
    private List<GraphPoint> observedPointList = new ArrayList<>();

    /** List of points in the forecast time series */
    private List<GraphPoint> forecastPointList = new ArrayList<>();

    private Date xMin;

    private Date xMax;

    private double yMin = Double.MAX_VALUE;

    private double yMax = Double.MIN_VALUE;

    private String name;

    private String lid;

    private String pe;

    private String dur;

    private String observedTs;

    private String forecastTs;

    private double actionStage = HydroConstants.MISSING_VALUE;

    private double floodStage = HydroConstants.MISSING_VALUE;

    private double moderateStage = HydroConstants.MISSING_VALUE;

    private double majorStage = HydroConstants.MISSING_VALUE;

    private double actionFlow = HydroConstants.MISSING_VALUE;

    private double floodFlow = HydroConstants.MISSING_VALUE;

    private double moderateFlow = HydroConstants.MISSING_VALUE;

    private double majorFlow = HydroConstants.MISSING_VALUE;

    private double riseAboveValue;

    private double crestValue;

    private double fallBelowValue;

    private String units;

    private int etn;

    private final Map<EventType, EventRegion> eventRegionList = new HashMap<EventType, EventRegion>();

    public GraphData() {
        xMin = TimeUtil.newGmtCalendar().getTime();
        xMax = new Date(0);
        initEventRegions();
    }

    private void initEventRegions() {
        EventRegion region = new EventRegion();
        region.setEventType(EventType.BEGIN);
        region.setVisible(true);

        eventRegionList.put(EventType.BEGIN, region);

        region = new EventRegion();
        region.setEventType(EventType.RISE);
        region.setVisible(true);

        eventRegionList.put(EventType.RISE, region);

        region = new EventRegion();
        region.setEventType(EventType.CREST);
        region.setVisible(true);

        eventRegionList.put(EventType.CREST, region);

        region = new EventRegion();
        region.setEventType(EventType.FALL);
        region.setVisible(true);

        eventRegionList.put(EventType.FALL, region);

        region = new EventRegion();
        region.setEventType(EventType.END);
        region.setVisible(true);

        eventRegionList.put(EventType.END, region);
    }

    /**
     * @return the xMin
     */
    public Date getxMin() {
        if (observedPointList != null) {
            for (GraphPoint p : this.observedPointList) {
                if (xMin == null) {
                    xMin = p.getX();
                } else if (p.getX().before(xMin)) {
                    xMin = p.getX();
                }
            }
        }
        return xMin;
    }

    /**
     * @return the xMax
     */
    public Date getxMax() {
        if (observedPointList != null) {
            for (GraphPoint p : this.observedPointList) {
                if (p.getX().after(xMax)) {
                    xMax = p.getX();
                }
            }
        }

        return xMax;
    }

    /**
     * @return the yMin
     */
    public double getyMin() {
        if (observedPointList != null) {
            for (GraphPoint p : this.observedPointList) {
                if (p.getY() < (yMin)) {
                    yMin = p.getY();
                }
            }
        }

        if (forecastPointList != null) {
            for (GraphPoint p : this.forecastPointList) {
                if (p.getY() < (yMin)) {
                    yMin = p.getY();
                }
            }
        }

        return yMin;
    }

    /**
     * @return the yMax
     */
    public double getyMax() {
        if (observedPointList != null) {
            for (GraphPoint p : this.observedPointList) {
                if (p.getY() > (yMax)) {
                    yMax = p.getY();
                }
            }
        }

        if (forecastPointList != null) {
            for (GraphPoint p : this.forecastPointList) {
                if (p.getY() > (yMax)) {
                    yMax = p.getY();
                }
            }
        }

        if (pe.startsWith("H") || pe.startsWith("h")) {
            if (yMax < majorStage) {
                yMax = majorStage;
            }
        }

        return yMax;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the lid
     */
    public String getLid() {
        return lid;
    }

    /**
     * @param lid
     *            the lid to set
     */
    public void setLid(String lid) {
        this.lid = lid;
    }

    /**
     * @return the pe
     */
    public String getPe() {
        return pe;
    }

    /**
     * @param pe
     *            the pe to set
     */
    public void setPe(String pe) {
        this.pe = pe;
    }

    /**
     * @return the dur
     */
    public String getDur() {
        return dur;
    }

    /**
     * @param dur
     *            the dur to set
     */
    public void setDur(String dur) {
        this.dur = dur;
    }

    /**
     * @return the observedTs
     */
    public String getObservedTs() {
        return observedTs;
    }

    /**
     * @param observedTs
     *            the observedTs to set
     */
    public void setObservedTs(String observedTs) {
        this.observedTs = observedTs;
    }

    /**
     * @return the forecastTs
     */
    public String getForecastTs() {
        return forecastTs;
    }

    /**
     * @param forecastTs
     *            the forecastTs to set
     */
    public void setForecastTs(String forecastTs) {
        this.forecastTs = forecastTs;
    }

    /**
     * @return the label
     */
    public String getLabel() {
        StringBuilder label = new StringBuilder();
        label.append(lid).append(SPLIT);
        label.append(name).append(SPLIT);
        label.append("Flood Stage: ").append(floodStage);
        return label.toString();
    }

    /**
     * @return the floodStage
     */
    public double getFloodStage() {
        return floodStage;
    }

    /**
     * @param floodStage
     *            the floodStage to set
     */
    public void setFloodStage(double floodStage) {
        this.floodStage = floodStage;
    }

    /**
     * @return the actionStage
     */
    public double getActionStage() {
        return actionStage;
    }

    /**
     * @param actionStage
     *            the actionStage to set
     */
    public void setActionStage(double actionStage) {
        this.actionStage = actionStage;
    }

    /**
     * @return the moderateStage
     */
    public double getModerateStage() {
        return moderateStage;
    }

    /**
     * @param moderateStage
     *            the moderateStage to set
     */
    public void setModerateStage(double moderateStage) {
        this.moderateStage = moderateStage;
    }

    /**
     * @return the majorStage
     */
    public double getMajorStage() {
        return majorStage;
    }

    /**
     * @param majorStage
     *            the majorStage to set
     */
    public void setMajorStage(double majorStage) {
        this.majorStage = majorStage;
    }

    /**
     * @return the actionFlow
     */
    public double getActionFlow() {
        return actionFlow;
    }

    /**
     * @param actionFlow
     *            the actionFlow to set
     */
    public void setActionFlow(double actionFlow) {
        this.actionFlow = actionFlow;
    }

    /**
     * @return the floodFlow
     */
    public double getFloodFlow() {
        return floodFlow;
    }

    /**
     * @param floodFlow
     *            the floodFlow to set
     */
    public void setFloodFlow(double floodFlow) {
        this.floodFlow = floodFlow;
    }

    /**
     * @return the moderateFlow
     */
    public double getModerateFlow() {
        return moderateFlow;
    }

    /**
     * @param moderateFlow
     *            the moderateFlow to set
     */
    public void setModerateFlow(double moderateFlow) {
        this.moderateFlow = moderateFlow;
    }

    /**
     * @return the majorFlow
     */
    public double getMajorFlow() {
        return majorFlow;
    }

    /**
     * @param majorFlow
     *            the majorFlow to set
     */
    public void setMajorFlow(double majorFlow) {
        this.majorFlow = majorFlow;
    }

    /**
     * @return the units
     */
    public String getUnits() {
        return units;
    }

    /**
     * @param units
     *            the units to set
     */
    public void setUnits(String units) {
        this.units = units;
    }

    /**
     * @return the etn
     */
    public int getEtn() {
        return etn;
    }

    /**
     * @param etn
     *            the etn to set
     */
    public void setEtn(int etn) {
        this.etn = etn;
    }

    public void setBeginDate(Date date) {
        eventRegionList.get(EventType.BEGIN).setDate(date);
    }

    public Date getBeginDate() {
        return eventRegionList.get(EventType.BEGIN).getDate();
    }

    public void setRiseDate(Date date) {
        eventRegionList.get(EventType.RISE).setDate(date);
    }

    public Date getRiseDate() {
        return eventRegionList.get(EventType.RISE).getDate();
    }

    public void setCrestDate(Date date) {
        eventRegionList.get(EventType.CREST).setDate(date);
    }

    public Date getCrestDate() {
        return eventRegionList.get(EventType.CREST).getDate();
    }

    public void setFallDate(Date date) {
        eventRegionList.get(EventType.FALL).setDate(date);
    }

    public Date getFallDate() {
        return eventRegionList.get(EventType.FALL).getDate();
    }

    public void setEndDate(Date date) {
        eventRegionList.get(EventType.END).setDate(date);
    }

    public Date getEndDate() {
        return eventRegionList.get(EventType.END).getDate();
    }

    public Map<EventType, EventRegion> getEventRegions() {
        return this.eventRegionList;
    }

    /**
     * @return the observedPointList
     */
    public List<GraphPoint> getObservedPointList() {
        return observedPointList;
    }

    /**
     * @param observedPointList
     *            the observedPointList to set
     */
    public void setObservedPointList(List<GraphPoint> observedPointList) {
        this.observedPointList = observedPointList;
    }

    /**
     * @return the forecastPointList
     */
    public List<GraphPoint> getForecastPointList() {
        return forecastPointList;
    }

    /**
     * @param forecastPointList
     *            the forecastPointList to set
     */
    public void setForecastPointList(List<GraphPoint> forecastPointList) {
        this.forecastPointList = forecastPointList;
    }

    public void addObservedPoint(GraphPoint point) {
        this.observedPointList.add(point);
    }

    public void addFcstPoint(GraphPoint point) {
        this.forecastPointList.add(point);
    }

    /**
     * @return the riseAboveValue
     */
    public double getRiseAboveValue() {
        return riseAboveValue;
    }

    /**
     * @param riseAboveValue
     *            the riseAboveValue to set
     */
    public void setRiseAboveValue(double riseAboveValue) {
        this.riseAboveValue = riseAboveValue;
    }

    /**
     * @return the crestValue
     */
    public double getCrestValue() {
        return crestValue;
    }

    /**
     * @param crestValue
     *            the crestValue to set
     */
    public void setCrestValue(double crestValue) {
        this.crestValue = crestValue;
    }

    /**
     * @return the fallBelowValue
     */
    public double getFallBelowValue() {
        return fallBelowValue;
    }

    /**
     * @param fallBelowValue
     *            the fallBelowValue to set
     */
    public void setFallBelowValue(double fallBelowValue) {
        this.fallBelowValue = fallBelowValue;
    }

    public void dispose() {
        for (Entry<EventType, EventRegion> entry : this.eventRegionList
                .entrySet()) {
            EventRegion reg = entry.getValue();
            if (reg != null) {
                reg.dispose();
            }
        }

    }
}

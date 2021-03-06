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
package com.raytheon.uf.viz.hazards.sessionmanager.config.types;

import java.util.Set;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.raytheon.uf.common.colormap.Color;

import gov.noaa.gsd.common.utilities.DragAndDropGeometryEditSource;
import gov.noaa.gsd.common.utilities.TimeResolution;

/**
 * Built from the objects defined in StartupConfig localization file.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * May 23, 2013 1257       bsteffen     Initial creation
 * Jan 19, 2015 4193       rferrel      Added disseminationOrder.
 * Feb 10, 2015 3961       chris.cody   Add gagePointFirstRecommender startup property
 * Feb 10, 2015 6393       Chris.Golden Added hazard detail tab text.
 * Apr 09, 2015 7382       Chris.Golden Added hazard detail "show sliders" flag.
 * Apr 15, 2015 3508       Chris.Golden Added hazard detail "assume wide" flag.
 * Sep 14, 2015 3473       Chris.Cody   Implement Hazard Services Import/Export through Central Registry server.
 * Sep 28, 2015 10302,8167 hansen       Added values to be optionally included in Settings - visibleSites, possibleSites, mapCenter, eventIdDisplayType
 * Mar 03, 2016  7452      Robert.Blum  Added configurable maps that are loaded on startup.
 * Oct 19, 2016 21873      Chris.Golden Added time resolution.
 * Mar 08, 2017 29138      Chris.Golden Added startup config option to allow persistence behavior
 *                                      to be tweaked via configuration.
 * Oct 23, 2017 21730      Chris.Golden Added defaultType.
 * Jan 22, 2018 25765      Chris.Golden Added "priority for drag-and-drop geometry edit" flag
 *                                      to make geometry editing from the spatial display more
 *                                      flexible.
 * Feb 02, 2018 26712      Chris.Golden Added geometryBufferColor and geometryBufferThickness
 *                                      properties to allow base geometries to be given visual
 *                                      buffers on the Spatial Display.
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */

public class StartUpConfig {
    @JsonProperty("Console")
    private Console console;

    @JsonProperty("isNational")
    boolean isNational;

    @JsonProperty("disseminationOrder")
    private String[] disseminationOrder;

    @JsonProperty("gagePointFirstRecommender")
    private String gagePointFirstRecommender;

    @JsonProperty("hazardDetailTabText")
    private String[] hazardDetailTabText;

    @JsonProperty("showHazardDetailStartEndTimeScale")
    private boolean showHazardDetailStartEndTimeScale;

    @JsonProperty("hazardDetailWide")
    private boolean hazardDetailWide;

    @JsonProperty("includeIssueButton")
    private boolean includeIssueButton;

    @JsonProperty("siteBackupBaseDir")
    private String siteBackupBaseDir;

    @JsonProperty("defaultType")
    private String defaultType;

    @JsonProperty("visibleSites")
    private Set<String> visibleSites;

    @JsonProperty("possibleSites")
    private Set<String> possibleSites;

    @JsonProperty("backupSites")
    private String[] backupSites;

    @JsonProperty("priorityForDragAndDropGeometryEdits")
    private DragAndDropGeometryEditSource priorityForDragAndDropGeometryEdits;

    @JsonProperty("eventIdDisplayType")
    private String eventIdDisplayType;

    @JsonProperty("mapCenter")
    private MapCenter mapCenter;

    @JsonProperty("displayMaps")
    private String[] displayMaps;

    @JsonProperty("timeResolution")
    private TimeResolution timeResolution;

    @JsonProperty("persistenceBehavior")
    private String persistenceBehavior;

    @JsonProperty("geometryBufferThickness")
    private double geometryBufferThickness;

    @JsonProperty("geometryBufferColor")
    private Color geometryBufferColor;

    public Console getConsole() {
        return console;
    }

    public void setConsole(Console console) {
        this.console = console;
    }

    public boolean isNational() {
        return isNational;
    }

    public void setIsNational(boolean isNational) {
        this.isNational = isNational;
    }

    public String[] getDisseminationOrder() {
        return disseminationOrder;
    }

    public void setDisseminationOrder(String[] disseminationOrder) {
        this.disseminationOrder = disseminationOrder;
    }

    public String getGagePointFirstRecommender() {
        return gagePointFirstRecommender;
    }

    public void setGagePointFirstRecommender(String gagePointFirstRecommender) {
        this.gagePointFirstRecommender = gagePointFirstRecommender;
    }

    public String[] getHazardDetailTabText() {
        return hazardDetailTabText;
    }

    public void setHazardDetailTabText(String[] hazardDetailTabText) {
        this.hazardDetailTabText = hazardDetailTabText;
    }

    public boolean includeIssueButton() {
        return includeIssueButton;
    }

    public void setIncludeIssueButton(boolean includeIssueButton) {
        this.includeIssueButton = includeIssueButton;
    }

    public boolean isShowingHazardDetailStartEndTimeScale() {
        return showHazardDetailStartEndTimeScale;
    }

    public void setShowingHazardDetailStartEndTimeScale(
            boolean showHazardDetailStartEndTimeScale) {
        this.showHazardDetailStartEndTimeScale = showHazardDetailStartEndTimeScale;
    }

    public boolean isHazardDetailToBeWide() {
        return hazardDetailWide;
    }

    public void setHazardDetailToBeWide(boolean hazardDetailWide) {
        this.hazardDetailWide = hazardDetailWide;
    }

    public String getSiteBackupBaseDir() {
        return siteBackupBaseDir;
    }

    public void setSiteBackupBaseDir(String siteBackupBaseDir) {
        this.siteBackupBaseDir = siteBackupBaseDir;
    }

    public String getDefaultType() {
        return defaultType;
    }

    public void setDefaultType(String defaultType) {
        this.defaultType = defaultType;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    public Set<String> getPossibleSites() {
        return possibleSites;
    }

    public void setPossibleSites(Set<String> possibleSites) {
        this.possibleSites = possibleSites;
    }

    public Set<String> getVisibleSites() {
        return visibleSites;
    }

    public void setVisibleSites(Set<String> visibleSites) {
        this.visibleSites = visibleSites;
    }

    public String[] getBackupSites() {
        return backupSites;
    }

    public void setBackupSites(String[] backupSites) {
        this.backupSites = backupSites;
    }

    public String getEventIdDisplayType() {
        return (this.eventIdDisplayType);
    }

    public void setEventIdDisplayType(String eventIdDisplayType) {
        this.eventIdDisplayType = eventIdDisplayType;
    }

    public MapCenter getMapCenter() {
        return mapCenter;
    }

    public void setMapCenter(MapCenter mapCenter) {
        this.mapCenter = mapCenter;
    }

    public TimeResolution getTimeResolution() {
        return (timeResolution == null ? TimeResolution.MINUTES
                : timeResolution);
    }

    public void setTimeResolution(TimeResolution timeResolution) {
        this.timeResolution = timeResolution;
    }

    public DragAndDropGeometryEditSource getPriorityForDragAndDropGeometryEdits() {
        return (priorityForDragAndDropGeometryEdits == null
                ? DragAndDropGeometryEditSource.BOUNDING_BOX
                : priorityForDragAndDropGeometryEdits);
    }

    public void setPriorityForDragAndDropGeometryEdits(
            DragAndDropGeometryEditSource priorityForDragAndDropGeometryEdits) {
        this.priorityForDragAndDropGeometryEdits = priorityForDragAndDropGeometryEdits;
    }

    public String getPersistenceBehavior() {
        return persistenceBehavior;
    }

    public void setPersistenceBehavior(String persistenceBehavior) {
        this.persistenceBehavior = persistenceBehavior;
    }

    public double getGeometryBufferThickness() {
        return geometryBufferThickness;
    }

    public void setGeometryBufferThickness(double geometryBufferThickness) {
        this.geometryBufferThickness = geometryBufferThickness;
    }

    public Color getGeometryBufferColor() {
        return geometryBufferColor;
    }

    public void setGeometryBufferColor(Color geometryBufferColor) {
        this.geometryBufferColor = geometryBufferColor;
    }

    public String[] getDisplayMaps() {
        return (displayMaps == null ? new String[0] : displayMaps);
    }

    public void setDisplayMaps(String[] displayMaps) {
        this.displayMaps = displayMaps;
    }
}

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

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Contains all the user customizable configuration options which affect the use
 * of the HazardServices UI.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 20, 2013 1257       bsteffen    Initial creation
 * Aug 22, 2013  787       blawrenc    Added capability to associate the setting
 *                                     with one or more perspectives.
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
@XmlRootElement(name = "HazardServicesSettings")
@XmlAccessorType(XmlAccessType.FIELD)
public class Settings {

    /**
     * The ID of this settings object.
     */
    private String settingsID;

    /**
     * Which types of events should be loaded/displayed
     */
    private Set<String> visibleTypes;

    /**
     * Which statuses of events should be loaded/displayed
     */
    private Set<String> visibleStatuses;

    /**
     * Which product generators can be run
     */
    private List<Tool> toolbarTools;

    /**
     * How long the time window should be
     */
    private Long defaultTimeDisplayDuration;

    /**
     * Where to center the map when this settings is loaded.
     */
    private MapCenter mapCenter;

    /**
     * Which hazard category new events should go into.
     */
    private String defaultCategory;

    /**
     * Which sites events should be loaded/displayed
     */
    private Set<String> visibleSites;

    /**
     * A Pretty name for this configuration to show users.
     */
    private String displayName;

    /**
     * A reasonable duration for new events.
     */
    private Long defaultDuration;

    /**
     * Which columns to display in the temporal view.
     */
    private List<String> visibleColumns;

    /**
     * All columns that can be displayed in the temporal view.
     */
    private Map<String, Column> columns;

    /**
     * ID of the settings on which this one is based, used for incremental
     * override.
     */
    private String staticSettingsID;

    /**
     * Whether new events should be added to the selection or replace the
     * selection.
     */
    private Boolean addToSelected;

    /**
     * Flag indicating whether or not newly drawn geometries should be added to
     * the current selected hazard event.
     */
    private Boolean addGeometryToSelected;

    /**
     * Identifiers of perspectives (if any) associated with this Setting. When
     * Hazard Services is started, these are searched to determine the
     * appropriate setting to load.
     */
    private Set<String> perspectiveIDs;

    public Settings() {

    }

    public Settings(Settings other) {
        apply(other);
    }

    /**
     * Copy all settings from another Settings object into this one.
     * 
     * @param other
     */
    public void apply(Settings other) {
        setSettingsID(other.getSettingsID());
        setVisibleTypes(other.getVisibleTypes());
        setVisibleStatuses(other.getVisibleStatuses());
        setToolbarTools(other.getToolbarTools());
        setDefaultTimeDisplayDuration(other.getDefaultTimeDisplayDuration());
        setMapCenter(other.getMapCenter());
        setDefaultCategory(other.getDefaultCategory());
        setVisibleSites(other.getVisibleSites());
        setDisplayName(other.getDisplayName());
        setDefaultDuration(other.getDefaultDuration());
        setVisibleColumns(other.getVisibleColumns());
        setColumns(other.getColumns());
        setStaticSettingsID(other.getStaticSettingsID());
        setAddToSelected(other.getAddToSelected());
        setAddGeometryToSelected(other.getAddGeometryToSelected());
    }

    public String getSettingsID() {
        return settingsID;
    }

    public void setSettingsID(String settingsID) {
        this.settingsID = settingsID;
    }

    public Set<String> getVisibleTypes() {
        return visibleTypes;
    }

    public void setVisibleTypes(Set<String> visibleTypes) {
        this.visibleTypes = visibleTypes;
    }

    public Set<String> getVisibleStatuses() {
        return visibleStatuses;
    }

    public void setVisibleStatuses(Set<String> visibleStatuses) {
        this.visibleStatuses = visibleStatuses;
    }

    public List<Tool> getToolbarTools() {
        return toolbarTools;
    }

    public void setToolbarTools(List<Tool> toolbarTools) {
        this.toolbarTools = toolbarTools;
    }

    public Long getDefaultTimeDisplayDuration() {
        return defaultTimeDisplayDuration;
    }

    public void setDefaultTimeDisplayDuration(Long defaultTimeDisplayDuration) {
        this.defaultTimeDisplayDuration = defaultTimeDisplayDuration;
    }

    public MapCenter getMapCenter() {
        return mapCenter;
    }

    public void setMapCenter(MapCenter mapCenter) {
        this.mapCenter = mapCenter;
    }

    public String getDefaultCategory() {
        return defaultCategory;
    }

    public void setDefaultCategory(String defaultCategory) {
        this.defaultCategory = defaultCategory;
    }

    public Set<String> getVisibleSites() {
        return visibleSites;
    }

    public void setVisibleSites(Set<String> visibleSites) {
        this.visibleSites = visibleSites;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Long getDefaultDuration() {
        return defaultDuration;
    }

    public void setDefaultDuration(Long defaultDuration) {
        this.defaultDuration = defaultDuration;
    }

    public List<String> getVisibleColumns() {
        return visibleColumns;
    }

    public void setVisibleColumns(List<String> visibleColumns) {
        this.visibleColumns = visibleColumns;
    }

    public Map<String, Column> getColumns() {
        return columns;
    }

    public void setColumns(Map<String, Column> columns) {
        this.columns = columns;
    }

    public String getStaticSettingsID() {
        return staticSettingsID;
    }

    public void setStaticSettingsID(String staticSettingsID) {
        this.staticSettingsID = staticSettingsID;
    }

    public Boolean getAddToSelected() {
        return addToSelected;
    }

    public Boolean getAddGeometryToSelected() {
        return addGeometryToSelected;
    }

    public void setAddToSelected(Boolean addToSelected) {
        this.addToSelected = addToSelected;
    }

    public void setAddGeometryToSelected(Boolean addGeometryToSelected) {
        this.addGeometryToSelected = addGeometryToSelected;
    }

    public Set<String> getPerspectiveIDs() {
        return perspectiveIDs;
    }

    public void setPerspectiveIDs(Set<String> perspectiveIDs) {
        this.perspectiveIDs = perspectiveIDs;
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}

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
 * Basic settings implementation.
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
 * Dec 05, 2014 4124       Chris.Golden Made implementation of new ISettings
 *                                      interface, needed to allow for proper
 *                                      use of ObservedSettings.
 * Jan 29, 2015 4375       Dan Schaffer Console initiation of RVS product generation
 * Feb 15, 2015 2271       Dan Schaffer Incur recommender/product generator init costs immediately
 * Feb 23, 2015 3618       Chris.Golden Added possible sites to settings.
 * May 18, 2015 8227       Chris.Cody   Remove NullRecommender
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
@XmlRootElement(name = "HazardServicesSettings")
@XmlAccessorType(XmlAccessType.FIELD)
public class Settings implements ISettings {

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
     * Which tools can be run
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
     * Which sites are possible for being loaded/displayed
     */
    private Set<String> possibleSites;

    /**
     * Which sites events should be loaded/displayed; must be a subset of
     * {@link #possibleSites}.
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

    public Settings(ISettings other) {
        apply(other);
    }

    /**
     * Copy all settings from another Settings object into this one.
     * 
     * @param other
     */
    @Override
    public SettingsChangeType apply(ISettings other) {
        setSettingsID(other.getSettingsID());
        setVisibleTypes(other.getVisibleTypes());
        setVisibleStatuses(other.getVisibleStatuses());
        setToolbarTools(other.getToolbarTools());
        setDefaultTimeDisplayDuration(other.getDefaultTimeDisplayDuration());
        setMapCenter(other.getMapCenter());
        setDefaultCategory(other.getDefaultCategory());
        setPossibleSites(other.getPossibleSites());
        setVisibleSites(other.getVisibleSites());
        setDisplayName(other.getDisplayName());
        setDefaultDuration(other.getDefaultDuration());
        setVisibleColumns(other.getVisibleColumns());
        setColumns(other.getColumns());
        setStaticSettingsID(other.getStaticSettingsID());
        setAddToSelected(other.getAddToSelected());
        setAddGeometryToSelected(other.getAddGeometryToSelected());
        setPerspectiveIDs(other.getPerspectiveIDs());

        return (SettingsChangeType.NO_CHANGE);
    }

    @Override
    public String getSettingsID() {
        return settingsID;
    }

    @Override
    public void setSettingsID(String settingsID) {
        this.settingsID = settingsID;
    }

    @Override
    public Set<String> getVisibleTypes() {
        return visibleTypes;
    }

    @Override
    public void setVisibleTypes(Set<String> visibleTypes) {
        this.visibleTypes = visibleTypes;
    }

    @Override
    public Set<String> getVisibleStatuses() {
        return visibleStatuses;
    }

    @Override
    public void setVisibleStatuses(Set<String> visibleStatuses) {
        this.visibleStatuses = visibleStatuses;
    }

    @Override
    public List<Tool> getToolbarTools() {
        return toolbarTools;
    }

    @Override
    public void setToolbarTools(List<Tool> toolbarTools) {
        this.toolbarTools = toolbarTools;
    }

    @Override
    public Long getDefaultTimeDisplayDuration() {
        return defaultTimeDisplayDuration;
    }

    @Override
    public void setDefaultTimeDisplayDuration(Long defaultTimeDisplayDuration) {
        this.defaultTimeDisplayDuration = defaultTimeDisplayDuration;
    }

    @Override
    public MapCenter getMapCenter() {
        return mapCenter;
    }

    @Override
    public void setMapCenter(MapCenter mapCenter) {
        this.mapCenter = mapCenter;
    }

    @Override
    public String getDefaultCategory() {
        return defaultCategory;
    }

    @Override
    public void setDefaultCategory(String defaultCategory) {
        this.defaultCategory = defaultCategory;
    }

    @Override
    public Set<String> getPossibleSites() {
        return possibleSites;
    }

    @Override
    public void setPossibleSites(Set<String> possibleSites) {
        this.possibleSites = possibleSites;
    }

    @Override
    public Set<String> getVisibleSites() {
        return visibleSites;
    }

    @Override
    public void setVisibleSites(Set<String> visibleSites) {
        this.visibleSites = visibleSites;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public Long getDefaultDuration() {
        return defaultDuration;
    }

    @Override
    public void setDefaultDuration(Long defaultDuration) {
        this.defaultDuration = defaultDuration;
    }

    @Override
    public List<String> getVisibleColumns() {
        return visibleColumns;
    }

    @Override
    public void setVisibleColumns(List<String> visibleColumns) {
        this.visibleColumns = visibleColumns;
    }

    @Override
    public Map<String, Column> getColumns() {
        return columns;
    }

    @Override
    public void setColumns(Map<String, Column> columns) {
        this.columns = columns;
    }

    @Override
    public String getStaticSettingsID() {
        return staticSettingsID;
    }

    @Override
    public void setStaticSettingsID(String staticSettingsID) {
        this.staticSettingsID = staticSettingsID;
    }

    @Override
    public Boolean getAddToSelected() {
        return addToSelected;
    }

    @Override
    public Boolean getAddGeometryToSelected() {
        return addGeometryToSelected;
    }

    @Override
    public void setAddToSelected(Boolean addToSelected) {
        this.addToSelected = addToSelected;
    }

    @Override
    public void setAddGeometryToSelected(Boolean addGeometryToSelected) {
        this.addGeometryToSelected = addGeometryToSelected;
    }

    @Override
    public Set<String> getPerspectiveIDs() {
        return perspectiveIDs;
    }

    @Override
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

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.viz.hazards.sessionmanager.config.types.ISettings#getTool
     * (java.lang.String)
     */
    @Override
    public Tool getTool(String toolName) {
        for (Tool tool : toolbarTools) {
            if (tool.getToolName().equals(toolName)) {
                return tool;
            }
        }
        return null;

    }

}

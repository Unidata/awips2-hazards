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
     * Which states of events should be loaded/displayed
     */
    private Set<String> visibleStates;

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
     * A reasonable duration for new evetns.
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
        setVisibleStates(other.getVisibleStates());
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

    public Set<String> getVisibleStates() {
        return visibleStates;
    }

    public void setVisibleStates(Set<String> visibleStates) {
        this.visibleStates = visibleStates;
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

    public void setAddToSelected(Boolean addToSelected) {
        this.addToSelected = addToSelected;
    }

    public Set<String> getPerspectiveIDs() {
        return perspectiveIDs;
    }

    public void setPerspectiveIDs(Set<String> perspectiveIDs) {
        this.perspectiveIDs = perspectiveIDs;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((addToSelected == null) ? 0 : addToSelected.hashCode());
        result = prime * result + ((columns == null) ? 0 : columns.hashCode());
        result = prime * result
                + ((defaultCategory == null) ? 0 : defaultCategory.hashCode());
        result = prime * result
                + ((defaultDuration == null) ? 0 : defaultDuration.hashCode());
        result = prime
                * result
                + ((defaultTimeDisplayDuration == null) ? 0
                        : defaultTimeDisplayDuration.hashCode());
        result = prime * result
                + ((displayName == null) ? 0 : displayName.hashCode());
        result = prime * result
                + ((mapCenter == null) ? 0 : mapCenter.hashCode());
        result = prime * result
                + ((settingsID == null) ? 0 : settingsID.hashCode());
        result = prime
                * result
                + ((staticSettingsID == null) ? 0 : staticSettingsID.hashCode());
        result = prime * result
                + ((toolbarTools == null) ? 0 : toolbarTools.hashCode());
        result = prime * result
                + ((visibleColumns == null) ? 0 : visibleColumns.hashCode());
        result = prime * result
                + ((visibleSites == null) ? 0 : visibleSites.hashCode());
        result = prime * result
                + ((visibleStates == null) ? 0 : visibleStates.hashCode());
        result = prime * result
                + ((visibleTypes == null) ? 0 : visibleTypes.hashCode());
        result = prime * result
                + ((perspectiveIDs == null) ? 0 : perspectiveIDs.hashCode());

        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Settings other = (Settings) obj;
        if (addToSelected == null) {
            if (other.addToSelected != null) {
                return false;
            }
        } else if (!addToSelected.equals(other.addToSelected)) {
            return false;
        }
        if (columns == null) {
            if (other.columns != null) {
                return false;
            }
        } else if (!columns.equals(other.columns)) {
            return false;
        }
        if (defaultCategory == null) {
            if (other.defaultCategory != null) {
                return false;
            }
        } else if (!defaultCategory.equals(other.defaultCategory)) {
            return false;
        }
        if (defaultDuration == null) {
            if (other.defaultDuration != null) {
                return false;
            }
        } else if (!defaultDuration.equals(other.defaultDuration)) {
            return false;
        }
        if (defaultTimeDisplayDuration == null) {
            if (other.defaultTimeDisplayDuration != null) {
                return false;
            }
        } else if (!defaultTimeDisplayDuration
                .equals(other.defaultTimeDisplayDuration)) {
            return false;
        }
        if (displayName == null) {
            if (other.displayName != null) {
                return false;
            }
        } else if (!displayName.equals(other.displayName)) {
            return false;
        }
        if (mapCenter == null) {
            if (other.mapCenter != null) {
                return false;
            }
        } else if (!mapCenter.equals(other.mapCenter)) {
            return false;
        }
        if (settingsID == null) {
            if (other.settingsID != null) {
                return false;
            }
        } else if (!settingsID.equals(other.settingsID)) {
            return false;
        }
        if (staticSettingsID == null) {
            if (other.staticSettingsID != null) {
                return false;
            }
        } else if (!staticSettingsID.equals(other.staticSettingsID)) {
            return false;
        }
        if (toolbarTools == null) {
            if (other.toolbarTools != null) {
                return false;
            }
        } else if (!toolbarTools.equals(other.toolbarTools)) {
            return false;
        }
        if (visibleColumns == null) {
            if (other.visibleColumns != null) {
                return false;
            }
        } else if (!visibleColumns.equals(other.visibleColumns)) {
            return false;
        }
        if (visibleSites == null) {
            if (other.visibleSites != null) {
                return false;
            }
        } else if (!visibleSites.equals(other.visibleSites)) {
            return false;
        }
        if (visibleStates == null) {
            if (other.visibleStates != null) {
                return false;
            }
        } else if (!visibleStates.equals(other.visibleStates)) {
            return false;
        }
        if (visibleTypes == null) {
            if (other.visibleTypes != null) {
                return false;
            }
        } else if (!visibleTypes.equals(other.visibleTypes)) {
            return false;
        }

        if (perspectiveIDs == null) {
            if (other.perspectiveIDs != null) {
                return false;
            }

        } else if (!perspectiveIDs.equals(other.perspectiveIDs)) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}

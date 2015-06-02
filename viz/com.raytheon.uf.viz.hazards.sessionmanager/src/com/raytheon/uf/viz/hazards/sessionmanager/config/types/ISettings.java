/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package com.raytheon.uf.viz.hazards.sessionmanager.config.types;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Description: Interface describing a settings object, used to hold display
 * configuration information.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Dec 05, 2014    4124    Chris.Golden Initial creation.
 * Jan 29, 2015    4375    Dan Schaffer Console initiation of RVS product generation
 * Feb 23, 2015    3618    Chris.Golden Added possible sites to settings.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public interface ISettings {

    /**
     * Copy all settings from another Settings object into this one.
     * 
     * @param other
     */
    public void apply(ISettings other);

    public String getSettingsID();

    public void setSettingsID(String settingsID);

    public Set<String> getVisibleTypes();

    public void setVisibleTypes(Set<String> visibleTypes);

    public Set<String> getVisibleStatuses();

    public void setVisibleStatuses(Set<String> visibleStatuses);

    public List<Tool> getToolbarTools();

    public void setToolbarTools(List<Tool> toolbarTools);

    public Tool getTool(String toolName);

    public Long getDefaultTimeDisplayDuration();

    public void setDefaultTimeDisplayDuration(Long defaultTimeDisplayDuration);

    public MapCenter getMapCenter();

    public void setMapCenter(MapCenter mapCenter);

    public String getDefaultCategory();

    public void setDefaultCategory(String defaultCategory);

    public Set<String> getPossibleSites();

    public void setPossibleSites(Set<String> possibleSites);

    public Set<String> getVisibleSites();

    public void setVisibleSites(Set<String> visibleSites);

    public String getDisplayName();

    public void setDisplayName(String displayName);

    public Long getDefaultDuration();

    public void setDefaultDuration(Long defaultDuration);

    public List<String> getVisibleColumns();

    public void setVisibleColumns(List<String> visibleColumns);

    public Map<String, Column> getColumns();

    public void setColumns(Map<String, Column> columns);

    public String getStaticSettingsID();

    public void setStaticSettingsID(String staticSettingsID);

    public Boolean getAddToSelected();

    public Boolean getAddGeometryToSelected();

    public void setAddToSelected(Boolean addToSelected);

    public void setAddGeometryToSelected(Boolean addGeometryToSelected);

    public Set<String> getPerspectiveIDs();

    public void setPerspectiveIDs(Set<String> perspectiveIDs);

}
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

import gov.noaa.gsd.common.utilities.TimeResolution;

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
 * Aug 03, 2015    8836    Chris.Cody   Changes for a configurable Event Id
 * May 10, 2016   18515    Chris.Golden Added "deselect after issuing" flag.
 * Oct 19, 2016   21873    Chris.Golden Added time resolution.
 * Oct 23, 2017   21730    Chris.Golden Added defaultType.
 * Jan 17, 2018   33428    Chris.Golden Removed no-longer-needed flag indicating
 *                                      whether a new geometry should be added to
 *                                      a selected event's geometry.
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

    public TimeResolution getTimeResolution();

    public void setTimeResolution(TimeResolution timeResolution);

    public MapCenter getMapCenter();

    public void setMapCenter(MapCenter mapCenter);

    public String getDefaultCategory();

    public void setDefaultCategory(String defaultCategory);

    public String getDefaultType();

    public void setDefaultType(String defaultType);

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

    public void setAddToSelected(Boolean addToSelected);

    public Set<String> getPerspectiveIDs();

    public void setPerspectiveIDs(Set<String> perspectiveIDs);

    public String getEventIdDisplayType();

    public void setEventIdDisplayType(String eventIdDisplayType);

    public Boolean getDeselectAfterIssuing();

    public void setDeselectAfterIssuing(Boolean deselectAfterIssuing);
}
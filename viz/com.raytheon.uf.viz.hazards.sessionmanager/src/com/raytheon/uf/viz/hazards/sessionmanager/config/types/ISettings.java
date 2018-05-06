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

import gov.noaa.gsd.common.utilities.DragAndDropGeometryEditSource;
import gov.noaa.gsd.common.utilities.TimeResolution;
import gov.noaa.gsd.viz.megawidgets.CheckListMegawidget;

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
 * Jan 22, 2018   25765    Chris.Golden Added "priority for drag-and-drop geometry
 *                                      edit" flag to make geometry editing from the
 *                                      spatial display more flexible.
 * May 04, 2018   50032    Chris.Golden Added "additionalFilters" and
 *                                      "visibleAdditionalFilters" properties.
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

    /**
     * Get additional filters, if any. These are filters to be applied in
     * addition to the usual hazard type, site, and status filters. If they
     * exist for the settings, they must be a list of one or more dictionaries,
     * each of the latter providing a custom filter. Within the dictionary, the
     * following values must be present:
     * <dl>
     * <dt><code>fieldName</code></dt>
     * <dd>Name of the filter; this must be the same as the name of a hazard
     * event generic attribute in order for the filter to have an effect.</dd>
     * <dt><code>label</code></dt>
     * <dd>Label to be displayed above the list of possible values for the
     * filter in the UI.</dd>
     * <dt><code>choices</code></dt>
     * <dd>List of all possible value that the attribute named in
     * <code>fieldName</code> may hold within the hazard events to be filtered.
     * Each element in the list may be specified in the same manner as those in
     * a <code>choices</code> list for a {@link CheckListMegawidget} may be
     * specified.</dd>
     * <dt><i><code>columnName</code></i></dt>
     * <dd>Optional parameter; if given, names the column within the defined
     * columns for this settings that is used to display the values of this same
     * attribute for the hazard events. There is no need to specify this if no
     * matching column is to be provided.</dd>
     * </dl>
     * 
     * @return List of additional filters, or <code>null</code> if none are to
     *         be provided.
     */
    public List<Object> getAdditionalFilters();

    public void setAdditionalFilters(List<Object> additionalFilters);

    /**
     * Get visible additional filters, if any; these are the initial values for
     * the filters provided by {@link #getAdditionalFilters()}. (Note that this
     * method's return value is meaningless if there are no such additional
     * filters.) If provided, these must be a dictionary holding a key-value
     * pairing for each filter dictionary provided by
     * <code>getAdditionalFilters()</code>, with each entry's key being the same
     * as the latter's <code>fieldName</code>, and the value being a list of
     * zero or more elements that are found within the list given as the value
     * for the latter's <code>choices</code> field.
     * 
     * @return Dictionary of starting values for additional filters, or
     *         <code>null</code> if none are to be provided.
     */
    public Map<String, Object> getVisibleAdditionalFilters();

    public void setVisibleAdditionalFilters(
            Map<String, Object> visibleAdditionalFilters);

    public DragAndDropGeometryEditSource getPriorityForDragAndDropGeometryEdits();

    public void setPriorityForDragAndDropGeometryEdits(
            DragAndDropGeometryEditSource priorityForDragAndDropGeometryEdits);

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
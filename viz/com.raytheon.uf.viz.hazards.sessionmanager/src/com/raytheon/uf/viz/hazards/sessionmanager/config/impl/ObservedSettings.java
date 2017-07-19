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
package com.raytheon.uf.viz.hazards.sessionmanager.config.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.eclipse.core.runtime.Assert;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.raytheon.uf.common.dataplugin.events.hazards.event.BaseHazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardEventUtilities;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.config.SettingsLoaded;
import com.raytheon.uf.viz.hazards.sessionmanager.config.SettingsModified;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.types.HazardCategoryAndTypes;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Column;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.ISettings;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.MapCenter;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Settings;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Tool;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.IOriginator;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.Originator;

import gov.noaa.gsd.common.utilities.TimeResolution;

/**
 * Settings object that notified the SessionConfigurationManager whenever a
 * field is set.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 13, 2013 1257       bsteffen    Initial creation
 * Nov 29, 2013 2380       daniel.s.schaffer@noaa.gov Fixing bugs in
 *                                     settings-based filtering
 * Mar 19, 2014 2925       Chris.Golden Added notification firing for
 *                                      methods that were missing it.
 * Dec 05, 2014 4124       Chris.Golden Thoroughly revamped to make a
 *                                      decorator of Settings, and to
 *                                      implement ISettings rather than
 *                                      extend Settings. This was necessary
 *                                      to provide similar notification
 *                                      functionality to that provided by
 *                                      ObservedHazardEvent.
 * Jan 29, 2015 4375       Dan Schaffer Console initiation of RVS product generation
 * Feb 23, 2015 3618       Chris.Golden Added possible sites to settings,
 *                                      and changed setting of identifier to
 *                                      trigger a settings-loaded notification
 *                                      instead of a settings-modified one.
 * Apr 10, 2015 6898       Chris.Cody   Removed all messaging from data object.
 * May 28, 2015 8401       Chris.Cody   Correct Hazard Filtering issue
 * Aug 03, 2015 8836       Chris.Cody   Changes for a configurable Event Id
 * May 10, 2016 18515      Chris.Golden Added "deselect after issuing" flag.
 * Oct 19, 2016 21873      Chris.Golden Added time resolution.
 * Feb 01, 2017 15556      Chris.Golden Changed to include set of changed settings
 *                                      elements when notifying of changes.
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */

@XmlRootElement(name = "HazardServicesSettings")
@XmlAccessorType(XmlAccessType.FIELD)
public class ObservedSettings implements ISettings {

    /**
     * Types of changes that may be made to a settings object.
     */
    public enum Type {
        IDENTIFIER, FILTERS, TOOLS, DEFAULT_DISPLAY_DURATION, TIME_RESOLUTION, MAP_CENTER, DEFAULT_CATEGORY, POSSIBLE_SITES, DISPLAY_NAME, DEFAULT_EVENT_DURATION, VISIBLE_COLUMNS, COLUMN_DEFINITIONS, STATIC_IDENTIFIER, ADD_TO_SELECTED, ADD_GEOMETRY_TO_SELECTED, EVENT_IDENTIFIER_DISPLAY_TYPE, PERSPECTIVE_IDENTIFIERS, DESELECT_AFTER_ISSUING
    };

    private SessionConfigurationManager configManager;

    private Settings delegate;

    /**
     * For JAXB serialization.
     */
    @SuppressWarnings("unused")
    private ObservedSettings() {

    }

    public ObservedSettings(SessionConfigurationManager configManager,
            ISettings other) {
        delegate = new Settings(other);
        Assert.isNotNull(configManager);
        setStaticSettingsID(getSettingsID());
        this.configManager = configManager;
    }

    private final boolean changed(Object newObj, Object oldObj) {
        if (newObj == null) {
            if (oldObj == null) {
                return false;
            }
        } else if (newObj.equals(oldObj)) {
            return false;
        } else if (newObj instanceof Object[] && oldObj instanceof Object[]) {
            return !Arrays.equals((Object[]) newObj, (Object[]) oldObj);
        }
        return true;
    }

    private <E> Set<E> getSetCopy(Set<E> original) {
        return (original == null ? null : new HashSet<E>(original));
    }

    private <E> List<E> getListCopy(List<E> original) {
        return (original == null ? null : new ArrayList<E>(original));
    }

    private List<Tool> getToolbarToolsCopy(List<Tool> original) {
        if (original == null) {
            return null;
        }
        List<Tool> copy = new ArrayList<>(original.size());
        for (Tool tool : original) {
            copy.add(new Tool(tool));
        }
        return copy;
    }

    private MapCenter getMapCenterCopy(MapCenter original) {
        return (original == null ? null
                : new MapCenter(original.getLon(), original.getLat(),
                        original.getZoom()));
    }

    private Map<String, Column> getColumnsCopy(Map<String, Column> original) {
        if (original == null) {
            return null;
        }
        Map<String, Column> copy = new HashMap<>(original.size());
        for (Map.Entry<String, Column> entry : original.entrySet()) {
            copy.put(entry.getKey(), new Column(entry.getValue()));
        }
        return copy;
    }

    private void settingsChanged(boolean notify, Type changed,
            IOriginator originator) {
        if (notify) {
            settingsChanged(
                    new SettingsModified(configManager, changed, originator));
        }
    }

    private void settingsChanged(boolean notify, Set<Type> changed,
            IOriginator originator) {
        if (notify) {
            settingsChanged(
                    new SettingsModified(configManager, changed, originator));
        }
    }

    private void settingsChangedIdentifier(boolean notify,
            IOriginator originator) {
        if (notify) {
            settingsChanged(new SettingsLoaded(configManager, originator));
        }
    }

    private void settingsChanged(SettingsModified notification) {

        /*
         * The configManager is only null during construction, we don't want any
         * notifications during construction anyway.
         */
        if (configManager != null) {
            configManager.settingsChanged(notification);
        }
    }

    @Override
    public String getSettingsID() {
        return delegate.getSettingsID();
    }

    @Override
    public Set<String> getVisibleTypes() {
        return getSetCopy(delegate.getVisibleTypes());
    }

    @Override
    public Set<String> getVisibleStatuses() {
        return getSetCopy(delegate.getVisibleStatuses());
    }

    @Override
    public List<Tool> getToolbarTools() {
        return getToolbarToolsCopy(delegate.getToolbarTools());
    }

    @Override
    public Long getDefaultTimeDisplayDuration() {
        return delegate.getDefaultTimeDisplayDuration();
    }

    @Override
    public MapCenter getMapCenter() {
        return getMapCenterCopy(delegate.getMapCenter());
    }

    @Override
    public String getDefaultCategory() {
        return delegate.getDefaultCategory();
    }

    @Override
    public Set<String> getPossibleSites() {
        return getSetCopy(delegate.getPossibleSites());
    }

    @Override
    public Set<String> getVisibleSites() {
        return getSetCopy(delegate.getVisibleSites());
    }

    @Override
    public String getDisplayName() {
        return delegate.getDisplayName();
    }

    @Override
    public Long getDefaultDuration() {
        return delegate.getDefaultDuration();
    }

    @Override
    public TimeResolution getTimeResolution() {
        return delegate.getTimeResolution();
    }

    @Override
    public List<String> getVisibleColumns() {
        return getListCopy(delegate.getVisibleColumns());
    }

    @Override
    public Map<String, Column> getColumns() {
        return getColumnsCopy(delegate.getColumns());
    }

    @Override
    public String getStaticSettingsID() {
        return delegate.getStaticSettingsID();
    }

    @Override
    public Boolean getAddToSelected() {
        return delegate.getAddToSelected();
    }

    @Override
    public Boolean getAddGeometryToSelected() {
        return delegate.getAddGeometryToSelected();
    }

    @Override
    public Set<String> getPerspectiveIDs() {
        return getSetCopy(delegate.getPerspectiveIDs());
    }

    @Override
    public String getEventIdDisplayType() {
        return delegate.getEventIdDisplayType();
    }

    @Override
    public Boolean getDeselectAfterIssuing() {
        return delegate.getDeselectAfterIssuing();
    }

    @Override
    public Tool getTool(String toolName) {
        return delegate.getTool(toolName);
    }

    @Override
    public void apply(ISettings other) {
        apply(other, Originator.OTHER);
    }

    @Override
    public void setSettingsID(String settingsID) {
        setSettingsID(settingsID, true, Originator.OTHER);
    }

    @Override
    public void setVisibleTypes(Set<String> visibleTypes) {
        setVisibleTypes(visibleTypes, true, Originator.OTHER);
    }

    @Override
    public void setVisibleStatuses(Set<String> visibleStatuses) {
        setVisibleStatuses(visibleStatuses, true, Originator.OTHER);
    }

    @Override
    public void setToolbarTools(List<Tool> toolbarTools) {
        setToolbarTools(toolbarTools, true, Originator.OTHER);
    }

    @Override
    public void setDefaultTimeDisplayDuration(Long defaultTimeDisplayDuration) {
        setDefaultTimeDisplayDuration(defaultTimeDisplayDuration, true,
                Originator.OTHER);
    }

    @Override
    public void setTimeResolution(TimeResolution timeResolution) {
        setTimeResolution(timeResolution, true, Originator.OTHER);
    }

    @Override
    public void setMapCenter(MapCenter mapCenter) {
        setMapCenter(mapCenter, true, Originator.OTHER);
    }

    @Override
    public void setDefaultCategory(String defaultCategory) {
        setDefaultCategory(defaultCategory, true, Originator.OTHER);
    }

    @Override
    public void setPossibleSites(Set<String> possibleSites) {
        setPossibleSites(possibleSites, true, Originator.OTHER);
    }

    @Override
    public void setVisibleSites(Set<String> visibleSites) {
        setVisibleSites(visibleSites, true, Originator.OTHER);
    }

    @Override
    public void setDisplayName(String displayName) {
        setDisplayName(displayName, true, Originator.OTHER);
    }

    @Override
    public void setDefaultDuration(Long defaultDuration) {
        setDefaultDuration(defaultDuration, true, Originator.OTHER);
    }

    @Override
    public void setVisibleColumns(List<String> visibleColumns) {
        setVisibleColumns(visibleColumns, true, Originator.OTHER);
    }

    @Override
    public void setColumns(Map<String, Column> columns) {
        setColumns(columns, true, Originator.OTHER);
    }

    @Override
    public void setStaticSettingsID(String staticSettingsID) {
        setStaticSettingsID(staticSettingsID, true, Originator.OTHER);
    }

    @Override
    public void setAddToSelected(Boolean addToSelected) {
        setAddToSelected(addToSelected, true, Originator.OTHER);
    }

    @Override
    public void setAddGeometryToSelected(Boolean addGeometryToSelected) {
        setAddGeometryToSelected(addGeometryToSelected, true, Originator.OTHER);
    }

    @Override
    public void setEventIdDisplayType(String eventIdDisplayType) {
        setEventIdDisplayType(eventIdDisplayType, true, Originator.OTHER);
    }

    @Override
    public void setPerspectiveIDs(Set<String> perspectiveIDs) {
        setPerspectiveIDs(perspectiveIDs, true, Originator.OTHER);
    }

    @Override
    public void setDeselectAfterIssuing(Boolean deselectAfterIssuing) {
        setDeselectAfterIssuing(deselectAfterIssuing, true, Originator.OTHER);
    }

    /**
     * Copy all settings from another Settings object into this one.
     * 
     * @param other
     * @param originator
     */
    public void apply(ISettings other, IOriginator originator) {
        Set<Type> changed = EnumSet.noneOf(Type.class);
        changed.addAll(setSettingsID(other.getSettingsID(), false, originator));
        changed.addAll(
                setVisibleTypes(other.getVisibleTypes(), false, originator));
        changed.addAll(setVisibleStatuses(other.getVisibleStatuses(), false,
                originator));
        changed.addAll(
                setToolbarTools(other.getToolbarTools(), false, originator));
        changed.addAll(setDefaultTimeDisplayDuration(
                other.getDefaultTimeDisplayDuration(), false, originator));
        changed.addAll(setTimeResolution(other.getTimeResolution(), false,
                originator));
        changed.addAll(setMapCenter(other.getMapCenter(), false, originator));
        changed.addAll(setDefaultCategory(other.getDefaultCategory(), false,
                originator));
        changed.addAll(
                setPossibleSites(other.getPossibleSites(), false, originator));
        changed.addAll(
                setVisibleSites(other.getVisibleSites(), false, originator));
        changed.addAll(
                setDisplayName(other.getDisplayName(), false, originator));
        changed.addAll(setDefaultDuration(other.getDefaultDuration(), false,
                originator));
        changed.addAll(setVisibleColumns(other.getVisibleColumns(), false,
                originator));
        changed.addAll(setColumns(other.getColumns(), false, originator));
        changed.addAll(setStaticSettingsID(other.getStaticSettingsID(), false,
                originator));
        changed.addAll(
                setAddToSelected(other.getAddToSelected(), false, originator));
        changed.addAll(setAddGeometryToSelected(
                other.getAddGeometryToSelected(), false, originator));
        changed.addAll(setEventIdDisplayType(other.getEventIdDisplayType(),
                false, originator));
        changed.addAll(setPerspectiveIDs(other.getPerspectiveIDs(), false,
                originator));
        changed.addAll(setDeselectAfterIssuing(other.getDeselectAfterIssuing(),
                false, originator));
        if (changed.contains(Type.IDENTIFIER)) {
            settingsChangedIdentifier(true, originator);
        } else if (changed.isEmpty() == false) {
            settingsChanged(true, changed, originator);
        }
    }

    /**
     * Apply any changes to the persisted settings. Any changes to persisted
     * settings that have not been changed in this will be applied from update.
     * Local changes to this settings object will not reflect changes in
     * updates.
     * 
     * For example: If a user has two caves open with hazard services with the
     * same settings. In caveA the user adds the site ID column, if the user
     * saves then the settings change will be applied to the settings in caveB.
     * Now if caveA removes the site ID column and caveB adds the endTime
     * column, then when cave A saves this will not be directly applied to caveB
     * because it would overwrite caveBs local settings.
     * 
     * @param persisted
     *            the previously persisted settings, from which this settings
     *            object is based.
     * @param update
     *            the new persisted settings that have changed in localization.
     */
    public void applyPersistedChanges(ISettings persisted, ISettings update) {
        Set<Type> changed = EnumSet.noneOf(Type.class);
        if (changed(getSettingsID(), persisted.getSettingsID()) == false) {
            changed.addAll(setSettingsID(update.getSettingsID(), false, null));
        }
        if (changed(getVisibleTypes(), persisted.getVisibleTypes()) == false) {
            changed.addAll(
                    setVisibleTypes(update.getVisibleTypes(), false, null));
        }
        if (changed(getVisibleStatuses(),
                persisted.getVisibleStatuses()) == false) {
            changed.addAll(setVisibleStatuses(update.getVisibleStatuses(),
                    false, null));
        }
        if (changed(getToolbarTools(), persisted.getToolbarTools()) == false) {
            changed.addAll(
                    setToolbarTools(update.getToolbarTools(), false, null));
        }
        if (changed(getDefaultTimeDisplayDuration(),
                persisted.getDefaultTimeDisplayDuration()) == false) {
            changed.addAll(setDefaultTimeDisplayDuration(
                    update.getDefaultTimeDisplayDuration(), false, null));
        }
        if (changed(getTimeResolution(),
                persisted.getTimeResolution()) == false) {
            changed.addAll(
                    setTimeResolution(update.getTimeResolution(), false, null));
        }
        if (changed(getMapCenter(), persisted.getMapCenter()) == false) {
            changed.addAll(setMapCenter(update.getMapCenter(), false, null));
        }
        if (changed(getDefaultCategory(),
                persisted.getDefaultCategory()) == false) {
            changed.addAll(setDefaultCategory(update.getDefaultCategory(),
                    false, null));
        }
        if (changed(getPossibleSites(),
                persisted.getPossibleSites()) == false) {
            changed.addAll(
                    setPossibleSites(update.getPossibleSites(), false, null));
        }
        if (changed(getVisibleSites(), persisted.getVisibleSites()) == false) {
            changed.addAll(
                    setVisibleSites(update.getVisibleSites(), false, null));
        }
        if (changed(getDisplayName(), persisted.getDisplayName()) == false) {
            changed.addAll(
                    setDisplayName(update.getDisplayName(), false, null));
        }
        if (changed(getDefaultDuration(),
                persisted.getDefaultDuration()) == false) {
            changed.addAll(setDefaultDuration(update.getDefaultDuration(),
                    false, null));
        }
        if (changed(getVisibleColumns(),
                persisted.getVisibleColumns()) == false) {
            changed.addAll(
                    setVisibleColumns(update.getVisibleColumns(), false, null));
        }
        if (changed(getColumns(), persisted.getColumns()) == false) {
            changed.addAll(setColumns(update.getColumns(), false, null));
        }
        if (changed(getStaticSettingsID(),
                persisted.getStaticSettingsID()) == false) {
            changed.addAll(setStaticSettingsID(update.getStaticSettingsID(),
                    false, null));
        }
        if (changed(getAddToSelected(),
                persisted.getAddToSelected()) == false) {
            changed.addAll(
                    setAddToSelected(update.getAddToSelected(), false, null));
        }
        if (changed(getAddGeometryToSelected(),
                persisted.getAddGeometryToSelected()) == false) {
            changed.addAll(setAddGeometryToSelected(
                    update.getAddGeometryToSelected(), false, null));
        }
        if (changed(getEventIdDisplayType(),
                persisted.getEventIdDisplayType()) == false) {
            changed.addAll(setEventIdDisplayType(update.getEventIdDisplayType(),
                    false, null));
        }
        if (changed(getPerspectiveIDs(),
                persisted.getPerspectiveIDs()) == false) {
            changed.addAll(
                    setPerspectiveIDs(update.getPerspectiveIDs(), false, null));
        }
        if (changed(getDeselectAfterIssuing(),
                persisted.getDeselectAfterIssuing()) == false) {
            changed.addAll(setDeselectAfterIssuing(
                    update.getDeselectAfterIssuing(), false, null));
        }

        if (changed.isEmpty() == false) {
            if (changed.contains(Type.IDENTIFIER)) {
                settingsChangedIdentifier(true, Originator.OTHER);
            } else {
                settingsChanged(true, changed, Originator.OTHER);
            }
        }
    }

    public void setSettingsID(String settingsID, IOriginator originator) {
        setSettingsID(settingsID, true, originator);
    }

    public void setVisibleTypes(Set<String> visibleTypes,
            IOriginator originator) {
        setVisibleTypes(visibleTypes, true, originator);
    }

    public void setVisibleStatuses(Set<String> visibleStatuses,
            IOriginator originator) {
        setVisibleStatuses(visibleStatuses, true, originator);
    }

    public void setToolbarTools(List<Tool> toolbarTools,
            IOriginator originator) {
        setToolbarTools(toolbarTools, true, originator);
    }

    public void setDefaultTimeDisplayDuration(Long defaultTimeDisplayDuration,
            IOriginator originator) {
        setDefaultTimeDisplayDuration(defaultTimeDisplayDuration, true,
                originator);
    }

    public void setTimeResolution(TimeResolution timeResolution,
            IOriginator originator) {
        setTimeResolution(timeResolution, true, originator);
    }

    public void setMapCenter(MapCenter mapCenter, IOriginator originator) {
        setMapCenter(mapCenter, true, originator);
    }

    public void setDefaultCategory(String defaultCategory,
            IOriginator originator) {
        setDefaultCategory(defaultCategory, true, originator);
    }

    public void setPossibleSites(Set<String> possibleSites,
            IOriginator originator) {
        setPossibleSites(possibleSites, true, originator);
    }

    public void setVisibleSites(Set<String> visibleSites,
            IOriginator originator) {
        setVisibleSites(visibleSites, true, originator);
    }

    public void setDisplayName(String displayName, IOriginator originator) {
        setDisplayName(displayName, true, originator);
    }

    public void setDefaultDuration(Long defaultDuration,
            IOriginator originator) {
        setDefaultDuration(defaultDuration, true, originator);
    }

    public void setVisibleColumns(List<String> visibleColumns,
            IOriginator originator) {
        setVisibleColumns(visibleColumns, true, originator);
    }

    public void setColumns(Map<String, Column> columns,
            IOriginator originator) {
        setColumns(columns, true, originator);
    }

    public void setStaticSettingsID(String staticSettingsID,
            IOriginator originator) {
        setStaticSettingsID(staticSettingsID, true, originator);
    }

    public void setAddToSelected(Boolean addToSelected,
            IOriginator originator) {
        setAddToSelected(addToSelected, true, originator);
    }

    public void setAddGeometryToSelected(Boolean addGeometryToSelected,
            IOriginator originator) {
        setAddGeometryToSelected(addGeometryToSelected, true, originator);
    }

    public void setEventIdDisplayType(String eventIdDisplayType,
            IOriginator originator) {
        setEventIdDisplayType(eventIdDisplayType, true, originator);
    }

    public void setPerspectiveIDs(Set<String> perspectiveIDs,
            IOriginator originator) {
        setPerspectiveIDs(perspectiveIDs, true, originator);
    }

    public void setDeselectAfterIssuing(Boolean deselectAfterIssuing,
            IOriginator originator) {
        setDeselectAfterIssuing(deselectAfterIssuing, true, originator);
    }

    protected Set<Type> setSettingsID(String settingsID, boolean notify,
            IOriginator originator) {
        if (changed(settingsID, getSettingsID())) {
            delegate.setSettingsID(settingsID);
            settingsChangedIdentifier(notify, originator);
            return EnumSet.of(Type.IDENTIFIER);
        }
        return EnumSet.noneOf(Type.class);
    }

    protected Set<Type> setVisibleTypes(Set<String> visibleTypes,
            boolean notify, IOriginator originator) {
        if (changed(visibleTypes, getVisibleTypes())) {
            delegate.setVisibleTypes(getSetCopy(visibleTypes));
            settingsChanged(notify, Type.FILTERS, originator);
            return EnumSet.of(Type.FILTERS);
        }
        return EnumSet.noneOf(Type.class);
    }

    protected Set<Type> setVisibleStatuses(Set<String> visibleStatuses,
            boolean notify, IOriginator originator) {
        if (changed(visibleStatuses, getVisibleStatuses())) {
            delegate.setVisibleStatuses(getSetCopy(visibleStatuses));
            settingsChanged(notify, Type.FILTERS, originator);
            return EnumSet.of(Type.FILTERS);
        }
        return EnumSet.noneOf(Type.class);
    }

    protected Set<Type> setToolbarTools(List<Tool> toolbarTools, boolean notify,
            IOriginator originator) {
        if (changed(toolbarTools, getToolbarTools())) {
            delegate.setToolbarTools(getToolbarToolsCopy(toolbarTools));
            settingsChanged(notify, Type.TOOLS, originator);
            return EnumSet.of(Type.TOOLS);
        }
        return EnumSet.noneOf(Type.class);
    }

    protected Set<Type> setDefaultTimeDisplayDuration(
            Long defaultTimeDisplayDuration, boolean notify,
            IOriginator originator) {
        if (changed(defaultTimeDisplayDuration,
                getDefaultTimeDisplayDuration())) {
            delegate.setDefaultTimeDisplayDuration(defaultTimeDisplayDuration);
            settingsChanged(notify, Type.DEFAULT_DISPLAY_DURATION, originator);
            return EnumSet.of(Type.DEFAULT_DISPLAY_DURATION);
        }
        return EnumSet.noneOf(Type.class);
    }

    protected Set<Type> setTimeResolution(TimeResolution timeResolution,
            boolean notify, IOriginator originator) {
        if (changed(timeResolution, getTimeResolution())) {
            delegate.setTimeResolution(timeResolution);
            settingsChanged(notify, Type.TIME_RESOLUTION, originator);
            return EnumSet.of(Type.TIME_RESOLUTION);
        }
        return EnumSet.noneOf(Type.class);
    }

    protected Set<Type> setMapCenter(MapCenter mapCenter, boolean notify,
            IOriginator originator) {
        if (changed(mapCenter, getMapCenter())) {
            delegate.setMapCenter(getMapCenterCopy(mapCenter));
            settingsChanged(notify, Type.MAP_CENTER, originator);
            return EnumSet.of(Type.MAP_CENTER);
        }
        return EnumSet.noneOf(Type.class);
    }

    protected Set<Type> setDefaultCategory(String defaultCategory,
            boolean notify, IOriginator originator) {
        if (changed(defaultCategory, getDefaultCategory())) {
            delegate.setDefaultCategory(defaultCategory);
            settingsChanged(notify, Type.DEFAULT_CATEGORY, originator);
            return EnumSet.of(Type.DEFAULT_CATEGORY);
        }
        return EnumSet.noneOf(Type.class);
    }

    protected Set<Type> setPossibleSites(Set<String> possibleSites,
            boolean notify, IOriginator originator) {
        if (changed(possibleSites, getPossibleSites())) {
            delegate.setPossibleSites(getSetCopy(possibleSites));
            settingsChanged(notify, Type.POSSIBLE_SITES, originator);
            return EnumSet.of(Type.POSSIBLE_SITES);
        }
        return EnumSet.noneOf(Type.class);
    }

    protected Set<Type> setVisibleSites(Set<String> visibleSites,
            boolean notify, IOriginator originator) {
        if (changed(visibleSites, getVisibleSites())) {
            delegate.setVisibleSites(getSetCopy(visibleSites));
            settingsChanged(notify, Type.FILTERS, originator);
            return EnumSet.of(Type.FILTERS);
        }
        return EnumSet.noneOf(Type.class);
    }

    protected Set<Type> setDisplayName(String displayName, boolean notify,
            IOriginator originator) {
        if (changed(displayName, getDisplayName())) {
            delegate.setDisplayName(displayName);
            settingsChanged(notify, Type.DISPLAY_NAME, originator);
            return EnumSet.of(Type.DISPLAY_NAME);
        }
        return EnumSet.noneOf(Type.class);
    }

    protected Set<Type> setDefaultDuration(Long defaultDuration, boolean notify,
            IOriginator originator) {
        if (changed(defaultDuration, getDefaultDuration())) {
            delegate.setDefaultDuration(defaultDuration);
            settingsChanged(notify, Type.DEFAULT_EVENT_DURATION, originator);
            return EnumSet.of(Type.DEFAULT_EVENT_DURATION);
        }
        return EnumSet.noneOf(Type.class);
    }

    protected Set<Type> setVisibleColumns(List<String> visibleColumns,
            boolean notify, IOriginator originator) {
        if (changed(visibleColumns, getVisibleColumns())) {
            delegate.setVisibleColumns(getListCopy(visibleColumns));
            settingsChanged(notify, Type.VISIBLE_COLUMNS, originator);
            return EnumSet.of(Type.VISIBLE_COLUMNS);
        }
        return EnumSet.noneOf(Type.class);
    }

    protected Set<Type> setColumns(Map<String, Column> columns, boolean notify,
            IOriginator originator) {
        if (changed(columns, getColumns())) {
            delegate.setColumns(getColumnsCopy(columns));
            settingsChanged(notify, Type.COLUMN_DEFINITIONS, originator);
            return EnumSet.of(Type.COLUMN_DEFINITIONS);
        }
        return EnumSet.noneOf(Type.class);
    }

    protected Set<Type> setStaticSettingsID(String staticSettingsID,
            boolean notify, IOriginator originator) {
        if (changed(staticSettingsID, getStaticSettingsID())) {
            delegate.setStaticSettingsID(staticSettingsID);
            settingsChanged(notify, Type.STATIC_IDENTIFIER, originator);
            return EnumSet.of(Type.STATIC_IDENTIFIER);
        }
        return EnumSet.noneOf(Type.class);
    }

    protected Set<Type> setAddToSelected(Boolean addToSelected, boolean notify,
            IOriginator originator) {
        if (changed(addToSelected, getAddToSelected())) {
            delegate.setAddToSelected(addToSelected);
            settingsChanged(notify, Type.ADD_TO_SELECTED, originator);
            return EnumSet.of(Type.ADD_TO_SELECTED);
        }
        return EnumSet.noneOf(Type.class);
    }

    protected Set<Type> setAddGeometryToSelected(Boolean addGeometryToSelected,
            boolean notify, IOriginator originator) {
        if (changed(addGeometryToSelected, getAddGeometryToSelected())) {
            delegate.setAddGeometryToSelected(addGeometryToSelected);
            settingsChanged(notify, Type.ADD_GEOMETRY_TO_SELECTED, originator);
            return EnumSet.of(Type.ADD_GEOMETRY_TO_SELECTED);
        }
        return EnumSet.noneOf(Type.class);
    }

    protected Set<Type> setEventIdDisplayType(String eventIdDisplayType,
            boolean notify, IOriginator originator) {
        if (changed(eventIdDisplayType, getEventIdDisplayType())) {
            delegate.setEventIdDisplayType(eventIdDisplayType);
            settingsChanged(notify, Type.EVENT_IDENTIFIER_DISPLAY_TYPE,
                    originator);
            return EnumSet.of(Type.EVENT_IDENTIFIER_DISPLAY_TYPE);
        }
        return EnumSet.noneOf(Type.class);
    }

    protected Set<Type> setPerspectiveIDs(Set<String> perspectiveIDs,
            boolean notify, IOriginator originator) {
        if (changed(perspectiveIDs, getPerspectiveIDs())) {
            delegate.setPerspectiveIDs(getSetCopy(perspectiveIDs));
            settingsChanged(notify, Type.PERSPECTIVE_IDENTIFIERS, originator);
            return EnumSet.of(Type.PERSPECTIVE_IDENTIFIERS);
        }
        return EnumSet.noneOf(Type.class);
    }

    protected Set<Type> setDeselectAfterIssuing(Boolean deselectAfterIssuing,
            boolean notify, IOriginator originator) {
        if (changed(deselectAfterIssuing, getDeselectAfterIssuing())) {
            delegate.setDeselectAfterIssuing(deselectAfterIssuing);
            settingsChanged(notify, Type.DESELECT_AFTER_ISSUING, originator);
            return EnumSet.of(Type.DESELECT_AFTER_ISSUING);
        }
        return EnumSet.noneOf(Type.class);
    }

    /**
     * This method uses the manager to get categories and convert visibleTypes
     * into a format useable by megawidgets.
     * 
     * @return
     */
    @JsonProperty
    public List<HazardCategoryAndTypes> getHazardCategoriesAndTypes() {
        Map<String, HazardCategoryAndTypes> typeMap = new HashMap<String, HazardCategoryAndTypes>();
        for (String type : getVisibleTypes()) {
            IHazardEvent event = new BaseHazardEvent();
            HazardEventUtilities.populateEventForHazardType(event, type);
            String cat = configManager.getHazardCategory(event);
            HazardCategoryAndTypes hcat = typeMap.get(cat);
            if (hcat == null) {
                hcat = new HazardCategoryAndTypes();
                hcat.setDisplayString(cat);
                hcat.setIdentifier(cat);
                hcat.setChildren(new ArrayList<String>(Arrays.asList(type)));
                typeMap.put(cat, hcat);
            } else {
                hcat.getChildren().add(type);
            }
        }
        return new ArrayList<>(typeMap.values());
    }

    /**
     * This method is used by megawidgets to set the visibleTypes.
     * 
     * @param hazardCategoriesAndTypes
     *            some hazardTypes that are organized by category.
     * @param originator
     */
    public void setHazardCategoriesAndTypes(
            HazardCategoryAndTypes[] hazardCategoriesAndTypes,
            IOriginator originator) {
        Set<String> types = new HashSet<String>();
        for (HazardCategoryAndTypes hcat : hazardCategoriesAndTypes) {
            types.addAll(hcat.getChildren());
        }
        setVisibleTypes(types, originator);
    }

}

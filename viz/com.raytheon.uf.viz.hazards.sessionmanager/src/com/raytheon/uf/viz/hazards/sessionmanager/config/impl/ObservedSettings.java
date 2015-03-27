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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.annotate.JsonProperty;

import com.raytheon.uf.common.dataplugin.events.hazards.event.BaseHazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardEventUtilities;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.types.HazardCategoryAndTypes;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Column;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.ISettings;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.MapCenter;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Settings;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Tool;

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
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */

@XmlRootElement(name = "HazardServicesSettings")
@XmlAccessorType(XmlAccessType.FIELD)
public class ObservedSettings implements ISettings {

    private Settings delegate;

    /**
     * For JAXB serialization.
     */
    @SuppressWarnings("unused")
    private ObservedSettings() {

    }

    public ObservedSettings(ISettings other) {
        delegate = new Settings(other);
        setStaticSettingsID(getSettingsID());
    }

    public boolean isValueChanged(Object newObj, Object oldObj) {

        return (changed(newObj, oldObj));
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
        return (original == null ? null : new MapCenter(original.getLon(),
                original.getLat(), original.getZoom()));
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
    public Tool getTool(String toolName) {
        return delegate.getTool(toolName);
    }

    /**
     * Copy all settings from another Settings object into this one.
     * 
     * @param other
     * @return True if the Settings ID has changed an a SettingsLoaded message
     *         should be dispatched. False otherwise and a SettingsModified
     *         message should be dispatched.
     */
    @Override
    public SettingsChangeType apply(ISettings other) {

        SettingsChangeType changeType = SettingsChangeType.NO_CHANGE;
        boolean fieldChanged = false;
        boolean curChanged = false;
        boolean idChanged = changed(getSettingsID(), other.getSettingsID());
        curChanged = setInternalSettingsID(other.getSettingsID());
        if (curChanged == true) {
            fieldChanged = true;
        }
        curChanged = setInternalVisibleTypes(other.getVisibleTypes());
        if (curChanged == true) {
            fieldChanged = true;
        }
        curChanged = setInternalVisibleStatuses(other.getVisibleStatuses());
        curChanged = setInternalToolbarTools(other.getToolbarTools());
        if (curChanged == true) {
            fieldChanged = true;
        }
        curChanged = setInternalDefaultTimeDisplayDuration(other
                .getDefaultTimeDisplayDuration());
        if (curChanged == true) {
            fieldChanged = true;
        }
        curChanged = setInternalMapCenter(other.getMapCenter());
        if (curChanged == true) {
            fieldChanged = true;
        }
        curChanged = setInternalDefaultCategory(other.getDefaultCategory());
        if (curChanged == true) {
            fieldChanged = true;
        }
        curChanged = setInternalPossibleSites(other.getPossibleSites());
        if (curChanged == true) {
            fieldChanged = true;
        }
        curChanged = setInternalVisibleSites(other.getVisibleSites());
        if (curChanged == true) {
            fieldChanged = true;
        }
        curChanged = setInternalDisplayName(other.getDisplayName());
        if (curChanged == true) {
            fieldChanged = true;
        }
        curChanged = setInternalDefaultDuration(other.getDefaultDuration());
        if (curChanged == true) {
            fieldChanged = true;
        }
        curChanged = setInternalVisibleColumns(other.getVisibleColumns());
        if (curChanged == true) {
            fieldChanged = true;
        }
        curChanged = setInternalColumns(other.getColumns());
        if (curChanged == true) {
            fieldChanged = true;
        }
        curChanged = setInternalStaticSettingsID(other.getStaticSettingsID());
        if (curChanged == true) {
            fieldChanged = true;
        }
        curChanged = setInternalAddToSelected(other.getAddToSelected());
        if (curChanged == true) {
            fieldChanged = true;
        }
        curChanged = setInternalAddGeometryToSelected(other
                .getAddGeometryToSelected());
        if (curChanged == true) {
            fieldChanged = true;
        }

        if (idChanged) {
            changeType = SettingsChangeType.LOAD_CHANGE;
        } else {
            if (fieldChanged == true) {
                changeType = SettingsChangeType.MODIFY_CHANGE;
            }
        }

        return (changeType);
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
     * @return SettingsChangeType
     */
    public SettingsChangeType applyPersistedChanges(ISettings persisted,
            ISettings update) {

        SettingsChangeType changeType = SettingsChangeType.NO_CHANGE;
        boolean notify = false;
        boolean idChanged = false;
        if (!changed(getSettingsID(), persisted.getSettingsID())) {
            idChanged = changed(update.getSettingsID(), getSettingsID());
            setSettingsID(update.getSettingsID());
            notify = true;
        }
        if (!changed(getVisibleTypes(), persisted.getVisibleTypes())) {
            setVisibleTypes(update.getVisibleTypes());
            notify = true;
        }
        if (!changed(getVisibleStatuses(), persisted.getVisibleStatuses())) {
            setVisibleStatuses(update.getVisibleStatuses());
            notify = true;
        }
        if (!changed(getToolbarTools(), persisted.getToolbarTools())) {
            setToolbarTools(update.getToolbarTools());
            notify = true;
        }
        if (!changed(getDefaultTimeDisplayDuration(),
                persisted.getDefaultTimeDisplayDuration())) {
            setDefaultTimeDisplayDuration(update
                    .getDefaultTimeDisplayDuration());
            notify = true;
        }
        if (!changed(getMapCenter(), persisted.getMapCenter())) {
            setMapCenter(update.getMapCenter());
            notify = true;
        }
        if (!changed(getDefaultCategory(), persisted.getDefaultCategory())) {
            setDefaultCategory(update.getDefaultCategory());
            notify = true;
        }
        if (!changed(getPossibleSites(), persisted.getPossibleSites())) {
            setPossibleSites(update.getPossibleSites());
            notify = true;
        }
        if (!changed(getVisibleSites(), persisted.getVisibleSites())) {
            setVisibleSites(update.getVisibleSites());
            notify = true;
        }
        if (!changed(getDisplayName(), persisted.getDisplayName())) {
            setDisplayName(update.getDisplayName());
            notify = true;
        }
        if (!changed(getDefaultDuration(), persisted.getDefaultDuration())) {
            setDefaultDuration(update.getDefaultDuration());
            notify = true;
        }
        if (!changed(getVisibleColumns(), persisted.getVisibleColumns())) {
            setVisibleColumns(update.getVisibleColumns());
            notify = true;
        }
        if (!changed(getColumns(), persisted.getColumns())) {
            setColumns(update.getColumns());
            notify = true;
        }
        if (!changed(getStaticSettingsID(), persisted.getStaticSettingsID())) {
            setStaticSettingsID(update.getStaticSettingsID());
            notify = true;
        }
        if (!changed(getAddToSelected(), persisted.getAddToSelected())) {
            setAddToSelected(update.getAddToSelected());
            notify = true;
        }
        if (notify) {
            if (idChanged) {
                changeType = SettingsChangeType.LOAD_CHANGE;
            } else {
                changeType = SettingsChangeType.LOAD_CHANGE;
            }
        }

        return (changeType);
    }

    @Override
    public void setSettingsID(String settingsID) {
        setInternalSettingsID(settingsID);
    }

    @Override
    public void setVisibleTypes(Set<String> visibleTypes) {
        setInternalVisibleTypes(visibleTypes);
    }

    @Override
    public void setVisibleStatuses(Set<String> visibleStatuses) {
        setInternalVisibleStatuses(visibleStatuses);
    }

    @Override
    public void setToolbarTools(List<Tool> toolbarTools) {
        setInternalToolbarTools(toolbarTools);
    }

    @Override
    public void setDefaultTimeDisplayDuration(Long defaultTimeDisplayDuration) {
        setInternalDefaultTimeDisplayDuration(defaultTimeDisplayDuration);
    }

    @Override
    public void setMapCenter(MapCenter mapCenter) {
        setInternalMapCenter(mapCenter);
    }

    @Override
    public void setDefaultCategory(String defaultCategory) {
        setInternalDefaultCategory(defaultCategory);
    }

    @Override
    public void setPossibleSites(Set<String> possibleSites) {
        setInternalPossibleSites(possibleSites);
    }

    @Override
    public void setVisibleSites(Set<String> visibleSites) {
        setInternalVisibleSites(visibleSites);
    }

    @Override
    public void setDisplayName(String displayName) {
        setInternalDisplayName(displayName);
    }

    @Override
    public void setDefaultDuration(Long defaultDuration) {
        setInternalDefaultDuration(defaultDuration);
    }

    @Override
    public void setVisibleColumns(List<String> visibleColumns) {
        setInternalVisibleColumns(visibleColumns);
    }

    @Override
    public void setColumns(Map<String, Column> columns) {
        setInternalColumns(columns);
    }

    @Override
    public void setStaticSettingsID(String staticSettingsID) {
        setInternalStaticSettingsID(staticSettingsID);
    }

    @Override
    public void setAddToSelected(Boolean addToSelected) {
        setInternalAddToSelected(addToSelected);
    }

    @Override
    public void setAddGeometryToSelected(Boolean addGeometryToSelected) {
        setInternalAddGeometryToSelected(addGeometryToSelected);
    }

    @Override
    public void setPerspectiveIDs(Set<String> perspectiveIDs) {
        setInternalPerspectiveIDs(perspectiveIDs);
    }

    // Internal Set Methods. Set methods that return a isChanged boolean flag
    public boolean setInternalSettingsID(String settingsID) {
        boolean isChanged = changed(settingsID, getSettingsID());
        if (isChanged == true) {
            delegate.setSettingsID(settingsID);
        }

        return (isChanged);
    }

    public boolean setInternalVisibleTypes(Set<String> visibleTypes) {
        boolean isChanged = changed(visibleTypes, getVisibleTypes());
        if (isChanged == true) {
            delegate.setVisibleTypes(getSetCopy(visibleTypes));
        }

        return (isChanged);
    }

    public boolean setInternalVisibleStatuses(Set<String> visibleStatuses) {
        boolean isChanged = changed(visibleStatuses, getVisibleStatuses());
        if (isChanged == true) {
            delegate.setVisibleStatuses(getSetCopy(visibleStatuses));
        }

        return (isChanged);
    }

    public boolean setInternalToolbarTools(List<Tool> toolbarTools) {
        boolean isChanged = changed(toolbarTools, getToolbarTools());
        if (isChanged == true) {
            delegate.setToolbarTools(getToolbarToolsCopy(toolbarTools));
        }

        return (isChanged);
    }

    public boolean setInternalDefaultTimeDisplayDuration(
            Long defaultTimeDisplayDuration) {
        boolean isChanged = changed(defaultTimeDisplayDuration,
                getDefaultTimeDisplayDuration());
        if (isChanged == true) {
            delegate.setDefaultTimeDisplayDuration(defaultTimeDisplayDuration);
        }

        return (isChanged);
    }

    public boolean setInternalMapCenter(MapCenter mapCenter) {
        boolean isChanged = changed(mapCenter, getMapCenter());
        if (isChanged == true) {
            delegate.setMapCenter(getMapCenterCopy(mapCenter));
        }

        return (isChanged);
    }

    public boolean setInternalDefaultCategory(String defaultCategory) {
        boolean isChanged = changed(defaultCategory, getDefaultCategory());
        if (isChanged == true) {
            delegate.setDefaultCategory(defaultCategory);
        }

        return (isChanged);
    }

    public boolean setInternalPossibleSites(Set<String> possibleSites) {
        boolean isChanged = changed(possibleSites, getPossibleSites());
        if (isChanged == true) {
            delegate.setPossibleSites(getSetCopy(possibleSites));
        }

        return (isChanged);
    }

    public boolean setInternalVisibleSites(Set<String> visibleSites) {
        boolean isChanged = changed(visibleSites, getVisibleSites());
        if (isChanged == true) {
            delegate.setVisibleSites(getSetCopy(visibleSites));
        }

        return (isChanged);
    }

    public boolean setInternalDisplayName(String displayName) {
        boolean isChanged = changed(displayName, getDisplayName());
        if (isChanged == true) {
            delegate.setDisplayName(displayName);
        }

        return (isChanged);
    }

    public boolean setInternalDefaultDuration(Long defaultDuration) {
        boolean isChanged = changed(defaultDuration, getDefaultDuration());
        if (isChanged == true) {
            delegate.setDefaultDuration(defaultDuration);
        }

        return (isChanged);
    }

    public boolean setInternalVisibleColumns(List<String> visibleColumns) {
        boolean isChanged = changed(visibleColumns, getVisibleColumns());
        if (isChanged == true) {
            delegate.setVisibleColumns(getListCopy(visibleColumns));
        }

        return (isChanged);
    }

    public boolean setInternalColumns(Map<String, Column> columns) {
        boolean isChanged = changed(columns, getColumns());
        if (isChanged == true) {
            delegate.setColumns(getColumnsCopy(columns));
        }

        return (isChanged);
    }

    public boolean setInternalStaticSettingsID(String staticSettingsID) {
        boolean isChanged = changed(staticSettingsID, getStaticSettingsID());
        if (isChanged == true) {
            delegate.setStaticSettingsID(staticSettingsID);
        }

        return (isChanged);
    }

    public boolean setInternalAddToSelected(Boolean addToSelected) {
        boolean isChanged = changed(addToSelected, getAddToSelected());
        if (isChanged == true) {
            delegate.setAddToSelected(addToSelected);
        }

        return (isChanged);
    }

    public boolean setInternalAddGeometryToSelected(
            Boolean addGeometryToSelected) {
        boolean isChanged = changed(addGeometryToSelected,
                getAddGeometryToSelected());
        if (isChanged == true) {
            delegate.setAddGeometryToSelected(addGeometryToSelected);
        }

        return (isChanged);
    }

    public boolean setInternalPerspectiveIDs(Set<String> perspectiveIDs) {
        boolean isChanged = changed(perspectiveIDs, getPerspectiveIDs());
        if (isChanged == true) {
            delegate.setPerspectiveIDs(getSetCopy(perspectiveIDs));
        }

        return (isChanged);
    }

    /**
     * This method uses the manager to get categories and convert visibleTypes
     * into a format useable by megawidgets.
     * 
     * @return
     */
    @JsonProperty
    public List<HazardCategoryAndTypes> getHazardCategoriesAndTypes() {

        ObservedSettingsHelper observedSettingsHelper = ObservedSettingsHelper
                .getInstance();
        Map<String, HazardCategoryAndTypes> typeMap = new HashMap<String, HazardCategoryAndTypes>();
        for (String type : getVisibleTypes()) {
            IHazardEvent event = new BaseHazardEvent();
            HazardEventUtilities.populateEventForHazardType(event, type);
            String cat = observedSettingsHelper.getHazardCategory(event);
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
        return new ArrayList<HazardCategoryAndTypes>(typeMap.values());
    }

    /**
     * This method is used by megawidgets to set the visibleTypes.
     * 
     * @param hazardCategoriesAndTypes
     *            some hazardTypes that are organized by category.
     */
    public void setHazardCategoriesAndTypes(
            HazardCategoryAndTypes[] hazardCategoriesAndTypes) {
        Set<String> types = new HashSet<String>();
        for (HazardCategoryAndTypes hcat : hazardCategoriesAndTypes) {
            types.addAll(hcat.getChildren());
        }
        setVisibleTypes(types);
    }
}

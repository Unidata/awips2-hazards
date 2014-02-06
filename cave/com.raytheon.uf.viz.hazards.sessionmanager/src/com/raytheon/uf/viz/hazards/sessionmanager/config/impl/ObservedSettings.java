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

import org.codehaus.jackson.annotate.JsonProperty;
import org.eclipse.core.runtime.Assert;

import com.raytheon.uf.common.dataplugin.events.hazards.event.BaseHazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardEventUtilities;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.config.SettingsIDModified;
import com.raytheon.uf.viz.hazards.sessionmanager.config.SettingsModified;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.types.HazardCategoryAndTypes;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Column;
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
 *  
 * Nov 29, 2013 2380       daniel.s.schaffer@noaa.gov Fixing bugs in settings-based filtering
 * 
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */

public class ObservedSettings extends Settings {

    private SessionConfigurationManager configManager;

    /**
     * For JAXB serialization.
     */
    @SuppressWarnings("unused")
    private ObservedSettings() {

    }

    public ObservedSettings(SessionConfigurationManager configManager,
            Settings other) {
        super(other);
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

    private void settingsChanged() {
        settingsChanged(new SettingsModified(configManager));
    }

    private void settingsChanged(SettingsModified notification) {
        // configManager is only null during construction, we don't want any
        // notifications during construction anyway.
        if (configManager != null) {
            configManager.settingsChanged(notification);
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
    public void applyPersistedChanges(Settings persisted, Settings update) {
        if (!changed(getSettingsID(), persisted.getSettingsID())) {
            setSettingsID(update.getSettingsID());
        }
        if (!changed(getVisibleTypes(), persisted.getVisibleTypes())) {
            setVisibleTypes(update.getVisibleTypes());
        }
        if (!changed(getVisibleStates(), persisted.getVisibleStates())) {
            setVisibleStates(update.getVisibleStates());
        }
        if (!changed(getToolbarTools(), persisted.getToolbarTools())) {
            setToolbarTools(update.getToolbarTools());
        }
        if (!changed(getDefaultTimeDisplayDuration(),
                persisted.getDefaultTimeDisplayDuration())) {
            setDefaultTimeDisplayDuration(update
                    .getDefaultTimeDisplayDuration());
        }
        if (!changed(getMapCenter(), persisted.getMapCenter())) {
            setMapCenter(update.getMapCenter());
        }
        if (!changed(getDefaultCategory(), persisted.getDefaultCategory())) {
            setDefaultCategory(update.getDefaultCategory());
        }
        if (!changed(getVisibleSites(), persisted.getVisibleSites())) {
            setVisibleSites(update.getVisibleSites());
        }
        if (!changed(getDisplayName(), persisted.getDisplayName())) {
            setDisplayName(update.getDisplayName());
        }
        if (!changed(getDefaultDuration(), persisted.getDefaultDuration())) {
            setDefaultDuration(update.getDefaultDuration());
        }
        if (!changed(getVisibleColumns(), persisted.getVisibleColumns())) {
            setVisibleColumns(update.getVisibleColumns());
        }
        if (!changed(getColumns(), persisted.getColumns())) {
            setColumns(update.getColumns());
        }
        if (!changed(getStaticSettingsID(), persisted.getStaticSettingsID())) {
            setStaticSettingsID(update.getStaticSettingsID());
        }
        if (!changed(getAddToSelected(), persisted.getAddToSelected())) {
            setAddToSelected(update.getAddToSelected());
        }
    }

    @Override
    public void setSettingsID(String settingsID) {
        if (changed(settingsID, getSettingsID())) {
            super.setSettingsID(settingsID);
            settingsChanged(new SettingsIDModified(configManager));
        }
    }

    @Override
    public void setVisibleTypes(Set<String> visibleTypes) {
        if (changed(visibleTypes, getVisibleTypes())) {
            super.setVisibleTypes(visibleTypes);
        }
    }

    @Override
    public void setVisibleStates(Set<String> visibleStates) {
        if (changed(visibleStates, getVisibleStates())) {
            super.setVisibleStates(visibleStates);
        }
    }

    @Override
    public void setToolbarTools(List<Tool> toolbarTools) {
        if (changed(toolbarTools, getToolbarTools())) {
            super.setToolbarTools(toolbarTools);
            settingsChanged();
        }
    }

    @Override
    public void setDefaultTimeDisplayDuration(Long defaultTimeDisplayDuration) {
        if (changed(defaultTimeDisplayDuration, getDefaultTimeDisplayDuration())) {
            super.setDefaultTimeDisplayDuration(defaultTimeDisplayDuration);
            settingsChanged();
        }
    }

    @Override
    public void setMapCenter(MapCenter mapCenter) {
        if (changed(mapCenter, getMapCenter())) {
            super.setMapCenter(mapCenter);
            settingsChanged();
        }
    }

    @Override
    public void setDefaultCategory(String defaultCategory) {
        if (changed(defaultCategory, getDefaultCategory())) {
            super.setDefaultCategory(defaultCategory);
            settingsChanged();
        }
    }

    @Override
    public void setVisibleSites(Set<String> visibleSites) {
        if (changed(visibleSites, getVisibleSites())) {
            super.setVisibleSites(visibleSites);
        }
    }

    @Override
    public void setDisplayName(String displayName) {
        if (changed(displayName, getDisplayName())) {
            super.setDisplayName(displayName);
            settingsChanged();
        }
    }

    @Override
    public void setDefaultDuration(Long defaultDuration) {
        if (changed(defaultDuration, getDefaultDuration())) {
            super.setDefaultDuration(defaultDuration);
            settingsChanged();
        }
    }

    @Override
    public void setVisibleColumns(List<String> visibleColumns) {
        if (changed(visibleColumns, getVisibleColumns())) {
            super.setVisibleColumns(visibleColumns);
            settingsChanged();
        }
    }

    @Override
    public void setColumns(Map<String, Column> columns) {
        if (changed(columns, getColumns())) {
            super.setColumns(columns);
            settingsChanged();
        }
    }

    @Override
    public void setStaticSettingsID(String staticSettingsID) {
        if (changed(staticSettingsID, getStaticSettingsID())) {
            super.setStaticSettingsID(staticSettingsID);
            settingsChanged();
        }
    }

    @Override
    public void setAddToSelected(Boolean addToSelected) {
        if (changed(addToSelected, getAddToSelected())) {
            super.setAddToSelected(addToSelected);
            settingsChanged();
        }
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

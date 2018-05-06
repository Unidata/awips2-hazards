/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.setting;

import java.util.List;

import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.ObservedSettings;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Field;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Settings;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.SettingsConfig;

import gov.noaa.gsd.viz.mvp.IView;

/**
 * Interface describing the methods required for implementing a settings view,
 * used by the user to view and manipulate settings.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Chris.Golden      Initial induction into repo
 * Dec 05, 2014    4124    Chris.Golden      Changed to work with ObservedSettings.
 * Feb 23, 2015    3618    Chris.Golden      Added ability to close settings dialog
 *                                           from public method.
 * Nov 17, 2015   11776    Roger.Ferrel      Add {@link ISaveAs} argument to
 *                                           showSettingsDetail.
 * Jan 17, 2018   33428    Chris.Golden      Changed to work with new, more flexible
 *                                           toolbar contribution code.
 * May 04, 2018   50032    Chris.Golden      Added additional filters to settings.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public interface ISettingsView<I, C, E extends Enum<E>> extends IView<I, C, E> {

    // Public Static Constants

    /**
     * Settings pulldown identifier.
     */
    public static final String SETTINGS_PULLDOWN_IDENTIFIER = "settingsPulldown";

    /**
     * Filters pulldown identifier.
     */
    public static final String FILTERS_PULLDOWN_IDENTIFIER = "filtersPulldown";

    // Public Methods

    /**
     * Initialize the view.
     * 
     * @param presenter
     *            Presenter managing this view.
     * @param settings
     * @param fields
     * @param currentSettings
     */
    public void initialize(SettingsPresenter presenter, List<Settings> settings,
            Field[] fields, ObservedSettings currentSettings);

    /**
     * Show the settings detail subview.
     * 
     * @param settingsConfig
     * @param settings
     * @param saveAs
     */
    public void showSettingDetail(SettingsConfig settingsConfig,
            ObservedSettings settings, ISaveAs saveAs);

    /**
     * Delete the settings detail subview.
     */
    public void deleteSettingDetail();

    /**
     * Determine whether or not the setting detail is currently in existence.
     * 
     * @return True if the setting detail currently exists, false otherwise.
     */
    public boolean isSettingDetailExisting();

    /**
     * Set the settings to those specified.
     * 
     * @param settings
     */
    public void setSettings(List<Settings> settings);

    /**
     * @param currentSettings
     */
    public void setCurrentSettings(ObservedSettings currentSettings);

    /**
     * Set the fields to be used to build filters.
     * 
     * @param fields
     *            Fields defining the filters.
     */
    public void setFilterFields(Field[] fields);
}

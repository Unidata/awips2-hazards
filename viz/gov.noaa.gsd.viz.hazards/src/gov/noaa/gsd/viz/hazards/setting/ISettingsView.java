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

import gov.noaa.gsd.viz.mvp.IView;

import java.util.List;

import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Field;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Settings;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.SettingsConfig;

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
 * 
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public interface ISettingsView<C, E extends Enum<E>> extends IView<C, E> {

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
    public void initialize(SettingsPresenter presenter,
            List<Settings> settings, Field[] fields, Settings currentSettings);

    /**
     * Show the settings detail subview.
     * 
     * @param settingsConfig
     * @param settings
     */
    public void showSettingDetail(SettingsConfig settingsConfig,
            Settings settings);

    /**
     * Set the settings to those specified.
     * 
     * @param settings
     */
    public void setSettings(List<Settings> settings);

    /**
     * @param currentSettings
     */
    public void setCurrentSettings(Settings currentSettings);
}

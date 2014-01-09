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

import gov.noaa.gsd.viz.hazards.jsonutilities.Dict;
import gov.noaa.gsd.viz.hazards.jsonutilities.DictList;
import gov.noaa.gsd.viz.mvp.IView;

import java.util.List;

import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Settings;

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
     * @param jsonFilters
     *            JSON string providing a list of dictionaries, each specifying
     *            a filter megawidget.
     * @param currentSettings
     */
    public void initialize(SettingsPresenter presenter,
            List<Settings> settings, String jsonFilters,
            Settings currentSettings);

    /**
     * Show the settings detail subview.
     * 
     * @param fields
     *            List of dictionaries, each providing a field to be displayed
     *            in the subview.
     * @param values
     *            Dictionary pairing keys found as the field names in
     *            <code>fields</code> with their values.
     */
    public void showSettingDetail(DictList fields, Dict values);

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

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

import gov.noaa.gsd.common.eventbus.BoundedReceptionEventBus;
import gov.noaa.gsd.viz.hazards.UIOriginator;
import gov.noaa.gsd.viz.hazards.display.HazardServicesPresenter;
import net.engio.mbassy.listener.Handler;

import com.raytheon.uf.viz.core.IDisplayPane;
import com.raytheon.uf.viz.core.drawables.IRenderableDisplay;
import com.raytheon.uf.viz.hazards.sessionmanager.ISessionManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.ISessionConfigurationManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.SettingsLoaded;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.ObservedSettings;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.ISettings;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.MapCenter;
import com.raytheon.uf.viz.hazards.sessionmanager.events.impl.ObservedHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.IOriginator;
import com.raytheon.viz.ui.VizWorkbenchManager;
import com.raytheon.viz.ui.editor.AbstractEditor;

/**
 * Settings presenter, used to mediate between the model and the settings view.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Chris.Golden      Initial induction into repo
 * Jul 15, 2013     585    Chris.Golden      Changed to support loading from bundle,
 *                                           including the passing in of the event
 *                                           bus so that the latter is no longer a
 *                                           singleton.
 * Dec 03, 2013 2182 daniel.s.schaffer@noaa.gov Refactoring - eliminated IHazardsIF
 * May 17, 2014 2925       Chris.Golden      Added newly required implementation of
 *                                           reinitialize(), and made initialize()
 *                                           protected as it is called by setView().
 * Dec 05, 2014 4124       Chris.Golden      Changed to work with newly parameterized
 *                                           config manager and with ObservedSettings.
 * Feb 23, 2015 3618       Chris.Golden      Added code to close and reopen the
 *                                           settings subview if a new setting is
 *                                           loaded. This is required because a new
 *                                           setting may have a different column list,
 *                                           possible site IDs, etc., any of which
 *                                           would necessitate the recreation of the
 *                                           dialog.
 * Apr 10, 2015 6898       Chris.Cody        Removed modelChanged legacy messaging method
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class SettingsPresenter extends
        HazardServicesPresenter<ISettingsView<?, ?>> implements IOriginator {

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param model
     *            Model to be handled by this presenter.
     * @param eventBus
     *            Event bus used to signal changes.
     */
    public SettingsPresenter(
            ISessionManager<ObservedHazardEvent, ObservedSettings> model,
            BoundedReceptionEventBus<Object> eventBus) {
        super(model, eventBus);
    }

    // Public Methods

    /**
     * Respond to a new settings object being loaded.
     * 
     * @param change
     *            Change that occurred.
     */
    @Handler
    public void settingsLoaded(SettingsLoaded change) {
        if (getView().isSettingDetailExisting()) {
            getView().deleteSettingDetail();
            showSettingDetail();
        }
    }

    /**
     * Show a subview providing setting detail for the current setting.
     */
    public final void showSettingDetail() {

        /*
         * Update the setting's zoom parameters with the current values.
         */
        double[] zoomParams = getDisplayZoomParameters();
        MapCenter mapCenter = new MapCenter(zoomParams[0], zoomParams[1],
                zoomParams[2]);
        ISessionConfigurationManager<ObservedSettings> configManager = getModel()
                .getConfigurationManager();
        ISettings modSettings = configManager.getSettings();
        MapCenter modSettingsMapCenter = modSettings.getMapCenter();
        if ((modSettingsMapCenter == null)
                || ((mapCenter.getLat() == modSettingsMapCenter.getLat()) && (mapCenter
                        .getLon() == modSettingsMapCenter.getLon()))) {
            modSettings.setMapCenter(mapCenter);
            configManager.updateCurrentSettings(modSettings,
                    UIOriginator.SETTINGS_DIALOG);
        }
        ObservedSettings settings = configManager.getSettings();
        /*
         * Have the view open the setting detail subview.
         */
        getView()
                .showSettingDetail(configManager.getSettingsConfig(), settings);
    }

    // Protected Methods

    @Override
    protected void initialize(ISettingsView<?, ?> view) {
        ISessionConfigurationManager<ObservedSettings> configManager = getModel()
                .getConfigurationManager();
        view.initialize(this, configManager);
    }

    @Override
    protected void reinitialize(ISettingsView<?, ?> view) {

        /*
         * No action.
         */
    }

    // Private Methods

    /**
     * Get the display's current zoom parameters.
     * 
     * @return Array of three numbers, the first being the longitude of the zoom
     *         center point, the second being the latitude of the zoom center
     *         point, and the third being the zoom multiplier.
     */
    private double[] getDisplayZoomParameters() {
        AbstractEditor editor = (AbstractEditor) VizWorkbenchManager
                .getInstance().getActiveEditor();
        IDisplayPane pane = editor.getActiveDisplayPane();
        IRenderableDisplay display = pane.getRenderableDisplay();
        double[] params = new double[3];
        double[] center = pane.getDescriptor().pixelToWorld(
                display.getExtent().getCenter());
        for (int j = 0; j < center.length; j++) {
            params[j] = center[j];
        }
        params[2] = 1.0 / display.getZoom();
        return params;
    }
}

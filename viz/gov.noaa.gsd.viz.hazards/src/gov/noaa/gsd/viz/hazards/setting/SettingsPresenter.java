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

import java.util.EnumSet;

import net.engio.mbassy.listener.Handler;

import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.viz.core.IDisplayPane;
import com.raytheon.uf.viz.core.drawables.IRenderableDisplay;
import com.raytheon.uf.viz.hazards.sessionmanager.ISessionManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.SettingsLoaded;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.ObservedSettings;
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
 * Nov 17, 2015 11776      Roger.Ferrel      Use the {@link ISaveAs} interface.
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
     * Receive notification of a model change.
     * 
     * @param changes
     *            Set of elements within the model that have changed.
     */
    @Override
    public void modelChanged(EnumSet<HazardConstants.Element> changed) {
        if (changed.contains(HazardConstants.Element.SETTINGS)) {
            getView()
                    .setSettings(
                            getModel().getConfigurationManager()
                                    .getAvailableSettings());
        }
        if (changed.contains(HazardConstants.Element.CURRENT_SETTINGS)) {
            getView().setCurrentSettings(
                    getModel().getConfigurationManager().getSettings());
        }
    }

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
            showSettingDetail(new ISaveAs() {

                @Override
                public void saveAsPerformed() {
                    getView().setSettings(
                            getModel().getConfigurationManager()
                                    .getAvailableSettings());
                }
            });
        }
    }

    /**
     * Show a subview providing setting detail for the current setting.
     * 
     * @param saveAs
     *            Callback to be used if a save-as is performed.
     */
    public final void showSettingDetail(ISaveAs saveAs) {

        ObservedSettings settings = getModel().getConfigurationManager()
                .getSettings();

        /*
         * Update the setting's zoom parameters with the current values.
         */
        double[] zoomParams = getDisplayZoomParameters();
        MapCenter mapCenter = new MapCenter(zoomParams[0], zoomParams[1],
                zoomParams[2]);
        settings.setMapCenter(mapCenter, UIOriginator.SETTINGS_DIALOG);

        /*
         * Have the view open the setting detail subview.
         */
        getView().showSettingDetail(
                getModel().getConfigurationManager().getSettingsConfig(),
                settings, saveAs);
    }

    // Protected Methods

    @Override
    protected void initialize(ISettingsView<?, ?> view) {
        ObservedSettings settings = getModel().getConfigurationManager()
                .getSettings();
        view.initialize(this, getModel().getConfigurationManager()
                .getAvailableSettings(), getModel().getConfigurationManager()
                .getFilterConfig(), settings);
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

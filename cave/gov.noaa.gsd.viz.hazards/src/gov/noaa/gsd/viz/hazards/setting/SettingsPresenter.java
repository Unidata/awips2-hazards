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

import gov.noaa.gsd.viz.hazards.display.HazardServicesPresenter;
import gov.noaa.gsd.viz.hazards.jsonutilities.Dict;
import gov.noaa.gsd.viz.hazards.jsonutilities.DictList;

import java.util.EnumSet;

import com.google.common.eventbus.EventBus;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.viz.core.IDisplayPane;
import com.raytheon.uf.viz.core.drawables.IRenderableDisplay;
import com.raytheon.uf.viz.hazards.sessionmanager.ISessionManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Settings;
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
 * 
 * Dec 03, 2013 2182 daniel.s.schaffer@noaa.gov Refactoring - eliminated IHazardsIF
 * 
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class SettingsPresenter extends
        HazardServicesPresenter<ISettingsView<?, ?>> {

    // Private Static Constants

    /**
     * Zoom parameters key in a setting dictionary.
     */
    private static final String ZOOM_PARAMETERS = "mapCenter";

    /**
     * Keys for specific zoom parameters in the dictionary value for the
     * <code>ZOOM_PARAMETERS</code> key in the setting dictionary.
     */
    private static final String[] ZOOM_PARAM_NAMES = { "lon", "lat", "zoom" };

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param model
     *            Model to be handled by this presenter.
     * @param view
     *            Settings view to be handled by this presenter.
     * @param eventBus
     *            Event bus used to signal changes.
     */
    public SettingsPresenter(ISessionManager model, ISettingsView<?, ?> view,
            EventBus eventBus) {
        super(model, view, eventBus);
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
            getView().setSettings(configurationManager.getSettingsListAsJSON());
        }
        if (changed.contains(HazardConstants.Element.DYNAMIC_SETTING)) {
            Settings settings = configurationManager.getSettings();
            getView().setDynamicSetting(jsonConverter.toJson(settings));
        }
    }

    /**
     * Show a subview providing setting detail for the current setting.
     */
    public final void showSettingDetail() {

        // Get the parameters for the settings view.
        DictList fields = DictList.getInstance(configurationManager
                .getConfigItem(HazardConstants.SETTING_CONFIG));
        Settings settings = configurationManager.getSettings();
        Dict values = Dict.getInstance(jsonConverter.toJson(settings));

        // Update the setting's zoom parameters with the current
        // values.
        double[] zoomParams = getDisplayZoomParameters();
        Dict zoomDict = new Dict();
        for (int j = 0; j < ZOOM_PARAM_NAMES.length; j++) {
            zoomDict.put(ZOOM_PARAM_NAMES[j], zoomParams[j]);
        }
        values.put(ZOOM_PARAMETERS, zoomDict);

        // Have the view open the setting detail subview.
        getView().showSettingDetail(fields, values);
    }

    // Protected Methods

    /**
     * Initialize the specified view in a subclass-specific manner.
     * 
     * @param view
     *            View to be initialized.
     */
    @Override
    public void initialize(ISettingsView<?, ?> view) {
        Settings settings = configurationManager.getSettings();
        view.initialize(this, configurationManager.getSettingsListAsJSON(),
                configurationManager.getConfigItem("filterConfig"),
                jsonConverter.toJson(settings));
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

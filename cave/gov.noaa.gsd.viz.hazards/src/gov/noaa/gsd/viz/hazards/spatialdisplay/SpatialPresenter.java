/**
 * This software was developed and / or modified by the National Oceanic and
 * Atmospheric Administration (NOAA), Earth System Research Laboratory (ESRL),
 * Global Systems Division (GSD), Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.spatialdisplay;

import gov.noaa.gsd.viz.hazards.display.HazardServicesAppBuilder;
import gov.noaa.gsd.viz.hazards.display.HazardServicesPresenter;
import gov.noaa.gsd.viz.hazards.display.IHazardServicesModel;
import gov.noaa.gsd.viz.hazards.display.IHazardServicesModel.Element;
import gov.noaa.gsd.viz.hazards.jsonutilities.Dict;
import gov.noaa.gsd.viz.hazards.spatialdisplay.mousehandlers.MouseHandlerFactory;

import java.util.Date;
import java.util.EnumSet;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

/**
 * Spatial presenter, used to mediate between the model and the spatial view.
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
public class SpatialPresenter extends
        HazardServicesPresenter<ISpatialView<?, ?>> {

    /**
     * Logging mechanism.
     */
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(SpatialPresenter.class);

    /**
     * Zoom parameter names for setting dictionary.
     */
    private final static String ZOOM_PARAMETERS = "mapCenter";

    /**
     * Zoom parameter element names.
     */
    private final static String[] ZOOM_PARAM_NAMES = { "lon", "lat", "zoom" };

    /**
     * Construct a standard instance.
     * 
     * @param model
     *            Model to be handled by this presenter.
     * @param view
     *            Spatial view to be handled by this presenter.
     */
    public SpatialPresenter(IHazardServicesModel model, ISpatialView<?, ?> view) {
        super(model, view);
    }

    /**
     * Initialize the specified view in a subclass-specific manner.
     * 
     * @param view
     *            View to be initialized.
     */
    @Override
    protected void initialize(ISpatialView<?, ?> view) {
        MouseHandlerFactory mouseFactory = new MouseHandlerFactory(this);
        getView().initialize(this, mouseFactory);
        updateSpatialDisplay();
    }

    /**
     * Receive notification of a model change.
     * 
     * @param changes
     *            Set of elements within the model that have changed.
     */
    @Override
    public void modelChanged(EnumSet<Element> changed) {
        if (changed.contains(IHazardServicesModel.Element.SETTINGS)) {
            String settingID = getModel().getCurrentSettingsID();
            useSettingZoomParameters(settingID);
        } else if (changed.contains(IHazardServicesModel.Element.CAVE_TIME)) {
            updateCaveSelectedTime();
        }

        updateSpatialDisplay();
    }

    /**
     * Update the event areas drawn in the spatial view.
     */
    public void updateSpatialDisplay() {
        IHazardServicesModel model = getModel();
        getView().drawEvents(
                model.getComponentData(
                        HazardServicesAppBuilder.SPATIAL_ORIGINATOR, "all"));
    }

    /**
     * Use the specified setting's zoom parameters.
     * 
     * @param settingID
     *            Identifier of setting that is to supply the setting
     *            parameters.
     */
    public void useSettingZoomParameters(String settingID) {

        // Get the new setting parameters, and from them, get the
        // zoom parameters.
        double[] zoomParams = new double[ZOOM_PARAM_NAMES.length];
        boolean zoomExtracted = true;
        try {
            Dict settingDict = Dict.getInstance(getModel().getStaticSettings(
                    settingID));
            Dict zoomDict = settingDict
                    .getDynamicallyTypedValue(ZOOM_PARAMETERS);
            for (int j = 0; j < zoomParams.length; j++) {
                zoomParams[j] = ((Number) zoomDict.get(ZOOM_PARAM_NAMES[j]))
                        .doubleValue();
            }
        } catch (Exception e) {
            statusHandler.error("SpatialPresenter.useSettingZoomParameters(): "
                    + "Error: could not parse zoom parameter values "
                    + "for setting ID " + settingID + " from JSON.", e);
            zoomExtracted = false;
        }

        // Set the display to the setting's extracted zoom parameters.
        if (zoomExtracted) {
            getView().setDisplayZoomParameters(zoomParams[0], zoomParams[1],
                    zoomParams[2]);
        }
    }

    /**
     * Updates the CAVE time.
     * 
     * @param selectedTime_ms
     *            The Hazard Services selected time in milliseconds.
     * @return
     */
    public void updateCaveSelectedTime() {
        long selectedTime = Long.parseLong(getModel().getSelectedTime());
        getView().manageViewFrames(new Date(selectedTime));
    }

    /**
     * Creates a new event area and returns its id.
     * 
     * @param
     * @return
     */
    public String getNewEventAreaId(String eventAreaJSON) {
        return getModel().newEvent(eventAreaJSON);
    }

}

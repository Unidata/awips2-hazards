/**
 * This software was developed and / or modified by the National Oceanic and
 * Atmospheric Administration (NOAA), Earth System Research Laboratory (ESRL),
 * Global Systems Division (GSD), Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.spatialdisplay;

import gov.noaa.gsd.viz.hazards.display.HazardServicesPresenter;
import gov.noaa.gsd.viz.hazards.jsonutilities.Dict;
import gov.noaa.gsd.viz.hazards.spatialdisplay.mousehandlers.MouseHandlerFactory;

import java.util.Date;
import java.util.EnumSet;

import com.google.common.eventbus.EventBus;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.hazards.sessionmanager.ISessionManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Settings;

/**
 * Spatial presenter, used to mediate between the model and the spatial view.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Chris.Golden      Initial induction into repo
 * Jul 12, 2013     585    Chris.Golden      Changed to support loading from bundle,
 *                                           including the passing in of the event
 *                                           bus so that the latter is no longer a
 *                                           singleton.
 * Aug 6, 2013     1265    Bryon.Lawrence    Added support for undo/redo.
 * Aug  9, 2013 1921       daniel.s.schaffer@noaa.gov  Support of replacement of JSON with POJOs
 * Nov 23, 2013    1462    bryon.lawrence    Added support for drawing hazard
 *                                           hatched areas.
 * 
 * Dec 03, 2013 2182 daniel.s.schaffer@noaa.gov Refactoring - eliminated IHazardsIF
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
     * Mouse handler factory.
     */
    private MouseHandlerFactory mouseFactory = null;

    /**
     * Construct a standard instance.
     * 
     * @param model
     *            Model to be handled by this presenter.
     * @param view
     *            Spatial view to be handled by this presenter.
     * @param eventBus
     *            Event bus used to signal changes.
     */
    public SpatialPresenter(ISessionManager model, ISpatialView<?, ?> view,
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
            useSettingZoomParameters();
        } else if (changed.contains(HazardConstants.Element.DYNAMIC_SETTING)) {
            Settings dset = configurationManager.getSettings();
            String settingsAsJSON = jsonConverter.toJson(dset);
            getView().setSetting(settingsAsJSON);
        } else if (changed.contains(HazardConstants.Element.CAVE_TIME)) {
            updateCaveSelectedTime();
        }

        updateSpatialDisplay();
    }

    /**
     * Update the event areas drawn in the spatial view.
     */
    public void updateSpatialDisplay() {
        getView().setUndoEnabled(getModel().isUndoable());
        getView().setRedoEnabled(getModel().isRedoable());
        getView().drawEvents(getModel().isAutoHazardCheckingOn(),
                getModel().areHatchedAreasDisplayed());
    }

    /**
     * Updates the CAVE time.
     * 
     * @param selectedTime_ms
     *            The Hazard Services selected time in milliseconds.
     * @return
     */
    public void updateCaveSelectedTime() {
        Date selectedTime = timeManager.getSelectedTime();
        getView().manageViewFrames(selectedTime);
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

    /**
     * Get the selected time.
     * 
     * @return Selected time.
     */
    public Date getSelectedTime() {
        return timeManager.getSelectedTime();
    }

    // Protected Methods

    /**
     * Initialize the specified view in a subclass-specific manner.
     * 
     * @param view
     *            View to be initialized.
     */
    @Override
    public void initialize(ISpatialView<?, ?> view) {
        if (mouseFactory == null) {
            mouseFactory = new MouseHandlerFactory(this);
        }
        getView().initialize(this, mouseFactory);
        updateSpatialDisplay();
    }

    // Package Methods

    /**
     * Get the hazard events with the specified identifier.
     * 
     * @param eventId
     *            Event identifier.
     * @return JSON string containing a list of key-value mappings that define
     *         the events.
     */
    String getEvents(String eventId) {
        return getModel().getComponentData("Spatial", eventId);
    }

    // Private Methods

    /**
     * Use the specified setting's zoom parameters.
     * 
     */
    private void useSettingZoomParameters() {

        // Get the new setting parameters, and from them, get the
        // zoom parameters.
        double[] zoomParams = new double[ZOOM_PARAM_NAMES.length];
        boolean zoomExtracted = true;
        Settings settings = configurationManager.getSettings();
        try {
            Dict settingDict = Dict.getInstance(jsonConverter.toJson(settings));
            Dict zoomDict = settingDict
                    .getDynamicallyTypedValue(ZOOM_PARAMETERS);
            for (int j = 0; j < zoomParams.length; j++) {
                zoomParams[j] = ((Number) zoomDict.get(ZOOM_PARAM_NAMES[j]))
                        .doubleValue();
            }
        } catch (Exception e) {
            statusHandler.error("SpatialPresenter.useSettingZoomParameters(): "
                    + "Error: could not parse zoom parameter values "
                    + "for setting ID " + settings.getSettingsID()
                    + " from JSON.", e);
            zoomExtracted = false;
        }

        // Set the display to the setting's extracted zoom parameters.
        if (zoomExtracted) {
            getView().setDisplayZoomParameters(zoomParams[0], zoomParams[1],
                    zoomParams[2]);
        }
    }
}

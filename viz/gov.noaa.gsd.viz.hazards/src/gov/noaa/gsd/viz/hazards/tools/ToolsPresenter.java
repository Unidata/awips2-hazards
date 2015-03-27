/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.tools;

import gov.noaa.gsd.common.eventbus.BoundedReceptionEventBus;
import gov.noaa.gsd.viz.hazards.display.HazardServicesPresenter;

import java.util.List;

import net.engio.mbassy.listener.Handler;

import com.raytheon.uf.viz.hazards.sessionmanager.ISessionManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.ISessionConfigurationManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.SettingsModified;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.ObservedSettings;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.ISettings;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Tool;
import com.raytheon.uf.viz.hazards.sessionmanager.events.impl.ObservedHazardEvent;

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
 * Dec 03, 2013    2182    daniel.s.schaffer Refactoring
 * May 17, 2014    2925    Chris.Golden      Added newly required implementation of
 *                                           reinitialize(), and made initialize()
 *                                           protected as it is called by setView().
 * Dec 05, 2014    4124    Chris.Golden      Changed to work with newly parameterized
 *                                           config manager.
 * Jan 29, 2015 4375       Dan Schaffer      Console initiation of RVS product generation
 * Jan 30, 2015    3626    Chris.Golden      Added ability to pass event type when
 *                                           running a recommender.
 * Apr 10, 2015    6898    Chris.Cody        Removed modelChanged legacy messaging method
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class ToolsPresenter extends HazardServicesPresenter<IToolsView<?, ?>> {

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param model
     *            Model to be handled by this presenter.
     * @param view
     *            Tools view to be handled by this presenter.
     * @param eventBus
     *            Event bus used to signal changes.
     */
    public ToolsPresenter(
            ISessionManager<ObservedHazardEvent, ObservedSettings> model,
            BoundedReceptionEventBus<Object> eventBus) {
        super(model, eventBus);
    }

    // Public Methods

    /**
     * Initialize the specified view in a subclass-specific manner.
     * 
     * @param view
     *            View to be initialized.
     */
    @Override
    protected void initialize(IToolsView<?, ?> view) {
        List<Tool> toolList = getModel().getConfigurationManager()
                .getSettings().getToolbarTools();
        view.initialize(this, toolList);
    }

    /**
     * Handle Settings Modified (Toolbar) changes. This method also catches
     * SettingsToolsModified messages.
     * 
     * @param loaded
     */
    @Handler
    public void settingsChanged(SettingsModified loaded) {

        ISessionConfigurationManager<ObservedSettings> configManager = getModel()
                .getConfigurationManager();
        ISettings currentSettings = configManager.getSettings();

        getView().setTools(currentSettings.getToolbarTools());
    }

    /**
     * Show a tool subview that is used to gather parameter values for a tool
     * that is to be executed.
     * 
     * @param eventType
     *            The type of the event that this tool is to create; if present,
     *            the tool is being run as a result of a hazard-type-first
     *            invocation. Otherwise, it will be <code>null</code>.
     * @param tool
     *            The tool for which parameters are to be gathered.
     * @param jsonParams
     *            JSON string giving the parameters for this subview. Within the
     *            set of all fields that are defined by these parameters, all
     *            the fields (megawidget specifiers) must have unique
     *            identifiers.
     */
    public void showToolParameterGatherer(Tool tool, String eventType,
            String jsonParams) {
        getView().showToolParameterGatherer(tool, eventType, jsonParams);

    }

    @Override
    protected void reinitialize(IToolsView<?, ?> view) {

        /*
         * No action.
         */
    }
}

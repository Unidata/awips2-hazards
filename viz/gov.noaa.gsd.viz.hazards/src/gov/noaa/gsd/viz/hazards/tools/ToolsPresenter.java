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

import java.util.EnumSet;
import java.util.List;

import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.viz.hazards.sessionmanager.ISessionManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.SettingsModified;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.ObservedSettings;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Tool;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.ToolType;

import gov.noaa.gsd.common.eventbus.BoundedReceptionEventBus;
import gov.noaa.gsd.viz.hazards.display.HazardServicesPresenter;
import net.engio.mbassy.listener.Handler;

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
 * Jan 29, 2015    4375    Dan Schaffer      Console initiation of RVS product generation
 * Jan 30, 2015    3626    Chris.Golden      Added ability to pass event type when
 *                                           running a recommender.
 * Nov 10, 2015   12762    Chris.Golden      Added support for use of new recommender
 *                                           manager.
 * Feb 01, 2017   15556    Chris.Golden      Changed to take advantage of new
 *                                           finer-grained settings change messages.
 * Aug 15, 2017   22757    Chris.Golden      Added ability for recommenders to specify
 *                                           either a message to display, or a dialog to
 *                                           display, with their results (that is, within
 *                                           the returned event set).
 * Sep 27, 2017   38072    Chris.Golden      Changed to work with new recommender manager.
 * Dec 17, 2017   20739    Chris.Golden      Refactored away access to directly mutable
 *                                           session events.
 * Jan 17, 2018   33428    Chris.Golden      Changed to work with new, more flexible
 *                                           toolbar contribution code.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class ToolsPresenter
        extends HazardServicesPresenter<IToolsView<?, ?, ?>> {

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
    public ToolsPresenter(ISessionManager<ObservedSettings> model,
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
    @Deprecated
    public void modelChanged(EnumSet<HazardConstants.Element> changed) {

        /*
         * No action.
         */
    }

    @Handler
    public void settingsChanged(SettingsModified modified) {
        if (modified.getChanged().contains(ObservedSettings.Type.TOOLS)) {
            getView().setTools(modified.getSettings().getToolbarTools());
        }
    }

    /**
     * Show a tool subview that is used to gather parameter values for a tool
     * that is to be executed.
     * 
     * @param type
     *            Type of the tool.
     * @param jsonParams
     *            JSON string giving the parameters for this subview. Within the
     *            set of all fields that are defined by these parameters, all
     *            the fields (megawidget specifiers) must have unique
     *            identifiers.
     */
    public void showToolParameterGatherer(ToolType type, String jsonParams) {
        getView().showToolParameterGatherer(type, jsonParams);
    }

    /**
     * Show a tool subview that is used to display results for a tool that was
     * executed.
     * 
     * @param type
     *            Type of the tool.
     * @param jsonParams
     *            JSON string giving the parameters for this subview. Within the
     *            set of all fields that are defined by these parameters, all
     *            the fields (megawidget specifiers) must have unique
     *            identifiers.
     */
    public void showToolResults(ToolType type, String jsonParams) {
        getView().showToolResults(type, jsonParams);
    }

    // Protected Methods

    @Override
    protected void initialize(IToolsView<?, ?, ?> view) {
        List<Tool> toolList = getModel().getConfigurationManager().getSettings()
                .getToolbarTools();
        view.initialize(this, toolList);
    }

    @Override
    protected void reinitialize(IToolsView<?, ?, ?> view) {

        /*
         * No action.
         */
    }
}

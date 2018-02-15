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

import java.io.Serializable;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.widgets.Display;

import com.google.common.collect.Sets;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.time.TimeRange;
import com.raytheon.uf.viz.hazards.sessionmanager.ISessionManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.SettingsModified;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.ObservedSettings;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Tool;
import com.raytheon.uf.viz.hazards.sessionmanager.tools.ToolExecutionIdentifier;
import com.raytheon.uf.viz.hazards.sessionmanager.tools.ToolParameterDialogSpecifier;
import com.raytheon.uf.viz.hazards.sessionmanager.tools.ToolResultDialogSpecifier;

import gov.noaa.gsd.common.eventbus.BoundedReceptionEventBus;
import gov.noaa.gsd.viz.hazards.UIOriginator;
import gov.noaa.gsd.viz.hazards.display.HazardServicesPresenter;
import net.engio.mbassy.listener.Handler;

/**
 * Settings presenter, used to mediate between the model and the settings view.
 * <p>
 * TODO: Convert to use H.S. MVP style loose coupling between view and presenter
 * (state changers and invokers), which will make this safer for multithreading.
 * </p>
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
 * May 22, 2018    3782    Chris.Golden      Changed to have tool dialog configuration
 *                                           options passed in using dedicated objects and
 *                                           having already been vetted, instead of
 *                                           passing them in as raw maps. Also changed to
 *                                           conform somewhat better to the MVP design 
 *                                           guidelines. Also added ability to change the
 *                                           tool dialog's mutable properties while the
 *                                           dialog is open.
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
     * @param toolIdentifier
     *            Tool execution identifier.
     * @param dialogSpecifier
     *            Specifier of the dialog to be created to gather parameters.
     * @param notifyOfIncrementalChanges
     *            Flag indicating whether or not the recommender manager should
     *            be notified of incremental changes to the dialog as the latter
     *            occur.
     */
    public void showToolParameterGatherer(
            final ToolExecutionIdentifier toolIdentifier,
            final ToolParameterDialogSpecifier dialogSpecifier,
            final boolean notifyOfIncrementalChanges) {

        /*
         * TODO: When reimplementing using loose coupling between presenter and
         * view, there should not be a need to ensure the UI thread is being
         * used anymore.
         */
        if (Display.getDefault().getThread() == Thread.currentThread()) {
            IToolDialogListener listener = new IToolDialogListener() {

                @Override
                public void toolDialogInitialized(
                        final Map<String, Map<String, Object>> mutableProperties) {
                    if (notifyOfIncrementalChanges) {

                        /*
                         * TODO: This should not need to be run explicitly
                         * asynchronously; this would occur implicitly if the
                         * presenter and view were more loosely coupled as per
                         * the MVP design, but until this package is refactored
                         * to conform to the design, this is required so as to
                         * avoid having the "dialog initialized" notification be
                         * propagated before the dialog is fully created.
                         */
                        Display.getDefault().asyncExec(new Runnable() {
                            @Override
                            public void run() {
                                getModel().getRecommenderManager()
                                        .parameterDialogChanged(toolIdentifier,
                                                null, mutableProperties);
                            }
                        });
                    }
                }

                @Override
                public void toolDialogCommandInvoked(String identifier,
                        Map<String, Map<String, Object>> mutableProperties) {
                    if (notifyOfIncrementalChanges) {
                        getModel().getRecommenderManager()
                                .parameterDialogChanged(toolIdentifier,
                                        Sets.newHashSet(identifier),
                                        mutableProperties);
                    }
                }

                @Override
                public void toolDialogStateElementChanged(String identifier,
                        Object state,
                        Map<String, Map<String, Object>> mutableProperties) {
                    if (notifyOfIncrementalChanges) {
                        getModel().getRecommenderManager()
                                .parameterDialogChanged(toolIdentifier,
                                        Sets.newHashSet(identifier),
                                        mutableProperties);
                    }
                }

                @Override
                public void toolDialogStateElementsChanged(
                        Map<String, ?> statesForIdentifiers,
                        Map<String, Map<String, Object>> mutableProperties) {
                    if (notifyOfIncrementalChanges) {
                        getModel().getRecommenderManager()
                                .parameterDialogChanged(toolIdentifier,
                                        statesForIdentifiers.keySet(),
                                        mutableProperties);
                    }
                }

                @Override
                public void toolDialogVisibleTimeRangeChanged(String identifier,
                        long lower, long upper) {
                    getModel().getTimeManager().setVisibleTimeRange(
                            new TimeRange(lower, upper),
                            UIOriginator.TOOL_DIALOG);
                }

                @Override
                public void toolDialogClosed(
                        Map<String, Serializable> statesForIdentifiers,
                        boolean cancelled) {
                    if (cancelled) {
                        getModel().getRecommenderManager()
                                .parameterDialogCancelled(toolIdentifier);
                    } else {
                        getModel().getRecommenderManager()
                                .parameterDialogComplete(toolIdentifier,
                                        statesForIdentifiers);
                    }
                }
            };
            getView().showToolParameterGatherer(dialogSpecifier, listener);
        } else {
            Display.getDefault().asyncExec(new Runnable() {

                @Override
                public void run() {
                    showToolParameterGatherer(toolIdentifier, dialogSpecifier,
                            notifyOfIncrementalChanges);
                }
            });
        }
    }

    /**
     * Update the tool subview that is being used to gather parameter values for
     * a tool that is to be executed.
     * 
     * @param toolIdentifier
     *            Tool execution identifier.
     * @param changedMutableProperties
     *            Mutable properties of the subview that have changed.
     */
    public void updateToolParameterGatherer(
            final ToolExecutionIdentifier toolIdentifier,
            final Map<String, Map<String, Object>> changedMutableProperties) {

        /*
         * TODO: When reimplementing using loose coupling between presenter and
         * view, there should not be a need to ensure the UI thread is being
         * used anymore.
         */
        if (Display.getDefault().getThread() == Thread.currentThread()) {
            getView().updateToolParameterGatherer(changedMutableProperties);
        } else {
            Display.getDefault().asyncExec(new Runnable() {

                @Override
                public void run() {
                    updateToolParameterGatherer(toolIdentifier,
                            changedMutableProperties);
                }
            });
        }
    }

    /**
     * Show a tool subview that is used to display results for a tool that was
     * executed.
     * 
     * @param toolIdentifier
     *            Tool execution identifier.
     * @param dialogSpecifier
     *            Specifier of the dialog to be created to gather parameters.
     */
    public void showToolResults(final ToolExecutionIdentifier toolIdentifier,
            final ToolResultDialogSpecifier dialogSpecifier) {

        /*
         * TODO: When reimplementing using loose coupling between presenter and
         * view, there should not be a need to ensure the UI thread is being
         * used anymore.
         */
        if (Display.getDefault().getThread() == Thread.currentThread()) {
            IToolDialogListener listener = new IToolDialogListener() {

                @Override
                public void toolDialogInitialized(
                        Map<String, Map<String, Object>> mutableProperties) {
                }

                @Override
                public void toolDialogCommandInvoked(String identifier,
                        Map<String, Map<String, Object>> mutableProperties) {
                }

                @Override
                public void toolDialogStateElementChanged(String identifier,
                        Object state,
                        Map<String, Map<String, Object>> mutableProperties) {
                }

                @Override
                public void toolDialogStateElementsChanged(
                        Map<String, ?> statesForIdentifiers,
                        Map<String, Map<String, Object>> mutableProperties) {
                }

                @Override
                public void toolDialogVisibleTimeRangeChanged(String identifier,
                        long lower, long upper) {
                    getModel().getTimeManager().setVisibleTimeRange(
                            new TimeRange(lower, upper),
                            UIOriginator.TOOL_DIALOG);
                }

                @Override
                public void toolDialogClosed(
                        Map<String, Serializable> statesForIdentifiers,
                        boolean cancelled) {
                    getModel().getRecommenderManager()
                            .resultDialogClosed(toolIdentifier);
                }
            };
            getView().showToolResults(dialogSpecifier, listener);
        } else {
            Display.getDefault().asyncExec(new Runnable() {

                @Override
                public void run() {
                    showToolResults(toolIdentifier, dialogSpecifier);
                }
            });
        }
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

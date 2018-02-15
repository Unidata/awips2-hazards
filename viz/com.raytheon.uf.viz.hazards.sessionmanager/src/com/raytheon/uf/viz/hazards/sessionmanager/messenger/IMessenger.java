/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package com.raytheon.uf.viz.hazards.sessionmanager.messenger;

import java.util.Map;

import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEventView;
import com.raytheon.uf.viz.hazards.sessionmanager.tools.ToolExecutionIdentifier;
import com.raytheon.uf.viz.hazards.sessionmanager.tools.ToolParameterDialogSpecifier;
import com.raytheon.uf.viz.hazards.sessionmanager.tools.ToolResultDialogSpecifier;

import gov.noaa.gsd.common.visuals.VisualFeaturesList;

/**
 * Description: Provides access to tools for alerting the user and retrieving
 * yes/no responses from the user. This allows the Session Manager and other
 * Hazard Services managers access to a common way of creating message dialogs
 * which can be easily stubbed for testing purposes. It also prevents a
 * dependency on the gov.noaa.gsd.viz.hazards plugin.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Dec 29, 2013            Bryon.Lawrence      Initial creation
 * Feb 28, 2015   3847     mpduff      Add Rise Crest Fall editor
 * May 14, 2015   7560     mpduff      Added apply callback.
 * Nov 10, 2015  12762     Chris.Golden Added tool parameter gatherer inner
 *                                      class to support the new recommender
 *                                      manager.
 * Nov 23, 2015  13017     Chris.Golden Added ability to specify a many-line
 *                                      message below the main message for a
 *                                      question being asked.
 * Jun 23, 2016  19537     Chris.Golden Changed to work with new generic
 *                                      tool spatial info gathering.
 * Jan 18, 2017  27671     dgilling     Added tri-state method to
 *                                      IQuestionAnswerer.
 * Jan 31, 2017  28013     dgilling     Added "feature disabled for simulated
 *                                      time reasons" warning method.
 * Jun 26, 2017  19207     Chris.Golden Changes to view products for specific
 *                                      events. Also added warnings/TODOs
 *                                      concerning use of the provided
 *                                      interfaces' methods that return
 *                                      something from outside the UI thread.
 * Aug 15, 2017  22757     Chris.Golden Added method to display tool execution
 *                                      results.
 * Sep 27, 2017  38072     Chris.Golden Changed methods used to get tool
 *                                      parameters or show tool results to
 *                                      use new arguments.
 * Dec 17, 2017  20739     Chris.Golden Refactored away access to directly
 *                                      mutable session events.
 * Apr 24, 2018  22308     Chris.Golden Changed product viewer to work with
 *                                      viewing of products coming from text
 *                                      database.
 * May 22, 2018   3782     Chris.Golden Changed recommender parameter gathering
 *                                      to be much more flexible, allowing the
 *                                      user to change dialog parameters together
 *                                      with visual features, and allowing visual
 *                                      feature changes to be made multiple times
 *                                      before the execution proceeds.
 * </pre>
 * 
 * @author Bryon.Lawrence
 * @version 1.0
 */
public interface IMessenger {

    /**
     * Tri-state pseudo-boolean.
     */
    public enum TriStateBoolean {
        TRUE, FALSE, CANCEL
    }

    /**
     * Interface defining a warner
     */
    public interface IWarner {

        /**
         * Display a warning message to the user. If the calling thread is the
         * UI thread, this will be done synchronously, that is, the method will
         * not return until the dialog is closed.
         */
        public void warnUser(String title, String warning);

        /**
         * Display a warning message to the user asynchronously, regardless of
         * whether the calling thread is the UI thread or not (unlike
         * {@link #warnUser(String, String)}).
         * 
         * @param title
         * @param warning
         * @param afterClosing
         *            Optional parameter; if provided, it is executed following
         *            the closing of the dialog.
         */
        public void warnUserAsynchronously(String title, String warning,
                Runnable afterClosing);

        public void warnUserOfSimulatedTimeProblem(String disabledFeature);
    }

    /**
     * Interface defining a questioner.
     * <p>
     * TODO: This is dangerous; the model (session manager) should never be
     * calling provided methods, as they must be run within the UI thread, and
     * this would mean that when a separate worker thread is used for the
     * session manager, that thread would block on the UI thread. Reimplement
     * use of these methods so that they are never required within the session
     * manager.
     * </p>
     */
    public interface IQuestionAnswerer {
        public boolean getUserAnswerToQuestion(String question);

        TriStateBoolean getUserAnswerToQuestionTriState(String question);

        public boolean getUserAnswerToQuestion(String question,
                String[] buttonLabels);

        public boolean getUserAnswerToQuestion(String baseQuestion,
                String potentiallyLongMessage, String[] buttonLabels);
    }

    /**
     * Interface allowing the user to continue or cancel an operation based on a
     * question.
     * <p>
     * TODO: This is dangerous; the model (session manager) should never be
     * calling provided methods, as they must be run within the UI thread, and
     * this would mean that when a separate worker thread is used for the
     * session manager, that thread would block on the UI thread. Reimplement
     * use of these methods so that they are never required within the session
     * manager.
     * </p>
     */
    public interface IContinueCanceller {
        public boolean getUserAnswerToQuestion(String title, String question);
    }

    /**
     * Interface allowing the user to edit rise-crest-fall information for the
     * specified hazard event graphically.
     * <p>
     * TODO: This is dangerous; the model (session manager) should never be
     * calling provided methods, as they must be run within the UI thread, and
     * this would mean that when a separate worker thread is used for the
     * session manager, that thread would block on the UI thread. Reimplement
     * use of these methods so that they are never required within the session
     * manager.
     * </p>
     */
    public interface IRiseCrestFallEditor {
        public IHazardEventView getRiseCrestFallEditor(IHazardEventView event,
                IEventApplier applier);
    }

    /**
     * Interface allowing the user to select products to be viewed or corrected.
     */
    public interface IProductViewerChooser {

        /**
         * Show the user the product viewer chooser.
         */
        public void getProductViewerChooser();
    }

    /**
     * Interface allowing the user to gather information for a tool.
     */
    public interface IToolParameterGatherer {

        /**
         * Get the parameters for the specified tool execution.
         * 
         * @param identifier
         *            Identifier of the tool execution.
         * @param dialogSpecifier
         *            Specifier of the dialog to be created to gather
         *            parameters.
         * @param notifyOfIncrementalChanges
         *            Flag indicating whether or not notifications of
         *            incremental changes to the parameters should be forwarded
         *            to the model.
         */
        public void getToolParameters(ToolExecutionIdentifier identifier,
                ToolParameterDialogSpecifier dialogSpecifier,
                boolean notifyOfIncrementalChanges);

        /**
         * Update parameters for the specified tool execution.
         * 
         * @param identifier
         *            Identifier of the tool execution.
         * @param changedMutableProperties
         *            Map of mutable properties of the dialog that have been
         *            changed.
         */
        public void updateToolParameters(ToolExecutionIdentifier identifier,
                Map<String, Map<String, Object>> changedMutableProperties);

        /**
         * Get spatial input for the specified tool execution.
         * 
         * @param identifier
         *            Identifier of the tool execution.
         * @param visualFeatures
         *            List of visual features to be used to get spatial input
         *            from the user.
         */
        public void getToolSpatialInput(ToolExecutionIdentifier identifier,
                VisualFeaturesList visualFeatures);

        /**
         * Finish gathering spatial input for the specified tool execution. This
         * must be called following any series of calls to
         * {@link IToolParameterGatherer#getToolSpatialInput(ToolExecutionIdentifier, VisualFeaturesList)}
         * in order to remove the visual features that were put in place by the
         * last call to that method.
         * 
         * @param identifier
         *            Identifier of the tool execution.
         */
        public void finishToolSpatialInput(ToolExecutionIdentifier identifier);

        /**
         * Show the results for the specified tool execution.
         * 
         * @param identifier
         *            Identifier of the tool execution.
         * @param dialogSpecifier
         *            Specifier of the dialog to be created to show the results.
         */
        public void showToolResults(ToolExecutionIdentifier identifier,
                ToolResultDialogSpecifier dialogSpecifier);
    }

    /**
     * Interface for applying the Graphical Editor changes without closing.
     */
    public interface IEventApplier {
        public void apply(IHazardEventView event);
    }

    /**
     * Get a question/answer.
     * 
     * @param
     * @return `A question/answer. The implementation will allow a question to
     *         be asked of the user and a boolean answer to be returned. This
     *         can be easily stubbed for testing.
     */
    public IQuestionAnswerer getQuestionAnswerer();

    /**
     * Get a warner.
     * 
     * @param
     * @return A warner. The implementation will allow a warning to be displayed
     *         to the user. This can be easily stubbed for testing.
     */
    public IWarner getWarner();

    /**
     * Get a continue/canceller.
     * 
     * @param
     * @return A continue/canceller. The implementation will allow a question to
     *         be displayed and give the forecaster the ability to continue or
     *         cancel the current operation.
     */
    public IContinueCanceller getContinueCanceller();

    /**
     * Get a rise-crest-fall editor.
     * 
     * @return A rise-crest-fall editor. The implementation will allow a
     *         graphical editor to be displayed for a specified event, and give
     *         the forecaster the ability to manipulate the rise-crest-fall
     *         values for that event.
     */
    public IRiseCrestFallEditor getRiseCrestFallEditor(IHazardEventView event);

    /**
     * Get a product viewer chooser.
     * 
     * @return A product viewer chooser. The implementaiton will allow a list of
     *         product information to be displayed so that the forecaster may
     *         choose to view or correct a product.
     */
    public IProductViewerChooser getProductViewerChooser();

    /**
     * Get a tool parameter gatherer.
     * 
     * @return Tool parameter gatherer. The implementation will allow a dialog
     *         to be displayed comprised of megawidgets specified by the caller
     *         in order to gather parameters for the running of a tool.
     */
    public IToolParameterGatherer getToolParameterGatherer();
}

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

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.hazards.productgen.data.ProductData;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.ToolType;
import com.raytheon.uf.viz.hazards.sessionmanager.recommenders.ISessionRecommenderManager;

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
 * </pre>
 * 
 * @author Bryon.Lawrence
 * @version 1.0
 */
public interface IMessenger {

    /**
     * Interface defining a warner
     */
    public interface IWarner {
        public void warnUser(String title, String warning);
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
        public IHazardEvent getRiseCrestFallEditor(IHazardEvent event,
                IEventApplier applier);
    }

    /**
     * Interface allowing the user to select products to be viewed or corrected.
     */
    public interface IProductViewerChooser {

        /**
         * Get the products to be viewed or corrected.
         * 
         * @param productData
         *            Data about the products to be shown and potentially
         *            chosen.
         */
        public void getProductViewerChooser(List<ProductData> productData);
    }

    /**
     * Interface allowing the user to gather information for a tool.
     */
    public interface IToolParameterGatherer {

        /**
         * Get the parameters for the specified type of tool.
         * 
         * @param type
         *            Type of the tool.
         * @param dialogInput
         *            Map holding the parameters governing the contents of the
         *            dialog to be created to gather the parameters.
         * @param dialogParametersReceiver
         *            Receiver to be passed parameters that the user chooses in
         *            the dialog.
         */
        public void getToolParameters(ToolType type,
                Map<String, Serializable> dialogInput,
                ISessionRecommenderManager.IDialogParametersReceiver dialogParametersReceiver);

        /**
         * Get spatial input for the specified type of tool.
         * 
         * @param type
         *            Type of the tool.
         * @param visualFeatures
         *            List of visual features to be used to get spatial input
         *            from the user.
         * @param spatialParametersReceiver
         *            Receiver to be passed parameters that the user chooses in
         *            the spatial display.
         */
        public void getToolSpatialInput(ToolType type,
                VisualFeaturesList visualFeatures,
                ISessionRecommenderManager.ISpatialParametersReceiver spatialParametersReceiver);

        /**
         * Show the results for for the specified type of tool.
         * 
         * @param type
         *            Type of the tool.
         * @param dialogResults
         *            Map holding the parameters governing the contents of the
         *            dialog to be created to show the results.
         * @param displayCompleteNotifier
         *            Notifier to be told when the tool results display is
         *            complete.
         */
        public void showToolResults(ToolType type,
                Map<String, Serializable> dialogResults,
                ISessionRecommenderManager.IResultsDisplayCompleteNotifier displayCompleteNotifier);
    }

    /**
     * Interface for applying the Graphical Editor changes without closing.
     */
    public interface IEventApplier {
        public void apply(IHazardEvent event);
    }

    /**
     * Returns a question/answer.
     * 
     * @param
     * @return `A question/answer. The implementation will allow a question to
     *         be asked of the user and a boolean answer to be returned. This
     *         can be easily stubbed for testing.
     */
    public IQuestionAnswerer getQuestionAnswerer();

    /**
     * Returns a warner.
     * 
     * @param
     * @return A warner. The implementation will allow a warning to be displayed
     *         to the user. This can be easily stubbed for testing.
     */
    public IWarner getWarner();

    /**
     * Returns a continue/canceller.
     * 
     * @param
     * @return A continue/canceller. The implementation will allow a question to
     *         be displayed and give the forecaster the ability to continue or
     *         cancel the current operation.
     */
    public IContinueCanceller getContinueCanceller();

    /**
     * Returns a rise-crest-fall editor.
     * 
     * @return A rise-crest-fall editor. The implementation will allow a
     *         graphical editor to be displayed for a specified event, and give
     *         the forecaster the ability to manipulate the rise-crest-fall
     *         values for that event.
     */
    public IRiseCrestFallEditor getRiseCrestFallEditor(IHazardEvent event);

    /**
     * Returns a product viewer chooser.
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

/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package com.raytheon.uf.viz.hazards.sessionmanager.recommenders;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.raytheon.uf.common.recommenders.EventRecommender;
import com.raytheon.uf.viz.hazards.sessionmanager.tools.ToolExecutionIdentifier;

import gov.noaa.gsd.common.visuals.VisualFeaturesList;

/**
 * Description: Interface describing the methods that must be implemented in
 * order to create a session recommender manager, used to manage the running of
 * recommenders.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Oct 23, 2015   12762    Chris.Golden Initial creation.
 * Mar 04, 2016   15933    Chris.Golden Added ability to run multiple recommenders in
 *                                      sequence in response to a time interval trigger,
 *                                      instead of just one recommender.
 * Jun 23, 2016   19537    Chris.Golden Changed to use visual features for spatial
 *                                      info collection.
 * May 31, 2017   34684    Chris.Golden Moved recommender-specific methods to the
 *                                      session recommender manager where they belong.
 * Aug 15, 2017   22757    Chris.Golden Added ability for recommenders to specify either
 *                                      a message to display, or a dialog to display,
 *                                      with their results (that is, within the returned
 *                                      event set).
 * Sep 27, 2017   38072    Chris.Golden Removed methods that should not be public, and
 *                                      added interfaces to be used to provide callback
 *                                      objects for dialog and spatial input, and for
 *                                      the displaying of results.
 * May 22, 2018    3782    Chris.Golden Changed recommender parameter gathering to be
 *                                      much more flexible, allowing the user to change
 *                                      dialog parameters together with visual features,
 *                                      and allowing visual feature changes to be made
 *                                      multiple times before the execution proceeds.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public interface ISessionRecommenderManager {

    // Public Interfaces

    /**
     * Interface specifying the methods that must be implemented in order to
     * function as a spatial parameters receiver. The latter is a class that
     * responds to the user manipulating recommender-provided visual features
     * via interaction with a spatial display prior to the recommender's
     * execution.
     */
    public interface ISpatialParametersReceiver {

        /**
         * Receive the specified spatial parameters.
         * 
         * @param parameters
         *            Visual features that have been manipulated to provide
         *            input parameters, or <code>null</code> if the user
         *            canceled the running of the recommender that originally
         *            asked for input parameters.
         */
        void receiveSpatialParameters(VisualFeaturesList parameters);
    }

    /**
     * Interface specifying the methods that must be implemented in order to
     * function as a notifier of a running recommender's results display being
     * complete. The latter is a class that responds to the user completing
     * viewing of the results of the currently running recommender by notifying
     * the recommender manager of the completion.
     */
    public interface IResultsDisplayCompleteNotifier {

        /**
         * Handle the completion of the display of recommender results.
         */
        void resultsDisplayCompleted();
    }

    // Public Methods

    /**
     * Get the specified event recommender.
     * 
     * @param recommenderIdentifier
     *            Identifier of the desired recommender.
     * @return Recommender.
     */
    public EventRecommender getRecommender(String recommenderIdentifier);

    /**
     * Run the specified recommender. If parameters must be gathered from the
     * user via spatial or dialog input, this method will do so. If not, it will
     * simply run the recommender.
     * 
     * @param recommenderIdentifier
     *            Identifier of the recommender to be run.
     * @param context
     *            Execution context in which to run the recommender.
     */
    public void runRecommender(String recommenderIdentifier,
            RecommenderExecutionContext context);

    /**
     * Run the specified recommenders sequentially, waiting for the first to
     * complete before running the second and so on. If parameters must be
     * gathered from the user via spatial or dialog input, this method will do
     * so. If not, it will simply run the recommender.
     * 
     * @param recommenderIdentifiers
     *            Identifiers of the recommenders to be run, in the order in
     *            which they should be run.
     * @param context
     *            Execution context in which to run the recommenders.
     */
    public void runRecommenders(List<String> recommenderIdentifiers,
            RecommenderExecutionContext context);

    /**
     * Receive notification that a command was invoked within the user interface
     * that may require a recommender to be run in response.
     * 
     * @param eventIdentifier
     *            Identifier of the hazard event for which to run a recommender,
     *            if any.
     * @param commandIdentifier
     *            Identifier of the command that was invoked.
     */
    public void eventCommandInvoked(String eventIdentifier,
            String commandIdentifier);

    /**
     * Receive notification that tool dialog parameters have changed in some
     * way.
     * 
     * @param identifier
     *            Identifier of the tool execution for which the dialog was
     *            instantiated.
     * @param parameterIdentifiers
     *            Identifiers of the dialog parameters that triggered this
     *            invocation. If <code>null</code>, nothing has changed; the
     *            dialog is merely providing its mutable properties to the
     *            manager at initialization time.
     * @param mutableProperties
     *            Mutable properties of the dialog.
     */
    public void parameterDialogChanged(ToolExecutionIdentifier identifier,
            Collection<String> parameterIdentifiers,
            Map<String, Map<String, Object>> mutableProperties);

    /**
     * Receive notification that tool dialog-based parameter gathering is
     * complete.
     * 
     * @param identifier
     *            Identifier of the tool execution for which the dialog was
     *            instantiated.
     * @param valuesForParameters
     *            Map pairing parameter identifiers with their values.
     */
    public void parameterDialogComplete(ToolExecutionIdentifier identifier,
            Map<String, Serializable> valuesForParameters);

    /**
     * Receive notification that tool dialog-based parameter gathering has been
     * cancelled.
     * 
     * @param identifier
     *            Identifier of the tool execution for which the dialog was
     *            instantiated.
     */
    public void parameterDialogCancelled(ToolExecutionIdentifier identifier);

    /**
     * Receive notification that tool spatial parameters have changed in some
     * way.
     * 
     * @param identifier
     *            Identifier of the tool execution for which the spatial
     *            parameters have changed.
     * @param parameterIdentifiers
     *            Identifiers of the parameters that have changed.
     * @param parameters
     *            Tool spatial parameters.
     */
    public void spatialParametersChanged(ToolExecutionIdentifier identifier,
            Collection<String> parameterIdentifiers,
            VisualFeaturesList parameters);

    /**
     * Receive notification that tool dialog showing results has been closed.
     * 
     * @param identifier
     *            Identifier of the tool execution for which the dialog was
     *            instantiated.
     */
    public void resultDialogClosed(ToolExecutionIdentifier identifier);

    /**
     * Shut the manager down.
     */
    public void shutdown();
}

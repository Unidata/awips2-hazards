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
import java.util.List;
import java.util.Map;

import com.raytheon.uf.common.recommenders.EventRecommender;

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
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public interface ISessionRecommenderManager {

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
     * Add the specified event identifier to the set of events that have been
     * removed. These are tracked so that if a recommmender is running while
     * such an event is removed, and the recommender returns a result including
     * a modified version of the event, the modification is ignored instead of
     * re-adding the event to the session.
     * 
     * @param eventIdentifier
     *            Identifier to be added.
     */
    public void rememberRemovedEventIdentifier(String eventIdentifier);

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
     * Run the specified recommender in the specified context and with the
     * specified user-provided dialog parameters.
     * 
     * @param recommenderIdentifier
     *            Identifier of the recommender to be run.
     * @param context
     *            Execution context in which to run the recommender.
     * @param visualFeatures
     *            List of visual features provided by the recommender earlier to
     *            allow the user to input spatial info, if any.
     * @param dialogInfo
     *            Map of dialog parameters, if any.
     */
    public void runRecommender(String recommenderIdentifier,
            RecommenderExecutionContext context,
            VisualFeaturesList visualFeatures,
            Map<String, Serializable> dialogInfo);

    /**
     * Handle the completion of the viewing of recommender results.
     * 
     * @param recommenderIdentifier
     *            Identifier of the recommender that was run.
     * @param context
     *            Execution context in which the recommender was run.
     */
    public void handleResultsDisplayComplete(String recommenderIdentifier,
            RecommenderExecutionContext context);

    /**
     * Shut down the recommenders.
     */
    public void shutdown();
}

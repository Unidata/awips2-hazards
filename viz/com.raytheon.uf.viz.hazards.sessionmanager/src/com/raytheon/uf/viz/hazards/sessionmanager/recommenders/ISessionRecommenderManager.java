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
     * Run the specified recommender. If parameters must be gathered from the
     * user via spatial or dialog input, this method will do so. If not, it will
     * simply run the recommender.
     * 
     * @param recommenderIdentifier
     *            The identifier of the recommender to be run.
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
     *            The identifiers of the recommenders to be run, in the order in
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
     *            The identifier of the recommender to be run.
     * @param context
     *            Execution context in which to run the recommender.
     * @param spatialInfo
     *            Map of spatial parameters, if any.
     * @param dialogInfo
     *            Map of dialog parameters, if any.
     */
    public void runRecommender(String recommenderIdentifier,
            RecommenderExecutionContext context,
            Map<String, Serializable> spatialInfo,
            Map<String, Serializable> dialogInfo);

    /**
     * Shut down the recommenders.
     */
    public void shutdown();
}

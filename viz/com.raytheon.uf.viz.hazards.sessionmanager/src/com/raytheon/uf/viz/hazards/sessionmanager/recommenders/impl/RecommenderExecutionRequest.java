/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Evaluation & Decision Support Branch (EDS)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package com.raytheon.uf.viz.hazards.sessionmanager.recommenders.impl;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.raytheon.uf.viz.hazards.sessionmanager.recommenders.RecommenderExecutionContext;

import gov.noaa.gsd.common.utilities.IMergeable;
import gov.noaa.gsd.common.utilities.MergeResult;

/**
 * Description: Execution request which to run one or more recommenders.
 * <p>
 * TODO: Consider putting this in a new package for tools, and renaming it
 * ToolExecutionRequest, so that it is not only for recommenders.
 * </p>
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Sep 26, 2017   38072    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
class RecommenderExecutionRequest
        implements IMergeable<RecommenderExecutionRequest> {

    // Private Variables

    /**
     * Recommender execution context.
     */
    private final RecommenderExecutionContext context;

    /**
     * Recommenders to be run, in the order specified, using this context.
     */
    private final List<String> recommenderIdentifiers;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param context
     *            Context in which to run the recommender(s).
     * @param recommenderIdentifiers
     *            Identifiers of the recommenders to be run in this context.
     */
    public RecommenderExecutionRequest(RecommenderExecutionContext context,
            List<String> recommenderIdentifiers) {
        this.context = context;
        this.recommenderIdentifiers = ImmutableList
                .copyOf(recommenderIdentifiers);
    }

    // Public Methods

    /**
     * Get the context.
     * 
     * @return Context.
     */
    public RecommenderExecutionContext getContext() {
        return context;
    }

    /**
     * Get the identifiers of the recommenders to be run. Note that the returned
     * list is not modifiable.
     * 
     * @return Identifiers of the recommenders to be run.
     */
    public List<String> getRecommenderIdentifiers() {
        return recommenderIdentifiers;
    }

    /**
     * Merge the specified request with this one (the subject) if possible,
     * returning the result. Note that for this implementation, there are only
     * two possible results: a merge failure, or a merge success with the object
     * being nullified.
     * 
     * @param original
     *            Object request.
     * @param modified
     *            Ignored for this implementation.
     * @return Result of the attempt.
     */
    @Override
    public MergeResult<RecommenderExecutionRequest> merge(
            RecommenderExecutionRequest original,
            RecommenderExecutionRequest modified) {
        if (getRecommenderIdentifiers()
                .equals(original.getRecommenderIdentifiers())) {
            MergeResult<RecommenderExecutionContext> result = getContext()
                    .merge(original.getContext(), modified.getContext());
            if (result.isSuccess()) {
                return IMergeable.getSuccessObjectCancellationResult(
                        new RecommenderExecutionRequest(
                                result.getSubjectReplacement(),
                                getRecommenderIdentifiers()));
            } else {
                return IMergeable.getFailureResult();
            }
        } else {
            return IMergeable.getFailureResult();
        }
    }
}

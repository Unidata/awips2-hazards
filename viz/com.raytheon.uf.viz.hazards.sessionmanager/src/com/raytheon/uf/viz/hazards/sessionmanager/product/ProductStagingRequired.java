/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package com.raytheon.uf.viz.hazards.sessionmanager.product;

import com.raytheon.uf.viz.hazards.sessionmanager.ISessionNotification;
import com.raytheon.uf.viz.hazards.sessionmanager.product.ISessionProductManager.StagingRequired;

/**
 * Description: Notification indicating that product staging is required.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Oct 08, 2014    4042    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class ProductStagingRequired implements ISessionNotification {

    // Private Variables

    /**
     * Flag indicating whether or not the product staging requirement is a
     * result of an issue command. If false, it was prompted by a preview
     * command.
     */
    private final boolean issue;

    /**
     * Product staging required.
     */
    private final StagingRequired stagingRequired;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param issue
     *            Flag indicating whether or not the product staging requirement
     *            is a result of an issue command. If false, it was prompted by
     *            a preview command.
     * @param stagingRequired
     *            Product staging required; must be either
     *            {@link StagingRequired#POSSIBLE_EVENTS} or
     *            {@link StagingRequired#PRODUCT_SPECIFIC_INFO}.
     */
    public ProductStagingRequired(boolean issue, StagingRequired stagingRequired) {
        this.issue = issue;
        this.stagingRequired = stagingRequired;
    }

    // Public Methods

    /**
     * Determine whether or not the staging is required as a result of an issue
     * command. If false, it is the result of a preview command.
     * 
     * @return Flag indicating whether or not the staging required is a result
     *         of an issue command.
     */
    public boolean isIssue() {
        return issue;
    }

    /**
     * Get the staging required.
     * 
     * @return Staging required; will be either
     *         {@link StagingRequired#POSSIBLE_EVENTS} or
     *         {@link StagingRequired#PRODUCT_SPECIFIC_INFO}.
     */
    public StagingRequired getStagingRequired() {
        return stagingRequired;
    }
}

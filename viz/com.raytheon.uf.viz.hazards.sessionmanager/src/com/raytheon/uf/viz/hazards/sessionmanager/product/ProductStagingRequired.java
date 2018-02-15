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

import gov.noaa.gsd.common.utilities.IMergeable;
import gov.noaa.gsd.common.utilities.MergeResult;

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
 * Feb 24, 2016   13929    Robert.Blum  Remove first part of staging dialog.
 * Mar 21, 2017   29996    Robert.Blum  Removed stagingRequired member var.
 * Sep 27, 2017   38072    Chris.Golden Implemented merge() method.
 * Dec 07, 2017   41886    Chris.Golden Removed Java 8/JDK 1.8 usage.
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

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param issue
     *            Flag indicating whether or not the product staging requirement
     *            is a result of an issue command. If false, it was prompted by
     *            a preview command.
     */
    public ProductStagingRequired(boolean issue) {
        this.issue = issue;
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

    @Override
    public MergeResult<? extends ISessionNotification> merge(
            ISessionNotification originalNotification,
            ISessionNotification modifiedNotification) {
        return IMergeable.Helper.getFailureResult();
    }
}

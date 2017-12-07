/**
 * This software was developed and / or modified by Raytheon Company,
 * pursuant to Contract DG133W-05-CQ-1067 with the US Government.
 * 
 * U.S. EXPORT CONTROLLED TECHNICAL DATA
 * This software product contains export-restricted data whose
 * export/transfer/disclosure is restricted by U.S. law. Dissemination
 * to non-U.S. persons whether in the United States or abroad requires
 * an export license or other authorization.
 * 
 * Contractor Name:        Raytheon Company
 * Contractor Address:     6825 Pine Street, Suite 340
 *                         Mail Stop B8
 *                         Omaha, NE 68106
 *                         402.291.0100
 * 
 * See the AWIPS II Master Rights File ("Master Rights File.pdf") for
 * further licensing information.
 **/
package com.raytheon.uf.viz.hazards.sessionmanager.originator;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.raytheon.uf.viz.hazards.sessionmanager.ISessionNotification;

import gov.noaa.gsd.common.utilities.IMergeable;
import gov.noaa.gsd.common.utilities.MergeResult;
import gov.noaa.gsd.common.utilities.Utils;

/**
 * Session notification implementation containing the originator of the change
 * that prompted the notification. This may be used by handlers of such
 * notifications to decide how they want to respond, allowing for optimizations.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Feb 06, 2014            mnash        Initial creation.
 * Sep 27, 2017   38072    Chris.Golden Implemented merge() method, and added
 *                                      helper methods for subclasses.
 * Dec 07, 2017   41886    Chris.Golden Removed Java 8/JDK 1.8 usage.
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */
public class OriginatedSessionNotification implements ISessionNotification {

    // Private Static Constants

    /**
     * Name of the environment variable indicating whether or not automated
     * tests are enabled.
     */
    private static final String ENVIRONMENT_VARIABLE = "HAZARD_SERVICES_AUTO_TESTS_ENABLED";

    /**
     * Flag indicating whether or not automated tests are enabled.
     */
    private static final boolean AUTOMATED_TESTS_ENABLED;

    static {
        AUTOMATED_TESTS_ENABLED = BooleanUtils
                .toBoolean(System.getenv(ENVIRONMENT_VARIABLE));
    }

    // Private Variables

    /**
     * Originator of the notification.
     */
    private final IOriginator originator;

    /**
     * Context of the notification.
     */
    private String notificationContext;

    // Private Static Methods

    /**
     * Determine whether or not automated tests are enabled.
     * 
     * @return <code>true</code> if automated tests are enabled,
     *         <code>false</code> otherwise.
     */
    private static boolean areAutomatedTestsEnabled() {
        return AUTOMATED_TESTS_ENABLED;
    }

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param originator
     *            Originator of the notification.
     */
    public OriginatedSessionNotification(IOriginator originator) {
        this.originator = originator;
        buildNotificationContext();
    }

    // Public Methods

    /**
     * Get the originator of the notification.
     * 
     * @return Originator.
     */
    public IOriginator getOriginator() {
        return originator;
    }

    /**
     * Get the context of the notification.
     * 
     * @return Context.
     */
    String getNotificationContext() {
        return notificationContext;
    }

    /*
     * By default, these notifications do not merge. Subclasses may override
     * this method to return something other than a failure result if merging
     * behavior is desirable.
     */
    @Override
    public MergeResult<? extends ISessionNotification> merge(
            ISessionNotification original, ISessionNotification modified) {
        return IMergeable.Helper.getFailureResult();
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);

    }

    // Protected Methods

    /**
     * Helper method for subclasses'
     * {@link #merge(ISessionNotification, ISessionNotification)}
     * implementations that returns a merge result nullifying this notification
     * if the latter has the same class and originator as the specified
     * notification. If not, the merge failed result is returned.
     * 
     * @param originalNotification
     *            Notification to be merged with this one, if possible. This is
     *            the original version of the notification, untouched by any
     *            modifications resulting from other invocations of
     *            <code>merge()</code> (for other notifications enqueued before
     *            this one) with it as an argument
     * @param modifiedNotification
     *            Version of <code>originalNotification</code> as modified by
     *            any earlier invocations of <code>merge()</code> with the
     *            former as an argument.
     * @return Result of the merge.
     */
    protected final MergeResult<ISessionNotification> getMergeResultNullifyingSubjectIfSameClassAndOriginator(
            ISessionNotification originalNotification,
            ISessionNotification modifiedNotification) {
        if (getClass().isAssignableFrom(originalNotification.getClass())
                && getOriginator()
                        .equals(((OriginatedSessionNotification) originalNotification)
                                .getOriginator())) {
            return IMergeable.Helper
                    .getSuccessSubjectCancellationResult(modifiedNotification);
        }
        return IMergeable.Helper.getFailureResult();
    }

    // Private Methods

    /**
     * Build the notification context.
     */
    private void buildNotificationContext() {
        if (areAutomatedTestsEnabled()) {
            try {
                throw new RuntimeException();
            } catch (RuntimeException e) {
                notificationContext = Utils.getStackTraceAsString(e);
            }
        } else {
            notificationContext = "Not Available";
        }
    }
}

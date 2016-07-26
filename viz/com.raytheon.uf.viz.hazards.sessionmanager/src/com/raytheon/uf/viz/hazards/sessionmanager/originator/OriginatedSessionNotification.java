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

import gov.noaa.gsd.common.utilities.Utils;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.raytheon.uf.viz.hazards.sessionmanager.ISessionNotification;

/**
 * Contains the originator of what sent this out so that can be filtered upon.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 6, 2014            mnash     Initial creation
 * 
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */

public class OriginatedSessionNotification implements ISessionNotification {

    private IOriginator originator;

    private String notificationContext;

    public OriginatedSessionNotification(IOriginator originator) {
        this.originator = originator;
        buildNotificationContext();
    }

    private static final String ENVIRONMENT_VARIABLE = "HAZARD_SERVICES_AUTO_TESTS_ENABLED";

    /*
     * TODO. Move this.
     */
    private static final boolean automatedTestsEnabled;

    static {
        automatedTestsEnabled = BooleanUtils.toBoolean(System
                .getenv(ENVIRONMENT_VARIABLE));
    }

    public IOriginator getOriginator() {
        return originator;
    }

    public void setOriginator(IOriginator originator) {
        this.originator = originator;
    }

    String getNotificationContext() {
        return notificationContext;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);

    }

    private static boolean areAutomatedTestsEnabled() {
        return automatedTestsEnabled;
    }

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

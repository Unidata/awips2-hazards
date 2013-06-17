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
package com.raytheon.uf.viz.hazards.sessionmanager.config.impl.types;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * 
 * Entry in the hazard types table.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 10, 2013 1257       bsteffen    Initial creation
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public class HazardTypeEntry {

    private String headline;

    private boolean combinableSegments;

    private boolean allowAreaChange;

    private boolean allowTimeChange;

    private int[] expirationTime;

    public String getHeadline() {
        return headline;
    }

    public void setHeadline(String headline) {
        this.headline = headline;
    }

    public boolean isCombinableSegments() {
        return combinableSegments;
    }

    public void setCombinableSegments(boolean combinableSegments) {
        this.combinableSegments = combinableSegments;
    }

    public boolean isAllowAreaChange() {
        return allowAreaChange;
    }

    public void setAllowAreaChange(boolean allowAreaChange) {
        this.allowAreaChange = allowAreaChange;
    }

    public boolean isAllowTimeChange() {
        return allowTimeChange;
    }

    public void setAllowTimeChange(boolean allowTimeChange) {
        this.allowTimeChange = allowTimeChange;
    }

    public int[] getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(int[] expirationTime) {
        this.expirationTime = expirationTime;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
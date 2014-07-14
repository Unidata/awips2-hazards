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
package com.raytheon.uf.common.recommenders.requests;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.serialization.comm.IServerRequest;
import com.raytheon.uf.common.time.TimeRange;

/**
 * Request for EDEX to execute recommenders.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 23, 2013            mnash     Initial creation
 * 
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */
@DynamicSerialize
public class ExecuteRecommenderRequest implements IServerRequest {

    @DynamicSerializeElement
    private String recommenderName;

    @DynamicSerializeElement
    private String site;

    @DynamicSerializeElement
    private TimeRange timeRange;

    /**
     * 
     */
    public ExecuteRecommenderRequest() {
    }

    /**
     * 
     */
    public ExecuteRecommenderRequest(String name) {
        this.recommenderName = name;
    }

    /**
     * @param recommenderName
     *            the recommenderName to set
     */
    public void setRecommenderName(String recommenderName) {
        this.recommenderName = recommenderName;
    }

    /**
     * @return the recommenderName
     */
    public String getRecommenderName() {
        return recommenderName;
    }

    /**
     * @return the site
     */
    public String getSite() {
        return site;
    }

    /**
     * @param site
     *            the site to set
     */
    public void setSite(String site) {
        this.site = site;
    }

    /**
     * @return the timeRange
     */
    public TimeRange getTimeRange() {
        return timeRange;
    }

    /**
     * @param timeRange
     *            the timeRange to set
     */
    public void setTimeRange(TimeRange timeRange) {
        this.timeRange = timeRange;
    }
}

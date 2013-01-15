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
package com.raytheon.uf.common.dataplugin.events.hazards.requests;

import java.util.Map;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.serialization.comm.IServerRequest;

/**
 * The serialized request to be sent to EDEX and which allows the user to
 * specify filters and whether or not to use the practice database for retrieval
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 8, 2012            mnash     Initial creation
 * 
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */
@DynamicSerialize
public class HazardRetrieveRequest implements IServerRequest {

    @DynamicSerializeElement
    private Map<String, Object> filters;

    @DynamicSerializeElement
    private boolean practice;

    /**
     * @return the filters
     */
    public Map<String, Object> getFilters() {
        return filters;
    }

    /**
     * @param filters
     *            the filters to set
     */
    public void setFilters(Map<String, Object> filters) {
        this.filters = filters;
    }

    public void addFilter(String key, String value) {
        this.filters.put(key, value);
    }

    /**
     * @return the mode
     */
    public boolean isPractice() {
        return practice;
    }

    /**
     * @param mode
     *            the mode to set
     */
    public void setPractice(boolean practice) {
        this.practice = practice;
    }
}

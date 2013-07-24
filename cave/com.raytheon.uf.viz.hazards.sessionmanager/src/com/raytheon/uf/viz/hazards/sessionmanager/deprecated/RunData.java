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
package com.raytheon.uf.viz.hazards.sessionmanager.deprecated;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Reverse engineered to represent rundata as JSON.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 22, 2013 1257       bsteffen    Initial creation
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
@Deprecated
public class RunData {

    private String[] eventSet;

    private Map<String, String> spatialInfo;

    private Map<String, Object> dialogInfo;

    public String[] getEventSet() {
        return eventSet;
    }

    public void setEventSet(String[] eventSet) {
        this.eventSet = eventSet;
    }

    public Map<String, String> getSpatialInfo() {
        return spatialInfo;
    }

    public void setSpatialInfo(Map<String, String> spatialInfo) {
        this.spatialInfo = spatialInfo;
    }

    public Map<String, Object> getDialogInfo() {
        return dialogInfo;
    }

    public void setDialogInfo(Map<String, Object> dialogInfo) {
        this.dialogInfo = dialogInfo;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    public Map<String, Serializable> getDialogInfoSerializable() {
        Map<String, Serializable> result = new HashMap<String, Serializable>();
        for (Entry<String, Object> entry : dialogInfo.entrySet()) {
            Object val = entry.getValue();
            if (val instanceof Serializable) {
                result.put(entry.getKey(), (Serializable) val);
            } else {
                throw new RuntimeException(entry + ", "
                        + val.getClass().getSimpleName()
                        + " does not implement Serializable");
            }
        }
        return result;
    }

}

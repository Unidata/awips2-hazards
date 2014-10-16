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
package com.raytheon.uf.common.dataplugin.events.hazards.interoperability.requests;

import java.util.List;
import java.util.Map;

import com.raytheon.uf.common.serialization.ISerializableObject;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * TODO Add Description
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 18, 2014            jsanchez     Initial creation
 * 
 * </pre>
 * 
 * @author jsanchez
 * @version 1.0
 */
@DynamicSerialize
public class VtecInteroperabilityWarningResponse implements
        ISerializableObject, IReturnResults {

    @DynamicSerializeElement
    private List<Map<String, Object>> warnings;

    @DynamicSerializeElement
    private String phensig;

    @DynamicSerializeElement
    private boolean success;

    @DynamicSerializeElement
    private String exceptionText;

    public List<Map<String, Object>> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<Map<String, Object>> warnings) {
        this.warnings = warnings;
    }

    public String getPhensig() {
        return phensig;
    }

    public void setPhensig(String phensig) {
        this.phensig = phensig;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getExceptionText() {
        return exceptionText;
    }

    public void setExceptionText(String exceptionText) {
        this.exceptionText = exceptionText;
    }

    @Override
    public List<Map<String, Object>> getResults() {
        // TODO Auto-generated method stub
        return warnings;
    }

}

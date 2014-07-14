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
package com.raytheon.uf.common.hazards.productgen.editable;

import java.util.List;

import com.raytheon.uf.common.serialization.ISerializableObject;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * The response received from the service as to what was retrieved from the
 * database.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 26, 2013            mnash     Initial creation
 * 
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */
@DynamicSerialize
public class ProductTextResponse implements ISerializableObject {

    @DynamicSerializeElement
    private List<ProductText> text;

    @DynamicSerializeElement
    private Throwable exceptions;

    /**
     * 
     */
    public ProductTextResponse() {
    }

    /**
     * @return the text
     */
    public List<ProductText> getText() {
        return text;
    }

    /**
     * @param text
     *            the text to set
     */
    public void setText(List<ProductText> text) {
        this.text = text;
    }

    /**
     * @return the exceptions
     */
    public Throwable getExceptions() {
        return exceptions;
    }

    /**
     * @param exceptions
     *            the exceptions to set
     */
    public void setExceptions(Throwable exceptions) {
        this.exceptions = exceptions;
    }
}

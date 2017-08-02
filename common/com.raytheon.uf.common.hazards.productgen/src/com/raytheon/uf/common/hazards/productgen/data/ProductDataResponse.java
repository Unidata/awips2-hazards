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
package com.raytheon.uf.common.hazards.productgen.data;

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
 * Apr 11, 2014            jsanchez     Initial creation
 * Nov 11, 2016 22119      Kevin.Bisanz Change type of this.exceptions from
 *                                      Throwable to Exception
 * 
 * </pre>
 * 
 * @author jsanchez
 * @version 1.0
 */
@DynamicSerialize
public class ProductDataResponse implements ISerializableObject {

    @DynamicSerializeElement
    private List<ProductData> data;

    @DynamicSerializeElement
    private Exception exceptions;

    /**
     * 
     */
    public ProductDataResponse() {
    }

    /**
     * @return the data
     */
    public List<ProductData> getData() {
        return data;
    }

    /**
     * @param data
     *            the data to set
     */
    public void setData(List<ProductData> data) {
        this.data = data;
    }

    /**
     * @return the exceptions
     */
    public Exception getExceptions() {
        return exceptions;
    }

    /**
     * @param exceptions
     *            the exceptions to set
     */
    public void setExceptions(Exception exceptions) {
        this.exceptions = exceptions;
    }
}

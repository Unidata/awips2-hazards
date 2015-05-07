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
package com.raytheon.uf.common.hazards.productgen.executors;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.raytheon.uf.common.dataplugin.events.EventSet;
import com.raytheon.uf.common.dataplugin.events.IEvent;
import com.raytheon.uf.common.hazards.productgen.GeneratedProductList;
import com.raytheon.uf.common.hazards.productgen.product.ProductScript;

/**
 * Executes the updateProduct method of ProductScript
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 22, 2015    6979    Robert.Blum Initial creation
 * 
 * </pre>
 * 
 * @author Robert.Blum
 * @version 1.0
 */

public class UpdateProductExecutor extends
        AbstractProductExecutor<GeneratedProductList> {

    /** provide the information for the product generator */
    private EventSet<IEvent> eventSet;

    /** product dictionary to be updated */
    private List<Map<String, Serializable>> updatedDataList;

    /** String array of formats */
    private String[] formats;

    /**
     * Constructor.
     * 
     * @param product
     *            name of the product generator
     * @param eventSet
     *            the EventSet<IEvent> object that will provide the information
     *            for the product generator
     */
    public UpdateProductExecutor(String product, EventSet<IEvent> eventSet,
            List<Map<String, Serializable>> updateDataList,
            String[] formats) {
        this.product = product;
        this.eventSet = eventSet;
        this.updatedDataList = updateDataList;
        this.formats = formats;
    }

    @Override
    public GeneratedProductList execute(ProductScript script) {
        return script
                .updateProduct(product, eventSet, updatedDataList, formats);
    }

}

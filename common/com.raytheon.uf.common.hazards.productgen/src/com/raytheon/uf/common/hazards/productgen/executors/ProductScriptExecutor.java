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
import java.util.Map;

import com.raytheon.uf.common.dataplugin.events.EventSet;
import com.raytheon.uf.common.dataplugin.events.IEvent;
import com.raytheon.uf.common.hazards.productgen.GeneratedProductList;
import com.raytheon.uf.common.hazards.productgen.product.ProductScript;

/**
 * Executes the generateProduct method of ProductScript
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 18, 2013            jsanchez     Initial creation
 * Nov  5, 2013 2266       jsanchez     Used GeneratedProductList.
 * 
 * </pre>
 * 
 * @author jsanchez
 * @version 1.0
 */

public class ProductScriptExecutor extends
        AbstractProductExecutor<GeneratedProductList> {

    /** provide the information for the product generator */
    private EventSet<IEvent> eventSet;

    private Map<String, Serializable> dialogInfo;

    /** String array of formats */
    private String[] formats;

    /** Name of the product generator */
    private String product;

    /**
     * Constructor.
     * 
     * @param product
     *            name of the product generator
     * @param eventSet
     *            the EventSet<IEvent> object that will provide the information
     *            for the product generator
     */
    public ProductScriptExecutor(String product, EventSet<IEvent> eventSet,
            Map<String, Serializable> dialogInfo, String[] formats) {
        this.product = product;
        this.eventSet = eventSet;
        this.dialogInfo = dialogInfo;
        this.formats = formats;
    }

    @Override
    public GeneratedProductList execute(ProductScript script) {
        return script.generateProduct(product, eventSet, dialogInfo, formats);
    }

}

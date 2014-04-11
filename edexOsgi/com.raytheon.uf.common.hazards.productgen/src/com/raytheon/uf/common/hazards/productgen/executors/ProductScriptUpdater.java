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
import java.util.LinkedHashMap;
import java.util.List;

import jep.JepException;

import com.raytheon.uf.common.hazards.productgen.GeneratedProductList;
import com.raytheon.uf.common.hazards.productgen.KeyInfo;
import com.raytheon.uf.common.hazards.productgen.product.ProductScript;

/**
 * Allows a python dictionary to be updated without having to go through a
 * product generator.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 24, 2013  2266      jsanchez     Initial creation
 * 
 * </pre>
 * 
 * @author jsanchez
 * @version 1.0
 */

public class ProductScriptUpdater extends
        AbstractProductExecutor<GeneratedProductList> {

    private String product;

    private List<LinkedHashMap<KeyInfo, Serializable>> updatedDataList;

    /** String array of formats */
    private String[] formats;

    public ProductScriptUpdater(String product,
            List<LinkedHashMap<KeyInfo, Serializable>> updateDataList,
            String[] formats) {
        this.product = product;
        this.updatedDataList = updateDataList;
        this.formats = formats;
    }

    @Override
    public GeneratedProductList execute(ProductScript script)
            throws JepException {
        GeneratedProductList generatedProducts = script
                .updateGeneratedProducts(product, updatedDataList, formats);
        return generatedProducts;
    }

}

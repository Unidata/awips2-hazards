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

import java.util.List;

import com.raytheon.uf.common.hazards.productgen.GeneratedProductList;
import com.raytheon.uf.common.hazards.productgen.ProductPart;
import com.raytheon.uf.common.hazards.productgen.product.ProductScript;

import jep.JepException;

/**
 * Executes the generateProductFrom method of ProductScript
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 24, 2013  2266      jsanchez     Initial creation
 * Apr 23, 2014  1480      jsanchez     Added isCorrection attribute.
 * Apr 16, 2015  7579      Robert.Blum  Replaced prevDataList with keyinfo.
 * Apr 23, 2015  6979      Robert.Blum  Renamed - changes for product corrections.
 * Feb 23, 2017  29170     Robert.Blum  Product Editor refactor.
 * Jun 05, 2017  29996     Robert.Blum  Now handles product parts.
 * 
 * </pre>
 * 
 * @author jsanchez
 * @version 1.0
 */

public class GenerateProductFromExecutor
        extends AbstractProductExecutor<GeneratedProductList> {

    private GeneratedProductList generatedProducts;

    private List<ProductPart> productParts;

    /** String array of formats */
    private String[] formats;

    public GenerateProductFromExecutor(String product,
            GeneratedProductList generatedProducts,
            List<ProductPart> productParts, String[] formats) {
        this.product = product;
        this.generatedProducts = generatedProducts;
        this.formats = formats;
        this.productParts = productParts;
    }

    @Override
    public GeneratedProductList execute(ProductScript script)
            throws JepException {
        GeneratedProductList generatedProducts = script.generateProductFrom(
                product, this.generatedProducts, productParts, formats);
        return generatedProducts;
    }

}

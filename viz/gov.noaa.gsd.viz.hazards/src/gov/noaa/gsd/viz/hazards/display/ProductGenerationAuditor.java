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
package gov.noaa.gsd.viz.hazards.display;

import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;

import com.raytheon.uf.common.hazards.productgen.GeneratedProductList;
import com.raytheon.uf.viz.hazards.sessionmanager.product.ProductInformation;

/**
 * Used to track which products need to be generated and which products have
 * been generated for a particular request.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 7, 2014  2890       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class ProductGenerationAuditor {
    private boolean issue;

    private String productGenerationTrackingID;

    private List<GeneratedProductList> generatedProducts;

    private List<ProductInformation> productsToBeGenerated;

    /**
     * 
     */
    public ProductGenerationAuditor(boolean issue,
            String productGenerationTrackingID) {
        this.issue = issue;
        this.productGenerationTrackingID = productGenerationTrackingID;
        this.generatedProducts = new LinkedList<GeneratedProductList>();
        this.productsToBeGenerated = new ArrayList<ProductInformation>();
    }

    public void addProductToBeGenerated(ProductInformation productInformation) {
        this.productsToBeGenerated.add(productInformation);
    }

    public synchronized boolean productGenerated(
            GeneratedProductList generatedProductList,
            ProductInformation productInformation) {
        if (this.productsToBeGenerated.contains(productInformation) == false) {
            // unlikely case
            return false;
        }

        this.productsToBeGenerated.remove(productInformation);
        this.generatedProducts.add(generatedProductList);
        return this.productsToBeGenerated.isEmpty();
    }

    public synchronized boolean productGenerationFailure(
            ProductInformation productInformation) {
        if (this.productsToBeGenerated.contains(productInformation) == false) {
            // unlikely case
            return false;
        }

        this.productsToBeGenerated.remove(productInformation);
        return this.productsToBeGenerated.isEmpty();
    }

    public boolean isIssue() {
        return issue;
    }

    public void setIssue(boolean issue) {
        this.issue = issue;
    }

    public String getProductGenerationTrackingID() {
        return productGenerationTrackingID;
    }

    public void setProductGenerationTrackingID(
            String productGenerationTrackingID) {
        this.productGenerationTrackingID = productGenerationTrackingID;
    }

    public List<GeneratedProductList> getGeneratedProducts() {
        return generatedProducts;
    }

    public void setGeneratedProducts(
            List<GeneratedProductList> generatedProducts) {
        this.generatedProducts = generatedProducts;
    }

    public List<ProductInformation> getProductsToBeGenerated() {
        return productsToBeGenerated;
    }

    public void setProductsToBeGenerated(
            List<ProductInformation> productsToBeGenerated) {
        this.productsToBeGenerated = productsToBeGenerated;
    }
}
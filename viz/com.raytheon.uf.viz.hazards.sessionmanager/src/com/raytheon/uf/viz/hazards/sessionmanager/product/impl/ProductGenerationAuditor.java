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
package com.raytheon.uf.viz.hazards.sessionmanager.product.impl;

import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;

import com.raytheon.uf.common.hazards.productgen.GeneratedProductList;
import com.raytheon.uf.viz.hazards.sessionmanager.product.ProductGeneratorInformation;

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

    private List<ProductGeneratorInformation> allProductGeneratorInformation;

    /**
     * 
     */
    public ProductGenerationAuditor(boolean issue,
            String productGenerationTrackingID) {
        this.issue = issue;
        this.productGenerationTrackingID = productGenerationTrackingID;
        this.generatedProducts = new LinkedList<GeneratedProductList>();
        this.allProductGeneratorInformation = new ArrayList<ProductGeneratorInformation>();
    }

    public void addProductGeneratorInformation(ProductGeneratorInformation productGeneratorInformation) {
        this.allProductGeneratorInformation.add(productGeneratorInformation);
    }

    public synchronized boolean productGenerated(
            GeneratedProductList generatedProductList,
            ProductGeneratorInformation productGeneratorInformation) {
        if (this.allProductGeneratorInformation.contains(productGeneratorInformation) == false) {
            // unlikely case
            return false;
        }

        this.allProductGeneratorInformation.remove(productGeneratorInformation);
        this.generatedProducts.add(generatedProductList);
        return this.allProductGeneratorInformation.isEmpty();
    }

    public synchronized boolean productGenerationFailure(
            ProductGeneratorInformation productGeneratorInformation) {
        if (this.allProductGeneratorInformation.contains(productGeneratorInformation) == false) {
            // unlikely case
            return false;
        }

        this.allProductGeneratorInformation.remove(productGeneratorInformation);
        return this.allProductGeneratorInformation.isEmpty();
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

    public List<ProductGeneratorInformation> getAllProductGeneratorInformation() {
        return allProductGeneratorInformation;
    }

    public void setAllProductGeneratorInformation(
            List<ProductGeneratorInformation> allProductGeneratorInformation) {
        this.allProductGeneratorInformation = allProductGeneratorInformation;
    }
}
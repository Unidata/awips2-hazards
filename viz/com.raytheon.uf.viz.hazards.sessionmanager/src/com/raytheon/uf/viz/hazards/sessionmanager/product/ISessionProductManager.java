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
package com.raytheon.uf.viz.hazards.sessionmanager.product;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.raytheon.uf.common.hazards.productgen.GeneratedProductList;
import com.raytheon.uf.common.hazards.productgen.data.ProductData;

/**
 * Manages product generation for a session.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 19, 2013 1257       bsteffen    Initial creation
 * Apr 29, 2014 3558       bkowal      generate now returns a boolean indicating
 *                                     whether or not product generation has been cancelled.
 * Apr 17, 2014  696       dgilling    Added setVTECFormat().
 * Apr 29, 2014 1480       jsanchez    Added generateCorrectionProduct and issueCorrection.
 * Oct 02, 2014 4042       Chris.Golden Changed to support two-step product staging dialog
 *                                      (first step allows user to select additional events
 *                                      to be included in products, second step allows the
 *                                      inputting of additional product-specific information
 *                                      using megawidgets). Also made many public, interface-
 *                                      specified methods private, as they are only to be
 *                                      used internally by this class.
 * Jan 29, 2015 4375       Dan Schaffer Console initiation of RVS product generation
 * Jun 02, 2015 7138       Robert.Blum  Changes for issuing RVS products.
 * Jul 30, 2015 9681       Robert.Blum  Changes for viewOnly products.
 * Feb 24, 2016 13929      Robert.Blum  Remove first part of staging dialog.
 * Nov 23, 2016 26423      Robert.Blum  Removed dead code.
 * Jan 27, 2017 22308      Robert.Blum  Removed parameter that is no longer needed.
 * Mar 21, 2017 29996      Robert.Blum  Added method to get staging dialog metadata.
 * Apr 14, 2017 32733      Robert.Blum  Code clean up.
 * Jun 26, 2017 19207      Chris.Golden Changes to view products for specific events.
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */

public interface ISessionProductManager {

    /**
     * Types of possible staging required when attempting to generate products.
     */
    public enum StagingRequired {
        NO_APPLICABLE_EVENTS, NONE, PRODUCT_SPECIFIC_INFO
    };

    /**
     * Execute any shutdown needed.
     */
    public void shutdown();

    /**
     * Change the VTEC format generation parameters.
     * 
     * @param vtecMode
     *            The first character of the VTEC string, usually "O", but
     *            sometimes "T" or "E" or "X".
     * @param testMode
     *            Whether or not this is a test mode product. Controls headline
     *            generation.
     */
    public void setVTECFormat(String vtecMode, boolean testMode);

    /**
     * Issue the specified corrected product.
     * 
     * @param productGeneratorInformation
     *            Information about the product being corrected.
     */
    public void issueCorrection(
            ProductGeneratorInformation productGeneratorInformation);

    /**
     * Create a text version that can be brought up in the product editor/viewer
     * after the product is issued. The product can be brought up to be
     * corrected and re-issued in the product editor or brought up as view only
     * in the product viewer.
     * 
     * @param productData
     *            Hibernate representation for the storage of product data to
     *            retrieve for review.
     */
    public void generateProductsForCorrection(List<ProductData> productData);

    /**
     * Generate updated products from pre-existing products.
     * 
     * @param generatedProductsList
     *            List of generated products to be created.
     */
    public void setupForRunningFinalProductGen(
            List<GeneratedProductList> generatedProductsList);

    /**
     * Generate products, or request product staging if it is required before
     * generation can occur.
     * 
     * @param issue
     *            Flag indicating whether or not the generation is the result of
     *            an issue command; if false, it is the result of a preview
     *            command.
     */
    public void generateProducts(boolean issue);

    /**
     * Get the product generation information associated with the most recent
     * issuance or preview attempt.
     * <p>
     * <strong>Note</strong>: This method must only be called after calling
     * {@link #generateProducts(boolean)} with the same value for
     * <code>issue</code>.
     * 
     * @param issue
     *            Flag indicating whether or not the hazard events are to be
     *            issued. If false, they are to be previewed.
     * @return Product generation information.
     */
    public Collection<ProductGeneratorInformation> getAllProductGeneratorInformationForSelectedHazards(
            boolean issue);

    /**
     * Generate products from the preliminary product staging information edited
     * by the user if possible, or return a value indicating that further
     * information must be collected before generation can commence.
     * <p>
     * <strong>Note</strong>: This method must only be called after calling
     * {@link #generateProducts(boolean)} with the same value for
     * <code>issue</code>.
     * 
     * @param issue
     *            Flag indicating whether or not the hazard events are to be
     *            issued. If false, they are to be previewed.
     * @param selectedEventIdentifiersForProductGeneratorNames
     *            Map of product generator names to the lists of event
     *            identifiers that are to be incorporated into the associated
     *            products.
     * @return True if the second stage of the product staging dialog is
     *         required to allow users to fill in product-specific information,
     *         false if product generation has commenced.
     */
    public boolean createProductsFromPreliminaryProductStaging(boolean issue,
            Map<String, List<String>> selectedEventIdentifiersForProductGeneratorNames);

    /**
     * Generate products from the final product staging information edited by
     * the user.
     * 
     * @param issue
     *            Flag indicating whether or not the hazard events are to be
     *            issued. If false, they are to be previewed.
     * @param metadataMapsForProductGeneratorNames
     *            Map of product generator names to associated metadata maps.
     */
    public void createProductsFromFinalProductStaging(boolean issue,
            Map<String, Map<String, Serializable>> metadataMapsForProductGeneratorNames);

    /**
     * Run the given product generator for selected hazard events
     * 
     * @param productGeneratorName
     */
    public void generateProducts(String productGeneratorName);

    /**
     * Run the given non hazard product generator
     * 
     * @param productGeneratorName
     */
    public void generateNonHazardProducts(String productGeneratorName);

    /**
     * Show the product viewer selection.
     */
    public void showUserProductViewerSelection();

    /**
     * Get the result of the execution of the product level metadata script.
     * 
     * @param info
     * @param issue
     * @param map
     * @return
     */
    public List<Map<String, Object>> getStagingDialogMetadata(
            ProductGeneratorInformation info, boolean issue,
            Map<String, Serializable> map);
}

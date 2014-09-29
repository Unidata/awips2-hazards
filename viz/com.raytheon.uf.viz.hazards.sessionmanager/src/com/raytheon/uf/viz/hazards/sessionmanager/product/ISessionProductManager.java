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
import java.util.LinkedHashMap;
import java.util.List;

import com.raytheon.uf.common.hazards.productgen.GeneratedProductList;
import com.raytheon.uf.common.hazards.productgen.KeyInfo;
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
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */

public interface ISessionProductManager {

    /**
     * Returns product generator information appropriate for the current hazard
     * selection.
     * 
     * @return
     */
    public Collection<ProductGeneratorInformation> getAllProductGeneratorInformationForSelectedHazards(
            boolean issue);

    /**
     * Generate a product from the given {@link ProductGeneratorInformation}
     * 
     * @param productGeneratorInformation
     *            the information about the product to generate
     * @param issue
     *            - true if hazard events are being issued; false if previewed
     * @param confim
     *            whether or not to confirm issuance
     * @return whether or not to continue product generation
     */
    public boolean generate(
            ProductGeneratorInformation productGeneratorInformation,
            boolean issue, boolean confirm);

    /**
     * Generates the issued product from the given
     * {@link ProductGeneratorInformation} and the updatedDataList derived from
     * the database.
     * 
     * @param productGeneratorInformation
     * @param updatedDataList
     */
    public void generateProductReview(
            ProductGeneratorInformation productGeneratorInformation,
            List<LinkedHashMap<KeyInfo, Serializable>> updatedDataList);

    /**
     * Issue the corrected product
     * 
     * @param productGeneratorInformation
     */
    public void issueCorrection(
            ProductGeneratorInformation productGeneratorInformation);

    /**
     * Issue the provided product and all the events associated with it.
     * 
     * @param productGeneratorInformation
     */
    public void issue(ProductGeneratorInformation productGeneratorInformation);

    /**
     * Execute any shutdown needed.
     */
    public void shutdown();

    /**
     * Validate the selected events before product generation.
     * 
     * @return true - the selected events are valid for product generation false
     *         - the selected events are not valid.
     */
    public boolean validateSelectedHazardsForProductGeneration();

    /**
     * Return a list of hazard types which product generation currently does not
     * support (i.e. these are hazared types for which product generation cannot
     * generate products).
     * 
     * @return List of unsupported hazard types.
     */
    public List<String> getUnsupportedHazards();

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
     * Creates a text version that can be brought up in the product editor after
     * the product is issued. The product can be brought up to be reviewed and
     * even corrected and re-issued.
     * 
     * @param The
     *            hibernate representation for the storage of product data to
     *            retrieve for correction or review.
     */
    public void generateReviewableProduct(List<ProductData> productData);

    /**
     * Generate products from Hazard Event
     * 
     * @param issue
     *            - true if hazard events are being issued; false if previewed
     * @param generatedProductsList
     * @return
     */
    public void createProductsFromHazardEventSets(boolean issue,
            List<GeneratedProductList> generatedProductsList);

    /**
     * Generates products from the product staging info edited by the user.
     * 
     * @param issue
     *            - true if hazard events are being issued; false if previewed
     * @param productStagingInfo
     *            - information used to populate the product staging dialog
     * @return
     */
    public void createProductsFromProductStagingInfo(boolean issue,
            ProductStagingInfo productStagingInfo);

    /**
     * Determines if product generation is needed.
     * 
     * @param issue
     *            - true if hazard events are being issued; false if previewed
     * @return true if product generation is required
     */
    public boolean isProductGenerationRequired(boolean issue);

    /**
     * Get the cached {@link ProductGeneratorInformation} depending on whether
     * an issue or preview is occurring.
     * 
     * @param issue
     *            - true if hazard events are being issued; false if previewed
     * @return cached product information used for optimization.
     */
    public Collection<ProductGeneratorInformation> getAllProductGeneratorInformationForSelectedHazardsCache(
            boolean issue);

    /**
     * Generate the products
     * 
     * @param issue
     *            - true if hazard events are being issued; false if previewed
     * @return
     */
    public void generateProducts(boolean issue);
}

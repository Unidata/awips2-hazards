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

import gov.noaa.gsd.viz.megawidgets.MegawidgetSpecifierManager;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.hazards.productgen.GeneratedProductList;

/**
 * 
 * Information about a product that is used to configure it for generation.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 24, 2013 1257       bsteffen    Initial creation
 * Nov  5, 2013 2266       jsanchez    Used GeneratedProductList.
 * Jan 10, 2014 2890       bkowal      Added an identifier to associate a
 *                                     ProductInformation with a product generation
 *                                     request.
 * Mar 18, 2014 2917       jsanchez    Added getter/setters for ProductFormats.
 * Oct 03, 2014 4042       Chris.Golden Changed dialog info (list of maps specifying
 *                                      megawidgets) to megawidget specifier manager.
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public class ProductGeneratorInformation {

    private String productGeneratorName;

    private Set<IHazardEvent> productEvents;

    private Set<IHazardEvent> possibleProductEvents;

    private MegawidgetSpecifierManager stagingMegawidgetSpecifierManager;

    private Map<String, Serializable> dialogSelections = Collections.emptyMap();

    private ProductFormats productFormats;

    private GeneratedProductList generatedProducts;

    private Throwable error;

    private String generationID;

    public String getProductGeneratorName() {
        return productGeneratorName;
    }

    public void setProductGeneratorName(String productGeneratorName) {
        this.productGeneratorName = productGeneratorName;
    }

    public Set<IHazardEvent> getProductEvents() {
        return productEvents;
    }

    public void setProductEvents(Set<IHazardEvent> productEvents) {
        this.productEvents = productEvents;
    }

    public Set<IHazardEvent> getPossibleProductEvents() {
        return possibleProductEvents;
    }

    public void setPossibleProductEvents(Set<IHazardEvent> possibleProductEvents) {
        this.possibleProductEvents = possibleProductEvents;
    }

    public MegawidgetSpecifierManager getStagingDialogMegawidgetSpecifierManager() {
        return stagingMegawidgetSpecifierManager;
    }

    public void setStagingDialogMegawidgetSpecifierManager(
            MegawidgetSpecifierManager stagingMegawidgetSpecifierManager) {
        this.stagingMegawidgetSpecifierManager = stagingMegawidgetSpecifierManager;
    }

    public Map<String, Serializable> getDialogSelections() {
        return dialogSelections;
    }

    public void setDialogSelections(Map<String, Serializable> dialogSelections) {
        this.dialogSelections = dialogSelections;
    }

    public ProductFormats getProductFormats() {
        return productFormats;
    }

    public void setProductFormats(ProductFormats productFormats) {
        this.productFormats = productFormats;
    }

    public GeneratedProductList getGeneratedProducts() {
        return generatedProducts;
    }

    public void setGeneratedProducts(GeneratedProductList generatedProducts) {
        this.generatedProducts = generatedProducts;
    }

    public Throwable getError() {
        return error;
    }

    public void setError(Throwable error) {
        this.error = error;
    }

    public String getGenerationID() {
        return generationID;
    }

    public void setGenerationID(String generationID) {
        this.generationID = generationID;
    }
}
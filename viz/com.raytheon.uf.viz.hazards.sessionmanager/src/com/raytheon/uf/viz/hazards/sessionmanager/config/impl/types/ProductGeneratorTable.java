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
package com.raytheon.uf.viz.hazards.sessionmanager.config.impl.types;

import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.ASCII_PRODUCT_KEY;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.NULL_PRODUCT_GENERATOR;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.google.common.collect.Lists;
import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardEventUtilities;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.product.ProductFormats;

/**
 * JSon compatible object for loading and storing Product Generator Tables.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 29, 2013 1257       bsteffen    Initial creation
 * Apr 24, 2014 1480       jsanchez    Added getProductFormats method.
 * Feb 15, 2015 2271       Dan Schaffer Incur recommender/product generator init costs immediately
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */

public class ProductGeneratorTable extends
        HashMap<String, ProductGeneratorEntry> {

    private static final long serialVersionUID = -6842654894871115837L;

    public String getProduct(IHazardEvent event) {
        String key = HazardEventUtilities.getHazardType(event);
        for (Entry<String, ProductGeneratorEntry> entry : entrySet()) {
            for (String[] pair : entry.getValue().getAllowedHazards()) {
                if (pair[0].equals(key)) {
                    return entry.getKey();
                }
            }
        }
        return null;
    }

    public ProductFormats getProductFormats(String productGeneratorName) {
        ProductFormats productFormats = new ProductFormats();

        ProductGeneratorEntry entry = get(productGeneratorName);
        if (entry != null) {
            productFormats.setIssueFormats(Arrays.asList(entry
                    .getIssueFormatters()));
            productFormats.setPreviewFormats(Arrays.asList(entry
                    .getPreviewFormatters()));
        } else if (productGeneratorName.equals(NULL_PRODUCT_GENERATOR)) {
            List<String> formats = Lists.newArrayList(ASCII_PRODUCT_KEY);
            productFormats.setIssueFormats(formats);
            productFormats.setPreviewFormats(formats);
        }

        return productFormats;
    }
}

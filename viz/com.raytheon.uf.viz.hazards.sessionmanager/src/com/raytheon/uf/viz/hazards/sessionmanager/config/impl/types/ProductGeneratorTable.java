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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;

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
 * 
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
        }

        return productFormats;
    }
}

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

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.hazards.productgen.IGeneratedProduct;

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
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public class ProductInformation {

    private String productName;

    private Set<IHazardEvent> productEvents;

    private Set<IHazardEvent> possibleProductEvents;

    private Map<String, String> dialogInfo;

    private Map<String, String> dialogSelections;

    private String[] formats;

    private List<IGeneratedProduct> products;

    private Throwable error;

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
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

    public Map<String, String> getDialogInfo() {
        return dialogInfo;
    }

    public void setDialogInfo(Map<String, String> dialogInfo) {
        this.dialogInfo = dialogInfo;
    }

    public Map<String, String> getDialogSelections() {
        return dialogSelections;
    }

    public void setDialogSelections(Map<String, String> dialogSelections) {
        this.dialogSelections = dialogSelections;
    }

    public String[] getFormats() {
        return formats;
    }

    public void setFormats(String[] formats) {
        this.formats = formats;
    }

    public List<IGeneratedProduct> getProducts() {
        return products;
    }

    public void setProducts(List<IGeneratedProduct> products) {
        this.products = products;
    }

    public Throwable getError() {
        return error;
    }

    public void setError(Throwable error) {
        this.error = error;
    }

}
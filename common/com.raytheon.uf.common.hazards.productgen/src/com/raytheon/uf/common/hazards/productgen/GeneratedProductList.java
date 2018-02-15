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
package com.raytheon.uf.common.hazards.productgen;

import java.util.ArrayList;

import com.raytheon.uf.common.dataplugin.events.EventSet;
import com.raytheon.uf.common.dataplugin.events.IEvent;

/**
 * Subclass of ArrayList<IGeneratedProduct> that contains an event set
 * attribute.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 29, 2013  2266      jsanchez     Initial creation
 * Feb 07, 2014  2890      bkowal      Fix serializable warning.
 * Apr 23, 2014  1480      jsanchez    Added isCorrection attribute.
 * Jun  3, 2014  1480      jsanchez    Added a copy constructor.
 * Jul 30, 2015  9681      Robert.Blum Added isViewOnly attribute.
 * Jan 27, 2017  22308     Robert.Blum Removed isViewOnly attribute.
 * </pre>
 * 
 * @author jsanchez
 * @version 1.0
 */

public class GeneratedProductList extends ArrayList<IGeneratedProduct> {

    private static final long serialVersionUID = -2395834336533109701L;

    private String productInfo;

    private EventSet<IEvent> eventSet;

    private boolean isCorrectable;

    public GeneratedProductList() {

    }

    /**
     * Copy constructor.
     * 
     * @param generatedProductList
     */
    public GeneratedProductList(GeneratedProductList generatedProductList) {
        this.productInfo = generatedProductList.getProductInfo();
        this.isCorrectable = generatedProductList.isCorrectable();
        this.eventSet = generatedProductList.getEventSet();
        for (IGeneratedProduct generatedProduct : generatedProductList) {
            add(new GeneratedProduct(generatedProduct));
        }
    }

    public String getProductInfo() {
        return productInfo;
    }

    public void setProductInfo(String productInfo) {
        this.productInfo = productInfo;
    }

    public EventSet<IEvent> getEventSet() {
        return eventSet;
    }

    public void setEventSet(EventSet<IEvent> eventSet) {
        this.eventSet = eventSet;
    }

    public boolean isCorrectable() {
        return isCorrectable;
    }

    public void setCorrectable(boolean isCorrectable) {
        this.isCorrectable = isCorrectable;
    }
}

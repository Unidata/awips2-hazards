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

import java.util.ArrayList;
import java.util.List;

import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IReadableHazardEvent;
import com.raytheon.uf.common.dataplugin.text.db.StdTextProduct;
import com.raytheon.uf.viz.productgen.validation.util.VtecObject;

/**
 * Data object to back a row in the Product Viewer Dialog.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 16, 2016  16538     mpduff      Initial creation
 * Apr 27, 2016  17742     Roger.Ferrel Added getter methods for new columns.
 * Jul 06, 2016  18257     Kevin.Bisanz Implemented toString()
 * Jan 27, 2017  22308     Robert.Blum Refactored to work with pulling products
 *                                     out of the text database.
 * 
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class ProductViewerRowData {

    /**
     * List of events that were used to generate the product.
     */
    private List<? extends IReadableHazardEvent> events;

    /**
     * The Text Product for the text database.
     */
    private StdTextProduct product;

    /**
     * List of VTEC objects parsed from the text product.
     */
    private List<VtecObject> vtecs;

    /**
     * @return the events
     */
    public List<? extends IReadableHazardEvent> getEvents() {
        return events;
    }

    /**
     * @param events
     *            the events to set
     */
    public void setEvents(List<? extends IReadableHazardEvent> events) {
        this.events = events;
    }

    /**
     * @return the product
     */
    public StdTextProduct getProduct() {
        return product;
    }

    /**
     * @param product
     *            the product to set
     */
    public void setProduct(StdTextProduct product) {
        this.product = product;
    }

    /**
     * @return the product
     */
    public List<VtecObject> getVtecs() {
        return vtecs;
    }

    /**
     * @param product
     *            the product to set
     */
    public void setVtecs(List<VtecObject> vtecs) {
        this.vtecs = vtecs;
    }

    public String getEventIds() {
        if (events == null) {
            return "";
        }
        List<String> eventIds = new ArrayList<>(events.size());
        for (IReadableHazardEvent event : events) {
            eventIds.add(event.getEventID());
        }
        return eventIds.toString();
    }

    /**
     * Returns the VTEC action(s) from the text product as a single string.
     * 
     * @return
     */
    public String getActions() {
        List<String> productActions = new ArrayList<>(vtecs.size());
        for (VtecObject vtec : vtecs) {
            productActions.add(vtec.getAction());
        }
        return productActions.toString();
    }

    public String getUserName() {
        if (events != null && events.isEmpty() == false) {
            String userName = events.get(0).getWsId().getUserName();
            if (userName != null) {
                return userName;
            }
        }
        return "";
    }

    public String getSite() {
        if (events != null && events.isEmpty() == false) {
            String site = events.get(0).getSiteID();
            if (site != null) {
                return site;
            }
        }
        return "";
    }

    public Long getIssueTime() {
        if (events != null && events.isEmpty() == false) {
            return (Long) events.get(0)
                    .getHazardAttribute(HazardConstants.ISSUE_TIME);
        }
        return null;
    }
}

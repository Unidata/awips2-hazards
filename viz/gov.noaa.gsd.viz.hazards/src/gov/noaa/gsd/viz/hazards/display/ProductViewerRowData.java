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

import java.util.Date;

import com.raytheon.uf.common.hazards.productgen.data.ProductData;

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
 * 
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class ProductViewerRowData {

    /**
     * The product's category
     */
    private String productCategory;

    /**
     * The product's issue date
     */
    private Date issueDate;

    private Date endDate;

    /**
     * The product's event IDs
     */
    private String eventIds;

    /**
     * The raw product data
     */
    private ProductData productData;

    /**
     * @return the productCategory
     */
    public String getProductCategory() {
        return productCategory;
    }

    /**
     * @param productCategory
     *            the productCategory to set
     */
    public void setProductCategory(String productCategory) {
        this.productCategory = productCategory;
    }

    /**
     * @return the issueDate
     */
    public Date getIssueDate() {
        return issueDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    /**
     * @param issueDate
     *            the issueDate to set
     */
    public void setIssueDate(Date issueDate) {
        this.issueDate = issueDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    /**
     * @return the eventIds
     */
    public String getEventIds() {
        return eventIds;
    }

    /**
     * @param eventIds
     *            the eventIds to set
     */
    public void setEventIds(String eventIds) {
        this.eventIds = eventIds;
    }

    /**
     * @return the productData
     */
    public ProductData getProductData() {
        return productData;
    }

    /**
     * @param productData
     *            the productData to set
     */
    public void setProductData(ProductData productData) {
        this.productData = productData;
    }

    public String getHazardType() {
        String retVal = null;
        if (productData != null) {
            retVal = productData.getHazardType();
        }
        return retVal;
    }

    public String getVtecStr() {
        String retVal = null;
        if (productData != null) {
            retVal = productData.getVtecStr();
        }
        return retVal;
    }

    public Long getExpirationTime() {
        Long retVal = null;
        if (productData != null) {
            retVal = productData.getExpirationTime();
        }
        return retVal;
    }

    public String getUserName() {
        String retVal = null;
        if (productData != null) {
            retVal = productData.getUserName();
        }
        return retVal;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(getEventIds());
        sb.append(" ");
        sb.append(getVtecStr());
        sb.append(" ");
        sb.append(getHazardType());
        return sb.toString();
    }
}

/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.display.action;

import gov.noaa.gsd.viz.hazards.display.ProductStagingInfo;

/**
 * Action class "fired" from the Product Editor. Registered observers receive
 * this object and act on it.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Bryon.Lawrence      Initial induction into repo
 * 
 * </pre>
 * 
 * @author Bryon.Lawrence
 */
public class ProductStagingAction {
    private String action;

    private ProductStagingInfo productStagingInfo;

    private String eventID;

    private String issueFlag;

    /**
     * @return the issueFlag
     */
    public String getIssueFlag() {
        return issueFlag;
    }

    /**
     * @param issueFlag
     *            the issueFlag to set
     */
    public void setIssueFlag(String issueFlag) {
        this.issueFlag = issueFlag;
    }

    public ProductStagingAction(String action) {
        // TODO Auto-generated constructor stub
        this.action = action;
    }

    public ProductStagingAction(String action, String ID,
            ProductStagingInfo productStagingInfo) {
        // TODO Auto-generated constructor stub
        this.action = action;
        this.productStagingInfo = productStagingInfo;
        this.eventID = ID;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getAction() {
        return action;
    }

    public void setEventID(String ID) {
        this.eventID = ID;
    }

    public String getEventID() {
        return eventID;
    }

    public ProductStagingInfo getProductStagingInfo() {
        return productStagingInfo;
    }

    public void setProductStagingInfo(ProductStagingInfo productStagingInfo) {
        this.productStagingInfo = productStagingInfo;
    }

}

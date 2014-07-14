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

    private ProductStagingInfo productStagingInfo;

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

    public ProductStagingAction() {
    }

    public ProductStagingInfo getProductStagingInfo() {
        return productStagingInfo;
    }

    public void setProductStagingInfo(ProductStagingInfo productStagingInfo) {
        this.productStagingInfo = productStagingInfo;
    }

}

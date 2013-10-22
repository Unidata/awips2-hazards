/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.display.test;

import gov.noaa.gsd.viz.hazards.jsonutilities.Dict;
import gov.noaa.gsd.viz.hazards.productstaging.IProductStagingView;
import gov.noaa.gsd.viz.mvp.widgets.ICommandInvoker;

import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Description: Mock {@link IProductStagingView} used for testing.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 22, 2013  2166      daniel.s.schaffer@noaa.gov      Initial creation
 * 
 * </pre>
 * 
 * @author daniel.s.schaffer@noaa.gov
 * @version 1.0
 */
@SuppressWarnings("rawtypes")
public class ProductStagingViewForTesting implements IProductStagingView {

    private boolean toBeIssued;

    private Dict productStagingInfo;

    @Override
    public void dispose() {

    }

    @Override
    public List contributeToMainUI(Enum type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void showProductStagingDetail(boolean toBeIssued,
            Dict productStagingInfo) {
        this.toBeIssued = false;
        this.productStagingInfo = productStagingInfo;

    }

    @Override
    public ICommandInvoker getContinueInvoker() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isToBeIssued() {
        return toBeIssued;
    }

    @Override
    public Dict getProductInfo() {
        return productStagingInfo;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}

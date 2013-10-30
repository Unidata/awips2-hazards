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
import gov.noaa.gsd.viz.hazards.producteditor.IProductEditorView;
import gov.noaa.gsd.viz.mvp.widgets.ICommandInvocationHandler;
import gov.noaa.gsd.viz.mvp.widgets.ICommandInvoker;

import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;

/**
 * Description: Mock {@link IProductEditorView} used for testing.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 22, 2013 2166       daniel.s.schaffer@noaa.gov      Initial creation
 * 
 * </pre>
 * 
 * @author daniel.s.schaffer@noaa.gov
 * @version 1.0
 */
@SuppressWarnings("rawtypes")
public class ProductEditorViewForTesting implements IProductEditorView {

    private String productInfo;

    private List<Dict> generatedProducts;

    private List<Dict> hazardEventSets;

    @Override
    public void dispose() {
    }

    @Override
    public List contributeToMainUI(Enum type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void initialize() {
    }

    @Override
    public boolean showProductEditorDetail(String productInfo) {
        Dict productDict = Dict.getInstance(productInfo);
        this.productInfo = productInfo;
        this.generatedProducts = productDict
                .getDynamicallyTypedValue(HazardConstants.GENERATED_PRODUCTS);
        this.hazardEventSets = productDict
                .getDynamicallyTypedValue(HazardConstants.HAZARD_EVENT_SETS);
        return true;
    }

    @Override
    public void closeProductEditorDialog() {
    }

    @Override
    public List getGeneratedProductsDictList() {
        return generatedProducts;
    }

    @Override
    public List getHazardEventSetsList() {
        return hazardEventSets;
    }

    @Override
    public ICommandInvoker getIssueInvoker() {
        return new ICommandInvoker() {

            @Override
            public void setCommandInvocationHandler(
                    ICommandInvocationHandler handler) {
            }

        };
    }

    @Override
    public ICommandInvoker getDismissInvoker() {
        return new ICommandInvoker() {

            @Override
            public void setCommandInvocationHandler(
                    ICommandInvocationHandler handler) {
            }

        };
    }

    @Override
    public ICommandInvoker getShellClosedInvoker() {
        return new ICommandInvoker() {

            @Override
            public void setCommandInvocationHandler(
                    ICommandInvocationHandler handler) {
            }

        };
    }

    @Override
    public void openDialog() {
    }

    public Dict getProductInfo() {
        return Dict.getInstance(productInfo);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}

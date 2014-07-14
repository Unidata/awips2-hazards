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

import gov.noaa.gsd.viz.hazards.producteditor.IProductEditorView;
import gov.noaa.gsd.viz.mvp.widgets.ICommandInvocationHandler;
import gov.noaa.gsd.viz.mvp.widgets.ICommandInvoker;

import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.raytheon.uf.common.hazards.productgen.GeneratedProductList;

/**
 * Description: Mock {@link IProductEditorView} used for testing.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 22, 2013 2166       daniel.s.schaffer@noaa.gov      Initial creation
 * Feb 07, 2014 2890       bkowal      Product Generation JSON refactor.
 * Apr 11, 2014 2819       Chris.Golden      Fixed bugs with the Preview and Issue
 *                                           buttons in the HID remaining grayed out
 *                                           when they should be enabled.
 * </pre>
 * 
 * @author daniel.s.schaffer@noaa.gov
 * @version 1.0
 */
@SuppressWarnings("rawtypes")
public class ProductEditorViewForTesting implements IProductEditorView {

    private ICommandInvocationHandler<String> issueInvocationHandler;

    private ICommandInvocationHandler<String> dismissInvocationHandler;

    private GeneratedProductList generatedProducts;

    private List<GeneratedProductList> generatedProductsList;

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
    public void closeProductEditorDialog() {
    }

    @Override
    public ICommandInvoker<String> getIssueInvoker() {
        return new ICommandInvoker<String>() {

            @Override
            public void setEnabled(String identifier, boolean enable) {
            }

            @Override
            public void setCommandInvocationHandler(String identifier,
                    ICommandInvocationHandler<String> handler) {
                issueInvocationHandler = handler;
            }
        };
    }

    @Override
    public ICommandInvoker<String> getDismissInvoker() {
        return new ICommandInvoker<String>() {

            @Override
            public void setEnabled(String identifier, boolean enable) {
            }

            @Override
            public void setCommandInvocationHandler(String identifier,
                    ICommandInvocationHandler<String> handler) {
                dismissInvocationHandler = handler;
            }
        };
    }

    @Override
    public void openDialog() {
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public List<GeneratedProductList> getGeneratedProductsList() {
        return this.generatedProductsList;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean showProductEditorDetail(List generatedProductsList) {
        this.generatedProductsList = generatedProductsList;
        this.generatedProducts = new GeneratedProductList();
        for (GeneratedProductList productList : this.generatedProductsList) {
            this.generatedProducts.addAll(productList);
        }

        return true;
    }

    public void invokeIssueButton() {
        if (issueInvocationHandler != null) {
            issueInvocationHandler.commandInvoked(null);
        }
    }

    public void invokeDismissButton() {
        if (issueInvocationHandler != null) {
            dismissInvocationHandler.commandInvoked(null);
        }
    }
}
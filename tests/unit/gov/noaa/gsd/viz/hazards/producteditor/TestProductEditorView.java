/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.producteditor;

import gov.noaa.gsd.viz.hazards.display.RCPMainUserInterfaceElement;
import gov.noaa.gsd.viz.mvp.widgets.ICommandInvocationHandler;
import gov.noaa.gsd.viz.mvp.widgets.ICommandInvoker;

import java.util.Collections;
import java.util.List;

import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardAction;
import com.raytheon.uf.common.hazards.productgen.GeneratedProductList;

/**
 * Description: Test implementation of the product editor view.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 01, 2013            Bryon.Lawrence      Initial creation
 * Jul 15, 2013     585    Chris.Golden        Changed to use new version of IView.
 * Nov 16, 2013  2166       daniel.s.schaffer@noaa.gov    Some tidying
 * Feb 07, 2014 2890       bkowal      Product Generation JSON refactor.
 * Apr 11, 2014   2819     Chris.Golden      Fixed bugs with the Preview and Issue
 *                                           buttons in the HID remaining grayed out
 *                                           when they should be enabled.
 * </pre>
 * 
 * @author Bryon.Lawrence
 * @version 1.0
 */
public class TestProductEditorView implements
        IProductEditorView<Object, RCPMainUserInterfaceElement> {

    /**
     * Dismiss command invocation handler.
     */
    private ICommandInvocationHandler<String> dismissHandler = null;

    /**
     * Issue command invocation handler.
     */
    private ICommandInvocationHandler<String> issueHandler = null;

    /**
     * Dismiss command invoker.
     */
    private final ICommandInvoker<String> dismissInvoker = new ICommandInvoker<String>() {

        @Override
        public void setEnabled(String identifier, boolean enable) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setCommandInvocationHandler(String identifier,
                ICommandInvocationHandler<String> handler) {
            dismissHandler = handler;
        }
    };

    /**
     * Dismiss command invoker.
     */
    private final ICommandInvoker<String> issueInvoker = new ICommandInvoker<String>() {

        @Override
        public void setEnabled(String identifier, boolean enable) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setCommandInvocationHandler(String identifier,
                ICommandInvocationHandler<String> handler) {
            issueHandler = handler;
        }
    };

    /**
     * @param
     * @return
     */
    @Override
    public void dispose() {
    }

    /**
     * Get any contributions to the main UI that the implementation desires to
     * make. Note that this method may be called multiple times per <code>type
     * </code> to (re)populate the main UI with the specified <code>type</code>;
     * implementations are responsible for cleaning up after contributed items
     * that may exist from a previous call with the same <code>type</code>.
     * 
     * @param type
     *            Type of contribution to be made to the main user interface.
     * @return List of contributions; this may be empty if none are to be made.
     */
    @Override
    public final List<Object> contributeToMainUI(
            RCPMainUserInterfaceElement type) {
        return Collections.emptyList();
    }

    /**
     * 
     * @param
     * @return
     */
    public void dismissButtonPressed() {
        dismissHandler.commandInvoked("Dismiss");
    }

    /**
     * 
     * @param
     * @return
     */
    public void issueButtonPressed() {
        issueHandler.commandInvoked(HazardAction.ISSUE.getValue());
    }

    /**
     * @param
     * @return
     */
    @Override
    public void initialize() {
    }

    @Override
    public boolean showProductEditorDetail(
            List<GeneratedProductList> generatedProductsList) {
        return true;
    }

    /**
     * @param
     * @return
     */
    @Override
    public void closeProductEditorDialog() {
    }

    /**
     * @param
     * @return
     */
    @Override
    public ICommandInvoker<String> getIssueInvoker() {
        return issueInvoker;
    }

    /**
     * @param
     * @return
     */
    @Override
    public ICommandInvoker<String> getDismissInvoker() {
        return dismissInvoker;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * gov.noaa.gsd.viz.hazards.producteditor.IProductEditorView#openDialog()
     */
    @Override
    public void openDialog() {
    }

    @Override
    public List<GeneratedProductList> getGeneratedProductsList() {
        return null;
    }

}

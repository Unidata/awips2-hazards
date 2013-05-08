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
import gov.noaa.gsd.viz.hazards.jsonutilities.Dict;
import gov.noaa.gsd.viz.hazards.producteditor.IProductEditorView;
import gov.noaa.gsd.viz.mvp.widgets.ICommandInvocationHandler;
import gov.noaa.gsd.viz.mvp.widgets.ICommandInvoker;

import java.util.List;

import com.google.common.collect.Lists;

/**
 * Description:  Test implementation of the 
 *               product editor view.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * March 01, 2013            Bryon.Lawrence      Initial creation
 * 
 * </pre>
 * 
 * @author Bryon.Lawrence
 * @version 1.0
 */
public class TestProductEditorView implements IProductEditorView<DummyToolBarContributionManager, RCPMainUserInterfaceElement>
{
    
    /**
     * Product information passed into the 
     * product staging dialog.
     */
     String productStagingInfo    = null;
    
    /**
     * Dismiss command invocation handler.
     */
    private ICommandInvocationHandler               dismissHandler            = null;
    
    /**
     * Issue command invocation handler.
     */
    private ICommandInvocationHandler               issueHandler            = null;

    /**
     * Shell closed command invocation handler.
     */
    private ICommandInvocationHandler               shellCloseHandler            = null;

    /**
     * Dismiss command invoker. 
     */
    private ICommandInvoker                         dismissInvoker            = new ICommandInvoker()
    {
        @Override
        public void setCommandInvocationHandler(
                ICommandInvocationHandler handler)
        {
            dismissHandler = handler;
        }
    };
    

    /**
     * Dismiss command invoker. 
     */
    private ICommandInvoker                         issueInvoker            = new ICommandInvoker()
    {
        @Override
        public void setCommandInvocationHandler(
                ICommandInvocationHandler handler)
        {
            issueHandler = handler;
        }
    };

    /**
     * Shell Closed command invoker. 
     */
    private ICommandInvoker                         shellClosedInvoker            = new ICommandInvoker()
    {
        @Override
        public void setCommandInvocationHandler(
                ICommandInvocationHandler handler)
        {
            shellCloseHandler = handler;
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
     * Contribute to the main UI, if desired. Note that this
     * method may be called multiple times per <code>type
     * </code> to (re)populate the main UI with the specified
     * <code>type</code>; implementations are responsible for
     * cleaning up after contributed items that may exist
     * from a previous call with the same <code>type</code>. 
     * 
     * @param  mainUI Main user interface to which to
     *                contribute.
     * @param  type   Type of contribution to be made to the
     *                main user interface.
     * @return True if items were contributed, otherwise
     *         false.
     */
    @Override
    public final boolean contributeToMainUI(DummyToolBarContributionManager toolBarManager,
            RCPMainUserInterfaceElement type) {
	return false;
    }

    
    /**
     * 
     * @param
     * @return
     */
    public void dismissButtonPressed ()
    {
	dismissHandler.commandInvoked("Dismiss");
    }
    
    /**
     * 
     * @param
     * @return
     */
    public void issueButtonPressed ()
    {
	issueHandler.commandInvoked("Issue");
    }
    
    /**
     * 
     * @param
     * @return
     */
    public void shellClosed()
    {
	shellCloseHandler.commandInvoked("Shell Closed");
    }

    /**
     * @param
     * @return
     */
    @Override
    public void initialize() {
    }

    /**
     * @param
     * @return
     */
    @Override
    public boolean showProductEditorDetail(String productInfo) {
	this.productStagingInfo = productInfo;
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
    public List<Dict> getGeneratedProductsDictList() {
	return Lists.newArrayList() ;
    }

    /**
     * @param
     * @return
     */
    @Override
    public List<Dict> getHazardEventSetsList() {
	return Lists.newArrayList();
    }

    /**
     * @param
     * @return
     */
    @Override
    public ICommandInvoker getIssueInvoker() {
	return issueInvoker;
    }

    /**
     * @param
     * @return
     */
    @Override
    public ICommandInvoker getDismissInvoker() {
	return dismissInvoker;
    }

    /**
     * @param
     * @return
     */
    @Override
    public ICommandInvoker getShellClosedInvoker() {
	
	return shellClosedInvoker;
    }

}

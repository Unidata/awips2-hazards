/**
 * This software was developed and / or modified by the National Oceanic and
 * Atmospheric Administration (NOAA), Earth System Research Laboratory (ESRL),
 * Global Systems Division (GSD), Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.producteditor;

import java.util.List;

import gov.noaa.gsd.viz.mvp.IView;
import gov.noaa.gsd.viz.mvp.widgets.ICommandInvoker;

import com.raytheon.uf.common.hazards.productgen.GeneratedProductList;

/**
 * Description: Defines the interface a concrete Product Editor View must
 * fulfill.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 18, 2013            Bryon.Lawrence      Initial creation
 * Sep 19, 2013 2046       mnash        Update for product generation.
 * Jan  7, 2013 2367       jsanchez     Used GeneratedProductList.
 * Feb 7, 2014  2890       bkowal      Product Generation JSON refactor.
 * 
 * </pre>
 * 
 * @author Bryon.Lawrence
 * @version 1.0
 */
public interface IProductEditorView<C, E extends Enum<E>> extends IView<C, E> {

    // Public Methods

    /**
     * Initialize the view.
     */
    public void initialize();

    public boolean showProductEditorDetail(
            List<GeneratedProductList> generatedProductsList);

    /**
     * Close the product editor dialog.
     */
    public void closeProductEditorDialog();

    /**
     * Get the generated products list.
     * 
     * @return Generated products list.
     */
    public GeneratedProductList getGeneratedProductList();

    public List<GeneratedProductList> getGeneratedProductsList();

    /**
     * Get the command invoker associated with the dialog's issue button.
     * 
     * @return Command invoker associated with the dialog's issue button.
     */
    public ICommandInvoker getIssueInvoker();

    /**
     * Get the command invoker associated with the dialog's dismiss button.
     * 
     * @return Command invoker associated with the dialog's dismiss button.
     */
    public ICommandInvoker getDismissInvoker();

    /**
     * Get the command invoker associated with the dialog's close button.
     * 
     * @return Command invoker associated with the dialog's close button.
     */
    public ICommandInvoker getShellClosedInvoker();

    public void openDialog();
}

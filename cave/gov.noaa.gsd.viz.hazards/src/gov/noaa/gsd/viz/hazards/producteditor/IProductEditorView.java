/**
 * This software was developed and / or modified by the National Oceanic and
 * Atmospheric Administration (NOAA), Earth System Research Laboratory (ESRL),
 * Global Systems Division (GSD), Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.producteditor;

import gov.noaa.gsd.viz.hazards.jsonutilities.Dict;
import gov.noaa.gsd.viz.mvp.IView;
import gov.noaa.gsd.viz.mvp.widgets.ICommandInvoker;

import java.util.List;

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

    /**
     * Open the product editor dialog.
     * 
     * @param productInfo
     *            Product Information to display in the product editor view.
     *            This is represented as a JSON string.
     * @return true or false indicating whether or not the dialog was opened.
     * 
     */
    public boolean showProductEditorDetail(String productInfo);

    /**
     * Close the product editor dialog.
     */
    public void closeProductEditorDialog();

    /**
     * Get the generated products dictionary list.
     * 
     * @return Generated products dictionary list.
     */
    public List<Dict> getGeneratedProductsDictList();

    /**
     * Get the hazard event set list.
     * 
     * @return Hazard event set list.
     */
    public List<Dict> getHazardEventSetsList();

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

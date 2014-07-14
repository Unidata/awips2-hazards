/**
 * This software was developed and / or modified by the National Oceanic and
 * Atmospheric Administration (NOAA), Earth System Research Laboratory (ESRL),
 * Global Systems Division (GSD), Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.producteditor;

import gov.noaa.gsd.common.eventbus.BoundedReceptionEventBus;
import gov.noaa.gsd.viz.hazards.display.HazardServicesPresenter;
import gov.noaa.gsd.viz.hazards.display.action.ProductEditorAction;
import gov.noaa.gsd.viz.mvp.widgets.ICommandInvocationHandler;

import java.util.EnumSet;
import java.util.List;

import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardAction;
import com.raytheon.uf.common.hazards.productgen.GeneratedProductList;
import com.raytheon.uf.viz.hazards.sessionmanager.ISessionManager;
import com.raytheon.uf.viz.hazards.sessionmanager.events.impl.ObservedHazardEvent;

/**
 * Description: Product Editor presenter, used to mediate between the model and
 * the product editor view.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer       Description
 * ------------ ---------- -------------- --------------------------
 * Feb 19, 2013            bryon.lawrence    Initial creation
 * Jul 15, 2013     585    Chris.Golden      Changed to support loading from bundle,
 *                                           including the passing in of the event
 *                                           bus so that the latter is no longer a
 *                                           singleton.
 * Sep 19, 2013 2046    mnash           Update for product generation.
 * Nov 16, 2013  2166       daniel.s.schaffer@noaa.gov    Some tidying
 * 
 * Dec 03, 2013 2182 daniel.s.schaffer@noaa.gov Refactoring - eliminated IHazardsIF
 * Feb 07, 2014 2890       bkowal         Product Generation JSON refactor.
 * Apr 11, 2014     2819   Chris.Golden      Fixed bugs with the Preview and Issue
 *                                           buttons in the HID remaining grayed out
 *                                           when they should be enabled.
 * May 17, 2014 2925       Chris.Golden      Changed to work with MVP framework
 *                                           widget changes. Also added newly
 *                                           required implementation of
 *                                           reinitialize(), and made initialize()
 *                                           protected as it is called by setView().
 * Apr 23, 2014 1480        jsanchez      Added product editor action CORRECT.
 * </pre>
 * 
 * @author Bryon.Lawrence
 * @version 1.0
 */
public class ProductEditorPresenter extends
        HazardServicesPresenter<IProductEditorView<?, ?>> {

    // Public Constructors

    /**
     * Construct a standard instance of a ProductEditorPresenter.
     * 
     * @param model
     *            Model to be handled by this presenter.
     * @param eventBus
     *            Event bus used to signal changes.
     */
    public ProductEditorPresenter(ISessionManager<ObservedHazardEvent> model,
            BoundedReceptionEventBus<Object> eventBus) {
        super(model, eventBus);
    }

    // Public Methods

    /**
     * Receive notification of a model change. For the moment, the product
     * editor dialog doesn't care about model events.
     * 
     * @param changes
     *            Set of elements within the model that have changed.
     */
    @Override
    public void modelChanged(EnumSet<HazardConstants.Element> changed) {

        // No action.
    }

    public final void showProductEditorDetail(
            List<GeneratedProductList> generatedProductsList) {
        this.getView().showProductEditorDetail(generatedProductsList);
        this.bind();
        this.getView().openDialog();
    }

    // Protected Methods

    @Override
    protected void initialize(IProductEditorView<?, ?> view) {
        view.initialize();
    }

    @Override
    protected final void reinitialize(IProductEditorView<?, ?> view) {

        /*
         * No action.
         */
    }

    // Private Methods

    /**
     * Binds the presenter to the view which implements the IProductEditorView
     * interface. The interface is the contract, and it is all the presenter
     * needs to know about the view. This allows different views to easily be
     * created and given to this presenter.
     * <p>
     * By binding to the view, the presenter handles all of the view's events.
     * 
     * @param
     * @return
     */
    private void bind() {
        getView().getIssueInvoker().setCommandInvocationHandler(null,
                new ICommandInvocationHandler<String>() {

                    @Override
                    public void commandInvoked(String identifier) {
                        ProductEditorAction action = null;
                        if (identifier != null
                                && identifier
                                        .equalsIgnoreCase(HazardConstants.CORRECTION_FLAG)) {
                            action = new ProductEditorAction(
                                    HazardAction.CORRECT);
                        } else {
                            action = new ProductEditorAction(HazardAction.ISSUE);
                        }

                        action.setGeneratedProductsList(getView()
                                .getGeneratedProductsList());
                        getModel().setIssueOngoing(true);
                        ProductEditorPresenter.this.fireAction(action);
                        getModel().setPreviewOngoing(false);
                        getView().closeProductEditorDialog();
                    }
                });

        getView().getDismissInvoker().setCommandInvocationHandler(null,
                new ICommandInvocationHandler<String>() {

                    @Override
                    public void commandInvoked(String identifier) {
                        dismissProductEditor();
                    }
                });

    }

    private void dismissProductEditor() {
        getModel().setPreviewOngoing(false);
        getView().closeProductEditorDialog();
    }
}

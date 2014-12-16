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
import gov.noaa.gsd.viz.hazards.display.action.HazardDetailAction;
import gov.noaa.gsd.viz.hazards.display.action.ProductEditorAction;
import gov.noaa.gsd.viz.mvp.widgets.ICommandInvocationHandler;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

import net.engio.mbassy.listener.Handler;

import com.raytheon.uf.common.dataplugin.events.IEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardAction;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.hazards.productgen.GeneratedProductList;
import com.raytheon.uf.viz.hazards.sessionmanager.ISessionManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.ObservedSettings;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventsModified;
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
 * Apr 23, 2014 1480       jsanchez          Added product editor action CORRECT.
 * Jun 30, 2014 3512       Chris.Golden      Changed to work with changes to
 *                                           ICommandInvoker.
 * Jul 14, 2014 4187        jsanchez         Check if the generatedProductsList is valid.
 * Jul 28, 2014 3412        jsanchez         Close the product editor on regeneration request.
 * Dec 05, 2014 4124       Chris.Golden      Changed to work with newly parameterized
 *                                           config manager.
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
    public ProductEditorPresenter(
            ISessionManager<ObservedHazardEvent, ObservedSettings> model,
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
        if (generatedProductsList != null
                && generatedProductsList.isEmpty() == false) {
            this.getView().showProductEditorDetail(generatedProductsList);
            this.bind();
            this.getView().openDialog();
        } else {
            getModel().setPreviewOngoing(false);
        }
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
        getView().getIssueInvoker().setCommandInvocationHandler(
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
                        dismissProductEditor();
                    }
                });

        getView().getDismissInvoker().setCommandInvocationHandler(
                new ICommandInvocationHandler<String>() {

                    @Override
                    public void commandInvoked(String identifier) {
                        dismissProductEditor();
                        if (identifier != null
                                && identifier
                                        .equalsIgnoreCase(HazardConstants.REGENERATE_FLAG)) {
                            HazardDetailAction action = new HazardDetailAction(
                                    HazardDetailAction.ActionType.PREVIEW);
                            ProductEditorPresenter.this.fireAction(action);
                        }
                    }
                });

    }

    private void dismissProductEditor() {
        getModel().setPreviewOngoing(false);
        getView().closeProductEditorDialog();
    }

    @Handler
    public void sessionEventsModified(final SessionEventsModified notification) {
        if (getView() != null) {
            List<GeneratedProductList> generatedProductListStorage = getView()
                    .getGeneratedProductsList();
            List<String> currentEvents = new ArrayList<String>();
            for (GeneratedProductList productList : generatedProductListStorage) {
                Iterator<IEvent> iterator = productList.getEventSet()
                        .iterator();
                while (iterator.hasNext()) {
                    IHazardEvent hazardEvent = (IHazardEvent) iterator.next();
                    currentEvents.add(hazardEvent.getEventID());
                }
            }
            List<String> modifiedEvents = new ArrayList<String>();
            for (ObservedHazardEvent observedHazardEvent : notification
                    .getEvents()) {
                modifiedEvents.add(observedHazardEvent.getEventID());
            }
            currentEvents.retainAll(modifiedEvents);
            if (!currentEvents.isEmpty()) {
                getView().notifySessionEventsModified();
            }
        }
    }
}
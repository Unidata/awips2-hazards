/**
 * This software was developed and / or modified by the National Oceanic and
 * Atmospheric Administration (NOAA), Earth System Research Laboratory (ESRL),
 * Global Systems Division (GSD), Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.productstaging;

import gov.noaa.gsd.viz.hazards.display.HazardServicesPresenter;
import gov.noaa.gsd.viz.hazards.display.ProductStagingInfo;
import gov.noaa.gsd.viz.hazards.display.action.ProductStagingAction;
import gov.noaa.gsd.viz.mvp.widgets.ICommandInvocationHandler;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.hazards.sessionmanager.ISessionManager;

/**
 * Settings presenter, used to mediate between the model and the settings view.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 18, 2013            Bryon.Lawrence    Initial creation
 * Jul 15, 2013     585    Chris.Golden      Changed to support loading from bundle,
 *                                           including the passing in of the event
 *                                           bus so that the latter is no longer a
 *                                           singleton.
 * Nov 15, 2013  2182       daniel.s.schaffer@noaa.gov    Refactoring JSON - ProductStagingDialog
 * Nov 21, 2013  2446       daniel.s.schaffer@noaa.gov Bug fixes in product staging dialog
 * 
 * Dec 03, 2013 2182 daniel.s.schaffer@noaa.gov Refactoring - eliminated IHazardsIF
 * 
 * </pre>
 * 
 * @author bryon.lawrence
 * @version 1.0
 */
public class ProductStagingPresenter extends
        HazardServicesPresenter<IProductStagingView<?, ?>> {

    // Private Static Constants

    /**
     * Logging mechanism.
     */
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(ProductStagingPresenter.class);

    // Private Variables

    /**
     * Continue command invocation handler.
     */
    private final ICommandInvocationHandler continueHandler = new ICommandInvocationHandler() {
        @Override
        public void commandInvoked(String command) {
            try {
                String issueFlag = (getView().isToBeIssued() ? Boolean.TRUE
                        .toString() : Boolean.FALSE.toString());
                ProductStagingAction action = new ProductStagingAction();
                action.setIssueFlag(issueFlag);
                ProductStagingInfo productStagingInfo = getView()
                        .getProductStagingInfo();

                /*
                 * TODO Right now there are no cases with more than one product.
                 * This will have to be refactored when that case comes up.
                 */
                ProductStagingInfo.Product product = productStagingInfo
                        .getProducts().get(0);

                List<String> eventIds = product.getSelectedEventIDs();
                Collection<IHazardEvent> events = getSessionManager()
                        .getEventManager().getEvents();
                Collection<IHazardEvent> selectedEvents = Lists.newArrayList();
                for (IHazardEvent eve : events) {
                    if (eventIds.contains(eve.getEventID())) {
                        eve.addHazardAttribute(
                                HazardConstants.HAZARD_EVENT_SELECTED, true);
                        selectedEvents.add(eve);
                    }
                }
                getSessionManager().getEventManager().setSelectedEvents(
                        selectedEvents);

                action.setProductStagingInfo(productStagingInfo);
                fireAction(action);
            } catch (Exception e1) {
                statusHandler.error("ProductStatingPresenter.bind(): ", e1);
            }
        }
    };

    // Public Constructors

    /**
     * Construct a standard instance of a product staging presenter.
     * 
     * @param model
     *            Model to be handled by this presenter.
     * @param view
     *            Product staging view to be handled by this presenter.
     * @param eventBus
     *            Event bus used to signal changes.
     */
    public ProductStagingPresenter(ISessionManager model,
            IProductStagingView<?, ?> view, EventBus eventBus) {
        super(model, view, eventBus);
    }

    // Public Methods

    /**
     * Receive notification of a model change. For the moment, the product
     * staging dialog doesn't care about model event.
     * 
     * @param changes
     *            Set of elements within the model that have changed.
     */
    @Override
    public void modelChanged(EnumSet<HazardConstants.Element> changed) {

        // No action.
    }

    /**
     * Show a subview providing setting detail for the current setting.
     * 
     * @param issueFlag
     *            Whether or not this is a result of an issue action
     * @param productStagingInfo
     */
    public final void showProductStagingDetail(boolean issueFlag,
            ProductStagingInfo productStagingInfo) {
        getView().showProductStagingDetail(issueFlag, productStagingInfo);
        bind();
    }

    // Protected Methods

    /**
     * Initialize the specified view in a subclass-specific manner.
     * 
     * @param view
     *            View to be initialized.
     */
    @Override
    public void initialize(IProductStagingView<?, ?> view) {

        // No action.
    }

    /**
     * Binds the presenter to the view which implements the IProductStagingView
     * interface. The interface is the contract, and it is all the presenter
     * needs to know about the view. This allows different views to easily be
     * created and given to this presenter.
     * <p>
     * By binding to the view, the presenter handles all of the view's events.
     */
    private void bind() {
        getView().getContinueInvoker().setCommandInvocationHandler(
                continueHandler);
    }
}

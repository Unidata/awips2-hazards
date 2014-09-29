/**
 * This software was developed and / or modified by the National Oceanic and
 * Atmospheric Administration (NOAA), Earth System Research Laboratory (ESRL),
 * Global Systems Division (GSD), Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.productstaging;

import gov.noaa.gsd.common.eventbus.BoundedReceptionEventBus;
import gov.noaa.gsd.viz.hazards.display.HazardServicesPresenter;
import gov.noaa.gsd.viz.hazards.display.action.ProductStagingAction;
import gov.noaa.gsd.viz.megawidgets.CheckListSpecifier;
import gov.noaa.gsd.viz.megawidgets.GroupSpecifier;
import gov.noaa.gsd.viz.megawidgets.MegawidgetSpecifier;
import gov.noaa.gsd.viz.mvp.widgets.ICommandInvocationHandler;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.hazards.sessionmanager.ISessionManager;
import com.raytheon.uf.viz.hazards.sessionmanager.events.impl.ObservedHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.IOriginator;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.Originator;
import com.raytheon.uf.viz.hazards.sessionmanager.product.ProductGeneratorInformation;
import com.raytheon.uf.viz.hazards.sessionmanager.product.ProductStagingInfo;

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
 * Apr 11, 2014   2819     Chris.Golden      Fixed bugs with the Preview and Issue
 *                                           buttons in the HID remaining grayed out
 *                                           when they should be enabled.
 * May 17, 2014 2925       Chris.Golden      Changed to work with MVP framework
 *                                           widget changes. Also added newly
 *                                           required implementation of
 *                                           reinitialize(), and made initialize()
 *                                           protected as it is called by setView().
 * Jun 30, 2014 3512    Chris.Golden         Changed to work with changes to
 *                                           ICommandInvoker.
 * Sep 09, 2014 4042    Chris.Golden         Moved product staging info generation
 *                                           method into this class where it belongs,
 *                                           and changed it to use maps instead of
 *                                           Field instances to hold raw megawidget
 *                                           specifiers.
 * </pre>
 * 
 * @author bryon.lawrence
 * @version 1.0
 */
public class ProductStagingPresenter extends
        HazardServicesPresenter<IProductStagingView<?, ?>> implements
        IOriginator {

    // Private Static Constants

    /**
     * Label for the related hazards megawidget.
     */
    private static final String COMBINE_MESSAGE = "When issuing this hazard, there are other related hazards that could be included in the legacy product:";

    /**
     * Type of the related hazards megawidget.
     */
    private static final String CHECK_LIST_FIELD_TYPE = "CheckList";

    /**
     * Identifier of the related hazards megawidget.
     */
    private static final String POSSIBLE_EVENTS_IDENTIFIER = "possibleEvents";

    /**
     * Type of the related hazards grouping megawidget.
     */
    private static final String GROUP_FIELD_TYPE = "Group";

    /**
     * Label of the related hazards grouping megawidget.
     */
    private static final String EVENTS_SECTION_TEXT = "Events";

    /**
     * Margins for the related hazards grouping megawidget.
     */
    private static final Integer GROUP_BORDER_MARGIN = 10;

    /**
     * Logging mechanism.
     */
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(ProductStagingPresenter.class);

    // Private Variables

    /**
     * Continue command invocation handler.
     */
    private final ICommandInvocationHandler<String> commandHandler = new ICommandInvocationHandler<String>() {
        @Override
        public void commandInvoked(String identifier) {
            try {
                if (identifier.equals(HazardConstants.CONTINUE_BUTTON)) {
                    String issueFlag = (getView().isToBeIssued() ? Boolean.TRUE
                            .toString() : Boolean.FALSE.toString());
                    ProductStagingAction action = new ProductStagingAction();
                    action.setIssueFlag(issueFlag);
                    ProductStagingInfo productStagingInfo = getView()
                            .getProductStagingInfo();

                    Collection<ObservedHazardEvent> selectedEvents = Lists
                            .newArrayList();
                    for (ProductStagingInfo.Product product : productStagingInfo
                            .getProducts()) {

                        List<String> eventIds = product.getSelectedEventIDs();
                        Collection<ObservedHazardEvent> events = getModel()
                                .getEventManager().getEvents();

                        for (ObservedHazardEvent eve : events) {
                            if (eventIds.contains(eve.getEventID())) {
                                eve.addHazardAttribute(
                                        HazardConstants.HAZARD_EVENT_SELECTED,
                                        true);
                                selectedEvents.add(eve);
                            }
                        }
                    }

                    /*
                     * TODO: This sort of logic, and maybe the above, is
                     * business logic, and belongs in the session manager
                     * somewhere. A good indicator of this is that the use of
                     * Originator.OTHER is not really appropriate within a
                     * presenter.
                     */
                    getModel().getEventManager().setSelectedEvents(
                            selectedEvents, Originator.OTHER);

                    action.setProductStagingInfo(productStagingInfo);
                    fireAction(action);
                } else {
                    if (getView().isToBeIssued()) {
                        getModel().setIssueOngoing(false);
                    } else {
                        getModel().setPreviewOngoing(false);
                    }
                }
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
     * @param eventBus
     *            Event bus used to signal changes.
     */
    public ProductStagingPresenter(ISessionManager<ObservedHazardEvent> model,
            BoundedReceptionEventBus<Object> eventBus) {
        super(model, eventBus);
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
     * @param allProductGeneratorInformationForSelectedHazards
     * @param productStagingInfo
     */
    public final void showProductStagingDetail(boolean issueFlag,
            Collection<ProductGeneratorInformation> allProductGeneratorInformationForSelectedHazards) {
        getView().showProductStagingDetail(issueFlag,
                buildProductStagingInfo(issueFlag, allProductGeneratorInformationForSelectedHazards));
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
    protected void initialize(IProductStagingView<?, ?> view) {

        /*
         * No action.
         */
    }

    @Override
    protected void reinitialize(IProductStagingView<?, ?> view) {

        /*
         * No action.
         */
    }

    // Private Methods

    /**
     * Binds the presenter to the view which implements the IProductStagingView
     * interface. The interface is the contract, and it is all the presenter
     * needs to know about the view. This allows different views to easily be
     * created and given to this presenter.
     * <p>
     * By binding to the view, the presenter handles all of the view's events.
     */
    private void bind() {
        getView().getCommandInvoker().setCommandInvocationHandler(
                commandHandler);
    }

    /**
     * Compile the product staging info.
     * 
     * @param issue
     *            Flag indicating whether this is for an issue or preview
     *            action.
     * @param allProductGeneratorInformationForSelectedHazards
     * @return Product staging info that has been put together.
     */
    @SuppressWarnings("unchecked")
    private ProductStagingInfo buildProductStagingInfo(boolean issue,
            Collection<ProductGeneratorInformation> allProductGeneratorInformationForSelectedHazards) {
        ProductStagingInfo result = new ProductStagingInfo();
        for (ProductGeneratorInformation productGeneratorInformation : allProductGeneratorInformationForSelectedHazards) {
            ProductStagingInfo.Product product = new ProductStagingInfo.Product(
                    productGeneratorInformation.getProductGeneratorName());

            result.addProducts(product);
            for (IHazardEvent event : productGeneratorInformation.getProductEvents()) {
                product.addSelectedEventIDs(event.getEventID());
            }
            Map<String, Serializable> dialogInfo = productGeneratorInformation.getDialogInfo();
            if (dialogInfo.isEmpty() == false) {

                List<Map<String, Serializable>> allFieldParameters = (List<Map<String, Serializable>>) dialogInfo
                        .get(HazardConstants.FIELDS);

                for (Map<String, Serializable> fieldParameters : allFieldParameters) {
                    product.addField(new HashMap<String, Object>(
                            fieldParameters));
                }

            }

            if (productGeneratorInformation.getPossibleProductEvents().isEmpty() == false) {

                Map<String, Object> field = new HashMap<>();
                product.addField(field);
                field.put(MegawidgetSpecifier.MEGAWIDGET_IDENTIFIER,
                        POSSIBLE_EVENTS_IDENTIFIER);
                field.put(MegawidgetSpecifier.MEGAWIDGET_TYPE, GROUP_FIELD_TYPE);
                field.put(MegawidgetSpecifier.MEGAWIDGET_LABEL,
                        EVENTS_SECTION_TEXT);
                field.put(GroupSpecifier.LEFT_MARGIN, GROUP_BORDER_MARGIN);
                field.put(GroupSpecifier.RIGHT_MARGIN, GROUP_BORDER_MARGIN);
                field.put(GroupSpecifier.BOTTOM_MARGIN, GROUP_BORDER_MARGIN);
                List<Map<String, Object>> childFields = new ArrayList<>();
                field.put(GroupSpecifier.CHILD_MEGAWIDGETS, childFields);

                Map<String, Object> subField = new HashMap<>();
                childFields.add(subField);

                subField.put(MegawidgetSpecifier.MEGAWIDGET_IDENTIFIER,
                        HazardConstants.HAZARD_EVENT_IDS);
                subField.put(MegawidgetSpecifier.MEGAWIDGET_TYPE,
                        CHECK_LIST_FIELD_TYPE);
                subField.put(MegawidgetSpecifier.MEGAWIDGET_LABEL,
                        COMBINE_MESSAGE);

                /*
                 * Make the sub-window containing the selectable events fall
                 * within a reasonable size range.
                 */
                subField.put(
                        CheckListSpecifier.MEGAWIDGET_VISIBLE_LINES,
                        Math.min(10,
                                Math.max(productGeneratorInformation.getProductEvents().size(), 5)));

                List<IHazardEvent> eventChoices = new ArrayList<IHazardEvent>();
                eventChoices.addAll(productGeneratorInformation.getProductEvents());
                eventChoices.addAll(productGeneratorInformation.getPossibleProductEvents());

                List<Map<String, Object>> choices = new ArrayList<>();
                subField.put(CheckListSpecifier.MEGAWIDGET_VALUE_CHOICES,
                        choices);
                for (IHazardEvent event : eventChoices) {

                    StringBuilder displayString = new StringBuilder();
                    displayString.append(event.getEventID());
                    displayString.append(" ");
                    displayString.append(event.getPhenomenon());
                    displayString.append(".");
                    displayString.append(event.getSignificance());
                    if (event.getSubType() != null) {
                        displayString.append(".");
                        displayString.append(event.getSubType());
                    }

                    Map<String, Object> choice = new HashMap<>();
                    choices.add(choice);
                    choice.put(CheckListSpecifier.CHOICE_NAME,
                            displayString.toString());
                    choice.put(CheckListSpecifier.CHOICE_IDENTIFIER,
                            event.getEventID());

                }
            }

        }

        return result;
    }

}

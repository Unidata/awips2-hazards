/**
 * This software was developed and / or modified by the National Oceanic and
 * Atmospheric Administration (NOAA), Earth System Research Laboratory (ESRL),
 * Global Systems Division (GSD), Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.productstaging;

import gov.noaa.gsd.common.eventbus.BoundedReceptionEventBus;
import gov.noaa.gsd.viz.hazards.UIOriginator;
import gov.noaa.gsd.viz.hazards.display.HazardServicesPresenter;
import gov.noaa.gsd.viz.megawidgets.MegawidgetSpecifierManager;
import gov.noaa.gsd.viz.mvp.widgets.ICommandInvocationHandler;
import gov.noaa.gsd.viz.mvp.widgets.IQualifiedStateChangeHandler;
import gov.noaa.gsd.viz.mvp.widgets.IStateChangeHandler;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardEventUtilities;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.time.TimeRange;
import com.raytheon.uf.viz.hazards.sessionmanager.ISessionManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.ObservedSettings;
import com.raytheon.uf.viz.hazards.sessionmanager.events.ISessionEventManager;
import com.raytheon.uf.viz.hazards.sessionmanager.events.impl.ObservedHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.IOriginator;
import com.raytheon.uf.viz.hazards.sessionmanager.product.ISessionProductManager;
import com.raytheon.uf.viz.hazards.sessionmanager.product.ISessionProductManager.StagingRequired;
import com.raytheon.uf.viz.hazards.sessionmanager.product.ProductGeneratorInformation;

/**
 * Product staging presenter, used to mediate between the model and the product
 * staging view.
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
 * Oct 07, 2014   4042     Chris.Golden      Completely refactored to work with a
 *                                           two-step dialog, with the first stage
 *                                           allowing the user to choose additional
 *                                           events to go into each of the products
 *                                           (if applicable), and the second step
 *                                           allowing the user to change any
 *                                           product-generator-specific parameters
 *                                           specified for the products (again, if
 *                                           applicable).
 * Nov 18, 2014   4124     Chris.Golden      Adapted to new time manager.
 * Dec 05, 2014   4124     Chris.Golden      Changed to work with newly parameterized
 *                                           config manager.
 * </pre>
 * 
 * @author bryon.lawrence
 * @version 1.0
 */
public class ProductStagingPresenter extends
        HazardServicesPresenter<IProductStagingViewDelegate<?, ?>> implements
        IOriginator {

    // Public Enumerated Types

    /**
     * Commands that may be invoked within the product staging dialog.
     * 
     * TODO: This is only actually public because the automated tests (e.g.
     * ProductStagingViewForTesting) need it to be. If not for that, it would be
     * package-private.
     */
    public enum Command {
        CANCEL, BACK, CONTINUE
    };

    // Private Variables

    /**
     * Flag indicating whether the product staging dialog, if currently showing,
     * is staging events for potential issuance; if false, they are being staged
     * for previewing.
     */
    private boolean issue;

    /**
     * Current step being shown by the product staging dialog.
     */
    private StagingRequired stagingRequired = StagingRequired.NONE;

    /**
     * Map pairing product generator names with lists of event identifiers that
     * are to be incorporated into their generated products (used for the first
     * step).
     */
    private final Map<String, List<String>> selectedEventIdentifiersForProductGeneratorNames = new HashMap<>();

    /**
     * Map pairing product generator names with maps of
     * product-generator-specific options and their values as chosen by the user
     * (used for the second step).
     */
    private final Map<String, Map<String, Serializable>> metadataMapsForProductGeneratorNames = new HashMap<>();

    /**
     * Associated events state change handler.
     */
    private final IStateChangeHandler<String, List<String>> associatedEventsChangeHandler = new IStateChangeHandler<String, List<String>>() {

        @Override
        public void stateChanged(String identifier, List<String> value) {
            selectedEventIdentifiersForProductGeneratorNames.put(identifier,
                    value);
        }

        @Override
        public void statesChanged(Map<String, List<String>> valuesForIdentifiers) {
            throw new UnsupportedOperationException(
                    "cannot change multiple products' associated events simultaneously");
        }
    };

    /**
     * Associated events state change handler.
     */
    private final IQualifiedStateChangeHandler<String, String, Object> productMetadataChangeHandler = new IQualifiedStateChangeHandler<String, String, Object>() {

        @Override
        public void stateChanged(String qualifier, String identifier,
                Object value) {
            metadataMapsForProductGeneratorNames.get(qualifier).put(identifier,
                    (Serializable) value);
        }

        @Override
        public void statesChanged(String qualifier,
                Map<String, Object> valuesForIdentifiers) {
            for (Map.Entry<String, Object> entry : valuesForIdentifiers
                    .entrySet()) {
                metadataMapsForProductGeneratorNames.get(qualifier).put(
                        entry.getKey(), (Serializable) entry.getValue());
            }
        }
    };

    /**
     * Button command invocation handler.
     */
    private final ICommandInvocationHandler<Command> buttonInvocationHandler = new ICommandInvocationHandler<Command>() {
        @Override
        public void commandInvoked(Command identifier) {
            switch (identifier) {
            case CANCEL:
                hideAndUnsetPreviewOrIssueOngoing();
                break;
            case BACK:
                getModel().generate(issue);
                break;
            default:
                if (stagingRequired == StagingRequired.POSSIBLE_EVENTS) {

                    /*
                     * Ensure all events that have been associated with the
                     * products are selected. If all the events have been
                     * unchecked, hide the dialog and do nothing more.
                     */
                    ISessionEventManager<ObservedHazardEvent> eventManager = getModel()
                            .getEventManager();
                    Set<ObservedHazardEvent> selectedEvents = new HashSet<>();
                    for (List<String> selectedEventIdentifiers : selectedEventIdentifiersForProductGeneratorNames
                            .values()) {
                        for (String eventIdentifier : selectedEventIdentifiers) {
                            selectedEvents.add(eventManager
                                    .getEventById(eventIdentifier));
                        }
                    }
                    if (selectedEvents.isEmpty()) {
                        hideAndUnsetPreviewOrIssueOngoing();
                        break;
                    }
                    eventManager.setSelectedEvents(selectedEvents,
                            UIOriginator.STAGING_DIALOG);

                    /*
                     * Attempt to create products using the selected events; if
                     * this requires more staging, tell the view to show the
                     * user the second step.
                     */
                    if (getModel()
                            .getProductManager()
                            .createProductsFromPreliminaryProductStaging(issue,
                                    selectedEventIdentifiersForProductGeneratorNames)) {
                        showProductStagingDetail(issue,
                                StagingRequired.PRODUCT_SPECIFIC_INFO);
                    } else {
                        getView().hide();
                    }
                } else {
                    getView().hide();
                    getModel().getProductManager()
                            .createProductsFromFinalProductStaging(issue,
                                    metadataMapsForProductGeneratorNames);
                }
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
    public ProductStagingPresenter(
            ISessionManager<ObservedHazardEvent, ObservedSettings> model,
            BoundedReceptionEventBus<Object> eventBus) {
        super(model, eventBus);
    }

    // Public Methods

    @Override
    public void modelChanged(EnumSet<HazardConstants.Element> changed) {

        /*
         * No action.
         */
    }

    /**
     * Show a subview providing a staging dialog for the currently selected
     * events that may be used to create products.
     * <p>
     * This method should only be invoked if the session product manager's
     * {@link ISessionProductManager#getAllProductGeneratorInformationForSelectedHazards(boolean)}
     * method returns at least one product generator information object that (if
     * <code>stagingRequired</code> is {@link StagingRequired#POSSIBLE_EVENTS})
     * has at least one event that may be associated with it but is currently
     * unselected; or (if <code>stagingRequired</code> is
     * {@link StagingRequired#PRODUCT_SPECIFIC_INFO}) has a megawidget specifier
     * manager associated with it.
     * 
     * @param issue
     *            Flag indicating whether or not this is a result of an issue
     *            action; if false, it is a preview.
     * @param stagingRequired
     *            Type of product staging required; must be either
     *            {@link StagingRequired#POSSIBLE_EVENTS} or
     *            {@link StagingRequired#PRODUCT_SPECIFIC_INFO}.
     */
    public final void showProductStagingDetail(boolean issue,
            StagingRequired stagingRequired) {

        /*
         * Remember the passed-in parameters for later.
         */
        this.issue = issue;
        this.stagingRequired = stagingRequired;

        /*
         * Show whichever step of the staging dialog that is required.
         */
        if (stagingRequired == StagingRequired.POSSIBLE_EVENTS) {

            /*
             * Compile a list of the products for which to show lists of events
             * that may be selected.
             */
            selectedEventIdentifiersForProductGeneratorNames.clear();
            Collection<ProductGeneratorInformation> allProductGeneratorInfo = getModel()
                    .getProductManager()
                    .getAllProductGeneratorInformationForSelectedHazards(issue);
            List<String> productNames = new ArrayList<>(
                    allProductGeneratorInfo.size());
            Map<String, List<String>> possibleEventIdsForProductNames = new HashMap<>(
                    allProductGeneratorInfo.size(), 1.0f);
            Map<String, List<String>> possibleEventDescriptionsForProductNames = new HashMap<>(
                    allProductGeneratorInfo.size(), 1.0f);
            Map<String, List<String>> selectedEventIdsForProductNames = new HashMap<>(
                    allProductGeneratorInfo.size(), 1.0f);
            for (ProductGeneratorInformation info : allProductGeneratorInfo) {

                /*
                 * Only include staging for this product if it has at least one
                 * event that is unselected, but could be used. Otherwise, just
                 * associate the selected events with it.
                 */
                String name = info.getProductGeneratorName();
                Set<IHazardEvent> selectedEvents = info.getProductEvents();
                List<String> selectedEventIds = createListOfEventIdsFromEvents(selectedEvents);
                selectedEventIdentifiersForProductGeneratorNames.put(name,
                        selectedEventIds);
                Set<IHazardEvent> unselectedEvents = info
                        .getPossibleProductEvents();
                if (unselectedEvents.isEmpty()) {
                    continue;
                }
                productNames.add(name);
                Set<IHazardEvent> events = Sets.union(selectedEvents,
                        unselectedEvents);
                List<String> possibleEventIds = createListOfEventIdsFromEvents(events);
                List<String> possibleEventDescriptions = new ArrayList<>(
                        possibleEventIds.size());
                for (String eventId : possibleEventIds) {
                    possibleEventDescriptions.add(eventId
                            + " "
                            + HazardEventUtilities.getHazardType(getModel()
                                    .getEventManager().getEventById(eventId)));
                }
                possibleEventIdsForProductNames.put(name, possibleEventIds);
                possibleEventDescriptionsForProductNames.put(name,
                        possibleEventDescriptions);
                selectedEventIdsForProductNames.put(name, selectedEventIds);
            }

            /*
             * Ensure that the list of products for which to show the dialog is
             * not empty, then show it.
             */
            assert (productNames.isEmpty() == false);
            getView().showFirstStep(productNames,
                    possibleEventIdsForProductNames,
                    possibleEventDescriptionsForProductNames,
                    selectedEventIdsForProductNames);
        } else {

            /*
             * Compile a list of the products for which to show product-specific
             * information-gathering megawidgets. Also, determine whether the
             * first step of the dialog was skipped by seeing whether any of the
             * products have possible events that could have been associated
             * with them by the user.
             */
            metadataMapsForProductGeneratorNames.clear();
            Collection<ProductGeneratorInformation> allProductGeneratorInfo = getModel()
                    .getProductManager()
                    .getAllProductGeneratorInformationForSelectedHazards(issue);
            List<String> productNames = new ArrayList<>(
                    allProductGeneratorInfo.size());
            Map<String, MegawidgetSpecifierManager> megawidgetSpecifierManagersForProductNames = new HashMap<>(
                    allProductGeneratorInfo.size(), 1.0f);
            boolean firstStepSkipped = true;
            for (ProductGeneratorInformation info : allProductGeneratorInfo) {

                /*
                 * Only including staging for this product if it includes a
                 * megawidget specifier manager to be used to create megawidgets
                 * to get product-specific information from the user. Otherwise,
                 * just associate an empty metadata map with it.
                 */
                String name = info.getProductGeneratorName();
                MegawidgetSpecifierManager specifierManager = info
                        .getStagingDialogMegawidgetSpecifierManager();
                if (info.getPossibleProductEvents().isEmpty() == false) {
                    firstStepSkipped = false;
                }
                if (specifierManager == null) {
                    metadataMapsForProductGeneratorNames.put(name,
                            Collections.<String, Serializable> emptyMap());
                } else {
                    productNames.add(name);
                    Map<String, Object> rawStartingStates = new HashMap<>();
                    specifierManager
                            .populateWithStartingStates(rawStartingStates);
                    Map<String, Serializable> startingStates = new HashMap<>(
                            rawStartingStates.size(), 1.0f);
                    for (Map.Entry<String, Object> entry : rawStartingStates
                            .entrySet()) {
                        startingStates.put(entry.getKey(),
                                (Serializable) entry.getValue());
                    }
                    metadataMapsForProductGeneratorNames.put(name,
                            startingStates);
                    megawidgetSpecifierManagersForProductNames.put(name,
                            specifierManager);
                }
            }

            /*
             * Ensure that the list of products for which to show the dialog is
             * not empty, then show it.
             */
            assert (productNames.isEmpty() == false);
            TimeRange visibleTimeRange = getModel().getTimeManager()
                    .getVisibleTimeRange();
            getView().showSecondStep(productNames,
                    megawidgetSpecifierManagersForProductNames,
                    visibleTimeRange.getStart().getTime(),
                    visibleTimeRange.getEnd().getTime(), firstStepSkipped);
        }

        /*
         * Bind to the dialog's handlers so as to be notified of its invocations
         * and state changes.
         */
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
    protected void initialize(IProductStagingViewDelegate<?, ?> view) {

        /*
         * No action.
         */
    }

    @Override
    protected void reinitialize(IProductStagingViewDelegate<?, ?> view) {

        /*
         * No action.
         */
    }

    // Private Methods

    /**
     * Hide the detail view and unset preview or issue ongoing, as appropriate.
     */
    private void hideAndUnsetPreviewOrIssueOngoing() {
        getView().hide();
        if (issue) {
            getModel().setIssueOngoing(false);
        } else {
            getModel().setPreviewOngoing(false);
        }
    }

    /**
     * Create a sorted list of the specified events' identifiers.
     * 
     * @param events
     *            Set of events from which to compile the list of identifiers.
     * @return Sorted list of the specified events' identifiers.
     */
    private List<String> createListOfEventIdsFromEvents(Set<IHazardEvent> events) {
        List<String> list = new ArrayList<>(events.size());
        for (IHazardEvent event : events) {
            list.add(event.getEventID());
        }
        Collections.sort(list);
        return list;
    }

    /**
     * Binds the presenter to the view, so that the presenter is notified of
     * changes to the view initiated by the user.
     */
    private void bind() {
        getView().setAssociatedEventsChangeHandler(
                associatedEventsChangeHandler);
        getView().setProductMetadataChangeHandler(productMetadataChangeHandler);
        getView().setButtonInvocationHandler(buttonInvocationHandler);
    }
}

/**
 * This software was developed and / or modified by the National Oceanic and
 * Atmospheric Administration (NOAA), Earth System Research Laboratory (ESRL),
 * Global Systems Division (GSD), Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.productstaging;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.graphics.Point;

import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IReadableHazardEvent;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.TimeRange;
import com.raytheon.uf.common.util.Pair;
import com.raytheon.uf.viz.hazards.sessionmanager.ISessionManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.ObservedSettings;
import com.raytheon.uf.viz.hazards.sessionmanager.events.ISessionEventManager;
import com.raytheon.uf.viz.hazards.sessionmanager.product.ISessionProductManager;
import com.raytheon.uf.viz.hazards.sessionmanager.product.ISessionProductManager.StagingRequired;
import com.raytheon.uf.viz.hazards.sessionmanager.product.ProductGeneratorInformation;

import gov.noaa.gsd.common.eventbus.BoundedReceptionEventBus;
import gov.noaa.gsd.viz.hazards.display.HazardServicesPresenter;
import gov.noaa.gsd.viz.megawidgets.IControlSpecifier;
import gov.noaa.gsd.viz.megawidgets.ISideEffectsApplier;
import gov.noaa.gsd.viz.megawidgets.ISpecifier;
import gov.noaa.gsd.viz.megawidgets.MegawidgetManager;
import gov.noaa.gsd.viz.megawidgets.MegawidgetSpecificationException;
import gov.noaa.gsd.viz.megawidgets.MegawidgetSpecifierManager;
import gov.noaa.gsd.viz.megawidgets.TabbedCompositeSpecifier;
import gov.noaa.gsd.viz.megawidgets.displaysettings.IDisplaySettings;
import gov.noaa.gsd.viz.megawidgets.displaysettings.SelectableMultiPageScrollSettings;
import gov.noaa.gsd.viz.megawidgets.sideeffects.PythonSideEffectsApplier;
import gov.noaa.gsd.viz.mvp.widgets.ICommandInvocationHandler;
import gov.noaa.gsd.viz.mvp.widgets.IQualifiedStateChangeHandler;

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
 * Feb 24, 2016  13929     Robert.Blum       Remove first part of staging dialog.
 * Feb 01, 2017  15556     Chris.Golden      Changed to work with new selection manager.
 * Mar 21, 2017 29996      Robert.Blum       Updates for refreshing staging dialog.
 * Jun 05, 2017 29996      Robert.Blum       Update hazard events when staging data changes.
 * Dec 17, 2017  20739     Chris.Golden      Refactored away access to directly mutable
 *                                           session events.
 * Jan 17, 2018  33428     Chris.Golden      Changed to work with new, more flexible
 *                                           toolbar contribution code.
 * </pre>
 * 
 * @author bryon.lawrence
 * @version 1.0
 */
public class ProductStagingPresenter
        extends HazardServicesPresenter<IProductStagingViewDelegate<?, ?, ?>> {

    // Public Enumerated Types

    /**
     * Commands that may be invoked within the product staging dialog.
     * 
     * TODO: This is only actually public because the automated tests (e.g.
     * ProductStagingViewForTesting) need it to be. If not for that, it would be
     * package-private.
     */
    public enum Command {
        CANCEL, CONTINUE
    };

    // Private Static Constants

    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(ProductStagingPresenter.class);

    // Private Variables

    /**
     * Flag indicating whether the product staging dialog, if currently showing,
     * is staging events for potential issuance; if false, they are being staged
     * for previewing.
     */
    private boolean issue;

    /**
     * Map pairing product generator names with maps of
     * product-generator-specific options and their values as chosen by the user
     * (used for the second step).
     */
    private final Map<String, Map<String, Serializable>> metadataMapsForProductGeneratorNames = new HashMap<>();

    private final Map<String, ProductGeneratorInformation> infoForProductGeneratorNames = new HashMap<>();

    private final Map<String, Map<String, Map<String, Object>>> visiblePagesForProductGeneratorNames = new HashMap<>();

    /**
     * Associated events state change handler.
     */
    private final IQualifiedStateChangeHandler<String, String, Object> productMetadataChangeHandler = new IQualifiedStateChangeHandler<String, String, Object>() {

        @Override
        public void stateChanged(String qualifier, String identifier,
                Object value) {
            Set<IReadableHazardEvent> productEvents = infoForProductGeneratorNames
                    .get(qualifier).getProductEvents();
            for (IReadableHazardEvent event : productEvents) {
                getModel().getEventManager().changeEventProperty(
                        event.getEventID(),
                        ISessionEventManager.ADD_EVENT_ATTRIBUTE,
                        new Pair<String, Serializable>(identifier,
                                (Serializable) value));
            }

            Set<IReadableHazardEvent> possibleEvents = infoForProductGeneratorNames
                    .get(qualifier).getPossibleProductEvents();
            for (IReadableHazardEvent event : possibleEvents) {
                getModel().getEventManager().changeEventProperty(
                        event.getEventID(),
                        ISessionEventManager.ADD_EVENT_ATTRIBUTE,
                        new Pair<String, Serializable>(identifier,
                                (Serializable) value));
            }

            metadataMapsForProductGeneratorNames.get(qualifier).put(identifier,
                    (Serializable) value);
            ProductGeneratorInformation info = infoForProductGeneratorNames
                    .get(qualifier);
            if (info.getMetadataReloadIdentifiers().contains(identifier)) {
                storeVisiblePages(qualifier);
                getView().refreshStagingMetadata(qualifier,
                        createMegawidgetSpecifierManager(qualifier, info),
                        visiblePagesForProductGeneratorNames.get(qualifier));
            }
        }

        @Override
        public void statesChanged(String qualifier,
                Map<String, Object> valuesForIdentifiers) {
            boolean refreshMetadata = false;
            Map<String, Serializable> serializableValuesForIdentifiers = new HashMap<>(
                    valuesForIdentifiers.size(), 1.0f);
            for (Map.Entry<String, Object> entry : valuesForIdentifiers
                    .entrySet()) {
                serializableValuesForIdentifiers.put(entry.getKey(),
                        (Serializable) entry.getValue());
            }
            Set<IReadableHazardEvent> productEvents = infoForProductGeneratorNames
                    .get(qualifier).getProductEvents();
            for (IReadableHazardEvent event : productEvents) {
                getModel().getEventManager().changeEventProperty(
                        event.getEventID(),
                        ISessionEventManager.ADD_EVENT_ATTRIBUTES,
                        serializableValuesForIdentifiers);
            }
            Set<IReadableHazardEvent> possibleEvents = infoForProductGeneratorNames
                    .get(qualifier).getPossibleProductEvents();
            for (IReadableHazardEvent event : possibleEvents) {
                getModel().getEventManager().changeEventProperty(
                        event.getEventID(),
                        ISessionEventManager.ADD_EVENT_ATTRIBUTES,
                        serializableValuesForIdentifiers);
            }
            for (Map.Entry<String, Object> entry : valuesForIdentifiers
                    .entrySet()) {
                metadataMapsForProductGeneratorNames.get(qualifier)
                        .put(entry.getKey(), (Serializable) entry.getValue());
                if (infoForProductGeneratorNames.get(qualifier)
                        .getMetadataReloadIdentifiers()
                        .contains(entry.getKey())) {
                    refreshMetadata = true;
                }
            }
            if (refreshMetadata) {
                storeVisiblePages(qualifier);
                getView().refreshStagingMetadata(qualifier,
                        createMegawidgetSpecifierManager(qualifier,
                                infoForProductGeneratorNames.get(qualifier)),
                        visiblePagesForProductGeneratorNames.get(qualifier));
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
            default:
                getView().hide();
                getModel().getProductManager()
                        .createProductsFromFinalProductStaging(issue,
                                metadataMapsForProductGeneratorNames);
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
    public ProductStagingPresenter(ISessionManager<ObservedSettings> model,
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
     * <code>stagingRequired</code> is
     * {@link StagingRequired#PRODUCT_SPECIFIC_INFO}) has a megawidget specifier
     * manager associated with it.
     * 
     * @param issue
     *            Flag indicating whether or not this is a result of an issue
     *            action; if false, it is a preview.
     */
    public final void showProductStagingDetail(boolean issue) {

        /*
         * Remember the passed-in parameters for later.
         */
        this.issue = issue;

        /*
         * Compile a list of the products for which to show product-specific
         * information-gathering megawidgets.
         */
        metadataMapsForProductGeneratorNames.clear();
        infoForProductGeneratorNames.clear();
        Collection<ProductGeneratorInformation> allProductGeneratorInfo = getModel()
                .getProductManager()
                .getAllProductGeneratorInformationForSelectedHazards(issue);
        List<String> productNames = new ArrayList<>(
                allProductGeneratorInfo.size());
        Map<String, MegawidgetSpecifierManager> megawidgetSpecifierManagersForProductNames = new HashMap<>(
                allProductGeneratorInfo.size(), 1.0f);
        for (ProductGeneratorInformation info : allProductGeneratorInfo) {

            /*
             * Only including staging for this product if it includes a
             * megawidget specifier manager to be used to create megawidgets to
             * get product-specific information from the user. Otherwise, just
             * associate an empty metadata map with it.
             */
            String name = info.getProductGeneratorName();
            infoForProductGeneratorNames.put(name, info);
            MegawidgetSpecifierManager specifierManager = info
                    .getStagingDialogMegawidgetSpecifierManager();
            if (specifierManager == null) {
                metadataMapsForProductGeneratorNames.put(name,
                        Collections.<String, Serializable> emptyMap());
            } else {
                productNames.add(name);
                Map<String, Object> rawStartingStates = new HashMap<>();
                specifierManager.populateWithStartingStates(rawStartingStates);
                Map<String, Serializable> startingStates = new HashMap<>(
                        rawStartingStates.size(), 1.0f);
                for (Map.Entry<String, Object> entry : rawStartingStates
                        .entrySet()) {
                    startingStates.put(entry.getKey(),
                            (Serializable) entry.getValue());
                }
                metadataMapsForProductGeneratorNames.put(name, startingStates);
                megawidgetSpecifierManagersForProductNames.put(name,
                        specifierManager);
            }
        }

        /*
         * Ensure that the list of products for which to show the dialog is not
         * empty, then show it.
         */
        assert (productNames.isEmpty() == false);
        TimeRange visibleTimeRange = getModel().getTimeManager()
                .getVisibleTimeRange();
        getView().showStagingDialog(productNames,
                megawidgetSpecifierManagersForProductNames,
                visibleTimeRange.getStart().getTime(),
                visibleTimeRange.getEnd().getTime());

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
    protected void initialize(IProductStagingViewDelegate<?, ?, ?> view) {

        /*
         * No action.
         */
    }

    @Override
    protected void reinitialize(IProductStagingViewDelegate<?, ?, ?> view) {

        /*
         * No action.
         */
    }

    // Private Methods

    /**
     * Store the visible pages for the specified product.
     * 
     * @param productName
     *            Product name.
     */
    private void storeVisiblePages(String productName) {
        MegawidgetManager manager = getView().getMegawidgetManager(productName);

        /*
         * FIXME: The below code is a complete hack to store off the currently
         * visible tabs of TabbedCompositeMegawidgets. This is needed because
         * when "refreshMetadata" is triggered the Megawidgets are disposed and
         * new ones are created. Without this code they will default back to the
         * original tab every time.
         */
        Map<String, Map<String, Object>> mutablePropertiesMap = new HashMap<>();
        for (Map.Entry<String, IDisplaySettings> entry : manager
                .getDisplaySettings().entrySet()) {
            if (entry.getValue() instanceof SelectableMultiPageScrollSettings) {
                @SuppressWarnings("unchecked")
                SelectableMultiPageScrollSettings<Point, Integer> pageSettings = (SelectableMultiPageScrollSettings<Point, Integer>) entry
                        .getValue();

                /*
                 * The Display settings above have the Integer value of the
                 * selected page. However to set the mutable property on the
                 * Megawidget we need to the String name of the page. The below
                 * code gets the name from the Specifier Manager.
                 */
                for (ISpecifier specifier : manager.getSpecifierManager()
                        .getSpecifiers()) {
                    if (specifier.getIdentifier().equals(entry.getKey())) {
                        if (specifier instanceof TabbedCompositeSpecifier) {
                            TabbedCompositeSpecifier tabbedSpec = (TabbedCompositeSpecifier) specifier;

                            String pageName = tabbedSpec.getPageNames()
                                    .get(pageSettings.getSelection());
                            Map<String, Object> visiblePageMap = new HashMap<>();
                            visiblePageMap.put("visiblePage", pageName);
                            mutablePropertiesMap.put(entry.getKey(),
                                    visiblePageMap);
                            break;
                        }
                    }
                }
            }
        }
        if (mutablePropertiesMap.isEmpty() == false) {
            visiblePagesForProductGeneratorNames.put(productName,
                    mutablePropertiesMap);
        }
    }

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
     * Binds the presenter to the view, so that the presenter is notified of
     * changes to the view initiated by the user.
     */
    private void bind() {
        getView().setProductMetadataChangeHandler(productMetadataChangeHandler);
        getView().setButtonInvocationHandler(buttonInvocationHandler);
    }

    /**
     * Create a megawidget specifier manager for the specified product
     * generator.
     * 
     * @param name
     *            Name of the product generator.
     * @param info
     *            Information about the generator.
     * @return Megawidget specifier manager.
     */
    private MegawidgetSpecifierManager createMegawidgetSpecifierManager(
            String name, ProductGeneratorInformation info) {
        List<Map<String, Object>> rawSpecifiers = getModel().getProductManager()
                .getStagingDialogMetadata(info, issue,
                        metadataMapsForProductGeneratorNames.get(name));

        /*
         * Get the side effects applier, if any.
         */
        ISideEffectsApplier sideEffectsApplier = null;
        File scriptFile = getModel().getConfigurationManager()
                .getScriptFile(info.getProductGeneratorName());
        if (PythonSideEffectsApplier
                .containsSideEffectsEntryPointFunction(scriptFile)) {
            sideEffectsApplier = new PythonSideEffectsApplier(scriptFile);
        }

        /*
         * Create the megawidget specifier manager.
         */
        try {
            return new MegawidgetSpecifierManager(rawSpecifiers,
                    IControlSpecifier.class,
                    getModel().getTimeManager().getCurrentTimeProvider(),
                    sideEffectsApplier);
        } catch (MegawidgetSpecificationException e) {
            statusHandler.error("Could not get product staging megawidgets for "
                    + info.getProductGeneratorName() + ": " + e, e);
        }
        return null;
    }
}

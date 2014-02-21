/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.display;

import gov.noaa.gsd.viz.hazards.display.ProductStagingInfo.Product;
import gov.noaa.gsd.viz.hazards.productstaging.ProductStagingPresenter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.hazards.productgen.GeneratedProductList;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.hazards.sessionmanager.ISessionManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Choice;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Field;
import com.raytheon.uf.viz.hazards.sessionmanager.product.IProductGenerationComplete;
import com.raytheon.uf.viz.hazards.sessionmanager.product.ISessionProductManager;
import com.raytheon.uf.viz.hazards.sessionmanager.product.ProductFailed;
import com.raytheon.uf.viz.hazards.sessionmanager.product.ProductGenerated;
import com.raytheon.uf.viz.hazards.sessionmanager.product.ProductGenerationComplete;
import com.raytheon.uf.viz.hazards.sessionmanager.product.ProductInformation;

/**
 * Description: Handles products generated by the Product Generation Framework.
 * TODO It seems wrong to have a separate class in this plugin to do product
 * generation handling. Ideally the {@link ISessionProductManager} should have
 * this functionality. But the current road block is that this code references
 * several other classes that are in this plugin. Do we figure out a way to move
 * the other classes into {@link ISessionProductManager}s plugin?
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 15, 2013    2182    daniel.s.schaffer@noaa.gov      Initial creation
 * Nov 21, 2013  2446      daniel.s.schaffer@noaa.gov Bug fixes in product staging dialog
 * Dec 3, 2013   1472      bkowal      subtype field is now subType
 * 
 * Dec 03, 2013 2182 daniel.s.schaffer@noaa.gov Refactoring - Update for move of JSONConverter
 * Feb 07, 2014 2890       bkowal      Product Generation JSON refactor.
 * 
 * </pre>
 * 
 * @author daniel.s.schaffer@noaa.gov
 * @version 1.0
 */
class HazardServicesProductGenerationHandler {

    private final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(this.getClass());

    private static final String COMBINE_MESSAGE = "When issuing this hazard, there are other related hazards that could be included in the legacy product:";

    private static final String CHECK_LIST_FIELD_TYPE = "CheckList";

    private static final String POSSIBLE_EVENTS_IDENTIFIER = "possibleEvents";

    private static final String GROUP_FIELD_TYPE = "Group";

    private static final String EVENTS_SECTION_TEXT = "Events";

    private static final Integer GROUP_BORDER_MARGIN = 10;

    private final ISessionManager sessionManager;

    private final ISessionProductManager productManager;

    private final Map<String, ProductGenerationAuditor> productGenerationAuditManager;

    private final EventBus eventBus;

    HazardServicesProductGenerationHandler(ISessionManager sessionManager,
            EventBus eventBus) {
        this.sessionManager = sessionManager;
        this.productManager = sessionManager.getProductManager();
        this.eventBus = eventBus;
        this.productGenerationAuditManager = new HashMap<String, ProductGenerationAuditor>();

        this.eventBus.register(this);
    }

    boolean productGenerationRequired() {
        boolean result = true;
        for (ProductInformation info : productManager.getSelectedProducts()) {
            if (info.getDialogInfo() != null && !info.getDialogInfo().isEmpty()) {
                result = false;
            } else if (info.getPossibleProductEvents() != null
                    && !info.getPossibleProductEvents().isEmpty()) {
                result = false;
            }
            if (!result) {
                break;
            }
        }
        return result;
    }

    public void generateProducts(boolean issue) {
        ISessionProductManager productManager = sessionManager
                .getProductManager();
        Collection<ProductInformation> products = productManager
                .getSelectedProducts();
        this.runProductGeneration(products, issue);
    }

    /**
     * TODO Move this into the {@link ProductStagingPresenter} after the
     * presenters have been refactored to accept {@link ISessionManager}s
     * instead of {@link IHazardServicesModel}
     */
    @SuppressWarnings("unchecked")
    public ProductStagingInfo buildProductStagingInfo() {
        Collection<ProductInformation> products = productManager
                .getSelectedProducts();

        ProductStagingInfo result = new ProductStagingInfo();
        for (ProductInformation info : products) {
            ProductStagingInfo.Product product = new ProductStagingInfo.Product(
                    info.getProductGeneratorName());

            result.addProducts(product);
            for (IHazardEvent event : info.getProductEvents()) {
                product.addSelectedEventIDs(event.getEventID());
            }
            Map<String, Serializable> dialogInfo = info.getDialogInfo();
            if (dialogInfo.size() > 0) {

                List<Map<String, Serializable>> allFieldParameters = (List<Map<String, Serializable>>) dialogInfo
                        .get(HazardConstants.FIELDS);

                for (Map<String, Serializable> fieldParameters : allFieldParameters) {
                    Field field = new Field();
                    product.addFields(field);
                    field.setLabel((String) fieldParameters
                            .get(HazardConstants.LABEL));
                    field.setFieldName((String) fieldParameters
                            .get(HazardConstants.FIELD_NAME));
                    field.setVisibleChars((Integer) fieldParameters
                            .get(HazardConstants.VISIBLE_CHARS));
                    field.setMaxChars((Integer) fieldParameters
                            .get(HazardConstants.MAX_CHARS));
                    field.setFieldType((String) fieldParameters
                            .get(HazardConstants.FIELD_TYPE));
                    field.setExpandHorizontally((Boolean) fieldParameters
                            .get(HazardConstants.EXPAND_HORIZONTALLY));

                }

            }

            if (info.getPossibleProductEvents().size() > 0) {

                Field possibleEventsField = new Field();
                product.addFields(possibleEventsField);

                possibleEventsField.setFieldName(POSSIBLE_EVENTS_IDENTIFIER);

                possibleEventsField.setFieldType(GROUP_FIELD_TYPE);

                possibleEventsField.setLabel(EVENTS_SECTION_TEXT);
                possibleEventsField.setLeftMargin(GROUP_BORDER_MARGIN);
                possibleEventsField.setRightMargin(GROUP_BORDER_MARGIN);
                possibleEventsField.setBottomMargin(GROUP_BORDER_MARGIN);
                List<Field> childFields = new ArrayList<>();
                possibleEventsField.setFields(childFields);
                Field field = new Field();
                childFields.add(field);

                field.setFieldName(HazardConstants.HAZARD_EVENT_IDS);
                field.setFieldType(CHECK_LIST_FIELD_TYPE);
                field.setLabel(COMBINE_MESSAGE);

                /*
                 * Make the sub-window containing the selectable events fall
                 * within a reasonable size range.
                 */
                field.setLines(Math.min(10,
                        Math.max(info.getProductEvents().size(), 5)));

                List<IHazardEvent> eventChoices = new ArrayList<IHazardEvent>();
                eventChoices.addAll(info.getProductEvents());
                eventChoices.addAll(info.getPossibleProductEvents());

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

                    Choice choice = new Choice();
                    field.addChoice(choice);
                    choice.setDisplayString(displayString.toString());
                    choice.setIdentifier(event.getEventID());

                }
            }

        }

        return result;
    }

    /**
     * createProductsFromHazardEventSets -- Generate products from Hazard Event
     * Sets created from the Product Staging Dialog or by the previous method,
     * "createProductsFromEventIDs"
     * 
     * @param issueFlag
     *            -- if True, issue the results
     * @param hazardEventSets
     * 
     */
    public void createProductsFromHazardEventSets(boolean issue,
            List<GeneratedProductList> generatedProductsList) {
        Collection<ProductInformation> selectedProducts = productManager
                .getSelectedProducts();
        ProductInformation productInformation = null;

        Collection<ProductInformation> productsToGenerate = new ArrayList<ProductInformation>();

        List<String> selectedEventIDs = new ArrayList<>();
        for (IHazardEvent selectedEvent : this.sessionManager.getEventManager()
                .getSelectedEvents()) {
            selectedEventIDs.add(selectedEvent.getEventID());
        }

        for (GeneratedProductList productList : generatedProductsList) {
            for (ProductInformation selectedProductInformation : selectedProducts) {
                if (productList.getProductInfo().equals(
                        selectedProductInformation.getProductGeneratorName())) {
                    productInformation = selectedProductInformation;
                    break;
                }
            }

            Set<IHazardEvent> selectedEvents = new HashSet<IHazardEvent>();
            for (IHazardEvent hazardEvent : productInformation
                    .getProductEvents()) {
                if (selectedEventIDs.contains(hazardEvent.getEventID())) {
                    selectedEvents.add(hazardEvent);
                }
            }
            for (IHazardEvent hazardEvent : productInformation
                    .getPossibleProductEvents()) {
                if (selectedEventIDs.contains(hazardEvent.getEventID())) {
                    selectedEvents.add(hazardEvent);
                }
            }

            productInformation.setProductEvents(selectedEvents);
            productsToGenerate.add(productInformation);
        }

        this.runProductGeneration(productsToGenerate, issue);
    }

    public void createProductsFromProductStagingInfo(boolean issue,
            ProductStagingInfo productStagingInfo) {
        Collection<ProductInformation> products = productManager
                .getSelectedProducts();

        Collection<ProductInformation> productsToGenerate = new ArrayList<ProductInformation>();

        for (Product stagedProduct : productStagingInfo.getProducts()) {
            for (ProductInformation product : products) {
                if (stagedProduct.getProductGenerator().equals(
                        product.getProductGeneratorName())) {

                    Set<IHazardEvent> selectedEvents = new HashSet<IHazardEvent>();
                    for (String eventID : stagedProduct.getSelectedEventIDs()) {
                        for (IHazardEvent event : product.getProductEvents()) {
                            if (event.getEventID().equals(eventID)) {
                                selectedEvents.add(event);
                                break;
                            }
                        }
                        for (IHazardEvent event : product
                                .getPossibleProductEvents()) {
                            if (event.getEventID().equals(eventID)) {
                                selectedEvents.add(event);
                                break;
                            }
                        }
                    }
                    product.setProductEvents(selectedEvents);
                    product.setDialogSelections(stagedProduct
                            .getDialogSelections());
                    productsToGenerate.add(product);
                }
            }
        }
        this.runProductGeneration(productsToGenerate, issue);
    }

    private void runProductGeneration(
            Collection<ProductInformation> productInformationRecords,
            boolean issue) {
        boolean confirm = issue;

        /*
         * Build an audit trail to keep track of the products that have been /
         * will need to be generated.
         */
        synchronized (this.productGenerationAuditManager) {
            final String productGenerationTrackingID = UUID.randomUUID()
                    .toString();
            ProductGenerationAuditor productGenerationAuditor = new ProductGenerationAuditor(
                    issue, productGenerationTrackingID);
            for (ProductInformation productInformation : productInformationRecords) {
                productInformation.setGenerationID(productGenerationTrackingID);
                productGenerationAuditor
                        .addProductToBeGenerated(productInformation);
            }
            this.productGenerationAuditManager.put(productGenerationTrackingID,
                    productGenerationAuditor);
        }

        for (ProductInformation productInformation : productInformationRecords) {
            this.productManager.generate(productInformation, issue, confirm);
            confirm = false;
        }
    }

    @Subscribe
    public void auditProductGeneration(ProductGenerated generated) {
        ProductGenerationAuditor productGenerationAuditor = null;
        ProductInformation productInformation = generated
                .getProductInformation();
        final String generationID = productInformation.getGenerationID();
        final GeneratedProductList generatedProducts = productInformation
                .getProducts();
        synchronized (this.productGenerationAuditManager) {
            if (this.productGenerationAuditManager.get(generationID)
                    .productGenerated(generatedProducts, productInformation) == false) {
                return;
            }

            productGenerationAuditor = this.productGenerationAuditManager
                    .remove(generationID);
        }

        this.publishGenerationCompletion(productGenerationAuditor);
    }

    @Subscribe
    public void handleProductGeneratorResult(ProductFailed failed) {
        ProductGenerationAuditor productGenerationAuditor = null;
        ProductInformation productInformation = failed.getProductInformation();
        final String generationID = productInformation.getGenerationID();
        synchronized (this.productGenerationAuditManager) {
            if (this.productGenerationAuditManager.get(generationID)
                    .productGenerationFailure(productInformation) == false) {
                return;
            }

            productGenerationAuditor = this.productGenerationAuditManager
                    .remove(generationID);
        }

        this.publishGenerationCompletion(productGenerationAuditor);
        statusHandler.error("Product Generator "
                + failed.getProductInformation().getProductGeneratorName()
                + " failed.");
    }

    private void publishGenerationCompletion(
            ProductGenerationAuditor productGenerationAuditor) {
        IProductGenerationComplete productGenerationComplete = new ProductGenerationComplete(
                productGenerationAuditor.isIssue(),
                productGenerationAuditor.getGeneratedProducts());
        this.eventBus.post(productGenerationComplete);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}

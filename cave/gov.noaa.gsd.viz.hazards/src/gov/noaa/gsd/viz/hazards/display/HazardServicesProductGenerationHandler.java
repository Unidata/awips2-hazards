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

import gov.noaa.gsd.common.utilities.JSONConverter;
import gov.noaa.gsd.viz.hazards.display.ProductStagingInfo.Product;
import gov.noaa.gsd.viz.hazards.display.deprecated.ProductGenerationResult;
import gov.noaa.gsd.viz.hazards.display.deprecated.ProductGenerationResult.DeprecatedChoice;
import gov.noaa.gsd.viz.hazards.display.deprecated.ProductGenerationResult.DeprecatedField;
import gov.noaa.gsd.viz.hazards.display.deprecated.ProductGenerationResult.GeneratedProduct;
import gov.noaa.gsd.viz.hazards.display.deprecated.ProductGenerationResult.HazardEventSet;
import gov.noaa.gsd.viz.hazards.display.deprecated.ProductGenerationResult.StagingInfo;
import gov.noaa.gsd.viz.hazards.productstaging.ProductStagingPresenter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.hazards.productgen.IGeneratedProduct;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.hazards.sessionmanager.ISessionManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Choice;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Field;
import com.raytheon.uf.viz.hazards.sessionmanager.product.ISessionProductManager;
import com.raytheon.uf.viz.hazards.sessionmanager.product.ProductFailed;
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

    private final ISessionManager sessionManager;

    private int numProducts = 0;

    private boolean issue;

    private final JSONConverter jsonConverter;

    // The product editor presenter doesn't handle async multiple generations
    // very well so this code must accumulate products.
    private ProductGenerationResult generatedProducts = null;

    private final ISessionProductManager productManager;

    HazardServicesProductGenerationHandler(ISessionManager sessionManager,
            EventBus eventBus) {
        this.sessionManager = sessionManager;
        this.productManager = sessionManager.getProductManager();
        this.jsonConverter = new JSONConverter();
        eventBus.register(this);

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

    void generateProducts(boolean issue) {
        ISessionProductManager productManager = sessionManager
                .getProductManager();
        Collection<ProductInformation> products = productManager
                .getSelectedProducts();
        generatedProducts = new ProductGenerationResult();
        numProducts = products.size();
        this.issue = issue;
        boolean confirm = true;
        for (ProductInformation info : products) {
            productManager.generate(info, issue, confirm);
            confirm = false;
        }
    }

    /**
     * TODO Move this into the {@link ProductStagingPresenter} after the
     * presenters have been refactored to accept {@link ISessionManager}s
     * instead of {@link IHazardServicesModel}
     */
    ProductStagingInfo buildProductStagingInfo() {
        Collection<ProductInformation> products = productManager
                .getSelectedProducts();

        ProductStagingInfo result = new ProductStagingInfo();
        for (ProductInformation info : products) {
            ProductStagingInfo.Product product = new ProductStagingInfo.Product(
                    info.getProductGeneratorName());
            if (info.getPossibleProductEvents().size() > 0) {
                result.addProducts(product);
                List<Field> fields = Lists.newArrayList();
                Field field = new Field();
                fields.add(field);
                product.addFields(field);
                for (IHazardEvent event : info.getProductEvents()) {
                    product.addSelectedEventIDs(event.getEventID());
                }

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
    void createProductsFromHazardEventSets(boolean issue, String hazardEventSets) {
        Collection<ProductInformation> products = productManager
                .getSelectedProducts();
        ProductGenerationResult result = jsonConverter.fromJson(
                hazardEventSets, ProductGenerationResult.class);
        generatedProducts = new ProductGenerationResult();
        numProducts = result.getHazardEventSets().length;
        this.issue = issue;
        boolean confirm = true;
        for (HazardEventSet set : result.getHazardEventSets()) {
            ProductInformation info = null;
            for (ProductInformation testInfo : products) {
                if (set.getProductGeneratorName().equals(testInfo.getProductGeneratorName())) {
                    info = testInfo;
                    break;
                }
            }
            Set<IHazardEvent> selectedEvents = new HashSet<IHazardEvent>();
            String[] events = set.getStagingInfo().getValueDict()
                    .get("eventIDs");
            for (String eventID : events) {
                for (IHazardEvent event : info.getProductEvents()) {
                    if (event.getEventID().equals(eventID)) {
                        selectedEvents.add(event);
                        break;
                    }
                }
                for (IHazardEvent event : info.getPossibleProductEvents()) {
                    if (event.getEventID().equals(eventID)) {
                        selectedEvents.add(event);
                        break;
                    }
                }
            }
            info.setProductEvents(selectedEvents);
            productManager.generate(info, issue, confirm);
            confirm = false;
        }
    }

    /**
     * TODO Replace use of {@link ProductGenerationResult} after Product Editor
     * Dialog is refactored.
     */
    public void createProductsFromProductStagingInfo(boolean issue,
            ProductStagingInfo productStagingInfo) {
        Collection<ProductInformation> products = productManager
                .getSelectedProducts();
        generatedProducts = new ProductGenerationResult();
        numProducts = productStagingInfo.numProducts();
        this.issue = issue;
        boolean confirm = true;
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
                    productManager.generate(product, issue, confirm);
                    confirm = false;
                }
            }

        }
    }

    /**
     * Handle the generated products from an asynchronous run of a product
     * generator Collect the results for the list of product generators run When
     * all are collected, issue or display them
     * 
     * @param productGeneratorName
     *            -- name of product generator
     * @param generatedProducts
     *            -- list of IGeneratedProduct Java object
     * 
     */
    String handleProductGeneratorResult(String productGeneratorName,
            List<IGeneratedProduct> generatedProductsList) {
        numProducts -= 1;

        Collection<IHazardEvent> selectedEvents = sessionManager
                .getEventManager().getSelectedEvents();

        ProductGenerationResult result = generatedProducts;
        result.setReturnType(HazardConstants.GENERATED_PRODUCTS);
        List<GeneratedProduct> products = new ArrayList<GeneratedProduct>();
        for (IGeneratedProduct product : generatedProductsList) {
            GeneratedProduct genProduct = new GeneratedProduct();
            genProduct.setProductID(product.getProductID());
            for (String formatKey : product.getEntries().keySet()) {
                genProduct.addProduct(formatKey, product.getEntry(formatKey)
                        .get(0).toString());
            }
            products.add(genProduct);
        }
        if (products.isEmpty()) {
            GeneratedProduct genProduct = new GeneratedProduct();
            genProduct.setProductID("EMPTY");
            genProduct
                    .addProduct("EMPTY",
                            " EMPTY PRODUCT!  PLEASE MAKE SURE HAZARD(S) ARE WITHIN YOUR SITE CWA. ");
            products.add(genProduct);
        }
        if (result.getGeneratedProducts() != null) {
            products.addAll(Arrays.asList(result.getGeneratedProducts()));
        }
        result.setGeneratedProducts(products.toArray(new GeneratedProduct[0]));
        DeprecatedField field = new DeprecatedField();
        field.setLines(selectedEvents.size());
        List<DeprecatedChoice> choices = new ArrayList<DeprecatedChoice>();
        List<String> eventIDs = new ArrayList<String>();
        for (IHazardEvent event : selectedEvents) {
            DeprecatedChoice choice = new DeprecatedChoice();
            StringBuilder eventDisplayString = new StringBuilder(
                    event.getEventID());
            if (event.getPhenomenon() != null) {
                eventDisplayString.append(" ");
                eventDisplayString.append(event.getPhenomenon());
                if (event.getSignificance() != null) {
                    eventDisplayString.append(".");
                    eventDisplayString.append(event.getSignificance());
                    if (event.getSubType() != null) {
                        eventDisplayString.append(".");
                        eventDisplayString.append(event.getSubType());
                    }
                }
            }
            choice.setDisplayString(eventDisplayString.toString());
            choice.setIdentifier(event.getEventID());
            choices.add(choice);
            eventIDs.add(event.getEventID());
        }
        if (issue) {
            result = new ProductGenerationResult();
            result.setReturnType(null);
            return jsonConverter.toJson(result);
        }
        field.setChoices(choices.toArray(new DeprecatedChoice[0]));
        field.setFieldName("eventIDs");
        field.setFieldType(CHECK_LIST_FIELD_TYPE);
        field.setLabel(COMBINE_MESSAGE);
        StagingInfo stagingInfo = new StagingInfo();
        stagingInfo.setFields(new DeprecatedField[] { field });
        Map<String, String[]> valueDict = new HashMap<String, String[]>();
        valueDict.put("eventIDs", eventIDs.toArray(new String[0]));
        stagingInfo.setValueDict(valueDict);
        HazardEventSet hes = new HazardEventSet();
        hes.setStagingInfo(stagingInfo);
        hes.setProductGeneratorName(productGeneratorName);
        hes.setDialogInfo(new HashMap<String, String>());
        List<HazardEventSet> sets = new ArrayList<HazardEventSet>();
        sets.add(hes);
        if (result.getHazardEventSets() != null) {
            sets.addAll(Arrays.asList(result.getHazardEventSets()));
        }
        result.setHazardEventSets(sets.toArray(new HazardEventSet[0]));
        if (numProducts > 0) {
            return null;
        }
        return jsonConverter.toJson(result);
    }

    @Subscribe
    public void handleProductGeneratorResult(ProductFailed failed) {
        statusHandler.error("Product Generator "
                + failed.getProductInformation().getProductGeneratorName() + " failed.");

    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}

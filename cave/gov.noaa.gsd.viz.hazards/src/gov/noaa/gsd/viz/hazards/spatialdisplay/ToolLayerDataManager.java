/**
 * This software was developed and / or modified by the National Oceanic and
 * Atmospheric Administration (NOAA), Earth System Research Laboratory (ESRL),
 * Global Systems Division (GSD), Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.spatialdisplay;

import gov.noaa.nws.ncep.ui.pgen.elements.AbstractDrawableComponent;
import gov.noaa.nws.ncep.ui.pgen.elements.Layer;
import gov.noaa.nws.ncep.ui.pgen.elements.Product;
import gov.noaa.nws.ncep.ui.pgen.elements.ProductInfo;
import gov.noaa.nws.ncep.ui.pgen.elements.ProductTime;

import java.util.ArrayList;
import java.util.List;

/**
 * Description: TODO
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Xiangbao Jing      Initial induction into repo
 * </pre>
 * 
 * @author Xiangbao Jing
 */
public class ToolLayerDataManager {

    private final List<Product> productList;

    /**
     * Current active product in the PGEN drawing layer.
     */
    private Product activeProduct = null;

    /**
     * Current active layer in the PGEN drawing layer' active product.
     */
    private Layer activeLayer = null;

    public ToolLayerDataManager() {
        productList = new ArrayList<Product>();
        initializeProducts();
    }

    @Override
    public boolean equals(Object obj) {
        // TODO Auto-generated method stub
        return obj == this;
    }

    /**
     * @return the productList
     */
    public List<Product> getProductList() {
        return productList;
    }

    /**
     * @return the activeProduct
     */
    public Product getActiveProduct() {
        return activeProduct;
    }

    /**
     * @param activeProduct
     *            the activeProduct to set
     */
    public void setActiveProduct(Product activeProduct) {
        this.activeProduct = activeProduct;
    }

    /**
     * @return the activeLayer
     */
    public Layer getActiveLayer() {
        return activeLayer;
    }

    /**
     * @param activeLayer
     *            the activeLayer to set
     */
    public void setActiveLayer(Layer activeLayer) {
        this.activeLayer = activeLayer;
    }

    /**
     * Initialize product list for the resource.
     */
    public void initializeProducts() {

        /*
         * Create an active product with an active layer and add to the product
         * List .
         */
        if (productList.size() == 0) {

            activeProduct = new Product("Default", "Default", "Default",
                    new ProductInfo(), new ProductTime(),
                    new ArrayList<Layer>());

            activeLayer = new Layer();
            // activeLayer = new ContourLine();
            activeProduct.addLayer(activeLayer);

            productList.add(activeProduct);

        }

    }

    /**
     * Uses a PgenCommand to add a DrawableElement to the productList.
     * 
     * @param de
     *            The DrawableElement being added.
     */
    public void addElement(AbstractDrawableComponent de) {

        activeLayer.addElement(de);

    }

    public void removeElement(AbstractDrawableComponent de) {
        activeLayer.removeElement(de);
    }

    /**
     * @param autoSaveFilename
     *            the autoSaveFilename to set
     */
    public void setAutoSaveFilename(String autoSaveFilename) {
    }

    /**
     * @param autosave
     *            the autosave to set
     */
    public void setAutosave(boolean autosave) {
    }

    /**
     * Uses a PgenCommand to replace one drawable element in the product list
     * with another drawable element.
     * 
     * @param old
     *            Element to replace
     * @param newde
     *            New drawable element
     */
    public void replaceElement(AbstractDrawableComponent old,
            AbstractDrawableComponent newde) {

        for (Product currProd : productList) {

            for (Layer currLayer : currProd.getLayers()) {

                if (currLayer.replace(old, newde)) {
                    // layer = currLayer;
                    return;
                }
            }

        }

    }

}

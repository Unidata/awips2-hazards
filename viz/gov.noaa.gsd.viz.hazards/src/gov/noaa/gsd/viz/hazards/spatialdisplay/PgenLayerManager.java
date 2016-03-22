/**
 * This software was developed and / or modified by the National Oceanic and
 * Atmospheric Administration (NOAA), Earth System Research Laboratory (ESRL),
 * Global Systems Division (GSD), Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.spatialdisplay;

import gov.noaa.nws.ncep.ui.pgen.elements.AbstractDrawableComponent;
import gov.noaa.nws.ncep.ui.pgen.elements.DECollection;
import gov.noaa.nws.ncep.ui.pgen.elements.Layer;
import gov.noaa.nws.ncep.ui.pgen.elements.Product;
import gov.noaa.nws.ncep.ui.pgen.elements.ProductInfo;
import gov.noaa.nws.ncep.ui.pgen.elements.ProductTime;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Description: PGEN layer manager for the spatial display.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer      Description
 * ------------ ---------- ------------- --------------------------
 * Apr 04, 2013            Xiangbao Jing Initial induction into repo.
 * Jul 18, 2013   1264     Chris.Golden  Added support for drawing lines and
 *                                       points.
 * Mar 10, 2016  15676     Chris.Golden  Removed unused methods and members,
 *                                       and renamed.
 * Mar 22, 2016  15676     Chris.Golden  Made the layer fillable, so that
 *                                       drawable elements may be filled with
 *                                       a solid pattern of arbitrary RGBA
 *                                       value.
 * </pre>
 * 
 * @author Xiangbao Jing
 */
public class PgenLayerManager {

    // Private Variables

    /**
     * Current active product in the PGEN drawing layer.
     */
    private final Product activeProduct;

    /**
     * Current active layer in the PGEN drawing layer' active product.
     */
    private final Layer activeLayer;

    // Public Constructors

    /**
     * Construct a standard instance.
     */
    public PgenLayerManager() {
        activeProduct = new Product("Default", "Default", "Default",
                new ProductInfo(), new ProductTime(), new ArrayList<Layer>());
        activeLayer = new Layer();
        activeLayer.setFilled(true);
        activeProduct.addLayer(activeLayer);
    }

    // Public Methods

    /**
     * @return the activeLayer
     */
    public Layer getActiveLayer() {
        return activeLayer;
    }

    /**
     * Add the specified drawable component to the product.
     * 
     * @param element
     *            Drawable element to be added.
     */
    public void addElement(AbstractDrawableComponent element) {
        if (element instanceof DECollection) {
            Iterator<AbstractDrawableComponent> iter = ((DECollection) element)
                    .getComponentIterator();
            while (iter.hasNext()) {
                addElement(iter.next());
            }
        } else {
            activeLayer.addElement(element);
        }
    }

    /**
     * Remove the specified drawable component from the product.
     * 
     * @param element
     *            Drawable element to be removed.
     */
    public void removeElement(AbstractDrawableComponent element) {
        if (element instanceof DECollection) {
            Iterator<AbstractDrawableComponent> iter = ((DECollection) element)
                    .getComponentIterator();
            while (iter.hasNext()) {
                removeElement(iter.next());
            }
        } else {
            activeLayer.removeElement(element);
        }
    }
}

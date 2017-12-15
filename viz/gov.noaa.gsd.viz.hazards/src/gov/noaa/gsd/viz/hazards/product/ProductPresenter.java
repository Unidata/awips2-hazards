/**
 * This software was developed and / or modified by Raytheon Company,
 * pursuant to Contract DG133W-05-CQ-1067 with the US Government.
 * 
 * U.S. EXPORT CONTROLLED TECHNICAL DATA
 * This software product contains export-restricted data whose
 * export/transfer/disclosure is restricted by U.S. law. Dissemination
 * to non-U.S. persons whether in the United States or abroad requires
 * an export license or other authorization.
 * 
 * Contractor Name:        Raytheon Company
 * Contractor Address:     6825 Pine Street, Suite 340
 *                         Mail Stop B8
 *                         Omaha, NE 68106
 *                         402.291.0100
 * 
 * See the AWIPS II Master Rights File ("Master Rights File.pdf") for
 * further licensing information.
 **/
package gov.noaa.gsd.viz.hazards.product;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.viz.hazards.sessionmanager.ISessionManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.ObservedSettings;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Tool;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.ToolType;

import gov.noaa.gsd.common.eventbus.BoundedReceptionEventBus;
import gov.noaa.gsd.viz.hazards.display.HazardServicesPresenter;

/**
 * Product Presenter.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Apr 29, 2016   16373    mpduff       Initial creation.
 * Dec 17, 2017   20739    Chris.Golden Refactored away access to directly
 *                                      mutable session events.
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class ProductPresenter
        extends HazardServicesPresenter<IProductView<?, ?>> {

    public ProductPresenter(ISessionManager<ObservedSettings> model,
            BoundedReceptionEventBus<Object> eventBus) {
        super(model, eventBus);
    }

    @Override
    public void modelChanged(EnumSet<HazardConstants.Element> changed) {

        /*
         * No action.
         */
    }

    @Override
    protected void initialize(IProductView<?, ?> view) {
        List<Tool> listOfProductTools = getProductTools();
        view.initialize(this, listOfProductTools);
    }

    @Override
    protected void reinitialize(IProductView<?, ?> view) {

        /*
         * No action.
         */
    }

    private List<Tool> getProductTools() {
        List<Tool> products = new ArrayList<>();

        /*
         * Define products here. These don't need to be configurable by the
         * users.
         */
        Tool rvsTool = new Tool();
        rvsTool.setDisplayName("Generate RVS");
        rvsTool.setToolName("RVS_ProductGenerator");
        rvsTool.setToolType(ToolType.NON_HAZARD_PRODUCT_GENERATOR);
        rvsTool.setVisible(true);
        products.add(rvsTool);

        Tool corrTool = new Tool();
        corrTool.setDisplayName("Product Correction");
        corrTool.setToolName("Product_Correction");
        corrTool.setToolType(ToolType.PRODUCT_CORRECTOR);
        corrTool.setVisible(true);
        products.add(corrTool);

        Tool viewTool = new Tool();
        viewTool.setDisplayName("View Product");
        viewTool.setToolName("Product_VIEW");
        viewTool.setToolType(ToolType.PRODUCT_VIEWER);
        viewTool.setVisible(true);
        products.add(viewTool);

        return products;
    }
}

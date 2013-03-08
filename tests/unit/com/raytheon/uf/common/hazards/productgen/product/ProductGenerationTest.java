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
package com.raytheon.uf.common.hazards.productgen.product;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardState;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.ProductClass;
import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.HazardEventManager;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.collections.HazardEventSet;
import com.raytheon.uf.common.hazards.productgen.GeneratedProduct;
import com.raytheon.uf.common.hazards.productgen.IGeneratedProduct;
import com.raytheon.uf.common.hazards.productgen.ProductGeneration;
import com.raytheon.uf.common.localization.IPathManager;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationLevel;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationType;
import com.raytheon.uf.common.localization.PathManagerFactory;
import com.raytheon.uf.common.localization.PathManagerFactoryTest;
import com.raytheon.uf.common.python.concurrent.IPythonJobListener;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

/**
 * 
 * This class allows product generators to be tested with the Product Generation
 * Framework. The product generator to be tested needs only to be copied to
 * /utility/common_static/base/python/productgen/products of this project, such
 * as ExampleFFW.py. The input parameters can be adjusted as necessary.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 28, 2013            jsanchez     Initial creation
 * 
 * </pre>
 * 
 * @author jsanchez
 * @version 1.0
 */
public class ProductGenerationTest {

    /** This file is located in the productgen/products directory **/
    private String product = "ExampleFFW";

    private HazardEventSet hazardEventSet;

    private String[] formats = new String[] { "XML", "ASCII" };

    @Before
    public void setUp() {

        PathManagerFactoryTest.initLocalization();
        fillFiles();
        HazardEventManager manager = new HazardEventManager(
                HazardEventManager.Mode.PRACTICE);
        IHazardEvent event = manager.createEvent();
        event.setEndTime(new Date());
        event.setStartTime(new Date());
        event.setIssueTime(new Date());
        event.setSiteID("OAX");
        event.setPhenomenon("FF");
        event.setSignificance("W");
        event.setHazardMode(ProductClass.EXPERIMENTAL);
        event.setState(HazardState.PENDING);

        GeometryFactory factory = new GeometryFactory();
        Geometry geom = factory.createPoint(new Coordinate(22, 2));
        event.setGeometry(geom);

        hazardEventSet = new HazardEventSet();
        hazardEventSet.add(event);
    }

    /*
     * Method to help bring in correct localization files.
     */
    private static void fillFiles() {
        IPathManager manager = PathManagerFactory.getPathManager();
        manager.listFiles(manager.getContext(LocalizationType.COMMON_STATIC,
                LocalizationLevel.BASE), "python", null, true, false);
    }

    @Test
    public void generateTest() {
        IPythonJobListener<List<IGeneratedProduct>> listener = new IPythonJobListener<List<IGeneratedProduct>>() {

            @Override
            public void jobFinished(List<IGeneratedProduct> result) {
                for (IGeneratedProduct generatedProduct : result) {
                    System.out.println(generatedProduct.getEntries());
                }
            }

            @Override
            public void jobFailed(Throwable e) {
                GeneratedProduct generatedProduct = new GeneratedProduct(null);
                generatedProduct.setErrors(e.getLocalizedMessage());
                // TODO Pass result to the SessionManager via EventBus or
                // use ProductGeneration's listener
                System.out.println(e.getLocalizedMessage());
            }
        };

        ProductGeneration productGeneration = new ProductGeneration();
        productGeneration.generate(product, hazardEventSet, formats, listener);
        try {
            // A sleep on the current thread is needed due to that the product
            // generation is running asynchronously. There is a System.out in
            // the jobFinished method that will print the results.
            Thread.currentThread().sleep(500);
        } catch (InterruptedException e) {
            System.out.println(e);
        }
    }

    @Test
    public void getDialogTest() {
        ProductGeneration productGeneration = new ProductGeneration();
        Map<String, String> dialogInfo = productGeneration
                .getDialogInfo(product);
        System.out.println(dialogInfo);
    }
}

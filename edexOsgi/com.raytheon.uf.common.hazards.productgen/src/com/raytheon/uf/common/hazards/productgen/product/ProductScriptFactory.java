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

import jep.JepException;

import com.raytheon.uf.common.python.concurrent.AbstractPythonScriptFactory;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;

/**
 * Factory to create a ProductScript object.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 18, 2013            jsanchez     Initial creation
 * 
 * </pre>
 * 
 * @author jsanchez
 * @version 1.0
 */

public class ProductScriptFactory extends
        AbstractPythonScriptFactory<ProductScript> {

    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(ProductScriptFactory.class);

    /** Reference name to get instance in PythonJobCoordinator */
    public static final String NAME = "productScriptFactory";

    /**
     * Default Constructor.
     */
    public ProductScriptFactory() {
        this(NAME, 1);
    }

    /**
     * @param name
     *            reference name to get instance
     * @param maxThreads
     *            max number of threads
     */
    private ProductScriptFactory(String name, int maxThreads) {
        super(name, maxThreads);
    }

    @Override
    public ProductScript createPythonScript() {
        try {
            return new ProductScript();
        } catch (JepException e) {
            statusHandler.handle(Priority.ERROR,
                    "Unable to create product script", e);
        }
        return null;
    }
}

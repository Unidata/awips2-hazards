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

import gov.noaa.gsd.viz.mvp.IView;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Tool;

/**
 * Interface for the product view.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 29, 2016   16373    mpduff      Initial creation
 * 
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public interface IProductView<C, E extends Enum<E>> extends IView<C, E> {
    /**
     * Initialize the view.
     * 
     * @param presenter
     *            Presenter managing this view.
     * @param products
     *            - a List of Tool objects
     */
    public void initialize(ProductPresenter presenter, List<Tool> products);

    /**
     * Show a product subview that is used to gather parameter values for a tool
     * that is to be executed.
     * 
     * @param eventType
     *            The type of the event that this tool is to create; if present,
     *            the tool is being run as a result of a hazard-type-first
     *            invocation. Otherwise, it will be <code>null</code>.
     * @param tool
     *            The tool for which parameters are to be gathered.
     * @param dialogInput
     *            Map of dialog input key/value pairs
     */
    public void showToolParameterGatherer(Tool tool, String eventType,
            Map<String, Serializable> dialogInput,
            Map<String, Serializable> initialInput);

    /**
     * Set the products to those specified.
     * 
     * @param products
     *            List of tool objects.
     */
    public void setTools(List<Tool> products);

}

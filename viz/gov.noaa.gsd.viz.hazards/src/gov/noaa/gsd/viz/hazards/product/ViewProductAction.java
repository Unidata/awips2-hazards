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

import gov.noaa.gsd.viz.hazards.console.ConsolePresenter;
import gov.noaa.gsd.viz.hazards.display.ProductViewerSelectionDlg;

import java.util.Date;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Shell;

import com.raytheon.uf.common.hazards.productgen.data.ProductData;
import com.raytheon.uf.common.hazards.productgen.data.ProductDataUtil;
import com.raytheon.uf.common.time.SimulatedTime;
import com.raytheon.uf.viz.core.VizApp;
import com.raytheon.viz.core.mode.CAVEMode;
import com.raytheon.viz.ui.VizWorkbenchManager;

/**
 * Handles displaying the ProductViewerSelectionDlg.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 29, 2015    9681    Robert.Blum Initial creation
 * 
 * </pre>
 * 
 * @author Robert.Blum
 * @version 1.0
 */

public class ViewProductAction extends Action {

    public static final String PRODUCT_DATA_PARAM = "productData";

    private static ConsolePresenter presenter;

    public ViewProductAction(ConsolePresenter presenter) {
        super("View Product", Action.AS_PUSH_BUTTON);
        ViewProductAction.presenter = presenter;
    }

    public ViewProductAction(String text) {
        super(text, Action.AS_PUSH_BUTTON);
    }

    @Override
    public void run() {
        String mode = CAVEMode.getMode().toString();
        Date simulatedTime = SimulatedTime.getSystemTime().getTime();
        List<ProductData> viewableProductData = ProductDataUtil
                .retrieveViewableProductData(mode, simulatedTime);
        Shell shell = VizWorkbenchManager.getInstance().getCurrentWindow()
                .getShell();
        final ProductViewerSelectionDlg selectionDialog = new ProductViewerSelectionDlg(
                shell, presenter, viewableProductData);
        VizApp.runSync(new Runnable() {
            @Override
            public void run() {
                selectionDialog.open();
            }
        });
    }
}

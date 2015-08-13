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

package gov.noaa.gsd.viz.hazards.display;

import gov.noaa.gsd.viz.hazards.console.ConsolePresenter;
import gov.noaa.gsd.viz.hazards.display.action.HazardDetailAction;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.PlatformUI;

import com.raytheon.uf.common.hazards.productgen.data.ProductData;
import com.raytheon.viz.ui.dialogs.CaveSWTDialog;

/**
 * 
 * Dialog that allows the user to select which issued products they want to
 * display in the Product Viewer.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 29, 2015 9681       Robert.Blum Initial creation
 * Aug 13, 2015 8836       Chris.Cody   Changes for a configurable Event Id
 * 
 * </pre>
 * 
 * @author Robert.Blum
 * @version 1.0
 */
public class ProductViewerSelectionDlg extends CaveSWTDialog {

    private final String DIALOG_TITILE = "Select Product to View";

    private final String VIEW_BUTTON_TEXT = "View product";

    private final String CLOSE_BUTTON_TEXT = "Close";

    private final String PRODUCT_DATA_PARAM = "productData";

    private final SimpleDateFormat dateFormat = new SimpleDateFormat(
            "HH:mm'Z' dd-MMM-yy");

    private final ConsolePresenter presenter;

    /** List of all productData in the table */
    private final List<ProductData> productData;

    /** Map of TableItems to corresponding productData */
    private final Map<TableItem, ProductData> tableItemMap = new HashMap<TableItem, ProductData>();

    /** Table that holds the productData information */
    private Table table;

    /**
     * Creates a new ProductViewerSelectionDlg on the given shell with the
     * provided productData list.
     * 
     * @param parentShell
     * @param presenter
     * @param productData
     */
    public ProductViewerSelectionDlg(Shell parentShell,
            ConsolePresenter presenter, List<ProductData> productData) {
        super(parentShell, SWT.DIALOG_TRIM, CAVE.NONE);
        this.presenter = presenter;
        this.productData = productData;

        setText(DIALOG_TITILE);
    }

    @Override
    protected void initializeComponents(Shell shell) {
        Composite mainComposite = new Composite(shell, SWT.NONE);
        mainComposite.setLayout(new GridLayout(1, false));
        createTable(mainComposite);
        createButtonRow(mainComposite);
    }

    private void createTable(Composite mainComposite) {
        Composite tableComp = new Composite(mainComposite, SWT.NONE);
        tableComp.setLayout(new GridLayout(1, true));
        tableComp
                .setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));

        // Add the table
        table = new Table(tableComp, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        table.setLayout(new GridLayout(1, true));
        table.setLayoutData(new GridData(SWT.CENTER, SWT.DEFAULT, true, false));
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        String[] columnTitles = { "Product Category", "Event IDs", "Issue Time" };

        // Create the table columns
        for (int x = 0; x < columnTitles.length; x++) {
            TableColumn column = new TableColumn(table, SWT.NULL);
            column.setText(columnTitles[x]);
        }

        // Add the table contents
        for (ProductData tempData : productData) {
            TableItem item = new TableItem(table, SWT.NULL);

            // Set the product category column data
            String productID = tempData.getProductGeneratorName().replace(
                    "_ProductGenerator", "");
            item.setText(0, productID);

            // Set the event ids column data
            StringBuilder sb = new StringBuilder();
            String prefix = "";
            for (String eventID : tempData.getEventIDs()) {
                sb.append(prefix);
                sb.append(eventID);
                prefix = ",";
            }
            item.setText(1, sb.toString());

            // Set the issue time column data
            item.setText(2, dateFormat.format(tempData.getIssueTime()));

            // Add the tableItem to the map
            tableItemMap.put(item, tempData);
        }

        for (int x = 0; x < columnTitles.length; x++) {
            table.getColumn(x).pack();
        }
    }

    private void createButtonRow(Composite mainComposite) {
        Composite buttonRowComp = new Composite(mainComposite, SWT.NONE);
        buttonRowComp.setLayout(new GridLayout(2, true));
        buttonRowComp.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true,
                false));

        // Add the view button.
        GridData gd = new GridData(SWT.CENTER, SWT.DEFAULT, true, false);
        gd.widthHint = 150;
        Button viewBtn = new Button(buttonRowComp, SWT.PUSH);
        viewBtn.setText(VIEW_BUTTON_TEXT);
        viewBtn.setLayoutData(gd);
        viewBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                List<ProductData> selectedProductData = new ArrayList<ProductData>(
                        productData.size());
                final TableItem[] items = table.getSelection();
                for (int x = 0; x < items.length; x++) {
                    selectedProductData.add(tableItemMap.get(items[x]));
                }
                // Table only allows for single selection.
                if (items.length == 1) {
                    HazardDetailAction action = new HazardDetailAction(
                            HazardDetailAction.ActionType.VIEW);
                    Map<String, Serializable> parameters = new HashMap<String, Serializable>();
                    parameters.put(PRODUCT_DATA_PARAM,
                            (Serializable) selectedProductData);
                    action.setParameters(parameters);
                    presenter.publish(action);
                    close();
                } else {
                    // Display pop up stating no products selected to view
                    Shell shell = PlatformUI.getWorkbench()
                            .getActiveWorkbenchWindow().getShell();
                    MessageBox msgBox = new MessageBox(shell, SWT.ICON_ERROR);
                    msgBox.setText("Product Selection Error");
                    msgBox.setMessage("No products were selected.");
                    msgBox.open();
                }
            }
        });

        // Add the Cancel button.
        gd = new GridData(SWT.CENTER, SWT.DEFAULT, true, false);
        gd.widthHint = 150;
        Button closeBtn = new Button(buttonRowComp, SWT.PUSH);
        closeBtn.setText(CLOSE_BUTTON_TEXT);
        closeBtn.setLayoutData(gd);
        closeBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                close();
            }
        });
    }

    @Override
    protected void disposed() {
        table.dispose();
    }
}

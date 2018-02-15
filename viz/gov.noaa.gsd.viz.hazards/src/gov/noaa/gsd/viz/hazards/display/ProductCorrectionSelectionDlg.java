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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;

import com.raytheon.uf.common.dataplugin.events.hazards.event.AbstractHazardServicesEventIdUtil;
import com.raytheon.uf.common.dataplugin.events.locks.LockInfo.LockStatus;
import com.raytheon.uf.common.hazards.productgen.data.ProductData;
import com.raytheon.viz.core.mode.CAVEMode;

import gov.noaa.gsd.viz.hazards.display.action.ProductAction;

/**
 * 
 * Dialog that allows the user to select which issued products they want to
 * correct in the Product Editor.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 27, 2017 22308      Robert.Blum Initial creation
 * Mar 13, 2017 28708      mduff       Changes to support event id refactor.
 * 
 * </pre>
 * 
 * @author Robert.Blum
 * @version 1.0
 */
public class ProductCorrectionSelectionDlg extends AbstractProductSelectionDlg {

    private static final String PRODUCT_CATEGORY = "Product Category";

    private static final String ISSUE_TIME = "Issue Time";

    private static final String EVENT_IDS = "Event IDs";

    private static final String HAZARD_TYPE = "Hazard Type";

    private static final String VTEC = "VTEC";

    private static final String EXPIRATION_TIME = "Expiration Time";

    private static final String USER_NAME = "User Name";

    private final String DIALOG_TITLE_CORRECT = "Select Product to Correct";

    private final String CORRECT_BUTTON_TEXT = " Correct Product ";

    private final String PRODUCT_DATA_PARAM = "productData";

    private final List<ProductCorrectionRowData> rowDataList = new ArrayList<>();

    /**
     * Row sort comparator.
     */
    private final SortComparator sortComparator = new SortComparator();

    /**
     * Creates a new ProductViewerSelectionDlg on the given shell with the
     * provided productData list.
     * 
     * @param parentShell
     * @param presenter
     * @param productData
     * @param viewData
     */
    public ProductCorrectionSelectionDlg(Shell parentShell,
            HazardServicesPresenter<?> presenter,
            List<ProductData> productData) {
        super(parentShell, presenter);
        setText(DIALOG_TITLE_CORRECT);

        String mode = CAVEMode.getMode().toString();
        // Create the row data objects
        outer: for (ProductData tempData : productData) {
            if (mode.equalsIgnoreCase(tempData.getMode())) {
                List<String> eventIdList = tempData.getEventIDs();
                for (String eventId : eventIdList) {
                    LockStatus status = presenter.getSessionManager()
                            .getLockManager().getHazardEventLockStatus(eventId);
                    // Skip it if it is locked my someone else
                    if (status == LockStatus.LOCKED_BY_OTHER) {
                        continue outer;
                    }
                }

                ProductCorrectionRowData rd = new ProductCorrectionRowData();

                // Set the product category column data
                String productID = tempData.getProductGeneratorName()
                        .replace("_ProductGenerator", "");
                rd.setProductCategory(productID);

                // Set the issue time column data
                rd.setIssueDate(tempData.getIssueTime());

                // Sort the event IDs
                Collections.sort(eventIdList);

                // Set the event IDs column data
                StringBuilder sb = new StringBuilder();
                String prefix = "";
                for (String eventID : eventIdList) {
                    sb.append(prefix);
                    sb.append(eventID);
                    prefix = ",";
                }

                rd.setEventIds(sb.toString());
                rd.setProductData(tempData);
                rowDataList.add(rd);
            }
        }
    }

    /**
     * Create the buttons.
     * 
     * @param mainComposite
     *            The dialogs main composite
     */
    @Override
    protected void createButtonRow(Composite mainComposite) {
        Composite buttonRowComp = new Composite(mainComposite, SWT.NONE);
        buttonRowComp.setLayout(new GridLayout(2, true));
        buttonRowComp.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, true, false));

        GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        // Correction
        Button viewBtn = new Button(buttonRowComp, SWT.PUSH);
        viewBtn.setText(CORRECT_BUTTON_TEXT);
        viewBtn.setLayoutData(gd);
        viewBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                handleCorrectionAction();
            }
        });

        // Add the Close button.
        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
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

    /**
     * Search action handler.
     * 
     * @param searchText
     *            The text to search for
     */
    @Override
    protected void handleSearch(String searchText) {
        int idx = -1;
        String text = searchText.toUpperCase();
        boolean foundMatch = false;
        for (ProductCorrectionRowData rd : rowDataList) {
            idx++;
            if (rd.getEventIds().contains(text)) {
                foundMatch = true;
                break;
            } else if (dateFormat.format(rd.getIssueDate()).toUpperCase()
                    .contains(text)) {
                foundMatch = true;
                break;
            } else if (rd.getProductCategory().toUpperCase().contains(text)) {
                foundMatch = true;
                break;
            }
        }

        if (foundMatch) {
            table.select(idx);
        }
    }

    /**
     * Update the table to view the new data.
     */
    @Override
    protected void updateTable() {
        // Clear the tree
        table.removeAll();

        // Add the tree contents
        for (ProductCorrectionRowData rd : rowDataList) {
            TableItem item = new TableItem(table, SWT.NONE);
            item.setText(0, rd.getProductCategory());
            item.setText(1, dateFormat.format(rd.getIssueDate()) + " ");
            item.setText(2, rd.getEventIds());
            item.setText(3, rd.getHazardType());
            item.setText(4, rd.getVtecStr());
            Long expireTime = rd.getExpirationTime();
            if (expireTime != null) {
                Date expDate = new Date(expireTime);
                item.setText(5, dateFormat.format(expDate));
            }

            item.setText(6, rd.getUserName());
            item.setData(rd);
        }
    }

    @Override
    protected void addTableListeners() {
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDoubleClick(MouseEvent e) {
                handleCorrectionAction();
            }
        });
    }

    @Override
    protected ColumnTitle[] getColumnTitles() {
        return (new ColumnTitle[] { new ColumnTitle("Product Category", null),
                new ColumnTitle("Issue Time", "00:00Z 00-MMM-00"),
                new ColumnTitle("Event IDs",
                        "[" + AbstractHazardServicesEventIdUtil
                                .getInvalidFullId() + "]      "),
                new ColumnTitle("Hazard Type", "FA.W.            "),
                new ColumnTitle("VTEC", "[MMM, MMM]"),
                new ColumnTitle("Expiration Time", "00:00Z 00-MMM-00"),
                new ColumnTitle("User Name", null) });
    }

    @Override
    protected String getSortColumnTitle() {
        return ISSUE_TIME;
    }

    private void handleCorrectionAction() {
        if (table.getSelectionCount() == 0) {
            // Display pop up stating no products selected to view
            Shell shell = this.getShell();
            MessageBox msgBox = new MessageBox(shell, SWT.ICON_ERROR);
            msgBox.setText("Product Selection Error");
            msgBox.setMessage("No products were selected.");
            msgBox.open();
            return;
        }

        TableItem ti = table.getSelection()[0];
        ProductCorrectionRowData rowData = (ProductCorrectionRowData) ti
                .getData();
        List<ProductData> dataList = new ArrayList<>(1);
        ProductData selectedProductData = rowData.getProductData();
        dataList.add(selectedProductData);
        ProductAction action = new ProductAction(
                ProductAction.ActionType.REVIEW);
        Map<String, Serializable> parameters = new HashMap<>();
        parameters.put(PRODUCT_DATA_PARAM, (Serializable) dataList);
        action.setParameters(parameters);
        presenter.publish(action);
    }

    @Override
    protected void sortColumnData(String title) {
        sortComparator.setSortText(title);
        Collections.sort(this.rowDataList, sortComparator);
        if (reverseSort) {
            Collections.reverse(this.rowDataList);
        }
        updateTable();
    }

    /**
     * Comparator for sorting the rows in the tree.
     */
    private class SortComparator
            implements Comparator<ProductCorrectionRowData> {

        private String sortText;

        @Override
        public int compare(ProductCorrectionRowData o1,
                ProductCorrectionRowData o2) {
            int value = 0;
            if (ISSUE_TIME.equals(sortText)) {
                value = o1.getIssueDate().compareTo(o2.getIssueDate());
            } else if (PRODUCT_CATEGORY.equals(sortText)) {
                value = o1.getProductCategory()
                        .compareTo(o2.getProductCategory());
            } else if (EVENT_IDS.equals(sortText)) {
                value = o1.getEventIds().compareTo(o2.getEventIds());
            } else if (HAZARD_TYPE.equals(sortText)) {
                String o1Type = o1.getHazardType();
                String o2Type = o2.getHazardType();
                value = o1Type.compareTo(o2Type);
            } else if (VTEC.equals(sortText)) {
                String o1Vtec = o1.getVtecStr();
                String o2Vtec = o2.getVtecStr();
                value = o1Vtec.compareTo(o2Vtec);
            } else if (EXPIRATION_TIME.equals(sortText)) {
                long o1ExpTime = o1.getExpirationTime();
                long o2ExpTime = o2.getExpirationTime();
                value = Long.valueOf(o1ExpTime)
                        .compareTo(Long.valueOf(o2ExpTime));
            } else if (USER_NAME.equals(sortText)) {
                String o1Name = o1.getUserName();
                String o2Name = o2.getUserName();
                value = o1Name.compareTo(o2Name);
            } else {
                value = 0;
            }

            return value;
        }

        public void setSortText(String sortText) {
            this.sortText = sortText;
        }
    }

    @Override
    protected void createProductFilterComponents(Composite mainComposite) {
        // do nothing, required by abstract class
    }
}

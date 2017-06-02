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

import gov.noaa.gsd.common.utilities.Utils;
import gov.noaa.gsd.viz.hazards.display.action.ProductAction;

import java.io.Serializable;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.hazards.productgen.data.ProductData;
import com.raytheon.viz.core.mode.CAVEMode;
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
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Jul 29, 2015 9681       Robert.Blum  Initial creation
 * Aug 13, 2015 8836       Chris.Cody   Changes for a configurable Event Id
 * Mar 16, 2016 16538      mduff        Fix issue with size of dialog, added scrolling, searching, and sorting.
 * Mar 28, 2016 16538      mduff        Fixed issues with display and search
 * Apr 07, 2016 16538      Robert.Blum  Fixed search to select correct table row.
 * May 02, 2016 16373      mduff        Changed to display options for both view and correct.
 * May 16, 2016 18202      mduff        Fixes for operational/practice mode.
 * Jun 06, 2016 19443      mduff        Fix the double click action on the rows.
 * Jul 11, 2016 18257      kbisanz      Set dialog title based on view or correct
 * Aug 09, 2016 17067      Robert.Blum  Changes for RVS Products.
 * Aug 17, 2016 20594/663  sstewart     Changed default sort order, fixed expiration time
 *                                      sort order, and keep dialog open upon selecting 
 *                                      a product to view.
 * Feb 01, 2017 15556      Chris.Golden Minor changes to support console refactor.
 * Jun 30, 2017 19223      Chris.Golden Changed to use new HazardConstants constant.
 * </pre>
 * 
 * @author Robert.Blum
 * @version 1.0
 */
public class ProductViewerSelectionDlg extends CaveSWTDialog {

    private enum ColumnTitle {
        PRODUCT_CATEGORY("Product Category", null), ISSUE_TIME("Issue Time",
                "00:00Z 00-MMM-00"), EVENT_IDS("Event IDs",
                "MM-0000-MMM-000000"), HAZARD_IDS("Hazard Type",
                "FA.W.MMMMMMMMMMMM"), VTEC("VTEC", "[MMM, MMM]"), EXP_TIME(
                "Expiration Time", "00:00Z 00-MMM-00"), USER("User Name", null);

        private final String title;

        private final String widthPattern;

        ColumnTitle(String title, String widthPattern) {
            this.title = title;
            this.widthPattern = widthPattern == null ? title : widthPattern;
        }

        public String getText() {
            return title;
        }

        public String getWidthPattern() {
            return widthPattern;
        }
    }

    private final String DIALOG_TITLE_VIEW = "Select Product to View";

    private final String DIALOG_TITLE_CORRECT = "Select Product to Correct";

    private final String VIEW_BUTTON_TEXT = " View Product ";

    private final String CORRECT_BUTTON_TEXT = " Correct Product ";

    private final String CLOSE_BUTTON_TEXT = "Close";

    private final DateFormat dateFormat = Utils
            .getGmtDateTimeFormatterWithMinutesResolution();

    private final HazardServicesPresenter<?> presenter;

    /** List of all productData in the tree */
    private final List<ProductData> productData;

    private final List<ProductViewerRowData> rowDataList = new ArrayList<>();

    /** Tree that holds the productData information. */
    private Tree tree;

    /**
     * Row sort comparator.
     */
    private final SortComparator sortComparator = new SortComparator();

    /**
     * Reverse sort flag.
     */
    private boolean reverseSort = false;

    private boolean viewData = true;

    /**
     * Creates a new ProductViewerSelectionDlg on the given shell with the
     * provided productData list.
     * 
     * @param parentShell
     * @param presenter
     * @param productData
     * @param viewData
     */
    public ProductViewerSelectionDlg(Shell parentShell,
            HazardServicesPresenter<?> presenter,
            List<ProductData> productData, boolean viewData) {
        super(parentShell, SWT.DIALOG_TRIM | SWT.RESIZE, CAVE.NONE);
        this.presenter = presenter;
        this.productData = productData;
        this.viewData = viewData;

        if (this.viewData) {
            setText(DIALOG_TITLE_VIEW);
        } else {
            setText(DIALOG_TITLE_CORRECT);
        }
    }

    /**
     * Constructor for viewing products.
     * 
     * @param parentShell
     * @param presenter
     * @param productData
     */
    public ProductViewerSelectionDlg(Shell parentShell,
            HazardServicesPresenter<?> presenter, List<ProductData> productData) {
        this(parentShell, presenter, productData, true);
    }

    @Override
    protected void initializeComponents(Shell shell) {
        Composite mainComposite = new Composite(shell, SWT.NONE);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        GridLayout gl = new GridLayout(1, false);
        mainComposite.setLayout(gl);
        mainComposite.setLayoutData(gd);
        createTable(mainComposite);
        createFindRow(mainComposite);
        createButtonRow(mainComposite);
    }

    @Override
    protected void preOpened() {
        super.preOpened();
        shell.setMinimumSize(shell.getSize());
    }

    /**
     * Create the tree.
     * 
     * @param mainComposite
     *            The dialogs main composite
     */
    private void createTable(Composite mainComposite) {
        tree = new Tree(mainComposite, SWT.VIRTUAL);
        tree.setLayout(new GridLayout(1, true));

        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        // 25 rows tall
        gd.heightHint = tree.getItemHeight() * 25;
        tree.setLayoutData(gd);
        tree.setHeaderVisible(true);
        tree.setLinesVisible(true);
        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDoubleClick(MouseEvent e) {
                if (viewData) {
                    handleViewAction();
                } else {
                    handleCorrectionAction();
                }
            }
        });

        // Create the tree columns
        GC gc = new GC(tree.getDisplay());
        TreeColumn issueColumn = null;
        for (final ColumnTitle ct : ColumnTitle.values()) {
            TreeColumn column = new TreeColumn(tree, SWT.LEFT);
            column.setText(ct.getText());
            column.setMoveable(true);

            /*
             * Make width based on Column Title patterns.
             */
            column.setWidth(gc.textExtent(ct.getWidthPattern()).x);
            column.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent event) {
                    handleColumnSelection(event);
                }
            });

            if (ct == ColumnTitle.ISSUE_TIME) {
                issueColumn = column;
            }
        }
        gc.dispose();

        String mode = CAVEMode.getMode().toString();

        // Create the row data objects
        for (ProductData tempData : productData) {
            if (mode.equalsIgnoreCase(tempData.getMode())) {
                ProductViewerRowData rd = new ProductViewerRowData();

                // Set the product category column data
                String productID = tempData.getProductGeneratorName().replace(
                        "_ProductGenerator", "");
                rd.setProductCategory(productID);

                // Set the issue time column data
                rd.setIssueDate(tempData.getIssueTime());

                // Sort the event IDs
                List<String> eventIdList = tempData.getEventIDs();
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

                this.rowDataList.add(rd);
            }
        }

        // Sort by issue time by default
        tree.setSortColumn(issueColumn);
        tree.setSortDirection(SWT.UP);
        reverseSort = true;
        sortColumnData(ColumnTitle.ISSUE_TIME.getText());
    }

    /**
     * Create the buttons.
     * 
     * @param mainComposite
     *            The dialogs main composite
     */
    private void createButtonRow(Composite mainComposite) {
        Composite buttonRowComp = new Composite(mainComposite, SWT.NONE);
        buttonRowComp.setLayout(new GridLayout(2, true));
        buttonRowComp.setLayoutData(new GridData(SWT.CENTER, SWT.DEFAULT, true,
                false));

        GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        if (viewData) {
            // Add the view button.
            Button viewBtn = new Button(buttonRowComp, SWT.PUSH);
            viewBtn.setText(VIEW_BUTTON_TEXT);
            viewBtn.setLayoutData(gd);
            viewBtn.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent event) {
                    handleViewAction();
                }
            });
        } else {
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
        }

        // Add the Cancel button.
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
     * Create the text components.
     * 
     * @param mainComposite
     *            The dialogs main composite
     */
    private void createFindRow(Composite mainComposite) {
        GridData gd = new GridData(SWT.FILL, SWT.CENTER, false, false);
        GridLayout gl = new GridLayout(2, false);
        Composite findComp = new Composite(mainComposite, SWT.NONE);
        findComp.setLayout(gl);
        findComp.setLayoutData(gd);

        Label searchLbl = new Label(findComp, SWT.NONE);
        searchLbl.setText("Search: ");

        gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
        final Text searchTxt = new Text(findComp, SWT.BORDER);
        searchTxt.setLayoutData(gd);
        searchTxt.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                handleSearch(searchTxt.getText());
            }
        });
    }

    /**
     * Search action handler.
     * 
     * @param searchText
     *            The text to search for
     */
    private void handleSearch(String searchText) {
        int idx = -1;
        String text = searchText.toUpperCase();
        boolean foundMatch = false;
        for (ProductViewerRowData rd : rowDataList) {
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
            tree.select(tree.getItem(idx));
        }
    }

    @Override
    protected void disposed() {
        tree.dispose();
    }

    /**
     * Update the tree to view the new data.
     */
    private void updateTree() {
        // Clear the tree
        tree.removeAll();

        // Add the tree contents
        for (ProductViewerRowData rd : rowDataList) {
            TreeItem item = new TreeItem(tree, SWT.NONE);
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

    /**
     * Display the selected product.
     */
    private void handleViewAction() {
        if (tree.getSelectionCount() == 0) {
            // Display pop up stating no products selected to view
            Shell shell = getShell();
            MessageBox msgBox = new MessageBox(shell, SWT.ICON_ERROR);
            msgBox.setText("Product Selection Error");
            msgBox.setMessage("No products were selected.");
            msgBox.open();
            return;
        }

        TreeItem ti = tree.getSelection()[0];
        ProductViewerRowData rowData = (ProductViewerRowData) ti.getData();
        List<ProductData> dataList = new ArrayList<>(1);
        ProductData selectedProductData = rowData.getProductData();
        dataList.add(selectedProductData);
        ProductAction action = new ProductAction(ProductAction.ActionType.VIEW);
        Map<String, Serializable> parameters = new HashMap<>();
        parameters.put(HazardConstants.PRODUCT_DATA_PARAM,
                (Serializable) dataList);
        action.setParameters(parameters);
        presenter.publish(action);
    }

    private void handleCorrectionAction() {
        if (tree.getSelectionCount() == 0) {
            // Display pop up stating no products selected to view
            Shell shell = this.getShell();
            MessageBox msgBox = new MessageBox(shell, SWT.ICON_ERROR);
            msgBox.setText("Product Selection Error");
            msgBox.setMessage("No products were selected.");
            msgBox.open();
            return;
        }

        TreeItem ti = tree.getSelection()[0];
        ProductViewerRowData rowData = (ProductViewerRowData) ti.getData();
        List<ProductData> dataList = new ArrayList<>(1);
        ProductData selectedProductData = rowData.getProductData();
        dataList.add(selectedProductData);
        ProductAction action = new ProductAction(
                ProductAction.ActionType.REVIEW);
        Map<String, Serializable> parameters = new HashMap<>();
        parameters.put(HazardConstants.PRODUCT_DATA_PARAM,
                (Serializable) dataList);
        action.setParameters(parameters);
        presenter.publish(action);
    }

    private void handleColumnSelection(SelectionEvent e) {
        TreeColumn column = (TreeColumn) e.widget;
        TreeColumn sortColumn = tree.getSortColumn();

        if (column.equals(sortColumn) && (tree.getSortDirection() == SWT.UP)) {
            tree.setSortDirection(SWT.DOWN);
            reverseSort = true;
        } else {
            tree.setSortDirection(SWT.UP);
            reverseSort = false;
        }
        tree.setSortColumn(column);

        sortColumnData(column.getText());
    }

    private void sortColumnData(String title) {
        sortComparator.setSortText(title);
        Collections.sort(this.rowDataList, sortComparator);
        if (reverseSort) {
            Collections.reverse(this.rowDataList);
        }
        updateTree();
    }

    /**
     * Comparator for sorting the rows in the tree.
     */
    private class SortComparator implements Comparator<ProductViewerRowData> {

        private String sortText;

        @Override
        public int compare(ProductViewerRowData o1, ProductViewerRowData o2) {
            int value = 0;
            if (ColumnTitle.ISSUE_TIME.getText().equals(sortText)) {
                value = o1.getIssueDate().compareTo(o2.getIssueDate());
            } else if (ColumnTitle.PRODUCT_CATEGORY.getText().equals(sortText)) {
                value = o1.getProductCategory().compareTo(
                        o2.getProductCategory());
            } else if (ColumnTitle.EVENT_IDS.getText().equals(sortText)) {
                value = o1.getEventIds().compareTo(o2.getEventIds());
            } else if (ColumnTitle.HAZARD_IDS.getText().equals(sortText)) {
                String o1Type = o1.getHazardType();
                String o2Type = o2.getHazardType();
                value = o1Type.compareTo(o2Type);
            } else if (ColumnTitle.VTEC.getText().equals(sortText)) {
                String o1Vtec = o1.getVtecStr();
                String o2Vtec = o2.getVtecStr();
                // Strip the square brackets so they do not impact the sort.
                o1Vtec = o1Vtec.substring(1, o1Vtec.length() - 1);
                o2Vtec = o2Vtec.substring(1, o2Vtec.length() - 1);
                value = o1Vtec.compareTo(o2Vtec);
            } else if (ColumnTitle.EXP_TIME.getText().equals(sortText)) {
                long o1ExpTime = o1.getExpirationTime();
                long o2ExpTime = o2.getExpirationTime();
                value = Long.valueOf(o1ExpTime).compareTo(
                        Long.valueOf(o2ExpTime));
            } else if (ColumnTitle.USER.getText().equals(sortText)) {
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
}

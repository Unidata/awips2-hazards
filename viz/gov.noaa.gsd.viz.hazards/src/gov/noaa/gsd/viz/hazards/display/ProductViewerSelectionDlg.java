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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;

import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.event.AbstractHazardServicesEventIdUtil;
import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardEventUtilities;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IReadableHazardEvent;
import com.raytheon.uf.common.dataplugin.text.db.StdTextProduct;
import com.raytheon.uf.common.site.SiteMap;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.viz.core.VizApp;
import com.raytheon.uf.viz.productgen.validation.util.VtecObject;
import com.raytheon.viz.ui.dialogs.SWTMessageBox;

import gov.noaa.gsd.viz.hazards.product.TextProductQueryJob;
import gov.noaa.gsd.viz.hazards.utilities.ProductParserUtil;

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
 * Dec 21, 2016 21504      Robert.Blum  Cant correct hazards that are locked.
 * Jan 27, 2017 22308      Robert.Blum  Refactored to only be used for viewing products
 *                                      from the text database.
 * Jan 31, 2017 22308      Robert.Blum  Fixing build errors.
 * Feb 01, 2017 15556      Chris.Golden Minor changes to support console refactor.
 * Mar 13, 2017 28708      mduff        Changes to support event id refactor.
 * Mar 15, 2017 30219      bkowal       Notify the user when a query does not return
 *                                      any viewable products.
 * Jun 30, 2017 19223      Chris.Golden Changed to use new HazardConstants constant.
 * </pre>
 * 
 * @author Robert.Blum
 */
public class ProductViewerSelectionDlg extends AbstractProductSelectionDlg {

    private final String EVENT_IDS = "Event IDs";

    private final String ISSUE_TIME = "Issue Time";

    private final String PIL = "PIL";

    private final String VTEC = "VTEC";

    private final String USER_NAME = "User Name";

    private final String SITE = "Site";

    private final String DIALOG_TITLE_VIEW = "Select Product to View";

    private final String VIEW_BUTTON_TEXT = " View Product ";

    private final List<ProductViewerRowData> rowDataList = new ArrayList<>();

    private final Set<String> sites;

    private final Collection<? extends IReadableHazardEvent> events;

    private boolean populateOnCreation;

    /**
     * The progress bar to display when products are being loaded.
     */
    private ProgressBar progressBar;

    private ProductViewerRowDataComparator sortComparator = new ProductViewerRowDataComparator();

    /**
     * Creates a new ProductViewerSelectionDlg on the given shell with the
     * provided text products.
     * 
     * @param parentShell
     * @param presenter
     * @param textProducts
     */
    public ProductViewerSelectionDlg(Shell parentShell,
            HazardServicesPresenter<?> presenter,
            Collection<? extends IReadableHazardEvent> events,
            boolean populateOnCreation) {
        super(parentShell, presenter);
        setText(DIALOG_TITLE_VIEW);
        sites = presenter.getSessionManager().getConfigurationManager()
                .getStartUpConfig().getPossibleSites();
        this.events = events;
        this.populateOnCreation = populateOnCreation;
    }

    @Override
    protected void preOpened() {
        super.preOpened();
        /*
         * Viewing products for specific events, pre-populate the table.
         */
        if (populateOnCreation) {
            /*
             * Pull the sites from the events passed in. PILs could also be
             * pulled from the events but since they change over the life cycle
             * of the products it was left out.
             */
            Set<String> eventSites = new HashSet<String>();
            for (IReadableHazardEvent event : events) {
                List<? extends IReadableHazardEvent> hList = presenter
                        .getSessionManager().getEventManager()
                        .getEventHistoryById(event.getEventID());
                for (IReadableHazardEvent e : hList) {
                    eventSites.add(e.getSiteID());
                }
            }
            queryTextProducts(eventSites, HazardConstants.HYDRO_PILS);
        }
    }

    private void queryTextProducts(Set<String> sites, Set<String> pils) {
        /*
         * Display the progress bar, ensure this is done on the UI thread.
         */
        VizApp.runAsync(new Runnable() {
            @Override
            public void run() {
                if (progressBar != null && progressBar.isDisposed() == false) {
                    progressBar.setVisible(true);
                }
            }

        });

        /*
         * Create the job that will handle the query.
         */
        final TextProductQueryJob job = new TextProductQueryJob(sites, pils);
        job.addJobChangeListener(new JobChangeAdapter() {
            @Override
            public void done(IJobChangeEvent event) {
                /*
                 * Update the table then remove the progress bar.
                 */
                VizApp.runAsync(new Runnable() {
                    @Override
                    public void run() {
                        createRowDataObjects(events, job.getTextProducts());
                        if (progressBar != null
                                && progressBar.isDisposed() == false) {
                            progressBar.setVisible(false);
                        }
                    }
                });
            }
        });
        job.schedule();
    }

    private void createRowDataObjects(
            Collection<? extends IReadableHazardEvent> events,
            List<StdTextProduct> textProducts) {
        rowDataList.clear();
        if (events == null || events.isEmpty()) {
            SWTMessageBox mb = new SWTMessageBox(shell, "Product Viewer",
                    "No Hazard Events were found for the specified criteria.",
                    SWT.ICON_INFORMATION | SWT.OK);
            mb.open();
            return;
        }
        if (textProducts != null && textProducts.isEmpty() == false) {
            for (StdTextProduct product : textProducts) {
                ProductViewerRowData rowData = new ProductViewerRowData();
                rowData.setProduct(product);
                getRowDataEventsAndVtec(events, product, rowData);
                List<? extends IReadableHazardEvent> productEvents = rowData
                        .getEvents();
                if (productEvents != null && productEvents.isEmpty() == false) {
                    rowDataList.add(rowData);
                }
            }
        }

        if (rowDataList.isEmpty()) {
            SWTMessageBox mb = new SWTMessageBox(shell, "Product Viewer",
                    "No Text Products were found for the specified criteria.",
                    SWT.ICON_INFORMATION | SWT.OK);
            mb.open();
            return;
        }

        // Sort the rowDataList then update the table
        sortColumnData(getSortColumnTitle());
    }

    private void getRowDataEventsAndVtec(
            Collection<? extends IReadableHazardEvent> events,
            StdTextProduct product, ProductViewerRowData rowData) {
        // Parse the VTEC Record out of the Product
        List<IReadableHazardEvent> productEvents = new ArrayList<>();
        List<VtecObject> vtecs = ProductParserUtil
                .getVTECsFromProduct(product.getProduct());
        rowData.setVtecs(vtecs);

        // Parse the issueTime out of the product.
        Date productIssueTime = ProductParserUtil
                .getIssueTimeFromProduct(product.getProduct());
        if (productIssueTime == null) {
            return;
        }
        for (VtecObject vtec : vtecs) {
            for (IReadableHazardEvent event : events) {
                if (HazardConstants.HazardStatus
                        .hasEverBeenIssued(event.getStatus())) {
                    IReadableHazardEvent matchingEvent = vtecMatchEvent(vtec,
                            event, productIssueTime);
                    if (matchingEvent != null
                            && productEvents.contains(matchingEvent) == false) {
                        productEvents.add(matchingEvent);
                    }
                }
            }
        }
        rowData.setEvents(productEvents);
    }

    private IReadableHazardEvent vtecMatchEvent(VtecObject vtec,
            IReadableHazardEvent event, Date issueTime) {
        /*
         * If the Phen, Sig, ETN, and Site match then it is safe to assume that
         * the product was for this event.
         */
        List<Integer> eventEtns = HazardEventUtilities.parseEtnsToIntegers(
                String.valueOf(event.getHazardAttribute(HazardConstants.ETNS)));
        if (vtec.getPhenomena()
                .equalsIgnoreCase(event.getPhenomenon()) == false) {
            return null;
        } else if (vtec.getSignificance()
                .equalsIgnoreCase(event.getSignificance()) == false) {
            return null;
        } else if (vtec.getOffice().equalsIgnoreCase(SiteMap.getInstance()
                .getSite4LetterId(event.getSiteID())) == false) {
            return null;
        } else if (eventEtns.contains(vtec.getSequence()) == false) {
            return null;
        }

        /*
         * Loop through the hazard history to match up issueTimes to get the
         * correct version of the event.
         * 
         * TODO: This is not guaranteed to work since hazards can be saved
         * without issuing. But I have no better solutions at this point to
         * correctly match products to the correct event in the History List.
         */
        IReadableHazardEvent matchingEvent = null;
        for (IReadableHazardEvent historyHazard : presenter.getSessionManager()
                .getEventManager().getEventHistoryById(event.getEventID())) {
            Long t1 = (Long) historyHazard
                    .getHazardAttribute(HazardConstants.ISSUE_TIME);
            if (t1 == null) {
                // No issue Time, skip it
                continue;
            }

            Date eventIssuetime = new Date(t1);
            Calendar cal = TimeUtil.newGmtCalendar(eventIssuetime);
            TimeUtil.minCalendarFields(cal, Calendar.SECOND,
                    Calendar.MILLISECOND);
            eventIssuetime = cal.getTime();
            if (eventIssuetime.equals(issueTime)) {
                matchingEvent = historyHazard;
                break;
            }
        }

        return matchingEvent;
    }

    @Override
    protected void createProductFilterComponents(Composite mainComposite) {
        /*
         * Only add the site/PIL filter options if the table is not be being
         * populated when created. This means "View Product" was selected from
         * the toolbar and not from the right click menu of a specific event.
         */
        if (!populateOnCreation) {
            Group filterGroup = new Group(mainComposite, SWT.None);
            filterGroup.setLayout(new GridLayout(2, true));
            GridData gd = new GridData(GridData.FILL_BOTH);
            gd.heightHint = 175;
            filterGroup.setLayoutData(gd);
            filterGroup.setText("Filter/Query Products:");

            Label siteLabel = new Label(filterGroup, SWT.None);
            siteLabel.setText("Sites:");
            Label pilLabel = new Label(filterGroup, SWT.None);
            pilLabel.setText("PILs:");

            final org.eclipse.swt.widgets.List siteList = new org.eclipse.swt.widgets.List(
                    filterGroup, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
            siteList.setLayoutData(new GridData(GridData.FILL_BOTH));
            siteList.setItems(sites.toArray(new String[sites.size()]));
            siteList.select(0);

            final org.eclipse.swt.widgets.List pilList = new org.eclipse.swt.widgets.List(
                    filterGroup, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
            pilList.setLayoutData(new GridData(GridData.FILL_BOTH));
            pilList.setItems(HazardConstants.HYDRO_PILS
                    .toArray(new String[HazardConstants.HYDRO_PILS.size()]));
            pilList.select(0);

            Button applyBtn = new Button(filterGroup, SWT.PUSH);
            GridData gd2 = new GridData(SWT.CENTER, SWT.DEFAULT, true, false);
            gd2.horizontalSpan = 2;
            applyBtn.setLayoutData(gd2);
            applyBtn.setText("Apply");
            applyBtn.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    apply(siteList.getSelection(), pilList.getSelection());
                }
            });
        }
    }

    private void apply(String[] sites, String[] pils) {
        Set<String> siteSet = new HashSet<>(Arrays.asList(sites));
        Set<String> pilSet = new HashSet<>(Arrays.asList(pils));
        if (siteSet.isEmpty() || pilSet.isEmpty()) {
            MessageBox msgBox = new MessageBox(this.getParent(), SWT.None);
            msgBox.setText("Filter/Query Error");
            msgBox.setMessage("Must have at least one site/PIL selected.");
            msgBox.open();
            return;
        }
        queryTextProducts(siteSet, pilSet);
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
        buttonRowComp.setLayout(new GridLayout(3, true));
        buttonRowComp.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, true, false));

        // Dummy Label
        @SuppressWarnings("unused")
        Label dummyLbl = new Label(buttonRowComp, SWT.NONE);

        // Create a 2nd composite to hold the buttons in the center column
        Composite centerBtnComp = new Composite(buttonRowComp, SWT.NONE);
        centerBtnComp.setLayout(new GridLayout(2, true));
        centerBtnComp.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, true, false));

        // Add the view button.
        Button viewBtn = new Button(centerBtnComp, SWT.PUSH);
        viewBtn.setText(VIEW_BUTTON_TEXT);
        GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        viewBtn.setLayoutData(gd);
        viewBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                handleViewAction();
            }
        });

        // Add the Cancel button.
        Button closeBtn = new Button(centerBtnComp, SWT.PUSH);
        closeBtn.setText(CLOSE_BUTTON_TEXT);
        GridData gd2 = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        gd2.widthHint = gd.minimumWidth;
        closeBtn.setLayoutData(gd2);
        closeBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                close();
            }
        });

        /*
         * Create progress bar
         */
        progressBar = new ProgressBar(buttonRowComp, SWT.INDETERMINATE);
        progressBar.setLayoutData(
                new GridData(SWT.RIGHT, SWT.CENTER, true, false));
        progressBar.setVisible(false);
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
        for (ProductViewerRowData rd : rowDataList) {
            idx++;
            if (rd.getEventIds().toUpperCase().contains(text)) {
                foundMatch = true;
                break;
            } else if (dateFormat
                    .format(rd.getProduct().getInsertTime().getTime())
                    .toUpperCase().contains(text)) {
                foundMatch = true;
                break;
            } else if (rd.getProduct().getNnnid().toUpperCase()
                    .contains(text)) {
                foundMatch = true;
                break;
            } else if (rd.getActions().toUpperCase().contains(text)) {
                foundMatch = true;
                break;
            } else if (rd.getUserName().toUpperCase().contains(text)) {
                foundMatch = true;
                break;
            } else if (rd.getSite().toUpperCase().contains(text)) {
                foundMatch = true;
                break;
            }
        }

        if (foundMatch) {
            table.select(idx);
        }
    }

    @Override
    protected void sortColumnData(String title) {
        sortComparator.setSortText(title);
        Collections.sort(rowDataList, sortComparator);
        if (reverseSort) {
            Collections.reverse(rowDataList);
        }
        updateTable();
    }

    /**
     * Update the table to view the new data.
     */
    @Override
    protected void updateTable() {
        // Clear the tree
        if (table != null && table.isDisposed() == false) {
            table.removeAll();

            // Add the tree contents
            for (ProductViewerRowData rowData : rowDataList) {
                StdTextProduct textProduct = rowData.getProduct();
                TableItem item = new TableItem(table, SWT.NONE);
                item.setText(0, rowData.getEventIds());
                item.setText(1,
                        dateFormat.format(new Date(rowData.getIssueTime())));
                item.setText(2, textProduct.getNnnid());
                item.setText(3, rowData.getActions());
                item.setText(4, rowData.getUserName());
                item.setText(5, rowData.getSite());
                item.setData(textProduct);
            }
        }

    }

    /**
     * Display the selected product.
     */
    private void handleViewAction() {
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
        StdTextProduct product = (StdTextProduct) ti.getData();
        TextEditorDlg editor = new TextEditorDlg(this.getParent(), false,
                product.getProduct(), "Product Viewer - " + ti.getText(0));
        editor.open();
    }

    @Override
    protected void addTableListeners() {
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDoubleClick(MouseEvent e) {
                handleViewAction();
            }
        });
    }

    @Override
    protected ColumnTitle[] getColumnTitles() {
        return (new ColumnTitle[] {
                new ColumnTitle(EVENT_IDS,
                        "[" + AbstractHazardServicesEventIdUtil
                                .getInvalidFullId() + "]      "),
                new ColumnTitle(ISSUE_TIME, "03:45Z DD-MMM-YY  "),
                new ColumnTitle(PIL, "[FFW]  "),
                new ColumnTitle(VTEC, "[NEW]        "),
                new ColumnTitle(USER_NAME, "John Doe       "),
                new ColumnTitle(SITE, "OAX     ") });
    }

    @Override
    protected String getSortColumnTitle() {
        return ISSUE_TIME;
    }

    /**
     * Comparator for sorting the rows in the tree.
     */
    private class ProductViewerRowDataComparator
            implements Comparator<ProductViewerRowData> {

        private String sortText;

        @Override
        public int compare(ProductViewerRowData o1, ProductViewerRowData o2) {
            int value = 0;
            StdTextProduct product1 = o1.getProduct();
            StdTextProduct product2 = o2.getProduct();

            if (EVENT_IDS.equals(sortText)) {
                value = o1.getEventIds().compareTo(o2.getEventIds());
            } else if (ISSUE_TIME.equals(sortText)) {
                value = o1.getIssueTime().compareTo(o2.getIssueTime());
            } else if (PIL.equals(sortText)) {
                value = product1.getNnnid().compareTo(product2.getNnnid());
            } else if (SITE.equals(sortText)) {
                value = product1.getSite().compareTo(product2.getSite());
            } else if (VTEC.equals(sortText)) {
                value = o1.getActions().compareTo(o2.getActions());
            } else if (USER_NAME.equals(sortText)) {
                value = o1.getUserName().compareTo(o2.getUserName());
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

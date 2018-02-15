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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

import com.raytheon.viz.ui.dialogs.CaveSWTDialog;

/**
 * Abstract dialog for selecting products.
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 27, 2017 22308      Robert.Blum Initial creation
 *
 * </pre>
 *
 * @author Robert.Blum
 */

public abstract class AbstractProductSelectionDlg extends CaveSWTDialog {

    protected final static DateFormat dateFormat = new SimpleDateFormat(
            "HH:mm'Z' dd-MMM-yy");

    static {
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    protected static final String CLOSE_BUTTON_TEXT = "Close";

    protected final HazardServicesPresenter<?> presenter;

    /**
     * Reverse sort flag.
     */
    protected boolean reverseSort = false;

    /** Table that holds the productData information. */
    protected Table table;

    public AbstractProductSelectionDlg(Shell parentShell,
            HazardServicesPresenter<?> presenter) {
        super(parentShell, SWT.DIALOG_TRIM | SWT.RESIZE, CAVE.NONE);
        this.presenter = presenter;
    }

    @Override
    protected void preOpened() {
        super.preOpened();
        shell.setMinimumSize(shell.getSize());
    }

    @Override
    protected void initializeComponents(Shell shell) {
        Composite mainComposite = new Composite(shell, SWT.NONE);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.widthHint = 800;
        GridLayout gl = new GridLayout(1, false);
        mainComposite.setLayout(gl);
        mainComposite.setLayoutData(gd);
        createProductFilterComponents(mainComposite);
        createTable(mainComposite);
        createFindRow(mainComposite);
        createButtonRow(mainComposite);
    }

    /**
     * Create the table.
     * 
     * @param mainComposite
     *            The dialogs main composite
     */
    private void createTable(Composite mainComposite) {
        table = new Table(mainComposite, SWT.VIRTUAL | SWT.BORDER);
        table.setLayout(new GridLayout(1, true));

        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        // 20 rows tall
        gd.heightHint = table.getItemHeight() * 20;
        table.setLayoutData(gd);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        // Create the tree columns
        GC gc = new GC(table.getDisplay());
        TableColumn sortColumn = null;
        for (final ColumnTitle ct : getColumnTitles()) {
            TableColumn column = new TableColumn(table, SWT.LEFT);
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

            if (ct.getText().equals(getSortColumnTitle())) {
                sortColumn = column;
            }
        }
        gc.dispose();

        addTableListeners();

        // Sort by issue time by default
        table.setSortColumn(sortColumn);
        table.setSortDirection(SWT.UP);
        reverseSort = true;
        sortColumnData(getSortColumnTitle());
    }

    @Override
    protected void disposed() {
        table.dispose();
    }

    protected abstract String getSortColumnTitle();

    protected abstract ColumnTitle[] getColumnTitles();

    protected abstract void addTableListeners();

    protected abstract void updateTable();

    protected abstract void createButtonRow(Composite mainComposite);

    protected abstract void createProductFilterComponents(
            Composite mainComposite);

    protected abstract void handleSearch(String searchtext);

    protected abstract void sortColumnData(String title);

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
                handleSearch(searchTxt.getText().trim());
            }
        });
    }

    protected void handleColumnSelection(SelectionEvent e) {
        TableColumn column = (TableColumn) e.widget;
        TableColumn sortColumn = table.getSortColumn();

        if (column.equals(sortColumn) && (table.getSortDirection() == SWT.UP)) {
            table.setSortDirection(SWT.DOWN);
            reverseSort = true;
        } else {
            table.setSortDirection(SWT.UP);
            reverseSort = false;
        }
        table.setSortColumn(column);

        sortColumnData(column.getText());
    }

    public class ColumnTitle {
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
}

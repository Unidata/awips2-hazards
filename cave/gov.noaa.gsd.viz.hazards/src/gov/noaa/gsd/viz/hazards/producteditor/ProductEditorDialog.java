/**
 * This software was developed and / or modified by the National Oceanic and
 * Atmospheric Administration (NOAA), Earth System Research Laboratory (ESRL),
 * Global Systems Division (GSD), Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.producteditor;

import gov.noaa.gsd.viz.hazards.dialogs.BasicDialog;
import gov.noaa.gsd.viz.hazards.jsonutilities.Dict;
import gov.noaa.gsd.viz.hazards.productstaging.ProductConstants;
import gov.noaa.gsd.viz.mvp.widgets.ICommandInvocationHandler;
import gov.noaa.gsd.viz.mvp.widgets.ICommandInvoker;

import java.util.Iterator;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.viz.ui.dialogs.ModeListener;

/**
 * Description: The product editor dialog. This dialog allows the forecaster to
 * preview (and possibly edit) an ASCII text or XML product before it is issued.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 6/22/2011               X. Jing     Initial creation
 * 08/28/2012              X. Jing     Changed to multiple products display.
 * 02/19/2013              B. Lawrence Converted MVP architecture, added javadoc
 *                                     removed lots of unused code.
 * 02/25/2013              B. Lawrence Set up 70 character width limit and made this dialog modal.
 * 03/08/2013              B. Lawrence Changed to SWT.APPLICATION_MODAL and non-blocking.
 * 04/23/2013              B. Lawrence Made fixes based on code review responses.
 * 06/04/2013              C. Golden   Added support for changing background and foreground
 *                                     colors in order to stay in synch with CAVE mode.
 * </pre>
 * 
 * @author Bryon.Lawrence
 */
class ProductEditorDialog extends BasicDialog {

    /**
     * For logging...
     */
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(ProductEditorDialog.class);

    /**
     * The maximum characters per line in the product editor dialog.
     */
    private final int MAX_CHARACTERS_PER_LINE = 70;

    /**
     * Factor to adjust the width of 70 characters displayed in this dialog.
     */
    private final double WIDTH_ADJUSTMENT_FACTOR = 1.3;

    /**
     * The height of this dialog. Note that the width of this dialog is
     * dynamically determined by average font width and the maximum number of
     * characters per line.
     */
    private final int DIALOG_HEIGHT = 600;

    /** The 'Issue' button */
    private final int ISSUE_ID = 2;

    /** The 'Dismiss' button */
    private final int DISMISS_ID = 3;

    /** The body of the dialog */
    private Composite[] body = null;

    /** The title to associate with the editor. */
    private final String dialogTitle = "Product Editor";

    /**
     * Flag indicating whether or not to show the Issue, Propose and Dismiss
     * buttons.
     */
    private CTabFolder tabFolder = null;

    /**
     * List of generated products, some of which may be ASCII text, some which
     * may be XML and some which may be both.
     */
    private List<Dict> generatedProductsDictList = null;

    private List<Dict> hazardEventSetsList = null;

    private int dialogWidth;

    /**
     * Continue command invocation handler.
     */
    private ICommandInvocationHandler issueHandler = null;

    /**
     * Continue command invoker.
     */
    private final ICommandInvoker issueInvoker = new ICommandInvoker() {
        @Override
        public void setCommandInvocationHandler(
                ICommandInvocationHandler handler) {
            issueHandler = handler;
        }
    };

    /**
     * Continue command invocation handler.
     */
    private ICommandInvocationHandler dismissHandler = null;

    /**
     * Continue command invoker.
     */
    private final ICommandInvoker dismissInvoker = new ICommandInvoker() {
        @Override
        public void setCommandInvocationHandler(
                ICommandInvocationHandler handler) {
            dismissHandler = handler;
        }
    };

    /**
     * Continue command invocation handler.
     */
    private ICommandInvocationHandler shellClosedHandler = null;

    /**
     * Continue command invoker.
     */
    private final ICommandInvoker shellClosedInvoker = new ICommandInvoker() {
        @Override
        public void setCommandInvocationHandler(
                ICommandInvocationHandler handler) {
            shellClosedHandler = handler;
        }
    };

    /**
     * 
     * 
     * Build an instance of the Product Editor Dialog
     * 
     * @param parentShell
     *            The parent of this dialog.
     * @param generatedProductsDictList
     * @param hazardEventSetsList
     *            List of hazard event sets
     * 
     */
    public ProductEditorDialog(Shell parentShell,
            List<Dict> generatedProductsDictList, List<Dict> hazardEventSetsList) {
        super(parentShell);

        this.hazardEventSetsList = hazardEventSetsList;

        this.generatedProductsDictList = generatedProductsDictList;
        this.setShellStyle(SWT.CLOSE | SWT.LEFT_TO_RIGHT | SWT.CURSOR_SIZEALL
                | SWT.APPLICATION_MODAL);

        setBlockOnOpen(false);

        if (this.getGeneratedProductsDictList() == null
                || this.getGeneratedProductsDictList().size() < 1) {
            return;
        }

    }

    @Override
    protected Control createDialogArea(Composite parent) {

        Composite top = (Composite) super.createDialogArea(parent);
        top.setLayout(new FillLayout());

        // Build tabs
        tabFolder = new CTabFolder(top, SWT.BORDER);
        tabFolder.setBorderVisible(true);
        new ModeListener(tabFolder);

        body = new Composite[getGeneratedProductsDictList().size()];

        int scrollBarWidth = 1;

        for (int j = 0; j < getGeneratedProductsDictList().size(); ++j) {
            ScrolledComposite scrollComposite = new ScrolledComposite(
                    tabFolder, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);

            scrollBarWidth = scrollComposite.getVerticalBar().getSize().x;

            body[j] = new Composite(scrollComposite, SWT.NONE);
            body[j].setLayout(new GridLayout(1, true));

            Dict generatedProduct = getGeneratedProductsDictList().get(j);
            String productName = generatedProduct
                    .getDynamicallyTypedValue("productID");
            CTabItem tabItem = new CTabItem(tabFolder, SWT.NONE);

            tabItem.setText(productName);

            /*
             * A generated product may be ASCII, XML, or both. Favor XML over
             * ASCII when both are available.
             */
            if (generatedProduct.containsKey(ProductConstants.XML_PRODUCT_KEY)) {
                String xmlText = generatedProduct
                        .getDynamicallyTypedValue(ProductConstants.XML_PRODUCT_KEY);
                processXMLProduct(body[j], xmlText);
            } else {
                String asciiText = generatedProduct
                        .getDynamicallyTypedValue(ProductConstants.ASCII_PRODUCT_KEY);
                processASCIIProduct(body[j], asciiText);
            }

            scrollComposite.setContent(body[j]);
            scrollComposite.setExpandVertical(true);
            scrollComposite.setExpandHorizontal(true);
            scrollComposite.setShowFocusedControl(true);
            scrollComposite.setMinHeight(body[j].computeSize(SWT.DEFAULT,
                    SWT.DEFAULT).y + 100);

            tabItem.setControl(scrollComposite);
        }

        applyDialogFont(top);

        /*
         * Determine the width of the dialog based on MAX_CHARACTERS_PER_LINE
         */
        GC gc = new GC(top);
        gc.setFont(top.getFont());
        dialogWidth = gc.getFontMetrics().getAverageCharWidth()
                * MAX_CHARACTERS_PER_LINE;
        dialogWidth += scrollBarWidth;
        dialogWidth *= WIDTH_ADJUSTMENT_FACTOR;
        gc.dispose();

        tabFolder.pack();

        getShell().addDisposeListener(new DisposeListener() {

            @Override
            public void widgetDisposed(DisposeEvent e) {
                shellClosedHandler.commandInvoked("Shell Closed");
            }

        });

        return top;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        Button issueButton = createButton(parent, ISSUE_ID, "Issue", false);
        issueButton.setVisible(true);
        issueButton.setToolTipText("Issue the Event");

        Button dismissButton = createButton(parent, DISMISS_ID, "Dismiss",
                false);
        dismissButton.setVisible(true);
        dismissButton.setToolTipText("Dismiss this Window");
    }

    @Override
    protected void buttonPressed(int buttonId) {
        // super.buttonPressed(buttonId);
        if ((buttonId == ISSUE_ID) && (issueHandler != null)) {
            issueHandler.commandInvoked("Issue");
        } else if ((buttonId == DISMISS_ID) && (dismissHandler != null)) {
            dismissHandler.commandInvoked("Dismiss");
        } else {
            super.buttonPressed(buttonId);
        }
    }

    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText(dialogTitle);
    }

    @Override
    protected Point getInitialSize() {
        return new Point(dialogWidth, DIALOG_HEIGHT);
    }

    /**
     * Parses an XML string representing a product generated from Hazard
     * Services. This produces input for the Product Editor dialog.
     * 
     * @param body
     *            The panel in the Product Editor dialog the text will be
     *            displayed on.
     * @param xmlText
     *            The XML string to parse.
     * @return
     */
    private void processXMLProduct(Composite body, String xmlText) {
        Document doc = null;

        try {
            /*
             * Not sure which is better to use, DocumentHelper or SAXReader. I
             * think DocumentHelper is just an interface on top of SAX.
             */
            doc = DocumentHelper.parseText(xmlText);
            // doc = new SAXReader().read(new StringReader(xmlText));

            /*
             * Loops through the xml and makes the necessary text areas to
             * display
             */
            Element root = doc.getRootElement();

            for (Iterator<?> i = root.elementIterator(); i.hasNext();) {
                Element el = (Element) i.next();
                String tx = el.getText();
                String val = el.getName();

                Boolean editable = false;

                if (el.attribute("editable") != null) {
                    if (el.attribute("editable").getValue() == "true") {
                        editable = true;
                    }
                }

                if (editable) {
                    Text textBox = new Text(body, SWT.WRAP);
                    textBox.setText(tx);
                    textBox.setBounds(body.getClientArea());
                    GridData productTextGridData = new GridData(
                            GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
                    textBox.setLayoutData(productTextGridData);
                    textBox.setData(val);
                } else {
                    Label label = new Label(body, SWT.WRAP);
                    label.setText(tx);
                    label.setBounds(body.getClientArea());
                    GridData productTextGridData = new GridData(
                            GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
                    label.setLayoutData(productTextGridData);
                    label.setData(val);
                }
            }

        } catch (Exception e) {
            statusHandler.error("In ProductEditorDialog.createDialogArea(): ",
                    e);
        }
    }

    /**
     * Parses a string representing an ASCII text product generated from Hazard
     * Services. This produces input for the Product Editor dialog.
     * 
     * @param body
     *            The panel in the Product Editor dialog the text will be
     *            displayed on.
     * @param asciiText
     *            The string representing the product.
     * @return
     */
    private void processASCIIProduct(Composite body, String asciiText) {
        /*
         * Retrieve the point size of the current font.
         */
        Text textBox = new Text(body, SWT.WRAP);
        textBox.setText(asciiText);
        Rectangle boundingRect = body.getClientArea();
        textBox.setBounds(boundingRect);
        GridData productTextGridData = new GridData(GridData.FILL_HORIZONTAL
                | GridData.GRAB_HORIZONTAL);
        textBox.setLayoutData(productTextGridData);
    }

    /**
     * @return the generatedProductsDictList
     */
    public List<Dict> getGeneratedProductsDictList() {
        return generatedProductsDictList;
    }

    /**
     * @return the hazardEventSetsList
     */
    public List<Dict> getHazardEventSetsList() {
        return hazardEventSetsList;
    }

    /**
     * Get the issue command invoker.
     * 
     * @return Continue command invoker.
     */
    public ICommandInvoker getIssueInvoker() {
        return issueInvoker;
    }

    /**
     * Get the dismiss command invoker.
     * 
     * @param
     * @return The Dismiss command invoker
     */
    public ICommandInvoker getDismissInvoker() {
        return dismissInvoker;
    }

    /**
     * 
     * @return The shell closed invoker.
     */
    public ICommandInvoker getShellClosedInvoker() {
        return shellClosedInvoker;
    }

}

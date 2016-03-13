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
package com.raytheon.uf.viz.productgen.dialog;

import gov.noaa.gsd.viz.mvp.widgets.ICommandInvocationHandler;
import gov.noaa.gsd.viz.mvp.widgets.ICommandInvoker;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.raytheon.uf.common.dataplugin.events.IEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.hazards.productgen.GeneratedProductList;
import com.raytheon.uf.common.hazards.productgen.IGeneratedProduct;
import com.raytheon.uf.common.hazards.productgen.ITextProduct;
import com.raytheon.uf.common.hazards.productgen.KeyInfo;
import com.raytheon.uf.common.hazards.productgen.ProductGeneration;
import com.raytheon.uf.common.python.concurrent.IPythonJobListener;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.core.VizApp;
import com.raytheon.uf.viz.hazards.sessionmanager.product.impl.ProductValidationUtil;

/**
 * 
 * The dialog that allows the user to modify editable fields in data produced by
 * the content generator. This dialog also allows the user to view the resulting
 * formats produced by the formatter.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Jun 16, 2014 3519       jsanchez     Initial creation
 * Jun 30, 2014 3512       Chris.Golden Changed to work with changes to
 *                                      ICommandInvoker.
 * 01/15/2015   5109       bphillip     Refactored Product Editor
 * Jan 20, 2015 4476       rferrel      Implement shutdown of ProductGeneration.
 * Feb 26, 2015 6306       mduff        Pass site id to product generation.
 * Mar 06, 2015 6788       mduff        Temporary fix:  Comment out the code that 
 *                                      displays the regenerate dialog since it pops
 *                                      up every minute
 * Mar 10, 2015 6274       Robert.Blum  Changes for Product Corrections
 * Mar 23, 2015 7165       Robert.Blum  Modifications to allow for adding "*" to product tabs.
 * Apr 16, 2015 7579       Robert.Blum  Changes for amended Product Editor design.
 * Apr 30, 2015 7579       Robert.Blum  Added space between Issue and Dismiss buttons.
 * May 06, 2015 6979       Robert.Blum  Additional changes for product Corrections.
 * May 13, 2015 6899       Robert.Blum  Removed showSelectedEventsModifiedDialog().
 * May 14, 2015 7376       Robert.Blum  Added method to update the state of the issueAll button.
 * 07/08/2015   9063       Benjamin.Phillippe Fixed product name collision in dataEditorMap
 * Jul 07, 2015 7747       Robert.Blum  Moving product validation code to the product editor. It was
 *                                      found that the previous location could case the active table
 *                                      to incorrectly update when products failed validation, since
 *                                      the validation was done after the product generation.
 * Aug 03, 2015 8836       Chris.Cody   Changes for a configurable Event Id
 * </pre>
 * 
 * @author jsanchez
 * @version 1.0
 */

public class ProductEditor extends AbstractProductDialog {

    /** The status handler */
    private static final IUFStatusHandler handler = UFStatus
            .getHandler(ProductEditor.class);

    /** Product editor dialog title */
    private static final String DIALOG_TITLE = "Product Editor";

    /** Product editor dialog title title used for correctable products */
    private static final String REVIEW_TITLE = "Product Editor (Review)";

    /** The label for the Issue All button */
    private static final String ISSUE_ALL_BUTTON_LABEL = "Issue All";

    /** The label for the Dismiss button */
    private static final String DISMISS_BUTTON_LABEL = "Dismiss";

    /** The label for the Cancel and Dismiss button */
    private static final String CANCEL_AND_DISMISS_BUTTON_LABEL = "Return to Product Editor";

    /** The label for the Save and Dismiss button */
    private static final String SAVE_AND_DISMISS_BUTTON_LABEL = "Save and Dismiss";

    /** Dismiss Dialog title */
    private static final String DISMISS_DIALOG_TITLE = "Are you sure?";

    /** Message displayed by the dismiss dialog */
    private static final String DISMISS_DIALOG_MESSAGE = "Are you sure you want to 'Dismiss'?"
            + " Your product(s) will NOT be issued and your edits have not been saved.";

    /**
     * List of previously generated products. This is used for correctable
     * products
     */
    private List<GeneratedProductList> prevGeneratedProductListStorage;

    /**
     * Denotes that the products currently displayed in the Product Editor are
     * correctable
     */
    private final boolean isCorrectable;

    /** The progress bar to display that the formatting is being done currently. */
    private ProgressBar progressBar;

    /** The Issue All button */
    private Button issueAllButton;

    /** The Dismiss Button */
    private Button dismissButton;

    /** ProductGeneration instance used to regenerate products */
    private final ProductGeneration productGeneration;

    /** The total number of products that are able to be issued */
    protected int issuableProducts;

    /**
     * Creates a new ProductEditor on the given shell with the provided
     * generated product lists
     * 
     * @param parentShell
     *            The shell used to create the ProductEditor
     * @param generatedProductListStorage
     *            The generated products to be displayed on this product editor
     */
    public ProductEditor(Shell parentShell,
            List<GeneratedProductList> generatedProductListStorage,
            String siteId) {
        super(parentShell, SWT.RESIZE, CAVE.PERSPECTIVE_INDEPENDENT,
                generatedProductListStorage);
        this.productGeneration = new ProductGeneration(siteId);

        /*
         * If these products are editable, save a copy for future use
         */
        isCorrectable = generatedProductListStorage.get(0).isCorrectable();
        if (isCorrectable) {
            setPrevGeneratedProductListStorage(generatedProductListStorage);
            setText(REVIEW_TITLE);
        } else {
            setText(DIALOG_TITLE);
        }
    }

    @Override
    protected void initializeShellForSubClass(Shell shell) {
        shell.addListener(SWT.Close, shellCloseListener);
    }

    /**
     * Creates the product tabs which will contain the data editor tabs
     * 
     * @param parent
     *            The parent composite on which the product folder will get
     *            created
     * @param generatedProductListStorage
     *            The generated products used to generate the product tabs
     */
    @Override
    protected void createProductTabs(Composite parent,
            List<GeneratedProductList> generatedProductListStorage) {

        /*
         * Create the tab folder containing the product tabs
         */
        productFolder = new CTabFolder(parent, SWT.BORDER);
        ProductEditorUtil.setLayoutInfo(productFolder, 1, false, SWT.FILL,
                SWT.FILL, true, true);

        /*
         * Create progress bar
         */
        progressBar = new ProgressBar(productFolder, SWT.INDETERMINATE);
        progressBar.setVisible(false);
        productFolder.setTopRight(progressBar);

        /*
         * Iterate over each product list in the generated product list storage
         * and create a tab for each product. Then create a sub-tab for each
         * format contained in the product
         */
        for (GeneratedProductList products : generatedProductListStorage) {
            for (int folderIndex = 0; folderIndex < products.size(); folderIndex++) {

                IGeneratedProduct product = products.get(folderIndex);

                // Create a tab for the product
                CTabItem productTab = new CTabItem(productFolder, SWT.NONE);
                productTab.setText(product.getProductID());
                Composite productComposite = new Composite(productFolder,
                        SWT.NONE);
                productTab.setControl(productComposite);
                ProductEditorUtil.setLayoutInfo(productComposite, 1, true,
                        SWT.FILL, SWT.FILL, true, true);

                /*
                 * Create a tab folder to hold the data editor tabs
                 */
                CTabFolder editorAndFormatsTabFolder = new CTabFolder(
                        productComposite, SWT.BOTTOM);
                ProductEditorUtil.setLayoutInfo(editorAndFormatsTabFolder, 1,
                        false, SWT.FILL, SWT.FILL, true, true);

                /*
                 * Creates the DataEditor Manager and Data editor for this
                 * product
                 */
                editorManager.addProductDataEditor(product,
                        new ProductDataEditor(this, productTab, product,
                                editorAndFormatsTabFolder, SWT.VERTICAL));
                /*
                 * Iterate over the formatted entries in the product and create
                 * a FormattedTextDataEditor for each one.
                 */
                for (final Entry<String, List<Serializable>> entry : product
                        .getEntries().entrySet()) {
                    // Get the formatted text entries for this format
                    List<Serializable> values = entry.getValue();
                    for (int formattedTextIndex = 0; formattedTextIndex < values
                            .size(); formattedTextIndex++) {

                        /*
                         * If this is a text based product, create a
                         * FormattedTextDataEditor to hold the formatted text
                         */
                        if (product instanceof ITextProduct) {

                            // Add the text Viewer to the editor manager
                            editorManager.addFormattedTextViewer(product,
                                    new FormattedTextViewer(this, productTab,
                                            product, editorAndFormatsTabFolder,
                                            SWT.VERTICAL, entry.getKey(),
                                            formattedTextIndex));
                        } else {
                            throw new IllegalArgumentException(
                                    "Cannot create formatted text tab for format ["
                                            + entry.getKey()
                                            + "]. Unexpected product type");
                        }
                    }
                }

                // Make the First Formatted tab selected by default
                // Hazard Data Editor tab
                editorAndFormatsTabFolder.setSelection(1);
            }
        }
        productFolder.setSelection(0);
    }

    @Override
    protected void createButtons(Composite parent) {
        Composite buttonComp = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(2, false);
        layout.horizontalSpacing = HORIZONTAL_BUTTON_SPACING;
        GridData data = new GridData(SWT.CENTER, SWT.CENTER, true, false);
        buttonComp.setLayout(layout);
        buttonComp.setLayoutData(data);
        createIssueAllButton(buttonComp);
        createDismissButton(buttonComp);
    }

    /**
     * Creates the issue button on the provided composite
     * 
     * @param parent
     *            The parent composite to create the button on
     */
    private void createIssueAllButton(Composite parent) {
        issueAllButton = new Button(parent, SWT.PUSH);
        /*
         * Configure Issue All button
         */
        issueAllButton.setText(ISSUE_ALL_BUTTON_LABEL);
        ProductEditorUtil.setButtonGridData(issueAllButton);

        /*
         * Update the state of the issueAll button
         */
        updateButtons();

        /*
         * Adds the selection listener to this button
         */
        issueAllButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                issueAll();
            }
        });

        if (isCorrectable) {
            issueAllButton.setEnabled(false);
        }
    }

    /**
     * Issues all products
     */
    private void issueAll() {
        // Save all values first
        for (AbstractDataEditor de : editorManager.getAllEditors()) {
            if (de != null) {
                if (de.hasUnsavedChanges()) {
                    de.saveModifiedValues();
                    de.updateTabLabel();
                }
            }
        }

        // Validate that there is no framed text
        if (validateEditableFields()) {
            invokeIssue(isCorrectable);
        }
    }

    /**
     * Validates the editable fields to ensue all framed text has been removed.
     * 
     * @return passValidation
     */
    private boolean validateEditableFields() {
        boolean passValidation = true;
        StringBuilder sb = new StringBuilder();

        for (GeneratedProductList prodList : generatedProductListStorage) {
            for (IGeneratedProduct prod : prodList) {
                ProductDataEditor editor = this.editorManager
                        .getProductDataEditor(prod);
                EditableKeys keys = editor.editableKeys;
                List<String> productErrors = new ArrayList<String>();
                for (Entry<KeyInfo, EditableKeyInfo> entry : keys
                        .getEditableKeyEntries()) {
                    List<String> framedText = ProductValidationUtil
                            .checkForFramedText((String) entry.getValue()
                                    .getValue());
                    if (framedText.isEmpty() == false) {
                        passValidation = false;
                        productErrors.addAll(framedText);
                    }
                }
                if (productErrors.isEmpty() == false) {
                    sb.append(prod.getProductID()).append(" - ");
                    // Get the eventIDs for the label
                    String prefix = "";
                    for (IEvent event : prod.getEventSet()) {
                        IHazardEvent hazardEvent = (IHazardEvent) event;
                        sb.append(prefix);
                        sb.append(hazardEvent.getDisplayEventID());
                        prefix = "/";
                    }
                    sb.append(":\n");
                    for (String error : productErrors) {
                        sb.append(error).append("\n");
                    }
                    sb.append("\n");
                }
            }
        }
        if (passValidation == false) {
            // Display pop up of framed text
            Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                    .getShell();
            MessageBox msgBox = new MessageBox(shell, SWT.ICON_ERROR);
            msgBox.setText("Validation Error");
            sb.append("No products were issued.");
            msgBox.setMessage("Product(s) did not validate, you must modify the following regions to issue the product:\n\n"
                    + sb.toString());
            msgBox.open();
        }
        return passValidation;
    }

    /**
     * Creates the dismiss button on the given Composite
     * 
     * @param parent
     *            The parent composite to create the button on
     */
    private void createDismissButton(Composite parent) {
        dismissButton = new Button(parent, SWT.PUSH);

        // Configure the Dismiss button
        dismissButton.setText(DISMISS_BUTTON_LABEL);
        ProductEditorUtil.setButtonGridData(dismissButton);

        dismissButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                boolean hasUnsavedChanges = false;
                for (AbstractDataEditor dataEditor : editorManager
                        .getAllEditors()) {
                    if (dataEditor != null && dataEditor.hasUnsavedChanges()) {
                        String[] buttonLabels = null;

                        if (isCorrectable) {
                            buttonLabels = new String[] { DISMISS_BUTTON_LABEL,
                                    CANCEL_AND_DISMISS_BUTTON_LABEL };
                        } else {
                            buttonLabels = new String[] { DISMISS_BUTTON_LABEL,
                                    SAVE_AND_DISMISS_BUTTON_LABEL,
                                    CANCEL_AND_DISMISS_BUTTON_LABEL };
                        }

                        MessageDialog dismissDialog = new MessageDialog(null,
                                DISMISS_DIALOG_TITLE, null,
                                DISMISS_DIALOG_MESSAGE, MessageDialog.WARNING,
                                buttonLabels, 0) {
                            @Override
                            protected void buttonPressed(int buttonId) {
                                setReturnCode(buttonId);
                                close();
                            }
                        };

                        String buttonLabel = buttonLabels[dismissDialog.open()];
                        if (buttonLabel.equals(DISMISS_BUTTON_LABEL)) {
                            invokeDismiss(false);
                        } else if (buttonLabel
                                .equals(SAVE_AND_DISMISS_BUTTON_LABEL)) {
                            for (AbstractDataEditor de : editorManager
                                    .getAllEditors()) {
                                if (de != null) {
                                    if (de.hasUnsavedChanges()) {
                                        de.saveModifiedValues();
                                    }
                                }
                            }
                            invokeDismiss(false);
                        }
                        hasUnsavedChanges = true;
                        break;
                    }
                }

                if (hasUnsavedChanges == false) {
                    invokeDismiss(false);
                }

            }
        });
    }

    /**
     * Regenerates the product data for the generated products already
     * associated with this ProductEditor
     * 
     * @param keyInfo
     * 
     */
    @Override
    protected void regenerate(KeyInfo keyInfo) {

        progressBar.setVisible(true);
        for (GeneratedProductList products : generatedProductListStorage) {
            List<String> formats = new ArrayList<String>(products.get(0)
                    .getEntries().keySet());

            if (isCorrectable) {
                productGeneration.generateFrom(products.getProductInfo(),
                        products, keyInfo,
                        formats.toArray(new String[formats.size()]),
                        generateListener);
            } else {
                productGeneration.generateFrom(products.getProductInfo(),
                        products, null,
                        formats.toArray(new String[formats.size()]),
                        generateListener);
            }
        }
    }

    /**
     * Listener that handles generate events
     */
    private final IPythonJobListener<GeneratedProductList> generateListener = new IPythonJobListener<GeneratedProductList>() {

        @Override
        public void jobFinished(final GeneratedProductList productList) {
            VizApp.runAsync(new Runnable() {
                @Override
                public void run() {
                    try {
                        for (GeneratedProductList products : generatedProductListStorage) {
                            if (products.getProductInfo().equals(
                                    productList.getProductInfo())) {
                                for (int index = 0; index < productList.size(); index++) {
                                    IGeneratedProduct updatedProduct = productList
                                            .get(index);
                                    IGeneratedProduct product = products
                                            .get(index);

                                    product.setEntries(updatedProduct
                                            .getEntries());
                                    product.setEditableEntries(updatedProduct
                                            .getEditableEntries());
                                    product.setData(updatedProduct.getData());
                                    if (isDisposed() == false) {
                                        if (isCorrectable) {
                                            /*
                                             * Update the editor tab only for
                                             * corrections. The productHeader
                                             * and VTEC Strings will change from
                                             * regenerate call and need updated.
                                             */
                                            editorManager.getProductDataEditor(
                                                    product).updateValues(
                                                    product);
                                        }
                                        editorManager
                                                .updateFormattedTextViewers(product);
                                    }
                                }
                            }
                        }

                        if (isCorrectable) {
                            setPrevGeneratedProductListStorage(prevGeneratedProductListStorage);

                            /*
                             * Update the state of the issueAll button
                             */
                            updateButtons();
                        }
                    } finally {
                        if (isDisposed() == false) {
                            progressBar.setVisible(false);
                        }
                    }
                };
            });

        }

        @Override
        public void jobFailed(Throwable e) {
            handler.error("Unable to run product generation", e);
            progressBar.setVisible(false);
        }

    };

    /**
     * Sets the GeneratedProductList object
     * 
     * @param generatedProductListStorage
     *            The GeneratedProductList to set
     */
    public void setPrevGeneratedProductListStorage(
            List<GeneratedProductList> generatedProductListStorage) {
        List<GeneratedProductList> copy = new ArrayList<GeneratedProductList>(
                generatedProductListStorage.size());
        for (GeneratedProductList generatedProductList : generatedProductListStorage) {
            copy.add(new GeneratedProductList(generatedProductList));
        }
        this.prevGeneratedProductListStorage = copy;
    }

    /**
     * Updates the state of the issueAll button based on whether any
     * requiredFields are not completed.
     */
    @Override
    public void updateButtons() {
        boolean setEnabled = true;
        for (AbstractDataEditor editor : editorManager.getAllEditors()) {
            if (!editor.requiredFieldsCompleted()) {
                setEnabled = false;
                break;
            }
        }
        issueAllButton.setEnabled(setEnabled);
    }

    private void invokeIssue(boolean isCorrectable) {
        if (isCorrectable) {
            issueHandler.commandInvoked(HazardConstants.CORRECTION_FLAG);
        } else {
            issueHandler.commandInvoked(HazardConstants.ISSUE_FLAG);
        }
    }

    private void invokeDismiss(boolean regenerate) {
        if (regenerate) {
            dismissHandler.commandInvoked(HazardConstants.REGENERATE_FLAG);
        } else {
            dismissHandler.commandInvoked(DISMISS_BUTTON_LABEL);
        }
    }

    /**
     * Get the issue command invoker.
     * 
     * @return Continue command invoker.
     */
    public ICommandInvoker<String> getIssueInvoker() {
        return issueInvoker;
    }

    /**
     * Get the dismiss command invoker.
     * 
     * @param
     * @return The Dismiss command invoker
     */
    public ICommandInvoker<String> getDismissInvoker() {
        return dismissInvoker;
    }

    private final Listener shellCloseListener = new Listener() {
        @Override
        public void handleEvent(Event event) {
            if (dismissHandler != null) {
                dismissHandler.commandInvoked(null);
            }
        }
    };

    /**
     * Issuance command invocation handler. Using these handlers, together with
     * invokers, allows handlers to be registered with this view by the
     * presenter, thus in turn allowing the view to know nothing about the
     * presenter and remain stupid, as it should. These handlers and invoker
     * interfaces also allow the use of delegates that will, when a separate
     * worker thread is used in addition to the main UI thread, do the work of
     * safely going between the two threads.
     */
    private ICommandInvocationHandler<String> issueHandler = null;

    /**
     * Issuance command invoker; see comment for {@link #issueHandler} for an
     * explanation of the presenter-view communication scheme being used.
     */
    private final ICommandInvoker<String> issueInvoker = new ICommandInvoker<String>() {

        @Override
        public void setEnabled(String identifier, boolean enable) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setCommandInvocationHandler(
                ICommandInvocationHandler<String> handler) {
            issueHandler = handler;
        }
    };

    /**
     * Dismiss command invocation handler; see comment for {@link #issueHandler}
     * for an explanation of the presenter-view communication scheme being used.
     */
    private ICommandInvocationHandler<String> dismissHandler = null;

    /**
     * Dismiss command invoker; see comment for {@link #issueHandler} for an
     * explanation of the presenter-view communication scheme being used.
     */
    private final ICommandInvoker<String> dismissInvoker = new ICommandInvoker<String>() {

        @Override
        public void setEnabled(String identifier, boolean enable) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setCommandInvocationHandler(
                ICommandInvocationHandler<String> handler) {
            dismissHandler = handler;
        }
    };
}
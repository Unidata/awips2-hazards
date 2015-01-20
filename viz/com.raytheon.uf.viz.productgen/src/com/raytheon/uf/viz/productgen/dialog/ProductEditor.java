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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;

import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.hazards.productgen.GeneratedProductList;
import com.raytheon.uf.common.hazards.productgen.IGeneratedProduct;
import com.raytheon.uf.common.hazards.productgen.ITextProduct;
import com.raytheon.uf.common.hazards.productgen.KeyInfo;
import com.raytheon.uf.common.hazards.productgen.ProductGeneration;
import com.raytheon.uf.common.python.concurrent.IPythonJobListener;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.core.VizApp;
import com.raytheon.uf.viz.productgen.dialog.formats.AbstractFormatTab;
import com.raytheon.uf.viz.productgen.dialog.formats.TextFormatTab;
import com.raytheon.viz.ui.dialogs.CaveSWTDialog;

/**
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
 * Jan 20, 2015 4476       rferrel      Implement shutdown of ProductGeneration.
 * </pre>
 * 
 * @author jsanchez
 * @version 1.0
 */

public class ProductEditor extends CaveSWTDialog {

    private static final IUFStatusHandler handler = UFStatus
            .getHandler(ProductEditor.class);

    private static final String DIALOG_TITLE = "Product Editor";

    private static final String REVIEW_TITLE = "Product Editor (Review)";

    private static final String DISMISS_DIALOG_TITLE = "Are you sure?";

    private static final String DISMISS_DIALOG_MESSAGE = "Are you sure you want to 'Dismiss'? Your product(s) will NOT be issued and your edits have not been saved.";

    private static final String REGENERATE_DIALOG_TITLE = "Selected Events Changed";

    private static final String REGENERATE_DIALOG_MESSAGE = "The selected events have changed. Do you want to regenerate the products or do you want to close the product editor?";

    private static final String ISSUE_LABEL = "Issue All";

    private static final String SAVE_LABEL = "Save";

    private static final String REVERT_LABEL = "Undo";

    private static final String DISMISS_LABEL = "Dismiss";

    private static final String SAVE_AND_DISMISS_LABEL = "Save and Dismiss";

    private static final String CANCEL_DISMISS = "Return to Product Editor";

    private static final String EDITOR_TAB_LABEL = "Editor";

    private static final int MIN_HEIGHT = 500;

    private static final int MIN_WIDTH = 500;

    private static final String ENTRY_TAB_LABEL_FORMAT = "%s (%d)";

    public static final int ENTRY_PANE_WIDTH = 500;

    public static final int ENTRY_PANE_HEIGHT = 300;

    private final List<GeneratedProductList> generatedProductListStorage;

    private List<GeneratedProductList> prevGeneratedProductListStorage;

    private boolean isCorrectable;

    private CTabFolder productsFolder;

    private int buttonWidth = 0;

    /*
     * The progress bar to display that the formatting is being done currently.
     */
    private ProgressBar progressBar;

    private ProductGeneration productGeneration = new ProductGeneration();

    private Button issueButton;

    private Button saveButton;

    private Button revertButton;

    private MessageDialog selectedEventsModifiedDialog;

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

    private int issueCounter;

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

    public ProductEditor(Shell parentShell,
            List<GeneratedProductList> generatedProductListStorage) {
        super(parentShell, SWT.RESIZE, CAVE.PERSPECTIVE_INDEPENDENT);
        this.generatedProductListStorage = generatedProductListStorage;
        isCorrectable = generatedProductListStorage.get(0).isCorrectable();
        if (isCorrectable) {
            setPrevGeneratedProductListStorage(generatedProductListStorage);
            setText(REVIEW_TITLE);
        } else {
            setText(DIALOG_TITLE);
        }
    }

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
     * 
     * @return a list of GeneratedProductList objects
     */
    public List<GeneratedProductList> getGeneratedProductListStorage() {
        return this.generatedProductListStorage;
    }

    @Override
    protected void initializeComponents(Shell shell) {
        shell.setMinimumSize(MIN_WIDTH, MIN_HEIGHT);
        shell.setLayout(new GridLayout(1, false));
        shell.setLayoutData(new GridData(SWT.DEFAULT, SWT.DEFAULT, false, false));
        shell.addListener(SWT.Close, new Listener() {
            @Override
            public void handleEvent(Event event) {
                if (dismissHandler != null) {
                    dismissHandler.commandInvoked(null);
                }
            }
        });

        Composite fullComp = new Composite(shell, SWT.NONE);
        setLayoutInfo(fullComp, 1, false, SWT.FILL, SWT.FILL, true, true, null);
        buildProductTabs(fullComp, generatedProductListStorage);
        createButtonComp(fullComp);
    }

    protected static void setLayoutInfo(Composite comp, int cols,
            boolean colsEqualWidth, int horFil, int verFil,
            boolean grabHorSpace, boolean grabVerSpace, Point bounds) {
        GridLayout layout = new GridLayout(cols, colsEqualWidth);
        GridData layoutData = new GridData(horFil, verFil, grabHorSpace,
                grabVerSpace);
        if (bounds != null) {
            layoutData.widthHint = bounds.x;
            layoutData.heightHint = bounds.y;
        }
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        comp.setLayout(layout);
        comp.setLayoutData(layoutData);
    }

    /**
     * Method for ease of use to make all the button sizes the same.
     * 
     * @param button
     */
    private void setButtonGridData(Button button) {
        GridData data = new GridData(SWT.FILL, SWT.NONE, true, false);
        button.setLayoutData(data);
        button.pack();

        if (button.getSize().x < buttonWidth) {
            data.widthHint = buttonWidth;
            button.setLayoutData(data);
        }
    }

    private void buildProductTabs(Composite comp,
            List<GeneratedProductList> generatedProductListStorage) {
        productsFolder = new CTabFolder(comp, SWT.BORDER);
        setLayoutInfo(productsFolder, 1, false, SWT.FILL, SWT.FILL, true, true,
                null);

        // Create progress bar
        progressBar = new ProgressBar(productsFolder, SWT.INDETERMINATE);
        progressBar.setVisible(false);
        productsFolder.setTopRight(progressBar);

        for (GeneratedProductList products : generatedProductListStorage) {
            for (int folderIndex = 0; folderIndex < products.size(); folderIndex++) {
                IGeneratedProduct product = products.get(folderIndex);

                CTabItem productIDTab = new CTabItem(productsFolder, SWT.NONE);
                productIDTab.setText(product.getProductID());

                Composite productComposite = new Composite(productsFolder,
                        SWT.NONE);
                productIDTab.setControl(productComposite);

                setLayoutInfo(productComposite, 1, true, SWT.FILL, SWT.FILL,
                        true, true, null);
                createFormatTabs(productComposite, product);
            }
        }

        productsFolder.setSelection(0);
    }

    private void createFormatTabs(Composite comp, IGeneratedProduct product) {
        CTabFolder editorAndFormatsFolder = new CTabFolder(comp, SWT.BOTTOM);
        setLayoutInfo(editorAndFormatsFolder, 1, false, SWT.FILL, SWT.FILL,
                true, true, null);
        editorAndFormatsFolder.setSelection(0);

        DataEditor dataEditor = new DataEditor(this, product.getData());
        if (dataEditor.isDataEditable()) {
            CTabItem editorTab = new CTabItem(editorAndFormatsFolder,
                    SWT.VERTICAL);
            editorTab.setText(EDITOR_TAB_LABEL);
            Composite editorPane = new Composite(editorAndFormatsFolder,
                    SWT.NONE);
            setLayoutInfo(editorPane, 1, false, SWT.FILL, SWT.FILL, true, true,
                    null);
            dataEditor.create(editorPane);
            editorTab.setControl(editorPane);
            editorTab.setData(dataEditor);
        }

        for (Entry<String, List<Serializable>> entry : product.getEntries()
                .entrySet()) {
            List<Serializable> values = entry.getValue();
            for (int counter = 0; counter < values.size(); counter++) {
                Serializable formatterResult = values.get(counter);
                CTabItem formatterResultTab = new CTabItem(
                        editorAndFormatsFolder, SWT.VERTICAL);
                AbstractFormatTab tab = null;
                Composite formatterResultPane = new Composite(
                        editorAndFormatsFolder, SWT.NONE);
                setLayoutInfo(formatterResultPane, 1, false, SWT.FILL,
                        SWT.FILL, true, true, null);

                /*
                 * Takes into CAP products that do not segment but rather have
                 * separate results
                 */
                String label = entry.getKey();
                if (values.size() > 1) {
                    label = String.format(ENTRY_TAB_LABEL_FORMAT,
                            entry.getKey(), counter);
                }
                formatterResultTab.setText(label);

                if (product instanceof ITextProduct) {
                    StyledText text = new StyledText(formatterResultPane,
                            SWT.H_SCROLL | SWT.V_SCROLL | SWT.READ_ONLY);
                    text.setWordWrap(false);
                    text.setAlwaysShowScrollBars(false);
                    setLayoutInfo(text, 1, false, SWT.FILL, SWT.FILL, true,
                            true,
                            new Point(ENTRY_PANE_WIDTH, ENTRY_PANE_HEIGHT));

                    formatterResultTab.setControl(formatterResultPane);
                    tab = new TextFormatTab();
                    ((TextFormatTab) tab).setText(text);
                    tab.setTabItem(formatterResultTab);

                    String finalProduct = String.valueOf(formatterResult);
                    text.setText(finalProduct);
                }

                formatterResultTab.setData(tab);
            }
        }

        editorAndFormatsFolder.setSelection(0);
    }

    private CTabItem[] getCurrentFormatTabs() {
        CTabItem selectedProductIDTab = productsFolder.getSelection();
        Composite productComposite = (Composite) selectedProductIDTab
                .getControl();
        CTabFolder editorAndFormatsFolder = (CTabFolder) productComposite
                .getChildren()[0];
        return editorAndFormatsFolder.getItems();
    }

    private DataEditor[] getAllDataEditors() {
        DataEditor[] dataEditors = new DataEditor[productsFolder.getItemCount()];
        for (int i = 0; i < productsFolder.getItemCount(); i++) {
            CTabItem productTabItem = productsFolder.getItems()[i];
            Composite productComposite = (Composite) productTabItem
                    .getControl();
            CTabFolder editorAndFormatsFolder = (CTabFolder) productComposite
                    .getChildren()[0];
            CTabItem[] editorAndFormatTabs = editorAndFormatsFolder.getItems();
            DataEditor dataEditor = null;
            if (editorAndFormatTabs[0].getText().equals(EDITOR_TAB_LABEL)) {
                dataEditor = (DataEditor) editorAndFormatTabs[0].getData();
            }
            dataEditors[i] = dataEditor;
        }

        return dataEditors;
    }

    /**
     * Creates the button comp at the bottom populated with the necessary
     * buttons.
     * 
     * @param comp
     */
    private void createButtonComp(Composite comp) {
        Composite buttonComp = new Composite(comp, SWT.NONE);
        GridLayout layout = new GridLayout(4, false);
        GridData data = new GridData(SWT.CENTER, SWT.CENTER, true, false);
        buttonComp.setLayout(layout);
        buttonComp.setLayoutData(data);
        createIssueButton(buttonComp);
        createSaveButton(buttonComp);
        createRevertButton(buttonComp);
        createDismissButton(buttonComp);
        buttonWidth = 0;
    }

    private void createIssueButton(Composite buttonComp) {
        issueButton = new Button(buttonComp, SWT.PUSH);
        issueButton.setText(ISSUE_LABEL);
        setButtonGridData(issueButton);
        issueButton.setEnabled(true);
        buttonWidth = issueButton.getSize().x;
        /*
         * Checks to see if there are any required fields that needs to be
         * completed.
         */
        for (DataEditor dataEditor : getAllDataEditors()) {
            if (dataEditor != null && dataEditor.isDataEditable()) {
                if (dataEditor.requiredFieldsCompleted() == false) {
                    issueButton.setEnabled(false);
                    break;
                }
            }
        }

        issueButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                // Calling the product generators to apply any required changes
                // such as an updated ETN, issue time, etc.
                issueCounter = 0;
                for (GeneratedProductList products : generatedProductListStorage) {
                    List<LinkedHashMap<KeyInfo, Serializable>> dataList = new ArrayList<LinkedHashMap<KeyInfo, Serializable>>();
                    for (IGeneratedProduct product : products) {
                        dataList.add(product.getData());
                    }
                    List<String> formats = new ArrayList<String>(products
                            .get(0).getEntries().keySet());
                    productGeneration.update(products.getProductInfo(),
                            dataList, null,
                            formats.toArray(new String[formats.size()]),
                            issueListener);
                }
            }
        });

        /*
         * If the product is correctable, disable the issue button unless they
         * make a modification, which means the product has been corrected.
         */
        if (isCorrectable) {
            issueButton.setEnabled(false);
        }
    }

    protected IPythonJobListener<GeneratedProductList> issueListener = new IPythonJobListener<GeneratedProductList>() {

        @Override
        public void jobFinished(final GeneratedProductList productList) {
            VizApp.runAsync(new Runnable() {
                @Override
                public void run() {

                    int totalSize = 0;
                    for (GeneratedProductList products : generatedProductListStorage) {
                        if (products.getProductInfo().equals(
                                productList.getProductInfo())) {
                            int index = 0;
                            for (IGeneratedProduct product : productList) {
                                products.set(index, product);
                                index++;
                                issueCounter++;
                            }
                        }
                        totalSize += products.size();
                    }

                    // Indicates that all generation is completed
                    if (issueCounter == totalSize) {
                        for (DataEditor dataEditor : getAllDataEditors()) {
                            if (dataEditor != null) {
                                dataEditor.saveModifiedValues();
                            }
                        }
                        issueCounter = 0;
                        invokeIssue(isCorrectable);
                        close();
                    }
                };
            });

        }

        @Override
        public void jobFailed(Throwable e) {
            handler.error("Unable to issue", e);
        }

    };

    private void createSaveButton(Composite buttonComp) {
        saveButton = new Button(buttonComp, SWT.PUSH);
        saveButton.setText(SAVE_LABEL);
        setButtonGridData(saveButton);
        saveButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                CTabItem editorTab = getCurrentFormatTabs()[0];
                DataEditor dataEditor = (DataEditor) editorTab.getData();
                dataEditor.saveModifiedValues();
                saveButton.setEnabled(false);
                revertButton.setEnabled(false);
            }
        });
        saveButton.setEnabled(false);
    }

    private void createRevertButton(Composite buttonComp) {
        revertButton = new Button(buttonComp, SWT.PUSH);
        revertButton.setText(REVERT_LABEL);
        setButtonGridData(revertButton);
        revertButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                DataEditor dataEditor = (DataEditor) getCurrentFormatTabs()[0]
                        .getData();
                dataEditor.revertValues();
                saveButton.setEnabled(dataEditor.hasUnsavedChanges());
                revertButton.setEnabled(false);
            }
        });
        revertButton.setEnabled(false);
    }

    private void createDismissButton(Composite buttonComp) {
        Button dismissButton = new Button(buttonComp, SWT.PUSH);
        dismissButton.setText(DISMISS_LABEL);
        setButtonGridData(dismissButton);

        dismissButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                boolean hasUnsavedChanges = false;
                for (DataEditor dataEditor : getAllDataEditors()) {
                    if (dataEditor != null && dataEditor.hasUnsavedChanges()) {
                        String[] buttonLabels = null;

                        if (isCorrectable) {
                            buttonLabels = new String[] { DISMISS_LABEL,
                                    CANCEL_DISMISS };
                        } else {
                            buttonLabels = new String[] { DISMISS_LABEL,
                                    SAVE_AND_DISMISS_LABEL, CANCEL_DISMISS };
                        }

                        MessageDialog dismissDialog = new MessageDialog(null,
                                DISMISS_DIALOG_TITLE, null,
                                DISMISS_DIALOG_MESSAGE, MessageDialog.WARNING,
                                buttonLabels, 0) {
                            protected void buttonPressed(int buttonId) {
                                setReturnCode(buttonId);
                                close();
                            }
                        };

                        String buttonLabel = buttonLabels[dismissDialog.open()];
                        if (buttonLabel.equals(DISMISS_LABEL)) {
                            invokeDismiss(false);
                        } else if (buttonLabel.equals(SAVE_AND_DISMISS_LABEL)) {
                            for (DataEditor de : getAllDataEditors()) {
                                if (de != null) {
                                    de.saveModifiedValues();
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

    protected Button getIssueButton() {
        return issueButton;
    }

    protected Button getSaveButton() {
        return saveButton;
    }

    protected Button getRevertButton() {
        return revertButton;
    }

    private final IPythonJobListener<GeneratedProductList> generateListener = new IPythonJobListener<GeneratedProductList>() {

        @Override
        public void jobFinished(final GeneratedProductList productList) {
            VizApp.runAsync(new Runnable() {
                @Override
                public void run() {
                    try {
                        CTabItem[] currentFormatTabs = getCurrentFormatTabs();
                        Map<String, Object> formatTabsMap = new HashMap<>();
                        for (CTabItem formatTabItem : currentFormatTabs) {
                            formatTabsMap.put(formatTabItem.getText(),
                                    formatTabItem.getData());
                        }

                        DataEditor dataEditor = (DataEditor) formatTabsMap
                                .get(EDITOR_TAB_LABEL);
                        Serializable modifiedValue = dataEditor
                                .getOneModifiedValue();
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
                                    updateFormatTabs(formatTabsMap, product,
                                            modifiedValue);
                                }
                            }
                        }

                        if (isCorrectable) {
                            setPrevGeneratedProductListStorage(prevGeneratedProductListStorage);
                        }
                    } finally {
                        progressBar.setVisible(false);
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

    private void updateFormatTabs(Map<String, Object> formatTabsMap,
            IGeneratedProduct product, Serializable value) {
        Set<String> formats = product.getEntries().keySet();

        for (String format : formats) {
            List<Serializable> entries = product.getEntry(format);
            for (int labelCounter = 0; labelCounter < entries.size(); labelCounter++) {
                Serializable entry = entries.get(labelCounter);
                String label = format;
                if (entries.size() > 1) {
                    label = String.format(ENTRY_TAB_LABEL_FORMAT, format,
                            labelCounter);
                }
                AbstractFormatTab tab = (AbstractFormatTab) formatTabsMap
                        .get(label);

                if (tab instanceof TextFormatTab) {
                    TextFormatTab textTab = (TextFormatTab) tab;
                    StyledText styledText = textTab.getText();

                    String finalProduct = String.valueOf(entry);
                    styledText.setData(styledText.getText());
                    styledText.setText(finalProduct);
                    if (value != null) {
                        highlightText(styledText, String.valueOf(value));
                    }
                }
            }
        }
    }

    protected void regenerate() {
        progressBar.setVisible(true);
        for (GeneratedProductList products : generatedProductListStorage) {
            List<LinkedHashMap<KeyInfo, Serializable>> dataList = new ArrayList<>();
            for (IGeneratedProduct product : products) {
                dataList.add(product.getData());
            }

            List<LinkedHashMap<KeyInfo, Serializable>> prevDataList = null;
            if (isCorrectable) {
                prevDataList = new ArrayList<LinkedHashMap<KeyInfo, Serializable>>();
                for (GeneratedProductList prevGeneratedProductList : prevGeneratedProductListStorage) {
                    if (prevGeneratedProductList.getProductInfo().equals(
                            products.getProductInfo())) {
                        for (IGeneratedProduct prevProduct : prevGeneratedProductList) {
                            prevDataList.add(prevProduct.getData());
                        }
                        break;
                    }
                }
            }

            List<String> formats = new ArrayList<String>(products.get(0)
                    .getEntries().keySet());
            productGeneration.update(products.getProductInfo(), dataList,
                    prevDataList, formats.toArray(new String[formats.size()]),
                    generateListener);
        }
    }

    private void highlightText(StyledText styledText, String text) {
        // clears highlighting
        styledText.setSelectionRange(0, 0);

        String currentText = styledText.getText();
        String oldText = String.valueOf(styledText.getData());

        String currentLowerCase = currentText.toLowerCase();
        String textLowerCase = text.toLowerCase();
        List<Integer> possibleStartIndices = new ArrayList<Integer>();
        int startIndex = 0;
        do {
            startIndex = currentLowerCase.substring(startIndex).indexOf(
                    textLowerCase);
            if (startIndex != -1) {
                possibleStartIndices.add(startIndex);
            }
            startIndex += 1;
        } while (startIndex != 0);

        int length = text.trim().length();
        if (possibleStartIndices.isEmpty()) {
            startIndex = 0;
            length = 0;
        } else if (possibleStartIndices.size() == 1) {
            startIndex = possibleStartIndices.get(0);
        } else {
            /*
             * If the text is found in multiple places in the currentText, then
             * this will help determine which of the possibleStartIndices is the
             * correct one.
             */
            int difference = StringUtils
                    .indexOfDifference(currentText, oldText);
            for (Integer possibleStartIndex : possibleStartIndices) {
                if (possibleStartIndex <= difference
                        && difference < possibleStartIndex + text.length()) {
                    startIndex = possibleStartIndex;
                    break;
                }
            }
        }

        // performs highlighting
        styledText.setSelectionRange(startIndex, length);

        // scrolls to the line
        int counter = 0;
        for (int lineIndex = 0; lineIndex < styledText.getLineCount()
                && startIndex != -1; lineIndex++) {
            String line = styledText.getLine(lineIndex);
            // Add 1 for the new line char
            counter += line.length() + 1;
            if (counter >= startIndex) {
                styledText.setTopIndex(lineIndex);
                break;
            }

        }
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
            dismissHandler.commandInvoked(DISMISS_LABEL);
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

    /**
     * Shows a dialog notifying the user he has selected other hazard events in
     * the console. The user will have the option to regenerate the product(s)
     * or just close the product editor dialog.
     */
    public void showSelectedEventsModifiedDialog() {
        if (selectedEventsModifiedDialog == null) {
            String[] buttonLabels = new String[] { "Regenerate", "Close Dialog" };
            selectedEventsModifiedDialog = new MessageDialog(null,
                    REGENERATE_DIALOG_TITLE, null, REGENERATE_DIALOG_MESSAGE,
                    MessageDialog.WARNING, buttonLabels, 0) {
                protected void buttonPressed(int buttonId) {
                    setReturnCode(buttonId);
                    close();
                }
            };

            boolean regenerate = selectedEventsModifiedDialog.open() == 0;
            invokeDismiss(regenerate);
        }
    }

    @Override
    protected void disposed() {
        productGeneration.shutdown();
        super.disposed();
    }

}
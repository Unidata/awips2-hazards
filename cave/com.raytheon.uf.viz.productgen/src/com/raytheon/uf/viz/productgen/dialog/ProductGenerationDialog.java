package com.raytheon.uf.viz.productgen.dialog;

import gov.noaa.gsd.viz.mvp.widgets.ICommandInvocationHandler;
import gov.noaa.gsd.viz.mvp.widgets.ICommandInvoker;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.CaretEvent;
import org.eclipse.swt.custom.CaretListener;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.raytheon.uf.common.hazards.productgen.GeneratedProductList;
import com.raytheon.uf.common.hazards.productgen.IGeneratedProduct;
import com.raytheon.uf.common.hazards.productgen.ITextProduct;
import com.raytheon.uf.common.hazards.productgen.KeyInfo;
import com.raytheon.uf.common.hazards.productgen.ProductGeneration;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.productgen.dialog.data.AbstractProductGeneratorData;
import com.raytheon.uf.viz.productgen.dialog.data.SegmentData;
import com.raytheon.uf.viz.productgen.dialog.formats.AbstractFormatTab;
import com.raytheon.uf.viz.productgen.dialog.formats.TextFormatTab;
import com.raytheon.uf.viz.productgen.dialog.listener.IssueListener;
import com.raytheon.viz.ui.dialogs.CaveSWTDialog;

/**
 * The dialog to facilitate product generation. This allows users to view all
 * format types of products.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 13, 2013            mnash     Initial creation
 * Nov  5, 2013 2266       jsanchez  Moved ProductUtil to individual formats. Add a call to format again.
 * Feb 18, 2014 2702       jsanchez   Implemented individual CAP segments and the save method. Cleaned up along with the refactor.
 * Apr 11, 2014 2819       Chris.Golden Fixed bugs with the Preview and Issue
 *                                      buttons in the HID remaining grayed out
 *                                      when they should be enabled.
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */

public class ProductGenerationDialog extends CaveSWTDialog {

    private static final IUFStatusHandler handler = UFStatus
            .getHandler(ProductGenerationDialog.class);

    /*
     * ***** CONSTANTS *****
     */
    private static final String ISSUE_LABEL = "Issue";

    private static final String DISMISS_LABEL = "Dismiss";

    private static final String DIALOG_TITLE = "Product Editor";

    private static final int FORMAT_WIDTH = 510;

    private static final int FORMAT_HEIGHT = 200;

    private static final int MIN_HEIGHT = 500;

    private static final int MIN_WIDTH = 900;

    public static final String TAB_LABEL_FORMAT = "%s (%d)";

    /*
     * An arbitrary button width to make sure they are all equal.
     */
    private static final int BUTTON_WIDTH = 80;

    /*
     * ***** SWT ******
     */

    /*
     * The progress bar to display that the formatting is being done currently.
     */
    private ProgressBar bar;

    /*
     * Contains the left and right side
     */
    private CTabFolder folder;

    /*
     * The format CTabFolder (right hand side)
     */
    private final List<CTabFolder> formatFolderList = new ArrayList<CTabFolder>();

    /*
     * ****** Helper maps ******
     */

    private List<GeneratedProductList> generatedProductListStorage;

    /*
     * Map of format to AbstractFormatTab. This is helpful when doing
     * 'highlighting'.
     */
    private final List<Map<String, AbstractFormatTab>> formatTabList = new ArrayList<Map<String, AbstractFormatTab>>();

    /*
     * Maps format to text area. Used in conjunction with the formattedKeyCombo.
     */
    private final List<Map<String, Text>> textAreasList = new ArrayList<Map<String, Text>>();

    private final List<Combo> formattedKeyComboList = new ArrayList<Combo>();

    /*
     * The combo box that contains the segment ID. Controls what data to display
     * in the unformatted and formatted tabs.
     */
    private final List<Combo> segmentsComboList = new ArrayList<Combo>();

    /*
     * Helps identify the segments of a the python data dictionary
     */
    private final List<List<AbstractProductGeneratorData>> decodedAbstractProductGeneratorData = new ArrayList<List<AbstractProductGeneratorData>>();

    private boolean notHighlighting = true;

    /*
     * TODO, the following need to be looked into whether they are necessary
     */
    /*
     * TODO Continue command invocation handler, is this needed this way?
     */
    private ICommandInvocationHandler issueHandler = null;

    /*
     * TODO, Continue command invoker, is this needed this way?
     */
    private final ICommandInvoker issueInvoker = new ICommandInvoker() {
        @Override
        public void setCommandInvocationHandler(
                ICommandInvocationHandler handler) {
            issueHandler = handler;
        }
    };

    /*
     * TODO Dismiss command invocation handler, is this needed this way?
     */
    private ICommandInvocationHandler dismissHandler = null;

    /*
     * TODO Dismiss command invoker, is this needed this way?
     */
    private final ICommandInvoker dismissInvoker = new ICommandInvoker() {
        @Override
        public void setCommandInvocationHandler(
                ICommandInvocationHandler handler) {
            dismissHandler = handler;
        }
    };

    private final IssueListener issueListener;

    /**
     * @param parentShell
     */
    public ProductGenerationDialog(Shell parentShell) {
        super(parentShell, SWT.RESIZE, CAVE.PERSPECTIVE_INDEPENDENT);
        setText(DIALOG_TITLE);
        issueListener = new IssueListener(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.viz.ui.dialogs.CaveSWTDialogBase#initializeComponents(org
     * .eclipse.swt.widgets.Shell)
     */
    @Override
    protected void initializeComponents(Shell shell) {
        shell.setMinimumSize(MIN_WIDTH, MIN_HEIGHT);
        shell.setLayout(new GridLayout(1, false));
        shell.setLayoutData(new GridData(SWT.DEFAULT, SWT.DEFAULT, false, false));
        shell.addListener(SWT.Close, new Listener() {
            @Override
            public void handleEvent(Event event) {
                if (dismissHandler != null) {
                    dismissHandler.commandInvoked(DISMISS_LABEL);
                }
            }
        });

        Composite fullComp = new Composite(shell, SWT.NONE);
        setLayoutInfo(fullComp, 1, false, SWT.FILL, SWT.FILL, true, true, null);

        buildProductTabs(fullComp);
        createButtonComp(fullComp);
    }

    /**
     * Takes and puts together the products into their viewable form.
     * 
     * @param comp
     * @param product
     */
    private Map<String, AbstractFormatTab> createFormatEditor(Composite comp,
            IGeneratedProduct product) {
        CTabFolder formatFolder = new CTabFolder(comp, SWT.BORDER);
        setLayoutInfo(formatFolder, 1, false, SWT.FILL, SWT.FILL, true, true,
                null);
        Map<String, AbstractFormatTab> formatTabMap = buildFormatTabs(product,
                formatFolder);
        formatFolder.setSelection(0);
        formatFolderList.add(formatFolder);
        return formatTabMap;
    }

    /**
     * Builds a tab for each product that is set. Internally this calls into
     * methods to create the underlying format editor and data editor (right and
     * left sides).
     * 
     * @param comp
     */
    private void buildProductTabs(Composite comp) {
        folder = new CTabFolder(comp, SWT.BORDER);
        folder.setSelection(0);
        bar = new ProgressBar(folder, SWT.INDETERMINATE);
        bar.setVisible(false);
        folder.setTopRight(bar);
        setLayoutInfo(folder, 1, false, SWT.FILL, SWT.FILL, true, true, null);
        formatTabList.clear();
        formattedKeyComboList.clear();
        formatFolderList.clear();
        int offset = 0;
        for (GeneratedProductList products : generatedProductListStorage) {
            for (int folderIndex = 0; folderIndex < products.size(); folderIndex++) {
                IGeneratedProduct product = products.get(folderIndex);
                LinkedHashMap<KeyInfo, Serializable> data = product.getData();

                CTabItem item = new CTabItem(folder, SWT.NONE);
                String productId = product.getProductID();
                // populate the title with the product id
                item.setText(productId);

                SashForm sashForm = new SashForm(folder, SWT.HORIZONTAL);

                /*
                 * If no data is editable, or no data has been sent in at all
                 * (which shouldn't happen, more for the first case), then we
                 * only want to present the right hand side of the dialog that
                 * is not editable.
                 */
                boolean isEditable = ProductGenerationDialogUtility
                        .countEditables(data) > 0;
                DataEditor dataEditor = null;
                if (isEditable) {
                    folder.setSelection(0);

                    setLayoutInfo(sashForm, 2, false, SWT.FILL, SWT.FILL,
                            false, false, null);
                    Composite leftComp = new Composite(sashForm, SWT.NONE);
                    setLayoutInfo(leftComp, 1, false, SWT.FILL, SWT.FILL, true,
                            true, null);
                    dataEditor = new DataEditor(this, leftComp, offset
                            + folderIndex);
                } else {
                    /*
                     * Makes sure the segmentComboList size matches with the
                     * number of generated products.
                     */
                    addSegmentCombo(null);
                    setLayoutInfo(sashForm, 1, true, SWT.FILL, SWT.FILL, true,
                            true, null);
                }

                Composite rightComp = new Composite(sashForm, SWT.NONE);
                setLayoutInfo(rightComp, 1, false, SWT.FILL, SWT.FILL, true,
                        true, null);

                if (isEditable) {
                    sashForm.setWeights(new int[] { 35, 65 });
                }
                sashForm.layout();

                Map<String, AbstractFormatTab> formatTabMap = createFormatEditor(
                        rightComp, product);
                formatTabList.add(formatTabMap);
                item.setControl(sashForm);
                item.setData(dataEditor);
            }
            offset += products.size();
        }

        notifySegmentCombosList();
    }

    /**
     * Builds the format tabs, if the tab already exists it will take and
     * replace that one.
     * 
     * @param product
     */
    private Map<String, AbstractFormatTab> buildFormatTabs(
            IGeneratedProduct product, CTabFolder formatFolder) {
        Set<String> formats = product.getEntries().keySet();

        Map<String, AbstractFormatTab> formatTabMap = new HashMap<String, AbstractFormatTab>();
        for (String format : formats) {
            List<Serializable> entries = product.getEntry(format);
            int counter = 0;
            for (Serializable entry : entries) {
                AbstractFormatTab tab = null;
                Composite editorComp = new Composite(formatFolder, SWT.NONE);
                setLayoutInfo(editorComp, 1, false, SWT.FILL, SWT.FILL, true,
                        true, null);

                CTabItem formatItem = new CTabItem(formatFolder, SWT.NONE);
                String label = format;
                if (entries.size() > 1) {
                    label = String.format(TAB_LABEL_FORMAT, format, counter);
                }
                formatItem.setText(label);

                if (product instanceof ITextProduct) {
                    StyledText text = new StyledText(editorComp, SWT.H_SCROLL
                            | SWT.V_SCROLL | SWT.READ_ONLY);
                    text.setWordWrap(false);
                    text.setAlwaysShowScrollBars(false);
                    setLayoutInfo(text, 1, false, SWT.FILL, SWT.FILL, true,
                            true, new Point(FORMAT_WIDTH, FORMAT_HEIGHT));

                    formatItem.setControl(editorComp);
                    tab = new TextFormatTab();
                    ((TextFormatTab) tab).setText(text);
                    tab.setTabItem(formatItem);
                    formatTabMap.put(label, tab);
                }
                if (product instanceof ITextProduct) {
                    try {
                        String finalProduct = String.valueOf(entry);
                        StyledText text = ((TextFormatTab) tab).getText();
                        text.setText(finalProduct);
                        text.addCaretListener(new CaretListener() {

                            @Override
                            public void caretMoved(CaretEvent event) {
                                Combo currentCombo = getCurrentSegmentCombo();
                                if (currentCombo != null
                                        && currentCombo.getItemCount() > 1) {
                                    StyledText styledText = (StyledText) event
                                            .getSource();
                                    int offset = event.caretOffset;
                                    String text = styledText.getText();
                                    int selection = 0;
                                    for (int i = 1; i < currentCombo
                                            .getItemCount(); i++) {
                                        String item = currentCombo.getItem(i);
                                        int startIndex = text.indexOf(item);
                                        if (startIndex != -1) {
                                            int endIndex = text.indexOf(
                                                    SegmentData.SEGMENT_END,
                                                    startIndex);
                                            if (endIndex != -1
                                                    && offset > startIndex
                                                    && offset < endIndex) {
                                                selection = i;
                                                break;
                                            }
                                        }
                                    }

                                    if (notHighlighting && selection != 0) {
                                        currentCombo.select(selection);
                                        Event e = new Event();
                                        e.data = false;
                                        currentCombo.notifyListeners(
                                                SWT.Selection, e);
                                    }

                                }

                            }
                        });

                    } catch (ClassCastException e) {
                        handler.error("Error building format tabs", e);
                    }
                }

                counter++;
            }

        }

        return formatTabMap;
    }

    /**
     * Creates the button comp at the bottom populated with the necessary
     * buttons.
     * 
     * @param comp
     */
    private void createButtonComp(Composite comp) {
        Composite buttonComp = new Composite(comp, SWT.NONE);
        GridLayout layout = new GridLayout(2, false);
        GridData data = new GridData(SWT.CENTER, SWT.CENTER, true, false);
        buttonComp.setLayout(layout);
        buttonComp.setLayoutData(data);
        createIssueButton(buttonComp);
        createDismissButton(buttonComp);
    }

    /**
     * The issue button will take the product, put it to its "viewable" form,
     * and send it out.
     * 
     * @param buttonComp
     */
    private void createIssueButton(Composite buttonComp) {
        Button issueButton = new Button(buttonComp, SWT.PUSH);
        issueButton.setText(ISSUE_LABEL);
        setButtonGridData(issueButton);
        issueButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                // Calling the product generators to apply any required changes
                // such as an updated ETN, issue time, etc.
                ProductGeneration generation = new ProductGeneration();
                for (GeneratedProductList products : generatedProductListStorage) {
                    List<LinkedHashMap<KeyInfo, Serializable>> dataList = new ArrayList<LinkedHashMap<KeyInfo, Serializable>>();
                    for (IGeneratedProduct product : products) {
                        dataList.add(product.getData());
                    }
                    List<String> formats = new ArrayList<String>(products
                            .get(0).getEntries().keySet());
                    generation.update(products.getProductInfo(), dataList,
                            formats.toArray(new String[formats.size()]),
                            issueListener);
                }
            }
        });
    }

    /**
     * Saves any modifications made to the unformatted data.
     */
    public void save() {
        int offset = 0;
        for (GeneratedProductList products : generatedProductListStorage) {
            for (int index = 0; index < products.size(); index++) {
                ProductGenerationDialogUtility
                        .save(decodedAbstractProductGeneratorData.get(offset
                                + index));
            }
            offset += products.size();
        }
    }

    /**
     * The button to dismiss the dialog.
     * 
     * @param buttonComp
     */
    private void createDismissButton(Composite buttonComp) {
        Button cancelButton = new Button(buttonComp, SWT.PUSH);
        cancelButton.setText(DISMISS_LABEL);
        setButtonGridData(cancelButton);
        cancelButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                boolean dismiss = MessageDialog
                        .openQuestion(
                                getParent(),
                                "Are you sure?",
                                "Are you sure you want to 'Dismiss'? Your products will NOT be issued and will remain in a PENDING state.");
                if (dismiss) {
                    // TODO should reevaluate why we need a String passed in
                    // here
                    dismissHandler.commandInvoked(DISMISS_LABEL);
                }
            }
        });
    }

    /**
     * Method for ease of use to make all the button sizes the same.
     * 
     * @param button
     */
    public void setButtonGridData(Button button) {
        GridData data = new GridData(SWT.FILL, SWT.NONE, true, false);
        data.widthHint = BUTTON_WIDTH;
        button.setLayoutData(data);
    }

    /**
     * Helper method to make setting the grid data and grid layout shorter.
     * 
     * @param comp
     * @param cols
     * @param colsEqualWidth
     * @param horFil
     * @param verFil
     * @param grabHorSpace
     * @param grabVerSpace
     * @param bounds
     */
    public void setLayoutInfo(Composite comp, int cols, boolean colsEqualWidth,
            int horFil, int verFil, boolean grabHorSpace, boolean grabVerSpace,
            Point bounds) {
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

    public void setGeneratedProductListStorage(
            List<GeneratedProductList> generatedProductListStorage) {
        this.generatedProductListStorage = generatedProductListStorage;
        for (GeneratedProductList products : generatedProductListStorage) {
            for (IGeneratedProduct product : products) {
                List<AbstractProductGeneratorData> decodedData = new ArrayList<AbstractProductGeneratorData>();
                decodedData.addAll(ProductGenerationDialogUtility
                        .decodeProductGeneratorData(product.getData()));
                decodedAbstractProductGeneratorData.add(decodedData);
            }
        }

    }

    /**
     * 
     * @return a list of GeneratedProductList objects
     */
    public List<GeneratedProductList> getGeneratedProductListStorage() {
        return this.generatedProductListStorage;
    }

    /**
     * 
     * @param index
     * @return a format tab map for a particular index
     */
    public Map<String, AbstractFormatTab> getFormatTabMap(int index) {
        return formatTabList.get(index);
    }

    public Map<String, AbstractFormatTab> getCurrentFormatTabMap() {
        return formatTabList.get(folder.getSelectionIndex());
    }

    public Map<String, Text> getCurrentTextAreas() {
        return textAreasList.get(folder.getSelectionIndex());
    }

    /**
     * Resets the formattedKeyCombos to their first item
     */
    public void resetFormattedKeyCombos() {
        for (Combo formattedKeyCombo : formattedKeyComboList) {
            formattedKeyCombo.select(0);
            formattedKeyCombo.notifyListeners(SWT.Selection, new Event());
        }
    }

    private void notifySegmentCombosList() {
        for (Combo segmentCombos : segmentsComboList) {
            if (segmentCombos != null) {
                segmentCombos.notifyListeners(SWT.Selection, new Event());
            }
        }
    }

    /**
     * Updates the formattedKeyComboList when a new formattedKeyCombo is added
     * for a new folderIndex or replaces the existing formattedKeyCombo if the
     * folderIndex already exists in the list.
     * 
     * @param folderIndex
     * @param formattedKeyCombo
     */
    public void updateFormattedKeyComboList(int folderIndex,
            Combo formattedKeyCombo) {
        if (formattedKeyComboList.isEmpty()
                || folderIndex >= formattedKeyComboList.size()) {
            formattedKeyComboList.add(formattedKeyCombo);
        } else {
            formattedKeyComboList.set(folderIndex, formattedKeyCombo);
        }
    }

    /**
     * Updates the textAreaList when a new textAreaMap is added for a new
     * folderIndex or replaces the existing textAreaMap if the folderIndex
     * already exists in the list.
     * 
     * @param folderIndex
     * @param textAreaMap
     */
    public void updateTextAreasList(int folderIndex,
            Map<String, Text> textAreaMap) {
        if (textAreasList.isEmpty() || folderIndex >= textAreasList.size()) {
            textAreasList.add(textAreaMap);
        } else {
            textAreasList.set(folderIndex, textAreaMap);
        }
    }

    public Combo getCurrentFormattedKeyCombo() {
        return formattedKeyComboList.get(folder.getSelectionIndex());
    }

    public List<AbstractProductGeneratorData> getDecodedDataList(int folderIndex) {
        return decodedAbstractProductGeneratorData.get(folderIndex);
    }

    public CTabFolder getCurrentFormatFolder() {
        return formatFolderList.get(folder.getSelectionIndex());
    }

    public Combo getCurrentSegmentCombo() {
        Combo combo = null;
        if (segmentsComboList.isEmpty() == false) {
            combo = segmentsComboList.get(folder.getSelectionIndex());
        }
        return combo;
    }

    public void addSegmentCombo(Combo segmentsCombo) {
        segmentsComboList.add(segmentsCombo);
    }

    public void setNotHighlighting(boolean flag) {
        this.notHighlighting = flag;
    }

    /**
     * Retrieves the IGeneratedProduct associated with the folder index
     * 
     * @param folderIndex
     * 
     * @return
     */
    public IGeneratedProduct getGeneratedProduct(int folderIndex) {
        int offset = 0;
        for (GeneratedProductList products : generatedProductListStorage) {
            if (folderIndex < offset + products.size()) {
                return products.get(folderIndex - offset);
            }
            offset += products.size();
        }
        return null;
    }

    public IGeneratedProduct getCurrentGeneratedProduct() {
        return getGeneratedProduct(folder.getSelectionIndex());
    }

    public int getFolderSelectionIndex() {
        return folder.getSelectionIndex();
    }

    public ProgressBar getProgressBar() {
        return bar;
    }

    public void invokeIssue() {
        issueHandler.commandInvoked(ISSUE_LABEL);
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

}
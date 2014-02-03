package com.raytheon.uf.viz.productgen.dialog;

import gov.noaa.gsd.viz.megawidgets.ICurrentTimeProvider;
import gov.noaa.gsd.viz.megawidgets.IParametersEditorListener;
import gov.noaa.gsd.viz.megawidgets.MegawidgetException;
import gov.noaa.gsd.viz.megawidgets.ParametersEditorFactory;
import gov.noaa.gsd.viz.mvp.widgets.ICommandInvocationHandler;
import gov.noaa.gsd.viz.mvp.widgets.ICommandInvoker;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.raytheon.uf.common.hazards.productgen.GeneratedProductList;
import com.raytheon.uf.common.hazards.productgen.IGeneratedProduct;
import com.raytheon.uf.common.hazards.productgen.ITextProduct;
import com.raytheon.uf.common.hazards.productgen.ProductGeneration;
import com.raytheon.uf.common.python.concurrent.IPythonJobListener;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.SimulatedTime;
import com.raytheon.uf.viz.core.VizApp;
import com.raytheon.uf.viz.productgen.dialog.formats.AbstractFormatTab;
import com.raytheon.uf.viz.productgen.dialog.formats.TextFormatTab;
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

    private static final String GENERATE_LABEL = "Generate";

    private static final String SAVE_LABEL = "Save";

    private static final String DISMSS_LABEL = "Dismiss";

    private static final String SEGMENT_COMBO_LABEL = "Segments:";

    private static final String UNFORMATTED_LABEL = "Unformatted";

    private static final String FORMATTED_LABEL = "Formatted";

    private static final String DIALOG_TITLE = "Product Editor";

    private static final int FORMAT_TAB_WIDTH = 510;

    private static final int FORMAT_TAB_HEIGHT = 400;

    private static final String FORMATTED_COMBO_INSTRUCTION = "Select a key";

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
    private List<CTabFolder> formatFolderList = new ArrayList<CTabFolder>();

    /*
     * ****** Helper maps ******
     */

    private Map<String, GeneratorInfo> generatorInformationMap = new HashMap<String, GeneratorInfo>();

    /*
     * The products to be shown on the right hand side of the dialog.
     */
    private GeneratedProductList products;

    private int issueCounter;

    /*
     * Map of format to AbstractFormatTab. This is helpful when doing
     * 'highlighting'.
     */
    private List<Map<String, AbstractFormatTab>> formatTabList = new ArrayList<Map<String, AbstractFormatTab>>();

    /*
     * Maps format to text area. Used in conjunction with the formattedKeyCombo.
     */
    private List<Map<String, Text>> textAreasList = new ArrayList<Map<String, Text>>();

    private List<Combo> formattedKeyComboList = new ArrayList<Combo>();

    /*
     * The combo box that contains the segment ID. Controls what data to display
     * in the unformatted and formatted tabs.
     */
    private List<Combo> segmentsComboList = new ArrayList<Combo>();

    /*
     * The formatListener that handles all the work in product generation, as
     * well as sets up the progress bar and calls the method to re-populate the
     * formats
     */
    public final Listener formatListener = createGenerateListener();

    /*
     * Helps identify the segments of a the python data dictionary
     */
    private List<List<Segment>> decodedSegments;

    private boolean unformattedTabSelected = true;

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

    /*
     * TODO Shell closed command invocation handler, should we do this
     * differently?
     */
    private ICommandInvocationHandler shellClosedHandler = null;

    /*
     * TODO Shell closed command invoker, should we do this differently?
     */
    private final ICommandInvoker shellClosedInvoker = new ICommandInvoker() {
        @Override
        public void setCommandInvocationHandler(
                ICommandInvocationHandler handler) {
            shellClosedHandler = handler;
        }
    };

    private final ICurrentTimeProvider currentTimeProvider = new ICurrentTimeProvider() {
        @Override
        public long getCurrentTime() {
            return SimulatedTime.getSystemTime().getMillis();
        }
    };

    /**
     * @param parentShell
     */
    public ProductGenerationDialog(Shell parentShell) {
        super(parentShell, SWT.RESIZE);
        setText(DIALOG_TITLE);
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
        shell.setMinimumSize(500, 300);
        shell.setLayout(new GridLayout(1, false));
        shell.setLayoutData(new GridData(SWT.DEFAULT, SWT.DEFAULT, false, false));

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
        if (products != null) {
            formatTabList.clear();
            formattedKeyComboList.clear();
            formatFolderList.clear();
            for (int i = 0; i < products.size(); i++) {
                IGeneratedProduct product = products.get(i);
                LinkedHashMap<String, Serializable> data = product.getData();

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
                if (isEditable) {
                    folder.setSelection(0);
                    setLayoutInfo(sashForm, 2, true, SWT.FILL, SWT.FILL, true,
                            true, null);
                    Composite leftComp = new Composite(sashForm, SWT.NONE);
                    setLayoutInfo(leftComp, 1, false, SWT.FILL, SWT.FILL, true,
                            true, null);
                    createDataEditor(leftComp, i);
                } else {
                    setLayoutInfo(sashForm, 1, true, SWT.FILL, SWT.FILL, true,
                            true, null);
                }

                Composite rightComp = new Composite(sashForm, SWT.NONE);
                setLayoutInfo(rightComp, 1, false, SWT.FILL, SWT.FILL, true,
                        true, null);

                sashForm.layout();

                Map<String, AbstractFormatTab> formatTabMap = createFormatEditor(
                        rightComp, product);
                formatTabList.add(formatTabMap);
                item.setControl(sashForm);
            }
        }

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
            AbstractFormatTab tab = null;
            Composite editorComp = new Composite(formatFolder, SWT.NONE);
            setLayoutInfo(editorComp, 1, false, SWT.FILL, SWT.FILL, true, true,
                    null);

            CTabItem formatItem = new CTabItem(formatFolder, SWT.NONE);
            formatItem.setText(format);

            if (product instanceof ITextProduct) {
                StyledText text = new StyledText(editorComp, SWT.H_SCROLL
                        | SWT.V_SCROLL | SWT.READ_ONLY);
                text.setWordWrap(false);
                text.setAlwaysShowScrollBars(false);
                setLayoutInfo(text, 1, false, SWT.FILL, SWT.FILL, true, true,
                        new Point(FORMAT_TAB_WIDTH, FORMAT_TAB_HEIGHT));

                formatItem.setControl(editorComp);
                tab = new TextFormatTab();
                ((TextFormatTab) tab).setText(text);
                tab.setTabItem(formatItem);
                formatTabMap.put(format, tab);
            }
            if (product instanceof ITextProduct) {
                try {
                    String finalProduct = ((ITextProduct) product)
                            .getText(format);
                    ((TextFormatTab) tab).getText().setText(finalProduct);
                } catch (ClassCastException e) {
                    handler.error("Error building format tabs", e);
                }
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
        GridLayout layout = new GridLayout(3, false);
        GridData data = new GridData(SWT.CENTER, SWT.CENTER, true, false);
        buttonComp.setLayout(layout);
        buttonComp.setLayoutData(data);
        createIssueButton(buttonComp);
        createSaveButton(buttonComp);
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
                issueCounter = 0;
                final IPythonJobListener<GeneratedProductList> listener = new IPythonJobListener<GeneratedProductList>() {

                    @Override
                    public void jobFinished(
                            final GeneratedProductList productList) {

                        VizApp.runAsync(new Runnable() {
                            public void run() {
                                GeneratorInfo generatorInfo = generatorInformationMap
                                        .get(productList.getProductInfo());
                                int index = generatorInfo.getStart();
                                for (IGeneratedProduct product : productList) {
                                    products.set(index, product);
                                    index++;
                                    issueCounter++;
                                }

                                // Indicates that all generation is completed
                                if (issueCounter == products.size()) {
                                    issueHandler.commandInvoked(ISSUE_LABEL);
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

                // TODO saving the user edits
                // for (int folderIndex = 0; folderIndex <
                // folder.getItemCount(); folderIndex++) {
                // ProductGenerationDialogUtility.save(
                // products.get(folderIndex).getProductID(),
                // decodedSegments.get(folderIndex), products);
                // }

                // Calling the product generators to apply any required changes
                // such as an updated ETN, issue time, etc.
                ProductGeneration generation = new ProductGeneration();
                for (GeneratorInfo generatorInfo : generatorInformationMap
                        .values()) {
                    List<LinkedHashMap<String, Serializable>> dataList = new ArrayList<LinkedHashMap<String, Serializable>>();
                    for (int i = generatorInfo.getStart(); i < generatorInfo
                            .getSize(); i++) {
                        dataList.add(products.get(i).getData());
                    }
                    List<String> formats = new ArrayList<String>(products
                            .get(generatorInfo.getStart()).getEntries()
                            .keySet());
                    generation.update(generatorInfo.getProductGeneratorName(),
                            dataList,
                            formats.toArray(new String[formats.size()]),
                            listener);
                }
            }
        });
    }

    /*
     * Updates the contents on the right with the data provided on the left. If
     * the 'Unformatted' tab is selected than the formatListenered is fired,
     * which passes the updated dictionary to the formatters. If the 'Formatted'
     * view is selected than a simple text replace is performed.
     * 
     * @param buttonComp
     */
    private void createGenerateButton(Composite buttonComp) {
        Button generateButton = new Button(buttonComp, SWT.PUSH);
        generateButton.setText(GENERATE_LABEL);
        setButtonGridData(generateButton);
        generateButton.setEnabled(true);
        generateButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                bar.setVisible(true);
                // TODO Generating products based on 'formatted data' is not
                // fully implemented yet.
                // Only the tab is available for viewing
                if (unformattedTabSelected) {
                    formatListener.handleEvent(null);
                } else {
                    int folderIndex = folder.getSelectionIndex();
                    Combo formattedKeyCombo = formattedKeyComboList
                            .get(folderIndex);
                    int index = formattedKeyCombo.getSelectionIndex();
                    if (index > 0) {
                        String comboSelection = formattedKeyCombo
                                .getItem(index);
                        Map<String, List<LinkedHashMap<String, Serializable>>> editableEntries = products
                                .get(folderIndex).getEditableEntries();
                        Map<String, AbstractFormatTab> formatTabMap = formatTabList
                                .get(folderIndex);
                        Map<String, Text> textAreaMap = textAreasList
                                .get(folderIndex);
                        Combo segmentsCombo = segmentsComboList
                                .get(folderIndex);
                        ProductGenerationDialogUtility.updateEditableEntries(
                                segmentsCombo.getSelectionIndex(),
                                comboSelection, editableEntries, formatTabMap,
                                textAreaMap);
                    }
                }

                bar.setVisible(false);
            }
        });

    }

    /**
     * The save button will save the edits made to the database.
     * 
     * @param buttonComp
     */
    private void createSaveButton(Composite buttonComp) {
        Button saveButton = new Button(buttonComp, SWT.PUSH);
        saveButton.setText(SAVE_LABEL);
        setButtonGridData(saveButton);
        saveButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                // TODO user edited texts needs to be persisted
                // int folderIndex = folder.getSelectionIndex();
                // ProductGenerationDialogUtility.save(products.get(folderIndex)
                // .getProductID(), decodedSegments.get(folder
                // .getSelectionIndex()), products);
            }
        });
    }

    /**
     * The button to dismiss the dialog.
     * 
     * @param buttonComp
     */
    private void createDismissButton(Composite buttonComp) {
        Button cancelButton = new Button(buttonComp, SWT.PUSH);
        cancelButton.setText(DISMSS_LABEL);
        setButtonGridData(cancelButton);
        cancelButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                // TODO should reevaluate why we need a String passed in here
                dismissHandler.commandInvoked(DISMSS_LABEL);
            }
        });
    }

    /**
     * Method for ease of use to make all the button sizes the same.
     * 
     * @param button
     */
    private void setButtonGridData(Button button) {
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
    private void setLayoutInfo(Composite comp, int cols,
            boolean colsEqualWidth, int horFil, int verFil,
            boolean grabHorSpace, boolean grabVerSpace, Point bounds) {
        GridLayout layout = new GridLayout(cols, false);
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

    /*
     * Creates the left side editor for managing the dictionary data and
     * individual formatted data.
     */
    private void createDataEditor(Composite comp, final int folderIndex) {
        final List<Segment> decodedSegmentList = decodedSegments
                .get(folderIndex);
        Composite segmentComposite = new Composite(comp, SWT.NONE);

        segmentComposite.setLayout(new GridLayout(2, false));
        segmentComposite.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER,
                true, false));

        Label segmentsLabel = new Label(segmentComposite, SWT.NONE);
        segmentsLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
                false, 1, 1));
        segmentsLabel.setText(SEGMENT_COMBO_LABEL);
        final Combo segmentsCombo = new Combo(segmentComposite, SWT.READ_ONLY);
        for (Segment segment : decodedSegmentList) {
            segmentsCombo.add(segment.getSegmentID());
        }
        segmentsCombo.select(0);
        segmentsCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
                false, 1, 1));

        CTabFolder dataFolder = new CTabFolder(comp, SWT.BORDER);
        setLayoutInfo(dataFolder, 1, false, SWT.FILL, SWT.FILL, true, true,
                null);

        // set up the 'Unformatted' scroll composite
        ScrolledComposite unformattedScroller = new ScrolledComposite(
                dataFolder, SWT.BORDER | SWT.V_SCROLL);
        setLayoutInfo(unformattedScroller, 1, false, SWT.FILL, SWT.FILL, true,
                true, null);

        final Composite unformattedComp = new Composite(unformattedScroller,
                SWT.NONE);
        setLayoutInfo(unformattedComp, 1, false, SWT.FILL, SWT.FILL, true,
                true, null);
        LinkedHashMap<String, Serializable> unformatedData = (LinkedHashMap<String, Serializable>) decodedSegmentList
                .get(segmentsCombo.getSelectionIndex()).getData();
        addUnformattedEntries(unformattedComp, unformatedData,
                decodedSegmentList);

        unformattedScroller.setExpandHorizontal(true);
        unformattedScroller.setExpandVertical(true);
        unformattedScroller.setContent(unformattedComp);
        unformattedScroller.setMinSize(unformattedComp.computeSize(SWT.DEFAULT,
                SWT.DEFAULT));
        unformattedScroller.layout();

        final CTabItem unformattedTabItem = new CTabItem(dataFolder, SWT.NONE);
        unformattedTabItem.setText(UNFORMATTED_LABEL);
        unformattedTabItem.setControl(unformattedScroller);

        // set up the 'Formatted' scroll composite
        ScrolledComposite formattedScroller = new ScrolledComposite(dataFolder,
                SWT.BORDER | SWT.V_SCROLL);

        final Composite formattedComp = new Composite(formattedScroller,
                SWT.NONE);
        setLayoutInfo(formattedComp, 1, false, SWT.FILL, SWT.FILL, true, true,
                null);
        addFormattedEntries(formattedComp, folderIndex);

        formattedScroller.setExpandHorizontal(true);
        formattedScroller.setExpandVertical(true);
        formattedScroller.setContent(formattedComp);
        formattedScroller.setMinSize(formattedComp.computeSize(SWT.DEFAULT,
                SWT.DEFAULT));
        formattedScroller.layout();

        final CTabItem formattedTabItem = new CTabItem(dataFolder, SWT.NONE);
        formattedTabItem.setText(FORMATTED_LABEL);
        formattedTabItem.setControl(formattedScroller);

        // set up the generate button
        Composite buttonComp = new Composite(comp, SWT.NONE);
        GridLayout layout = new GridLayout(1, false);
        GridData data = new GridData(SWT.CENTER, SWT.CENTER, true, false);
        buttonComp.setLayout(layout);
        buttonComp.setLayoutData(data);
        createGenerateButton(buttonComp);

        segmentsCombo.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                // Clear out composite's contents
                for (Control control : unformattedComp.getChildren()) {
                    control.dispose();
                }

                for (Control control : formattedComp.getChildren()) {
                    control.dispose();
                }

                LinkedHashMap<String, Serializable> unformatedData = (LinkedHashMap<String, Serializable>) decodedSegmentList
                        .get(segmentsCombo.getSelectionIndex()).getData();
                addUnformattedEntries(unformattedComp, unformatedData,
                        decodedSegmentList);
                unformattedComp.layout();
                addFormattedEntries(formattedComp, folderIndex);
                formattedComp.layout();

                // Removes highlighting
                ProductGenerationDialogUtility.clearHighlighting(formatTabList
                        .get(folder.getSelectionIndex()));
            }

        });
        segmentsComboList.add(segmentsCombo);

        dataFolder.setSelection(0);
        dataFolder.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                if (e.item == unformattedTabItem) {
                    // removes the highlighting
                    ProductGenerationDialogUtility
                            .clearHighlighting(formatTabList.get(folder
                                    .getSelectionIndex()));
                    unformattedTabSelected = true;
                } else {
                    formattedKeyComboList.get(folder.getSelectionIndex())
                            .notifyListeners(SWT.Selection, new Event());
                    unformattedTabSelected = false;
                }
            }
        });

    }

    /**
     * Creates the 'Unformatted' tab with the dictionary from the generators
     */
    private void addUnformattedEntries(Composite comp,
            Map<String, Serializable> data,
            final List<Segment> decodedSegmentList) {

        final Map<String, WidgetInfo> widgetInfoMap = WidgetInfoFactory
                .createWidgetInfoMap(data);
        // widgetInfoMap = WidgetInfoFactory.createWidgetInfoMap(data);
        List<String> labels = new ArrayList<String>();
        Map<String, Object> parametersForLabels = new HashMap<String, Object>();

        for (Entry<String, WidgetInfo> entry : widgetInfoMap.entrySet()) {
            labels.add(entry.getKey());
            parametersForLabels
                    .put(entry.getKey(), entry.getValue().getValue());
        }

        try {
            // Use factory to create megawidgets
            ParametersEditorFactory factory = new ParametersEditorFactory();
            factory.buildParametersEditor(comp, labels, parametersForLabels,
                    System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1L),
                    System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1L),
                    currentTimeProvider, new IParametersEditorListener() {
                        @Override
                        public void parameterValueChanged(String label,
                                Object value) {
                            Combo segmentsCombo = segmentsComboList.get(folder
                                    .getSelectionIndex());
                            int segmentNumber = segmentsCombo
                                    .getSelectionIndex();
                            WidgetInfo info = widgetInfoMap.get(label);
                            ArrayList<String> path = new ArrayList<String>(
                                    decodedSegmentList.get(segmentNumber)
                                            .getPath());
                            if (info.getPath() != null) {
                                path.addAll(info.getPath());
                            }
                            path.add(label);
                            ProductGenerationDialogUtility.updateData(path,
                                    products.get(folder.getSelectionIndex())
                                            .getData(), segmentNumber,
                                    (Serializable) value);
                        }
                    });
        } catch (MegawidgetException e) {
            handler.error("Error creating megawidets", e);
        }
    }

    /**
     * Creates the 'Formatted' tab with the editable keys and formatted values.
     */
    private void addFormattedEntries(Composite comp, int folderIndex) {
        final Map<String, List<LinkedHashMap<String, Serializable>>> editableEntries = products
                .get(folderIndex).getEditableEntries();

        // gather all possible interior keys
        Set<String> uniqueKeys = new HashSet<String>();

        for (String key : editableEntries.keySet()) {
            if (editableEntries.get(key) != null) {
                for (LinkedHashMap<String, Serializable> element : editableEntries
                        .get(key)) {
                    if (element != null) {
                        for (String entryKey : element.keySet()) {
                            String keyname = entryKey
                                    .replace(
                                            ProductGenerationDialogUtility.EDITABLE,
                                            "");
                            uniqueKeys.add(keyname);
                        }
                    }
                }
            }
        }

        final Combo formattedKeyCombo = new Combo(comp, SWT.READ_ONLY);
        formattedKeyCombo.add(FORMATTED_COMBO_INSTRUCTION);
        for (String uniqueKey : uniqueKeys) {
            formattedKeyCombo.add(uniqueKey);
        }
        formattedKeyCombo.select(0);
        formattedKeyCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
                true, false, 1, 1));

        formattedKeyCombo.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                int index = formattedKeyCombo.getSelectionIndex();
                int folderIndex = folder.getSelectionIndex();
                Map<String, Text> textAreaMap = textAreasList.get(folderIndex);
                if (index > 0) {

                    String comboSelection = formattedKeyCombo.getItem(index);
                    Map<String, List<LinkedHashMap<String, Serializable>>> editableEntries = products
                            .get(folderIndex).getEditableEntries();
                    Map<String, AbstractFormatTab> formatTabMap = formatTabList
                            .get(folderIndex);
                    Combo segmentsCombo = segmentsComboList.get(folderIndex);
                    ProductGenerationDialogUtility.updateTextAreas(
                            segmentsCombo, comboSelection, editableEntries,
                            formatTabMap, textAreaMap);
                } else {
                    for (Text text : textAreaMap.values()) {
                        text.setText("");
                    }
                }
            }

        });

        if (formattedKeyComboList.isEmpty()
                || folderIndex >= formattedKeyComboList.size()) {
            formattedKeyComboList.add(formattedKeyCombo);
        } else {
            formattedKeyComboList.set(folderIndex, formattedKeyCombo);
        }

        // set up the text areas
        Composite pieceComp = new Composite(comp, SWT.NONE);
        setLayoutInfo(pieceComp, 1, false, SWT.FILL, SWT.DEFAULT, true, false,
                null);
        GridData gridData = new GridData(SWT.FILL, SWT.NONE, true, false);
        gridData.heightHint = 100;
        Map<String, Text> textAreaMap = new HashMap<String, Text>();
        for (final String format : editableEntries.keySet()) {
            Label label = new Label(pieceComp, SWT.FILL);
            label.setText(format + ":");
            Text text = new Text(pieceComp, SWT.MULTI | SWT.BORDER
                    | SWT.V_SCROLL);
            text.setLayoutData(gridData);
            text.addFocusListener(new FocusListener() {

                @Override
                public void focusLost(FocusEvent e) {

                }

                @Override
                public void focusGained(FocusEvent e) {
                    int folderIndex = folder.getSelectionIndex();
                    Map<String, Text> textAreaMap = textAreasList
                            .get(folderIndex);
                    CTabFolder formatFolder = formatFolderList.get(folderIndex);
                    ProductGenerationDialogUtility.selectFormatTab(format,
                            formatFolder,
                            formatTabList.get(folder.getSelectionIndex()),
                            textAreaMap);
                }
            });
            textAreaMap.put(format, text);
        }

        if (textAreasList.isEmpty() || folderIndex >= textAreasList.size()) {
            textAreasList.add(textAreaMap);
        } else {
            textAreasList.set(folderIndex, textAreaMap);
        }
    }

    /**
     * Parses the "editable" part of the key out to give the "pretty" text
     * 
     * @param key
     * @return
     */
    public static String parseEditable(String key) {
        String returnKey = key;
        if (key.contains(ProductGenerationDialogUtility.EDITABLE.substring(1))) {
            returnKey = key.substring(0, key
                    .indexOf(ProductGenerationDialogUtility.EDITABLE
                            .substring(1)) - 1);
        }
        return returnKey;
    }

    /**
     * Creates the reformat listener that will update the UI to notify the user
     * that formatting is/has happened.
     * 
     * @return
     */
    private Listener createGenerateListener() {
        return new Listener() {

            @Override
            public void handleEvent(Event event) {

                final IPythonJobListener<GeneratedProductList> listener = new IPythonJobListener<GeneratedProductList>() {

                    @Override
                    public void jobFinished(
                            final GeneratedProductList productList) {

                        VizApp.runAsync(new Runnable() {
                            public void run() {

                                GeneratorInfo generatorInfo = generatorInformationMap
                                        .get(productList.getProductInfo());
                                int index = generatorInfo.getStart();
                                for (IGeneratedProduct product : productList) {
                                    products.set(index, product);
                                    Set<String> formats = product.getEntries()
                                            .keySet();
                                    Map<String, AbstractFormatTab> formatTabMap = formatTabList
                                            .get(index);
                                    for (String format : formats) {
                                        AbstractFormatTab tab = formatTabMap
                                                .get(format);

                                        if (tab instanceof TextFormatTab) {
                                            TextFormatTab textTab = (TextFormatTab) tab;
                                            StyledText styledText = textTab
                                                    .getText();

                                            String finalProduct = ((ITextProduct) product)
                                                    .getText(format);
                                            styledText.setText(finalProduct);
                                        }
                                    }
                                    index++;
                                }

                                // reset the Formatted tab
                                for (Combo formattedKeyCombo : formattedKeyComboList) {
                                    formattedKeyCombo.select(0);
                                    formattedKeyCombo.notifyListeners(
                                            SWT.Selection, new Event());
                                }
                            };
                        });
                    }

                    @Override
                    public void jobFailed(Throwable e) {
                        handler.error("Unable to run product generation", e);

                    }
                };

                ProductGeneration generation = new ProductGeneration();
                for (GeneratorInfo generatorInfo : generatorInformationMap
                        .values()) {
                    List<LinkedHashMap<String, Serializable>> dataList = new ArrayList<LinkedHashMap<String, Serializable>>();
                    for (int i = generatorInfo.getStart(); i < generatorInfo
                            .getSize(); i++) {
                        dataList.add(products.get(i).getData());
                    }
                    List<String> formats = new ArrayList<String>(products
                            .get(generatorInfo.getStart()).getEntries()
                            .keySet());
                    generation.update(generatorInfo.getProductGeneratorName(),
                            dataList,
                            formats.toArray(new String[formats.size()]),
                            listener);
                }

            }
        };
    }

    /**
     * TODO Products should be set before the dialog pops up, or maybe we should
     * pop up and then run the formatting? That might be more inline with
     * rerunning it after we make changes.
     * 
     * @param products
     */
    public void setProducts(GeneratedProductList products) {
        this.products = products;
        decodedSegments = new ArrayList<List<Segment>>();
        for (IGeneratedProduct product : products) {
            List<Segment> decodedSegment = ProductGenerationDialogUtility
                    .separateSegments(product.getData());
            decodedSegments.add(decodedSegment);
        }
    }

    public void setGeneratorInformationMap(
            Map<String, GeneratorInfo> generatorInformationMap) {
        this.generatorInformationMap = generatorInformationMap;
    }

    /**
     * @return the generated products
     */
    public GeneratedProductList getGeneratedProducts() {
        return products;
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
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

import gov.noaa.gsd.viz.megawidgets.ICurrentTimeProvider;
import gov.noaa.gsd.viz.megawidgets.IParametersEditorListener;
import gov.noaa.gsd.viz.megawidgets.MegawidgetException;
import gov.noaa.gsd.viz.megawidgets.ParametersEditorFactory;

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
import org.eclipse.swt.custom.ScrolledComposite;
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
import org.eclipse.swt.widgets.Text;

import com.raytheon.uf.common.hazards.productgen.GeneratedProductList;
import com.raytheon.uf.common.hazards.productgen.IGeneratedProduct;
import com.raytheon.uf.common.hazards.productgen.ProductGeneration;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.SimulatedTime;
import com.raytheon.uf.viz.productgen.dialog.data.AbstractProductGeneratorData;
import com.raytheon.uf.viz.productgen.dialog.listener.GenerateListener;

/**
 * If there are any editable keys in the data dictionary returned by the product
 * generators then this composite will be used. This is the editor for the
 * unformatted and formatted data returned by the product generators and the
 * formatters.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 11, 2014            jsanchez     Initial creation
 * 
 * </pre>
 * 
 * @author jsanchez
 * @version 1.0
 */

public class DataEditor {

    private static final IUFStatusHandler handler = UFStatus
            .getHandler(DataEditor.class);

    private static final String GENERATE_LABEL = "Generate";

    private static final String REVERT_LABEL = "Revert";

    private static final String SEGMENT_LABEL = "Segments (%d):";

    private static final String UNFORMATTED_LABEL = "Unformatted";

    private static final String FORMATTED_LABEL = "Formatted";

    private static final String FORMATTED_COMBO_INSTRUCTION = "Select a key";

    private static final int WIDTH = 250;

    private static final int HEIGHT = 300;

    private Button revertButton;

    private final ICurrentTimeProvider currentTimeProvider = new ICurrentTimeProvider() {
        @Override
        public long getCurrentTime() {
            return SimulatedTime.getSystemTime().getMillis();
        }
    };

    /*
     * The formatListener that handles all the work in product generation, as
     * well as sets up the progress bar and calls the method to re-populate the
     * formats
     */
    public final Listener formatListener = createGenerateListener();

    private ProductGenerationDialog dialog;

    private boolean unformattedTabSelected = true;

    private GenerateListener generateListener;

    public DataEditor(ProductGenerationDialog dialog, Composite composite,
            int folderIndex) {
        this.dialog = dialog;
        generateListener = new GenerateListener(dialog);
        createDataEditor(composite, folderIndex);
    }

    /*
     * Creates the left side editor for managing the dictionary data and
     * individual formatted data.
     */
    private void createDataEditor(Composite comp, final int folderIndex) {
        final List<AbstractProductGeneratorData> decodedDataList = dialog
                .getDecodedDataList(folderIndex);
        Label segmentsLabel = new Label(comp, SWT.NONE);
        segmentsLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
                false, 1, 1));
        final Combo segmentsCombo = new Combo(comp, SWT.READ_ONLY);
        for (AbstractProductGeneratorData dataItem : decodedDataList) {
            segmentsCombo.add(dataItem.getDescriptionName());
        }
        segmentsLabel.setText(String.format(SEGMENT_LABEL,
                segmentsCombo.getItemCount()));
        segmentsCombo.select(0);
        segmentsCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
                false, 1, 1));

        CTabFolder dataFolder = new CTabFolder(comp, SWT.BORDER);
        dialog.setLayoutInfo(dataFolder, 1, false, SWT.FILL, SWT.FILL, true,
                true, new Point(WIDTH, HEIGHT));

        // set up the 'Unformatted' scroll composite
        final ScrolledComposite unformattedScroller = new ScrolledComposite(
                dataFolder, SWT.BORDER | SWT.V_SCROLL);
        dialog.setLayoutInfo(unformattedScroller, 1, false, SWT.FILL, SWT.FILL,
                true, true, new Point(WIDTH, HEIGHT));

        final Composite unformattedComp = new Composite(unformattedScroller,
                SWT.NONE);
        dialog.setLayoutInfo(unformattedComp, 1, false, SWT.FILL, SWT.FILL,
                true, true, null);
        addUnformattedEntries(unformattedComp, decodedDataList.get(0));

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
        final ScrolledComposite formattedScroller = new ScrolledComposite(
                dataFolder, SWT.BORDER | SWT.V_SCROLL);
        dialog.setLayoutInfo(formattedScroller, 1, false, SWT.FILL, SWT.FILL,
                true, true, new Point(WIDTH, HEIGHT));
        final Composite formattedComp = new Composite(formattedScroller,
                SWT.NONE);
        dialog.setLayoutInfo(formattedComp, 1, false, SWT.FILL, SWT.FILL, true,
                true, null);
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
        GridLayout layout = new GridLayout(2, false);
        GridData data = new GridData(SWT.CENTER, SWT.CENTER, true, false);
        buttonComp.setLayout(layout);
        buttonComp.setLayoutData(data);
        createGenerateButton(buttonComp);
        createRevertButton(buttonComp);

        segmentsCombo.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                // Clear out composite's contents
                for (Control control : unformattedComp.getChildren()) {
                    control.dispose();
                }

                for (Control control : formattedComp.getChildren()) {
                    control.dispose();
                }
                addUnformattedEntries(unformattedComp,
                        decodedDataList.get(segmentsCombo.getSelectionIndex()));
                dialog.setLayoutInfo(unformattedComp, 1, false, SWT.FILL,
                        SWT.FILL, true, true, null);
                unformattedScroller.setExpandHorizontal(true);
                unformattedScroller.setExpandVertical(true);
                unformattedScroller.setContent(unformattedComp);
                unformattedScroller.setMinSize(unformattedComp.computeSize(
                        SWT.DEFAULT, SWT.DEFAULT));
                unformattedScroller.layout();

                addFormattedEntries(formattedComp, folderIndex);
                dialog.setLayoutInfo(formattedComp, 1, false, SWT.FILL,
                        SWT.FILL, true, true, null);
                formattedScroller.setExpandHorizontal(true);
                formattedScroller.setExpandVertical(true);
                formattedScroller.setContent(formattedComp);
                formattedScroller.setMinSize(formattedComp.computeSize(
                        SWT.DEFAULT, SWT.DEFAULT));
                formattedScroller.layout();

                ProductGenerationDialogUtility.selectSegmentInTabs(
                        decodedDataList.get(segmentsCombo.getSelectionIndex()),
                        dialog.getCurrentFormatTabMap());
            }

        });
        dialog.addSegmentCombo(segmentsCombo);

        dataFolder.setSelection(0);
        dataFolder.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                if (e.item == unformattedTabItem) {
                    // removes the highlighting
                    ProductGenerationDialogUtility.clearHighlighting(dialog
                            .getCurrentFormatTabMap());
                    unformattedTabSelected = true;
                } else {
                    dialog.getCurrentFormattedKeyCombo().notifyListeners(
                            SWT.Selection, new Event());
                    unformattedTabSelected = false;
                }
            }
        });

    }

    /**
     * Creates the 'Unformatted' tab with the dictionary from the generators
     */
    private void addUnformattedEntries(Composite comp,
            final AbstractProductGeneratorData segment) {

        List<String> labels = new ArrayList<String>(segment.getEditableKeys());
        Map<String, Object> parametersForLabels = new HashMap<String, Object>();
        for (String key : segment.getEditableKeys()) {
            parametersForLabels.put(key, segment.getValue(key));
        }

        try {
            // Use factory to create megawidgets
            ParametersEditorFactory factory = new ParametersEditorFactory();
            factory.buildParametersEditor(comp, labels, parametersForLabels,
                    System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1L),
                    System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1L),
                    currentTimeProvider, new IParametersEditorListener() {
                        @Override
                        public void parameterValueChanged(String key,
                                Object value) {
                            Combo segmentsCombo = dialog
                                    .getCurrentSegmentCombo();
                            int segmentNumber = segmentsCombo
                                    .getSelectionIndex();

                            ArrayList<String> path = new ArrayList<String>(
                                    segment.getPath(key));
                            path.add(key
                                    + ProductGenerationDialogUtility.EDITABLE);
                            segment.modify(key, (Serializable) value);
                            ProductGenerationDialogUtility.updateData(path,
                                    dialog.getCurrentGeneratedProduct()
                                            .getData(), segmentNumber,
                                    (Serializable) value);
                            ProductGenerationDialogUtility.updateData(path,
                                    segment.getData(), segmentNumber,
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
        final IGeneratedProduct generatedProduct = dialog
                .getGeneratedProduct(folderIndex);
        final Combo formattedKeyCombo = new Combo(comp, SWT.READ_ONLY);

        formattedKeyCombo.add(FORMATTED_COMBO_INSTRUCTION);
        Combo currentSegmentCombo = dialog.getCurrentSegmentCombo();
        if (currentSegmentCombo != null) {
            String format = dialog.getCurrentFormatFolder().getSelection()
                    .getText();
            int segmentIndex = currentSegmentCombo.getSelectionIndex();
            final List<LinkedHashMap<String, Serializable>> editableEntries = generatedProduct
                    .getEditableEntries().get(format);

            // gather all possible interior keys
            Set<String> uniqueKeys = new HashSet<String>();

            for (Entry<String, Serializable> entry : editableEntries.get(
                    segmentIndex).entrySet()) {
                if (entry.getValue() != null) {
                    String keyname = entry.getKey().replace(
                            ProductGenerationDialogUtility.EDITABLE, "");
                    if (uniqueKeys.contains(keyname) == false) {
                        uniqueKeys.add(keyname);
                    }
                }
            }

            for (String uniqueKey : uniqueKeys) {
                formattedKeyCombo.add(uniqueKey);
            }
        }
        formattedKeyCombo.select(0);
        formattedKeyCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
                true, false, 1, 1));

        formattedKeyCombo.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                int index = formattedKeyCombo.getSelectionIndex();
                Map<String, Text> textAreaMap = dialog.getCurrentTextAreas();
                if (index > 0) {
                    dialog.setNotHighlighting(false);
                    String formatComboSelection = formattedKeyCombo
                            .getItem(index);
                    IGeneratedProduct product = dialog
                            .getCurrentGeneratedProduct();
                    int segmentIndex = dialog.getCurrentSegmentCombo()
                            .getSelectionIndex();
                    ProductGenerationDialogUtility.updateTextAreas(
                            segmentIndex, formatComboSelection, product,
                            dialog.getCurrentFormatTabMap(), textAreaMap);
                    dialog.setNotHighlighting(true);
                } else {
                    for (Text text : textAreaMap.values()) {
                        text.setText("");
                    }
                }
            }

        });

        dialog.updateFormattedKeyComboList(folderIndex, formattedKeyCombo);

        // set up the text areas
        Composite pieceComp = new Composite(comp, SWT.NONE);
        dialog.setLayoutInfo(pieceComp, 1, false, SWT.FILL, SWT.DEFAULT, true,
                false, null);
        GridData gridData = new GridData(SWT.FILL, SWT.NONE, true, false);
        gridData.heightHint = 75;
        Map<String, Text> textAreaMap = new HashMap<String, Text>();

        for (final String format : generatedProduct.getEntries().keySet()) {
            int size = generatedProduct.getEntry(format).size();
            for (int counter = 0; counter < size; counter++) {
                final String tabLabel = generatedProduct.getEntry(format)
                        .size() > 1 ? String.format(
                        ProductGenerationDialog.TAB_LABEL_FORMAT, format,
                        counter) : format;

                Label label = new Label(pieceComp, SWT.FILL);
                label.setText(tabLabel + ":");
                Text text = new Text(pieceComp, SWT.MULTI | SWT.BORDER
                        | SWT.V_SCROLL);
                text.setLayoutData(gridData);
                text.addFocusListener(new FocusListener() {

                    @Override
                    public void focusLost(FocusEvent e) {

                    }

                    @Override
                    public void focusGained(FocusEvent e) {
                        CTabFolder formatFolder = dialog
                                .getCurrentFormatFolder();
                        ProductGenerationDialogUtility.selectFormatTab(
                                tabLabel, formatFolder,
                                dialog.getCurrentFormatTabMap(),
                                dialog.getCurrentTextAreas());
                    }
                });
                textAreaMap.put(tabLabel, text);
                if (size == 1) {
                    break;
                }
            }
        }

        dialog.updateTextAreasList(folderIndex, textAreaMap);
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
        dialog.setButtonGridData(generateButton);
        generateButton.setEnabled(true);
        generateButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                dialog.getProgressBar().setVisible(true);
                try {
                    /*
                     * TODO Generating products based on 'formatted data' is not
                     * fully implemented yet. Only the tab is available for
                     * viewing
                     */
                    if (unformattedTabSelected) {
                        formatListener.handleEvent(null);
                    } else {
                        Combo formattedKeyCombo = dialog
                                .getCurrentFormattedKeyCombo();
                        int index = formattedKeyCombo.getSelectionIndex();
                        if (index > 0) {
                            dialog.setNotHighlighting(false);
                            int selectionIndex = dialog
                                    .getCurrentSegmentCombo()
                                    .getSelectionIndex();
                            String formatComboSelection = formattedKeyCombo
                                    .getItem(index);
                            IGeneratedProduct product = dialog
                                    .getCurrentGeneratedProduct();
                            ProductGenerationDialogUtility
                                    .updateEditableEntries(selectionIndex,
                                            formatComboSelection,
                                            product.getEditableEntries(),
                                            dialog.getCurrentFormatTabMap(),
                                            dialog.getCurrentTextAreas());
                            dialog.setNotHighlighting(true);
                        }
                    }
                } finally {
                    dialog.getProgressBar().setVisible(false);
                }

            }
        });

    }

    private void createRevertButton(Composite buttonComp) {
        revertButton = new Button(buttonComp, SWT.PUSH);
        revertButton.setText(REVERT_LABEL);
        dialog.setButtonGridData(revertButton);
        revertButton.setEnabled(true);
        revertButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                // TODO Reverts the last modification and calls generate again
                revertButton.setEnabled(false);
            }
        });
        revertButton.setEnabled(false);
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
                ProductGeneration generation = new ProductGeneration();
                for (GeneratedProductList products : dialog
                        .getGeneratedProductListStorage()) {
                    List<LinkedHashMap<String, Serializable>> dataList = new ArrayList<LinkedHashMap<String, Serializable>>();
                    for (IGeneratedProduct product : products) {
                        dataList.add(product.getData());
                    }
                    List<String> formats = new ArrayList<String>(products
                            .get(0).getEntries().keySet());
                    generation.update(products.getProductInfo(), dataList,
                            formats.toArray(new String[formats.size()]),
                            generateListener);
                }

            }
        };
    }

}

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

import gov.noaa.gsd.common.utilities.ICurrentTimeProvider;
import gov.noaa.gsd.viz.megawidgets.IControlSpecifier;
import gov.noaa.gsd.viz.megawidgets.IParametersEditorListener;
import gov.noaa.gsd.viz.megawidgets.MegawidgetException;
import gov.noaa.gsd.viz.megawidgets.MegawidgetManager;
import gov.noaa.gsd.viz.megawidgets.MegawidgetStateException;
import gov.noaa.gsd.viz.megawidgets.ParametersEditorFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.ScrolledComposite;
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

import com.raytheon.uf.common.hazards.productgen.GeneratedProductList;
import com.raytheon.uf.common.hazards.productgen.IGeneratedProduct;
import com.raytheon.uf.common.hazards.productgen.KeyInfo;
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
 * Apr 20, 2014  2336      Chris.Golden Changed to use improved version of
 *                                      ParametersEditorFactory that allows
 *                                      specification of keys as KeyInfo
 *                                      objects instead of requiring keys
 *                                      to be String instances.
 * </pre>
 * 
 * @author jsanchez
 * @version 1.0
 */

public class DataEditor {

    private static final IUFStatusHandler handler = UFStatus
            .getHandler(DataEditor.class);

    private static final String GENERATE_LABEL = "Generate";

    private static final String SAVE_LABEL = "Save";

    private static final String REVERT_LABEL = "Revert";

    private static final String UNFORMATTED_LABEL = "Unformatted";

    private static final int WIDTH = 250;

    private static final int HEIGHT = 300;

    private Button revertButton;

    private Button saveButton;

    private MegawidgetManager manager;

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

    private final ProductGenerationDialog dialog;

    private final GenerateListener generateListener;

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
        segmentsLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true,
                false, 1, 1));
        final Combo segmentsCombo = new Combo(comp, SWT.READ_ONLY);
        segmentsCombo.add(String.format("---- %d Segment(s) Available ----",
                decodedDataList.size() - 1));
        for (AbstractProductGeneratorData dataItem : decodedDataList) {
            segmentsCombo.add(dataItem.getDescriptionName());
        }

        segmentsCombo.select(0);
        dialog.setLayoutInfo(segmentsCombo, 1, false, SWT.FILL, SWT.FILL, true,
                true, new Point(WIDTH, 20));
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

        // set up the generate button
        Composite buttonComp = new Composite(comp, SWT.NONE);
        GridLayout layout = new GridLayout(3, false);
        GridData data = new GridData(SWT.CENTER, SWT.CENTER, true, false);
        buttonComp.setLayout(layout);
        buttonComp.setLayoutData(data);
        createGenerateButton(buttonComp);
        createSaveButton(buttonComp);
        createRevertButton(buttonComp);

        segmentsCombo.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                // Clear out composite's contents
                for (Control control : unformattedComp.getChildren()) {
                    control.dispose();
                }

                int selectionIndex = segmentsCombo.getSelectionIndex() - 1;
                if (selectionIndex > -1) {
                    addUnformattedEntries(unformattedComp,
                            decodedDataList.get(selectionIndex));
                    dialog.setLayoutInfo(unformattedComp, 1, false, SWT.FILL,
                            SWT.FILL, true, true, null);
                    unformattedScroller.setExpandHorizontal(true);
                    unformattedScroller.setExpandVertical(true);
                    unformattedScroller.setContent(unformattedComp);
                    unformattedScroller.setMinSize(unformattedComp.computeSize(
                            SWT.DEFAULT, SWT.DEFAULT));
                    unformattedScroller.layout();

                    if (e.data == null || Boolean.valueOf((boolean) e.data)) {
                        ProductGenerationDialogUtility.selectSegmentInTabs(
                                decodedDataList.get(selectionIndex),
                                dialog.getCurrentFormatTabMap());
                    }
                } else if (selectionIndex == -1) {
                    ProductGenerationDialogUtility.clearHighlighting(dialog
                            .getCurrentFormatTabMap());
                }
            }

        });
        dialog.addSegmentCombo(segmentsCombo);

        dataFolder.setSelection(0);
        dataFolder.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                // removes the highlighting
                ProductGenerationDialogUtility.clearHighlighting(dialog
                        .getCurrentFormatTabMap());
            }
        });

    }

    /**
     * Creates the 'Unformatted' tab with the dictionary from the generators
     */
    private void addUnformattedEntries(Composite comp,
            final AbstractProductGeneratorData segment) {

        if (segment.getEditableKeys().isEmpty()) {
            Label noEditableFieldsLabel = new Label(comp, SWT.CENTER);
            noEditableFieldsLabel
                    .setText("No editable fields for this segment");
            return;
        }

        List<KeyInfo> keyInfos = new ArrayList<>();
        Map<KeyInfo, Object> valuesForKeyInfos = new HashMap<>();
        for (KeyInfo key : segment.getEditableKeys()) {
            keyInfos.add(key);
            valuesForKeyInfos.put(key, segment.getValue(key));
        }

        try {
            // Use factory to create megawidgets
            ParametersEditorFactory factory = new ParametersEditorFactory();
            manager = factory.buildParametersEditor(comp, keyInfos,
                    valuesForKeyInfos, System.currentTimeMillis()
                            - TimeUnit.DAYS.toMillis(1L),
                    System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1L),
                    currentTimeProvider,
                    new IParametersEditorListener<KeyInfo>() {
                        @Override
                        public void parameterValueChanged(KeyInfo keyInfo,
                                Object value) {
                            Combo segmentsCombo = dialog
                                    .getCurrentSegmentCombo();
                            int segmentNumber = segmentsCombo
                                    .getSelectionIndex() - 1;
                            List<KeyInfo> path = new ArrayList<>(segment
                                    .getPath(keyInfo));
                            path.add(keyInfo);
                            ProductGenerationDialogUtility.updateData(path,
                                    dialog.getCurrentGeneratedProduct()
                                            .getData(), segmentNumber,
                                    (Serializable) value);
                            ProductGenerationDialogUtility.updateData(path,
                                    segment.getData(), segmentNumber,
                                    (Serializable) value);
                            segment.modify(keyInfo, (Serializable) value);
                        }
                    });

            // Disables those that 'displayable'
            if (segment.getDisplayableKeys().isEmpty() == false) {
                Map<String, Map<String, Object>> mutablePropertiesMap = manager
                        .getMutableProperties();
                Boolean falseObject = new Boolean(false);
                for (KeyInfo displayableKey : segment.getDisplayableKeys()) {
                    Map<String, Object> properties = mutablePropertiesMap
                            .get(displayableKey.getLabel());
                    if (properties != null) {
                        properties.put(IControlSpecifier.MEGAWIDGET_EDITABLE,
                                falseObject);
                    }
                }
                manager.setMutableProperties(mutablePropertiesMap);
            }
        } catch (MegawidgetException e) {
            handler.error("Error creating megawidets", e);
        }
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

                formatListener.handleEvent(null);
                revertButton.setEnabled(true);
                saveButton.setEnabled(true);

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
                Combo segmentsCombo = dialog.getCurrentSegmentCombo();
                int segmentNumber = segmentsCombo.getSelectionIndex() - 1;
                AbstractProductGeneratorData segment = dialog
                        .getDecodedDataList(dialog.getFolderSelectionIndex())
                        .get(segmentNumber);
                Map<String, Object> state = manager.getState();
                for (KeyInfo key : segment.getEditableKeys()) {
                    Serializable value = segment.revertValue(key);
                    if (value != null) {
                        List<KeyInfo> path = new ArrayList<>(segment
                                .getPath(key));
                        path.add(key);
                        ProductGenerationDialogUtility.updateData(path, dialog
                                .getCurrentGeneratedProduct().getData(),
                                segmentNumber, value);
                        ProductGenerationDialogUtility.updateData(path,
                                segment.getData(), segmentNumber, value);
                        state.put(key.getLabel(), value);
                    }
                }
                try {
                    manager.setState(state);
                } catch (MegawidgetStateException exception) {
                    handler.error("Error trying to reset megawidget state ",
                            exception);
                }

                formatListener.handleEvent(null);
                revertButton.setEnabled(false);
                saveButton.setEnabled(false);
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
                    List<LinkedHashMap<KeyInfo, Serializable>> dataList = new ArrayList<>();
                    for (IGeneratedProduct product : products) {
                        dataList.add(product.getData());
                    }
                    List<String> formats = new ArrayList<>(products.get(0)
                            .getEntries().keySet());
                    generation.update(products.getProductInfo(), dataList,
                            formats.toArray(new String[formats.size()]),
                            generateListener);
                }

            }
        };
    }

    /**
     * The save button will save the edits made to the database.
     * 
     * @param buttonComp
     */
    private void createSaveButton(Composite buttonComp) {
        saveButton = new Button(buttonComp, SWT.PUSH);
        saveButton.setText(SAVE_LABEL);
        dialog.setButtonGridData(saveButton);
        saveButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                save();
                saveButton.setEnabled(false);
            }
        });
        saveButton.setEnabled(false);
    }

    /**
     * Saves any modifications made to the unformatted data.
     */
    public void save() {
        int offset = 0;
        for (GeneratedProductList products : dialog
                .getGeneratedProductListStorage()) {
            int index = 0;
            for (@SuppressWarnings("unused")
            IGeneratedProduct product : products) {
                ProductGenerationDialogUtility.save(dialog
                        .getDecodedDataList(offset + index));
                index++;
            }

            offset += products.size();
        }
        revertButton.setEnabled(false);
    }
}

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

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;

import com.raytheon.uf.common.dataplugin.events.IEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardEventUtilities;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.hazards.configuration.types.HatchingStyle;
import com.raytheon.uf.common.hazards.configuration.types.HazardTypeEntry;
import com.raytheon.uf.common.hazards.configuration.types.HazardTypes;
import com.raytheon.uf.common.hazards.productgen.EditableEntryMap;
import com.raytheon.uf.common.hazards.productgen.IGeneratedProduct;
import com.raytheon.uf.common.hazards.productgen.KeyInfo;
import com.raytheon.uf.common.hazards.productgen.editable.ProductTextUtil;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.SimulatedTime;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.common.util.Pair;
import com.raytheon.uf.viz.core.VizApp;

import gov.noaa.gsd.viz.megawidgets.IControlSpecifier;
import gov.noaa.gsd.viz.megawidgets.MegawidgetException;
import gov.noaa.gsd.viz.megawidgets.MegawidgetManager;
import gov.noaa.gsd.viz.megawidgets.MegawidgetStateException;

/**
 * If there are any editable keys in the data dictionary returned by the product
 * generators then this composite will be used.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Jun 13, 2014 3519       jsanchez     Initial creation
 * Jun 24, 2014 4010       Chris.Golden Changed to work with parameters
 *                                      editor changes.
 * Jun 30, 2014 3512       Chris.Golden Changed to work with more
 *                                      parameters editor changes.
 * 01/15/2015   5109       bphillip     Refactored/Renamed
 * 03/11/2015   6889       bphillip     Modifications to allow more than one undo action in the Product Editor
 * 03/19/2015   7108       Robert.Blum  Rename Raw Data to Hazard Data Editor.
 * 03/23/2015   7165       Robert.Blum  Modifications to allow adding * to editor tab and product tab
 *                                      labels when there are unsaved changes.
 * 04/16/2015   7579       Robert.Blum  Changes for amended Product Editor design.
 * 05/07/2015   6979       Robert.Blum  Removed automatic saving of changed/reverted values.
 *                                      Also save/undo buttons are always enabled due to issues
 *                                      being able to correctly enable them via the megawidgets.
 * 5/14/2015    7376       Robert.Blum  Changed requiredFieldsCompleted() to looked at the value
 *                                      instead of modifiedValue. Updating issueAll button when
 *                                      changes are made.
 * 07/28/2015   9687       Robert.Blum  Added Save/Undo buttons from parent class, since they are
 *                                      unique for this subclass. Also Added new button to toggle
 *                                      the labels on the megawidget fields.
 * 07/28/2015   9633       Robert.Blum  Fixed middle mouse scroll issue on product editor.
 * 07/29/2015   9686       Robert.Blum  Adjustments to remove dead space at the bottom of the vertical
 *                                      scrollbar when labels are removed from the product editor.
 * 07/30/2015   9681       Robert.Blum  Changed to use new abstract product dialog class.
 * 08/19/2015   9639       mduff        Make undo message modal to this dialog.
 * 08/26/2015   8836       Chris.Cody   Changes for Unique (alpha-numeric) Event ID values
 * 08/31/2015   9617       Chris.Golden Modified to use local copy of parameters editor factory.
 * 09/11/2015   9508       Robert.Blum  Save MessageBox now notifies of missing required fields.
 * 06/08/2016   9620       Robert.Blum  Added controls to change the product expiration time.
 * 06/09/2016   9620       Robert.Blum  Fixed issue with rapidly changing the product purge hours.
 * 06/21/2016   9620       Robert.Blum  Accounting for arrow keys with expiration spinner.
 * 06/22/2016  19925      Thomas.Gurney Make "Toggle Labels" always checked by default
 * 09/29/2016  22602       Roger.Ferrel Disable mouse wheel on expires spinner.
 * 10/18/2016  22412       Roger.Ferrel When overview changes update the staging value entries.
 *                                      Set the overview synopsis to the value in product.
 * 11/08/2016  22509       bkowal       Required fields should always be indicated.
 * 11/10/2016  22119       Kevin.Bisanz Add siteId so that saved/issued changes can be tagged with it.
 * 12/06/2016  26855       Chris.Golden Removed explicit use of scrollable composite, since the
 *                                      megawidgets can be wrapped within a scrollable Composite
 *                                      megawidget instead, which will handle Label-wrapping behavior
 *                                      more correctly.
 * </pre>
 * 
 * @author jsanchez
 * @version 1.0
 */

public class ProductDataEditor extends AbstractDataEditor {

    /** The log handler */
    private static final IUFStatusHandler handler = UFStatus
            .getHandler(ProductDataEditor.class);

    /** Label used for the tab */
    private static final String TAB_LABEL = "Hazard Data Editor";

    /** Label for the Save button on the editor tab */
    private static final String SAVE_BUTTON_LABEL = "Save";

    /** Label for the Undo button on the editor tab */
    private static final String UNDO_BUTTON_LABEL = "Undo";

    /* Prefix for key info overview synopsis names. */
    private static final String OVERVIEW_KEY_PREFIX = "overviewSynopsis_";

    /** Label for the Undo button on the editor tab */
    private static final String TOGGLE_LABELS_BUTTON_LABEL = "Toggle Labels";

    /* Number of buttons */
    private final int BUTTON_COUNT = 3;

    /**
     * Date & time formatter.
     */
    private final SimpleDateFormat expireLabelFmt = new SimpleDateFormat(
            "HH:mm'Z' dd-MMM-yy");

    private boolean displayExpirationControls;

    /*
     * Expiration Date
     */
    private Date expireDate;

    /**
     * Hours spinner.
     */
    private Spinner hoursSpnr;

    private int lastSpnrValue;

    /**
     * Date & time label.
     */
    private Label dateTimeLbl;

    /** The toggle button widget */
    private Button toggleLabelsButton;

    /** The save button widget */
    private Button saveButton;

    /** The undo button widget */
    private Button undoButton;

    /** The parent composite for the megawidgets **/
    private Composite parentComposite;

    /**
     * The MegawidgetManager responsible for generating the GUI components for
     * this product
     */
    private MegawidgetManager manager;

    /**
     * The history of modifications made to editable data.
     */
    private final LinkedList<Pair<KeyInfo, EditableKeyInfo>> modificationHistory = new LinkedList<>();

    /** The current site ID */
    private String siteId;

    /**
     * Creates a new ProductDataEditor
     * 
     * @param siteId
     *            The site ID
     * @param productDialog
     *            The parent product dialog creating this
     * @param product
     *            The generated product associated with this editor
     * @param parent
     *            The CTabFolder parent object
     * @param style
     *            SWT style flags
     * @param hazardTypes
     *            Hazard types configuration information.
     */
    protected ProductDataEditor(String siteId,
            AbstractProductDialog productDialog, CTabItem productTab,
            IGeneratedProduct product, CTabFolder parent, int style,
            HazardTypes hazardTypes) {
        super(productDialog, productTab, product, parent, style);
        this.siteId = siteId;
        displayExpirationControls = true;
        for (IEvent event : product.getEventSet()) {
            IHazardEvent hazard = (IHazardEvent) event;
            HazardTypeEntry hazardTypeEntry = hazardTypes
                    .get(HazardEventUtilities.getHazardType(hazard));
            if ((hazardTypeEntry != null) && (hazardTypeEntry
                    .getHatchingStyle() == HatchingStyle.WARNGEN)) {
                displayExpirationControls = false;
                break;
            }
        }
    }

    /**
     * Creates the product specific GUI components using the megawidgets library
     * 
     * @param parent
     *            The parent composite to create the GUI components
     */
    @Override
    protected void initializeSubclass() {

        // Determine the editable keys present in this product
        editableKeys = new EditableKeys(product);

        // Set the tab label
        setText(TAB_LABEL);

        if (isDataEditable() == false) {
            handler.info(
                    "There are no editable fields. The data editor cannot be created.");
            return;
        }

        // Create the scroller composite and the layouts
        parentComposite = new Composite(editorPane, SWT.NONE);
        ProductEditorUtil.setLayoutInfo(parentComposite, 1, false, SWT.FILL,
                SWT.FILL, true, true, 500, 300);

        // Create the Megawidgets
        createMegawidgets();
    }

    private void createMegawidgets() {

        /*
         * If there is no parent composite, do nothing. This may be the case if
         * there are no editable fields, for example.
         */
        if (parentComposite == null) {
            return;
        }

        /* Dispose of any megawidget controls that may exist */
        Control[] controls = parentComposite.getChildren();
        if (controls.length != 0) {
            for (int i = 0; i < controls.length; i++) {
                controls[i].dispose();
            }
        }

        /*
         * Create the data structures necessary to pass to the megawidgets
         * library
         */
        List<KeyInfo> keyInfos = new ArrayList<>();
        Map<KeyInfo, Object> valuesForKeyInfos = new HashMap<>();
        for (KeyInfo key : editableKeys.getKeyInfos()) {
            keyInfos.add(key);
            if (key.getName().startsWith(OVERVIEW_KEY_PREFIX)
                    && product.getData().containsKey(key.getName())) {
                valuesForKeyInfos.put(key,
                        product.getData().get(key.getName()));
            } else {
                valuesForKeyInfos.put(key, editableKeys.getValue(key));
            }
        }

        try {
            /*
             * Use factory to create megawidgets
             */
            ProductParametersEditorFactory factory = new ProductParametersEditorFactory();
            manager = factory.buildParametersEditor(parentComposite, keyInfos,
                    valuesForKeyInfos,
                    System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1L),
                    System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1L),
                    ProductEditorUtil.currentTimeProvider,
                    new IProductParametersEditorListener() {
                        @Override
                        public void parameterValueChanged(KeyInfo keyInfo,
                                Object value) {
                            EditableKeyInfo editableKeyInfo = editableKeys
                                    .getEditableKeyInfo(keyInfo);
                            // Add a new entry in the undo queue
                            modificationHistory.add(
                                    new Pair<KeyInfo, EditableKeyInfo>(keyInfo,
                                            editableKeyInfo));

                            Serializable newValue = (Serializable) value;

                            // Update the map with the new value
                            updateKeyInfoValue(editableKeyInfo, keyInfo,
                                    newValue);

                            // Update the editable key value
                            editableKeyInfo.updateValue(newValue);

                            // Update the enabled state of the save and undo
                            // buttons
                            updateButtonState();

                            // Update the tab label
                            updateTabLabel();

                            // Regenerate with the updated data
                            productDialog.regenerate(keyInfo);

                            // Update IssueAll button state
                            productDialog.updateButtons();
                        }

                        @Override
                        public void parameterValuesChanged(
                                Map<KeyInfo, Object> valuesForLabels) {
                            for (Map.Entry<KeyInfo, Object> entry : valuesForLabels
                                    .entrySet()) {
                                parameterValueChanged(entry.getKey(),
                                        entry.getValue());
                            }
                        }

                        @Override
                        public void sizeChanged(KeyInfo parameter) {

                            /*
                             * No action; size changes of any children should be
                             * handled by scrollable wrapper megawidget.
                             */
                        }
                    });

            // Disables 'displayable'
            // TODO Check if the megawidget for a list handled editable
            List<KeyInfo> displayableKeys = editableKeys.getDisplayableKeys();
            if (displayableKeys.isEmpty() == false) {
                Map<String, Map<String, Object>> mutablePropertiesMap = manager
                        .getMutableProperties();

                for (KeyInfo displayableKey : displayableKeys) {
                    Map<String, Object> properties = mutablePropertiesMap
                            .get(displayableKey.toString());
                    if (properties != null) {
                        properties.put(IControlSpecifier.MEGAWIDGET_EDITABLE,
                                Boolean.FALSE);
                    }
                }

                manager.setMutableProperties(mutablePropertiesMap);
            }

        } catch (MegawidgetException e) {
            handler.error("Error creating megawidgets: " + e, e);
        }

        parentComposite.layout();
    }

    /**
     * Creates the Save, Undo, and toggle buttons
     * 
     * @param editorPane
     *            The parent composite
     */
    @Override
    protected void createEditorButtons(Composite editorPane) {

        // Initialize the composite to hold the buttons for the editor
        editorButtonPane = new Composite(editorPane, SWT.NONE);
        GridLayout buttonCompLayout = new GridLayout(getButtonCount(), false);
        buttonCompLayout.horizontalSpacing = HORIZONTAL_BUTTON_SPACING;
        GridData buttonCompData = new GridData(SWT.CENTER, SWT.CENTER, true,
                false);
        editorButtonPane.setLayout(buttonCompLayout);
        editorButtonPane.setLayoutData(buttonCompData);

        /*
         * Create the product expiration time composite
         */
        Composite expirationComp = new Composite(editorButtonPane, SWT.NONE);
        GridLayout gl = new GridLayout(4, false);
        GridData gd = new GridData(SWT.CENTER, SWT.DEFAULT, false, false);
        gd.horizontalSpan = 3;
        expirationComp.setLayoutData(gd);
        expirationComp.setLayout(gl);

        if (displayExpirationControls) {
            /*
             * Create the product expiration time controls
             */
            Label prodExpiresLbl = new Label(expirationComp, SWT.NONE);
            prodExpiresLbl.setText("Product expires in:");

            hoursSpnr = new Spinner(expirationComp, SWT.BORDER | SWT.READ_ONLY);
            Double purgeHours = getDefaultPurgeHours();
            purgeHours = purgeHours * 100;

            hoursSpnr.setValues(purgeHours.intValue(), 100, 4800, 2, 25, 25);
            lastSpnrValue = purgeHours.intValue();

            hoursSpnr.addMouseTrackListener(new MouseTrackListener() {
                @Override
                public void mouseEnter(MouseEvent e) {
                    // do nothing
                }

                @Override
                public void mouseExit(MouseEvent e) {
                    if (hoursSpnr.getSelection() != lastSpnrValue) {
                        lastSpnrValue = hoursSpnr.getSelection();
                        updateExpireTimeFromTimer(true);
                    }
                }

                @Override
                public void mouseHover(MouseEvent e) {
                    // do nothing
                }

            });

            hoursSpnr.addModifyListener(new ModifyListener() {
                @Override
                public void modifyText(ModifyEvent e) {
                    updateExpireTimeFromTimer(false);
                }
            });

            hoursSpnr.addKeyListener(new KeyListener() {

                @Override
                public void keyPressed(KeyEvent e) {
                    // do nothing
                }

                @Override
                public void keyReleased(KeyEvent e) {
                    if (e.keyCode == SWT.ARROW_UP
                            || e.keyCode == SWT.ARROW_DOWN) {
                        if (hoursSpnr.getSelection() != lastSpnrValue) {
                            lastSpnrValue = hoursSpnr.getSelection();
                            updateExpireTimeFromTimer(true);
                        }
                    }
                }

            });

            /*
             * DR#22602 - Disable the mouse wheel on hoursSpnr.
             */
            getDisplay().addFilter(SWT.MouseWheel, new Listener() {

                @Override
                public void handleEvent(Event event) {
                    if (event.widget.equals(hoursSpnr)) {
                        event.doit = false;
                    }
                }
            });

            Label atLbl = new Label(expirationComp, SWT.NONE);
            atLbl.setText(" At:");

            dateTimeLbl = new Label(expirationComp, SWT.NONE);
            updateExpireTime(false);
        }
        /*
         * Create the buttons for the editor tab
         */
        saveButton = new Button(editorButtonPane, SWT.PUSH);
        undoButton = new Button(editorButtonPane, SWT.PUSH);
        toggleLabelsButton = new Button(editorButtonPane, SWT.CHECK);

        /*
         * Configure Save button
         */
        saveButton.setText(SAVE_BUTTON_LABEL);
        ProductEditorUtil.setButtonGridData(saveButton);
        saveButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                boolean displayMB = false;
                StringBuilder messageSB = new StringBuilder();
                if (hasUnsavedChanges()) {
                    saveModifiedValues();
                    messageSB.append("Modified values saved.");
                } else {
                    displayMB = true;
                    // Update MessageBox message
                    messageSB.append("No modifications to save.");
                }

                // Check if all required fields are filled in
                if (requiredFieldsCompleted() == false) {
                    displayMB = true;
                    List<String> incompleteFields = getIncompleteRequiredFields();
                    messageSB.append(
                            "\n\nThe following required fields are incomplete:\n");
                    for (String field : incompleteFields) {
                        messageSB.append(field);
                        messageSB.append("\n");
                    }
                }

                // Display the messageBox is needed
                if (displayMB) {
                    Shell shell = getDisplay().getActiveShell();
                    MessageBox messageBox = new MessageBox(shell,
                            SWT.OK | SWT.ON_TOP | SWT.ICON_INFORMATION);
                    messageBox.setText("Save Status");
                    messageBox.setMessage(messageSB.toString());
                    messageBox.open();
                }

                updateTabLabel();
            }
        });

        // Editor save button always enabled.
        saveButton.setEnabled(true);

        /*
         * Configure Undo button
         */
        undoButton.setText(UNDO_BUTTON_LABEL);
        ProductEditorUtil.setButtonGridData(undoButton);
        undoButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                undoModification();
                updateButtonState();
                updateTabLabel();
            }
        });

        // Editor Undo button is always enabled.
        undoButton.setEnabled(true);

        /*
         * Configure toggle button
         */
        toggleLabelsButton.setText(TOGGLE_LABELS_BUTTON_LABEL);
        ProductEditorUtil.setButtonGridData(toggleLabelsButton);
        toggleLabelsButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                toggleLabels();
            }
        });

        // Editor toggle button always enabled.
        toggleLabelsButton.setEnabled(true);
        toggleLabelsButton.setSelection(true);
        toggleLabels();
    }

    private double getDefaultPurgeHours() {
        Map<String, Serializable> data = product.getData();
        if (data.containsKey(HazardConstants.PURGE_HOURS)) {
            Number purgeHoursObj = (Number) data
                    .get(HazardConstants.PURGE_HOURS);
            if (purgeHoursObj != null) {
                return purgeHoursObj.doubleValue();
            }
        }
        // Match GFE by defaulting to 12 hours
        return 12.0;
    }

    private void updateExpireTimeFromTimer(final boolean regenerate) {
        VizApp.runAsync(new Runnable() {
            @Override
            public void run() {
                updateExpireTime(regenerate);
            }
        });
    }

    private void updateExpireTime(boolean regenerate) {
        if (hoursSpnr.isDisposed() || dateTimeLbl.isDisposed()) {
            return;
        }

        int sel = hoursSpnr.getSelection();
        int hours = sel / 100;
        int minuteInc = (sel % 100) / 25;
        int purgeOffset = (hours * TimeUtil.MINUTES_PER_HOUR)
                + (minuteInc * 15); // minutes

        Date now = SimulatedTime.getSystemTime().getTime();
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        cal.setTime(now);
        cal.add(Calendar.MINUTE, purgeOffset);
        int min = cal.get(Calendar.MINUTE);
        if ((min % 15) >= 1) {
            cal.set(Calendar.MINUTE, ((min / 15) + 1) * 15);
            cal.set(Calendar.SECOND, 0);
        }
        expireDate = cal.getTime();
        dateTimeLbl.setText(expireLabelFmt.format(expireDate));

        if (regenerate) {
            // Add the updated time to the product data so it is available
            // for Product Generation
            updateSegmentExpirationTimes(expireDate, sel / 100.0);

            // regenerate with updated Product Expiration Time
            productDialog.regenerate(null);
        }
    }

    @SuppressWarnings("unchecked")
    private void updateSegmentExpirationTimes(Date expireDate,
            double purgeHours) {
        Map<String, Serializable> data = product.getData();
        List<Map<String, Serializable>> segments = (List<Map<String, Serializable>>) data
                .get(HazardConstants.SEGMENTS);
        data.put(HazardConstants.PURGE_HOURS, purgeHours);
        for (Map<String, Serializable> segment : segments) {
            segment.put(HazardConstants.EXPIRATION_TIME, expireDate.getTime());
        }
    }

    /**
     * Toggles the labels on the text fields contained in the Product Editor.
     */
    private void toggleLabels() {
        /*
         * Set the displayLabel flag on each KeyInfo object according to the
         * button state.
         */
        for (KeyInfo key : editableKeys.getKeyInfos()) {
            key.setDisplayLabel(toggleLabelsButton.getSelection());
            if (key.isRequired()) {
                /*
                 * Required labels should always be displayed.
                 */
                key.setDisplayLabel(true);
            }
        }

        // re-create the megawidgets with or without the labels
        createMegawidgets();
    }

    /**
     * Updates the value held in the keyinfo object
     * 
     * @param editableKeyInfo
     *            The editable key info
     * @param keyInfo
     *            The key info object
     * @param newValue
     *            The new value to assign to the keyinfo object
     */
    private void updateKeyInfoValue(EditableKeyInfo editableKeyInfo,
            KeyInfo keyInfo, Serializable newValue) {
        String format = editableKeyInfo.getFormat();
        for (EditableEntryMap map : product.getEditableEntries()) {
            if (map.getFormat().equals(format)) {
                map.getEditableEntries().put(keyInfo, newValue);
            }
        }
    }

    public void updateValues(IGeneratedProduct product) {
        editableKeys.updateEditableKeys(product);
        Map<String, Object> state = manager.getState();
        for (KeyInfo key : editableKeys.getKeyInfos()) {
            EditableKeyInfo editableKeyInfo = editableKeys
                    .getEditableKeyInfo(key);
            // updates the values displayed in the GUI
            state.put(key.toString(), editableKeyInfo.getOriginalValue());

        }

        try {
            manager.setState(state);
        } catch (MegawidgetStateException exception) {
            handler.error(
                    "Error trying to reset megawidget state: " + exception,
                    exception);
        }
    }

    @Override
    public void revertValues() {
        Map<String, Object> state = manager.getState();
        for (KeyInfo key : editableKeys.getKeyInfos()) {
            EditableKeyInfo editableKeyInfo = editableKeys
                    .getEditableKeyInfo(key);
            if (editableKeyInfo.isModified()) {
                editableKeyInfo.revertToOriginalValue();
                updateKeyInfoValue(editableKeyInfo, key,
                        editableKeyInfo.getOriginalValue());
                // updates the values displayed in the GUI
                state.put(key.toString(), editableKeyInfo.getOriginalValue());
            }

        }
        // regenerate with updated data
        productDialog.regenerate(null);
        try {
            manager.setState(state);
        } catch (MegawidgetStateException exception) {
            handler.error(
                    "Error trying to reset megawidget state: " + exception,
                    exception);
        }
    }

    @Override
    public void undoModification() {
        if (modificationHistory.isEmpty()) {
            String undoText = "No modifications to undo.";
            Shell shell = this.getDisplay().getActiveShell();
            MessageBox messageBox = new MessageBox(shell,
                    SWT.OK | SWT.ICON_INFORMATION);
            messageBox.setText("Unable to undo");
            messageBox.setMessage(undoText);
            messageBox.open();
            handler.info(undoText);
        } else {
            Map<String, Object> state = manager.getState();

            // Gets the previous state information
            Pair<KeyInfo, EditableKeyInfo> keyInfoPair = modificationHistory
                    .removeLast();

            // Retrieve the key info object
            KeyInfo keyInfo = keyInfoPair.getFirst();

            // Retrieve the editable key info object
            EditableKeyInfo editableKeyInfo = keyInfoPair.getSecond();

            // Update the current value with the previous value
            updateKeyInfoValue(editableKeyInfo, keyInfo,
                    editableKeyInfo.getLastValue());

            // Revert the editable key info object to the previous state
            editableKeyInfo.revertToLastValue();

            // updates the values displayed in the GUI
            state.put(keyInfo.toString(), editableKeyInfo.getValue());

            // Regenerate with the updated data
            productDialog.regenerate(null);

            // Update IssueAll button state
            productDialog.updateButtons();
            try {
                manager.setState(state);
            } catch (MegawidgetStateException exception) {
                handler.error(
                        "Error trying to reset megawidget state: " + exception,
                        exception);
            }
        }
    }

    @Override
    protected boolean undosRemaining() {
        return !modificationHistory.isEmpty();
    }

    @Override
    protected int getUndosRemaining() {
        return modificationHistory.size();
    }

    @Override
    public void refresh() {
        // no op
    }

    @Override
    public boolean isDataEditable() {
        return !editableKeys.isEmpty();
    }

    /**
     * Checks to see if there are any required fields that needs to be
     * completed.
     */

    @Override
    public boolean requiredFieldsCompleted() {
        for (KeyInfo keyInfo : editableKeys.getKeyInfos()) {
            Serializable value = editableKeys.getValue(keyInfo);
            if (keyInfo.isRequired() && (value == null
                    || String.valueOf(value).trim().length() == 0)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean hasUnsavedChanges() {
        return editableKeys.isModified();
    }

    @Override
    public void saveModifiedValues() {
        for (KeyInfo keyInfo : editableKeys.getKeyInfos()) {
            Serializable value = editableKeys.getModifiedValue(keyInfo, true);
            if (value != null) {
                String key = keyInfo.getName();

                ProductTextUtil.createOrUpdateProductText(key,
                        keyInfo.getProductCategory(), keyInfo.getProductID(),
                        keyInfo.getSegment(),
                        new ArrayList<>(keyInfo.getEventIDs()), siteId, value);
                if (key.startsWith(OVERVIEW_KEY_PREFIX)) {
                    /*
                     * Update the overview for staging values for each eventID.
                     * Staging dialog expects a quoted string.
                     */
                    String stagingValue = "\"" + value + "\"";
                    for (String eventId : keyInfo.getEventIDs()) {
                        ArrayList<String> list = new ArrayList<>(1);
                        list.add(eventId);

                        ProductTextUtil.createOrUpdateProductText(key, "", "",
                                "", list, siteId, stagingValue);
                    }
                }
            }
        }
        editableKeys.clearModifiedValues();
    }

    /**
     * Returns the last modified value
     * 
     * @return
     */
    public Serializable getLastModifiedValue() {
        Serializable value = null;
        for (KeyInfo keyInfo : editableKeys.getKeyInfos()) {
            Serializable modifiedValue = editableKeys.getModifiedValue(keyInfo,
                    false);
            if (modifiedValue != null) {
                value = modifiedValue;
            }
        }
        return value;
    }

    /**
     * Retrieves the number of buttons present on the editor GUI. The default
     * value is 3.
     * 
     * @return The number of buttons present on the button composite
     */
    @Override
    protected int getButtonCount() {
        return BUTTON_COUNT;
    }

    /**
     * Updates the text on the undo button.
     */
    @Override
    protected void updateButtonState() {
        // Update the undo button with how many undo actions are available
        if (undosRemaining()) {
            undoButton.setText(
                    UNDO_BUTTON_LABEL + "(" + getUndosRemaining() + ")");
        } else {
            undoButton.setText(UNDO_BUTTON_LABEL);
        }
    }

    /**
     * Returns a List (of Labels) of all the required fields that have not been
     * completed. If all the required fields are completed it returns a empty
     * List.
     */
    private List<String> getIncompleteRequiredFields() {
        List<String> incompleteFields = new ArrayList<>(
                editableKeys.getKeyInfos().size());
        for (KeyInfo keyInfo : editableKeys.getKeyInfos()) {
            String value = String.valueOf(editableKeys.getValue(keyInfo));
            if (keyInfo.isRequired()
                    && (value == null || value.trim().length() == 0)) {
                incompleteFields.add(keyInfo.getLabel());
            }
        }
        return incompleteFields;
    }
}

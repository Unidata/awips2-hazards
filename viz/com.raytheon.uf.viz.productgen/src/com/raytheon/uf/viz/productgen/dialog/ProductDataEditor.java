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
import java.util.Map.Entry;

import org.apache.commons.lang.NotImplementedException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;

import com.raytheon.uf.common.dataplugin.events.IEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardEventUtilities;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IReadableHazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.undoable.IUndoRedoManager;
import com.raytheon.uf.common.dataplugin.events.hazards.undoable.IUndoRedoable;
import com.raytheon.uf.common.hazards.configuration.types.HatchingStyle;
import com.raytheon.uf.common.hazards.configuration.types.HazardTypeEntry;
import com.raytheon.uf.common.hazards.configuration.types.HazardTypes;
import com.raytheon.uf.common.hazards.productgen.IGeneratedProduct;
import com.raytheon.uf.common.hazards.productgen.KeyInfo;
import com.raytheon.uf.common.hazards.productgen.ProductPart;
import com.raytheon.uf.common.hazards.productgen.editable.ProductTextUtil;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.SimulatedTime;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.viz.core.VizApp;
import com.raytheon.viz.core.mode.CAVEMode;

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
 * Jun 08, 2016 9620       Robert.Blum  Added controls to change the product expiration time.
 * Jun 09, 2016 9620       Robert.Blum  Fixed issue with rapidly changing the product purge hours.
 * Jun 21, 2016 9620       Robert.Blum  Accounting for arrow keys with expiration spinner.
 * Jun 22, 2016 19925      Thomas.Gurney Make "Toggle Labels" always checked by default
 * Sep 29, 2016 22602      Roger.Ferrel Disable mouse wheel on expires spinner.
 * Oct 18, 2016 22412      Roger.Ferrel When overview changes update the staging value entries.
 *                                      Set the overview synopsis to the value in product.
 * Nov 08, 2016 22509      bkowal       Required fields should always be indicated.
 * Nov 10, 2016 22119      Kevin.Bisanz Add siteId so that saved/issued changes can be tagged with it.
 * Dec 06, 2016 26855      Chris.Golden Removed explicit use of scrollable composite, since the
 *                                      megawidgets can be wrapped within a scrollable Composite
 *                                      megawidget instead, which will handle Label-wrapping behavior
 *                                      more correctly.
 * Dec 12, 2016 21504      Robert.Blum  Added method to set the enabled state of the Save button.
 * Jan 10, 2017 28024      Robert.Blum  Added a method to set the editable state of the fields/megawidget manager.
 * Feb 23, 2017 29170      Robert.Blum  Product Editor refactor.
 * Feb 27, 2017 29170      Robert.Blum  Additional changes for Product Editor refactor.
 * Mar 30, 2017 32569      Robert.Blum  Bolding and coloring blue Labels that divide product segments.
 * Apr 03, 2017 32572      Roger.Ferrel Added {@link #getIncompleteRequiredFields()}.
 * Apr 10, 2017 32735      Robert.Blum  Wrapping the uneditable Labels on Product Editor.
 * Jun 05, 2017 29996      Robert.Blum  Updates for previous text design.
 * Jun 12, 2017 35022      Kevin.Bisanz ProductText value changed from Serializable to String.
 * Dec 17, 2017 20739      Chris.Golden Refactored away access to directly mutable session events.
 * Jun 06, 2018 15561      Chris.Golden Made typecasting to floats safer (in case the object is a Number
 *                                      but not a Float).
 * </pre>
 * 
 * @author jsanchez
 * @version 1.0
 */

public class ProductDataEditor extends AbstractDataEditor
        implements IUndoRedoManager {

    /** The log handler */
    private static final IUFStatusHandler handler = UFStatus
            .getHandler(ProductDataEditor.class);

    /** Label used for the tab */
    private static final String TAB_LABEL = "Hazard Data Editor";

    /** Label for the Save button on the editor tab */
    private static final String SAVE_BUTTON_LABEL = "Save";

    /** Label for the Undo button on the editor tab */
    private static final String UNDO_BUTTON_LABEL = "Undo";

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

    /** The save button widget */
    private Button saveButton;

    /** The undo button widget */
    private Button undoButton;

    /** The scrolled composite for the text components **/
    private ScrolledComposite scrollerComposite;

    /** The parent composite for the text components **/
    private Composite parentComposite;

    private ProductPartsManager partsManager;

    /** Maps UI controls to their corresponding Product Part */
    private Map<Control, ProductPart> textWidgetMap = new HashMap<>();

    /** The history of modifications made to editable data. */
    private final LinkedList<IUndoRedoable> modificationHistory = new LinkedList<>();

    /** The current site ID */
    private String siteId;

    /** Bold Font used for Segment Divider Parts */
    private Font boldFont;

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
        partsManager = new ProductPartsManager(product.getEditableEntries());
        for (IEvent event : product.getEventSet()) {
            IReadableHazardEvent hazard = (IReadableHazardEvent) event;
            HazardTypeEntry hazardTypeEntry = hazardTypes
                    .get(HazardEventUtilities.getHazardType(hazard));
            if ((hazardTypeEntry != null) && (hazardTypeEntry
                    .getHatchingStyle() == HatchingStyle.WARNGEN)) {
                displayExpirationControls = false;
                break;
            }
        }

        addDisposeListener(new DisposeListener() {

            @Override
            public void widgetDisposed(DisposeEvent e) {
                if (boldFont != null && boldFont.isDisposed() == false) {
                    boldFont.dispose();
                }
            }
        });
    }

    /**
     * Creates the product specific GUI components.
     * 
     * @param parent
     *            The parent composite to create the GUI components
     */
    @Override
    protected void initializeSubclass() {

        // Set the tab label
        setText(TAB_LABEL);

        // Create the scroller composite and the layouts
        scrollerComposite = new ScrolledComposite(editorPane,
                SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        GridLayout gl = new GridLayout(1, false);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        scrollerComposite.setLayoutData(gd);
        scrollerComposite.setLayout(gl);

        parentComposite = new Composite(scrollerComposite, SWT.NONE);
        gl = new GridLayout(1, false);
        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        parentComposite.setLayout(gl);
        parentComposite.setLayoutData(gd);
        scrollerComposite.setExpandHorizontal(true);
        scrollerComposite.setExpandVertical(true);

        scrollerComposite.addControlListener(new ControlAdapter() {
            @Override
            public void controlResized(ControlEvent e) {
                Point size = parentComposite.computeSize(SWT.DEFAULT,
                        SWT.DEFAULT);
                scrollerComposite.setMinSize(size);
            }
        });

        scrollerComposite.addListener(SWT.MouseVerticalWheel, new Listener() {
            @Override
            public void handleEvent(Event event) {
                event.doit = false;
            }
        });
        scrollerComposite.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseScrolled(MouseEvent e) {
                Point origin = scrollerComposite.getOrigin();
                origin.y += 20 * (e.count < 0 ? 1 : -1);
                int maxOrigin = scrollerComposite.getContent()
                        .getBounds().height
                        - scrollerComposite.getClientArea().height;
                if (origin.y < 0) {
                    origin.y = 0;
                } else if (origin.y > maxOrigin) {
                    origin.y = maxOrigin;
                }
                scrollerComposite.setOrigin(origin);
            }
        });

        // Create the componenets
        createTextComponents();

        if (isDataEditable() == false) {
            handler.info(
                    "There are no editable fields. The data editor cannot be created.");
            return;
        }

        scrollerComposite.setContent(parentComposite);
    }

    private void createTextComponents() {
        List<ProductPart> editableEntries = product.getEditableEntries();
        if (editableEntries != null) {
            for (ProductPart part : editableEntries) {
                /*
                 * If the Product Part is editable or displayable then a SWT
                 * Control will be created.
                 */
                String value = part.getProductText();
                /*
                 * If the ProductPart is editable, create a TextComponent to
                 * allow the user the ability to edit/modify the text.
                 */
                Control control = null;
                if (part.isEditable()) {
                    control = new ProductEditorTextComponent(parentComposite,
                            this, part.getName(), value, part.getDisplayLabel(),
                            this, part.isRequired(), part.getNumLines(),
                            part.isUsePreviousText());
                } else {
                    // Not editable, add the text as a Label widget.
                    control = new Label(parentComposite, SWT.WRAP);

                    /*
                     * Check if the Product Part was configured as a
                     * "segmentDivider". If so, it needs to be bold and blue.
                     */
                    if (part.isSegmentDivider()) {
                        if (boldFont == null || boldFont.isDisposed()) {
                            FontData fontData = control.getFont()
                                    .getFontData()[0];
                            boldFont = new Font(control.getDisplay(),
                                    new FontData(fontData.getName(),
                                            fontData.getHeight(), SWT.BOLD));
                        }
                        control.setFont(boldFont);
                        control.setForeground(Display.getCurrent()
                                .getSystemColor(SWT.COLOR_BLUE));
                    }
                    GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
                    GC gc = new GC(control);
                    gc.setFont(control.getFont());
                    FontMetrics fm = gc.getFontMetrics();

                    gd.widthHint = fm.getAverageCharWidth()
                            * HazardConstants.LEGACY_TEXT_WRAP_LIMIT;
                    gc.dispose();
                    gd.horizontalIndent = 4;
                    control.setLayoutData(gd);
                    ((Label) control).setText(value);
                    control.setData(part);
                }
                textWidgetMap.put(control, part);
            }
        }
    }

    /**
     * Creates the Save and Undo buttons
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
                    List<ProductPart> incompleteFields = getIncompleteRequiredFields();
                    messageSB.append(
                            "\n\nThe following required fields are incomplete:\n");
                    for (ProductPart part : incompleteFields) {
                        messageSB.append(part.getLabel());
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
                undo();
            }
        });

        // Editor Undo button is always enabled.
        undoButton.setEnabled(true);
    }

    private double getDefaultPurgeHours() {
        Map<String, Serializable> data = product.getData();
        if (data.containsKey(HazardConstants.PURGE_HOURS)) {
            Number purgeHours = (Number) data.get(HazardConstants.PURGE_HOURS);
            if (purgeHours != null) {
                return purgeHours.doubleValue();
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
        Calendar cal = TimeUtil.newGmtCalendar(now);
        expireLabelFmt.setTimeZone(cal.getTimeZone());
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
            partsManager.clearModifiedProductParts();
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

    @Override
    public void refresh() {
        // no op
    }

    @Override
    public boolean isDataEditable() {
        return !this.textWidgetMap.isEmpty();
    }

    @Override
    public boolean requiredFieldsCompleted() {
        return partsManager.requiredFieldsCompleted();
    }

    @Override
    public boolean hasUnsavedChanges() {
        return partsManager.hasUnsavedChanges();
    }

    @Override
    public void saveModifiedValues() {
        String mode = CAVEMode.getMode().toString();
        for (ProductPart productPart : partsManager.getProductParts()) {
            if (partsManager.hasUnsavedChanges(productPart)) {
                partsManager.updateProductPartForSave(productPart);
                Control control = getControlForPart(productPart);
                if (control instanceof ProductEditorTextComponent) {
                    ((ProductEditorTextComponent) control).updateUseSavedText();
                }
                KeyInfo keyInfo = productPart.getKeyInfo();
                String value = productPart.getCurrentText();
                // Don't set productID due to pil changing
                ProductTextUtil.createOrUpdateProductText(keyInfo.getName(),
                        keyInfo.getProductCategory(), mode,
                        keyInfo.getSegment(),
                        new ArrayList<>(keyInfo.getEventIDs()), siteId, value);
            }
            partsManager.clearModified(productPart);
        }

        // Regenerate the product on saves
        productDialog.regenerate(partsManager.getModifiedProductParts());
        partsManager.clearModifiedProductParts();
    }

    private Control getControlForPart(ProductPart productPart) {
        for (Entry<Control, ProductPart> entry : textWidgetMap.entrySet()) {
            if (entry.getValue().equals(productPart)) {
                return entry.getKey();
            }
        }
        return null;
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
        if (isUndoable()) {
            undoButton.setText(
                    UNDO_BUTTON_LABEL + "(" + getUndosRemaining() + ")");
        } else {
            undoButton.setText(UNDO_BUTTON_LABEL);
        }
    }

    @Override
    public List<ProductPart> getIncompleteRequiredFields() {
        return partsManager.getIncompleteRequiredFields();
    }

    /**
     * Sets the enabled state of the save button.
     * 
     * @param enabled
     */
    public void setSaveButtonState(boolean enabled) {
        saveButton.setEnabled(enabled);
    }

    @Override
    public boolean undo() {
        if (isUndoable()) {
            IUndoRedoable undoable = modificationHistory.removeLast();
            undoable.undo();
            if (undoable instanceof ProductEditorTextComponent) {
                ProductEditorTextComponent peTextComp = (ProductEditorTextComponent) undoable;
                ProductPart productPart = textWidgetMap.get(peTextComp);
                /*
                 * Previous value was set in the undo call above, update
                 * corresponding data structures to match.
                 */
                String newValue = peTextComp.getText();
                partsManager.setPrevValue(productPart, newValue);
                partsManager.updateValue(productPart, newValue);
            }

            updateButtonState();
            updateTabLabel();

            // Update IssueAll button state
            productDialog.updateButtons();
            return true;
        } else {
            String undoText = "No modifications to undo.";
            Shell shell = this.getDisplay().getActiveShell();
            MessageBox messageBox = new MessageBox(shell,
                    SWT.OK | SWT.ICON_INFORMATION);
            messageBox.setText("Unable to undo");
            messageBox.setMessage(undoText);
            messageBox.open();
            handler.info(undoText);
            return false;
        }
    }

    @Override
    public boolean isUndoable() {
        return !modificationHistory.isEmpty();
    }

    @Override
    public void addUndo(IUndoRedoable undo) {
        modificationHistory.add(undo);
    }

    @Override
    protected int getUndosRemaining() {
        return modificationHistory.size();
    }

    @Override
    public boolean isRedoable() {
        return false;
    }

    @Override
    public boolean redo() {
        // Not implemented, this functionality has not been requested.
        throw new NotImplementedException();
    }

    @Override
    public void addRedo(IUndoRedoable redo) {
        // Not implemented, this functionality has not been requested.
        throw new NotImplementedException();
    }

    @Override
    public void clearUndoRedo() {
        modificationHistory.clear();
    }

    /**
     * Clears the list of modified Product Parts.
     */
    public void clearModifiedProductParts() {
        partsManager.clearModifiedProductParts();
    }

    /**
     * Gets the list of Product Parts in this Editor.
     * 
     * @return
     */
    public List<ProductPart> getProductParts() {
        return partsManager.getProductParts();
    }

    /**
     * Gets the list of modified Product Parts in this Editor.
     * 
     * @return
     */
    public List<ProductPart> getModifiedProductParts() {
        return partsManager.getModifiedProductParts();
    }

    /**
     * Sets the previous value on the corresponding Product Part for the widget
     * specified.
     * 
     * @param widget
     * @param currentValue
     */
    public void setPrevValue(ProductEditorTextComponent widget,
            String currentValue) {
        partsManager.setPrevValue(textWidgetMap.get(widget), currentValue);
    }

    /**
     * Sets the current value on the corresponding Product Part for the widget
     * specified.
     * 
     * @param widget
     * @param currentValue
     */
    public void updateValue(ProductEditorTextComponent widget,
            String currentValue) {
        ProductPart part = textWidgetMap.get(widget);
        int index = product.getEditableEntries().indexOf(part);
        ProductPart tempPart = product.getEditableEntries().get(index);
        tempPart.setCurrentText(currentValue);
        partsManager.updateValue(textWidgetMap.get(widget), currentValue);
    }

    /**
     * Sets the usePreviousText state on the corresponding Product Part for the
     * widget specified.
     * 
     * @param widget
     * @param selected
     */
    public void setUsePreviousText(ProductEditorTextComponent widget,
            boolean selected) {
        textWidgetMap.get(widget).setUsePreviousText(selected);
    }

    /**
     * Gets the saved text for the specified widget.
     * 
     * @param widget
     * @return
     */
    public String getSavedText(ProductEditorTextComponent widget) {
        return textWidgetMap.get(widget).getPreviousText();
    }

    /**
     * Gets the generated text for the specified widget.
     * 
     * @param widget
     * @return
     */
    public String getGeneratedText(ProductEditorTextComponent widget) {
        return textWidgetMap.get(widget).getGeneratedText();

    }

    /**
     * Returns whether or not the product needs regenerated based on this
     * editors content.
     * 
     * @return
     */
    public boolean needsRegenerated() {
        return partsManager.hasModifiedParts();
    }
}

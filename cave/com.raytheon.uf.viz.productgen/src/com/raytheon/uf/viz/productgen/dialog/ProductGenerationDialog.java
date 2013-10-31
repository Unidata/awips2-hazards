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

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.raytheon.uf.common.dataplugin.events.EventSet;
import com.raytheon.uf.common.dataplugin.events.IEvent;
import com.raytheon.uf.common.hazards.productgen.IGeneratedProduct;
import com.raytheon.uf.common.hazards.productgen.ITextProduct;
import com.raytheon.uf.common.hazards.productgen.ProductGeneration;
import com.raytheon.uf.common.hazards.productgen.ProductUtils;
import com.raytheon.uf.common.python.concurrent.IPythonJobListener;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.core.VizApp;
import com.raytheon.uf.viz.productgen.dialog.formats.AbstractFormatTab;
import com.raytheon.uf.viz.productgen.dialog.formats.TextFormatTab;
import com.raytheon.uf.viz.productgen.widgetcreation.ProductEditorComposite;
import com.raytheon.viz.ui.dialogs.CaveSWTDialogBase;

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
 * 
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */
/*
 * TODO work needs to be done to clean up this class. For example, once JSON
 * goes away some of this becomes much simpler. We might be able to put off some
 * of the events directly to the event bus as well, which would make things much
 * easier as well. I would like to look into the invokers as well, but those
 * were necessary at this point to work with the current system.
 */
public class ProductGenerationDialog extends CaveSWTDialogBase {

    private static final IUFStatusHandler handler = UFStatus
            .getHandler(ProductGenerationDialog.class);

    /*
     * An arbitrary button width to make sure they are all equal.
     */
    private static final int BUTTON_WIDTH = 80;

    /*
     * The tabs to be manipulated when product generation reruns.
     */
    private List<String> formatTabs = new ArrayList<String>();

    private Multimap<String, CTabItem> productTabs = ArrayListMultimap.create();

    /*
     * The progress bar to display that the formatting is being done currently.
     */
    private ProgressBar bar;

    /*
     * The data to be shown and modified on the left hand side of the dialog.
     */
    private Map<String, Serializable> data;

    /*
     * The products to be shown on the right hand side of the dialog.
     */
    private List<IGeneratedProduct> products;

    /*
     * The formatListener that handles all the work in product generation, as
     * well as sets up the progress bar and calls the method to repopulate the
     * formats
     */
    public final Listener formatListener = createListener();

    /*
     * The events to be generating products for
     */
    private EventSet<IEvent> events;

    /*
     * The format CTabFolder (right hand side)
     */
    private CTabFolder formatFolder;

    /*
     * For use to get values out, we need to know what has been put on the
     * composite. This could be done differently, but this is a little easier to
     * understand.
     */
    private Map<String, Map<String, ProductEditorComposite>> compositeMap = new LinkedHashMap<String, Map<String, ProductEditorComposite>>();

    private Composite editingFormatsComp;

    private Composite editingDataComp;

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

    /**
     * @param parentShell
     */
    public ProductGenerationDialog(Shell parentShell) {
        super(parentShell, SWT.RESIZE);
        setText("Product Editor");
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
    private void createFormatEditor(Composite comp, IGeneratedProduct product) {
        formatFolder = new CTabFolder(comp, SWT.BORDER);
        setLayoutInfo(formatFolder, 1, false, SWT.FILL, SWT.FILL, true, true,
                null);

        buildFormatTabs(product);
        formatFolder.setSelection(0);
    }

    /**
     * Builds a tab for each product that is set. Internally this calls into
     * methods to create the underlying format editor and data editor (right and
     * left sides).
     * 
     * @param comp
     */
    private void buildProductTabs(Composite comp) {
        CTabFolder folder = new CTabFolder(comp, SWT.BORDER);
        bar = new ProgressBar(folder, SWT.INDETERMINATE);
        bar.setVisible(false);
        folder.setTopRight(bar);
        setLayoutInfo(folder, 1, false, SWT.FILL, SWT.FILL, true, true, null);
        if (products != null) {
            for (IGeneratedProduct product : products) {
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
                if (countEditables() > 0) {
                    setLayoutInfo(sashForm, 2, true, SWT.FILL, SWT.FILL, true,
                            true, null);
                    ScrolledComposite scrolledComp = new ScrolledComposite(
                            sashForm, SWT.V_SCROLL | SWT.H_SCROLL);
                    setLayoutInfo(scrolledComp, 1, false, SWT.FILL, SWT.FILL,
                            true, true, null);

                    Composite leftComp = new Composite(scrolledComp, SWT.BORDER);
                    setLayoutInfo(leftComp, 1, false, SWT.FILL, SWT.NONE, true,
                            false, null);
                    addInput(leftComp, product.getProductID());
                    leftComp.pack();

                    scrolledComp.setBackground(Display.getCurrent()
                            .getSystemColor(SWT.COLOR_YELLOW));
                    scrolledComp.setExpandHorizontal(true);
                    scrolledComp.setExpandVertical(true);
                    scrolledComp.setContent(leftComp);
                    scrolledComp.setMinSize(leftComp.computeSize(SWT.DEFAULT,
                            SWT.DEFAULT));
                } else {
                    setLayoutInfo(sashForm, 1, true, SWT.FILL, SWT.FILL, true,
                            true, null);
                }

                Composite rightComp = new Composite(sashForm, SWT.NONE);
                setLayoutInfo(rightComp, 1, false, SWT.FILL, SWT.FILL, true,
                        true, null);

                createFormatEditor(rightComp, product);
                item.setControl(sashForm);
                productTabs.put(productId, item);
            }
        }
    }

    /**
     * Builds the format tabs, if the tab already exists it will take and
     * replace that one.
     * 
     * @param product
     */
    private void buildFormatTabs(IGeneratedProduct product) {
        Set<String> formats = product.getEntries().keySet();
        String productId = product.getProductID();

        formatTabs.add(productId);

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
                        new Point(400, 400));

                formatItem.setControl(editorComp);
                tab = new TextFormatTab();
                ((TextFormatTab) tab).setText(text);
                tab.setTabItem(formatItem);
            }
            if (product instanceof ITextProduct) {
                String finalProduct = ((ITextProduct) product).getText(format);
                // TODO FIXME XXX this should be in the formatter
                if ("XML".equals(format) || "CAP".equals(format)) {
                    finalProduct = ProductUtils.prettyXML(finalProduct);
                } else if ("Legacy".equals(format)) {
                    // temporary, in theory we should have no knowledge of
                    // format type in this class
                    tab.getTabItem().setShowClose(false);
                    finalProduct = ProductUtils.wrapLegacy(finalProduct);
                }
                ((TextFormatTab) tab).getText().setText(finalProduct);
            }
        }
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
        createGenerateButton(buttonComp);
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
        issueButton.setText("Issue");
        setButtonGridData(issueButton);
        issueButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                /*
                 * TODO XXX maybe move this to the actual issue method, if it is
                 * in viz, that way we don't have to do it in a few different
                 * places?
                 */
                // boolean answer = MessageDialog.openQuestion(getShell(),
                // "Product Editor",
                // "Are you sure you want to issue this product?");
                // if (answer) {
                // TODO would rather do this a little better, and not pass
                // around String
                issueHandler.commandInvoked("Issue");
                close();
                // }
            }
        });
    }

    private void createGenerateButton(Composite buttonComp) {
        Button generateButton = new Button(buttonComp, SWT.PUSH);
        generateButton.setText("Generate");
        setButtonGridData(generateButton);
        generateButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                formatListener.handleEvent(null);
            }
        });
        generateButton.setEnabled(false);
    }

    /**
     * The save button will save the edits made to the database.
     * 
     * @param buttonComp
     */
    private void createSaveButton(Composite buttonComp) {
        Button saveButton = new Button(buttonComp, SWT.PUSH);
        saveButton.setText("Save");
        setButtonGridData(saveButton);
        saveButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (Entry<String, Map<String, ProductEditorComposite>> entry : compositeMap
                        .entrySet()) {
                    for (Entry<String, ProductEditorComposite> compEntry : entry
                            .getValue().entrySet()) {
                        System.out.println(entry.getKey() + "/"
                                + compEntry.getKey() + ": "
                                + compEntry.getValue().getValue());
                        // TODO, fill in key information here for save.
                        // ProductTextUtil.createOrUpdateProductText(entry.getKey(),
                        // "", "", "", "", entry.getValue().getValue());
                    }
                }
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
        cancelButton.setText("Dismiss");
        setButtonGridData(cancelButton);
        cancelButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                // TODO should reevaluate why we need a String passed in here
                dismissHandler.commandInvoked("Dismiss");
            }
        });
    }

    /**
     * Method for ease of use to make all the button sizes the same.
     * 
     * @param button
     */
    private void setButtonGridData(Button button) {
        GridData data = new GridData(SWT.NONE, SWT.NONE, false, false);
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

    /**
     * Method that the data will get passed in for use in the editable column
     * (the left).
     * 
     * @param comp
     * @param data
     */
    private void addInput(Composite comp, String productId) {
        for (Entry<String, Serializable> entry : data.entrySet()) {
            Composite pieceComp = new Composite(comp, SWT.NONE);
            ProductEditorComposite composite = addFields(pieceComp, productId,
                    entry.getKey(), entry.getValue());
            if (compositeMap.containsKey(productId) == false) {
                compositeMap.put(productId,
                        new HashMap<String, ProductEditorComposite>());
            }
            compositeMap.get(productId).put(entry.getKey(), composite);
        }
    }

    /**
     * Adds fields to the left hand side
     * 
     * @param comp
     * @param productId
     * @param key
     * @param value
     * @param size
     * @param count
     */
    private ProductEditorComposite addFields(Composite comp, String productId,
            String key, Serializable value) {
        Composite pieceComp = new Composite(comp, SWT.NONE);
        setLayoutInfo(pieceComp, 1, false, SWT.FILL, SWT.DEFAULT, true, false,
                null);
        ProductEditorComposite composite = new ProductEditorComposite(
                pieceComp, key, value, formatListener);
        Label separator = new Label(comp, SWT.HORIZONTAL | SWT.SEPARATOR);
        separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        return composite;
    }

    /**
     * Method to return the number of editable fields.
     * 
     * @return
     */
    private int countEditables() {
        int count = 0;
        if (data != null) {
            for (String key : data.keySet()) {
                if (key.contains("editable")) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Parses the "editable" part of the key out to give the "pretty" text
     * 
     * @param key
     * @return
     */
    public static String parseEditable(String key) {
        String returnKey = key;
        if (key.contains("editable")) {
            returnKey = key.substring(0, key.indexOf("editable") - 1);
        }
        return returnKey;
    }

    /**
     * Creates the reformat listener that will update the UI to notify the user
     * that formatting is/has happened.
     * 
     * @return
     */
    private Listener createListener() {
        return new Listener() {

            @Override
            public void handleEvent(Event event) {
                bar.setVisible(true);
                ProductGeneration generation = new ProductGeneration();
                // this listener needs to be updated to rerun only the
                // formatting
                final IPythonJobListener<List<IGeneratedProduct>> listener = new IPythonJobListener<List<IGeneratedProduct>>() {

                    @Override
                    public void jobFinished(final List<IGeneratedProduct> result) {
                        final List<String> productIds = new ArrayList<String>();
                        for (IGeneratedProduct product : result) {
                            productIds.add(product.getProductID());
                        }

                        // remove any unneeded format tabs after running
                        VizApp.runAsync(new Runnable() {
                            public void run() {
                                for (String key : productTabs.keySet()) {
                                    if (productIds.contains(key) == false) {
                                        for (CTabItem item : productTabs
                                                .get(key)) {
                                            item.dispose();
                                        }
                                    }
                                }
                                productTabs.keySet().retainAll(productIds);

                                for (IGeneratedProduct product : result) {
                                    buildFormatTabs(product);
                                }
                                bar.setVisible(false);
                            };
                        });
                    }

                    @Override
                    public void jobFailed(Throwable e) {
                        handler.error("Unable to run product generation", e);
                        bar.setVisible(false);
                    }
                };
                // generation.generate("", events, new String[] { "XML",
                // "Legacy",
                // "CAP" }, listener);
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
    public void setProducts(List<IGeneratedProduct> products) {
        this.products = products;
    }

    /**
     * Data needs to be set before the dialog pops up to allow for editing of
     * products. If there are no editable fields, then we should probably only
     * have a right hand side of the dialog.
     * 
     * @param data
     */
    public void setData(Map<String, Serializable> data) {
        this.data = data;
    }

    // TODO, these should be changed to better handle non-JSON and determine if
    // this data is necessary in the actual dialog

    /**
     * @return the generated products
     */
    public List<IGeneratedProduct> getGeneratedProducts() {
        return products;
    }

    /**
     * 
     * @return the events
     */
    public EventSet<IEvent> getEvents() {
        return events;
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

    public static void main(String[] args) {
        // ProductGenerationDialog dialog = new ProductGenerationDialog(
        // new Shell());
        // List<IGeneratedProduct> products = new
        // ArrayList<IGeneratedProduct>();
        // GeneratedProduct product = new GeneratedProduct("FFW");
        // product.addEntry("Legacy", new ArrayList<Object>());
        // product.addEntry("XML", new ArrayList<Object>());
        // product.addEntry("CAP", new ArrayList<Object>());
        // GeneratedProduct product2 = new GeneratedProduct("FFA");
        // product2.addEntry("Legacy", new ArrayList<Object>());
        // product2.addEntry("XML", new ArrayList<Object>());
        // product2.addEntry("CAP", new ArrayList<Object>());
        // products.add(product);
        // products.add(product2);
        // Map<String, Serializable> data = new LinkedHashMap<String,
        // Serializable>();
        // // data.put("Date:editable", new Date());
        // ArrayList<String> cities = new ArrayList<String>();
        // cities.add("Lincoln");
        // cities.add("Omaha");
        // cities.add("Papillion");
        // data.put("Cities:editable", cities);
        // data.put(
        // "Call to Action:editable",
        // "Tommy used to work on the docks, Union's been on strike,He's down on his luck...it's tough, so tough,Gina works the diner all day,Working for her man, she brings home her pay,For love - for love,She says we've got to hold on to what we've got,'Cause it doesn't make a difference,If we make it or not,We've got each other and that's a lot,For love - we'll give it a shot,Whooah, we're half way there,Livin' on a prayer");
        // // dialog.setData(data);
        // dialog.setProducts(products);
        // dialog.open();
    }
}
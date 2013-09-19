package com.raytheon.uf.viz.productgen.dialog;

import gov.noaa.gsd.viz.mvp.widgets.ICommandInvocationHandler;
import gov.noaa.gsd.viz.mvp.widgets.ICommandInvoker;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.raytheon.uf.common.dataplugin.events.EventSet;
import com.raytheon.uf.common.dataplugin.events.IEvent;
import com.raytheon.uf.common.hazards.productgen.IGeneratedProduct;
import com.raytheon.uf.common.hazards.productgen.ITextProduct;
import com.raytheon.uf.common.hazards.productgen.ProductUtils;
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
public class ProductGenerationDialog extends CaveSWTDialog {

    private final Font boldFont;

    private final Color BLACK = Display.getCurrent().getSystemColor(
            SWT.COLOR_BLACK);

    private List<IGeneratedProduct> products;

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
     * @param parentShell
     */
    public ProductGenerationDialog(Shell parentShell) {
        super(parentShell, SWT.RESIZE);
        setText("Product Editor");
        FontData data = Display.getCurrent().getSystemFont().getFontData()[0];
        boldFont = new Font(Display.getCurrent(), data.getName(),
                data.getHeight(), SWT.BOLD);
    }

    public void setProducts(List<IGeneratedProduct> products) {
        this.products = products;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.viz.ui.dialogs.CaveSWTDialogBase#disposed()
     */
    @Override
    protected void disposed() {
        if (boldFont != null && boldFont.isDisposed() == false) {
            boldFont.dispose();
        }
        super.disposed();
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

        CTabFolder folder = new CTabFolder(fullComp, SWT.NONE);
        setLayoutInfo(folder, 1, false, SWT.FILL, SWT.FILL, true, true, null);

        for (int i = 0; i < products.size(); i++) {
            CTabItem item = new CTabItem(folder, SWT.NONE);
            item.setText(products.get(i).getProductID());

            /*
             * TODO XXX eventually this will be a SashForm as we will have two
             * separate sides, one for viewing and one for editing
             */
            Composite comp = new Composite(folder, SWT.NONE);
            setLayoutInfo(comp, 1, false, SWT.FILL, SWT.FILL, true, true, null);

            Composite rightComp = new Composite(comp, SWT.NONE);
            setLayoutInfo(rightComp, 1, false, SWT.FILL, SWT.FILL, true, true,
                    null);

            createFormatEditor(rightComp, products.get(i));
            item.setControl(comp);
        }
        createButtonComp(fullComp);
    }

    private void createFormatEditor(Composite comp, IGeneratedProduct product) {
        CTabFolder formatFolder = new CTabFolder(comp, SWT.BORDER);
        setLayoutInfo(formatFolder, 1, false, SWT.FILL, SWT.FILL, true, true,
                null);

        Set<String> formats = product.getEntries().keySet();

        for (String format : formats) {
            Composite editorComp = new Composite(formatFolder, SWT.NONE);
            setLayoutInfo(editorComp, 1, false, SWT.FILL, SWT.FILL, true, true,
                    null);

            CTabItem formatItem = new CTabItem(formatFolder, SWT.BORDER);
            formatItem.setText(format);

            if (product instanceof ITextProduct) {
                StyledText text = new StyledText(editorComp, SWT.H_SCROLL
                        | SWT.V_SCROLL | SWT.READ_ONLY);
                text.setWordWrap(false);
                text.setAlwaysShowScrollBars(false);
                setLayoutInfo(text, 1, false, SWT.FILL, SWT.FILL, true, true,
                        new Point(600, 400));
                String finalProduct = ((ITextProduct) product).getText(format);
                // TODO FIXME XXX this should be in the formatter
                if ("XML".equals(format) || "CAP".equals(format)) {
                    finalProduct = ProductUtils.prettyXML(finalProduct);
                } else if ("Legacy".equals(format)) {
                    // temporary, in theory we should have no knowledge of
                    // format type in this class
                    formatItem.setShowClose(false);
                    finalProduct = ProductUtils.wrapLegacy(finalProduct);
                }
                text.setText(finalProduct);
            }

            formatItem.setControl(editorComp);
        }
        formatFolder.setSelection(0);
    }

    private void createButtonComp(Composite comp) {
        Composite buttonComp = new Composite(comp, SWT.NONE);
        GridLayout layout = new GridLayout(3, true);
        GridData data = new GridData(SWT.CENTER, SWT.CENTER, true, false);
        buttonComp.setLayout(layout);
        buttonComp.setLayoutData(data);
        createIssueButton(buttonComp);
        // add back in when user edited text is supported
        Button saveButton = new Button(buttonComp, SWT.PUSH);
        saveButton.setText("Save");
        saveButton.setEnabled(false);
        createDismissButton(buttonComp);
    }

    private void createIssueButton(Composite buttonComp) {
        Button issueButton = new Button(buttonComp, SWT.PUSH);
        issueButton.setText("Issue");
        issueButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                // TODO XXX maybe move this to the actual issue method, if it is
                // in
                // viz, that way we don't have to do it in a few different
                // places?
                // boolean answer = MessageDialog.openQuestion(getShell(),
                // "Product Editor",
                // "Are you sure you want to issue this product?");
                // if (answer) {
                // // TODO would rather do this a little better, and not pass
                // // around String
                issueHandler.commandInvoked("Issue");
                close();
                // }
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
        cancelButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                // TODO should reevaluate why we need a String passed in here
                dismissHandler.commandInvoked("Dismiss");
            }
        });
    }

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
     * TODO XXX Methods below here are not currently called, but will be in
     * place for the future when we make the product generation dialog have
     * editable fields.
     */

    /**
     * Method that the data will get passed in for use in the editable column
     * (the left).
     * 
     * @param comp
     * @param data
     */
    public void setInput(Composite comp, Serializable... data) {
    }

    /**
     * Method contains how the data shows up in the left column.
     * 
     * @param folder
     * @return
     */
    private Composite createDataTab(CTabFolder folder) {
        final SashForm verticalSashForm = new SashForm(folder, SWT.VERTICAL);
        verticalSashForm.setBackground(BLACK);
        // setLayoutData(verticalSashForm, 1);
        return verticalSashForm;
    }

    private Composite addLabel(Composite comp, String text) {
        Composite individualComp = new Composite(comp, SWT.NONE);
        // setLayoutData(individualComp, 1);

        Label label = new Label(individualComp, SWT.NONE);
        label.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
        label.setText(text);
        label.setFont(boldFont);
        return individualComp;
    }

    // TODO, these should be changed to better handle non-JSON and determine if
    // this data is necessary in the actual dialog

    // TODO, the Issue Invoker seems difficult to use, plus it needs
    // modifications to make it work with objects

    // TODO, for utilization of product generation in the future, product
    // generation should be separate from hazards. Because of this, Dict cannot
    // be used due to circular dependencies. I am not sure how to create a Dict
    // from IGeneratedProducts or from an EventSet. This will go into the first
    // part of PV3 as those should go away anyway (at least from this code, we
    // shouldn't have anything but the actual objects).
    /**
     * @return the generatedProductsDictList
     */
    public List<IGeneratedProduct> getGeneratedProductsDictList() {
        return null;
    }

    /**
     * @return the hazardEventSetsList
     */
    public EventSet<IEvent> getHazardEventSetsList() {
        return null;
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
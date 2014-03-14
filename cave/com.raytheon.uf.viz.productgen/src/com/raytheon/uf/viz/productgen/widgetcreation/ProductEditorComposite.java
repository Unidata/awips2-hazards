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
package com.raytheon.uf.viz.productgen.widgetcreation;

import java.io.Serializable;
import java.util.InputMismatchException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

import com.raytheon.uf.viz.productgen.dialog.ProductGenerationDialogUtility;
import com.raytheon.uf.viz.productgen.widgetcreation.datatypes.IProductEditable;

/**
 * The composite that decides how to render data types
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 7, 2013            mnash     Initial creation
 * Feb 18, 2014 2702      jsanchez  Used parseEditable method from ProductGenerationDialogUtility.
 * 
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */

public class ProductEditorComposite extends Composite {

    /*
     * Bold font for use in the field label
     */
    private static Font BOLD_FONT;

    private IProductEditable<Serializable, Control> editable;

    private Control control;

    private Listener listener;

    public ProductEditorComposite(Composite parent, String key,
            Serializable data, Listener listener) {
        super(parent, SWT.NONE);
        if (BOLD_FONT == null || BOLD_FONT.isDisposed()) {
            // get the current font data and make a corresponding bold font
            FontData fontData = Display.getCurrent().getSystemFont()
                    .getFontData()[0];
            BOLD_FONT = new Font(Display.getCurrent(), fontData.getName(),
                    fontData.getHeight(), SWT.BOLD);
        }
        setData(data);
        setData("key", key);
        this.listener = listener;
        init();
    }

    public void setData(Serializable data) {
        if (data instanceof Serializable == false) {
            throw new InputMismatchException(
                    "Unable to use any data that is not of type Serializable");
        }
        super.setData(data);
    }

    private void init() {
        Serializable data = (Serializable) getData();
        buildLabel((String) getData("key"));
        buildComposite(data);
    }

    private void buildComposite(Serializable data) {
        editable = WidgetCreationRegistry.getInstance(listener)
                .getProductEditable(data);
        if (editable != null) {
            editable.setKey(ProductGenerationDialogUtility
                    .parseEditable((String) getData("key")));
            control = editable.getWidget(data, this);
            editable.addListeners(control);
        }
        return;
    }

    public Serializable getValue() {
        return editable.getData(control);
    }

    /**
     * Adds a bold label that defines which field is being edited, just above
     * the widget itself.
     * 
     * @param comp
     * @param text
     */
    private void buildLabel(String text) {
        Label label = new Label(getParent(), SWT.NONE);
        GridData gridData = new GridData(SWT.FILL, SWT.NONE, true, false);
        label.setLayoutData(gridData);

        label.setText(ProductGenerationDialogUtility.parseEditable(text));
        label.setFont(BOLD_FONT);
        label.moveAbove(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.swt.widgets.Widget#dispose()
     */
    @Override
    public void dispose() {
        if (BOLD_FONT != null && BOLD_FONT.isDisposed() == false) {
            BOLD_FONT.dispose();
        }
        super.dispose();
    }
}

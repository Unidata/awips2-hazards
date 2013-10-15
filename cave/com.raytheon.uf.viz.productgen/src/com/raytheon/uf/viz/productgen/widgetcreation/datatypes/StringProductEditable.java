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
package com.raytheon.uf.viz.productgen.widgetcreation.datatypes;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ST;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import com.raytheon.uf.viz.productgen.widgetcreation.ProductEditorComposite;

/**
 * Builds a list based on a {@link String} value.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 3, 2013            mnash     Initial creation
 * 
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */

public class StringProductEditable extends
        AbstractProductEditable<String, StyledText> {

    /**
     * @param formatListener
     */
    public StringProductEditable(Listener listener) {
        super(listener);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.viz.productgen.datatypes.IProductEditable#getComposite
     * (java.io.Serializable)
     */
    @Override
    public StyledText getWidget(String object,
            final ProductEditorComposite parent) {
        setLayoutInfo(parent, 2, false, SWT.FILL, SWT.NONE, true, false);

        final StyledText text = new StyledText(parent, SWT.MULTI | SWT.BORDER
                | SWT.WRAP | SWT.V_SCROLL);
        text.setAlwaysShowScrollBars(false);
        Menu menu = new Menu(text);
        MenuItem copyItem = createItem(menu, "Copy",
                ISharedImages.IMG_TOOL_COPY);
        copyItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                text.invokeAction(ST.COPY);
            }
        });

        MenuItem cutItem = createItem(menu, "Cut", ISharedImages.IMG_TOOL_CUT);
        cutItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                text.invokeAction(ST.CUT);
            }
        });

        MenuItem pasteItem = createItem(menu, "Paste",
                ISharedImages.IMG_TOOL_PASTE);
        pasteItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                text.invokeAction(ST.PASTE);
            }
        });
        MenuItem selectAll = createItem(menu, "Select All", null);
        selectAll.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                text.invokeAction(ST.SELECT_ALL);
            }
        });

        text.setMenu(menu);
        String string = (String) object;
        GridData gridData = new GridData(SWT.FILL, SWT.NONE, true, false);
        gridData.widthHint = 200;
        text.setLayoutData(gridData);
        text.setText(string);

        return text;
    }

    private MenuItem createItem(Menu menu, String itemName, String icon) {
        MenuItem item = new MenuItem(menu, SWT.PUSH);
        item.setText(itemName);
        if (icon != null) {
            try {
                item.setImage(PlatformUI.getWorkbench().getSharedImages()
                        .getImage(icon));
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
        return item;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.viz.productgen.datatypes.IProductEditable#getData(java
     * .io.Serializable)
     */
    @Override
    public String getData(StyledText text) {
        return text.getText();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.viz.productgen.datatypes.IProductEditable#addListener
     * (org.eclipse.swt.widgets.Control)
     */
    @Override
    public void addListeners(final StyledText control) {
        control.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                fireListener(control.getParent());
                super.focusLost(e);
            }
        });
    }

}

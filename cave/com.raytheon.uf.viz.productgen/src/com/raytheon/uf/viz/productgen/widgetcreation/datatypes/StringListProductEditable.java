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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;

import com.raytheon.uf.viz.productgen.widgetcreation.ProductEditorComposite;

/**
 * Builds a widget based on an {@link ArrayList} of values.
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

public class StringListProductEditable extends
        AbstractProductEditable<ArrayList<Serializable>, Composite> {

    /**
     */
    public StringListProductEditable(Listener listener) {
        super(listener);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.viz.productgen.datatypes.IProductEditable#getWidget(java
     * .io.Serializable, org.eclipse.swt.widgets.Composite)
     */
    @Override
    public Composite getWidget(ArrayList<Serializable> object,
            final ProductEditorComposite parent) {
        setLayoutInfo(parent, 1, false, SWT.FILL, SWT.NONE, true, false);
        java.util.List<?> list = (ArrayList<?>) object;

        if (list.size() > 0) {

            // we need to assume that all elements are the same
            final List swtList = new List(parent, SWT.MULTI | SWT.BORDER
                    | SWT.V_SCROLL | SWT.H_SCROLL);
            GridData gridData = new GridData(SWT.FILL, SWT.NONE, true, false);
            gridData.heightHint = 80;
            swtList.setLayoutData(gridData);
            swtList.setItems(list.toArray(new String[0]));

            Composite insideComp = new Composite(parent, SWT.NONE);
            setLayoutInfo(insideComp, 3, false, SWT.FILL, SWT.NONE, true, false);
            Button addButton = new Button(insideComp, SWT.PUSH);
            addButton.setText("Add...");
            gridData = new GridData(SWT.NONE, SWT.NONE, false, false);
            addButton.setLayoutData(gridData);

            Button upButton = new Button(insideComp, SWT.ARROW | SWT.UP);
            gridData = new GridData(SWT.NONE, SWT.NONE, false, false);
            upButton.setLayoutData(gridData);
            Button downButton = new Button(insideComp, SWT.ARROW | SWT.DOWN);
            gridData = new GridData(SWT.NONE, SWT.NONE, false, false);
            downButton.setLayoutData(gridData);
            return parent;
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.viz.productgen.datatypes.IProductEditable#getData(java
     * .io.Serializable)
     */
    @Override
    public ArrayList<Serializable> getData(Composite control) {
        // we need to assume that all elements are the same
        List list = (List) control.getChildren()[0];
        ArrayList<Serializable> arrayList = new ArrayList<Serializable>();
        arrayList.addAll(Arrays.asList(list.getItems()));
        return arrayList;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.viz.productgen.datatypes.IProductEditable#addListener
     * (org.eclipse.swt.widgets.Control)
     */
    @Override
    public void addListeners(final Composite control) {
        // first up the list
        control.getChildren()[0].addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                fireListener(control);
            }
        });
        Composite insideComp = (Composite) control.getChildren()[1];
        // then the buttons
        ((Button) insideComp.getChildren()[0])
                .addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        InputDialog dialog = new InputDialog(
                                control.getShell(), "Add Items to "
                                        + StringListProductEditable.this
                                                .getKey(), "", "", null);
                        dialog.open();

                        List list = (List) control.getChildren()[0];
                        String val = dialog.getValue();
                        if (dialog.getReturnCode() == 0 && val != null
                                && val.isEmpty() == false) {
                            list.add(val);
                            fireListener(control);
                        }
                    }
                });

        // up button
        ((Button) insideComp.getChildren()[1])
                .addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        List list = (List) control.getChildren()[0];
                        if (list.getSelection().length > 0) {
                            move(true, list);
                        }
                    }
                });

        // down button
        ((Button) insideComp.getChildren()[2])
                .addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        List list = (List) control.getChildren()[0];
                        if (list.getSelection().length > 0) {
                            move(false, list);
                        }
                    }
                });
    }

    /**
     * Move the selected items in a list.
     * 
     * @param up
     *            Flag indicating the direction. True is up, false is down.
     */
    private void move(boolean up, List itemList) {
        if (itemList.getSelectionCount() <= 0) {
            return;
        }

        int[] selIdxArray = itemList.getSelectionIndices();
        boolean[] selBoolArray = new boolean[itemList.getItemCount()];
        Arrays.fill(selBoolArray, false);
        for (int i : selIdxArray) {
            selBoolArray[i] = true;
        }

        if (up) {
            for (int i = 1; i < selBoolArray.length; i++) {
                if (selBoolArray[i] == true && selBoolArray[i - 1] == false) {
                    selBoolArray[i] = false;
                    selBoolArray[i - 1] = true;
                    String str = itemList.getItem(i - 1);
                    itemList.remove(i - 1);
                    itemList.add(str, i);
                }
            }
        } else {
            for (int i = selBoolArray.length - 1; i > 0; i--) {
                if (selBoolArray[i] == false && selBoolArray[i - 1] == true) {
                    selBoolArray[i] = true;
                    selBoolArray[i - 1] = false;
                    String str = itemList.getItem(i - 1);
                    itemList.remove(i - 1);
                    itemList.add(str, i);
                }
            }
        }

        int count = 0;
        for (int i = 0; i < selBoolArray.length; i++) {
            if (selBoolArray[i] == true) {
                selIdxArray[count] = i;
                ++count;
            }
        }

        itemList.deselectAll();
        itemList.select(selIdxArray);
        itemList.showSelection();
    }
}

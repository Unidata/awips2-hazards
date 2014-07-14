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
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Spinner;

import com.raytheon.uf.viz.productgen.widgetcreation.ProductEditorComposite;

/**
 * Builds a list based on a {@link Number}
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 8, 2013            mnash     Initial creation
 * 
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */

public class NumberProductEditable extends
        AbstractProductEditable<Number, Spinner> {

    private Number number;

    /**
     */
    public NumberProductEditable(Listener listener) {
        super(listener);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.viz.productgen.widgetcreation.datatypes.IProductEditable
     * #getWidget(java.io.Serializable,
     * com.raytheon.uf.viz.productgen.widgetcreation.ProductEditorComposite)
     */
    @Override
    public Spinner getWidget(Number object, ProductEditorComposite parent) {
        number = object;
        setLayoutInfo(parent, 1, false, SWT.FILL, SWT.NONE, true, false);
        Spinner spinner = new Spinner(parent, SWT.BORDER);
        spinner.setDigits((object instanceof Float || object instanceof Double) ? 1
                : 0);
        spinner.setMaximum(Integer.MAX_VALUE);
        spinner.setMinimum(Integer.MIN_VALUE);
        spinner.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                // fireListener();
            }
        });
        return spinner;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.viz.productgen.widgetcreation.datatypes.IProductEditable
     * #getData(org.eclipse.swt.widgets.Control)
     */
    @Override
    public Number getData(Spinner spinner) {
        if (number instanceof Float) {
            number = Float.valueOf(spinner.getText());
        } else if (number instanceof Integer) {
            number = Integer.valueOf(spinner.getText());
        } else if (number instanceof Double) {
            number = Double.valueOf(spinner.getText());
        } else {
            throw new NumberFormatException("Number type "
                    + number.getClass().getName()
                    + " is not currently supported");
        }
        return number;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.viz.productgen.widgetcreation.datatypes.IProductEditable
     * #addListener(org.eclipse.swt.widgets.Control)
     */
    @Override
    public void addListeners(final Spinner control) {
        control.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                fireListener(control.getParent());
                super.focusLost(e);
            }
        });
        control.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                fireListener(control.getParent());
            }
        });
    }
}
